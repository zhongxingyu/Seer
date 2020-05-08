 package agent.decision;
 
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Vector;
 
 import learning.*;
 
 import agent.CCMPAgent;
 
 import testbed.sim.AppraisalAssignment;
 import testbed.sim.Era;
 
 public class WekaDT extends DecisionTree {
 	
 	public DTLearningCollection dtreeCol;
 	private Vector<Vector<String>> treeAtts;
 	private Map<String,String> strategy;
 	
 	public enum DTLearningNames{
 		DT_ADJUSTAPPRAISAL,
 		DT_GENERATEOPINION,
 		DT_GETAPPRAISAL,
 		DT_GETCERTAINTY,
 		DT_GETREPUTATION,
 		DT_PROVIDECERTAINTY,
 		DT_PROVIDEOPINION,
 		DT_PROVIDEREPUTATION,
 		DT_REQUESTCERTAINTY,
 		DT_REQUESTOPINION,
 		DT_REQUESTREPUTATION,
 		DT_RESPONDCERTAINTY,
 		DT_RESPONDREPUTATION,
 		DT_PROVIDEWEIGHT,
 		DT_NUMDT
 	}	
 		
 	public WekaDT(CCMPAgent agent, DTLearningCollection treeCol)
 	{
 		super(agent);
 		this.dtreeCol = treeCol;
 		
 		for(DTLearning tree : treeCol)
 		{
 			Vector<String> atts = new Vector<String>();
 			for(DTAttribute att : tree.arff.attributes)
 			{
 				atts.add(att.name);
 			}
 			treeAtts.add(atts);
 		}
 	}
 	
 	private String BuildTest(DTLearningNames tree, 
 							 String agent, Era era, boolean ourCertainty,
 							 AppraisalAssignment art)
 	{
 		StringBuffer test = new StringBuffer();
 		
 		for(String att : treeAtts.get(tree.ordinal()))
 		{
 			if(att == "stategy")
 			{
 				test.append(strategy+",");
 			}
 			else if(att == "msgrem")
 			{
 				switch (tree)
 				{
 				case DT_REQUESTCERTAINTY:
 					test.append(mNumCertaintyRequestsLeft.toString());
 					break;
 				case DT_REQUESTOPINION:
 					test.append(mNumOpinionRequestsLeft.get(art.getPaintingID()).toString());
 					break;
 				default:
 					test.append("crash");
 					break;
 				}
 				test.append(",");
 			}
 			else if(att == "certainty")
 			{
 				if(ourCertainty)
 				{
 					test.append(Double.toString(mAgent.getEraCertainty(era)));
 				}
 				else
 				{
 					test.append(Double.toString(mCertainties.get(agent).get(era)));
 				}
 				test.append(",");
 			}
 			else if(att == "trust")
 			{
 				test.append(Double.toString(mReputations.get(agent))+",");
 			}
 			else // this should be the Cat
 			{
 				test.append("?");
 			}
 		}
 		
 		return test.toString();
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see agent.learning.LearningInterface#adjustAppraisalValue(java.lang.String, testbed.sim.Era, int)
 	 */
 	public int adjustAppraisalValue(String toAgent, Era era, int appraisal) {
 		String dtTest = BuildTest(DTLearningNames.DT_ADJUSTAPPRAISAL, toAgent, era, true, null);			
 		String result = dtreeCol.get(DTLearningNames.DT_ADJUSTAPPRAISAL.ordinal()).DTClassify(dtTest);
 		
 		if(result=="UNCHANGED")
 		{
 			return appraisal;
 		}
 		else if(result=="INFLATEx2")
 		{
 			return 2*appraisal;
 		}
 		else if(result=="INFLATEx10")
 		{
 			return 10*appraisal;
 		}
 		else
 		{
 			return appraisal;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see agent.learning.LearningInterface#generateOpinion(java.lang.String, testbed.sim.Era)
 	 */
 	public boolean generateOpinion(String requestingAgent, Era era) {
 		String dtTest = BuildTest(DTLearningNames.DT_GENERATEOPINION, requestingAgent, era, true, null);	
 		String result = dtreeCol.get(DTLearningNames.DT_GENERATEOPINION.ordinal()).DTClassify(dtTest);
 
 		if(result == "DO")
 		{
 			return true;
 		}
 		else if(result == "DONT")
 		{
 			return false;
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see agent.learning.LearningInterface#getAppraisalCost(java.lang.String, testbed.sim.Era)
 	 */
 	public double getAppraisalCost(String requestingAgent, Era era)
 	{
 		String dtTest = BuildTest(DTLearningNames.DT_GETAPPRAISAL, requestingAgent, era, false, null);
 		String result = dtreeCol.get(DTLearningNames.DT_GETAPPRAISAL.ordinal()).DTClassify(dtTest);
 		
 		if(result == "MINIMAL")
 		{
 			return 0.01;
 		}
 		else if(result == "MODERATE")
 		{
 			return mAgent.getOpinionCost()*0.8;
 		}
 		else if(result == "BEST")
 		{
 			return mAgent.getOpinionCost();
 		}
 		else
 		{
 			return 0.01;
 		}
     }
 
 	/* (non-Javadoc)
 	 * @see agent.learning.LearningInterface#getCertaintyRequestValue(java.lang.String, testbed.sim.Era)
 	 */
 	public double getCertaintyRequestValue(String agent, Era era)
 	{
 		// SimpleDT responds with the agents true expertise value
         String eraName = era.getName();
         double myExpertise = mAgent.getExpertise(eraName);
         
         String dtTest = BuildTest(DTLearningNames.DT_GETCERTAINTY, agent, era, false, null);
 		String result = dtreeCol.get(DTLearningNames.DT_GETCERTAINTY.ordinal()).DTClassify(dtTest);
 
 		if(result == "TRUTH")
 		{
 			return 1-myExpertise;
 		}
 		else if(result == "LIE")
 		{
 			return myExpertise;
 		}
 		else
 		{
 			return 1-myExpertise;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see agent.learning.LearningInterface#getReputationRequestValue(java.lang.String, java.lang.String, testbed.sim.Era)
 	 */
 	public double getReputationRequestValue(String requestingAgent,
 			String aboutAgent, Era era)
 	{
 		// The simple agent returns are real values
 		double repValue = mReputations.get(aboutAgent).doubleValue();
 		
 		String dtTest = BuildTest(DTLearningNames.DT_GETREPUTATION, requestingAgent, era, false, null);
 		String result = dtreeCol.get(DTLearningNames.DT_GETREPUTATION.ordinal()).DTClassify(dtTest);
 
 		if(result == "TRUTH")
 		{
 			return repValue;
 		}
 		else if(result == "LIE")
 		{
 			return 1-repValue;
 		}
 		else
 		{
 			return repValue;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see agent.learning.LearningInterface#provideCertaintyRequest(java.lang.String, testbed.sim.Era)
 	 */
 	public boolean provideCertaintyReply(String agent, Era era)
 	{
 		String dtTest = BuildTest(DTLearningNames.DT_PROVIDECERTAINTY, agent, era, true, null);
 		String result = dtreeCol.get(DTLearningNames.DT_PROVIDECERTAINTY.ordinal()).DTClassify(dtTest);
 		
 		if(result == "DO")
 		{
 			return true;
 		}
 		else if(result == "DONT")
 		{
 			return false;
 		}
 		else
 		{
 			return true;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see agent.learning.LearningInterface#provideOpinion(java.lang.String, testbed.sim.Era)
 	 */
 	public boolean provideOpinion(String requestionAgent, Era era)
 	{
 		String dtTest = BuildTest(DTLearningNames.DT_PROVIDEOPINION, requestionAgent, era, true, null);
 		String result = dtreeCol.get(DTLearningNames.DT_PROVIDEOPINION.ordinal()).DTClassify(dtTest);
 		
 		if(result == "DO")
 		{
 			return true;
 		}
 		else if(result == "DONT")
 		{
 			return false;
 		}
 		else
 		{
 			return true;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see agent.learning.LearningInterface#provideReputationReply(java.lang.String, java.lang.String, testbed.sim.Era)
 	 */
 	public boolean provideReputationReply(String requestingAgent,
 			String aboutAgent, Era era)
 	{
 		String dtTest = BuildTest(DTLearningNames.DT_PROVIDEREPUTATION, requestingAgent, era, true, null);
 		String result = dtreeCol.get(DTLearningNames.DT_PROVIDEREPUTATION.ordinal()).DTClassify(dtTest);
 		
 		if(result == "DO")
 		{
 			return true;
 		}
 		else if(result == "DONT")
 		{
 			return false;
 		}
 		else
 		{
 			return true;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see agent.learning.LearningInterface#requestAgentCertainty(java.lang.String, testbed.sim.Era)
 	 */
 	public boolean requestAgentCertainty(String toAgent, Era era)
 	{
 		String dtTest = BuildTest(DTLearningNames.DT_REQUESTCERTAINTY, toAgent, era, false, null);
 		String result = dtreeCol.get(DTLearningNames.DT_REQUESTCERTAINTY.ordinal()).DTClassify(dtTest);
 		
 		if(result == "DO")
 		{
 			return true;
 		}
 		else if(result == "DONT")
 		{
 			return false;
 		}
 		else
 		{
 			return true;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see agent.learning.LearningInterface#requestAgentOpinion(java.lang.String, testbed.sim.AppraisalAssignment)
 	 */
 	public boolean requestAgentOpinion(String toAgent, AppraisalAssignment art)
 	{
 		String dtTest = BuildTest(DTLearningNames.DT_REQUESTOPINION, toAgent, null, false, art);
 		String result = dtreeCol.get(DTLearningNames.DT_REQUESTOPINION.ordinal()).DTClassify(dtTest);
 		
 		if(result == "DO")
 		{
 			return true;
 		}
 		else if(result == "DONT")
 		{
 			return false;
 		}
 		else
 		{
 			return true;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see agent.learning.LearningInterface#requestAgentReputationUpdate(java.lang.String, java.lang.String, testbed.sim.Era, int)
 	 */
 	public boolean requestAgentReputationUpdate(String toAgent,
 			String aboutAgent, Era era, int currentTimestep)
 	{
 		String dtTest = BuildTest(DTLearningNames.DT_REQUESTREPUTATION, toAgent, era, false, null);
 		String result = dtreeCol.get(DTLearningNames.DT_REQUESTREPUTATION.ordinal()).DTClassify(dtTest);
 		
 		if(result == "DO")
 		{
 			return true;
 		}
 		else if(result == "DONT")
 		{
 			return false;
 		}
 		else
 		{
 			return true;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see agent.learning.LearningInterface#respondToCertaintyRequest(java.lang.String, testbed.sim.Era)
 	 */
 	public boolean respondToCertaintyRequest(String agent, Era era)
 	{
 		String dtTest = BuildTest(DTLearningNames.DT_RESPONDCERTAINTY, agent, era, true, null);
 		String result = dtreeCol.get(DTLearningNames.DT_RESPONDCERTAINTY.ordinal()).DTClassify(dtTest);
 		
 		if(result == "DO")
 		{
 			return true;
 		}
 		else if(result == "DONT")
 		{
 			return false;
 		}
 		else
 		{
 			return true;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see agent.learning.LearningInterface#respondToReputationRequest(java.lang.String, java.lang.String, testbed.sim.Era)
 	 */
 	public boolean respondToReputationRequest(String requestingAgent,
 			String aboutAgent, Era era)
 	{
 		String dtTest = BuildTest(DTLearningNames.DT_RESPONDREPUTATION, requestingAgent, era, false, null);
 		String result = dtreeCol.get(DTLearningNames.DT_RESPONDREPUTATION.ordinal()).DTClassify(dtTest);
 		
 		if(result == "DO")
 		{
 			return true;
 		}
 		else if(result == "DONT")
 		{
 			return false;
 		}
 		else
 		{
 			return true;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see agent.learning.LearningInterface#sentCertaintyRequest(java.lang.String, testbed.sim.Era)
 	 */
 	public void sentCertaintyRequest(String toAgent, Era era)
 	{
 		mNumCertaintyRequestsLeft--;
 	}
 
 	/* (non-Javadoc)
 	 * @see agent.learning.LearningInterface#sentOpinionRequest(java.lang.String, testbed.sim.AppraisalAssignment)
 	 */
 	public void sentOpinionRequest(String toAgent, AppraisalAssignment art)
 	{
 		Integer currentLeft = mNumOpinionRequestsLeft.get(art.getPaintingID());
 		currentLeft--;		
 		mNumOpinionRequestsLeft.put( art.getPaintingID(), currentLeft);
 	}
 
 	/* (non-Javadoc)
 	 * @see agent.learning.LearningInterface#sentReputationRequest(java.lang.String, java.lang.String)
 	 */
 	public void sentReputationRequest(String toAgent, String aboutAgent)
 	{
 		// No need to implement
 	}
 
 	/* (non-Javadoc)
 	 * @see agent.decision.DecisionTree#setAgentEraCertainty(java.lang.String, testbed.sim.Era, double)
 	 */
 	public void setAgentEraCertainty(String agent, Era era, double certainty)
 	{
         Map<Era,Double> agCert = mCertainties.get(agent);
         if (agCert == null) {
             agCert = new HashMap<Era,Double>();
             mCertainties.put(agent, agCert);
         }
         agCert.put(era, certainty);
 	}
 
 	/* (non-Javadoc)
 	 * @see agent.decision.DecisionTree#setAgentTrust(java.lang.String, testbed.sim.Era, double)
 	 */
 	public void setAgentTrust(String agent, Era era, double trust)
 	{
 		mReputations.put(agent, trust);
 	}
 	
 	/* (non-Javadoc)
 	 * @see agent.decision.DecisionTree#initTrustNetwork()
 	 */
 	public void init()
 	{
 		mReputations = new Hashtable<String,Double>();
 		mCertainties = new Hashtable<String,Map<Era,Double>>();
 		mNumCertaintyRequestsLeft = 0;
 		mNumOpinionRequestsLeft = new Hashtable<String,Integer>();
 		
 		mNumOpinionRequestsLeft.clear();
 	}
 	
 	/* (non-Javadoc)
 	 * @see agent.decision.DecisionTree#frameReset()
 	 */
 	public void frameReset()
 	{
 		mNumCertaintyRequestsLeft= mAgent.getMaxCertaintyRequests();;
 		mNumOpinionRequestsLeft.clear();
 		for( AppraisalAssignment art: mAgent.getAppraisalAssignments())
 		{
 			mNumOpinionRequestsLeft.put(art.getPaintingID(), mAgent.getMaxOpinionRequests());
 		}
 	}	
 
 	/* (non-Javadoc)
 	 * @see agent.decision.DecisionTree#addAgent()
 	 */	
 	public void addAgent( String newAgent )
 	{
 		mReputations.put(newAgent, new Double(1.0));
 		strategy.put(newAgent, "NICE");
 	}
 	
 	/* (non-Javadoc)
 	 * @see agent.decision.DecisionTree#removeAgent()
 	 */	
 	public void removeAgent( String agent )
 	{
 		mReputations.remove(agent);
 		strategy.remove(agent);
 	}	
 	
 	public boolean provideWeight( String aboutAgent, Era era )
 	{
 		String dtTest = BuildTest(DTLearningNames.DT_PROVIDEWEIGHT, aboutAgent, era, true, null);
 		String result = dtreeCol.get(DTLearningNames.DT_PROVIDEWEIGHT.ordinal()).DTClassify(dtTest);
 		
 		if(result == "DO")
 		{
 			return true;
 		}
 		else if(result == "DONT")
 		{
 			return false;
 		}
 		else
 		{
 			return true;
 		}		
 	}
 	
 	public void agentDidNotAcceptReputationRequest( String agent, Era era ) {}
 	public void agentDidNotProvideReputation( String agent, Era era ) {}
 	public void agentDidNotProvideCertainty( String agent, Era era) {}
 	public void agentDidNotProvideOpinion( String agent, Era era) {}
 	public void agentDidNotAcceptCertainty( String agent, Era era, double certaintyValue ) {}		
 }
