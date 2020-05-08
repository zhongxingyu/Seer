 package com.hauk142.hackles;
 
 import java.io.InputStream;
 import java.net.URL;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.AsyncTask;
 import android.content.Context;
 import android.widget.ImageView;
 import android.widget.Toast;
 import android.widget.Button;
 import android.view.View;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.File;
import android.os.Environment;
 
 
 public class HacklesActivity extends Activity
 {
    int comic = 1;
    File comicFile;
     ImageView image;
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
     	setContentView(R.layout.main);
 	
 	image = (ImageView) findViewById(R.id.image);
 	Button Next = (Button) findViewById(R.id.ButtonNext);
 	Button Previous = (Button) findViewById(R.id.ButtonPrevious);
 	new DownloadImage().execute("http://hackles.org/strips/cartoon" + comic + ".png");
    	try
	{
		comicFile = new File(Environment.getExternalStorageDirectory() + File.separator + "hackles");
	}
	catch (Exception e)
	{
		comicFile.createNewFile();
	}

    	FileReader in = new FileReader(comicFile);
 
 	Next.setOnClickListener(new Button.OnClickListener() 
 	{
 		public void onClick(View v) {
 			try
 			{
			/*	if(comic = null)
				{
					try{
						in
				}*/
 				if(comic != 364)
 				{
 					comic++;
 					new DownloadImage().execute("http://hackles.org/strips/cartoon" + comic + ".png");
 				}
 				else
 				{
 					toast("Reached the last comic!");
 				}
 			}
 			catch (Exception e) {
 			}
 		}
 	});
 	Previous.setOnClickListener(new Button.OnClickListener()
 	{
 		public void onClick(View v) {
 			try
 			{
 				if(comic != 1)
 				{
 					comic--;
 					new DownloadImage().execute("http://hackles.org/strips/cartoon" + comic + ".png");
 				}
 				else
 				{
 					toast("Reached the first comic!");
 				}
 			}
 			catch (Exception e) {
 			}
 		}
 	});
     }
 
     public void toast(String text)
     {
 	    Context context = getApplicationContext();
 	    Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
 	    toast.show();
     }
 
     private class DownloadImage extends AsyncTask<String, Void, Bitmap> {
 	    protected Bitmap doInBackground(String... url) {
 		    return loadImageFromNetwork(url[0]);
 	    }
 
 	    protected void onPostExecute(Bitmap result) {
 		    image.setImageBitmap(result);
 	    }
     }
 
     private Bitmap loadImageFromNetwork(String url) {
 	    try {
 		    Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(url).getContent());
 		    return bitmap;
 	    }
 	    catch (Exception e) {
 		    e.printStackTrace();
 		    return null; 
 	    }
     }
 }
