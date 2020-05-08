 package com.newsrss.Feed;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Toast;
 import com.facebook.*;
 import com.facebook.widget.WebDialog;
 
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Created with IntelliJ IDEA.
  * User: BruSD
  * Date: 03.05.13
  * Time: 16:25
  * To change this template use File | Settings | File Templates.
  */
 public class shareToSocial extends Activity {
 
     String postName;
     String postURL;
     String postImageURL;
 
     private Intent intent;
 
     private  Article currentArticle;
     private  Podcast currentPodcast;
     private Job    currentJob;
     public Session.StatusCallback statusCallback = new SessionStatusCallback();
 
 
 
 
    //listIDn = 1 Articl List
    //listIDn = 2 Podcast
    //listIDn = 3 jobs
     public void onClickbtnConnectFB( int listID,  int articleID) {
 
         onClickLogin();
         setDatetoShare(listID, articleID);
     }
     public void setDatetoShare(int list, int ID){
         switch (list){
             case 1:
                 currentArticle = DataStorage.getArticleList().get(ID);
 
                 //SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
                 //postName = sdf.format(currentArticle.getPubDate());
                 postName = currentArticle.getTitle();
                 postURL = currentArticle.getLink().toString();
                 //postImageURL = currentArticle.getNewsImage();
                 break;
             case 2:
                 currentPodcast = DataStorage.getPodcastList().get(ID);
 
                 //SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
                 //postName = sdf.format(currentArticle.getPubDate());
                 postName = currentPodcast.getTitle();
                 postURL = currentPodcast.getMP3Link().toString();
                 //postImageURL = currentArticle.getNewsImage();
                 break;
             case 3:
                 currentJob = DataStorage.getJobList().get(ID);
 
                 //SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
                 //postName = sdf.format(currentArticle.getPubDate());
                 postName = currentJob.getTitle();
                 postURL = currentJob.getLink().toString();
                 //postImageURL = currentArticle.getNewsImage();
                 break;
         }
 
 
 
     }
     @Override
     public void onStart() {
         super.onStart();
         if(Session.getActiveSession() != null)
             Session.getActiveSession().addCallback(statusCallback);
     }
 
     @Override
     public void onStop() {
         super.onStop();
         if(Session.getActiveSession() != null)
             Session.getActiveSession().removeCallback(statusCallback);
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if(Session.getActiveSession()!= null)
             Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         Session session = Session.getActiveSession();
         if(session != null)
             Session.saveSession(session, outState);
     }
 
     private void onClickLogin() {
         Session session = Session.getActiveSession();
         if (!session.isOpened() && !session.isClosed()) {
             Session.OpenRequest openRequest = new Session.OpenRequest(this).setCallback(statusCallback);
 
             openRequest.setLoginBehavior(SessionLoginBehavior.SUPPRESS_SSO);
             session.openForRead(openRequest);
         } else {
             Session.openActiveSession(this, true, statusCallback);
         }
 
     }
 
 
 
     public void updateView() {
         Session session = Session.getActiveSession();
         if (session.isOpened()) {
 
             publishFeedDialog();
             Toast toast = Toast.makeText(getApplicationContext(), "Вы вышли", Toast.LENGTH_SHORT);
             toast.show();
 
 
         } else {
             onClickLogin();
         }
     }
 
     private class SessionStatusCallback implements Session.StatusCallback {
         @Override
         public void call(Session session, SessionState state, Exception exception) {
             updateView();
         }
     }
     private void publishFeedDialog() {
         Bundle params = new Bundle();
         params.putString("name", postName);
         params.putString("caption", "Build great social apps and get more installs.");
         params.putString("description", "The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps.");
         params.putString("link", postURL);
         params.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");
 
         WebDialog feedDialog = (
                 new WebDialog.FeedDialogBuilder(this,
                         Session.getActiveSession(),
                         params))
                 .setOnCompleteListener(new WebDialog.OnCompleteListener() {
 
                     @Override
                     public void onComplete(Bundle values,
                                            FacebookException error) {
                         if (error == null) {
                             // When the story is posted, echo the success
                             // and the post Id.
                             final String postId = values.getString("post_id");
                             if (postId != null) {
                                 //   Toast.makeText(getApplicationContext(),
                                 //    "Posted story, id: "+postId,
                                 //    Toast.LENGTH_SHORT).show();
                             } else {
                                 // User clicked the Cancel button
                                 //Toast.makeText(getApplicationContext().getApplicationContext(),
                                 // "Publish cancelled",
                                 //   Toast.LENGTH_SHORT).show();
                             }
                         } else if (error instanceof FacebookOperationCanceledException) {
                             // User clicked the "x" button
                             // Toast.makeText(getApplicationContext().getApplicationContext(),
                             //  "Publish cancelled",
                             //     Toast.LENGTH_SHORT).show();
                         } else {
                             // Generic, ex: network error
                             //Toast.makeText(getApplicationContext().getApplicationContext(),
                             //   "Error posting story",
                             //   Toast.LENGTH_SHORT).show();
                         }
                     }
 
                 })
                 .build();
         feedDialog.show();
     }
 }
