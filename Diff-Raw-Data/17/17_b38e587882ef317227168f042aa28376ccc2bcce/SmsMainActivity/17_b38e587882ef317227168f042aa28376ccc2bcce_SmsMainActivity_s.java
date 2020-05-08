 package com.softmo.smssafe;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import com.softmo.smssafe.R;
 import com.softmo.smssafe.dbengine.IMContact;
 import com.softmo.smssafe.dbengine.IMSmsGroup;
 import com.softmo.smssafe.main.IMEvent;
 import com.softmo.smssafe.utils.MyException;
 import com.softmo.smssafe.utils.MyException.TTypMyException;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class SmsMainActivity extends CMBaseListActivity  {
 	
 	protected static final int NEW_SMS_RESULT = 1002;
 	
 	private static final int IDD_DELTHREAD = 1001;
 	private static final int IDD_DELALL = 1002;
 
 	private Button mBtNewSms;
 	private final android.os.Handler mHandler = new android.os.Handler();
 	
 	private MainAdapter mAdapter;
 	private Map<Integer, String> mSavedMap;
 	
 	private void GotoGroup(int idx) throws MyException {
 		String hash = getHashByIndex(idx);
 		
         Intent intent = new Intent(this, SmsViewerActivity.class); 
         intent.putExtra(SmsViewerActivity.HASH, hash); 
         startActivity(intent);	
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		Log.d("!!!", "List item clicked");
 		
 		try {
 			GotoGroup(position);
 		} catch (MyException e) {
 			ErrorDisplayer.displayError(this, e);
 		}
 
 	}
 	   
     void GotoNewSms() {
     	Intent intent = new Intent(this, SmsNewActivity.class); 
         startActivityForResult(intent, NEW_SMS_RESULT);
     }
 	    
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         mBtNewSms = (Button) findViewById(R.id.btNewSms);
         mBtNewSms.setOnClickListener( new View.OnClickListener() {
 			
 			public void onClick(View v) {
 		        GotoNewSms();
 			}
 		});
         
         registerForContextMenu(getListView());
     }
     
 
     public void onMainBind() throws MyException {
         mAdapter = new MainAdapter(getHelper().getMain(), this);
        	mAdapter.setMap(mSavedMap);
        	
        	Log.d("!!!", "Main: setting adapter...");
         setListAdapter(mAdapter);    	
     }
  
 
 	private final Runnable mRunReload = new Runnable() {
 		
 		public void run() {
 			Log.d("!!!", "Main: mRunReload");
 			mAdapter.notifyDataSetChanged();	
 		}
 	};
 	
 	private int mThreadForDelId;
 	
 	public void listenerEvent(IMEvent event) throws MyException {
 		Log.v("!!!", "listenerEvent: "+event.getTyp());
 		
 		switch (event.getTyp()) {
 		case ESmsRecieved:
 		case ESmsUpdated:
 		case ESmsOutboxAdded:
 		case ESmsDelMany:
 		case ESmsDeleted:
 		case ESmsUpdatedMany:
 			mHandler.removeCallbacks(mRunReload);
 			mHandler.postDelayed(mRunReload, TStruct.DEFAULT_DELAY_VIEW_RELOAD);
 			break;
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	       if (resultCode == RESULT_OK) {
 	            switch (requestCode) {
 	            case NEW_SMS_RESULT:
 	            	
 	            	String hash = data.getStringExtra(SmsViewerActivity.HASH);
 	            	
 	            	if (hash!=null && hash.length()>0) {
 		                Intent intent = new Intent(this, SmsViewerActivity.class); 
 		                intent.putExtra(SmsViewerActivity.HASH, hash); 
 		                startActivity(intent);
 	            	}
 
 	                break;
 	            }
 	       }
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		
 		MenuInflater i = getMenuInflater();
 		i.inflate(R.menu.menu_main, menu);
 
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		try {
 			switch(item.getItemId()) {
 			case R.id.mnuMainNewSms:
 				GotoNewSms();
 				break;
 			case R.id.mnuMainDelAll:
 				showDialog(IDD_DELALL);
 				break;
 			case R.id.mnuMainSettings:
 				startActivity(new Intent(this, OptionsActivity.class));
 				break;
 			case R.id.mnuMainAbout:
 				startActivity(new Intent(this, AboutActivity.class));
 				break;
 			case R.id.mnuMainLock:
 				getHelper().lockNow();
 				break;
 			case R.id.mnuMainRead:
 				getHelper().getMain().DbWriter().SmsMarkAllRead();
 				Toast.makeText(this, R.string.all_sms_readed, Toast.LENGTH_SHORT).show();
 				break;
 			}
 		} catch(Exception e) {
 			ErrorDisplayer.displayError(this, TTypMyException.EErrUnknown.getValue());
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		try {
 			if (v.getId() == android.R.id.list) {
 				
 				MenuInflater i = getMenuInflater();
 				i.inflate(R.menu.main_context, menu);
 				
 				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
 				
 				String nam = getNameByIndex(info.position);
 				menu.setHeaderTitle(nam);
 			}
 		} catch (MyException e) {
 			ErrorDisplayer.displayError(this, e.getId().getValue());
 		}
 		super.onCreateContextMenu(menu, v, menuInfo);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		try {
 			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 			int idx = info.position;
 			switch(item.getItemId()) {
 			case R.id.mnuMainView:
 				GotoGroup(idx);
 				break;
 			case R.id.mnuMainDelThread:
 				mThreadForDelId = idx;
 				showDialog(IDD_DELTHREAD);
 				break;
 			}
 		}catch (MyException e) {
 			ErrorDisplayer.displayError(this, e);
 		}catch(Exception e) {
 			ErrorDisplayer.displayError(this, TTypMyException.EErrUnknown.getValue());
 		}
 		return super.onContextItemSelected(item);
 	}
 	
 	private String getNameByIndex(int idx) throws MyException {
 		
 		int id = (int) mAdapter.getItemId(idx);
 		
 		IMSmsGroup group = getHelper().getLocator().createSmsGroup();
 		getHelper().getMain().DbReader().QuerySms().getGroupById(group,id);
 		
 		String nam = group.getPhone();
 		nam = getHelper().getMain().decryptString(nam);
 		IMContact c = getHelper().getMain().DbReader().QueryContact().getByPhone(nam);
 		if (c!=null) nam = c.getName();
 		
		String msg = getResources().getString(R.string.sms_delthreadconf, nam);	
		return msg;
 	}
 	
 	private String getHashByIndex(int idx) throws MyException {
 		
 		int id = (int) mAdapter.getItemId(idx);
 		
 		IMSmsGroup group = getHelper().getLocator().createSmsGroup();
 		getHelper().getMain().DbReader().QuerySms().getGroupById(group,id);
 		
 		String hash = group.getHash();
 		return hash;
 	}
 	
 	AlertDialog ShowDelThreadDialog(int idx) throws MyException {
 	
 		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
 		
		dlg.setMessage( getNameByIndex(idx) );
		
		final String hash = getHashByIndex(idx);
 		
 		dlg.setPositiveButton(getResources().getString(R.string.yes), new OnClickListener() {
 			
 			public void onClick(DialogInterface arg0, int arg1) {
 				try {
 					getHelper().getMain().DbWriter().SmsDeleteByHash( hash );
 				} catch (MyException e) {
 					ErrorDisplayer.displayError(SmsMainActivity.this, e.getId().getValue());
 				}
 				
 			}
 		});
 		
 		dlg.setNegativeButton(getResources().getString(R.string.no), new OnClickListener() {
 			
 			public void onClick(DialogInterface arg0, int arg1) {
 				arg0.cancel();
 			}
 		});
 		
 		return dlg.create();
 	}
 	
 	AlertDialog ShowDelAllDialog() throws MyException {
 		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
 		
 		String msg = getResources().getString(R.string.sms_delallconf);
 		dlg.setMessage(msg);
 		
 		dlg.setPositiveButton(getResources().getString(R.string.yes), new OnClickListener() {
 			
 			public void onClick(DialogInterface arg0, int arg1) {
 				try {
 					getHelper().getMain().DbWriter().SmsDelAll();
 				} catch (MyException e) {
 					ErrorDisplayer.displayError(SmsMainActivity.this, e.getId().getValue());
 				}
 				
 			}
 		});
 		
 		dlg.setNegativeButton(getResources().getString(R.string.no), new OnClickListener() {
 			
 			public void onClick(DialogInterface arg0, int arg1) {
 				arg0.cancel();
 			}
 		});
 		
 		return dlg.create();
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog dlg = super.onCreateDialog(id);
 		if (dlg!=null)
 			return dlg;
 		
 		try {
 			switch(id) {
 			case IDD_DELTHREAD:
 				dlg = ShowDelThreadDialog(mThreadForDelId);
 				break;
 			case IDD_DELALL:
 				dlg = ShowDelAllDialog();
 				break;
 			}
 		} catch (MyException e) {
 			ErrorDisplayer.displayError(this, e.getId().getValue());
 		}
 		return dlg;
 	}
 
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog) {
 		try {
 			switch(id) {
 			case IDD_DELTHREAD:
				((AlertDialog)dialog).setMessage( getNameByIndex(mThreadForDelId) );
 				break;
 			}
 		} catch (MyException e) {
 			ErrorDisplayer.displayError(this, e.getId().getValue());
 		}
 		
 		super.onPrepareDialog(id, dialog);
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		Log.d("!!!", "Main: onSaveInstanceState");
 		
 		if (mAdapter!=null) {
 			Map<Integer, String> map = mAdapter.getMap(); 
 			Iterator<Integer> i = map.keySet().iterator();
 			
 			int siz = map.size();
 			ArrayList<Integer> keys = new ArrayList<Integer>(siz);
 			ArrayList<String> vals = new ArrayList<String>(siz);
 			while (i.hasNext()) {
 				int key = i.next();
 				keys.add(key);
 				vals.add(map.get(key));
 			}
 			outState.putIntegerArrayList("mainKeys", keys);
 			outState.putStringArrayList("mainVals", vals);
 		}
 		
 		super.onSaveInstanceState(outState);
 	}	
 	
 	
 	@Override	
 	protected void onRestoreInstanceState(Bundle outState) {
 		Log.d("!!!", "Main: intRestoreInstanceState");
 		
 		ArrayList<Integer> keys = outState.getIntegerArrayList("mainKeys");
 		ArrayList<String> vals = outState.getStringArrayList("mainVals");
 		if (keys!=null && vals!=null) {
 			mSavedMap = new HashMap<Integer, String>();
 			for (int i=0; i<keys.size(); i++)
 				mSavedMap.put(keys.get(i), vals.get(i));
 		}	
 		
 		super.onRestoreInstanceState(outState);
 	}
 
 }
