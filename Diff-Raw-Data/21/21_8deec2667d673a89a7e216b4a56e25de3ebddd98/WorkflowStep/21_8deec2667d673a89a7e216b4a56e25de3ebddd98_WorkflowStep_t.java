 package edu.northwestern.bioinformatics.studycalendar.service.presenter;
 
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
 import org.restlet.util.Template;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
 import static edu.northwestern.bioinformatics.studycalendar.service.presenter.WorkflowStep.Scope.*;
 
 /**
  * @author Rhett Sutphin
  */
 public enum WorkflowStep {
     SET_ASSIGNED_IDENTIFIER(
        STUDY, STUDY_CREATOR,
         "needs the assigned identifier set.", null, null),
     ADD_AT_LEAST_ONE_EPOCH(
         REVISION, STUDY_CALENDAR_TEMPLATE_BUILDER,
         "Please add at least one epoch.", null, null),
     UNNAMED_EPOCH(
         REVISION, STUDY_CALENDAR_TEMPLATE_BUILDER,
         "Please name all epochs.", null, null),
     UNNAMED_STUDY_SEGMENT(
         REVISION, STUDY_CALENDAR_TEMPLATE_BUILDER,
         "Please name all the study segments in epoch {epoch-name}.", null, null),
     STUDY_SEGMENT_NO_PERIODS(
         REVISION, STUDY_CALENDAR_TEMPLATE_BUILDER,
         "Study segment {segment-name} does not have any periods.  {action}", "Add one.",
         "/pages/cal/newPeriod?studySegment={segment-id}"),
     PERIOD_NO_PLANNED_ACTIVITIES(
         REVISION, STUDY_CALENDAR_TEMPLATE_BUILDER,
         "Period {period-name} in {segment-name} does not have any planned activities.  {action}", "Add some.",
         "/pages/cal/managePeriodActivities?period={period-id}"),
     RELEASE_REVISION(
         REVISION, STUDY_QA_MANAGER,
         "When the {template-or-amendment} is complete, it will need to be {action}.", "released",
         "/pages/cal/template/release?study={study-id}"),
     COMPLETE_AND_RELEASE_INITIAL_TEMPLATE( // TODO: the description for this one isn't very helpful.
         STUDY, STUDY_QA_MANAGER,
         "needs at least one revision completed and released.", null, null),
     ASSIGN_SITE(
         STUDY, STUDY_SITE_PARTICIPATION_ADMINISTRATOR,
         "needs at least one site {action} for participation.", "assigned",
         "/pages/cal/assignSite?id={study-id}"),
     APPROVE_AMENDMENT(
         STUDY_SITE, STUDY_QA_MANAGER,
         "needs to be {action}.", "approved",
         "/pages/cal/template/approve?studySite={study-site-id}"),
     /* TODO: The following two steps aren't actually implemented due to #1107. */
     ADD_SSCM_FOR_SITE(
         STUDY_SITE, USER_ADMINISTRATOR,
         "Please {action} at least one Study Subject Calendar Manager for {site-name}.", "add",
         "/pages/admin/manage/listUsers"),
     ADD_SSCM_FOR_STUDY(
         STUDY_SITE, STUDY_TEAM_ADMINISTRATOR,
         "Please {action} at least one Study Subject Calendar Manager.", "add",
         "/pages/cal/team/manage?studySite={study-site-id}")
     ;
 
     private final Scope scope;
     private final PscRole necessaryRole;
     private final Template messageTemplate;
     private final String actionPhrase;
     private final Template uriTemplate;
 
     WorkflowStep(Scope scope, PscRole necessaryRole, String message, String actionPhrase, String uri) {
         this.scope = scope;
         this.necessaryRole = necessaryRole;
         this.messageTemplate = new Template(message);
         this.uriTemplate = uri == null ? null : new Template(uri);
         this.actionPhrase = actionPhrase;
     }
 
     public PscRole getNecessaryRole() {
         return necessaryRole;
     }
 
     public Scope getScope() {
         return scope;
     }
 
     public Template getMessageTemplate() {
         return messageTemplate;
     }
 
     public Template getUriTemplate() {
         return uriTemplate;
     }
 
     public String getActionPhrase() {
         return actionPhrase;
     }
 
     public enum Scope {
         REVISION,
         STUDY,
         STUDY_SITE
         ;
 
         public WorkflowStep[] steps() {
             List<WorkflowStep> steps = new LinkedList<WorkflowStep>();
             for (WorkflowStep step : WorkflowStep.values()) {
                 if (step.getScope() == this) steps.add(step);
             }
             return steps.toArray(new WorkflowStep[steps.size()]);
         }
     }
 }
