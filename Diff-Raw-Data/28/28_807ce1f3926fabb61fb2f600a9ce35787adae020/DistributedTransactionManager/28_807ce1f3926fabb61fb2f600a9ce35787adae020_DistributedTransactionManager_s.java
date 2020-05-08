 package distributedServices;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.UUID;
 
 import Rio.Protocol;
 import localServices.FileSystem;
 import localServices.Status;
 import localServices.StatusException;
 import distributedServices.DistributedTransaction.NodeState;
 import distributedServices.DistributedTransaction.TransactionStatus;
 import edu.washington.cs.cse490h.lib.PersistentStorageReader;
 
 /**
  * This class manages all distributed transactions on the current node. 
  * It will act as a coordinator for all 2-phase commit transactions, as well
  * as a participant in transactions initiated by other nodes. 
  * 
  * @author cgerea
  *
  */
 public class DistributedTransactionManager {
 
 	// this is the list of transactions that the manager coordinates
 	private HashMap<UUID, DistributedTransaction> _coordinatedTransactions;
 	
 	// this is the list of transactions that the manager participates to
 	private HashMap<UUID, DistributedTransaction> _participantTransactions;
 
 	private FileSystem _fileSystem;
 	ServiceNode node;
 	private static String LogName = "dtm_log";
 	// timeout for the participant waiting for a request 
 	private static int WaitRequestTimeout = 100;
 	// timeout for commit to receive responses from all nodes
 	private static int WaitCommitTimeout = 100;
 	private static String ParticipantsSeparator = "*";
 	RpcClient rpc;
 	
 	public DistributedTransactionManager(ServiceNode node, RpcClient rpc){
 		this.node = node;
 		this.rpc = rpc;
 		_coordinatedTransactions = new HashMap<UUID, DistributedTransaction>();
 		_participantTransactions = new HashMap<UUID, DistributedTransaction>();
 		_fileSystem = new FileSystem(node);
 	}
 	
 	public DistributedTransaction Create(){
 		DistributedTransaction dt = new DistributedTransaction(this, UUID.randomUUID());
 		_coordinatedTransactions.put(dt.Id, dt);
 		return dt;
 	}
 	
 	public void Commit(final DistributedTransaction transaction, final INotify notify){
 		StringBuilder participants = new StringBuilder();
 		// 		send VOTE-REQ to all participants;
 		for(NodeState ns: transaction.ParticipantNodes.values()){
 			Send(ns.NodeId, DtPacket.VoteRequest(transaction.Id));
 			participants.append(ns.NodeId.toString());
 			participants.append(ParticipantsSeparator);
 		}
 		
 		// write start-2PC record in DT log;
 		Log(LogSource.Coordinator, transaction.Id, LogAction.Start2Pc, participants.toString());
 		
 		transaction.WaitCommitId = rpc.registerPendingRequest(
 				new INotify() {
 					
 					@Override
 					public void onCompleted(Status result, String data) {
 						if(result == Status.OperationTimeout ) {
 							// write abort record in DT log;
 							Log(LogSource.Coordinator, transaction.Id, LogAction.Abort);
 							
 							// send ABORT to all processes who replied YES
 							for(NodeState ns:transaction.ParticipantNodes.values()){
 								if(ns.HasResponded && ns.HasAccepted) {
 									Send(ns.NodeId, DtPacket.Abort(transaction.Id));
 								}
 							}
 						}
 
 						// tell the user about the transaction's fate
 						notify.onCompleted(result, "");
 					}
 				}, 
 				WaitCommitTimeout);
 	}
 	
 	public void Abort(DistributedTransaction transaction, INotify notify) throws Exception{
 		if(transaction.Status != TransactionStatus.Created){
 			// this is an error
 			throw new Exception("Transaction cannot be aborted. Status = " + transaction.Status.toString());
 		}
 		
 		// write abort record in DT log;
 		Log(LogSource.Coordinator, transaction.Id, LogAction.Abort);
 		transaction.Status = TransactionStatus.Aborted;
 		
 		// tell the user about the result in case they initiated the transaction
 		if(transaction.WaitCommitId >= 0){
 			rpc.completeRequest(transaction.WaitCommitId, Status.TransactionAborted, "");
 		}
 		
 		// abort all transactions that accepted
 		for(DistributedTransaction.NodeState ns2:transaction.ParticipantNodes.values()){
 			if(ns2.HasResponded && ns2.HasAccepted){
 				Send(ns2.NodeId, DtPacket.Abort(transaction.Id));
 			}
 		}		
 	}
 	
 	public void OnReceive(Integer from, DtPacket packet){
 		switch(packet.MessageType){
 			case Enlist:
 				OnEnlist(packet);
 				break;
 			case Abort:
 				OnAbort(packet);
 				break;
 			case Commit:
 				OnCommit(packet);
 				break;
 			case CommitResponse:
 				// TODO garbage collect the list of coordinated transactions
 				// when all participants responded 
 				break;
 			case VoteRequest:
 				OnVoteRequest(from, packet);
 				break;
 			case VoteResponse:
 				OnVoteResponse(from, packet);
 				break;
 			default:
 				break;
 		}
 	}
 	
 	public void Enlist(Integer nodeId, UUID transactionId){
 		DistributedTransaction dt = _coordinatedTransactions.get(transactionId);
 		dt.ParticipantNodes.put(nodeId, dt.new NodeState(nodeId));
 		Send(nodeId, DtPacket.Enlist(transactionId));
 	}
 	
 	/**
 	 * Initializes the transaction manager by recovering its state from the log.
 	 */
 	public void Recover() {
 		// TODO run recovery for the local transactions
 		
 		// first read all the records in the log
 		PersistentStorageReader reader;
 		try {
 			reader = node.getReader(LogName);
 		} catch (FileNotFoundException e) {
 			// not finding the log means we're starting for the first time
 			try {
 				_fileSystem.create(LogName);
 			} catch (StatusException e1) {
 				// should not happen so we'll ignore it
 				e1.printStackTrace();
 			}
 			return;
 		}
 		
 		String line;
 		try {
 			line = reader.readLine();
 			
 			while (line != null){
 				String[] splits = line.split(" ");
 				LogSource source = LogSource.values()[Integer.parseInt(splits[0])];
 				UUID transactionId = UUID.fromString(splits[1]);
 				LogAction action = LogAction.values()[Integer.parseInt(splits[2])];
 				String message = splits.length > 3 ? splits[3] : "";
 				ProcessLogRecord(source, transactionId, action, message);
 				
 				line = reader.readLine();
 			}
 		} catch (IOException e) {
 			// TODO for the moment consider IOExceptions as benign 
 			//e.printStackTrace();
 		}
 		
 		RecoverCoordinatorTransactions();
 		RecoverParticipantTransactions();
 	}
 	
 	private void ProcessLogRecord(LogSource logSource, UUID transactionId, LogAction action, String message){
 		if(logSource == LogSource.Coordinator){
 			switch(action){
 			case Start2Pc: {
 				DistributedTransaction dt = new DistributedTransaction(this, transactionId);
 				dt.Status = TransactionStatus.Start2Pc;
 				// message contains the list of participant nodes
 				String[] splits = message.split(ParticipantsSeparator);
 				for(String id:splits){
 					Integer nodeId = Integer.parseInt(id);
 					dt.ParticipantNodes.put(nodeId, dt.new NodeState(nodeId));
 				}
 				_coordinatedTransactions.put(transactionId, dt);					
 			}
 				break;
 				
 			case Commit: 
 				_coordinatedTransactions.get(transactionId).Status = TransactionStatus.Commited;
 				break;
 			case Abort: 
 				_coordinatedTransactions.get(transactionId).Status = TransactionStatus.Aborted;
 				break;
 			default:
 				// TODO handle error when dealing with unknown action
 				break;
 			}
 		} else { // logSource == LogSource.Participant
 			switch(action){
 			case Enlist:{
 				DistributedTransaction dt = new DistributedTransaction(this, transactionId);
 				dt.Status = TransactionStatus.Created;
 				_participantTransactions.put(transactionId, dt);	
 				break;
 			}
 			case VoteYes: 
 				_participantTransactions.get(transactionId).Status = TransactionStatus.VotedYes;
 				break;
 			case Abort: 
 				_participantTransactions.get(transactionId).Status = TransactionStatus.Aborted;
 				break;
 			case Commit:
 				_participantTransactions.get(transactionId).Status = TransactionStatus.Commited;
 				break;
 			default:
 				// TODO handle error when dealing with unknown action
 				break;
 			}
 		}
 	}
 	
 	/**
 	 * Recovers transactions that the coordinator is responsible for.
 	 */
 	private void RecoverCoordinatorTransactions(){
 		for(DistributedTransaction dt:_coordinatedTransactions.values()){
 			if(dt.Status == TransactionStatus.Start2Pc){
 				// the coordinator had not decided before the failure. 
 				// at this point the coordinator decides to abort
 				// first log the decision then send it to all participants
 				Log(LogSource.Coordinator, dt.Id, LogAction.Abort);
 				for(NodeState ns:dt.ParticipantNodes.values()){
 					Send(ns.NodeId, DtPacket.Abort(dt.Id));
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Recovers all transactions that the coordinator was a participant into
 	 */
 	private void RecoverParticipantTransactions(){
 		for(DistributedTransaction dt:_participantTransactions.values()){
 			if(dt.Status == TransactionStatus.Created){
 				// The DT log does not contain a yes record. 
 				// Then either the participant failed before voting or voted No 
 				// (but did not write an abort record before failing).
 				// It can therefore abort by inserting an abort record in the DT log
 				Log(LogSource.Participant, dt.Id, LogAction.Abort);
 				dt.Status = TransactionStatus.Aborted;
 			} else if(dt.Status == TransactionStatus.VotedYes) {
 				// The DT log contains a yes but no commit or abort record. Then the
 				// participant failed while in its uncertainty period. It can try to reach
 				// a decision using the termination protocol. Recall that a yes record
 				// includes the name of the coordinator and participants, which are
 				// needed for the termination protocol.
 				// TODO run termination protocol
 			}
 		}
 		
 	}
 
 	private void OnVoteResponse(Integer from, DtPacket packet) {
 		/*
 		 * This is the logic for handling vote responses
 		if all votes were YES and coordinator votes Yes then begin
 		write commit record in DT log;
 		send COMMIT to all participants
 		end
 		else begin
 		let Py be the processes from which YES was received;
 		write abort record in DT log;
 		send ABORT to all processes in Py
 		end;
 		 */
 		
 		DistributedTransaction dt = _coordinatedTransactions.get(packet.TransactionId);
 		DistributedTransaction.NodeState ns = dt.ParticipantNodes.get(from);
 		ns.HasResponded = true;
 		ns.HasAccepted = packet.Data.compareTo("YES") == 0;
 		
 		if(!ns.HasAccepted){
 			dt.Status = TransactionStatus.Aborted;
 			
 			// write abort record in DT log;
 			Log(LogSource.Coordinator, dt.Id, LogAction.Abort);
 			
 			// tell the client that we failed
 			// need to do it before the send because those move the clock forward
 			// and we don't want to time out
 			rpc.completeRequest(dt.WaitCommitId, Status.TransactionAborted, "");
 			
 			// abort all transactions that accepted
 			for(DistributedTransaction.NodeState ns2:dt.ParticipantNodes.values()){
 				if(ns2.HasResponded && ns2.HasAccepted){
 					Send(ns2.NodeId, DtPacket.Abort(packet.TransactionId));
 				}
 			}
 		} else if(dt.Status == TransactionStatus.Aborted){
 			// this transaction was aborted already, we can tell the node on the spot
 			Send(ns.NodeId, DtPacket.Abort(packet.TransactionId));
 		} else {
 			// see if all nodes voted yes
 			boolean canCommit = true;
 			for(DistributedTransaction.NodeState ns2:dt.ParticipantNodes.values()){
 				if(!ns2.HasResponded || !ns2.HasAccepted){
 					canCommit = false;
 				}
 			}
 			
 			if(canCommit){
 				// All nodes voted yes and the coordinator will vote yes as well
 				// write commit record in DT log
 				Log(LogSource.Coordinator, dt.Id, LogAction.Commit);
 				
 				// tell the client that we committed
 				// need to do it before the send because those move the clock forward
 				// and we don't want to time out
				rpc.completeRequest(dt.WaitCommitId, Status.TransactionAborted, "");
 				
 				// send COMMIT to all participants
 				for(DistributedTransaction.NodeState ns2:dt.ParticipantNodes.values()){
 					Send(ns2.NodeId, DtPacket.Commit(packet.TransactionId));
 				}
 			}
 		}
 	}
 
 	private void OnVoteRequest(Integer from, DtPacket packet) {
 		// TODO need to hook up with local transaction here to see if 
 		// we can accept this transaction
 		// for testing purposes pretend to accept always
 		boolean vote = true;
 		
 		if(vote){
 			// write a yes record in DT log
 			Log(LogSource.Participant, packet.TransactionId, LogAction.VoteYes);
 			
 			// kill the abort timeout
 			int rid = _participantTransactions.get(packet.TransactionId).WaitRequestId;
 			if(rid >= 0){
 				rpc.completeRequest(rid, Status.Success, "");
 			}
 			
 			Send(from, DtPacket.VoteResponse(packet.TransactionId, "YES"));
 			// TODO 
 			// wait for decision message(COMMIT or ABORT) from coordinator
 			// on timeout initiate termination protocol /
 		} else {
 			// write abort record in DT log;
 			Log(LogSource.Participant, packet.TransactionId, LogAction.Abort);
 			Send(from, DtPacket.VoteResponse(packet.TransactionId, "NO"));
 
 		}
 	}
 
 	private void OnCommit(DtPacket packet) {
 		// write commit record in DT log
 		Log(LogSource.Participant, packet.TransactionId, LogAction.Commit);
 		// TODO work with local transaction to commit
 		_participantTransactions.get(packet.TransactionId).Status = TransactionStatus.Commited;
 	}
 
 	private void OnAbort(DtPacket packet) {
 		// write abort record in DT log
 		Log(LogSource.Participant, packet.TransactionId, LogAction.Abort);
 		// TODO work with local transaction to abort
 		_participantTransactions.get(packet.TransactionId).Status = TransactionStatus.Aborted;
 	}
 
 	private void OnEnlist(final DtPacket packet) {
 		final DistributedTransaction dt = new DistributedTransaction(this, packet.TransactionId);
 		// TODO we need to fill in the participant nodes
 		Log(LogSource.Participant, dt.Id, LogAction.Enlist);
 		_participantTransactions.put(packet.TransactionId, dt);
 		
 		// register a timeout so we can abort if the server does not 
 		// initiate commit in a timely manner
 		dt.WaitRequestId = rpc.registerPendingRequest(new INotify() {
 			@Override
 			public void onCompleted(Status result, String data) {
 				if(result == Status.OperationTimeout){
 					dt.Status = TransactionStatus.Aborted;
 					Log(LogSource.Participant, packet.TransactionId, LogAction.Abort);
 				} 
 			}
 		}, 
 		WaitRequestTimeout);
 	}
 	
 	private void Send(int destAddr, DtPacket packet) {
 		rpc.sendAndForget(destAddr, Protocol.DTM_PKT, packet.pack());
 	}
 
 	private void Log(LogSource logSource, UUID transactionId, LogAction action){
 		Log(logSource, transactionId, action, "");
 	}
 
 	private void Log(LogSource logSource, UUID transactionId, LogAction action, String message){
 		StringBuilder builder = new StringBuilder();
 		builder.append(logSource.ordinal());
 		builder.append(" ");
 		builder.append(transactionId.toString());
 		builder.append(" ");
 		builder.append(action.ordinal());
 		builder.append(" ");
 		builder.append(message);
 		builder.append("\n");
 		
 		// TODO what shall we do with exceptions? we can't ignore failures
 		// write to file
 		try {
 			_fileSystem.append(LogName, builder.toString());
 		} catch (StatusException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	private enum LogAction {
 		Enlist,
 		Start2Pc,
 		Abort,
 		Commit,
 		VoteYes
 	}
 	
 	private enum LogSource {
 		Coordinator,
 		Participant
 	}
 	
 }
