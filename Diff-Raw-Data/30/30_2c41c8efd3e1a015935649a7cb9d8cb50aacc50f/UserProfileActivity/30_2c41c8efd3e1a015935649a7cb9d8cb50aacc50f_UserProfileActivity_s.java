 package mit.edu.stemplusplus;
 
 import mit.edu.stemplusplus.helper.NavigateActionBar;
 import mit.edu.stemplusplus.helper.ParseDatabase;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 import java.util.ArrayList;
 
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseUser;
 
 import mit.edu.stemplusplus.helper.Project;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class UserProfileActivity extends NavigateActionBar {
     String username;
    Project[] projects;
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Parse.initialize(this, "iqIztkJYN0f0Y8iPYLjhpVYYFpV9zmnpBAoKTP1s", "4Z2u2qEfF4NtBq8PyIGjfewuhTU1iC7iEdxapoV5");
         setContentView(R.layout.activity_user_profile);
         Intent intent = getIntent();
         username = intent.getStringExtra("username");
         if(username==null)
             username = ParseUser.getCurrentUser().getUsername();
 
         TextView user = (TextView) findViewById(R.id.user_profile_activity_username_user);
         TextView reputation_points = (TextView) findViewById(R.id.user_profile_activity_reputation_points);
         user.setText(username);
         
         final ListView lv = (ListView) findViewById(R.id.user_profile_past_projects_list_view);
        String[] projects = getProject();
        lv.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,projects));
         lv.setOnItemClickListener(new OnItemClickListener(){
             @Override
             public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                String project = (String) lv.getItemAtPosition(position);
                //Intent intent = new Intent(UserProfileActivity.this, UserProfileActivity.class); //needs changed
                //intent.putExtra("project",project);
                //startActivity(intent);
             }
         });
         reputation_points.setText(projects.length+"");
     
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_user_profile, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         return navigate(item, this, true);
     }
     
     private ArrayList<Project> getProjectByUser(){
         try {
             return ParseDatabase.getListOfProjectsOfUser(username);
         } catch (ParseException e) {
             return new ArrayList<Project>();
         }
     }
    private String[] getProject(){
        ArrayList<Project> p = getProjectByUser();
        String[] strings = new String[p.size()];
        projects = new Project[p.size()];
        for(int i=0; i<p.size(); i++){
            strings[i] = p.get(i).getName();
            projects[i] = p.get(i);
        }
        return strings;
     }
 
 }
