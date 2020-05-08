 /*******************************************************************************
  * Copyright (c) 2012 Cesar Yeep.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the BSD 3-Clause License
  * ("New BSD" or "BSD Simplified") which accompanies this distribution,
  * and is available at
  * http://opensource.org/licenses/BSD-3-Clause
  * 
  * Contributors:
  *     Cesar Yeep - initial API and implementation
  ******************************************************************************/
 package tests;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 
 import testFramework.TestUtilities;
 
 /** This class contains test cases for type checking. 
  *	@author ceyeep
  */
 public class TypeCheckingTest {
 
 	private static TestUtilities testUtilities;
 	private static String fileName  = "TempTestFile";
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@BeforeClass
 	public static void setUpClass() throws Exception {
 		testUtilities = new TestUtilities(fileName);
 		testUtilities.createTempDirectory();
 	}
 	
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@AfterClass
 	public static void tearDownClass() throws Exception {
 		testUtilities.deleteTempDirectory();
 	}
 	
 	//Tests
     
 	/** Test simple integer assignment. */
 	@Test
 	public void testTypeChecking01() {
 		String setUp = " int x; ";
 		String testCase = " //@ [ x := 3] ";
 		assertEquals("",testUtilities.runChecker(setUp,testCase));
 		
 	}
 	
 	/** Test using keyword this. */
 	@Test
 	public void testTypeChecking02() {
 		String setUp = "int x;";
 		String testCase = " //@ [ this.x := 3]";
 		assertEquals("",testUtilities.runChecker(setUp,testCase));
 		
 	}
 	
 	/** Test using an undeclared variable. */
 	@Test
 	public void testTypeChecking03() {
 		String setUp = "int x;";
 		String testCase = " //@ [ y := 3]";
 		assertEquals("Semantic Error: no field named y is accessible",testUtilities.runChecker(setUp,testCase));
 		
 	}
 	
 	/** Test using different types. */
 	@Test
 	public void testTypeChecking04() {
 		String setUp = "int x;";
 		String testCase = " //@ [ x := \"hello\"]";
 		assertEquals("Semantic Error: cannot assign x of type int "
 			+"a value of type java.lang.String",testUtilities.runChecker(setUp,testCase));
 		
 	}
 	
 	/** Test multple assignment. */
 	@Test
 	public void testTypeChecking05() {
 		String setUp = "int x, y;";
 		String testCase = " //@ [ x, y := 1,2]";
 		assertEquals("",testUtilities.runChecker(setUp,testCase));
 		
 	}
 	
 	/** Test unbalanced concurrent assignment. */
 	@Test
 	public void testTypeChecking06() {
 		String setUp = "int x, y;";
 		String testCase = "//@ [ x, y := 3]";
 		assertEquals("Semantic Error: concurrent assignment is unbalanced",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test informal description 1. */
 	@Test
 	public void testTypeChecking07() {
 		String setUp = "boolean flag;";
 		String testCase = "//@ [ flag := (* informal description *) ]";
 		assertEquals("",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test informal description 2 (different type). */
 	@Test
 	public void testTypeChecking08() {
 		String setUp = "int x;";
 		String testCase = "//@ [ x := (* informal description *) ]";
 		assertEquals("",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	
 	/** Test variable duplicate. */
 	public void testTypeChecking09() {
 		String setUp = "int x;";
 		String testCase = "//@ [ x, x := 2, 3 ]";
 		assertEquals("Semantic Error: there are duplicates in the left-hand side of the equation: x",
 			testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test multiple duplicates. */
 	public void testTypeChecking10() {
 		String setUp = "String s1, s2;";
 		String testCase = "//# [ this.s1, s1, this.s2, s2 := \"s\", \"s\", \"s\", \"s\" ]";
 		assertEquals("Semantic Error: there are duplicates in the left-hand side of the equation: s1, s2",
 			testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test "anything" literal. */
 	@Test
 	public void testTypeChecking11() {
 		String setUp = "int x;";
 		String testCase = "//@ [ x := \\anything ]";
 		assertEquals("",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test "anything" literal with an object. */
 	@Test
 	public void testTypeChecking12() {
 		String setUp = "String s = \"\";";
 		String testCase = "//@ [ s := \\anything ]";
 		assertEquals("",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test conditional concurrent assignments. */
 	@Test
 	public void testTypeChecking13() {
 		String setUp = "int x = 2;";
 		String testCase = "//@ [ x > 0 -> x := 3 ]";
 		assertEquals("",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test conditional concurrent assignments (else statement). */
 	@Test
 	public void testTypeChecking14() {
 		String setUp = "int x = 2;";
 		String testCase = "//@ [ x > 0 -> x := 3 \\else x > 10 -> x := 20 ]";
 		assertEquals("",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test conditional concurrent assignments 
 	(multiple else statements and identity function). */
 	@Test
 	public void testTypeChecking15() {
 		String setUp = "int x = 2;";
 		String testCase = "//@ [ x > 0 -> x := 3 \\else x > 10 -> x := 20 \\else \\I]";
 		assertEquals("",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test a method with a return statement. */
 	@Test
 	public void testTypeChecking16() {
 		String testCase = "//@ [ \\result := 5 ]";
 		String code = "public int foo1(){return 5;}";
 		assertEquals("",testUtilities.runChecker(testCase,code));
 	}
 	
 	/** Test a method with a double \\result variable . */
 	@Test
 	public void testTypeChecking17() {
 		String testCase = "//@ [ \\result, \\result := 5, 4 ]";
 		String code = "public int foo1(){return 5;}";
 		assertEquals("Semantic Error: there are duplicates in the left-hand side of the equation: \\result",
 			testUtilities.runChecker(testCase,code));
 	}
 	
 	/** Test \\result variable with an invalid type. */
 	@Test
 	public void testTypeChecking18() {
 		String testCase = "//@ [ \\result := 5 ]";
 		String code = "public String foo1(){return \"hello\";}";
 		assertEquals("Semantic Error: cannot assign \\result of type java.lang.String a value of type int",testUtilities.runChecker(testCase,code));
 	}
 	
 	/** Test \\result variable in a constructor. */
 	@Test
 	public void testTypeChecking19() {
 		String testCase = "//@ [ \\result := 5 ]";
 		String code = "public " +fileName+ " (){}";
 		assertEquals("Semantic Error: incorrect use of \\result, \\result must be used within a method declaration",testUtilities.runChecker(testCase,code));
 	}
 	
 	/** Test concurrent assignment with referential semantics. */
 	@Test
 	public void testTypeChecking20() {
 		String setUp = "Object o1 = new Object(), o2 = new Object();";
 		String testCase = "//@ [ o1 @= 02 ]";
 		assertEquals("",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test incorrect use of simple concurrent assignment with referential semantics. */
 	@Test
 	public void testTypeChecking21() {
 		String setUp = "int x = 2;";
 		String testCase = "//@ [ x @= 3 ]";
 		assertEquals("Semantic Error: cannot use x of primitive int type using referential semantics",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test iterate operator. */
 	@Test
 	public void testTypeChecking22() {
 		String setUp = "int x = 2;\n"+
 					   "int[] myArray = new int[3];";
 		String testCase = "//@ [ x := myArray=>\\iterate(int a, int b = 0; a + b) ]";
 		assertEquals("",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test iterate operator incorrect caller type. */
 	@Test
 	public void testTypeChecking23() {
 		String setUp = "int x = 2;";
 		String testCase = "//@ [ x := x=>\\iterate(int a, int b = 0; a + b) ]";
 		assertEquals("Semantic Error: type int of caller expression is neither array type nor java.lang.Iterable",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test iterate operator with incorrect iterator variable type. */
 	@Test
 	public void testTypeChecking24() {
 		String setUp = "int x = 2;\n"+
 					   "String[] myArray = new String[3];";
 		String testCase = "//@ [ x := myArray=>\\iterate(int a, int b = 0; b) ]";
 		assertEquals("Semantic Error: iterator variable of type int cannot be assigned an element of type java.lang.String",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test iterate operator with duplicate parameter variable. */
 	@Test
 	public void testTypeChecking25() {
 		String setUp = "int x = 2;\n"+
 					   "int[] myArray = new int[3];";
 		String testCase = "//@ [ x := myArray=>\\iterate(int a, int a = 0; a) ]";
 		assertEquals("Semantic Error: duplicate declaration of parameter a",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test iterate operator E1 is of type T2. */
 	@Test
 	public void testTypeChecking26() {
 		String setUp = "int x = 2;\n"+
 					   "int[] myArray = new int[3];";
 		String testCase = "//@ [ x := myArray=>\\iterate(int a, int b = true; a) ]";
 		assertEquals("Semantic Error: E1 expression \"true\" in \\iterate operator is incompatible with type int",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test iterate operator E2 is of type T2. */
 	@Test
 	public void testTypeChecking27() {
 		String setUp = "int x = 2;\n"+
 					   "int[] myArray = new int[3];";
 		String testCase = "//@ [ x := myArray=>\\iterate(int a, int b = 0; true) ]";
 		assertEquals("Semantic Error: E2 expression \"true\" in \\iterate operator must be of type int",testUtilities.runChecker(setUp,testCase));
 	}
 	
	/** Test splitted definition with value semantics. */
 	@Test
 	public void testTypeChecking28() {
 		String setUp = "int x, y, z;";
 		String testCase = "//@ [ x := 1 \\, y := 1 \\, z := 1 ]";
 		assertEquals("",testUtilities.runChecker(setUp,testCase));
 	}
 	
	/** Test splitted definition with referential semantics. */
 	@Test
 	public void testTypeChecking29() {
 		String setUp = "String a = \"hi\", b, c;";
 		String testCase = "//@ [ b @= a \\, c @= a ]";
 		assertEquals("",testUtilities.runChecker(setUp,testCase));
 	}
 	
	/** Test splitted definition with different semantics. */
 	@Test
 	public void testTypeChecking30() {
 		String setUp = "String a, b;";
 		String testCase = "//@ [ b := \"hello\" \\, a @= b ]";
 		assertEquals("Semantic Error: incorrect use of splitting definitions; you must use the same type of equality tests (e.g. referential semantics or value semantics)",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test any operator. */
 	@Test
 	public void testTypeChecking31() {
 		String setUp =  "String s1; \n" +
 						"String[] myArray = new String[]{\"1\",\"2\"};";
 		String testCase = "//@ [ s1 := myArray=>\\any(String a; a.length() > 0) ]";
 		assertEquals("",testUtilities.runChecker(setUp,testCase));
 		
 		testCase = "//@ [ s1 := myArray=>\\any(String a; a) ]";
 		assertEquals("Semantic Error: B2 expression must be a boolean expression",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test collect operator. */
 	@Test
 	public void testTypeChecking32() {
 		String setUp =  "String s1; \n" +
 						"AST.CJCollection<String> collection = new AST.CJSet<String>(); \n" +
 						"String[] myArray = new String[]{\"1\",\"2\"};";
 		String testCase = "//@ [ collection := myArray=>\\collect(String a; a.length() > 0) ]";
 		assertEquals("",testUtilities.runChecker(setUp,testCase));
 		
 		testCase = "//@ [ s1 := myArray=>\\collect(String a; a.length() > 0) ]";
 		assertEquals("Semantic Error: cannot assign s1 of type java.lang.String a value of type AST.CJCollection<java.lang.String>",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test exists operator. */
 	@Test
 	public void testTypeChecking33() {
 		String setUp =  "String s1; \n" +
 					    "boolean b; \n" +
 						"String[] myArray = new String[]{\"1\",\"2\"};";
 		String testCase = "//@ [ s1 := myArray=>\\exists(String a; a.length() > 0) ]";
 		assertEquals("Semantic Error: cannot assign s1 of type java.lang.String a value of type boolean",testUtilities.runChecker(setUp,testCase));
 	
 		testCase = "//@ [ b := myArray=>\\exists(String a; a) ]";
 		assertEquals("Semantic Error: B2 expression must be a boolean expression",testUtilities.runChecker(setUp,testCase));
 	}
 
 	/** Test for all operator. */
 	@Test
 	public void testTypeChecking34() {
 		String setUp = "String s1; \n" +
 					   "boolean b; \n" +
 					   "String[] myArray = new String[]{\"1\",\"2\"};";
 		String testCase = "//@ [ s1 := myArray=>\\forAll(String a; a.length() > 0) ]";
 		assertEquals("Semantic Error: cannot assign s1 of type java.lang.String a value of type boolean",testUtilities.runChecker(setUp,testCase));
 	
 		testCase = "//@ [ b := myArray=>\\forAll(String a; a) ]";
 		assertEquals("Semantic Error: B2 expression must be a boolean expression",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test isUnique operator. */
 	@Test
 	public void testTypeChecking35() {
 		String setUp = "String s1; \n" +
 					   "String[] myArray = new String[]{\"1\",\"2\"};";
 		String testCase = "//@ [ s1 := myArray=>\\isUnique(String a; a.length() > 0) ]";
 		assertEquals("Semantic Error: cannot assign s1 of type java.lang.String a value of type boolean",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test one operator. */
 	@Test
 	public void testTypeChecking36() {
 		String setUp = "String s1; \n" +
 					   "boolean b; \n" +
 					   "String[] myArray = new String[]{\"1\",\"2\"};";
 		String testCase = "//@ [ s1 := myArray=>\\one(String a; a.length() > 0) ]";
 		assertEquals("Semantic Error: cannot assign s1 of type java.lang.String a value of type boolean",testUtilities.runChecker(setUp,testCase));
 	
 		testCase = "//@ [ b := myArray=>\\one(String a; a) ]";
 		assertEquals("Semantic Error: B2 expression must be a boolean expression",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test reject operator. */
 	@Test
 	public void testTypeChecking37() {
 		String setUp =  "String s1; \n" +
 						"AST.CJCollection<String> collection = new AST.CJSet<String>(); \n" +
 						"String[] myArray = new String[]{\"1\",\"2\"};";
 		String testCase = "//@ [ collection := myArray=>\\reject(String a; a.length() > 0) ]";
 		assertEquals("",testUtilities.runChecker(setUp,testCase));
 		
 		testCase = "//@ [ collection := myArray=>\\reject(String a; a) ]";
 		assertEquals("Semantic Error: B2 expression must be a boolean expression",testUtilities.runChecker(setUp,testCase));
 		
 		testCase = "//@ [ s1 := myArray=>\\reject(String a; a.length() > 0) ]";
 		assertEquals("Semantic Error: cannot assign s1 of type java.lang.String a value of type AST.CJCollection<java.lang.String>",testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test select operator. */
 	@Test
 	public void testTypeChecking38() {
 		String setUp =  "String s1; \n" +
 						"AST.CJCollection<String> collection = new AST.CJSet<String>(); \n" +
 						"String[] myArray = new String[]{\"1\",\"2\"};";
 		String testCase = "//@ [ collection := myArray=>\\select(String a; a.length() > 0) ]";
 		assertEquals("",testUtilities.runChecker(setUp,testCase));
 	
 		testCase = "//@ [ collection := myArray=>\\select(String a; a) ]";
 		assertEquals("Semantic Error: B2 expression must be a boolean expression",testUtilities.runChecker(setUp,testCase));
 		
 		testCase = "//@ [ s1 := myArray=>\\select(String a; a.length() > 0) ]";
 		assertEquals("Semantic Error: cannot assign s1 of type java.lang.String a value of type AST.CJCollection<java.lang.String>",testUtilities.runChecker(setUp,testCase));
 	}
 	
 }
