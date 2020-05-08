 package edu.sdsmt.cs293.example7.database;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class Model extends SQLiteOpenHelper
 {
 
 	public static final String KEY_ID = "CourseID";
 	public static final String KEY_NUMBER = "CourseNumber";
 	
 	private static final String TAG = "Example7.Database";
 
 	private static final String DATABASE_NAME = "Example7.Database.db";
 	private static final int DATABASE_VERSION = 1;
 	private static final String TABLE_MYCOURSES = "Courses";
 
 	private static final String TABLE_CREATE_MYCOURSES =
 			        "CREATE TABLE " +
 			        TABLE_MYCOURSES +
 					"(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
 					KEY_NUMBER + " TEXT);";
 
 	private SQLiteDatabase _db;
 	private static Model _instance;
 	
 	/**
 	 * Class defined to allow for the passing of class data between
 	 * the model, activity, and fragments.  Also, used to define the 
 	 * contents of an ArrayAdapter.
 	 * @author brianb
 	 *
 	 */
 	public static class Course implements Comparator<Course>
 	{
 		public Course()
 		{
 			ID = -1;
 		}
 		
 		public Course(long id)
 		{
 			ID = id;
 		}
 		
 		public long ID;
 		public String CourseNumber;
 		
 		// Used by ArrayAdapter to determine
 		// what to display in the list.
 		@Override
 		public String toString() 
 		{
 		    return CourseNumber;
 		}
 
 		@Override
 		public int compare(Course lhs, Course rhs)
 		{
 			return lhs.CourseNumber.compareTo(rhs.CourseNumber);
 		}
 	}
 
 	public Model(Context context)
 	{
 		// Call the parent class and pass the actual name and version of the
 		// database to be created. The version will be used in the future for
 		// determine whether onUpgrade() is called from the SQLiteOpenHelper
 		// extension.
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db)
 	{
 		// Execute the CREATE TABLE statement defined as a const.
 		db.execSQL(TABLE_CREATE_MYCOURSES);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
 	{
 		// If there is ever a need to upgrade the database and/or table.
 		// Compare old and new versions to determine if modifications
 		// to the database are necessary. Typically, this will be done with
 		// ALTER TABLE or CREATE TABLE SQL statements depending on the
 		// change required.
 
 		if (newVersion == 2)
 		{
 			// No version 2 upgrade process yet.
 		}
 	}
 	
 	public static synchronized Model getInstance(Context context)
 	{
 		// Used to synchronize access and force singleton on the 
 		// database helper object.
 		if (_instance == null)
 		{
 			_instance = new Model(context);
 		}
 		
 		return _instance;
 	}
 
 	public void insertCourse(Course course)
 	{
 		// Take parameters and pass to method to populate the
 		// ContentValues data structure.
 		ContentValues values = populateContentValues(course, true);
 
 		// Open the database connect, keep it close to the actual operation.
 		openDBConnection();
 
 		// Execute query to update the specified course.
 		long id = _db.insert(TABLE_MYCOURSES, null, values);
 
 		Log.d(TAG, "CourseID inserted = " + String.valueOf(id));
 
 		// Close the database connection as soon as possible.
 		closeDBConnection();
 
 	}
 
 	public void insertSampleCourses()
 	{
 		Course course;
 		
 		course = new Course();
 		course.CourseNumber = "CS150";
 		insertCourse(course);
 		
 		course = new Course();
 		course.CourseNumber = "CS250";
 		insertCourse(course);
 		
 		course = new Course();
 		course.CourseNumber = "CS251";
 		insertCourse(course);
 		
 		course = new Course();
 		course.CourseNumber = "CS300";
 		insertCourse(course);
 		
 		course = new Course();
 		course.CourseNumber = "CS492";
 		insertCourse(course);
 	}
 
 	public void updateCourse(Course course)
 	{
 		// Take parameters and pass to method to populate the
 		// ContentValues data structure.
 		ContentValues values = populateContentValues(course, false);
 
 		// Open the database connect, keep it close to the actual operation.
 		openDBConnection();
 
 		// Execute query to update the specified course.
 		int rowsAffected = _db.update(TABLE_MYCOURSES,
 										values,
 										KEY_ID + " = ?",
 										new String[] { String.valueOf(course.ID) });
 
 		// Close the database connection as soon as possible.
 		closeDBConnection();
 
 		if (rowsAffected == 0)
 		{
 			// The course row was not updated, what should be done?
 			Log.d(TAG, "Course not updated!");
 		}
 	}
 
 	public void deleteCourse(Course course)
 	{
 		// Open the database connect, keep it close to the actual operation.
 		openDBConnection();
 
 		// Execute query to delete the specified course.
 		int rowsAffected = _db.delete(TABLE_MYCOURSES,
 										KEY_ID + " = ?",
 										new String[] { String.valueOf(course.ID) });
 
 		// Close the database connection as soon as possible.
 		closeDBConnection();
 
 		if (rowsAffected == 0)
 		{
 			// The course row was not deleted, what should be done?
 			Log.d(TAG, "Course not deleted!");
 		}
 	}
 
 	public Course getCourse(long id)
 	{
 		Course course = null;
 		
 		openDBConnection();
 		
 		// Return the specific course row based on ID passed.
 		// _id is required by SimpleCursorAdaptor.
 		Cursor cursor = _db.query(TABLE_MYCOURSES,
 				                  new String[] { KEY_ID, KEY_NUMBER},
 				                  null,
 				                  null,
 								  null,
 								  null,
 								  KEY_NUMBER);
 		
 		if (cursor.moveToFirst())
 		{
 			course = cursorToContact(cursor);
 		}
 		
 		cursor.close();
 		closeDBConnection();
 
 		return course;
 	}
 	
 	public List<Course> getCourses()
 	{
 		List<Course> courses = new ArrayList<Course>();
 		
 		openDBConnection();
 		
 		// Query for a list of courses.
 		Cursor cursor = _db.query(TABLE_MYCOURSES,
 								  new String[] { KEY_ID, KEY_NUMBER},
 								  null,
 								  null,
 								  null,
 								  null,
 								  KEY_NUMBER);
 		
 		// Populate the course List by iterating through Cursor.
 		cursor.moveToFirst();
 	    while (cursor.isAfterLast() == false) 
 	    {
 	    	Course course = cursorToContact(cursor);
 			courses.add(course);
 		    cursor.moveToNext();
 		}
 		
 		cursor.close();
 		closeDBConnection();
 		
 		return courses;
 	}
 
 	private void openDBConnection()
 	{
 		// Opens connection to the database for writing specifically.
 		_db = getWritableDatabase();
 	}
 
 	private void closeDBConnection()
 	{
 		if (_db != null && _db.isOpen() == true)
 		{
 			// Close connection to database if open.
 			_db.close();
 		}
 	}
 
 	private Course cursorToContact(Cursor cursor)
 	{
 		Course course = new Course(cursor.getLong(cursor.getColumnIndex(KEY_ID))); 
 		course.CourseNumber = cursor.getString(cursor.getColumnIndex(KEY_NUMBER)); 
 		
 		return course;
 	}
 
 	private ContentValues populateContentValues(Course course, boolean inserting)
 	{
 		// Common function used to populate the ContentValues to be used in SQL
 		// insert or update methods.
 		
		String temp = " | UPDATED!";
		if (inserting == true)
		{
			temp = "";
		}
		
 		ContentValues values = new ContentValues();
<<<<<<< HEAD
		values.put(KEY_NUMBER, course.CourseNumber + temp);
=======
 		values.put(KEY_NUMBER, course.CourseNumber);
>>>>>>> Reworked the Fragments need for referencing Model.
 
 		return values;
 	}
 
 }
