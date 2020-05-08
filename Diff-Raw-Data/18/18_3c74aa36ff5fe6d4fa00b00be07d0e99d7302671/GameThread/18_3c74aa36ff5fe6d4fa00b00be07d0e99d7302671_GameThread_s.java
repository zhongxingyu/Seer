 package game;
 
 public class GameThread extends Thread {
     private static final long FRAME_RATE = 1000 / 60;
     private long lastUpdate = 0;
     
     public void run() {
         
         while (true) {
             int frames = waitForNextFrame();
            
             // CL - We need to get input even if the game is paused,
             //      that way we can unpause the game.
             game.Game.getInputHandler().update();
             
             // Don't update the game state if we are paused
             if(game.Game.isPaused())
                return;
 
             game.Game.getPlayer().updateCamera();
 
             
             // Update the actors
             actor.Actor.updateActors(frames);         
         }
         
     }

     private int waitForNextFrame() {
         long current = System.currentTimeMillis();
         // handle first frame case
         if (lastUpdate == 0) {
             lastUpdate = current;
             return 1;
         }
         
         long elapsed = current - lastUpdate;
         
         int frames = (int)(elapsed / FRAME_RATE) + 1;
         long delay = FRAME_RATE * frames - elapsed;
         
         try {
             sleep(delay);
         } catch (InterruptedException e) {
          
             e.printStackTrace();
         }
         
         return frames;
     }
 }
