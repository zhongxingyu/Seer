 package com.test;
 
 import com.Googlers;
 
 import junit.framework.TestCase;
 
 /**
  * @author antonio.diaz.arroyo@gmail.com
  * Test for Qualification Round of Code Jam 2012: Exercise B  
  */
 public class GooglersTest extends TestCase {
 
 	public void testCalculateTriplets1(){
 		String myInput = "3 1 5 15 13 11";
 		Googlers googlers = new Googlers(myInput);
 		assertEquals (3, googlers.calculateTripletsBestResult());
 	}
 	
 	public void testCalculateTriplets2(){
 		String myInput = "3 0 8 23 22 21";
 		Googlers googlers = new Googlers(myInput);
		assertEquals (3, googlers.calculateTripletsBestResult());
 	}
 	
 	public void testCalculateTriplets3(){
 		String myInput = "2 1 1 8 0";
 		Googlers googlers = new Googlers(myInput);
		assertEquals (3, googlers.calculateTripletsBestResult());
 	}
 	
 	public void testCalculateTriplets4(){
 		String myInput = "6 2 8 29 20 8 18 18 21";
 		Googlers googlers = new Googlers(myInput);
 		assertEquals (3, googlers.calculateTripletsBestResult());
 	}
 	
 }
