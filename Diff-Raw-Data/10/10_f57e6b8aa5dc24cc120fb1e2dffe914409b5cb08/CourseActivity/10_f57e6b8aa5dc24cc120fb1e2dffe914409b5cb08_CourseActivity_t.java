 package no.hials.muldvarp.courses;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import com.google.gson.Gson;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.URI;
 import java.util.ArrayList;
 import no.hials.muldvarp.MainActivity;
 import no.hials.muldvarp.R;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 /**
  *
  * @author kristoffer
  */
 public class CourseActivity extends Activity {
     private boolean showGrid;
     private Fragment CourseListFragment = new CourseListFragment();
     private Fragment CourseGridFragment = new CourseGridFragment();
     private Fragment currentFragment;
     ArrayList<Course> courseList = new ArrayList<Course>();
     
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.course_main);
         
         String url = "http://master.uials.no:8080/muldvarp/resources/course";
         new DownloadItems().execute(url);
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putInt("course", getActionBar().getSelectedNavigationIndex());
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.course_activity, menu);
 //        MenuItem menuItem = menu.findItem(R.id.actionItem);
 //        
 //        menuItem.setOnActionExpandListener(new OnActionExpandListener() {
 //            @Override
 //            public boolean onMenuItemActionCollapse(MenuItem item) {
 //                // Do something when collapsed
 //                return true;  // Return true to collapse action view
 //            }
 //
 //            @Override
 //            public boolean onMenuItemActionExpand(MenuItem item) {
 //                // Do something when expanded
 //                return true;  // Return true to expand action view
 //            }
 //        });
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 // app icon in action bar clicked; go home
                 Intent intent = new Intent(this, MainActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
                 return true;
             case R.id.showgrid:
                 System.out.println("Fragment change button pressed");
                 if(showGrid == false) {
                     addDynamicFragment(CourseGridFragment);
                     showGrid = true;
                     System.out.println("Showing grid");
                 } else {
                     addDynamicFragment(CourseListFragment);
                     showGrid = false;
                     System.out.println("Showing list");
                 }
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     private void addDynamicFragment(Fragment fg) {
         FragmentTransaction ft = getFragmentManager().beginTransaction();
         
         if (currentFragment != null) {
             ft.detach(currentFragment);
         }
 
         ft.attach(fg);
         ft.add(R.id.course_layout, fg).commit();
         
         currentFragment = fg;
         System.out.println("Fragment changed");
     }
     
     public InputStream getJSONData(String url){
         DefaultHttpClient httpClient = new DefaultHttpClient();
         URI uri;
         InputStream data = null;
         try {
             uri = new URI(url);
             HttpGet method = new HttpGet(uri);
             HttpResponse response = httpClient.execute(method);
             data = response.getEntity().getContent();
         } catch (Exception e) {
             e.printStackTrace();
         }
         
         return data;
     }
     
     private class DownloadItems extends AsyncTask<String, Void, ArrayList<Course>> {
         ProgressDialog dialog;
         @Override
         protected void onPreExecute() {
             dialog = new ProgressDialog(CourseActivity.this);
             dialog.setMessage(getString(R.string.loading));
             dialog.setIndeterminate(true);
             dialog.setCancelable(false);
             dialog.show();
         }
         
         protected ArrayList<Course> doInBackground(String... urls) {
             ArrayList<Course> items = new ArrayList<Course>();
             try{
                 Reader json = new InputStreamReader(getJSONData(urls[0]));
                 Gson gson = new Gson();
                 SearchResults result = gson.fromJson(json, SearchResults.class);
                 items = (ArrayList<Course>)result.course;
             }catch(Exception ex){
                 ex.printStackTrace();
                 dialog.setCancelable(true);
                 dialog.setMessage(getString(R.string.cannot_connect));
             }
             return items;
         } 
         
         @Override
         protected void onPostExecute(ArrayList<Course> items) {
            courseList.clear();
            courseList.addAll(items);
             addDynamicFragment(CourseListFragment); // default view
             dialog.dismiss();
         }
     }
 
     public ArrayList<Course> getCourseList() {
         return courseList;
     }
 }
