 package com.newsrss.Feed;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.view.*;
 import android.webkit.WebView;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.facebook.Session;
 
 import java.text.SimpleDateFormat;
 
 /**
  * Created with IntelliJ IDEA.
  * User: BruSD
  * Date: 16.04.13
  * Time: 10:17
  * To change this template use File | Settings | File Templates.
  */
 public class DetailsJobs extends shareToSocial implements GestureDetector.OnGestureListener {
     private GestureDetector gd;
 
     Job currentArticle = null;
 
     int positionArt;
     TextView titleArticle;
     TextView dateArticle;
     WebView descriptionArticle;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         setContentView(R.layout.details_jobs);
             getWindow().setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
 
         titleArticle = (TextView)findViewById(R.id.jobs_title);
         dateArticle = (TextView)findViewById(R.id.jobs_date);
         descriptionArticle = (WebView)findViewById(R.id.jobs_description);
         descriptionArticle.setBackgroundResource(R.drawable.bg_w);
 
         descriptionArticle.setBackgroundColor(0);
 
 
 
         Intent startDetailArticle = getIntent();
         positionArt = startDetailArticle.getIntExtra("position", -1);
 
         currentArticle = DataStorage.getJobList().get(positionArt);
         ShowArticle();
 
         ImageButton backBtn = (ImageButton)findViewById(R.id.back_btn);
         backBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 //To change body of implemented methods use File | Settings | File Templates.
                 finish();
             }
         });
         //FB
         Session session = Session.getActiveSession();
         if (session == null) {
             if (savedInstanceState != null) {
                 session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
             }
             if (session == null) {
                 session = new Session(this);
             }
             Session.setActiveSession(session);
 
         }
 
 
 
         //Double Tap
         gd = new GestureDetector(DetailsJobs.this);
 
         gd.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
             @Override
             public boolean onSingleTapConfirmed(MotionEvent e) {
 
                 return false;  //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public boolean onDoubleTap(MotionEvent e) {
                 Toast toast = Toast.makeText(getApplicationContext(),"Double Tap",Toast.LENGTH_SHORT);
                 toast.show();
                 final Dialog dialog = new Dialog(DetailsJobs.this);
                 dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                 dialog.getWindow().setGravity(Gravity.BOTTOM);
                 dialog.setContentView(R.layout.share_panel_bottom);
 
                 ImageButton facebokShareTo = (ImageButton)dialog.findViewById(R.id.share_panel_bottom_facebook);
                 facebokShareTo.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         onClickbtnConnectFB(1, positionArt);
                     }
                 });
 
                 ImageButton twitterShareTo = (ImageButton)dialog.findViewById(R.id.share_panel_bottom_twitter);
                 twitterShareTo.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         onTwitterClick(currentArticle.getTitle()+" "+ currentArticle.getLink().toString());
                     }
                 });
 
                 ImageButton linkedinShareTo = (ImageButton)dialog.findViewById(R.id.share_panel_bottom_linkedin);
                 linkedinShareTo.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
 
                     }
                 });
 
                 ImageButton mailShareTo = (ImageButton)dialog.findViewById(R.id.share_panel_bottom_mail);
                 mailShareTo.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         MailSender.send(DetailsJobs.this, currentArticle);
                     }
                 });
 
                 dialog.show();
                 return false;  //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public boolean onDoubleTapEvent(MotionEvent e) {
 
                 return false;  //To change body of implemented methods use File | Settings | File Templates.
             }
         });
 
         descriptionArticle.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 return gd.onTouchEvent(event);  //To change body of implemented methods use File | Settings | File Templates.
             }
         });
 
     }
     public void ShowArticle(){
         String dateArticleV;
         SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
         dateArticleV = sdf.format(currentArticle.getPubDate());
         titleArticle.setText(currentArticle.getTitle());
         dateArticle.setText(dateArticleV);
        descriptionArticle.loadData("<html><body>" + currentArticle.getDescription() + "</body></html>", "text/html; charset=UTF-8", null);
     }
     @Override
     public boolean onTouchEvent(MotionEvent event)
     {
         return gd.onTouchEvent(event);//return the double tap events
     }
 
     @Override
     public boolean onDown(MotionEvent e) {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void onShowPress(MotionEvent e) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean onSingleTapUp(MotionEvent e) {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void onLongPress(MotionEvent e) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
 }
