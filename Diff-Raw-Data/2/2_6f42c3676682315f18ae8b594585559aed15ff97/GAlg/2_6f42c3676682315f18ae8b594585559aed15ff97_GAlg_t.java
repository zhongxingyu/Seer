 package cz.urbangaming.galgs;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.content.Context;
 import android.content.pm.ConfigurationInfo;
 import android.opengl.GLSurfaceView;
 import android.os.Bundle;
 import android.support.v4.view.MotionEventCompat;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.SubMenu;
 
 /**
  * 
  * @author Michal Karm Babacek
  * @license GNU GPL 3.0
  * 
  */
 public class GAlg extends Activity {
     public static final String DEBUG_TAG = "KARM";
 
     private PointsRenderer pointsRenderer = null;
 
     // Menus begin
     private static final int WORK_MODE = 10;
     private static final int WORK_MODE_EDIT = 20;
     private static final int WORK_MODE_ADD = 30;
     private static final int WORK_MODE_DELETE = 40;
 
     private static final int REMOVE_ALL_POINTS = 50;
     private static final int ADD_RANDOM_POINTS = 60;
     // Menus end
 
     private int currentWorkMode = WORK_MODE_ADD;
 
     // /////// Some settings: Move it outside... /////////
 
     // TODO: THIS IS SO EPICLY WRONG! I must calculate it accordingly to display's density...
    public static final float POINT_SIZE = 7f;
     public static final int FINGER_ACCURACY = Math.round(POINT_SIZE) * 3;
     // No, it's not very convenient to have points too close to boundaries
     // public static final int BORDER_POINT_POSITION = Math.round(POINT_SIZE / 2);
     public static final int BORDER_POINT_POSITION = Math.round(POINT_SIZE) * 3;
     public static final int HOW_MANY_POINTS_GENERATE = Math.round(POINT_SIZE) * 2;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mGLSurfaceView = new GLSurfaceView(this);
         if (detectOpenGLES20()) {
             // Tell the surface view we want to create an OpenGL ES 2.0-compatible
             // context, and set an OpenGL ES 2.0-compatible renderer.
             mGLSurfaceView.setEGLContextClientVersion(2);
             pointsRenderer = new PointsRenderer();
             mGLSurfaceView.setRenderer(pointsRenderer);
         } else {
             // TODO: Handle as an unrecoverable error and leave the activity somehow...
         }
         setContentView(mGLSurfaceView);
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
         // Intentionally left blank
         return true;
     }
 
     public boolean onPrepareOptionsMenu(Menu menu) {
         super.onPrepareOptionsMenu(menu);
         menu.clear();
         SubMenu submenu = menu.addSubMenu(0, WORK_MODE, 0, R.string.work_mode);
         switch (currentWorkMode) {
         case WORK_MODE_ADD:
             submenu.add(0, WORK_MODE_DELETE, 0, R.string.workmode_delete);
             submenu.add(0, WORK_MODE_EDIT, 1, R.string.workmode_edit);
             break;
         case WORK_MODE_DELETE:
             submenu.add(0, WORK_MODE_ADD, 0, R.string.workmode_add);
             submenu.add(0, WORK_MODE_EDIT, 1, R.string.workmode_edit);
             break;
         case WORK_MODE_EDIT:
             submenu.add(0, WORK_MODE_ADD, 0, R.string.workmode_add);
             submenu.add(0, WORK_MODE_DELETE, 1, R.string.workmode_delete);
             break;
         default:
             // nothing
             break;
         }
         menu.add(1, REMOVE_ALL_POINTS, 1, R.string.remove_all_points);
         menu.add(1, ADD_RANDOM_POINTS, 2, R.string.generate_random_points);
         return true;
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
         boolean itemHandled = true;
         switch (item.getItemId()) {
         case WORK_MODE_ADD:
             currentWorkMode = WORK_MODE_ADD;
             break;
         case WORK_MODE_DELETE:
             currentWorkMode = WORK_MODE_DELETE;
             break;
         case WORK_MODE_EDIT:
             currentWorkMode = WORK_MODE_EDIT;
             break;
         case REMOVE_ALL_POINTS:
             pointsRenderer.clearScene();
             break;
         case ADD_RANDOM_POINTS:
             pointsRenderer.addRandomPoints();
             break;
         default:
             itemHandled = false;
             break;
         }
 
         return itemHandled;
     }
 
     private boolean detectOpenGLES20() {
         ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
         ConfigurationInfo info = am.getDeviceConfigurationInfo();
         return (info.reqGlEsVersion >= 0x20000);
     }
 
     @Override
     protected void onResume() {
         // Ideally a application should implement onResume() and onPause()
         // to take appropriate action when the activity looses focus
         super.onResume();
         mGLSurfaceView.onResume();
     }
 
     @Override
     protected void onPause() {
         // Ideally a game should implement onResume() and onPause()
         // to take appropriate action when the activity looses focus
         super.onPause();
         mGLSurfaceView.onPause();
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         int action = MotionEventCompat.getActionMasked(event);
         switch (action) {
         case (MotionEvent.ACTION_DOWN):
             float x = event.getAxisValue(MotionEvent.AXIS_X);
             float y = event.getAxisValue(MotionEvent.AXIS_Y);
             Log.d(DEBUG_TAG, "Action was DOWN [" + x + "," + y + "]");
             switch (currentWorkMode) {
             case WORK_MODE_ADD:
                 pointsRenderer.addVertex(x, y);
                 break;
             case WORK_MODE_DELETE:
                 pointsRenderer.removeVertex(x, y);
                 break;
             case WORK_MODE_EDIT:
                 pointsRenderer.selectVertex(x, y);
                 break;
             default:
                 break;
             }
             return true;
         case (MotionEvent.ACTION_MOVE):
             if (currentWorkMode == WORK_MODE_EDIT) {
                 pointsRenderer.moveSelectedVertexTo(event.getAxisValue(MotionEvent.AXIS_X), event.getAxisValue(MotionEvent.AXIS_Y));
             }
             return true;
         case (MotionEvent.ACTION_UP):
             Log.d(DEBUG_TAG, "Action was UP");
             if (currentWorkMode == WORK_MODE_EDIT) {
                 pointsRenderer.deselectVertex();
             }
             return true;
         case (MotionEvent.ACTION_CANCEL):
             Log.d(DEBUG_TAG, "Action was CANCEL");
             return true;
         case (MotionEvent.ACTION_OUTSIDE):
             Log.d(DEBUG_TAG, "Movement occurred outside bounds of current screen element");
             return true;
         default:
             return super.onTouchEvent(event);
         }
     }
 
     private GLSurfaceView mGLSurfaceView;
 }
