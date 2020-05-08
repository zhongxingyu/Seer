 package com.BreakingBytes.SifterReader;
 
 /*      SifterReader.java
  *      
  *      Copyright 2012 Mark Mikofski <bwanamarko@yahoo.com>
  *      
  *      This program is free software; you can redistribute it and/or modify
  *      it under the terms of the GNU General Public License as published by
  *      the Free Software Foundation; either version 2 of the License, or
  *      (at your option) any later version.
  *      
  *      This program is distributed in the hope that it will be useful,
  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
  *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *      GNU General Public License for more details.
  *      
  *      You should have received a copy of the GNU General Public License
  *      along with this program; if not, write to the Free Software
  *      Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  *      MA 02110-1301, USA. */
 
 import java.io.File;
 import java.net.URLConnection;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.res.Resources.NotFoundException;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class SifterReader extends ListActivity {
 
 	// Menu options and activity request codes
 	public static final int LOGIN_ID = Menu.FIRST; // enter login keys
 	public static final int DELETE_ID = Menu.FIRST+1; // delete login keys
 	public static final int ACTIVITY_LOGIN = 0;
 //	public static final int ACTIVITY_DELETE = 1;
 	// Context menu options
 	public static final int MILESTONES_ID = Menu.FIRST + 1;
 	public static final int CATEGORIES_ID = Menu.FIRST + 2;
 	public static final int PEOPLE_ID = Menu.FIRST + 3;
 	public static final int ISSUES_ID = Menu.FIRST + 4;
 	// SifterAPI URL token errors: JSON-object keys and intent-bundle keys
 	public static final String LOGIN_ERROR = "error";
 	public static final String LOGIN_DETAIL = "detail";
 	// Login keys: JSON-object keys and intent-bundle keys
 	public static final String KEY_FILE = "key_file"; // internal-memory filename
 	public static final String DOMAIN = "domain";
 	public static final String ACCESS_KEY = "accessKey";
 	// SifterAPI URL - use constants in case they change
 	public static final String HTTPS_PREFIX = "https://";
 	public static final String PROJECTS_URL = ".sifterapp.com/api/";
 	// SifterAPI JSON-object keys, resource end-points and intent-bundle keys
 	public static final String PROJECTS = "projects";
 	public static final String PROJECT_NAME = "name";
 	public static final String MILESTONES_URL = "api_milestones_url";
 	public static final String MILESTONES = "milestones";
 	public static final String CATEGORIES_URL = "api_categories_url";
 	public static final String CATEGORIES = "categories";
 	public static final String PEOPLE_URL = "api_people_url";
 	public static final String PEOPLE = "people";
 	public static final String ISSUES_URL = "api_issues_url";
 	public static final String ISSUES = "issues";
 	
 	// Members
 	private SifterHelper mSifterHelper;
 	private JSONObject[] mAllProjects;
 	private String mDomain;
 	private String mAccessKey;
 	private JSONObject mLoginError = new JSONObject();
 	/* must initialize mLoginError as empty JSON object,
 	 * or mLoginError.put() will fail. */
 	private ProgressDialog mDialog;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		setTitle(R.string.projects);
 		registerForContextMenu(getListView());
 		
 		mSifterHelper = new SifterHelper(this);
 		boolean haveKeys;
 		try {
 			haveKeys = mSifterHelper.getKey();
 		} catch (Exception e) {
 			e.printStackTrace();
 			mSifterHelper.onException(e.toString());
 			return;
 		}
 		if (haveKeys) {
 			mDomain = mSifterHelper.mDomain;
 			mAccessKey = mSifterHelper.mAccessKey;
 		} else {
 			mLoginError = mSifterHelper.mLoginError;
 			loginKeys();
 			return;
 		}
 		String projectsURL = HTTPS_PREFIX + mDomain + PROJECTS_URL + PROJECTS;
 		URLConnection sifterConnection = mSifterHelper.getSifterConnection(projectsURL);
 		if (sifterConnection == null) {
 			loginKeys();
 			return;
 		}
 		mDialog = ProgressDialog.show(this, "", "Loading ...",true);
 		new DownloadSifterTask().execute(sifterConnection);
 	}
 	
 	private class DownloadSifterTask extends AsyncTask<URLConnection, Void, JSONObject> {
 		@Override
 		protected JSONObject doInBackground(URLConnection... connections) {
 			URLConnection sifterConnection = connections[0];
 			JSONObject sifterJSONObject = null;
 			try {
 				sifterJSONObject = mSifterHelper.getSifterJSONObject(sifterConnection);
 			} catch (Exception e) {
 				e.printStackTrace();
 				mSifterHelper.onException(e.toString());
 			}
 			return sifterJSONObject;
 		}
 		@Override
 		protected void onPostExecute(JSONObject sifterJSONObject) {
 			mDialog.dismiss();
 			if (sifterJSONObject == null)
 				return;
 			if (getSifterError(sifterJSONObject)) {
 				loginKeys();
 				return;
 			}
 			try {
 			loadProjects(sifterJSONObject);
 			} catch (JSONException e) {
 				e.printStackTrace();
 				mSifterHelper.onException(e.toString());
 				return;
 			}
 			JSONObject statuses = getSifterFilters(IssuesActivity.STATUSES);
 			JSONObject priorities = getSifterFilters(IssuesActivity.PRIORITIES);
 			if (statuses == null || priorities == null)
 				return;
 			mSifterHelper.saveSifterFilters(statuses, priorities);
 			fillData();
 		}
 	}
 	
 	/** Method to pass project names to list adapter. */
 	private void fillData() {
 		int pNum = mAllProjects.length;
 		String[] p = new String[pNum];
 		try {
 			for (int j = 0; j < pNum; j++)
 				p[j] = mAllProjects[j].getString(PROJECT_NAME);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			mSifterHelper.onException(e.toString());
 			return;
 		}
 		setListAdapter(new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_1, p));
 	}
 
 	/** Menu button options. */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		boolean result = super.onCreateOptionsMenu(menu);
 		menu.add(0, LOGIN_ID, 0, R.string.menu_login);
 		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
 		return result;
 	}
 
 	/** Methods for selected menu option. */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case LOGIN_ID:
 			loginKeys();
 			return true;
 		case DELETE_ID:
 			deleteKeys();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	/** start LoginActivity to get domain and access key. */
 	private void loginKeys() {
 		Intent intent = new Intent(this, LoginActivity.class);
 		intent.putExtra(DOMAIN, mDomain);
 		intent.putExtra(ACCESS_KEY, mAccessKey);
 		intent.putExtra(LOGIN_ERROR, mLoginError.toString());
 		startActivityForResult(intent, ACTIVITY_LOGIN);
 	}
 	
 	private void deleteKeys() {
 		File keyFile = getFileStreamPath(KEY_FILE);
 		if (keyFile.exists()) {
 			keyFile.delete();
 			mAllProjects = null;
 			mDomain = null;
 			mAccessKey = null;
 			try {
 				mLoginError = mSifterHelper.onMissingToken();
 			} catch (Exception e) {
 				e.printStackTrace();
 				mSifterHelper.onException(e.toString());
 				return;
 			}
 			setListAdapter(null);
 			onContentChanged();
 		}
 	}
 
 	/** start ProjectDetail activity for clicked project in list. */
 	@Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         super.onListItemClick(l, v, position, id);
         Intent intent = new Intent(this, ProjectDetail.class);
         intent.putExtra(PROJECTS, mAllProjects[(int)id].toString());
         // TODO use safe long typecast to int
         startActivity(intent);
     }
 	
 	/** List context menu options. */
 	@Override
     public void onCreateContextMenu(ContextMenu menu, View v,
             ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         menu.add(0, MILESTONES_ID, 0, R.string.milestones);
         menu.add(0, CATEGORIES_ID, 0, R.string.categories);
         menu.add(0, PEOPLE_ID, 0, R.string.people);
         menu.add(0, ISSUES_ID, 0, R.string.issues);
         menu.setHeaderTitle(R.string.menu_header);
     }
 
     /** Methods for selected list context menu option. */
 	@Override
     public boolean onContextItemSelected(MenuItem item) {
     	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
         switch(item.getItemId()) {
             case MILESTONES_ID:
             	getProjDetail(info.id, MILESTONES_URL, MILESTONES, MilestonesActivity.class);
                 return true;
             case CATEGORIES_ID:
             	getProjDetail(info.id,CATEGORIES_URL,CATEGORIES,CategoriesActivity.class);
                 return true;
             case PEOPLE_ID:
             	getProjDetail(info.id,PEOPLE_URL,PEOPLE,PeopleActivity.class);
                 return true;
             case ISSUES_ID:
             	getProjDetail(info.id,ISSUES_URL,ISSUES,IssuesActivity.class);
                 return true;
         }
         return super.onContextItemSelected(item);
     }
     
 	/** start project details activities (milestones, categories, people, issues) */
     private void getProjDetail(long id, String PROJ_DETAIL_URL, String PROJ_DETAIL, Class<?> cls) {
     	String projDetailURL = null;
     	try {
     		projDetailURL = mAllProjects[(int)id].getString(PROJ_DETAIL_URL);
     		// TODO use safe long typecast to int
     	} catch (JSONException e) {
     		e.printStackTrace();
 			mSifterHelper.onException(e.toString());
 			return;
     	}
     	String issuesURL = projDetailURL;
 		if (PROJ_DETAIL.equals(ISSUES)) {
 			projDetailURL += getFilterSlug();
     	}
     	URLConnection sifterConnection = mSifterHelper.getSifterConnection(projDetailURL);
 		if (sifterConnection == null)
 			return;
 		mDialog = ProgressDialog.show(this, "", "Loading ...",true);
 		new DownloadSifterDetailTask().execute(sifterConnection, issuesURL, PROJ_DETAIL, cls);
 	}
     
     private class DownloadSifterDetailTask extends AsyncTask<Object, Void, JSONObject> {
     	String issuesURL;
     	String PROJ_DETAIL;
     	Class<?> cls;
 		@Override
 		protected JSONObject doInBackground(Object... objects) {
 			URLConnection sifterConnection = (URLConnection) objects[0];
 			issuesURL = (String) objects[1];
 			PROJ_DETAIL = (String) objects[2];
 			cls = (Class<?>) objects[3];
 			JSONObject sifterJSONObject = null;
 			try {
 				sifterJSONObject = mSifterHelper.getSifterJSONObject(sifterConnection);
 			} catch (Exception e) {
 				e.printStackTrace();
 				mSifterHelper.onException(e.toString());
 			}
 			return sifterJSONObject;
 		}
 		@Override
 		protected void onPostExecute(JSONObject sifterJSONObject) {
 			mDialog.dismiss();
 			if (sifterJSONObject == null)
 				return;
 			if (getSifterError(sifterJSONObject)) {
 				loginKeys();
 				return;
 			}
 			if (PROJ_DETAIL.equals(ISSUES)) {
 				Intent intent = new Intent(getBaseContext(), cls);
 				intent.putExtra(ISSUES, sifterJSONObject.toString());
 				intent.putExtra(ISSUES_URL, issuesURL);
 				startActivity(intent);
 				return;
 			}
 			JSONArray projDetail = new JSONArray();
 			try {
 				projDetail = sifterJSONObject.getJSONArray(PROJ_DETAIL);
 			} catch (JSONException e) {
 				e.printStackTrace();
 				mSifterHelper.onException(e.toString());
 				return;
 			}		
 			Intent intent = new Intent(getBaseContext(), cls);
 			intent.putExtra(PROJ_DETAIL, projDetail.toString());
 			startActivity(intent);
 		}
 	}
 	
     
     private String getFilterSlug() {
     	String projDetailURL = new String();
 		int issuesPerPage = IssuesActivity.MAX_PER_PAGE;
 		JSONArray status = new JSONArray();
 		JSONArray priority = new JSONArray();
 		int numStatuses;
 		int numPriorities;
 		boolean[] filterStatus;
 		boolean[] filterPriority;
 		try {
 			JSONObject filters = mSifterHelper.getFiltersFile();
 			if (filters.length() == 0)
 				return new String();
 			issuesPerPage = filters.getInt(IssuesActivity.PER_PAGE);
 			status = filters.getJSONArray(IssuesActivity.STATUS);
 			priority = filters.getJSONArray(IssuesActivity.PRIORITY);
 			numStatuses = status.length();
 			numPriorities = priority.length();
 			filterStatus = new boolean[numStatuses];
 			filterPriority = new boolean[numPriorities];
 			for (int i = 0; i < numStatuses; i++)
 				filterStatus[i] = status.getBoolean(i);
 			for (int i = 0; i < numPriorities; i++)
 				filterPriority[i] = priority.getBoolean(i);
 		} catch (Exception e) {
 			e.printStackTrace();
 			mSifterHelper.onException(e.toString());
 			return new String();
 		}
 		projDetailURL = "?" + IssuesActivity.PER_PAGE + "=" + issuesPerPage;
 		projDetailURL += "&" + IssuesActivity.GOTO_PAGE + "=1";
 		JSONObject statuses = new JSONObject();
 		JSONObject priorities = new JSONObject();
 		JSONArray statusNames = new JSONArray();
 		JSONArray priorityNames = new JSONArray();
 		try {
 			JSONObject sifterJSONObject = mSifterHelper.getSifterFilters();
 			statuses = sifterJSONObject.getJSONObject(IssuesActivity.STATUSES);
 			priorities = sifterJSONObject.getJSONObject(IssuesActivity.PRIORITIES);
 			statusNames = statuses.names();
 			priorityNames = priorities.names();
 		} catch (Exception e) {
 			e.printStackTrace();
 			mSifterHelper.onException(e.toString());
 			return new String();
 		}
 		try {
 			String filterSlug = "&s=";
 			for (int i = 0; i < numStatuses; i++) {
 				if (filterStatus[i])
 					filterSlug += String.valueOf(statuses.getInt(statusNames.getString(i))) + "-";
 			}
 			if (filterSlug.length() > 3) {
 				filterSlug = filterSlug.substring(0, filterSlug.length()-1);
 				projDetailURL += filterSlug;
 			}
 			filterSlug = "&p=";
 			for (int i = 0; i < numPriorities; i++) {
 				if (filterPriority[i])
 					filterSlug += String.valueOf(priorities.getInt(priorityNames.getString(i))) + "-";
 			}
 			if (filterSlug.length() > 3) {
 				filterSlug = filterSlug.substring(0, filterSlug.length()-1);
 				projDetailURL += filterSlug;
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			mSifterHelper.onException(e.toString());
 			return new String();
 		}
 		return projDetailURL;
 	}
 	
     /** Determine activity by result code. */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
     	super.onActivityResult(requestCode, resultCode, intent);
     	if (intent == null)
     		return;
 
     	Bundle extras = intent.getExtras();
     	if (extras == null)
     		return;
 
     	switch (requestCode) {
     	case ACTIVITY_LOGIN:
     		mDomain = extras.getString(DOMAIN);
     		mAccessKey = extras.getString(ACCESS_KEY);
     		mSifterHelper = new SifterHelper(this,mAccessKey);
     		if (mDomain.length()==0 || mAccessKey.length()==0) {
     			try {
     				mLoginError = mSifterHelper.onMissingToken();
     				loginKeys();
     			} catch (Exception e) {
     				e.printStackTrace();
     				mSifterHelper.onException(e.toString());
     				return;
     			}
     			break;
     		} // if keys are empty return to LoginActivity
     		String projectsURL = HTTPS_PREFIX + mDomain + PROJECTS_URL + PROJECTS;
     		URLConnection sifterConnection = mSifterHelper.getSifterConnection(projectsURL);
     		if (sifterConnection == null) {
     			loginKeys();
     			break;
     		} // if URL misformatted return to LoginActivity
    		mDialog = ProgressDialog.show(this, "", "Loading ...",true);
    		new DownloadSifterTask().execute(sifterConnection);
     		break;
     	}
     }
 	
     /** check if SifterAPI returned error
      * {"error":"Invalid Account","detail":"Please correct the account subdomain."}
 	 * {"error":"Invalid Token","detail":"Please make sure that you are using the correct token."} */
 	private boolean getSifterError(JSONObject sifterJSONObject) {
 		try {
 			JSONArray sifterJSONObjFieldNames = sifterJSONObject.names();
 			int numKeys = sifterJSONObjFieldNames.length();
 			if (numKeys==2
 					&& LOGIN_ERROR.equals(sifterJSONObjFieldNames.getString(0))
 					&& LOGIN_DETAIL.equals(sifterJSONObjFieldNames.getString(1))) {
 				mLoginError = sifterJSONObject;
 				return true;
 			}
 			mLoginError.put(LOGIN_ERROR,getResources().getString(R.string.token_accepted));
 			mLoginError.put(LOGIN_DETAIL,getResources().getString(R.string.token_accepted_msg));
 			return false;
 		} catch (NotFoundException e) {
 			e.printStackTrace();
 			mSifterHelper.onException(e.toString()); // return true below
 		} catch (JSONException e) {
 			e.printStackTrace();
 			mSifterHelper.onException(e.toString()); // return true below
 		}
 		return true;
 	}
 	
 	/** load all Sifter projects */
 	private void loadProjects(JSONObject sifterJSONObject) throws JSONException{
 		JSONArray projectArray = sifterJSONObject.getJSONArray(PROJECTS);
 		int numberProjects = projectArray.length();
 		JSONObject[] allProjects = new JSONObject[numberProjects];
 		for (int i = 0; i < numberProjects; i++)
 			allProjects[i] = projectArray.getJSONObject(i);
 		mAllProjects = allProjects;
 	}
 	
 	/** Load Sifter Statuses and Priorities */
 	private JSONObject getSifterFilters(String filter) {
		String filterURL = HTTPS_PREFIX + mDomain;
 		filterURL += PROJECTS_URL + filter;
 		URLConnection sifterConnection = mSifterHelper.getSifterConnection(filterURL);
 		if (sifterConnection == null)
 			return new JSONObject();
 		JSONObject filterJSONObject = new JSONObject();
 		try {
 			JSONObject sifterJSONObject = mSifterHelper.getSifterJSONObject(sifterConnection);
 			filterJSONObject = sifterJSONObject.getJSONObject(filter);
 		} catch (Exception e) {
 			e.printStackTrace();
 			mSifterHelper.onException(e.toString());
 			return new JSONObject();
 		}
 		return filterJSONObject;
 	}
 }
