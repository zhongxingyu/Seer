 package org.groink;
 
 
 import com.taskadapter.redmineapi.RedmineException;
 import com.taskadapter.redmineapi.RedmineManager;
 import com.taskadapter.redmineapi.bean.Issue;
 import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.internal.RedmineJSONBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.List;
 
 public class QuickStart {
 
     private static String redmineHost = "http://redmine.adencf.local";
     private static String apiAccessKey = "ada0245726a7ecc45c074b785e4614808d17b202";
     private static String sourceProjectKey = "adenservices";
     private static String destinationProjectKey = "ws-search-cv-add";
     private static Integer queryId = null; // any
 
     private static Logger logger = LoggerFactory.getLogger("QuickStart");
 
     public static void main(String[] args) {
         RedmineManager mgr = new RedmineManager(redmineHost, apiAccessKey);
         try {
             //tryGetIssues(mgr);
            tryMigrateSingleIssue(3664, mgr);
 
 
         } catch (Exception e) {
             logger.error(e.getMessage(),e);
             e.printStackTrace();
         }
     }
 
     private static void tryMigrateSingleIssue(int issueId, RedmineManager mgr) {
         try {
             Issue toMigrate = mgr.getIssueById(issueId);
             Project p = mgr.getProjectByKey(destinationProjectKey);
             toMigrate.getProject().setId(p.getId());
            //toMigrate.getProject().setDescription(p.getDescription());
            //logger.debug(toMigrate.getProject().toString());

            //toMigrate.setNotes("test update api");
             toMigrate.setProject(p);
            //mgr.update(toMigrate);
 
         } catch (RedmineException e) {
             logger.error(e.getMessage(),e);
         }
     }
 
     private static void tryGetIssues(RedmineManager mgr) throws Exception {
         List<Issue> issues = mgr.getIssues(sourceProjectKey, queryId);
         int i = 0;
         for (Issue issue : issues) {
             System.out.println(issue.toString());
             i++;
         }
         System.out.println(i + " tickets listés.");
 
     }
 }
 
 
