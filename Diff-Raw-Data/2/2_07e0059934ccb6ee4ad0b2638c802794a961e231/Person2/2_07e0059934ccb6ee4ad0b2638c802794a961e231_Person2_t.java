 package edu.gatech.oad.antlab.person;
 
 /**
  *  A simple class for person 2
  *  returns their name and a
  *  modified string 
  *
  * @author Bob
  * @version 1.1
  */
 public class Person2 {
     /** Holds the persons real name */
     private String name;
 	 	/**
 	 * The constructor, takes in the persons
 	 * name
 	 * @param pname the person's real name
 	 */
 	 public Person2(String pname) {
 	   name = pname;
 	 }
 	/**
 	 * This method should take the string
 	 * input and return its characters in
 	 * random order.
 	 * given "gtg123b" it should return
 	 * something like "g3tb1g2".
 	 *
 	 * @param input the string to be modified
 	 * @return the modified string
 	 */
 	private String calc(String input) {
 	  //Person 2 put your implementation here
 		if (input.length() <= 1) {
 			return input;
 		}
 		int split = input.length()/2;
 		
 		String temp1 = calc(input.substring(0,split));
 		String temp2 = calc(input.substring(split));
 		
		if (Math.random() > 0.5) {
 			return temp1 + temp2;
 		}
 		else {
 			return temp2 + temp1;
 		}
 	}
 	/**
 	 * Return a string rep of this object
 	 * that varies with an input string
 	 *
 	 * @param input the varying string
 	 * @return the string representing the 
 	 *         object
 	 */
 	public String toString(String input) {
 	  return name + calc(input);
 	}
 }
