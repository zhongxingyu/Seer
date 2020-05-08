 package com.choiboi.apiexamples;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import com.choiboi.apiexamples.button.ButtonMainActivity;
 import com.choiboi.apiexamples.camera.CameraMainActivity;
 import com.choiboi.apiexamples.gallery.GalleryMainActivity;
 import com.choiboi.apiexamples.toast.ToastMainActivity;
 
 public class MainActivity extends Activity {
     
     private final String[] LIST = new String[]{ "Toast", "Camera", "Gallery", "Button Listener" };
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         ListView listView = (ListView)findViewById(R.id.main_listview);
         
         ArrayAdapter<String> adapter =
                 new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, LIST);
         listView.setAdapter(adapter);
         
         listView.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("TAG", "position: " + position);
                Intent intent = null;
                 
                 switch(position) {
                 case 0:
                     intent = new Intent(getApplicationContext(), ToastMainActivity.class);
                     break;
                 case 1:
                     intent = new Intent(getApplicationContext(), CameraMainActivity.class);
                     break;
                 case 2:
                     intent = new Intent(getApplicationContext(), GalleryMainActivity.class);
                     break;
                 case 3:
                     intent = new Intent(getApplicationContext(), ButtonMainActivity.class);
                     break;
                 }
                startActivity(intent);
             }
         });
     }
 }
