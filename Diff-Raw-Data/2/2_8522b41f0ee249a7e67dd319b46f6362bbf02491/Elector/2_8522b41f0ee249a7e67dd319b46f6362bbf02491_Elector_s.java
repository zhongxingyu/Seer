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
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyAlive;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyAnnounce;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyElect;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyLeave;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyPriorityUpdate;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyReply;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.SignalingMessageBully;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.Localization;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.Cluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ClusterMember;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ComChannel;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ControlEntity;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.Coordinator;
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
 
 	//TODO: rckkehr von ELECTED zu ELECTING, wenn BullyAlive von koordinator ausbleibt
 	
 	/** 
 	 * Stores the internal state of the elector
 	 */
 	private ElectorState mState;
 	
 	/**
 	 * Pointer to the parent cluster, which owns this elector
 	 */
 	private ClusterMember mParent = null;
 
 	/**
 	 * The timeout for an awaited BullyAlive message (in s).
 	 */
 	private long TIMEOUT_FOR_REPLY = 25;
 	/**
 	 * The timeout for an awaited BullyAlive message (in s).
 	 */
 	private long TIMEOUT_FOR_ALIVE = 25;
 
 	/**
 	 * The time period between two BullyAlive messages (in s).
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
 	 * Stores the timestamp of the last ElectBroadcast signaling
 	 */
 	private Double mTimestampLastElectBroadcast =  new Double(0);
 	
 	public Elector(HRMController pHRMController, ClusterMember pCluster)
 	{
 		mState = ElectorState.START;
 		mParent = pCluster;
 		mElectionWon = false;
 		mHRMController = pHRMController;
 		
 		// set IDLE state
 		setElectorState(ElectorState.IDLE);
 	}
 	
 	/**
 	 * Elects the coordinator for this cluster.
 	 */
 	private void elect()
 	{
 		// set correct elector state
 		setElectorState(ElectorState.ELECTING);
 
 		Logging.log(this, "ELECTING now...");
 		
 		if(mParent instanceof Cluster){
 			// do we know more than 0 external cluster members?
 			if (mParent.countConnectedRemoteClusterMembers() > 0){
 				Logging.log(this, "Trying to ask " + mParent.countConnectedRemoteClusterMembers() + " external cluster members for their Bully priority");
 				signalElectBroadcast();
 			}else{
 				// we don'T have external members - but do we have local members?
 				if(mParent.countConnectedClusterMembers() > 0){					
 					/**
 					 * Send a priority update to all local cluster members
 					 */
 					signalBullyPriorityUpdate();
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
 	 * EVENT: detected isolation 
 	 */
 	private void eventDetectedIsolation()
 	{
 		Logging.log(this, "EVENT: isolation");
 		
 		Logging.log(this, "I AM WINNER because no alternative cluster member is known, known cluster channels:" );
 		Logging.log(this, "    ..: " + mParent.getComChannels());
 		eventElectionWon();
 	}
 	
 	/**
 	 * Restarts the election process for this cluster
 	 */
 	private void reelect()
 	{
 		if (mParent instanceof Cluster){
 			//reset ELECT BROADCAST timer
 			mTimestampLastElectBroadcast = new Double(0);
 			
 			Logging.log(this, "REELECTION");
 			elect();
 		}else{
 			Logging.log(this, "Reelection needed but we aren't the cluster head, we hope that the other local Cluster object will trigger a reelection" );
 		}
 	}
 	
 	/**
 	 * Starts the election process. This function is usually called by the GUI.
 	 */
 	public void startElection()
 	{
 		Logging.log(this, "#### STARTING ELECTION");
 		
 		// is the parent the cluster head?
 		if(mParent instanceof ClusterMember){
 			ClusterMember tParentClusterMember = (ClusterMember)mParent;
 			
 			// was the cluster already locally registered as neighbor? 
 			if (tParentClusterMember.isNeighborHoodInitialized()){
 				switch(mState){
 					case IDLE:
 						elect();
 						break;
 					case ELECTED:
 						if (isCoordinatorValid()){
 							Logging.log(this, "RESTARTING ELECTION, old coordinator was valid: " + isCoordinatorValid());
 							reelect();
 						}
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
 				Logging.err(this, "Neighborhood of cluster " + mParent + " has to be already initialized when calling startElection()");
 				
 				setElectorState(ElectorState.ERROR);
 			}
 		}else{
 			Logging.warn(this, "We skipped election start because parent isn't a cluster member: " + mParent);
 		}
 	}
 	
 	/**
 	 * Returns true if the election process was already started
 	 *  
 	 * @return true or false
 	 */
 	public boolean wasStarted()
 	{
 		return (mState != ElectorState.IDLE);
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
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY){
 					Logging.log(this, "STATE TRANSITION from " + mState + " to " + pNewState);
 				}
 	
 				// set new state
 				mState = pNewState;
 			}
 		} else {
 			throw new RuntimeException(toLocation() + "-cannot change its state from " + mState +" to " + pNewState);
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
 	 * Determines if the coordinator for this election domain (=cluster) is (still) valid-
 	 * 
 	 * @return true if valid, otherwise false
 	 */
 	public boolean isCoordinatorValid()
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
 				
 		Logging.log(this, "Checking timing of ELECT BROADCAST: last=" + mTimestampLastElectBroadcast.longValue() + ", MinPeriod=" + TIMEOUT_FOR_REPLY + ", now=" + tNow + ", MinTime=" + tTimeout);
 		
 		// is timing okay?
 		if ((mTimestampLastElectBroadcast.doubleValue() == 0) || (tNow > mTimestampLastElectBroadcast.doubleValue() + TIMEOUT_FOR_REPLY)){
 			tResult = true;
 			mTimestampLastElectBroadcast = new Double(tNow);
 			
 			Logging.log(this, "     ..ELECT BROADCAST is okay");
 		}else{
 			Logging.log(this, "     ..ELECT BROADCAST is skipped due to timer");
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * SIGNAL: start the election by signaling BULLY ELECT to all cluster members
 	 */
 	private void signalElectBroadcast()
 	{
 		if (mState == ElectorState.ELECTING){
 			if (isTimingOkayOfElectBroadcast()){
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY){
 					Logging.log(this, "SENDELECTIONS()-START, electing cluster is " + mParent);
 					Logging.log(this, "SENDELECTIONS(), external cluster members: " + mParent.countConnectedRemoteClusterMembers());
 				}
 		
 				// create the packet
 				BullyElect tPacketBullyElect = new BullyElect(mHRMController.getNodeName(), mParent.getPriority());
 				
 				// HINT: we send a broadcast to all cluster members, the common Bully algorithm sends this message only to alternative candidates which have a higher priority				
 				mParent.sendClusterBroadcast(tPacketBullyElect);
 				
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY){
 					Logging.log(this, "SENDELECTIONS()-END");
 				}
 			}else{
 				Logging.warn(this, "signalElectBroadcast() was triggered too frequently, timeout isn't reached yet, skipping this action");
 			}
 
 		}else{
 			Logging.warn(this, "Election has wrong state " + mState + " for signaling an ELECTION START, ELECTING expected");
 
 			// set correct elector state
 			setElectorState(ElectorState.ERROR);
 		}			
 	}
 
 	/**
 	 * SIGNAL: ends the election by signaling BULLY ANNOUNCE to all cluster members 		
 	 */
 	private void signalAnnounceBroadcast()
 	{
 		if (mState == ElectorState.ELECTED){
 			// get the size of the cluster
 			int tKnownClusterMembers = mParent.countConnectedClusterMembers();
 			
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY){
 				Logging.log(this, "SENDANNOUNCE()-START, electing cluster is " + mParent);
 				Logging.log(this, "SENDANNOUNCE(), cluster members: " + tKnownClusterMembers);
 			}
 	
 			// HINT: the coordinator has to be already created here
 
 			if (mParent.getCoordinator() != null){
 				// create the packet
 				BullyAnnounce tPacketBullyAnnounce = new BullyAnnounce(mHRMController.getNodeName(), mParent.getPriority(), mParent.getCoordinator().getCoordinatorID(), mParent.getCoordinator().toLocation() + "@" + HRMController.getHostName());
 		
 				// send broadcast
 				mParent.sendClusterBroadcast(tPacketBullyAnnounce, true);
 			}else{
 				Logging.warn(this, "Election has wrong state " + mState + " for signaling an ELECTION END, ELECTED expected");
 				
 				// set correct elector state
 				setElectorState(ElectorState.ERROR);
 			}
 	
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY){
 				Logging.log(this, "SENDANNOUNCE()-END");
 			}
 		}else{
 			// elector state is ELECTED
 			Logging.warn(this, "Election state isn't ELECTING, we cannot finishe an election which wasn't started yet, error in state machine");
 		}			
 	}
 	
 	/**
 	 * SIGNAL: BullyPriorityUpdate
 	 */
 	private void signalBullyPriorityUpdate()
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY){
 			Logging.log(this, "SENDPRIOUPDATE()-START, electing cluster is " + mParent);
 			Logging.log(this, "SENDPRIOUPDATE(), cluster members: " + mParent.getComChannels().size());
 		}
 
 		BullyPriorityUpdate tBullyPriorityUpdatePacket = new BullyPriorityUpdate(mHRMController.getNodeName(), mParent.getPriority());
 
 		// send broadcast
 		mParent.sendClusterBroadcast(tBullyPriorityUpdatePacket, true);
 
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY){
 			Logging.log(this, "SENDPRIOUPDATE()-END");
 		}
 	}
 
 	/**
 	 * SIGNAL: report itself as alive by signaling BULLY ALIVE to all cluster members
 	 */
 	private void signalAliveBroadcast()
 	{
 		if (HRMConfig.Election.SEND_BULLY_ALIVES){
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY){
 				Logging.log(this, "SENDALIVE()-START, electing cluster is " + mParent);
 				Logging.log(this, "SENDALIVE(), cluster members: " + mParent.getComChannels().size());
 			}
 	
 			// create the packet
 			BullyAlive tPacketBullyAlive = new BullyAlive(mHRMController.getNodeName(), mParent.getPriority());
 	
 			// send broadcast
 			mParent.sendClusterBroadcast(tPacketBullyAlive);
 	
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY){
 				Logging.log(this, "SENDALIVE()-END");
 			}
 		}else{
 			// BullyAlive messages are currently deactivated
 		}			
 	}
 
 	/**
 	 * SIGNAL: report itself as alive by signaling BULLY ALIVE to all cluster members
 	 * 
 	 * @param pComChannel the communication channel along which the RESPONSE should be send
 	 */
 	private void signalResponse(ComChannel pComChannel)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY){
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY){
 				Logging.log(this, "SENDRESPONSE()-START, electing cluster is " + mParent);
 				Logging.log(this, "SENDRESPONSE(), cluster members: " + mParent.getComChannels().size());
 			}
 		}
 
 		// create REPLY packet
 		BullyReply tReplyPacket = new BullyReply(mHRMController.getNodeName(), pComChannel.getPeerHRMID(), mParent.getPriority());
 			
 		// send the answer packet
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY)
 			Logging.log(this, "BULLY-sending to \"" + pComChannel + "\" a REPLY: " + tReplyPacket);
 
 		// send message
 		pComChannel.sendPacket(tReplyPacket);
 
 		Logging.log(this, "SENDRESPONSE()-END");
 	}
 
 	
 	/**
 	 * EVENT: sets the local node as coordinator for the parent cluster. 
 	 */
 	private void eventElectionWon()
 	{
 		if ((!isWinner()) || (!isCoordinatorValid())){
 			Logging.log(this, "ELECTION WON for cluster " + mParent);
 			
 			// mark as election winner
 			mElectionWon = true;
 			
 			// set correct elector state
 			setElectorState(ElectorState.ELECTED);
 	
 			// is the parent the cluster head?
 			if(mParent instanceof Cluster){
 				// get the coordinator from the parental cluster
 				Coordinator tCoordinator = mParent.getCoordinator();
 				if (tCoordinator == null){
 					Cluster tParentCluster = (Cluster)mParent;
 					
 					Logging.log(this, "    ..creating new coordinator at hierarch level: " + mParent.getHierarchyLevel().getValue());
 					
 					// create new coordinator instance
 					tCoordinator = new Coordinator(tParentCluster);
 				}else{
 					Logging.warn(this, "Cluster " + mParent + " has already a coordinator");
 				}
 	
 				Logging.log(this, "    ..coordinator is: " + tCoordinator);
 				
 				if(tCoordinator != null){
 					// send BULLY ANNOUNCE in order to signal all cluster members that we are the coordinator
 					signalAnnounceBroadcast();
 		
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
 	 */
 	private void eventElectionLost()
 	{
 		Logging.log(this, "ELECTION LOST for cluster " + mParent);
 	
 		// store the old election result
 		boolean tWasFormerWinner = mElectionWon;
 		
 		// mark as election loser
 		mElectionWon = false;
 		
 		// set correct elector state
 		setElectorState(ElectorState.ELECTED);
 		
 		// have we been the former winner of this election?
 		if (tWasFormerWinner){
 			Logging.log(this, "ELECTION LOST BUT WE WERE THE FORMER WINNER");
 
 			/**
 			 * TRIGGER: invalidate the local coordinator because it was deselected by another coordinator
 			 */
 			if(mParent instanceof Cluster){
 				if (mParent.getCoordinator() != null){
 					mParent.getCoordinator().eventCoordinatorRoleInvalid();
 				}else{
 					Logging.err(this, "We were the former winner of the election but the coordinator is invalid");
 				}
 			}else{
 				// we are not the cluster header, so we can't be the coordinator
 			}
 		}
 	}
 
 	/**
 	 * EVENT: priority update, triggered by ClusterMember when the priority is changed (e.g., if the base node priority was changed)
 	 */
 	public void eventPriorityUpdate()
 	{
 		Logging.log(this, "EVENT: priority update");
 		
 		/**
 		 * trigger signaling of "priority update"
 		 */
 		signalBullyPriorityUpdate();
 		
 		/**
 		 * check for winner
 		 */
 		if(mState == ElectorState.ELECTING){
 			checkForWinner();
 		}
 	}
 	
 	/**
 	 * EVENT: a candidate left the election process
 	 * 
 	 * @param pComChannel the communication channel to the cluster member which left the election
 	 */
 	private void eventElectionLeft(ComChannel pComChannel)
 	{
 		Logging.log(this, "EVENT: cluster member left, comm. channel was: " + pComChannel);
 
 		/**
 		 * TRIGGER: all cluster members are gone, we destroy the cluster
 		 */
 		mParent.eventClusterMemberLost(pComChannel);
 	}
 	
 	/**
 	 * EVENT: the election process was triggered by another cluster member
 	 */
 	private void eventReceivedElect()
 	{
 		Logging.log(this, "EVENT: received ELECT");
 		
 		// set correct elector state
 		setElectorState(ElectorState.ELECTING);
 	}
 	
 	/**
 	 * EVENT: another cluster member has sent its Bully priority
 	 * 
 	 * @param pSourceComChannel the source comm. channel 
 	 */
 	private void eventReceivedReply(ComChannel pSourceComChannel)
 	{
 		Logging.log(this, "EVENT: received REPLY");
 
 		/**
 		 * check for a winner
 		 */
 		if(mState == ElectorState.ELECTING){
 			checkForWinner();
 		}else{
 			/**
 			 *  we received a delayed reply, this can happen if:
 			 *      0.) we send an ELECT to all peers
 			 *      1.) we receive BullyPriorityUpdates from all peers
 			 *        ==> we know the priority of all peers
 			 *        ==> we have the highest priority
 			 *        ==> we decide to be the winner
 			 *      2.) a peer answers a former ELECT
 			 */
 			Logging.warn(this, "Received delayed REPLY via: " + pSourceComChannel);
 		}
 	}
 	
 	/**
 	 * Checks for a winner
 	 */
 	private void checkForWinner()
 	{
 		BullyPriority tHighestPrio = null;
 		ComChannel tExternalWinner = null;
 		boolean tIsWinner = false;
 		boolean tElectionComplete = true;
 		
 		if(mState == ElectorState.ELECTING){
 			// do we know more than 0 external cluster members?
 			if (mParent.countConnectedRemoteClusterMembers() > 0){
 				/**
 				 * Find the highest priority of all external cluster members
 				 */
 				Logging.log(this, "Searching for highest priority...");
 				for(ComChannel tComChannel : mParent.getComChannels()) {
 					BullyPriority tPriority = tComChannel.getPeerPriority(); 
 					
 					/**
 					 * are we still waiting for the Bully priority of some cluster member?
 					 */
 					if ((tPriority == null) || (tPriority.isUndefined())){
 						// election is incomplete
 						tElectionComplete = false;
 					
 						// leave the loop because we already known that the election is incomplete
 						break;
 					}
 					
 					Logging.log(this, "		..cluster member " + tComChannel + " has priority " + tPriority.getValue()); 
 					
 					/**
 					 * find the highest priority in the cluster
 					 */
 					if((tHighestPrio == null) || (tPriority.isHigher(this, tHighestPrio))) {
 						tHighestPrio = tPriority;
 						tExternalWinner = tComChannel;
 					}
 				}
 				
 				/**
 				 * Check if election is complete
 				 */
 				if (tElectionComplete){
 					/**
 					 * Is the local priority higher?
 					 */
 					tIsWinner = havingHigherPrioriorityThan(tExternalWinner);
 
 					/**
 					 * React on the result
 					 */
 					if(tIsWinner) {
 						Logging.log(this, "	        ..I AM WINNER");
 						eventElectionWon();
 					}else{
 						if (tExternalWinner != null){
 							Logging.log(this, "	        ..seeing " + tExternalWinner.getPeerL2Address() + " as election winner");
 						}else{
 							Logging.err(this, "External winner is unknown but also I am not the winner");
 						}
 						eventElectionLost();
 					}
 				}else{
 					// election is incomplete: we are still waiting for some priority value(s)
 				}
 			}else{
 				/**
 				 * trigger "detected isolation"
 				 */
 				eventDetectedIsolation();
 			}
 		}else{
 			Logging.err(this, "checkForWinner() EXPECTED STATE \"ELECTING\" here but got state: " + mState.toString());
 		}
 	}
 	
 	/**
 	 * Handles a Bully signaling packet
 	 * 
 	 * @param pPacketBully the packet
 	 * @param pComChannel the communication channel from where the message was received
 	 */
 	@SuppressWarnings("unused")
 	public void handleSignalingMessageBully(SignalingMessageBully pPacketBully, ComChannel pComChannel)
 	{
 		Node tNode = mHRMController.getNode();
 		Name tLocalNodeName = mHRMController.getNodeName(); 
 		ControlEntity tControlEntity = pComChannel.getParent();
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY)
 			Logging.log(this, "RECEIVED BULLY MESSAGE " + pPacketBully.getClass().getSimpleName() + " FROM " + pComChannel);
 
 		if (pComChannel == null){
 			Logging.err(this, "Communication channel is invalid.");
 		}
 		
 		if (tControlEntity == null){
 			Logging.err(this, "Control entity reference is invalid");
 		}
 		
 		// update the stored Bully priority of the cluster member
 		Logging.log(this, "      ..updating peer priority to: " + pPacketBully.getSenderPriority().getValue());
 		pComChannel.setPeerPriority(pPacketBully.getSenderPriority());		
 
 		if (!tControlEntity.getHierarchyLevel().isHigher(this, mParent.getHierarchyLevel())){
 			/**
 			 * ELECT
 			 */
 			if(pPacketBully instanceof BullyElect)	{
 				
 				// cast to Bully elect packet
 				BullyElect tPacketBullyElect = (BullyElect)pPacketBully;
 				
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY){
 					Logging.log(this, "BULLY-received from \"" + tControlEntity + "\" an ELECT: " + tPacketBullyElect);
 				}
 	
 				// update the state
 				eventReceivedElect();
 			
 				// answer the "elect" message
 				signalResponse(pComChannel);
 					
 				/**
 				 * do we have a higher priority than the peer?
 				 */
 				if (havingHigherPrioriorityThan(pComChannel)){
 					// start re-election
 					reelect();
 				}
 			}
 			
 			/**
 			 * REPLY
 			 */
 			if(pPacketBully instanceof BullyReply) {
 				
 				// cast to Bully replay packet
 				BullyReply tReplyPacket = (BullyReply)pPacketBully;
 	
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY){
 					Logging.log(this, "BULLY-received from \"" + tControlEntity + "\" a REPLY: " + tReplyPacket);
 				}
 	
 				eventReceivedReply(pComChannel);
 			}
 			
 			/**
 			 * ANNOUNCE
 			 */
 			if(pPacketBully instanceof BullyAnnounce)  {
 				// cast to Bully replay packet
 				BullyAnnounce tAnnouncePacket = (BullyAnnounce)pPacketBully;
 	
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY){
 					Logging.log(this, "BULLY-received from \"" + tControlEntity + "\" an ANNOUNCE: " + tAnnouncePacket);
 				}
 	
 				eventElectionLost();
 
 				// trigger: superior coordinator available	
 				tControlEntity.eventClusterCoordinatorAvailable(pComChannel, tAnnouncePacket.getSenderName(), tAnnouncePacket.getCoordinatorID(), pComChannel.getPeerL2Address(), tAnnouncePacket.getCoordinatorDescription());
 			}
 	
 			/**
 			 * PRIORITY UPDATE
 			 */
 			if(pPacketBully instanceof BullyPriorityUpdate) { //TODO: paket muss auch gesendet werden
 				// cast to Bully replay packet
 				BullyPriorityUpdate tPacketBullyPriorityUpdate = (BullyPriorityUpdate)pPacketBully;
 	
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY){
 					Logging.log(this, "BULLY-received from \"" + tControlEntity + "\" a PRIORITY UPDATE: " + tPacketBullyPriorityUpdate);
 				}
 				
 				eventReceivedPriorityUpdate(pComChannel);
 			}
 			
 			/**
 			 * LEAVE
 			 */
 			if(pPacketBully instanceof BullyLeave) {
 				// cast to Bully leave packet
 				BullyLeave tLeavePacket = (BullyLeave)pPacketBully;
 	
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY){
 					Logging.log(this, "BULLY-received from \"" + tControlEntity + "\" a LEAVE: " + tLeavePacket);
 				}
 	
 				eventElectionLeft(pComChannel);
 			}
 		}else{
 			Logging.log(this, "HIGHER LEVEL SENT BULLY MESSAGE " + pPacketBully.getClass().getSimpleName() + " FROM " + pComChannel);
 
 			/**
 			 * ANNOUNCE: a superior coordinator was elected and sends its announce towards its inferior coordinators 
 			 */
 			if(pPacketBully instanceof BullyAnnounce)  {
 				// cast to Bully replay packet
 				BullyAnnounce tAnnouncePacket = (BullyAnnounce)pPacketBully;
 	
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY){
 					Logging.log(this, "BULLY-received from \"" + tControlEntity + "\" an ANNOUNCE: " + tAnnouncePacket);
 				}
 	
 				if(tControlEntity instanceof Coordinator){
 					Coordinator tCoordinator = (Coordinator)tControlEntity;
 					
 					// trigger: superior coordinator available	
 					tCoordinator.eventClusterCoordinatorAvailable(pComChannel, tAnnouncePacket.getSenderName(), tAnnouncePacket.getCoordinatorID(), pComChannel.getPeerL2Address(), tAnnouncePacket.getCoordinatorDescription());
 				}else{
 					// HINT: this case shouldn't occur since the concept includes such messages only from a higher cluster towards its members (which are coordinators again)
 					Logging.err(this, "EXPECTED COORDINATOR as parent control entity for comm. channel: " + pComChannel);
 				}
 			}else{
 				Logging.log(this, "      ..ignoring Bully message: " + pPacketBully);
 			}
 		}
 
 	}
 
 	/**
 	 * @param pComChannel
 	 * @param pSenderPriority
 	 */
 	private void eventReceivedPriorityUpdate(ComChannel pComChannel)
 	{
 		// get the priority of the sender
 		BullyPriority tSenderPriority = pComChannel.getPeerPriority();
 		
 		Logging.log(this, "Got priority " + tSenderPriority.getValue() + " via comm. channel: " + pComChannel);
 
 		// do we have the higher priority?
 		if (havingHigherPrioriorityThan(pComChannel)){
 			Logging.log(this, "Received remote priority " + tSenderPriority.getValue() + " is lower than local " + mParent.getPriority().getValue());
 		}else{
 			/**
 			 * Trigger: new election round if we are the current winner
 			 */
 			if(isWinner()){
 				Logging.log(this, "Received remote priority " + tSenderPriority.getValue() + " is higher than local " + mParent.getPriority().getValue() + ", triggering re-election");
 				startElection();
 			}
 		}
 	}
 
 	/**
 	 * Returns true if the local priority is higher than the one of the peer (from the communication channel)
 	 * 
 	 * @param pComChannel the communication channel whose peer is interesting
 	 * 
 	 * @return true or false
 	 */
 	private boolean havingHigherPrioriorityThan(ComChannel pComChannel)
 	{
 		boolean tResult = false;
 
 		if (mParent.getPriority().isHigher(this, pComChannel.getPeerPriority())){
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY){
 				Logging.log(this, "	        ..HAVING HIGHER PRIORITY than " + pComChannel.getPeerL2Address());
 			}
 			
 			tResult = true;
 		}else{
 			if (mParent.getPriority().equals(pComChannel.getPeerPriority())){
 				Logging.log(this, "	        ..HAVING SAME PRIORITY like " + pComChannel.getPeerL2Address());
 
 				if(mHRMController.getNodeL2Address().isHigher(pComChannel.getPeerL2Address())) {
 					Logging.log(this, "	        ..HAVING HIGHER L2 address than " + pComChannel.getPeerL2Address());
 
 					tResult = true;
 				}else{
 					if (mHRMController.getNodeL2Address().isLower(pComChannel.getPeerL2Address())){
 						Logging.log(this, "	        ..HAVING LOWER L2 address " + mHRMController.getNodeL2Address() + " than " +  pComChannel.getPeerL2Address());
 					}else{
 						Logging.log(this, "	        ..DETECTED OWN LOCAL L2 address " + mHRMController.getNodeL2Address());
 						if(mParent instanceof Cluster){
 							// we are the cluster head and have won the election
 							tResult = true;
 						}else{
 							// we are a ClusterMember and have lost the game
 							tResult = false;
 						}
 					}
 				}
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Generates a descriptive string about the object
 	 * 
 	 * @return the descriptive string
 	 */
 	@Override
 	public String toString()
 	{
		return toLocation() + "@" + mParent.toString();
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
