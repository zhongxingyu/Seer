 package com.horsefire.syncaws.fingerprint;
 
 import java.io.File;
 import java.io.IOException;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 public class Md5CalculatorTest extends TestCase {
 
 	@Test
 	public void testMd5() throws IOException {
		testMd5("59c9a5690432939ec043b23e6d06b3fb.html");
 		testMd5("c56c88d8651ab8d5d8d42b188cea3e6a.gif");
 		testMd5("f3763c7b84c3f56d623391842e9fe149.txt");
 	}
 
 	private void testMd5(String file) throws IOException {
 		Md5Calculator md5Calc = new Md5Calculator(new NumberConverter());
 		String hash = file.split("\\.")[0].toUpperCase();
 		String result = md5Calc.getMd5String(
 				new File("src/test/resources/sampleFiles/md5/" + file))
 				.toUpperCase();
 		assertEquals(hash, result);
 	}
 }
