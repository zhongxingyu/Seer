 package test;
 
 import processing.core.PApplet;
 import com.stewsters.graphic.Animation;
 
 public class AnimationTest extends PApplet {
 
     Animation animation;
     @Override
     public void setup() {
         size(200,200);
         animation = new Animation(this,"character/walk");
     }
 
     @Override
     public void draw(){
 
         animation.display(this,10,10);
     }
 
 
 }
