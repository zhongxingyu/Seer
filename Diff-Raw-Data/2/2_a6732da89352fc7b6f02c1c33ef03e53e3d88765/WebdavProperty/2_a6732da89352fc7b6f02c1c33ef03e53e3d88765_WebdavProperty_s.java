 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 
 package org.bedework.webdav.servlet.shared;
 
 import org.bedework.util.misc.ToString;
import edu.rpi.sss.util.Util;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.namespace.QName;
 
 /** One of these for each property in a request.
  *
  *   @author Mike Douglass   douglm  rpi.edu
  */
 public class WebdavProperty implements Serializable {
   private final QName tag;
   private String pval;
 
   private List<Attribute> attrs;
 
   public static class Attribute implements Serializable {
     public String name;
     public String value;
 
     Attribute(final String name,
               final String value) {
       this.name = name;
       this.value = value;
     }
   }
 
   /** Constructor
    *
    * @param tag  QName name
    * @param pval String value
    */
   public WebdavProperty(final QName tag,
                         final String pval) {
     this.tag = tag;
     this.pval = pval;
   }
 
   /**
    * @return QName tag name
    */
   public QName getTag() {
     return tag;
   }
 
   /**
    * @param val the value
    */
   public void setPval(final String val) {
     pval = val;
   }
 
   /**
    * @return String value
    */
   public String getPval() {
     return pval;
   }
 
   /**
    * @return attribute list - never null
    */
   public List<Attribute> getAttrs() {
     if (attrs == null) {
       attrs = new ArrayList<>();
     }
 
     return attrs;
   }
 
   /**
    * @return true if we have some attributes
    */
   public boolean hasAttrs() {
     return !Util.isEmpty(attrs);
   }
 
   public String getAttr(final String name) {
     if (!hasAttrs()) {
       return null;
     }
 
     for (Attribute attr: attrs) {
       if (attr.name.equals(name)) {
         return attr.value;
       }
     }
 
     return null;
   }
 
   public void addAttr(final String name, final String val) {
     getAttrs().add(new Attribute(name, val));
   }
 
   public String toString() {
     final ToString ts = new ToString(this);
 
     ts.append("tag", getTag());
     ts.append("pval", getPval());
     ts.append("attrs", attrs);
 
     return ts.toString();
   }
 }
