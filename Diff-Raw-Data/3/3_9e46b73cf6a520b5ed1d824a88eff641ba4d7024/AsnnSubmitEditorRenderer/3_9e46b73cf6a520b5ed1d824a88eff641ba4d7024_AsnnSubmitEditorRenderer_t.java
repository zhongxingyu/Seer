 package org.sakaiproject.assignment2.tool.producers.renderers;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.assignment2.tool.params.FilePickerHelperViewParams;
 import org.sakaiproject.assignment2.tool.producers.AddAttachmentHelperProducer;
 import org.sakaiproject.assignment2.tool.producers.evolvers.AttachmentInputEvolver;
 
 import uk.org.ponder.beanutil.entity.EntityBeanLocator;
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.rsf.components.UIBoundBoolean;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIELBinding;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInput;
 import uk.org.ponder.rsf.components.UIInputMany;
 import uk.org.ponder.rsf.components.UIInternalLink;
 import uk.org.ponder.rsf.components.UIJointContainer;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.components.decorators.DecoratorList;
 import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
 import uk.org.ponder.rsf.evolvers.TextInputEvolver;
 import uk.org.ponder.rsf.producers.BasicProducer;
 
 /**
  * Renders the area of the Student Submit pages where the student does the 
  * actual work of putting in the submission text and uploading any attachments
  * that are part of the submission.
  * 
  * This does have to detect the type of assignment: Non-electronic, text,
  * text and attachments, attachments only.  In the future this may be 
  * modularized to allow new pluggable assignment types.
  * 
  * In the non-electronic or non-submission assignment type, this will really
  * just be a check box that says you've completed it and a "Save and Return"
  * button.
  * 
  * @author sgithens
  *
  */
 public class AsnnSubmitEditorRenderer implements BasicProducer {
     
     // Dependency
     private MessageLocator messageLocator;
     public void setMessageLocator(MessageLocator messageLocator) {
         this.messageLocator = messageLocator;
     }
     
     // Dependency
     private TextInputEvolver richTextEvolver;
     public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
         this.richTextEvolver = richTextEvolver;
     }
     
     // Dependency
     private AttachmentInputEvolver attachmentInputEvolver;
     public void setAttachmentInputEvolver(AttachmentInputEvolver attachmentInputEvolver){
         this.attachmentInputEvolver = attachmentInputEvolver;
     }
 
     /**
      *  
      * @param parent
      * @param clientID
      * @param assignmentSubmission
      * @param preview
      * @param asvOTP
      */
     public void fillComponents(UIContainer parent, String clientID, AssignmentSubmission assignmentSubmission, boolean preview) {
         Assignment2 assignment = assignmentSubmission.getAssignment();
         
         UIJointContainer joint = new UIJointContainer(parent, clientID, "asnn2-submit-editor-widget:");
         
         String asvOTP = "AssignmentSubmissionVersion.";
         String asvOTPKey = "";
         if (assignmentSubmission != null && assignmentSubmission.getCurrentSubmissionVersion() != null 
                 && assignmentSubmission.getCurrentSubmissionVersion().isDraft() == Boolean.TRUE) {
             asvOTPKey += assignmentSubmission.getCurrentSubmissionVersion().getId();
         } else {
             asvOTPKey += EntityBeanLocator.NEW_PREFIX + "1";
         }
         asvOTP = asvOTP + asvOTPKey;
         
         //For preview, get a decorated list of disabled="disabled"
         Map<String, String> disabledAttr = new HashMap<String, String>();
         disabledAttr.put("disabled", "disabled");
         DecoratorList disabledDecoratorList = new DecoratorList(new UIFreeAttributeDecorator(disabledAttr));
         
         UIForm form = UIForm.make(joint, "form");
         //Fill in with submission type specific instructions
         UIOutput.make(form, "submission_instructions", messageLocator.getMessage("assignment2.student-submit.instructions." + assignment.getSubmissionType())); 
         
         if (assignment.isHonorPledge()) {
             UIVerbatim.make(form, "required", messageLocator.getMessage("assignment2.student-submit.required"));
         }
         
         //Rich Text Input
         if (assignment.getSubmissionType() == AssignmentConstants.SUBMIT_INLINE_ONLY || 
                 assignment.getSubmissionType() == AssignmentConstants.SUBMIT_INLINE_AND_ATTACH){
 
             UIOutput.make(form, "submit_text");
             UIInput text = UIInput.make(form, "text:", asvOTP + ".submittedText");
             if (!preview) {
                 text.mustapply = Boolean.TRUE;
                 richTextEvolver.evolveTextInput(text);
             } else {
                 //disable textarea
 
                 UIInput text_disabled = UIInput.make(form, "text_disabled",asvOTP + ".submittedText");
                 text_disabled.decorators = disabledDecoratorList;
             }
 
         }
 
         //Attachment Stuff
         if (assignment.getSubmissionType() == AssignmentConstants.SUBMIT_ATTACH_ONLY ||
                 assignment.getSubmissionType() == AssignmentConstants.SUBMIT_INLINE_AND_ATTACH){
             UIOutput.make(form, "submit_attachments");
 
 
             if (!preview) {
                 //Attachments
                 UIInputMany attachmentInput = UIInputMany.make(form, "attachment_list:", asvOTP + ".submittedAttachmentRefs", 
                         assignmentSubmission.getCurrentSubmissionVersion().getSubmittedAttachmentRefs());
                 attachmentInputEvolver.evolveAttachment(attachmentInput);
 
                 UIInternalLink.make(form, "add_submission_attachments", UIMessage.make("assignment2.student-submit.add_attachments"),
                         new FilePickerHelperViewParams(AddAttachmentHelperProducer.VIEWID, Boolean.TRUE, 
                                 Boolean.TRUE, 500, 700, asvOTPKey));
                 
                 UIOutput.make(form, "no_attachments_yet", messageLocator.getMessage("assignment2.student-submit.no_attachments"));
             }
         }
 
         // attachment only situations will not return any values in the OTP map; thus,
         // we won't enter the processing loop in processActionSubmit (and nothing will be saved)
         // this will force rsf to bind the otp mapping
         if (assignment.getSubmissionType() == AssignmentConstants.SUBMIT_ATTACH_ONLY) {
             UIInput.make(form, "submitted_text_for_attach_only", asvOTP + ".submittedText");
         }
 
         if (assignment.isHonorPledge()) {
             UIOutput.make(joint, "honor_pledge_fieldset");
             UIMessage.make(joint, "honor_pledge_label", "assignment2.student-submit.honor_pledge_text");
             UIBoundBoolean.make(form, "honor_pledge", "#{AssignmentSubmissionBean.honorPledge}");
         }
         
        //form.parameters.add( new UIELBinding("#{AssignmentSubmissionBean.ASOTPKey}", assignmentSubmission.getId()));
        form.parameters.add( new UIELBinding("#{AssignmentSubmissionBean.ASOTPKey}", asvOTPKey));
         form.parameters.add( new UIELBinding("#{AssignmentSubmissionBean.assignmentId}", assignment.getId()));
 
         //Buttons
         UICommand submit_button = UICommand.make(form, "submit_button", UIMessage.make("assignment2.student-submit.submit"), 
         "#{AssignmentSubmissionBean.processActionSubmit}");
         UICommand preview_button = UICommand.make(form, "preview_button", UIMessage.make("assignment2.student-submit.preview"), 
         "#{AssignmentSubmissionBean.processActionPreview}");
         UICommand save_button = UICommand.make(form, "save_draft_button", UIMessage.make("assignment2.student-submit.save_draft"), 
         "#{AssignmentSubmissionBean.processActionSaveDraft}");
         // ASNN-288
         //UICommand cancel_button = UICommand.make(form, "cancel_button", UIMessage.make("assignment2.student-submit.cancel"), 
         //"#{AssignmentSubmissionBean.processActionCancel}");
 
         if (preview) {
             submit_button.decorators = disabledDecoratorList;
             preview_button.decorators = disabledDecoratorList;
             save_button.decorators = disabledDecoratorList;
             //cancel_button.decorators = disabledDecoratorList;
         }
 
     }
 
     public void fillComponents(UIContainer parent, String clientID) {
 
     }
 
 }
