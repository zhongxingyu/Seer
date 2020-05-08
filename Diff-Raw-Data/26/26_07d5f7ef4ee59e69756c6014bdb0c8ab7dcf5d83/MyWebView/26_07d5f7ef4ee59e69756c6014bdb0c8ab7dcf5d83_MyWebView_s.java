 package proj.Position;
 
 import ntu.com.google.zxing.client.android.R;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
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
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class MyWebView extends WebView {
 
 	Context c;
 
 	Bitmap pin;
 
 	// Touch range
 	int pointR = 25;
 
 	int scrollX = 0;
 	int scrollY = 0;
 	boolean scrollFlag = true;
 
 	public MyWebView(Context context, AttributeSet attrs) {
 
 		super(context, attrs);
 		c = context;
 		this.setClickable(true);
 		this.setInitialScale(120);
 
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 
 		try {
 
 			int[] x;
 			int[] y;
 
 			if (Global.MapId != null) {
 				// No action on touchEvent when MapId = null
 
 				// Parse x y points into x,y arrays
 
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
 					// ==========================================================================================================
 					// wait to fix
 					float cr = pointR * pointR;
 					for (int i = 0; i < jsonObjArray.length(); i++) {
 
 						float d = ((tx - (x[i])) * (tx - (x[i])) + (ty - (y[i] - 35)) * (ty - (y[i] - 35)));
 						// wait to fix
 						// ==========================================================================================================
 
 						if (d <= cr) {
 
 							LayoutInflater factory = LayoutInflater.from(c);
 							final View v = factory.inflate(R.layout.contentview, null);
 
 							Builder showPointDesc;
 							showPointDesc = new AlertDialog.Builder(c);
 							showPointDesc.setTitle(jsonObjArray.getJSONObject(i).getString("title")).setPositiveButton("T{", new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog, int which) {
 									// TODO Auto-generated method stub
 
 								}
 							}).setView(v);
 
 							TextView contentDesc = (TextView) v.findViewById(R.id.textDesc);
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
 
 			Matrix m = new Matrix();
 			JSONObject jsonObjCoordObject;
 			Bitmap b;
 
 			// Resize pin when zoom I/O
 			m.postScale(getScale(), getScale());
 
 			for (int i = 0; i < jsonObjArray.length(); i++) {
 				float bx = x[i] * getScale();
 				float by = y[i] * getScale();
 
 				jsonObjCoordObject = jsonObjArray.getJSONObject(i);
 
 				if (jsonObjCoordObject.getString("pointID").equals(Global.PointId)) {
 
 					pin = BitmapFactory.decodeResource(getResources(), R.drawable.marker_blue);
 
 				} else {
 					pin = BitmapFactory.decodeResource(getResources(), R.drawable.marker_pink);
 
 				}
 
 				b = Bitmap.createBitmap(pin, 0, 0, pin.getWidth(), pin.getHeight(), m, true);
 				// ====================================================================================================
 				canvas.drawBitmap(b, bx - ((b.getWidth() + 1 * getScale()) / 2), by - (b.getHeight() + 1 * getScale()), null);
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
 
 		LayoutInflater factory = LayoutInflater.from(c);
 
 		final View v = factory.inflate(R.layout.main, null);
 		int fx = 0;
 		int fy = 0;
 		LinearLayout l1 = (LinearLayout) v.findViewById(R.id.linearLayout1);
 		LinearLayout l2 = (LinearLayout) v.findViewById(R.id.linearLayout2);
 		l1.measure(fx, fy);
 		l2.measure(fx, fy);
 
 		Rect outRect = new Rect();
 		getWindowVisibleDisplayFrame(outRect);
 
 		outRect.centerX();
 		outRect.centerY();
 
 		scrollX = (int) ((x * getScale()) - outRect.centerX());
 		scrollY = (int) (((y * getScale()) + l1.getMeasuredHeight() + l2.getMeasuredHeight()) - outRect.centerY());
 
 		scrollFlag = true;
 	}
 
 }
