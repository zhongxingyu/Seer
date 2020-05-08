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
 package org.openmrs.module.auditlog.web.dwr;
 
 import java.lang.reflect.Field;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.GlobalProperty;
 import org.openmrs.Obs;
 import org.openmrs.OpenmrsMetadata;
 import org.openmrs.OpenmrsObject;
 import org.openmrs.Patient;
 import org.openmrs.Person;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.auditlog.AuditLog;
 import org.openmrs.module.auditlog.AuditLog.Action;
 import org.openmrs.module.auditlog.api.AuditLogService;
 import org.openmrs.module.auditlog.util.AuditLogConstants;
 import org.openmrs.module.auditlog.util.AuditLogUtil;
 import org.openmrs.util.Reflect;
 
 /**
  * Processes DWR calls for the module
  */
 public class DWRAuditLogService {
 	
 	protected final Log log = LogFactory.getLog(getClass());
 	
 	private AuditLogService service;
 	
 	private AuditLogService getService() {
 		if (service == null)
 			service = Context.getService(AuditLogService.class);
 		return service;
 	}
 	
 	/**
 	 * Gets the {@link AuditLogDetails} for the auditlog with the specified uuid
 	 * 
 	 * @param auditLogUuid
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public AuditLogDetails getAuditLogDetails(String auditLogUuid) {
 		if (StringUtils.isNotBlank(auditLogUuid)) {
 			AuditLog auditLog = getService().getObjectByUuid(AuditLog.class, auditLogUuid);
 			if (auditLog != null) {
 				String displayString = "";
 				boolean objectExists = false;
 				Integer objectId = null;
 				Map<String, String[]> propertyNameChangesMap = null;
 				if (!auditLog.getAction().equals(Action.DELETED)) {
 					Class<? extends OpenmrsObject> clazz;
 					try {
 						clazz = (Class<? extends OpenmrsObject>) Context.loadClass(auditLog.getClassName());
 						OpenmrsObject obj = getService().getObjectByUuid(clazz, auditLog.getObjectUuid());
 						if (obj != null) {
 							objectExists = true;
 							//some objects don't support this method e.g GlobalProperties
 							if (!GlobalProperty.class.isAssignableFrom(obj.getClass())) {
 								try {
 									objectId = obj.getId();
 								}
 								catch (UnsupportedOperationException e) {
 									//ignore
 								}
 								displayString = getDisplayString(obj, false);
 							} else {
 								displayString += ((GlobalProperty) obj).getProperty();
 							}
 						}
 						
 						if (auditLog.getAction().equals(Action.UPDATED) && auditLog.getChanges().size() > 0) {
 							propertyNameChangesMap = new HashMap<String, String[]>();
 							for (Map.Entry<String, String[]> entry : auditLog.getChanges().entrySet()) {
 								String propertyName = entry.getKey();
 								String newValueDisplay = "";
 								String preValueDisplay = "";
 								String newValue = null;
 								String previousValue = null;
 								if (!ArrayUtils.isEmpty(entry.getValue())) {
 									newValue = entry.getValue()[0];
 									if (entry.getValue().length > 0)
 										previousValue = entry.getValue()[1];
 									if (StringUtils.isNotBlank(newValue) || StringUtils.isNotBlank(previousValue)) {
 										Field field = AuditLogUtil.getField(clazz, propertyName);
 										//This can be null if the auditlog was created and then 
 										//later upgraded to a version where the field was removed
 										if (field != null) {
 											if (Date.class.isAssignableFrom(field.getType())) {
 												if (newValue != null) {
 													try {
 														newValueDisplay += Context.getDateFormat().format(
 														    new SimpleDateFormat(AuditLogConstants.DATE_FORMAT)
 														            .parse(newValue));
 													}
 													catch (ParseException e) {
 														log.warn(e.getMessage());
 														newValueDisplay = newValue;
 													}
 												}
 												if (previousValue != null) {
 													try {
 														preValueDisplay += Context.getDateFormat().format(
 														    new SimpleDateFormat(AuditLogConstants.DATE_FORMAT)
 														            .parse(previousValue));
 													}
 													catch (Exception e) {
 														log.warn(e.getMessage());
 														preValueDisplay = previousValue;
 													}
 												}
 											} else {
 												if (StringUtils.isNotBlank(newValue)
 												        && (newValue.startsWith(AuditLogConstants.UUID_LABEL) || newValue
 												                .startsWith(AuditLogConstants.ID_LABEL))) {
 													newValueDisplay += getPropertyDisplayString(clazz, propertyName,
 													    field.getType(), newValue);
 												} else if (newValue != null) {
 													newValueDisplay = newValue;
 												}
 												
 												if (StringUtils.isNotBlank(previousValue)
 												        && (previousValue.startsWith(AuditLogConstants.UUID_LABEL) || previousValue
 												                .startsWith(AuditLogConstants.ID_LABEL))) {
 													preValueDisplay += getPropertyDisplayString(clazz, propertyName,
 													    field.getType(), previousValue);
 												} else if (previousValue != null) {
 													preValueDisplay = previousValue;
 												}
 											}
 										}
 									}
 								}
 								
 								if (StringUtils.isBlank(preValueDisplay))
 									preValueDisplay = previousValue;
 								if (preValueDisplay == null)
 									preValueDisplay = "";
 								if (StringUtils.isBlank(newValueDisplay))
 									newValueDisplay = newValue;
 								if (newValueDisplay == null)
 									newValueDisplay = "";
 								
 								propertyNameChangesMap.put(propertyName, new String[] { newValueDisplay, preValueDisplay });
 							}
 						}
 					}
 					catch (ClassNotFoundException e) {
 						log.error("Cannot log class:" + auditLog.getClassName());
 					}
 				}
 				
 				AuditLogDetails details = new AuditLogDetails(displayString, auditLog.getObjectUuid(),
 				        auditLog.getClassName(), auditLog.getAction().name(), objectId, objectExists, propertyNameChangesMap);
 				if (!auditLog.getChildAuditLogs().isEmpty()) {
 					List<AuditLogDetails> childDetails = new ArrayList<AuditLogDetails>();
 					for (AuditLog childLog : auditLog.getChildAuditLogs()) {
 						childDetails.add(new AuditLogDetails(null, childLog.getUuid(), childLog.getSimpleClassname(), childLog
 						        .getAction().name(), null, false, null));
 					}
 					details.setChildAuditLogDetails(childDetails);
 				}
 				
 				return details;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets the display string for a property
 	 * 
 	 * @param owningType
 	 * @param propertyName
 	 * @param propertyValue
 	 * @return the display text
 	 */
 	private String getPropertyDisplayString(Class<?> owningType, String propertyName, Class<?> propertyType,
 	                                        String propertyValue) {
 		String displayString = "";
 		try {
 			Object actualObject = null;
 			if (Reflect.isCollection(propertyType)) {
 				//TODO this not to fail if the primary key was a String e.g for privileges and had a ',' in it
 				String[] uuidsOrIds = StringUtils.split(propertyValue, ",");
 				List<Object> items = new ArrayList<Object>();
 				List<String> unmatchedUuidsOrIds = new ArrayList<String>();
 				for (String currUuidOrStr : uuidsOrIds) {
 					Object item = null;
 					currUuidOrStr = currUuidOrStr.trim();
 					Class<?> itemType = AuditLogUtil.getCollectionElementType(owningType, propertyName);
 					if (currUuidOrStr.startsWith(AuditLogConstants.UUID_LABEL)) {
 						currUuidOrStr = currUuidOrStr.substring(currUuidOrStr.indexOf(AuditLogConstants.UUID_LABEL)
 						        + AuditLogConstants.UUID_LABEL.length());
 						item = getService().getObjectByUuid(itemType, currUuidOrStr);
 					} else {
 						currUuidOrStr = currUuidOrStr.substring(currUuidOrStr.indexOf(AuditLogConstants.ID_LABEL)
 						        + AuditLogConstants.ID_LABEL.length());
 						try {
 							item = getService().getObjectById(itemType, Integer.valueOf(currUuidOrStr));
 						}
 						catch (NumberFormatException nfe) {
 							//ignore
 						}
 					}
 					
 					if (item != null)
 						items.add(item);
 					else
 						unmatchedUuidsOrIds.add(currUuidOrStr);
 				}
 				
 				StringBuilder sb = new StringBuilder("<ul class='" + AuditLogConstants.MODULE_ID + "_collection_property'>");
 				for (Object o1 : items) {
 					sb.append("<li class='" + AuditLogConstants.MODULE_ID + "_collection_item'>"
 					        + getDisplayString(o1, true) + "</li>");
 				}
 				for (String str : unmatchedUuidsOrIds) {
 					sb.append("<li class='" + AuditLogConstants.MODULE_ID + "_collection_item "
 					        + AuditLogConstants.MODULE_ID + "_collection_item_unmatched'>" + str + "</li>");
 				}
 				sb.append("</ul>");
 				displayString += sb.toString();
 			} else {
 				if (propertyValue.startsWith(AuditLogConstants.UUID_LABEL)) {
 					propertyValue = propertyValue.substring(propertyValue.indexOf(AuditLogConstants.UUID_LABEL)
 					        + AuditLogConstants.UUID_LABEL.length());
 					actualObject = getService().getObjectByUuid(propertyType, propertyValue);
 				} else {
 					propertyValue = propertyValue.substring(propertyValue.indexOf(AuditLogConstants.ID_LABEL)
 					        + AuditLogConstants.ID_LABEL.length());
 					actualObject = getService().getObjectById(propertyType, Integer.valueOf(propertyValue));
 				}
 				
 				if (actualObject != null) {
 					displayString = getDisplayString(actualObject, true);
 				}
 			}
 		}
 		catch (Exception e) {
 			log.warn("Error:", e);
 		}
 		
 		return displayString;
 	}
 	
 	/**
 	 * Generates the display text for the specified object
 	 * 
 	 * @param obj
 	 * @return the display text
 	 */
 	private String getDisplayString(Object obj, boolean includeUuidAndId) {
 		String displayString = "";
 		if (OpenmrsMetadata.class.isAssignableFrom(obj.getClass())) {
 			OpenmrsMetadata metadataObj = (OpenmrsMetadata) obj;
 			if (StringUtils.isNotBlank(metadataObj.getName()))
 				displayString += metadataObj.getName();
 		} else if (Concept.class.isAssignableFrom(obj.getClass())) {
 			Concept concept = (Concept) obj;
 			displayString += ((concept.getName() != null) ? concept.getName().getName() : "");
 		} else if (Person.class.isAssignableFrom(obj.getClass())) {
			Person person = (Person) obj;
 			displayString += ((person.getPersonName() != null) ? person.getPersonName().getFullName() : "");
 		} else if (Obs.class.isAssignableFrom(obj.getClass())) {
 			Obs obs = (Obs) obj;
 			if (obs.getConcept() != null) {
 				if (obs.getConcept().getName() != null)
 					displayString += obs.getConcept().getName().getName();
 			}
 			
 			displayString += obs.getValueAsString(Context.getLocale());
 		} else {
 			displayString += obj.toString();
 		}
 		
 		if (includeUuidAndId && OpenmrsObject.class.isAssignableFrom(obj.getClass())) {
 			OpenmrsObject openmrsObj = (OpenmrsObject) obj;
 			String id = "";
 			try {
 				if (openmrsObj.getId() != null)
 					id = " [" + openmrsObj.getId() + "]";
 			}
 			catch (UnsupportedOperationException e) {
 				//ignore
 			}
 			if (StringUtils.isBlank(displayString)) {
 				displayString = openmrsObj.getUuid() + id;
 			} else {
 				displayString = displayString + " - " + openmrsObj.getUuid() + id;
 			}
 		}
 		return displayString;
 	}
 }
