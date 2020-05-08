 package edu.psu.iam.cpr.ip.ui.common;
 
 public final class UIConstants 
 {
 	public static final String UI_PROPERTY_FILE = "ui_properties.txt";  
 	
 	public static final String HELP_DESK_RESET_PSWD_HEADER     =  "ui.pswd.help.desk.reset.pswd.header";
 	
 	public static final String HELP_DESK_RESET_PSWD_TEXT = "ui.pswd.help.desk.must.reset.pswd";
 	
 	/* In core (application memory) high use database rows */
 	public static final String IN_CORE_COUNTRY_MAP_KEY  = "ip.country.map";
 	public static final String IN_CORE_STATE_MAP_KEY    = "ip.state.map"  ;
 	public static final String IN_CORE_GENDER_MAP_KEY   = "ip.gender.map";
 	
 	// This table mirrors the flow a user goes thru as they utilize the system
 	public static final String USER_ACTION_FLOW = "iam.ui.ip.prefix.list";
 
 	// Note: This IS key prefix -- the rest is built in ProxyDb
 	public static final String IN_CORE_RA_MAP_KEY_PREFIX       = "ip.ra.map"     ;
 
 	// Note: This IS key prefix -- the rest is built in ProxyDb
 	public static final String IN_CORE_RA_SCREEN_KEY_PREFIX    = "ip.ra.screens"     ;
 	
 	// Maximum number of tries to answer security questions 
 	public static final int    MAX_SECURITY_QUESTION_RETRIES = 3;
 	
 	public static final String SECURITY_QUESTION_ANSWER_INCORRECT = "ui.incorrect.security.answer.try.again";
 	
 	// Password Service Name 
 	public static final String PASSWORD_SERVICE_NAME = "SetPassword";
 
 	// Security Questions
 	public static final String IN_CORE_SECURITY_QUESTIONS = "ip.security.questions";
 	
 	// Front-end wants to know how deep into the processs we are, for progress meter
 	public static final String TOTAL_SCREENS_FOR_RA   = "rac.screenTotal";
 	public static final String CURRENT_SCREEN_NUMBER  = "rac.screenNumber";
 	public static final String SCREEN_POSITION_PREFIX = "ip.screen.position.";
 	public static final String RA_TOTAL_SECREENS      = "ip.screen.position.total.screens"; 
 	
 	/* Once a person agrees to policy, or answers security questions, 
 	 * data prior to that cannot be changed -- it's locked down
 	 */
 	public static final String DATA_LOCKDOWN_KEY      =  "pol.agree";
 	
 	// On entry to what screen should previously entered data be locked down
 	public static final String DATA_LOCK_ACTION_NAME  = "ip.datalock.action.name";
 	
 	// Navigation Map Key for session 
 	public static final String NAVIGATION_KEY = "nav.navigation.map";
 	
 	// Keys for retrieving EmailMsgUI objects from database during UIInitilization
 	public static final String EMAIL_ACCOUNT_CREATION_KEY = "Account Creation";
 	public static final String EMAIL_PASSWORD_RESET_KEY   = "Password Reset"  ;
 	
 	// Performance timings List
 	public static final String PERFORMANCE_DATA_KEY = "performance.data.list";
 	
 	/** Contains the name of the property containing the value of the CPR mode.  This property is set by Tomcat. */
 	public static final String CPR_MODE_PROPERTY = "edu.psu.iam.cpr.core.mode";
 	
 	// Reproduce the running mode for the front-end
 	public static final String RUNNING_MODE      = "running.mode";
 	
 	// RA Connection Constants
 	public static final String RAC_PRINCIPAL_ID = "rac.principalId";
 	public static final String RAC_PASSWORD     = "rac.password"    ;
 	public static final String RAC_AUTHENTICATED= "ra_authenticated";
 	
 	// The order of screens provided for the front-end
 	public static final String RA_SCREEN_ORDER = "rac.screen.order.list";
 	
 	// Application Name Property name
 	public static final String UI_APPLICATION_NAME = "ui.application.name";
 	
 	// Registration Authority Property names returned from LDAP Authentication 
 	public static final String RA_CREATE_USER_ID = "RA_CREATE_USER_ID";
 	public static final String RA_CREATE_PSUID   = "RA_CREATE_PSUID";
 	public static final String RA_NAME           = "RA_NAME";
 	public static final String RA_RETURN_URL     = "RA_RETURN_URL";
 	public static final String RA_HOME_URL       = "RA_HOME_URL";
 	public static final String RA_REQUESTED_BY   = "RA_REQUESTED_BY";
 	public static final String RA_SHOW_SSN       = "RA_SHOW_SSN";
 	public static final String RA_SSN_MSG        = "RA_SSN_MSG";
 	public static final String RA_MSG            = "RA_MSG";
 	public static final String RA_EMAIL_MSG      = "RA_EMAIL_MSG";
 	
 	// Registration Authority Failed Constants
 	public static final String UI_RA_AUTH_FAILED_HEADER = "ui.ra_auth_failed.header";
 	
 	// Database Exception Constants
 	public static final String EXCEPTION_DATABASE_CONNECTION_HEADER = "exception.database.connection.header";
 	public static final String EXCEPTION_DATABASE_CONNECTION_MESSAGE_COUNT = "exception.database.connection.message.count";
 	
 	// Other Exception Constants
 	public static final String UI_ERROR_CALL_CUST_SERVICES = "ui.error.call.cust.services";
 	
 	// Option requesting simple unique id numbering
 	public static final String TRANSACTION_ID_NUMBERING = "transaction.id.numbering"; 
 	
 	// Unique Id Suffix for re-mapping HttpSession Id(s)
 	public static final String SESSION_UNIQUE_ID_SUFFIX = "session.unique.id.suffix";
 	
 	// Unique Id session key id
 	public static final String SESSION_NEW_UNIQUE_ID = "short.session.id";
 	
 	// Session name for storing the generated test SSN -- only available in TEST and ACCEPTANCE
 	public static final String TEST_SSN  = "test.ssn";
 	
 	/** Constants for SoapClient and SoapClientIP */
 	public static final String IDENTIFIER_TYPE 	 		= "identifierType";
 	public static final String IDENTIFIER 			 	= "identifier";
 	public static final String AFFILIATION 		 		= "affiliation";
 	public static final String INTERNATIONAL_NUMBER 	= "internationalNumber";
 	public static final String EXTENSION 				= "extension";
 	public static final String EMAIL_TYPE 				= "emailType";
 	public static final String EMAIL 					= "email";
 	public static final String PHONE_TYPE 				= "phoneType";
 	public static final String PHONE_GROUP_ID           = "phoneGroupId";
 	public static final String PHONE_NUMBER 			= "phoneNumber";
 	public static final String CAMPUS 					= "campus";
 	public static final String SUFFIX 					= "suffix";
 	public static final String NICKNAME					= "nickname";
 	public static final String ADDRESS_TYPE 			= "addressType";
 	public static final String ADDRESS_DOCUMENT_TYPE 	= "addressDocumentType";
 	public static final String ADDRESS_GROUP_ID         = "addressGroupId";
 	public static final String NAME_TYPE 				= "nameType";
 	public static final String NAME_DOCUMENT_TYPE 		= "nameDocumentType";
 	public static final String ASSIGN_USER_ID 			= "assignUserId";
 	public static final String ASSIGN_PSU_ID 			= "assignPsuId";
 	public static final String STATE_OR_PROV 			= "stateOrProv";
 	public static final String POSTAL_CODE 				= "postalCode";
 	public static final String DOB 						= "dob";
 	public static final String GENDER                   = "gender";
 	public static final String COUNTRY 					= "country";
 	public static final String MIDDLE_NAMES 			= "middleNames";
 	public static final String CITY 					= "city";
 	public static final String ADDRESS3 				= "address3";
 	public static final String ADDRESS2 				= "address2";
 	public static final String ADDRESS1 				= "address1";
 	public static final String SSN 						= "ssn";
 	public static final String LAST_NAME 				= "lastName";
 	public static final String FIRST_NAME 				= "firstName";
 	public static final String REQUESTED_BY 			= "requestedBy";
 	public static final String PRINCIPAL_ID 			= "principalId";
 	public static final String PASSWORD 				= "password";
 	public static final String CODE 					= " code: ";
 	
 	/** Session fields needed across multiple screens */
 	public static final String RAC_ASSIGN_PSU_ID        = "rac.assignPsuId";
 	public static final String RAC_ASSIGN_USER_ID       = "rac.assignUserId";
 	
 	public static final String LNA_FIRST_NAME           = "lna.firstName";
 	public static final String LNA_MIDDLE_NAMES         = "lna.middleNames";
 	public static final String LNA_LAST_NAME            = "lna.lastName";
 	public static final String LNA_SUFFIX               = "lna.suffix";
	public static final String LNA_NICKNAME				= "lna.nickName";
 
 	public static final String IDI_SOCIAL_SECURITY_NUMBER = "idi.socialSecurityNumber";
 	
 	public static final String PER_BIRTH_MONTH          = "per.birthMonth";
 	public static final String PER_BIRTH_DAY            = "per.birthDay";
 	public static final String PER_BIRTH_YEAR           = "per.birthYear";
 	public static final String PER_GENDER               = "per.gender";
 
 	public static final String CRA_ADDRESS_TYPE         = "cra.addressType";
 	public static final String CRA_ADDRESS_LINE1        = "cra.addressLine1";
 	public static final String CRA_ADDRESS_LINE2        = "cra.addressLine2";
 	public static final String CRA_ADDRESS_LINE3        = "cra.addressLine3";
 	public static final String CRA_CITY                 = "cra.city";
 	public static final String CRA_COUNTRY              = "cra.country";
 	public static final String CRA_STATE                = "cra.state";
 	public static final String CRA_PROVINCE             = "cra.province";
 	public static final String CRA_POSTAL_CODE          = "cra.postalCode";
 	public static final String CRA_ADDRESS_DOCUMENT_TYPE= "cra.addressDocumentType";
 	public static final String CRA_ADDRESS_GROUP_ID     = "cra.addressGroupId";
 
 	public static final String CON_PHONE_TYPE           = "con.phoneType";
 	public static final String CON_PHONE_GROUP_ID       = "con.phoneGroupId";
 	public static final String CON_PHONE_NUMBER         = "con.phoneNumber";
 	public static final String CON_EXTENSION            = "con.extension";
 	public static final String CON_INTERNATIONAL_NUMBER = "con.internationalNumber";
 	public static final String CON_EMAIL_TYPE           = "con.emailType";
 	public static final String CON_EMAIL                = "con.email";
 	
 	public static final String FMR_FIRST_NAME           = "fmr.firstName";
 	public static final String FMR_LAST_NAME            = "fmr.lastName";
 	
 	public static final String RAC_REQUESTED_BY         = "rac.requestedBy";
 	
 	public static final String IDI_PENN_STATE_ID        = "idi.pennStateId";
 	public static final String IDI_USER_ID              = "idi.userId";
 	
 	public static final String PRA_ADDRESS_LINE1        = "pra.addressLine1";
 	public static final String PRA_ADDRESS_LINE2        = "pra.addressLine2";
 	public static final String PRA_ADDRESS_LINE3        = "pra.addressLine3";
 	public static final String PRA_STATE                = "pra.state";
 	public static final String PRA_PROVINCE             = "pra.province";
 	public static final String PRA_POSTAL_CODE          = "pra.postalCode";
 	public static final String PRA_COUNTRY              = "pra.country";
 	
 	// Key to access the security question they selected in a past iteration
 	public static final String SEC_PREVIOUS_SECURITY_QUESTION = "sec.previous.security.question";
 	
 	// Key to access the security answer previously recorded
 	public static final String SEC_PREVIOUS_RECORDED_ANSWER = "sec.previous.recorded.answer";
 	
 	// key for Incorrect Answer to a security question message
 	public static final String SECURITY_QUESTION_INCORRECT_ANSWER = "security.question.incorrect.answer";
 	
 	// Key for Failed to Satisfy security question answers prior to reaching threshold
 	public static final String SECURITY_QUESTION_ANSWER_FAILURE = "security.question.answer.failure";
 	
 	// Option in UI/IP Properites requesting  all 3 challenge question answers on one screen, or one per screen
 	public static final String LOGIC_SECURITY_QUESTIONS_PERSON_FOUND = "logic.security.questions.person.found";
 	
 	// SecurityQuestionAction message about answering all of the questions
 	public static final String ERROR_YOU_MUST_ANSWER_ALL_QUESTIONS = "ui.error.security.questions.must.answer.all";
 	
 	// Some RA(s) may allow email address to be userid, so let's pre-populate userid as email in those instances
 	public static final String POPULATE_USERID_WITH_EMAIL = "populate.userid.with.email.address";
 	
 	// The letter 'N'
 	public static final String LETTER_N = "N";
 	
 	// The letter 'Y'
 	public static final String LETTER_Y = "Y";
 	
 	// Key - In seconds, how long to delay before deleting individual session object
 	public static final String SESSION_DELETE_DELAY_IN_SECONDS = "delete.session.delay.in.seconds";
 	
 	// Application key retrieving/setting whether or not the TestSSN feature is enabled
 	public static final String TEST_SSN_FEATURE = "TestSSN.feature";
 	
 	// Undefined Struts Action requested either by end-user, or programmed Struts action class
 	public static final String _404_UNKNOWN_ACTION = "404 unknown action";
 	
 	// 404 unknown action header
 	public static final String _404_UNKOWN_ACTION_HEADER = "ui.404.error.header";
 	
 	// Error message key for invalid userid on Identity[idi] Information screen
 	public static final String IDI_INVALID_USERID = "userid.is.invalid";
 	
 	// Error message key for invalid SSN on Identity[idi] Information screen
 	public static final String IDI_INVALID_SSN = "ssn.is.invalid";
 	
 	// Error message key for invalid SSN on Identity[idi] Information screen
 	public static final String IDI_INVALID_ENTITY_ID = "entity.id.is.invalid";
 	
 	// Regex pattern for validating userid on Identity Information Screen [idi]
 	public static final String IDI_REGEX_KEY_USERID = "userid.regex";
 	
 	// Regex pattern for validating entity id (Penn State id) on Identity Information Screen [idi]
 	public static final String IDI_REGEX_KEY_ENTITY_ID    = "entity.id.regex"   ;
 	
 	// Regex pattern for validating SSN on Identity Information Screen [idi]
 	public static final String IDI_REGEX_KEY_SSN    = "ssn.regex"   ; 		
 	
         // Indicates whether or not a SSN has been specified as input --
         public static final String SSN_FLAG = "vfy.ssnFlag";
 
 	/**
 	 * Prevent or make it difficult for someone to instantiate this utility class
 	 */
 	private UIConstants()  { }
 }
