 package com.viva.mypad;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.viva.mypad.Adapter.DBAdapter;
 import com.viva.mypad.Util.Util;
 
 public class WriteMemoActivity extends SherlockActivity implements OnClickListener
 {
     private static final String ADDED_MEMO = "com.viva.mypad.ADDED_MEMO";
 
     private ActionBar mActionBar;
     private String mNow;
     private TextView mDateView;
     private EditText mEditTitle, mEditContent;
     private Button mImportantButton;
     private DBAdapter mDbAdapter;
     private int mIsEditMode;
     private long mMemoId;
     private boolean mIsImportant;
     private int mImportantNumber;
     private SharedPreferences mSharedPref;
     private boolean mCollectLog;
 
     public void onCreate(Bundle saveInstanceState)
     {
         super.onCreate(saveInstanceState);
         setContentView(R.layout.write_memo);
 
         mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
         mDbAdapter = new DBAdapter(this);
         mDbAdapter.open();
 
         mIsImportant = false;
         mImportantNumber = 0;
 
         mActionBar = getSupportActionBar();
         mActionBar.setTitle(getResources().getString(R.string.hint_memo));
         mActionBar.setDisplayHomeAsUpEnabled(true);
         mActionBar.setHomeButtonEnabled(true);
 
         mDateView = (TextView)findViewById(R.id.dateView);
         mNow = Util.getNowDateTime();
         mDateView.setText(mNow);
 
         mEditTitle = (EditText)findViewById(R.id.editTitle);
         mEditContent = (EditText)findViewById(R.id.editContent);
         mEditContent.setSelection(0);
 
         mImportantButton = (Button)findViewById(R.id.importantButton);
         mImportantButton.setOnClickListener(this);
 
         mIsEditMode = getIntent().getIntExtra("editMode", 0);
         if(mIsEditMode == 1)
         {
             // edit mode
             mActionBar.setTitle(getResources().getString(R.string.edit_label));
             mEditContent.setSelection(mEditContent.length());
             mMemoId = getIntent().getLongExtra("memoid", 0);
             Cursor cursor = mDbAdapter.getMemo(mMemoId);
             mEditTitle.setText(cursor.getString(1));
             mEditContent.setText(cursor.getString(2));
             if(cursor.getInt(4) == 1)
             {
                 mIsImportant = true;
                 mImportantNumber = 1;
                 mImportantButton.setBackgroundResource(R.drawable.ics_rate_important);
             }
             cursor.close();
         }
     }
 
     public void onDestroy()
     {
         super.onDestroy();
         mDbAdapter.close();
     }
 
     public boolean onCreateOptionsMenu(Menu menu)
     {
         getSupportMenuInflater().inflate(R.menu.write_menu, menu);
         return true;
     }
 
     public boolean onOptionsItemSelected(MenuItem item)
     {
         Intent intent = null;
         switch(item.getItemId())
         {
             case android.R.id.home:
                 intent = new Intent(this, MyMemoPadActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
                 this.finish();
             break;
 
             case R.id.menu_save:
                 if(checkIsEmpty())
                 {
                     Toast.makeText(this, getResources().getString(R.string.warning), Toast.LENGTH_SHORT).show();
                 }
                 else
                 {
                     saveMemo();
                     Toast.makeText(this, getResources().getString(R.string.saved), Toast.LENGTH_SHORT).show();
                     sendBroadcast(new Intent(ADDED_MEMO));
                     intent = new Intent(this, ViewMemoActivity.class);
                     intent.putExtra("memoid", mMemoId);
                     startActivity(intent);
                     this.finish();
                 }
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
 
     private void saveMemo()
     {
         if(mIsEditMode == 1)
         {
             mDbAdapter.updateMemo(mMemoId, mEditTitle.getText().toString(), mEditContent.getText().toString(), mImportantNumber);
         }
         else
         {
             mMemoId = mDbAdapter.insertMemo(mEditTitle.getText().toString(), mEditContent.getText().toString(), mNow, mImportantNumber);
         }
 
         //this.setResult(RESULT_OK, null);
         mDbAdapter.close();
     }
 
     private boolean checkIsEmpty()
     {
         boolean isEmpty = false;
         if(mEditTitle.getText().toString().matches("") && mEditContent.getText().toString().matches(""))
         {
             isEmpty = true;
         }
         return isEmpty;
     }
 
     public void onClick(View v)
     {
         if(mIsImportant)
         {
             mIsImportant = false;
             mImportantNumber = 0;
             v.setBackgroundResource(R.drawable.ics_not_important);
         }
         else
         {
             mIsImportant = true;
             mImportantNumber = 1;
             v.setBackgroundResource(R.drawable.ics_rate_important);
         }
     }
 }
