 /*
 * Session.java            $Revision: 1.8 $ $Date: 2001/04/26 18:34:06 $
  *
  * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
  *
  * The contents of this file are subject to the Blocks Public License (the
  * "License"); You may not use this file except in compliance with the License.
  *
  * You may obtain a copy of the License at http://www.invisible.net/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  */
 package org.beepcore.beep.core;
 
 
 import java.io.IOException;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Collection;
 import java.util.LinkedList;
 
 import javax.xml.parsers.*;
 
 import org.w3c.dom.*;
 
 import org.xml.sax.SAXException;
 
 import org.beepcore.beep.util.Log;
 
 
 /**
  * This class encapsulates the notion of a BEEP Session (a relationship
  * between BEEP peers).
  * <p>
  * The implementor should sub-class <code>Session</code>'s abstract methods
  * for a given transport.
  * It's principal function is to sit on whatever network or referential
  * 'connection' exists between the BEEP peers, read and write BEEP frames and
  * deliver them to or receive them from the associated <code>Channel</code>.
  *
  * @todo Improvments could include sharing buffers from DataStream to here,
  * minimizing stream writes/reads, and pooling I/O threads.
  *
  * @author Eric Dixon
  * @author Huston Franklin
  * @author Jay Kint
  * @author Scott Pead
 * @version $Revision: 1.8 $, $Date: 2001/04/26 18:34:06 $
  *
  * @see Channel
  */
 public abstract class Session {
 
     // Constants
     public static final int SESSION_STATE_UNINITIALIZED = 0;
     public static final int SESSION_STATE_INITIALIZED = 1;
     public static final int SESSION_STATE_GREETING_SENT = 2;
     public static final int SESSION_STATE_GREETING_RECEIVED = 4;
     public static final int SESSION_STATE_ACTIVE = 7;
     public static final int SESSION_STATE_TUNING = 7;
     public static final int SESSION_STATE_CLOSING = 15;
     public static final int SESSION_STATE_TERMINATING = 16;
     public static final int SESSION_STATE_CLOSED = 8;
 
     private static final String CORE = "core";
     private static final int DEFAULT_CHANNELS_SIZE = 4;
     private static final int DEFAULT_PROPERTIES_SIZE = 4;
     private static final int DEFAULT_POLL_INTERVAL = 500;
 
     /** @todo check this */
     private static final int MAX_PAYLOAD_SIZE = 4096;
     private static final int MAX_FRAME_SIZE = Frame.MAX_HEADER_SIZE
     + MAX_PAYLOAD_SIZE + Frame.TRAILER.length();
     private static final String ERR_BEEP_START_CHANNEL_TIMEOUT =
         "Channel start timed out.";
     private static final String ERR_GREETING_FAILED =
         "Greeting exchange failed";
     private static final String ERR_ILLEGAL_SHUTDOWN =
         "Illegal state for shutdown";
     private static final String ERR_MSG_PROFILES_UNAVAILABLE =
         "<error code='550'>all requested profiles are unsupported</error>";
     private static final String ERR_NONEXISTENT_CHANNEL =
         "Session call on nonexistent channel.";
     private static final String ERR_STATE_CHANGE =
         "Illegal session state transition";
     private static final String ERR_MALFORMED_XML_MSG = "Malformed XML";
     private static final String ERR_PCDATA_TOO_BIG_MSG =
         "Element's PCDATA exceeds the maximum size";
     private static final String ERR_UNKNOWN_OPERATION_ELEMENT_MSG =
         "Unknown operation element";
     private static final String ERR_MALFORMED_PROFILE_MSG =
         "Malformed profile";
 
     // Instance Data
     private int state;
     private long messageNumber;
     private long nextChannelNumber = 0;
     private Channel zero;
     private ChannelZeroListener zeroListener;
     private Hashtable channels = null;
     private Hashtable properties = null;
     private Hashtable eventTable = null;
     private ProfileRegistry profileRegistry = null;
     private SessionCredential localCredential, peerCredential;
     private Collection peerSupportedProfiles;
     private boolean overflow;
     private boolean allowChannelWindowUpdates;
     private DocumentBuilder builder;    // generic XML parser
 
 
     /**
      * Default Session Constructor.  A relationship between peers - a session -
      * consists of a set of profiles they share in common, and an ordinality
      * (to prevent new channel collision) so that the initiator starts odd
      * channels and the listener starts channels with even numbers.
      * @param The Profile Registry summarizing the profiles this Session will
      *        support
      * @param cred
      *
      * @param registry
      * @param firstChannel used internally in the API, an indication of the
      *        ordinality of the channels this peer can start, odd, or even.
      */
     protected Session(ProfileRegistry registry, int firstChannel,
                       SessionCredential localCred,
                       SessionCredential peerCred)
         throws BEEPException
     {
         state = SESSION_STATE_UNINITIALIZED;
         allowChannelWindowUpdates = true;
         localCredential = localCred;
         peerCredential = peerCred;
         nextChannelNumber = firstChannel;
         overflow = false;
         profileRegistry = registry;
         channels = new Hashtable(DEFAULT_CHANNELS_SIZE);
         eventTable = new Hashtable(DEFAULT_CHANNELS_SIZE);
         properties = new Hashtable(DEFAULT_PROPERTIES_SIZE);
 
         try {
             builder =
                 DocumentBuilderFactory.newInstance().newDocumentBuilder();
         } catch (ParserConfigurationException e) {
 
             throw new BEEPException("Invalid parser configuration");
         }
 
         // set starting channel number according to
         // listener or initiator (odd or even)
         changeState(SESSION_STATE_INITIALIZED);
     }
 
     /**
      * Initializes the <code>Session</code>.  Initializes Channel Zero and its
      * listener. Sends a greeting and waits for corresponding greeting.
      *
      * @throws BEEPException
      */
     protected void init() throws BEEPException
     {
         this.peerSupportedProfiles = null;
         GreetingListener greetingListener = new GreetingListener();
         zero = new Channel(this, greetingListener);
 
         zeroListener = new ChannelZeroListener();
         zero.setDataListener(zeroListener);
         channels.put(Constants.CHANNEL_ZERO, zero);
 
         // send greeting
         sendGreeting();
         changeState(Session.SESSION_STATE_GREETING_SENT);
 
         // start our listening thread we can now receive a greeting
         this.enableIO();
 
         // blocks until greeting is received or MAX_GREETING_WAIT is reached
         int waitCount = 0;
 
         while ((state < SESSION_STATE_ACTIVE)
                && (waitCount < Constants.MAX_START_CHANNEL_WAIT)) {
             try {
                 synchronized (greetingListener) {
 
                     //zero.wait(MAX_START_CHANNEL_INTERVAL);
                     greetingListener.wait(0);
 
                     waitCount += Constants.MAX_START_CHANNEL_INTERVAL;
                 }
             } catch (InterruptedException e) {
                 waitCount += Constants.MAX_START_CHANNEL_INTERVAL;
             }
         }
 
         // check the channel state and return the appropriate exception
         if (zero.getState() == Channel.STATE_ERROR) {
             throw new BEEPException(ERR_GREETING_FAILED);
         }
     }
 
     /**
      * A reentrant version of init() that doesn't block the
      * first I/O thread waiting on a greeting when it should die
      * and go away.
      *
      * @throws BEEPException
      */
     protected void tuningInit() throws BEEPException
     {
         Log.logEntry(Log.SEV_DEBUG, CORE, "Session.tuningInit");
 
         this.peerSupportedProfiles = null;
         GreetingListener greetingListener = new GreetingListener();
         zeroListener = new ChannelZeroListener();
         zero = new Channel(this, greetingListener);
 
         zero.setDataListener(zeroListener);
         channels.put(Constants.CHANNEL_ZERO, zero);
 
         // send greeting
         sendGreeting();
         changeState(Session.SESSION_STATE_GREETING_SENT);
 
         // start our listening thread we can now receive a greeting
         this.enableIO();
     }
 
     /**
      * Peer-level call to simply close a session down, no questions
      * asked.
      * @todo we need to do a "niceShutdown" or something like that which
      * will accomodate Darren's needs by considering the results (exceptions)
      * from the CCL close channel callbacks instead of simply ignoring them.
      *
      * @throws BEEPException
      */
     public void close() throws BEEPException
     {
         Log.logEntry(Log.SEV_DEBUG,
                      "Closing Session with " + channels.size() + " channels");
 
         //        changeState(SESSION_STATE_CLOSING);
 
         Iterator i = channels.values().iterator();
 
         while (i.hasNext()) {
             Channel ch = (Channel) i.next();
 
             // if this channel is not zero, call the channel's scl
             if (ch.getNumber() == 0) {
                 continue;
             }
 
             StartChannelListener scl =
                 profileRegistry.getStartChannelListener(ch.getProfile());
 
            if (scl == null) {
                continue;
            }

             // check locally first to see if it is ok to close the channel
             try {
                 scl.closeChannel(ch);
             } catch (CloseChannelException cce) {
                 throw new BEEPException("Close Session rejected by local "
                                         + "channel " + ch.getProfile());
             }
         }
 
         // check with the peer to see if it is ok to close the channel
         zero.close();
         shutdown();
     }
 
     /**
      * Get the local <code>SessionCredential</code> for this session.
      *
      *
      * @return May return <code>null</code> if this session has not
      *         been authenticated
      * */
     public SessionCredential getLocalCredential()
     {
         return localCredential;
     }
 
     /**
      * Get our peer's <code>SessionCredential</code> for this session.
      *
      *
      * @return May return <code>null</code> if this session has not
      *         been authenticated
      * */
     public SessionCredential getPeerCredential()
     {
         return peerCredential;
     }
 
     /**
      * Returns the profiles sent by the remote peer in the greeting.
      */
     public Collection getPeerSupportedProfiles()
     {
         return this.peerSupportedProfiles;
     }
 
     /**
      * Returns the <code>ProfileRegistry</code> for <code>Session</code>.
      *
      * @return A <code>ProfileRegistry</code>.
      * @see ProfileRegistry
      */
     public ProfileRegistry getProfileRegistry()
     {
         return profileRegistry;
     }
 
     /**
      * Returns the state of <code>Session</code>.
      *
      * @return Session state (see the Constants in this class).
      */
     public int getState()
     {
         return this.state;
     }
 
     /**
      * Indicates whehter or not this session is in the initiator role.
      */
     public boolean isInitiator()
     {
         return ((nextChannelNumber % 2) == 1);
     }
 
     /**
      * Registers a <code>SessionEventListener</code> for various
      * <code>Session</code> events.
      *
      * @param sel A reference to an implementation of a
      * <code>SessionEventListener</code> that will be called back once
      * an event is fired.
      *
      * @param event The type of event (i.e.
      * <code>SessionEvent.CHANNEL_OPENED_EVENT_CODE</code>) for which the
      * <code>SessionEventListener</code> is registered.
      *
      * @see SessionEvent
      * @see SessionEventListener
      * @see #fireEvent
      */
     public void registerForEvent(SessionEventListener sel, int event)
     {
         Integer i = new Integer(event);
         LinkedList l = (LinkedList) eventTable.get(i);
 
         if (l == null) {
             l = new LinkedList();
         }
 
         l.add(sel);
         eventTable.put(i, l);
     }
 
     /**
      * Sends a start channel request using the specified profile.
      *
      * @param profile The uri of the profile for the channel you wish to start.
      *
      * @return A <code>Channel</code> for the specified profile.
      *
      * @throws BEEPError Thrown if an error occurs in the under lying transport.
      * @throws BEEPException Thrown if any of the parameters are invalid,
      * or if the profile is unavailable on this <code>Session</code>.
      */
     public Channel startChannel(String profile)
             throws BEEPException, BEEPError
     {
         return startChannel(profile, null);
     }
 
     /**
      * Sends a start channel request using the specified profile.
      *
      * @param profile The uri of the profile for the channel you wish to start.
      * @param listener An implementation of <code>MessageListener</code> that
      * is to receive message callbacks for this channel.  It can be null, but
      * don't expect to be called back.
      *
      * @return A <code>Channel</code> for the specified profile.
      *
      * @throws BEEPError Thrown if an error occurs in the under lying transport.
      * @throws BEEPException Thrown if any of the parameters are invalid,
      * or if the profile is unavailable on this <code>Session</code>.
      * @see DataListener
      */
     public Channel startChannel(String profile, DataListener listener)
             throws BEEPException, BEEPError
     {
         StartChannelProfile p = new StartChannelProfile(profile);
         LinkedList l = new LinkedList();
 
         l.add(p);
 
         return startChannel(l, listener, false);
     }
 
     /**
      * Sends a start channel request using the specified profile.
      *
      * @param profile The uri of the profile for the channel you wish to start.
      * @param base64Encoding Indicates whether or not the data is base64
      * encoded.
      * @param data The associated data or initial element for the profile of
      * the channel you wish to start.
      * @param listener An implementation of <code>MessageListener</code> that
      * is to receive message callbacks for this channel.  It can be null, but
      * don't expect to be called back.
      *
      * @return A <code>Channel<code> for the specified profile.
      *
      * @throws BEEPError Thrown if an error occurs in the under lying
      *         transport.
      * @throws BEEPException Thrown if any of the parameters are invalid,
      * or if the profile is unavailable on this <code>Session</code>.
      * @see DataListener
      */
     public Channel startChannel(String profile, boolean base64Encoding,
                                 String data, DataListener listener)
             throws BEEPException, BEEPError
     {
         StartChannelProfile p = new StartChannelProfile(profile,
                                                         base64Encoding, data);
         LinkedList l = new LinkedList();
 
         l.add(p);
 
         return startChannel(l, listener, false);
     }
 
     /**
      * Sends a start channel request using the given list of profiles.
      *
      * @param profiles A collection of <code>StartChannelProfile</code>(s).
      * @param listener An implementation of <code>MessageListener</code>
      * that is to receive message callbacks for this channel.
      * It can be null, but don't expect to be called back.
      *
      * @return a started channel for the profile selected by the listener
      *
      * @throws BEEPError Thrown if an error occurs in the under lying
      *         transport.
      * @throws BEEPException Thrown if any of the parameters are invalid,
      * or if the profile is unavailable on this <code>Session</code>.
      * @see StartChannelProfile
      * @see DataListener
      */
     public Channel startChannel(Collection profiles, DataListener listener)
         throws BEEPException, BEEPError
     {
         return startChannel(profiles, listener, false);
     }
 
     /**
      * You should not see this.
      */
     Channel startChannel(Collection profiles, DataListener listener,
                                 boolean disableIO) 
             throws BEEPException, BEEPError
     {
 
         // Block here if there's an exclusive lock, which
         // would change our channel #...
         String channelNumber = getNextFreeChannelNumber();
 
         // create the message in a buffer and send it (via a StringDataStream)
         StringBuffer startBuffer = new StringBuffer(Session.MAX_FRAME_SIZE);
 
         startBuffer.append(Constants.FRAGMENT_START_PREFIX);
         startBuffer.append(Constants.FRAGMENT_NUMBER_PREFIX);
         startBuffer.append(channelNumber);
         startBuffer.append(Constants.FRAGMENT_QUOTE_ANGLE_SUFFIX);
 
         Iterator i = profiles.iterator();
 
         while (i.hasNext()) {
             StartChannelProfile p = (StartChannelProfile) i.next();
 
             // @todo maybe we should check these against peerSupportedProfiles
             startBuffer.append(Constants.FRAGMENT_PROFILE_PREFIX);
             startBuffer.append(Constants.FRAGMENT_URI_PREFIX);
             startBuffer.append(p.uri);
             startBuffer.append(Constants.FRAGMENT_QUOTE_SUFFIX);
 
             if (p.data == null) {
                 startBuffer.append(Constants.FRAGMENT_SLASH_ANGLE_SUFFIX);
             } else {
                 if (p.base64Encoding) {
                     startBuffer.append("encoding='base64' ");
                 }
 
                 startBuffer.append(Constants.FRAGMENT_ANGLE_SUFFIX);
                 startBuffer.append(Constants.FRAGMENT_CDATA_PREFIX);
                 startBuffer.append(p.data);
                 startBuffer.append(Constants.FRAGMENT_CDATA_SUFFIX);
                 startBuffer.append(Constants.FRAGMENT_PROFILE_SUFFIX);
             }
         }
 
         startBuffer.append(Constants.FRAGMENT_START_SUFFIX);
 
         // @todo handle the data element
         // Create a channel
         Channel ch = new Channel(null, channelNumber, listener, this);
 
         // Make a message
         DataStream ds = null;
 
         ds = new StringDataStream(DataStream.BEEP_XML_CONTENT_TYPE,
                                   startBuffer.toString());
 
         // Tell Channel Zero to start us up
         this.zero.sendMSG(ds, new StartReplyListener(ch, disableIO));
         waitForResult(ch, Channel.STATE_UNINITIALISED);
         
         return ch;
     }
 
     /**
      * This method is used to terminate the session when there is an
      * non-recoverable error in the BEEP protocol (framing error, etc.).
      *
      *
      * @param reason
      *
      */
     public void terminate(String reason)
     {
         Log.logEntry(Log.SEV_ERROR, reason);
         try {
             this.changeState(SESSION_STATE_TERMINATING);
             shutdown();
         } catch (BEEPException e) {
             // Ignore this since we are terminating anyway.
         }
     }
 
     /**
      * Changes the state of the Session.
      *
      *
      * @param newState
      *
      * @return Returns <code>true</code> if the state changed was successful.
      * Otherwise, returns <code>false</code>.
      *
      */
     protected synchronized boolean changeState(int newState)
         throws BEEPException
     {
         if ((state == SESSION_STATE_UNINITIALIZED)
                 &&!((newState == SESSION_STATE_INITIALIZED)
                     || (newState == SESSION_STATE_CLOSED))) {
             throw new BEEPException(ERR_STATE_CHANGE);
         }
 
         if ((state == SESSION_STATE_INITIALIZED)
                 &&!((newState == SESSION_STATE_GREETING_SENT)
                     || (newState == SESSION_STATE_GREETING_RECEIVED)
                     || (newState == SESSION_STATE_CLOSED))) {
             throw new BEEPException(ERR_STATE_CHANGE);
         }
 
         if (state == SESSION_STATE_ACTIVE
             && newState != SESSION_STATE_CLOSED
             && newState != SESSION_STATE_TERMINATING
             && newState != SESSION_STATE_CLOSING) {
             throw new BEEPException(ERR_STATE_CHANGE);
         }
 
         state |= newState;
 
         Log.logEntry(Log.SEV_DEBUG, CORE, "State changed to " + state);
 
         return true;
     }
 
     /**
      * This method is intended for use by tranport specific Sessions to create
      * a new <code>Frame</code> object representing a BEEP MSG, RPY, ERR,
      * or NUL frames.
      *
      * @param messageType indicates whether a <code>Frame</code> is a MSG,
      *    RPY, ERR, ANS or NUL.
      * @param channelNum Channel on which the <code>Frame</code> was sent.
      * @param msgno Message number of the <code>Frame</code>.
      * @param seqno Sequence number of the <code>Frame</code>.
      * @param payload Payload of the <code>Frame</code>.
      * @param last  Indicates if this is the last <code>Frame</code> sent in a
      *    sequence of frames.
      *
      * @return a <code>Frame</code> for the specified values
      *
      *
      * @throws BEEPException
      */
     protected Frame createFrame(byte[] header, int headerLength)
             throws BEEPException
     {
         Frame f = Frame.parseHeader(this, header, headerLength);
 
         // The window size and frame size have nothing in common.
         if (f.getSize() > f.getChannel().getAvailableWindow()) {
             throw new BEEPException("Payload size is greater than channel "
                                     + "window size");
         }
 
         return f;
     }
 
     /**
      * Method disableIO
      *
      *
      */
     protected abstract void disableIO();
 
     /**
      * Method enableIO
      *
      *
      */
     protected abstract void enableIO();
 
     /**
      * Publish a session event to registered <code>SessionEventListener</code>s.
      *
      * @param event Event to be passed to <code>SessionEventListener</code>s.
      * @param arg Data associated with event.
      *
      * @see #registerForEvent
      * @see SessionEvent
      * @see SessionEventListener
      */
     protected void fireEvent(int event, Object arg)
     {
         Integer k = new Integer(event);
         LinkedList l = (LinkedList) eventTable.get(k);
 
         if (l == null) {
             return;
         }
 
         // @todo how do those who have registered to receive events call us
         // back?
         SessionEvent se = new SessionEvent(event, arg);
         Iterator i = l.iterator();
 
         while (i.hasNext()) {
             ((SessionEventListener) i.next()).receiveEvent(se);
         }
     }
 
     /**
      * Returns the channel's available window size.
      */
     protected int getChannelAvailableWindow(int channel) throws BEEPException
     {
         Channel ch = (Channel) channels.get(Integer.toString(channel));
 
         if (ch == null) {
             throw new BEEPException(ERR_NONEXISTENT_CHANNEL);
         }
 
         return ch.getAvailableWindow();
     }
 
     /**
      * Get the channel number as a String
      *
      *
      * @param channel
      *
      */
     protected String getChannelNumberAsString(Channel channel)
     {
         return channel.getNumberAsString();
     }
 
     /**
      * Returns the maximum frame size that a channel should send for
      * this session.
      *
      *
      * @throws BEEPException
      *
      */
     protected abstract int getMaxFrameSize() throws BEEPException;
 
     /**
      * Method postFrame
      *
      *
      * @param f
      * @param number
      *
      * @throws BEEPException
      *
      */
     protected void postFrame(Frame f) throws BEEPException
     {
         try {
             if (state == SESSION_STATE_ACTIVE) {
                 f.getChannel().postFrame(f);
 
                 // If we're in a PRE-GREETING state
                 // only handle one frame at a time...
                 // to avoid processing post-greeting
                 // frames before the greeting has been
                 // fully handled.
             } else if (state < SESSION_STATE_ACTIVE) {
                 synchronized (this) {
                     f.getChannel().postFrame(f);
                 }
             } else {
                 // If we're in an error state
                 Log.logEntry(Log.SEV_DEBUG,
                              "Dropping a frame because the Session state is no longer active.");
             }
         } catch (BEEPException e) {
             this.terminate(e.getMessage());
             return;
         } catch (Throwable e) {
             Log.logEntry(Log.SEV_ERROR, e);
             this.terminate("Uncaught exception, terminating session");
             return;
         }
     }
 
     /**
      * Method prevents Channel's window from being updated.
      *
      *
      */
     protected void prohibitChannelWindowUpdates()
     {
         allowChannelWindowUpdates = false;
     }
 
     /**
      * This method is used by a tuning profile to reset the session after the
      * tuning is complete.
      *
      *
      * @return A new <code>Session</code> with the tuning complete.
      *
      */
     protected abstract Session reset(SessionCredential localCred,
                                      SessionCredential peerCred,
                                      ProfileRegistry registry, Object argument)
         throws BEEPException;
 
     /**
      * Implement this method to send frames and on the sub-classed transport.
      *
      *
      * @param BEEP frame to send.
      *
      *
      * @throws BEEPException
      */
     protected abstract void sendFrame(Frame f) throws BEEPException;
 
     /**
      * Method setLocalCredential
      *
      *
      * @param cred
      *
      */
     protected void setLocalCredential(SessionCredential cred)
     {
         localCredential = cred;
     }
 
     /**
      * Method setPeerCredential
      *
      *
      * @param cred
      *
      */
     protected void setPeerCredential(SessionCredential cred)
     {
         peerCredential = cred;
     }
 
     /**
      * This method is designed to allow for flow control across the multiplexed
      * connection we have. <p> The idea is to throttle data being sent over
      * this session to be manageable per Channel, so that a given Channel
      * doesn't take up all the bandwidth. <p>
      * This method restricts the bufferSize, per the beep spec, to be at most
      * two-thirds of the socket's receiveBufferSize.  If a size is requested
      * beyond that, an exception is thrown.
      * @channel indicates the Channel this flow-control message is for.
      * @ackno is the number of bytes we've received on this Channel.
      * @newSize is the number of bytes we're able to read at this point in
      *    time.
      *
      *
      * @param channel
      * @param previouslySeq
      * @param currentSeq
      * @param previouslyUsed
      * @param currentlyUsed
      * @param bufferSize
      *
      * @return true if the Receive Buffer Size was updated
      *
      * @exception throws BEEPException if a specified buffer size is larger
      *    than what's available on the Socket.
      *
      * @throws BEEPException
      */
     protected abstract boolean updateMyReceiveBufferSize(Channel channel,
                                                          long previouslySeq,
                                                          long currentSeq,
                                                          int previouslyUsed,
                                                          int currentlyUsed,
                                                          int bufferSize)
         throws BEEPException;
       // @todo update the java-doc to correctly identify the params
 
     /**
      * Method updatePeerReceiveBufferSize
      *
      *
      *
      * @param channelNum
      * @param lastSeq
      * @param size
      *
      *
      * @throws BEEPException
      */
     protected void updatePeerReceiveBufferSize(int channelNum, long lastSeq,
                                                int size)
             throws BEEPException
     {
         Channel channel = getValidChannel(channelNum);
 
         channel.updatePeerReceiveBufferSize(lastSeq, size);
     }
 
     /**
      * The Initiator Oriented close channel call...but this one is not an
      * external call, it's invoked from Channel.close();
      *
      * @param channel
      * @param code
      * @param xmlLang
      *
      * @throws BEEPException
      */
     void closeChannel(Channel channel, int code, String xmlLang)
             throws BEEPException
     {
 
         // Construct Message
         StringBuffer closeBuffer = new StringBuffer(Session.MAX_FRAME_SIZE);
 
         closeBuffer.append(Constants.FRAGMENT_CLOSE_PREFIX);
         closeBuffer.append(Constants.FRAGMENT_NUMBER_PREFIX);
         closeBuffer.append(channel.getNumberAsString());
         closeBuffer.append(Constants.FRAGMENT_QUOTE_SUFFIX);
         closeBuffer.append(Constants.FRAGMENT_CODE_PREFIX);
         closeBuffer.append(code);
 
         if (xmlLang != null) {
             closeBuffer.append(Constants.FRAGMENT_QUOTE_SUFFIX);
             closeBuffer.append(Constants.FRAGMENT_XML_LANG_PREFIX);
             closeBuffer.append(xmlLang);
         }
 
         closeBuffer.append(Constants.FRAGMENT_QUOTE_SLASH_ANGLE_SUFFIX);
 
         // Make a message
         DataStream ds = new StringDataStream(DataStream.BEEP_XML_CONTENT_TYPE,
                                              closeBuffer.toString());
 
         // Lock necessary because we have to know the msgNo
         // before we send the message, in order to be able
         // to associate the reply with this start request
         this.zero.sendMSG(ds, new CloseReplyListener(channel));
         waitForResult(channel, Channel.STATE_OK);
     }
 
     Channel getValidChannel(int number) throws BEEPException
     {
         Channel ch = (Channel) channels.get(Integer.toString(number));
 
         if (ch == null) {
             throw new BEEPException(ERR_NONEXISTENT_CHANNEL);
         }
 
         return ch;
     }
 
     void sendProfile(String uri, String datum, Channel ch)
             throws BEEPException
     {
 
         // Send the profile
         StringBuffer sb = new StringBuffer();
 
         sb.append(Constants.FRAGMENT_PROFILE_PREFIX);
         sb.append(Constants.FRAGMENT_URI_PREFIX);
         sb.append(uri);
 
         if (datum != null) {
             sb.append(Constants.FRAGMENT_QUOTE_ANGLE_SUFFIX);
             sb.append(Constants.FRAGMENT_CDATA_PREFIX);
             sb.append(datum);
             sb.append(Constants.FRAGMENT_CDATA_SUFFIX);
             sb.append(Constants.FRAGMENT_PROFILE_SUFFIX);
         } else {
             sb.append(Constants.FRAGMENT_QUOTE_SLASH_ANGLE_SUFFIX);
         }
 
         StringDataStream sds =
             new StringDataStream(DataStream.BEEP_XML_CONTENT_TYPE,
                                  sb.toString());
 
         // Store the Channel
         ch.setState(Channel.STATE_OK);
         channels.put(ch.getNumberAsString(), ch);
         zero.sendRPY(sds);
     }
 
     /**
      * This method is called when Channel Zero receives - from our
      * session peer - a request to close a channel.
      *
      * @param channelNumber
      * @param code
      * @param xmlLang
      * @param data
      *
      * @throws BEEPException
      */
     private void receiveCloseChannel(String channelNumber, String code,
                                      String xmlLang, String data)
         throws BEEPError
     {
 
         // @todo fix close channel
         if (channelNumber.equals(Constants.CHANNEL_ZERO)) {
             receiveCloseChannelZero();
             return;
         }
 
         Channel channel = (Channel) channels.get(channelNumber);
 
         if (channel == null) {
             throw new BEEPError(BEEPError.CODE_PARAMETER_INVALID,
                                 "Close requested for nonexistent channel");
         }
 
         try {
             StartChannelListener scl =
                 profileRegistry.getStartChannelListener(channel.getProfile());
 
             scl.closeChannel(channel);
             channel.setState(Channel.STATE_CLOSING);
         } catch (BEEPError x) {
 
             channel.setState(Channel.STATE_CLOSING);
             throw x;
         }
 
         // Send an ok
         StringDataStream sds =
             new StringDataStream(DataStream.BEEP_XML_CONTENT_TYPE,
                                  Constants.FRAGMENT_OK);
 
         try {
             zero.sendRPY(sds);
         } catch (BEEPException x) {
             terminate("Error sending RPY for <close>");
             return;
         }
 
         // We're past the CCL approval
         channel.setState(Channel.STATE_CLOSED);
         channels.remove(channel.getNumberAsString());
     }
 
     private void receiveCloseChannelZero() throws BEEPError
     {
         // closing the session
         // @todo fireEvent(SESSION_STATE_CLOSING);
         /*
         try {
             if (!changeState(SESSION_STATE_CLOSING)) {
 
                 // @todo got consecutive shutdowns... now what... log it?
                 // Utility.assert(ERR_ILLEGAL_SHUTDOWN, -1);
             }
         } catch (BEEPException e) {
             throw new BEEPError(BEEPError.CODE_REQUESTED_ACTION_ABORTED,
                                 e.getMessage());
         }
         */
 
         Log.logEntry(Log.SEV_DEBUG,
                      "Closing Session with " + channels.size() + " channels");
 
         /*
         try {
             changeState(SESSION_STATE_CLOSING);
         } catch (BEEPException x) {
             terminate("Error changing Session state to closing.");
             return;
         }
         */
 
         Iterator i = channels.values().iterator();
 
         while (i.hasNext()) {
             Channel ch = (Channel) i.next();
 
             // if this channel is not zero, call the channel's scl
             if (ch.getNumber() == 0) {
                 continue;
             }
 
             StartChannelListener scl =
                 profileRegistry.getStartChannelListener(ch.getProfile());
 
             // check locally first to see if it is ok to close the channel
             try {
                 scl.closeChannel(ch);
                 i.remove();
             } catch (CloseChannelException e) {
                 try {
                     changeState(SESSION_STATE_ACTIVE);
 
                     throw e;
                 } catch (BEEPException x) {
                     terminate("Error changing Session state from closing " +
                               "to active");
                     return;
                 }
             }
         }
 
         StringDataStream sds =
             new StringDataStream(DataStream.BEEP_XML_CONTENT_TYPE,
                                  Constants.FRAGMENT_OK);
 
         try {
             zero.sendRPY(sds);
         } catch (BEEPException x) {
             terminate("Error sending RPY for <close> for channel 0");
             return;
         }
 
         this.disableIO();
 
         try {
             this.changeState(SESSION_STATE_CLOSED);
         } catch (BEEPException e) {
             Log.logEntry(Log.SEV_ERROR, e);
         }
 
         fireEvent(SessionEvent.SESSION_CLOSED_EVENT_CODE,this);
     }
 
     private void receiveStartChannelResultOk(Channel channel, String data)
             throws BEEPException
     {
 
         // set the state
         channel.setState(Channel.STATE_OK);
         channels.put(channel.getNumberAsString(), channel);
 
         // release the block waiting for the channel to start or close
 
         /**
          * @todo something with data
          */
         synchronized (channel) {
             channel.notify();
         }
 
         if (TuningProfile.isTuningProfile(channel.getProfile())) {
             Log.logEntry(Log.SEV_DEBUG, CORE, "\nDisabling this I/O thread");
             disableIO();
         }
     }
 
     /**
      * received an OK element for the channel close element
      *
      *
      * @param channel
      *
      * @throws BEEPException
      *
      */
     private void receiveCloseChannelResultOk(Channel channel)
         throws BEEPException
     {
 
         /**
          * @todo CCL
          */
         /*
         StartChannelListener scl =
             profileRegistry.getStartChannelListener(channel.getProfile());
 
         if (scl != null) {
             scl.closeChannel(channel);
         }
         */
         // @todo we should fire an event instead.
 
         // set the state
         channel.setState(Channel.STATE_CLOSING);
         channels.remove(channel.getNumberAsString());
         channel.setState(Channel.STATE_CLOSED);
 
         // release the block waiting for the channel to start or close
         synchronized (channel) {
             channel.notify();
         }
     }
 
     /**
      * Returns the next free channel number as a string.
      * @return Channel number.
      */
     private synchronized String getNextFreeChannelNumber()
     {
         long i;
 
         synchronized (this) {
 
             // next channel needs to be incremented by two since the peer
             // uses every other channel (see beep spec)
             i = nextChannelNumber;
             nextChannelNumber += 2;
         }
 
         String nextChannel = Long.toString(i);
 
         if (overflow) {
 
             // Equally insane collision check after the rollover
             if (channels.get(nextChannel) != null) {
                 return getNextFreeChannelNumber();
             }
         }
 
         // Insane bounds check that will probably never happen
         if (nextChannelNumber > Constants.MAX_CHANNEL_NUMBER) {
             nextChannelNumber = nextChannelNumber
                                 % Constants.MAX_CHANNEL_NUMBER;
             overflow = true;
         }
 
         // Warning: nextChannelNumber is a long to detect overflow
         return nextChannel;
     }
 
     /**
      *  Listener oriented Start Channel call, a call here means that
      *  we've received a start channel request over the wire.
      */
     private boolean processStartChannel(String channelNumber,
                                         Collection profiles)
             throws BEEPError
     {
         StartChannelListener scl;
         Channel ch = null;
         Iterator i = profiles.iterator();
 
         while (i.hasNext()) {
             StartChannelProfile p = (StartChannelProfile) i.next();
 
             scl = profileRegistry.getStartChannelListener(p.uri);
 
             if (scl == null) {
                 continue;
             }
 
             ch = new Channel(p.uri, channelNumber, null, this);
 
             try {
                 String encoding = p.base64Encoding ? "base64" : "none";
 
                 scl.startChannel(ch, encoding, p.data);
             } catch (TuningResetException e) {
                 Log.logEntry(Log.SEV_DEBUG, CORE,
                              "Leaving profile response to Tuning Profile CCL");
 
                 return true;
             } catch (StartChannelException e) {
                 try {
                     zero.sendERR(new StringDataStream(DataStream.BEEP_XML_CONTENT_TYPE,
                                                       e.createErrorMessage()));
                 } catch (BEEPException x) {
                     terminate("Error sending ERR response to start channel");
                 }
 
                 return false;
             }
 
             try {
                 sendProfile(p.uri, ch.getStartData(), ch);
             } catch (BEEPException e) {
                 terminate("Error sending profile. " + e.getMessage());
                 return false;
             }
 
             return true;
         }
 
         try {
             zero.sendERR(new StringDataStream(ERR_MSG_PROFILES_UNAVAILABLE));
         } catch (Exception x) {
             terminate("Error sending error. " + x.getMessage());
         }
 
         return false;
     }
 
     private void shutdown() throws BEEPException
     {
         this.disableIO();
         channels.clear();
 
         zeroListener = null;
         zero = null;
         this.changeState(SESSION_STATE_CLOSED);
         fireEvent(SessionEvent.SESSION_TERMINATED_EVENT_CODE,this);
     }
 
     private void waitForResult(Channel ch, int expectedState)
             throws BEEPException, BEEPError
     {
         int waitCount = 0;
 
         while ((ch.getState() == expectedState)
                 && (waitCount < Constants.MAX_START_CHANNEL_WAIT)) {
             try {
                 synchronized (ch) {
                     ch.wait(Constants.MAX_START_CHANNEL_INTERVAL);
 
                     waitCount += Constants.MAX_START_CHANNEL_INTERVAL;
                 }
             } catch (InterruptedException e) {
                 waitCount += Constants.MAX_START_CHANNEL_INTERVAL;
             }
         }
 
         if (waitCount == Constants.MAX_START_CHANNEL_WAIT) {
             Log.logEntry(Log.SEV_DEBUG, CORE, "Wait for result timed out");
         }
 
         // check the channel state and return the appropriate exception
         if (ch.getState() == Channel.STATE_OK) {
             return;
         }
 
         if (ch.getState() == Channel.STATE_ERROR) {
             BEEPError e = ch.getErrorMessage();
 
             e.fillInStackTrace();
 
             throw e;
         }
 
         if (ch.getState() == Channel.STATE_UNINITIALISED) {
             throw new BEEPException(ERR_BEEP_START_CHANNEL_TIMEOUT);
         }
 
         // @todo what exception should we throw here?
     }
 
     private Element processMessage(Message message) throws BEEPException
     {
 
         // check the message content type
         if (!message.getDataStream().getContentType().equals(DataStream.BEEP_XML_CONTENT_TYPE)) {
             throw new BEEPException("Invalid content type for this message");
         }
 
         // parse the stream
         Document doc = null;
 
         try {
             doc = builder.parse(message.getDataStream().getInputStream());
         } catch (SAXException se) {
             throw new BEEPException(ERR_MALFORMED_XML_MSG);
         } catch (IOException ioe) {
             throw new BEEPException(ERR_MALFORMED_XML_MSG);
         }
 
         if (doc == null) {
             throw new BEEPException(ERR_MALFORMED_XML_MSG);
         }
 
         Element topElement = doc.getDocumentElement();
 
         if (topElement == null) {
             throw new BEEPException(ERR_MALFORMED_XML_MSG);
         }
 
         return topElement;
     }
 
     private void sendGreeting() throws BEEPException
     {
         Log.logEntry(Log.SEV_DEBUG, CORE, "sendGreeting");
 
         // get the greeting from the session
         byte[] greeting = Session.this.getProfileRegistry().getGreeting();
         ByteDataStream f = new ByteDataStream(DataStream.BEEP_XML_CONTENT_TYPE,
                                               greeting);
 
         // send the greeting
         this.zero.sendRPY(f);
     }
 
     private class ChannelZeroListener implements MessageListener {
 
         public void receiveMSG(Message message)
             throws BEEPError, AbortChannelException
         {
             Element topElement;
             try {
                 topElement = processMessage(message);
             } catch (BEEPException e) {
                 throw new BEEPError(BEEPError.CODE_GENERAL_SYNTAX_ERROR,
                                     ERR_MALFORMED_XML_MSG);
             }
 
             String elementName = topElement.getTagName();
 
             if (elementName == null) {
                 throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                     ERR_MALFORMED_XML_MSG);
             }
 
             // is this MSG a <start>
             if (elementName.equals(Constants.TAG_START)) {
                 Log.logEntry(Log.SEV_DEBUG, CORE,
                              "Received a start channel request");
 
                 String channelNumber =
                     topElement.getAttribute(Constants.TAG_NUMBER);
 
                 if (channelNumber == null) {
                     throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                         "Malformed <start>: no channel number");
                 }
 
                 // this attribute is implied
                 String serverName =
                     topElement.getAttribute(Constants.TAG_SERVER_NAME);
                 NodeList profiles =
                     topElement.getElementsByTagName(Constants.TAG_PROFILE);
 
                 if (profiles == null) {
                     throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                         "Malformed <start>: no profiles");
                 }
 
                 LinkedList profileList = new LinkedList();
 
                 for (int i = 0; i < profiles.getLength(); i++) {
                     Element profile = (Element) profiles.item(i);
                     String uri = profile.getAttribute(Constants.TAG_URI);
 
                     if (uri == null) {
                         throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                             "no profiles in start");
                     }
 
                     String encoding =
                         profile.getAttribute(Constants.TAG_ENCODING);
                     boolean b64;
 
                     if ((encoding == null) || encoding.equals("")) {
                         b64 = false;
                     } else if (encoding.equalsIgnoreCase("base64")) {
                         b64 = true;
                     } else if (encoding.equalsIgnoreCase("none")) {
                         b64 = false;
                     } else {
                         throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                             "unkown encoding in start");
                     }
 
                     String data = null;
                     Node dataNode = profile.getFirstChild();
 
                     if (dataNode != null) {
                         data = dataNode.getNodeValue();
 
                         if (data.length() > Constants.MAX_PCDATA_SIZE) {
                             throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                                 ERR_PCDATA_TOO_BIG_MSG);
                         }
                     }
 
                     profileList.add(new StartChannelProfile(uri, b64, data));
                 }
 
                 Session.this.processStartChannel(channelNumber,
                                                  profileList);
             }
 
             // is this MSG a <close>
             else if (elementName.equals(Constants.TAG_CLOSE)) {
                 Log.logEntry(Log.SEV_DEBUG, CORE,
                              "Received a channel close request");
 
                 String channelNumber =
                     topElement.getAttribute(Constants.TAG_NUMBER);
 
                 if (channelNumber == null) {
                     throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                         "Malformed <close>: no channel number");
                 }
 
                 String code = topElement.getAttribute(Constants.TAG_CODE);
 
                 if (code == null) {
                     throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                         "Malformed <close>: no code attribute");
                 }
 
                 // this attribute is implied
                 String xmlLang =
                     topElement.getAttribute(Constants.TAG_XML_LANG);
                 String data = null;
                 Node dataNode = topElement.getFirstChild();
 
                 if (dataNode != null) {
                     data = dataNode.getNodeValue();
 
                     if (data.length() > Constants.MAX_PCDATA_SIZE) {
                         throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                             ERR_PCDATA_TOO_BIG_MSG);
                     }
                 }
 
                 Session.this.receiveCloseChannel(channelNumber, code,
                                                  xmlLang, data);
             } else {
                 throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                     ERR_UNKNOWN_OPERATION_ELEMENT_MSG);
             }
         }
     }
 
     private class GreetingListener implements ReplyListener {
         public void receiveRPY(Message message)
         {
             try {
                 Element topElement = processMessage(message);
 
                 // is this RPY a <greeting>
                 String elementName = topElement.getTagName();
 
                 if (elementName == null) {
                     throw new BEEPException(ERR_MALFORMED_XML_MSG);
                 } else if (!elementName.equals(Constants.TAG_GREETING)) {
                     throw new BEEPException(ERR_UNKNOWN_OPERATION_ELEMENT_MSG);
                 }
 
                 Log.logEntry(Log.SEV_DEBUG, CORE, "Received a greeting");
 
                 // this attribute is implied
                 String features =
                     topElement.getAttribute(Constants.TAG_FEATURES);
 
                 // This attribute has a default value
                 String localize =
                     topElement.getAttribute(Constants.TAG_LOCALIZE);
 
                 if (localize == null) {
                     localize = Constants.LOCALIZE_DEFAULT;
                 }
 
                 // Read the profiles - note, the greeting is valid
                 // with 0 profiles
                 NodeList profiles =
                     topElement.getElementsByTagName(Constants.TAG_PROFILE);
 
                 if (profiles.getLength() > 0) {
                     LinkedList profileList = new LinkedList();
 
                     for (int i = 0; i < profiles.getLength(); i++) {
                         Element profile = (Element) profiles.item(i);
                         String uri = profile.getAttribute(Constants.TAG_URI);
 
                         if (uri == null) {
                             throw new BEEPException(ERR_MALFORMED_PROFILE_MSG);
                         }
 
                         String encoding =
                             profile.getAttribute(Constants.TAG_ENCODING);
 
                         // encoding is not allowed in greetings
                         if (encoding != null) {
 
                             // @todo check this
                             // terminate("Invalid attribute 'encoding' in greeting.");
                             // return;
                         }
 
                         profileList.add(i, profile);
                     }
 
                     Session.this.peerSupportedProfiles = profileList;
                 }
 
                 changeState(Session.SESSION_STATE_GREETING_RECEIVED);
 
                 synchronized (this) {
                     this.notifyAll();
                 }
             } catch (BEEPException e) {
                 terminate("Problem with RPY: " + e.getMessage());
             }
         }
 
         public void receiveERR(Message message)
         {
             terminate("Received an unexpected ERR");
         }
 
         public void receiveANS(Message message)
         {
             terminate("Received an unexpected ANS");
         }
 
         public void receiveNUL(Message message)
         {
             terminate("Received an unexpected NUL");
         }
     }
 
     private class StartReplyListener implements ReplyListener {
 
         Channel channel;
         boolean disableIO;
 
         StartReplyListener(Channel channel, boolean disableIO)
         {
             this.channel = channel;
             this.disableIO = disableIO;
         }
 
         public void receiveRPY(Message message)
         {
             try {
                 if (disableIO) {
                     Session.this.disableIO();
                 }
                 Element topElement = processMessage(message);
 
                 // is this RPY a <greeting>
                 String elementName = topElement.getTagName();
 
                 if (elementName == null) {
                     throw new BEEPException(ERR_MALFORMED_XML_MSG);
                 // is this RPY a <profile>
                 } else if (elementName.equals(Constants.TAG_PROFILE)) {
                     try {
                         String uri = topElement.getAttribute(Constants.TAG_URI);
 
                         if (uri == null) {
                             throw new BEEPException(ERR_MALFORMED_PROFILE_MSG);
                         }
 
                         String encoding =
                             topElement.getAttribute(Constants.TAG_ENCODING);
 
                         if (encoding == null) {
                             encoding = Constants.ENCODING_NONE;
                         }
 
                         // see if there is data and then turn it into a message
                         Node dataNode = topElement.getFirstChild();
                         String data = null;
 
                         if (dataNode != null) {
                             data = dataNode.getNodeValue();
 
                             if (data.length() > Constants.MAX_PCDATA_SIZE) {
                                 throw new BEEPException(ERR_PCDATA_TOO_BIG_MSG);
                             }
                         }
 
                         channel.setEncoding(encoding);
                         channel.setProfile(uri);
                         channel.setStartData(data);
                         channel.setErrorMessage(null);
                         Session.this.receiveStartChannelResultOk(channel,
                                                                  data);
                         
                     } catch (Exception x) {
                         throw new BEEPException(x.getMessage());
                     }
                 } else {
                     throw new BEEPException(ERR_UNKNOWN_OPERATION_ELEMENT_MSG);
                 }
             } catch (BEEPException e) {
                 terminate("Problem with RPY: " + e.getMessage());
             }
         }
 
         public void receiveERR(Message message)
         {
             BEEPError err;
 
             try {
                 err = BEEPError.convertMessageERRToException(message);
             } catch (BEEPException e) {
                 terminate(e.getMessage());
                 return;
             }
 
             Log.logEntry(Log.SEV_ERROR, CORE,
                          "Received an error in response to a start. code="
                          + err.getCode() + " diagnostic="
                          + err.getDiagnostic());
 
             // @todo slop
             channel.setErrorMessage(err);
 
             // set the state
             channel.setState(Channel.STATE_ERROR);
 
             channels.remove(channel.getNumberAsString());
 
             // release the block waiting for the channel to start or close
             synchronized (channel) {
                 channel.notify();
             }
         }
 
         public void receiveANS(Message message)
         {
             terminate("Received an unexpected ANS");
         }
 
         public void receiveNUL(Message message)
         {
             terminate("Received an unexpected NUL");
         }
     }
 
     private class CloseReplyListener implements ReplyListener {
         Channel channel;
 
         CloseReplyListener(Channel channel)
         {
             this.channel = channel;
         }
 
         public void receiveRPY(Message message)
         {
             try {
                 Element topElement = processMessage(message);
 
                 String elementName = topElement.getTagName();
 
                 if (elementName == null) {
                     throw new BEEPException(ERR_MALFORMED_XML_MSG);
                 // is this RPY an <ok> (the positive response to a
                 // channel close)
                 } else if (elementName.equals(Constants.TAG_OK)) {
                     Log.logEntry(Log.SEV_DEBUG, CORE,
                                  "Received an OK for channel close");
 
                     channel.setErrorMessage(null);
                     Session.this.receiveCloseChannelResultOk(channel);
                 } else {
                     throw new BEEPException(ERR_UNKNOWN_OPERATION_ELEMENT_MSG);
                 }
             } catch (BEEPException e) {
                 terminate("Problem with RPY: " + e.getMessage());
             }
         }
 
         public void receiveERR(Message message)
         {
             BEEPError err;
 
             try {
                 err = BEEPError.convertMessageERRToException(message);
             } catch (BEEPException e) {
                 terminate(e.getMessage());
                 return;
             }
 
             Log.logEntry(Log.SEV_DEBUG, CORE,
                          "Received an error in response to a close. code="
                          + err.getCode() + " diagnostic="
                          + err.getDiagnostic());
 
             // @todo slop
             channel.setErrorMessage(err);
 
             // set the state
             channel.setState(Channel.STATE_ERROR);
 
             channels.remove(channel.getNumberAsString());
 
             // release the block waiting for the channel to start or close
             synchronized (channel) {
                 channel.notify();
             }
         }
 
         public void receiveANS(Message message)
         {
             terminate("Received an unexpected ANS");
         }
 
         public void receiveNUL(Message message)
         {
             terminate("Received an unexpected NUL");
         }
     }
 }
