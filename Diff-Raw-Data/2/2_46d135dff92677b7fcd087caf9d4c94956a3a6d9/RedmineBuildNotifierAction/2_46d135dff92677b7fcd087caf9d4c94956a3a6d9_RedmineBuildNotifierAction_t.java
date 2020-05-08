 package org.jenkinsci.plugins.redmine_build_notifier;
 
 import hudson.model.AbstractBuild;
 import hudson.model.Action;
 
 /**
  *
  */
 public class RedmineBuildNotifierAction implements Action {
 
     private AbstractBuild<?, ?> build;
     private String issueId;
     private String url, icon;
 
     @Override
     public String getDisplayName() {
         return "Redmine: #" + this.issueId;
     }
 
     @Override
     public String getIconFileName() {
        return "/plugin/redmine_build_notifier/ticket_note.png";
     }
 
     @Override
     public String getUrlName() {
         return this.url;
     }
 
     public void setUrl(String url) {
         this.url = url;
     }
 
     public void setIssueId(String issueId) {
         this.issueId = issueId;
     }
 }
