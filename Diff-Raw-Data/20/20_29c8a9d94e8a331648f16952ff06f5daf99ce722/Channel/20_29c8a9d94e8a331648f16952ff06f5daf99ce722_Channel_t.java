 /*
 * Channel.java            $Revision: 1.6 $ $Date: 2001/04/25 03:47:27 $
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
 
 
 import java.io.*;
 
 import java.util.*;
 
 import org.beepcore.beep.util.Log;
 
 
 /**
  * Channel is a conduit for a certain kind of traffic over a session,
  * determined by the profile of the channel.  Channels are created by Session
  *
  * @author Eric Dixon
  * @author Huston Franklin
  * @author Jay Kint
  * @author Scott Pead
 * @version $Revision: 1.6 $, $Date: 2001/04/25 03:47:27 $
  *
  */
 public class Channel {
 
     // class variables
     static final int MAX_MESSAGES_WAITING_DEFAULT = 10;
     static final int STATE_UNINITIALISED = 1;
     static final int STATE_OK = 2;
     static final int STATE_CLOSING = 3;
     static final int STATE_CLOSED = 4;
     static final int STATE_ERROR = 5;
     private static final String ERR_CHANNEL_MESSAGE_NUMBER_PREFIX =
         "Incorrect message number: was ";
     private static final String ERR_CHANNEL_MIDDLE = "; expecting ";
     private static final String ERR_CHANNEL_SEQUENCE_NUMBER_PREFIX =
         "Incorrect sequence number: was ";
     private static final String ERR_CHANNEL_ERROR_STATE =
         "Channel in currently experiencing technical difficulties.";
     private static final String ERR_CHANNEL_UNINITIALISED_STATE =
         "Channel is uninitialised.";
     private static final String ERR_CHANNEL_INCONSISTENT_FRAME_TYPE_PREFIX =
         "Incorrect message type: was ";
     private static final String ERR_REPLY_RECEIVED_FOR_NO_MESSAGE =
         "Reply received for a message never sent.";
     private static final int MAX_PAYLOAD_SIZE = 4096;
 
     /** @todo check this */
 
     // default values for some variables
     static final int DEFAULT_WINDOW_SIZE = 4096;
     static final int DEFAULT_TIME_TO_LIVE = 30000;    // in millisecs
 
     // instance variables
     String profile;         // syntax of messages
     String encoding;        // encoding of uesed by profile
     String number;          // channel number on the session
     String startData;    // Used to pass data sent on the Start Channel request
     DataListener listener;  // receiver of messages (or partial messages)
     int lastAnswerSent;     // Used to track MSGNOs for Answers
     private int lastMessageSent;    // number of last message sent
     int lastReplyRecv;      // number of last reply received
     int lastMessageRecv;    // number of last message received
     int lastReplySent;      // number of last reply sent
     long sentSequence;       // sequence number for messages sent
     long recvSequence;       // sequence for messages received
     int ansWaiting;    // message number for which we're currently receiving answers
     List sentMSGQueue;      // messages waiting for replies
     LinkedList recvMSGQueue;// MSG we've received by awaiting proceesing of a former MSG
     int frameSize;          // size of the frames
     int ansno = -1;         // counter of the answer
     Session session;        // session this channel sends through.
     FrameDataStream currentDataStream;    // data stream that frames are coming in
 
     // message that we are receiving frames
     LinkedList recvReplyQueue;
     Message sentMessage;    // message that is being processed by a listener
     int state = STATE_UNINITIALISED;                  //
     BEEPError errMessage;   //
     Frame previousFrame;
     int peerWindowSize;     // size of the peer's receive buffer
     int recvWindowSize;     // size of the receive buffer
     int recvWindowUsed;     // amount of the buffer in use
     long prevAckno;
     int prevWindowUsed;
     int prevWindowSize;
     int waitTimeForPeer;    //
     int msgsPending;        // semaphore for waiting until all ANS have been
     private boolean notifyOnFirstFrame;
     private Object applicationData = null;
 
     // in shutting down the session
     // something for waiting synchronous messages (semaphores or something)
 
     /**
      * Create a <code>Channel</code> object.
      *
      * @param profile URI string of the profile that this channel will "speak".
      * @param number The channel number.
      * @param listener message listener that will receive callbacks for
      *    messages received on this channel
      * @param session <code>Session</code> over which this channel
      *                sends/receives messages
      *
      * @see org.beepcore.beep.core.Session
      * @see org.beepcore.beep.core.DataListener
      */
     protected Channel(String profile, String number, DataListener listener,
                       Session session)
     {
         this.profile = profile;
         this.encoding = Constants.ENCODING_DEFAULT;
         this.number = number;
         this.listener = listener;
         this.session = session;
         this.frameSize = MAX_PAYLOAD_SIZE;
         sentSequence = 0;
         recvSequence = 0;
         lastMessageSent = 1;
         lastMessageRecv = 1;
 
         if (Integer.parseInt(number) == 0) {
             lastReplyRecv = 0;
             lastReplySent = 0;
         } else {
             lastReplyRecv = 1;
             lastReplySent = 1;
         }
 
         ansno = -1;
         ansWaiting = -1;
         sentMSGQueue = Collections.synchronizedList(new LinkedList());
         recvMSGQueue = new LinkedList();
         recvReplyQueue = new LinkedList();
         currentDataStream = null;
         state = STATE_UNINITIALISED;
         recvWindowUsed = 0;
         recvWindowSize = DEFAULT_WINDOW_SIZE;
         prevWindowUsed = 0;
         prevWindowSize = DEFAULT_WINDOW_SIZE;
         msgsPending = 0;
         peerWindowSize = DEFAULT_WINDOW_SIZE;
         waitTimeForPeer = 0;
         this.notifyOnFirstFrame = false;
     }
 
     /**
      * This is a special constructor for Channel Zero
      *
      * @param Session
      * @param ReplyListener
      *
      */
     Channel(Session session, ReplyListener rl)
     {
         this(null, Constants.CHANNEL_ZERO, null, session);
 
         // Add a MSG to the SentMSGQueue to fake channel into accepting the
         // greeting which comes in an unsolicited RPY.
         Message m = new Message(this, 0, null, Message.MESSAGE_TYPE_MSG);
         sentMSGQueue.add(new MessageStatus(m, rl));
 
         state = STATE_OK;
     }
 
     /**
      * Closes the channel.
      *
      * @throws BEEPException
      */
     public void close() throws BEEPException
     {
 
         // @todo the other BEEP peer may refuse this request
         // should we return a boolean or throw a CloseChannelException?
         session.closeChannel(this, BEEPError.CODE_SUCCESS, null);
     }
 
     // instance methods
 
     /**
      * Returns application context data previously set using
      * <code>setAppData()</code>.
      *
      * @see #setAppData
      */
     public Object getAppData()
     {
         return this.applicationData;
     }
 
     /**
      * Set the application context data member for future retrieval.
      *
      * @see #getAppData
      */
     public void setAppData(Object applicationData)
     {
         this.applicationData = applicationData;
     }
 
     /**
      * Returns the receive buffer size for this channel.
      */
     public synchronized int getBufferSize()
     {
         return recvWindowSize;
     }
 
     /**
      * Returns the size of the used portion of the receive buffer for this
      * channel.
      */
     public synchronized int getBufferUsed()
     {
         return recvWindowUsed;
     }
 
     /**
      * Returns the encoding used on this <code>Channel</code>
      * @todo look at removing this and adding the information to getProfile()
      */
     public String getEncoding()
     {
         return encoding;
     }
 
     void setEncoding(String enc)
     {
         this.encoding = enc;
     }
 
     /**
      * Return the number of this <code>Channel</code>.
      *
      */
     public int getNumber()
     {
         return Integer.parseInt(number);
     }
 
     /**
      * Sets the receive buffer size for this channel.  Default size is 4K.
      *
      *
      * @param size
      *
      * @throws BEEPException
      *
      */
     public void setReceiveBufferSize(int size) throws BEEPException
     {
         synchronized (this) {
             if ((state != STATE_OK) && (state != STATE_UNINITIALISED)) {
                 throw new BEEPException("Channel in a bad state.");
             }
 
             // make sure we aren't setting the size less than what is currently
             // in the buffer right now.
             if (size < recvWindowUsed) {
                 throw new BEEPException("Size must be less than what is currently in use.");
             }
 
             // @TODO what if they decide to shrink the buffer?  Is that even
             // allowed?
             // set the new size and copy the buffer
             recvWindowSize = size;
 
             Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                          "Buffer size for channel " + number + " set to "
                          + recvWindowSize);
 
             // send a new SEQ message to update the buffer size
             if (session.updateMyReceiveBufferSize(this, prevAckno,
                                                   recvSequence,
                                                   prevWindowUsed,
                                                   recvWindowUsed,
                                                   recvWindowSize)) {
                 prevAckno = recvSequence;
                 prevWindowUsed = recvWindowUsed;
             }
         }
     }
 
     /**
      * Determines whether calls to the <code>MessageListener</code> are
      * made upon receiving the first or last <code>Frame</code> of the message.
      *
      * @param n If <code>true</code>, calls to <code>MessageListener</code> are
      * received on the first <code>Frame</code> of a message.  Otherwise, the
      * <code>MessageListener</code> will be called once the message is complete
      * (when the last <code>Frame</code> is received).
      *
      * @see MessageListener
      */
     public void setNotifyMessageListenerOnFirstFrame(boolean n)
     {
         this.notifyOnFirstFrame = n;
     }
 
     /**
      * Returns whether or not calls to the <code>MessageListener</code> are
      * made upon receiving the first or last <code>Frame</code> of the message.
      *
      * @see #setNotifyMessageListenerOnFirstFrame
      */
     public boolean getNotifyMessageListenerOnFirstFrame()
     {
         return this.notifyOnFirstFrame;
     }
 
     /**
      * get the number of this <code>Channel</code> as a <code>String</code>
      *
      */
     String getNumberAsString()
     {
         return number;
     }
 
     /**
      * returns the state of the <code>Channel</code>
      * The possible states are (all defined as Channel.STATE_*):
      * STATE_UNINITIALISED - after a channel is created
      * STATE_OK - a channel is acknowledged by the other session
      * STATE_CLOSED - the channel has been closed
      * STATE_ERROR - an error occured during creation or transmission
      */
     int getState()
     {
         return state;
     }
 
     private void receiveFrame(Frame frame) throws BEEPException
     {
         Message m = null;
 
         // if this is an incoming message rather than a reply to a
         // previously sent message
         if (frame.getMessageType() == Message.MESSAGE_TYPE_MSG) {
             synchronized (recvMSGQueue) {
                 if (recvMSGQueue.size() != 0) {
                     m = (Message) recvMSGQueue.getLast();
 
                     if (m.getMsgno() != frame.getMsgno()) {
                         m = null;
                     }
                 }
 
                 if (m == null) {
                     m = new Message(this, frame.getMsgno(),
                                     new FrameDataStream(true),
                                     frame.getMessageType());
 
                     recvMSGQueue.addLast(m);
                 }
             }
 
             ((FrameDataStream) m.getDataStream()).add(frame);
 
             // The MessageListener interface only allows one message
             // up to be processed at a time so if this is not the
             // first message on the queue just return.
             // Question, so how do we EVER catch up?  If something
             // gets stuck here.  I suspect it isn't getting taken off.
             synchronized (recvMSGQueue) {
                 if (m != recvMSGQueue.getFirst()) {
                     return;
                 }
 
                 if (frame.isLast()) {
                     recvMSGQueue.remove(m);
                 }
             }
 
             // notify message listener if this message has not been
             // notified before and notifyOnFirstFrame is set, the
             // window is full, this is the last frame.
             synchronized (m) {
                 if (m.isNotified()
                         || ((this.notifyOnFirstFrame == false)
                            && (recvSequence - prevAckno) !=
                            (recvWindowSize - prevWindowUsed)
                             && (frame.isLast() == false))) {
                     return;
                 }
 
                 m.setNotified();
             }
 
             ((MessageListener) this.listener).receiveMSG(m);
 
             return;
         }
 
         // This frame must be for a reply (RPY, ERR, ANS, NUL)
         MessageStatus mstatus;
 
         // Find corresponding MSG for this reply
         synchronized (sentMSGQueue) {
             Message sentMSG;
 
             if (sentMSGQueue.size() == 0) {
 
                 // @todo shutdown session (we think)
             }
 
             mstatus = (MessageStatus) sentMSGQueue.get(0);
             sentMSG = mstatus.getMessage();
 
             if (sentMSG.getMsgno() != frame.getMsgno()) {
 
                 // @todo shutdown session (we think)
             }
 
             // If this is the last frame for the reply (NUL, RPY, or
             // ERR) to this MSG.
             if ((frame.isLast() == true)
                     && (frame.getMessageType() != Message.MESSAGE_TYPE_ANS)) {
                 sentMSGQueue.remove(0);
             }
         }
 
         ReplyListener replyListener = mstatus.getReplyListener();
 
         // error if they don't have either a frame or reply listener
         if (replyListener == null) {
 
             // @todo should we check this on sendMSG instead?
         }
 
         if (frame.getMessageType() == Message.MESSAGE_TYPE_NUL) {
             synchronized (recvReplyQueue) {
                 if (recvReplyQueue.size() != 0) {
 
                     // There are ANS messages on the queue for which we
                     // haven't received the last frame.
                     Log.logEntry(Log.SEV_DEBUG,
                                  "Received NUL before last ANS");
                     session.terminate("Received NUL before last ANS");
                 }
             }
 
             m = new Message(this, frame.getMsgno(), null,
                             Message.MESSAGE_TYPE_NUL);
 
             mstatus.setMessageStatus(MessageStatus.MESSAGE_STATUS_RECEIVED_REPLY);
             replyListener.receiveNUL(m);
 
             return;
         }
 
         // is this an ANS message?
         if (frame.getMessageType() == Message.MESSAGE_TYPE_ANS) {
 
             // see if this answer number has already come in
             synchronized (recvReplyQueue) {
                 Iterator i = recvReplyQueue.iterator();
 
                 m = null;
 
                 while (i.hasNext()) {
                     Message tmp = (Message) i.next();
 
                     if (tmp.getAnsno() == frame.getAnsno()) {
                         m = tmp;
 
                         break;
                     }
                 }
 
                 // if no answer was found, then create a new one and
                 // add it to the queue
                 if (m == null) {
                     m = new Message(this, frame.getMsgno(), frame.getAnsno(),
                                     new FrameDataStream(true));
 
                     if (!frame.isLast()) {
                         recvReplyQueue.add(m);
                     }
                 } else if (frame.isLast()) {
 
                     // remove the found ANS from the recvReplyQueue
                     i.remove();
                 }
             }
         } else {    // ERR or RPY
             synchronized (recvReplyQueue) {
                 if (recvReplyQueue.size() == 0) {
                     m = new Message(this, frame.getMsgno(),
                                     new FrameDataStream(true),
                                     frame.getMessageType());
 
                     if (frame.isLast() == false) {
                         recvReplyQueue.add(m);
                     }
                 } else {
 
                     // @todo sanity check: make sure this is the
                     // right Message
                     m = (Message) recvReplyQueue.getFirst();
 
                     if (frame.isLast()) {
                         recvReplyQueue.removeFirst();
                     }
                 }
 
                 if (frame.isLast()) {
                     if (frame.getMessageType() == Message.MESSAGE_TYPE_ERR) {
                         mstatus.setMessageStatus(MessageStatus.MESSAGE_STATUS_RECEIVED_ERROR);
                     } else {
                         mstatus.setMessageStatus(MessageStatus.MESSAGE_STATUS_RECEIVED_REPLY);
                     }
                 }
             }
         }
 
         ((FrameDataStream) m.getDataStream()).add(frame);
 
         // notify message listener if this message has not been
         // notified before and notifyOnFirstFrame is set, the
         // window is full, this is the last frame.
         synchronized (m) {
             if (m.isNotified() || ((this.notifyOnFirstFrame == false)
 
             //                && recvWindowUsed != recvWindowSize
                                   && (recvSequence - prevAckno)
                                   != (recvWindowSize - prevWindowUsed) &&
                                   (frame.isLast() == false))) {
                 Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                              "recvWindowUsed = " + recvWindowUsed
                              + " recvWindowSize = " + recvWindowSize
                              + "\t\r\nNot notifying frame listener.");
                 return;
             }
 
             m.setNotified();
         }
 
         Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                     "Notifying reply listener.=>" + replyListener);
 
         if (m.messageType == Message.MESSAGE_TYPE_RPY) {
             replyListener.receiveRPY(m);
         } else if (m.messageType == Message.MESSAGE_TYPE_ERR) {
             replyListener.receiveERR(m);
         } else if (m.messageType == Message.MESSAGE_TYPE_ANS) {
             replyListener.receiveANS(m);
         }
     }
 
     /**
      * interface between the session.  The session receives a frame and then
      * calls this function.  The function then calls the message listener
      * via some intermediary thread functions.  The message hasn't been
      * completely received.  The data stream contained in the message will
      * block if more is expected.
      * @param frame - the frame received by the session
      */
     void postFrame(Frame frame) throws BEEPException
     {
         Log.logEntry(Log.SEV_DEBUG_VERBOSE, "Channel::postFrame");
 
         boolean firstFrame = false;
         boolean createAndPostMessage = false;
         Message currentMessage = null;
         int msgno = frame.getMsgno();
         int lastnumber;
         ReplyListener replyListener = null;
         FrameListener frameListener = null;
 
         if (state != STATE_OK) {
             throw new BEEPException("State is " + state);
         }
 
         // Validate Frame
         synchronized (this) {
             lastnumber = lastReplyRecv;
 
             // assume the frame has already been parsed by the session and
             // that the flags indicated are part of the frame object
             if (frame.getMessageType() == Message.MESSAGE_TYPE_MSG) {
                 lastnumber = lastMessageRecv;
             } else {
                 lastnumber = lastReplyRecv;
             }
 
             // is the message number correct?
             if (frame.getMsgno() != lastnumber) {
                 if ((frame.getMessageType() != Message.MESSAGE_TYPE_ANS)
                         && (frame.getMessageType()
                             != Message.MESSAGE_TYPE_NUL)) {
                     throw new BEEPException(ERR_CHANNEL_MESSAGE_NUMBER_PREFIX
                                                  + frame.getMsgno()
                                                  + ERR_CHANNEL_MIDDLE
                                                  + lastnumber);
                 }
 
                 Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                              "Accepting an ANS or NUL for a previous request");
             }
 
             // is the sequence number correct?
             if (frame.getSeqno() != recvSequence) {
                 throw new BEEPException(ERR_CHANNEL_SEQUENCE_NUMBER_PREFIX
                                              + frame.getSeqno()
                                              + ERR_CHANNEL_MIDDLE
                                              + recvSequence);
             }
 
             // is the message type the same as the previous frames?
             if ((previousFrame != null)
                     && (previousFrame.getMessageType()
                         != frame.getMessageType())) {
                 throw new BEEPException(ERR_CHANNEL_INCONSISTENT_FRAME_TYPE_PREFIX
                                              + frame.getMessageTypeString()
                                              + ERR_CHANNEL_MIDDLE
                                              + previousFrame.getMessageTypeString());
             }
         }
 
         recvSequence += frame.getSize();
 
         // subtract this from the amount available in the buffer
         recvWindowUsed += frame.getSize();
 
         // make sure we didn't overflow the buffer
         if (recvWindowUsed > recvWindowSize) {
             throw new BEEPException("Channel window overflow");
         }
 
         if (listener instanceof FrameListener) {
             frameListener = (FrameListener) this.listener;
         }
 
         if (frame.getMessageType() != Message.MESSAGE_TYPE_MSG) {
             MessageStatus mstatus;
 
             synchronized (sentMSGQueue) {
                 if (sentMSGQueue.size() == 0) {
                     throw new BEEPException("Received unsolicited reply");
                 }
 
                 mstatus = (MessageStatus) sentMSGQueue.get(0);
 
                 if (mstatus.getMessage().getMsgno() != frame.getMsgno()) {
                     throw new BEEPException("Received reply out of order");
                 }
 
                 FrameListener l = mstatus.getFrameListener();
 
                 if (l != null) {
                     frameListener = l;
 
                     // if this is the last frame and on a reply type (NUL,
                     // RPY, or ERR)
                     if ((frame.isLast() == true)
                             && (frame.getMessageType()
                                 != Message.MESSAGE_TYPE_ANS)) {
                         sentMSGQueue.remove(0);
                     }
                 }
             }
         }
 
         if (frameListener != null) {
 
             // call the frame listener and then subtract that from the used
             try {
                 frameListener.receiveFrame(frame);
 
                 recvWindowUsed -= frame.getSize();
 
                 if (session.updateMyReceiveBufferSize(this, prevAckno,
                                                       recvSequence,
                                                       prevWindowUsed,
                                                       recvWindowUsed,
                                                       recvWindowSize)) {
                     prevAckno = recvSequence;
                     prevWindowUsed = recvWindowUsed;
                 }
             } catch (BEEPException e) {
                 // @todo change this to do the right thing
                 throw new BEEPException(e.getMessage());
             }
         } else {
             try {
                 receiveFrame(frame);
             } catch (BEEPException e) {
                 // @todo change this to do the right thing
                 throw new BEEPException(e.getMessage());
             }
         }
 
         // is this the last frame in the message?
         if (frame.isLast() == true) {
             Log.logEntry(Log.SEV_DEBUG_VERBOSE, "Got the last frame");
 
             // increment the appropriate message number
             if (frame.getMessageType() == Message.MESSAGE_TYPE_MSG) {
                 lastMessageRecv++;
             } else if (frame.getMessageType() != Message.MESSAGE_TYPE_ANS) {
                 lastReplyRecv++;
             }
         }
 
         // save the previous frame to compare message types
         if (frame.isLast()) {
             previousFrame = null;
         } else {
             previousFrame = frame;
         }
     }
 
     /**
      * Sets the <code>DataListener</code> for this channel.
      *
      * @param ml
      */
     public void setDataListener(DataListener ml)
     {
 
         // @todo can this be called consecutively with different listeners?
         // if so, should it return the old listener?
         // if not, should we prevent it?
         this.listener = ml;
     }
 
     /**
      * Returns the message listener for this channel.
      *
      */
     public DataListener getDataListener()
     {
         return this.listener;
     }
 
     /**
      * Returns the session for this channel.
      *
      */
     public Session getSession()
     {
         return this.session;
     }
 
     /**
      * Sends a message of type ANS.
      *
      * @param stream Data to send in the form of <code>DataStream</code>.
      *
      * @see DataStream
      * @see MessageStatus
      * @see #sendNUL
      *
      * @return MessageStatus
      *
      * @throws BEEPException if an error is encoutered.
      */
     public MessageStatus sendANS(DataStream stream) throws BEEPException
     {
         if (state != STATE_OK) {
             switch (state) {
             case STATE_ERROR :
                 throw new BEEPException(ERR_CHANNEL_ERROR_STATE);
             case STATE_UNINITIALISED :
                 throw new BEEPException(ERR_CHANNEL_UNINITIALISED_STATE);
             default :
                 throw new BEEPException(Constants.EMPTY_STRING);
             }
         }
 
         // put a semaphore increment here to prevent another thread from
         // calling sendNUL before an answer has been transmitted completely.
         synchronized (this) {
             ++ansno;
         }
 
         MessageStatus status;
 
         synchronized (this) {
 
             // if no previous answer sent for this message then increment
             // the reply number.  Otherwise, just increment the answer number
             if (ansno == 0) {
                 lastAnswerSent = lastReplySent;
 
                 ++lastReplySent;
             }
 
             // create a new request
             status = new MessageStatus(new Message(this, lastAnswerSent,
                                                    ansno, stream));
         }
 
         // send it on the session
         sendToPeer(status);
 
         // put a semaphore decrement here to signal one answer has been
         // transmitted
         return status;
     }
 
     /**
      * Sends a message of type MSG.
      *
      * @param stream Data to send in the form of <code>DataStream</code>.
      * @param replyListener A "one-shot" listener that will handle replies
      * to this sendMSG listener.
      *
      * @see DataStream
      * @see MessageStatus
      *
      * @return MessageStatus
      *
      * @throws BEEPException if an error is encoutered.
      */
     public MessageStatus sendMSG(DataStream stream,
                                  ReplyListener replyListener)
             throws BEEPException
     {
         MessageStatus status;
 
         if (state != STATE_OK) {
             switch (state) {
             case STATE_ERROR :
                 throw new BEEPException(ERR_CHANNEL_ERROR_STATE);
             case STATE_UNINITIALISED :
                 throw new BEEPException(ERR_CHANNEL_UNINITIALISED_STATE);
             default :
                 throw new BEEPException(Constants.EMPTY_STRING);
             }
         }
 
         synchronized (this) {
 
             // create a new request
             status =
                 new MessageStatus(new Message(this, lastMessageSent, stream, Message.MESSAGE_TYPE_MSG),
                                   replyListener);
 
             // message 0 was the greeting, it was already sent, inc the counter
             ++lastMessageSent;
         }
 
         // put this in the list of messages waiting
         // may want to put an expiration or something in here so they
         // don't just stay around taking up space.
         // @todo it's a synchronized list, you don't have to sync
         synchronized (sentMSGQueue) {
             sentMSGQueue.add(status);
         }
 
         // send it on the session
         sendToPeer(status);
 
         return status;
     }
 
     /**
      * Sends a message of type MSG.
      *
      * @param stream Data to send in the form of <code>DataStream</code>.
      * @param frameListener A "one-shot" listener that will handle replies
      * to this sendMSG listener.
      *
      * @see DataStream
      * @see MessageStatus
      *
      * @return MessageStatus
      *
      * @throws BEEPException if an error is encoutered.
      */
     public MessageStatus sendMSG(DataStream stream,
                                  FrameListener frameListener)
             throws BEEPException
     {
         MessageStatus status;
 
         if (state != STATE_OK) {
             switch (state) {
             case STATE_ERROR :
                 throw new BEEPException(ERR_CHANNEL_ERROR_STATE);
             case STATE_UNINITIALISED :
                 throw new BEEPException(ERR_CHANNEL_UNINITIALISED_STATE);
             default :
                 throw new BEEPException(Constants.EMPTY_STRING);
             }
         }
 
         synchronized (this) {
 
             // create a new request
             status =
                 new MessageStatus(new Message(this, lastMessageSent, stream,
                                               Message.MESSAGE_TYPE_MSG),
                                   frameListener);
 
             // message 0 was the greeting, it was already sent, inc the counter
             ++lastMessageSent;
         }
 
         // put this in the list of messages waiting
         // may want to put an expiration or something in here so they
         // don't just stay around taking up space.
         // @todo it's a synchronized list, you don't have to sync
         synchronized (sentMSGQueue) {
             sentMSGQueue.add(status);
         }
 
         // send it on the session
         sendToPeer(status);
 
         return status;
     }
 
     /**
      * Sends a message of type NUL.
      *
      * @see MessageStatus
      * @see #sendANS
      *
      * @return MessageStatus
      *
      * @throws BEEPException if an error is encoutered.
      */
     public MessageStatus sendNUL() throws BEEPException
     {
         if (state != STATE_OK) {
             switch (state) {
             case STATE_ERROR :
                 throw new BEEPException(ERR_CHANNEL_ERROR_STATE);
             case STATE_UNINITIALISED :
                 throw new BEEPException(ERR_CHANNEL_UNINITIALISED_STATE);
             default :
                 throw new BEEPException(Constants.EMPTY_STRING);
             }
         }
 
         // what to do to assure that no other ANS follow this NUL
         // for this reply?  Throw an exception if they try and send
         // an ANS after the NUL is being sent?      Block on a mutex until
         // all the ANS frames have been sent that are pending in sendToPeer?
         // for now, we just reset the answer number, which means that if
         // more answers are sent, they are to another message.      It is up to
         // the message listener to synchronize this and call sendNUL when
         // done.
         // if no answer sent (only a NUL), then increment the message
         // number
         MessageStatus status = null;
 
         synchronized (this) {
             if (msgsPending > 0) {
                 try {
                     Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                                  "Waiting for ANS to finish");
                     wait();
                     Log.logEntry(Log.SEV_DEBUG_VERBOSE, "ANS finished");
                 } catch (InterruptedException e) {
                     throw new BEEPException(e.getMessage());
                 }
             }
 
             // create a new request
             status = new MessageStatus(new Message(this, lastAnswerSent, null,
                                                    Message.MESSAGE_TYPE_NUL));
 
             // reset the answer number
             ansno = -1;
         }
 
         // send it on the session and return it
         sendToPeer(status);
 
         return status;
     }
 
     /**
      * Sends a message of type RPY.
      *
      * @param stream Data to send in the form of <code>DataStream</code>.
      *
      * @see DataStream
      * @see MessageStatus
      *
      * @return MessageStatus
      *
      * @throws BEEPException if an error is encoutered.
      */
     public MessageStatus sendRPY(DataStream stream) throws BEEPException
     {
         if (state != STATE_OK) {
             switch (state) {
             case STATE_ERROR :
                 throw new BEEPException(ERR_CHANNEL_ERROR_STATE);
             case STATE_UNINITIALISED :
                 throw new BEEPException(ERR_CHANNEL_UNINITIALISED_STATE);
             default :
                 throw new BEEPException(Constants.EMPTY_STRING);
             }
         }
 
         // create a new request
         MessageStatus status;
 
         synchronized (this) {
             status = new MessageStatus(new Message(this, lastReplySent,
                                                    stream,
                                                    Message.MESSAGE_TYPE_RPY));
 
             ++lastReplySent;
         }
 
         // send it on the session
         sendToPeer(status);
 
         return status;
     }
 
     /**
      * Sends a message of type ERR.
      *
      * @param stream Data to send in the form of <code>DataStream</code>.
      *
      * @see DataStream
      * @see MessageStatus
      *
      * @return MessageStatus
      *
      * @throws BEEPException if an error is encoutered.
      */
     public MessageStatus sendERR(DataStream stream) throws BEEPException
     {
         if (state != STATE_OK) {
             switch (state) {
             case STATE_ERROR :
                 throw new BEEPException(ERR_CHANNEL_ERROR_STATE);
             case STATE_UNINITIALISED :
                 throw new BEEPException(ERR_CHANNEL_UNINITIALISED_STATE);
             default :
                 throw new BEEPException(Constants.EMPTY_STRING);
             }
         }
 
         MessageStatus status;
 
         synchronized (this) {
 
             // create a new request
             status = new MessageStatus(new Message(this, lastReplySent,
                                                    stream,
                                                    Message.MESSAGE_TYPE_ERR));
 
             ++lastReplySent;
         }
 
         // send it on the session
         sendToPeer(status);
 
         return status;
     }
 
     synchronized void sendToPeer(MessageStatus status) throws BEEPException
     {
         Message message = status.getMessage();
         Frame frame = null;
         int available = 0;
         DataStream stream;
         byte[] payload;
         int sessionBufferSize;
 
         // allocate a shared buffer for this message
         // @todo This call should be changed
         sessionBufferSize = session.getMaxFrameSize();
         payload = new byte[sessionBufferSize];
 
         // get the message data
         stream = message.getDataStream();
 
         if (stream == null || (stream.availableHeadersAndData() == 0 &&
                                stream.isComplete()))
         {
             Log.logEntry(Log.SEV_DEBUG, "Sending NUL or size 0 frame");
             frame = new Frame(message.getMessageType(),
                               message.getChannel(), message.getMsgno(), true,
                               sentSequence, 0, ansno);
             try {
                 session.sendFrame(frame);
             } catch (BEEPException e) {
                 Log.logEntry(Log.SEV_ERROR, e);
                 status.setMessageStatus(MessageStatus.MESSAGE_STATUS_NOT_SENT);
 
                 throw e;
             }
 
             // set the status to sent and return it
             status.setMessageStatus(MessageStatus.MESSAGE_STATUS_SENT);
 
             return;
         }
 
         // while we still have data to read
         available = stream.availableHeadersAndData();
 
         synchronized (this) {
             msgsPending++;
         }
 
         // for this message, is there anything waiting?
         boolean done = false;
         while (!done) {
             try {
                 synchronized (this) {
 
                     // make sure the other peer can accept something
                     if (peerWindowSize == 0) {
                         wait(waitTimeForPeer);
 
                         // wait until there is something to send up to
                         // our timeout
                         if (peerWindowSize == 0) {
                             throw new BEEPException("Time expired waiting for peer.");
                         }
                     }
 
                     // how much should we send?
                     int maxCanRead = 0;
 
                     // we should send the least of these:
                     // 1) the size of the send buffer in the session
                     // (transport specific)
                     maxCanRead = sessionBufferSize;
 
                     // 2) the amount our peer can accept
                     if (maxCanRead > peerWindowSize) {
                         maxCanRead = peerWindowSize;
                     }
 
                     int amountToSend = stream.readHeadersAndData(payload, 0,
                                                                  maxCanRead);
                     if ((stream.available() == 0) && stream.isComplete()) {
                         done = true;
                     }
                     else if (amountToSend == -1) {
                         done = true;
                         // send an empty payload
                         payload = new byte[0];
                         amountToSend = 0;
                     }
 
                     // create a frame
                     frame =
                         new Frame(message.getMessageType(),
                                   message.getChannel(), message.getMsgno(),
                                   done ? true : false,
                                   sentSequence, ansno,
                                   new Frame.BufferSegment(payload, 0,
                                                           amountToSend));
 
                     // update the sequence and peer window size
                     sentSequence += amountToSend;    // update the sequence
                     peerWindowSize -= amountToSend;
                 }
 
                 // send it
                 if (done) {
                     Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                                  "Channel.sendToPeer sending last frame on channel "
                                  + number);
                 } else {
                     Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                                  "Channel.sendToPeer sending a frame on channel "
                                  + number);
                 }
             } catch (Exception e) {
                 Log.logEntry(Log.SEV_ERROR, e);
                 status.setMessageStatus(MessageStatus.MESSAGE_STATUS_NOT_SENT);
 
                 throw new BEEPException(e.getMessage());
             }
 
             try {
                 session.sendFrame(frame);
             } catch (BEEPException e) {
                 Log.logEntry(Log.SEV_ERROR, e);
                 status.setMessageStatus(MessageStatus.MESSAGE_STATUS_NOT_SENT);
 
                 synchronized (this) {
                     msgsPending--;
 
                     if (msgsPending == 0) {
                         notify();
                     }
                 }
 
                 return;
             }
         }
 
         // set the status to sent and return it
         status.setMessageStatus(MessageStatus.MESSAGE_STATUS_SENT);
 
         synchronized (this) {
             msgsPending--;
 
             if (msgsPending == 0) {
                 notify();
             }
         }
     }
 
     // used by session to control the frame size of the channel
     void setFrameSize(int frameSize)
     {
         this.frameSize = frameSize;
     }
 
     /**
      * Method setState
      *
      *
      * @param newState
      *
      * @throws BEEPException
      *
      */
     synchronized void setState(int newState)
     {
         Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                      "CH" + number + " state=" + newState);
 
         this.state = newState;
 
         /**
          * @todo state transition rules and error checking
          */
         if (false) {
             session.terminate("Bad state transition in channel");
         }
     }
 
     /**
      * Method setProfile
      *
      *
      * @param profile
      *
      */
     void setProfile(String profile)
     {
         this.profile = profile;
     }
 
     /**
      * Returns the profile for this channel.
      */
     public String getProfile()
     {
         return this.profile;
     }
 
     /**
      * Method getErrorMessage
      *
      * @return BEEPError
      */
     BEEPError getErrorMessage()
     {
         return this.errMessage;
     }
 
     /**
      * Method setErrorMessage
      *
      * @param message
      */
     void setErrorMessage(BEEPError message)
     {
         this.errMessage = message;
     }
 
     /**
      * Method updatePeerReceiveBufferSize
      *
      *
      * @param lastSeq
      * @param size
      *
      */
     synchronized void updatePeerReceiveBufferSize(long lastSeq, int size)
     {
         int previousPeerWindowSize = peerWindowSize;
 
         Log.logEntry(Log.SEV_DEBUG,
                      "Channel.updatePeerReceiveBufferSize: size = " + size
                      + " lastSeq = " + lastSeq + " sentSequence = "
                      + sentSequence + " peerWindowSize = " + peerWindowSize);
 
         peerWindowSize = size - (int) (sentSequence - lastSeq);
 
         Log.logEntry(Log.SEV_DEBUG,
                      "Channel.updatePeerReceiveBufferSize: New window size = "
                      + peerWindowSize);
 
         if ((previousPeerWindowSize == 0) && (peerWindowSize > 0)) {
             notify();    // unblock if we're waiting to send
         }
     }
 
     /**
      * Method freeReceiveBufferBytes
      *
      *
      * @param size
      *
      */
     synchronized void freeReceiveBufferBytes(int size)
     {
         try {
             Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                          "Freed up " + size + " bytes on channel " + number);
 
             recvWindowUsed -= size;
 
             Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                          "recvWindowUsed = " + recvWindowUsed);
 
             if (session.updateMyReceiveBufferSize(this, prevAckno,
                                                   recvSequence,
                                                   prevWindowUsed,
                                                   recvWindowUsed,
                                                   recvWindowSize)) {
                 prevAckno = recvSequence;
                 prevWindowUsed = recvWindowUsed;
             }
         } catch (BEEPException e) {
 
             // do nothing
             Log.logEntry(Log.SEV_ALERT, e);
         }
     }
 
     /**
      * Method getAvailableWindow
      *
      *
      * @return int the amount of free buffer space
      * available.
      *
      * This is called from Session to provide a # used
      * to screen frame sizes against and enforce the
      * protocol.
      *
      */
     synchronized int getAvailableWindow()
     {
         return (recvWindowSize - recvWindowUsed);
     }
 
     /**
      * Used to set data that can be piggybacked on
      * a profile reply to a start channel request
      * (or any other scenario we choose)
      *
      * called by Channel Zero
      */
     void setStartData(String data)
     {
         startData = data;
     }
 
     /**
      * Used to get data that can be piggybacked on
      * a profile reply to a start channel request
      * (or any other scenario we choose)
      *
      * Could be called by users, profile implementors etc.
      * to fetch data off a profile response.
      *
      * @return String the attached data, if any
      */
     public String getStartData()
     {
         return startData;
     }
 }
