 package com.cs301w01.meatload.activities;
 
 import com.cs301w01.meatload.R;
 import com.cs301w01.meatload.controllers.PhotoManager;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 
 /**
 * Implements the logic for the TakePictureActivity, as well as the Take Picture dialog.
  * @author Joel Burford
  */
 public class TakePictureActivity extends Skindactivity {
 	private Bitmap imgOnDisplay;
 	
 	private PhotoManager photoManager;
 	
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.take_picture);
 	    
 	    Bundle b = getIntent().getExtras();
         photoManager = (PhotoManager) b.getSerializable("manager");
         photoManager.setContext(this);
 	    
 	    imgOnDisplay = photoManager.generatePicture();
     	ImageView image = (ImageView) findViewById(R.id.imgDisplay);
     	image.setImageBitmap(imgOnDisplay);
 	    
 	    //Take Picture button listener
         final Button takePicButton = (Button) findViewById(R.id.takePic);
         takePicButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 // Perform action on click
             	takePicture();
             }
         });
         
 	    //Generate Picture button listener
         final Button genPicButton = (Button) findViewById(R.id.genPic);
         genPicButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 // Perform action on click
             	imgOnDisplay = photoManager.generatePicture();
             	
             	//used http://stackoverflow.com/questions/6772024/how-to-update-or-change-images-of-imageview-dynamically-in-android
             	ImageView image = (ImageView) findViewById(R.id.imgDisplay);
             	image.setImageBitmap(imgOnDisplay);
             }
         });
 	}
 
     //@Override
     public void update(Object model) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
     
     /**
      * Opens a dialog asking the user if they want to keep the picture they have just taken.
      * <p>
      * If yes, saves the Picture in the Viewfinder (Or current randomly generated picture)
      * @see <a href="http://www.androidsnippets.com/prompt-user-input-with-an-alertdialog">
      	http://www.androidsnippets.com/prompt-user-input-with-an-alertdialog</a>
      */
     private void takePicture() {
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		alert.setTitle("Confirm");
 		alert.setMessage("Are you sure you want this picture?");
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				photoManager.takePicture(getFilesDir());
 				finish();
 			}
 		});
 
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				// Canceled.
 			}
 		});
 
 		alert.show();
     }
     
 }
