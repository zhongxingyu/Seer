 package net.sf.okapi.steps.wordcount;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class WordCountTest {
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@Test
 	public void testStatics() {
 		
 		assertEquals(5, WordCounter.getCount("Test word count is correct.", "en"));
 		assertEquals(9, WordCounter.getCount("The quick (\"brown\") fox can't jump 32.3 feet, right?", "en"));
		assertEquals(9, WordCounter.getCount("The quick (\u201Cbrown\u201D) fox can\u2019t jump 32.3 feet, right?", "en"));
 	}
 	
 }
