 import org.lwjgl.*;
 import org.lwjgl.input.*;
 import org.lwjgl.opengl.*;
 import org.lwjgl.opengl.GL11.*;
 import org.lwjgl.util.glu.*;
 import org.lwjgl.util.vector.*;
 import java.io.*;
 
 class BlenderImport {
 
 	String windowTitle = "Blender import example and model viewer";
 	private boolean closeRequested = false;
 	private long lastFrameTime;
 	private float rotation = 0f;
 
     private int objectModelId;
 
 	private void run(){
 		createWindow();
 		getDelta();
 
 		initGL();
 
 		while(!closeRequested){
 
             if(Display.isCloseRequested()){
                 closeRequested = true;
             }
 			input();
 			updateLogic(getDelta());
 			render();
 
 			Display.update();
 		}
 
 		cleanup();
 	}
     private void createWindow(){
 		try{
 			Display.setDisplayMode(new DisplayMode(640, 480));
 			Display.setVSyncEnabled(true);
 			Display.setTitle(windowTitle);
 			Display.create();
 		}catch(LWJGLException e){
 			Sys.alert("Error", "initialization failed!\n\n" + e.getMessage());
 			System.exit(0);
 		}
 	}
 
 	/*
 	*return how many milliseconds have passed since the last frame
 	*/
 	private int getDelta(){
 		long time = (Sys.getTime() * 1000) / Sys.getTimerResolution();
 		int delta = (int)(time-lastFrameTime);
 		lastFrameTime = time;
 		return delta;
 	}
 
 	private void initGL(){
 
 		GL11.glViewport(0, 0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight());
 		GL11.glMatrixMode(GL11.GL_PROJECTION);http://lwjgl.org
 		GL11.glLoadIdentity();
 		GLU.gluPerspective(45f, ((float)Display.getDisplayMode().getWidth()/(float)Display.getDisplayMode().getHeight()), 0.1f, 1000f); //wideness(pie/4) in degrees, aspect ratio, close-clip, far-clip
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 		GL11.glLoadIdentity();
 
 		GL11.glShadeModel(GL11.GL_SMOOTH);
 		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
 		GL11.glClearDepth(1.0f);
 		GL11.glEnable(GL11.GL_DEPTH_TEST);
 		GL11.glDepthFunc(GL11.GL_LEQUAL);
 		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
 
         //loading a model
         objectModelId = GL11.glGenLists(1);
         GL11.glNewList(objectModelId , GL11.GL_COMPILE);
         {
             Model m = null;
             try {
                m = OBJManager.loadModel("content\\models\\soldier1.obj");
             } catch(Exception e) {
                 e.printStackTrace();
                 cleanup();
                 System.exit(1);
             }
             GL11.glBegin(GL11.GL_POINTS);
                 for (Face f : m.faces ) {
                     Vector3f v1 = m.verticies.get((int) f.vertex.w -1);
                     GL11.glVertex3f(v1.x, v1.y, v1.z);
                     Vector3f v2 = m.verticies.get((int) f.vertex.x -1);
                     GL11.glVertex3f(v1.x, v1.y, v1.z); Vector3f v3 = m.verticies.get((int) f.vertex.y -1);
                     GL11.glVertex3f(v1.x, v1.y, v1.z);
                     Vector3f v4 = m.verticies.get((int) f.vertex.z -1);
                     GL11.glVertex3f(v1.x, v1.y, v1.z);
                 }
             GL11.glEnd();
 
         }
         GL11.glEndList();
 	}
 
 	private void input(){
 
         int x = Mouse.getX();
         int y = Mouse.getY();
 
 
         if (Mouse.isButtonDown(0)) {
             System.out.println("mouse down at : " + x + " , " + y);
         }
 
         if(Keyboard.isKeyDown(Keyboard.KEY_W)) {
             GL11.glTranslatef(0,0,0.25f);
         }
         if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
             GL11.glRotatef(1,0,1,0);
         }
         if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
             GL11.glTranslatef(0,0,-0.25f);
         }
         if(Keyboard.isKeyDown(Keyboard.KEY_D)) {
             GL11.glRotatef(-1,0,1,0);
         }
         while(Keyboard.next()) {
             if (Keyboard.getEventKeyState()) {
                 if(Keyboard.getEventKey() == Keyboard.KEY_W) {
                 }
                 if(Keyboard.getEventKey() == Keyboard.KEY_A) {
                 }
                 if(Keyboard.getEventKey() == Keyboard.KEY_S) {
                 }
                 if(Keyboard.getEventKey() == Keyboard.KEY_D) {
                 }
             } else {
                 if(Keyboard.getEventKey() == Keyboard.KEY_W) {
                 }
                 if(Keyboard.getEventKey() == Keyboard.KEY_A) {
                 }
                 if(Keyboard.getEventKey() == Keyboard.KEY_S) {
                 }
                 if(Keyboard.getEventKey() == Keyboard.KEY_D) {
                 }
             }
         }
     }
 
 	private void updateLogic(int delta){
 		rotation+=1f;
 	}
 
 	private void render(){
 		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
         GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK,  GL11.GL_LINE);
 
         GL11.glCallList(objectModelId);
 	}
 
 	private void cleanup(){
 		Display.destroy();
 	}
 
 	public static void main(String[] args){
 		BlenderImport ma = new BlenderImport();
 		ma.run();
 
     }
 }
