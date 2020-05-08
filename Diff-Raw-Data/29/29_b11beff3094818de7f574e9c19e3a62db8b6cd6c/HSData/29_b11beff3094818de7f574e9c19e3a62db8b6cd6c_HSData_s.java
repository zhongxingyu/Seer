 package knaps.hacker.school.data;
 
 import android.provider.BaseColumns;
 
 /**
  * Created by lisaneigut on 14 Sep 2013.
  */
 public final class HSData {
 
     public HSData() {}
 
     public static final String TEXT_TYPE = " TEXT";
     public static final String INT_TYPE = " INTEGER";
     public static final String PRIMARY_KEY_TYPE = INT_TYPE + " PRIMARY KEY";
     public static final String COMMA_SEP = ", ";
     public static final String STMT_CREATE_TABLE = "CREATE TABLE ";
     public static final String STMT_DROP_TABLE = "DROP TABLE IF EXISTS ";
     public static final String STMT_SELECT = "SELECT ";
     public static final String STMT_FROM = " FROM ";
     public static final String STMT_WHERE = " WHERE ";
     public static final String STMT_INSERT = "INSERT INTO ";
     public static final String STMT_IS_NOT_NULL = " IS NOT NULL ";
     public static final String STMT_LIMIT = " LIMIT ";
     public static final String STMT_AND = " AND ";
     public static final String STMT_EQUALS_Q = " = ?";
 
 
     public static abstract class HSer implements BaseColumns {
         public static final String TABLE_NAME = "students";
         public static final String COLUMN_NAME_ID = "id";
         public static final String COLUMN_NAME_FULL_NAME = "fullName";
         public static final String COLUMN_NAME_IMAGE_URL = "imageUrl";
         public static final String COLUMN_NAME_IMAGE_FILENAME = "imageFilename";
         public static final String COLUMN_NAME_JOB = "job";
         public static final String COLUMN_NAME_JOB_URL = "jobUrl";
         public static final String COLUMN_NAME_SKILLS = "skills";
         public static final String COLUMN_NAME_EMAIL = "email";
         public static final String COLUMN_NAME_TWITTER = "twitterUrl";
         public static final String COLUMN_NAME_GITHUB = "githubUrl";
         public static final String COLUMN_NAME_BATCH = "batchName";
         public static final String COLUMN_NAME_BATCH_ID = "batchId";
 
         public static final String _ALL =
                 _ID + COMMA_SEP +
                 COLUMN_NAME_ID + COMMA_SEP +
                 COLUMN_NAME_FULL_NAME + COMMA_SEP +
                 COLUMN_NAME_IMAGE_URL + COMMA_SEP +
                 COLUMN_NAME_IMAGE_FILENAME + COMMA_SEP +
                 COLUMN_NAME_JOB + COMMA_SEP +
                 COLUMN_NAME_JOB_URL + COMMA_SEP +
                 COLUMN_NAME_SKILLS + COMMA_SEP +
                 COLUMN_NAME_EMAIL + COMMA_SEP +
                 COLUMN_NAME_GITHUB + COMMA_SEP +
                 COLUMN_NAME_TWITTER + COMMA_SEP +
                 COLUMN_NAME_BATCH_ID + COMMA_SEP +
                 COLUMN_NAME_BATCH
                 ;
 
         public static final String SQL_CREATE =
                 STMT_CREATE_TABLE + TABLE_NAME + " (" +
                 _ID + PRIMARY_KEY_TYPE + COMMA_SEP +
                 COLUMN_NAME_ID + INT_TYPE + COMMA_SEP +
                 COLUMN_NAME_FULL_NAME + TEXT_TYPE + COMMA_SEP +
                 COLUMN_NAME_IMAGE_URL + TEXT_TYPE + COMMA_SEP +
                 COLUMN_NAME_IMAGE_FILENAME + TEXT_TYPE + COMMA_SEP +
                 COLUMN_NAME_JOB + TEXT_TYPE + COMMA_SEP +
                 COLUMN_NAME_JOB_URL + TEXT_TYPE + COMMA_SEP +
                 COLUMN_NAME_SKILLS + TEXT_TYPE + COMMA_SEP +
                 COLUMN_NAME_EMAIL + TEXT_TYPE + COMMA_SEP +
                 COLUMN_NAME_GITHUB + TEXT_TYPE + COMMA_SEP +
                 COLUMN_NAME_TWITTER + TEXT_TYPE + COMMA_SEP +
                 COLUMN_NAME_BATCH_ID + TEXT_TYPE + COMMA_SEP +
                 COLUMN_NAME_BATCH + TEXT_TYPE +
                 " )";
 
         public static final String SQL_DELETE =
                 STMT_DROP_TABLE + TABLE_NAME;
 
         public static final String SQL_INSERT_ALL =
                 STMT_INSERT + TABLE_NAME + "(" +
                 COLUMN_NAME_ID + COMMA_SEP +
                 COLUMN_NAME_FULL_NAME + COMMA_SEP +
                 COLUMN_NAME_IMAGE_URL + COMMA_SEP +
                 COLUMN_NAME_JOB + COMMA_SEP +
                 COLUMN_NAME_JOB_URL + COMMA_SEP +
                 COLUMN_NAME_SKILLS + COMMA_SEP +
                 COLUMN_NAME_EMAIL + COMMA_SEP +
                 COLUMN_NAME_GITHUB + COMMA_SEP +
                 COLUMN_NAME_TWITTER + COMMA_SEP +
                 COLUMN_NAME_BATCH_ID + COMMA_SEP +
                 COLUMN_NAME_BATCH + ")  VALUES (" +
                 "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 
 
         public static final String GET_ALL = STMT_SELECT + _ALL + STMT_FROM + TABLE_NAME;
 
         public static final String SQL_GET_FILENAME =
                 STMT_SELECT + COLUMN_NAME_IMAGE_FILENAME +
                 STMT_FROM + TABLE_NAME +
                 STMT_WHERE + COLUMN_NAME_IMAGE_URL + " = ? ";
 
         public static final String SQL_GET_ALL_SAVED_TO_DISK =
                 STMT_SELECT + _ALL +
                 STMT_FROM + TABLE_NAME +
                 STMT_WHERE + COLUMN_NAME_IMAGE_FILENAME + STMT_IS_NOT_NULL;
 
         public static final String SQL_GET_ALL_DISK_BY_BATCH =
                 SQL_GET_ALL_SAVED_TO_DISK +
                         STMT_AND + COLUMN_NAME_BATCH + " = ? ";
 
         public static final String SQL_GET_ALL_DISK_BATCH_WITH_LIMIT =
                 SQL_GET_ALL_DISK_BY_BATCH +
                         STMT_LIMIT + " ? ";
 
         public static final String SQL_GET_ALL_SAVED_TO_DISK_WITH_LIMIT =
                 SQL_GET_ALL_SAVED_TO_DISK +
                 STMT_LIMIT + " ?";
 
         public static final String SQL_GET_ALL_BATCH =
                 STMT_SELECT + _ALL +
                 STMT_FROM + TABLE_NAME +
                 STMT_WHERE + COLUMN_NAME_BATCH + " = ? ";
 
         public static final String SQL_GET_ALL_LIMIT =
                 STMT_SELECT + _ALL +
                 STMT_FROM + TABLE_NAME +
                 STMT_LIMIT + " ?";
 
         public static final String SQL_GET_ALL_BATCH_LIMIT =
                 SQL_GET_ALL_BATCH +
                 STMT_LIMIT + " ?";
 
         public static final String[] PROJECTION_ALL = {
                 _ID,
                 COLUMN_NAME_ID,
                 COLUMN_NAME_FULL_NAME,
                 COLUMN_NAME_IMAGE_URL,
                 COLUMN_NAME_IMAGE_FILENAME,
                 COLUMN_NAME_JOB,
                 COLUMN_NAME_JOB_URL,
                 COLUMN_NAME_SKILLS,
                 COLUMN_NAME_EMAIL,
                 COLUMN_NAME_GITHUB,
                 COLUMN_NAME_TWITTER,
                 COLUMN_NAME_BATCH_ID,
                 COLUMN_NAME_BATCH
         };
 
         public static final String SORT_DEFAULT =
                 COLUMN_NAME_BATCH_ID + " DESC" + COMMA_SEP +
                 COLUMN_NAME_FULL_NAME + " ASC";
     }
 }
