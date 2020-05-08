 package com.example.homeautomation;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import org.json.JSONObject;
 
 import com.example.homeautomation.utility.GetRequestTask;
 import com.example.homeautomation.utility.PostRequestTask;
 
 public class MainActivity extends TemplateActivity {
 
     private boolean locked;
     private TextView isLocked;
     private TextView isUnlocked;
     private TextView problem;
     
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
 		tag = "MainActivity";
 		Log.i(tag,"MainActivity started");
 		super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         isLocked = (TextView)findViewById(R.id.locked_message);
         isUnlocked = (TextView)findViewById(R.id.unlocked_message);
         problem = (TextView)findViewById(R.id.problem_message);
         try {
 			this.sendGet();
 			Log.i(tag,"Initial Get Sent");
 			Log.i(tag,"locked= "+locked);
 		} catch (Exception e) {
 			Log.e(tag,e.toString());
 		}
         if(locked==true){        	
         	isLocked.setVisibility(View.VISIBLE);        	
         }
         else if(locked==false){        	
         	isUnlocked.setVisibility(View.VISIBLE);
         }
         else{        	
         	problem.setVisibility(View.VISIBLE);
         }
         
         /*
          * Modified "test" in login activity. Should return "Modified in Login".
          */
         
        Toast toast = Toast.makeText(getApplicationContext(), ip, Toast.LENGTH_SHORT);
         toast.show();
         
     }
 	
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     /** Called when the user clicks the Key button */
     public void buttonPress(View view) throws Exception {
         if(locked==true)
         	sendPost("unlock");
         else if(locked==false)
         	sendPost("lock");
         else
         	System.out.println("Boolean not instantiated.");
     }
     
     private void sendGet() throws Exception{
         JSONObject jsonObject = new GetRequestTask(tag, ip, client, localContext).execute(ip).get();
         if(jsonObject.get("status").equals("locked")){
         		locked = true;
         		Log.i(tag, "Door is locked.");
         }
         else if(jsonObject.get("status").equals("unlocked")){
         		locked = false;
         		Log.i(tag, "Door is unlocked.");
         }
         else{
     		System.out.println("JSONObject Format Error");
     		problem.setVisibility(View.VISIBLE);
     	}
     }
     
     private void sendPost(String cmd) throws Exception{
     	Log.i(tag,"Sending Post Request");
     	JSONObject jsonObject = new PostRequestTask(tag, ip, client, localContext).execute(cmd).get();
     	
     	if(jsonObject.get("status").equals("locked")){
     		locked = true;
     		isUnlocked.setVisibility(View.GONE);
     		isLocked.setVisibility(View.VISIBLE);
     		System.out.println("Door has been locked.");
     	}
     	else if(jsonObject.get("status").equals("unlocked")){
     		locked = false;
     		isLocked.setVisibility(View.GONE);
     		isUnlocked.setVisibility(View.VISIBLE);
     		System.out.println("Door has been unlocked.");
     	}
     	else{
     		System.out.println("JSONObject Format Error");
     		problem.setVisibility(View.VISIBLE);
     	}
     }
 }
