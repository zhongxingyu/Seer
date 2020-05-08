 package com.ironiacorp.introspector;
 /*
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
  
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
  
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  
 Copyright (C) 2007 Marco Aurelio Graciotto Silva <magsilva@gmail.com>
 */
 
 
 
 import java.lang.annotation.Annotation;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.reflect.Field;
 
 
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 
import com.ironiacorp.datastructure.array.ArrayUtil;
 
 
 public class AnnotationUtilTest
 {
 	private static Class<? extends Object> annotatedClass;
 	private static Class<? extends Object> ordinaryClass;
 
 	private static Class<? extends Annotation> validAnnotation;
 	private static Class<? extends Annotation> invalidAnnotation;
 	
 	private final static String DEFAULT_VALUE = "test123";
 	private final static String DEFAULT_FIELD_NAME = AnnotationUtil.DEFAULT_PROPERTY;
 	private final static String VALID_FIELD_NAME = "test";
 	private final static String INVALID_FIELD_NAME = "asdfg";
 	private final static Field[] validFields = new Field[1];
 	private final static Field[] invalidFields = new Field[1];
 	
 	private DummyClass bean;
 	
 	
 	@Retention(RetentionPolicy.RUNTIME)
 	public @interface DummyAnnotation
 	{
 		String value() default DEFAULT_VALUE;
 		String test() default DEFAULT_VALUE;
 	}
 	
 	@Retention(RetentionPolicy.RUNTIME)
 	public @interface DummyDummyAnnotation
 	{
 		String value() default DEFAULT_VALUE;
 		String test() default DEFAULT_VALUE;
 	}
 	
 	@DummyAnnotation
 	public class DummyClass
 	{
 		@DummyAnnotation
 		public String test;
 		
 		public String asdfg;
 	}
 	
 	@Before
 	public void setUp() throws Exception
 	{
 		bean = new DummyClass();
 		validFields[0] = bean.getClass().getField("test"); 	
 		invalidFields[0] = bean.getClass().getField("asdfg");
 		
 		annotatedClass = bean.getClass();
 		ordinaryClass = String.class;
 		
 		validAnnotation = DummyAnnotation.class;
 		invalidAnnotation = DummyDummyAnnotation.class;
 	}
 
 	@Test
 	public void testGetAnnotationDefaultValue1()
 	{
 		assertEquals(DEFAULT_VALUE, AnnotationUtil.getAnnotationValue(annotatedClass, validAnnotation));
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testGetAnnotationDefaultValue2()
 	{
 		AnnotationUtil.getAnnotationValue(annotatedClass, invalidAnnotation);
 		fail();
 	}
 
 	@Test
 	public void testGetAnnotationValue1()
 	{
 		assertEquals(DEFAULT_VALUE, AnnotationUtil.getAnnotationValue(annotatedClass, validAnnotation, DEFAULT_FIELD_NAME));
 	}
 
 	@Test
 	public void testGetAnnotationValue2()
 	{
 		assertEquals(DEFAULT_VALUE, AnnotationUtil.getAnnotationValue(annotatedClass, validAnnotation, VALID_FIELD_NAME));
 	}
 
 	@Test
 	public void testGetAnnotationValue3()
 	{
 		assertFalse(DEFAULT_VALUE.equals(AnnotationUtil.getAnnotationValue(annotatedClass, validAnnotation, INVALID_FIELD_NAME)));
 	}
 
 	@Test
 	public void testHasAnnotatiosClass1()
 	{
 		assertTrue(AnnotationUtil.hasAnnotations(annotatedClass));
 	}
 
 	@Test
 	public void testHasAnnotatiosClass2()
 	{
 		assertFalse(AnnotationUtil.hasAnnotations(ordinaryClass));
 	}
 
 	@Test
 	public void testGetAnnotatedPropertiesClass1()
 	{
 		Field[] fields = AnnotationUtil.getAnnotatedFields(annotatedClass);
 		assertTrue(ArrayUtil.equalIgnoreOrder(validFields, fields));
 	}
 
 	@Test
 	public void testGetAnnotatedPropertiesClass2()
 	{
 		Field[] fields = AnnotationUtil.getAnnotatedFields(annotatedClass);
 		assertFalse(ArrayUtil.equalIgnoreOrder(invalidFields, fields));
 	}
 
 	@Test
 	public void testGetAnnotatedPropertiesClass3()
 	{
 		Field[] fields = AnnotationUtil.getAnnotatedFields(ordinaryClass);
 		assertTrue(fields.length == 0);
 	}
 
 	
 	@Test
 	public void testGetAnnotatedPropertiesClassAnnotation1()
 	{
 		Field[] fields = AnnotationUtil.getAnnotatedFields(annotatedClass, validAnnotation);
 		assertTrue(ArrayUtil.equalIgnoreOrder(validFields, fields));
 	}
 
 	@Test
 	public void testGetAnnotatedPropertiesClassAnnotation2()
 	{
 		Field[] fields = AnnotationUtil.getAnnotatedFields(annotatedClass, validAnnotation);
 		assertFalse(ArrayUtil.equalIgnoreOrder(invalidFields, fields));
 	}
 
 	@Test
 	public void testGetAnnotatedPropertiesClassAnnotation3()
 	{
 		Field[] fields = AnnotationUtil.getAnnotatedFields(annotatedClass, invalidAnnotation);
 		assertTrue(fields.length == 0);
 	}
 }
