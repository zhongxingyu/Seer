 /*
  * AbstractLayerEntity.java
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
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Entidad de capa
  * 
  * @author <a href="mailto:marcos@emergya.com">marcos</a>
  *
  */
 @SuppressWarnings("rawtypes")
 public abstract class AbstractLayerEntity extends AbstractEntity implements Cloneable {
 
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5351127306343028162L;
 
 	protected Long id;
 	
 	protected String name;
 	protected String layerTitle;
 	protected String order;
 	protected AbstractLayerTypeEntity type;
 	protected String server_resource;
 	protected byte[] data;
 
 	protected Boolean publicized;
 	protected Boolean enabled;
 	protected Boolean isChannel;
 	protected Date createDate;
 	protected Date updateDate;
 	
 	protected AbstractUserEntity user;
 	protected AbstractAuthorityEntity auth;
 	protected AbstractFolderEntity folder;
 	protected List styleList;
 
 	protected List properties;
 
 	public AbstractLayerEntity(){
 		
 	}
 	
 	public AbstractLayerEntity(String layerName){
 		name = layerName;
 	}
 
 	/**
 	 * @return the id
 	 */
 	public abstract Long getId();
 	/**
 	 * @return the name
 	 */
 	public abstract String getName();
 	/**
 	 * @return the order
 	 */
 	public abstract String getOrder();
 	/**
 	 * @return the type
 	 */
 	public abstract AbstractLayerTypeEntity getType();
 	/**
 	 * @return the server_resource
 	 */
 	public abstract String getServer_resource();
 	/**
 	 * @return the publicized
 	 */
 	public abstract Boolean getPublicized();
 	/**
 	 * @return the enabled
 	 */
 	public abstract Boolean getEnabled();
 	/**
 	 * @return the isChannel
 	 */
 	public abstract Boolean getIsChannel();
 	/**
 	 * @return the fechaCreacion
 	 */
 	public abstract Date getCreateDate();
 	/**
 	 * @return the fechaActualizacion
 	 */
 	public abstract Date getUpdateDate();
 	/**
 	 * @return the user
 	 */
 	public abstract AbstractUserEntity getUser();
 	/**
 	 * @return the auth
 	 */
 	public abstract AbstractAuthorityEntity getAuth();
 	/**
 	 * @return the folderList
 	 */
 	public abstract AbstractFolderEntity getFolder();
 	
 	/**
 	 * @return the data
 	 */
 	public abstract byte[] getData();
 	
 	/**
 	 * @return the properties
 	 */
 	public abstract List getProperties();
 	/**
 	 * @return the styleList
 	 */
 	public abstract List getStyleList();
 
 	public abstract String getLayerTitle();
 	
 	/**
 	 * @param id the id to set
 	 */
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	/**
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @param order the order to set
 	 */
 	public void setOrder(String order) {
 		this.order = order;
 	}
 
 	/**
 	 * @param type the type to set
 	 */
 	public void setType(AbstractLayerTypeEntity type) {
 		this.type = type;
 	}
 
 	/**
 	 * @param server_resource the server_resource to set
 	 */
 	public void setServer_resource(String server_resource) {
 		this.server_resource = server_resource;
 	}
 
 	/**
 	 * @param publicized the publicized to set
 	 */
 	public void setPublicized(Boolean publicized) {
 		this.publicized = publicized;
 	}
 
 	/**
 	 * @param enabled the enabled to set
 	 */
 	public void setEnabled(Boolean enabled) {
 		this.enabled = enabled;
 	}
 	
 	public void setLayerTitle(String layerTitle) {
 		this.layerTitle = layerTitle;
 	}
 
 
 	/**
 	 * @param pertenece_a_canal the pertenece_a_canal to set
 	 */
 	public void setIsChannel(Boolean isChannel) {
 		this.isChannel = isChannel;
 	}
 
 	/**
 	 * @param fechaCreacion the createDate to set
 	 */
 	public void setCreateDate(Date createDate) {
 		this.createDate = createDate;
 	}
 
 	/**
 	 * @param fechaActualizacion the fechaActualizacion to set
 	 */
 	public void setUpdateDate(Date updateDate) {
 		this.updateDate = updateDate;
 	}
 
 	/**
 	 * @param user the user to set
 	 */
 	public void setUser(AbstractUserEntity user) {
 		this.user = user;
 	}
 
 	/**
 	 * @param auth the auth to set
 	 */
 	public void setAuth(AbstractAuthorityEntity auth) {
 		this.auth = auth;
 	}
 
 	/**
 	 * @param folder the folder to set
 	 */
 	public void setFolder(AbstractFolderEntity folder) {
 		this.folder = folder;
 	}
 
 	/**
 	 * @param data the data to set
 	 */
 	public void setData(byte[] data) {
 		this.data = data;
 	}
 
 	/**
 	 * @param properties the properties to set
 	 */
 	@SuppressWarnings("unchecked")
 	public void setProperties(List properties) {
		/* #85692: Changes for orphan removal. 
		 * @see http://stackoverflow.com/questions/5587482/hibernate-a-collection-with-cascade-all-delete-orphan-was-no-longer-referenc
		 */
		if(this.properties != null){
			// 
			this.properties.clear();
			this.properties.addAll(properties);
		}else{
			this.properties = properties;
		}
 	}
 	
 	/**
 	 * @param styleList the styleList to set
 	 */
 	public void setStyleList(List styleList) {
 		this.styleList = styleList;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.emergya.persistenceGeo.metaModel.AbstractEntity#setId(java.io.Serializable)
 	 */
 	@Override
 	public void setId(Serializable id) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/** 
 	 * @see java.lang.Object#clone()
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public Object clone() throws CloneNotSupportedException {
 		AbstractLayerEntity layer =  (AbstractLayerEntity) super.clone();
 		
 		layer.name = this.getName() != null ? new String(this.name) : null;
 		layer.order = this.getOrder() != null ? new String(this.order) : null;
 		layer.server_resource = this.getServer_resource() != null ? new String(this.server_resource) : null;
 		layer.data = this.getData() != null ? data.clone() : null;
 
 		layer.publicized = this.getPublicized() != null ? new Boolean(this.publicized) : null;
 		layer.enabled = this.getEnabled() != null ? new Boolean(this.enabled) : null;
 		layer.isChannel = this.getIsChannel() != null ? new Boolean(this.isChannel) : null;
 		
 		layer.createDate = (Date) (this.getCreateDate() != null ? createDate.clone() : null);
 		layer.updateDate = (Date) (this.getUpdateDate() != null ? updateDate.clone() : null);
 
 		// type not cloned
 		layer.type = this.type;
 
 		// this entities are nulled
 		layer.user = null;
 		layer.auth = null;
 		layer.folder = null;
 		
 		// styleList clone
 		if(this.getStyleList() != null){
 			layer.styleList = new LinkedList();
 			for(Object obj: this.styleList){
 				AbstractStyleEntity style = (AbstractStyleEntity) obj;
 				layer.styleList.add(style.clone());
 			}
 		}
 		
 		// properties clone
 		if(this.properties != null){
 			layer.properties = new LinkedList();
 			for(Object obj: this.properties){
 				AbstractLayerPropertyEntity property = (AbstractLayerPropertyEntity) obj;
 				layer.properties.add(property.clone());
 			}
 		}
 
 		
 		return layer;
 	}
 	
 	
 }
