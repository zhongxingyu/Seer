 package node.facebook;
 
 import java.util.UUID;
 import java.util.Vector;
 
 import node.rpc.RPCNode;
 import node.rpc.I2pcParticipant;
 import node.twophasecommit.TwoPhaseCommit;
 
 public class FacebookRPCNode extends RPCNode {
 	private FacebookShardSystem m_shard;
 	private FacebookFrontendSystem m_frontEnd;
 	private TwoPhaseCommit m_twoPhase;
 
 	private static Vector<Integer> s_shardAddresses = new Vector<Integer>();
 
 	public static final String ERROR_MESSAGE_FORMAT = "Error: %s on facebook server %d. Returned error code was %s";
 
 	static {
 		for (int i = 1; i <= 5; i++) {
 			s_shardAddresses.add(i);
 		}
 	}
 	
 	public FacebookRPCNode() {
 		super();
 	}
 
 	@Override
 	public void start() {
 		super.start();
 		
 		this.user_info("Starting frontend instance on address " + this.addr);
 		this.m_frontEnd = new FacebookFrontendSystem(this);
 		
 		this.user_info("Starting sharding instance on address " + this.addr);
 		this.m_shard = new FacebookShardSystem(this);
 
 		// Enable this node to receive RPC calls for these interfaces
 		this.bindFacebookServerImpl(m_shard);
 
 		if (this.m_shard != null) {
 			this.m_shard.recoverFromCrash();
 		}
 		
 		// Create the 2pc after we recover from the crash otherwise the state will be messed up
 		this.user_info("Starting 2PC coordinator support on address " + this.addr);
 		this.m_twoPhase = new TwoPhaseCommit(this);
		
		this.bind2pcCoordinatorImpl(m_twoPhase);
		this.bind2pcParticipantImpl((I2pcParticipant) m_twoPhase);
 	}
 	
 	public static Vector<Integer> getShardAddresses() {
 		return s_shardAddresses;
 	}
 
 	public TwoPhaseCommit get2PC() {
 		return m_twoPhase;
 	}
 
 	@Override
 	public void onCommand(String command) {		
 		if (this.m_frontEnd != null) {
 			m_frontEnd.onCommand(command);
 		} 
 	}
 
 	@Override
 	protected void onConnectionAborted(int endpoint) {
 		this.user_info("connection aborted!");
 	}
 
 	private void user_info(String s) {
 		// Use a different prefix to be easy to distinguish
 		System.out.println(">>>> " + s);
 	}
 	
 	/**
 	 * Called by 2pc to inform the node that it must abort the 
 	 * active transaction.
 	 */
 	@Override
 	public void abort(UUID transactionId)
 	{
 		m_shard.abort(transactionId);
 	}
 	
 	/**
 	 * Called by 2pc to inform the node that it must commit the 
 	 * active transaction.
 	 */
 	@Override
 	public void commit(UUID transactionId)
 	{
 		m_shard.commit(transactionId);
 	}
 	
 	/**
 	 * Called by 2pc to inform the node that it should prepare to commit
 	 * the active transaction (i.e. save state in durable storage).
 	 * Returns true if the state is properly saved, false otherwise.
 	 */
 	@Override
 	public boolean prepare(UUID transactionId)
 	{
 		return m_shard.prepare(transactionId);
 	}
 
 	/**
 	 * onTwoPhaseCommitComplete() is called by the 2PC coordinator when
 	 * the transaction commits or aborts
 	 */
 	public void onTwoPhaseCommitComplete(UUID transactionId, boolean committed)
 	{
 		m_frontEnd.onTwoPhaseCommitComplete(transactionId, committed);
 	}
 }
 
