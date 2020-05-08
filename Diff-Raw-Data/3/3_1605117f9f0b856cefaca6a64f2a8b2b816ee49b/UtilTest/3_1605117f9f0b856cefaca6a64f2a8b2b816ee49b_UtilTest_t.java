 /* Copyright (c) 2009 Stanford University
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 package org.fiz;
 
 import java.io.*;
 import java.lang.reflect.*;
 
 import org.fiz.test.*;
 
 /**
  * Junit tests for the Util class.
  */
 
 public class UtilTest extends junit.framework.TestCase {
     // The following classes used for testing invokeStaticMethod.
     protected static class Dummy {
        
        private Dummy() {}
        
         public static String checkDataset(Dataset d, String key) {
             return d.check(key);
         }
         public static String checkDataset(Dataset d, Dataset d2) {
             return d2.check("name");
         }
         public static String checkDataset(Dataset d) {
             return d.check("name");
         }
         public String foo(String s) {
             return "abc";
         }
         public static void throwError(String message) {
             throw new InternalError(message);
         }
     }
 
     public void test_clearCache_classCache() {
         Config.setDataset("main", new Dataset("searchPackages",
                 "org.fiz"));
         Class result = Util.findClass("Dataset");
         assertEquals("cache successfully loaded", "org.fiz.Dataset",
                 result.getName());
         Config.setDataset("main", new Dataset());
         result = Util.findClass("Dataset");
         assertEquals("path bogus, but cache valid", "org.fiz.Dataset",
                 result.getName());
         Util.clearCache();
         result = Util.findClass("Dataset");
         assertEquals("cache flush so lookup fails", null, result);
     }
     public void test_clearCache_methodCache() {
         Util.methodCache.clear();
         Util.methodCacheMisses = 0;
         Method method = Util.findMethod("org.fiz.UtilTest$Dummy.checkDataset",
                 new Dataset(), "age");
         assertEquals("cache size before clearing", 1, Util.methodCache.size());
         Util.clearCache();
         assertEquals("cache size after clearing", 0, Util.methodCache.size());
     }
 
     public void test_copyStream() throws IOException {
         StringReader in = new StringReader("01234567890abcdefg");
         StringWriter out = new StringWriter();
         in.read(); in.read();
         out.write('x');
         Util.copyStream (in, out);
         assertEquals("destination stream", "x234567890abcdefg",
                 out.toString());
         out.getBuffer().setLength(0);
         out.write('q');
         Util.copyStream (in, out);
         assertEquals("input stream already at end-of-file", "q",
                 out.toString());
     }
 
     public void test_deleteTree_success() {
         (new File("_test1_")).mkdir();
         (new File("_test1_/child")).mkdir();
         TestUtil.writeFile("_test1_/first", "data for first file");
         TestUtil.writeFile("_test1_/child/second", "data for second file");
         assertEquals("successful return value", true,
                 Util.deleteTree("_test1_"));
         assertEquals("directory is gone", false,
                 (new File("_test1_")).exists());
     }
     public void test_deleteTree_failure() throws IOException {
         // This test only works on Windows, where a file cannot be
         // deleted if it is open.
         if (!System.getProperty("os.name").startsWith("Windows")) {
             return;
         }
         TestUtil.writeFile("_test", "data");
         FileReader reader = new FileReader("_test");
         assertEquals("can't delete open file", false,
                 Util.deleteTree("_test"));
         reader.close();
         assertEquals("can delete file once it's closed", true,
                 Util.deleteTree("_test"));
     }
 
     public void test_findClass_returnCachedValue() {
         Util.clearCache();
         Config.setDataset("main", new Dataset("searchPackages",
                 "org.fiz"));
         Class result = Util.findClass("Dataset");
         assertEquals("cache successfully loaded", "org.fiz.Dataset",
                 result.getName());
         Config.setDataset("main", new Dataset());
         result = Util.findClass("Dataset");
         assertEquals("path bogus, but cache valid", "org.fiz.Dataset",
                 result.getName());
     }
     public void test_findClass_classNameWorksImmediately() {
         Util.clearCache();
         Class result = Util.findClass("org.fiz.Dataset");
         assertEquals("name of result class", "org.fiz.Dataset",
                 result.getName());
     }
     public void test_findClass_noSearchPackagesConfig() {
         Config.setDataset("main", new Dataset());
         Class result = Util.findClass("Dataset");
         assertEquals("null result", null, result);
     }
     public void test_findClass_classInPackage() {
         Config.setDataset("main", new Dataset("searchPackages",
                 "foo.bar, org.fiz, bogus.moreBogus"));
         Class result = Util.findClass("Dataset");
         assertEquals("name of result class", "org.fiz.Dataset",
                 result.getName());
     }
     public void test_findClass_notInSearchPackages() {
         Config.setDataset("main", new Dataset("searchPackages",
                 "foo.bar, bogus.moreBogus"));
         Class result = Util.findClass("gorp");
         assertEquals("null result", null, result);
     }
 
      public void test_findFileWithExtension() {
         TestUtil.writeFile("_test_.abc", "abc");
         TestUtil.writeFile("_test_.x", "abc");
         assertEquals("found file", "_test_.x",
                 Util.findFileWithExtension("_test_", ".def", ".x", ".abc"));
         assertEquals("couldn't find file", null,
                 Util.findFileWithExtension("_test_", ".q", ".y"));
         TestUtil.deleteTree("_test_.abc");
         TestUtil.deleteTree("_test_.x");
     }
 
     public void test_findMethod_basics() {
         Util.methodCache.clear();
         Util.methodCacheMisses = 0;
         Method method = Util.findMethod("org.fiz.UtilTest$Dummy.checkDataset",
                 new Dataset(), "age");
         assertEquals("name of method", "checkDataset", method.getName());
     }
     public void test_findMethod_presentInCache() {
         Util.methodCache.clear();
         Util.methodCacheMisses = 0;
         Method method = Util.findMethod("org.fiz.UtilTest$Dummy.checkDataset",
                 new Dataset(), "age");
         assertEquals("name of method", "checkDataset", method.getName());
         assertEquals("cache misses", 1, Util.methodCacheMisses);
         method = Util.findMethod("org.fiz.UtilTest$Dummy.checkDataset",
                 new Dataset(), "age");
         assertEquals("name of method", "checkDataset", method.getName());
         assertEquals("cache misses", 1, Util.methodCacheMisses);
     }
     public void test_findMethod_sameNameDifferentArguments()
             throws IllegalAccessException, InvocationTargetException{
         Util.methodCache.clear();
         Util.methodCacheMisses = 0;
 
         // Lookup up three different methods: same name, but different
         // #'s and/or types of arguments.
         Method method = Util.findMethod("org.fiz.UtilTest$Dummy.checkDataset",
                 new Dataset(), "age");
         assertEquals("# arguments", 2, method.getParameterTypes().length);
         assertEquals("cache misses", 1, Util.methodCacheMisses);
         method = Util.findMethod("org.fiz.UtilTest$Dummy.checkDataset",
                 new Dataset());
         assertEquals("# arguments", 1, method.getParameterTypes().length);
         assertEquals("cache misses", 2, Util.methodCacheMisses);
         method = Util.findMethod("org.fiz.UtilTest$Dummy.checkDataset",
                 new Dataset(), new Dataset());
         assertEquals("# arguments", 2, method.getParameterTypes().length);
         assertEquals("cache misses", 3, Util.methodCacheMisses);
 
         // Now lookup each method, and invoke it to make sure we got the
         // right variant in each case.
         method = Util.findMethod("org.fiz.UtilTest$Dummy.checkDataset",
                 new Dataset(), "age");
         assertEquals("method result", "28", method.invoke(null,
                 new Dataset("name", "Alice", "age", "28"), "age"));
         assertEquals("cache misses", 3, Util.methodCacheMisses);
         method = Util.findMethod("org.fiz.UtilTest$Dummy.checkDataset",
                 new Dataset());
         assertEquals("method result", "Alice", method.invoke(null,
                 new Dataset("name", "Alice", "age", "28")));
         assertEquals("cache misses", 3, Util.methodCacheMisses);
         method = Util.findMethod("org.fiz.UtilTest$Dummy.checkDataset",
                 new Dataset(), new Dataset());
         assertEquals("method result", "Bill", method.invoke(null,
                 new Dataset("name", "Alice"), new Dataset("name", "Bill")));
         assertEquals("cache misses", 3, Util.methodCacheMisses);
     }
     public void test_findMethod_badSyntaxInMethodName() {
         boolean gotException = false;
         try {
             Util.findMethod("bogusName");
         }
         catch (InternalError e) {
             assertEquals("exception message",
                     "illegal method name \"bogusName\" in Util.findMethod " +
                     "(no \".\" separator)",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_findMethod_noSuchClass() {
         Method method = Util.findMethod("bogus.get", "abc");
         assertEquals("null return value", null, method);
     }
     public void test_findMethod_noSuchMethod() {
         Method method = Util.findMethod("Dataset.bogusMethod", "abc");
         assertEquals("null return value", null, method);
     }
     public void test_findMethod_foundIt() {
         Method method = Util.findMethod("Dataset.get", "abc");
         assertEquals("successful return", "get", method.getName());
     }
 
     public void test_getUriAndQuery() {
         ServletRequestFixture request = new ServletRequestFixture();
         request.uri = "/a/b";
         request.queryString = null;
         assertEquals("no query data", "/a/b",
                 Util.getUrlWithQuery(request));
         request.queryString = "x=24&y=13";
         assertEquals("query data", "/a/b?x=24&y=13",
                 Util.getUrlWithQuery(request));
     }
 
     public void test_invokeStaticMethod_basics() {
         assertEquals("result", "34",
                 Util.invokeStaticMethod("org.fiz.UtilTest$Dummy.checkDataset",
                 new Dataset("name", "Alice", "age", "34"), "age"));
     }
     public void test_invokeStaticMethod_cantFindMethod() {
         boolean gotException = false;
         try {
             Util.invokeStaticMethod("Dataset.bogusMethod");
         }
         catch (InternalError e) {
             assertEquals("exception message",
                     "can't find method \"Dataset.bogusMethod\" with " +
                     "matching arguments (Util.invokeStaticMethod)",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_invokeStaticMethod_methodNotStatic() {
         Config.setDataset("main", new Dataset("searchPackages", "org.fiz"));
         boolean gotException = false;
         try {
             Util.invokeStaticMethod("UtilTest$Dummy.foo", "abc");
         }
         catch (InternalError e) {
             assertEquals("exception message",
                     "method \"UtilTest$Dummy.foo\" isn't static " +
                     "(Util.invokeStaticMethod)",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_invokeStaticMethod_methodThrowsError() {
         Config.setDataset("main", new Dataset("searchPackages", "org.fiz"));
         boolean gotException = false;
         try {
             Util.invokeStaticMethod("UtilTest$Dummy.throwError",
                     "sample error message");
         }
         catch (InternalError e) {
             assertEquals("exception message",
                     "exception in method \"UtilTest$Dummy.throwError\" " +
                     "invoked by Util.invokeStaticMethod: sample error message",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
 
     public void test_newInstance_classNotFound() {
         Config.setDataset("main", new Dataset());
         boolean gotException = false;
         try {
             Util.newInstance("BogusClass", null);
         }
         catch (ClassNotFoundError e) {
             assertEquals("exception message",
                     "couldn't find class \"BogusClass\"",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_newInstance_requiredTypeClassNotFound() {
         boolean gotException = false;
         try {
             Util.newInstance("org.fiz.Dataset", "NonexistentType");
         }
         catch (ClassNotFoundError e) {
             assertEquals("exception message",
                     "couldn't find class \"NonexistentType\"",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_newInstance_notRequiredType() {
         boolean gotException = false;
         try {
             Util.newInstance("org.fiz.Dataset", "java.lang.String");
         }
         catch (InstantiationError e) {
             assertEquals("exception message",
                     "couldn't create an instance of class " +
                     "\"org.fiz.Dataset\": class isn't " +
                     "a subclass of java.lang.String",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_newInstance_cantFindMatchingConstructor() {
         boolean gotException = false;
         try {
             Util.newInstance("org.fiz.Dataset", null, new int[]{3,4});
         }
         catch (InstantiationError e) {
             assertEquals("exception message",
                     "couldn't create an instance of class " +
                     "\"org.fiz.Dataset\": couldn't " +
                     "find appropriate constructor " +
                     "(org.fiz.Dataset.<init>([I))",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_newInstance_argumentsForConstructor() {
         Object result = Util.newInstance("org.fiz.UtilTest1", null,
                 new Dataset("xyz", "abc"), "value1");
         assertEquals ("class of result", "org.fiz.UtilTest1",
                 result.getClass().getName());
         assertEquals ("first argument", "abc",
                 ((UtilTest1) result).dataset.get("xyz"));
         assertEquals ("second argument", "value1",
                 ((UtilTest1) result).string);
     }
     public void test_newInstance_exceptionInConstructor() {
         boolean gotException = false;
         try {
             Util.newInstance("org.fiz.UtilTest1", null, new Dataset(),
                     "error");
         }
         catch (InstantiationError e) {
             assertEquals("exception message",
                     "couldn't create an instance of class " +
                     "\"org.fiz.UtilTest1\": exception " +
                     "in constructor: test exception message",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
 
     public void test_readFile_shortFile() throws FileNotFoundException {
         String contents = "Line 1\nLine 2\n";
         TestUtil.writeFile("test.foo", contents);
         StringBuilder s = Util.readFile("test.foo");
         assertEquals("file length", 14, s.length());
         assertEquals("file contents", contents, s.toString());
         TestUtil.deleteTree("test.foo");
     }
     public void test_readFile_longFile() throws FileNotFoundException {
         StringBuilder contents = new StringBuilder();
         for (int i = 0; i <1000; i++) {
             contents.append(String.format("This is line #%04d\n", i));
         }
         TestUtil.writeFile("test.foo", contents.toString());
         StringBuilder s = Util.readFile("test.foo");
         assertEquals("file length", 19000, s.length());
         assertEquals("check one line", "This is line #0010",
                 s.substring(190, 208));
         assertEquals("full file contents", contents.toString(), s.toString());
         TestUtil.deleteTree("test.foo");
     }
     public void test_readFile_FileNotFoundException() {
         boolean gotException = false;
         try {
             Util.readFile("bogus/nonexistent");
         }
         catch (FileNotFoundException e) {
             assertEquals("exception message",
                     "bogus/nonexistent (...",
                     TestUtil.truncate(e.getMessage(), "ent (").replace(
                     '\\', '/'));
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_readFileFromPath() throws FileNotFoundError {
         (new File("_test1_")).mkdir();
         (new File("_test1_/child")).mkdir();
         TestUtil.writeFile("_test1_/foo.txt", "_test1_/foo.txt");
         TestUtil.writeFile("_test1_/child/foo.txt", "_test1_/child/foo.txt");
         StringBuilder contents = Util.readFileFromPath("foo.txt", "test",
                 ".", "bogus/xyz", "_test1_", "_test1_/child");
         assertEquals("full file contents", "_test1_/foo.txt",
                 contents.toString());
         TestUtil.deleteTree("_test1_");
     }
     public void test_readFileFromPath_notFound() {
         boolean gotException = false;
         try {
             Util.readFileFromPath("src", "test", ".", "bogus1/a",
                     "bogus2/x/y");
         }
         catch (FileNotFoundError e) {
             assertEquals("exception message",
                     "couldn't find test file \"src\" in path "
                     + "(\".\", \"bogus1/a\", \"bogus2/x/y\")",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
 
     public void test_respondWithFile() throws IOException {
         TestUtil.writeFile("test.html", "0123456789abcde");
         ServletResponseFixture response = new ServletResponseFixture();
         Util.respondWithFile(new File("test.html"), response);
         assertEquals("content length", 15, response.contentLength);
         assertEquals("returned data", "0123456789abcde",
                 response.toString());
         TestUtil.deleteTree("test.html");
     }
 }
