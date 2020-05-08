 package runs;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import student.Student;
 
 import database.DatabaseHandler;
 
 public class Run11SuperTestOfDatabaseBase {
 	private static ArrayList<String> courses;
 
 	public static void main(String[] args) {
 	
 		HashMap<Integer, String> classList = new HashMap<>();
 		courses = DatabaseHandler.databaseloadCourses();
 		
 		int i = 0; for (String course: courses){
 			classList.put(i++, course);
 		}
 		
 		ArrayList<Student> students = Run09Generator.generateStudents(20, classList);
 		
 		DatabaseHandler.databaseSave2(students);
 		ArrayList<Student> studentsDatabase;
 		studentsDatabase = DatabaseHandler.databaseloadStudents();
		int j = 1;
 	}
 }
