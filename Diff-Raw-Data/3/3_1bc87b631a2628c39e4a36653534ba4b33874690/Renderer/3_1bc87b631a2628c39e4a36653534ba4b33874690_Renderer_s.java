 package co.sidhant.soccerfield;
 
 import java.io.ObjectInputStream;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import rajawali.BaseObject3D;
 import rajawali.SerializedObject3D;
 import rajawali.lights.ALight;
 import rajawali.lights.DirectionalLight;
 import rajawali.materials.AMaterial;
 import rajawali.materials.DiffuseMaterial;
 import rajawali.materials.SimpleMaterial;
 import rajawali.materials.TextureInfo;
 import rajawali.materials.TextureManager.TextureType;
 import rajawali.primitives.Plane;
 import rajawali.primitives.Sphere;
 import rajawali.renderer.RajawaliRenderer;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.view.MotionEvent;
 import android.view.VelocityTracker;
 import co.sidhant.soccerfield.R;
 
 public class Renderer extends RajawaliRenderer implements SensorEventListener{
 	private BaseObject3D field;
 	private final SensorManager mSensorManager;
 	private final Sensor mAccelerometer;
 	private float accY;
 	private float accX;
 	private Sphere ball;
 	private float xSpeed;
 	private float ySpeed;
 	private VelocityTracker velocity;
 	private boolean landscape;
 	private boolean scoring;
 	private boolean scoreChanged;
 	private int homeScore;
 	private int awayScore;
 	private UserPrefs mUserPrefs;
 	private Canvas scoreC;
 	private String scoreStr;
 	private Bitmap scoreBitmap;
 	private Plane scorePlane;
 	private TextureInfo scoreTexture;
 	private int maxScore;
 	private BaseObject3D entireView;
 	private boolean leftLandscape;
 	public boolean defaultLandscape;
 	private boolean rightPortrait;
 	private boolean ballReset;
 	
 	public Renderer(Context context) {
 		super(context);
 		mSensorManager = (SensorManager) context.getSystemService("sensor");
         mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
         mUserPrefs = UserPrefs.getInstance(context.getApplicationContext());
 	}
 
 	public void initScene() {
 		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
 		ALight light = new DirectionalLight();
 		light.setPower(5);
 		light.setPosition(3, 0, 0);
 		mCamera.setPosition(3, -0.19f, 0);
 		mCamera.setLookAt(0, 0, 0);
 		
 		homeScore = 0;
 		awayScore = 0;
 		
 		scoring = mUserPrefs.getScoring();
 		maxScore = mUserPrefs.getMaxScore();
 		if(scoring)
 		{
 			ballReset = mUserPrefs.getBallReset();
 		}
 		else
 		{
 			ballReset = false;
 		}
 		
 		scoreChanged = true;
 		
 		TextureInfo fieldTex = mTextureManager.addEtc1Texture(mContext.getResources().openRawResource(R.drawable.sf_pkm), null, TextureType.DIFFUSE);
 		TextureInfo fieldTexAlpha = mTextureManager.addEtc1Texture(mContext.getResources().openRawResource(R.drawable.sf_pkm_alpha), null, TextureType.ALPHA);
 		SimpleMaterial fieldMat = new SimpleMaterial(AMaterial.ALPHA_MASKING);
 		
 		SerializedObject3D fieldSer = null;
 		try {
 			ObjectInputStream fieldOIS;
 			fieldOIS = new ObjectInputStream(mContext.getResources()
 					.openRawResource(R.raw.fieldser));
 			fieldSer = (SerializedObject3D) fieldOIS
 					.readObject();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		field = new BaseObject3D(fieldSer);
 		field.setMaterial(fieldMat);
 		field.addTexture(fieldTex);
 		
 		BaseObject3D goal;
 		
 		ball = new Sphere(0.05f, 16, 16);
 		DiffuseMaterial ballMat = new DiffuseMaterial(); 
 		ball.setMaterial(ballMat);
 		TextureInfo ballTex = mTextureManager.addEtc1Texture(mContext.getResources().openRawResource(R.drawable.ball_pkm), null, TextureType.DIFFUSE);
 		ball.addTexture(ballTex); 
 		ball.addLight(light);
 		ball.setX(0.2f);
 		
 		
 		scorePlane = new Plane(0.4f, 0.2f, 1, 1);
 		SimpleMaterial scoreMat = new SimpleMaterial(AMaterial.ALPHA_MASKING);
 		scorePlane.setMaterial(scoreMat);
 		scorePlane.setPosition(0.1f, 0.1f, 0);
 		generateScoreTexture();
 		scoreTexture = mTextureManager.addTexture(scoreBitmap);
 		scorePlane.addTexture(scoreTexture);
 		scorePlane.setRotY(270);
 		
 		SerializedObject3D goalSer = null;
 		try {
 			ObjectInputStream goalOIS;
 			goalOIS = new ObjectInputStream(mContext.getResources()
 					.openRawResource(R.raw.goalser));
 			goalSer = (SerializedObject3D) goalOIS
 					.readObject();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		goal = new BaseObject3D(goalSer);
 		goal.setMaterial(fieldMat);
 		goal.addTexture(fieldTex);
 		goal.addTexture(fieldTexAlpha);
 		field.addChild(goal);
 		
 		entireView = new BaseObject3D();
 		// Use this object to rotate the entire screen if the surface is in landscape
 		entireView.addChild(ball);
 		entireView.addChild(field);
 		entireView.addChild(scorePlane);
 		addChild(entireView);
 	}
 
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		super.onSurfaceCreated(gl, config);
 		xSpeed = 0;
 		ySpeed = 0;
 	}
 	
 	public void onSurfaceChanged(GL10 gl, int width, int height) 
 	{
 		if(width > height)
 		{
 			landscape = true;
 		}
 		else
 		{
 			landscape = false;
 		}
 		// Wallpaper is designed to display perfectly on a 1280 x 720 screen, so scale objects accordingly.
 		float scaleY = (float) height / 1280.0f;
 		float scaleZ = (float) width / 720.0f;
 		if(landscape)
 		{
 			rightPortrait = true;
 			scaleY = (float) width / 1280.0f;
 			scaleY += 0.2f;
 			scaleZ =  (float) height / 720.0f;
 			scaleZ += 0.07f;
 			if(accX > 0)
 			{
 				leftLandscape = true;
 				entireView.setRotX(-90);
 			}
 			else
 			{
 				leftLandscape = false;
 				entireView.setRotX(90);
 			}
 			mCamera.setPosition(2, 0.06f, 0);
 			mCamera.setLookAt(0, 0.06f, 0);
 		}
 		else
 		{
 			if(accY < 0)
 			{
 				rightPortrait = true;
 			}
 			else
 			{
 				rightPortrait = false;
 			}
 			entireView.setRotX(0);
 			mCamera.setPosition(3, -0.19f, 0);
 			mCamera.setLookAt(0, 0, 0);
 		}
		field.setScaleY(scaleY);
		field.setScaleZ(scaleZ);
		ball.setScale(scaleY);
 		super.onSurfaceChanged(gl, width, height);
 	}
 	
 	public void onDrawFrame(GL10 glUnused) {
 		super.onDrawFrame(glUnused);
 		
 		//spin the ball
 		float speed = Math.abs(xSpeed) + Math.abs(ySpeed);
 		speed *= 700;
 		float result = (float) Math.toDegrees(Math.atan2(-ySpeed, xSpeed));
 		if(result < 0)
 		{
 			result += 360; 
 		}
 		
 		ball.setRotY(ball.getRotY() + speed);
 		ball.setRotX(result);
 		
 		float yBound;
 		float zBound;
 		
 		if(landscape)
 		{
 			yBound = (float) mViewportWidth / 1280.0f;
 			yBound += 0.2f;
 			zBound = (float) mViewportHeight / 720.0f;
 			zBound += 0.07f;
 		}
 		else
 		{
 			yBound = (float) mViewportHeight / 1280.0f;
 			zBound = (float) mViewportWidth / 720.0f;
 		}
 		
 		yBound *= 1.1f;
 		float goalBound = 0.2f * zBound;
 		zBound *= 0.65f;
 		
 		
 		//move the ball
 		
 		if(ball.getZ() < zBound && ball.getZ() > -zBound)
 		{
 			if(xSpeed < 0.65f && xSpeed > -0.65f)
 			{
 				xSpeed += accX * (0.005 / 60);
 			}
 			else if(xSpeed < 0)
 			{
 				xSpeed = -0.6f;
 			}
 			else if(xSpeed > 0)
 			{
 				xSpeed = 0.6f;
 			}
 			
 			ball.setZ(ball.getZ() + xSpeed);
 		}
 		else
 		{
 			if(ball.getZ() < 0)
 			{
 				xSpeed = 0.005f;
 			}
 			else
 				xSpeed = -0.005f;
 			
 			ball.setZ(ball.getZ() + xSpeed);
 		}
 		
 		if(ball.getY() < yBound && ball.getY() > -yBound)
 		{
 			if(ySpeed < 0.65f && ySpeed > -0.65f)
 			{
 				ySpeed += accY * (0.004 / 60);
 			}
 			else if(ySpeed < 0)
 			{
 				ySpeed = -0.6f;
 			}
 			else if(ySpeed > 0)
 			{
 				ySpeed = 0.6f;
 			}
 			
 			ball.setY(ball.getY() - ySpeed);
 		}
 		else
 		{
 			if(ball.getY() < 0)
 			{
 				if(ball.getZ() < goalBound && ball.getZ() > -goalBound && ySpeed > 0)
 				{
 					awayScore++;
 					scoreChanged = true;
 					if(ballReset)
 					{
 						ball.setY(0);
 						ball.setZ(0);
 					}
 				}
 				ySpeed = -0.01f;
 				if(ballReset && scoreChanged)
 				{
 					ySpeed = 0;
 					xSpeed = 0;
 				}
 			}
 			else
 			{
 				if(ball.getZ() < goalBound && ball.getZ() > -goalBound && ySpeed < 0)
 				{
 					homeScore++;
 					scoreChanged = true;
 					if(ballReset)
 					{
 						ball.setY(0);
 						ball.setZ(0);
 					}
 				}
 				ySpeed = 0.01f;
 				if(ballReset && scoreChanged)
 				{
 					ySpeed = 0;
 					xSpeed = 0;
 				}
 			}
 			
 			ball.setY(ball.getY() - ySpeed);
 		}
 		
 
 		if(scoreChanged)
 		{
 			scoreChanged = false;
 			if(homeScore == maxScore || awayScore == maxScore)
 			{
 				homeScore = 0;
 				awayScore = 0;
 				xSpeed = 0;
 				ySpeed = 0;
 			}
 			generateScoreTexture();
 			mTextureManager.updateTexture(scoreTexture, scoreBitmap);
 		}
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 		
 	}
 	
 	@Override
 	public void onTouchEvent(MotionEvent event)
 	{
 		int action = event.getAction();
 		if(velocity == null)
 		{
 			velocity = VelocityTracker.obtain();
 		}
 
 		float xVelocity;
 		float yVelocity;
 		if(action == MotionEvent.ACTION_MOVE)
 		{
 			velocity.addMovement(event);
 			velocity.computeCurrentVelocity(1);
 			if(!landscape)
 			{
 				xVelocity = velocity.getXVelocity();
 				yVelocity = velocity.getYVelocity();
 			}
 			else
 			{
 				if(!leftLandscape)
 				{
 					xVelocity = velocity.getYVelocity();
 					yVelocity = -velocity.getXVelocity();
 				}
 				else
 				{
 					xVelocity = -velocity.getYVelocity();
 					yVelocity = velocity.getXVelocity();
 				}
 			}
 		}
 		else if(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_POINTER_UP)
 		{
 			velocity.computeCurrentVelocity(1);
 			if(!landscape)
 			{
 				xVelocity = velocity.getXVelocity();
 				yVelocity = velocity.getYVelocity();
 			}
 			else
 			{
 				if(!leftLandscape)
 				{
 					xVelocity = velocity.getYVelocity();
 					yVelocity = -velocity.getXVelocity();
 				}
 				else
 				{
 					xVelocity = -velocity.getYVelocity();
 					yVelocity = velocity.getXVelocity();
 				}
 			}
 			velocity.recycle();
 			velocity = null;
 		}
 		else
 		{
 			velocity.computeCurrentVelocity(1);
 			if(!landscape)
 			{
 				xVelocity = velocity.getXVelocity();
 				yVelocity = velocity.getYVelocity();
 			}
 			else
 			{
 				if(!leftLandscape)
 				{
 					xVelocity = velocity.getYVelocity();
 					yVelocity = -velocity.getXVelocity();
 				}
 				else
 				{
 					xVelocity = -velocity.getYVelocity();
 					yVelocity = velocity.getXVelocity();
 				}
 			}
 		}
 		
 		yVelocity /= 400;
 		xVelocity /= 400;
 		
 		xSpeed = -xVelocity;
 		ySpeed = yVelocity;
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
             if(!(event.values[1] > 10 || event.values[1] < -10 || event.values[0] < -10 || event.values[0] > 10))
             {
             	if(!defaultLandscape)
             	{
             		accY =  Math.round(event.values[1]);
             		accX =  Math.round(event.values[0]);
             	}
             	else
             	{
             		if(rightPortrait)
             		{
             			accY =  -Math.round(event.values[0]);
             		}
             		else
             		{
             			accY =  Math.round(event.values[0]);
             		}
             		accX =  Math.round(event.values[1]);
             	}
             }
 		}
 	}
 	
 	@Override
 	public void onVisibilityChanged(boolean visible)
 	{
 		super.onVisibilityChanged(visible);
 		if((scoring != mUserPrefs.getScoring() || maxScore != mUserPrefs.getMaxScore()))
 		{
 			homeScore = 0;
 			awayScore = 0;
 		}
 		scoring = mUserPrefs.getScoring();
 		if(scoring)
 		{
 			ballReset = mUserPrefs.getBallReset();
 		}
 		else
 		{
 			ballReset = false;
 		}
 		scoreChanged = true;
 		maxScore = mUserPrefs.getMaxScore();
 		
 		if(!visible)
 		{
 			mSensorManager.unregisterListener(this);
 		}
 		else
 			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
 	}
 	
 	private void generateScoreTexture()
 	{
 		scoreBitmap = Bitmap.createBitmap(128, 64, Bitmap.Config.ARGB_8888);
 		scoreC = new Canvas(scoreBitmap);
 		Paint scorePaint = new Paint();
 		scorePaint.setColor(Color.WHITE);
 		scorePaint.setTextSize(40);
 		scorePaint.setAntiAlias(true);
 		scorePaint.setAlpha(220);
 		scoreC.drawColor(Color.TRANSPARENT);
 		float offset = 10;
 		if(scoring)
 		{
 			String homeString = Integer.toString(homeScore);
 			if(homeScore < 10)
 			{
 				homeString += " ";
 				offset = 18;
 			}
 			
 			String awayString = Integer.toString(awayScore);
 			if(awayScore < 10)
 			{
 				awayString = " " + awayString;
 			}
 			scoreStr = (homeString + "-" + awayString);
 		}
 		else
 		{
 			scoreStr = " ";
 		}
 		scoreC.drawText(scoreStr, offset, 50, scorePaint);
 	}
 }
