 package com.mymed.controller.core.manager;
 
import com.mymed.utils.MyMedLogger;
 
 /**
  * Simple class to hold generic strings that can be used with all the managers.
  * We hold the names of the columns families and of the super columns.
  * <p>
  * It contains also the log facilities for all the managers.
  * 
  * @author Milo Casagrande
  * 
  */
public class ManagerValues extends MyMedLogger {
 	// Column families
 	/**
 	 * The 'Application' column family
 	 */
 	protected static final String CF_APPLICATION = "Application";
 
 	/**
 	 * The 'ApplicationView' column family
 	 */
 	protected static final String CF_APPLICATION_VIEW = "ApplicationView";
 
 	/**
 	 * The 'Authentication' column family
 	 */
 	protected static final String CF_AUTHENTICATION = "Authentication";
 
 	/**
 	 * The 'Interaction' column family
 	 */
 	protected static final String CF_INTERACTION = "Interaction";
 
 	/**
 	 * The 'Ontology' column family
 	 */
 	protected static final String CF_ONTOLOGY = "Ontology";
 
 	/**
 	 * The 'OntologyType' column family
 	 */
 	protected static final String CF_ONTOLOGY_TYPE = "OntologyType";
 
 	/**
 	 * The 'Reputation' column family
 	 */
 	protected static final String CF_REPUTATION = "Reputation";
 
 	/**
 	 * The 'Session' column family
 	 */
 	protected static final String CF_SESSION = "Session";
 
 	/**
 	 * The 'User' column family
 	 */
 	protected static final String CF_USER = "User";
 
 	// Super columns
 	/**
 	 * The 'ApplicationController' super column
 	 */
 	protected static final String SC_APPLICATION_CONTROLLER = "ApplicationController";
 
 	/**
 	 * The 'ApplicationList' super column
 	 */
 	protected static final String SC_APPLICATION_LIST = "ApplicationList";
 
 	/**
 	 * The 'ApplicationModel' super column
 	 */
 	protected static final String SC_APPLICATION_MODEL = "ApplicationModel";
 
 	/**
 	 * The 'UserList' super column
 	 */
 	protected static final String SC_USER_LIST = "UserList";
 
 	/**
 	 * The 'InteractionList' super column
 	 */
 	protected static final String SC_INTERACTION_LIST = "InteractionList";
 
 	/**
 	 * The 'RaterList' super column
 	 */
 	protected static final String SC_RATER_LIST = "RaterList";
 }
