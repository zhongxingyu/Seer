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
 package net.java.dev.cejug.classifieds.server.ejb3.entity;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 
 /**
  * @author $Author$
  * @version $Rev$ ($Date$)
  */
 @Entity
 @Table(name = "ADVERTISEMENT_TYPE")
 @NamedQuery(name = "selectFromAdvertisementTypeEntity", query = "SELECT type FROM AdvertisementTypeEntity type")
 public class AdvertisementTypeEntity extends AbstractEntity implements
 		Comparable<AdvertisementTypeEntity> {
 	@Column(name = "NAME", nullable = false)
 	private String name;
 
 	@Column(name = "DESCRIPTION", nullable = false)
 	private String description;
 
 	@Column(name = "TEXT_LENGTH", nullable = false)
 	private Integer textLength;
 
 	@Column(name = "MAX_ATTACHMENT_SIZE", nullable = false)
 	private Integer maxAttachmentSize;
 
 	public String getName() {
 
 		return name;
 	}
 
 	public void setName(String name) {
 
 		this.name = name;
 	}
 
 	public String getDescription() {
 
 		return description;
 	}
 
 	public void setDescription(String description) {
 
 		this.description = description;
 	}
 
 	public Integer getTextLength() {
 
 		return textLength;
 	}
 
 	public void setTextLength(Integer textLength) {
 
 		this.textLength = textLength;
 	}
 
 	public Integer getMaxAttachmentSize() {
 
 		return maxAttachmentSize;
 	}
 
 	public void setMaxAttachmentSize(Integer maxAttachmentSize) {
 
 		this.maxAttachmentSize = maxAttachmentSize;
 	}
 
 	@Override
 	public int compareTo(AdvertisementTypeEntity other) {
 		return getId() - other.getId();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		return (obj instanceof AdvertisementTypeEntity)
 				&& compareTo((AdvertisementTypeEntity) obj) == 0;
 	}
 
 	@Override
 	public int hashCode() {
		Integer id = getId();
		return id == null ? super.hashCode() : id;
 	}
 }
