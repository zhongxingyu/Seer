 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010-2012, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *******************************************************************************/
 package org.richfaces.tests.metamer;
 
 import java.io.Serializable;
 
 /**
  * Representation of a message pushed to the page using JMS.
 *
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version$Revision: 22493$
  */
 public class Message implements Serializable {
 
     private static final long serialVersionUID = 6869835374685376541L;
 
     private String text;
     private String author;
     private String timestamp;
 
     public Message() {
     }
 
     public Message(String text, String author, String timestamp) {
         this.text = text;
         this.author = author;
         this.timestamp = timestamp;
     }
 
     public String getText() {
         return text;
     }
 
     public void setText(String text) {
         this.text = text;
     }
 
     public String getAuthor() {
         return author;
     }
 
     public void setAuthor(String author) {
         this.author = author;
     }
 
     public String getTimestamp() {
         return timestamp;
     }
 
     public void setTimestamp(String timestamp) {
         this.timestamp = timestamp;
     }
 }
