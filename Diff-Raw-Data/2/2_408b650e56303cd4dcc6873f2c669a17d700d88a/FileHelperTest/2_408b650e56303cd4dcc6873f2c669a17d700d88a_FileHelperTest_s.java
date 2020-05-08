 package gov.usgs.cida.gdp.utilities;
 
 import org.junit.Ignore;
 import java.io.FileNotFoundException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import org.slf4j.LoggerFactory;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.hamcrest.Matchers.*;
 
 public class FileHelperTest {
 
     private static String sampleDir;
     private static String testFilePath;
     private static final String testFile = "demo_HUCs";
     private static final String secondTestFile = "Yahara_River_HRUs_geo_WGS84";
     private String tempDir = "";
     private String seperator = "";
     private static org.slf4j.Logger log = LoggerFactory.getLogger(FileHelperTest.class);
 
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
         log.debug("Started testing class: " + FileHelperTest.class.getName());
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws Exception {
         log.debug("Ended testing class: " + FileHelperTest.class.getName());
     }
 
     @Before
     public void setUp() throws Exception {
         this.tempDir = System.getProperty("java.io.tmpdir");
 
         if (!(this.tempDir.endsWith("/") || this.tempDir.endsWith("\\"))) {
             this.tempDir = this.tempDir + System.getProperty("file.separator");
         }
 
         String systemTempDir = System.getProperty("java.io.tmpdir");
         this.seperator = java.io.File.separator;
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
             FileHelper.copyFileToPath(sampleFiles, this.tempDir + this.seperator);
         } else {
             assertTrue("Sample files could not be loaded for test", false);
         }
 
         sampleDir = this.tempDir + this.seperator
                 + "Sample_Files" + this.seperator
                 + "Shapefiles" + this.seperator;
 
         testFilePath = sampleDir + testFile;
     }
 
     @After
     public void tearDown() {
         File delDir = new File(this.tempDir);
         try {
             FileHelper.deleteDirRecursively(delDir);
         } catch (IOException ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, "Failed to delete: " + delDir.getPath() + "  -- Remember to clean project or remove this file/dir.", ex);
         }
     }
 
     @Test
     public void base64EncodeWithFile() throws IOException {
 		System.out.println("Test: " + "base64EncodeWithFile");
         File file = new File(this.sampleDir + this.secondTestFile + ".prj");
         byte[] result = FileHelper.base64Encode(file);
         assertThat(result, is(not(nullValue())));
     }
 
     @Test
     public void base64EncodeWithByteArray() throws IOException {
 		System.out.println("Test: " + "base64EncodeWithByteArray");
         byte[] result = FileHelper.base64Encode(new byte[]{'a', 'b', 'c', 'd', 'e', 'f', 'g'});
         assertThat(result, is(not(nullValue())));
         assertThat(result.length, is(equalTo(14)));
     }
 
     @Test
     public void base64EncodeWithNullByteArray() throws IOException {
 		System.out.println("Test: " + "base64EncodeWithNullByteArray");
         byte[] input = null;
         byte[] result = FileHelper.base64Encode(input);
         assertThat(result, is(not(nullValue())));
         assertThat(result.length, is(equalTo(0)));
     }
 
     @Test
     public void testGetCanonicalPathname() {
 		System.out.println("Test: " + "testGetCanonicalPathname");
         File file = new File(this.sampleDir + this.secondTestFile + ".prj");
         assertTrue(file.exists());
 
         String result = null;
         try {
             result = file.getParent();
         } catch (Exception ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         assertNotNull(result);
     }
 
     @Test
     public void testRenameFile() {
 		System.out.println("Test: " + "testRenameFile");
         try {
             File file = File.createTempFile("delete", "me");
             assertTrue(file.exists());
 
             boolean result = FileHelper.renameFile(file, "new.file");
             assertTrue(result);
             File newFile = new File(file.getParent() + File.separator + "new.file");
             assertTrue(newFile.exists());
             assertFalse(file.exists());
             newFile.delete();
         } catch (IOException ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Test
     public void testRenameFilesWithoutBeingAbleToWriteDestinationFile() {
 			System.out.println("Test: " + "testRenameFilesWithoutBeingAbleToWriteDestinationFile");
             try {
             File file = File.createTempFile("delete", "me");
             assertTrue(file.exists());
 
             boolean result = FileHelper.renameFile(file, "test2");
             assertTrue(result);
         } catch (IOException ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Test
     public void testRenameFileWithSameFiles() {
 		System.out.println("Test: " + "testRenameFileWithSameFiles");
         try {
             File file = File.createTempFile("delete", "me");
             assertTrue(file.exists());
 
             boolean result = FileHelper.renameFile(file, "delete.me");
             assertTrue(result);
         } catch (IOException ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Test
     /**
      * If this fails, try cleaning/building the project and re-run
      */
     public void testWipeOldFilesWithNoOldFiles() {
 		System.out.println("Test: " + "testWipeOldFilesWithNoOldFiles");
         Collection<File> result = FileHelper.wipeOldFiles(new File(this.tempDir), 3600000l, true);
        assertFalse(result.isEmpty());
     }
 
     @Test
 	@Ignore
     public void testWipeOldFilesWithAnAlreadyOpenFile() throws FileNotFoundException, IOException {
         Collection<File> result = new ArrayList<File>();
         File openFile = new File(FileHelperTest.sampleDir);
         InputStream is = null;
         try {
             is = new FileInputStream(openFile);
             result = FileHelper.wipeOldFiles(openFile, 3600000l, false);
         } finally {
             if (is != null) {
                 is.close();
             }
         }
         assertTrue(result.isEmpty());
     }
 
     /**
      * If this fails, try cleaning/building the project and re-run
      */
     @Test
     public void testWipeOldFilesWithFakeDirectory() {
 		System.out.println("Test: " + "testWipeOldFilesWithFakeDirectory");
         Collection<File> result = FileHelper.wipeOldFiles(new File("/not/real"), 3600000l, true);
         assertTrue(result.isEmpty());
     }
 
     /**
      * If this fails, try cleaning/building the project and re-run
      */
     @Test
     public void testWipeOldFilesWithNullArgument() {
 		System.out.println("Test: " + "testWipeOldFilesWithNullArgument");
         Collection<File> result = new ArrayList<File>();
         result = FileHelper.wipeOldFiles(null, 3600000l, true);
         assertTrue(result.isEmpty());
     }
 
     @Test
     public void testWipeOldFilesWithOldFiles() {
 		System.out.println("Test: " + "testWipeOldFilesWithOldFiles");
         Collection<File> result = new ArrayList<File>();
         result = FileHelper.wipeOldFiles(new File(this.tempDir), 1l, true);
         assertTrue(!result.isEmpty());
     }
 
     @Test
     public void testWipeOldFilesWithOldFilesWhileLockingDirectory() throws FileNotFoundException, IOException {
 		System.out.println("Test: " + "testWipeOldFilesWithOldFilesWhileLockingDirectory");
         Collection<File> result = new ArrayList<File>();
 
         File directory = new File(this.tempDir);
         String singleFile = this.tempDir
                 + System.getProperty("file.separator")
                 + "Sample_Files"
                 + System.getProperty("file.separator")
                 + "Shapefiles"
                 + System.getProperty("file.separator")
                 + "demo_HUCs.dbf";
 
         // Lock a file
         File file1 = new File(singleFile);
         file1.setReadOnly();
         result = FileHelper.wipeOldFiles(directory, 1l, true);
 
         // Test that the locked file doesnt show up in the deleted files list
         boolean containsFail = true;
         for (File file : result) {
             if (file.getPath().equals(singleFile)) {
                 containsFail = false;
             }
         }
         assertTrue(containsFail);
 
         //Unlock the files
         file1.setWritable(false);
 
         // really delete the directory
         FileHelper.wipeOldFiles(directory, 1l, true);
     }
 
     @Test
     public void testCreateDirUsingStringObject() {
 		System.out.println("Test: " + "testCreateDirUsingStringObject");
         boolean result = false;
         String testDir = System.getProperty("java.io.tmpdir")
                 + java.io.File.separator
                 + Long.toString((new Date()).getTime()) + 1;
         result = FileHelper.createDir(testDir);
         assertTrue(result);
         (new File(testDir)).delete();
     }
 
     @Test
     public void testCreateDirUsingFileObject() {
 		System.out.println("Test: " + "testCreateDirUsingFileObject");
         boolean result = false;
         String testDir = System.getProperty("java.io.tmpdir")
                 + java.io.File.separator
                 + Long.toString((new Date()).getTime()) + 1;
         result = FileHelper.createDir(new File(testDir));
         assertTrue(result);
         (new File(testDir)).delete();
     }
 
     @Test
     public void testDoesDirectoryOrFileExist() {
 		System.out.println("Test: " + "testDoesDirectoryOrFileExist");
         boolean result = false;
         String fileToCheckFor = testFilePath + ".shx";
 
         String directoryToCheckFor = sampleDir;
 
         result = FileHelper.doesDirectoryOrFileExist(fileToCheckFor);
         assertTrue(result);
         result = FileHelper.doesDirectoryOrFileExist(directoryToCheckFor);
         assertTrue(result);
         result = FileHelper.doesDirectoryOrFileExist("does/not/exist");
         assertFalse(result);
     }
 
     @Test
     public void testCopyFileToPathWithoutDeletingOriginal() {
 		System.out.println("Test: " + "testCopyFileToPathWithoutDeletingOriginal");
         File fileToCopy = new File(testFilePath + ".shx");
 
         String fileToCopyTo = testFilePath + ".COPY";
 
         boolean result = false;
         try {
             result = FileHelper.copyFileToPath(fileToCopy, fileToCopyTo);
         } catch (IOException e) {
             fail(e.getMessage());
         }
         assertTrue(result);
 
         try {
             result = FileHelper.copyFileToPath(new File("doesnt/exist"), "doesnt/exist");
         } catch (IOException e) {
             assertNotNull(e);
             result = false;
         }
         assertFalse(result);
     }
 
     @Test
     public void testCopyFileToPathWithDeletingOriginal() {
 		System.out.println("Test: " + "testCopyFileToPathWithDeletingOriginal");
         File fileToCopy = new File(testFilePath + ".shx");
 
         String fileToCopyTo = testFilePath + ".COPY";
 
         boolean result = false;
         try {
             result = FileHelper.copyFileToPath(fileToCopy, fileToCopyTo, true);
         } catch (IOException e) {
             fail(e.getMessage());
         }
         assertTrue(result);
 
         try {
             result = FileHelper.copyFileToPath(new File("doesnt/exist"), "doesnt/exist");
         } catch (IOException e) {
             assertNotNull(e);
             result = false;
         }
         assertFalse(result);
     }
 
     @Test
     public void testDeleteFileQuietly() {
 		System.out.println("Test: " + "testDeleteFileQuietly");
         String fileToLoad = testFilePath + ".shx";
 
         boolean result = FileHelper.deleteFileQuietly("File/That/Doesnt/Exist");
         assertFalse(result);
         result = FileHelper.deleteFileQuietly(fileToLoad);
         assertTrue(result);
     }
 
     @Test
     public void testDeleteFile() {
 		System.out.println("Test: " + "testDeleteFile");
         String fileToLoad = testFilePath + ".shx";
 
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
 		System.out.println("Test: " + "testDeleteDirRecursively");
         File lockedFile = new File(testFilePath + ".shx");
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
         } catch (IOException e) {
             fail(e.getMessage());
         }
         assertFalse(result);
         lockedFile.setWritable(true);
         FileHelper.deleteFileQuietly(lockedFile);
     }
 
     @Test
     public void testDeleteDirRecursivelyUsingString() {
 		System.out.println("Test: " + "testDeleteDirRecursivelyUsingString");
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
 		System.out.println("Test: " + "testFileHelper");
         FileHelper result = new FileHelper();
         assertNotNull(result);
     }
 
     @Test
     public void testFindFile() {
 		System.out.println("Test: " + "testFindFile");
         String fileToLoad = testFile + ".shx";
         String rootDir = this.tempDir + this.seperator;
         File result = FileHelper.findFile(fileToLoad, rootDir);
         assertNotNull("FineFile did not find the file " + fileToLoad + " within " + rootDir, result);
         assertEquals("File loaded does not have the same name as the file suggested", fileToLoad, result.getName());
         result = FileHelper.findFile("should.not.work", rootDir);
         assertNull(result);
     }
 
     @Test
     public void testFindFileWithNull() {
 		System.out.println("Test: " + "testFindFileWithNull");
         File result = FileHelper.findFile(null, null);
         assertNull(result);
     }
 
 
     @Test
     public void testFindFileWithEmptyString() {
 		System.out.println("Test: " + "testFindFileWithEmptyString");
         File result = FileHelper.findFile("", "");
         assertNull(result);
     }
 
     @Test
     public void testGetFileList() {
 		System.out.println("Test: " + "testGetFileList");
         String dirToList = this.tempDir + this.seperator;
         List<String> result = null;
         result = FileHelper.getFileList(null, true);
         assertNull(result);
         result = FileHelper.getFileList(dirToList, true);
         assertNotNull("File listing came back null", result);
         assertFalse("There were no files listed", result.isEmpty());
         String fakeDirToList = this.tempDir + this.seperator + "9387509352" + this.seperator;
         try {
             result = FileHelper.getFileList(fakeDirToList, true);
         } catch (IllegalArgumentException e) {
             assertNotNull(e);
         }
 
     }
 
     @Test
     public void testGetSystemTemp() {
 		System.out.println("Test: " + "testGetSystemTemp");
         String result = FileHelper.getSystemTemp();
         assertNotNull(result);
         assertFalse("".equals(result));
         log.debug("System temp path: " + result);
     }
 
     @Test
     public void testGetFileCollection() {
 		System.out.println("Test: " + "testGetFileCollection");
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
     public void testCreateDirWithExistingDir() {
 		System.out.println("Test: " + "testCreateDirWithExistingDir");
         String existingDirectory = this.seperator + this.tempDir;
         boolean result = FileHelper.createDir(existingDirectory);
         assertTrue(result);
     }
     @Test
     public void testDeleteFileWithEmptyFilePath() {
 		System.out.println("Test: " + "testDeleteFileWithEmptyFilePath");
         boolean result = FileHelper.deleteFile("");
         assertFalse(result);
     }
 
     @Test
     public void testDeleteFileWithNullArgument() {
 		System.out.println("Test: " + "testDeleteFileWithNullArgument");
         File nullFile = null;
         boolean result = FileHelper.deleteFile(nullFile);
         assertFalse(result);
     }
 
     @Test
     public void testUnzipFile() {
 		System.out.println("Test: " + "testUnzipFile");
         File zipFile = new File(FileHelperTest.sampleDir + "test_zip.zip");
         boolean result = false;
         try {
             result = FileHelper.unzipFile(this.tempDir, zipFile);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, null, ex);
             fail(ex.getMessage());
         } catch (IOException ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, null, ex);
             fail(ex.getMessage());
         }
         assertTrue(result);
     }
 
     @Test
     public void testCreateUserDirectory() {
 		System.out.println("Test: " + "testCreateUserDirectory");
         String createdDir = FileHelper.createUserDirectory(this.tempDir + this.seperator);
         assertFalse(createdDir.isEmpty());
     }
 
     @Test
     @Ignore
     public void testCreateUserDirectoryWithInvalidDirectoryName() {
 		System.out.println("Test: " + "testCreateUserDirectoryWithInvalidDirectoryName");
         String dirToCreate = this.seperator + "nonexistent";
         String createdDir = FileHelper.createUserDirectory(dirToCreate);
         assertTrue(createdDir.equals(""));
     }
 
     @Test
     public void testUpdateTimeStampWithNullPath() {
 		System.out.println("Test: " + "testUpdateTimeStampWithNullPath");
         boolean result = true;
         try {
             result = FileHelper.updateTimestamp(null, true);
         } catch (IOException ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         assertFalse(result);
     }
 
     @Test
     public void testUpdateTimeStampWithEmptyStringPath() {
 		System.out.println("Test: " + "testUpdateTimeStampWithEmptyStringPath");
         boolean result = true;
         try {
             result = FileHelper.updateTimestamp("", true);
         } catch (IOException ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         assertFalse(result);
     }
 
     @Test
     public void testUpdateTimeStampOnSingleFile() {
 		System.out.println("Test: " + "testUpdateTimeStampOnSingleFile");
         File file1 = new File(FileHelperTest.testFilePath + ".dbf");
         long file1Date = file1.lastModified();
 
         boolean result = true;
         try {
             result = FileHelper.updateTimestamp(FileHelperTest.testFilePath + ".dbf", false);
         } catch (IOException ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         assertTrue(result);
         File file2 = new File(FileHelperTest.testFilePath + ".dbf");
         long file2Date = file2.lastModified();
 
         assertTrue(file2Date > file1Date);
     }
 
     @Test
     public void testUpdateTimeStampOnSingleNonexistentFile() {
 		System.out.println("Test: " + "testUpdateTimeStampOnSingleNonexistentFile");
         File file1 = new File(FileHelperTest.testFilePath);
         long file1Date = file1.lastModified();
 
         boolean result = true;
         try {
             result = FileHelper.updateTimestamp(FileHelperTest.testFilePath, false);
         } catch (IOException ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         assertFalse(result);
     }
 
     @Test
     public void testUpdateTimeStampOnSingleDirectoryRecursive() {
 		System.out.println("Test: " + "testUpdateTimeStampOnSingleDirectoryRecursive");
         File file1 = new File(FileHelperTest.sampleDir);
         long file1Date = file1.lastModified();
 
         boolean result = true;
         try {
             result = FileHelper.updateTimestamp(FileHelperTest.sampleDir, true);
         } catch (IOException ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         assertTrue(result);
     }
 
     @Test
     public void testGetFilesOlderThanWithNullFilePath() {
 		System.out.println("Test: " + "testGetFilesOlderThanWithNullFilePath");
         Collection<File> result = FileHelper.getFilesOlderThan(null, Long.MIN_VALUE, Boolean.TRUE);
         assertTrue(result.isEmpty());
     }
 
     @Test
     public void testGetFilesOlderThanWithNonExistantFilePath() {
 		System.out.println("Test: " + "testGetFilesOlderThanWithNonExistantFilePath");
         Collection<File> result = FileHelper.getFilesOlderThan(new File("derp"), Long.MIN_VALUE, Boolean.TRUE);
         assertTrue(result.isEmpty());
     }
 
     @Test
     public void testGetFilesOlderThanWithRealFilePathAndRecursiveWithMaxAgeValue() {
 		System.out.println("Test: " + "testGetFilesOlderThanWithRealFilePathAndRecursiveWithMaxAgeValue");
         // We should find no files older than the maximum long value
         Collection<File> result = FileHelper.getFilesOlderThan(new File(this.tempDir), Long.MAX_VALUE, Boolean.TRUE);
         assertTrue(result.isEmpty());
     }
 
     @Test
     public void testGetFilesOlderThanWithRealFilePathAndNonRecursiveWithMaxAgeValue() {
 		System.out.println("Test: " + "testGetFilesOlderThanWithRealFilePathAndNonRecursiveWithMaxAgeValue");
         // We should find no files older than the maximum long value
         Collection<File> result = FileHelper.getFilesOlderThan(new File(this.tempDir), Long.MAX_VALUE, Boolean.FALSE);
         assertTrue(result.isEmpty());
     }
 
     @Test
     public void testFileToByteArray() {
 		System.out.println("Test: " + "testFileToByteArray");
         File input = null;
         byte[] result = null;
 
         // First we attain a handler to a file
         input = new File(FileHelperTest.sampleDir + "test_zip.zip");
         try {
             // Try to get a byte array from it
             result = FileHelper.getByteArrayFromFile(input);
         } catch (IOException ex) {
             fail(ex.getMessage());
         }
 
         assertThat(result.length, is(not(0)));
         assertThat(result.length, is(equalTo((int) input.length())));
     }
 
     @Test
     public void testFileToByteArrayWithNullInput() {
 		System.out.println("Test: " + "testFileToByteArrayWithNullInput");
         File input = null;
         byte[] result = null;
 
         // First we attain a handler to a file
         try {
             // Try to get a byte array from it
             result = FileHelper.getByteArrayFromFile(input);
         } catch (IOException ex) {
             fail(ex.getMessage());
         }
 
         assertThat(result.length, is(not(nullValue())));
         assertThat(result.length, is(equalTo(0)));
     }
 
     @Test
     public void testByteArrayToBase64() {
 		System.out.println("Test: " + "testByteArrayToBase64");
         File input = null;
         byte[] inputByteArray = null;
         byte[] result = null;
 
         input = new File(FileHelperTest.sampleDir + "test_zip.zip");
 
         try {
             inputByteArray = FileHelper.getByteArrayFromFile(input);
         } catch (IOException ex) {
             fail(ex.getMessage());
         }
 
         result = FileHelper.base64Encode(inputByteArray);
         assertThat(result.length, is(not(0)));
         assertThat(result.length, is(not((int) input.length())));
         assertThat(result.length, is(not(inputByteArray.length)));
     }
 }
