 package com.cianmcgovern.android.ShopAndStore;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class ShopAndStore extends Activity
 {
 	private Button callPhoto;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         try {
 			testingLoad();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         Intent mIntent = new Intent(this,TakePhoto.class);
         startActivity(mIntent);
         callnativecode(getFilesDir().toString()+"/image.jpg");
 
     }
     
     private native void callnativecode(String dir);
     
     static{
	System.loadLibrary("lept");
	System.loadLibrary("tess");
     	System.loadLibrary("ShopAndStore");
     }
     
     /**
      * Loads a test image for testing
      * 
      * @return byte[]
      * @throws IOException
      */
     private void testingLoad() throws IOException{
     	
     	InputStream is = getResources().openRawResource(R.raw.test);
     	OutputStream os = new FileOutputStream(new File(getFilesDir().toString()+"/image.jpg"));
     	Log.v("ShopAndStore",getFilesDir().toString()+"/image.jpg");
     	BufferedInputStream bis = new BufferedInputStream(is);
     	BufferedOutputStream bos = new BufferedOutputStream(os);
     	byte[] buf = new byte[1024];
 
     	int n = 0;
     	int o = 0;
     	while ((n = bis.read(buf, o, buf.length)) > 0) {
     		bos.write(buf, 0, n);
     	}
 
     	bis.close();
     	bos.close();
     }
 }
