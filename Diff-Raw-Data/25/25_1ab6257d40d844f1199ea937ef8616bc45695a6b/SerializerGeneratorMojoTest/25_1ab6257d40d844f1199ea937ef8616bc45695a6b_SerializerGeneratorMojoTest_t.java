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
 
 import com.cedarsoft.matchers.ContainsFileMatcher;
 import org.apache.commons.io.FileUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.testing.AbstractMojoTestCase;
 import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 import org.junit.*;
 
 import java.io.File;
 import java.util.Collections;
 
 import static com.cedarsoft.matchers.ContainsFileMatcher.containsFiles;
 import static com.cedarsoft.matchers.ContainsFileMatcher.empty;
 import static org.junit.Assert.*;
 
 /**
  *
  */
 public class SerializerGeneratorMojoTest extends AbstractMojoTestCase {
   @Before
   @Override
   public void setUp() throws Exception {
     super.setUp();
   }
 
   @After
   @Override
   public void tearDown() throws Exception {
     super.tearDown();
   }
 
   @Test
   public void testBasic() throws Exception {
     SerializerGeneratorMojo mojo = createVerifiedMojo( "basic" );
 
     assertEquals( 2, mojo.getExcludes().size() );
     assertTrue( mojo.outputDirectory.getAbsolutePath(), mojo.outputDirectory.getAbsolutePath().endsWith( "target/test/unit/target/out" ) );
     assertTrue( mojo.testOutputDirectory.getAbsolutePath(), mojo.testOutputDirectory.getAbsolutePath().endsWith( "target/test/unit/target/test-out" ) );
     mojo.execute();
 
     assertThat( ContainsFileMatcher.toMessage( mojo.outputDirectory ), mojo.outputDirectory, containsFiles( "unit/basic/DaDomainObjectSerializer.java" ) );
     assertThat( ContainsFileMatcher.toMessage( mojo.testOutputDirectory ), mojo.testOutputDirectory, containsFiles( "unit/basic/DaDomainObjectSerializerVersionTest.java",
                                                                                                                     "unit/basic/DaDomainObjectSerializerTest.java" ) );
   }
 
   @Test
   public void testOnlyTests() throws Exception {
     SerializerGeneratorMojo mojo = createVerifiedMojo( "only-tests" );
     assertEquals( 2, mojo.getExcludes().size() );
     mojo.execute();
 
     assertThat( ContainsFileMatcher.toMessage( mojo.outputDirectory ), mojo.outputDirectory, empty() );
     assertThat( ContainsFileMatcher.toMessage( mojo.testOutputDirectory ), mojo.testOutputDirectory, containsFiles( "unit/basic/DaDomainObjectSerializerVersionTest.java",
                                                                                                                     "unit/basic/DaDomainObjectSerializerTest.java" ) );
   }
 
   @Test
   public void testNoTests() throws Exception {
     SerializerGeneratorMojo mojo = createVerifiedMojo( "no-tests" );
     assertEquals( 2, mojo.getExcludes().size() );
     mojo.execute();
 
     assertThat( ContainsFileMatcher.toMessage( mojo.outputDirectory ), mojo.outputDirectory, containsFiles( "unit/basic/DaDomainObjectSerializer.java" ) );
     assertThat( ContainsFileMatcher.toMessage( mojo.testOutputDirectory ), mojo.testOutputDirectory, empty() );
   }
 
   @Test
   public void testExcludes() throws Exception {
     SerializerGeneratorMojo mojo = createMojo( "excludes" );
     mojo.execute();
 
     assertThat( ContainsFileMatcher.toMessage( mojo.outputDirectory ), mojo.outputDirectory, containsFiles( "unit/basic/DaDomainObjectSerializer.java" ) );
     assertThat( ContainsFileMatcher.toMessage( mojo.testOutputDirectory ), mojo.testOutputDirectory, empty() );
   }
 
   @Test
   public void testExcludes2() throws Exception {
     SerializerGeneratorMojo mojo = createMojo( "excludes" );
 
     assertEquals( 2, mojo.getExcludes().size() );
     mojo.setExcludes( Collections.<String>emptySet() );
     assertEquals( 0, mojo.getExcludes().size() );
     mojo.execute();
 
     assertThat( ContainsFileMatcher.toMessage( mojo.outputDirectory ), mojo.outputDirectory, containsFiles( "unit/basic/DaDomainObjectSerializer.java" ) );
     assertThat( ContainsFileMatcher.toMessage( mojo.testOutputDirectory ), mojo.testOutputDirectory, empty() );
   }
 
   @Test
   public void testNoDirs() throws Exception {
     SerializerGeneratorMojo mojo = createMojo( "no-dirs" );
 
     try {
       mojo.getOutputDirectory();
       fail( "Where is the Exception" );
     } catch ( MojoExecutionException e ) {
       assertEquals( "output directory not set", e.getMessage() );
     }
 
     try {
       mojo.getTestOutputDirectory();
       fail( "Where is the Exception" );
     } catch ( MojoExecutionException e ) {
       assertEquals( "test output directory not set", e.getMessage() );
     }
   }
 
   @Test
   public void testNoDirs2() throws Exception {
     SerializerGeneratorMojo mojo = createMojo( "no-dirs" );
 
     try {
       mojo.execute();
       fail( "Where is the Exception" );
     } catch ( MojoExecutionException e ) {
       assertEquals( "output directory not set", e.getMessage() );
     }
 
     mojo.outputDirectory = new File( "a file" );
 
     try {
       mojo.execute();
       fail( "Where is the Exception" );
     } catch ( MojoExecutionException e ) {
       assertEquals( "test output directory not set", e.getMessage() );
     }
   }
 
   @Test
   public void testNoClass() throws Exception {
     SerializerGeneratorMojo mojo = createMojo( "no-class" );
 
     try {
       mojo.execute();
       fail( "Where is the Exception" );
     } catch ( MojoExecutionException e ) {
       assertEquals( "domain class source file pattern is missing", e.getMessage() );
     }
 
    mojo.domainClassSourceFilePattern = "invalid pattern";
 
     try {
       mojo.execute();
       fail( "Where is the Exception" );
     } catch ( MojoExecutionException e ) {
       assertEquals( "Generation failed due to: No domain class source files found for pattern <invalid pattern>", e.getMessage() );
     }
   }
 
   @NotNull
   private SerializerGeneratorMojo createVerifiedMojo( @NotNull @NonNls String name ) throws Exception {
     SerializerGeneratorMojo mojo = createMojo( name );
 
     assertNotNull( mojo.projectArtifact );
     assertNotNull( mojo.outputDirectory );
    assertNotNull( mojo.domainClassSourceFilePattern );
    assertTrue( mojo.domainClassSourceFilePattern.length() > 0 );
 
     assertNotNull( mojo.getTestOutputDirectory() );
     assertNotNull( mojo.getOutputDirectory() );
 
     //Clean up
     FileUtils.deleteQuietly( mojo.outputDirectory );
     FileUtils.deleteQuietly( mojo.testOutputDirectory );
     assertFalse( mojo.outputDirectory.exists() );
     assertFalse( mojo.testOutputDirectory.exists() );
 
     return mojo;
   }
 
   @NotNull
   private SerializerGeneratorMojo createMojo( @NotNull @NonNls String name ) throws Exception {
     File testPom = new File( getBasedir(), "src/test/resources/unit/" + name + "/basic-plugin-config.xml" );
     SerializerGeneratorMojo mojo = ( SerializerGeneratorMojo ) lookupMojo( "generate", testPom );
 
     assertNotNull( mojo );
     mojo.mavenProject = new MavenProjectStub();
     return mojo;
   }
 }
