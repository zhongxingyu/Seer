 package util;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
import controllers.TMTree;
 import models.tm.Defect;
 import models.tm.DefectStatus;
 import models.tm.Project;
 import models.tm.ProjectModel;
 import models.tm.ProjectTreeNode;
 import models.tree.jpa.TreeNode;
 import play.Play;
 import play.db.jpa.JPA;
 import play.db.jpa.Model;
 import play.test.Fixtures;
 
 /**
  * TestDataLoader for TM. We load demonstration / test data from initial-data.yml but also have template data for e.g. new Projects in project-data.yml.
  * <p/>
  * When a new project is created via initial-data.yml we call the template data provisioning via project-data.yml
  * When entities that refer to template data (e.g. a Defect in initial-data refers to a DefectStatus created via project-data) are loaded, we link them by getting
  * their value through a cache.
  *
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 public class TestDataLoader {
 
 
     public TestDataLoader() {
 
         final Map<String, Map<Project, Model>> templateDataCache = new HashMap<String, Map<Project, Model>>();
 
         Fixtures.deleteDatabase();
         YamlModelLoader.loadModels("initial-data.yml", new YamlModelLoader.Callback<Model>() {
                     public Model invoke(Model result) {
                         if (result instanceof Defect) {
                             // fetch the new defect status of the current project
                             Defect defect = (Defect) result;
                             DefectStatus status = (DefectStatus) templateDataCache.get(DefectStatus.class.getName()).get(defect.project);
                             status = JPA.em().merge(status);
                             defect.status = status;
                             return defect;
                         }
                         return result;
                     }
                 }, new YamlModelLoader.Callback<Model>() {
                     public Model invoke(Model result) {
                         // we have a project, load project template data
                         if (result instanceof Project) {
                             final Project project = (Project) result;
                             YamlModelLoader.loadModels("project-data.yml", new YamlModelLoader.Callback<Model>() {
                                         public Model invoke(Model result) {
                                             ProjectModel projectModel = (ProjectModel) result;
                                             projectModel.project = project;
                                             projectModel.account = project.account;
                                             return projectModel;
                                         }
                                     }, new YamlModelLoader.Callback<Model>() {
                                 public Model invoke(Model result) {
                                     // put template DefectStatus entities in the cache so we can use them later on
                                     if (result instanceof DefectStatus) {
                                         addCacheEntry(result.getClass().getName(), ((ProjectModel) result).project, result, templateDataCache);
                                     }
                                     return result;
                                 }
                             }
                             );
 
                             return project;
                         }
                         return result;
                     }
 
                 },
                 new YamlModelLoader.CustomLoadBinder() {
                     public String getPropertyName() {
                         return "nodeId";
                     }
 
                     public Class<?> getEntityType() {
                         return ProjectTreeNode.class;
                     }
 
                     public Object bindValue(String[] value, Map<String, String[]> params, Map<String, Object> idCache) {
                         if(value[0] == null) return null;
                         if(value[0] instanceof String) {
                             // is this really a string?
                             try {
                                 Long.parseLong(value[0]);
                             } catch(Throwable t) {
                                String type = params.get("object.type")[0];
                                String key = TMTree.getNodeType(type).getNodeClass().getName() + "-" + value[0];
                                 if (!idCache.containsKey(key)) {
                                     throw new RuntimeException("No previous reference found for object of type " + getPropertyName() + " with key " + key);
                                 }
                                return idCache.get(key);
                             }
                         }
                         return Long.parseLong(value[0]);
                     }
                 }
         );
 
         Play.pluginCollection.afterFixtureLoad();
 
         // fix the treeNodes
         List<ProjectTreeNode> rootNodes = TreeNode.find("from ProjectTreeNode n where n.threadRoot is null").fetch();
         for (ProjectTreeNode n : rootNodes) {
             n.threadRoot = n;
             n.save();
         }
     }
 
     private void addCacheEntry(String key, Project project, Model data, Map<String, Map<Project, Model>> templateDataCache) {
         Map<Project, Model> projectModelMap = templateDataCache.get(key);
         if (projectModelMap == null) {
             projectModelMap = new HashMap<Project, Model>();
             templateDataCache.put(key, projectModelMap);
         }
         projectModelMap.put(project, data);
     }
 }
