 package com.sto;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /**
  * Created by o.petrova on 16.05.13.
  */
 public class ShowItemDescription extends Activity {
 //
 //    String[] names = {"1", "2", "3", "4", "5"};
 //    protected void onCreate(Bundle savedInstanceState) {
 //        super.onCreate(savedInstanceState);
 //
 //        setContentView(findViewById(R.layout.show_item));
 //
 //        ListView listView = (ListView) findViewById(R.id.listView);
 //
 //        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.array.item_info_array_headers, names);
 //        listView.setAdapter(adapter);
 //    }
 //TODO fill in list with normal values and find out what happend to maps!!!!
 
     String[] name = {"Иван", "Марья", "Петр", "Антон", "Даша", "Борис",
             "Костя", "Игорь"};
     String[] position = {"Программер", "Бухгалтер", "Программер",
             "Программер", "Бухгалтер", "Директор", "Программер", "Охранник"};
     int salary[] = {13000, 10000, 13000, 13000, 10000, 15000, 13000, 8000};
 
     int[] colors = new int[2];
 
     /**
      * Called when the activity is first created.
      */
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.show_item);
 
        colors[0] = Color.parseColor("#559966CC");
        colors[1] = Color.parseColor("#55336699");
 
        LinearLayout linLayout = (LinearLayout) findViewById(R.id.linLayout);
 
        LayoutInflater ltInflater = getLayoutInflater();
 
         for (int i = 0; i < name.length; i++) {
             Log.d("myLogs", "i = " + i);
            View item = ltInflater.inflate(R.layout.item, linLayout, false);
            TextView tvName = (TextView) item.findViewById(R.id.tvName);
             tvName.setText(name[i]);
            TextView tvPosition = (TextView) item.findViewById(R.id.tvPosition);
             tvPosition.setText("Должность: " + position[i]);
            TextView tvSalary = (TextView) item.findViewById(R.id.tvSalary);
             tvSalary.setText("Оклад: " + String.valueOf(salary[i]));
             item.getLayoutParams().width = LayoutParams.MATCH_PARENT;
             item.setBackgroundColor(colors[i % 2]);
            linLayout.addView(item);
         }
     }
 }
