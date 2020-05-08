 package edu.chl.asciicam.activity;
 
 import java.io.IOException;
 
 import edu.chl.asciicam.file.FileController;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
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
  * This activity sets the taken picture as the background. You can choose what
  * to with you taken picture, save, convert or delete picture and take a new
  * one.
  * 
  * @author Kryckans
  * 
  */
 public class PreviewScreen extends Activity {
 
 	Button back_btn, save_pic_btn, convert_btn;
 	Bitmap bmp;
 	Bundle extras;
 	ImageView iv;
 	public static String DIRECTORY_PICTURES;
 	BroadcastReceiver mExternalStorageReceiver;
 	public FileController fc;
 	byte[] picDataArray = null;
 
 	/**
 	 * This is called automatically by the android system when the activity is
 	 * started.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_preview_screen);
 		back_btn = (Button) findViewById(R.id.back);
 		save_pic_btn = (Button) findViewById(R.id.save);
 		convert_btn = (Button) findViewById(R.id.convert);
 		iv = (ImageView) findViewById(R.id.preview_pic);
 		fc = new FileController(getBaseContext());
 
 		initiateButtons();
 
 		// still a work in progress
 		extras = this.getIntent().getExtras();
		String id = extras.getString("id"); //loads the id to check if the background should be loaded from the taken picture or from gallery
		
 		if (id.equals("taken")) {
 			loadFromCamera();
 		} else if (id.equals("loaded")) {
 			loadFromPhone();
 		}
 
 	}
 
 	/**
 	 * Loading the background from the taken picture
 	 */
 	private void loadFromCamera() {
 		try {
			picDataArray = fc.loadPicPrivate(); // loading the data from the private pic saved from camerascreen
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		bmp = (Bitmap) BitmapFactory.decodeByteArray(picDataArray, 0,
 				picDataArray.length);
 		setBackground(bmp);
 	}
 
 	/**
 	 * Loading the background from gallery
 	 */
 	private void loadFromPhone() {
 
 		String filePath = extras.getString("filePath");
		if (filePath != null) {			
 			bmp = (Bitmap) BitmapFactory.decodeFile(filePath);
			setBackground(bmp);
 		}
 	}
 
 	/**
 	 * This method takes a Bitmap and sets it as background
 	 * 
 	 * @param bg
 	 */
 	private void setBackground(Bitmap bg) {
 		if (bg != null) {
 			iv.setImageBitmap(bg);
 		}
 	}
 
 	/**
 	 * This is called by to initiate the buttons and add functionality
 	 */
 	private void initiateButtons() {
 		back_btn.setOnClickListener(new View.OnClickListener() {
 
 			/* Using the finish() to go back to the last activity */
 			public void onClick(View v) {
 				finish();
 			}
 		});
 
 		save_pic_btn.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				try {
 					fc.savePic(picDataArray);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				picDataArray = null;
 			}
 		});
 
 		convert_btn.setOnClickListener(new View.OnClickListener() {
 			// if click here, the picture will be converted
 			public void onClick(View v) {
 			}
 		}); // TODO convert picture to ascii
 	}
 
 }
