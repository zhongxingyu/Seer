 package com.atlassian.sal.jira;
 
 import com.atlassian.jira.config.properties.APKeys;
 import com.atlassian.jira.util.BuildUtils;
 import com.atlassian.sal.api.ApplicationProperties;
 
 import java.util.Date;
 
 /**
  * JIRA implementation of WebProperties
  */
 public class JiraApplicationProperties implements ApplicationProperties
 {
     private final com.atlassian.jira.config.properties.ApplicationProperties applicationProperties;
 
     public JiraApplicationProperties(com.atlassian.jira.config.properties.ApplicationProperties applicationProperties)
     {
         this.applicationProperties = applicationProperties;
     }
 
     public String getBaseUrl()
     {
         return applicationProperties.getDefaultBackedString(APKeys.JIRA_BASEURL);
     }
 
     public String getApplicationName()
     {
         return "JIRA";
     }
 
     public String getVersion()
     {
         return BuildUtils.getVersion();
     }
 
     public Date getBuildDate()
     {
         return BuildUtils.getCurrentBuildDate();
     }
 
     public String getBuildNumber()
     {
         return BuildUtils.getCurrentBuildNumber();
     }
 }
