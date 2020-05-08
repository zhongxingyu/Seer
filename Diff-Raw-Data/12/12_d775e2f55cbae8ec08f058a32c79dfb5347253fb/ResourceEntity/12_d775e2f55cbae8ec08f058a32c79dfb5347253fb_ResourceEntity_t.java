 /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  Copyright (C) 2008 CEJUG - Ceará Java Users Group
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
  
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
  
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  
  This file is part of the CEJUG-CLASSIFIEDS Project - an  open source classifieds system
  originally used by CEJUG - Ceará Java Users Group.
  The project is hosted https://cejug-classifieds.dev.java.net/
  
  You can contact us through the mail dev@cejug-classifieds.dev.java.net
  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
 package net.java.dev.cejug.classifieds.entity;
 
 import java.util.Calendar;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import net.java.dev.cejug_classifieds.metadata.common.AdvertisementCategory;
 
 /**
  * A resource can be an image, a MP3 sound or a Flash animation. Please check
  * the classifieds contract about the supported media types.
  * 
  * @author $Author: felipegaucho $
  * @version $Rev$ ($Date: 2008-09-03 19:58:27 +0200 (Wed, 03 Sep 2008) $)
  */
 @Entity
 @Table(name = "RESOURCE")
 public class ResourceEntity extends AbstractEntity<AdvertisementCategory> {
 	@Column(name = "URL", nullable = false)
 	private String url;
 
 	@Column(name = "DESCRIPTION", nullable = false)
 	private String description;
 
 	@Column(name = "CONTENT_TYPE", nullable = false)
 	private String contentType;
 
 	/**
 	 * Length in bytes of the resource content.
 	 */
 	@Column(name = "LENGTH", nullable = false)
 	private String length;
 
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(name = "UPDATED", nullable = false)
 	private Calendar updated;
 
 	public Calendar getUpdated() {
 		return updated;
 	}
 
 	public void setUpdated(Calendar updated) {
 		this.updated = updated;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	public String getLength() {
 		return length;
 	}
 
 	public void setLength(String length) {
 		this.length = length;
 	}
 
 	public String getContentType() {
 		return contentType;
 	}
 
 	public void setContentType(String contentType) {
 		this.contentType = contentType;
 	}
 
 	public String getName() {
 		return url;
 	}
 
 	public void setName(String name) {
 		this.url = name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 }
