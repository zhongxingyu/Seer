 /*
  * Created on Oct 27, 2004
  */
 package no.ntnu.fp.net.co;
 
 import java.io.EOFException;
 import java.io.File;
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
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
     
     private final static int INITIAL_PORT = 10000;
     private final static int PORT_RANGE = 100;
     private final static int RETRIES = 1;
 
     private static boolean shouldInitPortNumbers = true;
     
     private KtnDatagram datagram;
     /**
      * Initialise initial sequence number and setup state machine.
      * 
      * @param myPort
      *            - the local port to associate with this connection
      */
     public ConnectionImpl(int myPort) {
     	initPortNumbers();
     	this.myAddress = getIPv4Address();
     	datagram = new KtnDatagram();
     	this.myPort = myPort;
     }
     
     /**
      * 
      * 
      * @param packet
      * @throws ConnectException
      * @throws IOException
      */
     public ConnectionImpl(KtnDatagram packet) throws ConnectException, IOException {
     	initPortNumbers();
     	this.myAddress = getIPv4Address();
     	this.myPort = packet.getDest_port();
     	this.remotePort = packet.getSrc_port();
     	this.remoteAddress = packet.getSrc_addr();
     	this.lastValidPacketReceived = packet;
     	
     	// Send SYN-ACK
     	synchronized (this) {
     		try {
    			wait(500);
     		} catch (InterruptedException e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     		}
 		}
     	sendAck(this.lastValidPacketReceived, true);
     	this.state = State.SYN_RCVD;
     	// Wait for ACK
     	this.lastValidPacketReceived = internalReceiveAck(false, this.lastValidPacketReceived);
     	this.state = State.ESTABLISHED;
     	
 //    	this.lastValidPacketReceived = receiveAck();
 //    	if(this.lastValidPacketReceived.getFlag() != Flag.ACK) {
 //    		this.state = State.CLOSED;
 //    		throw new ConnectException("Did not receive ACK for sent SYN-ACK");
 //    	}
 //    	this.state = State.ESTABLISHED;
     }
 
     public static void initPortNumbers() {
     	if(shouldInitPortNumbers) {
 	    	for (int i = INITIAL_PORT; i < INITIAL_PORT + PORT_RANGE; i++) {
 				usedPorts.put(i, false);
 			}
     	}
     }
     
     public String getIPv4Address() {
         try {
         	Enumeration<NetworkInterface> networkInterfaces = java.net.NetworkInterface.getNetworkInterfaces();
         	while(networkInterfaces.hasMoreElements()){
 				Enumeration<InetAddress> networkAddresses = networkInterfaces.nextElement().getInetAddresses();
 				while(networkAddresses.hasMoreElements()){
 					String address = networkAddresses.nextElement().getHostAddress();
 					if(address.contains(".") && !address.equals("127.0.0.1")){
 						return address;
 					}
 				}
         	}
         	return InetAddress.getLocalHost().getHostAddress();
         }
         catch (UnknownHostException e) {
         	return "127.0.0.1";
         } catch (SocketException e) {
         	return "127.0.0.1";
 		}
     }
 
     public KtnDatagram internalReceiveAck(boolean synAck, KtnDatagram packetToAck) throws EOFException, IOException {
     	KtnDatagram temp;
     	for (int i = 0; i < RETRIES; i++) {
     		System.out.println("Waiting for ACK: " + i);
     		temp = receiveAck();
     		if(temp == null) {
     			System.out.println("ACK was null");
     		} else {
     			System.out.println("Received packet with flag: " + temp.getFlag().toString() + " and seq.number: " + temp.getSeq_nr());
     		}
     		if(temp != null && (synAck && temp.getFlag() == Flag.SYN_ACK || !synAck)) {
     			return temp;
     		} 
 //				else if (packetToAck.getSeq_nr() != temp.getSeq_nr()){
 //					
 //				}
     	}
     	throw new SocketTimeoutException();
     }
     
     public KtnDatagram internalReceive(Flag flag, boolean internal) throws EOFException, IOException {
     	KtnDatagram temp = null;
     	for (int i = 0; i < RETRIES; i++) {
     		if(flag == Flag.FIN) {
     			System.out.println("Waiting for FIN");
     		}
     		temp = receivePacket(internal);
 			if(temp != null && temp.getFlag() == flag) {
 				return temp;
 			} 
     	}
     	throw new SocketTimeoutException();
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
         KtnDatagram synPacket = constructInternalPacket(Flag.SYN);
         try {
 			simplySendPacket(synPacket);
 		} catch (ClException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         this.state = State.SYN_SENT;
         KtnDatagram ack = internalReceiveAck(true, synPacket);
         if(ack == null){
         	throw new SocketTimeoutException("Did not receive SYN-ACK!");
         }
         this.lastValidPacketReceived = ack;
         this.remotePort = this.lastValidPacketReceived.getSrc_port();
         sendAck(this.lastValidPacketReceived, false);
         this.state = State.ESTABLISHED;
     }
     
     /**
      * Creates the local folder "Log" if it doesn't already exist.
      * Creates the file "Log/logfile.txt" if it doesn't already exist.
      */
     public static void fixLogDirectory() {
     	File log = new File("Log");
     	if(!log.isDirectory()) {
     		log.mkdir();
     	}
     	String name = "logfile";
     	String path = "Log/" + name + ".txt";
     	File logFile = new File(path);
     	int counter = 0;
     	String newName = "";
     	while(logFile.exists()) {
     		name = path.substring(4, path.length() - 4);
     		newName = "Log/" + name + ++counter + ".txt";
     		logFile = new File(newName);
     	}
     	File ordFile = new File(path);
     	if(ordFile.exists() ) {
     		if(ordFile.length() == 0) {
     			return;
     		}
     		ordFile.renameTo(logFile);
     		ordFile = new File(path);
     	}
     	try {
     		ordFile.createNewFile();
     	} catch (IOException e) {
     		e.printStackTrace();
     	}
     }
     
     private static int getNextPortNumber() throws IOException {
     	for (int i = INITIAL_PORT; i < INITIAL_PORT + PORT_RANGE; i++) {
 			if(!usedPorts.get(i)) {
 				usedPorts.put(i, true);
 				return i;
 			}
 		}
     	throw new IOException("Out of ports!");
     }
     
     public static void serverMain(int port) {
     	ConnectionImpl c = new ConnectionImpl(port);
     	try {
     		System.out.println("Listening on port " + port);
     		Connection con = c.accept();
     		System.out.println("Connection established! " + con.toString());
     		while(true){
     			String msg = con.receive();
     			System.out.println("Message: " + msg);
     		}
     	} catch (SocketTimeoutException e) {
     		// TODO Auto-generated catch block
     		e.printStackTrace();
     	} catch (IOException e) {
     		// Do nothing
     	}
     	System.out.println("Closed");
     }
     
     public static void clientMain(String address, int port) {
     	ConnectionImpl c = new ConnectionImpl(INITIAL_PORT - 1);
     	System.out.println("Your IP-address is: " + c.getIPv4Address());
     	try {
     		System.out.println("Trying to connect to " + address + " on port " + port);
     		c.connect(Inet4Address.getByName(address), port);
     		System.out.println("Connection established!");
     		Scanner scanner = new Scanner(System.in);
     		while(true){
     			System.out.print("Type something to send: ");
 	    		String msg = scanner.nextLine();
 	    		if (msg.equals("quit")){
 	    			break;
 	    		}
 	    		try {
 	    			c.send(msg);
 	    		} catch(SocketTimeoutException e) {
 	    			System.out.println(e);
 	    			System.out.println("Could not send packet, please try again!");
 	    		}
     		}
     		System.out.println("Closing...");
     		c.close();
     	} catch (SocketTimeoutException e) {
     		// TODO Auto-generated catch block
     		e.printStackTrace();
     	} catch (IOException e) {
     		// TODO Auto-generated catch block
     		e.printStackTrace();
     	}
     	System.out.println("Finished!");
     }
     
     /**
      * Listen for, and accept, incoming connections.
      * 
      * @return A new ConnectionImpl-object representing the new connection.
      * @see Connection#accept()
      */
     public Connection accept() throws IOException, SocketTimeoutException {
     	while(true) {
 	    	// Receive SYN
     		try {
     			this.lastValidPacketReceived = this.internalReceive(Flag.SYN, true);
     		} catch (SocketTimeoutException e) {
     			continue;
     		}
 		    this.lastValidPacketReceived.setDest_port(getNextPortNumber());
 		    ConnectionImpl conn = new ConnectionImpl(this.lastValidPacketReceived);
 		    return conn;
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
     public void send(String msg) throws ConnectException, IOException, SocketTimeoutException {
     	int timeoutCounter = 0;
     	KtnDatagram ack = null;
     	KtnDatagram packet = constructDataPacket(msg);
     	packet.setChecksum(packet.calculateChecksum());
     	do {
     		if(timeoutCounter > RETRIES * 2) {
     			throw new SocketTimeoutException();
     		}
     		try {
     			System.out.println("STATE: " + this.state.toString());
     			this.
 				simplySendPacket(packet);
 				ack = internalReceiveAck(false, packet);
 			} catch (ClException e) {
 				ack = null;
 				System.out.println("Header error! Resending..");
 				continue;
 			} catch (SocketTimeoutException e) {
 				ack = null;
 			}
 			timeoutCounter++;
     	} while(ack == null || ack.getAck() != packet.getSeq_nr());
     	// La til dette den 20.03:14.37
     	this.lastValidPacketReceived = ack;
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
     	if(this.state != State.ESTABLISHED) {
     		throw new IOException("Connection is closed!");
     	}
     	boolean shouldThrowException = true;
     	while(this.state == State.ESTABLISHED)  {
 	    	try{
 	    		KtnDatagram packet = receivePacket(false);
 	    		if(packet.getSeq_nr() != this.lastValidPacketReceived.getSeq_nr() + 1 || !isValid(packet)) {
 	    			System.out.println("Corrupted or unexpected package!");
 	    			sendAck(this.lastValidPacketReceived, false);
 	    		} else {
 	    			this.lastValidPacketReceived = packet;
 	    			synchronized (this) {
 	    				try {
 							wait(200);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 					}
 	    			sendAck(this.lastValidPacketReceived, false);
 	    			return packet.getPayload().toString();
 	    		}
 	    	}catch (EOFException e){
 	    		serverClose();
 	    		shouldThrowException = false;
 	    	}
     	}
     	if(shouldThrowException) {
     		throw new IOException("Connection died while waiting for packet!");
     	}
     	return null;
     }
 
     /**
      * Close the connection.
      * 
      * @see Connection#close()
      */
     public void close() throws IOException {
     	KtnDatagram fin = constructInternalPacket(Flag.FIN);
     	KtnDatagram ack = null;
     	while(true) {
 	    	do {
 	    		try {
 					simplySendPacket(fin);
 				} catch (ClException e) {
 					continue;
 				}
 				System.out.println("Waiting for ACK with seq.number = " + fin.getSeq_nr());
 	    		ack = internalReceiveAck(false, fin);
 	    	} while(ack == null || ack.getAck() != fin.getSeq_nr());
 	    	this.lastValidPacketReceived = ack;
 	    	try{
 	    		fin = internalReceive(Flag.FIN, true);
 	    	}
 	    	catch (EOFException e) {
 	    		System.out.println("RECEIVED FIN!");
 	    		break;
 	    	}
 	    	catch (SocketTimeoutException e){
 	    		continue;
 	    	}
     	}
     	while(true){
     		synchronized (this) {
 	    		try {
 	    			wait(200);
 	    		} catch (InterruptedException e1) {
 	    			e1.printStackTrace();
 	    		}
     		}
 	    	sendAck(this.disconnectRequest, false);
 	    	try{
 	    		this.state = State.CLOSE_WAIT;
 	    		fin = internalReceive(Flag.FIN, true);
 	    	}
 	    	catch (EOFException e) {
 	    		continue;
 	    	}
 	    	catch (SocketTimeoutException e){
 	    		this.state = State.CLOSED;
 	    		return;
 	    	}
     	}
     }
     
     public void serverClose(){
     	while(true) {
     		try {
     			if (disconnectRequest.getSeq_nr() != this.lastValidPacketReceived.getSeq_nr() + 1){
     				sendAck(this.lastValidPacketReceived, false);
     				return;
     			}
     			synchronized (this) {
     				try {
     					System.out.println("STARTING WAIT");
     					wait(200);
     					System.out.println("ENDING WAIT");
     				} catch (InterruptedException e1) {
     					e1.printStackTrace();
     				}
     			}
     			sendAck(disconnectRequest, false);
     			KtnDatagram fin = constructInternalPacket(Flag.FIN);
     			KtnDatagram ack = null;
     			while(true){
     				if (ack != null && fin.getSeq_nr() == ack.getAck()){
     					this.state = State.CLOSED;
     					// TODO: Sjekk at porten faktisk blir frigjort!
     					ConnectionImpl.usedPorts.put(this.myPort, false);
     					return;
     				}
     				synchronized (this) {
     		    		try {
     		    			wait(200);
     		    		} catch (InterruptedException e1) {
     		    			e1.printStackTrace();
     		    		}
     	    		}
     				simplySendPacket(fin);
     				try {
     					ack = internalReceiveAck(false, fin);
     					continue;
     				} catch (EOFException e) {
     					continue;
     				}
     			}
     		} catch (ConnectException e) {
     			System.out.println("Could not send FIN!");;
     		} catch (IOException e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     		} catch (ClException e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     		}
     	}
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
     	return packet.getChecksum() == packet.calculateChecksum();
     }
 
 }
 
