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
 
 package com.cedarsoft.serialization.codemodel;
 
 import com.cedarsoft.Version;
 import com.cedarsoft.VersionRange;
 import com.sun.codemodel.CodeWriter;
 import com.sun.codemodel.JClass;
 import com.sun.codemodel.JClassAlreadyExistsException;
 import com.sun.codemodel.JCodeModel;
 import com.sun.codemodel.JDefinedClass;
 import com.sun.codemodel.JExpr;
 import com.sun.codemodel.JFieldVar;
 import com.sun.codemodel.JInvocation;
 import com.sun.codemodel.JMethod;
 import com.sun.codemodel.JMod;
 import com.sun.codemodel.JVar;
 import com.sun.codemodel.writer.SingleStreamCodeWriter;
 import org.testng.annotations.*;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.EventListener;
 import java.util.List;
 
 import static org.testng.Assert.*;
 
 /**
  *
  */
 public class CodemodelTest {
   private ByteArrayOutputStream out;
   private CodeWriter codeWriter;
   private JCodeModel model;
 
   @BeforeMethod
   protected void setUp() throws Exception {
     out = new ByteArrayOutputStream();
     codeWriter = new SingleStreamCodeWriter( out );
     model = new JCodeModel();
   }
 
   @Test
   public void testIt() throws IOException, JClassAlreadyExistsException, InterruptedException {
     {
       JDefinedClass fooClass = model._class( "com.cedarsoft.generator.test.Foo" );
       fooClass._implements( EventListener.class );
       fooClass.field( JMod.PRIVATE, String.class, "id" );
     }
     {
       JDefinedClass barClass = model._class( "com.cedarsoft.generator.test.bar.Bar" );
       barClass._implements( EventListener.class );
       barClass.field( Modifier.PRIVATE | Modifier.FINAL, Integer.TYPE, "id" );
       barClass.field( JMod.PRIVATE | JMod.FINAL, Integer.TYPE, "id2" );
     }
 
     model.build( codeWriter );
     assertEquals( out.toString().trim(), "-----------------------------------com.cedarsoft.generator.test.bar.Bar.java-----------------------------------\n" +
       "\n" +
       "package com.cedarsoft.generator.test.bar;\n" +
       "\n" +
       "import java.util.EventListener;\n" +
       "\n" +
       "public class Bar\n" +
       "    implements EventListener\n" +
       "{\n" +
       "\n" +
       "    protected static int id;\n" +
       "    private final int id2;\n" +
       "\n" +
       "}\n" +
       "-----------------------------------com.cedarsoft.generator.test.Foo.java-----------------------------------\n" +
       "\n" +
       "package com.cedarsoft.generator.test;\n" +
       "\n" +
       "import java.util.EventListener;\n" +
       "\n" +
       "public class Foo\n" +
       "    implements EventListener\n" +
       "{\n" +
       "\n" +
       "    private String id;\n" +
       "\n" +
       "}" );
   }
 
   @Test
   public void testGenerics() throws Exception {
     JDefinedClass aClass = model._class( "org.test.MyClass" );
 
     //    JExpression assignment = codeModel.ref( ArrayList.class ).dotclass();
     //    JExpression assignment = codeModel.ref( ArrayList.class ).;
     JInvocation assignment = JExpr._new( model.ref( ArrayList.class ).narrow( String.class ) );
 
     JFieldVar field = aClass.field( JMod.PRIVATE | JMod.FINAL, model.ref( List.class ).narrow( model.ref( String.class ) ), "ids", assignment );
     aClass.field( JMod.PRIVATE | JMod.FINAL, model.ref( List.class ).narrow( model.ref( String.class ).wildcard() ), "ids2", assignment );
 
 
     model.build( codeWriter );
     assertEquals( out.toString().trim(), "-----------------------------------org.test.MyClass.java-----------------------------------\n" +
       "\n" +
       "package org.test;\n" +
       "\n" +
       "import java.util.ArrayList;\n" +
       "import java.util.List;\n" +
       "\n" +
       "public class MyClass {\n" +
       "\n" +
       "    private final List<String> ids = new ArrayList<String>();\n" +
       "    private final List<? extends String> ids2 = new ArrayList<String>();\n" +
       "\n" +
       "}" );
   }
 
   @Test
   public void testConstructor() throws Exception {
     JDefinedClass aClass = model._class( "org.test.DaTestClass" );
 
     JClass versionType = model.ref( Version.class );
     JInvocation versionInvocation = versionType.staticInvoke( "valueOf" ).arg( JExpr.lit( 1 ) ).arg( JExpr.lit( 0 ) ).arg( JExpr.lit( 0 ) );
 
     JClass versionRangeType = model.ref( VersionRange.class );
 
     JInvocation versionRangeInvocation = versionRangeType.staticInvoke( "from" ).arg( versionInvocation ).invoke( "to" ).arg( versionInvocation );
 
     JMethod constructor = aClass.constructor( JMod.PUBLIC );
     constructor.body().invoke( "super" ).arg( "window" ).arg( "namespace/window" ).arg( versionRangeInvocation );
 
     model.build( codeWriter );
     assertEquals( out.toString().trim(), "-----------------------------------org.test.DaTestClass.java-----------------------------------\n" +
       "\n" +
       "package org.test;\n" +
       "\n" +
       "import com.cedarsoft.Version;\n" +
       "import com.cedarsoft.VersionRange;\n" +
       "\n" +
       "public class DaTestClass {\n" +
       "\n" +
       "\n" +
       "    public DaTestClass() {\n" +
       "        super(\"window\", \"namespace/window\", VersionRange.from(Version.valueOf(1, 0, 0)).to(Version.valueOf(1, 0, 0)));\n" +
       "    }\n" +
       "\n" +
       "}" );
   }
 
   @Test
   public void testMethod() throws Exception {
     JDefinedClass aClass = model._class( "org.test.DaTestClass" );
 
     aClass.method( JMod.PUBLIC, String.class, "getString" );
     aClass.method( JMod.PUBLIC, Void.TYPE, "doIt" );
 
     model.build( codeWriter );
     assertEquals( out.toString().trim(), "-----------------------------------org.test.DaTestClass.java-----------------------------------\n" +
       "\n" +
       "package org.test;\n" +
       "\n" +
       "\n" +
       "public class DaTestClass {\n" +
       "\n" +
       "\n" +
       "    public String getString() {\n" +
       "    }\n" +
       "\n" +
       "    public void doIt() {\n" +
       "    }\n" +
       "\n" +
       "}" );
   }
 
   @Test
   public void testMethodBody() throws Exception {
     JDefinedClass aClass = model._class( "org.test.DaTestClass" );
     JMethod method = aClass.method( JMod.PUBLIC, String.class, "getString" );
     JVar param = method.param( String.class, "daString" );
 
     method.body().add( param.invoke( "substring" ).arg( JExpr.lit( 0 ) ).arg( JExpr.lit( 7 ) ) );
     method.body()._return( param.invoke( "length" ) );
 
     model.build( codeWriter );
    assertEquals( out.toString().trim(), "ff" );

   }

 }
