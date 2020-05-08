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
 
 package com.cedarsoft.serialization.generator.common;
 
 import com.cedarsoft.codegen.AbstractGenerator;
 import com.cedarsoft.codegen.CodeGenerator;
 import com.cedarsoft.codegen.GeneratorConfiguration;
 import com.cedarsoft.codegen.model.FieldWithInitializationInfo;
 import com.cedarsoft.serialization.Serializer;
 import com.cedarsoft.serialization.generator.common.decision.DefaultXmlDecisionCallback;
 import com.cedarsoft.serialization.generator.common.decision.XmlDecisionCallback;
 import com.cedarsoft.serialization.generator.common.output.serializer.AbstractXmlGenerator;
 import com.cedarsoft.serialization.generator.common.output.serializer.test.XmlGenerator;
 import com.google.common.collect.Lists;
 import com.sun.codemodel.JClass;
 import com.sun.codemodel.JDefinedClass;
 import com.sun.codemodel.JMethod;
 import com.sun.codemodel.JVar;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.junit.*;
 import org.junit.rules.*;
 
 import javax.annotation.Nonnull;
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
 
     generatorConfiguration = new GeneratorConfiguration( sourceFiles, tmp.newFolder( "dest" ), tmp.newFolder( "resources-dest" ), tmp.newFolder( "test-dest" ), tmp.newFolder( "test-resources-dest" ), null, new PrintWriter( out ) );
   }
 
   @Test
   public void testIt() throws Exception {
     myGenerator.run( generatorConfiguration );
     assertEquals( "MyRunner called", out.toString() );
   }
 
   private static class MyGenerator extends Generator {
     @Nonnull
     @Override
     protected String getRunnerClassName() {
       return MyRunner.class.getName();
     }
   }
 
   private static class MyRunner implements AbstractGenerator.Runner {
     @Override
     public void generate( @Nonnull GeneratorConfiguration configuration ) throws Exception {
       configuration.getLogOut().print( "MyRunner called" );
     }
   }
 
   @Test
   public void testAbs() throws Exception {
     Generator.AbstractGeneratorRunner<XmlDecisionCallback> runner = new Generator.AbstractGeneratorRunner<XmlDecisionCallback>() {
       @Nonnull
       @Override
       protected XmlDecisionCallback createDecisionCallback() {
         return new DefaultXmlDecisionCallback();
       }
 
       @Nonnull
       @Override
       protected com.cedarsoft.serialization.generator.common.output.serializer.test.AbstractGenerator<XmlDecisionCallback> instantiateTestGenerator( @Nonnull CodeGenerator testCodeGenerator ) {
         return new XmlGenerator( testCodeGenerator );
       }
 
       @Nonnull
       @Override
       protected com.cedarsoft.serialization.generator.common.output.serializer.AbstractGenerator<XmlDecisionCallback> instantiateGenerator( @Nonnull CodeGenerator serializerCodeGenerator ) {
         CodeGenerator codeGenerator = new CodeGenerator( new DefaultXmlDecisionCallback() );
         return new AbstractXmlGenerator( codeGenerator ) {
           @Nonnull
           @Override
           protected JClass createSerializerExtendsExpression( @Nonnull JClass domainType ) {
            JClass ref = codeGenerator.ref( Serializer.class );
            return ref.narrow( Integer.class );
           }
 
           @Nonnull
           @Override
           protected Class<?> getExceptionType() {
             return NullPointerException.class;
           }
 
           @Nonnull
           @Override
           protected Class<?> getSerializeFromType() {
             return String.class;
           }
 
           @Nonnull
           @Override
           protected Class<?> getSerializeToType() {
             return StringBuilder.class;
           }
 
           @Nonnull
           @Override
           protected JVar appendDeserializeStatement( @Nonnull JDefinedClass serializerClass, @Nonnull JMethod deserializeMethod, @Nonnull JVar deserializeFrom, JVar wrapper, @Nonnull JVar formatVersion, @Nonnull FieldWithInitializationInfo fieldInfo ) {
             deserializeMethod.body().directStatement( "//deserialize:" + fieldInfo.getSimpleName() );
             return deserializeMethod.body().decl( codeGenerator.ref( Double.class ), "asdf" );
           }
 
           @Override
           protected void appendSerializeStatement( @Nonnull JDefinedClass serializerClass, @Nonnull JMethod serializeMethod, @Nonnull JVar serializeTo, @Nonnull JVar object, @Nonnull JVar formatVersion, @Nonnull FieldWithInitializationInfo fieldInfo ) {
             serializeMethod.body().directStatement( "//serialize:" + fieldInfo.getSimpleName() );
           }
         };
       }
     };
 
     runner.generate( generatorConfiguration );
 
     assertTrue( out.toString(), out.toString().contains( "Generating Serializer:\n" +
                                                            "Generating Serializer Tests:\n" +
                                                            "com/cedarsoft/serialization/generator/common/MyClassSerializerTest.java\n" +
                                                            "com/cedarsoft/serialization/generator/common/MyClassSerializerVersionTest.java\n" ) );
   }
 }
