 package com.tuit.ar.activities;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 import com.tuit.ar.R;
 import com.tuit.ar.api.Twitter;
 import com.tuit.ar.api.TwitterAccountRequestsObserver;
 import com.tuit.ar.api.TwitterRequest;
 import com.tuit.ar.api.request.Options;
 
 public class NewTweet extends Activity implements OnClickListener, TwitterAccountRequestsObserver {
 	static private final int MENU_ADD_PHOTO = 0;
 
 	static private final int MAX_CHARS = 140;
 
 	private String replyToTweetId;
 	private EditText messageField;
 	private TextView charCount;
 	private SeekBar imageSize;
 
 	private File photo;
 
 	private Bitmap bm = null;
 
 	private ImageView preview = null;
 	private int imageWidth = 0;
 	private int imageHeight = 0;
 	private float scale = 1.0F;
 	private TextView imageSizeWidth;
 	private TextView imageSizeHeight;
 	private TextView imageSizeMb;
 	static private final int MAX_PROGRESS = 10000;
 	private Button send;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.new_tweet);
 		send = (Button)findViewById(R.id.send);
 		send.setOnClickListener(this);
 		Twitter.getInstance().getDefaultAccount().addRequestObserver(this);
 
 		Intent intent = getIntent();
 		replyToTweetId = intent.getStringExtra("reply_to_id");
 		String replyToUser = intent.getStringExtra("reply_to_username");
 		if (replyToUser != null) {
 			TextView username = (TextView)findViewById(R.id.replyToUsername);
 			username.setText(getString(R.string.inReplyTo).replace("%s", "@" + replyToUser));
 		}
 		String defaultMessage = intent.getStringExtra("default_text"); 
 		messageField = (EditText) findViewById(R.id.tweetMessage);
 		charCount = (TextView) findViewById(R.id.charCount);
 		if (defaultMessage != null) {
 			messageField.setText(defaultMessage);
 			charCount.setText(String.valueOf(MAX_CHARS - defaultMessage.length()));
 		}
 
 		messageField.setOnKeyListener(new OnKeyListener() {
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				updateCharCount();
 				return false;
 			}
 		});
 
 		imageSize = (SeekBar) findViewById(R.id.imageSize);
 		imageSize.setMax(MAX_PROGRESS);
 		imageSize.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 			public void onStopTrackingTouch(SeekBar arg0) {
 				updateImageSizeKb();
 			}
 			
 			public void onStartTrackingTouch(SeekBar arg0) {
 			}
 			
 			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 				scale = (float)progress / MAX_PROGRESS;
 				imageSizeWidth.setText(getString(R.string.width) + " " + String.valueOf(Math.round(scale * imageWidth)) + "px");
 				imageSizeHeight.setText(getString(R.string.height) + " " + String.valueOf(Math.round(scale * imageHeight)) + "px");
 				imageSizeMb.setText(getString(R.string.loading));
 			}
 		});
 		imageSizeHeight = (TextView) findViewById(R.id.imageSizeHeight);
 		imageSizeWidth = (TextView) findViewById(R.id.imageSizeWidth);
 		imageSizeMb = (TextView) findViewById(R.id.imageSizeMb);
 		preview = (ImageView)findViewById(R.id.preview);
 		imageSizeMb.setText("");
 
 		imageSize.setVisibility(View.INVISIBLE);
 		imageSizeHeight.setVisibility(View.INVISIBLE);
 		imageSizeWidth.setVisibility(View.INVISIBLE);
 	}
 
 	protected void updateImageSizeKb() {
 		imageSizeMb.setText(getString(R.string.loading));
 		try {
 			createImage();
 			imageSizeMb.setText((photo.length() / 1024) + "KB");
 		} catch (Exception e) {
 			
 		}
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, MENU_ADD_PHOTO, 0, R.string.addPhoto);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {  
 	    switch (item.getItemId()) {  
 	    case MENU_ADD_PHOTO:
 	    {
             startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 0);
 	    	break;
 	    }
 	    }
 	    return true;
     }
 	    
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		try {
 			if (resultCode == Activity.RESULT_OK) {
 				bm = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
 				preview.setImageBitmap(bm);
 				imageWidth = bm.getWidth();
 				imageHeight = bm.getHeight();
 				imageSize.setVisibility(View.VISIBLE);
 				imageSizeHeight.setVisibility(View.VISIBLE);
 				imageSizeWidth.setVisibility(View.VISIBLE);
 				imageSize.setProgress(MAX_PROGRESS);
 				updateImageSizeKb();
 			}
 		} catch (Exception e) {
 			Toast.makeText(this, getString(R.string.unableToUpload), Toast.LENGTH_LONG).show();
 		}
 	}
 
 	private void updateCharCount() {
 		charCount.setText(String.valueOf(MAX_CHARS - messageField.getText().toString().length()));
 	}
 
 	protected void createImage() throws Exception {
 		FileOutputStream fos;
 		// FIXME: not using temporary files?
 		fos = super.openFileOutput("upload.jpg", MODE_WORLD_READABLE);
 		Bitmap bm;
 		if (scale < 1.0)
 			bm = Bitmap.createScaledBitmap(this.bm, Math.round(scale * imageWidth), Math.round(scale * imageHeight), true);
 		else
 			bm = this.bm;
 		bm.compress(CompressFormat.JPEG, 75, fos);
 
 		fos.flush();
 		fos.close();
 
 		photo = new File("/data/data/com.tuit.ar/files", "upload.jpg");
 	}
 
 	public void onClick(View v) {
 		String message = messageField.getText().toString();
 		try {
 			if (this.bm != null) {
 				createImage();
 				Twitter.getInstance().getDefaultAccount().upload(photo, message);
 			} else {
 				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
 				params.add(new BasicNameValuePair("status", message));
 				if (replyToTweetId != null) params.add(new BasicNameValuePair("in_reply_to_status_id", replyToTweetId));
 				int method = TwitterRequest.METHOD_POST;
 				Twitter.getInstance().getDefaultAccount().requestUrl(Options.POST_TWEET, params, method);
 			}
 		} catch (Exception e) {
 			sendFailed(e.getLocalizedMessage());
 		}
 	}
 	public void sendFailed(String message) {
 		if (message == null)
 			Toast.makeText(this, getString(R.string.unableToPost), Toast.LENGTH_SHORT).show();
 		else
			Toast.makeText(this, getString(R.string.unableToPost) + "(" + message + ")", Toast.LENGTH_SHORT).show();
 	}
 
 	public void requestHasStarted(TwitterRequest request) {
 		setProgressBarIndeterminateVisibility(true);
 	}
 
 	public void requestHasFinished(TwitterRequest request) {
 		setProgressBarIndeterminateVisibility(false);
     	if (request.getUrl().equals(Options.POST_TWEET) || request.getUrl().equals(Options.POST_TWEET_WITH_PHOTO)) {
     		if (request.getStatusCode() >= 200 && request.getStatusCode() < 400) {
     			Toast.makeText(this, getString(R.string.messageSent), Toast.LENGTH_SHORT).show();
     			finish();
     			try {
     				Twitter.getInstance().getDefaultAccount().requestUrl(Options.FRIENDS_TIMELINE);
     			} catch (Exception e) {}
     		}
     		else sendFailed(request.getErrorMessage());
     	}
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 		Twitter.getInstance().getDefaultAccount().removeRequestObserver(this);
 		send.setOnClickListener(null);
 		messageField.setOnKeyListener(null);
 		imageSize.setOnSeekBarChangeListener(null);
 		if (photo != null) photo.delete();
 	}
 }
