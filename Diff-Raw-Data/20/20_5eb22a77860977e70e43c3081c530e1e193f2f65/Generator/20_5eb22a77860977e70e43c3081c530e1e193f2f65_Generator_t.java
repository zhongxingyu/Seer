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
 
 package com.cedarsoft.rest.generator;
 
 import com.cedarsoft.codegen.CodeGenerator;
 import com.cedarsoft.codegen.NamingSupport;
 import com.cedarsoft.codegen.TypeUtils;
 import com.cedarsoft.codegen.model.DomainObjectDescriptor;
 import com.cedarsoft.codegen.model.FieldInfo;
 import com.cedarsoft.codegen.model.FieldTypeInformation;
 import com.cedarsoft.codegen.model.FieldWithInitializationInfo;
 import com.cedarsoft.id.NameSpaceSupport;
 import com.cedarsoft.jaxb.AbstractJaxbObject;
 import com.cedarsoft.jaxb.JaxbObject;
 import com.cedarsoft.jaxb.JaxbStub;
 import com.cedarsoft.rest.JaxbMapping;
 import com.cedarsoft.rest.JaxbMappingContext;
 import com.sun.codemodel.JAnnotationUse;
 import com.sun.codemodel.JBlock;
 import com.sun.codemodel.JClass;
 import com.sun.codemodel.JClassAlreadyExistsException;
 import com.sun.codemodel.JDefinedClass;
 import com.sun.codemodel.JExpr;
 import com.sun.codemodel.JExpression;
 import com.sun.codemodel.JFieldVar;
 import com.sun.codemodel.JInvocation;
 import com.sun.codemodel.JMethod;
 import com.sun.codemodel.JMod;
 import com.sun.codemodel.JStatement;
 import com.sun.codemodel.JType;
 import com.sun.codemodel.JVar;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 
 import javax.ws.rs.core.UriBuilder;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  *
  */
 public class Generator extends AbstractGenerator<JaxbObjectGenerator.StubDecisionCallback> {
   @NonNls
   public static final String ID = "id";
   @NotNull
   @NonNls
   public static final String METHOD_NAME_GET_STUB = "getStub";
   @NonNls
   public static final String METHOD_NAME_SET_ID = "setId";
   @NonNls
   public static final String METHOD_NAME_GET_ID = "getId";
   @NonNls
   public static final String METHOD_NAME_SET_URIS = "setUris";
   @NonNls
   public static final String METHOD_NAME_PATH = "path";
   @NonNls
   public static final String METHOD_NAME_BUILD = "build";
   @NonNls
   public static final String ARG_PLACEHOLDER_ID = "{id}";
   @NonNls
   public static final String METHOD_NAME_SET_HREF = "setHref";
   @NonNls
   public static final String NAME = "name";
   @NonNls
   public static final String NAMESPACE = "namespace";
   @NonNls
   public static final String VALUE = "value";
   @NonNls
   public static final String OBJECT = "object";
   @NonNls
   public static final String URI_BUILDER = "uriBuilder";
   @NonNls
   public static final String CONTEXT = "context";
   @NonNls
   public static final String JAXB_OBJECT = "jaxbObject";
   @NonNls
   public static final String STUB_OBJECT = "stub";
   @NonNls
   public static final String METHOD_NAME_CREATE_JAXB_OBJECT = "createJaxbObject";
   @NonNls
   public static final String METHOD_NAME_CREATE_JAXB_STUB = "createJaxbObjectStub";
   @NonNls
   public static final String STUB_NAMESPACE_SUFFIX = "/stub";
   @NonNls
   public static final String METHOD_NAME_ADD_MAPPING = "addMapping";
   @NonNls
   public static final String METHOD_NAME_GET_DELEGATES_MAPPING = "getDelegatesMapping";
   @NonNls
   public static final String JAXB = "Jaxb";
   @NonNls
   public static final String STUB = "Stub";
   @NonNls
   public static final String CONST_PATH = "PATH";
   @NonNls
   public static final String CONST_ID = "ID";
 
   public Generator( @NotNull CodeGenerator<JaxbObjectGenerator.StubDecisionCallback> codeGenerator, @NotNull DomainObjectDescriptor descriptor ) {
     super( codeGenerator, descriptor );
   }
 
   private JDefinedClass baseClass;
   private JDefinedClass jaxbObject;
   private JDefinedClass jaxbStub;
   private JDefinedClass mappingClass;
 
   public void generate() throws JClassAlreadyExistsException {
     if ( baseClass != null || jaxbObject != null || jaxbStub != null || mappingClass != null ) {
       throw new IllegalStateException( "generate has still been called!" );
     }
 
     createBaseClass();
 
     createJaxbObject();
     createJaxbStub();
 
     createJaxbMapping();
   }
 
   private void createBaseClass() throws JClassAlreadyExistsException {
     baseClass = _class( getJaxbBaseName(), JMod.PUBLIC | JMod.ABSTRACT )._extends( AbstractJaxbObject.class );
     baseClass.annotate( XmlType.class )
       .param( NAME, "abstract" + descriptor.getClassDeclaration().getSimpleName() );
 
     addFields( baseClass, Scope.COMMON );
   }
 
   /**
    * @noinspection InstanceMethodNamingConvention
    */
   @NotNull
   protected JDefinedClass _class( @NotNull @NonNls String jaxbBaseName, int mods ) throws JClassAlreadyExistsException {
     int idx = jaxbBaseName.lastIndexOf( '.' );
     return codeGenerator.getModel()._package( jaxbBaseName.substring( 0, idx ) )._class( mods, jaxbBaseName.substring( idx + 1 ) );
   }
 
   private void createJaxbMapping() throws JClassAlreadyExistsException {
     assert jaxbObject != null;
     assert jaxbStub != null;
 
     JClass objectType = codeGenerator.ref( descriptor.getQualifiedName() );
     JClass superType = codeGenerator.ref( JaxbMapping.class ).narrow( objectType ).narrow( jaxbObject ).narrow( jaxbStub );
 
     mappingClass = codeGenerator.getModel()._class( getJaxbMappingTypeName() )._extends( superType );
 
     createHrefMethod();
     createCreateJaxbObjectMethod();
     createCreateJaxbStubMethod();
   }
 
   private void createCreateJaxbObjectMethod() {
     assert jaxbObject != null;
     assert mappingClass != null;
 
 
     JMethod method = mappingClass.method( JMod.PROTECTED, jaxbObject, METHOD_NAME_CREATE_JAXB_OBJECT );
    method._throws( URISyntaxException.class );

     JVar object = method.param( codeGenerator.ref( descriptor.getQualifiedName() ), OBJECT );
     JVar context = method.param( codeGenerator.ref( JaxbMappingContext.class ), CONTEXT );
     method.annotate( Override.class );
 
     JVar jaxbObjectInstance = method.body().decl( jaxbObject, JAXB_OBJECT, JExpr._new( jaxbObject ) );
 
     addFieldCopyOperations( object, jaxbObjectInstance, context, method.body(), false );
     method.body()._return( jaxbObjectInstance );
   }
 
   private void createCreateJaxbStubMethod() {
     assert jaxbStub != null;
     assert mappingClass != null;
 
     JMethod method = mappingClass.method( JMod.PROTECTED, jaxbStub, METHOD_NAME_CREATE_JAXB_STUB );
    method._throws( URISyntaxException.class );
    method.annotate( Override.class );

     JVar object = method.param( codeGenerator.ref( descriptor.getQualifiedName() ), OBJECT );
     JVar context = method.param( codeGenerator.ref( JaxbMappingContext.class ), CONTEXT );
 
     JVar jaxbObjectInstance = method.body().decl( jaxbStub, STUB_OBJECT, JExpr._new( jaxbStub ) );
 
     addFieldCopyOperations( object, jaxbObjectInstance, context, method.body(), true );
     method.body()._return( jaxbObjectInstance );
   }
 
   private void addFieldCopyOperations( @NotNull JExpression object, @NotNull JExpression jaxbObjectInstance, @NotNull JExpression context, @NotNull JBlock block, boolean isStub ) {
     assert jaxbObject != null;
     assert jaxbStub != null;
     assert mappingClass != null;
 
 
     Collection<JStatement> statements = new ArrayList<JStatement>();
 
     for ( FieldWithInitializationInfo fieldInfo : descriptor.getFieldInfos() ) {
       JInvocation getterInvocation = object.invoke( fieldInfo.getGetterDeclaration().getSimpleName() );
 
       if ( isStub && !shallAddFieldCopyStatementToStub( fieldInfo ) ) {
         continue;
       }
 
       JInvocation value;
       if ( isProbablyOwnType( fieldInfo.getType() ) ) {
         JClass fieldStubType = getJaxbType( fieldInfo, true );
 
         value = JExpr.invoke( METHOD_NAME_GET_STUB )
           .arg( fieldStubType.dotclass() )
           .arg( getterInvocation )
           .arg( context )
           ;
 
         ensureDelegateAvailable( getJaxbType( fieldInfo, false ), fieldStubType );
 
       } else {
         value = getterInvocation;
       }
       statements.add( jaxbObjectInstance.invoke( NamingSupport.createSetter( fieldInfo.getSimpleName() ) ).arg( value ) );
     }
 
     for ( JStatement statement : statements ) {
       block.add( statement );
     }
   }
 
   private void ensureDelegateAvailable( @NotNull JClass fieldJaxbObject, @NotNull JClass fieldJaxbStub ) {
     JMethod constructor = getOrCreateConstructor();
 
     String paramName = NamingSupport.createVarName( fieldJaxbObject.outer().name() + MAPPING_SUFFIX );
 
     JClass mappingType = codeGenerator.ref( getMappingNameFor( fieldJaxbObject ) );
 
     //Check whether the mapping still exists
     for ( JVar param : constructor.listParams() ) {
       if ( param.type().equals( mappingType ) ) {
         return;
       }
     }
 
     //It does not exist, therefore let us add the serializer and map it
     JVar param = constructor.param( mappingType, paramName );
 
     constructor.body().add(
       JExpr.invoke( METHOD_NAME_GET_DELEGATES_MAPPING ).invoke( METHOD_NAME_ADD_MAPPING )
         .arg( fieldJaxbObject.dotclass() )
         .arg( fieldJaxbStub.dotclass() )
         .arg( param )
     );
   }
 
   @NotNull
   @NonNls
   private static String getMappingNameFor( @NotNull JClass jaxbObject ) {
     return jaxbObject.outer().fullName() + MAPPING_SUFFIX;
   }
 
   @NotNull
   private JMethod getOrCreateConstructor() {
     assert mappingClass != null;
 
     //If no constructor exists, create one
     if ( !mappingClass.constructors().hasNext() ) {
       mappingClass.constructor( JMod.PUBLIC );
     }
 
     return ( JMethod ) mappingClass.constructors().next();
   }
 
   private void createHrefMethod() {
     assert mappingClass != null;
 
     JMethod method = mappingClass.method( JMod.PROTECTED, Void.TYPE, METHOD_NAME_SET_URIS );
     JVar object = method.param( JaxbObject.class, OBJECT );
     JVar uriBuilder = method.param( UriBuilder.class, URI_BUILDER );
     method.annotate( Override.class );
 
     JFieldVar pathConst = mappingClass.field( JMod.PUBLIC | JMod.STATIC | JMod.FINAL, String.class, CONST_PATH,
                                               JExpr.lit( NamingSupport.plural( NamingSupport.createXmlElementName( getDescriptor().getClassDeclaration().getSimpleName() ) ) ) );
     JFieldVar idConst = mappingClass.field( JMod.PUBLIC | JMod.STATIC | JMod.FINAL, String.class, CONST_ID, JExpr.lit( ARG_PLACEHOLDER_ID ) );
 
     JInvocation uriBuilderInvocation = uriBuilder.invoke( METHOD_NAME_PATH )
       .arg( pathConst )
       .invoke( METHOD_NAME_PATH )
       .arg( idConst )
       .invoke( METHOD_NAME_BUILD )
       .arg( object.invoke( METHOD_NAME_GET_ID ) );
 
     method.body().add( object.invoke( METHOD_NAME_SET_HREF ).arg( uriBuilderInvocation ) );
   }
 
   protected void createJaxbObject() throws JClassAlreadyExistsException {
     assert baseClass != null;
 
     String name = NamingSupport.createVarName( descriptor.getClassDeclaration().getSimpleName() );
 
     jaxbObject = baseClass._class( JMod.PUBLIC | JMod.STATIC, JAXB )._extends( baseClass );
     jaxbObject.annotate( XmlType.class )
       .param( NAME, name );
     jaxbObject.annotate( XmlRootElement.class )
       .param( NAME, name )
       .param( NAMESPACE, NameSpaceSupport.createNameSpaceUriBase( descriptor.getQualifiedName() ) );
     jaxbObject.annotate( XmlAccessorType.class ).param( VALUE, XmlAccessType.FIELD );
 
     addFields( jaxbObject, Scope.JAXB );
   }
 
   protected void createJaxbStub() throws JClassAlreadyExistsException {
     assert baseClass != null;
     assert jaxbObject != null;
 
     String name = NamingSupport.createVarName( descriptor.getClassDeclaration().getSimpleName() ) + STUB;
 
     jaxbStub = baseClass._class( JMod.PUBLIC | JMod.STATIC, STUB )._extends( baseClass )._implements(
       codeGenerator.ref( JaxbStub.class ).narrow( jaxbObject )
     );
 
     jaxbStub.narrow( jaxbObject );
 
     jaxbStub.annotate( XmlType.class )
       .param( NAME, name );
     jaxbStub.annotate( XmlRootElement.class )
       .param( NAME, NamingSupport.createVarName( descriptor.getClassDeclaration().getSimpleName() ) )
       .param( NAMESPACE, NameSpaceSupport.createNameSpaceUriBase( descriptor.getQualifiedName() ) + STUB_NAMESPACE_SUFFIX );
     jaxbStub.annotate( XmlAccessorType.class ).param( VALUE, XmlAccessType.FIELD );
 
 
     addFields( jaxbStub, Scope.STUB );
 
     addGetJaxbType();
   }
 
   private void addGetJaxbType() {
     assert jaxbStub != null;
     assert jaxbObject != null;
 
     JMethod method = jaxbStub.method( JMod.PUBLIC, codeGenerator.ref( Class.class ).narrow( jaxbObject ), "getJaxbType" );
     method.annotate( Override.class );
     method.body()._return( jaxbObject.dotclass() );
   }
 
   /**
    * Adds all fields
    *
    * @param currentClass the jaxb class
    * @param type         the type
    */
   private void addFields( @NotNull JDefinedClass currentClass, @NotNull Scope type ) {
     for ( FieldWithInitializationInfo fieldInfo : descriptor.getFieldInfos() ) {
       //Skip the id, since it is defined in the super class
       if ( fieldInfo.getSimpleName().equals( ID ) ) {
         continue;
       }
 
       if ( !shallAddField( fieldInfo, type ) ) {
         continue;
       }
 
       JClass fieldType = getJaxbModelType( fieldInfo.getType(), true );
       JFieldVar field = addField( currentClass, fieldType, fieldInfo );
 
       if ( TypeUtils.isCollectionType( fieldInfo.getType() ) ) {
         JAnnotationUse annotation = field.annotate( XmlElement.class );
         annotation.param( "name", NamingSupport.createSingular( field.name() ) );
       }
 
       addGetter( currentClass, fieldType, fieldInfo, field );
       addSetter( currentClass, fieldType, fieldInfo, field );
     }
   }
 
   private boolean shallAddFieldCopyStatementToStub( @NotNull FieldTypeInformation fieldInfo ) {
     return !fieldInfo.isCollectionType();
   }
 
   protected boolean shallAddField( @NotNull FieldTypeInformation fieldInfo, @NotNull Scope type ) {
     return codeGenerator.getDecisionCallback().shallAddFieldStatement( this, fieldInfo, type );
   }
 
   private static void addSetter( @NotNull JDefinedClass currentClass, @NotNull JType fieldType, @NotNull FieldInfo fieldInfo, @NotNull JVar field ) {
     JMethod setter = currentClass.method( JMod.PUBLIC, Void.TYPE, NamingSupport.createSetter( fieldInfo.getSimpleName() ) );
     JVar param = setter.param( fieldType, fieldInfo.getSimpleName() );
     setter.body().assign( JExpr._this().ref( field ), param );
   }
 
   private static void addGetter( @NotNull JDefinedClass currentClass, @NotNull JType fieldType, @NotNull FieldInfo fieldInfo, @NotNull JExpression field ) {
     JMethod getter = currentClass.method( JMod.PUBLIC, fieldType, NamingSupport.createGetterName( fieldInfo.getSimpleName() ) );
     getter.body()._return( field );
   }
 
   @NotNull
   private static JFieldVar addField( @NotNull JDefinedClass currentClass, @NotNull JType fieldType, @NotNull FieldInfo fieldInfo ) {
     return currentClass.field( JMod.PRIVATE, fieldType, fieldInfo.getSimpleName() );
   }
 
   public enum Scope {
     COMMON,
     JAXB,
     STUB;
 
     public boolean isStub() {
       return this == STUB;
     }
   }
 }
