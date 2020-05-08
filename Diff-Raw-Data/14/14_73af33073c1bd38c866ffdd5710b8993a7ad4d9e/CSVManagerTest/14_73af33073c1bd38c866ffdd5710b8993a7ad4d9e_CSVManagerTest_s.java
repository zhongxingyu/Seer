 package ch22.ex11;
 
 import static org.junit.Assert.*;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.List;
 
 import org.junit.Test;
 
 public class CSVManagerTest {
 
 	@Test
 	public void testreadCSVTable1() {
 		String INPUT_FILE_PATH = "JPL/ch22/ex11/test1.csv";
 		try {
 			String[] expected = {"test1","test2","test3","test4"};
 			FileReader br = new FileReader(INPUT_FILE_PATH);			
 			List<String[]> vals = CSVManager.readCSVTable(br, 4);
 			String[] actual = vals.get(0);
 			for (int i = 0; i < expected.length; i++) {
 				assertEquals(actual[i], expected[i]);
 			}
 		} catch (FileNotFoundException e) {
 			fail();
 		} catch (IOException e) {
 			fail();
 		}
 	}
 	
 	@Test
 	public void testreadCSVTable2() {
 		String INPUT_FILE_PATH = "JPL/ch22/ex11/test2.csv";
 		try {
 			String[] expected = {"test1","test2","test3","test4","test5"};
 			FileReader br = new FileReader(INPUT_FILE_PATH);			
 			List<String[]> vals = CSVManager.readCSVTable(br, 5);
 			String[] actual = vals.get(0);
 			for (int i = 0; i < expected.length; i++) {
 				assertEquals(actual[i], expected[i]);
 			}
 		} catch (FileNotFoundException e) {
 			fail();
 		} catch (IOException e) {
 			fail();
 		}
 	}
 	
 	@Test
 	public void testreadCSVTable3() {
 		String INPUT_FILE_PATH = "JPL/ch22/ex11/test3.csv";
 		try {
 			String[] expected1 = {"test1","test2"};
 			String[] expected2 = {"test3","test4"};
 			FileReader br = new FileReader(INPUT_FILE_PATH);			
 			List<String[]> vals = CSVManager.readCSVTable(br, 2);
 			String[] actual1 = vals.get(0);
 			for (int i = 0; i < expected1.length; i++) {
 				assertEquals(actual1[i], expected1[i]);
 			}
 			String[] actual2 = vals.get(1);
 			for (int i = 0; i < expected2.length; i++) {
 				assertEquals(actual2[i], expected2[i]);
 			}
 		} catch (FileNotFoundException e) {
 			fail();
 		} catch (IOException e) {
 			fail();
 		}
 	}
 }
