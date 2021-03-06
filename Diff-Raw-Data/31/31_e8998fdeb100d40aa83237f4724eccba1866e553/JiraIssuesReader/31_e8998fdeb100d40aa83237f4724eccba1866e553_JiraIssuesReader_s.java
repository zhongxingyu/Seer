 /*
  * Sonar, entreprise quality control tool.
  * Copyright (C) 2007-2008 Hortis-GRC SA
  * mailto:be_agile HAT hortis DOT ch
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package org.sonar.plugins.jira;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Transformer;
 import org.codehaus.swizzle.jira.Issue;
 import org.codehaus.swizzle.jira.Jira;
 import org.codehaus.swizzle.jira.JiraRss;
 import org.codehaus.swizzle.jira.Project;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.net.MalformedURLException;
 import java.util.Collection;
 
 public class JiraIssuesReader {
 
   private static final Logger LOG = LoggerFactory.getLogger(JiraIssuesReader.class);
 
   private static final String RPC_PATH = "/rpc/xmlrpc";
   private static final String XML_PATH = "/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml";
   private static final String XML_PATH_OPT = "tempMax=1000";
   private static final String PID_OPT = "pid";
 
   private String urlParams;
 
   private String serverUrl;
   private String login;
   private String password;
   private String projectName;
 
 
   public JiraIssuesReader(String serverUrl, String projectName, String login, String password, String urlParams) {
     this.serverUrl = serverUrl;
     this.login = login;
     this.password = password;
     this.projectName = projectName;
     this.urlParams = urlParams;
   }
 
   private int getProjectId() {
     try {
       Project project = getProject(projectName);
       int id = project.getId();
       LOG.debug("Jira project Id is {}", id);
       return id;
 
     } catch (MalformedURLException e) {
       throw new JiraException("Problem with jira url" + getJiraRpcUrl(), e);
     } catch (Exception e) {
       throw new JiraException("Problem accessing jira rpc", e);
     }
   }
 
   private Project getProject(String projectName) throws Exception {
     String jiraRpcUrl = getJiraRpcUrl();
    LOG.debug("Jira RPC url is {}", jiraRpcUrl);
 
     Jira jira = new Jira(jiraRpcUrl);
     jira.login(login, password);
     return jira.getProject(projectName);
   }
 
   private String getJiraRpcUrl() {
     return serverUrl + RPC_PATH;
   }
 
   public Collection getIssues() {
     try {
       String jiraXmlUrl = getJiraXmlUrl();
      LOG.debug("Jira XML url is {}", jiraXmlUrl);
      System.out.println(jiraXmlUrl);
 
       JiraRss jirarss = new JiraRss(getJiraXmlUrl());
 
       return CollectionUtils.collect(jirarss.getIssues(), new Transformer() {
         public Object transform(Object o) {
           Issue issue = (Issue) o;
           return issue.getPriority().getName();
         }
       });
 
     } catch (Exception e) {
       throw new JiraException("Problem accessing jira rss filter", e);
     }
   }
 
   private String getJiraXmlUrl() {
     return serverUrl + XML_PATH + "?" + PID_OPT + "=" + getProjectId() + "&" + urlParams + "&" + XML_PATH_OPT;
   }
 
 
 }
