 /*******************************************************************************
  *
  * Redmine-Mylyn-Connector
  * 
  * This implementation is on the basis of the implementations of Trac and 
  * Bugzilla emerged and contains parts of source code from these projects.
  * The corresponding copyright notice follows below of this.
  * Copyright (C) 2008  Sven Krzyzak and others
  *  
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the 
  * Free Software Foundation; either version 3 of the License, 
  * or (at your option) any later version.
  *  
  * This program is distributed in the hope that it will be useful, but 
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
  * more details.
  *  
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, see <http://www.gnu.org/licenses/>.
  *  
  *******************************************************************************/
 /*******************************************************************************
  * Copyright (c) 2004, 2007 Mylyn project committers and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package org.svenk.redmine.core.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
 import org.eclipse.mylyn.tasks.core.data.TaskData;
 import org.svenk.redmine.core.RedmineAttribute;
 import org.svenk.redmine.core.RedmineClientData;
 import org.svenk.redmine.core.RedmineProjectData;
 
 public class RedmineTicket {
 
 	public enum Key {
 		ID("id", true),
 //		ADDITIONAL_INFO("additional_information"),
 		ASSIGNED_TO("assigned_to"),
 		CATEGORY("category"), 
 		CREATED_ON("created_on", true), 
 		DESCRIPTION("description"), 
 //		ETA("eta"), 
 		UPDATED_ON("updated_on", true), 
 		PRIORITY("priority"),
 		PROJECT("project", true), 
 //		PROJECTION("projection"),
 //		RELATIONSHIPS("relationships"),
 		AUTHOR("autor", true),  
 //		REPRODUCIBILITY("reproducibility"), 
 //		RESOLUTION("resolution"), 
 		TRACKER("tracker", true), 
 		STATUS("status"), 
 //		STEPS_TO_REPRODUCE("steps_to_reproduce"),
 		SUBJECT("subject"),
 		VERSION("version"), 
 		COMMENT("notes"), 
 //		VIEW_STATE("view_state"), 
 		;
 
 //		public static Key fromKey(String name) {
 //			for (Key key : Key.values()) {
 //				if (key.getKey().equals(name)) {
 //					return key;
 //				}
 //			}
 //			return null;
 //		}
 
 		private String key;
 		private boolean readonly;
 
 		Key(String key) {
 			this(key, false);
 		}
 
 		Key(String key, boolean readonly) {
 			this.key = key;
 			this.readonly = readonly;
 		}
 		
 		@Override
 		public String toString() {
 			return key;
 		}
 
 		public String getKey() {
 			return key;
 		}
 		
 		public boolean isReadonly() {
 			return readonly;
 		}
 		
 		public static Key fromString(String string) {
 			for (Key key : Key.values()) {
 				if (key.toString().equals(string)) {
 					return key;
 				}
 			}
 			return null;
 		}
 	}
 
 	private int id;
 	private Date lastChanged;
 	private Date created;
 	private Map<Key, String> valueByKey = new HashMap<Key, String>();
 	private Map<Integer, String> valueByCustomFieldId = new HashMap<Integer, String>();
 	private List<RedmineTicketJournal> journals;
 	private List<RedmineAttachment> attachments;
 	private List<RedmineTicketStatus> statuses;
 	
 
 	public RedmineTicket() {
 	}
 
 	public RedmineTicket(int id) {
 		this.id = id;
 	}
 	
 	public void putBuiltinValue(Key key, String value) {
 		if (value!=null) {
 			valueByKey.put(key, value);
 		}
 	}
 
 	public void putBuiltinValue(String name, String value) {
 		Key key = Key.fromString(name);
 		if (key != null) {
 			putBuiltinValue(key, value);
 		}
 	}
 	
 	public void putBuiltinValue(Key key, Integer value) {
 		if (value!=null) {
 			putBuiltinValue(key, value.toString());
 		}
 	}
 	
 	public void putCustomFieldValue(Integer customFieldId, String value) {
 		valueByCustomFieldId.put(customFieldId, value);
 	}
 	
 	public Map<String, String> getValues() {
 		Map<String, String> result = new HashMap<String, String>();
 		for (Key key : valueByKey.keySet()) {
 			result.put(key.getKey(), valueByKey.get(key));
 		}
 		return result;
 	}
 	
 	public Map<Integer, String> getCustomValues() {
 		return Collections.unmodifiableMap(valueByCustomFieldId);
 	}
 
 	public Date getLastChanged() {
 		return lastChanged;
 	}
 
 	public void setLastChanged(Date lastChanged) {
 		this.lastChanged = lastChanged;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public Date getCreated() {
 		return created;
 	}
 
 	public void setCreated(Date created) {
 		this.created = created;
 	}
 
 	public String getValue(Key key) {
 		return valueByKey.get(key);
 	}
 	
 	public int getIntValue(Key key) {
 		return Integer.parseInt(getValue(key));
 	}
 	
 	public void addJournal(RedmineTicketJournal journal) {
 		if (journals==null) {
 			journals = new ArrayList<RedmineTicketJournal>();
 		}
 		journals.add(journal);
 	}
 	
 	public RedmineTicketJournal[] getJournals() {
 		return journals==null ? null : journals.toArray(new RedmineTicketJournal[0]);
 	}
 
 	public void addAttachment(RedmineAttachment attachment) {
 		if (attachments==null) {
 			attachments = new ArrayList<RedmineAttachment>();
 		}
 		attachments.add(attachment);
 	}
 	
 	public RedmineAttachment[] getAttachments() {
 		return attachments==null ? null : attachments.toArray(new RedmineAttachment[0]);
 	}
 
 	public List<RedmineTicketStatus> getStatuses() {
 		return statuses==null ? null : Collections.unmodifiableList(statuses);
 	}
 
 	public void setStatuses(List<RedmineTicketStatus> statuses) {
 		this.statuses = statuses;
 	}
 	
 	public static RedmineTicket fromTaskData(TaskData taskData, RedmineClientData clientData) {
 		RedmineTicket ticket = taskData.getTaskId().equals("") 
 			? new RedmineTicket() 
 			: new RedmineTicket(Integer.parseInt(taskData.getTaskId()));
 		
 		Map<String, TaskAttribute> attributeValues = taskData.getRoot().getAttributes();
 		
 		//default attributes
 		for (RedmineAttribute redmineAttribute : RedmineAttribute.values()) {
 			if (redmineAttribute.isReadOnly()) {
 				continue;
 			}
 			
 			TaskAttribute taskAttribute = attributeValues.get(redmineAttribute.getRedmineKey());
 			if (taskAttribute != null) {
 				ticket.putBuiltinValue(redmineAttribute.getTicketKey(), taskAttribute.getValue());
 			}
 		}
 	
 		ticket.completeCustomFields(taskData, clientData);
 		
 		return ticket;
 	}
 	
 	private void completeCustomFields(TaskData taskData, RedmineClientData clientData) {
 		TaskAttribute rootAttribute = taskData.getRoot();
 		
 		TaskAttribute projAttr = rootAttribute.getMappedAttribute(RedmineAttribute.PROJECT.getRedmineKey());
 		RedmineProjectData projectData = clientData.getProjectFromName(projAttr.getValue());
 
 		int trackerId = Integer.parseInt(rootAttribute.getMappedAttribute(RedmineAttribute.TRACKER.getRedmineKey()).getValue());
 		List<RedmineCustomTicketField> ticketFields = projectData.getCustomTicketFields(trackerId); 
 		for (RedmineCustomTicketField customField :ticketFields) {
 			TaskAttribute taskAttribute = rootAttribute.getMappedAttribute(RedmineCustomTicketField.TASK_KEY_PREFIX + customField.getId());
 			if (taskAttribute != null) {
 				this.putCustomFieldValue(Integer.valueOf(customField.getId()), taskAttribute.getValue());
 			}
 		}
 	}
 }
