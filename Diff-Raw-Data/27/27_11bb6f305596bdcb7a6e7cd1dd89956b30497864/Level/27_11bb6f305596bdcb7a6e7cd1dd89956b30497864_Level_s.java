 package ch.gibb.project.activity;
 
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Display;
 import android.view.Menu;
 import android.widget.RelativeLayout;
 import ch.gibb.project.R;
 import ch.gibb.project.controller.Check;
 import ch.gibb.project.elements.Ball;
 import ch.gibb.project.elements.Hole;
 import ch.gibb.project.elements.Maze;
 import ch.gibb.project.elements.Point;
 import ch.gibb.project.elements.Wall;
 
 public class Level extends Activity implements SensorEventListener {
 	private SensorManager sensorManager;
 	// Bitmap ball;
 	private RelativeLayout layout;
 	private Maze mazeElement;
 	private Ball ballElement;
 	private Wall wallElement;
 	private Hole holeElement;
 	private Point pointElement;
 
 	private BackView backView;
 	private Check check;
 	private float x, y, accelX, accelY;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		initObjects();
 	}
 
 	@Override
 	protected void initObjects() {
		// Returns the display-value
 		Display display = getWindowManager().getDefaultDisplay();
 		android.graphics.Point size = new android.graphics.Point();
 		display.getSize(size);
 
		// Creates a new View
 		mazeElement = new Maze(this, size.x, size.y);
 		ballElement = new Ball(this, size.x, size.y);
 		wallElement = new Wall(this, size.x, size.y);
 		holeElement = new Hole(this, size.x, size.y);
 		pointElement = new Point(this, size.x, size.y);
 
 		backView = new BackView(this);
 
 		check = new Check(this);
 		layout = new RelativeLayout(this);
		layout.addView(backView);
		layout.addView(mazeElement);
		layout.addView(ballElement);
 		setContentView(layout);
 
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
 
 	public Hole getHoleElement() {
 		return holeElement;
 	}
 
 	public void setHoleElement(Hole holeElement) {
 		this.holeElement = holeElement;
 	}
 
 	public Point getPointElement() {
 		return pointElement;
 	}
 
 	public void setPointElement(Point pointElement) {
 		this.pointElement = pointElement;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 		// TODO Auto-generated method stub
 		// Testing Yanu
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		accelX = event.values[0];
 		accelY = event.values[1];
 		// boolean ballInHole = view.ballInHole();
 		updateBall(accelX, accelY);
 	}
 
 	private void updateBall(float accelX, float accelY) {
 		// TODO: Add Boucing
 		// TODO: Add Acceleration (Speed)
 		// TODO: Implement WallTouch
 		if (check.containsBallX(accelX)) {
 			ballElement.getCoordinates().x = ballElement.getCoordinates().x
 					- (accelX * 2f);
 		} else {
 			if (check.touchOnLeft()) {
 				ballElement.getCoordinates().x = mazeElement.getPlayGround().left;
 			} else {
 				ballElement.getCoordinates().x = (mazeElement.getPlayGround().right - (ballElement
 						.getRadius() * 2));
 			}
 		}
 		if (check.containsBallY(accelY)) {
 			ballElement.getCoordinates().y = ballElement.getCoordinates().y
 					+ (accelY * 2f);
 		} else {
 			if (check.touchOnTop()) {
 				ballElement.getCoordinates().y = mazeElement.getPlayGround().top;
 			} else {
 				ballElement.getCoordinates().y = (mazeElement.getPlayGround().bottom - (ballElement
 						.getRadius() * 2));
 			}
 		}
 		if (check.ballInHole()) {
 			// TODO: Replace with nicer code?
 			layout.removeView(mazeElement);
 			layout.addView(mazeElement);
 		}
 
 		check.checkStarTouch();
 
 	}
 
 }
