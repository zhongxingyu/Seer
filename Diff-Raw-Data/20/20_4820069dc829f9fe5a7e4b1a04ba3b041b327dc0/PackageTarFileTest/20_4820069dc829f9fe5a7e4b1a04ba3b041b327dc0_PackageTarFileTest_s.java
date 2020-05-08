 /*
  * Jpkg - Java library and tools for operating system package creation.
  *
  * Copyright (c) 2007 Three Rings Design, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. Neither the name of the copyright owner nor the names of contributors
  *    may be used to endorse or promote products derived from this software
  *    without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package com.threerings.jpkg;
 
 import java.io.File;
 import java.util.Arrays;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.tools.tar.TarEntry;
 import org.apache.tools.tar.TarInputStream;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 public class PackageTarFileTest extends TestTarFile
 {
     @Test
     public void testPackageTarFile ()
         throws Exception
     {
         final PackageTarFile tar = new PackageTarFile(TestData.TEMP_DIR);
         try {
             tar.close();
         } finally {
             tar.delete();
         }
     }
 
     @Test
     public void testAddFileNoStrip ()
         throws Exception
     {
         final PackageTarFile tar = new PackageTarFile(TestData.TEMP_DIR);
 
         TarInputStream input = null;
         try {
             tar.addFile(TEST_FILE);
             tar.close();
 
             input = getTarInput(tar);
             final TarEntry entry = input.getNextEntry();
 
             // verify the header looks correct
             assertEquals(PathUtils.stripLeadingSeparators(TEST_FILE.getAbsolutePath()), entry.getName());
 
             // verify there are no more entries in the tar file.
             assertNull(input.getNextEntry());
 
         } finally {
             tar.delete();
             IOUtils.closeQuietly(input);
         }
     }
 
     @Test
     public void testMd5Sums ()
         throws Exception
     {
         final PackageTarFile tar = new PackageTarFile(TestData.TEMP_DIR);
 
         try {
             tar.addFile(TEST_FILE, DESTROOT);
             tar.close();
 
             assertEquals(TEST_FILE_MD5, tar.getMd5s().get("file.txt"));
 
         } finally {
             tar.delete();
         }
     }
 
     @Test
     public void testSmallDataSize ()
         throws Exception
     {
         final PackageTarFile tar = new PackageTarFile(TestData.TEMP_DIR);
 
         try {
             tar.addFile(TEST_FILE, DESTROOT);
             tar.close();
 
             // the test file is less than a kilobyte so the size should be 1
             assertEquals(1, tar.getTotalDataSize());
 
         } finally {
             tar.delete();
         }
     }
 
     @Test
     public void testLargeDataSize ()
         throws Exception
     {
         final PackageTarFile tar = new PackageTarFile(TestData.TEMP_DIR);
 
         try {
             tar.addFile(TestData.LARGE_FILE, DESTROOT);
             tar.close();
 
             assertEquals(TestData.LARGE_FILE_SIZE_KBS, tar.getTotalDataSize());
 
         } finally {
             tar.delete();
         }
     }
 
     @Test
     public void testAddFile ()
         throws Exception
     {
         final PackageTarFile tar = new PackageTarFile(TestData.TEMP_DIR);
 
         TarInputStream input = null;
         try {
             tar.addFile(TEST_FILE, DESTROOT);
             tar.close();
 
             input = getTarInput(tar);
             final TarEntry entry = input.getNextEntry();
 
             // verify the header looks correct
             assertEquals("file.txt", entry.getName());
             assertEquals(TEST_FILE.length(), entry.getSize());
             assertEquals(UnixStandardPermissions.ROOT_USER.getName(), entry.getUserName());
             assertEquals(UnixStandardPermissions.ROOT_USER.getId(), entry.getUserId());
             assertEquals(UnixStandardPermissions.ROOT_GROUP.getName(), entry.getGroupName());
             assertEquals(UnixStandardPermissions.ROOT_GROUP.getId(), entry.getGroupId());
            assertEquals(UnixStandardPermissions.STANDARD_FILE_MODE, entry.getMode());
             assertEquals(TEST_FILE.lastModified(), entry.getModTime().getTime());
 
             // verify the file data looks correct.
             final byte[] data = new byte[(int)entry.getSize()];
             input.read(data);
             final byte[] fileData = FileUtils.readFileToByteArray(TEST_FILE);
             assertTrue(Arrays.equals(fileData, data));
 
             // verify there are no more entries in the tar file.
             assertNull(input.getNextEntry());
 
         } finally {
             tar.delete();
             IOUtils.closeQuietly(input);
         }
     }
 
     @Test
     public void testTrailingDirectorySlashAdded ()
         throws Exception
     {
         final PackageTarFile tar = new PackageTarFile(TestData.TEMP_DIR);
 
         TarInputStream input = null;
         try {
             tar.addFile(TEST_DIR, DESTROOT);
             tar.close();
 
             input = getTarInput(tar);
             final TarEntry entry = input.getNextEntry();
 
             // verify the name has a trailing /
             assertEquals("directory/", entry.getName());
 
             // verify there are no more entries in the tar file.
             assertNull(input.getNextEntry());
 
         } finally {
             tar.delete();
             IOUtils.closeQuietly(input);
         }
     }
 
     @Test
     public void testAddMultipleFiles ()
         throws Exception
     {
         final PackageTarFile tar = new PackageTarFile(TestData.TEMP_DIR);
 
         TarInputStream input = null;
         try {
             tar.addFile(TEST_FILE, DESTROOT);
             tar.addFile(TEST_LINK, DESTROOT);
             tar.addFile(TEST_DIR, DESTROOT);
             tar.addFile(TEST_DIR_FILE, DESTROOT);
             tar.close();
 
             input = getTarInput(tar);
             TarEntry entry = input.getNextEntry();
             assertEquals(TEST_FILE.getName(), entry.getName());
             assertEquals(TEST_FILE.length(), entry.getSize());
             assertEquals(UnixStandardPermissions.STANDARD_FILE_MODE, entry.getMode());
 
             entry = input.getNextEntry();
             assertEquals(TEST_LINK.getName(), entry.getName());
             assertEquals(TEST_LINK.length(), entry.getSize());
             assertEquals(UnixStandardPermissions.STANDARD_FILE_MODE, entry.getMode());
 
             entry = input.getNextEntry();
             assertEquals(TEST_DIR_PATH, entry.getName());
             assertEquals(0, entry.getSize());
             assertEquals(UnixStandardPermissions.STANDARD_DIR_MODE, entry.getMode());
 
             entry = input.getNextEntry();
             assertEquals(TEST_DIR_FILE_PATH, entry.getName());
             assertEquals(TEST_DIR_FILE.length(), entry.getSize());
             assertEquals(UnixStandardPermissions.STANDARD_FILE_MODE, entry.getMode());
 
             assertNull(input.getNextEntry());
 
         } finally {
             tar.delete();
             IOUtils.closeQuietly(input);
         }
     }
 
     @Test
     public void testSimplePermissions ()
         throws Exception
     {
         final PermissionsMap permissions = new PermissionsMap();
         permissions.addPathPermissions("/", new PathPermissions(TEST_USER, TEST_GROUP, TEST_MODE, false));
         // verify that an absolute permission path works.
         permissions.addPathPermissions("/directory", new PathPermissions(TEST_UID, TEST_GID, TEST_MODE, true));
 
         final PackageTarFile tar = new PackageTarFile(TestData.TEMP_DIR, permissions);
 
         TarInputStream input = null;
         try {
             tar.addFile(TEST_FILE, DESTROOT);
             tar.addFile(TEST_DIR, DESTROOT);
             tar.addFile(TEST_DIR_FILE, DESTROOT);
             tar.close();
 
             input = getTarInput(tar);
 
             TarEntry entry = input.getNextEntry();
             assertEquals("file.txt", entry.getName());
             assertEquals(UnixStandardPermissions.ROOT_USER.getName(), entry.getUserName());
             assertEquals(UnixStandardPermissions.ROOT_GROUP.getName(), entry.getGroupName());
             assertEquals(UnixStandardPermissions.STANDARD_FILE_MODE, entry.getMode());
 
             entry = input.getNextEntry();
             assertEquals("directory/", entry.getName());
             assertEquals(TEST_UID, entry.getUserId());
             assertEquals(TEST_GID, entry.getGroupId());
             assertEquals(TEST_MODE, entry.getMode());
 
             entry = input.getNextEntry();
             assertEquals("directory/file.txt", entry.getName());
             assertEquals(TEST_UID, entry.getUserId());
             assertEquals(TEST_GID, entry.getGroupId());
             assertEquals(TEST_MODE, entry.getMode());
 
             assertNull(input.getNextEntry());
         } finally {
             tar.delete();
             IOUtils.closeQuietly(input);
         }
     }
 
     @Test
     public void testRecursivePermissions ()
         throws Exception
     {
         final PermissionsMap permissions = new PermissionsMap();
         permissions.addPathPermissions(
             "non_recursive_dir/", new PathPermissions(TEST_USER, TEST_GROUP, TEST_MODE, false));
         permissions.addPathPermissions(
             "recursive_dir/", new PathPermissions(TEST_USER, TEST_GROUP, TEST_MODE, true));
 
         final PackageTarFile tar = new PackageTarFile(TestData.TEMP_DIR, permissions);
 
         TarInputStream input = null;
         try {
             tar.addFile(NON_RECURSIVE_DIR, RECURSIVE_TEST);
             tar.addFile(RECURSIVE_DIR, RECURSIVE_TEST);
             tar.close();
 
             input = getTarInput(tar);
 
             // verify non recursive permissions worked
             TarEntry entry = input.getNextEntry();
            assertEquals("non_recursive_dir/dir/", entry.getName());
             assertEquals(UnixStandardPermissions.ROOT_USER.getName(), entry.getUserName());
             assertEquals(UnixStandardPermissions.ROOT_USER.getId(), entry.getUserId());
             assertEquals(UnixStandardPermissions.ROOT_GROUP.getName(), entry.getGroupName());
             assertEquals(UnixStandardPermissions.ROOT_GROUP.getId(), entry.getGroupId());
            assertEquals(UnixStandardPermissions.STANDARD_DIR_MODE, entry.getMode());
 
             // verify recursive permissions worked.
             entry = input.getNextEntry();
            assertEquals("recursive_dir/dir/", entry.getName());
             assertEquals(TEST_USER, entry.getUserName());
             assertEquals(TEST_GROUP, entry.getGroupName());
             assertEquals(TEST_MODE, entry.getMode());
 
             assertNull(input.getNextEntry());
 
         } finally {
             tar.delete();
             IOUtils.closeQuietly(input);
         }
     }
 
     @Test
     public void testPermissionMatchesExactPath ()
         throws Exception
     {
         final PermissionsMap permissions = new PermissionsMap();
         permissions.addPathPermissions("/directory/file.txt", new PathPermissions(TEST_USER, TEST_GROUP, TEST_MODE, false));
 
         final PackageTarFile tar = new PackageTarFile(TestData.TEMP_DIR, permissions);
 
         TarInputStream input = null;
         try {
             tar.addFile(TEST_DIR_FILE, DESTROOT);
             tar.close();
 
             input = getTarInput(tar);
 
             final TarEntry entry = input.getNextEntry();
             assertEquals("directory/file.txt", entry.getName());
             assertEquals(TEST_USER, entry.getUserName());
             assertEquals(TEST_GROUP, entry.getGroupName());
             assertEquals(TEST_MODE, entry.getMode());
 
             assertNull(input.getNextEntry());
         } finally {
             tar.delete();
             IOUtils.closeQuietly(input);
         }
     }
 
     @Test
     public void testRecursivePermissionMatchesExactPath ()
         throws Exception
     {
         final PermissionsMap permissions = new PermissionsMap();
         permissions.addPathPermissions("/directory/file.txt", new PathPermissions(TEST_USER, TEST_GROUP, TEST_MODE, true));
 
         final PackageTarFile tar = new PackageTarFile(TestData.TEMP_DIR, permissions);
 
         TarInputStream input = null;
         try {
             tar.addFile(TEST_DIR_FILE, DESTROOT);
             tar.close();
 
             input = getTarInput(tar);
 
             final TarEntry entry = input.getNextEntry();
             assertEquals("directory/file.txt", entry.getName());
             assertEquals(TEST_USER, entry.getUserName());
             assertEquals(TEST_GROUP, entry.getGroupName());
             assertEquals(TEST_MODE, entry.getMode());
 
             assertNull(input.getNextEntry());
         } finally {
             tar.delete();
             IOUtils.closeQuietly(input);
         }
     }
 
     @Test(expected=DuplicatePermissionsException.class)
     public void testDuplicatePermissions ()
         throws Exception
     {
         final PermissionsMap permissions = new PermissionsMap();
         permissions.addPathPermissions(
             "recursive_dir", new PathPermissions(TEST_USER, TEST_GROUP, TEST_MODE, true));
         permissions.addPathPermissions(
             "recursive_dir/dir", new PathPermissions(TEST_USER, TEST_GROUP, TEST_MODE, true));
 
         final PackageTarFile tar = new PackageTarFile(TestData.TEMP_DIR, permissions);
         try {
             tar.addFile(RECURSIVE_DIR, RECURSIVE_TEST);
             tar.close();
         } finally {
             tar.delete();
         }
     }
 
     /** Test files in the destroot. */
     private static final String DESTROOT = new File("src/tests/data/package_destroot").getAbsolutePath();
     static final File TEST_FILE = new File(DESTROOT, "file.txt");
     private static final File TEST_LINK = new File(DESTROOT, "link");
     private static final File TEST_DIR = new File(DESTROOT, "/directory");
     private static final String TEST_DIR_PATH = TEST_DIR.getName() + File.separatorChar;
     private static final File TEST_DIR_FILE = new File(DESTROOT, "/directory/file.txt");
     private static final String TEST_DIR_FILE_PATH = TEST_DIR_PATH + TEST_DIR_FILE.getName();
 
     /** Directories used for recursive permission tests. */
     private static final String RECURSIVE_TEST = new File("src/tests/data/recursive_test").getAbsolutePath();
     private static final File RECURSIVE_DIR = new File(RECURSIVE_TEST, "recursive_dir/dir");
     private static final File NON_RECURSIVE_DIR = new File(RECURSIVE_TEST, "non_recursive_dir/dir");
 
     /** The known md5 checksum for the test file. */
     private static final String TEST_FILE_MD5 = "5228465a3ee0e7630c6748d16ee9dc00";
 
     /** Test users and groups. */
     private static final String TEST_USER = "testuser";
     private static final String TEST_GROUP = "testgroup";
     private static final int TEST_UID = 1001;
     private static final int TEST_GID = 1002;
     private static final int TEST_MODE = 0664;
 }
