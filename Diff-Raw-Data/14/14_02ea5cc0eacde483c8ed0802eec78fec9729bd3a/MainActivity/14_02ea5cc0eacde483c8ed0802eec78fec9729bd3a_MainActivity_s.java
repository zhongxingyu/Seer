 package com.android.apptime;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 import com.android.apptime.database.ItemDBAdapter;
 import com.android.apptime.database.MainDBAdapter;
 
 public class MainActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
        MainDBAdapter newdb = new MainDBAdapter(this);
        newdb.open();
       
         
        DatabaseInterface db = new DatabaseInterface(this);
        List<String> alerttype = new ArrayList<String>();
        alerttype.add("alert1"); alerttype.add("alert2");
        Item item = new Item("title test", "description test", "location test", "category test", 
        		alerttype, "priority test", "Event", "starttime test", "endtime test", 
        		"deadline test", "alerttime test", "repeat test", "completed test", 1);
       
        
        db.AddItemToDatabase(this, item);
        newdb.close();
     }
 }
