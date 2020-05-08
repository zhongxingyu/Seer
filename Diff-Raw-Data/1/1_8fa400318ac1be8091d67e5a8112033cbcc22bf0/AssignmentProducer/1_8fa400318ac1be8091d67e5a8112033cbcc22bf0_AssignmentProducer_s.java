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
 
 import org.sakaiproject.assignment2.tool.params.AssignmentViewParams;
 import org.sakaiproject.assignment2.tool.params.FilePickerHelperViewParams;
 import org.sakaiproject.assignment2.tool.producers.evolvers.AttachmentInputEvolver;
 import org.sakaiproject.assignment2.tool.producers.fragments.FragmentAssignment2SelectProducer;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.logic.GradebookItem;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.lang.String;
 
 import uk.org.ponder.beanutil.entity.EntityBeanLocator;
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.rsf.components.UIBoundBoolean;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInput;
 import uk.org.ponder.rsf.components.UIInputMany;
 import uk.org.ponder.rsf.components.UIInternalLink;
 import uk.org.ponder.rsf.components.UILink;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UISelect;
 import uk.org.ponder.rsf.components.UISelectChoice;
 import uk.org.ponder.rsf.components.UISelectLabel;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.components.decorators.DecoratorList;
 import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
 import uk.org.ponder.rsf.evolvers.TextInputEvolver;
 import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
 
 import org.sakaiproject.site.api.Group;
 
 /**
  * Paints the Assignment2 Page used by Instructors to create and edit 
  * assignments.
  * 
  * @author sgithens
  *
  */
 public class AssignmentProducer implements ViewComponentProducer, ViewParamsReporter {
 
     public static final String VIEW_ID = "assignment";
     public String getViewID() {
         return VIEW_ID;
     }
 
     String reqStar = "<span class=\"reqStar\">*</span>";
 
     private TextInputEvolver richTextEvolver;
     private MessageLocator messageLocator;
     private ExternalLogic externalLogic;
     private ExternalGradebookLogic externalGradebookLogic;
     private Locale locale;
     private EntityBeanLocator assignment2BeanLocator;
     private AttachmentInputEvolver attachmentInputEvolver;
 
     /*
      * You can change the date input to accept time as well by uncommenting the lines like this:
      * dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);
      * and commenting out lines like this:
      * dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_INPUT);
      * -AZ
      * And vice versa - RWE
      */
     private FormatAwareDateInputEvolver dateEvolver;
     public void setDateEvolver(FormatAwareDateInputEvolver dateEvolver) {
         this.dateEvolver = dateEvolver;
     }
 
     @SuppressWarnings("unchecked")
     public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
 
         //Get View Params
         AssignmentViewParams params = (AssignmentViewParams) viewparams;
 
         String currentContextId = externalLogic.getCurrentContextId();
 
         //get Passed assignmentId to pull in for editing if any
         Long assignmentId = params.assignmentId;
 
         // use a date which is related to the current users locale
         DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
 
         //Breadcrumbs
         UIInternalLink.make(tofill, "breadcrumb", 
                 messageLocator.getMessage("assignment2.list.heading"),
                 new SimpleViewParameters(ListProducer.VIEW_ID));
         if (params.assignmentId != null) {
             // Breadcrumb
             UIMessage.make(tofill, "last_breadcrumb", "assignment2.assignment_add.edit_heading");
             //Heading messages
             UIMessage.make(tofill, "page-title", "assignment2.assignment_add.title.edit");
         } else {
             // Breadcrumb
             UIMessage.make(tofill, "last_breadcrumb", "assignment2.assignment_add.add_heading");
             //Heading messages
             UIMessage.make(tofill, "page-title", "assignment2.assignment_add.title");
         }
 
         UIVerbatim.make(tofill, "instructions", messageLocator.getMessage("assignment2.assignment_add.instructions", 
                 new Object[]{ reqStar }));
 
         String assignment2OTP = "Assignment2.";
         String OTPKey = "";
         if (assignmentId != null) {
             OTPKey = assignmentId.toString();
         } else {
             // if we are returning from the preview page
             if (assignment2BeanLocator.getDeliveredBeans().size() == 1) {
                 OTPKey = (String) assignment2BeanLocator.getDeliveredBeans().keySet().toArray()[0];
             }
             // create new
             else {
                 OTPKey = EntityBeanLocator.NEW_PREFIX + "1";
             }
         }
         assignment2OTP += OTPKey;
         Assignment2 assignment = (Assignment2)assignment2BeanLocator.locateBean(OTPKey);
 
         //Initialize js otpkey
         UIVerbatim.make(tofill, "attachment-ajax-init", "otpkey=\"" + org.sakaiproject.util.Web.escapeUrl(OTPKey) + "\";\n" +
                 "fragGBPath=\"" + externalLogic.getAssignmentViewUrl(FragmentAssignment2SelectProducer.VIEW_ID) + "\";");
 
         UIForm form = UIForm.make(tofill, "assignment_form");
 
         //Setting up Dates
         Calendar cal = Calendar.getInstance();
         cal.set(Calendar.HOUR_OF_DAY, 12);
         cal.set(Calendar.MINUTE, 0);
         Date openDate = cal.getTime();
         cal.add(Calendar.DAY_OF_YEAR, 7);
         cal.set(Calendar.HOUR_OF_DAY, 17);
         Date closeDate = cal.getTime();
 
         //set dateEvolver
         dateEvolver.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);
 
         UIVerbatim title_label = UIVerbatim.make(form, "title_label", messageLocator.getMessage("assignment2.assignment_add.assignment_title",
                 new Object[]{ reqStar }));
         UIInput title = UIInput.make(form, "title", assignment2OTP + ".title");
 
         UIVerbatim.make(form, "open_date_label", messageLocator.getMessage("assignment2.assignment_add.open_date",
                 new Object[]{ reqStar }));
         UIInput openDateField = UIInput.make(form, "open_date:", assignment2OTP + ".openDate");
         dateEvolver.setInvalidDateKey("assignment2.assignment_add.invalid_open_date");
         dateEvolver.evolveDateInput(openDateField, null);
         UIMessage.make(form, "open_date_instruction", "assignment2.assignment_add.open_date_instruction");
 
         //Display None Decorator list
         Map attrmap = new HashMap();
         attrmap.put("style", "display:none");
         DecoratorList display_none_list =  new DecoratorList(new UIFreeAttributeDecorator(attrmap));
 
         Boolean require_due_date = (assignment.getDueDate() != null);
         UIBoundBoolean require_due = UIBoundBoolean.make(form, "require_due_date", "#{Assignment2Bean.requireDueDate}", require_due_date);
         require_due.mustapply = true;
 
         UIOutput require_due_container = UIOutput.make(form, "require_due_date_container");
         UIInput dueDateField = UIInput.make(form, "due_date:", assignment2OTP + ".dueDate");
         dateEvolver.setInvalidDateKey("assignment2.assignment_add.invalid_due_date");
         dateEvolver.evolveDateInput(dueDateField, (assignment.getDueDate() != null ? assignment.getDueDate() : closeDate));
 
         if (!require_due_date){
             require_due_container.decorators = display_none_list;
         }
 
 
         Boolean require_date = (assignment.getAcceptUntilDate() != null);
         UIBoundBoolean require = UIBoundBoolean.make(form, "require_accept_until", "#{Assignment2Bean.requireAcceptUntil}", require_date);
         require.mustapply = true;
 
         UIOutput require_container = UIOutput.make(form, "accept_until_container");
         UIInput acceptUntilDateField = UIInput.make(form, "accept_until:", assignment2OTP + ".acceptUntilDate");
         dateEvolver.setInvalidDateKey("assignment2.assignment_add.invalid_accept_until_date");
         dateEvolver.evolveDateInput(acceptUntilDateField, (assignment.getAcceptUntilDate() != null ? assignment.getAcceptUntilDate() : closeDate));
 
         if (!require_date){
             require_container.decorators = display_none_list;
         }
 
         //Assignment Count for How many Submissions
         Integer current_num_submissions = 1;
         if (assignment != null) {
             current_num_submissions = assignment.getNumSubmissionsAllowed();
         }
         int size = 20;
         String[] number_submissions_options = new String[size+1];
         String[] number_submissions_values = new String[size+1];
         number_submissions_values[0] = "" + AssignmentConstants.UNLIMITED_SUBMISSION;
         number_submissions_options[0] = messageLocator.getMessage("assignment2.indefinite_resubmit");
         for (int i=0; i < size; i++){
             number_submissions_values[i + 1] = Integer.valueOf(i+1).toString();
             number_submissions_options[i + 1] = Integer.valueOf(i+1).toString();
         }
         UISelect.make(form, "number_submissions", number_submissions_values, number_submissions_options, 
                 assignment2OTP + ".numSubmissionsAllowed", current_num_submissions.toString());
 
 
         //Submission Types
         String[] submission_type_values = new String[] {
                 String.valueOf(AssignmentConstants.SUBMIT_INLINE_ONLY),
                 String.valueOf(AssignmentConstants.SUBMIT_ATTACH_ONLY),
                 String.valueOf(AssignmentConstants.SUBMIT_INLINE_AND_ATTACH),
                 String.valueOf(AssignmentConstants.SUBMIT_NON_ELECTRONIC)
         };
         String[] submisison_type_labels = new String[] {
                 "assignment2.submission_type." + String.valueOf(AssignmentConstants.SUBMIT_INLINE_ONLY),
                 "assignment2.submission_type." + String.valueOf(AssignmentConstants.SUBMIT_ATTACH_ONLY),
                 "assignment2.submission_type." + String.valueOf(AssignmentConstants.SUBMIT_INLINE_AND_ATTACH),
                 "assignment2.submission_type." + String.valueOf(AssignmentConstants.SUBMIT_NON_ELECTRONIC)
         };
         UISelect.make(form, "submission_type", submission_type_values,
                 submisison_type_labels, assignment2OTP + ".submissionType").setMessageKeys();
 
         //Rich Text Input
         UIInput instructions = UIInput.make(form, "instructions:", assignment2OTP + ".instructions");
         instructions.mustapply = Boolean.TRUE;
         richTextEvolver.evolveTextInput(instructions);
 
         //Calendar Due Date
         if (externalLogic.siteHasTool(currentContextId, ExternalLogic.TOOL_ID_SCHEDULE)) {
             UIOutput.make(form, "add_to_schedule_container");
             UIBoundBoolean.make(form, "schedule", assignment2OTP + ".addedToSchedule");
         }
 
         //Announcement -  only display if site has the announcements tool
         if (externalLogic.siteHasTool(currentContextId, ExternalLogic.TOOL_ID_ANNC)) {
             UIBoundBoolean.make(form, "announcement", assignment2OTP + ".hasAnnouncement");
         }
 
         //Honor Pledge
         UIBoundBoolean.make(form, "honor_pledge", assignment2OTP + ".honorPledge");
 
         //Attachments
         UIInputMany attachmentInput = UIInputMany.make(form, "attachment_list:", assignment2OTP + ".assignmentAttachmentRefs", 
                 assignment.getAssignmentAttachmentRefs());
         attachmentInput.mustapply = true;
         attachmentInputEvolver.evolveAttachment(attachmentInput);
 
         UIOutput.make(form, "no_attachments_yet", messageLocator.getMessage("assignment2.assignment_add.no_attachments"));
         
         UIInternalLink.make(form, "add_attachments", UIMessage.make("assignment2.assignment_add.add_attachments"),
                 new FilePickerHelperViewParams(AddAttachmentHelperProducer.VIEWID, Boolean.TRUE, 
                         Boolean.TRUE, 500, 700, OTPKey));
 
         /********
          * Require Submissions
          */
         UIBoundBoolean.make(form, "require_submissions", assignment2OTP + ".requiresSubmission");
 
         /********
          *Grading
          */  
         //Get Gradebook Items
         List<GradebookItem> gradebook_items = externalGradebookLogic.getAllGradebookItems(currentContextId);
         //Get an Assignment for currently selected from the select box
         // by default this the first item on the list returned from the externalGradebookLogic
         // this will be overwritten if we have a pre-existing assignment with an assigned
         // item
         GradebookItem currentSelected = null;
         for (GradebookItem gi : gradebook_items){
             if (gi.getGradebookItemId().equals(assignment.getGradebookItemId())){
                 currentSelected = gi;
             }
         }
 
         String[] gradebook_item_labels = new String[gradebook_items.size()+1];
         String[] gradebook_item_values = new String[gradebook_items.size()+1];
         gradebook_item_values[0] = "0";
         gradebook_item_labels[0] = messageLocator.getMessage("assignment2.assignment_add.gradebook_item_select");
         String js_gradebook_items_data = "var gradebook_items_date = {\n";
         js_gradebook_items_data += "0: \"" + messageLocator.getMessage("assignment2.assignment_add.gradebook_item_not_selected") + "\"\n";
         for (int i=1; i <= gradebook_items.size(); i++) {
             //Fill out select options
             gradebook_item_labels[i] = gradebook_items.get(i-1).getTitle();
             gradebook_item_values[i] = gradebook_items.get(i-1).getGradebookItemId().toString();
 
             //store js hash of id => due_date string
             js_gradebook_items_data += "," + gradebook_items.get(i-1).getGradebookItemId().toString();
             if(gradebook_items.get(i-1).getDueDate() != null){
                 js_gradebook_items_data += ":\"" + df.format(gradebook_items.get(i-1).getDueDate()) + "\"\n";
             }else{
                 js_gradebook_items_data += ":\"" + messageLocator.getMessage("assignment2.assignment_add.gradebook_item_no_due_date") + "\"\n";
             }
         }
         js_gradebook_items_data += "}";
         UISelect.make(form, "gradebook_item",gradebook_item_values, gradebook_item_labels, assignment2OTP + ".gradebookItemId"); 
 
         //Radio Buttons for Grading
         String [] grading_values = new String[] {
                 Boolean.TRUE.toString(), Boolean.FALSE.toString()
         };
         String [] grading_labels = new String[] {
                "assignment2.assignment_add.assignment_graded",
                "assignment2.assignment_add.assignment_ungraded"
         };
         UISelect grading_select = UISelect.make(form, "graded-radios", 
                 grading_values, grading_labels, assignment2OTP + ".graded").setMessageKeys();
         String grading_select_id = grading_select.getFullID();
         UISelectChoice.make(form, "select_graded", grading_select_id, 0);
         UISelectLabel.make(form, "select_graded_label", grading_select_id, 0);
 
         UISelectChoice.make(form, "select_ungraded", grading_select_id, 1);
         UISelectLabel.make(form, "select_ungraded_label", grading_select_id, 1);
 
         //Output the JS vars
         UIVerbatim.make(tofill, "gradebook_items_data", js_gradebook_items_data);
 
 
         //Links to gradebook Helper
         String urlWithNameParam = externalLogic.getUrlForGradebookItemHelper(null, assignment.getTitle(), FinishedHelperProducer.VIEWID);
         UILink.make(form, "gradebook_item_new_helper",
                 UIMessage.make("assignment2.assignment_add.gradebook_item_new_helper"),
                 urlWithNameParam);
         // this link will be hidden and used as a base for adding the user-entered title as a param via javascript
         String urlWithoutNameParam = externalLogic.getUrlForGradebookItemHelper(null, FinishedHelperProducer.VIEWID);
         UILink.make(form, "gradebook_url_without_name", urlWithoutNameParam);
 
 
         /******
          * Access
          */
         UIMessage.make(form, "access_legend", "assignment2.assignment_add.access_legend");
         String[] access_values = new String[] {
                 Boolean.FALSE.toString(),
                 Boolean.TRUE.toString()
         };
         String[] access_labels = new String[] {
                 "assignment2.assignment_add.access.not_restricted",
                 "assignment2.assignment_add.access.restricted"
         };
         Boolean restrictedToGroups = (assignment.getAssignmentGroupSet() != null && !assignment.getAssignmentGroupSet().isEmpty());
         UISelect access = UISelect.make(form, "access_select", access_values, access_labels,
                 "#{Assignment2Bean.restrictedToGroups}", restrictedToGroups.toString()).setMessageKeys();
 
         String accessId = access.getFullID();
 
         for (int i=0; i < access_values.length; i++) {
             UIBranchContainer access_row = UIBranchContainer.make(form, "access_row:");
             UISelectChoice.make(access_row, "access_choice", accessId, i);
             UISelectLabel.make(access_row, "access_label", accessId, i);
         }
 
         /**
          * Groups
          */
         Collection<Group> groups = externalLogic.getSiteGroups(currentContextId);
         if (groups.size() > 0) {
             UIOutput.make(form, "access-selection-area");
             List<String> currentGroups = assignment.getListOfAssociatedGroupReferences();
             for (Group g : groups){
                 //Update OTP
                 UIBranchContainer groups_row = UIBranchContainer.make(form, "groups_row:");
                 UIBoundBoolean checkbox = UIBoundBoolean.make(groups_row, "group_check",  
                         "Assignment2Bean.selectedIds." + g.getId(), 
                         (currentGroups == null || !currentGroups.contains(g.getId()) ? Boolean.FALSE : Boolean.TRUE));
                 UIOutput.make(groups_row, "group_label", g.getTitle());
                 UIOutput.make(groups_row, "group_description", g.getDescription());
             }
         }
 
         //Notifications
         UIBoundBoolean.make(form, "sub_notif", assignment2OTP + ".sendSubmissionNotifications");
 
         //Post Buttons
         UICommand.make(form, "post_assignment", UIMessage.make("assignment2.assignment_add.post"), "Assignment2Bean.processActionPost");
         UICommand.make(form, "preview_assignment", UIMessage.make("assignment2.assignment_add.preview"), "Assignment2Bean.processActionPreview");
 
         if (assignment == null || assignment.getId() == null || assignment.isDraft()){
             UICommand.make(form, "save_draft", UIMessage.make("assignment2.assignment_add.save_draft"), "Assignment2Bean.processActionSaveDraft");
         }
         UICommand.make(form, "cancel_assignment", UIMessage.make("assignment2.assignment_add.cancel_assignment"), "Assignment2Bean.processActionCancel");
 
     }
 
     public ViewParameters getViewParameters() {
         return new AssignmentViewParams();
     }
 
     public void setMessageLocator(MessageLocator messageLocator) {
         this.messageLocator = messageLocator;
     }
 
     public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
         this.richTextEvolver = richTextEvolver;
     }
 
     public void setExternalLogic(ExternalLogic externalLogic) {
         this.externalLogic = externalLogic;
     }
 
     public void setExternalGradebookLogic(ExternalGradebookLogic externalGradebookLogic) {
         this.externalGradebookLogic = externalGradebookLogic;
     }
 
     public void setLocale(Locale locale) {
         this.locale = locale;
     }
 
     public void setAssignment2EntityBeanLocator(EntityBeanLocator entityBeanLocator) {
         this.assignment2BeanLocator = entityBeanLocator;
     }
 
     public void setAttachmentInputEvolver(AttachmentInputEvolver attachmentInputEvolver)
     {
         this.attachmentInputEvolver = attachmentInputEvolver;
     }
 }
