 package de.sportschulApp.server.databanker;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 import de.sportschulApp.shared.Course;
 
 public class DataBankerCourse implements DataBankerCourseInterface {
 
 	/**
 	 * Erstellt einen Kurseintrag
 	 * 
 	 * @param Object
 	 *            des typs Course
 	 * 
 	 * 
 	 * @return "name already used" wenn der Kursname schon vergeben ist, "error"
 	 *         wenn ein Fehler auftritt und "course created" wenn der Kurs
 	 *         angelegt wurde
 	 */
 
 	public String createCourse(Course course) {
 		DataBankerConnection dbc = new DataBankerConnection();
 		int courseID = 0;
 
 		try {
 
 			ResultSet rs = null;
 			Statement stmt2 = dbc.getStatement();
 			String query = "SELECT count(*) FROM Courses WHERE name='"
 					+ course.getName() + "'";
 
 			rs = stmt2.executeQuery(query);
 			while (rs.next()) {
 				if (rs.getInt(1) > 0) {
 					return "name alrady used";
 				}
 			}
 			stmt2.close();
 
 			PreparedStatement stmt = null;
 			stmt = dbc
 
 					.getConnection()
 					.prepareStatement(
 							"INSERT INTO Courses(name, time, instructor, location) VALUES(?,?,?,?)");
 			stmt.setString(1, course.getName());
 			stmt.setString(2, course.getTime());
 			stmt.setString(3, course.getInstructor());
 			stmt.setString(4, course.getLocation());
 
 			stmt.executeUpdate();
 
 			ResultSet rs2 = null;
 			Statement stmt3 = dbc.getStatement();
 			String query2 = "SELECT Courses_id FROM Courses WHERE name='"
 					+ course.getName() + "'";
 
 			rs2 = stmt3.executeQuery(query2);
 
 			while (rs2.next()) {
 				courseID = rs2.getInt(1);
 			}
 
 			rs.close();
 			rs2.close();
 			stmt3.close();
 			dbc.close();
 			stmt.close();
 		} catch (SQLException e) {
 			System.out.println(e);
 			return "error";
 		}
 
 		if (setBelts(courseID, course.getBeltColours())) {
 			return "course created";
 		} else {
 			return "error";
 		}
 	}
 
 	public boolean updateCourse(Course course) {
 		if (deleteCourse(course.getCourseID())) {
 			createCourse(course);
 		}
 		return false;
 	}
 
 	public boolean deleteCourse(int courseID) {
 
 		DataBankerConnection dbc = new DataBankerConnection();
 		Statement stmt = dbc.getStatement();
 
 		String query = "DELETE FROM Courses WHERE Courses_id='" + courseID
 				+ "'";
 
 		try {
 			stmt.executeUpdate(query);
 			dbc.close();
 			stmt.close();
 			dbc.closeStatement();
 
 		} catch (SQLException e) {
 			System.out.println(e);
 			return false;
 		}
 
 		if (deleteBelts(courseID)) {
 			return true;
 		} else {
 			return false;
 		}
 
 	}
 
 	public ArrayList<Course> getCourses() {
 
 		ArrayList<Course> courses = new ArrayList<Course>();
 
 		DataBankerConnection dbc = new DataBankerConnection();
 		ResultSet rs = null;
 		Statement stmt = dbc.getStatement();
 
 		String query = "SELECT * FROM Courses";
 
 		try {
 			rs = stmt.executeQuery(query);
 			while (rs.next()) {
 				Course newCourse = new Course();
 				newCourse.setCourseID(rs.getInt(1));
 				newCourse.setName(rs.getString(2));
 				newCourse.setTime(rs.getString(3));
 				newCourse.setInstructor(rs.getString(4));
 				newCourse.setLocation(rs.getString(5));
 				newCourse.setBeltColours(getBelts(rs.getInt(1)));
 				courses.add(newCourse);
 			}
 			rs.close();
 			dbc.close();
 			stmt.close();
 		} catch (Exception e) {
 			System.out.println(e);
 			return null;
 		}
 		return courses;
 	}
 
 	public ArrayList<String> getCourseNames() {
 
 		ArrayList<String> names = new ArrayList<String>();
 
 		DataBankerConnection dbc = new DataBankerConnection();
 		ResultSet rs = null;
 		Statement stmt = dbc.getStatement();
 
 		String query = "SELECT name FROM Courses";
 
 		try {
 			rs = stmt.executeQuery(query);
 			while (rs.next()) {
 				names.add(rs.getString(1));
 			}
 			rs.close();
 			dbc.close();
 			stmt.close();
 		} catch (Exception e) {
 			System.out.println(e);
 			return null;
 		}
 		return names;
 	}
 
 	public ArrayList<String> getBelts(int courseID) {
 
 		ArrayList<String> belts = new ArrayList<String>();
 		DataBankerConnection dbc = new DataBankerConnection();
 		ResultSet rs = null;
 		Statement stmt = dbc.getStatement();
 
 		String query = "SELECT * FROM Belts WHERE Course_id='" + courseID + "'";
 
 		try {
 			rs = stmt.executeQuery(query);
 			while (rs.next()) {
 				for (int i = 2; i < 22 && rs.getString(i).length() > 0; i++) {
 					belts.add(rs.getString(i));
 				}
 			}
 			rs.close();
 			dbc.close();
 			stmt.close();
 		} catch (Exception e) {
 			System.out.println(e);
 			return null;
 		}
 		return belts;
 	}
 
 	/**
 	 * Erstellt einen G�rteleintrag
 	 * 
 	 * @param courseID
 	 *            der G�rtel, ArraList<String> mit G�rtelfarben
 	 * 
 	 * 
 	 * @return true bei erfolg, false bei scheitern
 	 */
 	public boolean setBelts(int courseID, ArrayList<String> belts) {
 
 		DataBankerConnection dbc = new DataBankerConnection();
 		try {
 			PreparedStatement stmt = dbc
 
 					.getConnection()
 					.prepareStatement(
 							"INSERT INTO Belts(Course_id, grade1, grade2, grade3, grade4, grade5, grade6, grade7, grade8, grade9, grade10, grade11, grade12, grade13, grade14, grade15, grade16, grade17, grade18, grade19 ,grade20) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
 			stmt.setInt(1, courseID);
 			if (belts.isEmpty()) {
 				return false;
 			} else {
 
 				for (int i = 0; i < belts.size(); i++) {
 					stmt.setString(i + 2, belts.get(i));
 				}
 				for (int j = belts.size() + 2; j < 22; j++) {
 					stmt.setString(j, "");
 				}
 			}
 			stmt.executeUpdate();
 
 			dbc.close();
 			stmt.close();
 		} catch (SQLException e) {
 			System.out.println(e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Updated einen G�rteleintrag
 	 * 
 	 * @param courseID
 	 *            der G�rtel, ArrayList<String> mit den farben
 	 * 
 	 * 
 	 * @return true bei erfolg, false bei scheitern
 	 */
 	public boolean updateBelts(int courseID, ArrayList<String> belts) {
 		if (deleteBelts(courseID)) {
 			setBelts(courseID, belts);
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * L�scht einen G�rteleintrag
 	 * 
 	 * @param courseID
 	 *            der G�rtel
 	 * 
 	 * 
 	 * @return true bei erfolg, false bei scheitern
 	 */
 	public boolean deleteBelts(int courseID) {
 
 		DataBankerConnection dbc = new DataBankerConnection();
 		Statement stmt = dbc.getStatement();
 
 		String query = "DELETE FROM Belts WHERE Course_id='" + courseID + "'";
 
 		try {
 			stmt.executeUpdate(query);
 			dbc.close();
 			stmt.close();
 			dbc.closeStatement();
 
 		} catch (SQLException e) {
 			System.out.println(e);
 			return false;
 		}
 		return true;
 	}
 
 	public String nextBelt(int courseID, int lastBelt) {
 
 		int newBelt = lastBelt + 1;
 		String graduation = "grade" + newBelt;
 
 		DataBankerConnection dbc = new DataBankerConnection();
 		ResultSet rs = null;
 		Statement stmt = dbc.getStatement();
 
 		String query = "SELECT " + graduation + " FROM Belts WHERE Course_id='"
 				+ courseID + "'";
 
 		try {
 			rs = stmt.executeQuery(query);
 			while (rs.next()) {
 				return rs.getString(1);
 			}
 			rs.close();
 			dbc.close();
 			stmt.close();
 		} catch (Exception e) {
 			System.out.println(e);
 			return null;
 		}
 
 		return null;
 	}
 
 	public int getCourseID(String courseName) {
 		int courseID = 0;
 		DataBankerConnection dbc = new DataBankerConnection();
 		ResultSet rs = null;
 		Statement stmt = dbc.getStatement();
 
 		String query = "SELECT Courses_id FROM Courses WHERE name = '"
 				+ courseName + "'";
 
 		try {
 			rs = stmt.executeQuery(query);
 			while (rs.next()) {
 				courseID = rs.getInt(1);
 			}
 			rs.close();
 			dbc.close();
 			stmt.close();
 		} catch (Exception e) {
 			System.out.println(e);
 			return 0;
 		}
 		return courseID;
 	}
 
 	public String getCourseName(int courseID) {
 		String courseName = null;
 		DataBankerConnection dbc = new DataBankerConnection();
 		ResultSet rs = null;
 		Statement stmt = dbc.getStatement();
 
		String query = "SELECT name FROM Courses where Courses_id = '"
 				+ courseID + "'";
 
 		try {
 			rs = stmt.executeQuery(query);
 			while (rs.next()) {
 				courseName = rs.getString(1);
 			}
 			rs.close();
 			dbc.close();
 			stmt.close();
 		} catch (Exception e) {
 			System.out.println(e);
 			return null;
 		}
 		return courseName;
 	}
 }
