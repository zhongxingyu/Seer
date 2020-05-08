 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
/*
 *
 *Created by Luis Martinez (Subsage)
 *with major contributions from
 *reddit.com/u/bananavice (code)
 *
 *
 *Thread for game discussion at reddit.com/r/gamedevclub under the name of GDCalaga
 *
 *
 */

 public class Manifest extends JFrame //implements ActionListener
 {
     int Width=1280;
     int Height=720;
     private GamePanel gamePanel = new GamePanel(Width,Height);
     
     
     public Manifest()
     {
         super("GDCalaga");
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         Container cp = getContentPane();
         cp.setLayout(new BorderLayout());
         JPanel p = new JPanel();
         cp.add(gamePanel);
         setSize(Width,Height);
         setResizable(true);
         //this.setUndecorated(true); //Commented this out for now, easier to debug when you can move the window around and stuff
     }
     
     public static void main(String[] args)
     {
         Manifest glt = new Manifest();
         glt.setVisible(true);glt.gameLoop();
     }
     
     
     
     //Starts a new thread and runs the game loop in it.
     public void runGameLoop()
     {
         Thread loop = new Thread()
         {
             public void run()
             {
                 gameLoop();
             }
         };
         loop.start();
     }
     
     //Only run this in another Thread!
     private void gameLoop()
     {
         gamePanel.requestFocus();
         
         final double GAME_HERTZ = 30.0;
         final double TIME_BETWEEN_UPDATES = 1000000000 / GAME_HERTZ;
         //At the very most we will update the game this many times before a new render.
         //If you're worried about visual hitches more than perfect timing, set this to 1.
         final int MAX_UPDATES_BEFORE_RENDER =5;
         double lastUpdateTime = System.nanoTime();
         double lastRenderTime = System.nanoTime();
         
         //If we are able to get as high as this FPS, don't render again.
         final double TARGET_FPS = 60;
         final double TARGET_TIME_BETWEEN_RENDERS = 1000000000 / TARGET_FPS;
         
         //Simple way of finding FPS.
         int lastSecondTime = (int) (lastUpdateTime / 1000000000);
         
         while(true)
         {
             double now = System.nanoTime();
             int updateCount = 0;
             
             //Do as many game updates as we need to, potentially playing catchup.
             while( now - lastUpdateTime > TIME_BETWEEN_UPDATES )
             {
                 updateGame();
                 lastUpdateTime += TIME_BETWEEN_UPDATES;
                 updateCount++;
             }
             
             //If for some reason an update takes forever, we don't want to do an insane number of catchups.
             //If you were doing some sort of game that needed to keep EXACT time, you would get rid of this.
             if ( now - lastUpdateTime > TIME_BETWEEN_UPDATES)
             {
                 lastUpdateTime = now - TIME_BETWEEN_UPDATES;
             }
             
             //Render. To do so, we need to calculate interpolation for a smooth render.
             float interpolation = Math.min(1.0f, (float) ((now - lastUpdateTime) / TIME_BETWEEN_UPDATES) );
             drawGame(interpolation);
             lastRenderTime = now;
             
             //Update the frames we got.
             int thisSecond = (int) (lastUpdateTime / 1000000000);
             if (thisSecond > lastSecondTime)
             {
                 gamePanel.resetFPS();
                 lastSecondTime = thisSecond;
             }
             
             //Yield until it has been at least the target time between renders. This saves the CPU from hogging.
             while ( now - lastRenderTime < TARGET_TIME_BETWEEN_RENDERS && now - lastUpdateTime < TIME_BETWEEN_UPDATES)
             {
                 Thread.yield();
                 
                 //This stops the app from consuming all your CPU. It makes this slightly less accurate, but is worth it.
                 //You can remove this line and it will still work (better), your CPU just climbs on certain OSes.
                 //FYI on some OS's this can cause pretty bad stuttering. Scroll down and have a look at different peoples' solutions to this.
                 try {Thread.sleep(1);} catch(Exception e) {}
                 
                 now = System.nanoTime();
             }
         }
     }
     
     private void updateGame()
     {
         gamePanel.update();
     }
     
     private void drawGame(float interpolation)
     {
         gamePanel.setInterpolation(interpolation);
         gamePanel.repaint();
     }
     
     
 }
