 /*
  *	File: @(#)PafDimSpec.java 	Package: com.pace.base.server.comm 	Project: PafServer
  *	Created: Oct 13, 2005  		By: JWatkins
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
 
 /**
  * Class_description_goes_here
  *
  * @version	x.xx
  * @author JWatkins
  *
  */
 public class PafDimSpec implements Cloneable {
 
 	private transient final static Logger logger = Logger.getLogger(PafDimSpec.class);
 	
	private transient boolean isSelectable;
 	
     private String dimension;
     
     private String[] expressionList;
     
    public PafDimSpec() { }
     
     /**
      * @return Returns the dimension.
      */
     public String getDimension() {
         return dimension;
     }
     /**
      * @param dimension The dimension to set.
      */
     public void setDimension(String dimension) {
         this.dimension = dimension;
     }
     /**
      * @return Returns the expressionList.
      */
     public String[] getExpressionList() {
         return expressionList;
     }
     /**
      * @param expressionList The expressionList to set.
      */
     public void setExpressionList(String[] expressionList) {
         this.expressionList = expressionList;
     }
 	/**
 	 * @return Returns the isSelectable.
 	 */
 	public boolean isSelectable() {
 		return isSelectable;
 	}
 	/**
 	 * @param isSelectable The isSelectable to set.
 	 */
 	public void setSelectable(boolean isSelectable) {
 		this.isSelectable = isSelectable;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.PafDimSpec#clone()
 	 */
 	@Override
 	public PafDimSpec clone() {
 		
 		try {
 			
 			return (PafDimSpec) super.clone();
 			
 		} catch (CloneNotSupportedException e) {
 			//can't happen if implements cloneable
 			logger.warn(e.getMessage());
 		}
 		
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((dimension == null) ? 0 : dimension.hashCode());
 		result = prime * result + Arrays.hashCode(expressionList);
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
 		PafDimSpec other = (PafDimSpec) obj;
 		if (dimension == null) {
 			if (other.dimension != null)
 				return false;
 		} else if (!dimension.equals(other.dimension))
 			return false;
 		if (!Arrays.equals(expressionList, other.expressionList))
 			return false;
 		return true;
 	}
     
 	
 	
 }
