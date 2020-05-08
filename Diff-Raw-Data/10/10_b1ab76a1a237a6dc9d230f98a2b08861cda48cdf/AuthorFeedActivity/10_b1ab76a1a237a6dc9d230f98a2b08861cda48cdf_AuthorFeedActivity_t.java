 package ru.rutube.RutubeApp.ui;
 
 import ru.rutube.RutubeApp.MainApplication;
 import ru.rutube.RutubeApp.R;
 import ru.rutube.RutubeFeed.ui.FeedActivity;
 
 /**
  * Created by tumbler on 18.08.13.
  */
 public class AuthorFeedActivity extends FeedActivity {
     @Override
     public void setContentView(int layoutResID) {
         super.setContentView(R.layout.feed_activity);
     }
 
 
     @Override
     protected void onStart() {
        try {
            super.onStart();
            MainApplication.feedActivityStart(this, String.valueOf(getIntent().getData()));
        } catch (NullPointerException e) {
            ((MainApplication)MainApplication.getInstance()).reportError(this,
                    String.format("NullPointerException: %s", String.valueOf(e),
                            String.valueOf(getIntent())));
        }
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         MainApplication.activityStop(this);
     }
 
 }
