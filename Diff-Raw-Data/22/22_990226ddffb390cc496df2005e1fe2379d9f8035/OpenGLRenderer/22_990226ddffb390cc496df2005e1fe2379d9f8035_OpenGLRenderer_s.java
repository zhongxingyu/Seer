 package interaction;
 
 import engine.MainLoop;
 import game.Game;
 import game.Particle;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.util.ArrayList;
 import javax.media.opengl.*;
 import javax.media.opengl.glu.*;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import Entities.Entity;
 import Entities.Projectile;
 import Entities.Tower;
 import Entities.Unit;
 
 import util.Line2D;
 import util.Vector2D;
 
 public class OpenGLRenderer implements GLEventListener {
 
 	private Game game;
 	private int tileSize;
 	private double scaleFactor;
 	private Vector2D offset;
 
 	public OpenGLRenderer(Game g, int tS) {
 		game = g;
 		tileSize = tS;
 	}
 	
 	private void drawEntities(GL2 gl) {
 		for(Entity e : game.entities) {
         	if(e instanceof Tower) {
         		Tower tower = (Tower)e;
         		gl.glColor3d(0,0,1);
         		rhombus(gl, tower.pos);
         		line(gl, tower.pos, tower.pos.add(tower.aim.scalar(1.4)), 1);
         	}
         	else if(e instanceof Projectile) {
         		Projectile proj = (Projectile)e;
         		
         		double length;
         		if(proj.pos.distance(proj.origin) < proj.length) 
         			length = proj.pos.distance(proj.origin);
         		else length = proj.length;
         		gl.glColor3d(0.5,0.5,1);
         		line(gl, proj.pos, proj.pos.subtract(proj.direction.scalar(length)), 2);
         	}
         	else if(e instanceof Particle) {
         		Particle part = (Particle)e;
         		double c = part.life/part.lifetime;
     			gl.glColor4d(0.6,0.6,0.6,c);
     			line(gl, part.pos, part.pos.subtract(part.direction.scalar(0.1)), 1);
         	}
         	else if(e instanceof Unit) {
         		Unit u = (Unit)e;
         		gl.glColor3d(0,1,0);
         		circle(gl, u.pos, u.getRadius());
         	}
 
         }
 	}
 
 	private void drawField(GL2 gl) {
 		gl.glBegin(GL2.GL_QUADS);
 		
 		for(int x=0; x < game.field.tilesX; x++) {
 			for(int y=0; y < game.field.tilesY; y++) {
 				
 				gl.glColor3d(1, (double)x/game.field.tilesX, (double)y/game.field.tilesY);
 				
 				switch(game.field.tiles[x][y].getType()) {
 				case 1:
 					tile(gl, new Vector2D(x+0.5,y+0.5));
 					break;
 				}
 			}
 		}
 
 		gl.glEnd();
 	}
 	
 	private void drawHUD(GL2 gl) {
 		//Cursor
 		Point cursor = game.input.viewCursorPos;
 		gl.glLineWidth(2);
 		gl.glBegin(GL2.GL_LINES);
 		gl.glColor3d(0,1,0);
 		gl.glVertex2d(cursor.x, game.view.windowSize.height-cursor.y-10);
 		gl.glVertex2d(cursor.x, game.view.windowSize.height-cursor.y+10);
 		gl.glVertex2d(cursor.x-10, game.view.windowSize.height-cursor.y);
 		gl.glVertex2d(cursor.x+10, game.view.windowSize.height-cursor.y);
 		gl.glEnd();
 	}
 	
 	private void circle(GL2 gl, Vector2D pos, double radius) {
 		double angle;
 		gl.glBegin(GL2.GL_POLYGON);
 	    for(int i = 100; i > 1; i--) {
	        angle = i*2*Math.PI/100;
	        gl.glVertex2d(pos.x + (Math.cos(angle) * radius), pos.y + (Math.sin(angle) * radius));
 	    }
 	    gl.glEnd();
 	}
 	
 	private void line(GL2 gl, Vector2D a, Vector2D b, float width) {
 		gl.glLineWidth(width);
 		gl.glBegin(GL2.GL_LINES);
 		gl.glVertex2d((a.x()+offset.x)*scaleFactor, (a.y()+offset.y)*scaleFactor);
 		gl.glVertex2d((b.x()+offset.x)*scaleFactor, ((b.y()+offset.y)*scaleFactor));
 		gl.glEnd();
 	}
 	
 	private void rhombus(GL2 gl, Vector2D pos) {
 		gl.glBegin(GL2.GL_QUADS);
 		gl.glVertex2d((pos.x-0.5+offset.x)*scaleFactor, (pos.y+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x+offset.x)*scaleFactor, (pos.y-0.5+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x+0.5+offset.x)*scaleFactor, (pos.y+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x+offset.x)*scaleFactor, (pos.y+0.5+offset.y)*scaleFactor);
 		gl.glEnd();
 	}
 	
 	private void tile(GL2 gl, Vector2D pos) {
 		gl.glVertex2d((pos.x-0.5+offset.x)*scaleFactor, (pos.y+0.5+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x+0.5+offset.x)*scaleFactor, (pos.y+0.5+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x+0.5+offset.x)*scaleFactor, (pos.y-0.5+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x-0.5+offset.x)*scaleFactor, (pos.y-0.5+offset.y)*scaleFactor);
 	}
 
 	@Override
 	public void display(GLAutoDrawable drawable) {
 		//Draw
 		scaleFactor = tileSize * game.view.zoom;
 		offset = game.view.offset;
 		GL2 gl = drawable.getGL().getGL2();
 		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
 		
 		drawField(gl);
 		drawEntities(gl);
 		drawHUD(gl);
 	}
 	
 	@Override
 	public void dispose(GLAutoDrawable drawable) {
 		//Clean up
 
 	}
 
 	@Override
 	public void init(GLAutoDrawable drawable) {
 		//Initialize
 		GL2 gl = drawable.getGL().getGL2();
 		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
 		gl.glMatrixMode(GL2.GL_PROJECTION);
 		gl.glLoadIdentity();
 	}
 
 	@Override
 	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
 		//Resize window
 		GL2 gl = drawable.getGL().getGL2();
 		gl.glLoadIdentity();
 		GLU glu = new GLU();
 		gl.glViewport(0, 0, width, height);
 		glu.gluOrtho2D(0, width, 0, height);
 		game.view.windowSize = new Dimension(width, height);
 	}
 
 
 
 }
