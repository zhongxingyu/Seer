 package net.hackergarten.android.app;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 
 public class MainActivity extends Activity {
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         LinearLayout listLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.main, null);
         ListView listView = (ListView) listLayout.findViewById(R.id.eventListView);
 
         Button registerButton = (Button) listLayout.findViewById(R.id.registerButton);
         registerButton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
 				startActivity(intent);
 			}
 		});
         
        setContentView(listLayout);
     }
 	
 }
