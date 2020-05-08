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
 				String userName = "vtda357_014"; // Your username goes here!
 				String password = "pxfpxf"; // Your password goes here!
 				Connection conn = DriverManager.getConnection(url,userName,password);
 
 				String student = args[0]; // This is the identifier for the student.
 				BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
 				System.out.println("Welcome!");
 				
 			
 				while(true) {
 					System.out.println("Please choose a mode of operation:");
 					System.out.print("? > ");
 					String mode = input.readLine();
 					if ((new String("information")).startsWith(mode.toLowerCase())) {
 						getInformation(conn, student);
 					} else if ((new String("register")).startsWith(mode.toLowerCase())) {
 						System.out.print("Register for what course? > ");
 						String course = input.readLine();
 						registerStudent(conn, student, course);
 					} else if ((new String("unregister")).startsWith(mode.toLowerCase())) {
 						System.out.print("Unregister from what course? > ");
 						String course = input.readLine();
 						unregisterStudent(conn, student, course);
 					} else if ((new String("quit")).startsWith(mode.toLowerCase())) {
 						System.out.println("Goodbye!");
 						break;
 					} else {
 						System.out.println("Unknown argument, please choose either information, register, unregister or quit!");
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
 		String query = "SELECT * FROM DBStudents WHERE persnumber = '" + student + "'";	
 		try {
 			Statement stmt = conn.createStatement();
 			ResultSet result = stmt.executeQuery(query);
 			
 			String print_results = "Information for student " + student + "\n----------------------------------\n";
 				
 			if(result.next())
 			{
 				print_results  	+= "Name: " + result.getString(2) + "\n";
 				print_results 	+= "Programme: " + result.getString(3) + "\n";
 				print_results 	+= "Branch: " + result.getString(4) + "\n";
 			}
 			
			query = "SELECT * FROM HasTaken WHERE persnumber = '" + student + "'";
 			stmt = conn.createStatement();
 			result = stmt.executeQuery(query);
 			
 			print_results 	+= "\nRead courses (name (code), credits: grade):\n";
 			
 			Statement course_stmt = conn.createStatement();
 			ResultSet course_result;
 			
 			while(result.next())
 			{
				String course_code = result.getString(2);
				String course_grade = result.getString(3);
 				String course_query = "SELECT * FROM Course WHERE code = '" + course_code + "'";
 				
 				course_result = course_stmt.executeQuery(course_query);
 				
 				if(course_result.next())
 				{
 					String course_name = course_result.getString(2);
 					String course_credits = course_result.getString(4);
 					print_results += "\t" + course_name + "(" + course_code + "), " + course_credits + ": " + course_grade + "\n";
 				}
 			}
 			
 			query = "SELECT * FROM DBStudentStatus WHERE persnumber = '" + student + "'";
 			
 			stmt = conn.createStatement();
 			result = stmt.executeQuery(query);
 			
 			print_results += "\nRegistered Courses (name (code), credits: status):\n";
 			course_stmt = conn.createStatement();
 			
 			while(result.next())
 			{
 				String course_code = result.getString(2);
 				String course_status = result.getString(3);
 				query = "SELECT * FROM Course WHERE code = '" + course_code + "'";
 				
 				course_result = stmt.executeQuery(query);
 				
 				if(course_result.next())
 				{
 					String course_name = course_result.getString(2);
 					String course_credits = course_result.getString(4);
 					print_results += "\t" + course_name + "(" + course_code + "), " + course_credits + ": " + course_status + "\n";
 				}
 			}
 			
 			query 	= "SELECT * FROM DBCanGraduate WHERE persnumber = '" + student + "'";
 			stmt 	= conn.createStatement();
 			result 	= stmt.executeQuery(query);
 			
 			print_results += "\n";
 			
 			if(result.next())
 			{
 				print_results += "Seminar courses taken: " + result.getString(7) + "\n";
 				print_results += "Math credits taken: " + result.getString(5) + "\n";
 				print_results += "Research credits taken: " + result.getString(6) + "\n";
 				print_results += "Total credits taken: " + result.getString(2) + "\n";
 				print_results += "Fulfills the requirements for graduation: " + result.getString(8) + "\n";
 			}
 			
 			print_results += "----------------------------------";
 			System.out.println(print_results);
 			
 
 		} catch (SQLException e) {
 			System.err.println(e);
 			System.exit(2);
 		}
 		
 				
 	}
 
 
 	static void registerStudent(Connection conn, String student, String course)
 	{
 		try {
 			Statement myStmt = conn.createStatement();
 			myStmt.executeUpdate("INSERT INTO DBStudentStatus VALUES ('" + student + "', '" + course + "', 'registered')");
 			
 			// Check the new status of the registration
 			ResultSet rs = myStmt.executeQuery("SELECT * FROM DBStudentStatus WHERE persnumber = '" + student + "' AND coursecode = '" + course + "'");
 			if (rs.next())
 			{
 				String register_result = rs.getString(3);
 				
 				// Get course name
 				rs = myStmt.executeQuery("SELECT name FROM Course WHERE code = '" + course + "'");
 				if (rs.next())
 				{
 					String full_coursename = rs.getString(1);
 					
 					if (register_result.equals("registered"))
 					{
 						System.out.println("You are now successfully registered to course " + course + " " + full_coursename + "!");
 					} else {
 						
 						// Get number on the waiting list
						rs = myStmt.executeQuery("SELECT COUNT(*) FROM DBStudentStatus WHERE persnumber = '" + student + "' AND coursecode = '" + course + "' AND status = 'waiting'");
 						rs.next();
						System.out.println("Course " + course + " " + full_coursename + " is full, you are put in the waiting list as number " + Integer.toString(rs.getInt(1)) + ".");
 					}
 				} else {
 					System.err.println("Failed to get full course name for course code '" + course + "'.");
 					System.exit(2);
 				}
 				
 			} else {
 				System.out.println("Error: Could not find user entry after registering.");
 				System.out.println("Check so that the user have taken all prerequisite courses!");
 			}
 
 		} catch (SQLException e) {
 			System.err.println(e);
 			System.exit(2);
 		}
 		
 	}
 
 	static void unregisterStudent(Connection conn, String student, String course)
 	{
 		try {
 			Statement myStmt = conn.createStatement();
 			
 			
 			// Check the new status of the registration
 			ResultSet rs = myStmt.executeQuery("SELECT status FROM DBStudentStatus WHERE persnumber = '" + student + "' AND coursecode = '" + course + "'");
 			if (rs.next())
 			{
 				String reg_status = rs.getString(1);
 				myStmt.executeUpdate("DELETE FROM DBStudentStatus WHERE persnumber = '" + student + "' AND coursecode = '" + course + "'");
 				
 				if (reg_status.equals("registered"))
 				{
 					System.out.println("You were unregistered from the course.");
 				} else {
 					System.out.println("You were removed from the waiting list for the course.");
 				}
 				
 			} else {
 				System.out.println("You are not registered/on the waiting list for that course!");
 			}
 			
 			/*
 			// Check the new status of the registration
 			ResultSet rs = myStmt.executeQuery("SELECT * FROM DBStudentStatus WHERE persnumber = '" + student + "' AND coursecode = '" + course + "'");
 			if (rs.next())
 			{
 				String register_result = rs.getString(3);
 				
 				// Get course name
 				rs = myStmt.executeQuery("SELECT name FROM Course WHERE code = '" + course + "'");
 				if (rs.next())
 				{
 					String full_coursename = rs.getString(1);
 					
 					if (register_result == "registered")
 					{
 						System.out.println("You are now successfully registered to course " + course + " " + full_coursename + "!");
 					} else {
 						
 						// Get number on the waiting list
 						rs = myStmt.executeQuery("SELECT COUNT(*) FROM DBStudentStatus WHERE persnumber = '" + student + "' AND coursecode = '" + course + "' AND status = 'waiting'");
 						rs.next();
 						System.out.println("Course " + course + " " + full_coursename + " is full, you are put in the waiting list as number " + rs.getString(1) + ".");
 					}
 				} else {
 					System.err.println("Failed to get full course name for course code '" + course + "'.");
 					System.exit(2);
 				}
 				
 			} else {
 				System.out.println("Error: Could not find user entry after registering.");
 				System.out.println("Check so that the user have taken all prerequisite courses!");
 			}*/
 
 		} catch (SQLException e) {
 			System.err.println(e);
 			System.exit(2);
 		}
 	}
 }
