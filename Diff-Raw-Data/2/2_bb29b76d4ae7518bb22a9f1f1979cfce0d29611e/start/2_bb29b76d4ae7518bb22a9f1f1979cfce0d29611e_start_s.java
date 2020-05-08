 package org.noip.evan1026.graphics;
 
 
 import org.noip.evan1026.*;
 import java.awt.Color;
 import java.awt.Toolkit;
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 
 import org.lwjgl.util.glu.Cylinder;
 import org.lwjgl.util.glu.Disk;
 import org.lwjgl.util.glu.GLU;
 import org.lwjgl.util.glu.Sphere;
 
 import static org.lwjgl.opengl.GL11.*;
 
 public class start {
 
 	private static Game game;
 	
 	
 	private static Camera camera;
 	private static boolean mouseControl = true;
 	private static boolean fullScreen = false;
 
 
 	private static ArrayList<Projectile> projectiles;
 
 	private static Projectile firstProjectile;
 	private static Projectile secondProjectile;
 
 
 	private static ArrayList<Target> targets;
 	private static Point3D targetsPosition = new Point3D(-5, -3, 5);
 
 
 	private static ArrayList<Rope> ropes;
 
 
 	private static boolean playerTurn; //false then true
 
 	//									Neutral		Player 1	Player 2
 	public static Color[] teamColor = {Color.GREEN, Color.RED, Color.BLUE};
 
 	private static int boardSize = 5;
 
 
 	private static float roomSpin = 0;
 
 
 	public static void main(String[] args){
 
 		initDisplay();
 		initScene();
 		initGame();
 		initCamera();
 
 		while (!Display.isCloseRequested()){
 
 			physicsCalculations();
 			render();
 			checkInput();
 
 			Display.update();
 			Display.sync(60);
 		}
 
 		Display.destroy();
 
 		System.exit(0);
 
 	}
 
 	private static void initDisplay(){
 		try {
 			Display.setFullscreen(fullScreen);
 			Toolkit tk = Toolkit.getDefaultToolkit();
 
 			int height = (int) (tk.getScreenSize().height);
 			int width = (int) (tk.getScreenSize().width);
 
 			if (fullScreen){
 				Display.setDisplayMode(new DisplayMode(width, height));
 			}else{
 				//Display.setDisplayMode(new DisplayMode((int) (width * 0.75), (int) (height * 0.75)));
 				Display.setDisplayMode(new DisplayMode(1700, 800));
 			}
 
 			Display.setVSyncEnabled(true);
 
 			Display.sync(60);
 			Display.setTitle("Nim");
 
 			//Display.setLocation(100, 100);
 			Display.setLocation(1400, 100);
 
 			Display.create();
 		} catch (LWJGLException e) {
 			System.err.println("Error with initializing LWJGL display");
 			e.printStackTrace();
 		}
 	}
 
 	private static void initGame(){
 
 		game = new Game(boardSize);
 		
 		projectiles = new ArrayList<Projectile>();
 
 		targets = new ArrayList<Target>();
 
 		ropes = new ArrayList<Rope>();
 
 		float spherePadding = 0.25f;
 
 		//full width of triangle is the diameter of all the balls plus the padding at the base
 		float triFullWidth = boardSize * (Target.radius*2) + (spherePadding * (boardSize-1));
 
 		for (int triRow = 0; triRow < boardSize; triRow++){
 
 			float xPadding =  (float) ((boardSize - (triRow + 1)) / 2.0 * (Target.radius*4));
 
 			for (int triCol = 0; triCol < triRow + 1; triCol++){
 
 				targets.add(new Target(
 						new Point3D(targetsPosition.x + xPadding + (4*Target.radius * triCol),  targetsPosition.y + triRow * (spherePadding + Target.radius*2), targetsPosition.z),
 						new Rotation(0,0,0),
 						triRow,
 						triCol
 						));
 			}
 
 
 
 		}
 
 
 	}
 
 	private static void initScene(){
 
 		glClearColor(1.0f, 1.0f, 1.0f, 1);
 
 		//Don't put stuff that's behind a thing in front of it...
 		glEnable(GL_DEPTH_TEST);
 
 		glEnable(GL_DOUBLEBUFFER);
 
 
 		//Don't render the back of polygons
 		//REMEMBER TO INITIALIZE VERTICES IN
 		//COUNTER-CLOCKWISE ORDER!!!!!!!
 		glEnable(GL_CULL_FACE);
 		glCullFace(GL_BACK);
 
 
 		glPushAttrib(GL_TRANSFORM_BIT);
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		GLU.gluPerspective(90f, Display.getWidth()/Display.getHeight(), 0.001f, 100f);
 		glPopAttrib();
 
 		glMatrixMode(GL_MODELVIEW);
 
 		Mouse.setGrabbed(mouseControl);
 
 	}
 
 	private static void initCamera(){
 
 		camera = new Camera(new Point3D(0,0,0), new Rotation(0, 0, 0), 90, Display.getWidth()/Display.getHeight(), 0.001f, 100);
 		camera.initPerspective();
 	}
 
 	public static void physicsCalculations(){
 
 		for (int i = 0; i < projectiles.size(); i++){
 
 			//remove the far gone projectiles or ones with a dead partner... *sniffle*
 			if (projectiles.get(i).isOutOfZone() || projectiles.get(i).isToBeDestroyed()){
 				projectiles.remove(i);
 				i--;
 				continue;
 			}
 			projectiles.get(i).update();
 		}
 
 
 
 		for (int p = 0; p < projectiles.size(); p++){
 			if (projectiles.get(p).isStuck()) continue; //skip if stuck already
 			//this optimizes and makes it set the color only once it hits, not each loop through	
 			for (int t = 0; t < targets.size(); t++){
 				if (targets.get(t).isColliding(projectiles.get(p))){
 
 					projectiles.get(p).setStuck(true, targets.get(t)); //stick projectiles to target
 
 				}
 			}
 		}
 
 
 		for (int i = 0; i < ropes.size(); i++){
 			Projectile[] attachedProjs = ropes.get(i).getAttachedProjectiles();
 			
 			//remove ropes that have lost a partner :(
 			if (ropes.get(i).lostProjectile()){
 				for (int p = 0; p < attachedProjs.length; p++){
 					if (attachedProjs[p] != null){
 						attachedProjs[p].setToBeDestroyed(true);
 					}
 				}
 				ropes.remove(i);
 				i--;
 				continue;
 			}
 			
 			if (attachedProjs[0].getTargetStuckTo() == null || attachedProjs[1].getTargetStuckTo() == null) continue;
 
 			
 			boolean tempProjZeroIsLaterColomn = (attachedProjs[0].getTargetStuckTo().getColomn() > attachedProjs[1].getTargetStuckTo().getColomn());
 			
 			Target target1 = (tempProjZeroIsLaterColomn) ? attachedProjs[1].getTargetStuckTo() :  attachedProjs[0].getTargetStuckTo();
 			Target target2 = (tempProjZeroIsLaterColomn) ? attachedProjs[0].getTargetStuckTo() :  attachedProjs[1].getTargetStuckTo();
 			
 			//TODO check if targets are already occupied!!!!
 			
			if (tryDoGameMove(target1, target2)){
 				
 				ropes.get(i).setSolidified(true);
 				
 				
 				//target1.setCaptured( (playerTurn) ? 1 : 2 );
 				//TODO color the targets
 				
 			}else{
 				//remove the balls
 				attachedProjs[0].setToBeDestroyed(true);
 				attachedProjs[1].setToBeDestroyed(true);
 			}
 			
 			
 //			targets.get(t).setColor( (playerTurn) ? teamColor[1] : teamColor[2] );
 			
 				
 		}
 		
 		
 		//end of rope loop
 		
 		
 		
 		
 	}
 
 
 
 
 
 
 
 	private static void render(){
 
 		glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 
 		glLoadIdentity();
 
 		camera.doTranslations();
 
 
 		//render sphere room
 		renderSphere(new Point3D(0, 0, 0), Color.BLACK, GLU.GLU_LINE, new Rotation(90, 0, roomSpin), Projectile.projectileMaxDisplacement, 10, 10);
 		//round and round we go!
 		roomSpin +=0.25;
 		roomSpin %= 360;
 
 		//renderSphereTriangle(-1.0f, 0.0f, -5.0f, Color.YELLOW, 0.25f, 5, 0.25f);
 
 		Color[] cube1 = {Color.PINK, Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA};
 
 		renderCube(0.5, cube1, 0, 3, -5);
 
 		//draw platform cylinder
 		glColor3f(0.5f, 0.2f, 0.8f);
 		glPushMatrix();
 		glTranslatef(0, -1f, 0);
 		glRotated(90, 1, 0, 0);
 		Cylinder patCyl = new Cylinder();
 		patCyl.setDrawStyle(GLU.GLU_LINE);
 		patCyl.draw(1.5f, 1.5f, 50.0f, 8, 1); //basically sets the radius, and number of rows/columns of
 		//vertices that make up the circle
 		glPopMatrix(); //remove any translations made
 
 
 		//draw platform cylinder
 		glColor3f(0.5f, 0.2f, 0.8f);
 		glPushMatrix();
 		glTranslatef(0, -1f, 0);
 		glRotated(90, 1, 0, 0);
 		Disk platDisk = new Disk();
 		platDisk.setDrawStyle(GLU.GLU_LINE);
 		platDisk.draw(1.5f, 0.0f, 8, 10);
 		glPopMatrix(); 
 
 
 		for (int i = 0; i < targets.size(); i++){
 			targets.get(i).draw();
 		}
 
 		for (int i = 0; i < projectiles.size(); i++){
 			projectiles.get(i).draw();
 		}
 
 		for (int i = 0; i < ropes.size(); i++){
 			ropes.get(i).draw();
 		}
 
 		//draw Crosshair
 		glLoadIdentity();
 		renderSphere(new Point3D(0, 0, -0.1f), Color.BLACK, GLU.GLU_FILL, new Rotation(), 0.00025f, 20, 20);
 
 
 	}
 
 	private static void renderCube(double sideLength, Color[] vColors, double xCenter, double yCenter, double zCenter){
 		double halfLength = sideLength / 2.0;
 
 		//vColors should have 8 colors - because 8 vertices (corners)
 
 		/** Color Indexes List **
 		 * near top left =		0
 		 * near top right =		1
 		 * near bottom left =	2
 		 * near bottom right =	3
 		 * 
 		 * far top left =		4
 		 * far top right =		5
 		 * far bottom left =	6
 		 * far bottom right =	7
 		 */
 
 		if (vColors.length != 8) {
 			throw new InvalidParameterException( "Wrong Length of color array to fill all vertices" );
 		}
 
 		//HORRIBLY DEPRECATED GLBEGIN AND QUADS BLARGH
 		//TODO, try to use vertices or circles later
 
 
 		//Vertices are now described as if you are looking straight on
 		//at the face of the side
 
 		//==============================
 		//====		BACK			====
 		//==============================
 		//z is all the same
 		glBegin(GL11.GL_QUADS);
 		//top left
 		glColor3d(vColors[5].getRed()/255.0, vColors[5].getGreen()/255.0, vColors[5].getBlue()/255.0);
 		glVertex3d(xCenter + halfLength, yCenter + halfLength, zCenter - halfLength);
 		//bottom left
 		glColor3d(vColors[7].getRed()/255.0, vColors[7].getGreen()/255.0, vColors[7].getBlue()/255.0);
 		glVertex3d(xCenter + halfLength, yCenter - halfLength, zCenter - halfLength);
 		//bottom right
 		glColor3d(vColors[6].getRed()/255.0, vColors[6].getGreen()/255.0, vColors[6].getBlue()/255.0);
 		glVertex3d(xCenter - halfLength, yCenter - halfLength, zCenter - halfLength);
 		//top right
 		glColor3d(vColors[4].getRed()/255.0, vColors[4].getGreen()/255.0, vColors[4].getBlue()/255.0);
 		glVertex3d(xCenter - halfLength, yCenter + halfLength, zCenter - halfLength);
 
 		glEnd();
 
 
 		//==============================
 		//====		FRONT			====
 		//==============================
 		//z is all the same
 		glBegin(GL11.GL_QUADS);
 		//top left
 		glColor3d(vColors[0].getRed()/255.0, vColors[0].getGreen()/255.0, vColors[0].getBlue()/255.0);
 		glVertex3d(xCenter - halfLength, yCenter + halfLength, zCenter + halfLength);
 		//bottom left
 		glColor3d(vColors[2].getRed()/255.0, vColors[2].getGreen()/255.0, vColors[2].getBlue()/255.0);
 		glVertex3d(xCenter - halfLength, yCenter - halfLength, zCenter + halfLength);
 		//bottom right
 		glColor3d(vColors[3].getRed()/255.0, vColors[3].getGreen()/255.0, vColors[3].getBlue()/255.0);
 		glVertex3d(xCenter + halfLength, yCenter - halfLength, zCenter + halfLength);
 
 		//top right
 		glColor3d(vColors[1].getRed()/255.0, vColors[1].getGreen()/255.0, vColors[1].getBlue()/255.0);
 		glVertex3d(xCenter + halfLength, yCenter + halfLength, zCenter + halfLength);
 
 		glEnd();
 
 		//==============================
 		//====		LEFT			====
 		//==============================
 		//right is towards screen
 		//x is all the same
 		glBegin(GL11.GL_QUADS);
 		//top left
 		glColor3d(vColors[4].getRed()/255.0, vColors[4].getGreen()/255.0, vColors[4].getBlue()/255.0);
 		glVertex3d(xCenter - halfLength, yCenter + halfLength, zCenter - halfLength);
 		//bottom left
 		glColor3d(vColors[6].getRed()/255.0, vColors[6].getGreen()/255.0, vColors[6].getBlue()/255.0);
 		glVertex3d(xCenter - halfLength, yCenter - halfLength, zCenter - halfLength);
 		//bottom right
 		glColor3d(vColors[2].getRed()/255.0, vColors[2].getGreen()/255.0, vColors[2].getBlue()/255.0);
 		glVertex3d(xCenter - halfLength, yCenter - halfLength, zCenter + halfLength);
 		//top right
 		glColor3d(vColors[0].getRed()/255.0, vColors[0].getGreen()/255.0, vColors[0].getBlue()/255.0);
 		glVertex3d(xCenter - halfLength, yCenter + halfLength, zCenter + halfLength);
 
 
 		glEnd();
 
 
 		//==============================
 		//====		RIGHT			====
 		//==============================
 		//left is towards the screen
 		//x is all the same
 		glBegin(GL11.GL_QUADS);
 		//top left
 		glColor3d(vColors[1].getRed()/255.0, vColors[1].getGreen()/255.0, vColors[1].getBlue()/255.0);
 		glVertex3d(xCenter + halfLength, yCenter + halfLength, zCenter + halfLength);
 		//bottom left
 		glColor3d(vColors[3].getRed()/255.0, vColors[3].getGreen()/255.0, vColors[3].getBlue()/255.0);
 		glVertex3d(xCenter + halfLength, yCenter - halfLength, zCenter + halfLength);
 		//bottom right
 		glColor3d(vColors[7].getRed()/255.0, vColors[7].getGreen()/255.0, vColors[7].getBlue()/255.0);
 		glVertex3d(xCenter + halfLength, yCenter - halfLength, zCenter - halfLength);
 		//top right
 		glColor3d(vColors[5].getRed()/255.0, vColors[5].getGreen()/255.0, vColors[5].getBlue()/255.0);
 		glVertex3d(xCenter + halfLength, yCenter + halfLength, zCenter - halfLength);
 
 
 		glEnd();
 
 
 		//==============================
 		//====		TOP			====
 		//==============================
 		//y is all the same
 
 		glBegin(GL11.GL_QUADS);
 		//top left
 		glColor3d(vColors[4].getRed()/255.0, vColors[4].getGreen()/255.0, vColors[4].getBlue()/255.0);
 		glVertex3d(xCenter - halfLength, yCenter + halfLength, zCenter - halfLength);
 		//bottom left
 		glColor3d(vColors[0].getRed()/255.0, vColors[0].getGreen()/255.0, vColors[0].getBlue()/255.0);
 		glVertex3d(xCenter - halfLength, yCenter + halfLength, zCenter + halfLength);
 		//bottom right
 		glColor3d(vColors[1].getRed()/255.0, vColors[1].getGreen()/255.0, vColors[1].getBlue()/255.0);
 		glVertex3d(xCenter + halfLength, yCenter + halfLength, zCenter + halfLength);
 		//top right
 		glColor3d(vColors[5].getRed()/255.0, vColors[5].getGreen()/255.0, vColors[5].getBlue()/255.0);
 		glVertex3d(xCenter + halfLength, yCenter + halfLength, zCenter - halfLength);
 
 		glEnd();
 
 		//==============================
 		//====		BOTTOM			====
 		//==============================
 		//y is all the same
 
 		glBegin(GL11.GL_QUADS);
 		//top left
 		glColor3d(vColors[2].getRed()/255.0, vColors[2].getGreen()/255.0, vColors[2].getBlue()/255.0);
 		glVertex3d(xCenter - halfLength, yCenter - halfLength, zCenter + halfLength);
 		//bottom left
 		glColor3d(vColors[6].getRed()/255.0, vColors[6].getGreen()/255.0, vColors[6].getBlue()/255.0);
 		glVertex3d(xCenter - halfLength, yCenter - halfLength, zCenter - halfLength);
 		//bottom right
 		glColor3d(vColors[7].getRed()/255.0, vColors[7].getGreen()/255.0, vColors[7].getBlue()/255.0);
 		glVertex3d(xCenter + halfLength, yCenter - halfLength, zCenter - halfLength);
 		//top right
 		glColor3d(vColors[3].getRed()/255.0, vColors[3].getGreen()/255.0, vColors[3].getBlue()/255.0);
 		glVertex3d(xCenter + halfLength, yCenter - halfLength, zCenter + halfLength);
 
 
 		glEnd();
 
 
 	}
 
 
 	private static void renderSphere(Point3D pos, Color color, int drawStyle, Rotation rotation,  float radius, int row, int col) {
 		glPushMatrix();
 
 		glTranslatef(pos.x, pos.y, pos.z);
 		glRotatef(rotation.pitch, 1, 0, 0);
 		glRotatef(rotation.yaw, 0, 1, 0);
 		glRotatef(rotation.roll, 0, 0, 1);
 
 		glColor3f(color.getRed()/255.0f, color.getGreen()/255.0f, color.getBlue()/255.0f);
 		Sphere s = new Sphere();
 
 		s.setDrawStyle(drawStyle);
 		s.draw(radius, row, col); //basically sets the radius, and number of rows/columns of
 		//vertices that make up the circle
 		glPopMatrix(); //remove any translations made
 	}
 
 	private static void renderSphere(Point3D pos, Color color, int drawStyle, Rotation rotation,  float radius) {
 		renderSphere(pos, color, drawStyle, rotation, radius, (int) (radius * 75), (int) (radius * 75));
 	}
 
 
 	private static void checkInput(){
 
 
 		//keyboard control does not use the event buffer
 		//just real time state checking
 		camera.doKeyboardControl();
 
 		camera.doMouseControl();
 
 
 
 		//run through keyboard events
 		while (Keyboard.next()){ //while there are keyboard events in the event buffer
 			if (Keyboard.getEventKeyState()){ //true if press event
 
 
 			}else{ //if release event
 				if (Keyboard.getEventKey() == Keyboard.KEY_Q) System.exit(0);
 			}
 
 
 		}
 		while (Mouse.next()){ //event is whenever the mouse moves, or is clicked
 			if (Mouse.getEventButton() == 2 && Mouse.getEventButtonState() && mouseControl){
 				Mouse.setGrabbed(!Mouse.isGrabbed());
 			}
 			
 			/*
 			 * Event Buttons:
 			 *-1 : none
 			 * 0 : left button
 			 * 1 : right button
 			 * 2 : middle button
 			 * 3 : Back (ignore, few people have 3 or 4)
 			 * 4 : Forward  
 			 */
 			if (Mouse.getEventButton() == 0 ){ //only check mouse clicks
 
 				
 				//Generate a projectile on click and release
 				
 				
 
 				if (Mouse.getEventButtonState()){
 					firstProjectile = Projectile.createProjectileFromCamera(camera);
 					projectiles.add(firstProjectile);
 				}else{
 					secondProjectile = Projectile.createProjectileFromCamera(camera);
 					projectiles.add(secondProjectile);
 
 					
 					ropes.add(new Rope(firstProjectile, secondProjectile));
 				}
 				
 				destroyOpposition();
 				
 
 				
 				
 			}
 			
 			
 
 		}
 
 	}
 	
 	public static void destroyOpposition(){
 		
 		for (int i = 0; i < projectiles.size(); i++){
 			if ( ( !projectiles.get(i).isStuck() )  && !( firstProjectile == projectiles.get(i) || secondProjectile == projectiles.get(i) ) ){
 				projectiles.get(i).setToBeDestroyed(true);
 			}
 		}
 		
 	}
 	
 	
 	public static boolean tryDoGameMove(Target target1, Target target2){
 		
 		if (target1.getRow() != target2.getRow()) return false;
 		
 		int tempRow = target1.getRow();
 	
 		int tempDiffCol = Math.abs(target2.getColomn() - target1.getColomn());
 		
 		
 		return game.tryRemoveSelection(tempRow, target1.getColomn(), tempDiffCol);
 		
 	}
 	
 	
 	public static boolean getPlayerTurn(){
 		return playerTurn;
 	}
 
 	public static double degSin(double degrees){
 		return Math.sin(Math.toRadians(degrees));
 	}
 
 	public static double degCos(double degrees){
 		return Math.cos(Math.toRadians(degrees));
 	}
 
 
 
 
 
 }
