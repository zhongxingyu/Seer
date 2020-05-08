 package com.jobhound.datasource;
 
 import java.sql.SQLException;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 
 import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.dao.RuntimeExceptionDao;
 import com.j256.ormlite.support.ConnectionSource;
 import com.j256.ormlite.table.TableUtils;
 import com.jobhound.R;
 
 public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
 	
     // name of the database file for your application
     private static final String DATABASE_NAME = "jobhound.db";
      
     // any time you make changes to your database objects, you may have to increase the database version
    private static final int DATABASE_VERSION = 0;
 	
     
     // the DAO object we use to access the SimpleData table
     private Dao<DiaryEntry, Integer> diaryDAO;
     private RuntimeExceptionDao<DiaryEntry, Integer> diaryRuntimeDAO;
 	
 	 public DatabaseHelper(Context context) {
 	        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
 	    }
 
 	@Override
 	public void onCreate(SQLiteDatabase arg0, ConnectionSource connectionSource) {
 		// TODO Auto-generated method stub
 		try {
 			TableUtils.createTable(connectionSource, DiaryEntry.class);	
 		}catch(SQLException e) {
             throw new RuntimeException(e);
         }
 		
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase arg0, ConnectionSource connectionSource, int arg2,
 			int arg3) {
 		// TODO Auto-generated method stub
 		try {
             TableUtils.dropTable(connectionSource, DiaryEntry.class, true);
 		}catch(SQLException e) {
             throw new RuntimeException(e);
         }
 	}
 	
 	 /**
      * Returns the Database Access Object (DAO) for our SimpleData class. It will create it or just give the cached
      * value.
      */
     public Dao<DiaryEntry, Integer> getDiaryDAO() throws SQLException
     {
     	if (diaryDAO == null) {
     		diaryDAO = getDao(DiaryEntry.class);
         }
         return diaryDAO;	
     }
 
     /**
      * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our SimpleData class. It will
      * create it or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
      */
     public RuntimeExceptionDao<DiaryEntry, Integer> getProfileDataDao() {
         if (diaryRuntimeDAO == null) {
         	diaryRuntimeDAO = getRuntimeExceptionDao(DiaryEntry.class);
         }
         return diaryRuntimeDAO;
     }
     
     /**
      * Close the database connections and clear any cached DAOs.
      */
     @Override
     public void close() {
         super.close();
         diaryRuntimeDAO = null;
     }
 }
