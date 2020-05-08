 package eu.imagine.app;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.ViewGroup;
 import eu.imagine.R;
 import eu.imagine.framework.Flags;
 import eu.imagine.framework.MainInterface;
 
 public class ImagineActivity extends Activity {
 
     /**
      * Stores instance of framework for access.
      */
     private MainInterface framework;
 
     /**
      * Example object to render. Format is 3 coords followed by 4 colors.
      */
     private float[] oneData = new float[]{
             0, 0, 0,
             1.0f, 0.0f, 0.0f, 1.0f, // red
 
             -0.5f, 0.5f, 0,
             0.0f, 1.0f, 0.0f, 1.0f,  // green
 
             0.5f, 0.5f, 0,
             0.0f, 0.0f, 1.0f, 1.0f, // blue
 
             0, 0, 0,
             1.0f, 0.0f, 0.0f, 0.5f, // red
 
             -0.5f, -0.5f, 0,
             0.0f, 1.0f, 0.0f, 0.5f,  // green
 
             0.5f, -0.5f, 0,
             0.0f, 0.0f, 1.0f, 0.5f // blue
     };
 
     private float[] twoData = new float[]{
             0, 0, 0,
             1.0f, 0.0f, 0.0f, 1.0f, // red
 
             -0.5f, 0.5f, 0,
             0.0f, 0.0f, 1.0f, 0.8f,  // blue
 
             0.5f, 0.5f, 0,
             0.0f, 0.0f, 1.0f, 0.8f // blue
     };
 
     /*
     Orig:
     private float[][] cameraMatrix = new float[][]{
            new float[]{1251f, 0f, 639.5f},
            new float[]{0f, 1251f, 359.5f},
             new float[]{0f, 0f, 1f}
     };
     private float[] distortionCoefficients = new float[]{
            0.2610701252267455f, -2.229801972443634f, 0f, 0f, 4.354745457073879f
     };
      */
 
     // Camera matrix (here determined ahead of time)
     private float[][] cameraMatrix = new float[][]{
             new float[]{1280, 0, 640},
             new float[]{0, 1280, 360},
             new float[]{0, 0, 1}
     };
     // Distortion coefficients:
     private float[] distortionCoefficients = new float[]{
            0.2785042226314545f, -2.410807609558105f, 0, 0, 4.748225688934326f
     };
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.imagine);
 
         // Construct framework. This includes passing a reference to the
         // activity (here this), the viewgroup where it'll construct its
         // views, and the camera and distortioncoefficients.
         framework = new MainInterface(this, cameraMatrix, distortionCoefficients);
 
         // Get and parse options to set
         Bundle options = getIntent().getExtras();
         if (options != null) {
             framework.setFlag(Flags.DEBUG_LOGGING, options.getBoolean("debugLog", false));
             framework.setFlag(Flags.ALLOW_DUPLICATE_MARKERS,
                     options.getBoolean("dupMarkers", false));
             framework.setFlag(Flags.DEBUG_FRAME_LOGGING,
                     options.getBoolean("frameDebug", false));
             framework.setFlag(Flags.DEBUG_FRAME,
                     options.getBoolean("debugFrame",
                             false));
             framework.setFlag(Flags.DEBUG_PREP_FRAME,
                     options.getBoolean("prepFrame", false));
             framework.setFlag(Flags.DEBUG_CONTOURS,
                     options.getBoolean("contours", false));
             framework.setFlag(Flags.DEBUG_POLY,
                     options.getBoolean("poly", false));
 
             framework.setFlag(Flags.DEBUG_DRAW_MARKERS,
                     options.getBoolean("marker", false));
             framework.setFlag(Flags.DEBUG_DRAW_SAMPLING,
                     options.getBoolean("sample", false));
             framework.setFlag(Flags.DEBUG_DRAW_MARKER_ID,
                     options.getBoolean("marker_id", false));
 
             switch (options.getInt("bin", 0)) {
                 case 0:
                     // default is normal threshold
                     framework.setFlag(Flags.USE_CANNY, false);
                     framework.setFlag(Flags.USE_ADAPTIVE, false);
                     break;
                 case 1:
                     framework.setFlag(Flags.USE_ADAPTIVE, true);
                     framework.setFlag(Flags.USE_CANNY, false);
                     break;
                 case 2:
                     framework.setFlag(Flags.USE_CANNY, true);
                     framework.setFlag(Flags.USE_ADAPTIVE, false);
                     break;
                 default:
                     // do nothing
             }
         }
 
         // Add some test entities:
         Tracking one = new Tracking(42, true, oneData);
         Tracking two = new Tracking(234, true, twoData);
         framework.registerEntity(one);
         framework.registerEntity(two);
 
         // Call on create:
         framework.onCreate((ViewGroup) findViewById(R.id
                 .group));
     }
 
     public void onResume() {
         super.onResume();
         // Call on resume:
         framework.onResume();
     }
 
     public void onPause() {
         super.onPause();
         // Call on pause:
         framework.onPause();
     }
 
     public void onDestroy() {
         super.onDestroy();
         // Call on destroy:
         framework.onDestroy();
     }
 }
