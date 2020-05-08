 /*
  * LayerDto.java
  * 
  * Copyright (C) 2012
  * 
  * This file is part of Proyecto persistenceGeo
  * 
  * This software is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option) any
  * later version.
  * 
  * This software is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this library; if not, write to the Free Software Foundation, Inc., 51
  * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  * 
  * As a special exception, if you link this library with other files to produce
  * an executable, this library does not by itself cause the resulting executable
  * to be covered by the GNU General Public License. This exception does not
  * however invalidate any other reasons why the executable file might be covered
  * by the GNU General Public License.
  * 
  * Authors:: Alejandro Díaz Torres (mailto:adiaz@emergya.com)
  */
 package com.emergya.persistenceGeo.dto;
 
 import java.io.File;
 import java.io.Serializable;
 import java.util.Date;
 import java.util.Map;
 
 import com.emergya.persistenceGeo.dto.AbstractDto;
 
 /**
  * Layer Data Transfer Object 
  * 
  * @author <a href="mailto:adiaz@emergya.com">adiaz</a>
  * @author <a href="mailto:marcos@emergya.com">marcos</a>
  *
  */
 public class LayerDto extends AbstractDto implements Serializable, Cloneable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3733098369106415376L;
 
 	protected Long id;
 	
 	protected String name;
 	protected String order;
 	protected String type;
 	private Long typeId;
 	protected String server_resource;
 	protected File data;
 	protected Boolean published;
 	protected Boolean enabled;
 	protected Boolean pertenece_a_canal;
 	protected Date createDate;
 	protected Date updateDate;
 	
 	protected String user;
 	protected Map<StyleDto, Map<RuleDto, Map<String, String>>> styles;
 	protected Long folderId;
 	protected Map<String, String> properties;
 	protected Long authId;
 
 	private String layerTitle;
 	
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getOrder() {
 		return order;
 	}
 	public void setOrder(String order) {
 		this.order = order;
 	}
 	public String getType() {
 		return type;
 	}
 	public void setType(String type) {
 		this.type = type;
 	}
 	public String getServer_resource() {
 		return server_resource;
 	}
 	public void setServer_resource(String server_resource) {
 		this.server_resource = server_resource;
 	}
 	public Boolean getPublicized() {
 		return published;
 	}
 	public void setPublicized(Boolean publicized) {
 		this.published = publicized;
 	}
 	public Boolean getEnabled() {
 		return enabled;
 	}
 	public void setEnabled(Boolean enabled) {
 		this.enabled = enabled;
 	}
 	public Boolean getPertenece_a_canal() {
 		return pertenece_a_canal;
 	}
 	public void setPertenece_a_canal(Boolean pertenece_a_canal) {
 		this.pertenece_a_canal = pertenece_a_canal;
 	}
 	public Date getCreateDate() {
 		return createDate;
 	}
 	public void setCreateDate(Date createDate) {
 		this.createDate = createDate;
 	}
 	public Date getUpdateDate() {
 		return updateDate;
 	}
 	public void setUpdateDate(Date updateDate) {
 		this.updateDate = updateDate;
 	}
 	
 	public String getUser() {
 		return user;
 	}
 	public void setUser(String user) {
 		this.user = user;
 	}
 	/**
 	 * @return the data
 	 */
 	public File getData() {
 		return data;
 	}
 	/**
 	 * @param data the data to set
 	 */
 	public void setData(File data) {
 		this.data = data;
 	}
 	/**
 	 * @return the authId
 	 */
 	public Long getAuthId() {
 		return authId;
 	}
 	/**
 	 * @param authId the authId to set
 	 */
 	public void setAuthId(Long authId) {
 		this.authId = authId;
 	}
 	
 	/**
 	 * @return the properties
 	 */
 	public Map<String, String> getProperties() {
 		return properties;
 	}
 	/**
 	 * @param properties the properties to set
 	 */
 	public void setProperties(Map<String, String> properties) {
 		this.properties = properties;
 	}
 	/**
 	 * @return the folderId
 	 */
 	public Long getFolderId() {
 		return folderId;
 	}
 	/**
 	 * @param folderId the folderId to set
 	 */
 	public void setFolderId(Long folderId) {
 		this.folderId = folderId;
 	}
 	/**
 	 * @return the published
 	 */
 	public Boolean getPublished() {
 		return published;
 	}
 	/**
 	 * @param published the published to set
 	 */
 	public void setPublished(Boolean published) {
 		this.published = published;
 	}
 	/**
 	 * @return the styles
 	 */
 	public Map<StyleDto, Map<RuleDto, Map<String, String>>> getStyles() {
 		return styles;
 	}
 	/**
 	 * @param styles the styles to set
 	 */
 	public void setStyles(Map<StyleDto, Map<RuleDto, Map<String, String>>> styles) {
 		this.styles = styles;
 	}
 	
 	/**
 	 * Clone layer DTO
 	 */
 	public Object clone() throws CloneNotSupportedException {
 		LayerDto cloned = (LayerDto) super.clone();
 		LayerDto origin = this;
 		
 		cloned.id = null; // not clone id
 		//cloned.id = origin.id != null ? new Long(origin.id) : null; // clone id
 		cloned.name = origin.name != null ? new String(origin.name) : null;
 		cloned.enabled = origin.enabled != null ? new Boolean(origin.enabled)
 				: null;
 		cloned.createDate = origin.createDate != null ? (Date) origin.createDate
 				.clone() : null;
 		cloned.updateDate = origin.updateDate != null ? (Date) origin.updateDate
 				.clone() : null;
 		cloned.order = origin.order != null ? new String(origin.order) : null;
 		cloned.type = origin.type != null ? new String(origin.type) : null;
 		cloned.server_resource = origin.server_resource != null ? new String(
 				origin.server_resource) : null;
 		cloned.user = origin.user != null ? new String(origin.user) : null;
 		cloned.published = origin.published ? new Boolean(origin.published)
 				: null;
 		cloned.enabled = origin.enabled ? new Boolean(enabled) : null;
 		cloned.pertenece_a_canal = origin.pertenece_a_canal ? new Boolean(
 				pertenece_a_canal) : null;
 		cloned.folderId = origin.folderId != null ? new Long(origin.folderId)
 				: null;
 		cloned.authId = origin.authId != null ? new Long(origin.authId) : null;
 
 		cloned.data = origin.data; // FIXME: clone data
 		cloned.styles = origin.styles; // FIXME: clone styles
 		cloned.properties = origin.properties; // FIXME: clone properties
 		
 		return cloned;
 	}
 	
 	public String getLayerTitle() {
 		return layerTitle;
 	}
 	
 	public void setLayerTitle(String title) {
 		this.layerTitle = title;		
 	}
 
 	public Long getTypeId() {
 		return typeId;
 	}
 	public void setTypeId(Long typeId) {
 		this.typeId = typeId;
 	}
 
 }
