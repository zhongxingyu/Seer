 package com.rhoward;
 
 public class TDomino extends Domino {
 
 
     public TDomino(Block.BlockType type, int x, int y) {
         super(type, x, y);
        this.normalShape = "000,111,010";
        this.leftShape = "010,011,010";
        this.downShape = "010,111,000";
        this.rightShape = "010,110,010";
 
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
