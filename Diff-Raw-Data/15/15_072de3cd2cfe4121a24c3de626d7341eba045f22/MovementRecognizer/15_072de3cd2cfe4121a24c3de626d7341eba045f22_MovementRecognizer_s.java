 package io.metacake.game;
 
 import io.metacake.core.common.MilliTimer;
 import io.metacake.engine.KeyboardRecognizer;
 
 import java.awt.event.KeyEvent;
 
 /**
  * @author florence
  * @author rpless
  */
 class MovementRecognizer implements KeyboardRecognizer {
 
     final int positive;
     final int negative;
     KeyHeldRecognizer pos = new KeyHeldRecognizer();
     KeyHeldRecognizer neg = new KeyHeldRecognizer();
 
     MovementRecognizer(int positive, int negative) {
         this.positive = positive;
         this.negative = negative;
     }
 
     @Override
     public boolean wasTriggered() {
         return pos.wasTriggered() || neg.wasTriggered();
     }
 
     @Override
     /**
      * Also resets the amount
      */
     public int triggerWeight() {
         int res = pos.triggerWeight() - neg.triggerWeight();
        System.out.println(res);
         return res;
     }
 
 
     @Override
     public void forgetActions() {
        pos.forgetActions();
        neg.forgetActions();
     }
 
     @Override
     public void keyPressed(KeyEvent e) {
         if(e.getKeyCode() == positive) {
             pos.keyPressed(e);
         } else if (e.getKeyCode() == negative) {
             neg.keyPressed(e);
         }
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
         if(e.getKeyCode() == positive) {
             pos.keyReleased(e);
         } else if (e.getKeyCode() == negative) {
             neg.keyReleased(e);
         }
     }
 }
 
 class KeyHeldRecognizer implements KeyboardRecognizer {
 
     MilliTimer counter = new MilliTimer();
     boolean isHeld;
     boolean wasTriggered;
     int savedAmount = 0;
     private final int timerFactor = 10;
 
     @Override
     public void keyPressed(KeyEvent e) {
         if(!isHeld){
             counter.update();
             wasTriggered = true;
             isHeld = true;
         }
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
         savedAmount += currentTimeAmount();
         isHeld = false;
     }
 
     @Override
     public boolean wasTriggered() {
         return wasTriggered;
     }
 
     @Override
     public int triggerWeight() {
         int res = savedAmount + (isHeld?currentTimeAmount():0);
         savedAmount = 0;
         if(!isHeld) {
             wasTriggered = false;
         }
         return res;
     }
 
     @Override
     public void forgetActions() {
         isHeld = false;
         wasTriggered = false;
         savedAmount = 0;
         counter.update();
     }
 
     private int currentTimeAmount(){
         return (int) counter.update()/timerFactor;
     }
 }
