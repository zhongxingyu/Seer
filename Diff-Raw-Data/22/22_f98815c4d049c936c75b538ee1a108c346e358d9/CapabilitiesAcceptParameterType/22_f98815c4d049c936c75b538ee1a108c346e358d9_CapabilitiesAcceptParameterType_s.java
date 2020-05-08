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
 package org.bedework.util.timezones.model;
 
 import org.bedework.util.misc.ToString;
 
 /**
  *
  *         This defines the name, type and characteristics of an operation
  *         parameter.
  *
  *
  * <pre>
    ; Object defining an action parameter
    parameter = {
      param_name,
      ?param_required,
      ?param_multi,
      ?param_values
    }
 
    ; Name of the parameter
    param_name  "name" : string
 
    ; If true the parameter has to be present in the request-URI
    ; default is false
    param_required "required" : boolean
 
    ; If true the parameter can occur more than once in the request-URI
    ; default is false
    param_multi "multi" : boolean,
 
    ; An array that defines the allowed set of values for the parameter
    ; In the absence of this member, any string value is acceptable
    param_values "values" : [ * : string ]
  * </pre>
  *
  *
  */
 public class CapabilitiesAcceptParameterType {
   protected String name;
   protected boolean required;
   protected boolean multi;
  protected String value;
 
   /**
    * Gets the value of the name property.
    *
    * @return
    *     possible object is
    *     {@link String }
    *
    */
   public String getName() {
     return name;
   }
 
   /**
    * Sets the value of the name property.
    *
    * @param value
    *     allowed object is
    *     {@link String }
    *
    */
   public void setName(final String value) {
     name = value;
   }
 
   /**
    * Gets the value of the required property.
    *
    * @return true for a required param
    */
   public boolean isRequired() {
     return required;
   }
 
   /**
    * Sets the value of the required property.
    *
    * @param value
    */
   public void setRequired(final boolean value) {
     required = value;
   }
 
   /**
    * Gets the value of the multi property.
    *
    * @return true for multi-valued
    */
   public boolean isMulti() {
     return multi;
   }
 
   /**
    * Sets the value of the multi property.
    *
    * @param value
    */
   public void setMulti(final boolean value) {
     multi = value;
   }
 
   /**
    * Gets the value of the value property.
    *
    * @return
    *     possible object is
    *     {@link String }
    *
    */
  public String getValue() {
    return value;
   }
 
   /**
    * Sets the value of the value property.
    *
    * @param value
    *     allowed object is
    *     {@link String }
    *
    */
  public void setValue(final String value) {
    this.value = value;
   }
 
   @Override
   public String toString() {
     ToString ts = new ToString(this);
 
     ts.append("name", getName());
     ts.append("required", isRequired());
     ts.append("multi", isMulti());
    ts.append("value", getValue());
 
     return ts.toString();
   }
 }
