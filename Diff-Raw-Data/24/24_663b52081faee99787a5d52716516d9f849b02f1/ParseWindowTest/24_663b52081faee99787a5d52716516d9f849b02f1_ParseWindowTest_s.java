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
 
 package com.cedarsoft.serialization.generator.common.parsing;
 
 import com.cedarsoft.codegen.parser.Parser;
 import com.cedarsoft.codegen.parser.Result;
 import com.google.common.collect.ImmutableList;
 import com.sun.mirror.declaration.ClassDeclaration;
 import com.sun.mirror.declaration.ConstructorDeclaration;
 import com.sun.mirror.declaration.FieldDeclaration;
 import com.sun.mirror.declaration.MethodDeclaration;
 import com.sun.mirror.declaration.ParameterDeclaration;
 import org.junit.*;
 
 import java.io.File;
 import java.net.URL;
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 /**
  *
  */
 public class ParseWindowTest {
   private File javaFile;
 
   @Before
   public void setUp() throws Exception {
    URL resource = getClass().getResource( "/com/cedarsoft/serialization/generator/parsing/test/Window.java" );
     assertNotNull( resource );
     javaFile = new File( resource.toURI() );
     assertTrue( javaFile.exists() );
   }
 
   @Test
   public void testParsing() throws ClassNotFoundException, NoSuchMethodException {
     Result parsed = Parser.parse( null, javaFile );
     assertNotNull( parsed );
 
     assertEquals( 1, parsed.getClassDeclarations().size() );
 
     ClassDeclaration classDeclaration = parsed.getClassDeclaration( "com.cedarsoft.serialization.generator.common.parsing.test.Window" );
     {
       assertEquals( 1, classDeclaration.getConstructors().size() );
       ConstructorDeclaration constructor = classDeclaration.getConstructors().iterator().next();
       assertEquals( 3, constructor.getParameters().size() );
 
       assertEquals( constructor.getDocComment(), " the constructor\n" +
         "\n" +
         " @param description the descri\n" +
         " @param width       the width\n" +
         " @param height      the height\n" );
 
       List<ParameterDeclaration> params = ImmutableList.copyOf( constructor.getParameters() );
 
       assertEquals( "java.lang.String", params.get( 0 ).getType().toString() );
       assertEquals( "description", params.get( 0 ).getSimpleName() );
       assertEquals( null, params.get( 0 ).getDocComment() );
       assertEquals( 53, params.get( 0 ).getPosition().line() );
       assertEquals( 25, params.get( 0 ).getPosition().column() );
 
       assertEquals( "double", params.get( 1 ).getType().toString() );
       assertEquals( "width", params.get( 1 ).getSimpleName() );
       assertEquals( "double", params.get( 2 ).getType().toString() );
       assertEquals( "height", params.get( 2 ).getSimpleName() );
     }
 
     {
       List<FieldDeclaration> fields = ImmutableList.copyOf( classDeclaration.getFields() );
       assertEquals( 3, fields.size() );
       assertEquals( "double", fields.get( 0 ).getType().toString() );
       assertEquals( "double", fields.get( 1 ).getType().toString() );
       assertEquals( "java.lang.String", fields.get( 2 ).getType().toString() );
 
       assertEquals( "width", fields.get( 0 ).getSimpleName() );
       assertEquals( "height", fields.get( 1 ).getSimpleName() );
       assertEquals( "description", fields.get( 2 ).getSimpleName() );
 
       assertEquals( "the comment for field width", fields.get( 0 ).getDocComment().trim() );
       assertEquals( "[private, final]", fields.get( 0 ).getModifiers().toString() );
     }
 
     {
       ImmutableList<MethodDeclaration> methods = ImmutableList.copyOf( classDeclaration.getMethods() );
       assertEquals( 5, methods.size() );
 
       assertEquals( "getWidth", methods.get( 0 ).getSimpleName() );
       assertEquals( "getHeight", methods.get( 1 ).getSimpleName() );
       assertEquals( "getDescription", methods.get( 2 ).getSimpleName() );
       assertEquals( "equals", methods.get( 3 ).getSimpleName() );
       assertEquals( "hashCode", methods.get( 4 ).getSimpleName() );
 
 
       assertEquals( "[public]", methods.get( 0 ).getModifiers().toString() );
       assertEquals( "double", methods.get( 0 ).getReturnType().toString() );
       assertEquals( 0, methods.get( 0 ).getThrownTypes().size() );
       assertEquals( 0, methods.get( 0 ).getFormalTypeParameters().size() );
       assertEquals( 0, methods.get( 0 ).getParameters().size() );
 
       assertEquals( "[public]", methods.get( 3 ).getModifiers().toString() );
       assertEquals( "boolean", methods.get( 3 ).getReturnType().toString() );
       assertEquals( 0, methods.get( 3 ).getThrownTypes().size() );
       assertEquals( 0, methods.get( 3 ).getFormalTypeParameters().size() );
       assertEquals( 1, methods.get( 3 ).getParameters().size() );
       List<ParameterDeclaration> params = ImmutableList.copyOf( methods.get( 3 ).getParameters() );
       assertEquals( 1, params.size() );
       assertEquals( "java.lang.Object", params.get( 0 ).getType().toString() );
       assertEquals( "o", params.get( 0 ).getSimpleName() );
     }
   }
 }
