 package org.analyzer.factories;
 
 import static org.mockito.Mockito.*;
 import static org.testng.Assert.*;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.io.Reader;
 
 import org.analyzer.Source;
 import org.analyzer.exceptions.SourceException;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 @Test
 public class FileSourceTest extends SourceTestTemplate {
 	private File sourceFile;
 
 	@BeforeMethod(alwaysRun = true)
 	public void prepareFile() throws FileNotFoundException {
 		try {
			sourceFile = File.createTempFile("SourceFactoryTestFile", ".html");
 		} catch (IOException ex) {
 			sourceFile = new File("SourceFactoryTestFile.htlm");
 		}
 
 		sourceFile.deleteOnExit();
 
 		PrintWriter pw = new PrintWriter(sourceFile);
 		pw.print(sourceText);
 		pw.flush();
 		pw.close();
 	}
 
 	@Test(groups = "constructor", dataProvider = "booleans", expectedExceptions = NullPointerException.class)
 	public void testNewFileSource1(Boolean cache) throws Exception {
 		if (cache == null) {
 			SourceFactory.newFileSource((File)null);
 		} else {
 			SourceFactory.newFileSource((File)null, cache);
 		}
 	}
 
 	@Test(groups = "constructor", dataProvider = "booleans", expectedExceptions = SourceException.class)
 	public void testNewFileSource2(Boolean cache) throws Exception {
 		if (!sourceFile.delete()) {
 			fail("can't delete file!");
 		}
 
 		if (cache == null) {
 			SourceFactory.newFileSource(sourceFile);
 		} else {
 			SourceFactory.newFileSource(sourceFile, cache);
 		}
 	}
 
 	@Test(groups = "constructor", dataProvider = "booleans", expectedExceptions = SourceException.class)
 	public void testNewFileSource3(Boolean cache) throws Exception {
 		if (!sourceFile.setReadable(false)) {
 			fail("can't set file as unreadable!");
 		}
 
 		if (cache == null) {
 			SourceFactory.newFileSource(sourceFile);
 		} else {
 			SourceFactory.newFileSource(sourceFile, cache);
 		}
 	}
 
 	@Test(groups = "constructor", dataProvider = "booleans", expectedExceptions = SourceException.class)
 	public void testNewFileSource4(Boolean cache) throws Exception {
 		sourceFile = mock(File.class);
 		when(sourceFile.canRead()).thenReturn(true);
 		when(sourceFile.exists()).thenReturn(false);
 
 		if (cache == null) {
 			SourceFactory.newFileSource(sourceFile);
 		} else {
 			SourceFactory.newFileSource(sourceFile, cache);
 		}
 	}
 
 	@Test(groups = "constructor", dataProvider = "booleans")
 	public void testNewFileSource5(Boolean cache) throws Exception {
 		if (cache == null) {
 			SourceFactory.newFileSource(sourceFile);
 		} else {
 			SourceFactory.newFileSource(sourceFile, cache);
 		}
 	}
 
 	@Test(groups = "constructor", dataProvider = "booleans", expectedExceptions = NullPointerException.class)
 	public void testNewFileSource6(Boolean cache) throws Exception {
 		if (cache == null) {
 			SourceFactory.newFileSource((String)null);
 		} else {
 			SourceFactory.newFileSource((String)null, cache);
 		}
 	}
 
 	@Test(groups = "constructor", dataProvider = "booleans", expectedExceptions = SourceException.class)
 	public void testNewFileSource7(Boolean cache) throws Exception {
 		if (!sourceFile.delete()) {
 			fail("can't delete file!");
 		}
 
 		if (cache == null) {
 			SourceFactory.newFileSource(sourceFile.getPath());
 		} else {
 			SourceFactory.newFileSource(sourceFile.getPath(), cache);
 		}
 	}
 
 	@Test(groups = "constructor", dataProvider = "booleans", expectedExceptions = SourceException.class)
 	public void testNewFileSource8(Boolean cache) throws Exception {
 		if (!sourceFile.setReadable(false)) {
 			fail("can't set file as unreadable!");
 		}
 
 		if (cache == null) {
 			SourceFactory.newFileSource(sourceFile.getPath());
 		} else {
 			SourceFactory.newFileSource(sourceFile.getPath(), cache);
 		}
 	}
 
 	@Test(groups = "constructor", dataProvider = "booleans")
 	public void testNewFileSource9(Boolean cache) throws Exception {
 		if (cache == null) {
 			SourceFactory.newFileSource(sourceFile.getPath());
 		} else {
 			SourceFactory.newFileSource(sourceFile.getPath(), cache);
 		}
 	}
 
 	@Test(dependsOnGroups = "constructor")
 	public void testGetDescription() throws Exception {
 		Source s = SourceFactory.newFileSource(sourceFile);
 
		assertEquals(s.getDescription(), sourceFile.getAbsolutePath());
 	}
 
 	@Test(dependsOnGroups = "constructor", expectedExceptions = RuntimeException.class)
 	public void testGetStream1() throws Exception {
 		Source s = SourceFactory.newFileSource(sourceFile, false);
 
 		if (!sourceFile.delete()) {
 			fail("can't delete file!");
 		}
 
 		s.getStream();
 	}
 
 	@Test(dependsOnGroups = "constructor")
 	public void testGetStream2() throws Exception {
 		Source s = SourceFactory.newFileSource(sourceFile, false);
 
 		InputStream is = s.getStream();
 
 		assertNotNull(is);
 		String content = readStream(is);
 		assertNotNull(content);
 		assertEquals(content, sourceText);
 	}
 
 	@Test(dependsOnGroups = "constructor")
 	public void testGetStream3() throws Exception {
 		Source s = SourceFactory.newFileSource(sourceFile, true);
 		if (!sourceFile.delete()) {
 			fail("can't delete file!");
 		}
 
 		InputStream is = s.getStream();
 
 		assertNotNull(is);
 		String content = readStream(is);
 		assertNotNull(content);
 		assertEquals(content, sourceText);
 	}
 
 	@Test(dependsOnGroups = "constructor", expectedExceptions = RuntimeException.class)
 	public void testGetReader1() throws Exception {
 		Source s = SourceFactory.newFileSource(sourceFile, false);
 
 		if (!sourceFile.delete()) {
 			fail("can't delete file!");
 		}
 
 		s.getReader();
 	}
 
 	@Test(dependsOnGroups = "constructor")
 	public void testGetReader2() throws Exception {
 		Source s = SourceFactory.newFileSource(sourceFile, false);
 
 		Reader r = s.getReader();
 
 		assertNotNull(r);
 		String content = readReader(r);
 		assertNotNull(content);
 		assertEquals(content, sourceText);
 	}
 
 	@Test(dependsOnGroups = "constructor")
 	public void testGetReader3() throws Exception {
 		Source s = SourceFactory.newFileSource(sourceFile, true);
 		if (!sourceFile.delete()) {
 			fail("can't delete file!");
 		}
 
 		Reader r = s.getReader();
 
 		assertNotNull(r);
 		String content = readReader(r);
 		assertNotNull(content);
 		assertEquals(content, sourceText);
 	}
 
 	@Test(dependsOnGroups = "constructor", expectedExceptions = RuntimeException.class)
 	public void testGetText1() throws Exception {
 		Source s = SourceFactory.newFileSource(sourceFile, false);
 
 		if (!sourceFile.delete()) {
 			fail("can't delete file!");
 		}
 
 		s.getText();
 	}
 
 	@Test(dependsOnGroups = "constructor")
 	public void testGetText2() throws Exception {
 		Source s = SourceFactory.newFileSource(sourceFile, false);
 
 		String content = s.getText();
 		assertNotNull(content);
 		assertEquals(content, sourceText);
 	}
 
 	@Test(dependsOnGroups = "constructor")
 	public void testGetText3() throws Exception {
 		Source s = SourceFactory.newFileSource(sourceFile, true);
 		if (!sourceFile.delete()) {
 			fail("can't delete file!");
 		}
 
 		String content = s.getText();
 		assertNotNull(content);
 		assertEquals(content, sourceText);
 	}
 }
