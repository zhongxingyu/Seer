 package com.android.apptime;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.app.TabActivity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TabHost;
 
 import com.android.apptime.database.EventDBAdapter;
 import com.android.apptime.database.ItemDBAdapter;
 import com.android.apptime.database.MainDBAdapter;
 import com.android.apptime.view.CalendarView;
 import com.android.apptime.view.MapView;
 import com.android.apptime.view.OrganizerView;
 public class Main extends TabActivity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         Resources res = getResources();
         TabHost tabHost = getTabHost();
         TabHost.TabSpec spec; 
         Intent intent;  
 
         intent = new Intent().setClass(this, CalendarView.class);
 
         spec = tabHost.newTabSpec("calendar").setIndicator("Calendar",
                           res.getDrawable(R.drawable.ic_tab_calendar))
                       .setContent(intent);
         tabHost.addTab(spec);
 
         intent = new Intent().setClass(this, MapView.class);
         spec = tabHost.newTabSpec("map").setIndicator("Map",
                           res.getDrawable(R.drawable.ic_tab_map))
                       .setContent(intent);
         tabHost.addTab(spec);
 
         intent = new Intent().setClass(this, OrganizerView.class);
         spec = tabHost.newTabSpec("organizer").setIndicator("Organizer",
                           res.getDrawable(R.drawable.ic_tab_organizer))
                       .setContent(intent);
         tabHost.addTab(spec);
 
         tabHost.setCurrentTab(0);
         MainDBAdapter newdb = new MainDBAdapter(this);
         newdb.open();
         ItemDBAdapter idb1 = new ItemDBAdapter(this);
         EventDBAdapter idb = new EventDBAdapter(this);
         idb.open();
         idb1.open();
         
         
         DatabaseInterface db = new DatabaseInterface(this);
         List<String> alerttype = new ArrayList<String>();
         alerttype.add("alert1"); alerttype.add("alert2");
         Item item = new Item("title test", "description test", "location test", "category test", 
         		alerttype, "priority test", "Event", "1991-07-11 12:08:12", "2007-02-26 20:15:00", 
         		null, "2014-08-20 20:21:22", "repeat test", "completed test", 1);
         String et = item.GetAlertTime();
        //String et1 = item.GetDeadline();
         idb.createEvent(item);
         
         item.SetTitle("title test 2");
         idb.updateEvent(item);
         db.AddItemToDatabase(this, item);
        Cursor mycs = idb.getEventById(1);
         String titletest = mycs.getString(8);
         
         Date newdate = new Date();
         long ttt = newdate.getTime();
         /*
         Date olddate = new Date("1991-07-11 12:08:12");
         boolean tttt = false;
         if (olddate.getTime() < newdate.getTime()) tttt=true;
         */
         
         
         DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         try {
 			Date a = dfm.parse("2007-02-26 20:15:00");
 			int yu= 0;
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         
         
         newdb.close();
     }
     @Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	Log.d("calendarview", "WTF?");
     }
     
     
 }
