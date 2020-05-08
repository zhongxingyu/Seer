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
 package de.tuilmenau.ics.fog.facade;
 
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.application.InterOpIP;
 import de.tuilmenau.ics.fog.facade.properties.DatarateProperty;
 import de.tuilmenau.ics.fog.facade.properties.DelayProperty;
 import de.tuilmenau.ics.fog.facade.properties.FunctionalRequirementProperty;
 import de.tuilmenau.ics.fog.facade.properties.Property;
 import de.tuilmenau.ics.fog.facade.properties.IpDestinationProperty;
 import de.tuilmenau.ics.fog.facade.properties.LossRateProperty;
 import de.tuilmenau.ics.fog.facade.properties.NonFunctionalRequirementsProperty;
 import de.tuilmenau.ics.fog.facade.properties.OrderedProperty;
 import de.tuilmenau.ics.fog.facade.properties.PropertyException;
 import de.tuilmenau.ics.fog.facade.properties.TransportProperty;
 import de.tuilmenau.ics.fog.facade.properties.MinMaxProperty.Limit;
 
 public class Description implements Iterable<Property>, Serializable
 {
 	private static final long serialVersionUID = -8096508525836787147L;
 	private static final Iterator<Property> nullIterator = new Iterator<Property>() {
 		@Override
 		public boolean hasNext()
 		{
 			return false;
 		}
 
 		@Override
 		public Property next()
 		{
 			return null;
 		}
 
 		@Override
 		public void remove()
 		{
 		}
 	};
 
 	public Description()
 	{
 		mProperties = null;
 	}
 	
 	public Description(Description original)
 	{
 		mProperties = new LinkedList<Property>(original.mProperties);
 	}
 	
 	/**
 	 * Appends a description to an existing one 
 	 * 
 	 * @param pToAppend The description which has to be appended
 	 * @return none
 	 */
 	public void append(Description pToAppend) throws PropertyException
 	{
 		if (pToAppend != null)
 		{
 			for(Property tProperty: pToAppend)
 			{
 				add(tProperty);
 			}
 		}
 	}
 	
 	public void set(Property pProperty)
 	{
 		if(pProperty != null) {
 			if(mProperties == null) {
 				mProperties = new LinkedList<Property>();
 			} else {
 				Property tExisting = get(pProperty.getClass());
 				
 				if(tExisting != null) {
 					mProperties.remove(tExisting);
 				}
 			}
 			
 			mProperties.add(pProperty);
 		}
 	}
 	
 	public void add(Property pProperty) throws PropertyException
 	{
 		if(pProperty != null) {
 			Property tExisting = null;
 			
 			if(mProperties == null) {
 				mProperties = new LinkedList<Property>();
 			} else {
 				tExisting = get(pProperty.getClass());
 			}
 			
 			if(tExisting == null) {
 				mProperties.add(pProperty);
 			} else {
 				tExisting.fuse(pProperty);
 			}
 		}
 	}
 	
 	public boolean remove(Property pProperty)
 	{
 		if(mProperties != null)
 			return mProperties.remove(pProperty);
 		else
 			return true;
 	}
 	
 	public boolean isEmpty()
 	{
 		if(mProperties != null)
 			return mProperties.isEmpty();
 		else
 			return true;
 	}
 	
 	public boolean isBestEffort()
 	{
 		if(mProperties != null) {
 			for(Property prop : mProperties) {
 				if(prop instanceof NonFunctionalRequirementsProperty) {
 					if(!((NonFunctionalRequirementsProperty) prop).isBE()) {
 						return false;
 					}
 				}
 				else if(prop instanceof OrderedProperty) {
 					if(((OrderedProperty) prop).getActivation()) {
 						return false;
 					}
 				}
 			}
 		}
 		
 		return true;
 	}
 	
 	public int size()
 	{
 		if(mProperties != null)
 			return mProperties.size();
 		else
 			return 0;
 	}
 	
 	@Override
 	public Iterator<Property> iterator()
 	{
 		if(mProperties != null)
 			return mProperties.iterator();
 		else
 			return nullIterator;
 	}
 	
 	/**
 	 * Returns the first property in the description, which class equals
 	 * the given class.
 	 * 
 	 * @param pClassFilter Class of the desired property
 	 * @return Reference to property or null, if no such property exists
 	 */
 	public Property get(Class<?> pClassFilter)
 	{
 		if(pClassFilter != null) {
 			for(Property tProperty : this) {
 				if(tProperty != null) {
 					if(tProperty.getClass().equals(pClassFilter)) {
 						return tProperty;
 					}
 				}
 			}
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Returns the first property in the description, which type name equals
 	 * the given string.
 	 * 
 	 * @param pPropertyTypeName Type name of the property
 	 * @return Reference to property or null, if no such property exists
 	 */
 	public Property get(String pPropertyTypeName)
 	{
 		if(pPropertyTypeName != null) {
 			for(Property tProperty : this) {
 				if(tProperty != null) {
 					if(pPropertyTypeName.equals(tProperty.getTypeName())) {
 						return tProperty;
 					}
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Searches for DelayProperty property and determines the desired max. delay
 	 * 
 	 * @return the desired delay in [ms]
 	 */
 	public int getDesiredDelay()
 	{
 		int tResult = 0;
 		
 		for(Property tProperty : this) {
 			if (tProperty instanceof DelayProperty) {
 				DelayProperty tPropertyDelay = (DelayProperty)tProperty;
 				
 				tResult = tPropertyDelay.getMax();
 			}
 		}
 
 		return tResult;
 	}
 	
 	/**
 	 * Searches for DatarateProperty property and determines the desired min. data rate
 	 * 
 	 * @return the desired bandwidth in [kbit/s]
 	 */
 	public int getDesiredDataRate()
 	{
 		int tResult = 0;
 		
 		for(Property tProperty : this) {
 			if (tProperty instanceof DatarateProperty) {
 				DatarateProperty tPropertyBandwidth = (DatarateProperty)tProperty;
 				
 				tResult = tPropertyBandwidth.getMin();
 			}
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Searches for non-functional properties in the description.
 	 * 
 	 * @return Description with the references (!= null)
 	 */
 	public Description getNonFunctional()
 	{
 		Description tResDesc = new Description();
 		
 		for(Property tProperty : this) {
			if(tProperty != null) {
				if (tProperty instanceof NonFunctionalRequirementsProperty) {				
					tResDesc.set(tProperty);
				}
 			}
 		}
 
 		return tResDesc;
 	}
 
 	/**
 	 * Searches for functional properties in the description
 	 * 
 	 * @return List with functional properties (!= null)
 	 */
 	public Description getFunctional()
 	{
 		Description tRequirements = new Description();
 		
		for(Property tProperty : this) {
			if(tProperty != null) {
				if(tProperty instanceof FunctionalRequirementProperty) {
					tRequirements.set(tProperty);
 				}
 			}
 		}
 		
 		return tRequirements;
 	}
 
 	
 	/**
 	 * Calculate description for request to remote system based on the
 	 * description of the way from the remote system to the local one.
 	 * 
 	 * @param pDescr Description from original way to the local system
 	 * @return Description for way back (!= null)
 	 */
 	public Description calculateDescrForRemoteSystem() throws PropertyException
 	{
 		Description tReturnDescription = new Description();
 		
 		for(Property tProp : this) {
 			if(tProp instanceof FunctionalRequirementProperty) {
 				FunctionalRequirementProperty tRemoteProp = ((FunctionalRequirementProperty) tProp).getRemoteProperty();
 				if(tRemoteProp != null) {
 					tReturnDescription.add(tRemoteProp);
 				} /*else {
 					// Property for remote system is unknown. 
 				}*/
 			} else {
 				tReturnDescription.add(tProp);
 			}
 		}
 		
 		return tReturnDescription;
 	}
 
 	/**
 	 * {@link NonFunctionalRequirementsProperty} method deriveRequirements
 	 */
 	public Description deriveRequirements(Description pDescr) throws PropertyException
 	{
 		Description tRes = new Description();
 		
 		if(pDescr != null) {
 			// iterate all elements in capabilities
 			for(Property tProp : this) {
 				if(tProp instanceof NonFunctionalRequirementsProperty) {
 					Property tMinusProp = pDescr.get(tProp.getClass());
 					
 					if(tMinusProp != null) {
 						tRes.add(((NonFunctionalRequirementsProperty)tProp).deriveRequirements(tMinusProp));
 					} else {
 						// Requ is not listed in other list. Take over old one without changes.
 						tRes.add(tProp);
 					}
 				} else {
 					throw new PropertyException(this, "Can not handle non functional " +tProp);
 				}
 			}
 			
 			// check remaining elements in requirements
 			for(Property tProp : pDescr) {
 				// already handled in first loop?
 				Property alreadyCovered = tRes.get(tProp.getClass());
 				if(alreadyCovered == null) {
 					tRes.add(tProp);
 				}
 			}
 		} else {
 			// Other list is not defined. Take over old list without changes.
 			return this;
 		}
 		
 		
 		return tRes;
 	}
 
 	/**
 	 * {@link NonFunctionalRequirementsProperty} method removeCapabilities
 	 */
 	public Description removeCapabilities(Description pDescr) throws PropertyException
 	{
 		Description tRes = new Description();
 		
 		if(pDescr != null) {
 			for(Property tProp : this) {
 				if(tProp instanceof NonFunctionalRequirementsProperty) {
 					Property tPlusProp = pDescr.get(tProp.getClass());
 					
 					if(tPlusProp != null) {
 						tRes.add(((NonFunctionalRequirementsProperty)tProp).removeCapabilities(tPlusProp));
 					} else {
 						tRes.add(tProp);
 					}
 				} else {
 					if(tProp instanceof FunctionalRequirementProperty) {
 						throw new PropertyException(this, "Can not handle non functional " +tProp);
 					} else {
 						// it is a property, which does not belong in any category
 						tRes.add(tProp);
 					}
 				}
 			}
 		}
 		
 		
 		return tRes;
 	}
 
 	/**
 	 * TODO Method does not work for descriptions with same elements but in different order!
 	 */
 	public boolean equals(Object obj)
 	{
 		if(obj == this) return true;
 		
 		// empty list is equal to no description (both best effort)
 		if(obj == null) return isBestEffort();
 		
 		if(obj instanceof Description) {			
 			Description descr = (Description) obj;
 			Iterator<Property> tIterator = iterator();
 			Iterator<Property> tNewIterator = descr.iterator();
 			
 			while(tIterator.hasNext() && tNewIterator.hasNext()) {
 				Property tReq    = tIterator.next();
 				Property tNewReq = tNewIterator.next();
 				
 				if(tReq != null) {
 					if(!tReq.equals(tNewReq)) {
 						return false;
 					}
 				}
 			}
 			
 			// both lists ended?
 			if(tIterator.hasNext() || tNewIterator.hasNext()) {
 				return false;
 			}
 			
 			return true;
 		} else {
 			// not a description object
 			return false;
 		}
 	}
 
 	public Description clone()
 	{
 		Description tDescr = new Description();
 
 		for(Property tProp : this) {
 			tDescr.set(tProp.clone());
 		}
 		
 		return tDescr;
 	}
 	
 	/**
 	 * Creates stream description without any QoS requirements (best effort) 
 	 * 
 	 * @param ordered If the stream data should be ordered or not
 	 * @return Description object
 	 */
 	public static Description createBE(boolean ordered)
 	{
 		Description descr = new Description();
 
 		if(ordered)
 			descr.set(new OrderedProperty(ordered));
 		
 		return descr;
 	}
 
 	/**
 	 * Factory method for QoS descriptions.
 	 * 
 	 * @param delayMilliSec Maximum delay in milliseconds
 	 * @param bandwidthKBitSec Minimum bandwidth in kilobits per seconds
 	 * 
 	 * @return Description object
 	 */
 	public static Description createQoS(int pDelayMilliSec, int pBandwidthKBitSec)
 	{
 		Description descr = new Description();
 
 		if(pDelayMilliSec > 0){
 			descr.set(new DelayProperty(pDelayMilliSec, Limit.MAX));
 		}
 		if(pBandwidthKBitSec > 0){
 			descr.set(new DatarateProperty(pBandwidthKBitSec, Limit.MIN));
 		}
 
 		return descr;
 	}
 	
 	/**
 	 * Factory method for QoS descriptions.
 	 * 
 	 * @param ordered If the stream data should be ordered or not
 	 * @param delayMilliSec Maximum delay in milliseconds
 	 * @param bandwidthKBitSec Minimum bandwidth in kilobits per seconds
 	 * @return Description object
 	 */
 	public static Description createQoS(boolean ordered, int delayMilliSec, int bandwidthKBitSec)
 	{
 		Description descr = new Description();
 
 		descr.set(new OrderedProperty(ordered));
 		descr.set(new DatarateProperty(bandwidthKBitSec, Limit.MIN));
 		descr.set(new DelayProperty(delayMilliSec, Limit.MAX));
 		
 		return descr;
 	}
 
 	/**
 	 * Factory method for IP destination descriptions.
 	 * 
 	 * @param pDestIp Destination IP address
 	 * @param pDestPort Destination port number
 	 * @param pDestTransport Destination IP based transport (TCP, UDP,..)
 	 * @return Description object
 	 */
 	public static Description createIpDestination(byte[] pDestIp, int pDestPort, InterOpIP.Transport pDestTransport)
 	{
 		Description descr = new Description();
 //		System.out.println("Creating new IP destination description)");
 //		for (int i = 0; i < pDestIp.length; i++)
 //			System.out.println("Data " + i + " = " + pDestIp[i]);
 		descr.set(new IpDestinationProperty(pDestIp, pDestPort, pDestTransport));
 		
 		return descr;
 	}
 	
 	/**
 	 * @return Requirements modeling the assumptions about TCP
 	 */
 	public static Description createTCPlike()
 	{
 		Description requ = new Description();
 		
 		requ.set(new TransportProperty(true, false));
 		
 		return requ;
 	}
 
 	/**
 	 * Factory method for getting an empty description for avoiding
 	 * null pointer with description parameters.
 	 */
 	public static Description createEmpty()
 	{
 		return new Description();
 	}
 
 	//TODO: cleanup with createHostExtended
 	/**
 	 * Factory method for getting a description with all possible properties.
 	 */
 	public static Description createAll()
 	{
 		Description tDesc = createQoS(true, 200, 64);
 		byte[] tTargetIp = new byte[4];
 		tTargetIp[0] = 127;
 		tTargetIp[1] = 0;
 		tTargetIp[2] = 0;
 		tTargetIp[3] = 1;		
 
 		tDesc.set(new IpDestinationProperty(tTargetIp, 5000, InterOpIP.Transport.UDP));
 		tDesc.set(new TransportProperty(true, false));
 		tDesc.set(new LossRateProperty());
 		
 		return tDesc;
 	}
 	
 	/**
 	 * Factory method for getting a description with extended host properties.
 	 */
 	public static Description createHostExtended()
 	{
 		Description tDesc = createQoS(true, 200, 64);
 		byte[] tTargetIp = new byte[4];
 		tTargetIp[0] = 127;
 		tTargetIp[1] = 0;
 		tTargetIp[2] = 0;
 		tTargetIp[3] = 1;		
 
 		tDesc.set(new IpDestinationProperty(tTargetIp, 5000, InterOpIP.Transport.UDP));
 		tDesc.set(new TransportProperty(true, false));
 		tDesc.set(new LossRateProperty());
 		
 		return tDesc;
 	}
 	
 	/**
 	 * Factory method for getting a description with basic properties.
 	 * Basically, it represents end host functions.
 	 */
 	public static Description createHostBasic()
 	{
 		Description tDesc = new Description();
 
 		tDesc.set(new TransportProperty(true, false));
 		
 		return tDesc;
 	}
 	
 	public String toString()
 	{
 		String tResult = new String();
 		for(Property tProperty : this)
 			tResult += tProperty.toString()+ " ";
 		
 		return tResult;
 	}
 	
 	private LinkedList<Property> mProperties;
 
 }
