 
 package org.clc.android.app.redbox;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.android.actionbarcompat.ActionBarActivity;
 
 import org.clc.android.app.redbox.ad.AdvertisementManager;
 import org.clc.android.app.redbox.data.BlockSetting;
 import org.clc.android.app.redbox.data.BlockSettingsManager.OnBlockSettingChangeListener;
 import org.clc.android.app.redbox.data.DataManager;
 import org.clc.android.app.redbox.data.PatternSetting;
 import org.clc.android.app.redbox.data.PatternSettingsManager.OnPatternSettingChangeListener;
 import org.clc.android.app.redbox.widget.PhoneNumberEditWidget;
 
 import java.util.ArrayList;
 
 public class RedBoxActivity extends ActionBarActivity implements
         OnBlockSettingChangeListener, OnPatternSettingChangeListener {
     private static final String TAG = "RedBox";
 
     public static final String ID = "id";
 
     private ListView mNumbersListView = null;
     private NumbersListAdapter mAdapter = null;
     private LayoutInflater mLayoutInflater = null;
     private PhoneNumberEditWidget mPhoneNumberEditor = null;
 
     // for advertise.
     private View mAdView;
 
     private OnClickListener mNumberClickListener = new OnClickListener() {
         @Override
         public void onClick(View v) {
             final View parent = (View) v.getParent();
             final int id = (Integer) parent.getTag();
 
             Intent settingIntent = new Intent();
 
             BlockSetting setting = DataManager.getInstance().get(id);
             if (setting instanceof PatternSetting) {
                 settingIntent.setClass(RedBoxActivity.this,
                         RedBoxPatternSettingActivity.class);
             } else {
                 settingIntent.setClass(RedBoxActivity.this,
                         RedBoxBlockSettingActivity.class);
             }
             settingIntent.putExtra(ID, id);
 
             RedBoxActivity.this.startActivity(settingIntent);
 
         }
     };
 
     private OnCheckedChangeListener mRejectCallCheckChangeListener = new OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton view, boolean isChecked) {
             final View parent = (View) view.getParent();
             final int id = (Integer) parent.getTag();
             DataManager.getInstance().updateRejectCall(id, isChecked);
         }
     };
 
     private OnCheckedChangeListener mRemoveCallLogCheckChangeListener = new OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton view, boolean isChecked) {
             final View parent = (View) view.getParent();
             final int id = (Integer) parent.getTag();
             DataManager.getInstance().updateDeleteCallLog(id, isChecked);
         }
     };
 
     private OnCheckedChangeListener mSendAutoSMSCheckChangeListener = new OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton view, boolean isChecked) {
             final View parent = (View) view.getParent();
             final int id = (Integer) parent.getTag();
             BlockSetting setting = DataManager.getInstance().get(id);
             DataManager.getInstance().updateSendAutoSMS(id, isChecked);
         }
     };
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.phone_number_insert_list);
 
         mNumbersListView = (ListView) findViewById(R.id.numbersList);
 
         mAdapter = new NumbersListAdapter();
         mNumbersListView.setAdapter(mAdapter);
 
         mPhoneNumberEditor = (PhoneNumberEditWidget) findViewById(R.id.number_input_textView);
 
         Context context = getApplicationContext();
         context.startService(new Intent(context, RedBoxService.class));
 
         DataManager.getInstance().setOnBlockSettingChangeListener(this);
         DataManager.getInstance().setOnPatternSettingChangeListener(this);
 
         mAdView = AdvertisementManager.getAdvertisementView(this);
         LinearLayout adLayout = (LinearLayout) findViewById(R.id.advertiseLayout);
         adLayout.addView(mAdView);
     }
 
     public void onDestroy() {
         AdvertisementManager.destroyAd(mAdView);
         super.onDestroy();
     }
 
     @Override
     public void onPause() {
         super.onPause();
         DataManager.getInstance().saveSettings();
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         switch (requestCode) {
             case PhoneNumberEditWidget.PICK_CONTACT_REQUEST:
                 mPhoneNumberEditor.onContactActivityResult(resultCode, data);
                 break;
             default:
                 break;
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater menuInflater = getMenuInflater();
         menuInflater.inflate(R.menu.redbox_activity_menu, menu);
 
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_action_log:
                 startActivity(new Intent(this, RedBoxHistoryActivity.class));
         }
         return super.onOptionsItemSelected(item);
     }
 
     private void addNumber(String alias, String number, boolean notify) {
         if ("".equals(number)) {
             if (notify) {
                 Toast.makeText(this, R.string.error_blank_number,
                         Toast.LENGTH_SHORT).show();
             }
             return;
         }
         final String parsedNumber = DataManager.getParsedNumber(number);
         if (DataManager.getInstance().isExist(parsedNumber)) {
             if (notify) {
                 Toast.makeText(this, R.string.error_duplicate_number,
                         Toast.LENGTH_SHORT).show();
             }
             return;
         }
         BlockSetting setting = new BlockSetting(alias, number);
         DataManager.getInstance().add(setting);
         mAdapter.notifyDataSetChanged();
     }
 
     public void onAddNumberClicked(View v) {
         final ArrayList<BlockSetting> settings = mPhoneNumberEditor
                 .getBlockSettings();
         for (BlockSetting setting : settings) {
             if (!DataManager.isValid(setting.mNumber)) {
                 Toast.makeText(this, R.string.error_wrong_number,
                         Toast.LENGTH_SHORT).show();
             }
             addNumber(setting.mAlias, setting.mNumber, true);
         }
         mPhoneNumberEditor.setText("");
     }
 
     @Override
     public void onBlockSettingsChanged() {
         mAdapter.notifyDataSetChanged();
     }
 
     @Override
     public void onPatternSettingsChanged() {
         mAdapter.notifyDataSetChanged();
     }
 
     private class NumbersListAdapter extends BaseAdapter {
         NumbersListAdapter() {
             super();
             mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         }
 
         @Override
         public int getCount() {
             return DataManager.getInstance().getSize();
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             View numberList;
             if (convertView == null) {
                 numberList = mLayoutInflater.inflate(R.layout.number_list,
                         parent, false);
                 View trashButton = numberList.findViewById(R.id.delete_button);
                 trashButton.setVisibility(View.GONE);
             } else {
                 numberList = convertView;
             }
             numberList.setTag((Integer) position);
             TextView alias = (TextView) numberList
                     .findViewById(R.id.alias_textview);
             TextView number = (TextView) numberList
                     .findViewById(R.id.number_textview);
             CheckBox rejectCallCheckBox = (CheckBox) numberList
                     .findViewById(R.id.reject_call_checkBox);
             CheckBox removeCallLogCheckBox = (CheckBox) numberList
                     .findViewById(R.id.remove_call_log_checkBox);
             CheckBox sendAutoSMSCheckBox = (CheckBox) numberList
                     .findViewById(R.id.send_auto_sms_checkBox);
 
             View aliasNumberLayout = numberList
                     .findViewById(R.id.aliasNumberLayout);
             aliasNumberLayout.setOnClickListener(mNumberClickListener);
 
             BlockSetting setting = DataManager.getInstance().get(position);
             if (setting instanceof PatternSetting) {
                alias.setText(setting.mAlias);
                 number.setText("");
             } else if (setting.mAlias.equals("")) {
                 alias.setText(setting.mNumber);
                 number.setText("");
             } else {
                 alias.setText(setting.mAlias);
                 number.setText(setting.mNumber);
             }
             rejectCallCheckBox.setChecked(setting.mRejectCall);
             removeCallLogCheckBox.setChecked(setting.mDeleteCallLog);
             sendAutoSMSCheckBox.setChecked(setting.mSendAutoSMS);
 
             if (convertView == null) {
                 rejectCallCheckBox
                         .setOnCheckedChangeListener(mRejectCallCheckChangeListener);
                 removeCallLogCheckBox
                         .setOnCheckedChangeListener(mRemoveCallLogCheckChangeListener);
                 sendAutoSMSCheckBox
                         .setOnCheckedChangeListener(mSendAutoSMSCheckChangeListener);
             }
             return numberList;
         }
 
         @Override
         public Object getItem(int position) {
             return DataManager.getInstance().get(position);
         }
 
         @Override
         public long getItemId(int position) {
             return position;
         }
 
         @Override
         public void notifyDataSetChanged() {
             super.notifyDataSetChanged();
         }
     }
 }
