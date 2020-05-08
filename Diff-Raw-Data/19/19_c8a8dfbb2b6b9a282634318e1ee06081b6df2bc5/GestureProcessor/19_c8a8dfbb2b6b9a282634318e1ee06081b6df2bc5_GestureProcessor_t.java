 package com.pixelthieves.core.input;
 
 import com.badlogic.gdx.input.GestureDetector;
 import com.pixelthieves.core.graphics.camera.CameraHandler;
 
 public class GestureProcessor extends GestureDetector.GestureAdapter {
 
     protected final CameraHandler camera;
     private float initialDistance = 0;
 
     public GestureProcessor(CameraHandler camera) {
         this.camera = camera;
     }
 
     @Override
     public boolean pan(float x, float y, float deltaX, float deltaY) {
         camera.move(-deltaX, deltaY);
         return true;
     }
 
     @Override
     public boolean zoom(float initialDistance, float distance) {
         if (this.initialDistance != Float.MIN_VALUE) {
             initialDistance = this.initialDistance;
         }
        camera.zoom((initialDistance - distance) /
                 (Math.max(camera.getCamera().viewportWidth, camera.getCamera().viewportHeight)));
         this.initialDistance = distance;
         return true;
     }
 
     @Override
     public boolean touchDown(float x, float y, int pointer, int button) {
         initialDistance = Float.MIN_VALUE;
         return super.touchDown(x, y, pointer, button);
     }
 }
