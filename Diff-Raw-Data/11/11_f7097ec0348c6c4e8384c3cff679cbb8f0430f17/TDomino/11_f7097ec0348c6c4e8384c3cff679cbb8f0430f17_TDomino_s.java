 package com.rhoward;
 
import java.util.List;

 public class TDomino extends Domino {
 
 
     public TDomino(Block.BlockType type, int x, int y) {
         super(type, x, y);
        this.normalShape = "111,010,000";
        this.rightShape = "100,110,100";
        this.downShape = "000,010,111";
        this.leftShape = "001,011,001";
 
     }
 
     @Override
     public Domino rotate() {
         TDomino newState = new TDomino(this.type, this.center.getX(), this.center.getY());
         newState.rotateState = this.rotateState;
         newState.rotateState = newState.nextRotation();
 
         return newState;
     }
 
     @Override
     public Domino translate(int x, int y) {
         TDomino translated = new TDomino(this.type, this.getX() + x, this.getY() + y);
         translated.rotateState = this.rotateState;
         return translated;
     }
 
 }
