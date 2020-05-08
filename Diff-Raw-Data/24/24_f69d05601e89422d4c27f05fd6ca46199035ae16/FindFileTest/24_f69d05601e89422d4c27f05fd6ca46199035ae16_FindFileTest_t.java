 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: FindFileTest.java,v 1.3 2003-03-25 17:50:50 shahbaz.javeed Exp $
  */
 
 package com.netspective.commons.io;
 
 import java.io.IOException;
 import java.io.File;
 import junit.framework.TestCase;
 import com.netspective.commons.text.TextUtils;
 
 public class FindFileTest extends TestCase
 {
 	public void testDirectoryFinder() throws IOException
 	{
 		// Test the ability to locate the test directory
 		final String searchFileName = "com/netspective/commons/io/FindFileTestDirectory";
 
 		FileFind.FileFindResults ffrDir = FileFind.findInClasspath(searchFileName, FileFind.FINDINPATHFLAG_SEARCH_FILE_MAY_BE_DIRECTORY);
 		assertNotNull(ffrDir);
 
 		assertTrue(ffrDir.isFileFound());
         assertEquals(searchFileName, ffrDir.getSearchFileName());
 
 		// Verify that the search path is identical to the classpath...
 		String[] classPath = TextUtils.split(System.getProperty("java.class.path"), File.pathSeparator, true);
         String[] searchPath = ffrDir.getSearchPaths();
 
 		for (int i = 0; i < searchPath.length; i ++)
 			assertEquals(classPath[i], searchPath[i]);
 
 		File foundDir = ffrDir.getFoundFile();
 		assertNotNull(foundDir);
 		assertTrue(foundDir.isDirectory());
 
 		// Search through the exact path that contains the dir to see whether its found or not...
 		int searchPathIndex = ffrDir.getFoundFileInPathItem();
 		String searchPathName = searchPath[searchPathIndex];
         FileFind.FileFindResults ffrConfirm = FileFind.findInPath(new String[] { searchPathName }, searchFileName, FileFind.FINDINPATHFLAG_DEFAULT);
 		assertNotNull(ffrConfirm);
 		assertTrue(ffrConfirm.isFileFound());
 		assertEquals(searchFileName, ffrConfirm.getSearchFileName());
 		assertEquals(0, ffrConfirm.getFoundFileInPathItem());
 	}
 
 	public void testDirFinder() throws IOException
 	{
 		// Test the ability to locate the test directory
 		FileFind.FileFindResults ffrDir = FileFind.findInClasspath("com/netspective/commons/io/FindFileTestDirectory", FileFind.FINDINPATHFLAG_SEARCH_FILE_MAY_BE_DIRECTORY);
 		assertNotNull(ffrDir);
 		assertTrue(ffrDir.isFileFound());
 
 		File foundDir = ffrDir.getFoundFile();
 		assertNotNull(foundDir);
 		assertTrue(foundDir.isDirectory());
 		String dirNameOne = foundDir.getCanonicalPath();
 
 		// Test recursive search...
 		FileFind.FileFindResults ffrDirRecursive = FileFind.findInClasspath("FindFileTestDirectory", FileFind.FINDINPATHFLAG_SEARCH_FILE_MAY_BE_DIRECTORY | FileFind.FINDINPATHFLAG_SEARCH_RECURSIVELY);
 		assertNotNull(ffrDirRecursive);
 		assertTrue(ffrDirRecursive.isFileFound());
 
 		File foundDirRecursive = ffrDir.getFoundFile();
 		assertNotNull(foundDirRecursive);
 		assertTrue(foundDirRecursive.isDirectory());
 		assertEquals(dirNameOne, foundDirRecursive.getCanonicalPath());
 	}
 
 	public void testFileFinder() throws IOException
 	{
         // Test the ability to locate multiple files
 		FileFind.FileFindResults ffrDir = FileFind.findInClasspath("com/netspective/commons/io/FindFileTestDirectory", FileFind.FINDINPATHFLAG_SEARCH_FILE_MAY_BE_DIRECTORY);
 		assertNotNull(ffrDir);
 		assertTrue(ffrDir.isFileFound());
 
 		File foundDir = ffrDir.getFoundFile();
 		assertNotNull(foundDir);
 		assertTrue(foundDir.isDirectory());
 		String dirNameOne = foundDir.getCanonicalPath();
 
 		// Look for particular files known to be in this directory...
        FileFind.FileFindResults ffrFile11C = FileFind.findInPath(new String[] { dirNameOne }, "file11.c", FileFind.FINDINPATHFLAG_DEFAULT);
 		assertNotNull(ffrFile11C);
 		assertTrue(ffrFile11C.isFileFound());
 		assertFalse(ffrFile11C.isFoundFileInJar());
 		assertFalse(ffrFile11C.isFoundFileAbsoluteAndDoesntExist());
 
 		File file11C = ffrFile11C.getFoundFile();
 		assertEquals(dirNameOne + File.separator + "file11.c", file11C.getCanonicalPath());
 
 		assertFalse(FileFind.isJarFile(file11C.getCanonicalPath()));
 		assertEquals("c", FileFind.getExtension(file11C));
 		assertEquals("c", FileFind.getExtension(file11C.getCanonicalPath()));

		// Look for an absolute filename
		FileFind.FileFindResults ffr = FileFind.findInPath(new String[] { dirNameOne }, dirNameOne + File.separator + "file11.c", FileFind.FINDINPATHFLAG_DEFAULT);
		assertTrue(ffr.isFileFound());
		assertFalse(ffr.isFoundFileAbsoluteAndDoesntExist());

		// Look for an absolute filename
		FileFind.FileFindResults ffrTwo = FileFind.findInPath(new String[] { dirNameOne }, dirNameOne + File.separator + "file11.c" + "garbage", FileFind.FINDINPATHFLAG_DEFAULT);
		assertFalse(ffrTwo.isFileFound());
		assertTrue(ffrTwo.isFoundFileAbsoluteAndDoesntExist());
 	}
 
 	public void testJarFileFinder() throws IOException
 	{
 		final String manifest = "META-INF/MANIFEST.MF";
 
 		FileFind.FileFindResults ffrManifest = FileFind.findInClasspath(manifest, FileFind.FINDINPATHFLAG_SEARCH_INSIDE_ARCHIVES_DURING);
 		assertNotNull(ffrManifest);
 		assertTrue(ffrManifest.isFileFound());
 		assertEquals(manifest, ffrManifest.getSearchFileName());
 		assertTrue(ffrManifest.isFoundFileInJar());
 
 		String[] searchPath = ffrManifest.getSearchPaths();
 		int searchPathIdx = ffrManifest.getFoundFileInPathItem();
 		assertTrue(FileFind.existsInJarFile(ffrManifest.getFoundFile(), manifest, true));
		assertTrue(FileFind.existsInJarFile(ffrManifest.getFoundFile(), manifest, false));
 
 		String fileContents = new String (FileFind.getJarEntry(ffrManifest.getFoundFile(), manifest));
 		assertTrue(fileContents.indexOf("Manifest-Version") != -1);
 	}
 
 	public void testFinderErrors()
 	{
 		// Test the ability to locate a non-existant test directory
 		FileFind.FileFindResults ffrDir = FileFind.findInClasspath("com/netspective/commons/io/FileFindTestDirectory", FileFind.FINDINPATHFLAG_SEARCH_INSIDE_ARCHIVES_DURING | FileFind.FINDINPATHFLAG_SEARCH_FILE_MAY_BE_DIRECTORY);
 		assertNotNull(ffrDir);
 		assertFalse(ffrDir.isFileFound());
 		assertNull(ffrDir.getFoundFile());
 
 		// Test the ability to locate a non-existant test directory
 		FileFind.FileFindResults ffrDirTwo = FileFind.findInClasspath(null, FileFind.FINDINPATHFLAG_SEARCH_INSIDE_ARCHIVES_DURING | FileFind.FINDINPATHFLAG_SEARCH_FILE_MAY_BE_DIRECTORY);
 		assertNull(ffrDirTwo);
 
 		// Test recursive search for a non-existant test dir
 		FileFind.FileFindResults ffrDirRecursive = FileFind.findInClasspath("FileFindTestDirectory", FileFind.FINDINPATHFLAG_SEARCH_FILE_MAY_BE_DIRECTORY | FileFind.FINDINPATHFLAG_SEARCH_RECURSIVELY | FileFind.FINDINPATHFLAG_SEARCH_INSIDE_ARCHIVES_DURING);
 		assertNotNull(ffrDirRecursive);
 		assertFalse(ffrDirRecursive.isFileFound());
         assertNull(ffrDirRecursive.getFoundFile());

		assertFalse(FileFind.isJarFile("  "));

		File f = null;
		assertEquals("", FileFind.getExtension(f));
 	}
 
 }
