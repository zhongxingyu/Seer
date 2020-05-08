 /**********************************************************************************
  *
  * $URL: https://source.sakaiproject.org/contrib/etudes/melete/trunk/melete-hbm/src/java/org/etudes/component/app/melete/Bookmark.java $
  * $Id: Bookmark.java 60573 2009-05-19 20:17:20Z mallika@etudes.org $  
  ***********************************************************************************
  *
  * Copyright (c) 2010 Etudes, Inc.
  *
  * Portions completed before September 1, 2008 Copyright (c) 2004, 2005, 2006, 2007, 2008 Foothill College, ETUDES Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  *
  **********************************************************************************/
 package org.etudes.component.app.melete;
 
 import java.io.Serializable;
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 import org.etudes.api.app.melete.BookmarkObjService;
 
 /** @author Hibernate CodeGenerator */
 public class Bookmark implements Serializable, BookmarkObjService {
 
 	private int bookmarkId;
 	
 	/** nullable persistent field */
     private int sectionId;
     
     private String userId;
 
     private String siteId;
     
     private org.etudes.component.app.melete.Section section;
     
     private String title;
 
     private String notes;
     
     private Boolean lastVisited;   
     
     private boolean sectionVisibleFlag;
     
     public Bookmark()
     {
     	this.sectionId = 0;
     	this.userId = null;
     	this.siteId = null;
     	this.section = null;
     	this.title = null;
     	this.notes = null;
     	this.lastVisited = null;
     }
 	
 
 	/* (non-Javadoc)
 	 * @see org.etudes.component.app.melete.BookmarkObjService#getBookmarkId()
 	 */
 	public int getBookmarkId() {
 		return bookmarkId;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.etudes.component.app.melete.BookmarkObjService#setBookmarkId(int)
 	 */
 	public void setBookmarkId(int bookmarkId) {
 		this.bookmarkId = bookmarkId;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.etudes.component.app.melete.BookmarkObjService#getSectionId()
 	 */
 	public int getSectionId() {
 	     return this.sectionId;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.etudes.component.app.melete.BookmarkObjService#setSectionId(int)
 	 */
 	public void setSectionId(int sectionId) {
 	     this.sectionId = sectionId;
 	}
 	    
 	/* (non-Javadoc)
 	 * @see org.etudes.component.app.melete.BookmarkObjService#getUserId()
 	 */
 	public String getUserId() {
 		return userId;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.etudes.component.app.melete.BookmarkObjService#setUserId(java.lang.String)
 	 */
 	public void setUserId(String userId) {
 		this.userId = userId;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.etudes.component.app.melete.BookmarkObjService#getSiteId()
 	 */
 	public String getSiteId() {
 		return siteId;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.etudes.component.app.melete.BookmarkObjService#setSiteId(java.lang.String)
 	 */
 	public void setSiteId(String siteId) {
 		this.siteId = siteId;
 	}
 
 	/**
 	 * @return Returns the section.
 	 */
 	public org.etudes.api.app.melete.SectionObjService getSection() {
 		return section;
 	}
 	/**
 	 * @param section The section to set.
 	 */
 	public void setSection(
 			org.etudes.api.app.melete.SectionObjService section) {
 		this.section = (Section)section;
 	}	
 	
 	/* (non-Javadoc)
 	 * @see org.etudes.component.app.melete.BookmarkObjService#getTitle()
 	 */	
 	 public String getTitle() {
 	        return this.title;
 	    }
 
 	 /* (non-Javadoc)
 		 * @see org.etudes.component.app.melete.BookmarkObjService#setTitle(java.lang.String)
 		 */
 	 public void setTitle(String title) {
 	        this.title = title;
 	    }	
 
 	/* (non-Javadoc)
 	 * @see org.etudes.component.app.melete.BookmarkObjService#getNotes()
 	 */
 	public String getNotes() {
 		return notes;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.etudes.component.app.melete.BookmarkObjService#setNotes(java.lang.String)
 	 */
 	public void setNotes(String notes) {
 		this.notes = notes;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.etudes.component.app.melete.BookmarkObjService#getLastVisited()
 	 */
 	public Boolean getLastVisited() {
 		return lastVisited;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.etudes.component.app.melete.BookmarkObjService#setLastVisited(java.lang.Boolean)
 	 */
 	public void setLastVisited(Boolean lastVisited) {
 		this.lastVisited = lastVisited;
 	}
 
 	public boolean isSectionVisibleFlag()
 	{
 		return sectionVisibleFlag;
 	}
 	
 	public void setSectionVisibleFlag(boolean sectionVisibleFlag)
 	{
 		this.sectionVisibleFlag = sectionVisibleFlag;
 	}
 	
 }
