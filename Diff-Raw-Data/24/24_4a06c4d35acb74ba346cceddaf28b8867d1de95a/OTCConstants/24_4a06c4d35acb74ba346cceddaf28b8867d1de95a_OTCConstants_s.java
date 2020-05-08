 /*
  * This file is part of OTC.
  * 
  * Copyright (c) 2011, Marius MOULIS <moulis.marius@gmail.com>
  *
  * OTC is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * OTC is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with OTC.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * 
  * This file contains all constants used by OTC
  * 
  */
 package utils.config;
 
 import javax.swing.JComboBox;
 
 public class OTCConstants {
 	
 	private OTCConstants(){}
 	
 	/** 
 	 * 
 	 * <p>{@link JComboBox} default value
 	 * This constant is used in OTC when a user doesn't want to set a value 
 	 * in a specific field
 	 * </p> 
 	 * */
 	public static final String COMBO_BOX_DEFAULT_VALUE = "No value";
 	
 	/**
 	 * 
 	 * This constant contains the identifier for the parameter "AMISPIM TIFF generated files default location" 
 	 * 
 	 * */
 	public static final String OTC_PARAMETER_TIFF_DEFAULT_PATH_IDENTIFIER_KEY = "TiffDefaultPath";
 	
 	/**
 	 * 
 	 * This constant contains the identifier for the parameter "AMISPIM XML generated files default location" 
 	 * 
 	 * */
 	public static final String OTC_PARAMETER_XML_DEFAULT_PATH_IDENTIFIER_KEY = "XMLDefaultPath";
 	
 	/**
 	 * 
 	 * This constant contains the identifier for the parameter "OTC OME-XML generated files default location" 
 	 * 
 	 * */
 	public static final String OTC_PARAMETER_OME_XML_DEFAULT_PATH_IDENTIFIER_KEY = "OMEXMLDefaultPath";
 	
 	/**
 	 * 
 	 * This constant contains the identifier for the parameter "OTC XTML generated files default location" 
 	 * 
 	 * */
 	public static final String OTC_PARAMETER_HTML_DEFAULT_PATH_IDENTIFIER_KEY = "HTMLDefaultPath";
 	
 	/**
 	 * 
 	 * This constant contains the identifier for the parameter "OTC Templates files default location" 
 	 * 
 	 * */
 	public static final String OTC_PARAMETER_TEMPLATE_DEFAULT_PATH_IDENTIFIER_KEY = "TemplateDefaultLocation";
 	
 	/**
 	 * 
 	 * This constant contains the identifier for the parameter "OTC OME-TIFF generated files default location" 
 	 * 
 	 * */
 	public static final String OTC_PARAMETER_OME_TIFF_DEFAULT_PATH_IDENTIFIER_KEY = "OMETIFFDefaultPath";
 
 	/**
 	 * 
 	 * This constant contains the identifier for the parameter "PostgreSQL database server IP" 
 	 * 
 	 * */
 	public static final String OTC_PARAMETER_POSTGRES_IP_KEY = "PGHost";
 	
 	/**
 	 * 
 	 * This constant contains the identifier for the parameter "PostgreSQL database user name" 
 	 * 
 	 * */
 	public static final String OTC_PARAMETER_POSTGRES_USERNAME_KEY = "PGUser";
 	
 	/**
 	 * 
 	 * This constant contains the identifier for the parameter "PostgreSQL database user password" 
 	 * 
 	 * */
 	public static final String OTC_PARAMETER_POSTGRES_USER_PASSWORD_KEY = "PGPassword";
 	
 	/**
 	 * 
 	 * This constant contains the identifier for the parameter "PostgreSQL database name" 
 	 * 
 	 * */
 	public static final String OTC_PARAMETER_POSTGRES_DB_NAME_KEY = "PGDBName";
 	
 	/**
 	 * 
 	 * This constant contains the identifier for the parameter user session
 	 * 
 	 * */
 	public static final String OTC_PARAMETER_USER_SESSION_KEY = "usersession";
 	
 	/**
 	 * 
 	 * This constant contains the identifier for the parameter containing the list of couple (TIFF, XML) of file to be converted 
 	 * 
 	 * */
 	public static final String OTC_PARAMETER_FILE_TO_BE_CONVERTED_KEY = "fileToBeConverted";
 	
 	/**
 	 * 
 	 * This constant contains the identifier for the parameter storing the template used for the conversion  
 	 * 
 	 * */
 	public static final String OTC_PARAMETER_TEMPLATE_KEY = "template";
 	
 	/**
 	 * 
 	 * This constant contains the identifier for the parameter which store the HTTP address of OMERO server
 	 * 
 	 * */
 	public static final String OTC_PARAMETER_OMERO_HTTP_ADDRESS = "OmeroHttpAddress";
 	
 	/**
 	 * 
 	 * This constant contains the identifier for the parameter which store the directory of OMERO generated file
 	 * 
 	 * */
 	public static final String OTC_PARAMETER_OMERO_HTML_DIRECTORY = "OmeroHtmlDirectory";
 	
 	/**
 	 * 
 	 * This constant contains the identifier for the parameter which store the OTC log file path
 	 * 
 	 * */
 	public static final String OTC_PARAMETER_LOG_FILE_KEY = "logfile";
 	
 	/**
 	 * 
 	 * This constant contains the identifier for the parameter which says if we use OMERO database for loading users
 	 * 
 	 * */
 	public static final String OTC_PARAMETER_USE_DB_KEY = "LoadUsersFromDB";
 	
 	/**
 	 * 
 	 * This constant contains the identifier for the parameter which gives the path in which HTML files should be in OMERO server
 	 * 
 	 * */
 	public static final String OTC_PARAMETER_HTML_FILE_SERVER_PATH = "OmeroHtmlDestinationPath";
 	
 	/**
 	 * 
 	 * This constant contains the identifier for the parameter which gives the listening port
 	 * 
 	 * */
 	public static final String OTC_PARAMETER_LISTENING_PORT = "OmeroPort";
 
 }
