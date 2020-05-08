 package net.fhtagn.zoobeditor.browser;
 
 import java.io.InputStream;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import net.fhtagn.zoobeditor.Common;
 import net.fhtagn.zoobeditor.EditorConstants;
 import net.fhtagn.zoobeditor.R;
 import net.fhtagn.zoobeditor.Series;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.app.AlertDialog.Builder;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.RatingBar;
 import android.widget.TextView;
 
 public class OfflineSerieViewActivity extends Activity {
 	static final String TAG = "OfflineSerieViewActivity";
 	static final int DIALOG_CONFIRM_DELETE = 0;
 	static final int DIALOG_RATE = 1;
 	static final int DIALOG_PROGRESS = 2;
 	private long serieID;
 	
 	private JSONObject serieObj = null;
 	private JSONArray levelsArray = null;
 	
 	private long communityID;
 	
 	@Override
 	protected void onCreate (Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Intent i = getIntent();
 		if (i == null) {
 			Log.e(TAG, "onCreate : null intent");
 			finish();
 		}
 		serieID = i.getLongExtra("serieid", -1);
 		if (serieID == -1) {
 			Log.e(TAG, "onCreate : serieID = -1");
 			finish();
 		}
 		
 		Cursor cursor = managedQuery(ContentUris.withAppendedId(Series.CONTENT_URI, serieID), new String[]{Series.UPDATE_AVAILABLE, Series.COMMUNITY_ID, Series.JSON, Series.RATING, Series.MY_RATING, Series.NAME, Series.PROGRESS}, null, null, null);
 		if (!cursor.moveToFirst()) {
 			Log.e(TAG, "onCreate: !cur.moveToFirst");
 			finish();
 		}
 		
 		if (cursor.isNull(cursor.getColumnIndex(Series.COMMUNITY_ID))) {
 			Log.e(TAG, "serie (local id : " + serieID + ") with NULL community_id");
 			finish();
 		}
 		communityID = cursor.getLong(cursor.getColumnIndex(Series.COMMUNITY_ID));
 		
 		setContentView(R.layout.serieview);
 		
 		try {
 			serieObj = new JSONObject(cursor.getString(cursor.getColumnIndex(Series.JSON)));
 			levelsArray = serieObj.getJSONArray("levels");
 			TextView serieName = (TextView)findViewById(R.id.name);
 			serieName.setText(cursor.getShort(cursor.getColumnIndex(Series.NAME)));
 			/*GridView gridView = (GridView)findViewById(android.R.id.list);
 			gridView.setAdapter(new LevelsAdapter(this, serieObj.getJSONArray("levels")));*/
 			SeriePreviewGrid previewGrid = (SeriePreviewGrid)findViewById(R.id.seriepreview);
 	    previewGrid.setSerie(serieObj);
 	    
 	    
 	    //BEGIN rating
 	    RatingBar myRating = (RatingBar)findViewById(R.id.my_rating);
 	    myRating.setOnTouchListener(new OnTouchListener() {
 				@Override
 	      public boolean onTouch(View view, MotionEvent event) {
 					if (event.getAction() == MotionEvent.ACTION_UP) {
 						showDialog(DIALOG_RATE);
 					}
 	        return true;
 	      }
 	    });
 	    refreshRating();
 	    //END rating
 	    
 	    
 	    Button updateBtn = (Button)findViewById(R.id.btn_update);
 	    boolean updateAvailable = cursor.getInt(cursor.getColumnIndex(Series.UPDATE_AVAILABLE)) == 1;
 			if (!updateAvailable) {
 				updateBtn.setVisibility(View.GONE);
 			} else {
 				updateBtn.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick (View view) {
 						downloadUpdateAndPlay();
 					}
 				});
 			}
 			
 			
 			Button playBtn = (Button)findViewById(R.id.btn_play);
 			playBtn.setText(R.string.btn_play_serie);
 			playBtn.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View view) {
 					if (serieObj == null) {
 						Log.e(TAG, "serieObj = null");
 						return;
 					}
 					Intent i = Common.playSerie(serieID);
 					startActivity(i);
 				}
 			});
 		} catch (JSONException e) {
 			e.printStackTrace();
 			finish();
 		}
 	}
 	
 	private void downloadUpdateAndPlay () {
 		showDialog(DIALOG_PROGRESS);
 		(new Thread() {
 			@Override
 			public void run() {
 				String result = Common.urlQuery(new DefaultHttpClient(), EditorConstants.getDetailsUrl(serieID));
 				if (result != null) {
 					try {
 						JSONObject serieUpdated = new JSONObject(result);
 						ContentValues values = new ContentValues();
 						values.put(Series.JSON, serieUpdated.toString());
						values.put(Series.UPDATE_AVAILABLE, false);
 						getContentResolver().update(ContentUris.withAppendedId(Series.CONTENT_URI, serieID), values, null, null);
 						dismissProgressDialog();
 						Intent i = Common.playSerie(serieID);
 						startActivity(i);
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 				} else {
 					Log.e(TAG, "Unable to fetch update, result = null");
 				}
 				dismissProgressDialog();
 			}
 		}).start();
 	}
 	
 	private void dismissProgressDialog () {
 		runOnUiThread(new Runnable() {
 			public void run () {
 				dismissDialog(DIALOG_PROGRESS);
 			}
 		});
 	}
 	
 	
 	private void refreshRating () {
 		RatingBar communityRating = (RatingBar)findViewById(R.id.rating);
     RatingBar myRating = (RatingBar)findViewById(R.id.my_rating);
 		
 		Cursor cursor = managedQuery(ContentUris.withAppendedId(Series.CONTENT_URI, serieID), new String[]{Series.RATING, Series.MY_RATING}, null, null, null);
 		if (!cursor.moveToFirst()) {
 			Log.e(TAG, "refreshRating: !cur.moveToFirst");
 			return;
 		}
 		
     if (cursor.isNull(cursor.getColumnIndex(Series.RATING)))
     	communityRating.setVisibility(View.INVISIBLE);
     else
     	communityRating.setRating(cursor.getFloat(cursor.getColumnIndex(Series.RATING)));
     
     if (serieID == 1) { //special case for original serie, we cannot rate it
     	TextView sep = (TextView)findViewById(R.id.my_rating_separator);
     	sep.setVisibility(View.GONE);
     	myRating.setVisibility(View.GONE);
     } else {
 	    if (cursor.isNull(cursor.getColumnIndex(Series.MY_RATING)))
 	    	myRating.setRating(0);
 	    else
 	    	myRating.setRating(cursor.getFloat(cursor.getColumnIndex(Series.MY_RATING)));
     }
 	}
 	
 	@Override
 	protected Dialog onCreateDialog (int id) {
 		switch (id) {
 			case DIALOG_CONFIRM_DELETE: {
 				return Common.createConfirmDeleteDialog(this, R.string.confirm_delete_serie, new DialogInterface.OnClickListener() {
 	  			public void onClick (DialogInterface dialog, int id) {
 	  				Uri deleteUri = ContentUris.withAppendedId(Series.CONTENT_URI, serieID);
 	  				getContentResolver().delete(deleteUri, null, null);
 	  				OfflineSerieViewActivity.this.finish();
 	  			}
 				});
 			}
 			case DIALOG_RATE: {
 				return Common.createRateDialog(this, communityID);
 			}
 			case DIALOG_PROGRESS: {
 				ProgressDialog progressDialog = new ProgressDialog(this);
         progressDialog.setIcon(android.R.drawable.ic_dialog_info);
         progressDialog.setTitle(getString(R.string.progress_title));
         progressDialog.setIndeterminate(true);
         return progressDialog;
 			}
 			default:
 				return null;
 		}
 	}
 	
 	@Override
 	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 			case EditorConstants.REQUEST_RATE:
 				refreshRating();
 				break;
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu (Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.offlineserieview_menu, menu);
 		Common.createCommonOptionsMenu(this, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected (MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.delete:
 				showDialog(DIALOG_CONFIRM_DELETE);
 				return true;
 		}
 		return Common.commonOnOptionsItemSelected(this, item);
 	}
 }
