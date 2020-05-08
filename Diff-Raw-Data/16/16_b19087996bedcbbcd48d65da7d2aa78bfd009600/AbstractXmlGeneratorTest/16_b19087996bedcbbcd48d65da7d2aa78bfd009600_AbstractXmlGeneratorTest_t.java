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
 
 package com.cedarsoft.serialization.generator.output.serializer;
 
 import com.cedarsoft.Version;
 import com.cedarsoft.codegen.CodeGenerator;
 import com.cedarsoft.codegen.model.DomainObjectDescriptor;
 import com.cedarsoft.codegen.model.FieldInfo;
 import com.cedarsoft.codegen.model.FieldWithInitializationInfo;
 import com.cedarsoft.serialization.generator.decision.XmlDecisionCallback;
 import com.sun.codemodel.JClass;
 import com.sun.codemodel.JDefinedClass;
 import com.sun.codemodel.JFormatter;
 import com.sun.codemodel.JMethod;
 import com.sun.codemodel.JVar;
 import org.jetbrains.annotations.NotNull;
 import org.testng.annotations.*;
 
 import java.io.StringWriter;
 import java.util.Map;
 
 import static org.testng.Assert.*;
 
 /**
  *
  */
 public class AbstractXmlGeneratorTest {
   private AbstractXmlGenerator generator;
 
   @BeforeMethod
   public void setup() {
     generator = new AbstractXmlGenerator( new CodeGenerator<XmlDecisionCallback>( new XmlDecisionCallback() {
       @NotNull
       @Override
       public Target getSerializationTarget( @NotNull FieldInfo fieldInfo ) {
         throw new UnsupportedOperationException();
       }
     } ) ) {
       @NotNull
       @Override
       protected JClass createSerializerExtendsExpression( @NotNull JClass domainType ) {
         throw new UnsupportedOperationException();
       }
 
       @NotNull
       @Override
       protected Map<FieldWithInitializationInfo, JVar> fillDeSerializationMethods( @NotNull DomainObjectDescriptor domainObjectDescriptor, @NotNull JDefinedClass serializerClass, @NotNull JMethod serializeMethod, @NotNull JMethod deserializeMethod ) {
         throw new UnsupportedOperationException();
       }
 
       @NotNull
       @Override
       protected Class<?> getExceptionType() {
         throw new UnsupportedOperationException();
       }
 
       @NotNull
       @Override
       protected Class<?> getSerializeFromType() {
         throw new UnsupportedOperationException();
       }
 
       @NotNull
       @Override
       protected Class<?> getSerializeToType() {
         throw new UnsupportedOperationException();
       }
 
       @NotNull
       @Override
       protected JVar appendDeserializeStatement( @NotNull JDefinedClass serializerClass, @NotNull JMethod deserializeMethod, @NotNull JVar deserializeFrom, @NotNull JVar formatVersion, @NotNull FieldWithInitializationInfo fieldInfo ) {
         throw new UnsupportedOperationException();
       }
 
       @Override
       protected void appendSerializeStatement( @NotNull JDefinedClass serializerClass, @NotNull JMethod serializeMethod, @NotNull JVar serializeTo, @NotNull JVar object, @NotNull FieldWithInitializationInfo fieldInfo ) {
         throw new UnsupportedOperationException();
       }
     };
   }
 
   @Test
   public void testName() {
     assertEquals( generator.createSerializerClassName( "com.cedarsoft.serialization.generator.staxmate.StaxMateGenerator" ), "com.cedarsoft.serialization.generator.staxmate.StaxMateGeneratorSerializer" );
     assertEquals( generator.createSerializerClassName( "java.lang.String" ), "java.lang.StringSerializer" );
   }
 
   @Test
   public void testVersionRangeInvo() {
     StringWriter out = new StringWriter();
     generator.createDefaultVersionRangeInvocation( Version.valueOf( 1, 0, 0 ), Version.valueOf( 1, 0, 0 ) ).state( new JFormatter( out ) );
     assertEquals( out.toString().trim(), "com.cedarsoft.VersionRange.from(1, 0, 0).to(1, 0, 0);" );
   }
 
   @Test
   public void testNameSpace() {
    assertEquals( generator.getNamespace( "com.cedarsoft.serialization.generator.test.Window" ), "http://cedarsoft.com/serialization/generator/test/window" );
   }
 }
