 package com.gentics.cr.util.file;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class DirectoryScannerTest {
 	
 	File directory;
 	
 	@Before
 	public void prepare() throws URISyntaxException {
 		directory = new File(DirectoryScannerTest.class.getResource("DirectoryScanner").toURI());
 	}
 	
 	@Test
 	public void scanFiles() {
 		ArrayList<File> files = new ArrayList<File>();
 		files.addAll(Arrays.asList(DirectoryScanner.listFiles(directory, null)));
 		assertEquals("Directory did contain more/less files than expected.", 3, files.size());
 		assertTrue("Directory did not contain a folder \"folderA\"", files.contains(new File(directory, "folderA")));
 		assertTrue("Directory did not contain a file \"folderA/a.file\"",
 				files.contains(new File(new File(directory, "folderA"), "a.file")));
 		assertTrue("Directory did not contain a file \"b.file\"", files.contains(new File(directory, "b.file")));
 	}
 	
 	@Test
 	public void scanFileStrings() {
 		ArrayList<String> files = new ArrayList<String>();
 		files.addAll(Arrays.asList(DirectoryScanner.list(directory, null)));
 		assertEquals("Directory did contain more/less files than expected.", 3, files.size());
 		assertTrue("Directory did not contain a folder \"folderA\"", files.contains("folderA"));
 		assertTrue("Directory did not contain a file \"folderA" + File.separator + "a.file\"",
				files.contains("folderA/a.file"));
 		assertTrue("Directory did not contain a file \"b.file\"", files.contains("b.file"));
 	}
 	
 	@Test
 	public void scanFilesWithFilterOnRootLevel() {
 		ArrayList<File> files = new ArrayList<File>();
 		files.addAll(Arrays.asList(DirectoryScanner.listFiles(directory, "b\\.file")));
 		assertEquals("Directory did contain more/less files than expected.", 1, files.size());
 		assertTrue("Directory did not contain a file \"b.file\"", files.contains(new File(directory, "b.file")));
 	}
 	
 	@Test
 	public void scanFileStringsWithFilterOnRootLevel() {
 		ArrayList<String> files = new ArrayList<String>();
 		files.addAll(Arrays.asList(DirectoryScanner.list(directory, "b\\.file")));
 		assertEquals("Directory did contain more/less files than expected.", 1, files.size());
 		assertTrue("Directory did not contain a file \"b.file\"", files.contains("b.file"));
 	}
 	
 	@Test
 	public void scanFilesWithFilterOnSubLevel() {
 		ArrayList<File> files = new ArrayList<File>();
 		files.addAll(Arrays.asList(DirectoryScanner.listFiles(directory, "a\\.file")));
 		assertEquals("Directory did contain more/less files than expected.", 1, files.size());
 		assertTrue("Directory did not contain a file \"folderA/a.file\"", 
 				files.contains(new File(new File(directory, "folderA"), "a.file")));
 	}
 	
 	@Test
 	public void scanFileStringsWithFilterOnSubLevel() {
 		ArrayList<String> files = new ArrayList<String>();
 		files.addAll(Arrays.asList(DirectoryScanner.list(directory, "a\\.file")));
 		assertEquals("Directory did contain more/less files than expected.", 1, files.size());
 		assertTrue("Directory did not contain a file \"folderA/a.file\"", files.contains("folderA" + File.separator + "a.file"));
 	}
 }
