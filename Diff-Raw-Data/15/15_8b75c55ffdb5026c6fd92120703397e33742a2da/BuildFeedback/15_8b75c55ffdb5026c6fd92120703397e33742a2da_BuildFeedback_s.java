 package org.openengsb.opencit.core.projectmanager.model;
 
 import java.io.Serializable;
 import java.util.UUID;
 
 @SuppressWarnings("serial")
 public class BuildFeedback implements Serializable {
     public enum BuildResult {
         SUCCESS,
         MERGEFAIL,
         BUILDFAIL,
         TESTFAIL,
         DEPLOYFAIL,
         NESTEDFAIL      // A dependent project reported a failure
     };
 
     private UUID buildId;
     private BuildResult result;
     private BuildFeedback nestedFeedback;
     private String info;
     private String contactInfo;
     private String projectName;
 
     public void setBuildId(UUID buildId) {
         this.buildId = buildId;
     }
     public UUID getBuildId() {
         return buildId;
     }
     public void setResult(BuildResult result) {
         this.result = result;
     }
     public BuildResult getResult() {
         return result;
     }
     public void setNestedFeedback(BuildFeedback nestedFeedback) {
         this.nestedFeedback = nestedFeedback;
     }
     public BuildFeedback getNestedFeedback() {
         return nestedFeedback;
     }
     public void setInfo(String info) {
         this.info = info;
     }
     public String getInfo() {
         return info;
     }
     public void setContactInfo(String contactInfo) {
         this.contactInfo = contactInfo;
     }
     public String getContactInfo() {
         return contactInfo;
     }
     public void setProjectName(String projectName) {
         this.projectName = projectName;
     }
     public String getProjectName() {
         return projectName;
     }
 
     public String formatMessage() {
         // TODO: Internationalization
         String ret;
         
         ret = "Project " + projectName + " tested build " + buildId + " and reports ";
         switch(result) {
             case SUCCESS: ret += "success"; break;
             case MERGEFAIL: ret += "a merge failure"; break;
             case BUILDFAIL: ret += "a build failure"; break;
             case TESTFAIL: ret += "a test failure"; break;
             case DEPLOYFAIL: ret += "a deploy failure"; break;
             case NESTEDFAIL: ret += "an error from a dependent project"; break;
         }
         ret += ".\n";
         ret += "Contact information: " + contactInfo + ".\n";
 
         if (info != null) {
             ret += "Additional information:\n";
             ret += info + "\n\n";
         }
 
         if (nestedFeedback != null) {
             ret += "Nested feedback:\n";
            ret += nestedFeedback.toString();
         }
         return ret;
     }
 }
