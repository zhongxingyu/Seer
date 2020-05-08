 import java.sql.*; // JDBC stuff.
 import java.io.*;  // Reading user input.
 
 public class StudentPortal
 {
 	/* This is the driving engine of the program. It parses the
 	 * command-line arguments and calls the appropriate methods in
 	 * the other classes.
 	 *
 	 * You should edit this file in two ways:
 	 * 	1) 	Insert your database username and password (no @medic1!)
 	 *		in the proper places.
 	 *	2)	Implement the three functions getInformation, registerStudent
 	 *		and unregisterStudent.
 	 */
 	public static void main(String[] args)
 	{
 		if (args.length == 1) {
 			try {
 				DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
 				String url = "jdbc:oracle:thin:@tycho.ita.chalmers.se:1521/kingu.ita.chalmers.se";
 				String userName = "vtda357_023"; // Your username goes here!
 				String password = "anton"; // Your password goes here!
 				Connection conn = DriverManager.getConnection(url,userName,password);
 
 				String student = args[0]; // This is the identifier for the student.
 				BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
 				System.out.println("Welcome!");
 				while(true) {
 					System.out.println("Please choose a mode of operation:");
 					System.out.print("? > ");
 					String mode = input.readLine();
 					if ((new String("information")).startsWith(mode.toLowerCase())) {
 						/* Information mode */
 						getInformation(conn, student);
 					} else if ((new String("register")).startsWith(mode.toLowerCase())) {
 						/* Register student mode */
 						System.out.print("Register for what course? > ");
 						String course = input.readLine();
 						registerStudent(conn, student, course);
 					} else if ((new String("unregister")).startsWith(mode.toLowerCase())) {
 						/* Unregister student mode */
 						System.out.print("Unregister from what course? > ");
 						String course = input.readLine();
 						unregisterStudent(conn, student, course);
 					} else if ((new String("quit")).startsWith(mode.toLowerCase())) {
 						System.out.println("Goodbye!");
 						break;
 					} else {
 						System.out.println("Unknown argument, please choose either " +
 									 "information, register, unregister or quit!");
 						continue;
 					}
 				}
 				conn.close();
 			} catch (SQLException e) {
 				System.err.println(e);
 				System.exit(2);
 			} catch (IOException e) {
 				System.err.println(e);
 				System.exit(2);
 			}
 		} else {
 			System.err.println("Wrong number of arguments");
 			System.exit(3);
 		}
 	}
 
 	static void getInformation(Connection conn, String student) throws SQLException
 	{
 		//Name and program
 		Statement namePrgrmStmt = conn.createStatement();
 		ResultSet rs = namePrgrmStmt.executeQuery("SELECT name, program FROM STUDENTS WHERE id= '" + student + "'");
 		rs.next();
 		System.out.println("Name: " + rs.getString(1));
 		System.out.println("Line: " + rs.getString(2));
 		//Branch
 		Statement branchStmt = conn.createStatement();
 		ResultSet rs2 = branchStmt.executeQuery("SELECT branch FROM ORIENTATIONS WHERE student= '" + student + "'");
 		rs2.next();
 		System.out.println("Branch: " + rs2.getString(1));
 		
 		//Read Courses
 		Statement readStmt = conn.createStatement();
 		ResultSet rs3 = readStmt.executeQuery("SELECT name, completedCourse, Courses.credit, grade " +
 											"FROM Courses, FinishedCourses " +
 											"WHERE code=completedCourse AND studentID = '" + student + "'");
 		while(rs3.next()){
			System.out.println("Read courses (name (code), credits: grade):\n" + rs3.getString(1) + " (" + 
 								rs3.getString(2) + "), " + rs3.getInt(3) + "p: " + rs3.getInt(4));
 		}
 		
 		//Registered Courses
 		Statement regStmt = conn.createStatement();
		ResultSet rs4 = regStmt.executeQuery("");
		
 	}
 
 	static void registerStudent(Connection conn, String student, String course)
 	{
 		// Your implementation here
 	}
 
 	static void unregisterStudent(Connection conn, String student, String course)
 	{
 		// Your implementation here
 	}
 }
