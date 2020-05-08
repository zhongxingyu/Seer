 package controllers;
 
 import java.util.List;
 import javax.persistence.Query;
 
 import controllers.deadbolt.Restrict;
 import controllers.tabularasa.TableController;
 import models.general.UnitRole;
import models.tm.Defect;
 import models.tm.Requirement;
 import models.tm.test.Instance;
 import models.tm.test.InstanceParam;
 import models.tm.test.Script;
 import models.tm.test.ScriptParam;
 import models.tm.test.ScriptStep;
 import models.tm.test.Tag;
 import org.apache.commons.lang.StringEscapeUtils;
 import play.data.validation.Valid;
 import play.data.validation.Validation;
 import play.db.jpa.GenericModel;
 import play.db.jpa.JPA;
import util.FilterQuery;
 import util.Logger;
 import util.ParameterHandler;
 
 /**
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 public class Repository extends TMController {
 
     @Restrict(UnitRole.TESTREPOVIEW)
     public static void index() {
         render();
     }
 
     @Restrict(UnitRole.TESTREPOVIEW)
     public static void scriptDetailsData(Long baseObjectId, String[] fields) {
         Object base = Lookups.getScript(baseObjectId);
         renderFields(base, fields);
     }
 
     @Restrict(UnitRole.TESTREPOVIEW)
     public static void tags(Long scriptId) {
         Script script = Lookups.getScript(scriptId);
         Lookups.tags(script.tags);
     }
 
     public static void allTags(String q) {
         Lookups.allTags(getActiveProject().getId(), Tag.TagType.TESTSCRIPT, q);
     }
 
     @Restrict(UnitRole.TESTREPOVIEW)
     public static void stepDetailsData(Long scriptId, Long baseObjectId, String[] fields) {
         ScriptStep step = Lookups.getScriptStep(baseObjectId);
         renderFields(step, fields);
     }
 
     @Restrict(UnitRole.TESTREPOEDIT)
     public static void editScript(@Valid Script script, String tags) {
         checkInAccount(script);
         if (Validation.hasErrors()) {
             // TODO handle validation errors in view somehow
             error();
         }
         script.tags = getTags(tags, Tag.TagType.TESTSCRIPT);
         if (script.tags.isEmpty()) {
             script.tags.clear();
         }
         script.save();
         ok();
     }
 
     @Restrict(UnitRole.TESTREPOEDIT)
     public static void createOrUpdateStep(@Valid ScriptStep step, Long scriptId) {
         if (scriptId == null) {
             Logger.error(Logger.LogType.TECHNICAL, "Could not update script step '%s', no scriptId passed", step.name);
             error("Could not update script step, no script ID passed");
         }
         Script script = Script.findById(scriptId);
         if (script == null) {
             notFound();
         } else {
             checkInAccount(script);
 
             // unescape HTML
             step.description = StringEscapeUtils.unescapeHtml(step.description);
             step.expectedResult = StringEscapeUtils.unescapeHtml(step.expectedResult);
 
             // create parameters
             List<ScriptParam> scriptParameters = ParameterHandler.getScriptParameters(step.description, script);
             scriptParameters.addAll(ParameterHandler.getScriptParameters(step.expectedResult, script));
 
             // create only the new ones
             for (ScriptParam p : scriptParameters) {
                 if (ScriptParam.count("from ScriptParam p where p.name = ? and p.script = ?", p.name, script) == 0) {
                     p.create();
                     // propagate creation of a new param to all instances of this script
                     for (Instance i : script.getInstances()) {
                         InstanceParam ip = new InstanceParam(i.project);
                         ip.instance = i;
                         ip.scriptParam = p;
                         ip.create();
                     }
                 }
             }
 
             // for new steps only
             if (step.id == null) {
                 step.account = script.account;
                 step.project = script.project;
                 step.script = script;
             }
 
             // what's your favourite position?
             if (ScriptStep.count("script.id = ? and position = ?", script.getId(), step.position) > 0) {
                 Query query = JPA.em().createQuery("update ScriptStep s set s.position = s.position + 1 where s.script.id = :scriptId and s.position > :position");
                 query.setParameter("scriptId", script.getId());
                 query.setParameter("position", step.position - 1);
                 query.executeUpdate();
             }
 
             // TODO is there not a "createOrUpdate"
             if (step.id == null) {
                 boolean created = step.create();
                 if (!created) {
                     error();
                 } else {
                     ok();
                 }
             } else {
                 step.save();
                 ok();
             }
         }
     }
 
     @Restrict(UnitRole.TESTREPOVIEW)
     public static void steps(String tableId, Long scriptId,
                              Integer iDisplayStart,
                              Integer iDisplayLength,
                              String sColumns,
                              String sEcho,
                              String sSearch) {
         GenericModel.JPAQuery query = ScriptStep.find("script.id = ? order by position", scriptId).from(iDisplayStart == null ? 0 : iDisplayStart);
         List<ScriptStep> steps = query.fetch(iDisplayLength == null ? 10 : iDisplayLength);
         long totalRecords = ScriptStep.count("script.id", scriptId);
         TableController.renderJSON(steps, ScriptStep.class, totalRecords, sColumns, sEcho);
     }
 
     @Restrict(UnitRole.TESTREPOVIEW)
     public static void linkedRequirements(String tableId,
                                           String sColumns,
                                           String sEcho,
                                           Long scriptId) {

        GenericModel.JPAQuery query = Requirement.find("select r from Requirement r join r.linkedScripts s where s.id=?", scriptId);
         List<Requirement> requirements = query.fetch();
         long totalRecords = requirements.size();

         TableController.renderJSON(requirements, Requirement.class, totalRecords, sColumns, sEcho);
     }
 
 }
