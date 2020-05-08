 package org.imaginationforpeople.android.activity;
 
 import java.util.ArrayList;
 
 import org.imaginationforpeople.android.R;
 import org.imaginationforpeople.android.adapter.ProjectsGridAdapter;
 import org.imaginationforpeople.android.handler.ProjectsListHandler;
 import org.imaginationforpeople.android.handler.ProjectsListImageHandler;
 import org.imaginationforpeople.android.helper.DataHelper;
 import org.imaginationforpeople.android.helper.LanguageHelper;
 import org.imaginationforpeople.android.homepage.TabHelper;
 import org.imaginationforpeople.android.homepage.TabHelperEclair;
 import org.imaginationforpeople.android.homepage.TabHelperHoneycomb;
 import org.imaginationforpeople.android.model.I4pProjectTranslation;
 import org.imaginationforpeople.android.shake.ShakeEventListener;
 import org.imaginationforpeople.android.shake.ShakeListener;
 import org.imaginationforpeople.android.sqlite.FavoriteSqlite;
 import org.imaginationforpeople.android.thread.ProjectsListImagesThread;
 import org.imaginationforpeople.android.thread.ProjectsListThread;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.DialogInterface.OnClickListener;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 public class HomepageActivity extends Activity implements OnClickListener, OnCancelListener, ShakeListener {
 	private static ProjectsListThread bestThread;
 	private static ProjectsListThread latestThread;
 	private ProjectsListImagesThread bestImageThread;
 	private ProjectsListImagesThread latestImageThread;
 	private ArrayList<I4pProjectTranslation> bestProjects;
 	private ArrayList<I4pProjectTranslation> latestProjects;
 	private ProjectsGridAdapter bestAdapter;
 	private static ProgressDialog progress;
 	private AlertDialog languagesDialog;
 	private SharedPreferences preferences;
 	private ProjectsListHandler handler;
 	private TabHelper tabHelper;
 	private ShakeEventListener shaker;
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.projectslist, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case R.id.homepage_favorites:
 			FavoriteSqlite db = new FavoriteSqlite(this);
 			if(db.hasFavorites()) {
 				Intent intent = new Intent(this, FavoritesActivity.class);
 				startActivity(intent);
 			} else {
 				Toast t = Toast.makeText(this, R.string.favorites_no, Toast.LENGTH_SHORT);
 				t.show();
 			}
 			break;
 		case R.id.homepage_lang:
 			languagesDialog.show();
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.homepage);
 		// -- Initializing application
 		preferences = getPreferences(Context.MODE_PRIVATE);
 		LanguageHelper.setSharedPreferences(preferences);
 		
 		// -- Initializing projects list
 		if(savedInstanceState != null) {
 			bestProjects = savedInstanceState.getParcelableArrayList(DataHelper.BEST_PROJECTS_KEY);
 			latestProjects = savedInstanceState.getParcelableArrayList(DataHelper.LATEST_PROJECTS_KEY);
 		} else { 
 			bestProjects = new ArrayList<I4pProjectTranslation>();
 			latestProjects = new ArrayList<I4pProjectTranslation>();
 		}
 		bestAdapter = new ProjectsGridAdapter(this, bestProjects);
 		ProjectsGridAdapter latestAdapter = new ProjectsGridAdapter(this, latestProjects);
 		
 		if(Build.VERSION.SDK_INT >= 11)
 			tabHelper = new TabHelperHoneycomb();
 		else
 			tabHelper = new TabHelperEclair();
 		
 		tabHelper.setActivity(this);
 		tabHelper.setBestProjectsAdapter(bestAdapter);
 		tabHelper.setLatestProjectsAdapter(latestAdapter);
 		tabHelper.init();
 		
 		if(savedInstanceState != null && savedInstanceState.containsKey(TabHelper.STATE_KEY))
 			tabHelper.restoreCurrentTab(savedInstanceState.getInt(TabHelper.STATE_KEY));
 		
 		progress = new ProgressDialog(this);
 		progress.setOnCancelListener(this);
 		progress.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getText(R.string.cancel), this);
 		
 		handler = new ProjectsListHandler(this, progress, bestAdapter, latestAdapter);
 		
 		if(bestAdapter.getCount() == 0 || latestAdapter.getCount() == 0)
 			loadProjects();
 		else
 			launchAsynchronousImageDownload();
 		
 		// -- Initializing language chooser UI
 		int selectedLanguage = LanguageHelper.getPreferredLanguageInt();
 		
 		AlertDialog.Builder languagesBuilder = new AlertDialog.Builder(this);
 		languagesBuilder.setTitle(R.string.homepage_spinner_prompt);
 		languagesBuilder.setSingleChoiceItems(R.array.homepage_spinner_languages, selectedLanguage, this);
 		languagesDialog = languagesBuilder.create();
 		
 		// -- Initializing shaker
 		shaker = new ShakeEventListener(this);
 	}
 	
 	@Override
 	protected void onRestart() {
 		super.onRestart();
 		launchAsynchronousImageDownload();
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		tabHelper.saveCurrentTab(outState);
 		outState.putParcelableArrayList(DataHelper.BEST_PROJECTS_KEY, bestProjects);
 		outState.putParcelableArrayList(DataHelper.LATEST_PROJECTS_KEY, latestProjects);
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	protected void onPause() {
 		shaker.unregisterListener();
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		shaker.registerListener();
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		requestStopThreads();
 		if(languagesDialog.isShowing())
 			languagesDialog.cancel();
 		progress.dismiss();
 	}
 	
 	public void onClick(DialogInterface dialog, int which) {
 		if(which == DialogInterface.BUTTON_NEGATIVE) {
 			finish();
 		} else if(which != LanguageHelper.getPreferredLanguageInt()) {
 			Editor editor = preferences.edit();
 			editor.putInt("language", which);
 			editor.commit();
 			loadProjects();
 		}
 		dialog.dismiss();
 	}
 	
 	public void onCancel(DialogInterface arg0) {
 		finish();
 	}
 	
 	public void loadProjects() {
 		bestThread = new ProjectsListThread(handler, ProjectsListHandler.BEST_PROJECTS);
 		latestThread = new ProjectsListThread(handler, ProjectsListHandler.LATEST_PROJECTS);
 		bestThread.start();
 		latestThread.start();
 	}
 	
 	public void launchAsynchronousImageDownload() {
 		ProjectsListImageHandler handler = new ProjectsListImageHandler(bestAdapter);
 		if(bestImageThread == null || !bestImageThread.isAlive()) {
 			bestImageThread = new ProjectsListImagesThread(handler, bestProjects);
 			bestImageThread.start();
 		}
 		if(latestImageThread == null || !latestImageThread.isAlive()) {
 			latestImageThread = new ProjectsListImagesThread(handler, latestProjects);
 			latestImageThread.start();
 		}
 	}
 	
 	public void requestStopThreads() {
 		if(bestThread != null)
 			bestThread.requestStop();
 		if(latestThread != null)
 			latestThread.requestStop();
 		if(bestImageThread != null)
 			bestImageThread.requestStop();
 		if(latestImageThread != null)
 			latestImageThread.requestStop();
 	}
 
 	public void onShake() {
 		Intent intent = new Intent(this, ProjectViewActivity.class);
 		intent.putExtra("project_id", DataHelper.PROJECT_RANDOM);
 		startActivity(intent);
 	}
 }
