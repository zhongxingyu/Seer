 package org.sakaiproject.assignment2.tool.beans;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.SubmissionAttachment;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.model.FeedbackAttachment;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.exception.TypeException;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.tool.api.ToolSession;
 import org.sakaiproject.user.api.UserNotDefinedException;
 
 import uk.org.ponder.beanutil.entity.EntityBeanLocator;
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.messageutil.TargettedMessage;
 import uk.org.ponder.messageutil.TargettedMessageList;
 
 public class AssignmentSubmissionBean {
 	
 	private static final String SUBMIT = "submit";
 	private static final String PREVIEW = "preview";
 	private static final String SAVE_DRAFT = "save_draft";
 	private static final String EDIT = "edit";
 	private static final String CANCEL = "cancel";
 	private static final String FAILURE = "failure";
 	private static final String RELEASE_ALL= "release_all";
 	
 	public Map<String, Boolean> selectedIds = new HashMap<String, Boolean>();
 	public Long assignmentId;
 	public String ASOTPKey;
 	public String userId;
 	public Boolean releaseFeedback;
 	public Boolean resubmitUntil;
 	
     private TargettedMessageList messages;
     public void setMessages(TargettedMessageList messages) {
     	this.messages = messages;
     }
 	
 	private AssignmentLogic assignmentLogic;
 	public void setAssignmentLogic(AssignmentLogic assignmentLogic) {
 		this.assignmentLogic = assignmentLogic;
 	}
 	
 	private AssignmentSubmissionLogic submissionLogic;
 	public void setSubmissionLogic(AssignmentSubmissionLogic submissionLogic) {
 		this.submissionLogic = submissionLogic;
 	}
 	
 	
 	private EntityBeanLocator assignment2EntityBeanLocator;
 	public void setAssignment2EntityBeanLocator(EntityBeanLocator entityBeanLocator) {
 		this.assignment2EntityBeanLocator = entityBeanLocator;
 	}
 	
 	private Map<String, AssignmentSubmission> OTPMap;
 	private EntityBeanLocator asEntityBeanLocator;
 	@SuppressWarnings("unchecked")
 	public void setAssignmentSubmissionEntityBeanLocator(EntityBeanLocator entityBeanLocator) {
 		this.OTPMap = entityBeanLocator.getDeliveredBeans();
 		this.asEntityBeanLocator = entityBeanLocator;
 	}
 	
 	private Map<String, AssignmentSubmissionVersion> asvOTPMap;
 	private EntityBeanLocator asvEntityBeanLocator;
 	public void setAsvEntityBeanLocator(EntityBeanLocator entityBeanLocator) {
 		this.asvOTPMap = entityBeanLocator.getDeliveredBeans();
 		this.asvEntityBeanLocator = entityBeanLocator;
 	}
 		
 	private ExternalLogic externalLogic;
 	public void setExternalLogic(ExternalLogic externalLogic) {
 		this.externalLogic = externalLogic;
 	}
 	
 	private PreviewAssignmentSubmissionBean previewAssignmentSubmissionBean;
 	public void setPreviewAssignmentSubmissionBean (PreviewAssignmentSubmissionBean previewAssignmentSubmissionBean) {
 		this.previewAssignmentSubmissionBean = previewAssignmentSubmissionBean;
 	}
 	
 	private MessageLocator messageLocator;
 	public void setMessageLocator (MessageLocator messageLocator) {
 		this.messageLocator = messageLocator;
 	}
 	
 	private SessionManager sessionManager;
 	public void setSessionManager(SessionManager sessionManager) {
 		this.sessionManager = sessionManager;
 	}
 	
 	private Boolean honorPledge;
 	public void setHonorPledge(Boolean honorPledge) {
 		this.honorPledge = honorPledge;
 	}
 	
 	private NotificationBean notificationBean;
 	public void setNotificationBean(NotificationBean notificationBean) {
 		this.notificationBean = notificationBean;
 	}
 	
 	/*
 	 * STUDENT FUNCTIONS
 	 */
 	public String processActionSubmit(){
 		if (assignmentId == null ) {
 			return FAILURE;
 		}
 		
 		AssignmentSubmission assignmentSubmission = (AssignmentSubmission) asEntityBeanLocator.locateBean(ASOTPKey);
 		Assignment2 assignment = assignmentLogic.getAssignmentById(assignmentId);
 		assignmentSubmission.setAssignment(assignment);
 		
 		for (String key : asvOTPMap.keySet()) {
 			AssignmentSubmissionVersion asv = asvOTPMap.get(key);
 			
 			asv.setAssignmentSubmission(assignmentSubmission);
 			asv.setDraft(Boolean.FALSE);
 			
 			//Start attachment stuff
 			Set<SubmissionAttachment> set = new HashSet<SubmissionAttachment>();
 			if (asv.getSubmissionAttachSet() != null) {
 				set.addAll(asv.getSubmissionAttachSet());
 			}
 			
 	    	//get New attachments from session set
 	    	ToolSession session = sessionManager.getCurrentToolSession();
 	    	if (session.getAttribute("attachmentRefs") != null) {
 	    		for (String ref : (Set<String>)session.getAttribute("attachmentRefs")) {
 	    			SubmissionAttachment asa = new SubmissionAttachment();
 	    			asa.setAttachmentReference(ref);
 	    			set.add(asa);
 	    		}
 	    	}
 	    	Set<SubmissionAttachment> final_set = new HashSet<SubmissionAttachment>();
 	    	//Now check for attachments that have been removed
 	    	if (session.getAttribute("removedAttachmentRefs") != null) {
 		    	for (SubmissionAttachment asa : set) {
 		    		//If this item in the set does not have a reference id that is 
 		    		// located in the removed attachment reference ids set
 		    		if (!((Set<String>) session.getAttribute("removedAttachmentRefs")).contains(asa.getAttachmentReference())){
 		    			final_set.add(asa);
 		    		}
 		    	}
 	    	} else {
 	    		final_set.addAll(set);
 	    	}
 	    	asv.setSubmissionAttachSet(final_set);
 			//End Attachment stuff
 			
 	    	//check whether honor pledge was added if required
	    	if (assignment.isHonorPledge() && !(this.honorPledge != null && this.honorPledge == Boolean.TRUE)) {
 	    		messages.addMessage(new TargettedMessage("assignment2.student-submit.error.honor_pledge_required",
 						new Object[] { assignment.getTitle() }, TargettedMessage.SEVERITY_ERROR));
 	    		return FAILURE;
 	    	}else {
 	    		submissionLogic.saveStudentSubmission(assignmentSubmission.getUserId(), 
 	    				assignmentSubmission.getAssignment(), false, asv.getSubmittedText(), final_set);
 	    		messages.addMessage(new TargettedMessage("assignment2.student-submit.info.submission_submitted",
 						new Object[] { assignment.getTitle() }, TargettedMessage.SEVERITY_INFO));
 	    		// Send out notifications
 	    		try {
 	    			notificationBean.notifyStudentThatSubmissionWasAccepted(assignmentSubmission);
 	    			if (assignment.getNotificationType() ==  AssignmentConstants.NOTIFY_FOR_EACH)
 	    			{
 	    				notificationBean.notifyInstructorsOfSubmission(assignmentSubmission, assignment);
 	    			}
 	    		}catch (IdUnusedException e)
 	    		{
 	    			messages.addMessage(new TargettedMessage("assignment2.student-submit.error.unexpected",
 	    					new Object[]{e.getLocalizedMessage()}, TargettedMessage.SEVERITY_ERROR));
 	    		}
 	    		catch (UserNotDefinedException e)
 	    		{
 	    			messages.addMessage(new TargettedMessage("assignment2.student-submit.error.unexpected",
 	    					new Object[]{e.getLocalizedMessage()}, TargettedMessage.SEVERITY_ERROR));
 	    		}
 	    		catch (PermissionException e)
 	    		{
 	    			messages.addMessage(new TargettedMessage("assignment2.student-submit.error.unexpected",
 	    					new Object[]{e.getLocalizedMessage()}, TargettedMessage.SEVERITY_ERROR));
 	    		}
 	    		catch (TypeException e)
 	    		{
 	    			messages.addMessage(new TargettedMessage("assignment2.student-submit.error.unexpected",
 	    					new Object[]{e.getLocalizedMessage()}, TargettedMessage.SEVERITY_ERROR));
 	    		}
 	    	}
 		}
 
 		return SUBMIT;
 	}
 	
 	public String processActionPreview(){
 		AssignmentSubmission assignmentSubmission = (AssignmentSubmission) asEntityBeanLocator.locateBean(ASOTPKey);
 		previewAssignmentSubmissionBean.setAssignmentSubmission(assignmentSubmission);
 		for (String key : asvOTPMap.keySet()) {
 			AssignmentSubmissionVersion asv = asvOTPMap.get(key);
 			previewAssignmentSubmissionBean.setAssignmentSubmissionVersion(asv);
 		}
 		return PREVIEW;
 	}
 	
 	public String processActionSaveDraft() {
 		Assignment2 assignment = assignmentLogic.getAssignmentById(assignmentId);
 		AssignmentSubmission assignmentSubmission = (AssignmentSubmission) asEntityBeanLocator.locateBean(ASOTPKey);
 		if (assignmentId == null){
 			return FAILURE;
 		}
 		assignmentSubmission.setAssignment(assignment);
 		for (String key : asvOTPMap.keySet()) {
 			AssignmentSubmissionVersion asv = (AssignmentSubmissionVersion) asvOTPMap.get(key);
 			
 			asv.setAssignmentSubmission(assignmentSubmission);
 			asv.setDraft(Boolean.TRUE);
 
 			//Start attachment stuff
 			Set<SubmissionAttachment> set = new HashSet<SubmissionAttachment>();
 			if (asv.getSubmissionAttachSet() != null) {
 				set.addAll(asv.getSubmissionAttachSet());
 			}
 			
 			//get New attachments from session set
 	    	ToolSession session = sessionManager.getCurrentToolSession();
 	    	if (session.getAttribute("attachmentRefs") != null) {
 	    		for (String ref : (Set<String>)session.getAttribute("attachmentRefs")) {
 	    			SubmissionAttachment asa = new SubmissionAttachment();
 	    			asa.setAttachmentReference(ref);
 	    			set.add(asa);
 	    		}
 	    	}
 	    	Set<SubmissionAttachment> final_set = new HashSet<SubmissionAttachment>();
 	    	//Now check for attachments that have been removed
 	    	if (session.getAttribute("removedAttachmentRefs") != null) {
 		    	for (SubmissionAttachment asa : set) {
 		    		//If this item in the set does not have a reference id that is 
 		    		// located in the removed attachment reference ids set
 		    		if (!((Set<String>) session.getAttribute("removedAttachmentRefs")).contains(asa.getAttachmentReference())){
 		    			final_set.add(asa);
 		    		}
 		    	}
 	    	} else {
 	    		final_set.addAll(set);
 	    	}
 	    	asv.setSubmissionAttachSet(final_set);
 			//End Attachment stuff
 			
 			submissionLogic.saveStudentSubmission(assignmentSubmission.getUserId(),
 					assignmentSubmission.getAssignment(), true, asv.getSubmittedText(),
 					final_set);
 			messages.addMessage(new TargettedMessage("assignment2.student-submit.info.submission_save_draft",
 					new Object[] { assignment.getTitle() }, TargettedMessage.SEVERITY_INFO));
 		}
 		return SAVE_DRAFT;
 	}
 	
 	/*
 	 * INSTRUCTOR FUNCTIONS
 	 */
 	public String processActionReleaseAllFeedbackForAssignment() {
 		if (this.assignmentId != null) {
 			submissionLogic.releaseAllFeedbackForAssignment(assignmentId);
 		}
 		
 		return RELEASE_ALL;
 	}
 	
 	public String processActionSaveAndReleaseAllFeedbackForSubmission(){
 		processActionGradeSubmit();
 		
 		for (String key : OTPMap.keySet()) {
 			AssignmentSubmission as = OTPMap.get(key);
 			Long subId = as.getId();
 			if (subId == null) {
 				// we need to retrieve the newly created submission
 				AssignmentSubmission sub = submissionLogic.getCurrentSubmissionByAssignmentIdAndStudentId(
 						as.getAssignment().getId(), as.getUserId());
 				subId = sub.getId();
 			}
 			
 			submissionLogic.releaseAllFeedbackForSubmission(subId);
 		}
 		
 		return SUBMIT;
 	}
 	
 	public String processActionGradeSubmit(){
 		if (assignmentId == null || userId == null){
 			return FAILURE;
 		}
 		Assignment2 assignment = assignmentLogic.getAssignmentById(assignmentId);
 		AssignmentSubmission assignmentSubmission = new AssignmentSubmission();
 		
 		for (String key : OTPMap.keySet()){
 			assignmentSubmission = OTPMap.get(key);
 			assignmentSubmission.setAssignment(assignment);
 			assignmentSubmission.setUserId(userId);
 			
 			if (this.resubmitUntil == null || this.resubmitUntil == Boolean.FALSE) {
 				assignmentSubmission.setResubmitCloseTime(null);
 			}
 		}
 		for (String key : asvOTPMap.keySet()){
 			
 			AssignmentSubmissionVersion asv = asvOTPMap.get(key);
 			
 			asv.setAssignmentSubmission(assignmentSubmission);
 			if (this.releaseFeedback != null && asv.getReleasedTime() == null) {
 				asv.setReleasedTime(new Date());
 			}
 			
 			//Start attachment stuff
 			Set<FeedbackAttachment> set = new HashSet<FeedbackAttachment>();
 			if (assignmentSubmission.getCurrentSubmissionVersion() != null && 
 					assignmentSubmission.getCurrentSubmissionVersion().getFeedbackAttachSet() != null) {
 				set.addAll(assignmentSubmission.getCurrentSubmissionVersion().getFeedbackAttachSet());
 			}
 			
 			//get New attachments from session set
 	    	ToolSession session = sessionManager.getCurrentToolSession();
 	    	if (session.getAttribute("attachmentRefs") != null) {
 	    		for (String ref : (Set<String>)session.getAttribute("attachmentRefs")) {
 	    			FeedbackAttachment afa = new FeedbackAttachment();
 	    			afa.setAttachmentReference(ref);
 	    			set.add(afa);
 	    		}
 	    	}
 	    	Set<FeedbackAttachment> final_set = new HashSet<FeedbackAttachment>();
 	    	//Now check for attachments that have been removed
 	    	if (session.getAttribute("removedAttachmentRefs") != null) {
 		    	for (FeedbackAttachment afa : set) {
 		    		//If this item in the set does not have a reference id that is 
 		    		// located in the removed attachment reference ids set
 		    		if (!((Set<String>) session.getAttribute("removedAttachmentRefs")).contains(afa.getAttachmentReference())){
 		    			final_set.add(afa);
 		    		}
 		    	}
 	    	} else {
 	    		final_set.addAll(set);
 	    	}
 	    	asv.setFeedbackAttachSet(final_set);
 			//End Attachment stuff			
 			
 			submissionLogic.saveInstructorFeedback(asv.getId(), assignmentSubmission.getUserId(),
 					assignmentSubmission.getAssignment(), assignmentSubmission.getNumSubmissionsAllowed(),
 					assignmentSubmission.getResubmitCloseTime(), asv.getAnnotatedText(), asv.getFeedbackNotes(),
 					asv.getReleasedTime(), final_set);
 		}
 		return SUBMIT;
 	}
 	
 	public String processActionGradePreview(){
 		for (String key : OTPMap.keySet()){
 			AssignmentSubmission assignmentSubmission = OTPMap.get(key);
 			Assignment2 assignment = assignmentLogic.getAssignmentByIdWithAssociatedData(assignmentId);
 			assignmentSubmission.setAssignment(assignment);
 			previewAssignmentSubmissionBean.setAssignmentSubmission(assignmentSubmission);
 		}
 		for (String key : asvOTPMap.keySet()){
 			AssignmentSubmissionVersion asv = asvOTPMap.get(key);
 			previewAssignmentSubmissionBean.setAssignmentSubmissionVersion(asv);
 		}
 		return PREVIEW;
 	}
 	
 	public String processActionCancel() {
 		return CANCEL;
 	}
 	
 	public void populateNonPersistedFieldsForSubmissions(List<AssignmentSubmission> submissionList) {
 		if (submissionList == null || submissionList.isEmpty())
 			return;
 		
 		// Now, iterate through the viewable assignments and set the not persisted fields 
 		// that aren't related to the gradebook
 		
 		for (AssignmentSubmission submission : submissionList) {
 			if (submission != null) { 
 				// set the status for this submission: "In Progress, Submitted, etc"
 				if (submission.getSubmissionStatusConstant() != null) {
 					submission.setSubmissionStatus(messageLocator.getMessage(
 							"assignment2.assignment_grade-assignment.submission_status." + 
 							submission.getSubmissionStatusConstant()));
 				}
 			}
 		}
 	}
 	
 	public List filterListForPaging(List myList, int begIndex, int numItemsToDisplay) {
         if (myList == null || myList.isEmpty())
         	return myList;
         
         int endIndex = begIndex + numItemsToDisplay;
         if (endIndex > myList.size()) {
         	endIndex = myList.size();
         }
 
 		return myList.subList(begIndex, endIndex);
 	}
 	
 	/**
 	 * Will apply paging and sorting to the given list and populate any non-persisted
 	 * fields that need to be populated from the UI (ie fields that require access
 	 * to the bundle)
 	 * @param submissionList
 	 * @param currentStart
 	 * @param currentCount
 	 * @param sortBy
 	 * @param sortDir
 	 */
 	public void filterPopulateAndSortSubmissionList(List<AssignmentSubmission> submissionList, int currentStart, int currentCount, String sortBy, boolean sortDir) {
 		submissionList = filterListForPaging(submissionList, currentStart, currentCount);
         populateNonPersistedFieldsForSubmissions(submissionList);
         submissionLogic.sortSubmissions(submissionList, sortBy, sortDir);
 	}
 }
