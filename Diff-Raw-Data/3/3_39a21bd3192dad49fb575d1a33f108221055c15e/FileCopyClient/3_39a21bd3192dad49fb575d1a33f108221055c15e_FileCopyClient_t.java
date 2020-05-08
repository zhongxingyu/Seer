 /* FileCopyClient.java
  Version 0.1 - Muss erg�nzt werden!!
  Praktikum 3 Rechnernetze BAI4 HAW Hamburg
  Autoren:
  */
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 
 public class FileCopyClient extends Thread {
 
 	// -------- Constants
 	public final static boolean TEST_OUTPUT_MODE = false;
 
 	public final int SERVER_PORT = 23000;
 
 	public final int UDP_PACKET_SIZE = 1024;
 
 	// -------- Public parms
 	public String servername;
 
 	public String sourcePath;
 
 	public String destPath;
 
 	public int windowSize;
 
 	public long serverErrorRate;
 
 	// -------- Variables
 	// current default timeout in nanoseconds
 	private long timeoutValue = 100000000L;
 
 	private SendBuffer buffer;
 	
 	private DatagramSocket socket;
 	
 	private InetAddress server;
 	
 	private long sumOfRtts=0;
 	private long noOfRtts=0;
 	
 	private long noOfTimersTimedOut=0;
 	
 	public long invalidRtts=0;
 	
     private long estimatedRTT = -1;
     
     private long deviation = 0;
 
 	// Constructor
 	public FileCopyClient(String serverArg, String sourcePathArg,
 			String destPathArg, String windowSizeArg, String errorRateArg) {
 		servername = serverArg;
 		sourcePath = sourcePathArg;
 		destPath = destPathArg;
 		windowSize = Integer.parseInt(windowSizeArg);
 		serverErrorRate = Long.parseLong(errorRateArg);
 
 	}
 
 	public void runFileCopyClient() {
 		List<FCpacket> packetList = new ArrayList<FCpacket>();
 		packetList.add(makeControlPacket());
 		packetList.addAll(FileCopyUtils.readFileToPackets(sourcePath,
 				UDP_PACKET_SIZE));
 		System.out.println("Listsize: " + packetList.size());
 		try {
 			server = InetAddress.getByName(servername);
 			socket = new DatagramSocket();
 			ClientReceiver receiver = new ClientReceiver(socket);
 			this.buffer = new SendBuffer(receiver, packetList, windowSize,
 					server,this);
 			Date startTime=new Date();
 			receiver.setDaemon(true);
 			receiver.start();
 			buffer.setDaemon(true);
 			buffer.start();
 			sendPacket(packetList.get(0));
 			BlockingQueue<FCpacket> queue=buffer.getQueue();
 			boolean finished=false;
 			while (!finished) {
 				FCpacket packet=queue.take();
 				if (packet.getSeqNum()==-1)
 					finished=true;
 				else if(!packet.isValidACK()){
 					sendPacket(packet);
 				}
 				
 			}
 			long timeouts=noOfTimersTimedOut;
 			receiver.interrupt();
 			Date stopTime=new Date();
 			if (!socket.isClosed())
 				socket.close();
 			System.out.println("Done");
 			System.out.println("Transfering the file took " + (stopTime.getTime()-startTime.getTime()) + " ms");
 			System.out.println("No of timers timed out: " + timeouts);
 			System.out.println("Average RTT: " + sumOfRtts/noOfRtts);
 			System.out.println("Invalid RTTs: " + invalidRtts);
 			System.out.println("Last timeout value: " + timeoutValue);
 		} catch (SocketException e) {
 			e.printStackTrace();
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 	
 	private void sendPacket(FCpacket packet) {
 		DatagramPacket outgoing;
 		outgoing = new DatagramPacket(packet.getSeqNumBytesAndData(),
 				packet.getSeqNumBytesAndData().length, server,
 				FileCopyServer.SERVER_PORT);
 		testOut("Sending packet: " + packet.getSeqNum());
		packet.setTimestamp(System.nanoTime());
 		try {
 			socket.send(outgoing);
 		} catch (IOException e) {
 			testOut("Error sending packet: " + e.getMessage());
 		}
 		if(!packet.isValidACK()){
 			startTimer(packet);
 		}
 	}
 	
 	/**
 	 * 
 	 * Timer Operations
 	 */
 	public void startTimer(FCpacket packet) {
 		/* Create, save and start timer for the given FCpacket */
 		FC_Timer timer = new FC_Timer(timeoutValue, this, packet.getSeqNum());
 		packet.setTimer(timer);
 		timer.start();
 	}
 
 	public void cancelTimer(FCpacket packet) {
 		/* Cancel timer for the given FCpacket */
 		testOut("Cancel Timer for packet" + packet.getSeqNum());
 		if (packet.getTimer() != null) {
 			packet.getTimer().interrupt();
 		}
 	}
 
 	/**
 	 * Implementation specific task performed at timeout
 	 */
 	public void timeoutTask(long seqNum) {
 		noOfTimersTimedOut++;
 		buffer.addPacket(seqNum);
 	}
 
 	/**
 	 * 
 	 * Computes the current timeout value (in nanoseconds)
 	 */
 	public void computeTimeoutValue(long sampleRTT) {
 		sumOfRtts+=sampleRTT;
 		noOfRtts++;
 
 		if (estimatedRTT < 0) {
             estimatedRTT = sampleRTT;
         }
 
         double x = 0.1;
 
         estimatedRTT = (long) ((1 - x) * estimatedRTT + x * sampleRTT);
 
         //Deviation verstanden als sicherer Abstand
         deviation = (long) ((1 - x) * deviation + x * Math.abs(sampleRTT - estimatedRTT));
         timeoutValue = estimatedRTT + 4 * deviation;
 		testOut("New timeout value: " + timeoutValue);
 	}
 
 	/**
 	 * 
 	 * Return value: FCPacket with (0 destPath;windowSize;errorRate)
 	 */
 	public FCpacket makeControlPacket() {
 		/*
 		 * Create first packet with seq num 0. Return value: FCPacket with (0
 		 * destPath ; windowSize ; errorRate)
 		 */
 		String sendString = destPath + ";" + windowSize + ";" + serverErrorRate;
 		byte[] sendData = null;
 		try {
 			sendData = sendString.getBytes("UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		return new FCpacket(0, sendData, sendData.length);
 	}
 
 	public void testOut(String out) {
 		if (TEST_OUTPUT_MODE) {
 			System.err.printf("%,d %s: %s\n", System.nanoTime(), Thread
 					.currentThread().getName(), out);
 		}
 	}
 
 	public static void main(String argv[]) throws Exception {
 		FileCopyClient myClient = new FileCopyClient(argv[0], argv[1], argv[2],
 				argv[3], argv[4]);
 		myClient.runFileCopyClient();
 	}
 
 }
