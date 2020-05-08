 package controllers;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.persistence.Query;
 
 import controllers.deadbolt.Deadbolt;
 import controllers.deadbolt.Restrict;
 import controllers.tabularasa.TableController;
 import models.general.UnitRole;
 import models.tm.Defect;
 import models.tm.DefectStatus;
 import models.tm.TMUser;
 import models.tm.approach.Release;
 import models.tm.test.*;
 import org.apache.commons.lang.StringUtils;
 import play.db.jpa.GenericModel;
 import play.mvc.With;
 import util.FilterQuery;
 import util.Logger;
 
 /**
  * TODO security
  *
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 @With(Deadbolt.class)
 public class Execution extends TMController {
 
     public static final String ACTUAL_RESULT = "actualResult_";
     public static final String STATUS = "status_";
     public static final String PARAM = "param_";
 
     @Restrict(UnitRole.TESTEXECVIEW)
     public static void index() {
         List<Release> releases = Release.find("from Release r where r.project = ?", getActiveProject()).fetch();
         List<TMUser> users = TMUser.listByProject(getActiveProjectId());
         render(releases, users);
     }
 
     public static void allUsers() {
         Lookups.allUsers();
     }
 
     public static void allTags(String q) {
         Lookups.allTags(getActiveProject().getId(), Tag.TagType.TESTINSTANCE, q);
     }
 
     @Restrict(UnitRole.TESTEXECVIEW)
     public static void instances(String tableId,
                                  Integer iDisplayStart,
                                  Integer iDisplayLength,
                                  String sColumns,
                                  String sEcho,
                                  Long cycle,
                                  Integer status,
                                  String tags,
                                  Long responsible,
                                  Date dateFrom,
                                  Date dateTo) {
 
 
         FilterQuery fq = new FilterQuery(Instance.class);
 
         Map<String, Object> filters = new HashMap<String, Object>();
 
         fq.addFilter("project", "=", getActiveProject());
 
         if (cycle != null) {
             fq.addFilter("testCycle.id", "=", cycle);
         }
         if (status != null) {
             fq.addFilter("status", "=", status);
         }
         if (tags != null && !StringUtils.isEmpty(tags)) {
             fq.addJoin("tags", "o", "t");
             fq.setDistinct(true);
             // Hibernate has a nasty bug that will yield in a ClassCastException when passing a String[], so we need to cast here
             fq.addWhere("t.name in (:tags)", "tags", Arrays.asList(tags.split(",")));
             fq.addAfterWhere("group by o.id having count(t.id) = " + tags.split(",").length);
         }
         if (responsible != null) {
             fq.addFilter("responsible.id", "=", responsible);
         }
         if (dateFrom != null) {
             fq.addWhere("o.plannedExecution >= :dateFrom", "dateFrom", dateFrom);
         }
         if (dateTo != null) {
             fq.addWhere("o.plannedExecution <= :dateTo", "dateTo", dateTo);
         }
 
         Query query = fq.build();
         if (iDisplayStart != null) {
             query.setFirstResult(iDisplayStart);
         }
         if (iDisplayLength != null) {
             query.setMaxResults(iDisplayLength);
         }
         List instances = query.getResultList();
         long totalRecords = instances.size();
 
         TableController.renderJSON(instances, Instance.class, totalRecords, sColumns, sEcho);
     }
 
     @Restrict(UnitRole.TESTEXECVIEW)
     public static void runs(String tableId, Long instanceId,
                             Integer iDisplayStart,
                             Integer iDisplayLength,
                             String sColumns,
                             String sEcho,
                             String sSearch) {
 
         Instance instance = Lookups.getInstance(instanceId);
 
         GenericModel.JPAQuery query = null;
         String q = "from Run r where r.instance = ? and r.project = ? and r.temporary = false order by r.executionDate desc";
         if (sSearch != null && sSearch.length() > 0) {
             // TODO implement the search
             query = Run.find(q, instance, TMController.getActiveProject());
         } else {
             query = Run.find(q, instance, TMController.getActiveProject()).from(iDisplayStart == null ? 0 : iDisplayStart);
         }
         List<Run> runs = query.fetch(iDisplayLength == null ? 10 : iDisplayLength);
         long totalRecords = Run.count();
         TableController.renderJSON(runs, Run.class, totalRecords, sColumns, sEcho);
     }
 
     @Restrict(UnitRole.TESTEXECVIEW)
     public static void runSteps(String tableId, Long runId,
                                 Integer iDisplayStart,
                                 Integer iDisplayLength,
                                 String sColumns,
                                 String sEcho,
                                 String sSearch) {
         Run run = Lookups.getRun(runId);
         GenericModel.JPAQuery query = null;
         // TODO implement the search
         if (sSearch != null && sSearch.length() > 0) {
             query = RunStep.find("from RunStep s where s.run = ? and s.project = ? order by s.position asc", run, TMController.getActiveProject());
         } else {
             query = RunStep.find("from RunStep s where s.run = ? and s.project = ? order by s.position asc", run, TMController.getActiveProject()).from(iDisplayStart == null ? 0 : iDisplayStart);
         }
         List<RunStep> runSteps = query.fetch(iDisplayLength == null ? 10 : iDisplayLength);
         long totalRecords = runSteps.size();
         TableController.renderJSON(runSteps, RunStep.class, totalRecords, sColumns, sEcho);
     }
 
     @Restrict(UnitRole.TESTEXECCREATE)
     public static void createRunDialog(Long instanceId) {
         Instance instance = Lookups.getInstance(instanceId);
         if (instance == null) {
             notFound();
         }
 
         // create the run
         Run run = new Run(getActiveProject());
         run.project = instance.project;
         run.instance = instance;
         run.tester = getConnectedUser();
         run.executionDate = new Date();
         run.executionStatus = ExecutionStatus.NOT_RUN;
         // for now this run is temporary, it will be a permanent one when the user actually saves the run.
         run.temporary = true;
         run.create();
 
         // copy the steps
         for (ScriptStep step : instance.script.getSteps()) {
             RunStep runStep = new RunStep(getActiveProject());
             runStep.project = instance.project;
             runStep.run = run;
             runStep.executionStatus = ExecutionStatus.NOT_RUN;
 
             runStep.name = step.name;
             runStep.position = step.position;
             runStep.description = step.description;
             runStep.expectedResult = step.expectedResult;
 
             runStep.create();
         }
 
         // copy the parameters
         for (InstanceParam param : instance.getParams()) {
             RunParam runParam = new RunParam(getActiveProject());
             runParam.project = instance.project;
             runParam.run = run;
 
             runParam.name = param.scriptParam.name;
             runParam.value = param.value;
 
             runParam.create();
         }
         String formName = "runForm";
         Integer numberOfSteps = run.getSteps().size();
         render("Execution/runExecution.html", run, formName, numberOfSteps);
     }
 
     @Restrict(UnitRole.TESTEXECEDIT)
     public static void updateRunDialog(Long runId) {
         Run run = Lookups.getRun(runId);
         String formName = "updateForm";
        Integer numberOfSteps = run.getSteps().size();
        render("Execution/runExecution.html", run, formName, numberOfSteps);
     }
 
     @Restrict(UnitRole.TESTEXECEDIT)
     public static void updateRun(Long runId) {
         Run run = Lookups.getRun(runId);
         if (run == null) {
             notFound();
         }
 
         // Play can bind Lists of entities as well, using as form input name things like step[id].status and then in the action method List<RunStep> step (make sure it's the same name - no plural!)
         // that is, this automatic binding is sort of buggy: it generates a lot of null elements in the list and also does not pre-load the JPA entity, so this is sort of broken
 
         // TODO rewrite this once we have proper binding
         for (String param : params.all().keySet()) {
             String paramValue = params.all().get(param)[0];
             if (param.startsWith(ACTUAL_RESULT)) {
                 String id = param.substring(ACTUAL_RESULT.length());
                 RunStep step = getRunStep(id, run);
                 step.actualResult = paramValue;
                 step.save();
             } else if (param.startsWith(STATUS)) {
                 String id = param.substring(STATUS.length());
                 RunStep step = getRunStep(id, run);
                 if (paramValue != null && paramValue.length() > 0) {
                     try {
                         Integer status = Integer.parseInt(paramValue);
                         if (status != null) {
                             step.executionStatus = ExecutionStatus.fromPosition(status);
                             step.save();
                         }
                     } catch (NumberFormatException nfe) {
                         Logger.error(Logger.LogType.TECHNICAL, nfe, "Could not parse status, paramValue '%s', project id %s, user %s", paramValue, getActiveProject().getId(), Security.connected());
                         error("Error reading status of step " + step.name);
                     } catch (IllegalArgumentException iae) {
                         Logger.error(Logger.LogType.TECHNICAL, iae, "Could not infer step from position %s, project id %s, user %s", paramValue, getActiveProject().getId(), Security.connected());
                         error("Error retrieving status of step " + step.name);
                     }
                 } else {
                     // no status picked
                     step.executionStatus = ExecutionStatus.NOT_RUN;
                     step.save();
                 }
             }
         }
 
         // run is not temporary anymore
         run.temporary = false;
 
         // re-compute Run and Instance status
         run.updateStatus();
         run.instance.updateStatus();
 
         if (run.executionStatus == ExecutionStatus.FAILED) {
             renderJSON(run.id);
         }
 
         ok();
     }
 
     @Restrict(UnitRole.TESTEXECDELETE)
     public static void deleteRun(Long runId) {
         Run run = Lookups.getRun(runId);
         if (run == null) {
             notFound();
         } else {
             try {
                 run.delete();
                 run.instance.updateStatus();
             } catch (Throwable t) {
                 Logger.error(Logger.LogType.TECHNICAL, t, "Could not delete run %s", runId);
                 error("Error deleting run");
             }
         }
         ok();
     }
 
     @Restrict(UnitRole.TESTEXECEDIT)
     public static void updateParameter(Long runId, String id, String value) {
         Run run = Lookups.getRun(runId);
         if (run == null) {
             notFound("Could not find run " + runId);
         }
         if (id.startsWith(PARAM)) {
             String param = id.substring(PARAM.length());
             try {
                 // param_id_UUID
                 param = param.substring(0, param.indexOf("_"));
                 Long pId = Long.parseLong(param);
                 RunParam runParam = Lookups.getRunParam(pId);
                 if (runParam == null) {
                     notFound("Could not find parameter " + pId);
                 }
                 runParam.value = value;
                 runParam.save();
                 renderText(value);
             } catch (NumberFormatException nfe) {
                 Logger.error(Logger.LogType.TECHNICAL, nfe, "Could not parse parameter ID '%s'", param);
                 error("Could not update parameter");
             }
         } else {
             Logger.error(Logger.LogType.SECURITY, "Invalid parameter '%s'", id);
             error("Wrong parameter id");
         }
     }
 
     @Restrict(UnitRole.TESTEXECVIEW)
     private static RunStep getRunStep(String id, Run run) {
         if (id != null && id.length() > 0) {
             RunStep step = null;
             try {
                 Long lid = Long.parseLong(id);
                 step = RunStep.find("from RunStep step where step.id = ? and step.project = ? and step.run = ?", lid, getActiveProject(), run).first();
                 if (step != null) {
                     checkInAccount(step);
                 }
             } catch (NumberFormatException nfe) {
                 Logger.error(Logger.LogType.TECHNICAL, nfe, "Invalid parameter '%s'", id);
             }
             return step;
         }
         return null;
     }
 
     @Restrict(UnitRole.TESTEXECCREATE)
     public static void createDefect(Long id) {
         Run run = Lookups.getRun(id);
         Defect defect = new Defect(getActiveProject());
         defect.name = run.instance.name;
         defect.submittedBy = getConnectedUser();
         defect.status = DefectStatus.getDefaultDefectStatus();
         defect.save();
         run.instance.defects.add(defect);
 
         run.save();
 
         renderJSON(defect.getId());
     }
 }
