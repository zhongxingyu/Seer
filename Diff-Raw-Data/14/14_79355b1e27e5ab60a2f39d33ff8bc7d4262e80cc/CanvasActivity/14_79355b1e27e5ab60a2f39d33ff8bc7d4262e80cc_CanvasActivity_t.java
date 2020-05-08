 package com.alizarinarts.paintpaint;
 
 import java.io.File;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 import android.app.AlertDialog;
 
 import android.content.DialogInterface;
 import android.content.Intent;
 
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 
 import android.util.Log;
 
 
 import android.widget.EditText;
 
 import android.widget.SeekBar;
 
 /**
  * This is the main activity of this program. It presents the user with a
  * canvas to draw on and allows the user to share or save their image.
  *
  * @author <a href="mailto:david.e.shere@gmail.com">David Shere</a>
  */
 public class CanvasActivity extends SherlockActivity {
 
     /** The representation of the drawing surface. */
     private Canvas mCanvas;
 
     /** The save path for image saving. */
     private String mSavePath;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         mCanvas = new Canvas(this); 
         mSavePath = Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name) + "/";
 
         // Enable the ActionBar icon as Up button.
         ActionBar ab = getSupportActionBar();
         ab.setHomeButtonEnabled(true);
         ab.setDisplayHomeAsUpEnabled(true);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         mCanvas.getSurfaceView().onResume();
         //Do additional stuff to unpause program
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         mCanvas.getSurfaceView().onPause();
         //Do additional stuff to pause program
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getSupportMenuInflater().inflate(R.menu.activity_canvas, menu);
         return true;
     }
 
     /**
      * Share the canvas image via the Android Share ability.
      *
      * Following example from:
      * http://twigstechtips.blogspot.com/2011/10/android-share-image-to-other-apps.html
      */
     public void onClickShare(MenuItem mi) {
         mCanvas.getSurfaceView().queueEvent(new Runnable() {public void run() {
             mCanvas.saveCanvas(mSavePath,".tmp.png");
             File file = new File(mSavePath+".tmp.png");
             Intent intent = new Intent(android.content.Intent.ACTION_SEND);
             intent.setType("image/png");
             intent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(file));
             startActivity(Intent.createChooser(intent, "Share image"));
         }});
     }
 
     /**
      *  Prompt user for a file name and save the canvas to disk.
      *  Alert prompt example from:
      *  http://www.androidsnippets.com/prompt-user-input-with-an-alertdialog
      */
     public void onClickSave(MenuItem mi) {
         // Setup the EditText view for our save dialog
         final EditText input = new EditText(this);
         input.setSingleLine();
 
         AlertDialog.Builder alert = new AlertDialog.Builder(this);
         alert.setTitle("Save Image");
         alert.setMessage("Name");
 
         alert.setView(input);
         alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     final String fileName = input.getText().toString();
                     Log.d(PaintPaint.NAME,"saving: "+fileName);
                     mCanvas.getSurfaceView().queueEvent(new Runnable() {public void run() {
                         mCanvas.saveCanvas(mSavePath, fileName);
                     }});
                 }
         });
 
         alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 // Canceled
             }
         });
 
         alert.show();
 
     }
 
     /**
      * Opens the settings window for the brush.
      */
     public void onClickBrushSettings(MenuItem mi) {
         if (mCanvas.getBrush() == null)
             return;
         // Setup the EditText view for our save dialog
         final SeekBar sb = new SeekBar(this);
         sb.setMax(100);
         float size = mCanvas.getBrush().getSize();
         sb.setProgress((int)size);
 
         AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Brush Settings");
        alert.setMessage("Size");
 
         alert.setView(sb);
         alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     final float size = sb.getProgress();
                     mCanvas.getBrush().setSize(size);
                 }
         });
 
         alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 // Canceled
             }
         });
 
         alert.show();
 
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem mi) {
         switch (mi.getItemId()) {
             // Handle the Up action from ActionBar icon clicks.
             case android.R.id.home:
                 Intent i = new Intent(this, MainMenuActivity.class);
                 i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(i);
                 return true;
         }
         return false;
     }
 
 }
