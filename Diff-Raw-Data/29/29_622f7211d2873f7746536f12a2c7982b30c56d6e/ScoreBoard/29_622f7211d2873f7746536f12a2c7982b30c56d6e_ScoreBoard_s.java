 package com.psywerx.dh;
 
 import android.opengl.GLES20;
 import android.opengl.Matrix;
 
 public class ScoreBoard extends PersonSprite {
     
     private Text scoreText;
     private int score;
     
     
     public ScoreBoard(){
         s = new Square();
         s.size = new float[]{0.07f, 0.05f, 1};
         s.color = new float[]{0,0,0,1};
         s.position = new float[]{-0.8f, 0.9f, -0.5f};
         s.texture.enabled = true;
         s.texture.sprite = new int[]{9,3};
         s.texture.startSprite = new int[]{9,3};
         s.texture.size = new int[]{2,2};
         s.texture.anim = new int[]{0,};
 
         s.texture.animSpeed = 1f;
         score = 0;
         scoreText = new Text(String.format("%07d", score));
        scoreText.setSize(new float[]{0.05f, 0.05f});
         scoreText.move(new float[]{0.5f, 0.9f, -0.5f});
     }
     
     public void increaseScore(int amount){
         score += amount;
         scoreText.update(String.format("%07d", score));
     }
     
     public void draw() {
         float[] id = new float[16];
         Matrix.setIdentityM(id, 0);
         GLES20.glUniformMatrix4fv(Game.program.projectionMatrixLoc, 1, false,
                 id, 0);
         s.draw();
         scoreText.draw();
         GLES20.glUniformMatrix4fv(Game.program.projectionMatrixLoc, 1, false,
                 Game.projection, 0);
     };
 
     public void tick(float theta) {
         s.texture.update(theta);
         
     };
 
 }
