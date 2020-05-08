 /**
  * Copyright (C) 2013 Future Invent Informationsmanagement GmbH. All rights
  * reserved. <http://www.fuin.org/>
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 3 of the License, or (at your option) any
  * later version.
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.fuin.units4j.analyzer;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.fuin.utils4j.Utils4J;
 import org.junit.Test;
 
 // CHECKSTYLE:OFF
 
 /**
  * Test for {@link MethodCallAnalyzer}.
  */
 public final class MethodCallAnalyzerTest {
 
     @Test
     public final void testArrayConstructor() {
 
         // PREPARE & TEST
         final MCAMethod setScale = new MCAMethod(BigDecimal.class.getName(),
                 BigDecimal.class.getName() + " setScale(int)");
         final MethodCallAnalyzer testee = new MethodCallAnalyzer(setScale);
 
         // VERIFY
         assertThat(testee.getMethodCalls()).isEmpty();
         assertThat(testee.getMethodsToFind()).containsOnly(setScale);
 
     }
 
     @Test
     public final void testListConstructor() {
 
         // PREPARE & TEST
         final MCAMethod setScale = new MCAMethod(BigDecimal.class.getName(),
                 BigDecimal.class.getName() + " setScale(int)");
         final List<MCAMethod> methodsToFind = new ArrayList<MCAMethod>();
         methodsToFind.add(setScale);
         final MethodCallAnalyzer testee = new MethodCallAnalyzer(methodsToFind);
 
         // VERIFY
         assertThat(testee.getMethodCalls()).isEmpty();
         assertThat(testee.getMethodsToFind()).containsOnly(setScale);
 
     }
 
     @Test
     public final void testFindCallingMethodsInDir() {
 
         // PREPARE
         final File binDir = new File("target/test-classes");
 
         final MCAMethod setScale = new MCAMethod(BigDecimal.class.getName(),
                 BigDecimal.class.getName() + " setScale(int)");
         final MCAMethod divide = new MCAMethod(BigDecimal.class.getName(),
                 BigDecimal.class.getName() + " divide(" + BigDecimal.class.getName() + ")");
         final MCAMethod superCall = new MCAMethod(
                 "org.fuin.units4j.analyzer.FindMethodCallExampleClasz", "void <init>()");
 
         final MethodCallAnalyzer testee = new MethodCallAnalyzer(setScale, divide, superCall);
 
         // TEST
         testee.findCallingMethodsInDir(binDir);
 
         // VERIFY
         final List<String> list = new ArrayList<String>();
         for (MCAMethodCall call : testee.getMethodCalls()) {
             list.add(call.toString());
         }
         assertThat(list)
                 .containsExactly(
                         "Source='FindMethodCallExampleClasz.java', Line=28, Class='org.fuin.units4j.analyzer.FindMethodCallExampleClasz$InnerExampleClasz, Method='void <init>()' ==CALLS==> Class='org.fuin.units4j.analyzer.FindMethodCallExampleClasz, Method='void <init>()'",
                         "Source='FindMethodCallExampleClasz.java', Line=32, Class='org.fuin.units4j.analyzer.FindMethodCallExampleClasz$InnerExampleClasz, Method='void a()' ==CALLS==> Class='java.math.BigDecimal, Method='java.math.BigDecimal setScale(int)'",
                        "Source='FindMethodCallExampleClasz.java', Line=12, Class='org.fuin.units4j.analyzer.FindMethodCallExampleClasz, Method='void <clinit>()' ==CALLS==> Class='java.math.BigDecimal, Method='java.math.BigDecimal setScale(int)'",
                         "Source='FindMethodCallExampleClasz.java', Line=17, Class='org.fuin.units4j.analyzer.FindMethodCallExampleClasz, Method='void <init>()' ==CALLS==> Class='java.math.BigDecimal, Method='java.math.BigDecimal divide(java.math.BigDecimal)'",
                         "Source='FindMethodCallExampleClasz.java', Line=21, Class='org.fuin.units4j.analyzer.FindMethodCallExampleClasz, Method='void a()' ==CALLS==> Class='java.math.BigDecimal, Method='java.math.BigDecimal setScale(int)'",
                        "Source='FindMethodCallExampleClasz.java', Line=25, Class='org.fuin.units4j.analyzer.FindMethodCallExampleClasz, Method='boolean b(java.lang.Integer, java.lang.Boolean, java.lang.String)' ==CALLS==> Class='java.math.BigDecimal, Method='java.math.BigDecimal setScale(int)'");
 
     }
 
     @Test
     public final void testFindCallingMethodsInJar() throws IOException {
 
         // PREPARE
         final File binDir = new File("target/test-classes");
         final File zipFile = File.createTempFile("MethodCallAnalyzerTest-", ".jar");
         Utils4J.zipDir(binDir, null, zipFile);
 
         final MCAMethod setScale = new MCAMethod(BigDecimal.class.getName(),
                 BigDecimal.class.getName() + " setScale(int)");
         final MCAMethod divide = new MCAMethod(BigDecimal.class.getName(),
                 BigDecimal.class.getName() + " divide(" + BigDecimal.class.getName() + ")");
         final MCAMethod superCall = new MCAMethod(
                 "org.fuin.units4j.analyzer.FindMethodCallExampleClasz", "void <init>()");
 
         final MethodCallAnalyzer testee = new MethodCallAnalyzer(setScale, divide, superCall);
 
         // TEST
         testee.findCallingMethodsInJar(zipFile);
 
         // VERIFY
         final List<String> list = new ArrayList<String>();
         for (MCAMethodCall call : testee.getMethodCalls()) {
             list.add(call.toString());
         }
         assertThat(list)
                 .containsExactly(
                         "Source='FindMethodCallExampleClasz.java', Line=28, Class='org.fuin.units4j.analyzer.FindMethodCallExampleClasz$InnerExampleClasz, Method='void <init>()' ==CALLS==> Class='org.fuin.units4j.analyzer.FindMethodCallExampleClasz, Method='void <init>()'",
                         "Source='FindMethodCallExampleClasz.java', Line=32, Class='org.fuin.units4j.analyzer.FindMethodCallExampleClasz$InnerExampleClasz, Method='void a()' ==CALLS==> Class='java.math.BigDecimal, Method='java.math.BigDecimal setScale(int)'",
                        "Source='FindMethodCallExampleClasz.java', Line=12, Class='org.fuin.units4j.analyzer.FindMethodCallExampleClasz, Method='void <clinit>()' ==CALLS==> Class='java.math.BigDecimal, Method='java.math.BigDecimal setScale(int)'",
                         "Source='FindMethodCallExampleClasz.java', Line=17, Class='org.fuin.units4j.analyzer.FindMethodCallExampleClasz, Method='void <init>()' ==CALLS==> Class='java.math.BigDecimal, Method='java.math.BigDecimal divide(java.math.BigDecimal)'",
                         "Source='FindMethodCallExampleClasz.java', Line=21, Class='org.fuin.units4j.analyzer.FindMethodCallExampleClasz, Method='void a()' ==CALLS==> Class='java.math.BigDecimal, Method='java.math.BigDecimal setScale(int)'",
                        "Source='FindMethodCallExampleClasz.java', Line=25, Class='org.fuin.units4j.analyzer.FindMethodCallExampleClasz, Method='boolean b(java.lang.Integer, java.lang.Boolean, java.lang.String)' ==CALLS==> Class='java.math.BigDecimal, Method='java.math.BigDecimal setScale(int)'");
 
     }
 
 }
 // CHECKSTYLE:ON
