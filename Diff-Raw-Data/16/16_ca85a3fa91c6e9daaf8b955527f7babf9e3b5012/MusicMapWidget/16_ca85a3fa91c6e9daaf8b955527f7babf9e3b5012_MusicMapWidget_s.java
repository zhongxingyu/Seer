 package org.apps.notsorandom;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.opengl.GLSurfaceView;
 import android.os.Build;
 import android.util.AttributeSet;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import java.util.ArrayList;
 
 import org.apps.notsorandom.MusicMapView.MapMode;
 
 /**
  * Custom view containing the map views, labels and all other controls.
  * The currently defined map views are regular and GL, but others can be
  * defined.  Only one map view is visible at a time.
  */
 public class MusicMapWidget extends RelativeLayout implements View.OnTouchListener {
     private static final String TAG = MusicMapWidget.class.getSimpleName();
 
     private GLSurfaceView glView_;
     private MusicMapGLView musicMapView_;
     private TextView[] modeButtons_ = null;
 
 
     public MusicMapWidget(Context context, AttributeSet attrs) {
         super(context, attrs);
 
         LayoutInflater inflater = (LayoutInflater) context
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View view = inflater.inflate(R.layout.music_map_widget, this, true);
 
         FrameLayout mapFrame = (FrameLayout) view.findViewById(R.id.music_map_frame);
 
         musicMapView_ = new MusicMapGLView(context);
         glView_ = musicMapView_.getGlView();
 
         String xLabel = "softer / harder";
         String yLabel = "slower / faster";
         if (!isInEditMode()) {
             Config config = MusicPlayerApp.getConfig();
             if (config != null) {
                 xLabel = config.getXcomponent().getLabel();
                 yLabel = config.getYcomponent().getLabel();
             }
         }
 
         // Deal with the column labels around the music map
         TextView tvrl = (TextView) view.findViewById(R.id.column_label);
         tvrl.setText(xLabel);
         tvrl.setOnTouchListener(this);
        tvrl.setTranslationX(44);
         tvrl = (TextView) view.findViewById(R.id.row_label);
         tvrl.setRotation(-90);
         WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
         Display display = wm.getDefaultDisplay();
         Point size = new Point();
         display.getSize(size);
         if (size.x > 720) {
             tvrl.setTranslationX(-80);   // nudge left (for S4)
         } else {
             tvrl.setTranslationX(-50);   // nudge left
         }
         tvrl.setTranslationY(-62);   // nudge up
         tvrl.setText(yLabel);
         tvrl.setOnTouchListener(this);
 /*
         RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(600, 600);
         lp.addRule(RelativeLayout.BELOW, R.id.column_label);
         lp.setMargins(50, 50, 0, 0);
         lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
         lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
         lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
         addView(musicMapView_, lp);
         addView(glView_, lp);
 */
         mapFrame.addView(musicMapView_);
         mapFrame.addView(glView_);
 
         glView_.setVisibility(View.GONE);
         //RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.player_layout);
         //rl.addView(musicMapView_, lp);
         musicMapView_.setId(R.id.music_map);
 
         // Labeled Mode Buttons:
         // Select, Place, 3D, Animate
         LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.mode_selector, null);
         FrameLayout modeFrame = (FrameLayout) view.findViewById(R.id.mode_button_frame);
         modeFrame.addView(ll);
 /*
         RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(560, 100);
         lp.addRule(RelativeLayout.BELOW, R.id.music_map_frame);
         lp.addRule(RelativeLayout.ALIGN_LEFT, R.id.music_map_frame);
         addView(ll, lp);
 */
         modeButtons_ = new TextView[MapMode.values().length];
         ArrayList<View> arrTouchables = new ArrayList<View>(modeButtons_.length + 1);
         int idx = MapMode.SelectMode.ordinal();
         modeButtons_[idx] = (TextView) ll.findViewById(R.id.selectMode);
         modeButtons_[idx].setOnTouchListener(this);
         arrTouchables.add(modeButtons_[idx]);
         idx = MapMode.PlaceMode.ordinal();
         modeButtons_[idx] = (TextView) ll.findViewById(R.id.placeMode);
         modeButtons_[idx].setOnTouchListener(this);
         arrTouchables.add(modeButtons_[idx]);
         idx = MapMode.ThreeDMode.ordinal();
         modeButtons_[idx] = (TextView) ll.findViewById(R.id.threeDMode);
         modeButtons_[idx].setOnTouchListener(this);
         arrTouchables.add(modeButtons_[idx]);
         idx = MusicMapView.MapMode.AnimateMode.ordinal();
         modeButtons_[idx] = (TextView) ll.findViewById(R.id.animateMode);
         modeButtons_[idx].setOnTouchListener(this);
         arrTouchables.add(modeButtons_[idx]);
 
         arrTouchables.add(musicMapView_);
         addTouchables(arrTouchables);
 
         modeButtons_[MusicMapView.getMapMode().ordinal()].setTextColor(Color.GREEN);
     }
 
     public MusicMapWidget(Context context) {
         this(context, null);
     }
 
     public void cleanupViews() {
         if (musicMapView_ != null) {
             musicMapView_.cleanupGlView();
         }
     }
 
     @Override
     protected void onSizeChanged(int w, int h, int oldw, int oldh) {
         super.onSizeChanged(w, h, oldw, oldh);
 
     }
 
     public MusicMapView getMusicMapView() {
         return musicMapView_;
     }
 
     public GLSurfaceView getGlView() {
         return glView_;
     }
 
     /**
      * Handle touch events for the column and row labels, and the mode buttons.
      */
     @Override
     public boolean onTouch(View view, MotionEvent motionEvent) {
         int action = motionEvent.getActionMasked();
         if (action == MotionEvent.ACTION_DOWN ||
                 action == MotionEvent.ACTION_POINTER_DOWN) {
             return true;
         } else
         if (action == MotionEvent.ACTION_UP ||
                 action == MotionEvent.ACTION_POINTER_UP) {
 
             // save the old map mode
             MapMode oldMapMode = MusicMapView.getMapMode();
 
             switch (view.getId()) {
                 case R.id.column_label:
                 case R.id.row_label:
                     SenseComponent xc;
                     SenseComponent yc;
                     SenseComponent zc;
                     SenseComponent wc;
                     Config config = MusicPlayerApp.getConfig();
 
                     TextView tvxc = (TextView) findViewById(R.id.column_label);
                     TextView tvyc = (TextView) findViewById(R.id.row_label);
                     if (tvxc == view) {
                         MusicPlayerApp.log(TAG, "X component touched");
                         xc = config.getZcomponent();
                         yc = config.getYcomponent();
                         zc = config.getXcomponent();
                         wc = config.getWcomponent();
                         tvxc.setText(xc.getLabel());
                     } else
                     if (tvyc == view) {
                         MusicPlayerApp.log(TAG, "Y component touched");
                         xc = config.getXcomponent();
                         yc = config.getZcomponent();
                         zc = config.getYcomponent();
                         wc = config.getWcomponent();
                         tvyc.setText(yc.getLabel());
                     } else
                         return false;
 
                     config.setXYZWcomponents(xc, yc, zc, wc);
                     break;
                 case R.id.selectMode:
                     MusicPlayerApp.log(TAG, "Select Mode pressed");
                     MusicMapView.setMapMode(MusicMapView.MapMode.SelectMode);
                     MusicMapView.setPlaceMode(false);
                     break;
                 case R.id.placeMode:
                     MusicPlayerApp.log(TAG, "Place Mode pressed");
                     MusicMapView.setMapMode(MusicMapView.MapMode.PlaceMode);
                     MusicMapView.setPlaceMode(true);
                     break;
                 case R.id.threeDMode:
                     MusicMapView.setMapMode(MusicMapView.MapMode.ThreeDMode);
                     break;
                 case R.id.animateMode:
                     MusicMapView.setMapMode(MusicMapView.MapMode.AnimateMode);
                     break;
                 //TODO case R.id.fullScreenMode:
                 default:
                     MusicPlayerApp.log(TAG, "Unknown control pressed ID=" + view.getId());
                     return false;
             }
 
             MusicMapView.MapMode newMapMode = MusicMapView.getMapMode();
             if (oldMapMode != newMapMode) {
                 int idx = oldMapMode.ordinal();
                 if (idx >= 0 && idx < modeButtons_.length)
                     modeButtons_[idx].setTextColor(Color.WHITE);
 
                 if (newMapMode == MapMode.ThreeDMode) {
                     musicMapView_.setVisibility(View.INVISIBLE);
                     musicMapView_.setFrustum();
                     glView_.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                     glView_.setVisibility(View.VISIBLE);
                 } else if (newMapMode == MapMode.AnimateMode) {
                     musicMapView_.setVisibility(View.INVISIBLE);
                     musicMapView_.setFrustum();
                     glView_.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                     glView_.setVisibility(View.VISIBLE);
                 } else if (newMapMode == MapMode.SelectMode || newMapMode == MapMode.PlaceMode) {
                     musicMapView_.setVisibility(View.VISIBLE);
                     glView_.setVisibility(View.GONE);
                 }
             }
 
             modeButtons_[newMapMode.ordinal()].setTextColor(Color.GREEN);
 
             musicMapView_.redrawMap();     // make the map redraw
             return true;
         }
 
         return false;
     }
 
     public void setLabels() {
         Config config = MusicPlayerApp.getConfig();
 
         // Deal with the column labels around the music map
         TextView tvrl = (TextView) findViewById(R.id.column_label);
         tvrl.setText(config.getXcomponent().getLabel());
         tvrl = (TextView) findViewById(R.id.row_label);
         tvrl.setText(config.getYcomponent().getLabel());
     }
 
     public void setListener(MusicPlayer.OnPlayerListener listener) {
         musicMapView_.setListener(listener);
     }
 }
