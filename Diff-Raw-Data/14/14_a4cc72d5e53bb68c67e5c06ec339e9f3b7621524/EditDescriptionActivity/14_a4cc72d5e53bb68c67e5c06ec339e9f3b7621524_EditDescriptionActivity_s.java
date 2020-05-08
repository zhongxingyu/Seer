 package com.xianglanqi.angrygirl;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.xianglanqi.angrygirl.data.CalendarDB;
 import com.xianglanqi.angrygirl.model.CalendarCell;
 
 public class EditDescriptionActivity extends Activity {
 
     private Button cancelButton;
 
     private Button saveButton;
 
     private TextView editTitleTextView;
 
     private TextView moodTextView;
 
     private EditText dayDescriptionEditText;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_edit);
 
         final CalendarCell cell = (CalendarCell) getIntent().getSerializableExtra("cell");
 
         this.cancelButton = (Button) findViewById(R.id.button_cancel);
         this.cancelButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View arg0) {
                 finish();
             }
         });
 
         this.saveButton = (Button) findViewById(R.id.button_save);
         this.saveButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 String description = EditDescriptionActivity.this.dayDescriptionEditText.getText().toString();
                 if (TextUtils.isEmpty(description)) {
                     return;
                 }
                 cell.setDescription(description);
                 new CalendarDB(EditDescriptionActivity.this).changeMood(cell);
                 finish();
             }
         });
 
         this.editTitleTextView = (TextView) findViewById(R.id.textview_day_title);
         this.editTitleTextView
                 .setText(String.format("%d年%d月%d日: ", cell.getYear(), cell.getMonth() + 1, cell.getDay()));
         this.moodTextView = (TextView) findViewById(R.id.imageview_day_mood);
         this.moodTextView.setBackgroundResource(cell.getMood().getResource());
 
         this.dayDescriptionEditText = (EditText) findViewById(R.id.edittext_day_description);
         this.dayDescriptionEditText.setText(cell.getDescription());
     }
 }
