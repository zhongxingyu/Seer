 /**
  * Copyright (C) cedarsoft GmbH.
  *
  * Licensed under the GNU General Public License version 3 (the "License")
  * with Classpath Exception; you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *         http://www.cedarsoft.org/gpl3ce
  *         (GPL 3 with Classpath Exception)
  *
  * This code is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 3 only, as
  * published by the Free Software Foundation. cedarsoft GmbH designates this
  * particular file as subject to the "Classpath" exception as provided
  * by cedarsoft GmbH in the LICENSE file that accompanied this code.
  *
  * This code is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  * version 3 for more details (a copy is included in the LICENSE file that
  * accompanied this code).
  *
  * You should have received a copy of the GNU General Public License version
  * 3 along with this work; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
  * or visit www.cedarsoft.com if you need additional information or
  * have any questions.
  */
 
 package com.cedarsoft.rest;
 
 import com.cedarsoft.jaxb.AbstractJaxbObject;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementRef;
 import javax.xml.bind.annotation.XmlRootElement;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  */
 @XmlRootElement( namespace = "test:foo" )
 @XmlAccessorType( XmlAccessType.FIELD )
 public class Foo extends AbstractJaxbObject {
   private String daValue = "default";
 
   @XmlElement( name = "daName" )
   private List<String> names = new ArrayList<String>();
  @XmlElementRef(name = "daBars" )
   private List<Bar> bars = new ArrayList<Bar>();
 
   public List<String> getNames() {
     return names;
   }
 
   public void setNames( List<String> names ) {
     this.names = names;
   }
 
   public List<Bar> getBars() {
     return bars;
   }
 
   public void setBars( List<Bar> bars ) {
     this.bars = bars;
   }
 
   public String getDaValue() {
     return daValue;
   }
 
   public void setDaValue( String daValue ) {
     this.daValue = daValue;
   }
 
   @XmlRootElement( namespace = "test:bar" )
   public static class Bar extends AbstractJaxbObject {
     private int theId;
 
     public Bar() {
     }
 
     public Bar( int theId ) {
       this.theId = theId;
     }
 
     public int getTheId() {
       return theId;
     }
 
     public void setTheId( int theId ) {
       this.theId = theId;
     }
   }
 }
