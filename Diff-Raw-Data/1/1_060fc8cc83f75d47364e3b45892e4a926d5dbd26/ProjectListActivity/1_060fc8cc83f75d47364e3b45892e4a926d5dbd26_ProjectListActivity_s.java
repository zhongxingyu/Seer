 package org.akvo.rsr.android;
 
 import org.akvo.rsr.android.dao.RsrDbAdapter;
 import org.akvo.rsr.android.service.GetProjecDataService;
 import org.akvo.rsr.android.view.adapter.ProjectListCursorAdapter;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.ListActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Build;
 
 public class ProjectListActivity extends ListActivity {
 
 
 	private static final String TAG = "ProjectListActivity";
 
 	private RsrDbAdapter ad;
 	private Cursor dataCursor;
 	private TextView projCountLabel;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_project_list);
 
 		projCountLabel = (TextView) findViewById(R.id.projcountlabel);
 
 		Button refreshButton = (Button) findViewById(R.id.button_refresh_projects);		
 		refreshButton.setOnClickListener( new View.OnClickListener() {
 			public void onClick(View view) {
 				ad.clearAllData();
 				//fetch new data
 				//TODO
 				
 				//redisplay list
 				getData();
 			}
 		});
  
         //Create db
         ad = new RsrDbAdapter(this);
 	}
 
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_project_list, menu);
 		return true;
 	}
 
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		ad.open();
 		getData();
 	}
 	
 	
 	@Override
 	protected void onDestroy() {
 		if (dataCursor != null) {
 			try {
 				dataCursor.close();
 			} catch (Exception e) {
 
 			}
 		}
 		if (ad != null) {
 			ad.close();
 		}
 		super.onDestroy();
 	}
 
 
 
 	/**
 	 * show all the projects in the database
 	 */
 	private void getData() {
 		try{
 		if(dataCursor != null){
 			dataCursor.close();
 		}
 		}catch(Exception e){
 			Log.w(TAG, "Could not close old cursor before reloading list",e);
 		}
 		dataCursor = ad.findAllProjects();
 
 		projCountLabel.setText(Integer.valueOf(dataCursor.getCount()).toString());
 
 		ProjectListCursorAdapter projects = new ProjectListCursorAdapter(this, dataCursor);
 		setListAdapter(projects);
 
 
 	}
 
 	/*
 	 * Start the service fetching new project data
 	 */
 	private void startGetProjectsService() {
 		Intent i = new Intent(this, GetProjecDataService.class);
 		i.putExtra(URL_KEY, "http://test.akvo.org/api/v1/project/?format=xml");
 		getApplicationContext().startService(i);
 	}
 
 
 
 }
