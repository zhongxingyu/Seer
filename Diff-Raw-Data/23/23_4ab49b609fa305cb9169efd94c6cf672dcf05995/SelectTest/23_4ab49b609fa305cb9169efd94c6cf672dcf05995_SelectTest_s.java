 /**
  * @file SelectTest.java
  * @author wangsha
  * @date Oct 12, 2011 
  */
 package test;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 
 import javax.naming.spi.DirStateFactory.Result;
 
 import database.MySQLConnector;
 import database.Redis;
 
 /**
  * @author wangsha
  *
  */
 public class SelectTest {
 	private MySQLConnector sql;
 	private Redis redis;
 	private boolean debug_on = true;
 	
 	public SelectTest() {
 		String cmd = "redis-server";
 		Runtime run = Runtime.getRuntime();
 		Process pr;
 		try {
 			pr = run.exec(cmd);
 			pr.waitFor();
 			sql = new MySQLConnector("127.0.0.1", 3306, Config.db_user,
 					Config.db_pwd, Config.db_name);
 			redis = new Redis(Config.redis_server);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 	
 	// find the course title for CS1010
 	public void findCourseTitle() throws SQLException {
 		ResultSet data = sql.query("SELECT title FROM course WHERE code = 'CS1010'");
 		data.next();
 		printResult(data.getString(1), redis.hget("course:CS1010", "title"));
 	}
 	
 	// find the department which offer the course 'Programming Methodology
 	public void findCourseDepartment() throws SQLException {
 		
 		// SQL
 		ResultSet data = sql.query("select department.`name` FROM course, department " +
 				"WHERE course.title = 'Programming Methodology' AND  course.dept_id = department.id");
 		data.next();
 		String sname = data.getString(1);
 		
 		String rname = null;
 		// Redis
 		Set<String> codes = redis.smembers("course_code");
 		Iterator<String> it = codes.iterator();
 		while(it.hasNext()) {
 			String code = it.next();
 			if( redis.hget("course:"+code, "title").equals("Programming Methodology")) {
 				String dept_id = redis.hget("course:"+code, "dept_id");
 				rname = redis.hget("department", dept_id);
 				break;
 			}
 		}
 		
 		printResult(sname, rname);
 	}
 	
 	//// find the average score for CS2103
 	public void findAverageCourseMark() throws SQLException {
 		ResultSet data = sql.query("SELECT AVG(mark) FROM take WHERE cid = 'CS2103'");
 		data.next();
 		String res1 = data.getString(1);
 		
 		// Redis
 		Map<String, String> map = redis.hgetAll("take:course:CS2103");
 		Iterator<String> it = map.values().iterator();
 		int total = 0;
 		while (it.hasNext()) {
 			total+= Integer.parseInt(it.next());
 		}
 		String res2 = Float.toString((float) total/map.size());
 		printResult(res1, res2);
 		
 	}
 	
 	// find the average score for all courses Ethan Lim had taken
 	public void findAverageStudentMark() throws SQLException {
 		ResultSet data = sql.query("SELECT AVG(mark) FROM take, person, student WHERE" +
 				" person.`name`='Ethan Lim' AND person.nric = student.nric " +
 				"AND student.id = take.sid");
 		data.next();
 		String res1 = data.getString(1);
 		
 		// Redis
 		String sid = redis.hget("student:name_id", "Ethan Lim");
 		Map<String, String> map = redis.hgetAll("take:student:"+sid);
 		Iterator<String> it = map.values().iterator();
 		int total = 0;
 		while (it.hasNext()) {
 			total+= Integer.parseInt(it.next());
 		}
 		String res2 = Float.toString((float) total/map.size());
 		printResult(res1, res2);
 		
 	}
 	
 	// find all student (their matric number) whose mentor is Eva Tay
 	public void findStudentMetor() throws SQLException {
 		ResultSet data = sql.query("SELECT student.matric_no " +
 				"FROM person, staff, mentor, student " +
 				"WHERE person.name = 'Eva Tay' " +
 				"AND person.nric = staff.nric " +
 				"AND staff.id = mentor.stf_id " +
 				"AND mentor.stu_id = student.id");
 		String res1 = "\n";
 		while(data.next()) {
 			res1 += data.getString(1) + "\n";
 		}
 		
 		// Redis
 		String sid = redis.hget("staff:name_id", "Eva Tay");
 		Set<String> ids = redis.smembers("mentor:stf_id:"+sid);
 		Iterator<String> it = ids.iterator();
 		String res2 = "\n";
 		while (it.hasNext()) {
 			res2 += redis.hget("student:"+it.next(), "matric_no")+"\n";
 		}
 		printResult(res1, res2);
 		
 	}
 	
 	public void findDepartmentTutorialCapacity() throws SQLException {
 		ResultSet data = sql.query("SELECT d1.name, SUM(t1.capacity) " +
 				"FROM department d1,tutorial_room t1 " +
 				"WHERE d1.id = t1.dept_id " +
 				"GROUP BY d1.id");
 		String res1 ="\n";
 		while(data.next()) {
 			res1 += data.getString(1) + ": " + data.getString(2) + "\n";
 		}
 		
 		//Redis
 		String res2 = "\n";
 		Set<String> keys = redis.keys("tutorial_room:*");
 		Iterator<String> it = keys.iterator();
 		Map<String, LinkedList<String>> idMap = new HashMap<String, LinkedList<String>>();
 		while(it.hasNext()) {
 			String[] ids = it.next().split(":");
 			if(!idMap.containsKey(ids[1])) {
 				idMap.put(ids[1], new LinkedList<String>());
 			}
 			idMap.get(ids[1]).add(ids[2]);
 		}
 		Iterator<String> dept_ids = idMap.keySet().iterator();
 		while(dept_ids.hasNext()) {
 			String dept_id = dept_ids.next();
 			Iterator<String> rids = idMap.get(dept_id).iterator();
 			String dept_name = redis.hget("department", dept_id);
 			res2 += dept_name;
 			int total = 0;
 			while(rids.hasNext()) {
 				total += Integer.parseInt(redis.hget("tutorial_room:"+dept_id+":"+rids.next(), "capacity"));
 			}
 			res2 += ": "+Integer.toString(total)+"\n";
 		}
 		
 		printResult(res1, res2);
 	}
 	private void printResult(String sqlRst, String redisRst) {
 		System.out.println("SQL result: " + sqlRst + "\n" + "RDS result: " + redisRst);
 	}
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		SelectTest test = new SelectTest();
 		try {
			//test.findCourseTitle();
			//test.findCourseDepartment();
			//test.findAverageCourseMark();
			//test.findAverageStudentMark();
			//test.findStudentMetor();
 			test.findDepartmentTutorialCapacity();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 }
