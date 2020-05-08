 package com.roscopeco.scratch.runtime;
 
 public abstract class AbstractSprite extends MediaScriptable {
   final AbstractStage stage;
   double rotationD;    /* degrees */
   double rotationR;    /* radians */
   int x, y;
   int w, h;
   Costume costume;
   boolean visible = true;
   
   protected AbstractSprite(AbstractStage owner) {
     this.stage = owner;
     setHeading(90); 
   }
   
   public AbstractStage stage() {
     return stage;    
   }
   
   public int x() {
     return x;
   }
   
   public void setX(int x) {
     this.x = x;
   }
   
   public int y() {
     return y;
   }
   
   public void setY(int y) {
     this.y = y;
   }
   
   public int w() {
     return w;
   }
   
   public void setW(int w) {
     this.w = w;
   }
   
   public int h() {
     return h;
   }
   
   public void setH(int h) {
     this.h = h;
   }
   
   public void setXY(int x, int y) {
     this.x = x;
     this.y = y;
   }
   
   public void setWH(int w, int h) {
     this.w = w;
     this.h = h;
   }
   
   public void forward(int px) {
     double diry = Math.cos(rotationR);
     double dirx = Math.sin(rotationR);
     x = (int)(x + dirx * px);
    y = (int)(y + diry  * px);      
   }
   
   public void startGlideTo(int x, int y, long millis) {
     
   }
   
   public double heading() {
     return rotationD;
   }
   
   public void setHeading(double degrees) {
     rotationD = degrees;
     rotationR = Math.toRadians(degrees);
   }
   
   public void turnLeft(double degrees) {
     rotationD = degrees = rotationD + degrees;
     rotationR = Math.toRadians(degrees);
   }
   
   public void turnRight(double degrees) {
     rotationD = degrees = rotationD + degrees;
     rotationR = Math.toRadians(degrees);
   }
   
   /**
    * Turn 180 degrees if at edge...
    */
   public void bounceOffEdge() {
     // TODO implement    
   }
   
   public void show() {
     visible = true;
   }
   
   public void hide() {
     visible = false;
   }
   
   public boolean visible() {
     return visible;
   }
   
   public Costume costume() {
     return costume;
   }
   
   public void setCostume(Costume costume) {
     this.costume = costume;
   }
   
   public abstract void nextCostume();
   
   public void think(String text, long millis) {
     // TODO implement
   }
   
   public boolean touching(AbstractSprite other) {
     // TODO implement
     return false;
   }
 
   @Override
   public void showVariable(String name) {
     stage.showVariable(name);
   }
 
   @Override
   public void hideVariable(String name) {
     stage.hideVariable(name);
   }
 }
