 package com.viva.mypad;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.viva.mypad.Adapter.DBAdapter;
 import com.viva.mypad.Adapter.MemoArrayAdapter;
 import com.viva.mypad.Comparator.DateComparator;
 import com.viva.mypad.Comparator.ImportantComparator;
 import com.viva.mypad.Comparator.TitleComparator;
 import com.viva.mypad.Item.MemoItem;
 import com.viva.mypad.Util.Util;
 
 public class MyMemoPadActivity extends SherlockActivity implements OnItemClickListener
 {
     private final int INSERT_OK = 0;
     private final int UPDATE_MESSAGE = 100;
     private final int DELETE_ALL_MESSAGE = 200;
     private final String DELETED_MEMO = "com.viva.mypad.DELETED_MEMO";
     private final String DELETED_ALL_MEMO = "com.viva.mypad.DELETED_ALL_MEMO";
     private final int MENU_EDIT_ID = 300;
     private final int MENU_DELETE_ID = 400;
 
     private DBAdapter mDbAdapter;
     private ListView mMemoListView;
     private ArrayList<MemoItem> mMemoList;
     private MemoArrayAdapter mMemoListAdapter;
     private TextView mEmptyView;
     private TextView mStatusView;
     //private SwipeDetector mSwipeDetector;
     private SharedPreferences mSharedPref;
     private Editor mPrefEditor;
     private int mSelectedCompareItem;
     private boolean mCollectLog;
 
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         ActionBar actionBar = getSupportActionBar();
         actionBar.setTitle(this.getResources().getString(R.string.actionbar_title));
 
         mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
         mPrefEditor = mSharedPref.edit();
         mSelectedCompareItem = mSharedPref.getInt("compare_item", 0);
 
         if(mSharedPref.getBoolean("aaaa", false))
         {
             ;
         }
         else
         {
             showNotifyCollectLogDialog();
             mPrefEditor.putBoolean("aaaa", true);
             mPrefEditor.commit();
         }
 
         mDbAdapter = new DBAdapter(this);
         mDbAdapter.open();
 
         mMemoList = new ArrayList<MemoItem>();
         mMemoListView = (ListView)findViewById(R.id.note_list_view);
         mMemoListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
         mMemoListAdapter = new MemoArrayAdapter(this, mMemoList, mDbAdapter);
         mEmptyView = (TextView)findViewById(R.id.empty);
         mStatusView = (TextView)findViewById(R.id.statusTextView);
         //mSwipeDetector = new SwipeDetector();
 
         loadData();
         if(mMemoList.size() == 0)
         {
             mMemoListView.setEmptyView(mEmptyView);
         }
         else
         {
             mEmptyView.setVisibility(View.GONE);
             mMemoListView.setAdapter(mMemoListAdapter);
         }
 
         mStatusView.setText(mMemoList.size() + getResources().getString(R.string.status_text));
         mMemoListView.setOnItemClickListener(this);
         registerForContextMenu(mMemoListView);
         //mMemoListView.setOnTouchListener(mSwipeDetector);
     }
 
     public void onResume()
     {
         super.onResume();
         loadData();
         mHandler.sendEmptyMessage(UPDATE_MESSAGE);
     }
 
     protected void onDestroy()
     {
         super.onDestroy();
         if (mDbAdapter != null)
         {
             mDbAdapter.close();
         }
 
         if(mCollectLog)
         {
             if(Util.deleteLogFile())
                 Log.i("MYMEMOPAD", "Complete to elete log file");
         }
     }
 
     private Handler mHandler = new Handler()
     { 
         public void handleMessage(Message msg)
         {  
             switch(msg.what)
             {
                 case UPDATE_MESSAGE:
                     mMemoListAdapter.notifyDataSetChanged();
                     mMemoListView.setAdapter(mMemoListAdapter);
                 break;
 
                 case DELETE_ALL_MESSAGE:
                     mMemoListAdapter.notifyDataSetChanged();
                     mEmptyView.setVisibility(View.VISIBLE);
                 break;
             }
         }
     };
 
     public void loadData()
     {
         mMemoList.removeAll(mMemoList);
         Cursor cursor = mDbAdapter.getAllMemo();
         cursor.moveToFirst();
 
         if(cursor.getCount() >= 1)
         {
             mMemoList.add(new MemoItem(cursor.getLong(0), 
                     getTitle(cursor.getString(1), cursor.getString(2)), 
                     cursor.getString(3),
                     cursor.getInt(4)));
         }
 
         if(cursor.getCount() > 1)
         {
             while(cursor.moveToNext())
             {
                 mMemoList.add(new MemoItem(cursor.getLong(0), 
                                            getTitle(cursor.getString(1), 
                                            cursor.getString(2)),
                                            cursor.getString(3),
                                            cursor.getInt(4)));
             }
         }
 
         mStatusView.setText(mMemoList.size() + getResources().getString(R.string.status_text));
 
         sortMemo();
         cursor.close();
     }
 
     public boolean onCreateOptionsMenu(Menu menu)
     {
         getSupportMenuInflater().inflate(R.menu.main_menu, menu);
         return true;
     }
 
     public boolean onOptionsItemSelected(MenuItem item)
     {
         Intent intent;
         switch(item.getItemId())
         {
             case R.id.menu_add:
                 intent = new Intent(this, WriteMemoActivity.class);
                 intent.putExtra("editMode", 0);
                 startActivityForResult(intent, INSERT_OK);
             break;
 
             case R.id.menu_delete_all_memo:
                 showDeleteAllDialog();
             break;
 
             case R.id.menu_sorting:
                 showSortDialog();
             break;
 
             case R.id.menu_setting:
                 intent = new Intent(this, SettingActivity.class);
                 startActivity(intent);
             break;
 
             case R.id.menu_bug_report:
                 mCollectLog = mSharedPref.getBoolean("log_collect", false);
                 if(mCollectLog)
                 {
                     String filePath = Util.writeLog(this);
                     intent = new Intent(Intent.ACTION_SEND);
                     intent.setType("text/plain");
                     intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.mail_title));
                     intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.mail_form));
                     intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filePath));
                     startActivity(intent);
                 }
                 else
                 {
                     intent = new Intent(Intent.ACTION_SEND);
                     intent.setType("text/plain");
                     intent.putExtra(Intent.EXTRA_SUBJECT, "[" + Util.getNowDateTime() + "] " + getResources().getString(R.string.mail_title));
                     intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.mail_form));
                     startActivity(intent);
                 }
             break;
         }
         return true;
     }
 
     public void onActivityResult(int requestCode, int resultCode, Intent intent)
     {
         super.onActivityResult(requestCode, resultCode, intent);
         switch(requestCode)
         {
             case INSERT_OK:
                 ;
             break;
         }
     }
 
     private void showNotifyCollectLogDialog()
     {
         AlertDialog.Builder alert = new AlertDialog.Builder(this);
         alert.setTitle(getResources().getString(R.string.notify_log));
         alert.setMessage(getResources().getString(R.string.notify));
 
         alert.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
         {
             public void onClick(DialogInterface dialog, int id)
             {
                 dialog.dismiss();
             }
         });
 
         alert.setNegativeButton(getResources().getString(R.string.setting), new DialogInterface.OnClickListener()
         {
             public void onClick(DialogInterface dialog, int id)
             {
                 startActivity(new Intent(MyMemoPadActivity.this, SettingActivity.class));
                 dialog.dismiss();
             }
         });
         alert.show();
     }
 
     public void onItemClick(AdapterView<?> parent, View v, int position, long id)
     {
         /*if (mSwipeDetector.swipeDetected())
         {
             mDbAdapter.deleteMemo(mMemoList.get(position).getMemoId());
             sendBroadcast(new Intent(DELETED_MEMO));
             loadData();
             mHandler.sendEmptyMessage(UPDATE_MESSAGE);
         }
         else
         {*/
             Intent i = new Intent(this, ViewMemoActivity.class);
             i.putExtra("memoid", mMemoList.get(position).getMemoId());
             startActivity(i);
         //}
     }
 
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
     {
         super.onCreateContextMenu(menu, v, menuInfo);
         menu.setHeaderTitle(getResources().getString(R.string.context_menu_title));
         menu.add(0, MENU_EDIT_ID, Menu.NONE, getResources().getString(R.string.menu_edit));
         menu.add(0, MENU_DELETE_ID, Menu.NONE, getResources().getString(R.string.menu_delete));
     }
 
     public boolean onContextItemSelected(android.view.MenuItem item)
     {
         super.onContextItemSelected(item);
         AdapterView.AdapterContextMenuInfo menuInfo;
         int index;
 
         switch(item.getItemId())
         {
             case MENU_EDIT_ID :
                 menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                 index = menuInfo.position;
                 Intent intent = new Intent(this, WriteMemoActivity.class);
                 intent.putExtra("editMode", 1);
                 intent.putExtra("memoid", mMemoList.get(index).getMemoId());
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
             return true;
 
             case MENU_DELETE_ID:
                 menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                 index = menuInfo.position;
                 mDbAdapter.deleteMemo(mMemoList.get(index).getMemoId());
                 sendBroadcast(new Intent(DELETED_MEMO));
                 loadData();
                 mHandler.sendEmptyMessage(UPDATE_MESSAGE);
             return true;
         }
         return false;
     }
 
     private void showDeleteAllDialog()
     {
         AlertDialog.Builder alert = new AlertDialog.Builder(this);
         alert.setTitle(getResources().getString(R.string.alert_delete_title));
         alert.setMessage(getResources().getString(R.string.alert_delete_all_message));
 
         alert.setPositiveButton(getResources().getString(R.string.alert_delete_confirm), new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id)
             {
                 mDbAdapter.deleteAllMemo();
                 sendBroadcast(new Intent(DELETED_ALL_MEMO));
                 dialog.dismiss();
                 loadData();
                 mHandler.sendEmptyMessage(DELETE_ALL_MESSAGE);
             }
         });
 
         alert.setNegativeButton(getResources().getString(R.string.alert_delete_cancel), new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id)
             {
                 dialog.dismiss();
             }
         });
 
         alert.show();
     }
 
     private void showSortDialog()
     {
         AlertDialog.Builder alert = new AlertDialog.Builder(this);
         alert.setTitle(getResources().getString(R.string.menu_sorting));
         alert.setSingleChoiceItems(R.array.compare, mSelectedCompareItem, new DialogInterface.OnClickListener()
         {
             public void onClick(DialogInterface dialog, int item)
             {
                 switch(item)
                 {
                     case 0:
                         // compare by date
                         DateComparator dateCompare = new DateComparator();
                         Collections.sort(mMemoList, dateCompare);
                     break;
 
                     case 1:
                         // compare by title
                         TitleComparator titleCompare = new TitleComparator();
                         Collections.sort(mMemoList, titleCompare);
                     break;
 
                     case 2:
                         // compare by important
                         ImportantComparator importantCompare = new ImportantComparator();
                         Collections.sort(mMemoList, importantCompare);
                     break;
                 }
 
                 mPrefEditor.putInt("compare_item", item);
                 mPrefEditor.commit();
                 mHandler.sendEmptyMessage(UPDATE_MESSAGE);
                 dialog.dismiss();
             }
         });
         alert.show();
     }
 
     private String getTitle(String dbTitle, String dbContent)
     {
         String title = dbTitle;
         if(title.equals("") && !dbContent.equals(""))
         {
             if(dbContent.length() > 7)
             {
                 title = dbContent.substring(0, 7) + "...";
             }
             else
             {
                 title = dbContent;
             }
         }
         else if(title.length() > 9)
         {
             title = title.substring(0, 10) + "...";
         }
 
         return title;
     }
 
     private void sortMemo()
     {
         mSelectedCompareItem = mSharedPref.getInt("compare_item", 0);
         if(mSelectedCompareItem == 0)
         {
             DateComparator dateComparator = new DateComparator();
             Collections.sort(mMemoList, dateComparator);
         }
         else if(mSelectedCompareItem == 1)
         {
             TitleComparator titleComparator = new TitleComparator();
             Collections.sort(mMemoList, titleComparator);
         }
         else if(mSelectedCompareItem == 2)
         {
             ImportantComparator importantComparator = new ImportantComparator();
             Collections.sort(mMemoList, importantComparator);
         }
     }
 }
