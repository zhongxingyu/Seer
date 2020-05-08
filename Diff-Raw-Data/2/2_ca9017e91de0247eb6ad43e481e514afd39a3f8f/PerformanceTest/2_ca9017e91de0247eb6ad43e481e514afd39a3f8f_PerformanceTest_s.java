 package com.fexco.util.performance;
 
 import java.util.Locale;
 import java.util.Random;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.fexco.util.IWordifiedNumber;
 import com.fexco.util.manager.NumberConversionManager;
 import com.fexco.util.manager.UnsupportedLocaleException;
 
 /**
  * Sample usage of provided library
  */
 public class PerformanceTest {
 
 	private IWordifiedNumber converter;
 
 	@Before
 	public void setUp() throws Exception {
 		Locale locale = Locale.ENGLISH;
 		NumberConversionManager instance = NumberConversionManager.getInstance();
 		try {
 			converter = instance.getConverter(locale);
 		} catch (UnsupportedLocaleException e) {
 			System.out.println(locale.getDisplayLanguage() + " not implemented");
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void testConverter() {
 		long start = System.currentTimeMillis();
 		Random rand = new Random(System.nanoTime());
		for (int i = 1; i < 10000; i++) {
 			int nextInt = rand.nextInt(999999999);
 			converter.toWords(nextInt);
 		}
 		long stop = System.currentTimeMillis();
 		Assert.assertFalse((stop - start) > 1000);
 	}
 
 }
