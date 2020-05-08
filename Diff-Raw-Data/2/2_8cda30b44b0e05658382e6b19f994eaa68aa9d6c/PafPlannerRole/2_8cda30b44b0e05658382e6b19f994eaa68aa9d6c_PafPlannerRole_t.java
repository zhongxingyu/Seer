 /*
  *	File: @(#)PafPlannerRole.java 	Package: com.pace.base.security 	Project: Paf Base Libraries
  *	Created: Nov 2, 2005  		By: JWatkins
  *	Version: x.xx
  *
  * 	Copyright (c) 2005-2006 Palladium Group, Inc. All rights reserved.
  *
  *	This software is the confidential and proprietary information of Palladium Group, Inc.
  *	("Confidential Information"). You shall not disclose such Confidential Information and 
  * 	should use it only in accordance with the terms of the license agreement you entered into
  *	with Palladium Group, Inc.
  *
  *
  *
 	Date			Author			Version			Changes
 	xx/xx/xx		xxxxxxxx		x.xx			..............
  * 
  */
 package com.pace.base.app;
 
 import java.util.Arrays;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.app.Season;
 
 /**
  * Role
  *
  * @version	x.xx
  * @author JWatkins
  *
  */
 public class PafPlannerRole implements Cloneable {
     
 	private transient static final Logger logger = Logger.getLogger(PafPlannerRole.class);
 	
 	private String roleName = null;
     private String roleDesc = null;
     private String planType = null;
     private String planVersion = null;    
     private String seasonIds[] = null;    
     private Season[] seasons = null;    
     private boolean readOnly;
    private transient boolean assortmentRole = false;
 
 
     /**
      * @return Returns the roleDesc.
      */
     public String getRoleDesc() {
         return roleDesc;
     }
     /**
      * @param roleDesc The roleDesc to set.
      */
     public void setRoleDesc(String roleDesc) {
         this.roleDesc = roleDesc;
     }
     /**
      * @return Returns the roleName.
      */
     public String getRoleName() {
         return roleName;
     }
     /**
      * @param roleName The roleName to set.
      */
     public void setRoleName(String roleName) {
         this.roleName = roleName;
     }
     /**
      * @return Returns the planType.
      */
     public String getPlanType() {
         return planType;
     }
     /**
      * @param planType The planType to set.
      */
     public void setPlanType(String planType) {
         this.planType = planType;
     }
     /**
      * @return Returns the planVersion.
      */
     public String getPlanVersion() {
         return planVersion;
     }
     /**
      * @param planVersion The planVersion to set.
      */
     public void setPlanVersion(String planVersion) {
         this.planVersion = planVersion;
     }
     /**
      * @return Returns the seasonIds.
      */
     public String[] getSeasonIds() {
         return seasonIds;
     }
     /**
      * @param seasonIds The seasonIds to set.
      */
     public void setSeasonIds(String[] seasonIds) {
         this.seasonIds = seasonIds;
     }
     
     /**
      * @param seasons The array of Season objects to set..
      */
 	public void setSeasons(Season[] seasons) {
 		this.seasons = seasons;
 	}
 	
     /**
      * @return Returns an array of Season objects.
      */
 	public Season[] getSeasons() {
 		return seasons;
 	}
 	public boolean isReadOnly() {
 		return readOnly;
 	}
 	public void setReadOnly(boolean readOnly) {
 		this.readOnly = readOnly;
 	}
 
 	/**
 	 * @return the assortmentRole
 	 */
 	public boolean isAssortmentRole() {
 		return assortmentRole;
 	}
 	/**
 	 * @param assortmentRole the assortmentRole to set
 	 */
 	public void setAssortmentRole(boolean assortmentRole) {
 		this.assortmentRole = assortmentRole;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((planType == null) ? 0 : planType.hashCode());
 		result = prime * result
 				+ ((planVersion == null) ? 0 : planVersion.hashCode());
 		result = prime * result + (readOnly ? 1231 : 1237);
 		result = prime * result
 				+ ((roleDesc == null) ? 0 : roleDesc.hashCode());
 		result = prime * result
 				+ ((roleName == null) ? 0 : roleName.hashCode());
 		result = prime * result + Arrays.hashCode(seasonIds);
 		result = prime * result + Arrays.hashCode(seasons);
 		return result;
 	}
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		PafPlannerRole other = (PafPlannerRole) obj;
 		if (planType == null) {
 			if (other.planType != null)
 				return false;
 		} else if (!planType.equals(other.planType))
 			return false;
 		if (planVersion == null) {
 			if (other.planVersion != null)
 				return false;
 		} else if (!planVersion.equals(other.planVersion))
 			return false;
 		if (readOnly != other.readOnly)
 			return false;
 		if (roleDesc == null) {
 			if (other.roleDesc != null)
 				return false;
 		} else if (!roleDesc.equals(other.roleDesc))
 			return false;
 		if (roleName == null) {
 			if (other.roleName != null)
 				return false;
 		} else if (!roleName.equals(other.roleName))
 			return false;
 		if (!Arrays.equals(seasonIds, other.seasonIds))
 			return false;
 		if (!Arrays.equals(seasons, other.seasons))
 			return false;
 		return true;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#clone()
 	 */
 	@Override
 	public PafPlannerRole clone() {
 
 		PafPlannerRole pafPlannerRole = null;
 		
 		try {
 			
 			pafPlannerRole = (PafPlannerRole) super.clone();
 			
 			if ( this.getSeasons() != null ) {
 				
 				Season[] clonedSeasonAr = new Season[this.seasons.length];
 				
 				for (int i = 0; i < this.seasons.length; i++ ) {
 					
 					clonedSeasonAr[i] = this.seasons[i].clone();
 					
 				}
 				
 			}
 			
 		} catch (CloneNotSupportedException e) {
 			//can't happen if implements cloneable
 			logger.warn(e.getMessage());
 		}
 		
 		return pafPlannerRole;
 	}
 
 }
