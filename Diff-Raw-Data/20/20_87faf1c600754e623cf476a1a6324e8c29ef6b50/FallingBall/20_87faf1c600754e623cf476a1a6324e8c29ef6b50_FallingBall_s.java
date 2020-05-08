 import java.awt.Color;
 import java.util.Random;
 import java.awt.Graphics;
 
 class FallingBall implements Runnable {
 
   public static final int BALL_SIZE = 30;
   private BallManager manager;
   private int slot; 
   private int height;
   private int oldHeight;
   private Color ballColor;
   private boolean keepRunning; 
   private Thread myThread;
   
   public FallingBall(int aSlot, BallManager theManager) {
     manager = theManager;
     //System.out.println("FallingBall constructor");
 
     /** set slot and height */
     slot = aSlot;
     height = 0;
     oldHeight = height;
 
     myThread = new Thread(this);
     myThread.start();
     keepRunning = true;
 
     /** set ball color */
     Random rand = new Random();
     float r = rand.nextFloat();
     float g = rand.nextFloat();
     float b = rand.nextFloat();
     ballColor = new Color(r,g,b);
     
   }
 
   public void run() {
   int ballBelow;
     while (keepRunning) {
       try {
         Thread.sleep(10);
 
         /** check if ball below */
        if(manager.available(slot,(height/BALL_SIZE)+1)) { 
           ballBelow = (manager.MAX_BALLS*BALL_SIZE-BALL_SIZE)-((height/BALL_SIZE)+1)*BALL_SIZE;
         }
         else { 
           ballBelow = (manager.MAX_BALLS*BALL_SIZE-BALL_SIZE);
         }
         //System.out.println("ballBelow = " + ballBelow/BALL_SIZE);
 
         if(height < ballBelow) {
           height += 3;
           //System.out.println("height = " + height + ", " + (manager.MAX_BALLS*BALL_SIZE) );
           if( (height/BALL_SIZE) > (oldHeight/BALL_SIZE) ) {
             //System.out.println("moving ball...new row = " + (height/BALL_SIZE) + "old row = " + oldHeight/BALL_SIZE);
             manager.moveBall(slot, (oldHeight/BALL_SIZE), slot, (height/BALL_SIZE));
             oldHeight = height;
           }
         }
         
         /** check if ball to the left */
         if(manager.available(slot-1,(oldHeight/BALL_SIZE)+2)) {
           System.out.println("spot available to left " + (slot-1) + " : " + ((oldHeight/BALL_SIZE)+2));
           //manager.moveBall(slot, (oldHeight/BALL_SIZE), slot-1, (oldHeight/BALL_SIZE));
         }
         /** check if ball to the right */
       }
       catch (Exception e) {
 
       }
     }
   }
 
   public void draw(Graphics g) {
     g.setColor(ballColor);
     g.fillOval(slot*BALL_SIZE,height, BALL_SIZE, BALL_SIZE);
   }
 
   public void killBall() {
     System.out.println("FallingBall killBall()");
     keepRunning = false;
     myThread = null;
   }  
 }
