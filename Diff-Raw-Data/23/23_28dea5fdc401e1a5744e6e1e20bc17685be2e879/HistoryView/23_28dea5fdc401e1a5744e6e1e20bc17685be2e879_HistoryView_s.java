 package gov.cdc;
 
 //Author: Paul Brown
 //Originally created on 2013-01-29
 
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.ListView;
 
 public class HistoryView extends Activity {
 	
 	private ArrayList<PTTHistoryItem> mHistory;
 	
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
      // Make the application Full-Screen
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         
         setContentView(R.layout.activity_history_view);
 
         final Button doneButton = (Button)findViewById(R.id.doneButton);
        
        
         doneButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 //startActivity(new Intent(MyActivity.this,ActivityEins.class));
             	Log.d("TEST","TEST");
             	finish();
             }
         });
         
         final ListView myListView = (ListView)findViewById(R.id.my_list_view);
         
         // Create the adapter
         final HistoryItemAdapter historyItemAdapter = new HistoryItemAdapter(this);
         
         //bind adapter to the AdapterView
         myListView.setAdapter(historyItemAdapter);
         
         
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_history_view, menu);
         return true;
     }
 }
