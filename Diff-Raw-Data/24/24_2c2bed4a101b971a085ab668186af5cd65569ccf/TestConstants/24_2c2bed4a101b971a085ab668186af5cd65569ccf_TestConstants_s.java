 /*******************************************************************************
  * Copyright (c) 2012 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class defines various constants used in R4E JUnit UI tests
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.tests.utils;
 
 import java.util.Date;
 
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EAnomalyState;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EDecision;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewType;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EUserRole;
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRuleClass;
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRuleRank;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.preferences.PreferenceConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 
 @SuppressWarnings("restriction")
 public class TestConstants {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	//Preferences
 	public static final String DEFAULT_USER_ID = "defaultuser";
 
 	public static final String DEFAULT_USER_EMAIL = "defaultuser@company.com";
 
 	//Review Groups
 	public static final String REVIEW_GROUP_TEST_NAME = "testReviewGroup";
 
 	//The following chars should not be used as strange chars: \/<>:;*"?'
 	//May be we should add a validation for the review name and review group
 	public static final String REVIEW_GROUP_TEST_NAME_STRANGE = "test Review Group �.!@#$%^&()";
 
 	public static final String REVIEW_GROUP_TEST_NAME2 = "testReviewGroup2";
 
 	public static final String REVIEW_GROUP_TEST_DESCRIPTION = "testReviewGroup description";
 
 	public static final String REVIEW_GROUP_TEST_DESCRIPTION2 = "new description";
 
 	public static final String REVIEW_GROUP_TEST_ENTRY_CRITERIA = "default entry criteria";
 
 	public static final String REVIEW_GROUP_TEST_ENTRY_CRITERIA2 = "new  entry criteria";
 
 	public static final String[] REVIEW_GROUP_TEST_AVAILABLE_PROJECTS = { "ProjectX", "ProjectY" };
 
 	public static final String REVIEW_GROUP_TEST_REM_AVAILABLE_PROJECT = "ProjectY";
 
 	public static final String REVIEW_GROUP_TEST_ADD_AVAILABLE_PROJECT = "ProjectZ";
 
 	public static final String[] REVIEW_GROUP_TEST_AVAILABLE_PROJECTS2 = { "ProjectX", "ProjectZ" };
 
 	public static final String[] REVIEW_GROUP_TEST_AVAILABLE_COMPONENTS = { "ui", "core", "docs" };
 
 	public static final String REVIEW_GROUP_TEST_REM_AVAILABLE_COMPONENT = "ui";
 
 	public static final String REVIEW_GROUP_TEST_ADD_AVAILABLE_COMPONENT = "help";
 
 	public static final String[] REVIEW_GROUP_TEST_AVAILABLE_COMPONENTS2 = { "core", "docs", "help" };
 
 	//Reviews BASIC
 	public static final R4EReviewType REVIEW_TEST_TYPE_BASIC = R4EReviewType.BASIC;
 
 	public static final String REVIEW_TEST_NAME = "testReview";
 
 	public static final String REVIEW_TEST_DESCRIPTION = "testReview description";
 
 	public static final Date REVIEW_TEST_DUE_DATE = new Date();
 
 	public static final String REVIEW_TEST_PROJECT = "ProjectX";
 
 	public static final String[] REVIEW_TEST_COMPONENTS = { "core", "docs" };
 
 	public static final String REVIEW_TEST_ENTRY_CRITERIA = "testReview entry criteria";
 
 	public static final String REVIEW_TEST_OBJECTIVES = "testReview objectives";
 
 	public static final String REVIEW_TEST_REFERENCE_MATERIALS = "testReview reference materials";
 
 	//Reviews INFORMAL
 	public static final R4EReviewType REVIEW_TEST_TYPE_INFORMAL = R4EReviewType.INFORMAL;
 
 	public static final String REVIEW_TEST_NAME_INF = "testReviewInf";
 
 	public static final String REVIEW_DUPLICATE_NAME_INF = "testReviewDuplicateInf";
 
 	//The following chars should not be used as strange chars: \/<>:;*"?'
 	//May be we should add a validation for the review name and review group
 	public static final String REVIEW_STRANGE_NAME_INF = "test Review&%$/) Inf";
 
 	public static final R4EDecision REVIEW_EXIT_DECISION_ACCEPTED = R4EDecision.ACCEPTED;
 
 	//Participants
 	public static final String PARTICIPANT_TEST_ID = "user";
 
 	public static final String PARTICIPANT_TEST_EMAIL = "user@company.com";
 
 	public static final R4EUserRole[] PARTICIPANT_TEST_ROLES = { R4EUserRole.REVIEWER };
 
 	public static final String PARTICIPANT_TEST_FOCUS_AREA = "Test FocusArea";
 
 	public static final String PARTICIPANT_ASSIGN_TO = "userA";
 
 	public static final String PARTICIPANT_ASSIGN_TO2 = "user";
 
 	//Anomalies
 
 	public static final String GLOBAL_ANOMALY_TEST_TITLE = "test Global Anomaly";
 
 	public static final String GLOBAL_ANOMALY_TEST_DESCRIPTION = "new Global Anomaly description";
 
 	public static final String COMPARE_EDITOR_ANOMALY_TEST_TITLE = "test Compare Editor Anomaly";
 
 	public static final String COMPARE_EDITOR_ANOMALY_TEST_DESCRIPTION = "new Compare Editor Anomaly description";
 
 	public static final String LINKED_ANOMALY_TEST_TITLE = "test Linked Anomaly";
 
 	public static final String LINKED_ANOMALY_TEST_DESCRIPTION = "new Linked Anomaly description";
 
 	public static final String EXTERNAL_ANOMALY_TEST_TITLE = "test External Anomaly";
 
 	public static final String EXTERNAL_ANOMALY_TEST_DESCRIPTION = "new External Anomaly description";
 
 	public static final R4EDesignRuleClass ANOMALY_TEST_CLASS_ERRONEOUS = R4EDesignRuleClass.ERRONEOUS;
 
 	public static final R4EDesignRuleClass ANOMALY_TEST_CLASS_IMPROVEMENT = R4EDesignRuleClass.IMPROVEMENT;
 
 	public static final R4EDesignRuleClass ANOMALY_TEST_CLASS_QUESTION = R4EDesignRuleClass.QUESTION;
 
 	public static final R4EDesignRuleRank ANOMALY_TEST_RANK_MAJOR = R4EDesignRuleRank.MAJOR;
 
 	public static final R4EDesignRuleRank ANOMALY_TEST_RANK_MINOR = R4EDesignRuleRank.MINOR;
 
 	public static final Date ANOMALY_TEST_DUE_DATE = new Date();
 
 	public static final R4EAnomalyState ANOMALY_STATE_ASSIGNED = R4EAnomalyState.ASSIGNED;
 
 	public static final R4EAnomalyState ANOMALY_STATE_FIXED = R4EAnomalyState.FIXED;
 
 	public static final R4EAnomalyState ANOMALY_STATE_REJECTED = R4EAnomalyState.REJECTED;
 
 	public static final R4EAnomalyState ANOMALY_STATE_VERIFIED = R4EAnomalyState.VERIFIED;
 
 	public static final R4EAnomalyState ANOMALY_STATE_POSTPONED = R4EAnomalyState.DEFERRED;
 
 	//Comments
 	public static final String COMMENT_TEST = "This is a test comment for the anomaly";
 
 	//Rule Sets
 	public static final String RULE_SET_TEST_VERSION = "0.1";
 
 	public static final String RULE_SET_TEST_NAME = "testRuleSet";
 
 	public static final String RULE_SET_TEST_NAME2 = "testRuleSet2";
 
 	//Rule Areas
 	public static final String RULE_AREA_TEST_NAME = "testRuleArea";
 
 	//Rule Violations
 	public static final String RULE_VIOLATION_TEST_NAME = "testRuleViolation";
 
 	//Rules
 	public static final String RULE_TEST_ID = "0001";
 
 	public static final String RULE_TEST_TITLE = "testRule";
 
 	public static final String RULE_TEST_DESCRIPTION = "testRule description";
 
 	public static final String RULE_TEST_CLASS = R4EUIConstants.ANOMALY_CLASS_ERRONEOUS;
 
 	public static final String RULE_TEST_RANK = R4EUIConstants.ANOMALY_RANK_MINOR;
 
 	//Notifications
 	public static final String SEND_QUESTION_REVIEW_TEST_SOURCE = R4EUIPlugin.getDefault()
 			.getPreferenceStore()
 			.getString(PreferenceConstants.P_USER_EMAIL);
 
 	public static final String[] SEND_QUESTION_REVIEW_TEST_DESTINATIONS = { "user@company.com" };
 
 	public static final String SEND_QUESTION_REVIEW_TEST_SUBJECT = "[r4e-mail]  Review Basic: testReview - Question regarding review ";
 
 	public static final String SEND_QUESTION_REVIEW_TEST_BODY = "\nHi,\n\nI have a Question concerning the Following Elements: \n\nReview: testReview\n\n\n\nReview Information\nGroup: 		testReviewGroup\nReview: 	testReview\nComponents: 	core, docs, \nProject: 	ProjectX\nParticipants: 	"
 			+ R4EUIPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.P_USER_ID)
 			+ ", user, \n\nBest Regards,\n"
 			+ R4EUIPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.P_USER_ID);
 
 	public static final String SEND_QUESTION_REVIEW_TEST_SUBJECT_INFORMAL = "[r4e-mail]  Review Informal: "
 			+ REVIEW_TEST_NAME_INF + " - Question regarding review ";
 
 	public static final String SEND_QUESTION_REVIEW_TEST_BODY_INFORMAL = "\nHi,\n\nI have a Question concerning the Following Elements: "
 			+ "\n\n\n\nReview Information\nGroup: 		testReviewGroup\nReview: 	"
 			+ REVIEW_TEST_NAME_INF
 			+ "\nComponents: 	core, docs, \nProject: 	ProjectX\nParticipants: 	"
 			+ R4EUIPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.P_USER_ID)
 			+ ", user, \n\nBest Regards,\n"
 			+ R4EUIPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.P_USER_ID);
 
 }
