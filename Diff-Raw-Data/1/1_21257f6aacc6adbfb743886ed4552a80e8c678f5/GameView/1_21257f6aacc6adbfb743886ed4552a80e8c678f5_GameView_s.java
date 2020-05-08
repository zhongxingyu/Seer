 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.me.qpad;
 
 /**
  *
  * @author burke
  */
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.RectF;
 //import android.graphics.Typeface;
 import android.view.View;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 //import android.location.*;
 import android.os.Bundle;
 import java.util.LinkedList;
 import java.lang.Math;
 import android.content.Intent;
 
 public class GameView extends View {
   private boolean debug = false;
    private boolean beenReset = false;
    private boolean gameOver = false;
    private boolean burkeMode = false;
    private boolean touchingScreen = false;
    private boolean isColliding = false;
 
    private int lives = 3;
    private int paddleBottomMargin = 20;
    private int score = 0;
    private int xMin = 0;          // This view's bounds
    private int xMax;
    private int yMin = 0;
    private int yMax;
    private float ballRadius = 4; // Ball's radius
    private float ballX = ballRadius + 50;  // Ball's center (x,y)
    private float ballY = ballRadius + 75;
    private float ballSpeedX = 2;  // Ball's speed (x,y)
    private float ballSpeedY = 2;
    private float paddleX;// = 20;
    private float paddleY;// = 20;
    //private float paddleYFixed;
    //private float paddleXFixed;
    private float paddleWidth = 5f;
    private float paddleLength = 100f;
    private RectF ballBounds;      // Needed for Canvas.drawOval
    private Paint paint;           // The paint (e.g. style, color) used for drawing
    //LocationManager locationManager;
    //LocationListener locationListener;
    private RectF paddleLeft, paddleRight, paddleBottom, paddleTop;
 
    private float previousX;
    private float previousY;
    private Double latitude = 0.0;
    private Double drawLat = 0.0;
    private Double longitude = 0.0;
    private Double drawLong = 0.0;
    private Double altitude = 0.0;
    private float speed = 0.0f;
    private String exStr = " ";
    Context parentContext;
 
    float horizDiff = 0;
    float vertDiff = 0;
    float leftDiff = 0;
    float rightDiff = 0;
    float topDiff = 0;
    float bottomDiff = 0;
 
    int blocksMinX = 70;
    int blocksMinY = 100;
    int blocksMaxX = 100;
    int blocksMaxY = 220;
    int blockSetWidth = 0; //always leave these set to 0 - use the pixel values above
    int blockSetHeight = 0;//always leave these set to 0 - use the pixel values above
 
    private BlockSet set1;
 
    private RectF blocksRect;
 
    GraphicButton btnStart;
 
    private LinkedList<Block> lstBlocks;
    private LinkedList<GraphicButton> lstGbuttons;
 
    LinkedList<Block> blocksToRemove = new LinkedList<Block>();
 
    // Constructor
    public GameView(Context context) {
       super(context);
       parentContext = context;
 
       lstGbuttons = new LinkedList<GraphicButton>();
 
       ballBounds = new RectF();
       paddleLeft = new RectF();
       paddleRight = new RectF();
       paddleTop = new RectF();
       paddleBottom = new RectF();
 
       paint = new Paint();
       this.setFocusable(true);
       this.requestFocus();
       this.setFocusableInTouchMode(true);
 
       resetGame();
 
    }
 
    private void resetBall()
    {
      ballX = 40;
      ballY = 20;    
      ballSpeedX = 2;
      ballSpeedY = 2;
      resetPaddles();
    }
 
    private void detectBallCollisions()
    {
        boolean changeX = false;
        boolean changeY = false;
        
        if((ballBounds.intersect(paddleTop) || ballBounds.intersect(paddleBottom)) && !isColliding)
        {
            isColliding = true;
            changeX = false;
            ballSpeedY *= -1;
            if (ballSpeedX > 0 && ballX < (paddleTop.left + (paddleLength / 4.5)))
            {
                changeX = true;
            }
            else if (ballSpeedX < 0 && ballX > (paddleTop.right - (paddleLength / 4.5)))
            {
                changeX = true;
            }
 
            if(changeX)
            {
                ballSpeedX *= -1;
            }
        }
        else if((ballBounds.intersect(paddleLeft) || ballBounds.intersect(paddleRight)) && !isColliding)
        {
            isColliding = true;
            ballSpeedX *= -1;
            changeY = false;
 
            if (ballSpeedY > 0 && ballY < (paddleRight.top + (paddleLength / 4.5)))
            {
                changeY = true;
            }
 
            else if (ballSpeedY < 0 && ballY > (paddleRight.bottom - (paddleLength / 4.5)))
            {
                changeY = true;
            }
 
            if(changeY)
            {
                ballSpeedY *= -1;
            }
 
        }
        else
            isColliding = false;
 
        //detect collisions with walls
        if(ballX + ballRadius > xMax) {
          if(lives == 0)
          {
             gameOver = true;
          }
          else
          {
              lives--;
          }
          resetBall();
 
       } else if (ballX - ballRadius < xMin) {
          if(lives == 0)
          {
             gameOver = true;
          }
          else
          {
              lives--;
          }
          resetBall();
       }
       if (ballY + ballRadius > yMax) {
          if(lives == 0)
          {
             gameOver = true;
          }
          else
          {
              lives--;
          }
          resetBall();
       } else if (ballY - ballRadius < yMin) {
          if(lives == 0)
          {
             gameOver = true;
          }
          else
          {
              lives--;
          }
          resetBall();
       }
 
 
        else if(ballBounds.intersect(blocksRect)){
 
            for (Block b : lstBlocks)
            {
                RectF blockBounds = b.getRect();
                if (ballBounds.intersect(blockBounds))
                {
                     horizDiff = 0;
                     vertDiff = 0;
 
                     leftDiff = 0;
                     rightDiff = 0;
                     topDiff = 0;
                     bottomDiff = 0;
 
                     //check which sides intersect
                     if (ballBounds.right > blockBounds.left)
                         leftDiff = Math.abs(blockBounds.left - ballBounds.right);
 
                     if (ballBounds.left < blockBounds.right)
                         rightDiff = Math.abs(blockBounds.right - ballBounds.left);
 
                     if (ballBounds.bottom > blockBounds.top)
                         topDiff = Math.abs(blockBounds.top - ballBounds.bottom);
 
                     if (ballBounds.top < blockBounds.bottom)
                         bottomDiff = Math.abs(blockBounds.bottom - ballBounds.top);
 
                     if (ballSpeedX > 0) //check ball direction for X-axis intersect
                         horizDiff = leftDiff;
                     else
                         horizDiff = rightDiff;
 
                     if (ballSpeedY > 0) //check ball direction for Y-axis intersect
                         vertDiff = topDiff;
                     else
                         vertDiff = bottomDiff;
 
                     //compare diffs
                     if(horizDiff < vertDiff)
                         ballSpeedX *= -1;
                     else if (horizDiff > vertDiff)
                         ballSpeedY *= -1;
 
                     else if (horizDiff == vertDiff)
                     {
                         ballSpeedX *= -1;
                         ballSpeedY *= -1;
                     }
 
                     score++;
                     blocksToRemove.add(b);
                     break;
                }
            }
            if (blocksToRemove.size() > 0)
            {
                for (Block b : blocksToRemove)
                {
                    lstBlocks.remove(b);
                }
                blocksToRemove.clear();
            }
        }
    }
 
    private void resetGame()
    {
       score = 0;
       lives = 3;
       gameOver = false;
 
       ballX = 40;
       ballY = 20;
       ballSpeedX = 2;
       ballSpeedY = 2;
 
       //paddleX = (xMax - paddleLength) / 2;
       //paddleY = (yMax - paddleLength) / 2;
 
       //int width = 0;
       //int blkX = blocksMinX;
       //int blkY = blocksMinY;
       resetBlocksAndPaddles();
       beenReset = true;
    }
 
    // Called back to draw the view. Also called after invalidate().
    @Override
    protected void onDraw(Canvas canvas) {
       // Draw the ball
       if (gameOver)
       {
           canvas.drawText("Game Over", (xMax / 2) - 30, 20, paint);
 
           for (GraphicButton b : lstGbuttons)
           {
               b.draw(canvas);
           }
           canvas.drawText("Score: " + score, 5, yMax - 5, paint);
           
       }
       else
       {
           ballBounds.set(ballX-ballRadius, ballY-ballRadius, ballX+ballRadius, ballY+ballRadius);
           paddleLeft.set(1, paddleY, paddleWidth + 1, paddleY + paddleLength);
           paddleRight.set(xMax - paddleWidth, paddleY, xMax, paddleY + paddleLength);
           paddleTop.set(paddleX, 1, paddleX + paddleLength, paddleWidth + 1);
           paddleBottom.set(paddleX, yMax - paddleWidth - paddleBottomMargin, paddleX + paddleLength, yMax - paddleBottomMargin);
 
           paint.setColor(Color.GREEN);
           canvas.drawText("Score: " + score, 5, yMax - 5, paint);
           canvas.drawText("Lives: " + lives, xMax - 45, yMax - 5, paint);
 
 
           paint.setColor(Color.GRAY);
           //paint.setTextSize(20);
           //int test = Float.floatToIntBits(paddleXFixed);
           canvas.drawRect(ballBounds, paint);
 
           for (Block b : lstBlocks)
           {
             b.draw(canvas);
           }
 
           //paint.setColor(Color.GRAY);
           canvas.drawRect(paddleTop, paint);
           canvas.drawRect(paddleBottom, paint);
           canvas.drawRect(paddleLeft, paint);
           canvas.drawRect(paddleRight, paint);
           update();
 
 
           // Delay
           //try {
           //   Thread.sleep(0);
           //} catch (InterruptedException e) { }
 
             //invalidate();  // Force a re-draw
        }
       invalidate();
 
    }
 
    // Detect collision and update the position of the ball.
 
    private void update() {
       // Get new (x,y) position
       detectBallCollisions();
 
       ballX += ballSpeedX;
       ballY += ballSpeedY;
 
       if (!touchingScreen)
       {
           if (paddleX < -(paddleLength / 5))
               paddleX += 5;
           else if (paddleX < 0)
               paddleX += 1;
 
           if (paddleY < -(paddleLength / 5))
               paddleY += 5;
           else if (paddleY < 0)
               paddleY += 1;
 
           if (paddleX > ((xMax - paddleLength) + paddleLength / 5))
               paddleX -= 5;
           else if (paddleX > (xMax - paddleLength))
               paddleX -= 1;
 
           if (paddleY > ((yMax - paddleLength)- paddleBottomMargin) + paddleLength / 5)
               paddleY -= 5;
           else if (paddleY > (yMax - paddleLength)- paddleBottomMargin)
               paddleY -= 1;
       }
 
       if (burkeMode)
       {
           paddleX = ballX - (paddleLength / 2);
           paddleY = ballY - (paddleLength / 2);
       }
       // Detect collision and react
 
       //paddleX = ballX - 35;
       //paddleY = ballY - 35;
    }
 
    // Key-up event handler
    //@Override
    /*public boolean onKeyUp(int keyCode, KeyEvent event) {
       switch (keyCode) {
          case KeyEvent.KEYCODE_DPAD_RIGHT: // Increase rightward speed
             ballSpeedX++;
             break;
          case KeyEvent.KEYCODE_DPAD_LEFT:  // Increase leftward speed
             ballSpeedX--;
             break;
          case KeyEvent.KEYCODE_DPAD_UP:    // Increase upward speed
             ballSpeedY--;
             break;
          case KeyEvent.KEYCODE_DPAD_DOWN:  // Increase downward speed
             ballSpeedY++;
             break;
          case KeyEvent.KEYCODE_DPAD_CENTER: // Stop
             ballSpeedX = 0;
             ballSpeedY = 0;
             break;
          case KeyEvent.KEYCODE_A:    // Zoom in
             // Max radius is about 90% of half of the smaller dimension
             float maxRadius = (xMax > yMax) ? yMax / 2 * 0.9f  : xMax / 2 * 0.9f;
             if (ballRadius < maxRadius) {
                ballRadius *= 1.05;   // Increase radius by 5%
             }
             break;
          case KeyEvent.KEYCODE_Z:    // Zoom out
             if (ballRadius > 20) {   // Minimum radius
                ballRadius *= 0.95;   // Decrease radius by 5%
             }
             break;
       }
       return true;  // Event handled
    }*/
 
    // Touch-input handler
    @Override
    public boolean onTouchEvent(MotionEvent event) {
       float currentX = event.getX();
       float currentY = event.getY();
 
       if (gameOver)
       {
           RectF touchRect = new RectF(currentX - 1, currentY - 1, currentX + 1, currentY + 1);
           
           for (GraphicButton b : lstGbuttons)
           {
               if (touchRect.intersect(b.getRect()))
               {
                       if(b.getLabel().equals("Play Again"))
                           resetGame();
 
                       if(b.getLabel().equals("Exit"))
                           System.exit(0);
               }                
           }
 
 
       }
       else
       {
           burkeMode = false;
           float deltaX, deltaY;
           //float scalingFactor = 5.0f / ((xMax > yMax) ? yMax : xMax);
           switch (event.getAction()) {
              case MotionEvent.ACTION_MOVE:
                 // Modify rotational angles according to movement
                 deltaX = currentX - previousX;
                 deltaY = currentY - previousY;
                 //ballSpeedX += deltaX * scalingFactor;
                 //ballSpeedY += deltaY * scalingFactor;
 
 
 
                 if (paddleX >= -(paddleLength -5) && paddleX <= (xMax - 5))
                 {
                     paddleX = paddleX + (currentX - previousX);
                 }
 
                 if (paddleY >= -(paddleLength - 5) && paddleY <= (yMax - 5))
                 paddleY = paddleY + (currentY - previousY);
                 break;
               case MotionEvent.ACTION_UP:
                   touchingScreen = false;
                   break;
               case MotionEvent.ACTION_DOWN:
                   touchingScreen = true;
 
 
           }
           // Save current x, y
           previousX = currentX;
           previousY = currentY;
       }
       return true;  // Event handled
    }
 
    public void resetPaddles()
    {
       paddleX = (xMax - paddleLength) / 2;
       paddleY = (yMax - paddleLength) / 2;
    }
 
    public void resetBlocksAndPaddles()
    {
       set1 = new BlockSet(xMax/4, (yMax / 6), ((xMax - (xMax/2)) / 12), ((yMax - (yMax / 2)) / 12), 1);
       lstBlocks = set1.getBlocks();
       blocksRect = set1.getBounds();
       paddleX = (xMax - paddleLength) / 2;
       paddleY = (yMax - paddleLength) / 2;
       beenReset = false;
    }
 
    // Called back when the view is first created or its size changes.
    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
       // Set the movement bounds for the ball
       xMax = w-1;
       yMax = h-1;
 
       lstGbuttons.clear();
       int btnX = ((xMax / 2) / 2) - 50;
       lstGbuttons.add(new GraphicButton(btnX, 50, 85, 55, 16, 30, "Play Again"));
       btnX = ((xMax / 2) + ((xMax / 2) / 2)) - 50;
       lstGbuttons.add(new GraphicButton(btnX, 50, 85, 55, 35, 30, "Exit"));
       //lstGbuttons.add(new GraphicButton(50, 100, 85, 85, Integer.toString(btnX)));
 
       if (beenReset)
       {
         resetBlocksAndPaddles();
       }
 
    }
 }
