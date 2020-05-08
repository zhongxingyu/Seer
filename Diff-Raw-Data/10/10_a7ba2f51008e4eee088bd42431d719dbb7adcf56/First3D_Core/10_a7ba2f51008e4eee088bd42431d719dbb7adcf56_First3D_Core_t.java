 package com.tgra;
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
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
 	private float wiggleValue = 1f;
 	private float count = 0;
 	private float speed = 30.0f;
 	private MazeBase mazebase = null;
 	private List<Wall> walls;
 	private List<Spongebob> spongebobs;
 
 	private FloatBuffer vertexBuffer;
 	private InputHandler mazeDesign;
 	private Spongebob human;
 	
 	@Override
 	public void create() {
 		Gdx.gl11.glEnable(GL11.GL_LIGHT0);
 
 		Gdx.gl11.glEnable(GL11.GL_NORMALIZE);
 		Gdx.gl11.glEnable(GL11.GL_SMOOTH);
 		
 
 		this.walls = new ArrayList<Wall>();
 		InputHandler inputhandler = new InputHandler("assets/maze.txt");
         //this.walls = new ArrayList<Wall>();
 		for(int i=0;i<20;i++)
             {
             this.walls.add(new Wall(-1,i,false));
             this.walls.add(new Wall(19,i,false));
             this.walls.add(new Wall(i,-1,true));
             this.walls.add(new Wall(i,19,true));
             }
 		this.walls.addAll(inputhandler.getWalls());
 
 		mazebase = new MazeBase(0,0,0);
 		FloatBuffer mvm = BufferUtils.newFloatBuffer(100);
 		Gdx.gl11.glMatrixMode(GL11.GL_MODELVIEW);
 		Gdx.gl11.glLoadIdentity();
 		Gdx.glu.gluLookAt(Gdx.gl11,8.0f, 5.0f, 7.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
 		Gdx.input.setInputProcessor(this);
 		
 		Gdx.gl11.glEnable(GL11.GL_LIGHTING);
 		Gdx.gl11.glShadeModel(GL11.GL_SMOOTH);
 
 		Gdx.gl11.glEnable(GL11.GL_DEPTH_TEST);
 		
 		Gdx.gl11.glClearColor(0.0f, 0.1f, 0.2f, 1.0f);
 
 		Gdx.gl11.glMatrixMode(GL11.GL_PROJECTION);
 		Gdx.gl11.glLoadIdentity();
 		Gdx.glu.gluPerspective(Gdx.gl11, 90, 1.333333f, 1.0f, 1000.0f);
 
 		Gdx.gl11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
 
 		this.vertexBuffer = BufferUtils.newFloatBuffer(72);
 		vertexBuffer.put(new float[] {
 				-5, 0f, -5f, 
 				-5f, 0f, 5f,
 				5f, 0f, -5f, 
 				5f, 0f, 5f});
 		vertexBuffer.put(new float[] {
 				0f, 0, 0f,  
 				0f, 5f,0f,
 				10f, 0,0f,  
 				10f, 5f,0f });
 		vertexBuffer.rewind();
 
 		Gdx.gl11.glVertexPointer(3, GL11.GL_FLOAT, 0, vertexBuffer);
 		cam = new Camera(new Point3D(-95.0f, 2.5f, -95.0f), new Point3D(3.0f, 2.5f, 10.0f), new Vector3D(0.0f, 1.0f, 0.0f));
         this.spongebobs = inputhandler.getSpongebobs();
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
 			
 			
 		System.out.println(this.cam.eye.x + "," + this.cam.eye.y + "," + this.cam.eye.z);
 		this.mazebase.preventCollision(this.cam);
 
 		float deltaTime = Gdx.graphics.getDeltaTime();
 		//System.out.println(cam.eye.x + " " + cam.eye.y + " " + cam.eye.z);
 
 		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) 
 			cam.yaw(-90.0f * deltaTime);
 		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) 
 			cam.yaw(90.0f * deltaTime);
 		if(Gdx.input.isKeyPressed(Input.Keys.W)) 
 		{
 				cam.slide(0.0f, 0.0f, -this.speed * deltaTime);
 		}
 		if(Gdx.input.isKeyPressed(Input.Keys.S)) 
 			cam.slide(0.0f, 0.0f,this.speed * deltaTime);
 		if(Gdx.input.isKeyPressed(Input.Keys.A)) {
 			
 				cam.slide(-this.speed * deltaTime, 0.0f, 0.0f);
 		}
 		if(Gdx.input.isKeyPressed(Input.Keys.D)) {
 			cam.slide(this.speed * deltaTime, 0.0f, 0.0f);
 		}
 		this.mazebase.preventCollision(this.cam);
 		for(Wall wall: this.walls)
 		{
 			if(Math.abs(rowcol(this.cam.eye.z)-wall.getColumn()) <=1
 			  && Math.abs(rowcol(this.cam.eye.x)-wall.getRow()) <=1) {
 		       wall.preventCollision(this.cam);
 			}
 		}
 
 	}
 
 	public int rowcol(float z) {
 		return (int)(((z + 100)/200)*20);
 	}
 	private void display() {
 
 		cam.setModelViewMatrix();	
 
 		Gdx.gl11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
 
 
 
 		float[] lightPosition1 = new float[4];
 		lightPosition1 = new float[4];
 		lightPosition1[0] = this.cam.eye.x;
 		lightPosition1[1] = 1f;
 		lightPosition1[2] = this.cam.eye.z;
 		lightPosition1[3] = 1.0f;
 
 		
 		Gdx.gl11.glLightfv(GL11.GL_LIGHT0, GL11.GL_POSITION, lightPosition1, 0);
 
 
 		float[] lightDiffuse1 = {0.02f, 0.02f, 0.02f, 0};
 
 		Gdx.gl11.glLightfv(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, lightDiffuse1, 0);
 		
 		Gdx.gl11.glDisable(GL11.GL_COLOR_MATERIAL);
 		this.mazebase.display();
 
 		for(Wall wall: this.walls)
 		    wall.display();
 		Gdx.gl11.glEnable(GL11.GL_COLOR_MATERIAL);
 
 		Gdx.gl11.glPushMatrix();
 		Gdx.gl11.glTranslatef(1.0f, 1.0f, 1.0f);
 		Gdx.gl11.glRotatef(90, 0.0f, 1.0f, 0.0f);
 		
 		Gdx.gl11.glEnable(GL11.GL_COLOR_MATERIAL);
 
 		for(Spongebob s : this.spongebobs)
 			s.display();
 		
 		Gdx.gl11.glPopMatrix();
 
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
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char arg0) {
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int arg0) {
 
 		return false;
 	}
 
 	public boolean scrolled(int arg0) {
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int arg0, int arg1, int arg2, int arg3) {
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int arg0, int arg1, int arg2) {
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int arg0, int arg1, int arg2, int arg3) {
 		return false;
 	}
 
 	@Override
 	public boolean touchMoved(int arg0, int arg1) {
 		return false;
 	}
 }
