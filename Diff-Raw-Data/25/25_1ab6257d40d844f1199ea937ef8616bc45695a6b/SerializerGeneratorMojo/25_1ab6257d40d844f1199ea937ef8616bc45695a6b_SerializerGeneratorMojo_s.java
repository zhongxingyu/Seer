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
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.codehaus.plexus.util.DirectoryScanner;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Generate a Serializer and the corresponding unit tests
  *
  * @goal generate
  */
 public class SerializerGeneratorMojo extends AbstractGeneratorMojo {
 
   /**
    * The pattern path to the domain class the Serializer is generated for.
    *
    * @parameter expression="${domain.class}"
    * @required
    */
  protected String domainSourceFilePattern;
 
   /**
    * A list of exclusion filters for the compiler.
    *
    * @parameter
    */
   private Set<String> excludes = new HashSet<String>( Arrays.asList( "**/*Serializer.java", "**/*Test*.java" ) );
 
   /**
    * Whether to create the serializer
    *
    * @parameter expression="${createSerializer}"
    */
   protected boolean createSerializer = true;
   /**
    * Whether to create the tests
    *
    * @parameter expression="${createTests}"
    */
   protected boolean createTests = true;
 
   @Override
   public void execute() throws MojoExecutionException, MojoFailureException {
     getLog().info( "Serializer Generator Mojo" );
     getLog().info( "-------------------------" );
 
    if ( domainSourceFilePattern == null ) {
      throw new MojoExecutionException( "domain class source file is missing" );
     }
 
     prepareOutputDirectories();
 
     getLog().debug( "Output Dir: " + outputDirectory.getAbsolutePath() );
     getLog().debug( "Test output Dir: " + testOutputDirectory.getAbsolutePath() );
 
     PrintWriter printWriter = new PrintWriter( new LogWriter( getLog() ) );
     try {
       List<? extends File> domainClassSourceFiles = getDomainSourceFiles();
       if ( domainClassSourceFiles.isEmpty() ) {
        throw new MojoExecutionException( "No domain class source files found for pattern <" + domainSourceFilePattern + ">" );
       }
 
       getLog().info( "Running Generator for" );
       for ( File domainClassSourceFile : domainClassSourceFiles ) {
         getLog().info( "\t" + domainClassSourceFile.getPath() );
       }
       GeneratorConfiguration configuration = new GeneratorConfiguration( domainClassSourceFiles, outputDirectory, testOutputDirectory, printWriter, GeneratorConfiguration.CreationMode.get( createSerializer, createTests ) );
       new StaxMateGenerator().run( configuration );
     } catch ( Exception e ) {
       throw new MojoExecutionException( "Generation failed due to: " + e.getMessage(), e );
     } finally {
       printWriter.close();
     }
   }
 
   @NotNull
   protected List<? extends File> getDomainSourceFiles() throws IOException {
     DirectoryScanner scanner = new DirectoryScanner();
     scanner.setBasedir( getBaseDir() );
 
     scanner.setExcludes( excludes.toArray( new String[0] ) );
    scanner.setIncludes( new String[]{domainSourceFilePattern} );
     scanner.scan();
 
 
     List<File> files = new ArrayList<File>();
 
     for ( String fileName : scanner.getIncludedFiles() ) {
       files.add( new File( fileName ) );
     }
 
     return files;
   }
 
   public void setExcludes( @NotNull Collection<String> excludes ) {
     this.excludes.clear();
     this.excludes.addAll( excludes );
   }
 
   @NotNull
   @NonNls
   public Set<String> getExcludes() {
     return Collections.unmodifiableSet( excludes );
   }
 }
