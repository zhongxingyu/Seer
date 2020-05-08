 /*
  * ###
  * Phresco Commons
  *
  * Copyright (C) 1999 - 2012 Photon Infotech Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ###
  */
 package com.photon.phresco.commons.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 
 import com.photon.phresco.util.SizeConstants;
 
 @SuppressWarnings("restriction")
 @XmlRootElement
 public class SettingsTemplate extends CustomerBasedElement {
     
     private static final long serialVersionUID = 1L;
     
     //Database, Server, Email
     private String type;
     
     //List of properties available for the template
     private List<PropertyTemplate> properties = new ArrayList<PropertyTemplate>(SizeConstants.SIZE_PROPERTIES_MAP);
     //List of technology ids
     private List<Element> appliesToTechs;
     private List<Element> possibleTypes;
     
     public SettingsTemplate() {
         super();
     }
 
 	public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public List<PropertyTemplate> getProperties() {
         return properties;
     }
 
     public void setProperties(List<PropertyTemplate> properties) {
         this.properties = properties;
     }
 
     public List<Element> getAppliesToTechs() {
         return appliesToTechs;
     }
 
     public void setAppliesToTechs(List<Element> appliesTo) {
         this.appliesToTechs = appliesTo;
     }
     
     public List<Element> getPossibleTypes() {
 		return possibleTypes;
 	}
 
 	public void setPossibleTypes(List<Element> types) {
 		this.possibleTypes = types;
 	}
 
 	@Override
     public String toString() {
         return new ToStringBuilder(this,
                 ToStringStyle.DEFAULT_STYLE)
                 .append(super.toString())
                 .append("type", getType())
                 .append("properties", getProperties())
                 .append("appliesToTechs", getAppliesToTechs())
                 .toString();
     }
 
 }
