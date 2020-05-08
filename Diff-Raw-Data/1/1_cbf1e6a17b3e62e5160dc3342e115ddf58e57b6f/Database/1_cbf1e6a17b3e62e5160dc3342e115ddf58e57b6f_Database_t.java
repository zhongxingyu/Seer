 package ycp.edu.seniordesign.model.persist;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Random;
 
 import ycp.edu.seniordesign.model.Admin;
 import ycp.edu.seniordesign.model.Assignment;
 import ycp.edu.seniordesign.model.Course;
 import ycp.edu.seniordesign.model.EnrolledCourse;
 import ycp.edu.seniordesign.model.User;
 import ycp.edu.seniordesign.util.HashPassword;
 
 
 public class Database {
 	static {
 		try {
 			Class.forName("org.hsqldb.jdbc.JDBCDriver");
 		} catch (ClassNotFoundException e) {
 			throw new IllegalStateException("Could not load hsql driver");
 		}
 	}
 
 	private static final String JDBC_URL ="jdbc:hsqldb:file:whiteboard.db";
 	
 	private static final Database theInstance = new Database();
 	
 	public static Database getInstance() {
 		return theInstance;
 	}
 	
 	public Database(){
 
 	}
 	
 	/**
 	 * Authenticate the user via username and password
 	 * @param username the username of the user trying to login
 	 * @param password the plain-text password of the user trying to login
 	 * @return the User object associated with the username and password
 	 * @throws SQLException
 	 */
 	public User authenticateUser(String username, String password) throws SQLException {
 		Connection connection = null;
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);	
 			
 			// look up user with the given username
 			statement = connection.prepareStatement("select * from users where username=?");
 			statement.setString(1, username);
 			resultSet = statement.executeQuery();
 			 
 			if (resultSet.next()){
 				// there is someone with the given username
 				User user = new User();
 				user.loadFrom(resultSet);
 				
 				// Check password
 				String hashedPassword = HashPassword.computeHash(password, user.getSalt());
 				if (hashedPassword.equals(user.getPassword())){
 					// passwords matched
 					return user;
 				} else {
 					// passwords did not match
 					return null;
 				}
 			} else {
 				// the user does not exist
 				return null;
 			}
 				 	 
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 			DBUtil.closeQuietly(resultSet);
 		}
 	}
 	
 	/**
 	 * Authenticate the admin via username and password
 	 * @param username the username of the admin trying to login
 	 * @param password the plain-text password of the admin trying to login
 	 * @return the Admin object associated with the username and password
 	 * @throws SQLException
 	 */
 	public Admin authenticateAdmin(String username, String password) throws SQLException {
 		Connection connection = null;
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);	
 			
 			// look up user with the given username
 			statement = connection.prepareStatement("select * from admins where username=?");
 			statement.setString(1, username);
 			resultSet = statement.executeQuery();
 			 
 			if (resultSet.next()){
 				// there is an admin with the given username
 				Admin admin = new Admin();
 				admin.loadFrom(resultSet);
 				
 				// Check password
 				String hashedPassword = HashPassword.computeHash(password, admin.getSalt());
 				if (hashedPassword.equals(admin.getHashedPassword())){
 					// passwords matched
 					return admin;
 				} else {
 					// passwords did not match
 					return null;
 				}
 			} else {
 				// the user does not exist
 				return null;
 			}
 				 	 
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 			DBUtil.closeQuietly(resultSet);
 		}
 	}
 	
 	/**
 	 * Create an account in the user table
 	 * @param username the username of the new account
 	 * @param name the name of the new user
 	 * @param password the plain-text password of the new account
 	 * @param emailAddress the emailAddress of the new account
 	 * @param type the type of the new account (1 for student, 2 for professor, 3 for both)
 	 * @return the id of the newly inserted row, or -1 if the createAccount failed
 	 * @throws SQLException
 	 */
 	public int createAccount(String username, String name, String password, String emailAddress, int type) throws SQLException{
 		Connection connection = null;
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		
 		// TODO: do not allow creation of account with a variation in case on a current username
 		// Example: If there is a user with a user name of "username" do not allow creation of an account with the username of 
 		// "UsErNaME"
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);
 						
 			// check to see if username is taken
 			statement = connection.prepareStatement("select * from users where username=?");
 			statement.setString(1, username);
 			
 			resultSet = statement.executeQuery();
 			 
 			if (resultSet.next()){
 				// the username is already taken
 				return -1;
 			}
 			
 			// generate random salt and hash password
 			String salt = HashPassword.generateRandomSalt(new Random());
 			String hashedPassword = HashPassword.computeHash(password, salt);
 			
 			// add the user to the database
 			statement = connection.prepareStatement("insert into users values(NULL,?,?,?,?,?,?,'', 'false')");
 			statement.setString(1, username);
 			statement.setString(2, name);
 			statement.setString(3, hashedPassword);
 			statement.setString(4, salt);
 			statement.setString(5, emailAddress);
 			statement.setInt(6, type);
 			statement.execute();
 			
 			// get the id of the newly inserted row
 			statement = connection.prepareStatement("select * from users where username=?");
 			statement.setString(1, username);
 			resultSet = statement.executeQuery();
 			
 			resultSet.next();
 			return resultSet.getInt(1);
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 			DBUtil.closeQuietly(resultSet);
 		}
 	}
 	
 	/**
 	 * Delete account from the user table
 	 * @param username the username of the account to be deleted
 	 * @param password the password of the account to be deleted
 	 * @return ture if the account is sucessfully deleted, false otherwise
 	 * @throws SQLException
 	 */
 	public boolean deleteAccount(String username, String password) throws SQLException{
 		Connection connection = null;
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);
 				
 			// check to see if user exists
 			statement = connection.prepareStatement("select * from users where username=?");
 			statement.setString(1, username);
 			resultSet = statement.executeQuery();
 			 
 			if (resultSet.next()){
 				// there is someone with the given username
 				User user = new User();
 				user.loadFrom(resultSet);
 				
 				// Check password
 				String hashedPassword = HashPassword.computeHash(password, user.getSalt());
 				if (hashedPassword.equals(user.getPassword())){
 					// delete the user from the database
 					statement = connection.prepareStatement("delete from users where username=? and password=?");
 					statement.setString(1,  username);
 					statement.setString(2, hashedPassword);
 					statement.execute();
 				
 					return true;
 				} else {
 					// the user does not exist
 					return false;
 				}
 			}
 			
 			return false;
 		
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 			DBUtil.closeQuietly(resultSet);
 		}
 	}
 	
 	/**
 	 * Gets the user with the given id
 	 * @param id the id of the user to search for
 	 * @return the User object with the given id if it exists, otherwise null
 	 * @throws SQLException
 	 */
 	public User getUserById(int id) throws SQLException{
 		Connection connection = null;
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);
 						
 			statement = connection.prepareStatement("select * from users where id=?");
 			statement.setInt(1, id);
 			
 			resultSet = statement.executeQuery();
 			 
 			if (resultSet.next()){
 				// the user exists
 				User user = new User();
 				user.loadFrom(resultSet);
 				return user;
 			}
 			
 			else {
 				// no user exist with the given id
 				return null;
 			}
 			
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 			DBUtil.closeQuietly(resultSet);
 		}
 	}
 	
 	public Course getCourseById(int id) throws SQLException{
 		Connection connection = null;
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);
 						
 			statement = connection.prepareStatement("select * from courses where id=?");
 			statement.setInt(1, id);
 			
 			resultSet = statement.executeQuery();
 			 
 			if (resultSet.next()){
 				// the course exists
 				Course course = new Course();
 				course.loadFrom(resultSet);
 				return course;
 			}
 			
 			else {
 				// no course exist with the given id
 				return null;
 			}
 			
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 			DBUtil.closeQuietly(resultSet);
 
 		}
 	}
 	
 	public EnrolledCourse getEnrolledCourseById(int id) throws SQLException{
 		Connection connection = null;
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);
 						
 			statement = connection.prepareStatement("select * from enrolled_courses where id=?");
 			statement.setInt(1, id);
 			resultSet = statement.executeQuery();
 			 
 			if (resultSet.next()){
 				// the enrolled course exists
 				EnrolledCourse course = new EnrolledCourse();
 				course.loadFrom(resultSet);
 				return course;
 			}
 			
 			else {
 				// no enrolled course exist with the given id
 				return null;
 			}
 			
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 			DBUtil.closeQuietly(resultSet);
 
 		}
 	}
 	
 	public Assignment getAssignmentById(int id) throws SQLException {
 		Connection connection = null;
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);
 			
 			statement = connection.prepareStatement("select * from assignments where id=?");
 			statement.setInt(1,  id);
 			
 			resultSet = statement.executeQuery();
 			
 			if (resultSet.next()) {
 				Assignment assignment = new Assignment();
 				assignment.loadFrom(resultSet);
 				return assignment;
 			} else {
 				return null;
 			}
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 			DBUtil.closeQuietly(resultSet);
 		}
 	}
 	
 	/**
 	 * This method returns a list of all the courses taken by the student
 	 * @param user
 	 * @return if the user is a professor the method returns null, otherwise the method returns an ArrayList of all the courses the student is enrolled in
 	 * @throws SQLException
 	 */
 	public ArrayList<EnrolledCourse> getEnrolledCoursesForStudent(User user) throws SQLException{
 		if (!user.isStudent()){
 			// the user that was passed is a professor and thus does not take any classes
 			return null;
 		}
 		
 		Connection connection = null;
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);
 						
 			statement = connection.prepareStatement("select * from enrolled_courses where student_id=?");
 			statement.setInt(1, user.getId());
 			resultSet = statement.executeQuery();
 			
 			ArrayList<EnrolledCourse> courses = new ArrayList<EnrolledCourse>(); 
 			while (resultSet.next()){
 				EnrolledCourse course = new EnrolledCourse();
 				course.loadFrom(resultSet);
 				courses.add(course);
 			}
 			
 			return courses;
 			
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 			DBUtil.closeQuietly(resultSet);
 		}
 	}
 	
 	/**
 	 * This method returns a list of all the courses taught by a professor
 	 * @param user 
 	 * @return if the user is a student the method returns null, otherwise the method returns an ArrayList of all the course the professor teaches
 	 * @throws SQLException
 	 */
 	public ArrayList<Course> getCoursesForProfessor(User user) throws SQLException{
 		if (!user.isProfessor()){
 			// the user that was passed is a student and thus does not teach any classes
 			return null;
 		}
 		
 		Connection connection = null;
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);
 						
 			statement = connection.prepareStatement("select * from courses where professor_id=?");
 			statement.setInt(1, user.getId());
 			resultSet = statement.executeQuery();
 			
 			ArrayList<Course> courses = new ArrayList<Course>(); 
 			while (resultSet.next()){
 				Course course = new Course();
 				course.loadFrom(resultSet);
 				courses.add(course);
 			}
 			
 			return courses;
 			
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 			DBUtil.closeQuietly(resultSet);
 		}
 	}
 	
 	/**
 	 * This method gets a list of all assignments for the given courses and student
 	 * @param courseId the courseId the assignment is for
 	 * @param studentId the studentId the assignment is for
 	 * @return an ArrayList of the assignments for the given course and student
 	 * @throws SQLException
 	 */
 	public ArrayList<Assignment> getAssignmentsForCourse(int courseId, int studentId) throws SQLException{		
 		Connection connection = null;
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);
 						
 			statement = connection.prepareStatement("select * from assignments where course_id=? and student_id=?");
 			statement.setInt(1, courseId);
 			statement.setInt(2,  studentId);
 			resultSet = statement.executeQuery();
 			
 			ArrayList<Assignment> assignments = new ArrayList<Assignment>(); 
 			while (resultSet.next()){
 				Assignment assignment = new Assignment();
 				assignment.loadFrom(resultSet);
 				assignments.add(assignment);
 			}
 			
 			return assignments;
 			
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 			DBUtil.closeQuietly(resultSet);
 		}
 	}
 	
 	public ArrayList<Assignment> getAssignmentsForProfessor(int courseId) throws SQLException{		
 		Connection connection = null;
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		ArrayList<Assignment> assignments = new ArrayList<Assignment>(); 
 		
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);
 						
 			statement = connection.prepareStatement("select distinct name from assignments where course_id=?");
 			statement.setInt(1, courseId);
 			resultSet = statement.executeQuery();
 			ArrayList<String> names = new ArrayList<String>();
 			
 			while(resultSet.next()){
 				names.add(resultSet.getString((1)));
 			}
 			
 			statement = connection.prepareStatement("select * from assignments where name = ?");
 			for (String name: names){
 				statement.setString(1, name);
 				resultSet = statement.executeQuery();
 				if (resultSet != null){
 					resultSet.next();
 					Assignment assignment = new Assignment();
 					assignment.loadFrom(resultSet);
 					assignments.add(assignment);
 				} 
 			}
 			if (assignments.isEmpty()){
 				return null;
 			}
			
 			return assignments;
 			
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 			DBUtil.closeQuietly(resultSet);
 		}
 	}
 	
 	/**
 	 * This method adds an entry to the Enrolled_Courses table representing a Course that the given user is enrolled in
 	 * @param courseId the id of the course to add
 	 * @param studentId the id of the student who is enrolling in the course
 	 * @param professorId the id of the professor who teaches the course
 	 * @return returns the id of the newly inserted EnrolledCourse
 	 * @throws SQLException
 	 */
 	public int addEnrolledCourseForStudent(int studentId, int professorId, int courseId) throws SQLException{
 		Connection connection = null;
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);			
 			
 			statement = connection.prepareStatement("insert into enrolled_courses values(null,?, ?, ?, ?)");
 			statement.setInt(1, studentId);
 			statement.setInt(2,  professorId);
 			statement.setInt(3, courseId);
 			statement.setInt(4, 100); // students start with a 100 in a course they have just enrolled in
 			statement.execute();
 			
 			// Get the id that the course was added with
 			statement = connection.prepareStatement("select id from enrolled_courses where student_id=? and professor_id =? and course_id=?");
 			statement.setInt(1, studentId);
 			statement.setInt(2,  professorId);
 			statement.setInt(3, courseId);
 			resultSet = statement.executeQuery();
 			
 			resultSet.next();
 			return resultSet.getInt(1);
 						
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 			DBUtil.closeQuietly(resultSet);
 		}
 	}
 	
 	/**
 	 * This method deletes the record in the database representing a course taken by the given student. It is used in the case of 
 	 * withdrawals, drops, etc.
 	 * @param courseId the id of the course to remove
 	 * @param studentId the id of the student who is removing the course
 	 * @throws SQLException
 	 */
 	public void removeEnrolledCourseForStudent(int courseId, int studentId) throws SQLException{
 		Connection connection = null;
 		PreparedStatement statement = null;
 		
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);			
 			
 			statement = connection.prepareStatement("delete from enrolled_courses where student_id=? and course_id =?");
 			statement.setInt(1, studentId);
 			statement.setInt(2, courseId);
 			statement.execute();
 						
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 		}
 	}
 	
 	
 	/**
 	 * This method adds a row to the Courses table in the database for a given professor
 	 * @param course the course object to store (it should have the professor field set to the id of the professor who will be 
 	 * teaching the course
 	 * @return the id of the newly inserted row
 	 * @throws SQLException
 	 */
 	public int addCourseForProfessor(Course course) throws SQLException{
 		Connection connection = null;
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);			
 			
 			statement = connection.prepareStatement("insert into courses values(?,?,?,?,?,?,?,?,?,?,?)");
 			course.storeTo(statement);
 			statement.setString(1, null);
 			statement.execute();
 			
 			
 			// Get the id that the course was added with
 			statement = connection.prepareStatement("select id from courses where name =? and professor_id=? and time=? and course_num =? and sec_num=? and credits=? and weekly_days=? and location = ? and CRN =? and description =?");
 			statement.setString(1, course.getName());
 			statement.setInt(2, course.getProfessorId());
 			statement.setString(3, course.getTime());
 			statement.setInt(4, course.getCourseNumber());
 			statement.setInt(5, course.getSectionNumber());
 			statement.setInt(6, course.getCredits());
 			statement.setString(7, course.getDays());
 			statement.setString(8, course.getLocation());
 			statement.setInt(9, course.getCRN());
 			statement.setString(10, course.getDescription());
 			resultSet = statement.executeQuery();
 			
 			resultSet.next();
 			return resultSet.getInt(1);						
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 			DBUtil.closeQuietly(resultSet);
 		}
 	}
 	
 	/**
 	 * This method deletes the row from the course table with the courseId
 	 * @param courseId the id of the course to remove
 	 * @throws SQLException
 	 */
 	public void removeCourseForProfessor(int courseId) throws SQLException{
 		Connection connection = null;
 		PreparedStatement statement = null;
 		
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);			
 			
 			statement = connection.prepareStatement("delete from courses where id =?");
 			statement.setInt(1,  courseId);
 			statement.execute();
 						
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 		}
 	}
 	
 	
 	/**
 	 * This method adds a row to the Assignments table in the database
 	 * @param assignment the assignment object to store
 	 * @return the id of the newly inserted row
 	 * @throws SQLException
 	 */
 	public int addAssignmentForCourse(Assignment assignment) throws SQLException{
 		Connection connection = null;
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);			
 			
 			statement = connection.prepareStatement("insert into assignments values(?,?,?,?,?,?,?,?)");
 			assignment.storeTo(statement);
 			statement.setString(1, null);
 			statement.execute();
 			
 			// Get the id that the assignment was added with
 			statement = connection.prepareStatement("select id from assignments where course_id=? and student_id=? and name=? and due_date=? and grade_weight_type=? and earned_points=? and possible_points=?");
 			statement.setInt(1, assignment.getCourseId());
 			statement.setInt(2, assignment.getStudentId());
 			statement.setString(3, assignment.getName());
 			statement.setDate(4, new java.sql.Date(assignment.getDueDate().getTime()));
 			statement.setInt(5, assignment.getGradeWeightType());
 			statement.setInt(6, assignment.getEarnedPoints());
 			statement.setInt(7, assignment.getPossiblePoints());
 			resultSet = statement.executeQuery();
 
 			resultSet.next();
 			return resultSet.getInt(1);						
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 			DBUtil.closeQuietly(resultSet);
 		}
 	}
 	
 	public void removeAssignmentForCourse(int assignmentId) throws SQLException{
 		Connection connection = null;
 		PreparedStatement statement = null;
 		
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);			
 			
 			statement = connection.prepareStatement("delete from assignments where id =?");
 			statement.setInt(1,  assignmentId);
 			statement.execute();
 						
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 		}
 	}
 	
 	public boolean changePassword(User user, String newPassword) throws Exception{
 		Connection connection = null;
 		PreparedStatement statement = null;
 		
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);		
 			
 			String hashedPassword = HashPassword.computeHash(newPassword, user.getSalt());
 			
 			statement = connection.prepareStatement("update users set password=? where id=?");  
 			statement.setString(1,  hashedPassword);
 			statement.setInt(2, user.getId());
 			
 			int rowsUpdated = statement.executeUpdate();
 
 			if (rowsUpdated == 1){
 				// Perfect only one user's password was changed
 				return true;
 			} else if (rowsUpdated == 0){
 				// No user found with the given id
 				return false;
 			} else {
 				// This is bad the password for multiple users was changed
 				throw new Exception("Multiple users with the same id (should not be possible)");
 			}
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 		}   
 
 	}
 	public User getUserByUsername(String username) throws SQLException{
 		Connection connection = null;
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);
 						
 			statement = connection.prepareStatement("select * from users where username=?");
 			statement.setString(1, username);
 			
 			resultSet = statement.executeQuery();
 			 
 			if (resultSet.next()){
 				// the user exists
 				User user = new User();
 				user.loadFrom(resultSet);
 				return user;
 			}
 			else {
 				// no user exist with the given id
 				return null;
 			}
 			
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 			DBUtil.closeQuietly(resultSet);
 		}
 	}
 	
 	public User getUserByEmail(String emailAddress) throws SQLException{
 		Connection connection = null;
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);
 						
 			statement = connection.prepareStatement("select * from users where emailAddress=?");
 			statement.setString(1, emailAddress);
 			
 			resultSet = statement.executeQuery();
 			 
 			if (resultSet.next()){
 				// the user exists
 				User user = new User();
 				user.loadFrom(resultSet);
 				return user;
 			}
 			
 			else {
 				// no user exist with the given id
 				return null;
 			}
 			
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 			DBUtil.closeQuietly(resultSet);
 		}
 	}
 
 	public ArrayList<Assignment> getInstancesofAssignment(int id, String name) throws SQLException
 	{
 		Connection connection = null;
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		
 		try 
 		{
 			connection = DriverManager.getConnection(JDBC_URL);
 			
 			statement = connection.prepareStatement("select * from assignments where id=? and name=?");
 			statement.setInt(1,  id);
 			statement.setString(2, name);
 			
 			resultSet = statement.executeQuery();
 			
 			ArrayList<Assignment> returnList = new ArrayList<Assignment>();
 
 			while (resultSet.next())
 			{
 				Assignment assignment = new Assignment();
 				assignment.loadFrom(resultSet);
 				returnList.add(assignment);
 			}
 			
 			if(returnList.isEmpty())
 			{
 				return null;
 			}
 			return returnList;
 
 			
 		} 
 		finally 
 		{
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 			DBUtil.closeQuietly(resultSet);
 		}
 			
 	}
 	
 	public ArrayList<Assignment> getInstancesofAssignment(int id) throws SQLException
 	{
 		Connection connection = null;
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		
 		try 
 		{
 			connection = DriverManager.getConnection(JDBC_URL);
 			
 			statement = connection.prepareStatement("select * from assignments where id=?");
 			statement.setInt(1,  id);
 			
 			resultSet = statement.executeQuery();
 			
 			ArrayList<Assignment> returnList = new ArrayList<Assignment>();
 
 			while (resultSet.next())
 			{
 				Assignment assignment = new Assignment();
 				assignment.loadFrom(resultSet);
 				returnList.add(assignment);
 			}
 			
 			if(returnList.isEmpty())
 			{
 				return null;
 			}
 			return returnList;
 
 			
 		} 
 		finally 
 		{
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 			DBUtil.closeQuietly(resultSet);
 		}
 	}
 
 	public void updateUser(User user) throws SQLException{
 		Connection connection = null;
 		PreparedStatement statement = null;
 		
 		try {
 			connection = DriverManager.getConnection(JDBC_URL);
 			
 			statement = connection.prepareStatement("update users set emailaddress =?, major =?, commuter =?, password =? where id =?");
 			statement.setString(1, user.getEmailAddress());
 			statement.setString(2, user.getMajor());
 			statement.setBoolean(3, user.isCommuter());
 			statement.setString(4, user.getPassword());
 			statement.setInt(5, user.getId());
 			statement.execute();
 			
 		} finally {
 			DBUtil.close(connection);
 			DBUtil.closeQuietly(statement);
 
 		}
 	}
 }
