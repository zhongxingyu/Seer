 package tagEditor;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import static java.nio.file.StandardCopyOption.*;
 
 import javax.swing.Box.Filler;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.xml.bind.JAXBException;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.junit.Test;
 import org.xml.sax.SAXException;
 
 public class CacheTests {
 
 	@Test
 	public void TestXML (){
 		
 		Control myControl = new Control();
 		DefaultMutableTreeNode newRoot = null;		
 		try {
 			myControl.fillTree("tests");
 		} 
 		catch (JAXBException | SAXException | IOException | ParserConfigurationException e) {
 			fail("Exception while loading directory");
 		}
 		DefaultMutableTreeNode myRoot= myControl.getRoot();
 		try {
 			XMLControl.writeCache(myRoot, "tests/cache.xml");
 		} catch (ParserConfigurationException | TransformerException e) {
 			fail("Exception while writing xml ");
 		}
 		try {
 			newRoot = XMLControl.readCache(new File("tests/cache.xml"));
 		} catch (ParserConfigurationException | SAXException | IOException e) {
 			fail("Exception while writing reading  xml ");
 		}
 		assertEquals(myRoot.isRoot(), newRoot.isRoot());
 		assertEquals(myRoot.getChildCount(), newRoot.getChildCount());
 		int count = myRoot.getChildCount();
 		for (int i = 0; i < count; i++) {
 			DefaultMutableTreeNode originalChild = (DefaultMutableTreeNode) myRoot.getChildAt(i);
 			DefaultMutableTreeNode newChild = (DefaultMutableTreeNode) newRoot.getChildAt(i);
 			
 			MP3File oldFile = (MP3File) originalChild.getUserObject();
 			MP3File newFile = (MP3File) newChild.getUserObject();
 			
 			assertEquals(oldFile.album, newFile.album);
 			assertEquals(oldFile.artist, newFile.artist);
 			assertEquals(oldFile.path, newFile.path);
 			assertEquals(oldFile.song, newFile.song);
 			assertEquals(oldFile.year, newFile.year);
 		}
 	}
 	
 	@Test
 	public void TestDeleteTreeElement(){
 	
 		Control myControl = new Control();
 		DefaultMutableTreeNode newRoot = null;
 		try {
 			myControl.fillTree("tests");
 		} 
 		catch (JAXBException | SAXException | IOException | ParserConfigurationException e) {
 			fail("Exception while loading directory");
 		}
 		DefaultMutableTreeNode myRoot= myControl.getRoot();
 		assertEquals(2, myRoot.getChildCount());
 		myRoot.remove(1);
 		assertEquals(1, myRoot.getChildCount());
 		try {
 			XMLControl.writeCache(myRoot, "tests/cache.xml");
 		} catch (ParserConfigurationException | TransformerException e) {
 			fail("Exception while writing xml ");
 		}
 		try {
 			newRoot = XMLControl.readCache(new File("tests/cache.xml"));
 		} catch (ParserConfigurationException | SAXException | IOException e) {	
 			fail("Exception while writing reading  xml ");
 		}
 		assertEquals(1, newRoot.getChildCount());
 		
 		try {
 			XMLControl.writeCache(newRoot, "tests/cache.xml");
 		} catch (ParserConfigurationException | TransformerException e) {
 			fail("Exception while writing xml ");
 		}
 	}
 	
 	
 	
 	@Test
 	public void TestAddFile(){
 	
 		Control myControl = new Control();
 		DefaultMutableTreeNode newRoot = null;
 		// we first make sure that we have a "good" cache
 		try {
 			myControl.fillTree("tests");
 		} 
 		catch (JAXBException | SAXException | IOException | ParserConfigurationException e) {
 			fail("Exception while loading directory");
 		}
 		DefaultMutableTreeNode myRoot= myControl.getRoot();
 		try {
 			XMLControl.writeCache(myRoot, "tests/cache.xml");
 		} catch (ParserConfigurationException | TransformerException e) {
 			fail("Exception while writing xml ");
 		}
 		// we load again and expect all the files to be read via cache
 		try {
 			myControl.fillTree("tests");
 		} 
 		catch (JAXBException | SAXException | IOException | ParserConfigurationException e) {
 			fail("Exception while loading directory");
 		}
 		myRoot= myControl.getRoot();
 		// check if all of them where loaded from cache
 		System.out.println(XMLControl.filesReadFromCache);
 		
 		assertEquals(XMLControl.filesReadFromCache, myRoot.getChildCount());
 		
 		try {
 			Files.copy(Paths.get("tests/01_What_i_hope Kopie.mp3"), Paths.get("tests/01_What_i_hope_testcase.mp3"), REPLACE_EXISTING);
 		} catch (IOException e) {
 			fail("Exception while copying directory");
 		}
 		// we load again and expect all the files to be read via cache
 		try {
 			myControl.fillTree("tests");
 		} 
 		catch (JAXBException | SAXException | IOException | ParserConfigurationException e) {
 			fail("Exception while loading directory");
 		}
 		myRoot= myControl.getRoot();
 		assertEquals(true,XMLControl.filesReadFromCache < myRoot.getChildCount());
 		assertEquals(2,XMLControl.filesReadFromCache);
 		assertEquals(3,myRoot.getChildCount());
 		// we clean up after us
 		try {
 			Files.delete(Paths.get("tests/01_What_i_hope_testcase.mp3"));
 		} catch (IOException e) {
 			fail("Exception while deleting file");
 		}
 	}
 	
 	
 	@Test
 	public void TestRemoveFile(){
 	
 		Control myControl = new Control();
 		DefaultMutableTreeNode newRoot = null;
 		// we first make sure that we have a "good" cache
 		try {
 			myControl.fillTree("tests");
 		} 
 		catch (JAXBException | SAXException | IOException | ParserConfigurationException e) {
 			fail("Exception while loading directory");
 		}
 		DefaultMutableTreeNode myRoot= myControl.getRoot();
 		assertEquals(XMLControl.filesReadFromCache, myRoot.getChildCount());
 		try {
 			Files.delete(Paths.get("tests/01_What_i_hope Kopie.mp3"));
 		} catch (IOException e) {
 			fail("Exception while deleting file");
 		}
 		try {
 			myControl.fillTree("tests");
 		} 
 		catch (JAXBException | SAXException | IOException | ParserConfigurationException e) {
 			fail("Exception while loading directory");
 		}
 		myRoot= myControl.getRoot();
 		assertEquals(XMLControl.filesReadFromCache, myRoot.getChildCount());
 		assertEquals(1, myRoot.getChildCount());
 		assertEquals(XMLControl.filesReadFromCache, 1);		
 		try {
 			Files.copy(Paths.get("tests/01_What_i_hope.mp3"), Paths.get("tests/01_What_i_hope Kopie.mp3"), REPLACE_EXISTING);
 		} catch (IOException e) {
 			fail("Exception while copying directory");
 		}
 		try {
 			myControl.fillTree("tests");
 		} 
 		catch (JAXBException | SAXException | IOException | ParserConfigurationException e) {
 			fail("Exception while loading directory");
 		}
 		myRoot= myControl.getRoot();
 		assertEquals(2,myRoot.getChildCount());	
 	}
 
 	@Test
 	public void TestModifyFile(){
 	
 		Control myControl = new Control();
 		MP3Saver saver = new MP3Saver();	
 		// we first make sure that we have a "good" cache
 		try {
 			myControl.fillTree("tests");
 		} 
 		catch (JAXBException | SAXException | IOException | ParserConfigurationException e) {
 			fail("Exception while loading directory");
 		}
 		DefaultMutableTreeNode myRoot= myControl.getRoot();
 		assertEquals(XMLControl.filesReadFromCache, myRoot.getChildCount());
 		DefaultMutableTreeNode child = (DefaultMutableTreeNode) myRoot.getChildAt(1);
 		MP3File myFile = (MP3File) child.getUserObject();
 		myFile.album ="test";
 		try {
 			saver.saveMP3(myFile);
 		} catch (IOException e) {
 			fail("exception While writing file");
 		}
 		//lets load the cache again
 		try {
 			myControl.fillTree("tests");
 		} 
 		catch (JAXBException | SAXException | IOException | ParserConfigurationException e) {
 			fail("Exception while loading directory");
 		}
 		myRoot= myControl.getRoot();
 		child = (DefaultMutableTreeNode) myRoot.getChildAt(1);
 		myFile = (MP3File) child.getUserObject();
 		// since we have set the album previously it should be that value now
 		assertEquals("test", myFile.album);
 		//lets clean up
 		myFile.album ="bansen";
 		try {
 			saver.saveMP3(myFile);
 		} catch (IOException e) {
 			fail("exception While writing file");
 		}
 		try {
 			XMLControl.writeCache(myRoot, "tests/cache.xml");
 		} catch (ParserConfigurationException | TransformerException e) {
 			fail("Exception while writing xml ");
 		}	
 	}
 	
 }
 
