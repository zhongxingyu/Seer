 package org.dimitri_lang.test;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.dimitri_lang.runtime.level1.*;
 import org.dimitri_lang.runtime.level3.*;
 import org.dimitri_lang.generated.*;
 
 public class TestLevel4Test2 {
 
 	public final static String TEST_DIRECTORY = "testdata";
 	public final static String TEST_FILE = "test2.l4t2";
 	private String _name;
 	
 	public TestLevel4Test2() {
 		_name = TEST_FILE;
 	}
 
 	@Test
 	public void testGeneratedValidator() {
		org.dimitri_lang.runtime.level3.Validator validator = new L4T2Validator();
 		org.dimitri_lang.runtime.level3.ValidatorInputStream stream = org.dimitri_lang.runtime.level3.ValidatorInputStreamFactory.create(TEST_DIRECTORY + "/" + _name);
 		validator.setStream(stream);
 		ParseResult result = validator.tryParse();
 		Assert.assertTrue("Parsing failed. " + validator.getClass() + " on " + _name + ". Last read: " + result.getLastRead() + "; Last location: " + result.getLastLocation() + "; Last symbol: " + result.getSymbol() + "; Sequence: " + result.getSequence(), result.isSuccess());
 	}
 
 }
