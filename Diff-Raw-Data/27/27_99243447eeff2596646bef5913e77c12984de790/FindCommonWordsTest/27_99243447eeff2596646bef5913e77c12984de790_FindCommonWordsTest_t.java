 package gr.elchetz.idocs;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class FindCommonWordsTest {
 
	private static final String TEST_FILE1_TXT = "test-file1.txt";
 	private static final String TEST_FILE2_TXT = "test-file2.txt";
	private static final String[] EXPECTED_RESULTS = { "b", "e", "i", "o", "u",
			"x", "y", "z" };
	private File file1;
 	private File file2;
 	private FindCommonWords testMe;
 
 	@Before
 	public void setUp() throws Exception {
 
 		this.testMe = new FindCommonWords();
		this.file1 = new File(this.getClass().getClassLoader()
				.getResource(TEST_FILE1_TXT).toURI());
 		this.file2 = new File(this.getClass().getClassLoader()
 				.getResource(TEST_FILE2_TXT).toURI());
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		this.testMe = null;
		this.file1 = null;
 		this.file2 = null;
 	}
 
 	@Test
 	public void testFindCommonWords() throws Exception {
 		StringWriter writer = new StringWriter();
		testMe.findCommonWords(file1, file2, 5, writer);
 		writer.close();
 		String[] result = writer.toString().split("\n");
		assertEquals("Results: " + Arrays.toString(result),8, result.length);
 		assertArrayEquals(EXPECTED_RESULTS, result);
 	}
 
 }
