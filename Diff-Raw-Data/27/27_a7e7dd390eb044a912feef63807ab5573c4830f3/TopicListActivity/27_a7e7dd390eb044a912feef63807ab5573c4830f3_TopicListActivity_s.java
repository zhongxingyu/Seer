 /*
  * Copyright (C) 2011 htbest2000@gmail.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.htbest2000.v2ex.ui;
 
 
 import com.htbest2000.v2ex.R;
 import com.htbest2000.v2ex.io.Downloader;
 import com.htbest2000.v2ex.provider.Database;
 import com.htbest2000.v2ex.service.SyncService;
 import com.htbest2000.v2ex.util.DetachableResultReceiver;
 import com.htbest2000.v2ex.util.Misc;
 import com.htbest2000.v2ex.util.ToastMaster;
 import com.htbest2000.v2ex.util.State;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.Calendar;
 import java.util.TimeZone;
 
 import roboguice.activity.RoboActivity;
 import roboguice.inject.InjectResource;
 import roboguice.inject.InjectView;
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.ConnectivityManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.text.format.DateUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.animation.Animation;
 import android.widget.AdapterView;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.SimpleCursorAdapter.ViewBinder;
 
 
 public class TopicListActivity extends RoboActivity implements DetachableResultReceiver.Receiver {
 	static final int CONTENT_MAX_LEN = 40; 
 	static final String TOPIC_ID = "topic_id";
 	static final String LATEST_TOPICS="/api/topics/latest.json";
 
 	Handler mHandler = new Handler();
 
 	@InjectView(R.id.topics_list) private ListView mTopicList;
 	@InjectView(R.id.btn_title_refresh) private ImageView mRefreshView;
 	@InjectResource(R.anim.spin_self) private Animation mAniSpinSelf;
 
 	private MyState mState;
 	private SimpleCursorAdapter mTopicsAdapter;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.topic_list_activity);
 		
         // restore/create activity state
         mState = (MyState) getLastNonConfigurationInstance();
         final boolean previousState = (mState != null);
         if (previousState) {
             // Start listening for SyncService updates again
             mState.mReceiver.setReceiver(this);
         } else {
             mState = new MyState();
             mState.mReceiver.setReceiver(this);
         }
         
         setupTopicListAdapter();
 	}
 	
     @Override
     public Object onRetainNonConfigurationInstance() {
     	mState.mReceiver.clearReceiver();
         return mState;
     }
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		// setupTopicListAdapter();
 	}
 	
 
 	/** Handle "refresh" title-bar action. */
 	public void onRefreshClick(View v) {
 		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 
 		if (cm != null && cm.getActiveNetworkInfo() != null) {
 			final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, SyncService.class);
 			intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mState.mReceiver);
 			intent.putExtra(SyncService.OPERATOR, SyncService.OPERATOR_FETCH_TOPIC_LIST);
 			intent.putExtra(SyncService.PATH, LATEST_TOPICS);
 			startService(intent);
 		} else {
 			final Toast toast = Toast.makeText(TopicListActivity.this.getApplicationContext(),
 					R.string.no_available_connection, Toast.LENGTH_SHORT);
 			ToastMaster.setToast(toast);
 			toast.show();
 		}
 
 	}
 
 	void setupTopicListAdapter() {
 		Cursor cur = Misc.getTopicsCursor( this );
 		if (null != cur) {
 			if (cur.getCount() != 0) {
 				cur.moveToFirst();
 				this.startManagingCursor(cur);
 				mTopicsAdapter = new SimpleCursorAdapter(this, R.layout.topic_list_row, cur,
 						new String[] { Database.Columns.Topics.NAME_TITLE, 
 										Database.Columns.Topics.NAME_CONTENT,
 										Database.Columns.Topics.NAME_REPLIES,
 										Database.Columns.Topics.NAME_MEMBER_USERNAME,
 										Database.Columns.Topics.NAME_NODE_TITLE,
 										Database.Columns.Topics.NAME_CREATED,},
 						new int[] { R.id.topic_title, R.id.topic_content, R.id.topic_replies,
 									R.id.topic_author, R.id.topic_node, R.id.created});
 
 				mTopicsAdapter.setViewBinder(new ViewBinder() {
 					@Override
 					public boolean setViewValue(View view, Cursor cur, int field) {
 						final int content_field = Database.Columns.Topics.ID_CONTENT;
 						final int created_field = Database.Columns.Topics.ID_CREATED;
 						
 						if (content_field == field) {
 							final TextView tv = (TextView)view;
 							String content = cur.getString(content_field);
 							if (null == content)
 								content = "";
 							if (content.length() > CONTENT_MAX_LEN) {
 								content = content.substring(0, CONTENT_MAX_LEN-1) + "...";
 							}
 							if (content.length() == 0) {
 								tv.setVisibility(View.GONE);
 							} else {
 								tv.setText( content );
 								tv.setVisibility(View.VISIBLE);
 							}
 							return true;
 						} else if (created_field == field) {
 							final TextView tv = (TextView)view;
 							final long postAt = cur.getLong(created_field);
 							final long curr = System.currentTimeMillis();
 							final int timeZone = TimeZone.getDefault().getOffset(postAt);
 							final long interval = (curr - postAt - timeZone) / 1000;
 							String ret = Misc.formatRelativeTime(interval);
 							tv.setText(ret);
 							return true;
 						}
 						return false;
 					}
 				});
 
 				mTopicList.setAdapter(mTopicsAdapter);
 				mTopicList.setOnItemClickListener( new OnItemClickListener() {
 					@Override
 					public void onItemClick(AdapterView<?> parent, View view,
 							int position, long id) {
 						Intent intent = new Intent(TopicListActivity.this.getApplicationContext(), TopicActivity.class);
 						intent.putExtra(TOPIC_ID, id);
 						startActivity(intent);
 					}
 				});
 			}
 			else {
 				cur.close();
 			}
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main_menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_login: {
 			Intent i = new Intent(this, LoginActivity.class);
 			startActivity(i);
 			break;
 		}
 
 		case R.id.menu_settings: {
 			Intent i = new Intent(this, SettingsActivity.class);
 			startActivity(i);
 			break;
 		}
 
 		default: {
 			break;
 		}
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public void onReceiveResult(int resultCode, Bundle resultData) {
         switch (resultCode) {
         	case SyncService.STATUS_RUNNING: {
         		mState.mSyncing = true;
         		mRefreshView.setFocusable(false);
         		mRefreshView.setClickable(false);
         		mRefreshView.startAnimation(mAniSpinSelf);
         		break;
         	}
         	case SyncService.STATUS_FINISHED: {
         		mState.mSyncing = false;
         		mState.mGotError = false;
         		mRefreshView.setFocusable(true);
         		mRefreshView.setClickable(true);
         		mRefreshView.clearAnimation();
         		removeOldTopics();
         		setupTopicListAdapter();
         		break;
         	}
         	case SyncService.STATUS_ERROR: {
         		// Error happened down in SyncService, show as toast.
         		mState.mSyncing = false;
         		mState.mGotError = true;
         		mRefreshView.setFocusable(true);
         		mRefreshView.setClickable(true);
         		mRefreshView.clearAnimation();
         		final Toast toast = Toast.makeText(TopicListActivity.this.getApplicationContext(),
         				R.string.fetch_topic_list_error, Toast.LENGTH_SHORT);
         		ToastMaster.setToast(toast);
         		toast.show();
         		break;
         	}
         }
 	}
 	
 	private void removeOldTopics() {
 		final int days = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("days_to_sync", "7"));
 		final long limit = System.currentTimeMillis() - days*24*60*60*1000;
 		Database.getInstance(this).delete(Database.TABLE_TOPICS, Database.Columns.Topics.NAME_CREATED+"<"+limit, null);
 	}
 
 	private static class MyState extends State {
 		int mPos;
 	}
 }
