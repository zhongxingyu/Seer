 /*
  *	File: @(#)PafServerConstants.java 	Package: com.pace.base.server 	Project: PafServer
  *	Created: Jan 6, 2006  		By: jim
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
 package com.pace.server;
 
 import com.pace.base.PafBaseConstants;
 
 /**
  * Application server constants
  *
  * @version	x.xx
  * @author Jim Watkins
  *
  */
 public class PafServerConstants {
 
	public static final String SERVER_VERSION = "2.8.4 RC1";
 
 	public static String SERVER_SETTINGS_FILE = PafMetaData.getConfigServerDirPath()  + PafBaseConstants.FN_ServerSettings;
 	public static String LDAP_SETTINGS_FILE = PafMetaData.getConfigServerDirPath()  + PafBaseConstants.FN_LdapSettings;
 	public static String RDB_DATASOURCES_FILE = PafMetaData.getConfigServerDirPath()  + PafBaseConstants.FN_RdbDataSources;
 	public static String MDB_DATASOURCES_FILE = PafMetaData.getConfigServerDirPath()  + PafBaseConstants.FN_MdbDataSources;
 
 }
