 /*
  *	File: @(#)AppSettings.java 		Package: com.pace.base.app 	Project: Paf Base Libraries
  *	Created: Apr 9, 2007  			By: AFarkas
  *	Version: x.xx
  *
  * 	Copyright (c) 2005-2007 Palladium Group, Inc. All rights reserved.
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
 import java.util.Set;
 
 import com.pace.base.comm.DataFilterSpec;
 import com.pace.base.comm.UserFilterSpec;
 
 
 /**
  * Application specific settings
  *
  * @author AFarkas
  * @version	x.xx
  *
  */
 public class AppSettings {
 	
     private String appTitle;
     private AllocType globalAllocType;
 	private Integer globalUowSizeLarge;
 	private Integer globalUowSizeMax;
 	private boolean globalReplicateEnabled;	
 	private boolean globalReplicateAllEnabled;
	private boolean globalLiftEnabled;					//1793 - Lift Allocation
	private boolean globalLiftAllEnabled; 				//1793 - Lift Allocation
 	private boolean isGlobalUserFilteredUow;
	private boolean isGlobalUserFilteredMultiSelect;	// TTN 1733 - multi-select 
 	private boolean isGlobalDataFilteredUow;
 	private UserFilterSpec globalUserFilterSpec;
 	private DataFilterSpec globalDataFilterSpec;
  	private boolean enableRounding = false;
 	private Set<String> week53Years;					// TTN-1858 - Week 53 Support
 	private Set<String> week53Members;					// TTN-1858 - Week 53 Support
  	private AppColors appColors;
 	
  	private AliasMapping[] globalAliasMappings;
  	
  	private SuppressZeroSettings globalSuppressZeroSettings;
  	
 	public AppSettings(){
 	}
 
 	/**
 	 * @return the appTitle
 	 */
 	public String getAppTitle() {
 		return appTitle;
 	}
 
 	/**
 	 * @param appTitle the appTitle to set
 	 */
 	public void setAppTitle(String appTitle) {
 		this.appTitle = appTitle;
 	}
 	
 	/**
 	 * @return the globalAllocType
 	 */
 	public AllocType getGlobalAllocType() {
 		return globalAllocType;
 	}
 
 
 	public void setGlobalAllocType(AllocType globalAllocType) {
 		this.globalAllocType = globalAllocType;
 	}
 
 	/**
 	 * @return the globalUowSizeLarge
 	 */
 	public Integer getGlobalUowSizeLarge() {
 		return globalUowSizeLarge;
 	}
 	
 	/**
 	 * @param globalUowSizeLarge the globalUowSizeLarge to set
 	 */
 	public void setGlobalUowSizeLarge(Integer globalUowSizeLarge) {
 		this.globalUowSizeLarge = globalUowSizeLarge;
 	}
 	
 	/**
 	 * @return the globalUowSizeMax
 	 */
 	public Integer getGlobalUowSizeMax() {
 		return globalUowSizeMax;
 	}
 	
 	/**
 	 * @param globalUowSizeMax the globalUowSizeMax to set
 	 */
 	public void setGlobalUowSizeMax(Integer globalUowSizeMax) {
 		this.globalUowSizeMax = globalUowSizeMax;
 	}
 	
 	/**
 	 * @return the globalReplicateAllEnabled
 	 */
 	public boolean isGlobalReplicateAllEnabled() {
 		return globalReplicateAllEnabled;
 	}
 
 	/**
 	 * @param globalReplicateAllEnabled the globalReplicateAllEnabled to set
 	 */
 	public void setGlobalReplicateAllEnabled(boolean globalReplicateAllEnabled) {
 		this.globalReplicateAllEnabled = globalReplicateAllEnabled;
 	}
 
 	/**
 	 * @return the globalReplicateEnabled
 	 */
 	public boolean isGlobalReplicateEnabled() {
 		return globalReplicateEnabled;
 	}
 
 
 	/**
 	 * @param globalReplicateEnabled the globalReplicateEnabled to set
 	 */
 	public void setGlobalReplicateEnabled(boolean globalReplicateEnabled) {
 		this.globalReplicateEnabled = globalReplicateEnabled;
 	}
 
 	/**
 	 * @return the globalLiftEnabled
 	 */
 	public boolean isGlobalLiftEnabled() {
 		return globalLiftEnabled;
 	}
 
 	/**
 	 * @param globalLiftEnabled the globalLiftEnabled to set
 	 */
 	public void setGlobalLiftEnabled(boolean globalLiftEnabled) {
 		this.globalLiftEnabled = globalLiftEnabled;
 	}
 
 	/**
 	 * @return the globalLiftAllEnabled
 	 */
 	public boolean isGlobalLiftAllEnabled() {
 		return globalLiftAllEnabled;
 	}
 
 	/**
 	 * @param globalLiftAllEnabled the globalLiftAllEnabled to set
 	 */
 	public void setGlobalLiftAllEnabled(boolean globalLiftAllEnabled) {
 		this.globalLiftAllEnabled = globalLiftAllEnabled;
 	}
 
 	/**
 	 * @return the globalDataFilterSpec
 	 */
 	public DataFilterSpec getGlobalDataFilterSpec() {
 		return globalDataFilterSpec;
 	}
 
 	/**
 	 * @param globalDataFilterSpec the globalDataFilterSpec to set
 	 */
 	public void setGlobalDataFilterSpec(DataFilterSpec globalDataFilterSpec) {
 		this.globalDataFilterSpec = globalDataFilterSpec;
 	}
 
 	/**
 	 * @return the globalUserFilterSpec
 	 */
 	public UserFilterSpec getGlobalUserFilterSpec() {
 		return globalUserFilterSpec;
 	}
 
 	/**
 	 * @param globalUserFilterSpec the globalUserFilterSpec to set
 	 */
 	public void setGlobalUserFilterSpec(UserFilterSpec globalUserFilterSpec) {
 		this.globalUserFilterSpec = globalUserFilterSpec;
 	}
 
 	/**
 	 * @return the isGlobalDataFilteredUow
 	 */
 	public boolean isGlobalDataFilteredUow() {
 		return isGlobalDataFilteredUow;
 	}
 
 	/**
 	 * @param isGlobalDataFilteredUow the isGlobalDataFilteredUow to set
 	 */
 	public void setGlobalDataFilteredUow(boolean isGlobalDataFilteredUow) {
 		this.isGlobalDataFilteredUow = isGlobalDataFilteredUow;
 	}
 
 	/**
 	 * @return the isGlobalUserFilteredUow
 	 */
 	public boolean isGlobalUserFilteredUow() {
 		return isGlobalUserFilteredUow;
 	}
 
 	/**
 	 * @param isGlobalUserFilteredUow the isGlobalUserFilteredUow to set
 	 */
 	public void setGlobalUserFilteredUow(boolean isGlobalUserFilteredUow) {
 		this.isGlobalUserFilteredUow = isGlobalUserFilteredUow;
 	}
 
 	
 	public void setEnableRounding(boolean enableRounding) {
 		this.enableRounding = enableRounding;
 	}
 
 	
 	public boolean isEnableRounding() {
 		return enableRounding;
 	}
 
 	/**
 	 * @return the globalAliasMappings
 	 */
 	public AliasMapping[] getGlobalAliasMappings() {
 		return globalAliasMappings;
 	}
 
 	/**
 	 * @param globalAliasMappings the globalAliasMappings to set
 	 */
 	public void setGlobalAliasMappings(AliasMapping[] globalAliasMappings) {
 		this.globalAliasMappings = globalAliasMappings;
 	}
 
 	
 	/**
 	 * @return the appColors
 	 */
 
 	public AppColors getAppColors() {
 		return appColors;
 	}
 
 
 	/**
 	 * @param appColors the appColors to set
 	 */
 
 	public void setAppColors(AppColors appColors) {
 		this.appColors = appColors;
 	}
 
 	/**
 	 * @return the globalSuppressZeroSettings
 	 */
 	public SuppressZeroSettings getGlobalSuppressZeroSettings() {
 		return globalSuppressZeroSettings;
 	}
 
 	/**
 	 * @param globalSuppressZeroSettings the globalSuppressZeroSettings to set
 	 */
 	public void setGlobalSuppressZeroSettings(
 			SuppressZeroSettings globalSuppressZeroSettings) {
 		this.globalSuppressZeroSettings = globalSuppressZeroSettings;
 	}
 
 	/**
 	 * @return the week53Years
 	 */
 	public Set<String> getWeek53Years() {
 		return week53Years;
 	}
 
 	/**
 	 * @param week53Years the week53Years to set
 	 */
 	public void setWeek53Years(Set<String> week53Years) {
 		this.week53Years = week53Years;
 	}
 
 	/**
 	 * @return the week53Members
 	 */
 	public Set<String> getWeek53Members() {
 		return week53Members;
 	}
 
 	/**
 	 * @param week53Members the week53Members to set
 	 */
 	public void setWeek53Members(Set<String> week53Members) {
 		this.week53Members = week53Members;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((appColors == null) ? 0 : appColors.hashCode());
 		result = prime * result
 				+ ((appTitle == null) ? 0 : appTitle.hashCode());
 		result = prime * result + (enableRounding ? 1231 : 1237);
 		result = prime * result + Arrays.hashCode(globalAliasMappings);
 		result = prime
 				* result
 				+ ((globalDataFilterSpec == null) ? 0 : globalDataFilterSpec
 						.hashCode());
 		result = prime * result + (globalReplicateAllEnabled ? 1231 : 1237);
 		result = prime * result + (globalReplicateEnabled ? 1231 : 1237);
 		result = prime
 				* result
 				+ ((globalSuppressZeroSettings == null) ? 0
 						: globalSuppressZeroSettings.hashCode());
 		result = prime
 				* result
 				+ ((globalUowSizeLarge == null) ? 0 : globalUowSizeLarge
 						.hashCode());
 		result = prime
 				* result
 				+ ((globalUowSizeMax == null) ? 0 : globalUowSizeMax.hashCode());
 		result = prime
 				* result
 				+ ((globalUserFilterSpec == null) ? 0 : globalUserFilterSpec
 						.hashCode());
 		result = prime * result + (isGlobalDataFilteredUow ? 1231 : 1237);
 		result = prime * result + (isGlobalUserFilteredUow ? 1231 : 1237);
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
 		AppSettings other = (AppSettings) obj;
 		if (appColors == null) {
 			if (other.appColors != null)
 				return false;
 		} else if (!appColors.equals(other.appColors))
 			return false;
 		if (appTitle == null) {
 			if (other.appTitle != null)
 				return false;
 		} else if (!appTitle.equals(other.appTitle))
 			return false;
 		if (enableRounding != other.enableRounding)
 			return false;
 		if (!Arrays.equals(globalAliasMappings, other.globalAliasMappings))
 			return false;
 		if (globalDataFilterSpec == null) {
 			if (other.globalDataFilterSpec != null)
 				return false;
 		} else if (!globalDataFilterSpec.equals(other.globalDataFilterSpec))
 			return false;
 		if (globalReplicateAllEnabled != other.globalReplicateAllEnabled)
 			return false;
 		if (globalReplicateEnabled != other.globalReplicateEnabled)
 			return false;
 		if (globalSuppressZeroSettings == null) {
 			if (other.globalSuppressZeroSettings != null)
 				return false;
 		} else if (!globalSuppressZeroSettings
 				.equals(other.globalSuppressZeroSettings))
 			return false;
 		if (globalUowSizeLarge == null) {
 			if (other.globalUowSizeLarge != null)
 				return false;
 		} else if (!globalUowSizeLarge.equals(other.globalUowSizeLarge))
 			return false;
 		if (globalUowSizeMax == null) {
 			if (other.globalUowSizeMax != null)
 				return false;
 		} else if (!globalUowSizeMax.equals(other.globalUowSizeMax))
 			return false;
 		if (globalUserFilterSpec == null) {
 			if (other.globalUserFilterSpec != null)
 				return false;
 		} else if (!globalUserFilterSpec.equals(other.globalUserFilterSpec))
 			return false;
 		if (isGlobalDataFilteredUow != other.isGlobalDataFilteredUow)
 			return false;
 		if (isGlobalUserFilteredUow != other.isGlobalUserFilteredUow)
 			return false;
 		return true;
 	}
 	//TTN 1733 - role filter 
 	public boolean isGlobalUserFilteredMultiSelect() {
 		return isGlobalUserFilteredMultiSelect;
 	}
 
 	public void setGlobalUserFilteredMultiSelect(
 			boolean isGlobalUserFilteredMultiSelect) {
 		this.isGlobalUserFilteredMultiSelect = isGlobalUserFilteredMultiSelect;
 	}
 
 	
 }
 
