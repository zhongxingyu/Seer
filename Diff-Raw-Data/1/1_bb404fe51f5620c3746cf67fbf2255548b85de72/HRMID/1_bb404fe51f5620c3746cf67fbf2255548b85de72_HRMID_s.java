 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.naming.hierarchical;
 
 import java.awt.Color;
 import java.math.BigInteger;
 
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.HierarchyLevel;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class is used to identify a node in the HRM graph.
  * 
  * An HRMID can identify:
  * 	1.) a physical node, e.g., "1.1.5"
  *  2.) a coordinator or a cluster as a whole, e.g., "1.1.0"
  */
 public class HRMID extends HRMName implements Comparable<HRMID>
 {
 	private static final long serialVersionUID = -8441496024628988477L;
 
 	/**
 	 * Create an HRMID instance based on a BigInteger value.
 	 * 
 	 * @param pAddress the BigInteger value which is used for HRMID address generation.
 	 */
 	private HRMID(BigInteger pAddress)
 	{
 		super(pAddress);
 	}
 	
 	/**
 	 * Create an HRMID instance based on a long value.
 	 * 
 	 * @param pAddress the long value which used for HRMID address generation.
 	 */
 	public HRMID(long pAddress)
 	{
 		super(BigInteger.valueOf(pAddress));
 	}
 	
 	
 	/**
 	 * Factory function: create an HRMID for broadcasts.
 	 * 
 	 * @return the new HRMID for broadcasts
 	 */
 	public static HRMID createBroadcast()
 	{
 		return new HRMID(HRMConfig.Addressing.BROADCAST_ADDRESS);
 	}
 	
 	/**
 	 * Determine the address part at a specific hierarchical level.
 	 * 
 	 * @param pHierarchyLevel the hierarchy level
 	 * 
 	 * @return the determined address of the specified hierarchical level
 	 */
 	private BigInteger getLevelAddressBigInteger(int pHierarchyLevel)
 	{
 		return (mAddress.mod((BigInteger.valueOf(2)).pow(HRMConfig.Hierarchy.USED_BITS_PER_LEVEL * (pHierarchyLevel + 1))).shiftRight((HRMConfig.Hierarchy.USED_BITS_PER_LEVEL * (pHierarchyLevel))));
 	}
 	
 	/**
 	 * Determine the address part at a specific hierarchical level.
 	 * 
 	 * @param pHierarchyLevel the hierarchy level
 	 * 
 	 * @return the determined address of the specified hierarchical level
 	 */
 	public int getLevelAddress(int pHierarchyLevel)
 	{
 		BigInteger tAdr = getLevelAddressBigInteger(pHierarchyLevel);
 		
 		int tResult = tAdr.intValue();
 		
 		return tResult;
 	}
 
 	/**
 	 * Determine the address part at a specific hierarchical level.
 	 * 
 	 * @param pHierarchyLevel the hierarchy level
 	 * 
 	 * @return the determined address of the specified hierarchical level
 	 */
 	public int getLevelAddress(HierarchyLevel pHierarchyLevel)
 	{
 		return getLevelAddress(pHierarchyLevel.getValue());
 	}
 
 	/**
 	 * Set the address part for a specific hierarchy level.
 	 * 
 	 * @param pHierarchyLevel the hierarchy level
 	 * @param pAddress the address part for the given hierarchy level
 	 */
 	public void setLevelAddress(int pHierarchyLevel, int pAddress)
 	{
 		setLevelAddress(new HierarchyLevel(this, pHierarchyLevel), BigInteger.valueOf(pAddress));
 	}
 
 	/**
 	 * Set the address part for a specific hierarchy level.
 	 * 
 	 * @param pHierarchyLevel the hierarchy level
 	 * @param pAddress the address part for the given hierarchy level
 	 */
 	public void setLevelAddress(int pHierarchyLevel, BigInteger pAddress)
 	{
 		BigInteger tLevelAddr = getLevelAddressBigInteger(pHierarchyLevel);
 		
 		/**
 		 * Subtract the old value
 		 */
 		if(!tLevelAddr.equals(BigInteger.valueOf(0))){
 			mAddress = mAddress.subtract(tLevelAddr.shiftLeft(pHierarchyLevel * HRMConfig.Hierarchy.USED_BITS_PER_LEVEL));
 		}
 		
 		/**
 		 * Add the new value
 		 */
 		if(!pAddress.equals(BigInteger.valueOf(0))){
 			mAddress = mAddress.add(pAddress.shiftLeft(pHierarchyLevel * HRMConfig.Hierarchy.USED_BITS_PER_LEVEL));
 		}
 	}
 
 	/**
 	 * Set the address part for a specific hierarchy level.
 	 * 
 	 * @param pHierarchyLevel the hierarchy level
 	 * @param pAddress the address part for the given hierarchy level
 	 */
 	public void setLevelAddress(HierarchyLevel pHierarchyLevel, BigInteger pAddress)
 	{
 		setLevelAddress(pHierarchyLevel.getValue(), pAddress);
 	}
 	
 	/**
 	 * Creates an instance clone with the same address.
 	 * 
 	 * @return the cloned object
 	 */
 	public HRMID clone()
 	{
 		// create new instance with the same address
 		HRMID tID = new HRMID(mAddress);
 
 		return tID;
 	}
 	
 	/**
 	 * Returns the hierarchy level at which this HRMID differs from another given one
 	 * 
 	 * @param pAddress the address that should be compared to this one
 	 * 
 	 * @return The first occurrence at which a difference was found will be returned.
 	 */
 	public int getPrefixDifference(HRMID pAddress)
 	{
 		int tResult = -1;
 		
 		//Logging.log(this, "Comparing with HRMID: " + pAddress);
 
 		if(pAddress != null){
 			for(int i = HRMConfig.Hierarchy.HEIGHT - 1; i >= 0; i--) {
 				BigInteger tOtherLevelAddress = pAddress.getLevelAddressBigInteger(i);
 				BigInteger tLevelAddress = getLevelAddressBigInteger(i);
 				
 				if(!tLevelAddress.equals(tOtherLevelAddress)) {
 					// return the hierarchy level as result
 					tResult = i;
 					
 					// return immediately
 					break;
 				}
 			}
 		}else{
 			tResult = HRMConfig.Hierarchy.HEIGHT;
 		}
 		
 		//Logging.log(this, "   ..result: " + tResult);
 
 		return tResult;
 	}
 	
 	/**
 	 * Returns if a given HRMID has the same prefix like this one - the compared prefix ends at the given hierarchy level
 	 * 
 	 * @param pOtherAddress the other HRMID
 	 * @param pHierarchyLevel the hierarchy level which marks the end of the compared prefix
 	 * 
 	 * @return true or false
 	 */
 	public boolean hasPrefix(HRMID pOtherAddress, HierarchyLevel pHierarchyLevel)
 	{
 		boolean tResult = false;
 		
 		//Logging.log(this, "Comparing with prefix of HRMID: " + pOtherAddress + ", prefix for level: " + pHierarchyLevel.getValue());
 		
 		int tDiffLevel = getPrefixDifference(pOtherAddress);
 		
 		if((tDiffLevel < 0) || (tDiffLevel <= pHierarchyLevel.getValue())){
 			tResult = true;
 		}
 		
 		//Logging.log(this, "   ..result: " + tResult);
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns the hierarchy level of this cluster address
 	 * 
 	 * @return the hierarchy level, returns -1 is the address is an L0 node address
 	 */
 	public int getHierarchyLevel()
 	{
 		int tResult = -1;
 		
 		for(int i = 0; i < HRMConfig.Hierarchy.HEIGHT; i++){
 			int tLevelValue = getLevelAddress(i);
 			// are we still searching for the cluster prefix?
 			if (tLevelValue == 0){
 				tResult = i;
 			}else{
 				// we found a value unequal to 0
 				break;
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Returns true if this HRMID belongs to the cluster of a given cluster address
 	 * 
 	 * @param pClusterAddress the address of the cluster
 	 * 
 	 * @return true or false
 	 */
 	public boolean isCluster(HRMID pClusterAddress)
 	{
 		boolean tResult = true;
 		boolean tDebug = false;
 		
 		if(tDebug){
 			Logging.err(this, "isCluster() for " + pClusterAddress);
 		}
 		
 		/**
 		 * Catch invalid parameter
 		 */
 		if(pClusterAddress == null){
			Logging.err(this, "isCluster() cannot operate on a null pointer");
 			return false;
 		}
 		
 		/**
 		 * Search for the start of the cluster prefix
 		 */
 		int tCheckLevel = pClusterAddress.getHierarchyLevel();
 		if(tDebug){
 			Logging.err(this, "   ..cluster address prefix found at: " + tCheckLevel);
 		}
 
 		/**
 		 * Compare the prefix of the cluster address with this address
 		 */
 		for(int i = tCheckLevel + 1; i < HRMConfig.Hierarchy.HEIGHT; i++){
 			int tClusterAddressLevelValue = pClusterAddress.getLevelAddress(i);
 			int tLevelValue = getLevelAddress(i);
 			
 			// have we found a difference between both values?
 			if(tClusterAddressLevelValue != tLevelValue){
 				if(tDebug){
 					Logging.err(this, "   ..found difference (" + tClusterAddressLevelValue + " != " + tLevelValue + ") at level " + i);
 				}
 				tResult = false;
 				// return immediately
 				break;
 			}
 		}
 		
 		if(tDebug){
 			Logging.err(this, "   ..result: " + tResult);
 		}
 				
 		return tResult;
 	}
 
 	/**
 	 * Returns the foreign cluster (in relation to this address)
 	 * For example, if this address is 1.3.7 and the foreign address is 1.4.2, the result will be 1.4.0.  
 	 * 
 	 * @param pForeignAddress the foreign address
 	 * 
 	 * @return the foreign cluster address
 	 */
 	public HRMID getForeignCluster(HRMID pForeignAddress)
 	{
 		HRMID tResult = new HRMID(0);
 		boolean tDebug = false;
 		
 		if(tDebug){
 			Logging.log(this, "getForeignCluster() for " + pForeignAddress);
 		}
 		
 		/**
 		 * Search for the start of the cluster prefix
 		 */
 		int tCheckLevel = pForeignAddress.getHierarchyLevel();
 		if(tDebug){
 			Logging.log(this, "   ..cluster address prefix found at: " + tCheckLevel);
 		}
 
 		/**
 		 * Compare the foreign address with this address
 		 */
 		for(int i = HRMConfig.Hierarchy.HEIGHT - 1; i > tCheckLevel; i--){
 			int tForeignClusterAddressLevelValue = pForeignAddress.getLevelAddress(i);
 			int tLevelValue = getLevelAddress(i);
 			
 			//add the digit to the result address
 			tResult.setLevelAddress(i, BigInteger.valueOf(tForeignClusterAddressLevelValue));
 
 			// have we found a difference between both values?
 			if(tForeignClusterAddressLevelValue != tLevelValue){
 				if(tDebug){
 					Logging.log(this, "   ..found difference (" + tForeignClusterAddressLevelValue + " != " + tLevelValue + ") at level " + i);
 				}
 				
 				// return immediately
 				break;
 			}
 		}
 		
 		if(tDebug){
 			Logging.log(this, "   ..result: " + tResult);
 		}
 				
 		return tResult;
 	}
 	
 	
 	
 	//TODO
 	@Override
 	public int getSerialisedSize()
 	{
 		return mAddress.bitLength();
 	}
 
 	@Override
 	//TODO
 	public int compareTo(HRMID pCompareTo) {
 		return getLevelAddressBigInteger(pCompareTo.getPrefixDifference(this)).subtract(pCompareTo.getLevelAddressBigInteger(pCompareTo.getPrefixDifference(this))).intValue();
 	}
 	
 	
 	
 	
 	
 	
 	
 	/**
 	 * Defines the decoration color for the ARG viewer
 	 * 
 	 * @return color for the HRMID
 	 */
 	@Override
 	public Color getColor()
 	{
 		return Color.PINK;
 	}
 
 	/**
 	 * Compares the address value of both class instances and return true if they are equal to each other.
 	 * 
 	 * @return true or false
 	 */
 	@Override
 	public boolean equals(Object pObj)
 	{
 		if(pObj instanceof HRMID) {
 			HRMID tOther = (HRMID)pObj;
 			
 			// compare the addresses
 			return mAddress.intValue() == tOther.mAddress.intValue();
 		}
 		return false;
 	}
 
 	/**
 	 * Determines if this HRMID has a valid prefix, e.g., "1.3.0"
 	 * 
 	 * @return true or false
 	 */
 	public boolean isZero()
 	{
 		return (mAddress.longValue() == 0);
 	}
 	
 	/**
 	 * Determines if this HRMID has a valid prefix, e.g., "1.3.0"
 	 * 
 	 * @return true or false
 	 */
 	public boolean isRelativeAddress()
 	{
 		// true if the first character is a leading zero
 		return toString().startsWith("0");
 	}
 	
 	/**
 	 * Determines if this HRMID has a suffix of a cluster/coordinator address, e.g., "2.1.0"
 	 * 
 	 * @return true or false
 	 */
 	public boolean isClusterAddress()
 	{
 		// true if the first character is a leading zero
 		return toString().endsWith(".0");
 	}
 
 	/**
 	 * Generate an HRMID output, e.g., "4.7.2.3"
 	 */
 	@Override
 	public String toString()
 	{
 		String tOutput = new String();
 		
 		for(int i = HRMConfig.Hierarchy.HEIGHT - 1; i > 0; i--){
 			tOutput += (mAddress.mod((BigInteger.valueOf(2)).pow(HRMConfig.Hierarchy.USED_BITS_PER_LEVEL * (i + 1))).shiftRight((HRMConfig.Hierarchy.USED_BITS_PER_LEVEL * i))).toString();
 			tOutput += ".";
 		}
 		
 		tOutput += (mAddress.mod((BigInteger.valueOf(2)).pow(HRMConfig.Hierarchy.USED_BITS_PER_LEVEL * 1)).shiftRight((HRMConfig.Hierarchy.USED_BITS_PER_LEVEL * 0))).toString();
 		
 		return tOutput;
 	}
 }
