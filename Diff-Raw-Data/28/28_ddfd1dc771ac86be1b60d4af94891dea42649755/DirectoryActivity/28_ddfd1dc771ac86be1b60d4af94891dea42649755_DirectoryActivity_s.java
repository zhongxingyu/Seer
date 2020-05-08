 package com.dev.campus.directory;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.dev.campus.R;
 import com.dev.campus.SettingsActivity;
 import com.dev.campus.event.Category;
 import com.dev.campus.util.FilterDialog;
 import com.unboundid.ldap.sdk.LDAPException;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class DirectoryActivity extends ListActivity {
 
 	private ActionBar mActionBar;
 	private FilterDialog mFilterDialog;
 	private ListView listview;
 	private DirectoryAdapter adapter;
 	private Resources mResources;
 	private Activity mContext;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		mContext = this;
 		mFilterDialog = new FilterDialog(this);
 		mActionBar = getActionBar();
 		mActionBar.setDisplayHomeAsUpEnabled(true);
 		mResources = getResources();
 		ArrayList<Contact> matchingContacts = new ArrayList<Contact>();
 
 		listview = getListView();
 		View header = (View) getLayoutInflater().inflate(R.layout.directory_list_header, listview, false);
 		listview.addHeaderView(header, null, true);
 		adapter = new DirectoryAdapter(this, matchingContacts);
 		listview.setAdapter(adapter);
 
 		final ImageButton searchButton = (ImageButton) findViewById(R.id.buttonSearchDirectory);
 		searchButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				new SearchResultTask().execute();
 			}
 		});
 	}
 
 	public void reloadContacts(List<Contact> matchingContacts){
 		adapter.clear();
 		adapter.addAll(matchingContacts);
 		adapter.notifyDataSetChanged();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.with_actionbar, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			startActivity(new Intent(DirectoryActivity.this, SettingsActivity.class));
 			return true;
 		case R.id.menu_filters:
 			mFilterDialog.showDialog();
 			return true;
 		case android.R.id.home:
 			finish();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 
 	private class SearchResultTask extends AsyncTask<Category, Void, List<Contact>> {
 
 		private ProgressDialog progressDialog = new ProgressDialog(DirectoryActivity.this);
 
 		@Override
 		protected void onPreExecute() {
 			progressDialog.setTitle(mResources.getString(R.string.contacts_loading));
 			progressDialog.setMessage(mResources.getString(R.string.please_wait));
 			progressDialog.setIndeterminate(true);
 			progressDialog.setCancelable(false);
 			progressDialog.show();
 		}
 
 		@Override
 		protected List<Contact> doInBackground(Category... params) {
 			final TextView firstName = (TextView) findViewById(R.id.editTextFirstName);
 			final TextView lastName = (TextView) findViewById(R.id.editTextLastName);
 
 			int searchMinChar = 3;
			int choice = 1; // temporary put : UB1 = 1 , Labri = 2
 			ArrayList<Contact> matchingContacts = new ArrayList<Contact>();
 			if (firstName.getText().toString().length() >= searchMinChar || lastName.getText().toString().length() >= searchMinChar) {
 				ArrayList<Contact> contacts = new ArrayList<Contact>();
 
 				if (choice == 1) {
 					try {
 						if (Directory_UB1.authenticate() == true) {
 							contacts = Directory_UB1.search(lastName.getText().toString(), firstName.getText().toString());
 						}
 					} catch (LDAPException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				if (choice == 2) {
 					try {
 						contacts = new Directory().labriDirectoryParser();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				matchingContacts = new Directory().directoryFilter(contacts, firstName.getText().toString(), lastName.getText().toString());	
 			}
 			else {
 				//Toast.makeText(mContext, searchMinChar+" charactères minimum!", Toast.LENGTH_SHORT).show();
 			}
 
 			return matchingContacts;
 		}
 
 		@Override
 		protected void onPostExecute(List<Contact> contacts) {
 			reloadContacts(contacts);
 			progressDialog.dismiss();
 		}
 
 		@Override
 		protected void onCancelled() {
 			progressDialog.dismiss();
 			super.onCancelled();
 		}
 	}
 
 }
