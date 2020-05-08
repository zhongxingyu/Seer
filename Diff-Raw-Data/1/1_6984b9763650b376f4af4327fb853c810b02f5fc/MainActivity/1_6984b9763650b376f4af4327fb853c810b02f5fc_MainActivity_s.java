 package com.example.testing;
 
 import java.util.ArrayList;
 
 import android.R.color;
 import android.graphics.Color;
 import android.graphics.ColorFilter;
 import android.hardware.Camera;
 import android.opengl.Visibility;
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.pm.ActivityInfo;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.GridLayout;
 import android.widget.ImageView;
 
 public class MainActivity extends Activity {
     // Variables definition:
     private Camera mCamera;
     private ARCameraPreview surfaceRender;
     private AnimEvents animation_0, animation_1, animation_2, animation_3;
     private int mDisplayWidth, mDisplayHeight;
     private ScaleFactor scaleDisp;
     private SetVisibility mVisibility;
     int previewId;
 
     @SuppressLint("NewApi")
     @Override
     protected void onCreate(Bundle savedInstanceState) {
 
         super.onCreate(savedInstanceState); // Salvez instanta anterioara
         setContentView(R.layout.activity_main); // Stabilesc interfata de
                                                 // afisare
 
         DisplayMetrics displayMetrics = new DisplayMetrics(); // Obtin
                                                               // coordonatele
                                                               // ecranului
         getWindowManager().getDefaultDisplay().getMetrics(displayMetrics); //
         mDisplayHeight = displayMetrics.heightPixels; //
         mDisplayWidth = displayMetrics.widthPixels; //
         scaleDisp = new ScaleFactor(mDisplayWidth, mDisplayHeight);
         previewId = 0;
 
         animation_0 = new AnimEvents(this, scaleDisp, previewId); // Adaug
                                                                   // suprafete
                                                                   // in care
         GridLayout mRootLayout = (GridLayout) findViewById(R.id.layout_root); // randez
                                                                               // si
                                                                               // le
                                                                               // leg
                                                                               // la
                                                                               // layout-ul
         animation_0.setLayoutParams(new LayoutParams(mDisplayWidth / 2,
                 mDisplayHeight / 2));//
         mRootLayout.addView(animation_0); // de baza
         previewId++;
 
         animation_1 = new AnimEvents(this, scaleDisp, previewId);
         animation_1.setLayoutParams(new LayoutParams(mDisplayWidth / 2,
                 mDisplayHeight / 2));
         mRootLayout.addView(animation_1);
         previewId++;
 
         animation_2 = new AnimEvents(this, scaleDisp, previewId);
         animation_2.setLayoutParams(new LayoutParams(mDisplayWidth / 2,
                 mDisplayHeight / 2));
         mRootLayout.addView(animation_2);
         previewId++;
 
         animation_3 = new AnimEvents(this, scaleDisp, previewId);
         animation_3.setLayoutParams(new LayoutParams(mDisplayWidth / 2,
                 mDisplayHeight / 2));
         mRootLayout.addView(animation_3);
 
         ArrayList<AnimEvents> listViews = new ArrayList<AnimEvents>(); // Create
                                                                        // display
                                                                        // list
                                                                        // with
                                                                        // all
                                                                        // rendereing
                                                                        // surfaces
         listViews.add(animation_0); //
         listViews.add(animation_1);
         listViews.add(animation_2);
         listViews.add(animation_3);
 
         surfaceRender = new ARCameraPreview(this, listViews, 240, 320); // Initializez
                                                                         // camera
                                                                         // cu
                                                                         // Surface
                                                                         // Holder
 
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // Setez
                                                                             // orientarea
                                                                             // pe
                                                                             // LOCK
                                                                             // LANDSCAPE
         AnimMainFrame r_1 = new AnimMainFrame(this); // Obtin legatura de pe
                                                      // display la FrameLayout
         r_1.setLayoutParams(new LayoutParams(mDisplayWidth / 2,
                 mDisplayHeight / 2));
         mRootLayout.addView(r_1);
         r_1.setVisibility(View.INVISIBLE);
         r_1.setBackgroundColor(color.black);
         mVisibility = new SetVisibility(listViews);
 
         animation_0.setVisibilityObject(mVisibility);
         animation_1.setVisibilityObject(mVisibility);
         animation_2.setVisibilityObject(mVisibility);
         animation_3.setVisibilityObject(mVisibility);
 
         if (r_1 != null) { // Randez continutul de la camera in FrameLayout
             r_1.addView(surfaceRender); // daca nu e intors null
         } //
         else { //
             Log.e("error in layout", "null exception"); // Tratez cazul de
                                                         // eroare daca nu e
                                                         // definit
         } // Frame_1
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     public void gallery(){
         Intent intent = new Intent();
     	intent.setType("image/*");
     	intent.setAction(Intent.ACTION_GET_CONTENT);//
     	startActivityForResult(Intent.createChooser(intent, "Select Picture"), BIND_IMPORTANT);
     }
 
 }
