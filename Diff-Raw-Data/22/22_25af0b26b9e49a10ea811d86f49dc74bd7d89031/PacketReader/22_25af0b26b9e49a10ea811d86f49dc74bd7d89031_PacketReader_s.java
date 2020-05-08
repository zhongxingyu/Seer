 /**
  * $RCSfile$
  * $Revision$
  * $Date$
  *
  * Copyright 2003-2007 Jive Software.
  *
  * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.jivesoftware.smack;
 
 import org.jivesoftware.smack.Connection.ListenerWrapper;
 import org.jivesoftware.smack.packet.*;
 import org.jivesoftware.smack.sasl.SASLMechanism.Challenge;
 import org.jivesoftware.smack.sasl.SASLMechanism.Failure;
 import org.jivesoftware.smack.sasl.SASLMechanism.Success;
 import org.jivesoftware.smack.util.PacketParserUtils;
 import org.jivesoftware.smack.util.ThreadUtil;
 import org.jivesoftware.smack.util.XmlPullParserDom;
 import org.w3c.dom.Element;
 import org.xmlpull.v1.XmlPullParser;
 
 import java.util.concurrent.*;
 
 /**
  * Listens for XML traffic from the XMPP server and parses it into packet objects.
  * The packet reader also invokes all packet listeners and collectors.<p>
  *
  * @see Connection#createPacketCollector
  * @see Connection#addPacketListener
  * @author Matt Tucker
  */
 class PacketReader {
 
     private Thread readerThread;
     private ExecutorService listenerExecutor;
 
     private XMPPConnection connection;
     private boolean done;
 
     protected PacketReader(final XMPPConnection connection) {
         this.connection = connection;
         done = false;
     }
 
     /**
      * Starts the packet reader thread and returns once a connection to the server
      * has been established. A connection will be attempted for a maximum of five
      * seconds. An XMPPException will be thrown if the connection fails.
      *
      * @throws XMPPException if the server fails to send an opening stream back
      *      for more than five seconds.
      */
     private Semaphore connectionSemaphore;
     private XMPPException connectionException;
     private void connectionEstablished() {
         connectionSemaphore.release();
     }
     private void connectionEstablishError(XMPPException e) {
         connectionException = e;
         connectionSemaphore.release();
     }
 
     /**
      * Start the reader thread, blocking until the transport is established.  If
      * an exception is thrown, the caller must shut down the data stream to ensure
      * the reader thread exits, and call our shutdown() method.
      *
      * @throws XMPPException if the connection could not be established
      */
     public void startup() throws XMPPException {
         if(readerThread != null)
             throw new RuntimeException("ReaderThread.startup called while already running");
 
         done = false;
         connectionException = null;
         connectionSemaphore = new Semaphore(0);
 
         // Create an executor to deliver incoming packets to listeners. We'll use a single
         // thread with an unbounded queue.
         listenerExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
             public Thread newThread(Runnable runnable) {
                 Thread thread = new Thread(runnable,
                         "Smack Listener Processor (" + connection.connectionCounterValue + ")");
                 thread.setDaemon(true);
                 return thread;
             }
         });
 
         // Begin connecting.
         readerThread = new Thread() {
             public void run() {
                 parsePackets(this);
             }
         };
         readerThread.setName("Smack Packet Reader (" + connection.connectionCounterValue + ")");
         readerThread.setDaemon(true);
         readerThread.start();
 
         // Wait until the connection is established before returning.
         int waitTime = SmackConfiguration.getPacketReplyTimeout();
 
         try {
             if(!connectionSemaphore.tryAcquire(3 * waitTime, TimeUnit.MILLISECONDS)) {
                 /* The connection timed out. */
                 throw new XMPPException("Connection failed. No response from server.");
             }
         }
         catch (InterruptedException ie) {
             throw new XMPPException("Connection interrupted", ie);
         }
 
         // If an exception occurred during connection, re-throw it.
         if (connectionException != null)
             throw connectionException;
     }
 
     /**
      * Shuts the packet reader down.
      *
      * The caller must first shut down the data stream to ensure the thread will exit.
      */
     public void shutdown() {
         if(readerThread == Thread.currentThread())
             throw new AssertionError("shutdown() can't be called from the packet reader thread");
 
         // The actual shutdown happens due to the caller closing the data stream.
         done = true;
 
         // Do nothing if we're already shut down.
         if(readerThread == null)
             return;
 
         // Wait for the reader thread to exit.  It's the caller's responsibility to ensure
         // that the underlying reader returns an EOF before calling this function.
         ThreadUtil.uninterruptibleJoin(readerThread);
         readerThread = null;
 
         // Shut down the listener executor.
         listenerExecutor.shutdown();

        connection.recvListeners.clear();
        connection.collectors.clear();
     }
 
     /** Assert that the current thread is not the reader thread. */
     public void assertNotInThread() {
         if(Thread.currentThread() == readerThread)
             throw new RuntimeException("Call from within reader thread prohibited");
     }
 
     /**
      * Parse top-level packets in order to process them further.
      *
      * @param thread the thread that is being used by the reader to parse incoming packets.
      */
     private void parsePackets(Thread thread) {
         boolean waitingForEstablishedConnection = true;
         try {
             try {
                 connection.initializeConnection();
             } catch(XMPPException e) {
                 /* Before connection, users can't yet attach error listeners.  Errors before
                  * connection are thrown from startup(). */
                 connectionEstablishError(e);
                 return;
             }
 
             while(!done) {
                 // Read the next packet.
                 Element packet = this.connection.readPacket();
 
                 if(packet == null) {
                     // The session has terminated.
                     throw new XMPPException("Connection closed");
                 }
 
                 /* Convert the stanza to an XmlPullParser. */
                 XmlPullParser parser = new XmlPullParserDom(packet, true);
 
                 for ( ; parser.getEventType() != XmlPullParser.END_DOCUMENT; parser.next() ) {
                     if(parser.getEventType() != XmlPullParser.START_TAG)
                         continue;
 
                     if (parser.getName().equals("message")) {
                         processPacket(PacketParserUtils.parseMessage(parser));
                     }
                     else if (parser.getName().equals("iq")) {
                         processPacket(PacketParserUtils.parseIQ(parser, connection));
                     }
                     else if (parser.getName().equals("presence")) {
                         processPacket(PacketParserUtils.parsePresence(parser));
                     }
                     else if (parser.getName().equals("error")) {
                         throw new XMPPException(PacketParserUtils.parseStreamError(parser));
                     }
                     else if (parser.getName().equals("features")) {
                         parseFeatures(parser);
 
                         if(waitingForEstablishedConnection) {
                             /* When initializeConnection returns, the connection is established and ready to use.
                              * However, don't signal to continue until we receive the first packet, which will
                              * be <features/>.  Otherwise, we're not ready for login() to be called. */
                             connectionEstablished();
                             waitingForEstablishedConnection = false;
                         }
                     }
                     else if (parser.getName().equals("failure") &&
                             parser.getNamespace().equals("urn:ietf:params:xml:ns:xmpp-sasl")) {
                         // SASL authentication has failed. The server may close the connection
                         // depending on the number of retries
                         final Failure failure = PacketParserUtils.parseSASLFailure(parser);
                         processPacket(failure);
                         connection.getSASLAuthentication().authenticationFailed(failure.getCondition());
                     }
                     else if (parser.getName().equals("challenge") &&
                         parser.getNamespace().equals("urn:ietf:params:xml:ns:xmpp-sasl")) {
                         // The server is challenging the SASL authentication made by the client
                         String challengeData = parser.nextText();
                         processPacket(new Challenge(challengeData));
                         connection.getSASLAuthentication().challengeReceived(challengeData);
                     }
                     else if (parser.getName().equals("success") &&
                             parser.getNamespace().equals("urn:ietf:params:xml:ns:xmpp-sasl")) {
                         processPacket(new Success(parser.nextText()));
 
                         // After a <success>, the stream is reset.  Inform the stream.
                         connection.streamReset();
 
                         // The SASL authentication with the server was successful. The next step
                         // will be to bind the resource
                         connection.getSASLAuthentication().authenticated();
                     }
                 }
             }
         }
         catch (RuntimeException e) {
             throw e; // don't handle unchecked exceptions below
         } catch (Exception e) {
             e.printStackTrace();
             if(waitingForEstablishedConnection) {
                 waitingForEstablishedConnection = false;
                 // If waitingForEstablishedConnection is true, startup() is still waiting for
                 // us, so deliver the exception there instead.
                 if(e instanceof XMPPException)
                     connectionEstablishError((XMPPException) e);
                 else
                     connectionEstablishError(new XMPPException(e));
             }
             else if (!done) {
                 // Close the connection and notify connection listeners of the
                 // error.
                 done = true;
                 connection.readerThreadException(e);
             }
         }
     }
 
     /**
      * Processes a packet after it's been fully parsed by looping through the installed
      * packet collectors and listeners and letting them examine the packet to see if
      * they are a match with the filter.
      *
      * @param packet the packet to process.
      */
     private void processPacket(Packet packet) {
         if (packet == null) {
             return;
         }
 
         // Deliver the incoming packet to listeners.
         listenerExecutor.submit(new ListenerNotification(packet));
     }
 
     private void parseFeatures(XmlPullParser parser) throws Exception {
         boolean done = false;
         while (!done) {
             int eventType = parser.next();
 
             if (eventType == XmlPullParser.START_TAG) {
                 if (parser.getName().equals("mechanisms")) {
                     // The server is reporting available SASL mechanisms. Store this information
                     // which will be used later while logging (i.e. authenticating) into
                     // the server
                     connection.getSASLAuthentication()
                             .setAvailableSASLMethods(PacketParserUtils.parseMechanisms(parser));
                 }
                 else if (parser.getName().equals("bind")) {
                     // The server requires the client to bind a resource to the stream
                     connection.getSASLAuthentication().bindingRequired();
                 }
                 else if (parser.getName().equals("session")) {
                     // The server supports sessions
                     connection.getSASLAuthentication().sessionsSupported();
                 }
                 else if (parser.getName().equals("register")) {
                     connection.getAccountManager().setSupportsAccountCreation(true);
                 }
             }
             else if (eventType == XmlPullParser.END_TAG) {
                 if (parser.getName().equals("features")) {
                     done = true;
                 }
             }
         }
     }
 
     /**
      * A runnable to notify all listeners of a packet.
      */
     private class ListenerNotification implements Runnable {
 
         private Packet packet;
 
         public ListenerNotification(Packet packet) {
             this.packet = packet;
         }
 
         public void run() {
             // Listeners are run synchronously to this thread.  Run them before
             // collectors, so a collector can be used to wait until all listeners
             // on a packet have been run.
             for (ListenerWrapper listenerWrapper : connection.recvListeners.values()) {
                 listenerWrapper.notifyListener(packet);
             }
 
             // Loop through all collectors and notify the appropriate ones.
             System.out.print("processPacket id " + packet.getPacketID());
             for (PacketCollector collector: connection.getPacketCollectors()) {
                 collector.processPacket(packet);
             }
 
         }
     }
 }
