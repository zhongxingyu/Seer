 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical.management;
 
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.Localization;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class encapsulates the hierarchy level and is used to accumulate all hierarchy level checks in one class.
  * It includes check functions such as isBaseLevel() and isHigherLevel().
  */
 public class HierarchyLevel
 {
 	/**
 	 * Allow for a better debugging based on messages each time such an object is created.
 	 */
 	private static boolean DEBUG_CREATION = false;
 	
 	/**
 	 * This value represents an undefined level.
 	 */
 	private static int UNDEFINED_LEVEL = -1;
 
 	/**
 	 * Identifies the base level of the hierarchy.
 	 */
 	public static final int BASE_LEVEL = 0;		
 
 	/**
 	 * Stores the numeric hierarchy level
 	 */
 	private int mLevel = UNDEFINED_LEVEL;
 
 	/**
 	 * Constructor: initializes the hierarchy level with the given value.
 	 * 
 	 * @param pHierarchyLevelValue the defined new hierarchy level value
 	 */
 	public HierarchyLevel(Object pParent, int pHierarchyLevelValue)
 	{
 		if ((pParent instanceof Integer) || (pParent instanceof Long)){
 			Logging.warn(this, "The parent object is an Integer/Long class, this often means a wrong call");
 		}
 		
 		mLevel = pHierarchyLevelValue;
 		
 		/**
 		 * HINT: Be aware of recursion here. The Bully priority is very often used inside toString(). For example, this would lead 
 		 * to recursive calls caused by getBullyPriority in the Cluster/Coordinator class. 
 		 */
 		
 		if (DEBUG_CREATION){
 			Logging.log(this,  "Created object (explicit level is " + pHierarchyLevelValue + ") for class \"" + pParent.getClass().getSimpleName() + "\"");
 		}
 	}
 
 	/**
 	 * Returns the hierarchy level value.
 	 * 
 	 * @return hierarchy level
 	 */
 	public int getValue()
 	{
 		return mLevel;
 	}
 
 	/**
 	 * Check if the level is still undefined.
 	 * 
 	 * @return return true if this level is undefined, otherwise false is returned
 	 */
 	public boolean isUndefined()
 	{
 		return (mLevel == UNDEFINED_LEVEL);
 	}
 
 	/**
 	 * Check if the level is the base level.
 	 * 
 	 * @return return true if this level is the base level, otherwise false is returned
 	 */
 	public boolean isBaseLevel()
 	{
 		return (mLevel == BASE_LEVEL);
 	}
 	
 	/**
 	 * Check if the level is the base level.
 	 * 
 	 * @return return true if this level is the base level, otherwise false is returned
 	 */
 	public boolean isHigherLevel()
 	{
 		return (mLevel > BASE_LEVEL);
 	}
 
 	/**
 	 * Check if the hierarchy level is the highest one.
 	 * 
 	 * @return return true if the highest possible hierarchy level is used
 	 */
 	public boolean isHighest()
 	{
		return (mLevel >= HRMConfig.Hierarchy.HEIGHT - 1);
 	}
 
 	/**
 	 * Checks if the hierarchy level is in the range [0 - HRMConfig.Hierarchy.HEIGHT]
 	 * 
 	 * @return true or false
 	 */
 	public boolean isValid()
 	{
 		return ((mLevel >= 0) && (mLevel < HRMConfig.Hierarchy.HEIGHT));
 	}
 
 	/**
 	 * Check if the hierarchy level is higher than the other given one.
 	 * 
 	 * @param pCheckLocation a reference to the origin object from where the call comes from
 	 * @param pOtherLevel the other given hierarchy level
 	 * @return return "true" if the hierarchy level is higher than the other one, otherwise return "false"
 	 */
 	public boolean isHigher(Object pCheckLocation, HierarchyLevel pOtherLevel)
 	{
 		String pLocationDescription = pCheckLocation.getClass().getSimpleName();
 		if (pCheckLocation instanceof Localization){
 			Localization tHRMEntity = (Localization)pCheckLocation;
 			pLocationDescription = tHRMEntity.toLocation();
 		}
 
 		if (pOtherLevel == null){
 			Logging.log(pLocationDescription + ": COMPARING HIERARCHY LEVEL " + mLevel + " with NULL POINTER, returning always \"true\" for isHigher()");
 			return true;
 		}
 		
 		Logging.log(pLocationDescription + ": COMPARING HIERARCHY LEVEL " + mLevel + " with alternative " + pOtherLevel.getValue());
 		
 		// if the priority values are equal, we return "true"
 		if (mLevel > pOtherLevel.getValue()){
 			return true;
 		}
 		
 		// otherwise always "false"
 		return false;
 	}
 
 	@Override
 	public boolean equals(Object pObj)
 	{
 		// do we have another HierarchyLevel object?
 		if(pObj instanceof HierarchyLevel) {
 			
 			// cast to HierarchyLevel object
 			HierarchyLevel tOtherLevel = (HierarchyLevel) pObj;
 			
 			// if the level values are equal, we return "true"
 			if (tOtherLevel.getValue() == mLevel){
 				return true;
 			}
 		}
 		
 		// otherwise always "false"
 		return false;
 	}
 
 	/**
 	 * Factory function for a new object indexing the hierarchy base level.
 	 * 
 	 * @return returns a new object for the base hierarchy level
 	 */
 	public static HierarchyLevel createBaseLevel()
 	{
 		return new HierarchyLevel(BASE_LEVEL);
 	}	
 
 	private HierarchyLevel(int pLevel)
 	{
 		mLevel = pLevel;
 	}
 	
 	public String toString()
 	{
 		return "HierLevel(Lvl=" + Long.toString(getValue()) + ")";
 	}
 }
