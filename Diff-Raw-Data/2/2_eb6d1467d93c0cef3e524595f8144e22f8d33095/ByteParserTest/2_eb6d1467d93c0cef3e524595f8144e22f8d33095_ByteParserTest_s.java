 package com.alexrnl.commons.arguments.parsers;
 
 import static org.junit.Assert.assertEquals;
 
 import java.lang.reflect.Field;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Test suite for the {@link ByteParser} class.
  * @author Alex
  */
 public class ByteParserTest {
	/** The float parser */
 	private ByteParser			parser;
 	/** The field to set */
 	private byte				field;
 	/** The reference to the field to set */
 	private Field				fieldReference;
 	
 	/**
 	 * Set up test attributes.
 	 * @throws NoSuchFieldException
 	 *         if the field does not exists.
 	 * @throws SecurityException
 	 *         if the field is not accessible.
 	 */
 	@Before
 	public void setUp () throws NoSuchFieldException, SecurityException {
 		parser = new ByteParser();
 		fieldReference = ByteParserTest.class.getDeclaredField("field");
 		fieldReference.setAccessible(true);
 	}
 	
 	/**
 	 * Test method for {@link ByteParser#parse(Object, Field, String)}.
 	 */
 	@Test
 	public void testParse () {
 		parser.parse(this, fieldReference, "28");
 		assertEquals(28, field);
 	}
 	
 	/**
 	 * Test method for error cases of {@link ByteParser#parse(Object, Field, String)}.
 	 */
 	@Test(expected = IllegalArgumentException.class)
 	public void testParseIllegalArgument () {
 		parser.parse(this, fieldReference, "8a");
 	}
 }
