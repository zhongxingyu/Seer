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
 
 package com.cedarsoft.serialization.generator;
 
 import com.cedarsoft.AssertUtils;
 import com.cedarsoft.TestUtils;
 import com.cedarsoft.codegen.GeneratorConfiguration;
 import com.google.common.collect.ImmutableList;
 import com.sun.codemodel.JClassAlreadyExistsException;
 import org.apache.commons.io.FileUtils;
 import org.junit.*;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URISyntaxException;
 
 import static org.junit.Assert.*;
 
 /**
  *
  */
 public class GeneratorTest {
   private File destDir;
   private File testDestDir;
 
   @Before
   public void setUp() throws Exception {
     destDir = TestUtils.createEmptyTmpDir();
     testDestDir = TestUtils.createEmptyTmpDir();
   }
 
   @After
   public void tearDown() throws IOException {
     FileUtils.deleteDirectory( destDir );
     FileUtils.deleteDirectory( testDestDir );
   }
 
   @Test
   public void testIt() throws URISyntaxException, IOException, JClassAlreadyExistsException {
     File javaFile = new File( getClass().getResource( "/com/cedarsoft/serialization/generator/staxmate/test/Foo.java" ).toURI() );
 
     GeneratorConfiguration configuration = new GeneratorConfiguration( ImmutableList.of( javaFile ), destDir, testDestDir, new PrintWriter( new ByteArrayOutputStream() ) );
     Generator.AbstractGeneratorRunner<?> runner = new StaxMateGenerator.StaxGeneratorRunner();
     runner.generate( configuration );
 
 
     File serializerFile = new File( destDir, "com/cedarsoft/serialization/generator/staxmate/test/FooSerializer.java" );
     assertTrue( serializerFile.exists() );
 
     AssertUtils.assertEquals( getClass().getResource( "GeneratorTest.testIt_1.txt" ), FileUtils.readFileToString( serializerFile ).trim() );
 
     File serializerTestFile = new File( testDestDir, "com/cedarsoft/serialization/generator/staxmate/test/FooSerializerTest.java" );
     assertTrue( serializerTestFile.exists() );
     AssertUtils.assertEquals( getClass().getResource( "GeneratorTest.testIt_2.txt" ), FileUtils.readFileToString( serializerTestFile ).trim() );
 
     File serializerVersionTestFile = new File( testDestDir, "com/cedarsoft/serialization/generator/staxmate/test/FooSerializerVersionTest.java" );
     assertTrue( serializerVersionTestFile.exists() );
     AssertUtils.assertEquals( getClass().getResource( "GeneratorTest.testIt_3.txt" ), FileUtils.readFileToString( serializerVersionTestFile ).trim() );
   }

  @Test
  public void testOnlyTests() throws Exception {
    File javaFile = new File( getClass().getResource( "/com/cedarsoft/serialization/generator/staxmate/test/Foo.java" ).toURI() );

    GeneratorConfiguration configuration = new GeneratorConfiguration( ImmutableList.of( javaFile ), destDir, testDestDir, new PrintWriter( new ByteArrayOutputStream() ), GeneratorConfiguration.CreationMode.TESTS_ONLY );
    Generator.AbstractGeneratorRunner<?> runner = new StaxMateGenerator.StaxGeneratorRunner();
    runner.generate( configuration );

    File serializerFile = new File( destDir, "com/cedarsoft/serialization/generator/staxmate/test/FooSerializer.java" );
    assertFalse( serializerFile.exists() );

    File serializerTestFile = new File( testDestDir, "com/cedarsoft/serialization/generator/staxmate/test/FooSerializerTest.java" );
    assertTrue( serializerTestFile.exists() );
    AssertUtils.assertEquals( getClass().getResource( "GeneratorTest.testIt_2.txt" ), FileUtils.readFileToString( serializerTestFile ).trim() );

    File serializerVersionTestFile = new File( testDestDir, "com/cedarsoft/serialization/generator/staxmate/test/FooSerializerVersionTest.java" );
    assertTrue( serializerVersionTestFile.exists() );
    AssertUtils.assertEquals( getClass().getResource( "GeneratorTest.testIt_3.txt" ), FileUtils.readFileToString( serializerVersionTestFile ).trim() );
  }
 }
