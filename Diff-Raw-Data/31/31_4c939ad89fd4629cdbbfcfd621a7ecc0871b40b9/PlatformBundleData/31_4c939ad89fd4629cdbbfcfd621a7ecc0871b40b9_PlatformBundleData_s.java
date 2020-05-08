 package com.twam.boostrunner.data;
 
 import com.twam.boostrunner.entity.StaticObstacle;
 
 /**
  * Created by parsia on 7/15/13.
  */
 public class PlatformBundleData {
     //============================================================
     // fields
     //============================================================
 
     private int numberOfPlatform;
     private int numberOfStaticObstacle;
 
     private float[] staticObstacleX;
     private float[] staticObstacleY;
 
     private int difficulties;
 
     //============================================================
     // constructors
     //============================================================
 
    public PlatformBundleData() {
         this.staticObstacleX = new float[numberOfStaticObstacle];
         this.staticObstacleY = new float[numberOfStaticObstacle];
     }
 
     //============================================================
     // getters & setters
     //============================================================
 
     public int getNumberOfPlatform() {
         return numberOfPlatform;
     }
 
     public void setNumberOfPlatform(int numberOfPlatform) {
         this.numberOfPlatform = numberOfPlatform;
     }
 
     public int getNumberOfStaticObstacle() {
         return numberOfStaticObstacle;
     }
 
     public void setNumberOfStaticObstacle(int numberOfStaticObstacle) {
         this.numberOfStaticObstacle = numberOfStaticObstacle;
     }
 
     public float[] getStaticObstacleX() {
         return staticObstacleX;
     }
 
     public void setStaticObstacleX(float[] staticObstacleX) {
         this.staticObstacleX = staticObstacleX;
     }
 
     public float[] getStaticObstacleY() {
         return staticObstacleY;
     }
 
     public void setStaticObstacleY(float[] staticObstacleY) {
         this.staticObstacleY = staticObstacleY;
     }
 }
