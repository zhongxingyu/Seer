 package ca.ualberta.cs.completemytask.activities;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 
 import ca.ualberta.cs.completemytask.R;
 import ca.ualberta.cs.completemytask.background.BackgroundTask;
 import ca.ualberta.cs.completemytask.background.HandleInBackground;
 import ca.ualberta.cs.completemytask.database.DatabaseManager;
 import ca.ualberta.cs.completemytask.saving.LocalSaving;
 import ca.ualberta.cs.completemytask.settings.Settings;
 import ca.ualberta.cs.completemytask.userdata.ImageAdapter;
 import ca.ualberta.cs.completemytask.userdata.MyPhoto;
 import ca.ualberta.cs.completemytask.userdata.Task;
 import ca.ualberta.cs.completemytask.userdata.TaskManager;
 import ca.ualberta.cs.completemytask.userdata.User;
 import ca.ualberta.cs.completemytask.views.LoadingView;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 //import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Gallery;
 import android.widget.ImageView;
 
 /**
  * 
  * 
  * @author Devon Waldon
  */
 public class ViewImageActivity extends Activity {
 
 	private static final int CAPTURE_IMAGE_REQUEST_CODE = 100;
 	//private static final String TAG = "ViewImageActivity";
 	Gallery photoGallery;
 	ImageAdapter adapter;
 	ImageView imagePreview;
 	private Task task;
 	Uri imageFileUri;
 	
 	private LoadingView loadingView;
 	public LocalSaving saver;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_image);
 		
 		saver = new LocalSaving();
 		
 		this.loadingView = new LoadingView(this, R.id.ViewImageMain,
         		"Getting Image ...");
 
 		int position = TaskManager.getInstance().getCurrentTaskPosition();
 		task = TaskManager.getInstance().getTaskAt(position);
 
 		photoGallery = (Gallery) findViewById(R.id.ImageGallery);
 		imagePreview = (ImageView) findViewById(R.id.TaskImageView);
 		
 		adapter = new ImageAdapter(this, task);
 
 		photoGallery.setAdapter(adapter);
 		adapter.notifyDataSetChanged();
 		
 		photoGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 				updateImagePreview(position);
 			}
 		});
 		
 		BackgroundTask bg = new BackgroundTask();	
     	bg.runInBackGround(new HandleInBackground() {
     		public void onPreExecute() {
     			loadingView.showLoadView(true);
     		}
     		
     		public void onPostExecute(int response) {
     			if (task.getNumberOfPhotos() > 0) {
     				imagePreview.setImageBitmap(task.getPhotoAt(0).getContent());
     			}
     			
     			adapter.notifyDataSetChanged();
     			
     			loadingView.showLoadView(false);
     		}
     		
     		public void onUpdate(int response) {
     		}
     		
     		public boolean handleInBackground(Object o) {	
     			DatabaseManager.getInstance().getPhotos(task);
 				return true;
     		}
     	});
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_view_image, menu);
 		return true;
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		adapter.notifyDataSetChanged();
 	}
 	
 	/**
 	 * Updates the image preview to show the image selected in the gallery.
 	 */
 	public void updateImagePreview(int position){
 		Bitmap selectedImage = task.getPhotoAt(position).getContent();
 		imagePreview.setImageBitmap(selectedImage);
 	}
 
 	/**
 	 * Calls for the camera to take a picture and save it to local storage
 	 * @param view
 	 */
 	public void takePhoto(View view){
 		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 
 		String folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp";
 		File folderF = new File(folder);
 		if (!folderF.exists()) {
 			folderF.mkdir();
 		}
 
 		String imageFilePath = folder + "/" + String.valueOf(System.currentTimeMillis()) +".jpg";
 		File imageFile = new File(imageFilePath);
 		imageFileUri = Uri.fromFile(imageFile);
 
 		intent.putExtra(MediaStore.ACTION_IMAGE_CAPTURE, imageFileUri);
 		startActivityForResult(intent, CAPTURE_IMAGE_REQUEST_CODE);
 	}
 
 	/**
 	 * Gets the bitmap out of an intent. 
 	 * @param intent
 	 * @return a bitmap that was carried by the intent
 	 */
 	private Bitmap getBitmap(Intent intent) {
 		Bundle extras = intent.getExtras();
 		Bitmap newPhoto = (Bitmap) extras.get("data");
 		
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		newPhoto.compress(CompressFormat.PNG, 50, out);
 		Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
 		
 		int width, height;
 		double ratio;
 		
 		int size = 320;
 		
 		if (bitmap.getWidth() > bitmap.getHeight()) {
 			width = size;
 			ratio = width/bitmap.getWidth();
 			height = (int)(bitmap.getHeight()*ratio);
 		} else {
 			height = size;
 			ratio = height/bitmap.getHeight();
 			width = (int)(bitmap.getWidth()*ratio);
 		}
 		
 		bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
 		
 		return bitmap;
 	}
 
 	/**
 	 * Adds the bitmap 'b' to both the task and the ImageView gallery
 	 * @param b
 	 */
 	private void addImage(Bitmap b){
 		
 		MyPhoto image = new MyPhoto();
 		image.setContent(b);
 
 		User user = null;
 
 		if (Settings.getInstance().hasUser()) {
 			user = Settings.getInstance().getUser();
 		} else {
 			user = new User("Unknown");
 		}
 
 		image.setUser(user);
 		image.setLocalParentId(task.getLocalId());
 		image.setParentId(task.getId());
 		sync(image);
 	}
 
 	/**
 	 * Syncs the image to the task (copied from CommentActivity, 12/11/01)
 	 * @param image
 	 */
 	public void sync(final MyPhoto image) {
 		BackgroundTask bg = new BackgroundTask();
     	bg.runInBackGround(new HandleInBackground() {
     		public void onPreExecute() {
     			loadingView.showLoadView(true);
     		}
     		
     		public void onPostExecute(int response) {
     			loadingView.showLoadView(false);
 				adapter.notifyDataSetChanged();
     		}
     		
     		public void onUpdate(int response) {
     		}
     		
     		public boolean handleInBackground(Object o) {
     			
     			if (task.isPublic()) {
     				DatabaseManager.getInstance().syncPhoto(image);
     			}
 				
 				if (task.isLocal()) {
 					saver.open();
 					saver.savePhoto(image);
 					saver.close();

 				}
 				
 				task.addPhoto(image);
 				
 				return true;
     		}
     	});
 	}
 
 	/**
 	 * Checks the returned activities for a captured image, then adds the image
 	 * to the current task
 	 */
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent){
 		if(requestCode == CAPTURE_IMAGE_REQUEST_CODE){
			Bitmap b = getBitmap(intent);
			imagePreview.setImageBitmap(b);
			addImage(b);
 		}
 	}
 
 	/**
 	 * Closes the task
 	 * @param view
 	 */
 	public void close(View view) {
 		this.finish();
 	}
 }
