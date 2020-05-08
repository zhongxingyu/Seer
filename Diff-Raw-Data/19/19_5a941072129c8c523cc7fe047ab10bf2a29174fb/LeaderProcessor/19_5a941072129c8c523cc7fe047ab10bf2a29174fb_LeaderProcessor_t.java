 package com.nick.Paxos.Network.Leader;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 
 import com.nick.Paxos.Command;
 import com.nick.Paxos.Paxos;
 import com.nick.Paxos.Messages.AcceptNotificationMessage;
 import com.nick.Paxos.Messages.AcceptReplyMessage;
 import com.nick.Paxos.Messages.AcceptRequestMessage;
 import com.nick.Paxos.Messages.PaxosMessage;
 import com.nick.Paxos.Messages.PrepareRequestMessage;
 import com.nick.Paxos.Messages.PromiseMessage;
 import com.nick.Paxos.Network.PaxosQueue;
 import com.nick.Paxos.Network.SerializationUtil;
 
 public class LeaderProcessor extends Thread  {
 	
 	// Used by the leader to receive responses
 	private DatagramSocket leaderRepliesSocket;
 	private final static int LEADER_REPLIES_PORT = 1235;
 	private static final String GROUP = "239.0.0.1";
 		
 	private PaxosQueue queue;
 	private DatagramSocket toGroup;
 	private DatagramSocket replySocket;
 	
 	private int minSeqn = 0;
 	
 	private final int SUCCESS = 1;
 	private final int FAILURE = -1;
 	
 	private int QUORUM = Paxos.node.nodes.size();
 	
 	public boolean running = false;
 	
 	public LeaderProcessor() {
 		
 		this.queue = new PaxosQueue();
 		
 		try {
 			this.leaderRepliesSocket = new DatagramSocket(LEADER_REPLIES_PORT);
 		} catch (SocketException e1) {
 			e1.printStackTrace();
 		}
 		
 		try {
 			toGroup = new DatagramSocket();
 		} catch (SocketException e) {
 			e.printStackTrace();
 		}
 		running = true;
 	}
 	
 	public void enqueueCommand(Command cmd) {
 		queue.enqueue(cmd);
 	}
 	
 	public void run() {
 		int reply;
 		
 		while(running) {
 			while(!queue.isEmpty()) {
 			
 				reply = sendPrepareMessage();
 			
 				if(reply == SUCCESS) {
 					System.out.println("Received enough promises. Sending Accept Request.");
 					int acceptReply = sendAcceptRequest(queue.peek());
 				
 					if(acceptReply == SUCCESS) { 
 						System.out.println("Received enough accepts. Value accepted.");
 						sendAcceptNotification(queue.dequeue());
 						
 						increaseSeqNo();
 					} else {
 						System.out.println("Received Old Round for sequence number " + this.minSeqn + ". Increasing to " + (this.minSeqn + 5));
 						increaseSeqNo();
 					}
 				} else {
 					System.out.println("Did not receive promises from the majority.");
					increaseSeqNo();
 					// Retry? Increase sequence number?
 				}
 			}
 		}
 	}
 	
 	public void resultReply(String s, InetAddress to) {
 		try {
 			replySocket = new DatagramSocket();
 			byte[] buf = SerializationUtil.serialize(s);
 			DatagramPacket dp = new DatagramPacket(buf,buf.length,to,1237);
 			replySocket.send(dp);
 		} catch (SocketException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		replySocket.close();
 	}
 	
 	private void sendAcceptNotification(Command cmd) {
 		AcceptNotificationMessage ANM = new AcceptNotificationMessage(minSeqn, cmd);
 		sendToGroup(ANM);
 	}
 	
 	private void increaseSeqNo() {
 		minSeqn += 5;
 	}
 	
 	private int sendPrepareMessage() {
 		
 		PrepareRequestMessage PRM = new PrepareRequestMessage(minSeqn);
 		sendToGroup(PRM);		
 		return receiveReplies();	
 		
 	}
 	
 	private int sendAcceptRequest(Command cmd) {
 		AcceptRequestMessage ARM = new AcceptRequestMessage(minSeqn, cmd);
 		sendToGroup(ARM);
 		return receiveReplies();
 	}
 	
 	private void sendToGroup(PaxosMessage msg) {
 		
 		byte[] sendingBuf = SerializationUtil.serialize(msg);
 		DatagramPacket sendPacket = null;
 		
 		try {
 			 sendPacket = new DatagramPacket(sendingBuf,sendingBuf.length,InetAddress.getByName(GROUP), 1234);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 		
 		try {
 			toGroup.send(sendPacket);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private int receiveReplies() {
 		
 		byte[] receiveBuf = new byte[512];
 		DatagramPacket receivePacket = new DatagramPacket(receiveBuf,receiveBuf.length);
 		try {
			leaderRepliesSocket.setSoTimeout(500);
 		} catch (SocketException e1) {
 			e1.printStackTrace();
 		}
 		
 		int accepts = 0;
 		int timeout = 8; //Wait 2 seconds for replies
 		
 		while(timeout > 0 && accepts < ((QUORUM/2)+1) ) {
 			try {
 				leaderRepliesSocket.receive(receivePacket);
 				PaxosMessage reply = (PaxosMessage) SerializationUtil.deSerialize(receiveBuf);
 				
 				if(reply instanceof PromiseMessage || reply instanceof AcceptReplyMessage) {
 					++accepts;
 				}
 			} catch (SocketTimeoutException STE) {
 				
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 			--timeout;
 		}
 		
 		
 		if(accepts >= ((QUORUM/2)+1)) {
 			return SUCCESS;
 		} else {
 			return FAILURE;
 		}
 	}
 
 
 }
