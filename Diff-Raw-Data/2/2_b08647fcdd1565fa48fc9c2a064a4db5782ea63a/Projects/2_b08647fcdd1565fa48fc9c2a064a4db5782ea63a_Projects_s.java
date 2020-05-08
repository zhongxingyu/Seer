 package controllers.admin;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import controllers.Lookups;
 import controllers.TMController;
 import controllers.deadbolt.Deadbolt;
 import controllers.deadbolt.Restrict;
 import controllers.tabularasa.TableController;
 import models.general.UnitRole;
 import models.tm.Defect;
 import models.tm.Project;
 import models.tm.ProjectModel;
 import models.tm.ProjectRole;
 import models.tm.Requirement;
 import models.tm.TMUser;
 import models.tm.test.Instance;
 import models.tm.test.Script;
 import models.tm.test.Tag;
 import models.tm.test.TagHolder;
 import play.Logger;
 import play.db.jpa.GenericModel;
 import play.mvc.With;
 
 /**
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 @With(Deadbolt.class)
 public class Projects extends TMController {
 
     @Restrict(UnitRole.ACCOUNTADMIN)
     public static void index() {
         render();
     }
 
     @Restrict(UnitRole.ACCOUNTADMIN)
     public static void projectDetails(Long projectId) {
         Project project = Lookups.getProject(projectId);
         render(project);
     }
 
     @Restrict(UnitRole.ACCOUNTADMIN)
     public static void edit(Project project) {
         checkInAccount(project);
         project.save();
         // TODO validation
         ok();
     }
 
     @Restrict(UnitRole.ACCOUNTADMIN)
     public static void roles(Long projectId) {
         Project project = Lookups.getProject(projectId);
         render(project);
     }
 
     @Restrict(UnitRole.ACCOUNTADMIN)
     public static void users(Long projectId) {
         Project project = Lookups.getProject(projectId);
         List<ProjectRole> projectRoles = ProjectRole.findByProject(projectId);
         List<TMUser> accountUsers = TMUser.listByAccount(getConnectedUserAccount().getId());
         render(project, projectRoles, accountUsers);
     }
 
     @Restrict(UnitRole.ACCOUNTADMIN)
     public static void tags(Long projectId){
         render(projectId);
     }
 
     @Restrict(UnitRole.ACCOUNTADMIN)
     public static void tagsData(String tableId,
                                 Integer tagType,
                             Integer iDisplayStart,
                             Integer iDisplayLength,
                             String sColumns,
                             String sEcho,
                             Long projectId,
                             String sSearch) {
 
         if(sSearch==null){
             sSearch = "";
         }
 
         if (projectId == null) {
             error();
         } else {
             GenericModel.JPAQuery query;
             List<Tag> tags = new ArrayList<Tag>();
             switch (tagType){
                 case 0:
                     query = Tag.find("from Tag t where t.project.id = ? and t.type = ? and t.name like ?", projectId, Tag.TagType.REQUIREMENT, sSearch + "%").from(iDisplayStart == null ? 0 : iDisplayStart);
                     tags = query.fetch();
                     break;
                 case 1:
                     query = Tag.find("from Tag t where t.project.id = ? and t.type = ? and t.name like ?", projectId, Tag.TagType.TESTSCRIPT, sSearch + "%").from(iDisplayStart == null ? 0 : iDisplayStart);
                     tags = query.fetch();
                     break;
                 case 2:
                     query = Tag.find("from Tag t where t.project.id = ? and t.type = ? and t.name like ?", projectId, Tag.TagType.TESTINSTANCE, sSearch + "%").from(iDisplayStart == null ? 0 : iDisplayStart);
                     tags = query.fetch();
                     break;
                 case 3:
                     query = Tag.find("from Tag t where t.project.id = ? and t.type = ? and t.name like ?", projectId, Tag.TagType.DEFECT, sSearch + "%").from(iDisplayStart == null ? 0 : iDisplayStart);
                     tags = query.fetch();
                     break;
             }
 
 
             long totalRecords = Tag.count();
             TableController.renderJSON(tags, Tag.class, totalRecords, sColumns, sEcho);
             ok();
         }
     }
 
     @Restrict(UnitRole.ACCOUNTADMIN)
     public static void renameTag(Long tagId, String tagNewName, Long projectId){
         Tag tag = Tag.find("select t from Tag t where t.id=? and t.project.id=?", tagId, projectId).first();
 
         // are there duplicate tags
         List<Tag> tags = Tag.find("select t from Tag t where t.name=? and t.type=? and t.project.id=?", tagNewName, tag.type, projectId).fetch();
 
         if(tags.size()==0){
             tag.name = tagNewName;
             tag.save();
         }
         else if(tags.size()>0){
             renderJSON(tags.get(0).getId());
         }
         
 
     }
 
     @Restrict(UnitRole.ACCOUNTADMIN)
     public static void mergeTags(Long firstTagId, Long secondTagId, Long projectId){
 
         Tag firstTag = Tag.findById(firstTagId);
         Tag secondTag = Tag.findById(secondTagId);
         if(!firstTag.equals(secondTag)){
             List<TagHolder> tagHolder = new ArrayList<TagHolder>();
 
             if(firstTag.type== Tag.TagType.REQUIREMENT){
                 tagHolder = Requirement.find("from Requirement r where r.project.id=?",projectId).fetch();
             }
             else if(firstTag.type== Tag.TagType.TESTSCRIPT){
                 tagHolder = Script.find("from Script s where s.project.id=?", projectId).fetch();
             }
             else if(firstTag.type==Tag.TagType.TESTINSTANCE){
                 tagHolder =  Instance.find("from Instance i where i.project.id=?", projectId).fetch();
             }
             else if(firstTag.type==Tag.TagType.DEFECT){
                 tagHolder = Defect.find("from Defect d where d.project.id=?", projectId).fetch();
             }
 
              for(TagHolder th:tagHolder){
                  if(th.getTags().contains(firstTag)) {
                     th.getTags().remove(firstTag);
                     th.getTags().add(secondTag);
                  }
                  ((ProjectModel)th).save();
              }
             firstTag.delete();
         }
     }
 
     @Restrict(UnitRole.ACCOUNTADMIN)
     public static void addTag(Long projectId, String name, String type){
         
         Project project =  Project.findById(projectId);
         Tag tag = new Tag(project);
         tag.name = name;
         if(type.equals("requirement")){
             tag.type = Tag.TagType.REQUIREMENT;
         }
         else if(type.equals("testScript")){
             tag.type = Tag.TagType.TESTSCRIPT;
         }
         else if(type.equals("testInstance")){
             tag.type = Tag.TagType.TESTINSTANCE;
         }
         else if(type.equals("defect")){
             tag.type = Tag.TagType.DEFECT;
         }
 
        Long tagsNo = Tag.count("from Tag t where t.name = ? and t.type = ?", tag.name, tag.type);
         if(tagsNo==0){
             tag.create();
         }
         else{
             error("Tag with given name already exist for type " + tag.type);
             util.Logger.error(util.Logger.LogType.USER, "Attempt to create a tag with existing name " + tag.name + " type: " +tag.type);
         }
     }
 
     //todo put logging
 
     @Restrict(UnitRole.ACCOUNTADMIN)
     public static void deleteTag(Long tagId, String type){
         Tag tag = Tag.findById(tagId);
         if(tag!=null){
             if(type.equals("requirement")){
                 Long requirementNo = Requirement.count("from Requirement r where ? in elements(r.tags)", tag);
                 if(requirementNo==0){
                     tag.delete();
                 }
                 else{
                     error("This tag is used in requirements. Tag not deleted!");
                     util.Logger.error(util.Logger.LogType.USER, "Attempt to delete used tag " + tag.id + " type: " +tag.type);
                 }
             }
             else if(type.equals("testScript")){
                 Long scriptsNo = Script.count("from Script s where ? in elements(s.tags)", tag);
                 if(scriptsNo==0){
                     tag.delete();
                 }
                 else{
                     error("This tag is used in test scripts. Tag not deleted!");
                     util.Logger.error(util.Logger.LogType.USER, "Attempt to delete used tag " + tag.id + " type: " +tag.type);
                 }
             }
             else if(type.equals("testInstance")){
                 Long instancesNo = Instance.count("from Instance i where ? in elements(i.tags)", tag);
                 if(instancesNo==0){
                     tag.delete();
                 }
                 else{
                     error("This tag is used in test instances. Tag not deleted!");
                     util.Logger.error(util.Logger.LogType.USER, "Attempt to delete used tag " + tag.id + " type: " +tag.type);
                 }
             }
             else if(type.equals("defect")){
                 Long defectsNo = Defect.count("from Defect d where ? in elements(d.tags)", tag);
                 if(defectsNo==0){
                     tag.delete();
                 }
                 else{
                     error("This tag is used in defects. Tag not deleted!");
                     util.Logger.error(util.Logger.LogType.USER, "Attempt to delete used tag " + tag.id + " type: " +tag.type);
                 }
             }
         }
         else{
             error("Tag doesn't exist!");
             util.Logger.error(util.Logger.LogType.SECURITY, "Attempt to delete not existing tag with given id " + tagId);
         }
     }
 
 }
