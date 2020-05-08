 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2007, 2008 The Sakai Foundation.
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
 
 package org.sakaiproject.assignment2.tool.producers;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.AssignmentPermissionLogic;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.logic.GradeInformation;
 import org.sakaiproject.assignment2.logic.GradebookItem;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.assignment2.tool.LocalAssignmentLogic;
 import org.sakaiproject.assignment2.tool.params.AssignmentViewParams;
 import org.sakaiproject.assignment2.tool.params.GradeViewParams;
 import org.sakaiproject.assignment2.tool.params.ViewSubmissionsViewParams;
 import org.sakaiproject.assignment2.tool.params.ZipViewParams;
 import org.sakaiproject.assignment2.tool.producers.renderers.AttachmentListRenderer;
 import org.sakaiproject.assignment2.tool.producers.renderers.PagerRenderer;
 import org.sakaiproject.assignment2.tool.producers.renderers.SortHeaderRenderer;
 import org.sakaiproject.site.api.Group;
 
 import uk.org.ponder.htmlutil.HTMLUtil;
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.messageutil.TargettedMessage;
 import uk.org.ponder.messageutil.TargettedMessageList;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIELBinding;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInput;
 import uk.org.ponder.rsf.components.UIInternalLink;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UISelect;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
 import uk.org.ponder.rsf.flow.ARIResult;
 import uk.org.ponder.rsf.flow.ActionResultInterceptor;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
 
 /**
  * This producer renders the page that has the table of all the students
  * with links to their submissions and information on the current grade, 
  * feedback released, etc.
  * 
  * @author rjlowe
  * @author wagnermr
  * @author sgithens
  *
  */
 public class ViewSubmissionsProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter, ActionResultInterceptor {
 
     public static final String VIEW_ID = "viewSubmissions";
     public String getViewID() {
         return VIEW_ID;
     }
 
     //sorting strings
     public static final String DEFAULT_SORT_DIR = AssignmentLogic.SORT_DIR_ASC;
     public static final String DEFAULT_OPPOSITE_SORT_DIR = AssignmentLogic.SORT_DIR_DESC;
     public static final String DEFAULT_SORT_BY = AssignmentSubmissionLogic.SORT_BY_NAME;
 
     private String current_sort_by = DEFAULT_SORT_BY;
     private String current_sort_dir = DEFAULT_SORT_DIR;
 
     //images
     public static final String BULLET_UP_IMG_SRC = "/sakai-assignment2-tool/content/images/bullet_arrow_up.png";
     public static final String BULLET_DOWN_IMG_SRC = "/sakai-assignment2-tool/content/images/bullet_arrow_down.png";
 
     private PagerRenderer pagerRenderer;
     private MessageLocator messageLocator;
     private AssignmentLogic assignmentLogic;
     private AssignmentSubmissionLogic submissionLogic;
     private TargettedMessageList messages;
     private ExternalLogic externalLogic;
     private Locale locale;
     private SortHeaderRenderer sortHeaderRenderer;
     private AttachmentListRenderer attachmentListRenderer;
     private AssignmentPermissionLogic permissionLogic;
     private ExternalGradebookLogic gradebookLogic;
 
     private Long assignmentId;
 
     public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
         ViewSubmissionsViewParams params = (ViewSubmissionsViewParams) viewparams;
         //make sure that we have an AssignmentID to work with
         if (params.assignmentId == null){
             //ERROR SHOULD BE SET, OTHERWISE TAKE BACK TO ASSIGNMENT_LIST
             messages.addMessage(new TargettedMessage("GeneralActionError"));
             return;
         }
         assignmentId = params.assignmentId;
         Assignment2 assignment = assignmentLogic.getAssignmentByIdWithAssociatedData(assignmentId);
         
         String currContextId = externalLogic.getCurrentContextId();
 
         //use a date which is related to the current users locale
         DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
 
         //Edit Permission
         boolean edit_perm = permissionLogic.isCurrentUserAbleToEditAssignments(externalLogic.getCurrentContextId());
         boolean grade_perm = permissionLogic.isUserAllowedToProvideFeedbackForAssignment(assignment);
 
         //get parameters
         if (params.sort_by == null) params.sort_by = DEFAULT_SORT_BY;
         if (params.sort_dir == null) params.sort_dir = DEFAULT_SORT_DIR;
         current_sort_by = params.sort_by;
         current_sort_dir = params.sort_dir;
         UIVerbatim.make(tofill, "defaultSortBy", HTMLUtil.emitJavascriptVar("defaultSortBy", DEFAULT_SORT_BY));
 
         // we need to retrieve the history for the release/retract feedback logic
         List<AssignmentSubmission> submissions = submissionLogic.getViewableSubmissionsWithHistoryForAssignmentId(assignmentId, params.groupId);
 
         // get grade info, if appropriate
         Map<String, GradeInformation> studentIdGradeInfoMap = new HashMap<String, GradeInformation>();
         if (submissions != null && assignment.isGraded() && assignment.getGradebookItemId() != null) {
             // put studentIds in a list
             List<String> studentIdList = new ArrayList<String>();
             for (AssignmentSubmission submission : submissions) {
                 studentIdList.add(submission.getUserId());
             }
 
             // now retrieve all of the GradeInformation
             studentIdGradeInfoMap = gradebookLogic.getGradeInformationForStudents(
                     studentIdList, assignment.getContextId(), assignment.getGradebookItemId());
         }
 
         //Breadcrumbs
         UIInternalLink.make(tofill, "breadcrumb", 
                 messageLocator.getMessage("assignment2.assignment_list-sortview.heading"),
                 new SimpleViewParameters(ListProducer.VIEW_ID));
         UIMessage.make(tofill, "last_breadcrumb", "assignment2.assignment_grade-assignment.heading", new Object[] { assignment.getTitle() });
 
         // ACTION BAR
         boolean displayReleaseGrades = false;
         boolean displayReleaseFB = false;
         boolean displayDownloadAll = false;
         boolean displayUploadAll = false;
         
         if (edit_perm || grade_perm) {
             UIOutput.make(tofill, "navIntraTool");
         }
         
         // RELEASE GRADES
         if (edit_perm && assignment.isGraded()){
             displayReleaseGrades = true;
             
             // determine if grades have been released yet
             GradebookItem gbItem = gradebookLogic.getGradebookItemById(currContextId, assignment.getGradebookItemId());
             boolean gradesReleased = gbItem.isReleased();
             String releaseLinkText = messageLocator.getMessage("assignment2.assignment_grade-assignment.grades.release");
             if (gradesReleased) {
                 releaseLinkText = messageLocator.getMessage("assignment2.assignment_grade-assignment.grades.retract");
             }
 
             UIForm releaseGradesForm = UIForm.make(tofill, "release_grades_form");
             releaseGradesForm.addParameter(new UIELBinding("ReleaseGradesAction.gradebookItemId", assignment.getGradebookItemId()));
             releaseGradesForm.addParameter(new UIELBinding("ReleaseGradesAction.curContext", currContextId));
             releaseGradesForm.addParameter(new UIELBinding("ReleaseGradesAction.releaseGrades", !gradesReleased));
 
             UICommand releaseGradesButton = UICommand.make(releaseGradesForm, "release_grades", "ReleaseGradesAction.execute");
 
             UIOutput.make(tofill, "release_grades_li");
             UIInternalLink releaseGradesLink = UIInternalLink.make(tofill, 
                     "release_grades_link", releaseLinkText, viewparams);
             Map<String,String> idmap = new HashMap<String,String>();
             idmap.put("onclick", "asnn2.releaseGradesDialog('"+releaseGradesButton.getFullID()+"'); return false;");
             releaseGradesLink.decorate(new UIFreeAttributeDecorator(idmap));
             
             makeReleaseGradesDialog(gradesReleased, assignment, tofill);
         }
         
         // RELEASE FEEDBACK
         if (grade_perm) {
             displayReleaseFB = true;
             makeReleaseFeedbackLink(tofill, params, submissions);
         }
         
         // DOWNLOAD ALL
         if (grade_perm) {
             // do not display download all link if assign is ungraded and has non-electronic submission
             if (assignment.getSubmissionType() == AssignmentConstants.SUBMIT_NON_ELECTRONIC && !assignment.isGraded()) {
                 displayDownloadAll = false;
             } else {
                 displayDownloadAll = true;
             }
 
             if (displayDownloadAll) {
                 // only allow download if it is graded or at least one submission
                 // otherwise we display a "disabled" link
                 boolean allowDownload = false;
                 if (assignment.isGraded()) {
                     allowDownload = true;
                 } else {
                     List<String> studentIds = new ArrayList<String>();
                     for (AssignmentSubmission sub : submissions) {
                         studentIds.add(sub.getUserId());
                     }
 
                     int numSubmissions = submissionLogic.getNumStudentsWithASubmission(assignment, studentIds);
                     if (numSubmissions > 0) {
                         allowDownload = true;
                     }
                 }
 
                 if (allowDownload) {
                     ZipViewParams zvp = new ZipViewParams("zipSubmissions", assignmentId);
                     UIInternalLink.make(tofill, "downloadall",
                             UIMessage.make("assignment2.assignment_grade-assignment.downloadall.button"), zvp);
                 } else {
                     // show a disabled link if no submissions yet 
                     UIOutput.make(tofill, "downloadall_disabled", messageLocator.getMessage("assignment2.assignment_grade-assignment.downloadall.button"));
                 } 
             }
         }
         
         // UPLOAD GRADES
         // upload grades should only appear for graded items
         if (grade_perm && assignment.isGraded()) {
             displayUploadAll = true;
 
             AssignmentViewParams avp = new AssignmentViewParams("uploadall", assignmentId);
             UIInternalLink.make(tofill, "uploadall",
                     UIMessage.make("assignment2.assignment_grade-assignment.uploadall.button"), avp);
         }
         
         // handle those pesky separators
         if (displayReleaseGrades && (displayReleaseFB || displayUploadAll || displayDownloadAll)) {
             UIOutput.make(tofill, "release_grades_sep");
         }
         
         if (displayReleaseFB && (displayUploadAll || displayDownloadAll)) {
             UIOutput.make(tofill, "release_feedback_sep");
         }
         
         if (displayDownloadAll && displayUploadAll) {
             UIOutput.make(tofill, "downloadall_sep");
         }
 
         UIMessage.make(tofill, "page-title", "assignment2.assignment_grade-assignment.title");
         //navBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID);
         pagerRenderer.makePager(tofill, "pagerDiv:", VIEW_ID, viewparams, submissions.size());
         //UIMessage.make(tofill, "heading", "assignment2.assignment_grade-assignment.heading", new Object[] { assignment.getTitle() });
 
         // now make the "View By Sections/Groups" filter
         makeViewByGroupFilter(tofill, params);
         
         //Do Student Table
         sortHeaderRenderer.makeSortingLink(tofill, "tableheader.student", viewparams, 
                 AssignmentSubmissionLogic.SORT_BY_NAME, "assignment2.assignment_grade-assignment.tableheader.student");
         
         if (assignment.getSubmissionType() != AssignmentConstants.SUBMIT_NON_ELECTRONIC) {
             sortHeaderRenderer.makeSortingLink(tofill, "tableheader.status", viewparams, 
                     LocalAssignmentLogic.SORT_BY_STATUS, "assignment2.assignment_grade-assignment.tableheader.status");
             sortHeaderRenderer.makeSortingLink(tofill, "tableheader.submitted", viewparams, 
                     AssignmentSubmissionLogic.SORT_BY_SUBMIT_DATE, "assignment2.assignment_grade-assignment.tableheader.submitted");
             
         }
         
         if (assignment.isGraded()) {
             String releasedString;
             GradebookItem gradebookItem = gradebookLogic.getGradebookItemById(assignment.getContextId(), assignment.getGradebookItemId());
             if (gradebookItem.isReleased()) {
                 releasedString = "assignment2.assignment_grade-assignment.tableheader.grade.released";
             } else {
                 releasedString = "assignment2.assignment_grade-assignment.tableheader.grade.not_released";
             }
             sortHeaderRenderer.makeSortingLink(tofill, "tableheader.grade", viewparams, 
                     LocalAssignmentLogic.SORT_BY_GRADE, releasedString);
         }
         sortHeaderRenderer.makeSortingLink(tofill, "tableheader.released", viewparams, 
                 AssignmentSubmissionLogic.SORT_BY_RELEASED, "assignment2.assignment_grade-assignment.tableheader.released");
 
         for (AssignmentSubmission as : submissions) {
             UIBranchContainer row = UIBranchContainer.make(tofill, "row:");
 
             UIInternalLink.make(row, "row_grade_link",
                     externalLogic.getUserSortName(as.getUserId()),
                     new GradeViewParams(GradeProducer.VIEW_ID, as.getAssignment().getId(), as.getUserId()));
 
 
             // submission info columns are not displayed for non-electronic assignments
             if (assignment.getSubmissionType() != AssignmentConstants.SUBMIT_NON_ELECTRONIC) {
                 if (as.getCurrentSubmissionVersion() != null && as.getCurrentSubmissionVersion().getSubmittedDate() != null){
                     UIOutput.make(row, "row_submitted", df.format(as.getCurrentSubmissionVersion().getSubmittedDate()));
                 } else {
                     UIOutput.make(row, "row_submitted", "");
                 }
                 
                 // set the textual representation of the submission status
                 String status = "";
                 int statusConstant = AssignmentConstants.SUBMISSION_NOT_STARTED;
                 if (as != null) {
                     statusConstant = submissionLogic.getSubmissionStatusConstantForCurrentVersion(
                             as.getCurrentSubmissionVersion(), assignment.getDueDate());
                     status = messageLocator.getMessage(
                             "assignment2.assignment_grade-assignment.submission_status." + 
                             statusConstant);
                 }
 
                 UIOutput.make(row, "row_status", status);
             }
 
             if (assignment.isGraded()) {
                 String grade = "";
                 GradeInformation gradeInfo = studentIdGradeInfoMap.get(as.getUserId());
                 if (gradeInfo != null) {
                     grade = gradeInfo.getGradebookGrade();
                 }
                 UIOutput.make(row, "row_grade", grade);
             }
 
             String released = "0";
             if (as.getCurrentSubmissionVersion() != null)  {
                 if (as.getCurrentSubmissionVersion().isFeedbackReleased()) {
                     UIOutput.make(row, "row_released");
                     released += 1;
                 }
             }
 
             //For JS Sorting
             UIOutput.make(row, "released", released);
         }
 
         /*
          * Form for assigning a grade to all submissions without a grade.
          */
         if (submissions != null && !submissions.isEmpty() && 
                 grade_perm && assignment.isGraded()) {
             String lowestPossibleGrade = gradebookLogic.getLowestPossibleGradeForGradebookItem(currContextId, assignment.getGradebookItemId());
             UIForm unassignedForm = UIForm.make(tofill, "unassigned-apply-form");
             unassignedForm.addParameter(new UIELBinding("GradeAllRemainingAction.assignmentId", assignment.getId()));
             UIInput.make(unassignedForm, "new-grade-value", "GradeAllRemainingAction.grade", lowestPossibleGrade);
             UICommand.make(unassignedForm, "apply-button", "GradeAllRemainingAction.execute");
         }
         
         // Confirmation Dialogs
         // These are only added here for internationalization. They are not part
         // of a real form.
         UICommand.make(tofill, "release-feedback-confirm", UIMessage.make("assignment2.dialogs.release_all_feedback.confirm"));
         UICommand.make(tofill, "release-feedback-cancel", UIMessage.make("assignment2.dialogs.release_all_feedback.cancel"));
         
         UICommand.make(tofill, "retract-feedback-confirm", UIMessage.make("assignment2.dialogs.retract_all_feedback.confirm"));
         UICommand.make(tofill, "retract-feedback-cancel", UIMessage.make("assignment2.dialogs.retract_all_feedback.cancel"));
     }
     
     private void makeReleaseFeedbackLink(UIContainer tofill, ViewSubmissionsViewParams viewparams, List<AssignmentSubmission> submissionsWithHistory) {
         // check to see if there is anything to release yet
         boolean feedbackExists = false;
         // determine if we are releasing or retracting
         boolean release = false;
 
         if (submissionsWithHistory != null) {
             for (AssignmentSubmission submission : submissionsWithHistory) {
                 if (submission.getSubmissionHistorySet() != null) {
                     for (AssignmentSubmissionVersion version : submission.getSubmissionHistorySet()) {
                         // only look at versions that have had feedback activity
                         if (version.getLastFeedbackDate() != null) {
                             feedbackExists = true;
                             // if there is at least one version with unreleased feedback,
                             // we will show the "Release" link
                             if (!version.isFeedbackReleased()) {
                                 release = true;
                             }
                         }
                     }
                 }
             }
         }
 
         if (feedbackExists) {
             String releaseLinkText;
             if (release) {
                 releaseLinkText = messageLocator.getMessage("assignment2.assignment_grade-assignment.feedback.release");
             } else {
                 releaseLinkText = messageLocator.getMessage("assignment2.assignment_grade-assignment.feedback.retract");
             }
             UIForm releaseFeedbackForm = UIForm.make(tofill, "release-feedback-form");
             releaseFeedbackForm.parameters.add(new UIELBinding("#{ReleaseFeedbackAction.assignmentId}", assignmentId));
             releaseFeedbackForm.parameters.add(new UIELBinding("#{ReleaseFeedbackAction.releaseFeedback}", release));
             UICommand submitAllFeedbackButton = UICommand.make(releaseFeedbackForm, "release_feedback", releaseLinkText,
             "#{ReleaseFeedbackAction.execute}");
 
             UIInternalLink releaseFeedbackLink = UIInternalLink.make(tofill, 
                     "release-feedback-link", releaseLinkText, viewparams);
             Map<String,String> idmap = new HashMap<String,String>();
           //  idmap.put("onclick", "document.getElementById('"+submitAllFeedbackButton.getFullID()+"').click(); return false;");
             idmap.put("onclick", "asnn2.releaseFeedbackDialog('"+submitAllFeedbackButton.getFullID()+"', " + release + "); return false;");
             releaseFeedbackLink.decorate(new UIFreeAttributeDecorator(idmap));
         } else {
             // show a disabled link if no feedback to release or retract
             UIOutput.make(tofill, "release_feedback_disabled", messageLocator.getMessage("assignment2.assignment_grade-assignment.feedback.release"));
         }
     }
     
     private void makeViewByGroupFilter(UIContainer tofill, ViewSubmissionsViewParams params) {
         List<Group> viewableGroups = permissionLogic.getViewableGroupsForCurrUserForAssignment(assignmentId);
         if (viewableGroups != null && !viewableGroups.isEmpty()) {
             UIForm groupFilterForm = UIForm.make(tofill, "group_filter_form", params);
             
             // we need to order the groups alphabetically. Group names are unique
             // per site, so let's make a map
             Map<String, String> groupNameToIdMap = new HashMap<String, String>();
             for (Group group : viewableGroups) { 
                 groupNameToIdMap.put(group.getTitle(), group.getId());
             }
             
             List<String> orderedGroupNames = new ArrayList<String>(groupNameToIdMap.keySet());
             Collections.sort(orderedGroupNames, new Comparator<String>() {
                 public int compare(String groupName1, String groupName2) {
                     return groupName1.compareToIgnoreCase(groupName2);
                 }
             });
 
             String selectedValue = "";
             if (params.groupId != null && params.groupId.trim().length() > 0) {
                 selectedValue = params.groupId;
             }
             
             int numItemsInDropDown = viewableGroups.size();
             
             // if there is more than one viewable group, add the 
             // "All Sections/Groups option"
             if (viewableGroups.size() > 1) {
                 numItemsInDropDown++;
             }
 
             // Group Ids
             String[] view_filter_values = new String[numItemsInDropDown]; 
             // Group Names
             String[] view_filter_options = new String[numItemsInDropDown];
 
             int index = 0;
             
             // the first entry is "All Sections/Groups"
             if (viewableGroups.size() > 1) {  
                 view_filter_values[index] = "";
                 view_filter_options[index] = messageLocator.getMessage("assignment2.assignment_grade.filter.all_sections");
                 index++;
             }
 
             for (String groupName : orderedGroupNames) { 
                 view_filter_values[index] = groupNameToIdMap.get(groupName);
                 view_filter_options[index] = groupName;
                 index++;
             }
 
             UISelect.make(groupFilterForm, "group_filter", view_filter_values,
                     view_filter_options, "groupId", selectedValue);
         }
     }
     
     private void makeReleaseGradesDialog(boolean gradesReleased, Assignment2 assignment, UIContainer tofill) {
      // Release Grades Dialog 
         String releaseGradesTitle;
         String releaseGradesMessage;
         String releaseGradesConfirm;
         String releaseGradesCancel;
         
         if (gradesReleased) {
             // if the grades are already released, the option is "retract"
             releaseGradesTitle = messageLocator.getMessage("assignment2.dialogs.retract_grades.title");
             releaseGradesConfirm = messageLocator.getMessage("assignment2.dialogs.retract_grades.confirm");
             releaseGradesCancel = messageLocator.getMessage("assignment2.dialogs.retract_grades.cancel");
             if (assignment.getAssignmentGroupSet() == null || assignment.getAssignmentGroupSet().isEmpty()) {
                 // this assignment is not restricted to groups
                 releaseGradesMessage = messageLocator.getMessage("assignment2.dialogs.retract_grades.nogroups.message");
             } else {
                 // this has groups, so we display a different warning and
                 // require a confirmation checkbox to be clicked
                 releaseGradesMessage = messageLocator.getMessage("assignment2.dialogs.retract_grades.groups.message");
                 UIOutput.make(tofill, "confirm-checkbox-label", messageLocator.getMessage("assignment2.dialogs.retract_grades.groups.confirmcheckbox"));
                 UIOutput.make(tofill, "confirm-checkbox-area");
             }
         } else {
             // the user has the option to release
             releaseGradesTitle = messageLocator.getMessage("assignment2.dialogs.release_grades.title");
             releaseGradesConfirm = messageLocator.getMessage("assignment2.dialogs.release_grades.confirm");
             releaseGradesCancel = messageLocator.getMessage("assignment2.dialogs.release_grades.cancel");
             if (assignment.getAssignmentGroupSet() == null || assignment.getAssignmentGroupSet().isEmpty()) {
                 // this assignment is not restricted to groups
                 releaseGradesMessage = messageLocator.getMessage("assignment2.dialogs.release_grades.nogroups.message");
             } else {
                 // this has groups, so we display a different warning and
                 // require a confirmation checkbox to be clicked
                 releaseGradesMessage = messageLocator.getMessage("assignment2.dialogs.release_grades.groups.message");
                 UIOutput.make(tofill, "confirm-checkbox-label", messageLocator.getMessage("assignment2.dialogs.release_grades.groups.confirmcheckbox"));
                 UIOutput.make(tofill, "confirm-checkbox-area");
             }
         }
         
         UIOutput.make(tofill, "release-grades-message", releaseGradesMessage);
         UIOutput.make(tofill, "release-grades-confirm", releaseGradesConfirm);
         UIOutput.make(tofill, "release-grades-cancel", releaseGradesCancel);
     }
 
     public List<NavigationCase> reportNavigationCases() {
         List<NavigationCase> nav= new ArrayList<NavigationCase>();
         nav.add(new NavigationCase("release_all", new ViewSubmissionsViewParams(
                 ViewSubmissionsProducer.VIEW_ID, null)));
         return nav;
     }
 
     public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
         if (result.resultingView instanceof ViewSubmissionsViewParams) {
             ViewSubmissionsViewParams outgoing = (ViewSubmissionsViewParams) result.resultingView;
             ViewSubmissionsViewParams in = (ViewSubmissionsViewParams) incoming;
             outgoing.assignmentId = in.assignmentId;
             outgoing.groupId = in.groupId;
         }
     }
 
     public ViewParameters getViewParameters(){
         return new ViewSubmissionsViewParams();
     }
 
     public void setMessageLocator(MessageLocator messageLocator) {
         this.messageLocator = messageLocator;
     }
 
     public void setPagerRenderer(PagerRenderer pagerRenderer){
         this.pagerRenderer = pagerRenderer;
     }
 
     public void setAssignmentLogic(AssignmentLogic assignmentLogic) {
         this.assignmentLogic = assignmentLogic;
     }
 
     public void setAssignmentSubmissionLogic(AssignmentSubmissionLogic submissionLogic) {
         this.submissionLogic = submissionLogic;
     }
 
     public void setMessages(TargettedMessageList messages) {
         this.messages = messages;
     }
 
     public void setExternalLogic(ExternalLogic externalLogic) {
         this.externalLogic = externalLogic;
     }
 
     public void setLocale(Locale locale) {
         this.locale = locale;
     }
 
     public void setSortHeaderRenderer(SortHeaderRenderer sortHeaderRenderer) {
         this.sortHeaderRenderer = sortHeaderRenderer;
     }
 
     public void setAttachmentListRenderer(AttachmentListRenderer attachmentListRenderer){
         this.attachmentListRenderer = attachmentListRenderer;
     }
 
     public void setPermissionLogic(AssignmentPermissionLogic permissionLogic) {
         this.permissionLogic = permissionLogic;
     }
 
     public void setExternalGradebookLogic(ExternalGradebookLogic gradebookLogic) {
         this.gradebookLogic = gradebookLogic;
     }
 }
