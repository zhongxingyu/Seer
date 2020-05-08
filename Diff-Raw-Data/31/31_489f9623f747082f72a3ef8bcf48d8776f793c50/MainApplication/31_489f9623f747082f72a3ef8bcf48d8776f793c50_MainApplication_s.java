 package garin.artemiy.sqlitesimple.example;
 
 import android.app.Application;
 import android.util.Log;
 import garin.artemiy.sqlitesimple.example.model.Record;
 import garin.artemiy.sqlitesimple.example.model.Test;
 import garin.artemiy.sqlitesimple.example.operator.TestDAO;
 import garin.artemiy.sqlitesimple.library.SQLiteSimple;
import garin.artemiy.sqlitesimple.library.util.SimpleDatabaseUtil;
 
 /**
  * author: Artemiy Garin
  * date: 13.12.12
  */
 public class MainApplication extends Application {
 
     @Override
     public void onCreate() {
         super.onCreate();
 
        if (SimpleDatabaseUtil.isFirstApplicationStart(this)) {
 
            SQLiteSimple databaseSimple = new SQLiteSimple(this);
            databaseSimple.create(Record.class);

            SQLiteSimple localSimple = new SQLiteSimple(this, "test.sqlite");
            localSimple.create(Test.class);

        }
 
         TestDAO testDAO = new TestDAO(this);
         Log.d("SQLiteSimple: Local database rows", String.valueOf(testDAO.getCount()));
 
     }
 
 }
