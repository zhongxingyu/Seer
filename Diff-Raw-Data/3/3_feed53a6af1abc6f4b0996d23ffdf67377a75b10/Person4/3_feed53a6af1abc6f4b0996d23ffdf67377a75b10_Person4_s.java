 package edu.gatech.oad.antlab.person;
 
 /**
  *  A simple class for person 4
  *  returns their name and a
  *  modified string 
  *  
  *  @author Jackson
  *  @version 1.1
  */
 public class Person4 {
   /** Holds the persons real name */
   private String name;
     /**
      * The constructor, takes in the persons
      * name
      * @param pname the person's real name
      */
   public Person4(String pname) {
     name = pname;
   }
     /**
      * This method should take the string
      * input and return its characters rotated
      * 1 position.
      * given "gtg123b" it should return
      * "tg123bg".
      *
      * @param input the string to be modified
      * @return the modified string
      */
   private String calc(String input) {
    return input.substring(2,input.length()) + input.substring(0,2);
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
 
