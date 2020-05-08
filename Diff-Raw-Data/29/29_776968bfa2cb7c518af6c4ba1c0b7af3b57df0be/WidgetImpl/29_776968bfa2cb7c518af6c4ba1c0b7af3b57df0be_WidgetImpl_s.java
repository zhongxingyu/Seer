 /*
  * jMaki Version 2
  *
  * Copyright (c) 2009 jmaki.org
  * 
  * Licensed under the MIT License:
  * 
  *  http://www.opensource.org/licenses/mit-license.php
  *
  */
 package org.jmaki.model.impl;
 
 import java.util.List;
 
 import org.jmaki.model.IWidget;
 
 public class WidgetImpl implements IWidget {
 
   private WidgetConfig wcfg = null;
   private List<ResourceURI> scripts;
   private List<ResourceURI> styles;
   private String uuid;
   private String name;
   private String template;
   private Object value;
   private Object args;
   private String publish;
   private String subscribe;
   private String service;
 
   public WidgetImpl(String name, String uuid) {
       this.uuid = uuid;
       this.name = name;
   }
 
     public String getName() {
         return name;
     }
 
     public String getPublish() {
         return publish;
     }
 
     public List<ResourceURI> getScripts() {
         return scripts;
     }
 
     public String getService() {
         return service;
     }
 
     public List<ResourceURI>  getStyles() {
         return styles;
     }
 
     public String getSubscribe() {
         return subscribe;
     }
 
     public String getTemplate() {
         return template;
     }
     
     public String getUuid() {
         return uuid;
     }
 
     public Object getValue() {
         return value;
     }
 
     public WidgetConfig getWidgetConfig() {
         return wcfg;
     }
     
     public void setWidgetConfig(WidgetConfig config) {
         this.wcfg = config;
     }
     
     public void setScripts(List<ResourceURI> scripts) {
         this.scripts = scripts;
     }
 
     public void setStyles(List<ResourceURI> styles) {
         this.styles = styles;
     }
 
     public void setTemplate(String template) {
         this.template = template;
     }
 
     public void setValue(Object value) {
         this.value = value;
     }
 
     public void setPublish(String publish) {
         this.publish = publish;
     }
 
     public void setSubscribe(String subscribe) {
         this.subscribe = subscribe;
     }
 
     public void setService(String service) {
         this.service = service;
     }
 
     public Object getArgs() {
         return args;
     }
 
     public void setArgs(Object args) {
         this.args = args;
        
     }
 }
