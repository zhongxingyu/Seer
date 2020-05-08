 package controllers;
 
 import java.util.List;
import java.util.regex.Pattern;
 
 import controllers.deadbolt.Restrict;
 import controllers.tabularasa.TableController;
 import models.general.UnitRole;
 import models.tm.Defect;
 import models.tm.Requirement;
 import models.tm.test.Instance;
 import models.tm.test.Script;
 import models.tm.test.Tag;
 import play.data.validation.Valid;
 import play.data.validation.Validation;
 import play.db.jpa.GenericModel;
 import play.mvc.Before;
 
 /**
  * TODO security
  *
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 public class Requirements extends TMController {
 
    public static final Pattern REQ_TAGS = Pattern.compile("^" + "requirement.tags" + "\\[([^\\]]+)\\](.*)$");

     @Restrict(UnitRole.REQVIEW)
     public static void index() {
         render();
     }
 
     @Restrict(UnitRole.REQVIEW)
     public static void requirementsDetailsData(Long baseObjectId, String[] fields) {
         Object base = Lookups.getRequirement(baseObjectId);
         renderFields(base, fields);
     }
 
     @Restrict(UnitRole.REQVIEW)
     public static void tags(Long requirementId) {
         Requirement requirement = Lookups.getRequirement(requirementId);
         Lookups.tags(requirement.tags);
     }
 
     @Restrict(UnitRole.REQEDIT)
     public static void allTags(String q) {
         Lookups.allTags(getActiveProject().getId(), Tag.TagType.REQUIREMENT, q);
     }
 
     @Restrict(UnitRole.REQEDIT)
     public static void linkScript(Long requirementId, Long scriptId) {
         Requirement requirement = Lookups.getRequirement(requirementId);
         Script script = Lookups.getScript(scriptId);
         requirement.linkedScripts.add(script);
         requirement.save();
         ok();
     }
 
     @Restrict(UnitRole.REQEDIT)
     public static void unlinkScript(Long requirementId, Long scriptId) {
         Requirement requirement = Lookups.getRequirement(requirementId);
         Script script = Lookups.getScript(scriptId);
         requirement.linkedScripts.remove(script);
         requirement.save();
         ok();
     }
 
     @Restrict(UnitRole.REQVIEW)
     public static void linkedScripts(String tableId,
                                      String sColumns,
                                      String sEcho,
                                      Long requirementId) {
         GenericModel.JPAQuery query = Requirement.find("select s from Requirement r, Script s where r.id = ? and s in elements(r.linkedScripts)", requirementId);
         List<Script> scripts = query.fetch();
         long totalRecords = scripts.size();
         TableController.renderJSON(scripts, Script.class, totalRecords, sColumns, sEcho);
     }
 
     @Restrict(UnitRole.REQVIEW)
     public static void linkedInstances(String tableId,
                                        String sColumns,
                                        String sEcho,
                                        Long requirementId) {
         GenericModel.JPAQuery query = Requirement.find("select i from Requirement r, Script s, Instance i where r.id = ? and s in elements(r.linkedScripts) and i.script = s", requirementId);
         List<Instance> instances = query.fetch();
         long totalRecords = instances.size();
         TableController.renderJSON(instances, Instance.class, totalRecords, sColumns, sEcho);
     }
 
     @Restrict(UnitRole.REQVIEW)
     public static void linkedDefects(String tableId,
                                      String sColumns,
                                      String sEcho,
                                      Long requirementId) {
         GenericModel.JPAQuery query = Requirement.find("select d from Requirement r, Script s, Instance i, Defect d where r.id = ? and s in elements(r.linkedScripts) and i.script = s and d in elements(i.defects)", requirementId);
         List<Defect> defects = query.fetch();
         long totalRecords = defects.size();
         TableController.renderJSON(defects, Defect.class, totalRecords, sColumns, sEcho);
     }
 
     @Before
     public static void handleTags() {
         if (request.actionMethod.equals("edit")) {
             processTags("requirement.tags", Tag.TagType.REQUIREMENT);
         }
     }
 
     @Restrict(UnitRole.REQEDIT)
     public static void edit(@Valid Requirement requirement) {
         checkInAccount(requirement);
         checkAuthenticity();
         if (Validation.hasErrors()) {
             // TODO handle validation errors in view somehow
             error();
         }
         requirement.save();
         ok();
     }
 
 }
