 /*
  * Copyright (C) 2013 bwgz.org
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3 as 
  * published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.bwgz.quotation.activity;
 
 import org.bwgz.quotation.R;
 import org.bwgz.quotation.content.provider.QuotationContract.Quotation;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.content.Loader.OnLoadCompleteListener;
 import android.util.Log;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 public class QuotationActivity extends SherlockFragmentActivity implements OnLoadCompleteListener<Cursor> {
 	static public final String TAG = QuotationActivity.class.getSimpleName();
 	
 	static private final String INSTANCE_STATE_URI	= "quotation.content.uri";
 	
 	private QuotationFragment fragment;
 	private Uri uri = null;
 	
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		Log.d(TAG, String.format("onCreate - savedInstanceState: %s", savedInstanceState));
 
 		if (savedInstanceState != null) {
 			String string = savedInstanceState.getString(INSTANCE_STATE_URI);
 			if (string != null) {
 				uri = Uri.parse(string);
 			}
 		}
 
 		FragmentManager fm = getSupportFragmentManager();
 		
         if (fm.findFragmentById(android.R.id.content) == null) {
         	fragment = new QuotationFragment();
             fm.beginTransaction().add(android.R.id.content, fragment).commit();
         }
         else {
         	fragment = (QuotationFragment) fm.findFragmentById(android.R.id.content);
         }
 
 		setContentView(R.layout.quote_activity);
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
 		Log.d(TAG, String.format("onResume - uri: %s", uri));
 		
 		// If there's an intent and it holds a uri then use it
 		Intent intent = getIntent();
 		Log.d(TAG, String.format("onResume - intent: %s", intent));
 		if (intent != null && intent.getData() != null) {
 			uri = intent.getData();
 		}
 	
 		Log.d(TAG, String.format("onResume - uri: %s", uri));
 		
 		if (uri == null) {
 			loadRandomQuote();
 		}
 		else {
 			fragment.setUri(uri);
 		}
     }
 
     @Override
 	protected void onSaveInstanceState(final Bundle outState) {
     	super.onSaveInstanceState(outState);
 		Log.d(TAG, String.format("onSaveInstanceState - outState: %s", outState));
 		
 		if (uri != null) {
     		outState.putString(INSTANCE_STATE_URI, uri.toString());
     	}
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         
         menu.add(Menu.NONE, R.id.actionbar_new, 0, R.string.actionbar_new)
             .setIcon(R.drawable.navigation_refresh)
             .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 
         menu.add(Menu.NONE, R.id.actionbar_share, 0, R.string.actionbar_share)
 	        .setIcon(R.drawable.social_share)
 	        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.main_menu, menu);
 		return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	boolean result = false;
     	
     	switch (item.getItemId()) {
     	case R.id.actionbar_new:
 			{
 	    	//Intent intent = new Intent(this, QuotationActivity.class);
     	    //startActivity(intent);
 			loadRandomQuote();
 			result = true;
 			break;
 			}
     	case R.id.actionbar_share:
     		{
     		Intent intent = new Intent(Intent.ACTION_SEND);
     		intent.setType("text/plain");
     		
             StringBuilder buffer = new StringBuilder();
             buffer.append(((TextView)findViewById(R.id.quote)).getText());
             if (((TextView)findViewById(R.id.quote)).getText().length() != 0) {
             	buffer.append(" ...");
             	buffer.append(((TextView)findViewById(R.id.author)).getText());
             }
 
     		intent.putExtra(Intent.EXTRA_TEXT, buffer.toString());
     		startActivity(Intent.createChooser(intent, "Dialog title text"));
     		result = true;
     		break;
     		}
     	case R.id.settings:
     		{
 	        Intent intent = new Intent().setClass(this, SettingsActivity.class);
     	    startActivity(intent);
     		break;
     		}
     	}
     	
         return result;
     }
     
     private void loadRandomQuote() {
         CursorLoader loader = new CursorLoader(
                 this,		
                 Quotation.withAppendedId("/random"),        
                 new String[] { Quotation.QUOTATION },
                 null,      
                 null,      
                 null       
         		);
         
         loader.registerListener(0, this);
         loader.startLoading();
     }
 
 	@Override
 	public void onLoadComplete(Loader<Cursor> loader, Cursor cursor) {
 		Log.d(TAG, String.format("onLoadComplete - loader: %s  cursor: %s", loader, cursor));
 		
 		if (cursor.moveToFirst()) {
 			uri = Quotation.withAppendedId(cursor.getString(cursor.getColumnIndex(Quotation._ID)));
 			Log.d(TAG, String.format("onLoadComplete - uri: %s", uri));
 			fragment.setUri(uri);
 		}
 		cursor.close();
 		
 	}
 }
