 package se.mah.kd330a.project.framework;
 
 
 
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 
 import org.json.JSONException;
 
 import net.fortuna.ical4j.data.ParserException;
 
 
 import se.mah.kd330a.project.schedule.data.KronoxJSON;
 import se.mah.kd330a.project.schedule.data.KronoxCalendar;
 import se.mah.kd330a.project.schedule.data.KronoxCourse;
 import se.mah.kd330a.project.schedule.data.KronoxReader;
 import se.mah.kd330a.project.R;
 import se.mah.kd330a.project.home.data.*;
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 
 import android.content.Context;
 
 import android.content.Intent;
 
 
 
 public class SplashActivity extends Activity {
 
     //how long until we go to the next activity
     protected int _splashTime = 4000;
     private String RSSNEWSFEEDURL = "http://www.mah.se/Nyheter/RSS/Nyheter-fran-Malmo-hogskola/";
     RSSFeed feed;
     FileOutputStream fout = null;
     ObjectOutputStream out = null;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_splash);
         /*ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		if (conMgr.getActiveNetworkInfo() == null
 				&& !conMgr.getActiveNetworkInfo().isConnected()
 				&& !conMgr.getActiveNetworkInfo().isAvailable()) {
 			// No connectivity - Show alert
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(
 					"Unable to reach server, \nPlease check your connectivity.")
 					.setTitle("Itslearning")
 					.setCancelable(false)
 					.setPositiveButton("Exit",
 							new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(DialogInterface dialog,
 										int id) {
 									finish();
 								}
 							});
 
 			AlertDialog alert = builder.create();
 			alert.show();
 
 		} else {
 		
         
 		}
        */
         ArrayList<KronoxCourse> courses = new ArrayList<KronoxCourse>();
 		courses.add(new KronoxCourse("KD330A-20132-62311"));
 		// this all seems cumbersome, but I need an array to pass to the ASyncTask
 		KronoxCourse[] courses_array = new KronoxCourse[courses.size()];
 		courses.toArray(courses_array);
 		try {
 			KronoxCalendar.createCalendar(KronoxReader.getFile(getApplicationContext()));
 			Log.i("try", "createCalender");
 		} catch(Exception e) {
 			new DownloadSchedule().execute(courses_array);
 			Log.i("try", "catch1");
 		} 
 		//setupListView();
 		//listToday();
 		new FetchCourseName().execute(courses_array);
 		new GetDataTask().execute();
     }
     
     private class DownloadSchedule extends AsyncTask<KronoxCourse,Void,Void> {
 		@Override
 		protected Void doInBackground(KronoxCourse... courses) {
 			try {
 				KronoxReader.update(getApplicationContext(), courses);
 			} catch(IOException e) {
 				e.printStackTrace();
 				// TODO: toast on error?
 			}
 			return null;
 		}
 		@Override
 		protected void onPostExecute(Void _void) {
 			// TODO: update current view
 		}
 	}
     private class FetchCourseName extends
 	  AsyncTask<KronoxCourse,Void,KronoxCourse> {
 		@Override
 		protected KronoxCourse doInBackground(KronoxCourse... courses) {
 			try {
 				return KronoxJSON.getCourse(courses[0].getFullCode());
 			} catch(IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch(JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 		}
 		@Override
 		protected void onPostExecute(KronoxCourse course) {
 			if(course != null) {
 				Log.i("Schedule",
 				      String.format("course:%s,%s", course.getFullCode(),
 				                    course.getName()));
 			}
 		}
 	}
 	
 
 	private class GetDataTask extends AsyncTask<Void, Void, String[]> {
     			
 
 		@Override
 		protected String[] doInBackground(Void... params) {
 			// Simulates a background job.
 			try {
 			DOMParser myParser = new DOMParser();
 			feed = myParser.parseXml(RSSNEWSFEEDURL);
 			} catch (Exception e) {
 				
 			}
 			return null;
 
 		}
 
 		@Override
 		protected void onPostExecute(String[] result) {
 			try {
 				fout = openFileOutput("filename", Context.MODE_PRIVATE);
 		        out = new ObjectOutputStream(fout);
 		        out.writeObject(feed);
 		        out.close();
 		        fout.close();
 		        
 		    } catch (IOException ioe) {
 		        System.out.println("Error in save method");
 
 		    } finally {
 		
 		    
 			Intent intent = new Intent(SplashActivity.this, MainActivity.class);
 			startActivity(intent);
 			Log.i("onPostExecute", Integer.toString(feed.getItemCount()));
 			
			finish();
 		    }
 		}
     
 			
 			
 			
 		
 	}
 }
