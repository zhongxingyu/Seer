 package cc.hughes.droidchatty;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import cc.hughes.droidchatty.legacy.LegacyFactory;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 
 public class ComposePostView extends Activity {
 
 	protected static final int SELECT_IMAGE = 0;
 	protected static final int TAKE_PICTURE = 1;
 	
 	static final long MAX_SIZE_LOGGED_IN = 6 * 1024 * 1024;
 	static final long MAX_SIZE_NOT_LOGGED_IN = 3 * 1024 * 1024;
 	
     private int _replyToPostId = 0;
 	private ProgressDialog _progressDialog;
 	
 	Uri _cameraImageLocation;
 	
 	SharedPreferences _prefs;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
         super.onCreate(savedInstanceState);
         setContentView(R.layout.edit_post);
         
         // grab the post being replied to, if this is a reply
         Bundle extras = getIntent().getExtras();
         if (extras != null && extras.containsKey(SingleThreadView.THREAD_ID))
             _replyToPostId = getIntent().getExtras().getInt(SingleThreadView.THREAD_ID);
         
         Button reply_button = (Button)findViewById(R.id.replyButton);
         Button cancel_button = (Button)findViewById(R.id.cancelButton);
         ImageButton camera_button = (ImageButton)findViewById(R.id.takePicture);
         ImageButton gallery_button = (ImageButton)findViewById(R.id.uploadPicture);
         
         if (_replyToPostId == 0)
             setTitle(getTitle() + " - New Post");
         else
             setTitle(getTitle() + " - Reply");
         
         reply_button.setOnClickListener(onPostButtonClick);
         cancel_button.setOnClickListener(new OnClickListener()
         {
     		@Override
     		public void onClick(View v)
     		{
     		    finish();
     		}
         });
         camera_button.setOnClickListener(onCameraButtonClick);
         gallery_button.setOnClickListener(onGalleryButtonClick);
         
         // if they have no camera, turn off the camera button
         if (!LegacyFactory.getLegacy().hasCamera(this))
             camera_button.setVisibility(View.INVISIBLE);
             
         _prefs = PreferenceManager.getDefaultSharedPreferences(this);
         
         if (savedInstanceState == null)
         {
             String userName = _prefs.getString("userName", "");
             
             if (userName.length() == 0)
             {
                 ErrorDialog.display(this, "Error", "You must set your username before you can post.");
                 return;
             }
         }
         else
         {
             // we kinda need this
             if (savedInstanceState.containsKey("cameraImageLocation"))
                 _cameraImageLocation = Uri.parse(savedInstanceState.getString("cameraImageLocation"));
         }
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState)
 	{
 	    super.onSaveInstanceState(outState);
 	    
 	    EditText edit = (EditText)findViewById(R.id.textContent);
 	    outState.putString("postContent", edit.getText().toString());
 	    if (_cameraImageLocation != null)
     	    outState.putString("cameraImageLocation", _cameraImageLocation.toString());
 	}
 	
 	void appendText(String text)
 	{
 	    EditText edit = (EditText)findViewById(R.id.textContent);
 	    
 	    // if there is text in there, put the image on a new line
 	    if (edit.length() > 0 && !edit.getText().toString().endsWith(("\n")))
 	        text = "\n" + text;
 	    
 	    edit.append(text + "\n");
 	}
 	
 	OnClickListener onPostButtonClick = new OnClickListener()
 	{
 		@Override
 		public void onClick(View v)
 		{
 		    // grab the content to post
 		    EditText et = (EditText)findViewById(R.id.textContent);
 		    String content = et.getText().toString();
 		    
 		    // post in the background
 		    _progressDialog = ProgressDialog.show(ComposePostView.this, "Posting", "Attempting to post...");
 		    new PostTask().execute(content);
 		}
 	};
 	
 	OnClickListener onCameraButtonClick = new OnClickListener()
 	{
 		@Override
 		public void onClick(View v)
 		{
 		    // store our image in a temp spot
 		    String state = Environment.getExternalStorageState();
 		    if (Environment.MEDIA_MOUNTED.equals(state))
 		    {
 		        // application directory, per Android Data Storage guidelines
 		        final String APP_DIRECTORY = "/Android/data/cc.hughes.droidchatty/files/";
 		        File app_dir = new File(Environment.getExternalStorageDirectory(), APP_DIRECTORY);
 		        
 		        // make sure the directory exists
 		        if (!app_dir.exists())
 		        {
     		        if (!app_dir.mkdirs())
     		        {
         		        ErrorDialog.display(ComposePostView.this, "Error", "Could not create application directory.");
         		        return;
     		        }
 		        }
 		        
 		        // our temp file for taking a picture, delete if we already have one
     		    File file = new File(app_dir, "droidchatty.jpg");
     		    if (file.exists())
     		        file.delete();
     		    
     		    _cameraImageLocation = Uri.fromFile(file);
     		    
     		    // start the camera
     		    Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
     		    i.putExtra(MediaStore.EXTRA_OUTPUT, _cameraImageLocation);
     		    startActivityForResult(i, TAKE_PICTURE);
 		    }
 		    else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
 		    {
 		        ErrorDialog.display(ComposePostView.this, "Error", "External storage is mounted as read only.");
 		    }
 		    else
 		    {
 		        ErrorDialog.display(ComposePostView.this, "Error", "External storage is in an unknown state.");
 		    }
 		}
 	};
 	
 	OnClickListener onGalleryButtonClick = new OnClickListener()
 	{
 		@Override
 		public void onClick(View v)
 		{
 		    // startup the image picker!
             startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), SELECT_IMAGE);
 		}
 	};
 	
 	@Override
     public void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
         super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == SELECT_IMAGE)
         {
             if (resultCode == Activity.RESULT_OK)
             {
                 Uri selectedImage = data.getData();
                 String realPath = getRealPathFromURI(selectedImage);
                 uploadImage(realPath);
             } 
         }
         else if (requestCode == TAKE_PICTURE)
         {
             if (resultCode == Activity.RESULT_OK)
             {
                 // picture was taken, and resides at the location we specified
                 uploadImage(_cameraImageLocation.getPath());
             }
         }
 	}
 	
 	// convert the image URI to the direct file system path of the image file
     public String getRealPathFromURI(Uri contentUri) {
 
         String [] proj= { MediaStore.Images.Media.DATA };
         Cursor cursor = managedQuery(contentUri, proj, null, null, null);
         int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
         cursor.moveToFirst();
 
         return cursor.getString(column_index);
 }
 	
 	void uploadImage(String imageLocation)
 	{
 	    _progressDialog = ProgressDialog.show(ComposePostView.this, "Upload", "Uploading image to chattypics");
 	    new UploadAndInsertTask().execute(imageLocation);
 	}
 	
 	void postSuccessful(int postId)
 	{
 	    Intent reply = new Intent();
 	    if (postId > 0)
 		    reply.putExtra("postId", postId);
 	    setResult(RESULT_OK, reply);
 	    
 	    // lets get the hell out of here!
 	    finish();
 	}
 	
 	class PostTask extends AsyncTask<String, Void, Integer>
 	{
 	    Exception _exception;
 	    
         @Override
         protected Integer doInBackground(String... params)
         {
             try
             {
                 String content = params[0];
             
                 int reply_id = ShackApi.postReply(ComposePostView.this, _replyToPostId, content);
 	    
                 return new Integer(reply_id);
             }
             catch (Exception e)
             {
                 Log.e("DroidChatty", "Error posting reply", e);
                 _exception = e;
                 return null;
             }
         }
 
         @Override
         protected void onPostExecute(Integer result)
         {
             _progressDialog.dismiss();
             
             if (_exception != null)
                 ErrorDialog.display(ComposePostView.this, "Error", "Error posting:\n" + _exception.getMessage());
             else
                 postSuccessful(result.intValue());
         }
         
 	}
 	
 	class UploadAndInsertTask extends AsyncTask<String, Void, String>
 	{
 	    Exception _exception;
 	    
         @Override
         protected String doInBackground(String... params)
         {
             try
             {
                 String imageLocation = params[0];
                 
                 // resize the image for faster uploading
                 String smallImageLocation = resizeImage(imageLocation);
                 
                 String userName = _prefs.getString("chattyPicsUserName", null);
                 String password = _prefs.getString("chattyPicsPassword", null);
                 
                 // attempt to log in so the image will appear in the user's gallery
                 String login_cookie = null;
                 if (userName != null && password != null)
                     login_cookie = ShackApi.loginToUploadImage(userName, password);
                 
                 // actually upload the thing
                 String content = ShackApi.uploadImage(smallImageLocation, login_cookie);
                 
                 // if the image was resized, delete the small one
                 if (imageLocation != smallImageLocation)
                 {
                     try
                     {
                         File file = new File(smallImageLocation);
                         file.delete();
                     }
                     catch (Exception ex)
                     {
                         Log.e("DroidChatty", "Error deleting resized image", ex);
                     }
                 }
                 
                Pattern p = Pattern.compile("http\\:\\/\\/chattypics\\.com\\/viewer\\.php\\?file=(.*?\\.jpg)");
                 Matcher match = p.matcher(content);
                                 
                 if (match.find())
                     return "http://chattypics.com/files/" + match.group(1);
                 
                 return null;
             }
             catch (Exception e)
             {
                 Log.e("DroidChatty", "Error posting reply", e);
                 _exception = e;
                 return null;
             }
         }
         
         String resizeImage(String path) throws Exception
         {
             final int MAXIMUM_SIZE = 1024;
             
             // get the original image
             Bitmap original = BitmapFactory.decodeFile(path);
             float scale = Math.min((float)MAXIMUM_SIZE / original.getWidth(), (float)MAXIMUM_SIZE / original.getHeight());
             
             // work around for older devices that don't support EXIF
             int rotation = LegacyFactory.getLegacy().getRequiredImageRotation(path);
             
             Matrix matrix = new Matrix();
             matrix.postScale(scale, scale);
             matrix.postRotate(rotation);
             
             Bitmap resized = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
             
             // generate the new image
             File file = new File(path);
             File newFile = getFileStreamPath(file.getName());
             
             // save the image
             FileOutputStream f = new FileOutputStream(newFile);
             try
             {
                 resized.compress(CompressFormat.JPEG, 80, f);
             }
             finally
             {
                 f.close();
             }
             
             return newFile.getAbsolutePath();
         }
 
         @Override
         protected void onPostExecute(String result)
         {
             _progressDialog.dismiss();
             
             if (_exception != null)
                 ErrorDialog.display(ComposePostView.this, "Error", "Error posting:\n" + _exception.getMessage());
             else if (result == null)
                 ErrorDialog.display(ComposePostView.this, "Error", "Couldn't find image URL after uploading.");
             else
                 appendText(result);
         }
         
 	}
 	
 }
