 package com.metat.main;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.apache.http.util.ByteArrayBuffer;
 
 import com.metat.contacts.R;
 import com.metat.dataaccess.GroupsDataAccess;
 import com.metat.dataaccess.MeetupContactDataAccess;
 import com.metat.helpers.ImagesHelper;
 import com.metat.models.Group;
 import com.metat.models.MeetupContact;
 import com.metat.models.NavigationSource;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class ViewMeetupContactActivity extends Activity {
 	private MeetupContact _meetupContact;
 	private NavigationSource _navigationSource = NavigationSource.AllContacts;
 	
 	private ImageView _contactImage;
 	private TextView _name;
 	private TextView _groupName;
 
 	private ImageView _twitterImageView;
 	private ImageView _facebookImageView;
 	private ImageView _flickrImageView;
 	private ImageView _tumblrImageView;
 	private ImageView _linkedInImageView;
 	
 	private LinearLayout _introductionContainer;
 	private TextView _introduction;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.view_meetup_contact);
         
         getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar));
         getActionBar().setDisplayShowHomeEnabled(true);
         getActionBar().setDisplayShowTitleEnabled(false);
         
         Bundle extras = getIntent().getExtras();
 
         if (extras.containsKey("navigationSource"))
         	_navigationSource = (NavigationSource)extras.get("navigationSource");
         
         MeetupContactDataAccess contactDataAccess = new MeetupContactDataAccess(this);
         _meetupContact = contactDataAccess.getMeetupContact(extras.getLong("contactId"));
 
         _contactImage = (ImageView) findViewById(R.id.contact_image);
     	
     	if (_meetupContact.getPhotoThumbnail().length() > 0)
     	{
     		_contactImage.setVisibility(View.VISIBLE);
 
             Thread thread = new Thread() {
                 @Override
                 public void run() {
             		byte[] downloadedImage = new byte[0];
         		
         			URL imageUrl;
         			URLConnection imageConnection;
         			InputStream inputStream;
         			
         			String[] urlParts = _meetupContact.getPhotoThumbnail().split("/");
         			
         			if (urlParts.length == 0)
         			{
         				return;
         			}
         			
         			if (!urlParts[urlParts.length-1].contains("."))
         			{
         				return;
         			}
         			
         			try
         			{
         				imageUrl = new URL(_meetupContact.getPhotoThumbnail());
         			}
         			catch (Exception ex)
         			{
         				return;
         			}
         			
         			try
         			{
         				imageConnection = imageUrl.openConnection();
         				inputStream = imageConnection.getInputStream();
         			}
         			catch (IOException ex)
         			{
         				return;
         			}
         			
         			BufferedInputStream bis = new BufferedInputStream(inputStream);
         			ByteArrayBuffer baf = new ByteArrayBuffer(50);
         		
         			int current = 0;
 
         			try
         			{
                         while ((current = bis.read()) != -1) 
                         {
                             baf.append((byte) current);
                         }
         			}
         			catch (IOException ex)
         			{
         				return;
         			}
         			
         			if (baf.isEmpty())
         			{
         				return;
         			}
         			
         			downloadedImage = baf.toByteArray();
         			
         			try
         			{
         				bis.close();
         			}
         			catch (Exception ex)
         			{
         				bis = null;
         			}
         			
         			try
         			{
         				inputStream.close();
         			}
         			catch (Exception ex)
         			{
         				inputStream = null;
         			}
         			
         			baf.clear();
         			imageConnection = null;
         			baf = null;
         			
                     Bitmap bitmap = ImagesHelper.ImageFileToBitmap(downloadedImage);
                     
                     Message message = handler.obtainMessage(1, bitmap);
                     handler.sendMessage(message);
                 }
             };
             
             thread.start();
     	}
     	else
     	{
     		_contactImage.setVisibility(View.GONE);
     	}
     	
     	_name = (TextView) findViewById(R.id.name);
     	_name.setText(_meetupContact.getName());
     	
     	GroupsDataAccess groupsDataAccess = new GroupsDataAccess(this);
     	Group group = groupsDataAccess.getGroup(_meetupContact.getMeetupGroupId());
     	
     	_groupName = (TextView) findViewById(R.id.meetup_group);
     	_groupName.setText(group.getName());
 
     	_twitterImageView = (ImageView) findViewById(R.id.twitter_link);
     	if ((_meetupContact.getTwitterId() != null) && (_meetupContact.getTwitterId().trim().length() > 0))
     	{
     		_twitterImageView.setVisibility(View.VISIBLE);
     		_twitterImageView.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View view) {
 					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + _meetupContact.getTwitterId().replaceAll("@", "")));
 					browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 					getBaseContext().startActivity(browserIntent);
 				}
     			
     		});
     	}
     	else
     	{
     		_twitterImageView.setVisibility(View.GONE);
     	}
     	
     	_facebookImageView = (ImageView) findViewById(R.id.facebook_link);
     	if ((_meetupContact.getFacebookId() != null) && (_meetupContact.getFacebookId().trim().length() > 0))
     	{
     		_facebookImageView.setVisibility(View.VISIBLE);
     		_facebookImageView.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View view) {
 					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com/profile.php?id=" + _meetupContact.getFacebookId()));
 					browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 					getBaseContext().startActivity(browserIntent);
 				}
     			
     		});
     	}
     	else
     	{
     		_facebookImageView.setVisibility(View.GONE);
     	}
     	
     	_flickrImageView = (ImageView) findViewById(R.id.flickr_link);
     	if ((_meetupContact.getFlickrId() != null) && (_meetupContact.getFlickrId().trim().length() > 0))
     	{
     		_flickrImageView.setVisibility(View.VISIBLE);
     		_flickrImageView.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View view) {
 					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(_meetupContact.getFlickrId()));
 					browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 					getBaseContext().startActivity(browserIntent);
 				}
     			
     		});
     	}
     	else
     	{
     		_flickrImageView.setVisibility(View.GONE);
     	}
     	
     	_tumblrImageView = (ImageView) findViewById(R.id.tumblr_link);
     	if ((_meetupContact.getTumblrId() != null) && (_meetupContact.getTumblrId().trim().length() > 0))
     	{
     		_tumblrImageView.setVisibility(View.VISIBLE);
     		_tumblrImageView.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View view) {
 					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(_meetupContact.getTumblrId()));
 					browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 					getBaseContext().startActivity(browserIntent);
 				}
     			
     		});
     	}
     	else
     	{
     		_tumblrImageView.setVisibility(View.GONE);
     	}
     	
     	_linkedInImageView = (ImageView) findViewById(R.id.linkedin_link);
     	if ((_meetupContact.getLinkedInId() != null) && (_meetupContact.getLinkedInId().trim().length() > 0))
     	{
     		_linkedInImageView.setVisibility(View.VISIBLE);
     		_linkedInImageView.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View view) {
 					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(_meetupContact.getLinkedInId()));
 					browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 					getBaseContext().startActivity(browserIntent);
 				}
     			
     		});
     	}
     	else
     	{
     		_linkedInImageView.setVisibility(View.GONE);
     	}
 
     	_introductionContainer = (LinearLayout) findViewById(R.id.introduction_container);
     	_introductionContainer.setBackgroundResource(R.drawable.contact_detail_full);
 		
     	if (_meetupContact.getBio().trim().length() > 0)
     	{
     		_introductionContainer.setVisibility(View.VISIBLE);
 	    	_introduction = (TextView) findViewById(R.id.introduction);
 	    	_introduction.setText(_meetupContact.getBio());
     	}
     	else
     	{
     		_introductionContainer.setVisibility(View.GONE);
     	}
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.view_existing_contact_menu, menu);
         return true;
     }
 
     @Override
     public void onBackPressed() {
		Intent cancelIntent = new Intent(getBaseContext(), MainActivity.class);

		switch (_navigationSource)
		{
		}
		
		cancelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		
		getBaseContext().startActivity(cancelIntent);	
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch (item.getItemId())
         {
 		case R.id.back:
 			Intent backIntent = new Intent(getBaseContext(), GroupActivity.class);
 			backIntent.putExtra("groupId", _meetupContact.getMeetupGroupId());
 			backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			getBaseContext().startActivity(backIntent);	
     		return true;
 		case R.id.add:
 			Intent intent = new Intent(getBaseContext(), AddContactActivity.class);
     		intent.putExtra("contactId", _meetupContact.getId());
     		intent.putExtra("groupId", _meetupContact.getMeetupGroupId());
     		intent.putExtra("navigationSource", NavigationSource.ViewMeetupContact);
 			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			
 			getBaseContext().startActivity(intent);	
     		return true;
         }
         
         return false;
     }
 
     final Handler handler = new Handler() {
         @Override
         public void handleMessage(Message message) {
         	_contactImage.setImageBitmap((Bitmap) message.obj);
         }
     };
 }
