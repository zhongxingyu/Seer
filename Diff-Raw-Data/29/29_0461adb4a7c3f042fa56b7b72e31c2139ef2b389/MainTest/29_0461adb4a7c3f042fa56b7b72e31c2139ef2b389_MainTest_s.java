 package MainClasses;
 import java.util.ArrayList;
 
 import ArrayListTests.ArrayListAddTest;
 import ArrayListTests.ArrayListContainsTest;
 import ArrayListTests.ArrayListGetTest;
 import ArrayListTests.ArrayListIndexOfTest;
 import ArrayListTests.ArrayListRemoveTest;
 import ArrayListTests.ArrayListSizeTest;
 import HashMapTests.HashMapConstainsKeyTest;
 import HashMapTests.HashMapContainsValueTest;
 import HashMapTests.HashMapGetTest;
 import HashMapTests.HashMapPutTest;
 import HashMapTests.HashMapRemoveTest;
 import HashMapTests.HashMapSizeTest;
 import HashSetTests.HashSetAddTest;
 import HashSetTests.HashSetContainsTest;
 import HashSetTests.HashSetRemoveTest;
 import HashSetTests.HashSetSizeTest;
 import LinkedListTests.LinkedListAddtest;
 import LinkedListTests.LinkedListContainsTest;
 import LinkedListTests.LinkedListGetTest;
 import LinkedListTests.LinkedListIndexOfTest;
 import LinkedListTests.LinkedListRemoveTest;
 import LinkedListTests.LinkedListSizeTest;
 import StackTests.StackAddTest;
 import StackTests.StackContainsTest;
 import StackTests.StackGetTest;
 import StackTests.StackRemoveTest;
 import StackTests.StackSizeTest;
 
 
 /**
  * This class runs all the tests it knows
  *
  * @author Gandalf.
  *         Created 27.3.2013.
  */
 public class MainTest
 {
 	// ATTRIBUTES	-------------------------------------------------------
 	
 	private ArrayList<AbstractTest> tests;
 	private int startinstancecount;
 	private int maxiterates;
 	
 	
 	// CONSTRUCTOR	-------------------------------------------------------
 	
 	/**
 	 * Creates a new maintest
 	 * 
 	 * @param startinstancecount How many instances does a data structure have 
 	 * in the beginning of the test?
 	 * @param maxiterates How many a method is called in a single test?
 	 */
 	public MainTest(int startinstancecount, int maxiterates)
 	{
 		// Initializes the attributes
 		this.tests = new ArrayList<AbstractTest>();
 		this.startinstancecount = startinstancecount;
 		this.maxiterates = maxiterates;
 		
 		// Adds the necessary tests
 		// TODO: Add your tests here!
 		addTest(new HashSetContainsTest());
 		addTest(new HashSetAddTest());
 		addTest(new HashSetRemoveTest());
 		addTest(new HashSetSizeTest());
 		
 		addTest(new HashMapConstainsKeyTest());
 		addTest(new HashMapContainsValueTest());
 		addTest(new HashMapPutTest());
 		addTest(new HashMapRemoveTest());
 		addTest(new HashMapSizeTest());
 		addTest(new HashMapGetTest());
 		
 		addTest(new ArrayListAddTest());
 		addTest(new ArrayListRemoveTest());
 		addTest(new ArrayListGetTest());
 		addTest(new ArrayListContainsTest());
 		addTest(new ArrayListSizeTest());
 		addTest(new ArrayListIndexOfTest());
 		
 		addTest(new LinkedListAddtest());
 		addTest(new LinkedListContainsTest());
 		addTest(new LinkedListGetTest());
 		addTest(new LinkedListIndexOfTest());
 		addTest(new LinkedListRemoveTest());
 		addTest(new LinkedListSizeTest());
 		
 		addTest(new StackAddTest());
 		addTest(new StackContainsTest());
 		addTest(new StackGetTest());
 		addTest(new StackRemoveTest());
 		addTest(new StackSizeTest());

 	}
 	
 	
 	// OTHER METHODS	---------------------------------------------------
 	
 	// Adds a test to the list of tests ran
 	private void addTest(AbstractTest test)
 	{
 		this.tests.add(test);
 	}
 	
 	/**
 	 * Goes through all the tests and runs them. This method should only 
 	 * be called once for a single MainTest
 	 */
 	public void runTests()
 	{
 		for (AbstractTest test: this.tests)
 		{
 			test.runTest(this.startinstancecount, this.maxiterates);
 			System.out.println("--------------------------------------------");
 		}
 	}
 	
 	
 	// MAIN METHOD	-------------------------------------------------------
 	
 	/**
 	 * Runs all the tests in this class with the given arguments
 	 *
 	 * @param args There are two ways to give arguments:<br>
 	 * 1) Two arguments:<br>
 	 * - 1: How many instances does a data structure have in the beginning of a 
 	 * test<br>
 	 * - 2: How many times a tested method is called in a single test 
 	 * (no more than 25 is recommended)<br>
 	 * 2) One argument:<br>
 	 * - 1: How many times a tested method is called in a single test 
 	 * (no more than 25 is recommended)<p>
 	 * 
 	 * These arguments should be given in the long and / or integer formats
 	 */
 	public static void main(String[] args)
 	{
 		/*
 		System.out.println(args.length);
 		if (args.length == 3)
 		System.out.println(args[1] + " " + args[2]);
 		*/
 		
 		int startinstancecount = 1;
		int maxiterates = 20;
 		
 		// Updates the values with the arguments given with the 
 		// method call (if any)
 		if (args.length > 0)
 		{
 			int arg1 = 0;
 			
 			try
 			{
 				arg1 = Integer.parseInt(args[0]);
 			}
 			catch (NumberFormatException nfe)
 			{
 				System.err.println("Please give the first argument as an integer");
 				System.exit(-1);
 			}
 			
 			// Normally the arguments are startcount and maxiterations
 			if (args.length > 1)
 			{
 			int arg2 = 0;
 				
 				try
 				{
 					arg2 = Integer.parseInt(args[1]);
 				}
 				catch (NumberFormatException nfe)
 				{
 					System.err.println("Please give the second argument as " +
 							"an integer");
 					System.exit(-1);
 				}
 				
 				// Updates the values
 				startinstancecount = arg1;
 				maxiterates = arg2;
 			}
 			// When only one argument is present, it is considered maxiterations
 			else
 			{
 				maxiterates = arg1;
 			}
 		}
 		
 		// Starts the actual test
 		MainTest test = new MainTest(startinstancecount, maxiterates);
 		test.runTests();
 	}
 }
