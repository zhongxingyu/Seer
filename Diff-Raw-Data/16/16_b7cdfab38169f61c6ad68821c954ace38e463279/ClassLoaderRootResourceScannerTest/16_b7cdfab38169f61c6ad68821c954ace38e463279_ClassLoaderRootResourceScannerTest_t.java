 /**
  * Copyright 2012 Alistair Dutton
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.kelveden.rastajax.core;
 
 import com.kelveden.rastajax.core.ClassLoaderRootResourceScanner;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 
 import java.io.IOException;
 import java.util.Set;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.empty;
 import static org.hamcrest.Matchers.is;
 
 
 public class ClassLoaderRootResourceScannerTest {
 
     private final String DUMMY_PACKAGE = "somepackage";
     private final ClassLoader DUMMY_CLASS_LOADER = this.getClass().getClassLoader();
 
     private DynamicClassCompiler compiler = null;
 
     @Rule
     public TemporaryFolder tempFolder = new TemporaryFolder();
 
     @Before
     public void before() throws IOException {
         compiler = new DynamicClassCompiler(tempFolder.newFolder());
     }
 
     @Test
     public void canInstantiate() throws IOException {
         new ClassLoaderRootResourceScanner(DUMMY_CLASS_LOADER, DUMMY_PACKAGE);
     }
 
     @Test
     public void allRootResourceClassesInPackageAreScanned() throws IOException {
 
         // Given
        final String source1 = "package my.package1; import javax.ws.rs.*; @Path(\"some/path1\") public class SomeClass1 { }";
        final String source2 = "package my.package1; import javax.ws.rs.*; @Path(\"some/path2\") public class SomeClass2 { }";
        final String source3 = "package my.package1; import javax.ws.rs.*; @Path(\"some/path3\") public class SomeClass3 { }";
 
         compiler.compileFromSource(source1, source2, source3);
 
         // When
        final ClassLoaderRootResourceScanner scanner = new ClassLoaderRootResourceScanner(compiler.getClassLoader(), "my.package1");
         final Set<Class<?>> results = scanner.scan();
 
         // Then
         assertThat(results.size(), is(3));
     }
 
     @Test
     public void allRootResourceClassesInMultiplePackagesAreScanned() throws IOException {
 
         // Given
         final String source1 = "package package1; import javax.ws.rs.*; @Path(\"some/path1\") public class SomeClass1 { }";
         final String source2 = "package package2; import javax.ws.rs.*; @Path(\"some/path2\") public class SomeClass2 { }";
 
         compiler.compileFromSource(source1, source2);
 
         // When
         final ClassLoaderRootResourceScanner scanner = new ClassLoaderRootResourceScanner(compiler.getClassLoader(), "package1" ,"package2");
         final Set<Class<?>> results = scanner.scan();
 
         // Then
         assertThat(results.size(), is(2));
     }
 
     @Test
     public void onlyRootResourcesAreScanned() throws IOException {
 
         // Given
         final String source1 = "package package1; import javax.ws.rs.*; @Path(\"some/path1\") public class RootResourceClass { }";
         final String source2 = "package package1; public class NotARootResourceClass { }";
 
         compiler.compileFromSource(source1, source2);
 
         // When
         final ClassLoaderRootResourceScanner scanner = new ClassLoaderRootResourceScanner(compiler.getClassLoader(), "package1");
         final Set<Class<?>> results = scanner.scan();
 
         // Then
         assertThat(results.size(), is(1));
         assertThat(results.iterator().next().getSimpleName(), is("RootResourceClass"));
     }
 
     @Test
     public void rootResourceAnnotatedAtInterfaceLevelIsNotScanned() throws IOException {
 
         // Given
         final String interface1 = "package package1; import javax.ws.rs.*; @Path(\"some/path1\") public interface RootResourceInterface { }";
         final String source1 = "package package1; import javax.ws.rs.*; public class RootResourceClass implements RootResourceInterface { }";
 
         compiler.compileFromSource(interface1, source1);
 
         // When
         final ClassLoaderRootResourceScanner scanner = new ClassLoaderRootResourceScanner(compiler.getClassLoader(), "package1");
         final Set<Class<?>> results = scanner.scan();
 
         // Then
         assertThat(results, is(empty()));
     }
 
     @Test
     public void rootResourceAnnotatedAtInterfaceLevelIsScannedIfInstanceIsInterfaceSensitive() throws IOException {
 
         // Given
         final String interface1 = "package package1; import javax.ws.rs.*; @Path(\"some/path1\") public interface RootResourceInterface { }";
         final String source1 = "package package1; import javax.ws.rs.*; public class RootResourceClass implements RootResourceInterface { }";
 
         compiler.compileFromSource(interface1, source1);
 
         // When
         final ClassLoaderRootResourceScanner scanner = new ClassLoaderRootResourceScanner(compiler.getClassLoader(), "package1").allowInterfaceInheritance();
         final Set<Class<?>> results = scanner.scan();
 
         // Then
         assertThat(results.size(), is(1));
         assertThat(results.iterator().next().getSimpleName(), is("RootResourceClass"));
     }
 }
