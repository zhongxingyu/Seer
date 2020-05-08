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
 import org.apache.log4j.Logger;
 
 /**
  *
  */
 public class RecentChange {
 
 	private static Logger logger = Logger.getLogger(RecentChange.class);
 	private Integer authorId = null;
 	private String authorName = null;
 	private String editComment = null;
 	private Timestamp editDate = null;
 	private int editType = -1;
 	private Integer previousTopicVersionId = null;
 	private int topicId = -1;
 	private String topicName = null;
 	private int topicVersionId = -1;
 	private String virtualWiki = null;
 
 	/**
 	 *
 	 */
 	public RecentChange() {
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
 	public boolean getDelete() {
 		return this.editType == TopicVersion.EDIT_DELETE;
 	}
 
 	/**
 	 *
 	 */
 	public String getEditComment() {
 		return this.editComment;
 	}
 
 	/**
 	 *
 	 */
 	public void setEditComment(String editComment) {
 		this.editComment = editComment;
 	}
 
 	/**
 	 *
 	 */
 	public Timestamp getEditDate() {
 		return this.editDate;
 	}
 
 	/**
 	 *
 	 */
 	public void setEditDate(Timestamp editDate) {
 		this.editDate = editDate;
 	}
 
 	/**
 	 *
 	 */
 	public int getEditType() {
 		return this.editType;
 	}
 
 	/**
 	 *
 	 */
 	public void setEditType(int editType) {
 		this.editType = editType;
 	}
 
 	/**
 	 *
 	 */
 	public boolean getMinor() {
 		return this.editType == TopicVersion.EDIT_MINOR;
 	}
 
 	/**
 	 *
 	 */
 	public boolean getMove() {
 		return this.editType == TopicVersion.EDIT_MOVE;
 	}
 
 	/**
 	 *
 	 */
 	public boolean getNormal() {
 		return this.editType == TopicVersion.EDIT_NORMAL;
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
 	public int getTopicId() {
 		return this.topicId;
 	}
 
 	/**
 	 *
 	 */
 	public void setTopicId(int topicId) {
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
 	public int getTopicVersionId() {
 		return this.topicVersionId;
 	}
 
 	/**
 	 *
 	 */
 	public void setTopicVersionId(int topicVersionId) {
 		this.topicVersionId = topicVersionId;
 	}
 
 	/**
 	 *
 	 */
 	public boolean getUndelete() {
 		return this.editType == TopicVersion.EDIT_UNDELETE;
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
 }
