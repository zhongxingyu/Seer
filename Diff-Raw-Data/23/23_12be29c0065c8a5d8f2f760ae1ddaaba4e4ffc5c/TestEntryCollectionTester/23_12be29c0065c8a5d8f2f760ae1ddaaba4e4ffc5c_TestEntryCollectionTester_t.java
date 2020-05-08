 package org.testtoolinterfaces.testsuite;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 import org.junit.Before;
 import org.testtoolinterfaces.testsuite.ParameterArrayList;
 import org.testtoolinterfaces.testsuite.TestCaseImpl;
 import org.testtoolinterfaces.testsuite.TestEntryCollection;
 import org.testtoolinterfaces.testsuite.TestGroupImpl;
 import org.testtoolinterfaces.testsuite.TestStep;
 
 
 public class TestEntryCollectionTester extends TestCase
 {
 	TestStep myTestStep = null;
 	TestCaseImpl myTestCase = null;
 	TestCaseLink myTestCase2 = null;
 	TestGroup myTestGroup = null;
 	
 	TestEntryCollection myTestEntryList = null;
 
 	private void assertOrder( TestEntryCollection atsList )
 	{
 		Iterator<TestEntry> itr = atsList.iterator();
 		int curSequence = 0;
 		while(itr.hasNext())
 		{
 			TestEntry entry = itr.next();
 			Assert.assertTrue("Not ordered " + entry.toString(), curSequence <= entry.getSequenceNr());
 			curSequence = entry.getSequenceNr();
 		}		
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception
 	{
 		System.out.println("==========================================================================");
 		System.out.println(this.getName() + ":");
 
 		if ( myTestStep == null )
 		{
 	   		TestInterface_stub iface = new TestInterface_stub( "interface" );
 	   		myTestStep = new TestStepCommand(
 	   		                                 2,
 	   		                                 "description2",
 	   		                                 "command2",
 	   		                                 iface,
 	   		                                 new ParameterArrayList() );
 		}
 
 		if ( myTestCase == null )
 		{
 			myTestCase = new TestCaseImpl( "tcId",
 			                               "An extensive description",
 			                               3,
 			                               new TestStepSequence(),
 			                               new TestStepSequence(),
 			                               new TestStepSequence() );
 		}
 
 		if ( myTestCase2 == null )
 		{
 			myTestCase2 = new TestCaseLink( "tcId",
 			                               4,
			                              "link" );
 		}
 
 		if ( myTestGroup == null )
 		{
 			myTestGroup = new TestGroupImpl( "ID",
 			                                 "A Description",
 			                                 1,
 			                                 new ArrayList<String>(),
 			                                 new TestStepSequence(),
 			                                 new TestEntryCollection(),
 			                                 new TestStepSequence() );
 		}
 
 		if ( myTestEntryList == null )
 		{
 			myTestEntryList = new TestEntryCollection();
 			myTestEntryList.add(myTestCase);
 			myTestEntryList.add(myTestStep);
 			myTestEntryList.add(myTestGroup);
 			
 		}
 	}
 
 	/**
 	 * Test Cases
 	 */
 	public void testCase_constructor()
 	{
 		TestEntryCollection teArray = new TestEntryCollection();
 
 		Assert.assertEquals("Incorrect Size", 0, teArray.size());
 		Assert.assertTrue(  "Incorrect isEmpty", teArray.isEmpty());
 	}
 
 	/**
 	 * Test Cases
 	 */
 	public void testCase_add()
 	{
 		TestEntryCollection teArray = new TestEntryCollection();
 
 		Assert.assertTrue("Add not OK", teArray.add(myTestCase) );
 		Assert.assertTrue("Add not OK", teArray.add(myTestStep) );
 		Assert.assertTrue("Add not OK", teArray.add(myTestGroup) );
 
 		Assert.assertEquals("Incorrect Size", 3, teArray.size());
 		assertOrder( teArray );
 	}
 
 	/**
 	 * Test Cases
 	 */
 	public void testCase_iterator()
 	{
 		Iterator<TestEntry> itr = myTestEntryList.iterator();
 		int i = 0;
 		while(itr.hasNext())
 		{
 		    TestEntry entry = itr.next();
 			Assert.assertEquals("Incorrect SeqNr " + entry.toString(), ++i, entry.getSequenceNr());
 		} 
 
 		assertOrder(myTestEntryList);
 	}
 
 	/**
 	 * Test Cases
 	 */
 	public void testCase_clear()
 	{
 		TestEntryCollection teArray = new TestEntryCollection(0);
 		teArray.add(myTestCase);
 		teArray.add(myTestStep);
 		teArray.add(myTestGroup);
 
 		Assert.assertEquals("Incorrect Size", 3, teArray.size());
 		teArray.clear();
 		Assert.assertEquals("Incorrect Size", 0, teArray.size());
 
 		assertOrder(teArray);
 	}
 
 	/**
 	 * Test Cases
 	 */
 	public void testCase_remove()
 	{
 		TestEntryCollection teArray = new TestEntryCollection(0);
 		teArray.add(myTestCase);
 		teArray.add(myTestStep);
 		teArray.add(myTestGroup);
 		Assert.assertEquals("Incorrect Size", 3, teArray.size());
 
 		Assert.assertTrue("Remove not OK", teArray.remove(myTestStep) );
 		Assert.assertEquals("Incorrect Size", 2, teArray.size());
 		assertOrder(teArray);
 
 		Assert.assertFalse("Remove OK, should not", teArray.remove(myTestStep) ); //again
 		Assert.assertEquals("Incorrect Size", 2, teArray.size());
 		assertOrder(teArray);
 
 		Assert.assertTrue("Remove not OK", teArray.remove(myTestCase) );
 		Assert.assertEquals("Incorrect Size", 1, teArray.size());
 		assertOrder(teArray);
 
 		Assert.assertFalse("Remove OK, should not", teArray.remove(myTestCase2) ); //non-existing
 		Assert.assertEquals("Incorrect Size", 1, teArray.size());
 		assertOrder(teArray);
 
 		teArray.add(myTestCase2); // new one
 		Assert.assertEquals("Incorrect Size", 2, teArray.size());
 		assertOrder(teArray);
 
 		Assert.assertTrue("Remove not OK", teArray.remove(myTestGroup) ); // last of orginal
 		Assert.assertEquals("Incorrect Size", 1, teArray.size());
 		assertOrder(teArray);
 
 		teArray.add(myTestStep);
 		teArray.add(myTestStep); // Duplicates are possible
 		Assert.assertEquals("Incorrect Size", 3, teArray.size());
 		assertOrder(teArray);
 
 		Assert.assertTrue("Remove not OK", teArray.remove(myTestStep) ); // Removes one
 		Assert.assertEquals("Incorrect Size", 2, teArray.size());
 		assertOrder(teArray);
 
 		Assert.assertTrue("Remove not OK", teArray.remove(myTestStep) ); // Removes other
 		Assert.assertEquals("Incorrect Size", 1, teArray.size());
 		assertOrder(teArray);
 
 		Assert.assertTrue("Remove not OK", teArray.remove(myTestCase2) ); // last remaining
 		Assert.assertEquals("Incorrect Size", 0, teArray.size());
 		assertOrder(teArray);
 	}
 
 	/**
 	 * Test Cases
 	 */
 	public void testCase_removeAll()
 	{
 		TestEntryCollection teArray1 = new TestEntryCollection(0);
 		teArray1.add(myTestCase);
 		teArray1.add(myTestStep);
 		teArray1.add(myTestGroup);
 		teArray1.add(myTestCase); // Duplicates are possible
 		teArray1.add(myTestCase2);
 
 		TestEntryCollection teArray2 = new TestEntryCollection(0);
 		teArray2.add(myTestCase);
 		teArray2.add(myTestGroup);
 
 		TestEntryCollection teArray3 = new TestEntryCollection(0);
 		teArray3.add(myTestStep);
 		teArray3.add(myTestGroup);
 
 		Assert.assertEquals("Incorrect Size", 5, teArray1.size());
 		assertOrder(teArray1);
 
 		teArray1.removeAll(teArray2); // all of teArray2 match
 		Assert.assertEquals("Incorrect Size", 2, teArray1.size()); // Also both entry3 are removed
 		assertOrder(teArray1);
 
 		teArray1.removeAll(teArray3); // some match
 		Assert.assertEquals("Incorrect Size", 1, teArray1.size());
 		assertOrder(teArray1);
 	}
 
 	/**
 	 * Test Cases
 	 */
 	public void testCase_retainAll()
 	{
 		TestEntryCollection teArray1 = new TestEntryCollection(0);
 		teArray1.add(myTestCase);
 		teArray1.add(myTestStep);
 		teArray1.add(myTestGroup);
 		teArray1.add(myTestStep); // Duplicates are possible
 		teArray1.add(myTestCase2);
 
 		TestEntryCollection teArray2 = new TestEntryCollection(1);
 		teArray2.add(myTestStep);
 		teArray2.add(myTestGroup);
 
 		TestEntryCollection teArray3 = new TestEntryCollection(2);
 		teArray3.add(myTestGroup);
 		teArray3.add(myTestCase2);
 
 		TestEntryCollection teArray4 = new TestEntryCollection(3);
 		teArray4.add(myTestGroup);
 
 		Assert.assertEquals("Incorrect Size", 5, teArray1.size());
 
 		Assert.assertTrue( "List not changed", teArray1.retainAll(teArray2) );
 		Assert.assertEquals("Incorrect Size", 3, teArray1.size()); // Also both entry3 are retained
 		assertOrder(teArray1);
 
 		Assert.assertTrue( "List not changed", teArray1.retainAll(teArray3) ); // some match
 		Assert.assertEquals("Incorrect Size", 1, teArray1.size());
 		assertOrder(teArray1);
 
 		Assert.assertFalse( "List changed, should not", teArray1.retainAll(teArray4) ); // all matches
 		Assert.assertEquals("Incorrect Size", 1, teArray1.size());
 		assertOrder(teArray1);
 	}
 
 	/**
 	 * Test Cases
 	 */
 	public void testCase_containsAll()
 	{
 		TestEntryCollection teArray1 = new TestEntryCollection();
 		teArray1.add(myTestCase);
 		teArray1.add(myTestStep);
 		
 		TestEntryCollection teArray2 = new TestEntryCollection(1);
 		teArray2.add(myTestCase);
 		teArray2.add(myTestStep);
 		teArray2.add(myTestGroup);
 		teArray2.add(myTestStep);
 		teArray2.add(myTestGroup);
 
 		TestEntryCollection teArray3 = new TestEntryCollection(2);
 		teArray3.add(myTestStep);
 		teArray3.add(myTestCase2);
 		TestStepCommand testStep7 = new TestStepCommand(4, "command7", new TestInterface_stub( "interface1" ));
 		teArray3.add(testStep7);
 
 		TestEntryCollection teArray4 = new TestEntryCollection(3);
 		teArray4.add(myTestStep);
 		teArray4.add(myTestCase);
 		teArray4.add(myTestCase);
 
 		Assert.assertTrue( "List did not contain all", myTestEntryList.containsAll(teArray2) );
 		Assert.assertFalse( "List contains all, but should not", myTestEntryList.containsAll(teArray3) );
 		Assert.assertTrue( "List did not contain all", teArray4.containsAll(teArray1) ); // Also works with duplicates
 	}
 
 	/**
 	 * Test Cases
 	 */
 	public void testCase_toArray()
 	{
 		TestEntry[] teArray = myTestEntryList.toArray();
 
 		Assert.assertEquals("Incorrect size", 3, teArray.length );
 	}
 
 	/**
 	 * Test Cases
 	 */
 	public void testCase_toArray_Array()
 	{
 		TestEntry[] array_small = new TestEntry[ 2 ];
 		TestEntry[] teArray1 = myTestEntryList.toArray( array_small );
 		Assert.assertNotSame("Same array returned", array_small, teArray1);
 		Assert.assertEquals( "Incorrect size", 3, teArray1.length );
 
 		TestEntry[] array_ok = new TestEntry[ 3 ];
 		TestEntry[] teArray2 = myTestEntryList.toArray( array_ok );
 		Assert.assertSame("New array returned", array_ok, teArray2);
 		Assert.assertEquals( "Incorrect size", 3, teArray2.length );
 
 		TestEntry[] array_large = new TestEntry[ 5 ];
 		TestEntry[] teArray3 = myTestEntryList.toArray( array_large );
 		Assert.assertSame("New array returned", array_large, teArray3);
 		Assert.assertEquals( "Incorrect size", 5, teArray3.length );
 	}
 
 	/**
 	 * Test Cases
 	 */
 	public void testCase_addAll_Collection()
 	{
 		// order of steps: 2 7 5 8
 		TestEntryCollection teArray = new TestEntryCollection(0);
 		teArray.add(myTestStep);
 		teArray.add(myTestCase);
 		TestStepCommand testStep7 = new TestStepCommand(4, "command7", new TestInterface_stub( "interface1" ));
 		teArray.add(testStep7);
 		TestStepCommand testStep8 = new TestStepCommand(7, "command8", new TestInterface_stub( "interface1" ));
 		teArray.add(testStep8);
 
 		Assert.assertTrue("No steps were added", teArray.addAll(myTestEntryList) );
 		Assert.assertEquals("Incorrect size", 7, teArray.size());
 		Assert.assertEquals("Incorrect size", 3, myTestEntryList.size()); // unchanged
 		assertOrder(teArray);
 
 		Assert.assertTrue("No steps were added", teArray.addAll(myTestEntryList) );
 		Assert.assertEquals("Incorrect size", 10, teArray.size());
 		assertOrder(teArray);
 	}
 }
