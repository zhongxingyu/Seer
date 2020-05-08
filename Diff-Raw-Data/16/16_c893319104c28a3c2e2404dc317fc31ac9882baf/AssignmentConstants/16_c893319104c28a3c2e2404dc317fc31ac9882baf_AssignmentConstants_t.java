 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2007 The Sakai Foundation.
  * 
  * Licensed under the Educational Community License, Version 1.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at
  * 
  *      http://www.opensource.org/licenses/ecl1.php
  * 
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.assignment2.model.constants;
 
 /**
  * @author <a href="mailto:wagnermr@iupui.edu">michelle wagner</a>
  */
 public class AssignmentConstants {
 
     /**
      * Used to indicate that the assignment may only be viewed, not graded by
      * the specified user
      */
     public final static String VIEW = "view";
 
     /**
      * Used to indicate that the assignment may be viewed and graded by the
      * specified user
      */
     public final static String GRADE = "grade";
 
     /**
      * Used to indicate that a user may submit indefinitely
      */
     public final static int UNLIMITED_SUBMISSION = -1;
 
     // Notification Types
     /**
      * Do not send notification emails for any submissions
      */
     public final static int NOTIFY_NONE = 0;
     /**
      * Send a notification email for each submission
      */
     public final static int NOTIFY_FOR_EACH = 1;
     /**
      * Send me one email per day summarizing notifications for submissions
      */
     public final static int NOTIFY_DAILY_SUMMARY = 2;
 
     // Submission Types
     /**
      * Submissions may only be inline
      */
     public final static int SUBMIT_INLINE_ONLY = 0;
     /**
      * Submissions may only be attachments
      */
     public final static int SUBMIT_ATTACH_ONLY = 1;
     /**
      * Submissions may be inline and/or attachments
      */
     public final static int SUBMIT_INLINE_AND_ATTACH = 2;
     /**
      * Submissions will be non-electronic
      */
     public final static int SUBMIT_NON_ELECTRONIC = 3;
 
     // Assignment status
     /**
      * This assignment is in draft status
      */
     public final static int STATUS_DRAFT = 0;
     /**
      * The assignment is not draft status but the current date is prior to
      * the open date
      */
     public final static int STATUS_NOT_OPEN = 1;
     /**
      * The assignment is not draft. The current date is after the open date
      * but prior to the "accept until" date and due date.
      */
     public final static int STATUS_OPEN = 2;
     /**
      * The assignment is not draft. The current date is after the "accept
      * until" date.
      */
     public final static int STATUS_CLOSED = 3;
     /**
      * The assignment is not draft. The current date is after the open and
      * due dates but before the "accept until" date.
      */
     public final static int STATUS_DUE = 4;
 
     // Submission status
     /**
      * Submission has not been started
      */
     public final static int SUBMISSION_NOT_STARTED = 0;
 
     /**
      * Submission has been saved (draft) but not submitted
      */
     public final static int SUBMISSION_IN_PROGRESS = 1;
 
     /**
      * Student has made a submission
      */
     public final static int SUBMISSION_SUBMITTED = 2;
 
     /**
      * Student made a submission but it was submitted after the due date
      */
     public final static int SUBMISSION_LATE = 3;	
 
    public static final String REFERENCE_ROOT = "asnn2";
     public static final String ASSIGNMENT_TYPE = "a";
     public static final String SUBMISSION_TYPE = "s";
     
     // Properties for ContentReviewService integration
     /**
      * The score received upon review for display. String
      */
     public final static String PROP_REVIEW_SCORE_DISPLAY = "review_score_display";
     
     /**
      * The numeric (Integer) representation of the review score
      */
     public final static String PROP_REVIEW_SCORE = "review_score";
     /**
      * The url to the report constructed for the user for the review
      */
     public final static String PROP_REVIEW_URL = "review_url";
     
     /**
      * The actual code defined by the ContentReviewService representing the
      * error (Long). Only exists if {@link #PROP_REVIEW_STATUS} is {@link #REVIEW_STATUS_ERROR}.
      */
     public final static String PROP_REVIEW_ERROR_CODE = "review_error_code";
     /**
      * Indicates the status of the review: {@link #REVIEW_STATUS_ERROR},
      * {@link #REVIEW_STATUS_NONE}, {@link #REVIEW_STATUS_SUCCESS}, {@link #REVIEW_STATUS_PENDING}
      */
     public final static String PROP_REVIEW_STATUS = "review_status";
     
     // These are the possible status values for PROP_REVIEW_STATUS
     /**
      * There was an error submitting the item for review
      */
     public final static String REVIEW_STATUS_ERROR = "review_error";
     
     /**
      * The item has not been submitted for review
      */
     public final static String REVIEW_STATUS_NONE = "review_not_submitted";
     
     /**
      * The item has been submitted but review has not taken place yet
      */
     public final static String REVIEW_STATUS_PENDING = "review_pending";
     
     /**
      * The item was successfully reviewed
      */
     public final static String REVIEW_STATUS_SUCCESS = "review_success";
     
     /* These properties are specific to content review implementation using Turnitin */
     
     /**
      * Set this property to true if you want to turn on the Turnitin option for assignment2
      */
     public static final String TII_ENABLED = "turnitin.enable.assignment2";
     
     /**
      * This property is specific to Turnitin implementation of ContentReview.
      * if you want to restrict how your content is submitted to a repository, 
      * include this property in sakai.properties. Possible values are {@link #VALUE_NO_REPO}, 
      * {@link #VALUE_INSTITUTION_REPO}, {@link #VALUE_STANDARD_REPO}. If this property is
      * not included, will default to all three options. Set using the list approach in sakai.properties:
      * ie, to include the "no repo" and "standard repo" options only, use: 
      * turnitin.repository.setting.count=2
      * turnitin.repository.setting.1 = 0
      * turnitin.repository.setting.2 = 2
      * 
      */
     public static final String TII_PROP_SUBMIT_TO_REPO = "turnitin.repository.setting";
     
     /**
      * The default value for the {@link #TII_PROP_SUBMIT_TO_REPO}. Possible values are:
      * {@link #TII_VALUE_INSTITUTION_REPO}, {@link #TII_VALUE_NO_REPO}, {@link #TII_VALUE_STANDARD_REPO}
      */
     public static final String TII_PROP_DEFAULT_SUBMIT_TO_REPO = "turnitin.repository.setting.value";
     
     /**
      * possible value for {@link #TII_PROP_SUBMIT_TO_REPO}.
      * use this option if you do not want submissions saved to a repository.
      */
     public static final String TII_VALUE_NO_REPO = "0";
     /**
      * possible value for {@link #TII_PROP_SUBMIT_TO_REPO}.
      * use this option if you want submissions saved only to your institutional repository.
      */
     public static final String TII_VALUE_INSTITUTION_REPO = "1";
     /**
      * possible value for {@link #TII_PROP_SUBMIT_TO_REPO}.
      * use this option if you want all submissions saved to the content review
      * standard repository. This generally means that submissions from your institution
      * can be used to check plagiarism at other institutions.
      */
     public static final String TII_VALUE_STANDARD_REPO = "2";
     
     /**
      * This property is specific to Turnitin implementation of ContentReview.
      * Optional property to set the name of your institutional repository (most likely
      * for use in the UI)
      */
     public static final String TII_PROP_INSTITUTION_REPO_NAME = "turnitin.repository.institutional.name";
     
     /**
      * Optional property in sakai.properties to add a url link from the UI that points to a page
      * containing the requirements for attachments to be accepted by turnitin
      */
     public static final String TII_PROP_FILE_REQUIREMENTS_URL = "turnitin.file.requirements.url";
 
     public static final String TII_RETCODE_RCODE = "rcode";
     public static final String TII_RETCODE_RMESSAGE = "rmessage";
     public static final String TII_RETCODE_OBJECT = "object";
     public static final String TII_RETCODE_SUBMIT_PAPERS_TO = "submit_papers_to";
     public static final String TII_RETCODE_REPORT_GEN_SPEED = "report_gen_speed";
     public static final String TII_RETCODE_SEARCHPAPERS = "searchpapers";
     public static final String TII_RETCODE_SEARCHINTERNET = "searchinternet";
     public static final String TII_RETCODE_SEARCHJOURNALS = "searchjournals";
     public static final String TII_RETCODE_SEARCHINSTITUTION = "searchinstitution";
     public static final String TII_RETCODE_SVIEWREPORTS = "sviewreports";
 
     public static final String TII_API_PARAM_REPOSITORY = "repository";
     public static final String TII_API_PARAM_GENERATE = "generate";
     public static final String TII_API_PARAM_S_PAPER_CHECK = "s_paper_check";
     public static final String TII_API_PARAM_INTERNET_CHECK = "internet_check";
     public static final String TII_API_PARAM_JOURNAL_CHECK = "journal_check";
     public static final String TII_API_PARAM_INSTITUTION_CHECK = "institution_check";
     public static final String TII_API_PARAM_S_VIEW_REPORT = "s_view_report";
 
 }
