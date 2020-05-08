 package com.hertas.bowling;
 
 import junit.framework.Assert;
 import org.junit.Test;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Utilistateur
  * Date: 23/07/13
  * Time: 10:25
  */
 public class BowlingGameTest {
 
     @Test
    public void frame1_score_should_equals_5() {
         BowlingGame bowlingGame1 = buildBowlingGame();
         bowlingGame1.play();
        Assert.assertEquals(5,bowlingGame1.getFrame(1).getScore());
     }
 
     private BowlingGame buildBowlingGame() {
         return new BowlingGame();
     }
 }
