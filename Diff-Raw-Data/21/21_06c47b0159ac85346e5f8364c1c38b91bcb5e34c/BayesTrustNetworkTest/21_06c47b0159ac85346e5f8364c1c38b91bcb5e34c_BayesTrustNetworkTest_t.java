 package tests.agent.trust;
 
import java.util.LinkedList;
import java.util.List;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 
 import testbed.sim.*;
 
 import agent.*;
 import agent.trust.BayesTrustNetwork;
 
 public class BayesTrustNetworkTest {
 
 	private BayesTrustNetwork trustNetwork;
 	
 	public static final double certainty = 0.5;
 	public static final double hoursSpent = 200;
 	public static final double reputation = 0.5;
 	public static final int appraised = 100;
 	
 	public static final String agent = "agent";
 	public static final String fromAgent = "fromAgent";
 	public static final String aboutAgent = "aboutAgent";
 	public static final String toAgent = "toAgent";
 	
	public static final String painting = "Magnificat";
 	public static final Era era = new Era("renaissance");
 	AppraisalAssignment appraisalAssignment = new AppraisalAssignment(agent, era, agent);
 	
 	
 	@Before
 	public void setup() {
		CCMPAgent agent = new BayesWekaCCMPAgent();
		agent.setName("agentAlpha");
		
		List<Era> eras = new LinkedList<Era>();
		eras.add(era);
		agent.receiveEras(eras);
		
 		this.trustNetwork = new BayesTrustNetwork(agent);
 	}
 	
 	@Test
 	public void testCrashInit() {
 		this.trustNetwork.init();
 	}
 	
 
 	@Test
 	public void testCrashaddAgent() {
 		this.trustNetwork.addAgent(agent);
 	}
 	
 	@Test
 	public void testCrashAgentDidNotAcceptCertainty() {
 		this.trustNetwork.agentDidNotAcceptCertainty(agent, era, certainty);
 	}
 
 	@Test
 	public void testCrashAgentDidNotAcceptReputationRequest() {
 		this.trustNetwork.agentDidNotAcceptReputationRequest(agent, era);
 	}
 	
 	@Test
 	public void testCrashAgentDidNotProvideCertainty() {
 		this.trustNetwork.agentDidNotProvideCertainty(agent, era);
 	}
 	
 	@Test
 	public void testCrashAgentDidNotProvideOpinion() {
 		this.trustNetwork.agentDidNotProvideOpinion(agent, era);
 	}
 	
 	@Test
 	public void testCrashAgentDidNotProvideReputation() {
 		this.trustNetwork.agentDidNotProvideReputation(agent, era);
 	}
 	
 	@Test
 	public void testCrashDidNotAcceptCertaintyRequest() {
 		this.trustNetwork.didNotAcceptCertaintyRequest(fromAgent, era);
 	}
 	
 	@Test
 	public void testCrashDidNotProvideAcceptReputationRequest() {
 		this.trustNetwork.didNotProvideAcceptReputationRequest(fromAgent, aboutAgent, era);
 	}
 	
 	@Test
 	public void testCrashDidNotProvideOpinionAfterPayment() {
 		this.trustNetwork.didNotProvideOpinionAfterPayment(fromAgent, era);
 	}
 	
 	@Test
 	public void testCrashDidNotProvideReputationAfterPayment() {
 		this.trustNetwork.didNotProvideReputationAfterPayment(fromAgent, aboutAgent, era);
 	}
 	
 	@Test
 	public void testCrashGeneratedOpinion() {
 		this.trustNetwork.generatedOpinion(toAgent, appraisalAssignment, hoursSpent);
 	}
 	
 	@Test
 	public void testCrashGetInferredTrustValue() {
 		this.trustNetwork.getInferredTrustValue(agent, era);
 	}
 	
 	@Test
 	public void testCrashGetReputationWeight() {
 		this.trustNetwork.getReputationWeight(agent, era);
 	}
 	
 	@Test
 	public void testCrashGetTrustValue() {
 		this.trustNetwork.getTrustValue(agent, era);
 	}
 	
 	@Test
 	public void testCrashProvidedAcceptReputationRequest() {
 		this.trustNetwork.providedAcceptReputationRequest(toAgent, aboutAgent, era);
 	}
 	
 	@Test
 	public void testCrashProvidedCertaintyReply() {
 		this.trustNetwork.providedCertaintyReply(toAgent, era, certainty);
 	}
 	
 	@Test
 	public void testCrashProvidedOpinion() {
 		this.trustNetwork.providedOpinion(toAgent, appraisalAssignment, appraised);
 	}
 	
 	@Test
 	public void testCrashProvidedReputationReply() {
 		this.trustNetwork.providedReputationReply(toAgent, aboutAgent, era, reputation);
 	}
 	
 	@Test
 	public void testCrashReceiveAgentReputationUpdate() {
 		this.trustNetwork.receiveAgentReputationUpdate(fromAgent, aboutAgent, era, reputation);
 	}
 	
 	@Test
 	public void testCrashRemoveAgent() {
 		this.trustNetwork.removeAgent(agent);
 	}
 	
 	@Test
 	public void testCrashSetAgentEraCertainty() {
 		this.trustNetwork.setAgentEraCertainty(agent, era, certainty);
 	}
 	
 	@Test
 	public void testCrashSetOurEraCertainty() {
 		this.trustNetwork.setOurEraCertainty(era);
 	}
 	
 	@Test
 	public void testCrashUpdateAgentTrustFromFinalAppraisal() {
		Appraisal a = new Appraisal(fromAgent, painting, era, appraised);
		Opinion o = new Opinion(fromAgent, painting, appraised + 100, agent, "transactionID");
		this.trustNetwork.updateAgentTrustFromFinalAppraisal(agent, a, o);
 	}
 	
 	@Test
 	public void testCrashFrameReset() {
 		this.trustNetwork.frameReset();
 	}
 }
