 /*******************************************************************************
  * Copyright (c) 2004, 2007 Mylyn project committers and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Mylyn project committers
  *******************************************************************************/
 /*******************************************************************************
  * Copyright (c) 2008 Sven Krzyzak
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Sven Krzyzak - adapted Trac implementation for Redmine
  *******************************************************************************/
 package org.svenk.redmine.core.client;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.mylyn.commons.net.AbstractWebLocation;
 import org.eclipse.mylyn.tasks.core.TaskRepository;
 import org.svenk.redmine.core.client.container.Version;
 import org.svenk.redmine.core.client.container.Version.Release;
 import org.svenk.redmine.core.exception.RedmineException;
 import org.svenk.redmine.core.model.RedmineActivity;
 import org.svenk.redmine.core.model.RedmineCustomField;
 import org.svenk.redmine.core.model.RedminePriority;
 import org.svenk.redmine.core.model.RedmineTicket;
 import org.svenk.redmine.core.model.RedmineTicketStatus;
 
 
 public class RedmineRestfulClient extends AbstractRedmineClient {
 
 	protected final static String PLACEHOLDER = "_PARAM_";
 	
 	protected final static String PATH_GET_VERSION = "/mylyn/version";
 
 	protected final static String PATH_GET_PROJECTS = "/mylyn/projects";
 
 	protected final static String PATH_GET_PRIORITIES = "/mylyn/priorities";
 	
 	protected final static String PATH_GET_ISSUE_STATUS = "/mylyn/issuestatus";
 
 	protected final static String PATH_GET_ACTIVITIES = "/mylyn/activities";
 
 	protected final static String PATH_GET_CUSTOM_FIELDS = "/mylyn/customfields";
 	
 	protected final static String PATH_GET_TICKET = "/mylyn/issue/" + PLACEHOLDER;
 
 	protected final static String PATH_SEARCH_TICKETS = "/mylyn/" + PLACEHOLDER + "/search";
 	protected final static String PATH_SEARCH_TICKETS_QUERY_STRING = "query_string";
 	protected final static String PATH_SEARCH_TICKETS_QUERY_ID = "query_id";
 
 	protected final static String PATH_GET_CHANGED_TICKETS = "/mylyn/" + PLACEHOLDER + "/updatedsince";
 	protected final static String PATH_GET_CHANGED_TICKETS_PARAM = "unixtime";
 
 	private Version.Plugin vPlugin;
 	
 	private IRedmineResponseParser<Version> versionParser;
 
 	private IRedmineResponseParser<List<Integer>> updatedTicketsParser;
 
 	private IRedmineResponseParser<RedmineTicket> ticketParser;
 
 	private IRedmineResponseParser<List<RedmineTicket>> ticketsParser;
 
 	private IRedmineResponseParser<List<RedmineProjectData>> projectsParser;
 
 	private IRedmineResponseParser<List<RedminePriority>> prioritiesParser;
 	
 	private IRedmineResponseParser<List<RedmineTicketStatus>> ticketStatusParser;
 
 	private IRedmineResponseParser<List<RedmineActivity>> activitiesParser;
 
 	private IRedmineResponseParser<List<RedmineCustomField>> customFieldsParser;
 
 	public RedmineRestfulClient(AbstractWebLocation location, RedmineClientData clientData, TaskRepository repository) {
 		super(location, clientData, repository);
 		
 		createResponseParsers();
 	}
 
 	public void refreshRepositorySettings(TaskRepository repository) {
 		super.refreshRepositorySettings(repository);
 		if (!repository.getVersion().equals(TaskRepository.NO_VERSION_SPECIFIED)) {
 			vPlugin = Version.Plugin.fromString(repository.getVersion());
 		}
 	}
 
 	@Override
 	protected Version checkClientVersion(IProgressMonitor monitor) throws RedmineException {
 		GetMethod method = new GetMethod(PATH_GET_VERSION);
 		return executeMethod(method, versionParser, monitor);
 	}
 
 	public List<Integer> getChangedTicketId(Integer projectId, Date changedSince, IProgressMonitor monitor) throws RedmineException {
 		GetMethod method = new GetMethod(PATH_GET_CHANGED_TICKETS.replace(PLACEHOLDER, projectId.toString()));
 		method.setQueryString(new NameValuePair[]{new NameValuePair(PATH_GET_CHANGED_TICKETS_PARAM, ""+changedSince.getTime()/1000)});
 		
 		return executeMethod(method, updatedTicketsParser, monitor);
 	}
 
 	public RedmineClientData getClientData() {
 		return data;
 	}
 
 	public RedmineTicket getTicket(int id, IProgressMonitor monitor) throws RedmineException {
 		GetMethod method = new GetMethod(PATH_GET_TICKET.replace(PLACEHOLDER, ""+id));
 
 		RedmineTicket ticket = executeMethod(method, ticketParser, monitor);
 		if(ticket!=null) {
 			completeAvailableStatus(ticket, data);
 			return ticket;
 		}
 		
 		return null;
 	}
 
 	public boolean hasAttributes() {
 		return data.lastupdate!=0;
 	}
 
 	public void search(String searchParam, String projectId, String storedQueryId, List<RedmineTicket> tickets, IProgressMonitor monitor) throws RedmineException {
 		PostMethod method = new PostMethod(PATH_SEARCH_TICKETS.replace(PLACEHOLDER, projectId));
 		
 		if (storedQueryId==null || storedQueryId.equals("0")) {
 			method.setQueryString(new NameValuePair[]{new NameValuePair(PATH_SEARCH_TICKETS_QUERY_STRING, searchParam)});
 		} else {
 			method.setQueryString(new NameValuePair[]{new NameValuePair(PATH_SEARCH_TICKETS_QUERY_ID, storedQueryId)});
 		}
 
 		for (RedmineTicket redmineTicket :  executeMethod(method, ticketsParser, monitor)) {
 			completeAvailableStatus(redmineTicket, data);
 			tickets.add(redmineTicket);
 		}
 	}
 
 	@Override
 	public boolean supportStartDueDate() {
		return vPlugin!=null && vPlugin.compareTo(Release.WS_TWOSIX)>=0;
 	}
 
 	@Override
 	public boolean supportTimeEntries() {
		return vPlugin!=null && vPlugin.compareTo(Release.WS_TWOSIX)>=0;
 	}
 
 	public synchronized void updateAttributes(boolean force, IProgressMonitor monitor) throws RedmineException {
 		if (!force && hasAttributes()) {
 			return;
 		}
 
 		monitor.beginTask(Messages.RedmineRestfulClient_UPDATING_ATTRIBUTES, supportTimeEntries() ? 5 : 3);
 
 		GetMethod method = new GetMethod(PATH_GET_PROJECTS);
 		data.projects.clear();
 		data.projects.addAll(executeMethod(method, projectsParser, monitor));
 		monitor.worked(1);
 		if (monitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 
 		method = new GetMethod(PATH_GET_PRIORITIES);
 		data.priorities.clear();
 		data.priorities.addAll(executeMethod(method, prioritiesParser, monitor));
 		monitor.worked(1);
 		if (monitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 
 		method = new GetMethod(PATH_GET_ISSUE_STATUS);
 		data.statuses.clear();
 		data.statuses.addAll(executeMethod(method, ticketStatusParser, monitor));
 		monitor.worked(1);
 		if (monitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 
 		if (supportTimeEntries()) {
 			method = new GetMethod(PATH_GET_ACTIVITIES);
 			data.activities.clear();
 			data.activities.addAll(executeMethod(method, activitiesParser, monitor));
 			monitor.worked(1);
 			if (monitor.isCanceled()) {
 				throw new OperationCanceledException();
 			}
 			
 			method = new GetMethod(PATH_GET_CUSTOM_FIELDS);
 			data.customFields.clear();
 			data.customFields.addAll(executeMethod(method, customFieldsParser, monitor));
 			monitor.worked(1);
 			if (monitor.isCanceled()) {
 				throw new OperationCanceledException();
 			}
 		}
 			
 		data.lastupdate=new Date().getTime();
 	}
 	
 	//TODO nach Entfernung des XmlRpc Clients den Status überarbeiten 
 	public void completeAvailableStatus(RedmineTicket ticket, RedmineClientData clientData) {
 		List<Integer> idList = ticket.getAvailableStatusList();
 		List<RedmineTicketStatus> statuses = new ArrayList<RedmineTicketStatus>(idList==null ? 0 : idList.size()); 
 		for (Integer intval : idList) {
 			//TODO wenn status nicht verfügbar, clientdata aktualisieren
 			statuses.add(clientData.getStatus(intval));
 		}
 		ticket.setStatuses(statuses);
 	}
 	
 	private void createResponseParsers() {
 		final RedmineRestfulStaxReader reader = new RedmineRestfulStaxReader();
 
 		versionParser = new IRedmineResponseParser<Version>() {
 			public Version parseResponse(InputStream input, int sc) throws RedmineException {
 				return reader.readVersion(input);
 			}
 		}; 
 
 		updatedTicketsParser = new IRedmineResponseParser<List<Integer>>() {
 			public List<Integer> parseResponse(InputStream input, int sc) throws RedmineException {
 				return reader.readUpdatedTickets(input);
 			}
 		};
 
 		ticketParser = new IRedmineResponseParser<RedmineTicket>() {
 			public RedmineTicket parseResponse(InputStream input, int sc) throws RedmineException {
 				return reader.readTicket(input);
 			}
 		};
 		
 		ticketsParser = new IRedmineResponseParser<List<RedmineTicket>>() {
 			public List<RedmineTicket> parseResponse(InputStream input, int sc) throws RedmineException {
 				return reader.readTickets(input);
 			}
 		};
 
 		projectsParser = new IRedmineResponseParser<List<RedmineProjectData>>() {
 			public List<RedmineProjectData> parseResponse(InputStream input, int sc) throws RedmineException {
 				return reader.readProjects(input);
 			}
 		};
 
 		prioritiesParser = new IRedmineResponseParser<List<RedminePriority>>() {
 			public List<RedminePriority> parseResponse(InputStream input, int sc) throws RedmineException {
 				return reader.readPriorities(input);
 			}
 		};
 
 		ticketStatusParser = new IRedmineResponseParser<List<RedmineTicketStatus>>() {
 			public List<RedmineTicketStatus> parseResponse(InputStream input, int sc) throws RedmineException {
 				return reader.readTicketStatuses(input);
 			}
 		};
 
 		activitiesParser = new IRedmineResponseParser<List<RedmineActivity>>() {
 			public List<RedmineActivity> parseResponse(InputStream input, int sc) throws RedmineException {
 				return reader.readActivities(input);
 			}
 		};
 
 		customFieldsParser = new IRedmineResponseParser<List<RedmineCustomField>>() {
 			public List<RedmineCustomField> parseResponse(InputStream input, int sc) throws RedmineException {
 				return reader.readCustomFields(input);
 			}
 		};
 	}
 }
