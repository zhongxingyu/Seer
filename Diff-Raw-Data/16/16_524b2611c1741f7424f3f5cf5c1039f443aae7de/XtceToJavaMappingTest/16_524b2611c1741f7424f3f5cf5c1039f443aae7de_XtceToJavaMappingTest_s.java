 package org.hbird.transport.xtce.utils;
 
 import static org.junit.Assert.*;
 
 import org.hbird.transport.generatedcode.xtce.IntegerParameterType;
 import org.junit.Test;
 
 public class XtceToJavaMappingTest {
 
	private static final int MAX_BITS_FOR_JAVA_SIGNED_INTEGER = 31;
	private static final int MAX_BITS_FOR_JAVA_SIGNED_UINTEGER = 32;
 
 	@Test
 	public void testDoesIntRequireJavaLong() {
 		IntegerParameterType type = new IntegerParameterType();
 		
 		type.setName("Signed requires Long test parameter");
 		type.setSizeInBits(MAX_BITS_FOR_JAVA_SIGNED_INTEGER + 1);
 		type.setSigned(true);
 		
 		boolean condition = XtceToJavaMapping.doesIntRequireJavaLong(type);
 		assertTrue("Type should required a java long", condition);
 		
 		type.setSizeInBits(MAX_BITS_FOR_JAVA_SIGNED_UINTEGER + 1);
 		type.setSigned(false);
 		condition = XtceToJavaMapping.doesIntRequireJavaLong(type);
 		assertTrue("Type should required a java long", condition);
 	}
 
 }
