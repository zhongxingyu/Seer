 package com.DGSD.SecretDiary.Activities;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ActivityNotFoundException;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.ClipboardManager;
 import android.text.format.DateUtils;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.DGSD.SecretDiary.Database;
 import com.DGSD.SecretDiary.DiaryApplication;
 import com.DGSD.SecretDiary.Encryption;
 import com.DGSD.SecretDiary.R;
 import com.DGSD.SecretDiary.ActionBar.ActionBar;
 import com.DGSD.SecretDiary.ActionBar.ActionBar.AbstractAction;
 import com.DGSD.SecretDiary.ActionBar.ActionBar.IntentAction;
 import com.DGSD.SecretDiary.QuickActions.ActionItem;
 import com.DGSD.SecretDiary.QuickActions.QuickAction;
 
 public class EntryListActivity extends Activity{
 
 	private ListView mListView;
 
 	private ActionBar mActionBar;
 
 	private DiaryApplication mApplication;
 
 	private LoadEntriesTask mCurrentLoadingTask;
 
 	private SimpleCursorAdapter mAdapter;
 
 	private QuickAction mQuickAction;
 
 	private final ActionItem mDeleteAction = new ActionItem();
 
 	private final ActionItem mShareAction = new ActionItem();
 
 	private final ActionItem mCopyKeyAction = new ActionItem();
 
 	private final ActionItem mCopyValueAction = new ActionItem();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.entrylist);
 
 		mApplication = (DiaryApplication) getApplication();
 
 		mListView = (ListView)findViewById(R.id.listview);
 
 		mActionBar = (ActionBar) findViewById(R.id.actionbar);
 
 		setupActionBar();
 
 		setupListView();
 
 		setupQuickActions();
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		mCurrentLoadingTask = new LoadEntriesTask();
 		
 		mCurrentLoadingTask.execute();
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		if(mCurrentLoadingTask != null){
 			mCurrentLoadingTask.cancel(true);
 			mCurrentLoadingTask = null;
 		}
 
 		if(mAdapter != null) {
 			if(mAdapter.getCursor() != null) {
 				mAdapter.getCursor().close();
 			}
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		if(mCurrentLoadingTask == null) {
 			mCurrentLoadingTask = new LoadEntriesTask();
 			mCurrentLoadingTask.execute();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.entrylist_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 			case R.id.log_out:
 				Intent loginIntent = new Intent(this, LoginActivity.class);
 				loginIntent.putExtra(LoginActivity.EXTRA_INTERNAL, true);
 				startActivity(loginIntent);
 				finish();
 				return true;
 
 			case R.id.help:
 				Intent dialIntent = new Intent(Intent.ACTION_DIAL);
 				dialIntent.setData(Uri.parse("tel:0433315077"));
 				try{
 					startActivity(dialIntent);
 				}catch(ActivityNotFoundException e) {
 					Toast.makeText(this, "Sorry, help is not available! Haha!", 
 							Toast.LENGTH_SHORT).show();
 				}
 				return true;
 
 			case R.id.about:
 				AlertDialog.Builder builder = 
 				new AlertDialog.Builder(EntryListActivity.this);
 
 				builder.setTitle("Secret Diary");
 				builder.setMessage("This app has been a Daniel Lee Grech production!");
 
 				builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.dismiss();
 					}
 				});
 
 				builder.create().show();
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void setupListView() {
 		mListView.setDrawingCacheEnabled(true);
 
 		mListView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> adapter, View v, int pos,
 					long id) {
 				ViewHolder vh = (ViewHolder) v.getTag();
 
 				Intent intent = new Intent(EntryListActivity.this, 
 						EntryActivity.class);
 
				intent.putExtra(EntryActivity.EXTRA_ID, vh.id);
				intent.putExtra(Intent.EXTRA_SUBJECT, vh.key);
				intent.putExtra(Intent.EXTRA_TEXT, vh.value);
				intent.putExtra(EntryActivity.EXTRA_IMG_URI, vh.imgUri);
				intent.putExtra(EntryActivity.EXTRA_FILES, vh.files);
				intent.putExtra(EntryActivity.EXTRA_LAT, vh.lat);
				intent.putExtra(EntryActivity.EXTRA_LONG, vh.lon);
 
 				mActionBar.setProgressBarVisibility(View.VISIBLE);
 				
 				startActivity(intent);
 
 			}
 		});
 
 		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
 			@Override
 			public boolean onItemLongClick(AdapterView<?> adapter, View v, int pos,
 					long id) {
 				final ViewHolder vh = (ViewHolder) v.getTag();
 
 				setQuickActionListeners(vh);
 
 				mQuickAction = new QuickAction(v);
 
 				mQuickAction.addActionItem(mDeleteAction);
 				mQuickAction.addActionItem(mShareAction);
 				mQuickAction.addActionItem(mCopyKeyAction);
 				mQuickAction.addActionItem(mCopyValueAction);
 
 				mQuickAction.setAnimStyle(QuickAction.ANIM_AUTO);
 
 				mQuickAction.show();
 
 				return false;
 			}
 		});
 	}
 
 	private void setupQuickActions() {
 		mDeleteAction.setTitle("Delete");
 		mDeleteAction.setIcon(Resources.getSystem().getDrawable(android.R.drawable.ic_menu_delete));
 
 		mShareAction.setTitle("Share");
 		mShareAction.setIcon(Resources.getSystem().getDrawable(android.R.drawable.ic_menu_share));
 
 		mCopyKeyAction.setTitle("Copy Key");
 		mCopyKeyAction.setIcon(getResources().getDrawable(R.drawable.copy));
 
 		mCopyValueAction.setTitle("Copy Value");
 		mCopyValueAction.setIcon(getResources().getDrawable(R.drawable.copy));
 	}
 
 	private void setupActionBar() {
 
 		mActionBar.addAction(new IntentAction(this, new Intent(this, EntryActivity.class), 
 				android.R.drawable.ic_menu_edit));
 
 		mActionBar.addAction(new AbstractAction(android.R.drawable.ic_menu_search) {
 			@Override
 			public void performAction(View view) {
 				Toast.makeText(EntryListActivity.this, "Search doesn't work yet :(", 
 						Toast.LENGTH_SHORT).show();
 				//onSearchRequested();
 			}
 		});
 	}
 
 	private void setQuickActionListeners(final ViewHolder vh) {
 		mDeleteAction.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				AlertDialog.Builder builder = 
 						new AlertDialog.Builder(EntryListActivity.this);
 
 				builder.setTitle("Are you sure?");
 				builder.setMessage("This really cant be undone. Im super serious");
 
 				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						if(mApplication.getDatabase().delete(Database.TABLE_NAME, 
 								Integer.valueOf(vh.id)) > 0) {
 							Toast.makeText(EntryListActivity.this, "Entry deleted!", 
 									Toast.LENGTH_SHORT).show();
 
 							mCurrentLoadingTask = new LoadEntriesTask();
 							mCurrentLoadingTask.execute();
 						} else {
 							Toast.makeText(EntryListActivity.this, "Error deleting entry!", 
 									Toast.LENGTH_SHORT).show();
 						}
 					}
 				});
 
 				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				});
 
 				builder.create().show();
 
 				dismissPopup();
 			}
 
 		});
 
 		mShareAction.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent sharingIntent = new Intent(Intent.ACTION_SEND);
 				sharingIntent.setType("text/plain");
 				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, vh.value);
 				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, vh.key);
 				startActivity(Intent.createChooser(sharingIntent, "Share entry"));
 
 				dismissPopup();
 			}
 		});
 
 		mCopyKeyAction.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				ClipboardManager clipboard = 
 						(ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
 
 				clipboard.setText(vh.key);
 
 				Toast.makeText(EntryListActivity.this, vh.key + " copied", 
 						Toast.LENGTH_SHORT).show();
 
 				dismissPopup();
 			}
 		});
 
 		mCopyValueAction.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				ClipboardManager clipboard = 
 						(ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
 
 				clipboard.setText(vh.value);
 
 				Toast.makeText(EntryListActivity.this, vh.value + " copied", 
 						Toast.LENGTH_SHORT).show();
 
 				dismissPopup();
 			}
 		});
 	}
 
 	private void dismissPopup(){
 		//Dismisses the popup if possible..
 		try{
 			if(mQuickAction != null)
 				mQuickAction.dismiss();
 		}catch(Exception e){
 			//doesnt matter..
 			e.printStackTrace();
 		}
 	}
 
 	private class LoadEntriesTask extends AsyncTask<Void, Void, Void> {
 		private int dateCol;
 		private int idCol;
 		private int keyCol;
 		private int valCol;
 		private int imgCol;
 		private int fileCol;
 		private int latCol;
 		private int lonCol;
 		
 		private Cursor mCursor;
 
 		private boolean hasError;
 
 		private final String[] FROM = { Database.C_DATE, Database.C_KEY, 
 				Database.C_VALUE, Database.C_ID };
 
 		private final int[] TO = { R.id.date, R.id.key_text, R.id.value_text };
 
 		public LoadEntriesTask() {
 			hasError = false;
 		}
 
 		@Override
 		public void onPreExecute() {
 			mActionBar.setProgressBarVisibility(View.VISIBLE);
 		}
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			mCursor = mApplication.getDatabase().getAll(Database.TABLE_NAME);
 			
 			dateCol = mCursor.getColumnIndex(Database.C_DATE);
 			idCol = mCursor.getColumnIndex(Database.C_ID);
 			keyCol = mCursor.getColumnIndex(Database.C_KEY);
 			valCol = mCursor.getColumnIndex(Database.C_VALUE);
 			imgCol = mCursor.getColumnIndex(Database.C_IMG_URI);
 			fileCol = mCursor.getColumnIndex(Database.C_FILES);
 			latCol = mCursor.getColumnIndex(Database.C_LAT);
 			lonCol = mCursor.getColumnIndex(Database.C_LONG);
 			
 			if(mCursor == null) {
 				hasError = true;
 			}
 			return null;
 		}
 
 		@Override
 		public void onPostExecute(Void arg) {
 			mActionBar.setProgressBarVisibility(View.GONE);
 
 			if(hasError) {
 				Toast.makeText(EntryListActivity.this, "Error getting entries!", 
 						Toast.LENGTH_LONG).show();
 			} else {
 				if(mAdapter == null) {
 					mAdapter = 
 							new SimpleCursorAdapter(EntryListActivity.this, 
 									R.layout.entrylist_item, mCursor, FROM, TO);
 
 					mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
 						public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
 							
 
 							final String pword = mApplication.getPassword();
 
 							if(columnIndex == dateCol) {
 								((TextView)view).setText(
 										DateUtils.getRelativeTimeSpanString(view.getContext(),
 												Long.valueOf(cursor.getString(columnIndex)) ) );
 
 								//Store the tag in the row's tag
 								try {
 									((View)view.getParent()).setTag(
 											new ViewHolder(cursor.getString(idCol), 
 													Encryption.decrypt(pword, cursor.getString(keyCol)), 
 													Encryption.decrypt(pword, cursor.getString(valCol)), 
 													cursor.getString(dateCol),
 													Encryption.decrypt(pword,  cursor.getString(imgCol)),
 													Encryption.decrypt(pword,  cursor.getString(fileCol)),
 													Encryption.decrypt(pword, cursor.getString(latCol)),
 													Encryption.decrypt(pword, cursor.getString(lonCol))));
 								} catch (Exception e) {
									Toast.makeText(EntryListActivity.this, "Error decrypting values", 
 											Toast.LENGTH_SHORT).show();
 									e.printStackTrace();
 								}
 
 								return true;
 							} else if(columnIndex == keyCol) {
 								try {
 									((TextView)view).setText(Encryption.decrypt(pword, 
 											cursor.getString(columnIndex)) );
 								} catch (Exception e) {
 									Toast.makeText(EntryListActivity.this, "Error decrypting values", 
 											Toast.LENGTH_SHORT).show();
 									e.printStackTrace();
 								}
 
 								return true;
 							} else if(columnIndex == valCol) {
 								try {
 									((TextView)view).setText(Encryption.decrypt(pword, 
 											cursor.getString(columnIndex)) );
 								} catch (Exception e) {
 									Toast.makeText(EntryListActivity.this, "Error decrypting values", 
 											Toast.LENGTH_SHORT).show();
 									e.printStackTrace();
 								}
 								
 								return true;
 							} else {
 								return false;
 							}
 						}
 					});
 
 				} else {
 					mAdapter.changeCursor(mCursor);
 				}
 
 				mListView.setAdapter(mAdapter);
 			}
 		}
 
 	}
 
 	public class ViewHolder {
 		public String id;
 		public String key;
 		public String value;
 		public String date;
 		public String imgUri;
 		public String files;
 		public String lat;
 		public String lon;
 
 		public ViewHolder(String i, String k, String v, String d, 
 				String u, String f, String la, String lo) {
 			id = i;
 			key = k;
 			value = v;
 			date = d;
 			imgUri = u;
 			files = f;
 			lat = la;
 			lon = lo;
 		}
 	}
 
 }
