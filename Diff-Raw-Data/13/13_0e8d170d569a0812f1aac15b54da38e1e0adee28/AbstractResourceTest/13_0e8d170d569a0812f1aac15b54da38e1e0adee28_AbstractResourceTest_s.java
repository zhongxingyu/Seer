 /*
  * Copyright 2012 Marco Soeima
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package com.soeima.resources;
 
 import com.soeima.resources.util.IOUtil;
 import com.soeima.resources.util.Paths;
 import org.junit.AfterClass;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.fail;
 import org.junit.Test;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 /**
  * Provides a common infrastructure for {@link Resource} unit tests.
  *
  * @author   <a href="mailto:marco.soeima@gmail.com">Marco Soeima</a>
  * @version  2012/09/30
  */
 public abstract class AbstractResourceTest {
 
     /** A resource. */
     protected static final String TEST_FILE_RESOURCE1 = "test1.file";
 
     /** A second resource. */
     protected static final String TEST_FILE_RESOURCE2 = "test2.file";
 
     /** The root directory of the unit tests. */
     private static File testDir;
 
     /** {@link #testDir}'s absolute path. */
     private static String testDirPath;
 
     /**
      * Creates a new {@link AbstractResourceTest} object.
      */
     protected AbstractResourceTest() {
     }
 
     /**
      * Returns the test directory as a {@link File}.
      *
      * @return  The test directory as a {@link File}.
      */
     protected static File getTestDir() {
         return testDir;
     }
 
     /**
      * Returns the test directory as a string.
      *
      * @return  The test directory as a string.
      */
     protected static String getTestDirPath() {
         return testDirPath;
     }
 
     /**
      * Sets the up the unit tests.
      *
      * @param  testPath  The path to the test directory.
      */
     protected static void setUp(String testPath) {
         testDir =
             createDirs(Paths.join(System.getProperty("java.io.tmpdir"), testPath),
                        "a",
                        "b",
                        "c",
                        "d",
                        Paths.join("a", "a"));
         testDirPath = testDir.getAbsolutePath();
 
         try {
             new File(Paths.join(testDirPath, TEST_FILE_RESOURCE1)).createNewFile();
             new File(Paths.join(Paths.join(testDirPath, "a"), TEST_FILE_RESOURCE1)).createNewFile();
             new File(Paths.join(Paths.join(testDirPath, "a", "a"), TEST_FILE_RESOURCE1)).createNewFile();
             new File(Paths.join(Paths.join(testDirPath, "a", "a"), TEST_FILE_RESOURCE2)).createNewFile();
             new File(Paths.join(Paths.join(testDirPath, "b"), TEST_FILE_RESOURCE2)).createNewFile();
             new File(Paths.join(Paths.join(testDirPath, "c"), TEST_FILE_RESOURCE2)).createNewFile();
         }
         catch (IOException e) {
         }
     }
 
     /**
      * Cleans up the mess left over by the unit tests.
      */
     @AfterClass public static void tearDown() {
         deletePath(testDir);
     }
 
     /**
      * Creates the given root <code>path</code> and all of its <code>subpaths</code>.
      *
      * @param   path      The root path.
      * @param   subpaths  The paths to place inside the root path.
      *
      * @return  The root path.
      */
     private static File createDirs(String path, String... subpaths) {
         File root = new File(path);
         String rootPath = root.getAbsolutePath();
         root.mkdir();
 
         for (String subpath : subpaths) {
             File dir = new File(Paths.join(rootPath, subpath));
             dir.mkdirs();
         }
 
         return root;
     }
 
     /**
      * Recursively deletes the given <code>path</code>.
      *
      * @param   path  The path to recursively delete.
      *
      * @return  <code>true</code> if <code>path</code> was successfully delete; <code>false</code> otherwise.
      */
     private static boolean deletePath(File path) {
 
         if (!path.exists()) {
             return false;
         }
 
         boolean success = false;
 
         for (File file : path.listFiles()) {
 
             if (file.isDirectory()) {
                 success |= deletePath(file);
             }
             else {
                 success |= file.delete();
             }
         }
 
         return (success |= path.delete());
     }
 
     /**
      * Returns the protocol scheme used by the resources of this unit test.
      *
      * @param   path
      *
      * @return  The protocol scheme.
      */
     protected abstract String toURL(String path);
 
     /**
      * Returns the resource path to use within the resource loader for this unit test.
      *
      * @return  The resource path to use for the resource loader.
      */
     protected abstract String getResourcePath();
 
     /**
      * Performs non-recursive resource loading tests.
      */
     @Test public void testNonRecursive() {
         String resourcePath = getResourcePath();
         ResourceLoader rl = new ResourceLoader();
         rl.addPath(resourcePath);
         Resource resource = rl.getResource(TEST_FILE_RESOURCE1);
         assertNotNull(resource);
         InputStream is = null;
 
         try {
             is = resource.getInputStream();
             assertNotNull(is);
         }
         finally {
             IOUtil.close(is);
         }
 
         // Look for all resources with the "file" prefix.
         List<Resource> resources = rl.getResourcesForExtension(".file");
         assertNotNull(resources);
         assertFalse(resources.isEmpty());
         assertEquals(1, resources.size());
 
         try {
             is = resources.get(0).getInputStream();
             assertNotNull(is);
         }
         finally {
             IOUtil.close(is);
         }
 
         // Use a protocol.
         rl = new ResourceLoader();
         rl.addPath(toURL(resourcePath));
         resource = rl.getResource(TEST_FILE_RESOURCE1);
         assertNotNull(resource);
 
         try {
             is = resource.getInputStream();
             assertNotNull(is);
         }
         finally {
             IOUtil.close(is);
         }
 
         // Look for "a/TEST_FILE_RESOURCE1".
         String relativePath = Paths.join("a", TEST_FILE_RESOURCE1);
         resource = rl.getResource(relativePath);
         assertNotNull(resource);
 
         try {
             is = resource.getInputStream();
             assertNotNull(is);
         }
         finally {
             IOUtil.close(is);
         }
 
         compareURLs(resourcePath, relativePath, resource);
         assertNotNull(resource.getURI());
 
         // Look for "c/TEST_FILE_RESOURCE2".
         relativePath = Paths.join("c", TEST_FILE_RESOURCE2);
         resource = rl.getResource(relativePath);
         assertNotNull(resource);
 
         try {
             is = resource.getInputStream();
             assertNotNull(is);
         }
         finally {
             IOUtil.close(is);
         }
 
         compareURLs(resourcePath, relativePath, resource);
         assertNotNull(resource.getURI());
 
         // Look for "a/a/TEST_FILE_RESOURCE1".
         relativePath = Paths.join("a", "a", TEST_FILE_RESOURCE1);
         resource = rl.getResource(relativePath);
         assertNotNull(resource);
 
         try {
             is = resource.getInputStream();
             assertNotNull(is);
         }
         finally {
             IOUtil.close(is);
         }
 
         compareURLs(resourcePath, relativePath, resource);
         assertNotNull(resource.getURI());
 
         // Look for non-existent resources.
         resource = rl.getResource("foo");
         assertNull(resource);
 
         // See if an exception is thrown if we try to search in a directory that does not exist.
         try {
             rl.addPath("foo");
             fail("Should have raised a ResourceException");
         }
         catch (ResourceException e) {
             // Expected.
         }
 
         try {
             rl.addPath(toURL("foo"));
             fail("Should have raised a ResourceException");
         }
         catch (ResourceException e) {
             // Expected.
         }
     } // end method testNonRecursive
 
     /**
      * Compares two <tt>URL</tt>s by constructing a <tt>URL</tt> from the given <code>path</code> and <code>
      * relativePath</code> and comparing it with the <code>resource</code> <tt>URL</tt>.
      *
      * @param  path          The path with which to construct a <tt>URL</tt>.
      * @param  relativePath  The relative path with which to construct a <tt>URL</tt>.
      * @param  resource      The resource whose <tt>URL</tt> will be compared with the one constructed with <code>
      *                       path</code> and <code>relativePath</code>.
      */
     private void compareURLs(String path, String relativePath, Resource resource) {
         assertEquals(Paths.normalize(Paths.join(toURL(path), relativePath), '/'), resource.getURI().toString());
     }
 
     /**
      * Performs recursive resource loading tests.
      */
     @Test public void testRecursive() {
         ResourceLoader rl = new ResourceLoader();
         rl.setRecursionType(RecursionType.Recursive);
         rl.addPath(getResourcePath());
         Resource resource = rl.getResource(TEST_FILE_RESOURCE1);
         assertNotNull(resource);
         InputStream is = null;
 
         try {
             is = resource.getInputStream();
             assertNotNull(is);
         }
         finally {
             IOUtil.close(is);
         }
 
         List<Resource> resources = rl.getResourcesForExtension("file");
         assertNotNull(resources);
         assertFalse(resources.isEmpty());
        assertEquals(6, resources.size());
 
         for (Resource r : resources) {
 
             try {
                 is = resource.getInputStream();
                 assertNotNull(is);
             }
             finally {
                 IOUtil.close(is);
             }
 
             assertNotNull(r.getPath());
             assertNotNull(r.getURI());
         }
 
         // Look for "a/TEST_FILE_RESOURCE1".
         resources = rl.getResources(Paths.join("a", TEST_FILE_RESOURCE1));
         assertNotNull(resources);
         assertEquals(2, resources.size());
 
         // Look for all "TEST_FILE_RESOURCE2".
         resources = rl.getResources(TEST_FILE_RESOURCE2);
         assertNotNull(resources);
         assertEquals(3, resources.size());
     } // end method testRecursive
 } // end class AbstractResourceTest
