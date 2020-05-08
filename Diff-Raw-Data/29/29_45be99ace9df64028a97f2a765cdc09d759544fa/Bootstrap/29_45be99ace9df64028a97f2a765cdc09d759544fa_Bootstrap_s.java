 import java.util.List;
 
 import models.SchemaCreation;
 import models.tm.ProjectTreeNode;
 import models.tm.TMUser;
 import models.tree.jpa.TreeNode;
 import org.hibernate.Session;
 import play.Play;
 import play.db.jpa.JPA;
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 import play.libs.F;
 import play.test.Fixtures;
 import util.Logger;
 
 /**
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 @OnApplicationStart()
 public class Bootstrap extends Job<F.None> {
     @Override
     public void doJob() throws Exception {
 
         // for the fun of it
        Logger.info(Logger.LogType.TECHNICAL, "\n=== Ground control to major Tom\n=== Commencing countdown engines on");
 
         // this is a Bootstrap job for the development mode, making it easy to re-create data and schema elements out of nowhere
         // for production, we'll need to export the schema creation into a consolidated script
         if (Play.mode == Play.Mode.DEV) {
             if (TMUser.count() == 0) {
 
                 // for development: create triggers && validate model
                 SchemaCreation schemaCreation = new SchemaCreation(((Session) JPA.em().getDelegate()).getSessionFactory());
                 schemaCreation.validateCompositeModels();
                 schemaCreation.createTriggers();
 
                 Fixtures.loadModels("initial-data.yml");
 
                 // fix the treeNodes
                 List<ProjectTreeNode> rootNodes = TreeNode.find("from ProjectTreeNode n where n.threadRoot is null").fetch();
                 for (ProjectTreeNode n : rootNodes) {
                     n.threadRoot = n;
                     n.save();
                 }
             }
         }
 
        Logger.info(Logger.LogType.TECHNICAL, "\n=== This is major Tom to ground control\n=== I'm stepping through the door\n=== and I'm in floating in the most perculiar way\n=== and the stars look very different today");
     }
 }
