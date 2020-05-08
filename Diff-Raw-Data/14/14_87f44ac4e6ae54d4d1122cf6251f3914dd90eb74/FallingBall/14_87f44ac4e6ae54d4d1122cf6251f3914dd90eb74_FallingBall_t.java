 /**
  *  Class FallingBall
  *  @author Casey Harford
  *  @version 1.0
 */
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
 
     /** set slot and height */
     slot = aSlot;
     height = 0;
     oldHeight = height;
 
     /** set ball color */
     Random rand = new Random();
     float r = rand.nextFloat();
     float g = rand.nextFloat();
     float b = rand.nextFloat();
     ballColor = new Color(r,g,b);
 
     /** start the thread */
     myThread = new Thread(this);
     myThread.start();
     keepRunning = true;
     
   }
 
   public void run() {
    int ballBelow = 0;
     while (keepRunning) {
       try {
         Thread.sleep(10);
 
         /** check if ball below */
        if(manager.available(slot,(height/BALL_SIZE)+1)) { 
          ballBelow = height + BALL_SIZE;
         }
 
        if( (height+BALL_SIZE) <= ballBelow) {
          System.out.println(ballBelow);     
           height += 3;
           if( (height/BALL_SIZE) > (oldHeight/BALL_SIZE) ) {
             manager.moveBall(slot, (oldHeight/BALL_SIZE), slot, (height/BALL_SIZE));
             oldHeight = height;
           }
         }
         else {
           /** check if ball to the left */
           if(manager.available(slot-1,(oldHeight/BALL_SIZE)+1)) {
             manager.moveBall(slot, (oldHeight/BALL_SIZE), slot-1, (oldHeight/BALL_SIZE)+1);
             slot = slot-1;
             height = ((oldHeight/BALL_SIZE)+1)*BALL_SIZE;
             oldHeight = height;
           }
           /** check if ball to the right */
           if(manager.available(slot+1,(oldHeight/BALL_SIZE)+1)) {
             manager.moveBall(slot, (oldHeight/BALL_SIZE), slot+1, (oldHeight/BALL_SIZE)+1);
             slot = slot+1;
             height = ((oldHeight/BALL_SIZE)+1)*BALL_SIZE;
             oldHeight = height;
           }
         }
       }
       catch (Exception e) { }
     }
   }
 
   public void draw(Graphics g) {
     g.setColor(ballColor);
     g.fillOval(slot*BALL_SIZE,height, BALL_SIZE, BALL_SIZE);
   }
 
   public void killBall() {
     keepRunning = false;
   }  
 }
