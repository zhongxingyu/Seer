 package database;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import constraint.Constraint;
 import constraint.MustHaveCourse;
 import constraint.PreferedGeneralCourseTime;
 
 import runs.Run09Generator;
 import student.Student;
 
 public class DatabaseHandler {
     public static void main(String[] args) {
         HashMap<Integer,String> courses = new HashMap<>(Run09Generator.generateClassList(10));
         databaseSave(new ArrayList<>(courses.values()));
         databaseSave2(new ArrayList<>(Run09Generator.generateStudents(10, courses)));
     }
     
    private static void databaseSave(ArrayList<String> courses){
         try {
             Class.forName("org.sqlite.JDBC");
         } catch (ClassNotFoundException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         }
         
         Connection connection = null;
         try {        
             connection = DriverManager.getConnection("jdbc:sqlite:database.db");
             
             Statement statement = connection.createStatement();
 
             statement.setQueryTimeout(20);
             
             statement.executeUpdate("drop table if exists courselist");
             statement.executeUpdate("create table courselist (name string)");
             
             for (String course: courses){
                 statement.executeUpdate("insert into courselist values('"+ course +"')");
             }
             
             ResultSet rs = statement.executeQuery("select * from courselist");
             
             while (rs.next()){
                 System.out.println(rs.getString(1));
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
     }
     
     public static void databaseSave2(ArrayList<Student> students){
         //students = Run09Generator.generateStudents(20, classList);
         
         try {
             Class.forName("org.sqlite.JDBC");
         } catch (ClassNotFoundException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         }
         
         Connection connection = null;
         try {        
             connection = DriverManager.getConnection("jdbc:sqlite:database.db");
             
             Statement statement = connection.createStatement();
             statement.setQueryTimeout(20);
             
             statement.executeUpdate("drop table if exists studentlist");
             statement.executeUpdate("drop table if exists takingclasses");
             statement.executeUpdate("create table studentlist (name string, prefStartTimeHour int, prefStartTimeMin int, prefEndTimeHour int, prefEndTimeMinute int)");
             statement.executeUpdate("create table takingclasses (student string, class string)");
             
             for (Student student: students){
                 PreferedGeneralCourseTime cons = null;
                 ArrayList<String> coursesTaken = new ArrayList<>();
                 
                 for (Constraint constraint: student.getConstraints()){
                     if (constraint instanceof PreferedGeneralCourseTime){
                         cons = (PreferedGeneralCourseTime) constraint; break;
                     }else if (constraint instanceof MustHaveCourse){
                         coursesTaken.add(((MustHaveCourse)constraint).getCourseName());
                     }
                 }
                 
                 if (cons != null)//fill studentlist table
                     statement.executeUpdate("insert into studentlist values('"+ student.getUsername() +"','"+ cons.getPrefBegin().getHourOfDay() +"','"+ cons.getPrefBegin().getMinuteOfHour() +"','"+ cons.getPrefEnd().getHourOfDay() +"','"+ cons.getPrefEnd().getMinuteOfHour() +"')");
                 else
                     statement.executeUpdate("insert into studentlist values('"+ student.getUsername() +"','"+ "" +",'"+ "" +",'"+ "" +",'"+ "" +"')");
                 
                 for (String course: coursesTaken){    //fill takingclasses table
                     statement.executeUpdate("insert into takingclasses values('"+ student.getUsername() +"','"+ course +"')");
                 }
             }
             
             ResultSet rs = statement.executeQuery("select * from studentlist");
             
             while (rs.next()){
                 System.out.println(rs.getString(1)+" added!");//this is not meant for students
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
     }
     
 	public static ArrayList<String> databaseloadCourses(){
 		try {
 			Class.forName("org.sqlite.JDBC");
 		} catch (ClassNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		Connection connection = null;
 		try {		
 			connection = DriverManager.getConnection("jdbc:sqlite:database.db");
 			
 			Statement statement = connection.createStatement();
 
 			statement.setQueryTimeout(20);
 			
 			ResultSet rs = statement.executeQuery("select name from courselist");
 			
 			ArrayList<String> courses = new ArrayList<>();
 			
 			while (rs.next()){
 				courses.add(rs.getString(1));
 			}
 			
 			return courses;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	
 		return new ArrayList<>();
 	}
 	
 	public static ArrayList<Student> databaseloadStudents(){
 		try {
 			Class.forName("org.sqlite.JDBC");
 		} catch (ClassNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}		
 		Connection connection = null;
		
 		try {		
 			connection = DriverManager.getConnection("jdbc:sqlite:database.db");
 			Statement statement = connection.createStatement();
 			statement.setQueryTimeout(20);
 			Statement statement2 = connection.createStatement();
 			statement2.setQueryTimeout(20);
 			ResultSet rs = statement.executeQuery("select name, prefStartTimeHour, prefStartTimeMin, prefEndTimeHour, prefEndTimeMinute from studentlist");
 			ResultSet rs2 = statement2.executeQuery("select student, class from takingclasses");
 			ArrayList<Student> students = new ArrayList<>();
 			while (rs.next()){
 				Student temp = new Student();
 				ArrayList<String> classes = new ArrayList<>();
				while (rs2.next()){
					if (rs2.getString(1).equals(rs.getString(1))){
						classes.add(rs2.getString(2));
						temp.addConstraint(new MustHaveCourse(rs2.getString(2)));
 					}
 				}
 				temp.setListOfCourses(classes);
 				temp.addConstraint(new PreferedGeneralCourseTime(rs.getInt(2),rs.getInt(3),rs.getInt(4),rs.getInt(5)));
 				students.add(temp);
 			}
 			return students;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return new ArrayList<>();
 	}
 }
