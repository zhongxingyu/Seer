 package at.roadrunner.android.activity;
 
 import java.io.ByteArrayOutputStream;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.gesture.Gesture;
 import android.gesture.GestureOverlayView;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Base64;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Toast;
 import at.roadrunner.android.R;
 import at.roadrunner.android.couchdb.RequestWorker;
 import at.roadrunner.android.model.Log.LogType;
 
 public class Signature extends Activity {
 
 	private Gesture _gesture;
 	private GestureOverlayView _overlay;
 	private String _item;
 	private String _container;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_signature);
 		Bundle extras = getIntent().getExtras();
 		_item = extras.getString("item");
 		_container = extras.getString("container");
 		
 		_overlay = (GestureOverlayView) findViewById(R.id.gestures_overlay);
 		_overlay.addOnGestureListener(new GesturesProcessor());
 	}
 	
     public void addSignature(View view)  {
     	if (_gesture == null || _gesture.getStrokes().size() == 0)  {
     		Toast.makeText(this, "Please make a Signature", Toast.LENGTH_SHORT).show();
     		return;
     	}
     	
     	int targetWidth = 200;
     	double factor = _overlay.getWidth()/200;
     	int scaledHeight = (int) (_overlay.getHeight()/factor);
     	Bitmap bitmap = _gesture.toBitmap(targetWidth, scaledHeight, 0, Color.BLACK);
     	ByteArrayOutputStream byteStream = new ByteArrayOutputStream();  
     	bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
     	byte[] bytes = byteStream.toByteArray(); 
     	
     	JSONObject signature = new JSONObject();
     	JSONObject attachment = null;
     	try {
 			signature.put("content_type", "image/png");
 			signature.put("data", Base64.encodeToString(bytes, Base64.DEFAULT));
 	    	attachment = new JSONObject();
 	    	attachment.put("signature.png", signature);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
  
     	new RequestWorker(this).saveLog(new JSONArray().put(_item), LogType.UNREGISTER, _container, attachment);
     	finish();
     }
     
     public void cancelSignature(View view)  {
     	_overlay.clear(false);
     	_gesture = null;
     }
     
     private class GesturesProcessor implements GestureOverlayView.OnGestureListener {
         public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
         	_gesture = null;
         }
         public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
         	_gesture = overlay.getGesture();
         }
         public void onGesture(GestureOverlayView overlay, MotionEvent event) {}
         public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {}
     }
 }
