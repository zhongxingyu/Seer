 package database;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 
 public class Database 
 {
 	private CourseTable courses;
 	private StudentTable students;
 	private GradeTable grades;
 //	private Map<String, Table> tables; //will hold all tables
 	
 	public Database()
 	{
 		this.courses = new CourseTable();
 		this.students = new StudentTable();
 		this.grades = new GradeTable();
 //		this.tables = new HashMap<String, Table>();
 //			this.tables.put("courses", this.courses);
 //			this.tables.put("students", this.students);
 //			this.tables.put("grades", this.grades);
 	}
 	
 	// Load from file.  Throws error if location doesn't exist or fails to load
 //	public Database(Folder location)
 //	{
 //		
 //	}
 	
 	// Saves database to file. Throws error if location doesn't exist or save fails
 //	public void save(Folder location)
 //	{
 //		
 //	}
 	
 	// Checks to make sure required fields (for the unique ids) are present and
 	// fills in missing ones with blanks
	public void insert(String table, ArrayList<String> fields, ArrayList<Object> values)
 	{
 		// Using courses because all tables have same orderValues method
 		values = this.courses.orderValues(fields, values);
 		if(table == "courses")
 			this.courses.insert(values);
 		else if(table == "students")
 			this.students.insert(values);
 		else if(table == "grades")
 		{
 			
 			// TODO: Verify this castings stuff
 			if(!(this.students.uniqueIdExists(new Integer((String) values.get(0)))))
 				throw new IllegalArgumentException("Error: student ID must exist in students table");
 			if(!(this.courses.uniqueIdExists((String) values.get(0))))
 				throw new IllegalArgumentException("Error: courses number must exist in courses table");
 			this.grades.insert(values);
 		}
 		else
 			// TODO: Raise error, not valid table
 			throw new IllegalArgumentException("Error: Invalid table, must be courses, students, or grades.");
 	}
 	
 	// Adds values in "standard" order must at least have required unique entries for table
	public void insert(String table, ArrayList<Object> values)
 	{
 		if(table == "courses")
 			this.courses.insert(values);
 		else if(table == "students")
 			this.students.insert(values);
 		else if(table == "grades")
 		{
 			if(!(this.students.records.get("student").contains(values.get(0))))
 				// TODO: throw error
 				;
 			else if(!(this.students.records.get("course").contains(values.get(1))))
 				// TODO: throw error
 				;
 			this.grades.insert(values);
 		}
 		else
 			throw new IllegalArgumentException("Error: Invalid table, must be courses, students, or grades.");
 	}
 	
 	// Throw error if field doesn't exist, value is the wrong type, table doesn't exist,
 	// or field is a uniqueId
 	public <T> void update(String table, String field, T value)
 	{
 		
 	}
 	
 	// Throw error if types aren't right or things are missing
 	public <T,E> void update(String table, T uniqueId, String field, E value)
 	{
 		
 	}
 	
 	// Throw error if deleting from students or courses if student or course exist in
 	// in the respective columns in grades
 	public <T extends Object> void delete(String table, T uniqueId)
 	{
 		if(table == "courses")
 			
 			this.courses.delete((String) uniqueId);
 		else if(table == "students")
 			this.students.delete((Integer) uniqueId);
 		else if(table == "grades")
 			// TODO: Extra checks for grades table
 			;
 		else
 			// TODO: Raise error, not valid table
 			throw new IllegalArgumentException("Error: Invalid table, must be courses, students, or grades.");
 	}
 	
 //	public ArrayList<?> getField(String table, String field)
 //	{
 //		return ArrayList<?>;
 //	}
 	public static void main(String[] args)
 	{
 		Database test = new Database();
 		ArrayList<Object> values = new ArrayList<Object>();
 			values.add((Integer) 88694986);
 			values.add("Luke");
 			values.add((Integer) 2013);
 		ArrayList<String> fields = new ArrayList<String>();
 			fields.add("student");
 			fields.add("first");
 			fields.add("year");
 		test.insert("students", fields, values);
 		System.out.println("student: " + test.students.getField("student").toString());
 		System.out.println("first: " + test.students.getField("first").toString());
 		System.out.println("last: " + test.students.getField("last").toString());
 		System.out.println("age: " + test.students.getField("age").toString());
 		System.out.println("year: " + test.students.getField("year").toString());
 	}
 
 }
