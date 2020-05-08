 /*
    Copyright 2011 kanata3249
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package com.github.kanata3249.ffxieq.android;
 
 import com.github.kanata3249.ffxieq.Equipment;
 import com.github.kanata3249.ffxieq.FFXICharacter;
 import com.github.kanata3249.ffxieq.R;
 import com.github.kanata3249.ffxieq.android.db.FFXIDatabase;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnDismissListener;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.SubMenu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.TextView;
 
 public class AugmentSelectorActivity extends FFXIEQBaseActivity {
 	int mPart;
 	int mJob;
 	int mLevel;
 	int mRace;
 	long mCurrent;
 	long mAugID;
 	long mFilterID;
 	long mLongClickingItemId;
 	boolean mOrderByName;
 	String mFilterByType;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		Bundle param;
 		
 		super.onCreate(savedInstanceState);
 		
 		if (savedInstanceState != null) {
 			param = savedInstanceState;
 		} else {
 			param = getIntent().getExtras();
 		}
 		
 		mPart = param.getInt("Part");
 		mRace = param.getInt("Race");
 		mLevel = param.getInt("Level");
 		mJob = param.getInt("Job");
 		mCurrent = param.getLong("Current");
 		mAugID = param.getLong("AugId");
 		mFilterID = param.getLong("Filter");
 		mOrderByName = param.getBoolean("OrderByName");
 		mFilterByType = param.getString("FilterByType");
 		
 		setContentView(R.layout.augmentselector);
 	}
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
 
 		AugmentListView lv;
 		
 		lv = (AugmentListView)findViewById(R.id.ListView);
 		if (lv != null) {
 			lv.setFilterByID(mFilterID);
 			lv.setOrderByName(mOrderByName);
 			lv.setFilterByType(mFilterByType);
 			lv.setParam(getDAO(), mPart, mRace, mJob, mLevel);
 			
 			lv.setOnItemClickListener(new OnItemClickListener() {
 
 				public void onItemClick(AdapterView<?> arg0, View arg1,
 						int arg2, long arg3) {
 					Intent result = new Intent();
 					
 					result.putExtra("From", "AugmentSelector");
 					result.putExtra("Part", mPart);
 					result.putExtra("Id", -1);
 					result.putExtra("AugId", arg3);
 					setResult(RESULT_OK, result);
 					finish();
 				}
 				
 			});
 			
 			lv.setOnItemLongClickListener(new OnItemLongClickListener() {
 				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
 						int arg2, long arg3) {
 					mLongClickingItemId = arg3;
 					AugmentSelectorActivity.this.openContextMenu(arg0);
 					return true;
 				}
 			});
 			
 			registerForContextMenu(lv);
 		}
 		
 		{
 			Equipment cur = getDAO().instantiateEquipment(mCurrent, mAugID);
 			if (cur == null) {
 				cur = new Equipment(-1, getString(R.string.EquipmentNotSelected), "", "", "", "", 0, false, false, "");
 			}
 			if (cur != null) {
 				TextView tv;
 				View.OnLongClickListener listener = new View.OnLongClickListener() {
 					public boolean onLongClick(View v) {
 						mLongClickingItemId = mAugID;
 						AugmentSelectorActivity.this.openContextMenu(v);
 						return true;
 					}
 				};
 				
 				tv = (TextView)findViewById(R.id.Name);
 				if (tv != null) {
 					tv.setText(cur.getName());
 					tv.setOnLongClickListener(listener);
 					registerForContextMenu(tv);
 				}
 				tv = (TextView)findViewById(R.id.Job);
 				if (tv != null) {
 					tv.setText(cur.getJob());
 					tv.setOnLongClickListener(listener);
 					registerForContextMenu(tv);
 				}
 				tv = (TextView)findViewById(R.id.Description);
 				if (tv != null) {
 					tv.setText(cur.getDescription());
 					tv.setOnLongClickListener(listener);
 					registerForContextMenu(tv);
 				}
 				tv = (TextView)findViewById(R.id.Augment);
 				if (tv != null) {
 					if (cur.getAugment().length() == 0) {
 						tv.setHeight(0);
 					} else {
 						tv.setText(cur.getAugment());
 					}
 					tv.setOnLongClickListener(listener);
 					registerForContextMenu(tv);
 				}
 				tv = (TextView)findViewById(R.id.Level);
 				if (tv != null) {
 					if (tv != null) {
 						int lvl = cur.getLevel();
 						if (lvl > 0)
 							tv.setText(((Integer)lvl).toString());
 						else
 							tv.setText("");
 					}
 					tv.setOnLongClickListener(listener);
 					registerForContextMenu(tv);
 				}
 				tv = (TextView)findViewById(R.id.Race);
 				if (tv != null) {
 					tv.setText(cur.getRace());
 					tv.setOnLongClickListener(listener);
 					registerForContextMenu(tv);
 				}
 				tv = (TextView)findViewById(R.id.Ex);
 				if (tv != null) {
 					if (cur.isEx())
 						tv.setVisibility(View.VISIBLE);
 					else
 						tv.setVisibility(View.INVISIBLE);
 					tv.setOnLongClickListener(listener);
 					registerForContextMenu(tv);
 				}
 				tv = (TextView)findViewById(R.id.Rare);
 				if (tv != null) {
 					if (cur.isRare())
 						tv.setVisibility(View.VISIBLE);
 					else
 						tv.setVisibility(View.INVISIBLE);
 					tv.setOnLongClickListener(listener);
 					registerForContextMenu(tv);
 				}
 				
 			}
 		}
 	}
 
 	@Override
 	protected void onStop() {
 		AugmentListView lv;
 		
 		lv = (AugmentListView)findViewById(R.id.ListView);
 		if (lv != null) {
 			lv.setOnItemClickListener(null);
 			lv.setOnItemLongClickListener(null);
 		}
 
 		TextView tv;
 		
 		tv = (TextView)findViewById(R.id.Name);
 		if (tv != null) {
 			tv.setOnLongClickListener(null);
 		}
 		tv = (TextView)findViewById(R.id.Job);
 		if (tv != null) {
 			tv.setOnLongClickListener(null);
 		}
 		tv = (TextView)findViewById(R.id.Description);
 		if (tv != null) {
 			tv.setOnLongClickListener(null);
 		}
 		tv = (TextView)findViewById(R.id.Augment);
 		if (tv != null) {
 			tv.setOnLongClickListener(null);
 		}
 		tv = (TextView)findViewById(R.id.Level);
 		if (tv != null) {
 			tv.setOnLongClickListener(null);
 		}
 		tv = (TextView)findViewById(R.id.Race);
 		if (tv != null) {
 			tv.setOnLongClickListener(null);
 		}
 		super.onStop();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		
 		outState.putInt("Part", mPart);
 		outState.putInt("Race", mRace);
 		outState.putInt("Job", mJob);
 		outState.putInt("Level", mLevel);
 		outState.putLong("Current", mCurrent);
 		outState.putLong("AugId", mAugID);
 		outState.putLong("Filter", mFilterID);
 		outState.putBoolean("OrderByName", mOrderByName);
 		outState.putString("FilterByType", mFilterByType);
 	}
 
 	static public boolean startActivity(Activity from, int request, FFXICharacter charInfo, int part, long current, long augId) {
 		Intent intent = new Intent(from, AugmentSelectorActivity.class);
 		
 		intent.putExtra("Part", part);
 		intent.putExtra("Race", charInfo.getRace());
 		intent.putExtra("Job", charInfo.getJob());
 		intent.putExtra("Level", charInfo.getJobLevel());
 		intent.putExtra("Current", current);
 		intent.putExtra("AugId", augId);
 		intent.putExtra("Filter", (long)-1);
 		intent.putExtra("OrderByName", false);
 		intent.putExtra("FilterByType", "");
 		
 		from.startActivityForResult(intent, request);
 		return true;
 	}
 
 	static public boolean startActivity(Fragment from, int request, FFXICharacter charInfo, int part, long current, long augId) {
 		Intent intent = new Intent(from.getActivity(), AugmentSelectorActivity.class);
 		
 		intent.putExtra("Part", part);
 		intent.putExtra("Race", charInfo.getRace());
 		intent.putExtra("Job", charInfo.getJob());
 		intent.putExtra("Level", charInfo.getJobLevel());
 		intent.putExtra("Current", current);
 		intent.putExtra("AugId", augId);
 		intent.putExtra("Filter", (long)-1);
 		intent.putExtra("OrderByName", false);
 		intent.putExtra("FilterByType", "");
 		
 		from.startActivityForResult(intent, request);
 		return true;
 	}
 	static public boolean isComeFrom(Intent data) {
 		return data.getStringExtra("From").equals("AugmentSelector");
 	}
 	static public int getPart(Intent data) {
 		return data.getIntExtra("Part", -1);
 	}
 	
 	static public long getEquipmentId(Intent data) {
 		return data.getLongExtra("Id", -1);
 	}
 
 	static public long getAugmentId(Intent data) {
 		return data.getLongExtra("AugId", -1);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.augmentselector, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		MenuItem item;
 		
 		item = menu.findItem(R.id.OrderByName);
 		if (item != null) {
 			if (mOrderByName)
 				item.setTitle(getString(R.string.OrderByLevel));
 			else
 				item.setTitle(getString(R.string.OrderByName));
 		}
 		
 		AugmentListView lv;
 		
 		item = menu.findItem(R.id.FilterByType);
 		SubMenu submenu = item.getSubMenu();
 		submenu.removeGroup(R.id.FilterByType);
 
 		lv = (AugmentListView)findViewById(R.id.ListView);
 		if (lv != null) {
 			String types[] = lv.getAvailableWeaponTypes();
 			
 			if (types == null || types.length == 1) {
 				item.setEnabled(false);
 			} else {
 				item.setEnabled(true);
 				submenu.add(R.id.FilterByType, -1, Menu.NONE, getString(R.string.ResetFilterByType));
 				for (int i = 0; i < types.length; i++) {
 					submenu.add(R.id.FilterByType, i, Menu.NONE, types[i]);
 				}
 			}
 			
 		}
 
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		AugmentListView lv = (AugmentListView)findViewById(R.id.ListView);
 
 		if (item.getGroupId() == R.id.FilterByType) {
 			if (item.getItemId() < 0) {
 				mFilterByType = "";
 			} else {
 				mFilterByType = (String)item.getTitle();
 			}
 			lv.setFilterByType(mFilterByType);
 			return true;
 		}
 
 		switch (item.getItemId()) {
 		case R.id.OrderByName:
 			mOrderByName = !mOrderByName;
 			if (lv != null) {
 				lv.setOrderByName(mOrderByName);
 			}
 			return true;
 		case R.id.Remove:
 			{
 				Intent result = new Intent();
 				
 				result.putExtra("From", "AugmentSelector");
 				result.putExtra("Part", mPart);
 				result.putExtra("Id", -1);
 				result.putExtra("AugId", -1);
 				setResult(RESULT_OK, result);
 				finish();
 				return true;
 			}
 		case R.id.Filter:
 			showDialog(0);
 			return true;
 		case R.id.ResetFilter:
 			if (lv != null) {
 				lv.setFilter("");
 			}
 			mFilterID = -1;
 			return true;
 		case R.id.EquipmentList:
 			{
 				Intent result = new Intent();
 				
 				result.putExtra("From", "AugmentSelector");
 				result.putExtra("Part", mPart);
 				setResult(RESULT_FIRST_USER, result);
 				finish();
 				return true;
 			}
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.EditAugment:
 			if (mLongClickingItemId == -1)
 				AugmentEditActivity.startActivity(this, 0, getFFXICharacter(), mPart, mCurrent, -1);
 			else
 				AugmentEditActivity.startActivity(this, 0, getFFXICharacter(), mPart, -1, mLongClickingItemId);
 			return true;
 		case R.id.DeleteAugment:
 			showDialog(R.string.QueryDeleteAugment);
 			return true;
 		}
 
 		Equipment eq = getDAO().instantiateEquipment(-1, mLongClickingItemId);
		String name = eq.getName();
 		Intent intent;
 		if (eq != null) {
 			String[] urls = getResources().getStringArray(R.array.SearchURIs);
 			String url;
 
 			url = null;
 			switch (item.getItemId()) {
 			case R.id.WebSearch0:
 				url = urls[0];
 				break;
 			case R.id.WebSearch1:
 				url = urls[1];
 				break;
 			case R.id.WebSearch2:
 				url = urls[2];
 				break;
 			case R.id.WebSearch3:
 				url = urls[3];
 				break;
 			case R.id.WebSearch4:
 				url = urls[4];
 				break;
 			case R.id.WebSearch5:
 				url = urls[5];
 				break;
 			case R.id.WebSearch6:
 				url = urls[6];
 				break;
 			case R.id.WebSearch7:
 				url = urls[7];
 				break;
 			default:
 				url = null;
 				break;
 			}
 			if (url != null) {
 				intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url + Uri.encode(name.split("\\+")[0])));
 				startActivity(intent);
 				return true;
 			}
 		}
 		return super.onContextItemSelected(item);
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.augmentselector_context, menu);
 		
 		MenuItem item = menu.findItem(R.id.DeleteAugment);
 		if (item != null) {
 			item.setEnabled(mLongClickingItemId != mAugID);
 		}
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case R.string.QueryDeleteAugment:
 			{
 				Dialog dialog;
 				AlertDialog.Builder builder;
 
 				builder = new AlertDialog.Builder(this);
 				builder.setCancelable(true);
 		    	builder.setMessage(getString(R.string.QueryDeleteAugment));
 		    	builder.setTitle(getString(R.string.app_name));
 		    	builder.setPositiveButton(R.string.DeleteOK, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 		    			((FFXIDatabase)getDAO()).deleteAugment(mLongClickingItemId);
 
 		    			AugmentListView lv = (AugmentListView)findViewById(R.id.ListView);
 		    			if (lv != null)
 		    				lv.setParam(getDAO(), mPart, mRace, mJob, mLevel);
 						dismissDialog(R.string.QueryDeleteAugment);
 					}
 				});
 		    	builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						dismissDialog(R.string.QueryDeleteAugment);
 					}
 				});
 				dialog = builder.create();
 				return dialog;
 			}
 		default:
 			{
 				FilterSelectorDialog dialog = new FilterSelectorDialog(this);
 		
 				dialog.setOnDismissListener(new OnDismissListener() {
 					public void onDismiss(DialogInterface dialog) {
 						FilterSelectorDialog fsd = (FilterSelectorDialog)dialog;
 						String filter = fsd.getFilterString();
 						mFilterID = fsd.getFilterID();
 		
 						if (filter.length() > 0) {
 							AugmentListView lv = (AugmentListView)findViewById(R.id.ListView);
 							if (lv != null) {
 								lv.setFilter(filter);
 							}
 						}
 						
 					}
 					
 				});
 				return dialog;
 			}
 		}
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == Activity.RESULT_OK) {
 			int part;
 			long id, augid;
 			if (AugmentEditActivity.isComeFrom(data)) {
 				part = AugmentEditActivity.getPart(data);
 				id = AugmentEditActivity.getEquipmentId(data);
 				augid = AugmentEditActivity.getAugmentId(data);
 			} else {
 				part = -1;
 				id = augid = -1;
 			}
 				
 			if (part == mPart) {
 				Intent result = new Intent();
 				
 				result.putExtra("From", "AugmentSelector");
 				result.putExtra("Part", mPart);
 				result.putExtra("Id", id);
 				result.putExtra("AugId", augid);
 				setResult(RESULT_OK, result);
 				finish();
 				return;
 			}
 		}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 }
