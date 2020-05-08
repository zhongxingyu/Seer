 package com.turlutu;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Looper;
 import android.util.Log;
 import android.view.MotionEvent;
 
 import com.android.angle.AngleActivity;
 import com.android.angle.AngleObject;
 import com.android.angle.AnglePhysicObject;
 import com.android.angle.AngleSegmentCollider;
 import com.android.angle.AngleSound;
 import com.android.angle.AngleSprite;
 import com.android.angle.AngleSpriteLayout;
 import com.android.angle.AngleString;
 import com.android.angle.AngleUI;
 import com.android.angle.AngleVector;
 import com.turlutu.Ball.Color;
 import com.turlutu.Bonus.TypeBonus;
 
 public class GameUI extends AngleUI {
 
 	protected AngleSpriteLayout mBallLayout[], mPlateformeLayout, mBackGroundLayout, mBonusLayout[];
 	protected Ball mBall;
 	protected int mScore;
 	protected BorderSprite mSpriteLeft, mSpriteRight;
 	protected AngleSprite mPlateformew,mPlateformer,mPlateformev,mPlateformej;
 	protected AngleSound sndJump, sndBonus[];
 	protected TypeBonus mTypeBonus = TypeBonus.NONE;
 	protected float WIDTH,HEIGHT;
 	protected MyPhysicsEngine mPhysics;
 
 	private AngleSpriteLayout mBordsLayout[];
 	private AngleString mString, mString2;
 	private AngleObject ogDashboard;
 	private LifePlateforme mLife;
 	private float mTimeEllapsedBonus, mTimeActionBonus;
 	
 	public GameUI(AngleActivity activity, Background mBackGround)
 	{
 		super(activity);
 		Log.i("GameUI", "GameUI constructor debut");
 		if(mBackGround != null)
 			addObject(mBackGround);
 		WIDTH = 320f;
 		HEIGHT = 480f;
		mScore = 0;
 		sndJump=new AngleSound(mActivity,R.raw.rebond);
 		
 		// BONUS
 		sndBonus  = new AngleSound[7];
 		sndBonus[0] = new AngleSound(mActivity,R.raw.bonus); // On s'en fout (no bonus)
 		sndBonus[1] = new AngleSound(mActivity,R.raw.earnmoney); // ADDSCORE
 		sndBonus[2] = new AngleSound(mActivity,R.raw.bonus2); //MORE JUMP
 		sndBonus[3] = new AngleSound(mActivity,R.raw.bonus1); // LESS JUMP
 		sndBonus[4] = new AngleSound(mActivity,R.raw.changephysic); // PHYSIC
 		sndBonus[5] = new AngleSound(mActivity,R.raw.nochangecolor); // DISABLE CHANGE COLOR
 		sndBonus[6] = new AngleSound(mActivity,R.raw.jump); // ALL PLATEFORME
 		
 		
 		mBonusLayout = new AngleSpriteLayout[8];
 		for(int i=0;i<8;i++) {
 			mBonusLayout[i] = new AngleSpriteLayout(activity.mGLSurfaceView, 32, 32, com.turlutu.R.drawable.bonus, i*32, 0, 32, 32);
 		}
 		
 		// PLATEFORMES
 		mPlateformeLayout = new AngleSpriteLayout(activity.mGLSurfaceView, 64, 30, com.turlutu.R.drawable.plateformes, 0, 1, 64, 30);
 		mPlateformer = new AngleSprite(mPlateformeLayout);
 		mPlateformeLayout = new AngleSpriteLayout(activity.mGLSurfaceView, 64, 30, com.turlutu.R.drawable.plateformes, 0, 34, 64, 30);
 		mPlateformej = new AngleSprite(mPlateformeLayout);
 		mPlateformeLayout = new AngleSpriteLayout(activity.mGLSurfaceView, 64, 30, com.turlutu.R.drawable.plateformes, 0, 66, 64, 30);
 		mPlateformev = new AngleSprite(mPlateformeLayout);
 		mPlateformeLayout = new AngleSpriteLayout(activity.mGLSurfaceView, 64, 30, com.turlutu.R.drawable.plateformes, 0, 98, 64, 30);
 		mPlateformew = new AngleSprite(mPlateformeLayout);
 
 		// BALL
 		mBallLayout = new AngleSpriteLayout[8];
 		for (int i=0;i<8;++i)
 			mBallLayout[i] = new AngleSpriteLayout(activity.mGLSurfaceView, 64, 64, com.turlutu.R.drawable.persos,64*i,0,64,64);
 		
 		
 		mBordsLayout = new AngleSpriteLayout[3];
 		mBordsLayout[0] = new AngleSpriteLayout(activity.mGLSurfaceView, 64, 256, com.turlutu.R.drawable.bords,0,0,64,256);
 		mBordsLayout[1] = new AngleSpriteLayout(activity.mGLSurfaceView, 64, 256, com.turlutu.R.drawable.bords,64,0,64,256);
 		mBordsLayout[2] = new AngleSpriteLayout(activity.mGLSurfaceView, 64, 256, com.turlutu.R.drawable.bords,128,0,64,256);
 		
 		mSpriteLeft = new BorderSprite(mActivity, 0,240,mBordsLayout[0]);
 		mSpriteRight = new BorderSprite(mActivity, 320,240,mBordsLayout[1]);
 		addObject(mSpriteLeft);
 		addObject(mSpriteRight);
 		
 		
 		
 		mPhysics=new MyPhysicsEngine(20,WIDTH,HEIGHT,activity.mGLSurfaceView, this);
 		mPhysics.mViscosity = 1f; // Air viscosity
 		mPhysics.mGravity = new AngleVector(0,10f);
 		addObject(mPhysics);
 
 		// le score
 		ogDashboard=addObject(new AngleObject());
 		mString = (AngleString)ogDashboard.addObject(new AngleString(((MainActivity)mActivity).fntGlobal,"0",50,20,AngleString.aCenter));
 		mString2 = (AngleString)ogDashboard.addObject(new AngleString(((MainActivity)mActivity).fntGlobal,"0",50,20,AngleString.aCenter));
 		mString2.mLength = 0;
 
 		mBall = new Ball ((MainActivity)mActivity,mBallLayout,32,80,1,sndJump);
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
 	
 	public void backToMenu()
 	{
 		((MainActivity) mActivity).setUI(((MainActivity) mActivity).mScores);
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
 	
 	private void init()
 	{
 		mPhysics.init();
 		mTimeEllapsedBonus = 0;
 		mTypeBonus = TypeBonus.NONE;
 
 		mBall.mPosition.set(50,300);
 		mBall.jump();
 		
		// ajoute une plateforme en bas qui prend toute la place pour le debut
 		mLife = new LifePlateforme();
 		mPhysics.addObject(mLife); // Down wall
 		
 		mLife.setLife(3);
 		
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
 		Plateforme.mPosition.set(120,100);
 		mPhysics.addObject(Plateforme);
 		
 		Plateforme = new Plateforme(mPlateformew);
 		Plateforme.mPosition.set(200,50);
 		mPhysics.addObject(Plateforme);
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event)
 	{
 		// TODO RELEASE Supprimer (ou commenter) tout ce qui n'est pas entre PAUSE et FIN PAUSE dans la fonction pour les versions mobiles
 		  // /* 
 		  float eY = event.getY();
 
 		if(eY < 100) { 
 			 // */ // PAUSE
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 			{
 				new Thread() 
 				{
 					@Override 
 					public void run() 
 					{
 						Looper.prepare();
 						AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
 						builder.setMessage("  --- Pause ! ---  ")
 						       .setCancelable(false)
 						       .setNeutralButton("Retour au jeu", new DialogInterface.OnClickListener() {
 						           public void onClick(DialogInterface dialog, int id) {
 						        	   ((MainActivity)mActivity).onResume();
 						           }
 						       });
 						AlertDialog alert = builder.create();
 						alert.show();
 						Looper.loop();
 					}
 				}.start();
 				((MainActivity)mActivity).onPause();
 			}
 			// FIN PAUSE 
 			// /*
 		} else {
 			mBall.mVelocity.mX = (event.getX()-WIDTH/2)*((MainActivity)mActivity).mOptions.mSensibility/25;
 			if (mTypeBonus == TypeBonus.CHANGEPHYSICS)
 				mBall.mVelocity.mX = -mBall.mVelocity.mX;
 		} // */
 		return true;
 	}
 	
 	public void setGravity(float x, float y)
 	{
 		mPhysics.mGravity.set(x,y);
 	}
 	
 	public void upScore(int value)
 	{
 		mScore += value;
 		if (mString.mLength > 0)
 		{
 			mString.setAndHide(String.valueOf(mScore));
 			mString2.mLength =mString2.getLength();
 		}
 		else
 		{
 			mString2.setAndHide(String.valueOf(mScore));
 			mString.mLength = mString.getLength();
 		}
 	}
 
 	
 	@Override
 	public void step(float secondsElapsed)
 	{
 		if (secondsElapsed > 0.08)
 			secondsElapsed = (float) 0.04;
 		
 		if (mTypeBonus != TypeBonus.NONE)
 		{
 			if (mTimeEllapsedBonus > mTimeActionBonus)
 			{
 				Log.i("GameUI", "GameUI step fin bonus : "+mTypeBonus);
 				mTypeBonus = TypeBonus.NONE;
 			}
 			else
 			mTimeEllapsedBonus += secondsElapsed;
 		}
 		
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
 	
 	public void setBonus(TypeBonus t, float s)
 	{
 		Log.i("GameUI", "GameUI setBonus debut");
 		Log.i("GameUI", "bonus : "+t);
 		mTimeEllapsedBonus = 0;
 		mTypeBonus = t;
 		mTimeActionBonus = s;
 		if (t == TypeBonus.ADDSCORE)
 			upScore(600);
 		else
 			upScore(200);
 		Log.i("GameUI", "GameUI setBonus fin");
 	}
 }
