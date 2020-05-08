 /*******************************************************************************
  * Copyright (c) Feb 20, 2012 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdklib.monitor;
 
 import java.net.MalformedURLException;
 import java.text.MessageFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.zend.sdklib.internal.application.ZendConnection;
 import org.zend.sdklib.internal.monitor.ZendIssue;
 import org.zend.sdklib.mapping.IMappingLoader;
 import org.zend.sdklib.target.ITargetLoader;
 import org.zend.sdklib.target.IZendTarget;
 import org.zend.webapi.core.WebApiClient;
 import org.zend.webapi.core.WebApiException;
 import org.zend.webapi.core.connection.data.Issue;
 import org.zend.webapi.core.connection.data.IssueList;
 import org.zend.webapi.core.connection.data.values.ZendServerVersion;
 import org.zend.webapi.core.progress.BasicStatus;
 import org.zend.webapi.core.progress.StatusCode;
 
 /**
  * Utility class which provides methods to perform operation related to code
  * monitor feature provided by Zend Server.
  * 
  * @author Wojciech Galanciak, 2012
  */
 public class ZendMonitor extends ZendConnection {
 
 	/**
 	 * Predefined filters.
 	 */
 	public enum Filter {
 
 		ALL_OPEN_EVENTS("All Open Events"),
 
 		ALL_EVENTS("All Events"),
 
 		PERFORMANCE_ISSUES("Performance Issues"),
 
 		CRITICAL_ERRORS("Critical Errors");
 
 		private String name;
 
 		private Filter(String name) {
 			this.name = name;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 	}
 
 	private String targetId;
 
 	public ZendMonitor(String targetId) {
 		super();
 		this.targetId = targetId;
 	}
 
 	public ZendMonitor(String targetId, IMappingLoader mappingLoader) {
 		super(mappingLoader);
 		this.targetId = targetId;
 	}
 
 	public ZendMonitor(String targetId, ITargetLoader loader) {
 		super(loader);
 		this.targetId = targetId;
 	}
 
 	public ZendMonitor(String targetId, ITargetLoader loader,
 			IMappingLoader mappingLoader) {
 		super(loader, mappingLoader);
 		this.targetId = targetId;
 	}
 
 	/**
 	 * Provides list of issues by using {@link Filter#ALL_EVENTS} filter.
 	 * 
 	 * @return list of issues
 	 */
 	public List<IZendIssue> getAllIssues() {
 		try {
 			List<Issue> issues = doGetIssues(Filter.ALL_EVENTS);
 			if (issues != null) {
 				return ZendIssue.create(issues, this);
 			}
 		} catch (WebApiException e) {
 			String message = MessageFormat.format(
 					"Error during retrieving all issues from '{0}'", targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Retrieving All Issues", message, e));
 			log.error(message + "':");
 			log.error("\tpossible error: " + e.getMessage());
 		} catch (MalformedURLException e) {
 			String message = MessageFormat.format(
 					"Error during retrieving all issues from '{0}'", targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Retrieving All Issues", message, e));
 			log.error(e);
 		}
 		return null;
 	}
 
 	/**
 	 * Provides issue instance based on specified id.
 	 * 
 	 * @return concrete issue
 	 */
 	public IZendIssue get(int issueId) {
 		try {
 			List<Issue> issues = doGetIssues(Filter.ALL_EVENTS);
 			if (issues != null) {
 				for (Issue issue : issues) {
 					if (issue.getId() == issueId) {
 						return new ZendIssue(issue, this);
 					}
 				}
 			}
 		} catch (WebApiException e) {
 			String message = MessageFormat.format(
 					"Error during retrieving issue with id {0}", issueId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Retrieving Issue", message, e));
 			log.error(message + "':");
 			log.error("\tpossible error: " + e.getMessage());
 		} catch (MalformedURLException e) {
 			String message = MessageFormat.format(
 					"Error during retrieving issue with id {0}", issueId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Retrieving Issue", message, e));
 			log.error(e);
 		}
 		return null;
 	}
 
 	/**
 	 * Provides list of issues by using {@link Filter#ALL_OPEN_EVENTS} filter.
 	 * 
 	 * @return list of issues
 	 */
 	public List<IZendIssue> getOpenIssues() {
 		try {
 			List<Issue> issues = doGetIssues(Filter.ALL_OPEN_EVENTS);
 			if (issues != null) {
 				return ZendIssue.create(issues, this);
 			}
 		} catch (WebApiException e) {
 			String message = MessageFormat.format(
 					"Error during retrieving all open issues from '{0}'",
 					targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Retrieving All Open Issues", message, e));
 			log.error(message + "':");
 			log.error("\tpossible error: " + e.getMessage());
 		} catch (MalformedURLException e) {
 			String message = MessageFormat.format(
 					"Error during retrieving all open issues from '{0}'",
 					targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Retrieving All Open Issues", message, e));
 			log.error(e);
 		}
 		return null;
 	}
 
 	/**
 	 * Provides list of issues by using specified filter.
 	 * 
 	 * @return list of issues
 	 */
 	public List<IZendIssue> getIssues(Filter filter, int offset) {
 		try {
 			List<Issue> issues = doGetIssues(filter, offset);
 			if (issues != null) {
 				return ZendIssue.create(issues, this);
 			}
 		} catch (WebApiException e) {
 			String message = MessageFormat.format(
 					"Error during retrieving all open issues from '{0}'",
 					targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Retrieving All Open Issues", message, e));
 			log.error(message + "':");
 			log.error("\tpossible error: " + e.getMessage());
 		} catch (MalformedURLException e) {
 			String message = MessageFormat.format(
 					"Error during retrieving all open issues from '{0}'",
 					targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Retrieving All Open Issues", message, e));
 			log.error(e);
 		}
 		return null;
 	}
 
 	public List<IZendIssue> getIssues(Filter filter, long lastTime,
 			IZendTarget target) {
 		List<IZendIssue> result = new ArrayList<IZendIssue>();
 		int offset = 0;
 		try {
 			WebApiClient client = getClient();
 			while (true) {
 				IssueList list = client.monitorGetIssuesListPredefinedFilter(
 						filter.getName(), 20, offset, "id", "DESC");
				if (list != null) {
 					List<IZendIssue> issues = ZendIssue.create(
 							list.getIssues(), this);
 					if (issues != null && issues.size() > 0) {
 						for (IZendIssue issue : issues) {
 							Date lastDate = getTime(issue.getIssue()
 									.getLastOccurance(), target);
 							if (lastDate != null) {
 								long time = lastDate.getTime();
 								if (time > lastTime) {
 									result.add(issue);
 								} else {
 									return result;
 								}
 							}
 						}
 						offset += 20;
 					} else {
 						break;
 					}
 				} else {
 					break;
 				}
 			}
 		} catch (WebApiException e) {
 			String message = MessageFormat.format(
 					"Error during retrieving open issues from '{0}'", targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Retrieving Open Issues", message, e));
 			log.error(message + "':");
 			log.error("\tpossible error: " + e.getMessage());
 		} catch (MalformedURLException e) {
 			String message = MessageFormat.format(
 					"Error during retrieving open issues from '{0}'", targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Retrieving Open Issues", message, e));
 			log.error(e);
 		}
 		return result;
 	}
 
 	/**
 	 * Provides list of issues by using {@link Filter#CRITICAL_ERRORS} filter.
 	 * 
 	 * @return list of issues
 	 */
 	public List<IZendIssue> getCriticalErrors() {
 		try {
 			List<Issue> issues = doGetIssues(Filter.CRITICAL_ERRORS);
 			if (issues != null) {
 				return ZendIssue.create(issues, this);
 			}
 		} catch (WebApiException e) {
 			String message = MessageFormat.format(
 					"Error during retrieving critical errors from '{0}'",
 					targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Retrieving Critical Errors", message, e));
 			log.error(message + "':");
 			log.error("\tpossible error: " + e.getMessage());
 		} catch (MalformedURLException e) {
 			String message = MessageFormat.format(
 					"Error during retrieving critical errors from '{0}'",
 					targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Retrieving Critical Errors", message, e));
 			log.error(e);
 		}
 		return null;
 	}
 
 	/**
 	 * Provides list of issues by using {@link Filter#PERFORMANCE_ISSUES}
 	 * filter.
 	 * 
 	 * @return list of issues
 	 */
 	public List<IZendIssue> getPerformanceIssues() {
 		try {
 			List<Issue> issues = doGetIssues(Filter.PERFORMANCE_ISSUES);
 			if (issues != null) {
 				return ZendIssue.create(issues, this);
 			}
 		} catch (WebApiException e) {
 			String message = MessageFormat.format(
 					"Error during retrieving performance issues from '{0}'",
 					targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Retrieving Performance Issues", message, e));
 			log.error(message + "':");
 			log.error("\tpossible error: " + e.getMessage());
 		} catch (MalformedURLException e) {
 			String message = MessageFormat.format(
 					"Error during retrieving performance issues from '{0}'",
 					targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Retrieving Performance Issues", message, e));
 			log.error(e);
 		}
 		return null;
 	}
 
 	public long getLastEventTime(List<IZendIssue> issues, IZendTarget target) {
 		if (issues != null && issues.size() > 0) {
 			Date lastDate = getTime(issues.get(issues.size() - 1).getIssue()
 					.getLastOccurance(), target);
 			if (lastDate != null) {
 				return lastDate.getTime();
 			}
 		}
 		return -1;
 	}
 
 	public WebApiClient getClient() throws MalformedURLException {
 		return getClient(targetId);
 	}
 
 	public Date getTime(String time, IZendTarget target) {
 		SimpleDateFormat formatter = null;
 		if (ZendServerVersion
 				.byName(target.getProperty(IZendTarget.SERVER_VERSION))
 				.getName().startsWith("6")) { //$NON-NLS-1$
 			int index = time.indexOf("T"); //$NON-NLS-1$
 			String prefix = time.substring(0, index);
 			String suffix = time.substring(index + 1, time.length());
 			time = prefix + ' ' + suffix;
 			index = time.lastIndexOf('+');
 			if (index == -1) {
 				index = time.lastIndexOf('-');
 			}
 			time = time.substring(0, index);
 			formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
 		} else {
 			formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm"); //$NON-NLS-1$
 		}
 		Date date = null;
 		try {
 			date = formatter.parse(time);
 		} catch (ParseException e) {
 			return null;
 		}
 		return date;
 	}
 
 	private List<Issue> doGetIssues(Filter filter)
 			throws MalformedURLException, WebApiException {
 		List<Issue> result = new ArrayList<Issue>();
 		int offset = 0;
 		while (true) {
 			List<Issue> list = doGetIssues(filter, offset);
 			if (list != null) {
 				result.addAll(list);
 				offset += list.size();
 			} else {
 				return result;
 			}
 		}
 	}
 
 	private List<Issue> doGetIssues(Filter filter, int offset)
 			throws MalformedURLException, WebApiException {
 		WebApiClient client = getClient();
 		while (true) {
 			IssueList list = client.monitorGetIssuesListPredefinedFilter(
 					filter.getName(), 100, offset, "id", "ASC");
 			if (list != null) {
 				return list.getIssues();
 			}
 		}
 	}
 
 }
