 package com.newsrss.Feed;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.ImageButton;
 import android.widget.Toast;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.facebook.*;
 import com.facebook.widget.WebDialog;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.EnumSet;
 
 import com.newsrss.Feed.TwitterApp.TwDialogListener;
 import android.app.AlertDialog;
 import oauth.signpost.OAuth;
 import oauth.signpost.OAuthConsumer;
 import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.brickred.socialauth.android.DialogListener;
 import org.brickred.socialauth.android.SocialAuthAdapter;
 import org.brickred.socialauth.android.SocialAuthError;
 
 /**
  * Created with IntelliJ IDEA.
  * User: BruSD
  * Date: 03.05.13
  * Time: 16:25
  * To change this template use File | Settings | File Templates.
  */
 public class shaerToSocial extends SherlockActivity {
 
 
 
 
     //Twitter Variable
     private TwitterApp mTwitter;
     //private CheckBox mTwitterBtn;
 
     private static final String twitter_consumer_key = "rLdUmSbLw38ZPujnbGZM8g";
     private static final String twitter_secret_key = "oVLV1wvDmBimSf69iWs9dUtGmPJOY0VNMTSCBRzH3E";
 
 
 
 
     //FB Variable
     String postName;
     String postURL;
     String postImageURL;
     String postDiscription;
 
 
 
     private  Article currentArticle;
     private  Podcast currentPodcast;
     private Job    currentJob;
     public Session.StatusCallback statusCallback = new SessionStatusCallback();
 
 
     //LinkedIn
 
     SocialAuthAdapter adapter;
 
 
     //LinkedIn
     public void RunLinkedIn(int listID,  int articleID,Context ctx ){
         setDatetoShare(listID, articleID);
 
         adapter = new SocialAuthAdapter(new ResponseListener());
 
         adapter.authorize(ctx, SocialAuthAdapter.Provider.LINKEDIN);
         adapter.updateStatus(postName + postURL);
         Toast.makeText(ctx, "Message posted on LinkedIn" , Toast.LENGTH_LONG).show();
     }
     final class ResponseListener implements DialogListener {
         @Override
         public void onComplete(Bundle values) {
 
             // Variable to receive message status
             Log.d("Share-Bar", "Authentication Successful");
 
             // Get name of provider after authentication
             final String providerName = values.getString(SocialAuthAdapter.PROVIDER);
             Log.d("Share-Bar", "Provider Name = " + providerName);
             //Toast.makeText(this, providerName + " connected", Toast.LENGTH_SHORT).show();
 
 
 
 
             // Please avoid sending duplicate message. Social Media Providers
             // block duplicate messages.
 
                     //adapter.updateStatus(postName + postURL);
                     //Toast.makeText(ctx, "Message posted on " + providerName, Toast.LENGTH_LONG).show();
 
         }
 
         @Override
         public void onError(SocialAuthError error) {
             error.printStackTrace();
             Log.d("Share-Bar", error.getMessage());
         }
 
         @Override
         public void onCancel() {
             Log.d("Share-Bar", "Authentication Cancelled");
         }
 
         @Override
         public void onBack() {
             Log.d("Share-Bar", "Dialog Closed by pressing Back Key");
 
         }
     }
 
 
     //FB
 
     //listIDn = 1 Articl List
     //listIDn = 2 Podcast
     //listIDn = 3 jobs
 
     public void onClickbtnConnectFB( int listID,  int articleID) {
 
         onClickLogin();
         setDatetoShare(listID, articleID);
     }
 
     public void setDatetoShare(int list, int ID) {
         switch (list){
             case 1:
                 currentArticle = DataStorage.getArticleList().get(ID);
 
                 //SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
                 //postName = sdf.format(currentArticle.getPubDate());
                 postName = currentArticle.getTitle();
                 postURL = currentArticle.getLink().toString();
                 postDiscription = currentArticle.getDescription();
                 //postImageURL = currentArticle.getNewsImage();
                 break;
             case 2:
                 currentPodcast = DataStorage.getPodcastList().get(ID);
 
                 //SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
                 //postName = sdf.format(currentArticle.getPubDate());
                 postName = currentPodcast.getTitle();
                 postURL = currentPodcast.getMP3Link().toString();
                 postDiscription = currentPodcast.getDescription();
                 //postImageURL = currentArticle.getNewsImage();
                 break;
             case 3:
                 currentJob = DataStorage.getJobList().get(ID);
 
                 //SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
                 //postName = sdf.format(currentArticle.getPubDate());
                 postName = currentJob.getTitle();
                 postURL = currentJob.getLink().toString();
                 postDiscription= currentJob.getDescription();
                 //postImageURL = currentArticle.getNewsImage();
                 break;
 
             case 4:
                     try {
                         LocalDB.open(getApplicationContext());
                     } catch (SQLException e) {
                         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                 currentArticle = LocalDB.getArticle(ID);
 
                 //SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
                 //postName = sdf.format(currentArticle.getPubDate());
                 postName = currentArticle.getTitle();
                 postURL = currentArticle.getLink().toString();
                 postDiscription= currentArticle.getDescription();
                 //postImageURL = currentArticle.getNewsImage();
                 break;
 
             case 5:
                 try {
                     LocalDB.open(getApplicationContext());
                 } catch (SQLException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
                 currentArticle = LocalDB.getArticle(ID);
 
                 //SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
                 //postName = sdf.format(currentArticle.getPubDate());
                 postName = currentArticle.getTitle();
                 postURL = currentArticle.getLink().toString();
                 postDiscription= currentArticle.getDescription();
                 //postImageURL = currentArticle.getNewsImage();
                 break;
 
         }
         //:TODO get Fav Article & Get Searched Article to shaer in soc
 
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
         //params.putString("caption", "Build great social apps and get more installs.");
         //params.putString("description", "The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps.");
         params.putString("link", postURL);
         //params.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");
 
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
                                    Toast.makeText(getApplicationContext(),
                                     "Posted story: "+postName,
                                    Toast.LENGTH_SHORT).show();
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
 
 
 
     // Twitter Functionality
     public void onTwitterClick(String messageToTwitter) {
 
         mTwitter 	= new TwitterApp(this, twitter_consumer_key,twitter_secret_key);
 
         mTwitter.setListener(mTwLoginDialogListener);
 
         if (mTwitter.hasAccessToken()) {
            // SHARE MSG
            mTwitter.postToTwitter(messageToTwitter);
         } else {
 
 
             mTwitter.authorize();
         }
     }
 
     private final TwDialogListener mTwLoginDialogListener = new TwDialogListener() {
         @Override
         public void onComplete(String value) {
             String username = mTwitter.getUsername();
             username		= (username.equals("")) ? "No Name" : username;
 
 
 
             Toast.makeText(getApplicationContext(), "Connected to Twitter as " + username, Toast.LENGTH_LONG).show();
         }
 
         @Override
         public void onError(String value) {
 
 
             Toast.makeText(getApplicationContext(), "Twitter connection failed", Toast.LENGTH_LONG).show();
         }
     };
 
 
 
 
     //LinkedIN
 
 }
