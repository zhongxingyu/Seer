 package interaction;
 
 import engine.MainLoop;
 import engine.Tile;
 import entities.Entity;
 import entities.Projectile;
 import entities.Tower;
 import entities.Unit;
 import fx.FxEntity;
 import fx.Particle;
 import fx.Shockwave;
 import game.Game;
 import game.GameMode;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.nio.Buffer;
 import java.nio.ByteBuffer;
 import java.nio.IntBuffer;
 import java.util.ArrayList;
 import javax.media.opengl.*;
 import javax.media.opengl.glu.*;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import com.jogamp.opengl.util.texture.Texture;
 
 
 import util.Line2D;
 import util.Rectangle2D;
 import util.SafeList;
 import util.Vector2D;
 
 public class OpenGLRenderer implements GLEventListener {
 
 	private Game game;
 	private int tileSize;
 	private double scaleFactor;
 	private Vector2D offset;
 
 	
 	//shockwave shader
 	int radiusUniform;
 	int posUniform;
 	int numberUniform;		
 	
 	//only valid while display is running
 	private GL2 gl;
 	
 	public OpenGLRenderer(Game g, int tS, BFrame frame) {
 		game = g;
 		tileSize = tS;
 		frame.canvas.addGLEventListener(this);
 	}
 	
 	@Override
 	public void display(GLAutoDrawable drawable) {
 		//Draw
 		scaleFactor = tileSize * game.view.zoom;
 		offset = game.view.offset;
 		gl = drawable.getGL().getGL2();
 		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
 		
 		
 		drawField();
 		drawEntities();
 		gl.glEnable(GL2.GL_BLEND);
 		drawParticles();
 		drawEffects();
 		
 		Line2D line = new Line2D(new Vector2D(11.4,7), new Vector2D(76,1));
 		ArrayList<Tile> tilesOnLine = game.collisionSystem.getTilesOn(line);
 		System.out.println(tilesOnLine.size());
 		
 		gl.glEnable(GL2.GL_BLEND);
 		gl.glColor4d(0.0,0.3,0.3, 0.5);
 		for(Tile t : tilesOnLine) {
 			square(t.center,1);
 		}
 		line(line.a,line.b,1);
		gl.glDisable(GL2.GL_BLEND);
 		
 		/*
 		gl.glGenFramebuffersEXT
 		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, myFBO);
 		glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, myTexture, 0);
 		*/
 		
 		drawHUD();
 		gl.glDisable(GL2.GL_BLEND);
 	}
 	
 	private void setupShaders(GL2 gl) {
 		int f = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);
 		
 		BufferedReader brf = null;
 		try {
 			brf = new BufferedReader(new FileReader("src/shader/shockwave.glsl"));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return;
 		}
 		String[] fsrc = {""};
 		String line;
 		try {
 			while ((line=brf.readLine()) != null) {
 			  fsrc[0] += line + "\n";
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		}
 		
 		int len[] = new int[1];
 		len[0] = fsrc[0].length();
 		
 		gl.glShaderSource(f, 1, fsrc, len, 0);
 		gl.glCompileShader(f);
 		
 		checkLogInfo(gl, f);
 		
 		
 		
 		
 
 		int shaderprogram = gl.glCreateProgram();
 		gl.glAttachShader(shaderprogram, f);
 		gl.glLinkProgram(shaderprogram);
 		gl.glValidateProgram(shaderprogram);
 
 		gl.glUseProgram(shaderprogram);
 		
 		radiusUniform = gl.glGetUniformLocation(shaderprogram, "radius");
 		posUniform = gl.glGetUniformLocation(shaderprogram, "pos");
 		numberUniform = gl.glGetUniformLocation(shaderprogram, "number");		
 	}
 	
 	private void checkLogInfo(GL2 gl, int programObject) {
         IntBuffer intValue = IntBuffer.allocate(1);
         gl.glGetObjectParameterivARB(programObject, GL2.GL_OBJECT_INFO_LOG_LENGTH_ARB, intValue);
         
         int lengthWithNull = intValue.get();
 
         if (lengthWithNull <= 1) {
             return;
         }
 
         ByteBuffer infoLog = ByteBuffer.allocate(lengthWithNull);
 
         intValue.flip();
         gl.glGetInfoLogARB(programObject, lengthWithNull, intValue, infoLog);
 
         int actualLength = intValue.get();
 
         byte[] infoBytes = new byte[actualLength];
         infoLog.get(infoBytes);
         System.out.println("GLSL Validation >> " + new String(infoBytes));
     }
 	
 	private void drawParticles() {
 		SafeList<Particle> particles = game.particleSystem.particles;
 		
 		gl.glPointSize((float)(1*scaleFactor));
 		gl.glBegin(GL.GL_POINTS);
 		
 		if(particles.size()>0)
 		System.out.println(particles.size());
 		
 		for(FxEntity fe : particles) {
 			Particle p = (Particle)fe;
 			gl.glColor4d(0.2, Math.random()*0.2, 0.01, 0.5);
 			particle(p.pos);
 		}
 		
 		gl.glEnd();
 	}
 	
 	private void drawEffects() {
 		SafeList<FxEntity> fxs = game.particleSystem.fxEntities;
 		
 		float swPositions[] = new float[20];
 		float swRadiuses[] = new float[10];
 		int shockwaves = 0;
 		
 		for(FxEntity e : fxs) {
 			if(e instanceof Shockwave) {
 				Shockwave sw = (Shockwave)e;
 				
 				Point screenPos = game.view.worldToViewShader(sw.pos);
 
 				swPositions[shockwaves*2] = screenPos.x;
 				swPositions[shockwaves*2+1] = screenPos.y;
 
 				swRadiuses[shockwaves] = (float) (sw.radius*scaleFactor);
 				shockwaves++;
 				
 				System.out.println("SW: " + screenPos + ", " + sw.radius*scaleFactor);
 			}
 		}	
 		
 		gl.glUniform1i(numberUniform, shockwaves);
 		gl.glUniform1fv(radiusUniform, swRadiuses.length, swRadiuses, 0);
 		gl.glUniform2fv(posUniform, swPositions.length, swPositions, 0);
 	}
 	
 	private void drawEntities() {
 		for(Entity e : game.entities) {
         	if(e instanceof Tower) {
         		Tower tower = (Tower)e;
         		gl.glColor3d(0,0,1);
         		rhombus(tower.pos, 1);
         		line(tower.pos, tower.pos.add(tower.aim.scalar(1.4)), 0.05f);
         	}
         	else if(e instanceof Projectile) {
         		Projectile proj = (Projectile)e;
         		
         		double length;
         		if(proj.pos.distance(proj.origin) < proj.length) 
         			length = proj.pos.distance(proj.origin);
         		else length = proj.length;
         		gl.glColor3d(0.5,0.5,1);
         		line(proj.pos, proj.pos.subtract(proj.direction.scalar(length)), 0.1f);
         	}
         	else if(e instanceof Unit) {
         		Unit u = (Unit)e;
         		
         		if(u == game.selectedUnit && game.mode == GameMode.ACTION) {
         			gl.glColor3d(1,0,0);
         			circle(u.pos, u.getRadius()+0.2);
         		}
         		else if(u == game.selectedUnit && game.mode == GameMode.STRATEGY) {
         			gl.glColor3d(1, 0, 0);
         			line(u.pos, u.pos.add(new Vector2D(u.getRadius(), u.getRadius())), 0.1f);
         			line(u.pos, u.pos.add(new Vector2D(-u.getRadius(), u.getRadius())), 0.1f);
         			line(u.pos, u.pos.add(new Vector2D(u.getRadius(), -u.getRadius())), 0.1f);
         			line(u.pos, u.pos.add(new Vector2D(-u.getRadius(), -u.getRadius())), 0.1f);
         		}
         		if(u.path != null && u.path.size() > 3) {
         			gl.glEnable(GL2.GL_BLEND);
         			gl.glColor3d(0,.2,0);
         			line(u.pos, u.path.get(u.path.size()-1), 10f);
         			gl.glDisable(GL2.GL_BLEND);
         		}
         			
         		gl.glColor3d(0,1,0);
         		circle(u.pos, u.getRadius());
         	}
 
         }
 	}
 
 	private void drawField() {
 		gl.glBegin(GL2.GL_QUADS);
 		
 		for(int x=0; x < game.field.tilesX; x++) {
 			for(int y=0; y < game.field.tilesY; y++) {
 				
 				gl.glColor3d((double)x/game.field.tilesX-0.2, (double)x/game.field.tilesX-0.2, (double)y/game.field.tilesY-0.2);
 				
 				switch(game.field.tiles[x][y].getType()) {
 				case 1:
 					tile(new Vector2D(x+0.5,y+0.5));
 					break;
 				}
 			}
 		}
 
 		gl.glEnd();
 	}
 	
 	private void drawHUD() {
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
 		
 		//Selection Rectangle
 		if(game.selectionRect != null) {
 			Rectangle2D sel = game.selectionRect;
 			gl.glColor3d(0.2,0.2,0.3);
 			rectangle(sel.topleft, sel.bottomright);
 		}
 	}
 	
 	private void circle(Vector2D pos, double radius) {
 		double angle;
 		gl.glBegin(GL2.GL_POLYGON);
 	    for(int i = 100; i > 1; i--) {
 	        angle = i * 2 * Math.PI / 100;
 	        gl.glVertex2d((offset.x + pos.x + (Math.cos(angle) * radius)) * scaleFactor, 
 	        		(offset.y + pos.y + (Math.sin(angle) * radius)) * scaleFactor);
 	    }
 	    gl.glEnd();
 	}
 	
 	private void particle(Vector2D pos) {
 		gl.glVertex2d(scaleFactor * (offset.x + pos.x), scaleFactor * (pos.y + offset.y));
 	}
 	
 	private void line(Vector2D a, Vector2D b, float width) {
 		gl.glLineWidth(width*(float)scaleFactor);
 		gl.glBegin(GL2.GL_LINES);
 		gl.glVertex2d((a.x()+offset.x)*scaleFactor, (a.y()+offset.y)*scaleFactor);
 		gl.glVertex2d((b.x()+offset.x)*scaleFactor, ((b.y()+offset.y)*scaleFactor));
 		gl.glEnd();
 	}
 	
 	private void rhombus(Vector2D pos, double edgeLength) {
 		edgeLength /= 2;
 		gl.glBegin(GL2.GL_QUADS);
 		gl.glVertex2d((pos.x-edgeLength+offset.x)*scaleFactor, (pos.y+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x+offset.x)*scaleFactor, (pos.y-edgeLength+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x+edgeLength+offset.x)*scaleFactor, (pos.y+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x+offset.x)*scaleFactor, (pos.y+edgeLength+offset.y)*scaleFactor);
 		gl.glEnd();
 	}
 	
 	private void square(Vector2D pos, double edgeLength) {
 		edgeLength /= 2;
 		gl.glBegin(GL2.GL_QUADS);
 		gl.glVertex2d((pos.x-edgeLength+offset.x)*scaleFactor, (pos.y+edgeLength+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x+edgeLength+offset.x)*scaleFactor, (pos.y+edgeLength+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x+edgeLength+offset.x)*scaleFactor, (pos.y-edgeLength+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x-edgeLength+offset.x)*scaleFactor, (pos.y-edgeLength+offset.y)*scaleFactor);
 		gl.glEnd();
 	}
 	
 	private void rectangle(Vector2D topleft, Vector2D bottomright) {
 		gl.glBegin(GL2.GL_QUADS);
 		gl.glVertex2d((topleft.x+offset.x)*scaleFactor, (topleft.y+offset.y)*scaleFactor);
 		gl.glVertex2d((topleft.x+offset.x)*scaleFactor, (bottomright.y+offset.y)*scaleFactor);
 		gl.glVertex2d((bottomright.x+offset.x)*scaleFactor, (bottomright.y+offset.y)*scaleFactor);
 		gl.glVertex2d((bottomright.x+offset.x)*scaleFactor, (topleft.y+offset.y)*scaleFactor);
 		gl.glEnd();
 	}
 	
 	private void tile(Vector2D pos) {
 		gl.glVertex2d((pos.x-0.5+offset.x)*scaleFactor, (pos.y+0.5+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x+0.5+offset.x)*scaleFactor, (pos.y+0.5+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x+0.5+offset.x)*scaleFactor, (pos.y-0.5+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x-0.5+offset.x)*scaleFactor, (pos.y-0.5+offset.y)*scaleFactor);
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
 		
 		gl.glBlendFunc(GL2.GL_ONE, GL2.GL_ONE);
 		
 		setupShaders(gl);
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
