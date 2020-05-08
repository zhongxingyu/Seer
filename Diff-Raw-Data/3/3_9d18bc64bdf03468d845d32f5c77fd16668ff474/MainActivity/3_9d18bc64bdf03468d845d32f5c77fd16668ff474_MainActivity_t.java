 package com.philiptorchinsky.TimeAppe;
 
 import android.app.ListActivity;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 
 import java.util.ArrayList;
 
 public class MainActivity extends ListActivity {
     /**
      * Called when the activity is first created.
      */
 
 
     static public boolean isTest = false;
     private DBAdapter dh;
     private Adapter notes;
     public ArrayList<Item> list = new ArrayList<Item>();
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         dh = new DBAdapter (this);
         dh.open();
 //        dh.destroy();
 //        dh.close();
 //
 //        dh = new DBAdapter (this);
 
        dh.insert("TeamCity training", "inactive", 0,0);
        dh.insert("Android Study", "inactive", 0,0);
 //       dh.insert("FreelancersFeed", "inactive",0,0);
 
         // GET ROWS
 
         Cursor c = dh.getAll();
 

        int i = 9;
         c.moveToFirst();
         if (!c.isAfterLast()) {
             do {
                 String project = c.getString(1);
                 String status = c.getString(2);
                 long secondsSpent = c.getLong(3);
                 long lastactivated = c.getLong(4);
                 Item newitem = new Item(project, status, secondsSpent, lastactivated);
                 list.add(newitem);
             } while (c.moveToNext());
         }
 
         notes = new Adapter (this,list);
 
         setContentView(R.layout.main);
         setListAdapter(notes);
 
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         Item item = (Item) getListAdapter().getItem(position);
         String project = item.getProject();
         String status = item.getStatus();
         long secondsSpent = item.getSecondsSpent();
         long lastActivated = item.getLastactivated();
 
 
         if (status.equalsIgnoreCase("active")) {
             status = "inactive";
             item.setStatus(status);
             long spentThisTime = System.currentTimeMillis() - lastActivated;
             item.setSecondsSpent(secondsSpent + spentThisTime);
             list.set((int)id,item);
 
             dh.updateSpentTimeByProject(project,status,secondsSpent + spentThisTime);
 //            Toast.makeText(this, project + " selected. Now " + (secondsSpent + spentThisTime)/1000.0, Toast.LENGTH_LONG).show();
                     }
         else {
             status = "active";
             item.setStatus(status);
             lastActivated = System.currentTimeMillis();
             item.setLastactivated(lastActivated);
             list.set((int)id,item);
             dh.updateActivatedByProject(project,status,lastActivated);
         }
         notes.notifyDataSetChanged();
     }
 
 
 
 }
