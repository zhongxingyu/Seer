 package com.normalexception.forum.rx8club.view.threadpost;
 
 /************************************************************************
  * NormalException.net Software, and other contributors
  * http://www.normalexception.net
  * 
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  ************************************************************************/
 
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.text.Html;
 import android.text.method.LinkMovementMethod;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.normalexception.forum.rx8club.Log;
 import com.normalexception.forum.rx8club.R;
 import com.normalexception.forum.rx8club.activities.pm.NewPrivateMessageActivity;
 import com.normalexception.forum.rx8club.activities.thread.EditPostActivity;
 import com.normalexception.forum.rx8club.handler.ForumImageHandler;
 import com.normalexception.forum.rx8club.handler.ImageLoader;
 import com.normalexception.forum.rx8club.html.LoginFactory;
 import com.normalexception.forum.rx8club.preferences.PreferenceHelper;
 import com.normalexception.forum.rx8club.view.ViewHolder;
 
 /**
  * Custom adapter to handle PostView objects
  */
 public class PostViewArrayAdapter extends ArrayAdapter<PostView> {
 	private Context activity;
 	private List<PostView> data;
 	private ImageLoader imageLoader; 
 	private final String TAG = "PostViewArrayAdapter";
 
 	/**
 	 * Custom adapter to handle PostView's
 	 * @param context				The source context
 	 * @param textViewResourceId	The resource id
 	 * @param objects				The list of objects
 	 */
 	public PostViewArrayAdapter(Context context, int textViewResourceId,
 			List<PostView> objects) {
 		super(context, textViewResourceId, objects);
 		activity = context;
 		data = objects;
 		imageLoader=new ImageLoader(activity.getApplicationContext());
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see android.widget.ArrayAdapter#getCount()
 	 */
 	@Override
 	public int getCount() {
 		return data == null? 0 : data.size();
 	}
 	
 	 /*
      * (non-Javadoc)
      * @see android.widget.ArrayAdapter#getItem(int)
      */
     @Override  
     public PostView getItem(int position) {     
         return data.get(position);  
     } 
     
     /*
 	 * (non-Javadoc)
 	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
 	 */
 	public View getView(int position, View convertView, ViewGroup parent) {		
 		View vi = convertView;
         if(vi == null) {
         	LayoutInflater vinf =
                     (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             vi = vinf.inflate(R.layout.view_newreply, null);
         }
         
         final PostView cv = data.get(position);
         
         ((TextView)ViewHolder.get(vi,R.id.nr_username)).setText(cv.getUserName());
         ((TextView)ViewHolder.get(vi,R.id.nr_userTitle)).setText(cv.getUserTitle());
         ((TextView)ViewHolder.get(vi,R.id.nr_userPosts)).setText(cv.getUserPostCount());
         ((TextView)ViewHolder.get(vi,R.id.nr_userJoin)).setText(cv.getJoinDate());
         ((TextView)ViewHolder.get(vi,R.id.nr_postDate)).setText(cv.getPostDate());
         
         TextView postText = ((TextView)ViewHolder.get(vi,R.id.nr_postText));
         ForumImageHandler fih = new ForumImageHandler(postText, activity);  
         postText.setMovementMethod(LinkMovementMethod.getInstance());
         
         // Lets make sure we remove any font formatting that was done within
         // the text
         String trimmedPost = 
        		cv.getUserPost().replaceAll("(?i)<(/*)font(.*?)>", "");
        
         // Show attachments if the preference allows it
         if(PreferenceHelper.isShowAttachments(activity)) 
         	trimmedPost = appendAttachments(trimmedPost, cv.getAttachments());
         
         postText.setText(Html.fromHtml(trimmedPost, fih, null));
         postText.setTextColor(Color.WHITE);
         postText.setLinkTextColor(Color.WHITE);
         
         // Load up the avatar of hte user, but remember to remove
         // the dateline at the end of the file so that we aren't
         // creating multiple images for a user.  The image still
         // gets returned without a date
         if(PreferenceHelper.isShowAvatars(activity)) {
 	        String nodate_avatar = 
 	        		cv.getUserImageUrl().indexOf('?') == -1? 
 	        				cv.getUserImageUrl() : 
 	        					cv.getUserImageUrl().substring(0, cv.getUserImageUrl().indexOf('?'));
 	        ImageView avatar = ((ImageView)ViewHolder.get(vi,R.id.nr_image));
 	        imageLoader.DisplayImage(nodate_avatar, avatar);
         }
         
         // Set the text size based on our preferences
         int font_size = PreferenceHelper.getFontSize(activity);
         postText.setTextSize(font_size);
         
         // Display the right items if the user is logged in
         setUserIcons(vi, cv.isLoggedInUser());
         
         // Set click listeners if we are logged in, hide the buttons
         // if we are not logged in
         if(LoginFactory.getInstance().isGuestMode()) {
         	((ImageView)ViewHolder.get(vi,R.id.nr_quoteButton)).setVisibility(View.GONE);
         	((ImageView)ViewHolder.get(vi,R.id.nr_editButton)).setVisibility(View.GONE);
         	((ImageView)ViewHolder.get(vi,R.id.nr_pmButton)).setVisibility(View.GONE);
         	((ImageView)ViewHolder.get(vi,R.id.nr_deleteButton)).setVisibility(View.GONE);
         } else {
 	        ((ImageView)ViewHolder.get(vi,R.id.nr_quoteButton))
 	        	.setOnClickListener(new OnClickListener() {
 	        	@Override
 	            public void onClick(View arg0) {
 	        		Log.d(TAG, "Quote Clicked");
 					String txt = Html.fromHtml(cv.getUserPost()).toString();
 					String finalText = String.format("[quote=%s]%s[/quote]",
 							cv.getUserName(), txt);
 					((TextView)((Activity) activity).findViewById(R.id.postBox)).setText(finalText);
 					((TextView)((Activity) activity).findViewById(R.id.postBox)).requestFocus();
 	        	}
 	        });
 	        
 	        ((ImageView)ViewHolder.get(vi,R.id.nr_editButton))
 	        	.setOnClickListener(new OnClickListener() {
 	        	@Override
 	        	public void onClick(View arg0) {
 	        		Log.d(TAG, "Edit Clicked");
 	        		Intent _intent = new Intent(activity, EditPostActivity.class); 
 	        		_intent.putExtra("postid", cv.getPostId());
 					_intent.putExtra("securitytoken", cv.getToken());
 					activity.startActivity(_intent);
 	        	}
 	        });
 	        
 	        ((ImageView)ViewHolder.get(vi,R.id.nr_pmButton))
 	        	.setOnClickListener(new OnClickListener() {
 	        	@Override
 	        	public void onClick(View arg0) {
 	        		Log.d(TAG, "PM Clicked");
 	        		Intent _intent =  new Intent(activity, NewPrivateMessageActivity.class);
 					_intent.putExtra("user", cv.getUserName());
 					activity.startActivity(_intent);
 	        	}
 	        });
 	    	
 	        final boolean isFirstPost = (position == 0);
 	        ((ImageView)ViewHolder.get(vi,R.id.nr_deleteButton))
 	        	.setOnClickListener(new OnClickListener() {
 	        	@Override
 	        	public void onClick(View arg0) {
 	        		final Intent _intent = new Intent(activity, EditPostActivity.class);     		
 					DialogInterface.OnClickListener dialogClickListener = 
 						new DialogInterface.OnClickListener() {
 					    @Override
 					    public void onClick(DialogInterface dialog, int which) {
 					        switch (which){
 					        case DialogInterface.BUTTON_POSITIVE:  
 					        	_intent.putExtra("postid", cv.getPostId());
 					        	_intent.putExtra("securitytoken", cv.getToken());
 					        	_intent.putExtra("delete", true);
 					        	_intent.putExtra("deleteThread", isFirstPost && cv.isLoggedInUser());
 					        	activity.startActivity(_intent);
 					            break;
 					        }
 					    }
 					};
 		
 					AlertDialog.Builder builder = 
 							new AlertDialog.Builder(activity);
 					builder
 						.setMessage("Are you sure you want to delete your post?")
 						.setPositiveButton("Yes", dialogClickListener)
 					    .setNegativeButton("No", dialogClickListener)
 					    .show();
 	        	}
 	        });
         }
         
         return vi;
 	}
 	
 	/**
 	 * Append attachments to the end of this post if they exist
 	 * @param trimmedPost	The current post
 	 * @param attachments	The attachments of the post
 	 * @return				An appended html string
 	 */
 	private String appendAttachments(String trimmedPost,
 			List<String> attachments) {
 		if(attachments == null || attachments.isEmpty())
 			return trimmedPost;
 		
 		// Create an html string for the attachments
 		String attachString = "";
 		for(String attachment : attachments)
 			attachString += 
 				String.format("<a href=\"%s\"><img src=\"%s\"></a>&nbsp;", 
 						attachment, attachment);
 		
 		// Now append to the original text
 		trimmedPost = 
 				String.format("%s<br><br><b>Attachments:</b><br>%s", 
 						trimmedPost, attachString);
 		
 		return trimmedPost;
 	}
 
 	/**
 	 * If the post is by the logged in user, make sure that they can see the edit and 
 	 * delete buttons
 	 * @param vi				The thread view object
 	 * @param isLoggedInUser	True if post is by logged user
 	 */
 	private void setUserIcons(View vi, boolean isLoggedInUser) {
 		((ImageView)ViewHolder.get(vi,R.id.nr_quoteButton))
 			.setVisibility(View.VISIBLE);
 		
 		((ImageView)ViewHolder.get(vi,R.id.nr_pmButton))
 			.setVisibility(View.VISIBLE);
 		
 		((ImageView)ViewHolder.get(vi,R.id.nr_editButton))
 			.setVisibility(isLoggedInUser? View.VISIBLE : View.GONE);
 		
 		((ImageView)ViewHolder.get(vi,R.id.nr_deleteButton))
 			.setVisibility(isLoggedInUser? View.VISIBLE : View.GONE);
 	}
 }
