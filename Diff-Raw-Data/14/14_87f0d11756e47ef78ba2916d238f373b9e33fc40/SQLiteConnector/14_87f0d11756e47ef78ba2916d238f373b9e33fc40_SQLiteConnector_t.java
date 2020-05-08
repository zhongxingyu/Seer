 package com.example.haw_app.stisysManager.persistenz;
 
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 import com.example.haw_app.Util;
 
 public class SQLiteConnector extends SQLiteOpenHelper {
 //TODO NextApplicationDates, RegisteredTrainigs, SolvedTrainings,SolvedTests,Mailinglists
 	public SQLiteConnector(Context context) {
 		super(context, dbName, null, dbversion);
 		
 	}
 	private static final String KEY_GROUP = "gruppe";
 	private static final String TABLE_COURSES = "Courses";
 	private static final int GWS = 0, REGISTERED_TESTS = 1, WPS = 2;
 	private static final String dbName = "StiSysManagerDB";
 	private static final int dbversion = 2;
 	private static final String[] tableName = new String[] {"Student","NextApplicationDates",TABLE_COURSES,
 		"Trainings","SolvedTests","SolvedTrainings","Mailinglists"};
 	private static final String[] columns = new String[] {
 		"( name VARCHAR , avgGrade REAL , userName VARCHAR , password VARCHAR , matNr INT , pwExpDate VARCHAR , birthday VARCHAR , printCredit INT )",
 		"( type VARCHAR , fromDate VARCHAR , toDate VARCHAR )",//NextApplicationDates
 		"( name VARCHAR , type INT , prof VARCHAR , status VARCHAR )",//Courses
 		"( name VARCHAR , prof VARCHAR , status VARCHAR , "+KEY_GROUP+" INT )", //registeredTrainings		
 		"( name VARCHAR , grade INT )",//solvedTests
 		"( name VARCHAR , status INT )", //solvedTrainings
 		"( name VARCHAR , adress VARCHAR )"//mailinglists
 		}; 
 	
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i<tableName.length;i++)
 		{		
 			sb.delete(0, sb.length());
 			sb.append("CREATE TABLE IF NOT EXISTS ").append(tableName[i]).append(" ").append(columns[i]).append(";");
 			db.execSQL(sb.toString());
 		}	
 //		String[] sary = this.getDBNames();
 //		for (String s : sary)
 //		{
 //			Log.d("DBNames", "s");
 //		}
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase arg0, int oldVersion, int newVersion) {
 		if (oldVersion == 1 && newVersion >= 2)
 		{
 			arg0.execSQL("alter table Student add column password VARCHAR default ''");			
 
 		}
 	}
 	
 	public boolean saveNextApplicationDates(Map<String,DateTime[]> map)
 	{
 		if(map== null)
 			return false;
 		boolean ret = true;
 		SQLiteDatabase db = this.getWritableDatabase();
 		for (Map.Entry<String, DateTime[]> e : map.entrySet())
 		{
 			ContentValues values = new ContentValues();
 			values.put("type", e.getKey());
 			values.put("fromDate", Util.getStringFromDateTime(e.getValue()[0]));
 			values.put("toDate",Util.getStringFromDateTime(e.getValue()[1]));
 			if (db.insert("NextApplicationDates", null, values)==-1)
 				ret = false;
 		}
 		return ret;
 	}
 
 	public boolean saveRegisteredTrainings(Map<String,String[]> map)
 	{
 		if(map== null)
 			return false;
 		boolean ret = true;
 		SQLiteDatabase db = this.getWritableDatabase();
 		for (Map.Entry<String, String[]> e : map.entrySet())
 		{
 			ContentValues values = new ContentValues();
 			values.put("name", e.getKey());
 			values.put("prof", e.getValue()[0]);
			String _tmp = e.getValue()[1];
 			values.put(KEY_GROUP,Integer.parseInt(e.getValue()[1]));
 			values.put("status",e.getValue()[2]);
 			if (db.insert("Trainings", null, values)==-1)
 				ret = false;
 		}
 		return ret;
 	}
 
 
 	public boolean saveSolvedTrainings(Map<String,Boolean> map)
 	{
 		if(map== null)
 			return false;
 		boolean ret = true;
 		SQLiteDatabase db = this.getWritableDatabase();
 		for (Map.Entry<String, Boolean> e : map.entrySet())
 		{
 			ContentValues values = new ContentValues();
 			values.put("name", e.getKey());
 			values.put("status", (e.getValue())?1:0);			
 			if (db.insert("SolvedTrainings", null, values)==-1)
 				ret = false;
 		}
 		return ret;
 	}
 
 
 	public boolean saveSolvedTests(Map<String,Integer> map)
 	{
 		if(map== null)
 			return false;
 		boolean ret = true;
 		SQLiteDatabase db = this.getWritableDatabase();
 		for (Map.Entry<String, Integer> e : map.entrySet())
 		{
 			ContentValues values = new ContentValues();
 			values.put("name", e.getKey());
 			values.put("grade", e.getValue());			
 			if (db.insert("SolvedTests", null, values)==-1)
 				ret = false;
 		}
 		return ret;
 	}
 
 
 	public boolean saveMailinglists(Map<String,String> map)
 	{
 		if(map== null)
 			return false;
 		boolean ret = true;
 		SQLiteDatabase db = this.getWritableDatabase();
 		for (Map.Entry<String, String> e : map.entrySet())
 		{
 			ContentValues values = new ContentValues();
 			values.put("name", e.getKey());
 			values.put("adress", e.getValue());			
 			if (db.insert("Mailinglists", null, values)==-1)
 				ret = false;
 		}
 		return ret;
 	}
 	
 	
 	
 	
 	
 	public Map<String,DateTime[]> getNextApplicationDates()
 	{
 		Map<String,DateTime[]> ret = null;
 		SQLiteDatabase db = this.getReadableDatabase();				
 		Cursor cursor = db.rawQuery("SELECT type, fromDate, toDate FROM `NextApplicationDates`", null);		
 		if (cursor.getCount()!=0)
 		{			
 			ret = new HashMap<String, DateTime[]>();
 			while (cursor.moveToNext()){
 				String name;
 				name = cursor.getString(cursor.getColumnIndex("type"));
 				DateTime[] ary = new DateTime[]{Util.createDateTimeFromString(cursor.getString(cursor.getColumnIndex("fromDate"))),Util.createDateTimeFromString(cursor.getString(cursor.getColumnIndex("toDate")))};
 				ret.put(name, ary);
 			}
 			
 		}
 		return ret;
 	}
 
 	public Map<String,String[]> getRegisteredTrainings()
 	{
 		//prof status group
 		SQLiteDatabase db = this.getReadableDatabase();
 		Map<String,String[]> ret = null;		
 		// ( name VARCHAR , prof VARCHAR , status VARCHAR , "+KEY_GROUP+" INT )
 		Cursor cursor = db.rawQuery("SELECT name , prof , "+KEY_GROUP+" , status FROM `Trainings`", null);		
 		if (cursor.getCount()!=0)
 		{
 			ret = new HashMap<String, String[]>();
 			while (cursor.moveToNext()){
 				String name;
 				name = cursor.getString(cursor.getColumnIndex("name"));
 				String[] ary = new String[]{cursor.getString(cursor.getColumnIndex("prof")),Integer.toString(cursor.getInt(cursor.getColumnIndex(KEY_GROUP))),cursor.getString(cursor.getColumnIndex("status"))};
 				ret.put(name, ary);
 			}
 		}
 		return ret;
 	}
 	//TODO rausschmeien
 	public String[] getColumnNames()
 	{
 		String[] ary = null;
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor ti = db.rawQuery("PRAGMA table_info(Trainings)", null);
 		ary = new String[ti.getColumnCount()];
 		int i = 0;
 		if ( ti.moveToFirst() ) {
 		    do {
 		        ary[i++] = "col: " + ti.getString(1);
 		    } while (ti.moveToNext());
 		}
 		return ary;
 	}
 	
 	public Map<String,Boolean> getSolvedTrainings()
 	{
 		SQLiteDatabase db = this.getReadableDatabase();
 		Map<String,Boolean> ret = null;		
 		// name VARCHAR, type INT, prof VARCHAR, status VARCHAR)
 		Cursor cursor = db.rawQuery("SELECT name, status FROM `SolvedTrainings`", null);		
 		if (cursor.getCount()!=0)
 		{
 			ret = new HashMap<String, Boolean>();
 			while (cursor.moveToNext()){
 				String name;
 				name = cursor.getString(cursor.getColumnIndex("name"));
 				Boolean ary = (cursor.getInt(cursor.getColumnIndex("status"))==0)?false:true;
 				ret.put(name, ary);
 			}
 		}
 		return ret;
 	}
 	
 	public Map<String,Integer> getSolvedTests()
 	{
 		SQLiteDatabase db = this.getReadableDatabase();
 		Map<String,Integer> ret = null;		
 		// name VARCHAR, type INT, prof VARCHAR, status VARCHAR)
 		Cursor cursor = db.rawQuery("SELECT name, grade FROM `SolvedTests`", null);		
 		if (cursor.getCount()!=0)
 		{
 			ret = new HashMap<String, Integer>();
 			while (cursor.moveToNext()){
 				String name;
 				name = cursor.getString(cursor.getColumnIndex("name"));
 				Integer ary = cursor.getInt(cursor.getColumnIndex("grade"));
 				ret.put(name, ary);
 			}
 		}
 		return ret;
 	}
 	
 	public Map<String,String>getMailinglists()
 	{
 		SQLiteDatabase db = this.getReadableDatabase();
 		Map<String,String> ret = null;		
 		Cursor cursor = db.rawQuery("SELECT name, adress FROM `Mailinglists`", null);		
 		if (cursor.getCount()!=0)
 		{
 			ret = new HashMap<String, String>();
 			while (cursor.moveToNext()){
 				String name;
 				name = cursor.getString(cursor.getColumnIndex("name"));
 				String ary = cursor.getString(cursor.getColumnIndex("adress"));
 				ret.put(name, ary);
 			}
 		}
 		return ret;
 	}
 	
 	public boolean clearNextApplicationDates()
 	{
 		SQLiteDatabase db = this.getWritableDatabase();		
 		return (db.delete("NextApplicationDates", null, null)==0)?false:true;		
 	}
 	
 
 	public boolean clearRegisteredTrainings()
 	{
 		SQLiteDatabase db = this.getWritableDatabase();		
 		return (db.delete("Trainings", null, null)==0)?false:true;		
 	}
 	
 
 	public boolean clearSolvedTrainings()
 	{
 		SQLiteDatabase db = this.getWritableDatabase();		
 		return (db.delete("SolvedTrainings", null, null)==0)?false:true;		
 	}
 	
 
 	public boolean clearSolvedTests()
 	{
 		SQLiteDatabase db = this.getWritableDatabase();		
 		return (db.delete("SolvedTests", null, null)==0)?false:true;		
 	}
 	
 	public boolean clearMailinglists()
 	{
 		SQLiteDatabase db = this.getWritableDatabase();		
 		return (db.delete("Mailinglists", null, null)==0)?false:true;		
 	}
 	
 	
 	
 	public Map<String, String[]> getSocialSciencesCourses()
 	{
 		return getCourses(GWS);
 	}
 	
 	public boolean clearSocialSienceCourses()
 	{
 		return clearCourses(GWS);
 	}
 	
 	public boolean saveSocialSienceCourses(Map<String,String[]> courses)
 	{
 		return saveCourses(courses, GWS);
 	}
 	
 	public Map<String, String[]> getRegisteredTests()
 	{
 		return getCourses(REGISTERED_TESTS);
 	}
 	
 	public boolean clearRegisteredTests()
 	{
 		return clearCourses(REGISTERED_TESTS);
 	}
 	
 	public boolean saveRegisteredTests(Map<String,String[]> tests)
 	{
 		return saveCourses(tests, REGISTERED_TESTS);
 	}
 	
 	public Map<String, String[]> getChosenCourses()
 	{
 		return getCourses(WPS);
 	}
 	
 	public boolean clearChosenCourses()
 	{
 		return clearCourses(WPS);
 	}
 	
 	public boolean saveChosenCourses(Map<String,String[]> courses)
 	{
 		return saveCourses(courses, WPS);
 	}
 	
 	private Map<String,String[]> getCourses(int type)
 	{
 		SQLiteDatabase db = this.getReadableDatabase();
 		Map<String,String[]> ret = null;		
 		// name VARCHAR, type INT, prof VARCHAR, status VARCHAR)
 		Cursor cursor = db.rawQuery("SELECT name, prof, status FROM `"+TABLE_COURSES+"` where type = "+ type, null);		
 		if (cursor.getCount()!=0)
 		{
 			ret = new HashMap<String, String[]>();
 			while (cursor.moveToNext()){
 				String name;
 				name = cursor.getString(cursor.getColumnIndex("name"));
 				String[] ary = new String[]{cursor.getString(cursor.getColumnIndex("prof")),cursor.getString(cursor.getColumnIndex("status"))};
 				ret.put(name, ary);
 			}
 		}
 		return ret;
 	}
 	
 	private boolean clearCourses(int type)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		return (db.delete(TABLE_COURSES, "type = ?", new String[]{""+type})==0)?false:true;		
 	}
 	
 	private boolean saveCourses(Map<String,String[]> courses,int type)
 	{
 		if(courses== null)
 			return false;
 		boolean ret = true;
 		SQLiteDatabase db = this.getWritableDatabase();
 		for (Map.Entry<String, String[]> e : courses.entrySet())
 		{
 			ContentValues values = new ContentValues();
 			values.put("name", e.getKey());
 			values.put("prof", e.getValue()[0]);
 			values.put("status",e.getValue()[1]);
 			values.put("type", type);
 			if (db.insert(TABLE_COURSES, null, values)==-1)
 				ret = false;
 		}
 		return ret;
 	}
 	
 	
 	public String getName()
 	{
 		return this.getStudent().name;
 	}
 	
 	public String getUserName()
 	{
 		return this.getStudent().userName;
 	}
 	
 	public double getAverageGrade()
 	{
 		return this.getStudent().avgGrade;
 	}
 	
 	public int getMatricularNumber()
 	{
 		return this.getStudent().matNr;
 	}
 	
 	public DateTime getPasswordExpirationDate()
 	{
 		return this.getStudent().pwExpDate;
 	}
 		
 	public DateTime getBirthday()
 	{
 		return this.getStudent().birthdate;
 	}
 	
 	public int getPrintCredit()
 	{
 		return this.getStudent().printCredit;
 	}
 	
 	public String getPassword()
 	{
 		return this.getStudent().pw; 
 	}
 	
 	public boolean setPassword(String password)
 	{
 		Student std = this.getStudent();
 		std.pw = password;
 		return (saveStudent(std)>0)?true:false;
 	}
 	
 	public boolean setName(String name)
 	{
 		Student std = this.getStudent();
 		std.name = name;		
 		return (saveStudent(std)>0)?true:false;
 	}
 	
 	public boolean setUserName(String userName)
 	{
 		Student std = this.getStudent();
 		std.userName= userName;		
 		return (saveStudent(std)>0)?true:false;
 	}
 	
 	public boolean setAverageGrade(double avgGrade)
 	{
 		Student std = this.getStudent();
 		std.avgGrade = avgGrade;
 		return (saveStudent(std)>0)?true:false;
 	}
 	
 	public boolean setMatriculationNumber(int matNr)
 	{
 		Student std = this.getStudent();
 		std.matNr = matNr;		
 		return (saveStudent(std)>0)?true:false;
 	}
 	
 	public boolean setPasswordExpirationDate(DateTime date)
 	{
 		Student std = this.getStudent();
 		std.pwExpDate = date;
 		return (saveStudent(std)>0)?true:false;
 	}
 	
 	public boolean setBirthday(DateTime date)
 	{
 		Student std = this.getStudent();
 		std.birthdate = date;
 		return (saveStudent(std)>0)?true:false;
 	}
 	
 	public boolean setPrintCredit(int creditsInCent)
 	{
 		Student std = this.getStudent();
 		std.printCredit= creditsInCent;
 		return (saveStudent(std)>0)?true:false;
 	}
 	
 	private int saveStudent(Student std)
 	{		
 		 ContentValues val = new ContentValues();
 		 val.put(Student.NAME, std.name);
 		 val.put(Student.AVERAGEGRADE, std.avgGrade);
 		 val.put(Student.USERNAME, std.userName);
 		 val.put(Student.MATRIKELNUMMER, std.matNr);
 		 DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
 		 val.put(Student.PASSWORD_EXP_DATE, fmt.print(std.pwExpDate));
 		 val.put(Student.BIRTHDAY, fmt.print(std.birthdate));
 		 val.put(Student.PRINTCREDIT, std.printCredit);
 		 val.put(Student.PASSWORD, std.pw);
 		 SQLiteDatabase db = this.getWritableDatabase();
 		 db.delete(Student.TABLENAME, null, null);
 		 int i = (int)(db.insert(Student.TABLENAME, null, val));
 		 db =  null;
 		 return i;
 		 		
 	}
 	
 	private Student getStudent()
 	{
 		SQLiteDatabase db = this.getReadableDatabase();
 		Student std = new Student();		
 		Cursor cursor = db.rawQuery("SELECT "+Student.columnList()+" FROM Student", null);
 		db = null;
 		if (cursor.getCount()!=0)
 		{
 			cursor.moveToNext();							
 			std.name = cursor.getString(cursor.getColumnIndex(Student.NAME));				
 			std.avgGrade = cursor.getDouble(cursor.getColumnIndex(Student.AVERAGEGRADE));
 			std.userName = cursor.getString(cursor.getColumnIndex(Student.USERNAME));
 			std.matNr = cursor.getInt(cursor.getColumnIndex(Student.MATRIKELNUMMER));
 			String pwExpDate = cursor.getString(cursor.getColumnIndex(Student.PASSWORD_EXP_DATE));		
 			std.pwExpDate= Util.createDateTimeFromString(pwExpDate);
 			String birthday = cursor.getString(cursor.getColumnIndex(Student.BIRTHDAY));
 			std.birthdate= Util.createDateTimeFromString(birthday);
 			std.printCredit = cursor.getInt(cursor.getColumnIndex(Student.PRINTCREDIT));
 			std.pw = cursor.getString(cursor.getColumnIndex(Student.PASSWORD));		
 		}else{
 			saveStudent(std);
 		}		
 		return std;
 	}
 	
 	public String[] getDBNames(){
 	    String[] result = null;
 	    try {
 	    	SQLiteDatabase _db = this.getReadableDatabase();
 	        StringBuilder sb = new StringBuilder();
 	        sb.append("SELECT name FROM sqlite_master "); 
 	        sb.append("WHERE type IN ('table','view') AND name NOT LIKE 'sqlite_%' ");
 	        sb.append("UNION ALL ");
 	        sb.append("SELECT name FROM sqlite_temp_master "); 
 	        sb.append("WHERE type IN ('table','view') ");
 	        sb.append("ORDER BY 1");
 
 	        Cursor c = _db.rawQuery(sb.toString(), null);
 	        c.moveToFirst();
 
 	        result = new String[c.getCount()];
 	        int i = 0;
 	        while (c.moveToNext()) {
 	            result[i]= c.getString(c.getColumnIndex("name"));
 	            i++;
 	        }
 	        c.close();
 	    }
 	    catch(SQLiteException e){
 	        Log.e("OOPS", e.getMessage());
 	    }
 	    return result;
 	}
 }
 
 class Student{
 	
 	static final String NAME = "name", USERNAME = "userName", AVERAGEGRADE ="avgGrade", MATRIKELNUMMER = "matNr", 
 			PASSWORD_EXP_DATE = "pwExpDate", BIRTHDAY = "birthday", PRINTCREDIT = "printCredit", PASSWORD  = "password";
 	static final String TABLENAME ="Student";
 	String name,userName,pw;
 	double avgGrade;
 	int matNr, printCredit;
 	DateTime birthdate, pwExpDate;
 	
 	static String columnList()
 	{
 		StringBuilder sb = new StringBuilder();
 		sb.append(NAME).append(", ").append(USERNAME).append(", ").append(AVERAGEGRADE).append(", ").append(MATRIKELNUMMER)
 		.append(", ").append(PASSWORD_EXP_DATE).append(", ").append(BIRTHDAY).append(", ").append(PRINTCREDIT).append(", ").append(PASSWORD);
 		return sb.toString();
 	}
 	
 	public Student()
 	{
 		name = null;
 		userName = null;
 		avgGrade = 0;
 		matNr = 0;
 		printCredit = 0;
 		birthdate = null;
 		pwExpDate = null;
 		pw = null;
 	}
 	
 	
 }
