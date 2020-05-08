 package com.livejournal.karino2.guitarscorevisualizer;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.app.Activity;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 import java.util.Date;
 
 public class EditActivity extends Activity {
     public static final String ARG_ITEM_ID = "item_id";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_edit);
 
         getActionBar().setDisplayHomeAsUpEnabled(true);
 
 
         long id = getIntent().getLongExtra(ARG_ITEM_ID, -1);
         if(id == -1) {
             score = new Score(new Date(), "", "");
         } else {
             score = Database.getInstance(this).getScoreById(id);
             EditText etTitle = (EditText)findViewById(R.id.editTextTitle);
             EditText etScore = (EditText)findViewById(R.id.editTextScore);
             etTitle.setText(score.getTitle());
             etScore.setText(score.getEncodedTexts());
 
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.edit, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     Score score;
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()) {
            case R.id.action_save:
                 saveScoreIfNecessary();
                 setResult(Activity.RESULT_OK);
                 finish();
                 return true;
             case android.R.id.home:
                 // This ID represents the Home or Up button. In the case of this
                 // activity, the Up button is shown. Use NavUtils to allow users
                 // to navigate up one level in the application structure. For
                 // more details, see the Navigation pattern on Android Design:
                 //
                 // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                 //
                 NavUtils.navigateUpTo(this, new Intent(this, ScoreListActivity.class));
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     private void saveScoreIfNecessary() {
         EditText etTitle = (EditText)findViewById(R.id.editTextTitle);
         EditText etScore = (EditText)findViewById(R.id.editTextScore);
 
         score.setTitle(etTitle.getText().toString());
         score.setTextsString(etScore.getText().toString());
         score.setModifiedAt(new Date());
         score.setChords(null);
 
         Database.getInstance(this).saveScore(score);
     }
 
 
 }
