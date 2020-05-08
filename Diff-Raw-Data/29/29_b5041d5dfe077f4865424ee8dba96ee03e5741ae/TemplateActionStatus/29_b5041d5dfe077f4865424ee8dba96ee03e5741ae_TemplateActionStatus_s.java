 package edu.northwestern.bioinformatics.studycalendar.service.presenter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class TemplateActionStatus {
     private StudyWorkflowStatus studyWorkflowStatus;
     private boolean developmentRevision;
 
     public TemplateActionStatus(StudyWorkflowStatus studyWorkflowStatus, boolean developmentRevision) {
         this.studyWorkflowStatus = studyWorkflowStatus;
         this.developmentRevision = developmentRevision;
     }
 
     public List<TemplateAction> getActions() {
         List<TemplateAction> actions = new ArrayList<TemplateAction>();
         
         if (developmentRevision) {
             WorkflowMessage studyMessage = studyWorkflowStatus.getMessagesIgnoringRevisionMessages();
             if ((studyMessage == null || !studyMessage.getStep().equals(WorkflowStep.SET_ASSIGNED_IDENTIFIER)) && studyWorkflowStatus.isRevisionComplete()) {
                actions.add(TemplateAction.PREVIEW_SCHEDULE);
                 actions.add(TemplateAction.RELEASE_REVISION);
             }
        } else {
            actions.add(TemplateAction.PREVIEW_SCHEDULE);
         }
 
         return actions;
     }
 
 
 }
