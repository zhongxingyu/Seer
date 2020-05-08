 package com.uiproject.meetingplanner;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 
 import com.uiproject.meetingplanner.R;
 import android.app.Activity;
 import android.os.Bundle;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 
 public class MainPage extends Activity {
     /** Called when the activity is first created. */
  
     //public Button button1;
     public TextView textview1;
     ServerMsg sm1=new ServerMsg(5,33,44);
     
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.mainpage);
    
         // Link buttons to activities
         // Server Test Btn
 	    Button serverTestBtn = (Button) findViewById(R.id.test1);      
 	    serverTestBtn.setOnClickListener(new Button.OnClickListener(){
 	        public void onClick(View v)
 	        {
 	        	try {
 					displayResult();
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	        	
 	        }
 	    });
 	    
 	    // Create Meeting Btn
 	    Button createMeetingBtn = (Button) findViewById(R.id.createMeeting);
 	    createMeetingBtn.setOnClickListener(new View.OnClickListener() {
 	    	public void onClick(View view) {
 	    		startActivity(new Intent(MainPage.this, CreateMeeting.class));
 	    }});
     
 	    // View Meeting List Btn
 	    Button viewMeetingBtn = (Button) findViewById(R.id.meetingList);
 	    viewMeetingBtn.setOnClickListener(new View.OnClickListener() {
 	    	public void onClick(View view) {
 	    		startActivity(new Intent(MainPage.this, MeetingList.class));
 	    }});
 	    
 	    // Display Meeting Details Btn
 	    Button displayMeetingBtn = (Button) findViewById(R.id.meetingDetail);
 	    displayMeetingBtn.setOnClickListener(new View.OnClickListener() {
 	    	public void onClick(View view) {
 	    		startActivity(new Intent(MainPage.this, DisplayMeeting.class));
 	    }});
     
     }
 
     
     
     public void displayResult() throws JSONException
     {
     	
     	String data = getResponseResult(sm1);
     	HashMap<Integer, Msg> map = new HashMap<Integer, Msg>();
 		JSONObject myjson = new JSONObject(data);
 		JSONArray nameArray = myjson.names();
 		JSONArray valArray = myjson.toJSONArray(nameArray);
 		for (int i = 0; i < valArray.length(); i++) {
 			int la = valArray.getJSONObject(i).getInt("lat");
 			int lo = valArray.getJSONObject(i).getInt("lon");
 			map.put(nameArray.getInt(i), new Msg(la,lo));
 		}
 		
 		// Output the map
 		for (Integer i: map.keySet()){
 			
 			//System.out.println(i+":"+map.get(i).lat+","+map.get(i).lon);
 			//textview1.setText(i+":"+map.get(i).lat+","+map.get(i).lon);
 			ServerMsg[] responseitem=new ServerMsg[10];
 			int j=0;
 			ServerMsg response=new ServerMsg(i,map.get(i).lat,map.get(i).lon);
 			responseitem[j]=response;
 			textview1.setText(responseitem[j].userID+":"+responseitem[j].myLat+","+responseitem[j].myLong);
 			j++;
 		}
 	
     }
     
     public String getResponseResult(ServerMsg sm) {
     	String param1=new Integer(sm.userID).toString(); 
     	String param2=new Integer(sm.myLat).toString();
     	String param3=new Integer(sm.myLong).toString();
     	String urlStr = "http://cs-server.usc.edu:21542/newwallapp/forms/project?id="+param1+"&lat="+param2+"&lon="+param3;
     	String responseResult="";
     	try {
     		URL objUrl = new URL(urlStr);
     		URLConnection connect1 = objUrl.openConnection();
     		connect1.setConnectTimeout(10000);
     		connect1.connect();
     		BufferedReader in = new BufferedReader(new InputStreamReader(connect1.getInputStream()));
     		//Data
     		String content;
     		System.out.println("right");
     		while((content=in.readLine())!=null)
     		{
     			responseResult+=content;
     		}
     		in.close();
     	} catch (Exception e) {
     		System.out.println("error!");
     	}
     	return responseResult;
     }
 }
 
