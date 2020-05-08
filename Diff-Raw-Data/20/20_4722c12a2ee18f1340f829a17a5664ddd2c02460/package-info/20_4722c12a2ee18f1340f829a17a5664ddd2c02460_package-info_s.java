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
 
 @javax.xml.bind.annotation.XmlSchema(
   xmlns = {
     @javax.xml.bind.annotation.XmlNs( prefix = "cam", namespaceURI = Camera.NS ),
    @javax.xml.bind.annotation.XmlNs( prefix = "cam-s", namespaceURI = Camera.Stub.NS_STUB ),
 
     @javax.xml.bind.annotation.XmlNs( prefix = "ci", namespaceURI = CameraInfo.NS ),
    @javax.xml.bind.annotation.XmlNs( prefix = "ci-s", namespaceURI = CameraInfo.Stub.NS_STUB ),
 
     @javax.xml.bind.annotation.XmlNs( prefix = "detail", namespaceURI = Detail.NS ),
    @javax.xml.bind.annotation.XmlNs( prefix = "detail-s", namespaceURI = Detail.Stub.NS_STUB ),
 
     @javax.xml.bind.annotation.XmlNs( prefix = "email", namespaceURI = Email.NS ),
     @javax.xml.bind.annotation.XmlNs( prefix = "email-stub", namespaceURI = Email.Stub.NS_STUB ),
 
     @javax.xml.bind.annotation.XmlNs( prefix = "group", namespaceURI = Group.NS ),
     @javax.xml.bind.annotation.XmlNs( prefix = "group-stub", namespaceURI = Group.Stub.NS_STUB ),
 
     @javax.xml.bind.annotation.XmlNs( prefix = "user", namespaceURI = User.Jaxb.NS ),
    @javax.xml.bind.annotation.XmlNs( prefix = "user-s", namespaceURI = User.Stub.NS_STUB ),
     @javax.xml.bind.annotation.XmlNs( prefix = "users", namespaceURI = User.Collection.NS_COLLECTION )
 
   },
   elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED )
 package com.cedarsoft.rest.sample.jaxb;
