 /**
  * References by http://www3.ntu.edu.sg/home/ehchua/programming/opengl/JOGL1.1.html
  * Written Kristof Friess
  */
 
 package mkpc.model3D;
 
 import java.awt.BorderLayout;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.media.opengl.*;
 import javax.media.opengl.glu.*;
 import javax.swing.JPanel;
 
 import mkpc.app.Application;
 import mkpc.comm.MKData3D;
 import mkpc.comm.MKParameter;
 import mkpc.log.LogSystem;
 
 import com.sun.opengl.util.FPSAnimator;
 
 
 enum MKCopterModel
 {
 	kCopterModelQuadro,
 	kCopterModelHexa,
 	kCopterModelOkto
 };
 
 public class MKopter3DView extends JPanel implements GLEventListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private static final int REFRESH_FPS = 30; // Display refresh frames per second
 	
 	private GLU glu; // For the GL Utility
 	final FPSAnimator animator; // Used to drive display()
 
 	static float anglePyramid = 0; // rotational angle in degree for pyramid
 	static float angleCube = 0; // rotational angle in degree for cube
 	static float speedPyramid = 2.0f; // rotational speed for pyramid
 	static float speedCube = -1.5f; // rotational speed for cube
 
 	static float rotateY = 0;
 	static float rotateZ = 0;
 	static float rotateX = 0;
 	
 	static float roll = 20;
 	static float yaw = 140;
 	static float nick = 20;
 	
 	public static float currectYaw = 0;
 	
 	private boolean isCoordianteLineHidden = true;
 	private MKCopterModel copterModel = null;
 	
 	// Constructor
 	public MKopter3DView() 
 	{
 		GLCanvas canvas = new GLCanvas();
 		this.setLayout(new BorderLayout());
 		this.add(canvas, BorderLayout.CENTER);
 		canvas.addGLEventListener(this);
 
 		// Run the animation loop using the fixed-rate Frame-per-second
 		// animator,
 		// which calls back display() at this fixed-rate (FPS).
 		animator = new FPSAnimator(canvas, REFRESH_FPS, true);
 	}
 
 	public void startAnimation()
 	{
 		Timer atimer = new Timer();
		atimer.schedule(new requestTask(), 100, 4000);
 		new Thread() {
 			@Override
 			public void run() {
 				animator.start(); // start the animation loop
 			}
 		}.start();
 	}
 	
 	public void stopAnimation()
 	{
 		new Thread() {
 			@Override
 			public void run() {
 				animator.stop(); // stop the animator loop
 			}
 		}.start();
 	}
 
 	// Implement methods defined in GLEventListener
 	@Override
 	public void init(GLAutoDrawable drawable) 
 	{
 		LogSystem.CLog("TEST");
 		GL gl = drawable.getGL();
 		glu = new GLU();
 		
 		gl.glShadeModel(GL.GL_SMOOTH);
 		gl.glClearColor(240f/255f, 240f/255f, 240f/255f, 1.0f);
 	    //gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
 		//gl.glClearDepth(5.0f);          // clear z-buffer to the farthest
 		//gl.glClearDepth(GL.GL_DEPTH_TEST);
 		gl.glEnable(GL.GL_DEPTH_TEST);
 		//gl.glDepthFunc(GL.GL_LEQUAL);
 		
 		gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
 	}
 
 	@Override
 	public void display(GLAutoDrawable drawable) {
 		this.render(drawable);
 		this.update();
 
 	}
 
 	@Override
 	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) 
 	{
 		// Get the OpenGL graphics context
 		GL gl = drawable.getGL();
 
 		height = (height == 0) ? 1 : height; // prevent divide by zero
 		float aspect = (float) width / height;
 
 		// Reset the current view port
 		gl.glViewport(0, 0, width, height);
 
 		// Set up the projection matrix - choose perspective view
 		gl.glMatrixMode(GL.GL_PROJECTION);
 		gl.glLoadIdentity(); // reset
 		
 		// Angle of view (fovy) is 45 degrees (in the up y-direction). Based on this
 		// canvas's aspect ratio. Clipping z-near is 0.1f and z-near is 100.0f.
 		glu.gluPerspective(50.0f, aspect, 0.1f, 100.0f); // fovy, aspect, zNear, zFar
 
 		// Enable the model-view transform
 		gl.glMatrixMode(GL.GL_MODELVIEW);
 		gl.glLoadIdentity(); // reset
 	}
 
 	@Override
 	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) 
 	{
 		// Not implemented in JOGL.
 	}
 	
 	/**
 	 * Draw the copter and show it.
 	 */
 	private void render(GLAutoDrawable drawable)
 	{
 		// Get the OpenGL graphics context
 		GL gl = drawable.getGL();
 		// Clear the color and the depth buffers
 		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
 		
 		gl.glLoadIdentity(); // reset the current model-view matrix
 		
 		gl.glLoadIdentity(); // reset the current model-view matrix
 		gl.glTranslatef(0.0f, 0.0f, -10.0f);
 		gl.glRotatef(rotateY, 0.0f, 1.0f, 0.0f); // rotate about the y axe
 		//this.createGrid(drawable, 30);
 		
 		if(!isCoordianteLineHidden)
 		{
 			// draw axes (x,y,z)
 			// y
 			gl.glBegin(GL.GL_LINES);
 				gl.glColor3ub( (byte)0, (byte)255, (byte)0);
 				gl.glVertex3f( 0.0f, 5.0f, 0.0f);	
 				gl.glVertex3f( 0.0f, 0.0f, 0.0f);	
 			gl.glEnd();
 
 			// x
 			gl.glBegin(GL.GL_LINES);
 				gl.glColor3ub( (byte)0, (byte)0, (byte)255);
 				gl.glVertex3f( 5.0f, 0.0f, 0.0f);	
 				gl.glVertex3f( 0.0f, 0.0f, 0.0f);	
 			gl.glEnd();
 
 			// z
 			gl.glBegin(GL.GL_LINES);
 				gl.glColor3ub( (byte)255, (byte)0, (byte)0);
 				gl.glVertex3f( 0.0f, 0.0f, 5.0f);	
 				gl.glVertex3f( 0.0f, 0.0f, 0.0f);	
 			gl.glEnd();
 		}
 		
 		if(copterModel == MKCopterModel.kCopterModelQuadro)
 		{
 			this.drawCopterWithArms(drawable, (short)4);
 		}
 		else if(copterModel == MKCopterModel.kCopterModelHexa)
 		{
 			this.drawCopterWithArms(drawable, (short)6);
 		}
 		else
 		{
 			this.drawOktoCopter(drawable);
 		}
 				
 		
 		
 	}
 	
 	
 	/**
 	 * Update variables for some animations.
 	 */
 	public boolean goON = false;
 	private void update()
 	{
 		// Update the rotational angle after each refresh.
 		
 		MKData3D data = MKParameter.shardParameter().getData3D();
 		
		roll = data.roll()/10;
		nick = -data.nick()/10;
		yaw = (-data.compass()/10)+currectYaw;
 		
 //		rotateY = rotateY%360 + 1;
 //		if(goON)
 //		{
 //			rotateZ += 1;
 //		}
 //		else
 //		{
 //			rotateZ -= 1;
 //		}
 //		
 //		if(rotateZ > 30)
 //		{
 //			goON = false;
 //		}
 //		
 //		if(rotateZ < -30)
 //		{
 //			goON = true;
 //		}
 //		
 //		roll = rotateZ;
 //		yaw =rotateY;
 	}
 
 	@SuppressWarnings("unused")
 	private void createGrid(GLAutoDrawable drawable, int size)
 	{
 		GL gl = drawable.getGL();
 		
 		for ( int x = -(size/2); x < (size/2); x++)
 		{
 			for ( int z = -(size/2); z < (size/2); z++)
 			{
 				gl.glBegin(GL.GL_LINE_LOOP);
 				gl.glColor3f( 1.0f, 1.0f, 1.0f);
 				gl.glVertex3f( x, 0.0f, z+1);	
 				gl.glVertex3f( x+1, 0.0f, z+1);		
 				gl.glVertex3f( x+1, 0.0f, z);		
 				gl.glVertex3f( x, 0.0f, z);	
 				gl.glEnd();
 			}
 		}
 	}
 	
 	private void drawCopterWithArms(GLAutoDrawable drawable, short armes)
 	{
 		GL gl = drawable.getGL();
 		
 		gl.glPushMatrix();
 		{	
 			gl.glRotatef(roll, 0.0f, 0.0f, 1.0f); // rotate about the z axe
 			gl.glRotatef(yaw, 0.0f, 1.0f, 0.0f); // rotate about the y axe
 			gl.glRotatef(nick, 1.0f, 0.0f, 0.0f); // rotate about the x axe
 			
 			gl.glPushMatrix();
 			{
 				gl.glScalef(1.0f, 0.6f, 1.0f);
 				gl.glColor3f(1.0f, 0.0f, 0.0f);
 				
 				GLUquadric gobj = glu.gluNewQuadric();
 				glu.gluQuadricDrawStyle(gobj, GLU.GLU_FILL);
 				glu.gluSphere(gobj, 1.0f, 10, 5);
 				glu.gluDeleteQuadric(gobj);
 			}
 			gl.glPopMatrix();
 			
 			for(int i = 0; i < armes; i++)
 			{
 				gl.glPushMatrix();
 				{
 					// arm
 					gl.glRotatef(i*(360f/armes), 0.0f, 1.0f, 0.0f);
 					gl.glTranslatef(0.0f, 0.0f, -3.0f);
 					if(i == 0)
 					{
 						gl.glColor3f(1.0f, 0.0f, 0.0f);
 					}
 					else
 					{
 						gl.glColor3f(0.1f, 0.1f, 0.1f);
 					}
 					
 					GLUquadric gobj = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj, GLU.GLU_FILL);
 					glu.gluCylinder(gobj, 0.1f, 0.1f, 3.0f, 10, 5);
 					glu.gluDeleteQuadric(gobj);
 					
 					// motor
 					gl.glTranslatef(0.0f, 0.3f, 0.1f);
 					gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
 					gl.glColor3f(0.6f, 0.0f, 0.0f);
 					
 					GLUquadric gobj0 = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj0, GLU.GLU_FILL);
 					glu.gluCylinder(gobj0, 0.15f, 0.15f, 0.3f, 10, 5);
 					glu.gluDeleteQuadric(gobj0);
 					
 				}
 				gl.glPopMatrix();
 			}
 		}
 		gl.glPopMatrix();
 	}
 	
 	private void drawOktoCopter(GLAutoDrawable drawable)
 	{
 		GL gl = drawable.getGL();
 		
 		gl.glPushMatrix();
 		{	
 			
 			gl.glRotatef(roll, 0.0f, 0.0f, 1.0f); // rotate about the z axe
 			gl.glRotatef(yaw, 0.0f, 1.0f, 0.0f); // rotate about the y axe
 			gl.glRotatef(nick, 1.0f, 0.0f, 0.0f); // rotate about the x axe
 			
 			gl.glPushMatrix();
 			{
 				gl.glScalef(1.0f, 0.6f, 1.0f);
 				gl.glColor3f(1.0f, 0.0f, 0.0f);
 				
 				GLUquadric gobj = glu.gluNewQuadric();
 				glu.gluQuadricDrawStyle(gobj, GLU.GLU_FILL);
 				glu.gluSphere(gobj, 1.0f, 10, 5);
 				glu.gluDeleteQuadric(gobj);
 			}
 			gl.glPopMatrix();
 			
 			
 			// front arm
 			gl.glPushMatrix();
 			{
 				// middle way
 				gl.glPushMatrix();
 				{
 					gl.glTranslatef(0.0f, 0.0f, -2.25f);
 					gl.glColor3f(1.0f, 0.0f, 0.0f);
 					
 					GLUquadric gobj = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj, GLU.GLU_FILL);
 					glu.gluCylinder(gobj, 0.1f, 0.1f, 2.0f, 10, 5);
 					glu.gluDeleteQuadric(gobj);
 				}
 				gl.glPopMatrix();
 				// middle left
 				gl.glPushMatrix();
 				{
 					// arm
 					gl.glTranslatef(-0.7f, 0.0f, -3.0f);
 					gl.glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
 					gl.glTranslatef(0.0f, 0.0f, -1.0f);
 					gl.glColor3f(1.0f, 0.0f, 0.0f);
 					
 					GLUquadric gobj = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj, GLU.GLU_FILL);
 					glu.gluCylinder(gobj, 0.1f, 0.1f, 2.0f, 10, 5);
 					glu.gluDeleteQuadric(gobj);
 				
 					// motor
 					gl.glTranslatef(0.0f, 0.3f, 0.1f);
 					gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
 					gl.glColor3f(0.6f, 0.0f, 0.0f);
 					
 					GLUquadric gobj0 = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj0, GLU.GLU_FILL);
 					glu.gluCylinder(gobj0, 0.15f, 0.15f, 0.3f, 10, 5);
 					glu.gluDeleteQuadric(gobj0);
 				}
 				gl.glPopMatrix();
 				// middle right
 				gl.glPushMatrix();
 				{
 					// arm
 					gl.glTranslatef(0.7f, 0.0f, -3.0f);
 					gl.glRotatef(-45.0f, 0.0f, 1.0f, 0.0f);
 					gl.glTranslatef(0.0f, 0.0f, -1.0f);
 					gl.glColor3f(1.0f, 0.0f, 0.0f);
 					
 					GLUquadric gobj = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj, GLU.GLU_FILL);
 					glu.gluCylinder(gobj, 0.1f, 0.1f, 2.0f, 10, 5);
 					glu.gluDeleteQuadric(gobj);
 					
 					// motor
 					gl.glTranslatef(0.0f, 0.3f, 0.1f);
 					gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
 					gl.glColor3f(0.6f, 0.0f, 0.0f);
 					
 					GLUquadric gobj0 = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj0, GLU.GLU_FILL);
 					glu.gluCylinder(gobj0, 0.15f, 0.15f, 0.3f, 10, 5);
 					glu.gluDeleteQuadric(gobj0);
 					
 				}
 				gl.glPopMatrix();
 			}
 			gl.glPopMatrix();
 			
 			// right arm
 			gl.glPushMatrix();
 			{
 				gl.glRotatef(-90.0f, 0.0f, 1.0f, 0.0f);
 				// middle way
 				gl.glPushMatrix();
 				{
 					gl.glTranslatef(0.0f, 0.0f, -2.25f);
 					gl.glColor3f(0.1f, 0.1f, 0.1f);
 					
 					GLUquadric gobj = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj, GLU.GLU_FILL);
 					glu.gluCylinder(gobj, 0.1f, 0.1f, 2.0f, 10, 5);
 					glu.gluDeleteQuadric(gobj);
 				}
 				gl.glPopMatrix();
 				// middle left
 				gl.glPushMatrix();
 				{
 					// arm
 					gl.glTranslatef(-0.7f, 0.0f, -3.0f);
 					gl.glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
 					gl.glTranslatef(0.0f, 0.0f, -1.0f);
 					gl.glColor3f(0.1f, 0.1f, 0.1f);
 					
 					GLUquadric gobj = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj, GLU.GLU_FILL);
 					glu.gluCylinder(gobj, 0.1f, 0.1f, 2.0f, 10, 5);
 					glu.gluDeleteQuadric(gobj);
 				
 					// motor
 					gl.glTranslatef(0.0f, 0.3f, 0.1f);
 					gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
 					gl.glColor3f(0.6f, 0.0f, 0.0f);
 					
 					GLUquadric gobj0 = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj0, GLU.GLU_FILL);
 					glu.gluCylinder(gobj0, 0.15f, 0.15f, 0.3f, 10, 5);
 					glu.gluDeleteQuadric(gobj0);
 				}
 				gl.glPopMatrix();
 				// middle right
 				gl.glPushMatrix();
 				{
 					// arm
 					gl.glTranslatef(0.7f, 0.0f, -3.0f);
 					gl.glRotatef(-45.0f, 0.0f, 1.0f, 0.0f);
 					gl.glTranslatef(0.0f, 0.0f, -1.0f);
 					gl.glColor3f(0.1f, 0.1f, 0.1f);
 					
 					GLUquadric gobj = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj, GLU.GLU_FILL);
 					glu.gluCylinder(gobj, 0.1f, 0.1f, 2.0f, 10, 5);
 					glu.gluDeleteQuadric(gobj);
 					
 					// motor
 					gl.glTranslatef(0.0f, 0.3f, 0.1f);
 					gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
 					gl.glColor3f(0.6f, 0.0f, 0.0f);
 					
 					GLUquadric gobj0 = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj0, GLU.GLU_FILL);
 					glu.gluCylinder(gobj0, 0.15f, 0.15f, 0.3f, 10, 5);
 					glu.gluDeleteQuadric(gobj0);
 					
 				}
 				gl.glPopMatrix();
 			}
 			gl.glPopMatrix();
 			
 			// left arm
 			gl.glPushMatrix();
 			{
 				gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
 				// middle way
 				gl.glPushMatrix();
 				{
 					gl.glTranslatef(0.0f, 0.0f, -2.25f);
 					gl.glColor3f(0.1f, 0.1f, 0.1f);
 					
 					GLUquadric gobj = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj, GLU.GLU_FILL);
 					glu.gluCylinder(gobj, 0.1f, 0.1f, 2.0f, 10, 5);
 					glu.gluDeleteQuadric(gobj);
 				}
 				gl.glPopMatrix();
 				// middle left
 				gl.glPushMatrix();
 				{
 					// arm
 					gl.glTranslatef(-0.7f, 0.0f, -3.0f);
 					gl.glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
 					gl.glTranslatef(0.0f, 0.0f, -1.0f);
 					gl.glColor3f(0.1f, 0.1f, 0.1f);
 					
 					GLUquadric gobj = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj, GLU.GLU_FILL);
 					glu.gluCylinder(gobj, 0.1f, 0.1f, 2.0f, 10, 5);
 					glu.gluDeleteQuadric(gobj);
 				
 					// motor
 					gl.glTranslatef(0.0f, 0.3f, 0.1f);
 					gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
 					gl.glColor3f(0.6f, 0.0f, 0.0f);
 					
 					GLUquadric gobj0 = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj0, GLU.GLU_FILL);
 					glu.gluCylinder(gobj0, 0.15f, 0.15f, 0.3f, 10, 5);
 					glu.gluDeleteQuadric(gobj0);
 				}
 				gl.glPopMatrix();
 				// middle right
 				gl.glPushMatrix();
 				{
 					// arm
 					gl.glTranslatef(0.7f, 0.0f, -3.0f);
 					gl.glRotatef(-45.0f, 0.0f, 1.0f, 0.0f);
 					gl.glTranslatef(0.0f, 0.0f, -1.0f);
 					gl.glColor3f(0.1f, 0.1f, 0.1f);
 					
 					GLUquadric gobj = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj, GLU.GLU_FILL);
 					glu.gluCylinder(gobj, 0.1f, 0.1f, 2.0f, 10, 5);
 					glu.gluDeleteQuadric(gobj);
 					
 					// motor
 					gl.glTranslatef(0.0f, 0.3f, 0.1f);
 					gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
 					gl.glColor3f(0.6f, 0.0f, 0.0f);
 					
 					GLUquadric gobj0 = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj0, GLU.GLU_FILL);
 					glu.gluCylinder(gobj0, 0.15f, 0.15f, 0.3f, 10, 5);
 					glu.gluDeleteQuadric(gobj0);
 					
 				}
 				gl.glPopMatrix();
 			}
 			gl.glPopMatrix();
 			
 			// backarm arm
 			gl.glPushMatrix();
 			{
 				gl.glRotatef(180.0f, 0.0f, 1.0f, 0.0f);
 				// middle way
 				gl.glPushMatrix();
 				{
 					gl.glTranslatef(0.0f, 0.0f, -2.25f);
 					gl.glColor3f(0.1f, 0.1f, 0.1f);
 					
 					GLUquadric gobj = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj, GLU.GLU_FILL);
 					glu.gluCylinder(gobj, 0.1f, 0.1f, 2.0f, 10, 5);
 					glu.gluDeleteQuadric(gobj);
 				}
 				gl.glPopMatrix();
 				// middle left
 				gl.glPushMatrix();
 				{
 					// arm
 					gl.glTranslatef(-0.7f, 0.0f, -3.0f);
 					gl.glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
 					gl.glTranslatef(0.0f, 0.0f, -1.0f);
 					gl.glColor3f(0.1f, 0.1f, 0.1f);
 					
 					GLUquadric gobj = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj, GLU.GLU_FILL);
 					glu.gluCylinder(gobj, 0.1f, 0.1f, 2.0f, 10, 5);
 					glu.gluDeleteQuadric(gobj);
 				
 					// motor
 					gl.glTranslatef(0.0f, 0.3f, 0.1f);
 					gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
 					gl.glColor3f(0.6f, 0.0f, 0.0f);
 					
 					GLUquadric gobj0 = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj0, GLU.GLU_FILL);
 					glu.gluCylinder(gobj0, 0.15f, 0.15f, 0.3f, 10, 5);
 					glu.gluDeleteQuadric(gobj0);
 				}
 				gl.glPopMatrix();
 				// middle right
 				gl.glPushMatrix();
 				{
 					// arm
 					gl.glTranslatef(0.7f, 0.0f, -3.0f);
 					gl.glRotatef(-45.0f, 0.0f, 1.0f, 0.0f);
 					gl.glTranslatef(0.0f, 0.0f, -1.0f);
 					gl.glColor3f(0.1f, 0.1f, 0.1f);
 					
 					GLUquadric gobj = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj, GLU.GLU_FILL);
 					glu.gluCylinder(gobj, 0.1f, 0.1f, 2.0f, 10, 5);
 					glu.gluDeleteQuadric(gobj);
 					
 					// motor
 					gl.glTranslatef(0.0f, 0.3f, 0.1f);
 					gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
 					gl.glColor3f(0.6f, 0.0f, 0.0f);
 					
 					GLUquadric gobj0 = glu.gluNewQuadric();
 					glu.gluQuadricDrawStyle(gobj0, GLU.GLU_FILL);
 					glu.gluCylinder(gobj0, 0.15f, 0.15f, 0.3f, 10, 5);
 					glu.gluDeleteQuadric(gobj0);
 					
 				}
 				gl.glPopMatrix();
 			}
 			gl.glPopMatrix();
 		}
 		gl.glPopMatrix();
 	}
 	
 	public void setCoordianteLineHidden(boolean isCoordianteLineHidden) 
 	{
 		this.isCoordianteLineHidden = isCoordianteLineHidden;
 	}
 
 	public boolean isCoordianteLineHidden() 
 	{
 		return isCoordianteLineHidden;
 	}
 
 	public void setCopterModel(MKCopterModel copterModel) {
 		this.copterModel = copterModel;
 	}
 
 	public MKCopterModel getCopterModel() {
 		return copterModel;
 	}
 	
 	class requestTask extends TimerTask
 	{
 
 		@Override
 		public void run() {
 			char[] value = new char[1];
			value[0] = 10;
 			Application.sharedApplication().serialComm.sendCommand('c', 'c', value);
 		}
 		
 	}
 }
