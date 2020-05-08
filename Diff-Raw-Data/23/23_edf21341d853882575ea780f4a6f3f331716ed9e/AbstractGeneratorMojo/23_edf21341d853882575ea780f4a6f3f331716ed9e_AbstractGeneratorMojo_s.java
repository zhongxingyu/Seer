 /**
  * Copyright (C) cedarsoft GmbH.
  *
  * Licensed under the GNU General Public License version 3 (the "License")
  * with Classpath Exception; you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *         http://www.cedarsoft.org/gpl3ce
  *         (GPL 3 with Classpath Exception)
  *
  * This code is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 3 only, as
  * published by the Free Software Foundation. cedarsoft GmbH designates this
  * particular file as subject to the "Classpath" exception as provided
  * by cedarsoft GmbH in the LICENSE file that accompanied this code.
  *
  * This code is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  * version 3 for more details (a copy is included in the LICENSE file that
  * accompanied this code).
  *
  * You should have received a copy of the GNU General Public License version
  * 3 along with this work; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
  * or visit www.cedarsoft.com if you need additional information or
  * have any questions.
  */
 
 package com.cedarsoft.generator.maven;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.jetbrains.annotations.NotNull;
 
 import java.io.File;
 
 /**
  *
  */
 public abstract class AbstractGeneratorMojo extends AbstractMojo {
   /**
    * Location of the output directory for the placeholder poms
    *
    * @parameter expression="${basedir}/target/generated-sources/cedarsoft-serialization-main"
    * @required
   * @readonly
    */
   protected File outputDirectory;
   /**
    * Location of the output directory for the placeholder poms
    *
    * @parameter expression="${basedir}/target/generated-sources/cedarsoft-serialization-test"
    * @required
   * @readonly
    */
   protected File testOutputDirectory;
   /**
    * Project artifacts.
    *
    * @parameter default-value="${project.artifact}"
    * @required
    * @readonly
    */
   protected Artifact projectArtifact;
 
   /**
    * The maven session
    *
    * @parameter expression="${project}"
    * @required
    * @readonly
    */
   protected MavenProject mavenProject;
 
   protected void prepareOutputDirectories() throws MojoExecutionException {
     verifyDirectories();
     outputDirectory.mkdirs();
     testOutputDirectory.mkdirs();
   }
 
   protected void verifyDirectories() throws MojoExecutionException {
     if ( outputDirectory == null ) {
       throw new MojoExecutionException( "output directory not set" );
     }
     if ( testOutputDirectory == null ) {
       throw new MojoExecutionException( "test output directory not set" );
     }
   }
 
   @NotNull
   public File getTestOutputDirectory() throws MojoExecutionException {
     if ( testOutputDirectory == null ) {
       throw new MojoExecutionException( "test output directory not set" );
     }
     return testOutputDirectory;
   }
 
   @NotNull
   public File getOutputDirectory() throws MojoExecutionException {
     if ( outputDirectory == null ) {
       throw new MojoExecutionException( "output directory not set" );
     }
     return outputDirectory;
   }
 
   @NotNull
   protected MavenProject getProject() {
     return mavenProject;
   }
 
   @NotNull
   protected File getBaseDir() {
     return getProject().getBasedir();
   }
 }
