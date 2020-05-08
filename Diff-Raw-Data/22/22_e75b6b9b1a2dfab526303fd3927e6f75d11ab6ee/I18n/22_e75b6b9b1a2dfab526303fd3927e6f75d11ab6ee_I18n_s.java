 /* 
  * @(#)I18n.java
  * 
  * Copyright (c) 2006 by Blip Networks, Inc.
  * 239 Centre St, 3rd Floor
  * New York, NY 10013
  * All rights reserved.
  *
  * This software is the confidential and
  * proprietary information of Blip Networks, Inc.
  */
 
 package com.blipnetworks.util;
 
 import java.util.ResourceBundle;
 
 /**
  * This class serves as an internationalization helper.
  *
  * @author Jared Klett
 * @version $Id: I18n.java,v 1.9 2006/10/20 17:26:46 jklett Exp $
  */
 
 public class I18n {
 
 // CVS info ////////////////////////////////////////////////////////////////////
 
	public static final String CVS_REV = "$Revision: 1.9 $";
 
 // Static variables ////////////////////////////////////////////////////////////
 
 	private static ResourceBundle bundle = null;
 
 // Static methods //////////////////////////////////////////////////////////////
 
	private static ResourceBundle getBundle() {
		if (bundle == null)
            bundle = ResourceBundle.getBundle("com-blipnetworks-upperblip");
 
 		return bundle;
 	}
 
 	/**
 	 *
 	 * @param key The key to the requested property.
 	 * @return The value of the requested property.
 	 */
 	public static String getString(String key) {
 		return getBundle().getString(key);
 	}
 
 	/**
 	 *
 	 */
 	public static Object getObject(String key) {
 		return getBundle().getObject(key);
 	}
 
 	/**
 	 *
 	 * @param key The key to the requested property.
 	 * @return The value of the requested property.
 	 */
 	public static String[] getStringArray(String key) {
 		return getBundle().getStringArray(key);
 	}
 
 } // class I18n
