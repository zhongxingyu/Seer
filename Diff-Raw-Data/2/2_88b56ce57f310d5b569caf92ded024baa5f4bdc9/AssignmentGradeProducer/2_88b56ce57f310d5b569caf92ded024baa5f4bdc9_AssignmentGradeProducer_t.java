 package org.sakaiproject.assignment2.tool.producers;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 import java.util.Stack;
 
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.model.AssignmentFeedbackAttachment;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.assignment2.tool.params.AssignmentGradeAssignmentViewParams;
 import org.sakaiproject.assignment2.tool.params.AssignmentGradeViewParams;
 import org.sakaiproject.assignment2.tool.params.FilePickerHelperViewParams;
 import org.sakaiproject.assignment2.tool.params.SimpleAssignmentViewParams;
 import org.sakaiproject.assignment2.tool.producers.fragments.FragmentAttachmentsProducer;
 import org.sakaiproject.assignment2.tool.producers.fragments.FragmentSubmissionGradePreviewProducer;
 import org.sakaiproject.assignment2.tool.producers.fragments.FragmentGradebookDetailsProducer;
 import org.sakaiproject.assignment2.tool.producers.renderers.AttachmentListRenderer;
 import org.sakaiproject.assignment2.tool.producers.renderers.GradebookDetailsRenderer;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.tool.api.ToolSession;
 
 import uk.org.ponder.beanutil.entity.EntityBeanLocator;
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.rsf.components.UIBoundBoolean;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIELBinding;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInput;
 import uk.org.ponder.rsf.components.UIInternalLink;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
 import uk.org.ponder.rsf.evolvers.TextInputEvolver;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
 import uk.org.ponder.rsf.flow.ActionResultInterceptor;
 import uk.org.ponder.rsf.flow.ARIResult;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
 
 
 public class AssignmentGradeProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter, ActionResultInterceptor {
 
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
     	
     	//Get Params
     	AssignmentGradeViewParams params = (AssignmentGradeViewParams) viewparams;
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
     	
     	//Init JS
         String frameId = org.sakaiproject.util.Web.escapeJavascript("Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId());
         UIVerbatim.make(tofill, "iframeId_init", "var iframeId = \"" + frameId + "\";");
         
     	
     	AssignmentSubmission as = submissionLogic.getCurrentSubmissionByAssignmentIdAndStudentIdForInstructorView(assignmentId, userId);
     	Assignment2 assignment = assignmentLogic.getAssignmentByIdWithAssociatedData(assignmentId);
     	
        	// use a date which is related to the current users locale
         DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
         
     	//Breadcrumbs
         UIInternalLink.make(tofill, "breadcrumb", 
         		messageLocator.getMessage("assignment2.assignment_list-sortview.heading"),
         		new SimpleViewParameters(AssignmentListSortViewProducer.VIEW_ID));
         UIInternalLink.make(tofill, "breadcrumb2",
         		messageLocator.getMessage("assignment2.assignment_grade-assignment.heading", new Object[] { assignment.getTitle()}),
         		new SimpleAssignmentViewParams(AssignmentViewSubmissionsProducer.VIEW_ID, assignment.getId()));
         UIMessage.make(tofill, "last_breadcrumb", "assignment2.assignment_grade.heading", 
         		new Object[]{assignment.getTitle(), externalLogic.getUserDisplayName(params.userId)});
         
         //Heading messages
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
     	UIVerbatim.make(tofill, "attachment-ajax-init", "otpkey=\"" + org.sakaiproject.util.Web.escapeUrl(OTPKey) + "\";\n" +
     			"userId=\"" + userId + "\";\n" +
     			"assignmentId=\"" + assignmentId + "\";\n" +
     			"fragAttachPath=\"" + externalLogic.getAssignmentViewUrl(FragmentAttachmentsProducer.VIEW_ID) + "\";\n" +
     			"fragGBDetailsPath=\"" + externalLogic.getAssignmentViewUrl(FragmentGradebookDetailsProducer.VIEW_ID) + "\";");
         
     	
     	/**
     	 * Begin the Form
     	 */
         UIForm form = UIForm.make(tofill, "form");
         
         UIOutput.make(form, "details_student", externalLogic.getUserDisplayName(userId));
         if (assignmentSubmissionVersion.getSubmittedText() != null){
         	UIOutput.make(form, "details_submitted_date", df.format(assignmentSubmissionVersion.getSubmittedTime()));
         } else {
         	UIOutput.make(form, "details_submitted_date", "");
         }
         String status = (as != null && as.getSubmissionStatus() != null ? as.getSubmissionStatus() : String.valueOf(AssignmentConstants.SUBMISSION_NOT_STARTED));
         UIMessage.make(form, "details_status", "assignment2.submission_status." + status);
         
         
         //If current submitted submission is a draft, display note to instructor
         if (submissionLogic.isMostRecentVersionDraft(as) && !OLD_VERSION){
         	UIMessage.make(form, "current_is_draft", "assignment2.assignment_grade.current_is_draft");
         }
         
         //If editing Old Version, remind UI
         if (OLD_VERSION) {
         	UIMessage.make(form, "editing_previous_submission", "assignment2.assignment_grade.editing_previous_submission");
         }
         
         UIVerbatim.make(form, "assignment_instructions", assignment.getInstructions());
         attachmentListRenderer.makeAttachmentFromAssignmentAttachmentSet(tofill, "assignment_attachment_list:", params.viewID, 
         		assignment.getAttachmentSet(), Boolean.FALSE);
         
         //If assignment allows for submitted text
         if (assignment.getSubmissionType() == AssignmentConstants.SUBMIT_INLINE_ONLY || 
         		assignment.getSubmissionType() == AssignmentConstants.SUBMIT_INLINE_AND_ATTACH) {
         	UIOutput.make(form, "submitted_text_fieldset");
         
         
 	        UIVerbatim.make(form, "feedback_instructions", messageLocator.getMessage("assignment2.assignment_grade.feedback_instructions"));
 	        UIInput feedback_text = UIInput.make(form, "feedback_text:", asvOTP + ".annotatedText");
 	        richTextEvolver.evolveTextInput(feedback_text);
         }
         
         //If assignment allows for submitted attachments
         if (assignment.getSubmissionType() == AssignmentConstants.SUBMIT_ATTACH_ONLY ||
         		assignment.getSubmissionType() == AssignmentConstants.SUBMIT_INLINE_AND_ATTACH) {
 	        if (assignmentSubmissionVersion.getSubmissionAttachSet() != null){
 	        	attachmentListRenderer.makeAttachmentFromAssignmentSubmissionAttachmentSet(tofill, "submitted_attachment_list:", params.viewID, 
 	        			assignmentSubmissionVersion.getSubmissionAttachSet(), Boolean.FALSE);
 	        } else {
 	        	UIMessage.make(tofill, "submitted_attachment_list:", "assignment2.assignment_grade.no_attachments_submitted");
 	        }
         }
         
     	UIInput feedback_notes = UIInput.make(form, "feedback_notes:", asvOTP + ".feedbackNotes");
     	richTextEvolver.evolveTextInput(feedback_notes);
                
         //Attachments
         Set<AssignmentFeedbackAttachment> afaSet = new HashSet();
         if (assignmentSubmissionVersion.getFeedbackAttachSet() != null) {
         	afaSet.addAll(assignmentSubmissionVersion.getFeedbackAttachSet());
         }
         attachmentListRenderer.makeAttachmentFromAssignmentFeedbackAttachmentSet(tofill, "attachment_list:", 
         		params.viewID, afaSet, Boolean.TRUE);
         UIInternalLink.make(form, "add_attachments", UIMessage.make("assignment2.assignment_add.add_attachments"),
         		new FilePickerHelperViewParams(AddAttachmentHelperProducer.VIEWID, Boolean.TRUE, 
         				Boolean.TRUE, 500, 700, OTPKey));
         
         //set dateEvolver
         dateEvolver.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);
         
         
         UIBoundBoolean.make(form, "allow_resubmit", asOTP + ".allowResubmit");
         UIInput acceptUntilTimeField = UIInput.make(form, "accept_until:", asOTP + ".resubmitCloseTime");
         dateEvolver.evolveDateInput(acceptUntilTimeField, null);
         
         if (!assignment.isUngraded()){
         	gradebookDetailsRenderer.makeGradebookDetails(tofill, "gradebook_details", as, assignmentId, userId);
         }        
         
         //Begin Looping for previous submissions
         Set<AssignmentSubmissionVersion> history = as.getSubmissionHistorySet();
         if (history != null) {
 	        //reverse the set
 	        Stack <AssignmentSubmissionVersion> stack = new Stack();
 	        for (AssignmentSubmissionVersion asv : history) {
 	        	stack.add(asv);
 	        }
 	        
 	        while (stack.size() > 0){
 	        	AssignmentSubmissionVersion asv = stack.pop();
 	        	UIBranchContainer loop = UIBranchContainer.make(form, "previous_submissions:");
 	        	
 	        	UIMessage.make(loop, "loop_submission", "assignment2.assignment_grade.loop_submission", 
	        			new Object[] { (asv.getSubmittedTime() != null ? df.format(asv.getSubmittedTime()) : "") });
 	        	if (asvOTPKey.equals(asv.getId().toString())){
 	        		//we are editing this version
 	        		UIMessage.make(loop, "currently_editing", "assignment2.assignment_grade.currently_editing");
 	        	} else {
 	        		//else add link to edit this submission
 	        		UIInternalLink.make(loop, "loop_edit_submission", 
 	        			new AssignmentGradeViewParams(AssignmentGradeProducer.VIEW_ID, assignmentId, userId, asv.getId()));
 	        	}
 	        	UIVerbatim.make(loop, "loop_submitted_text", asv.getSubmittedText());
 	        	UIVerbatim.make(loop, "loop_feedback_text", asv.getAnnotatedTextFormatted());
 	        	UIVerbatim.make(loop, "loop_feedback_notes", asv.getFeedbackNotes());
 	        	attachmentListRenderer.makeAttachmentFromAssignmentSubmissionAttachmentSet(loop, "loop_submitted_attachment_list:", 
 	        			AssignmentGradeProducer.VIEW_ID, asv.getSubmissionAttachSet(), Boolean.FALSE);
 	        	attachmentListRenderer.makeAttachmentFromAssignmentFeedbackAttachmentSet(loop, "loop_returned_attachment_list:", 
 	        			AssignmentGradeProducer.VIEW_ID, asv.getFeedbackAttachSet(), Boolean.FALSE);
 	        	if (asv.getLastFeedbackSubmittedBy() != null) {
 		        	UIMessage.make(loop, "feedback_updated", "assignment2.assignment_grade.feedback_updated",
 		        			new Object[]{ 
 		        				(asv.getLastFeedbackTime() != null ? df.format(asv.getLastFeedbackTime()) : ""), 
 		        				externalLogic.getUserDisplayName(asv.getLastFeedbackSubmittedBy()) });
 	        	} else {
 	        		UIMessage.make(loop, "feedback_updated", "assignment2.assignment_grade.feedback_not_updated");
 	        	}
 	        }
         }
         if (history == null || history.size() == 0) {
         	//no history, add dialog
         	UIMessage.make(form, "no_history", "assignment2.assignment_grade.no_history");
         }
         
         
         
         form.parameters.add(new UIELBinding("#{AssignmentSubmissionBean.assignmentId}", assignmentId));
         form.parameters.add(new UIELBinding("#{AssignmentSubmissionBean.userId}", userId));
         
         UICommand.make(form, "submit", UIMessage.make("assignment2.assignment_grade.submit"), "#{AssignmentSubmissionBean.processActionGradeSubmit}");
         UICommand.make(form, "preview", UIMessage.make("assignment2.assignment_grade.preview"), "#{AssignmentSubmissionBean.processActionGradePreview}");
         UICommand.make(form, "cancel", UIMessage.make("assignment2.assignment_grade.cancel"), "#{AssignmentSubmissionBean.processActionCancel}");
     }
     
 	public List reportNavigationCases() {
     	List<NavigationCase> nav= new ArrayList<NavigationCase>();
     	nav.add(new NavigationCase("submit", new AssignmentGradeAssignmentViewParams(
                AssignmentViewSubmissionsProducer.VIEW_ID)));
         nav.add(new NavigationCase("preview", new SimpleViewParameters(
               FragmentSubmissionGradePreviewProducer.VIEW_ID)));
         nav.add(new NavigationCase("cancel", new AssignmentGradeAssignmentViewParams(
                 AssignmentViewSubmissionsProducer.VIEW_ID)));
         return nav;
     }
 	
 	public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
 		    if (result.resultingView instanceof AssignmentGradeAssignmentViewParams) {
 		    	AssignmentGradeAssignmentViewParams outgoing = (AssignmentGradeAssignmentViewParams) result.resultingView;
 		    	AssignmentGradeViewParams in = (AssignmentGradeViewParams) incoming;
 		    	outgoing.assignmentId = in.assignmentId;
 		    }
 	}
 	
     public ViewParameters getViewParameters() {
         return new AssignmentGradeViewParams();
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
 }
