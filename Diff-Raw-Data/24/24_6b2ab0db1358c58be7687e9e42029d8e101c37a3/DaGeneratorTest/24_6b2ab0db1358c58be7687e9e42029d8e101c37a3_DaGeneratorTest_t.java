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
 
 import com.cedarsoft.codegen.AbstractGenerator;
 import com.cedarsoft.codegen.CodeGenerator;
 import com.cedarsoft.codegen.GeneratorConfiguration;
 import com.cedarsoft.codegen.model.FieldWithInitializationInfo;
 import com.cedarsoft.serialization.Serializer;
 import com.cedarsoft.serialization.generator.decision.DefaultXmlDecisionCallback;
 import com.cedarsoft.serialization.generator.decision.XmlDecisionCallback;
 import com.cedarsoft.serialization.generator.output.serializer.AbstractXmlGenerator;
 import com.cedarsoft.serialization.generator.output.serializer.test.XmlGenerator;
 import com.google.common.collect.Lists;
 import com.sun.codemodel.JClass;
 import com.sun.codemodel.JDefinedClass;
 import com.sun.codemodel.JMethod;
 import com.sun.codemodel.JVar;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.jetbrains.annotations.NotNull;
 import org.junit.*;
 import org.junit.rules.*;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 /**
  *
  */
 public class DaGeneratorTest {
   @Rule
   public TemporaryFolder tmp = new TemporaryFolder();
 
   private MyGenerator myGenerator;
   private StringWriter out;
   private GeneratorConfiguration generatorConfiguration;
   private File sourceFile;
 
   @Before
   public void setUp() throws Exception {
     myGenerator = new MyGenerator();
 
    sourceFile = tmp.newFile( "MyClass.java" );
    FileUtils.writeByteArrayToFile( sourceFile, IOUtils.toByteArray( getClass().getResourceAsStream( "MyClass.java" ) ) );
 
     List<File> sourceFiles = Lists.newArrayList( sourceFile );
     out = new StringWriter();
 
     generatorConfiguration = new GeneratorConfiguration( sourceFiles, tmp.newFolder( "dest" ), tmp.newFolder( "test-dest" ), new PrintWriter( out ) );
   }
 
   @Test
   public void testIt() throws Exception {
     myGenerator.run( generatorConfiguration );
     assertEquals( "MyRunner called", out.toString() );
   }
 
   private static class MyGenerator extends Generator {
     @NotNull
     @Override
     protected String getRunnerClassName() {
       return MyRunner.class.getName();
     }
   }
 
   private static class MyRunner implements AbstractGenerator.Runner {
     @Override
     public void generate( @NotNull GeneratorConfiguration configuration ) throws Exception {
       configuration.getLogOut().print( "MyRunner called" );
     }
   }
 
   @Test
   public void testAbs() throws Exception {
     Generator.AbstractGeneratorRunner<XmlDecisionCallback> runner = new Generator.AbstractGeneratorRunner<XmlDecisionCallback>() {
       @NotNull
       @Override
       protected XmlDecisionCallback createDecisionCallback() {
         return new DefaultXmlDecisionCallback();
       }
 
       @NotNull
       @Override
       protected com.cedarsoft.serialization.generator.output.serializer.test.AbstractGenerator<XmlDecisionCallback> instantiateTestGenerator( @NotNull CodeGenerator<XmlDecisionCallback> testCodeGenerator ) {
         return new XmlGenerator( testCodeGenerator );
       }
 
       @NotNull
       @Override
       protected com.cedarsoft.serialization.generator.output.serializer.AbstractGenerator<XmlDecisionCallback> instantiateGenerator( @NotNull CodeGenerator<XmlDecisionCallback> serializerCodeGenerator ) {
         CodeGenerator<XmlDecisionCallback> codeGenerator = new CodeGenerator<XmlDecisionCallback>( new DefaultXmlDecisionCallback() );
         return new AbstractXmlGenerator( codeGenerator ) {
           @NotNull
           @Override
           protected JClass createSerializerExtendsExpression( @NotNull JClass domainType ) {
             return codeGenerator.ref( Serializer.class ).narrow( Integer.class );
           }
 
           @NotNull
           @Override
           protected Class<?> getExceptionType() {
             return NullPointerException.class;
           }
 
           @NotNull
           @Override
           protected Class<?> getSerializeFromType() {
             return String.class;
           }
 
           @NotNull
           @Override
           protected Class<?> getSerializeToType() {
             return StringBuilder.class;
           }
 
           @NotNull
           @Override
           protected JVar appendDeserializeStatement( @NotNull JDefinedClass serializerClass, @NotNull JMethod deserializeMethod, @NotNull JVar deserializeFrom, @NotNull JVar formatVersion, @NotNull FieldWithInitializationInfo fieldInfo ) {
             deserializeMethod.body().directStatement( "//deserialize:" + fieldInfo.getSimpleName() );
             return deserializeMethod.body().decl( codeGenerator.ref( Double.class ), "asdf" );
           }
 
           @Override
           protected void appendSerializeStatement( @NotNull JDefinedClass serializerClass, @NotNull JMethod serializeMethod, @NotNull JVar serializeTo, @NotNull JVar object, @NotNull JVar formatVersion, @NotNull FieldWithInitializationInfo fieldInfo ) {
             serializeMethod.body().directStatement( "//serialize:" + fieldInfo.getSimpleName() );
           }
         };
       }
     };
 
     runner.generate( generatorConfiguration );
 
     assertEquals( "Generating Serializer:\n" +
                     "Generating Serializer Tests:\n" +
                    "com/cedarsoft/serialization/generator/MyClassSerializerTest.java\n" +
                    "com/cedarsoft/serialization/generator/MyClassSerializerVersionTest.java\n", out.toString() );
   }
 }
