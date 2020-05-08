 /*
  * Copyright 2010-2011, CloudBees Inc.
  */
 
 package com.cloudbees.sdk.commands.config.model;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamImplicit;
import sun.security.krb5.Config;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * @author Fabian Donze
  */
 @XStreamAlias("config")
 public class ConfigParameters {
     @XStreamImplicit(itemFieldName = "resource")
     private List<ResourceSettings> resources;
 
     @XStreamImplicit(itemFieldName = "env")
     private List<Environment> environments;
 
     @XStreamImplicit(itemFieldName="param")
     private List<ParameterSettings> parameters;
 
     public ConfigParameters() {
     }
 
     public List<Environment> getEnvironments() {
         if (environments == null)
             environments = new ArrayList<Environment>();
         return environments;
     }
 
     public void setEnvironments(List<Environment> environments) {
         this.environments = environments;
     }
 
     public void setEnvironment(Environment environment) {
         deleteEnvironment(environment.getName());
         getEnvironments().add(environment);
     }
 
     public Environment getEnvironment(String env) {
         for (Environment environment: getEnvironments()) {
             if (env.equals(environment.getName()))
                 return environment;
         }
         return null;
     }
     public void deleteEnvironment(String name) {
         Iterator<Environment> it = getEnvironments().iterator();
         while (it.hasNext()) {
             if (it.next().getName().equals(name)) {
                 it.remove();
             }
         }
     }
 
 
     public List<ResourceSettings> getResources() {
         if (resources == null)
             resources = new ArrayList<ResourceSettings>();
         return resources;
     }
 
     public void setResources(List<ResourceSettings> resources) {
         this.resources = resources;
     }
 
     public void setResource(ResourceSettings resource) {
         deleteResource(resource.getName());
         getResources().add(resource);
     }
 
     public ResourceSettings getResource(String name) {
         for (ResourceSettings resource : getResources()) {
             if (resource.getName().equals(name))
                 return resource;
         }
         return null;
     }
     public void deleteResource(String name) {
         Iterator<ResourceSettings> it = getResources().iterator();
         while (it.hasNext()) {
             if (it.next().getName().equals(name)) {
                 it.remove();
             }
         }
     }
 
     public List<ParameterSettings> getParameters() {
         if (parameters == null)
             parameters = new ArrayList<ParameterSettings>();
         return parameters;
     }
 
     public void setParameters(List<ParameterSettings> parameters) {
         this.parameters = parameters;
     }
 
     public void deleteParameter(String name) {
         Iterator<ParameterSettings> it = getParameters().iterator();
         while (it.hasNext()) {
             if (it.next().getName().equals(name)) {
                 it.remove();
             }
         }
     }
     public void setParameter(ParameterSettings parameter) {
         deleteParameter(parameter.getName());
         getParameters().add(parameter);
     }
 
     public ParameterSettings getParameter(String name) {
         for (ParameterSettings resource : getParameters()) {
             if (resource.getName().equals(name))
                 return resource;
         }
         return null;
     }
 
 
     private static XStream createXStream() {
         XStream xstream = new XStream();
        xstream.setClassLoader(ConfigParameters.class.getClassLoader());
         xstream.processAnnotations(ParameterSettings.class);
         xstream.processAnnotations(ResourceSettings.class);
         xstream.processAnnotations(Environment.class);
         xstream.processAnnotations(ConfigParameters.class);
         return xstream;
     }
 
     public String toXML() {
         return createXStream().toXML(this);
     }
 
     public static ConfigParameters parse(String xml) {
         if (xml == null)
             return new ConfigParameters();
         return (ConfigParameters) createXStream().fromXML(xml);
     }
 
 }
