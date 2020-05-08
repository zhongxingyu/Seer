 /********************************/
 /* Lab: Grade to meaning    */
 /* Author: Evan Simmons         */
 /* CMP 12A/L, Fall 2011         */
 /* October 11th, 2011           */
 /*                              */
 /* See javadoc for more info    */
 /*                              */
 /* PLEASE HAVE A LOOK AT MY     */
 /* JUNIT TEST SUITE:            */
 /* GradesTests.java             */
 /********************************/
 
 import java.util.*;
 
 /**
  * Shows the meaning of a grade given.
  * can be calculated programatically via .gradeToMeaning
 
  @author Evan Simmons
  */
 
 public class Grades {
   static class InvalidGradeException extends Exception {
     public InvalidGradeException(String message) { super(message); }
   }
   /** Facility to get meaning interactively */
   public static void main(String[] args) {
     Scanner scan = new Scanner(System.in);
     System.out.print("Enter the grade letter: ");
 
     try {
       char grade = Character.toUpperCase( scan.next().charAt(0) );
       String meaning = gradeToMeaning(grade);
       System.out.printf("Grade letter %c means %s.\n", grade, meaning);
     }
     catch (InvalidGradeException e) {
       System.out.println( e.getMessage() );
     }
     finally { System.out.println("Bye."); }
   }
   
   /** Takes a grade of type char, and returns the meaning. */
   
   public static String gradeToMeaning(char grade)
   throws InvalidGradeException
   {
     grade = Character.toUpperCase(grade);
     switch (grade) {
       case 'A':
         return "Average";
       case 'B':
         return "Bad";
       case 'C':
         return "Catastrophe";
       case 'D':
         return "Disowned";
       case 'F':
         return "Forever Forgotten";
       default:
        throw new InvalidGradeException("The grade entered is not valid.");
     }
   }
 }
