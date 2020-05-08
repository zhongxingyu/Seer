 package com.example.webphotos;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.Menu;
 import android.view.Window;
 import android.widget.ImageView;
 import android.widget.RatingBar;
 import android.widget.TextView;
 
 public class AddPicActivity extends Activity {
 	//could fairly simply allow for phones without a camera to use the app but not a priority
 	
 	//SO FAR this should launch the camera and come back and preview the picture taken but it doesn't preview
 	//debugging this shit is hard because of weird usb mass storage interactions
 	//usb takes over the memory so that the camera can't write to files but other usb connections don't talk to logcat
 	//something in onActivityResult isn't working but I can't debug so I have no idea what
 	//TODO upload photo and rating to server. started method sendToServer at bottom.
 	//TODO make a submit graphic to replace the toMap graphic
 	
 	static RestaurantData restaurant;
 	static ListOfRestaurants restList;
 	String currentPhotoPath;
 	int pictureActionCode = 42;
 	ImageView preview;
 	static TextView restName;
 	RatingBar stars;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		Intent intent = getIntent();
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_add_pic);
 		if (intent.hasExtra("restaurant")) {setRest((RestaurantData) intent.getSerializableExtra("restaurant"));}
 		else {
 			restList = (ListOfRestaurants) intent.getSerializableExtra("restaurantList");
 			DialogFragment picker = new RestaurantSelectDialog();
 			picker.show(getFragmentManager(), "restPicker");
 			Log.d("dialog", "where my dialog at");
 		}
 		preview = (ImageView)findViewById(R.id.newPhoto);
 		restName = (TextView)findViewById(R.id.addPicRestName);
 		try {
 			createImageFile();
 		} catch (IOException e) {
 			Log.d("IOException", "failed to create image file");
 			e.printStackTrace();
 		}
 		dispatchTakePictureIntent(pictureActionCode);
 		restName.setText("bloop");
 		restName.setTextColor(Color.parseColor("#D60000"));
 		restName.setTextSize(20);
 		preview.setImageDrawable(getResources().getDrawable(R.drawable.burger));
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		//Check receiving correct intent
 		if (requestCode == pictureActionCode) {
 			//Check for successful request
 			if (resultCode == RESULT_OK) {
 				galleryAddPic();
 				Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
 				Log.d("bitmap", "bitmap created");
 				this.preview.setImageBitmap(bitmap);
 				Log.d("bitmap", "preview should be showing");
 			}
 		}
 	}
 	
 	private void dispatchTakePictureIntent(int actionCode) {
 		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		File picFile = new File(currentPhotoPath);
 		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(picFile));
 		startActivityForResult(takePictureIntent, actionCode);
 	}
 	
 	private File createImageFile() throws IOException {
 		// Create a unique image file name
 		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
 		String imageFileName = "cs" + timeStamp + "_";
 //		File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CuisineStream");
 		File image = File.createTempFile(imageFileName, ".jpg", this.getCacheDir());
 		currentPhotoPath = image.getAbsolutePath();
 		return image;
 	}
 	
 	private void galleryAddPic() {
 		//I think this just makes the picture available in the gallery behind the scenes
 		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
 		File f = new File(currentPhotoPath);
 		Uri contentUri = Uri.fromFile(f);
 		mediaScanIntent.setData(contentUri);
 		this.sendBroadcast(mediaScanIntent);
 	}
 	
 	public static class RestaurantSelectDialog extends DialogFragment {
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			builder.setTitle("Select Restaurant");
 			final RestaurantData[] fullList = restList.getRestaurantArray();
 			String[] names = new String[fullList.length];
 			for (int i=0; i<names.length; i++) {names[i] = fullList[i].name;}
 			builder.setItems(names, new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					Log.d("which", "which is "+which);
 					setRest(fullList[which]);
 					Log.d("restCheck", "rest: "+restaurant.toString());
 				}
 			});
 			return builder.create();
 		}
 	}
 	
	private void submitButton() {
 		//sendToServer();
 		Intent home = new Intent(AddPicActivity.this, MainWebActivity.class);
 		startActivity(home);
 	}
 	
 	private void sendToServer() {
 		double rating = stars.getRating();
 		String name = restaurant.name;
 		String location = Double.toString(restaurant.lat) + "+" + Double.toString(restaurant.lng);
 		String id = restaurant.id;
 		
 	}
 	
 	public static void setRest(RestaurantData rest) {restaurant = rest; restName.setText(restaurant.name);}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.add_pic, menu);
 		return true;
 	}
 
 }
