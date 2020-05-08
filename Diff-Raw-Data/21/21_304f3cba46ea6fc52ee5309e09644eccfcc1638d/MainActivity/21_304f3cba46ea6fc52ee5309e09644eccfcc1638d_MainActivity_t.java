 package com.wsu_codemichigan;
 

 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.ListView;
 import com.wsu_codemichigan.Utils.JSONParsing;
 import com.wsu_codemichigan.Utils.StableArrayAdapter;
 import com.wsu_codemichigan.parcelables.CustomList;
 
 import java.util.ArrayList;
 
 
 public class MainActivity extends Activity {
 
 
     // url to make request
     public final String url = "http://opendata.socrata.com/resource/es6f-iz4c..json";
     private CustomList[] cust;
     public ArrayList<String> list;
     private String json;
     public StableArrayAdapter adapter;
     public ListView listView;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         list = new ArrayList<String>();
 
         JSONParsing parsing = new JSONParsing(this);
 
         parsing.execute();
     }
 }
 
 
 
 
 
 
 
