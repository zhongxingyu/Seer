 package com.livejournal.karino2.guitarscorevisualizer;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 import java.util.Date;
 
 public class EditActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_edit);
 
         ((Button)findViewById(R.id.buttonSave)).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 saveScoreIfNecessary();
             }
         });
         ((Button)findViewById(R.id.buttonCancel)).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 finish();
             }
         });
     }
 
     private void saveScoreIfNecessary() {
         EditText etTitle = (EditText)findViewById(R.id.editTextTitle);
         EditText etScore = (EditText)findViewById(R.id.editTextScore);
 
         Score score = new Score(new Date(), etTitle.getText().toString(), etScore.getText().toString());
         Database.getInstance(this).insertScore(score);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.edit, menu);
         return true;
     }
     
 }
