 package grisu.backend.model.fs;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import grisu.backend.AllTests;
 import grisu.control.exceptions.RemoteFileSystemException;
 import grisu.frontend.control.clientexceptions.FileTransactionException;
 import grisu.model.FileManager;
 import grisu.model.MountPoint;
 import grisu.model.dto.DtoStringList;
 import grisu.model.dto.GridFile;
 import grisu.utils.FileHelpers;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.UUID;
 
 import javax.activation.DataHandler;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class FileSystemManagerTest {
 
 	public static final String TEST_TARGET_DIR = "unit_tests";
 
 	public static final File TEST_DIR_INPUT = new File(
 			System.getProperty("java.io.tmpdir"), "grisu-tests-input");
 	public static final File TEST_DIR_OUTPUT = new File(
 			System.getProperty("java.io.tmpdir"), "grisu-tests-output");
 
 	public static final String TEST_FILE_0_NAME = "test0.txt";
 
 	public static final File INPUT_TEST_FILE_0 = new File(TEST_DIR_INPUT,
 			TEST_FILE_0_NAME);
 
 	public static final File OUTPUT_TEST_FILE_0 = new File(TEST_DIR_OUTPUT,
 			TEST_FILE_0_NAME);
 
 
 
 	private static MountPoint mp = null;
 	private static MountPoint mp2 = null;
 
 	private static String targetDirUrl = null;
 	private static String targetFileUrl = null;
 
 	private static String targetDirUrl2 = null;
 	private static String targetFileUrl2 = null;
 
 	public static void deleteTestFiles() throws RemoteFileSystemException {
 	}
 
 	public static void downloadAndAssert(String url) {
 
 		try {
 			System.out.println("Downloading and asserting file: " + url);
 			AllTests.getFileManager().downloadUrl(url, TEST_DIR_OUTPUT, true);
 
 			File file = new File(TEST_DIR_OUTPUT, FileManager.getFilename(url));
 			System.out.println("Asserting whether "+INPUT_TEST_FILE_0+" equals "+file);
 			assertTrue(FileUtils.contentEquals(INPUT_TEST_FILE_0, file));
 
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 
 		System.out.println("Copying input files...");
 		if (!TEST_DIR_INPUT.exists()) {
 			TEST_DIR_INPUT.mkdirs();
 		}
 
 		InputStream in = FileSystemManagerTest.class.getResourceAsStream("/"
 				+ TEST_FILE_0_NAME);
 		IOUtils.copy(in,
 				new FileOutputStream(
 						FileSystemManagerTest.INPUT_TEST_FILE_0));
 		in.close();
 
 		// System.out.println("Creating proxy...");
 		// String dir = System.getProperty("user.home") + File.separator
 		// + ".globus" + File.separator;
 		// LocalProxy.gridProxyInit(dir + "testcert.pem", dir + "testkey.pem",
 		// "".toCharArray(),
 		// 12);
 
 
 
 		MountPoint[] mps = AllTests.getUserEnvironmentManager()
 				.getMountPoints();
 		int i = 0;
 		do {
 			mp = mps[i];
 			i = i + 1;
 		} while (mp.getRootUrl().contains("grid-vs")
 				|| mp.getRootUrl().contains("acsrc")
 				|| mp.getRootUrl().contains("sbs")
 				|| mp.getRootUrl().contains("mech"));
 
 		do {
 			mp2 = mps[i];
 			i = i + 1;
 		} while (mp2.getRootUrl().contains("grid-vs")
 				|| mp2.getRootUrl().contains("acsrc")
 				|| mp2.getRootUrl().contains("sbs")
 				|| mp2.getRootUrl().contains("mech"));
 
 		System.out.println("Mountpoint1: " + mp.getRootUrl());
 		System.out.println("Mountpoint2: " + mp2.getRootUrl());
 
 		targetDirUrl = mp.getRootUrl() + "/" + TEST_TARGET_DIR;
 		System.out.println("TargetDirUrl: " + targetDirUrl);
 		targetFileUrl = targetDirUrl + "/" + TEST_FILE_0_NAME;
 		System.out.println("TargetFileUrl: " + targetFileUrl);
 		targetDirUrl2 = mp2.getRootUrl() + "/" + TEST_TARGET_DIR + "_2";
 		System.out.println("TargetDirUrl2: " + targetDirUrl2);
 		targetFileUrl2 = targetDirUrl2 + "/" + TEST_FILE_0_NAME;
 		System.out.println("TargetFileUrl2: " + targetFileUrl2);
 
 		System.out.println("Uploading file: " + targetFileUrl);
 		uploadTo(targetFileUrl);
 
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 
 		System.out.println("Delete files: ");
 		System.out.println("\t" + targetFileUrl);
 		AllTests.getFileManager().deleteFile(targetFileUrl);
 		System.out.println("\t" + targetFileUrl2);
 		AllTests.getFileManager().deleteFile(targetFileUrl2);
 		System.out.println("\t" + OUTPUT_TEST_FILE_0);
 		FileUtils.deleteQuietly(OUTPUT_TEST_FILE_0);
 
 		System.out.println("Deleting input files...");
 		FileUtils.deleteDirectory(TEST_DIR_INPUT);
 
 	}
 
 	public static void uploadTo(String targetFileUrl) throws RemoteFileSystemException {
 		System.out.println("Uploading to: " + targetFileUrl);
 		DataHandler dh = FileManager.createDataHandler(INPUT_TEST_FILE_0);
 		AllTests.getServiceInterface().upload(dh, targetFileUrl);
 	}
 
 	@Before
 	public void setUp() throws Exception {
 
 		System.out.println("Deleting: " + OUTPUT_TEST_FILE_0);
 		FileUtils.deleteQuietly(OUTPUT_TEST_FILE_0);
 	}
 
 	@Test
 	public void testCopyFile() throws RemoteFileSystemException,
 	FileTransactionException {
 
 		String target = targetDirUrl2 + "_" + UUID.randomUUID().toString();
 		System.out.println("Copying: " + targetFileUrl + " -> " + target);
 		AllTests.getServiceInterface().cp(
 				DtoStringList.fromSingleString(targetFileUrl), target,
 				false, true);
 
 		downloadAndAssert(target + "/" + TEST_FILE_0_NAME);

 		System.out.println("Deleting file: " + target);
 		AllTests.getFileManager().deleteFile(target);
 
 	}
 
 	@Test
 	public void testCreateFolderAndDeleteFolder()
 			throws RemoteFileSystemException {
 
 		String folderUrl = targetDirUrl + "/" + "folder" + UUID.randomUUID();
 
 		System.out.println("Creating folder: " + folderUrl);
 		AllTests.getServiceInterface().mkdir(folderUrl);
 
 		assertTrue("Asserting whether " + folderUrl + " is folder",
 AllTests
 				.getFileManager().isFolder(folderUrl));
 
 		System.out.println("Deleting folder: " + folderUrl);
 		AllTests.getServiceInterface().deleteFile(folderUrl);
 
 		assertFalse("Asserting whether folder " + folderUrl + " still exists",
 				AllTests.getFileManager().fileExists(folderUrl));
 
 	}
 
 	@Test
 	public void testDownload() throws Exception {
 
 		System.out.println("Deleting " + OUTPUT_TEST_FILE_0);
 		FileUtils.deleteQuietly(OUTPUT_TEST_FILE_0);
 
 		System.out.println("Downloading " + targetFileUrl);
 		DataHandler dh = AllTests.getServiceInterface().download(targetFileUrl);
 
 		System.out.println("Saving to disk...");
 		FileHelpers.saveToDisk(dh.getDataSource(), OUTPUT_TEST_FILE_0);
 
 		assertTrue("Checking whether " + INPUT_TEST_FILE_0 + " equals "
 				+ OUTPUT_TEST_FILE_0,
 				FileUtils.contentEquals(INPUT_TEST_FILE_0,
 						OUTPUT_TEST_FILE_0));
 
 		System.out.println("Deleting " + OUTPUT_TEST_FILE_0);
 		FileUtils.deleteQuietly(OUTPUT_TEST_FILE_0);
 
 	}
 
 	@Test
 	public void testFileExists() throws RemoteFileSystemException {
 
 		assertTrue("Checking whether " + targetFileUrl + " exists",
 AllTests
 				.getServiceInterface().fileExists(targetFileUrl));
 
 	}
 
 	@Test
 	public void testGetFileSize() throws RemoteFileSystemException {
 
 		System.out.println("Checking filesize of " + targetFileUrl);
 		long size = AllTests.getServiceInterface().getFileSize(targetFileUrl);
 
 		assertTrue("Checking whether filesize > 0", size > 0);
 	}
 
 	@Test
 	public void testGetFolderListing() throws RemoteFileSystemException {
 
 		System.out.println("Listing " + targetDirUrl);
 		GridFile f = AllTests.getServiceInterface().ls(targetDirUrl, 1);
 
 		System.out.println("Checking whether " + TEST_FILE_0_NAME
 				+ " is amongst result");
 		for (GridFile c : f.getChildren()) {
 			if (c.getName().equals(TEST_FILE_0_NAME)) {
 				System.out.println(TEST_FILE_0_NAME + " found.");
 				return;
 			}
 		}
 		System.out.println(TEST_FILE_0_NAME + " not found.");
 		fail("Folder listing not containing test file.");
 	}
 
 	@Test
 	public void testIsFolder() throws RemoteFileSystemException {
 
 		assertTrue("Checking whether " + targetDirUrl + " is folder",
 AllTests
 				.getServiceInterface().isFolder(targetDirUrl));
 	}
 
 	@Test
 	public void testLastModified() throws RemoteFileSystemException {
 		System.out.println("Checking last modified time for " + targetFileUrl);
 		long l = AllTests.getServiceInterface().lastModified(targetFileUrl);
 		// TODO make that better
 		assertTrue("Checking whether last modified time > 0", l > 0);
 	}
 
 
 	@Test
 	public void testUploadAndDeleteFile() throws RemoteFileSystemException {
 
 		String target = targetDirUrl2 + "/" + TEST_FILE_0_NAME + "_"
 				+ UUID.randomUUID().toString();
 		System.out.println("Uploading " + target);
 		DataHandler dh = FileManager.createDataHandler(INPUT_TEST_FILE_0);
 		AllTests.getServiceInterface().upload(dh, target);
 
 		downloadAndAssert(target);
 
 		assertTrue("Checking whether " + target + " exists",
 AllTests
 				.getFileManager().fileExists(target));
 
 		System.out.println("Deleting " + target);
 		AllTests.getServiceInterface().deleteFile(target);
 
 		assertFalse("Checking whether " + target + " is deleted",
 AllTests
 				.getFileManager().fileExists(target));
 
 
 	}
 
 }
