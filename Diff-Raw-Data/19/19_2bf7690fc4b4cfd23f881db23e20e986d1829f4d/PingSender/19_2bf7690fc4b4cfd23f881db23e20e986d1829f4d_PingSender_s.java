 package nz.ac.auckland.netlogin;
 
 import nz.ac.auckland.cs.des.Key_schedule;
 import nz.ac.auckland.cs.des.desDataOutputStream;
 import java.io.IOException;
 import java.lang.Exception;
 import java.lang.InterruptedException;
 import java.lang.String;
 import java.lang.Thread;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 
 public class PingSender extends Thread {
 
 	private DatagramSocket s = null;
 	private int authRef = -1;
 	private int outToken = 0;
 	private InetAddress host;
 	private int port;
 	private Key_schedule schedule = null; // set up encryption key to the users old passwd
 	private int sequenceNumber = 0;
 	private volatile boolean stop = false;
 	private volatile int outstandingPings = 0;
 	private PingListener netLogin;
 
 	public PingSender(String host, int port, PingListener netLogin) throws IOException {
 		try {
 			s = new DatagramSocket(); // Allocate a datgram socket
 		} catch ( Exception e ) {
 			throw new IOException("Error creating DatagramSocket: " + e);
 		}
 		this.host = InetAddress.getByName(host);
 		this.port = port;
 		this.netLogin = netLogin;
 	}
 
 	public void prepare(Key_schedule schedule, int authRef, int outToken, int sequenceNumber){
 		this.schedule = schedule;
 		this.authRef = authRef;
 		this.outToken = outToken;
 		this.sequenceNumber = sequenceNumber;
 	}
 
 	public DatagramSocket getSocket() {
 		return s;
 	}
 
 	public void stopPinging() {
 		DatagramPacket sendPacket;
 		desDataOutputStream packet = new desDataOutputStream(128);
 		desDataOutputStream des_out = new desDataOutputStream(128);
 		
 		byte outputBufferEncrypted[];
 		byte messageBytes[];
 
 		try{
 			// Tell gate were disconnecting
 			des_out.writeInt(outToken);
 			des_out.writeInt(sequenceNumber + 10000);
 
 			outputBufferEncrypted = des_out.des_encrypt(schedule);
 
 			packet.writeInt(authRef);
 			packet.write(outputBufferEncrypted, 0, outputBufferEncrypted.length);
 			messageBytes = packet.toByteArray();
 			sendPacket = new DatagramPacket(messageBytes, messageBytes.length, host, port);
 
 			s.send(sendPacket);
 		} catch(Exception e){
 			// ignored
 		}
 
 		stop = true;
 		interrupt();
 	}
 
 	public int getOutstandingPings() {
 		return outstandingPings;
 	}
 
 	public void zeroOutstandingPings() {
 		outstandingPings = 0;
 	}
 
	public void sendMessage( String user, String message ){
		DatagramPacket sendPacket;
		desDataOutputStream packet = new desDataOutputStream(8192);
		byte messageBytes[];

		try {
			packet.writeInt(-1);					// -1 tells Pingd this is a message
			packet.writeBytes("senduser " + user + " " + message + " ");
			messageBytes = packet.toByteArray();
			sendPacket = new DatagramPacket(messageBytes, messageBytes.length, host, port);
			s.send(sendPacket);
		} catch (IOException e) {
			System.out.println( "nz.ac.auckland.netlogin.PingSender: Error sending message" );
		}
	}

 	public synchronized void run( ) {
 		desDataOutputStream packet = new desDataOutputStream(128);
 		desDataOutputStream desOut = new desDataOutputStream(128);
 		int missedPings = 0;
 
 		setPriority(Thread.MAX_PRIORITY / 4);
 		while (!stop && outstandingPings < 5 && missedPings < 10) {
 
 			try {
 				desOut.writeInt(outToken);
 				desOut.writeInt(sequenceNumber);
 				byte[] outputBufferEncrypted = desOut.des_encrypt(schedule); // encrypt buffer
 
 				packet.writeInt(authRef);
 				packet.write(outputBufferEncrypted, 0, outputBufferEncrypted.length);
 
 				byte[] messageBytes = packet.toByteArray();
 				DatagramPacket sendPacket = new DatagramPacket(messageBytes, messageBytes.length, host, port);
 				s.send(sendPacket);
 
 				sequenceNumber++;
 				outstandingPings++;
 				missedPings = 0;
 			} catch (IOException e) {
				System.out.println("nz.ac.auckland.netlogin.PingSender: Error sending ping packet");
 				missedPings++; // Ignore it at least 10 times in a row
 			}
 
 			// clear the buffers so we can reuse them
 			desOut.reset();
 			packet.reset();
 
 			// Sleep for 10 seconds
 			try {
 				sleep(10000);
 			} catch (InterruptedException e) {
 				// stopPinging wants us to stop.
 			}
 		}
 		
 		try {
 			s.close();
 		} catch (Exception e) {
 			System.err.println("Error closing socket: " + e);
 		}
 		
 		netLogin.disconnected();
 
 	}
 }
