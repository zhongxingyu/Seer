 /* Copyright (C) 2012 Benjamin Lewis
  * 
  * Licensed under the GNU GPLv3
  */
 
 /* Grade.java, an example grading program. */
 
 /* Just some code to start from; we'll see how this can become far more
  * interesting.
  */
 
 public class Grade {
    public static void main (String[] args) {
	int midtermGrade, finalGrade;
	midtermGrade = 60;
	finalGrade = 70;
	courseGrade = (midtermGrade + finalGrade) / 200;
	if (courseGrade >= 50) {
	    System.out.println("Satisfactory");
	} else {
	    System.out.println("Unsatisfactory");
	}
     }
 }
