 package agent.trust;
 
 import testbed.sim.Era;
 import agent.CCMPAgent;
 import agent.trust.TrustNetwork;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import testbed.sim.Appraisal;
 import testbed.sim.Opinion;
 import trust.model.BayesTrust;
 import trust.model.primitives.Context;
 import trust.model.primitives.Peer;
 
 /**
  * Bayesian Trust agent shim.
  */
 public class BayesTrustNetwork extends TrustNetwork {
 		/** Trust levels */
 		public static final int TRUST_LEVELS = 5;
 		
 		/** Fully trustworthy */
		public static final double TRUST_LEVEL_FULL = 4/TRUST_LEVELS;
 		/** Moderately trustworthy */
		public static final double TRUST_LEVEL_MID = 3/TRUST_LEVELS;
 		/** Moderately untrustworthy */
		public static final double TRUST_LEVEL_MODERATE = 2/TRUST_LEVELS;
 		/** Untrustworthy */
		public static final double TRUST_LEVEL_MID_UNTRUST = 1/TRUST_LEVELS;
 		/** Fully untrustworthy */
		public static final double TRUST_LEVEL_FULL_UNTRUST = 0;
 		
 		/** B-trust framework */
 		protected BayesTrust mTrust;
 		/** Era certainty */
 		protected double eraCertainty = 0.0;
 		/** Era certainties */
 		protected HashMap<String,Double> agentCertainties;
 		
 		/**
 		 * Constructor.
 		 * @param agent Requires the agent to interrogate ART for problem parameters
 		 */
 		public BayesTrustNetwork(CCMPAgent agent)
 		{
 			super(agent);
 			List<Context> contexts = new LinkedList<Context>();
 			this.agentCertainties = new HashMap<String,Double>();
 			
 			for(Era era : mAgent.getEras()) {
 				Context c = new Context(era.getName());
 				contexts.add(c);
 			}
 			
 			mTrust = new BayesTrust(TRUST_LEVELS, contexts);
 		}
 		
 		/**
 		 * Add an agent to be watched by the Trust framework.
 		 * @param newAgent Agent to watch
 		 */
 		public void addAgent(String newAgent)
 		{
 			Peer p = new Peer(newAgent);
 			mTrust.addPeer(p);
 		}
 
 		/** Handler for DidNotAcceptCertainty event.
 		 * @param agent Agent
 		 * @param era Era
 		 * @Param certaintyValue Certainty [0,1]
 		 * @see agent.trust.TrustInterface#agentDidNotAcceptCertainty(java.lang.String, testbed.sim.Era)
 		 */
 		public void agentDidNotAcceptCertainty(String agent, Era era, double certaintyValue)
 		{
 			// Benign encounter
 		}
 
 		/** Handler for DidNotAcceptReputationRequest event.
 		 * @param agent Agent
 		 * @param era Era
 		 * @see agent.trust.TrustInterface#agentDidNotAcceptReputationRequest(java.lang.String, testbed.sim.Era)
 		 */
 		public void agentDidNotAcceptReputationRequest(String agent, Era era)
 		{
 			// Benign encounter
 		}
 
 		/** Handler for DidNotProvideCertainty event.
 		 * @param agent Agent
 		 * @param era Era
 		 * @see agent.trust.TrustInterface#agentDidNotProvideCertainty(java.lang.String, testbed.sim.Era)
 		 */
 		public void agentDidNotProvideCertainty(String agent, Era era)
 		{
 			// Record negative encounter
 			this.storeEncounter(agent, era.getName(), TRUST_LEVEL_MID_UNTRUST);
 		}
 
 		/** Handler for DidNotProvideOpinion event.
 		 * @param agent Agent
 		 * @param era Era
 		 * @see agent.trust.TrustInterface#agentDidNotProvideOpinion(java.lang.String, testbed.sim.Era)
 		 */
 		public void agentDidNotProvideOpinion(String agent, Era era)
 		{
 			// Record negative encounter
 			this.storeEncounter(agent, era.getName(), TRUST_LEVEL_FULL_UNTRUST);
 		}
 
 		/** Handler for DidNotProvideReputation event.
 		 * @param agent Agent
 		 * @param era Era
 		 * @see agent.trust.TrustInterface#agentDidNotProvideReputation(java.lang.String, testbed.sim.Era)
 		 */
 		public void agentDidNotProvideReputation(String agent, Era era)
 		{
 			// Record negative encounter
 			this.storeEncounter(agent, era.getName(), TRUST_LEVEL_MODERATE);
 		}
 
 		/**
 		 * Get the weight, or confidence, in a trust value of an agent
 		 * @param agent Agent to query
 		 * @param agent Context or era to query
 		 * @see agent.trust.TrustInterface#getReputationWeight(java.lang.String, testbed.sim.Era)
 		 */
 		public double getReputationWeight(String agent, Era era)
 		{
 			Context ck = new Context(era.getName());
 			Peer py = new Peer(agent);
 			return mTrust.getCondensedOverallTrust(ck, py);
 		}
 		
 		/**
 		 * Get the trust value of an agent
 		 * @param agent Agent to query
 		 * @param agent Context or era to query
 		 * @see agent.trust.TrustInterface#getTrustValue(java.lang.String, testbed.sim.Era)
 		 */
 		public double getTrustValue(String agent, Era era)
 		{
 			Context ck = new Context(era.getName());
 			Peer py = new Peer(agent);
 			return mTrust.getCondensedOverallTrust(ck, py);
 		}
 
 		/**
 		 * Store a recommendation from agent X agent about agent Y.
 		 * @param fromAgent Recommender
 		 * @param aboutAgent Recommendee
 		 * @param era Context, or era
 		 * @param double Reputation [0,1] 
 		 * @see agent.trust.TrustInterface#receiveAgentReputationUpdate(java.lang.String, java.lang.String, testbed.sim.Era, double)
 		 */
 		public void receiveAgentReputationUpdate(String fromAgent, String aboutAgent, Era era, double reputation)
 		{
 			Context ck = new Context(era.getName());
 			Peer py = new Peer(fromAgent);
 			Peer pr = new Peer(aboutAgent);
 			
 			// ctsBeta should be [0,1]
 			double ctsBeta = reputation;
 			
 			mTrust.storeRecommendation(ck, pr, py, ctsBeta);
 		}
 
 		/**
 		 * Set an agent's era certainty
 		 * @param agent Other agent
 		 * @param era Era
 		 * @param certainty Certainty [0,1]
 		 * @see agent.trust.TrustInterface#setAgentEraCertainty(java.lang.String, testbed.sim.Era, double)
 		 */
 		public void setAgentEraCertainty(String agent, Era era, double certainty)
 		{
 			agentCertainties.put(this.getAgentEraKey(agent,era), new Double(certainty));
 		}
 
 		/**
 		 * Get our era certainty.
 		 * @param era Era
 		 * @see agent.trust.TrustInterface#setOurEraCertainty(testbed.sim.Era)
 		 */
 		protected double getOurEraCertainty(Era era)
 		{
 			return mAgent.getEraCertainty(era);
 		}
 
 		/**
 		 * Handle encounter of final appraisal
 		 * @see agent.trust.TrustInterface#updateAgentTrustValue(java.lang.String, testbed.sim.AppraisalAssignment, int, int, double)
 		 */
 		public void updateAgentTrustFromFinalAppraisal(String agent, Appraisal appraisal, Opinion opinion)
 		{			
 			// What was the difference encountered
 			double difference = Math.abs(appraisal.getTrueValue() - opinion.getAppraisedValue());
 			
 			// What is that difference in percent
 			difference = difference / ((double)appraisal.getTrueValue());
 	        
 			// What is the satisfaction level of this encounter
 			double satisfaction = 0.0; // range [0,1]
 			
 			// No satisfaction if difference > 100%
 			if (difference > 1.0)
 				satisfaction = 0;
 			// Satisfaction is the inverse of % difference if 0% > difference > 100%
 			else if (difference >= 0 && difference <= 1.0)
 				satisfaction = 1 - difference;
 			
 			// Record encounter
 			this.storeEncounter(agent, appraisal.getEra().getName(), satisfaction);
 		}
 		
 		/**
 		 * Indicates that the frame has changed.
 		 */
 		public void frameReset()
 		{
 		}
 		
 		protected void storeEncounter(String agent, String context, double satisfaction)
 		{
 			Context ck = new Context(context);
 			Peer py = new Peer(agent);
 
 			// Record the encounter
 			mTrust.storeEncounter(ck, py, (double)satisfaction);
 		}
 		
 		/**
 		 * Creates a key for the hashmap.
 		 * @param agent Agent
 		 * @param era Era
 		 * @return Key string
 		 */
 		private String getAgentEraKey(String agent, Era era)
 		{
 			return agent + "." + era.getName();
 		}
 
 		public String toString()
 		{
 			return mTrust.toString();
 		}
 }
