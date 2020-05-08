 /**
  * 
  */
 package com.appspot.anaki808built.utils.converters;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 /**
  * @author andi
  */
 public final class BooleanConverterTest {
 
 	/**
 	 * 
 	 */
 	@Test(timeout = 100L)
 	public void convert() {
 		final boolean result = BooleanConverter.convert("y");
 		assertTrue(result);
 		assertFalse(BooleanConverter.convert(null));
 		assertFalse(BooleanConverter.convert("n"));
 	}
 }
