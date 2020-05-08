 package ch.ethz.inf.vs.android.siwehrli.antitheft;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.app.Activity;
 import android.app.NotificationManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.util.FloatMath;
 import android.util.Log;
 import android.view.Menu;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.SeekBar;
 import android.widget.ToggleButton;
 
 public class MainActivity extends Activity {
 	private static final String SETTINGS_NAME = "Settings";
 
 	public static final boolean ACTIVATE_DEFAULT = false;
 	public static final int SENSITIVITY_DEFAULT = 60;
 	public static final int TIMEOUT_DEFAULT = 5;
 	public static final boolean INFORM_DEFAULT = false;
 	public static final String PHONE_NUMBER_DEFAULT = "";
 
 	private boolean activate = ACTIVATE_DEFAULT;
 	private int sensitivity = SENSITIVITY_DEFAULT;
 	private int timeout = TIMEOUT_DEFAULT;
 	private boolean inform = INFORM_DEFAULT;
 	private String phoneNumber = PHONE_NUMBER_DEFAULT;
 
 	private SeekBar bar;
 
 	// graph drawer
 	private GraphDrawer graphDrawer;
 	private DrawThread drawThread;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// read settings into private fields
 		SharedPreferences settings = getSharedPreferences(SETTINGS_NAME,
 				MODE_PRIVATE);
 		this.activate = settings.getBoolean("activate", ACTIVATE_DEFAULT);
 		this.sensitivity = settings.getInt("sensitivity", SENSITIVITY_DEFAULT);
 		this.timeout = settings.getInt("timeout", TIMEOUT_DEFAULT);
 		this.inform = settings.getBoolean("inform", INFORM_DEFAULT);
 		this.phoneNumber = settings.getString("phone_number", "");
 
 		// set setting values to view components
 		ToggleButton tb = (ToggleButton) findViewById(R.id.toggleButtonActivate);
 		tb.setChecked(activate);
 		bar = (SeekBar) findViewById(R.id.seekBarSensitivity);
 		bar.setProgress(sensitivity);
 		EditText editText = (EditText) findViewById(R.id.editTextTimeout);
 		editText.setHint(timeout + " "
 				+ getResources().getString(R.string.timeout_unit));
 		CheckBox cb = (CheckBox) findViewById(R.id.checkBoxInform);
 		cb.setSelected(this.inform);
 		EditText editText2 = (EditText) findViewById(R.id.editTextPhoneNumber);
 		if (phoneNumber.equals(PHONE_NUMBER_DEFAULT))
 			editText2.setHint(getResources().getString(R.string.no_friend));
 		else
 			editText2.setHint(phoneNumber);
 
 		this.graphDrawer = new GraphDrawer(
 				(SurfaceView) findViewById(R.id.graph_surface));
 		this.drawThread = new DrawThread();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	public void onClickActivate(View view) {
 		activate = ((ToggleButton) view).isChecked();
 		onUpdateSettings(view);
 
 		if (activate) {
 			startAntiTheftService();
 		} else {
 			stopAntiTheftService();
 		}
 	}
 
	public voihd onUpdateSettings(View view) {
 		Log.d("Main", "Settings read and updated to local variables");
 		sensitivity = ((SeekBar) findViewById(R.id.seekBarSensitivity))
 				.getProgress();
 		try {
 			timeout = Integer
 					.parseInt(((EditText) findViewById(R.id.editTextTimeout))
 							.getText().toString());
 		} catch (NumberFormatException e) {
 
 		}
 
 		inform = ((CheckBox) findViewById(R.id.checkBoxInform)).isChecked();
 		phoneNumber = ((EditText) findViewById(R.id.editTextPhoneNumber))
 				.getText().toString();
 		if(phoneNumber.equals("") || phoneNumber.equals(PHONE_NUMBER_DEFAULT))
 			inform = false;
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		super.onNewIntent(intent);
 
 		if (!intent.getBooleanExtra("activate", ACTIVATE_DEFAULT)) {
 			stopAntiTheftService();
 		}
 	}
 
 	private void startAntiTheftService() {
 		Intent intent = new Intent(this, AntiTheftService.class);
 
 		intent.putExtra("ch.ethz.inf.vs.android.siwehrli.antitheft.activate",
 				activate);
 		intent.putExtra(
 				"ch.ethz.inf.vs.android.siwehrli.antitheft.sensitivity",
 				sensitivity);
 		intent.putExtra("ch.ethz.inf.vs.android.siwehrli.antitheft.timeout",
 				timeout);
 		intent.putExtra("ch.ethz.inf.vs.android.siwehrli.antitheft.inform",
 				inform);
 		intent.putExtra(
 				"ch.ethz.inf.vs.android.siwehrli.antitheft.phone_number",
 				phoneNumber);
 
 		startService(intent);
 
 		// play sound
 		MediaPlayer mp = MediaPlayer.create(this, R.raw.activate);
 		mp.setVolume(1.0f, 1.0f);
 		mp.start();
 
 	}
 
 	private void stopAntiTheftService() {
 		activate = false;
 
 		Intent intent = new Intent(this, AntiTheftService.class);
 
 		// cancel notification (if exists)
 		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		mNotificationManager.cancel(AntiTheftService.NOTIFICATION_ACTIVATED_ID);
 
 		// adjust activate button
 		ToggleButton tb = (ToggleButton) findViewById(R.id.toggleButtonActivate);
 		tb.setChecked(activate);
 
 		// stop service
 		stopService(intent);
 
 		// play sound
 		MediaPlayer mp = MediaPlayer.create(this, R.raw.deactivate);
 		mp.setVolume(1.0f, 1.0f);
 		mp.start();
 
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 
 		// save settings
 		SharedPreferences settings = getSharedPreferences(SETTINGS_NAME,
 				MODE_PRIVATE);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putBoolean("activate", activate);
 		editor.putInt("sensitivity", sensitivity);
 		editor.putInt("timeout", timeout);
 		editor.putBoolean("inform", inform);
 		editor.putString("phone_number", phoneNumber);
 		editor.commit(); // Commit changes to file!!!
 
 		// Graph Stuff
 		graphDrawer.mySensorManager
 				.unregisterListener(graphDrawer.mySensorListener);
 		drawThread.running = false;
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		// Graph Stuff
 		graphDrawer.mySensorManager.registerListener(
 				graphDrawer.mySensorListener, graphDrawer.sensor,
 				SensorManager.SENSOR_DELAY_FASTEST);
 		drawThread = new DrawThread();
 		drawThread.start();
 	}
 
 	/**
 	 * This inner class encapsulates the hole logic to draw the graph for the
 	 * sensor values.
 	 * 
 	 * @author Frederik
 	 * 
 	 */
 	private class GraphDrawer {
 
 		private Sensor sensor;
 		private SensorManager mySensorManager;
 		private SensorEventListener mySensorListener;
 		private SurfaceView graphSurface;
 		private SurfaceHolder surfaceViewHolder;
 		private Canvas graphCanvas;
 		private Paint graphPaint;
 		private Paint graphGridPaint;
 		private Paint graphGridLightPaint;
 		private Paint graphThresholdPaint;
 		private float max;
 		private float divider;
 		private static final float min_max = 10f;
 		private float scaleValue;
 		private float yZero;
 		private float value;
 		private static final float leftBorder = 60f;
 		private LinkedList<Float> graphYValues;
 		private float lv1 = 0, lv2 = 0, lv3 = 0;
 
 		GraphDrawer(SurfaceView surface) {
 			mySensorListener = new SensorEventListener() {
 
 				public void onSensorChanged(SensorEvent event) {
 					// take norm of values vector and calculate average sensor
 					// value
 
 					float v1 = event.values[0];
 					float v2 = event.values[1];
 					float v3 = event.values[2];
 
 					float change = (v1 - lv1) * (v1 - lv1) + (v2 - lv2)
 							* (v2 - lv2) + (v3 - lv3) * (v3 - lv3);
 					change = FloatMath.sqrt(change);
 
 					value = ((divider - 1) / divider * value)
 							+ (change / divider);
 					lv1 = v1;
 					lv2 = v2;
 					lv3 = v3;
 				}
 
 				public void onAccuracyChanged(Sensor sensor, int accuracy) {
 				}
 			};
 
 			// register listener
 			mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 			sensor = mySensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER)
 					.get(0);
 
 			// start sensor
 			mySensorManager.registerListener(mySensorListener, sensor,
 					SensorManager.SENSOR_DELAY_NORMAL);
 
 			// graph Stuff
 			graphSurface = surface;
 			surfaceViewHolder = graphSurface.getHolder();
 
 			graphPaint = new Paint();
 			graphPaint.setColor(Color.BLUE);
 			graphPaint.setStrokeWidth(3);
 			graphGridPaint = new Paint(graphPaint);
 			graphGridPaint.setColor(Color.BLACK);
 			graphGridPaint.setTextSize(20f);
 			graphGridLightPaint = new Paint(graphGridPaint);
 			graphGridLightPaint.setStrokeWidth(1f);
 			graphThresholdPaint = new Paint(graphGridPaint);
 			graphThresholdPaint.setColor(Color.RED);
 
 			max = min_max;
 			divider = 1;
 			scaleValue = 0f;
 			graphYValues = new LinkedList<Float>();
 
 		}
 
 		// Draw Logic
 		public void drawCanvas() {
 			graphCanvas = surfaceViewHolder.lockCanvas();
 			if (graphCanvas != null) {
 				// add value to be drawn
 				graphYValues.addFirst(value);
 				// reset divider --> new value
 				divider = 1;
 
 				// remove old points
 				if (graphYValues.size() > (graphCanvas.getWidth() - leftBorder) / 4) {
 					graphYValues.removeLast();
 				}
 
 				drawGraph();
 
 				// force canvas to be drawn
 				surfaceViewHolder.unlockCanvasAndPost(graphCanvas);
 			}
 		}
 
 		public void drawGraph() {
 
 			yZero = (float) graphCanvas.getHeight() - 20;
 
 			// draw graph grid
 			// set background
 			graphCanvas.drawColor(Color.WHITE);
 
 			// draw vertical line
 			graphCanvas.drawLine(leftBorder, 0f, leftBorder,
 					(float) graphCanvas.getHeight(), graphGridPaint);
 
 			// draw red Threshold line
 			float threshold = AntiTheftService.calculateNormThreshhold(bar
 					.getProgress());
 
 			graphCanvas.drawLine(leftBorder, yZero - (threshold * scaleValue),
 					(float) graphCanvas.getWidth(), yZero
 							- (threshold * scaleValue), graphThresholdPaint);
 
 			// draw base horizontal line
 			graphCanvas.drawLine(leftBorder, yZero,
 					(float) graphCanvas.getWidth(), yZero, graphGridPaint);
 			// add number to line
 			graphCanvas.drawText("0", 15f,
 					(float) graphCanvas.getHeight() - 12, graphGridPaint);
 
 			// draw top scale horizontal line
 			float top_scale = Math.round((max * 0.95f) / 10) * 10;
 			if (top_scale < 10) {
 				top_scale = 10f;
 			}
 			graphCanvas.drawLine(leftBorder, yZero - (top_scale * scaleValue),
 					(float) graphCanvas.getWidth(), yZero
 							- (top_scale * scaleValue), graphGridLightPaint);
 
 			// add number to line
 			graphCanvas.drawText("" + top_scale, 1f, yZero
 					- (top_scale * scaleValue - 20), graphGridPaint);
 
 			// draw mid scale horizontal line
 			float mid_scale = top_scale / 2;
 			graphCanvas.drawLine(leftBorder, yZero - (mid_scale * scaleValue),
 					(float) graphCanvas.getWidth(), yZero
 							- (mid_scale * scaleValue), graphGridLightPaint);
 
 			// add number to line
 			graphCanvas.drawText("" + mid_scale, 1f, yZero
 					- (mid_scale * scaleValue - 10), graphGridPaint);
 
 			// draw bot scale horizontal line
 			float bot_scale = top_scale / 4;
 			graphCanvas.drawLine(leftBorder, yZero - (bot_scale * scaleValue),
 					(float) graphCanvas.getWidth(), yZero
 							- (bot_scale * scaleValue), graphGridLightPaint);
 
 			// add number to line
 			graphCanvas.drawText("" + bot_scale, 1f, yZero
 					- (bot_scale * scaleValue - 10), graphGridPaint);
 
 			// draw upper scale horizontal line
 			float upper_scale = 3 * top_scale / 4;
 			graphCanvas.drawLine(leftBorder,
 					yZero - (upper_scale * scaleValue),
 					(float) graphCanvas.getWidth(), yZero
 							- (upper_scale * scaleValue), graphGridLightPaint);
 
 			// add number to line
 			graphCanvas.drawText("" + upper_scale, 1f, yZero
 					- (upper_scale * scaleValue - 10), graphGridPaint);
 
 			// draw top horizontal line
 			graphCanvas.drawLine(leftBorder, 1f,
 					(float) graphCanvas.getWidth(), 1f, graphGridPaint);
 			// add number to line
 			graphCanvas.drawText("Max: " + max, leftBorder + 2f, 22f,
 					graphGridPaint);
 
 			Iterator<Float> valuesIterator = graphYValues.iterator();
 
 			// fill values array, find max
 			float[] values = new float[4 * graphYValues.size()];
 			int i = 0;
 
 			max = min_max;
 
 			float tmp;
 			// prepare values array and find next max
 			while (valuesIterator.hasNext()) {
 				tmp = valuesIterator.next();
 				values[i] = graphCanvas.getWidth() - i;
 				values[i + 1] = yZero - (tmp * scaleValue);
 				values[i + 2] = graphCanvas.getWidth() - i;
 				values[i + 3] = yZero;
 				if (tmp > max) {
 					max = tmp;
 				}
 				i = i + 4;
 			}
 
 			// determine next scaleValue
 			float drawHeigth = (float) graphCanvas.getHeight() - 20;
 			scaleValue = drawHeigth / max;
 
 			// draw points
 			graphCanvas.drawLines(values, graphPaint);
 		}
 	}
 
 	// used for drawing on the SurfaceView
 	private class DrawThread extends Thread {
 
 		public Boolean running;
 
 		// used to fix to 30fps
 		private long sleepTime;
 		private long delay = 33;
 		private long beforeRender;
 
 		public DrawThread() {
 			running = false;
 		}
 
 		@Override
 		public void run() {
 			running = true;
 			while (running) {
 				beforeRender = SystemClock.currentThreadTimeMillis();
 
 				graphDrawer.drawCanvas();
 
 				sleepTime = delay
 						- (SystemClock.currentThreadTimeMillis() - beforeRender);
 
 				try {
 					// sleep until next frame
 					if (sleepTime > 0) {
 						Thread.sleep(sleepTime);
 					}
 				} catch (InterruptedException ex) {
 				}
 			}
 		}
 	}
 }
