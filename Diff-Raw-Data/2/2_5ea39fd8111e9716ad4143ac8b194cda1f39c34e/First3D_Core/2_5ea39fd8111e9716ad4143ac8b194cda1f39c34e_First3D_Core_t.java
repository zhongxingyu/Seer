 import java.awt.Point;
 import java.nio.FloatBuffer;
 
 import com.badlogic.gdx.graphics.GL11;
 import com.badlogic.gdx.utils.BufferUtils;
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.InputProcessor;
 
 
 public class First3D_Core implements ApplicationListener, InputProcessor
 {
 	private Point GetPlayerLocation(){
 		Point result = new Point();
 		result.x = (int)(cam.eye.x+2)/5;
 		result.y = (int)(cam.eye.z+2)/5;
 		return result;
 	}
 	
 	private Cell[][] FMaze = Cell.ExampleWalls(); // TODO remove example
 	
 	Camera cam;
 	private boolean ligthBulbState = true;
 	private boolean wiggleLights = false;
 	private float wiggleValue = 0f;
 	private float count = 0;
 		
 	@Override
 	public void create() {
 		
 		Gdx.input.setInputProcessor(this);
 		
 		Gdx.gl11.glEnable(GL11.GL_LIGHTING);
 		
 		Gdx.gl11.glEnable(GL11.GL_LIGHT1);
 		Gdx.gl11.glEnable(GL11.GL_DEPTH_TEST);
 		
 		Gdx.gl11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
 
 		Gdx.gl11.glMatrixMode(GL11.GL_PROJECTION);
 		Gdx.gl11.glLoadIdentity();
 		Gdx.glu.gluPerspective(Gdx.gl11, 90, 1.333333f, 1.0f, 30.0f);
 
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
 
 		/*
 		if(Gdx.input.isKeyPressed(Input.Keys.UP)) 
 			cam.pitch(-90.0f * deltaTime);
 		
 		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) 
 			cam.pitch(90.0f * deltaTime);
 		*/
 		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) 
 			cam.yaw(-90.0f * deltaTime);
 		
 		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) 
 			cam.yaw(90.0f * deltaTime);
 		
 		if(Gdx.input.isKeyPressed(Input.Keys.W)){
 			cam.slide(0.0f, 0.0f, -10.0f * deltaTime);
 			
 			if (cam.eye.z<0 || cam.eye.z>(FMaze[0].length-2)*5 ||
 				cam.eye.x<0 || cam.eye.x>(FMaze.length-2)*5){
 				cam.slide(0, 0, 10.0f * deltaTime);
 			}
 		}
 
 		if(Gdx.input.isKeyPressed(Input.Keys.S)){
 			cam.slide(0.0f, 0.0f, 10.0f * deltaTime);
 			
 			if (cam.eye.z<0 || cam.eye.z>(FMaze[0].length-2)*5 ||
 				cam.eye.x<0 || cam.eye.x>(FMaze.length-2)*5){
 				cam.slide(0, 0, -10.0f * deltaTime);
 			}
 		}
 		
 		if(Gdx.input.isKeyPressed(Input.Keys.A)){
 			cam.slide(-10.0f * deltaTime, 0.0f, 0.0f);
 			
 			if (cam.eye.z<0 || cam.eye.z>(FMaze[0].length-2)*5 ||
 				cam.eye.x<0 || cam.eye.x>(FMaze.length-2)*5){
 				cam.slide(10.0f * deltaTime, 0.0f, 0.0f);
 			}
 		}
 		
 		if(Gdx.input.isKeyPressed(Input.Keys.D)){
 			cam.slide(10.0f * deltaTime, 0.0f, 0.0f);
 			if (cam.eye.z<0 || cam.eye.z>(FMaze[0].length-2)*5 ||
 				cam.eye.x<0 || cam.eye.x>(FMaze.length-2)*5){
 				cam.slide(-10.0f * deltaTime, 0.0f, 0.0f);
 			}
 		}
 		
 		// TODO collision
 		// old position, new position
 		// does this line intersect any wall
 		// TODO retrace or go as far as possible
 		
 		/*
 		if(Gdx.input.isKeyPressed(Input.Keys.R)) 
 			cam.slide(0.0f, 10.0f * deltaTime, 0.0f);
 		
 		if(Gdx.input.isKeyPressed(Input.Keys.F)) 
 			cam.slide(0.0f, -10.0f * deltaTime, 0.0f);
 		*/
 		
 		/*System.out.println();
 		System.out.println("n: "+cam.n);
 		System.out.println("u: "+cam.u);*/
 		//System.out.println("v: "+cam.v); // always same
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
 	
 	private void drawFloor(int AStartX, int AStartY, int AEndX, int AEndY) {
 		// set material for the floor
 		float[] materialDiffuse = {0.2f, 0.3f, 0.6f, 1.0f};
 		Gdx.gl11.glMaterialfv(GL11.GL_FRONT, GL11.GL_DIFFUSE, materialDiffuse, 0);
 		
 		// draw floor
 		for (int fx=AStartX; fx<AEndX; fx++){
 			for (int fz=AStartY; fz<AEndY; fz++){
 				Gdx.gl11.glPushMatrix();				
 				Gdx.gl11.glTranslatef(fx*5, 1.0f, fz*5);
 				Gdx.gl11.glScalef(0.95f*5, 0.95f, 0.95f*5);
 				drawBox();
 				Gdx.gl11.glPopMatrix();
 			}
 		}
 	}
 	
 	private void drawWalls(int AStartX, int AStartY, int AEndX, int AEndY){
 		// set material for the walls
 		float[] materialDiffuse = {0.2f, 0.8f, 0.6f, 1.0f};
 		Gdx.gl11.glMaterialfv(GL11.GL_FRONT, GL11.GL_DIFFUSE, materialDiffuse, 0);
 
 		// draw walls		
 		for (int i=AStartX; i<AEndX; i++){
 			for (int j=AStartY; j<AEndY; j++){
 				if (FMaze[i][j].WestWall()){
 					Gdx.gl11.glPushMatrix();
 					Gdx.gl11.glTranslatef(i*5, 6.0f, j*5-2.5f);
 					Gdx.gl11.glScalef(5.25f, 10.0f, 0.25f);
 					drawBox();
 					Gdx.gl11.glPopMatrix();
 				} else {
 					Gdx.gl11.glPushMatrix();
 					Gdx.gl11.glTranslatef(i*5, 1.0f, j*5-2.5f);
 					Gdx.gl11.glScalef(5.25f, 0.95f, 0.25f);
 					drawBox();
 					Gdx.gl11.glPopMatrix();
 				}
 				if (FMaze[i][j].SouthWall()){
 					Gdx.gl11.glPushMatrix();
 					Gdx.gl11.glTranslatef(i*5-2.5f, 6.0f, j*5);
 					Gdx.gl11.glScalef(0.25f, 10.0f, 5.25f);
 					drawBox();
 					Gdx.gl11.glPopMatrix();
 				} else {
 					Gdx.gl11.glPushMatrix();
 					Gdx.gl11.glTranslatef(i*5-2.5f, 1.0f, j*5);
 					Gdx.gl11.glScalef(0.25f, 0.95f, 5.25f);
 					drawBox();
 					Gdx.gl11.glPopMatrix();
 				}
 			}
 		}
 	}
 	
 	private void display() {
 		Gdx.gl11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
 		cam.setModelViewMatrix();
 				
 		// Configure light 0
 		float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
 		Gdx.gl11.glLightfv(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, lightDiffuse, 0);
 
 		//float[] lightPosition = {this.wiggleValue, 10.0f, 15.0f, 1.0f};
 		float[] lightPosition = {cam.eye.x, cam.eye.y+2, cam.eye.z, 1.0f};
 		Gdx.gl11.glLightfv(GL11.GL_LIGHT0, GL11.GL_POSITION, lightPosition, 0);
 
 		// Configure light 1
 		float[] lightDiffuse1 = {0.5f, 0.5f, 0.5f, 1.0f};
 		Gdx.gl11.glLightfv(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, lightDiffuse1, 0);
 
 		float[] lightPosition1 = {-5.0f, -10.0f, -15.0f, 1.0f};
 		//float[] lightPosition1 = {cam.eye.x, cam.eye.y, cam.eye.z, 1.0f};
 		Gdx.gl11.glLightfv(GL11.GL_LIGHT1, GL11.GL_POSITION, lightPosition1, 0);
 		
 		// Draw objects!
 		// limit view to area around the player 20X20
 		Point player = GetPlayerLocation();
 		int x_start = Math.max(player.x-10, 0);
 		int y_start = Math.max(player.y-10, 0);
 		int x_end = Math.min(player.x+10, FMaze.length-1);
		int y_end = Math.min(player.y+10, FMaze[0].length-1);
 		
 		drawFloor(x_start, y_start, x_end, y_end);
 		drawWalls(x_start, y_start, x_end, y_end);
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
 	public boolean mouseMoved(int arg0, int arg1) {
 		// TODO Auto-generated method stub
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
 }
