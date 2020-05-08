 package com.game.src;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.util.vector.Vector3f;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.util.glu.GLU.*;
 
 public class Viewer {
 
 	private Vector3f location, rotation;
 	private Model m;
 	private List<Point> points;
 
 	public void run() {
 		try {
 			Display.setDisplayMode(new DisplayMode(1200, 900));
			Display.setTitle("Unnamed Game");
 			Display.create();
			Mouse.setGrabbed(true);
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 			Display.destroy();
 			System.exit(1);
 		}
 
 		points = new ArrayList<Point>();
 		Random rand = new Random();
 		for (int i = 0; i < 10000; i++) {
 			points.add(new Point(rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f, -rand.nextFloat() * 200));
 		}
 		location = new Vector3f(0f, 0f, 0f);
 		rotation = new Vector3f(0f, 0f, 0f);
 		try {
 			m = Model.getModel("res/teste.obj");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		gluPerspective(30f, (float) (640 / 480), 0.3f, 100f);
 		glMatrixMode(GL_MODELVIEW);
 		glEnable(GL_DEPTH_TEST);
 
		try {
			Mouse.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		
 		while (!Display.isCloseRequested()) {
 			input();
 			render();
 			Display.update();
 			Display.sync(60);
 		}
 
 		Display.destroy();
 		System.exit(0);
 	}
 
 	private void input() {
 		boolean up = Keyboard.isKeyDown(Keyboard.KEY_W);
 		boolean down = Keyboard.isKeyDown(Keyboard.KEY_S);
 		boolean left = Keyboard.isKeyDown(Keyboard.KEY_A);
 		boolean right = Keyboard.isKeyDown(Keyboard.KEY_D);
 
 		boolean flyup = Keyboard.isKeyDown(Keyboard.KEY_E);
 		boolean flydown = Keyboard.isKeyDown(Keyboard.KEY_Q);
 
 		boolean speedup = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
 		boolean slowdown = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
 
 		float walkspeed = 0.15f;
	
		if ((Mouse.getEventDX() != 0) || (Mouse.getEventDY() != 0)) {
			System.out.println("H");
		} else {
			System.out.println(Mouse.EVENT_SIZE + " : " + ((900 / 2) - Mouse.getY()));			
		}
		//Mouse.setCursorPosition((int) 1200 / 2, (int) 900 / 2);
 
 		float mx = Mouse.getDX();
 		float my = Mouse.getDY();
		mx *= 0.25f;
		my *= 0.25f;
 
 		rotation.y += mx;
 		if (rotation.y > 360) rotation.y -= 360;
 		rotation.x -= my;
 		if (rotation.x > 85) rotation.x = 85;
 		if (rotation.x < -85) rotation.x = -85;
 
		if (speedup && !slowdown) walkspeed = 0.30f;
 		if (!speedup && slowdown) walkspeed = 0.10f;
 
 		if (up && !down) {
 			float cz = (float) (walkspeed * 2 * Math.cos(Math.toRadians(rotation.y)));
 			float cx = (float) (walkspeed * Math.sin(Math.toRadians(rotation.y)));
 			location.z += cz;
 			location.x -= cx;
 		}
 
 		if (!up && down) {
 			float cz = (float) (walkspeed * 2 * Math.cos(Math.toRadians(rotation.y)));
 			float cx = (float) (walkspeed * Math.sin(Math.toRadians(rotation.y)));
 			location.z -= cz;
 			location.x += cx;
 		}
 
 		if (!left && right) {
 			float cz = (float) (walkspeed * 2 * Math.cos(Math.toRadians(rotation.y + 90)));
 			float cx = (float) (walkspeed * Math.sin(Math.toRadians(rotation.y + 90)));
 			location.z += cz;
 			location.x -= cx;
 		}
 
 		if (left && !right) {
 			float cz = (float) (walkspeed * 2 * Math.cos(Math.toRadians(rotation.y + 90)));
 			float cx = (float) (walkspeed * Math.sin(Math.toRadians(rotation.y + 90)));
 			location.z -= cz;
 			location.x += cx;
 		}
 
 		if (flyup && !flydown) location.y -= walkspeed;
 		if (!flyup && flydown) location.y += walkspeed;
 	}
 
 	private void render() {
 		glPushMatrix();
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 		glRotatef(rotation.x, 1f, 0f, 0f);
 		glRotatef(rotation.y, 0f, 1f, 0f);
 		glRotatef(rotation.z, 0f, 0f, 1f);
 		glTranslatef(location.x, location.y, location.y);
 		for (Point p : points) {
 			p.render();
 		}
 		m.render();
 		glPopMatrix();
 	}
 
 	private class Point {
 		float x, y, z;
 
 		public Point(float x, float y, float z) {
 			this.x = x;
 			this.y = y;
 			this.z = z;
 		}
 
 		public void render() {
 			glBegin(GL_POINTS);
 			glVertex3f(x, y, z);
 			glEnd();
 		}
 	}
 }
