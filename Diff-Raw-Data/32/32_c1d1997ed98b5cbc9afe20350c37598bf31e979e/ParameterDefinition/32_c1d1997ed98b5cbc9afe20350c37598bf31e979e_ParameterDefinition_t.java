 /*
  * Copyright (c) 2010-2011, Dmitry Sidorenko. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.googlecode.commandme.impl.introspector;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Dmitry Sidorenko
  */
 public final class ParameterDefinition {
     @SuppressWarnings({"UnusedDeclaration"})
     private static final Logger LOGGER = LoggerFactory.getLogger(ParameterDefinition.class);
 
     private String  longName;
     private String  shortName;
     private Class   type;
     private String  defaultValue;
     private String  description;
     private boolean showInHelp;
 
     public ParameterDefinition() {
     }
 
     public void setLongName(String longName) {
         if (this.longName != null) {
             throw new IllegalStateException("LongName can't be set twice");
         }
         this.longName = longName;
     }
 
     public void setShortName(String shortName) {
         if (this.shortName != null) {
             throw new IllegalStateException("ShortName can't be set twice");
         }
         this.shortName = shortName;
     }
 
     public void setType(Class type) {
         this.type = type;
     }
 
     public void setDefaultValue(String defaultValue) {
         this.defaultValue = defaultValue;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public void setShowInHelp(boolean showInHelp) {
         this.showInHelp = showInHelp;
     }
 
     public String getLongName() {
         return longName;
     }
 
     public String getShortName() {
         return shortName;
     }
 
     public Class getType() {
         return type;
     }
 
     public String getDefaultValue() {
         return defaultValue;
     }
 
     public String getDescription() {
         return description;
     }
 
     public boolean showInHelp() {
         return showInHelp;
     }
 
     @Override
    @SuppressWarnings({"RedundantIfStatement"})
     public boolean equals(Object o) {
         if (this == o) {
             return true;
         }
         if (o == null || getClass() != o.getClass()) {
             return false;
         }
 
         ParameterDefinition that = (ParameterDefinition) o;
 
         if (showInHelp != that.showInHelp) {
             return false;
         }
         if (defaultValue != null ? !defaultValue.equals(that.defaultValue) : that.defaultValue != null) {
             return false;
         }
         if (description != null ? !description.equals(that.description) : that.description != null) {
             return false;
         }
         if (longName != null ? !longName.equals(that.longName) : that.longName != null) {
             return false;
         }
         if (shortName != null ? !shortName.equals(that.shortName) : that.shortName != null) {
             return false;
         }
         if (type != null ? !type.equals(that.type) : that.type != null) {
             return false;
         }
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return longName != null ? longName.hashCode() : 0;
     }
 
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder();
         sb.append("ParameterDefinition");
         sb.append("{longName='").append(longName).append('\'');
         sb.append(", shortName='").append(shortName).append('\'');
         sb.append(", type=").append(type);
         sb.append(", defaultValue='").append(defaultValue).append('\'');
         sb.append(", description='").append(description).append('\'');
         sb.append(", showInHelp=").append(showInHelp);
         sb.append('}');
         return sb.toString();
     }
 }
