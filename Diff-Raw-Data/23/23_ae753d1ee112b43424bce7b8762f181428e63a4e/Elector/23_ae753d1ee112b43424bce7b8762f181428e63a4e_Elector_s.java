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
 
 import java.rmi.RemoteException;
 import java.util.ConcurrentModificationException;
 import java.util.LinkedList;
 import java.util.Random;
 
 import de.tuilmenau.ics.fog.exceptions.AuthenticationException;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.RequirementsException;
 import de.tuilmenau.ics.fog.facade.RoutingException;
 import de.tuilmenau.ics.fog.packets.hierarchical.RequestCoordinator;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyAnnounce;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyElect;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMEntity;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMIdentity;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMSignature;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.HierarchyLevel;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.ICluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.Cluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.NeighborCluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.coordination.Coordinator;
 import de.tuilmenau.ics.fog.routing.hierarchical.coordination.CoordinatorCEPChannel;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class is responsible for coordinator elections. It is instantiated per cluster. 
  *
  */
 public class Elector extends Thread implements HRMEntity
 {
 	/**
 	 * Pointer to the parent cluster, which owns this elector
 	 */
 	private Cluster mParentCluster = null;
 	
 	private Boolean mPleaseInterrupt = false;
 	private long TIMEOUT_FOR_PEERS = 1000;
 	private long WAIT_BEFORE_ADDRESS_DISTRIBUTION = 5000;
 	private long TIMEOUT_FOR_ANNOUNCEMENT=5000;
 	
 	private boolean mWillInitiateManager = false;
 	private boolean mLostElection = false;
 	
 	public Elector(Cluster pCluster)
 	{
 		mParentCluster = pCluster;
 	}
 	
 	/**
 	 * Starts an election by signaling BULLY ELECT to all cluster members
 	 */
 	private void signalElectionStart()
 	{
 		Logging.log(this, "SENDELECTIONS()-START, electing cluster is " + mParentCluster);
 		Logging.log(this, "SENDELECTIONS()-CEPs: " + mParentCluster.getParticipatingCEPs().size());
 
 		for(CoordinatorCEPChannel tCEP : mParentCluster.getParticipatingCEPs()) {
 			//TODO: enforce sending of BullyElect in any case
 			if(tCEP.getPeerPriority().isUndefined() && ! tCEP.isEdgeCEP()/* || tCEP.getPeerPriority() > tCluster.getPriority()*/) {
 				Node tNode = mParentCluster.getHRMController().getPhysicalNode();
 				
 				tCEP.sendPacket(new BullyElect(tNode.getCentralFN().getName(), mParentCluster.getBullyPriority()));
 			}
 		}
 		Logging.log(this, "SENDELECTIONS()-END");
 	}
 	
 	/**
 	 * Signals to all cluster members that we are coordinator and creates coordinator instance. 
 	 * @param pCluster the cluster for which the coordinator was elected
 	 */
 	private void signalAndCreateCoordinator(Cluster pCluster)
 	{
 		Random tRandom = new Random(System.currentTimeMillis());
 		HRMController tHRMController = pCluster.getHRMController();
 		Node tNode = tHRMController.getPhysicalNode();
 		int tToken = tRandom.nextInt();
 		
 		pCluster.setToken(tToken);
 		Logging.log(this, "INIT COORDINATOR functions for cluster " + pCluster);
 
 		if(pCluster.getHRMController().getIdentity() == null) {
 			String tName = tNode.getName();
 			HRMIdentity tIdentity= new HRMIdentity(tName);
 			pCluster.getHRMController().setIdentity(tIdentity);
 		}
 		
 
 		/**
 		 * Create signature of cluster
 		 */
 		HRMSignature tSignature = null;
 		try {
 			tSignature = tHRMController.getIdentity().createSignature(tNode.toString(), null, pCluster.getHierarchyLevel());
 		} catch (AuthenticationException tExc) {
 			pCluster.getHRMController().getLogger().err(this, "Unable to create signature for coordinator", tExc);
 		}
 		
 		/**
 		 * Send BULLY ANNOUNCE in order to signal that we are the coordinator
 		 */
 		BullyAnnounce tAnnounce = new BullyAnnounce(tNode.getCentralFN().getName(), pCluster.getBullyPriority(), tSignature, pCluster.getToken());
 		for(CoordinatorCEPChannel tCEP : pCluster.getParticipatingCEPs()) {
 			tAnnounce.addCoveredNode(tCEP.getPeerName());
 		}
 		if(tAnnounce.getCoveredNodes() == null || (tAnnounce.getCoveredNodes() != null && tAnnounce.getCoveredNodes().isEmpty())) {
 			pCluster.getHRMController().getLogger().log(this, "Sending announce that does not cover anyhting");
 		}
 		pCluster.sendClusterBroadcast(tAnnounce, null);
 		Name tAddress = tNode.getRoutingService().getNameFor(tNode.getCentralFN());; 
 		pCluster.setCoordinatorCEP(null, tSignature, tNode.getCentralFN().getName(), (L2Address)tAddress);
 		LinkedList<HRMSignature> tSignatures = tHRMController.getApprovedSignatures();
 		tSignatures.add(tSignature);
 		if(getHierarchLevel().isHigherLevel()) {
 			pCluster.getHRMController().getLogger().log(pCluster, "has the coordinator and will now announce itself");
 			for(ICluster tToAnnounce : pCluster.getNeighbors()) {
 //					List<VirtualNode> tNodesBetween = pCluster.getCoordinator().getClusterMap().getIntermediateNodes(pCluster, tToAnnounce);
 				/*
 				 * OK: Because of the formerly sent 
 				 */
 				if(tToAnnounce instanceof NeighborCluster) {
 					BullyAnnounce tBullyAnnounce = new BullyAnnounce(tNode.getCentralFN().getName(), pCluster.getBullyPriority(), tSignature, pCluster.getToken());
 					for(CoordinatorCEPChannel tCEP: pCluster.getParticipatingCEPs()) {
 						tBullyAnnounce.addCoveredNode(tCEP.getPeerName());
 					}
 					for(CoordinatorCEPChannel tCEP : ((NeighborCluster)tToAnnounce).getAnnouncedCEPs()) {
 						tCEP.sendPacket(tBullyAnnounce);
 					}
 				}
 			}
 		}
 		
 		
 		if(!mPleaseInterrupt) {
 			/*
 			 * synchronized(mPleaseInterrupt) {
 			 *
 				try {
 					mPleaseInterrupt.wait(WAIT_BEFORE_ADDRESS_DISTRIBUTION);
 				} catch (InterruptedException tExc) {
 					Logging.trace(this, "interrupted before address distribution");
 				}
 			}
 			 */
 			
 			
 			Coordinator tElectedCoordinator = new Coordinator(pCluster, pCluster.getHierarchyLevel(), pCluster.getHrmID());
 			int tClusterHierLvl = pCluster.getHierarchyLevel().getValue() + 1;
 			
 			pCluster.setCoordinator(tElectedCoordinator);
 			pCluster.getHRMController().setSourceIntermediateCluster(tElectedCoordinator, pCluster);
 			tElectedCoordinator.setPriority(pCluster.getBullyPriority());
 			pCluster.getHRMController().addCluster(tElectedCoordinator);
 			if(tClusterHierLvl != HRMConfig.Hierarchy.HEIGHT) {
 				// stepwise hierarchy creation
 				Logging.log(this, "Will now wait because hierarchy build up is done stepwise");
 				mWillInitiateManager = true;
 				Logging.log(this, "Reevaluating whether other processes settled");
 				ElectionManager.getElectionManager().reevaluate(pCluster.getHierarchyLevel().getValue());
 				synchronized(this) {
 					try {
 						wait();
 					} catch (InterruptedException tExc) {
 						Logging.err(this, "Unable to fulfill stepwise hierarchy preparation", tExc);
 					}
 				}
 				tElectedCoordinator.prepareAboveCluster(tClusterHierLvl);
 			} else {
 				Logging.log(this, "Beginning address distribution");
 				try {
 					tElectedCoordinator.setHRMID(new HRMID(0));
 					synchronized(mPleaseInterrupt) {
 						Logging.log(this, "ACTIVE WAITING (init) - " + WAIT_BEFORE_ADDRESS_DISTRIBUTION);
 						mPleaseInterrupt.wait(WAIT_BEFORE_ADDRESS_DISTRIBUTION);
 					}
 					tElectedCoordinator.distributeAddresses();
 				} catch (RemoteException tExc) {
 					Logging.err(this, "Remoe problem - error when trying to distribute addresses", tExc);
 				} catch (RoutingException tExc) {
 					Logging.err(this, "Routing problem - error when trying to distribute addresses", tExc);
 				} catch (RequirementsException tExc) {
 					Logging.err(this, "Requirements problem - error when trying to distribute addresses", tExc);
 				} catch (InterruptedException tExc) {
 					Logging.err(this, "interrupt problem - error when trying to distribute addresses", tExc);
 				}
 			}
 		}	
 	}
 	
 	private void checkClustersForHighestPriority()
 	{
 		String tOutput = new String();
 		for(CoordinatorCEPChannel tCEP : mParentCluster.getParticipatingCEPs()) {
 			BullyPriority tPriority = tCEP.getPeerPriority(); 
 			tOutput +=  (tOutput.equals("") ? "" : ", ") +  tPriority;
 			
 			Logging.log(this, "Comparing found Bully priority \"" + tPriority.getValue() + "\" with known highest value \"" + mParentCluster.getHighestPriority().getValue() + "\", found priority belongs to " + tCEP); 
 					
 			if(tPriority.isHigher(this, mParentCluster.getHighestPriority())) {
 				mParentCluster.setHighestPriority(tPriority);
 			} else {
 				Logging.log(this, "Cluster " + mParentCluster + " has priority " + mParentCluster.getBullyPriority().getValue() + ", which is lower than " + tCEP.getPeerPriority().getValue() + " of " + tCEP);
 			}
 		}
 
 		Cluster tNodessClusterForCoordinator = null;
 		
 		Logging.log(this, "COMPARING prio " + mParentCluster.getBullyPriority().getValue() + " and prio " + mParentCluster.getHighestPriority().getValue());
 		
 		if(mParentCluster.getBullyPriority().isHigher(this, mParentCluster.getHighestPriority()))	{
 			tNodessClusterForCoordinator = mParentCluster;
 		}else{
 			if (mParentCluster.getBullyPriority().equals(mParentCluster.getHighestPriority())){
 				Logging.warn(this, "Peers have SAME BULLY PRIORITY");
 				//TODO: rework signaling and calculate random number which has to be communicated to peers!
 				tNodessClusterForCoordinator = mParentCluster;				
 			}
 		}
 	
 		if(tNodessClusterForCoordinator != null) {
 			if(!mPleaseInterrupt) {
 				signalAndCreateCoordinator(tNodessClusterForCoordinator);
 			} else {
 				Logging.err(this, "I had the highest priority, but election was cancelled");
 				restart();
 			}
 		} else {
 			mLostElection = true;
 		}
 	}
 	
 	/**
 	 * 
 	 * @param pSourceCluster is the cluster from which you want the path
 	 * @param pTargetCluster is the cluster
 	 * @return 
 	 */
 	
 	public void run()
 	{
 		try {
 			long tTimeWaitUntil = 0;
 			
 			signalElectionStart();
 			
 			tTimeWaitUntil = System.currentTimeMillis() + TIMEOUT_FOR_PEERS;
 			checkWait(System.currentTimeMillis(), tTimeWaitUntil);
 			Logging.log(this, "Sending elections..");
 			if(!mPleaseInterrupt) {
 				checkClustersForHighestPriority();
 				/*
 				 * initiate new election in case other clusters had higher priority
 				 */
 				tTimeWaitUntil = System.currentTimeMillis() + TIMEOUT_FOR_ANNOUNCEMENT;
 				checkWait(System.currentTimeMillis(), tTimeWaitUntil);
 				if(getHierarchLevel().isHigherLevel()) {
 					/*
 					 * For loop can be ignored as this can only happen in case we are above level one
 					 */
 					while((mParentCluster.getHRMController().getClusterWithCoordinatorOnLevel(mParentCluster.getHierarchyLevel().getValue()) == null)) {
 						mParentCluster.setHighestPriority(mParentCluster.getBullyPriority());
 						Logging.log(mParentCluster, " did not yet receive an announcement");
 						for(CoordinatorCEPChannel tCEP : mParentCluster.getParticipatingCEPs()) {
 							RequestCoordinator tRequest = new RequestCoordinator(/* false */);
 							tCEP.sendPacket(tRequest);
 							synchronized(tRequest) {
 								if(!tRequest.mWasNotified){
 									Logging.log(this, "ACTIVE WAITING (run)");
 									tRequest.wait(10000);
 								}
 								if(!tRequest.mWasNotified) {
 									Logging.log(this, "Was still waiting for " + tRequest);
 									tRequest.wait();
 								}
 							}
 						}
 						/*
 						tTimeWaitUntil = System.currentTimeMillis()+TIMEOUT_FOR_LAGGARDS;
 						checkWait(System.currentTimeMillis(), tTimeWaitUntil);
 						*/
 						try {
 							LinkedList<CoordinatorCEPChannel> tCEPs = new LinkedList<CoordinatorCEPChannel>();
 							tCEPs.addAll(mParentCluster.getParticipatingCEPs());
 							if(mParentCluster.getOldParticipatingCEPs() != null) {
 								tCEPs.addAll(mParentCluster.getOldParticipatingCEPs());
 							}
 							for(CoordinatorCEPChannel tCEP: mParentCluster.getParticipatingCEPs()) {
 								if(! tCEP.knowsCoordinator()) {
 									if(!mParentCluster.getHRMController().checkPathToTargetContainsCovered(mParentCluster.getHRMController().getSourceIntermediate(tCEP.getRemoteClusterName()), tCEP.getRemoteClusterName(), tCEPs)) {
 										Logging.log(mParentCluster, "adding laggard " + tCEP + " while clusters between are " + mParentCluster.getHRMController().getRoutableClusterGraph().getIntermediateNodes(mParentCluster.getHRMController().getSourceIntermediate(tCEP.getRemoteClusterName()), tCEP.getRemoteClusterName()));
 										mParentCluster.addLaggard(tCEP);
 									} else {
 										Logging.info(mParentCluster, "not adding laggard " + tCEP);
 									}
 								} 
 							}
 						} catch (ConcurrentModificationException tExc) {
 							Logging.err(this, "Error when looking for uncovered clusters", tExc);
 						}
 						if(mParentCluster.getLaggards() != null) {
 							((Cluster)mParentCluster).setParticipatingCEPs((LinkedList<CoordinatorCEPChannel>) mParentCluster.getLaggards().clone());
 							mParentCluster.getLaggards().clear();
 						}
 						if(mParentCluster.getHRMController().getClusterWithCoordinatorOnLevel(mParentCluster.getHierarchyLevel().getValue()) == null) {
 							checkClustersForHighestPriority();
 						} else {
 							break;
 						}
 					}
 				}
 			} else {
 				restart();
 			}
 		} catch (Exception tExc) {
 			Logging.warn(this, "Election interrupted", tExc);
 			run();
 		}
 		ElectionManager.getElectionManager().removeElection(mParentCluster.getHierarchyLevel().getValue(), mParentCluster.getClusterID());
 	}
 	
 	private void restart()
 	{
 		mPleaseInterrupt=false;
 		run();
 	}
 	
 	private void checkWait(long pReference, long pCompare)
 	{
 		synchronized(mPleaseInterrupt) {
 			if(pReference >= pCompare || mPleaseInterrupt) {
 				return;
 			} else {
 				long tWaitTime = pCompare-pReference;
 				if(mPleaseInterrupt) {
 					Logging.log(this, "Election was interrupted, not waiting for settlement of peer responses");
 				}
 				if(tWaitTime == 0 || mPleaseInterrupt) return;
 				try	{
 					Logging.log(this, "ACTIVE WAITING (checkWait) - " + tWaitTime);
 					mPleaseInterrupt.wait(tWaitTime);
 				} catch (InterruptedException tExc) {
 					Logging.trace(this, "was interrupted");
 				}
 				checkWait(System.currentTimeMillis(), pCompare);
 			}
 		}
 	}
 	
 	/**
 	 * Determine the parent cluster, which owns this elector. 
 	 * @return the parent cluster
 	 */
 	public Cluster getCluster()
 	{
 		return mParentCluster;
 	}
 	
 	public boolean aboutToContinue()
 	{
 		return mWillInitiateManager || mLostElection;
 	}
 
 	@Override
 	public String toString()
 	{
 		return toLocation() + "(Cluster=" + (mParentCluster != null ? "(Cluster=" + mParentCluster.getClusterID() + ")" : "");
 	}
 
 	@Override
 	public String toLocation()
 	{
		String tResult = getClass().getSimpleName() + "@" + getCluster().getHRMController().getPhysicalNode().getName() + "@" + getCluster().getHierarchyLevel();
 		
 		return tResult;
 	}
 
 	private HierarchyLevel getHierarchLevel()
 	{
 		return mParentCluster.getHierarchyLevel();	
 	}	
 }
