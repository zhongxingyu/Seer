 package edu.northwestern.bioinformatics.studycalendar.service.presenter;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
 import edu.northwestern.bioinformatics.studycalendar.domain.Period;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
 import edu.northwestern.bioinformatics.studycalendar.tools.spring.WebContextPathAware;
 
 /**
  * @author Rhett Sutphin
  */
 public class WorkflowMessageFactory implements WebContextPathAware {
     private String webContextPath;
 
     public WorkflowMessage createMessage(WorkflowStep step, UserTemplateRelationship template) {
         return new WorkflowMessage(step, webContextPath, mayPerform(step, template)).
             setUriVariable("study-id", template.getStudy().getId().toString()).
             setMessageVariable("template-or-amendment",
                 template.getStudy().isReleased() ? "amendment" : "initial template");
     }
 
     public WorkflowMessage createMessage(WorkflowStep step, UserTemplateRelationship template, StudySegment studySegment) {
         return createMessage(step, template).
             setUriVariable("segment-id", studySegment.getId().toString()).
             setMessageVariable("segment-name", studySegment.getQualifiedName()).
             setMessageVariable("epoch-name", studySegment.getEpoch().getName());
     }
 
     public WorkflowMessage createMessage(WorkflowStep step, UserTemplateRelationship template, Period period) {
         return createMessage(step, template, period.getParent()).
             setUriVariable("period-id", period.getId().toString()).
             setMessageVariable("period-name", period.getDisplayName());
     }
 
     public WorkflowMessage createMessage(WorkflowStep step, UserStudySiteRelationship participation) {
         return new WorkflowMessage(step, webContextPath, mayPerform(step, participation)).
             setUriVariable("study-site-id", participation.getStudySite().getId().toString()).
             setMessageVariable("site-name", participation.getStudySite().getSite().getName())
             ;
     }
 
     ////// HELPERS
 
     private boolean mayPerform(WorkflowStep step, UserTemplateRelationship template) {
         switch (step.getScope()) {
             case REVISION:
                 if (step == WorkflowStep.RELEASE_REVISION) {
                     return template.getCanRelease();
                 } else {
                     return template.getCanDevelop();
                 }
             case STUDY:
                 switch (step.getNecessaryRole()) {
                     case STUDY_CALENDAR_TEMPLATE_BUILDER: return template.getCanDevelop();
                     case STUDY_QA_MANAGER: return template.getCanRelease();
                     case STUDY_SITE_PARTICIPATION_ADMINISTRATOR: return template.getCanSetParticipation();
                     default: throw new StudyCalendarError("Unimplemented role for study scope: %s", step.getNecessaryRole());
                 }
             default: throw new StudyCalendarError("Unimplemented scope: %s", step.getScope());
         }
     }
 
     private boolean mayPerform(WorkflowStep step, UserStudySiteRelationship participation) {
         if (step.getScope() != WorkflowStep.Scope.STUDY_SITE) {
             throw new StudyCalendarError("Unimplemented scope: %s", step.getScope());
         }
         switch (step.getNecessaryRole()) {
             case STUDY_QA_MANAGER: return participation.getCanApproveAmendments();
             case USER_ADMINISTRATOR: return participation.getCanAdministerUsers();
             case STUDY_TEAM_ADMINISTRATOR: return participation.getCanAdministerTeam();
             default: throw new StudyCalendarError("Unimplemented role: %s", step.getNecessaryRole());
         }
     }
 
     ////// CONFIGURATION
 
     public void setWebContextPath(String contextPath) {
         this.webContextPath = contextPath;
     }
 }
