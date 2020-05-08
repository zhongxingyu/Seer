 /*
  * Created on 11. september 2008
  */
 package no.ntnu.fp.net.co;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.net.ConnectException;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Timer;
 
 import no.ntnu.fp.net.admin.Log;
 import no.ntnu.fp.net.cl.ClException;
 import no.ntnu.fp.net.cl.ClSocket;
 import no.ntnu.fp.net.cl.KtnDatagram;
 import no.ntnu.fp.net.cl.KtnDatagram.Flag;
 import no.ntnu.fp.net.co.ClSocketReceiver;
 import no.ntnu.fp.net.co.Connection;
 /**
  * @author mariubje A partial implementation of the Connection-interface. It
  *         implements partial functionality and some utility functions.
  */
 public abstract class AbstractConnection implements Connection {
 
     /**
     * The possible states for a Connection.
      */
     protected enum State {
         CLOSED, LISTEN, SYN_SENT, SYN_RCVD, ESTABLISHED, FIN_WAIT_1, FIN_WAIT_2, TIME_WAIT, CLOSE_WAIT, LAST_ACK
     }
 
     /**
      * Time between retransmissions. When setting this, also consider setting
      * {@link #TIMEOUT}: There has to be time for a few retransmissions within
      * the timeout. Setting RETRANSMIT too low will result in a lot of traffic
      * and duplicate packets because of the delays in A2. Note: Low values of
      * RETRANSMIT will generate duplicate packets independently of the setting
      * for duplicate packets in the configuration for A2!
      */
     protected final static int RETRANSMIT = 800;
 
     /**
      * Timeout for receives. Setting this too high can cause slow operation in
      * the case of many errors, while setting it too low can cause failure of
      * operation because of the delays in A2. It is now set to three times the
      * {@link #RETRANSMIT} value, for a total of 4 possible transmissions before
      * timing out.
      */
     protected static int TIMEOUT = 3 * RETRANSMIT + (RETRANSMIT / 2);
 
     /**
      * To prevent more than one thread to concurrently execute certain parts of
      * doReceive(). DO NOT alter the value of this variable unless you KNOW what
      * you are doing, as this may hang the implementation or cause
      * BindExceptions in A2 - you are warned!
      */
     private boolean isReceiving;
 
     /**
      * Unhandled internal packets. Packets are put in this queue when waiting
      * for a data packet and receiving an internal packet (e.g. ACK).
      */
     private List<KtnDatagram> internalQueue;
 
     /**
      * Unhandled external (application-destined) packets. Packets are put in
      * this queue when waiting for internal packets (e.g. ACK) and receiving a
      * data packet.
      */
     private List<KtnDatagram> externalQueue;
 
     /**
      * Identifies the state of the connection.
      */
     protected State state;
 
     /** Local and remote ip-address. */
     protected String myAddress, remoteAddress;
     /** Local and remote port number. */
     protected int myPort, remotePort;
     /** Reference to the last packet received. */
     protected KtnDatagram lastValidPacketReceived;
     /** Reference to the last data packet sent. */
     protected KtnDatagram lastDataPacketSent;
     /** The current sequence number used in packets to be sent. */
     protected int nextSequenceNo;
 
     /** The sequence number used in disconnection. */
     protected int disconnectSeqNo;
      /** If a FIN has been received, it is stored in disconnectRequest. */
      protected KtnDatagram disconnectRequest;
 
     /** Initialize variables to default values. */
     public AbstractConnection() {
         internalQueue = Collections.synchronizedList(new LinkedList<KtnDatagram>());
         externalQueue = Collections.synchronizedList(new LinkedList<KtnDatagram>());
         isReceiving = false;
         nextSequenceNo = (int)(Math.random() * 10000 + 1);
         disconnectRequest = null;
         lastDataPacketSent = null;
         lastValidPacketReceived = null;
         state = State.CLOSED;
     }
 
     /**
      * Construct a datagram with the given payload. <br>
      * <br>
      * Note: This method *depends* on the values of `remotePort',
      * `remoteAddress', `myPort', `myAddress' and `sequenceNo'. Failing to set
      * these before calling this method causes undefined behaviour. Also note
      * that if you want to set values to something else than the default, you
      * must construct the packet manually or alter the returned object.<br>
      * <br>
      * This method also increments the sequenceNo.<br>
      * <br>
      * This method sets the following fields:
      * <ol>
      * <li>Remote address
      * <li>Remote port
      * <li>Local address
      * <li>Local port
      * <li>Flag to NONE.
      * <li>Sequence no.
      * <li>Payload.
      * </ol>
      * 
      * @param payload
      *            Payload for packet, can not be null.
      * @return Initialised datagram.
      */
     protected KtnDatagram constructDataPacket(String payload) {
         if (payload == null) throw new IllegalArgumentException("Payload can not be null.");
 
         KtnDatagram packet = new KtnDatagram();
         packet.setDest_port(remotePort);
         packet.setDest_addr(remoteAddress);
         packet.setSrc_addr(myAddress);
         packet.setSrc_port(myPort);
         packet.setFlag(Flag.NONE);
         packet.setSeq_nr(nextSequenceNo++);
         packet.setPayload(payload);
 
         return packet;
     }
 
     /**
      * Construct a datagram with the given flag. <br>
      * <br>
      * Note: This method *depends* on the values of `remotePort',
      * `remoteAddress', `myPort', `myAddress' and `sequenceNo'. Failing to set
      * these before calling this method causes undefined behaviour. Also note
      * that if you want to set values to something else than the default, you
      * must construct the packet manually or alter the returned object.<br>
      * <br>
      * This method also increments the sequenceNo.<br>
      * <br>
      * This method sets the following fields:
      * <ol>
      * <li>Remote address
      * <li>Remote port
      * <li>Local address
      * <li>Local port
      * <li>Flag
      * <li>Sequence no.
      * <li>Payload to null.
      * </ol>
      * 
      * @param flag
      *            Flag for the packet, see {@link KtnDatagram.Flag}. Setting
      *            this to KtnDatagram.Flag.NONE constructs a packet with no flag
      *            or data, and makes no sense.
      * @return Initialised flagged datagram.
      */
     protected KtnDatagram constructInternalPacket(Flag flag) {
 
         KtnDatagram packet = new KtnDatagram();
         packet.setDest_port(remotePort);
         packet.setDest_addr(remoteAddress);
         packet.setSrc_addr(myAddress);
         packet.setSrc_port(myPort);
         packet.setFlag(flag);
         packet.setSeq_nr(nextSequenceNo++);
         packet.setPayload(null);
 
         return packet;
     }
 
     /**
      * Sends a packet. Hides the underlying ClSocket from the students, and is
      * there to clearify that there are not only the other send-methods that
      * exists.
      * 
      * @param packet
      *            The {@link KtnDatagram} to send.
      * @throws IOException
      *             If thrown by the underlying
      *             {@link ClSocket#send(KtnDatagram)}.
      * @throws ClException
      *             If thrown by the underlying
      *             {@link ClSocket#send(KtnDatagram)}.
      * @see #sendDataPacketWithRetransmit(KtnDatagram)
      * @see #sendAck(KtnDatagram, boolean)
      * @see ClSocket#send(KtnDatagram)
      */
     protected synchronized void simplySendPacket(KtnDatagram packet) throws ClException, IOException {
         new ClSocket().send(packet);
     }
 
     /**
      * Send a data packet and wait for ack in one operation. This method employs
      * a timer that resends the packet until an ack is received (or the timeout
      * is reached). <br>
      * <br>
      * This method sets the {@link #lastDataPacketSent} variable. This method
      * can only be used in the Established state, see {@link State}.
      * 
      * @param packet
      *            the packet to be sent.
      * @return The ack-package received for the send packet (NB: ack can be
      *         null)
      * @throws IOException
      *             thrown if unable to send packet.
      * @see no.ntnu.fp.net.cl.ClSocket#send(KtnDatagram)
      */
     protected synchronized KtnDatagram sendDataPacketWithRetransmit(KtnDatagram packet)
             throws IOException {
         if (state != State.ESTABLISHED)
             throw new IllegalStateException("Should only be used in ESTABLISHED state.");
         if (packet.getFlag() != Flag.NONE)
             throw new IllegalArgumentException("Packet must be a data packet.");
         /*
          * Algorithm: 1 Start a timer used to resend the packet with a specified
          * interval, and that immediately starts trying (sending the first
          * packet as well as the retransmits). 2 Wait for the ACK using
          * receiveAck(). 3 Cancel the timer. 4 Return the ACK-packet.
          */
 
         lastDataPacketSent = packet;
 
         // Create a timer that sends the packet and retransmits every
         // RETRANSMIT milliseconds until cancelled.
         Timer timer = new Timer();
         timer.scheduleAtFixedRate(new SendTimer(new ClSocket(), packet), 0, RETRANSMIT);
 
         KtnDatagram ack = receiveAck();
         timer.cancel();
 
         return ack;
     }
 
     /**
      * Send an ack or synack for the given packet. <br>
      * If the send fails, there is no retransmission of the ack-packet: Just
      * wait for the other side to retransmit the original packet.<br>
      * <br>
      * This method relies on
      * {@link #constructInternalPacket(Flag)}, i.e. myAddress,
      * myPort, remoteAddress, remotePort and sequenceNo must be initialized
      * properly.
      * 
      * @param packetToAck
      *            The packet that should be acked
      * @param synAck
      *            true if a synack should be sent, false if a regular ack, see
      *            {@link KtnDatagram.Flag}.
      * @throws ConnectException
      *             Thrown if unable to send packet.
      * @see #constructInternalPacket(Flag)
      */
     protected synchronized void sendAck(KtnDatagram packetToAck, boolean synAck)
             throws IOException, ConnectException {
     
         /*
          * Algorithm: Generate a new ack packet based on the packet given as
          * input Try to send the ack Catch a ConnectException if the sending
          * failed - and write this to the Log.
          */
     
         int tries = 3;
         boolean sent = false;
     
         KtnDatagram ackToSend = constructInternalPacket(synAck ? Flag.SYN_ACK : Flag.ACK);
         ackToSend.setAck(packetToAck.getSeq_nr());
     
         // Send the ack, trying at most `tries' times.
         Log.writeToLog(ackToSend, "Sending Ack: " + ackToSend.getAck(), "AbstractConnection");
     
         do {
             try {
                 new ClSocket().send(ackToSend);
                 sent = true;
             }
             catch (ClException e) {
                 Log.writeToLog(ackToSend, "CLException: Could not establish a "
                         + "connection to the specified address/port!", "AbstractConnection");
             }
             catch (ConnectException e) {
                 // Silently ignore: Maybe recipient was processing and didn't
                 // manage to call receiveAck() before we were ready to send.
                 try {
                     Thread.sleep(100);
                 }
                 catch (InterruptedException ex) {
                 }
             }
         }
         while (!sent && (tries-- > 0));
     
         if (!sent) {
             nextSequenceNo--;
             throw new ConnectException("Unable to send ACK.");
         }
     }
 
     /**
      * Receives a packet from the connectionless layer. This function handles
      * concurrency issues related to that only one thread may listen to a port
      * at the same time.<br>
      * <br>
      * It calls {@link #isValid(KtnDatagram)} on FIN-packets in ESTABLISHED
      * state, before an EOFException is thrown.
      * 
      * @param internal
      *            true if you want to receive non-external packet, i.e. not a
      *            packet with data intended for the application. False
      *            otherwise.
      * @return A received datagram
      * @throws IOException
      *             If the underlying connectionless layer throws an IOException.
      * @throws EOFException
      *             If a packet with a FIN-flag was received in ESTABLISHED
      *             state.
      */
     protected KtnDatagram receivePacket(boolean internal) throws IOException, EOFException {
         /*
          * Acquire monitor for this instance, and see if another thread runs
          * receive on our port. If so, see if that thread gets the packet that
          * was meant for us.
          */
         synchronized (this) {
             long before, after;
     
             before = System.currentTimeMillis();
             while (isReceiving) {
                 try {
                     if (internal)
                         wait(TIMEOUT); // wait with timeout
                     else wait(); // wait (potentially) forever
                 }
                 catch (InterruptedException e) { /* do nothing */
                 }
                 after = System.currentTimeMillis();
     
                 // If a packet for us has arrived, return it.
                 if (internal) {
                     // Case 1: Internal (protocol) caller, check internalQueue
                     if (!internalQueue.isEmpty()) {
                         return internalQueue.remove(0);
                     }
                     // If no packet arrived, see if timeout has expired.
                     else if ((after - before) > TIMEOUT) {
                         return null;
                     }
                 }
                 else {
                     // Case 2: Non-internal (application) caller, check
                     // externalQueue
                     if (!externalQueue.isEmpty()) {
                         return externalQueue.remove(0);
                     }
                     // else try loop again unless connection is free
                 }
             }
     
             // When we get here, this thread has not got its packet, and it's
             // allowed to enter the listening part of doReceive().
             isReceiving = true;
         }
     
         Log.writeToLog("Waiting for incoming packet in doReceive()", "AbstractConnection");
     
         KtnDatagram incomingPacket;
     
         /*
          * Waiting for internal and external packets should be handled
          * differently. Waiting for internal packets should time out.
          */
         if (internal) {
             // We are waiting for an internal packet, ie. a packet with a flag
             long start = System.currentTimeMillis();
             while (System.currentTimeMillis() - start < TIMEOUT) {
                 ClSocketReceiver receiver = new ClSocketReceiver(myPort);
                 receiver.start();
                 // Wait at most what is left of the TIMEOUT period for thread to
                 // die, but never less than 1 millisecond
                 try {
                     long timeout  = TIMEOUT - (System.currentTimeMillis() - start);
                     receiver.join(Math.max(timeout, 1));
                 }
                 catch (InterruptedException e) { /* do nothing */
                 }
     
                 receiver.stopReceive();
                 incomingPacket = receiver.getPacket();
                 if (incomingPacket == null) {
                     // No packet was received
                     synchronized (this) {
                         isReceiving = false;
                         notifyAll();
                         return null;
                     }
                 }
                 else {
                     // We have a packet
                     if (incomingPacket.getFlag() != Flag.NONE) {
                         // Packet is internal
                         Log.writeToLog("Received an internal packet in doReceive",
                                 "AbstractConnection");
     
                         if (incomingPacket.getFlag() == Flag.FIN && state == State.ESTABLISHED) {
                             // A FIN-packet has arrived in established state,
                             // stop receiving and throw and exception
                             disconnectRequest = incomingPacket;
                             synchronized (this) {
                                 isReceiving = false;
                                 notifyAll();
                                 throw new EOFException("FIN packet received.");
                             }
                         }
                         else {
                             // Not a FIN packet in established state, return it
                             // normally.
                             synchronized (this) {
                                 isReceiving = false;
                                 notifyAll();
                                 return incomingPacket;
                             }
                         }
                     }
                     else {
                         // Packet was meant for the application, continue
                         // listening until timeout.
                         Log.writeToLog("Received an external packet in doReceive",
                                 "AbstractConnection");
     
                         synchronized (this) {
                             synchronized (this) {
                                 externalQueue.add(incomingPacket);
                                 notifyAll();
                             }
                         }
                     }
                 }
             }
             // We have now waited at least TIMEOUT milliseconds, still no
             // packet.
             synchronized (this) {
                 isReceiving = false;
                 notifyAll();
                 return null;
             }
         }
         else {
             // We are waiting for a packet to an external application, ie. a
             // packet with no flags. Can possibly wait forever.
             incomingPacket = new ClSocket().receive(myPort);
             if (incomingPacket == null) {
                 // We should get a packet, try again.
                 synchronized (this) {
                     isReceiving = false;
                     notifyAll();
                 }
                 return receivePacket(internal);
             }
             else {
                 // We have a packet
                 if (incomingPacket.getFlag() != Flag.NONE) {
                     // Packet is internal
                     Log.writeToLog("Received an internal packet in doReceive", "AbstractConnection");
     
                     if (incomingPacket.getFlag() == Flag.FIN && state == State.ESTABLISHED) {
                         // A FIN-packet has arrived in established state,
                         // stop receiving and throw and exception
                         disconnectRequest = incomingPacket;
                         synchronized (this) {
                             isReceiving = false;
                             notifyAll();
                             throw new EOFException("FIN packet received.");
                         }
                     }
                     else {
                         // Not a FIN packet, continue listening
                         synchronized (this) {
                             internalQueue.add(incomingPacket);
                             isReceiving = false;
                             notifyAll();
                         }
                         return receivePacket(internal);
                     }
                 }
                 else {
                     // Packet was meant for the application, yei!
                     Log.writeToLog("Received an external packet in doReceive", "AbstractConnection");
     
                     synchronized (this) {
                         isReceiving = false;
                         notifyAll();
                         return incomingPacket;
                     }
                 }
             }
         }
     }
 
     /**
      * Waits for an ACK or SYN_ACK. Blocks until the ack is recieved. Returns
      * null if no ack recieved after the specified time, see
      * {@link AbstractConnection#TIMEOUT}. <br>
      * <br>
      * If a FIN-packet is received and the state is not ESTABLISHED, this will
      * also be returned.<br>
      * <br>
      * If a FIN-packet is received and the connection is in ESTABLISHED state,
      * an EOFException is thrown. It calls {@link #isValid(KtnDatagram)} on
      * FIN-packets in ESTABLISHED state, before an EOFException is thrown.
      * 
      * @return The ACK or SYN_ACK KtnDatagram recieved (can be null), may also
      *         be a FIN if not in established state.
      * @throws IOException
      *             If caused by the underlying connectionless layer.
      * @throws EOFException
      *             If a FIN-packet is received in ESTABLISHED state.
      */
     protected KtnDatagram receiveAck() throws IOException, EOFException {
         /*
          * Acquire monitor for this instance, and see if another thread runs
          * receive on our port. If so, see if that thread gets the packet that
          * was meant for us.
          */
         synchronized (this) {
             long before, after;
 
             before = System.currentTimeMillis();
             while (isReceiving) {
                 try {
                     wait(TIMEOUT); // wait with timeout
                 }
                 catch (InterruptedException e) { /* do nothing */
                 }
                 after = System.currentTimeMillis();
 
                 // If an ack for us has arrived, return it.
                 // check internalQueue
                 if (!internalQueue.isEmpty()) {
                     KtnDatagram packet = internalQueue.get(0);
                     if (packet.getFlag() == Flag.ACK || packet.getFlag() == Flag.SYN_ACK) {
                         return internalQueue.remove(0);
                     }
                 }
                 // If no packet arrived, see if timeout has expired.
                 else if ((after - before) > TIMEOUT) {
                     return null;
                 }
             }
 
             // When we get here, this thread has not got its packet, and it's
             // allowed to enter the listening part of doReceive().
             isReceiving = true;
         }
 
         Log.writeToLog("Waiting for incoming packet in receiveAck()", "AbstractConnection");
 
         KtnDatagram incomingPacket;
 
         // We are waiting for an ack or syn_ack packet
         long start = System.currentTimeMillis();
         while (System.currentTimeMillis() - start < TIMEOUT) {
             ClSocketReceiver receiver = new ClSocketReceiver(myPort);
             receiver.start();
             // Wait at most what is left of the TIMEOUT period for thread to
             // die, but never less than 1 millisecond
             try {
                 receiver.join(Math.max(TIMEOUT - (System.currentTimeMillis() - start), 1));
             }
             catch (InterruptedException e) { /* do nothing */
             }
 
             receiver.stopReceive();
             incomingPacket = receiver.getPacket();
             if (incomingPacket == null) {
                 // No packet was received
                 synchronized (this) {
                     isReceiving = false;
                     notifyAll();
                     return null;
                 }
             }
             else {
                 // We have a packet
                 if (incomingPacket.getFlag() != Flag.NONE) {
                     // Packet is internal
                     Log.writeToLog("Received an internal packet in receiveAck",
                             "AbstractConnection");
 
                     if (incomingPacket.getFlag() == Flag.FIN && state == State.ESTABLISHED) {
                         // A FIN-packet has arrived in established state,
                         // stop receiving and throw and exception
                         disconnectRequest = incomingPacket;
                         synchronized (this) {
                             isReceiving = false;
                             notifyAll();
                             throw new EOFException("FIN packet received.");
                         }
                     }
                     else if (incomingPacket.getFlag() == Flag.ACK
                             || incomingPacket.getFlag() == Flag.SYN_ACK
                             || incomingPacket.getFlag() == Flag.FIN) {
                         // Not a FIN packet in established state, return if it
                         // is SYN, SYN_ACK or FIN
                         synchronized (this) {
                             isReceiving = false;
                             notifyAll();
                             return incomingPacket;
                         }
                     }
                     else {
                         // Not a packet we want to return, continue looping.
                         synchronized (this) {
                             internalQueue.add(incomingPacket);
                             notifyAll();
                         }
                     }
                 }
                 else {
                     // Packet was meant for the application, continue
                     // listening until timeout.
                     Log.writeToLog("Received an external packet in receiveAck",
                             "AbstractConnection");
 
                     synchronized (this) {
                         synchronized (this) {
                             externalQueue.add(incomingPacket);
                             notifyAll();
                         }
                     }
                 }
             }
         }
         // We have now waited at least TIMEOUT milliseconds, still no
         // packet.
         synchronized (this) {
             isReceiving = false;
             notifyAll();
             return null;
         }
     }
 
     /**
      * Test a packet for transmission errors. This function should only called
      * in the ESTABLISHED state.
      * 
      * @param packet
      *            Packet to test.
      * @return true if packet is free of errors, false otherwise.
      */
     protected abstract boolean isValid(KtnDatagram packet);
 }
