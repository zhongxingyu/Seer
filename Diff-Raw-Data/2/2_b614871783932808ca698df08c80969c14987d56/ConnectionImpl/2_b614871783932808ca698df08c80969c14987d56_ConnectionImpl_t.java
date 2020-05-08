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
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 import no.ntnu.fp.net.admin.Log;
 import no.ntnu.fp.net.cl.ClException;
 import no.ntnu.fp.net.cl.ClSocket;
 import no.ntnu.fp.net.cl.KtnDatagram;
 import no.ntnu.fp.net.cl.KtnDatagram.Flag;
 
 /**
  * Implementation of the Connection-interface. <br>
  * <br>
  * This class implements the behaviour in the methods specified in the interface
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
 
     /** Keeps track of the used ports for each server port. */
     private static Map<Integer, Boolean> usedPorts = Collections.synchronizedMap(new HashMap<Integer, Boolean>());
     
     
     
     /**
      * Initialise initial sequence number and setup state machine.
      * 
      * @param myPort
      *            - the local port to associate with this connection
      */
     public ConnectionImpl(int myPort) {
     	this.myPort = myPort;
     	this.myAddress = getIPv4Address();
     }
 
     private String getIPv4Address() {
         try {
             return InetAddress.getLocalHost().getHostAddress();
         }
         catch (UnknownHostException e) {
             return "127.0.0.1";
         }
     }
 
     /**
      * Establish a connection to a remote location.
      * 
      * @param remoteAddress
      *            - the remote IP-address to connect to
      * @param remotePort
      *            - the remote portnumber to connect to
      * @throws IOException
      *             If there's an I/O error.
      * @throws java.net.SocketTimeoutException
      *             If timeout expires before connection is completed.
      * @see Connection#connect(InetAddress, int)
      */
     public void connect(InetAddress remoteAddress, int remotePort) throws IOException,
             SocketTimeoutException {
     	this.remoteAddress = remoteAddress.getHostAddress();
     	this.remotePort = remotePort;
     	
     	KtnDatagram syn = constructInternalPacket(Flag.SYN);
     	try {
 			simplySendPacket(syn);
 			System.out.println("sent syn");
 		} catch (ClException e) {
 			e.printStackTrace();
 		}
     	
     	KtnDatagram synAck = receiveAck();
     	if (synAck.getFlag() == Flag.SYN_ACK && isValid(synAck)) {
     		sendAck(synAck, false);
     		this.state = State.ESTABLISHED;
     	}
     	
     }
 
     /**
      * Listen for, and accept, incoming connections.
      * 
      * @return A new ConnectionImpl-object representing the new connection.
      * @see Connection#accept()
      */
     public Connection accept() throws IOException, SocketTimeoutException {
     	this.state=State.LISTEN;
     	KtnDatagram datagram = null;
     	
     	do {
     		datagram = receivePacket(true);
     	} while (datagram == null || datagram.getFlag()!=Flag.SYN);
     	
     	ConnectionImpl newConn = new ConnectionImpl(myPort);
     	this.state = State.SYN_RCVD;
     	newConn.fillConnfields(datagram.getSrc_port(), datagram.getSrc_addr());
     	newConn.sendAck(datagram, true);
     	this.state = State.SYN_SENT;
     	
     	//listen for final ack
     	KtnDatagram finalAck = receiveAck();
     	if(finalAck.getFlag()==Flag.ACK){
     		this.state = State.ESTABLISHED;
     		return newConn;
     	} else {
     		throw new IOException();
     	}
     }
 
     /**
      * Send a message from the application.
      * 
      * @param msg
      *            - the String to be sent.
      * @throws ConnectException
      *             If no connection exists.
      * @throws IOException
      *             If no ACK was received.
      * @see AbstractConnection#sendDataPacketWithRetransmit(KtnDatagram)
      * @see no.ntnu.fp.net.co.Connection#send(String)
      */
     public void send(String msg) throws ConnectException, IOException {
         if (this.state != State.ESTABLISHED) {
         	throw new ConnectException();
         }
         
         KtnDatagram ack;
         do
         	ack = sendDataPacketWithRetransmit(constructDataPacket(msg));
        while ( ack == null );
         
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
         KtnDatagram datagram = receivePacket(false);
     	sendAck(datagram, false);
     
         return (String)datagram.getPayload();
     }
 
     /**
      * Close the connection.
      * 
      * @see Connection#close()
      */
     public void close() throws IOException {
     	throw new NotImplementedException();
     }
 
     /**
      * Test a packet for transmission errors. This function should only called
      * with data or ACK packets in the ESTABLISHED state.
      * 
      * @param packet
      *            Packet to test.
      * @return true if packet is free of errors, false otherwise.
      */
     protected boolean isValid(KtnDatagram packet) {
         return (packet.calculateChecksum() == packet.getChecksum());
     }
     private void fillConnfields(int remotePort, String remoteAddress){
     	this.remotePort=remotePort;
     	this.remoteAddress=remoteAddress;
     }
 }
