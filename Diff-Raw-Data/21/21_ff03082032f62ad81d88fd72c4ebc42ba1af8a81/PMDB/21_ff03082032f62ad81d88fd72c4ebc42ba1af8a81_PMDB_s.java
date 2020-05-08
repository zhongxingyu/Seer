 package by.bsu.courseproject.db;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import by.bsu.courseproject.datasources.PersonDataSource;
 import by.bsu.courseproject.datasources.ProjectDataSource;
 import by.bsu.courseproject.datasources.StageDataSource;

import static by.bsu.courseproject.db.DBConstants.Columns;
import static by.bsu.courseproject.db.DBConstants.Tables;
 
 /**
  * User: Artyom Strok
  * Date: 23.03.13
  * Time: 11:54
  */
 public class PMDB {
   private static final String DATABASE_NAME = "project_manager.db";
   private final SQLiteDatabase db;
   private ProjectDataSource projectDataSource;
   private PersonDataSource personDataSource;
   private StageDataSource stageDataSource;
   private static boolean isNotInitialized = true;
 
   private final String[] sqlDump = {
       "INSERT INTO \"person\" VALUES(1,'E',  'empFN1',   'empLN1',   'empP1', 'sokolov@gmail.com', 'mishail.sokolov', '(8-029) 234-89-23','23-01-1991','', '',  '','1111-111111');",
       "INSERT INTO \"person\" VALUES(2,'E',  'empFN2',   'empLN2',   'empP2', 'dmitry@yandex.ru',  'misha.ivanov',    '(8-025) 988-45-34','25-01-1991','', '',  '','1111-111111');",
       "INSERT INTO \"person\" VALUES(3,'E',  'empFN3',   'empLN3',   'empP3', 'misha@yandex.ru',   'misha.ivanov',    '(8-025) 453-45-34','26-01-1991','', '',  '','1111-111111');",
       "INSERT INTO \"person\" VALUES(4,'E',  'empFN4',   'empLN4',   'empP4', 'misha@yandex.ru',   'misha.ivanov',    '(8-044) 534-43-23','24-01-1991','', '',  '','1111-111111');",
       "INSERT INTO \"person\" VALUES(5,'E',  'empFN5',   'empLN5',   'empP5', 'misha@yandex.ru',   'misha.ivanov',    '(8-029) 432-54-45','24-01-1991','', '',  '','1111-111111');",
       "INSERT INTO \"person\" VALUES(6,'E',  'empFN6',   'empLN6',   'empP6', 'misha@yandex.ru',   'misha.ivanov',    '(8-025) 234-43-44','22-01-1991','', '',  '','1111-111111');",
       "INSERT INTO \"person\" VALUES(7,'E',  'empFN7',   'empLN7',   'empP7', 'misha@yandex.ru',   'misha.ivanov',    '(8-029) 454-62-55','20-01-1991','', '',  '','1111-111111');",
       "INSERT INTO \"person\" VALUES(8,'E',  'empFN8',   'empLN8',   'empP8', 'misha@yandex.ru',   'misha.ivanov',    '(8-029) 948-50-05','24-01-1991','', '',  '','1111-111111');",
       "INSERT INTO \"person\" VALUES(9,'C',  'cusFN1',   'cusLN1',   'cusP1', 'sokolov@gmail.com', 'mishail.sokolov', '(8-029) 234-89-23','23-01-1991','','IBA','','');",
       "INSERT INTO \"person\" VALUES(10,'I', 'invFN1',   'invLN1',   'invP1', 'sokolov@gmail.com', 'mishail.sokolov', '(8-044) 678-89-23','23-01-1991','','IBA','','');",
       "INSERT INTO \"person\" VALUES(11,'C', 'cusFN2',   'cusLN2',   'cusP2', 'sokolov@gmail.com', 'mishail.sokolov', '(8-029) 676-80-23','23-01-1991','','IBA','','');",
       "INSERT INTO \"person\" VALUES(12,'I', 'invFN2',   'invLN2',   'invP2', 'sokolov@gmail.com', 'mishail.sokolov', '(8-029) 435-54-23','23-01-1991','','IBA','','');",
       "INSERT INTO \"person\" VALUES(13,'C', 'cusFN3',   'cusLN3',   'cusP3', 'sokolov@gmail.com', 'mishail.sokolov', '(8-025) 234-45-23','23-01-1991','','IBA','','');",
       "INSERT INTO \"person\" VALUES(14,'I', 'invFN3',   'invLN3',   'invP3', 'sokolov@gmail.com', 'mishail.sokolov', '(8-029) 342-12-32','23-01-1991','','IBA','','');",
 
       "INSERT INTO \"project\" VALUES(1,'Проект 1','Описание 1','22-05-2012', 'Категория 1', 1, 2, 11, 10);",
       "INSERT INTO \"project\" VALUES(2,'Проект 2','Описание 2','22-06-2012', 'Категория 2', 1, 2, 13, 12);",
       "INSERT INTO \"project\" VALUES(3,'Проект 3','Описание 3','05-04-2012', 'Категория 3', 1, 2, 13, 14);",
       "INSERT INTO \"project\" VALUES(4,'Проект 4','Описание 4','23-04-2012', 'Категория 4', 1, 2, 11, 10);",
       "INSERT INTO \"project\" VALUES(5,'Проект 5','Описание 5','07-11-2012', 'Категория 5', 1, 2, 11, 12);",
 
       "INSERT INTO \"stage\" VALUES(1, 0, 1, 1)",
       "INSERT INTO \"stage\" VALUES(2, 1, 1, 2)",
       "INSERT INTO \"stage\" VALUES(3, 2, 1, 3)",
       "INSERT INTO \"stage\" VALUES(4, 3, 1, 4)",
       "INSERT INTO \"stage\" VALUES(5, 4, 1, 5)",
 
       "INSERT INTO \"stage\" VALUES(6, 0, 2, 6)",
       "INSERT INTO \"stage\" VALUES(7, 1, 2, 7)",
       "INSERT INTO \"stage\" VALUES(8, 2, 2, 8)",
       "INSERT INTO \"stage\" VALUES(9, 3, 2, 1)",
       "INSERT INTO \"stage\" VALUES(10, 4, 2, 2)",
 
       "INSERT INTO \"stage\" VALUES(11, 0, 3, 3)",
       "INSERT INTO \"stage\" VALUES(12, 1, 3, 4)",
       "INSERT INTO \"stage\" VALUES(13, 2, 3, 5)",
       "INSERT INTO \"stage\" VALUES(14, 3, 3, 6)",
       "INSERT INTO \"stage\" VALUES(15, 4, 3, 7)",
 
       "INSERT INTO \"stage\" VALUES(16, 0, 4, 8)",
       "INSERT INTO \"stage\" VALUES(17, 1, 4, 1)",
       "INSERT INTO \"stage\" VALUES(18, 2, 4, 2)",
       "INSERT INTO \"stage\" VALUES(19, 3, 4, 3)",
       "INSERT INTO \"stage\" VALUES(20, 4, 4, 4)",
 
       "INSERT INTO \"stage\" VALUES(21, 0, 5, 5)",
       "INSERT INTO \"stage\" VALUES(22, 1, 5, 6)",
       "INSERT INTO \"stage\" VALUES(23, 2, 5, 7)",
       "INSERT INTO \"stage\" VALUES(24, 3, 5, 8)",
       "INSERT INTO \"stage\" VALUES(25, 4, 5, 1)"
   };
 
   private static final String CREATE_TABLE_PROJECT = "CREATE TABLE " + Tables.PROJECT + "("
                                                      + Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                                                      + Columns.PROJECT_PROJECTNAME + " TEXT NOT NULL,"
                                                      + Columns.PROJECT_DESCRIPTION + " TEXT,"
                                                      + Columns.PROJECT_PROJECTDUEDATE + " TEXT,"
                                                      + Columns.PROJECT_CATEGORY + " TEXT,"
                                                      + Columns.PROJECT_STATUS + " INTEGER,"
                                                      + Columns.PROJECT_PRIORITY + " INTEGER,"
                                                      + Columns.PROJECT_CUSTOMER_ID + " INTEGER,"
                                                      + Columns.PROJECT_INVESTOR_ID + " INTEGER );";
 
   private static final String CREATE_TABLE_AUTHORIZATION = "CREATE TABLE " + Tables.AUTHORIZATION + " ("
                                                            + Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                                                            + Columns.LOGIN + " BLOB,"
                                                            + Columns.SALT + " TEXT,"
                                                            + Columns.PASSWORD + " BLOB );";
 
   private static final String CREATE_TABLE_PERSON = "CREATE TABLE " + Tables.PERSON + " ("
                                                     + Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                                                     + Columns.PERSON_DISCRIMINATOR + " TEXT,"
                                                     + Columns.PERSON_LASTNAME + " TEXT,"
                                                     + Columns.PERSON_FIRSTNAME + " TEXT,"
                                                     + Columns.PERSON_MIDDLENAME + " TEXT,"
                                                     + Columns.PERSON_EMAIL + " TEXT,"
                                                     + Columns.PERSON_SKYPE + " TEXT,"
                                                     + Columns.PERSON_PHONE + " TEXT,"
                                                     + Columns.PERSON_BIRTHDAY + " TEXT,"
                                                     + Columns.PERSON_INFO + " TEXT,"
                                                     + Columns.CUSTOMER_INVESTOR_COMPANY + " TEXT,"
                                                     + Columns.EMPLOYEE_EXPERIENCE + " TEXT,"
                                                     + Columns.EMPLOYEE_EDUCATION + " TEXT );";
 
   private static final String CREATE_TABLE_STAGE_EMPLOYEE = "CREATE TABLE " + Tables.STAGE_EMPLOYEE + " ("
                                                             + Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                                                             + Columns.STAGE_ID + " INTEGER NOT NULL ,"
                                                             + Columns.EMPLOYEE_ID + " INTEGER NOT NULL );";
 
   private static final String CREATE_TABLE_STAGE = "CREATE TABLE " + Tables.STAGE + " ("
                                                    + Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                                                    + Columns.STAGE_TYPE + " INTEGER NOT NULL ,"
                                                    + Columns.STAGE_PROJECT_ID + " INTEGER NOT NULL ,"
                                                    + Columns.STAGE_MANAGER + " INTEGER NOT NULL );";
 
   private static final String DROP_TABLE_STAGE = "DROP TABLE IF EXISTS " + Tables.STAGE;
   private static final String DROP_TABLE_STAGE_EMPLOYEE = "DROP TABLE IF EXISTS " + Tables.STAGE_EMPLOYEE;
   private static final String DROP_TABLE_PERSON = "DROP TABLE IF EXISTS " + Tables.PERSON;
   private static final String DROP_TABLE_PROJECT = "DROP TABLE IF EXISTS " + Tables.PROJECT;
   private static final String DROP_TABLE_AUTHORIZATION = "DROP TABLE IF EXISTS " + Tables.AUTHORIZATION;
 
   public PMDB(Context context) {
     db = context.openOrCreateDatabase(DATABASE_NAME, 0, null);
    if (isNotInitialized) {
       createDBStructure();
       dumpImport();
       isNotInitialized = false;
    }
   }
 
   private void createDBStructure() {
     db.execSQL(DROP_TABLE_STAGE);
     db.execSQL(DROP_TABLE_STAGE_EMPLOYEE);
     db.execSQL(DROP_TABLE_PERSON);
     db.execSQL(DROP_TABLE_PROJECT);
     db.execSQL(DROP_TABLE_AUTHORIZATION);
     db.execSQL(CREATE_TABLE_AUTHORIZATION);
     db.execSQL(CREATE_TABLE_PROJECT);
     db.execSQL(CREATE_TABLE_PERSON);
     db.execSQL(CREATE_TABLE_STAGE_EMPLOYEE);
     db.execSQL(CREATE_TABLE_STAGE);
   }
 
   private void dumpImport() {
     for (String aSqlDump : sqlDump) {
       db.execSQL(aSqlDump);
     }
   }
 
   public SQLiteDatabase getDB() {
     return db;
   }
 
   public ProjectDataSource getProjectDataSource() {
     if (projectDataSource == null) {
       projectDataSource = new ProjectDataSource(db);
       projectDataSource.setPersonDataSource(getPersonDataSource());
       projectDataSource.setStageDataSource(getStageDataSource());
     }
     return projectDataSource;
   }
 
   PersonDataSource getPersonDataSource() {
     if (personDataSource == null) {
       personDataSource = new PersonDataSource(db);
     }
     return personDataSource;
   }
 
 
   public StageDataSource getStageDataSource() {
     if (stageDataSource == null) {
       stageDataSource = new StageDataSource(db);
       stageDataSource.setPersonDataSource(getPersonDataSource());
     }
     return stageDataSource;
   }
 }
