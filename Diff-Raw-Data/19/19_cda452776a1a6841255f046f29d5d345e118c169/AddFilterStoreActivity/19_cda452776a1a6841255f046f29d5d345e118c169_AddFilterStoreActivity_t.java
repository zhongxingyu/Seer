 package com.sbar.rebudget.activities;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.sbar.rebudget.R;
 import com.sbar.rebudget.activities.AddFilterRemainingActivity;
 import com.sbar.rebudget.activities.AddOutcomeFilterActivity;
 import com.sbar.rebudget.activities.MainTabActivity;
 import com.sbar.rebudget.Common;
 import com.sbar.rebudget.FilterStruct;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class AddFilterStoreActivity extends Activity {
     static AddFilterStoreActivity instance = null;
     TextView m_smsText = null;
     TextView m_storeParseExample = null;
     String m_exampleStore = "??";
     Button m_nextButton = null;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         instance = this;
         setContentView(R.layout.add_filter_store);
 
         m_smsText = (TextView) findViewById(R.id.sms_text);
         if (AddOutcomeFilterActivity.instance != null) {
             String s = AddOutcomeFilterActivity
                             .instance.m_smsSelected.get("text");
             m_smsText.setText(s);
         }
 
         m_storeParseExample = (TextView) findViewById(R.id.store_parse_example);
 
         EditText smsStore = (EditText) findViewById(R.id.sms_store_regexp);
         smsStore.addTextChangedListener(new TextWatcher() {
             @Override
             public void afterTextChanged(Editable s) {
                 AddFilterStoreActivity.instance.setSmsStoreRegexp(s.toString());
                 AddFilterStoreActivity.instance.m_nextButton.setEnabled(m_exampleStore != "??");
             }
 
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 
             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count) {}
         });
 
         m_nextButton = (Button) findViewById(R.id.next);
         m_nextButton.setEnabled(false);
         m_nextButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View arg0) {
                 if (FilterStruct.s_instance == null ||
                     !MainTabActivity.s_dc.addFilter(FilterStruct.s_instance)) {
                     Common.LOGE("Failed to create a filter (instance==null)==" + (FilterStruct.s_instance == null));
                     Builder builder = new AlertDialog.Builder(AddFilterStoreActivity.this);
                     //builder.setView(v);
                     builder.setMessage("Failed to create a filter.");
                     builder.setCancelable(true);
                     builder.setPositiveButton(
                         "ok",
                         new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int id) {
                             }
                         }
                     );
                     builder.create().show();
                 } else {
                     FilterStruct.s_instance = null;
                    if (AddOutcomeFilterActivity.instance != null)
                        AddOutcomeFilterActivity.instance.finish();
                    else
                        AddIncomeFilterActivity.instance.finish();
                    AddFilterCostActivity.instance.finish();
                    AddFilterRemainingActivity.instance.finish();
                    AddFilterStoreActivity.instance.finish();
                     System.gc();
                 }
             }
         });
 
         updateStoreParseExample();
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         instance = null;
     }
 
     public void setSmsStoreRegexp(String regexp) {
         try {
             Pattern r = Pattern.compile(regexp);
             Matcher m = r.matcher(m_smsText.getText().toString());
             m_exampleStore = m.find() ? m.group(1) : "??";
         } catch (Throwable e) {
             m_exampleStore = "??";
         }
         updateStoreParseExample();
     }
 
     public void updateStoreParseExample() {
         String text = String.format("Store/service: %s", m_exampleStore);
         m_storeParseExample.setText(text);
     }
 }
