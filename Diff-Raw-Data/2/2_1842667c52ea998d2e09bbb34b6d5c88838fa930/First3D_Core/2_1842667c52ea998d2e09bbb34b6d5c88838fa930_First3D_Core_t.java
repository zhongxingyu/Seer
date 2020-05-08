 package com.tgra;
 import java.nio.FloatBuffer;
 
 import com.badlogic.gdx.graphics.GL11;
 import com.badlogic.gdx.utils.BufferUtils;
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.InputProcessor;
 
 
 public class First3D_Core implements ApplicationListener, InputProcessor
 {
 	Camera cam;
 	private boolean ligthBulbState = true;
 	private boolean wiggleLights = false;
 	private float wiggleValue = 0f;
 	private float count = 0;
 		
 	@Override
 	public void create() {
 		
 		Gdx.input.setInputProcessor(this);
 		
 		Gdx.gl11.glEnable(GL11.GL_LIGHTING);
 		
 		//Gdx.gl11.glEnable(GL11.GL_LIGHT1);
 		Gdx.gl11.glEnable(GL11.GL_DEPTH_TEST);
 		
 		Gdx.gl11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
 
 		Gdx.gl11.glMatrixMode(GL11.GL_PROJECTION);
 		Gdx.gl11.glLoadIdentity();
 		Gdx.glu.gluPerspective(Gdx.gl11, 90, 1.333333f, 1.0f, 100.0f);
 
 		Gdx.gl11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
 
 		FloatBuffer vertexBuffer = BufferUtils.newFloatBuffer(72);
 		vertexBuffer.put(new float[] {-0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f,
 									  0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f,
 									  0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f,
 									  0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
 									  0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
 									  -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f,
 									  -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f,
 									  -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f,
 									  -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f,
 									  0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f,
 									  -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f,
 									  0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f});
 		vertexBuffer.rewind();
 
 		Gdx.gl11.glVertexPointer(3, GL11.GL_FLOAT, 0, vertexBuffer);
 		cam = new Camera(new Point3D(0.0f, 3.0f, 2.0f), new Point3D(2.0f, 3.0f, 3.0f), new Vector3D(0.0f, 1.0f, 0.0f));
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	private void update() {
 		
 		if(this.wiggleLights){
 			count += 0.03;
 			this.wiggleValue = (float) Math.sin(count) * 10;
 		}
 		
 		
 		
 		if(this.ligthBulbState)
 			Gdx.gl11.glEnable(GL11.GL_LIGHT0);
 		else
 			Gdx.gl11.glDisable(GL11.GL_LIGHT0);
 		
 		float deltaTime = Gdx.graphics.getDeltaTime();
 
 		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) 
 			cam.pitch(-90.0f * deltaTime);
 		
 		if(Gdx.input.isKeyPressed(Input.Keys.UP)) 
 			cam.pitch(90.0f * deltaTime);
 		
 		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) 
 			cam.yaw(-90.0f * deltaTime);
 		
 		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) 
 			cam.yaw(90.0f * deltaTime);
 		
 		if(Gdx.input.isKeyPressed(Input.Keys.W)) 
 			cam.slide(0.0f, 0.0f, -10.0f * deltaTime);
 		
 		if(Gdx.input.isKeyPressed(Input.Keys.S)) 
 			cam.slide(0.0f, 0.0f, 10.0f * deltaTime);
 		
 		if(Gdx.input.isKeyPressed(Input.Keys.A)) 
 			cam.slide(-10.0f * deltaTime, 0.0f, 0.0f);
 		
 		if(Gdx.input.isKeyPressed(Input.Keys.D)) 
 			cam.slide(10.0f * deltaTime, 0.0f, 0.0f);
 		
 		if(Gdx.input.isKeyPressed(Input.Keys.R)) 
 			cam.slide(0.0f, 10.0f * deltaTime, 0.0f);
 		
 		if(Gdx.input.isKeyPressed(Input.Keys.F)) 
 			cam.slide(0.0f, -10.0f * deltaTime, 0.0f);
 	}
 	
 	private void drawBox() {
 		Gdx.gl11.glNormal3f(0.0f, 0.0f, -1.0f);
 		Gdx.gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
 		Gdx.gl11.glNormal3f(1.0f, 0.0f, 0.0f);
 		Gdx.gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 4, 4);
 		Gdx.gl11.glNormal3f(0.0f, 0.0f, 1.0f);
 		Gdx.gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 8, 4);
 		Gdx.gl11.glNormal3f(-1.0f, 0.0f, 0.0f);
 		Gdx.gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 12, 4);
 		Gdx.gl11.glNormal3f(0.0f, 1.0f, 0.0f);
 		Gdx.gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 16, 4);
 		Gdx.gl11.glNormal3f(0.0f, -1.0f, 0.0f);
 		Gdx.gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 20, 4);
 	}
 	
 	private void drawFloor(float size) {
 		for(float fx = 0.0f; fx < size; fx += 1.0) {
 			for(float fz = 0.0f; fz < size; fz += 1.0) {
 				Gdx.gl11.glPushMatrix();
 				Gdx.gl11.glTranslatef(fx, 1.0f, fz);
 				Gdx.gl11.glScalef(0.95f, 0.95f, 0.95f);
 				drawBox();
 				Gdx.gl11.glPopMatrix();
 			}
 		}
 	}
 	
 	private void display() {
 		Gdx.gl11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
 		cam.setModelViewMatrix();
 				
 		// Configure light 0
		float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
 		Gdx.gl11.glLightfv(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, lightDiffuse, 0);
 
 		float[] lightPosition = {this.wiggleValue, 10.0f, 15.0f, 1.0f};
 		Gdx.gl11.glLightfv(GL11.GL_LIGHT0, GL11.GL_POSITION, lightPosition, 0);
 
 		// Configure light 1
 		float[] lightDiffuse1 = {0.5f, 0.5f, 0.5f, 1.0f};
 		Gdx.gl11.glLightfv(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, lightDiffuse1, 0);
 
 		float[] lightPosition1 = {-5.0f, -10.0f, -15.0f, 1.0f};
 		Gdx.gl11.glLightfv(GL11.GL_LIGHT1, GL11.GL_POSITION, lightPosition1, 0);
 		
 		// Set material on the cube.
 		float[] materialDiffuse = {0.2f, .3f, 0.6f, 1.0f};
 		Gdx.gl11.glMaterialfv(GL11.GL_FRONT, GL11.GL_DIFFUSE, materialDiffuse, 0);
 
 		// Draw floor!
 		drawFloor(50);
 	}
 
 	@Override
 	public void render() {
 		update();
 		display();
 	}
 
 	@Override
 	public void resize(int arg0, int arg1) {
 	}
 
 	@Override
 	public void resume() {
 	}
 
 	@Override
 	public boolean keyDown(int arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int arg0) {
 
 		if(arg0 == Input.Keys.L){
 			this.ligthBulbState = this.ligthBulbState ? false:true;
 		}
 		if(arg0 == Input.Keys.O){
 			this.wiggleLights = this.wiggleLights ? false:true;
 		}
 		
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int arg0, int arg1, int arg2, int arg3) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int arg0, int arg1, int arg2) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int arg0, int arg1, int arg2, int arg3) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchMoved(int arg0, int arg1) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
