 package edu.chl.asciicam.activity;
 
 import android.net.Uri;
 
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.ImageView;
//Copyright 2012 Robin Braaf, Ossian Madisson, Marting Thrnesson, Fredrik Hansson and Jonas strm.
 //
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
  * This activity is the first one you see when you start the app.
  * Its a menu with the buttons, take picture, load picture and options
  * @author Ossian, Jonas
  */
 public class MenuScreen extends Activity {
 
 	Button take_Pic_B, load_Pic_B, optionsB;
 	ImageView chosenPic;
 	Intent load;
 
 	/**
 	 * This is called automatically by the android system when the activity is started
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		//Set fullscreen, must be before setContent!
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
 				             	WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		
 		setContentView(R.layout.activity_menu_screen);
 		take_Pic_B = (Button) findViewById(R.id.Take_pic_B);
 		load_Pic_B = (Button) findViewById(R.id.Load_pic_B);
 		optionsB = (Button) findViewById(R.id.Options_B);
 
 		/* Setting actionListeners to the buttons */
 		take_Pic_B.setOnClickListener(new View.OnClickListener() {			
 			//opening our cameraScreen
 			public void onClick(View v) {
 				Intent i = new Intent(MenuScreen.this, CameraScreen.class);
 				startActivity(i);
 			}
 		});
 		//calls for the phones local gallery to pick a saved picture
 		load_Pic_B.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				load = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 				startActivityForResult(load, 0);
 			}
 		});
 		//calls for the optionsScreen; not yet implemented
 		optionsB.setOnClickListener(new View.OnClickListener() {			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub				
 			}
 		});
 	}
 
 	/**
 	 * Checking for a result from the phones local gallery and starts the previewScren activity
 	 * @param request, result, data
 	 */
 	protected void onActivityResult(int request, int result, Intent data){
 		super.onActivityResult(request, result, data);
 		String filePath = null;			
 		if (result == RESULT_OK){
 			Uri selectedImage = data.getData();
 			String[] filePathColumn = {MediaStore.Images.Media.DATA};
 
 			Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
 			cursor.moveToFirst();
 
 			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
 			filePath = cursor.getString(columnIndex);  //set the filepath to locate the picture from previewScreen				
 			cursor.close();
 		}
 		//If no picture is chosen, PreviewScreen will not start (returning to MenuScreen)
 		//If a picture is chosen, open PreviewScreen and make sure it can find the chosen picture
 		if (result != 0){
 			Intent startPreview = new Intent(MenuScreen.this, PreviewScreen.class);
 			startPreview.putExtra("filePath",filePath);
 			startPreview.putExtra("id", "loaded"); 
 			startActivity(startPreview);
 		}
 	}	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_menu_screen, menu);
 		return true;
 	}
 	
 	/**
 	 * Used for testing.
 	 * @return Button used by instance.
 	 */
 	public Button getTakePicButton(){
 		return take_Pic_B;
 	}
 	
 	/**
 	 * Used for Testing
 	 * @return Button used by instance.
 	 */
 	public Button getLoadPicButton(){
 		return load_Pic_B;
 	}
 	
 	/**
 	 * Used for testing.
 	 * @return Button used by instance.
 	 */
 	public Button getOptionsButton(){
 		return optionsB;
 	}
 }
