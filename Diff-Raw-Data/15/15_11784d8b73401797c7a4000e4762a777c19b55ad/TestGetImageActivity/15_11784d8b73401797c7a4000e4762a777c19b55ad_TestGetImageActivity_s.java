  package se.mah.kd330a.project.find.data;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 
 import se.mah.kd330a.project.R;
 import se.mah.kd330a.project.R.layout;
 import se.mah.kd330a.project.R.menu;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class TestGetImageActivity extends Activity {
	private long startTime; 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_test_get_image);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.test_get_image, menu);
 		return true;
 	}
 	
 	public void load(View v){
 		//Use GetImage.doesFileExists(String filename,Context c ) to check if a file is stored locally
 		//Use Bitmap getImageFromLocalStorage(String filename, Context c) to load Bitmap from local storage
 		//Use  Bitmap getImageFromNet(String filename, boolean storeImageLocally, Context c){ should be enclosed in AsyncTask
 		//Say we want 5 picts do like this
 		new DownloadFilesTask().execute("k2.jpg","k2e1.jpg","k2e1cdirectleft.jpg","k2e1cdirectright.jpg","k2e2.jpg");
 	}
 	
 	public void delete(View v){
 		GetImage.deletefile("k2e1.jpg", this);
 		GetImage.deletefile("k2e1.jpg", this);
 		GetImage.deletefile("k2e1cdirectleft.jpg",this);
 		GetImage.deletefile("k2e1cdirectright.jpg",this);
 		GetImage.deletefile("k2e2.jpg",this);
 	}
 	
 	private class DownloadFilesTask extends AsyncTask<String, Void, Void> {
 	     protected Void doInBackground(String... fileNames) {
 	    	 startTime = System.currentTimeMillis(); //start timer
 	        CheckBox c = (CheckBox)TestGetImageActivity.this.findViewById(R.id.checkBox1);
 	         for (int i = 0; i < fileNames.length; i++) {
 	        	 	Log.i("UserInfo","Image: "+fileNames[i] +" to download");
 	        	 	if (!GetImage.doesFileExists(fileNames[i],TestGetImageActivity.this)){
 	        	 		Log.i("UserInfo","fileNames[i] not in local storage lets download from Net");
 	        	 		GetImage.getImageFromNet(fileNames[i], c.isChecked(), TestGetImageActivity.this);
 	        	 	}else{
 	        	 		
 	        	 	}
 	         }
 	         return null;
 	     }
 	     @Override
 	    protected void onProgressUpdate(Void... b) {
 
 	    }
 
 	     protected void onPostExecute(Void v) {
 	    	 long difference = System.currentTimeMillis() - startTime;
 	    	 TextView tv = (TextView)findViewById(R.id.infotextView);
	    	 tv.setText("All images downloaded to local storage it took: " + difference + "millisec");
	    	 //Show one Image
	    	 Bitmap bitmap = GetImage.getImageFromLocalStorage("k2e2.jpg", TestGetImageActivity.this);
	    	 ImageView iv = (ImageView)findViewById(R.id.imageView1);
	    	 iv.setImageBitmap(bitmap);
 	     }
 	 }
 
 
 }
