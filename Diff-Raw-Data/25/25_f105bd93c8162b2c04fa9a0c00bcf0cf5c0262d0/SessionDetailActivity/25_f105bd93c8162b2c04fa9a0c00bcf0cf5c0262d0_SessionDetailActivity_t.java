 package com.hasgeek.funnel.activity;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.text.Html;
 import android.text.method.LinkMovementMethod;
 import android.view.MenuItem;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.hasgeek.funnel.R;
 import com.hasgeek.funnel.bus.BusProvider;
 import com.hasgeek.funnel.bus.SessionFeedbackAlreadySubmittedEvent;
 import com.hasgeek.funnel.bus.SessionFeedbackSubmittedEvent;
 import com.hasgeek.funnel.fragment.SubmitFeedbackFragment;
 import com.hasgeek.funnel.misc.DataProvider;
 import com.hasgeek.funnel.misc.EventSession;
 import com.squareup.otto.Subscribe;
 
 
 public class SessionDetailActivity extends Activity {
 
     private final Handler mHandler = new Handler(Looper.getMainLooper());
 
     private EventSession mSession;
     private static final int MENU_SUBMIT_FEEDBACK = 4201;
     private static final int MENU_BOOKMARK = 4202;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         mSession = (EventSession) getIntent().getSerializableExtra("session");
 
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setTitle(mSession.getSlotInIst24Hrs());
         getActionBar().setSubtitle(mSession.getRoomTitle());
 
         setContentView(R.layout.activity_sessiondetail);
 
 
         TextView title = (TextView) findViewById(R.id.tv_sd_title);
         title.setText(mSession.getTitle());
         TextView speaker = (TextView) findViewById(R.id.tv_sd_speaker);
         speaker.setText(mSession.getSpeaker());
 
         Typeface fontawesome = Typeface.createFromAsset(getAssets(), "fonts/FontAwesome.otf");
         TextView section = (TextView) findViewById(R.id.tv_sd_section);
         section.setText("\uf018  " + mSession.getSection());
         section.setTypeface(fontawesome);
         TextView level = (TextView) findViewById(R.id.tv_sd_level);
         level.setText("\uf0e4  " + mSession.getLevel());
         level.setTypeface(fontawesome);
 
         TextView description = (TextView) findViewById(R.id.tv_sd_description);
         description.setText(Html.fromHtml(mSession.getDescription()));
         description.setMovementMethod(LinkMovementMethod.getInstance());
     }
 
 
     @Override
     protected void onResume() {
         super.onResume();
         BusProvider.getInstance().register(this);
     }
 
 
     @Override
     protected void onPause() {
         super.onPause();
         BusProvider.getInstance().unregister(this);
     }
 
 
     @Subscribe
     public void feedbackSubmittedEvent(SessionFeedbackSubmittedEvent event) {
         if (event.hasMessage()) {
             final SessionFeedbackSubmittedEvent devent = event;
             mHandler.post(new Runnable() {
                 @Override
                 public void run() {
                     new AlertDialog.Builder(SessionDetailActivity.this)
                             .setMessage(devent.getMessage())
                             .setPositiveButton(android.R.string.ok, null)
                             .show();
                 }
             });
 
         } else {
             mHandler.post(new Runnable() {
                 @Override
                 public void run() {
                     Toast.makeText(SessionDetailActivity.this, R.string.message_feedback_received_thanks, Toast.LENGTH_LONG).show();
                 }
             });
         }
     }
 
 
     @Subscribe
     public void feedbackAlreadySubmittedEvent(SessionFeedbackAlreadySubmittedEvent event) {
         mHandler.post(new Runnable() {
             @Override
             public void run() {
                 Toast.makeText(SessionDetailActivity.this, R.string.message_feedback_already_submitted, Toast.LENGTH_LONG).show();
             }
         });
     }
 
 
     //todo Enable this when we're going ahead with bookmarking
 //    @Override
 //    public boolean onCreateOptionsMenu(Menu menu) {
 //        menu.add(0, MENU_SUBMIT_FEEDBACK, 0, R.string.menu_submit_feedback)
 //                .setIcon(R.drawable.ic_ab_feedback)
 //                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
 //
 //        if (mSession.isBookmarked()) {
 //            menu.add(0, MENU_BOOKMARK, 0, R.string.menu_remove_bookmark)
 //                    .setIcon(R.drawable.ic_rating_important)
 //                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
 //        } else {
 //            menu.add(0, MENU_BOOKMARK, 0, R.string.menu_save_bookmark)
 //                    .setIcon(R.drawable.ic_rating_not_important)
 //                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
 //        }
 //
 //        return true;
 //    }
 
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case MENU_SUBMIT_FEEDBACK:
                 SubmitFeedbackFragment sff = new SubmitFeedbackFragment();
                 Bundle b = new Bundle();
                 b.putString("url", mSession.getUrl());
                 sff.setArguments(b);
                 sff.show(getFragmentManager(), sff.getClass().getName());
                 return true;
 
             case MENU_BOOKMARK:
                 toggleSessionBookmark();
                 return true;
 
             case android.R.id.home:
                onBackPressed();
                 return true;
 
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
 
     private void toggleSessionBookmark() {
         ContentValues cv = new ContentValues();
 
         if (mSession.isBookmarked()) {
             cv.put("bookmarked", "false");
             getContentResolver().update(DataProvider.SESSION_URI, cv, "id is ?", new String[] { mSession.getId() });
             Toast.makeText(this, R.string.bookmark_removed, Toast.LENGTH_SHORT).show();
             mSession.setBookmarked(false);
 
         } else {
             cv.put("bookmarked", "true");
             getContentResolver().update(DataProvider.SESSION_URI, cv, "id is ?", new String[] { mSession.getId() });
             Toast.makeText(this, R.string.bookmark_saved, Toast.LENGTH_SHORT).show();
             mSession.setBookmarked(true);
         }
 
         invalidateOptionsMenu();
     }
 }
