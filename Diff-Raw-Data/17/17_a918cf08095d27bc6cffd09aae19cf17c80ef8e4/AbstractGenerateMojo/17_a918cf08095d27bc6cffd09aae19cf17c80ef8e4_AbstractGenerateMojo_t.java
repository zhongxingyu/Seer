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
 
 import com.cedarsoft.codegen.AbstractGenerator;
 import com.cedarsoft.codegen.GeneratorConfiguration;
 import com.google.common.collect.Sets;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.codehaus.plexus.util.DirectoryScanner;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public abstract class AbstractGenerateMojo extends SourceFolderAwareMojo {
   /**
    * The pattern path to the domain classes the serializers (and tests) are generated for.
    *
    * @parameter expression="${domain.class.pattern}"
    * @required
    */
   protected String domainSourceFilePattern;
 
   /**
    * A list of exclusion filters for the generator.
    * The default excludes contain:
    * <ul>
    * <li>**&#47*Test.java/li>
    * <li>**&#47;*Jaxb*.java</li>
    * </ul>
    * <p/>
    * Those excludes are useful to avoid recursive creation of serializers and tests.
    *
    * @parameter
    */
  protected Set<String> excludes = Sets.newHashSet( "**/*Test.java" );
 
   /**
    * Whether to create the production classes
    *
    * @parameter expression="${createClasses}"
    */
   protected boolean createClasses = true;
   /**
    * Whether to create the tests
    *
    * @parameter expression="${createTests}"
    */
   protected boolean createTests = true;
 
   protected AbstractGenerateMojo() {
   }
 
   protected AbstractGenerateMojo( @NotNull @NonNls Collection<? extends String> defaultExcludes ) {
     this.excludes.addAll( defaultExcludes );
   }
 
   protected boolean createClasses() {
     return createClasses;
   }
 
   protected boolean createTests() {
     return createTests;
   }
 
   @NonNls
   public Set<String> getExcludes() {
     return Collections.unmodifiableSet( excludes );
   }
 
   @NonNls
   protected String getDomainSourceFilePattern() {
     return domainSourceFilePattern;
   }
 
   @Override
   public void execute() throws MojoExecutionException, MojoFailureException {
     getLog().info( "Generator Mojo" );
     getLog().info( "-------------------------" );
 
     if ( getDomainSourceFilePattern() == null ) {
       throw new MojoExecutionException( "domain class source file pattern is missing" );
     }
 
     prepareOutputDirectories();
 
     getLog().debug( "Sources dir: " + getSourceRoot().getAbsolutePath() );
     getLog().debug( "Output Dir: " + getOutputDirectory().getAbsolutePath() );
     getLog().debug( "Resources output Dir: " + getResourcesOutputDirectory().getAbsolutePath() );
     getLog().debug( "Test output Dir: " + getTestOutputDirectory().getAbsolutePath() );
     getLog().debug( "Test resources output Dir: " + getTestResourcesOutputDirectory().getAbsolutePath() );
 
     PrintWriter printWriter = new PrintWriter( new LogWriter( getLog() ) );
     try {
       List<? extends File> domainSourceFiles = getDomainSourceFiles();
       if ( domainSourceFiles.isEmpty() ) {
         throw new MojoExecutionException( "No domain class source files found for pattern <" + getDomainSourceFilePattern() + ">" );
       }
 
       getLog().info( "Running Generator for" );
       for ( File domainClassSourceFile : domainSourceFiles ) {
         getLog().info( "\t" + domainClassSourceFile.getPath() );
       }
       GeneratorConfiguration configuration = new GeneratorConfiguration( domainSourceFiles, getOutputDirectory(), getResourcesOutputDirectory(), getTestOutputDirectory(), getTestResourcesOutputDirectory(), buildClassPath(), printWriter, GeneratorConfiguration.CreationMode.get( createClasses(), createTests() ) );
       createGenerator().run( configuration );
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
 
     Set<String> excludes = getExcludes();
     scanner.setExcludes( excludes.toArray( new String[excludes.size()] ) );
     scanner.setIncludes( new String[]{getDomainSourceFilePattern()} );
     scanner.scan();
 
     List<File> files = new ArrayList<File>();
 
     for ( String fileName : scanner.getIncludedFiles() ) {
       files.add( new File( fileName ) );
     }
 
     return files;
   }
 
   @NotNull
   protected abstract AbstractGenerator createGenerator();
 }
