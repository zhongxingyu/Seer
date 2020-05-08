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
 
 import java.awt.Color;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.tool.StudentAction;
 import org.sakaiproject.assignment2.tool.beans.AssignmentSubmissionBean;
 import org.sakaiproject.assignment2.tool.params.AssignmentListSortViewParams;
 import org.sakaiproject.assignment2.tool.params.SimpleAssignmentViewParams;
 
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.rsf.components.UIBoundBoolean;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIELBinding;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInternalLink;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.decorators.DecoratorList;
 import uk.org.ponder.rsf.components.decorators.UIColourDecorator;
 import uk.org.ponder.rsf.components.decorators.UIDecorator;
 import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
 
 /**
  * This view is responsible for showing the Student Landing page which will have
  * the To Do style list of assignments that need to be completed.
  * 
  * @author rjlowe
  * @author sgithens
  *
  */
 public class StudentAssignmentListProducer implements ViewComponentProducer, ViewParamsReporter {
 
     public static final String VIEW_ID = "student-assignment-list";
     public String getViewID(){
         return VIEW_ID;
     }
 
     private AssignmentSubmissionLogic submissionLogic;
     private Locale locale;
     private AssignmentSubmissionBean submissionBean;
     private ExternalGradebookLogic externalGradebookLogic;
     private ExternalLogic externalLogic;
     private MessageLocator messageLocator;
 
     public static final String DEFAULT_SORT_DIR = AssignmentLogic.SORT_DIR_ASC;
     public static final String DEFAULT_OPPOSITE_SORT_DIR = AssignmentLogic.SORT_DIR_DESC;
     public static final String DEFAULT_SORT_BY = AssignmentLogic.SORT_BY_INDEX;
 
     //images
     public static final String BULLET_UP_IMG_SRC = "/sakai-assignment2-tool/content/images/bullet_arrow_up.png";
     public static final String BULLET_DOWN_IMG_SRC = "/sakai-assignment2-tool/content/images/bullet_arrow_down.png";
     public static final String ATTACH_IMG_SRC = "/sakai-assignment2-tool/content/images/attach.png";
 
 
     public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
 
         // use a date which is related to the current users locale
         DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
 
         //get parameters
         AssignmentListSortViewParams params = (AssignmentListSortViewParams) viewparams;
         
         String currContextId = externalLogic.getCurrentContextId();
 
         //get paging data
         //List<Assignment2> entries = assignmentLogic.getViewableAssignments();
         List<AssignmentSubmission> submissionsWithHistory = submissionLogic.getSubmissionsForCurrentUser(currContextId);
 
         //Breadcrumbs
         UIMessage.make(tofill, "last_breadcrumb", "assignment2.student-assignment-list.heading");
 
         UIMessage.make(tofill, "page-title", "assignment2.student-assignment-list.title");
 
         /* 
          * If there are no assignments, print out the message String, otherwise,
          * create the table element.
          */
         if (submissionsWithHistory.size() <= 0) {
             UIMessage.make(tofill, "assignment_empty", "assignment2.student-assignment-list.assignment_empty");
             return;
         }
         else {
             UIOutput.make(tofill, "assignment-list-table");
         }
 
         //Fill out Table
         for (AssignmentSubmission assignmentSubmission : submissionsWithHistory){
             UIBranchContainer row = UIBranchContainer.make(tofill, "assignment-row:");
 
             Assignment2 assignment = assignmentSubmission.getAssignment();
    
             boolean assignmentCompleted = assignmentSubmission.isCompleted();
             
             // Todo
             UIForm markTodoForm = UIForm.make(row, "todo-check-form");
             UIBoundBoolean todoCheck = UIBoundBoolean.make(markTodoForm, "todo-checkbox", "MarkTodoBean.checkTodo", assignmentCompleted);
             UICommand hiddenSubmit = UICommand.make(markTodoForm, "submit-button", "MarkTodoBean.markTodo");
             todoCheck.decorate(new UIFreeAttributeDecorator("onclick", "document.getElementById('"+hiddenSubmit.getFullID()+"').click()"));
             hiddenSubmit.addParameter(new UIELBinding("MarkTodoBean.assignmentId", assignment.getId()));
             
             /*
              * TODO FIXME I'm having major issues getting the CSS style to take
              * effect here, so wer are creating this decorated color for now.
              * If you pass null to the UIColourDecorator it doesn't do anything,
              * so this lets us create a color to pass to each item that needs
              * greyed out (or doesn't, in which case we just leave it null).
              */
             Color decoratedColor = assignmentCompleted ? Color.gray : null;
             UIDecorator assnItemDecorator = new UIColourDecorator(decoratedColor, null);
             
             /*
              * Title and Action Links
              * 
              * There are 4 options for the string on the action link:
              * 
              * 1) View Details and Submit
              *    - Assignment has not been submitted
              *    - accept until date has not passed
              *    - submission type of text only, attachments only, or text and attachments
              * 
              * 2) View Details
              *    - submission type of non-electronic or do not require a submission
              * 
              * 3) Resubmit
              *    - assignment allows resubmission
              *    - accept until date has not passed
              * 
              * 4) View Submission / View Submissions
              *    - shown for assignments 
              * 
              */            
             UIOutput.make(row, "assignment-title", assignment.getTitle())
                     .decorate(assnItemDecorator);
             
             /*
              * if curr submission is draft, let the student know it is in progress,
              * but don't display if the assignment has been deleted
              */
             if (!assignment.isRemoved() && assignmentSubmission.getCurrentSubmissionVersion() != null && 
                     assignmentSubmission.getCurrentSubmissionVersion().isDraft()) {
                 UIMessage.make(row, "assignment-in-progress", "assignment2.student-assignment-list.in-progress");
             }
             /*
              * If the assignment has been deleted, we are suppose to show this
              * bit of text.
              */
             if (assignment.isRemoved()) {
                 UIMessage.make(row, "assignment-deleted", "assignment2.student-assignment-list.assignment-deleted");
             }
             
             StudentAction availStudentAction = submissionBean.determineStudentAction(assignmentSubmission.getUserId(), assignment.getId());
             
             AssignmentSubmissionVersion latestSubmission = assignmentSubmission.retrieveMostRecentSubmission();
             
             String actionLinkText;
             // if there is at least one submission, we display the submitted date/time for the link text
             if (latestSubmission != null) {
                 if (assignment.getDueDate() != null && assignment.getDueDate().before(latestSubmission.getSubmittedDate())) {
                     actionLinkText = messageLocator.getMessage("assignment2.student-assignment-list.submitted_link.late", 
                             df.format(latestSubmission.getSubmittedDate()));
                 } else {
                     actionLinkText = messageLocator.getMessage("assignment2.student-assignment-list.submitted_link", 
                             df.format(latestSubmission.getSubmittedDate()));
                 }
             } else {
                 actionLinkText = messageLocator.getMessage("assignment2.student-assignment-list.action." + availStudentAction.toString().toLowerCase());
             }
             
             UIInternalLink.make(row, "assignment-action-link", actionLinkText,  
                 new SimpleAssignmentViewParams(StudentSubmitProducer.VIEW_ID, assignment.getId()));
             
             // add resubmit link if appropriate
             if (availStudentAction.equals(StudentAction.VIEW_AND_RESUBMIT)) {
                 UIOutput.make(row, "resubmit-action");
                 UIInternalLink.make(row, "assignment-resubmit-link", UIMessage.make("assignment2.student-assignment-list.resubmit_link"),  
                         new SimpleAssignmentViewParams(StudentSubmitProducer.VIEW_ID, assignment.getId()));
             }
             
             // Due date
             if (assignment.getDueDate() != null) {
                 UIOutput.make(row, "assignment_row_due", df.format(assignment.getDueDate())).decorate(assnItemDecorator);
                 // if submission is open and would be late, we add a late flag
                 if (assignment.getDueDate().before(new Date()) && 
                         (availStudentAction.equals(StudentAction.VIEW_AND_RESUBMIT) || 
                                 availStudentAction.equals(StudentAction.VIEW_AND_SUBMIT))) {
                     UIMessage.make(row, "assignment_late", "assignment2.student-assignment-list.late").decorate(assnItemDecorator);
                 }
             } 
             else {
                 UIMessage.make(row, "assignment_row_due", "assignment2.student-assignment-list.no_due_date").decorate(assnItemDecorator);
             }
             
             /*
              *  Feedback
              */
             boolean feedbackExists = false;
             boolean unreadFeedbackExists = false;
             Set<AssignmentSubmissionVersion> submissions = assignmentSubmission.getSubmissionHistorySet();
             for (AssignmentSubmissionVersion version: submissions) {
                 if (version.isFeedbackReleased()) {
                     feedbackExists = true;
                 }
                 if (!version.isFeedbackRead()) {
                     unreadFeedbackExists = true;
                 }
             }
             
             if (feedbackExists && unreadFeedbackExists) {
                 UIInternalLink.make(row, "unread-feedback-link",
                         new SimpleAssignmentViewParams(StudentSubmitProducer.VIEW_ID, assignment.getId()));
                 
                 // add the alt text to the image
                 Map<String, String> unreadImgAttr = new HashMap<String, String>();
                 String unreadText = messageLocator.getMessage("assignment2.student-assignment-list.icon_text.unread");
                 unreadImgAttr.put("alt", unreadText);
                 unreadImgAttr.put("title", unreadText);
                 DecoratorList unreadDecoratorList = new DecoratorList(new UIFreeAttributeDecorator(unreadImgAttr));
                 
                 UIOutput unreadImg = UIOutput.make(row, "unread-feedback-img");
                 unreadImg.decorators = unreadDecoratorList;
             }
             else if (feedbackExists) {
                 UIInternalLink.make(row, "read-feedback-link",
                         new SimpleAssignmentViewParams(StudentSubmitProducer.VIEW_ID, assignment.getId()));
 
                 // add the alt text to the image
                 Map<String, String> readImgAttr = new HashMap<String, String>();
                 String readText = messageLocator.getMessage("assignment2.student-assignment-list.icon_text.read");
                 readImgAttr.put("alt", readText);
                 readImgAttr.put("title", readText);
                 DecoratorList readDecoratorList = new DecoratorList(new UIFreeAttributeDecorator(readImgAttr));
 
                 UIOutput unreadImg = UIOutput.make(row, "read-feedback-img");
                 unreadImg.decorators = readDecoratorList;
             }
             // else.  TODO FIXME
             // I know you're always supposed to have an ending else
             // but I can't think of what to put here at the moment.
             // We should probably put an accessible note.
             
             /*
              * Grade
              */
             if (!assignment.isGraded()) {
                 UIMessage.make(row, "grade", "assignment2.student-assignment-list.not-graded").decorate(assnItemDecorator);
             } else {
                 String grade = null;
                 if (assignment.getGradebookItemId() != null) {
                    grade = externalGradebookLogic.getStudentGradeForItem(
                             assignment.getContextId(), 
                             assignmentSubmission.getUserId(), 
                             assignment.getGradebookItemId());
                 }
                 
                 if (grade == null) {
                     UIMessage.make(row, "grade", "assignment2.student-assignment-list.no-grade-yet").decorate(assnItemDecorator);
                 } else {
                     UIOutput.make(row, "grade", grade).decorate(assnItemDecorator);
                 }
                 
             }
 
         }
     }
 
     public ViewParameters getViewParameters() {
         return new AssignmentListSortViewParams();
     }
 
     public void setAssignmentSubmissionLogic (AssignmentSubmissionLogic submissionLogic) {
         this.submissionLogic = submissionLogic;
     }
 
     public void setLocale(Locale locale) {
         this.locale = locale;
     }
     
     public void setAssignmentSubmissionBean(AssignmentSubmissionBean submissionBean) {
         this.submissionBean = submissionBean;
     }
 
     public void setExternalGradebookLogic(
             ExternalGradebookLogic externalGradebookLogic) {
         this.externalGradebookLogic = externalGradebookLogic;
     }
     
     public void setExternalLogic(ExternalLogic externalLogic) {
         this.externalLogic = externalLogic;
     }
     
     public void setMessageLocator(MessageLocator messageLocator) {
         this.messageLocator = messageLocator;
     }
 }
