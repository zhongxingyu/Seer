 package edu.uw.cs.cse461.Net.Base;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.lang.Math;
 
 /*************
  * Runnable server thread for a single socket.
  * Waits for a UDP packet to be received, then sends a packet of a given size to the same client.
  * Makes no guarantees what the packet's contents will be.
  * Call end() to signal the thread to shut down; this may take up to TIMEOUT ms to occur. 
  * @author mmattb
  *
  */
 public class UDPDataThread implements DataThreadInterface {
 
 	private int _timeOut;    //time in MS between checks to see if its time to shut down
 	private int _portNumber;					  //port number to receive connections in
 	private DatagramSocket _dSocket; 		      //a server socket for the current available connection
 	private boolean _timeToClose = false;      //flag set when end() is called to signal the thread to shut down
 	private int _xferSize;						  //size of server response in bytes
 	private final static int MAX_PACKET_SIZE = 1000;	      //size of the largest packet in bytes; we will split _xferSize
 												  //  bytes across packets no larger than this.	
 
 	/*************
 	 * Constructs a new thread, but does not start it running
 	 * @param portNumber - a valid network port number in this domain
 	 * @param xferSize - the size in bytes of the response to be sent
 	 */
 	public UDPDataThread(int portNumber, int xferSize, int timeout){
 		_portNumber = portNumber;
 		_xferSize = xferSize;
 		_timeOut = timeout;
 		System.out.println("UDPDataThread constructor: server set up at port: " + portNumber);
 	}
 	
 
 	/*************
 	 * Starts this running in its own thread if called using the Java Thread functionality.
 	 * Returns prematurely if the socket cannot be set up properly.  This can happen
 	 *   for many reasons, including:
 	 *     - _portNumber is not available
 	 *     - an exception is thrown while the socket TIMEOUT is being set (perhaps a bad value)
 	 * Once the socket is made, it will wait for a UDP packet to arrive.  If this occurs,
 	 *   it will send a packet of size _xferSize bytes to the client.  There are no guarantees
 	 *   what the contents of the packet will be.
 	 * Every TIMEOUT ms, the thread will check the _timeToClose flag to see if its time to shut
 	 *   this server thread down.  The flag can be set to 'true' by a call to end() from this or 
 	 *   another thread.
 	 */
 	@Override
 	public void run() {
 		//attempt to set up socket on target port number
 		try {
 			_dSocket = new DatagramSocket(_portNumber);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return;
 		}
 		
 		//attempt to set the timeout between successive checks of _timeToClose flag
 		try {
 			_dSocket.setSoTimeout(_timeOut);
 		} catch (SocketException e) {
 			e.printStackTrace();
 			return;
 		}
 		
 		System.out.println("UDPDataThread.run: server started at port: " + _portNumber);
 		
 		boolean error = false;
 		boolean timedOut = false;
 		//loop until time to close
 		while(true){
 			byte byteBuffer[] = new byte[2];
 			DatagramPacket p = new DatagramPacket(byteBuffer,byteBuffer.length);
 			//Code hangs on .receive() until a UDP packet is received or
 			// TIMEOUT ms have passed.
 			try {
 				_dSocket.receive(p);
 			} catch (SocketTimeoutException e) {
 				timedOut = true;
 			} catch (IOException e) {
 				System.err.println("UDPDataThread.run: IOException on accept.");
 				error = true;
 			} 
 			
 			//if we recieved a packet, then send a response back to the client
 			if(!error && !timedOut){
 				try {
 					System.out.println("UDPDataThread.run: transmitting message of length " + _xferSize + " to " + p.getSocketAddress());
 					
 					int bytesLeft = _xferSize;
 					while(bytesLeft>0){
 						int bufferLen = Math.min(bytesLeft,MAX_PACKET_SIZE);
 						byte buffer[] = new byte[bufferLen];
 						
 						DatagramPacket message = new DatagramPacket(buffer, bufferLen);
 						message.setAddress(p.getAddress());
						message.setPort(p.getPort());
 						_dSocket.send(message);
 						
 						bytesLeft -= bufferLen;
 					}
 					
 					
 					System.out.println("UDPDataThread.run: message transmitted.");
 				} catch (IOException e1) {
 					System.err.println("UDPDataThread.run: trouble sending response");
 					e1.printStackTrace();
 				}
 				
 			} 
 			
 			
 			//check if time to close
 			if(_timeToClose){
 				_dSocket.close();
 				return;				
 			}
 			
 			error = false;
 			timedOut = false;
 		}
 		
 	}
 	
 	/*************
 	 * Call this function from the parent process to end this server thread.
 	 * May take up to TIMEOUT ms until the thread checks this value and shuts down.
 	 */
 	public void end(){
 		_timeToClose = true;
 	}
 }
 
