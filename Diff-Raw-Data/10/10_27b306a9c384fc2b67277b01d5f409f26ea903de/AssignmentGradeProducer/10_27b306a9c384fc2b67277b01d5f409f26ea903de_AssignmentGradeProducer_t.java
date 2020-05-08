 package org.sakaiproject.assignment2.tool.producers;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.tool.beans.Assignment2Bean;
 import org.sakaiproject.assignment2.tool.beans.PreviewAssignmentBean;
 import org.sakaiproject.assignment2.tool.params.AssignmentGradeAssignmentViewParams;
 import org.sakaiproject.assignment2.tool.params.AssignmentGradeViewParams;
 import org.sakaiproject.assignment2.tool.params.FilePickerHelperViewParams;
 import org.sakaiproject.assignment2.tool.producers.FragmentSubmissionGradePreviewProducer;
 import org.sakaiproject.entitybroker.EntityBroker;
 import org.sakaiproject.entitybroker.IdEntityReference;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.tool.api.ToolSession;
 
 import uk.org.ponder.beanutil.entity.EntityBeanLocator;
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.rsf.components.UIBoundBoolean;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIELBinding;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInput;
 import uk.org.ponder.rsf.components.UIInternalLink;
 import uk.org.ponder.rsf.components.UILink;
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
 
     public static final String VIEW_ID = "assignment_grade";
     public String getViewID() {
         return VIEW_ID;
     }
     
     
     private NavBarRenderer navBarRenderer;
     private TextInputEvolver richTextEvolver;
     private MessageLocator messageLocator;
     private AssignmentLogic assignmentLogic;
     private ExternalLogic externalLogic;
     private ExternalGradebookLogic externalGradebookLogic;
     private PreviewAssignmentBean previewAssignmentBean;
     private Locale locale;
     private Assignment2Bean assignment2Bean;
     private SessionManager sessionManager;
     private EntityBeanLocator assignment2BeanLocator;
     private AttachmentListRenderer attachmentListRenderer;
     private AssignmentSubmissionLogic submissionLogic;
     private EntityBroker entityBroker;
     
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
     	
     	//Init JS
         String frameId = org.sakaiproject.util.Web.escapeJavascript("Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId());
         UIVerbatim.make(tofill, "iframeId_init", "var iframeId = \"" + frameId + "\";");
         
     	
     	AssignmentSubmission as = submissionLogic.getCurrentSubmissionByAssignmentIdAndStudentIdForInstructorView(assignmentId, userId);
     	Assignment2 assignment = assignmentLogic.getAssignmentByIdWithAssociatedData(assignmentId);
     	
        	// use a date which is related to the current users locale
         DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
         
         //Heading messages
         UIMessage.make(tofill, "page-title", "assignment2.assignment_grade.title");
         navBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID);
         UIMessage.make(tofill, "heading", "assignment2.assignment_grade.heading", new Object[]{assignment.getTitle()});
         
         String asOTP = "AssignmentSubmission.";
         String OTPKey = "";
         if (as != null && as.getSubmissionId() != null){
         	OTPKey += as.getSubmissionId();
         } else {
         	OTPKey += EntityBeanLocator.NEW_PREFIX + "1";
         }
         asOTP += OTPKey;
         String asvOTP = asOTP + ".currentSubmissionVersion";
         
     	//Initialize js otpkey
     	UIVerbatim.make(tofill, "attachment-ajax-init", "otpkey=\"" + org.sakaiproject.util.Web.escapeUrl(OTPKey) + "\"");
         
     	
     	/**
     	 * Begin the Form
     	 */
         UIForm form = UIForm.make(tofill, "form");
         
         UIOutput.make(form, "details_student", externalLogic.getUserDisplayName(userId));
         if (as != null && as.getCurrentSubmissionVersion() != null && as.getCurrentSubmissionVersion().getSubmittedText() != null){
         	UIOutput.make(form, "details_submitted_date", df.format(as.getCurrentSubmissionVersion().getSubmittedTime()));
         } else {
         	UIOutput.make(form, "details_submitted_date", "");
         }
         String status = (as != null && as.getSubmissionStatus() != null ? as.getSubmissionStatus() : "0");
         UIMessage.make(form, "deatils_status", "assignment2.submission_status." + status);
         
         UIVerbatim.make(form, "assignment_instructions", assignment.getInstructions());
         attachmentListRenderer.makeAttachmentFromAssignmentAttachmentSet(tofill, "assignment_attachment_list:", params.viewID, 
         		assignment.getAttachmentSet(), Boolean.FALSE);
         
         UIVerbatim.make(form, "feedback_instructions", messageLocator.getMessage("assignment2.assignment_grade.feedback_instructions"));
         UIInput feedback_text = UIInput.make(form, "feedback_text:", asvOTP + ".feedbackText");
         richTextEvolver.evolveTextInput(feedback_text);
         
         if (as != null && as.getCurrentSubmissionVersion() != null && as.getCurrentSubmissionVersion().getSubmissionAttachSet() != null){
         	attachmentListRenderer.makeAttachmentFromAssignmentSubmissionAttachmentSet(tofill, "submitted_attachment_list:", params.viewID, 
             		as.getCurrentSubmissionVersion().getSubmissionAttachSet(), Boolean.FALSE);
         } else {
         	UIMessage.make(tofill, "submitted_attachment_list:", "assignment2.assignment_grade.no_attachments_submitted");
         }
         
         /** This now goes in the helper
         UIInput gradebook_comment = UIInput.make(form, "gradebook_comment:", asOTP + ".gradebookComment");
         richTextEvolver.evolveTextInput(gradebook_comment);
         ***/
         
         //Attachments
         attachmentListRenderer.makeAttachmentFromAssignment2OTPAttachmentSet(tofill, "attachment_list:", 
         		params.viewID, OTPKey, Boolean.TRUE);
         UIInternalLink.make(form, "add_attachments", UIMessage.make("assignment2.assignment_add.add_attachments"),
         		new FilePickerHelperViewParams(AddAttachmentHelperProducer.VIEWID, Boolean.TRUE, 
         				Boolean.TRUE, 500, 700, OTPKey));
         
         
         //Grading Helper Link
         String ref = new IdEntityReference("grade-entry-grade", externalLogic.getCurrentContextId()).toString();
         String url = entityBroker.getEntityURL(ref);
         String contextId = externalLogic.getCurrentContextId();
         UILink.make(form, "gradebook_grading_helper",
         		UIMessage.make("assignment2.assignment_grade.gradebook_grade"),
        		url + "?TB_iframe=true&width=700&height=500&KeepThis=true" +
        				"&contextId=" + contextId + "&userId=" + userId + "&assignmentId=" + assignment.getGradableObjectId());
      
         
         UIBoundBoolean.make(form, "allow_resubmit", asOTP + ".allowResubmit");
         UIInput acceptUntilTimeField = UIInput.make(form, "accept_until:", asOTP + ".resubmitCloseTime");
         dateEvolver.evolveDateInput(acceptUntilTimeField, null);
         
         form.parameters.add(new UIELBinding("#{AssignmentSubmissionBean.assignmentId}", assignmentId));
         form.parameters.add(new UIELBinding("#{AssignmentSubmissionBean.userId}", userId));
         
         UICommand.make(form, "submit", UIMessage.make("assignment2.assignment_grade.submit"), "#{AssignmentSubmissionBean.processActionGradeSubmit}");
         UICommand.make(form, "preview", UIMessage.make("assignment2.assignment_grade.preview"), "#{AssignmentSubmissionBean.processActionGradePreview}");
         UICommand.make(form, "cancel", UIMessage.make("assignment2.assignment_grade.cancel"), "#{AssignmentSubmissionBean.processActionCancel}");
     }
     
 	public List reportNavigationCases() {
     	List<NavigationCase> nav= new ArrayList<NavigationCase>();
     	nav.add(new NavigationCase("post", new SimpleViewParameters(
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
 
 
     public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
         this.navBarRenderer = navBarRenderer;
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
     
     public void setExternalGradebookLogic(ExternalGradebookLogic externalGradebookLogic) {
     	this.externalGradebookLogic = externalGradebookLogic;
     }
     
     public void setPreviewAssignmentBean(PreviewAssignmentBean previewAssignmentBean) {
     	this.previewAssignmentBean = previewAssignmentBean;
     }
     
     public void setLocale(Locale locale) {
     	this.locale = locale;
     }
     
     public void setAssignment2Bean(Assignment2Bean assignment2Bean) {
     	this.assignment2Bean = assignment2Bean;
     }
     
 	public void setSessionManager(SessionManager sessionManager) {
 		this.sessionManager = sessionManager;
 	}
 	
 	public void setAssignment2EntityBeanLocator(EntityBeanLocator entityBeanLocator) {
 		this.assignment2BeanLocator = entityBeanLocator;
 	}
 	
 	public void setAttachmentListRenderer(AttachmentListRenderer attachmentListRenderer){
 		this.attachmentListRenderer = attachmentListRenderer;
 	}
 
 	public void setSubmissionLogic(AssignmentSubmissionLogic submissionLogic) {
 		this.submissionLogic = submissionLogic;
 	}
 	
     public void setEntityBroker(EntityBroker entityBroker) {
         this.entityBroker = entityBroker;
     }
 }
