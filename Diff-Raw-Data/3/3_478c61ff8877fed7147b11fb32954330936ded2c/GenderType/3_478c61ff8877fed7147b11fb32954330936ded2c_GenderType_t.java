 /**
  * Copyright 2011 Intellectual Reserve, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.gedcomx.types;
 
 import org.codehaus.enunciate.qname.XmlQNameEnum;
 import org.codehaus.enunciate.qname.XmlUnknownQNameEnumValue;
 
 import java.net.URI;
 
 /**
  * Enumeration of known gender types.
  *
  * @author Ryan Heaton
  */
 @XmlQNameEnum (
   base = XmlQNameEnum.BaseType.URI
 )
 public enum GenderType {
 
   /**
    * Male.
    */
   male,
 
   /**
    * Female.
    */
   female,
 
   /**
   * Unknown. Note that this should be used strictly as "unknown" and not to
   * indicate a type that is not set or not understood.
    */
   unknown,
 
   /**
    * Custom
    */
   @XmlUnknownQNameEnumValue
   other;
 
   /**
    * Return the QName value for this enum.
    *
    * @return The QName value for this enum.
    */
   public URI toQNameURI() {
     return org.codehaus.enunciate.XmlQNameEnumUtil.toURI(this);
   }
 
   /**
    * Get the enumeration from the QName.
    *
    * @param qname The qname.
    * @return The enumeration.
    */
   public static GenderType fromQNameURI(URI qname) {
     return org.codehaus.enunciate.XmlQNameEnumUtil.fromURI(qname, GenderType.class);
   }
 
 }
