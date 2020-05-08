 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.v2.database;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import java.util.ArrayList;
 import no.hials.muldvarp.v2.database.tables.*;
 import no.hials.muldvarp.v2.domain.*;
 
 /**
  * This class is responsible for managing the SQLITE data source, integrating it
  * with the Muldvarp application Domain.
  * @author johan
  */
 public class MuldvarpDataSource {
 
     // Database fields
     private SQLiteDatabase database;
     private MuldvarpDBHelper dbHelper;  
 
     public MuldvarpDataSource(Context context) {
         dbHelper = MuldvarpDBHelper.getInstance(context);
     }
 
     public void open() throws SQLException {
         database = dbHelper.getWritableDatabase();    
     }
 
     public void close() {
         dbHelper.close();
     }
     
     /**
      * This method generates unix time.
      * @return long unix time
      */
     public long getTimeStamp() {        
         return (System.currentTimeMillis() / 1000L);
     }
     
     public long createRelation(String tableName, String table1, long id1, String table2, long id2){
         ContentValues values = new ContentValues();
         values.put(table1, id1);
         values.put(table2, id2);
         
         return database.insert(tableName, null, values);
     }
     
     /** 
      * This method implements creatRelation for CourseTable and ProgrammeTable.
      * @param id1
      * @param id2
      * @return long id
      */
     public long createProgrammeCourseRelation(long id1, long id2){
         return createRelation(ProgrammeHasCourseTable.TABLE_NAME,
                 ProgrammeTable.TABLE_NAME + MuldvarpTable.COLUMN_ID, id1,
                 CourseTable.TABLE_NAME + MuldvarpTable.COLUMN_ID, id2);
     }
     
     /** 
      * This method implements creatRelation for CourseTable and TopicTable.
      * @param id1
      * @param id2
      * @return long id
      */
     public long createCourseTopicRelation(long id1, long id2){
         return createRelation(CourseHasTopicTable.TABLE_NAME,
                 CourseTable.TABLE_NAME + MuldvarpTable.COLUMN_ID, id1,
                 TopicTable.TABLE_NAME + MuldvarpTable.COLUMN_ID, id2);
     }
     
     /**
      * This function checks if a given value exists in a given field in a given table.
      * Returns true if the record(s) exist, returns false if not.
      * 
      * @param table
      * @param field
      * @param value
      * @return 
      */
     public boolean checkRecord(String table, String field, String value){
         Cursor cursor = database.rawQuery("SELECT * FROM " 
                 + table + " WHERE " 
                 + field + " = '" 
                 + value + "'", null);
         System.out.println("cursor size " + cursor.getCount());
         return (cursor.getCount() > 0);
     }
     
     /**
      * This function inserts a Programme into the SQLITE database, and if the database
      * record already exists, updates the table instead.
      * 
      * @param programme
      * @return primary key id
      */
     public long insertProgramme(Programme programme) {
         
         //Get text/int value fields from Domain and insert into table
         ContentValues values = new ContentValues();
         values.put(ProgrammeTable.COLUMN_ID, programme.getId());
        values.put(ProgrammeTable.COLUMN_ID, programme.getProgrammeId());
         values.put(ProgrammeTable.COLUMN_NAME, programme.getName());
         values.put(ProgrammeTable.COLUMN_DESCRIPTION, programme.getDescription());
         values.put(ProgrammeTable.COLUMN_REVISION, programme.getRevision());
         values.put(ProgrammeTable.COLUMN_UPDATED, getTimeStamp());
         long insertId;
         if(checkRecord(ProgrammeTable.TABLE_NAME, ProgrammeTable.COLUMN_NAME, programme.getName())){
             System.out.println("updating " + programme.getName());
             insertId = database.update(ProgrammeTable.TABLE_NAME, values,
                     ProgrammeTable.COLUMN_ID + "='" + getProgrammeId(programme) + "'",
                     null);
         } else {
             System.out.println("inserting " + programme.getName());
             insertId = database.insert(ProgrammeTable.TABLE_NAME, null,
             values);
         }
         
         //Inserts the courses in the Programme and sets up relations       
         ArrayList<Course> courseList = (ArrayList<Course>) programme.getCourses();
         
         if (courseList != null) {
             for (int i = 0; i < courseList.size(); i++) {
                 createProgrammeCourseRelation(insertId, insertCourse(courseList.get(i)));
             }
         }
         
         return insertId;
     }
 
     public void deleteProgramme(Programme programme) {
         
         //Now deletes based on name
         String[] name = {programme.getName()};
         System.out.println("programme deleted with name: " + name);
         database.delete(ProgrammeTable.TABLE_NAME, ProgrammeTable.COLUMN_NAME
             + "=?", name);
     }
 
     public Programme getProgrammeByName(String name){
         Cursor cursor = database.rawQuery("SELECT * FROM " 
                 + ProgrammeTable.TABLE_NAME 
                 + " WHERE " + ProgrammeTable.COLUMN_NAME 
                 + " = '"+name+"'", null);
         Programme retVal = cursorToProgramme(cursor);
         return retVal;
     }
     
     public Programme getProgrammeByUniqueId(String name){
         Cursor cursor = database.rawQuery("SELECT * FROM " 
                 + ProgrammeTable.TABLE_NAME 
                 + " WHERE " + ProgrammeTable.COLUMN_UNIQUEID 
                 + " = '"+name+"'", null);
         Programme retVal = cursorToProgramme(cursor);
         return retVal;
     }
     
     public long getProgrammeId(Programme programme){
                 
         System.out.println(programme.getName());
         Cursor cursor = database.rawQuery("SELECT "
                 + "*" + " FROM " 
                 + ProgrammeTable.TABLE_NAME 
                 + " WHERE " + ProgrammeTable.COLUMN_NAME 
                 + " = '"+programme.getName()+"'", null);
         
         cursor.moveToFirst();
         System.out.println("size " + cursor.getCount());
         long retVal = cursor.getLong(0);
         cursor.close();
         
         return retVal;
     }
 
     public ArrayList<Domain> getAllProgrammes() {
         ArrayList<Domain> programmes = new ArrayList<Domain>();
 
         Cursor cursor = database.query(ProgrammeTable.TABLE_NAME ,
             MuldvarpDBHelper.getColumns(ProgrammeTable.TABLE_COLUMNS),
             null, null, null, null, null);
 
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
         Programme programme = cursorToProgramme(cursor);
         programmes.add(programme);
         cursor.moveToNext();
         }
         // Make sure to close the cursor
         cursor.close();
         return programmes;
     }
 
     public long insertCourse(Course course) {
         ContentValues values = new ContentValues();
         values.put(CourseTable.COLUMN_ID, course.getId());
         values.put(CourseTable.COLUMN_UNIQUEID, course.getCourseId());  
         values.put(CourseTable.COLUMN_NAME, course.getName());                
         values.put(CourseTable.COLUMN_REVISION, course.getRevision());
         values.put(CourseTable.COLUMN_UPDATED, getTimeStamp());
         long insertId;
         if(checkRecord(CourseTable.TABLE_NAME, CourseTable.COLUMN_NAME, course.getName())){
             insertId = database.update(CourseTable.TABLE_NAME, values,
                     CourseTable.COLUMN_ID + "='" + getCourseByName(null) + "'",
                     null);
             System.out.println("updating " + course.getName());
 
         } else {
             System.out.println("inserting " + course.getName());
             insertId = database.insert(CourseTable.TABLE_NAME, null,
             values);
         }
         
         //Set up relation etc      
         ArrayList<Topic> topicList = (ArrayList<Topic>) course.getTopics();
         
         if (topicList != null) {
             for (int i = 0; i < topicList.size(); i++) {
                 createCourseTopicRelation(insertId, insertTopic(topicList.get(i)));
             }
         }        
         return insertId;
     }
 
     public void deleteCourse(Course course) {
         //Now deletes based on name
         String[] name = {course.getName()};
         System.out.println("course deleted with name: " + name);
         database.delete(CourseTable.TABLE_NAME, CourseTable.COLUMN_NAME
             + "=?", name);
     }
 
     public Course getCourseByName(String name){
         Cursor cursor = database.rawQuery("SELECT * FROM " 
                 + CourseTable.TABLE_NAME 
                 + " WHERE " + CourseTable.COLUMN_NAME 
                 + " = '"+name+"'", null);
         Course retVal = cursorToCourse(cursor);
         return retVal;
     }
     
     public Course getCourseByUniqueId(String name){
         Cursor cursor = database.rawQuery("SELECT * FROM " 
                 + CourseTable.TABLE_NAME 
                 + " WHERE " + CourseTable.COLUMN_UNIQUEID 
                 + " = '"+name+"'", null);
         Course retVal = cursorToCourse(cursor);
         return retVal;
     }
     
     public long getCourseId(Course course){
                 
         System.out.println(course.getName());
         Cursor cursor = database.rawQuery("SELECT "
                 + "*" + " FROM " 
                 + CourseTable.TABLE_NAME 
                 + " WHERE " + CourseTable.COLUMN_NAME 
                 + " = '"+course.getName()+"'", null);
         
         cursor.moveToFirst();
         System.out.println("size " + cursor.getCount());
         long retVal = cursor.getLong(0);
         cursor.close();
         
         return retVal;
     }
     
     public ArrayList<Domain> getCoursesByProgramme(Programme programme){
         
         System.out.println("getprogrammeid " + getProgrammeId(programme));
         
         System.out.println("SELECT * FROM " + ProgrammeHasCourseTable.TABLE_NAME);
         Cursor cursorTest = database.query(ProgrammeHasCourseTable.TABLE_NAME, null, null, null, null, null, null);
         System.out.println("asdasdasdasdsada " + cursorTest.getColumnCount());
         System.out.println("derrrrrrr " + cursorTest.getCount());
         
         
         //Course ID column is the second one
         String[] column = new String[] { ProgrammeHasCourseTable.TABLE_COLUMNS[2][0] };
         String[] selectionArgs = new String[] {
         "1" };
         //Query for course ID's in programme
         Cursor cursor = database.query(ProgrammeHasCourseTable.TABLE_NAME,
                 column,
                 ProgrammeHasCourseTable.TABLE_COLUMNS[1][0] + "= ?",
                 selectionArgs,
                 null, null, null);
         
         cursor.moveToFirst();
         System.out.println("GET COURSES BY PROGRAM SIZE " + cursor.getCount());
         ArrayList<Domain> courses = new ArrayList<Domain>();
         while (!cursor.isAfterLast()) {
             
             System.out.println("SELECT * FROM "
                     + CourseTable.TABLE_NAME
                     + " WHERE " + CourseTable.COLUMN_ID
                     + "='" + cursor.getString(0) + "'");
             
             Cursor currentCursor = database.rawQuery("SELECT * FROM "
                     + CourseTable.TABLE_NAME
                     + " WHERE " + CourseTable.COLUMN_ID
                     + "='" + cursor.getString(0) + "'",
                     null);
             currentCursor.moveToFirst();
             Course course = cursorToCourse(currentCursor);
             courses.add(course);
             cursor.moveToNext();
         }
         // Make sure to close the cursor
         cursor.close();
         return courses;
     }
     
     /**
      * This function inserts a Topic into the SQLITE database, and if the database
      * record already exists, updates the table instead.
      * 
      * @param programme
      * @return primary key id
      */
     public long insertTopic(Topic topic) {
         
         //Get text/int value fields from Domain and insert into table
         ContentValues values = new ContentValues();
         values.put(TopicTable.COLUMN_ID, topic.getId());
         values.put(TopicTable.COLUMN_NAME, topic.getName());
         values.put(TopicTable.COLUMN_DESCRIPTION, topic.getDescription());
         values.put(TopicTable.COLUMN_UPDATED, getTimeStamp());
         long insertId;
         if(checkRecord(TopicTable.TABLE_NAME, TopicTable.COLUMN_NAME, topic.getName())){
             System.out.println("updating " + topic.getName());
             insertId = database.update(TopicTable.TABLE_NAME, values,
                     TopicTable.COLUMN_ID + "='" + getTopicId(topic) + "'",
                     null);
         } else {
             System.out.println("inserting " + topic.getName());
             insertId = database.insert(TopicTable.TABLE_NAME, null,
             values);
         }        
         
         return insertId;
     }
     
         public Topic getTopic(String name){
         Cursor cursor = database.rawQuery("SELECT * FROM " 
                 + TopicTable.TABLE_NAME 
                 + " WHERE " + TopicTable.COLUMN_NAME 
                 + " = '"+name+"'", null);
         Topic retVal = cursorToTopic(cursor);
         return retVal;
     }
     
     public long getTopicId(Topic topic){
                 
         System.out.println(topic.getName());
         Cursor cursor = database.rawQuery("SELECT "
                 + "*" + " FROM " 
                 + TopicTable.TABLE_NAME 
                 + " WHERE " + TopicTable.COLUMN_NAME 
                 + " = '"+topic.getName()+"'", null);
         
         cursor.moveToFirst();
         System.out.println("size " + cursor.getCount());
         long retVal = cursor.getLong(0);
         cursor.close();
         
         return retVal;
     }
     
     public long insertUser(User user) {        
         ContentValues values = new ContentValues();
         values.put(UserTable.COLUMN_NAME, user.getName());        
         values.put(UserTable.COLUMN_PASSWORD, user.getPassword());
         
         long insertId;
         if(checkRecord(UserTable.TABLE_NAME, UserTable.COLUMN_NAME, user.getName())){
             insertId = database.update(UserTable.TABLE_NAME, values, null, null);
                     System.out.println("updating " + user.getName());
         } else {
                     System.out.println("inserting " + user.getName());
             insertId = database.insert(UserTable.TABLE_NAME, null,
             values);
         }
         String[] columns = MuldvarpDBHelper.getColumns(UserHasCourseTable.TABLE_COLUMNS);        
         ArrayList<Domain> domainList = (ArrayList<Domain>) user.getUserDomains();
         
         if (domainList != null) {
             for (int i = 0; i < domainList.size(); i++) {
                 values = new ContentValues();
                 values.put(columns[1], insertId);
                 values.put(columns[2], 1);
                 database.insert(UserHasCourseTable.TABLE_NAME, null, values);
             }
         }
         
         return insertId;
     }
     
     public void deleteUser(User user) {
         long id = user.getId();
         System.out.println("User deleted with id: " + id);
         database.delete(UserTable.TABLE_NAME, UserTable.COLUMN_ID
             + " = " + id, null);
     }
     
     public User getUser(String name){
         Cursor cursor = database.rawQuery("SELECT * FROM " 
                 + UserTable.TABLE_NAME 
                 + " WHERE " + ProgrammeTable.COLUMN_NAME 
                 + " = '"+name+"'", null);
         User retVal = cursorToUser(cursor);
         return retVal;
     }
     
     public ArrayList<Course> getCoursesFromUser(int userId){
         
         String[] columns = new String[] { CourseTable.TABLE_NAME + CourseTable.COLUMN_ID };
         String[] selectionArgs = new String[] {
         String.valueOf(userId) };
         Cursor cursor = database.query(UserHasCourseTable.TABLE_NAME, columns,
         UserTable.TABLE_NAME + UserTable.COLUMN_ID + "= ?", selectionArgs, null,
         null, null);        
         
         ArrayList<Course> courses = new ArrayList<Course>();
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
             Course course = cursorToCourse(cursor);
             courses.add(course);
             cursor.moveToNext();
         }
         // Make sure to close the cursor
         cursor.close();
         
         return courses;
         
     }
 
     private Programme cursorToProgramme(Cursor cursor) {
         Programme programme = new Programme();
         int id = (int) cursor.getLong(0);
         programme.setId(id);
         programme.setProgrammeId(cursor.getString(1));
         programme.setName(cursor.getString(2));     
         programme.setDescription(cursor.getString(4));
         programme.setRevision(cursor.getInt(5));
         return programme;
     }
 
     private Course cursorToCourse(Cursor cursor) {
         Course course = new Course();
         int id = (int) cursor.getLong(0);
         course.setId(id);
         course.setCourseId(cursor.getString(1));
         course.setName(cursor.getString(2));
         course.setRevision(cursor.getInt(3));
         return course;
     }
 
     private Topic cursorToTopic(Cursor cursor) {
         Topic topic = new Topic();
         int id = (int) cursor.getLong(0);
         topic.setId(id);
         topic.setName(cursor.getString(1));
         return topic;
     }
 
     private Video cursorToVideo(Cursor cursor) {
         Video video = new Video();
         int id = (int) cursor.getLong(0);
         video.setId(id);
         video.setName(cursor.getString(1));
         return video;
     }
     
     private User cursorToUser(Cursor cursor) {
         User user = new User();
         int id = (int) cursor.getLong(0);
         user.setId(id);
         user.setName(cursor.getString(1));        
         user.setPassword(cursor.getString(2));
         
         return user; 
     }
 }
