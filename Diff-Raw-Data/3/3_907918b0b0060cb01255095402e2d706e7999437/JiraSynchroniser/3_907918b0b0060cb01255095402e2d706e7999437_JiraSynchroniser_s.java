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
 
 package com.gordcorp.jira2db;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.gordcorp.jira2db.jira.Jira;
 import com.gordcorp.jira2db.persistence.JiraIssueDao;
 import com.gordcorp.jira2db.persistence.SqlSessionFactorySingleton;
 import com.gordcorp.jira2db.persistence.dto.JiraIssueDto;
 import com.gordcorp.jira2db.util.PropertiesWrapper;
 
 public class JiraSynchroniser {
 
 	protected final static Logger log = LoggerFactory
 			.getLogger(JiraSynchroniser.class);
 
 	List<String> projectNames = null;
 
 	Integer minutes = null;
 
 	Date lastSyncDate = null;
 
 	JiraIssueDao jiraIssueDao = null;
 
 	public JiraSynchroniser() {
 		this.jiraIssueDao = new JiraIssueDao(JiraIssueDto.class,
 				SqlSessionFactorySingleton.instance());
 	}
 
 	/**
 	 * Limits the issues returned from Jira to these projects when it is
 	 * searched. An exception is thrown if a given project name is not found in
 	 * Jira.
 	 * 
 	 * @param projectsNames
 	 */
 	public void setProjects(List<String> projectsNames) {
 		this.projectNames = projectsNames;
 		List<String> allProjects = Jira.getAllProjects();
 		for (String project : projectsNames) {
 			if (!allProjects.contains(project)) {
 				throw new RuntimeException("Project not found in Jira: "
 						+ project);
 			}
 		}
 	}
 
 	/**
 	 * If this issue already exists in the database, it is updated. Otherwise it
 	 * is created.
 	 * 
 	 * @param jiraIssueDto
 	 */
 	protected void updateOrCreateIssue(JiraIssueDto jiraIssueDto) {
 		log.info("Checking if issue already exists: " + jiraIssueDto.getKey());
 		JiraIssueDto readJiraIssueDto = jiraIssueDao.getByKey(jiraIssueDto
 				.getKey());
 		if (readJiraIssueDto == null) {
 			log.info("Creating " + jiraIssueDto.getKey());
 			if (jiraIssueDao.create(jiraIssueDto) != 1) {
 				throw new RuntimeException("Problem inserting " + jiraIssueDto);
 			}
 		} else {
 			log.info("Updating " + jiraIssueDto.getKey());
 			if (jiraIssueDao.update(jiraIssueDto) != 1) {
 				throw new RuntimeException("Problem updating " + jiraIssueDto);
 			}
 		}
 	}
 
 	/**
 	 * Sync issues updated since the last sync occurred.
 	 */
 	protected void syncIssuesUpdatedSinceLastSync() {
 
 		int minutesSinceLastSync = minutesSinceLastSync();
 
 		lastSyncDate = Calendar.getInstance().getTime();
 		for (String projectName : projectNames) {
 			List<JiraIssueDto> dtos = Jira.getIssuesUpdatedWithin(projectName,
 					minutesSinceLastSync);
 			for (JiraIssueDto jiraIssueDto : dtos) {
 				updateOrCreateIssue(jiraIssueDto);
 			}
 		}
 	}
 
 	/**
 	 * Calculates the number of minutes elapsed since the last sync
 	 * 
 	 * @return the number of minutes elapsed since the last sync
 	 */
 	protected int minutesSinceLastSync() {
 		if (lastSyncDate == null) {
 			return Integer.MAX_VALUE;
 		}
 
 		Date now = Calendar.getInstance().getTime();
 
 		return (int) ((now.getTime() / 60000) - (lastSyncDate.getTime() / 60000));
 
 	}
 
 	/**
 	 * Sync all issues from jira, regardles of when it was updated
 	 */
 	public void syncAll() {
 		if (projectNames == null) {
 			projectNames = Jira.getAllProjects();
 		}
 
 		log.info("Syncing projects: " + projectNames);
 		lastSyncDate = Calendar.getInstance().getTime();
 		for (String projectName : projectNames) {
 			log.info("Syncing project " + projectName);
 			List<JiraIssueDto> dtos = null;
 			dtos = Jira.getAllIssuesInProject(projectName);
 
 			log.info("Number of issues found: " + dtos.size());
 			for (JiraIssueDto jiraIssueDto : dtos) {
 				updateOrCreateIssue(jiraIssueDto);
 			}
 			log.info("Finished syncing project " + projectName);
 		}
 	}
 
 	/**
 	 * Never returns. Sync and keep syncing in an infinite loop. Jira is polled
 	 * at a rate as given in the properties.
 	 */
 	public void syncForever() {
 
 		int pollRateInMillis = 1000 * Integer.parseInt(PropertiesWrapper
 				.get("jira.pollrate.seconds"));
 		if (pollRateInMillis <= 0) {
 			throw new RuntimeException(
 					"jira.pollrate.seconds must be greater than 0");
 		}
 
 		log.info("Jira will be polled every " + pollRateInMillis / 1000
 				+ "seconds");
 
 		if (projectNames == null) {
 			projectNames = Jira.getAllProjects();
 		}
 
 		log.info("Forever syncing projects " + projectNames);
 
 		try {
 			while (true) {
 
 				syncIssuesUpdatedSinceLastSync();
 
 				Thread.sleep(pollRateInMillis);
 			}
 		} catch (InterruptedException e) {
 			log.info("Stopped infinte loop syncing");
 		}
 	}
 
 }
