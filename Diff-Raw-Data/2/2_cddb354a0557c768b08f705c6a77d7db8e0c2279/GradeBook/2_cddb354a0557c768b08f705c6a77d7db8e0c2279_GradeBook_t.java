 package GradeBook;
 
 import java.util.Scanner;
 /**
  * @author Dan Haag
  *
  */
 public class GradeBook 
 {
 	private String courseName;
 	
 	public GradeBook( String name )
 	{
 		courseName = name;
 	}
 	
 	public void setCourseName( String name )
 	{
 		courseName = name;
 	}
 	
 	public String getCourseName()
 	{
 		return courseName;
 	}
 	
 	public void displayMessage()
 	{
 		System.out.printf( "Welcome to the grade book for %s!\n", getCourseName() );
 	}
 	
 	public void determineClassAverage()
 	{
 		Scanner input = new Scanner( System.in );
 		
 		int total;
 		int gradeCounter;
 		int grade;
 		int average;
 		
 		total = 0;
 		gradeCounter = 1;
 		
 		while ( gradeCounter <= 10 )
 		{
 			System.out.print( "enter grade: " );
 			grade = input.nextInt();
 			total = total + grade;
 			gradeCounter = gradeCounter + 1;
 		}
 		
 		average = total/10;
 		
 		System.out.printf( "\nTotal of all 10 grades is %d\n", total );
	    System.out.printf( "Class average is %d\n", average );
 		
 		input.close();
 	}
 }
