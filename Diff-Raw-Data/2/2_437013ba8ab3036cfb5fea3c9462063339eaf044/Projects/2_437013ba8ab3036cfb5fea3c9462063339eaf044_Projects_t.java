 package controllers.admin;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import controllers.Lookups;
 import controllers.TMController;
 import controllers.deadbolt.Deadbolt;
 import controllers.deadbolt.Restrict;
 import controllers.deadbolt.Restrictions;
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
 import play.mvc.Util;
 import play.mvc.With;
 import util.Logger;
 
 /**
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 @With(Deadbolt.class)
 public class Projects extends TMController {
 
     @Restrictions({@Restrict(UnitRole.PROJECTEDIT), @Restrict(UnitRole.PROJECTADMIN)})
     public static void index() {
         render();
     }
 
     @Restrict(UnitRole.PROJECTEDIT)
     public static void projectDetails(Long baseObjectId, String[] fields) {
         Object base = Lookups.getProject(baseObjectId);
         renderFields(base, fields);
     }
 
     @Restrictions({@Restrict(UnitRole.PROJECTEDIT), @Restrict(UnitRole.PROJECTADMIN)})
     public static void edit(Project project) {
         checkInAccount(project);
         project.save();
         // TODO validation
         ok();
     }
 
     @Restrictions({@Restrict(UnitRole.PROJECTEDIT), @Restrict(UnitRole.PROJECTADMIN)})
     public static void roles(Long projectId) {
         Project project = Lookups.getProject(projectId);
         render(project);
     }
 
 
 
     @Restrictions({@Restrict(UnitRole.PROJECTEDIT), @Restrict(UnitRole.PROJECTADMIN)})
     public static void tagsData(String tableId,
                                 String tagType,
                                 Integer iDisplayStart,
                                 Integer iDisplayLength,
                                 String sColumns,
                                 String sEcho,
                                 Long projectId,
                                 String sSearch) {
 
         if (sSearch == null) {
             sSearch = "";
         }
 
         if (projectId == null) {
             Logger.error(Logger.LogType.TECHNICAL, "No projectId passed when loading tags");
             error();
         } else {
             Tag.TagType type = getTagType(tagType);
             List<Tag> tags = Tag.find("from Tag t where t.project.id = ? and t.type = ? and t.name like ?", projectId, type, sSearch + "%").from(iDisplayStart == null ? 0 : iDisplayStart).fetch();
             long totalRecords = Tag.count();
             TableController.renderJSON(tags, Tag.class, totalRecords, sColumns, sEcho);
         }
     }
 
     @Restrictions({@Restrict(UnitRole.PROJECTEDIT), @Restrict(UnitRole.PROJECTADMIN)})
     public static void renameTag(Long tagId, String tagNewName, Long projectId) {
         Tag tag = Tag.find("select t from Tag t where t.id=? and t.project.id=?", tagId, projectId).first();
 
         // are there duplicate tags
         List<Tag> tags = Tag.find("select t from Tag t where t.id!=? and t.name=? and t.type=? and t.project.id=?", tagId, tagNewName, tag.type, projectId).fetch();
 
         if (tags.size() == 0) {
             tag.name = tagNewName;
             tag.save();
         } else if (tags.size() > 0) {
             renderJSON(tags.get(0).getId());
         }
 
 
     }
 
     @Restrictions({@Restrict(UnitRole.PROJECTEDIT), @Restrict(UnitRole.PROJECTADMIN)})
     public static void mergeTags(Long firstTagId, Long secondTagId, Long projectId) {
 
         Tag firstTag = Lookups.getTag(firstTagId);
         Tag secondTag = Lookups.getTag(secondTagId);
 
         if (firstTag == null) {
             notFound("Could not find tag with ID " + firstTagId);
         } else if (secondTag == null) {
             notFound("Could not find tag with ID " + secondTagId);
        } else if (firstTag.id!=secondTag.id) {
             List<TagHolder> tagHolder = new ArrayList<TagHolder>();
 
             if (firstTag.type == Tag.TagType.REQUIREMENT) {
                 tagHolder = Requirement.find("from Requirement r where r.project.id=?", projectId).fetch();
             } else if (firstTag.type == Tag.TagType.TESTSCRIPT) {
                 tagHolder = Script.find("from Script s where s.project.id=?", projectId).fetch();
             } else if (firstTag.type == Tag.TagType.TESTINSTANCE) {
                 tagHolder = Instance.find("from Instance i where i.project.id=?", projectId).fetch();
             } else if (firstTag.type == Tag.TagType.DEFECT) {
                 tagHolder = Defect.find("from Defect d where d.project.id=?", projectId).fetch();
             }
 
             for (TagHolder th : tagHolder) {
                 if (th.getTags().contains(firstTag)) {
                     th.getTags().remove(firstTag);
                     th.getTags().add(secondTag);
                 }
                 ((ProjectModel) th).save();
             }
             firstTag.delete();
         }
         ok();
     }
 
     @Restrictions({@Restrict(UnitRole.PROJECTEDIT), @Restrict(UnitRole.PROJECTADMIN)})
     public static void addTag(Long projectId, String name, String type) {
 
         Project project = Lookups.getProject(projectId);
         Tag tag = new Tag(project);
         tag.name = name;
         tag.type = getTagType(type);
 
 
         Long tagsNo = Tag.count("from Tag t where t.name = ? and t.type = ?", tag.name, tag.type);
         if (tagsNo == 0) {
             tag.create();
         } else {
             error("Tag with given name already exist for type " + tag.type);
             Logger.error(Logger.LogType.USER, "Attempt to create a tag with existing name " + tag.name + " type: " + tag.type);
         }
     }
 
     @Restrictions({@Restrict(UnitRole.PROJECTEDIT), @Restrict(UnitRole.PROJECTADMIN)})
     public static void deleteTag(Long tagId, String type) {
         Tag tag = Lookups.getTag(tagId);
 
         if (tag == null) {
             error("Tag doesn't exist!");
             Logger.error(Logger.LogType.SECURITY, "Attempt to delete not existing tag with given id " + tagId);
 
         }
 
         if (tag != null) {
             Tag.TagType tagType = getTagType(type);
             Long usageCount = 0l;
             switch (tagType) {
                 case REQUIREMENT:
                     usageCount = Requirement.count("from Requirement r where ? in elements(r.tags)", tag);
                     break;
                 case TESTSCRIPT:
                     usageCount = Script.count("from Script s where ? in elements(s.tags)", tag);
                     break;
                 case TESTINSTANCE:
                     usageCount = Instance.count("from Instance i where ? in elements(i.tags)", tag);
                     break;
                 case DEFECT:
                     usageCount = Defect.count("from Defect d where ? in elements(d.tags)", tag);
                     break;
                 default:
                     // unknown type
                     Logger.error(Logger.LogType.SECURITY, "Attempt to delete tag with unknown type %s", type);
                     error("Unknown tag type");
             }
             if (usageCount == 0l) {
                 tag.delete();
             } else {
                 error("This tag is still in use in a " + tagType.getName() + ". Tag not deleted.");
                 Logger.error(Logger.LogType.USER, "Attempt to delete used tag " + tag.id + " type: " + tag.type);
             }
         }
     }
 
     @Util
     private static Tag.TagType getTagType(String type) {
         if (type == null) {
             Logger.error(Logger.LogType.TECHNICAL, "Null tag type passed");
             error("No type passed");
         }
         Tag.TagType tagType = null;
         try {
             tagType = Tag.TagType.valueOf(type.toUpperCase());
         } catch (IllegalArgumentException iae) {
             Logger.error(Logger.LogType.SECURITY, "Invalid tag type %s", type);
             error("Unknown tag type " + tagType);
         }
         return tagType;
     }
 
 }
