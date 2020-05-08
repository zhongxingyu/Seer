 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.jbi.security.acl.impl;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.xml.XMLConstants;
 import javax.xml.namespace.QName;
 
 import org.apache.servicemix.jbi.security.GroupPrincipal;
 
 /**
  * 
  * @author gnodet
  * @org.apache.xbean.XBean 
  */
 public class AuthorizationEntry {
     
     /**
      * Add the roles to the ACLs list
      */
     public static final String TYPE_ADD = "add";
     /**
      * Set the ACLs to the given roles
      */
     public static final String TYPE_SET = "set";
     /**
      * Remove the given roles from the ACLs list
      */
     public static final String TYPE_REM = "rem";
 
     private Set acls;
     private QName service;
     private String endpoint;
     private String type = TYPE_ADD;
 
     public AuthorizationEntry() {
     }
     
     public AuthorizationEntry(QName service, String endpoint, String roles) {
         this.service = service;
         this.endpoint = endpoint;
         setRoles(roles);
     }
     
     public AuthorizationEntry(QName service, String endpoint, String roles, String type) {
         this.service = service;
         this.endpoint = endpoint;
         setRoles(roles);
         this.type = type;
     }
     
     /**
      * @return the type
      */
     public String getType() {
         return type;
     }
 
     /**
      * @param type the type to set
      */
     public void setType(String type) {
         this.type = type;
     }
 
     /**
      * @return the endpoint
      */
     public String getEndpoint() {
         return endpoint;
     }
 
     /**
      * @param endpoint the endpoint to set
      */
     public void setEndpoint(String endpoint) {
         this.endpoint = endpoint;
     }
 
     /**
      * @return the service
      */
     public QName getService() {
         return service;
     }
 
     /**
      * @param service the service to set
      */
     public void setService(QName service) {
         // Hack a bit to support wildcards
         // If the attribute was service="*:*", then the namespace is not found, but the prefix is set
         if (XMLConstants.NULL_NS_URI.equals(service.getNamespaceURI()) &&
             service.getPrefix() != null && service.getPrefix().length() > 0) {
             service = new QName(service.getPrefix(), service.getLocalPart());
         }
         this.service = service;
     }
 
     /**
      * @return the acls
      */
     public Set getAcls() {
         return acls;
     }
 
     /**
      * @param acls the acls to set
      */
     public void setAcls(Set acls) {
         this.acls = acls;
     }
     
     public void setRoles(String roles) {
         this.acls = new HashSet();
         StringTokenizer iter = new StringTokenizer(roles, ",");
         while (iter.hasMoreTokens()) {
             String name = iter.nextToken().trim();
             this.acls.add(new GroupPrincipal(name));
         }
     }
     
     public String getRoles() {
         StringBuffer sb = new StringBuffer();
        if (this.acls != null) {
            for (Iterator iter = this.acls.iterator(); iter.hasNext();) {
                GroupPrincipal p = (GroupPrincipal) iter.next();
                sb.append(p);
                if (iter.hasNext()) {
                    sb.append(",");
                }
             }
         }
         return sb.toString();
     }
     
     public String toString() {
         return "AuthorizationEntry[service=" + service + ", endpoint=" + endpoint + ", roles=" + getRoles() + "]";
     }
 }
