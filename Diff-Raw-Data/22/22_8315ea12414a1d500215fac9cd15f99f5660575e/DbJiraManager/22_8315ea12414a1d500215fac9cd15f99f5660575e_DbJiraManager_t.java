 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
  * as indicated by the @authors tag. All rights reserved.
  */
 package org.jboss.community.sbs.plugin.jira;
 
 import com.atlassian.jira.rpc.soap.beans.RemoteIssue;
 import com.jivesoftware.base.event.v2.EventListener;
 import com.jivesoftware.cache.Cache;
 import com.jivesoftware.community.*;
 import com.jivesoftware.community.lifecycle.ApplicationState;
 import com.jivesoftware.community.lifecycle.ApplicationStateChangeEvent;
import com.jivesoftware.community.web.GlobalResourceResolver;
 import edu.emory.mathcs.backport.java.util.Arrays;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.jboss.community.sbs.plugin.jira.dao.IssueLinkDAO;
 import org.jboss.community.sbs.plugin.jira.dao.JiraDAO;
 import org.jboss.community.sbs.plugin.jira.dao.RelatedIssueBean;
 import org.springframework.dao.DataIntegrityViolationException;
 import org.springframework.dao.EmptyResultDataAccessException;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * DB Jira manager implementation
  *
  * @author Libor Krzyzanek (lkrzyzan)
  */
 public class DbJiraManager implements JiraManager, EventListener<ApplicationStateChangeEvent> {
 
 	private static final Logger log = LogManager.getLogger(DbJiraManager.class);
 
 	private IssueLinkDAO issueLinkDAO;
 
 	/**
 	 * A cache for HF URL titles.<br>
 	 * Key is {objectype}-{objectId}<br>
 	 * Value is list of issue keys
 	 */
 	private Cache<String, List<String>> issueLinkCache;
 
 	private JiveObjectLoader jiveObjectLoader;
 
 	private RemoteJiraManager remoteJiraManager;
 
	private GlobalResourceResolver globalResourceResolver;

 	private JiraForumReferenceCheckerThread jiraForumReferenceCheckerThread;
 
 	private JiveContext jiveContext;
 
 	private JiraDAO jiraDAO;
 
 	private String sbsDomain;
 
 	public static enum RESULT {
 		OK, FORBIDDEN, ISSUE_NOT_FOUND, OBJECT_NOT_FOUND, ISSUE_CLOSED
 	}
 
 	@Override
 	public void handle(ApplicationStateChangeEvent e) {
 		if (e.getNewState().equals(ApplicationState.RUNNING)) {
 			jiraForumReferenceCheckerThread = new JiraForumReferenceCheckerThread(this);
 			jiraForumReferenceCheckerThread.setDaemon(true);
 			jiraForumReferenceCheckerThread.start();
 		}
 		if (e.getNewState().equals(ApplicationState.SHUTDOWN)) {
 			jiraForumReferenceCheckerThread.stopNextExecution();
 		}
 	}
 
 	private String getCacheKey(int objectType, long objectId) {
 		return objectType + "-" + objectId;
 	}
 
 	@Override
 	public List<String> getRelatedIssues(int objectType, long objectId) {
 		if (log.isInfoEnabled()) {
 			log.info("Get Related Issues for objectType: " + objectType + " id: " + objectId);
 		}
 		final String cacheKey = getCacheKey(objectType, objectId);
 		List<String> issues = issueLinkCache.get(cacheKey);
 		if (issues == null) {
 			try {
 				issues = issueLinkDAO.getByJiveObject(objectType, objectId);
 				issueLinkCache.put(cacheKey, issues);
 			} catch (EmptyResultDataAccessException e) {
 				return null;
 			}
 		}
 		return issues;
 	}
 
 	@Override
 	public String addLink(int objectType, long objectId, String issueID) throws IllegalArgumentException {
 		// Jira uses upper case IDs
 		issueID = issueID.toUpperCase();
 
 		// 1. Check if jiveObject exists
 		JiveObject jiveObject = getJiveObject(objectType, objectId);
 		if (jiveObject == null) {
 			throw new IllegalArgumentException("SBS object is not valid.");
 		}
 
 		// 2. Check if issue exists
 		// and user has permission to this issue - this is not possible
 		RemoteIssue issue;
 		try {
 			issue = remoteJiraManager.getIssue(issueID);
 			if ("6".equalsIgnoreCase(issue.getStatus())) {
 				return RESULT.ISSUE_CLOSED.toString();
 			}
 
 		} catch (Exception e) {
 			log.error("Cannot get Issue from JIRA", e);
 			return RESULT.ISSUE_NOT_FOUND.toString();
 		}
 
 		// 3. Check if link already exists.
 		try {
 			List<String> issues = issueLinkDAO.getByJiveObject(objectType, objectId);
 			if (issues.contains(issueID)) {
 				throw new DataIntegrityViolationException("Link already exists");
 			}
 		} catch (EmptyResultDataAccessException e) {
 			// This is OK. Process can continue.
 		}
 
 		// 5. Update JIRA ticket
		final String url = globalResourceResolver.getURL(jiveObject, true);
 		try {
 			String[] references = remoteJiraManager.getForumReference(issue);
 			if (references != null) {
 				@SuppressWarnings("unchecked")
 				Set<String> refs = new HashSet<String>(Arrays.asList(references));
 
 				refs.add(url);
 
 				remoteJiraManager.updateIssueForumReference(issueID, refs.toArray(new String[refs.size()]));
 			} else {
 				remoteJiraManager.updateIssueForumReference(issueID, new String[]{url});
 			}
 		} catch (Exception e) {
 			log.error("Cannot update JIRA Issue", e);
 			throw new RuntimeException("Cannot update JIRA Issue");
 		}
 
 		// 6. Add link
 		createLinkInDB(objectType, objectId, issueID);
 
 		return RESULT.OK.toString();
 	}
 
 	private void createLinkInDB(int objectType, long objectId, String issueID) {
 		issueLinkDAO.createLink(objectType, objectId, issueID);
 
 		String cacheKey = getCacheKey(objectType, objectId);
 
 		List<String> issues = issueLinkDAO.getByJiveObject(objectType, objectId);
 		issueLinkCache.put(cacheKey, issues);
 	}
 
 	@Override
 	public String removeLink(int objectType, long objectId, String issueID) throws IllegalArgumentException {
 		// 1. Check if jiveObject exists
 		JiveObject jiveObject = getJiveObject(objectType, objectId);
 		if (jiveObject == null) {
 			throw new IllegalArgumentException("SBS object is not valid.");
 		}
 
		final String url = globalResourceResolver.getURL(jiveObject, true);
 
 		// 2. Update JIRA ticket
 		try {
 			RemoteIssue issue = remoteJiraManager.getIssue(issueID);
 			String[] references = remoteJiraManager.getForumReference(issue);
 
 			if (references != null) {
 				@SuppressWarnings("unchecked")
 				Set<String> refs = new HashSet<String>(Arrays.asList(references));
 
 				refs.remove(url);
 
 				remoteJiraManager.updateIssueForumReference(issueID, refs.toArray(new String[refs.size()]));
 			} else {
 				remoteJiraManager.updateIssueForumReference(issueID, null);
 			}
 		} catch (Exception e) {
 			log.error("Cannot update JIRA Issue", e);
 			throw new RuntimeException("Cannot update JIRA Issue");
 		}
 
 		issueLinkDAO.removeLink(objectType, objectId, issueID);
 
 		final String cacheKey = getCacheKey(objectType, objectId);
 		// just completely remove from cache and let retrieve new value in next call
 		issueLinkCache.remove(cacheKey);
 
 		return RESULT.OK.toString();
 	}
 
 	@Override
 	public void updateLinks(int period) {
 		log.debug("Get links from JIRA");
 
 		RemoteIssue[] remoteIssues;
 		try {
 			remoteIssues = remoteJiraManager.getIssuesFromJqlSearch("updated >= \"-" + period + "m\"");
 		} catch (Exception e) {
 			log.error("Cannot get Issues from Remote JIRA", e);
 			return;
 		}
 
 		log.debug("Check each issue if we have it in SBS database");
 		for (RemoteIssue remoteIssue : remoteIssues) {
 			try {
 				updateLink(remoteIssue);
 			} catch (Exception e) {
 				log.error("Updating link of JIRA issue failed. Issue key: " + remoteIssue.getKey(), e);
 			}
 		}
 	}
 
 	private void updateLink(RemoteIssue remoteIssue) {
 		final String issueKey = remoteIssue.getKey();
 
 		String[] values = remoteJiraManager.getForumReference(remoteIssue);
 
 		if (log.isInfoEnabled()) {
 			log.info("Remove All Related Issue in SBS related to ticket: " + issueKey);
 		}
 		// Remove those related issues in all cases and insert them afterwards
 		List<RelatedIssueBean> issuesInDB = issueLinkDAO.getByIssueID(issueKey);
 		for (RelatedIssueBean relatedIssueBean : issuesInDB) {
 			issueLinkCache.remove(getCacheKey(relatedIssueBean.getObjectType(), relatedIssueBean.getObjectID()));
 		}
 		issueLinkDAO.removeLinks(issueKey);
 
 		if (values != null) {
 			for (int i = 0; i < values.length; i++) {
 				String value = values[i];
 				if (value == null || value.isEmpty()) {
 					continue;
 				}
 
 				JiveObject relatedIssueObject = getRelatedIssueJiveObject(value);
 				if (relatedIssueObject != null) {
 					log.info("Create new link (based on JIRA value)");
 					createLinkInDB(relatedIssueObject.getObjectType(), relatedIssueObject.getID(), issueKey);
 				} else {
 					if (log.isInfoEnabled()) {
 						log.info("JBoss Forum Reference is not community.jboss.org resource. URI: " + value);
 					}
 				}
 
 			}
 		}
 
 	}
 
 	private String getSbsDomain() {
 		if (sbsDomain == null) {
 			sbsDomain = JiveGlobals.getJiveProperty("jboss.jira.sbsDomainToCheck", "community.jboss.org/");
 		}
 		return sbsDomain;
 	}
 
 	private JiveObject getRelatedIssueJiveObject(String jiraCustomFieldValueURL) {
 		if (!jiraCustomFieldValueURL.contains(getSbsDomain())) {
 			return null;
 		}
		JiveObject o;
		try {
			o = globalResourceResolver.getObj(jiraCustomFieldValueURL);
		} catch (NotFoundException e) {
 			return null;
 		}
 		// In forums root message is used
 		if (o.getObjectType() == JiveConstants.THREAD) {
 			o = ((ForumThread) o).getRootMessage();
 		}
 		if (o.getObjectType() == JiveConstants.MESSAGE) {
 			o = ((ForumMessage) o).getForumThread().getRootMessage();
 		}
 		return o;
 	}
 
 	private JiveObject getJiveObject(int objectType, long objectId) {
 		try {
 			return jiveObjectLoader.getJiveObject(objectType, objectId);
 		} catch (NotFoundException e) {
 			return null;
 		}
 	}
 
 	@Override
 	public int syncJira2SBS() {
 		log.info("Full Sync JIRA -> SBS started");
 		issueLinkDAO.removeAllLinks();
 		issueLinkCache.clear();
 
 		int count = 0;
 
 		List<RemoteIssue> issues = jiraDAO.getIssuesWithForumReference();
 		for (RemoteIssue remoteIssue : issues) {
 			String[] values = remoteJiraManager.getForumReference(remoteIssue);
 			if (values != null) {
 				for (int i = 0; i < values.length; i++) {
 					String value = values[i];
 					if (value == null || value.isEmpty()) {
 						continue;
 					}
 
 					JiveObject relatedIssueObject = getRelatedIssueJiveObject(value);
 					if (relatedIssueObject != null) {
 						log.info("Create new link (based on JIRA value)");
 						createLinkInDB(relatedIssueObject.getObjectType(), relatedIssueObject.getID(), remoteIssue.getKey());
 						count++;
 					} else {
 						if (log.isInfoEnabled()) {
 							log.info("JBoss Forum Reference is not community.jboss.org resource. URI: " + value);
 						}
 					}
 				}
 			}
 		}
 
 		if (log.isInfoEnabled()) {
 			log.info("Full Sync JIRA -> SBS finished. Count: " + count);
 		}
 		return count;
 	}
 
 	public void setIssueLinkDAO(IssueLinkDAO issueLinkDAO) {
 		this.issueLinkDAO = issueLinkDAO;
 	}
 
 	public void setJiveObjectLoader(JiveObjectLoader jiveObjectLoader) {
 		this.jiveObjectLoader = jiveObjectLoader;
 	}
 
 	public void setRemoteJiraManager(RemoteJiraManager remoteJiraManager) {
 		this.remoteJiraManager = remoteJiraManager;
 	}
 
 	public void setJiveContext(JiveContext jiveContext) {
 		this.jiveContext = jiveContext;
 	}
 
 	public void setJiraDAO(JiraDAO jiraDAO) {
 		this.jiraDAO = jiraDAO;
 	}
 
 	public void setIssueLinkCache(Cache<String, List<String>> issueLinkCache) {
 		this.issueLinkCache = issueLinkCache;
 	}
 
	public void setGlobalResourceResolver(GlobalResourceResolver globalResourceResolver) {
		this.globalResourceResolver = globalResourceResolver;
	}
 }
