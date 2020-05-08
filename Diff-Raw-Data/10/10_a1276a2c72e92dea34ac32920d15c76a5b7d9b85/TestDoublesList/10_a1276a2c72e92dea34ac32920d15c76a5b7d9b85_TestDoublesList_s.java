 package tests;
 
 import doublesLibrary.*;
 
 public class TestDoublesList {
 
 	/* Test simple insertion. Owner: Giorgos */
 	static void testInsertion() {
 		DoublesList list = new DoublesList();
 		list.insert(0, 0.1);
 		list.insert(1, 0.2);
 
 		double[] array = list.toArray();
 		assert array.length == 2 && array[0] == 0.1 && array[1] == 0.2;
 	
 		list.insert(0, 0.5);
 		array = list.toArray();
 		assert array.length == 3 && array[0] == 0.5 && array[1] == 0.1 && array[2] == 0.2;
 	}
 
	/* It attempt to insert at invalid positions, so it should throw exceptions. Owner: Giorgos */
 	static void testFaultyInsertion() {
 		DoublesList list = new DoublesList();
 		try {
 			list.insert(-1, 0.1); // should throw Exception
 			assert false;
 		}
 		catch(IllegalArgumentException e) {
 		}
 		
 		try {
 			list.insert(0, 0.5);
 			list.insert(1, 0.6);
 			list.insert(5, 0.7); // should throw Exception
 			assert false;
 		}
 		catch(IllegalArgumentException e) {
 		}
 	}
 
 	/* Test a lot of insertions. Owner: Giorgos */
 	static void testManyInsertions() {
 		DoublesList list = new DoublesList();
 
 		for(int i = 0; i <= 200; i++)
 			list.insert(0, i+0.5);
 
 		double[] array = list.toArray();
 		for(int i = 0; i <= 200; i++)
 			assert array[i] == (200-i)+0.5;
 
 	}
 
 	/* Test add value after index i. Owner: bitzesmichail */
 	static void testaddNumAfter() {
 		DoublesList list = new DoublesList();
 
 		for(int i = 0; i <= 200; i++)
 			list.insert(i, i);
 
 		list.addNumAfter(100, 453.0);
 
 		double[] array = list.toArray();
 		for(int i = 0; i <= 99; i++) {
 			assert array[i] == i;
 		}
 
 		for(int i = 101; i <= 200; i++) {
 			assert array[i] == i + 453.0;
 		}
 	}
 
 	/* It attempts to add at invalid positions, so it should throw exceptions. Owner: bitzesmichail */
 	static void testFaultyIndexInaddNumAfter() {
 		DoublesList list = new DoublesList();
 		try {
 			list.addNumAfter(-1, 453.0); // should throw Exception
 			assert false;
 		}
 		catch(IllegalArgumentException e) {
 		}
 		
 		try {
 			list.insert(0, 0.1);
 			list.insert(1, 0.2);
 			list.addNumAfter(5, 0.3); // should throw Exception
 			assert false;
 		}
 		catch(IllegalArgumentException e) {
 		}
 	}
 
 	/* Test subtract number after index. Owner: bitzesmichail */
 	static void testsubtractNumAfter() {
 		DoublesList list = new DoublesList();
 
 		for(int i = 0; i <= 200; i++)
 			list.insert(i, i);
 
 		for(int i = 200; i >= 1; i--)
 			list.subtractNumAfter(i-1, i);
 
 		double[] array = list.toArray();
 		assert array.length == 1;
 		assert array[0] == 0.0;
 	}
 
 	/* It attempts to insert at invalid positions, so it should throw exceptions. Owner: bitzesmichail */
 	static void testFaultyIndexInsubtractNumAfter() {
 		DoublesList list = new DoublesList();
 		try {
 			list.subtractNumAfter(-1, 453.0); // should throw Exception
 			assert false;
 		}
 		catch(IllegalArgumentException e) {
 		}
 		
 		try {
 			list.insert(0, 0.1);
 			list.insert(1, 0.2);
 			list.subtractNumAfter(5, 0.3); // should throw Exception
 			assert false;
 		}
 		catch(IllegalArgumentException e) {
 		}
 	}
 
 	/* Runs all tests */
 	public static void main(String[] args) {
 		testInsertion();
 		testFaultyInsertion();
 		testManyInsertions();
 		testaddNumAfter();
 		testFaultyIndexInaddNumAfter();
 		testsubtractNumAfter();
 		testFaultyIndexInsubtractNumAfter();
 		
 		System.out.println("All tests run OK.");
 	}
 
 
 }
 
 
  
