 package org.droidstack;
 
 import org.droidstack.stackapi.StackAPI;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class Sites extends Activity {
 	
 	private SitesDatabase mSitesDatabase;
 	private Cursor mSites;
 	private ListView mListView;
 	private SimpleCursorAdapter mAdapter;
 	private View mAddSiteDialogView;
 	private Context mContext;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(R.layout.sites);
         mContext = (Context) this;
         mSitesDatabase = new SitesDatabase(mContext);
         mSites = mSitesDatabase.getSites();
         startManagingCursor(mSites);
         
         mListView = (ListView) findViewById(R.id.SitesListView);
         mListView.setEmptyView(findViewById(R.id.NoSitesText));
         mAdapter = new SimpleCursorAdapter(
         	this,
         	android.R.layout.simple_list_item_1,
         	mSites,
         	new String[] { SitesDatabase.KEY_NAME },
         	new int[] { android.R.id.text1 }
         );
         mListView.setAdapter(mAdapter);
         mListView.setOnItemClickListener(onSiteClicked);
         registerForContextMenu(mListView);
     }
     
     @Override
 	protected void onDestroy() {
 		super.onDestroy();
 		mSites.close();
 		mSitesDatabase.dispose();
 	}
 
 	private OnItemClickListener onSiteClicked = new OnItemClickListener() {
 
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			mSites.moveToPosition(position);
 			String domain = mSites.getString(mSites.getColumnIndex(SitesDatabase.KEY_DOMAIN));
 			Intent i = new Intent(mContext, Site.class);
 			i.setAction(Intent.ACTION_VIEW);
 			i.setData(Uri.parse("stack://" + domain + "/"));
 			startActivity(i);
 		}
 	};
     
     @Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
     	if (v.getId() == R.id.SitesListView) {
     		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
     		mSites.moveToPosition(info.position);
     		String domain = mSites.getString(mSites.getColumnIndex(SitesDatabase.KEY_DOMAIN));
     		menu.setHeaderTitle(domain);
     		getMenuInflater().inflate(R.menu.sites_context, menu);
     	}
 	}
     
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		mSites.moveToPosition(info.position);
 		String domain = mSites.getString(mSites.getColumnIndex(SitesDatabase.KEY_DOMAIN));
 		switch(item.getItemId()) {
 		case R.id.menu_set_user:
 			addOrEditSite(info.position);
 			return true;
 		case R.id.menu_remove:
 			mSitesDatabase.removeSite(domain);
 			mSites.requery();
 			mAdapter.notifyDataSetChanged();
 			return true;
 		}
 		return super.onContextItemSelected(item);
 	}
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	getMenuInflater().inflate(R.menu.sites, menu);
     	return true;
     }
 	
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     	case R.id.menu_add_site:
     		addOrEditSite(-1);
     		break;
     	}
     	return false;
     }
     
     private void addOrEditSite(int position) {
     	final int pos = position;
     	AlertDialog.Builder dialog = new AlertDialog.Builder(this);
     	mAddSiteDialogView = getLayoutInflater().inflate(R.layout.add_site, null);
 		EditText siteEdit = (EditText) mAddSiteDialogView.findViewById(R.id.site);
 		EditText userEdit = (EditText) mAddSiteDialogView.findViewById(R.id.user);
 		
     	if (pos != -1) {
     		mSites.moveToPosition(pos);
     		String domain = mSites.getString(mSites.getColumnIndex(SitesDatabase.KEY_DOMAIN));
     		long userID = mSites.getLong(mSites.getColumnIndex(SitesDatabase.KEY_UID));
     		
     		dialog.setTitle(R.string.menu_edit_site);
     		siteEdit.setText(domain);
 			siteEdit.setEnabled(false);
 			siteEdit.clearFocus();
 			userEdit.requestFocus();
 			if (userID != 0) {
 				userEdit.setText(String.valueOf(userID));
 			}
     	}
     	else {
     		dialog.setTitle(R.string.menu_add_site);
     		siteEdit.setText("");
 			siteEdit.setEnabled(true);
     	}
 		dialog.setView(mAddSiteDialogView);
 		dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				String domain = ((EditText)mAddSiteDialogView.findViewById(R.id.site)).getText().toString();
 				long userID = 0;
 				try {
 					userID = Long.parseLong(((EditText)mAddSiteDialogView.findViewById(R.id.user)).getText().toString(), 10);
 				}
 				catch (Exception e) { }
 				if (pos != -1) {
 					mSites.moveToPosition(pos);
 					String name = mSites.getString(mSites.getColumnIndex(SitesDatabase.KEY_NAME));
 					String endpoint = mSites.getString(mSites.getColumnIndex(SitesDatabase.KEY_ENDPOINT));
 					mSitesDatabase.addSite(domain, name, userID, endpoint);
 					mSites.requery();
 					mAdapter.notifyDataSetChanged();
 				}
 				else {
 					ContentValues cv = new ContentValues();
 					cv.put(SitesDatabase.KEY_DOMAIN, domain);
 					cv.put(SitesDatabase.KEY_UID, userID);
 					new AddSiteTask().execute(cv);
 				}
 			}
 		});
 		dialog.setNegativeButton(android.R.string.cancel, null);
 		dialog.show();
     }
     
     private class AddSiteTask extends AsyncTask<ContentValues, Void, ContentValues> {
     	
     	private Exception mException;
     	
     	@Override
 		protected void onPreExecute() {
 			setProgressBarIndeterminateVisibility(true);
 		}
 
 		@Override
 		protected ContentValues doInBackground(ContentValues... params) {
 			ContentValues cv = params[0];
 			String domain = cv.getAsString(SitesDatabase.KEY_DOMAIN);
 			try {
 				String endpoint = StackAPI.getEndpoint(domain);
 				StackAPI api = new StackAPI(endpoint, Const.APIKEY);
				String name = api.getStats().name;
 				
 				cv.put(SitesDatabase.KEY_NAME, name);
 				cv.put(SitesDatabase.KEY_ENDPOINT, endpoint);
 			}
 			catch (Exception e) {
 				mException = e;
 			}
 			return cv;
 		}
 
 		@Override
 		protected void onPostExecute(ContentValues result) {
 			setProgressBarIndeterminateVisibility(false);
 			if (mException != null) {
 				new AlertDialog.Builder(mContext)
 				.setTitle(R.string.title_error)
 				.setMessage(R.string.site_add_error)
 				.setNeutralButton(android.R.string.ok, null)
 				.create().show();
 				Log.e(Const.TAG, "Exception: " + mException.getMessage());
 			}
 			else {
 				String domain = result.getAsString(SitesDatabase.KEY_DOMAIN);
 				String name = result.getAsString(SitesDatabase.KEY_NAME);
 				long userID = result.getAsLong(SitesDatabase.KEY_UID);
 				String endpoint = result.getAsString(SitesDatabase.KEY_ENDPOINT);
 				mSitesDatabase.addSite(domain, name, userID, endpoint);
 				mSites.requery();
 				mAdapter.notifyDataSetChanged();
 			}
 		}
     	
     }
     
 }
