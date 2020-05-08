 /**
  * Phresco Commons
  *
  * Copyright (C) 1999-2013 Photon Infotech Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.photon.phresco.commons.model;
 
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 
 @XmlRootElement
 @JsonIgnoreProperties(ignoreUnknown = true)
 public class User extends Element {
 
     private static final long serialVersionUID = 1L;
     
     public enum AuthType {
     	LOCAL, AUTHSERVICE, OAUTH
     }
     
     private String loginId;
 	private String email;
 	private String firstName;
 	private String lastName;
 	private List<String> roleIds;
 	private boolean phrescoEnabled;
 	private List<Customer> customers;
 	private String token;
 	private boolean validLogin;
 	private AuthType authType;
 	private String password;
 	
 	public User() {
 		super();
 	}
 
 	public User(String id) {
 		super(id);
 	}
 	
 	/**
 	 * @param id
 	 * @param name
 	 * @param description
 	 */
 	public User(String id, String name, String description) {
 		super(id);
 	}
 
 	/**
 	 * @return
 	 */
 	public String getLoginId() {
         return loginId;
     }
 
     /**
      * @param loginId
      */
     public void setLoginId(String loginId) {
         this.loginId = loginId;
     }
     
     /**
      * @return
      */
     public String getEmail() {
         return email;
     }
     
     /**
      * @param mailId
      */
     public void setEmail(String mailId) {
         this.email = mailId;
     }
     
     /**
      * @return
      */
     public String getFirstName() {
         return firstName;
     }
 
     /**
      * @param firstName
      */
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
     
     /**
      * @return
      */
     public String getLastName() {
         return lastName;
     }
 
     /**
      * @param lastName
      */
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     /**
      * @return
      */
     public List<String> getRoleIds() {
 		return roleIds;
 	}
 
 	/**
 	 * @param roles
 	 */
 	public void setRoleIds(List<String> roleIds) {
 		this.roleIds = roleIds;
 	}
 
 	/**
 	 * @return
 	 */
 	public boolean isPhrescoEnabled() {
         return phrescoEnabled;
     }
 	
 	/**
 	 * @param phrescoEnabled
 	 */
 	public void setPhrescoEnabled(boolean phrescoEnabled) {
         this.phrescoEnabled = phrescoEnabled;
     }
 	
     /**
      * @return
      */
     public String getToken() {
         return token;
     }
 
     /**
      * @param token
      */
     public void setToken(String token) {
         this.token = token;
     }
 	
 	public boolean isValidLogin() {
 		return validLogin;
 	}
 
 	public void setValidLogin(boolean validLogin) {
 		this.validLogin = validLogin;
 	}
 
 	public AuthType getAuthType() {
 		return authType;
 	}
 
 	public void setAuthType(AuthType authType) {
 		this.authType = authType;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public List<Customer> getCustomers() {
 		return customers;
 	}
 
 	public void setCustomers(List<Customer> customers) {
 		this.customers = customers;
 	}
 
 	public String toString() {
         return new ToStringBuilder(this,
                 ToStringStyle.DEFAULT_STYLE)
                 .append(super.toString())
                 .append("loginId", loginId)
                 .append("email", email)
                 .append("firstName", firstName)
                 .append("lastName", lastName)
                 .append("roleIds", roleIds)
                 .append("phrescoEnabled", phrescoEnabled)
                 .append("customers", customers)
                 .append("token", token)
                 .append("authType", authType)
                 .append("password", password)
                 .toString();
     }
 }
