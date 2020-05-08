 package de.bennir.DVBViewerController;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.os.Vibrator;
import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.androidquery.AQuery;
 import com.androidquery.callback.AjaxCallback;
 import com.androidquery.callback.AjaxStatus;
 
 public class RemoteFragment extends SherlockFragment {
     private static final String TAG = RemoteFragment.class.toString();
     private AQuery aq;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         // Inflate the layout for this fragment
         return inflater.inflate(R.layout.remote_fragment, container, false);
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         aq = ((DVBViewerControllerActivity) getSherlockActivity()).aq;
 
         ImageView remote = (ImageView) getSherlockActivity().findViewById(
                 R.id.remote);
 
         remote.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 if (event.getAction() == MotionEvent.ACTION_UP) {
                     int coords[] = new int[2];
                     v.getLocationOnScreen(coords);
 
                     int x = (int) event.getRawX() - coords[0];
                     int y = (int) event.getRawY() - coords[1];
 
                     if (x < 0 || y < 0) {
                         return false;
                     }
 
                     ImageView img = (ImageView) getActivity().findViewById(
                             R.id.remote_touchmap);
                     Bitmap bitmap = ((BitmapDrawable) img.getDrawable())
                             .getBitmap();

                    double scaleWidthRatio = (double) img.getWidth() /  (double) bitmap.getWidth();
                    double scaleHeightRatio = (double) img.getHeight() / (double) bitmap.getHeight();

                    int scaleX = (int) (x / scaleWidthRatio);
                    int scaleY = (int) (y / scaleHeightRatio);

                    int pixel = bitmap.getPixel(scaleX, scaleY);
 
                     int red = Color.red(pixel);
                     int green = Color.green(pixel);
                     int blue = Color.blue(pixel);
 
                     /**
                      * Button Events
                      */
 
                     // Chan+
                     if (red == 119 && blue == 119 && green == 119) {
                         sendCommand("sendUp");
                     }
                     // Chan-
                     if (red == 0 && blue == 0 && green == 0) {
                         sendCommand("sendDown");
                     }
                     // Vol+
                     if (red == 49 && blue == 49 && green == 49) {
                         sendCommand("sendRight");
                     }
                     // Vol-
                     if (red == 204 && blue == 204 && green == 204) {
                         sendCommand("sendLeft");
                     }
                     // Menu
                     if (red == 0 && blue == 255 && green == 255) {
                         sendCommand("sendMenu");
                     }
                     // Ok
                     if (red == 255 && blue == 255 && green == 0) {
                         sendCommand("sendOk");
                     }
                     // Back
                     if (red == 255 && blue == 0 && green == 168) {
                         sendCommand("sendBack");
                     }
                     // Red
                     if (red == 255 && blue == 0 && green == 0) {
                         sendCommand("sendRed");
                     }
                     // Yellow
                     if (red == 255 && blue == 0 && green == 255) {
                         sendCommand("sendYellow");
                     }
                     // Green
                     if (red == 0 && blue == 0 && green == 255) {
                         sendCommand("sendGreen");
                     }
                     // Blue
                     if (red == 0 && blue == 255 && green == 0) {
                         sendCommand("sendBlue");
                     }
                 }
 
                 return true;
             }
         });
 
     }
 
     void sendCommand(String command) {
         Log.d(TAG, "Remote Command: " + command);
 
         ((Vibrator) getSherlockActivity().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);
         if (!DVBViewerControllerActivity.dvbHost.equals("Demo Device")) {
             String url = "http://" +
                     DVBViewerControllerActivity.dvbIp + ":" +
                     DVBViewerControllerActivity.dvbPort +
                     "/?" + command;
 
             aq.ajax(url, String.class, new AjaxCallback<String>() {
 
                 @Override
                 public void callback(String url, String html, AjaxStatus status) {
 
                 }
 
             });
         }
     }
 }
