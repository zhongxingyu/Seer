 /**
  * Copyright 2012 Maroun Baydoun
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
 *
  */
 package com.mb.intervention.context;
 
 import com.mb.intervention.log.LocalizedLogger;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 public class Context{
 
     private Configuration configuration;
     private Map<String, ContextEntry> contextEntires;
     
 
     public Context() {
 
         contextEntires = new HashMap<String, ContextEntry>();
         
     }
 
     public void addContextEntry(ContextEntry entry) {
 
         contextEntires.put(entry.dynamicClassId, entry);
     }
 
     public Map<String, ContextEntry> getContextEntires() {
         return contextEntires;
     }
 
     public ContextEntry getContextEntry(String dynamicClassId) {
         return contextEntires.get(dynamicClassId);
     }
 
     public boolean containsContextEntry(String dynamicClassId) {
         return contextEntires.containsKey(dynamicClassId);
     }
 
     
     public void setConfiguration(Configuration configuration) {
         this.configuration = configuration;
     }
 
     public Configuration getConfiguration() {
         return configuration;
     }
 
     public static class ContextEntry {
 
         private String dynamicClassId;
         private Class<?> dynamicClass;
         private InterceptionPolicy interceptionPolicy;
         private String script;
         private List<String> excludedMethods;
 
         public ContextEntry() {
         }
 
         public Class<?> getDynamicClass() {
             return dynamicClass;
         }
 
         public String getDynamicClassId() {
             return dynamicClassId;
         }
 
         public InterceptionPolicy getInterceptionPolicy() {
             return interceptionPolicy;
         }
 
         public String getScript() {
             return script;
         }
 
         public List<String> getExcludedMethods() {
             return excludedMethods;
         }
         
         
 
         public void setDynamicClass(Class<?> dynamicClass) {
             this.dynamicClass = dynamicClass;
         }
 
         public void setDynamicClassId(String dynamicClassId) {
             this.dynamicClassId = dynamicClassId;
         }
 
         public void setInterceptionPolicy(InterceptionPolicy interceptionPolicy) {
             this.interceptionPolicy = interceptionPolicy;
         }
 
         public void setScript(String script) {
             this.script = script;
         }
         
         public void addExcludedMethod(String methodName) {
             if(excludedMethods==null){
                 excludedMethods=new ArrayList<String>();
             }
             
             excludedMethods.add(methodName);
 
         }
     }
 
     public static class Configuration {
 
         private String defaultScript;
         private String dynamicLanguage;
         private String scriptExtension;
         private String preInvokeFunction;
         private String postInvokeFunction;
         private String scriptLocation;
         private String scriptLocationType;
         private InterceptionPolicy interceptionPolicy;
         private static Configuration defaultConfiguration;
         
         public static final String SCRIPT_LOCATION_CLASSPATH="classpath";
         public static final String SCRIPT_LOCATION_FOLDER="folder";
         public static final String SCRIPT_LOCATION_DELIMITER=":";
 
         public static Configuration getDefault() {
 
 
             if (defaultConfiguration == null) {
 
                 try {
                     Properties properties = new Properties();
                    properties.load(AnnotationContextProvider.class.getResourceAsStream("../defaults.properties"));
 
                     defaultConfiguration = new Configuration();
                     defaultConfiguration.setDefaultScript(properties.getProperty("defaultScript"));
                     defaultConfiguration.setDynamicLanguage(properties.getProperty("dynamicLanguage"));
                     defaultConfiguration.setScriptExtension(properties.getProperty("scriptExtension"));
                     defaultConfiguration.setPostInvokeFunction(properties.getProperty("postInvokeFunction"));
                     defaultConfiguration.setPreInvokeFunction(properties.getProperty("preInvokeFunction"));
                     defaultConfiguration.setInterceptionPolicy(InterceptionPolicy.valueOf(properties.getProperty("interceptionPolicy").toUpperCase()));
                     defaultConfiguration.setScriptLocation(properties.getProperty("scriptLocation"));
                     
                 } catch (IOException ex) {
                     LocalizedLogger.severe(Configuration.class.getName(), "exception_occurred", ex.getMessage());
                 }
             }
 
             return defaultConfiguration;
         }
         
         public Configuration merge(Configuration other){
             
             Configuration result=new  Configuration();
             
             result.defaultScript = (defaultScript == null || defaultScript.length()==0 ? other.defaultScript : defaultScript);
             result.dynamicLanguage = (dynamicLanguage == null || dynamicLanguage.length()==0 ? other.dynamicLanguage : dynamicLanguage);
             result.interceptionPolicy = (interceptionPolicy == null ? other.interceptionPolicy : interceptionPolicy);
             result.postInvokeFunction = (postInvokeFunction == null || preInvokeFunction.length()==0 ? other.postInvokeFunction : postInvokeFunction);
             result.preInvokeFunction = (preInvokeFunction == null || preInvokeFunction.length()==0 ? other.preInvokeFunction : preInvokeFunction);
             result.scriptExtension = (scriptExtension == null || scriptExtension.length()==0 ? other.scriptExtension : scriptExtension);
             result.scriptLocation = (scriptLocation == null || scriptLocation.length()==0 ? other.scriptLocation : scriptLocation);
             result.scriptLocationType=(scriptLocationType == null ? other.scriptLocationType : scriptLocationType);
            
             return result;
         }
 
         public String getDefaultScript() {
             return defaultScript;
         }
 
         public String getDynamicLanguage() {
             return dynamicLanguage;
         }
 
         public String getScriptExtension() {
             return scriptExtension;
         }
 
         public InterceptionPolicy getInterceptionPolicy() {
             return interceptionPolicy;
         }
 
         public String getPostInvokeFunction() {
             return postInvokeFunction;
         }
 
         public String getPreInvokeFunction() {
             return preInvokeFunction;
         }
 
         public String getScriptLocation() {
             return scriptLocation;
         }
 
         public String getScriptLocationType() {
             return scriptLocationType;
         }
         
         
 
         
         public void setDefaultScript(String defaultScript) {
             this.defaultScript = defaultScript;
         }
 
         public void setDynamicLanguage(String dynamicLanguage) {
             this.dynamicLanguage = dynamicLanguage;
         }
 
         public void setScriptExtension(String scriptExtension) {
             this.scriptExtension = scriptExtension;
         }
 
         public void setInterceptionPolicy(InterceptionPolicy interceptionPolicy) {
             this.interceptionPolicy = interceptionPolicy;
         }
 
         public void setPostInvokeFunction(String postInvokeFunction) {
             this.postInvokeFunction = postInvokeFunction;
         }
 
         public void setPreInvokeFunction(String preInvokeFunction) {
             this.preInvokeFunction = preInvokeFunction;
         }
 
         public void setScriptLocation(String scriptLocation) {
             this.scriptLocation = scriptLocation;
             
            if(scriptLocation!=null){
                 this.scriptLocationType=scriptLocation.substring(0,scriptLocation.indexOf(SCRIPT_LOCATION_DELIMITER));
                 this.scriptLocation=scriptLocation.substring(scriptLocationType.length()+Context.Configuration.SCRIPT_LOCATION_DELIMITER.length());
             }
             
         }
         
         
     }
 }
