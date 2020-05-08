 package com.praeses;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 import com.j256.ormlite.dao.Dao;
 import com.praeses.data.Person;
 import com.praeses.data.orm.DatabaseHelper;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Random;
 import roboguice.activity.RoboActivity;
 import roboguice.inject.InjectView;
 import roboguice.util.Ln;
 
 public class HelloAndroidActivity extends RoboActivity {
 
     private static String TAG = "demo";
 
     @InjectView(R.id.helloText)
     private TextView helloText;
     
     private DatabaseHelper dbHelper = null;
     /**
      * Called when the activity is first created.
      * @param savedInstanceState If the activity is being re-initialized after 
      * previously being shut down then this Bundle contains the data it most 
      * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		Log.i(TAG, "onCreate");
         setContentView(R.layout.main);
         helloText.setText("Injected");
         
         doDatabaseWork();
     }
     
     private DatabaseHelper getDbHelper() {
         if (dbHelper == null) {
             dbHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
         }
         return dbHelper;
     }
 
     private void doDatabaseWork() {
         try {
             Dao<Person, String> personDao = getDbHelper().getPersonDao();
             List<Person> list = personDao.queryForAll();
             StringBuilder builder = new StringBuilder();
             int count = 0;
             for (Person person : list) {
                 builder.append("------------------------\n");
                 builder.append("[").append(count).append("] = ").append(person).append("\n");
                 count++;
             }
             builder.append("------------------------\n");
             for (Person person : list) {
                 personDao.delete(person);
                 builder.append("deleted ").append(person.getName()).append("\n");
                 Ln.i(TAG, "deleting person " + person.getName());
                 count++;
             }
             
             int createNum;
             do {
                 createNum = new Random().nextInt(3) + 1;
             } while (createNum == list.size());
             
            for (int i = 0; i < createNum; ++i) {
                 Person person = new Person();
                 person.setName("John");
                 personDao.create(person);
                 builder.append("------------------------\n");
                 builder.append("created a new entry: ").append(person.getName()).append("\n");
             }
             try {
                 Thread.sleep(5);
             } catch (InterruptedException ex) {
                 
             }
             helloText.setText(builder.toString());
         } catch (SQLException ex) {
             Ln.e(TAG, "Database exception", ex);
             helloText.setText("Database exception: " + ex);
         }
     }
 }
 
