 package com.tuit.ar.activities;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.tuit.ar.R;
 import com.tuit.ar.api.Avatar;
 import com.tuit.ar.api.AvatarObserver;
 import com.tuit.ar.api.Twitter;
 import com.tuit.ar.api.TwitterAccountRequestsObserver;
 import com.tuit.ar.api.TwitterRequest;
 import com.tuit.ar.api.request.Options;
 import com.tuit.ar.models.User;
 
 public class Profile extends Activity implements AvatarObserver, TwitterAccountRequestsObserver {
 	private ImageView avatar;
 	private TextView nickname;
 	private TextView fullname;
 	private TextView description;
 	private Button following;
 	private TextView followingNumber;
 	private TextView followerNumber;
 	private Button seeInMap;
 	private Button url;
 
 	private User user = null;
 	static private User userToDisplay = null;
 
 	static public void setUserToDisplay(User user) {
 		userToDisplay = user;
 	}
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.profile);
 		Twitter.getInstance().getDefaultAccount().addRequestObserver(this);
 		user = userToDisplay;
 		
 		userToDisplay = null;
 
 		avatar = (ImageView)findViewById(R.id.avatar);
 		avatar.setVisibility(View.INVISIBLE);
 		nickname = (TextView)findViewById(R.id.nickname);
 		fullname = (TextView)findViewById(R.id.fullname);
 		following = (Button)findViewById(R.id.follow);
 		following.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				following();
 			}
 		});
 		description = (TextView)findViewById(R.id.description);
 		followingNumber = (TextView)findViewById(R.id.following_number);
 		followerNumber = (TextView)findViewById(R.id.follower_number);
 
 		url = (Button)findViewById(R.id.url);
 		url.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				startActivity(new Intent(Intent.ACTION_VIEW , Uri.parse(user.getUrl())));
 			}
 		});
 
 		seeInMap = (Button)findViewById(R.id.see_in_map);
 		seeInMap.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent intent = new Intent(Intent.ACTION_VIEW , Uri.parse("geo:" + user.getLocation()));
 				try {
 					startActivity(intent);
 				} catch (Exception e) {
 					Toast.makeText(Profile.this, getString(R.string.unableToShowMap), Toast.LENGTH_SHORT).show();
 					e.printStackTrace();
 				}
 			}
 		});
 
 		showFollowing();
 
 		if (user != null) {
 			Avatar avatar = new Avatar(user.getProfileImageUrl());
 			avatar.addRequestObserver(this);
 			avatar.download();
 			
 			nickname.setText(user.getScreenName());
 			fullname.setText(user.getName());
 			description.setText(user.getDescription());
			followingNumber.setText(String.valueOf(user.getFollowersCount()));
			followerNumber.setText(String.valueOf(user.getFriendsCount()));
 			seeInMap.setVisibility(user.getLocation() == null ? View.INVISIBLE : View.VISIBLE);
 			String _url = user.getUrl();
 			boolean hasUrl = _url != null && _url.length() > 0;
 			url.setVisibility(hasUrl ? View.VISIBLE : View.INVISIBLE);
 			if (hasUrl) url.setText(_url);
 		}
 	}
 
 	public void avatarHasFailed(Avatar avatar) {
 		this.avatar.setVisibility(View.INVISIBLE);
 	}
 
 	public void avatarHasFinished(Avatar avatar) {
 		this.avatar.setImageBitmap(avatar.getResponse());
 		this.avatar.setVisibility(View.VISIBLE);
 	}
 
 	protected void following() {
         new AlertDialog.Builder(this)
         .setIcon(android.R.drawable.ic_dialog_alert)
         .setMessage(user.isFollowing() ? R.string.confirmUnfollow : R.string.confirmFollow)
         .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
             	try {
             		if (user.isFollowing()) user.stopFollowing();
             		else user.follow();
             	} catch (Exception e) {
             		Toast.makeText(Profile.this, getString(R.string.unableToFollow), Toast.LENGTH_SHORT).show();
             	}
             }
         })
         .setNegativeButton(R.string.no, null)
         .show();
 	}
 
 	public void requestHasFinished(TwitterRequest request) {
 		if ((!request.getUrl().equals(Options.FOLLOW)) && (!request.getUrl().equals(Options.UNFOLLOW))) return;
 		user.requestHasFinished(request);
 		showFollowing();
 	}
 
 	private void showFollowing() {
 		following.setText(getString(user.isFollowing() ? R.string.following : R.string.notFollowing));
 	}
 
 	public void requestHasStarted(TwitterRequest request) {
 	}
 }
