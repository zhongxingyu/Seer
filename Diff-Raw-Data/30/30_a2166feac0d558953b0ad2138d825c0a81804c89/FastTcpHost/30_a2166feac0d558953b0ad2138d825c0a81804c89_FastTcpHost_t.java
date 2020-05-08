 package netproj.hosts.fasttcp;
 
 import java.util.LinkedList;
 import java.util.Queue;
 
 import netproj.hosts.Host;
 import netproj.skeleton.Packet;
 
 /**
  * A FastTCP sender/receiver Host.
  * 
  * Sends "files" of certain size through the network using the FastTCP congestion control algorithm.
  * 
  * @author emil
  */
 public class FastTcpHost extends Host {
 	private static int PACKET_SIZE = 1024 * 8;
 	private int windowSize = 20;
 	private long timeout = 2000;
 	private long lastId = 0;
 	private ResenderThread resenderThread;
 	private Queue<FastTcpPacket> outstandingPackets;
 	private long baseRtt = Long.MAX_VALUE;
 	
 	// Roundtrip stats
 	private double An = -1, Dn = -1;
 	
 	public FastTcpHost(int inputBuffSize, int outputBuffSize, int address) {
 		super(inputBuffSize, outputBuffSize, address);
 		
 		outstandingPackets = new LinkedList<FastTcpPacket>();
 		resenderThread = new ResenderThread();
 		resenderThread.start();
 	}
 
 	/**
 	 * Sends a file through the network.
 	 * 
 	 * Starts a new session and sends {@code size} bits to {@code dest}.
 	 */
 	public void sendFile(int dest, int size) {
 		// Reset stats for a new session
 		baseRtt = Long.MAX_VALUE;
 		timeout = 2000;
		windowSize = 1;
 		
		for (int i = 0; i < Math.ceil(size / (double) PACKET_SIZE); ) {
			
 			synchronized (outstandingPackets) {
 				if (outstandingPackets.size() < windowSize) {
 					FastTcpPacket packet = new FastTcpPacket(getAddress(), dest, PACKET_SIZE,
 							                                 lastId++);
					i++;
 					sendPacket(packet);
 					outstandingPackets.add(packet);
 					outstandingPackets.notifyAll();
 				} else {
 					// Wait until an ACK is received
 					try {
 						outstandingPackets.wait();
 					} catch (InterruptedException e) {
 						// This should not happen!
 						assert(false);
 					}
 				}
 			}
 		}
 		synchronized (outstandingPackets) {
 			while (!outstandingPackets.isEmpty()) {
 				try {
 					outstandingPackets.wait();
 				} catch (InterruptedException e) {
 
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void processPacket(Packet packet) {
 		if (packet.getDestAddress() == getAddress() && packet instanceof FastTcpPacket) {
 			FastTcpPacket fastPacket = (FastTcpPacket) packet;
 			if (fastPacket.isAck()) {
 				// Update roundtrip stats
 				if (An < -0.5) {
 					// This is the first reply
 					An = Dn = fastPacket.getRtt();
 				} else {
 					double t = fastPacket.getRtt();
 					An = 0.5 * An + 0.5 * t;
 					Dn = 0.5 * Dn + 0.5 * Math.abs(t - An);
 				}
 				timeout = (long) Math.ceil(An + 4 * Dn);
 				if (fastPacket.getRtt() < baseRtt) {
 					baseRtt = fastPacket.getRtt();
 				}
 				double gamma = 0.5;
				double alpha = 5;
 				if (fastPacket.getId() % 10 == 5) {
 					windowSize = Math.min(2*windowSize, (int) ((1-gamma) * windowSize + gamma * (baseRtt/An * windowSize + alpha)));
 				}
 				
 				synchronized (outstandingPackets) {
 					for (FastTcpPacket p : outstandingPackets) {
 						if (p.getId() == fastPacket.getId()) {
 							outstandingPackets.remove(p);
 							outstandingPackets.notifyAll();
 							break;
 						}
 					}
 				}
 			} else {
 				sendPacket(fastPacket.acknowledge());
 			}
 		}
 	}
 	
 	@Override
 	public void handleCommand(String command) {
 		if (command.equals("help")) {
 			super.handleCommand("help");
 			System.out.println("send <address> <size> - sends <size> bits to <address>");
 		} else if (command.startsWith("send")) {
 			int address = Integer.decode(command.split(" ")[1]);
 			int size = Integer.decode(command.split(" ")[2]);
 			long time = System.currentTimeMillis();
 			sendFile(address, size);
 			time = System.currentTimeMillis() - time;
 			System.out.println("transfer completed in " + time + "ms");
			System.out.println("bps: " + size * 1000 / time);
 			
 		} else {
 			super.handleCommand(command);
 		}
 	}
 	
 	class ResenderThread extends Thread {
 		@Override
 		public void run() {
 			while (!isInterrupted()) {
 				synchronized (outstandingPackets) {
 					try {
 						if (outstandingPackets.isEmpty()) {
 							outstandingPackets.wait();
 							continue;
 						}
 						if (outstandingPackets.peek().getRtt() < timeout) {
 							outstandingPackets.wait(timeout - outstandingPackets.peek().getRtt());
 							continue;
 						}
 						FastTcpPacket packet = outstandingPackets.poll();
 						packet.reset();
 						sendPacket(packet);
 						outstandingPackets.add(packet);
 					} catch (InterruptedException e) {
 						break;
 					}
 				}
 			}
 		}
 	}
 }
