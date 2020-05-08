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
 
 package org.mumod.android.activity;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.net.URLConnection;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.mumod.android.Account;
 import org.mumod.android.MustardApplication;
 import org.mumod.android.MustardDbAdapter;
 import org.mumod.android.Preferences;
 import org.mumod.android.R;
 import org.mumod.android.provider.StatusNet;
 import org.mumod.statusnet.RowStatus;
 import org.mumod.urlshortener.B1tit;
 import org.mumod.urlshortener.Ndgd;
 import org.mumod.urlshortener.Ur1ca;
 import org.mumod.urlshortener.UrlShortener;
 import org.mumod.util.AuthException;
 import org.mumod.util.ImageUtil;
 import org.mumod.util.LocationUtil;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.Typeface;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.Preference;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore.Images.ImageColumns;
 import android.text.Editable;
 import android.text.Html;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MustardUpdate extends Activity {
 
 	private final String TAG = "MustardUpdate";
 	private static String AT_SIGNS_CHARS = "@\uFF20";
 	public static final Pattern AT_SIGNS = Pattern.compile("[" + AT_SIGNS_CHARS + "]");
 	
 	public static final String KEY_ACCOUNT_ID = "_account_id";
 
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
 	private String mLocation=null;
 
 	private boolean mRefreshOnPost = false;
 	private String mErrorUpdateDescription = "";
 	protected static SharedPreferences mPreferences = null;
 	private MustardApplication mMustardApplication ;
 	private static boolean mReplyAll;
 
 	//	private long mSenderAccountId = -1;
 	//	private int mTextLimit = 0;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.dent_add_media);
 
 		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		mPreferences =  PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		mDbHelper = new MustardDbAdapter(this);
 		mDbHelper.open();
 
 		mRefreshOnPost = mPreferences.getBoolean(Preferences.REFRESH_ON_POST_ENABLES_KEY, false);
 		boolean multiAccount = true;
 		mMustardApplication = (MustardApplication) getApplication();
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		mTextViewFileName = (TextView) findViewById(R.id.filename);
 		
 		mBodyText = (EditText) findViewById(R.id.body);
 		Typeface tf = Typeface.createFromAsset(getAssets(),MustardApplication.MUSTARD_FONT_NAME);
 		mBodyText.setTypeface(tf);
 
 		Intent intent = getIntent();
 		Bundle extras = intent.getExtras();        
 
 		Long mAccountId = extras != null ? extras.containsKey(MustardDbAdapter.KEY_ACCOUNT_ID) ? extras.getLong(MustardDbAdapter.KEY_ACCOUNT_ID) : null : null;
 		Long mRowId = extras != null ? extras.containsKey(MustardDbAdapter.KEY_ROWID) ? extras.getLong(MustardDbAdapter.KEY_ROWID) : null : null;
 
 		String text = "";
 		if(mAccountId != null && mRowId == null) {
 			onSetAccount(extras.getLong(KEY_ACCOUNT_ID));
 			//			mStatusNet = mMustardApplication.checkAccount(mDbHelper,false,);
 			//			mMultiAccount=false;
 		} else if (mRowId != null) {
 			mStatusType = extras.getInt(Preferences.STATUS_TYPE);
 			Log.i(TAG,"REPLY/REDENT: " + mStatusType + " of rowid: " + mRowId);
 			Cursor dent = mDbHelper.fetchStatus(mRowId);
 			if (dent != null && dent.getCount()>0) {
 				try {
 					mInReplyTo = dent.getLong(dent.getColumnIndexOrThrow(MustardDbAdapter.KEY_STATUS_ID));
 
 					onSetAccount(dent.getLong(dent.getColumnIndexOrThrow(MustardDbAdapter.KEY_ACCOUNT_ID)));
 
 					switch(mStatusType) {
 						case Preferences.STATUS_TYPE_REDENT:
 							text ="\u267B @"+dent.getString(dent.getColumnIndexOrThrow(MustardDbAdapter.KEY_SCREEN_NAME));
 							text += " " + dent.getString(dent.getColumnIndexOrThrow(MustardDbAdapter.KEY_STATUS));
 							text = Html.fromHtml(text).toString();
 							break;
 						case Preferences.STATUS_TYPE_REPLY:
 							boolean shownick = mPreferences.getBoolean(Preferences.SHOW_NICKNAME_IN_REPLY_KEY, true);
 							boolean replyall = mPreferences.getBoolean("always_reply_all", false);
 							String source = " " + dent.getString(dent.getColumnIndexOrThrow(MustardDbAdapter.KEY_SOURCE));
 							source = Html.fromHtml(source).toString();
 							boolean fromTwitter = source.equalsIgnoreCase("twitter");
 							String mentionnick = dent.getString(dent.getColumnIndexOrThrow(MustardDbAdapter.KEY_SCREEN_NAME));
 
							if ( fromTwitter ) {
 								Toast.makeText(this, getString(R.string.fromTwitter, source), Toast.LENGTH_LONG).show();
 							}
 							
 							if( shownick || fromTwitter ) {
 								text="@"+mentionnick+" ";
 								if( replyall || mReplyAll ) {
 									String status = " " + dent.getString(dent.getColumnIndexOrThrow(MustardDbAdapter.KEY_STATUS));
 									status = Html.fromHtml(status).toString();
 									String sUserName = org.mumod.android.MustardApplication.sUserName;
 									
 									Pattern pattern = Pattern.compile("([^a-z0-9_!#$%&*" + AT_SIGNS_CHARS + "]|^|RT:?)(" + AT_SIGNS + "+)([a-z0-9_]{1,20})(/[a-z][a-z0-9_\\-]{0,24})?", Pattern.CASE_INSENSITIVE);
 									CharSequence inputStr = status;
 									Matcher matcher = pattern.matcher(inputStr);
 									while( matcher.find() ) {
 										int start = matcher.start();
 										int end = matcher.end();
 										String nick = inputStr.subSequence(start, end).toString();
 										boolean sameNick = nick.trim().equalsIgnoreCase("@" + mentionnick.trim());
 										if( !sameNick && !nick.trim().equalsIgnoreCase("@" + sUserName.trim()) ) {
 											text = text + nick + " ";
 										}
 									}
 								}
 							} else {
 								((TextView) findViewById(R.id.status_text)).setText("Reply to @"+mentionnick);
 							}
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
 			multiAccount=false;
 		} else if (extras != null && extras.containsKey(Preferences.STATUS_ACCOUNT_ROWID)){
 			onSetAccount(extras.getLong(Preferences.STATUS_ACCOUNT_ROWID));
 		} else {
 			mStatusNet = mMustardApplication.checkAccount(mDbHelper);
 			onSetAccount(mStatusNet.getUserId());
 		}
 
 		if (mStatusNet == null) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "No account found. Starting Login activity");
 			showLogin();
 			finish();
 			return;
 		}
 
 		//		String version=mStatusNet.getAccount().getVersion();
 
 		// GEO LOCATION
 		//mCheckBoxLocation = (CheckBox)findViewById(R.id.enable_location);
 		boolean geoEnabled = mPreferences.getBoolean(Preferences.GEOLOCATION_ENABLES_KEY, true);
 		//if(geoEnabled) {
 		//			mCheckBoxLocation.setChecked(true);
 		//			mSharedPreferences = getSharedPreferences(MustardApplication.APPLICATION_NAME, 0);
 		//if(mSharedPreferences.getBoolean(Preferences.GEOLOCATION_ENABLE, false))
 		//	mCheckBoxLocation.setChecked(true);
 		//mCheckBoxLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 		//
 		//	public void onCheckedChanged(CompoundButton buttonView,
 		//			boolean isChecked) {
 		//		mSharedPreferences.edit().putBoolean(Preferences.GEOLOCATION_ENABLE, isChecked).commit();
 		//	}
 		//
 		//});
 		//} else {
 		//mCheckBoxLocation.setChecked(false);
 		//mCheckBoxLocation.setVisibility(View.GONE);
 		//}
 
 		//		setTextLimit(mTextLimit);
 
 		mBodyText.addTextChangedListener(mTextWatcher);
 		mCharCounter= (TextView) findViewById(R.id.char_counter);
 
 		setStatusText(text);
 
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
 					try {
 						Cursor cursor = getContentResolver().query(mImageUri, null, null, null,
 								null);
 
 						if (cursor.moveToFirst()) {
 							mFilename = new File( cursor.getString(cursor.getColumnIndexOrThrow(ImageColumns.DATA)));
 							mTextViewFileName.setVisibility( View.VISIBLE );
 							mTextViewFileName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_attachment, 0, 0, 0);
 							mTextViewFileName.setText( mFilename.getName() );
 						}
 						cursor.close();
 					} catch (Exception e) {
 						Log.e(TAG, "From share: " + e.getMessage());
 					}
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
 			setStatusText(origMessage);
 		}
 
 		if(extras != null) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "Extra is not null");
 			setFromExtras(extras);
 		} else {
 			if (MustardApplication.DEBUG) Log.i(TAG, "Extra is null");
 		}
 
 		if(multiAccount) {
 			setAccountsSpinner();
 		} else {
 			hideSpinner();
 		}
 	}
 
 	private void setStatusText(String text) {
 		mBodyText.setText(text);
 		mBodyText.setSelection(text.length());
 	}
 	
 	private void setFromExtras(Bundle extras) {
 		String lText = extras.getString(Preferences.STATUS_TEXT)  ;
 		if( lText != null) {
 			try {
 				mNotificationManager.cancel(0);
 			} catch (Exception e) {
 				// Should be already disappeared
 			}
 			if (MustardApplication.DEBUG) Log.i(TAG, "Back from a failure");
 			setStatusText(lText);
 			mInReplyTo = extras.getLong(Preferences.STATUS_IN_REPLY_TO);
 			mStatusType = extras.getInt(Preferences.STATUS_TYPE);
 			mLocation = extras.getString(Preferences.STATUS_LOCATION);
 			String fname = extras.getString(Preferences.STATUS_FILE);
 			if (fname != null) {
 				mFilename = new File(fname);
 				mTextViewFileName.setVisibility( View.VISIBLE );
 				mTextViewFileName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_attachment, 0, 0, 0);
 				mTextViewFileName.setText( mFilename.getName() );
 			}
 			//			mSenderAccountId =  extras.getLong(Preferences.STATUS_ACCOUNT_ROWID);
 		} else {
 			if (MustardApplication.DEBUG) Log.i(TAG, "Text is null");
 		}
 	}
 
 	private void setAccountsSpinner() {
 		Spinner accountsSpinner = (Spinner) findViewById(R.id.account_spinner);
 		if (mInReplyTo<0) {
 			Cursor cur = mDbHelper.fetchAllAccountsDefaultFirst();
 			startManagingCursor(cur);
 			if(cur.getCount()>1) {
 				int savedPosition=0;
 				long[] accountIDs = new long[cur.getCount()];
 				//				int[] tmp_textlimits = new int[cur.getCount()];
 				int cc=0;
 				while(cur.moveToNext()) {
 					long rowId=cur.getLong(cur.getColumnIndex(MustardDbAdapter.KEY_ROWID));
 					//					int limit =cur.getInt(cur.getColumnIndex(MustardDbAdapter.KEY_TEXTLIMIT));
 					accountIDs[cc]=rowId;
 					//					tmp_textlimits[cc]=limit;
 					if(rowId==mStatusNet.getUserId())
 						savedPosition=cc;
 					cc++;
 				}
 				cur.moveToPosition(-1);
 				final long[] rowIds = accountIDs; 
 				//final int[] textlimits = tmp_textlimits;
 				SimpleCursorAdapter adapter2 = new SimpleCursorAdapter(this,
 						android.R.layout.simple_spinner_item, 
 						cur,
 						new String[] {MustardDbAdapter.KEY_USER}, 
 						new int[] {android.R.id.text1});
 
 				adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 				accountsSpinner.setAdapter(adapter2);
 
 				accountsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 					public void onItemSelected(AdapterView<?> parent,
 							View view, int pos, long id) {
 						onSetAccount(rowIds[pos]);
 						//setTextLimit(textlimits[pos]);
 						//						Log.d(TAG,"Selected " + mSenderAccountId);
 					}
 
 					public void onNothingSelected(AdapterView<?> arg0) {
 
 					}
 
 				});
 				if (mStatusNet.getUserId() > 0) {
 					accountsSpinner.setSelection(savedPosition);
 				}
 			} else {
 				hideSpinner();
 			}
 		} else {
 			hideSpinner();
 		}
 	}
 
 	private void onSetAccount(long accountId) {
 		//		mSenderAccountId = accountId;
 		mStatusNet = mMustardApplication.checkAccount(mDbHelper,false,accountId);
 		Account account = mStatusNet.getAccount();
 //		setTitle(getString(R.string.app_name)  + " - " + account.getUsername() + "@" + mStatusNet.getURL().getHost());
 		setTextLimit(account.getTextLimit());
 
 	}
 
 	private void setTextLimit(int textlimit) {
 		if (textlimit <= 0) {
 			findViewById(R.id.char_separator).setVisibility(View.GONE);
 			findViewById(R.id.char_limit).setVisibility(View.GONE);
 		} else {
 			//			mTextLimit=textlimit;
 			findViewById(R.id.char_separator).setVisibility(View.VISIBLE);
 			findViewById(R.id.char_limit).setVisibility(View.VISIBLE);
 			((TextView)findViewById(R.id.char_limit)).setText(""+textlimit);
 		}
 	}
 
 	private void hideSpinner() {
 		findViewById(R.id.account_spinner).setVisibility(View.GONE);
 		findViewById(R.id.lbl_account_spinner).setVisibility(View.GONE);
 	}
 
 
 	private void updateStatus() {
 		String status = mBodyText.getText().toString();
 		if (status == null || "".equals(status)) 
 			return;
 
 		if(mStatusNet.getAccount().getTextLimit() > 0 && status.length()>mStatusNet.getAccount().getTextLimit()) {
 			showTextToLong();
 		} else {
 			update();
 		}
 	}
 
 	private void showTextToLong() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(R.string.warning)
 		.setMessage(getString(R.string.warning_text_too_long,mStatusNet.getAccount().getTextLimit()))
 		.setCancelable(false)
 		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				update();
 			}
 		})
 		.setNegativeButton(R.string.no, null);
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	private void showAttachmentTooBig(long maxLength) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(R.string.warning)
 		.setMessage(getString(R.string.warning_attachment_too_long,maxLength))
 		.setCancelable(true)
 		.setNegativeButton(R.string.close, null);
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 	private void update() {
 		String status = mBodyText.getText().toString();
 		mCurrentIntent = new Intent(this,MustardUpdate.class);
 		mCurrentIntent.putExtra(Preferences.STATUS_IN_REPLY_TO, mInReplyTo);
 		if(mStatusType>0)
 			mCurrentIntent.putExtra(Preferences.STATUS_TYPE, mStatusType);
 		mCurrentIntent.putExtra(Preferences.STATUS_ACCOUNT_ROWID, mStatusNet.getUserId());
 		mCurrentIntent.putExtra(Preferences.STATUS_TEXT,status);
 		if (mFilename != null) {
 			long maxLength = mStatusNet.getAccount().getAttachlimit();
 			long fileSize = mFilename.length();
 			
 			if(maxLength > 0 && fileSize > maxLength) {
 				if(fileSize > mStatusNet.getAccount().getAttachlimit()) {
 					// We need to resize it..
 					Log.d(TAG,"Origianl size >> " + fileSize);
 					// Try to guess mime type
 					String mime = URLConnection.guessContentTypeFromName( Uri.fromFile(mFilename ).toString());
 					if(mime != null && mime.indexOf("image") >= 0) {
 						try {
 							
 							Bitmap.CompressFormat bmFormat =  (mime.indexOf("png")>=0) ?  Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
 							// I do 2 tries...
 							UUID uuid = UUID.randomUUID();
 							File dest = File.createTempFile("mustard", uuid.toString() );
 							// First..
 							downsampleFile(mFilename,dest,maxLength,bmFormat);
 							if(dest.length() > maxLength) {
 								Log.d(TAG,"downsampleFile >> " + dest.length());
 								// Second.. and last..
 								downsampleFile(dest,dest,maxLength,bmFormat);
 							}
 							if(dest.length() > maxLength) {
 								Log.d(TAG,"downsampleFile >> " + dest.length());
 								showAttachmentTooBig(maxLength);
 								return;
 							}
 							mFilename = dest;
 						} catch (Exception e) {
 							Log.e(TAG, "Resizing image.. " + e.getMessage(),e);
 						}
 					} else {
 						// Show a message...
 						showAttachmentTooBig(maxLength);
 						return;
 					}
 				}
 			}
 			mCurrentIntent.putExtra(Preferences.STATUS_FILE, mFilename.getAbsolutePath());
 		}
 		new StatusUpdater().execute("");
 		finish();
 	}
 
 	private void downsampleFile(File source, File dest, long maxLength,Bitmap.CompressFormat bmFormat) throws IOException {
 		Bitmap bm = ImageUtil.resize(source, maxLength);
 		bm.compress(bmFormat, 100, new FileOutputStream(dest) );
 		bm.recycle();
 	}
 	
 	private void onSendComplete(int result) {
 		Log.d(TAG,"onSendComplete: " + result);
 
 		if (result != OK) {
 
 			PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0, mCurrentIntent,PendingIntent.FLAG_ONE_SHOT);
 			notify(mPendingIntent, 0, R.drawable.ic_stat_name,
 					getString(R.string.status_update_ko,mErrorUpdateDescription),
 					getString(R.string.status_update_ko_title),
 					mErrorUpdateDescription
 			);
 		} else {
 			if (mHandler != null && mRefreshOnPost) {
 				mHandler.sendEmptyMessage(MSG_REFRESH);
 			}
 		}
 		//		}
 	}
 
 	private void shortUrl() {
 		String status = mBodyText.getText().toString();
 		if (status == null || "".equals(status))
 			return;
 		int pos=0;
 		HashMap<String, String> hmShort = new HashMap<String, String>();
 		while (pos >= 0 && status.toLowerCase().indexOf("http",pos)>=0) {
 			int sp = status.toLowerCase().indexOf("http",pos);
 			int ep = status.indexOf(" ", sp);
 			if (MustardApplication.DEBUG)
 				Log.d("Mustard", "URL begin " + sp + " ends " + ep);
 			pos=ep;
 			String longUrl = "";
 			if (ep < sp) {
 				longUrl = status.substring(sp);
 			} else {
 				longUrl = status.substring(sp, ep);
 			}
 			if (MustardApplication.DEBUG)
 				Log.d("Mustard", "LongURL: " + longUrl);
 			if (longUrl.length() < 10) {
 				Toast.makeText(getApplicationContext(), "Looks like it's already short :)",
 						Toast.LENGTH_LONG).show();
 				return;
 			}
 			UrlShortener shortener;
 			String urlShortener = mPreferences.getString(
 					Preferences.URL_SHORTENER, "ur1.ca");
 			if (urlShortener.equalsIgnoreCase("b1t.it"))
 				shortener = new B1tit(this);
 			else if (urlShortener.equalsIgnoreCase("nd.gd"))
 				shortener = new Ndgd(this);
 			else
 				shortener = new Ur1ca(this);
 
 			String shortUrl = "";
 			try {
 				shortUrl = shortener.doShort(longUrl);
 			} catch (Exception e) {
 				Toast.makeText(getApplicationContext(), "Can't short the URL",
 						Toast.LENGTH_LONG).show();
 				Log.e(MustardApplication.APPLICATION_NAME, "ShortURL: " + e.getMessage());
 				if (MustardApplication.DEBUG)
 					e.printStackTrace();
 			}
 			if (!shortUrl.equals("")) {
 				if (MustardApplication.DEBUG)
 					Log.d("Mustard", "ShortURL: " + shortUrl);
 				hmShort.put(longUrl, shortUrl);
 			}
 		}
 		for (String k : hmShort.keySet()) {
 			status = status.replace(k, hmShort.get(k));
 		}
 
 		if (MustardApplication.DEBUG)
 			Log.i("Mustard", "New status: " + status);
 		setStatusText(status);
 	}
 
 
 	private void notify(PendingIntent intent, int notificationId, int notifyIconId, String tickerText, String title, String text) {
 		
 //		Notification notification = new Notification(notifyIconId, tickerText, System.currentTimeMillis());
 		Notification notification = new Notification(R.drawable.ic_action_users, tickerText, System.currentTimeMillis());
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
 					if (cursor != null) {
 						if (cursor.moveToFirst()) {
 							mFilename = new File( cursor.getString(cursor
 									.getColumnIndexOrThrow(ImageColumns.DATA)));
 							if(mFilename != null)
 								mTextViewFileName.setVisibility( View.VISIBLE );
 								mTextViewFileName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_attachment, 0, 0, 0);
 								mTextViewFileName.setText( mFilename.getName() );								
 						}
 						cursor.close();
 					}
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
 
 				if(mLocation==null) {
 
 					String lsGeoFuzzy = mPreferences.getString(Preferences.GEOLOCATION_FUZZY_KEY, getString(R.string.pref_geo_fuzzy_default));
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
 				} else {
 					String[] l = mLocation.split("|");
 					lon = l[0];
 					lat = l[1];
 				}
 			}
 			try {
 				mCurrentIntent.putExtra(Preferences.STATUS_LOCATION,lon+"|"+lat);
 				//				Log.d(TAG, "mSenderAccountId: " + mSenderAccountId);
 				//				Log.d(TAG, "mStatusNet.getUsernameId(): " + mStatusNet.getUsernameId());
 				//				if(mSenderAccountId > 0 && mStatusNet.getUserId() != mSenderAccountId) {
 				//					mStatusNet = mMustardApplication.loadAccount(mDbHelper, mSenderAccountId);
 				//				}
 				if (mStatusNet == null) {
 					mErrorUpdateDescription = "mStatusNet is null!?!?!?";
 					return KO;
 				}
 				long nid = mStatusNet.update(
 						mBodyText.getText().toString(), 
 						String.valueOf(mInReplyTo), lon, lat, mFilename);
 				//				System.out.println(">>>>>>>>>>>>>> Nid: " + nid);
 				if (nid < 0) {
 					mErrorUpdateDescription = getString(R.string.error_generic);
 					return KO;
 				}
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
 					if(!mRefreshOnPost || mHandler==null) {
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
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.compose, menu);
 
 	    String urlShortener = mPreferences.getString("url_shortener", "");
 	    Log.i( TAG, "URL Shortener: " + urlShortener );
 		if( urlShortener.equalsIgnoreCase( getString(R.string.disabled) ) ) {
 	    	menu.removeItem( R.id.shorturl );
 		}
 		
 	    return true;
 	}
 	
     @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			finish();
             break;
 		case R.id.shorturl:
 			shortUrl();
 			break;
 		case R.id.mn_attach:
 			showFileChooser();
 			break;
 			
 		case R.id.mn_send:
 			updateStatus();
 			break;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		return true;
 	}
 	
 	private static Handler mHandler;
 
 	public static void actionCompose(Context context,Handler handler) {
 		mHandler=handler;
 		Intent i = new Intent(context, MustardUpdate.class);
 		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		context.startActivity(i);
 	}
 
 	public static Intent getActionCompose(Context context,Handler handler) {
 		mHandler=handler;
 		Intent i = new Intent(context, MustardUpdate.class);
 		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		return i;
 	}
 
 	public static void actionReply(Context context,Handler handler,long rowid, boolean replyall) {
 		mHandler=handler;
 		mReplyAll = replyall;
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
 	
 	public static void actionSpamReport(Context context,Handler handler,String user, long userid, String SpamUser, String SpamGroup) {
 		mHandler=handler;
 		Intent i = new Intent(context, MustardUpdate.class);
 		String sGroup;
 		String sUser;
 		
 		if( !SpamGroup.equals("") ) {
 			sGroup = " !" + SpamGroup;  
 		}
 		else {
 			sGroup = "";
 		}
 		
 		if( !SpamUser.equals("") ) {
 			sUser = "@" + SpamUser + " ";
 		}
 		else {
 			sUser = "";
 		}
 		String spamDent = context.getString(R.string.spamReportDent, sUser, user, userid, sGroup);
 		i.putExtra(Preferences.STATUS_TEXT, spamDent);		
 		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		context.startActivity(i);
 	}
 
 }
