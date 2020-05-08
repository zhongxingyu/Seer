 package org.motechproject.care.qa.utils;
 
 import org.motechproject.care.domain.Child;
 import org.motechproject.care.domain.Mother;
 import org.motechproject.care.repository.AllChildren;
 import org.motechproject.care.repository.AllMothers;
 import org.motechproject.commcarehq.domain.AlertDocCase;
 import org.motechproject.commcarehq.repository.AllAlertDocCases;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
 
 import java.util.List;
 
@Component
 public class DbUtils {
 
     private AllChildren allChildren;
 
     private AllMothers allMothers;
 
     private AllAlertDocCases allAlertDocCases;
 
     @Autowired
     public DbUtils(AllChildren allChildren, AllMothers allMothers, AllAlertDocCases allAlertDocCases) {
         this.allChildren = allChildren;
         this.allMothers = allMothers;
         this.allAlertDocCases = allAlertDocCases;
     }
 
     public Child getChildFromDb(final String childCaseId) {
         RetryTask<Child> taskToFetchChild = new RetryTask<Child>() {
             @Override
             protected Child perform() {
                 return allChildren.findByCaseId(childCaseId);
             }
         };
 
         return taskToFetchChild.execute(120, 1000);
     }
 
     public Mother getMotherFromDb(final String motherCaseId) {
         RetryTask<Mother> taskToFetchMother = new RetryTask<Mother>() {
             @Override
             protected Mother perform() {
                 return allMothers.findByCaseId(motherCaseId);
             }
         };
 
         return taskToFetchMother.execute(120, 1000);
     }
 
     public AlertDocCase getAlertDocFromDb(final String clientCaseId, final String taskName) {
         RetryTask<AlertDocCase> taskToFetchAlertDocCase = new RetryTask<AlertDocCase>() {
             @Override
             protected AlertDocCase perform() {
                 List<AlertDocCase> alertDocCases = allAlertDocCases.findAllByCaseId(clientCaseId);
                 for(AlertDocCase alertDocCase : alertDocCases) {
                     if(alertDocCase.getXmlDocument().contains("<task_id>"+taskName+"</task_id>")) {
                         return alertDocCase;
                     }
                 }
                 return null;
             }
         };
         return taskToFetchAlertDocCase.execute(300, 1000);
     }
 
 
 
 
 }
