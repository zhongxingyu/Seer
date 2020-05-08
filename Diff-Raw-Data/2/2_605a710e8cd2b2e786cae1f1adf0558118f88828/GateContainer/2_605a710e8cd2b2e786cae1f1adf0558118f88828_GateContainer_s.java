 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.transfer.forwardingNodes;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.CommonSim.datastream.StreamTime;
 import de.tuilmenau.ics.CommonSim.datastream.numeric.IDoubleWriter;
 import de.tuilmenau.ics.CommonSim.datastream.numeric.SumNode;
 import de.tuilmenau.ics.fog.FoGEntity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.transfer.ForwardingElement;
 import de.tuilmenau.ics.fog.transfer.ForwardingNode;
 import de.tuilmenau.ics.fog.transfer.Gate.GateState;
 import de.tuilmenau.ics.fog.transfer.TransferPlaneObserver.NamingLevel;
 import de.tuilmenau.ics.fog.transfer.gates.AbstractGate;
 import de.tuilmenau.ics.fog.transfer.gates.GateID;
 import de.tuilmenau.ics.fog.transfer.gates.GateIterator;
 import de.tuilmenau.ics.fog.util.Helper;
 import de.tuilmenau.ics.fog.util.Logger;
 
 
 /**
  * Provides basic functionality for storing/searching/iterating gate references.
  * It is the base class for forwarding nodes.
  */
 abstract public class GateContainer implements ForwardingNode
 {
 	private HashMap<Integer,AbstractGate> mGates = new HashMap<Integer,AbstractGate>();
 	protected Name mName;
 	protected NamingLevel mLevel;
 	protected FoGEntity mEntity;
 	protected Logger mLogger;
 	
 	/**
 	 * It is static in order to enforce global unique gate IDs.
 	 * In reality this is not needed! The gate numbers need to
 	 * be locally unique only. But for simulation it makes debugging
 	 * much easier.
 	 */
	private static int sLastUsedGateNumber = 0;
 	
 	
 	public GateContainer(FoGEntity pNode, Name pName, NamingLevel pLevel)
 	{
 		mEntity = pNode;
 		mName = pName;
 		mLevel = pLevel;
 		mLogger = pNode.getLogger();
 	}
 	
 	/**
 	 * Initializes forwarding node
 	 */
 	public void open()
 	{
 		mEntity.getTransferPlane().registerNode(this, mName, mLevel, getDescription());
 	}
 	
 	@Override
 	public GateID registerGate(AbstractGate newgate)
 	{
 		return registerGate(newgate, new GateID(getFreeGateNumber()));
 	}
 	
 	/**
 	 * Internal function doing the work for <code>registerGate</code>.
 	 */
 	private GateID registerGate(AbstractGate newgate, GateID gateID)
 	{
 		if(newgate != null) {
 			try {
 				newgate.setID(gateID);
 				mGates.put(gateID.GetID(), newgate);
 				
 				if(mEntity.getTransferPlane() != null){
 					mEntity.getTransferPlane().registerLink(this, newgate);
 				}
 				
 				mEntity.getNode().count(newgate.getClass().getName(), true);
 
 				mLogger.log(this, newgate +" added");
 				return newgate.getGateID();
 			}
 			catch (NetworkException exc) {
 				newgate.setID(null);
 				mGates.remove(gateID.GetID());
 				
 				mLogger.err(this, "Error while adding " +newgate, exc);
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Replaced an existing gate with a new one. The old one is removed and
 	 * the new one is registered with the gate id of the old gate.
 	 *  
 	 * @param oldGate Gate, which should be replaced
 	 * @param byNewGate Gate (!=null), which will replace the old gate
 	 * @return true, if replacement successfully; false otherwise
 	 */
 	@Override
 	public boolean replaceGate(AbstractGate oldGate, AbstractGate byNewGate)
 	{
 		// check method inputs
 		if((byNewGate != null) && (oldGate != null)) {
 			GateID id = oldGate.getGateID();
 			
 			// try to delete old gate
 			if(unregisterGate(oldGate)) {
 				// ok, gate was really attached to this container
 				// and therefore the gate id is valid.
 				if(registerGate(byNewGate, id) == null) {
 					// Should not happen because parameters are valid!
 					// Try to repair previous state and throw an exception.
 					registerGate(oldGate, id);
 					
 					throw new RuntimeException("Unexpected failure during replacing gates at container " +this +".");
 				}
 				
 				return true;
 			}
 		}
 		
 		
 		return false;
 	}
 
 	@Override
 	public boolean unregisterGate(AbstractGate oldgate)
 	{
 		Integer tID = Helper.removeValueFromHashMap(mGates, oldgate);
 		if(tID != null) {
 			if(mEntity.getTransferPlane() != null){
 				mEntity.getTransferPlane().unregisterLink(this, oldgate);
 			}
 			
 			if(oldgate != null) {
 				StreamTime tNow = mEntity.getTimeBase().nowStream();
 				String baseName = mEntity.getClass().getName() +"." +mEntity +"." +oldgate.getClass().getName();
 				
 				IDoubleWriter tSum = SumNode.openAsWriter(baseName +".number");
 				tSum.write(-1.0d, tNow);
 			}
 
 			mLogger.log(this, oldgate +" removed");
 			oldgate.setID(null);
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Unregisters all gates between this Container and a given direct
 	 * neighbor of it.
 	 * 
 	 * @param destination direct neighbor of Container
 	 * @return number of deleted gates
 	 */
 	public int unregisterGatesTo(ForwardingElement destination)
 	{
 		int gates = 0;
 		boolean found = true;
 		
 		// while loop is need because the iterator gets invalid
 		// in the for loop.
 		while(found) {
 			found = false;
 			
 			for(AbstractGate tGate : mGates.values()) {		
 				if(tGate.getNextNode() == destination) {
 					gates++;
 					unregisterGate(tGate);
 					found = true;
 					break;
 				}
 			}
 		}
 		
 		return gates;
 	}
 	
 	/**
 	 * Closes all gates and remove them from the forwarding node.
 	 */
 	public void close()
 	{
 		while(mGates.size() > 0) {
 			AbstractGate tGate = mGates.values().iterator().next();
 			ForwardingElement tNext = tGate.getNextNode();
 			
 			if(!unregisterGate(tGate)) {
 				mLogger.err(this, "Internal error: Existing gate '" +tGate +"' can not be deleted.");
 				Helper.removeValueFromHashMap(mGates, tGate);
 			}
 			
 			// if next FN is a container gate, we have to delete all gates
 			// to the selected FN
 			if(tNext instanceof GateContainer) {
 				((GateContainer) tNext).unregisterGatesTo(this); 
 			}
 		}
 		
 		if(getEntity().getTransferPlane() != null){
 			getEntity().getTransferPlane().unregisterNode(this);
 		}
 	}
 	
 	/**
 	 * Query gate based on gate number.
 	 * 
 	 * @param id number of gate
 	 * @return Reference to gate or null if not defined
 	 */
 	public AbstractGate getGate(GateID id)
 	{
 		if(id == null) return null;
 		
 		return mGates.get(id.GetID());
 	}
 
 	public int getNumberGates()
 	{
 		return mGates.size();
 	}
 	
 	/**
 	 * Searches for gate number of a given gate.
 	 * Comparison will be done based on reference and not on equals method.
 	 * 
 	 * @param gate Reference to gate to search for
 	 * @return Gate number of gate at this multiplexer or null if gate not known
 	 */
 	protected GateID searchForGate(ForwardingElement gate)
 	{
 		for(AbstractGate tGate : mGates.values()) {		
 			if(gate == tGate) {
 				return tGate.getGateID();
 			}
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public GateIterator getIterator(Class<?> requestedGateClass)
 	{
 		return new GateIterator(mGates, requestedGateClass);
 	}
 	
 	/**
 	 * Executes keep-alive for all gates of the container.
 	 * Gates, which can not be kept alive are removed from
 	 * the container.
 	 * 
 	 * @return Number of removed gates, where the keep alive failed.
 	 */
 	public int refresh()
 	{
 		LinkedList<AbstractGate> removeList = null;
 		
 		mLogger.log(this, "Running keep alive.");
 		
 		for(AbstractGate tGate : mGates.values()) {
 			tGate.refresh();
 			
 			if(tGate.getState() == GateState.DELETED) {
 				// Gate no longer valid -> remove it
 				// Do the remove operation later, in order to preserve the
 				// iterator from the for loop.
 				if(removeList == null) removeList = new LinkedList<AbstractGate>();
 				removeList.add(tGate);
 			}
 		}
 		
 		// doing the removal of gates
 		if(removeList != null) {
 			for(AbstractGate tGate : removeList) {
 				unregisterGate(tGate);
 			}
 			
 			return removeList.size();
 		} else {
 			return 0;
 		}
 	}
 	
 	/**
 	 * TODO remove since FN does not have a name
 	 */
 	@Deprecated
 	public Name getName()
 	{
 		return mName;
 	}
 	
 	@Override
 	public FoGEntity getEntity()
 	{
 		return mEntity;
 	}
 	
 	@Override
 	public String toString()
 	{
 		if(mName != null) return this.getClass().getSimpleName() +"(" +mName +")@" +mEntity;
 		else return super.toString();
 	}
 	
 	@Override
 	public int getFreeGateNumber()
 	{
 		synchronized(GateContainer.class) {
 			sLastUsedGateNumber++;
 			return sLastUsedGateNumber;
 		}
 	}
 }
