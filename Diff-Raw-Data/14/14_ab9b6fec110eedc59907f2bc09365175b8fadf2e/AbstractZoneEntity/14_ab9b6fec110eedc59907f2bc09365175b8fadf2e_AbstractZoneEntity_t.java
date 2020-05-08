 /*
  * AbstractZoneEntity.java
  * 
  * Copyright (C) 2011
  * 
  * This file is part of Proyecto persistenceGeo
  * 
  * This software is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General public abstract License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option) any
  * later version.
  * 
  * This software is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General public abstract License for more
  * details.
  * 
  * You should have received a copy of the GNU General public abstract License along with
  * this library; if not, write to the Free Software Foundation, Inc., 51
  * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  * 
  * As a special exception, if you link this library with other files to produce
  * an executable, this library does not by itself cause the resulting executable
  * to be covered by the GNU General public abstract License. This exception does not
  * however invalidate any other reasons why the executable file might be covered
  * by the GNU General public abstract License.
  * 
  * Authors:: Moisés Arcos Santiago (mailto:marcos@emergya.com)
  */
 package com.emergya.persistenceGeo.metaModel;
 
import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Zone entity
  * 
  * @author <a href="mailto:marcos@emergya.com">marcos</a>
  * @author <a href="mailto:adiaz@emergya.com">adiaz</a>
  * 
  */
 @SuppressWarnings("rawtypes")
 public abstract class AbstractZoneEntity extends AbstractEntity {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -7702334870312919540L;
 
 	protected Long id;
 
 	protected String code;
 	protected String name;
 	protected String type;
 
 	protected String extension;
 	protected Date createDate;
 	protected Date updateDate;
 	protected List zoneList;
 	protected List folderList;
 	protected List authList;
 
 	/**
 	 * @return the id
 	 */
 	public abstract Long getId();
 
 	/**
 	 * @return the code
 	 */
 	public abstract String getCode();
 
 	/**
 	 * @return the name
 	 */
 	public abstract String getName();
 
 	/**
 	 * @return the type
 	 */
 	public abstract String getType();
 
 	/**
 	 * @return the extension
 	 */
 	public abstract String getExtension();
 
 	/**
 	 * @return the createDate
 	 */
 	public abstract Date getCreateDate();
 	
 	/**
 	 * @return the updateDate
 	 */
 	public abstract Date getUpdateDate();
 
 	/**
 	 * @return the zoneList
 	 */
 	public abstract List getZoneList();
 
 	/**
 	 * @return the folderList
 	 */
 	public abstract List getFolderList();
 
 	/**
 	 * @return the authList
 	 */
 	public abstract List getAuthList();
 
 	/**
 	 * @param id
 	 *            the id to set
 	 */

	public void setId(Serializable id) {
		this.id = (Long) id;
 	}
 
 	/**
 	 * @param code
 	 *            the code to set
 	 */
 	public void setCode(String code) {
 		this.code = code;
 	}
 
 	/**
 	 * @param name
 	 *            the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @param type
 	 *            the type to set
 	 */
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	/**
 	 * @param extension
 	 *            the extension to set
 	 */
 	public void setExtension(String extension) {
 		this.extension = extension;
 	}
 
 	/**
 	 * @param createDate the createDate to set
 	 */
 	public void setCreateDate(Date createDate) {
 		this.createDate = createDate;
 	}
 
 	/**
 	 * @param updateDate the updateDate to set
 	 */
 	public void setUpdateDate(Date updateDate) {
 		this.updateDate = updateDate;
 	}
 
 	/**
 	 * @param zoneList
 	 *            the zoneList to set
 	 */
 	public void setZoneList(List zoneList) {
 		this.zoneList = zoneList;
 	}
 
 	/**
 	 * @param folderList
 	 *            the folderList to set
 	 */
 	public void setFolderList(List folderList) {
 		this.folderList = folderList;
 	}
 
 	/**
 	 * @param authList
 	 *            the authList to set
 	 */
 	public void setAuthList(List authList) {
 		this.authList = authList;
 	}
 
 }
