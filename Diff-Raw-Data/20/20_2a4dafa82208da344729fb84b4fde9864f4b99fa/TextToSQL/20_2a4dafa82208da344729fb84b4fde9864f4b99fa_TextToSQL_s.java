 package loaders;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public class TextToSQL {
 
 	/**
 	 * @author AV
 	 * @param args
 	 * args[0] = text file to parse and upload ex  uploadThis.txt
 	 * args[1] = database username
 	 * args[2] = database password
 	 * args[3] = database connection string ex. "jdbc:postgresql://192.168.1.101:5432/UV"
 	 *  
 	 *  This loader assumes the sql schema is already established in the given database.
 	 *  Note: you may want to disable indexes before running this.
 	 * 
 	 */
 	public static void main(String[] args) {
 
 		System.out.println("Entering Main");
 		
 		if(args.length != 4) {
 			System.out.println("Expected 4 args but only received " + args.length);
 			System.exit(1);
 		}
 
 		String filePath = args[0];
 		System.out.println("text file path: " + filePath);
 		String username = args[1];
 		String password = args[2];
 		String connString = args[3];
 		System.out.println("username: " + username + " password: " + password + " DB Connection String: " + connString);
 		Connection connection = getDatabaseConnection(username, password, connString);		
 		System.out.println("Database Connection Established.");
 		
 		try {
 			
 	        long startTime = System.currentTimeMillis();
 	        
 	        getAndInsertLinesOfFile(filePath, connection);
 	        
 			//insert rows for people, facilities, events, and relationships
 			
 			long endTime = System.currentTimeMillis();
 			System.out.println("Total time in seconds: " + (endTime - startTime)/1000);
 			
         } catch(Exception e) {
         	e.printStackTrace();
         } finally {
 			try {
 				connection.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
         }
 	}
 	
 	private static void getAndInsertLinesOfFile(String filePath, Connection connection) {
 		FileReader fileReader = null;
 		BufferedReader buffReader = null;
 
 		try {			
 			File textFile = new File(filePath);
 			fileReader = new FileReader(textFile);
 			buffReader = new BufferedReader(fileReader);
 			//read the first line
 			String line = buffReader.readLine();
 			
 			//while the read line is not null, add it to the list
 			while(line != null) {
 				processLine(line, connection);
 				
 				line = buffReader.readLine();
 			}	
 		} catch (Exception e) {
 				e.printStackTrace();
 		} finally {
 			try {
 				buffReader.close();
 				fileReader.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}		
 	}
 	
 	private static void processLine(String line, Connection connection) {		 
 		 String[] values = line.split("\t"); 
 		 String university = values[0];
 		 String prof = values[1];
 		 String overallProfQual = values[2];
 		 String goodPoorAvgQual = values[3];
 		 String depart = values[4];
 		 String courseNum = values[5];
 		 String easiness = values[6];
 		 String helpfulness = values[7];
 		 String clarity = values[8];
 		 String raterInterest = values[9];
 		 String rootJSPTid = values[10];
 		 String timestamp = values[11];
 		 String comment = values[12];
 		 
 		 
 		 String univ_id = insertUniversity(university, connection);
 		 String depart_id = insertDepartment(depart, univ_id, connection);
 		 String prof_id = insertProfessor(prof, overallProfQual, rootJSPTid,  connection);
 		 String course_id = insertCourse(courseNum, depart_id, connection);
 		 if(course_id.equalsIgnoreCase("0")) {
 			 return;
 		 }
 		 insertComment(easiness, helpfulness, raterInterest, goodPoorAvgQual, clarity, comment, timestamp, course_id, prof_id, connection);
 		 insertProfessorCourseXRef(prof_id, course_id, connection);
 		 insertProfessorDepartmentXRef(prof_id, depart_id, connection);
 	}
 
 	
 	//The following methods insert the given entity to the database if it does not exist yet, else it does nothing - 
 	//Either way, they return the id of the existing or new entity in the database
 	private static void insertProfessorDepartmentXRef(String prof_id, String depart_id, Connection connection) {
 		System.out.println("insertProfessorDepartmentXRef: " + prof_id + " " + depart_id);
         PreparedStatement insertStmt = null;
         PreparedStatement statement = null;
         String checkString = "SELECT prof_id FROM ProfessorDepartmentXRef WHERE prof_id = ?::int4 AND depart_id = ?::int4";
         String insertString = "INSERT INTO ProfessorDepartmentXRef(prof_id, depart_id) VALUES(?::int4,?::int4)";
         try {
         	statement = connection.prepareStatement(checkString);
         	statement.setString(1, prof_id);
         	statement.setString(2, depart_id);
             statement.execute();
             ResultSet rs = statement.getResultSet();
             if(rs.next()) {
             	//there is a record in the database -- get the id from it and return it
             	System.out.println("existed entry - prof_id - depart_id: " + prof_id + " " + depart_id);
             } else {
             	//no record in the database -- insert one into the db, get the id of the new row, and return it
     			insertStmt = connection.prepareStatement(insertString);
     			insertStmt.setString(1, prof_id);
     			insertStmt.setString(2, depart_id);
     			insertStmt.execute();
     			
     			statement = connection.prepareStatement(checkString);
             	statement.setString(1, prof_id);
             	statement.setString(2, depart_id);
                 statement.execute();
                 rs = statement.getResultSet();
                 if(rs.next()) {
                 	//there is now a record in the database -- get the id from it and return it
                 	System.out.println("inserted entry - prof_id - depart_id: " + prof_id + " " + depart_id);
                 } else {
                 	System.out.println("ERROR: Result set is null after an insert.");                	
                 }
             }
             rs.close();
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		} finally {
 			try {
 				if(statement != null) statement.close();
 				if(insertStmt != null) insertStmt.close();
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private static void insertProfessorCourseXRef(String prof_id, String course_id, Connection connection) {
 		System.out.println("insertProfessorCourseXRef: " + prof_id + " " + course_id);
         PreparedStatement insertStmt = null;
         PreparedStatement statement = null;
         String checkString = "SELECT prof_id FROM ProfessorCourseXRef WHERE prof_id = ?::int4 AND course_id = ?::int4";
         String insertString = "INSERT INTO ProfessorCourseXRef(prof_id, course_id) VALUES(?::int4,?::int4)";
         try {
         	statement = connection.prepareStatement(checkString);
         	statement.setString(1, prof_id);
         	statement.setString(2, course_id);
             statement.execute();
             ResultSet rs = statement.getResultSet();
             if(rs.next()) {
             	//there is a record in the database -- get the id from it and return it
             	System.out.println("existed entry - prof_id - course_id: " + prof_id + " " + course_id);
             } else {
             	//no record in the database -- insert one into the db, get the id of the new row, and return it
     			insertStmt = connection.prepareStatement(insertString);
     			insertStmt.setString(1, prof_id);
     			insertStmt.setString(2, course_id);
     			insertStmt.execute();
     			
     			statement = connection.prepareStatement(checkString);
             	statement.setString(1, prof_id);
             	statement.setString(2, course_id);
                 statement.execute();
                 rs = statement.getResultSet();
                 if(rs.next()) {
                 	//there is now a record in the database -- get the id from it and return it
                 	System.out.println("inserted entry - prof_id - course_id: " + prof_id + " " + course_id);
                 } else {
                 	System.out.println("ERROR: Result set is null after an insert.");                	
                 }
             }
             rs.close();
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		} finally {
 			try {
 				if(statement != null) statement.close();
 				if(insertStmt != null) insertStmt.close();
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private static String insertComment(String easiness, String helpfulness, String raterInterest, String quality, String clarity, String comment, String timestamp, String course_id, String prof_id, Connection connection) {
 		System.out.println("insertComment: " + easiness + " " + helpfulness + " " + raterInterest + " " + quality + " " + clarity + " " + comment + " " + timestamp + " " + course_id + " " + prof_id);
 		String comment_id = null;
         PreparedStatement insertStmt = null;
         PreparedStatement statement = null;
         String checkString = "SELECT comment_id FROM Comments WHERE easiness = ?::numeric " +
         		"AND helpfulness = ?::numeric " +
         		"AND interest = ?::numeric " +
         		"AND quality = ?::int4 " +
         		"AND clarity = ?::numeric " +
         		"AND comment_text = ? " +
         		"AND time_stamp = ?::timestamp " +
         		"AND course_id = ?::int4 " +
         		"AND prof_id = ?::int4";
         String insertString = "INSERT INTO Comments(easiness, helpfulness, interest, quality, clarity, comment_text, time_stamp, course_id, prof_id) " +
         		"VALUES(?::numeric,?::numeric,?::numeric,?::int4,?::numeric,?,?::timestamp,?::int4,?::int4)";
         try {
         	statement = connection.prepareStatement(checkString);
         	statement.setString(1, easiness);
         	statement.setString(2, helpfulness);
         	statement.setString(3, raterInterest);
         	statement.setString(4, quality);
         	statement.setString(5, clarity);
         	statement.setString(6, comment);
         	statement.setString(7, timestamp);
         	statement.setString(8, course_id);
         	statement.setString(9, prof_id);
             statement.execute();
             ResultSet rs = statement.getResultSet();
             if(rs.next()) {
             	//there is a record in the database -- get the id from it and return it
             	course_id = rs.getString("comment_id");
             	System.out.println("existed- comment_id: " + comment_id);
             } else {
             	//no record in the database -- insert one into the db, get the id of the new row, and return it
     			insertStmt = connection.prepareStatement(insertString);
     			insertStmt.setString(1, easiness);
     			insertStmt.setString(2, helpfulness);
     			insertStmt.setString(3, raterInterest);
     			insertStmt.setString(4, quality);
     			insertStmt.setString(5, clarity);
     			insertStmt.setString(6, comment);
     			insertStmt.setString(7, timestamp);
     			insertStmt.setString(8, course_id);
     			insertStmt.setString(9, prof_id);
     			insertStmt.execute();
     			
     			statement = connection.prepareStatement(checkString);
             	statement.setString(1, easiness);
             	statement.setString(2, helpfulness);
             	statement.setString(3, raterInterest);
             	statement.setString(4, quality);
             	statement.setString(5, clarity);
             	statement.setString(6, comment);
             	statement.setString(7, timestamp);
             	statement.setString(8, course_id);
             	statement.setString(9, prof_id);
                 statement.execute();
                 rs = statement.getResultSet();
                 if(rs.next()) {
                 	//there is now a record in the database -- get the id from it and return it
                 	course_id = rs.getString("comment_id");
                 	System.out.println("inserted- comment_id: " + comment_id);
                 } else {
                 	System.out.println("ERROR: Result set is null after an insert.");                	
                 }
             }
             rs.close();
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 			return null;
 		} finally {
 			try {
 				if(statement != null) statement.close();
 				if(insertStmt != null) insertStmt.close();
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 
     	return course_id;
 	}
 	
 	private static String insertCourse(String courseNum, String depart_id, Connection connection) {
 		System.out.println("insertCourse: " + courseNum + " " + depart_id);
 		if(courseNum.equalsIgnoreCase("")) {
 			System.out.println("CourseNum is the empty string: returning the DBNull string");
 			return "0";
 		}
 		String course_id = null;
         PreparedStatement insertStmt = null;
         PreparedStatement statement = null;
         String checkString = "SELECT course_id FROM Courses WHERE course_num = ? AND depart_id = ?::int4";
         String insertString = "INSERT INTO Courses(course_num, depart_id) VALUES(?,?::int4)";
         try {
         	statement = connection.prepareStatement(checkString);
         	statement.setString(1, courseNum);
         	statement.setString(2, depart_id);
             statement.execute();
             ResultSet rs = statement.getResultSet();
             if(rs.next()) {
             	//there is a record in the database -- get the id from it and return it
             	course_id = rs.getString("course_id");
             	System.out.println("existed- course_id: " + course_id);
             } else {
             	//no record in the database -- insert one into the db, get the id of the new row, and return it
     			insertStmt = connection.prepareStatement(insertString);
     			insertStmt.setString(1, courseNum);
     			insertStmt.setString(2, depart_id);
     			insertStmt.execute();
     			
     			statement = connection.prepareStatement(checkString);
             	statement.setString(1, courseNum);
             	statement.setString(2, depart_id);
                 statement.execute();
                 rs = statement.getResultSet();
                 if(rs.next()) {
                 	//there is now a record in the database -- get the id from it and return it
                 	course_id = rs.getString("course_id");
                 	System.out.println("inserted- course_id: " + course_id);
                 } else {
                 	System.out.println("ERROR: Result set is null after an insert.");                	
                 }
             }
             rs.close();
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 			return null;
 		} finally {
 			try {
 				if(statement != null) statement.close();
 				if(insertStmt != null) insertStmt.close();
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 
     	return course_id;
 	}
 
 	private static String insertProfessor(String prof, String overallProfQual, String rootJSPTid, Connection connection) {
 		System.out.println("insertProfessor: " + prof + " " + overallProfQual + " " + rootJSPTid);
 		String prof_id = null;
         PreparedStatement insertStmt = null;
         PreparedStatement statement = null;
         String checkString = "SELECT prof_id FROM Professors WHERE prof_name = ? AND prof_qual = ?::numeric AND jsp_is = ?";
         String insertString = "INSERT INTO Professors(prof_name, prof_qual, jsp_is) VALUES(?,?::numeric,?)";
         try {
         	statement = connection.prepareStatement(checkString);
         	statement.setString(1, prof);
         	statement.setString(2, overallProfQual);
         	statement.setString(3, rootJSPTid);
             statement.execute();
             ResultSet rs = statement.getResultSet();
             if(rs.next()) {
             	//there is a record in the database -- get the id from it and return it
             	prof_id = rs.getString("prof_id");
             	System.out.println("existed- prof_id: " + prof_id);
             } else {
             	//no record in the database -- insert one into the db, get the id of the new row, and return it
     			insertStmt = connection.prepareStatement(insertString);
     			insertStmt.setString(1, prof);
     			insertStmt.setString(2, overallProfQual);
     			insertStmt.setString(3, rootJSPTid);
     			insertStmt.execute();
     			
     			statement = connection.prepareStatement(checkString);
             	statement.setString(1, prof);
             	statement.setString(2, overallProfQual);
             	statement.setString(3, rootJSPTid);
                 statement.execute();
                 rs = statement.getResultSet();
                 if(rs.next()) {
                 	//there is now a record in the database -- get the id from it and return it
                 	prof_id = rs.getString("prof_id");
                 	System.out.println("inserted- prof_id: " + prof_id);
                 } else {
                 	System.out.println("ERROR: Result set is null after an insert.");                	
                 }
             }
             rs.close();
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 			return null;
 		} finally {
 			try {
 				if(statement != null) statement.close();
 				if(insertStmt != null) insertStmt.close();
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 
     	return prof_id;
 	}
 
 	private static String insertDepartment(String depart, String univ_id, Connection connection) {
 		System.out.println("insertDepartment: " + depart + " " + univ_id);
 		String depart_id = null;
         PreparedStatement insertStmt = null;
         PreparedStatement statement = null;
         String checkString = "SELECT depart_id FROM Departments WHERE depart_name = ? AND univ_id = ?::int4";
         String insertString = "INSERT INTO Departments(depart_name, univ_id) VALUES(?,?::int4)";
         try {
         	statement = connection.prepareStatement(checkString);
         	statement.setString(1, depart);
         	statement.setString(2, univ_id);
             statement.execute();
             ResultSet rs = statement.getResultSet();
             if(rs.next()) {
             	//there is a record in the database -- get the id from it and return it
             	depart_id = rs.getString("depart_id");
             	System.out.println("existed- depart_id: " + depart_id);
             } else {
             	//no record in the database -- insert one into the db, get the id of the new row, and return it
     			insertStmt = connection.prepareStatement(insertString);
     			insertStmt.setString(1, depart);
     			insertStmt.setString(2, univ_id);
     			insertStmt.execute();
     			
     			statement = connection.prepareStatement(checkString);
             	statement.setString(1, depart);
             	statement.setString(2, univ_id);
                 statement.execute();
                 rs = statement.getResultSet();
                 if(rs.next()) {
                 	//there is now a record in the database -- get the id from it and return it
                 	depart_id = rs.getString("depart_id");
                 	System.out.println("inserted- depart_id: " + depart_id);
                 } else {
                 	System.out.println("ERROR: Result set is null after an insert.");                	
                 }
             }
             rs.close();
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 			return null;
 		} finally {
 			try {
 				if(statement != null) statement.close();
 				if(insertStmt != null) insertStmt.close();
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 
     	return depart_id;
 	}
 
 	private static String insertUniversity(String university, Connection connection) {
 		System.out.println("insertUniversity: " + university);
 		String univ_id = null;
         PreparedStatement insertStmt = null;
         PreparedStatement statement = null;
         String checkString = "SELECT univ_id FROM Universities WHERE univ_name = ?";
         String insertString = "INSERT INTO Universities(univ_name) VALUES(?)";
         try {
         	statement = connection.prepareStatement(checkString);
         	statement.setString(1, university);
             statement.execute();
             ResultSet rs = statement.getResultSet();
             if(rs.next()) {
             	//there is a record in the database -- get the id from it and return it
             	univ_id = rs.getString("univ_id");
             	System.out.println("existed- univ_id: " + univ_id);
             } else {
             	//no record in the database -- insert one into the db, get the id of the new row, and return it
     			insertStmt = connection.prepareStatement(insertString);
     			insertStmt.setString(1, university);
     			insertStmt.execute();
     			
             	statement = connection.prepareStatement(checkString);
             	statement.setString(1, university);
                 statement.execute();
                 rs = statement.getResultSet();
                 if(rs.next()) {
                 	//there is now a record in the database -- get the id from it and return it
                 	univ_id = rs.getString("univ_id");
                 	System.out.println("inserted- univ_id: " + univ_id);
                 } else {
                 	System.out.println("ERROR: Result set is null after an insert.");                	
                 }
             }
             rs.close();
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 			return null;
 		} finally {
 			try {
 				if(statement != null) statement.close();
 				if(insertStmt != null) insertStmt.close();
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 
     	return univ_id;
 	}
 	
     public static Connection getDatabaseConnection(String username, String password, String connectionString)
     {
         String dbURL = connectionString;
 
         try {
             return DriverManager.getConnection(dbURL, username, password);
         } catch(Exception e) {
             System.out.println("DB ERROR: " + dbURL + "\n with exception\n " + e.toString());
             return null;
         }
     }
 	
 }
