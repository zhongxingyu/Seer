 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.*;
 
 import edu.washington.cs.cse490h.lib.PersistentStorageReader;
 import edu.washington.cs.cse490h.lib.Utility;
 
 public class LearnerLayer {
 	public static final String LEARN_FILE = ".learned";
 	public static final String OUT_OF_ORDER = ".outOfOrder";
 	
 	private PaxosLayer paxosLayer;
 	private HashMap<Integer, Proposal> proposals; //these are accepted proposals that haven't been processed, instance num, pro num, transaction string
 	private HashMap<Integer, HashMap<Integer, Integer>> proposalCount; //instance num, proposal num, proposal count
 	private HashMap<Integer, Proposal> outOfOrder;
 	
 	//TODO: SEND TO TXN LAYER WHEN ADDED TO THIS MAP, ALSO DETERMINE IF ABORT OR COMMIT ON THIS LAYER
 	private HashMap<Integer, Proposal> learned; //instance num, proposal num
 	private DistNode n;
 	private int lastContInstance;
 	private int largestInstanceNum;
 	
 	public LearnerLayer(PaxosLayer paxosLayer) {
 		this.paxosLayer = paxosLayer;
 		this.n = this.paxosLayer.n;
 		this.lastContInstance = -1;
 		this.largestInstanceNum = -1;
 		
 		this.proposals = new HashMap<Integer, Proposal>();
 		this.proposalCount = new HashMap<Integer, HashMap<Integer,Integer>>();
 		this.outOfOrder = new HashMap<Integer, Proposal>();
 		this.learned = new HashMap<Integer, Proposal>();
 	}
 	
 	public void start() {
 		this.readLog();
 		this.readOutOfOrder();
 	}
 	
 	private void updateLargestInstanceNum( int instanceNum ) {
 		if( instanceNum > this.largestInstanceNum ) {
 			this.largestInstanceNum = instanceNum;
 		}
 	}
 	
 	public void receive(int from, PaxosPacket pkt) {
 		if( pkt.getProtocol() == PaxosProtocol.ACCEPT ) {
 			//we are the distinguished learner
 			this.receivedAccept(from, pkt);
 		} else {
 			//we are just a learner
 			this.receivedLearn(from, pkt);
 		}
 	}
 	
 	private void receivedLearn(int from, PaxosPacket pkt){
 		Proposal p = new Proposal(pkt);
 		executeProposal( p );
 	}
 	
 	
 	private void executeProposal( Proposal p ) {
 		int iNum = p.getInstanceNum();
 		
 		proposals.put(iNum, p ); //add this proposal to our proposals...
 		updateLargestInstanceNum(iNum);
 		
 		if( iNum == this.lastContInstance + 1 ) {
 			this.lastContInstance++;
 			//ok we can process this
 			this.pushProposalValueToDisk(p);
 			
 			//add to our learned
 			learned.put( iNum, p );
 			this.outOfOrder.remove( iNum );
 			writeToLearnedLog();
 			writeOutOfOrder();
 			
 			//now see if we can execute more proposals 
 			this.executeNextLearnedProposal();
 		} else if( iNum > this.lastContInstance + 1 ) {
 			//ok we are missing something so write this proposal to the out of order log
 			this.outOfOrder.put( iNum, p );
 			writeOutOfOrder( );
 		}
 	}
 	
 	private void executeNextLearnedProposal() {
 		Proposal p = proposals.get(this.lastContInstance + 1); //the next higher instance
 		if( p != null )
 			executeProposal( p );
 	}
 	
 	private void pushProposalValueToDisk( Proposal p ) {
 		Transaction txn = Transaction.fromString( p.getValue(), this.paxosLayer.getTransactionLayer().cache );
 		boolean committed = commit( txn );
 
 		if( !committed ) {
 			//replace value of this proposal with txid followed by ABORT
 			p.setValue( txn.id + "#ABORT" );
 		}
 	}
 	
 	private boolean  commit( Transaction txn ) {
 		//call the transaction layer to see if we can commit
 		return this.paxosLayer.getTransactionLayer().paxosFinished(txn);
 	}
 	
 	
 	
 	/**
 	 * Leader methods
 	 * 
 	 * 
 	 * 
 	 */
 	
 	
 	private void sendChosenToAllLearners( Proposal p ) {
 		int[] learners = PaxosLayer.LEARNERS;
 		PaxosPacket learnPacket = new PaxosPacket(PaxosProtocol.LEARN, p.getProposalNum(), p.getInstanceNum(), Utility.stringToByteArray(p.getValue()));
 		for( int learner : learners ) {
 			if( learner != this.n.addr ) //do not send to ourself
 				this.send(learner, learnPacket);
 		}
 	}
 	
 	private void receivedAccept(int from, PaxosPacket pkt){
 		//current number of the instance/proposal number we have seen so far
 		HashMap<Integer, Integer> instanceHash = proposalCount.get(pkt.getInstanceNumber());
 		if( instanceHash == null ) {
 			instanceHash = new HashMap<Integer, Integer>();
 			proposalCount.put( pkt.getInstanceNumber(), instanceHash );
 		}
 		Integer pcount = instanceHash.get(pkt.getProposalNumber());
 		if( pcount == null ) {
 			pcount = 1;
 		} else {
 			pcount++;
 		}
 		//increment our proposer count
 		proposalCount.get(pkt.getInstanceNumber()).put(pkt.getProposalNumber(), pcount);
 		
 		int majority;
 		int numAcceptors = PaxosLayer.ACCEPTORS.length;
 		if( numAcceptors % 2 == 1 ) {
 			majority = numAcceptors / 2 + 1;
 		} else {
 			majority = numAcceptors / 2;
 		}
 		
 		//if we have the majority then do stuff
 		if( pcount >= majority ) {
 			Proposal p = new Proposal( pkt );
 			proposalCount.remove(p.getInstanceNum()); //not sure if we should remove from our counts.
 			this.masterLearn( p );
 		}
 	}
 	
 	/**
 	 * Takes a proposal and pushes it to disk as well as tells all the other learners
 	 * @param p
 	 */
 	private void masterLearn( Proposal p ) {
 		executeProposal(p);
 		sendChosenToAllLearners( p );
 	}
 	
 	private void send(int dest, PaxosPacket pkt){
 		this.paxosLayer.send(dest, pkt);
 	}
 	
 	
 	
 	/**
 	 * Methods used by proposer
 	 * 
 	 * 
 	 */
 	
 	
 	public ArrayList<Integer> getMissingInstanceNums() {
 		ArrayList<Integer> missing = new ArrayList<Integer>();
 			try {
 				PersistentStorageReader r = this.n.getReader(LEARN_FILE);
 				String line = r.readLine();
 				while( line != null ) {
 					String[] entry = line.split("|");
 					int instanceNum = Integer.parseInt(entry[0]);
 					int proposalNum = Integer.parseInt(entry[1]);
 					String value = entry[2];
 					learned.put( instanceNum, new Proposal( instanceNum, proposalNum, value ) );
 					updateLargestInstanceNum( instanceNum );
 				}
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		for( int i = this.lastContInstance + 1; i < this.largestInstanceNum; i++ ) {
 			Proposal p = this.proposals.get(i);
 			if( p == null ) {
 				missing.add(i);
 			}
 		}
 		return missing;
 	}
 	
 	public int getLargestInstanceNum() {
 		return this.largestInstanceNum;
 	}
 	
 	public void writeValue( Proposal p ){
 		this.masterLearn(p);
 	}
 	
 	public int learnedProposalNumForInstance( int instanceNum ) {
 		return this.learned.get( instanceNum ).getProposalNum();
 	}
 	
 	
 	
 	/**
 	 * Disk methods
 	 * 
 	 * 
 	 */
 	
 	
 	private void readLog() {
 		try {
 			PersistentStorageReader r = this.n.getReader(LEARN_FILE);
 			String line = r.readLine();
 			while( line != null ) {
 				String[] entry = line.split("|");
 				int instanceNum = Integer.parseInt(entry[0]);
 				int proposalNum = Integer.parseInt(entry[1]);
 				String value = entry[2];
 				Proposal p = new Proposal( instanceNum, proposalNum, value );
 				learned.put( instanceNum, p );
 				updateLargestInstanceNum( instanceNum );
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private void writeToLearnedLog() {
 		String learnContent = "";
 		for( Integer iNum : this.learned.keySet() ) {
			learnContent += this.learned.get( iNum ).toString() + "\n";
 		}
 		
 		try {
 			this.n.write(LEARN_FILE, learnContent, false, true);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void readOutOfOrder() {
 		try {
 			PersistentStorageReader r = this.n.getReader(OUT_OF_ORDER);
 			String line = r.readLine();
 			while( line != null ) {
 				String[] entry = line.split("|");
 				int instanceNum = Integer.parseInt(entry[0]);
 				int proposalNum = Integer.parseInt(entry[1]);
 				String value = entry[2];
 				Proposal p = new Proposal( instanceNum, proposalNum, value );
 				proposals.put( instanceNum, p);
 				outOfOrder.put( instanceNum, p);
 				updateLargestInstanceNum( instanceNum );
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void writeOutOfOrder() {
 		String outoforderContent = "";
 		for( Integer iNum : this.outOfOrder.keySet() ) {
 			Proposal p = this.outOfOrder.get( iNum );
 			outoforderContent += p.toString() + "\n";
 		}
 		try {
 			this.n.write(OUT_OF_ORDER, outoforderContent, false, true);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String getLearnedForInstance(int instanceNum) {
 		Proposal p = learned.get( instanceNum );
 		if( p != null ) { 
 			return p.getValue();
 		}
 		return null;
 	}
 	
 	
 	
 }
