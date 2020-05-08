 
 package edu.chl.asciicam.activity;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.ImageView;
 import edu.chl.asciicam.file.FileController;
 import edu.chl.asciicam.filter.AsciiFilter;
 import edu.chl.asciicam.filter.GrayScaleFilter;
 
 //Copyright 2012 Robin Braaf, Ossian Madisson, Martin Thrnesson, Fredrik Hansson and Jonas strm.
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
  * This activity is the one that is shown after convert is clicked in PreviewScreen.
  * During the creation of this activity it calls for the conversion of the picture with
  * the current settings and set it as background
  * @author Ossian
  *
  */
 public class ConvertedPicScreen extends Activity {
 
 	Button menu_btn, options_btn, save_pic_btn;
 	ImageView iv;
 	FileController fc;
 	byte[] picDataArray = null;
 	AlertDialog dialog;
 	Bitmap bmp;
 	AsciiFilter filter;
 	Bundle extras;
 	String id;
 
 	//TODO REMOVE LATER
 	GrayScaleFilter gFilter; //This should be removed when AsciiFilter is completed
 
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
		
 		setContentView(R.layout.activity_convert_pic_screen);
 		menu_btn = (Button) findViewById(R.id.converted_menu);		
 		options_btn = (Button) findViewById(R.id.converted_options);
 		save_pic_btn = (Button) findViewById(R.id.converted_save);
 		iv = (ImageView) findViewById(R.id.preview_converted);
 		fc = new FileController(getBaseContext());
 		filter = new AsciiFilter();
 		initiateButtons();
 
 		extras = this.getIntent().getExtras(); //get all the extras from intent to the Bundle extras
 		id = extras.getString("id"); //loads the id to check if the picture for convertion should be loaded from gallery
 
 
 
 		//**********
 		//TODO THIS SHOULD BE REMOVED WHEN ASCIIFILTER IS DONE
 		gFilter = new GrayScaleFilter();
 		bmp = loadPic();
 		bmp = gFilter.convert(bmp);
 		setBackground(bmp);
 		//**********
 
 		//This should be used when the Asciifilter is done
 		//		bmp = loadPic(); //Load the array that was privately saved from cameraScren
 		//		bmp = filter.convert(bmp); // Convert the picture with the asciifilter
 		//		setBackground(bmp);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 
 	private void initiateButtons(){
 		menu_btn.setOnClickListener(new View.OnClickListener() {
 			//Go back to the menuScreen when this button is clicked
 			public void onClick(View v) {
 				Intent menu = new Intent(getBaseContext(), MenuScreen.class);
 				menu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //This closes the activities between this and menuscreen
 				startActivity(menu);				
 			}
 		});
 
 		options_btn.setOnClickListener(new View.OnClickListener() {
 			// Go to the optionsScreen when this is clicked to change the properties of the conversion
 			public void onClick(View v) {
				//TODO remove the comments when OptionsScreen is implemented!
				//				Intent optionsScreen = new Intent(getBaseContext(), OptionsScreen.class);
				//				startActivity(optionsScreen);
 
 			}
 		});
 
 		save_pic_btn.setOnClickListener(new View.OnClickListener() {
 			// Save the converted picture on the phone when this is clicked
 			public void onClick(View v) {
 				if(picDataArray != null){
 					try {
 						fc.savePic(picDataArray); //saves the picture on the phone
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 					picDataArray = null;
 				}else{
 					openDialog(); //opens a dialog to tell the user that the picture is saved
 				}
 			}
 		});
 	}
 
 	/**
 	 * Set the Bitmap in param as background in the ImageView
 	 * @param bg
 	 */
 	private void setBackground(Bitmap bg){
 		if(bg != null){
 			bg = rotatePic(bg);
 			iv.setImageBitmap(bg);
 		}
 	}
 
 	/**
 	 * Will rotate the Bitmap so it's shown as portrait
 	 * @param bg
 	 * @return a rotated Bitmap
 	 */
 	private Bitmap rotatePic(Bitmap bg){ 
 		//this had to be implemented because the picture was shown turned 90 degrees when set as background, this is to prevent that
 		Matrix matrix = new Matrix();
 		matrix.setRotate(90, bg.getWidth()/2, bg.getHeight()/2);
 		return Bitmap.createBitmap(bg, 0, 0, bg.getWidth(), bg.getHeight(), matrix, true);
 	}
 
 	/**
 	 * Opens a dialog to the user that says "The picture is saved!"
 	 */
 	private void openDialog(){
 		dialog = new AlertDialog.Builder(ConvertedPicScreen.this).create();
 		dialog.setMessage("The picture is already saved!");
 		dialog.setButton("OK", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 			}
 		});
 		dialog.show();
 	}
 
 	/**
 	 * Check what picture to load and return it as a bitmap
 	 * @return a Bitmap ready for conversion
 	 */
 	private Bitmap loadPic(){
 		//Check if picture should be loaded from the gallery or if it's picture taken with the application 
 		if(id.equals("loaded")){
 			//Get the filepath and make a bitmap to return
 			String filePath = extras.getString("filePath");
 			if(filePath != null){
 				BitmapFactory.Options options = new BitmapFactory.Options();
 				options.inSampleSize = 4;
 				bmp = (Bitmap) BitmapFactory.decodeFile(filePath, options);
 				return bmp;
 			}
 		}	
		
 		try{ //Read the privately saved array
 			picDataArray = fc.loadPicPrivate();
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 		// Decode and return the array as a bitmap
 		return (Bitmap) BitmapFactory.decodeByteArray(picDataArray, 0, picDataArray.length);
 	}
 }
