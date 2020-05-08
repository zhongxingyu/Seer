 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 // JDBC stuff.
 // Reading user input.
 
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
				String url = "XXXX";
 				String userName = ""; // Your username goes here!
 				String password = ""; // Your password goes here!
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
 
 	static void getInformation(Connection conn, String student)
 	{
 		// Create statement and exit if it fails
 		Statement st = null;
 		try {
 			st = conn.createStatement();
 		} catch (SQLException e) {
 			System.out.println("SQL ERROR: Failed to create statement! Exiting.");
 			System.exit(1);
 		}
 
 		// Print name, programme and branch
 		System.out.println("Information for student " + student);
 		System.out.println("-------------------------------------");
 		try {
 			ResultSet set = st.executeQuery("SELECT name, studyProgramme, branch " +
 					"FROM StudentsFollowing SF " +
 					"WHERE studentId = " + student + " ");
 			
 			set.next();
 			
 			System.out.println("Name: " + set.getString(1));
 			System.out.println("Line: " + set.getString(2));
 			System.out.println("Branch: " + set.getString(3));
 		} catch (SQLException e) {
 			System.out.println("SQL ERROR: " + e.getMessage());
 			return;
 		}
 		
 		// Print Read courses
 		System.out.println();
 		System.out.println("Read courses (code, credits: grade):");		
 		try {
 			ResultSet set = st.executeQuery("SELECT course, credits, grade " +
 											"FROM FinishedCourses " +
 											"WHERE studentId = " + student);
 			while (set.next()) {
 				System.out.print("\t" + set.getString(1) + ", ");
 				System.out.println(set.getString(2) + ": " + set.getString(3));
 			}
 			
 		} catch (SQLException e) {
 			System.out.println("SQL ERROR: " + e.getMessage());
 			return;
 		}
 		
 		// Print registered courses
 		System.out.println("Registered courses (code, credits: status):");
 		try {
 			ResultSet set = st.executeQuery("SELECT R.course, credits, status, NULL " +
 											"FROM Registrations R, Courses " +
 											"WHERE R.studentId = " + student + " " +
 											"AND Courses.code = R.course " +
 											"AND status = 'Registered'" + 
 											"UNION " + 
 											"SELECT Q.course, credits, 'Waiting', position " +
 											"FROM CourseQueuePositions Q, Courses " + 
 											"WHERE Q.studentId = " + student + " " +
 											"AND Courses.code = Q.course");
 			while (set.next()) {
 				System.out.print("\t" + set.getString(1) + ", " + set.getString(2) + ": ");
 				if ("Waiting".equals(set.getString(3))) {
 					System.out.println("waiting as nr " + set.getString(4));
 				} else {
 					System.out.println(set.getString(3));					
 				}
 			}
 			
 		} catch (SQLException e) {
 			System.out.println("SQL ERROR: " + e.getMessage());
 			return;
 		}
 		
 		try {
 			ResultSet set = st.executeQuery("SELECT seminarCourses, mathematicalCredits, reserachCredits, achievedCredits, qualifiedForGraduation " +
 											"FROM PathToGraduation " +
 											"WHERE studentId = " + student);
 			set.next();
 			
 			System.out.println("Seminar courses taken: " + set.getString(1));
 			System.out.println("Math credits taken: " + set.getString(2));
 			System.out.println("Research credits taken: " + set.getString(3));
 			System.out.println("Total credits taken: " + set.getString(4));
 			System.out.println("Fulfills graduation requirements: " + set.getString(5));
 		} catch (SQLException e) {
 			System.out.println("SQL ERROR: " + e.getMessage());
 			return;			
 		}
 	}
 
 	static void registerStudent(Connection conn, String student, String course)
 	{
 		Statement st = null;
 		try {
 			st = conn.createStatement();
 		} catch (SQLException e) {
 			System.out.println("SQL ERROR: Failed to create statement! Exiting.");
 			System.exit(1);
 		}
		
		try {
			ResultSet set = st.executeQuery("SELECT status " +
											"FROM Registrations " +
											"WHERE studentId = " + student + " " +
											"AND course = '" + course + "'");
			
			if (set.next()) {
				System.out.print("Sorry, but you are already ");
				if ("Waiting".equals(set.getString(1))) {
					System.out.println("waiting in line for " + course);
				} else if ("Registered".equals(set.getString(1))) {
					System.out.println("registered on " + course);
				}
				return;
			}
		} catch (SQLException e) {
			System.out.println("SQL ERROR: " + e.getMessage());
			return;			
		}
 	
 		try {
 			st.executeUpdate("INSERT INTO Registrations (studentId, course) " +
 											"VALUES ('" + student + "', '" + course + "')");
 			ResultSet set = st.executeQuery("SELECT status " +
 											"FROM Registrations " +
 											"WHERE studentId = " + student + " " + 
 											"AND course = '" + course + "'");
 			set.next();
 			if ("Registered".equals(set.getString(1))) {
 				System.out.println("You are now successfully registered to course " + course + "!");
 			} else {
 				set = st.executeQuery("SELECT position " +
 										"FROM CourseQueuePositions " +
 										"WHERE studentId = " + student + " " +
 										"AND course = " + course);
 				set.next();
 				System.out.println("Course " + course + " is full, " +
 									"you are put in the waiting list as number " +
 									set.getString(1) + ".");
 			}
 		} catch (SQLException e) {
 			System.out.println("SQL ERROR: " + e.getMessage());
 			return;
 		}
 	}
 
 	static void unregisterStudent(Connection conn, String student, String course)
 	{
 		// Your implementation here
 	}
 }
