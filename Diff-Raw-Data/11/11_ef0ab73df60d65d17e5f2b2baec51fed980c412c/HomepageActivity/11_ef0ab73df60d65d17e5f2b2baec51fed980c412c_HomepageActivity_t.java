 package org.imaginationforpeople.android2.activity;
 
 import java.util.ArrayList;
 
 import org.imaginationforpeople.android2.R;
 import org.imaginationforpeople.android2.fragment.FavoritesFragment;
 import org.imaginationforpeople.android2.fragment.GroupListFragment;
 import org.imaginationforpeople.android2.fragment.LoadingFragment;
 import org.imaginationforpeople.android2.fragment.ProjectListFragment;
 import org.imaginationforpeople.android2.helper.DataHelper;
 import org.imaginationforpeople.android2.helper.LanguageHelper;
 import org.imaginationforpeople.android2.homepage.SpinnerHelper;
 import org.imaginationforpeople.android2.homepage.SpinnerHelper.OnSpinnerItemSelectedListener;
 import org.imaginationforpeople.android2.homepage.SpinnerHelperEclair;
 import org.imaginationforpeople.android2.homepage.SpinnerHelperHoneycomb;
 import org.imaginationforpeople.android2.model.Group;
 import org.imaginationforpeople.android2.model.I4pProjectTranslation;
 import org.imaginationforpeople.android2.shake.ShakeAnimation;
 import org.imaginationforpeople.android2.shake.ShakeEventListener;
 import org.imaginationforpeople.android2.shake.ShakeAnimation.AnimationListener;
 
 import com.darvds.ribbonmenu.OnRibbonChangeListener;
 import com.darvds.ribbonmenu.RibbonMenuView;
 import com.darvds.ribbonmenu.iRibbonMenuCallback;
 
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.DialogInterface.OnClickListener;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.SearchView;
 
 public class HomepageActivity extends FragmentActivity implements OnClickListener,
 		OnCancelListener, ShakeEventListener.ShakeListener, AnimationListener,
 		OnCheckedChangeListener, LoadingFragment.OnContentLoadedListener, OnSpinnerItemSelectedListener,
 		iRibbonMenuCallback, OnRibbonChangeListener {
 	private ArrayList<ArrayList<I4pProjectTranslation>> projects = new ArrayList<ArrayList<I4pProjectTranslation>>(DataHelper.CONTENT_NUMBER);
 	private ArrayList<Group> groups = new ArrayList<Group>();
 	private AlertDialog languagesDialog;
 	private SharedPreferences preferences;
 	private SpinnerHelper spinnerHelper;
 	private AlertDialog contentDialog;
 	private ShakeEventListener shaker;
 	private ShakeAnimation animation;
 	private SearchView searchView;
 	private RibbonMenuView rbm;
 	private int activeRibonItem = R.id.ribbon_menu_projects;
 	
 	@TargetApi(11)
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		switch(activeRibonItem) {
 		case R.id.ribbon_menu_projects:
 			inflater.inflate(R.menu.projectslist, menu);
 			
 			if(Build.VERSION.SDK_INT >= 11) {
 				SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
 				searchView = (SearchView) menu.findItem(R.id.homepage_search).getActionView();
 				searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
 				searchView.setIconifiedByDefault(true);
 			}
 			break;
 		case R.id.ribbon_menu_favorites:
 		case R.id.ribbon_menu_workgroups:
 			if(Build.VERSION.SDK_INT < 11)
 				inflater.inflate(R.menu.ribbon_controller, menu);
 			break;
 		}
 		
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		if(Build.VERSION.SDK_INT < 11) {
 			MenuItem ribbon = null;
 			switch(activeRibonItem) {
 			case R.id.ribbon_menu_projects:
 				ribbon = menu.getItem(2);
 				break;
 			default:
 				ribbon = menu.getItem(0);
 				break;
 			}
 			
 			if(rbm.isMenuVisible())
 				ribbon.setTitle(R.string.homepage_menu_ribbon_close);
 			else
 				ribbon.setTitle(R.string.homepage_menu_ribbon_open);
 		}		
 		return super.onPrepareOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case android.R.id.home:
 		case R.id.homepage_ribbon_button:
 			rbm.toggleMenu();
 			break;
 		case R.id.homepage_content:
 			AlertDialog.Builder contentBuilder = new AlertDialog.Builder(this);
 			contentBuilder.setTitle(R.string.homepage_spinner_content_prompt);
 			contentBuilder.setSingleChoiceItems(R.array.homepage_spinner_dropdown, spinnerHelper.getCurrentSelection(), spinnerHelper);
 			contentDialog = contentBuilder.create();
 			contentDialog.show();
 			break;
 		case R.id.homepage_search:
 			onSearchRequested();
 			break;
 		case R.id.homepage_lang:
 			languagesDialog.show();
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	@Override
 	public void RibbonMenuItemClick(int itemId) {
 		activeRibonItem = itemId;
 		Fragment fragment;
 		FragmentManager fm = getSupportFragmentManager();
 		switch(itemId) {
 		case R.id.ribbon_menu_projects:
 			spinnerHelper.init();
 			onSpinnerItemSelected(spinnerHelper.getCurrentSelection());
 			if(Build.VERSION.SDK_INT >= 11)
 				invalidateOptionsMenu();
 			break;
 		case R.id.ribbon_menu_workgroups:
 			setTitle(R.string.app_groups);
 			if(Build.VERSION.SDK_INT >= 11) {
 				getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
 				invalidateOptionsMenu();
 			}
 			Bundle data = new Bundle();
 			if(groups.size() > 0) {
 				data.putParcelableArrayList(GroupListFragment.GROUPS_KEY, groups);
 				fragment = new GroupListFragment();
 				fragment.setArguments(data);
 				fm.beginTransaction().replace(R.id.homepage_content, fragment).commit();
 			} else {
 				data.putInt(LoadingFragment.CONTENT_TO_LOAD, LoadingFragment.LOAD_GROUPS);
 				data.putInt(LoadingFragment.TEXT_RESID, R.string.loading_groups);
 				fragment = new LoadingFragment();
 				fragment.setArguments(data);
 				fm.beginTransaction().replace(R.id.homepage_content, fragment).commit();
 			}
 			break;
 		case R.id.ribbon_menu_favorites:
 			if(Build.VERSION.SDK_INT >= 11)
 				getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
 			fragment = new FavoritesFragment();
 			fm.beginTransaction().replace(R.id.homepage_content, fragment).commit();
 			break;
 		}
 	}
 	
 	@TargetApi(11)
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.homepage);
 		// -- Initializing application
 		preferences = getPreferences(Context.MODE_PRIVATE);
 		LanguageHelper.setSharedPreferences(preferences);
 		
 		// -- Initializing ribbon menu
 		rbm = (RibbonMenuView) findViewById(R.id.homepage_ribbon);
 		rbm.setListener(this);
 		rbm.setMenuClickCallback(this);
 		rbm.setMenuItems(R.menu.homepage_ribbon);
 		rbm.setActiveItem(R.id.ribbon_menu_projects);
 		
 		if(Build.VERSION.SDK_INT >= 11)
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		
 		// -- Initializing projects list
 		if(savedInstanceState != null) {
 			for(int i = 0; i < DataHelper.CONTENT_NUMBER; i++) {
 				ArrayList<I4pProjectTranslation> project = savedInstanceState.getParcelableArrayList("projects_" + String.valueOf(i)); 
 				projects.add(i, project); 
 			}
 			ArrayList<Group> groups = savedInstanceState.getParcelableArrayList("groups");
 			this.groups = groups;
 		} else
 			for(int i = 0; i < DataHelper.CONTENT_NUMBER; i++) {
 				projects.add(i, new ArrayList<I4pProjectTranslation>());
 			}
 		
		if(Build.VERSION.SDK_INT >= 11)
 			spinnerHelper = new SpinnerHelperHoneycomb(this);
 		else
 			spinnerHelper = new SpinnerHelperEclair();
 		
		if(savedInstanceState != null)
			RibbonMenuItemClick(savedInstanceState.getInt("ribbon_item"));
		
 		spinnerHelper.setListener(this);
 		if(activeRibonItem == R.id.ribbon_menu_projects) {
 			spinnerHelper.init();
 			
 			if(savedInstanceState != null && savedInstanceState.containsKey(SpinnerHelper.STATE_KEY))
 				spinnerHelper.restoreCurrentSelection(savedInstanceState.getInt(SpinnerHelper.STATE_KEY));
 		}
 		
 		// -- Initializing language chooser UI
 		int selectedLanguage = LanguageHelper.getPreferredLanguageInt();
 		
 		AlertDialog.Builder languagesBuilder = new AlertDialog.Builder(this);
 		languagesBuilder.setTitle(R.string.homepage_spinner_language_prompt);
 		languagesBuilder.setSingleChoiceItems(R.array.homepage_spinner_languages, selectedLanguage, this);
 		languagesDialog = languagesBuilder.create();
 		
 		// -- Initializing shaker
 		shaker = new ShakeEventListener(this);
 		if(Build.VERSION.SDK_INT >= 12)
 			animation = new ShakeAnimation(findViewById(R.id.homepage_shake), this);
 		else
 			animation = new ShakeAnimation(this, findViewById(R.id.homepage_shake), this);
 	}
 	
 	@TargetApi(11)
 	@Override
 	protected void onRestart() {
 		super.onRestart();
 		if(Build.VERSION.SDK_INT >= 11) {
 			// Calling twice: first empty text field, second iconify the view
 			searchView.setIconified(true);
 			searchView.setIconified(true);
 		}
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		for(int i = 0; i < DataHelper.CONTENT_NUMBER; i++)
 			outState.putParcelableArrayList("projects_" + String.valueOf(i), projects.get(i));
 		outState.putParcelableArrayList("groups", groups);
 		spinnerHelper.saveCurrentSelection(outState);
 		outState.putInt("ribbon_item", activeRibonItem);
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
 		if(languagesDialog.isShowing())
 			languagesDialog.cancel();
 	}
 	
 	@Override
 	public void onBackPressed() {
 		if(rbm.isMenuVisible())
 			rbm.hideMenu();
 		else
 			super.onBackPressed();
 	}
 
 	public void onClick(DialogInterface dialog, int which) {
 		if(which == DialogInterface.BUTTON_NEGATIVE) {
 			finish();
 		} else if(which != LanguageHelper.getPreferredLanguageInt()) {
 			Editor editor = preferences.edit();
 			editor.putInt("language", which);
 			editor.commit();
 			resetProjects();
 		}
 		dialog.dismiss();
 	}
 	
 	public void onCancel(DialogInterface arg0) {
 		finish();
 	}
 	
 	public void resetProjects() {
 		for(ArrayList<I4pProjectTranslation> array : projects)
 			array.clear();
 		onSpinnerItemSelected(spinnerHelper.getCurrentSelection());
 	}
 
 	public void onShake() {
 		Intent intent = new Intent(this, ProjectViewActivity.class);
 		intent.putExtra("project_id", DataHelper.PROJECT_RANDOM);
 		startActivity(intent);
 	}
 	
 	public void onLittleShake() {
 		if(preferences.getBoolean("shake_hint", true))
 			animation.showAnimation();
 	}
 	
 	public void onClick() {
 		animation.hideAnimation();
 		
 		View shakeLayout = getLayoutInflater().inflate(R.layout.shake_dialog, null);
 		CheckBox shakeCheckbox = (CheckBox) shakeLayout.findViewById(R.id.shake_checkbox);
 		shakeCheckbox.setOnCheckedChangeListener(this);
 		
 		AlertDialog.Builder shakeAlert = new AlertDialog.Builder(this);
 		shakeAlert.setIcon(android.R.drawable.ic_dialog_info);
 		shakeAlert.setTitle(R.string.shake_dialog_title);
 		shakeAlert.setView(shakeLayout);
 		shakeAlert.setPositiveButton(android.R.string.ok, null);
 		shakeAlert.create().show();
 	}
 	
 	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 		Editor editor = preferences.edit();
 		editor.putBoolean("shake_hint", !isChecked);
 		editor.commit();
 	}
 
 	@Override
 	public void onContentLoaded(int contentType, Bundle bundle) {
 		Fragment fragment = null;
 		switch(contentType) {
 		case LoadingFragment.LOAD_BESTOF_PROJECTS:
 		case LoadingFragment.LOAD_LATEST_PROJECTS:
 		case LoadingFragment.LOAD_MYCOUNTRY_PROJECTS:
 			ArrayList<I4pProjectTranslation> projectsList = bundle.getParcelableArrayList(ProjectListFragment.PROJECTS_KEY);
 			projects.set(contentType, projectsList);
 			fragment = new ProjectListFragment();
 			fragment.setArguments(bundle);
 			break;
 		case LoadingFragment.LOAD_GROUPS:
 			groups = bundle.getParcelableArrayList(GroupListFragment.GROUPS_KEY);
 			fragment = new GroupListFragment();
 			fragment.setArguments(bundle);
 		}
 		FragmentManager fm = getSupportFragmentManager();
 		fm.beginTransaction().replace(R.id.homepage_content, fragment).commit();
 	}
 
 	@Override
 	public void onSpinnerItemSelected(int itemId) {
 		if(Build.VERSION.SDK_INT >= 11)
 			setTitle("");
 		else
 			setTitle(getResources().getStringArray(R.array.homepage_spinner_dropdown)[itemId]);
 		
 		Bundle data = new Bundle();
 		data.putParcelableArrayList(ProjectListFragment.PROJECTS_KEY, projects.get(itemId));
 		Fragment fragment;
 		if(projects.get(itemId).size() > 0)
 			fragment = new ProjectListFragment();
 		else {
 			fragment = new LoadingFragment();
 			data.putInt(LoadingFragment.CONTENT_TO_LOAD, itemId);
 			switch(itemId) {
 			case LoadingFragment.LOAD_MYCOUNTRY_PROJECTS:
 				data.putInt(LoadingFragment.TEXT_RESID, R.string.loading_location);
 				break;
 			case LoadingFragment.LOAD_BESTOF_PROJECTS:
 			case LoadingFragment.LOAD_LATEST_PROJECTS:
 				data.putInt(LoadingFragment.TEXT_RESID, R.string.loading_projects);
 				break;
 			}
 		}
 		fragment.setArguments(data);
 		FragmentManager fm = getSupportFragmentManager();
 		fm.beginTransaction().replace(R.id.homepage_content, fragment).commit();
 	}
 	
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	@Override
 	public void onRibbonOpen() {
 		if(Build.VERSION.SDK_INT >= 11)
 			getActionBar().setDisplayHomeAsUpEnabled(false);
 	}
 	
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	@Override
 	public void onRibbonClose() {
 		if(Build.VERSION.SDK_INT >= 11)
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 	}
 }
