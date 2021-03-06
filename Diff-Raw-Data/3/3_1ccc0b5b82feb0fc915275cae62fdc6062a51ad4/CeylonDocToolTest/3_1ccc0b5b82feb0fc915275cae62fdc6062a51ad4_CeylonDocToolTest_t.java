 /*
  * Copyright Red Hat Inc. and/or its affiliates and other contributors
  * as indicated by the authors tag. All rights reserved.
  *
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU General Public License version 2.
  * 
  * This particular file is subject to the "Classpath" exception as provided in the 
  * LICENSE file that accompanied this code.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License,
  * along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 package com.redhat.ceylon.ceylondoc.test;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.CharBuffer;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileChannel.MapMode;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import com.redhat.ceylon.ceylondoc.CeylonDocTool;
 import com.redhat.ceylon.ceylondoc.Util;
 import com.redhat.ceylon.compiler.java.tools.CeyloncTool;
 import com.redhat.ceylon.compiler.typechecker.model.Module;
 import com.sun.source.util.JavacTask;
 
 public class CeylonDocToolTest {
 
     private CeylonDocTool tool(String pathname, String testName, String moduleName, String... repositories)
             throws IOException {
         CeylonDocTool tool = new CeylonDocTool(Arrays.asList(new File(pathname)), 
                 Arrays.asList(repositories), 
                 Arrays.asList(moduleName),
                 true/* throw on error */);
         File dir = new File(System.getProperty("java.io.tmpdir"), "CeylonDocToolTest/" + testName);
         if (dir.exists()) {
             Util.delete(dir);
         }
         tool.setOutputRepository(dir.getAbsolutePath());
         return tool;
     }
 
     protected void assertFileExists(File destDir, String path) {
         File file = new File(destDir, path);
         Assert.assertTrue(file + " doesn't exist", file.exists());
         Assert.assertTrue(file + " exists but is not a file", file.isFile());
     }
     
     protected void assertFileNotExists(File destDir, String path) {
         File file = new File(destDir, path);
         Assert.assertFalse(file + " does exist", file.exists());
     }
     
     protected void assertDirectoryExists(File destDir, String path) {
         File file = new File(destDir, path);
         Assert.assertTrue(file + " doesn't exist", file.exists());
         Assert.assertTrue(file + " exist but isn't a directory", file.isDirectory());
     }
     
     static interface GrepAsserter {
 
         void makeAssertions(Matcher matcher);
 
     }
     
     static GrepAsserter AT_LEAST_ONE_MATCH = new GrepAsserter() {
 
         @Override
         public void makeAssertions(Matcher matcher) {
             Assert.assertTrue("Zero matches for " + matcher.pattern().pattern(), matcher.find());
         }
         
     };
     
     static GrepAsserter NO_MATCHES = new GrepAsserter() {
 
         @Override
         public void makeAssertions(Matcher matcher) {
             boolean found = matcher.find();
             if (found) {
                 Assert.fail("Unexpected match for " + matcher.pattern().pattern() + ": " + matcher.group(0));
             }
         }
         
     };
     
     protected void assertMatchInFile(File destDir, String path, Pattern pattern, GrepAsserter asserter) throws IOException {
         assertFileExists(destDir, path);
         Charset charset = Charset.forName("UTF-8");
         
         File file = new File(destDir, path);
         FileInputStream stream = new FileInputStream(file);
         try  {
             FileChannel channel = stream.getChannel();
             try {
                 MappedByteBuffer map = channel.map(MapMode.READ_ONLY, 0, channel.size());
                 CharBuffer chars = charset.decode(map);
                 Matcher matcher = pattern.matcher(chars);
                 asserter.makeAssertions(matcher);
             } finally {
                 channel.close();
             }
         } finally {
             stream.close();
         }
     }
     
     protected void assertMatchInFile(File destDir, String path, Pattern pattern) throws IOException {
         assertMatchInFile(destDir, path, pattern, AT_LEAST_ONE_MATCH);
     }
     
     protected void assertNoMatchInFile(File destDir, String path, Pattern pattern) throws IOException {
         assertMatchInFile(destDir, path, pattern, NO_MATCHES);
     }
     
     @Test
     public void moduleA() throws IOException {
         String pathname = "test-src/com/redhat/ceylon/ceylondoc/test/modules/single";
         String testName = "moduleA";
         CeylonDocTool tool = tool(pathname, testName, "a");
         tool.setShowPrivate(false);
         tool.setOmitSource(false);
         tool.makeDoc();
         
         Module module = new Module();
         module.setName(Arrays.asList("a"));
         module.setVersion("3.1.4");
         
         File destDir = getOutputDir(tool, module);
         
         assertDirectoryExists(destDir, ".resources");
         assertFileExists(destDir, ".resources/index.js");
         assertFileExists(destDir, ".resources/icons.png");
         assertFileExists(destDir, "index.html");
         assertFileExists(destDir, "search.html");
         assertFileExists(destDir, "interface_Types.html");
         assertFileNotExists(destDir, "class_PrivateClass.html");
         assertFileExists(destDir, "class_SharedClass.html");
         assertFileExists(destDir, "class_CaseSensitive.html");
         assertFileNotExists(destDir, "class_caseSensitive.html");
         assertFileExists(destDir, "object_caseSensitive.html");
         
         assertMatchInFile(destDir, "index.html", 
                 Pattern.compile("This is a <strong>test</strong> module"));
         assertMatchInFile(destDir, "index.html", 
                 Pattern.compile("This is a <strong>test</strong> package"));
         
         assertMatchInFile(destDir, "class_SharedClass.html", 
                 Pattern.compile("<.*? id='sharedAttribute'.*?>"));
         assertNoMatchInFile(destDir, "class_SharedClass.html", 
                 Pattern.compile("<.*? id='privateAttribute'.*?>"));
         assertMatchInFile(destDir, "class_SharedClass.html", 
                 Pattern.compile("<.*? id='sharedGetter'.*?>"));
         assertNoMatchInFile(destDir, "class_SharedClass.html", 
                 Pattern.compile("<.*? id='privateGetter'.*?>"));
         assertMatchInFile(destDir, "class_SharedClass.html", 
                 Pattern.compile("<.*? id='sharedMethod'.*?>"));
         assertNoMatchInFile(destDir, "class_SharedClass.html", 
                 Pattern.compile("<.*? id='privateMethod'.*?>"));
         
         assertMatchInFile(destDir, "index.html", 
                 Pattern.compile("<div class='by'>By: Tom Bentley</div>"));
         assertMatchInFile(destDir, "interface_Types.html", 
                 Pattern.compile("<div class='by'>By: Tom Bentley</div>"));
         
         assertMatchInFile(destDir, "class_StubClass.html", 
                 Pattern.compile("<div class='throws'>Throws:"));        
         assertMatchInFile(destDir, "class_StubClass.html", 
                 Pattern.compile("OverflowException<p>if the number is too large to be represented as an integer</p>"));        
         assertMatchInFile(destDir, "class_StubClass.html", 
                 Pattern.compile("<a href='class_StubException.html'>StubException</a><p><code>when</code> with <strong>WIKI</strong> syntax</p>"));
         
         assertFileExists(destDir, "interface_StubClass.StubInnerInterface.html");
         assertFileExists(destDir, "class_StubClass.StubInnerClass.html");
         
         assertMatchInFile(destDir, "class_StubClass.html", 
                 Pattern.compile("Nested Interfaces"));
         assertMatchInFile(destDir, "class_StubClass.html", 
                 Pattern.compile("<a href='interface_StubClass.StubInnerInterface.html'>StubInnerInterface</a>"));
         assertMatchInFile(destDir, "class_StubClass.html", 
                 Pattern.compile("Nested Classes"));
         assertMatchInFile(destDir, "class_StubClass.html", 
                 Pattern.compile("<a href='class_StubClass.StubInnerClass.html'>StubInnerClass</a>"));
         
         assertMatchInFile(destDir, "interface_StubClass.StubInnerInterface.html", 
                 Pattern.compile("Enclosing class: <a href='class_StubClass.html'>StubClass</a>"));
         assertMatchInFile(destDir, "class_StubClass.StubInnerClass.html", 
                 Pattern.compile("Enclosing class: <a href='class_StubClass.html'>StubClass</a>"));
         assertMatchInFile(destDir, "class_StubClass.StubInnerClass.html", 
                 Pattern.compile("Satisfied Interfaces: <a href='interface_StubClass.StubInnerInterface.html'>StubInnerInterface</a>"));
         
         assertIcons(destDir);
     }
     
     private void assertIcons(File destDir) throws IOException {
         assertMatchInFile(destDir, "interface_StubInterface.html", Pattern.compile("Interface <i class='icon-interface'></i><code>StubInterface</code>"));
         assertMatchInFile(destDir, "interface_StubInterface.html", Pattern.compile("id='defaultMethod'><td><code><i class='icon-shared-member'></i>"));
         assertMatchInFile(destDir, "interface_StubInterface.html", Pattern.compile("id='formalMethod'><td><code><i class='icon-shared-member'><i class='icon-decoration-formal'></i></i>"));
         
         assertMatchInFile(destDir, "class_StubClass.html", Pattern.compile("<i class='icon-interface'></i><a href='interface_StubClass.StubInnerInterface.html'>StubInnerInterface</a>"));
         assertMatchInFile(destDir, "class_StubClass.html", Pattern.compile("<i class='icon-class'></i><a href='class_StubClass.StubInnerClass.html'>StubInnerClass</a>"));
         assertMatchInFile(destDir, "class_StubClass.html", Pattern.compile("<i class='icon-class'></i>StubClass()"));
         assertMatchInFile(destDir, "class_StubClass.html", Pattern.compile("id='formalMethod'><td><code><i class='icon-shared-member'><i class='icon-decoration-impl'></i></i>"));
        assertMatchInFile(destDir, "class_StubClass.html", Pattern.compile("id='defaultMethod'><td><code><i class='icon-shared-member'><i class='icon-decoration-over'></i></i>"));        
     }
     
     private File getOutputDir(CeylonDocTool tool, Module module) {
         String outputRepo = tool.getOutputRepository();
         return new File(com.redhat.ceylon.compiler.java.util.Util.getModulePath(new File(outputRepo), module),
                 "module-doc");
     }
 
     @Test
     public void moduleAWithPrivate() throws IOException {
         String pathname = "test-src/com/redhat/ceylon/ceylondoc/test/modules/single";
         String testName = "moduleAWithPrivate";
         CeylonDocTool tool = tool(pathname, testName, "a");
         tool.setShowPrivate(true);
         tool.setOmitSource(false);
         tool.makeDoc();
         
         Module module = new Module();
         module.setName(Arrays.asList("a"));
         module.setVersion("3.1.4");
 
         File destDir = getOutputDir(tool, module);
         
         assertDirectoryExists(destDir, ".resources");
         assertFileExists(destDir, ".resources/index.js");
         assertFileExists(destDir, "index.html");
         assertFileExists(destDir, "search.html");
         assertFileExists(destDir, "interface_Types.html");
         assertFileExists(destDir, "class_PrivateClass.html");
         assertFileExists(destDir, "class_SharedClass.html");
         assertFileExists(destDir, "class_CaseSensitive.html");
         assertFileNotExists(destDir, "class_caseSensitive.html");
         assertFileExists(destDir, "object_caseSensitive.html");
         
         assertMatchInFile(destDir, "index.html", 
                 Pattern.compile("This is a <strong>test</strong> module"));
         assertMatchInFile(destDir, "index.html", 
                 Pattern.compile("This is a <strong>test</strong> package"));
         
         assertMatchInFile(destDir, "class_SharedClass.html", 
                 Pattern.compile("<.*? id='sharedAttribute'.*?>"));
         assertMatchInFile(destDir, "class_SharedClass.html", 
                 Pattern.compile("<.*? id='privateAttribute'.*?>"));
         assertMatchInFile(destDir, "class_SharedClass.html", 
                 Pattern.compile("<.*? id='sharedGetter'.*?>"));
         assertMatchInFile(destDir, "class_SharedClass.html", 
                 Pattern.compile("<.*? id='privateGetter'.*?>"));
         assertMatchInFile(destDir, "class_SharedClass.html", 
                 Pattern.compile("<.*? id='sharedMethod'.*?>"));
         assertMatchInFile(destDir, "class_SharedClass.html", 
                 Pattern.compile("<.*? id='privateMethod'.*?>"));
     }
     
     @Test
     public void dependentOnBinaryModule() throws IOException {
         String pathname = "test-src/com/redhat/ceylon/ceylondoc/test/modules/dependency";
         String testName = "dependentOnBinaryModule";
         
         // compile the b module
         compile(pathname+"/b", "b");
         
         CeylonDocTool tool = tool(pathname+"/c", testName, "c", "build/ceylon-cars");
         tool.makeDoc();
     }
 
     private void compile(String pathname, String moduleName) throws IOException {
         CeyloncTool compiler = new CeyloncTool();
         List<String> options = Arrays.asList("-src", pathname, "-out", "build/ceylon-cars");
         JavacTask task = compiler.getTask(null, null, null, options, Arrays.asList(moduleName), null);
         Boolean ret = task.call();
         Assert.assertEquals("Compilation failed", Boolean.TRUE, ret);
     }
 }
