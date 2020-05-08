 package edu.ua.moundville;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.concurrent.ExecutionException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import edu.ua.moundville.DBHandler.DBResult;
 
 public abstract class Article extends Activity implements DBResult {
 
	protected final String URL = "http://betatesting.as.ua.edu/mapexperience/images";
 	protected String primaryImageSubUrl;
 	protected ImageView primaryImage;
 	protected DBHandler db = new DBHandler();
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
         setContentView(R.layout.article);
 	}
 	
 	protected abstract void prepareContent();
 	
 	protected void displayImage() {
 		primaryImage = (ImageView)findViewById(R.id.primary_image);
 		/* Check for a valid image URL. If not, hide the ImageView */
 		if (primaryImageSubUrl == null || primaryImageSubUrl.matches("^(null)?$")) {
 			primaryImage.setVisibility(View.GONE);	
 		} else {
 			primaryImage = (ImageView)findViewById(R.id.primary_image);
 			final FetchImageTask imageFetcher = new FetchImageTask();
			imageFetcher.execute(URL + "/" + primaryImageSubUrl);
 			
 			primaryImage.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					Intent launchActivity = new Intent(v.getContext(), ImageViewer.class);
 					launchActivity.putExtra("image",imageFetcher.imageFile);
 					startActivity(launchActivity);
 				}
 			});
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.default_menu, menu);
 	    return true;
 	}    
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	        case R.id.home:
 	            // app icon in action bar clicked; go home
 	            Intent intent = new Intent(this, MoundvilleActivity.class);
 	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 	            startActivity(intent);
 	            return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}    
 	
 	protected abstract void addFieldToLayout(LinearLayout layout, String text);
 	
 	protected class FetchImageTask extends AsyncTask<String, Integer, Bitmap> {
 		public String imageFile;
 	    @Override
 	    protected Bitmap doInBackground(String... arg0) {
 	    	Bitmap b = null;
 	    	try {
 				 File temp = File.createTempFile("image", "", getFilesDir());
 				 imageFile = temp.getAbsolutePath();
 				 
 				 OutputStream out = new FileOutputStream(temp);
 				 InputStream in = new URL(arg0[0]).openStream();
 				 
 				 byte[] buffer = new byte[1024];
 				 int length;
 				 while ((length = in.read(buffer)) > 0){
 					 out.write(buffer, 0, length);
 				 }
 				 
 				 in.close();
 				 out.close();
 				 
 				 b = BitmapFactory.decodeFile(imageFile);
 				 
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} 
 	        return b;
 	    }	
 	    protected void onPostExecute(Bitmap result) {
 	    	if (result != null) {
 	    		primaryImage.setImageBitmap(result);
 	    	}
 	    }
 	}
 }
