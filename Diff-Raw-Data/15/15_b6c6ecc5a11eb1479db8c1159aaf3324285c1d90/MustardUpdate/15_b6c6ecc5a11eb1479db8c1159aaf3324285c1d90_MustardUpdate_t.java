 /**
  * MUSTARD: Android's Client for StatusNet
  * 
  * Copyright (C) 2009-2010 macno.org, Michele Azzolari
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
  * for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 
 package org.mustard.android.activity;
 
 import java.io.File;
 import java.math.BigDecimal;
 import java.util.Random;
 
 import org.mustard.android.MustardApplication;
 import org.mustard.android.MustardDbAdapter;
 import org.mustard.android.Preferences;
 import org.mustard.android.R;
 import org.mustard.android.provider.StatusNet;
 import org.mustard.util.AuthException;
 import org.mustard.util.LocationUtil;
 import org.mustard.util.StringUtil;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore.Images.ImageColumns;
 import android.text.Editable;
 import android.text.Html;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class MustardUpdate extends Activity {
 
 	private final String TAG = "MustardUpdate";
 
 	private static final int MSG_REFRESH = 5;
 	private final int OK=0;
 	private final int KO=1;
 //	private final int ACCOUNT_ADD = 2;
 	private final int UNAUTH=3;	
 
 	private final int CHOOSE_FILE_ID=0;
 
 	private MustardDbAdapter mDbHelper;
 
 	private EditText mBodyText;
 	private StatusNet mStatusNet;
 	private TextView mTextViewFileName;
 	private TextView mCharCounter;
 	private CheckBox mCheckBoxLocation;
 	private File mFilename;
 	private SharedPreferences mSharedPreferences;
 	private NotificationManager mNotificationManager;
 	private Intent mCurrentIntent ;
 
 	private long mInReplyTo=-1;
 	private int mStatusType=-1;
 
 	private boolean mRefreshOnPost = false;
 	private String mErrorUpdateDescription = "";
 	protected SharedPreferences preferences = null;
 	private MustardApplication _ma ;
 
 	private long mSenderAccountId = -1;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 
 		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		preferences =  PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		mDbHelper = new MustardDbAdapter(this);
 		mDbHelper.open();
 
 		mRefreshOnPost = preferences.getBoolean(Preferences.REFRESH_ON_POST_ENABLES_KEY, false);
 		boolean mMultiAccount = preferences.getBoolean(Preferences.MULTI_ACCOUNT_POST_ENABLES_KEY, false);
 		_ma = (MustardApplication) getApplication();
 		mStatusNet = _ma.checkAccount(mDbHelper);
 
 		if (mStatusNet == null) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "No account found. Starting Login activity");
 			showLogin();
 			return;
 		}
 
 		Button uploadButton = null;
 		Cursor c = null;
 		String version="";
 		int textlimit = 0;
 		try {
 			c = mDbHelper.fetchAccount(mStatusNet.getUserId());
 			if (c.moveToNext()) {
 				version = c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_VERSION));
 			}
 			textlimit = c.getInt(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_TEXTLIMIT));
 		} catch (Exception e) {
 			Log.e(TAG,"DbError: " + e.toString());
 			e.printStackTrace();
 		} finally {
 			if (c!=null)
 				c.close();
 		}
 		if (version==null || "".equals(version)) {
 			Log.e(TAG, "Version is null. Forced to 0");
 			version = "0";
 		}
 
 		if(StringUtil.compareVersion(version,"0.9")>0) {
 			setContentView(R.layout.dent_add_media);
 			mTextViewFileName = (TextView) findViewById(R.id.filename);
 			uploadButton = (Button) findViewById(R.id.selectfilename);
 			uploadButton.setOnClickListener(new View.OnClickListener() {
 
 				public void onClick(View view) {
 					showFileChooser();
 				}
 			});
 
 			// GEO LOCATION
 			mCheckBoxLocation = (CheckBox)findViewById(R.id.enable_location);
 			boolean geoEnabled = preferences.getBoolean(Preferences.GEOLOCATION_ENABLES_KEY, true);
 			if(geoEnabled) {
 				mSharedPreferences = getSharedPreferences(MustardApplication.APPLICATION_NAME, 0);
 				if(mSharedPreferences.getBoolean(Preferences.GEOLOCATION_ENABLE, false))
 					mCheckBoxLocation.setChecked(true);
 				mCheckBoxLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 
 					public void onCheckedChanged(CompoundButton buttonView,
 							boolean isChecked) {
 						mSharedPreferences.edit().putBoolean(Preferences.GEOLOCATION_ENABLE, isChecked).commit();
 					}
 
 				});
 			} else {
 				mCheckBoxLocation.setChecked(false);
 				mCheckBoxLocation.setVisibility(View.GONE);
 			}
 		} else {
 			Log.e(TAG,"Version < 0.9: " + version);
 			setContentView(R.layout.dent_add);
 		}
 		setTextLimit(textlimit);
 
 		mBodyText = (EditText) findViewById(R.id.body);
 		mBodyText.addTextChangedListener(mTextWatcher);
 
 		mCharCounter= (TextView) findViewById(R.id.char_counter);
 		Button confirmButton = (Button) findViewById(R.id.confirm);
 
 		Intent intent = getIntent();
 		Bundle extras = intent.getExtras();        
 		Long mRowId = extras != null ? extras.getLong(MustardDbAdapter.KEY_ROWID) : null;
 
 		Uri mImageUri = null;
 
 		if (Intent.ACTION_SEND.equals(intent.getAction()) && extras != null) {
 			// Force mRefreshOnPost to false, because I have no parent
 			mRefreshOnPost=false;
 			if (extras.containsKey(Intent.EXTRA_STREAM)) {
 				if(mTextViewFileName==null) {
 					new AlertDialog.Builder(MustardUpdate.this)
 					.setTitle(R.string.error)
 					.setMessage(R.string.error_no_attachment_supported)
 					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface xdialog, int id) {
 							finish();
 						}
 					}).show();
 					return;
 				}
 				mImageUri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
 				if (mImageUri != null) {
 					Cursor cursor = getContentResolver().query(mImageUri, null, null, null,
 							null);
 
 					if (cursor.moveToFirst()) {
 						mFilename = new File( cursor.getString(cursor.getColumnIndexOrThrow(ImageColumns.DATA)));
 						mTextViewFileName.setText(mFilename.getName());
 					}
 					cursor.close();
 				}
 			}
 
 			String origMessage = "";
 			if(extras.containsKey(Intent.EXTRA_SUBJECT)) {
 				String s = extras.getString(Intent.EXTRA_SUBJECT);
 				if (s != null)
 					origMessage = s;
 			}			
 			if(extras.containsKey(Intent.EXTRA_TEXT)) {
 				CharSequence cs = extras.getCharSequence(Intent.EXTRA_TEXT);
 				if (cs != null) {
 					if(!origMessage.equals(""))
 						origMessage += " - ";
 					origMessage += cs;
 				}
 			}
 			mBodyText.setText(origMessage);
 		}
 
 
 		if (mRowId != null) {
 
 			mStatusType = extras.getInt(Preferences.STATUS_TYPE);
 			Cursor dent = mDbHelper.fetchStatus(mRowId);
 			if (dent != null && dent.getCount()>0) {
 				try {
 					mInReplyTo = dent.getLong(dent.getColumnIndexOrThrow(MustardDbAdapter.KEY_STATUS_ID));
 					String text = "";
 					switch(mStatusType) {
 					case Preferences.STATUS_TYPE_REDENT:
 						text ="\u267B @"+dent.getString(dent.getColumnIndexOrThrow(MustardDbAdapter.KEY_SCREEN_NAME));
 						text += " " + dent.getString(dent.getColumnIndexOrThrow(MustardDbAdapter.KEY_STATUS));
 						mBodyText.setText(Html.fromHtml(text).toString());
 						break;
 					case Preferences.STATUS_TYPE_REPLY:
 						text = dent.getString(dent.getColumnIndexOrThrow(MustardDbAdapter.KEY_SCREEN_NAME));
 						mBodyText.setText("@"+text+" ");
 						break;
 					}
 				} catch (Exception e) {
 					if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 					Toast.makeText(this, getString(R.string.error_generic_detail,e.getMessage() == null ? e.toString() : e.getMessage() ), Toast.LENGTH_LONG);
 				} finally {
 					dent.close();
 				}
 			} else {
 				if (MustardApplication.DEBUG) Log.e(TAG,"No row found");
 				Toast.makeText(this, getString(R.string.error_generic_detail,getString(R.string.error_no_dents_from)), Toast.LENGTH_LONG);
 			}
 		}
 
 
 		if(extras != null) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "Extra is not null");
 			String lText = extras.getString(Preferences.STATUS_TEXT)  ;
 			if( lText != null) {
 				mNotificationManager.cancel(0);
 				if (MustardApplication.DEBUG) Log.i(TAG, "Back from a failure");
 				mBodyText.setText(lText);
 				mInReplyTo = extras.getLong(Preferences.STATUS_IN_REPLY_TO);
 				mStatusType = extras.getInt(Preferences.STATUS_TYPE);
 
 				String fname = extras.getString(Preferences.STATUS_FILE);
 				if (fname != null) {
 					mFilename = new File(fname);
 					mTextViewFileName.setText(mFilename.getName());
 				}
 				mSenderAccountId =  extras.getLong(Preferences.STATUS_ACCOUNT_ROWID);
 			} else {
 				if (MustardApplication.DEBUG) Log.i(TAG, "Text is null");
 			}
 		} else {
 			if (MustardApplication.DEBUG) Log.i(TAG, "Extra is null");
 		}
 
 		confirmButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				updateStatus();
 			}
 		});
 		
 		if(mMultiAccount) {
 			Spinner account_spinner = (Spinner) findViewById(R.id.account_spinner);
 			if (mInReplyTo<0) {
 				Cursor cur = mDbHelper.fetchAllAccountsDefaultFirst();
 				startManagingCursor(cur);
 				if(cur.getCount()>1) {
 					int savedPosition=0;
 					long[] accountIDs = new long[cur.getCount()];
 					int[] tmp_textlimits = new int[cur.getCount()];
 					int cc=0;
 					while(cur.moveToNext()) {
 						long rowId=cur.getLong(cur.getColumnIndex(MustardDbAdapter.KEY_ROWID));
 						int limit =cur.getInt(cur.getColumnIndex(MustardDbAdapter.KEY_TEXTLIMIT));
 						accountIDs[cc]=rowId;
 						tmp_textlimits[cc]=limit;
 						if(rowId==mSenderAccountId)
 							savedPosition=cc;
 						cc++;
 					}
 					cur.moveToPosition(-1);
 					final long[] rowIds = accountIDs; 
 					final int[] textlimits = tmp_textlimits;
 					SimpleCursorAdapter adapter2 = new SimpleCursorAdapter(this,
 							android.R.layout.simple_spinner_item, 
 							cur,
 							new String[] {MustardDbAdapter.KEY_USER}, 
 							new int[] {android.R.id.text1});
 
 					adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 					account_spinner.setAdapter(adapter2);
 					account_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 						public void onItemSelected(AdapterView<?> parent,
 								View view, int pos, long id) {
 							mSenderAccountId = rowIds[pos];
 							setTextLimit(textlimits[pos]);
 //							Log.d(TAG,"Selected " + mSenderAccountId);
 						}
 
 						public void onNothingSelected(AdapterView<?> arg0) {
 
 						}
 
 					});
 					if (mSenderAccountId > 0) {
 						account_spinner.setSelection(savedPosition);
 					}
 				} else {
 					hideSpinner();
 				}
 			} else {
 				hideSpinner();
 			}
 		} else {
 			hideSpinner();
 		}
 	}
 	
 	private void setTextLimit(int textlimit) {
 		if (textlimit <= 0) {
 			findViewById(R.id.char_separator).setVisibility(View.GONE);
 			findViewById(R.id.char_limit).setVisibility(View.GONE);
 		} else {
 			findViewById(R.id.char_separator).setVisibility(View.VISIBLE);
 			findViewById(R.id.char_limit).setVisibility(View.VISIBLE);
 			((TextView)findViewById(R.id.char_limit)).setText(""+textlimit);
 		}
 	}
 	
 	private void hideSpinner() {
 		findViewById(R.id.account_spinner).setVisibility(View.GONE);
 		findViewById(R.id.lbl_account_spinner).setVisibility(View.GONE);
 	}
 	
 	private void onSendComplete(int result) {
 		Log.d(TAG,"onSendComplete: " + result);
 		
 			if (result != OK) {
 				
 				PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0, mCurrentIntent,PendingIntent.FLAG_ONE_SHOT);
 				notify(mPendingIntent, 0, R.drawable.icon,
 						getString(R.string.error), 
 						getString(R.string.status_update_ko_title), 
 						getString(R.string.status_update_ko,mErrorUpdateDescription));
 			} else {
 				if (mHandler != null && mRefreshOnPost) {
 					mHandler.sendEmptyMessage(MSG_REFRESH);
 				}
 			}
 //		}
 	}
 
 	private void updateStatus() {
 		String status = mBodyText.getText().toString();
 		if (status == null || "".equals(status)) 
 			return;
 
 		
 //		if(!mRefreshOnPost) {
 			mCurrentIntent = new Intent(this,MustardUpdate.class);
 			mCurrentIntent.putExtra(Preferences.STATUS_IN_REPLY_TO, mInReplyTo);
 			if(mStatusType>0)
 				mCurrentIntent.putExtra(Preferences.STATUS_TYPE, mStatusType);
 			mCurrentIntent.putExtra(Preferences.STATUS_ACCOUNT_ROWID, mSenderAccountId);
 			mCurrentIntent.putExtra(Preferences.STATUS_TEXT,status);
 			if (mFilename != null)
 				mCurrentIntent.putExtra(Preferences.STATUS_FILE, mFilename.getAbsolutePath());
 
 		new StatusUpdater().execute("");
 		finish();
 	}
 
 	private void notify(PendingIntent intent, int notificationId,
 			int notifyIconId, String tickerText, String title, String text) {
 		Notification notification = new Notification(notifyIconId, tickerText,
 				System.currentTimeMillis());
 
 		notification.setLatestEventInfo(this, title, text, intent);
 
 		notification.flags = Notification.FLAG_AUTO_CANCEL
 		| Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_SHOW_LIGHTS;
 
 		notification.ledARGB = 0xFF84E4FA;
 		notification.ledOnMS = 5000;
 		notification.ledOffMS = 5000;
 
 		notification.defaults = Notification.DEFAULT_LIGHTS;
 
 		mNotificationManager.notify(notificationId, notification);
 	}
 
 
 	private void showFileChooser() {
 		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
 		intent.setType("image/*");
 		startActivityForResult(intent, CHOOSE_FILE_ID);
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode,
 			Intent intent) {
 		super.onActivityResult(requestCode, resultCode, intent);
 		if (requestCode == CHOOSE_FILE_ID) {
 			if (resultCode == RESULT_OK) {
 				Uri uri = intent.getData();
 				if (uri != null) {
 					Cursor cursor = getContentResolver().query(uri, null, null, null,null);
 					if (cursor.moveToFirst()) {
 						mFilename = new File( cursor.getString(cursor
 								.getColumnIndexOrThrow(ImageColumns.DATA)));
 
 						mTextViewFileName.setText(mFilename.getName());
 					}
 					cursor.close();
 					mTextViewFileName.setText(mFilename.getName());
 				}
 			}
 		}
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 		Log.d(TAG,"onDestroy");
 		if(mDbHelper != null)
 			mDbHelper.close();
 	}
 
 	private void showLogin() {
 //		Intent i = new Intent(this, Login.class);
 //		startActivityForResult(i, ACCOUNT_ADD);
 		Login.actionHandleLogin(this);
 	}
 
 	private void updateCounter() {
 		mCharCounter.setText(""+mBodyText.getText().length());
 	}
 
 	private TextWatcher mTextWatcher = new TextWatcher() {
 
 		public void afterTextChanged(Editable e) {
 
 			updateCounter();
 		}
 
 		public void beforeTextChanged(CharSequence s, int start, int count,
 				int after) {
 		}
 
 		public void onTextChanged(CharSequence s, int start, int before,
 				int count) {
 		}
 
 	};
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		ProgressDialog dialog;
 
 		dialog = new ProgressDialog(this);
 		dialog.setIndeterminate(true);
 		dialog.setCancelable(true);
 		dialog.setMessage(getString(R.string.please_wait_updating_dent));
 
 		return dialog;
 	}
 
 	public class StatusUpdater extends AsyncTask<String, Integer, Integer> {
 
 		private final String TAG = getClass().getCanonicalName();
 
 		private boolean isErrorLocation=false;
 		private boolean noLocationProvider=false;
 
 		@Override
 		protected Integer doInBackground(String... s) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 
 			String lat = null;
 			String lon = null;
 
 			if (mCheckBoxLocation!=null && mCheckBoxLocation.isChecked()) {
 
 				String lsGeoFuzzy = preferences.getString(Preferences.GEOLOCATION_FUZZY_KEY, getString(R.string.pref_geo_fuzzy_default));
 				int liGeoFuzzy = Integer.parseInt(lsGeoFuzzy);
 				LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 				Location location = LocationUtil.getMostRecentLastKnownLocation(locationManager);
 
 				if (location == null) {
 					if (MustardApplication.DEBUG) Log.d(this.getClass().getCanonicalName(), "location is null");
 					isErrorLocation=true;
 				}
 
 				boolean resetLocation = false;
 				if (liGeoFuzzy==0) {
 					resetLocation=true;
 				} else if (MustardApplication.sLocation != null) {
 					double distance = LocationUtil.getDistance(location, MustardApplication.sLocation);
 					if (distance > liGeoFuzzy*1000) {
 						resetLocation=true;
 					}
 				} else if (MustardApplication.sLocation == null) {
 					resetLocation=true;
 				}
 
 				if (!isErrorLocation) {
 					if(resetLocation) {
 						try {
 
 
 							double d_lat = location.getLatitude();
 							double d_lon = location.getLongitude();
 
 							/*
 							Fuzzy position
 							0 = 0Km
 							0.05 = 5km
 							0.1 = 10Km
 							0.2 = 20Km
 							0.5 = 50Km
 							 */
 
 
 							if (liGeoFuzzy > 0){
 								liGeoFuzzy++;
 								Random generator = new Random();
 								Random generator2 = new Random();
 								double randomLat = generator.nextInt(liGeoFuzzy);
 								double randomLon = generator2.nextInt(liGeoFuzzy);
 
 								if (generator.nextInt(2)==0) {
 									d_lon = d_lon + (randomLon/105);
 								} else {
 									d_lon = d_lon - (randomLon/105);
 								}
 
 								if(generator2.nextInt(2)==0) {
 									d_lat = d_lat + (randomLat/105);
 								} else {
 									d_lat = d_lat - (randomLat/105);
 								}
 							}
 
 							lat = new BigDecimal(d_lat).setScale(4,BigDecimal.ROUND_FLOOR).toPlainString();
 							lon = new BigDecimal(d_lon).setScale(4,BigDecimal.ROUND_FLOOR).toPlainString();
 
 							MustardApplication.sLocation=location;
 
 						} catch (Exception e) {
 							if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 							isErrorLocation=true;
 						}
 					} else {
 						lat = new BigDecimal(MustardApplication.sLocation.getLatitude()).setScale(4,BigDecimal.ROUND_FLOOR).toPlainString();
 						lon = new BigDecimal(MustardApplication.sLocation.getLongitude()).setScale(4,BigDecimal.ROUND_FLOOR).toPlainString();
 					}
 				}
 			}
 			try {
 				
 				if(mSenderAccountId > 0 && mStatusNet.getUsernameId() != mSenderAccountId) {
 					mStatusNet = _ma.loadAccount(mDbHelper, mSenderAccountId);
 				}
 				mStatusNet.update(mBodyText.getText().toString(), String.valueOf(mInReplyTo), lon, lat, mFilename);
 				Log.d(TAG, "Ended Status Update");
 			} catch (AuthException e) {
 				e.printStackTrace();
 				mErrorUpdateDescription = e.getMessage() == null ? e.toString() : e.getMessage();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return UNAUTH;
 			} catch(Exception e) {
 				e.printStackTrace();
 				mErrorUpdateDescription = e.getMessage() == null ? e.toString() : e.getMessage();
 //				e.printStackTrace();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return KO;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end ");
 			}
 			return OK;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 		}
 
 		protected void onPostExecute(Integer result) {

 			if (noLocationProvider)
 				Toast.makeText(getApplicationContext(),
 						getString(R.string.no_locaton_provider),
 						Toast.LENGTH_LONG).show();
 
 			if (isErrorLocation)
 				Toast.makeText(getApplicationContext(),
 						getString(R.string.error_detecting_locaton),
 						Toast.LENGTH_LONG).show();
 
 			int ret=0;
 			try {
 				if (result==OK) {
 					if(!mRefreshOnPost) {
 						Toast.makeText(
 								getApplicationContext(),
 								getString(R.string.status_update_ok),
 								Toast.LENGTH_SHORT).show();
 					}
 					ret=OK;
 				} else {
 					ret=result;
 				}
 			} catch(IllegalArgumentException e) {
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				ret=KO;
 			} finally {
 				onSendComplete(ret);
 			}
 		}
 	}
 	
 	private static Handler mHandler;
 	
 	public static void actionCompose(Context context,Handler handler) {
 		mHandler=handler;
 		Intent i = new Intent(context, MustardUpdate.class);
 		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		context.startActivity(i);
 		
 	}
 
 	public static void actionReply(Context context,Handler handler,long rowid) {
		mHandler=handler;
 		Intent i = new Intent(context, MustardUpdate.class);
 		i.putExtra(MustardDbAdapter.KEY_ROWID, rowid);
 		i.putExtra(Preferences.STATUS_TYPE, Preferences.STATUS_TYPE_REPLY);
 		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		context.startActivity(i);
 	}
 	
 	public static void actionForward(Context context,Handler handler,long rowid) {
		mHandler=handler;
 		Intent i = new Intent(context, MustardUpdate.class);
 		i.putExtra(MustardDbAdapter.KEY_ROWID, rowid);
 		i.putExtra(Preferences.STATUS_TYPE, Preferences.STATUS_TYPE_REDENT);
 		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		context.startActivity(i);
 	}
 }
