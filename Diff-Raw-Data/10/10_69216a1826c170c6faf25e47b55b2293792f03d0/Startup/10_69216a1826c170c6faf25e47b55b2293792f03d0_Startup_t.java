 package lab1;
 
 /**
  *
  * @author Mark Urbanski
  */
 public class Startup {
     //I decided to make my Course as abstract as possible.  It means that I have
     //a lot of code duplication at this point, but it also means that adding
     //another course type like FirstSociologyCourse is simple.  But it also means
     //that string and credit validation are up to the programmer of the 
     //individual course, and are not enforced throughout.  If my Startup method
     //allowed data input, perhaps that is where the validation should take place.
     
     //The main advantage to declaring a variable by its abstract type is that 
     //we could make an array of them, and we could treat them all the same way.
     //In this case, I would treat everything as a course, and all reporting 
     //can be done based on the course methods.  The negative to this approach 
     //is that we ignore any special public functions that are provided as part
     //of the child class.  AdvancedJavaCourse has a getCapitalizedCourseName() 
     //function that could be nice, but it doesn't exist anywhere else.  If we
     //needed it for this Startup method, we could chose to put it there.
     
     public static void main(String[] args){
     
         //Make one of each course
         Course course1 = new IntroToProgrammingCourse("Programming - A First Look",
             "152-101");
         course1.setCredits(3.0);
         Course course2 = new IntroJavaCourse("Java From Scratch", "152-130");
         course2.setCredits(3.5);
         course2.setPrerequisites("152-101");
         Course course3 = new AdvancedJavaCourse("Java - The Hard Stuff", "152-182");
         course3.setCredits(4.0);
        course3.setPrerequisites("152-130");
         
         //Output the courses
         outputCourse(course1);
         outputCourse(course2);
         outputCourse(course3);
     }
         private static void outputCourse(Course c){
             System.out.println("Course Name: " + c.getCourseName());
             System.out.println("Course Number: " + c.getCourseNumber());
             System.out.println("Course Credits: " + c.getCredits());
             System.out.println("Course Prerequisites: " + c.getPrerequisites());
             System.out.println();
         }    
 
 }
