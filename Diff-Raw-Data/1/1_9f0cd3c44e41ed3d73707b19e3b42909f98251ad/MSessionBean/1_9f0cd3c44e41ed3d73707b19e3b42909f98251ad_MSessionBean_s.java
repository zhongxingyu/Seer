 /*
  * Copyright 2012 INRIA
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
 package com.mymed.model.data.session;
 
 import com.mymed.model.data.AbstractMBean;
 
 /**
  * A session bean
  * 
  * @author Milo Casagrande
  * @author lvanni
  * 
  */
 public class MSessionBean extends AbstractMBean {
   /** SESSION_ID */
   private String id;
   /** USER_ID */
   private String user;
   /** APPLICATION_LIST_ID */
   private String currentApplications;
   private long timeout;
   private boolean isP2P;
   private String ip;
   private int port;
   private String accessToken;
   private boolean isExpired = false;
 
   /**
    * Create a new MSessionBean
    */
   public MSessionBean() {
     super();
   }
 
   /**
    * Copy constructor
    * <p>
    * Provide a clone of the actual bean
    * 
    * @param toClone
    *          the object to clone
    */
   protected MSessionBean(final MSessionBean toClone) {
     id = toClone.getId();
     user = toClone.getUser();
     currentApplications = toClone.getCurrentApplications();
     timeout = toClone.getTimeout();
     isP2P = toClone.isP2P();
     port = toClone.getPort();
     accessToken = toClone.getAccessToken();
     isExpired = toClone.isExpired();
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#clone()
    */
   @Override
   public MSessionBean clone() {
     return new MSessionBean(this);
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.mymed.model.data.AbstractMBean#toString()
    */
   @Override
   public String toString() {
     return "Session:\n" + super.toString();
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
     int result = 1;
 
     result = PRIME * result + (id == null ? 0 : id.hashCode());
     result = PRIME * result + (ip == null ? 0 : ip.hashCode());
     result = PRIME * result + (isP2P ? 1231 : 1237);
     result = PRIME * result + port;
     result = PRIME * result + (user == null ? 0 : user.hashCode());
 
     return result;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(final Object object) {
 
     boolean equal = false;
 
     if (this == object) {
       equal = true;
     } else if (object != null && object instanceof MSessionBean) {
       final MSessionBean comparable = (MSessionBean) object;
 
       equal = true;
 
       if (id == null && comparable.getId() != null || id != null && comparable.getId() == null) {
         equal &= false;
       } else if (id != null && comparable.getId() != null) {
         equal &= id.equals(comparable.getId());
       }
 
       if (ip == null && comparable.getIp() != null || ip != null && comparable.getIp() == null) {
         equal &= false;
       } else if (ip != null && comparable.getIp() != null) {
         equal &= ip.equals(comparable.getIp());
       }
 
       equal &= isP2P == comparable.isP2P();
       equal &= port == comparable.getPort();
       equal &= isExpired == comparable.isExpired();
 
       if (accessToken == null && comparable.getAccessToken() != null || accessToken != null
           && comparable.getAccessToken() == null) {
         equal &= false;
       } else if (accessToken != null && comparable.getAccessToken() != null) {
         equal &= accessToken.equals(comparable.getAccessToken());
       }
 
       if (user == null && comparable.getUser() != null || user != null && comparable.getUser() == null) {
         equal &= false;
       } else if (user != null && comparable.getUser() != null) {
         equal &= user.equals(comparable.getUser());
       }
     }
 
     return equal;
   }
 
   /**
    * @return the id of the session
    */
   public String getId() {
     return id;
   }
 
   /**
    * Set the id of the session
    * 
    * @param id
    */
   public void setId(final String id) {
     this.id = id;
   }
 
   /**
    * @return the ID of the user associated with the session
    */
   public String getUser() {
     return user;
   }
 
   /**
    * Set the ID of the user to associate with this session
    * 
    * @param user
    */
   public void setUser(final String user) {
     this.user = user;
   }
 
   /**
    * @return the ID of the applications list associated with this session
    */
   public String getCurrentApplications() {
     return currentApplications;
   }
 
   /**
    * Set the ID of the applications list to associate with this session
    * 
    * @param currentApplications
    */
   public void setCurrentApplications(final String currentApplications) {
     this.currentApplications = currentApplications;
   }
 
   public long getTimeout() {
     return timeout;
   }
 
   public void setTimeout(final long timestamp) {
     timeout = timestamp;
   }
 
   /**
    * @return true if the session is on a P2P protocol
    */
   public boolean isP2P() {
     return isP2P;
   }
 
   /**
    * Set whatever the session is on a P2P protocol
    * 
    * @param isP2P
    */
   public void setP2P(final boolean isP2P) {
     this.isP2P = isP2P;
   }
 
   /**
    * @return the IP bound to this session
    */
   public String getIp() {
     return ip;
   }
 
   /**
    * Set the IP of this session
    * 
    * @param ip
    */
   public void setIp(final String ip) {
     this.ip = ip;
   }
 
   /**
    * @return the port of the session
    */
   public int getPort() {
     return port;
   }
 
   /**
    * Set the port of the session
    * 
    * @param port
    */
   public void setPort(final int port) {
     this.port = port;
   }
 
   public String getAccessToken() {
     return accessToken;
   }
 
   public void setAccessToken(final String accessToken) {
     this.accessToken = accessToken;
   }
 
   public boolean isExpired() {
     return isExpired;
   }
 
   public void setExpired(final boolean isExpired) {
     this.isExpired = isExpired;
   }
 }
