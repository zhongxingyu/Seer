 package com.gooddata.util;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 
 import org.junit.Test;
 
 import junit.framework.Assert;
 
 public class CSVReaderTest {
 
 	@Test
 	public void testLoadCrazyFile() throws IOException {
 		Reader reader = new InputStreamReader(
 				CSVReaderTest.class.getResourceAsStream("/com/gooddata/util/crazy.csv"),
 				"utf8");
 		CSVReader csv = new CSVReader(reader); 
 				
 		final int expectedRows = 6;
 		int i = 0;
 		String[] line = null;
 		while ((line = csv.readNext()) != null) {
 			Assert.assertEquals(4, line.length);
 			i++;
 			if (i == expectedRows) {
 				for (Integer j = 1; j <= 4; j++) {
 					Assert.assertEquals(j.toString(), line[j - 1]);
 				}
 			}
 		}
 		Assert.assertEquals(expectedRows, i);
 	}
 
     @Test
 	public void testEscaping() throws IOException {
 		Reader reader = new InputStreamReader(
 				CSVReaderTest.class.getResourceAsStream("/com/gooddata/util/escaping.csv"),
 				"utf8");
 		CSVReader csv = new CSVReader(reader);
 
 		final int expectedRows = 2;
 		int i = 0;
 		String[] line = null;
 		while ((line = csv.readNext()) != null) {
 			Assert.assertEquals(4, line.length);
 			i++;
 		}
 		Assert.assertEquals(expectedRows, i);
 	}
 }
