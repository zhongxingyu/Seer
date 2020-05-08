 package com.mamlambo.artut;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Locale;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.Paint.Style;
 import android.graphics.Point;
 import android.hardware.Camera;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.location.Location;
 import android.text.Layout.Alignment;
 import android.text.StaticLayout;
 import android.text.TextPaint;
 import android.util.Log;
 import android.view.View;
 
 import com.mamlambo.globals.Global;
 
 public class OverlayView extends View implements SensorEventListener {
 
 	public static final String DEBUG_TAG = "OverlayView Log";
 
 	private final Context context;
 
 	// Mount Washington, NH: 44.27179, -71.3039, 6288 ft (highest peak
 	private final static Location mountWashington = new Location("manual");
 	static {
 		mountWashington.setLatitude(90.00d);
 		mountWashington.setLongitude(-90.000d);
 		mountWashington.setAltitude(1916.5d);
 	}
 
 	String accelData = "Accelerometer Data";
 	String compassData = "Compass Data";
 	String gyroData = "Gyro Data";
 
 	private SensorManager sensors = null;
 
 	private float[] lastAccelerometer;
 	private float[] lastCompass;
 
 	private float verticalFOV;
 	private float horizontalFOV;
 	private float slopeBaseOfferHeight;
 
 	private boolean isAccelAvailable = true;
 	private boolean isCompassAvailable = true;
 	private boolean isGyroAvailable = true;
 	private Sensor accelSensor;
 	private Sensor compassSensor;
 	private Sensor gyroSensor;
 
 	private TextPaint contentPaint;
 	private Paint targetPaint;
 
 	private Location slopeCurMyLocation;
 
 	private static final int filterQueueLen = 10;
 	private static int filterQueueIdx = 0;
 	private static float filterQueueX[] = new float[filterQueueLen];
 	private static float filterQueueY[] = new float[filterQueueLen];
 	private static float filterQueueZ[] = new float[filterQueueLen];
 
 	public OverlayView(Context context) {
 		super(context);
 		this.context = context;
 
 		sensors = (SensorManager) context
 				.getSystemService(Context.SENSOR_SERVICE);
 		accelSensor = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 		compassSensor = sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
 		gyroSensor = sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
 
 		startSensors();
 		// get some camera parameters
 		Camera camera = Camera.open();
 		Camera.Parameters params = camera.getParameters();
 		verticalFOV = params.getVerticalViewAngle();
 		horizontalFOV = params.getHorizontalViewAngle();
 		camera.release();
 		// paint for text
 		contentPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG
 				| Paint.LINEAR_TEXT_FLAG);
 		contentPaint.setStyle(Style.FILL);
 		contentPaint.setTextAlign(Align.LEFT);
 		contentPaint.setTextSize(20);
 		contentPaint.setSubpixelText(true);
 		contentPaint.setColor(Color.RED);
 		contentPaint.setShadowLayer(1, 0, 0, Color.BLACK);
 		// paint for target
 		targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		targetPaint.setColor(Color.WHITE);
 		targetPaint.setStrokeWidth(3);
 		// init filter queue
 		filterQueueIdx = 0;
 		initSlopeValue();
 	}
 
 	private void initSlopeValue() {
 		slopeBaseOfferHeight = Global.slopeBaseOfferHeight1;
 		float slopeBaseHeightOfMesure = Global.slopeBaseHeightOfMesureType1;
 
 		if (verticalFOV == 42.5f && horizontalFOV == 54.8f) {
 			slopeBaseHeightOfMesure = Global.slopeBaseHeightOfMesureType1;
 			slopeBaseOfferHeight = Global.slopeBaseOfferHeight1;
 		} else if (verticalFOV == 46.3f && horizontalFOV == 59.6f) {
 			slopeBaseHeightOfMesure = Global.slopeBaseHeightOfMesureType2;
 			slopeBaseOfferHeight = Global.slopeBaseOfferHeight2;
 		} else if (verticalFOV == 60.0f && horizontalFOV == 60.0f) {
 			slopeBaseHeightOfMesure = Global.slopeBaseHeightOfMesureType3;
 			slopeBaseOfferHeight = Global.slopeBaseOfferHeight3;
 		} else {
 			slopeBaseHeightOfMesure = Global.slopeBaseHeightOfMesureType1;
 			slopeBaseOfferHeight = Global.slopeBaseOfferHeight1;
 		}
 
 		// Init Devive spec.
 		if (Global.deviceType == 0) {
 			slopeBaseHeightOfMesure = Global.slopeBaseHeightOfMesureType1;
 			slopeBaseOfferHeight = Global.slopeBaseOfferHeight1;
 		} else if (Global.deviceType == 1) {
 			slopeBaseHeightOfMesure = Global.slopeBaseHeightOfMesureType2;
 			slopeBaseOfferHeight = Global.slopeBaseOfferHeight2;
 		} else {
 			slopeBaseHeightOfMesure = Global.slopeBaseHeightOfMesureType1;
 			slopeBaseOfferHeight = Global.slopeBaseOfferHeight1;
 		}
 
 		for (int i = 0; i < Global.slopeMeasureMeter.length; i++)
 			Global.slopeHeightOfMesureType[i] = slopeBaseHeightOfMesure
 					* Global.slopeBaseMeasureMeter
 					/ Global.slopeMeasureMeter[i];
 	}
 
 	private void startSensors() {
 		isAccelAvailable = sensors.registerListener(this, accelSensor,
 				SensorManager.SENSOR_DELAY_NORMAL);
 		isCompassAvailable = sensors.registerListener(this, compassSensor,
 				SensorManager.SENSOR_DELAY_NORMAL);
 		isGyroAvailable = sensors.registerListener(this, gyroSensor,
 				SensorManager.SENSOR_DELAY_NORMAL);
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		// Log.d(DEBUG_TAG, "onDraw");
 		super.onDraw(canvas);
 
 		drawInfo(canvas);
 	}
 
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 		Log.d(DEBUG_TAG, "onAccuracyChanged");
 	}
 
 	public void onSensorChanged(SensorEvent event) {
 		// Log.d(DEBUG_TAG, "onSensorChanged");
 
 		StringBuilder msg = new StringBuilder(event.sensor.getName())
 				.append(" ");
 		for (float value : event.values) {
 			msg.append("[").append(String.format("%.3f", value)).append("]");
 		}
 
 		switch (event.sensor.getType()) {
 		case Sensor.TYPE_ACCELEROMETER:
 			lastAccelerometer = event.values.clone();
 			accelData = msg.toString();
 			break;
 		case Sensor.TYPE_GYROSCOPE:
 			gyroData = msg.toString();
 			break;
 		case Sensor.TYPE_MAGNETIC_FIELD:
 			lastCompass = event.values.clone();
 			compassData = msg.toString();
 			break;
 		}
 
 		this.invalidate();
 	}
 
 	// this is not an override
 	public void onPause() {
 		sensors.unregisterListener(this);
 	}
 
 	// this is not an override
 	public void onResume() {
 		startSensors();
 	}
 
 	// Generate Bitmap File
 	void generateBitmapWithSlopeInfo(String filename, String infofilename) {
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inSampleSize = 2;
 		options.inPurgeable = true;
 		Bitmap bitmap = BitmapFactory.decodeFile(filename, options);
 
 		Canvas canvas = new Canvas(bitmap);
 
 		drawInfo(canvas);
 
 		FileOutputStream out;
 		try {
 			out = new FileOutputStream(new File(infofilename));
 			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
 			out.flush();
 			out.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		Log.d(DEBUG_TAG, "Done bitmap compress.");
 	}
 
 	private void drawInfo(Canvas canvas) {
 		float curBearingToMW = 0.0f;
 		StringBuilder text = new StringBuilder();
 		if (Global.isDebug) {
 			text.append(accelData).append("\n");
 			text.append(compassData).append("\n");
 			text.append(gyroData).append("\n");
 		}
 
 		if (slopeCurMyLocation != null) {
 			text.append("Current Position").append("\n");
 			text.append(
 					String.format("Latitude  : %.5f",
 							slopeCurMyLocation.getLatitude())).append("\n");
 			text.append(
 					String.format("Longitude : %.5f",
 							slopeCurMyLocation.getLongitude())).append("\n");
 			text.append(
 					String.format("Altitude  : %.2f",
 							slopeCurMyLocation.getAltitude())).append("\n");
 		} else {
 			text.append("Current Position").append("\n");
 			text.append(String.format("Latitude  : unknown")).append("\n");
 			text.append(String.format("Longitude : unknown")).append("\n");
 			text.append(String.format("Altitude  : unknown")).append("\n");
 		}
 
 		// compute rotation matrix
 		float rotationX = 0.0f;
 		float rotationY = 0.0f;
 		float rotationZ = 0.0f;
 		float rotation[] = new float[9];
 		float identity[] = new float[9];
 		if (lastAccelerometer != null && lastCompass != null) {
 			boolean gotRotation = SensorManager.getRotationMatrix(rotation,
 					identity, lastAccelerometer, lastCompass);
 			if (gotRotation) {
 				float cameraRotation[] = new float[9];
 				// remap such that the camera is pointing straight down the Y
 				// axis
 				SensorManager.remapCoordinateSystem(rotation,
 						SensorManager.AXIS_X, SensorManager.AXIS_Z,
 						cameraRotation);
 
 				// orientation vector
 				float orientation[] = new float[3];
 				SensorManager.getOrientation(cameraRotation, orientation);
 				rotationX = orientation[0];
 				rotationY = orientation[1];
				rotationZ = orientation[2];
 
 				// apply filter
 				filterQueueX[filterQueueIdx % filterQueueLen] = orientation[0];
 				filterQueueY[filterQueueIdx % filterQueueLen] = orientation[1];
 				filterQueueZ[filterQueueIdx % filterQueueLen] = orientation[2];
 				filterQueueIdx++;
 				if (filterQueueIdx > filterQueueLen) {
 					rotationX = 0;
 					rotationY = 0;
 					rotationZ = 0;
 
 					for (int i = 0; i < filterQueueLen; i++) {
 						rotationX += filterQueueX[i];
 						rotationY += filterQueueY[i];
 						rotationZ += filterQueueZ[i];
 					}
 
 					rotationX = rotationX / filterQueueLen;
 					rotationY = rotationY / filterQueueLen;
 					rotationZ = rotationZ / filterQueueLen;
 				}
 
 				// if(Global.isDebug)
 				{
 					text.append(
 							String.format("Orientation %.3f, %.3f, %.3f",
 									Math.toDegrees(rotationX),
 									Math.toDegrees(rotationY),
 									Math.toDegrees(rotationZ))).append("\n");
 				}
 
 				{
 					// Translate, but normalize for the FOV of the camera --
 					// basically, pixels per degree, times degrees == pixels
 					float dx = (float) ((this.getWidth() / horizontalFOV) * (Math
 							.toDegrees(rotationX) - curBearingToMW));
 					float dy = (float) ((this.getHeight() / verticalFOV) * Math
 							.toDegrees(rotationY));
 					float dHeightPerDegree = this.getHeight() / verticalFOV;
 					float dRotateDegree = Global.slopeHeightOfMesureType[Global.slopeMeasureType]
 							/ dHeightPerDegree;
 					float dUnitDegree = (float) Math
 							.toDegrees(Math
 									.atan(1.0f / Global.slopeMeasureMeter[Global.slopeMeasureType]));
//					dy = (float) (dy * dRotateDegree / dUnitDegree);
 					// Draw Slope Line, Horizontal Line
 					canvas.save();
 					if (Global.isDebug) {
 						text.append(
 								String.format(
 										"[dx, dy]:[%.2f, %.2f], [hFOV, vFOV]:[%.2f, %.2f]",
 										dx, dy, horizontalFOV, verticalFOV))
 								.append("\n");
 					}
 
 					float crossCenterX = this.getWidth() / 2.0f;
 					float crossCenterY = this.getHeight() / 2.0f;
 
 					// // use roll for screen rotation
 					// canvas.rotate((float)(0.0f- Math.toDegrees(rotationZ)));
 					canvas.rotate((float) (90 - Math.toDegrees(rotationZ)),
 							(int) crossCenterX, (int) crossCenterY);
 
 					// wait to translate the dx so the horizon doesn't get
 					// pushed off
 					Log.i("Slope", "Translate: " + Float.toString(dy));
 					Log.i("Slope", "Rotation: " + Float.toString(rotationZ));
					canvas.translate(0.0f, 0.0f - dy);
 
 					// // now translate the dx
 					// canvas.translate(0.0f-dx, 0.0f);
 
 					// Draw Slope Lines
 					Point targetPosition = new Point();
 					Point startPosition = new Point();
 					Paint paintLine = new Paint();
 
 					// Draw Horizontal Line (0 Line)
 					paintLine.setARGB(128, 128, 128, 128);
 					paintLine.setStrokeWidth(5);
 					paintLine.setAntiAlias(true);
 
 					startPosition.x = -this.getWidth();
 					targetPosition.x = this.getWidth() * 2;
 					startPosition.y = (int) (this.getHeight() / 2);
 							//+ (int) slopeBaseOfferHeight;
 					targetPosition.y = startPosition.y;
 					canvas.drawLine(targetPosition.x, targetPosition.y,
 							startPosition.x, startPosition.y, paintLine);
 
 					if (Global.isDebug) {
 						text.append(
 								String.format("[%4d, %4d] - [%4d, %d]",
 										startPosition.x, startPosition.y,
 										targetPosition.x, targetPosition.y))
 								.append("\n");
 						text.append(
 								String.format("[%4d, %4d] - [%4d, %d]",
 										canvas.getWidth(), canvas.getHeight(),
 										this.getWidth(), this.getHeight()))
 								.append("\n");
 					}
 
 					// Draw Measure Slope Line
 					
 					paintLine.setARGB(128, 255, 0, 0);
 					paintLine.setStrokeWidth(5);
 					paintLine.setAntiAlias(true);
 
 					startPosition.x = -this.getWidth();
 					targetPosition.x = this.getWidth() * 2;
 
 					startPosition.y = (int) (startPosition.y + Global.slopeHeightOfMesureType[Global.slopeMeasureType]);
 					targetPosition.y = startPosition.y;
 
 					canvas.drawLine(targetPosition.x, targetPosition.y,
 							startPosition.x, startPosition.y, paintLine);
 
 					canvas.restore();
 				}
 				{
 					// draw cross hair
 					canvas.save();
 					// draw cross hair
 					float crossWidth = this.getWidth() / 4.0f;
 					float crossHeight = this.getHeight() / 8.0f;
 					float crossCenterX = this.getWidth() / 2.0f;
 					float crossCenterY = this.getHeight() / 2.0f
 							+ slopeBaseOfferHeight;
 					float crossStartX = (this.getWidth() - crossWidth) / 2.0f;
 					float crossStartY = this.getHeight() / 2.0f
 							+ slopeBaseOfferHeight;
 
 					canvas.drawLine(crossStartX, crossStartY, crossStartX
 							+ crossWidth, crossStartY, targetPaint);
 					canvas.drawLine(crossCenterX, crossCenterY - crossHeight,
 							crossCenterX, crossCenterY + crossHeight,
 							targetPaint);
 					canvas.drawLine(crossCenterX - crossWidth / 4.0f,
 							crossCenterY - crossHeight / 2.0f, crossCenterX
 									- crossWidth / 4.0f, crossCenterY
 									+ crossHeight / 2.0f, targetPaint);
 					canvas.drawLine(crossCenterX + crossWidth / 4.0f,
 							crossCenterY - crossHeight / 2.0f, crossCenterX
 									+ crossWidth / 4.0f, crossCenterY
 									+ crossHeight / 2.0f, targetPaint);
 
 					canvas.restore();
 				}
 			}
 		}
 
 		canvas.save();
 
 		// Output Slope Value
 		Paint paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
 		Paint paintLine1 = new Paint(Paint.ANTI_ALIAS_FLAG);
 		String strSlope = "";
 		String setSlope = "";
 		float slopeMeasureMeter = Global.slopeMeasureMeter[Global.slopeMeasureType];
 		float slopeLineMeter = slopeMeasureMeter;
 
 		// slopeLineMeter = (float)(slopeMeasureMeter * Math.tan(1.0f /
 		// slopeMeasureMeter) / Math.tan(Math.abs(rotationY)));
 		slopeLineMeter = (float) (1.0f / Math.tan(Math.abs(rotationY)));
 
 		if (slopeLineMeter > Global.slopeMeasureMeter[Global.slopeMeasureType] - 0.07
 				&& slopeLineMeter < Global.slopeMeasureMeter[Global.slopeMeasureType] + 0.07)
 			slopeLineMeter = Global.slopeMeasureMeter[Global.slopeMeasureType];
 
 		if (slopeLineMeter > 250.0f)
 			slopeLineMeter = 250.0f;
 
 		if (rotationY > 0)
 			slopeLineMeter = -slopeLineMeter;
 
 		Global.slopeLineMeter = slopeLineMeter;
 		// slopeLineMeter = Math.abs(slopeLineMeter);
 		strSlope = String.format(Locale.getDefault(),
 				"Screen Slope = %.2f : 1", slopeLineMeter);
 		setSlope = String.format(Locale.getDefault(),
 				"Desired Slope = %.2f : 1", slopeMeasureMeter);
 
 		paintLine.setStyle(Paint.Style.FILL);
 		paintLine.setStrokeWidth(2);
 		paintLine.setColor(Color.RED);
 		paintLine.setShadowLayer(3, 0, 0, Color.BLACK);
 		paintLine.setTextSize(40);
 		paintLine.setTextAlign(Paint.Align.CENTER);
 
 		paintLine1.setStyle(Paint.Style.FILL);
 		paintLine1.setStrokeWidth(2);
 		paintLine1.setColor(Color.RED);
 		paintLine1.setShadowLayer(3, 0, 0, Color.BLACK);
 		paintLine1.setTextSize(20);
 		paintLine1.setTextAlign(Paint.Align.CENTER);
 
 		int textBoundWidth = (int) Math.ceil(paintLine.measureText(strSlope));
 		int textBoundHeight = 80;
 		int textStartX = (canvas.getWidth() / 2);
 		int textStartY = textBoundHeight - 00;
 
 		canvas.drawText(strSlope, textStartX, textStartY, paintLine);
 
 		canvas.drawText(setSlope, textStartX, textBoundHeight - 40, paintLine1);
 
 		canvas.restore();
 
 		// Output APP VERSION.
 		if (Global.isShowVersionText == true) {
 			canvas.save();
 
 			Paint paintText = new Paint();
 			String strDeviceType = String.format("%s",
 					Global.deviceTypeName[Global.deviceType]);
 
 			paintText.setStyle(Paint.Style.FILL);
 			paintText.setStrokeWidth(1);
 			paintText.setColor(Color.WHITE);
 			paintText.setTextSize(30);
 
 			textBoundWidth = (int) Math.ceil(paintLine
 					.measureText(strDeviceType));
 			textBoundHeight = 40;
 			textStartX = (canvas.getWidth() - textBoundWidth) / 2;
 			textStartY = canvas.getHeight() - textBoundHeight - 10;
 
 			canvas.drawText(strDeviceType, textStartX, textStartY, paintText);
 
 			canvas.restore();
 		}
 
 		// if(Global.isDebug) {
 		// text.append(strSlope);
 		// }
 
 		canvas.save();
 		canvas.translate(15.0f, 15.0f);
 		StaticLayout textBox = new StaticLayout(text.toString(), contentPaint,
 				480, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
 		textBox.draw(canvas);
 		canvas.restore();
 	}
 
 	public void setLocation(Location mCurrentLocation) {
 		slopeCurMyLocation = mCurrentLocation;
 	}
 
 }
