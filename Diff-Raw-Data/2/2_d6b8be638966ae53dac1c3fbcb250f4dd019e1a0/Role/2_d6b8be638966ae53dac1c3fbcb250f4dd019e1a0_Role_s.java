 /*
  * Copyright 2013 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.evinceframework.membership.model;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Transient;
 
 import org.springframework.beans.factory.annotation.Configurable;
 import org.springframework.security.core.GrantedAuthority;
 
 import com.evinceframework.jpa.BaseEntity;
 import com.evinceframework.membership.Configuration;
 import com.evinceframework.membership.authentication.AuthenticationProviderImpl;
 
 /**
  * A JPA backed implementation of {@link GrantedAuthority}.  Security roles and their relationship 
  * to {@link User}s are stored in a database.
  * 
  * @author Craig Swing
  * 
  * @see GrantedAuthority
  * @see User
  * @see AuthenticationProviderImpl
  */
 @Configurable("evf.membership.role")
 @Entity(name="evf_membership_role")
 public class Role extends BaseEntity implements GrantedAuthority {
 
 	private static final long serialVersionUID = 313449404324420067L;
 
 	private Configuration configuration;
 	
 	private String token;
 	
 	private String description;
 
 	protected Role(){}
 	
 	@Transient
 	public Configuration getConfiguration() {
 		return configuration;
 	}
 
 	public void setConfiguration(Configuration configuration) {
 		this.configuration = configuration;
 	}
 	
 	/**
 	 * A token that uniquely identifies the security role.
 	 * 
 	 * @return the token
 	 */
 	@Column(name="token", length=64, unique=true, nullable=false)
 	public String getToken() {
 		return token;
 	}
 
 	protected void setToken(String token) {
 		this.token = token;
 	}
 
 	/**
 	 * The description of the security role.
 	 * 
 	 * @return the description
 	 */
 	@Column(name="description", length=512, nullable=false)
 	public String getDescription() {
 		return description;
 	}
 
 	protected void setDescription(String description) {
 		this.description = description;
 	}
 
 	@Override
 	@Transient
 	public String getAuthority() {
		return String.format("%s%s", token);
 	}	
 }
