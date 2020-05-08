 package com.github.mobileartisans.bawall;
 
import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import com.github.mobileartisans.bawall.component.ProgressAsyncTask;
 import com.github.mobileartisans.bawall.domain.AkhadaClient;
 import com.github.mobileartisans.bawall.domain.UserPreference;
 
 import java.util.List;
 
 public class ProjectPreference extends ListActivity {
 
     private List<String> projectList;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
         new ProjectListTask(this).execute();
     }
 
     @Override
     protected void onListItemClick(ListView parent, View v, int position, long id) {
         final String selectedProject = projectList.get(position);
         new UserPreference(this).update(new UserPreference.PreferenceUpdate() {
             public UserPreference.Preference update(UserPreference.Preference preference) {
                 return preference.withDefaultProject(selectedProject);
             }
         });
         startActivity(new Intent(this, HomeActivity.class));
     }
 
     public class ProjectListTask extends ProgressAsyncTask<Void, Void, List<String>> {
 
         public ProjectListTask(Context context) {
             super(context);
         }
 
         @Override
         protected List<String> doInBackground(Void... param) {
             UserPreference.Preference preference = new UserPreference(context).getPreference();
             return new AkhadaClient(preference).getProjects();
         }
 
         @Override
         protected void onPostExecute(List<String> strings) {
             super.onPostExecute(strings);
             projectList = strings;
             UserPreference.Preference preference = new UserPreference(ProjectPreference.this).getPreference();
             setListAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_checked, projectList));
             getListView().setItemChecked(projectList.indexOf(preference.defaultProject), true);
 
         }
     }
 
 }
