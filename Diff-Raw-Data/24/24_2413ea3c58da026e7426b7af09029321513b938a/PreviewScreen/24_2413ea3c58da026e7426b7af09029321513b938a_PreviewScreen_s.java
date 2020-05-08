 package edu.chl.asciicam.activity;
 
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

 import android.app.Activity;
 import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
 import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 
 //This file is part of Asciicam.
 //
 //Asciicam is free software: you can redistribute it and/or modify
 //it under the terms of the GNU General Public License as published by
 //the Free Software Foundation, either version 3 of the License, or
 //(at your option) any later version.
 //
 //Asciicam is distributed in the hope that it will be useful,
 //but WITHOUT ANY WARRANTY; without even the implied warranty of
 //MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //GNU General Public License for more details.
 //
 //You should have received a copy of the GNU General Public License
 //along with Asciicam.  If not, see <http://www.gnu.org/licenses/>.
 
 /**
  * 
  * This activity sets the taken picture as the background.
  * You can choose what to with you taken picture, 
  * save, convert or delete picture and take a new one.
  * @author Kryckans
  *
  */
 public class PreviewScreen extends Activity {
 
 	Button back_btn, save_pic_btn, convert_btn;
 	Bitmap bmp;
 	ImageView iv = (ImageView) findViewById(R.id.preview_pic);
 	public static String DIRECTORY_PICTURES;
 	BroadcastReceiver mExternalStorageReceiver;
 	boolean mExternalStorageAvailable = false;
 	boolean mExternalStorageWriteable = false;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	setContentView(R.layout.activity_preview_screen);
     	back_btn = (Button) findViewById(R.id.back);
     	save_pic_btn = (Button) findViewById(R.id.save);
     	convert_btn = (Button) findViewById(R.id.convert);
 
     	//Setting the taken picture as background
     	Bundle extras = this.getIntent().getExtras();
     	byte[] jpgArray = (byte[]) extras.getByteArray("jpgByteArray");
     	//byte[] jpgArray = (byte[]) this.getIntent().getByteArrayExtra("jpgByteArray");
     	//savedInstanceState.getByteArray("jpgByteArray");
     	bmp = (Bitmap) BitmapFactory.decodeByteArray(jpgArray, 0, jpgArray.length);
     	if(bmp != null){
     		iv.setImageBitmap(bmp);
     	}
 
     
         back_btn.setOnClickListener(new View.OnClickListener() {
     		
         	/*Using the finish() to go back to the last activity */
     		public void onClick(View v) {
     			finish();
     		}
     	});
         
         save_pic_btn.setOnClickListener(new View.OnClickListener() {
     		
     		public void onClick(View v) {
    			
        		}
     	}); 
     
     
         convert_btn.setOnClickListener(new View.OnClickListener() {
     		// if click here, the picture will be converted
     		public void onClick(View v) {
     		}
     	});    //TODO convert picture to ascii
         
         
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_preview_screen, menu);
         return true;
     }
 }
