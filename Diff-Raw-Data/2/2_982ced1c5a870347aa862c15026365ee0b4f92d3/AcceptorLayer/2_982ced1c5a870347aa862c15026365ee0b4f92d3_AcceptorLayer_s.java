 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.*;
 
 import edu.washington.cs.cse490h.lib.PersistentStorageReader;
 import edu.washington.cs.cse490h.lib.Utility;
 
 public class AcceptorLayer {
 
 	private static final String ACCEPT_FILE = ".acceptor_record";
 	private static final String PROMISE_FILE = ".promises";
 		
 	
 	private PaxosLayer paxosLayer;
 	
 	private HashMap<Integer, Proposal> acceptorRecord; //instance num -> accepted proposal
 	private HashMap<Integer, Integer> promised; //instance num -> highest promised or accepted proposal number
 	private DistNode n;
 	
 	public AcceptorLayer(PaxosLayer paxosLayer) {
 		this.paxosLayer = paxosLayer;
 		this.n = this.paxosLayer.n;
 		this.acceptorRecord = new HashMap<Integer, Proposal>();
 		this.promised = new HashMap<Integer, Integer>();
 	}
 	
 	public void start() {
 		this.readLogs();
 	}
 	
 	public void send(int dest, PaxosPacket pkt){
 		this.paxosLayer.send(dest, pkt);
 	}
 	
 	public void receivedPrepare(int from, PaxosPacket pkt){
 		
 		int newProposalNum = pkt.getProposalNumber();
 		
 		Integer promisedValue = promised.get( pkt.getInstanceNumber() );
 		
 		Proposal acceptedProposal = acceptorRecord.get(pkt.getInstanceNumber());
 		
 		PaxosPacket response;
 		if( promisedValue == null ||  newProposalNum >= promisedValue ) {
 			promised.put( pkt.getInstanceNumber(), pkt.getProposalNumber());
 			if( acceptedProposal == null ) {
 				response = new PaxosPacket(PaxosProtocol.PROMISE, pkt.getProposalNumber(), pkt.getInstanceNumber(), new byte[0] );
 			} else {
 				response = acceptedProposal.getPaxosPacket(PaxosProtocol.PROMISE);
 			}
 			
 			//write to disk before our response
 			updateState();
 			
 		} else {
 			//REJECTION OOOOHHHH BURRRRNNN
 			response = reject(pkt.getInstanceNumber(), promisedValue, acceptedProposal );
 		}
 		
 		//send the response
 		this.send(from, response);
 	}
 	
 	public void receivedPropose(int from, PaxosPacket pkt){
 		Proposal acceptedProposal = acceptorRecord.get(pkt.getInstanceNumber());
 		Integer promisedValue = promised.get( pkt.getInstanceNumber() );
 		
 		if( acceptedProposal == null && ( promisedValue == null || pkt.getProposalNumber() >= promisedValue ) ) {
 			acceptedProposal = new Proposal( pkt );
 			acceptorRecord.put( pkt.getInstanceNumber(), acceptedProposal );
 			promised.put( pkt.getInstanceNumber(), pkt.getProposalNumber() );
 			
 			updateState();
 			
 			this.send(from, acceptedProposal.getPaxosPacket(PaxosProtocol.ACCEPT));
 			
 		} else {
 			//REJECTION DAMNNN
 			this.send(from, reject(pkt.getInstanceNumber(), promisedValue, acceptedProposal ));
 		}
 	}
 	
 	public PaxosPacket reject(int instanceNum, int promisedValue, Proposal acceptedProposal ) {
 		PaxosPacket response;
 		if( acceptedProposal == null ) {
 			response = new PaxosPacket(PaxosProtocol.REJECT, promisedValue, instanceNum, new byte[0]);
 		} else {
 			response = acceptedProposal.getPaxosPacket(PaxosProtocol.REJECT);
 		}
 		return response;
 	}
 
 	public void receivedRecovery(int from, PaxosPacket pkt) {
 		int instanceNum = pkt.getInstanceNumber();
 		
 		String response = this.paxosLayer.getLearnerLayer().getLearnedForInstance( instanceNum );
 		if( response == null ) {
 			Proposal acceptedProposal = this.acceptorRecord.get(instanceNum);
 			if( acceptedProposal == null ) {
 				//not quite sure
 			} else {
 				this.send(from, acceptedProposal.getPaxosPacket(PaxosProtocol.RECOVERY_ACCEPTED));
 			}
 			
 		} else {
 			PaxosPacket chosen = new PaxosPacket( PaxosProtocol.RECOVERY_CHOSEN, -1, instanceNum, Utility.stringToByteArray(response));
 			this.send( from, chosen );
 		}
 	}
 	
 	
 	//write the acceptorRecord and promises to disk
 	private void updateState() {
 		try {
 			String fileContents = "";
 			for( int instanceNum : acceptorRecord.keySet() ) {
 				Proposal p = acceptorRecord.get(instanceNum);
 				fileContents += p.toString() + "\n";
 			}
 			this.n.write(ACCEPT_FILE, fileContents, false, true);
 			
 			String promisedContents = "";
 			for( int instanceNum : promised.keySet() ) {
				fileContents += instanceNum + " " + promised.get( instanceNum ) + "\n";
 			}
 			this.n.write(PROMISE_FILE, promisedContents, false, true);
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void readLogs() {
 		PersistentStorageReader r;
 		try {
 			r = this.n.getReader(ACCEPT_FILE);
 			String line = r.readLine();
 			while( line != null ) {
 				String[] entry = line.split("|");
 				Proposal p = new Proposal( Integer.parseInt(entry[0]), Integer.parseInt(entry[1]), entry[2] );
 				this.acceptorRecord.put(p.getInstanceNum(), p);
 			}
 			
 			r = this.n.getReader(PROMISE_FILE);
 			line = r.readLine();
 			while( line != null ) {
 				String[] entry = line.split(" ");
 				this.promised.put(Integer.parseInt(entry[0]), Integer.parseInt(entry[1]));
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	
 }
