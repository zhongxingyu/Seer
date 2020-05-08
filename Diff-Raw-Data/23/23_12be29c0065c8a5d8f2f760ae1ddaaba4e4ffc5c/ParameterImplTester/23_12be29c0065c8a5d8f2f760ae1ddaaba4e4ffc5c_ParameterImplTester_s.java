 /**
  * 
  */
 package org.testtoolinterfaces.testsuite;
 
import java.io.File;

 import junit.framework.TestCase;
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.testtoolinterfaces.testsuite.ParameterImpl;
 
 /**
  * @author Arjan Kranenburg
  *
  */
 public class ParameterImplTester extends TestCase
 {
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception
 	{
 		System.out.println("==========================================================================");
 		System.out.println(this.getName() + ":");
 	}
 
 	/**
 	 * Test method for {@link org.testtoolinterfaces.testsuite.ParameterImpl#Parameter(java.lang.String, java.lang.Object)}.
 	 */
 	@Test
 	public void testConstructorStringObject()
 	{
 		ParameterImpl param = new ParameterImpl( "name", new Integer(3) );
 		
 		Assert.assertEquals("Incorrect name", "name", param.getName());
 		Assert.assertEquals("Incorrect Index", 99, param.getIndex());
 		Assert.assertEquals("Incorrect Value Type", Integer.class, param.getValueType());
 		Assert.assertEquals("Incorrect Value", new Integer(3), param.getValue());
 
 		ParameterImpl param2 = new ParameterImpl( "name2", 2, 5 );
 		
 		Assert.assertEquals("Incorrect name2", "name2", param2.getName());
 		Assert.assertEquals("Incorrect Index", 5, param2.getIndex());
 		Assert.assertEquals("Incorrect Value Type", Integer.class, param2.getValueType());
 		Assert.assertEquals("Incorrect Value", 2, param2.getValueAsInt());
 	}
 
 	/**
 	 * Test method for {@link org.testtoolinterfaces.testsuite.ParameterImpl#getValueAsString()}.
 	 */
 	@Test
 	public void testGetValueAsString()
 	{
 		ParameterImpl param = new ParameterImpl( "name", "Value" );
 		
 		Assert.assertEquals("Incorrect value", "Value", param.getValueAsString());
 	}
 
 	/**
 	 * Test method for {@link org.testtoolinterfaces.testsuite.Parameter#getValueAs( java.lang.Class<Type> )}.
 	 */
 	@Test
 	public void testGetValueAsDerivedObject()
 	{
		TestGroupLink tgLink = new TestGroupLink( "anId", 2, new File("a/link") );
 		ParameterImpl param = new ParameterImpl( "name", tgLink );
 		
 		TestEntryImpl paramValue = param.getValueAs(TestEntryImpl.class);
 		Assert.assertNotNull(paramValue);
 		Assert.assertEquals("Incorrect class", TestGroupLink.class, paramValue.getClass());
 
 		TestEntry paramSameValue = param.getValueAs(TestEntry.class);
 		Assert.assertNotNull(paramSameValue);
 		Assert.assertEquals("Incorrect class", TestGroupLink.class, paramSameValue.getClass());
 
 		ParameterImpl boolPar = new ParameterImpl( "boolName", true );
 		Boolean valueAsBoolean = boolPar.getValueAs(Boolean.class);
 		Assert.assertNotNull(valueAsBoolean);
 		Assert.assertEquals("Incorrect class", Boolean.class, valueAsBoolean.getClass());
 		Assert.assertTrue("Incorrect value", valueAsBoolean);
 	}
 
 	/**
 	 * Test method for {@link org.testtoolinterfaces.testsuite.ParameterImpl#getValueAsInt()}.
 	 */
 	@Test
 	public void testGetValueAsInt()
 	{
 		ParameterImpl param = new ParameterImpl( "name", 3 );
 		Assert.assertEquals("Incorrect value", 3, param.getValueAsInt());
 
 		ParameterImpl paramString = new ParameterImpl( "name", "five" );
 		Assert.assertEquals("Incorrect value", 0, paramString.getValueAsInt());
 	}
 
 	/**
 	 * Test method for {@link org.testtoolinterfaces.testsuite.ParameterImpl#setIndex(int)}.
 	 */
 	@Test
 	public void testSetIndex()
 	{
 		ParameterImpl param = new ParameterImpl( "name", "A String" );
 	
 		Assert.assertEquals("Incorrect Index (a)", 99, param.getIndex());
 		
 		param.setIndex(5);
 		Assert.assertEquals("Incorrect Index (b)", 5, param.getIndex());
 	}
 
 	/**
 	 * Test method for {@link org.testtoolinterfaces.testsuite.ParameterImpl#setIndex(int)}.
 	 */
 	@Test
 	public void testSetValue()
 	{
 		ParameterImpl param = new ParameterImpl( "name", "A String" );
 	
 		Assert.assertEquals("Incorrect value (a)", "A String", param.getValueAsString());
 		
 		Object obj = new Object();
 		param.setValue( obj );
 		Assert.assertEquals("Incorrect value (b)", obj, param.getValue());
 	}
 }
