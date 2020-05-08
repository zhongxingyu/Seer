 package org.imaginationforpeople.android.activity;
 
 import java.util.List;
 
 import org.imaginationforpeople.android.R;
 import org.imaginationforpeople.android.adapter.ProjectViewAdapter;
 import org.imaginationforpeople.android.handler.ProjectViewHandler;
 import org.imaginationforpeople.android.helper.DataHelper;
 import org.imaginationforpeople.android.helper.UriHelper;
 import org.imaginationforpeople.android.model.I4pProjectTranslation;
 import org.imaginationforpeople.android.sqlite.FavoriteSqlite;
 import org.imaginationforpeople.android.thread.ProjectViewThread;
 
 import com.viewpagerindicator.PageIndicator;
 import com.viewpagerindicator.TitlePageIndicator;
 
 import android.annotation.TargetApi;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.Window;
 import android.widget.Toast;
 
 public class ProjectViewActivity extends FragmentActivity {
 	private boolean displayMenu = false;
 	private Intent shareIntent;
 	private FavoriteSqlite db;
 	private ProjectViewThread thread;
 	private I4pProjectTranslation project;
 	
 	@TargetApi(14)
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		if(displayMenu) {
 			// Inflate menu only if it hasn't been done before
 			if(menu.size() == 0) {
 				// Inflating the menu
 				MenuInflater inflater = getMenuInflater();
 				inflater.inflate(R.menu.projectview, menu);
 				
 				// Creating share intent
 				Intent prepareShareIntent = new Intent(Intent.ACTION_SEND);
 				prepareShareIntent.putExtra(Intent.EXTRA_TEXT, UriHelper.getProjectUrl(project));
 				prepareShareIntent.putExtra(Intent.EXTRA_SUBJECT, project.getTitle());
 				prepareShareIntent.setType("text/plain");
 				shareIntent = Intent.createChooser(prepareShareIntent, getResources().getText(R.string.projectview_menu_share_dialog));
 			}
 			
 			// Defining favorite state
			MenuItem favoriteItem = menu.getItem(1);
 			if(db.isFavorite(project))
 				favoriteItem.setTitle(R.string.projectview_menu_favorites_remove);
 			else
 				favoriteItem.setTitle(R.string.projectview_menu_favorites_add);
 		}
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case android.R.id.home:
 			if(getIntent().getData() != null) {
 				Intent intent = new Intent(this, HomepageActivity.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
 				startActivity(intent);
 			} else 
 				finish();
 			break;
 		case R.id.projectview_favorite:
 			Toast t;
 			if(db.isFavorite(project)) {
 				db.removeFavorite(project);
 				t = Toast.makeText(this, getResources().getString(R.string.projectview_toast_favorites_remove, project.getTitle()), Toast.LENGTH_SHORT);
 			} else {
 				db.addFavorite(project);
 				t = Toast.makeText(this, getResources().getString(R.string.projectview_toast_favorites_add, project.getTitle()), Toast.LENGTH_SHORT);
 			}
 			t.show();
 			break;
 		case R.id.projectview_share:
 			startActivity(shareIntent);
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@TargetApi(11)
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if(Build.VERSION.SDK_INT < 11)
 			requestWindowFeature(Window.FEATURE_NO_TITLE);
 		db = new FavoriteSqlite(this);
 		
 		if(Build.VERSION.SDK_INT >= 11)
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		
 		if(savedInstanceState != null && savedInstanceState.containsKey(DataHelper.PROJECT_VIEW_KEY)) {
 			project = savedInstanceState.getParcelable(DataHelper.PROJECT_VIEW_KEY);
 			displayProject();
 		} else {
 			setContentView(R.layout.loading);
 			ProjectViewHandler handler = new ProjectViewHandler(this);
 			
 			String projectLang = null;
 			String projectSlug = null;
 			
 			Uri data = getIntent().getData();
 			if(data != null) {
 				List<String> path = data.getPathSegments();
 				projectLang = path.get(0);
 				projectSlug = path.get(2);
 			} else {
 				Bundle extras = getIntent().getExtras();
 				
 				if(extras.containsKey("project_title"))
 					setTitle(extras.getString("project_title"));
 				
 				if(extras.containsKey("project_id")) { // Mostly used if we want a random project
 					thread = new ProjectViewThread(handler, extras.getInt("project_id"));
 				} else {
 					projectLang = extras.getString("project_lang");
 					projectSlug = extras.getString("project_slug");
 				}
 			}
 			
 			if(thread == null)
 				thread = new ProjectViewThread(handler, projectLang, projectSlug);
 			
 			thread.start();
 		}
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		if(thread == null || !thread.isAlive())
 			outState.putParcelable(DataHelper.PROJECT_VIEW_KEY, project);
 		super.onSaveInstanceState(outState);
 	}
 	
 	@Override
 	protected void onStop() {
 		if(thread != null)
 			thread.requestStop();
 		super.onStop();
 	}
 	
 	public void setProject(I4pProjectTranslation p) {
 		project = p;
 	}
 	
 	@TargetApi(11)
 	public void displayProject() {
 		setContentView(R.layout.projectview_root);
 		displayMenu = true;
 		if(Build.VERSION.SDK_INT >= 11)
 			invalidateOptionsMenu(); // Rebuild the menu
 		
 		setTitle(project.getTitle());
 		
 		ProjectViewAdapter adapter = new ProjectViewAdapter(getSupportFragmentManager(), project, getResources());
 		
 		ViewPager pager = (ViewPager)findViewById(R.id.pager);
         pager.setAdapter(adapter);
 
         PageIndicator indicator = (TitlePageIndicator)findViewById(R.id.indicator);
         indicator.setViewPager(pager);
 	}
 }
