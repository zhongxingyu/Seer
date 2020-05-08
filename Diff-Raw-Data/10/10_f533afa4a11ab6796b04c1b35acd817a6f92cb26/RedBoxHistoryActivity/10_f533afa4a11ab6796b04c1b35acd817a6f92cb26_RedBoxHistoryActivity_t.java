 package org.clc.android.app.redbox;
 
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 
 import org.clc.android.app.redbox.data.ActionRecord;
 import org.clc.android.app.redbox.data.BlockSetting;
 import org.clc.android.app.redbox.data.DataManager;
 import org.clc.android.app.redbox.data.ActionHistoryManager.OnHistoryChangeListener;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 
 public class RedBoxHistoryActivity extends Activity implements
         OnHistoryChangeListener {
     private static final String TAG = "RedBox_history";
 
     private ListView mRecordsListView;
     private AdView mAdView;
     private RecordsListAdapter mAdapter;
 
     private LayoutInflater mLayoutInflater;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.history_activity_layout);
 
         mRecordsListView = (ListView) findViewById(R.id.recordsList);
 
         mAdapter = new RecordsListAdapter();
         mRecordsListView.setAdapter(mAdapter);
 
         DataManager.getInstance().setOnHistoryChangeListener(this);
 
         mAdView = new AdView(this, AdSize.BANNER, RedBoxActivity.AD_ID);
         LinearLayout mainLayout = (LinearLayout) findViewById(R.id.advertiseLayout);
         mainLayout.addView(mAdView);
         mAdView.loadAd(new AdRequest());
     }
 
     @Override
     public void onHistoryChanged() {
         mAdapter.notifyDataSetChanged();
     }
 
     private class RecordsListAdapter extends BaseAdapter {
         RecordsListAdapter() {
             super();
             mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         }
 
         @Override
         public int getCount() {
             return DataManager.getInstance().getHistorySize();
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             View recordList;
             if (convertView == null) {
                 recordList = mLayoutInflater.inflate(R.layout.record_list,
                         parent, false);
             } else {
                 recordList = convertView;
             }
 
             final ActionRecord record = DataManager.getInstance().getHistory(
                     position);
             final BlockSetting rule = record.mMatchedRule;
 
             final TextView from = (TextView) recordList
                     .findViewById(R.id.record_from_textView);
             final TextView when = (TextView) recordList
                     .findViewById(R.id.record_when_textView);
             final TextView matchedBy = (TextView) recordList
                     .findViewById(R.id.record_matched_by_textView);
             final TextView rejectedCall = (TextView) recordList
                     .findViewById(R.id.record_rejected_call_textView);
             final TextView deletedCallLog = (TextView) recordList
                     .findViewById(R.id.record_deleted_call_log_textView);
             final TextView sentAutoSMS = (TextView) recordList
                     .findViewById(R.id.record_sent_auto_sms_textView);
             final TextView autoSMS = (TextView) recordList
                     .findViewById(R.id.record_auto_sms_textView);
 
             from.setText(getString(R.string.record_from, rule.mAlias,
                     rule.mNumber));
 
             SimpleDateFormat format = new SimpleDateFormat(
                     "yyyy.mm.dd hh:mm:ss");
             Timestamp timeStamp = new Timestamp(record.mTimeStamp);
             when.setText(format.format(timeStamp));
 
             String ruleName = rule.mAlias;
             if (ruleName == null || "".equals(ruleName)) {
                 ruleName = rule.mNumber;
             }
             matchedBy
                     .setText(getString(R.string.record_matched_rule, ruleName));
 
             if (!rule.mRejectCall) {
                 rejectedCall.setVisibility(View.GONE);
            } else {
                rejectedCall.setVisibility(View.VISIBLE);
             }

             if (!rule.mDeleteCallLog) {
                 deletedCallLog.setVisibility(View.GONE);
            } else {
                deletedCallLog.setVisibility(View.VISIBLE);
             }
             if (!rule.mSendAutoSMS) {
                 sentAutoSMS.setVisibility(View.GONE);
                 autoSMS.setVisibility(View.GONE);
             } else {
                sentAutoSMS.setVisibility(View.VISIBLE);
                autoSMS.setVisibility(View.VISIBLE);
                 autoSMS.setText(rule.mAutoSMS);
             }
 
             recordList.setTag((Integer) position);
 
             return recordList;
         }
 
         @Override
         public Object getItem(int position) {
             return DataManager.getInstance().getHistory(position);
         }
 
         @Override
         public long getItemId(int position) {
             return position;
         }
     }
 }
