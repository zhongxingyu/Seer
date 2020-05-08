 package eu.alertproject.iccs.socrates.ui.services;
 
 import eu.alertproject.iccs.socrates.datastore.api.IssueSubjectDao;
 import eu.alertproject.iccs.socrates.datastore.api.UuidClassDao;
 import eu.alertproject.iccs.socrates.datastore.api.UuidIssueDao;
 import eu.alertproject.iccs.socrates.datastore.api.UuidSubjectDao;
 import eu.alertproject.iccs.socrates.domain.IssueSubject;
 import eu.alertproject.iccs.socrates.ui.bean.Bug;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.locks.ReentrantLock;
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 /**
  * Created by IntelliJ IDEA. User: fotis Date: 08/02/12 Time: 6:58 PM
  */
 @Service("bugSearchService")
 public class DefaultBugSearchService implements BugSearchService {
 
     private Logger logger = LoggerFactory.getLogger(DefaultBugSearchService.class);
     private Map<Integer, Bug> data;
 
     
     @Autowired
     IssueSubjectDao issueSubjectDao;
     @Autowired
     UuidIssueDao uuidIssueDao;
  
 
     public DefaultBugSearchService() throws IOException {
 
 
         data = new HashMap<Integer, Bug>();
 
         //Read all issues
 //        List<Integer> allIssues = issueSubjectDao.findAllIssues();
 //
 //        String bugTitle, bugDescription;
 //        Bug currentBug;
 //        //for each issue
 //        for (Integer i : allIssues) {
 //            // make its title "Bug" + bug id
 //            bugTitle = "Bug #" + i;
 //            bugDescription = "";
 //            List<IssueSubject> thisIssueSubjects = issueSubjectDao.findByIssueId(i);
 //
 //            //get its annotations into a string
 //            for (IssueSubject is : thisIssueSubjects) {
 //                bugDescription = bugDescription + "'" + is.getSubject() + "' ";
 //
 //            }
 //            logger.debug(bugDescription);
 //            // create a Bug & add it to data            
 //            data.put(i, new Bug(i, bugTitle, bugDescription));
 //        }
 
 
 
 
         data.put(1010, new Bug(1010, "I am a super bug", "This is my long description"));
         data.put(2050, new Bug(2050, "There is a problem with ACPI", "When I close the lid on my laptop it continues running"));
 
     }
 

    @PostConstruct
    public void init(){

        logger.trace("void init()");

        if (issueSubjectDao == null) {
            logger.error("null autowired object found a");
        }else{
            logger.trace("void init() {} ",issueSubjectDao);
        }

        if (uuidIssueDao == null) {
            logger.error("null autowired object found {} ",issueSubjectDao);
        }

    }

     @Override
     public Bug retrieveBugById(Integer id) {
 
         logger.trace("Bug retrieveBugById()");
         //return the same bug
         if (!data.containsKey(id)) {
             return null;
         }
         return data.get(id);
     }
 }
