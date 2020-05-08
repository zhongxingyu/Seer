 package org.sakaiproject.assignment2.tool.producers;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 import java.util.HashSet;
 
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.tool.beans.Assignment2Bean;
 import org.sakaiproject.assignment2.tool.beans.PreviewAssignmentBean;
 import org.sakaiproject.assignment2.tool.params.FilePickerHelperViewParams;
 import org.sakaiproject.assignment2.tool.params.SimpleAssignmentViewParams;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentAttachment;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.tool.api.ToolSession;
 
 import uk.org.ponder.beanutil.entity.EntityBeanLocator;
 import uk.org.ponder.messageutil.MessageLocator;
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
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
 
 public class StudentSubmitProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {
 	public static final String VIEW_ID = "student-submit";
 	public String getViewID() {
 		return this.VIEW_ID;
 	}
 	
     String reqStar = "<span class=\"reqStar\">*</span>";
 
     private TextInputEvolver richTextEvolver;
     private MessageLocator messageLocator;
     private AssignmentLogic assignmentLogic;
     private ExternalLogic externalLogic;
     private AssignmentSubmissionLogic submissionLogic;
     private ExternalGradebookLogic externalGradebookLogic;
     private PreviewAssignmentBean previewAssignmentBean;
     private Locale locale;
     private Assignment2Bean assignment2Bean;
     private SessionManager sessionManager;
     private EntityBeanLocator assignment2BeanLocator;
     private AttachmentListRenderer attachmentListRenderer;
     private EntityBeanLocator assignmentSubmissionBeanLocator;
 
     @SuppressWarnings("unchecked")
 	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
     	SimpleAssignmentViewParams params = (SimpleAssignmentViewParams) viewparams;
     		
     	//Clear out session attachment information if everything successful
     	ToolSession session = sessionManager.getCurrentToolSession();
     	session.removeAttribute("attachmentRefs");
     	session.removeAttribute("removedAttachmentRefs");
     	
     	//get Passed assignmentId to pull in for editing if any
     	Long assignmentId = params.assignmentId;
     	if (assignmentId == null){
     		//error 
     		return;
     	}
     	Assignment2 assignment = (Assignment2) assignment2BeanLocator.locateBean(assignmentId.toString());
     	if (assignment == null) {
     		//error 
     		return;
     	}
     	
     	AssignmentSubmission submission = submissionLogic.getCurrentSubmissionByAssignmentIdAndStudentIdForStudentView(assignmentId, externalLogic.getCurrentUserId());
     	String ASOTPKey = "";
     	if (submission == null || submission.getSubmissionId() == null) {
     		ASOTPKey += EntityBeanLocator.NEW_PREFIX + "1";
     	} else {
     		ASOTPKey += submission.getSubmissionId();
     	}
     	
     	//Now do submission stuff
     	String assignmentSubmissionOTP = "AssignmentSubmission.";		//Base for AssignmentSubmission object
     	String submissionVersionOTP = "currentSubmissionVersion";			//Base for the currentSubmissionVersion object
     	assignmentSubmissionOTP += ASOTPKey;							//Full path to current object
     	String versionOTP = assignmentSubmissionOTP + "." + submissionVersionOTP;			//Full path to current version object
     	AssignmentSubmission assignmentSubmission = (AssignmentSubmission) assignmentSubmissionBeanLocator.locateBean(ASOTPKey); 
     	AssignmentSubmissionVersion submissionVersion = assignmentSubmission.getCurrentSubmissionVersion();
     	
     	
     	// use a date which is related to the current users locale
         DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
 
         
     	UIMessage.make(tofill, "heading_status", "assignment2.student-submit.heading_status", 
     			new Object[]{ assignmentSubmission.getSubmissionStatus() });
     	UIVerbatim.make(tofill, "page_instructions", messageLocator.getMessage("assignment2.student-submit.instructions"));
     	
     	//Display Assignment Info
     	UIOutput.make(tofill, "header.title", assignment.getTitle());
     	if (assignment.isUngraded()){
     		UIOutput.make(tofill, "header.due_date", (assignment.getDueDate() != null ? df.format(assignment.getDueDate()) : ""));
     	} else {
     		UIOutput.make(tofill, "header.due_date", (assignment.getDueDateForUngraded() != null ? df.format(assignment.getDueDateForUngraded()) : ""));
     	}
     	UIOutput.make(tofill, "header.status", assignmentSubmission.getSubmissionStatus());
     	UIOutput.make(tofill, "header.grade_scale", "Grade Scale from Gradebook");  //HERE
     	if (assignment.getModifiedTime() != null) {
     		UIOutput.make(tofill, "modified_by_header_row");
     		UIOutput.make(tofill, "header.modified_by", df.format(assignment.getModifiedTime()));
     	}
     	UIVerbatim.make(tofill, "instructions", assignment.getInstructions());
     	Set<String> refSet = new HashSet();
         attachmentListRenderer.makeAttachmentFromAssignmentAttachmentSet(tofill, "attachment_list:", params.viewID, 
         	assignment.getAttachmentSet(), Boolean.FALSE);
 
     	
     	UIForm form = UIForm.make(tofill, "form");
    	UIOutput.make(form, "submission_instructions"); //Fill in with submission type specific instructions
     	UIVerbatim.make(form, "instructions", messageLocator.getMessage("assignment2.student-submit.instructions"));
     	
         //Rich Text Input
     	if (assignment.getSubmissionType() == AssignmentConstants.SUBMIT_INLINE_ONLY || 
     			assignment.getSubmissionType() == AssignmentConstants.SUBMIT_INLINE_AND_ATTACH){
     		
     		UIOutput.make(form, "submit_text");
 	        UIInput text = UIInput.make(form, "text:", versionOTP + ".submittedText");
 	        richTextEvolver.evolveTextInput(text);
     	}
         
     	//Attachment Stuff
     	if (assignment.getSubmissionType() == AssignmentConstants.SUBMIT_ATTACH_ONLY ||
     			assignment.getSubmissionType() == AssignmentConstants.SUBMIT_INLINE_AND_ATTACH){
     		UIOutput.make(form, "submit_attachments");
     		
 	    	//Initialize js otpkey
 	    	UIVerbatim.make(tofill, "attachment-ajax-init", "otpkey=\"" + org.sakaiproject.util.Web.escapeUrl(ASOTPKey) + "\";\n" +
 	    			"fragAttachPath=\"" + externalLogic.getAssignmentViewUrl(FragmentAttachmentsProducer.VIEW_ID) + "\";\n");
 	    	
 	    	//Init JS
 	        String frameId = org.sakaiproject.util.Web.escapeJavascript("Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId());
 	        UIVerbatim.make(tofill, "iframeId_init", "var iframeId = \"" + frameId + "\";");
 	        
 	        //Attachments
 	    	attachmentListRenderer.makeAttachmentFromAssignmentSubmissionAttachmentSet(tofill, "submission_attachment_list:", params.viewID, 
 	    			submissionVersion.getSubmissionAttachSet(), Boolean.TRUE);
 	        UIInternalLink.make(form, "add_submission_attachments", UIMessage.make("assignment2.student-submit.add_attachments"),
 	        		new FilePickerHelperViewParams(AddAttachmentHelperProducer.VIEWID, Boolean.TRUE, 
 	        				Boolean.TRUE, 500, 700, ASOTPKey));
     	}
         
         form.parameters.add( new UIELBinding("#{AssignmentSubmissionBean.assignmentId}", assignmentId));
         
         //Buttons
 	     UICommand.make(form, "submit_button", UIMessage.make("assignment2.student-submit.submit"), 
 	    		 "#{AssignmentSubmissionBean.processActionSubmit}");
 	     UICommand.make(form, "preview_button", UIMessage.make("assignment2.student-submit.preview"), 
 	    		 "#{AssignmentSubmissionBean.processActionPreview}");
 	     UICommand.make(form, "save_draft_button", UIMessage.make("assignment2.student-submit.save_draft"), 
 	    		 "#{AssignmentSubmissionBean.processActionSaveDraft}");
 	     UICommand.make(form, "cancel_button", UIMessage.make("assignment2.student-submit.cancel"), 
 	    		 "#{AssignmentSubmissionBean.processActionCancel}");
     	
     }
 	
 	public List reportNavigationCases() {
     	List<NavigationCase> nav= new ArrayList<NavigationCase>();
         nav.add(new NavigationCase("submit", new SimpleViewParameters(
             StudentAssignmentListProducer.VIEW_ID)));
         nav.add(new NavigationCase("preview", new SimpleViewParameters(
         	FragmentSubmissionPreviewProducer.VIEW_ID)));
         nav.add(new NavigationCase("save_draft", new SimpleViewParameters(
         	StudentAssignmentListProducer.VIEW_ID)));
         nav.add(new NavigationCase("cancel", new SimpleViewParameters(
         	StudentAssignmentListProducer.VIEW_ID)));
         return nav;
     }
 	
     public ViewParameters getViewParameters() {
         return new SimpleAssignmentViewParams();
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
 	
 	public void setAssignmentSubmissionBeanLocator(EntityBeanLocator entityBeanLocator) {
 		this.assignmentSubmissionBeanLocator = entityBeanLocator;
 	}
 	
 	public void setAttachmentListRenderer(AttachmentListRenderer attachmentListRenderer){
 		this.attachmentListRenderer = attachmentListRenderer;
 	}
 	
 	public void setAssignmentSubmissionEntityBeanLocator(EntityBeanLocator entityBeanLocator) {
 		this.assignmentSubmissionBeanLocator = entityBeanLocator;
 	}
 
 	public void setSubmissionLogic(AssignmentSubmissionLogic submissionLogic) {
 		this.submissionLogic = submissionLogic;
 	}
 
 }
