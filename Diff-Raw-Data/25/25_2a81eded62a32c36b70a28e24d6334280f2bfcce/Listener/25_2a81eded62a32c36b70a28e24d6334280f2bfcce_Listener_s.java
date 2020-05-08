 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package co.knights.games.bubblepopper;
 
 import javafx.event.EventHandler;
 import javafx.scene.input.KeyCode;
 import javafx.scene.input.KeyEvent;
 
 /**
  *
  * @author hope
  */
 public class Listener {
 
     private EventHandler<KeyEvent> keyListener;
 
     public Listener() {
         keyListener = new EventHandler<KeyEvent>() {
             @Override
             public void handle(KeyEvent event) {
                 if (event.getCode() == KeyCode.ESCAPE) {
                     System.exit(0);
                 }
             }
         };
     }
 
     public EventHandler<KeyEvent> getKeyListener() {
         return keyListener;
     }
 }
