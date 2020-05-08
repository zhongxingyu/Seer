 package proj.Position;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 
 import ntu.com.google.zxing.client.android.R;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
import android.R.integer;
import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Rect;
 import android.os.Environment;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.webkit.WebView;
import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class MyWebView extends WebView {
 
 	Context c;
 
 	// AlertDialog.Builder test;
 	Bitmap pin;
 
 	int pointR = 23;
 	int scrollX = 0;
 	int scrollY = 0;
 	boolean scrollFlag = true;
 
 	public MyWebView(Context context, AttributeSet attrs) {
 		// TODO Auto-generated constructor stub
 
 		super(context, attrs);
 		c = context;
 		this.setClickable(true);
 		
//		int l1h = ((Activity)context).findViewById(R.id.linearLayout1).getHeight();
//		int l1w =
//		View ac = ((Activity)context).findViewById(R.id.linearLayout1);//.getWidth();
//		
//ac.getHeight();
//		
//
//		
//		int l2h = ((Activity)context).findViewById(R.id.linearLayout2).getHeight();
//		int l2w =((Activity)context).findViewById(R.id.linearLayout2).getWidth();
		
		


		
		//Log.e("HFDUHFUDHSF", ""+l1h+""+l1w+""+l2h+""+l2w);
		
 
 		this.setInitialScale(120);
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 
 		try {
 
 			int[] x;
 			int[] y;
 
 			if (Global.MapId != null) {
 				// Not check touchEvent when MapId = null
 
 				// Parse x y points into x,y arrays
 				// JsonParser parser = new JsonParser();
 				//
 				// JSONObject jsonObj = new
 				// JSONObject(parser.getJsonRespon(Global.SDPathRoot + "/" +
 				// Global.MapDirName + "/" + Global.MapId + "/" + Global.MapId +
 				// ".json"));
 
 				JSONObject jsonObj = new JSONObject(JsonParser.getJsonRespon(Global.SDPathRoot + "/" + Global.MapDirName + "/" + Global.MapId + "/" + Global.MapId + ".json"));
 				JSONArray jsonObjArray = jsonObj.getJSONArray("points");
 
 				x = new int[jsonObjArray.length()];
 				y = new int[jsonObjArray.length()];
 
 				JSONObject jsonObjCoordJsonObject;
 				for (int i = 0; i < jsonObjArray.length(); i++) {
 
 					jsonObjCoordJsonObject = jsonObjArray.getJSONObject(i).getJSONObject("coord");
 
 					x[i] = jsonObjCoordJsonObject.getInt("x");
 					y[i] = jsonObjCoordJsonObject.getInt("y");
 
 				}
 
 				if (event.getAction() == MotionEvent.ACTION_UP) {
 
 					float tx = (getScrollX() + event.getX()) / getScale();
 					float ty = (getScrollY() + event.getY()) / getScale();
 					//==========================================================================================================
 					//wait to fix
 					float cr = pointR * pointR * getScale();
 					for (int i = 0; i < jsonObjArray.length(); i++) {
 
 						float d = ((tx - (x[i])) * (tx - (x[i])) + (ty - (y[i] - 30)) * (ty - (y[i] - 30))) * getScale();
 					//wait to fix
 					//==========================================================================================================	
 						
 						
 						// float sr = 0.5f;
 						// d = (float) Math.pow(d, sr);
 
 						if (d <= cr) {
 							// Toast.makeText(c, "test", Toast.LENGTH_SHORT)
 							// .show();
 
 							LayoutInflater factory = LayoutInflater.from(c);
 							final View v1 = factory.inflate(R.layout.contentview, null);
 
 							Builder showPointDesc;
 							showPointDesc = new AlertDialog.Builder(c);
 							// testt.setMessage(jsonObjArray.getJSONObject(i)
 							// .getString("description"));
 							showPointDesc.setTitle(jsonObjArray.getJSONObject(i).getString("title")).setPositiveButton("T{", new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog, int which) {
 									// TODO Auto-generated
 									// method stub
 
 								}
 							}).setView(v1);
 
 							TextView contentDesc = (TextView) v1.findViewById(R.id.ttt);
 							contentDesc.setTextSize(20);
 							contentDesc.setTextColor(Color.YELLOW);
 
 							if (jsonObjArray.getJSONObject(i).getString("description").equals("null") || jsonObjArray.getJSONObject(i).getString("description").equals("")) {
 								contentDesc.setText("aI|Lyz!");
 							} else {
 								contentDesc.setText(jsonObjArray.getJSONObject(i).getString("description"));
 							}
 							showPointDesc.show();
 							break;
 						}
 
 					}
 
 				}
 
 			}
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 		return super.onTouchEvent(event);
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 
 		int[] x;
 		int[] y;
 		try {
 
 			// Parse x y points into x,y arrays
 			File SD = Environment.getExternalStorageDirectory();
 			File test = new File(SD + "/Position/" + Global.MapId + "/" + Global.MapId + ".json");
 
 			FileReader in = new FileReader(test);
 			BufferedReader stdin = new BufferedReader(in);
 
 			String jsonString = "";
 			String jsonString1 = null;
 			while (((jsonString1 = stdin.readLine()) != null)) {
 				jsonString = jsonString + jsonString1;
 			}
 			in.close();
 
 			JSONObject jsonObj = new JSONObject(jsonString);
 
 			JSONArray jsonObjArray = jsonObj.getJSONArray("points");
 
 			x = new int[jsonObjArray.length()];
 			y = new int[jsonObjArray.length()];
 
 			JSONObject jsonObjCoordJsonObject;
 			for (int i = 0; i < jsonObjArray.length(); i++) {
 
 				jsonObjCoordJsonObject = jsonObjArray.getJSONObject(i).getJSONObject("coord");
 
 				x[i] = jsonObjCoordJsonObject.getInt("x");
 				y[i] = jsonObjCoordJsonObject.getInt("y");
 
 			}
 
 			// float x = pointX * getScale();
 			// float y = pointY * getScale();
 
 			// float r = pointR; // * getScale();
 
 			Matrix m = new Matrix();
 			JSONObject jsonObjCoordObject;
 			Bitmap b;
 
 			// Change pin scale when zoom I/O
 			m.postScale(getScale(), getScale());
 
 			for (int i = 0; i < jsonObjArray.length(); i++) {
 				float xx = x[i] * getScale();
 				float yy = y[i] * getScale();
 
 				jsonObjCoordObject = jsonObjArray.getJSONObject(i);
 
 				if (jsonObjCoordObject.getString("pointID").equals(Global.PointId)) {
 
 					pin = BitmapFactory.decodeResource(getResources(), R.drawable.marker_blue);
 
 				} else {
 					pin = BitmapFactory.decodeResource(getResources(), R.drawable.marker_pink);
 
 				}
 
 				b = Bitmap.createBitmap(pin, 0, 0, pin.getWidth(), pin.getHeight(), m, true);
 				// ====================================================================================================
 				canvas.drawBitmap(b, xx - ((b.getWidth() + 1 * getScale()) / 2), yy - (b.getHeight() + 1 * getScale()), null);
 				// ====================================================================================================
 
 			}
 
 			if (scrollFlag) {
 				scrollFlag = false;
 				scrollTo(scrollX, scrollY);
 			}
 
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 
 	}
 
 	public void focusPoint(int x, int y) {
 		scrollX = (int) (((x-getWidth()/2.0) * getScale()));
 		scrollY = (int) (((y-getHeight()/2.0) * getScale()));
 		
 //		this.getWidth();
 //		this.getHeight();
 //		
 //		scrollX = x;
 //		scrollY = y;
 		Rect outRect=new Rect();
 		getWindowVisibleDisplayFrame(outRect);
 		outRect.centerX();
 		outRect.centerY();

 		
 		scrollX = (int) ((x * getScale())-outRect.centerX());
 		scrollY = (int) (((y+60) * getScale())-outRect.centerY());
 		
 		scrollFlag = true;
 	}
 
 }
