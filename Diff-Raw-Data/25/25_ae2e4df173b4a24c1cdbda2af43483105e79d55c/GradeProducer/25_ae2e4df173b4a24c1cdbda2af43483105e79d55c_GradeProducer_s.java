 package org.sakaiproject.assignment2.tool.producers;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.AssignmentPermissionLogic;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.model.FeedbackAttachment;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.assignment2.tool.params.FilePickerHelperViewParams;
 import org.sakaiproject.assignment2.tool.params.FragmentViewSubmissionViewParams;
 import org.sakaiproject.assignment2.tool.params.GradeViewParams;
 import org.sakaiproject.assignment2.tool.params.ViewSubmissionsViewParams;
 import org.sakaiproject.assignment2.tool.producers.fragments.FragmentAssignmentInstructionsProducer;
 import org.sakaiproject.assignment2.tool.producers.fragments.FragmentAttachmentsProducer;
 import org.sakaiproject.assignment2.tool.producers.fragments.FragmentGradebookDetailsProducer;
 import org.sakaiproject.assignment2.tool.producers.fragments.FragmentSubmissionGradePreviewProducer;
 import org.sakaiproject.assignment2.tool.producers.fragments.FragmentViewSubmissionProducer;
 import org.sakaiproject.assignment2.tool.producers.renderers.AttachmentListRenderer;
 import org.sakaiproject.assignment2.tool.producers.renderers.GradebookDetailsRenderer;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.tool.api.ToolSession;
 
 import uk.org.ponder.beanutil.entity.EntityBeanLocator;
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIELBinding;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInput;
 import uk.org.ponder.rsf.components.UIInternalLink;
 import uk.org.ponder.rsf.components.UILink;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UISelect;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
 import uk.org.ponder.rsf.evolvers.TextInputEvolver;
 import uk.org.ponder.rsf.flow.ARIResult;
 import uk.org.ponder.rsf.flow.ActionResultInterceptor;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
 
 public class GradeProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter, ActionResultInterceptor {
 
     public static final String VIEW_ID = "grade";
     public String getViewID() {
         return VIEW_ID;
     }
     
     private TextInputEvolver richTextEvolver;
     private MessageLocator messageLocator;
     private AssignmentLogic assignmentLogic;
     private ExternalLogic externalLogic;
     private Locale locale;
     private SessionManager sessionManager;
     private AttachmentListRenderer attachmentListRenderer;
     private AssignmentSubmissionLogic submissionLogic;
     private GradebookDetailsRenderer gradebookDetailsRenderer;
     private EntityBeanLocator asvEntityBeanLocator;
     private AssignmentPermissionLogic permissionLogic;
 		
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
     
     public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
     	
     	//Clear out session attachment information if everything successful
     	ToolSession session = sessionManager.getCurrentToolSession();
     	session.removeAttribute("attachmentRefs");
     	session.removeAttribute("removedAttachmentRefs");
     	
     	//Edit Permission
         Boolean edit_perm = permissionLogic.isCurrentUserAbleToEditAssignments(externalLogic.getCurrentContextId());
     	
     	//Get Params
         GradeViewParams params = (GradeViewParams) viewparams;
     	String userId = params.userId;
     	Long assignmentId = params.assignmentId;
     	if (assignmentId == null || userId == null){
     		//handle error
     		return;
     	}
     	Boolean OLD_VERSION = false;
     	//Check if we are modifying an older version
     	if (params.submissionId != null){
     		OLD_VERSION = true;
     	}
     	
     	AssignmentSubmission as = submissionLogic.getCurrentSubmissionByAssignmentIdAndStudentId(assignmentId, userId);
     	Assignment2 assignment = assignmentLogic.getAssignmentByIdWithAssociatedData(assignmentId);
     	
        	// use a date which is related to the current users locale
         DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
         
     	//Breadcrumbs
         UIInternalLink.make(tofill, "breadcrumb", 
         		messageLocator.getMessage("assignment2.assignment_list-sortview.heading"),
         		new SimpleViewParameters(AssignmentListSortViewProducer.VIEW_ID));
         UIInternalLink.make(tofill, "breadcrumb2",
         		messageLocator.getMessage("assignment2.assignment_grade-assignment.heading", new Object[] { assignment.getTitle()}),
         		new ViewSubmissionsViewParams(ViewSubmissionsProducer.VIEW_ID, assignment.getId()));
         UIMessage.make(tofill, "last_breadcrumb", "assignment2.assignment_grade.heading", 
         		new Object[]{assignment.getTitle(), externalLogic.getUserDisplayName(params.userId)});
         
         //Heading messages
         UIMessage.make(tofill, "heading", "assignment2.assignment_grade.heading", 
         		new Object[]{assignment.getTitle(), externalLogic.getUserDisplayName(params.userId)});
         UIMessage.make(tofill, "page-title", "assignment2.assignment_grade.title");
         //navBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID);
         //UIMessage.make(tofill, "heading", "assignment2.assignment_grade.heading", new Object[]{assignment.getTitle()});
         
         //AssignmentSubmission OTP Stuff
         String asOTP = "AssignmentSubmission.";
         String OTPKey = "";
         if (as != null && as.getId() != null){
         	OTPKey += as.getId();
         } else {
         	OTPKey += EntityBeanLocator.NEW_PREFIX + "1";
         }
         asOTP += OTPKey;
         
         //AssignmentSubmissionVersion OTP Stuff
         String asvOTP = "AssignmentSubmissionVersion.";
         String asvOTPKey = "";
         if (OLD_VERSION && params.submissionId != null) {
         	asvOTPKey += params.submissionId;
         }else if (as != null && as.getCurrentSubmissionVersion() != null && as.getCurrentSubmissionVersion().getId() != null) {
         	asvOTPKey += as.getCurrentSubmissionVersion().getId();
         } else {
         	asvOTPKey += EntityBeanLocator.NEW_PREFIX + "1";
         }
         AssignmentSubmissionVersion assignmentSubmissionVersion = (AssignmentSubmissionVersion)asvEntityBeanLocator.locateBean(asvOTPKey);
         asvOTP += asvOTPKey;
         
       //Initialize js otpkey
     	UIVerbatim.make(tofill, "attachment-ajax-init", "otpkey=\"" + org.sakaiproject.util.Web.escapeUrl(asvOTPKey) + "\";\n" +
     			"userId=\"" + userId + "\";\n" +
     			"assignmentId=\"" + assignmentId + "\";\n" +
     			"fragAttachPath=\"" + externalLogic.getAssignmentViewUrl(FragmentAttachmentsProducer.VIEW_ID) + "\";\n" +
     			"fragGBDetailsPath=\"" + externalLogic.getAssignmentViewUrl(FragmentGradebookDetailsProducer.VIEW_ID) + "\";");
         
     	
     	/**
     	 * Begin the Form
     	 */
         UIForm form = UIForm.make(tofill, "form");
         
         if (assignmentSubmissionVersion.getSubmittedTime() != null){
         	UIOutput.make(form, "status", df.format(assignmentSubmissionVersion.getSubmittedTime()));
         } else {
         	UIOutput.make(form, "status", "");
         }
         
         int statusConstant = as != null && as.getSubmissionStatusConstant() != null ? as.getSubmissionStatusConstant() : AssignmentConstants.SUBMISSION_NOT_STARTED;
         
         if (statusConstant == AssignmentConstants.SUBMISSION_IN_PROGRESS || statusConstant == AssignmentConstants.SUBMISSION_NOT_STARTED) {
         	UIMessage.make(form, "status", "assignment2.assignment_grade.submission_status." + statusConstant);
         } else {
         		UIMessage.make(form, "status", "assignment2.assignment_grade.submission_status." + statusConstant, new Object[] { df.format(assignmentSubmissionVersion.getSubmittedTime()) });
         }
         
         //If current submitted submission is a draft, display note to instructor
         if (submissionLogic.isMostRecentVersionDraft(as) && !OLD_VERSION){
         	UIMessage.make(form, "current_is_draft", "assignment2.assignment_grade.current_is_draft");
         }
         
         //If editing Old Version, remind UI
         if (OLD_VERSION) {
         	UIMessage.make(form, "editing_previous_submission", "assignment2.assignment_grade.editing_previous_submission");
         }
 
         //TODO - make these viewparams their own actual view params, not raw
         UILink.make(form, "view_assignment_instructions", 
         		messageLocator.getMessage("assignment2.assignment_grade.view_assignment_instructions"),
         		externalLogic.getAssignmentViewUrl(FragmentAssignmentInstructionsProducer.VIEW_ID) + "/" + assignment.getId() + "?TB_iframe=true&height=300");
         
         //If assignment allows for submitted text
         if (assignmentSubmissionVersion.getSubmittedTime() != null &&
         		(assignment.getSubmissionType() == AssignmentConstants.SUBMIT_INLINE_ONLY || 
         		assignment.getSubmissionType() == AssignmentConstants.SUBMIT_INLINE_AND_ATTACH)) {
         	UIOutput.make(form, "submitted_text_fieldset");
         
         
 	        UIVerbatim.make(form, "feedback_instructions", messageLocator.getMessage("assignment2.assignment_grade.feedback_instructions"));
 	        UIInput feedback_text = UIInput.make(form, "feedback_text:", asvOTP + ".annotatedText");
 	        feedback_text.mustapply = Boolean.TRUE;
 	        richTextEvolver.evolveTextInput(feedback_text);
         }
         
         //If assignment allows for submitted attachments
         if (assignment.getSubmissionType() == AssignmentConstants.SUBMIT_ATTACH_ONLY ||
         		assignment.getSubmissionType() == AssignmentConstants.SUBMIT_INLINE_AND_ATTACH) {
 	        if (assignmentSubmissionVersion.getSubmissionAttachSet() != null){
 	        	UIOutput.make(tofill, "submitted_attachments_fieldset");
 	        	attachmentListRenderer.makeAttachmentFromSubmissionAttachmentSet(tofill, "submitted_attachment_list:", params.viewID, 
 	        			assignmentSubmissionVersion.getSubmissionAttachSet(), Boolean.FALSE);
 	        } else {
 	        	UIMessage.make(tofill, "submitted_attachment_list:", "assignment2.assignment_grade.no_attachments_submitted");
 	        }
         }
         
     	UIInput feedback_notes = UIInput.make(form, "feedback_notes:", asvOTP + ".feedbackNotes");
     	feedback_notes.mustapply = Boolean.TRUE;
     	richTextEvolver.evolveTextInput(feedback_notes);
                
         //Attachments
         Set<FeedbackAttachment> afaSet = new HashSet<FeedbackAttachment>();
         if (assignmentSubmissionVersion.getFeedbackAttachSet() != null) {
         	afaSet.addAll(assignmentSubmissionVersion.getFeedbackAttachSet());
         }
         attachmentListRenderer.makeAttachmentFromFeedbackAttachmentSet(tofill, "attachment_list:", 
         		params.viewID, afaSet, Boolean.TRUE);
         UIInternalLink.make(form, "add_attachments", UIMessage.make("assignment2.assignment_add.add_attachments"),
         		new FilePickerHelperViewParams(AddAttachmentHelperProducer.VIEWID, Boolean.TRUE, 
         				Boolean.TRUE, 500, 700, OTPKey));
         
         //set dateEvolver
         dateEvolver.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);
         
         /**
         UIBoundBoolean release_feedback = UIBoundBoolean.make(form, "release_feedback", "#{AssignmentSubmissionBean.releaseFeedback}", 
         		assignmentSubmissionVersion.getReleasedTime() != null);
         UIMessage release_feedback_label = UIMessage.make(form, "release_feedback_label", "assignment2.assignment_grade.release_feedback");
         UILabelTargetDecorator.targetLabel(release_feedback_label, release_feedback);
         **/
         
         //Assignment LEvel
         //Integer assignment_num_submissions = 1;
         //if (assignment != null && assignment.getNumSubmissionsAllowed() != null) {
         //	assignment_num_submissions = assignment.getNumSubmissionsAllowed();
         //}
         
         //Submission Level
         Integer current_num_submissions = 1;
         if (as != null && as.getNumSubmissionsAllowed() != null) {
         	current_num_submissions = as.getNumSubmissionsAllowed();
         }
         int size = 20;
         String[] number_submissions_options = new String[size+1];
         String[] number_submissions_values = new String[size+1];
         number_submissions_values[0] = "-1";
         number_submissions_options[0] = messageLocator.getMessage("assignment2.indefinite_resubmit");
         for (int i=0; i < size; i++){
         	number_submissions_values[i + 1] = new Integer(i + current_num_submissions).toString();
         	number_submissions_options[i + 1] = new Integer(i).toString();
         }
         
         //Output
         UIMessage.make(form, "resubmission_text_1", "assignment2.assignment_grade.resubmission_text_1", 
         		new Object[] { externalLogic.getUserDisplayName(params.userId), current_num_submissions});
         
         UISelect.make(form, "resubmission_additional", number_submissions_values, number_submissions_options, 
         		asOTP + ".numSubmissionsAllowed", current_num_submissions.toString());
         
         if (as.getResubmitCloseTime() == null) {
         	as.setResubmitCloseTime(new Date());
         }
         UIInput acceptUntilTimeField = UIInput.make(form, "accept_until:", asOTP + ".resubmitCloseTime");
         dateEvolver.evolveDateInput(acceptUntilTimeField, as.getResubmitCloseTime());
         
         
         
         if (!assignment.isUngraded()){
         	gradebookDetailsRenderer.makeGradebookDetails(tofill, "gradebook_details", as, assignmentId, userId);
         }        
         
         
         
         //Begin Looping for previous submissions
         List<AssignmentSubmissionVersion> history = submissionLogic.getVersionHistoryForSubmission(as);
                 
     	for (AssignmentSubmissionVersion asv : history){
     		
         	UIBranchContainer loop = UIBranchContainer.make(form, "previous_submissions:");
         	UIOutput.make(loop, "previous_date", (asv.getSubmittedTime() != null ? df.format(asv.getSubmittedTime()) : ""));
         	if (asvOTPKey.equals(asv.getId().toString())){
         		//we are editing this version
         		UIMessage.make(loop, "current_version", "assignment2.assignment_grade.current_version");
         	} else {
         		//else add link to edit this submission
         		UIInternalLink.make(loop, "previous_link", 
         				messageLocator.getMessage("assignment2.assignment_grade.view_submission"),
             			new FragmentViewSubmissionViewParams(FragmentViewSubmissionProducer.VIEW_ID, asv.getId()));
         	}
         }
         if (history == null || history.size() == 0) {
         	//no history, add dialog
         	UIMessage.make(form, "no_history", "assignment2.assignment_grade.no_history");
         }
         
         
         
         form.parameters.add(new UIELBinding("#{AssignmentSubmissionBean.assignmentId}", assignmentId));
         form.parameters.add(new UIELBinding("#{AssignmentSubmissionBean.userId}", userId));
         if (edit_perm){
         	UICommand.make(form, "release_feedback", UIMessage.make("assignment2.assignment_grade.release_feedback"),
    			"#{AssignmentSubmissionBean.processActionReleaseAllFeedbackForSubmission}");
 	        UICommand.make(form, "submit", UIMessage.make("assignment2.assignment_grade.submit"), "#{AssignmentSubmissionBean.processActionGradeSubmit}");
 	        //UICommand.make(form, "preview", UIMessage.make("assignment2.assignment_grade.preview"), "#{AssignmentSubmissionBean.processActionGradePreview}");
 	        UICommand.make(form, "cancel", UIMessage.make("assignment2.assignment_grade.cancel"), "#{AssignmentSubmissionBean.processActionCancel}");
         }
     }
     
 	public List<NavigationCase> reportNavigationCases() {
     	List<NavigationCase> nav= new ArrayList<NavigationCase>();
     	nav.add(new NavigationCase("release_all", new GradeViewParams(
     			GradeProducer.VIEW_ID, null, null)));
     	nav.add(new NavigationCase("submit", new ViewSubmissionsViewParams(
                ViewSubmissionsProducer.VIEW_ID)));
         nav.add(new NavigationCase("preview", new SimpleViewParameters(
               FragmentSubmissionGradePreviewProducer.VIEW_ID)));
         nav.add(new NavigationCase("cancel", new ViewSubmissionsViewParams(
                 ViewSubmissionsProducer.VIEW_ID)));
         return nav;
     }
 	
 	public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
 		    if (result.resultingView instanceof ViewSubmissionsViewParams) {
 		    	ViewSubmissionsViewParams outgoing = (ViewSubmissionsViewParams) result.resultingView;
 		    	GradeViewParams in = (GradeViewParams) incoming;
 		    	outgoing.assignmentId = in.assignmentId;
 		    } else if (result.resultingView instanceof GradeViewParams) {
 		    	GradeViewParams outgoing = (GradeViewParams) result.resultingView;
 		    	GradeViewParams in = (GradeViewParams) incoming;
 		    	outgoing.assignmentId = in.assignmentId;
 		    	outgoing.userId = in.userId;
 		    	outgoing.submissionId = in.submissionId;
 		    }
 	}
 	
     public ViewParameters getViewParameters() {
         return new GradeViewParams();
     }
     
     public void setMessageLocator(MessageLocator messageLocator) {
         this.messageLocator = messageLocator;
     }
     
     public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
         this.richTextEvolver = richTextEvolver;
     }
     
     public void setAssignmentLogic(AssignmentLogic assignmentLogic) {
     	this.assignmentLogic = assignmentLogic;
     }
     
     public void setExternalLogic(ExternalLogic externalLogic) {
     	this.externalLogic = externalLogic;
     }
     
     public void setLocale(Locale locale) {
     	this.locale = locale;
     }
 
 	public void setSessionManager(SessionManager sessionManager) {
 		this.sessionManager = sessionManager;
 	}
 	
 	public void setAttachmentListRenderer(AttachmentListRenderer attachmentListRenderer){
 		this.attachmentListRenderer = attachmentListRenderer;
 	}
 
 	public void setSubmissionLogic(AssignmentSubmissionLogic submissionLogic) {
 		this.submissionLogic = submissionLogic;
 	}
     
     public void setGradebookDetailsRenderer(GradebookDetailsRenderer gradebookDetailsRenderer){
 		this.gradebookDetailsRenderer = gradebookDetailsRenderer;
 	}
 
 	public void setAsvEntityBeanLocator(EntityBeanLocator asvEntityBeanLocator) {
 		this.asvEntityBeanLocator = asvEntityBeanLocator;
 	}
 
 	public void setPermissionLogic(AssignmentPermissionLogic permissionLogic) {
 		this.permissionLogic = permissionLogic;
 	}
 }
