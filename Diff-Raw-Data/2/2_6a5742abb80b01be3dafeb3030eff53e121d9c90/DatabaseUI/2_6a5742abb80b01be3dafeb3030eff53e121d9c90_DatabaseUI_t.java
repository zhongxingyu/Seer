 /* SVN FILE: $Id: DatabaseUI.java 6495 2013-03-13 17:40:30Z dvm105 $ */
 package edu.psu.iam.cpr.core.ui;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.Query;
 
 import edu.psu.iam.cpr.core.database.Database;
 import edu.psu.iam.cpr.core.database.beans.Country;
 import edu.psu.iam.cpr.core.database.beans.DataTypes;
 import edu.psu.iam.cpr.core.database.beans.EmailNotification;
 import edu.psu.iam.cpr.core.database.beans.RaApplicationProperties;
 import edu.psu.iam.cpr.core.database.beans.RaApplications;
 import edu.psu.iam.cpr.core.database.beans.RaScreenFields;
 import edu.psu.iam.cpr.core.database.beans.RaScreens;
 import edu.psu.iam.cpr.core.database.beans.RaServerPrincipals;
 import edu.psu.iam.cpr.core.database.beans.RegistrationAuthority;
 import edu.psu.iam.cpr.core.database.beans.SecurityQuestionAnswers;
 import edu.psu.iam.cpr.core.database.beans.SecurityQuestions;
 import edu.psu.iam.cpr.core.database.beans.UiApplications;
 import edu.psu.iam.cpr.core.database.beans.UspsStates;
 import edu.psu.iam.cpr.core.database.types.AccessType;
 import edu.psu.iam.cpr.core.error.CprException;
 import edu.psu.iam.cpr.core.error.ReturnType;
 
 /**
  * Database is a utility class that will facility the opening and closing of database connections
  * to the database pool that maintained in the broker.  In addition it provides helper methods
  * that are used to obtain person identifiers from the CPR.
  * 
  * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 United States License. To 
  * view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/us/ or send a letter to Creative 
  * Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
  *
  * @package edu.psu.iam.cpr.core.database
  * @author $Author: dvm105 $
  * @version $Rev: 6495 $
  * @lastrevision $Date: 2013-03-13 13:40:30 -0400 (Wed, 13 Mar 2013) $
  */
 
 public class DatabaseUI extends Database {
 	
 	
 	private static final int NUMBER_OF_SECURITY_QUESTIONS = 3;
 	private static final int SECURITY_QUESTION_0 = 0;
 	private static final int SECURITY_QUESTION_1 = 1;
 	private static final int SECURITY_QUESTION_2 = 1;
 	private static final int UI_APPLICATION = 0;
 	private static final int RETURN_LIST_SIZE = 1;
 	private static final int GET_ITEM_0 = 0;
 	
 	private Map<Long, String> raMap = null;
 	private Map<Long, List<String>> raScreenMap = new HashMap<Long, List<String>>();
 	
 	/**
 	 * This routine will fetch the list of states from the database
 	 * @return List of key-value pairs for the two digit state code and the state name
 	 */
 	public List<Pair<String, String>> getStateList()  {
 
 		final List<Pair<String, String>> stateList = new ArrayList<Pair<String, String>>();
 
 		// Pull all the states from the database
 		final String sqlQuery = "from UspsStates ORDER BY stateName";
 		final Query query = getSession().createQuery(sqlQuery);
 		for (final Iterator<?> it = query.list().iterator(); it.hasNext(); ) {
 			UspsStates dbBean = (UspsStates) it.next();
 			stateList.add(new Pair<String, String>(dbBean.getStateCode(), dbBean.getStateName()));
 		}
 
 		return stateList;	
 	}
 	
 	/**
 	 * This routine will fetch the list of countries from the database
 	 * @return List of key-value pairs for the three digit country code and the country na
 	 */
 	public List<Pair<String, String>> getCountryList() {
 
 		List<Pair<String, String>> countryList = new ArrayList<Pair<String, String>>();
 
 		// Pull all the countries from the database
 		final String sqlQuery = "from Country where usTerritoryFlag = 'N' AND endDate IS NULL ORDER BY country";
 		final Query query = getSession().createQuery(sqlQuery);
 		for (final Iterator<?> it = query.list().iterator(); it.hasNext(); ) {
 			Country dbBean = (Country) it.next();
 			countryList.add(new Pair<String, String>(dbBean.getCountryCodeThree(), dbBean.getCountry()));
 		}
 
 		return countryList;	
 	}
 	
 	/**
 	 * This routine will fetch the list of genders from the database
 	 * @return List of key-value pairs for the gender enum string and the gender description
 	 */
 	public List<Pair<String, String>> getGenderList()  {
 
 		final List<Pair<String, String>> genderList = new ArrayList<Pair<String, String>>();
 
 		// Pull all the genders from the dataTypes table in the database
 		final String sqlQuery = "from DataTypes where parentDataTypeKey = :gender AND canAssignFlag = 'Y' AND activeFlag = 'Y' ORDER BY dataTypeDesc";
 		final Query query = getSession().createQuery(sqlQuery);
 		query.setParameter("gender", AccessType.GENDER.index());
 		for (final Iterator<?> it = query.list().iterator(); it.hasNext(); ) {
 			DataTypes dbBean = (DataTypes) it.next();
 			genderList.add(new Pair<String, String>(dbBean.getEnumString(), dbBean.getDataTypeDesc()));
 		}
 
 		return genderList;	
 	}
 	
 	
 	/**
 	 * This routine will fetch the list of types from the database for the specified parent type
 	 * @param parentTypeIndex contains the index of the parent type
 	 * @return Map of key-value pairs for the type enum key and the type enum string
 	 * @throws CprException 
 	 
 	 */
 	public Map<Long, String> getTypeList(long parentTypeIndex) throws CprException {
 
 		if (AccessType.get(parentTypeIndex) == null) {
 			throw new CprException(ReturnType.GENERAL_DATABASE_EXCEPTION ,"No type exists for the index " + parentTypeIndex);
 		}
 		final Map<Long, String> typeList = new HashMap<Long, String>();
 
 		// Pull all the email types from the dataTypes table in the database
 		final String sqlQuery = "from DataTypes where parentDataTypeKey = :parentTypeIndex AND canAssignFlag = 'Y' AND activeFlag = 'Y'";
 		final Query query = getSession().createQuery(sqlQuery);
 		query.setParameter("parentTypeIndex", parentTypeIndex);
 		for (final Iterator<?> it = query.list().iterator(); it.hasNext(); ) {
 			DataTypes dbBean = (DataTypes) it.next();
 			typeList.put(dbBean.getDataTypeKey(), dbBean.getEnumString());
 		}
 		return typeList;	
 	}
 	
 	/**
 	 * This routine will fetch a specific group of security questions from the database
 	 * @param groupNum contains the list number for the group to be fetched
 	 * @return List of key-value pairs for the security question key and the question text
 	 */
 	public List<Pair<String, String>> getSecurityQuestionList(long groupNum) {
 
 		final List<Pair<String, String>> securityQuestList = new ArrayList<Pair<String, String>>();
 
 		// Pull all the security questions associated with the given group from the securityQuestions table in the database
 		final String sqlQuery = "from SecurityQuestions where secQuestGroupKey = :groupNum AND endDate IS NULL";
 		final Query query = getSession().createQuery(sqlQuery);
 		query.setParameter("groupNum", groupNum);
 		for (final Iterator<?> it = query.list().iterator(); it.hasNext(); ) {
 			SecurityQuestions dbBean = (SecurityQuestions) it.next();
 			securityQuestList.add(new Pair<String, String>(dbBean.getSecQuestKey().toString(), dbBean.getQuestion()));
 		}
 
 		return securityQuestList;	
 	}   
     
 	/**
 	 * This routine will fetch a specific user's set of security questions and answers from the database
 	 * @param personId contains the identifier for the person to be fetched
 	 * @param userId contains the identifier for the person's user to be fetched
 	 * @return List of key-value pairs for the security question key and the question text
 	 */
 	public List<SecurityQuestionAnswers> getUserSecurityQuestionAnswerList(long personId, String userId) {
 
 		final List<SecurityQuestionAnswers> securityQuestList = new ArrayList<SecurityQuestionAnswers>();
 
 		// Pull all the security questions associated with the given user from the securityQuestionAnswers table in the database
		final String sqlQuery = "from SecurityQuestionAnswers where personId = :personId AND userId = :userId order by secQuestGroupKey ASC";
 		final Query query = getSession().createQuery(sqlQuery);
 		query.setParameter("personId", personId);
 		query.setParameter("userId", userId);
 		for (final Iterator<?> it = query.list().iterator(); it.hasNext(); ) {
 			SecurityQuestionAnswers dbBean = (SecurityQuestionAnswers) it.next();
 			securityQuestList.add(dbBean);
 		}
 
 		return securityQuestList;	
 	}
 	
 	/**
 	 * This routine will save a specific user's set of security questions and answers to the database
 	 * @param securityQuestList contains the list of securityQuestionAnswers objects to store
 	 * @throws CprException 
 	 */
 	public void setUserSecurityQuestionAnswers(final List<SecurityQuestionAnswers> securityQuestList) throws CprException  {
 
 
 		// Make sure there are 3 objects in the list
 		if (securityQuestList.size() != NUMBER_OF_SECURITY_QUESTIONS) {
 			throw new CprException(ReturnType.GENERAL_DATABASE_EXCEPTION, "Must have 3 security question answers to store in database. Was given " + securityQuestList.size());
 		}
 		
 		// Make sure personId and userId match for all 3 answers
 		final long personId = securityQuestList.get(SECURITY_QUESTION_0).getPersonId();
 		if (personId != securityQuestList.get(SECURITY_QUESTION_1).getPersonId() || personId != securityQuestList.get(SECURITY_QUESTION_2).getPersonId()) {
 			throw new CprException(ReturnType.GENERAL_DATABASE_EXCEPTION,"PersonId is not equal for all security question answers.");
 		}
 		
 		final String userId = securityQuestList.get(SECURITY_QUESTION_0).getUserid();
 		if (!userId.equals(securityQuestList.get(SECURITY_QUESTION_1).getUserid()) || !userId.equals(securityQuestList.get(SECURITY_QUESTION_2).getUserid())) {
 			throw new CprException(ReturnType.GENERAL_DATABASE_EXCEPTION,"userId is not equal for all security question answers.");
 		}
 
 		// Delete any existing questions stored in the database
 		final List<SecurityQuestionAnswers> existingSecurityQuestList = getUserSecurityQuestionAnswerList(personId, userId);
 		for (final Iterator<?> it = existingSecurityQuestList.iterator(); it.hasNext(); ) {
 			SecurityQuestionAnswers dbBean = (SecurityQuestionAnswers) it.next();
 			getSession().delete(dbBean);
 		}
 		
 		// Save all the security questions associated with the given user to the securityQuestionAnswers table in the database
 		for (final Iterator<?> it = securityQuestList.iterator(); it.hasNext(); ) {
 			SecurityQuestionAnswers dbBean = (SecurityQuestionAnswers) it.next();
 			getSession().save(dbBean);
 		}
 	}
 	
 	/**
 	 * This routine will fetch from the database the list of RA application properties associated with a specific UI application
 	 * @param uiApplicationName contains the identifier for the application to be fetched
 	 * @param principalId contains the identifier for the principal to be fetched
 	 * @return Map of key-value pairs of application properties for the RA
 	 * @throws CprException 
 	 */
 	public Map<String, String> getRaApplicationProperties(final String uiApplicationName, final String principalId) throws CprException {
 		
 		// Look-up the Application key
 		final long uiApplicationKey = getAppKey(uiApplicationName);
 		
 		final long raApplicationKey = getRaAppKey(uiApplicationKey, principalId);
 		
 		final Map<String, String> raPropertiesHash = new HashMap<String, String>();
 
 		// Pull all the screens associated with the given RA from the ra screens table in the database
 		final String appPropertiesSqlQuery = "from RaApplicationProperties where raApplicationKey = :raApplicationKey AND uiApplicationKey = :uiApplicationKey";
 		final Query propertiesQuery = getSession().createQuery(appPropertiesSqlQuery);
 		propertiesQuery.setParameter("raApplicationKey", raApplicationKey);
 		propertiesQuery.setParameter("uiApplicationKey", uiApplicationKey);
 		for (final Iterator<?> it = propertiesQuery.list().iterator(); it.hasNext(); ) {
 			RaApplicationProperties propertiesDbBean = (RaApplicationProperties) it.next();
 			raPropertiesHash.put(propertiesDbBean.getKeyName(), propertiesDbBean.getKeyValue());
 		}
 
 		return raPropertiesHash;	
 	}
 	
 	/**
 	 * This routine will fetch from the database the list of application properties for all RAs associated with a specific UI application
 	 * @param uiApplicationName contains the identifier for the application to be fetched
 	 * @return HashMap of key-value pairs of RA name to application properties
 	 * @throws CprException
 	 */
 	public Map<String, Map<String, String>> getAllApplicationProperties(String uiApplicationName) throws CprException {
 		
 		// Look-up the Application key
 		long uiApplicationKey = getAppKey(uiApplicationName);
 		
 		Map<Long, Long> raAppsHash = new HashMap<Long, Long>();
 		Map<String, String> raPropertiesHash = null;
 		Map<String, Map<String, String>> raAppPropertiesHash = new HashMap<String, Map<String, String>>();
 
 		// Pull all the ra application properties associated with the given UI application from the ra app properties table in the database
 		final String appPropertiesSqlQuery = "from RaApplicationProperties where uiApplicationKey = :uiApplicationKey ORDER BY ra_application_key";
 		final Query propertiesQuery = getSession().createQuery(appPropertiesSqlQuery);
 		propertiesQuery.setParameter("uiApplicationKey", uiApplicationKey);
 		// Pull all the ra apps associated with the given UI application from the ra app table in the database
 		final String raAppsSqlQuery = "from RaApplications where uiApplicationKey = :uiApplicationKey AND suspendFlag = 'N'";
 		final Query appsQuery = getSession().createQuery(raAppsSqlQuery);
 		appsQuery.setParameter("uiApplicationKey", uiApplicationKey);
 		// loop through all ra applications associated with UI application
 		for (final Iterator<?> it = appsQuery.list().iterator(); it.hasNext(); ) {
 			RaApplications raAppsDbBean = (RaApplications) it.next();
 			raAppsHash.put(raAppsDbBean.getRaApplicationKey(), raAppsDbBean.getRegistrationAuthorityKey());
 		}
 		createRaMap();
 		Long currentRaKey = new Long(0);
 		// loop through all ra application properties associated with UI application
 		for (final Iterator<?> it = propertiesQuery.list().iterator(); it.hasNext(); ) {
 			RaApplicationProperties raAppPropsDbBean = (RaApplicationProperties) it.next();
 			if (!currentRaKey.equals(raAppPropsDbBean.getRaApplicationKey())) {
 				if (raPropertiesHash != null && !raPropertiesHash.isEmpty()) {
 					// get the ra name associated with the currentRaKey
 					String raName = raMap.get(raAppsHash.get(currentRaKey));
 					// finished with current ra, add to hash with ra name
 					raAppPropertiesHash.put(raName, raPropertiesHash);
 				}
 				// set new ra
 				currentRaKey = raAppPropsDbBean.getRaApplicationKey();
 				// create new hash
 				raPropertiesHash = new HashMap<String, String>();
 			}
 			// add entry to current properties hash
 			raPropertiesHash.put(raAppPropsDbBean.getKeyName(), raAppPropsDbBean.getKeyValue());
 		}
 		return raAppPropertiesHash;	
 	}
 	
 	
 	/**
 	 * This routine will fetch from the database the list of screens to display for a specific RA
 	 * @param raApplicationKey contains the identifier for the ra application to be fetched
 	 * @return List of sequenced screen names in the order they should appear
 	 */
 	private List<RaScreens> getRaScreenBeanList(long raApplicationKey) {
 
 		final List<RaScreens> raScreenBeanList = new ArrayList<RaScreens>();
 		
 		// Pull all the screens associated with the given RA from the ra screens table in the database
 		final String screenSqlQuery = "from RaScreens where raApplicationKey = :raApplicationKey ORDER BY raScreenOrder";
 		final Query screenQuery = getSession().createQuery(screenSqlQuery);
 		screenQuery.setParameter("raApplicationKey", raApplicationKey);
 		for (final Iterator<?> it = screenQuery.list().iterator(); it.hasNext(); ) {
 			RaScreens screenDbBean = (RaScreens) it.next();
 			raScreenBeanList.add(screenDbBean);
 		}
 
 		return raScreenBeanList;	
 	}
 	
 	/**
 	 * This routine will fetch from the database the list of screens to display for a specific RA
 	 * @param raApplicationKey contains the identifier for the ra application to be fetched
 	 * @return List of sequenced screen names in the order they should appear
 	 */
 	private List<String> getScreensForRa(long raApplicationKey) {
 		
 		final List<String> raScreenList = new ArrayList<String>();
 
 		// Pull all the screens associated with the given RA from the ra screens table in the database
 		final String screenSqlQuery = "from RaScreens where raApplicationKey = :raApplicationKey ORDER BY raScreenOrder";
 		final Query screenQuery = getSession().createQuery(screenSqlQuery);
 		screenQuery.setParameter("raApplicationKey", raApplicationKey);
 		for (final Iterator<?> it = screenQuery.list().iterator(); it.hasNext(); ) {
 			RaScreens screenDbBean = (RaScreens) it.next();
 			raScreenList.add(screenDbBean.getUiScreenName());
 		}
 
 		return raScreenList;	
 	}
 	
 	/**
 	 * This routine will fetch from the database the list of screens to display for a specific RA
 	 * @param uiApplicationName contains the identifier for the application to be fetched
 	 * @param principalId contains the identifier for the principal to be fetched
 	 * @return List of sequenced screen names in the order they should appear
 	 * @throws CprException
 	 */
 	public List<String> getRaScreenList(String uiApplicationName, String principalId) throws CprException {
 		
 		long raApplicationKey = getRaAppKey(uiApplicationName, principalId);
 		
 		return getScreensForRa(raApplicationKey);	
 	}
 	
 	/**
 	 * This routine will fetch from the database the list of screens to display per RA for a specific UI application
 	 * @param uiApplicationName contains the identifier for the application to be fetched
 	 * @return Map of RA to ordered screen names list
 	 * @throws CprException
 	 */
 	public Map<String, List<String>> getAllScreenLists(String uiApplicationName) throws CprException {
 		
 		// fetch ui_applications with name for key
 		long uiApplicationKey = getAppKey(uiApplicationName);
 		
 		final Map<String, List<String>> screenHashByRa = new HashMap<String, List<String>>();
 		createRaMap();
 		
 		// use ui_application_key to get ra_app_keys
 		// Pull all the ra applications associated with the given ui application from the ra applications table in the database
 		final String appSqlQuery = "from RaApplications where uiApplicationKey = :uiApplicationKey AND suspend_flag = 'N'";
 		final Query appQuery = getSession().createQuery(appSqlQuery);
 		appQuery.setParameter("uiApplicationKey", uiApplicationKey);
 		// loop through all ra applications and add the screens to the map
 		for (final Iterator<?> it = appQuery.list().iterator(); it.hasNext(); ) {
 			RaApplications appDbBean = (RaApplications) it.next();
 			// get the ra name from the map
 			String raName = raMap.get(appDbBean.getRegistrationAuthorityKey());
 			// get the ra screens
 			List<String> raScreens = getScreensForRa(appDbBean.getRaApplicationKey());
 			screenHashByRa.put(raName, raScreens);
 			raScreenMap.put(appDbBean.getRaApplicationKey(), raScreens);
 		}
 		return screenHashByRa;	
 	}
 	
 	/**
 	 * This routine will fetch from the database the list of fields per screen to display per RA for a specific UI application
 	 * @param uiApplicationName contains the identifier for the application to be fetched
 	 * @return Map of RA to ordered ScreenUI objects
 	 * @throws CprException
 	 */
 	public Map<String, List<ScreenUI>> getAllScreenAndFieldLists(String uiApplicationName) throws CprException {
 		
 		// fetch ui_applications with name for key
 		long uiApplicationKey = getAppKey(uiApplicationName);
 		
 		final Map<String, List<ScreenUI>> screenHashByRa = new HashMap<String, List<ScreenUI>>();
 		createRaMap();
 		
 		// use ui_application_key to get ra_app_keys
 		// Pull all the ra applications associated with the given ui application from the ra applications table in the database
 		final String appSqlQuery = "from RaApplications where uiApplicationKey = :uiApplicationKey AND suspend_flag = 'N'";
 		final Query appQuery = getSession().createQuery(appSqlQuery);
 		appQuery.setParameter("uiApplicationKey", uiApplicationKey);
 		// loop through all ra applications and add the screens to the map
 		for (final Iterator<?> it = appQuery.list().iterator(); it.hasNext(); ) {
 			RaApplications appDbBean = (RaApplications) it.next();
 			// get the ra name from the map
 			String raName = raMap.get(appDbBean.getRegistrationAuthorityKey());
 			// get the ra screens
 			List<RaScreens> raScreens = getRaScreenBeanList(appDbBean.getRaApplicationKey());
 			List<ScreenUI> screenList = new ArrayList<ScreenUI>();
 			// loop through all ra screens and add fields to map
 			for (final Iterator<?> iter = raScreens.iterator(); iter.hasNext(); ) {
 				RaScreens screenDbBean = (RaScreens) iter.next();
 				ScreenUI currentScreen = new ScreenUI();
 				currentScreen.setScreenName(screenDbBean.getUiScreenName());
 				currentScreen.setFieldList(getFieldsForRa(screenDbBean.getRaScreenKey(), screenDbBean.getUiScreenName()));
 				screenList.add(currentScreen);
 			}
 			screenHashByRa.put(raName, screenList);
 		}
 		
 		return screenHashByRa;	
 	}
 	
 	/**
 	 * This routine will fetch from the database the list of fields to display for a specific RA screen
 	 * @param raScreenKey contains the identifier for the RA screen to be fetched
 	 * @param uiScreenName contains the name for the screen to be fetched
 	 * @return List of FieldUI objects
 	 */
 	private List<FieldUI> getFieldsForRa(long raScreenKey, String uiScreenName) {
 		
 		String flagYesValue = "Y";
 		
 		final List<FieldUI> raScreenFieldList = new ArrayList<FieldUI>();
 
 		// Pull all the fields associated with the given RA screen from the RA screens fields table in the database
 		final String screenSqlQuery = "from RaScreenFields where raScreenKey = :raScreenKey ORDER BY uiFieldName";
 		final Query screenQuery = getSession().createQuery(screenSqlQuery);
 		screenQuery.setParameter("raScreenKey", raScreenKey);
 		for (final Iterator<?> it = screenQuery.list().iterator(); it.hasNext(); ) {
 			RaScreenFields screenDbBean = (RaScreenFields) it.next();
 			// if the given field should be displayed, add it to the list with the required flag
 			if (screenDbBean.getDisplayFlag().equals(flagYesValue)) {
 				boolean fieldRequired = false;
 				if (screenDbBean.getRequiredFlag().equals(flagYesValue)) {
 					fieldRequired = true;
 				}
 				FieldUI currentField = new FieldUI(screenDbBean.getUiFieldName(), fieldRequired);
 				raScreenFieldList.add(currentField);
 			}
 		}
 
 		return raScreenFieldList;	
 	}
 	
 	/**
 	 * This routine will fetch from the database the list of fields to display for a specific RA screen
 	 * @param uiApplicationName contains the identifier for the application to be fetched
 	 * @param principalId contains the identifier for the principal to be fetched
 	 * @param uiScreenName contains the name of the screen
 	 * @return List of FieldUI objects
 	 * @throws CprException
 	 */
 	public List<FieldUI> getRaFieldList(String uiApplicationName, String principalId, String uiScreenName) throws CprException {
 		
 		long raScreenKey = getRaScreenKey(uiApplicationName, principalId, uiScreenName);
 		
 		return getFieldsForRa(raScreenKey, uiScreenName);
 	}
 	
 	/**
 	 * This routine will fetch from the database the contents of the email message, given a notification process
 	 * @param process contains the process this email notification is for
 	 * @return EmailMsgUI object containing all the information for the particular email message
 	 * @throws CprException
 	 */
 	public EmailMsgUI getEmailContents(String process) throws CprException {
 		
 		final String emailSqlQuery = "from EmailNotification where notificationProcess = :notificationProcess";
 		final Query emailQuery = getSession().createQuery(emailSqlQuery);
 		emailQuery.setParameter("notificationProcess", process);
 		if (emailQuery.list().isEmpty()) {
 			throw new CprException(ReturnType.GENERAL_DATABASE_EXCEPTION,"No email notification found for " + process);
 		}
 		
 		EmailNotification emailDbBean = (EmailNotification)emailQuery.list().get(GET_ITEM_0);
 
 		return new EmailMsgUI(emailDbBean.getEmailSubject(), emailDbBean.getTextBody(), emailDbBean.getHtmlBody(), emailDbBean.getNotificationProcess());
 	}
 	
 	/**
 	 * This routine will fetch from the database the contents of all email messages
 	 * @return List of EmailMsgUI objects containing all the information for the particular email message
 	 * @throws CprException
 	 */
 	public List<EmailMsgUI> getAllEmailContents() throws CprException {
 		
 		final String emailSqlQuery = "from EmailNotification";
 		final Query emailQuery = getSession().createQuery(emailSqlQuery);
 		if (emailQuery.list().isEmpty()) {
 			throw new CprException(ReturnType.GENERAL_DATABASE_EXCEPTION,"No email notifications found");
 		}
 		List<EmailMsgUI> emailList = new ArrayList<EmailMsgUI>();
 		// loop through all notifications and add the msg to the list
 		for (final Iterator<?> it = emailQuery.list().iterator(); it.hasNext(); ) {
 			EmailNotification emailDbBean = (EmailNotification) it.next();
 			EmailMsgUI emailMsg = new EmailMsgUI(emailDbBean.getEmailSubject(), emailDbBean.getTextBody(), emailDbBean.getHtmlBody(), emailDbBean.getNotificationProcess());
 			emailList.add(emailMsg);
 		}
 		return emailList;
 	}
 	
 	/**
 	 * This routine will fetch from the database the key for a specific RA
 	 * @param principalId contains the identifier for the principal to be fetched
 	 * @return key for the specified RA
 	 * @throws CprException 
 	 */
 	private long getRaKey(String principalId) throws CprException  {
 		
 		final String raSqlQuery = "from RaServerPrincipals where raServerPrincipal = :raServerPrincipal";
 		final Query raQuery = getSession().createQuery(raSqlQuery);
 		raQuery.setParameter("raServerPrincipal", principalId);
 		if (raQuery.list().size() != RETURN_LIST_SIZE) {
 			throw new CprException(ReturnType.GENERAL_DATABASE_EXCEPTION,"RA not found for principalId " + principalId);
 		}
 		RaServerPrincipals dbBean = (RaServerPrincipals)raQuery.list().get(GET_ITEM_0);
 		if (dbBean.getEndDate() != null) {
 			throw new CprException(ReturnType.GENERAL_DATABASE_EXCEPTION,"RA not valid.");
 		}
 		
 		return dbBean.getRegistrationAuthorityKey();
 		
 	}
 	
 	/**
 	 * This routine will fetch from the database the key for a specific UI Application
 	 * @param applicationName contains the identifier for the application to be fetched
 	 * @return key for the specified application
 	 * @throws CprException 
 	 */
 	private long getAppKey(String applicationName) throws CprException {
 		
 		final String appSqlQuery = "from UiApplications where applicationName = :applicationName";
 		final Query appQuery = getSession().createQuery(appSqlQuery);
 		appQuery.setParameter("applicationName", applicationName);
 		if (appQuery.list().size() != RETURN_LIST_SIZE) {
 			throw new CprException(ReturnType.GENERAL_DATABASE_EXCEPTION,"Application not found with name " + applicationName);
 		}
 		UiApplications dbBean = (UiApplications)appQuery.list().get(UI_APPLICATION);
 		if (dbBean.getSuspendFlag().equals("Y")) {
 			throw new CprException(ReturnType.GENERAL_DATABASE_EXCEPTION,"UI Application suspended.");
 		}
 		
 		return dbBean.getUiApplicationKey();
 		
 	}
 	
 	/**
 	 * This routine will fetch from the database the key for a specific RA UI Application
 	 * @param uiApplicationName contains the name of the application to be fetched
 	 * @param principalId contains the identifier for the principal to be fetched
 	 * @return key for the specified application
 	 * @throws CprException 
 	 */
 	private long getRaAppKey(String uiApplicationName, String principalId) throws CprException  {
 		
 		// Look-up the Application key
 		long uiApplicationKey = getAppKey(uiApplicationName);
 		
 		return getRaAppKey(uiApplicationKey, principalId);
 	}
 	
 	/**
 	 * This routine will fetch from the database the key for a specific RA UI Application
 	 * @param uiApplicationKey contains the identifier for the application to be fetched
 	 * @param principalId contains the identifier for the principal to be fetched
 	 * @return key for the specified application
 	 * @throws CprException 
 	 */
 	private long getRaAppKey(long uiApplicationKey, String principalId) throws CprException  {
 		
 		// Look-up the RA key
 		long registrationAuthorityKey = getRaKey(principalId);
 		
 		// Look-up the ra_application_key so can get to the RA application data
 		final String raSqlQuery = "from RaApplications where uiApplicationKey = :uiApplicationKey AND registrationAuthorityKey = :registrationAuthorityKey";
 		final Query raQuery = getSession().createQuery(raSqlQuery);
 		raQuery.setParameter("uiApplicationKey", uiApplicationKey);
 		raQuery.setParameter("registrationAuthorityKey", registrationAuthorityKey);
 		if (raQuery.list().size() != RETURN_LIST_SIZE) {
 			throw new CprException(ReturnType.GENERAL_DATABASE_EXCEPTION,"RA application not found for ui_application_key " + uiApplicationKey + " and registration_authority_key " + registrationAuthorityKey);
 		}
 		RaApplications dbBean = (RaApplications)raQuery.list().get(GET_ITEM_0);
 		if (dbBean.getSuspendFlag().equals("Y")) {
 			throw new CprException(ReturnType.GENERAL_DATABASE_EXCEPTION,"RA application suspended.");
 		}
 		
 		return dbBean.getRaApplicationKey();
 	}
 	
 	/**
 	 * This routine will fetch from the database the key for a specific screen name for a specific RA
 	 * @param uiApplicationName contains the identifier for the application to be fetched
 	 * @param principalId contains the identifier for the principal to be fetched
 	 * @param uiScreenName contains the name of the UI screen for the principal to be fetched
 	 * @return key for the specified RA screen
 	 * @throws CprException
 	 */
 	private long getRaScreenKey(String uiApplicationName, String principalId, String uiScreenName) throws CprException {
 		
 		long raApplicationKey = getRaAppKey(uiApplicationName, principalId);
 		return getRaScreenKey(raApplicationKey, uiScreenName);
 	
 	}
 	
 	/**
 	 * This routine will fetch from the database the key for a specific screen name for a specific RA
 	 * @param raApplicationkey contains the identifier for the application to be fetched
 	 * @param uiScreenName contains the name of the UI screen for the principal to be fetched
 	 * @return key for the specified RA screen
 	 * @throws CprException
 	 */
 	private long getRaScreenKey(long raApplicationKey, String uiScreenName) throws CprException {
 		
 		// Pull the RA screen associated with the given RA from the ra screens table in the database
 		final String screenSqlQuery = "from RaScreens where raApplicationKey = :raApplicationKey AND uiScreenName = :uiScreenName";
 		final Query screenQuery = getSession().createQuery(screenSqlQuery);
 		screenQuery.setParameter("raApplicationKey", raApplicationKey);
 		screenQuery.setParameter("uiScreenName", uiScreenName);
 		if (screenQuery.list().size() != RETURN_LIST_SIZE) {
 			throw new CprException(ReturnType.GENERAL_DATABASE_EXCEPTION,"RA screen not found for ra_application_key " + raApplicationKey + " and ui_screen_name " + uiScreenName);
 		}
 		RaScreens screenDbBean = (RaScreens)screenQuery.list().get(GET_ITEM_0);
 		return screenDbBean.getRaScreenKey();
 	}
 	
 	private void createRaMap() {
 		if (raMap == null) {
 			raMap = new HashMap<Long, String>();
 			// Pull the valid RAs from the registration_authority table in the database
 			final String raSqlQuery = "from RegistrationAuthority where suspendFlag = 'N'";
 			final Query raQuery = getSession().createQuery(raSqlQuery);
 			for (final Iterator<?> it = raQuery.list().iterator(); it.hasNext(); ) {
 				RegistrationAuthority raDbBean = (RegistrationAuthority)it.next();
 				raMap.put(raDbBean.getRegistrationAuthorityKey(), raDbBean.getRegistrationAuthority());
 			}
 		}
 	}
 
 }
