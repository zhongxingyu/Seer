 package by.bsu.courseproject.db;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import by.bsu.courseproject.services.*;
 
 import static by.bsu.courseproject.db.DBConstants.Columns;
 import static by.bsu.courseproject.db.DBConstants.Tables;
 
 /**
  * User: Artyom Strok
  * Date: 23.03.13
  * Time: 11:54
  */
 public class PMDB {
   private static final String DATABASE_NAME = "project_manager.db";
   private static final int DATABASE_VERSION = 1;
 
   private Context context;
   //TODO:delete static
   private SQLiteDatabase db;
   private ProjectDataSource projectDataSource;
   private EmployeeDataSource employeeDataSource;
   private CustomerDataSource customerDataSource;
   private InvestorDataSource investorDataSource;
   private StageDataSource stageDataSource;
   private static boolean isNotInitialized = true;
 
   private final String[] sqlDump = {
       "INSERT INTO \"person\" VALUES(1,'E',  '1111',   '111',   '1111',       'sokolov@gmail.com', 'mishail.sokolov', '(8-029) 234-89-23','23-01-1991','', '',  '','1111-111111');",
       "INSERT INTO \"person\" VALUES(2,'E',  '11111', '1111',  '1111111', 'dmitry@yandex.ru',  'misha.ivanov',    '(8-025) 988-45-34','25-01-1991','', '',  '','1111-111111');",
       "INSERT INTO \"person\" VALUES(3,'E',  '111',    '111',   '11111',     'misha@yandex.ru',   'misha.ivanov',    '(8-025) 453-45-34','26-01-1991','', '',  '','1111-111111');",
       "INSERT INTO \"person\" VALUES(4,'E',  '1111',   '11111','1111',      'misha@yandex.ru',   'misha.ivanov',    '(8-044) 534-43-23','24-01-1991','', '',  '','1111-111111');",
       "INSERT INTO \"person\" VALUES(5,'E',  '1111',   '1111',  '11111',    'misha@yandex.ru',   'misha.ivanov',    '(8-029) 432-54-45','24-01-1991','', '',  '','1111-111111');",
       "INSERT INTO \"person\" VALUES(6,'E',  '1111',   '1111',  '11111',    'misha@yandex.ru',   'misha.ivanov',    '(8-025) 234-43-44','22-01-1991','', '',  '','1111-111111');",
       "INSERT INTO \"person\" VALUES(7,'E',  '1111',   '1111',  '1111',       'misha@yandex.ru',   'misha.ivanov',    '(8-029) 454-62-55','20-01-1991','', '',  '','1111-111111');",
       "INSERT INTO \"person\" VALUES(8,'E',  '1111',   '111',   '11111',     'misha@yandex.ru',   'misha.ivanov',    '(8-029) 948-50-05','24-01-1991','', '',  '','1111-111111');",
       "INSERT INTO \"person\" VALUES(9,'C', '1111',   '111',   '1111',       'sokolov@gmail.com', 'mishail.sokolov', '(8-029) 234-89-23','23-01-1991','','IBA','','');",
       "INSERT INTO \"person\" VALUES(10,'I', '1111',   '1111',  '11111',    'sokolov@gmail.com', 'mishail.sokolov', '(8-044) 678-89-23','23-01-1991','','IBA','','');",
       "INSERT INTO \"person\" VALUES(11,'C', '1111',   '111',   '1111',      'sokolov@gmail.com', 'mishail.sokolov', '(8-029) 676-80-23','23-01-1991','','IBA','','');",
       "INSERT INTO \"person\" VALUES(12,'I', '1111',   '1111',  '1111111', 'sokolov@gmail.com', 'mishail.sokolov', '(8-029) 435-54-23','23-01-1991','','IBA','','');",
       "INSERT INTO \"person\" VALUES(13,'C','1111',   '1111',  '11111',     'sokolov@gmail.com', 'mishail.sokolov', '(8-025) 234-45-23','23-01-1991','','IBA','','');",
       "INSERT INTO \"person\" VALUES(14,'I', '11',    '11111','11111',     'sokolov@gmail.com', 'mishail.sokolov', '(8-029) 342-12-32','23-01-1991','','IBA','','');",
 
       "INSERT INTO \"project\" VALUES(1,'P1','2012-05-22','EDUCATION', 'ACTIVE', 'NORMAL', 11, 10);",
       "INSERT INTO \"project\" VALUES(2,'P2','2012-06-22','EDUCATION', 'ACTIVE', 'NORMAL', 13, 12);",
       "INSERT INTO \"project\" VALUES(3,'P3','2012-05-06','EDUCATION', 'ACTIVE', 'NORMAL', 13, 14);",
       "INSERT INTO \"project\" VALUES(4,'P4','2012-04-23','EDUCATION', 'ACTIVE', 'NORMAL', 11, 10);",
       "INSERT INTO \"project\" VALUES(5,'P5','2012-07-11','EDUCATION', 'ACTIVE', 'NORMAL', 11, 12);"
   };
   private final String CREATE_TABLE_PROJECT = "CREATE TABLE " + Tables.PROJECT + "("
                                               + Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                                               + Columns.PROJECT_PROJECTNAME + " TEXT NOT NULL,"
                                               + Columns.PROJECT_PROJECTDUEDATE + " TEXT,"
                                               + Columns.PROJECT_CATEGORY + " TEXT,"
                                               + Columns.PROJECT_STATUS + " TEXT,"
                                               + Columns.PROJECT_PRIORITY + " TEXT,"
                                               + Columns.PROJECT_CUSTOMER_ID + " INTEGER,"
                                               + Columns.PROJECT_INVESTOR_ID + " INTEGER );";
 
   private final String CREATE_TABLE_AUTHORIZATION = "CREATE TABLE " + Tables.AUTHORIZATION + " ("
                                                     + Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                                                     + Columns.LOGIN + " BLOB,"
                                                     + Columns.SALT + " TEXT,"
                                                     + Columns.PASSWORD + " BLOB );";
 
   private final String CREATE_TABLE_PERSON = "CREATE TABLE " + Tables.PERSON + " ("
                                              + Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                                             + Columns.PERSON_DISCRIMINATOR + "TEXT,"
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
 
 /*  private final String CREATE_VIEW_PROJECT = "CREATE VIEW "
                                                + Tables.PROJECT_VIEW
                                                + " AS SELECT "
                                                + "P." + Columns._ID
                                                + ",P." + Columns.PROJECT_PROJECTNAME
                                                + ",P." + Columns.PROJECT_PROJECTDUEDATE
                                                + ",P." + Columns.PROJECT_CATEGORY
                                                + ",P." + Columns.PROJECT_STATUS
                                                + ",P." + Columns.PROJECT_PRIORITY
                                                + ",P. " + Columns.PROJECT_INVESTOR_ID
                                                + ",P. " + Columns.PROJECT_CUSTOMER_ID
                                                + ",i. " + Columns.PERSON_FIRSTNAME + " as " + Columns.INVESTOR_FIRSTNAME
                                                + ",i. " + Columns.PERSON_MIDDLENAME + " as " + Columns.INVESTOR_MIDDLENAME
                                                + ",i. " + Columns.PERSON_LASTNAME + " as " + Columns.INVESTOR_LASTNAME
                                                + ",cust. " + Columns.PERSON_FIRSTNAME + " as " + Columns.CUSTOMER_FIRSTNAME
                                                + ",cust. " + Columns.PERSON_MIDDLENAME + " as " + Columns.CUSTOMER_MIDDLENAME
                                                + ",cust. " + Columns.PERSON_LASTNAME + " as " + Columns.CUSTOMER_LASTNAME
                                                + " FROM  project p"
                                                + " LEFT OUTER JOIN " + Tables.PERSON + " i ON  p._ID = i._id "
                                                + " LEFT OUTER JOIN " + Tables.PERSON + " cust ON  p._ID = cust._id ";*/
 
   private final String CREATE_TABLE_STAGE_EMPLOYEE = "CREATE TABLE " + Tables.STAGE_EMPLOYEE + " ("
                                                      + Columns._ID + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                                                      + Columns.STAGE_ID + "STAGE_ID C,"
                                                      + Columns.EMPLOYEE_ID + " INTEGER );";
 
   private final String CREATE_TABLE_STAGE = "CREATE TABLE " + Tables.STAGE + " ("
                                             + Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                                             + Columns.STAGE_TYPE + " TEXT,"
                                             + Columns.STAGE_PROJECT_ID + " INTEGER,"
                                             + Columns.STAGE_MANAGER + " INTEGER );";
 
   private final String DROP_TABLE_STAGE = "DROP TABLE IF EXISTS " + Tables.STAGE;
   private final String DROP_TABLE_STAGE_EMPLOYEE = "DROP TABLE IF EXISTS " + Tables.STAGE_EMPLOYEE;
   private final String DROP_TABLE_PERSON = "DROP TABLE IF EXISTS " + Tables.PERSON;
   private final String DROP_TABLE_PROJECT = "DROP TABLE IF EXISTS " + Tables.PROJECT;
   private final String DROP_TABLE_AUTHORIZATION = "DROP TABLE IF EXISTS " + Tables.AUTHORIZATION;
 
   public PMDB(Context context) {
     this.context = context;
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
 //    db.execSQL(CREATE_VIEW_PROJECT);
     db.execSQL(CREATE_TABLE_STAGE_EMPLOYEE);
     db.execSQL(CREATE_TABLE_STAGE);
     /*db.execSQL("CREATE VIEW "
                + Tables.PROJECT_VIEW
                + " AS SELECT "
                + "P." + Columns._ID
                + ",P." + Columns.PROJECT_PROJECTNAME
                + ",P." + Columns.PROJECT_PROJECTDUEDATE
                + ",P." + Columns.PROJECT_CATEGORY
                + ",P." + Columns.PROJECT_STATUS
                + ",P." + Columns.PROJECT_PRIORITY
                + ",P. " + Columns.PROJECT_COLUMN_PROJECTDESCRIPTION
                + ",P. " + Columns.STAGE1
                + ",P. " + Columns.STAGE2
                + ",P. " + Columns.STAGE3
                + ",P. " + Columns.STAGE4
                + ",P. " + Columns.STAGE5
                + ",E1." + Columns.PERSON_FIRSTNAME + " as " + Columns.EMPLOYEE_COLUMN_EMPLNAME1
                + ",E2." + Columns.PERSON_FIRSTNAME + " as " + Columns.EMPLOYEE_COLUMN_EMPLNAME2
                + ",E3." + Columns.PERSON_FIRSTNAME + " as " + Columns.EMPLOYEE_COLUMN_EMPLNAME3
                + ",E4." + Columns.PERSON_FIRSTNAME + " as " + Columns.EMPLOYEE_COLUMN_EMPLNAME4
                + ",E5." + Columns.PERSON_FIRSTNAME + " as " + Columns.EMPLOYEE_COLUMN_EMPLNAME5
                + " FROM  PROJECT P"
                + " LEFT OUTER JOIN STAGE S1 ON P.STAGE1 = S1._id "
                + " LEFT OUTER JOIN PERSON E1 ON S1.MANAGER_ID = E1._id"
                + " LEFT OUTER JOIN STAGE S2 ON P.STAGE2 = S2._id "
                + " LEFT OUTER JOIN PERSON E2 ON S2.MANAGER_ID = E2._id"
                + " LEFT OUTER JOIN STAGE S3 ON P.STAGE3 = S3._id "
                + " LEFT OUTER JOIN PERSON E3 ON S3.MANAGER_ID = E3._id"
                + " LEFT OUTER JOIN STAGE S4 ON P.STAGE4 = S4._id "
                + " LEFT OUTER JOIN PERSON E4 ON S4.MANAGER_ID = E4._id"
                + " LEFT OUTER JOIN STAGE S5 ON P.STAGE5 = S5._id "
                + " LEFT OUTER JOIN PERSON E5 ON S5.MANAGER_ID = E5._id");
     */
   }
 
   private void dumpImport() {
     for (int i = 0; i < sqlDump.length; i++) {
       db.execSQL(sqlDump[i]);
     }
   }
 
   public SQLiteDatabase getDB() {
     return db;
   }
 
   public ProjectDataSource getProjectDataSource() {
     if (projectDataSource == null) {
       projectDataSource = new ProjectDataSource(db);
     }
     return projectDataSource;
   }
 
   public EmployeeDataSource getEmployeeDataSource() {
     if (employeeDataSource == null) {
       employeeDataSource = new EmployeeDataSource(db);
     }
     return employeeDataSource;
   }
 
   public CustomerDataSource getCustomerDataSource() {
     if (customerDataSource == null) {
       customerDataSource = new CustomerDataSource(db);
     }
     return customerDataSource;
   }
 
   public InvestorDataSource getInvestorDataSource() {
     if (investorDataSource == null) {
       investorDataSource = new InvestorDataSource(db);
     }
     return investorDataSource;
   }
 
   public StageDataSource getStageDataSource() {
     if (stageDataSource == null) {
       stageDataSource = new StageDataSource(db);
     }
     return stageDataSource;
   }
 }
