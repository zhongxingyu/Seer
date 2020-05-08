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
  * @author Sebjrn Birkeland and Stein Jakob Nordb
  * @see no.ntnu.fp.net.co.Connection
  * @see no.ntnu.fp.net.cl.ClSocket
  */
 public class ConnectionImpl extends AbstractConnection {
 	
 	private InternalReceiver rec;
 
 	private int retries = 10;
 	
     /** Keeps track of the used ports for each server port. */
     private static Map<Integer, Boolean> usedPorts = Collections.synchronizedMap(new HashMap<Integer, Boolean>());
 
     /**
      * Initialise initial sequence number and setup state machine.
      * 
      * @param myPort
      *            - the local port to associate with this connection
      * @throws IOException 
      */
     public ConnectionImpl(int myPort){
     	super();
     	if (!usedPorts.containsKey(myPort) || usedPorts.get(myPort) != true){
     		this.usedPorts.put(myPort, true);
             this.myPort = myPort;
             this.myAddress = getIPv4Address();
     	}
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
         if (state != State.CLOSED)
         	throw new IllegalStateException("WTF! Y u no close before connect?");
         
     	this.remoteAddress = remoteAddress.getHostAddress();
     	this.remotePort = remotePort;
     	
     	KtnDatagram synAck = sendSyn();
     	lastValidPacketReceived = synAck;
     	sendAck(synAck, false);
     	state = State.ESTABLISHED;
     }
     
     private synchronized KtnDatagram sendSyn()
             throws IOException{
     
         KtnDatagram synToSend = constructInternalPacket(Flag.SYN);
         // Send the syn, trying at most `tries' times.
         Log.writeToLog(synToSend, "Sending Syn: " + synToSend.getSeq_nr(), "AbstractConnection");
        for(int i = 0; i<=retries; i++){
         	try{
         	    simplySendPacket(synToSend);
         	} catch (ClException e) {
         		continue;
         	}
         	state = State.SYN_SENT;
         	KtnDatagram acc = receiveAck();
         	if (isValid(acc)){
         		return acc;
         	}
         }
         throw new SocketTimeoutException("Syn did not reach it's destination.");
     }
 
     /**
      * Listen for, and accept, incoming connections.
      * 
      * @return A new ConnectionImpl-object representing the new connection.
      * @see Connection#accept()
      */
     public Connection accept() throws IOException, SocketTimeoutException {
     	if (state != State.CLOSED)
     		throw new IllegalStateException("WTF! Y u no close before accept?");
     	state = State.LISTEN;
     	
    	KtnDatagram packet = receivePacket(false);
         
         ConnectionImpl c = new ConnectionImpl(randomPort());
         c.state = State.SYN_RCVD;
         c.remoteAddress = packet.getSrc_addr();
         c.remotePort = packet.getSrc_port();
         c.lastValidPacketReceived = packet;
 
        for (int i = 0; i<=retries; i++){
             c.sendAck(packet, true);
             KtnDatagram acc = c.receiveAck();
             if(c.isValid(acc)){
             	c.state = State.ESTABLISHED;
             	c.lastValidPacketReceived = acc;
             	return c;
             }
         }
         state = State.CLOSED;
         throw new SocketTimeoutException("Sorry Mate, this didn't work out");
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
         KtnDatagram packetToSend = constructDataPacket(msg);
        for(int i = 0; i<=retries; i++){
             KtnDatagram ackPacket = sendDataPacketWithRetransmit(packetToSend);
             if(isValid(ackPacket)){
             	this.lastValidPacketReceived = ackPacket;
             }
         }
         throw new IOException("Timed out!");
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
     	while(true){
     	    KtnDatagram data = receivePacket(false);
     	    if(isValid(data)){
     	    	if(data.getFlag() == Flag.NONE){
     	    	    lastValidPacketReceived = data;
     	    	   	sendAck(lastValidPacketReceived, false);
     	    	   	return (String) data.getPayload();
     	    	} else {
     	    		sendAck(lastValidPacketReceived, false);
     	    	}
     	    } else {
     	    	sendAck(lastValidPacketReceived, false);
     	    }
     	}
     }
 
     /**
      * Close the connection.
      * 
      * @see Connection#close()
      */
     public void close() throws IOException {
     	if(disconnectRequest != null){
     		close_wait();
     	} else {
     		fin1();
     	}
     }
     
     private void close_wait() throws IOException{
     	state = State.CLOSE_WAIT;
 	    KtnDatagram fin = constructInternalPacket(Flag.FIN);
		for(int i = 0; i<=retries; i++){
     	    sendAck(disconnectRequest, false);
     	    state = State.LAST_ACK;
 			try {
 			    Thread.sleep(20);
 				simplySendPacket(fin);
 				KtnDatagram finacc = receiveAck();
 				if(isValid(finacc)){
 					state = State.CLOSED;
 					return;
 				}
 			} catch (ClException e) {
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
     	    
 		}  
 		throw new IOException("Oh my, I can't help!");
     }
     
     
     private void fin1() throws IOException{
 		state = State.FIN_WAIT_1;
 		KtnDatagram fin = constructInternalPacket(Flag.FIN);
		for(int i = 0; i<=retries; i++){
 			try {
 				simplySendPacket(fin);
 				KtnDatagram finacc = receiveAck();
 				if(isValid(finacc)){
 					fin2();
 				}
 			} catch (ClException e) {
 				e.printStackTrace();
 			}
 		}    	
     }
     
     private void fin2() throws IOException {
 		state = State.FIN_WAIT_2;
		for(int i = 0; i<=retries; i++){
 	    	KtnDatagram fin = receivePacket(true);
     		if (fin != null && isValid(fin)){
     			sendAck(fin, false);
     			state = State.CLOSED;
     			return;
     		}
 		}
 		state = State.CLOSED;
     	throw new IOException("Oh my!");
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
     	if (!(	(packet != null)
     			&& (packet.getDest_port() == myPort)
     			&& (packet.getDest_addr() == myAddress))
     			&& (packet.getChecksum() == packet.calculateChecksum())
     			&& (packet.getSeq_nr() == lastValidPacketReceived.getSeq_nr() + 1)) {
     		return false;
     	}
     	switch(packet.getFlag()) {
     	case NONE:
     		return (state == State.ESTABLISHED)
     				&& (packet.getSrc_addr().equals(remoteAddress))
    				 	&& (packet.getSrc_port() == remotePort);
     	case SYN:
     		return (state == State.LISTEN);
     	case FIN:
     		return (state == State.FIN_WAIT_2)
     				&& (packet.getSrc_addr().equals(remoteAddress))
    				 	&& (packet.getSrc_port() == remotePort);
     	case ACK:
     		return ((state == State.ESTABLISHED)
     				|| (state == State.FIN_WAIT_1)
     				|| (state == State.SYN_RCVD))
     				|| (state == State.CLOSE_WAIT)
     				&& (packet.getAck() == lastDataPacketSent.getSeq_nr())
     				&& (packet.getSrc_addr().equals(remoteAddress))
     				&& (packet.getSrc_port() == remotePort);
     	case SYN_ACK:
     		return (state == State.SYN_SENT)
     				 && (packet.getSrc_addr().equals(remoteAddress))
     				 && (packet.getSrc_port() == remotePort);
     	}
     	return false;
     }
     
     protected int randomPort(){
     	Random rnd = new Random();
     	int p = 1024;
     	if (!usedPorts.containsKey(p) || usedPorts.get(p) != true){
         	p = rnd.nextInt(2048)+1024;
     	}
     	return p;
     }
     
 }
