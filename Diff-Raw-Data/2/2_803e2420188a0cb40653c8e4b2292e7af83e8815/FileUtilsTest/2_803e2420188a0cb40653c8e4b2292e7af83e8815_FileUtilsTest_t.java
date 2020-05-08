 package com.google.code.maven_replacer_plugin.file;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.util.UUID;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 
 public class FileUtilsTest {
 	@Rule
 	public TemporaryFolder folder = new TemporaryFolder();
 
 	private FileUtils fileUtils;
 
 	@Before
 	public void setUp() {
 		fileUtils = new FileUtils();
 	}
 
 	@Test
 	public void shouldDetermineIfFileExists() throws Exception {
 		File file = folder.newFile("tempfile");
 		assertTrue(fileUtils.fileNotExists("non existant"));
 		assertFalse(fileUtils.fileNotExists(file.getAbsolutePath()));
 	}
 
 	@Test
 	public void shouldEnsureFileFolderExists() throws Exception {
 		String tempFile = System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID() + "/tempfile";
 		fileUtils.ensureFolderStructureExists(tempFile);
 		new File(tempFile).createNewFile();
 		assertTrue(new File(tempFile).exists());
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void shouldThrowIllegalArgumentExceptionIfFileIsDirectory() throws Exception {
		String tempFile = System.getProperty("java.io.tmpdir");
 		fileUtils.ensureFolderStructureExists(tempFile);
 		new File(tempFile).createNewFile();
 		assertTrue(new File(tempFile).exists());
 	}
 
 	@Test
 	public void shouldReadFile() throws Exception {
 		File file = folder.newFile("tempfile");
 		FileWriter writer = new FileWriter(file);
 		writer.write("test");
 		writer.close();
 
 		String data = fileUtils.readFile(file.getAbsolutePath());
 		assertEquals("test", data);
 	}
 }
