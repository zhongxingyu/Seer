 package org.testtoolinterfaces.testsuite;
 
 import java.util.Hashtable;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.testtoolinterfaces.testsuite.ParameterArrayList;
 import org.testtoolinterfaces.testsuite.TestEntry;
 
 public class TestStepCommandTester extends TestCase
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
 	 * Test method for {@link org.testtoolinterfaces.testsuite.TestStepCommand#TestStepCommand()}.
 	 */
 	@Test
 	public void testConstructor0()
 	{
 		Hashtable<String, String> anyParams = new Hashtable<String, String>();
 		anyParams.put("param1", "value1");
 		anyParams.put("param2", "value2");
 		Hashtable<String, String> anyElems = new Hashtable<String, String>();
 		anyElems.put("paramA", "valueA");
 		anyElems.put("paramB", "valueB");
 		anyElems.put("paramC", "valueC");
 
 		TestInterface_stub iface = new TestInterface_stub( "interface1" );
 
 		ParameterArrayList paramTable = new ParameterArrayList();
 
 		ParameterImpl param_e = new ParameterImpl("-e", "e-parameter");
 		param_e.setIndex(0);
 		paramTable.add(param_e);
 
 		TestStepCommand testStep = new TestStepCommand(
 				0,
 				"An extensive description",
 				"commandA",
 				iface,
 				paramTable,
 				anyParams,
 				anyElems );
 
 		Assert.assertEquals("Incorrect Type", TestEntry.TYPE.Step, testStep.getType());
 		Assert.assertNull(  "Step has an ID", testStep.getId());
 		Assert.assertEquals("Incorrect Description", "An extensive description", testStep.getDescription());
 		Assert.assertEquals("Incorrect Parameters", "e-parameter", ((ParameterImpl) testStep.getParameters().get("-e")).getValue());
 
 		Assert.assertEquals("Incorrect Command", "commandA", testStep.getCommand());
 		Assert.assertEquals("Incorrect Interface", "interface1", testStep.getInterface().getInterfaceName());
		Assert.assertEquals("Incorrect Display Name", "interface1:commandA", testStep.getDisplayName());
 		Assert.assertEquals("Incorrect toString", testStep.getDisplayName(), testStep.toString());
 
 		Assert.assertEquals("Incorrect Any Attributes size", 2, testStep.getAnyAttributes().size());
 		Assert.assertEquals("Incorrect Any Elements size", 3, testStep.getAnyElements().size());
 	}
 
 	/**
 	 * Test method for {@link org.testtoolinterfaces.testsuite.TestStepCommand#TestStepCommand()}.
 	 */
 	@Test
 	public void testConstructor1()
 	{
 		TestInterface_stub iface = new TestInterface_stub( "interface1" );
 
 		ParameterArrayList paramTable = new ParameterArrayList();
 
 		ParameterImpl param_e = new ParameterImpl("-e", "e-parameter");
 		param_e.setIndex(0);
 		paramTable.add(param_e);
 
 		TestStepCommand testStep = new TestStepCommand(
 				0,
 				"An extensive description",
 				"commandA",
 				iface,
 				paramTable );
 
 		Assert.assertEquals("Incorrect Type", TestEntry.TYPE.Step, testStep.getType());
 		Assert.assertNull(  "Step has an ID", testStep.getId());
 		Assert.assertEquals("Incorrect Description", "An extensive description", testStep.getDescription());
 		Assert.assertEquals("Incorrect Parameters", "e-parameter", ((ParameterImpl) testStep.getParameters().get("-e")).getValue());
 
 		Assert.assertEquals("Incorrect Command", "commandA", testStep.getCommand());
 		Assert.assertEquals("Incorrect Interface", "interface1", testStep.getInterface().getInterfaceName());
		Assert.assertEquals("Incorrect Display Name", "interface1:commandA", testStep.getDisplayName());
 		Assert.assertEquals("Incorrect toString", testStep.getDisplayName(), testStep.toString());
 
 		Assert.assertTrue(  "Any Attributes not empty", testStep.getAnyAttributes().isEmpty());
 		Assert.assertTrue(  "Any Elements not empty", testStep.getAnyElements().isEmpty());
 	}
 
 	/**
 	 * Test method for {@link org.testtoolinterfaces.testsuite.TestStepCommand#TestStepCommand()}.
 	 */
 	@Test
 	public void testConstructor2()
 	{
 		TestInterface_stub iface = new TestInterface_stub( "interface1" );
 
 		TestStepCommand testStep = new TestStepCommand(
 				0,
 				"An extensive description",
 				"commandB",
 				iface );
 
 		Assert.assertEquals("Incorrect Type", TestEntry.TYPE.Step, testStep.getType());
 		Assert.assertNull(  "Step has an ID", testStep.getId());
 		Assert.assertEquals("Incorrect Description", "An extensive description", testStep.getDescription());
 		Assert.assertTrue(  "Incorrect Parameters", testStep.getParameters().isEmpty());
		Assert.assertEquals("Incorrect Display Name", "interface1:commandB", testStep.getDisplayName());
 
 		Assert.assertTrue(  "Any Attributes not empty", testStep.getAnyAttributes().isEmpty());
 		Assert.assertTrue(  "Any Elements not empty", testStep.getAnyElements().isEmpty());
 	}
 
 	/**
 	 * Test method for {@link org.testtoolinterfaces.testsuite.TestStepCommand#TestStepCommand()}.
 	 */
 	@Test
 	public void testConstructor3()
 	{
 		TestInterface_stub iface = new TestInterface_stub( "interface1" );
 
 		ParameterArrayList paramTable = new ParameterArrayList();
 
 		ParameterImpl param_e = new ParameterImpl("-f", "f-parameter");
 		param_e.setIndex(0);
 		paramTable.add(param_e);
 
 		TestStepCommand testStep = new TestStepCommand(
 				0,
 				"commandC",
 				iface,
 				paramTable );
 
 		Assert.assertEquals("Incorrect Type", TestEntry.TYPE.Step, testStep.getType());
 		Assert.assertNull(  "Step has an ID", testStep.getId());
 		Assert.assertEquals("Incorrect Description", "", testStep.getDescription());
 		Assert.assertEquals("Incorrect Parameters", "f-parameter", ((ParameterImpl) testStep.getParameters().get("-f")).getValue());
		Assert.assertEquals("Incorrect Display Name", "interface1:commandC", testStep.getDisplayName());
 	}
 
 	/**
 	 * Test method for {@link org.testtoolinterfaces.testsuite.TestStepCommand#TestStepCommand()}.
 	 */
 	@Test
 	public void testConstructor4()
 	{
 		TestInterface_stub iface = new TestInterface_stub( "interface1" );
 
 		TestStepCommand testStep = new TestStepCommand(
 				0,
 				"commandD",
 				iface );
 
 		Assert.assertEquals("Incorrect Type", TestEntry.TYPE.Step, testStep.getType());
 		Assert.assertNull(  "Step has an ID", testStep.getId());
 		Assert.assertEquals("Incorrect Description", "", testStep.getDescription());
 		Assert.assertTrue(  "Incorrect Parameters", testStep.getParameters().isEmpty());
		Assert.assertEquals("Incorrect Display Name", "interface1:commandD", testStep.getDisplayName());
 	}
 
 	/**
 	 * Test Case
 	 */
 	@Test
 	public void testConstructor_setDescription()
 	{
 		TestInterface_stub iface = new TestInterface_stub( "interface1" );
 
 		TestStepCommand testStep = new TestStepCommand(
 				0,
 				"An extensive description",
 				"commandD",
 				iface );
 
 		Assert.assertEquals("Incorrect Type", TestEntry.TYPE.Step, testStep.getType());
 		Assert.assertEquals("Incorrect Description (1)", "An extensive description", testStep.getDescription());
 
 		testStep.setDescription( "new description" );
 		Assert.assertEquals("Incorrect Description (2)", "new description", testStep.getDescription());
 	}
 }
