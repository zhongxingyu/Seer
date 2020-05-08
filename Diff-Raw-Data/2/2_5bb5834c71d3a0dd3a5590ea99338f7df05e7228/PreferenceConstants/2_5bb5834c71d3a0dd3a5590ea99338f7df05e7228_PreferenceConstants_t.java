 /*******************************************************************************
  * Copyright (c) 2011 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class holds R4E LDAP preferences constants
  * 
  * Contributors:
  *   Jacques Bouthillier - Created for Mylyn Review R4E LDAP project
  *   
  *******************************************************************************/
 
 
 
 package org.eclipse.mylyn.reviews.ldap.internal.preferences;
 
 /**
  * @author Jacques Bouthillier
  */
 public class PreferenceConstants {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 	
 	/**
 	 * The preferences description text
 	 */
 	public static final String	FP_DESC						= "R4E LDAP Preferences";
 
 	/**
 	 * Server information
 	 */
 
 	/**
 	 * The group to keep the information about the LDAP server
 	 */
 	public static final String	FP_SERVER_GROUP				= " Server Information";
 
 	// The value is : ldap://, ldaps://
 	public static final String		FP_SERVER_TYPE_LABEL				= "Type";
 	public static final String		FP_SERVER_TYPE_ID					= "serverType";
 	public static final String		FP_SERVER_BASIC						= "ldap://";
 	public static final String		FP_SERVER_SECURE					= "ldaps://";
 
 	public static final String[][]	FP_SERVER_TYPE_VALUES				= { { "ldap", FP_SERVER_BASIC },
 			{ "ldaps", FP_SERVER_SECURE }								};
 	/**
 	 * The Host ID preference name ex: hostname.server
 	 */
 	public static final String		FP_HOST_ID							= "hostIdPreference";
 	public static final String		FP_HOST_LABEL						= "Host: ";
 
 	/**
 	 * The base ID ex: DC=cie, DC=se
 	 */
 	public static final String	FP_BASE_ID					= "baseIdPreference";
 	public static final String		FP_BASE_LABEL						= "Base:      DN=";
 
 	/**
 	 * The port ID
 	 */
 	public static final String	FP_PORT_ID					= "portIdPreference";
 	public static final String		FP_PORT_LABEL						= "Port:";
 
 
 	/**
 	 * Security section
 	 */
 
 	/**
 	 * The group to keep the user name and password
 	 */
 	public static final String	FP_SECURITY_GROUP			= "Security ";
 
 	public static final String		FP_SECURITY_AUTHENTICATION_LABEL	= "Authentication";
 
 	// The value is : none simple or strong
 	public static final String		FP_SECURITY_AUTHENTICATION_ID			= "authentication";
 	public static final String	FP_NONE = "none";
 	public static final String	FP_SIMPLE = "simple";
 	public static final String	FP_STRONG = "strong";
 	
 	public static final String[][]	FP_AUTHENTICATION_RADIO_VALUES	= { { "None", FP_NONE },
 			{"Simple",FP_SIMPLE}, {"Strong",FP_STRONG} };
 	
 	/**
 	 * The user name id to connect to the LDAP server
 	 */
 	public static final String	FP_SECURITY_USER_NAME_ID	= "userNamePreference";
 	public static final String		FP_SECURITY_USER_NAME_LABEL			= "User Name: ";
 
 	/**
 	 * The password ID to connect to the LDAP database
 	 */
 	public static final String	FP_SECURITY_PASSWORD_ID				= "passwordIdPreference";
 	public static final String		FP_SECURITY_PASSWORD_LABEL			= "Password: ";
 	
 	/**
 	 * LDAP fields definition section
 	 */
 
 	/**
 	 * The group to keep the LDAP field definition
 	 */
 	public static final String	FP_FIELD_GROUP				= "LDAP field definition ";
 	public static final String	FP_FIELD_MANDATORY_GROUP	= "Mandatory ";
 	public static final String	FP_FIELD_OPTIONAL_GROUP		= "Optional ";
 
 	public static final String	FP_UID_ID					= "cn";
 	public static final String	FP_UID_LABEL				= "User Id:";
 
 	public static final String	FP_NAME_ID					= "displayName";
 	public static final String	FP_NAME_LABEL				= "Name:";
 
 	public static final String	FP_TELEPHONE_ID				= "telephoneNumber";
 	public static final String	FP_TELEPHONE_LABEL			= "Telephone:";
 
 	public static final String	FP_MOBILE_ID				= "mobile";
 	public static final String	FP_MOBILE_LABEL				= "Mobile:";
 
 	public static final String	FP_ECN_ID					= "otherTelephone";
	public static final String	FP_ECN_LABEL				= "Other Phone:";
 
 	public static final String	FP_COMPANY_ID				= "company";
 	public static final String	FP_COMPANY_LABEL			= "Company";
 
 	public static final String	FP_DEPARTMENT_ID			= "department";
 	public static final String	FP_DEPARTMENT_LABEL			= "Department";
 
 	public static final String	FP_OFFICE_NAME_ID			= "physicalDeliveryOfficeName";
 	public static final String	FP_OFFICE_ROOM_Label		= "Office";
 
 	public static final String	FP_ROOM_NUMBER_ID			= "extensionAttribute2";
 	public static final String	FP_ROOM_NUMBER_LABEL		= "Room";
 
 	public static final String	FP_CITY_ID					= "l";
 	public static final String	FP_CITY_LABEL				= "City";
 
 	public static final String	FP_COUNTRY_ID				= "co";
 	public static final String	FP_COUNTRY_LABEL			= "Country";
 
 	public static final String	FP_EMAIL_ID					= "mail";
 	public static final String	FP_EMAIL_LABEL				= "E-Mail";
 
 	// Fields that we could use as well
 	public static final String	FP_DOMAIN_ID				= "userPrincipalName";
 	public static final String	FP_DOMAIN_LABEL				= "Domain";
 
 	public static final String	FP_TITLE_ID					= "title";
 	public static final String	FP_TITLE_LABEL				= "Title";
 
 	public static final String	FP_STREET_ADDRESS_ID		= "streetAddress";
 	public static final String	FP_STREET_ADDRESS_LABEL		= "Address";
 
 	public static final String	FP_POSTAL_CODE_ID			= "postalCode";
 	public static final String	FP_POSTAL_CODE_LABEL		= "Postal Code";
 
 }
