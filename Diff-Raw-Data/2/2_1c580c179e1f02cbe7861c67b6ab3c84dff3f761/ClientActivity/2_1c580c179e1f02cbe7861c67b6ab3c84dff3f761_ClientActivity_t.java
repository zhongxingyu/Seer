 package ntu.real.sense;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.List;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageButton;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 public class ClientActivity extends Activity implements SensorEventListener {
 
 	SensorManager sensorManager;
 
 	RealSurface surface;
 	RelativeLayout layout;
 	int CurrentButtonNumber = 0; // CurrentButtonNumber流水號 設定物件ID
 
 	Agent mca = Global.mClientAgent;
 	int cId;
 	int users;
 
 	Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message m) {
 			switch (m.what) {
 
 			// 收到傳遞照片的訊息
 			case 0x103:
 				String show = (String) m.obj;
 				Toast.makeText(ClientActivity.this, show, Toast.LENGTH_SHORT)
 						.show();
 				break;
 			}
 
 		}
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		DisplayMetrics dm = new DisplayMetrics(); 
 	    this.getWindowManager().getDefaultDisplay().getMetrics(dm);
 	    Log.e("123", dm.widthPixels + "" + dm.heightPixels);
 		// 隱藏title bar&notifiaction bar
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		// 設定顯示照片的layout
 		layout = new RelativeLayout(this);
 		layout.setBackgroundColor(Color.BLACK);
 		setContentView(layout);
 		// 讀取照片
 		ListAllPath demoTest = new ListAllPath();
 		File rootFile = new File("/sdcard/DCIM");
 		demoTest.print(rootFile, 0);
 
 		InputStream inputStream = null;
 		RelativeLayout RL_temp = new RelativeLayout(this);
 		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
 		RL_temp.setLayoutParams(params);
 		layout.addView(RL_temp);
 		// 注意顯示太多照片會out of memory
 		int index = demoTest.file_list.size();
 		if (index > 15) {
 			index = 13;
 		}
 		for (int i = 1; i < index; i++) {
 
 			Log.e("圖片網址：", demoTest.file_list.get(i));
 			Bitmap bitmap = decodeBitmap(demoTest.file_list.get(i));
 
 			ImageButton image_temp = new ImageButton(this);
 			image_temp.setImageBitmap(bitmap);
 			image_temp.setBackgroundColor(Color.BLUE);
 			Log.e("oriID", Integer.toString(image_temp.getId()));
 			image_temp.setId(i); // ID不能是零，不然會爛掉！
 			Log.e("newID", Integer.toString(image_temp.getId()));
 			image_temp.setLayoutParams(params);
 			params = new RelativeLayout.LayoutParams(180, 180);
 			params.setMargins(15, 15, 15, 15);
 			if (i == 1) {
 
 			}
 			if (i > 3) {
 				Log.e("在誰的下面：", Integer.toString(i - 3));
 				params.addRule(RelativeLayout.BELOW, (i - 3));
 			}
 			if (i % 3 != 1) {// 非列首的條件，要margin
 				Log.e("在誰的右邊：", Integer.toString(i - 1));
 				params.addRule(RelativeLayout.RIGHT_OF, (i - 1));
 			}
 
 			image_temp.setLayoutParams(params);
 			RL_temp.addView(image_temp);
 
 		}
 
 		Global.flagIsPlaying = true;
 		// 註冊orientation sensor
 		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 		List<Sensor> sensors = sensorManager
 				.getSensorList(Sensor.TYPE_ORIENTATION);
 		if (sensors.size() > 0) {
 			sensorManager.registerListener(this, sensors.get(0),
 					SensorManager.SENSOR_DELAY_NORMAL);
 		}
 
 		// 加入RealSense
		surface = new RealSurface(this, dm.widthPixels, dm.heightPixels);
 		this.addContentView(surface, new LayoutParams(LayoutParams.FILL_PARENT,
 				LayoutParams.FILL_PARENT));
 
 		String m = mca.read();
 		if ("init".equals(m)) {
 			cId = Integer.parseInt(mca.read());
 			users = Integer.parseInt(mca.read());
 			Log.e("init", cId + ":" + users);
 			for (int i = 0; i < users; i++) {
 				surface.target.add(new Target(Global.userName[i], 0, Global.userColor[i]));
 			}
 		}
 		// 設定繪圖與傳遞照片之Thread
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				while (Global.flagIsPlaying) {
 					surface.drawView();
 					if (surface.flagCanSend) {
 						surface.flagCanSend = false;
 						String show = "Send to:";
 						for (Target t : surface.selected) {
 							show += (t.name + " ");
 						}
 						Message m = new Message();
 						m.what = 0x103;
 						m.obj = show;
 						handler.sendMessage(m);
 						surface.selected.clear();
 					}
 					if (surface.flagClick) {
 						surface.flagClick = false;
 						Message m = new Message();
 						m.what = 0x102;
 						handler.sendMessage(m);
 					}
 
 					try {
 						Thread.sleep(100);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}).start();
 
 		// 從server接訊息的thread
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				while (Global.flagIsPlaying) {
 					String m = mca.read();
 
 					if ("setdeg".equals(m)) {
 						int who = Integer.parseInt(mca.read());
 						int deg = Integer.parseInt(mca.read());
 						surface.target.get(who).degree = deg;
 					}
 				}
 			}
 		}).start();
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		sensorManager.unregisterListener(this);
 		Global.flagIsPlaying = false;
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		// TODO Auto-generated method stub
 
 		surface.myDeg = (int) event.values[0];
 		mca.write("setdeg");
 		mca.write("" + surface.myDeg);
 
 	}
 
 	private Bitmap decodeBitmap(String path) {
 		BitmapFactory.Options op = new BitmapFactory.Options();
 		op.inJustDecodeBounds = true;
 		op.inSampleSize = 4;
 		Bitmap bmp = BitmapFactory.decodeFile(path, op);
 		op.inJustDecodeBounds = false;
 		bmp = BitmapFactory.decodeFile(path, op);
 		return bmp;
 	}
 }
