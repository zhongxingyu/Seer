 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.auditlog;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.LinkedHashSet;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.User;
 import org.openmrs.util.OpenmrsConstants;
 
 /**
  * Encapsulates data for a single audit log entry
  */
 public class AuditLog implements Serializable {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private static final Log log = LogFactory.getLog(AuditLog.class);
 	
 	private String uuid = UUID.randomUUID().toString();
 	
 	private Integer auditLogId;
 	
 	//the fully qualified java class name of the create/updated/deleted object
 	private String className;
 	
 	//the uuid of the created/updated/deleted object
 	private String objectUuid;
 	
 	//the performed operation that which could be a create, update or delete
 	private Action action;
 	
 	private User user;
 	
 	private Date dateCreated;
 	
 	private String openmrsVersion;
 	
 	private AuditLog parentAuditLog;
 	
 	private Set<AuditLog> childAuditLogs;
 	
 	/**
	 * Used to store Json for field new and old values for udpated items or last properties values
 	 * of deleted items
 	 */
 	private String serializedData;
 	
 	public enum Action {
 		CREATED, UPDATED, DELETED
 		//, REVERTED
 	}
 	
 	/**
 	 * Default constructor
 	 */
 	public AuditLog() {
 		openmrsVersion = OpenmrsConstants.OPENMRS_VERSION_SHORT;
 	}
 	
 	/**
 	 * Convenience constructor
 	 * 
 	 * @param className the fully qualified classname of the Object type
 	 * @param objectUuid the id of the object
 	 * @param action the operation performed on the object
 	 * @param user the user that triggered the operation
 	 * @param dateCreated the date when the operation was done
 	 */
 	public AuditLog(String className, String objectUuid, Action action, User user, Date dateCreated) {
 		this();
 		this.className = className;
 		this.objectUuid = objectUuid;
 		this.action = action;
 		this.user = user;
 		this.dateCreated = dateCreated;
 	}
 	
 	/**
 	 * @return the auditLogId
 	 */
 	public Integer getAuditLogId() {
 		return auditLogId;
 	}
 	
 	/**
 	 * @param auditLogId the auditLogId to set
 	 */
 	public void setAuditLogId(Integer auditLogId) {
 		this.auditLogId = auditLogId;
 	}
 	
 	/**
 	 * @return the className
 	 */
 	public String getClassName() {
 		return className;
 	}
 	
 	/**
 	 * @param className the className to set
 	 */
 	public void setClassName(String className) {
 		this.className = className;
 	}
 	
 	/**
 	 * @return the objectUuid
 	 */
 	public String getObjectUuid() {
 		return objectUuid;
 	}
 	
 	/**
 	 * @param objectUuid the objectUuid to set
 	 */
 	public void setObjectUuid(String objectUuid) {
 		this.objectUuid = objectUuid;
 	}
 	
 	/**
 	 * @return the action
 	 */
 	public Action getAction() {
 		return action;
 	}
 	
 	/**
 	 * @param action the action to set
 	 */
 	public void setAction(Action action) {
 		this.action = action;
 	}
 	
 	/**
 	 * @return the user
 	 */
 	public User getUser() {
 		return user;
 	}
 	
 	/**
 	 * @param user the user to set
 	 */
 	public void setUser(User user) {
 		this.user = user;
 	}
 	
 	/**
 	 * @return the dateCreated
 	 */
 	public Date getDateCreated() {
 		return dateCreated;
 	}
 	
 	/**
 	 * @param dateCreated the dateCreated to set
 	 */
 	public void setDateCreated(Date dateCreated) {
 		this.dateCreated = dateCreated;
 	}
 	
 	/**
 	 * @return the openmrsVersion
 	 */
 	public String getOpenmrsVersion() {
 		return openmrsVersion;
 	}
 	
 	/**
 	 * @param openmrsVersion the openmrsVersion to set to
 	 */
 	public void setOpenmrsVersion(String openmrsVersion) {
 		this.openmrsVersion = openmrsVersion;
 	}
 	
 	/**
 	 * @return the uuid
 	 */
 	public String getUuid() {
 		return uuid;
 	}
 	
 	/**
 	 * @param uuid the uuid to set
 	 */
 	public void setUuid(String uuid) {
 		this.uuid = uuid;
 	}
 	
 	/**
 	 * @return the parentAuditLog
 	 */
 	public AuditLog getParentAuditLog() {
 		return parentAuditLog;
 	}
 	
 	/**
 	 * @param parentAuditLog the parentAuditLog to set
 	 */
 	public void setParentAuditLog(AuditLog parentAuditLog) {
 		this.parentAuditLog = parentAuditLog;
 	}
 	
 	/**
 	 * @return the childAuditLogs
 	 */
 	public Set<AuditLog> getChildAuditLogs() {
 		if (childAuditLogs == null)
 			childAuditLogs = new LinkedHashSet<AuditLog>();
 		
 		return childAuditLogs;
 	}
 	
 	/**
 	 * @param childAuditLogs the childAuditLogs to set
 	 */
 	public void setChildAuditLogs(Set<AuditLog> childAuditLogs) {
 		this.childAuditLogs = childAuditLogs;
 	}
 	
 	/**
 	 * @return the serializedData
 	 */
 	public String getSerializedData() {
 		return serializedData;
 	}
 	
 	/**
 	 * @param serializedData the serializedData to set
 	 */
 	public void setSerializedData(String serializedData) {
 		this.serializedData = serializedData;
 	}
 	
 	/**
 	 * Returns the simple forms of the classname property e.g 'Concept Name' will be returned for
 	 * ConceptName
 	 * 
 	 * @return the classname
 	 */
 	public String getSimpleClassname() {
 		String section = getClassName().substring(getClassName().lastIndexOf(".") + 1);
 		String[] sections = StringUtils.splitByCharacterTypeCamelCase(section);
 		for (int i = 0; i < sections.length; i++) {
 			sections[i] = StringUtils.capitalize(sections[i]);
 		}
 		
 		return StringUtils.join(sections, " ");
 	}
 	
 	/**
 	 * Adds the specified auditLog as a child
 	 * 
 	 * @param auditLog the AuditLog to add
 	 */
 	public void addChildAuditLog(AuditLog auditLog) {
 		if (auditLog == null)
 			return;
 		auditLog.setParentAuditLog(this);
 		getChildAuditLogs().add(auditLog);
 	}
 	
 	/**
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		return this == obj
 		        || (obj instanceof AuditLog && getUuid() != null && ((AuditLog) obj).getUuid().equals(this.getUuid()));
 		
 	}
 	
 	/**
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		if (getUuid() == null)
 			return super.hashCode();
 		return getUuid().hashCode();
 	}
 	
 	/**
 	 * @see Object#toString() ()
 	 */
 	@Override
 	public String toString() {
 		return action + " " + className + " " + objectUuid;
 	}
 	
 	public boolean hasChildLogs() {
 		return getChildAuditLogs().size() > 0;
 	}
 }
