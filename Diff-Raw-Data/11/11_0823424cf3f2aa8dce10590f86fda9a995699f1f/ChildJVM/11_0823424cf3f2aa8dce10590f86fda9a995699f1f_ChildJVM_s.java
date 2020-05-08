 /*
  * Copyright (c) 2013 Christian Gleissner.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * - Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  *
  * - Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  *
  * Neither the name of the jisolate nor the names of its contributors may
  * be used to endorse or promote products derived from this software without
  * specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
  * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package org.kevoree.watchdog.child.jvm;
 
 import org.kevoree.watchdog.RuntimeDowloader;
 import org.kevoree.watchdog.Version;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 public class ChildJVM {
 
     public static class Builder {
 
         private final ChildJVM template = new ChildJVM();
 
         public Process isolate() {
             return template.isolate();
         }
 
         public Builder withAdditionalCommandLineArguments(
                 Collection<String> additionalCommandLineArguments) {
             template.additionalCommandLineArguments = new ArrayList<String>(additionalCommandLineArguments);
             return this;
         }
 
         public Builder withInheritClassPath(boolean inheritClassPath) {
             template.inheritClasspath = inheritClassPath;
             return this;
         }
 
         public Builder withInheritSystemProperties(List<String> inheritedSystemPropertyNames) {
             template.inheritedSystemPropertyNames = new ArrayList<String>(inheritedSystemPropertyNames);
             return this;
         }
 
         public Builder withMainClassArguments(Collection<String> mainClassArguments) {
             template.mainClassArguments = new ArrayList<String>(mainClassArguments);
             return this;
         }
 
         public Builder withMainClassName(String mainClassName) {
             template.mainClassName = mainClassName;
             return this;
         }
     }
 
     private List<String> additionalCommandLineArguments;
     private boolean inheritClasspath = true;
     private List<String> inheritedSystemPropertyNames;
     private List<String> mainClassArguments;
     private String mainClassName;
 
     private ChildJVM() {
     }
 
     public Process isolate() {
         try {
             ProcessBuilder builder = new ProcessBuilder(buildCommandLine());
             final Process process = builder.start();
             return process;
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
     }
 
     private List<String> buildCommandLine() {
 
 
         List<String> commandLine = null;
         commandLine = new ArrayList<String>();
         commandLine.add("java");
 
 
         if (additionalCommandLineArguments != null) {
             commandLine.addAll(additionalCommandLineArguments);
         }
         if (System.getProperty("java.vm.vendor").toLowerCase().contains("robovm")) {
             File localJar = new File(Version.NAME + "-" + Version.VERSION + ".jar");
             if (localJar.exists()) {
                 commandLine.add("-cp");
                 commandLine.add(getClasspath() + File.pathSeparator + localJar.getAbsolutePath());
             }
         } else {
             if (inheritClasspath) {
                 commandLine.add("-cp");
                 commandLine.add(getClasspath());
             }
         }
 
         RuntimeDowloader dwl = new RuntimeDowloader();
        commandLine.add("-Xbootclasspath/p:" + dwl.getExtRTJar().getAbsolutePath()+";"+dwl.getSharedResourceAccounting());
         commandLine.add("-javaagent:" + dwl.getExtAgent().getAbsolutePath());
 
         if (inheritedSystemPropertyNames != null && !inheritedSystemPropertyNames.isEmpty()) {
             commandLine.addAll(getInheritedSystemProperties());
         }
         commandLine.add(mainClassName);
         if (!mainClassArguments.isEmpty()) {
             commandLine.addAll(mainClassArguments);
         }
 
         System.out.println("Command line : "+commandLine);
 
         return commandLine;
     }
 
     private String getClasspath() {
         return ClassPathUtil.getClassPath();
     }
 
     private Collection<String> getInheritedSystemProperties() {
         List<String> systemProperties = new ArrayList<String>();
         for (String systemPropertyName : inheritedSystemPropertyNames) {
             systemProperties.add(String.format("-D%s=%s", systemPropertyName,
                     System.getProperty(systemPropertyName)));
         }
         return systemProperties;
     }
 
 }
