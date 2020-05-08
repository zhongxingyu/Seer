 /**
  * Copyright (C) 2000 - 2009 Silverpeas
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * As a special exception to the terms and conditions of version 3.0 of
  * the GPL, you may redistribute this Program in connection with Free/Libre
  * Open Source Software ("FLOSS") applications as described in Silverpeas's
  * FLOSS exception.  You should have recieved a copy of the text describing
  * the FLOSS exception, and it is also available here:
  * "http://repository.silverpeas.com/legal/licensing"
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.silverpeas.installedtree;
 
 import java.io.File;
 
 /**
  * Titre :        Application Builder
  * Description :
  * Copyright :    Copyright (c) 2001
  * Socit :      Stratlia
  * @author Jean-Christophe Carry
  * @version 1.0
  */
 
 public class DirectoryLocator {
 
 	/* CONSTANTS */
 
 	// Application level
 	private static final String APPLICATION_HOME_SUBDIR = "silverpeas";
 
 	// Sublevel 1
 	private static final String PROPERTIES_HOME_SUBDIR = "properties";
 	private static final String HELP_HOME_SUBDIR = "help"+File.separator+"fr";
 	private static final String LOG_SUBDIR = "log";
 	private static final String LIB_SUBDIR = "jar";
 	private static final String REPOSITORY_SUBDIR = "repository";
 	private static final String VERSION_SUBDIR = "version";
 	private static final String TEMP_SUBDIR = "temp";
 
 	// Contributed pieces Sublevels (repository sublevels)
 	private static final String WAR_CONTRIB_SUBDIR = "war";
 	private static final String CLIENT_CONTRIB_SUBDIR = "client";
 	private static final String LIB_CONTRIB_SUBDIR = "java";
 	private static final String CONTRIB_FILES_SUBDIR = "data";
 	private static final String EJB_CONTRIB_SUBDIR = "ejb";
 
 	/* MEMBERS */
 
 	// Install location
 	private static String silverpeasHome = null;
 	// Application level
 	private static String applicationHome = null;
 	// Sublevel 1
 	private static String propertiesHome = null;
 	private static String helpHome = null;
 	private static String logHome = null;
 	private static String libHome = null;
 	private static String repositoryHome = null;
 	private static String versionHome = null;
 	private static String tempHome = null;
 	
 	// Contributed pieces locations
 	private static String warContribHome = null;
 	private static String clientContribHome = null;
 	private static String libContribHome = null;
 	private static String ejbContribHome = null;
 	private static String contribFilesHome = null;
 
 	/**
 	 * @return the Silverpeas install location
 	 */
 	public static String getSilverpeasHome() {
 		if (silverpeasHome == null) {
 			silverpeasHome = SilverpeasHomeResolver.getHome();
 		}
 		return silverpeasHome;
 	}
 
 	/**
 	 * @return the root directory of Silverpeas installed tree. the parent of 'bin',
 	 * 'properties', etc.
 	 */
 	static public String getApplicationHome() {
 		if (applicationHome == null) {
			applicationHome = getSilverpeasHome() + File.separator + APPLICATION_HOME_SUBDIR;
 		}
 		return applicationHome;
 	}
 
 	/**
 	 * @return the root directory of the properties tree
 	 */
 	public static String getPropertiesHome() {
 		if (propertiesHome == null) {
 			propertiesHome = getApplicationHome() + File.separator + PROPERTIES_HOME_SUBDIR;
 		}
 		return propertiesHome;
 	}
 
 	/**
 	 * @return a map of the help paths (String) indexed by the locales("fr", "en", "de", ...)
 	 */
 	public static String getHelpHome() {
 		if (helpHome == null) {
 			helpHome = getApplicationHome()+File.separator+HELP_HOME_SUBDIR;
 		}
 		return helpHome;
 	}
 
 	/**
 	 * @return the log directory
 	 */
 	public static String getLogHome() {
 		if (logHome == null) {
 			logHome = getApplicationHome()+File.separator+LOG_SUBDIR;
 		}
 		return logHome;
 	}
 
 	/**
 	 * @return the version directory
 	 */
 	public static String getVersionHome() {
 		if (versionHome == null) {
 			versionHome = getApplicationHome()+File.separator+VERSION_SUBDIR;
 		}
 		return versionHome;
 	}
 
 	/**
 	 * @return the jar directory
 	 */
 	public static String getLibraryHome() {
 		if (libHome == null) {
 			libHome = getApplicationHome()+File.separator+LIB_SUBDIR;
 		}
 		return libHome;
 	}
 
 	/**
 	 * @return the temp directory
 	 */
 	public static String getTempHome() {
 		if (tempHome == null) {
 			tempHome = getApplicationHome()+File.separator+TEMP_SUBDIR;
 		}
 		return tempHome;
 	}
 
 	/**
 	 * @return the repository directory
 	 */
 	public static void setRepositoryHome(String repository) {
 		repositoryHome = getApplicationHome()+File.separator+repository;
 		warContribHome = getRepositoryHome()+File.separator+WAR_CONTRIB_SUBDIR;
 		clientContribHome = getRepositoryHome()+File.separator+CLIENT_CONTRIB_SUBDIR;
 		libContribHome = getRepositoryHome()+File.separator+LIB_CONTRIB_SUBDIR;
 		ejbContribHome = getRepositoryHome()+File.separator+EJB_CONTRIB_SUBDIR;
 		contribFilesHome = getRepositoryHome()+File.separator+CONTRIB_FILES_SUBDIR;
 	}
 
 	/**
 	 * @return the repository directory
 	 */
 	public static String getRepositoryHome() {
 		if (repositoryHome == null) {
 			repositoryHome = getApplicationHome()+File.separator+REPOSITORY_SUBDIR;
 		}
 		return repositoryHome;
 	}
 
 	/**
 	 * @return the war contributions directory
 	 */
 	public static String getWarContribHome() {
 		if (warContribHome == null) {
 			warContribHome = getRepositoryHome()+File.separator+WAR_CONTRIB_SUBDIR;
 		}
 		return warContribHome;
 	}
 
 	/**
 	 * @return the client contributions directory
 	 */
 	public static String getClientContribHome() {
 		if (clientContribHome == null) {
 			clientContribHome = getRepositoryHome()+File.separator+CLIENT_CONTRIB_SUBDIR;
 		}
 		return clientContribHome;
 	}
 
 	/**
 	 * @return the library contibutions directory
 	 */
 	public static String getLibContribHome() {
 		if (libContribHome == null) {
 			libContribHome = getRepositoryHome()+File.separator+LIB_CONTRIB_SUBDIR;
 		}
 		return libContribHome;
 	}
 
 	/**
 	 * @return the EJB contributions directory
 	 */
 	public static String getEjbContribHome() {
 		if (ejbContribHome == null) {
 			ejbContribHome = getRepositoryHome()+File.separator+EJB_CONTRIB_SUBDIR;
 		}
 		return ejbContribHome;
 	}
 
 	/**
 	 * @return the contribution XML files directory
 	 */
 	public static String getContribFilesHome() {
 		if (contribFilesHome == null) {
 			contribFilesHome = getRepositoryHome()+File.separator+CONTRIB_FILES_SUBDIR;
 		}
 		return contribFilesHome;
 	}
 
 }
