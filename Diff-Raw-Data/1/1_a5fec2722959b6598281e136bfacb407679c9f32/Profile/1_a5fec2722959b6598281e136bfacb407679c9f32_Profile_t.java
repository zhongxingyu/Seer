 package com.example.ucrinstagram;
 
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.amazonaws.auth.BasicAWSCredentials;
 import com.amazonaws.services.s3.AmazonS3Client;
 import com.amazonaws.services.s3.model.CannedAccessControlList;
 import com.amazonaws.services.s3.model.PutObjectRequest;
 import com.example.ucrinstagram.Models.Photo;
 import com.example.ucrinstagram.Models.User;
 import com.example.ucrinstagram.Models.UserProfile;
 
 public class Profile extends Activity implements OnClickListener {
 	// final int TAKE_PICTURE = 1;
 	private static final int ACTIVITY_SELECT_IMAGE = 1234;
 	// private String selectedImagePath;
 	// private ImageView img;
 
 	String username = Login.username;
 	ArrayList<String> image_links = new ArrayList<String>();
 	ImageView[] image;
 	User user1;
 
 	private AmazonS3Client s3Client = new AmazonS3Client(
 			new BasicAWSCredentials("", ""));
 	final String s3Link = "https://s3.amazonaws.com/ucrinstagram/";
 	String filePath;
 	String fileName;
 	String link;
 
 	InputStream is;
 	ArrayList<String> image_links2 = new ArrayList<String>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_profile);
 
 		username = Login.username;
 		loadInfo();
 		loadPics();
 
 		user1 = new User(Login.username);
 
 		// WebAPI api = new WebAPI();
 		// Photo profilePic = api.getPhoto(user1.getProfile().profile_photo);
         Photo profilePic = user1.getProfile().getProfilePhoto();
 
 		// Photo profilePic = user1.getProfile().getProfilePhoto();
 		// Loader image - will be shown before loading image
 		int loader = R.drawable.loader;
 		// Imageview to show
 		ImageView image = (ImageView) findViewById(R.id.image);
 		// Image url
 		String image_url = profilePic.path + "/" + profilePic.filename;
 		System.out.println(image_url);
 		// String image_url = "http://api.androidhive.info/images/sample.jpg";
 		// ImageLoader class instance
 		ImageLoader imgLoader = new ImageLoader(getApplicationContext());
 		imgLoader.DisplayImage(image_url, loader, image);
 
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		username = Login.username;
 		loadInfo();
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 
 		SharedPreferences settings = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		settings.edit().clear().commit();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_profile, menu);
 		return true;
 	}
 
 	public void loadInfo() {
 		// Using the data from the sharedPreference and updating it to the
 		// server
 		user1 = new User(Login.username);
 		SharedPreferences defSharedPrefs = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		UserProfile tempUserProfile = user1
 				.getProfile();
 		String tempNick = tempUserProfile.nickname;
 		String tempGender = tempUserProfile.gender;
 		String tempBio = tempUserProfile.bio;
 		String tempMail = user1.email;
 
 		tempUserProfile.nickname = defSharedPrefs.getString("nickPref", tempNick);
 		tempUserProfile.gender = defSharedPrefs.getString("listpref", tempGender);
 		tempUserProfile.bio = defSharedPrefs.getString("bioPref", tempBio);
 		user1.email = defSharedPrefs.getString("emailPref", tempMail);
 
 		tempUserProfile.save();
 		user1.save();
 
 		// setting data to SharedPrefs
 		Editor editor = defSharedPrefs.edit();
 		editor.putString("nickPref", user1.getProfile().nickname);
 		editor.putString("listPref", user1.getProfile().gender);
 		editor.putString("bioPref", user1.getProfile().bio);
 		editor.putString("emailPref", user1.email);
 		editor.commit();
 
 		TextView usernametv = (TextView) findViewById(R.id.username);
 		TextView nicknametv = (TextView) findViewById(R.id.nickname);
 		TextView gendertv = (TextView) findViewById(R.id.gender);
 		TextView biotv = (TextView) findViewById(R.id.aboutme);
 		TextView followerstv = (TextView) findViewById(R.id.followers);
 		TextView followingtv = (TextView) findViewById(R.id.following);
 		TextView datetv = (TextView) findViewById(R.id.profile_creation);
 		TextView phototv = (TextView) findViewById(R.id.photos);
 		TextView bdaytv = (TextView) findViewById(R.id.bday);
 
 		String un = user1.username;
 
 		UserProfile user1profile = user1.getProfile();
 		String nickname = user1profile.nickname;
 		String gender = user1profile.gender;
 		String bio = user1profile.bio;
 
 		SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yy");
 		Date birthdate = user1profile.birthday;
 		String bday = "Birthday: " + dateFormatter.format(birthdate);
 
 		Date created = user1profile.getCreatedAt();
 		String prof_created = "Profile Created On:\n"
 				+ dateFormatter.format(created);
 
 		// following
 		User[] following = user1.getFriends();
 		int fCount1 = following.length;
 		String followingCount = fCount1 + " Following";
 
 		// followers
 		User[] follower = user1.getFriendedBy();
 		int fCount2 = follower.length;
 		String followerCount = fCount2 + " Followers";
 
 		Photo[] userphotos = user1.getPhotos();
 		int pCount = userphotos.length;
 		String photoCount = pCount + " Photos";
 
 		usernametv.setText(un);
 		nicknametv.setText(nickname);
 		gendertv.setText(gender);
 		biotv.setText(bio);
 		followingtv.setText(followingCount);
 		followerstv.setText(followerCount);
 		datetv.setText(prof_created);
 		phototv.setText(photoCount);
 		bdaytv.setText(bday);
 	}
 
 	public void loadPics() {
 		User user1 = new User(username);
 		Photo[] userphotos = user1.getPhotos();
 
 		image = new ImageView[userphotos.length];
 		for (int i = userphotos.length - 1; i >= 0; i--) {
 			image[i] = new ImageView(this);
 			image[i].setImageResource(R.drawable.ic_launcher);
 			image[i].setAdjustViewBounds(true);
 			image[i].setOnClickListener(this);
 			LayoutParams lp = new LayoutParams(400, 400);
 			image[i].setLayoutParams(lp);
 			LinearLayout linlay = (LinearLayout) findViewById(R.id.linearPictures);
 			linlay.addView(image[i]);
 
 			// System.out.println(userphotos[i].path + '/' +
 			// userphotos[i].filename);
 			new DownloadImageTask(image[i]).execute(userphotos[i].path + '/'
 					+ userphotos[i].filename);
 		}
 	}
 
 	public void onClick(View view) {
 		User user1 = new User(username);
 		Photo[] userphotos = user1.getPhotos();
 
 		Intent intent = new Intent(this, SinglePicture.class);
 
 		for (int i = userphotos.length - 1; i >= 0; i--) {
 			if (view == image[i]) {
 				String link1 = userphotos[i].path + '/'
 						+ userphotos[i].filename;
 				String cap = userphotos[i].caption;
 				String gps = userphotos[i].gps;
 				intent.putExtra("gps", gps);
 				int photoid = userphotos[i].getId();
 				intent.putExtra("link", link1);
 				intent.putExtra("caption", cap);
 				intent.putExtra("photoid", photoid);
 				startActivity(intent);
 			}
 		}
 	}
 
 	public void home(View view) {
 		Intent intent = new Intent(this, HomeScreen.class);
 		startActivity(intent);
 	}
 
 	public void explore(View view) {
 		Intent intent = new Intent(this, Explore.class);
 		startActivity(intent);
 	}
 
 	public void camera(View view) {
 		Intent intent = new Intent(this, Camera.class);
 		startActivity(intent);
 	}
 
 	public void updates(View view) {
 		Intent intent = new Intent(this, Updates.class);
 		startActivity(intent);
 	}
 
 	public void settings(View view) {
 		Intent intent = new Intent(this, PrefsActivity.class);
 		startActivity(intent);
 	}
 
 	public void followers(View view) {
 		Intent intent = new Intent(this, Followers.class);
 		intent.putExtra("username",username);
 
 		startActivity(intent);
 	}
 
 	public void following(View view) {
 		Intent intent = new Intent(this, Following.class);
 		intent.putExtra("username",username);
 
 		startActivity(intent);
 	}
 
 	public void logout(View view) {
 		// Clearing all data from Shared Preferences
 
 		SharedPreferences settings = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		settings.edit().clear().commit();
 		Intent intent = new Intent(this, Signup_Login.class);
 		startActivity(intent);
 		finish();
 	}
 
 	public void startGallery(View view) {
 		Intent intent = new Intent();
 		intent.setType("image/*");
 		intent.setAction(Intent.ACTION_GET_CONTENT);
 		startActivityForResult(
 				Intent.createChooser(intent, "Select Profile Picture"),
 				ACTIVITY_SELECT_IMAGE);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 
 		filePath = null;
 		super.onActivityResult(requestCode, resultCode, data);
 		if (resultCode == RESULT_OK && requestCode == ACTIVITY_SELECT_IMAGE) {
 			Uri selectedImage = data.getData();
 			String[] filePathColumn = { MediaStore.Images.Media.DATA };
 
 			Cursor cursor = getContentResolver().query(selectedImage,
 					filePathColumn, null, null, null);
 			cursor.moveToFirst();
 
 			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
 			filePath = cursor.getString(columnIndex);
 			System.out.println(filePath);
 			cursor.close();
 		}
 		if (filePath != null) {
 			done(filePath);
 		}
 
 	}
 
 	public void done(String path) {
 		String[] tokens = filePath.split("/");
 		fileName = tokens[tokens.length - 1];
 
 		link = s3Link + username;
 
 		Photo photo1 = new Photo(link, fileName);
        photo1.save();
 
 		System.out.println("photo path before save");
 		System.out.println(photo1.path + "/" + photo1.filename);
 
 		user1.getProfile().saveProfilePhoto(photo1);
 		user1.save();
 
 		System.out.println("photo path after save");
 		System.out.println(user1.getProfile().getProfilePhoto().path + "/"
 				+ user1.getProfile().getProfilePhoto().filename);
 
 		new S3PutObjectTask().execute();
 
 		Intent intent = new Intent(this, Profile.class);
 		startActivity(intent);
 	}
 
 	private class S3PutObjectTask extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			s3Client.createBucket("ucrinstagram");
 			PutObjectRequest por = new PutObjectRequest("ucrinstagram",
 					username + "/" + fileName, new java.io.File(filePath));
 			por.setCannedAcl(CannedAccessControlList.PublicRead);
 			s3Client.putObject(por);
 			return null;
 		}
 	}
 
 	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
 		ImageView bmImage;
 
 		public DownloadImageTask(ImageView bmImage) {
 			this.bmImage = bmImage;
 		}
 
 		protected Bitmap doInBackground(String... urls) {
 			String urldisplay = urls[0];
 			Bitmap mIcon11 = null;
 			try {
 				BitmapFactory.Options options = new BitmapFactory.Options();
 				options.inSampleSize = 5;
 				InputStream in = new java.net.URL(urldisplay).openStream();
 				mIcon11 = BitmapFactory.decodeStream(in, null, options);
 			} catch (Exception e) {
 				Log.e("Error", e.getMessage());
 				e.printStackTrace();
 			}
 			return mIcon11;
 		}
 
 		protected void onPostExecute(Bitmap result) {
 			bmImage.setImageBitmap(result);
 		}
 	}
 }
