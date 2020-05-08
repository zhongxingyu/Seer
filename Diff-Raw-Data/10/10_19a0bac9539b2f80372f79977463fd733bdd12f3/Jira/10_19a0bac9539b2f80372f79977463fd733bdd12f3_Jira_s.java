 /*******************************************************************************
  * Copyright 2012 Brendan Gordon
  * 	
  * 	This file is part of Jira2Db.
  * 	
  * 	Jira2Db is free software: you can redistribute it and/or modify
  * 	it under the terms of the GNU General Public License as published by
  * 	the Free Software Foundation, either version 3 of the License, or
  * 	(at your option) any later version.
  * 	
  * 	Jira2Db is distributed in the hope that it will be useful,
  * 	but WITHOUT ANY WARRANTY; without even the implied warranty of
  * 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * 	GNU General Public License for more details.
  * 	
  * 	You should have received a copy of the GNU General Public License
  * 	along with Jira2Db.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package com.gordcorp.jira2db.jira;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.atlassian.jira.rest.client.JiraRestClient;
 import com.atlassian.jira.rest.client.NullProgressMonitor;
 import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
 import com.atlassian.jira.rest.client.domain.BasicIssue;
 import com.atlassian.jira.rest.client.domain.BasicProject;
 import com.atlassian.jira.rest.client.domain.Issue;
 import com.atlassian.jira.rest.client.domain.SearchResult;
 import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
 import com.gordcorp.jira2db.persistence.dto.JiraIssueDto;
 import com.gordcorp.jira2db.util.JiraTransformer;
 import com.gordcorp.jira2db.util.PropertiesWrapper;
 
 public class Jira {
 
 	protected final static Logger log = LoggerFactory.getLogger(Jira.class);
 
 	protected static JerseyJiraRestClientFactory factory = null;
 	protected static JiraRestClient restClient = null;
 
 	static {
 		try {
 
			if (factory != null) {
 				factory = new JerseyJiraRestClientFactory();
 			}
 
			if (restClient != null) {
 				URI jiraServerUri = new URI(
 						PropertiesWrapper.get("jira.server.uri"));
 
 				String username = PropertiesWrapper.get("jira.username");
 				String password = PropertiesWrapper.get("jira.password");
 
 				restClient = factory.create(jiraServerUri,
 						new BasicHttpAuthenticationHandler(username, password));
 			}
 		} catch (URISyntaxException e) {
 			throw new RuntimeException("Problem initialising Jira interface: "
 					+ e.getMessage(), e);
 		}
 	}
 
 	public static List<String> getAllProjects() {
 
 		Iterable<BasicProject> projects = restClient.getProjectClient()
 				.getAllProjects(new NullProgressMonitor());
 		List<String> results = new ArrayList<String>();
 		for (BasicProject basicProject : projects) {
 			results.add(basicProject.getName());
 		}
 		return results;
 	}
 
 	protected static List<JiraIssueDto> getIssues(String jql) {
 		List<JiraIssueDto> result = new ArrayList<JiraIssueDto>();
 		int ISSUES_PER_SEARCH = 50;
 		int issues = 0;
 		SearchResult searchResult = null;
 
 		do {
 			searchResult = restClient.getSearchClient().searchJql(jql,
 					ISSUES_PER_SEARCH, issues, new NullProgressMonitor());
 			for (BasicIssue issueResult : searchResult.getIssues()) {
 				issues++;
 				log.info("issue " + issueResult.getKey());
 
 				Issue issue = restClient.getIssueClient().getIssue(
 						issueResult.getKey(), new NullProgressMonitor());
 
 				JiraIssueDto newJiraIssueDto = JiraTransformer
 						.toJiraIssueDto(issue);
 				result.add(newJiraIssueDto);
 			}
 		} while (issues < searchResult.getTotal());
 
 		return result;
 	}
 
 	public static List<JiraIssueDto> getAllIssuesInProject(String projectName) {
 
 		String jql = "project = \"" + projectName + "\"";
 
 		return getIssues(jql);
 	}
 
 	public static List<JiraIssueDto> getIssuesUpdatedWithin(String projectName,
 			int updatedWithinMinutes) {
 
		String jql = "updated >= \"";
 
 		jql += "-" + updatedWithinMinutes + "m";
 		jql += "\"";
 		return getIssues(jql);
 	}
 }
