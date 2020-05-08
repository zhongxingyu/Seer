 /*
  * Copyright 2012 Joseph Spencer.
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
  */
 
 package com.betterjargs.elements;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  * @author Joseph Spencer
  */
 public class NestedElement extends Element {
    private String defaultValue;
    private String fieldName;
    private String fieldType;
    private boolean isAntProperty;
    private boolean isAntTask;
    private String name;
    private boolean overwrite;
    private boolean required;
    protected String type;
 
    private static boolean hasFilesType;
 
    public void setDefault(final String defaultVal) throws Exception {
       if(defaultVal != null){
          addQueuedAttribute(new QueuedAttribute() {
             @Override
             public void handle() {
                if(type != null){
                   switch(type){
                   case "boolean":
                      if(getBoolean(defaultVal)){
                         defaultValue = "true";
                      }
                     break;
                   case "int":
                      if(getInt(defaultVal)){
                         defaultValue = defaultVal;
                      }
                     break;

                  default:
                     defaultValue = "null";
                     break;
                   }
                }
             }
          });
       }
    }
    public String getDefault(){return defaultValue;}
    public boolean hasDefault(){return defaultValue != null;}
 
    public void setIsAntTask(boolean isAntTask) {
       if(!this.isAntTask){
          this.isAntTask=isAntTask;
       }
    }
    public boolean getIsAntTask(){return isAntTask;}
 
    public void setIsAntProperty(boolean isAntProperty) {
       if(!this.isAntProperty){
          this.isAntProperty=isAntProperty;
       }
    }
    public boolean getIsAntProperty(){return isAntProperty;}
 
    public final void setName(String name) throws Exception {
       dup(this.name);
       empty(name);
       uniqueName(name);
       this.name=name;
       this.fieldName = name.toLowerCase().replaceAll("[^a-z]", "");
    }
    public final String getName(){return name;}
    public final boolean hasName(){return name!=null;}
 
    public void setOverwrite(String type) throws Exception {
       this.overwrite=getBoolean(type);
    }
    public boolean getOverwrite(){return overwrite;}
 
    public void setRequired(String required) throws Exception {
       this.required=getBoolean(required);
    }
    public boolean getRequired(){return required;}
 
    public final void setType(String type) throws Exception {
       if("files".equals(type)){
          if(hasFilesType){
             throw new IllegalArgumentException("The files type may not be used more than once.");
          }
          hasFilesType = true;
       }
       dup(this.type);
       empty(type);
       this.type=type.toLowerCase().replaceAll("\\s+", "");
       this.fieldType=convertType(type);
    }
    public final String getType(){return type;}
    public final boolean hasType(){return type!=null;}
 
    public final String getFieldName(){return fieldName;}
    public final String getFieldType(){return fieldType;}
 
 }
