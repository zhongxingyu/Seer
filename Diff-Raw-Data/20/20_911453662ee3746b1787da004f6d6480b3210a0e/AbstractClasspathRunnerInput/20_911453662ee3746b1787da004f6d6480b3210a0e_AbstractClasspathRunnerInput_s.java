 /**
  * This file is part of the XP-Framework
  *
  * Maven plugin for XP-Framework
  * Copyright (c) 2011, XP-Framework Team
  */
 package net.xp_forge.maven.plugins.xp.runners.input;
 
 import java.io.File;
 import java.util.Set;
 import java.util.List;
 import java.util.Iterator;
 import java.util.ArrayList;
 
 import org.apache.maven.artifact.Artifact;
 
 /**
  * Runner input with classpath configuration
  *
  */
 public class AbstractClasspathRunnerInput {
   public List<String> classpaths;
   public boolean verbose;
 
 
   /**
    * Constructor
    *
    */
   public AbstractClasspathRunnerInput() {
     this.classpaths = new ArrayList<String>();
     this.verbose    = false;
   }
 
   /**
    * Setter for classpaths
    *
    * @param  java.util.Set<org.apache.maven.artifact.Artifact> artifacts
    * @return void
    */
   public void addClasspath(Set<Artifact> artifacts) {
     for (Artifact artifact : artifacts) {
 
       // Add to classpath
      if (artifact.getClassifier().equals("patch")) {
         this.addClasspath("!" + artifact.getFile().getAbsolutePath());
       } else {
         this.addClasspath(artifact.getFile());
       }
     }
   }
 
   /**
    * Setter for classpaths
    *
    * @param  java.util.List<java.lang.String> classpaths
    * @return void
    */
   public void addClasspath(List<String> classpaths) {
     if (null == classpaths) return;
     for (String classpath : classpaths) {
       this.addClasspath(classpath);
     }
   }
 
   /**
    * Setter for classpaths
    *
    * @param  java.lang.String classpath
    * @return void
    */
   public void addClasspath(String classpath) {
 
     // Check classpath not added twice
     for (String cp : this.classpaths) {
       if (cp.equals(classpath)) {
         return;
       }
     }
 
     // Add to list
     this.classpaths.add(classpath);
   }
 
   /**
    * Setter for classpaths
    *
    * @param  java.io.File file Archive to add to classpath
    * @return void
    */
   public void addClasspath(File file) {
     if (!file.exists()) return;
 
     // Add to list
     this.addClasspath(file.getAbsolutePath());
   }
 }
