 package org.gtug.trafficcam;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.content.res.TypedArray;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.media.MediaScannerConnection;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.text.format.Time;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 
 public class TrafficCamIntent extends Activity 
 {
 	public static final String PREFS_NAME = "MyPrefsFile";
 	private ArrayList<Drawable> pics = new ArrayList<Drawable>();
 	final Context c = getBaseContext();
 	private int fetchNumber = 1;
 	private int spinnerPos;
 	private Gallery g;
 	private TextView tV;
 	private int visiCount = 0;
 	private Boolean singleFile;
 	private SynchronisedCounter syncCount = new SynchronisedCounter();
 	private final SynchronisedPics syncPics = new SynchronisedPics();
 
     /** Called when the activity is first created. **/
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 		{
 			// Request progress bar
 			requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 			setProgressBarIndeterminateVisibility(true);
 			setContentView(R.layout.main);
 			this.drawInterface();
 			super.onCreate(savedInstanceState);
 			g = (Gallery) findViewById(R.id.gallery);
 		}
 	@Override
 	protected void onResume()
 		{
 			g = (Gallery) findViewById(R.id.gallery);
 			clearInternalFiles();
 			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 			if(fetchNumber != settings.getInt("fetchNumber", 2)||singleFile !=settings.getBoolean("singleFile", false))
 			{	
 				this.drawInterface();
 			}
 			super.onResume();
 		}
 	
 	protected void drawInterface()
 		{
 			// Request progress spinner
 			
 			//Define Camera Name Spinner
 			Spinner s = (Spinner)findViewById(R.id.Spinner01);
 			//Restore last selection
 			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 			spinnerPos = settings.getInt("spinnerPos", 0);
 	
 			//Set listener for Spinner
 			MyOnItemSelectedListener l = new MyOnItemSelectedListener();
 			s.setOnItemSelectedListener(l);
 		    
 			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.cameras_array, android.R.layout.simple_spinner_item);
 			adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
 			s.setAdapter(adapter);
 			s.setSelection(spinnerPos);
 			s.invalidate();
 		}
 	/*private class FetchSinglePicture extends AsyncTask<URL, Integer, Drawable>
 	{
 		protected void onPreExecute()
 		{
 			setProgressBarIndeterminateVisibility(true);
 		}
 		protected Drawable doInBackground(URL... cam)
 		{
 			//
 			InputStream is = null;
 			try
 			{
 				is = (InputStream)cam[0].getContent();
 			}
 			catch (IOException e)
 			{
 				android.util.Log.e("Problem", "Exception fetching data", e);
 				e.printStackTrace();
 			}
 			return Drawable.createFromStream(is, cam[0].toString());
 		}
 		
 		protected void onProgressUpdate(Integer... progress)
 		{
 			TextView DownloadProgress;
 			DownloadProgress = (TextView)findViewById(R.id.DownloadProgress);		
 			DownloadProgress.setText(progress[0] + "%");
 			
 		}
 		protected void onPostExecute(Drawable pictureDraw)
 		{
 			pics.add(pictureDraw);
 			g.setAdapter(new ImageAdapter(c, pics));
 			int right = g.getCount() -1;
 	        g.setSelection(right);
 	        registerForContextMenu(g);
 	        setProgressBarIndeterminateVisibility(false);
 			
 		}
 	}*/
 	
 	public class MyOnItemSelectedListener implements OnItemSelectedListener
 		{
 		public void onItemSelected(AdapterView<?> parent, View view, final int pos, long id)
 		{
 		setProgressBarIndeterminateVisibility(true);
 		final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		final Context c = getBaseContext();
 		final Resources res = getResources();
 		
 		//Save Spinner Position to Shared Preferences
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putInt("spinnerPos", pos);
 		editor.commit();
 		fetchNumber = settings.getInt("fetchNumber", 2);
 		g = (Gallery) findViewById(R.id.gallery);
 		singleFile = settings.getBoolean("singleFile", true);
 		if (singleFile == true)
 		{
 		pics.clear();
 	
 		tV = (TextView) findViewById(R.id.DownloadProgress);
 		visiCount =0;
 		editor.putInt("visiCount", visiCount);
 		editor.commit();
 		tV.setText(visiCount+"/"+fetchNumber);
 		tV.setVisibility(0);
 		syncCount.setTo(fetchNumber);
 		syncPics.restore();
 			for (int a = fetchNumber; a > 0; a--)
 			{
 				final int b = a; 
 				
 				
 				new Thread(new Runnable()
 				{
 					public void run()
 					{
 						int syncCountValue;
 						while(true)
 						{
 							 syncCountValue = syncCount.value();
 							if (syncCountValue == b)
 							{ break; }
 						}
 						
 						//setProgressBarIndeterminateVisibility(true);
 						String[] camera_filenames = res.getStringArray(R.array.camera_filenames); /*load filenames from R*/
 						final String selectedCamera = camera_filenames[pos]; /*set filename to the camera selected in the spinner*/
 						Drawable newDraw=(new FetchPicture()).fetch_pic(selectedCamera, syncCountValue);
 						syncPics.add(newDraw);
 						
 						
 						// Set the adapter to our custom adapter (below)
 						
 						g.post(new Runnable()
 					{
 					public void run()
 					{
 						visiCount = (fetchNumber-b)+1;
 						tV.setText(visiCount+"/"+ fetchNumber);
 						g.setAdapter(new ImageAdapter(c, syncPics.output()));
 						//g.setAdapter(new ImageAdapter(c, pics));
 						int right = g.getCount() -1;
 						g.setSelection(right);
 						// We also want to show context menu for longpressed items in the gallery
 						if (visiCount == fetchNumber)
 						{
 						setProgressBarIndeterminateVisibility(false);
						pics = syncPics.output();
 						}
 						registerForContextMenu(g);
 						syncCount.decrement();
 					}
 					});
 					}
 			}
 			).start();
 			}
 
 		}
 		else
 		{
 		tV = (TextView) findViewById(R.id.DownloadProgress);
 		tV.setVisibility(0);
 		new Thread(new Runnable()
 		{
 		public void run()
 		{
 			String[] camera_filenames = res.getStringArray(R.array.camera_filenames); /*load filenames from R*/
 			final String selectedCamera = camera_filenames[pos]; /*set filename to the camera selected in the spinner*/
 			// Restore number of pictures to fetch preference
 			//fetchNumber = settings.getInt("fetchNumber", 2);
 			pics = (new FetchPicture()).fetch_pics(selectedCamera, fetchNumber); /*use FetchPicture to get the image for that camera*/
 			// Reference the Gallery view
 			//g = (Gallery) findViewById(R.id.gallery);
 			// Set the adapter to our custom adapter (below)
 			g.post(new Runnable()
 		{
 		public void run()
 		{
 			setProgressBarIndeterminateVisibility(false);
 			g.setAdapter(new ImageAdapter(c, pics));
 			//Set the view to show the most recent picture, which is farthest to the right.
 			int right = g.getCount() -1;
 			g.setSelection(right);
 			// We also want to show context menu for longpressed items in the gallery
 			registerForContextMenu(g);
 			
 		}
 		});
 		}
 		}
 		).start();
 		
 		}
 		
 		}
 
 		@Override
 		public void onNothingSelected(AdapterView<?> arg0)
 		{
 			// TODO Auto-generated method stub
 			
 		}
 		}
 		
 	
 	
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
     {
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.traffficcam_context,menu);
     	super.onCreateContextMenu(menu, v, menuInfo);
     }
 	@Override
     public boolean onContextItemSelected(MenuItem item)
 	{
 		final int galleryPos = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
 		Resources res = getResources();
 		Drawable myDrawable = pics.get(galleryPos);
 		String[] cameras_array = res.getStringArray(R.array.cameras_array);
 		String cameraName = cameras_array[spinnerPos];
 		Uri cachedPicUri;
 		
         switch (item.getItemId()) 
         {
         case R.id.view:
         	if (ExternalStorageTest() == true)
         	{
 	        	cachedPicUri = getExternalDrawableUri(myDrawable); 
         	}
         	else
         	{
         		cachedPicUri = getDrawableUri(myDrawable); 
 	        }
         	Intent viewPic = new Intent(android.content.Intent.ACTION_VIEW);
         	viewPic.setDataAndType(cachedPicUri, "image/png");
         	startActivity(Intent.createChooser(viewPic, "View image in"));
             return true;
         case R.id.save:
             Time cDT = new Time();
             cDT.setToNow();
             
             long milliTime = cDT.toMillis(false);
             milliTime = milliTime + (galleryPos*15)*60*1000;
             cDT.set(milliTime);
             String pictureFileName = cameraName + cDT.format("%Y%m%d_%H%M");
             
             if (ExternalStorageTest() == true)
             {
             	Boolean picSaved = createExternalStoragePublicPicture(pictureFileName, pics.get(galleryPos));
             	if (picSaved == true)
             	{
             		Toast.makeText(TrafficCamIntent.this, "Image saved as: " + pictureFileName + ".png", Toast.LENGTH_LONG).show();	
             	}
             	else
             	{
             		Toast.makeText(TrafficCamIntent.this, "Image Saving Failed.", Toast.LENGTH_LONG).show();
             		return false;
             	}
             }
             else
             {
             	Toast.makeText(TrafficCamIntent.this, "External Storage Unavailable.", Toast.LENGTH_LONG).show();
             	return false;
             }
             return true;
         case R.id.share:
         	if (ExternalStorageTest() == true)
         	{
 	        	cachedPicUri = getExternalDrawableUri(myDrawable);
         	}
         	else
         	{	//Fall back to Internal Storage
 	        	cachedPicUri = getDrawableUri(myDrawable);
         	}
 	        Intent sharePic = new Intent(android.content.Intent.ACTION_SEND);
 	        sharePic.setType("image/png");
 	        sharePic.putExtra(Intent.EXTRA_STREAM, cachedPicUri);
 	        startActivity(Intent.createChooser(sharePic, "Share image using"));
 	        return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) 
     	{
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.trafficcam_menu, menu);
         return true;
     	}
     //Menu with just 2 entries: info and preferences
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         // Handle item selection
         switch (item.getItemId()) 
         {
         case R.id.info:
         	Intent about = new Intent(this, About.class);
         	startActivity(about);
             return true;
         case R.id.preferences:
             // Preferences screen to be defined
         	Intent prefs = new Intent(this, Preferences.class);
         	startActivity(prefs);
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
     
     
 	public class ImageAdapter extends BaseAdapter
 	{
 		int mGalleryItemBackground;
     
 		public ImageAdapter(Context c, ArrayList<Drawable> loadedPics)
 		{
 			mContext = c;
 			TypedArray a = obtainStyledAttributes(R.styleable.Gallery1);
 			mGalleryItemBackground = a.getResourceId(R.styleable.Gallery1_android_galleryItemBackground, 0);
 			a.recycle();
 			pics = loadedPics;
 		}
     
 		public int getCount() 
 		{
 			return pics.size();
 		}
 
 		public Object getItem(int position)
 		{
 			return pics.get(position);
 		}
 
 		public long getItemId(int position)
 		{
 			return position;
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) 
 		{
 			ImageView i = new ImageView(mContext);
 			DisplayMetrics metrics = new DisplayMetrics();
 			getWindowManager().getDefaultDisplay().getMetrics(metrics);
 			double space = 0.99;
 			double widthDiff =  (space*metrics.widthPixels)/pics.get(0).getMinimumWidth();
 			
 			i.setImageDrawable(pics.get(position));
 			i.setScaleType(ImageView.ScaleType.FIT_CENTER);
 			i.setLayoutParams(new Gallery.LayoutParams((int) Math.rint(widthDiff*pics.get(0).getMinimumWidth()),(int)Math.rint(widthDiff*pics.get(0).getMinimumHeight())));
 			
 			
 
 			 
 			// The preferred Gallery item background
 			i.setBackgroundResource(mGalleryItemBackground);
         
         	return i;
 		}
 		private Context mContext;
 		private ArrayList<Drawable> pics;
 	}
 
 
 public boolean ExternalStorageTest ()
 {
 	boolean mExternalStorageWriteable = false;
 	String state = Environment.getExternalStorageState();
 
 	if (Environment.MEDIA_MOUNTED.equals(state))
 	{
 	    // We can read and write the media
 	    mExternalStorageWriteable = true;
 	} 
 	else 
 	{
 	    // Something else is wrong. It may be one of many other states, but all we need
 	    //  to know is we cannot write.
 	    mExternalStorageWriteable = false;
 	}
 	return mExternalStorageWriteable;
 }
 Boolean createExternalStoragePublicPicture(String pictureFileName, Drawable selectedPicture)
 {
 	//Get the external storage location for Pictures from the Environment.
     File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
     //Create a file with the name provided.
     File file = new File(path, pictureFileName + ".png");
     //Convert the Drawable to a Bitmap
     Bitmap myBitmap = ((BitmapDrawable) selectedPicture).getBitmap();
     try
     {
         // Make sure the Pictures directory exists.
         path.mkdirs();
         //Open created file as an output stream.
         OutputStream os = new FileOutputStream(file);
         //Compress the Bitmap into a PNG format and write it to the file created.
         myBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
         //Close the Output Stream
         os.close();
         
         // Tell the media scanner about the new file so that it is
         // immediately available to the user.
         MediaScannerConnection.scanFile(this,
                 new String[] { file.toString() }, null,
                 new MediaScannerConnection.OnScanCompletedListener()
         {
             public void onScanCompleted(String path, Uri uri)
             {
                 Log.i("ExternalStorage", "Scanned " + path + ":");
                 Log.i("ExternalStorage", "-> uri=" + uri);
             }
         });
     }
     catch (IOException e) 
     {
         // Unable to create file, likely because external storage is
         // not currently mounted.
         Log.w("ExternalStorage", "Error writing " + file, e);
         return false;
     }
     return true;
 }
 
 Uri getExternalDrawableUri (Drawable selectedPicture)
 {
 	File path = getExternalCacheDir();
     //Create a file with the name provided.
     File file = new File(path, "passedImage.png");
     //Convert the Drawable to a Bitmap
     Bitmap myBitmap = ((BitmapDrawable) selectedPicture).getBitmap();
     try
     {
         //Open created file as an output stream.
         OutputStream os = new FileOutputStream(file);
         //Compress the Bitmap into a PNG format and write it to the file created.
         myBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
         //Close the Output Stream
         os.close();
         
     }
     catch (IOException e) 
     {
         // Unable to create file, likely because external storage is
         // not currently mounted.
         Log.w("ExternalCacheStorage", "Error writing " + file, e); 
     }
     
     Uri returnUri = Uri.fromFile(file);
     return returnUri;
 }
 Uri getDrawableUri (Drawable selectedPicture)
 {
     File path = getFilesDir();
     
     //Create a file with the name provided.
     
     //String fileName = path + ;
     //Convert the Drawable to a Bitmap
     Bitmap myBitmap = ((BitmapDrawable) selectedPicture).getBitmap();
     try
     {
         //Open created file as an output stream.
         FileOutputStream os = openFileOutput("passedImage.png", Context.MODE_WORLD_READABLE);
         //Compress the Bitmap into a PNG format and write it to the file created.
         myBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
         //Close the Output Stream
         os.close();
     }
     catch (FileNotFoundException e) 
     {
         Log.w("InternalCacheStorage", "Error writing " + path + "passedImage.png", e); 
     }
     catch (IOException e) 
     {
         Log.w("InternalCacheStorage", "Error writing " + path + "passedImage.png", e); 
     }
     
     File[] internalFiles = path.listFiles();
     Uri returnUri = Uri.fromFile(internalFiles[0]);
     return returnUri;
 }
 Boolean clearInternalFiles ()
 {
 	File[] internalFiles = getFilesDir().listFiles();
 	if (internalFiles.length > 0)
 	{
 		for (int i=0; i <= internalFiles.length; i++)
 		{
 			internalFiles[i].delete();
 			Log.i("InternalStorage", "Deleted: " + internalFiles[i]);
 		}
 		return true;
 	}
 	else
 	{
 		return false;
 	}
 }
 }
