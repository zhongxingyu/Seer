 package com.rashaunj.ruregistered;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Set;
 
 
 import parse.Course;
 import parse.CourseWriter;
 import parse.Email;
 import parse.Section;
 import parse.TrackedCourse;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParser;
 import com.google.gson.stream.JsonReader;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 
 
 public class Tracker extends Service {
 	public ArrayList<TrackedCourse> open = new ArrayList<TrackedCourse>();
 	public ArrayList<TrackedCourse> closed = new ArrayList<TrackedCourse>();
 	Hashtable<String,ArrayList<TrackedCourse>> in = new Hashtable<String,ArrayList<TrackedCourse>>();
 	public String email;
     @Override
     public void onCreate() {
 		//CharSequence text = "Attempting to start Course Tracker...";
 		//int duration = Toast.LENGTH_SHORT;
       	
 		//Toast toast = Toast.makeText(context, text, duration);
 		//toast.show();
  
     }
 
 	  @Override
 	  public int onStartCommand(Intent intent, int flags, int startId) {
 		Runnable r = new Runnable() {
 
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				try {
 
 					create(in);
 					Set<String> keySet = in.keySet();
 					for(String key: keySet){
 						checkOpen(in.get(key));
 					}
 					if(!open.isEmpty()){
 					    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 				       	String email = settings.getString("email","");
 						Email.MandrillDeploy(open,email);
 						open.clear();
 						in.clear();
 						update(closed);
 					}
 					
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 			
 		};
 		Thread t = new Thread(r);
 		t.start();
 		return Service.START_STICKY;
 	  }
 		
 public Hashtable<String,ArrayList<TrackedCourse>> create(Hashtable<String,ArrayList<TrackedCourse>> in) throws IOException{
 		Gson gson = new Gson();    	
 	        File file = new File(getFilesDir(), "RUTracker.json");
 	    	FileInputStream stream = new FileInputStream(file);
 	    	if( file.exists()){
 			BufferedReader br = new BufferedReader(
 					new InputStreamReader(stream));
 			
 	      	StringBuilder jsonText = new StringBuilder();
 	      	String curr = null;
 	      	while ((curr = br.readLine()) != null){	
 	      	jsonText.append(curr);
 	      	}
 	      	br.close();
 	      	InputStream jsonStream = new ByteArrayInputStream(jsonText.toString().getBytes());
 	      	//Json source file
 	      	JsonReader reader = new JsonReader(new InputStreamReader(jsonStream));//Converts String to type InputStream
 	      	JsonParser parser = new JsonParser();           
 	      	JsonArray userarray= parser.parse(reader).getAsJsonArray();
 	      	
 	      	for(JsonElement singleClass: userarray){
 	      		TrackedCourse singleCourse = gson.fromJson(singleClass, TrackedCourse.class);
 	      		if(in.containsKey(singleCourse.major)){
 	      			in.get(singleCourse.major).add(singleCourse);
 	      		}
 	      		else{
 	      			ArrayList<TrackedCourse> push = new ArrayList<TrackedCourse>();
 	      			push.add(singleCourse);
 	      			in.put(singleCourse.major, push);
 	      		}
 	      	}
 	    }
 	      		    
 	    return in;
 	    
 	}
 
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	public void update(ArrayList<TrackedCourse> in ) throws IOException{
 		Gson gson = new Gson();
 	    	//Properly format json table
 	      File file = new File(Tracker.this.getFilesDir() , "RUTracker.json");
 	      	RandomAccessFile raf = new RandomAccessFile(file,"rw");
 	      	raf.setLength(0);//Clears RUTracker.json
 	      	for(int i =0;i<in.size();i++){
 	      		JsonElement json = gson.toJsonTree(in.get(i));
 		      	if(raf.length()==0){
 		      		raf.writeBytes("["+json.toString()+"]");
 		      	}
 		      	else{
 		          	raf.setLength(file.length()-1);
 		          	raf.seek(file.length());
 		          	raf.writeBytes(","+json.toString()+"]");
 		      	}
 		      	raf.close();
 	      	}
 
 	      	closed.clear();
 	    
 	}
 
 
 	public void checkOpen(ArrayList<TrackedCourse> in) throws Exception{
 		ArrayList<Course> curr = CourseWriter.create(in.get(0).major, in.get(0).term, in.get(0).campus);
 		for(int k = 0;k<in.size();k++){
 		for(int i= 0;i<curr.size();i++){
 				if(curr.get(i).title.equals(in.get(k).course)){
 				Section[] check =  curr.get(i).sections;
 				for(int j = 0;j<check.length;j++){
 					if(check[j].index.equals(in.get(k).section)){
						if(check[j].openStatus==true)
 						open.add(in.get(k));
 						else{
 							closed.add(in.get(k));
 						}
 					}
 				}
 				}
 			}
 
 		}
 	
 }
 
 }
