 /*
  * File: Breakout.java
  * -------------------
  * Name: Raunaq Suri
  * This program is based off of Stanford Edu's CS106a course
  * The starter file as well as any references can be found at the following url:
  * http://see.stanford.edu/see/courseinfo.aspx?coll=824a47e1-135f-4508-a5aa-866adcae1111
  * This file will eventually implement the game of Breakout.
  */
 
 /*TODO:
  * Under play method, create the ball and let it bounce off walls <--DONE
  * Then let it bounce off of other elements and test that it works <--DONE
  * Learn how to move the paddle around <--DONE
  * Debug <--DONE
  * Declare winner <--DONE
  * add sound effects<--DONE
  * add score<--DONE
  * Hopeful due date: July 1, 2013
  * 
  */
 
 import acm.graphics.*;
 import acm.program.*;
 import acm.util.*;
 
 import java.applet.*;
 import java.awt.*;
 import java.awt.event.*;
 
 public class Breakout extends GraphicsProgram {
 
 /** Width and height of application window in pixels */
 	public static final int APPLICATION_WIDTH = 400;
 	public static final int APPLICATION_HEIGHT = 700;
 
 /** Dimensions of game board (usually the same) */
 	private static final int INFO_BOARD_SIZE=100;
 	private static final int INFO_OFFSET=30;
 	private static final int WIDTH = APPLICATION_WIDTH;
 	private static final int HEIGHT = APPLICATION_HEIGHT-INFO_BOARD_SIZE;
 	private static final int BORDER_OFFSET = 5;
 
 /** Dimensions of the paddle */
 	private static final int PADDLE_WIDTH = 80;
 	private static final int PADDLE_HEIGHT = 10;
 
 /** Offset of the paddle up from the bottom */
 	private static final int PADDLE_Y_OFFSET = 30;
 
 /** Number of bricks per row */
 	private static final int NBRICKS_PER_ROW = 10;
 
 /** Number of rows of bricks */
 	private static final int NBRICK_ROWS = 10;
 
 /** Separation between bricks */
 	private static final int BRICK_SEP = 4;
 
 /** Width of a brick */
 	private static final int BRICK_WIDTH =
 	  (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;
 
 /** Height of a brick */
 	private static final int BRICK_HEIGHT = 8;
 
 /** Radius of the ball in pixels */
 	private static final int BALL_RADIUS = 10;
 
 /** Offset of the top brick row from the top */
 	private static final int BRICK_Y_OFFSET = 70;
 
 /** Number of turns */
 	private static final int NTURNS = 3;
 	
 /**Speed at which paddle moves */
 	private static final int moveSpeed=35;
 	
 /**Pause time for the paddle as it moves */
 	private static final int pauseTimePaddle=0;
 	
 //Other instance variables
 	private GOval ball;
 	private GRect paddle;
 	private double vx, vy;//x and y velocities of the ball
 	private int hitPaddle=0; //the number of times the ball has hit the paddle
 	private int bricksLeft =NBRICK_ROWS*NBRICKS_PER_ROW;
 	private int livesLeft = NTURNS;
 	private GLabel lives= new GLabel("Filler text");
 	private GLabel score= new GLabel("Filler text");
 	private int scoreCount=0;
 	private GPoint initialBallPos;
 
 //Sound files
 	//From starter file found on: http://see.stanford.edu/see/materials/icspmcs106a/assignments.aspx
	private AudioClip bounceClip= MediaTools.loadAudioClip("bounce.au");
 	//Royalty-free music found from: http://teknoaxe.com/cgi-bin/link_code_2.pl?386
 	//The person's youtube channel is :http://www.youtube.com/user/teknoaxe/
 	private AudioClip BGM = MediaTools.loadAudioClip("EightBitRockinVillageLoop.au");//Background game music
 
 	
 
 /** Runs the Breakout program. */
 	//Random generator to be used throughout
 	private RandomGenerator rgen = new RandomGenerator();
 	
 	public void run() {
 		setBackground(Color.black);
 		//Sets up the game
 			addKeyListeners();
 			addMouseListeners();
             BGM.loop();
             setup();
        //The game is being played
             play();
             //Game has ended
             BGM.stop();
             removeAll();
         	displayResult();
             
             
 	}
         private void setup(){
         	drawBorders();
         	createInfoBoard();
             	setBricks();
             	createPaddle();
         }
         private void drawBorders() {
         	//The following method will create borders around the game in order to visually show where the walls are
 			GLine leftBorder = createBorder(0,0,0,APPLICATION_HEIGHT);
 			GLine rightBorder = createBorder(APPLICATION_WIDTH+BORDER_OFFSET,0,APPLICATION_WIDTH+BORDER_OFFSET,APPLICATION_HEIGHT+BORDER_OFFSET);
 			GLine topBorder =createBorder(0,0,APPLICATION_WIDTH+BORDER_OFFSET,0);
 			GLine bottomBorder=createBorder(0,APPLICATION_HEIGHT,APPLICATION_WIDTH+BORDER_OFFSET,APPLICATION_HEIGHT+BORDER_OFFSET);
 			GLine centerBorder=createBorder(0,HEIGHT+BORDER_OFFSET,WIDTH+BORDER_OFFSET,HEIGHT+BORDER_OFFSET);
 			add(leftBorder);
 			add(rightBorder);
 			add(topBorder);
 			add(bottomBorder);
 			add(centerBorder);
 		}
         private GLine createBorder(double x, double y, double x2, double y2){
         	//Creates a specific border in blue
         	GLine name = new GLine(x,y,x2,y2);
         	name.setColor(Color.blue);
         	return name;
         }
 		private void createInfoBoard() {
 			//Creates the board which displays the score and number of lives left
 			int textPadding=25;
         	//Displays the number of lives left
 			lives.setFont(new Font("Arial",Font.BOLD,18));
 			lives.setLocation(textPadding,APPLICATION_HEIGHT-INFO_BOARD_SIZE+INFO_OFFSET);
 			lives.setLabel("Lives Left: "+Integer.toString(livesLeft));
 			lives.setColor(Color.WHITE);
 			add(lives);
 			
 			//Displays score
 			score.setFont(new Font("Arial",Font.BOLD,18));
 			score.setLabel("Score: "+Integer.toString(scoreCount));
 			score.setLocation(APPLICATION_WIDTH-score.getWidth()-textPadding,APPLICATION_HEIGHT-INFO_BOARD_SIZE+INFO_OFFSET);
 			score.setColor(Color.WHITE);
 			add(score);
 			
 		}
         private void updateInfoBoard(){
         	//Updates the board as new information is recorded
         	lives.setLabel("Lives Left: "+Integer.toString(livesLeft));
         	add(lives);
         	
         	score.setLabel("Score: "+Integer.toString(scoreCount));
         	add(score);
         }
 		private void setBricks(){
 			//This method creates all the bricks
             for(int row=0; row<NBRICK_ROWS; row++){
                 for(int col=0; col<NBRICKS_PER_ROW;col++){
                     //Creates all the bricks in a row
                     GRect brick = new GRect(BRICK_SEP+col*(BRICK_SEP+BRICK_WIDTH),BRICK_Y_OFFSET+row*(BRICK_SEP+BRICK_HEIGHT),BRICK_WIDTH,BRICK_HEIGHT);
                     brick.setVisible(true);
                     brick.setFilled(true);
                     brick.setColor(Color.BLACK);
                     //Does a switch statement to choose between the different colours
                     switch(row){
                         //First and second rows are red
                         case 0:
                         case 1:
                             brick.setFillColor(Color.RED);
                             break;
                         //Third and fourth rows are orange
                         case 2:
                         case 3:
                             brick.setFillColor(Color.ORANGE);
                             break;
                        //fifth and sixth rows are yellow
                         case 4:
                         case 5:
                             brick.setFillColor(Color.YELLOW);
                             break;
                         //7th and 8th rows are green
                         case 6:
                         case 7:
                             brick.setFillColor(Color.GREEN);
                             break;
                         //9th and 10th rows are cyan
                         case 8:
                         case 9:
                             brick.setFillColor(Color.CYAN);
                             break;
                         //Any row further than the ones are automatically CYAN
                         default:
                             brick.setFillColor(Color.CYAN);
                             break;
                     }
                     add(brick);
                 }
             }
         }
         private void createPaddle(){
         	//sets the x and y coordinates for the paddle
         	int paddleX=(WIDTH/2 -(PADDLE_WIDTH/2));
         	int paddleY=(HEIGHT-PADDLE_Y_OFFSET-PADDLE_HEIGHT);
         	//creates the paddle
         	paddle = new GRect(paddleX, paddleY, PADDLE_WIDTH, PADDLE_HEIGHT);
         	paddle.setFilled(true);
         	paddle.setColor(Color.white);
         	paddle.setFillColor(Color.WHITE);
         	add(paddle);
         }
         private void play(){
         	//Creates the ball
         	createBall();
         	//Ball starts moving when mouse is clicked
         	//The game is playing
         	while(!isGameWon()&&!isGameLost()){
         		ball.move(vx, vy);
         		checkForCollisions();
         		pause(30);
         	}
         	//The game has ended
         	
         }
         private void createBall(){
         	//Creates the ball
         	ball = new GOval(WIDTH/2-BALL_RADIUS,HEIGHT/2-BALL_RADIUS, 2*BALL_RADIUS, 2*BALL_RADIUS);
         	ball.setColor(Color.white);
         	ball.setFilled(true);
         	ball.setFillColor(Color.WHITE);
         	add(ball);
         	//The ball's initial position is stored in a point
         	initialBallPos=new GPoint(ball.getX(),ball.getY());
         }
 		private void checkForCollisions() {
 			//Checks if the ball has collided with anything
 			//If ball has collided with a wall, it moves in the opposite direction
 			if(ball.getX()<=0||(ball.getX()+2*BALL_RADIUS)>=WIDTH){
 				vx=-vx;
 				bounceClip.play();
 			}
 			if(ball.getY()<=0){
 				vy=-vy;
 				bounceClip.play();
 			}
 			
 			//If the ball collides with a bottom wall, the player loses a life and the ball is returned to initial position
 			//A life is also lost if the ball goes below the paddle
 			if(((ball.getY()+2*BALL_RADIUS)>=HEIGHT)||(ball.getY()>paddle.getY())){
 				livesLeft--;
 				updateInfoBoard();
 				remove(ball);
 				createBall();
 				pause(2000);
 				setInitialVelocities();
 			}
 			
 			//Checks to see which object the ball collided with, if it did
 			GObject collider = getCollidingObject();
 			if(collider!=null){
 				//If the ball collided with the paddle, the ball bounces off
 				if(collider==paddle){
 					hitPaddle++;
 					/*In order to make the game interesting, every 5 times the ball
 					 * hits the paddle, the x and y velocity of the ball is increased
 					 */
 					if(hitPaddle%5==0){
 						if(vx>0){
 							vx+=0.5;
 							if(vy>0){
 								vy+=0.5;
 							}else{
 								vy-=0.5;
 							}
 						}else{
 							vx-=0.5;
 							if(vy>0){
 								vy+=0.5;
 							}else{
 								vy-=0.5;
 							}
 						}
 					}
 					vy=-vy;
 					//Ball hits left corner of paddle and is coming from the left
 					bounceClip.play();
 					
 				}else if(collider.getColor()!=Color.blue){
 					//The object that it collided with must have been a brick
 					//The brick is removed after the ball has hit it
 					bricksLeft--;
 					vy*=-1;
 					scoreCount+=countScore((GRect) collider);
 					updateInfoBoard();
 					remove(collider);
 					bounceClip.play();
 				}
 			}
 		}
 		private GObject getCollidingObject(){
 			//Checks to see if there is another object present where the ball is
 			//If there isn't, a null value is returned
 			
 		
 				if(getElementAt(ball.getX(),ball.getY())!=null){
 				
 					/*Checks the top left of the ball*/
 					return(getElementAt(ball.getX(),ball.getY()));
 				
 				}else if(getElementAt(ball.getX()+2*BALL_RADIUS, ball.getY())!=null){
 				
 					/*Checks the top right of the ball*/
 					return getElementAt(ball.getX()+2*BALL_RADIUS, ball.getY());
 			
 				}else if(getElementAt(ball.getX(), ball.getY()+2*BALL_RADIUS)!=null){
 			
 					/*checks the bottom left of the ball*/
 					return getElementAt(ball.getX(), ball.getY()+2*BALL_RADIUS);
 				
 				}else if(getElementAt(ball.getX()+2*BALL_RADIUS, ball.getY()+2*BALL_RADIUS)!=null){
 				
 					/*checks the bottom right of the ball*/
 					return getElementAt(ball.getX()+2*BALL_RADIUS, ball.getY()+2*BALL_RADIUS);
 				
 				}else{return null;}
 			}
 			
 		private int countScore(GRect brick){
 			//Counts the score received by hitting a certain brick
 			//Each brick colour gives a different score
 			int score;
 			if(brick.getFillColor()==Color.RED){
 				score=50;
 			}else if(brick.getFillColor()==Color.ORANGE){
 				score=40;
 			}else if(brick.getFillColor()==Color.YELLOW){
 				score=30;
 			}else if(brick.getFillColor()==Color.GREEN){
 				score=20;
 			}else{
 				score=10;
 			}
 			return score;
 		}
 			
 		private boolean isGameWon(){
 			//The game is over when either all the bricks are gone
 			if(bricksLeft==0){return true;}else{return false;}
 		}
 		private boolean isGameLost(){
 			//The game is also if all lives are lost
 			if(livesLeft==0){return true;}else{return false;}
 		}
 		private void displayResult(){
 			//Displays Whether the user has won or lost
 			String answer = null;
 			if(isGameWon()){answer=" Won";}
 			if(isGameLost()){answer= " Lost";}
 			GLabel result = new GLabel("Game Over. You"+answer+"! Final score of: "+Integer.toString(scoreCount));
 			result.setFont(new Font("Arial",Font.BOLD,20));
 			result.setLocation((WIDTH/2)-(result.getWidth()/2),(HEIGHT/2)+(result.getHeight()/2));
 			result.setColor(Color.WHITE);
 			add(result);
 		}
 			
 		public void keyPressed(KeyEvent e){
 			switch(e.getKeyCode()){
 			//Paddle moves to the right
 			case KeyEvent.VK_RIGHT:
 				//Paddle is not out of bounds
 				if((paddle.getX()+PADDLE_WIDTH)<WIDTH-BORDER_OFFSET){
 				paddle.move(moveSpeed, 0); //Paddle moves  to the right per time as long as key is pressed
 				pause(pauseTimePaddle);
 				}
 				break;
 			case KeyEvent.VK_LEFT:
 				//Paddle is not out of bounds
 				if(paddle.getX()>0){
 				paddle.move(-moveSpeed, 0);//Paddle moves to the left as long as key is pressed
 				pause(pauseTimePaddle);
 				}
 				break;
 				
 			}
 		}
 		
 		
 		public void mouseClicked(MouseEvent e){
 			//If the ball is reset or the game is starting off, a click will start the ball's movement
 			
 			if((ball.getX()==initialBallPos.getX())&&(ball.getY()==initialBallPos.getY())){
 				setInitialVelocities();
 				
 			}
 		}
 		private void setInitialVelocities(){
 			// Sets the initial velocities of the ball
 			vx=rgen.nextDouble(3.0,5.0);
         	if(rgen.nextBoolean(0.5)){
         		vx=-vx;
         	}
         	vy=rgen.nextDouble(5.0,7.0);
 		}
 			
 				
 }
