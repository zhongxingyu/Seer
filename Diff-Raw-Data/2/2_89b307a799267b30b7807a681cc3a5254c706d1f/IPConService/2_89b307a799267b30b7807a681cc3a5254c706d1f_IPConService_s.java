 /**
  * 
  */
 package uk.ac.imperial.dws04.Presage2Experiments.IPCon;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map.Entry;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 import org.drools.lang.dsl.DSLMapParser.entry_return;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.drools.runtime.rule.FactHandle;
 import org.drools.runtime.rule.QueryResults;
 import org.drools.runtime.rule.QueryResultsRow;
 import org.drools.runtime.rule.Variable;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 import uk.ac.imperial.dws04.Presage2Experiments.RoadAgent;
 import uk.ac.imperial.dws04.Presage2Experiments.RoadAgentGoals;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPCNV;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPConAction;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPConTime;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Prepare1A;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Request0A;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.Chosen;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.HasRole;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConFact;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConRIC;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.QuorumSize;
 import uk.ac.imperial.dws04.utils.record.Pair;
 import uk.ac.imperial.dws04.utils.record.PairAThenBAscComparator;
 import uk.ac.imperial.presage2.core.IntegerTime;
 import uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest;
 import uk.ac.imperial.presage2.core.environment.EnvironmentService;
 import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
 import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
 import uk.ac.imperial.presage2.core.event.EventBus;
 import uk.ac.imperial.presage2.core.event.EventListener;
 import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
 import uk.ac.imperial.presage2.core.simulator.SimTime;
 import uk.ac.imperial.presage2.core.util.random.Random;
 
 /**
  * @author dws04
  *
  */
 @Singleton
 public class IPConService extends EnvironmentService {
 
 	private final Logger logger = Logger.getLogger(this.getClass());
 	StatefulKnowledgeSession session;
 	private final EnvironmentServiceProvider serviceProvider;
 	private FactHandle timeHandle;
 
 	@Inject
 	public IPConService(EnvironmentSharedStateAccess sharedState, EnvironmentServiceProvider serviceProvider,
 			StatefulKnowledgeSession session) {
 		super(sharedState);
 		this.session = session;
 		this.serviceProvider = serviceProvider;
 		session.setGlobal("logger", this.logger);
 		session.setGlobal("IPCNV_val", IPCNV.val());
 		session.setGlobal("IPCNV_bal", IPCNV.bal());
 		timeHandle = session.insert(new IPConTime(SimTime.get().intValue()));
 		for (Role role : Role.values()) {
 			session.insert(role);
 		}
 	}
 	
 	protected IPConService(EnvironmentSharedStateAccess sharedState, EnvironmentServiceProvider serviceProvider) {
 		super(sharedState);
 		this.serviceProvider = serviceProvider;
 	}
 	
 	/**
 	 * Lazyload the session
 	 */
 	@Inject
 	protected void setSession(StatefulKnowledgeSession session) {
 		this.session = session;
 		if (session.getGlobal("logger")==null) {
 			session.setGlobal("logger", this.logger);
 			session.setGlobal("IPCNV_val", IPCNV.val());
 			session.setGlobal("IPCNV_bal", IPCNV.bal());
 			
 			for (Role role : Role.values()) {
 				session.insert(role);
 			}
 		}
 	}
 	
 	@Inject
 	void setEventBus(EventBus eb) {
 	    eb.subscribe(this);
 	}
 	
 	@EventListener
 	public void onEndOfCycle(EndOfTimeCycle event) {
 		session.update(timeHandle, event.getTime().intValue()+1);
 	}
 	
 	/** 
 	 * Inserts the agent fact for the registered agent, and sets them to be LEAD/ACC/PROP/LEARN for a cluster containing RICS on all their goals
 	 * @see uk.ac.imperial.presage2.core.environment.EnvironmentService#registerParticipant(uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest)
 	 */
 	@Override
 	public void registerParticipant(EnvironmentRegistrationRequest req) {
 		// do insertion of IPConAgent fact and such
 		logger.trace("Inserting agent via global IPConService " + ((RoadAgent)req.getParticipant()).getIPConHandle());
 		IPConAgent handle = ((RoadAgent)req.getParticipant()).getIPConHandle();
 		session.insert( handle );
 		UUID cluster = Random.randomUUID();
 		HashMap<String, Pair<Integer, Integer>> goals = ((RoadAgentGoals)sharedState.get("ipcon.goals", req.getParticipantID())).getMap();
 		for (Entry<String, Pair<Integer, Integer>> goal : goals.entrySet()) {
 			Integer revision = 0;
 			String issue = goal.getKey();
 			session.insert(new IPConRIC(revision, issue, cluster));
 			session.insert(new HasRole(Role.LEADER, handle, revision, issue, cluster));
 			session.insert(new HasRole(Role.ACCEPTOR, handle, revision, issue, cluster));
 			session.insert(new HasRole(Role.LEARNER, handle, revision, issue, cluster));
 			session.insert(new HasRole(Role.PROPOSER, handle, revision, issue, cluster));
 		}
 	}
 	
 	/**
 	 * 
 	 * @param revision
 	 * @param issue
 	 * @param cluster
 	 * @return the quorum size for the RIC specified
 	 */
 	public Integer getQuorumSize(Integer revision, String issue, UUID cluster) {
 		ArrayList<IPConFact> obj = new ArrayList<IPConFact>();
 		obj.addAll(getFactQueryResults("QuorumSize", revision, issue, cluster));
 		if (obj.size()==1) {
 			return ((QuorumSize)obj.get(0)).getQuorumSize();
 		}
 		else {
 			logger.warn("Got multiple values for getQuorumSize(" + revision + "," + issue + "," + cluster  + ") : " + obj);
 			return null;
 		}
 	}
 	
 	/**
 	 * 
 	 * @param revision
 	 * @param issue
 	 * @param cluster
	 * @return the IPConFact 'Chosen' (NOT the value for the specified RIC or null if zero or multiple (shouldn't happen) values
 	 */
 	public Chosen getChosen(Integer revision, String issue, UUID cluster) {
 		ArrayList<IPConFact> obj = new ArrayList<IPConFact>();
 		if ( (revision==null) || (issue==null) || (cluster==null) ) {
 			logger.warn("Can't call getChosen without specifying the RIC");
 			return null;
 		}
 		else {
 			ArrayList<Object> lookup = new ArrayList<Object>();
 			lookup.addAll(Arrays.asList(new Object[]{revision, issue, cluster}));
 			QueryResults facts = session.getQueryResults("getChosen", lookup.toArray());
 			for (QueryResultsRow row : facts) {
 				obj.add((IPConFact)row.get("$chosen"));
 			}
 			if (obj.size()==1) {
 				return ((Chosen)obj.get(0));
 			}
 			else {
 				logger.warn("Got zero or multiple facts for getChosen(" + revision + "," + issue + "," + cluster  + ") : " + obj);
 				return null;
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @param factType typename of facts to match, or null to match all
 	 * @param revision revision to match, or null to match all
 	 * @param issue issue to match, or null to match all
 	 * @param cluster cluster to match, or null to match all
 	 * @return facts matching the query
 	 */
 	public Collection<IPConFact> getFactQueryResults(
 			final String factType,
 			final Integer revision,
 			final String issue,
 			final UUID cluster) {
 		String queryString = "getFacts";
 		HashSet<IPConFact> set = new HashSet<IPConFact>();
 		ArrayList<Object> lookup = new ArrayList<Object>();
 		lookup.addAll(Arrays.asList(new Object[]{revision, issue, cluster}));
 		if (revision==null) {
 			lookup.set(0, Variable.v);
 		}
 		if (issue==null) {
 			lookup.set(1, Variable.v);
 		}
 		if (cluster==null) {
 			lookup.set(2, Variable.v);
 		}
 		if (factType!=null) {
 			queryString = queryString+"Named";
 			lookup.add(factType);
 		}
 		QueryResults facts = session.getQueryResults(queryString, lookup.toArray());
 		for (QueryResultsRow row : facts) {
 			set.add((IPConFact)row.get("$fact"));
 		}
 		return set;
 	}
 
 	/**
 	 * 
 	 * @param revision
 	 * @param issue
 	 * @param cluster
 	 * @return the agents with the role of leader for the specified RIC
 	 */
 	public ArrayList<IPConAgent> getRICLeader(Integer revision, String issue, UUID cluster) {
 		ArrayList<IPConAgent> leaders = new ArrayList<IPConAgent>();
 		if ( (revision==null) || (issue==null) || (cluster==null) ) {
 			logger.warn("Can't call getRICLeader without specifying the RIC");
 			return null;
 		}
 		else {
 			ArrayList<Object> lookup = new ArrayList<Object>();
 			lookup.addAll(Arrays.asList(new Object[]{revision, issue, cluster}));
 			QueryResults facts = session.getQueryResults("getRICLeader", lookup.toArray());
 			for (QueryResultsRow row : facts) {
 				leaders.add((IPConAgent)row.get("$leader"));
 			}
 			if (leaders.size()==0) {
 				logger.trace("Got zero facts for getRICLeader(" + revision + "," + issue + "," + cluster  + ") : " + leaders);
 				return null;
 			}
 			else {
 				logger.trace("Got the following for getRICLeader(" + revision + "," + issue + "," + cluster  + ") : " + leaders);
 				return leaders;
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @param agent agent to match, or null to match all
 	 * @param revision revision to match, or null to match all
 	 * @param issue issue to match, or null to match all
 	 * @param cluster cluster to match, or null to match all
 	 * @return roles the agent holds in the given RIC
 	 */
 	public Collection<HasRole> getAgentRoles(
 			final IPConAgent agent,
 			final Integer revision,
 			final String issue,
 			final UUID cluster) {
 		String queryString = "getAgentRoles";
 		HashSet<HasRole> set = new HashSet<HasRole>();
 		ArrayList<Object> lookup = new ArrayList<Object>();
 		lookup.addAll(Arrays.asList(new Object[]{agent, revision, issue, cluster}));
 		if (agent==null) {
 			lookup.set(0, Variable.v);
 		}
 		if (revision==null) {
 			lookup.set(1, Variable.v);
 		}
 		if (issue==null) {
 			lookup.set(2, Variable.v);
 		}
 		if (cluster==null) {
 			lookup.set(3, Variable.v);
 		}
 		QueryResults facts = session.getQueryResults(queryString, lookup.toArray());
 		for (QueryResultsRow row : facts) {
 			set.add((HasRole)row.get("$role"));
 		}
 		return set;
 	}
 	
 	/**
 	 * 
 	 * @param queryName should be "getPowers" "getPermissions" or "getObligations"
 	 * @param actionType
 	 * @param agent
 	 * @param revision
 	 * @param issue
 	 * @param cluster
 	 * @return matching actions; use null for any argument except queryName to get all matching
 	 */
 	public Collection<IPConAction> getActionQueryResultsForRIC(
 			final String queryName,
 			final String actionType,
 			final IPConAgent agent,
 			final Integer revision,
 			final String issue,
 			final UUID cluster) {
 		// don't bother checking for RIC if you want them all
 		if ((revision==null)&&(issue==null)&&(cluster==null)) {
 			return getActionQueryResults(queryName, actionType, agent);
 		}
 		//else
 		HashSet<IPConAction> set = new HashSet<IPConAction>();
 		for (IPConAction action : getActionQueryResults(queryName, actionType, agent)) {
 			//logger.trace("Checking: " + action);
 			if (matchesRIC(action, revision, issue, cluster)) {
 				set.add((IPConAction)action);
 			}
 		}
 		return set;
 	}
 	
 	/**
 	 * 
 	 * @param queryName should be "getPowers" "getPermissions" or "getObligations"
 	 * @param actionType type of action to filter by, or null to get all
 	 * @param agent agent to get filter by, or null to get all
 	 * @return Collection of IPConActions
 	 */
 	private final Collection<IPConAction> getActionQueryResults(final String queryName, final String actionType, final IPConAgent agent) {
 		HashSet<IPConAction> set = new HashSet<IPConAction>();
 		QueryResults results = null;
 		// Set agent to look up
 		Object lookup = null;
 		if (agent==null) {
 			lookup = Variable.v;
 		}
 		else {
 			lookup = agent;
 		}
 		// Add "Actions" to the string to get the other query
 		if (actionType!=null) {
 			results = session.getQueryResults(queryName+"Actions", new Object[]{ lookup, actionType });
 		}
 		else {
 			results = session.getQueryResults(queryName, new Object[]{ lookup });
 		}
 		for ( QueryResultsRow row : results ) {
 			set.add((IPConAction)row.get("$action"));
 		}
 		return set;
 	}
 	
 	/**
 	 * 
 	 * @param agent can be null to match all agents
 	 * @param revision can be null to match all revisions
 	 * @param issue can be null to match all clusters
 	 * @param cluster can be null to match all clusters
 	 * @return the obligations for the agent&RIC specified. 
 	 */
 	public Collection<IPConAction> getObligations(
 			final IPConAgent agent,
 			final Integer revision,
 			final String issue,
 			final UUID cluster) {
 		HashSet<IPConAction> set = new HashSet<IPConAction>();
 		for (IPConAction action : getActionQueryResults("getObligations", null, agent)) {
 			//logger.trace("Checking: " + action);
 			if (matchesRIC(action, revision, issue, cluster)) {
 				set.add((IPConAction)action);
 			}
 		}
 		return set;
 	}
 	
 	/**
 	 * 
 	 * @param agent can be null to match all agents
 	 * @param revision can be null to match all revisions
 	 * @param issue can be null to match all clusters
 	 * @param cluster can be null to match all clusters
 	 * @return the permissions for the agent&RIC specified. 
 	 */
 	public Collection<IPConAction> getPermissions(
 			final IPConAgent agent,
 			final Integer revision,
 			final String issue,
 			final UUID cluster) {
 		HashSet<IPConAction> set = new HashSet<IPConAction>();
 		for (IPConAction action : getActionQueryResults("getPermissions", null, agent)) {
 			//logger.trace("Checking: " + action);
 			if (matchesRIC(action, revision, issue, cluster)) {
 				set.add((IPConAction)action);
 			}
 		}
 		return set;
 	}
 	
 	/**
 	 * 
 	 * @param agent can be null to match all agents
 	 * @param revision can be null to match all revisions
 	 * @param issue can be null to match all clusters
 	 * @param cluster can be null to match all clusters
 	 * @return the powers for the agent&RIC specified. 
 	 */
 	public Collection<IPConAction> getPowers(
 			final IPConAgent agent,
 			final Integer revision,
 			final String issue,
 			final UUID cluster) {
 		HashSet<IPConAction> set = new HashSet<IPConAction>();
 		for (IPConAction action : getActionQueryResults("getPowers", null, agent)) {
 			//logger.trace("Checking: " + action);
 			if (matchesRIC(action, revision, issue, cluster)) {
 				set.add((IPConAction)action);
 			}
 		}
 		return set;
 	}
 	
 	/**
 	 * Utility function to check if an object either has-equal, or does not have, the given RIC.
 	 * If any arg is null, the check for that arg is ignored.
 	 * @param object
 	 * @param revision
 	 * @param issue
 	 * @param cluster
 	 * @return true if object's RIC match the given arguments. Any null value (on either side of the check) acts as if that value matches.
 	 */
 	private final boolean matchesRIC(Object object, Integer revision, String issue, UUID cluster) {
 		Integer actionRev = null;
 		if (revision!=null) {
 			try {
 				// Check to see if object's class has a getRevision method, and invoke if it does
 				actionRev = (Integer) object.getClass().getMethod("getRevision").invoke(object, (Object[])null);
 			} catch (Exception e) {
 				// do nothing - if it doesn't have such a method then stay null
 				//e.printStackTrace();
 			}
 		}
 		// If rev was null, then aR will be the initialised null, so do nothing and it will pass.
 		
 		String actionIssue = null;
 		if (issue!=null) {
 			try {
 				actionIssue = (String) object.getClass().getMethod("getIssue").invoke(object, (Object[])null);
 			} catch (Exception e) {
 				// do nothing
 			}
 		}
 		
 		UUID actionCluster = null;
 		if (cluster!=null) {
 			try {
 				actionCluster = (UUID) object.getClass().getMethod("getCluster").invoke(object, (Object[])null);
 			} catch (Exception e) {
 				// do nothing
 			}
 		}
 		
 		//logger.trace("Matching " + object + " against " + revision + ", " + issue + ", " + cluster + 
 		//				" and got r:" + actionRev + ", i:" + actionIssue + ", c:" + actionCluster + ".");
 		
 		// Check to see if the values match.
 		return	( ( actionRev==null || actionRev.equals(revision) ) &&
 				( actionIssue==null || actionIssue.equals(issue)) &&
 				( actionCluster==null || actionCluster.equals(cluster)) );
 	}
 
 	/**
 	 * 
 	 * @param handle
 	 * @return all RICs the agent is a member of
 	 */
 	public Collection<IPConRIC> getCurrentRICs(IPConAgent handle) {
 		Collection<IPConRIC> result = new HashSet<IPConRIC>();
 		Collection<HasRole> coll = getAgentRoles(handle, null, null, null);
 		for (HasRole fact : coll) {
 			//if ( (fact instanceof HasRole) && ((HasRole)fact).getAgent().equals(handle) ) {
 				result.add( fact.getRIC() );
 			//}
 		}
 		return result;
 	}
 
 	/**
 	 * @return all RICs known
 	 */
 	public Collection<IPConRIC> getCurrentRICs() {
 		Collection<IPConRIC> result = new HashSet<IPConRIC>();
 		Collection<IPConFact> coll = getFactQueryResults("IPConRIC", null, null, null);
 		for (IPConFact fact : coll) {
 			result.add((IPConRIC)fact);
 		}
 		return result;
 	}
 	
 	/**
 	 * @return all the RICs in the specified cluster
 	 */
 	public Collection<IPConRIC> getRICsInCluster(UUID cluster) {
 		Collection<IPConRIC> result = new HashSet<IPConRIC>();
 		Collection<IPConFact> coll = getFactQueryResults("IPConRIC", null, null, cluster);
 		for (IPConFact fact : coll) {
 			result.add((IPConRIC)fact);
 		}
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param issue may not be null
 	 * @param cluster may not be null
 	 * @return the highest revision/ballot pair that has been made in a pre_vote, open_vote, voted, or reported_vote fact
 	 * @throws IPConException if no suitable facts can be found
 	 */
 	public Pair<Integer, Integer> getHighestRevisionBallotPair(String issue, UUID cluster) throws IPConException {
 		ArrayList<Pair<Integer,Integer>> list = new ArrayList<Pair<Integer,Integer>>();
 		QueryResults pvFacts = session.getQueryResults("getHighestBallotPV", new Object[]{issue, cluster});
 		for (QueryResultsRow row : pvFacts) {
 			list.add(new Pair<Integer,Integer>((Integer)row.get("$pvRev"), (Integer)row.get("$pvBal")));
 		}
 		QueryResults ovFacts = session.getQueryResults("getHighestBallotOV", new Object[]{issue, cluster});
 		for (QueryResultsRow row : ovFacts) {
 			list.add(new Pair<Integer,Integer>((Integer)row.get("$ovRev"), (Integer)row.get("$ovBal")));
 		}
 		QueryResults vFacts = session.getQueryResults("getHighestBallotV", new Object[]{issue, cluster});
 		for (QueryResultsRow row : vFacts) {
 			list.add(new Pair<Integer,Integer>((Integer)row.get("$vRev"), (Integer)row.get("$vBal")));
 		}
 		QueryResults rvFacts = session.getQueryResults("getHighestBallotRV", new Object[]{issue, cluster});
 		for (QueryResultsRow row : rvFacts) {
 			list.add(new Pair<Integer,Integer>((Integer)row.get("$rvRev"), (Integer)row.get("$rvBal")));
 		}
 		Collections.sort(list, new PairAThenBAscComparator<Integer,Integer>());
 		if (!list.isEmpty()) {
 			logger.trace("Found highest rev/bal in issue:" + issue + " / cluster:" + cluster + " to be " + list.get(list.size()-1));
 			return list.get(list.size()-1);
 		}
 		else
 			throw new IPConException("No votes found");
 		
 	}
 	
 }
