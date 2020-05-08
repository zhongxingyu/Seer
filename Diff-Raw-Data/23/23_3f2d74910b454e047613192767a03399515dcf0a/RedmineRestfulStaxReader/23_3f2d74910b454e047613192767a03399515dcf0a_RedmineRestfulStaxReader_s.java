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
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 import org.svenk.redmine.core.accesscontrol.internal.RedmineAcl;
 import org.svenk.redmine.core.client.container.Version;
 import org.svenk.redmine.core.client.container.Version.Plugin;
 import org.svenk.redmine.core.client.container.Version.Redmine;
 import org.svenk.redmine.core.exception.RedmineErrorException;
 import org.svenk.redmine.core.exception.RedmineException;
 import org.svenk.redmine.core.exception.RedmineInputParserException;
 import org.svenk.redmine.core.model.RedmineActivity;
 import org.svenk.redmine.core.model.RedmineAttachment;
 import org.svenk.redmine.core.model.RedmineCustomField;
 import org.svenk.redmine.core.model.RedmineCustomValue;
 import org.svenk.redmine.core.model.RedmineIssueCategory;
 import org.svenk.redmine.core.model.RedmineMember;
 import org.svenk.redmine.core.model.RedminePriority;
 import org.svenk.redmine.core.model.RedmineProject;
 import org.svenk.redmine.core.model.RedmineStoredQuery;
 import org.svenk.redmine.core.model.RedmineTicket;
 import org.svenk.redmine.core.model.RedmineTicketJournal;
 import org.svenk.redmine.core.model.RedmineTicketRelation;
 import org.svenk.redmine.core.model.RedmineTicketStatus;
 import org.svenk.redmine.core.model.RedmineTimeEntry;
 import org.svenk.redmine.core.model.RedmineTracker;
 import org.svenk.redmine.core.model.RedmineVersion;
 import org.svenk.redmine.core.model.RedmineTicket.Key;
 import org.svenk.redmine.core.util.RedmineUtil;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
 public class RedmineRestfulStaxReader {
 	
 	public final static  String NS_API_25 = "http://redmin-mylyncon.sf.net/schemas/WS-API-2.5";
 	
 	private final static String NS_PREFIX = null; 
 	
 	private SAXParserFactory parserFactory;
 	
 	private XMLInputFactory inputFactory;
 	
 	public RedmineRestfulStaxReader() {
 //		inputFactory = org.codehaus.stax2.XMLInputFactory2.newInstance("com.ctc.wstx.osgi.InputFactoryProviderImpl", this.getClass().getClassLoader());
 		inputFactory = new com.ctc.wstx.osgi.InputFactoryProviderImpl().createInputFactory();
 	}
 	
 	public RedmineTicket readTicket(InputStream in) throws RedmineException {
 		RedmineTicket ticket = null;
 
 		try {
 			XMLStreamReader reader = inputFactory.createXMLStreamReader(in);
 			reader.nextTag();
 			ticket = readCurrentTagAsTicket(reader);
 		} catch (XMLStreamException e) {
 			throw new RedmineErrorException("An error ocurred during the parsing of ticket", e);
 		}
 
 		return ticket;
 	}
 	
 	public List<RedmineTicket> readTickets(InputStream in) throws RedmineException {
 		List<RedmineTicket> tickets = new ArrayList<RedmineTicket>();
 		try {
 			XMLStreamReader reader = inputFactory.createXMLStreamReader(in);
 			reader.nextTag();
 			
 			RedmineTicket ticket = null;
 			while(reader.nextTag()==XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("issue")) {
 				ticket = readCurrentTagAsTicket(reader);
 				if (ticket!=null) {
 					tickets.add(ticket);
 				}
 			}
 		} catch (XMLStreamException e) {
 			throw new RedmineErrorException("An error ocurred during the parsing of tickets", e);
 		}
 		
 		return tickets;
 	}
 
 	public List<Integer> readUpdatedTickets(InputStream in) throws RedmineException {
 		List<Integer> list = new ArrayList<Integer>();
 
 		try {
 			XMLStreamReader reader = inputFactory.createXMLStreamReader(in);
 			
 			reader.nextTag();//updatedIssues
 			String[] values = reader.getElementText().split(" ");
 			for (String val : values) {
 				val = val.trim();
 				if (val.length()>0) {
 					list.add(Integer.valueOf(val));
 				}
 			}
 			
 		} catch (XMLStreamException e) {
 			throw new RedmineErrorException("An error ocurred during the parsing of updated tickets", e);
 		}
 
 		return list;
 	}
 
 	public List<RedmineTicketStatus> readTicketStatuses(InputStream in) throws RedmineException {
 		List<RedmineTicketStatus> list = new ArrayList<RedmineTicketStatus>();
 		
 		try {
 			XMLStreamReader reader = inputFactory.createXMLStreamReader(in);
 			
 			reader.nextTag();//issueStatuses
 			while(reader.nextTag()==XMLStreamConstants.START_ELEMENT) {
 				int id = Integer.parseInt(reader.getAttributeValue(NS_PREFIX, "id"));
 				
 				reader.nextTag();//name
 				String name = reader.getElementText().trim();
 
 				RedmineTicketStatus status = new RedmineTicketStatus(name, id);
 
 				reader.nextTag();//position, unused
 				Integer.parseInt(reader.getElementText());
 
 				reader.nextTag();//closed
 				status.setClosed(Boolean.parseBoolean(reader.getElementText()));
 
 				reader.nextTag();//default
 				status.setDefaultStatus(Boolean.parseBoolean(reader.getElementText()));
 
 				list.add(status);
 				
 				reader.nextTag();//issueStatus End-Tag
 			}
 			
 		} catch (XMLStreamException e) {
 			throw new RedmineErrorException("An error ocurred during the parsing of status", e);
 		}
 
 		return list;
 	}
 
 	public List<RedminePriority> readPriorities(InputStream in) throws RedmineException {
 		List<RedminePriority> list = new ArrayList<RedminePriority>();
 		
 		try {
 			XMLStreamReader reader = inputFactory.createXMLStreamReader(in);
 			
 			reader.nextTag();//priorities
 			while(reader.nextTag()==XMLStreamConstants.START_ELEMENT) {
 				int id = Integer.parseInt(reader.getAttributeValue(NS_PREFIX, "id"));
 				
 				reader.nextTag();//name
 				String name = reader.getElementText().trim();
 
 				reader.nextTag();//position
 				int position = Integer.parseInt(reader.getElementText());
 
 				RedminePriority priority = new RedminePriority(name, id, position);
 
 				reader.nextTag();//default
 				priority.setDefaultPriority(Boolean.parseBoolean(reader.getElementText()));
 
 				list.add(priority);
 				
 				reader.nextTag();//priority End-Tag
 			}
 			
 		} catch (XMLStreamException e) {
 			throw new RedmineErrorException("An error ocurred during the parsing of priorities", e);
 		}
 
 		return list;
 	}
 
 	public List<RedmineActivity> readActivities(InputStream in) throws RedmineException {
 		List<RedmineActivity> list = new ArrayList<RedmineActivity>();
 		
 		try {
 			XMLStreamReader reader = inputFactory.createXMLStreamReader(in);
 			
 			reader.nextTag();//priorities
 			while(reader.nextTag()==XMLStreamConstants.START_ELEMENT) {
 				int id = Integer.parseInt(reader.getAttributeValue(NS_PREFIX, "id"));
 				
 				reader.nextTag();//name
 				String name = reader.getElementText().trim();
 				
 				reader.nextTag();//position
 				int position = Integer.parseInt(reader.getElementText());
 				
 				RedmineActivity activity = new RedmineActivity(name, id, position);
 				
 				reader.nextTag();//default
 				activity.setDefaultPriority(Boolean.parseBoolean(reader.getElementText()));
 				
 				list.add(activity);
 				
 				reader.nextTag();//priority End-Tag
 			}
 			
 		} catch (XMLStreamException e) {
 			throw new RedmineErrorException("An error ocurred during the parsing of activities", e);
 		}
 		
 		return list;
 	}
 
 	public List<RedmineCustomField> readCustomFields(InputStream in) throws RedmineException {
 		List<RedmineCustomField> list = new ArrayList<RedmineCustomField>();
 		
 		try {
 			XMLStreamReader reader = inputFactory.createXMLStreamReader(in);
 			
 			reader.nextTag();//customFields
 			while(reader.nextTag()==XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("customField")) {
 				RedmineCustomField customField = readCurrentTagAsCustomField(reader);
 				if(customField!=null) {
 					list.add(customField);
 				}
 			}
 		} catch (XMLStreamException e) {
 			throw new RedmineErrorException("An error ocurred during the parsing of customfields", e);
 		}
 		
 		return list;
 	}
 
 	public List<RedmineProjectData> readProjects(InputStream in) throws RedmineException {
 		List<RedmineProjectData> projects = new ArrayList<RedmineProjectData>();
 		try {
 			XMLStreamReader reader = inputFactory.createXMLStreamReader(in);
 			reader.nextTag();
 			
 			RedmineProjectData projectData = null;
 			while(reader.nextTag()==XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("project")) {
 				projectData = readCurrentTagAsProject(reader);
 				if (projectData!=null) {
 					projects.add(projectData);
 				}
 			}
 		} catch (XMLStreamException e) {
 			throw new RedmineErrorException("An error ocurred during the parsing of projects", e);
 		}
 		
 		return projects;
 	}
 
 	public Version readVersion(InputStream in) throws RedmineException {
 		
 		Version version = null;
 		
 		try {
 			XMLStreamReader reader = inputFactory.createXMLStreamReader(in);
 
 			reader.nextTag(); //version
 			version = new Version();
 			
 			reader.nextTag(); //plugin
 			version.plugin = Plugin.fromString(reader.getElementText().trim());
 			
 			reader.nextTag(); //redmine
 			version.redmine = Redmine.fromString(reader.getElementText().trim());
 
 		} catch (XMLStreamException e) {
 			throw new RedmineErrorException("An error ocurred during the parsing of versions", e);
 		}
 
 		return version;
 	}
 	
 	protected XMLReader newReader() throws SAXException, ParserConfigurationException {
 		if (parserFactory==null) {
 			parserFactory = SAXParserFactory.newInstance();
 			parserFactory.setNamespaceAware(false);
 		}
 		return parserFactory.newSAXParser().getXMLReader();
 	}
 
 	protected RedmineProjectData readCurrentTagAsProject(XMLStreamReader reader) throws RedmineInputParserException, XMLStreamException {
 		RedmineProjectData data = null;
 		try {
 			int id = Integer.parseInt(reader.getAttributeValue(NS_PREFIX, "id"));
 			
 			reader.nextTag();
 			RedmineProject project = new RedmineProject(reader.getElementText(), id);
 			
 			//skip identifier
 			reader.nextTag();
 			reader.getElementText();
 			
 			reader.nextTag();
 			project.setIssueEditAllowed(Boolean.parseBoolean(reader.getElementText()));
 			
 			data = new RedmineProjectData(project);
 
 			while(reader.nextTag()==XMLStreamConstants.START_ELEMENT) {
 				if (reader.getLocalName().equals("trackers")) {
 					while(reader.nextTag()==XMLStreamConstants.START_ELEMENT) {
 						RedmineTracker tracker = readCurrentTagAsTracker(reader);
 						if (tracker!=null) {
 							data.trackers.add(tracker);
 						}
 					}
 				} else if (reader.getLocalName().equals("versions")) {
 					while(reader.nextTag()==XMLStreamConstants.START_ELEMENT) {
 						RedmineVersion version = readCurrentTagAsVersion(reader);
 						if (version!=null) {
 							data.versions.add(version);
 						}
 					}
 				} else if (reader.getLocalName().equals("members")) {
 					while(reader.nextTag()==XMLStreamConstants.START_ELEMENT) {
 						RedmineMember member = readCurrentTagAsMember(reader);
 						if (member!=null) {
 							data.members.add(member);
 						}
 					}
 				} else if (reader.getLocalName().equals("issueCategories")) {
 					while(reader.nextTag()==XMLStreamConstants.START_ELEMENT) {
 						RedmineIssueCategory category = readCurrentTagAsCategory(reader);
 						if (category!=null) {
 							data.categorys.add(category);
 						}
 					}
 				} else if (reader.getLocalName().equals("issueCustomFields")) {
 					while(reader.nextTag()==XMLStreamConstants.START_ELEMENT) {
 						RedmineCustomField field = readCurrentTagAsCustomField(reader);
 						if (field!=null) {
 							data.customTicketFields.add(field);
 						}
 					}
 				} else if (reader.getLocalName().equals("queries")) {
 					while(reader.nextTag()==XMLStreamConstants.START_ELEMENT) {
 						RedmineStoredQuery query = readCurrentTagAsQuery(reader);
 						if (query!=null) {
 							data.storedQueries.add(query);
 						}
 					}
 				} else {
 					//catch/skip unknown Tags
 					skipToEndTag(reader.getLocalName(), reader);
 				}
 			}
 		} finally {
 			if (!(reader.getEventType()==XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("project"))) {
 				skipToEndTag("project", reader);
 			}
 		}
reader.nextTag();
reader.nextTag();
 
 		return data;
 	}
 
 	protected RedmineTicket readCurrentTagAsTicket(XMLStreamReader reader) throws XMLStreamException {
 		RedmineTicket ticket = null;
 		try {
 			ticket = new RedmineTicket(Integer.parseInt(reader.getAttributeValue(NS_PREFIX, "id")));
 
 			while(reader.nextTag()==XMLStreamConstants.START_ELEMENT) {
 				Key key = Key.fromTagName(reader.getLocalName());
 				if (key==null) {
 					if (reader.getLocalName().equals("availableStatus")) {
 						List<Integer> idList = ticket.getAvailableStatusList();
 						for (String id : reader.getElementText().split("\\s+")) {
 							id = id.trim();
 							if (id.length()>0) {
 								idList.add(Integer.valueOf(id));
 							}
 						}
 					} else if (reader.getLocalName().equals("customValues")) {
 						while(reader.nextTag()==XMLStreamConstants.START_ELEMENT) {
 							try {
 								Integer cfId = Integer.valueOf(reader.getAttributeValue(NS_PREFIX, "customFieldId"));
 								String value = reader.getElementText();
 								ticket.putCustomFieldValue(cfId, value);
 							} catch (NumberFormatException e) {
 								reader.getElementText(); //Skip Content
 							}
 						}
 						
 					} else if (reader.getLocalName().equals("journals")) {
 						while(reader.nextTag()==XMLStreamConstants.START_ELEMENT) {
 							RedmineTicketJournal journal = readCurrentTagAsJournal(reader);
 							if (journal!=null) {
 								ticket.addJournal(journal);
 							}
 						}
 					} else if (reader.getLocalName().equals("attachments")) {
 						while(reader.nextTag()==XMLStreamConstants.START_ELEMENT) {
 							RedmineAttachment attachment = readCurrentTagAsAttachment(reader);
 							if (attachment!=null) {
 								ticket.addAttachment(attachment);
 							}
 						}
 					} else if (reader.getLocalName().equals("issueRelations")) {
 						while(reader.nextTag()==XMLStreamConstants.START_ELEMENT) {
 							RedmineTicketRelation relation = readCurrentTagAsRelation(reader);
 							if (relation!=null) {
 								ticket.addRelation(relation);
 							}
 						}
 					} else if (reader.getLocalName().equals("timeEntries")) {
						ticket.putRight(RedmineAcl.TIMEENTRY_VIEW, Boolean.parseBoolean(reader.getAttributeValue(NS_PREFIX, "viewAllowed")));
 						ticket.putRight(RedmineAcl.TIMEENTRY_NEW, Boolean.parseBoolean(reader.getAttributeValue(NS_PREFIX, "newAllowed")));
 						
						reader.nextTag();//sum
						ticket.putBuiltinValue(Key.TIME_ENTRY_TOTAL, reader.getElementText());

						while(reader.nextTag()==XMLStreamConstants.START_ELEMENT) {
							RedmineTimeEntry timeEntry = readCurrentTagAsTimeEntry(reader);
							if (timeEntry!=null) {
								ticket.addTimeEntry(timeEntry);
 							}
 						}
 					} else {
 						//catch/skip unknown Tags
 						skipToEndTag(reader.getLocalName(), reader);
 					}
 				} else {
 					switch (key) {
 					case CREATED_ON : {
 						long unixtime = Long.parseLong(reader.getAttributeValue(NS_PREFIX, "unixtime"));
 						ticket.setCreated(new Date(unixtime*1000L));
 						reader.getElementText(); //Skip Content
 						break;
 					}
 					case UPDATED_ON : {
 						long unixtime = Long.parseLong(reader.getAttributeValue(NS_PREFIX, "unixtime"));
 						ticket.setLastChanged(new Date(unixtime*1000L));
 						reader.getElementText(); //Skip Content
 						break;
 					}
 					default : ticket.putBuiltinValue(key, reader.getElementText());
 					}
 					
 				}
 				
 			}
 		} catch (NumberFormatException e) {
 			ticket = null;
 		} finally {
 			if (!(reader.getEventType()==XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("issue"))) {
 				skipToEndTag("issue", reader);
 			}
 		}
 		return ticket;
 	}
 	
 	protected RedmineTicketJournal readCurrentTagAsJournal(XMLStreamReader reader) throws XMLStreamException {
 		RedmineTicketJournal journal = new RedmineTicketJournal();
 		try {
 			journal.setId(Integer.parseInt(reader.getAttributeValue(NS_PREFIX, "id")));
 
 			reader.nextTag();
 			journal.setAuthorName(reader.getElementText());
 			
 			reader.nextTag();
 			long unixtime = Long.parseLong(reader.getAttributeValue(NS_PREFIX, "unixtime"));
 			journal.setCreated(new Date(unixtime*1000L));
 			reader.getElementText(); //Skip Content
 			
 			reader.nextTag();
 			journal.setNotes(reader.getElementText());
 			
 			reader.nextTag();
 			journal.setEditable(Boolean.parseBoolean(reader.getElementText()));
 		} catch (NumberFormatException e) {
 			journal = null;
 		} finally {
 			skipToEndTag("journal", reader);
 		}
 		return journal;
 	}
 
 	protected RedmineAttachment readCurrentTagAsAttachment(XMLStreamReader reader) throws XMLStreamException {
 		RedmineAttachment attachment = null;
 		try {
 			attachment = new RedmineAttachment(Integer.parseInt(reader.getAttributeValue(NS_PREFIX, "id")));
 			
 			reader.nextTag();
 			attachment.setAuthorName(reader.getElementText());
 
 			reader.nextTag();
 			long unixtime = Long.parseLong(reader.getAttributeValue(NS_PREFIX, "unixtime"));
 			attachment.setCreated(new Date(unixtime*1000L));
 			reader.getElementText(); //Skip Content
 
 			reader.nextTag();
 			attachment.setFilename(reader.getElementText());
 
 			reader.nextTag();
 			attachment.setFilesize(Integer.parseInt(reader.getElementText()));
 
 			reader.nextTag();
 			attachment.setDigest(reader.getElementText());
 
 			reader.nextTag();
 			attachment.setContentType(reader.getElementText());
 
 			reader.nextTag();
 			attachment.setDescription(reader.getElementText());
 
 		} finally {
 			skipToEndTag("attachment", reader);
 		}
 		return attachment;
 	}
 
 	protected RedmineTicketRelation readCurrentTagAsRelation(XMLStreamReader reader) throws XMLStreamException {
 		RedmineTicketRelation relation = null;
 		try {
 			int id = Integer.parseInt(reader.getAttributeValue(NS_PREFIX, "id"));
 			
 			reader.nextTag();
 			int fromId = Integer.parseInt(reader.getElementText());
 
 			reader.nextTag();
 			int toId = Integer.parseInt(reader.getElementText());
 
 			reader.nextTag();
 			String type = reader.getElementText();
 
 			relation = new RedmineTicketRelation(id, fromId, toId, type);
 		} finally {
 			skipToEndTag("issueRelation", reader);
 		}
 		return relation;
 	}
 
 	protected RedmineTimeEntry readCurrentTagAsTimeEntry(XMLStreamReader reader) throws XMLStreamException {
 		RedmineTimeEntry timeEntry = null;
 		try {
 			int id = Integer.parseInt(reader.getAttributeValue(NS_PREFIX, "id"));
 
 			reader.nextTag();
 			float hours = Float.parseFloat(reader.getElementText());
 
 			reader.nextTag();
 			int activityId = Integer.parseInt(reader.getElementText());
 
 			reader.nextTag();
 			int userId = Integer.parseInt(reader.getElementText());
 
 			timeEntry = new RedmineTimeEntry();
 			timeEntry.setId(id);
 			timeEntry.setHours(hours);
 			timeEntry.setActivityId(activityId);
 			timeEntry.setUserId(userId);
 
 			reader.nextTag();
 			timeEntry.setSpentOn(RedmineUtil.parseDate(reader.getElementText()));
 
 			reader.nextTag();
 			timeEntry.setComments(reader.getElementText());
 
 			reader.nextTag();//customValues
 			while(reader.nextTag()==XMLStreamConstants.START_ELEMENT) {
 				RedmineCustomValue customValue = readCurrentTagAsCustomValue(reader);
 				if (customValue!=null) {
 					timeEntry.addCustomValue(customValue);
 				}
 			}
 			
 		} finally {
 			skipToEndTag("timeEntry", reader);
 		}
 		return timeEntry;
 	}
 
 	protected RedmineCustomValue readCurrentTagAsCustomValue(XMLStreamReader reader) throws XMLStreamException {
 		RedmineCustomValue customValue = null;
 		try {
 			int customFieldId = Integer.parseInt(reader.getAttributeValue(NS_PREFIX, "customFieldId"));
 			String value = reader.getElementText();
 			
 			customValue = new RedmineCustomValue();
 			customValue.setCustomFieldId(customFieldId);
 			customValue.setValue(value);
 		} finally {
 //			skipToEndTag("customValue", reader);
 		}
 		return customValue;
 	}
 
 	protected RedmineTracker readCurrentTagAsTracker(XMLStreamReader reader) throws XMLStreamException {
 		RedmineTracker tracker = null;
 		try {
 			int id = Integer.parseInt(reader.getAttributeValue(NS_PREFIX, "id"));
 			reader.nextTag();
 			tracker = new RedmineTracker(reader.getElementText(), id);
 			
 			//skip position
 		} finally {
 			skipToEndTag("tracker", reader);
 		}
 		return tracker;
 	}
 
 	protected RedmineVersion readCurrentTagAsVersion(XMLStreamReader reader) throws XMLStreamException {
 		RedmineVersion version = null;
 		try {
 			int id = Integer.parseInt(reader.getAttributeValue(NS_PREFIX, "id"));
 			reader.nextTag();
 			version = new RedmineVersion(reader.getElementText(), id);
 			
 			//skip completed
 		} finally {
 			skipToEndTag("version", reader);
 		}
 		return version;
 	}
 
 	protected RedmineMember readCurrentTagAsMember(XMLStreamReader reader) throws XMLStreamException {
 		RedmineMember member = null;
 		try {
 			int id = Integer.parseInt(reader.getAttributeValue(NS_PREFIX, "id"));
 			reader.nextTag();
 			String name = reader.getElementText();
 			reader.nextTag();
 			member = new RedmineMember(name, id, Boolean.parseBoolean(reader.getElementText()));
 			
 			//skip emailAddress
 			//skip emailNotification
 		} finally {
 			skipToEndTag("member", reader);
 		}
 		return member;
 	}
 
 	protected RedmineIssueCategory readCurrentTagAsCategory(XMLStreamReader reader) throws XMLStreamException {
 		RedmineIssueCategory category = null;
 		try {
 			int id = Integer.parseInt(reader.getAttributeValue(NS_PREFIX, "id"));
 			reader.nextTag();
 			category = new RedmineIssueCategory(reader.getElementText(), id);
 		} finally {
 			skipToEndTag("issueCategory", reader);
 		}
 		return category;
 	}
 
 	protected RedmineStoredQuery readCurrentTagAsQuery(XMLStreamReader reader) throws XMLStreamException {
 		RedmineStoredQuery query = null;
 		try {
 			int id = Integer.parseInt(reader.getAttributeValue(NS_PREFIX, "id"));
 			reader.nextTag();
 			query = new RedmineStoredQuery(reader.getElementText(), id);
 		} finally {
 			skipToEndTag("query", reader);
 		}
 		return query;
 	}
 
 	protected void skipToEndTag(String tagname, XMLStreamReader reader) throws XMLStreamException {
 		while(!(reader.isEndElement() && reader.getLocalName().equals(tagname))) {
 			if(reader.isStartElement() && reader.hasText()) {
 				reader.getElementText();
 			}
 			reader.next();
 		}
 	}
 
 	private enum CustomFieldTag {name, type, fieldFormat, minLength, maxLength, regexp, possibleValues, defaultValue, required, filter, trackers;
 		private static CustomFieldTag fromTagName(String tagName) {
 			for (CustomFieldTag tag : CustomFieldTag.values()) {
 				if (tag.name().equals(tagName)) {
 					return tag;
 				}
 			}
 			return null;
 		}
 	};
 
 		
 	private RedmineCustomField readCurrentTagAsCustomField(XMLStreamReader reader) throws XMLStreamException, RedmineInputParserException{
 		String tagName = reader.getLocalName();
 		RedmineCustomField field = null;
 		
 		try {
 			int id = Integer.parseInt(reader.getAttributeValue(NS_PREFIX, "id"));
 			String name = null;
 			RedmineCustomField.CustomType customType = null;
 			while(!(reader.nextTag()==XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals(tagName))) {
 				CustomFieldTag tag = CustomFieldTag.fromTagName(reader.getLocalName());
 				if (tag==null) {
 					throw new RedmineInputParserException(Messages.RedmineRestfulStaxReader_PARSING_OF_CUSTOMFIELD_FAILED_UNKNOWN_TAG + reader.getLocalName());
 				}
 
 				switch(tag) {
 					case name : name = reader.getElementText().trim(); break;
 					case type : 
 						customType = RedmineCustomField.CustomType.fromString(reader.getElementText().trim());
 						if (customType==null) {
 							//unsed CustomType
 							skipToEndTag(tagName, reader);
 							return null;
 						}
 						break;
 					case fieldFormat : 
 						field = customType==null
 							? new RedmineCustomField(id, reader.getElementText().trim())
 							: new RedmineCustomField(id, reader.getElementText().trim(), customType);
 						field.setName(name);
 						break;
 					case minLength : field.setMin(Integer.parseInt(reader.getElementText().trim())); break;
 					case maxLength : field.setMax(Integer.parseInt(reader.getElementText().trim())); break;
 					case regexp : field.setValidationRegex(reader.getElementText().trim()); break;
 					case defaultValue : field.setDefaultValue(reader.getElementText().trim()); break;
 					case required : field.setRequired(Boolean.parseBoolean(reader.getElementText().trim())); break;
 					case filter : field.setSupportFilter(Boolean.parseBoolean(reader.getElementText().trim()));break;
 					case possibleValues :
 						List<String> possibleValues = new ArrayList<String>();
 						while(reader.nextTag()==XMLStreamConstants.START_ELEMENT) {
 							possibleValues.add(reader.getElementText());
 						}
 						field.setListValues(possibleValues.toArray(new String[possibleValues.size()]));
 						break;
 					case trackers : 
 						String[] trackerIdVals = reader.getElementText().trim().split("\\s+");
 						int[] trackerIds = new int[trackerIdVals.length];
 						for (int i=trackerIdVals.length-1; i>=0; i--) {
 							trackerIds[i] = Integer.parseInt(trackerIdVals[i]);
 						}
 						field.setTrackerId(trackerIds);
 						break;
 					default :
 				}
 			}
 			
 		} catch (NumberFormatException e) {
 			field = null;
 			throw new RedmineInputParserException(Messages.RedmineRestfulStaxReader_PARSING_OF_CUSTOMFIELD_FAILED, e);
 		} catch (XMLStreamException e) {
 			field = null;
 			throw new RedmineInputParserException(Messages.RedmineRestfulStaxReader_PARSING_OF_CUSTOMFIELD_FAILED, e);
 		} finally {
 			skipToEndTag(tagName, reader);
 		} 
 		
 		return field;
 	}
 }
