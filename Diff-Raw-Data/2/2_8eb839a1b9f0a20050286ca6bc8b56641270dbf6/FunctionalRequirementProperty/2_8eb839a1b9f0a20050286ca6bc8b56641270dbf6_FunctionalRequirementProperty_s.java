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
 package de.tuilmenau.ics.fog.facade.properties;
 
 import java.io.Serializable;
 import java.util.HashMap;
 
 /**
  * Base-class for all functional requirements.
  */
 public abstract class FunctionalRequirementProperty extends AbstractProperty
 {
 	private static final long serialVersionUID = -1934434296873150160L;
 	
 	/**
 	 * @return The direction-pair of functionalities relative to the direction
 	 * of the data-flow.
 	 */
 	public abstract IDirectionPair getDirectionPair();
 	
 	/**
 	 * @return A Map with parameter-values for initialization or
 	 * (re-)configuration of the gate directed upwards relative to the
 	 * direction of the data-flow or {@code null} if not needed.
 	 * 
 	 * @see #getDirectionPair()
 	 * @see #getDownValueMap()
 	 */
 	public abstract HashMap<String, Serializable> getUpValueMap();
 	
 	/**
 	 * @return A Map with parameter-values for initialization or
 	 * (re-)configuration of the gate directed downwards relative to the
 	 * direction of the data-flow or {@code null} if not needed.
 	 * 
 	 * @see #getDirectionPair()
 	 * @see #getUpValueMap()
 	 */
 	public abstract HashMap<String, Serializable> getDownValueMap();
 	
 	/**
 	 * @return A functional requirement of same type to be used at remote
 	 * system.
 	 */
 	public abstract FunctionalRequirementProperty getRemoteProperty();
 	
 	@Override
 	public boolean equals(Object obj)
 	{
 		if(obj == null) return false;
 		if(obj == this) return true;
 		
 		if(obj.getClass().equals(getClass())) {
 			FunctionalRequirementProperty tObj = (FunctionalRequirementProperty) obj;
 			IDirectionPair tDirPair    = getDirectionPair();
 			IDirectionPair tNewDirPair = tObj.getDirectionPair();
 			
 			if(tDirPair != tNewDirPair) {
				// So they are also not both nullpointer.
 				if(tDirPair != null) {
 					if(!tDirPair.equals(tNewDirPair)) {
 						return false;
 					}
 				}
 			}
 			// Required functionality seem to be equal.
 			
 			// Check config data for equality.
 			HashMap<String,Serializable> tConfigUp = getUpValueMap();
 			HashMap<String,Serializable> tConfigDown = getDownValueMap();
 			HashMap<String,Serializable> tNewConfigUp = tObj.getUpValueMap();
 			HashMap<String,Serializable> tNewConfigDown = tObj.getDownValueMap();
 			
 			if(tConfigUp == null) tConfigUp = new HashMap<String,Serializable>();
 			if(tConfigDown == null) tConfigDown = new HashMap<String,Serializable>();
 			if(tNewConfigUp == null) tNewConfigUp = new HashMap<String,Serializable>();
 			if(tNewConfigDown == null) tNewConfigDown = new HashMap<String,Serializable>();
 			
 			return tConfigUp.equals(tNewConfigUp) && tConfigDown.equals(tNewConfigDown);
 		} else {
 			return false;
 		}
 	}
 }
