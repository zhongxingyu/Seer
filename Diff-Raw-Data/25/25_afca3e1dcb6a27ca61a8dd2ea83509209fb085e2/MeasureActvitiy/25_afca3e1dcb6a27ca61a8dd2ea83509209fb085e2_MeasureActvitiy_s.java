 package tw.edu.ntust.dt.herbal;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.xml.datatype.Duration;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.graphics.AvoidXfermode.Mode;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.PixelFormat;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.SurfaceHolder;
 import android.view.SurfaceHolder.Callback;
 import android.view.SurfaceView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MeasureActvitiy extends Activity {
 
 	final static int MEASURE_BOUND_MIN = 0;
 	final static int MEASURE_BOUND_MAX = 100;
 
 	private final static String TAG = MeasureActvitiy.class.getSimpleName();
 
 	private TextView bmpTextView;
 	private SurfaceHolder holder;
 	private SurfaceView surface;
 	private List<Integer> points;
 	private Timer timer;
 
 	private boolean isRunning = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.measure);
 
 		bmpTextView = (TextView) findViewById(R.id.bmpTextView);
 		surface = (SurfaceView) findViewById(R.id.chart);
 		surface.setZOrderOnTop(true);
 
 		SurfaceHolder sfhTrack = surface.getHolder();
 		sfhTrack.setFormat(PixelFormat.TRANSPARENT);
 
 		holder = surface.getHolder();
 		holder.addCallback(new Callback() {
 			public void surfaceChanged(SurfaceHolder holder, int format,
 					int width, int height) {
 				drawSequence(holder, points);
 			}
 
 			@Override
 			public void surfaceCreated(SurfaceHolder holder) {
 				// TODO Auto-generated method stub
 				points = new ArrayList<Integer>();
 				points.add(50);
 				// timer = new Timer();
 			}
 
 			@Override
 			public void surfaceDestroyed(SurfaceHolder holder) {
 				// timer.cancel();
 			}
 		});
 	}
 
 	private void autuGenerate(List<Integer> points, int times) {
 		while (times-- != 0) {
 			final int tmp = points.get(points.size() - 1);
 			int last = points.get(points.size() - 1);
 			int r = (new Random().nextInt()) % 2 == 0 ? 1 : -1;
 			int d = 0;
 
 			if (last + r * 2 < MEASURE_BOUND_MIN) {
 				last -= r * 2;
 				d = -1;
 			} else if (last + r * 2 > MEASURE_BOUND_MAX) {
 				last -= r * 2;
 				d = -1;
 			} else {
 				last += r * 2;
 				d = 1;
 			}
 			this.runOnUiThread(new Runnable() {
 
 				@Override
 				public void run() {
 					bmpTextView.setText(String.valueOf(30 + tmp));
 				}
 			});
 			points.add(last);
 		}
 	}
 
 	private void drawSequence(SurfaceHolder holder, List<Integer> sequence) {
 		Paint p = new Paint();
 		p.setColor(Color.rgb(170, 170, 170));
 		p.setStrokeWidth(4);
 		holder.setFormat(PixelFormat.TRANSPARENT);
 		Canvas canvas = holder.lockCanvas();
 		canvas.drawColor(Color.TRANSPARENT,
 				android.graphics.PorterDuff.Mode.CLEAR);
 
 		int counter = sequence.size() - 1;
 		for (Integer point : sequence) {
 			canvas.drawPoint(counter--, point, p);
 		}
 		holder.unlockCanvasAndPost(canvas);
 	}
 
 	private void clear(SurfaceHolder holder) {
 		holder.setFormat(PixelFormat.TRANSPARENT);
 		Canvas canvas = holder.lockCanvas();
 		canvas.drawColor(Color.TRANSPARENT,
 				android.graphics.PorterDuff.Mode.CLEAR);
 		holder.unlockCanvasAndPost(canvas);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.measure, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.menu_start: {
 			/* http://teslacoilsw.com/teslaled/ */
 
 			String title = item.getTitle().toString();
 			if ("start".equals(title)) {
				String flashLightApk = "com.teslacoilsw.intent.FLASHLIGHT";
				if (isApkInstalled(flashLightApk)) {
					Toast.makeText(this, "請安裝 teslacoilsw apk",
							Toast.LENGTH_LONG).show();
					return true;
				}

				Intent intent = new Intent(flashLightApk);
				intent.putExtra("toggle", true);
				startService(intent);
 
 				TimerTask task = new TimerTask() {
 					@Override
 					public void run() {
 						autuGenerate(points, 10);
 						drawSequence(holder, points);
 					}
 				};
 
 				timer = new Timer();
 				timer.schedule(task, 1000, 100);
 				item.setTitle("stop");
 				isRunning = true;
 			} else if ("stop".equals(title)) {
 				timer.cancel();
 				item.setTitle("start");
 				isRunning = false;
 			}
 
 			return true;
 		}
 		case R.id.menu_next: {
 
 			if (isRunning == false) {
 				Intent intent = new Intent(this, ResultActivity.class);
 				intent.putExtra(ResultActivity.EXTRA_PRAM_BMP,
 						Integer.valueOf(bmpTextView.getText().toString()));
 				startActivity(intent);
 			} else {
				Toast.makeText(this, "請先按 STOP，再進行下一步。", Toast.LENGTH_LONG).show();
 			}
 			return true;
 		}
 		case R.id.menu_logout:
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private boolean isApkInstalled(String apkPackage) {
 		PackageManager pm = getPackageManager();
 		boolean isInstalled = false;
 		try {
 			pm.getPackageInfo(apkPackage, PackageManager.GET_ACTIVITIES);
 			isInstalled = true;
 		} catch (PackageManager.NameNotFoundException e) {
 			isInstalled = false;
 		}
 		return isInstalled;
 	}
 }
