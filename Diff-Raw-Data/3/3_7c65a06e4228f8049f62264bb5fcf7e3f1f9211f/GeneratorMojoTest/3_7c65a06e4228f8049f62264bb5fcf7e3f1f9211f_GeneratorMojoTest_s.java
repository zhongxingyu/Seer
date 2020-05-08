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
 import com.google.common.collect.Lists;
 import org.apache.commons.io.FileUtils;
 import org.apache.maven.artifact.DependencyResolutionRequiredException;
 import org.apache.maven.plugin.testing.AbstractMojoTestCase;
 import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 import org.junit.*;
 
 import java.io.File;
 import java.util.List;
 
 import static com.cedarsoft.matchers.ContainsFileMatcher.empty;
 import static com.cedarsoft.matchers.ContainsOnlyFilesMatcher.containsOnlyFiles;
 import static org.junit.Assert.*;
 
 /**
  *
  */
 public class GeneratorMojoTest extends AbstractMojoTestCase {
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
     GeneratorMojo mojo = createVerifiedMojo( "basic" );
 
     assertEquals( 2, mojo.getExcludes().size() );
     assertTrue( mojo.outputDirectory.getAbsolutePath(), mojo.outputDirectory.getAbsolutePath().endsWith( "target/test/unit/target/out" ) );
     assertTrue( mojo.testOutputDirectory.getAbsolutePath(), mojo.testOutputDirectory.getAbsolutePath().endsWith( "target/test/unit/target/test-out" ) );
     mojo.execute();
 
     assertJaxbObjects( mojo );
     assertTests( mojo );
   }
 
   private void assertJaxbObjects( AbstractGenerateMojo mojo ) {
     assertThat( ContainsFileMatcher.toMessage( mojo.outputDirectory ), mojo.outputDirectory, containsOnlyFiles(
       "unit/basic/jaxb/DaObject.java",
       "unit/basic/jaxb/DaObjectMapping.java"
     ) );
     assertThat( ContainsFileMatcher.toMessage( mojo.resourcesOutputDirectory ), mojo.resourcesOutputDirectory, containsOnlyFiles() );
   }
 
   private void assertTests( AbstractGenerateMojo mojo ) {
     assertThat( ContainsFileMatcher.toMessage( mojo.testOutputDirectory ), mojo.testOutputDirectory,
                containsOnlyFiles( "unit/basic/jaxb/DaObjectJaxbTest.java" ) );
     assertThat( ContainsFileMatcher.toMessage( mojo.testResourcesOutputDirectory ), mojo.testResourcesOutputDirectory,
                 containsOnlyFiles(
                   "unit/basic/jaxb/DaObjectJaxbTest.dataPoint1.xml",
                   "unit/basic/jaxb/DaObjectJaxbTest.stub.xml"
                 ) );
   }
 
   private void assertNoJaxbObject( AbstractGenerateMojo mojo ) {
     assertThat( ContainsFileMatcher.toMessage( mojo.outputDirectory ), mojo.outputDirectory, empty() );
     assertThat( ContainsFileMatcher.toMessage( mojo.resourcesOutputDirectory ), mojo.resourcesOutputDirectory, empty() );
   }
 
   private void assertNoTests( AbstractGenerateMojo mojo ) {
     assertThat( ContainsFileMatcher.toMessage( mojo.testOutputDirectory ), mojo.testOutputDirectory, empty() );
     assertThat( ContainsFileMatcher.toMessage( mojo.testResourcesOutputDirectory ), mojo.testResourcesOutputDirectory, empty() );
   }
 
   @NotNull
   private GeneratorMojo createVerifiedMojo( @NotNull @NonNls String name ) throws Exception {
     GeneratorMojo mojo = createMojo( name );
 
     assertNotNull( mojo.projectArtifact );
     assertNotNull( mojo.outputDirectory );
     assertNotNull( mojo.domainSourceFilePattern );
     assertTrue( mojo.domainSourceFilePattern.length() > 0 );
 
     assertNotNull( mojo.getTestOutputDirectory() );
     assertNotNull( mojo.getOutputDirectory() );
     assertNotNull( mojo.getResourcesOutputDirectory() );
     assertNotNull( mojo.getTestResourcesOutputDirectory() );
 
     return mojo;
   }
 
   @NotNull
   private GeneratorMojo createMojo( @NotNull @NonNls String name ) throws Exception {
     File testPom = new File( getBasedir(), "src/test/resources/unit/" + name + "/plugin-config.xml" );
     assertTrue( testPom.getAbsolutePath() + " not found", testPom.exists() );
     GeneratorMojo mojo = ( GeneratorMojo ) lookupMojo( "generate", testPom );
 
     assertNotNull( mojo );
 
     MavenProjectStub project = new MavenProjectStub() {
       @Override
       public List<? extends String> getCompileClasspathElements() throws DependencyResolutionRequiredException {
         File target = new File( getBasedir(), "target/test-classes" );
         return Lists.newArrayList( target.getAbsolutePath() );
       }
     };
 
     mojo.mavenProject = project;
 
     cleanUp( mojo );
     return mojo;
   }
 
   private void cleanUp( GeneratorMojo mojo ) {
     FileUtils.deleteQuietly( mojo.outputDirectory );
     FileUtils.deleteQuietly( mojo.testOutputDirectory );
     FileUtils.deleteQuietly( mojo.resourcesOutputDirectory );
     FileUtils.deleteQuietly( mojo.testResourcesOutputDirectory );
   }
 }
