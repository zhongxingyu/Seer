 package ch.gibb.project.activity;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.widget.RelativeLayout;
 import ch.gibb.project.R;
 import ch.gibb.project.controller.ActionHandler;
 import ch.gibb.project.elements.Ball;
 import ch.gibb.project.elements.Maze;
 import ch.gibb.project.elements.Point;
 import ch.gibb.project.elements.Text;
 import ch.gibb.project.elements.Wall;
 import ch.gibb.project.enums.StageEnum;
 import ch.gibb.project.util.MessageUtil;
 
 public class Level extends Activity implements SensorEventListener {
 	public SensorManager sensorManager;
 	private RelativeLayout layout;
 	private Maze mazeElement;
 	private Ball ballElement;
 	private Wall wallElement;
 	private Text textElement;
 	private Point pointElement;
 	private BackView backView;
 	private ActionHandler actionHandler;
 	private float accelX, accelY;
 	private int stageNumber;
 
 	public static Bitmap backgroundImage;
 	public static Bitmap wallImage;
 	public static Bitmap finishImage;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.stageNumber = 1;
 		Display display = getWindowManager().getDefaultDisplay();
 		android.graphics.Point size = new android.graphics.Point();
 		display.getSize(size);
 		setStaticBitmaps(size.x, size.y);
 		initObjects(stageNumber);
 
 		actionHandler = new ActionHandler(this);
 
 		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 		if (sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() == 0) {
 			Log.d("Message", "No accelerometer installed");
 		} else {
 			Sensor accelerometer = sensorManager.getSensorList(
 					Sensor.TYPE_ACCELEROMETER).get(0);
 			if (!sensorManager.registerListener(this, accelerometer,
 					SensorManager.SENSOR_DELAY_FASTEST)) {
 				Log.d("Message", "Couldn't register sensor listener");
 			}
 		}
 	}
 
	protected void initObjects(int stageNumber) {
		Display display = getWindowManager().getDefaultDisplay();
		android.graphics.Point size = new android.graphics.Point();
		display.getSize(size);
		initViews(size.x, size.y);
		addelementsToView();
	}

 	private void initViews(int x, int y) {
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inSampleSize = 8;
 		mazeElement = new Maze(this, x, y);
 		ballElement = new Ball(this, x, y);
 		wallElement = new Wall(this, x, y);
 		textElement = new Text(this, x, y);
 		pointElement = new Point(this, x, y);
 		backView = new BackView(this);
 	}
 
 	private void addelementsToView() {
 		layout = new RelativeLayout(this);
 		layout.addView(backView);
 		layout.addView(mazeElement);
 		layout.addView(textElement);
 		layout.addView(pointElement);
 		layout.addView(wallElement);
 		layout.addView(ballElement);
 		setContentView(layout);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		accelX = event.values[0];
 		accelY = event.values[1];
 		updateBall(accelX, accelY);
 	}
 
 	private void updateBall(float accelX, float accelY) {
 		// TODO: Acceleration
 		if (!actionHandler.checkIfFinished()) {
 			actionHandler.moveAndCheckX(accelX);
 			actionHandler.moveAndCheckY(accelY);
 			actionHandler.checkStarTouch();
 		} else {
 			initObjects(++stageNumber);
 			changeStage();
 		}
 
 		if (actionHandler.ballInHole()) {
 			// // TODO: Replace with nicer code?
 			// layout.removeView(mazeElement);
 			// layout.addView(mazeElement);
			// sensorManager.unregisterListener(this);
 			if (stageNumber == 1) {
 				initObjects(stageNumber);
 			} else {
 				initObjects(--stageNumber);
 			}
 			changeStage();
 		}
 
 	}
 
 	private void changeStage() {
 		// TODO zinggpa animation for stage switching
 	}
 
 	public StageEnum getStage() {
 		switch (stageNumber) {
 		case 1:
 			return StageEnum.STAGE_1;
 		case 2:
 			return StageEnum.STAGE_2;
 		case 3:
 			return StageEnum.STAGE_3;
 		case 4:
 			return StageEnum.STAGE_4;
 		case 5:
 			return StageEnum.STAGE_5;
 		default:
 			return null;
 		}
 	}
 
 	public void setStaticBitmaps(int width, int height) {
 		BitmapFactory.Options opts = new BitmapFactory.Options();
 		opts.inDither = false;
 		opts.inPurgeable = true;
 		opts.inInputShareable = true;
 		opts.inTempStorage = new byte[32 * 1024];
 		backgroundImage = BitmapFactory.decodeResource(getResources(),
 				R.drawable.wood, opts);
 		backgroundImage = Bitmap.createScaledBitmap(backgroundImage,
 				width - 40, height - 40, true);
 		wallImage = BitmapFactory.decodeResource(getResources(),
 				R.drawable.wall, opts);
 		wallImage = Bitmap.createScaledBitmap(wallImage, width, height, true);
 		finishImage = BitmapFactory.decodeResource(getResources(),
 				R.drawable.finish, opts);
 		finishImage = Bitmap.createScaledBitmap(finishImage, 73, 73, true);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 			MessageUtil.getInstance().createAlertMessage(Level.this,
 					MessageUtil.DIALOG_LEVELEXIT);
 			return true;
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	public SensorManager getSensorManager() {
 		return sensorManager;
 	}
 
 	public void setSensorManager(SensorManager sensorManager) {
 		this.sensorManager = sensorManager;
 	}
 
 	public Maze getMazeElement() {
 		return mazeElement;
 	}
 
 	public void setMazeElement(Maze mazeElement) {
 		this.mazeElement = mazeElement;
 	}
 
 	public Ball getBallElement() {
 		return ballElement;
 	}
 
 	public void setBallElement(Ball ballElement) {
 		this.ballElement = ballElement;
 	}
 
 	public Wall getWallElement() {
 		return wallElement;
 	}
 
 	public void setWallElement(Wall wallElement) {
 		this.wallElement = wallElement;
 	}
 
 	public Point getPointElement() {
 		return pointElement;
 	}
 
 	public void setPointElement(Point pointElement) {
 		this.pointElement = pointElement;
 	}
 
 	public Text getTextElement() {
 		return textElement;
 	}
 
 	public int getStageNumber() {
 		return stageNumber;
 	}
 
 	public void setStageNumber(int stageNumber) {
 		this.stageNumber = stageNumber;
 	}
 
 }
