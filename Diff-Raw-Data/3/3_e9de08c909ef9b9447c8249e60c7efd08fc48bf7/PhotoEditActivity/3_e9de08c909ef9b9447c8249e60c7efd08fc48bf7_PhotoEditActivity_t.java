 /**
  /* @author Niklas Bauer
  */
 package com.uc.memeapp;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 public class PhotoEditActivity extends Activity implements OnClickListener {
 	public int topMaxLines = 3;
 	public int bottomMaxLines = 3;
 	public EditText topEditText;
 	public EditText bottomEditText;
 	public FrameLayout fLayout;
 	private static final String TAG = "PhotoEditActivity";
 	protected static final int MEDIA_TYPE_IMAGE = 1;
 	public static String imagePath = "";
 	private final int FONT_SIZE = 50;
 	private final int FONT_SIZE_UPDATE =150;
 
 	/**
 	 * -Sets the layout to be the one defined in the activity_photo_edit.xml
 	 * file -Depending on which activity called it, Stock or Gallery, loads a
 	 * photo in a different manner due to nature of how images are sent Todo's
 	 * listed in order of importance: TODO: pass the image Uri to a
 	 * "posting Activity" to be posted TODO:Implement ability to save the image
 	 * 
 	 * @param savedInstanceState
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_photo_edit);
 
 		// caller is the activity that initiated this activity
 		String caller = getIntent().getStringExtra("caller");
 		ImageView displayImage = (ImageView) findViewById(R.id.image_to_edit);
 		// if StockActivity
 		if (caller.equals("stock")) {
 			Bundle bdl = getIntent().getExtras();
 			// gets index of array where stock photo is held, returns that
 			// Drawable
 			int index = bdl.getInt("Index");
 			displayImage.setImageResource(ImageAdapter.mThumbIds[index]);
 		}
 		// if GalleryActivity or if camera activity
 		else if (caller.equals("Gallery")) {
 			// Uri of image is passed as a string, parses the string and loads
 			// it as a Uri
 			String receivedPath = getIntent().getStringExtra("path");
 			Uri receivedUri = Uri.parse(receivedPath);
 			displayImage.setImageURI(receivedUri);
 		} else if (caller.equals("camera")) {
 			String receivedPath = getIntent().getStringExtra("path");
 			Uri receivedUri = Uri.parse(receivedPath);
 
 			Bitmap bmpImage = BitmapFactory.decodeFile(receivedUri.getPath());
 			Matrix matrix = new Matrix();
 			matrix.postRotate(270);
 			Bitmap rotateImage = Bitmap.createBitmap(bmpImage, 0, 0,
 					bmpImage.getWidth(), bmpImage.getHeight(), matrix, true);
 			displayImage.setImageBitmap(rotateImage);
 		}
 
 		// create buttons, set onclicklisteners
 		ImageButton deleteButton = (ImageButton) findViewById(R.id.imgButton_delete);
 		ImageButton saveButton = (ImageButton) findViewById(R.id.imgButton_save);
 		ImageButton postButton = (ImageButton) findViewById(R.id.imgButton_post);
 		postButton.setOnClickListener(this);
 		deleteButton.setOnClickListener(this);
 		saveButton.setOnClickListener(this);
 
 		// for saving image
 		fLayout = (FrameLayout) findViewById(R.id.editPhoto);
 		fLayout.setDrawingCacheEnabled(true);
 
 		// Create the two editText objects
 		topEditText = (EditText) findViewById(R.id.topInputText);
 		bottomEditText = (EditText) findViewById(R.id.bottomInputText);
 		
 		//Create the font
 		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/impact.ttf");
 		topEditText.setTypeface(tf);
 		bottomEditText.setTypeface(tf);
 
 		// cant scroll right, number of lines, and font size set
 		topEditText.setHorizontallyScrolling(false);
 		topEditText.setMaxLines(topMaxLines);
 		topEditText.setTextSize(FONT_SIZE);
 		topEditText.setBackgroundColor(Color.argb(65, 139, 137, 137));	
 		topEditText.setMaxWidth(displayImage.getWidth());		
 	
 		
 		bottomEditText.setMaxLines(bottomMaxLines);
 		bottomEditText.setHorizontallyScrolling(false);
 		bottomEditText.setTextSize(FONT_SIZE);
 		bottomEditText.setBackgroundColor(Color.argb(65, 139, 137, 137));	
 		bottomEditText.setMaxWidth(displayImage.getWidth());
 		
 
 		topEditText.addTextChangedListener(new TextWatcher() {
 
 			EditText topEditText = (EditText) findViewById(R.id.topInputText);
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 
 			// if font goes past max lines, decrement font and increase number
 			// of lines
 			// more lines of text at a smaller size now allowed to be typed in
 			@Override
 			public void afterTextChanged(Editable s) {
 				int currLineCount = topEditText.getLineCount();
 				if (currLineCount > topMaxLines) {
 					topMaxLines++;
 					topEditText.setMaxLines(topMaxLines);
 					topEditText.setTextSize((float) FONT_SIZE_UPDATE / topMaxLines);
 				}
 
 			}
 		});
 		// same as for the top EditText
 		bottomEditText.addTextChangedListener(new TextWatcher() {
 
 			EditText bottomEditText = (EditText) findViewById(R.id.bottomInputText);
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 
 			@Override
 			public void afterTextChanged(Editable s) {
 				int currLineCount = bottomEditText.getLineCount();
 				if (currLineCount > bottomMaxLines) {
 					bottomMaxLines++;
 					bottomEditText.setMaxLines(bottomMaxLines);
 					bottomEditText.setTextSize((float) FONT_SIZE_UPDATE / bottomMaxLines);
 				}
 
 			}
 		});
 
 	}
 
 	public String savePicture(byte[] bArray) {
 		File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
 		if (pictureFile == null) {
 			Log.d(PhotoEditActivity.TAG,
 					"Error creating media file, check storage permissions: ");
 			return "";
 		}
 		galleryAddPic(pictureFile);
 		try {
 			FileOutputStream fos = new FileOutputStream(pictureFile);
 			fos.write(bArray);
 			fos.close();
 
 		} catch (FileNotFoundException e) {
 			Log.d(TAG, "File not found: " + e.getMessage());
 		} catch (IOException e) {
 			Log.d(TAG, "Error accessing file: " + e.getMessage());
 		}
 		Toast.makeText(this, "Image saved!", Toast.LENGTH_SHORT).show();
 		return pictureFile.getAbsolutePath();
 	}
 
 	public void onClick(View v) {
 
 		switch (v.getId()) {
 		// wipes text off of the image
 		case (R.id.imgButton_delete): {
 			topEditText.setText("");
 			bottomEditText.setText("");
			break;
 		}
 		// get the image in a byte[]
 		case (R.id.imgButton_save): {
 			byte[] bArray = capturePic();
 			savePicture(bArray);
 			break;
 
 		}
 		case (R.id.imgButton_post): {
 			byte[] bArray = capturePic();
 			Intent mInDisplay = new Intent(PhotoEditActivity.this,
 					TestActivity.class);
 			mInDisplay.putExtra("testtest", bArray);
 			mInDisplay.putExtra("Path", savePicture(bArray));
 			startActivity(mInDisplay);
			break;
 		}
 
 		}
 	}
 
 	public byte[] capturePic() {
 		// disables visibility of cursor before it saves
 		topEditText.setCursorVisible(false);
 		bottomEditText.setCursorVisible(false);
 		topEditText.setBackgroundColor(0);
 		bottomEditText.setBackgroundColor(0);
 		topEditText.setAlpha((float)1);
 		bottomEditText.setAlpha((float)1);
 
 		// turns framelayout containgint edittexts ad imageview into a
 		// bitmap
 		// sends the bitmap to a new activity as proof that it is created
 		Bitmap imageToSave = fLayout.getDrawingCache();
 		ByteArrayOutputStream stream = new ByteArrayOutputStream();
 		imageToSave.compress(Bitmap.CompressFormat.PNG, 100, stream);
 		byte[] byteArray = stream.toByteArray();
 		
 
 		// enables cursor visibility for user
 		topEditText.setCursorVisible(true);
 		bottomEditText.setCursorVisible(true);
 		topEditText.setBackgroundColor(Color.LTGRAY);
 		bottomEditText.setBackgroundColor(Color.LTGRAY);
 		topEditText.setAlpha((float).5);
 		bottomEditText.setAlpha((float).5);
 
 		return byteArray;
 	}
 
 	public void galleryAddPic(File file) {
 
 		Uri contentUri = Uri.fromFile(file);
 		Intent mediaScanIntent = new Intent(
 				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
 		sendBroadcast(mediaScanIntent);
 
 	}
 
 	/** Create a File for saving a new picture */
 	private static File getOutputMediaFile(int type) {
 		Log.d(TAG, Environment.getExternalStorageState());
 
 		File mediaStorageDir = new File(
 				Environment
 						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
 				"InstaMeme");
 
 		// This location works best if you want the created images to be shared
 		// between applications and persist after your app has been uninstalled.
 
 		/** Errs if storage directory does not exist */
 		if (!mediaStorageDir.exists()) {
 			if (!mediaStorageDir.mkdirs()) {
 				Log.d("InstaMeme", "failed to create directory");
 				return null;
 			}
 		}
 
 		// Create a media file name
 		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
 				.format(new Date());
 		File mediaFile;
 		if (type == MEDIA_TYPE_IMAGE) {
 			mediaFile = new File(mediaStorageDir.getPath() + File.separator
 					+ "EDITED_IMG_" + timeStamp + ".jpg");
 		} else {
 			return null;
 		}
 		imagePath = mediaFile.getAbsolutePath();
 		return mediaFile;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.photo_edit, menu);
 		return true;
 	}
 
 }
