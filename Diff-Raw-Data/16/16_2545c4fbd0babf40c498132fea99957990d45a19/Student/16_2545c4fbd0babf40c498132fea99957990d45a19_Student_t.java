 package cs236369.hw5.dal;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.regex.Pattern;
 
 import cs236369.hw5.Logger;
 
 public class Student extends Base {
 
 	static String tableName = "students";
 
 	//TODO table initialization
 	@Override
 	void setFieldTypes() {
 		fieldsTypes.put("username", "string");
 		fieldsTypes.put("password", "string");
 		fieldsTypes.put("name", "string");
 		fieldsTypes.put("phone_number", "string");
 	}
 
 	public String getTableName() {
 		return tableName;
 	}
 	public Student(ResultSet rs) {
 		super(rs);
 	}
 
 	public Student() {
 		super();
 		key = "username";
 	}
 
 	public Student(int id) {
 		super(id);
 	}
 
 	public String getName() {
 		return getStringField("name");
 	}
 
 	public String getUsername() {
 		return getStringField("username");
 	}
 
 	public String getPhoneNumber() {
 		return getStringField("phone_number");
 	}
 
 	public static Student authenticate(String username, String password) throws Exception {
 		try {
 			Connection conn = Utils.getConnection();
 			PreparedStatement ps = conn.prepareStatement("SELECT * FROM students WHERE username = ? and password = ? LIMIT 1");
 			ps.setString(1, username);
 			ps.setString(2, password);
 			Logger.log(ps.toString());
 			ResultSet rs = ps.executeQuery();
 			if (rs.next()) {
 				Student ret = new Student(rs);
 				Utils.closeConnection(rs, ps, conn);
 				return ret;
 			}
 			Utils.closeConnection(rs, ps, conn);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static Student[] GetByIds(int[] ids) throws SQLException {
 		if (ids == null || ids.length == 0){
 			return new Student[0];
 		}
 		Connection connection = Utils.getConnection();
 		PreparedStatement prepStmt = null;
 		ResultSet rs = null;
 		StringBuilder stringIds = new StringBuilder();
 		stringIds.append(ids[0]);
 		for (int i = 1; i < ids.length; ++i)
 			stringIds.append(", ").append(ids[i]);
 		try {
 			prepStmt = connection.prepareStatement("SELECT * FROM students WHERE id in (" + stringIds.toString() + ")");
 			rs = prepStmt.executeQuery();
 			//connection.close();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		ArrayList<Student> students = new ArrayList<Student>();
 		while(rs.next()) {
 			students.add(new Student(rs));
 		}
 		Student[] arrayStudents = new Student[students.size()];
 		students.toArray(arrayStudents);
 		Utils.closeConnection(rs, prepStmt, connection);
 		return arrayStudents;
 	}
 
 	public void signToACourse(int course_id) {
 		try
 		{
 			Connection conn = Utils.getConnection();
 			String query = "INSERT INTO courses_students (course_id, student_id) VALUES (?, ?)";
 			PreparedStatement ps = conn.prepareStatement(query);
 			ps.setInt(1,course_id);
 			ps.setInt(2,getId());
 			Logger.log(ps.toString());
 			ps.executeUpdate();
 			Utils.closeConnection(null, ps, conn);
 		}
 		catch (SQLException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public ArrayList<Integer> getAvailableCoursesIds() {
 		ArrayList<Integer> ids = new ArrayList<Integer>();
 		Course[] courses;
 		try {
 			courses = Course.getAll();
 		} catch (SQLException e1) {
 			// TODO Auto-generated catch block
 			courses = new Course[0];
 			e1.printStackTrace();
 		}
 		for (Course course : courses) {
 			try {
 				if (isCourseAvailable(course)) {
 					Logger.log("another avail " + course.getId());
 					ids.add(course.getId());
 				}
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return ids;
 	}
 
 	public Session[] getSessions() throws SQLException {
 		Course[] courses = getCourses();
 		int[] ids = new int[courses.length];
 		for (int i = 0; i < ids.length; ++i) {
 			ids[i] = courses[i].getId();
 		}
 
 		return Session.getByCoursesIds(ids);
 	}
 
 	public Course[][] getTimeTable(int days_per_week, int hours_per_day) throws SQLException {
 		Course[][] time_table = new Course[days_per_week][hours_per_day];
 		for (int day = 0; day < days_per_week; ++day) {
 			for (int hour = 0; hour < hours_per_day; ++ hour) {
 				time_table[day][hour] = new Course();
 			}
 		}
 		Course[] courses = getCourses();
 		HashMap<Integer, Course> idsToCourses = new HashMap<Integer, Course>();
 		for (Course course : courses) {
 			System.out.println("insert to map: " + course.getName());
 			idsToCourses.put(course.getId(), course);
 		}
 		for (Session session : getSessions()) {
 			System.out.println("in session");
 			if (session.getEndHour() >= hours_per_day || session.getDayOfWeek() >= days_per_week) {
 				continue;
 			}
 			System.out.println("valid session");
 			for (int hour = session.getStartHour(); hour <= session.getEndHour(); ++hour) {
 				time_table[session.getDayOfWeek()][hour] = idsToCourses.get(session.getCourseId());
 				System.out.println("day: " + session.getDayOfWeek() + "; hour: " + hour + "; course_id: " + session.getCourseId() + "; course_name: " + idsToCourses.get(session.getCourseId()).getName());
 			}
 		}
 		return time_table;
 	}
 
 	public boolean isCourseAvailableById(int course_id) throws SQLException {
 		Course course = new Course(course_id);
 		return isCourseAvailable(course);
 	}
 
 	public boolean isCourseAvailable(Course course) throws SQLException {
 		if (isRegisteredToCourse(course.getId())){
 			return false;
 		}
 
 		if (course.getNumStudents() >= course.getCapacity()) {
 			return false;
 		}
 
 		Course[] courses = getCourses();
 		for (Course registered_course : courses) {
 			if (Course.doCoursesConflict(course, registered_course)) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 	public boolean registerToCourse(int course_id) throws SQLException {
 		Connection conn = Utils.getConnection();
 		String query = "SELECT * FROM courses_students WHERE student_id = ?";
 		System.out.println("register query: " + query);
 		PreparedStatement ps = conn.prepareStatement(query);
 		ps.setInt(1,getId());
 		Logger.log(ps.toString());
 		ResultSet rs = ps.executeQuery();
 		ArrayList<Integer> ids = new ArrayList<Integer>();
 		while(rs.next()) {
 			ids.add(rs.getInt("course_id"));
 		}
 		Utils.closeConnection(rs, ps, conn);
 
 		if (ids.contains(course_id)) {
 			System.out.println("already reg!!!!");
 			return false;
 		}
 		if (!isCourseAvailableById(course_id)) {
 			System.out.println("not avail!!!!!!");
 			return false;
 		}
 		Utils.executeUpdate("INSERT INTO courses_students (course_id, student_id) VALUES (" + course_id + ", " + getId() + ")");
 
 		Course course = new Course(course_id);
 		if(isTimeTableValid() && course.getNumStudents() <= course.getCapacity()) {
 			return true;
 		}
 
 		System.out.println("is valid: " + isTimeTableValid());
 		Utils.executeUpdate("DELETE FROM courses_students where course_id = " + course_id + " and student_id = " + getId());
 		System.out.println("baaaa!!!!");
 		return false;
 	}
 
 	private boolean isTimeTableValid() throws SQLException {
 		for (Course course1 : getCourses()) {
 			for (Course course2: getCourses()) {
 				System.out.println("comaring courses: " + course1.getName() + " : " + course2.getName() );
 				if (!course1.equals(course2) && Course.doCoursesConflict(course1, course2)) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	public Course[] getCourses() throws SQLException {
 		Connection conn = Utils.getConnection();
 		String query = "SELECT * FROM courses_students WHERE student_id = ?";
 		PreparedStatement ps = conn.prepareStatement(query);
 		ps.setInt(1, getId());
 		Logger.log(ps.toString());
 		ResultSet rs = ps.executeQuery();
 		ArrayList<Integer> ids = new ArrayList<Integer>();
 		while(rs.next()) {
 			ids.add(rs.getInt("course_id"));
 		}
 
 		Utils.closeConnection(rs, ps, conn);
 		int[] arrayIds = new int[ids.size()];
 		for (int i=0; i < arrayIds.length; i++){
 			arrayIds[i] = ids.get(i).intValue();
 		}
 		Course[] courses =  Course.GetByIds(arrayIds);
 		return courses;
 	}
 
 	public boolean isRegisteredToCourse(int course_id) {
 		Connection conn = Utils.getConnection();
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try {
 			ps = conn.prepareStatement("SELECT * FROM courses_students WHERE student_id = ? and course_id = ?");
 			ps.setInt(1, getId());
 			ps.setInt(2, course_id);
 			Logger.log(ps.toString());
 			rs = ps.executeQuery();
 			rs.last();
 			boolean ret = rs.getRow() == 1;
 			Utils.closeConnection(rs, ps, conn);
 			return ret;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public void unregisterFromCourse(int course_id) {
 		Utils.executeUpdate("DELETE FROM courses_students where course_id = " + course_id + " and student_id = " + getId());
 	}
 
 	public static Student[] getAll() throws SQLException{
 		Connection conn = Utils.getConnection();
 		PreparedStatement ps = conn.prepareStatement("SELECT * FROM students");
 		Logger.log(ps.toString());
 		ResultSet rs = ps.executeQuery();
 		ArrayList<Student> students = new ArrayList<Student>();
 		while(rs.next()) {
 			students.add(new Student(rs));
 		}
 		Student[] arrayCourses = new Student[students.size()];
 		students.toArray(arrayCourses);
 		Utils.closeConnection(rs, ps, conn);
 		return arrayCourses;
 	}
 
 	public boolean hasDuplicate() {
 		Connection conn = Utils.getConnection();
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try {
 			System.out.println("GSDG");
 			ps = conn.prepareStatement("SELECT * FROM students WHERE id != ? and username = ?");
 			ps.setInt(1, getId());
 			ps.setString(2, getStringField("username"));
 			System.out.println(getStringField("username"));
 			rs = ps.executeQuery();
 			rs.last();
 			return rs.getRow() == 1;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		} finally {
 			Utils.closeConnection(rs, ps, conn);
 		}
 	}
 
 	public boolean validate() {
 		if (!Pattern.matches("^[a-zA-Z]{5,12}$", getStringField("username"))) {
 			return false;
 		}
 
 		if (!Pattern.matches("^[a-zA-Z0-9]{5,12}$", getStringField("password"))) {
 			return false;
 		}
 
 		if (!Pattern.matches("^.{1,25}$", getStringField("name"))) {
 			return false;
 		}
 
 		if (!Pattern.matches("^[0-9\\-]{0,25}$", getStringField("phone_number"))) {
 			return false;
 		}
 
 		return true;
 	}
 
 	public boolean delete(){
 		if (!duplicate("id", new Integer(id).toString())){
 			return false;
 		}
 		Connection conn = Utils.getConnection();
 		String prepStmt = "DELETE FROM " + getTableName() + " WHERE id = ?;";
 		PreparedStatement ps = null;
 
 		try
 		{
 			ps = conn.prepareStatement(prepStmt);
 			ps.setInt(1, id);
 			Logger.log(ps.toString());
 			ps.executeUpdate();
 			ps = conn.prepareStatement("DELETE FROM courses_students WHERE student_id  = ?");
 			ps.setInt(1, id);
 			ps.executeUpdate();
 			Utils.closeConnection(null, ps, conn);
 			return true;
 		}catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Utils.closeConnection(null, ps, conn);
 		return false;
 	}
 
 	public static Student getByUsername(String username) {
 		Connection conn = Utils.getConnection();
 		String query = "SELECT * FROM students WHERE username = ?";
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try
 		{
 			ps = conn.prepareStatement(query);
 			ps.setString(1, username);
 			Logger.log(ps.toString());
 			rs = ps.executeQuery();
			rs.next();
 			Student student =  new Student(rs);
 			return student;
 		}catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		} finally {
 			Utils.closeConnection(rs, ps, conn);
 		}
 	}
 
 }
