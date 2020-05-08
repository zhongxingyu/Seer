 /*
  * AuthorityEntity.java
  * 
  * Copyright (C) 2011
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
 package com.emergya.persistenceGeo.metaModel;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Entity that represents the users group
  * 
  * @author <a href="mailto:adiaz@emergya.com">adiaz</a>
  * @author <a href="mailto:marcos@emergya.com">marcos</a>
  *
  */
 @SuppressWarnings("rawtypes")
 public abstract class AbstractAuthorityEntity extends AbstractEntity{
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 2468366677563450048L;
 	
 	protected Long id;
 	protected String name;
     
 	protected Date createDate;
 	protected Date updateDate;
     
 	protected Set<? extends AbstractUserEntity> people;
 	protected AbstractAuthorityTypeEntity authType;
 	protected List layerList;
 	protected AbstractZoneEntity zone;
 
 	/**
 	 * Getters methods to map this entity
 	 */
     public abstract Long getId();
     public abstract String getName();
     public abstract Set getPeople();
     public abstract Date getCreateDate();
     public abstract Date getUpdateDate();
 	public abstract AbstractAuthorityTypeEntity getAuthType();
 	public abstract List getLayerList();
 	public abstract AbstractZoneEntity getZone();
 
     public AbstractAuthorityEntity() {
     }
 
     public AbstractAuthorityEntity(String authString) {
         name = authString;
     }
 
     public void setName(String authority) {
         this.name = authority;
     }
 
     public void setId(Serializable id) {
         this.id = (Long) id;
     }
 
     public void setPeople(Set<AbstractUserEntity> people) {
         this.people = people;
     }
 
	public void setCreateDate(Date createDate) {
 		this.createDate = createDate;
 	}
 
 	public void setUpdateDate(Date updateDate) {
 		this.updateDate = updateDate;
 	}
 
 	public void setAuthType(AbstractAuthorityTypeEntity authType) {
 		this.authType = authType;
 	}
 
 	public void setLayerList(List layerList) {
 		this.layerList = layerList;
 	}
 
 	public void setZone(AbstractZoneEntity zone) {
 		this.zone = zone;
 	}
 
 }
