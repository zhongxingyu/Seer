 /**
  * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
  * Licensed under the Common Development and Distribution License,
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.sun.com/cddl/
  *   
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  */
 
 package com.sun.facelets.mock;
 
 import java.util.Enumeration;
import java.util.Hashtable;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.HttpSessionContext;
 
 /**
  * 
  * @author Jacob Hookom
 * @version $Id: MockHttpSession.java,v 1.2 2005-07-19 00:49:02 jhook Exp $
  */
 public class MockHttpSession implements HttpSession {
 
     private final Hashtable attributes = new Hashtable();
     private final long creationTime;
     private String id;
     private long lastAccessedTime;
     private final ServletContext servletContext;
     private int maxInactiveInterval = 20;
     
     public MockHttpSession(ServletContext servletContext) {
         this.servletContext = servletContext;
         this.creationTime = System.currentTimeMillis();
         this.id = "" + this.creationTime;
         this.lastAccessedTime = this.creationTime;
     }
 
     public long getCreationTime() {
         return this.creationTime;
     }
 
     public String getId() {
         return this.id;
     }
 
     public long getLastAccessedTime() {
         return this.lastAccessedTime;
     }
 
     public ServletContext getServletContext() {
         return this.servletContext;
     }
 
     public void setMaxInactiveInterval(int interval) {
         this.maxInactiveInterval = interval;
     }
 
     public int getMaxInactiveInterval() {
         return this.maxInactiveInterval;
     }
 
     public HttpSessionContext getSessionContext() {
         throw new UnsupportedOperationException();
     }
 
     public Object getAttribute(String name) {
         return this.attributes.get(name);
     }
 
     public Object getValue(String name) {
         throw new UnsupportedOperationException();
     }
 
     public Enumeration getAttributeNames() {
         return this.attributes.keys();
     }
 
     public String[] getValueNames() {
         throw new UnsupportedOperationException();
     }
 
     public void setAttribute(String name, Object value) {
         this.attributes.put(name, value);
     }
 
     public void putValue(String arg0, Object arg1) {
         throw new UnsupportedOperationException();
 
     }
 
     public void removeAttribute(String name) {
         this.attributes.remove(name);
     }
 
     public void removeValue(String arg0) {
         throw new UnsupportedOperationException();
     }
 
     public void invalidate() {
 
     }
 
     public boolean isNew() {
         return false;
     }
 
 }
