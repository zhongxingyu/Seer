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
 
 import com.cedarsoft.codegen.GeneratorConfiguration;
 import com.cedarsoft.serialization.generator.StaxMateGenerator;
 import com.google.common.collect.ImmutableList;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 import java.io.File;
 import java.io.PrintWriter;
 
 /**
  * Generate a Serializer and the corresponding unit tests
  *
  * @goal generate
  */
 public class SerializerGeneratorMojo extends AbstractMojo {
   /**
    * Location of the output directory for the placeholder poms
    *
    * @parameter expression="${basedir}/target/generated-sources/cedarsoft-serialization-main"
    */
   protected File outputDirectory;
 
   /**
    * Location of the output directory for the placeholder poms
    *
    * @parameter expression="${basedir}/target/generated-sources/cedarsoft-serialization-test"
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
    * The path to the domain class the Serializer is generated for.
    *
    * @parameter expression="${domain.class}"
    * @required
    * @readonly
    */
   protected File domainClassSourceFile;
 
   /**
    * Whether to create the serializer
    *
    * @parameter expression="${createSerializer}"
    * @readonly
    */
   protected boolean createSerializer = true;
   /**
    * Whether to create the tests
    *
   * @parameter expression="${createSerializer}"
    * @readonly
    */
   protected boolean createTests = true;
 
   @Override
   public void execute() throws MojoExecutionException, MojoFailureException {
     getLog().info( "Serializer Generator Mojo" );
     getLog().info( "-------------------------" );
 
     if ( domainClassSourceFile == null ) {
       throw new MojoExecutionException( "domain class source file is missing" );
     }
 
     if ( outputDirectory == null ) {
       throw new MojoExecutionException( "output directory not set" );
     }
     outputDirectory.mkdirs();
 
     if ( testOutputDirectory == null ) {
       throw new MojoExecutionException( "test output directory not set" );
     }
     testOutputDirectory.mkdirs();
 
     getLog().debug( "Output Dir: " + outputDirectory.getAbsolutePath() );
     getLog().debug( "Test output Dir: " + testOutputDirectory.getAbsolutePath() );
 
     PrintWriter printWriter = new PrintWriter( new LogWriter( getLog() ) );
     try {
       getLog().info( "Running Generator for " + domainClassSourceFile.getAbsolutePath() );
 
       GeneratorConfiguration configuration = new GeneratorConfiguration( ImmutableList.of( domainClassSourceFile ), outputDirectory, testOutputDirectory, printWriter, GeneratorConfiguration.CreationMode.get( createSerializer, createTests ) );
       new StaxMateGenerator().run( configuration );
     } catch ( Exception e ) {
       throw new MojoExecutionException( "Generation failed due to " + e.getMessage(), e );
     } finally {
       printWriter.close();
     }
   }
 }
