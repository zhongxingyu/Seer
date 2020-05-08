 package node;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.commons.collections.map.LinkedMap;
 import org.apache.commons.collections.map.MultiKeyMap;
 import java.sql.Timestamp;
 
 import common.Common;
 import common.Common.ClientOPMsgType;
 import common.Triplet;
 import common.Tuple;
 import common.Common.SiteCrashMsgType;
 import common.Common.TPCState;
 import common.Common.TwoPCMsgType;
 import common.MessageWrapper;
 import message.ClientOpMsg;
 import message.SiteCrashMsg;
 import message.TwoPCMsg;
 import common.Common.State;
 
 
 public class TPCCoordinator extends Node {
 
 	final class TransactionStatus {
 		Integer gsn;
 		TPCState state;
 		String clientRoutingKey;
 
 		Set<String> paxosLeaderListPrepare;
 		Set<String> paxosLeaderListCommit;
 		Set<String> paxosLeaderListAbort;
 
 		Timestamp timeout;
 
 		public TransactionStatus(String clientRoutingKey)
 		{
 			this.gsn = -1;
 			this.state = TPCState.INIT;
 			this.clientRoutingKey = clientRoutingKey;
 			this.timeout = new Timestamp(new Date().getTime());
 
 			this.paxosLeaderListPrepare = new HashSet<String>();	
 			this.paxosLeaderListCommit =  new HashSet<String>();
 			this.paxosLeaderListAbort =  new HashSet<String>();	
 		}
 	}
 
 
 	private String TwoPCExchange;
 	private static int currentReadLineNumber = 0;
 
 	private Map<String, String> resourcePaxosLeaderMap;
 	private Map<UUID, TransactionStatus> uidTransactionStatusMap;
 	private static int gsnCounter = 0;
 	private Iterator<UUID> itr;
 	public TPCCoordinator(String nodeId) throws IOException {
 		//TPCCoordinator does not handle any resource.
 		super(nodeId, "");
 
 		//Initialize the Message Coordinator Exchanges.
 		this.TwoPCExchange = Common.TPCCoordinatorExchange + this.nodeId;
 
 		ArrayList<Triplet<String, String, Boolean>> exchanges = new ArrayList<Triplet<String, String, Boolean>>();
 		exchanges.add(new Triplet(Common.DirectMessageExchange, Common.directExchangeType, true));
 		exchanges.add(new Triplet(this.TwoPCExchange, Common.bcastExchangeType, false));
 
 		this.DeclareExchanges(exchanges);
 		this.InitializeConsumer();
 
 		//Initialize the Local data structures.
 		this.resourcePaxosLeaderMap = new HashMap<String, String>();
 		this.uidTransactionStatusMap = new HashMap<UUID, TPCCoordinator.TransactionStatus>();
 	}
 
 	public void AddResourcePaxosLeaderMapping(String paxosLeaderId, String resourceName){
 		this.resourcePaxosLeaderMap.put(resourceName, paxosLeaderId);		
 	}
 
 
 	public void run() throws IOException, InterruptedException, ClassNotFoundException{
 
 		while (true) {
 			MessageWrapper msgwrap= messageController.ReceiveMessage();    
 			if (msgwrap != null )
 			{				
 
 				if(msgwrap.getmessageclass() == TwoPCMsg.class && this.NodeState == State.ACTIVE)
 				{
 					TwoPCMsg msg = (TwoPCMsg) msgwrap.getDeSerializedInnerMessage();
 
 					//Print msg
 					System.out.println("Received " + msg);
 					
 					if (msg.getType() == TwoPCMsgType.INFO)
 						ProcessInfoRequest(msg.getUID(), msg.getNodeid(), msg.getClientRoutingKey());
 
 					else if (msg.getType() == TwoPCMsgType.COMMIT)
 						ProcessCommitAck(msg.getUID(), msg.getNodeid(),msg.getGsn());
 
 					else if (msg.getType() == TwoPCMsgType.ABORT)
 						ProcessAbortAck(msg.getUID(), msg.getNodeid());
 
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
 					// Discarded Message.
 				}
 			}
 			//send bcast msgs for timeout transactions
 			itr=uidTransactionStatusMap.keySet().iterator();
 			while(itr.hasNext())
 			{
 				UUID uid=itr.next();
 				TransactionStatus temp= uidTransactionStatusMap.get(uid);
 				Timestamp curtime;
 				if(temp.state == TPCState.INIT)
 				{
 					System.out.println("Timeout Check");
 					curtime=new Timestamp(new Date().getTime());
 					if(curtime.after(Common.getUpdatedTimestamp(temp.timeout, Common.init_timeout)))
 					{
 						temp.state=TPCState.ABORT;
 						temp.timeout=new Timestamp(new Date().getTime());
 						this.uidTransactionStatusMap.put(uid, temp);
 						SendAbortMessage(uid, this.nodeId);
 						System.out.println("Aborting");
 					}				
 				}
 				if(temp.state == TPCState.COMMIT)
 				{
 					curtime=new Timestamp(new Date().getTime());
 					if(curtime.after(Common.getUpdatedTimestamp(temp.timeout, Common.commitabort_timeout)))
 					{
 						temp.timeout=new Timestamp(new Date().getTime());
 						this.uidTransactionStatusMap.put(uid,temp);
 						SendCommitMessage(uid);				
 					}
 				}
 			}
 		}		
 	}
 
 
 	//method used to process New data from the client (forwarded by paxos leader after getting the majority
 	public void ProcessInfoRequest(UUID uid, String nodeid, String clientRoutingKey) throws IOException
 	{
 		if(this.uidTransactionStatusMap.containsKey(uid))
 		{
 			System.out.println("Processing Info Request");
 			
 			TransactionStatus temp = this.uidTransactionStatusMap.get(uid);
 			if(temp.state == TPCState.ABORT)
 			{
 				return;
 			}
 			temp.paxosLeaderListPrepare.add(nodeid);
 			
 
 			if (temp.paxosLeaderListPrepare.size() == Common.NoPaxosLeaders)
 			{
 				System.out.println("Preparing Commit");
 				temp.state = TPCState.COMMIT;
 				int gsn = this.getNewGSN();
 				temp.gsn = gsn;
 				this.uidTransactionStatusMap.put(uid, temp);
 				SendCommitMessage(uid);
 				
 			}
 			else 
 			{
 				this.uidTransactionStatusMap.put(uid, temp);
 			}
 		}
 		else
 		{
 			TransactionStatus temp = new TransactionStatus(clientRoutingKey);
 			temp.paxosLeaderListPrepare.add(nodeid);
 			this.uidTransactionStatusMap.put(uid, temp);
 		}
 
 	}
 
 	//method used to process commit acknowledgment from either of paxos leader
 	public void ProcessCommitAck(UUID uid, String nodeid, int gsn) throws IOException
 	{
 		TransactionStatus temp = this.uidTransactionStatusMap.get(uid);
 		temp.paxosLeaderListCommit.add(nodeid);
 
		if (temp.paxosLeaderListCommit.size() == 1 && temp.state != TPCState.COMMIT_ACK)
 		{	
 			temp.timeout=new Timestamp(new Date().getTime());
 			temp.gsn=gsn;
 			this.uidTransactionStatusMap.put(uid, temp);
 			ClientOpMsg msg = new ClientOpMsg(nodeid, ClientOPMsgType.APPEND_RESPONSE, "Committed", uid);
 			SendClientMessage(msg, temp.clientRoutingKey);			
 		}
		else if (temp.paxosLeaderListCommit.size() == Common.NoPaxosLeaders && temp.state != TPCState.COMMIT_ACK) 
 		{
 			temp.state = TPCState.COMMIT_ACK;
 			temp.gsn=gsn;
 			this.uidTransactionStatusMap.put(uid, temp);
 			//TODO: Update/Send Readline number to paxos leader.
 			TwoPCMsg msg = new TwoPCMsg(this.nodeId, uid, GetReadlineNumber(),temp.gsn,TwoPCMsgType.ACK);
 			SendTPCMessage(msg);
 		}
 	}
 
 
 
 	//method used to process the abort acknowledgement msg from either of paxos leader
 	public void ProcessAbortAck(UUID uid, String nodeid) throws IOException
 	{
 		TransactionStatus temp = this.uidTransactionStatusMap.get(uid);
 		temp.paxosLeaderListCommit.add(nodeid);
 
		if (temp.paxosLeaderListCommit.size() == 1 && temp.state != TPCState.ABORT_ACK)
 		{	
 			ClientOpMsg msg = new ClientOpMsg(nodeid, ClientOPMsgType.APPEND_RESPONSE, "Aborted", uid);
 			SendClientMessage(msg, temp.clientRoutingKey);
 		}
		else if (temp.paxosLeaderListCommit.size() == Common.NoPaxosLeaders && temp.state != TPCState.ABORT_ACK) 
 		{
 			temp.state = TPCState.ABORT_ACK;
 			this.uidTransactionStatusMap.put(uid, temp);
 		}
 	}
 
 
 	public void SendClientMessage(ClientOpMsg msg, String routingKey) throws IOException
 	{
 		MessageWrapper msgwrap = new MessageWrapper(Common.Serialize(msg), msg.getClass());
 		this.messageController.SendMessage(msgwrap, Common.DirectMessageExchange, routingKey);
 
 	}
 
 	//if TPC doesnt receive info msg from both paxos leaders, it sends a abort msg paxos leader who has just sent the data
 	// or just the data(is there a need for lsn? if no, we can inform both the paxos leaders
 	//we just know the lsn from only one PL
 	public void SendAbortMessage(UUID uid,String nodeid) throws IOException
 	{
 		TwoPCMsg abortmsg = new TwoPCMsg(this.nodeId, Common.TwoPCMsgType.ABORT,uid);
 		SendTPCMessage(abortmsg);
 	}
 
 	//method used to send commit msg after receiving info msg from both the paxos leaders
 	//call this method for each PL with diff lsn values
 	public void SendCommitMessage(UUID uid) throws IOException
 	{
 		System.out.println("Sending Commit Message.");
 		TransactionStatus temp = this.uidTransactionStatusMap.get(uid);		
 		TwoPCMsg commitmsg = new TwoPCMsg(this.nodeId, Common.TwoPCMsgType.COMMIT, uid, temp.gsn);
 
 		SendTPCMessage(commitmsg);
 	}
 
 
 	public void SendTPCMessage(TwoPCMsg msg) throws IOException
 	{
 		//Print msg
 		System.out.println("Sent " + msg);
 		
 		MessageWrapper msgwrap = new MessageWrapper(Common.Serialize(msg), msg.getClass());
 		this.messageController.SendMessage(msgwrap, this.TwoPCExchange, "");
 	}
 
 
 	public int GetReadlineNumber()
 	{
 		currentReadLineNumber += 1;
 		return currentReadLineNumber;
 	}
 
 	private static int getNewGSN(){
 		return ++gsnCounter;
 	}
 
 }
