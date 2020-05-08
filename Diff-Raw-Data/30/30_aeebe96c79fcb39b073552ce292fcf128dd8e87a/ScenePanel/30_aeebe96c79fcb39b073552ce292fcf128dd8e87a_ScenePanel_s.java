 import javax.media.opengl.*;
 import javax.media.opengl.awt.GLJPanel;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import com.jogamp.opengl.util.*;
 import java.awt.Dimension;
 
 public class ScenePanel extends GLJPanel implements GLEventListener, KeyListener {
 	private static final long serialVersionUID = 702382815287044105L;
 
 	public ScenePanel() {
 		setPreferredSize(new Dimension(500, 500));
 
 		addGLEventListener(this);
 
 		FPSAnimator animator = new FPSAnimator(this, 60); // 60 fps
 		animator.add(this);
 		animator.start();
 	}
 
 	@Override
 	public void init(GLAutoDrawable drawable) {
 		GL2 gl = drawable.getGL().getGL2();
 
 		// Enable Textures
 		gl.glEnable(GL.GL_TEXTURE_2D);
 		// Enable alpha blending
 		gl.glEnable (GL.GL_BLEND);
 		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
 
 		// Create sprite and load the texture files
 		Sprite.loadSprites(gl);
 		
 		Asteroids.init();
 		// We have to have focus for our KeyListener to get messages
 		requestFocus();
 		// Register ourself as the listener
 		addKeyListener(this);
 	}
 
 	@Override
 	public void display(GLAutoDrawable drawable) {
 		Asteroids.update();
 		render(drawable);
 	}
 
 	@Override
 	public void dispose(GLAutoDrawable drawable) {
 		Asteroids.dispose();
 	}
 
 	@Override
 	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
 	}
 
 	private void render(GLAutoDrawable drawable) {
 		GL2 gl = drawable.getGL().getGL2();
 		
 		// Clear the buffer in case we don't draw in every position
 		// we won't have ghosting
 		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
 
 		// Render background
 		gl.glLoadIdentity();
 		gl.glBindTexture(GL.GL_TEXTURE_2D, Sprite.background().getTextureId());
 		gl.glTranslatef(0, 0, -1);
 		gl.glScalef(2, 2, 1);
 		// The Polygon for our background image to map a texture to
 		drawNormalSquare(gl);
 
 		// This loop renders all of our actors
 		for (int i = 0; i < Asteroids.actors.size(); i++) {
 			// Get the ith Actor
 			Actor actor = Asteroids.actors.get(i);
 
 			// Clear the Current matrix from the GL Stack to Identity so we aren't
 			// working with left over data from someone else
 			gl.glLoadIdentity();
 
 			// Bind our texture for this actor. This tells OpenGL which texture we want to use
 			// for the next glBegin()
 			gl.glBindTexture(GL.GL_TEXTURE_2D, actor.getSprite().getTextureId());
 
 			// Transformations are on a Stack so its "First In Last Out"
 			// Meaning we Translate, Rotate, Scale.
 			// OpenGL uses a +Y-axis up and +Z-axis out coordinate system.
 
 			// Translate our object to its position
 			gl.glTranslatef(actor.getPosition().x(), actor.getPosition().y(), 0.0f);
 			// Rotate it by its rotation about the Z axis
 			gl.glRotatef(actor.getTheta(),0,0,1);
 			// Scale our image by its size in the X and Y dimension
 			gl.glScalef(actor.getSize(), actor.getSize(), 1);
 			// Draw a Normal Square at the origin at will be transformed as
 			// described above in glScale,glRotate,glTranslate
 			// For our actor to map a texture to
 			drawNormalSquare(gl);  	
 		}
 	}
 	
 	/**
 	 * Draw a normalized square at the origin
 	 * @param gl - the OpenGL context
 	 */
 	private void drawNormalSquare(GL2 gl){
 		// These points will be multiplied by the transformations above
 		// to produce the desired and described transformations.
 		gl.glBegin(/* GL.GL_QUADS */ 7);//GL_QUADS isn't defined in the JOGL
 		    // Points must be counter clockwise defined or they will
 		    // be removed by back face culling
 		
 			// Bottom Left
			gl.glTexCoord2f(0, 0);
 			gl.glVertex2d(-0.5f, -0.5f);
 			// Bottom Right
			gl.glTexCoord2f(1, 0);
 			gl.glVertex2d(0.5f, -0.5f);
 			// Top Right
			gl.glTexCoord2f(1, 1);
 			gl.glVertex2d(0.5f, 0.5f);
 			// Top Left
			gl.glTexCoord2f(0, 1);
 			gl.glVertex2d(-0.5f, 0.5f);
 		gl.glEnd();
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		// TODO Auto-generated method stub
 		System.err.println("Key pressed");		
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// TODO Auto-generated method stub
 		System.err.println("Key pressed");	
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 		System.err.println("Key pressed");
 	}
 }
