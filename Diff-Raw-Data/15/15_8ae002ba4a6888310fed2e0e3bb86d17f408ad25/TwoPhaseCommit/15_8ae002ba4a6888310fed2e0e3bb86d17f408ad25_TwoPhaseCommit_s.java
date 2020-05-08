 package node.twophasecommit;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.UUID;
 import java.util.Vector;
 
 import settings.MessagingSettings;
 import util.NodeUtility;
 
 import edu.washington.cs.cse490h.lib.Callback;
 import edu.washington.cs.cse490h.lib.PersistentStorageReader;
 import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
 import edu.washington.cs.cse490h.lib.Utility;
 
 import node.rpc.I2pcCoordinator;
 import node.rpc.I2pcCoordinatorReply;
 import node.rpc.I2pcParticipant;
 import node.rpc.I2pcParticipantReply;
 import node.rpc.RPCException;
 import node.rpc.RPCNode;
 import node.rpc.RPCStub;
 import node.facebook.FacebookException;
 
 
 public class TwoPhaseCommit implements I2pcCoordinator, I2pcParticipant, I2pcCoordinatorReply, I2pcParticipantReply 
 {		
 	private static final String PARTICIPANT_FILENAME = "2pc-participant";
 	private RPCNode m_node;
 	private Method m_waitForVotesTimeoutMethod;
 	private Method m_waitForDecisionTimeoutMethod;
 	protected boolean m_inRecovery = false;
 
 	private Hashtable<Integer, I2pcCoordinator> m_coordStubs = new Hashtable<Integer, I2pcCoordinator>();
 	private Hashtable<Integer, I2pcParticipant> m_partcStubs = new Hashtable<Integer, I2pcParticipant>();
 	private Hashtable<Integer, UUID> m_replyMap = new Hashtable<Integer, UUID>();
 	
 	/**
 	 * We need to keep the history of two phase commit for the case of a node waking up and asking us for data about
 	 * an old transaction.
 	 * 
 	 * This keeps track of the transactions that the node participated as a coordinator.
 	 */
 	private Dictionary<UUID, TwoPhaseCommitContext> _twoPhaseCommitContexts;
 	
 	/**
 	 * This keeps track of the current transaction that the node is participating as a participant. There can be only
 	 * one of these at a time.
 	 */
 	private TwoPhaseCommitContext _participantContext;
 	
 	/**
 	 * Constructor.
 	 * 
 	 * Restores the node state from the log from a Two Phase Commit point of view.
 	 */
 	public TwoPhaseCommit(RPCNode node)
 	{
 		m_node = node;
 		_twoPhaseCommitContexts = new Hashtable<UUID, TwoPhaseCommitContext>();
 		
 		recover();
 		
 		try {
 			this.m_waitForVotesTimeoutMethod = Callback.getMethod("onWaitForVotesTimeout", this, new String[] { "java.util.UUID" });
 			this.m_waitForDecisionTimeoutMethod = Callback.getMethod("onWaitForDecisionTimeout", this, new String[] { "java.util.UUID" });
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * get2pcCoordinator()
 	 * 
 	 * @param coordinatorAddress
 	 * @return
 	 */
 	private I2pcCoordinator get2pcCoordinator(int coordinatorAddress)
 	{
 		if (!m_coordStubs.contains(coordinatorAddress))
 		{
 			I2pcCoordinator coordStub = m_node.connectTo2pcCoordinator(coordinatorAddress, this);
 			m_coordStubs.put(coordinatorAddress, coordStub);
 		}
 		
 		return m_coordStubs.get(coordinatorAddress);
 	}
 	
 	/**
 	 * get2pcParticipant()
 	 * 
 	 * @param participantAddress
 	 * @return
 	 */
 	private I2pcParticipant get2pcParticipant(int participantAddress)
 	{
 		if (!m_partcStubs.contains(participantAddress))
 		{
 			I2pcParticipant partStub = m_node.connectTo2pcParticipant(participantAddress, this);
 			m_partcStubs.put(participantAddress, partStub);
 		}
 		
 		return m_partcStubs.get(participantAddress);
 	}
 
 	/**
 	 * startTransaction()
 	 * 
 	 * @return
 	 */
 	public UUID startTransaction()
 	{
 		TwoPhaseCommitContext newContext = new TwoPhaseCommitContext(m_node.addr);
 		_twoPhaseCommitContexts.put(newContext.getId(), newContext);
 		return newContext.getId();
 	}
 
 	/**
 	 * addParticipant()
 	 * 
 	 * @param transactionId
 	 * @param participantId
 	 * @throws FacebookException
 	 */
 	public void addParticipant(UUID transactionId, int participantId) throws FacebookException
 	{
 		TwoPhaseCommitContext context;
 		context = _twoPhaseCommitContexts.get(transactionId);
 		context.addParticipant(participantId);
 	}
 	
 	/**
 	 * Starts a new two phase commit process. It tries to execute the passed command on the passed list of 
 	 * participants.
 	 */
 	public void startTwoPhaseCommit(UUID transactionId)
 	{
 		TwoPhaseCommitContext context;
 		context = _twoPhaseCommitContexts.get(transactionId);
 		
 		context.setInProgress(true);
 		
 		Callback cb = new Callback(this.m_waitForVotesTimeoutMethod, this, new Object[] { context.getId() });
 		m_node.addTimeout(cb, context.getTimeout());
 		
 		for (Participant participant : context.getParticipants()) {
 			I2pcParticipant stub;
 			stub = get2pcParticipant(participant.getId());
 			
 			try {
 				// Request that the participant prepare for commit and return a vote
 				stub.requestPrepare(String.valueOf(m_node.addr), transactionId.toString());
 				
 				// Remember which reply belongs to this participant
 				participant.setReplyId(RPCStub.getCurrentReplyId());
 				
 				// Remember which transaction the reply maps to.
 				// This is needed to protect against very-late replies.
 				m_replyMap.put(RPCStub.getCurrentReplyId(), transactionId);
 			} catch (RPCException ex) {
 				// Won't happen, stubs don't actually throw
 				ex.printStackTrace();
 			}
 		}
 				
 		saveContext(context);
 	}
 	
 	/**
 	 * abortTwoPhaseCommit()
 	 * 
 	 * @param twoPhaseCommitContextId
 	 */
 	public void abortTwoPhaseCommit(UUID twoPhaseCommitContextId)
 	{
 		TwoPhaseCommitContext context = _twoPhaseCommitContexts.get(twoPhaseCommitContextId);
 		
 		if (context == null)
 		{
 			m_node.error("abortTwoPhaseCommit: context not found.");
 			return;
 		}
 		
 		context.setDecision(Decision.Abort);
 		saveContext(context);
 		
 		Vector<Participant> participantsWhoVotedYes = context.getAllParticipantsWhoVotedYes();	
 		for (Participant participant : participantsWhoVotedYes) {
 			I2pcParticipant stub;
 			stub = get2pcParticipant(participant.getId());
 			
 			try {
 				// Request that the participant abort the transaction
 				stub.abortTransaction(String.valueOf(m_node.addr), twoPhaseCommitContextId.toString());
 			} catch (RPCException ex) {
 				// Won't happen, stubs don't actually throw
 				ex.printStackTrace();
 			}
 		}		
 		
 		// Notify frontend that the operation aborted
 		m_node.onTwoPhaseCommitComplete(twoPhaseCommitContextId, false /* aborted */);
 
 		context.setFinished(true);
 		saveContext(context);
 	}	
 
 	/**
 	 * abortOrCommit() is called by the participant to effectively commit or abort
 	 * this transaction.
 	 * 	 
 	 * @param context
 	 * @param abort
 	 */
 	private void abortOrCommit(TwoPhaseCommitContext context, boolean abort) {
 		try
 		{
 			Participant participant = _participantContext.getParticipant(m_node.addr);
 			if (_participantContext.getDecision() != Decision.NotDecided && participant.getFinished())
 			{
 				// Spew an error if this happens, for debugging purposes 
 				m_node.error("2PC inconsistency detected: must only abort/commit undecided transactions!");
 			}
 			
 			if (abort)
 			{
 				_participantContext.setDecision(Decision.Abort);
 				saveContext(_participantContext);
 				m_node.abort(context.getId());
 			}
 			else
 			{
 				_participantContext.setDecision(Decision.Commit);
 				saveContext(_participantContext);
 				m_node.commit(context.getId());
 			}		
 			
 			context.getParticipant(m_node.addr).setFinished(true);
 			saveContext(context);
 			
 			// Clear participant context so that we can start a new transaction
 			_participantContext = null;
 			deleteParticipantTwoPhaseCommitFile();
 		}
 		catch (Exception e)
 		{
 			m_node.error(e.getMessage());
 		}
 	}
 
 	private void deleteParticipantTwoPhaseCommitFile()
 	{
 		try {
 			if (Utility.fileExists(this.m_node, PARTICIPANT_FILENAME)) {
 				PersistentStorageWriter f = m_node.getWriter(PARTICIPANT_FILENAME, false);
 				f.delete();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * commitTwoPhaseCommit()
 	 * 
 	 * @param twoPhaseCommitContextId
 	 */
 	private void commitTwoPhaseCommit(UUID twoPhaseCommitContextId) {
 		TwoPhaseCommitContext context = _twoPhaseCommitContexts.get(twoPhaseCommitContextId);
 		
 		if (context == null)
 		{
 			m_node.error("commitTwoPhaseCommit: context not found.");
 			return;
 		}		
 		
 		context.setDecision(Decision.Commit);
 		saveContext(context);
 		
 		Vector<Participant> participants = context.getParticipants();	
 		for (Participant participant : participants) {
 			I2pcParticipant stub;
 			stub = get2pcParticipant(participant.getId());
 			
 			try {
 				// Request that the participant commit the transaction
 				stub.commitTransaction(String.valueOf(m_node.addr), twoPhaseCommitContextId.toString());
 			} catch (RPCException ex) {
 				// Won't happen, stubs don't actually throw
 				ex.printStackTrace();
 			}
 		}
 		
 		// Notify frontend that the operation committed
 		m_node.onTwoPhaseCommitComplete(context.getId(), true /* committed */);
 		
 		context.setFinished(true);
 		saveContext(context);
 	}
 	
 	
 	private void startTerminationProtocol(UUID twoPhaseCommitContextId)
 	{
 		if (_participantContext == null) 
 		{
 			m_node.error("receiveAbort: this node is not participanting in a transaction as a particiapnt.");
 			return;
 		}
 		
 		if (_participantContext.getId().compareTo(twoPhaseCommitContextId) != 0)
 		{
 			m_node.error("receiveAbort: this node is not participanting in this transaction as a particiapnt.");
 			return;
 		}
 		
 		Callback cb = new Callback(this.m_waitForDecisionTimeoutMethod, this, new Object[] { _participantContext.getId() });
 		m_node.addTimeout(cb, MessagingSettings.MaxTimeout);
 				
 		try 
 		{
 			// Only ask the coordinator
 			I2pcCoordinator stub = get2pcCoordinator(_participantContext.getCoordinatorId());
 			stub.queryDecision(twoPhaseCommitContextId.toString());
 			m_replyMap.put(RPCStub.getCurrentReplyId(), twoPhaseCommitContextId);
 		} 
 		catch (RPCException ex) 
 		{
 			// Won't happen, stubs don't actually throw
 			ex.printStackTrace();
 		}
 	}
 	
 	/**
 	 * onWaitForVotesTimeout() is called if all participant votes are not received
 	 * within some predetermined amount of time
 	 * 
 	 * @param twoPhaseCommitContextId
 	 */
 	public void onWaitForVotesTimeout(UUID twoPhaseCommitContextId)
 	{
 		TwoPhaseCommitContext context = _twoPhaseCommitContexts.get(twoPhaseCommitContextId);
 
 		if (context.getDecision() == Decision.NotDecided)
 		{
 			abortTwoPhaseCommit(twoPhaseCommitContextId);
 		}
 	}
 	
 	/**
 	 * onWaitForDecisionTimeout() is called if the coordinator does not reply to a 
 	 * decision request within some predetermined amount of time.
 	 *  
 	 * @param twoPhaseCommitContextId
 	 */
 	public void onWaitForDecisionTimeout(UUID twoPhaseCommitContextId)
 	{
		if (_participantContext == null) 
		{
			m_node.error("onWaitForDecisionTimeout: this node is not participanting in a transaction as a particiapnt.");
			return;
		}
		
 		if (_participantContext.getId().compareTo(twoPhaseCommitContextId) != 0)
 		{
 			m_node.error("onWaitForDecisionTimeout: this node is not participanting in this transaction as a particiapnt.");
 			return;
 		}
 		
 		// timed out and we didn't get to a decision, start timer again
 		if (_participantContext.getDecision() == Decision.NotDecided)
 		{
 			startTerminationProtocol(twoPhaseCommitContextId);
 		}
 		
 		// if we already reached a decision, don't have to do anything else.
 	}
 	
 	
 	private TwoPhaseCommitContext getPendingTwoPhaseCommitAsCoordinator()
 	{
 		Enumeration<TwoPhaseCommitContext> twoPhaseCommitContexts = _twoPhaseCommitContexts.elements();
 		while(twoPhaseCommitContexts.hasMoreElements())
 		{
 			TwoPhaseCommitContext twoPhaseCommitContext = twoPhaseCommitContexts.nextElement();
 			
 			if (twoPhaseCommitContext.getDecision() == Decision.NotDecided)
 			{				
 				return twoPhaseCommitContext;
 			}
 		}
 		
 		return null;
 	}
 	
 	
 	private void recover()
 	{
 		boolean running2pcTermination = false;
 		
 		m_node.info("2pc recovery in progress");
 		recoverContexts();
 		
 		TwoPhaseCommitContext coordinatorContext = getPendingTwoPhaseCommitAsCoordinator();
 		if (coordinatorContext != null)
 		{
 			if (coordinatorContext.getFinished())
 			{
 				m_node.error("Inconsistent state detected: must not be pending if already finished");
 			}
 			else
 			{
 				if (coordinatorContext.getDecision() == Decision.NotDecided)
 				{
 					// We haven't promised to commit, so it's ok to abort
 					m_node.info("2pc recovery: coordinator aborting pending transaction: " + coordinatorContext.getId().toString());
 					abortTwoPhaseCommit(coordinatorContext.getId());
 				}
 				else
 				{
 					// Simply mark as finished and move on
 					coordinatorContext.setFinished(true);
 					saveContext(coordinatorContext);
 				}
 			}
 		}
 		
 		if (_participantContext != null)
 		{
 			Participant participant = _participantContext.getParticipant(m_node.addr);
 			if (!participant.getFinished())
 			{
 				if (_participantContext.getDecision() == Decision.Commit)
 				{
 					m_node.info("2pc recovery: participant re-processing transaction commit: " + _participantContext.getId().toString());
 					abortOrCommit(_participantContext, false /* commit */);
 				}
 				else if (_participantContext.getDecision() == Decision.Abort)
 				{
 					m_node.info("2pc recovery: participant re-processing transaction abort: " + _participantContext.getId().toString());
 					abortOrCommit(_participantContext, true /* abort */);
 				}
 				else	
 				{
 					Vote vote = _participantContext.getParticipant(m_node.addr).getVote();
 					if (vote == Vote.No || vote == Vote.None)
 					{
 						m_node.info("2pc recovery: participant unilaterally aborting transaction: " + _participantContext.getId().toString());
 						_participantContext.getParticipant(m_node.addr).setVote(Vote.No);
 						_participantContext.getParticipant(m_node.addr).setDecision(Decision.Abort);
 						saveContext(_participantContext);
 						
 						abortOrCommit(_participantContext, true /* abort */);
 					}
 					else
 					{
 						m_node.info("2pc recovery: participant voted yes - running termination protocol: " + _participantContext.getId().toString());
 						startTerminationProtocol(_participantContext.getId());
 						running2pcTermination = true;
 					}
 				}
 			}
 		}
 
 		if (!running2pcTermination) {
 			m_node.info("2pc recovery complete");
 		} else {
 			m_node.info("BLOCKED: 2pc recovery won't be complete until the termination protocol concludes");
 		}
 	}
 	
 	private void recoverContexts()
 	{
 		String[] files = NodeUtility.listFiles(this.m_node);
 		
 		for (int i = 0; i < files.length; i++) {
 			if (files[i].startsWith("2pc-"))
 			{
 				try {
 					PersistentStorageReader psr = m_node.getReader(files[i]);
 					
 					String line;
 					StringBuffer buffer = new StringBuffer();
 					while ((line = psr.readLine()) != null) {
 						buffer.append(line);
 					}
 					
 					TwoPhaseCommitContext context = TwoPhaseCommitContext.deserialize(buffer.toString());
 					if (files[i].equals(PARTICIPANT_FILENAME))
 					{
 						_participantContext = context;
 					}
 					else
 					{
 						_twoPhaseCommitContexts.put(context.getId(), context);
 					}
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 			
 	private void saveContext(TwoPhaseCommitContext context) {		
 		try {
 			String fileName = context == _participantContext ? PARTICIPANT_FILENAME : "2pc-" + context.getId();
 			PersistentStorageWriter psw = m_node.getWriter(fileName, false);
 			psw.write(context.serialize());
 			psw.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * RPC: I2pcCoordinator.notifyPrepared()
 	 * (Implemented by coordinator, invoked by participant)
 	 */
 	@Override 
 	public String notifyPrepared(String transactionId) throws RPCException
 	{
 		// Not actually used. The vote is returned as part of reply_requestPrepare().
 		return null;
 	}
 
 	/**
 	 * RPC: I2pcCoordinator.notifyAborted()
 	 * (Implemented by coordinator, invoked by participant)
 	 */
 	@Override 
 	public String notifyAborted(String transactionId) throws RPCException
 	{
 		// Not actually used. Participants don't proactively notify aborts, instead 
 		// they return errors when the coordinator RPC-calls.
 		return null;
 	}
 
 	/**
 	 * RPC: I2pcCoordinator.queryDecision()
 	 * (Implemented by coordinator, invoked by participant)
 	 * 
 	 * Method called by participants when they need to know the decision
 	 * on a transaction (e.g. during recovery).
 	 */
 	@Override 
 	public String queryDecision(String transactionId) throws RPCException
 	{
 		UUID twoPhaseCommitContextId = UUID.fromString(transactionId);
 		
 		TwoPhaseCommitContext context = _twoPhaseCommitContexts.get(twoPhaseCommitContextId);
 		if (context == null) 
 		{
 			m_node.error("receiveDecisionRequest: context not found.");
 			return Decision.Abort.toString();
 		}
 		
 		return context.getDecision().toString();
 	}
 	
 
 	/**
 	 * RPC: I2pcParticipant.startTransaction
 	 * (Implemented by participant, invoked by coordinator)
 	 */
 	@Override 
 	public String startTransaction(String coordId, String transactionId) throws RPCException		
 	{
 		// Not actually used, the transaction is auto-started by the participant
 		// upon receiving the write_message_all command.
 		return null;
 	}
 	
 	/**
 	 * RPC: I2pcParticipant.requestPrepare
 	 * (Implemented by participant, invoked by coordinator)
 	 * 
 	 * Method on the participant that handles the Vote Request.
 	 */
 	@Override 
 	public String requestPrepare(String coordAddress, String transactionId) throws RPCException
 	{
 		Integer coordId = Integer.parseInt(coordAddress);
 		UUID twoPhaseCommitContextId = UUID.fromString(transactionId);
 		
 		if (_participantContext == null)
 		{
 			_participantContext = new TwoPhaseCommitContext(twoPhaseCommitContextId, coordId);	
 
 			try {
 				// Add itself as a participant for book-keeping purposes
 				_participantContext.addParticipant(m_node.addr);
 			} catch (FacebookException e) {
 				// won't happen - single participant
 			}
 		}
 		
 		// If this is a request for our current transaction and we have already voted, just re-send the vote.
 		if (_participantContext.getId().compareTo(twoPhaseCommitContextId) == 0 &&
     		(_participantContext.getParticipant(m_node.addr).getVote() != Vote.None || 
 			 _participantContext.getDecision() != Decision.NotDecided))
 		{
 			String vote;
 			
 			if (_participantContext.getDecision() == Decision.Commit)
 			{
 				vote = Vote.Yes.toString();
 			}
 			else if (_participantContext.getDecision() == Decision.Abort)
 			{
 				vote = Vote.No.toString();
 			}
 			else
 			{
 				vote = _participantContext.getParticipant(m_node.addr).getVote().toString();
 			}
 			
 			return vote;
 		}
 			
 		// Prepare for commit
 		boolean saved = m_node.prepare(twoPhaseCommitContextId);
 		if (!saved)
 		{
 			_participantContext.getParticipant(m_node.addr).setVote(Vote.No);
 			_participantContext.setDecision(Decision.Abort);
 			saveContext(_participantContext);
 			
 			abortOrCommit(_participantContext, true /* abort */);
 			
 			return Vote.No.toString();
 		}		
 		else
 		{
 			_participantContext.getParticipant(m_node.addr).setVote(Vote.Yes);
 			saveContext(_participantContext);
 			
 			Callback cb = new Callback(this.m_waitForDecisionTimeoutMethod, this, new Object[] { _participantContext.getId() });
 			m_node.addTimeout(cb, _participantContext.getTimeout());
 			
 			return Vote.Yes.toString();
 		}
 	}
 	
 	/**
 	 * RPC: I2pcParticipant.commitTransaction
 	 * (Implemented by participant, invoked by coordinator)
 	 */
 	@Override 
 	public String commitTransaction(String coordId, String transactionId) throws RPCException
 	{
 		UUID twoPhaseCommitContextId = UUID.fromString(transactionId);
 		
 		if (_participantContext == null) 
 		{
 			m_node.error("commitTransaction: this node is not participanting in a transaction as a particiapnt.");
 			return null;
 		}
 		
 		if (_participantContext.getId().compareTo(twoPhaseCommitContextId) != 0)
 		{
 			m_node.error("commitTransaction: this node is not participanting in this transaction as a particiapnt.");
 			return null;
 		}
 		
 		abortOrCommit(_participantContext, false /* commit */);
 		return null;
 	}
 	
 	/**
 	 * RPC: I2pcParticipant.abortTransaction
 	 * (Implemented by participant, invoked by coordinator)
 	 * 
 	 * Called by the coordinator to notify this participant that it must abort the transaction
 	 */
 	@Override 
 	public String abortTransaction(String coordId, String transactionId) throws RPCException
 	{
 		UUID twoPhaseCommitContextId = UUID.fromString(transactionId);		
 		
 		if (_participantContext == null) 
 		{
 			m_node.error("abortTransaction: this node is not participanting in a transaction as a particiapnt.");
 			return null;
 		}
 		
 		if (_participantContext.getId().compareTo(twoPhaseCommitContextId) != 0)
 		{
 			m_node.error("abortTransaction: this node is not participanting in this transaction as a particiapnt.");
 			return null;
 		}
 		
 		abortOrCommit(_participantContext, true /* abort */);
 		return null;
 	}
 	
 	/**
 	 * RPC: I2pcCoordinatorReply.reply_notifyPrepared
 	 * (Implemented by participant, auto-called in response to I2pcCoordinator.notifyPrepared)
 	 * 
 	 * Method on the coordinator that handles Votes from the participants.
 	 */
 	public void reply_notifyPrepared(int replyId, int sender, int result, String reply)
 	{
 		// Nothing do to here for this reply
 		return;
 	}
 	
 	/**
 	 * RPC: I2pcCoordinatorReply.reply_notifyAborted
 	 * (Implemented by participant, auto-called in response to I2pcCoordinator.notifyAborted)
 	 */
 	public void reply_notifyAborted(int replyId, int sender, int result, String reply)
 	{
 		// Nothing do to here for this reply
 		return;
 	}
 	
 	/**
 	 * RPC: I2pcCoordinatorReply.reply_queryDecision
 	 * (Implemented by participant, auto-called in response to I2pcCoordinator.queryDecision)
 	 */
 	public void reply_queryDecision(int replyId, int sender, int result, String reply)
 	{
 		if (result != 0)
 		{
 			// Remote call failed.
 			
 			// TODO: what to do here? recovery can't proceed if we don't get a decision.
 			// should probably keep resending the request.
 			
 			return;
 		}
 			
 		// Here, the call succeeded and the coordinator returned a decision
 
 		int coordinatorId = sender;
 		UUID twoPhaseCommitContextId = m_replyMap.get(replyId);
 		
 		if (_participantContext == null || _participantContext.getId().compareTo(twoPhaseCommitContextId) != 0) 
 		{
 			m_node.error("reply_queryDecision: context not found.");
 			return;
 		}
 
 		try
 		{
 			if (reply.equalsIgnoreCase("Commit"))
 			{
 				commitTransaction(String.valueOf(coordinatorId), twoPhaseCommitContextId.toString());
 			}
 			else if (reply.equalsIgnoreCase("Abort"))
 			{
 				abortTransaction(String.valueOf(coordinatorId), twoPhaseCommitContextId.toString());
 			}
 			else 
 			{
 				// Still undecided
 			}
 		} 
 		catch (RPCException ex)
 		{
 			ex.printStackTrace();
 		}
 	}
 		
 	
 	
 	/**
 	 * RPC: I2pcParticipantReply.reply_startTransaction
 	 * (Implemented by coordinator, auto-called in response to I2pcParticipant.startTransaction)
 	 */
 	@Override
 	public void reply_startTransaction(int replyId, int sender, int result, String reply)
 	{
 		// Nothing do to here for this reply
 		return;
 	}
 
 	/**
 	 * RPC: I2pcParticipantReply.reply_requestPrepare
 	 * (Implemented by coordinator, auto-called in response to I2pcParticipant.requestPrepare)
 	 * 
 	 * Method on the coordinator that handles Votes from the participants.
 	 */
 	@Override
 	public void reply_requestPrepare(int replyId, int sender, int result, String reply)
 	{
 		if (result != 0)
 		{
 			// Remote call failed.
 			// If this transaction is ongoing, abort it now.
 			
 			UUID transactionId = m_replyMap.get(replyId);
 			TwoPhaseCommitContext context = _twoPhaseCommitContexts.get(transactionId);
 		
 			if (context.getDecisionBasedOnVotes() == Decision.NotDecided) 
 			{
 				// Must abort the transaction
 				abortTwoPhaseCommit(transactionId);
 			}
 			else if (context.getDecisionBasedOnVotes() == Decision.Commit)
 			{
 				// Won't happen if the code is right. It if isn't, make sure we notice it.
 				m_node.error("CATASTROPHIC FAILURE! Trying to abort a committed transaction!");
 			}
 			else
 			{
 				// already aborted
 			}
 			
 			return;
 		}
 			
 		// Here, the call succeeded and the participant returned a vote
 
 		int participantId = sender;
 		UUID twoPhaseCommitContextId = m_replyMap.get(replyId);
 		
 		TwoPhaseCommitContext context = _twoPhaseCommitContexts.get(twoPhaseCommitContextId);
 		if (context == null) 
 		{
 			m_node.error("reply_requestPrepare: context not found.");
 			return;
 		}
 		
 		Participant participant = context.getParticipant(participantId);
 		if (participant.getVote() == Vote.None)
 		{
 			String vote = reply;
 			
 			if (vote.equals("Yes"))
 			{
 				participant.setVote(Vote.Yes);
 			}
 			else if (vote.equals("No"))
 			{
 				participant.setVote(Vote.No);				
 			}
 			else
 			{
 				m_node.error("reply_requestPrepare: Unknown vote type");
 				return;
 			}
 		}
 		else
 		{
 			m_node.warn("reply_requestPrepare: already received vote for participant.");
 		}
 		
 		switch(context.getDecisionBasedOnVotes())
 		{
 			case Commit:
 				commitTwoPhaseCommit(twoPhaseCommitContextId);
 				break;
 				
 			case Abort:
 				abortTwoPhaseCommit(twoPhaseCommitContextId);
 				break;
 				
 			default:
 				m_node.info("reply_requestPrepare: haven't decided yet.");
 				break;
 		}		
 	}
 
 	/**
 	 * RPC: I2pcParticipantReply.reply_commitTransaction
 	 * (Implemented by coordinator, auto-called in response to I2pcParticipant.commitTransaction)
 	 */
 	@Override
 	public void reply_commitTransaction(int replyId, int sender, int result, String reply)
 	{
 		// Nothing do to here for this reply
 		return;
 	}
 
 	/**
 	 * RPC: I2pcParticipantReply.reply_abortTransaction
 	 * (Implemented by coordinator, auto-called in response to I2pcParticipant.abortTransaction)
 	 */
 	@Override
 	public void reply_abortTransaction(int replyId, int sender, int result, String reply)
 	{
 		// Nothing do to here for this reply
 		return;
 	}
 }
 
 
 
 
