 package se.kth.ict.id2203.assignment4.ac;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import se.kth.ict.id2203.assignment3.beb.BebBroadcastPort;
 import se.kth.ict.id2203.pp2p.PerfectPointToPointLink;
 import se.sics.kompics.ComponentDefinition;
 import se.sics.kompics.Handler;
 import se.sics.kompics.Negative;
 import se.sics.kompics.Positive;
 
 public class AcComponent extends ComponentDefinition {
 
 	private Positive<PerfectPointToPointLink> pp2pPort = requires(PerfectPointToPointLink.class);
 	private Positive<BebBroadcastPort> bebPort = requires(BebBroadcastPort.class);
 	private Negative<AcPort> acPort = provides(AcPort.class);
 
 	private Set<Integer> seenIds = new HashSet<Integer>();
 	private Map<Integer, Integer> tempValue = new HashMap<Integer, Integer>();
 	private Map<Integer, Integer> value = new HashMap<Integer, Integer>();
 	private Map<Integer, Integer> writeAck = new HashMap<Integer, Integer>();
 	private Map<Integer, Integer> rts = new HashMap<Integer, Integer>();
 	private Map<Integer, Integer> wts = new HashMap<Integer, Integer>();
 	private Map<Integer, Integer> tstamp = new HashMap<Integer, Integer>();
 	private Map<Integer, Set<TupleTsVal>> readSet = new HashMap<Integer, Set<TupleTsVal>>();
 
 	private int majority;
 	private int rank;
 	private int numberOfNodes;
 
 	public AcComponent() {
 		subscribe(initHandler, control);
 		subscribe(initAcPropose, acPort);
 	}
 
 	private void initInstance(int consensusId) {
 		if (!seenIds.contains(consensusId)) {
 			tempValue.put(consensusId, null);
 			value.put(consensusId, null);
 			writeAck.put(consensusId, 0);
 			rts.put(consensusId, 0);
 			wts.put(consensusId, 0);
 			tstamp.put(consensusId, rank);
 			readSet.put(consensusId, new HashSet<TupleTsVal>());
 			seenIds.add(consensusId);
 		}
 	}
 
 	Handler<AcInit> initHandler = new Handler<AcInit>() {
 		public void handle(AcInit event) {
 			majority = 1 + (event.getNumberOfNodes() / 2);
 			rank = event.getNodeId();
 			numberOfNodes = event.getNumberOfNodes();
 		}
 	};
 	
 	Handler<AcPropose> initAcPropose = new Handler<AcPropose>() {
 		public void handle(AcPropose event) {
			int id = event.getConsensusId();
 			initInstance(id);
			int oldTstamp = tstamp.get(id);
			tstamp.put(id,oldTstamp+numberOfNodes);
			tempValue.put(event.getConsensusId(), event.getValue());
 		}
	};
 
 
 
 
 }
