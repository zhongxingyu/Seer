 package com.alexrnl.commons.arguments.parsers;
 
 import static org.junit.Assert.assertEquals;
 
 import java.lang.reflect.Field;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Test suite for the {@link FloatParser} class.
  * @author Alex
  */
 public class FloatParserTest {
 	/** The delta for comparing floating numbers */
 	private static final float	DELTA_FLOAT_COMPARISON	= 0.001f;
 	/** The float parser */
	private FloatParser	parser;
 	/** The field to set */
	private float		field;
 	/** The reference to the field to set */
	private Field	fieldReference;
 	
 	/**
 	 * Set up test attributes.
 	 * @throws NoSuchFieldException
 	 *         if the field does not exists.
 	 * @throws SecurityException
 	 *         if the field is not accessible.
 	 */
 	@Before
 	public void setUp () throws NoSuchFieldException, SecurityException {
 		parser = new FloatParser();
 		fieldReference = FloatParserTest.class.getDeclaredField("field");
 		fieldReference.setAccessible(true);
 	}
 	
 	/**
 	 * Test method for {@link FloatParser#parse(Object, Field, String)}.
 	 */
 	@Test
 	public void testParse () {
 		parser.parse(this, fieldReference, "2.8");
 		assertEquals(2.8f, field, DELTA_FLOAT_COMPARISON);
 	}
 	
 	/**
 	 * Test method for error cases of {@link FloatParser#parse(Object, Field, String)}.
 	 */
 	@Test(expected = IllegalArgumentException.class)
 	public void testParseIllegalArgument () {
 		parser.parse(this, fieldReference, "12..12");
 	}
 }
