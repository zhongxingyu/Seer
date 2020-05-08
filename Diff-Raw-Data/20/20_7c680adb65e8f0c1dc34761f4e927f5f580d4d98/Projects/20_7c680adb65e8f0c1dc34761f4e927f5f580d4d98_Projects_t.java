 package com.calendar.reporter;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.LightingColorFilter;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.*;
 import com.calendar.reporter.database.project.ProjectDataSource;
 import com.calendar.reporter.database.project.ProjectStructure;
 import com.calendar.reporter.database.task.TaskStructure;
 import com.calendar.reporter.helper.Session;
 import com.calendar.reporter.helper.LocalDate;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.List;
 
 public class Projects extends ListActivity {
     private static final int PROJECT = 0;
     private static final int TABS = 0;
     private final Context context = this;
     private ArrayAdapter<ProjectStructure> adapter;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.projects);
 
         final ProjectDataSource dataSource = new ProjectDataSource(Projects.this);
         SharedPreferences settings = getSharedPreferences(Session.PREFS_NAME, 0);
         final Session session = new Session(settings);
         session.resetDate();
 
         AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 ProjectStructure project = (ProjectStructure) adapterView.getItemAtPosition(i);
 
                 SharedPreferences settings = getSharedPreferences(Session.PREFS_NAME, 0);
                 final Session session = new Session(settings);
                 session.setProjectId(project.getId());
                 session.setUserId(session.getUserId());
                 Calendar cal = Calendar.getInstance();
                 SimpleDateFormat sdf = new SimpleDateFormat(TaskStructure.DATE_FORMAT);
 
 
                 Intent cross = new Intent(view.getContext(
                 ), Tabs.class);
                 cross.putExtra("date", sdf.format(cal.getTime()));
                 startActivityForResult(cross, TABS);
             }
         };
         AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
             @Override
             public boolean onItemLongClick(AdapterView<?> adapterView, final View view, int i, long l) {
                 final ProjectStructure project = (ProjectStructure) adapterView.getItemAtPosition(i);
 
                 if (!project.getName().equals("N/A")) {
                     final CharSequence[] items = {"Edit", "Delete"};
                     final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                     builder.setTitle("Choose the action:");
 
                     builder.setItems(items, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int item) {
                             switch (item) {
                                 case 0:
                                     Intent cross = new Intent(view.getContext(), Project.class);
                                     cross.putExtra("type", "edit");
                                    session.setProjectId(project.getId());
                                     startActivityForResult(cross, PROJECT);
                                     break;
                                 case 1:
                                     AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                                     alertBuilder.setMessage("Do you want to remove " + project.getName() + "?");
                                     alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                         @Override
                                         public void onClick(DialogInterface dialog, int which) {
                                             adapter.remove(project);
                                             dataSource.deleteProject(project.getId());
                                             adapter.notifyDataSetChanged();
 
                                             Toast.makeText(getApplicationContext(), project.getName() +
                                                     " has been removed.", Toast.LENGTH_LONG).show();
                                         }
                                     });
                                     alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                         @Override
                                         public void onClick(DialogInterface dialog, int which) {
                                             dialog.cancel();
                                         }
                                     });
                                     alertBuilder.show();
                                     break;
                             }
                         }
                     });
                     builder.show();
                 }
                 return true;
             }
         };
         Button projectButton = (Button) findViewById(R.id.createProject);
         Button taskButton = (Button) findViewById(R.id.createTask);
         List<ProjectStructure> projects = dataSource.getAllProjects(session.getUserId());
 
         adapter = new ArrayAdapter<ProjectStructure>(this, android.R.layout.simple_list_item_1, projects);
         setListAdapter(adapter);
         getListView().setOnItemClickListener(onItemClickListener);
         getListView().setOnItemLongClickListener(onItemLongClickListener);
 
         projectButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent cross = new Intent(view.getContext(), Project.class);
                 cross.putExtra("type", "create");
                 startActivityForResult(cross, PROJECT);
             }
         });
         taskButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent cross = new Intent(view.getContext(), Task.class);
                 cross.putExtra("type", "create");
                 startActivityForResult(cross, PROJECT);
             }
         });
 
     }
 
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_BACK) {
             Intent intent = new Intent(this, Login.class);
             intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             startActivity(intent);
             return true;
         }
         return super.onKeyDown(keyCode, event);
     }
 }
