 /*
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
 
 import org.mumod.android.MustardDbAdapter;
 import org.mumod.android.R;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.widget.TextView;
 
 public class MustardConversation extends MustardBaseActivity {
 	
 	public void onCreate(Bundle savedInstanceState) {
 		TAG = getClass().getCanonicalName();
 		super.onCreate(savedInstanceState);
 		
 		if( mStatusNet != null) {
 			TextView tagInfo = (TextView) findViewById(R.id.dent_info);
 			tagInfo.setText(R.string.menu_conversation);
 			getStatuses();
 		}
 	}
 
 	@Override
 	protected void onSetupTimeline() {
 		DB_ROW_TYPE=MustardDbAdapter.ROWTYPE_CONVERSATION;
 		Intent intent = getIntent();
 		Bundle extras = intent.getExtras();    
 		if (extras!=null && extras.containsKey(MustardDbAdapter.KEY_ROWID))
 			DB_ROW_EXTRA = Long.toString(extras.getLong(MustardDbAdapter.KEY_ROWID));
 		else {
 			Uri data = intent.getData();
 			DB_ROW_EXTRA=data.getLastPathSegment();
 		}
 		DB_ROW_ORDER = "ASC";
 		MustardDbAdapter mDbHelper = getDbAdapter();
 		Cursor c = mDbHelper.fetchStatus(Long.parseLong(DB_ROW_EXTRA));
 		mStatusNetAccountId = c.getLong(c.getColumnIndex(MustardDbAdapter.KEY_ACCOUNT_ID));
 		DB_ROW_EXTRA=c.getString(c.getColumnIndex(MustardDbAdapter.KEY_STATUS_ID));
 		c.close();
 		mDbHelper.close();
 	}
 
 	@Override
 	protected void onBeforeFetch() {
 		mNoMoreDents=true;
 		isRefreshEnable=false;
 		isBookmarkEnable=false;
 		isConversationEnable=false;
 	}
 
 	@Override
 	protected void onAfterFetch() {
 	}
 
 	@Override
 	protected void onSetListView() {
 		if(mLayoutLegacy) {
 			setContentView(R.layout.legacy_dents_list);
			R_ROW_ID=R.layout.legacy_conversation_list_item;
 		} else {
 			setContentView(R.layout.dents_list);
 			R_ROW_ID=R.layout.conversation_list_item;
 		}
 	}
 	
 	public static void actionHandleTimeline(Context context,long rowid) {
 		Intent intent = new Intent(context, MustardConversation.class);
 		intent.putExtra(MustardDbAdapter.KEY_ROWID, rowid);
 	    context.startActivity(intent);
 	}
 
 }
