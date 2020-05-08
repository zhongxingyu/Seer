 package com.turlutu;
 
 import android.graphics.Typeface;
 import android.util.Log;
 import android.view.MotionEvent;
 
 import com.android.angle.AngleActivity;
 import com.android.angle.AngleFont;
 import com.android.angle.AngleObject;
 import com.android.angle.AnglePhysicObject;
 import com.android.angle.AngleSound;
 import com.android.angle.AngleSprite;
 import com.android.angle.AngleSegmentCollider;
 import com.android.angle.AngleSpriteLayout;
 import com.android.angle.AngleString;
 import com.android.angle.AngleUI;
 import com.android.angle.AngleVector;
 import com.turlutu.Ball.Color;
 
 public class GameUI extends AngleUI {
 
 	protected AngleSpriteLayout mBallLayout[], mPlateformeLayout, mBackGroundLayout;
 	private MyPhysicsEngine mPhysics;
 	private float WIDTH,HEIGHT;
 	protected Ball mBall;
 	private AngleObject ogDashboard;
 	private AngleString mString;
 	protected int mScore,lastupdate;
 	protected AngleSprite mSpriteLeft, mSpriteRight;
 	protected AngleSprite mPlateformew,mPlateformer,mPlateformev,mPlateformej;
 	private AngleSpriteLayout mBordsLayout[];
 	//protected AngleSound sndJump, sndBonus[], sndBonusDefault;
 	protected AngleSound sndJump, sndBonusDefault;
 	protected AngleSpriteLayout bonusTexture0,bonusTexture1,bonusTexture2,bonusTexture3,bonusTexture4,bonusTexture5;
 	
 	public GameUI(AngleActivity activity)
 	{
 		super(activity);
 		Log.i("GameUI", "GameUI constructor debut");
 		WIDTH = 320f;
 		HEIGHT = 480f;
 		mScore = 0;
 		sndJump=new AngleSound(mActivity,R.raw.jump);
 		// BONUS
 		sndBonusDefault=new AngleSound(mActivity,R.raw.bonus);
 		
 		//sndBonus[0]=new AngleSound(mActivity,R.raw.bonus1);
 		
 		bonusTexture0 = new AngleSpriteLayout(activity.mGLSurfaceView, 16, 16, com.turlutu.R.drawable.bonus, 0, 0, 16, 16);
 		bonusTexture1 = new AngleSpriteLayout(activity.mGLSurfaceView, 16, 16, com.turlutu.R.drawable.bonus, 16, 0, 16, 16);
 		bonusTexture2 = new AngleSpriteLayout(activity.mGLSurfaceView, 16, 16, com.turlutu.R.drawable.bonus, 32, 0, 16, 16);
 		bonusTexture3 = new AngleSpriteLayout(activity.mGLSurfaceView, 16, 16, com.turlutu.R.drawable.bonus, 48, 0, 16, 16);
 		bonusTexture4 = new AngleSpriteLayout(activity.mGLSurfaceView, 16, 16, com.turlutu.R.drawable.bonus, 64, 0, 16, 16);
 		bonusTexture5 = new AngleSpriteLayout(activity.mGLSurfaceView, 16, 16, com.turlutu.R.drawable.bonus, 80, 0, 16, 16);	
 		
 		// PLATEFORME
 		mPlateformeLayout = new AngleSpriteLayout(activity.mGLSurfaceView, 64, 30, com.turlutu.R.drawable.plateforme, 0, 1, 64, 30);
 		mPlateformer = new AngleSprite(mPlateformeLayout);
 		mPlateformeLayout = new AngleSpriteLayout(activity.mGLSurfaceView, 64, 30, com.turlutu.R.drawable.plateforme, 0, 34, 64, 30);
 		mPlateformej = new AngleSprite(mPlateformeLayout);
 		mPlateformeLayout = new AngleSpriteLayout(activity.mGLSurfaceView, 64, 30, com.turlutu.R.drawable.plateforme, 0, 66, 64, 30);
 		mPlateformev = new AngleSprite(mPlateformeLayout);
 		mPlateformeLayout = new AngleSpriteLayout(activity.mGLSurfaceView, 64, 30, com.turlutu.R.drawable.plateforme, 0, 98, 64, 30);
 		mPlateformew = new AngleSprite(mPlateformeLayout);
 
 		
 		mBallLayout = new AngleSpriteLayout[6];
 		mBallLayout[0] = new AngleSpriteLayout(activity.mGLSurfaceView, 42, 64, com.turlutu.R.drawable.persos,0,0,42,64);
		mBallLayout[1] = new AngleSpriteLayout(activity.mGLSurfaceView, 42, 64, com.turlutu.R.drawable.persos,62,0,42,64);
		mBallLayout[2] = new AngleSpriteLayout(activity.mGLSurfaceView, 42, 64, com.turlutu.R.drawable.persos,124,0,42,64);
		mBallLayout[3] = new AngleSpriteLayout(activity.mGLSurfaceView, 42, 64, com.turlutu.R.drawable.persos,186,0,42,64);
		mBallLayout[4] = new AngleSpriteLayout(activity.mGLSurfaceView, 42, 64, com.turlutu.R.drawable.persos,248,0,42,64);
		mBallLayout[5] = new AngleSpriteLayout(activity.mGLSurfaceView, 42, 64, com.turlutu.R.drawable.persos,310,0,42,64);		
 		
 		
 		mBordsLayout = new AngleSpriteLayout[3];
 		mBordsLayout[0] = new AngleSpriteLayout(activity.mGLSurfaceView, 64, 256, com.turlutu.R.drawable.bords,0,0,64,256);
 		mBordsLayout[1] = new AngleSpriteLayout(activity.mGLSurfaceView, 64, 256, com.turlutu.R.drawable.bords,64,0,64,256);
 		mBordsLayout[2] = new AngleSpriteLayout(activity.mGLSurfaceView, 64, 256, com.turlutu.R.drawable.bords,128,0,64,256);
 		// TODO voir quelle image convient le mieu au background
 		mBackGroundLayout =new AngleSpriteLayout(activity.mGLSurfaceView,320,480,com.turlutu.R.drawable.fond);
 		
 		// on ajoute le background en premier à MyDemo pour qu'il soit dessiné en premier
 		Background mBackGround = new Background(mBackGroundLayout);
 		addObject(mBackGround);
 		mSpriteLeft = new AngleSprite(0,240,mBordsLayout[0]);
 		mSpriteRight = new AngleSprite(320,240,mBordsLayout[1]);
 		addObject(mSpriteLeft);
 		addObject(mSpriteRight);
 		
 		
 		
 		// on ajoute la balle au moteur en premier pour qu'il la dessine en dernier, voir la fonction draw surchargé de MyPhysicEngine
 		mPhysics=new MyPhysicsEngine(20,WIDTH,HEIGHT,activity.mGLSurfaceView, this);
 		mPhysics.mViscosity = 1f; // Air viscosity
 		mPhysics.mGravity = new AngleVector(0,10f);
 		addObject(mPhysics);
 
 		// le score
 		ogDashboard=addObject(new AngleObject());
 		AngleFont fntCafe25 = new AngleFont(mActivity.mGLSurfaceView, 25, Typeface.createFromAsset(activity.getAssets(),"cafe.ttf"), 222, 0, 0, 30, 200, 255, 255);
 		//AngleFont fntBazaronite=new AngleFont(mActivity.mGLSurfaceView, 18, Typeface.createFromAsset(mActivity.getAssets(),"bazaronite.ttf"), 222, 0, 2, 255, 100, 255, 255);
 		mString = (AngleString)ogDashboard.addObject(new AngleString(fntCafe25,"0",50,20,AngleString.aCenter));
 		
 
 		mBall = new Ball (mBallLayout,32,80,1,sndJump,this);
 		mPhysics.addObject(mBall);
 
 		//init();
 		Log.i("GameUI", "GameUI constructor fin");
 	}
 	
 	@Override
 	public void onActivate()
 	{
 		Log.i("GameUI", "GameUI onActivate debut");
 		mScore=0;
 		mString.set("0");
 		init();
 		super.onActivate();
 		Log.i("GameUI", "GameUI onActivate fin");
 	}
 
 	@Override
 	public void onDeactivate()
 	{
 		Log.i("GameUI", "GameUI onDeactivate debut");
 		for (int i=1; i<20; i++)
 		{
 			if (mPhysics.childAt(i) instanceof AnglePhysicObject)
 			{
 				mPhysics.removeObject(i);
 			}
 		}
 		super.onDeactivate();
 		Log.i("GameUI", "GameUI onDeactivate fin");
 	}
 	/*
 	@Override
 	public void onPause()
 	{
       mSensorManager.unregisterListener(mListener); 
       super.onPause();
 	}
 
 
 	@Override
 	public void onResume()
 	{
       mSensorManager.registerListener(mListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST); 		
 		super.onResume();
 	}
 	*/
 	private void init()
 	{
 
 		mBall.mPosition.set(50,300);
 		mBall.mVelocity.mY = -600;
 		
 		// ajoute une plateforme en bas qui prend toute la place pour le debut
 		AnglePhysicObject mWall = new AnglePhysicObject(1, 0);
 		mWall.mPosition.set(0, HEIGHT-15);
 		mWall.addSegmentCollider(new AngleSegmentCollider(0, 0, WIDTH, 0));
 		mWall.mBounce = 1f;
 		mPhysics.addObject(mWall); // Down wall
 		
 		
 
 		// add barre
 		Plateforme Plateforme = new Plateforme(mPlateformew);
 		Plateforme.mPosition.set(140,420);
 		mPhysics.addObject(Plateforme);
 		
 		Plateforme = new Plateforme(mPlateformew);
 		Plateforme.mPosition.set(50,350);
 		mPhysics.addObject(Plateforme);
 		
 		Plateforme = new Plateforme(mPlateformew);
 		Plateforme.mPosition.set(160,300);
 		mPhysics.addObject(Plateforme);
 		
 		Plateforme = new Plateforme(mPlateformew);
 		Plateforme.mPosition.set(200,250);
 		mPhysics.addObject(Plateforme);
 		
 		Plateforme = new Plateforme(mPlateformew);
 		Plateforme.mPosition.set(130,200);
 		mPhysics.addObject(Plateforme);
 		
 		Plateforme = new Plateforme(mPlateformew);
 		Plateforme.mPosition.set(300,100);
 		mPhysics.addObject(Plateforme);
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event)
 	{
 		mBall.mVelocity.mX = (event.getX()-WIDTH/2)*2;
 		
 		return true;
 	}
 	
 	public void setGravity(float x, float y)
 	{
 		mPhysics.mGravity.set(x,y);
 	}
 	
 	public void upScore(int value)
 	{
 		mScore += value;
 		lastupdate++;
 		if(lastupdate>7)
 		{
 			mString.set(String.valueOf(mScore));
 			lastupdate=0;
 		}
 	}
 	
 	public void backToMenu()
 	{
 		((MainActivity) mActivity).setUI(((MainActivity) mActivity).mMenu);
 	}
 	
 	@Override
 	public void step(float secondsElapsed)
 	{
 		if (secondsElapsed > 0.08)
 			secondsElapsed = (float) 0.04;
 		
 		super.step(secondsElapsed);
 	}
 	
 	public void setSpriteRight(Color newColor)
 	{
 		if (newColor == Color.ROUGE)
 			mSpriteRight.setLayout(mBordsLayout[0]);
 		else if (newColor == Color.VERT)
 			mSpriteRight.setLayout(mBordsLayout[2]);
 		else
 			mSpriteRight.setLayout(mBordsLayout[1]);
 	}
 	
 	public void setSpriteLeft(Color newColor)
 	{
 		if (newColor == Color.ROUGE)
 			mSpriteLeft.setLayout(mBordsLayout[0]);
 		else if (newColor == Color.VERT)
 			mSpriteLeft.setLayout(mBordsLayout[2]);
 		else
 			mSpriteLeft.setLayout(mBordsLayout[1]);
 	}
 	
 }
