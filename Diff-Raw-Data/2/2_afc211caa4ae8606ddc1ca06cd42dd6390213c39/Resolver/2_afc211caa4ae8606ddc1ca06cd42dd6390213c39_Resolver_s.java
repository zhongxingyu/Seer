 /*
  * Copyright (c) 2011 Henrik Lynggaard Hansen 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Henrik Lynggaard Hansen
  */
 package dk.hlyh.hudson.tools.jobcreator;
 
 import dk.hlyh.hudson.tools.jobcreator.helper.HierarchyWalker;
 import dk.hlyh.hudson.tools.jobcreator.helper.IncludedJobsVisitor;
 import dk.hlyh.hudson.tools.jobcreator.helper.ResolvePropertyVisitor;
 import dk.hlyh.hudson.tools.jobcreator.helper.TemplateValuesBuilder;
 import dk.hlyh.hudson.tools.jobcreator.schema.v1.Environment;
 import dk.hlyh.hudson.tools.jobcreator.schema.v1.Job;
 import dk.hlyh.hudson.tools.jobcreator.schema.v1.Pipeline;
 import dk.hlyh.hudson.tools.jobcreator.schema.v1.Propagation;
 import dk.hlyh.hudson.tools.jobcreator.schema.v1.Property;
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 public class Resolver {
 
   private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
   private final Arguments arguments;
   private final Pipeline pipeline;
 
   public Resolver() {
     super();
     arguments = null;
     pipeline = null;
   }
 
   public Resolver(Arguments arguments, Pipeline pipeline) {
     this.arguments = arguments;
     this.pipeline = pipeline;
   }
 
  public void resolve() throws IOException, TemplateException {
 
     // Find the active environment
     Environment activeEnvironment = findEnvironment();
     if (activeEnvironment == null) {
       return;
     }
     LOGGER.info("Active environment: " + activeEnvironment);
 
     // get The list of jobs
     IncludedJobsVisitor includedJobsVisitor = new IncludedJobsVisitor();
     HierarchyWalker.walkEnvironments(activeEnvironment, includedJobsVisitor);
     Set<Job> jobs = includedJobsVisitor.getJobs();
     LOGGER.info("Jobs to build: " + jobs);
 
     // build the environment properties
     ResolvePropertyVisitor environmentPropertyVisitor = new ResolvePropertyVisitor(activeEnvironment);
     HierarchyWalker.walkEnvironments(activeEnvironment, environmentPropertyVisitor);
 
 
     // loop all jobs and set initial properties
     for (Job job : jobs) {
       LOGGER.info("Resolving job: " + job);
       ResolvePropertyVisitor visitor = new ResolvePropertyVisitor(job);
       HierarchyWalker.walkJobs(job, visitor);
       job.getUpstreamJobs().retainAll(jobs);
       job.getDownstreamJobs().retainAll(jobs);
     }
 
     // Apply environment properties
     for (Job job : jobs) {
 
       Map<String, Property> globalSet = activeEnvironment.getResolvesProperties().get("global");
       Map<String, Property> jobSpecificSet = activeEnvironment.getResolvesProperties().get(job.getName());
       Map<String, Property> currentSet = job.getResolvedProperties();
       if (globalSet != null) {
         LOGGER.info("Applying global environment properties to " + job);
         mergePropertySet(globalSet, currentSet);
       }
       if (jobSpecificSet != null) {
         LOGGER.info("Applying job specific environment properties to " + job);
         mergePropertySet(jobSpecificSet, currentSet);
       }
     }
 
     // loop all jobs and propagate
     for (Job job : jobs) {
       if (job.getUpstreamJobs().isEmpty()) {
         Map<String, Property> pushedProperties = new HashMap<String, Property>();
         pushDownstream(pushedProperties, job);
       }
     }
     for (Job job : jobs) {
       if (job.getDownstreamJobs().isEmpty()) {
         Map<String, Property> pushedProperties = new HashMap<String, Property>();
         pushUpstream(pushedProperties, job);
       }
     }
 
     // Loop all jobs and and build the template model
     for (Job job : jobs) {
       LOGGER.info("Processing job: " + job);
       TemplateValuesBuilder builder = new TemplateValuesBuilder();
       builder.setProperty("import.pipeline.name", pipeline.getName());
       builder.setProperty("import.env.name", arguments.getEnvironment());
       builder.setProperty("import.jobs", jobListAsString(jobs));
       builder.setProperty("import.job.upstream", jobListAsString(job.getUpstreamJobs()));
       builder.setProperty("import.job.downstream", jobListAsString(job.getDownstreamJobs()));
       for (Property property : job.getResolvedProperties().values()) {
         builder.setProperty(property.getName(), property.getValue());
       }
 
       // merge with template
       String outputName = activeEnvironment.getResolvesOutputPattern();
       outputName = outputName.replace("${environment}", activeEnvironment.getName());
       outputName = outputName.replace("${pipeline}", pipeline.getName());
       outputName = outputName.replace("${job}", job.getName());
       File jobDirectory = new File(arguments.getOutputDirectory(), outputName);
       if (!jobDirectory.exists()) {
         jobDirectory.mkdir();
       }
       File configXml = new File(jobDirectory, "config.xml");
 
       // merge with template
       Configuration cfg = setupFreemarker();
 
       Template template = cfg.getTemplate(job.getResolvedTemplate());
       Writer out = new OutputStreamWriter(new FileOutputStream(configXml));
       template.process(builder.getValues(), out);
       System.out.println("Completed  job: " + job.getName());
       out.close();
     }
   }
 
   private Environment findEnvironment() {
     for (Environment currentEnvironment : pipeline.getEnvironments()) {
       if (arguments.getEnvironment().equals(currentEnvironment.getName())) {
         return currentEnvironment;
       }
     }
     return null;
   }
 
   private void pushUpstream(Map<String, Property> pushedProperties, Job job) {
     Map<String, Property> pushedPropertiesDownstream = propagate(pushedProperties, job, Propagation.Downstream);
 
 
     for (Job upstreamJob : job.getUpstreamJobs()) {
       pushUpstream(pushedPropertiesDownstream, upstreamJob);
     }
   }
 
   private void pushDownstream(Map<String, Property> pushedProperties, Job job) {
     Map<String, Property> pushedPropertiesDownstream = propagate(pushedProperties, job, Propagation.Downstream);
 
 
     for (Job downstreamJob : job.getDownstreamJobs()) {
       pushDownstream(pushedPropertiesDownstream, downstreamJob);
     }
   }
 
   private Map<String, Property> propagate(Map<String, Property> pushedProperties, Job job, Propagation direction) {
     Map<String, Property> furtherPushedProperties = new HashMap<String, Property>();
     Map<String, Property> resolvedProperties = job.getResolvedProperties();
 
     // apply currently pushed properties
     List<String> toRemove = new ArrayList<String>();
     for (Property pushed : pushedProperties.values()) {
       Property currentProperty = resolvedProperties.get(pushed.getName());
       if (currentProperty == null) {
         resolvedProperties.put(pushed.getName(), pushed);
         continue;
       }
 
       switch (currentProperty.getPropagation()) {
         case Skip:
           // then we should not do anyting
           break;
         case Stop:
         // fallthrough
         case Upstream:
           toRemove.add(pushed.getName());
           break;
         case None:
           switch (pushed.getMerging()) {
             case Leave:
               // empty, don't replace existing values
               break;
             case Append:
               String appendedValue = currentProperty.getValue() + "," + pushed.getValue();
               Property appendedProperty = new Property(currentProperty);
               appendedProperty.setValue(appendedValue);
               resolvedProperties.put(appendedProperty.getName(), appendedProperty);
               break;
             case Prefix:
               String prefixedValue = pushed.getValue() + "," + currentProperty.getValue();
               Property prefixedProperty = new Property(currentProperty);
               prefixedProperty.setValue(prefixedValue);
               resolvedProperties.put(prefixedProperty.getName(), prefixedProperty);
               break;
             case Replace:
               String replacedValue = pushed.getValue();
               Property replacedProperty = new Property(currentProperty);
               replacedProperty.setValue(replacedValue);
               resolvedProperties.put(replacedProperty.getName(), replacedProperty);
               break;
           }
           break;
       }
     }
 
 
     furtherPushedProperties.putAll(pushedProperties);
     for (String name : toRemove) {
       furtherPushedProperties.remove(name);
     }
     for (Property property : job.getResolvedProperties().values()) {
       if (property.getPropagation() == direction) {
         furtherPushedProperties.put(property.getName(), property);
       }
     }
 
     return furtherPushedProperties;
   }
 
   private void mergePropertySet(Map<String, Property> source, Map<String, Property> destination) {
     if (source == null) {
       return;
     }
     for (Property sourceProperty : source.values()) {
       destination.put(sourceProperty.getName(), sourceProperty);
     }
   }
 
   private String jobListAsString(Set<Job> jobs) {
     StringBuilder sb = new StringBuilder();
     for (Job job : jobs) {
       if (sb.length() > 0) {
         sb.append(',');
       }
       sb.append(job.getName());
     }
     return sb.toString();
   }
 
   private Configuration setupFreemarker() throws IOException {
     Configuration cfg = new Configuration();
     cfg.setDirectoryForTemplateLoading(arguments.getTemplateDirectory());
     cfg.setObjectWrapper(new DefaultObjectWrapper());
     return cfg;
   }
 }
