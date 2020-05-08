 
 package vooga.scroller.scrollingmanager;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import util.Location;
 import vooga.scroller.model.Model;
 import vooga.scroller.sprites.superclasses.Player;
 import vooga.scroller.util.PlatformerConstants;
 import vooga.scroller.view.View;
 /**
  * This manager prevents players from scrolling in a directing but so far EX like Mario
  * @author Ross
  *
  */
 public class UniScrollingManager extends ScrollingManager {
     private Model myGame;
     private View myView;
     private int myDirection = -1;
     private double myMaxDirection;
 
     public UniScrollingManager(int restrictiondirection){
         if(restrictiondirection != 1 & restrictiondirection != 2 & restrictiondirection != 3 & restrictiondirection != 4) {
             myDirection = 2;
         }
         else {
             myDirection = restrictiondirection;
         }
     }
 
     public void initGame(Model game) {
         myGame = game;
     }
 
     public void initView(View view) {
         myView = view;
     }
 
     public int upperpaintbound() {
         if(myGame != null & myView != null) {
             int vertical = ((int) myGame.getLowerBoundary() + myView.getHeight()*1000) % myView.getHeight();
             return 0 - vertical;
         }
         return 0;
     }
 
     public int lowerpaintbound() { 
         if(myGame != null & myView != null) {
             int vertical = ((int) myGame.getLowerBoundary() + myView.getHeight()*1000) % myView.getHeight();
             return myView.getHeight() - vertical;
         }
         return 0;
 
     }
 
     public int leftpaintbound() {
         if(myGame != null & myView != null) {
             int horizontal = ((int) myGame.getRightBoundary() + myView.getWidth()*1000) % myView.getWidth();
             return 0 - horizontal;
         }
         return 0;
     }
 
     public int rightpaintbound() {
         if(myGame != null & myView != null) {
             int horizontal = ((int) myGame.getRightBoundary() + myView.getWidth()*1000) % myView.getWidth();
             return myView.getWidth() - horizontal;
         }
         return 0;
     }
 
     public double getRightBoundary(Dimension frame, Location center) {
         return (center.getX() + frame.getWidth() / 2);
     }
 
     public double getLeftBoundary(Dimension frame, Location center) {
         return (center.getX() - frame.getWidth() / 2);
     }
 
     public double getUpperBoundary(Dimension frame, Location center) {
         return (center.getY() - frame.getHeight() / 2);
     }
 
     public double getLowerBoundary(Dimension frame, Location center) { 
         return (center.getY() + frame.getHeight() / 2);
     }
 
     public double levelRightBoundary () {
         return myGame.getLevelBounds().getWidth();
     }
 
     public double levelLeftBoundary () {
         return 0;
     }
 
     public double levelUpperBoundary () {
         return 0;
     }
 
     public double levelLowerBoundary () { 
         return myGame.getLevelBounds().getHeight();
     }
 
     private double uniRightBoundary (Player p) {
         if(myDirection == 1){
             if(myMaxDirection > levelRightBoundary()) {
                 return levelRightBoundary();
             }
             return myMaxDirection;
         }
         return levelRightBoundary();
     }
 
     private double uniLeftBoundary (Player p) {
         if(myDirection == 3){
        
             if(myMaxDirection < levelLeftBoundary()){
                 return levelLeftBoundary();
             }
             return myMaxDirection;
         }
         return levelLeftBoundary();
     }
 
     private double uniUpperBoundary (Player p) {
         if(myDirection == 4){
             if(myMaxDirection < levelUpperBoundary()){
                 return levelUpperBoundary();
             }
             return myMaxDirection;
         }
         return levelUpperBoundary();
     }
 
     private double uniLowerBoundary (Player p) { 
         if(myDirection == 2){
             if(myMaxDirection > levelLowerBoundary()){
                 return levelLowerBoundary();
             }
             return myMaxDirection;
         }
         return levelLowerBoundary();
     }
 
     public Image getBackground() {
         return myGame.getBackground();
     }
 
     public void viewPaint(Graphics pen) {
         Image img = getBackground();
         int imgwidth = img.getWidth(null);
         int imgheight = img.getHeight(null);
         int leftpaintbound = leftpaintbound();
         int upperpaintbound = upperpaintbound();
         int rightpaintbound = rightpaintbound();
         int lowerpaintbound = lowerpaintbound();
 
         if(myGame.getLeftBoundary() < levelLeftBoundary()) {
             leftpaintbound = (int) levelLeftBoundary();
             rightpaintbound = (int) levelRightBoundary();
         }
 
         if(myGame.getRightBoundary() > levelRightBoundary()) {
             leftpaintbound =  - ((int) levelRightBoundary() % myGame.getBackground().getWidth(null));
             rightpaintbound = myView.getWidth()  - ((int) levelRightBoundary() % myGame.getBackground().getWidth(null));
 
         }
         if(myGame.getLowerBoundary() > levelLowerBoundary()) {
             upperpaintbound = - ((int) levelLowerBoundary() % myGame.getBackground().getHeight(null));
             lowerpaintbound = myView.getHeight()  - ((int) levelLowerBoundary() % myGame.getBackground().getHeight(null));
         }
         if(myGame.getUpperBoundary() < levelUpperBoundary()) {
             upperpaintbound = (int) levelUpperBoundary();
             lowerpaintbound = (int) levelLowerBoundary();
         }
         pen.drawImage(img, leftpaintbound, upperpaintbound, imgwidth, imgheight, null);
         pen.drawImage(img, rightpaintbound, upperpaintbound, imgwidth, imgheight, null);
         pen.drawImage(img, leftpaintbound, lowerpaintbound, imgwidth, imgheight, null);
         pen.drawImage(img, rightpaintbound, lowerpaintbound, imgwidth, imgheight, null);
         myGame.paint((Graphics2D) pen);
     }
 
     public Location playerPaintLocation (Player p) {
         updateLocation(p);
         double halfwidth = myView.getWidth() / 2;
         double halfheight = myView.getHeight() / 2;
         double x = halfwidth;
         double y = halfheight;
         double playerlocx = p.getX();
         double playerlocy = p.getY();
         if(playerlocx > (uniRightBoundary(p) - halfwidth)) {
             x =  halfwidth + (halfwidth - (Math.abs(uniRightBoundary(p) - playerlocx)));
         }
 
         if(playerlocx < (uniLeftBoundary(p) + halfwidth)) {
             x =  halfwidth - (halfwidth - (Math.abs(uniLeftBoundary(p) - playerlocx)));
         }
         if(playerlocy > (uniLowerBoundary(p) - halfheight)) {
             y =  halfheight + (halfheight - (Math.abs(uniLowerBoundary(p) - playerlocy)));
         }
         if(playerlocy < (uniUpperBoundary(p) + halfheight)) {
             y =  halfheight - (halfheight - (Math.abs(uniUpperBoundary(p) - playerlocy)));
        }        
        System.out.println("Paint: " + x);
         return new Location(x, y);
 
     }
 
     private void updateLocation (Player p) {
         if(myDirection == -1){
             if(myDirection == 1){
                 myMaxDirection = (p.getX() + myView.getWidth() /2);
 
             }
             if(myDirection == 3){
                 myMaxDirection = (p.getX() - myView.getWidth() / 2);
 
             }
             if(myDirection == 2 ){
                 myMaxDirection = (p.getY() + myView.getHeight() / 2);
 
             }
             if(myDirection == 4){
                 myMaxDirection = (p.getY() - myView.getHeight() / 2);
 
             }
         }
         if(myDirection == 1){
             if((p.getX() + myView.getWidth() /2) < myMaxDirection){
                 myMaxDirection = (p.getX() + myView.getWidth() /2);
             }
         }
         if(myDirection == 2){
             if((p.getY() + myView.getHeight() / 2)  < myMaxDirection){
                 myMaxDirection = (p.getY() + myView.getHeight() / 2);
             }
         }
         if(myDirection == 3){
             if((p.getX() - myView.getWidth() / 2) > myMaxDirection){
                 myMaxDirection = (p.getX() - myView.getWidth() / 2);
             }
         }
         if(myDirection == 4){
             if((p.getY() - myView.getHeight() / 2) > myMaxDirection){
                 myMaxDirection = (p.getY() - myView.getHeight() / 2);
             }
         }
     }
 }
