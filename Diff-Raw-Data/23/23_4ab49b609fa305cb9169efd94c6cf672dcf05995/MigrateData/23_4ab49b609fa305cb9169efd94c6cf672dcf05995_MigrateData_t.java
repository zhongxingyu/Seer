 /**
  * @file CaseTesting.java
  * @author wangsha
  * @date Oct 11, 2011 
  */
 package test;
 
 import java.sql.ResultSet;
 import java.util.ArrayList;
 
 import object.DatabaseStructure;
 import object.Schema;
 import object.TableStructure;
 import script.Converter;
 import database.MySQLConnector;
 import database.Redis;
 
 /**
  * @author wangsha
  *
  */
 public class MigrateData {
 	public static void main(String[] args) {
 		try {
 			String cmd = "redis-server";
 			Runtime run = Runtime.getRuntime();
 			Process pr;
 			try {
 				pr = run.exec(cmd);
 				pr.waitFor();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			MySQLConnector sql = new MySQLConnector("127.0.0.1", 3306, Config.db_user,
 					Config.db_pwd, Config.db_name);
 			Redis redis = new Redis(Config.redis_server);
 			
 			DatabaseStructure db = new DatabaseStructure();
 			db.getStructure(sql);
 			
 			Converter converter = new Converter(sql, redis);
 			
 			//Clean up redis.db
 			redis.flushDB();
 			/**
 			 * Convert Department(id, name)
 			 */
 			ResultSet data = sql.query("SELECT id, name FROM department WHERE 1");
 			Schema dept = new Schema("department", new String[]{"id", "name"}, new String[]{"id"});
 			converter.toHashSet("department", dept, data);
 			//store largest dept_id
 			data = sql.query("SELECT MAX(id) FROM department WHERE 1");
 			data.next();
 			redis.set("deptid", data.getString(1));
			System.out.println(redis.hgetAll("department"));
 			
 			/**
 			 * Convert Course (id, title, dept_id)
 			 * 1. create a hashset for EACH course
 			 * 2. create a set for course code
 			 */
 			data = sql.query("SELECT code, title, dept_id FROM course WHERE 1 ORDER BY code ASC");
 			Schema course = new Schema("course", new String[] {"code", "title", "dept_id"}, new String[]{"code"});
 			converter.toHashSets(course, data);
 			data = sql.query("SELECT code FROM course WHERE 1");
 			converter.toSet("course_code", data);
 		
 			//System.out.println(redis.hgetAll("course:CS1010"));
 			
 			/**
 			 * Convert mentor(student_id, staff_id) many-to-one relationship
 			 * 1. create a hashset for stu_id => stf_id query  {1:3 , 2:3, 3:3}
 			 * 2. create a set for each stf_id for stf_id => stu_id query mentor:stf_id:3 => set (1, 2, 3)
 			 */
 			data = sql.query("SELECT stu_id, stf_id FROM mentor");
 			Schema stustf = new Schema("mentor", new String[] {"stu_id", "stf_id"}, new String[] {"stu_id"});
 			converter.toHashSet("mentor", stustf, data);
			System.out.println(redis.hgetAll("mentor"));
 			
 			data = sql.query("SELECT stu_id, stf_id FROM mentor");
 			Schema stfstu = new Schema("mentor", new String[] {"stu_id", "stf_id"}, new String[] {"stf_id"});
 			converter.createReveserMappingSet(stfstu, "stf_id", data);
			System.out.println(redis.smembers("mentor:stf_id:3"));
 			
 			/**
 			 * Convert student, join person table with student table and delete person table in key-value store
 			 * 1. create a hashset for EACH student
 			 * 2. create a hastset for nric=>id mapping  student:nric_id => {S6543287A:1, S2324131B:2}
 			 * 3. create a hashset for matric=>id mapping student:matric_id => {U076123A:1, U084321S:2}
 			 * 4. create a hashset for name=>id mapping student:name_id => {Alice:1, Benny:2, Charlie:3}
 			 */
 			data = sql.query("SELECT id, name, student.nric, dob, dept_id, matric_no " +
 					"FROM student, person WHERE student.nric = person.nric");
 			Schema student = new Schema("student", new String[] {"id", "name", "nric", "dob", "dept_id", "matric_no"}, 
 					new String[] {"id"});
 			converter.toHashSets(student, data);
 			//store largest studentid
 			data = sql.query("SELECT MAX(id) FROM student WHERE 1");
 			data.next();
 			redis.set("studentid", data.getString(1));
 			System.out.println(redis.get("studentid"));
 			//System.out.println(redis.hgetAll("student:1"));
 			
 			data = sql.query("SELECT s.nric, id FROM student s, person p WHERE s.nric = p.nric");
 			Schema nric_id = new Schema("student:nric", new String[]{"nric", "id"}, new String[]{"nric"});
 			converter.toHashSet("student:nric_id", nric_id, data);
 			//System.out.println(redis.hgetAll("student:nric_id"));
 			
 			data = sql.query("SELECT matric_no, id FROM student s, person p WHERE s.nric = p.nric");
 			Schema matric_id = new Schema("student:matric_id", new String[]{"matric_no", "id"}, new String[]{"matric_no"});
 			converter.toHashSet("student:matric_id", matric_id, data);
 			//System.out.println(redis.hgetAll("student:matric_id"));
 			
 			data = sql.query("SELECT name, id FROM student s, person p WHERE s.nric = p.nric");
 			Schema name_id = new Schema("student:name_id", new String[]{"name", "id"}, new String[]{"name"});
 			converter.toHashSet("student:name_id", name_id, data);
 			//System.out.println(redis.hgetAll("student:name_id"));
 			
 			/**
 			 * Convert staff, join person table with staff table and delete person table in key-value store
 			 * 1. create a hashset for EACH student
 			 * 2. create a hastset for nric=>id mapping  student:nric_id => {S6543287A:1, S2324131B:2}
 			 * 3. create a hashset for name=>id mapping student:name_id => {Alice:1, Benny:2, Charlie:3}
 			 */
 			data = sql.query("SELECT id, name, staff.nric, dob, dept_id " +
 					"FROM staff, person WHERE staff.nric = person.nric");
 			Schema staff = new Schema("staff", new String[] {"id", "name", "nric", "dob", "dept_id"}, 
 					new String[]{"id"});
 			converter.toHashSets(staff, data);
 			//store largest staffid
 			data = sql.query("SELECT MAX(id) FROM staff WHERE 1");
 			data.next();
 			redis.set("staffid", data.getString(1));
 			//System.out.println(redis.hgetAll("staff:2"));
 			
 			data = sql.query("SELECT s.nric, id FROM staff s, person p WHERE s.nric = p.nric");
 			nric_id = new Schema("staff:nric", new String[]{"nric", "id"}, new String[]{"nric"});
 			converter.toHashSet("staff:nric_id", nric_id, data);
 			//System.out.println(redis.hgetAll("staff:nric_id"));
 			
 			data = sql.query("SELECT name, id FROM staff s, person p WHERE s.nric = p.nric");
 			name_id = new Schema("staff:name_id", new String[]{"name", "id"}, new String[]{"name"});
 			converter.toHashSet("staff:name_id", name_id, data);
 			//System.out.println(redis.hgetAll("staff:name_id"));
 			
 			/**
 			 * Convert take(sid, cid, mark) relationship with attributes
 			 * 1. create a hashset for EACH sid take:student:1 => {CS1010:90, CS2103:80}
 			 * 2. create a hashset for EACH cid take:course:CS1010 => {1:90, 3:70}
 			 */
 			data = sql.query("SELECT sid, cid, mark FROM take WHERE 1");
 			Schema stake = new Schema("take:student", new String[] {"sid", "cid", "mark"}, 
 					new String[]{"sid"});
 			converter.toValueHash("take:student", stake, data);
 			//System.out.println(redis.hgetAll("take:student:1"));
 			
 			data = sql.query("SELECT cid, sid, mark FROM take WHERE 1");
 			Schema ctake = new Schema("take:course", new String[] {"cid", "sid", "mark"}, 
 					new String[]{"cid"});
 			converter.toValueHash("take:course", ctake, data);
 			//System.out.println(redis.hgetAll("take:course:CS1010"));
 			
 			
 			/**
 			 * Convert tutorial_room(dept_id, room_id, capacity, location, year_start_use)
 			 * 1. create a hashset for EACH tutorial room, use both dept_id, room_id as key
 			 */
 			data = sql.query("SELECT dept_id, room_id, capacity, location, year_start_use FROM tutorial_room WHERE 1");
 			Schema tm = new Schema("tutorial_room", new String[] {"dept_id", "room_id", 
 			"capacity", "location", "year_start_use"}, new String[] {"dept_id", "room_id"});
 			converter.toHashSets(tm, data);
 			System.out.println(redis.hgetAll("tutorial_room:1:1"));
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		System.out.println("Done");
 
 	}
 }
