 package com.appathon.letsgo;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.Intent;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.ViewPager;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TwoLineListItem;
 
 public class LetsGo_Main extends Activity {
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.lets_go__main);
 
         Button button = (Button)findViewById(R.id.button1);
         OnClickListener o = new View.OnClickListener() {
         	@Override 
             public void onClick(View arg0) {
                 Intent myIntent = new Intent(LetsGo_Main.this, EventDetails.class);
                 Bundle ex = new Bundle();
                 ex.putInt("extra", 1);
                 myIntent.putExtras(ex);
                 LetsGo_Main.this.startActivity(myIntent);
                 // TODO Auto-generated method stub
             }
         };
         button.setOnClickListener(o);
         final ListView listview = (ListView) findViewById(R.id.listEventItems);
         String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
                 "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
                 "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
                 "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2",
                 "Android", "iPhone", "WindowsMobile" };
 
 
         Event[] temp = HTTPHelper.es;
 
         final ArrayList<String> list = new ArrayList<String>();
         for (int i = 0; i < values.length; ++i) {
             list.add(values[i]);
         }
 
         final ArrayList<Event> tempList = new ArrayList<Event>();
         for (int i = 0; i < temp.length; ++i) {
             tempList.add(temp[i]);
         }
 
         final StableArrayAdapter adapter = new StableArrayAdapter(this, /*R.layout.listview_event_list*/
                 android.R.layout.simple_list_item_2, tempList);
         listview.setAdapter(adapter);
 
         listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
             @Override
             public void onItemClick(AdapterView<?> parent, final View view,
                                     int position, long id) {
                Intent myIntent = new Intent(LetsGo_Main.this, EventDetails.class);
                Bundle ex = new Bundle();
                ex.putInt("extra", (position));
                myIntent.putExtras(ex);
                LetsGo_Main.this.startActivity(myIntent);

             }
 
         });
     }
     private class StableArrayAdapter extends ArrayAdapter<Event> {
 
         HashMap<Event, Integer> mIdMap=new HashMap<Event, Integer>();
 
         public StableArrayAdapter(Context context, int textViewResourceId,List<Event> events) {
             super(context, textViewResourceId, events);
 
             for(int i=0;i<events.size();i++)
             {
                 mIdMap.put(events.get(i), i);
             }
         }
 
         @Override
         public long getItemId(int position) {
             Event item=getItem(position);
             return mIdMap.get(item);
         }
 
         @Override
         public boolean hasStableIds() {
             return true;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
 
             TwoLineListItem twoLineListItem;
 
             if (convertView == null) {
                 LayoutInflater inflater = (LayoutInflater) getContext()
                         .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 twoLineListItem = (TwoLineListItem) inflater.inflate(
                         android.R.layout.simple_list_item_2, null);
             } else {
                 twoLineListItem = (TwoLineListItem) convertView;
             }
 
             TextView text1 = twoLineListItem.getText1();
             TextView text2 = twoLineListItem.getText2();
 
             text1.setText(getItem(position).getLocation());
             text2.setText(getItem(position).getStartTime().toString());
 
             return twoLineListItem;
         }
 
     }
 
 }
