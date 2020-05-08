 package org.effrafax.reflection;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.lang.reflect.Field;
 
 import org.effrafax.querybuilder.core.criteria.PropertyCriterium;
 import org.effrafax.querybuilder.core.criteria.StringPropertyCriterium;
 import org.junit.Test;
 
 public class ReflectionTest
 {
 	@Test
 	public void shouldReturnFields()
 	{
 		Class<?> aClass = PropertyCriterium.class;
 
 		Field[] fields = aClass.getFields();
 
 		assertTrue(0 == fields.length);
 	}
 
 	@Test
 	public void shouldReturnFieldsOfParent()
 	{
 		Class<?> aClass = StringPropertyCriterium.class;
 
 		Field[] fields = aClass.getFields();
 
 		assertTrue(0 == fields.length);
 	}
 
 	@Test(expected = NoSuchFieldException.class)
 	public void aFieldHasAName() throws SecurityException, NoSuchFieldException
 	{
 		Class<?> aClass = PropertyCriterium.class;
 
		aClass.getField("propertyName");
 	}
 
 	@Test
 	public void aClassHasAPackage()
 	{
 		Class<?> aClass = PropertyCriterium.class;
 
 		assertEquals("org.effrafax.querybuilder.core.criteria", aClass.getPackage().getName());
 	}
 }
