 package ntu.real.sense;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.PixelFormat;
 import android.graphics.RectF;
 import android.graphics.PorterDuff.Mode;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.widget.Toast;
 
 public class RealSurface extends SurfaceView {
 	int selectedPhoto = -1;
 	int photoNum = 0;
 	boolean flagTouchUp = false;
 	boolean flagLongTouch = false;
 	boolean flagCanSend = false;
 	boolean flagClick = false;
 	int displayWidth = 480;
 	int displayHeight = 800;
 	int myDeg;
 	int radius = 130;
 	int cnt = -1;//0 for testing mode
 	float px, py;
 	SurfaceHolder holder;
 	ArrayList<Target> target = new ArrayList<Target>();
 	ArrayList<Target> showTarget = new ArrayList<Target>();
 	Set<Target> selected = new HashSet<Target>();
 	TouchPoint tp = new TouchPoint(radius);
 	
 	Handler h = new Handler() {
 		@Override
 		public void handleMessage(Message m) {
 			switch (m.what) {
 			case 0x101:
 				if (!flagTouchUp) {
 					setTempTarget();
 					flagLongTouch = true;
 				}
 				flagTouchUp = false;
 				break;
 
 			}
 		}
 	};
 
 	public RealSurface(Context context) {
 		super(context);
 		setZOrderOnTop(true);
 		holder = getHolder();
 		holder.setFormat(PixelFormat.TRANSPARENT);
 		// TODO Auto-generated constructor stub
 	}
 
 	public RealSurface(Context context, int width, int height, int num) {
 		super(context);
 		setZOrderOnTop(true);
 		holder = getHolder();
 		holder.setFormat(PixelFormat.TRANSPARENT);
 		displayWidth = width;
 		displayHeight = height;
 		radius = radius * displayWidth / 768;
 		photoNum = num;
 		// TODO Auto-generated constructor stub
 	}
 
 	void drawView() {
 		
 		boolean locked = false;
 		Canvas canvas = holder.lockCanvas();
 		locked = true;
 		
		if (canvas != null && selectedPhoto < photoNum && selectedPhoto >=1) {
 			// canvas.drawColor(Color.argb(0, 0, 0, 0));
 			canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
 			//左右兩排相片的radius設成較小
 			if (flagLongTouch) {
 				if(selectedPhoto != 2 && selectedPhoto != 5 && selectedPhoto != 8 && selectedPhoto != 11){
 					radius = radius * 8 / 10;
 				}
 				Paint p2 = new Paint();
 				p2.setColor(Color.WHITE);
 				canvas.drawCircle(px, py, radius * 1.5f, p2);
 				Paint p = new Paint();
 				p.setColor(Color.RED);
 				// 除去title bar跟notification bar的高度
 				
 				canvas.drawCircle(px, py, radius, p);
 				
 				for (Target t : showTarget) {
 
 					float deg = (int) (t.degree) % 360;
 					Paint p3 = new Paint();
 					p3.setColor(t.color);
 					p3.setTextSize(32);
 
 					RectF oval = new RectF();
 					oval.left = px - radius;
 					oval.top = py - radius;
 					oval.right = px + radius;
 					oval.bottom = py + radius;
 					canvas.drawArc(oval, deg + 60, 60, true, p3);
 
 					Log.e("deg", deg + "");
 
 					double ox = 200 * Math.cos((deg + 90) / 180 * Math.PI);
 					double oy = 200 * Math.sin((deg + 90) / 180 * Math.PI);
 
 					canvas.drawText(t.name, (float) (px + ox) - 50,
 							(float) (py + oy), p3);
 				}
 
 				canvas.drawCircle(px, py, radius - 5, p2);
 				if(selectedPhoto != 2 && selectedPhoto != 5 && selectedPhoto != 8 && selectedPhoto != 11){
 					radius = radius * 10 / 8;
 				}
 			}
 			holder.unlockCanvasAndPost(canvas);
 			locked = false;
 		}if (canvas != null && locked == true){
 			holder.unlockCanvasAndPost(canvas);
 		}
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent e) {
 		Log.e("sur", "touch");
 		if (!flagLongTouch) {
 			px = e.getX();
 			py = e.getY();
 			tp.setTouch(e.getX(), e.getY());
 		}
 		switch (e.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			h.removeMessages(0x101);
 			h.sendEmptyMessageDelayed(0x101, 500);
 			flagTouchUp = false;
 			setSelectedNumber(e.getX(), e.getY());
 			return true;
 		case MotionEvent.ACTION_MOVE:
 			Log.e("tar", "  ");
 			if (flagLongTouch) {
 				double deg = tp.moveTouch(e.getX(), e.getY());
 
 				if (deg == -1) {
 					selected.clear();
 				} else {
 					for (Target t : showTarget) {
 
 						float td = t.degree;
 						while (td < 0) {
 							td += 360;
 						}
 						Log.e("tar", t.name + ":" + td + " " + deg);
 						if (td - deg < 30 && td - deg > -30) {
 							selected.add(t);
 						}
 					}
 				}
 			}
 			break;
 		case MotionEvent.ACTION_UP:
 			flagTouchUp = true;
 			if (flagLongTouch) {
 				flagLongTouch = false;
 
 				boolean inrange = tp.removeTouch(e.getX(), e.getY());
 				if (inrange && selected.size() > 0) {
 
 					flagCanSend = true;
 				}
 				// else {
 				// Toast.makeText(this.getContext(), "not in range",
 				// Toast.LENGTH_LONG).show();
 				// }
 			} else {
 				flagClick = true;
 			}
 			return false;
 		}
 		return true;
 	}
 	
 	public void setSelectedNumber(float x, float y){
 		int centerWidth = displayWidth / 2;
 		int centerHeight = displayHeight / 2;
 		int imgWidth = displayWidth / 4;
 		int imgHeight = displayHeight / 4;
 		int layoutMargin = displayWidth / 10;
 		int imgMargin = displayWidth / 100;
 		
 		if(x >= layoutMargin + imgMargin && x <= layoutMargin + imgMargin + imgWidth){
 			if(y >= layoutMargin + imgMargin && y<= layoutMargin + imgMargin + imgWidth){
 				selectedPhoto = 1;
 			}else if(y >= layoutMargin + imgMargin * 3 + imgWidth && y <= layoutMargin + imgMargin * 3 + imgWidth * 2){
 				selectedPhoto = 4;
 			}else if(y >= layoutMargin + imgMargin * 5 + imgWidth * 2 && y <= layoutMargin + imgMargin * 5 + imgWidth * 3){
 				selectedPhoto = 7;
 			}else if(y >= layoutMargin + imgMargin * 7 + imgWidth * 3 && y <= layoutMargin + imgMargin * 7 + imgWidth * 4){
 				selectedPhoto = 10;
 			}else{
 				selectedPhoto = -1;
 			}
 		}else if(x >= layoutMargin + imgMargin * 3 + imgWidth && x <= layoutMargin + imgMargin * 3 + imgWidth * 2){
 			if(y >= layoutMargin + imgMargin && y<= layoutMargin + imgMargin + imgWidth){
 				selectedPhoto = 2;
 			}else if(y >= layoutMargin + imgMargin * 3 + imgWidth && y <= layoutMargin + imgMargin * 3 + imgWidth * 2){
 				selectedPhoto = 5;
 			}else if(y >= layoutMargin + imgMargin * 5 + imgWidth * 2 && y <= layoutMargin + imgMargin * 5 + imgWidth * 3){
 				selectedPhoto = 8;
 			}else if(y >= layoutMargin + imgMargin * 7 + imgWidth * 3 && y <= layoutMargin + imgMargin * 7 + imgWidth * 4){
 				selectedPhoto = 11;
 			}else{
 				selectedPhoto = -1;
 			}
 		}else if(x >= layoutMargin + imgMargin * 5 + imgWidth * 2 && x <= layoutMargin + imgMargin * 5 + imgWidth * 3){
 			if(y >= layoutMargin + imgMargin && y<= layoutMargin + imgMargin + imgWidth){
 				selectedPhoto = 3;
 			}else if(y >= layoutMargin + imgMargin * 3 + imgWidth && y <= layoutMargin + imgMargin * 3 + imgWidth * 2){
 				selectedPhoto = 6;
 			}else if(y >= layoutMargin + imgMargin * 5 + imgWidth * 2 && y <= layoutMargin + imgMargin * 5 + imgWidth * 3){
 				selectedPhoto = 9;
 			}else if(y >= layoutMargin + imgMargin * 7 + imgWidth * 3 && y <= layoutMargin + imgMargin * 7 + imgWidth * 4){
 				selectedPhoto = 12;
 			}else{
 				selectedPhoto = -1;
 			}
 		}else{
 			selectedPhoto = -1;
 		}
 		Log.e("selectedPhotoIndex", selectedPhoto + "");
 		
 	}
 
 	
 	//建立圓弧
 	void setTempTarget() {
 		showTarget.clear();
 
 		if (cnt >= 0) {
 			cnt = (cnt + 1) % 2;
 		}
 
 		Target tmp[] = null;
 		if (cnt < 0) {
 			tmp = new Target[target.size()];
 			for (int i = 0; i < tmp.length; i++) {
 				tmp[i] = target.get(i).clone(myDeg);
 			}
 		} else {
 
 			tmp = new Target[5];
 			tmp[0] = new Target(Global.userName[0], 0, Global.userColor[0]);
 			tmp[1] = new Target(Global.userName[1], 30, Global.userColor[1]);
 			tmp[2] = new Target(Global.userName[2], 50, Global.userColor[2]);
 			tmp[3] = new Target(Global.userName[3], 260, Global.userColor[3]);
 			tmp[4] = new Target(Global.userName[4], 280, Global.userColor[4]);
 		}
 		float minDeg = 180 / tmp.length;
 		minDeg = 45;
 		if (cnt <= 0) {
 			for (int j = 1; j < tmp.length; j++) {
 				for (int i = 0; i < tmp.length - j; i++) {
 					if (tmp[i].degree > tmp[i + 1].degree) {
 						Target t = tmp[i];
 						tmp[i] = tmp[i + 1];
 						tmp[i + 1] = t;
 					}
 				}
 			}
 
 			for (int i = 1; i < tmp.length; i++) {
 				if (tmp[i].degree - tmp[i - 1].degree < minDeg) {
 					float d = tmp[i - 1].degree + minDeg;
 					if (d < 360 - minDeg) {
 						tmp[i].degree = tmp[i - 1].degree + minDeg;
 					}
 				}
 
 			}
 			if (360 - tmp[tmp.length - 1].degree < minDeg) {
 				tmp[tmp.length - 1].degree = 360 - minDeg;
 			}
 			for (int i = tmp.length - 2; i > 0; i--) {
 				if (tmp[i + 1].degree - tmp[i].degree < minDeg) {
 					float d = tmp[i].degree - minDeg;
 					if (d > 0) {
 						tmp[i].degree = tmp[i + 1].degree - minDeg;
 					}
 				}
 
 			}
 		}
 		for (int i = 0; i < tmp.length; i++) {
 			showTarget.add(tmp[i]);
 		}
 	}
 }
