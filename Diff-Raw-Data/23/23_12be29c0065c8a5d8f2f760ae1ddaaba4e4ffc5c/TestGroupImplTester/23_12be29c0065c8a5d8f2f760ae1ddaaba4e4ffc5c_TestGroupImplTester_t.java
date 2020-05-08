 package org.testtoolinterfaces.testsuite;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 import org.junit.Before;
 import org.testtoolinterfaces.testsuite.TestEntry;
 import org.testtoolinterfaces.testsuite.TestEntryCollection;
 import org.testtoolinterfaces.testsuite.TestGroupImpl;
 
 
 public class TestGroupImplTester extends TestCase
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
 	 * Test Cases
 	 */
 	public void testCase_constructor0()
 	{
 		TestGroupImpl testGroup = new TestGroupImpl( "ID",
 		                                             "A Description",
 		                                             6,
 		                                             new ArrayList<String>(),
 		                                             new TestStepSequence(),
 		                                             new TestEntryCollection(),
 		                                             new TestStepSequence(),
 		                                             new Hashtable<String, String>(),
 		                                             new Hashtable<String, String>() );
 
 		Assert.assertEquals("Incorrect Type", TestEntry.TYPE.Group, testGroup.getType());
 		Assert.assertEquals("Incorrect ID", "ID", testGroup.getId());
 		Assert.assertEquals("Incorrect Description", "A Description", testGroup.getDescription());
 
 		Assert.assertEquals("Incorrect Sequence Number", 6, testGroup.getSequenceNr());
 		Assert.assertTrue(  "Requirements not empty", testGroup.getRequirements().isEmpty());
 
 		Assert.assertTrue(  "Preparation Steps not empty", testGroup.getPrepareSteps().isEmpty());
 		Assert.assertTrue(  "Execution Steps not empty", testGroup.getExecutionEntries().isEmpty());
 		Assert.assertTrue(  "Restore Steps not empty", testGroup.getRestoreSteps().isEmpty());
 
 		Assert.assertTrue(  "Any Attributes not empty", testGroup.getAnyAttributes().isEmpty());
 		Assert.assertTrue(  "Any Elements not empty", testGroup.getAnyElements().isEmpty());
 	}
 
 	/**
 	 * Test Cases
 	 */
 	public void testCase_constructor1()
 	{
 		TestGroupImpl testGroup = new TestGroupImpl( "ID",
 		                                             "A Description",
 		                                             6,
 		                                             new ArrayList<String>(),
 		                                             new TestStepSequence(),
 		                                             new TestEntryCollection(),
 		                                             new TestStepSequence() );
 
 		Assert.assertEquals("Incorrect Type", TestEntry.TYPE.Group, testGroup.getType());
 		Assert.assertEquals("Incorrect ID", "ID", testGroup.getId());
 		Assert.assertEquals("Incorrect Description", "A Description", testGroup.getDescription());
 
 		Assert.assertTrue(  "Any Attributes not empty", testGroup.getAnyAttributes().isEmpty());
 		Assert.assertTrue(  "Any Elements not empty", testGroup.getAnyElements().isEmpty());
 	}
 
 	/**
 	 * Test Cases
 	 */
 	public void testCase_constructor2()
 	{
 		TestGroupImpl testGroup = new TestGroupImpl( "ID",
 		                                             6,
 		                                             new ArrayList<String>(),
 		                                             new TestStepSequence(),
 		                                             new TestEntryCollection(),
 		                                             new TestStepSequence() );
 
 		Assert.assertEquals("Incorrect Type", TestEntry.TYPE.Group, testGroup.getType());
 		Assert.assertEquals("Incorrect ID", "ID", testGroup.getId());
 		Assert.assertEquals("Incorrect Description", "", testGroup.getDescription());
 	}
 
 	/**
 	 * Test Cases
 	 */
 	public void testCase_constructor3()
 	{
 		TestGroupImpl testGroup = new TestGroupImpl( "ID",
 		                                             "A Description",
 		                                             6,
 		                                             new TestStepSequence(),
 		                                             new TestEntryCollection(),
 		                                             new TestStepSequence() );
 
 		Assert.assertEquals("Incorrect Type", TestEntry.TYPE.Group, testGroup.getType());
 		Assert.assertEquals("Incorrect ID", "ID", testGroup.getId());
 		Assert.assertEquals("Incorrect Description", "A Description", testGroup.getDescription());
 		Assert.assertTrue(  "Requirements not empty", testGroup.getRequirements().isEmpty());
 	}
 
 	/**
 	 * Test Cases
 	 */
 	public void testCase_constructor4()
 	{
 		TestGroupImpl testGroup = new TestGroupImpl( "ID",
 		                                             6,
 		                                             new TestStepSequence(),
 		                                             new TestEntryCollection(),
 		                                             new TestStepSequence() );
 
 		Assert.assertEquals("Incorrect Type", TestEntry.TYPE.Group, testGroup.getType());
 		Assert.assertEquals("Incorrect ID", "ID", testGroup.getId());
 		Assert.assertEquals("Incorrect Description", "", testGroup.getDescription());
 		Assert.assertTrue(  "Requirements not empty", testGroup.getRequirements().isEmpty());
 	}
 	
 	/**
 	 * Test Cases
 	 */
 	public void testCase_hasGroupId()
 	{
 		// Test Group ID1_1
 		TestGroupImpl testGroup1_1 = new TestGroupImpl( "ID1_1",
 		                                             1,
 		                                             new TestStepSequence(),
 		                                             new TestEntryCollection(),
 		                                             new TestStepSequence() );
 
 		// Test Group ID1_2
 		TestGroupImpl testGroup1_2 = new TestGroupImpl( "ID1_2",
 		                                             2,
 		                                             new TestStepSequence(),
 		                                             new TestEntryCollection(),
 		                                             new TestStepSequence() );
 
 		// Test Group ID1
 		TestEntryCollection te1 = new TestEntryCollection();
 		te1.add(testGroup1_1);
 		te1.add(testGroup1_2);
 		TestGroupImpl testGroup1 = new TestGroupImpl( "ID1",
 		                                              6,
 		                                              new TestStepSequence(),
 		                                              te1,
 		                                              new TestStepSequence() );
 
 		// Test Group ID2
 		TestGroupImpl testGroup2 = new TestGroupImpl( "ID2",
 		                                              4,
 		                                              new TestStepSequence(),
 		                                              new TestEntryCollection(),
 		                                              new TestStepSequence() );
 
 		// Test Case
 		TestCaseImpl testCase = new TestCaseImpl(
 		                         				"tcId",
 		                         				0,
 		                         				new TestStepSequence(0),
 		                         				new TestStepSequence(0),
 		                         				new TestStepSequence(0) );
 
 		// Test Group ID2
 		TestGroupLink testGroupLink = new TestGroupLink( "tgLink",
 		                                              3,
		                                              "a/link" );
 
 		// Test Group all
 		TestEntryCollection te_all = new TestEntryCollection();
 		te_all.add( testGroup1 );
 		te_all.add( testGroup2 );
 		te_all.add( testCase );
 		te_all.add( testGroupLink );
 		TestGroupImpl testGroup_all = new TestGroupImpl( "all",
 		                                             6,
 		                                             new TestStepSequence(),
 		                                             te_all,
 		                                             new TestStepSequence() );
 
 		Assert.assertTrue(  "TestGroup not present", testGroup_all.hasGroupId("all") );   // top
 		Assert.assertTrue(  "TestGroup not present", testGroup_all.hasGroupId("ID2") );   // leaf
 		Assert.assertTrue(  "TestGroup not present", testGroup_all.hasGroupId("ID1") );   // not top, not leaf
 		Assert.assertTrue(  "TestGroup not present", testGroup_all.hasGroupId("ID1_2") ); // sub-sub
 		Assert.assertFalse( "TestCase was searched, should not", testGroup_all.hasGroupId("tcId") ); // test case
 		Assert.assertTrue(  "TestGroupLink not present", testGroup_all.hasGroupId("tgLink") ); // TestGroupLink
 	}
 }
