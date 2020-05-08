 package com.Android.CodeInTheAir.UI;
 
 import android.app.Activity;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Toast;
 import android.view.View;
 import android.widget.Spinner;
 import android.widget.ArrayAdapter;
 import android.util.Log; 
 import com.Android.CodeInTheAir.Global.AppContext;
 import com.Android.CodeInTheAir.ShellClient.ShellClientComponents;
 import com.Android.CodeInTheAir.ShellClient.ShellClientHandler;
 import java.lang.*;
 
 public class StartDialog extends Activity 
 {
     Spinner actionSpinner;
     Spinner triggerSpinner;
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         AppContext.initContext(this);
         
         Components.start();
 
         // trigger drop-down. Drops downs are called spinners in Android
         triggerSpinner = (Spinner) findViewById(R.id.trigger);
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
             this, R.array.triggers_array, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         triggerSpinner.setAdapter(adapter);
         triggerSpinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
 
         // Similarly Actions drop-down 
         actionSpinner = (Spinner) findViewById(R.id.action);
         ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
             this, R.array.actions_array, android.R.layout.simple_spinner_item);
         adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         actionSpinner.setAdapter(adapter2);
         actionSpinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
   
 
  
     
         /*String code = "<script language=\"JavaScript\"> function start() { var p = new Object(); p.sampleType = \"event\"; p.period = 1; p.lifeTime = \"timed\"; p.runTime = 10000; accl.add(\"accl.sample\", JSON.stringify(p), \"rcv\", \"abcd\");  } function rcv(e) { log.v(\"fromJS\", e.value.accl.z); } </script>";
         String jsName = "main";
         
         JSONObject jMainObj = new JSONObject();
         try
         {
         	JSONObject jObj = new JSONObject();
 	        jObj.put("file", jsName);
 	        jObj.put("code", code);
 	        
 	        JSONArray jArr = new JSONArray();
 	        jArr.put(jObj);
 	        jMainObj.put("source", jArr);
 	        jMainObj.put("mainFile", jsName);
 	        jMainObj.put("mainFunc", "start();");	        
         }
         catch (Exception e)
         {
         	
         }
         
         Task task = TaskManager.createTask();
         task.start(jMainObj.toString());*/
     }
 
         // Button to run the app  
 
     public void onButtonClicked(View v) {
         // Do something when the button is clicked
           Toast.makeText(StartDialog.this, "Running CITA Task ", Toast.LENGTH_SHORT).show();
         // RUN CITA Task 
         Log.v("CITA: StartDialog","Running CITA Task");
         String strTask = "4aef23232"; // random task id
         String strSession = "23ae234"; // random strID
         String trigger = triggerSpinner.getSelectedItem().toString();
         String action="vibrate()";
         action=actionSpinner.getSelectedItem().toString(); 
         Log.v("CITA : StartDialog","Action is "+action+" Toast is toast()");
         if (action.startsWith("toa")) {
           Log.v("CITA: StartDialog","It's a toast here, so modifying \n");
           action="toast(\"Hello CITA\")";
         }
        String strCommand = "function foo(pred) { phone."+action+"; };  addCallback(\""+trigger+"\",\"foo\")";
 
         ShellClientHandler handler = ShellClientComponents.shellClientManager.getHandler(strTask);
         handler.execute(strSession, strCommand);
 
     }
     
 
 	
 	@Override 
 	public void onConfigurationChanged(Configuration newConfig) 
 	{ 
 		super.onConfigurationChanged(newConfig); 
 	}
 
     public class MyOnItemSelectedListener implements OnItemSelectedListener {
 
     public void onItemSelected(AdapterView<?> parent,
         View view, int pos, long id) {
 //      Toast.makeText(parent.getContext(), "Selected item is " +
 //          parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG).show();
     }
 
     public void onNothingSelected(AdapterView parent) {
       // Do nothing.
     }
 }
 }
 
 
