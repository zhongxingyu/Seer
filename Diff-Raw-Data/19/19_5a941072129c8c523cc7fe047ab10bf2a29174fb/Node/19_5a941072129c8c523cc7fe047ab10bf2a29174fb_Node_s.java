 package com.nick.Paxos.Network;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Vector;
 
 import com.nick.Paxos.Data;
 import com.nick.Paxos.Paxos;
 import com.nick.Paxos.UpdateLog;
 import com.nick.Paxos.Messages.AcceptedNotificationMessage;
 import com.nick.Paxos.Messages.CatchUpRequestMessage;
 import com.nick.Paxos.Messages.ElectionMessage;
 import com.nick.Paxos.Messages.HeartbeatMessage;
 import com.nick.Paxos.Messages.LeaderHeartbeatMessage;
 import com.nick.Paxos.Messages.NewLeaderMessage;
 import com.nick.Paxos.Messages.PaxosMessage;
 import com.nick.Paxos.Network.Leader.LeaderListener;
 import com.nick.Paxos.Network.Leader.LeaderProcessor;
 
 
 
 public class Node extends Thread {
 
 	
 	// Multicast group and port
 	private MulticastSocket msocket;
 	private static final String GROUP = "239.0.0.1";
 	private static final int PORT = 1234;
 	
 	private final static int LEADER_REPLIES_PORT = 1235;	
 	private final static int LEADER_LISTENER_PORT = 1236;
 	private final static int LEADER_SYNC_PORT = 1237;
 
 	private boolean isLearner = false;
 	
 	// Heartbeat processing
 	public HeartbeatSender heartbeatSender;
 	public HeartbeatProcess heartbeatListener;
 	
 	// List of online nodes and timeout timers
 	public HashMap<InetAddress, Integer> nodeTimers;
 	public Vector<InetAddress> nodes;
 	
 	public boolean running;
 	public Data data = new Data();
 	
 	private int DEFAULT_TIMER = 6;
 	
 	// Heartbeat Types
 	private int LEADER_HB = 1;
 	private int NORMAL_HB = 0;
 	
 	boolean isLeader = false;
 	private static InetAddress leaderAddress = null;
 	
 	private InetAddress localAddress;
 	
 	public LeaderProcessor leaderProc;
 	public LeaderListener leaderListener;
 	
 	public Node() {	
 		
 		try {
 			 this.msocket = new MulticastSocket(PORT);
 			 this.msocket.joinGroup(InetAddress.getByName(GROUP));
 			 this.localAddress = InetAddress.getLocalHost();
 		} catch (IOException e) {
 			System.err.println("Error: Could not create multicast socket.");
 			Paxos.cleanExit();
 		}
 		
 		this.nodes = new Vector<InetAddress>();
 		this.nodeTimers = new HashMap<InetAddress, Integer>();
 		this.running = true;
 	}
 	
 	public void run() {
 		
 		DatagramPacket packet = null;
 		Object obj = null;
 		
 		checkForLeader();
 		
 		this.heartbeatListener = new HeartbeatProcess();
 		this.heartbeatListener.start();
 		
 		while(running) {
 			
 			packet = receiveMessage();
 			obj = SerializationUtil.deSerialize(packet.getData());
 		
 			if(obj!= null){		
 				if(obj instanceof PaxosMessage) {
 					if(!isLearner){
 						data.process(obj);
 					} else if(obj instanceof AcceptedNotificationMessage) {
 						// TODO: Append at the end of UpdateLog
 					}
 				} else if (obj instanceof HeartbeatMessage) {	
 					if(!packet.getAddress().equals(localAddress) ) {
 						resetNodeTimer(packet.getAddress());
 						if(obj instanceof LeaderHeartbeatMessage) {
 							//System.out.println("New leader hb. Should sync..");
 							LeaderHeartbeatMessage lhbm = (LeaderHeartbeatMessage) SerializationUtil.deSerialize(packet.getData());
 							if(lhbm.getLatestRound() > data.getLargestSeqNumber()) {
 								this.isLearner = true;
 								System.out.println("Missing rounds. Will sync with leader..");
 								// TODO: Move to another thread.
 								requestCatchUp(new CatchUpRequestMessage(data.getLargestSeqNumber()));
 								try {
 									DatagramSocket syncFromLeader = new DatagramSocket(LEADER_LISTENER_PORT);
 									byte[] buf = new byte[2048];
 									DatagramPacket syncPacket = new DatagramPacket(buf,buf.length);
 									syncFromLeader.receive(syncPacket);
 									UpdateLog log = (UpdateLog) SerializationUtil.deSerialize(syncPacket.getData());
 									for(int i=0;i<log.getSeqNumbers().size();i++){
 										Paxos.node.data.addFromSync(log.getSeqNumbers().elementAt(i), log.getCommands().get(log.getSeqNumbers().elementAt(i)));
 									}
 									isLearner = false;
 								} catch (SocketException e) {
 									e.printStackTrace();
 								} catch (IOException e) {
 									e.printStackTrace();
 								}
 							}
 						}
 					}			
 				} else if (obj instanceof ElectionMessage) {
 					leaderAddress = null;
 					ElectionMessage em = (ElectionMessage) obj;
 					if(em.getProcn() < Paxos.getProcn()) {
 						System.out.println("Election message from lower process, bullying..");
 						sendMessageToGroup(new ElectionMessage(Paxos.getProcn()));
 					} else {
 						System.out.println("Election message from higher process, will wait for new leader..");
 					}
 						Object _obj;
 						DatagramPacket _packet;
 						while(leaderAddress == null) {
 							_packet=receiveMessage();
 							_obj = SerializationUtil.deSerialize(_packet.getData());
 							if(_obj instanceof NewLeaderMessage) {
 								leaderAddress = _packet.getAddress();
 							}
 					
 					}
 				} 
 			} else {
 				System.out.println("Error: Could not receive packet.");
 			}		
 		}
 	}
 	
 	
 	private void checkForLeader() {
 		
 		int secondsRemaining = 5; // Listen for 5 seconds for a leader
 		DatagramPacket pkt ;
 		Object _obj;
 		byte[] _buff = new byte[512];
 		while(!isLeader && leaderAddress == null && secondsRemaining > 0) {
 			pkt = new DatagramPacket(_buff, _buff.length);
 			_obj = null;
 			try {
 				msocket.setSoTimeout(1000);
 			} catch (SocketException e1) {
 				e1.printStackTrace();
 			}
 			pkt = receiveMessage();
 			
 			if(pkt != null) {
 				_obj = SerializationUtil.deSerialize(pkt.getData());	 
 					
 				if(_obj != null && _obj instanceof LeaderHeartbeatMessage) {
 					leaderAddress = pkt.getAddress();
 				}
 			}	
 			secondsRemaining--;
 		}
 		
 		if(!isLeader && leaderAddress == null) { 
 					
 			becomeLeader();
 			
 		} else {
 			System.out.println("Leader already exists..");
 			this.heartbeatSender = new HeartbeatSender(NORMAL_HB);
 			this.heartbeatSender.start();	
 		}
 		
 		try {
 			msocket.setSoTimeout(0);
 		} catch (SocketException e1) {
 			System.err.println("Error setting socket timeout.");
 			Paxos.cleanExit();
 		}
 	}
 
 	private void becomeLeader() {
 		System.out.println("Becoming leader..");
 		
 		try {
 			leaderAddress = InetAddress.getLocalHost();
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 		this.isLeader = true;
 		this.heartbeatSender = new HeartbeatSender(LEADER_HB);
 		this.heartbeatSender.start();
 		
 		leaderProc = new LeaderProcessor();
 		leaderProc.start();
 		leaderListener = new LeaderListener(LEADER_LISTENER_PORT);
 		leaderListener.start();
 	}
 
 	private void resetNodeTimer(InetAddress node) {
 		
 		if(!nodes.contains(node)) {
 			nodes.add(node);
 		}
 		nodeTimers.put(node, DEFAULT_TIMER);
 	}
 	
 	public static int sendMessageToGroup(Object obj) {
 		
 		byte[] buff;
 		try {
 			buff = SerializationUtil.serialize(obj);
 			DatagramPacket sendingPacket = new DatagramPacket(buff,buff.length, InetAddress.getByName(GROUP),PORT);
 			DatagramSocket ds = new DatagramSocket();
 			ds.send(sendingPacket);
 			return 0;
 		} catch (IOException e) {
 			return 1;
 		}	
 	}
 	
 	public DatagramPacket receiveMessage() { 
 		
 		byte[] buf = new byte[1024];
 		DatagramPacket inputPacket = new DatagramPacket(buf,buf.length);
 		try {
 				msocket.receive(inputPacket);
 			} catch (SocketTimeoutException ste) {
 				return null;
 			} catch (IOException e) {
 				System.err.println("Could not receive packet.");		
 			}	
 		return inputPacket;
 	}
 	
 	public static void respondToLeader(Object message) {
 		
 		byte[] buff = SerializationUtil.serialize(message);
 		DatagramPacket response = new DatagramPacket(buff, buff.length, leaderAddress , LEADER_REPLIES_PORT);
 		DatagramSocket socketToLeader = null;
 		
 		try {
 			socketToLeader = new DatagramSocket();
 		} catch (SocketException e1) {
 			System.err.println("Error while creating socket to leader.");
 			return;
 		}
 		try {
 			socketToLeader.send(response);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}		
 	}
 	
 	public static void requestCatchUp(Object message) {
 		
 		byte[] buff = SerializationUtil.serialize(message);
		DatagramPacket response = new DatagramPacket(buff, buff.length, leaderAddress , LEADER_REPLIES_PORT);
 		DatagramSocket socketToLeader = null;
 		
 		try {
 			socketToLeader = new DatagramSocket();
 		} catch (SocketException e1) {
 			System.err.println("Error while creating socket to leader.");
 			return;
 		}
 		try {
 			socketToLeader.send(response);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}		
 	}
 	public int getQuorumSize() {
 		return this.nodes.size();
 	}
 	
 	
 	public class HeartbeatProcess extends Thread {
 		
 		public boolean running;
 		public HeartbeatProcess() {
 			this.running = true;
 		}
 		
 		@Override
 		public void run() {
 			while(running) {
 				for(int i=0;i<nodes.size(); i++) {		
 					int currentTime = nodeTimers.get(nodes.elementAt(i));		
 					nodeTimers.put(nodes.elementAt(i), --currentTime);
 					
 					if(nodeTimers.get(nodes.elementAt(i)) < 1) {
 						if(nodes.elementAt(i).equals(leaderAddress)) {
 							// New leader election
 						}
 						
 					}
 				}
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
     public class HeartbeatSender extends Thread {
 		
     	public boolean running;
 		private int type;
 		
 		public HeartbeatSender(int type) {
 			this.running = true;
 			this.type = type;
 		}
 		
 		public void run() {
 			while(running) {
 				try {
 					Thread.sleep(2000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				if(type == LEADER_HB) {
 					sendMessageToGroup(new LeaderHeartbeatMessage(data.getLargestSeqNumber()));
 				} else {
 					sendMessageToGroup(new HeartbeatMessage());
 				}
 			}
 		}
 	}
     
     
     
     
     
 }
 
 
