 package uk.ac.ebi.fgpt.kama;
 
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.net.URISyntaxException;
 import java.util.List;
 
 import org.junit.Test;
 
 public class FileManipulatorsTest {
 
 	@Test 
 	public void testFileToArrayList(){
 		File file;
 		try {
 			file = new File(getClass().getClassLoader().getResource("accessiontest.txt").toURI());
 			List<String> arrayList = FileManipulators.fileToArrayList(file);
 			assertEquals("E-GEOD-24734", arrayList.get(0));
 		} catch (URISyntaxException e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	@Test
 	public void fileToArray(){
 		File file;
 		try {
 			file = new File(getClass().getClassLoader().getResource("accessiontest.txt").toURI());
 			String[] array = FileManipulators.fileToArray(file);
 			assertEquals("E-GEOD-24734", array[0]);
 		} catch (URISyntaxException e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	@Test
 	public void stringToFile(){
		String output = FileManipulators.stringToFile("target/write.txt", "Hello World");
 		File file;
 		file = new File(output);
 		String[] array = FileManipulators.fileToArray(file);
 		assertEquals("Hello World", array[0]);
 		
 	}
 	@Test
 	public void testMainClass(){
 		assertNotNull(new FileManipulators());
 	}
 	
 
 }
