 package com.pjf.mat.util.comms;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 
 import org.apache.log4j.Logger;
 
 import com.pjf.mat.api.InMsgCallbackInt;
 
 
 public class UDPCxn {
 		private final static Logger logger = Logger.getLogger(UDPCxn.class);
 		private static final int SKT_TMO_MS = 500;
    	private DatagramSocket skt;
     	private InetAddress dstIP;
     	private final int LOCAL_PORT = 3500;
     	private boolean shutdown;
     	private boolean sktInUse;
     	private InMsgCallbackInt loopbackCb = null;
 
     	public UDPCxn(long dstIPadr) throws SocketException, UnknownHostException {
 			byte[] target = new byte[4];
 			target[0] = (byte) ((dstIPadr >> 24) & 0xff);
 			target[1] = (byte) ((dstIPadr >> 16) & 0xff);
 			target[2] = (byte) ((dstIPadr >> 8) & 0xff);
 			target[3] = (byte) (dstIPadr & 0xff);
 			dstIP = InetAddress.getByAddress(target);
 			initialise();
     	}
 
 
 		public UDPCxn(String dstIPadr) throws SocketException, UnknownHostException {
     		String[] parts = dstIPadr.split("[.]");
     		if (parts.length == 4) {
     			// treat as numeric IP
     			byte[] target = new byte[4];
     			for (int i = 0; i<4; i++) {
     				target[i] = (byte) Integer.parseInt(parts[i]);
     			}
     			dstIP = InetAddress.getByAddress(target);
     		} else {
     			// treat as named host
     			dstIP = InetAddress.getByName(dstIPadr);    			
     		}
 			initialise();
     	}
 
 		/**
 		 * Common initialisation
 		 * 
 		 * @throws SocketException
 		 */
     	private void initialise() throws SocketException {
     		skt = new DatagramSocket(LOCAL_PORT);
     		skt.setSoTimeout(SKT_TMO_MS);
     		shutdown = false;
     		sktInUse = false;
 		}
 
     	/**
     	 * Set the object that should receive outgoing messages
     	 * 
     	 * @param cb	callback object
     	 */
     	public void setLoopbackCallback(InMsgCallbackInt cb) {
     		logger.info("Loopback mode set to " + cb);
     		this.loopbackCb = cb;
     	}
 
     	public void send(byte[] data, int port) throws IOException {
     		DatagramPacket pkt = new DatagramPacket(data, data.length, dstIP, port);
 			logger.debug("Msg sent (port=" + port + "):  [" + toHexString(data) + "]");
 			if (loopbackCb != null) {
 				// perform the loopback
 				loopbackCb.processIncomingMsg(port, data);
 			} else {
 				skt.send(pkt);
 			}
     	}
     	
     	public void fixSend(String str, int port, boolean print) throws IOException {
     		String s1 = str.replace('~','\001');
     		byte[] data = s1.getBytes();
     		DatagramPacket pkt = new DatagramPacket(data, data.length, dstIP, port);
 			logger.debug("Msg sent (port=" + port + "):  [" + toHexString(data) + "]");
     		skt.send(pkt);   		
     	}
 
     	/**
     	 * Receive one datagram. Abort if cxn is shutdown
     	 * 
     	 * @return
     	 * @throws IOException
     	 */
     	public RxPkt rcv() throws IOException {
   	      	byte[] buf = new byte[1500];
 	  	    DatagramPacket pkt = new DatagramPacket(buf, buf.length);
 	  	    boolean gotPkt = false;
 			byte[] data = new byte[0];
 			int port = 0;
 			logger.debug("Waiting to receive ...");
 			sktInUse = true;
 			while (!gotPkt  &&  !shutdown) {
 				try {
 					skt.receive(pkt);
 					gotPkt = true;
 				    int len = pkt.getLength();
 				    byte[] rd = pkt.getData();
 				    port = pkt.getPort();
 		  	      	data = new byte[len];
 				    for (int i=0; i<len; i++) {
 				    	data[i] = rd[i];
 				    }
 					logger.info("Msg received (port=" + pkt.getPort() + "):  [" + toHexString(data) + "]");
 					} catch (SocketTimeoutException e) {
 						// ignore timeout and just keep going
 					}
 			}
 			if (shutdown) {
 				logger.info("Shutting down.");
 			}
 			sktInUse = false;
 		    return new RxPkt(port,data);
     	}
     	
     	public void close() {
     		shutdown = true;
     		logger.debug("Waiting for skt to be not in use...");
     		while (sktInUse) {
     			try {
 					Thread.sleep(200);
 				} catch (InterruptedException e) {
 					sktInUse = false;
 				}
      		}
     		logger.debug("Closing socket.");
    			skt.close(); 		
     	}
     	    	
     	private String toHexString(byte[] rep) {
     		StringBuffer buf = new StringBuffer();
     		char[] map = new char[] {'0', '1', '2', '3', '4', '5', '6', '7',
     								 '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
     		for (byte b : rep) {
     			int d = b & 0xff;
     			buf.append(map[(d >> 4) & 0xf]);
     			buf.append(map[d & 0xf]);
     			buf.append(' ');
     		}
     		return buf.toString();
     	}
 
 		public String getIp() {
 			return dstIP.toString();
 		}
 
 	}
