 package org.sakaiproject.assignment2.tool.producers.renderers;
 
 import java.util.Date;
 import java.util.Map;
 
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIJointContainer;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
 import uk.org.ponder.rsf.producers.BasicProducer;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 
 /**
  * Renders the read only view of the Assignment Instructions. This currently
  * involves the Text and Attachments. It does not render the title of the 
  * assignment currently, as it is tooled to be used inside pages and displays.
  * 
  * 
  * @author sgithens
  *
  */
 public class AsnnInstructionsRenderer implements BasicProducer {
 
     // Dependency
     private ViewParameters viewParameters;
     public void setViewParameters(ViewParameters viewParameters) {
         this.viewParameters = viewParameters;
     }
 
     // Dependency
     private AttachmentListRenderer attachmentListRenderer;
     public void setAttachmentListRenderer (AttachmentListRenderer attachmentListRenderer) {
         this.attachmentListRenderer = attachmentListRenderer;
     }
     
     private AsnnToggleRenderer toggleRenderer;
     public void setAsnnToggleRenderer(AsnnToggleRenderer toggleRenderer) {
         this.toggleRenderer = toggleRenderer;
     }
     
     /**
      * 
      * @param tofill
      * @param divID
      * @param assignment
      * @param includeToggle true if the instructions should be toggleable
      * @param includeToggleBar true if the toggleable instructions should use a toggle bar
      * and not just the toggle arrow. if includeToggle is false, will ignore this property
      * @param toggleExpanded true if the toggle should be expanded. if includeToggle is false, will 
      * ignore this property
      * @param optionalParams optional extra information that might be useful for rendering the assignment info.
      * ie, you may need extended privileges for viewing the attachments so you could pass that info here
      */
     public void makeInstructions(UIContainer tofill, String divID, Assignment2 assignment, boolean includeToggle, boolean includeToggleBar, boolean toggleExpanded, Map<String, Object> optionalParams){
 
         UIJointContainer joint = new UIJointContainer(tofill, divID, "assn2-assignment-instructions-widget:");
         
         // model answer stuff
         boolean modelAnswerEnabled = false;
         if (assignment.isModelAnswerEnabled())
         {
             boolean isInstructor = false;
             boolean isPreview = false;
             
             if (optionalParams != null)
             {
                 if (optionalParams.containsKey(AssignmentConstants.MODEL_ANSWER_IS_PREVIEW))
                 {
                     isPreview = optionalParams.get(AssignmentConstants.MODEL_ANSWER_IS_PREVIEW).equals(true);
                 }
                 
                 if (optionalParams.containsKey(AssignmentConstants.MODEL_ANSWER_IS_INSTRUCTOR))
                 {
                     isInstructor = optionalParams.get(AssignmentConstants.MODEL_ANSWER_IS_INSTRUCTOR).equals(true);
                 }
             }
             
             if (!isPreview && isInstructor)
             {
                 // if this is an instructor and not a preview screen, we don't care about the display rule
                 modelAnswerEnabled = true;
             }
             else
             {
                 int madr = assignment.getModelAnswerDisplayRule();
                 if (madr==AssignmentConstants.MODEL_NEVER)
                 {
                     modelAnswerEnabled = false;
                 }
                 else if (madr==AssignmentConstants.MODEL_IMMEDIATELY)
                 {
                     modelAnswerEnabled = true;
                 }
                 else if (madr==AssignmentConstants.MODEL_AFTER_STUDENT_SUBMITS)
                 {
                     if (optionalParams!=null && optionalParams.containsKey(AssignmentConstants.ASSIGNMENT_SUBMISSION))
                     {
                         AssignmentSubmission assignmentSubmission = (AssignmentSubmission) optionalParams.get(AssignmentConstants.ASSIGNMENT_SUBMISSION);
                        if (assignmentSubmission.getCurrentSubmissionVersion().isSubmitted())
                         {
                             modelAnswerEnabled = true;
                         }
                         else
                         {
                             modelAnswerEnabled = false;
                         }
                     }
                     else
                     {
                         modelAnswerEnabled = false;
                     }
                 }
                 else if (madr==AssignmentConstants.MODEL_AFTER_FEEDBACK_RELEASED)
                 {
                     if (optionalParams!=null && optionalParams.containsKey(AssignmentConstants.ASSIGNMENT_SUBMISSION))
                     {
                         AssignmentSubmission assignmentSubmission = (AssignmentSubmission) optionalParams.get(AssignmentConstants.ASSIGNMENT_SUBMISSION);
                         if (assignmentSubmission.getCurrentSubmissionVersion().isFeedbackReleased())
                         {
                             modelAnswerEnabled = true;
                         }
                         else
                         {
                             modelAnswerEnabled = false;
                         }
                     }
                     else
                     {
                         modelAnswerEnabled = false;
                     }
                 }
                 else if (madr==AssignmentConstants.MODEL_AFTER_DUE_DATE)
                 {
                     if (assignment.getDueDate().before(new Date()))
                     {
                         modelAnswerEnabled = true;
                     }
                     else
                     {
                         modelAnswerEnabled = false;
                     }
                 }
                 else if (madr==AssignmentConstants.MODEL_AFTER_ACCEPT_DATE)
                 {
                     if (assignment.getAcceptUntilDate().before(new Date()))
                     {
                         modelAnswerEnabled = true;
                     }
                     else
                     {
                         modelAnswerEnabled = false;
                     }
                 }
             }
         }
 
         if (includeToggle) {
             String hoverText = messageLocator.getMessage("assignment2.instructions.toggle.hover");
             String heading = "";
             if (modelAnswerEnabled)
             {
                 heading = messageLocator.getMessage("assignment2.instructions.maheading");
             }
             else
             {
                 heading = messageLocator.getMessage("assignment2.instructions.heading");
             }
             
             toggleRenderer.makeToggle(joint, "instructions_toggle_section:", null, includeToggleBar, 
                     heading, hoverText, false, false, false, false, null);
             
             UIMessage.make(joint, "toggle_instructions_heading", "assignment2.instructions.heading");
         } else {
             UIMessage.make(joint, "instructions_heading", "assignment2.instructions.heading");
         }
         
         UIOutput instructionsSection = UIOutput.make(joint, "instructionsSection");
         if (includeToggle) {
             // everything below the toggle is a subsection
             instructionsSection.decorate(new UIFreeAttributeDecorator("class", "toggleSubsection subsection1"));
             // should we hide or show the instructions section?
             if(!toggleExpanded) {
                 instructionsSection.decorate(new UIFreeAttributeDecorator("style", "display: none;"));
             }
             
             // display a different heading for the attachments
             UIMessage.make(joint, "toggle_attach_heading", "assignment2.instructions.attachments");
             if (modelAnswerEnabled)
             {
                 UIMessage.make(joint, "toggle_model_answer_attach_heading", "assignment2.instructions.model_answer.heading");
             }
         } else {
             UIMessage.make(joint, "attach_heading", "assignment2.instructions.attachments");
             if (modelAnswerEnabled)
             {
                 UIMessage.make(joint, "model_answer_heading", "assignment2.instructions.model_answer.heading");
             }
         }
         
         if (modelAnswerEnabled)
         {
             UIVerbatim.make(joint, "modelAnswerText", assignment.getModelAnswerText());
             if (assignment.getModelAnswerAttachmentSet() != null && !assignment.getModelAnswerAttachmentSet().isEmpty()) {
                 UIOutput.make(joint, "modelAnswerAttachmentsFieldset");
                 attachmentListRenderer.makeAttachmentFromModelAssignmentAttachmentSet(tofill, "model_answer_assign_attach_list:", viewParameters.viewID, 
                         assignment.getModelAnswerAttachmentSet(), optionalParams);
             }
         }
         
         // Instructions
         if (assignment.getInstructions() == null || assignment.getInstructions().equals("")) {
             UIMessage.make(joint, "instructions", "assignment2.instructions.none");
         }
         else {
             UIVerbatim.make(joint, "instructions", assignment.getInstructions());
         }
         
         if (assignment.getAttachmentSet() != null && !assignment.getAttachmentSet().isEmpty()) {
             UIOutput.make(joint, "assignAttachmentsFieldset");
             attachmentListRenderer.makeAttachmentFromAssignmentAttachmentSet(tofill, "assign_attach_list:", viewParameters.viewID, 
                     assignment.getAttachmentSet(), optionalParams);
         }
     }
     
     /**
      * 
      * @param tofill
      * @param divID
      * @param assignment
      * @param includeToggle true if the instructions should be toggleable
      * @param includeToggleBar true if the toggleable instructions should use a toggle bar
      * and not just the toggle arrow. if includeToggle is false, will ignore this property
      * @param toggleExpanded true if the toggle should be expanded. if includeToggle is false, will 
      * ignore this property
      */
     public void makeInstructions(UIContainer tofill, String divID, Assignment2 assignment, boolean includeToggle, boolean includeToggleBar, boolean toggleExpanded){
         makeInstructions(tofill, divID, assignment, includeToggle, includeToggleBar, toggleExpanded, null);
     }
     
     public void fillComponents(UIContainer parent, String clientID) {
 
     }
     
     private MessageLocator messageLocator;
     public void setMessageLocator(MessageLocator messageLocator) {
         this.messageLocator = messageLocator;
     }
 
 }
