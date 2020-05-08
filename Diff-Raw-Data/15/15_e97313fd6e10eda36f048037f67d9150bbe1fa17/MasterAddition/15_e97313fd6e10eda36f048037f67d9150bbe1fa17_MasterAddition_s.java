 /*
  * math.createEquation();
     equation = math.getEquation();
  */
 
 package com.example.pokemonfractions;
 
 import helpers.MasterLocation;
 import helpers.TutorialScreenCreator;
 
 import java.util.ArrayList;
 
 import mathClasses.MasterMath;
 import android.os.Bundle;
 import android.os.Handler;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.animation.OvershootInterpolator;
 import android.widget.FrameLayout;
 
 
 public class MasterAddition extends Activity {
 	private static final String DEBUG_TAG = "Addition";
     private Matrix animateStart;
     private OvershootInterpolator animateInterpolator;
     private long startTime;
     private long endTime;
     private float totalAnimDx;
     private float totalAnimDy;
     private MasterMath math;
     private int ans=0,question=0,userAns=0,correct=0;
     private float cxDex=0,cyDex=0;
     private String equation="";
     private boolean initializing = true;
     private ArrayList<MasterLocation> pokeBall = new ArrayList<MasterLocation>();
     private boolean[] setBall = {true,true,true,true,true,true};
     private ArrayList<MasterLocation> pokemon = new ArrayList<MasterLocation>();
     private ArrayList<Bitmap> legendary = new ArrayList<Bitmap>();
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_master_addition);
 	    FrameLayout frame = (FrameLayout) findViewById(R.id.graphics_holder);
 	    PlayAreaView image = new PlayAreaView(this);
 	    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 	    frame.addView(image);
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_master_addition, menu);
         return true;
     }
     
     public void onResume()
     {
     	super.onResume();
     }
 
     
     private class PlayAreaView extends View {
 
         private GestureDetector gestures;
         private Matrix translate;
         private Matrix animateStart;
         private OvershootInterpolator animateInterpolator;
         private long startTime;
         private long endTime;
         private float totalAnimDx;
         private float totalAnimDy;
         private Bitmap pokeDex, sixth1, sixth2, sixth3, sixth4,sixth5, sixth6,egg,mew,mew2,art,mol,zap,lug;
         private Paint p;
         private float[] dexLoc = { 1, 0, cxDex, 0, 1, cyDex, 0, 0, 1 };
 
         
         public PlayAreaView(Context context) {
             super(context);
             translate = new Matrix();
             translate.setValues(dexLoc);
             gestures = new GestureDetector(MasterAddition.this,
                     new GestureListener(this));
             math = new MasterMath(6);
             pokeDex = BitmapFactory.decodeResource(getResources(), R.drawable.pokedex1);
             ans = math.getAnswerNum();
             equation = math.getEquation();
             p=new Paint();
             sixth1 = BitmapFactory.decodeResource(getResources(), R.drawable.sixth1);
             sixth2 = BitmapFactory.decodeResource(getResources(), R.drawable.sixth2);
             sixth3 = BitmapFactory.decodeResource(getResources(), R.drawable.sixth3);
             sixth4 = BitmapFactory.decodeResource(getResources(), R.drawable.sixth4);
             sixth5 = BitmapFactory.decodeResource(getResources(), R.drawable.sixth5);
             sixth6 = BitmapFactory.decodeResource(getResources(), R.drawable.sixth6);
             egg = BitmapFactory.decodeResource(getResources(), R.drawable.egg_insert);
             setPieceLocations();
         	setPokeLocations();
         	createLegendary();
         }
         public void onMove(float dx, float dy) {     
         	Log.v(DEBUG_TAG, "moving");
             translate.postTranslate(dx, dy);
             invalidate();
         }
 
         public void onResetLocation() {
             translate.setValues(dexLoc);
             invalidate();
         }
         
         public void onResetLocation(boolean flag) {
             if(flag)
             {
             	translate.reset();
             	invalidate();
             }
         }
         
         public void wiggle()
         {       	 
  			final float shift = 25;
  			onMove(-shift,0);
  			Log.v(DEBUG_TAG, "in wiggle");
 			final Handler backHandler = new Handler();
 			backHandler.postDelayed(new Runnable() {
              public void run() {
              	onResetLocation();
              	onMove(-shift*2,0);
    	          backHandler.postDelayed(new Runnable() {
  	                public void run() {
  	                	onResetLocation();
  	                	onMove(-shift,0);
  	                	Log.v(DEBUG_TAG, ":("); 
  	                	}
  	            	}, 350);  	       
              }             
          }, 350);
 			onResetLocation();
         }
         
         //creates the legendary boxes for the answers
         public void createLegendary()
         {
         	mew = BitmapFactory.decodeResource(getResources(), R.drawable.mew_insert);
         	legendary.add(mew);
         	mew2 = BitmapFactory.decodeResource(getResources(), R.drawable.mewtwo_insert);
         	legendary.add(mew2);
         	art = BitmapFactory.decodeResource(getResources(), R.drawable.articuno_insert);
         	legendary.add(art);
         	mol = BitmapFactory.decodeResource(getResources(), R.drawable.moltres_insert);
         	legendary.add(mol);
         	zap = BitmapFactory.decodeResource(getResources(), R.drawable.zapdos_insert);
         	legendary.add(zap);
         	lug = BitmapFactory.decodeResource(getResources(), R.drawable.lugia_insert);
         	legendary.add(lug);
         	
         }
         
         //sets the locations for the answers
         public void setPokeLocations()
         {
         	MasterLocation m = new MasterLocation(egg,1000,5);
        	 pokemon.add(m);
       	 m = new MasterLocation(egg,1000,120);
        	 pokemon.add(m);
       	 m = new MasterLocation(egg,1000,220);
        	 pokemon.add(m);
        	 m = new MasterLocation(egg,1000,320);
        	 pokemon.add(m);
       	 m = new MasterLocation(egg,1000,420);
        	 pokemon.add(m);
       	 m = new MasterLocation(egg,1000,520);
        	 pokemon.add(m);
         }
         
         //sets the locations for the fraction pieces
         public void setPieceLocations()
         {
         	MasterLocation m = new MasterLocation(sixth1,524-sixth1.getWidth(),168);
         	pokeBall.add(m);
         	m = new MasterLocation(sixth2,526,168);
         	pokeBall.add(m);
         	m = new MasterLocation(sixth3,528,225);
         	pokeBall.add(m);
         	m = new MasterLocation(sixth4, 524,281);
         	pokeBall.add(m);
         	m = new MasterLocation(sixth5, 524-sixth5.getWidth(),281);
         	pokeBall.add(m);
         	m = new MasterLocation(sixth6, 520-sixth6.getWidth(),225);
         	pokeBall.add(m);
         }
         
         //draws the answer counter
         public void drawPokemon(Canvas canvas)
         {
         	 Bitmap answerBg = BitmapFactory.decodeResource(getResources(), R.drawable.poke_menu);
         	 canvas.drawBitmap(answerBg, canvas.getWidth()-answerBg.getWidth(),0, p);
         	 for(MasterLocation m : pokemon)
         		 m.draw(canvas, p);
         	 
         }
         
         //draws the pieces currently being shown
         public void drawPieces(Canvas canvas)
         {
         	Bitmap full = BitmapFactory.decodeResource( getResources(), R.drawable.sixths_lines);
         	
         	for(int i = 0; i < 6; i++)
         	{
         		if(setBall[i])
         		{
         			pokeBall.get(i).draw(canvas,p);
         		}
         	}
         	canvas.drawBitmap(full, canvas.getWidth()/2 - full.getWidth(),canvas.getHeight() - full.getHeight()*2, p);
 
         }
         
         public void drawEquation(Canvas canvas)
         {
         	p.setColor(Color.BLACK);
         	p.setTextSize(30);
         	canvas.drawText(equation, (30 +pokeDex.getWidth())/2 - equation.length()*7, (canvas.getHeight()-pokeDex.getHeight()/2), p);
         }
         
         //call this when the user clicks in the pokedex
         public void nextQuestion()
         {
         	if(math.isCorrect(userAns))
         	{
         		pokemon.get(correct).setIcon(legendary.get(correct));
         		correct++;
 
         	}
         	if(correct == 6)
         	{
         		moveOn();
         	}
         	question++;
         	math.createEquation();
         	equation = math.getEquation();
         		
         }
 
         @Override
         protected void onDraw(Canvas canvas) {
             // Log.v(DEBUG_TAG, "onDraw");
 
         	
         	cxDex = 25;
         	cyDex = canvas.getHeight() - pokeDex.getHeight();
         	//cxS1 = 50;
         	//cyS1 = 50;
         	if(initializing)
         	{
         		setPieceLocations();
         		dexLoc[2] = cxDex;
         		dexLoc[5] = cyDex;
         		start( canvas );        		
         		onResetLocation();
         	}
         	
             p.setColor(Color.rgb(174,208,198));
             canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), p);
         	canvas.drawBitmap(pokeDex, translate, null);        	
         	drawPieces(canvas);        	
         	drawEquation(canvas);
         	drawPokemon(canvas);
         	if(correct == 6)
         	{
         		p.setColor(Color.BLACK);
         		p.setTextSize(30);
         		canvas.drawText("You have finished the game", 100, 100, p);
         	}
             //Log.d(DEBUG_TAG, "Matrix: " + translate.toShortString());
             //Log.d(DEBUG_TAG, "Canvas: " + m.toShortString());
         }
         
         public void start( Canvas Canvas ) {
         	Bitmap ball = BitmapFactory.decodeResource( getResources(), R.drawable.pokeball );
         	Bitmap ballPiece = BitmapFactory.decodeResource( getResources(), R.drawable.sixth6 );
         	TutorialScreenCreator t = new TutorialScreenCreator(p,Canvas);
         	t.createAdditionScreen(ballPiece, ball);	
        }
         
         public void moveOn()
         {
         	final Intent intent = new Intent(MasterAddition.this.getBaseContext(), MasterScore.class);
         	intent.putExtra("results", " "+question+" ");
 			final Handler h1 = new Handler();
 			  	h1.postDelayed(new Runnable() 
 				{
 					public void run() 
 					{
 						Log.d(DEBUG_TAG, "moving on");
 						//write a message to canvas
 						recreate();
 						startActivity(intent);						
 					}
 				}, 4000);
 			
         }   
         
 
         @Override
         public boolean onTouchEvent(MotionEvent event) {
             return gestures.onTouchEvent(event);
         }
     
     private class GestureListener implements GestureDetector.OnGestureListener,GestureDetector.OnDoubleTapListener {
     	PlayAreaView view;
     	
     	public GestureListener(PlayAreaView view)
     	{
     		this.view = view;
     	}
     	
     	
     	public boolean onDown(MotionEvent e) {
     	    Log.v(DEBUG_TAG, "onDown"); 
     	    initializing = false;
     	    //if the user is on the first question and they have filled in the correct amount of the master ball
     	    //the pokedex wiggles to show them that to answer they click in the pokedex
     	    if(question == 0 )
     	    {
     	    	Log.v(DEBUG_TAG, "start wiggle"); 
     	    	view.wiggle();
     	    	initializing = false;
     	    } 
     	    return true;
     	}
 
     	
     	public boolean onDoubleTap(MotionEvent e) {
     	    Log.v(DEBUG_TAG, "onDoubleTap");
 //    	    view.onResetLocation();
     	    return true;
     	}
 
 		public boolean onDoubleTapEvent(MotionEvent e) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public boolean onSingleTapConfirmed(MotionEvent e) {
 			
 			return false;
 		}
 
 
 		public boolean onFling(MotionEvent e1, MotionEvent e2,final float velocityX, final float velocityY) {
 		    Log.v(DEBUG_TAG, "onFling");
 		    return true;
 		}
 
 
 		public void onLongPress(MotionEvent e) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public boolean onScroll(MotionEvent e1, MotionEvent e2,
 		    float distanceX, float distanceY) {
 		    Log.v(DEBUG_TAG, "onScroll");
 		   
 		    return true;
 		}
 
 		public void onShowPress(MotionEvent e) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public boolean onSingleTapUp(MotionEvent e) {
 
 			return false;
 		}
 		
 		
 
     }
 
 }
 }
