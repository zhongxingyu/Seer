 package com.bruinlyfe.bruinlyfe;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.ListView;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by chris on 10/10/13.
  */
 public class MenuDisplayActivity extends Activity {
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_menu_display);
 
         Intent intent = this.getIntent();
         List<String> meal = intent.getExtras().getStringArrayList("menuData");
         String hallMeal = intent.getExtras().getString("hallMeal");
         setTitle(hallMeal);
 
         List<Item> items = new ArrayList<Item>();
         for(int i=0;i<meal.size();i++) {
             //Check to see if the item is a section header
             if(meal.get(i).toString().contains("\"title\"")) {
                 StringBuilder sb = new StringBuilder(meal.get(i).toString());
                 //Remove some JSON stuff
                 sb.delete(0, 10);
                 sb.delete(sb.length()-2, sb.length());
                 items.add(new Header(getLayoutInflater(), sb.toString()));
 
             }
             else {
                 items.add(new ListItem(getLayoutInflater(), meal.get(i).toString()));
             }
         }
 
 
         ListView lv = (ListView)findViewById(R.id.listViewMenu);
         TwoTextArrayAdapter adapter = new TwoTextArrayAdapter(this, items);
         lv.setAdapter(adapter);
     }
 }
