 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical.properties;
 
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.facade.properties.AbstractProperty;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingEntry;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This property is used for HRM based application data routing.
  * For HRM internal routing, this property should NOT be used.
  */
 public class HRMRoutingProperty extends AbstractProperty
 {
 	private static final long serialVersionUID = 5927732011559094117L;
 	
 	/**
 	 * Time to route: recorded hop count
 	 * This value is not part of the concept. It is only used for debugging purposes and logging measurements. 
 	 */
 	private long mTTR = HRMConfig.Routing.MAX_HOP_COUNT;
 
 	/**
 	 * Recorded route
 	 * This value is not part of the concept. It is only used for debugging purposes and logging measurements. 
 	 */
 	private LinkedList<HRMID> mRecordedRoute = new LinkedList<HRMID>();
 
 	/**
 	 * Recorded delay
 	 * This value is not part of the concept. It is only used for debugging purposes and logging measurements. 
 	 */
 	private long mRecordedDelay = 0;
 
 	/**
 	 * Recorded max. data rate, which is theoretically possible along the taken route
 	 * This value is not part of the concept. It is only used for debugging purposes and logging measurements. 
 	 */
 	private long mRecordedMaxDataRate = RoutingEntry.INFINITE_DATARATE;
 	
 	/**
 	 * Recorded max. data rate, which is assigned to this flow along the taken route
 	 * This value is not part of the concept. It is only used for debugging purposes and logging measurements. 
 	 */
 	private long mRecordedDataRate = -1;
 
 	/**
 	 * The destination HRMID
 	 * This value is not part of the concept. It is only used for debugging purposes and logging measurements. 
 	 */
 	private HRMID mDestination = null;
 
 	/**
 	 * Stores the description of the source of the corresponding packet/connection
 	 * This value is not part of the concept. It is only used for debugging purposes and logging measurements. 
 	 */
 	private String mSourceDescription = null;
 
 	/**
 	 * The desired delay from the sending application
 	 */
 	private long mDesiredDelay = 0;
 	
 	/**
 	 * The desired data rate from the sending application
 	 */
 	private long mDesiredDataRate = 0;
 	
 	/**
 	 * The HRMID of the last hop
 	 */
 	private HRMID mLastHopHRMID = null;
 	
 	/**
 	 * The L2address of the last hop
 	 */
 	private L2Address mLastHopL2Address = null;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param pSource the description of the source of the corresponding packet/connection
 	 * @param pDesiredMaxDelay the desired delay limit
 	 * @param pDesiredMinDataRate the desired data rate limit
 	 */
 	public HRMRoutingProperty(String pSourceDescription, HRMID pDestination, long pDesiredMaxDelay, long pDesiredMinDataRate)
 	{
 		mSourceDescription = pSourceDescription;
 		mDestination = pDestination;
 		mDesiredDataRate = pDesiredMinDataRate;
 		mDesiredDelay = pDesiredMaxDelay;
 	}
 	
 	/**
 	 * Returns the description of the sender
 	 *  
 	 * @return the description
 	 */
 	public String getSourceDescription()
 	{
 		return mSourceDescription;
 	}
 	
 	/**
 	 * Adds a hop(its HRMID) to the recorded route
 	 * 
 	 * @param pHRMID the HRMID of the recorded hop
 	 */
 	public void addRecordedHop(HRMID pHRMID)
 	{
 		synchronized (mRecordedRoute) {
 			mRecordedRoute.add(pHRMID);
 			if(mRecordedRoute.size() > HRMConfig.Routing.MAX_HOP_COUNT){
 				Logging.err(this, "The max. hop count " + HRMConfig.Routing.MAX_HOP_COUNT + " was reached at: " + mRecordedRoute.size() + ", passed:");
 				for(HRMID tHRMID : mRecordedRoute){
 					Logging.log(this, "   .." + tHRMID);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Returns the last hop(its HRMID) of the recorded route
 	 * 
 	 * @param pHRMID the HRMID of the last recorded hop
 	 */
 	public HRMID getLastRecordedHop()
 	{
 		if (mRecordedRoute.size() > 0){
 			return mRecordedRoute.getLast();
 		}else{
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the HRMID of the last hop
 	 * 
 	 * @param pHRMID the HRMID of the last hop
 	 */
 	public HRMID getLastHopHRMID()
 	{
 		return mLastHopHRMID;
 	}	
 	
 	/**
 	 * Returns the L2Address of the last hop
 	 * 
 	 * @param pHRMID the L2Address of the last hop
 	 */
 	public L2Address getLastHopL2Address()
 	{
 		return mLastHopL2Address;
 	}
 	
 	/**
 	 * Increases the recorded hop count 
 	 */
 	public void incHopCount(HRMID pNewLastHopHRMID, L2Address pNewLastHopL2Address)
 	{
 		mTTR--;
 		mLastHopHRMID = pNewLastHopHRMID;
 		mLastHopL2Address = pNewLastHopL2Address;
 	}
 
 	/**
 	 * Returns true if the TTR is still okay
 	 * 
 	 * @return true or false
 	 */
 	public boolean isTTROkay()
 	{
 		/**
 		 * Return true depending on the TTR value
 		 */
 		return (mTTR > 0);
 	}
 	
 	/**
 	 * Adds delay to the recorded delay value
 	 * 
 	 * @param pAdditionalDelay the additional delay
 	 */
 	public void recordAdditionalDelay(long pAdditionalDelay)
 	{
 		if(pAdditionalDelay > 0){
 			mRecordedDelay += pAdditionalDelay;
 		}
 	}
 
 	/**
 	 * Records the data rate
 	 * 
 	 * @param pMaxAvailableDataRate the max. available DR for a link
 	 */
 	public void recordDataRate(long pMaxAvailableDataRate)
 	{
		Logging.err(this,  "Recording DR: " + pMaxAvailableDataRate);
		
		if(mDesiredDataRate > 0){
			if((pMaxAvailableDataRate < mRecordedDataRate) || (mRecordedDataRate == -1)){
 				// assume that HRS::getRoute() automatically reserves MIN(desired DR, available DR)
 				if(pMaxAvailableDataRate < mDesiredDataRate){
 					mRecordedDataRate = pMaxAvailableDataRate;
 				}else{
 					mRecordedDataRate = mDesiredDataRate;
 				}
 			}
		}

		if(mRecordedMaxDataRate > pMaxAvailableDataRate){
			mRecordedMaxDataRate = pMaxAvailableDataRate;
 		}
 	}
 
 	/**
 	 * Returns the desired min.data rate from the sender
 	 * 
 	 * @return the desired data rate in [kbit/s]
 	 */
 	public long getDesiredDataRate()
 	{
 		return mDesiredDataRate;
 	}
 
 	/**
 	 * Returns the desired max. delay from the sender
 	 * 
 	 * @return the desired delay in [ms]
 	 */
 	public long getDesiredDelay()
 	{
 		return mDesiredDelay;
 	}
 	
 	/**
 	 * Return the list of recorded HRMIDs of passed hops
 	 * 
 	 * @return the list of HRMIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<HRMID> getRecordedHops()
 	{
 		return (LinkedList<HRMID>) mRecordedRoute.clone();
 	}
 	
 	/**
 	 * Returns the recorded hop count
 	 * 
 	 * @return the recorded hop count
 	 */
 	public long getRecordedHopCount()
 	{
 		return (HRMConfig.Routing.MAX_HOP_COUNT - mTTR);
 	}
 	
 	/**
 	 * Returns the recorded overall delay
 	 * 
 	 * @return the recorded overall delay
 	 */
 	public long getRecordedDelay()
 	{
 		return mRecordedDelay;	
 	}
 	
 	/**
 	 * Returns the recorded max. available data rate along the route
 	 * 
 	 * @return the recorded data rate
 	 */
 	public long getRecordedMaxDataRate()
 	{
 		return mRecordedMaxDataRate;
 	}
 
 	/**
 	 * Returns the recorded resulting data rate
 	 * 
 	 * @return the recorded data rate
 	 */
 	public long getRecordedDataRate()
 	{
 		return mRecordedDataRate;
 	}
 
 	/**
 	 * Returns the destination of this probe-request
 	 * 
 	 * @return the destination
 	 */
 	public HRMID getDest()
 	{
 		return mDestination;
 	}
 
 	/**
 	 * Return if this property is also needed for intermediate nodes
 	 */
 	@Override
 	public boolean isIntermediateRequirement()
 	{
 		return true;
 	}
 
 	/**
 	 * Logs everything about this property
 	 * 
 	 * @param pParent the parent object
 	 */
 	public void logAll(Object pParent)
 	{
 		// get the recorded route from the property
 		LinkedList<HRMID> tRecordedHRMIDs = getRecordedHops();
 		
 		String tDesiredDataRate = "";
 		if(getDesiredDataRate() >= 1000000)
 			tDesiredDataRate += (getDesiredDataRate() / 1000000) + " Gbit/s";
 		else if(getDesiredDataRate() >= 1000)
 			tDesiredDataRate += (getDesiredDataRate() / 1000) + " Mbit/s";
 		else
 			tDesiredDataRate += getDesiredDataRate() + " kbit/s";
 
 		String tReservedDataRate = "";
 		if(getRecordedDataRate() >= 1000000)
 			tReservedDataRate += (getRecordedDataRate() / 1000000) + " Gbit/s";
 		else if(getRecordedDataRate() >= 1000)
 			tReservedDataRate += (getRecordedDataRate() / 1000) + " Mbit/s";
 		else
 			tReservedDataRate += getRecordedDataRate() + " kbit/s";
 
 		String tMaxAvailableDataRate = "";
 		if(getRecordedMaxDataRate() >= 1000000)
 			tMaxAvailableDataRate += (getRecordedMaxDataRate() / 1000000) + " Gbit/s";
 		else if(getRecordedMaxDataRate() >= 1000)
 			tMaxAvailableDataRate += (getRecordedMaxDataRate() / 1000) + " Mbit/s";
 		else
 			tMaxAvailableDataRate += getRecordedDataRate() + " kbit/s";
 		
 		Logging.log(pParent, "     ..detected a probe-routing connection(source=" + getSourceDescription() + ")");
 		Logging.log(pParent, "       ..source: " + tRecordedHRMIDs.getFirst());
 		Logging.log(pParent, "       ..destination: " + getDest());
 		Logging.log(pParent, "       ..desired E2E data rate: " + tDesiredDataRate);
 		Logging.log(pParent, "       ..desired E2E delay: " + getDesiredDelay() + " ms");
		Logging.log(pParent, "       ..recorded E2E data rate: " + tReservedDataRate + " (this is the minimum of all data rate reservations along the taken route)");
 		Logging.log(pParent, "       ..recorded E2E delay: " + getRecordedDelay() + " ms (this is the sum of all delays of all used links)");
 		Logging.log(pParent, "       ..recorded HOP count: " + getRecordedHopCount() + " nodes (this represents the list of passed physical hosts)");
 		Logging.log(pParent, "       ..recorded max. E2E data rate: " + tMaxAvailableDataRate + " (this is the max. avilable data rate along the taken route)");
 		Logging.log(pParent, "       ..passed " + tRecordedHRMIDs.size() + " HRM hops: (this represents the list of passed physical interfaces)");
 
 		// print the recorded route
 		int i = 0;
 		for(HRMID tHRMID : tRecordedHRMIDs){
 			if(i % 2 == 0){
 				Logging.log(pParent, "        ..source[" + i + "]: " + tHRMID);
 			}else{
 				Logging.log(pParent, "          ..hop[" + i + "]: " + tHRMID);
 			}
 			i++;
 		}
 	}
 
 	/**
 	 * Returns a descriptive string about the object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		return getClass().getSimpleName() + "(" + mRecordedRoute.size() + " recorded hops)";
 	}
 }
