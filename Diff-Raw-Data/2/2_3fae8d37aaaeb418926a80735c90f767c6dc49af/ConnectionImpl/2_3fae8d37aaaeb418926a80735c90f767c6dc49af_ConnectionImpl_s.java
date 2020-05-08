 /*
  * Created on Oct 27, 2004
  */
 package no.ntnu.fp.net.co;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.InetAddress;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 import no.ntnu.fp.net.admin.Log;
 import no.ntnu.fp.net.cl.ClException;
 import no.ntnu.fp.net.cl.ClSocket;
 import no.ntnu.fp.net.cl.KtnDatagram;
 import no.ntnu.fp.net.cl.KtnDatagram.Flag;
 
 /**
  * Implementation of the Connection-interface. <br> <br> This class implements
  * the behaviour in the methods specified in the interface
  * {@link Connection} over the unreliable, connectionless network realised in
  * {@link ClSocket}. The base class, {@link AbstractConnection} implements some
  * of the functionality, leaving message passing and error handling to this
  * implementation.
  *
  * @author Sebj�rn Birkeland and Stein Jakob Nordb�
  * @see no.ntnu.fp.net.co.Connection
  * @see no.ntnu.fp.net.cl.ClSocket
  */
 public class ConnectionImpl extends AbstractConnection {
 
     /**
      * Keeps track of the used ports for each server port.
      */
     private static Map<Integer, Boolean> usedPorts = Collections.synchronizedMap(new HashMap<Integer, Boolean>());
 
     /**
      * Initialise initial sequence number and setup state machine.
      *
      * @param myPort - the local port to associate with this connection
      */
     public ConnectionImpl(int myPort) {
         super();
         this.myPort = myPort;
         this.myAddress = getIPv4Address();
         usedPorts.put(new Integer(myPort), true);
     }
 
     private String getIPv4Address() {
         try {
             return InetAddress.getLocalHost().getHostAddress();
         } catch (UnknownHostException e) {
             return "127.0.0.1";
         }
     }
 
     public ConnectionImpl(String myAddress, int myPort, String remoteAddress, int remotePort, int nextSequenceNo) {
         super();
         this.myAddress = myAddress;
         this.myPort = myPort;
         this.remoteAddress = remoteAddress;
         this.remotePort = remotePort;
         this.nextSequenceNo = nextSequenceNo;
         this.state = State.ESTABLISHED;
     }
 
     /**
      * Establish a connection to a remote location.
      *
      * @param remoteAddress - the remote IP-address to connect to
      * @param remotePort - the remote portnumber to connect to
      * @throws IOException If there's an I/O error.
      * @throws java.net.SocketTimeoutException If timeout expires before
      * connection is completed.
      * @see Connection#connect(InetAddress, int)
      */
     public void connect(InetAddress remoteAddress, int remotePort) throws IOException,
             SocketTimeoutException {
 
         /*
          * Sets necessary addressing info
          */
         this.remoteAddress = remoteAddress.getHostAddress();
         this.remotePort = remotePort;
 
         try {
             myAddress = InetAddress.getLocalHost().getHostAddress();
         } catch (UnknownHostException e) {
             e.printStackTrace();
         }
 
         /*
          * Constructs the port and sends the SYN package
          */
         myPort = 1024 + (int) (Math.random() * 64000);
         usedPorts.put(myPort, true);
 
         /*
          * Creats and sends the SYN request
          */
         KtnDatagram first = constructInternalPacket(Flag.SYN);
 
         try {
             simplySendPacket(first);
             this.state = State.SYN_SENT;
         } catch (ClException ex) {
         }
 
         /*
          * Expects and tries to receive an ACK
          */
         /*
          * Tries twice before giving up
          */
         KtnDatagram second = receiveAck();
 
         if (second != null) {
         } else if (second == null) {
             second = receiveAck();
         }
 
         /*
          * SYN_ACK received, connection set up
          */
         if (second != null && second.getFlag() == Flag.SYN_ACK) {
             sendAck(second, false);
             this.state = State.ESTABLISHED;
         } else {
             this.state = State.CLOSED;
             throw new IOException("Timeout: Never received SYN_ACK");
         }
 
         this.remotePort = second.getSrc_port();
         this.remoteAddress = second.getSrc_addr();
     }
 
     /**
      * Listen for, and accept, incoming connections.
      *
      * @return A new ConnectionImpl-object representing the new connection.
      * @see Connection#accept()
      */
     public Connection accept() throws IOException, SocketTimeoutException {
 
         if( this.state != State.CLOSED ) {
             throw new IllegalStateException("Cannot accept new connections, connection not closed");
         }
         this.state = State.LISTEN;
 
         /*
          * Loops and listens for a connection until it gets one
          */
         KtnDatagram firstPacket = null;
         while (this.state == State.LISTEN) {
             firstPacket = receivePacket(true);
             if (firstPacket != null && firstPacket.getFlag() == Flag.SYN) {
                 /*
                  * SYN received - setting information and changing state
                  */
                 this.state = State.SYN_RCVD;
                 remotePort = firstPacket.getSrc_port();
                 remoteAddress = firstPacket.getSrc_addr();
             }
         }
 
         try {
             myAddress = InetAddress.getLocalHost().getHostAddress();
         } catch (UnknownHostException e) {
             e.printStackTrace();
         }
 
         sendAck(firstPacket, true);
 
         /*
          * Waits for the ACK to return. Gives it two attempts before it shuts
          * down
          */
         KtnDatagram ack = receiveAck();
 
         /*
          * TODO: Figure out a way to handle sequence numbers
          */
         if (ack == null) {
             ack = receiveAck();
         }
         /*
          * Should consider a better behaviour
          */
         if (ack == null) {
            this.state = State.LISTEN;
             return accept();
         } else if (ack != null) {
             this.state = State.ESTABLISHED;
             this.remotePort = ack.getSrc_port();
             this.remoteAddress = ack.getSrc_addr();
         }
 
         usedPorts.put(myPort, Boolean.TRUE);
 
         /*
          * Reserves the port and creates the connection
          */
         Connection newConn = new ConnectionImpl(myAddress, myPort, remoteAddress, remotePort, nextSequenceNo);
         this.state = State.CLOSED;
         return newConn;
     }
 
     /**
      * Send a message from the application.
      *
      * @param msg - the String to be sent.
      * @throws ConnectException If no connection exists.
      * @throws IOException If no ACK was received.
      * @see AbstractConnection#sendDataPacketWithRetransmit(KtnDatagram)
      * @see no.ntnu.fp.net.co.Connection#send(String)
      */
     public void send(String msg) throws ConnectException, IOException {
 
         if (this.state != State.ESTABLISHED) {
             throw new IllegalStateException("Cannot send from a non-established state");
         }
         /*
          * Sends the data package with retransmission enabled
          */
         try {
             sendDataPacketWithRetransmit(constructDataPacket(msg));
         } catch (IOException e) {
             throw new IOException("Broken link");
         }
     }
 
     /**
      * Wait for incoming data.
      *
      * @return The received data's payload as a String.
      * @see Connection#receive()
      * @see AbstractConnection#receivePacket(boolean)
      * @see AbstractConnection#sendAck(KtnDatagram, boolean)
      */
     public String receive() throws ConnectException, IOException {
         /*
          * Receives the packet and returns an ACK
          */
         
         if (this.state != State.ESTABLISHED) {
             throw new IllegalStateException("Tried to receive in a non-connected state");
         }
         KtnDatagram packet = null;
 
         packet = receivePacket(false);
 
 
         /*
          * Tests the incoming packages for validity
          */
         if (isValid(packet)) {
             sendAck(packet, false);
             return packet.getPayload().toString();
         }
         return receive();
     }
 
     /**
      * Close the connection.
      *
      * @see Connection#close()
      */
     public void close() throws IOException {
 
         if (this.state == State.CLOSED || this.state == State.SYN_SENT || this.state == State.SYN_RCVD ) {
             throw new IllegalStateException("Cannot close from this state");
         }
 
         /*
          * Handle passive disconnect
          */
         if (this.disconnectRequest != null) {
             /*
              * Got FIN, sending ACK
              */
             this.state = State.CLOSE_WAIT;
             sendAck(this.disconnectRequest, false);
 
             /*
              * Sending FIN, waiting for ACK
              */
             KtnDatagram fin = constructInternalPacket(Flag.FIN);
             this.disconnectSeqNo = fin.getSeq_nr();
 
             try {
                 simplySendPacket(fin);
                 this.state = State.LAST_ACK;
                 KtnDatagram ack = receiveAck();
                 while (ack != null) {
                     /*
                      * Got wrong ACK
                      */
                     ack = receiveAck();
                 }
             } catch (ClException e) {
                 /*
                  * Do nothing
                  */
             }
             this.state = State.CLOSED;
             return;
         }
 
         /*
          * Handles active disconnect
          */
         KtnDatagram fin = constructInternalPacket(Flag.FIN);
         try {
             /*
              * Builds and sends a FIN packet
              */
             simplySendPacket(fin);
             this.state = State.FIN_WAIT_1;
             this.disconnectSeqNo = fin.getSeq_nr();
 
         } catch (ClException ex) {
             
         }
 
         KtnDatagram ack = receiveAck();
         this.state = State.FIN_WAIT_2;
         /*
          * Receives the ACK
          */
         /*
          * Handles a wrongly sequenced ACK
          */
         if (ack != null && ack.getSeq_nr() != this.nextSequenceNo) {
             this.state = State.ESTABLISHED;
             close();
             return;
         }
         /*
          * Receives FIN
          */
         KtnDatagram packet = receivePacket(true);
         if (packet == null) {
         } else if (packet.getFlag() == Flag.FIN) {
             try {
                 sendAck(packet, false);
             } catch (ConnectException e) {
                 System.out.println("Could not send ACK after FIN");
                 e.printStackTrace();
             }
         }
         this.state = State.TIME_WAIT;
         
         // Wait grace period
         
         this.state = State.CLOSED;
         
     }
 
     /**
      * Test a packet for transmission errors. This function should only called
      * with data or ACK packets in the ESTABLISHED state.
      *
      * @param packet Packet to test.
      * @return true if packet is free of errors, false otherwise.
      */
     protected boolean isValid(KtnDatagram packet) {
         if (packet.getSrc_port() != remotePort) {
             return false;
         }
 
         if ((packet.getSrc_addr() + ".").equals(remoteAddress)) {
             return false;
         }
 
         if (packet.getChecksum() != packet.calculateChecksum()) {
             return false;
         }
 
         if (lastValidPacketReceived != null && packet.getSeq_nr() == lastValidPacketReceived.getSeq_nr()) {
             return false;
         }
 
         if (lastValidPacketReceived != null && packet.getSeq_nr() - 1 != lastValidPacketReceived.getSeq_nr()) {
             return false;
         }
 
         return true;
     }
 }
