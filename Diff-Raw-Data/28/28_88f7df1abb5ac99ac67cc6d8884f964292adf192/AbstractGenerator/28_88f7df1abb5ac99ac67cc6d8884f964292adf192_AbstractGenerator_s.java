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
 
 package com.cedarsoft.serialization.generator.output.serializer.test;
 
 import com.cedarsoft.Version;
 import com.cedarsoft.codegen.CodeGenerator;
 import com.cedarsoft.codegen.DecisionCallback;
 import com.cedarsoft.codegen.model.DomainObjectDescriptor;
 import com.cedarsoft.codegen.model.FieldWithInitializationInfo;
 import com.cedarsoft.serialization.Serializer;
 import com.cedarsoft.serialization.generator.output.GeneratorBase;
 import com.sun.codemodel.JClass;
 import com.sun.codemodel.JClassAlreadyExistsException;
 import com.sun.codemodel.JDefinedClass;
 import com.sun.codemodel.JExpr;
 import com.sun.codemodel.JInvocation;
 import com.sun.codemodel.JMethod;
 import com.sun.codemodel.JMod;
 import com.sun.codemodel.JType;
 import com.sun.codemodel.JVar;
 import com.sun.mirror.declaration.ConstructorDeclaration;
 import com.sun.mirror.declaration.ParameterDeclaration;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 
 import java.util.Arrays;
 
 /**
  * @param <T>
  */
 public abstract class AbstractGenerator<T extends DecisionCallback> extends GeneratorBase<T> {
   @NonNls
   @NotNull
   public static final String SERIALIZER_TEST_NAME_SUFFIX = "Test";
   @NonNls
   @NotNull
   public static final String SERIALIZER_VERSION_TEST_NAME_SUFFIX = "VersionTest";
   @NonNls
   public static final String METHOD_NAME_GET_SERIALIZER = "getSerializer";
   @NonNls
   public static final String METHOD_NAME_CREATE_OBJECT_TO_SERIALIZE = "createObjectsToSerialize";
 
   public static final int NUMBER_OF_OBJECTS = 1;
   @NonNls
   public static final String METHOD_NAME_VERIFY_DESERIALIZED = "verifyDeserialized";
   @NonNls
   public static final String PARAM_NAME_DESERIALIZED = "deserialized";
   @NonNls
   public static final String PARAM_NAME_VERSION = "version";
   @NonNls
   public static final String METHOD_NAME_ASSERT_EQUALS = "assertEquals";
 
   protected AbstractGenerator( @NotNull CodeGenerator<T> codeGenerator ) {
     super( codeGenerator );
   }
 
   @NotNull
   public JDefinedClass generateSerializerVersionTest( @NotNull String serializerClassName, @NotNull DomainObjectDescriptor domainObjectDescriptor ) throws JClassAlreadyExistsException {
    JClass serializerClass = codeGenerator.ref( serializerClassName );
     JClass domainType = codeGenerator.ref( domainObjectDescriptor.getQualifiedName() );
 
     //the class
     JDefinedClass testClass = codeModel._class( createSerializerVersionTestName( serializerClass.fullName() ) )._extends( createVersionExtendsClass( domainType, serializerClass ) );
 
     //getSerializer
     createGetSerializerMethod( testClass, serializerClass, domainType );
 
     createGetSerializedMethod( testClass, serializerClass, domainType );
     createVersionVerifyMethod( testClass, serializerClass, domainObjectDescriptor );
 
     return testClass;
   }
 
   protected void createVersionVerifyMethod( @NotNull JDefinedClass testClass, @NotNull JClass serializerClass, @NotNull DomainObjectDescriptor domainObjectDescriptor ) {
     JClass domainType = codeGenerator.ref( domainObjectDescriptor.getQualifiedName() );
 
     JMethod method = testClass.method( JMod.PROTECTED, Void.TYPE, METHOD_NAME_VERIFY_DESERIALIZED )._throws( Exception.class );
     method.annotate( Override.class );
     JVar deserialized = method.param( domainType, PARAM_NAME_DESERIALIZED );
     method.param( Version.class, PARAM_NAME_VERSION );
 
     JClass assertClass = codeGenerator.ref( "org.testng.Assert" );
 
     for ( FieldWithInitializationInfo fieldInfo : domainObjectDescriptor.getFieldsToSerialize() ) {
       method.body().add( assertClass.staticInvoke( METHOD_NAME_ASSERT_EQUALS ).arg( deserialized.invoke( fieldInfo.getGetterDeclaration().getSimpleName() ) ).arg( "daValue" ) );
     }
   }
 
   /**
    * Creates the getSerialized method (used for versioned tests
    *
    * @param testClass       the test class
    * @param serializerClass the serializer class
    * @param domainType      the domain type
    */
   protected abstract void createGetSerializedMethod( @NotNull JDefinedClass testClass, @NotNull JClass serializerClass, @NotNull JClass domainType );
 
   @NotNull
   public JDefinedClass generateSerializerTest( @NotNull String serializerClassName, @NotNull DomainObjectDescriptor domainObjectDescriptor ) throws JClassAlreadyExistsException {
     JClass domainType = codeGenerator.ref( domainObjectDescriptor.getQualifiedName() );
     JClass serializerClass = codeGenerator.ref( serializerClassName );
 
     //the class
     JDefinedClass testClass = codeModel._class( createSerializerTestName( serializerClassName ) )._extends( createExtendsClass( domainType, serializerClass ) );
 
 
     //getSerializer
     createGetSerializerMethod( testClass, serializerClass, domainType );
 
     //createObjectToSerialize
     createCreateObjectsToSerializeMethod( domainObjectDescriptor, testClass, serializerClass, domainType );
 
     //Create the verify method
     createVerifyMethod( testClass, serializerClass, domainType );
 
     return testClass;
   }
 
   @NotNull
   protected JMethod createCreateObjectsToSerializeMethod( @NotNull DomainObjectDescriptor domainObjectDescriptor, @NotNull JDefinedClass serializerTestClass, @NotNull JClass serializerClass, @NotNull JClass domainType ) {
     JClass returnType = codeGenerator.ref( Iterable.class ).narrow( domainType.wildcard() );
 
     JMethod method = serializerTestClass.method( JMod.PROTECTED, returnType, METHOD_NAME_CREATE_OBJECT_TO_SERIALIZE )._throws( Exception.class );
     method.annotate( Override.class );
 
     JInvocation asListInvocation = codeGenerator.ref( Arrays.class ).staticInvoke( "asList" );
     for ( int i = 0; i < NUMBER_OF_OBJECTS; i++ ) {
       asListInvocation.arg( createDomainObjectCreationExpression( domainObjectDescriptor ) );
     }
 
     method.body()._return( asListInvocation );
     return method;
   }
 
   @NotNull
   protected JInvocation createDomainObjectCreationExpression( @NotNull DomainObjectDescriptor domainObjectDescriptor ) {
     JInvocation invocation = JExpr._new( codeGenerator.ref( domainObjectDescriptor.getQualifiedName() ) );
 
     ConstructorDeclaration constructor = domainObjectDescriptor.findBestConstructor();
     for ( ParameterDeclaration parameterDeclaration : constructor.getParameters() ) {
       invocation.arg( codeGenerator.getNewInstanceFactory().create( parameterDeclaration.getType(), parameterDeclaration.getSimpleName() ) );
     }
 
     return invocation;
   }
 
   @NotNull
   protected JMethod createGetSerializerMethod( @NotNull JDefinedClass serializerTestClass, @NotNull JClass serializerClass, @NotNull JClass domainType ) {
     JType returnType = codeGenerator.ref( Serializer.class ).narrow( domainType );
     JMethod createSerializerMethod = serializerTestClass.method( JMod.PROTECTED, returnType, METHOD_NAME_GET_SERIALIZER )._throws( Exception.class );
     createSerializerMethod.annotate( Override.class );
 
     //Return the serializer
     createSerializerMethod.body()._return( JExpr._new( serializerClass ) );
 
     return createSerializerMethod;
   }
 
   @NotNull
   protected abstract JMethod createVerifyMethod( @NotNull JDefinedClass serializerTestClass, @NotNull JClass serializerClass, @NotNull JClass domainType );
 
   @NotNull
   protected abstract JClass createExtendsClass( @NotNull JClass domainType, @NotNull JClass serializerClass );
 
   @NotNull
   protected abstract JClass createVersionExtendsClass( @NotNull JClass domainType, @NotNull JClass serializerClass );
 
   @NotNull
   @NonNls
   public String createSerializerTestName( @NotNull @NonNls String serializerClassName ) {
     return serializerClassName + SERIALIZER_TEST_NAME_SUFFIX;
   }
 
   @NotNull
   @NonNls
   public String createSerializerVersionTestName( @NotNull @NonNls String serializerClassName ) {
     return serializerClassName + SERIALIZER_VERSION_TEST_NAME_SUFFIX;
   }
 }
