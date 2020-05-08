 package com.pixelthieves.core.input;
 
 import com.badlogic.gdx.input.GestureDetector;
 import com.pixelthieves.core.graphics.camera.CameraHandler;
 
 public class GestureProcessor extends GestureDetector.GestureAdapter {
 
     protected final CameraHandler camera;
 
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
        camera.zoom((distance - initialDistance) /
                Math.max(camera.getCamera().viewportWidth, camera.getCamera().viewportHeight));
         return true;
     }
 
 }
