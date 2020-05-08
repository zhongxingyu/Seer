 package com.allplayers.android;
 
 import java.util.concurrent.ExecutionException;
 
 import com.allplayers.objects.GroupData;
 import com.allplayers.rest.RestApiV1;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class GroupPageActivity extends Activity {
     GroupData group;
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         group = (new Router(this)).getIntentGroup();
         super.onCreate(savedInstanceState);
         setContentView(R.layout.grouppage);
 
         String logoURL = group.getLogo();
         String uuid = group.getUUID();
 
         setButtonState(uuid);
 
         GetRemoteImageTask helper = new GetRemoteImageTask();
         helper.execute(logoURL);
     }
 
     /**
      * Checks if the user is a member of the group in order to determine if they should have
      * access to the buttons provided to view members, events, and photos.
      *
      * @param group_uuid
      */
     private void setButtonState(String group_uuid) {
         GetGroupMembersByGroupIdTask helper = new GetGroupMembersByGroupIdTask();
         helper.execute(group_uuid);
     }
 
     /*
      * Gets a remote image using a rest call and uses it in a view.
      */
     public class GetRemoteImageTask extends AsyncTask<String, Void, Bitmap> {
 
         protected Bitmap doInBackground(String... logoURL) {
             return RestApiV1.getRemoteImage(logoURL[0]);
         }
 
         protected void onPostExecute(Bitmap logo) {
             ImageView imView = (ImageView)findViewById(R.id.groupLogo);
             imView.setImageBitmap(logo);
         }
     }
 
     /*
      * Checks if the user is a group member. If the user is a group member the group page interface is set up.
      */
     public class GetGroupMembersByGroupIdTask extends AsyncTask<String, Void, String> {
         protected String doInBackground(String... group_uuid) {
             return RestApiV1.getGroupMembersByGroupId(group_uuid[0]);
         }
 
         protected void onPostExecute(String jsonResult) {
            boolean isMember;
            if (jsonResult.trim().equals("null") || jsonResult.trim().equals("error") ||
                    jsonResult.equals("You are not logged in")) {
                isMember = false;
            } else {
                isMember = true;
             }
 
             final Button groupMembersButton = (Button)findViewById(R.id.groupMembersButton);
             if (isMember) groupMembersButton.setVisibility(View.VISIBLE);
             groupMembersButton.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     Intent intent = (new Router(GroupPageActivity.this)).getGroupMembersActivityIntent(group);
                     startActivity(intent);
                 }
             });
 
             final Button groupEventsButton = (Button)findViewById(R.id.groupEventsButton);
             if (isMember) groupEventsButton.setVisibility(View.VISIBLE);
             groupEventsButton.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     Intent intent = (new Router(GroupPageActivity.this)).getGroupEventsActivityIntent(group);
                     startActivity(intent);
                 }
             });
 
             final Button groupPhotosButton = (Button)findViewById(R.id.groupPhotosButton);
             if (isMember) groupPhotosButton.setVisibility(View.VISIBLE);
             groupPhotosButton.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     Intent intent = (new Router(GroupPageActivity.this)).getGroupAlbumsActivityIntent(group);
                     startActivity(intent);
                 }
             });
 
             TextView groupInfo = (TextView)findViewById(R.id.groupDetails);
             groupInfo.setText("Title: " + group.getTitle() + "\n\nDescription: " + group.getDescription());
         }
     }
 }
