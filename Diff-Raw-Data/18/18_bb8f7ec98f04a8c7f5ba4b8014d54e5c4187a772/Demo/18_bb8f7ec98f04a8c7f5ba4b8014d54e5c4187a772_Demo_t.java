 package client;
 
 public class Demo {
 	
 	public static int i;
	public static int System;
 	
 	public static void main(String[] args) {
 		//int i;
 		
 		i = 3 + 6;
 		
 		i = 10;
 		
 		i++;
 		
 		String abc;
 		
 		abc = "i = " + i;
 		
 		int j = test() + i;
 		
 		int x = 3;
 		
 		Test test = new Test();
		x = test.getTest();
 	}
 	
 	public static int test() {
 		int returnval = 2;
 		
 		return returnval;
 	}
 
 }
