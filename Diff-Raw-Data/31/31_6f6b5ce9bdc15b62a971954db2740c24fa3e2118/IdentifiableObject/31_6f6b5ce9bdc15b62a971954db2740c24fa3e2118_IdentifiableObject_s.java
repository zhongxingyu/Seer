 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
  * 
  * Licensed under the Educational Community License, Version 1.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at
  * 
  *      http://www.opensource.org/licenses/ecl1.php
  * 
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.metaobj.shared.model;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
 * Created by IntelliJ IDEA.
  * User: jbush
  * Date: May 15, 2004
  * Time: 1:55:47 PM
  * To change this template use File | Settings | File Templates.
  */
 abstract public class IdentifiableObject {
    private Id id;
    private Id newId;
    protected final Log logger = LogFactory.getLog(this.getClass());
 
    public boolean equals(Object in) {
       if (this == in) {
          return true;
       }
       if (in == null && this == null) {
          return true;
       }
       if (in == null && this != null) {
          return false;
       }
       if (this == null && in != null) {
          return false;
       }
       if (!this.getClass().isAssignableFrom(in.getClass())) {
          return false;
       }
       if (this.getId() == null && ((IdentifiableObject) in).getId() == null) {
          return true;
       }
       if (this.getId() == null || ((IdentifiableObject) in).getId() == null) {
          return false;
       }
       return this.getId().equals(((IdentifiableObject) in).getId());
    }
 
    public int hashCode() {
       return (id != null ? id.hashCode() : 0);
    }
 
    public Id getId() {
       return id;
    }
 
    public void setId(Id id) {
       this.id = id;
    }
 
    public Id getNewId() {
       return newId;
    }
 
    public void setNewId(Id newId) {
       this.newId = newId;
    }
 }
