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
import java.util.Locale;
 
 /**
  * This class serves as an internationalization helper.
  *
  * @author Jared Klett
 * @version $Id: I18n.java,v 1.10 2007/03/20 22:00:26 jklett Exp $
  */
 
 public class I18n {
 
 // CVS info ////////////////////////////////////////////////////////////////////
 
	public static final String CVS_REV = "$Revision: 1.10 $";
 
 // Static variables ////////////////////////////////////////////////////////////
 
 	private static ResourceBundle bundle = null;
 
 // Static methods //////////////////////////////////////////////////////////////
 
    private static ResourceBundle getBundle() {
        if (bundle == null) {
            try {
                bundle = ResourceBundle.getBundle("com-blipnetworks-upperblip");
            }
            catch (Exception e) {
                // couldn't load the bundle for the current locale
                // fall back to US English
                bundle = ResourceBundle.getBundle("com-blipnetworks-upperblip", Locale.US);
            }
        }
 
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
