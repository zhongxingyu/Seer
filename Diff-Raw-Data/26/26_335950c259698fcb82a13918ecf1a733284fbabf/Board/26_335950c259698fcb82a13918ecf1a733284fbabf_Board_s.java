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
 import android.widget.Toast;
 
 import java.util.HashMap;
 import java.util.Map;
 
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
      * The height of the board at initialization
      */
 	private int boardHeight = 0;
 
     /**
      * the playable area of the ball
      */
     private int fieldHeight = 0;
 
     /**
      * The location of the goal the away team is defending
      */
     private int awayGoalLine = 0;
 
     /**
      * The location of the goal the home team is defending
      */
     private int homeGoalLine = 0;
 
     /**
      * The amount the chip moves in pixels
      */
     private int pMoveAmount = 0;
 
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
     protected int maxScore = 10;
 
 	/**
 	 * The y position of the two dice
 	 */
 	protected int[] diceYPos = {0,0}; //represents the current position
 
     /**
      * The map of values the ball can land
      */
      protected int[] boardMap = {   0,    //11
                                     0,    //10
                                     0,    // 9
                                     0,    // 8
                                     0,    // 7
                                     0,    // 6
                                     0,    // 5
                                     0,    // 4
                                     0,    // 3
                                     0,    // 2
                                     0,    // 1
                                     0,    // 0
                                     0,    //-1
                                     0,    //-2
                                     0,    //-3
                                     0,    //-4
                                     0,    //-5
                                     0,    //-6
                                     0,    //-7
                                     0,    //-8
                                     0,    //-9
                                     0,    //-10
                                     0,    //-11
     };
 
 	/**
 	 * The home y position of the two dice
 	 */
 	protected int[] diceHomeYPosition = {0,0}; //set in init
 
 	/**
 	 * The away y position of the two dice
      * Set in init
 	 */
 	protected int[] diceAwayYPosition = {0, 0};
 
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
 //        this.diceXPos[dice - 1] = this.
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
 
 
         /*
         *   treat each line as a container...
          */
        this.chipLine = 1;
         switch(this.chipLine){
             case 11:
                 this.chipYPos = this.boardMap[0];
                 break;
             case 10:
                 this.chipYPos = this.boardMap[1];
                 break;
             case 9:
                 this.chipYPos = this.boardMap[2];
                 break;
             case 8:
                 this.chipYPos = this.boardMap[3];
                 break;
             case 7:
                 this.chipYPos = this.boardMap[4];
                 break;
             case 6:
                 this.chipYPos = this.boardMap[5];
                 break;
             case 5:
                 this.chipYPos = this.boardMap[6];
                 break;
             case 4:
                 this.chipYPos = this.boardMap[7];
                 break;
             case 3:
                 this.chipYPos = this.boardMap[8];
                 break;
             case 2:
                 this.chipYPos = this.boardMap[9];
                 break;
             case 1:
                 this.chipYPos = this.boardMap[10];
                 break;
             case 0:
                 this.chipYPos = this.boardMap[11];
                 break;
             case -1:
                 this.chipYPos = this.boardMap[12];
                 break;
             case -2:
                 this.chipYPos = this.boardMap[13];
                 break;
             case -3:
                 this.chipYPos = this.boardMap[14];
                 break;
             case -4:
                 this.chipYPos = this.boardMap[15];
                 break;
             case -5:
                 this.chipYPos = this.boardMap[16];
                 break;
             case -6:
                 this.chipYPos = this.boardMap[17];
                 break;
             case -7:
                 this.chipYPos = this.boardMap[18];
                 break;
             case -8:
                 this.chipYPos = this.boardMap[19];
                 break;
             case -9:
                 this.chipYPos = this.boardMap[20];
                 break;
             case -10:
                 this.chipYPos = this.boardMap[21];
                 break;
             case -11:
                 this.chipYPos = this.boardMap[22];
                 break;
         }
        // 40 shouldn't be hard coded in
		//this.chipYPos = this.chipInitYPos - (this.chipLine * this.pMoveAmount);
        Log.v(LOGTAG, "adjust amount: " + ((9*boardHeight)/22 - (8*boardHeight)/22)/2);
        Log.v(LOGTAG, "BOARD HEIGHT: " + boardHeight);
        Log.v(LOGTAG, "Chip Line: " + this.chipLine);
		Log.v(LOGTAG, "Setting chipYPos: " + this.chipYPos);
 		
 		invalidate();
 		
 		return this.chipLine;
 	}
 
     /**
      * Initializes the boardmap for proper chip placement
      */
     private void initBoardMap(){
        int adjustAmount = (fieldHeight)/22;
         //Log.v(LOGTAG, "initboardadjust: " + adjustAmount);
         this.boardMap[0] = this.chipInitYPos - adjustAmount*21;
         this.boardMap[1] = this.chipInitYPos - adjustAmount*19;
         this.boardMap[2] = this.chipInitYPos - adjustAmount*17;
         this.boardMap[3] = this.chipInitYPos - adjustAmount*15;
         this.boardMap[4] = this.chipInitYPos - adjustAmount*13;
         this.boardMap[5] = this.chipInitYPos - adjustAmount*11;
         this.boardMap[6] = this.chipInitYPos - adjustAmount*9;
         this.boardMap[7] = this.chipInitYPos - adjustAmount*7;
         this.boardMap[8] = this.chipInitYPos - adjustAmount*5;
         this.boardMap[9] = this.chipInitYPos - adjustAmount*3;
         this.boardMap[10] = this.chipInitYPos - adjustAmount;
         this.boardMap[11] = this.chipInitYPos;
         this.boardMap[12] = this.chipInitYPos + adjustAmount;
         this.boardMap[13] = this.chipInitYPos + adjustAmount*3;
         this.boardMap[14] = this.chipInitYPos + adjustAmount*5;
         this.boardMap[15] = this.chipInitYPos + adjustAmount*7;
         this.boardMap[16] = this.chipInitYPos + adjustAmount*9;
         this.boardMap[17] = this.chipInitYPos + adjustAmount*11;
         this.boardMap[18] = this.chipInitYPos + adjustAmount*13;
         this.boardMap[19] = this.chipInitYPos + adjustAmount*15;
         this.boardMap[20] = this.chipInitYPos + adjustAmount*17;
         this.boardMap[21] = this.chipInitYPos + adjustAmount*19;
         this.boardMap[22] = this.chipInitYPos + adjustAmount*21;
 
     }
 	
 	/**
 	 * Prepares the resources for onDraw
 	 */
 	private void init() {
         this.boardHeight = this.canvas.getHeight();
 
         this.awayGoalLine = boardHeight*1/24;
         this.homeGoalLine = boardHeight*23/24;
         this.fieldHeight = this.homeGoalLine - this.awayGoalLine;
 		
 		//need to make this more dynamic for the smaller devices
 		//
 		this.diceHomeYPosition[0] = this.canvas.getHeight() - (this.dice[0].getCurrent().getHeight() + this.boardHeight/22 + 20);
 		this.diceHomeYPosition[1] = this.diceHomeYPosition[0] - this.dice[0].getCurrent().getHeight() - 20;
 
         this.diceAwayYPosition[0] = this.boardHeight/22 + 20;
         this.diceAwayYPosition[1] = this.diceAwayYPosition[0] + this.dice[0].getCurrent().getHeight() + 20;
 		
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
         this.pMoveAmount = (this.homeGoalLine - this.awayGoalLine)/22;
 
        Log.v(LOGTAG, "Cur Y Pos: " + this.chipYPos);
        Log.v(LOGTAG, "Board Height: " + this.boardHeight);
        Log.v(LOGTAG, "Move Amount: " + this.pMoveAmount);

 		this.chipLine = 0;
 		
 		this.paint = new Paint(); 
 		paint.setColor(Color.WHITE); 
 		paint.setStyle(Style.FILL); 
 		this.canvas.drawPaint(paint); 
 
 		paint.setColor(Color.RED); 
 		paint.setTextSize(40);
         this.initBoardMap();
 		
 		this.initSet = 1;
 		
 	}
 
    public void showToast(String text) {
        Context context = this.getContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		
 		this.canvas = canvas;
 		
 		if (this.initSet == 0) {
 			this.init();
 		}
 		
 		canvas.drawBitmap(this.dice[0].getCurrent(), this.canvas.getWidth() * 1/6, this.diceYPos[0], null);
 		canvas.drawBitmap(this.dice[1].getCurrent(), this.canvas.getWidth() * 1/6, this.diceYPos[1], null);
 		
 		canvas.drawBitmap(this.chip, this.chipXPos, this.chipYPos, null);
 		
 		//canvas.drawBitmap(this.goalAwayImage, this.goalXPos[1], this.goalYPos[1], null);
 		//canvas.drawBitmap(this.goalHomeImage, this.goalXPos[0], this.goalYPos[0], null);
 		
 		paint.setTextSize(this.canvas.getHeight()*2/22);
         canvas.drawText(Integer.toString(this.goalsHome), this.canvas.getWidth()*3/4, this.canvas.getHeight()*9/10, this.paint);
         canvas.drawText(Integer.toString(this.goalsAway), this.canvas.getWidth()*3/4, this.canvas.getHeight()*2/12, this.paint);
 		
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
