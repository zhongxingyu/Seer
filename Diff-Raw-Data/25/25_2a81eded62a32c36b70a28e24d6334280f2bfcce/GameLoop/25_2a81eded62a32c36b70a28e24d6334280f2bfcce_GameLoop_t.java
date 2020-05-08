 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package co.knights.games.bubblepopper;
 
 import javafx.animation.Animation;
 import javafx.animation.KeyFrame;
 import javafx.animation.Timeline;
 import javafx.animation.TimelineBuilder;
 import javafx.application.Application;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.scene.Parent;
 import javafx.util.Duration;
 import co.knights.games.bubblepopper.data.PlayerData;
 
 /**
 * Runs the Game Loop, 
  *
  * @author hope
  */

// Only way i could make it thread friendly
// Ideas are open
 public class GameLoop {
     private static Timeline gameLoop;
     private final int fps;
     private final PlayerData pd;
     private static Parent p;
 
     public GameLoop(final int fps, Parent p) {
         this.fps = fps;
         GameLoop.p = p;
         pd = new PlayerData("Erick");
         buildAndSetGameLoop();
     }
     
     private static Parent getRoot() {
         return p;
     }
 
    protected final void buildAndSetGameLoop() {
 
         final Duration oneFrameAmt = Duration.millis(1000 / (float) getFramesPerSecond());
         final KeyFrame oneFrame = new KeyFrame(oneFrameAmt,
                 new EventHandler<ActionEvent>() {
 
                     @Override
                     public void handle(javafx.event.ActionEvent event) {
                        // Everthing thing that needs to be listened to
                         pd.update();
                     }
                });
 
         // sets the game world's game loop (Timeline)
         setGameLoop(TimelineBuilder.create()
                 .cycleCount(Animation.INDEFINITE)
                 .keyFrames(oneFrame)
                 .build());
     }
 
     private void setGameLoop(Timeline build) {
         GameLoop.gameLoop = build;
     }
 
     public static Timeline getGameLoop() {
         return gameLoop;
     }
     
     private int getFramesPerSecond() {
         return fps;
     }
     
     public void beginGameLoop() {
         getGameLoop().play();
     }
 }
