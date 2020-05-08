 /**
  * 
  */
 package uk.ac.imperial.dws04.Presage2Experiments.IPCon;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.drools.runtime.rule.QueryResults;
 import org.drools.runtime.rule.QueryResultsRow;
 import org.drools.runtime.rule.Variable;
 
 import com.google.inject.Inject;
 
import uk.ac.imperial.dws04.Presage2Experiments.FinishEarlyEvent;
 import uk.ac.imperial.dws04.Presage2Experiments.RoadAgent;
 import uk.ac.imperial.dws04.Presage2Experiments.RoadAgentGoals;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPConAction;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.Chosen;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.HasRole;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConFact;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConRIC;
 import uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest;
 import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
 import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
 import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
 import uk.ac.imperial.presage2.core.environment.SharedStateAccessException;
 import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
 import uk.ac.imperial.presage2.core.event.EventListener;
 import uk.ac.imperial.presage2.core.participant.Participant;
 import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.core.simulator.FinalizeEvent;
 import uk.ac.imperial.presage2.util.environment.EnvironmentMembersService;
 
 /**
  * @author dws04
  *
  */
 public class ParticipantIPConService extends IPConService {
 
 	protected final IPConAgent handle;
 	private final Logger logger = Logger.getLogger(ParticipantIPConService.class);
 	
 	public ParticipantIPConService(Participant p, EnvironmentSharedStateAccess sharedState, EnvironmentServiceProvider serviceProvider) {
 		// construct from the protected one, then hope it injects correctly !
 		super(sharedState, serviceProvider);
 		if (p instanceof HasIPConHandle) {
 			this.handle = ((HasIPConHandle)p).getIPConHandle();
 		}
 		else {
 			this.handle = null;
 			logger.error(p.getID() + " does not have an IPCon handle...");
 		}
     }
 	
 	/**
 	 * Create the {@link ParticipantSharedState} required for this service.
 	 * This is for registration purposes only, so don't make a getter!
 	 * 
 	 * @param pid
 	 *            {@link UUID} of the participant to create sharedstate object
 	 *            for.
 	 * @param s
 	 *            Goals of the agent, for registration purposes
 	 * @return {@link ParticipantSharedState} on the type that this service
 	 *         uses.
 	 */
 	public static ParticipantSharedState createSharedState(UUID pid, RoadAgentGoals s) {
 		return new ParticipantSharedState("ipcon.goals", s, pid);
 	}
 	
 /*	@Override
 	public void registerParticipant(EnvironmentRegistrationRequest req) {
 		// do insertion of IPConAgent fact and such
 		logger.trace("Inserting agent via ParticipantIPConService " + ((RoadAgent)req.getParticipant()).getIPConHandle());
 		session.insert( ((RoadAgent)req.getParticipant()).getIPConHandle() );
 	}*/
 	
 	private StatefulKnowledgeSession getSession() {
 		if (this.session==null) {
 			setSession(session);
 		}
 		return this.session;
 	}
 	
 	/**
 	 * @return the RICs that the agent is a member of
 	 */
 	@Override
 	public Collection<IPConRIC> getCurrentRICs(){
 		return super.getCurrentRICs(this.handle);
 	}
 	
 	/**
 	 * @return the RICs that the agent is a member of
 	 */
 	@Override
 	public Collection<IPConRIC> getCurrentRICs(IPConAgent handle) {
 		if (handle.equals(this.handle)) {
 			return super.getCurrentRICs(handle);
 		}
 		else {
 			throw new SharedStateAccessException("A participant may not view another agent's RICs!");
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConService#getRICsInCluster(java.util.UUID)
 	 */
 	@Override
 	public Collection<IPConRIC> getRICsInCluster(UUID cluster) {
 		if ( cluster!=null /*&& (!super.getAgentRoles(this.handle, null, null, cluster).isEmpty())*/ ) {
 			// Have to hack this rather than calling the super because the super calls getFacts which is unavailable to agents
 			HashSet<IPConRIC> set = new HashSet<IPConRIC>();
 			QueryResults facts = session.getQueryResults("getFactsNamed", new Object[]{Variable.v, Variable.v, cluster, "IPConRIC"});
 			for (QueryResultsRow row : facts) {
 				set.add((IPConRIC)row.get("$fact"));
 			}
 			return set;
 		}
 		else {
 			throw new SharedStateAccessException("A participant may not view info about a RIC that does not exist!");
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConService#getObligations(uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent, java.lang.Integer, java.lang.String, java.util.UUID)
 	 */
 	@Override
 	public Collection<IPConAction> getObligations(IPConAgent agent,
 			Integer revision, String issue, UUID cluster) {
 		if (agent.equals(this.handle)) {
 			return super.getObligations(agent, revision, issue, cluster);
 		}
 		else {
 			throw new SharedStateAccessException("A participant (" + this.handle + ") may not view another agent's (" + agent + ") obligations!");
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConService#getObligations(uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent, java.lang.Integer, java.lang.String, java.util.UUID)
 	 */
 	@Override
 	public Collection<IPConAction> getPermissions(IPConAgent agent,
 			Integer revision, String issue, UUID cluster) {
 		if (agent.equals(this.handle)) {
 			return super.getPermissions(agent, revision, issue, cluster);
 		}
 		else {
 			throw new SharedStateAccessException("A participant may not view another agent's permissions!");
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConService#getObligations(uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent, java.lang.Integer, java.lang.String, java.util.UUID)
 	 */
 	@Override
 	public Collection<IPConAction> getPowers(IPConAgent agent,
 			Integer revision, String issue, UUID cluster) {
 		if (agent.equals(this.handle)) {
 			return super.getPowers(agent, revision, issue, cluster);
 		}
 		else {
 			throw new SharedStateAccessException("A participant may not view another agent's powers!");
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConService#getFactQueryResults(java.lang.String, java.lang.Integer, java.lang.String, java.util.UUID)
 	 */
 	@Override
 	public Collection<IPConFact> getFactQueryResults(String factType,
 			Integer revision, String issue, UUID cluster) {
 		throw new SharedStateAccessException("getFactQueryResults not available to agents!");
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConService#getActionQueryResultsForRIC(java.lang.String, java.lang.String, uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent, java.lang.Integer, java.lang.String, java.util.UUID)
 	 */
 	@Override
 	public Collection<IPConAction> getActionQueryResultsForRIC(
 			String queryName, String actionType, IPConAgent agent,
 			Integer revision, String issue, UUID cluster) {
 		throw new SharedStateAccessException("getActionQueryResultsForRIC not available to agents!");
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConService#getQuorumSize(java.lang.Integer, java.lang.String, java.util.UUID)
 	 */
 	@Override
 	public Integer getQuorumSize(Integer revision, String issue, UUID cluster) {
 		if ( revision!=null && issue!=null && cluster!=null /*&& (!super.getAgentRoles(this.handle, revision, issue, cluster).isEmpty())*/ ) {
 			return super.getQuorumSize(revision, issue, cluster);
 		}
 		else {
 			throw new SharedStateAccessException("A participant may not view info about a RIC that does not exist!");
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConService#getChosen(java.lang.Integer, java.lang.String, java.util.UUID)
 	 */
 	/*@Override
 	public Chosen getChosen(Integer revision, String issue, UUID cluster) {
 		if ( revision!=null && issue!=null && cluster!=null && (!super.getAgentRoles(this.handle, revision, issue, cluster).isEmpty()) ) {
 			return super.getChosen(revision, issue, cluster);
 		}
 		else {
 			throw new SharedStateAccessException("A participant may not view info about a RIC they are not in!");
 		}
 	}*/
 
 	/* (non-Javadoc)
 	 * @see uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConService#getAgentRoles(uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent, java.lang.Integer, java.lang.String, java.util.UUID)
 	 */
 	@Override
 	public Collection<HasRole> getAgentRoles(IPConAgent agent,
 			Integer revision, String issue, UUID cluster) {
 		if ( ( revision!=null && issue!=null && cluster!=null && (!super.getAgentRoles(this.handle, revision, issue, cluster).isEmpty()) ) ||
 				(agent.equals(this.handle)) ) {
 			return super.getAgentRoles(agent, revision, issue, cluster);
 		}
 		else {
 			throw new SharedStateAccessException("A participant may not view another agent's roles unless they are in the same RIC!");
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConService#getRICLeader(java.lang.Integer, java.lang.String, java.util.UUID)
 	 */
 	@Override
 	public ArrayList<IPConAgent> getRICLeader(Integer revision, String issue,
 			UUID cluster) {
 		if ( revision!=null && issue!=null && cluster!=null /*&& (!super.getAgentRoles(this.handle, revision, issue, cluster).isEmpty())*/ ) {
 			return super.getRICLeader(revision, issue, cluster);
 		}
 		else {
 			throw new SharedStateAccessException("A participant may not view info about a RIC that does not exist!");
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConService#onEndOfCycle(uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle)
 	 */
 	@Override
 	public void onEndOfCycle(EndOfTimeCycle event) {
 		// remove annotation so that it doesn't listen (since it doesn't have session access)
 		// do nothing
 	}
	
	@Override
	public void onSimulationEarlyEnd(FinishEarlyEvent event) {
	}
	
	@Override
	public void onSimulationEnd(FinalizeEvent event) {
	}
 
 	/* (non-Javadoc)
 	 * @see uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConService#getAllRICs(uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent)
 	 */
 	@Override
 	public Collection<IPConRIC> getAllRICs(IPConAgent handle) {
 		if (handle.equals(this.handle)) {
 			return super.getAllRICs(handle);
 		}
 		else {
 			throw new SharedStateAccessException("A participant may not view another agent's RICs!");
 		}
 	}
 
 	
 	
 	
 	
 	
 }
