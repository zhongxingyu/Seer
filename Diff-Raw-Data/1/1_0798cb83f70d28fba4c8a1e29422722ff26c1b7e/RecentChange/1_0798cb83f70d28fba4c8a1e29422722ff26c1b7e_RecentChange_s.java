 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.model;
 
 import java.sql.Timestamp;
 import java.util.List;
 import java.util.Locale;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * Provides an object representing a Wiki recent change.
  */
 public class RecentChange {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(RecentChange.class.getName());
 	private Integer authorId = null;
 	private String authorName = null;
 	private Integer charactersChanged = null;
 	private String changeComment = null;
 	private Timestamp changeDate = null;
 	private transient WikiMessage changeWikiMessage = null;
 	private Integer editType = null;
 	private Integer logType = null;
 	private List<String> params = null;
 	private Integer previousTopicVersionId = null;
 	private Integer topicId = null;
 	private String topicName = null;
 	private Integer topicVersionId = null;
 	private String virtualWiki = null;
 
 	/**
 	 *
 	 */
 	public RecentChange() {
 	}
 
 	/**
 	 *
 	 */
 	public static RecentChange initRecentChange(Topic topic, TopicVersion topicVersion, String authorName) {
 		RecentChange recentChange = new RecentChange();
 		recentChange.setTopicId(topic.getTopicId());
 		recentChange.setTopicName(topic.getName());
 		recentChange.setTopicVersionId(topicVersion.getTopicVersionId());
 		recentChange.setPreviousTopicVersionId(topicVersion.getPreviousTopicVersionId());
 		recentChange.setAuthorId(topicVersion.getAuthorId());
 		recentChange.setAuthorName(authorName);
 		recentChange.setCharactersChanged(topicVersion.getCharactersChanged());
 		recentChange.setChangeComment(topicVersion.getEditComment());
 		recentChange.setChangeDate(topicVersion.getEditDate());
 		recentChange.setEditType(topicVersion.getEditType());
 		recentChange.setVirtualWiki(topic.getVirtualWiki());
 		recentChange.setParamString(topicVersion.getVersionParamString());
 		recentChange.initChangeWikiMessageForVersion(topicVersion.getEditType(), topicVersion.getVersionParamString());
 		return recentChange;
 	}
 
 	/**
 	 *
 	 */
 	public static RecentChange initRecentChange(LogItem logItem) {
 		RecentChange recentChange = new RecentChange();
 		recentChange.setAuthorId(logItem.getUserId());
 		recentChange.setAuthorName(logItem.getUserDisplayName());
 		recentChange.setChangeComment(logItem.getLogComment());
 		recentChange.setChangeDate(logItem.getLogDate());
 		recentChange.setVirtualWiki(logItem.getVirtualWiki());
 		recentChange.setParamString(logItem.getLogParamString());
 		recentChange.initChangeWikiMessageForLog(logItem.getLogType(), logItem.getLogParamString());
 		return recentChange;
 	}
 
 	/**
 	 *
 	 */
 	public void initChangeWikiMessageForLog(int logType, String logParamString) {
 		String[] logParams = null;
 		if (!StringUtils.isBlank(logParamString)) {
 			logParams = logParamString.split("\\|");
 		}
 		if (logType == LogItem.LOG_TYPE_DELETE) {
 			this.setChangeWikiMessage(new WikiMessage("log.message.deletion", logParams));
 		} else if (logType == LogItem.LOG_TYPE_IMPORT) {
 			this.setChangeWikiMessage(new WikiMessage("log.message.import", logParams));
 		} else if (logType == LogItem.LOG_TYPE_MOVE) {
 			this.setChangeWikiMessage(new WikiMessage("log.message.move", logParams));
 		} else if (logType == LogItem.LOG_TYPE_PERMISSION) {
 			this.setChangeWikiMessage(new WikiMessage("log.message.permission", logParams));
 		} else if (logType == LogItem.LOG_TYPE_UPLOAD) {
 			this.setChangeWikiMessage(new WikiMessage("log.message.upload", logParams));
 		} else if (logType == LogItem.LOG_TYPE_USER_CREATION) {
 			this.setChangeWikiMessage(new WikiMessage("log.message.user"));
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void initChangeWikiMessageForVersion(int editType, String versionParamString) {
 		if (StringUtils.isBlank(versionParamString)) {
 			// older versions of JAMWiki did not have this field, so it may not always be populated as expected
 			return;
 		}
 		if (editType == TopicVersion.EDIT_MOVE) {
 			this.setChangeWikiMessage(new WikiMessage("log.message.move", versionParamString.split("\\|")));
 		}
 	}
 
 	/**
 	 *
 	 */
 	public Integer getAuthorId() {
 		return this.authorId;
 	}
 
 	/**
 	 *
 	 */
 	public void setAuthorId(Integer authorId) {
 		this.authorId = authorId;
 	}
 
 	/**
 	 *
 	 */
 	public String getAuthorName() {
 		return this.authorName;
 	}
 
 	/**
 	 *
 	 */
 	public void setAuthorName(String authorName) {
 		this.authorName = authorName;
 	}
 
 	/**
 	 *
 	 */
 	public String getChangeComment() {
 		return this.changeComment;
 	}
 
 	/**
 	 *
 	 */
 	public void setChangeComment(String changeComment) {
 		this.changeComment = changeComment;
 	}
 
 	/**
 	 *
 	 */
 	public Timestamp getChangeDate() {
 		return this.changeDate;
 	}
 
 	/**
 	 *
 	 */
 	public void setChangeDate(Timestamp changeDate) {
 		this.changeDate = changeDate;
 	}
 
 	/**
 	 *
 	 */
 	public String getChangeTypeNotification() {
 		StringBuffer changeTypeNotification = new StringBuffer();
 		if (this.previousTopicVersionId == null) {
 			changeTypeNotification.append('n');
 		}
 		if (this.editType == null) {
 			return "";
 		}
 		if (this.editType == TopicVersion.EDIT_MINOR) {
 			changeTypeNotification.append('m');
 		}
 		if (this.editType == TopicVersion.EDIT_DELETE) {
 			changeTypeNotification.append('d');
 		}
 		if (this.editType == TopicVersion.EDIT_UNDELETE) {
 			changeTypeNotification.append('u');
 		}
 		if (this.editType == TopicVersion.EDIT_IMPORT) {
 			changeTypeNotification.append('i');
 		}
 		return changeTypeNotification.toString();
 	}
 
 	/**
 	 * This field is a generated field used to return a <code>WikiMessage</code> object
 	 * that represents any auto-generated message information for the recent change entry,
 	 * such as "Topic A renamed to Topic B" when renaming a topic.
 	 */
 	public WikiMessage getChangeWikiMessage() {
 		return this.changeWikiMessage;
 	}
 
 	/**
 	 * This field is a generated field used to return a <code>WikiMessage</code> object
 	 * that represents any auto-generated message information for the recent change entry,
 	 * such as "Topic A renamed to Topic B" when renaming a topic.
 	 */
 	public void setChangeWikiMessage(WikiMessage changeWikiMessage) {
 		this.changeWikiMessage = changeWikiMessage;
 	}
 
 	/**
 	 *
 	 */
 	public Integer getCharactersChanged() {
 		return this.charactersChanged;
 	}
 
 	/**
 	 *
 	 */
 	public void setCharactersChanged(Integer charactersChanged) {
 		this.charactersChanged = charactersChanged;
 	}
 
 	/**
 	 *
 	 */
 	public Integer getEditType() {
 		return this.editType;
 	}
 
 	/**
 	 *
 	 */
 	public void setEditType(Integer editType) {
 		this.editType = editType;
 	}
 
 	/**
 	 *
 	 */
 	public Integer getLogType() {
 		return this.logType;
 	}
 
 	/**
 	 *
 	 */
 	public void setLogType(Integer logType) {
 		this.logType = logType;
 	}
 
 	/**
 	 *
 	 */
 	public List<String> getParams() {
 		return this.params;
 	}
 
 	/**
 	 *
 	 */
 	public void setParams(List<String> params) {
 		this.params = params;
 	}
 
 	/**
 	 * Utility method for converting the params to a pipe-delimited string.
 	 */
 	public String getParamString() {
 		return Utilities.listToDelimitedString(this.params, "|");
 	}
 
 	/**
 	 * Utility method for converting a params pipe-delimited string to a list.
 	 */
 	public void setParamString(String paramsString) {
 		this.setParams(Utilities.delimitedStringToList(paramsString, "|"));
 	}
 
 	/**
 	 *
 	 */
 	public Integer getPreviousTopicVersionId() {
 		return this.previousTopicVersionId;
 	}
 
 	/**
 	 *
 	 */
 	public void setPreviousTopicVersionId(Integer previousTopicVersionId) {
 		this.previousTopicVersionId = previousTopicVersionId;
 	}
 
 	/**
 	 *
 	 */
 	public Integer getTopicId() {
 		return this.topicId;
 	}
 
 	/**
 	 *
 	 */
 	public void setTopicId(Integer topicId) {
 		this.topicId = topicId;
 	}
 
 	/**
 	 *
 	 */
 	public String getTopicName() {
 		return this.topicName;
 	}
 
 	/**
 	 *
 	 */
 	public void setTopicName(String topicName) {
 		this.topicName = topicName;
 	}
 
 	/**
 	 *
 	 */
 	public Integer getTopicVersionId() {
 		return this.topicVersionId;
 	}
 
 	/**
 	 *
 	 */
 	public void setTopicVersionId(Integer topicVersionId) {
 		this.topicVersionId = topicVersionId;
 	}
 
 	/**
 	 *
 	 */
 	public String getVirtualWiki() {
 		return this.virtualWiki;
 	}
 
 	/**
 	 *
 	 */
 	public void setVirtualWiki(String virtualWiki) {
 		this.virtualWiki = virtualWiki;
 	}
 
 	/**
 	 *
 	 */
 	public boolean isDelete() {
 		if (this.editType != null && this.editType == TopicVersion.EDIT_DELETE) {
 			return true;
 		} else if (this.logType != null && this.logType == LogItem.LOG_TYPE_DELETE) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 *
 	 */
 	public boolean isImport() {
 		if (this.editType != null && this.editType == TopicVersion.EDIT_IMPORT) {
 			return true;
 		} else if (this.logType != null && this.logType == LogItem.LOG_TYPE_IMPORT) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 *
 	 */
 	public boolean getMinor() {
 		return (this.editType != null && this.editType == TopicVersion.EDIT_MINOR);
 	}
 
 	/**
 	 *
 	 */
 	public boolean isMove() {
 		if (this.editType != null && this.editType == TopicVersion.EDIT_MOVE) {
 			return true;
 		} else if (this.logType != null && this.logType == LogItem.LOG_TYPE_MOVE) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 *
 	 */
 	public boolean isPermission() {
 		return (this.logType != null && this.logType == LogItem.LOG_TYPE_PERMISSION);
 	}
 
 	/**
 	 *
 	 */
 	public boolean isNormal() {
 		return (this.editType != null && this.editType == TopicVersion.EDIT_NORMAL);
 	}
 
 	/**
 	 *
 	 */
 	public boolean isUndelete() {
 		return (this.editType != null && this.editType == TopicVersion.EDIT_UNDELETE);
 	}
 
 	/**
 	 *
 	 */
 	public boolean isUpload() {
 		return (this.logType != null && this.logType == LogItem.LOG_TYPE_UPLOAD);
 	}
 
 	/**
 	 *
 	 */
 	public boolean isUser() {
 		return (this.logType != null && this.logType == LogItem.LOG_TYPE_USER_CREATION);
 	}
 }
