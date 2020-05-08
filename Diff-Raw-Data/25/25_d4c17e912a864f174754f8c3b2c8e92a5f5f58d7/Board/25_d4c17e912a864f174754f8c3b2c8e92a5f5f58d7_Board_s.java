 package com.redcarddev.kickshot.views;
 
 import com.redcarddev.kickshot.R;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class Board extends View {
 	
 	String LOGTAG = "BoardView";
 
 	Bitmap chip = null;
 	
 	/**
 	 * Holds the dice objects
 	 */
 	protected Dice[] dice;
 	Bitmap diceImage = null;
 	
 	/**
 	 * The area that will display the dice and the ball chip
 	 */
 	private Canvas canvas = null;
	
 	/**
 	 * The current x position of the ball
 	 */
 	protected int chipXPos = 0;
 	
 	/**
 	 * The current y position of the ball
 	 */
 	protected int chipYPos = 0;
 	
 	/**
 	 * The initial y position of the ball
 	 */
 	protected int chipInitYPos = 0;
 	
 	/**
 	 * The line the ball should be on
 	 */
 	protected int chipLine = 0;
 
     /**
      * The Score to win the game
      */
     protected int maxScore = 1;
 
 	/**
 	 * The y position of the two dice
 	 */
 	protected int[] diceYPos = {0,0}; //represents the current position
 
 	/**
 	 * The home y position of the two dice
 	 */
 	protected int[] diceHomeYPosition = {0,0}; //set in init
 
 	/**
 	 * The away y position of the two dice
 	 */
 	protected int[] diceAwayYPosition = {50, 250};
 
 	protected Bitmap goalAwayImage = null;
 
 	protected Bitmap goalHomeImage = null;
 
 	protected int[] goalYPos = {0,0};
 	protected int[] goalXPos = {0,0};
 
 	protected int goalsAway = 0;
 	protected int goalsHome = 0;
 
 	protected Paint paint;
 
 	/**
 	 * Class variable for HOME
 	 */
 	public final static int HOME = 1;
 
 	/**
 	 * Class variable for AWAY
 	 */
 	public final static int AWAY = 2;
 
 	int initSet = 0;
 
 	public Board(Context context, AttributeSet attrs) {
 	    super(context, attrs);
 
 	    dice = new Dice[2];
 	    this.dice[0] = new Dice(context);
 	    this.dice[1] = new Dice(context);
 
 	    Resources res = getContext().getResources();
 
 	    this.goalAwayImage = BitmapFactory.decodeResource(res, R.drawable.ballchipaway);
 	    this.goalHomeImage = BitmapFactory.decodeResource(res, R.drawable.ballchiphome);
 
 	}
 
 	public boolean onTouchEvent(MotionEvent event) {
 		return super.onTouchEvent(event);
 	}
 
 	/**
 	 * sets the location of the chip
 	 * @param loc the integer the chipLine is set to
 	 */
 	public void setChipLocation(int loc){
 		this.chipLine = loc;
 		this.ballMove();
 	}
 
 	/**
 	 * Changes the Bitmap displayed for the ball chip
 	 * @param playerTurn Either Board.HOME or Board.AWAY
 	 * @return True if a possible player
 	 */
 	public Boolean ballPosession(int playerTurn) {
 
 		int chipDrawable;
 
 		if (playerTurn == Board.HOME) {//home chip
 			chipDrawable = R.drawable.ballchiphome;
 		} else if (playerTurn == Board.AWAY) {//away chip
 			chipDrawable = R.drawable.ballchipaway;
 		} else {
 			return false;
 		}
 
 		Resources res = getContext().getResources();
 		this.chip = BitmapFactory.decodeResource(res, chipDrawable);
 
 		invalidate();
 		return true;
 	}
 
 	/**
 	 * Changes the face of a specific dice
 	 * @param dice The dice that should be changes (1 or 2)
 	 * @param diceFace The number to display on the dice (1..6)
 	 * @return True if a possible dice index
 	 */
 	public Boolean diceChangeFace(int dice, int diceFace) {
 
 		if (dice < 1 || dice > 2) {
 			return false;
 		}
 
 		this.dice[dice - 1].setDiceFace(diceFace);
 		invalidate();
 
 		return true;
 	}
 
 	/**
 	 * Moves the specified dice to the home position
 	 * @param dice The die to move
 	 * @return True if a possible dice index
 	 */
 	public Boolean dicePositionHome(int dice) {
 
 		if (dice < 1 || dice > 2) {
 			return false;
 		}
 
 		this.diceYPos[dice - 1] = this.diceHomeYPosition[dice - 1];
 		invalidate();
 
 		return true;
 	}
 
 	/**
 	 * Moves the specified dice to the away position
 	 * @param dice The die to move
 	 * @return True if a possible dice index
 	 */
 	public Boolean dicePositionAway(int dice) {
 
 		if (dice < 1 || dice > 2) {
 			return false;
 		}
 
 		this.diceYPos[dice - 1] = this.diceAwayYPosition[dice - 1];
 		invalidate();
 
 		return true;
 	}
 	
 	/**
 	 * Moves the specified ball towards the away goal
 	 * @param steps The number of steps to move the ball
 	 * @return The line number that the ball moved to
 	 */
 	public int ballTowardsAway(int steps) {
 		this.chipLine += steps;
 		return this.ballMove();
 	}
 	
 	/**
 	 * Moves the specified ball towards the home goal
 	 * @param steps The number of steps to move the ball
 	 * @return The line number that the ball moved to
 	 */
 	public int ballTowardsHome(int steps) {
 		this.chipLine -= steps;
 		return this.ballMove();
 	}
 	
 	/**
 	 * Moves the ball to the line specified by chipLine
 	 * @return The line number that the ball moved to
 	 */
 	private int ballMove() {
 		
 		if(this.chipLine <= -11){ //do not allow past the home goal
 			//for some reason this isn't working quite right... sends it too far past the goal line
 			this.chipLine = -11;
 		}
 		else if(this.chipLine >= 11){//do not allow past the away goal
 			this.chipLine = 11;
 		}
 		
 		Log.v(LOGTAG, "Chip Line: " + this.chipLine);
		
		this.chipYPos = this.chipInitYPos - (this.chipLine * 40);
 		Log.v(LOGTAG, "Setting chipYPos: " + this.chipYPos);
 		
 		invalidate();
 		
 		return this.chipLine;
 	}
 	
 	/**
 	 * Prepares the resources for onDraw
 	 */
 	private void init() {
 		
 		//need to make this more dynamic for the smaller devices
 		//
 		this.diceHomeYPosition[0] = this.canvas.getHeight() - 150;
 		this.diceHomeYPosition[1] = this.canvas.getHeight() - 350;
 		
 		ballPosession(1);
 		dicePositionHome(1);
 		dicePositionHome(2);
 		
 		//set goal position away
 		this.goalYPos[Board.AWAY - 1] = 0;
 		this.goalXPos[Board.AWAY - 1] = this.canvas.getWidth() - this.goalAwayImage.getWidth();
 		
 		//set goal position home
 		this.goalYPos[Board.HOME - 1] = this.canvas.getHeight() - ( 4 * this.chip.getHeight() );
 		this.goalXPos[Board.HOME - 1] = this.canvas.getWidth() - this.goalHomeImage.getWidth();
 		
 		
 		this.chipXPos = (this.canvas.getWidth() - this.chip.getWidth()) / 2;
 						
 		this.chipInitYPos = this.chipYPos = (this.canvas.getHeight() - this.chip.getHeight())/2;
 		this.chipLine = 0;
 		
 		this.paint = new Paint(); 
 		paint.setColor(Color.WHITE); 
 		paint.setStyle(Style.FILL); 
 		this.canvas.drawPaint(paint); 
 
 		paint.setColor(Color.RED); 
 		paint.setTextSize(40); 
 		
 		this.initSet = 1;
 		
 	}
 
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		
 		this.canvas = canvas;
 		
 		if (this.initSet == 0) {
 			this.init();
 		}
 		
 		canvas.drawBitmap(this.dice[0].getCurrent(), 0, this.diceYPos[0], null);
 		canvas.drawBitmap(this.dice[1].getCurrent(), 0, this.diceYPos[1], null);
 		
 		canvas.drawBitmap(this.chip, this.chipXPos, this.chipYPos, null);
 		
 		canvas.drawBitmap(this.goalAwayImage, this.goalXPos[1], this.goalYPos[1], null);
 		canvas.drawBitmap(this.goalHomeImage, this.goalXPos[0], this.goalYPos[0], null);
 		
 		
 		canvas.drawText(Integer.toString(this.goalsHome), this.goalXPos[0] + 15, this.goalYPos[0] + 40, this.paint);
 		canvas.drawText(Integer.toString(this.goalsAway), this.goalXPos[1] + 15, this.goalYPos[1] + 40, this.paint);
 		
 	}
 
 	public Boolean goalAddAway() {
 		
 		this.goalsAway++;
 
 		return true;
 	}
 	
 	public Boolean goalAddHome() {
 		
 		this.goalsHome++;
 
 		return true;
 	}
 
     public Integer GameOver(){
         if(this.goalsAway == maxScore){
             return 1;
         }
         if(this.goalsHome == maxScore){
             return 2;
         }
         return 0;
     }
 
 }
