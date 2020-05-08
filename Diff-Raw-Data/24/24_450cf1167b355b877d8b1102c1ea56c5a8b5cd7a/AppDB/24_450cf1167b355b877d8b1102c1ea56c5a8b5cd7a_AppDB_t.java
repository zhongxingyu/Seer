 package brusd.mediummg.StudentCalendar.AppDatabase;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * Created with Android Studio.
  * User: MediumMG
  * Date: 05.10.13
  * Time: 22:01
  */
 public class AppDB {
 
     private Context context;
 
     private SQLiteDatabase appDB;
     private AppOpenHelper appOpenHelper;
 
     private static AppDB instance = null;
     public static AppDB getInstance(Context context) {
         if (instance == null) {
             synchronized (AppDB.class) {
                 if (instance == null) {
                     instance = new AppDB(context);
                 }
             }
         }
         return instance;
     }
 
     private AppDB(Context context) {
         this.context = context;
         this.appOpenHelper = new AppOpenHelper(context);
         appDB = appOpenHelper.getWritableDatabase();
     }
 
     public void open(){
         appDB = appOpenHelper.getWritableDatabase();
     }
 
     public void close(){
         appDB.close();
     }
 
     public void addTeacher(String teacherName) {
 
         if (teacherName == null)
             return;
         if (teacherName.isEmpty())
             return;
 
         if (!appDB.isOpen())
             open();
 
         teacherName = teacherName.replace("'","''");
 
         Cursor check = appDB.query(AppOpenHelper.TABLE_TEACHERS,
                                         new String[]{AppOpenHelper.TABLE_TEACHERS_COLUMN_Teacher_Name},
                                         AppOpenHelper.TABLE_TEACHERS_COLUMN_Teacher_Name + " = '" + teacherName + "'",
                                         null,
                                         null,
                                         null,
                                         null);
 
         int count = check.getCount();
         check.close();
 
         if (count == 0) {
             ContentValues values = new ContentValues();
             values.put(AppOpenHelper.TABLE_TEACHERS_COLUMN_Teacher_Name, teacherName);
             appDB.insert(AppOpenHelper.TABLE_TEACHERS, null, values);
         }
 
         close();
     }
 
     public void addLessons(String lessonName, String rgbColor) {
 
         if (lessonName == null)
             return;
         if (lessonName.isEmpty())
             return;
 
         if (rgbColor == null)
             rgbColor = "#FFFFFF";
 
         if (rgbColor.isEmpty())
             rgbColor = "#FFFFFF";
 
         if (!appDB.isOpen())
             open();
 
         lessonName = lessonName.replace("'","''");
 
         Cursor check = appDB.query(AppOpenHelper.TABLE_LESSONS,
                 new String[]{AppOpenHelper.TABLE_LESSONS_COLUMN_Lesson_Name},
                 AppOpenHelper.TABLE_LESSONS_COLUMN_Lesson_Name + " = '" + lessonName + "'",
                 null,
                 null,
                 null,
                 null);
 
         int count = check.getCount();
         check.close();
 
         if (count == 0) {
             ContentValues values = new ContentValues();
             values.put(AppOpenHelper.TABLE_LESSONS_COLUMN_Lesson_Name, lessonName);
             values.put(AppOpenHelper.TABLE_LESSONS_COLUMN_Lesson_Color, rgbColor);
             appDB.insert(AppOpenHelper.TABLE_LESSONS, null, values);
         }
 
         close();
     }
 
     public void addTemplates(String templateName, ArrayList<HashMap<String, Object>> lessons) {
 
         if (templateName == null)
             return;
         if (templateName.isEmpty())
             return;
 
         if(lessons.size() == 0)
             return;
 
         if (!appDB.isOpen())
             open();
 
         templateName = templateName.replace("'","''");
 
         Cursor check = appDB.query(AppOpenHelper.TABLE_TEMPLATES,
                                     new String[]{AppOpenHelper.TABLE_TEMPLATES_COLUMN_Template_Name},
                                     AppOpenHelper.TABLE_TEMPLATES_COLUMN_Template_Name + " = '" + templateName + "'",
                                     null,
                                     null,
                                     null,
                                     null);
 
         int count = check.getCount();
         check.close();
 
         if (count > 0)
             return;
 
         for (int i = 0; i < lessons.size(); i++) {
             HashMap <String, Object> curItem = lessons.get(i);
             ContentValues values = new ContentValues();
 
             int dayOfWeek = 0;
             if (curItem.containsKey(AppOpenHelper.TABLE_TEMPLATES_COLUMN_Week_Day))
                 dayOfWeek = (Integer)curItem.get(AppOpenHelper.TABLE_TEMPLATES_COLUMN_Week_Day);
             else
                 continue;
 
             int lessonNumber = 0;
             if (curItem.containsKey(AppOpenHelper.TABLE_TEMPLATES_COLUMN_Lesson_Number))
                 lessonNumber = (Integer)curItem.get(AppOpenHelper.TABLE_TEMPLATES_COLUMN_Lesson_Number);
             else
                 continue;
 
             String startTime = "";
             if (curItem.containsKey(AppOpenHelper.TABLE_TEMPLATES_COLUMN_Start_Time))
                 startTime = (String)curItem.get(AppOpenHelper.TABLE_TEMPLATES_COLUMN_Start_Time);
             else
                 continue;
 
             String endTime = "";
             if (curItem.containsKey(AppOpenHelper.TABLE_TEMPLATES_COLUMN_End_Time))
                 endTime = (String)curItem.get(AppOpenHelper.TABLE_TEMPLATES_COLUMN_End_Time);
             else
                 continue;
 
             int lessonID = 0;
             if (curItem.containsKey(AppOpenHelper.TABLE_TEMPLATES_COLUMN_Lesson_Name_ID))
                 lessonID = (Integer)curItem.get(AppOpenHelper.TABLE_TEMPLATES_COLUMN_Lesson_Name_ID);
             else
                 continue;
 
             int lessonTypeID = 0;
             if (curItem.containsKey(AppOpenHelper.TABLE_TEMPLATES_COLUMN_Lesson_Type_ID))
                 lessonTypeID = (Integer)curItem.get(AppOpenHelper.TABLE_TEMPLATES_COLUMN_Lesson_Type_ID);
             else
                 continue;
 
             int teacherID = 0;
             if (curItem.containsKey(AppOpenHelper.TABLE_TEMPLATES_COLUMN_Teacher_Name_ID))
                 teacherID = (Integer)curItem.get(AppOpenHelper.TABLE_TEMPLATES_COLUMN_Teacher_Name_ID);
             else
                 continue;
 
             values.put(AppOpenHelper.TABLE_TEMPLATES_COLUMN_Template_Name, templateName);
             values.put(AppOpenHelper.TABLE_TEMPLATES_COLUMN_Week_Day, dayOfWeek);
             values.put(AppOpenHelper.TABLE_TEMPLATES_COLUMN_Lesson_Number, lessonNumber);
             values.put(AppOpenHelper.TABLE_TEMPLATES_COLUMN_Start_Time, startTime);
             values.put(AppOpenHelper.TABLE_TEMPLATES_COLUMN_End_Time, endTime);
             values.put(AppOpenHelper.TABLE_TEMPLATES_COLUMN_Lesson_Name_ID, lessonID);
             values.put(AppOpenHelper.TABLE_TEMPLATES_COLUMN_Lesson_Type_ID, lessonTypeID);
             values.put(AppOpenHelper.TABLE_TEMPLATES_COLUMN_Teacher_Name_ID, teacherID);
 
             appDB.insert(AppOpenHelper.TABLE_TEMPLATES, null, values);
         }
 
         close();
     }
 
     public ArrayList<HashMap<String, Object>> getAllLessons() {
         if (!appDB.isOpen())
             open();
 
         Cursor cursor = appDB.query(AppOpenHelper.TABLE_LESSONS,
                 new String[]{
                         AppOpenHelper.TABLE_LESSONS_COLUMN_ID_Lesson,
                         AppOpenHelper.TABLE_LESSONS_COLUMN_Lesson_Name,
                         AppOpenHelper.TABLE_LESSONS_COLUMN_Lesson_Color},
                 null,
                 null,
                 null,
                 null,
                 AppOpenHelper.TABLE_LESSONS_COLUMN_Lesson_Name);
 
         ArrayList<HashMap<String, Object>> lessons = new ArrayList<HashMap<String, Object>>();
         while(cursor.moveToNext()) {
             HashMap<String, Object> temp = new HashMap<String, Object>();
             temp.put(AppOpenHelper.TABLE_LESSONS_COLUMN_ID_Lesson, cursor.getInt(0));
             temp.put(AppOpenHelper.TABLE_LESSONS_COLUMN_Lesson_Name, cursor.getString(1));
             temp.put(AppOpenHelper.TABLE_LESSONS_COLUMN_Lesson_Color, cursor.getString(2));
             lessons.add(temp);
         }
         cursor.close();
         close();
 
         return lessons;
     }
 
     public ArrayList<HashMap<String, Object>> getAllTeachers() {
         if (!appDB.isOpen())
             open();
 
         Cursor cursor = appDB.query(AppOpenHelper.TABLE_TEACHERS,
                 new String[]{
                         AppOpenHelper.TABLE_TEACHERS_COLUMN_ID_Teacher,
                         AppOpenHelper.TABLE_TEACHERS_COLUMN_Teacher_Name},
                 null,
                 null,
                 null,
                 null,
                 AppOpenHelper.TABLE_TEACHERS_COLUMN_Teacher_Name);
 
         ArrayList<HashMap<String, Object>> teachers = new ArrayList<HashMap<String, Object>>();
         while(cursor.moveToNext()) {
             HashMap<String, Object> temp = new HashMap<String, Object>();
             temp.put(AppOpenHelper.TABLE_TEACHERS_COLUMN_ID_Teacher, cursor.getInt(0));
             temp.put(AppOpenHelper.TABLE_TEACHERS_COLUMN_Teacher_Name, cursor.getString(1));
             teachers.add(temp);
         }
         cursor.close();
         close();
 
 
         return teachers;
     }
 
     public ArrayList<HashMap<String, Object>> getLessonsFor(String templateName, int dayOfWeek) {
         ArrayList<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
 
         if (templateName == null)
             return result;
         if (templateName.isEmpty())
             return result;
 
         if (dayOfWeek < 1 || dayOfWeek > 7)
             return result;
 
         if (!appDB.isOpen())
             open();
 
         templateName = templateName.replace("'","''");
 
         HashMap<Integer, String> teachers = getAllTeachersAsHashMap();
        HashMap<Integer, HashMap<String, String>> lessons = getAllLessonsAsHashMap();
 
         Cursor cursor = appDB.query(AppOpenHelper.TABLE_TEMPLATES,
                 new String[]{AppOpenHelper.TABLE_TEMPLATES_COLUMN_Lesson_Number,
                             AppOpenHelper.TABLE_TEMPLATES_COLUMN_Start_Time,
                             AppOpenHelper.TABLE_TEMPLATES_COLUMN_End_Time,
                             AppOpenHelper.TABLE_TEMPLATES_COLUMN_Lesson_Name_ID,
                             AppOpenHelper.TABLE_TEMPLATES_COLUMN_Teacher_Name_ID},
                 AppOpenHelper.TABLE_TEMPLATES_COLUMN_Template_Name + " = '" + templateName + "' AND " +
                 AppOpenHelper.TABLE_TEMPLATES_COLUMN_Week_Day + "  = '" + dayOfWeek + "'",
                 null,
                 null,
                 null,
                 AppOpenHelper.TABLE_TEMPLATES_COLUMN_Lesson_Number);
 
         while(cursor.moveToNext()) {
             HashMap<String, Object> temp = new HashMap<String, Object>();
             temp.put(AppOpenHelper.TABLE_TEMPLATES_COLUMN_Lesson_Number, cursor.getInt(0));
             temp.put(AppOpenHelper.TABLE_TEMPLATES_COLUMN_Start_Time, cursor.getString(1));
             temp.put(AppOpenHelper.TABLE_TEMPLATES_COLUMN_End_Time, cursor.getString(2));
            temp.put(AppOpenHelper.TABLE_LESSONS_COLUMN_Lesson_Name, lessons.get(cursor.getInt(3)).get(AppOpenHelper.TABLE_LESSONS_COLUMN_Lesson_Name));
            temp.put(AppOpenHelper.TABLE_LESSONS_COLUMN_Lesson_Color, lessons.get(cursor.getInt(3)).get(AppOpenHelper.TABLE_LESSONS_COLUMN_Lesson_Color));
             temp.put(AppOpenHelper.TABLE_TEACHERS_COLUMN_Teacher_Name, teachers.get(cursor.getInt(4)));
             result.add(temp);
         }
 
         cursor.close();
         close();
 
         return result;
     }
 
    private HashMap<Integer, HashMap<String, String>> getAllLessonsAsHashMap() {
        HashMap<Integer, HashMap<String, String>> result = new HashMap<Integer, HashMap<String, String>>();
 
         Cursor cursor = appDB.query(AppOpenHelper.TABLE_LESSONS,
                 new String[]{
                         AppOpenHelper.TABLE_LESSONS_COLUMN_ID_Lesson,
                        AppOpenHelper.TABLE_LESSONS_COLUMN_Lesson_Name,
                        AppOpenHelper.TABLE_LESSONS_COLUMN_Lesson_Color},
                 null,
                 null,
                 null,
                 null,
                 AppOpenHelper.TABLE_LESSONS_COLUMN_ID_Lesson);
 
         while(cursor.moveToNext()) {
             int id = cursor.getInt(0);

            HashMap<String, String> temp = new HashMap<String, String>();
            temp.put(AppOpenHelper.TABLE_LESSONS_COLUMN_Lesson_Name, cursor.getString(1));
            temp.put(AppOpenHelper.TABLE_LESSONS_COLUMN_Lesson_Color, cursor.getString(2));
            result.put(id, temp);
         }
         cursor.close();
 
         return result;
     }
 
     private HashMap<Integer, String> getAllTeachersAsHashMap() {
         HashMap<Integer, String> result = new HashMap<Integer, String>();
 
         Cursor cursor = appDB.query(AppOpenHelper.TABLE_TEACHERS,
                 new String[]{
                         AppOpenHelper.TABLE_TEACHERS_COLUMN_ID_Teacher,
                         AppOpenHelper.TABLE_TEACHERS_COLUMN_Teacher_Name},
                 null,
                 null,
                 null,
                 null,
                 AppOpenHelper.TABLE_TEACHERS_COLUMN_ID_Teacher);
 
         while(cursor.moveToNext()) {
             int id = cursor.getInt(0);
             String name = cursor.getString(1);
             result.put(id, name);
         }
         cursor.close();
 
         return result;
     }
 
 
 }
