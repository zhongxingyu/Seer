 /**************************************************************************************
  * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
  * http://fusesource.com                                                              *
  * ---------------------------------------------------------------------------------- *
  * The software in this package is published under the terms of the AGPL license      *
  * a copy of which has been included with this distribution in the license.txt file.  *
  **************************************************************************************/
 package org.fusesource.cloudmix.agent.mop;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.fusesource.cloudmix.common.dto.ProvisioningAction;
 import org.fusesource.mop.MOP;
 import org.fusesource.mop.ProcessRunner;
import org.fusesource.mop.com.google.common.collect.Lists;
 
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 
 /**
  * @version $Revision: 1.1 $
  */
 public class MopProcess {
     private static final transient Log LOG = LogFactory.getLog(MopProcess.class);
 
     private ProvisioningAction action;
     private String credentials;
     private String commandLine;
     private ClassLoader mopClassLoader;
     private MOP mop = new MOP();
     private int statusCode = -1;
     private Thread thread;
     private AtomicBoolean completed = new AtomicBoolean(false);
 
     public MopProcess(ProvisioningAction action, String credentials, String commandLine, ClassLoader mopClassLoader) {
         this.action = action;
         this.credentials = credentials;
         this.commandLine = commandLine;
         this.mopClassLoader = mopClassLoader;
     }
 
     public String getId() {
         return action.getFeature();
     }
 
     public ProvisioningAction getAction() {
         return action;
     }
 
     public String getCommandLine() {
         return commandLine;
     }
 
     public String getCredentials() {
         return credentials;
     }
 
     public void start() throws Exception {
         final List<String> argList = Lists.newArrayList();
         if (commandLine != null) {
             StringTokenizer iter = new StringTokenizer(commandLine);
             while (iter.hasMoreTokens()) {
                 argList.add(iter.nextToken());
             }
         }
         if (argList.isEmpty()) {
             throw new IllegalArgumentException("No arguments specified");
         }
 
         // lets ensure the first statement is a fork to ensure we spin off into a separate child process
         String first = argList.get(0);
         if (!first.startsWith("fork")) {
             argList.add(0, "fork");
         }
 
 
         // lets transform the class loader to exclude the parent (to avoid maven & jetty plugin dependencies)
         // lets run in a background thread
         thread = new Thread("Feature: " + getId() + "MOP " + argList) {
             @Override
             public void run() {
                 LOG.debug("Using class loader: " + mopClassLoader + " of type: " + mopClassLoader.getClass());
                 dumpClassLoader(mopClassLoader);
 
                 Thread.currentThread().setContextClassLoader(mopClassLoader);
 
                 LOG.info("Starting feature: " + getId() + " via MOP: " + argList);
                 String[] args = argList.toArray(new String[argList.size()]);
                 try {
                     statusCode = mop.executeAndWait(args);
                     LOG.info("Stopped feature: " + getId() + " with status code: " + statusCode);
                 } catch (Exception e) {
                     LOG.error("Failed running feature: " + getId() + ". Reason: " + e, e);
                 }
                 finally {
                     clear();
                 }
             }
         };
         thread.setContextClassLoader(mopClassLoader);
         thread.start();
     }
 
     void dumpClassLoader(ClassLoader cl) {
         if (LOG.isDebugEnabled()) {
             if (cl instanceof URLClassLoader) {
                 URLClassLoader urlClassLoader = (URLClassLoader) cl;
                 URL[] urls = urlClassLoader.getURLs();
                 for (URL url : urls) {
                     LOG.debug("ClassLoader URL: " + url);
                 }
             }
             ClassLoader parent = cl.getParent();
             if (parent != null) {
                 LOG.debug("Parent Class Loader: " + parent);
                 dumpClassLoader(parent);
             }
         }
     }
 
     public void stop() throws Exception {
         if (mop != null) {
             ProcessRunner processRunner = mop.getProcessRunner();
             if (processRunner != null) {
                 processRunner.kill();
             }
         }
         clear();
     }
 
     public boolean isCompleted() {
         return completed.get();
     }
     
     private void clear() {
         completed.set(true);
         mop = null;
         thread = null;
     }
 }
