 package com.race604.sms;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.race604.sms.model.SmsInfo;
 import com.race604.sms.model.Utility;
 
 public class ThreadActivity extends SherlockListActivity {
 
 	private long thread_id;
 	private List<SmsInfo> mList;
 	private ThreadActivityAdapter mAdapter;
 	private ListView mSmsLv;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		thread_id = getIntent().getExtras().getLong("id");
 		mList = Utility.getSmsAllByThreadId(this, thread_id);
 		mAdapter = new ThreadActivityAdapter(this, mList);
 
 		if (mList.size() > 0) {
 			mAdapter.setContactName(Utility.getCantactByPhone(this,
 					mList.get(0).address).displayName);
 		}
 		
 		mSmsLv = getListView();
 		mSmsLv.setAdapter(mAdapter);
 
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
     	menu.clear();
         menu.add(0, R.string.search, 0, R.string.search)
         	.setActionView(R.layout.action_search)
             .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		int id = item.getItemId();
         switch(id) {
        case android.R.id.home: // home button
         	finish();
         	break;
         case R.string.search:
         	break;
         }
         return true;
 	}
 
 }
