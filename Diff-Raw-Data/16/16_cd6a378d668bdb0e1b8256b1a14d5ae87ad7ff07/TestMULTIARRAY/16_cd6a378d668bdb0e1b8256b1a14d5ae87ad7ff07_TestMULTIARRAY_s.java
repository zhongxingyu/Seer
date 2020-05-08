 package com.foursquare.heapaudit.test;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 public class TestMULTIARRAY extends TestUtil {
 
     // Test allocations of multi-dimensional arrays.
 
     @Test public void MULTIARRAY_I() {
 
 	clear();
 
 	int[][] arrayI = new int[3][5];
 
 	assertTrue(expect("int",
 			  15,
 			  152));
 
 	assertTrue(empty());
 
     }
 
     @Test public void MULTIARRAY_X() {
 
         clear();
 
 	TestChild[][] arrayL = new TestChild[1][7];
 
 	assertTrue(expect("com.foursquare.heapaudit.test.TestChild",
 			  7,
 			  72));
 
 	assertTrue(empty());
 
     }
 
     // test allocating multi-dim arrays with not all dims specified
 
     @Test
     public void MULTIARRAY_I_EmptySecondDim() {
 
         clear();
 
         int[][] arrayI = new int[3][];
 
         assertTrue(expect("[I",
                           3,
                           32));
 
 	assertTrue(empty());
 
     }
 
     @Test
     public void MULTIARRAY_I_InitiallyEmptySecondDim() {
 
         clear();
 
         int[][] arrayI = new int[3][];
         arrayI[0] = new int[5];
         arrayI[1] = new int[5];
         arrayI[2] = new int[5];
 
         assertTrue(expect("[I",
                           3,
                           32));
 
         assertTrue(expect("int",
                           5,
                           40));
 
         assertTrue(expect("int",
                           5,
                           40));
 
         assertTrue(expect("int",
                           5,
                           40));
 
         assertFalse(expect("int",
                            5,
                            40));
 
         assertTrue(empty());
 
     }
 
     @Test
     public void MULTIARRAY_I_InitiallyEmptyThirdDim() {
 
         clear();
 
         int[][][] arrayI = new int[3][][];
         arrayI[0] = new int[5][];
         arrayI[1] = new int[5][];
         arrayI[2] = new int[5][];
 
         assertTrue(expect("[[I",
                           3,
                           32));
 
         assertTrue(expect("[I",
                           5,
                           40));
 
         assertTrue(expect("[I",
                           5,
                           40));
 
         assertTrue(expect("[I",
                           5,
                           40));
 
         assertFalse(expect("[I",
                            5,
                            40));
 
         assertTrue(empty());
 
     }
 
     @Test
     public void MULTIARRAY_I_EmptyThirdDim() {
 
         clear();
 
         int[][][] arrayI = new int[3][5][];
 
         assertTrue(expect("[I",
                           15,
                           72));
 
         assertTrue(empty());
 
         arrayI[0][0] = new int[5];
 
         assertTrue(expect("I",
                           5,
                           40));
 
         assertTrue(empty());
         
     }
 
     @Test
     public void MULTIARRAY_I_EmptyThirdAndFourthDim() {
 
         clear();
 
         int[][][][] arrayI = new int[3][5][][];
 
         assertTrue(expect("[[I",
                           15,
                           72));
 
         assertTrue(empty());
 
         arrayI[2][4] = new int[5][];
 
         assertTrue(expect("[I",
                           5,
                           40));
 
         assertTrue(empty());
 
         // replace last array with an int[3][]
         arrayI[2][4] = new int[3][];
 
         assertTrue(expect("[I",
                           3,
                           32));
 
         assertTrue(empty());
 
         // add a couple arrays
         arrayI[0][0] = new int[3][];
         arrayI[0][1] = new int[3][];
 
         assertTrue(expect("[I",
                           3,
                           32));
 
         assertTrue(expect("[I",
                           3,
                           32));
 
         assertTrue(empty());
 
     }
 
     @Test
     public void MULTIARRAY_X_EmptySecondDim() {
 
         clear();
 
         TestChild[][] arrayL = new TestChild[1][];
 
         assertTrue(expect("com.foursquare.heapaudit.test.TestChild",
                           1,
                           24));
 
         assertTrue(empty());
 
     }
 
     @Test
     public void MULTIARRAY_X_InitiallyEmptySecondDim() {
 
         clear();
 
         TestChild[][] arrayL = new TestChild[1][];
         arrayL[0] = new TestChild[7];
 
         assertTrue(expect("com.foursquare.heapaudit.test.TestChild",
                           1,
                           24));
 
         assertTrue(expect("com.foursquare.heapaudit.test.TestChild",
                           7,
                           48));
 
         assertTrue(empty());
 
     }
 
     @Test
     public void MULITMULTIARRAY_X_EmptySecondAndThirdDim() {
 
         clear();
 
         TestChild[][][] arrayL = new TestChild[1][][];
 
         assertTrue(expect("com.foursquare.heapaudit.test.TestChild",
                           1,
                           24));
 
         assertTrue(empty());
 
     }
 
     @Test
     public void MULITMULTIARRAY_X_EmptySecondAndInitiallyEmptyThirdDim() {
 
         clear();
 
         TestChild[][][] arrayL = new TestChild[1][][];
         arrayL[0] = new TestChild[7][];
 
         assertTrue(expect("com.foursquare.heapaudit.test.TestChild",
                           1,
                           24));
 
         assertTrue(expect("com.foursquare.heapaudit.test.TestChild",
                           7,
                           48));
 
         assertTrue(empty());
 
     }
 
     @Test
     public void MULITMULTIARRAY_X_EmptyThirdDim() {
 
         clear();
 
         TestChild[][][] arrayL = new TestChild[1][7][];
 
         assertTrue(expect("com.foursquare.heapaudit.test.TestChild",
                           7,
                           72));
 
         assertTrue(empty());
 
     }
 
     @Test
     public void MULTIARRAY_X_Two_EmptySecondDim() {
 
         clear();
 
         TestChild[][] arrayL = new TestChild[1][];
 
         assertTrue(expect("com.foursquare.heapaudit.test.TestChild",
                           1,
                           24));
 
         TestChild[][] arrayL_2 = new TestChild[1][];
 
         assertTrue(expect("com.foursquare.heapaudit.test.TestChild",
                           1,
                           24));
 
         assertTrue(empty());
 
     }
 
     @Test
     public void MULTIMULTIARRAY_C() {
 
         clear();
 
         char[][][][] arrayC = new char[10][10][][];
 
         assertTrue(expect("[[C",
                           100,
                           112));
 
         assertTrue(empty());
 
     }
 
 }
