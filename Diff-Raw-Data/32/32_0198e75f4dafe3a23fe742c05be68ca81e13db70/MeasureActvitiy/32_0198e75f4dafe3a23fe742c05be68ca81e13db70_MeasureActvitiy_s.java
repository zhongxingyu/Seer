 package tw.edu.ntust.dt.herbal;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.util.Log;
 import android.util.Pair;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.SurfaceHolder;
 import android.view.View;
 import android.view.SurfaceHolder.Callback;
 import android.view.View.OnClickListener;
 import android.view.SurfaceView;
 import android.widget.Button;
 
 public class MeasureActvitiy extends Activity {
 
 	final static int MEASURE_BOUND_MIN = 0;
 	final static int MEASURE_BOUND_MAX = 100;
 
 	private final static String TAG = MeasureActvitiy.class.getSimpleName();
 
 	private SurfaceHolder holder;
 	private SurfaceView surface;
 	private List<Integer> points;
 	private Timer timer;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.measure);
 		surface = (SurfaceView) findViewById(R.id.chart);
 
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
 				timer = new Timer();
 			}
 
 			@Override
 			public void surfaceDestroyed(SurfaceHolder holder) {
 				timer.cancel();
 			}
 		});
 	}
 
 	private void autuGenerate(List<Integer> points, int times) {
 		while (times-- != 0) {
 			int last = points.get(points.size() - 1);
 			int r = (new Random().nextInt()) % 2 == 0 ? 1 : -1;
 			if (last + r * 2 < MEASURE_BOUND_MIN) {
 				last -= r * 2;
 			} else if (last + r * 2 > MEASURE_BOUND_MAX) {
 				last -= r * 2;
 			} else {
 				last += r * 2;
 			}
 			points.add(last);
 		}
 	}
 
 	private void drawSequence(SurfaceHolder holder, List<Integer> sequence) {
 		Paint p = new Paint();
 		p.setColor(Color.BLACK);
 		p.setStrokeWidth(4);
 		Canvas canvas = holder.lockCanvas();
 		canvas.drawColor(Color.WHITE);
 
 		int counter = sequence.size() - 1;
 		for (Integer point : sequence) {
 			canvas.drawPoint(counter--, point, p);
 		}
 		holder.unlockCanvasAndPost(canvas);
 	}
	
 	private void clear(SurfaceHolder holder) {
 		Canvas canvas = holder.lockCanvas();
 		canvas.drawColor(Color.WHITE);
 		holder.unlockCanvasAndPost(canvas);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.measure, menu);
 		return true;
 	}
 
 	private TimerTask task = new TimerTask() {
 
 		@Override
 		public void run() {
 			autuGenerate(points, 20);
 			drawSequence(holder, points);
 		}
 	};
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.menu_start: {
 			/* http://teslacoilsw.com/teslaled/ */
			Intent intent = new Intent("com.teslacoilsw.intent.FLASHLIGHT");
 			intent.putExtra("toggle", true);
 			startService(intent);
			
 			if (timer != null) {
 				try {
 					timer.schedule(task, 1000, 100);
 				} catch (Exception e) {
 					timer.cancel();
 					clear(holder);
 				}
 			}
 
 			return true;
 		}
 		case R.id.menu_next: {
 
 			Intent intent = new Intent(this, ResultActivity.class);
 			startActivity(intent);
 			return true;
 		}
 		case R.id.menu_logout:
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 }
