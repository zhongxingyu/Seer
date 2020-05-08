 
 package com.marakana.android.lognative;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioGroup;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class LogActivity extends Activity implements OnClickListener {
     private static final String TAG = "LogActivity";
 
     private static final int[] LOG_LEVEL = {
            Log.VERBOSE, Log.DEBUG, Log.INFO, Log.WARN, Log.ERROR
     };
 
     private Spinner priority;
 
     private EditText tag;
 
     private EditText msg;
 
     private Button button;
 
     private RadioGroup type;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         super.setContentView(R.layout.main);
         this.priority = (Spinner) super.findViewById(R.id.log_priority);
         this.tag = (EditText) super.findViewById(R.id.log_tag);
         this.msg = (EditText) super.findViewById(R.id.log_msg);
         this.type = (RadioGroup) super.findViewById(R.id.type);
         this.type.check(R.id.type_log_j);
         this.button = (Button) super.findViewById(R.id.log_button);
         this.button.setOnClickListener(this);
     }
 
     public void onClick(View v) {
         int priorityPosition = this.priority.getSelectedItemPosition();
         if (priorityPosition != AdapterView.INVALID_POSITION) {
             final int priority = LOG_LEVEL[priorityPosition];
             final String tag = this.tag.getText().toString();
             final String msg = this.msg.getText().toString();
             if (TextUtils.isEmpty(tag) || TextUtils.isEmpty(msg)) {
                 new AlertDialog.Builder(this)
                         .setMessage(R.string.log_tag_errors)
                         .setPositiveButton(android.R.string.yes,
                                 new DialogInterface.OnClickListener() {
                                     public void onClick(DialogInterface dialog, int id) {
                                         LogActivity.this.log(priority, tag, msg);
                                     }
                                 }).setNegativeButton(android.R.string.no, null).create().show();
             } else {
                 log(priority, tag, msg);
             }
         }
     }
 
     private void log(int priority, String tag, String msg) {
         try {
             switch (this.type.getCheckedRadioButtonId()) {
                 case R.id.type_log_j:
                     LogLib.logJ(priority, tag, msg);
                     break;
                 case R.id.type_log_n:
                     LogLib.logN(priority, tag, msg);
                     break;
                 default:
                     return;
             }
 //            this.tag.getText().clear();
 //            this.msg.getText().clear();
             Toast.makeText(this, R.string.log_success, Toast.LENGTH_SHORT).show();
         } catch (RuntimeException e) {
             Toast.makeText(this, R.string.log_error, Toast.LENGTH_SHORT).show();
             Log.wtf(TAG, "Failed to log the message", e);
         }
     }
 }
