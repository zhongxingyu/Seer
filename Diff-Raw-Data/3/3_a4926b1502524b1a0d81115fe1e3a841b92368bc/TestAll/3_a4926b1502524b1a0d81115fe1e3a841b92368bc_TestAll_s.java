 
 package edu.common.dynamicextensions;
 
 /**
  * 
  */
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import edu.common.dynamicextensions.categoryManager.TestCategoryManager;
 import edu.common.dynamicextensions.entitymanager.TestEntityManager;
 import edu.common.dynamicextensions.entitymanager.TestEntityManagerForAssociations;
 import edu.common.dynamicextensions.entitymanager.TestEntityManagerForInheritance;
 import edu.common.dynamicextensions.entitymanager.TestEntityManagerWithPrimaryKey;
 import edu.common.dynamicextensions.entitymanager.TestEntityMangerForXMIImportExport;
 
 /**
  * Test Suite for testing all DE  related classes.
  */
 public class TestAll
 {
 
 	/**
 	 * @param args arg
 	 */
 	public static void main(String[] args)
 	{
 		junit.swingui.TestRunner.run(TestAll.class);
 	}
 
 	/**
 	 * @return test suite
 	 */
 	public static Test suite()
 	{
 		TestSuite suite = new TestSuite("Test suite for Query Interface Classes");
 		suite.addTestSuite(TestEntityManager.class);
 		suite.addTestSuite(TestEntityManagerForAssociations.class);
 		suite.addTestSuite(TestEntityManagerForInheritance.class);
 		suite.addTestSuite(TestEntityMangerForXMIImportExport.class);
 		suite.addTestSuite(TestCategoryManager.class);
 		suite.addTestSuite(TestEntityManagerWithPrimaryKey.class);
 		return suite;
 	}
 }
