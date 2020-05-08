 package com.github.abalone.util;
 
 import com.github.abalone.elements.Ball;
import java.io.Serializable;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  *
  * @author keruspe
  */
public class Move implements Serializable {
 
    private Set<Ball> initialBalls;
 
    private Set<Ball> finalBalls;
 
    public Move(Set<Ball> initialBalls) {
       this.initialBalls = new HashSet<Ball>();
       this.finalBalls = new HashSet<Ball>();
       for (Ball b : initialBalls) {
          this.initialBalls.add(new Ball(b));
       }
    }
 
    public void setFinalState(Set<Ball> finalBalls) {
       for (Ball b : finalBalls) {
          this.finalBalls.add(new Ball(b));
       }
    }
 }
