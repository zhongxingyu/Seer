 package com.tgra;
 
 import java.nio.FloatBuffer;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL11;
 
 public class MazeBase {
 	private float x;
 	private float y;
 	private float z;
 	
 	public MazeBase(float x, float y, float z) {
 		this.x = x;
 		this.y = y;
 		this.z = z;
 	}
 	private void drawSurface() {
 		Gdx.gl11.glPushMatrix();
 		float[] materialDiffuse = {5.2f, 1.3f, 0.0f, 0.0f};
 		Gdx.gl11.glMaterialfv(GL11.GL_FRONT, GL11.GL_AMBIENT, materialDiffuse, 0);
 		Gdx.gl11.glTranslatef(this.x,this.y,this.z);
 		Gdx.gl11.glNormal3f(0.0f, 0.0f, -1.0f);
 		Gdx.gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
 		Gdx.gl11.glPopMatrix();
 
 	}
 	private void drawWalls() {
 		Gdx.gl11.glPushMatrix();
 		Gdx.gl11.glScalef(1f, 0.03f, 1f);
		Gdx.gl11.glTranslatef(99.99f,75f, 0.0f);
 		Gdx.gl11.glRotatef(90, 0,0, 1);
 		float[] materialDiffuse = {0.2f, 7.3f, 0.0f, 0.0f};
 		Gdx.gl11.glMaterialfv(GL11.GL_FRONT, GL11.GL_AMBIENT, materialDiffuse, 0);
 		Gdx.gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
 		for(int i=0;i<4;i++) {
 			Gdx.gl11.glRotatef(90,1,0, 0);
 			Gdx.gl11.glTranslatef(0.0f,-100f, -100.0f);
 	        Gdx.gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
 		}
 		Gdx.gl11.glPopMatrix();
 	}
 	public void display() {
 		Gdx.gl11.glEnable(GL11.GL_LIGHT0);
        this.drawSurface();
        this.drawWalls();
 	}
 	public void preventCollision(Camera camera) {
 		if(camera.eye.z <= this.z-100+2)
 		   camera.eye.z = (float) (Math.round(camera.eye.z)+0.5);
 		if(camera.eye.z >= this.z+100-2)
 			camera.eye.z = (float) (Math.round(camera.eye.z)-0.5);
 		if(camera.eye.x <= this.x-100+2)
 			   camera.eye.x = (float) (Math.round(camera.eye.x)+0.5);
 		if(camera.eye.x >= this.x+100-2)
 			camera.eye.x = (float) (Math.round(camera.eye.x)-0.5);
 	}
 }
