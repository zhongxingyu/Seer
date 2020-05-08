 package jp.angelforest.arrow.particlesystem;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.Context;
 import android.view.MotionEvent;
 import jp.arrow.angelforest.engine.core.AngelforestRenderer;
 
 public class ParticleSystemRenderer extends AngelforestRenderer {
     private Particle particle;
 
     public ParticleSystemRenderer(Context context) {
         super(context);
 
         particle = new Particle(4);
		particle.setX(40);
		particle.setY(40);
         particle.setGoal(100, 100, 400);
     }
 
     @Override
     public void initTextures(GL10 gl) {
     }
 
     @Override
     public void draw(GL10 gl) {
         particle.draw(gl);
         particle.move();
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         return false;
     }
 
 }
