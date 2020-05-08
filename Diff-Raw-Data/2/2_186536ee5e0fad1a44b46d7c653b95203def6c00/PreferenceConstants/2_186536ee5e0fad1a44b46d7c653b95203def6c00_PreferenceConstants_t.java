 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, packageJavadoc
 /*******************************************************************************
  * Copyright (c) 2010, 2012 Ericsson AB and others.
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class holds R4E preferences constants
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  *******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.preferences;
 
 import java.io.IOException;
 
 import javax.naming.NamingException;
 
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.userSearch.query.IQueryUser;
 import org.eclipse.mylyn.reviews.userSearch.query.QueryUserFactory;
 import org.eclipse.mylyn.reviews.userSearch.userInfo.IUserInfo;
 
 /**
  * Constant definitions for plug-in preferences
  * 
  * @author Sebastien Dubois
  * @version $Revision: 1.0 $
  */
 public class PreferenceConstants { // $codepro.audit.disable convertClassToInterface
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * The preferences description text
 	 */
 	public static final String P_DESC = "R4E Global Preferences";
 
 	/**
 	 * The user ID preference name
 	 */
 	public static final String P_USER_ID = "userIdPreference";
 
 	/**
 	 * The user Email preference name
 	 */
 	public static final String P_USER_EMAIL = "userEmailPreference";
 
 	/**
 	 * The user ID main label text
 	 */
 	public static final String P_USER_ID_LABEL = "User ID:";
 
 	/**
 	 * The user Email main label text
 	 */
 	public static final String P_USER_EMAIL_LABEL = "User Email:";
 
 	/**
 	 * Field P_USER_GROUPS. (value is ""usersGroup"")
 	 */
 	public static final String P_PARTICIPANTS_LISTS = "participantsLists";
 
 	/**
 	 * Field P_USER_GROUPS_USERS. (value is ""userGroupUsers"")
 	 */
	public static final String P_PARTICIPANTS = "participants";
 
 	/**
 	 * Flag that state whether deltas are created for commit review items
 	 */
 	public static final String P_USE_DELTAS = "useDeltasPreferences";
 
 	/**
 	 * Flag that state whether postponed global anomalies should be imported
 	 */
 	public static final String P_IMPORT_GLOBAL_ANOMALIES_POSTPONED = "importPostponedGlobalAnomaliesPreferences";
 
 	/**
 	 * Flag that state whether the sender shall be included in originating mail notifications
 	 */
 	public static final String P_SEND_NOTIFICATION_TO_SENDER = "SendNotificationToSenderPreferences";
 
 	/**
 	 * Label for Use Deltas option
 	 */
 	public static final String P_USE_DELTAS_LABEL = "Create Delta Elements to track changes for Version-Controlled"
 			+ " Review Items";
 
 	/**
 	 * Label for import postponed global anomalies option
 	 */
 	public static final String P_IMPORT_POSTPONED_GLOBAL_ANOMALIES_LABEL = "Import Postponed Global Anomalies ";
 
 	/**
 	 * Label for the option indicating if the sender shall be included in notification e-mails
 	 */
 	public static final String P_SEND_NOTIFICATION_TO_SENDER_LABEL = "Include sender in e-mail notifications";
 
 	/**
 	 * The group file path preference name
 	 */
 	public static final String P_GROUP_FILE_PATH = "groupFilePathPreference";
 
 	/**
 	 * The group FilePathEditor main label text
 	 */
 	public static final String P_GROUP_FILE_PATH_LABEL = "Review Group Files:";
 
 	/**
 	 * The review group file extension
 	 */
 	public static final String P_GROUP_FILE_EXT = "*_group_root.xrer";
 
 	/**
 	 * The rule set file path preference name
 	 */
 	public static final String P_RULE_SET_FILE_PATH = "ruleSetFilePathPreference";
 
 	/**
 	 * The rule set file path preference name
 	 */
 	public static final String P_RULE_SET_FILE_PATH_LABEL = "Rule Set Files:";
 
 	/**
 	 * The rule set file extension
 	 */
 	public static final String P_RULE_SET_FILE_EXT = "*_rule_set.xrer";
 
 	/**
 	 * The file extension (MAC only)
 	 */
 	public static final String P_FILE_EXT_MAC = "*.xrer";
 
 	/**
 	 * Field P_SHOW_DISABLED. (value is ""showDisabled"")
 	 */
 	public static final String P_SHOW_DISABLED = "showDisabled";
 
 	/**
 	 * Field P_REVIEWS_COMPLETED_FILTER. (value is ""reviewsCompletedFilter"")
 	 */
 	public static final String P_REVIEWS_COMPLETED_FILTER = "reviewsCompletedFilter";
 
 	/**
 	 * Field P_REVIEWS_ONLY_FILTER. (value is ""reviewsOnlyFilter"")
 	 */
 	public static final String P_REVIEWS_ONLY_FILTER = "reviewsOnlyFilter";
 
 	/**
 	 * Field P_REVIEWS_MY_FILTER. (value is ""reviewsMyFilter"")
 	 */
 	public static final String P_REVIEWS_MY_FILTER = "reviewsMyFilter";
 
 	/**
 	 * Field P_PARTICIPANT_FILTER. (value is ""participantFilter"")
 	 */
 	public static final String P_PARTICIPANT_FILTER = "participantFilter";
 
 	/**
 	 * Field P_ASSIGN_MY_FILTER. (value is ""assignMyFilter"")
 	 */
 	public static final String P_ASSIGN_MY_FILTER = "assignMyFilter";
 
 	/**
 	 * Field P_ASSIGN_FILTER. (value is ""assignFilter"")
 	 */
 	public static final String P_ASSIGN_FILTER = "assignFilter";
 
 	/**
 	 * Field P_UNASSIGN_FILTER. (value is ""unassignFilter"")
 	 */
 	public static final String P_UNASSIGN_FILTER = "unassignFilter";
 
 	/**
 	 * Field P_ANOMALIES_ALL_FILTER. (value is ""anomaliesFilter"")
 	 */
 	public static final String P_ANOMALIES_ALL_FILTER = "anomaliesFilter";
 
 	/**
 	 * Field P_ANOMALIES_MY_FILTER. (value is ""anomaliesMyFilter"")
 	 */
 	public static final String P_ANOMALIES_MY_FILTER = "anomaliesMyFilter";
 
 	/**
 	 * Field P_REVIEWED_ITEMS_FILTER. (value is ""reviewItemsFilter"")
 	 */
 	public static final String P_REVIEWED_ITEMS_FILTER = "reviewItemsFilter";
 
 	/**
 	 * Field P_HIDE_RULE_SETS_FILTER. (value is ""hideRuleSetsFilter"")
 	 */
 	public static final String P_HIDE_RULE_SETS_FILTER = "hideRuleSetsFilter";
 
 	/**
 	 * Field P_HIDE_DELTAS_FILTER. (value is ""hideDeltasFilter"")
 	 */
 	public static final String P_HIDE_DELTAS_FILTER = "hideDeltasFilter";
 
 	//Inline markers preferences
 
 	/**
 	 * Field OPEN_ANOMALY_ANNOTATION_TEXT. (value is ""anomalyOpen_text"")
 	 */
 	public static final String OPEN_ANOMALY_ANNOTATION_TEXT = "anomalyOpen_text";
 
 	/**
 	 * Field OPEN_ANOMALY_ANNOTATION_STYLE. (value is ""anomalyOpen_style"")
 	 */
 	public static final String OPEN_ANOMALY_ANNOTATION_STYLE = "anomalyOpen_style";
 
 	/**
 	 * Field CLOSED_ANOMALY_ANNOTATION_TEXT. (value is ""anomalyClosed_text"")
 	 */
 	public static final String CLOSED_ANOMALY_ANNOTATION_TEXT = "anomalyClosed_text";
 
 	/**
 	 * Field CLOSED_ANOMALY_ANNOTATION_STYLE. (value is ""anomalyClosed_style"")
 	 */
 	public static final String CLOSED_ANOMALY_ANNOTATION_STYLE = "anomalyClosed_style";
 
 	/**
 	 * Field DISABLED_ANOMALY_ANNOTATION_TEXT. (value is ""anomalyDisabled_text"")
 	 */
 	public static final String DISABLED_ANOMALY_ANNOTATION_TEXT = "anomalyDisabled_text";
 
 	/**
 	 * Field DISABLED_ANOMALY_ANNOTATION_STYLE. (value is ""anomalyDisabled_style"")
 	 */
 	public static final String DISABLED_ANOMALY_ANNOTATION_STYLE = "anomalyDisabled_style";
 
 	/**
 	 * Method setUserEmailDefaultPreferences.
 	 */
 	public static void setUserEmailDefaultPreferences() {
 		if (R4EUIModelController.isUserQueryAvailable()) {
 			try {
 				//If no email preferences are set, try to retrieve it from the external DB
 				final IPreferenceStore store = R4EUIPlugin.getDefault().getPreferenceStore();
 				final String userId = store.getDefaultString(PreferenceConstants.P_USER_ID);
 				final IQueryUser query = new QueryUserFactory().getInstance();
 				final java.util.List<IUserInfo> userInfos = query.searchByUserId(userId);
 				if (userInfos.size() > 0) {
 					store.setDefault(PreferenceConstants.P_USER_EMAIL, userInfos.get(0).getEmail());
 				}
 			} catch (NamingException e) {
 				R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 			} catch (IOException e) {
 				R4EUIPlugin.Ftracer.traceWarning("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 			}
 		}
 	}
 
 }
