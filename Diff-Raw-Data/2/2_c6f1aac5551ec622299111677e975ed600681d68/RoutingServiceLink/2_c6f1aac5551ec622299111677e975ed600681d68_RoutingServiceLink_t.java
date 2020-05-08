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
 package de.tuilmenau.ics.fog.routing;
 
 import de.tuilmenau.ics.fog.IEventRef;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.transfer.gates.AbstractGate;
 import de.tuilmenau.ics.fog.transfer.gates.GateID;
 import de.tuilmenau.ics.fog.ui.Viewable;
 
 /**
  * Class masks GateID and makes them different objects even if GateIDs are equal.
  * Feature is needed for equal GateIDs at different multiplexers.
  */
 public class RoutingServiceLink
 {
	protected static final String UNNUMBERED_LINK_NAME = "n.a.";
 	public static final Number INFINITE = Double.POSITIVE_INFINITY;
 	public static final Number DEFAULT = 1;
 
 	
 	public RoutingServiceLink(GateID pID, Description pDescription, Number pLinkCost)
 	{
 		mGateID = pID;
 		if(pDescription != null) {
 			mDescr = pDescription.clone();
 		} else {
 			mDescr = null;
 		}
 		mDescr = pDescription;
 		mGateCosts = pLinkCost;
 	}
 	
 	public boolean equals(Object pObj)
 	{
 		if(pObj == null){
 			return false;
 		}
 		
 		if(pObj == this){
 			return true;
 		}
 		
 		if(pObj instanceof GateID){
 			return ((GateID) pObj).equals(mGateID);
 		}
 
 		if (pObj instanceof AbstractGate){
 			return ((AbstractGate) pObj).getGateID().equals(mGateID);
 		}
 		
 		if(pObj instanceof RoutingServiceLink) {
 			if(mGateID != null) {
 				return mGateID.equals(((RoutingServiceLink) pObj).mGateID);
 			}
 		}
 		
 		return false;
 	}
 	
 	public GateID getID()
 	{
 		return mGateID;
 	}
 	
 	public Description getDescription()
 	{
 		return mDescr;
 	}
 	
 	public void setCost(Number pNewCost)
 	{
 		mGateCosts = pNewCost;
 	}
 	
 	public Number getCost()
 	{
 		if(mGateCosts == null) mGateCosts = 1;
 		
 		return mGateCosts;
 	}
 	
 	public boolean hasInfiniteCost()
 	{
 		if(mGateCosts != null) return INFINITE.equals(mGateCosts);
 		else return false;
 	}
 	
 	public void setEvent(IEventRef pTimer)
 	{
 		mTimer = pTimer;
 	}
 	
 	public IEventRef getEvent()
 	{
 		return mTimer;
 	}
 	
 	public String toString()
 	{
 		if(mGateID != null) {
 			if(mGateCosts != null) {
 				return mGateID.toString() +" (c=" + mGateCosts +")";
 			} else {
 				return mGateID.toString();
 			}
 		}
 		else return UNNUMBERED_LINK_NAME;
 	}
 	
 	@Viewable("Gate ID")
 	private GateID mGateID;
 	
 	@Viewable("Description")
 	private Description mDescr;
 	
 	@Viewable("Cost value")
 	private Number mGateCosts;
 	
 	@Viewable("Associated timer")
 	private IEventRef mTimer;
 }
