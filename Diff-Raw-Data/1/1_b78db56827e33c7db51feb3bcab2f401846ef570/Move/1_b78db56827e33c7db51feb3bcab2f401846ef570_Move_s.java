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
 
    private Direction direction;
 
    private Boolean isAIMove;
 
    public Move(Set<Ball> initialBalls) {
       this.initialBalls = new HashSet<Ball>();
       this.finalBalls = new HashSet<Ball>();
       for (Ball b : initialBalls) {
          this.initialBalls.add(new Ball(b));
       }
       this.isAIMove = false;
    }
 
    public Move() {
       this.isAIMove = true;
    }
 
    public Boolean isAIMove() {
       return isAIMove;
    }
 
    public void setInitialState(Set<Ball> initialBalls) {
       for (Ball b : initialBalls) {
          this.initialBalls.add(new Ball(b));
       }
    }
 
    public void setFinalState(Set<Ball> finalBalls) {
       for (Ball b : finalBalls) {
          this.finalBalls.add(new Ball(b));
       }
    }
 
    public Set<Ball> getFinalBalls() {
       return finalBalls;
    }
 
    public Set<Ball> getInitialBalls() {
       return initialBalls;
    }
 
    public Direction getDirection() {
       return direction;
    }
 
    public void setDirection(Direction direction) {
       this.direction = direction;
    }
 
 }
