 package node;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.PriorityQueue;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.UUID;
 
 import common.Common;
 import common.Common.SiteCrashMsgType;
 import common.Triplet;
 import common.Tuple;
 import common.Common.PaxosLeaderState;
 import common.Common.State;
 import common.Common.TwoPCMsgType;
 import common.MessageWrapper;
 
 import message.BcastMsg;
 import message.ClientOpMsg;
 import message.PaxosMsg;
 import message.SiteCrashMsg;
 import message.TwoPCMsg;
 
 
 import org.apache.commons.collections.BidiMap;
 import org.apache.commons.collections.bidimap.*;
 
 public class PaxosLeader extends Node{
 
 	final class TransactionStatus {
 
 		Integer gsn;
 		PaxosLeaderState state;
 		String data;
 		String clientRoutingKey;
 		Boolean isCommitAckReceived;
 
 		Set<String> acceptorListPrepare;
 		Set<String> acceptorListCommit;
 		Set<String> acceptorListAbort;
 
 		public TransactionStatus(String data)
 		{			
 			this.gsn = -1;
 			this.state = PaxosLeaderState.PREPARE;
 			this.acceptorListPrepare = new HashSet<String>();	
 			this.acceptorListCommit =  new HashSet<String>();
 			this.acceptorListAbort =  new HashSet<String>();
 			this.data = data;
 			this.isCommitAckReceived = false;
 		}		
 	}
 
 	private String tpcCoordinatorId;
 	private String paxosLeaderExchange;
 	private String tpcCoordinatorExchance;
 
 	private int readLineNumber;
 	private Map<UUID, TransactionStatus> uidTransactionStatusMap;
 	private SortedSet<Map.Entry<UUID, TransactionStatus>> uidCommitAckStatusSet;
 
 	public PaxosLeader(String nodeId, String fileName, String tpcCoordinatorId) throws IOException {		
 		super(nodeId, fileName);
 
 		this.tpcCoordinatorId = tpcCoordinatorId;
 		this.paxosLeaderExchange = Common.PaxosLeaderExchange + this.nodeId;
 		this.tpcCoordinatorExchance = Common.TPCCoordinatorExchange + this.tpcCoordinatorId;
 
 		ArrayList<Triplet<String, String, Boolean>> exchanges = new ArrayList<Triplet<String, String, Boolean>>();
 		exchanges.add(new Triplet(Common.DirectMessageExchange, Common.directExchangeType, true));
 		exchanges.add(new Triplet(this.paxosLeaderExchange, Common.bcastExchangeType, false));
 		exchanges.add(new Triplet(this.tpcCoordinatorExchance, Common.bcastExchangeType, true));
 
 		this.DeclareExchanges(exchanges);
 		this.InitializeConsumer();
 
 		this.readLineNumber = Common.ReadLineCount;
 		this.uidTransactionStatusMap = new HashMap<UUID, TransactionStatus>();
 
 		this.uidCommitAckStatusSet = new TreeSet<Map.Entry<UUID, TransactionStatus>>(
 				new Comparator<Map.Entry<UUID, TransactionStatus>>() {
 					@Override
 					public int compare(Map.Entry<UUID, TransactionStatus> e1, 
 							Map.Entry<UUID, TransactionStatus> e2)
 					{
 						return e1.getValue().gsn.compareTo(e2.getValue().gsn);
 					}
 				});
 	}
 
 
 	public void run() throws IOException, InterruptedException, ClassNotFoundException{
 
 		while (true) {
 
 			MessageWrapper msgwrap =  messageController.ReceiveMessage();    
 			if (msgwrap != null ) {
 
 				if(msgwrap.getmessageclass() == ClientOpMsg.class && this.NodeState == State.ACTIVE)
 				{
 					ClientOpMsg msg = (ClientOpMsg) msgwrap.getDeSerializedInnerMessage();
 
 					//Print msg
 					System.out.println("Received " + msg);
 
 					ProcessClientMessageData(msg);
 				}
 
 				else if(msgwrap.getmessageclass() == PaxosMsg.class && this.NodeState == State.ACTIVE)
 				{
 
 
 					PaxosMsg msg = (PaxosMsg) msgwrap.getDeSerializedInnerMessage();
 
 					//Print msg
 					System.out.println("Received " + msg);
 
 					TransactionStatus temp = uidTransactionStatusMap.get(msg.getUID());
 
 					if (temp.state == PaxosLeaderState.PREPARE)
 						ProcessPrepareAck(msg.getUID(), msg.getNodeid());
 
 					else if (temp.state == PaxosLeaderState.COMMIT)
 						ProcessCommitAck(msg.getUID(), msg.getNodeid());
 
 					else if (temp.state == PaxosLeaderState.ABORT)
 						ProcessAbortAck(msg.getUID(), msg.getNodeid());
 				}
 
 				else if(msgwrap.getmessageclass() == TwoPCMsg.class && this.NodeState == State.ACTIVE)
 				{
 					TwoPCMsg msg = (TwoPCMsg) msgwrap.getDeSerializedInnerMessage();
 
 					//Print msg
 					System.out.println("Received " + msg);
 
 					if (msg.getType() == TwoPCMsgType.COMMIT)
 					{						
 						ProcessCommitRequest(msg.getUID(), msg.getGsn());
 
 					}
 
 					else if (msg.getType() == TwoPCMsgType.ACK)
 						ProcessCommitAckRequestFromTPC(msg.getUID());
 
 					else 
 						ProcessAbortRequest(msg.getUID());
 				}
 				else if (msgwrap.getmessageclass() == SiteCrashMsg.class)
 				{
 					SiteCrashMsg msg = (SiteCrashMsg) msgwrap.getDeSerializedInnerMessage();
 
 					//Print msg
 					System.out.println("Received " + msg);
 
 					if(msg.getType() == SiteCrashMsgType.CRASH && this.NodeState == State.ACTIVE)
 					{
 						this.NodeState = State.PAUSED;
 					}
 					else if(msg.getType() == SiteCrashMsgType.RECOVER && this.NodeState == State.PAUSED)
 					{
 						this.NodeState = State.ACTIVE;
 					}					
 				}
 				else
 				{
 					//Message Discarded.
 				}
 			}
 
 			//process transaction timeouts.
 		}		
 	}
 
 	//method used to process client msg
 	public void ProcessClientMessageData(ClientOpMsg msg) throws IOException
 	{
 		if(msg.getType()==Common.ClientOPMsgType.APPEND)
 		{
 			this.ProcessAppendRequest(msg.getUid(), msg.getData(), msg.getNodeid());
 		}
 		else
 		{
 			//TODO: Respond with read of file.
 			this.localResource.ReadResource(this.readLineNumber);
 		}		
 	}
 
 	//Process New Append Request from Client
 	public void ProcessAppendRequest(UUID uid, String data, String clientRoutingKey) throws IOException 
 	{
 		TransactionStatus temp = new TransactionStatus(data);
 		temp.clientRoutingKey = clientRoutingKey;
 
 		this.uidTransactionStatusMap.put(uid, temp);
 
 		PaxosMsg paxosMsg = new PaxosMsg(this.nodeId, Common.PaxosMsgType.ACCEPT, uid, data);
 		SendPaxosMsg(paxosMsg);
 	}
 
 
 	//Process New Read Request from Client
 	public String ProcessReadRequest()
 	{
 		//TODO : Implement file line number logic.
 		return new String();
 	}
 
 	//Process Ack from Acceptor
 	public void ProcessPrepareAck(UUID uid, String nodeId) throws IOException
 	{
 		TransactionStatus temp = this.uidTransactionStatusMap.get(uid);
 		temp.acceptorListPrepare.add(nodeId);
 				
 		System.out.println("Acceptor List " + temp.acceptorListPrepare.toString());
 		//System.out.println("Quorum size " + Common.GetQuorumSize());
 		
 		if (temp.acceptorListPrepare.size() >= Common.GetQuorumSize() && temp.state == PaxosLeaderState.PREPARE) 
 		{
 			temp.state = Common.PaxosLeaderState.ACCEPT;
 
 			TwoPCMsg msg = new TwoPCMsg(this.nodeId, TwoPCMsgType.INFO, uid);
 			msg.setClientRoutingKey(temp.clientRoutingKey);
 			this.SendTPCMsg(msg);
 		}
 		else
 		{
 			//Add if required.
 		}
 		this.uidTransactionStatusMap.put(uid, temp);
 	}
 
 
 	// Update GSN after receiving commit response from 2PC.
 	public void ProcessCommitRequest(UUID uid, int gsn) throws IOException
 	{
 		TransactionStatus temp = this.uidTransactionStatusMap.get(uid);
 
 		temp.gsn = gsn;
 		temp.state = Common.PaxosLeaderState.COMMIT;
 		this.uidTransactionStatusMap.put(uid, temp);
 
 		//propagate info to all acceptors
 		PaxosMsg msg = new PaxosMsg(this.nodeId, Common.PaxosMsgType.COMMIT, uid, gsn);
 		SendPaxosMsg(msg);
 	}
 
 	//method used to process abort msg from the two phase coordinator
 	public void ProcessAbortRequest(UUID uid) throws IOException
 	{
 		/*
 		 * 0. Broadcast Message to all Acceptors.
 		 * 1. Change State to Abort.
 		 */
 
 		//propagate info to all acceptors
 		TransactionStatus temp = uidTransactionStatusMap.get(uid);
 		temp.state = Common.PaxosLeaderState.ABORT;
 		uidTransactionStatusMap.put(uid, temp);
 
 		PaxosMsg msg = new PaxosMsg(this.nodeId, Common.PaxosMsgType.ABORT, uid);		
 		SendPaxosMsg(msg);
 	}
 
 
 	public void ProcessCommitAck(UUID uid, String nodeId) throws IOException
 	{
 		TransactionStatus temp = this.uidTransactionStatusMap.get(uid);
 		temp.acceptorListCommit.add(nodeId);
 		if (temp.acceptorListCommit.size() >= Common.GetQuorumSize() && temp.state == PaxosLeaderState.COMMIT) 
 		{
 			temp.state = Common.PaxosLeaderState.COMMIT_ACK;
 			TwoPCMsg msg = new TwoPCMsg(this.nodeId, TwoPCMsgType.COMMIT, uid, temp.gsn);
 			this.SendTPCMsg(msg);
 		}
 		else
 		{
 			//Add if required.
 		}
 		this.uidTransactionStatusMap.put(uid, temp);
 	}
 
 	public void ProcessAbortAck(UUID uid, String nodeId) throws IOException
 	{
 
 		TransactionStatus temp = this.uidTransactionStatusMap.get(uid);
 		temp.acceptorListAbort.add(nodeId);
 		if (temp.acceptorListAbort.size() >= Common.GetQuorumSize() && temp.state == PaxosLeaderState.ABORT) 
 		{			
 			temp.state = Common.PaxosLeaderState.ABORT_ACK;
 			TwoPCMsg msg = new TwoPCMsg(this.nodeId, TwoPCMsgType.ABORT, uid, temp.gsn);
 			this.SendTPCMsg(msg);
 
 		}
 		else 
 		{
 			//Add if required.
 		}
 		this.uidTransactionStatusMap.put(uid, temp);
 	}
 	public void ProcessCommitAckRequestFromTPC(UUID uid)
 	{
 		TransactionStatus temp = this.uidTransactionStatusMap.get(uid);
 		temp.isCommitAckReceived = true;
 		this.uidTransactionStatusMap.put(uid, temp);
 
 		this.uidCommitAckStatusSet.addAll(this.uidTransactionStatusMap.entrySet());
 		
 		while(true)
 		{
 			if(this.uidCommitAckStatusSet.isEmpty())
 				break;
 			
 			Entry<UUID, TransactionStatus> e =  this.uidCommitAckStatusSet.first();
 			
 			/*
 			UUID uidtemp = e.getKey();
 			TransactionStatus tstemp = e.getValue();
 			
 			System.out.println(uidtemp);
 			System.out.println(tstemp.clientRoutingKey + " " + tstemp.data);
 			*/
 			
 			if(e.getValue().gsn == -1 )
 				this.uidCommitAckStatusSet.remove(e);
 
 			else if (e.getValue().isCommitAckReceived)
 			{
 				this.readLineNumber += 1;
 				this.uidCommitAckStatusSet.remove(e);
 			}
 			else 
 			{
 				break;
 			}
 		}
 
 	}
 
 	//Send Message to TPC Coord.
 	public void SendTPCMsg(TwoPCMsg msg) throws IOException
 	{		
 		//Print msg
 		System.out.println("Sent " + msg);
 
 		MessageWrapper msgwrap = new MessageWrapper(Common.Serialize(msg), msg.getClass());
 		this.messageController.SendMessage(msgwrap, Common.DirectMessageExchange, this.tpcCoordinatorId);
 	}
 
 	//Broadcast append request to all acceptors.
 	public void SendPaxosMsg(PaxosMsg msg) throws IOException
 	{		
 		//Print msg
 		System.out.println("Sent " + msg);
 
 		MessageWrapper msgwrap = new MessageWrapper(Common.Serialize(msg), msg.getClass());
 		this.messageController.SendMessage(msgwrap, this.paxosLeaderExchange, "");
 	}
 
 }
