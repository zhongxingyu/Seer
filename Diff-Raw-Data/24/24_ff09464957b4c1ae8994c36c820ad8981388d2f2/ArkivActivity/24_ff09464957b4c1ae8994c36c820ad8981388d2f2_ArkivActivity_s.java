 package com.eprog.arkiv;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.hardware.Camera;
 import android.hardware.Camera.AutoFocusCallback;
 import android.hardware.Camera.Parameters;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Camera.ShutterCallback;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore;
 import android.provider.MediaStore.Images;
 import android.text.Editable;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ArkivActivity extends Activity implements SurfaceHolder.Callback {
 	private static final int SELECT_PROGRAM = 0;
 	private static final int SELECT_FROM_ARCHIVE = 1;
 	private Camera camera = null;
 	private static String folder = null;
 	private static String dirPath = null;
 	private static String filename = null;
 	private Uri selectedImageUri;
 	private ImageView imageView;
 	private SharedPreferences settings;
 	SurfaceView surface = null;
 	private SurfaceHolder holder = null;
 	
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 
 //        Boolean onTop = settings.getBoolean("PREF_BUTTONS_ON_TOP", true);
 //		if (onTop) {
 //			setContentView(R.layout.main);
 //		} else {
 //			setContentView(R.layout.main2);
 //		}
 		
         // Create folders
         createFolder("/sdcard/Arkiv/Intyg");
         createFolder("/sdcard/Arkiv/Ledighet");
         createFolder("/sdcard/Arkiv/Kvitto");
         createFolder("/sdcard/Arkiv/Faktura");
         createFolder("/sdcard/Arkiv/Other");        
         
 //		surface = (SurfaceView)findViewById(R.id.surface);
 //		holder = surface.getHolder();
 //        holder.addCallback(this);
 //        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 //        holder.setFixedSize(300, 200);
     }
     
     @Override
 	protected void onPause() {
     	Log.d("Arkiv", "onPause()");
 		super.onPause();
 		deactivateCamera();
 		holder.removeCallback(this);
 		surface = null;
 		
 		// TODO Save folder, path and filename, reinitiate them in onResume()
 	}
 
 	@Override
 	protected void onResume() {
 		Log.d("Arkiv", "onResume()");
 		super.onResume();
 		
 		Boolean statusbar = settings.getBoolean("PREF_STATUS_BAR", true);
 		if (statusbar) {
 			WindowManager.LayoutParams attrs = getWindow().getAttributes();
 	        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
 	        getWindow().setAttributes(attrs);
 		} else {
 			WindowManager.LayoutParams attrs = getWindow().getAttributes();
 	        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
 	        getWindow().setAttributes(attrs);
 		}
 		
 		surface = (SurfaceView)findViewById(R.id.surface);
 		holder = surface.getHolder();
         holder.addCallback(this);
         holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
         holder.setFixedSize(300, 200);
 		
 //		Boolean onTop = settings.getBoolean("PREF_BUTTONS_ON_TOP", true);
 //		if (onTop) {
 //			setContentView(R.layout.main);
 //		} else {
 //			setContentView(R.layout.main2);
 //		}
 	}
 
 
 	private void createFolder(String path) {
     	File folder = new File(path);
         boolean success = false;
         if(!folder.exists())
         {
             success = folder.mkdirs();
         }         
     }
     
     public void clickHandler(View view) {
 		switch (view.getId()) {
 		case R.id.buttonIntyg:
 			takePicture("Intyg");
 			break;
 		case R.id.buttonLedighet:
 			takePicture("Ledighet");
 			break;
 
 		case R.id.buttonKvitto:
 			takePicture("Kvitto");
 			break;
 
 		case R.id.buttonFaktura:
 			takePicture("Faktura");
 			break;
 
 		case R.id.buttonOther:
 			takePicture("Other");
 			break;
 
 		case R.id.buttonLog:
 	        Intent intent = new Intent();
             intent.setType("image/*");
             intent.setAction(Intent.ACTION_GET_CONTENT);
             startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.chooserTitle)), SELECT_FROM_ARCHIVE);
 			break;
 		}
     }
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode != RESULT_OK) {
 			// Operation was cancelled, reactivate camera if needed
 			activateCamera(holder);
 			return;
 		}
 		 
         Bitmap bitmap   = null;
         String path     = "";
  
         if (requestCode == SELECT_FROM_ARCHIVE) {
             selectedImageUri = data.getData();
             path = getRealPathFromURI(selectedImageUri); //from Gallery
  
             if (path == null)
                 path = selectedImageUri.getPath(); //from File Manager
  
             if (path != null)
                 bitmap  = BitmapFactory.decodeFile(path);
         }
         imageView.setImageBitmap(bitmap);
 	}
 	
 	public String getRealPathFromURI(Uri contentUri) {
         String [] proj      = {MediaStore.Images.Media.DATA};
         Cursor cursor       = managedQuery( contentUri, proj, null, null,null);
  
         if (cursor == null) {
         	return null;
         }
  
         int column_index    = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
         cursor.moveToFirst();
         return cursor.getString(column_index);
     }
 	
 	private void activateCamera(SurfaceHolder holder) {
 		Log.d("Arkiv", "activateCamera() camera = " + camera);
 		if (camera != null) {
 			deactivateCamera();
 		}
 		try {
 			camera = Camera.open();
 			camera.setPreviewDisplay(holder);
			camera.setDisplayOrientation(90);
//			camera.autoFocus(autoFocusCallback);
 			Parameters params = camera.getParameters();
 			params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
 			params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			params.setJpegQuality(80);
 			camera.setParameters(params);
 			camera.startPreview();						
 		} catch (IOException e) {
 			Log.d("Arkiv", e.getMessage());
 			deactivateCamera();
 		}
 	}
 	
 	private void deactivateCamera() {
 		if (camera != null) {
 			camera.stopPreview();
 			camera.release();
 			camera = null;
 		}
 	}
 
 	private void takePicture(String folder) {		
 		Log.d("Arkiv", "takePicture()");
 		this.folder = folder;
 	
 		if (camera != null) {
 			int sdk = android.os.Build.VERSION.SDK_INT;
 			if (sdk > 10) {
 				camera.takePicture(shutterCallback, rawCallback, jpegCallback);  // TODO Remove this special case when AutoFocus works on X10 mini with ICS. 
 			} else {
 				camera.autoFocus(new AutoFocusCallback() {
 
 					public void onAutoFocus(boolean success, Camera camera) {
 						Log.d("Arkiv", "onAutoFocus()");
 						if (success) {
 							camera.takePicture(shutterCallback, rawCallback, jpegCallback);
 						} else {
 							// Inform user that the camera could not focus.
 							Toast toast = Toast.makeText(getApplicationContext(), R.string.noFocus, Toast.LENGTH_SHORT);
 							toast.show();
 						}
 					}
 				});
 			}
 		}
 	}
 	
 	ShutterCallback shutterCallback = new ShutterCallback() {
 		public void onShutter() {
 		}
 	};
 	
 	PictureCallback rawCallback = new PictureCallback() {
 		public void onPictureTaken(byte[] data, Camera camera) {
 			
 		}
 	};
 	
 	PictureCallback jpegCallback = new PictureCallback() {
 		public void onPictureTaken(byte[] data, Camera camera) {
 			
 			Calendar c = Calendar.getInstance();
 	        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
 	        String formattedDate = df.format(c.getTime());
 	        
 			dirPath = "/sdcard/Arkiv/" + folder;
 	        filename = folder + formattedDate + ".jpg";
 			
 	        // Save the image to the right folder on the SD card
 			FileOutputStream outStream = null;
 			try {
 				outStream = new FileOutputStream(dirPath + "/" + filename);
 				outStream.write(data);
 				outStream.close();
 			} catch (FileNotFoundException e) {
 				Log.d("onPictureTaken", e.getMessage());
 			} catch (IOException e) {
 				Log.d("onPictureTaken", e.getMessage());
 			}
 			
 			// Insert image into Media Store
 	    	ContentValues content = new ContentValues(1);
 	    	content.put(Images.Media.MIME_TYPE, "image/jpg");
 	    	content.put(MediaStore.Images.Media.DATA, dirPath + "/" + filename);
 	    	
 	    	ContentResolver resolver = getContentResolver();
 	    	Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, content);
 	    	sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
 			
 	    	// Check if subcategory dialog shall be shown
 	    	Boolean subCategories = settings.getBoolean("PREF_SUB_CATEGORIES", true);
 	    	if (subCategories) {
 	    		showSubCategoryDialog();
 	    	} else {
 	    		sendMail(folder, dirPath, filename);
 	    		camera.startPreview();  // TODO Is this needed?
 	    	}
 		}
 	};
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		Log.d("Arkiv", "surfaceChanged()");
 	}
 
 	public void surfaceCreated(SurfaceHolder holder) {
 		Log.d("Arkiv", "surfaceCreated()");
 		activateCamera(holder);
 	}
 	
//	AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {
//
//		public void onAutoFocus(boolean arg0, Camera arg1) {
//			// TODO Auto-generated method stub
//		}
//	};

 	public void surfaceDestroyed(SurfaceHolder holder) {
 		Log.d("Arkiv", "surfaceDestroyed()");
 		deactivateCamera();
 	}
 	
 	private void sendMail(String type, String folder, String filename) {
 		
 		if (filename == null) {
 			Log.d("Arkiv", "sendMail(): filename == null");
 			return;
 		}
 		// Check if mail shall be sent
 		Boolean sendMail = settings.getBoolean("PREF_SEND_MAIL", false);
 		String emailAddress = settings.getString("PREF_EMAIL_ADDRESS", null);
 		if (!sendMail || emailAddress == null) {
 			return;
 		}
 		
         // Get sub-category
         String sub = settings.getString(Settings.PREF_SELCETED_SUB_CATEGORY, "");
 				
 		Uri uri = Uri.fromFile(new File(folder, filename));
 		
 		Intent sendIntent = new Intent(Intent.ACTION_SEND);
 		sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.emailBody));
 		if (sub != null && !sub.equals("")) {
 			sendIntent.putExtra(Intent.EXTRA_SUBJECT, "[" + type + "] [" + sub + "] " + filename);
 		} else {
 			sendIntent.putExtra(Intent.EXTRA_SUBJECT, "[" + type + "] " + filename);
 		}
 		sendIntent.putExtra(Intent.EXTRA_EMAIL,
 				new String[] { emailAddress }); 
 		sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
 		sendIntent.setType("plain/txt");
 		startActivityForResult(Intent.createChooser(sendIntent, getResources().getString(R.string.chooserTitle)), SELECT_PROGRAM);
 	}
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.arkiv_menu, menu);
 		
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 
 		switch (item.getItemId()) {
 		case R.id.menuPreferences:
 			startActivity(new Intent(this, Settings.class));
 			break;
 		case R.id.menuHelp:
 		{
 			AlertDialog helpDialog = new AlertDialog.Builder(this).create();
 			Window w = helpDialog.getWindow();
 			w.setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
 			helpDialog.setTitle(R.string.helpTitle);
 			helpDialog.setMessage(getResources().getText(R.string.helpText));
 			helpDialog.show();
 		}
 		break;
 		case R.id.menuAbout:
 		{
 			AlertDialog aboutDialog = new AlertDialog.Builder(this).create();
 			Window w = aboutDialog.getWindow();
 			w.setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
 			aboutDialog.setTitle(R.string.aboutTitle);
 			aboutDialog.setMessage(getResources().getText(R.string.aboutText));
 			aboutDialog.show();
 		}
 		break;
 		}
 		return true;
 	}
 	
 	private View dialoglayout = null;
 	
 	private void showSubCategoryDialog() {
 	
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(R.string.subCategoryTitle);
 		LayoutInflater inflater = getLayoutInflater();
 		dialoglayout = inflater.inflate(R.layout.subcategory, (ViewGroup) getCurrentFocus());
 		builder.setView(dialoglayout);
 		AlertDialog dialog = builder.create();
 		Window w = dialog.getWindow();
 		w.setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
 		
 		TextView text = (TextView)dialoglayout.findViewById(R.id.subCategoryDescription);
 		text.setText(R.string.subCategoryText);
 		
 		Spinner spinCategory = (Spinner) dialoglayout.findViewById(R.id.subCategorySpinner);
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		int count = settings.getInt(Settings.PREF_CATEGORY1_SUB_COUNT, 0);
 		for (int i = 0; i < count; i++) {
 			adapter.add(settings.getString(Settings.PREF_CATEGORY1_SUB + i, ""));
 		}
 		spinCategory.setAdapter(adapter);
 		
 		builder.setPositiveButton("OK", new OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int which) {
 				Editor editor = settings.edit();
 				// Get sub-category
 				EditText text = (EditText)dialoglayout.findViewById(R.id.editSubCategory);
 				Editable newCategory = text.getText();
 				
 				if (newCategory.length() > 0) {
 					editor.putString(Settings.PREF_SELCETED_SUB_CATEGORY, newCategory.toString());
 					// Save new category
 					int count = settings.getInt(Settings.PREF_CATEGORY1_SUB_COUNT, 0);
 					editor.putString(Settings.PREF_CATEGORY1_SUB + count, newCategory.toString());
 					editor.putInt(Settings.PREF_CATEGORY1_SUB_COUNT, count + 1);
 				} else {
 					Spinner spinCategory = (Spinner) dialoglayout.findViewById(R.id.subCategorySpinner);
 					editor.putString(Settings.PREF_SELCETED_SUB_CATEGORY, (String) spinCategory.getSelectedItem());
 				}
 				editor.commit();
 				sendMail(folder, dirPath, filename);
 	    		camera.startPreview();  // TODO Is this needed?
 			}
 		});
 		
 		builder.show();
 	}
 	
 }
