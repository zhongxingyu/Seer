 package esposito.fall_detection;
 
 import java.util.Date;
 
 import org.openintents.sensorsimulator.hardware.Sensor;
 import org.openintents.sensorsimulator.hardware.SensorEvent;
 import org.openintents.sensorsimulator.hardware.SensorEventListener;
 import org.openintents.sensorsimulator.hardware.SensorManagerSimulator;
 
 //import android.hardware.Sensor;
 //import android.hardware.SensorEvent;
 //import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 
 public class FallDetector implements SensorEventListener {
 
 	private float mLastValues[] = new float[3];
 	protected final float RssTreshold = 2.8f;
 	private float mRssValues[] = new float[256];
 	private int mRssCount = 0;
 	private int mRssIndex = 0;
 	private long RssStartTime = 0;
 	protected final float VveWindow = 0.6f;
 	protected final float VveTreshold = -0.7f;
 	private final int OriOffset = 1000;
 	private final int OriWindow = 2000;
 	private long OriStartTime = 0;
 	protected final float OriTreshold = 60;
 	private final float OriConstraint = 0.75f;
 	private float OriValues[] = new float[256];
 	private int ori_index = 0;
 	protected float mLastXOri;
 	protected float mLastX;
 	protected float newX;
 	private GraphView mGraphView;
 
 	private SensorManagerSimulator mSensorManager;
 
 	private FallDetection activity;
 
 	public FallDetector(FallDetection activity) {
 		this.activity = activity;
 		this.mGraphView = activity.mGraphView;
 		// Code for accessing the real sensors
 		// mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 		// Sensor simulation code
 		mSensorManager = SensorManagerSimulator.getSystemService(activity,
 				activity.SENSOR_SERVICE);
 		mSensorManager.connectSimulator();
 	}
 
 	public void registerListeners() {
 		mSensorManager.registerListener(this,
 				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 				SensorManager.SENSOR_DELAY_UI);
 		mSensorManager.registerListener(this,
 				mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
 				SensorManager.SENSOR_DELAY_UI);
 		mSensorManager.registerListener(this,
 				mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
 				SensorManager.SENSOR_DELAY_UI);
 	}
 
 	public void unregisterListeners() {
 		mSensorManager.unregisterListener(this);
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		synchronized (this) {
 			if (mGraphView.mBitmap != null) {
 				if (activity.fall_detected) {
 					if (!activity.handling_fall) {
 						activity.handling_fall = true;
 						activity.handle_fall();
 					}
 				} else {
 					final Canvas canvas = mGraphView.mCanvas;
 					final Paint paint = mGraphView.mPaint;
 					Date date = new Date();
 					if (event.type == Sensor.TYPE_ACCELEROMETER) {
 						// determine stepsize
 						newX = mLastX + mGraphView.mSpeed;
 						// Calculalte RSS
 						float rss = (float) Math.sqrt(Math.pow(event.values[0],
 								2)
 								+ Math.pow(event.values[1], 2)
 								+ Math.pow(event.values[2], 2));
 						if (rss > RssTreshold * SensorManager.STANDARD_GRAVITY
 								&& activity.RssTime == 0) {
 							if (activity.VveTime == 0) {
 								activity.RssVal = rss;
 								activity.RssTime = date.getTime();
 							}
 							paint.setColor(0xFF0000FF);
 							canvas.drawText("v", newX - 3, mGraphView.mYOffset
 									* 2 + 4 * SensorManager.STANDARD_GRAVITY
 									* mGraphView.mScale[1], paint);
 						}
 						float draw_rss = mGraphView.mYOffset * 2 + rss
 								* mGraphView.mScale[1];
 						paint.setColor(mGraphView.mColors[0]);
 						canvas.drawLine(mLastX, mLastValues[0], newX, draw_rss,
 								paint);
 						mLastValues[0] = draw_rss;
 						// Calculate Vve numeric integral over RSS
 						if (RssStartTime == 0) {
 							RssStartTime = date.getTime();
 							mRssCount++;
 						} else if (date.getTime() - RssStartTime <= VveWindow * 1000
 								&& mRssCount < mRssValues.length) {
 							mRssIndex = mRssCount++;
 						} else {
 							mRssIndex = ++mRssIndex % mRssCount;
 						}
 						mRssValues[mRssIndex] = rss
 								- SensorManager.STANDARD_GRAVITY;
 						float vve = 0;
 						for (int i = 0; i < mRssCount; i++) {
 							vve += mRssValues[i];
 						}
 						vve = (vve * VveWindow) / mRssCount;
 						if (vve < VveTreshold * SensorManager.STANDARD_GRAVITY
 								&& activity.VveTime == 0) {
 							activity.VveVal = vve;
 							activity.VveTime = date.getTime();
 							paint.setColor(0xFF0000FF);
 							canvas.drawText("^", newX - 3, mGraphView.mYOffset
 									/ 2.0f - SensorManager.STANDARD_GRAVITY
									* mGraphView.mScale[0] + 10, paint);
 						}
 						vve = mGraphView.mYOffset / 2.0f + vve
 								* mGraphView.mScale[0];
 						paint.setColor(mGraphView.mColors[1]);
 						canvas.drawLine(mLastX, mLastValues[1], newX, vve,
 								paint);
 						mLastValues[1] = vve;
 						// Increment graph position
 						mLastX = newX;
 					} else if (event.type == Sensor.TYPE_ORIENTATION) {
 						// Calculate orientation
 						float ori = (90 - Math.abs(event.values[1]));
 						float draw_ori = mGraphView.mYOffset * 3 + ori
 								* mGraphView.mScale[2];
 						paint.setColor(mGraphView.mColors[2]);
 						canvas.drawLine(mLastXOri, mLastValues[2], newX,
 								draw_ori, paint);
 						mLastValues[2] = draw_ori;
 						// Calculate Position feature
 						long wait_interval = (activity.RssTime != 0 ? date
 								.getTime() - activity.RssTime
 								: (activity.VveTime != 0 ? date.getTime()
 										- activity.VveTime : 0));
 						if (wait_interval >= OriOffset) {
 							if (OriStartTime == 0)
 								OriStartTime = date.getTime();
 							else if (date.getTime() - OriStartTime < OriWindow) {
 								OriValues[ori_index++] = ori;
 								canvas.drawLine(mLastXOri, mGraphView.mYOffset
 										* 3 + 90 * mGraphView.mScale[2] - 2,
 										newX, mGraphView.mYOffset * 3 + 90
 												* mGraphView.mScale[2] - 2,
 										paint);
 							} else {
 								int count = 0;
 								for (int i = 0; i < ori_index; i++) {
 									if (OriValues[i] > OriTreshold)
 										count++;
 								}
 								if (count / ori_index >= OriConstraint
 										&& activity.hasAcquiredGps) {
 									// A fall has been detected => Time to
 									// take action!!!
 									paint.setColor(0xFF0000FF);
 									canvas.drawText("v", newX - 4,
 											mGraphView.mYOffset * 3 + 90
 													* mGraphView.mScale[2] - 2,
 											paint);
 									activity.fall_detected = true;
 								}
 								// Reset variables for next fall
 								OriStartTime = ori_index = 0;
 							}
 						}
 						mLastXOri = newX;
 					}
 					mGraphView.invalidate();
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 	}
 
 }
