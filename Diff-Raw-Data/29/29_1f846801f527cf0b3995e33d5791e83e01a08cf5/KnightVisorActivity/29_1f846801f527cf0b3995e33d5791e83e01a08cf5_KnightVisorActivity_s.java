 package edu.uncc.cci.KnightVisor;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.ImageFormat;
 import android.hardware.Camera;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
import android.widget.FrameLayout;
 import android.widget.SeekBar;
 import android.widget.Toast;
 
 public class KnightVisorActivity extends Activity {
 
     private EdgeView edgeView;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         
         /* set up window so we get full screen */
         Window window = this.getWindow();
         window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
         window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         
         
         /* set up a surfaceView where the camera display will be put */
        SurfaceView   surfaceView   = new SurfaceView(this);
         SurfaceHolder surfaceHolder = surfaceView.getHolder();
         surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
         surfaceHolder.addCallback(new SurfaceHolder.Callback() 
         {
             private Camera camera = null;
             
             public void surfaceDestroyed(SurfaceHolder holder) 
             {
                 if (camera == null) return;
                 
                 try { 
                     camera.setPreviewDisplay(null);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 
                 camera.stopPreview();
                 camera.setPreviewCallback(null);
                 camera.release();
                 camera = null;
             }
             
             public void surfaceCreated  (SurfaceHolder holder) { }
             
             public void surfaceChanged  (SurfaceHolder holder, int format, int width, int height) 
             {
                 camera = Camera.open();
                 
                 try {
                     camera.setPreviewDisplay(holder);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
 
 
                 Camera.Parameters parameters = camera.getParameters();
                 Camera.Size       size       = getBestPreviewSize(width, height, parameters);
 
                 if (size == null) 
                     return;
                 
                 
                 parameters.setPreviewSize(size.width, size.height);
                 parameters.setPreviewFormat(ImageFormat.NV21);
                 camera.setParameters(parameters);
                 
                 camera.addCallbackBuffer(new byte[width * height * 4]);
                 camera.setPreviewCallbackWithBuffer(edgeView);                
                 camera.startPreview();
                 
             }
             
             /* helper function to set up the display */
             private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) 
             {
                 Camera.Size result = null;
 
                 /* trying to get largest possible size */
                 for (Camera.Size size : parameters.getSupportedPreviewSizes())
                     if (size.width <= width && size.height <= height)
                         if (result == null || size.width * size.height > result.width * result.height)
                             result = size;
                 
                 return result;
             }
         });
         
         
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.mainFrameLayout);
        frameLayout.addView(surfaceView);
        
        edgeView = new EdgeView(this);
        frameLayout.addView(edgeView);
        
        
         SeekBar seekbar = (SeekBar)findViewById(R.id.seekBar);
         seekbar.setMax(150); 
         seekbar.setProgress(75);
         seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
             public void onStartTrackingTouch(SeekBar seekBar) { }
             public void onStopTrackingTouch (SeekBar seekBar) { }
             public void onProgressChanged   (SeekBar seekBar, int progress, boolean fromUser) {
                 edgeView.setThresholdManually(150 - progress);
             } 
         });
         
         final Context ctx = this;
         ((CheckBox)findViewById(R.id.medianCheckBox))
         .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                 edgeView.setMedianFiltering(checked);
                 if (checked) Toast.makeText(ctx, "Median Filtering", Toast.LENGTH_SHORT).show();
             }
         });
         
         
         ((CheckBox)findViewById(R.id.automaticCheckBox))
         .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                 edgeView.automaticThresholding(checked);
                 if (checked) Toast.makeText(ctx, "Automatic Thresholding", Toast.LENGTH_SHORT).show();
             }
         });
         
         ((CheckBox)findViewById(R.id.logarithmicCheckBox))
         .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                edgeView.automaticThresholding(checked);
                 if (checked) Toast.makeText(ctx, "Log Transform", Toast.LENGTH_SHORT).show();
             }
         });
         
         
         ((CheckBox)findViewById(R.id.grayscaleCheckBox))
         .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                 edgeView.grayscaleOnly(checked);
                 if (checked) Toast.makeText(ctx, "Grayscale", Toast.LENGTH_SHORT).show();
             }
         });
         
     }
     
     
     /* options menu */
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         this.getMenuInflater().inflate(R.menu.menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
             case R.id.red:
                 edgeView.setColorSelected(Color.RED);
                 return true;
             case R.id.green:
                 edgeView.setColorSelected(Color.GREEN);
                 return true;
             case R.id.blue:
                 edgeView.setColorSelected(Color.BLUE);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 }
