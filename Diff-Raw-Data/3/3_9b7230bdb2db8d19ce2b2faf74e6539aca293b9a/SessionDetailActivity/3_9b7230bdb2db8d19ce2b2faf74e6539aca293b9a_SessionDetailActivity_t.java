 package com.hasgeek.activity;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.text.Html;
import android.text.method.LinkMovementMethod;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 import com.hasgeek.R;
 import com.hasgeek.misc.EventSession;
 
 
 public class SessionDetailActivity extends Activity {
 
     private final int MENU_SUBMIT_FEEDBACK = 4201;
     private final int MENU_BOOKMARK = 4202;
     private EventSession mSession;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         mSession = (EventSession) getIntent().getSerializableExtra("session");
 
         getActionBar().setTitle(mSession.getTitle());
         getActionBar().setSubtitle(mSession.getSpeaker());
 
         setContentView(R.layout.activity_sessiondetail);
 
         TextView title = (TextView) findViewById(R.id.tv_sd_title);
         title.setText(mSession.getTitle());
         TextView speaker = (TextView) findViewById(R.id.tv_sd_speaker);
         speaker.setText(mSession.getSpeaker());
         TextView section = (TextView) findViewById(R.id.tv_sd_section);
         section.setText(mSession.getSection());
         TextView level = (TextView) findViewById(R.id.tv_sd_level);
         level.setText(mSession.getLevel());
         TextView description = (TextView) findViewById(R.id.tv_sd_description);
         description.setText(Html.fromHtml(mSession.getDescription()));
        description.setMovementMethod(LinkMovementMethod.getInstance());
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add(0, MENU_SUBMIT_FEEDBACK, 0, R.string.menu_submit_feedback)
                 .setIcon(R.drawable.ic_ab_content_edit)
                 .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
 
         menu.add(0, MENU_BOOKMARK, 0, R.string.menu_bookmark)
                 .setIcon(R.drawable.ic_ab_rating_favorite)
                 .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
 
         return true;
     }
 
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case MENU_SUBMIT_FEEDBACK:
                 return true;
 
             case MENU_BOOKMARK:
                 return true;
 
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 }
