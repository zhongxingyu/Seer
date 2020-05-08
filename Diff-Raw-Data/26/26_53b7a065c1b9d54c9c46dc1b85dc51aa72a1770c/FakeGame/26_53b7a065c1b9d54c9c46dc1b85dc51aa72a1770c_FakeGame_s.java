 package ru.knk.JavaGL;
 
 import android.opengl.GLES20;
 import android.util.Log;
 import ru.knk.JavaGL.Interfaces.GameInterface;
 import ru.knk.JavaGL.Interfaces.GraphicsInterface;
 
 public class FakeGame implements GameInterface {
     private GraphicsInterface g;
     private long lastTime;
 
     private FPSCounter fpsCounter = new FPSCounter(5000, "JavaGL", "Game fps: ");
 
     float ballX = 0.0f, ballY = 0.0f;
 
     public FakeGame(GraphicsInterface g) {
         this.g = g;
         lastTime = System.currentTimeMillis();
     }
 
     @Override
     public void tick() {
         final long now = System.currentTimeMillis();
         final float el_time = (now - lastTime) / 1000.0f;
 
         ballX += el_time * 0.3f;
         ballX = ballX - (float)Math.floor(ballX);
 
         ballY = 0.5f;
         lastTime = now;
 
         g.updateBallLocal(ballX, ballY);
 
         fpsCounter.update();
     }
 
     @Override
     public void updatePlayerPaddleLocal(int paddleId, float x, float y) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public float getXMins() {
         return 0.0f;
     }
 
     @Override
     public float getXMaxs() {
         return 1.0f;
     }
 
     @Override
     public float getYMins() {
         return 0.0f;
     }
 
     @Override
     public float getYMaxs() {
         return 1.0f;
     }
 
     @Override
     public float getBallRadius() {
        return 0.05f;
     }
 }
