 package com.codesortium.photoswithnotes.activity;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 
 import com.codesortium.photoswithnotes.R;
 import com.codesortium.photoswithnotes.db.Photo;
 import com.codesortium.photoswithnotes.db.PhotoCategory;
 import com.codesortium.photoswithnotes.model.DatabaseModel;
 import com.codesortium.photoswithnotes.view.ChoosePhotoForm;
 import com.google.inject.Inject;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import roboguice.activity.RoboFragmentActivity;
 
 public class ChoosePhotoActivity extends RoboFragmentActivity
 {
 	private static final String TAG = ChoosePhotoActivity.class.getSimpleName();
 	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
 	private static final int SELECT_IMAGE_ACTIVITY_REQUEST_CODE = 200;
 
 	@Inject
 	DatabaseModel model;
 	ChoosePhotoForm view;
 
 	private Photo photo;
 	private Uri fileUri;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		Log.d(TAG, "onCreate(): " + photo);
 
 		super.onCreate(savedInstanceState);
 
 		view = (ChoosePhotoForm) View.inflate(this, R.layout.choose_photo_form, null);
 		setContentView(view);
 
 		view.onSaveClick.add(this, "onSaveClick");
 
 		if(model.getAddingPhoto() == null)
 		{
 			photo = new Photo();
 			photo.setTitle("Photo Title");
 			photo.setNotes("Photo Notes.");
 
 			model.setAddingPhoto(photo);
 		}
 		else
 		{
 			photo = model.getAddingPhoto();
 
 			if(photo.getUrl() != null)
 			{
				fileUri = Uri.parse(photo.getUrl());//.fromFile(new File(photo.getUrl()));
 				view.setImage(fileUri);
 			}
 		}
 	}
 
 	public void onSaveClick()
 	{
 		if(fileUri != null)
 		{
 			Log.d(TAG, "Image Saved Uri: " + photo.getUrl());
 
 			model.saveAddingPhoto();
 			model.setEditingPhoto(photo);
 
 			Intent intent = new Intent(this, EditNotesActivity.class);
 			this.startActivityForResult(intent,0);
 		}
 		else
 		{
 			Log.d(TAG, "Have not selected an image, so cannot save.");
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		getMenuInflater().inflate(R.menu.choose_photo, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch(item.getItemId())
 		{
 			case R.id.action_camera:
 				startCamera();
 				return true;
 
 			case R.id.action_choose_image:
 				startImageSelect();
 				return true;
 
 			case R.id.action_settings:
 				return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void startCamera()
 	{
 		// create Intent to take a picture and return control to the calling application
 		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 
 		fileUri = getOutputMediaFileUri(); // create a file to save the image
 
 		if(fileUri != null)
 		{
 			intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name.
 			startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE); // start the image capture Intent
 		}
 		else
 		{
 			Log.d(TAG, "Unable to create a file.");
 		}
 	}
 
 	private void startImageSelect()
 	{
 		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
 		photoPickerIntent.setType("image/*");
 		startActivityForResult(photoPickerIntent, SELECT_IMAGE_ACTIVITY_REQUEST_CODE);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent)
 	{
 		Log.d(TAG, "onActivityResult() fired: " + resultCode);
 
 		switch(requestCode)
 		{
 			case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
 				if(resultCode == RESULT_OK)
 				{
 					Log.d(TAG, "Image captured and saved to fileUri specified in the Intent. " + fileUri);
					//Uri.parse();
					photo.setUrl(fileUri.toString());
 					view.setImage(fileUri);
 				}
 				else if(resultCode == RESULT_CANCELED)
 				{
 					// User cancelled the image capture
 				}
 				else
 				{
 					// Image capture failed, advise user
 				}
 				break;
 
 			case SELECT_IMAGE_ACTIVITY_REQUEST_CODE:
 				if(resultCode == RESULT_OK)
 				{
					Log.d(TAG, "Image selected from the gallery.: " + intent.getData());
 
 					fileUri = (Uri) intent.getData();
 
					if(fileUri != null)
					{
						photo.setUrl(fileUri.toString());
						view.setImage(fileUri);
					}
					else
					{
						Log.d(TAG, "There was an error getting the Uri of the image.");
					}
 				}
 				break;
 		}
 
 	}
 
 	/**
 	 * Create a file Uri for saving an image or video
 	 */
 	private static Uri getOutputMediaFileUri()
 	{
 		return Uri.fromFile(getOutputMediaFile());
 	}
 
 	/**
 	 * Create a File for saving an image or video
 	 */
 	private static File getOutputMediaFile()
 	{
 		String state = Environment.getExternalStorageState();
 		Boolean mExternalStorageAvailable = false;
 		Boolean mExternalStorageWriteable = false;
 
 		if(Environment.MEDIA_MOUNTED.equals(state))
 		{
 			Log.d(TAG, "We can read and write the media.");
 			mExternalStorageAvailable = mExternalStorageWriteable = true;
 		}
 		else if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
 		{
 			Log.d(TAG, "We can only read the media.");
 			mExternalStorageAvailable = true;
 			mExternalStorageWriteable = false;
 		}
 		else
 		{
 			Log.e(TAG, "Something is wrong. It may be one of many other states, but all we need to know is we can neither read nor write.");
 			mExternalStorageAvailable = mExternalStorageWriteable = false;
 		}
 
 		if(mExternalStorageWriteable)
 		{
 			File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "PhotosWithNotes");
 
 			if(!mediaStorageDir.exists())
 			{
 				if(!mediaStorageDir.mkdirs())
 				{
 					Log.d(TAG, "failed to create directory on external storage. " + mediaStorageDir.getAbsoluteFile());
 					return null;
 				}
 			}
 
 			// Create a media file name
 			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
 			File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
 			return mediaFile;
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 	private String getRealPathFromURI(Uri uri)
 	{
 		Cursor cursor = null;
 		try
 		{
 			String[] proj = {MediaStore.Images.Media.DATA};
 			cursor = getApplicationContext().getContentResolver().query(uri, proj, null, null, null);
 			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 			cursor.moveToFirst();
 			return cursor.getString(column_index);
 		}
 		finally
 		{
 			if(cursor != null)
 			{
 				cursor.close();
 			}
 		}
 	}
 }
