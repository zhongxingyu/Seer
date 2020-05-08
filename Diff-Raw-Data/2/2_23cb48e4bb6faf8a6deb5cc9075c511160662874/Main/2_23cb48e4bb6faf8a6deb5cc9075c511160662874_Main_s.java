 package net.qmsource.android.cvut.kos.activity;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.qmsource.android.cvut.kos.auth.KosHttpClient;
 import net.qmsource.android.cvut.kos.domain.Course;
 import net.qmsource.android.cvut.kos.domain.CourseInstance;
 import net.qmsource.android.cvut.kos.domain.CourseInstanceEntry;
 import net.qmsource.android.cvut.kos.domain.CourseInstanceWrapper;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpUriRequest;
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.ArrayAdapter;
 import com.google.gson.Gson;
 import com.google.gson.JsonParseException;
 
 /**
  * Sample usage of KOSapi in Android using kos-auth-android library
  * @author Tonda Novak (http://qmsource.net/), 2011
  *
  */
 
 public class Main extends ListActivity {
     
	private static final String TAG = ListActivity.class.getSimpleName();
 	private static final String username = "username", password = "password";		//put your KOS credentials here!
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        
         getListView().post(new Runnable() {			//time consuming operations runs on separed thread
 			
 			@Override
 			public void run() {
 				 
 				HttpClient client = new KosHttpClient(username, password);		//instance KOS HTPP Client
 				
 				//KOSapi call (see https://kosapi.feld.cvut.cz/doc/2/resources) - this resource works only if 'username' is a student...
 				HttpUriRequest request = new HttpGet("https://kosapi.feld.cvut.cz/api/2/people/"+username+"/student/courseInstances");
 				request.setHeader("Accept", "application/json");		//json representation of resources
 				InputStream data = null;
 				Reader r = null;
 				try {
 					
 					Log.i(TAG, "starting request...");
 					
 					HttpResponse response = client.execute(request);
 					data = response.getEntity().getContent();
 					r = new InputStreamReader(data);
 					
 					
 					Gson gson = new Gson();
 					CourseInstanceWrapper list =  gson.fromJson(r, CourseInstanceWrapper.class);	//maping using GSON library - much faster and better then Android impl
 					
 					List<String> courseList = new ArrayList<String>();
 					
 					for(CourseInstanceEntry entry: list.courseInstance) {
 						
 						//course instance
 						HttpUriRequest classRequest = new HttpGet(entry.getUri());
 						classRequest.setHeader("Accept", "application/json");
 						
 						HttpResponse classResponse = client.execute(classRequest);
 						data = classResponse.getEntity().getContent();
 						r = new InputStreamReader(data);
 						
 						CourseInstance courseInstance = gson.fromJson(r, CourseInstance.class);
 						
 						//course detail
 						HttpUriRequest courseRequest = new HttpGet(courseInstance.getCourse().getUri());
 						courseRequest.setHeader("Accept", "application/json");
 						
 						HttpResponse courseResponse = client.execute(courseRequest);
 						data = courseResponse.getEntity().getContent();
 						r = new InputStreamReader(data);
 						
 						Course course = gson.fromJson(r, Course.class);
 						courseList.add(course.getNameCz());
 						
 						r.close();
 						data.close();
 					}
 					
 					setListAdapter(new ArrayAdapter(Main.this, android.R.layout.simple_list_item_1, courseList.toArray()));
 					
 				} catch (ClientProtocolException e) {
 					Log.e(TAG, "todo error handlig", e);
 				} catch (IOException e) {
 					Log.e(TAG, "todo error handlig", e);
 				} catch (NullPointerException e) {
 					Log.e(TAG, "todo error handlig", e);
 				} catch (JsonParseException e) {
 					Log.e(TAG, "todo error handlig", e);
 				} finally {
 					try {
 						r.close();
 						data.close();
 					} catch (IOException e) {
 						Log.e(TAG, "todo error handlig", e);
 					}
 				}
 				
 			}
         });
         
     }
     
 }
