 /**
  * See http://www.presage2.info/ for more details on Presage2
  */
 package uk.ac.imperial.dws04.Presage2Experiments;
 
 import java.io.IOException;
 import java.io.InvalidClassException;
 import java.io.Serializable;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.log4j.Level;
 
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.HasIPConHandle;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConBallotService;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConException;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.ParticipantIPConService;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.Role;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.Messages.ClusterPing;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.Messages.IPConActionMsg;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.AddRole;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.ArrogateLeadership;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPCNV;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPConAction;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.JoinAsLearner;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.LeaveCluster;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Prepare1A;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Request0A;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.ResignLeadership;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Response1B;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Submit2A;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.SyncAck;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.SyncReq;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Vote2B;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.Chosen;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.HasRole;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConRIC;
 import uk.ac.imperial.dws04.Presage2Experiments.MoveComparators.ConstantWeightedMoveComparator;
 import uk.ac.imperial.dws04.Presage2Experiments.MoveComparators.ExitMoveComparator;
 import uk.ac.imperial.dws04.Presage2Experiments.MoveComparators.InstitutionalSafeMoveComparator;
 import uk.ac.imperial.dws04.Presage2Experiments.MoveComparators.SafeInstitutionalMoveComparator;
 import uk.ac.imperial.dws04.Presage2Experiments.MoveComparators.SpeedWeightedMoveComparator;
 import uk.ac.imperial.dws04.utils.MathsUtils.MathsUtils;
 import uk.ac.imperial.dws04.utils.convert.StringSerializer;
 import uk.ac.imperial.dws04.utils.convert.ToDouble;
 import uk.ac.imperial.dws04.utils.record.Pair;
 import uk.ac.imperial.dws04.utils.record.PairBDescComparator;
 import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
 import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
 import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
 import uk.ac.imperial.presage2.core.messaging.Input;
 import uk.ac.imperial.presage2.core.messaging.Performative;
 import uk.ac.imperial.presage2.core.network.Message;
 import uk.ac.imperial.presage2.core.simulator.SimTime;
 import uk.ac.imperial.presage2.core.util.random.Random;
 import uk.ac.imperial.presage2.util.fsm.Action;
 import uk.ac.imperial.presage2.util.fsm.EventTypeCondition;
 import uk.ac.imperial.presage2.util.fsm.FSM;
 import uk.ac.imperial.presage2.util.fsm.FSMDescription;
 import uk.ac.imperial.presage2.util.fsm.FSMException;
 import uk.ac.imperial.presage2.util.fsm.StateType;
 import uk.ac.imperial.presage2.util.location.CannotSeeAgent;
 import uk.ac.imperial.presage2.util.location.CellMove;
 import uk.ac.imperial.presage2.util.participant.AbstractParticipant;
 
 /**
  * @author dws04
  * 
  */
 public class RoadAgent extends AbstractParticipant implements HasIPConHandle {
 
 	public enum OwnChoiceMethod {
 		SAFE_FAST, PLANNED, SAFE_GOALS, SAFE_CONSTANT, GOALS, SAFE_INST, INST_SAFE, MOVE_TO_EXIT;
 		public boolean isInstChoice() {
 			return (this.equals(INST_SAFE) || this.equals(SAFE_INST));
 		}
 		public static OwnChoiceMethod fromString(String string) {
 			for (OwnChoiceMethod value : OwnChoiceMethod.values()) {
 				if (string.equalsIgnoreCase(value.toString())) {
 					return value;
 				}
 			}
 			return null;
 		}
 	};
 	public enum NeighbourChoiceMethod {WORSTCASE, GOALS, INSTITUTIONAL};
 
 	
 	protected Driver driver;
 	protected RoadLocation myLoc;
 	protected int mySpeed;
 	protected final RoadAgentGoals goals;
 	private Integer junctionsLeft;
 	
 	
 	/**
 	 * FSM Stuff
 	 */
 	/**
 	 * Event to trigger the change in an FSM indicating the agent should move to the exit
 	 * @author dws04
 	 *
 	 */
 	private class MoveToExitEvent {
 		MoveToExitEvent(){
 		}
 	}
 	private FSMDescription fsmDesc = FSM.description();
 	private FSM fsm;
 
 	
 	// service variables
 	ParticipantRoadLocationService locationService;
 	ParticipantSpeedService speedService;
 	/*RoadEnvironmentService environmentService;*/
 	
 	ParticipantIPConService ipconService;
 	protected final IPConAgent ipconHandle;
 	
 	IPConBallotService ballotService;
 	/*
 	 * Should be a long from 0-10 representing how likely an agent is to not arrogate this cycle
 	 */
 	private final int startImpatience;
 	private HashMap<String,Integer> impatience;
 	
 	/*
 	 * Collection for RICs - should be populated every cycle with agents that ping
 	 */
 	private HashMap<IPConRIC,ClusterPing> nearbyRICs;
 	private HashMap<UUID,HashMap<String,ClusterPing>> agentRICs;
 	private LinkedList<IPConRIC> ricsToJoin;
 	private LinkedList<IPConRIC> ricsToArrogate;
 	private LinkedList<IPConRIC> ricsToResign;
 	private LinkedList<IPConRIC> ricsToLeave;
 	private LinkedList<IPConAction> prospectiveActions;
 	private LinkedList<IPConAction> ipconActions;
 	@SuppressWarnings("rawtypes")
 	private LinkedList<Message> messageQueue;
 	private LinkedList<IPConActionMsg> ownMsgs;
 	/** Issue to Chosen fact **/
 	private HashMap<String,Chosen> institutionalFacts;
 	private OwnChoiceMethod ownChoiceMethod;
 	private NeighbourChoiceMethod neighbourChoiceMethod;
 	 
 	@SuppressWarnings("rawtypes")
 	public RoadAgent(UUID id, String name, RoadLocation myLoc, int mySpeed, RoadAgentGoals goals, OwnChoiceMethod ownChoiceMethod, NeighbourChoiceMethod neighbourChoiceMethod) {
 		super(id, name);
 		this.myLoc = myLoc;
 		this.mySpeed = mySpeed;
 		this.goals = goals;
 		if (this.goals.getDest()!=null) {
 			// you want to pass one fewer than your destination
 			this.junctionsLeft = goals.getDest()-1;
 		}
 		else {
 			this.junctionsLeft = null;
 		}
 		this.ipconHandle = new IPConAgent(this.getID(), this.getName());
 		this.startImpatience = (new Long(Math.round(Random.randomDouble()*10))).intValue();
 		this.impatience = new HashMap<String,Integer>();
 		this.nearbyRICs = new HashMap<IPConRIC,ClusterPing>();
 		this.agentRICs = new HashMap<UUID,HashMap<String,ClusterPing>>();
 		ricsToJoin = new LinkedList<IPConRIC>();
 		ricsToArrogate = new LinkedList<IPConRIC>();
 		ricsToResign = new LinkedList<IPConRIC>();
 		ricsToLeave = new LinkedList<IPConRIC>();
 		prospectiveActions = new LinkedList<IPConAction>();
 		ipconActions = new LinkedList<IPConAction>();
 		messageQueue = new LinkedList<Message>();
 		ownMsgs = new LinkedList<IPConActionMsg> ();
 		institutionalFacts = new HashMap<String, Chosen>();
 		if (ownChoiceMethod.equals(OwnChoiceMethod.MOVE_TO_EXIT)) {
 			logger.error(OwnChoiceMethod.MOVE_TO_EXIT + " is not a valid move choice method !");
 			this.ownChoiceMethod = OwnChoiceMethod.SAFE_CONSTANT;
 		}
 		else {
 			this.ownChoiceMethod = ownChoiceMethod;
 		}
 		this.neighbourChoiceMethod = neighbourChoiceMethod;
 	}
 	
 	public RoadAgent(UUID uuid, String name, RoadLocation startLoc,
 			int startSpeed, RoadAgentGoals goals) {
 		this(uuid, name, startLoc, startSpeed, goals, OwnChoiceMethod.SAFE_CONSTANT, NeighbourChoiceMethod.WORSTCASE);
 	}
 
 	/**
 	 * @return the ipconHandle
 	 */
 	public IPConAgent getIPConHandle() {
 		return ipconHandle;
 	}
 
 	@Override
 	protected Set<ParticipantSharedState> getSharedState() {
 		Set<ParticipantSharedState> ss = super.getSharedState();
 		ss.add(ParticipantRoadLocationService.createSharedState(getID(), myLoc));
 		ss.add(ParticipantSpeedService.createSharedState(getID(), mySpeed));
 		// add this here for registration purposes, but don't put a getter anywhere
 		ss.add(ParticipantIPConService.createSharedState(getID(), goals));
 		return ss;
 	}
 	
 	@Override
 	public void initialise() {
 		logger.debug("Initialising RoadAgent " + this.getName() + " / " + this.getID());
 		super.initialise();
 		// get the ParticipantRoadLocationService.
 		try {
 			this.locationService = getEnvironmentService(ParticipantRoadLocationService.class);
 		} catch (UnavailableServiceException e) {
 			logger.warn(e);
 		}
 		// get the ParticipantRoadSpeedService.
 		try {
 			this.speedService = getEnvironmentService(ParticipantSpeedService.class);
 		} catch (UnavailableServiceException e) {
 			logger.warn(e);
 		}
 		try {
 			this.driver = new Driver(getID(), this);
 		} catch (UnavailableServiceException e) {
 			e.printStackTrace();
 		}
 		// get the IPConService.
 		try {
 			this.ipconService = getEnvironmentService(ParticipantIPConService.class);
 		} catch (UnavailableServiceException e) {
 			logger.warn(e);
 			e.printStackTrace();
 		}
 		// get the BallotService.
 		try {
 			this.ballotService = getEnvironmentService(IPConBallotService.class);
 		} catch (UnavailableServiceException e) {
 			logger.warn(e);
 			e.printStackTrace();
 		}
 		/*// get the RoadEnvironmentService.
 		try {
 			this.environmentService = getEnvironmentService(RoadEnvironmentService.class);
 		} catch (UnavailableServiceException e) {
 			logger.warn(e);
 		}*/
 		// Init the FSM
 		try {
 			fsmDesc.addState("IDLE", StateType.START)
 				.addState("MOVE_TO_EXIT", StateType.ACTIVE)
 				.addTransition("IDLE_TO_MOVE", new EventTypeCondition(MoveToExitEvent.class), "IDLE", "MOVE_TO_EXIT", Action.NOOP);
 			fsm = new FSM(fsmDesc, null);
 			if ( (junctionsLeft!=null) && (junctionsLeft<=1) ){
 				fsm.applyEvent(new MoveToExitEvent());
 			}
 		} catch (FSMException e) {
 			logger.warn("You can't initialise the FSM like this:" + e);
 		}
 	}
 	
 	@Override
 	public void execute() {
 		// clear temp storage
 		this.nearbyRICs.clear();
 		this.agentRICs.clear();
 		
 		// pull in Messages from the network
 		enqueueInput(this.network.getMessages());
 		
 		// insert msgs from self (since broadcasts don't self-deliver)
 		this.inputQueue.addAll(ownMsgs);
 		ownMsgs.clear();
 		
 		// check inputs
 		logger.trace(getID() + " has msgs:" + this.inputQueue);
 
 		// process inputs
 		while (this.inputQueue.size() > 0) {
 			this.processInput(this.inputQueue.poll());
 		}
 		
 		
 		/*
 		 * Get physical state
 		 */
 		myLoc = (RoadLocation) locationService.getAgentLocation(getID());
 		mySpeed = speedService.getAgentSpeed(getID());
 		Integer junctionDist = this.locationService.getDistanceToNextJunction();
 		final Collection<IPConRIC> currentRICs = ipconService.getCurrentRICs();
 		
 		/*
 		 * Could retrieve (in case we want to change them...) macrogoals
 		 *  (probably not required)
 		 * 
 		 */
 		
 		/*
 		 * Throw a warning if in more than one cluster, because that doesn't seem to be too useful
 		 */
 		Boolean inMultipleClusters = checkClusterMembership();
 		if (inMultipleClusters) {
 			logger.warn(getID() + " is in multiple clusters ! : " + ipconService.getCurrentRICs(getIPConHandle()));
 		}
 		
 		// check for vehicles infront/behind you with matching issues and compatible values
 		// look in nearbyRICs - possible to get location of those vehicles ? clusterpings are range-less...
 		for (Entry<IPConRIC,ClusterPing> entry : this.nearbyRICs.entrySet()) {
 			logger.trace(getID() + " is checking " + entry);
 			
 			// TODO should remove duplicates around here somewhere
 			
 			
 			
 			
 			// if youre not in the cluster and it has a chosen value
 			if (!currentRICs.contains(entry.getKey()) && entry.getValue().getChosen()!=null ) {
 				// try to get their location - if you can, then they're close enough (throwing away the result)
 				try {
 					locationService.getAgentLocation(entry.getValue().getFrom().getId());
 				}
 				catch (CannotSeeAgent e) {
 					// do nothing
 				}
 				int join = 0;
 				int stay = 0;
 				Collection<IPConRIC> clusterRICs = ipconService.getRICsInCluster(entry.getKey().getCluster());
 				// for (rics in cluster)
 				for (IPConRIC ricInCluster : clusterRICs) {
 				// checkAcceptability of chosen value in ric
 					logger.debug(getID() + " is considering " + ricInCluster);
 					if (isPreferableToCurrent(ricInCluster)) {
 					//if (checkAcceptability(ricInCluster, ipconService.getChosen(ricInCluster.getRevision(), ricInCluster.getIssue(), ricInCluster.getCluster()))) {
 						// if true, increment "join"
 						join++;
 						logger.debug(getID() + " incremented in favour of " + ricInCluster + " - " + join + ":" + stay);
 					}
 					else {
 						// if false, increment stay
 						stay++;
 						logger.debug(getID() + " incremented against " + ricInCluster + " - " + join + ":" + stay);
 					}
 				// end if
 				} // end for
 				if (join>stay) {
 					logger.debug(getID() + " found that it should join cluster " + entry.getKey().getCluster());
 					// TODO fix this to take rics out of consideration for rest of cycle because youre about to leave it
 					// and also to join all of the RICs in the cluster, not just the one you got the msg from...
 					for (IPConRIC ricInCluster : clusterRICs) {
 						// ricsToLeave should remove yourself from the cluster the RIC is in, not just the RIC
 						ricsToLeave.addAll(getRICsForIssue(ricInCluster.getIssue()));
 						ricsToJoin.add(ricInCluster);
 					}
 				}
 			}
 		}
 					
 		
 		
 		for (IPConRIC ric : currentRICs) {
 			Chosen value = getChosenFact(ric.getRevision(), ric.getIssue(), ric.getCluster());
 			if (value!=null) {
 				// "null" should never be chosen as a value, so we can do this ?
 				institutionalFacts.put(ric.getIssue(), value);
 				logger.trace(getID() + " thinks " + value.getValue() + " has been chosen in " + ric);
 			}
 			else {
 				// "null" should never be chosen as a value, so we can do this ?
 				logger.trace(getID() + " thinks there is no currently chosen value in " + ric + ", but has " + institutionalFacts.get(ric.getIssue()) + " in memory from a previous cycle.");
 			}
 			// Check for leaders
 			ArrayList<IPConAgent> leaders = ipconService.getRICLeader(ric.getRevision(), ric.getIssue(), ric.getCluster());
 			// no leaders, maybe arrogate?
 			if (leaders.isEmpty()) {
 				logger.trace(getID() + " is in RIC " + ric + " which has no leader(s), so is becoming impatient to arrogate (" + getImpatience(ric.getIssue()) + " cycles left).");
 				if (!ricsToArrogate.contains(ric) && isImpatient(ric.getIssue())) {
 					logger.debug(getID() + " will arrogate in " + ric);
 					ricsToArrogate.add(ric);
 				}
 				// update impatience whether or not you were impatient (will reset if you were impatient)
 				updateImpatience(ric.getIssue());
 			}
 			else {
 				// multiple leaders and you're one of them
 				if ( leaders.size()>1 && leaders.contains(getIPConHandle()) ) {
 					leaders.remove(leaders.indexOf(getIPConHandle()));
 					for (IPConAgent leader : leaders) {
 						if (leaderIsMoreSenior(leader)) {
 							logger.debug(getID() + " is less senior than another leader (" + leader + ") in " + ric + " so will resign");
 							ricsToResign.add(ric);
 							break; // only need to resign once
 						}
 					}
 				}
 				// else (1 or more leaders and you're not one, or you're the only leader) do nothing
 			}
 		}
 		// For all goals
 		// check if represented by an issue in any RIC youre in
 		// if not,
 		// Check other RICs in cluster
 		// - Join if they do
 		// - arrogate otherwise
 		// - Check if a nearby cluster has issue
 		// - - Join if yes
 		// - - Arrogate if no (always want to be in a RIC)
 		for (String goalIssue : getGoalMap().keySet()) {
 			logger.trace(getID() + " is thinking about their goal \'" + goalIssue + "\'");
 			Boolean found = false;
 			Boolean foundInCluster = false;
 			Object goalValue = getGoalMap().get(goalIssue).getA();
 			IPConRIC issueRIC = null;
 			for (IPConRIC ric : currentRICs) {
 				if (!found && ric.getIssue().equalsIgnoreCase(goalIssue)) {
 					found = true;
 					issueRIC = ric;
 					logger.trace(getID() + " is in a RIC (" + ric + ") for " + goalIssue + ").");
 					break;
 				}
 			}
 			if (!found || issueRIC==null) {
 				logger.trace(getID() + " could not find a RIC for " + goalIssue + " so will check RICs in current cluster(s).");
 				foundInCluster = findRICsToJoin_FromExecute(currentRICs, goalIssue, foundInCluster);
 				if (!foundInCluster) {
 					logger.trace(getID() + " could not find a RIC to join for " + goalIssue + /*" and is impatient " +*/ " so will arrogate.");
 					findRICToArrogate_FromExecute(goalIssue);
 				}
 				else {
 					// found a RIC in a cluster youre in, and joined it already
 				}
 				logger.trace(getID() + " found a RIC to join for " + goalIssue);				
 			}
 			// else do stuff for RICs youre in
 			// check for chosen values - if there is nothing chosen then do stuff with impatience and think about proposing/leaving/etc
 			// if the nearby clusters have the same (or an as-acceptable) value for your issues, then join(merge) ?
 			// check to see if the RICs you're in with chosen values have values that are acceptable to you
 			// if not, propose/leave ?
 			else {
 				logger.trace(getID() + " is in a RIC for " + goalIssue + " - " + issueRIC + ", so is checking the chosen value");
 				// if youre in a RIC for the issue, check a value is chosen
 				Chosen chosen = this.getChosenFact(issueRIC.getRevision(), issueRIC.getIssue(), issueRIC.getCluster());
 				// if not, then propose if it makes sense (ie, you can and there's not another process in play)
 				if (chosen==null) {
 					// check for proposal sensibleness
 					Boolean proposalMakesSense = checkProposalSensibleness_FromExecute(issueRIC);
 					// propose if yes
 					if (proposalMakesSense) {
 						logger.debug(getID() + " could not find a chosen value in " + issueRIC + " so is proposing their goalValue");
 						ipconActions.add(new Request0A(getIPConHandle(), issueRIC.getRevision(), goalValue, issueRIC.getIssue(), issueRIC.getCluster()));
 					}
 					// else wait for something else
 					else { 
 						logger.trace(getID() + " could not find a chosen value in " + issueRIC + " but cannot do anything, so is waiting");
 					}
 				}
 				// if there is a chosen value, check it is suitable - if not, then propose if it makes sense, or leave
 				else {
 					// check for suitability of chosen value
 					Boolean suitable = false;
 					try {
 						suitable = isWithinTolerance(goalIssue, chosen.getValue());
 					} catch (InvalidClassException e) {
 						logger.debug(e);
 					}
 					// if not suitable, 
 					if (!suitable) {
 						// check for proposal sensibleness
 						Boolean proposalMakesSense = checkProposalSensibleness_FromExecute(issueRIC);
 						// propose if yes
 						if (proposalMakesSense) {
 							logger.debug(getID() + " did not like the chosen value in " + issueRIC + " so is proposing their goalValue");
 							ipconActions.add(new Request0A(getIPConHandle(), issueRIC.getRevision(), goalValue, issueRIC.getIssue(), issueRIC.getCluster()));
 						}
 						// else if it doesnt make sense to propose then just wait
 						else {
 							if (Random.randomInt()%9==0) {
 								logger.debug(getID() + " did not like the chosen value in " + issueRIC + " and cannot propose so is being sulky and leaving");
 								ricsToLeave.add(issueRIC);
 							}
 							else {
 								logger.debug(getID() + " did not like the chosen value in " + issueRIC + " and cannot propose but is putting up with it");
 							}
 						}
 					}
 					// if suitable then yay
 					else {
 						logger.trace(getID() + " is happy with the chosen value (" + chosen.getValue() + ") in " + issueRIC);
 					}
 				}
 			}
 			
 		}
 		
 		
 		
 		/*
 		 * Arrogate in all RICS you need to
 		 */
 		while (!ricsToArrogate.isEmpty()) {
 			IPConRIC ric = ricsToArrogate.removeFirst();
 			ArrogateLeadership act = new ArrogateLeadership(getIPConHandle(), ric.getRevision(), ric.getIssue(), ric.getCluster());
 			ipconActions.add(act);
 		}
 		/*for (IPConRIC ric : ricsToArrogate) {
 			ArrogateLeadership act = new ArrogateLeadership(getIPConHandle(), ric.getRevision(), ric.getIssue(), ric.getCluster());
 			ipconActions.add(act);
 		}*/
 		
 		/*
 		 * Join all RICS you need to
 		 */
 		while (!ricsToJoin.isEmpty()) {
 			IPConRIC ric = ricsToJoin.removeFirst();
 			JoinAsLearner act = new JoinAsLearner(getIPConHandle(), ric.getRevision(), ric.getIssue(), ric.getCluster());
 			ipconActions.add(act);
 		}
 		/*for (IPConRIC ric : ricsToJoin) {
 			JoinAsLearner act = new JoinAsLearner(getIPConHandle(), ric.getRevision(), ric.getIssue(), ric.getCluster());
 			ipconActions.add(act);
 		}*/
 		
 		/*
 		 * Resign leadership in all RICs you should
 		 */
 		while (!ricsToResign.isEmpty()) {
 			IPConRIC ric = ricsToResign.removeFirst();
 			ResignLeadership act = new ResignLeadership(getIPConHandle(), ric.getRevision(), ric.getIssue(), ric.getCluster());
 			ipconActions.add(act);
 		}
 		/*for (IPConRIC ric : ricsToResign) {
 			ResignLeadership act = new ResignLeadership(getIPConHandle(), ric.getRevision(), ric.getIssue(), ric.getCluster());
 			ipconActions.add(act);
 		}*/
 		
 		/*
 		 * Leave all RICs you should
 		 * FIXME this is probably bad, since most will duplicate clusters ?
 		 * Also you might think that you want to do stuff in these clusters before you leave, so...
 		 * (ie, you will leave a cluster after doing stuff in RICs inside it because you only knew you would leave one RIC)
 		 */
 		while (!ricsToLeave.isEmpty()) {
 			IPConRIC ric = ricsToLeave.removeFirst();
 			LeaveCluster act = new LeaveCluster(getIPConHandle(), ric.getCluster());
 			ipconActions.add(act);
 		}
 		
 		/*
 		 * Get obligations
 		 * Get permissions
 		 * Use permissions to instantiate obligations
 		 * Do something sensible from your permissions (eg responses if you didn't vote yet, and voting itself !)
 		 * Check for conflicting obligations/permissions
 		 * Take note of permission to vote
 		 * Add all relevant actions to queue of actions
 		 */
 		LinkedList<IPConAction> obligatedActions = getInstatiatedObligatedActionQueue();
 		while(!obligatedActions.isEmpty()) {
 			ipconActions.add(obligatedActions.removeFirst());
 		}
 		
 		// deal with prospective actions
 		while (!prospectiveActions.isEmpty()) {
 			IPConAction act = instantiateProspectiveAction(prospectiveActions.removeFirst());
 			if (act!=null) {
 				ipconActions.add(act);
 			}
 			// else do nothing
 		}
 		
 		/*
 		 * Derive microgoals to fulfil macrogoals
 		 *  - take into account distance to exit
 		 *  - time to get to exit
 		 *  - fuel economy ?
 		 *  - IPCon agreed speed etc
 		 * Reason actions to fulfil microgoals
 		 * Check for conflicts
 		 * All all relevant actions to queue of actions
 		 * TODO - this is sort of done with the movechoice being based on speedgoal...
 		 * Could be extended to take more goals into account ?
 		 */
 		
 		
 
 		/*
 		 * Do all IPConActions
 		 */
 		while(!ipconActions.isEmpty()) {
 			messageQueue.add(generateIPConActionMsg(ipconActions.removeFirst()));
 		}
 		/*for (IPConAction act : ipconActions) {
 			messageQueue.add(generateIPConActionMsg(act));
 		}*/
 		
 		/*
 		 * Generate broadcast msgs indicating which RICs you're in
 		 */
 		for (Entry<String,Chosen> entry : institutionalFacts.entrySet()) {
 			IPConRIC ric = new IPConRIC(entry.getValue().getRevision(), entry.getValue().getIssue(), entry.getValue().getCluster());
 			messageQueue.add(
 					new ClusterPing(
 							Performative.INFORM, getTime(), network.getAddress(), 
 							new Pair<RoadLocation,Pair<IPConRIC,Chosen>>( myLoc, new Pair<IPConRIC,Chosen>(ric,entry.getValue()) )
 					)
 			);
 		}
 		
 		/*
 		 * Send all messages queued
 		 */
 		//for (Message msg : messageQueue) {
 		while (!messageQueue.isEmpty()) {
 			sendMessage(messageQueue.removeFirst());
 		}
 		
 		
 	 
 		logger.info("[" + getID() + "] My location is: " + this.myLoc + 
 										", my speed is " + this.mySpeed + 
 										", my goalSpeed is " + this.goals.getSpeed() + 
 										", and I have " + junctionsLeft + " junctions to pass before my goal of " + goals.getDest() +
 										", so I am in state " + fsm.getState());
 		logger.info("I can see the following agents:" + locationService.getNearbyAgents());
 		saveDataToDB();
 
 		
 		Pair<CellMove,Integer> move;
 		// Check to see if you want to turn off, then if you can end up at the junction in the next timecycle, do so
 		if (	(fsm.getState().equals("MOVE_TO_EXIT")) && (junctionDist!=null) ) {
 			//move = driver.turnOff();
 			resignAndLeaveAll();
 			move = createExitMove(junctionDist, neighbourChoiceMethod);
 		}
 		else {
 			//move = createMove(ownChoiceMethod, neighbourChoiceMethod);
 			move = newCreateMove(ownChoiceMethod, neighbourChoiceMethod);
 		}
 		if ((junctionDist!=null) && (junctionDist <= move.getA().getYInt())) {
 			passJunction();
 		}
 		saveMoveAndUtilToDB(move);
 		submitMove(move.getA());
 	}
 
 	/**
 	 * Agent should resign and leave all clusters before leaving.
 	 * Should send the msgs in here, since normal msg processing is already finished.
 	 * FIXME Potential issue of joining/arrogating/whatever in the same cycle before this is processed...
 	 */
 	private void resignAndLeaveAll() {
 		logger.info(getID() + " leaving all clusters");
 		Collection<IPConRIC> currentRICs = ipconService.getAllRICs(getIPConHandle());
 		LinkedList<IPConAction> acts = new LinkedList<IPConAction>();
 		LinkedList<IPConActionMsg> msgs = new LinkedList<IPConActionMsg>();
 		for (IPConRIC ric : currentRICs) {
 			if (ipconService.getRICLeader(ric.getRevision(), ric.getIssue(), ric.getCluster()).contains(getIPConHandle())) {
 				acts.add(new ResignLeadership(getIPConHandle(), ric.getRevision(), ric.getIssue(), ric.getCluster()));
 			}
 			acts.add(new LeaveCluster(getIPConHandle(), ric.getCluster()));
 		}
 		for (IPConAction act : acts) {
 			msgs.add(generateIPConActionMsg(act));
 		}
 		while (!msgs.isEmpty()) {
 			logger.debug(getID() + " sending msg " + msgs.getFirst());
 			this.network.sendMessage(msgs.removeFirst());
 		}
 	}
 
 	private Boolean checkProposalSensibleness_FromExecute(IPConRIC issueRIC) {
 		Collection<HasRole> roles = ipconService.getAgentRoles(getIPConHandle(), issueRIC.getRevision(), issueRIC.getIssue(), issueRIC.getCluster());
 		Boolean areProposer = false;
 		Boolean correctPhase = true;
 		// make sure you're a proposer
 		for (HasRole role : roles) {
 			if (role.getRole().equals(Role.PROPOSER)) {
 				areProposer = true;
 				break;
 			}
 		}
 		// make sure there isnt something else going on (eg, you're in a submit phase)
 		// if you have permission to respond, submit, or vote, then you shouldnt request
 		// This might create 2 Requests as the agent will repeat during the next cycle (while the leader is processing it's request)
 		Collection<IPConAction> permissions = ipconService.getPermissions(getIPConHandle(), issueRIC.getRevision(), issueRIC.getIssue(), issueRIC.getCluster());
 		for (IPConAction act : permissions) {
 			if ( (act instanceof Response1B) || (act instanceof Submit2A) || (act instanceof Vote2B) ) {
 				correctPhase = false;
 				break;
 			}
 		}
 		if (areProposer && correctPhase) {
 			return true;
 		}
 		else {
 			return false;
 		}
 		
 	}
 
 	/**
 	 * @param issue
 	 */
 	private void findRICToArrogate_FromExecute(String issue) {
 		// Make a RIC to arrogate
 		// I = issue
 		// C = cluster you are in, if in one
 		// R = ?
 		UUID cluster = null;
 		if (!institutionalFacts.isEmpty()) {
 			// pick a very-psuedo-random cluster you're already in
 			cluster = institutionalFacts.entrySet().iterator().next().getValue().getCluster();
 			logger.trace(getID() + " arrogating issue " + issue + " in existing cluster " + cluster);
 		}
 		else {
 			// check the clusters you're about to join/arrogate
 			HashSet<IPConRIC> set = new HashSet<IPConRIC>();
 			set.addAll(ricsToArrogate);
 			set.addAll(ricsToJoin);
 			if (!set.isEmpty()) {
 				cluster = set.iterator().next().getCluster();
 				logger.trace(getID() + " arrogating issue " + issue + " in to-be-joined cluster " + cluster);
 			}
 			else {
 				// pick a psuedo-random cluster that doesn't exist yet
 				cluster = Random.randomUUID();
 				logger.trace(getID() + " arrogating issue " + issue + " in new cluster " + cluster);
 			}
 		}
 		IPConRIC newRIC = new IPConRIC(0, issue, cluster);
 		ricsToArrogate.add(newRIC);
 		resetImpatience(issue);
 	}
 
 	/**
 	 * @param currentRICs
 	 * @param issue
 	 * @param foundInCluster
 	 * @return
 	 */
 	private Boolean findRICsToJoin_FromExecute(final Collection<IPConRIC> currentRICs,
 			String issue, Boolean foundInCluster) {
 		for (IPConRIC ric : currentRICs) {
 			Collection<IPConRIC> inClusterRICs = ipconService.getRICsInCluster(ric.getCluster());
 			for (IPConRIC ric1 : inClusterRICs) {
 				if (!foundInCluster && ric1.getIssue().equalsIgnoreCase(issue)) {
 					foundInCluster = true;
 					logger.debug(getID() + " found RIC in current cluster (" + ric1 + ") for " + issue + " so will join it.");
 					ricsToJoin.add(ric1);
 					break;
 				}
 			}
 		}
 		return foundInCluster;
 	}
 	
 	/*
 	 * FIXME TODO fix this horrific program flow-of-control (so many returns !)
 	 */
 	private boolean isPreferableToCurrent(IPConRIC ric) {
 		Chosen chosen = ipconService.getChosen(ric.getRevision(), ric.getIssue(), ric.getCluster());
 		if (chosen!=null) {
 			Object value = chosen.getValue();
 			try {
 				// if other cluster is tolerable and current isnt, then switch
 				if (isWithinTolerance(ric.getIssue(), value) && (( !institutionalFacts.containsKey(ric.getIssue()) ) || !isWithinTolerance(ric.getIssue(), institutionalFacts.get(ric.getIssue()).getValue())) ) {
 					return true;
 				}
 				// if current and other cluster are both tolerable... compare leader seniority
 				else if (isWithinTolerance(ric.getIssue(), value) && isWithinTolerance(ric.getIssue(), institutionalFacts.get(ric.getIssue()).getValue())) {
 					// check the other leader's seniority
 					ArrayList<IPConAgent> currentLeads = getLeadersOfCurrentRIC(ric.getIssue());
 					IPConAgent currentLead = null;
 					if (currentLeads!=null && !currentLeads.isEmpty()) {
 						currentLead = currentLeads.get(0);
 					}
 					else {
 						return true; // you want to join the other cluster, because yours doesn't exist or doesn't have a leader
 					}
 					ArrayList<IPConAgent> leads = ipconService.getRICLeader(ric.getRevision(), ric.getIssue(), ric.getCluster());
 					if (leads!=null) {
 						for (IPConAgent lead : leads) {
 							// if the leader is more senior (so you might join) 
 							if (leaderIsMoreSenior(lead, currentLead)) {
 								return true;
 								// break out of loop with return, so you only add one at most
 							}
 						}
 					}
 					return false; // if none of them are more senior (or it doesnt have any leaders) then don't join
 				}
 				// else (if other cluster is not tolerable - stay where you are as after the try)
 			} catch (InvalidClassException e) {
 				logger.debug(e);
 				return false;
 			}
 		}
 		// return false if you except or something else goes wrong
 		return false;
 	}
 
 	/**
 	 * 
 	 * @param issue
 	 * @return the leaders of the ric the agent is in corresponding to issue (may be emptylist), or null if the agent is not in such a ric
 	 */
 	private ArrayList<IPConAgent> getLeadersOfCurrentRIC(String issue) {
 		IPConRIC ric = null;
 		for (IPConRIC current : ipconService.getCurrentRICs()) {
 			if (current.getIssue().equalsIgnoreCase(issue)) {
 				ric = current;
 				break;
 			}
 		}
 		if (ric!=null) {
 			ArrayList<IPConAgent> leaders = ipconService.getRICLeader(ric.getRevision(), ric.getIssue(), ric.getCluster());
 			return leaders;
 		}
 		else {
 			return null;
 		}
 	}
 
 	private ArrayList<IPConRIC> getRICsForIssue(String issue) {
 		ArrayList<IPConRIC> ricsForIssue = new ArrayList<IPConRIC>();
 		for (IPConRIC ric : ipconService.getCurrentRICs(getIPConHandle())) {
 			if (ric.getIssue().equalsIgnoreCase(issue)) {
 				ricsForIssue.add(ric);
 			}
 		}
 		return ricsForIssue;
 	}
 
 	private Boolean checkClusterMembership() {
 		ArrayList<UUID> clusters = new ArrayList<UUID>();
 		for (IPConRIC ric : ipconService.getCurrentRICs(getIPConHandle())) {
 			clusters.add(ric.getCluster());
 		}
 		return ( (!clusters.isEmpty()) && (clusters.size()==1) );
 	}
 
 	/**
 	 * @return map of goals <value,tolerance>
 	 */
 	protected HashMap<String, Pair<Integer, Integer>> getGoalMap() {
 		return getGoals().getMap();
 	}
 	
 	private RoadAgentGoals getGoals() {
 		return this.goals;
 	}
 	
 	private Integer getImpatience(String issue) {
 		if (!impatience.containsKey(issue)) {
 			setImpatience(issue, startImpatience);
 		}
 		return impatience.get(issue);
 	}
 	
 	private void setImpatience(String issue, Integer value) {
 		impatience.put(issue,value);
 	}
 
 	/**
 	 * Determines whether or not the agent may arrogate in this cycle.
 	 * Should be defined so that all agents do not try to arrogate the same
 	 * thing at the same time.
 	 */
 	private boolean isImpatient(String issue) {
 		return (getImpatience(issue)==0);
 	}
 	
 	/**
 	 * Call this after checking for impatience !
 	 * 
 	 * Should only change when the agent has something
 	 * to be impatient about, rather than every cycle, but it's really just
 	 * so that agents don't all arrogate the same RIC at once.
 	 * 
 	 * TODO should reset impatience when nothing to be impatient about
 	 */
 	private void updateImpatience(String issue) {
 		if (getImpatience(issue)==null) {
 			resetImpatience(issue);
 		}
 		setImpatience(issue, getImpatience(issue)-1);
 		if (getImpatience(issue)<0) {
 			resetImpatience(issue);
 		}
 	}
 	
 	/**
 	 * Resets impatience to the startImpatience (when agent has nothing to be impatient about)
 	 */
 	private void resetImpatience(String issue) {
 		setImpatience(issue, startImpatience);
 	}
 	
 	/**
 	 * @param leader IPConAgent to compare to
 	 * @return true if the given agent is more senior than you (so you should resign), false otherwise.
 	 */
 	private boolean leaderIsMoreSenior(IPConAgent leader) {
 		 return ( leader.getIPConID().compareTo(getIPConHandle().getIPConID()) == 1); 
 	}
 	
 	/**
 	 * @param leader1
 	 * @param leader2
 	 * @return true if leader1 is more senior than leader2
 	 */
 	private boolean leaderIsMoreSenior(IPConAgent leader1, IPConAgent leader2) {
 		 return ( leader1.getIPConID().compareTo(leader2.getIPConID()) == 1); 
 	}
 
 	/**
 	 * TODO should probably not just get this from IPCon (that's sort of cheating)
 	 * @param revision
 	 * @param issue
 	 * @param cluster
 	 * @return the Chosen IPConFact for the RIC specified.
 	 */
 	private Chosen getChosenFact(Integer revision, String issue, UUID cluster) {
 		return ipconService.getChosen(revision, issue, cluster);
 	}
 	
 	/**
 	 * 
 	 * @param issue name of issue to be checked
 	 * @param value value to be checked
 	 * @return true if the given value is within the tolerance for the goal relating to the given issue, null if no goal for that issue, false otherwise (or if tested value was null, since null cannot be a goal)
 	 * @throws InvalidClassException if value is the wrong type
 	 */
 	protected Boolean isWithinTolerance(String issue, Object value) throws InvalidClassException {
 		if (value==null) {
 			return false;
 		}
 		else if (!(value instanceof Integer)) {
 			throw new InvalidClassException("Only integer goals are supported. Value was a " + value.getClass());
 			
 		}
 		else {
 			if (!this.getGoalMap().containsKey(issue)){
 				return null;
 			}
 			else {
 				Pair<Integer, Integer> pair = this.getGoalMap().get(issue);
 				return Math.abs(((Integer)value)-pair.getA())<=pair.getB();
 			}
 		}
 	}
 	
 
 	/**
 	 * Instantiates any nulls in prospective actions and checks given values are permitted.
 	 * Only works for Response1B and Vote2B
 	 * @param prospectiveAction Should be a Response1B or Vote2B
 	 * @return fully instantiated action, or null if no permitted instantiation could be found
 	 */
 	private IPConAction instantiateProspectiveAction(final IPConAction prospectiveAction) {
 		ArrayList<IPConAction> permissions = new ArrayList<IPConAction>();
 		IPConAction result = null;
 		// Get permissions for class
 		for (IPConAction per : ipconService.getPermissions(getIPConHandle(), null, null, null)) {
 			String type = per.getClass().getSimpleName();
 			if (type.equalsIgnoreCase(prospectiveAction.getClass().getSimpleName())) {
 				permissions.add(per);
 			}
 		}
 		// if no permissions
 		if (permissions.isEmpty()) {
 			logger.warn(getID() + " is not permitted to " + prospectiveAction + " !");
 			result = null;
 		}
 		else {
 			logger.trace(getID() + " found permissions " + permissions);
 			// if only one option
 			if (permissions.size()==1) {
 				result = instantiateFieldsForProspectivePermission(prospectiveAction, permissions.get(0));
 			}
 			else {
 				// more than one permission
 				// get all the valid instantiations (discard nulls)
 				ArrayList<IPConAction> instantiations = new ArrayList<IPConAction>();
 				for (IPConAction per : permissions) {
 					IPConAction inst = instantiateFieldsForProspectivePermission(prospectiveAction, per);
 					if (inst!=null) {
 						instantiations.add(inst);
 					}
 				}
 				if (instantiations.isEmpty()) {
 					logger.warn(getID() + " could not get any valid actions from " + prospectiveAction);
 					result = null;
 				}
 				else {
 				// if you passed it a fully instantiated permitted set of values, just do that.
 				// (this will probably be the most used option...)
 					if (instantiations.contains(prospectiveAction)) {
 						logger.trace("Passed in action " + prospectiveAction + " was valid, so using that.");
 						result = prospectiveAction;
 					}
 					else {
 						// do something different depending on what action it is.
 						String actionType = prospectiveAction.getClass().getSimpleName();
 						if (actionType.equalsIgnoreCase("Response1B")) {
 							// should never be multiple options
 							if (instantiations.size()!=1) {
 								logger.warn(getID() + " had multiple options for Response1B (" + instantiations + ") so pseudorandomly choosing " + instantiations.get(0));
 							}
 							result = instantiations.get(0);
 						}
 						else if (actionType.equalsIgnoreCase("Vote2B")) {
 							logger.warn(getID() + " didn't pass in a fully instantiated Vote2B (" + prospectiveAction + ") so pseudorandomly choosing " + instantiations.get(0));
 							result = instantiations.get(0);
 						}
 						else {
 							logger.warn(getID() + " cannot instantiate prospective actions of type " + actionType + " so psuedorandomly choosing " + instantiations.get(0));
 							result = instantiations.get(0);
 						}
 					}
 				}
 			}
 		}
 		logger.trace(getID() + " instantiated " + result + " from " + prospectiveAction);
 		return result;
 	}
 
 	/**
 	 * @param prospectiveAction
 	 * @param permission
 	 * @return instantiated action, or null if no permitted instantiation could not be found
 	 */
 	private IPConAction instantiateFieldsForProspectivePermission( final IPConAction prospectiveAction, final IPConAction permission) {
 		ArrayList<Pair<Field,Object>> fieldVals = new ArrayList<Pair<Field,Object>>();
 		IPConAction result = prospectiveAction.copy();
 		for (Field f : permission.getClass().getFields()) {
 			try {
 				// if the field is null, get the permitted value
 				if (f.get(result)==null) {
 					fieldVals.add(new Pair<Field,Object>(f,f.get(permission)));
 				}
 				else {
 					// if the field is not null, check it is actually permitted
 					if (! ( f.get(result).equals(f.get(permission) ) ) ) {
 						logger.debug(getID() + " wanted to use " + f.get(result) + " for " + f.getName() + " which is not permitted! (should never happen). Permitted action was: " + permission);
 						return null;
 					}
 					else {
 						logger.debug(getID() + " got " + f.get(result) + " for " + f.getName() + " which is permitted! Permitted action was: " + permission);
 					}
 				}
 			} catch (IllegalArgumentException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		}
 		if (!fieldVals.isEmpty()) {
 			logger.trace(getID() + " has to instantiate : " + fieldVals);
 			// if there were any nulls in result...
 			// check there are no nulls in fieldVals
 			Boolean nulls = false;
 			
 			for (Pair<Field,Object> pair : fieldVals) {
 				nulls = (nulls || ( pair.getB() == null ) );
 			}
 			// if none then go for it
 			if (!nulls) {
 				// fill in nulls in result
 				for (Pair<Field,Object> pair : fieldVals) {
 					try {
 						pair.getA().set(result, pair.getB());
 					} catch (IllegalArgumentException e) {
 						e.printStackTrace();
 					} catch (IllegalAccessException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			// if there are nulls, then... ? (should never happen)
 			else {
 				logger.warn(getID() + " found nulls in their permission (" + permission + ") when trying to instantiate " + prospectiveAction);
 				result = null;
 			}
 		}
 		else {
 			logger.trace(getID() + " has nothing to do to instantiate " + result);
 		}
 		return result;
 	}
 
 	public LinkedList<IPConAction> TESTgetInstantiatedObligatedActionQueue() {
 		return getInstatiatedObligatedActionQueue();
 	}
 	
 	private LinkedList<IPConAction> getInstatiatedObligatedActionQueue() {
 		HashSet<IPConAction> obligations = (HashSet<IPConAction>) ipconService.getObligations(ipconHandle, null, null, null);
 		HashSet<IPConAction> permissions = (HashSet<IPConAction>) ipconService.getPermissions(ipconHandle, null, null, null);
 		HashMap<String, ArrayList<IPConAction>> perMap = new HashMap<String, ArrayList<IPConAction>>();
 		LinkedList<IPConAction> queue = new LinkedList<IPConAction>();
 		
 		// Split permissions by class
 		for (IPConAction per : permissions) {
 			String type = per.getClass().getSimpleName();
 			if (!perMap.containsKey(type)) {
 				perMap.put(type, new ArrayList<IPConAction>());
 			}
 			perMap.get(type).add(per);
 		}
 		
 		// check to see if you have a permission for each obligation, if you do then fill in any nulls
 		for (IPConAction obl : obligations) {
 			logger.trace(getID() + " is attempting to discharge their obligation to " + obl);
 			// Make your proto-action
 			IPConAction actToDo = null;
 			/* Make sure you have permission to do it,
 			 * and dbl check you really do,
 			 * and make sure they're actually the same class, since we'll be reflecting
 			 */
 			if (!(perMap.containsKey(obl.getClass().getSimpleName()))) logger.trace("perMap does not contains right classname");
 			if (perMap.get(obl.getClass().getSimpleName())!=null) {
 				if (!(!perMap.get(obl.getClass().getSimpleName()).isEmpty())) logger.trace("perMap match is empty");
 				if (!(perMap.get(obl.getClass().getSimpleName()).get(0).getClass().isAssignableFrom(obl.getClass()))) {
 					logger.trace("perMap match (" + perMap.get(obl.getClass().getSimpleName()).get(0) + ") has class " + 
 							perMap.get(obl.getClass().getSimpleName()).get(0).getClass() + ", which is not assignable from " + obl.getClass());
 				}
 			}
 			
 			
 			if (	(perMap.get(obl.getClass().getSimpleName())!=null) && 
 					(perMap.containsKey(obl.getClass().getSimpleName())) &&
 					(!perMap.get(obl.getClass().getSimpleName()).isEmpty()) &&
 					(perMap.get(obl.getClass().getSimpleName()).get(0).getClass().isAssignableFrom(obl.getClass())) ) {
 				
 				actToDo = obl.copy();
 				Field[] fields = obl.getClass().getFields();
 				for (Field f : fields) {
 					logger.trace(getID() + " currently has " + actToDo);
 					// Chop down the string of the field to something more readable
 					String s = f.toString();
 					String delims = "[.]+"; // use + to treat consecutive delims as one, omit to treat consecutive delims separately
 					String[] tokens = s.split(delims);
 					String fName = (Arrays.asList(tokens)).get(tokens.length-1);
 					logger.trace(getID() + " checking field " + fName + " in " + obl);
 					Object fOblVal = null;
 					try {
 						fOblVal = f.get(obl);
 						logger.trace(getID() + " found the value of field " + fName + " in " + obl + " to be " + fOblVal);
 					} catch (Exception e) {
 						logger.error(getID() + " had a problem extracting the fields of an obligation (this should never happen !)" + obl + "...");
 						e.printStackTrace();
 					}
 					if (fOblVal==null) {
 						logger.trace(getID() + " found a null field (" + fName + ") in " + obl);
 						// Go through the permitted values, and get all non-null ones
 						ArrayList<Object> vals = new ArrayList<Object>();
 						ArrayList<IPConAction> perList = perMap.get(obl.getClass().getSimpleName());
 						for (IPConAction act : perList) {
 							Object fActVal = null;
 							try {
 								fActVal = f.get(act);
 								logger.trace(getID() + " found the value of field " + fName + " in " + act + " to be " + fActVal);
 							} catch (Exception e) {
 								logger.error(getID() + " had a problem extracting the fields of an action (this should never happen !)" + act + "...");
 								e.printStackTrace();
 							}
 							if (fActVal!=null && !vals.contains(fActVal)) {
 								vals.add(fActVal);
 							}
 						}
 						
 						// Take the permitted actions and choose one to instantiate with
 						instantiateFieldInObligatedAction(f, actToDo, obl, vals);
 						
 					}
 				}
 				queue.add(actToDo);
 			}
 			else {
 				logger.warn(getID() + " is not permitted to discharge its obligation to " + obl);
 			}
 		}
 		return queue;
 	}
 	
 	/**
 	 * Sets the field f in actToDo to fulfil the obligation obl depending on your permitted values vals
 	 * @param f the field to be filled in
 	 * @param actToDo copy of the obligation, possibly with some nulls filled in
 	 * @param obl the actual obligation, with nulls
 	 * @param vals the permitted values for f. If empty, indicates that the agent is permitted to use any value (though they might not all make sense !)
 	 */
 	private void instantiateFieldInObligatedAction(final Field f, IPConAction actToDo, final IPConAction obl, final ArrayList<Object> vals) {
 
 		// Make it humanreadable
 		String[] tokens = f.toString().split("[.]+");
 		String fName = (Arrays.asList(tokens)).get(tokens.length-1);
 		// If all the permissions are also null, then you can do anything so pick something at random :P 
 		// (You know there are permissions because we previously checked against the list of permissions being empty)
 		if (vals.size()==0) {
 			logger.trace(getID() + " is not constrained by permission on what to set the value of field " + fName + " to be in " + actToDo);
 			if (fName.equals("ballot")) {
 				// choose a valid ballot number
 				/*
 				 * Possible situations where it will be null:
 				 * Prepare1A - need to pick a ballot number that is higher than all current ballot numbers in the same RIC
 				 * Can either rely on responses to tell you to retry with a higher ballot (obligation not implemented yet)
 				 * or pull highest vote/pre_vote/open_vote for RIC and get highest ballot, then add some value
 				 *  ( ballot number is unique unless an error is encountered - should always be unique )
 				 */
 				Integer bal = null;
 				try {
 					if (!obl.getClass().isAssignableFrom(Prepare1A.class)) {
 						throw new IPConException("Obligation was not to Prepare1A. Class was: " + obl.getClass().getSimpleName());
 					}
 					Integer revision = (Integer)(obl.getClass().getField("revision").get(obl));
 					String issue = (String)(obl.getClass().getField("issue").get(obl));
 					UUID cluster = (UUID)(obl.getClass().getField("cluster").get(obl));
 					
 					bal = ballotService.getNext(revision, issue, cluster);
 					
 					/*Pair<Integer, Integer> pair = ipconService.getHighestRevisionBallotPair(issue, cluster);
 					// If we found some valid ones but not in the right revision, then throw an exception anyway
 					if (pair.getA()!=revision) {
 						// technically we should check for higher revisions and adjust based on that, 
 						// but you would hope that you never get obligated to do something in an old revision...
 						throw new IPConException("Only found ballots in the wrong revision. Highest was " + pair);
 					}
 					else {
 						// If you found one, then add one to the ballot :D
 						bal = pair.getB()+1;
 					}*/
 				} catch (IPConException e) {
 					// FIXME technically this should guarantee uniqueness, so picking 0 breaks that requirement
 					// no valid votes, so just go with 0
 					logger.trace(getID() + " couldn't find any ballots so is picking 0 due to error: " + e);
 					bal = 0;
 				} catch (Exception e) {
 					// FIXME technically this should guarantee uniqueness, so picking 0 breaks that requirement
 					// from the getFields... something went wrong...
 					logger.trace(getID() + " had a problem ( " + e + " ) getting the issue or cluster from " + obl + " so is picking a ballot of 0...");
 					bal = 0;
 				}
 				try {
 					logger.trace(getID() + " set the value of field " + fName + " to be " + bal + " in " + actToDo);
 					f.set(actToDo, bal);
 					logger.trace(getID() + " now has " + actToDo);
 				} catch (Exception e) {
 					logger.error(getID() + " had a problem setting the fields of an action to discharge " + obl + "...");
 					e.printStackTrace();
 				}
 			}
 			// biiiiiig if statement...
 			else if ( (fName.equals("value")) ||
 				// pick one - from your goals ?
 				/*
 				 * Possible situations where it will be null:
 				 * Should be never ! (value is constrained to one of two options by permissions in SyncAck)
 				 */
 			
 				(fName.equals("agent")) ||
 				// pick an agent to act on
 				/*
 				 * Possible situations where it will be null:
 				 * Should be never ! (constrained by permission to be yourself, or in case of leader/agent difference, you should never be obligated to do something to *just anyone*)
 				 */
 			
 				(fName.equals("leader")) ||
 				// this should probably be yourself
 				/*
 				 * Possible situations where it will be null:
 				 * Should be never ! (constrained by permission to be yourself)
 				 */
 			
 				(fName.equals("revision")) ||
 				// pick a revision - probably this one ?
 				/*
 				 * Possible situations where it will be null:
 				 * Should be never ! Always have a specific revision in mind when an obligation is formed....
 				 * If we expand to non-obligated actions, then permissions in general can be null... (eg arrogate)
 				 */
 			
 				(fName.equals("issue")) ||
 				// pick an issue - probably this one ?
 				/*
 				 * Possible situations where it will be null:
 				 * Should be never ! Always have a specific issue in mind when an obligation is formed....
 				 * If we expand to non-obligated actions, then permissions in general can be null... (eg arrogate)
 				 */
 			
 				(fName.equals("cluster")) ||
 				// pick a cluster - probably this one ?
 				/*
 				 * Possible situations where it will be null:
 				 * Should be never ! Always have a specific cluster in mind when an obligation is formed....
 				 * If we expand to non-obligated actions, then permissions in general can be null... (eg arrogate)
 				 */
 			
 				(fName.equals("voteBallot")) ||
 				// pick one
 				/*
 				 * Possible situations where it will be null:
 				 * Should be never ! Will either be 0 (if you didn't vote yet) or the ballot you voted in...
 				 */
 			
 				(fName.equals("voteRevision")) ||
 				// pick one
 				/*
 				 * Possible situations where it will be null:
 				 * Should be never ! Will either be the current revision (if you didn't vote yet) or the revision you voted in...
 				 */
 			
 				(fName.equals("voteValue")) ||
 				// pick one
 				/*
 				 * Possible situations where it will be null:
 				 * Should be never ! Will either be IPCNV.val() (if you didn't vote yet) or the value you voted for...
 				 */
 				
 				(fName.equals("role")) ) {
 				// pick one
 				/*
 				 * Possible situations where it will be null:
 				 * Should be never ! No obligated actions concern roles...
 				 */
 				logger.warn(getID() + " encountered a null \"" + fName + "\" field, which should never happen! Obligation was " + obl);
 			}
 			else {
 				logger.warn(getID() + " encountered the unrecognised field \"" + fName + "\" in " + obl);
 			}
 		}
 		// If there is only one then use that.
 		else if (vals.size()==1) {
 			try {
 				logger.trace(getID() + " set the value of field " + fName + " to be " + vals.get(0) + " in " + actToDo);
 				f.set(actToDo, vals.get(0));
 				logger.trace(getID() + " now has " + actToDo);
 			} catch (Exception e) {
 				logger.error(getID() + " had a problem setting the fields of an action to discharge " + obl + "...");
 				e.printStackTrace();
 			}
 		}
 		// If there is more than one, pick one at random ?
 		else {
 			if (fName.equals("value")) {
 				Object val = null;
 				try {
 					// choose a valid response
 					/*
 					 * Only possible in a Syncack - two options of IPCNV.val or the value proposed:
 					 * *NOTE* actually will get permissions for all SyncAcks ! Should only be the ones
 					 * in clusters you have permissions for though, so don't have to mess about too much.
 					 * 
 					 */
 	
 					if (!obl.getClass().isAssignableFrom(SyncAck.class)) {
 						throw new IPConException("Obligation was not to SyncAck. Class was: " + obl.getClass().getSimpleName());
 					}
 					/*if ((vals.size()!=2) || (!vals.contains(IPCNV.val())) ){
 						logger.warn(getID() + " encountered too many, or unexpected, options than usual for a SyncAck (" + vals + ") so is psuedorandomly picking " + vals.get(0));
 						val = vals.get(0);
 					}*/
 					else {						
 						// choose between ipcnv and the given values
 						/*
 						 * Need to work out what the issue is, which goal that corresponds to, and then
 						 * decide whether the given value is close enough to your goal to be acceptable.
 						 * If not (or if you can't work out which goal it matched), then reply IPCNV.val().... 
 						 * 
 						 * note that every time you do this you'll get all the possible values and issues,
 						 * so iterate to find the right one...
 						 */
 						String issue = (String)obl.getClass().getField("issue").get(obl);
 						
 						//remove the IPCNV so you can get the proposed vals
 						vals.remove(IPCNV.val());
 						
 						for (Object currVal : vals) {
 							if (currVal.getClass().isAssignableFrom(Integer.class)) {
 								if (
 									(	(issue.equalsIgnoreCase("speed")) && 
 										(Math.abs( (Integer)currVal - this.goals.getSpeed()  ) <= this.goals.getSpeedTolerance() )
 									) ||
 									(	(issue.equalsIgnoreCase("spacing")) && 
 										(Math.abs( (Integer)currVal - this.goals.getSpacing()  ) <= this.goals.getSpacingTolerance() )
 									)
 									) {
 										logger.trace(getID() + " chose the current value " + currVal + " for the issue " + issue);
 										val = currVal;
 										break;
 								}
 							}
 							else {
 								//vals.remove(IPCNV.val());
 								logger.warn(getID() + " doesn't have a goal for the issue (" + issue + ") or the types didn't match so is happy with the current value " + vals.get(0));
 								val = vals.get(0);
 							}
 						}
 						if (val!=null) {
 							logger.trace(getID() + " found a value it liked (" + val + ") for the issue " + issue);
 						}
 						else {
 							logger.trace(getID() + " did not like any of the values (" + vals + ") for the issue " + issue);
 							val = IPCNV.val();
 						}
 					}
 				} catch (IPConException e) {
 					// no valid votes, so just go with 0
 					logger.trace(getID() + " couldn't find any values so is picking " + IPCNV.val() + " due to error: " + e);
 					val = IPCNV.val();
 				} catch (Exception e) {
 					// from the getFields... something went wrong...
 					logger.trace(getID() + " had a problem getting the issue or cluster from " + obl + " so is picking " + IPCNV.val());
 					val = IPCNV.val();
 				}
 				try {
 					logger.trace(getID() + " set the value of field " + fName + " to be " + val + " in " + actToDo);
 					f.set(actToDo, val);
 					logger.trace(getID() + " now has " + actToDo);
 				} catch (Exception e) {
 					logger.error(getID() + " had a problem setting the fields of an action to discharge " + obl + "...");
 					e.printStackTrace();
 				}
 			}
 			else if ((fName.equals("voteBallot")) ||
 			
 				(fName.equals("voteRevision")) ||
 			
 				(fName.equals("voteValue")) ) {
 				// fill this in for prospective Response1B action
 			}
 			
 			// biiiiiig if statement...
 			else if ( (fName.equals("ballot")) ||
 			
 				(fName.equals("agent")) ||
 				
 				(fName.equals("leader")) ||
 			
 				(fName.equals("revision")) ||
 			
 				(fName.equals("issue")) ||
 			
 				(fName.equals("cluster")) ||
 				
 				(fName.equals("role")) ) {
 					logger.warn(getID() + " encountered a multivalue \"" + fName + "\" field (" + vals + "), which should never happen! Obligation was " + obl);
 					logger.warn(getID() + " pesudorandomly picked the value of field " + fName + " to be " + vals.get(0) + " in " + actToDo);
 					try {
 						f.set(actToDo, vals.get(0));
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 			}
 			else {
 				logger.warn(getID() + " encountered the unrecognised field \"" + fName + "\" in " + obl);
 				logger.warn(getID() + " pesudorandomly picked the value of field " + fName + " to be " + vals.get(0) + " in " + actToDo);
 				try {
 					f.set(actToDo, vals.get(0));
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	private void passJunction(){
 		if (junctionsLeft!=null) {
 			junctionsLeft--;
 			if ((junctionsLeft<1)&& (!fsm.getState().equals("MOVE_TO_EXIT"))) {
 				try {
 					fsm.applyEvent(new MoveToExitEvent());
 					logger.info("[" + getID() + "] Agent " + getName() + " will move towards the exit in the next cycle.");
 				} catch (FSMException e) {
 					logger.warn("FSM can't handle event type MoveToExitEvent:" + e);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Create a safe move with the intention of heading towards the exit
 	 * @return
 	 */
 	private Pair<CellMove,Integer> createExitMove(int nextJunctionDist, NeighbourChoiceMethod neighbourChoiceMethod) {
 		Pair<CellMove,Integer> result = null;
 		if (myLoc.getLane()==0) {
 			if (	(nextJunctionDist>= Math.max((mySpeed-speedService.getMaxDecel()), 1)) &&
 					(nextJunctionDist<= Math.min((mySpeed+speedService.getMaxAccel()), speedService.getMaxSpeed())) ) {
 				result = new Pair<CellMove,Integer>(driver.turnOff(),Integer.MAX_VALUE);
 				logger.debug("[" + getID() + "] Agent " + getName() + " turning off in " + nextJunctionDist);
 			}
 			else {
 				if (nextJunctionDist > Math.min(mySpeed+speedService.getMaxAccel(), speedService.getMaxSpeed())) {
 					// if you can't go past the junction 
 					// choose lowest speed such that you won't crash and will be in lane0 (maybe choose to change up if there are no even tenuously safe moves in lane0)
 					logger.debug("[" + getID() + "] Agent " + getName() + " trying to generate an exitMove");
 					result = newCreateMove(OwnChoiceMethod.MOVE_TO_EXIT, neighbourChoiceMethod);
 				}
 				else {
 					// get min possible speed (have to move somewhere to change lane)
 					int minPossibleSpeed = Math.min(mySpeed-speedService.getMaxDecel(), 1);
 					// get the lowest possible speed you should go to turn off (you already know njd is less than your topspeed)
 					int moveOffset = Math.max(nextJunctionDist, minPossibleSpeed);
 					if (moveOffset<=0) {
 						moveOffset=1;
 					}
 					logger.debug("[" + getID() + "] Agent " + getName() + " turning off at " + nextJunctionDist + " with move " + moveOffset);
 					result = new Pair<CellMove,Integer>(new CellMove(-1, moveOffset),Integer.MAX_VALUE);
 				}
 			}
 		}
 		else {
 			// you're not in lane0 (check validity anyway)
 			logger.debug("[" + getID() + "] Agent " + getName() + " trying to generate an exitMove");
 			result = newCreateMove(OwnChoiceMethod.MOVE_TO_EXIT, neighbourChoiceMethod);
 		}
 		if (result==null){
 			logger.warn("Shouldn't get here.");
 			result = new Pair<CellMove,Integer>(driver.decelerateMax(),Integer.MIN_VALUE);
 		}
 		return result;
 	}
 
 	private Pair<CellMove,Integer> newCreateMove(OwnChoiceMethod ownChoiceMethod, NeighbourChoiceMethod neighbourChoiceMethod){
 		// This is an indirect assumption of only three lanes
 		//  - yes we only want to check in lanes we can move into, but
 		//  - we should also take into account agents not in those lanes which might move into them ahead of us.
 		ArrayList<Integer> availableLanes = new ArrayList<Integer>(3);
 		availableLanes.add(myLoc.getLane());
 		availableLanes.add(myLoc.getLane()+1);
 		availableLanes.add(myLoc.getLane()-1);
 		@SuppressWarnings("unused")
 		Level lvl = logger.getLevel();
 		//logger.setLevel(Level.TRACE);
 		logger.trace("list of lanes is: " + availableLanes);
 		//logger.setLevel(lvl);		
 		
 		
 		// set of agents
 		Map<UUID, Boolean> set = new HashMap<UUID, Boolean>();
 		// map of agents to their set of possible moves
 		HashMap<UUID,HashMap<CellMove,Pair<RoadLocation,RoadLocation>>> agentMoveMap = new HashMap<UUID,HashMap<CellMove,Pair<RoadLocation,RoadLocation>>>();
 
 		for (int lane = 0; lane<=locationService.getLanes(); lane++) {
 			UUID agentFront = locationService.getAgentToFront(lane);
 			if (agentFront!=null) {
 				logger.debug("[" + getID() + "] Agent " + getName() + " saw " + agentFront + " in front in lane " + lane);
 				set.put( agentFront, true );
 			}
 			// Could check all agents in all lanes ? - don't think this is worth it
 			if (lane!=myLoc.getLane()) {
 				UUID agentRear = locationService.getAgentStrictlyToRear(lane);
 				if (agentRear!=null) {
 					logger.debug("[" + getID() + "] Agent " + getName() + " saw " + agentRear + " behind in lane " + lane);
 					set.put( agentRear, false );
 				}
 			}
 		}
 		for (Entry<UUID, Boolean> agent : set.entrySet()) {
 			agentMoveMap.put(agent.getKey(), generateMoves(agent.getKey(), agent.getValue()));
 			/*if (locationService.getAgentLocation(agent.getKey()).getOffset() >= myLoc.getOffset()) {
 				// generate all possible moves for agents in front of you and save start/end location to set in map
 				agentMoveMap.put( agent, generateMoves(agent, true) );
 			}
 			else {
 				// generate possible moves IN SAME LANE for agents behind you - they won't cut you up if theyre behind you
 				agentMoveMap.put( agent, generateMoves(agent, false) );
 			}*/
 		}
 		
 		// generate all possible moves for yourself, and save start/end locs for them
 		HashMap<CellMove,Pair<RoadLocation,RoadLocation>> myMoves = generateMoves(this.getID(), true);
 		HashMap<CellMove,Pair<RoadLocation,RoadLocation>> collisionCheckMoves = new HashMap<CellMove,Pair<RoadLocation,RoadLocation>>(myMoves);
 		
 		if (set.isEmpty()) {
 			// you're done
 		}
 		else {
 			Iterator<Entry<CellMove,Pair<RoadLocation,RoadLocation>>> it = collisionCheckMoves.entrySet().iterator();
 			while (it.hasNext()) {
 				Entry<CellMove,Pair<RoadLocation,RoadLocation>> entryMe = it.next();
 				CellMove myMove = entryMe.getKey();
 				Pair<RoadLocation,RoadLocation> myPair = entryMe.getValue();
 				for (Entry<UUID, Boolean> entry : set.entrySet()) {
 					UUID agent = entry.getKey();
 					for (Entry<CellMove,Pair<RoadLocation,RoadLocation>> entryThem : agentMoveMap.get(agent).entrySet()) {
 						if (collisionCheckMoves.containsKey(myMove)) {
 							Pair<RoadLocation,RoadLocation> pairThem = entryThem.getValue(); 
 							// check all my moves against all their moves, and keep any of mine which don't cause collisions
 							boolean collision = checkForCollisions(myPair.getA(), myPair.getB(), pairThem.getA(), pairThem.getB());
 							if (collision) {
 								it.remove();
 								logger.trace("[" + getID() + "] Agent " + getName() + " found a move collision : " + entryMe.getKey() + " between " + entryMe.getValue());
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		Pair<CellMove, Integer> move;
 		if (collisionCheckMoves.isEmpty()) {
 			logger.warn("[" + getID() + "] Agent " + getName() + " could not find any moves that guarantee no collisions ! Passing out all moves to see which is the best.");
 			collisionCheckMoves = myMoves;
 			logger.debug("[" + getID() + "] Agent " + getName() + " found (no) collisionless moves: " + collisionCheckMoves);
 		}
 		else {
 			logger.debug("[" + getID() + "] Agent " + getName() + " found collisionless moves: " + collisionCheckMoves);
 		}
 		// check for stopping distance (agents to front (& back if diff lane))
 		// return a move with a safety weight - "definitely" safe moves vs moves that you can't stop in time
 		// weight should be the shortfall between your ability to stop in time and where you need to stop
 		HashMap<CellMove,Integer>safetyWeightedMoves = generateStoppingUtilities(collisionCheckMoves);  
 
 		// choose a move from the safe ones, depending on your move choice method
 		move = chooseMove(safetyWeightedMoves, ownChoiceMethod); 
 		return move;
 	}
 	
 	/**
 	 * 
 	 * @param safetyWeightedMoves map of move to weight. Weight is -ve if unsafe, otherwise bigger is better
 	 * @param ownChoiceMethod
 	 * @return preferred move from map based on ownChoiceMethod
 	 */
 	@SuppressWarnings("unchecked")
 	private Pair<CellMove,Integer> chooseMove(HashMap<CellMove, Integer> safetyWeightedMoves, OwnChoiceMethod ownChoiceMethod) {
 		logger.debug("[" + getID() + "] Agent " + getName() + " choosing " + ownChoiceMethod + " move from moves: " + safetyWeightedMoves);
 		Pair<CellMove,Integer> result = new Pair<CellMove,Integer>(driver.constantSpeed(), Integer.MIN_VALUE);
 		if (!safetyWeightedMoves.isEmpty()) {
 			switch (ownChoiceMethod) {
 			case SAFE_CONSTANT : {
 				// sort by safety weighting and use constant speed to break deadlock
 				LinkedList<Map.Entry<CellMove,Integer>> list = new LinkedList<Entry<CellMove,Integer>>();
 				list.addAll(safetyWeightedMoves.entrySet());
 				Collections.sort(list, new ConstantWeightedMoveComparator<Integer>(this.mySpeed));
 				logger.trace("[" + getID() + "] Agent " + getName() + " sorted moves to: " + list);
 				result = Pair.entryToPair(list.getLast());
 				if (result.getA().getYInt()==0 && list.size()>=2 && // if chosen move is to stop, and next move is safe, choose next move instead
 						(list.get(list.size()-2).getValue()>=0)
 						) {
 					result = Pair.entryToPair(list.get(list.size()-2));
 				}
 				break;
 			}
 			case SAFE_FAST : {
 				// sort by weighting and return the one with the highest weight
 				LinkedList<Map.Entry<CellMove,Integer>> list = new LinkedList<Entry<CellMove,Integer>>();
 				list.addAll(safetyWeightedMoves.entrySet());
 				Collections.sort(list, new SpeedWeightedMoveComparator<Integer>());
 				logger.trace("[" + getID() + "] Agent " + getName() + " sorted moves to: " + list);
 				result = Pair.entryToPair(list.getLast());
 				if (result.getA().getYInt()==0 && list.size()>=2 && // if chosen move is to stop, and next move is safe, choose next move instead
 						(list.get(list.size()-2).getValue()>=0)
 						) {
 					result = Pair.entryToPair(list.get(list.size()-2));
 				}
 				break;
 			}
 			case GOALS : {
 				// sort by weighting
 				LinkedList<Map.Entry<CellMove,Integer>> list = new LinkedList<Entry<CellMove,Integer>>();
 				list.addAll(safetyWeightedMoves.entrySet());
 				// take any that are "safe" (ie not negative weight) and discard the rest if there are any that are safe, otherwise return the least-bad
 				LinkedList<Map.Entry<CellMove,Integer>> safestMoves = getSafestMovesAndSort(list);
 				// sort by difference between moveSpeed and goalSpeed
 				LinkedList<Pair<CellMove,Integer>> sortedList = sortBySpeedDiff(safestMoves);
 				logger.trace("[" + getID() + "] Agent " + getName() + " sorted moves to: " + sortedList);
 				result = sortedList.getLast();
 				break;
 			}
 			case SAFE_GOALS : {
 				// sort by safety weighting and use goal speed to break deadlock
 				LinkedList<Map.Entry<CellMove,Integer>> list = new LinkedList<Entry<CellMove,Integer>>();
 				list.addAll(safetyWeightedMoves.entrySet());
 				Collections.sort(list, new ConstantWeightedMoveComparator<Integer>(this.getGoals().getSpeed()));
 				logger.trace("[" + getID() + "] Agent " + getName() + " sorted moves to: " + list);
 				result = Pair.entryToPair(list.getLast());
 				if (result.getA().getYInt()==0 && list.size()>=2 && // if chosen move is to stop, and next move is safe, choose next move instead
 						(list.get(list.size()-2).getValue()>=0)
 						) {
 					result = Pair.entryToPair(list.get(list.size()-2));
 				}
 				break;
 			}
 			case SAFE_INST : {
 				result = chooseInstitutionalMove(safetyWeightedMoves, true);
 				break;
 			}
 			case INST_SAFE : {
 				result = chooseInstitutionalMove(safetyWeightedMoves, false);
 				break;
 			}
 			case MOVE_TO_EXIT : {
 				// choose the slowest safe move that gets you closer to lane0
 				LinkedList<Map.Entry<CellMove,Integer>> list = new LinkedList<Entry<CellMove,Integer>>();
 				list.addAll(safetyWeightedMoves.entrySet());
 				Collections.sort(list, new ExitMoveComparator<Integer>());
 				logger.trace("[" + getID() + "] Agent " + getName() + " sorted moves to: " + list);
 				result = Pair.entryToPair(list.getLast());
 				if (result.getA().getYInt()==0 && list.size()>=2 && // if chosen move is to stop, and next move is safe, choose next move instead
 						(list.get(list.size()-2).getValue()>=0)
 						) {
 					result = Pair.entryToPair(list.get(list.size()-2));
 				}
 				break;
 			}
 			default : {
 				logger.warn("[" + getID() + "] Agent " + getName() + " does not know how to choose by the method \"" + ownChoiceMethod.toString() + "\" so is continuing at current speed");
 				break;
 			}
 			}
 		}
 		else {
 			logger.warn("[" + getID() + "] Agent " + getName() + " was not given any moves to choose from ! Continuing at current speed ...");
 		}
 		logger.debug("[" + getID() + "] Agent " + getName() + " chose to " + result);
 		return result;
 	}
 
 	/**
 	 * @param safetyWeightedMoves
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	private Pair<CellMove,Integer> chooseInstitutionalMove(HashMap<CellMove, Integer> safetyWeightedMoves, Boolean safe) {
 		Pair<CellMove,Integer> result;
 		// sort by safety weighting and use institutional speed and lane to break deadlock
 		LinkedList<Map.Entry<CellMove,Integer>> list = new LinkedList<Entry<CellMove,Integer>>();
 		list.addAll(safetyWeightedMoves.entrySet());
 		Comparator<Entry<CellMove, Integer>> comparator = getInstComparator(safe);
 		Collections.sort(list, comparator);
 		logger.trace("[" + getID() + "] Agent " + getName() + " sorted moves to: " + list);
 		result = Pair.entryToPair(list.getLast());
 		// don't give nudge if inst speed is 0...
 		Chosen instChosen = this.institutionalFacts.get("speed");
 		if (instChosen!=null) {
 			Integer speed = (Integer)(instChosen.getValue());
 			if (speed==null) {
 				speed = this.getGoals().getSpeed();
 			}
 			if (speed!=0 && result.getA().getYInt()==0 && list.size()>=2 && // if chosen move is to stop, and next move is safe, choose next move instead (unless your inst chosen speed is 0)
 					(list.get(list.size()-2).getValue()>=0)
 					) {
 				result = Pair.entryToPair(list.get(list.size()-2));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @param safe
 	 * @return a SafeInst comparator if safe is true, an InstSafe comparator otherwise
 	 */
 	private Comparator<Map.Entry<CellMove, Integer>> getInstComparator(Boolean safe) {
 		Chosen chosen = this.institutionalFacts.get("speed");
 		Integer speed = null;
 		if (chosen!=null) {
 			speed = (Integer)(chosen.getValue());
 		}
 		if (chosen==null || speed==null) {
 			speed = this.getGoals().getSpeed();
 		}
 		Integer laneChange;
 		IPConRIC ric = null;
 		for (IPConRIC current : ipconService.getCurrentRICs()) {
 			if (current.getIssue().equalsIgnoreCase("speed")) {
 				ric = current;
 				break;
 			}
 		}
 		if (ric!=null) {
 			ArrayList<IPConAgent> leaders = ipconService.getRICLeader(ric.getRevision(), ric.getIssue(), ric.getCluster());
 			if (leaders.isEmpty() || leaders.contains(getIPConHandle())) {
 				// don't want to change lanes
 				laneChange = 0;
 			}
 			else { 
 				Integer lane;
 				try {
 					lane = locationService.getAgentLocation(leaders.get(0).getIPConID()).getLane();
 				}
 				catch (CannotSeeAgent e) {
 					logger.info(e);
 					// can't see leader, so stay in current lane
 					lane = myLoc.getLane();
 				}
 				laneChange = lane - myLoc.getLane();
 			}
 		}
 		else {
 			// don't want to change lanes
 			laneChange = 0;
 		}
 		if (safe) {
 			return new SafeInstitutionalMoveComparator<Integer>(speed, laneChange);
 		}
 		else {
 			return new InstitutionalSafeMoveComparator<Integer>(speed, laneChange);
 		}
 	}
 
 	/**
	 * FIXME change this to a %age of goalspeed so that bigger is better (which means switching to ascending order)
	 * This will make the charts at the end make sense...
 	 * 
 	 * @param list - can ignore the value in the entry. Only key (move) is of interest
	 * @return list sorted in desc order by the difference between the speed of the move and the agent's goal speed (the Pair.B) (so last entry is best)
 	 */
 	private LinkedList<Pair<CellMove,Integer>> sortBySpeedDiff(LinkedList<Entry<CellMove, Integer>> list) {
 		LinkedList<Pair<CellMove,Integer>> result = new LinkedList<Pair<CellMove,Integer>>();
 		Integer goalSpeed = this.getGoals().getSpeed();
 		// iterate the list and calculate the difference between the move's speed and the goal speed, then insert to the new list
 		for (Entry<CellMove,Integer> entry : list) {
 			CellMove move = entry.getKey();
			result.add(new Pair<CellMove, Integer>(move, Math.abs(move.getYInt()-goalSpeed)));
 		}		
		Collections.sort(result, new PairBDescComparator<Integer>());
 		return result;
 	}
 
 	/**
 	 * If any of the values in the list entries are +ve (or 0), result will only contain those entries 
 	 * Otherwise, the result will contain only the entries with the highest value
 	 * @param list unsorted
 	 * @return list sorted in descending order with additional constraints as above
 	 */
 	private LinkedList<Entry<CellMove, Integer>> getSafestMovesAndSort(final LinkedList<Entry<CellMove, Integer>> list) {
 		@SuppressWarnings("unchecked")
 		LinkedList<Entry<CellMove,Integer>> result = (LinkedList<Entry<CellMove, Integer>>)list.clone();
 		Collections.sort(result, new SpeedWeightedMoveComparator<Integer>());
 		Collections.reverse(result);
 		/* LIST NOW DESCENDING ORDER */
 		// if you have any +ve entries, you can remove all -ves
 		boolean havePositives = list.getFirst().getValue()>=0;
 		Integer highestSoFar = Integer.MIN_VALUE;
 		
 		// Pass 1 : remove negative values if you have any positives, and find +ve value
 		/*for (Entry<CellMove,Integer> entry : result) {
 			if (entry.getValue()>highestSoFar) {
 				highestSoFar = entry.getValue();
 			}
 			if (havePositives && entry.getValue()<0) {
 				result.remove(entry);
 			}
 		}*/
 		Iterator<Entry<CellMove,Integer>> it1 = result.iterator();
 		while (it1.hasNext()) {
 			Entry<CellMove,Integer> entry = it1.next();
 			if (entry.getValue()>highestSoFar) {
 				highestSoFar = entry.getValue();
 			}
 			if (havePositives && entry.getValue()<0) {
 				it1.remove();
 			}
 		}
 		
 		// Pass 2: if you have no positives, remove all values less than highest value (if positives, then the list is as you want it)
 		if (!havePositives) {
 			/*for (Entry<CellMove,Integer> entry : result) {
 				if (entry.getValue()<highestSoFar) {
 					result.remove(entry);
 				}
 			}*/
 			Iterator<Entry<CellMove,Integer>> it2 = result.iterator();
 			while (it2.hasNext()) {
 				Entry<CellMove,Integer> entry = it2.next();
 				if (entry.getValue()<highestSoFar) {
 					it2.remove();
 				}
 			}
 		}
 		
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param a_start
 	 * @param a_end
 	 * @param b_start
 	 * @param b_end
 	 * @return true if there would be a collision between a and b, false otherwise
 	 */
 	private boolean checkForCollisions(RoadLocation a_start, RoadLocation a_end, RoadLocation b_start, RoadLocation b_end) {
 		boolean result = false;
 		if (a_end.equals(b_end)) {
 			result = true;
 		}
 		else {
 			// start in same lane and end in same lane
 			if ( (b_start.getLane() == a_start.getLane()) && (b_end.getLane() == a_end.getLane())) {
 				// if i was behind and am now in front (or vice versa)
 				int hisEndOffset = b_end.getOffset();
 				int hisStartOffset = b_start.getOffset();
 				int myEndOffset = a_end.getOffset();
 				int myStartOffset = a_start.getOffset();
 				int areaLength = locationService.getWrapPoint();
 				boolean heWrapped = b_end.getOffset() < b_start.getOffset();
 				boolean iWrapped = a_end.getOffset() < a_start.getOffset();
 				if (!iWrapped && heWrapped) {
 					hisEndOffset += areaLength;
 				}
 				if (iWrapped && !heWrapped) {
 					myEndOffset += areaLength;
 				}
 				
 				if ( ( (hisStartOffset<myStartOffset) && (hisEndOffset>myEndOffset) ) ||
 					 ( (hisStartOffset>myStartOffset) && (hisEndOffset<myEndOffset) ) ) {
 					result = true;
 				}
 			}
 		}
 		return result;	
 	}
 
 	/**
 	 * 
 	 * @param moves map of own moves to startloc/endloc pair
 	 * @return map of own moves to utilities. If the utility is negative, it's not a safe move. The larger the utility the better.
 	 */
 	private HashMap<CellMove, Integer> generateStoppingUtilities( HashMap<CellMove,Pair<RoadLocation, RoadLocation>> moves) {
 		HashMap<CellMove,Integer> result = new HashMap<CellMove,Integer>();
 		
 		
 		// for all moves in set
 		for (Entry<CellMove,Pair<RoadLocation, RoadLocation>> entry : moves.entrySet()) {
 			logger.trace("[" + getID() + "] Agent " + getName() + " processing move " + entry + " for stopping utilities.");
 			int startLane = entry.getValue().getA().getLane();
 			int endLane = entry.getValue().getB().getLane();
 			int speed = entry.getKey().getYInt();
 		// get stopping distance based on the movespeed
 			Integer moveStoppingDist = speedService.getStoppingDistance(speed);
 			Integer frontStoppingDist = getStoppingDistanceFront(endLane);
 			Integer rearStoppingDist;
 			if (endLane!=startLane) {
 				rearStoppingDist = getStoppingDistanceRear(endLane);
 			}
 			else {
 				rearStoppingDist = Integer.MIN_VALUE;
 			}
 			logger.trace("[" + getID() + "] Agent " + getName() + " got moveStop=" + moveStoppingDist + ", rearStop=" + rearStoppingDist + ", frontStop=" + frontStoppingDist);
 		// get difference between stopDist and agentToFront's stopDist
 			int frontDiff = Integer.MAX_VALUE;
 			if (frontStoppingDist!=Integer.MAX_VALUE) {
 				frontDiff = frontStoppingDist - moveStoppingDist;
 			}
 		// if moving to another lane, also get difference between stopDist and agentToRear's stopDist
 		// (if no agent behind or if same lane, rearStopDist==MinInt -> rearDiff = MaxInt
 			int rearDiff = Integer.MAX_VALUE;
 			if (rearStoppingDist!=Integer.MIN_VALUE) {
 				rearDiff = rearStoppingDist - moveStoppingDist;
 			}
 			logger.trace("[" + getID() + "] Agent " + getName() + " got rearDiff=" + rearDiff + ", frontDiff=" + frontDiff);
 			// give the smallest difference as the utility -> if it's negative, then it's not a safe move
 			result.put(entry.getKey(), Math.min(frontDiff, rearDiff));
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param agent agent to check
 	 * @param allMoves true if should return all moves, false if should return only moves in same lane
 	 * @return hashmap of moves that the given agent could make in the next cycle, or emptySet if agent could not be seen. Key is CellMove, Value is pair of start/end loc
 	 */
 	private HashMap<CellMove,Pair<RoadLocation,RoadLocation>> generateMoves(UUID agent, boolean allMoves) {
 		logger.debug("[" + getID() + "] Agent " + getName() + " trying to generate moves for " + agent);
 		HashMap<CellMove,Pair<RoadLocation,RoadLocation>> result = new HashMap<CellMove,Pair<RoadLocation,RoadLocation>>();
 		
 		RoadLocation startLoc = null;
 		ArrayList<Integer> laneOffsets= new ArrayList<Integer>(1);
 		Integer startSpeed = null;
 		
 		// get the current location of the agent in question
 		try {
 			startLoc = locationService.getAgentLocation(agent);
 			startSpeed = speedService.getAgentSpeed(agent);
 		} catch (CannotSeeAgent e) {
 			// return empty set
 			logger.info("[" + getID() + "] Agent " + getName() + " tried to generateMoves for " + agent + ", who they cannot see.");
 		}
 		
 		if ( (startLoc!=null) && (startSpeed!=null) ) {
 			if (this.ownChoiceMethod.isInstChoice() && !agent.equals(getID()) && this.agentRICs.containsKey(agent) ) {
 				// get the chosen for the agent you're looking at
 				Integer chosenSpeed = null;
 				Integer chosenLaneChange = null;
 				Pair<Integer, Integer> chosenPair = getInstSpeedAndLane_ForGenerateMoves(agent, startLoc);
 				chosenSpeed = chosenPair.getA();
 				chosenLaneChange = chosenPair.getB();
 				if (chosenSpeed!=null && chosenLaneChange!=null) {
 					// if the agent is doing those already, then just return those
 					// if the agent isnt at those, then generate the moves between what theyre currently doing, and what will let them do that
 					logger.debug("[" + getID() + "] Agent " + getName() + " generating for " + agent + " and found chosenSpeed:" + chosenSpeed + " and chosenLaneChange:" + chosenLaneChange);
 				}
 				else {
 					// get the lanes to be considered, but in relative terms
 					laneOffsets = generateLanes_ForGenerateMoves(allMoves, startLoc, laneOffsets);
 					// generate the moves from those lanes
 					result = generateAllMovesInLanes_ForGenerateMoves(agent, startLoc, laneOffsets, startSpeed, result);
 				}
 			}
 			else {
 				// get the lanes to be considered, but in relative terms
 				laneOffsets = generateLanes_ForGenerateMoves(allMoves, startLoc, laneOffsets);
 				// generate the moves from those lanes
 				result = generateAllMovesInLanes_ForGenerateMoves(agent, startLoc, laneOffsets, startSpeed, result);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @param agent
 	 * @param startLoc
 	 */
 	private Pair<Integer,Integer> getInstSpeedAndLane_ForGenerateMoves(final UUID agent, final RoadLocation startLoc) {
 		Integer chosenSpeed = null;
 		Integer chosenLaneChange = null;
 		IPConRIC ric = null;
 		HashMap<String,ClusterPing> map = this.agentRICs.get(agent);
 		// get the speed (and lane) that agent should choose
 		if (map.containsKey("speed")) {
 			chosenSpeed = (Integer) map.get("speed").getChosen().getValue();
 			ric = map.get("speed").getRIC();
 			ArrayList<IPConAgent> leaders = ipconService.getRICLeader(ric.getRevision(), ric.getIssue(), ric.getCluster());
 			if (leaders==null || leaders.isEmpty()) {
 				// don't want to change lanes
 				chosenLaneChange = 0;
 			}
 			else { 
 				Integer leaderLane;
 				try {
 					// be a little dodgy and use the IPConID instead of the network address on the presumption theyre the same
 					leaderLane = locationService.getAgentLocation(leaders.get(0).getIPConID()).getLane();
 				}
 				catch (CannotSeeAgent e) {
 					logger.info(e);
 					// can't see leader, so stay in current lane
 					leaderLane = startLoc.getLane();
 				}
 				chosenLaneChange = leaderLane - startLoc.getLane();
 			}
 		}
 		return new Pair<Integer,Integer>(chosenSpeed, chosenLaneChange);
 	}
 
 	/**
 	 * @param agent
 	 * @param startLoc
 	 * @param laneOffsets
 	 * @param startSpeed
 	 * @param result
 	 */
 	private HashMap<CellMove, Pair<RoadLocation, RoadLocation>> generateAllMovesInLanes_ForGenerateMoves(final UUID agent,
 			final RoadLocation startLoc, final ArrayList<Integer> laneOffsets,
 			final Integer startSpeed, HashMap<CellMove, Pair<RoadLocation, RoadLocation>> result) {
 		int maxAccel = speedService.getMaxAccel();
 		int maxDecel = speedService.getMaxDecel();
 		int maxSpeed = speedService.getMaxSpeed();
 		int wrapPoint = locationService.getWrapPoint();
 		// for all lane offsets
 		for (int laneMove : laneOffsets) {
 			// for all speeds from currentSpeed-maxDecel to max(currentSpeed+maxAccel,maxSpeed)
 			int topSpeed = Math.min(startSpeed+maxAccel, maxSpeed);
 			int minSpeed = Math.max(0, startSpeed-maxDecel);
 			for (int speedMove=minSpeed; speedMove<=topSpeed; speedMove++) {
 				// add the move corresponding to the lane offset + speed
 				CellMove move = new CellMove(laneMove,speedMove);
 				RoadLocation endLoc = new RoadLocation(startLoc.getLane()+laneMove,MathsUtils.mod(startLoc.getOffset()+speedMove, wrapPoint) ); 
 				// need to not insert laneChanges with no speed...
 				if (!( (move.getYInt()==0) && (move.getXInt()!=0) )) {
 					Pair<RoadLocation, RoadLocation> locations = new Pair<RoadLocation,RoadLocation>(startLoc,endLoc);
 					logger.trace("[" + getID() + "] Agent " + getName() + " generated move for " + agent + ":" + move + " between " + locations);
 					result.put(move, locations);
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @param allMoves
 	 * @param startLoc
 	 * @param laneOffsets
 	 */
 	private ArrayList<Integer> generateLanes_ForGenerateMoves(final boolean allMoves, final RoadLocation startLoc, ArrayList<Integer> laneOffsets) {
 		if (allMoves) {
 			for (int i=-1;i<2;i++) {
 				if (locationService.isValidLane(startLoc.getLane() + i)) {
 					laneOffsets.add(i);
 				}
 			}
 		}
 		else {
 			laneOffsets.add(0);
 		}
 		return laneOffsets;
 	}
 
 	/**
 	 * 
 	 * @param lane
 	 * @return stopping distance required due to agent behind (not) you in given lane. Will be MIN Int (ie big -ve) if no agent found. 
 	 */
 	private Integer getStoppingDistanceRear(int lane) {
 		Integer reqStopDistRear;
 		// get agent to check
 		UUID targetRear = this.locationService.getAgentStrictlyToRear(lane);
 		// if there is someone there
 		if (targetRear!=null) {
 			RoadLocation targetRearLoc = (RoadLocation)locationService.getAgentLocation(targetRear);
 			boolean targetIsAhead = false;
 			if (targetRearLoc.getOffset() > myLoc.getOffset()) {
 				targetIsAhead = true; 
 				logger.debug("[" + getID() + "] Agent " + getName() + " saw that " + targetRear + " is actually ahead");
 			}
 			// get the agent behind's stopping distance
 			logger.debug("[" + getID() + "] Agent " + getName() + " saw agent " + targetRear + " at " + targetRearLoc);
 			int targetStopDist = speedService.getAdjustedStoppingDistance(targetRear, false);
 			logger.debug("[" + getID() + "] Agent " + getName() + " thinks that target's stopping distance is " + targetStopDist);
 			// get the location they will stop at if they accel this turn then decell until stopping
 			// -> their currentLoc + their stopDist
 			int targetStopOffset = targetStopDist + ((RoadLocation)locationService.getAgentLocation(targetRear)).getOffset() + 1;
 			logger.debug("[" + getID() + "] Agent " + getName() + " thinks that target could stop at " + targetStopOffset);
 			
 			/* if this offset is greater than the length of the world then the agent's move will wrap
 			 * so if targetIsAhead then you care, otherwise you don't care
 			 * (if theyre behind you and theyre going to wrap then you should wrap as well... CHECK THIS ?)
 			 * 
 			 * Need to make sure that if you are detecting someone infront of you as being behind you (due to wrap)
 			 * then they are going to wrap - if theyre not going to wrap, then you dont care about them -
 			 * they will be infront of you the whole time...
 			 * 
 			 * ie..
 			 * 
 			 * if (targetIsAhead) {
 			 * 	if (targetStopOffset>length) {
 			 * 		// care
 			 * 	}
 			 * 	else {
 			 * 		// don't care
 			 * 	}
 			 * }
 			 * else {
 			 * 	// theyre actually behind you, so do the below
 			 * }
 			 * 
 			 *  --> if (targetIsAhead && !(targetStopOffset>length)) don't care ; stoppingSpeedRear = -1
 			 *  --> else care
 			 * 
 			 */
 			/* Need to make sure that if you are detecting someone infront of you as being behind you (due to wrap)
 			 * then they are going to wrap - if theyre not going to wrap, then you dont care about them -
 			 * they will be infront of you the whole time..
 			 */
 			if (targetIsAhead && (targetStopOffset<=this.locationService.getWrapPoint())) {
 				// if target is ahead of you and they won't wrap, don't care
 				logger.debug("[" + getID() + "] Agent " + getName() + " saw target ahead of them when checking behind, and they won't wrap, so discounting");
 				reqStopDistRear = Integer.MIN_VALUE;
 			}
 			else {
 				targetStopOffset = MathsUtils.mod(targetStopOffset, this.locationService.getWrapPoint());
 				// you need to be able to stop on the location one infront of it (which is why previous plus one), so work out how far that is from you
 				// gives myLoc-theirLoc -> +ve means you have to make a move, will be -ve if they dont end up in front of you
 				reqStopDistRear = locationService.getOffsetDistanceBetween(new RoadLocation(targetStopOffset, lane), myLoc);
 				logger.debug("[" + getID() + "] Agent " + getName() + " is at " + myLoc.getOffset() + " so has a reqStopDistRear of " + reqStopDistRear);
 			}
 		}
 		else {
 			logger.debug("[" + getID() + "] Agent " + getName() + " didn't see anyone behind them");
 			reqStopDistRear = Integer.MIN_VALUE;
 		}
 		return reqStopDistRear;
 	}
  
 	/**
 	 * 
 	 * @param lane
 	 * @return stopping distance required due to agent ahead (or alongside) you in given lane. Will be MaxInt if no agent found.
 	 */
 	private Integer getStoppingDistanceFront(int lane) {
 		Integer reqStopDistFront;
 		// get agent to check
 		UUID targetFront = this.locationService.getAgentToFront(lane);
 		// if there is someone there
 		if (targetFront!=null) {
 			// get agent in front's stopping distance
 			logger.debug("[" + getID() + "] Agent " + getName() + " saw agent " + targetFront + " at " + (RoadLocation)locationService.getAgentLocation(targetFront));
 			int targetStopDist = speedService.getAdjustedStoppingDistance(targetFront, true);
 			logger.debug("[" + getID() + "] Agent " + getName() + " thinks that target's stopping distance is " + targetStopDist);
 			// add the distance between you and their current location (then minus 1 to make sure you can stop BEFORE them)
 			reqStopDistFront = targetStopDist + (locationService.getOffsetDistanceBetween(myLoc, (RoadLocation)locationService.getAgentLocation(targetFront)))-1;
 			logger.debug("[" + getID() + "] Agent " + getName() + " got a reqStopDistFront of " + reqStopDistFront
 					+ " ( " + targetStopDist + " + (distanceBetween(" + myLoc + "," + (RoadLocation)locationService.getAgentLocation(targetFront) +") = " + (locationService.getOffsetDistanceBetween(myLoc, (RoadLocation)locationService.getAgentLocation(targetFront))) + ") - 1 ) ");
 		}
 		else {
 			logger.debug("[" + getID() + "] Agent " + getName() + " didn't see anyone in front of them");
 			reqStopDistFront = Integer.MAX_VALUE;
 		}
 		return reqStopDistFront;
 	}
 
 	/**
 	 * @param move
 	 */
 	private void submitMove(CellMove move) {
 		// submit move action to the environment.
 		try {
 			logger.debug("[" + getID() + "] Agent " + getName() + " attempting move: " + move);
 			environment.act(move, getID(), authkey);
 		} catch (ActionHandlingException e) {
 			logger.warn("Error trying to move", e);
 		}
 	}
 
 	/**
 	 * @param <T>
 	 * @param act the IPConAction to be sent as a message
 	 * @return a correctly formed Message to be sent, matching act
 	 */
 	private <T extends IPConAction> IPConActionMsg generateIPConActionMsg(T act) {
 		Performative perf = null; 
 		if (act instanceof Prepare1A || act instanceof SyncReq || act instanceof Submit2A) perf = Performative.REQUEST;
 		else if (act instanceof Request0A) perf = Performative.QUERY_REF;
 		else if (act instanceof SyncAck) {
 			if (((SyncAck)act).getValue().equals(IPCNV.val())) perf = Performative.REFUSE;
 			else perf = Performative.AGREE;
 		}
 		else {
 			perf = Performative.INFORM;
 		}
 		return new IPConActionMsg(perf, getTime(), network.getAddress(), act);
 	}
 	
 	@SuppressWarnings("rawtypes") 
 	private void sendMessage(Message msg) {
 		this.network.sendMessage(msg);
 		if (msg instanceof IPConActionMsg) {
 			this.ownMsgs.add((IPConActionMsg)msg);
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void saveDataToDB() {
 		// get current simulation time
 		int time = SimTime.get().intValue();
 		// check db is available
 		if (this.persist != null) {
 			storeInDB("location", time, this.myLoc);
 			storeInDB("speed", time, this.mySpeed);
 			
 			Double speedDissatisfaction = calcStateDissatisfaction();
 			storeInDB("dissatisfaction", time, speedDissatisfaction);
 			
 		}
 	}
 
 	private void saveMoveAndUtilToDB(Pair<CellMove, Integer> move) {
 		// get current simulation time
 		int time = SimTime.get().intValue();
 		// check db is available
 		if (this.persist != null) {
 			storeInDB("move", time, move);
 		}
 	}
 
 	/**
 	 * @param key
 	 * @param time
 	 * @param value
 	 */
 	private void storeInDB(String key, int time, Serializable value) {
 		if (value==null) {
 			value = "";
 		}
 		if (value.getClass().isAssignableFrom(String.class)) {
 			this.persist.getState(time).setProperty(key, (String) value);
 		}
 		else {
 			try {
 				this.persist.getState(time).setProperty(key, StringSerializer.toString(value));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private Double calcStateDissatisfaction() {
 		Integer speed = this.mySpeed;
 		Integer speedGoal = this.goals.getSpeed();
 		Integer speedTolerance = this.goals.getSpeedTolerance();
 		Double speedDissatisfaction;
 		if (speed==speedGoal) {
 			speedDissatisfaction = 0.0;
 		}
 		// FIXME this is probably a stupid way of doing it since I'm going to be abs'ing it...
 		else if (speed>speedGoal) {
 			if (speed > speedGoal+speedTolerance) {
 				speedDissatisfaction = (ToDouble.toDouble(speedTolerance)/2) + speed-(speedGoal+speedTolerance);
 			}
 			else {
 				speedDissatisfaction = (ToDouble.toDouble(speed-speedGoal)/2);
 			}
 		}
 		else {
 			if (speed < speedGoal-speedTolerance) {
 				speedDissatisfaction = (ToDouble.toDouble(speedTolerance)/2) + (speedGoal+speedTolerance)-speed;
 			}
 			else {
 				speedDissatisfaction = (ToDouble.toDouble(speedGoal-speed)/2);
 			}
 		}
 		return Math.abs(speedDissatisfaction);
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.ac.imperial.presage2.util.participant.AbstractParticipant#processInput(uk.ac.imperial.presage2.core.messaging.Input)
 	 */
 	@Override
 	protected void processInput(Input arg0) {
 		if (arg0 instanceof ClusterPing) {
 			process((ClusterPing)arg0);
 		}
 		else if (arg0 instanceof IPConActionMsg) {
 			if ( ((IPConActionMsg)arg0).getType().equalsIgnoreCase("IPConActionMsg[Submit2A]") ) {
 				process((Submit2A)(((IPConActionMsg) arg0).getData()));
 			}
 			else if ( ((IPConActionMsg)arg0).getType().equalsIgnoreCase("IPConActionMsg[Prepare1A]") ) {
 				process((Prepare1A)(((IPConActionMsg) arg0).getData()));
 			}
 			else if ( ((IPConActionMsg)arg0).getType().equalsIgnoreCase("IPConActionMsg[JoinAsLearner]") ) {
 				process((JoinAsLearner)(((IPConActionMsg) arg0).getData()));
 			}
 		}
 		else {
 			logger.debug("[" + getID() + "] Agent " + getName() + " not processing input: " + arg0.toString());
 		}
 	}
 
 	/**
 	 * When an agent recieves a join as learner msgs, it should check to see if it a leader in that cluster.
 	 * If it is, it should make the joining agent an acceptor (kicking off the sync process)
 	 * @param joinAsLearner
 	 */
 	private void process(JoinAsLearner arg0) {
 		logger.debug(getID() + " processing " + arg0);
 		IPConRIC ric = new IPConRIC(arg0.getRevision(), arg0.getIssue(), arg0.getCluster());
 		if ( !(ipconService.getCurrentRICs().contains(ric)) || !(ipconService.getRICLeader(ric.getRevision(), ric.getIssue(), ric.getCluster()).contains(getIPConHandle())) ) {
 			logger.debug(getID() + " is not in " + ric + " and/or is not the leader");
 		}
 		else {
 			Integer revision = ric.getRevision();
 			String issue = ric.getIssue();
 			UUID cluster = ric.getCluster();
 			// doublecheck you have permission
 			Collection<IPConAction> addRolePers = ipconService.getPermissions(getIPConHandle(), revision, issue, cluster);
 			IPConAction addRoleAct = new AddRole(getIPConHandle(), arg0.getAgent(), Role.ACCEPTOR, revision, issue, cluster);
 			if (addRolePers.contains(addRoleAct)) {
 				logger.info(getID() + " will try to " + addRoleAct);
 				ipconActions.add(addRoleAct);
 				// randomly also make them proposer
 				if (Random.randomInt()%9==0) {
 					IPConAction addRolePropAct = new AddRole(getIPConHandle(), arg0.getAgent(), Role.PROPOSER, revision, issue, cluster);
 					if (addRolePers.contains(addRolePropAct)) {
 						logger.info(getID() + " will also try to " + addRolePropAct);
 					}
 					else {
 						logger.warn(getID() + " had permission to " + addRoleAct + " but not to " + addRolePropAct);
 					}
 				}
 			}
 			else {
 				logger.debug(getID() + " thought it was the leader in " + ric + " but does not have permission to " + addRoleAct);
 			}
 		}
 	}
 
 	private void process(ClusterPing arg0) {
 		logger.debug(getID() + " processing ClusterPing " + arg0);
 		this.nearbyRICs.put(arg0.getRIC(), arg0);
 		UUID agent = arg0.getFrom().getId();
 		HashMap<String,ClusterPing> map = this.agentRICs.get(agent);
 		if (map==null) {
 			this.agentRICs.put(agent, new HashMap<String,ClusterPing>());
 			map = this.agentRICs.get(agent);
 		}
 		map.put(arg0.getRIC().getIssue(), arg0);
 	}
 
 	/**
 	 * After receiving a Submit2A message, the agent should decide whether or not to vote or abstain.
 	 * This is done by checking whether or not the submitted value is within tolerance or not.
 	 * If so, or if the agent does not have a goal for the issue, the agent will vote for it
 	 * If not, or if the proposed value is the wrong type compared to what the agent expected, it will abstain.
 	 * @param arg0
 	 */
 	private void process(Submit2A arg0) {
 		logger.debug(getID() + " processing " + arg0);
 		IPConRIC ric = new IPConRIC(arg0.getRevision(), arg0.getIssue(), arg0.getCluster());
 		if (!ipconService.getCurrentRICs().contains(ric)) {
 			logger.debug(getID() + " is not in " + ric);
 		}
 		else {
 			Integer revision = ric.getRevision();
 			String issue = ric.getIssue();
 			UUID cluster = ric.getCluster();
 			Object value = arg0.getValue();
 			Integer ballot = arg0.getBallot();
 			Boolean isWithinTolerance = null;
 			try {
 				isWithinTolerance = isWithinTolerance(issue, value);
 			} catch (InvalidClassException e) {
 				// should warn about it if type is wrong
 				logger.warn(e);
 				isWithinTolerance = false;
 			}
 			if (isWithinTolerance==null || isWithinTolerance) {
 				// vote yes
 				logger.info(getID() + " will try to Vote for " + value + " in " + ric);
 				prospectiveActions.add(new Vote2B(getIPConHandle(), revision, ballot, value, issue, cluster));
 			}
 			else {
 				// don't vote
 				// TODO feed into propose/leaveCluster likelihood ?
 			}
 		}
 	}
 	
 	private void process(Prepare1A arg0) {
 		logger.debug(getID() + " processing " + arg0);
 		IPConRIC ric = new IPConRIC(arg0.getRevision(), arg0.getIssue(), arg0.getCluster());
 		if (!ipconService.getCurrentRICs().contains(ric)) {
 			logger.debug(getID() + " is not in " + ric);
 		}
 		else {
 			Integer revision = ric.getRevision();
 			String issue = ric.getIssue();
 			UUID cluster = ric.getCluster();
 			Integer ballot = arg0.getBallot();
 			// submit prospective action with nulls to be filled in from permission
 			logger.info(getID() + " will try to send a Response1B in " + ric);
 			prospectiveActions.add(new Response1B(getIPConHandle(), null, null, null, revision, ballot, issue, cluster));
 		}
 	}
 	
 	/**
 	 * Used for testing and if the environment needs it. Agents can never get agent objects, so should be safe.
 	 * @return
 	 */
 	public UUID getAuthKey() {
 		return this.authkey;
 	}
 }
