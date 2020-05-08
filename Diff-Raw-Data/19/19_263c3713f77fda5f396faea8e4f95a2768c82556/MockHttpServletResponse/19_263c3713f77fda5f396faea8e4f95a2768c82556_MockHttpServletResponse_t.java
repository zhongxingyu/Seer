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
 
 import java.io.IOException;
 import java.io.PrintWriter;
import java.util.Hashtable;
 import java.util.Locale;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * 
  * @author Jacob Hookom
 * @version $Id: MockHttpServletResponse.java,v 1.2 2005-07-19 00:49:01 jhook Exp $
  */
 public class MockHttpServletResponse implements HttpServletResponse {
     
     private boolean committed = false;
     private int status;
     private String message;
     private Hashtable headers = new Hashtable();
     private String characterEncoding = "ISO-8859-1";
     private String contentType = "text/html";
     private long contentLength = 0;
     private int bufferSize = 0;
     private Locale locale = Locale.getDefault();
     
     public MockHttpServletResponse() {
         super();
     }
 
     public void addCookie(Cookie cookie) {
         // TODO Auto-generated method stub
 
     }
 
     public boolean containsHeader(String name) {
         return this.headers.contains(name);
     }
 
     public String encodeURL(String url) {
         return url;
     }
 
     public String encodeRedirectURL(String url) {
         return url;
     }
 
     public String encodeUrl(String url) {
         return url;
     }
 
     public String encodeRedirectUrl(String url) {
         return this.encodeRedirectURL(url);
     }
 
     public void sendError(int status, String message) throws IOException {
         if (this.committed) {
             throw new IllegalStateException("Response is already committed");
         }
         this.status = status;
         this.message = message;
         this.committed = true;
     }
 
     public void sendError(int status) throws IOException {
         if (this.committed) {
             throw new IllegalStateException("Response is already committed");
         }
         this.status = status;
         this.committed = true;
     }
 
     public void sendRedirect(String path) throws IOException {
         if (this.committed) {
             throw new IllegalStateException("Response is already committed");
         }
         this.committed = true;
     }
 
     public void setDateHeader(String name, long date) {
         this.headers.put(name, ""+date);
     }
 
     public void addDateHeader(String name, long date) {
         this.headers.put(name, ""+date);
     }
 
     public void setHeader(String name, String value) {
         this.headers.put(name, value);
     }
 
     public void addHeader(String name, String value) {
         this.headers.put(name, value);
     }
 
     public void setIntHeader(String name, int value) {
         this.headers.put(name, ""+value);
     }
 
     public void addIntHeader(String name, int value) {
         this.headers.put(name, ""+value);
     }
 
     public void setStatus(int sc) {
         this.status = sc;
     }
 
     public void setStatus(int sc, String message) {
         this.status = sc;
         this.message = message;
     }
 
     public String getCharacterEncoding() {
         return this.characterEncoding;
     }
 
     public String getContentType() {
         return this.contentType;
     }
 
     public ServletOutputStream getOutputStream() throws IOException {
         // TODO Auto-generated method stub
         return null;
     }
 
     public PrintWriter getWriter() throws IOException {
         // TODO Auto-generated method stub
         return null;
     }
 
     public void setCharacterEncoding(String characterEncoding) {
         this.characterEncoding = characterEncoding;
     }
 
     public void setContentLength(int contentLength) {
         this.contentLength = contentLength;
     }
 
     public void setContentType(String contentType) {
         this.contentType = contentType;
     }
 
     public void setBufferSize(int sz) {
         this.bufferSize = sz;
     }
 
     public int getBufferSize() {
         return this.bufferSize;
     }
 
     public void flushBuffer() throws IOException {
 
     }
 
     public void resetBuffer() {
 
     }
 
     public boolean isCommitted() {
         return this.committed;
     }
 
     public void reset() {
         // TODO Auto-generated method stub
 
     }
 
     public void setLocale(Locale locale) {
         this.locale = locale;
     }
 
     public Locale getLocale() {
         return this.locale;
     }
 
 }
