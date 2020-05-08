 package gov.usgs.gdp.helper;
 
 import static org.junit.Assert.*;
 
 import gov.usgs.gdp.helper.FileHelper;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.TrueFileFilter;
 import org.apache.log4j.Logger;
 import org.geotools.data.FileDataStore;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class FileHelperTest {
 	
 	private static final String testFile = "demo_HUCs";
 	private static final String secondTestFile = "Yahara_River_HRUs_geo_WGS84";
 	private String tempDir = "";
 	private String seperator = "";
 
 	private static org.apache.log4j.Logger log = Logger.getLogger(FileHelperTest.class);
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		log.debug("Started testing class");
 	} 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 		log.debug("Ended testing class");
 	}
 	
 	
 	@Before
 	public void setUp() throws Exception {
 		this.tempDir = System.getProperty("java.io.tmpdir");
 
 		if ( !(this.tempDir.endsWith("/") || this.tempDir.endsWith("\\")) )
 		   this.tempDir = this.tempDir + System.getProperty("file.separator");
 
 		String systemTempDir = System.getProperty("java.io.tmpdir"); 
 		this.seperator =  java.io.File.separator;
 		String currentTime = Long.toString((new Date()).getTime());
 		this.tempDir = systemTempDir + this.seperator + currentTime;
 		(new File(this.tempDir)).mkdir();
 		
 		// Copy example files 
 		ClassLoader cl = Thread.currentThread().getContextClassLoader();
 		URL sampleFileLocation = cl.getResource("Sample_Files/");
 		if (sampleFileLocation != null) {
 			File sampleFiles = null;
 			try {
 				sampleFiles = new File(sampleFileLocation.toURI());
 			} catch (URISyntaxException e) {
 				assertTrue("Exception encountered: " + e.getMessage(), false);
 			}
 			FileHelper.copyFileToFile(sampleFiles, this.tempDir + this.seperator);
 		} else {
 			assertTrue("Sample files could not be loaded for test", false);
 		}
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		FileUtils.deleteDirectory((new File(this.tempDir)));
 	}
 
         @Test
         public void testWipeOldFilesWithNoOldFiles() {
            Collection<File> result = new ArrayList<File>();
           result = FileHelper.wipeOldFiles(new File(this.tempDir), 12000000l);
            assertTrue(result.isEmpty());
         }
 
         @Test
         public void testWipeOldFilesWithOldFiles() {
             Collection<File> result = new ArrayList<File>();
             result = FileHelper.wipeOldFiles(new File(this.tempDir), 1l);
             assertTrue(!result.isEmpty());
         }
 
 	@Test
 	public void testCreateDir() {
 		boolean result = false;
 		String testDir =  System.getProperty("java.io.tmpdir") + 
 			java.io.File.separator + 
 			Long.toString((new Date()).getTime()) + 1;
 		result = FileHelper.createDir(testDir);
 		assertTrue(result);
 		(new File(testDir)).delete();
 	}
 
 	@Test 
 	public void testDoesDirectoryOrFileExist() {
 		boolean result = false;
 		String fileToCheckFor = this.tempDir 
 		+ this.seperator 
 		+ "Sample_Files" 
 		+ this.seperator
 		+ "Shapefiles" 
 		+ this.seperator
 		+ testFile + ".shx";
 		
 		String directoryToCheckFor = this.tempDir 
 		+ this.seperator 
 		+ "Sample_Files" 
 		+ this.seperator
 		+ "Shapefiles" 
 		+ this.seperator;
 		
 		result = FileHelper.doesDirectoryOrFileExist(fileToCheckFor);
 		assertTrue(result);
 		result = FileHelper.doesDirectoryOrFileExist(directoryToCheckFor);
 		assertTrue(result);
 		result = FileHelper.doesDirectoryOrFileExist("does/not/exist");
 		assertFalse(result);
 	}
 	
 	@Test
 	public void testCopyFileToFile() {
 		File fileToCopy = new File(this.tempDir 
 		+ this.seperator 
 		+ "Sample_Files" 
 		+ this.seperator
 		+ "Shapefiles" 
 		+ this.seperator
 		+ testFile + ".shx");
 		
 		String fileToCopyTo = this.tempDir 
 		+ this.seperator 
 		+ "Sample_Files" 
 		+ this.seperator
 		+ "Shapefiles" 
 		+ this.seperator
 		+ testFile + ".COPY";
 		
 		boolean result = false;
 		try {
 			result = FileHelper.copyFileToFile(fileToCopy, fileToCopyTo);
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		assertTrue(result);
 		
 		try {
 			result = FileHelper.copyFileToFile(new File("doesnt/exist"), "doesnt/exist");
 		} catch (IOException e) {
 			assertNotNull(e);
 			result = false;
 		}
 		assertFalse(result);
 		
 	}
 	
 	@Test 
 	public void testDeleteFileQuietly() {
 		String fileToLoad = this.tempDir 
 		+ this.seperator 
 		+ "Sample_Files" 
 		+ this.seperator
 		+ "Shapefiles" 
 		+ this.seperator
 		+ testFile + ".shx";
 		
 		boolean result = FileHelper.deleteFileQuietly("File/That/Doesnt/Exist");
 		assertFalse(result);
 		result = FileHelper.deleteFileQuietly(fileToLoad);
 		assertTrue(result);
 	}
 	
 	
 	@Test 
 	public void testDeleteFile() {
 		String fileToLoad = this.tempDir 
 		+ this.seperator 
 		+ "Sample_Files" 
 		+ this.seperator
 		+ "Shapefiles" 
 		+ this.seperator
 		+ testFile + ".shx";
 		
 		boolean result = false;
 		try {
 			FileHelper.deleteFile("File/That/Doesnt/Exist");
 		} catch (SecurityException e) {
 			fail(e.getMessage());
 		}
 		assertFalse(result);
 		result = FileHelper.deleteFile(fileToLoad);
 		assertTrue(result);
 	}
 	
 	@Test 
 	public void testDeleteDirRecursively() {
 		File lockedFile = new File(this.tempDir 
 		+ this.seperator 
 		+ "Sample_Files" 
 		+ this.seperator
 		+ "Shapefiles" 
 		+ this.seperator
 		+ testFile + ".shx");
 		lockedFile.setWritable(false);
 		
 		String dirToDelete = this.tempDir 
 		+ this.seperator;
 		boolean result = false; 
 			try {
 				result = FileHelper.deleteDirRecursively(new File(dirToDelete));
 				assertTrue(result);
 			} catch (IOException e) {
 				fail(e.getMessage());
 			}
 		
 		
 		try {
 			result = FileHelper.deleteDirRecursively("Directory/That/Doesnt/Exist");
 			assertFalse(result);
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		try {
 			result = FileHelper.deleteDirRecursively(new File("Directory/That/Doesnt/Exist"));
 			assertFalse(result);
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		try {
 			result = FileHelper.deleteDirRecursively(lockedFile);
 		}  catch (IOException e) {
 			fail(e.getMessage());
 		}
 		assertFalse(result);
 		lockedFile.setWritable(true);
 		FileHelper.deleteFileQuietly(lockedFile);
 	}
 	
 	@Test public void testDeleteDirRecursivelyUsingString() {
 		String dirToDelete = this.tempDir 
 		+ this.seperator;
 		boolean result = false;
 		try {
 			result = FileHelper.deleteDirRecursively(dirToDelete);
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		assertTrue(result);
 		try {
 			result = FileHelper.deleteDirRecursively("Directory/That/Doesnt/Exist");
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		assertFalse(result);
 	}
 	
 	@Test
 	public void testFileHelper() {
 		FileHelper result = new FileHelper();
 		assertNotNull(result);
 	}
 
 	@Test
 	public void testFindFile() {
 		String fileToLoad = testFile + ".shx";
 		String rootDir = this.tempDir + this.seperator;
 		File result = FileHelper.findFile(fileToLoad, rootDir);
 		assertNotNull("FineFile did not find the file " + fileToLoad + " within " + rootDir, result);
 		assertEquals("File loaded does not have the same name as the file suggested", fileToLoad, result.getName());
 		result = FileHelper.findFile("should.not.work", rootDir);
 		assertNull(result);
 	}
 
 	@Test
 	public void testGetFileList() {
 		String dirToList = this.tempDir + this.seperator;
 		List<String> result = null;
 		result = FileHelper.getFileList(null, true);
 		assertNull(result);
 		result = FileHelper.getFileList(dirToList, true);
 		assertNotNull("File listing came back null", result);
 		assertFalse("There were no files listed", result.isEmpty());
 		String fakeDirToList = this.tempDir + this.seperator + "9387509352"  + this.seperator;
 		try {
 			result = FileHelper.getFileList(fakeDirToList, true);
 		} catch (IllegalArgumentException e) {
 			assertNotNull(e);
 		}
 		
 	}
 
 	@Test
 	public void testGetSeparator() {
 		String result = FileHelper.getSeparator();
 		assertNotNull(result);
 		assertFalse("".equals(result));
 		log.debug("System separator: " + result);
 	}
 	
 	@Test
 	public void testGetSystemPathSeparator() {
 		String result = FileHelper.getSystemPathSeparator();
 		assertNotNull(result);
 		assertFalse("".equals(result));
 		log.debug("System path separator: " + result);
 	}
 	
 	@Test
 	public void testGetSystemTemp() {
 		String result = FileHelper.getSystemTemp();
 		assertNotNull(result);
 		assertFalse("".equals(result));
 		log.debug("System temp path: " + result);
 	}
 	
 	@Test
 	public void testLoadFile() { 
 		String fileToLoad = this.tempDir 
 			+ this.seperator 
 			+ "Sample_Files" 
 			+ this.seperator
 			+ "Shapefiles" 
 			+ this.seperator
 			+ testFile + ".shx";
 		
 		File result = FileHelper.loadFile(fileToLoad);
 		assertNotNull("File came back null", result);
 		assertTrue("File is not a file", result.isFile());
 	}
 	
 	@Test
 	public void testGetFileCollection() {
 		String dirToList = this.tempDir + this.seperator;
 		Collection<File> result = null;
 		
 		String nullString = null;
 		result = FileHelper.getFileCollection(nullString, true);
 		assertNull(result);
 		result = FileHelper.getFileCollection(dirToList, true);
 		assertNotNull("File listing came back null", result);
 		assertFalse("There were no files listed", result.isEmpty());
 		String fakeDirToList = this.tempDir + this.seperator + "9387509352";
 		try {
 			result = FileHelper.getFileCollection(fakeDirToList, true);
 		} catch (IllegalArgumentException e) {
 			assertNotNull(e);
 		}
 		
 	}
 	
 	@Test
 	public void testGetShapeFileDataStores() {
 		String firstFileToLoad = this.tempDir 
 		+ this.seperator 
 		+ "Sample_Files" 
 		+ this.seperator
 		+ "Shapefiles" 
 		+ this.seperator
 		+ testFile + ".shp";
 		
 		String secondFileToLoad = this.tempDir 
 		+ this.seperator 
 		+ "Sample_Files" 
 		+ this.seperator
 		+ "Shapefiles" 
 		+ this.seperator
 		+ secondTestFile + ".shp";
 		
 		List<String> fileList = new ArrayList<String>();
 		
 		List<FileDataStore> result = null;
 		try {
 			result = FileHelper.getShapeFileDataStores(fileList);
 			assertNotNull(result);
 			assertTrue(result.isEmpty());
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		
 		fileList.add(firstFileToLoad);
 		try {
 			result = FileHelper.getShapeFileDataStores(fileList);
 			assertNotNull(result);
 			assertFalse(result.isEmpty());
 			assertEquals(1, result.size());
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		
 		fileList.add(secondFileToLoad);
 		try {
 			result = FileHelper.getShapeFileDataStores(fileList);
 			assertNotNull(result);
 			assertFalse(result.isEmpty());
 			assertEquals(2, result.size());
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 	}
 	/*
 	@Test 
 	public void testSaveFileItems() {
 		String firstFileToLoad = "hru20VSR.SHP";
 		
 		String secondFileToLoad = "Yahara_River_HRUs_geo_WGS84.shp";
 		
 		boolean result = false;
 		FileItem file1 = new DiskFileItem("A", null, false, firstFileToLoad, 100000000, new File(this.tempDir));
 		FileItem file2 = new DiskFileItem("A", null, false, secondFileToLoad, 100000000, new File(this.tempDir));
 		List<FileItem> files = new ArrayList<FileItem>();
 		files.add(file1);
 		files.add(file2);
 		
 		try {
 			result = FileHelper.saveFileItems(this.tempDir, files);
 			assertTrue(result);
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 	}*/
 }
