 package server;
 
 
 public class GraderTest {
 
 	/**
 	 * @author erensezener
 	 * 
 	 * This is a class to test Grader class.
 	 */
 	
	public static void main(String[] args) {
 		Student myStudent = new Student();
 		myStudent.setHomeworkOutput("6 8");
		myStudent.grade(HW1);
 		System.out.println(myStudent.getGrade());
 
	}
	
	private static final int HW1=1;
 
 }
