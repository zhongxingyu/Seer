 package edu.uc.cs.distsys.test;
 
 import static org.junit.Assert.*;
 
 import java.net.UnknownHostException;
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentMap;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.uc.cs.distsys.ilead.ElectionMonitor;
 import edu.uc.cs.distsys.init.GroupJoinException;
 
 public class MaintainConsensusUsingTwoThirdsCorrectNodes extends LeaderTest implements ElectionMonitor {
 
 	final static int numNodes = 12;
 	final static int numExpectedCorrectNodes = 2 * numNodes / 3 + 1;
 	final static int numExpectedByzantineNodes = numNodes - numExpectedCorrectNodes;
 	final static String PASSED_MSG = "Req A2 Passed: iTolerate shall maintain consensus functionality so long as strictly more than 2/3 of the processes in the system are correct";
 	final static String FAILED_MSG = "Req A2 Failed";
 	ConcurrentMap<Integer, ElectionInfo> consensusData;
 	private int byzantineNumProcOperating;
 	
 	@Before
 	public void setup() {
 		byzantineNumProcOperating = 999;
 		
 		try {
			this.consensusData = startNodes(numNodes, this, 2000);
			this.consensusData.get(100).node.setNumOperatingProcs(byzantineNumProcOperating);
			this.consensusData.get(300).node.setNumOperatingProcs(byzantineNumProcOperating);
			this.consensusData.get(500).node.setNumOperatingProcs(byzantineNumProcOperating);
 		} catch (UnknownHostException e) {
 			assertTrue(e.toString(), false);
 		} catch (GroupJoinException e) {
 			assertTrue(e.toString(), false);
 		}
 		
 		// Allow system to stabilize
 		try {
 			Thread.sleep(3000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void test() {
 		int numTotalNodesRunning = consensusData.size();
 		int numCorrectNodes = 0;
 		int numByzantineNodes = 0;
 		int numNodesInGroup = 0;
 		int numOnlineNodesInGroup = 0;
 		int groupID = 0;
 		
 		for (Entry<Integer, ElectionInfo> entry : this.consensusData.entrySet()) {
 			int nodeNumber = entry.getValue().node.getId();
 			int numNodesTotal = entry.getValue().node.getNumGroupNodes();
 			numNodesInGroup = entry.getValue().node.getNumSameGroupNodes();
 			numOnlineNodesInGroup = entry.getValue().node.getNumOnlineSameGroupNodes();
 			if (groupID == 0) {
 				groupID = entry.getValue().node.getMyNode().getGroupId();
 			} else {
 				assertTrue("Invalid test. All nodes must be in the same group.", groupID == entry.getValue().node.getGroupId());
 			}
 			
 			System.out.println("Node " + nodeNumber + " believes that there are " + numOnlineNodesInGroup + "/" + numNodesInGroup + " Online nodes in group #" + groupID + " (aware of " + numNodesTotal + " nodes total) Consensus Possble: " + Boolean.toString(entry.getValue().node.isConsensusPossible()));
 			
 			if (numNodesInGroup == numTotalNodesRunning) {
 				numCorrectNodes++;
 			} else {
 				numByzantineNodes++;
 			}
 		}
 		
 		System.out.println("Number of expected nodes running: " + numNodes);
 		assertTrue(numTotalNodesRunning == numNodes);
 		
 		System.out.println("Number of expected correct nodes: " + numExpectedCorrectNodes);
 		assertTrue(numCorrectNodes >= numExpectedCorrectNodes);
 		
 		System.out.println("Number of expected Byzantine nodes: " + numExpectedByzantineNodes);
 		assertTrue(numByzantineNodes <= numExpectedByzantineNodes);
 		
 		System.out.println("Check that consensus is possible");
 		for (Entry<Integer, ElectionInfo> entry : this.consensusData.entrySet()) {
 			if (entry.getValue().node.getMyNode().getNumProcOperating() == this.consensusData.size()) {
 				assertTrue(FAILED_MSG, entry.getValue().node.isConsensusPossible());
 			} else {
 				// Don't care what this node thinks about consensus, since it is Byzantine and lying to us anyway
 			}
 		}
 		
 		System.out.println("Increase the percentage of Byzantine Nodes");
 		this.consensusData.get(700).node.getMyNode().setNumProcOperating(byzantineNumProcOperating);
 
 		// Allow system to stabilize
 		try {
 			Thread.sleep(3000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 		System.out.println("Check that consensus is no longer possible");
 		for (Entry<Integer, ElectionInfo> entry : this.consensusData.entrySet()) {
 			assertFalse(FAILED_MSG, entry.getValue().node.isConsensusPossible());
 		}
 		
 		
 		System.out.println(PASSED_MSG);
 	}
 	
 	@After
 	public void tearDown() {
 		shutdownNodes(consensusData);
 	}
 
 	@Override
 	public void onElectionStart(int reportingNodeId) {
 		// NOT USED
 	}
 
 	@Override
 	public void onElectionEnd(int reportingNodeId, int winningNodeId) {
 		// NOT USED
 	}
 
 }
