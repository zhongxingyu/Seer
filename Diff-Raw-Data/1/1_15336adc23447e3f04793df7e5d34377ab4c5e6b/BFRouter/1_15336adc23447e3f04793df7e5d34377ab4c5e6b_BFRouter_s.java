 package netproj.routers;
 
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import netproj.skeleton.Packet;
 
 public class BFRouter extends Router {
 	private static final Logger logger = Logger.getLogger(BFRouter.class.getCanonicalName());
 
 	// Maps router addresses of routers that are only one hop away to
 	// (time it takes to make one hop, gate at which router resides) pairs.
 	private HashMap<Integer, TimeGatePair> oneHop;
 	private HashMap<Integer, TimeGatePair> timeEstimates;
 	
 	private OneHopPinger pinger;
 	
 	public BFRouter(int inputBuffSize, int outputBuffSize, int address) {
 		super(inputBuffSize, outputBuffSize, address);
 		oneHop = new HashMap<Integer, TimeGatePair>();
 		timeEstimates = new HashMap<Integer, TimeGatePair>();
 		pinger = new OneHopPinger();
 	}
 	
 	public void start() {
 		pinger.start();
 		super.start();
 	}
 	
 	public void processPacket(Packet msg) {
 		if (msg instanceof BFWeightPacket) {
 			// this is a packet of updated weights -- we only care if it's coming from one hop away
 			BFWeightPacket wmsg = (BFWeightPacket)msg;
 			TimeGatePair msgCost = oneHop.get(wmsg.getSourceAddress());
 			if (msgCost != null) {
 				// we have changes to consider, so prepare a packet to send
 				BFWeightPacket newBroadcast = new BFWeightPacket(getAddress(), -1, 32*8, 1);
 				for (WeightUpdate u : wmsg.getWeights()) {
 					TimeGatePair currCost = timeEstimates.get(u.getDestAddress());
 					int newWeight = msgCost.getTime() + u.getWeight();
 					if (currCost == null) {
 						// this is an all-new entry
 						
 						// create time estimate
 						timeEstimates.put(u.getDestAddress(),
 								new TimeGatePair(newWeight, msgCost.getGate()));
 						
 						// create routing table entry
 						addRoutingTableEntry(u.getDestAddress(), 24, msgCost.getGate());
 						
 						// prepare the update to be broadcast
 						newBroadcast.addWeight(u.getDestAddress(), newWeight);
 					} else if (currCost.getGate() == msgCost.getGate()) {
 						newWeight = (int)(.9 * currCost.getTime() + .1 * newWeight);
 						// update time estimate
 						timeEstimates.put(u.getDestAddress(),
 								new TimeGatePair(newWeight, msgCost.getGate()));
 						
 						// prepare the update to be broadcast
 						if (Math.abs(currCost.getTime() - newWeight) > 5)
 							newBroadcast.addWeight(u.getDestAddress(), newWeight);
 					} else if (newWeight < currCost.getTime()) {
 						newWeight = (int)(.9 * currCost.getTime() + .1 * newWeight);
 						// update time estimate
 						timeEstimates.put(u.getDestAddress(),
 								new TimeGatePair(newWeight, msgCost.getGate()));
 						
 						// update routing table entry
 						addRoutingTableEntry(u.getDestAddress(), 24, msgCost.getGate());
 						
 						// prepare the update to be broadcast
 						newBroadcast.addWeight(u.getDestAddress(), newWeight);
 					}
 				}
 				if (newBroadcast.getWeights().size() > 0) {
 					// broadcast changes
 					broadcastPacket(newBroadcast);
 				}
 			}
 		} else if (msg instanceof BFDiscoverPacket) {
 			// this is a packet for gathering one-hop information
 			BFDiscoverPacket pmsg = (BFDiscoverPacket)msg;
 
 			if (pmsg.getDestAddress() == -1) {
 				// we're receiving a ping from one hop away, measure the time it took
 				// and see if we can get it back to the sender
 				int elapsed = (int)(System.currentTimeMillis() - pmsg.getTimestamp());
 
 				BFDiscoverPacket broadcastReply = new BFDiscoverPacket(getAddress(),
 						pmsg.getSourceAddress(), 32, 1, elapsed);
 				broadcastReply.setSendGate(pmsg.getSendGate());
 				BFDiscoverPacket routedReply = new BFDiscoverPacket(getAddress(),
 						pmsg.getSourceAddress(), 32, 16, elapsed);
 				routedReply.setSendGate(pmsg.getSendGate());
 
 				broadcastReply.recordTimestamp();
 				routedReply.recordTimestamp();
 				broadcastPacket(broadcastReply);
 				super.processPacket(routedReply);
 			} else if (pmsg.getDestAddress() == getAddress()) {
 				oneHop.put(pmsg.getSourceAddress(),
 						new TimeGatePair(pmsg.getElapsedTime(), pmsg.getSendGate()));
 			} else {
 				// treat this like a normal packet if we aren't involved
 				super.processPacket(msg);
 			}
 		} else {
 			super.processPacket(msg);
 		}
 	}
 	
 	protected class TimeGatePair {
 		private int time;
 		private int gate;
 		
 		public TimeGatePair(int t, int g) {
 			time = t;
 			gate = g;
 		}
 		
 		public int getTime() {
 			return time;
 		}
 		public int getGate() {
 			return gate;
 		}
 		
 		public void setTime(int t) {
 			time = t;
 		}
 		public void setGate(int g) {
 			gate = g;
 		}
 	}
 	
 	private class OneHopPinger extends Thread {
 		/**
 		 * Periodically pings neighbors to keep one-hop data accurate
 		 */
 		@Override
 		public void run() {
 			while (!this.isInterrupted()) {
 				for (int gate = 0; gate < getLinks().size(); gate++) {
 					BFDiscoverPacket ping = new BFDiscoverPacket(getAddress(), -1, 32*8, 1);
 					BFWeightPacket selfweight = new BFWeightPacket(getAddress(), -1, 32*8, 1);
 					selfweight.addWeight(getAddress(), 0);
 					ping.setSendGate(gate);
 					
 					ping.recordTimestamp();
 					sendPacket(ping, gate);
 					sendPacket(selfweight, gate);
 				}
 				
 				try {
 					synchronized (this) {
 						wait(10000 + (int)(20000 * Math.random()));
 					}
 				} catch (InterruptedException e) {
 					logger.log(Level.WARNING, "Pinger for router " + getAddress() + " was "
 							   + "interrupted.", e);
 					break;
 				}
 			}
 		}
 	}
 }
