 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical.election;
 
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.ElectionAlive;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.ElectionAnnounceWinner;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.ElectionElect;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.ElectionLeave;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.ElectionPriorityUpdate;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.ElectionReply;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.ElectionResignWinner;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.ElectionReturn;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.SignalingMessageElection;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.Localization;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.Cluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ClusterMember;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ComChannel;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ControlEntity;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.Coordinator;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.CoordinatorAsClusterMember;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 //TODO: invalidateElection() missing, if error during communication with coordinator occurred
 
 /**
  * This class is responsible for coordinator elections. It is instantiated per Cluster and ClusterProxy object.
  *  For a Cluster object, this class plays the role of the cluster head.
  *  For a ClusterProxy, this class acts in the role of a cluster member.
  *
  */
 public class Elector implements Localization
 {
 	private enum ElectorState {
 		START,    // Constructor
 		IDLE,     // no coordinator known, no election running
 		ELECTING, // election process is currently running
 		ELECTED,   // election process has established common consensus about the coordinator of the cluster
 		ERROR // election process run into an error state
 	}
 
 	//TODO: rckkehr von ELECTED zu ELECTING, wenn ElectionAlive von koordinator ausbleibt
 	
 	/** 
 	 * Stores the internal state of the elector
 	 */
 	private ElectorState mState;
 	
 	/**
 	 * Pointer to the parent cluster, which owns this elector
 	 */
 	private ClusterMember mParent = null;
 
 	/**
 	 * The timeout for an awaited "alive" message in [s].
 	 */
 	private long TIMEOUT_FOR_REPLY = 25;
 	/**
 	 * The timeout for an awaited "alive" message in [s].
 	 */
 	private long TIMEOUT_FOR_ALIVE = 25;
 
 	/**
 	 * The time period between two "alive" messages in [s].
 	 */
 	private long PERIOD_FOR_ALIVE = 10;
 
 	/**
 	 * Stores if election was won.
 	 */
 	private boolean mElectionWon = false;
 	
 	/**
 	 * Stores a reference to the HRMController instance
 	 */
 	private HRMController mHRMController = null;
 	
 	/**
 	 * Stores if the parent is an active member
 	 */
 	private boolean mParentIsActiveMember = false;
 	
 	/**
 	 * Stores a counter for processed "re-elects"
 	 */
 	private long mCounterReelects = 0;
 
 	/**
 	 * Stores the causes for re-elections
 	 */
 	private LinkedList<String> mReelectCauses = new LinkedList<String>();
 	
 	/**
 	 * Stores the causes for changes in the election result
 	 */
 	private LinkedList<String> mResultChangeCauses = new LinkedList<String>();
 
 	/**
 	 * Stores the timestamp of the last ElectBroadcast signaling
 	 */
 	private Double mTimestampLastElectBroadcast =  new Double(0);
 	
 	/**
 	 * Stores the node-global election state: the active best ClusterMember instances per hierarchy level.
 	 * All entries per higher (!) hierarchy level have to be part of the same superior cluster. This fact is enforced by leaveWorseAlternativeElections().
 	 * For example, two or more ClusterAsClusterMember instances can be registered, which are part of the same local superior cluster.
 	 * In general, this list stores the best choices for the distributed election process for each higher hierarchy level.
 	 */
 	private LinkedList<ClusterMember>[] mNodeActiveClusterMembers = null;
 
 	/**
 	 * Constructor
 	 *  
 	 * @param pHRMController the HRMController instance
 	 * @param pCluster the parent cluster member
 	 */
 	@SuppressWarnings("unchecked")
 	public Elector(HRMController pHRMController, ClusterMember pClusterMember)
 	{
 		mState = ElectorState.START;
 		mParent = pClusterMember;
 		mElectionWon = false;
 		mHRMController = pHRMController;
 		mNodeActiveClusterMembers = (LinkedList<ClusterMember>[]) mHRMController.getNodeElectionState();
 		
 		// set IDLE state
 		setElectorState(ElectorState.IDLE);
 	}
 	
 	/**
 	 * Creates the node-global election state object.
 	 * It is created once a node is initialized and its HRMController instance is created.
 	 * 
 	 * @return the node-global election state
 	 */
 	@SuppressWarnings("unchecked")
 	public static Object createNodeElectionState()
 	{
 		LinkedList<ClusterMember>[] tResult = null;
 
 		tResult = (LinkedList<ClusterMember>[])new LinkedList[HRMConfig.Hierarchy.HEIGHT];
 		for(int i = 0; i < HRMConfig.Hierarchy.HEIGHT; i++){
 			tResult[i] = new LinkedList<ClusterMember>();
 		}
 		
 		return tResult;	
 	}
 
 	/**
 	 * Creates the node-global election state change description object.
 	 * It is created once a node is initialized and its HRMController instance is created.
 	 * 
 	 * @return the description object
 	 */
 	@SuppressWarnings("unchecked")
 	public static Object createDescriptionNodeElectionState()
 	{
 		String tResult = null;
 
 		tResult = new String();
 		
 		return tResult;	
 	}
 
 	/**
 	 * Adds a ClusterMember as active entity to the database
 	 * 
 	 * @param pClusterMember the new active ClusterMember
 	 * @param pCause the cause for this call
 	 */
 	private void addActiveClusterMember(String pCause)
 	{
 		if(mNodeActiveClusterMembers == null){
 			throw new RuntimeException("Invalid node-global election state");
 		}
 
 		if (mParent instanceof Cluster){
 			throw new RuntimeException("Invalid active ClusterMember: " + mParent);
 		}
 	
 		if(mParent instanceof CoordinatorAsClusterMember){
 			CoordinatorAsClusterMember tCoordinatorAsClusterMember = (CoordinatorAsClusterMember)mParent;
 			
 			tCoordinatorAsClusterMember.eventClusterMembershipToSuperiorCoordinator();
 		}
 		
 		Logging.log(this, "Adding active ClusterMember: " + mParent);
 		synchronized (mNodeActiveClusterMembers) {
 			LinkedList<ClusterMember> tLevelList = mNodeActiveClusterMembers[mParent.getHierarchyLevel().getValue()];
 			
 			if(!tLevelList.contains(mParent)){
 				tLevelList.add(mParent);
 				Logging.log(this, "    ..added");
 				mHRMController.addGUIDescriptionNodeElectionStateChange("\n + " + mParent + "\n   ^^^^" + pCause);
 				
 				mParentIsActiveMember = true;
 			}else{
 				Logging.log(this, "    ..NOT added");
 			}
 		}
 	}
 	
 	/**
 	 * Removes a ClusterMember as active entity from the database
 	 * 
 	 * @param pClusterMember the formerly active ClusterMember
 	 * @param pCause the cause for this call
 	 */
 	private void removeActiveClusterMember(ClusterMember pClusterMember, String pCause)
 	{
 		if(mNodeActiveClusterMembers == null){
 			throw new RuntimeException("Invalid node-global election state");
 		}
 
 		Logging.log(this, "Removing active ClusterMember: " + pClusterMember);
 		synchronized (mNodeActiveClusterMembers) {
 			LinkedList<ClusterMember> tLevelList = mNodeActiveClusterMembers[pClusterMember.getHierarchyLevel().getValue()];
 			
 			if(tLevelList.contains(pClusterMember)){
 				tLevelList.remove(pClusterMember);
 				Logging.log(this, "    ..removed");
 				mHRMController.addGUIDescriptionNodeElectionStateChange("\n - " + pClusterMember + "\n   ^^^^" + pCause);
 				pClusterMember.getElector().mParentIsActiveMember = false;
 			}else{
 				Logging.log(this, "    ..NOT removed");
 			}
 		}
 	}
 
 	/**
 	 * Elects the coordinator for this cluster.
 	 */
 	private void elect()
 	{
 		// set correct elector state
 		setElectorState(ElectorState.ELECTING);
 
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 			Logging.log(this, "ELECTING now...");
 		}
 		
 		if(head()){
 			// do we know more than 0 external cluster members?
 			if (mParent.countConnectedRemoteClusterMembers() > 0){
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 					Logging.log(this, "Trying to ask " + mParent.countConnectedRemoteClusterMembers() + " external cluster members for their Election priority: " + mParent.getComChannels());
 				}
 				distributeELECT();
 			}else{
 				// we don'T have external members - but do we have local members?
 				if(mParent.countConnectedClusterMembers() > 0){					
 					/**
 					 * Send a priority update to all local cluster members
 					 */
 					distributePRIRORITY_UPDATE();
 				}
 				/**
 				 * trigger "detected isolation"
 				 */
 				eventDetectedIsolation();
 			}
 		}else{
 			Logging.log(this, "elect() stops here because parent is not the cluster head: " + mParent);
 		}
 	}
 	
 	/**
 	 * EVENT: elector is invalidated, triggered by ClusterMember if it gets invalidated
 	 */
 	public void eventInvalidation()
 	{
 		Logging.log(this, "EVENT: invalidation");
 		
 		if(mParentIsActiveMember){
 			Logging.log(this, "   ..removing as active ClusterMember");
 			removeActiveClusterMember(mParent, this + "::eventInvalidation()");
 			
 			/**
 			 * Has a local coordinator lost its membership for a foreign cluster? -> we have to activate the CoordinatorAsClusterMember instance which belongs to best (highest prio.) active cluster (has valid coordinator)
 			 */
 			if(mParent instanceof CoordinatorAsClusterMember){
 				mHRMController.addGUIDescriptionNodeElectionStateChange("\n * active ClusterMember gots invalidated: " + mParent);
 
 				Logging.log(this, "    ..active CoordinatorAsClusterMember gots invalidated: " + mParent);
 				Logging.log(this, "    ..will reactive all alternative CoordinatorAsClusterMember at this hierarchy levels..");
 				LinkedList<CoordinatorAsClusterMember> tCoordinatorAsClusterMembers = mHRMController.getAllCoordinatorAsClusterMembers(mParent.getHierarchyLevel().getValue());
 				for(CoordinatorAsClusterMember tCoordinatorAsClusterMember : tCoordinatorAsClusterMembers){
 					if(!tCoordinatorAsClusterMember.equals(mParent)){
 						Logging.log(this, "      ..reactivating link for: " + tCoordinatorAsClusterMember);
 		
 						updateElectionParticipation(tCoordinatorAsClusterMember.getComChannelToClusterHead(), true, this + "::eventInvalidation()");
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * EVENT: all links were deactivated
 	 */
 	private void eventAllLinksInactive()
 	{
 		Logging.log(this, "EVENT: all links inactive");
 		
 		/**
 		 * trigger: "election lost"
 		 */
 		eventElectionLost("eventAllLinksInactive()");
 	}
 	
 	/**
 	 * EVENT: detected isolation, we are the only ClusterMember for this cluster 
 	 */
 	private void eventDetectedIsolation()
 	{
 		Logging.log(this, "EVENT: isolation");
 		
 		Logging.log(this, "I AM WINNER because no alternative cluster member is known, known cluster channels:" );
 		Logging.log(this, "    ..: " + mParent.getComChannels());
 		eventElectionWon("eventDetectedIsolation()");
 	}
 	
 	/**
 	 * EVENT: we have joined a remote cluster, triggered by ClusterMember
 	 * 
 	 * @param pComChannelToRemoteCluster the comm. channel to the cluster
 	 */
 	public void eventJoinedRemoteCluster(ComChannel pComChannelToRemoteCluster)
 	{
 		Logging.log(this, "HAVE JOINED remote cluster");
 
 		/**
 		 * Send: priority update
 		 */
 		distributePRIRORITY_UPDATE();
 	}
 
 	/**
 	 * EVENT: lost candidate
 	 * 
 	 * @param pComChannelToCandidate the comm. channel to the lost candidate (the channel is already removed from all database and this is the last change to get some data from it)
 	 */
 	public void eventLostCandidate(ComChannel pComChannelToCandidate)
 	{
 		//nothing to be done
 	}
 
 	/**
 	 * Counts the re-elects
 	 * 
 	 * @return the number of processed re-elects
 	 */
 	public long countReelects()
 	{
 		return mCounterReelects;
 	}
 	
 	/**
 	 * Returns the causes for re-elections
 	 * 
 	 * @return the list of causes
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<String> getReelectCauses()
 	{
 		LinkedList<String> tResult = null;
 		
 		synchronized (mReelectCauses) {
 			tResult = (LinkedList<String>) mReelectCauses.clone();
 		}
 		return tResult; 
 	}
 
 	/**
 	 * Returns the causes for changes in the election result
 	 * 
 	 * @return the list of causes
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<String> getResultChangeCauses()
 	{
 		LinkedList<String> tResult = null;
 		
 		synchronized (mResultChangeCauses) {
 			tResult = (LinkedList<String>) mResultChangeCauses.clone();
 		}
 		return tResult; 
 	}	
 	
 	/**
 	 * Restarts the election process for this cluster
 	 * 
 	 * @param pCause the causes for this re-election
 	 */
 	private void reelect(String pCause)
 	{
 		if (head()){
 			mCounterReelects++;
 
 			synchronized (mReelectCauses) {
 				mReelectCauses.add("[" + mState.toString() + (isWinner() ? " WINNER, prio=" + mParent.getPriority().getValue() : "") + "]\n   ^^^^" + pCause);
 			}
 			
 			//reset ELECT BROADCAST timer
 			mTimestampLastElectBroadcast = new Double(0);
 			
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 				Logging.log(this, "REELECTION");
 			}
 			elect();
 		}else{
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 				Logging.log(this, "Reelection needed but we aren't the cluster head, we hope that the other local Cluster object will trigger a reelection" );
 			}
 		}
 	}
 	
 	/**
 	 * Starts the election process. This function is usually called by the GUI.
 	 * 
 	 * @param pCause the cause for this election start
 	 */
 	public synchronized void startElection(String pCause)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 			Logging.log(this, "#### STARTING ELECTION");
 		}
 		
 		/**
 		 * Broadcast "ELECT"
 		 */
 		if(mParent instanceof ClusterMember){
 			switch(mState){
 				case IDLE:
 					elect();
 					break;
 				case ELECTED:
 					if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 						Logging.log(this, "RESTARTING ELECTION, old coordinator was valid: " + finished());
 					}
 					reelect(this + "::startElection()\n   ^^^^" + pCause);
 					break;
 				case ELECTING:
 					Logging.log(this, "Election is already running");
 					break;
 				case ERROR:
 					Logging.err(this, "Election is in ERROR state");
 					break;
 				case START:
 					Logging.err(this, "Election is stuck");
 					break;
 				default:
 					break;
 			}
 		}else{
 			throw new RuntimeException("We skipped election start because parent isn't a cluster head/member: " + mParent);
 		}
 	}
 	
 	/**
 	 * Sets the current elector state
 	 * 
 	 * @param pNewState the new state
 	 */
 	private void setElectorState(ElectorState pNewState)
 	{
 		// check if state transition is valid
 		if((pNewState == ElectorState.ERROR) ||
 			(mState == pNewState) || 
 			( ((mState == ElectorState.START) && (pNewState == ElectorState.IDLE)) ||
 			((mState == ElectorState.IDLE) && (pNewState == ElectorState.ELECTING)) ||
 			((mState == ElectorState.ELECTING) && (pNewState == ElectorState.ELECTED)) ||
 			((mState == ElectorState.ELECTED) && (pNewState == ElectorState.ELECTING)) ||
 			((mState == ElectorState.ELECTED) && (pNewState == ElectorState.IDLE))
 		   )) 
 		{
 			if (mState != pNewState){
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 					Logging.log(this, "STATE TRANSITION from " + mState + " to " + pNewState);
 				}
 	
 				// set new state
 				mState = pNewState;
 			}
 		} else {
 			throw new RuntimeException(this + "::setElectorState() can't change the state from " + mState +" to " + pNewState);
 		}
 	}
 	
 	/**
 	 * Determines the current election state and returns a descriptive string.
 	 * 
 	 * @return current state of the election as string
 	 */
 	public String getElectionStateStr()
 	{
 		return mState.toString();
 	}	
 
 	/**
 	 * Determines of this elector has won the election.
 	 * 
 	 * @return true if won, otherwise false
 	 */
 	public boolean isWinner()
 	{
 		return mElectionWon;
 	}
 
 	/**
 	 * Determines if the election process was already finished
 	 * 
 	 * @return true or false
 	 */
 	public boolean finished()
 	{
 		return (mState == ElectorState.ELECTED);
 	}
 
 	/**
 	 * Determines if the timing of an action is okay because the minimum time period between two of such actions is maintained.
 	 * 
 	 * @param pTimestampLastSignaling the timestamp of the last action
 	 * @param pMinPeriod
 	 * @return
 	 */
 	private boolean isTimingOkayOfElectBroadcast()
 	{
 		boolean tResult = false;
 		double tNow = mHRMController.getSimulationTime();
 		double tTimeout = mTimestampLastElectBroadcast.longValue() + TIMEOUT_FOR_REPLY;
 				
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 			Logging.log(this, "Checking timing of ELECT BROADCAST: last=" + mTimestampLastElectBroadcast.longValue() + ", MinPeriod=" + TIMEOUT_FOR_REPLY + ", now=" + tNow + ", MinTime=" + tTimeout);
 		}
 		
 		// is timing okay?
 		if ((mTimestampLastElectBroadcast.doubleValue() == 0) || (tNow > mTimestampLastElectBroadcast.doubleValue() + TIMEOUT_FOR_REPLY)){
 			tResult = true;
 			mTimestampLastElectBroadcast = new Double(tNow);
 			
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 				Logging.log(this, "     ..ELECT BROADCAST is okay");
 			}
 		}else{
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 				Logging.log(this, "     ..ELECT BROADCAST is skipped due to timer");
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * SEND: start the election by signaling BULLY ELECT to all cluster members
 	 */
 	private void distributeELECT()
 	{
 		if (mState == ElectorState.ELECTING){
 			if(mParent.isThisEntityValid()){
 				if (isTimingOkayOfElectBroadcast()){
 					if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 						Logging.log(this, "SENDELECTIONS()-START, electing cluster is " + mParent);
 						Logging.log(this, "SENDELECTIONS(), external cluster members: " + mParent.countConnectedRemoteClusterMembers());
 					}
 			
 					// create the packet
 					ElectionElect tElectionElectPacket = new ElectionElect(mHRMController.getNodeName(), mParent.getPriority());
 					
 					// HINT: we send a broadcast to all cluster members, the common Bully algorithm sends this message only to alternative candidates which have a higher priority				
 					mParent.sendClusterBroadcast(tElectionElectPacket, true);
 					
 					if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 						Logging.log(this, "SENDELECTIONS()-END");
 					}
 				}else{
 					Logging.warn(this, "signalElectBroadcast() was triggered too frequently, timeout isn't reached yet, skipping this action");
 				}
 			}else{
 				Logging.warn(this, "distributeELECT() skipped because parent entity is already invalidated");
 			}
 		}else{
 			Logging.warn(this, "Election has wrong state " + mState + " for signaling an ELECTION START, ELECTING expected");
 
 			// set correct elector state
 			setElectorState(ElectorState.ERROR);
 		}			
 	}
 
 	/**
 	 * SEND: ends the election by signaling BULLY ANNOUNCE to all cluster members 		
 	 */
 	private void distributeANNOUNCE()
 	{
 		if (mState == ElectorState.ELECTED){
 			// get the size of the cluster
 			int tKnownClusterMembers = mParent.countConnectedClusterMembers();
 			
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 				Logging.log(this, "SENDANNOUNCE()-START, electing cluster is " + mParent);
 				Logging.log(this, "SENDANNOUNCE(), cluster members: " + tKnownClusterMembers);
 			}
 	
 			// HINT: the coordinator has to be already created here
 
 			if (mParent.getCoordinator() != null){
 				// create the packet
 				ElectionAnnounceWinner tElectionAnnounceWinnerPacket = new ElectionAnnounceWinner(mHRMController.getNodeName(), mParent.getPriority(), mParent.getCoordinator().getCoordinatorID(), mParent.getCoordinator().toLocation() + "@" + HRMController.getHostName());
 		
 				// send broadcast
 				mParent.sendClusterBroadcast(tElectionAnnounceWinnerPacket, true);
 			}else{
 				Logging.warn(this, "Election has wrong state " + mState + " for signaling an ELECTION END, ELECTED expected");
 				
 				// set correct elector state
 				setElectorState(ElectorState.ERROR);
 			}
 	
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 				Logging.log(this, "SENDANNOUNCE()-END");
 			}
 		}else{
 			// elector state is ELECTED
 			Logging.warn(this, "Election state isn't ELECTING, we cannot finishe an election which wasn't started yet, error in state machine");
 		}			
 	}
 	
 	/**
 	 * SEND: ends the election by signaling BULLY RESIGN to all cluster members 		
 	 */
 	private void distributeRESIGN()
 	{
 		if (mState == ElectorState.ELECTED){
 			// get the size of the cluster
 			int tKnownClusterMembers = mParent.countConnectedClusterMembers();
 			
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 				Logging.log(this, "SENDRESIGN()-START, electing cluster is " + mParent);
 				Logging.log(this, "SENDRESIGN(), cluster members: " + tKnownClusterMembers);
 			}
 	
 			// HINT: the coordinator has to be already created here
 
 			if (mParent.getCoordinator() != null){
 				// create the packet
 				ElectionResignWinner tElectionResignWinnerPacket = new ElectionResignWinner(mHRMController.getNodeName(), mParent.getPriority(), mParent.getCoordinator().getCoordinatorID(), mParent.getCoordinator().toLocation() + "@" + HRMController.getHostName());
 		
 				// send broadcast
 				mParent.sendClusterBroadcast(tElectionResignWinnerPacket, true);
 			}else{
 				Logging.warn(this, "Election has wrong state " + mState + " for signaling an ELECTION END, ELECTED expected");
 				
 				// set correct elector state
 				setElectorState(ElectorState.ERROR);
 			}
 	
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 				Logging.log(this, "SENDRESIGN()-END");
 			}
 		}else{
 			// elector state is ELECTED
 			Logging.warn(this, "Election state isn't ELECTING, we cannot finishe an election which wasn't started yet, error in state machine");
 		}			
 	}
 
 	/**
 	 * SEND: ElectionPriorityUpdate
 	 */
 	private void distributePRIRORITY_UPDATE()
 	{
 		if(mParent.isThisEntityValid()){
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 				Logging.log(this, "SENDPRIOUPDATE()-START, electing cluster is " + mParent);
 				Logging.log(this, "SENDPRIOUPDATE(), cluster members: " + mParent.getComChannels().size());
 			}
 	
 			ElectionPriorityUpdate tElectionPriorityUpdatePacket = new ElectionPriorityUpdate(mHRMController.getNodeName(), mParent.getPriority());
 	
 			// send broadcast
 			Logging.log(this, "Distributing priority update: " + tElectionPriorityUpdatePacket);
 			mParent.sendClusterBroadcast(tElectionPriorityUpdatePacket, true);
 	
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 				Logging.log(this, "SENDPRIOUPDATE()-END");
 			}
 		}else{
 			Logging.warn(this, "distributePRIRORITY_UPDATE() skipped because parent entity is already invalidated");
 		}
 	}
 
 	/**
 	 * SEND: ElectionAlive, report itself as alive by signaling BULLY ALIVE to all cluster members
 	 */
 	public void distributeALIVE()
 	{
 		if (HRMConfig.Election.SEND_ALIVES){
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 				Logging.log(this, "SENDALIVE()-START, electing cluster is " + mParent);
 				Logging.log(this, "SENDALIVE(), cluster members: " + mParent.getComChannels().size());
 			}
 	
 			// create the packet
 			ElectionAlive tElectionAlivePacket = new ElectionAlive(mHRMController.getNodeName(), mParent.getPriority());
 	
 			// send broadcast
 			mParent.sendClusterBroadcast(tElectionAlivePacket, true);
 	
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 				Logging.log(this, "SENDALIVE()-END");
 			}
 		}else{
 			// ElectionAlive messages are currently deactivated
 		}			
 	}
 
 	/**
 	 * (De-)activates the participation for an election group of a cluster head, should only be called by distributeLEAVE() and distributeRETURN()
 	 * 
 	 * @param pComChannel the comm. channel towards the cluster head
 	 * @param pState the new participation state
 	 * @param pCauseForStateChange the cause for this state change 
 	 */
 	private void updateElectionParticipation(ComChannel pComChannel, boolean pState, String pCauseForStateChange)
 	{
 		Logging.log(this, "Changing for election participation to " + Boolean.toString(pState) + " for comm. channel: " + pComChannel);
 		
 		if(pState){
 			// create the packet
 			ElectionReturn tElectionReturnPacket = new ElectionReturn(mHRMController.getNodeName(), pComChannel.getParent().getPriority());
 
 			pComChannel.setLinkActivation(pState, "LEAVE[" + tElectionReturnPacket.getOriginalMessageNumber() + "]\n   ^^^^" + pCauseForStateChange);
 
 			// send
 			pComChannel.sendPacket(tElectionReturnPacket);
 		}else{
 			// create the packet
 			ElectionLeave tElectionLeavePacket = new ElectionLeave(mHRMController.getNodeName(), pComChannel.getParent().getPriority());
 
 			pComChannel.setLinkActivation(pState, "LEAVE[" + tElectionLeavePacket.getOriginalMessageNumber() + "]\n   ^^^^" + pCauseForStateChange);
 
 			// send
 			pComChannel.sendPacket(tElectionLeavePacket);
 		}
 	}
 
 	/**
 	 * SEND: ElectionLeave, report itself as a passive cluster member, should only be called for a ClusterMember
 	 * 
 	 * @param pCause the cause for this signaling
 	 */
 	private void distributeLEAVE(String pCause)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 			Logging.log(this, "SENDLEAVE()-START, electing cluster is " + mParent);
 			Logging.log(this, "SENDLEAVE(), cluster members: " + mParent.getComChannels().size());
 		}
 
 		Logging.log(this, "Leaving election, cause: " + pCause);
 		
 		if(!head()){
 			LinkedList<ComChannel> tChannels = mParent.getComChannels();
 
 			if(tChannels.size() == 1){
 				ComChannel tComChannelToPeer = mParent.getComChannels().getFirst();
 
 				if(tComChannelToPeer.isLinkActive()){
 					updateElectionParticipation(tComChannelToPeer, false, this + "::distributeLEAVE\n   ^^^^" + pCause);
 				}else{
 					if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 						Logging.log(this, "    ..skipped LEAVE");
 					}
 				}
 			}else{
 				throw new RuntimeException("Found an invalid comm. channel list: " + tChannels);
 			}
 		}
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 			Logging.log(this, "SENDLEAVE()-END");
 		}
 	}
 
 	/**
 	 * SEND: ElectionReturn, report itself as a returning active cluster member
 	 * 
 	 * @param pCause the cause for this signaling
 	 */
 	private void distributeRETURN(String pCause)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 			Logging.log(this, "SENDRETURN()-START, electing cluster is " + mParent);
 			Logging.log(this, "SENDRETURN(), cluster members: " + mParent.getComChannels().size());
 		}
 
 		if(!head()){
 			if(mParent.isThisEntityValid()){
 				LinkedList<ComChannel> tChannels = mParent.getComChannels();
 	
 				if(tChannels.size() == 1){
 					ComChannel tComChannelToPeer = mParent.getComChannels().getFirst();
 					
 					if(!tComChannelToPeer.isLinkActive()){
 						updateElectionParticipation(tComChannelToPeer, true, this + "::distributeRETURN()\n   ^^^^" + pCause);
 					}else{
 						if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 							Logging.log(this, "    ..skipped RETURN");
 						}
 					}
 				}else{
 					if(tChannels.size() > 1){
 						throw new RuntimeException("Found an invalid comm. channel list: " + tChannels);
 					}else{
						Logging.warn(this, "Found empty channel list for: " + mParent + ", event cause=" + pCause);
 					}
 				}
 			}
 		}
 
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 			Logging.log(this, "SENDRETURN()-END");
 		}
 	}
 
 	/**
 	 * Leaves alternative elections with a lower priority than the ClusterMember behind the given comm. channel 
 	 * This function is triggered if an ANNOUNCE is received.
 	 * 
 	 * @param pSourceL2Address the L2Address of the source
 	 * @param pSourcePriority the priority of the source
 	 * @param pCause the cause for this call
 	 */
 	private void leaveWorseAlternativeElections(ComChannel pReferenceChannel, String pCause)
 	{
 		L2Address tRefL2Address = pReferenceChannel.getPeerL2Address();
 		ElectionPriority tRefPriority = pReferenceChannel.getPeerPriority();
 		Long tRefClusterID = pReferenceChannel.getRemoteClusterName().getClusterID();
 		ControlEntity tRefParent = pReferenceChannel.getParent();
 		
 		if(HRMConfig.Election.USE_LINK_STATES){
 			// do this only for higher hierarchy levels!
 			// at base hierarchy level, we have local redundant clusters covering the same bus (network interface)
 			if(mParent.getHierarchyLevel().isHigherLevel()){
 				/**
 				 * AVOID multiple LEAVES
 				 */
 				synchronized (mNodeActiveClusterMembers){
 					// get all possible elections
 					LinkedList<CoordinatorAsClusterMember> tLevelClusterMembers = mHRMController.getAllCoordinatorAsClusterMembers(mParent.getHierarchyLevel().getValue());
 					if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_DISTRIBUTED_ELECTIONS){
 						Logging.log(this, "Distributing LEAVE, found ClusterMembers: " + tLevelClusterMembers);
 					}
 					
 					// have we found elections?
 					if(tLevelClusterMembers.size() > 0){					
 						/**
 						 * Iterate over all alternatives
 						 */
 						int tMemberCount = 0;
 						for (CoordinatorAsClusterMember tLevelClusterMember : tLevelClusterMembers){
 							tMemberCount++;
 							
 							// get the priority of the cluster head of the alternative election
 							ElectionPriority tAlternativeElectionClusterHeadPriority = ElectionPriority.create(this);
 							if(tLevelClusterMember.getComChannelToClusterHead() != null){
 								tAlternativeElectionClusterHeadPriority = tLevelClusterMember.getComChannelToClusterHead().getPeerPriority(); 
 							}
 
 							/**
 							 * don't leave this election: is the parent the alternative?
 							 */ 
 							if(!mParent.equals(tLevelClusterMember)){
 								/**
 								 * don't leave this election: is the source the coordinator of this alternative election process?
 								 * HINT: this is only used to make sure we don't leave this election, under normal conditions this case should never happen (more than one local cluster member for the same cluster/coordinator can only happen for base hierarchy levels!)
 								 */
 								if(!tRefL2Address.equals(tLevelClusterMember.getCoordinatorNodeL2Address())){
 									// get the elector
 									Elector tAlternativeElection = tLevelClusterMember.getElector();
 
 									// get the clusterID of the remote cluster to which this ClusterMember belongs
 									Long tAlternativeElectionRemoteClusterID = null;
 									if(tLevelClusterMember.getComChannelToClusterHead() != null){
 										if(tLevelClusterMember.getComChannelToClusterHead().getRemoteClusterName() != null){
 											tAlternativeElectionRemoteClusterID = tLevelClusterMember.getComChannelToClusterHead().getRemoteClusterName().getClusterID(); 
 										}
 									}										
 									
 									if(tAlternativeElection != null){
 										/**********************************************************************************************************************************
 										 * DO ONLY LEAVE elections with a lower priority -> incrementally leave all bad possible elections and find the best election
 										 **********************************************************************************************************************************/
 										if((!tLevelClusterMember.equals(tRefParent)) /* avoid that we compare a control entity with itself and decide by mistake that the priority is lower and we should leave this election */ && 
 										   ((tAlternativeElectionRemoteClusterID == null) || (!tAlternativeElectionRemoteClusterID.equals(tRefClusterID))) /* avoid that we compare two local CoordinatorAsCluster instances, which belong to the same remote cluster*/ && 
 										   (tAlternativeElection.hasClusterLowerPriorityThan(tRefL2Address, tRefPriority)) /* compare the two priorities */){
 											/**
 											 * Update the list of active ClusterMembers:  (it represents the best choices per hier. level)
 											 * 		=> remove this ClusterMember from the list because it's - in every case - not active anymore 
 											 */ 
 											if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_DISTRIBUTED_ELECTIONS){
 												Logging.log(this, "      ..losing active ClusterMember: " + tLevelClusterMember);
 											}
 											removeActiveClusterMember(tLevelClusterMember, this + "::leaveWorseAlternativeElections()_1 [ThisPrio: " + tLevelClusterMember.getPriority().getValue() + " < WinPrio: " + tRefPriority.getValue() + "]\n   ^^^^" + pCause);
 
 											/**
 											 * Distribute "LEAVE" for the alternative election process
 											 */
 											if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_DISTRIBUTED_ELECTIONS){
 												Logging.log(this, "      ..LEAVING: " + tAlternativeElection);
 											}
 											tAlternativeElection.distributeLEAVE(this + "::leaveWorseAlternativeElections()_2 for " + tMemberCount + "/" + tLevelClusterMembers.size() + " member [ThisPrio: " + tAlternativeElectionClusterHeadPriority.getValue() + " < WinPrio: " + tRefPriority.getValue() + ", " + tRefL2Address + "]\n   ^^^^" + pCause);
 										}else{
 											if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_DISTRIBUTED_ELECTIONS){
 												Logging.log(this, "      ..NOT LEAVING: " + tAlternativeElection);
 											}
 											Logging.log(this, "leaveWorseAlternativeElections() aborted for " + tMemberCount + "/" + tLevelClusterMembers.size() + " member [ThisPrio: " + tAlternativeElectionClusterHeadPriority.getValue() + " <> SourcePrio: " + tRefPriority.getValue() + ", " + tRefL2Address + "]\n   ^^^^ " + pCause);
 										}
 										/**********************************************************************************************************************************/
 									}else{
 										throw new RuntimeException("Found invalid elector for: " + tLevelClusterMember);
 									}
 								}else{
 									Logging.log(this, "leaveWorseAlternativeElections() aborted (same cluster!) for " + tMemberCount + "/" + tLevelClusterMembers.size() + " member [ThisPrio: " + tAlternativeElectionClusterHeadPriority.getValue() + " <> SourcePrio: " + tRefPriority.getValue() + ", " + tRefL2Address + "]\n   ^^^^ " + pCause);
 									// we have found a local cluster member which belongs to the same cluster like we do
 								}
 							}else{
 								Logging.log(this, "leaveWorseAlternativeElections() aborted (same cluster!) for " + tMemberCount + "/" + tLevelClusterMembers.size() + " member [ThisPrio: " + tAlternativeElectionClusterHeadPriority.getValue() + " <> SourcePrio: " + tRefPriority.getValue() + ", " + tRefL2Address + "]\n   ^^^^ " + pCause);
 								// we have found this election process
 							}
 						}// for
 					}else{
 						// we haven't even found our parent as ClusterMember at this hierarchy level
 					}
 				}
 			}
 		}	
 	}
 	
 	/**
 	 * Deactivates the local cluster if it is active and has a lower priority than the peer
 	 * 
 	 * @param pComChannel the comm. channel to the possible better peer
 	 */
 	private void deactivateWorseLocalActiveCluster(ComChannel pComChannel)
 	{
 		if(HRMConfig.Election.USE_LINK_STATES){
 			// only do this for a higher hierarchy level! at base hierarchy level we have local redundant cluster covering the same bus (network interface)
 			if(mParent.getHierarchyLevel().isHigherLevel()){
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_DISTRIBUTED_ELECTIONS){
 					Logging.log(this, "Deactivating worse active clusters..");
 				}
 				/**
 				 * AVOID multiple RETURNS
 				 */
 				synchronized (mNodeActiveClusterMembers){
 					Cluster tLocalCluster = mHRMController.getCluster(mParent.getHierarchyLevel().getValue());
 					if(tLocalCluster != null){
 						if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_DISTRIBUTED_ELECTIONS){
 							Logging.log(this, "     ..found locally active cluster: " + tLocalCluster);
 						}
 						Elector tElectorCluster = tLocalCluster.getElector();
 						if(!tElectorCluster.havingHigherPrioriorityThan(pComChannel, true)){
 							if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_DISTRIBUTED_ELECTIONS){
 								Logging.log(this, "     ..found locally worse active cluster: " + tLocalCluster);
 							}
 							
 							/**
 							 * Mark the election as "lost" for the cluster elector 
 							 */
 							if(tElectorCluster.isWinner()){
 								tElectorCluster.eventElectionLost("deactivateWorseLocalActiveCluster() with the better candidate behind: " + pComChannel);
 							}
 						}
 					}
 				}
 			}
 		}		
 	}
 
 	/**
 	 * Leaves alternative elections with a lower priority than this ClusterMember.
 	 * This function is triggered if an ANNOUNCE is simulated by an external leaveReturnOnNewPeerPriority().
 	 * 
 	 * @param pCause the cause for the call
 	 */
 	private void leaveWorseAlternativeElections(String pCause)
 	{
 		if(mParent.isThisEntityValid()){
 			LinkedList<ComChannel> tChannels = mParent.getComChannels();
 	
 			if(tChannels.size() == 1){
 				ComChannel tComChannelToCoordinator = tChannels.getFirst();
 				
 				leaveWorseAlternativeElections(tComChannelToCoordinator, pCause);
 			}else{
 				Logging.err(this, "leaveWorseAlternativeElections() found an unplausible amount of comm. channels: " + tChannels + ", call cause=" + pCause);
 			}
 		}else{
 			Logging.warn(this, "leaveWorseAlternativeElections() because entity is already invalidated, cause=" + pCause);
 		}
 	}
 	
 	/**
 	 * Return to alternative elections if the current parent is an active ClusterMember for this node
 	 * This function is triggered if a RESIGN is received.
 	 * 
 	 * @param pSourceL2Address the L2Address of the source
 	 * @param pSourcePriority the priority of the source
 	 * @param pCause the cause for this call
 	 */
 	private void returnToAlternativeElections(L2Address pSourceL2Address, ElectionPriority pSourcePriority, String pCause)
 	{
 		if(HRMConfig.Election.USE_LINK_STATES){
 			// only do this for a higher hierarchy level! at base hierarchy level we have local redundant cluster covering the same bus (network interface)
 			if(mParent.getHierarchyLevel().isHigherLevel()){
 //				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_DISTRIBUTED_BULLY){
 					Logging.log(this, "Returning to elections..");
 //				}
 				/**
 				 * AVOID multiple RETURNS
 				 */
 				synchronized (mNodeActiveClusterMembers){
 					LinkedList<ClusterMember> tLevelList = mNodeActiveClusterMembers[mParent.getHierarchyLevel().getValue()];
 //					if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_DISTRIBUTED_BULLY){
 						Logging.log(this, "      ..knowing these ACTIVE ClusterMember instances: " + tLevelList);
 //					}
 					
 					/**
 					 * ONLY PROCEED IF THE PARENT IS AN ACTIVE ClusterMember!
 					 */
 					if(tLevelList.contains(mParent)){
 						/**
 						 * Mark/remove this ClusterMember (best choice election) because it's not active anymore
 						 */ 
 						if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_DISTRIBUTED_ELECTIONS){
 							Logging.log(this, "      ..lost active (best choice) ClusterMember: " + mParent);
 						}
 						
 						// get all possible elections
 						LinkedList<CoordinatorAsClusterMember> tLevelClusterMembers = mHRMController.getAllCoordinatorAsClusterMembers(mParent.getHierarchyLevel().getValue());
 						if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_DISTRIBUTED_ELECTIONS){
 							Logging.log(this, "Distributing RETURN, found CoordinatorAsClusterMembers: " + tLevelClusterMembers);
 						}
 						
 						// have we found elections?
 						if(tLevelClusterMembers.size() > 0){					
 							/**
 							 * Iterate over all alternatives
 							 */
 							for (CoordinatorAsClusterMember tLevelClusterMember : tLevelClusterMembers){
 								/**
 								 * don't return to this election: is the parent the alternative?
 								 */ 
 								if(!mParent.equals(tLevelClusterMember)){
 									// are we the coordinator (so, the coordinator is on this node!)?
 									//if(!mHRMController.getNodeL2Address().equals(tLevelClusterMember.getCoordinatorNodeL2Address())){
 										// get the elector
 										Elector tAlternativeElection = tLevelClusterMember.getElector();
 										if(tAlternativeElection != null){
 											/**
 											 * Distribute "RETURN" for the alternative election process
 											 */
 											if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_DISTRIBUTED_ELECTIONS){
 												Logging.log(this, "      ..RETURN to: " + tAlternativeElection);
 											}
 											tAlternativeElection.distributeRETURN(this + "::returnToAlternativeElections()\n   ^^^^" + pCause);
 										}else{
 											throw new RuntimeException("Found invalid elector for: " + tLevelClusterMember);
 										}
 //									}else{
 //										Logging.log(this, "      ..skipping reference to local cluster member which belongs to the same cluster like we do: " + tLevelClusterMember + ", coordinator=" + tLevelClusterMember.getCoordinatorNodeL2Address());
 //									}
 								}else{
 									if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_DISTRIBUTED_ELECTIONS){
 										Logging.log(this, "      ..skipping reference to ourself");
 									}
 								}
 							}// for
 						}else{
 							Logging.err(this, "We haven't even found our parent as ClusterMember at hierarchy level: " + mParent.getHierarchyLevel().getValue());
 						}
 					}else{
 						// parent is not an active ClusterMember
 					}
 				}
 			}
 		}	
 	}
 	
 	/**
 	 * Leave/return alternative elections as reaction on new peer priority in order to correct the distributed election.
 	 * This function reacts on delayed priority updates and is only used for ClusterMember instances.
 	 * 
 	 * @param pComChannel the comm. channel for which a new priority (with influence on the current election result) was received
 	 * @param pPacket the packet which caused this call
 	 */
 	private void leaveReturnOnNewPeerPriority(ComChannel pComChannel, SignalingMessageElection pCausingPacket)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_DISTRIBUTED_ELECTIONS){
 			Logging.log(this, "LEAVE/RETURN because of new priority from: " + pComChannel);
 		}
 
 		if(HRMConfig.Election.USE_LINK_STATES){
 			// only do this for a higher hierarchy level! at base hierarchy level we have local redundant cluster covering the same bus (network interface)
 			if(mParent.getHierarchyLevel().isHigherLevel()){
 				if((mParent instanceof Cluster) || (mParent.getComChannelToClusterHead().isLinkActive())){
 					/**
 					 * AVOID multiple LEAVES/RETURNS
 					 */
 					synchronized (mNodeActiveClusterMembers){
 						if(finished()){
 							/***********************************
 							 ** ELECTED: React similar to a received ANNOUNCE/RESIGN if the election is already finished
 							 ***********************************/
 
 							
 							/**
 							 * Do we belong to an active cluster with an existing (remote) coordinator?
 							 */
 							if(mParent.hasClusterValidCoordinator()){
 								/**
 								 * We behave like we would do if we receive an ANNOUNCE packet
 								 */
 								if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_DISTRIBUTED_ELECTIONS){
 									Logging.log(this, "      ..leave all alternative election processes with a lower priority than the peer");
 								}
 								leaveWorseAlternativeElections(pComChannel, this + "::leaveReturnOnNewPeerPriority()_1 for " + pCausingPacket);
 							}else{
 								/**
 								 * We behave like we would do if we receive a RESIGN packet
 								 */
 								//we skip returnToAlternativeElections(pComChannel.getPeerL2Address(), pComChannel.getPeerPriority()) here because this step was already processed based on the already received RESIGN, a priority update doesn't change anything
 							}
 						}else{
 							/***********************************
 							 * NOT ELECTED:
 							 ***********************************/
 							LinkedList<ClusterMember> tLevelList = mNodeActiveClusterMembers[mParent.getHierarchyLevel().getValue()];
 	
 							/**
 							 * ONLY PROCEED IF AN ACTIVE ClusterMember is already known
 							 */
 							if(tLevelList.size() > 0){
 								/**
 								 * Iterate over all known active ClusterMember entries
 								 */ 
 								for(ClusterMember tClusterMember : tLevelList){
 									Elector tElectorClusterMember = tClusterMember.getElector();
 									
 									/**
 									 * don't leave this election: is the parent the alternative?
 									 */ 
 									if(!mParent.equals(tClusterMember)){
 										if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_DISTRIBUTED_ELECTIONS){
 											Logging.log(this, "      ..leave all alternative election processes in relation to foreign election: " + tElectorClusterMember);
 										}
 										tElectorClusterMember.leaveWorseAlternativeElections(this + "::leaveReturnOnNewPeerPriority()_2 for " + pCausingPacket);
 									}
 								}								
 							}else{
 								// no active ClusterMember is known and the priority update affects only the current ClusterMember
 							}
 						}					
 					}
 				}else{
 					// link is not active
 				}
 			}
 		}
 	}
 
 	/**
 	 * EVENT: sets the local node as coordinator for the parent cluster.
 	 * 
 	 * @param pCause the cause for this event
 	 */
 	private synchronized void eventElectionWon(String pCause)
 	{
 		if ((!isWinner()) || (!finished())){
 			Logging.log(this, "ELECTION WON for cluster " + mParent);
 			
 			// mark as election winner
 			mElectionWon = true;
 			
 			synchronized (mResultChangeCauses) {
 				mResultChangeCauses.add("WON <== " + pCause);
 			}
 
 			// set correct elector state
 			setElectorState(ElectorState.ELECTED);
 	
 			// is the parent the cluster head?
 			if(head()){
 				// get the coordinator from the parental cluster
 				Coordinator tCoordinator = mParent.getCoordinator();
 				if (tCoordinator == null){
 					Cluster tParentCluster = (Cluster)mParent;
 					
 					Logging.log(this, "    ..creating new coordinator at hierarch level: " + mParent.getHierarchyLevel().getValue());
 					
 					// create new coordinator instance
 					tCoordinator = new Coordinator(tParentCluster);
 				}else{
 					Logging.log(this, "Cluster " + mParent + " has already a coordinator");
 				}
 	
 				Logging.log(this, "    ..coordinator is: " + tCoordinator);
 				
 				if(tCoordinator != null){
 					// send BULLY ANNOUNCE in order to signal all cluster members that we are the coordinator
 					distributeANNOUNCE();
 		
 					// trigger event "announced" for the coordinator
 					tCoordinator.eventAnnouncedAsCoordinator();
 				}
 			}else{
 				Logging.log(this, "We have won the election, parent isn't the cluster head: " + mParent + ", waiting for cluster head of alternative cluster");
 			}
 		}else{
 			Logging.warn(this, "Cluster " + mParent + " has still a valid and known coordinator, skipping eventElectionWon() here");
 		}
 	}
 
 	/**
 	 * EVENT: sets the local node as simple cluster member.
 	 * 
 	 * @pCause the cause for this event
 	 */
 	private synchronized void eventElectionLost(String pCause)
 	{
 		Logging.log(this, "ELECTION LOST for cluster " + mParent);
 	
 		// store the old election result
 		boolean tWasFormerWinner = mElectionWon;
 		
 		// mark as election loser
 		mElectionWon = false;
 		
 		synchronized (mResultChangeCauses) {
 			mResultChangeCauses.add("LOST <== " + pCause);
 		}
 
 		// set correct elector state
 		setElectorState(ElectorState.ELECTED);
 		
 		// have we been the former winner of this election?
 		if (tWasFormerWinner){
 			Logging.log(this, "ELECTION LOST BUT WE WERE THE FORMER WINNER");
 
 			/**
 			 * TRIGGER: invalidate the local coordinator because it was deselected by another coordinator
 			 */
 			if(head()){
 				Coordinator tCoordinator = mParent.getCoordinator();
 				if (tCoordinator != null){
 					// send BULLY ANNOUNCE in order to signal all cluster members that we are the coordinator
 					distributeRESIGN();
 
 					/**
 					 * Invalidate the coordinator
 					 */
 					Logging.log(this, "     ..invalidating the coordinator role of: " + tCoordinator);
 					tCoordinator.eventCoordinatorRoleInvalid();
 				}else{
 					Logging.err(this, "We were the former winner of the election but the coordinator is invalid");
 				}
 			}else{
 				// we are not the cluster header, so we can't be the coordinator
 			}
 		}
 	}
 
 	/**
 	 * EVENT: a cluster member left the election process
 	 * 
 	 * @param pComChannel the communication channel to the cluster member which left the election
 	 * @param pLeavePacket the received packet
 	 */
 	private void eventReceivedLEAVE(ComChannel pComChannel, ElectionLeave pLeavePacket)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 			Logging.log(this, "EVENT: cluster member left election: " + pComChannel);
 		}
 
 		// check if the link state has changed	
 		if(pComChannel.isLinkActive()){
 			/**
 			 * deactivate the link for the remote cluster member
 			 */
 			if(head()){
 				pComChannel.setLinkActivation(false, "LEAVE[" + pLeavePacket.getOriginalMessageNumber() + "] received");
 
 				LinkedList<ComChannel> tActiveChannels = mParent.getActiveLinks();
 				
 				// check if we have found at least one active link
 				if(tActiveChannels.size() > 0){
 					// are we the winner?
 					if(isWinner()){
 						// we are the winner and had a higher priority than every other candidate -> ignore this LEAVE because it doesn't influence the result
 						if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 							Logging.log(this, "   ..we are the winner and had a higher priority than ever other candidate, ignoring this LEAVE: " + pLeavePacket);
 						}
 					}else{
 						// maybe it's time for a change -> send re-elect
 						reelect("eventReceivedLEAVE() from " + pComChannel);
 					}
 				}else{
 					/**
 					 * trigger "all links inactive"
 					 */
 					eventAllLinksInactive();
 				}
 			}else{
 				Logging.err(this, "Received as cluster head a LEAVE from: " + pComChannel);
 			}
 		}
 	}
 	
 	/**
 	 * EVENT: a cluster member returned to the election process
 	 * 
 	 * @param pComChannel the communication channel to the cluster member which returned to the election
 	 * @param pReturnPacket the received packet
 	 */
 	private void eventReceivedRETURN(ComChannel pComChannel, ElectionReturn pReturnPacket)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 			Logging.log(this, "EVENT: cluster member returned: " + pComChannel);
 		}
 		
 		// check if the link state has changed	
 		if(!pComChannel.isLinkActive()){
 			/**
 			 * activate the link for the remote cluster member 
 			 */
 			if(head()){
 				pComChannel.setLinkActivation(true, "RETURN[" + pReturnPacket.getOriginalMessageNumber() + "] received");
 
 				// are we the winner?
 //				if(isWinner()){
 					/**
 					 * Trigger : reelect
 					 */
 					reelect("eventReceivedRETURN() from [peerPrio=" + pComChannel.getPeerPriority().getValue() + "]" + pComChannel);
 //				}else{
 //					// we weren't the previous winner and the joined member won't change this
 //					if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 //						Logging.log(this, "   ..we weren't the previous winner and the joined member won't change this, ignoring this RETURN: " + pReturnPacket);
 //					}
 //				}
 			}else{
 				Logging.err(this, "Received as cluster head a RETURN from: " + pComChannel);
 			}
 		}
 	}
 	
 	/**
 	 * SIGNAL: ElectionReply
 	 * 
 	 * @param pComChannel the communication channel along which the RESPONSE should be sent
 	 */
 	private void sendREPLY(ComChannel pComChannel)
 	{
 		if(mParent.isThisEntityValid()){
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 					Logging.log(this, "SENDRESPONSE()-START, electing cluster is " + mParent);
 					Logging.log(this, "SENDRESPONSE(), cluster members: " + mParent.getComChannels().size());
 				}
 			}
 	
 			// create REPLY packet
 			ElectionReply tReplyPacket = new ElectionReply(mHRMController.getNodeName(), pComChannel.getPeerHRMID(), mParent.getPriority());
 				
 			// send the answer packet
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 				Logging.log(this, "BULLY-sending to \"" + pComChannel + "\" a REPLY: " + tReplyPacket);
 			}
 	
 			// send message
 			pComChannel.sendPacket(tReplyPacket);
 	
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 				Logging.log(this, "SENDRESPONSE()-END");
 			}
 		}else{
 			Logging.warn(this, "sendREPLY() for comm. channel \"" + pComChannel + "\" skipped because parent entity is already invalidated");
 		}
 	}
 
 	/**
 	 * SIGNAL: ElectionAnnounce
 	 * 
 	 * @param pComChannel the communication channel along which the ANNOUNCE should be sent
 	 */
 	private void sendANNOUNCE(ComChannel pComChannel)
 	{
 		// create the packet
 		ElectionAnnounceWinner tElectionAnnounceWinnerPacket = new ElectionAnnounceWinner(mHRMController.getNodeName(), mParent.getPriority(), mParent.getCoordinator().getCoordinatorID(), mParent.getCoordinator().toLocation() + "@" + HRMController.getHostName());
 
 		// send message
 		pComChannel.sendPacket(tElectionAnnounceWinnerPacket);
 	}
 	
 	/**
 	 * EVENT: the election process was triggered by another cluster member
 	 * 
 	 * @param pComChannel the source comm. channel
 	 */
 	private void eventReceivedELECT(ComChannel pComChannel)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 			Logging.log(this, "EVENT: received ELECT");
 		}
 
 		// are we the cluster head?
 		if(head()){			
 			// do we have a higher priority than the peer?
 			if (havingHigherPrioriorityThan(pComChannel)){
 				// are we already the election winner?
 				if(isWinner()){
 					sendANNOUNCE(pComChannel);
 				}else{
 					// maybe it's time for a change! -> start re-election
 					reelect("eventReceivedELECT()[" + mState.toString() + "] from " + pComChannel);
 				}
 			}else{
 				// we cannot win -> answer the "elect" message
 				sendREPLY(pComChannel);
 			}
 		}else{
 			// be a fine ClusterMember -> answer the "elect" message
 			sendREPLY(pComChannel);
 		}
 	}
 	
 	/**
 	 * EVENT: another cluster member has sent its Election priority
 	 * 
 	 * @param pSourceComChannel the source comm. channel 
 	 */
 	private void eventReceivedREPLY(ComChannel pSourceComChannel)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 			Logging.log(this, "EVENT: received REPLY");
 		}
 
 		/**
 		 * check for a winner
 		 */
 		if(mState == ElectorState.ELECTING){
 			checkForWinner("eventReceivedREPLY() via " + pSourceComChannel);
 		}else{
 			/**
 			 *  If we are in ELECTED state, we received a delayed reply. This can happen if:
 			 *      0.) we send an ELECT to all peers
 			 *      1.) we receive ElectionPriorityUpdates from all peers
 			 *        ==> we know the priority of all peers
 			 *        ==> we have the highest priority
 			 *        ==> we decide to be the winner
 			 *      2.) a peer answers a former ELECT
 			 */
 			if (mState != ElectorState.ELECTED){
 				Logging.err(this, "Received REPLY in state " + mState + " via: " + pSourceComChannel);
 			}
 		}
 	}
 	
 	/**
 	 * EVENT: the remote (we are not the coordinator!) coordinator was announced
 	 * 
 	 * @param pComChannel the comm. channel from where the packet was received
 	 * @param pAnnouncePacket the packet itself
 	 */
 	private void eventReceivedANNOUNCE(ComChannel pComChannel, ElectionAnnounceWinner pAnnouncePacket)
 	{
 		Logging.log(this, "EVENT: announce: " + pAnnouncePacket);
 		
 		if(!head()){
 			Logging.log(this, "    ..we are a cluster member");
 			
 			ControlEntity tControlEntity = pComChannel.getParent();
 
 			/**
 			 * For an active link we do extended processing of this event for distributed election 
 			 */
 			if(pComChannel.isLinkActive()){
 				Logging.log(this, "    ..we received the ANNOUNCE via an active link");
 
 				LinkedList<ClusterMember> tLevelList = mNodeActiveClusterMembers[mParent.getHierarchyLevel().getValue()];
 
 				// does the previous active ClusterMember for this hier. level has a lower priority than the new candidate?
 				if((tLevelList == null) || (tLevelList.isEmpty()) || 
 				   (tLevelList.getFirst().getElector().hasClusterLowerPriorityThan(pComChannel.getPeerL2Address(), pComChannel.getPeerPriority())) || // the new ClusterMember is the better choice?
 				   ((tLevelList.getFirst().getComChannelToClusterHead().getPeerL2Address().equals(pComChannel.getPeerL2Address()) /* both have the coordinator at the same node? */) && (mParent.getHierarchyLevel().getValue() == 1 /* this exception is only possible for hierarchy level 1 because two L0 coordinator are allowed to e active ClusterMember simultaneously */))){
 					addActiveClusterMember(this + "::eventReceivedANNOUNCE() for " + pAnnouncePacket);
 				}
 				
 				// check local cluster head if it is active and has a lower priority than the peer -> in this case we have to deactivate it 
 				deactivateWorseLocalActiveCluster(pComChannel);
 				
 				// leave all alternative election processes with a lower priority than the peer
 				leaveWorseAlternativeElections(pComChannel, this + "::eventReceivedANNOUNCE() for " + pAnnouncePacket);
 
 			}	
 
 			// mark this cluster as active
 			mParent.setClusterWithValidCoordinator(true);
 	
 			// trigger "election lost"
 			eventElectionLost("eventReceivedANNOUNCE() via " + pComChannel);
 			
 			// trigger: superior coordinator available	
 			tControlEntity.eventClusterCoordinatorAvailable(pAnnouncePacket.getSenderName(), pAnnouncePacket.getCoordinatorID(), pComChannel.getPeerL2Address(), pAnnouncePacket.getCoordinatorDescription());
 		}else{
 			throw new RuntimeException("Got an ANNOUNCE as cluster head");
 		}
 	}
 	
 	/**
 	 * EVENT: the remote (we are not the coordinator!) coordinator resigned
 	 * 
 	 * @param pComChannel the comm. channel from where the packet was received
 	 * @param pResignPacket the packet itself
 	 */
 	private void eventReceivedRESIGN(ComChannel pComChannel, ElectionResignWinner pResignPacket)
 	{
 		Logging.log(this, "EVENT: resign: " + pResignPacket);
 
 		if(!head()){
 			Logging.log(this, "    ..we are a cluster member");
 
 			ControlEntity tControlEntity = pComChannel.getParent();
 
 			/**
 			 * For an active link we do extended processing of this event for distributed election 
 			 */
 			if(pComChannel.isLinkActive()){
 				Logging.log(this, "    ..we received the RESIGN via an active link");
 
 				// return to all other election processes because we have lost this coordinator at this hierarchy level
 				returnToAlternativeElections(pComChannel.getPeerL2Address(), pComChannel.getPeerPriority(), this + "::eventReceivedRESIGN() for " + pResignPacket);
 
 				// mark/store as inactive ClusterMember
 				removeActiveClusterMember(mParent, this + "::eventReceivedRESIGN() for " + pResignPacket);
 			}	
 
 			// mark this cluster as active
 			mParent.setClusterWithValidCoordinator(false);
 
 			// fake (for reset) trigger: superior coordinator available	
 			tControlEntity.eventClusterCoordinatorAvailable(pResignPacket.getSenderName(), -1, pComChannel.getPeerL2Address(), "N/A");
 		}else{
 			throw new RuntimeException("Got a RESIGN as cluster head");
 		}
 	}
 
 	/**
 	 * EVENT: priority update
 	 * 
 	 * @param pComChannel the comm. channel from where the packet was received
 	 * 
 	 * @return true if the new priority could have influence on the election result
 	 */
 	private boolean eventReceivedPRIORITY_UPDATE(ComChannel pComChannel)
 	{
 		boolean tNewPriorityCouldInfluenceElectionResult = false; 
 		
 		// get the priority of the sender
 		ElectionPriority tSenderPriority = pComChannel.getPeerPriority();
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 			Logging.log(this, "EVENT: priority " + tSenderPriority.getValue() + " via comm. channel: " + pComChannel);
 		}
 
 		/**
 		 * React only if the link is active
 		 */
 		if(pComChannel.isLinkActive()){
 			/**
 			 * Have we already won the election and the new priority still lower than ours?
 			 */
 			if(isWinner()){
 				// do we have the higher priority?
 				if (havingHigherPrioriorityThan(pComChannel)){
 //					if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY){
 						Logging.log(this, "eventReceivedPRIORITY_UPDATE(): remote priority " + tSenderPriority.getValue() + " is lower than local " + mParent.getPriority().getValue() + " and we are already the election winner");
 //					}
 				
 					/**
 					 * We (still) have the highest priority -> nothing to change here
 					 */
 					// ..
 				}else{
 					/**
 					 * New received peer priority could influence the election result
 					 */
 					tNewPriorityCouldInfluenceElectionResult = true;
 				}
 			}else{
 				// do we have the higher priority?
 				if (havingHigherPrioriorityThan(pComChannel)){
 //					if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY){
 						Logging.log(this, "eventReceivedPRIORITY_UPDATE(): remote priority " + tSenderPriority.getValue() + " is lower than local " + mParent.getPriority().getValue() + " and we lost the last election");
 //					}
 					/**
 					 * New received peer priority could influence the election result
 					 */
 					tNewPriorityCouldInfluenceElectionResult = true;
 				}
 			}
 		}
 
 		if(tNewPriorityCouldInfluenceElectionResult){
 //			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY){
 				Logging.log(this, "eventReceivedPRIORITY_UPDATE() triggers a re-election");
 //			}
 
 			/**
 			 * Trigger: election
 			 */
 			reelect("eventReceivedPRIORITY_UPDATE(): received new priority " + pComChannel.getPeerPriority().getValue() + " from " + pComChannel);
 		}else{
 			/**
 			 * If the election wasn't finished yet, maybe all needed priorities are available now and the election could be finished.
 			 */
 			if(!finished()){
 				checkForWinner("eventReceivedPRIORITY_UPDATE(): received new priority " + pComChannel.getPeerPriority().getValue() + " from " + pComChannel);
 			}
 		}
 
 		/**
 		 * Deactivate local active cluster if it has a lower priority than the currently received priority from the peer 
 		 */
 		if(pComChannel.getParent() instanceof CoordinatorAsClusterMember){
 			CoordinatorAsClusterMember tCoordinatorAsClusterMember = (CoordinatorAsClusterMember)pComChannel.getParent();
 			
 			if(tCoordinatorAsClusterMember.hasClusterValidCoordinator()){
 				deactivateWorseLocalActiveCluster(pComChannel);
 			}
 		}
 
 		return tNewPriorityCouldInfluenceElectionResult;
 	}
 
 	/**
 	 * Check if we are the elector of a cluster head
 	 * 
 	 * @return true or false
 	 */
 	private boolean head()
 	{
 		return (mParent instanceof Cluster);
 	}
 	
 	/**
 	 * Returns if this Cluster/ClusterMember is allowed to win
 	 *  
 	 * @return true of false
 	 */	
 	public boolean isAllowedToWin()
 	{
 		boolean tAllowedToWin = true;
 		boolean DEBUG = false;
 		
 		if(DEBUG){
 			Logging.log(this, "Checking if election win is allowed..");
 		}
 		
 		if(mParent.getHierarchyLevel().isHigherLevel()){
 			synchronized (mNodeActiveClusterMembers) {
 				LinkedList<ClusterMember> tLevelList = mNodeActiveClusterMembers[mParent.getHierarchyLevel().getValue()];
 				if(DEBUG){
 					Logging.log(this, "       ..found list of known active ClusterMember entries: " + tLevelList);
 				}
 						
 				/**
 				 * ONLY PROCEED IF AN ACTIVE ClusterMember is already known
 				 */
 				if(tLevelList.size() > 0){
 					/**
 					 * Iterate over all known active ClusterMember entries
 					 */ 
 					for(ClusterMember tClusterMember : tLevelList){
 						/**
 						 * Only proceed for memberships of foreign clusters
 						 */
 						if(tClusterMember.isRemoteCluster()){
 							Elector tElectorClusterMember = tClusterMember.getElector();
 							/**
 							 * Only proceed if the remote cluster has a higher priority
 							 */
 							if(DEBUG){
 								Logging.log(this, "   ..checking if Cluster of ClusterMember " + tClusterMember + " has lower priority than local priority " + mParent.getPriority().getValue());
 							}
 							if(!tElectorClusterMember.hasClusterLowerPriorityThan(mHRMController.getNodeL2Address(), mParent.getPriority())){
 								if(DEBUG){
 									Logging.log(this, "      ..NOT ALLOWED TO WIN because alternative better cluster membership exists, elector: " + tElectorClusterMember);
 								}
 								tAllowedToWin = false;
 								break;
 							}
 						}
 					}								
 				}else{
 					if(DEBUG){
 						// no active ClusterMember is known and the Cluster/ClusterMember is allowed to win
 						Logging.log(this, "       ..no active ClusterMember is known and the Cluster/ClusterMember is allowed to win");
 					}
 				}
 			}
 		}
 		
 		if(DEBUG){
 			Logging.log(this, "   ..isAllowedToWin() result: " + tAllowedToWin);
 		}
 				
 		return tAllowedToWin;
 	}
 	
 	/**
 	 * Checks for a winner
 	 * 
 	 * @param pCause the cause for this event
 	 */
 	private synchronized void checkForWinner(String pCause)
 	{
 		boolean tIsWinner = true;
 		boolean tElectionComplete = true;
 		ComChannel tExternalWinner = null;
 		
 		if(mState == ElectorState.ELECTING){
 			Logging.log(this, "Checking for election winner..");
 			
 			if(isAllowedToWin()){
 				LinkedList<ComChannel> tActiveChannels = mParent.getActiveLinks();
 				
 				// check if we have found at least one active link
 				if(tActiveChannels.size() > 0){
 					// do we know more than 0 external cluster members?
 					if (mParent.countConnectedRemoteClusterMembers() > 0){
 						/**
 						 * Iterate over all cluster members and check if their priority is available, check every cluster member if it has a higher priority
 						 */
 						Logging.log(this, "   ..searching for highest priority...");
 						for(ComChannel tComChannel : tActiveChannels) {
 							ElectionPriority tPriority = tComChannel.getPeerPriority(); 
 							
 							/**
 							 * are we still waiting for the Election priority of some cluster member?
 							 */
 							if ((tPriority == null) || (tPriority.isUndefined())){
 								Logging.log(this, "		   ..missing peer priority for: " + tComChannel);
 								
 								// election is incomplete
 								tElectionComplete = false;
 							
 								// leave the loop because we already known that the election is incomplete
 								break;
 							}
 							
 							Logging.log(this, "		..cluster member " + tComChannel + " has priority " + tPriority.getValue());
 							
 							/**
 							 * compare our priority with each priority of a cluster member 
 							 */
 							if(!havingHigherPrioriorityThan(tComChannel)) {
 								Logging.log(this, "		   ..found better candidate: " + tComChannel);
 								tExternalWinner = tComChannel;
 								tIsWinner = false;
 							}
 						}
 						
 						/**
 						 * Check if election is complete
 						 */
 						if (tElectionComplete){
 							/**
 							 * React on the result
 							 */
 							if(tIsWinner) {
 								Logging.log(this, "	        ..I AM WINNER");
 								eventElectionWon("checkForWinner()\n   ^^^^" + pCause);
 							}else{
 								if (tExternalWinner != null){
 									Logging.log(this, "	        ..seeing " + tExternalWinner.getPeerL2Address() + " as better coordinator candidate");
 								}else{
 									Logging.err(this, "External winner is unknown but also I am not the winner");
 								}
 								eventElectionLost("checkForWinner() [" + tActiveChannels.size() + " active channels, winner: " + tExternalWinner + "]\n   ^^^^" + pCause);
 							}
 						}else{
 							Logging.log(this, "	        ..incomplete election");
 							// election is incomplete: we are still waiting for some priority value(s)
 						}
 					}else{
 						/**
 						 * trigger "detected isolation"
 						 */
 						eventDetectedIsolation();
 					}
 				}else{
 					/**
 					 * trigger "all links inactive"
 					 */
 					eventAllLinksInactive();
 				}
 			}else{
 				Logging.log(this, "	        ..NOT ALLOWED TO WIN because alternative better cluster membership exists");
 				eventElectionLost("checkForWinner() [not allowed to win] <== " + pCause);
 			}
 		}else{
 			Logging.err(this, "checkForWinner() EXPECTED STATE \"ELECTING\" here but got state: " + mState.toString());
 		}
 	}
 	
 	/**
 	 * Handles an election signaling packet
 	 * 
 	 * @param pPacket the packet
 	 * @param pComChannel the communication channel from where the message was received
 	 */
 	@SuppressWarnings("unused")
 	public synchronized void handleElectionMessage(SignalingMessageElection pPacket, ComChannel pComChannel)
 	{
 		Node tNode = mHRMController.getNode();
 		Name tLocalNodeName = mHRMController.getNodeName(); 
 		ControlEntity tControlEntity = pComChannel.getParent();
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS)
 			Logging.log(this, "RECEIVED BULLY MESSAGE " + pPacket.getClass().getSimpleName() + " FROM " + pComChannel);
 
 		if (pComChannel == null){
 			Logging.err(this, "Communication channel is invalid.");
 		}
 		
 		if (tControlEntity == null){
 			Logging.err(this, "Control entity reference is invalid");
 		}
 		
 		/***************************
 		 * UPDATE PEER PRIORITY
 		 ***************************/ 
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 			Logging.log(this, "      ..updating peer priority to: " + pPacket.getSenderPriority().getValue());
 		}		
 				
 		boolean tReceivedNewPriority = pComChannel.setPeerPriority(pPacket.getSenderPriority());
 		
 		/***************************
 		 * REACT ON THE MESSAGE
 		 ***************************/
 		if (!tControlEntity.getHierarchyLevel().isHigher(this, mParent.getHierarchyLevel())){
 			/**
 			 * ELECT
 			 */
 			if(pPacket instanceof ElectionElect)	{
 				
 				// cast to an ElectionElect packet
 				ElectionElect tElectionElectPacket = (ElectionElect)pPacket;
 				
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 					Logging.log(this, "BULLY-received from \"" + tControlEntity + "\" an ELECT: " + tElectionElectPacket);
 				}
 	
 				// update the state
 				eventReceivedELECT(pComChannel);
 			}
 			
 			/**
 			 * REPLY
 			 */
 			if(pPacket instanceof ElectionReply) {
 				
 				// cast to an ElectionReply packet
 				ElectionReply tReplyPacket = (ElectionReply)pPacket;
 	
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 					Logging.log(this, "BULLY-received from \"" + tControlEntity + "\" a REPLY: " + tReplyPacket);
 				}
 	
 				eventReceivedREPLY(pComChannel);
 			}
 			
 			/**
 			 * ANNOUNCE
 			 */
 			if(pPacket instanceof ElectionAnnounceWinner)  {
 				// cast to an ElectionAnnounceWinner packet
 				ElectionAnnounceWinner tAnnouncePacket = (ElectionAnnounceWinner)pPacket;
 	
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 					Logging.log(this, "BULLY-received from \"" + tControlEntity + "\" an ANNOUNCE: " + tAnnouncePacket);
 				}
 	
 				eventReceivedANNOUNCE(pComChannel, tAnnouncePacket);
 			}
 	
 			/**
 			 * RESIGN
 			 */
 			if(pPacket instanceof ElectionResignWinner)  {
 				// cast to an ElectionResignWinner packet
 				ElectionResignWinner tResignPacket = (ElectionResignWinner)pPacket;
 	
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 					Logging.log(this, "BULLY-received from \"" + tControlEntity + "\" an RESIGN: " + tResignPacket);
 				}
 	
 				eventReceivedRESIGN(pComChannel, tResignPacket);
 			}
 
 			/**
 			 * PRIORITY UPDATE
 			 */
 			if(pPacket instanceof ElectionPriorityUpdate) {
 				// cast to an ElectionPriorityUpdate packet
 				ElectionPriorityUpdate tElectionPriorityUpdatePacket = (ElectionPriorityUpdate)pPacket;
 	
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 					Logging.log(this, "BULLY-received from \"" + tControlEntity + "\" a PRIORITY UPDATE: " + tElectionPriorityUpdatePacket);
 				}
 				
 				tReceivedNewPriority = eventReceivedPRIORITY_UPDATE(pComChannel);
 			}
 			
 			/**
 			 * LEAVE
 			 */
 			if(pPacket instanceof ElectionLeave) {
 				// cast to an ElectionLeave packet
 				ElectionLeave tLeavePacket = (ElectionLeave)pPacket;
 	
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 					Logging.log(this, "BULLY-received from \"" + tControlEntity + "\" a LEAVE: " + tLeavePacket);
 				}
 	
 				eventReceivedLEAVE(pComChannel, tLeavePacket);
 			}
 			
 			/**
 			 * RETURN
 			 */
 			if(pPacket instanceof ElectionReturn) {
 				// cast to an ElectionReturn packet
 				ElectionReturn tReturnPacket = (ElectionReturn)pPacket;
 	
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 					Logging.log(this, "BULLY-received from \"" + tControlEntity + "\" a RETURN: " + tReturnPacket);
 				}
 	
 				eventReceivedRETURN(pComChannel, tReturnPacket);
 			}
 		}else{
 			Logging.log(this, "HIGHER LEVEL SENT BULLY MESSAGE " + pPacket.getClass().getSimpleName() + " FROM " + pComChannel);
 
 			/**
 			 * ANNOUNCE: a superior coordinator was elected and sends its announce towards its inferior coordinators 
 			 */
 			if(pPacket instanceof ElectionAnnounceWinner)  {
 				// cast to an ElectionAnnounceWinner packet
 				ElectionAnnounceWinner tAnnouncePacket = (ElectionAnnounceWinner)pPacket;
 	
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 					Logging.log(this, "BULLY-received from \"" + tControlEntity + "\" an ANNOUNCE: " + tAnnouncePacket);
 				}
 	
 				if(tControlEntity instanceof Coordinator){
 					Coordinator tCoordinator = (Coordinator)tControlEntity;
 					
 					// trigger: superior coordinator available	
 					tCoordinator.eventClusterCoordinatorAvailable(tAnnouncePacket.getSenderName(), tAnnouncePacket.getCoordinatorID(), pComChannel.getPeerL2Address(), tAnnouncePacket.getCoordinatorDescription());
 				}else{
 					// HINT: this case shouldn't occur since the concept includes such messages only from a higher cluster towards its members (which are coordinators again)
 					Logging.err(this, "EXPECTED COORDINATOR as parent control entity for comm. channel: " + pComChannel);
 				}
 			}else{
 				Logging.log(this, "      ..ignoring Election message: " + pPacket);
 			}
 		}
 
 		/*****************************
 		 * REACT ON THE NEW PEER PRIORITY
 		 *****************************/
 		if(tReceivedNewPriority){
 			leaveReturnOnNewPeerPriority(pComChannel, pPacket);
 		}
 	}
 
 	/**
 	 * This is the central function for comparing two priorities.
 	 * It returns true if the priority of the source is higher than the one of the peer (from the communication channel)
 	 * 
 	 * @param pSourceL2Address the L2Address of the source (to which the priority should be compared to)
 	 * @param pSourcePriority the priority of the source (to which the priority should be compared to)
 	 * @param pComChannelToPeer the communication channel to the peer
 	 * @param pIgnoreLinkState defines if the link state should be ignored
 	 * 
 	 * @return true or false
 	 */
 	private synchronized boolean hasSourceHigherPrioriorityThan(L2Address pSourceL2Address, ElectionPriority pSourcePriority, ComChannel pComChannelToPeer, boolean pIgnoreLinkState)
 	{
 		boolean tResult = false;
 		boolean tDEBUG = false;
 		
 		/**
 		 * Return true if the comm. channel has a deactivated link
 		 */
 		if(HRMConfig.Election.USE_LINK_STATES){
 			if (!pIgnoreLinkState){
 				if (!pComChannelToPeer.isLinkActive()){
 					return true;
 				}
 			}
 		}
 		
 		if (tDEBUG){
 			if(mHRMController.getNodeL2Address().equals(pSourceL2Address)){
 				Logging.log(this, "COMPARING LOCAL PRIORITY with: " + pComChannelToPeer);
 			}else{
 				Logging.log(this, "COMPARING REMOTE PRIORITY of: " + pSourceL2Address + "(" + pSourcePriority.getValue() + ") with: " + pComChannelToPeer);
 			}
 		}
 		
 		/**
 		 * Return false if the comm. channel hasn't received a valid priority yet
 		 */
 		if(pComChannelToPeer.getPeerPriority().isUndefined()){
 			if (tDEBUG){
 				Logging.log(this, "	        ..UNDEFINED PRIORITY value for: " + pComChannelToPeer);
 			}
 			return false;
 		}
 			
 		/**
 		 * Compare the priorities
 		 */
 		if (pSourcePriority.isHigher(this, pComChannelToPeer.getPeerPriority())){
 			if (tDEBUG){
 				Logging.log(this, "	        ..HAVING HIGHER PRIORITY (" + pSourcePriority.getValue() + " > " + pComChannelToPeer.getPeerPriority().getValue() + ") than " + pComChannelToPeer.getPeerL2Address());
 			}
 			
 			tResult = true;
 		}else{
 			/**
 			 * Check if both priorities are equal and the L2Addresses have to be checked
 			 */
 			if (pSourcePriority.equals(pComChannelToPeer.getPeerPriority())){
 				if (tDEBUG){
 					Logging.log(this, "	        ..HAVING SAME PRIORITY like " + pComChannelToPeer.getPeerL2Address());
 				}
 
 				if(pSourceL2Address.isHigher(pComChannelToPeer.getPeerL2Address())) {
 					if (tDEBUG){
 						Logging.log(this, "	        ..HAVING HIGHER L2 address than " + pComChannelToPeer.getPeerL2Address());
 					}
 
 					tResult = true;
 				}else{
 					if (pSourceL2Address.isLower(pComChannelToPeer.getPeerL2Address())){
 						if (tDEBUG){
 							Logging.log(this, "	        ..HAVING LOWER L2 address " + pSourceL2Address + " than " +  pComChannelToPeer.getPeerL2Address());
 						}
 					}else{
 						if (tDEBUG){
 							Logging.log(this, "	        ..DETECTED OWN LOCAL L2 address " + pSourceL2Address);
 						}
 						if(head()){
 							// we are the cluster head and have won the election
 							tResult = true;
 						}else{
 							// we are a ClusterMember and have lost the game
 							tResult = false;
 						}
 					}
 				}
 			}else{
 				if (tDEBUG){
 					Logging.log(this, "	        ..HAVING LOWER PRIORITY (" + pSourcePriority.getValue() + " < " + pComChannelToPeer.getPeerPriority().getValue() + ") than " + pComChannelToPeer.getPeerL2Address());
 				}
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Returns true if the local priority is higher than the one of the peer (from the communication channel)
 	 * 
 	 * @param pComChannelToPeer the comm. channel to the peer
 	 * @param pIgnoreLinkState define if the link state should be ignored
 	 * 
 	 * @return true or false
 	 */
 	private synchronized boolean havingHigherPrioriorityThan(ComChannel pComChannelToPeer, boolean pIgnoreLinkState)
 	{
 		return hasSourceHigherPrioriorityThan(mHRMController.getNodeL2Address(), mParent.getPriority(), pComChannelToPeer, pIgnoreLinkState);
 	}
 
 	/**
 	 * Returns true if the local priority is higher than the one of the peer (from the communication channel)
 	 * 
 	 * @param pComChannelToPeer the comm. channel to the peer
 	 * 
 	 * @return true or false
 	 */
 	private synchronized boolean havingHigherPrioriorityThan(ComChannel pComChannelToPeer)
 	{
 		return hasSourceHigherPrioriorityThan(mHRMController.getNodeL2Address(), mParent.getPriority(), pComChannelToPeer, false);
 	}
 
 	/**
 	 * Returns true if the source priority is higher than the one of the peer
 	 * 
 	 * @param pSourceL2Address the L2Address of the source (to which the priority should be compared to)
 	 * @param pSourcePriority the priority of the source (to which the priority should be compared to)
 	 * 
 	 * @return true or false
 	 */
 	private synchronized boolean hasClusterLowerPriorityThan(L2Address pSourceL2Address, ElectionPriority pSourcePriority)
 	{
 		if(mParent.isThisEntityValid()){
 			LinkedList<ComChannel> tChannels = mParent.getComChannels();
 	
 			if(tChannels.size() == 1){
 				ComChannel tComChannelToPeer = mParent.getComChannels().getFirst();
 					
 				return hasSourceHigherPrioriorityThan(pSourceL2Address, pSourcePriority, tComChannelToPeer, true);
 			}else{
 				Logging.err(this, "hasClusterLowerPriorityThan() found an unplausible amount of comm. channels: " + tChannels);
 			}
 		}else{
 			Logging.warn(this, "hasClusterLowerPriorityThan() skipped for \"" + pSourceL2Address + "\" with prio " + pSourcePriority.getValue() + " because entity is already invalidated");
 		}
 		
 		return false;
 	}
 
 	/**
 	 * SEND: priority update, triggered by ClusterMember when the priority is changed (e.g., if the base node priority was changed)
 	 * 
 	 * @param pCause the cause for this update
 	 */
 	public void updatePriority(String pCause)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS){
 			Logging.log(this, "Updating local priority");
 		}
 		
 		/**
 		 * trigger signaling of "priority update"
 		 */
 		distributePRIRORITY_UPDATE();
 		
 		/**
 		 * check for winner
 		 */
 		if(mState == ElectorState.ELECTING){
 			checkForWinner(this + "::updatePriority()\n   ^^^^" + pCause);
 		}else if(mState == ElectorState.ELECTED){
 			startElection(this + "::updatePriority()\n   ^^^^" + pCause);
 		}
 	}
 
 	/**
 	 * Generates a descriptive string about the object
 	 * 
 	 * @return the descriptive string
 	 */
 	@Override
 	public String toString()
 	{
 		return toLocation() + "@" + mParent.toString() +"[" + mState + ", " + mParentIsActiveMember + "]";
 	}
 
 	/**
 	 * Generates a description of the location of this object instance
 	 * 
 	 * @return the location description
 	 */
 	@Override
 	public String toLocation()
 	{
 		String tResult = null;
 		
 		tResult = getClass().getSimpleName();
 		
 		return tResult;
 	}
 }
