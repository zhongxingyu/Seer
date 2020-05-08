 package com.newsrss.Feed;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.util.Log;
 import android.view.*;
 
 import android.os.Bundle;
 import com.nineoldandroids.animation.*;
 import com.facebook.Session;
 import android.webkit.WebView;
 import android.widget.*;
 import android.widget.Toast;
 
 
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 
 import android.view.ViewGroup.LayoutParams;
 
 
 import com.nineoldandroids.view.ViewHelper;
 import org.brickred.socialauth.android.DialogListener;
 import org.brickred.socialauth.android.SocialAuthAdapter;
 import org.brickred.socialauth.android.SocialAuthAdapter.Provider;
 import org.brickred.socialauth.android.SocialAuthError;
 
 /**
  * Created with IntelliJ IDEA.
  * User: BruSD
  * Date: 4/10/13
  * Time: 10:31 AM
  * To change this template use File | Settings | File Templates.
  */
 public class DetailsArticle extends shaerToSocial implements GestureDetector.OnGestureListener  {
     private GestureDetector gd;
 
     SocialAuthAdapter adapter;
 
     Article currentArticle = null;
     Article nextArticle = null;
     int positionArt;
     TextView titleArticle;
     TextView dateArticle;
     WebView descriptionArticle;
     TextView nextTitle;
     TextView nextDate;
     ImageView nextImage;
     LinearLayout layoutToAddSharePanel,mainArticleLayout,nextLayout;
     RelativeLayout descriptionAndNextPanel;
     LinearLayout.LayoutParams lParamsOfSharePanel;
     LayoutInflater inflaterSharePanel;
     ScrollView scrollView;
     int sharePanelHeight;
     AnimatorSet setSharePanelShow, setSharePanelHide;
 
 
     boolean sharePanelTopIsShow=false;
     ImageButton shareButtonTest,starButton,shareTwitter,shareFacebook,shareLinkedin,shareMail;
 
     public void onCreate(Bundle savedInstanceState) {
 
     super.onCreate(savedInstanceState);
     setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
     setContentView(R.layout.details_articl);
         getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
 
         layoutToAddSharePanel = (LinearLayout)findViewById(R.id.article_layoutToShare);
         nextLayout = (LinearLayout) findViewById(R.id.next_article);
         descriptionAndNextPanel = (RelativeLayout)findViewById(R.id.article_descriptionAndNext);
         lParamsOfSharePanel=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
         inflaterSharePanel = getLayoutInflater();
         layoutToAddSharePanel.addView(inflaterSharePanel.inflate(R.layout.share_panel_top, null), lParamsOfSharePanel);
         layoutToAddSharePanel.setVisibility(View.GONE);
         mainArticleLayout = (LinearLayout)findViewById(R.id.article_main_layout);
 
         scrollView = (ScrollView)findViewById(R.id.article_scroll);
         titleArticle = (TextView)findViewById(R.id.article_title);
         dateArticle = (TextView)findViewById(R.id.article_date);
         descriptionArticle = (WebView)findViewById(R.id.article_description);
         descriptionArticle.setBackgroundResource(R.drawable.bg_w);
         descriptionArticle.setBackgroundColor(0);
         descriptionArticle.setAlwaysDrawnWithCacheEnabled(true);
 
         nextTitle = (TextView)findViewById(R.id.next_a_title);
         nextDate = (TextView)findViewById(R.id.next_a_date);
         nextImage = (ImageView) findViewById(R.id.next_a_img);
 
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
 
         Intent startDetailArticle = getIntent();
         positionArt = startDetailArticle.getIntExtra("position", -1);
 
         ShowArticle();
         NextArticle();
 
         ImageButton backBtn = (ImageButton)findViewById(R.id.back_btn);
         backBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 //To change body of implemented methods use File | Settings | File Templates.
                 finish();
             }
         });
 
         shareButtonTest = (ImageButton)findViewById(R.id.article_share);
         starButton = (ImageButton)findViewById(R.id.article_fav);
         shareTwitter = (ImageButton) findViewById(R.id.share_panel_bottom_twitter);
         shareFacebook = (ImageButton) findViewById(R.id.share_panel_bottom_facebook);
         shareLinkedin = (ImageButton) findViewById(R.id.share_panel_bottom_linkedin);
         shareMail = (ImageButton) findViewById(R.id.share_panel_bottom_mail);
 
 
         ImageButton.OnClickListener ocTwitter = new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 onTwitterClick(currentArticle.getTitle()+" "+ currentArticle.getLink().toString());
             }
         };
 
         ImageButton.OnClickListener ocFacebook = new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 onClickbtnConnectFB(1, positionArt);
             }
         };
 
         ImageButton.OnClickListener ocLinkedIn = new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 RunLinkedIn(1, positionArt, DetailsArticle.this);
 
             }
         };
 
 
 
         ImageButton.OnClickListener ocMail = new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 MailSender.send(DetailsArticle.this, currentArticle);
             }
         };
 
         final AnimatorSet.AnimatorListener sharePanelShowListener  = new Animator.AnimatorListener() {
             @Override
             public void onAnimationStart(Animator animation) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public void onAnimationEnd(Animator animation) {
                 for (int i = 0; i < mainArticleLayout.getChildCount(); i++) {
                     View child = mainArticleLayout.getChildAt(i);
                     if ((i!=2)&&(i!=3))
                         ObjectAnimator.ofFloat(child, "alpha", 1, 0.5f ).setDuration(0).start();
                 }
                 ObjectAnimator.ofFloat(starButton, "alpha", 1, 0.5f ).setDuration(0).start();
                 ObjectAnimator.ofFloat(dateArticle, "alpha", 1, 0.5f ).setDuration(0).start();
                 nextLayout.setEnabled(false);
             }
 
             @Override
             public void onAnimationCancel(Animator animation) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public void onAnimationRepeat(Animator animation) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
         };
 
 
         final AnimatorSet.AnimatorListener sharePanelHideListener  = new Animator.AnimatorListener() {
             @Override
             public void onAnimationStart(Animator animation) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public void onAnimationEnd(Animator animation) {
                 for (int i = 0; i < mainArticleLayout.getChildCount(); i++) {
                     View child = mainArticleLayout.getChildAt(i);
                     ObjectAnimator.ofFloat(child, "alpha", 0.5f, 1f ).setDuration(0).start();
                     child.setEnabled(true);
                 }
                 ObjectAnimator.ofFloat(starButton, "alpha", 0.5f, 1f ).setDuration(0).start();
                 ObjectAnimator.ofFloat(dateArticle, "alpha", 0.5f, 1f ).setDuration(0).start();
                 ViewHelper.setAlpha(descriptionArticle, 1f);
                 ObjectAnimator.ofFloat(nextLayout, "alpha", 0.5f, 1f ).setDuration(0).start();
                 nextLayout.setEnabled(true);
 
                 layoutToAddSharePanel.setVisibility(View.GONE);
                 ObjectAnimator.ofFloat(descriptionAndNextPanel, "translationY", 0, 0).setDuration(0).start();
 
             }
 
             @Override
             public void onAnimationCancel(Animator animation) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public void onAnimationRepeat(Animator animation) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
         };
 
         ImageButton.OnClickListener ocShare = new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (!sharePanelTopIsShow)   {
                     layoutToAddSharePanel.setVisibility(View.VISIBLE);
                     //TODO size of layoutSharePanel
                     sharePanelHeight=layoutToAddSharePanel.getHeight();
                     setSharePanelShow = new AnimatorSet();
                     setSharePanelShow.playTogether(
                             ObjectAnimator.ofFloat(descriptionAndNextPanel, "translationY", -105, 0 ),
                             ObjectAnimator.ofFloat(layoutToAddSharePanel, "Y", -105, 0)
                     );
                     setSharePanelShow.addListener(sharePanelShowListener);
                     setSharePanelShow.setDuration(200).start();
                     sharePanelTopIsShow=true;
                 }
                 else  {
                     setSharePanelHide = new AnimatorSet();
                     setSharePanelHide.playTogether(
                             ObjectAnimator.ofFloat(descriptionAndNextPanel, "translationY", 0, -105),
                             ObjectAnimator.ofFloat(layoutToAddSharePanel, "Y", 0, -105)
                     );
                     setSharePanelHide.addListener(sharePanelHideListener);
                     setSharePanelHide.setDuration(200).start();
                     sharePanelTopIsShow=false;
                 }
 
             }
         };
 
         shareTwitter.setOnClickListener(ocTwitter);
         shareFacebook.setOnClickListener(ocFacebook);
         shareLinkedin.setOnClickListener(ocLinkedIn);
         shareMail.setOnClickListener(ocMail);
         shareButtonTest.setOnClickListener(ocShare);
 
 
         final ImageButton addToFav = (ImageButton)findViewById(R.id.article_fav);
         try {
             LocalDB.open(getApplicationContext());
         } catch (SQLException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         if (!LocalDB.isArticleFavotites(currentArticle.getGuid())){
             addToFav.setImageDrawable(getResources().getDrawable(R.drawable.articles_star_button));
         }   else {
             addToFav.setImageDrawable(getResources().getDrawable(R.drawable.articles_star_push_button));
 
         }
         LocalDB.close();
 
         addToFav.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 try {
                     LocalDB.open(getApplicationContext());
                 } catch (SQLException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
                 if (!LocalDB.isArticleFavotites(currentArticle.getGuid())){
                     LocalDB.addArticle(currentArticle);
                     addToFav.setImageDrawable(getResources().getDrawable(R.drawable.articles_star_push_button));
                     Toast toast = Toast.makeText(getApplicationContext(),"Article Added to Favorites " ,Toast.LENGTH_SHORT);
                     toast.show();
                 }else{
                     LocalDB.deleteArticle(currentArticle.getGuid());
                     addToFav.setImageDrawable(getResources().getDrawable(R.drawable.articles_star_button));
                    Toast toast = Toast.makeText(getApplicationContext(),"Article Removed from Favorites " ,Toast.LENGTH_SHORT);
                     toast.show();
                 }
                 LocalDB.close();
             }
         });
 
         //Double Tap
         gd = new GestureDetector(DetailsArticle.this);
 
         gd.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
             @Override
             public boolean onSingleTapConfirmed(MotionEvent e) {
                 return false;  //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public boolean onDoubleTap(MotionEvent e) {
                 if(sharePanelTopIsShow)
                 return  false;
 
                 final Dialog dialog = new Dialog(DetailsArticle.this);
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
                         RunLinkedIn(1, positionArt, DetailsArticle.this);
                     }
                 });
 
                 ImageButton mailShareTo = (ImageButton)dialog.findViewById(R.id.share_panel_bottom_mail);
                 mailShareTo.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         MailSender.send(DetailsArticle.this, currentArticle);
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
                 if(sharePanelTopIsShow)
                 return true;
                 return gd.onTouchEvent(event);  //To change body of implemented methods use File | Settings | File Templates.
             }
         });
 
 
     }
 
 
     public void ShowArticle(){
         currentArticle = DataStorage.getArticleList().get(positionArt);
         String dateArticleV;
         SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
         dateArticleV = sdf.format(currentArticle.getPubDate());
         titleArticle.setText(currentArticle.getTitle());
         dateArticle.setText(dateArticleV);
 
         String data = "<html><head>" +
                 "<style type=\"text/css\">" +
                 "body{color:#280016; margin:0 10px; font-family:Helvetica; font-size:15px; line-height:24px; }" +
                 //"ul{list-style-type:none; padding-left:1.5em; margin-top:-2.5em;margin-bottom:2em;} " +
                 //"ul li{margin-bottom:-2em;text-indent:5px; position:relative;top:0em;margin-top:-.15em;} " +
                 //"ul li:before{position:relative; margin-left:-.5em !important; font-size:2em; content:'\\2022'; color:#860945; left:-.15em  !important; top:1.1em;margin-top:2em;} " +
                 "img{border:1px ridge #777774;} " +
                 "p{margin:10px 0;} " +
                 "a{font-weight:bold;text-decoration:none; color:#860945;} " +
                 "ol{counter-reset:my-counter;} " +
                 "ol li:before{content:counter(my-counter); counter-increment(my-counter); color:#860945;}" +
                 "</style>"+
                 "</head><body>" + currentArticle.getDescription()+"</body></html>";
         descriptionArticle.loadDataWithBaseURL(null, data, "text/html", "utf-8", null);
         //descriptionArticle.loadData("<html><body>"+ currentArticle.getDescription()+"</body></html>", "text/html; charset=UTF-8", null);
         //descriptionArticle.loadData("<html><body style='margin:0;padding:0;background-color:#efeee9'> <style type='text/css'> body{color:#280016; margin:0 10px; font-family:Helvetica; font-size:15px; line-height:24px; } ul{list-style-type:none; padding-left:1.5em;} ul li{margin-bottom:1em;text-indent:5px;} ul li:before{margin-left:-.5em;  position:relative; font-size:2em; content:'\\2022'; color:#860945; left:-.15em; top:.2em;} img{border:1px ridge #777774;} p{margin:10px 0;} a{font-weight:bold;text-decoration:none; color:#860945;}ol{counter-reset:my-counter;} ol li:before{content:counter(my-counter); counter-increment(my-counter); color:#860945;}</style>"+ currentArticle.getDescription()+"</body></html>", "text/html; charset=UTF-8", null);
     }
     public void NextArticle(){
         if ( positionArt+1 != DataStorage.getArticleList().size()) {
             nextArticle = DataStorage.getArticleList().get(positionArt + 1);
         }
          if (nextArticle != null){
              nextTitle.setText(nextArticle.getTitle());
              String dateArticleV;
              SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
              dateArticleV = sdf.format(nextArticle.getPubDate());
              nextDate.setText(dateArticleV);
              if (nextArticle.getNewsImage() == null) {
                  nextImage.setImageDrawable(getResources().getDrawable(R.drawable.default_news_icon));
              }
              else {
                  nextImage.setImageDrawable(nextArticle.getNewsImage());
              }
          }
     }
     public void ShowNextArticle(final View view){
 
         /*
 
         try {
             Thread.sleep(2000,0);
         }
         catch (InterruptedException e) {}
         descriptionArticle.requestLayout();  */
         //descriptionArticle.setVisibility(View.GONE);
 
         positionArt = positionArt+1;
         //descriptionArticle.reload();
 
         NextArticle();
         ShowArticle();
 
         descriptionArticle.reload();
         ShowArticle();
 
         //descriptionArticle.setVisibility(View.VISIBLE);
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
