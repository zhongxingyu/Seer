 package com.SifterReader.android;
 
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
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URLConnection;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.res.Resources.NotFoundException;
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
 	public static final int ACTIVITY_DELETE = 1;
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
 	// SifterAPI headers
 	public static final String X_SIFTER_TOKEN = "X-Sifter-Token";
 	public static final String HEADER_REQUEST_ACCEPT = "Accept";
 	public static final String APPLICATION_JSON = "application/json";
 	// SifterAPI URL - use constants in case they change
 	public static final String HTTPS_PREFIX = "https://";
 	public static final String PROJECTS_URL = ".sifterapp.com/api/projects";
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
 	public static final String OOPS = "oops";
 	
 	// Members
 	private SifterHelper mSifterHelper;
 	private JSONObject[] mAllProjects;
 	private String mDomain;
 	private String mAccessKey;
 	private JSONObject mLoginError = new JSONObject();
 	/* must initialize mLoginError as empty JSON object,
 	 * or mLoginError.toString() will fail. */
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		setTitle(R.string.projects);
 		registerForContextMenu(getListView());
 		
 		File keyFile = getFileStreamPath(KEY_FILE);
 		if (!keyFile.exists()) {
 			if (onMissingToken())
 				loginKeys();
 			return;
 		}
 		
 		boolean fileReadError = false;
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(keyFile));
 			String inputLine;
 			StringBuilder x = new StringBuilder();
 			while ((inputLine = in.readLine()) != null) {
 				x.append(inputLine);
 			}
 			in.close();
 			JSONObject loginKeys = new JSONObject(x.toString());
 			mDomain = loginKeys.getString(DOMAIN);
 			mAccessKey = loginKeys.getString(ACCESS_KEY);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			fileReadError = true;
 		} catch (IOException e) {
 			e.printStackTrace();
 			fileReadError = true;
 		} catch (JSONException e) {
 			e.printStackTrace();
 			fileReadError = true;
 		}
 		if (fileReadError || mDomain.isEmpty() || mAccessKey.isEmpty()) {
 			if (onMissingToken())
 				loginKeys();
 			return;
 		}
 		mSifterHelper = new SifterHelper(this,mAccessKey);
 		
 		String projectsURL = HTTPS_PREFIX + mDomain + PROJECTS_URL;
 		URLConnection sifterConnection = mSifterHelper.getSifterConnection(projectsURL);
 		if (sifterConnection == null) {
 			loginKeys();
 			return;
 		}
 		
 		JSONObject sifterJSONObject = new JSONObject();
 		try {
 			sifterJSONObject = mSifterHelper.getSifterJSONObject(sifterConnection);
 		} catch (Exception e) {
 			e.printStackTrace();
 			onException(e.toString());
 			return;
 		}
 		if (getSifterError(sifterJSONObject)) {
 			loginKeys();
 			return;
 		}
 
 		try {
 		loadProjects(sifterJSONObject);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			onException(e.toString());
 			return;
 		}
 		
 		fillData();
 	}
 	
 	private boolean onMissingToken(){
 		try {
 			mLoginError.put(LOGIN_ERROR,getResources().getString(R.string.token_missing));
 			mLoginError.put(LOGIN_DETAIL,getResources().getString(R.string.token_missing_msg));
 		} catch (NotFoundException e) {
 			e.printStackTrace();
 			onException(e.toString());
 			return false;
 		} catch (JSONException e) {
 			e.printStackTrace();
 			onException(e.toString());
 			return false;
 		}
 		return true;
 	}
 	
 	/** Intent for OopsActivity to display exceptions. */
 	private void onException(String eString) {
 		Intent intent = new Intent(this, OopsActivity.class);
 		intent.putExtra(OOPS, eString);
 		startActivity(intent);
 	}
 	
 	/** Method to pass project names to list adapter. */
 	private void fillData() {
 		int pNum = mAllProjects.length;
 		String[] p = new String[pNum];
 		try {
 			for (int j = 0; j < pNum; j++) {
 				p[j] = mAllProjects[j].getString(PROJECT_NAME);
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
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
 	
 	/** Intent for LoginActivity to get domain and access key. */
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
 			onMissingToken();
 			setListAdapter(null);
 			onContentChanged();
 		}
 	}
 
 	/** Intent for ProjectDetail activity for clicked project in list. */
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
     
 	/** Intent for Project Details Activities. */
     private void getProjDetail(long id, String PROJ_DETAIL_URL, String PROJ_DETAIL, Class<?> cls) {
     	String projDetailURL = null;
     	// get project detail url from project
     	try {
     		projDetailURL = mAllProjects[(int)id].getString(PROJ_DETAIL_URL);
     		// TODO use safe long typecast to int
     	} catch (JSONException e) {
     		e.printStackTrace();
 			onException(e.toString());
 			return;
     	}
     	// get url connection
     	URLConnection sifterConnection = mSifterHelper.getSifterConnection(projDetailURL);
 		if (sifterConnection == null)
 			return;
 		// get JSON object
 		JSONObject sifterJSONObject = new JSONObject();
 		try {
 			sifterJSONObject = mSifterHelper.getSifterJSONObject(sifterConnection);
 		} catch (Exception e) {
 			e.printStackTrace();
 			onException(e.toString());
 			return;
 		}
 		if (getSifterError(sifterJSONObject)) {
 			loginKeys();
 			return;
 		}
 		if (PROJ_DETAIL.equals(ISSUES)) {
 			Intent intent = new Intent(this, cls);
 			intent.putExtra(ISSUES, sifterJSONObject.toString());
 			intent.putExtra(ISSUES_URL, projDetailURL);
 			startActivity(intent);
 			return;
 		}
 		// get project detail
 		JSONArray projDetail = new JSONArray();
 		try {
 			projDetail = sifterJSONObject.getJSONArray(PROJ_DETAIL);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			onException(e.toString());
 			return;
 		}		
 		// intent for MilestonesActivity
     	Intent intent = new Intent(this, cls);
 		intent.putExtra(PROJ_DETAIL, projDetail.toString());
 		startActivity(intent);
 	}
 	
     /** Determine activity by result code. */
     @Override
     protected void onActivityResult(int requestCode, int resultCode,
     		Intent intent) {
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
     		if (mDomain.isEmpty() || mAccessKey.isEmpty()) {
     			if (onMissingToken())
     				loginKeys();
     			break;
     		} // if keys are empty return to LoginActivity
     		String projectsURL = HTTPS_PREFIX + mDomain + PROJECTS_URL;
     		URLConnection sifterConnection = mSifterHelper.getSifterConnection(projectsURL);
     		if (sifterConnection == null) {
     			loginKeys();
     			break;
     		} // if URL misformatted return to LoginActivity
     		JSONObject sifterJSONObject = new JSONObject();
     		try {
     			sifterJSONObject = mSifterHelper.getSifterJSONObject(sifterConnection);
     		} catch (Exception e) {
     			e.printStackTrace();
     			onException(e.toString());
     			return;
     		}
     		if (getSifterError(sifterJSONObject)) {
     			loginKeys();
     			break;
     		} // if SifterAPI reports error return LoginActivity
     		try {
     		loadProjects(sifterJSONObject);
     		} catch (JSONException e) {
     			e.printStackTrace();
     			onException(e.toString());
     			break;
     		}
     		fillData();
     		break;
     	}
     }
 	
 	private boolean getSifterError(JSONObject sifterJSONObject) {
 		try {
 			JSONArray sifterJSONObjFieldNames = sifterJSONObject.names();
 			int numKeys = sifterJSONObjFieldNames.length();
 			if (numKeys==2
 					&& LOGIN_ERROR.equals(sifterJSONObjFieldNames.getString(0))
 					&& LOGIN_DETAIL.equals(sifterJSONObjFieldNames.getString(1))) {
 				// check for incorrect header
 				// SifterAPI says:
 				// {"error":"Invalid Account","detail":"Please correct the account subdomain."}
 				// {"error":"Invalid Token","detail":"Please make sure that you are using the correct token."}
 				mLoginError = sifterJSONObject;
 				return true;
 			}
 			mLoginError.put(LOGIN_ERROR,getResources().getString(R.string.token_accepted));
 			mLoginError.put(LOGIN_DETAIL,getResources().getString(R.string.token_accepted_msg));
 			return false;
 		} catch (NotFoundException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return true;
 	}
 	
 	private void loadProjects(JSONObject sifterJSONObject) throws JSONException{
 		// array of projects
 		JSONArray projectArray = sifterJSONObject.getJSONArray(PROJECTS);
 		int numberProjects = projectArray.length();
 		JSONObject[] allProjects = new JSONObject[numberProjects];
 
 		// projects
 		for (int i = 0; i < numberProjects; i++) {
 			allProjects[i] = projectArray.getJSONObject(i);
 		}
 		mAllProjects = allProjects;
 	}
 }
