 package org.gameflow.utils;
 
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 
 /**
  * Caches the image region with a specific name, as well as the size and other parameters on how to render it.
  */
 public class ImageRef {
 
     public static final int NO_INDEX = -1;
 
     private String imageName = null;
     private int imageIndex = NO_INDEX;
     private double scaleX = 1;
     private double scaleY = 1;
     private double angleTurns = 0;
     private double turnPointX = 0.5;
     private double turnPointY = 0.5;
     private boolean mirrorX = false;
     private boolean mirrorY = false;
 
     private transient TextureAtlas.AtlasRegion image = null;
 
     public ImageRef(String imageName) {
         this.imageName = imageName;
     }
 
     public ImageRef(String imageName, int imageIndex) {
         this.imageName = imageName;
         this.imageIndex = imageIndex;
     }
 
     public ImageRef(String imageName, int imageIndex, double scaleX, double scaleY) {
         this.imageName = imageName;
         this.imageIndex = imageIndex;
         this.scaleX = scaleX;
         this.scaleY = scaleY;
     }
 
     public ImageRef(String imageName, int imageIndex, double scaleX, double scaleY, double angleTurns) {
         this.imageName = imageName;
         this.imageIndex = imageIndex;
         this.scaleX = scaleX;
         this.scaleY = scaleY;
         this.angleTurns = angleTurns;
     }
 
     public ImageRef(String imageName, int imageIndex, double scaleX, double scaleY, double angleTurns, double turnPointX, double turnPointY) {
         this.imageName = imageName;
         this.imageIndex = imageIndex;
         this.scaleX = scaleX;
         this.scaleY = scaleY;
         this.angleTurns = angleTurns;
         this.turnPointX = turnPointX;
         this.turnPointY = turnPointY;
     }
 
     public String getImageName() {
         return imageName;
     }
 
     public void setImageName(String imageName) {
         this.imageName = imageName;
         image = null;
     }
 
     public void setImageIndex(int imageIndex) {
         this.imageIndex = imageIndex;
         image = null;
     }
 
     public void setImage(String imageName, int imageIndex) {
         this.imageName = imageName;
         this.imageIndex = imageIndex;
         image = null;
     }
 
     public void setScale(double scale) {
         this.scaleX = scale;
         this.scaleY = scale;
     }
 
     public double getScaleX() {
         return scaleX;
     }
 
     public void setScaleX(double scaleX) {
         this.scaleX = scaleX;
     }
 
     public double getScaleY() {
         return scaleY;
     }
 
     public void setScaleY(double scaleY) {
         this.scaleY = scaleY;
     }
 
     public double getAngleTurns() {
         return angleTurns;
     }
 
     public void setAngleTurns(double angleTurns) {
         this.angleTurns = angleTurns;
     }
 
     public boolean isMirrorX() {
         return mirrorX;
     }
 
     public void setMirrorX(boolean mirrorX) {
         this.mirrorX = mirrorX;
     }
 
     public boolean isMirrorY() {
         return mirrorY;
     }
 
     public void setMirrorY(boolean mirrorY) {
         this.mirrorY = mirrorY;
     }
 
     /**
      * The point around with the image should be rotated.
      * 0 = left / bottom edge, 1 = right upper edge.
      */
     public void setTurnPoint(double turnPointX, double turnPointY) {
         this.turnPointX = turnPointX;
         this.turnPointY = turnPointY;
     }
 
     public double getTurnPointX() {
         return turnPointX;
     }
 
     public double getTurnPointY() {
         return turnPointY;
     }
 
     public void render(float x, float y, TextureAtlas atlas, SpriteBatch spriteBatch) {
         if (imageName != null) {
             if (image == null) image = atlas.findRegion(imageName, imageIndex);
 
            float w = image.getRegionWidth();
            float h = image.getRegionHeight();
            float originY = (float) (h * turnPointY);
            float originX = (float) (w * turnPointX);
            spriteBatch.draw(
                    image.getTexture(),
                    x, y,
                    originX, originY,
                    w, h,
                    (float) scaleX, (float) scaleY,
                    MathTools.toDegrees(angleTurns * MathTools.Tau),
                    image.getRegionX(), image.getRegionY(), image.getRegionWidth(), image.getRegionHeight(),
                    isMirrorX(), isMirrorY());
         }
     }
 
 }
