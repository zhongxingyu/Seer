 package com.lovamimi;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import java.util.List;
 
 public class CommentActivity extends BaseActivity {
 
     @Override
     protected void onStart() {
         super.onStart();
         track("Comment Loaded");
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_comment);
         Secret secret = (Secret) getIntent().getExtras().get("secret");
         assert (secret != null);
         setSecretToLayout(secret);
         LinearLayout mainLayout = (LinearLayout) findViewById(R.id.main_container);
         setCommentsToLayout(mainLayout, secret.comments);
     }
 
     private void setCommentsToLayout(LinearLayout mainLayout, List<Secret> comments) {
         for (final Secret comment : comments) {
             LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(
                     Context.LAYOUT_INFLATER_SERVICE);
             RelativeLayout incLayout = (RelativeLayout) inflater.inflate(R.layout.comment, null);
             ImageView commentIcon = (ImageView) incLayout.findViewById(R.id.profile_image);
             TextView dateText = (TextView) incLayout.findViewById(R.id.secret_datetime);
             TextView commentBody = (TextView) incLayout.findViewById(R.id.comment_body);
             TextView numLikes = (TextView) incLayout.findViewById(R.id.num_likes);
 
             View.OnClickListener likeListener = new View.OnClickListener() {
 
                 @Override
                 public void onClick(final View v) {
                     track("Liked comment");
                     new AsyncTask<Secret, Void, Boolean>() {
                         @Override
                         protected void onPreExecute() {
                             super.onPreExecute();
                             TextView view = (TextView) v;
                             comment.numLikes++;
                             view.setText("いいね(" + String.valueOf(comment.numLikes) + ")");
                         }
 
                         @Override
                         protected Boolean doInBackground(Secret... secrets) {
                             if (getSessionId() == null) {
                                 return false;
                             }
                             Secret secret = secrets[0];
                             return secret.postCommentLike(getSessionId());
                         }
 
                         @Override
                         protected void onPostExecute(Boolean isLikePosted) {
                             super.onPostExecute(isLikePosted);
                         }
                     }.execute(comment);
 
                 }
             };
             numLikes.setOnClickListener(likeListener);
 
             commentIcon.setImageResource(comment.getIconResource());
             dateText.setText(comment.datetime);
             commentBody.setText(comment.body);
             numLikes.setText("いいね(" + String.valueOf(comment.numLikes) + ")");
             mainLayout.addView(incLayout);
         }
     }
 
     private void setSecretToLayout(Secret secret) {
         TextView tv = (TextView) findViewById(R.id.secret_body);
         tv.setText(secret.body);
         TextView secretDatetime = (TextView) findViewById(R.id.secret_datetime);
         secretDatetime.setText(secret.datetime);
         ImageView icon = (ImageView) findViewById(R.id.profile_image);
         icon.setImageResource(secret.getIconResource());
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.comment, menu);
         return true;
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == R.id.action_status_update) {
             loginAndNextActivity(PostCommentActivity.class);
             return true;
         }
         return false;
     }
 }
