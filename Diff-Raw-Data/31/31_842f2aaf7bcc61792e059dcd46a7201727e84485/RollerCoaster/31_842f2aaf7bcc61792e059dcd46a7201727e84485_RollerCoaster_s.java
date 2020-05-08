 //A simulation of first person view on a roller coaster
 //Authors: Daisy Zhuo, Gareth Aye
 //Credit: Christopher Twigg (Carnegie Mellon University) project instruction on:
 //http://graphics.cs.cmu.edu/nsp/course/15-462/Fall07/462/assts/Assn2.html
 
 package roller_coaster;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.logging.Logger;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.GLProfile;
 import javax.media.opengl.glu.GLU;
 
 import roller_coaster.Parser;
 
 import com.jogamp.opengl.util.gl2.GLUT;
 import com.jogamp.opengl.util.texture.Texture;
 import com.jogamp.opengl.util.texture.TextureIO;
 import com.jogamp.newt.event.KeyAdapter;
 import com.jogamp.newt.event.KeyEvent;
 import com.jogamp.newt.event.MouseEvent;
 import com.jogamp.newt.event.MouseListener;
 import com.jogamp.newt.event.WindowAdapter;
 import com.jogamp.newt.event.WindowEvent;
 import com.jogamp.newt.opengl.GLWindow;
 import com.jogamp.opengl.util.FPSAnimator;
 
 
 public class RollerCoaster implements GLEventListener {
 	private static final Logger LOGGER = Logger.getLogger(RollerCoaster.class.getName());
 	private static final int WIDTH = 800;
 	private static final int HEIGHT = 600;
 	private static final int X0 = 100;
 	private static final int Y0 = 50;
 	private static final int REFRESH_RATE = 30;
 	private static final int NEAR = 1;
 	private static final int FAR = 25;
	private static int lightMode = 3;
 	
 	private double dtheta = 0.008;
 	private double camDist = -3;
     private double camPhi = 0;    // horizontal (azimuth) angle
     private double camTheta = 0;  // vertical (elevation) angle
     private boolean shiftKeyDown = false;
     private int mouseDownx, mouseDowny;
     private double mouseDownDist, mouseDownPhi, mouseDownTheta;
     
 	private FPSAnimator animator;
 	private GLWindow window;
 	private GLU glu;
 	private GLUT glut;
 	private SpeedProvider speedProvider;
 	
 	private Texture groundTexture;
 	private Texture railTexture;
 	private Texture[] skyTexture = new Texture[5];
 	
 	private int pos = 0;
 	private int num_rails = 800;
 	private Point3[] centerPos = new Point3[num_rails];
 	
 	private float aspect = 1;
 	
 	public RollerCoaster() {
         glu = new GLU();
         glut = new GLUT();
         speedProvider = new ConstantSpeedProvider().setSpeed(1);
 
         for (int i = 0; i < num_rails; i++) {
         	centerPos[i] = new Point3((400-i)/20.0*Math.sin(dtheta*i), 2*Math.sin(i/20.0)+2, (400-i)/20.0*Math.cos(dtheta*i));
 //        }
 //        for (int i = num_rails/4; i < num_rails; i++) {
 //        	centerPos[i] = new Point3(Math.atan(-(100-i)/20.0)*2, 3*Math.sin(i/20.0)+3, (100-i)/20.0);
         	//centerPos[i] = new Point3(10-i/10.0, Math.sin((-i/10.0)*(-i/10.0)-100)+1, (-i/10.0)*(-i/10.0)-100);
         }
         
 		window = GLWindow.create(new GLCapabilities(GLProfile.getDefault()));
         window.setSize(WIDTH, HEIGHT);
         window.setVisible(true);
         window.setTitle("RollerCoaster");
         window.setPosition(X0, Y0);
         window.addGLEventListener(this);
         window.addWindowListener(new WindowAdapter() {
             public void windowDestroyNotify(WindowEvent arg0) {
                 LOGGER.info("Terminating...");
             	System.exit(0);
             }
         });
         
         window.addKeyListener(new KeyAdapter() {
             public void keyPressed(KeyEvent ke){
                 switch(ke.getKeyCode()) {
                 case KeyEvent.VK_ESCAPE:
                 case KeyEvent.VK_Q:
                     System.exit(0);
                 case KeyEvent.VK_SHIFT:
                     shiftKeyDown = true; break;
                 case KeyEvent.VK_A:
                    dtheta += 0.01; break;
                 case KeyEvent.VK_UP:
                     camDist += shiftKeyDown ? 1 : .2; break;
                 case KeyEvent.VK_DOWN:
                     camDist -= shiftKeyDown ? 1 : .2; break;
                 case KeyEvent.VK_LEFT:
                     camPhi -= 5; break;
                 case KeyEvent.VK_RIGHT:
                     camPhi += 5; break;
                 case KeyEvent.VK_SPACE:
                    camDist = -3; camPhi = 0; camTheta = 0; break;
                 }
                 //System.out.printf("camDist=%.2g  camTheta=%d ortho=%s\n", camDist, camTheta, useOrtho);
             }
             public void keyReleased(KeyEvent ke){
                 switch(ke.getKeyCode()) {
                 case KeyEvent.VK_SHIFT:
                     shiftKeyDown = false; break;
                 }
             }
         });
         window.addMouseListener(new MouseListener() {
             public void mousePressed(MouseEvent e) {
                 mouseDownx = e.getX();
                 mouseDowny = e.getY();      
                 mouseDownDist = camDist;
                 mouseDownPhi = camPhi;
                 mouseDownTheta = camTheta;
             }
             public void mouseDragged(MouseEvent e) {
                 int dx =   e.getX() - mouseDownx;
                 int dy = -(e.getY() - mouseDowny);  // screen coords are upside down
                 //System.out.printf("moved mouse by %d, %d\n", dx, dy);
                 if (shiftKeyDown) { // mouse controls zoom
                     double zoomFactor = 0.05;
                     camDist = mouseDownDist + zoomFactor * dy;
                 } else { // mouse controls azimuth / elevation
                     double panFactor = 0.5;
                     camPhi = mouseDownPhi + panFactor * dx;
                     camTheta = Math.min(89, Math.max(-89, mouseDownTheta + panFactor * dy));
                 }
             }
             public void mouseClicked(MouseEvent e) {}
             public void mouseEntered(MouseEvent e) {}
             public void mouseExited(MouseEvent e) {}
             public void mouseReleased(MouseEvent e) {}
             public void mouseMoved(MouseEvent e) {}
             public void mouseWheelMoved(MouseEvent e) {}
         });
         animator = new FPSAnimator(window, REFRESH_RATE);
         animator.add(window);
         animator.start();
 	}
 
 	@Override
 	public void init(GLAutoDrawable drawable) {
         GL2 gl = drawable.getGL().getGL2();
         gl = drawable.getGL().getGL2();
         //gl.glEnable(GL2.GL_DEPTH_TEST);     // Enables Depth Testing
         gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);    // draw front faces filled
         gl.glPolygonMode(GL2.GL_BACK, GL2.GL_LINES);    // draw back faces with lines
         groundTexture = loadTexture("./roller_coaster/data/ground.jpg");
         skyTexture[0] = loadTexture("./roller_coaster/data/otop7.jpg");
         skyTexture[1] = loadTexture("./roller_coaster/data/oleft7.jpg");
         skyTexture[2] = loadTexture("./roller_coaster/data/oback7.jpg");
         skyTexture[3] = loadTexture("./roller_coaster/data/oright7.jpg");
         skyTexture[4] = loadTexture("./roller_coaster/data/ofront7.jpg");
         railTexture = loadTexture("./roller_coaster/data/wood3.jpg");
         if(groundTexture == null)
         	System.exit(0);
         groundTexture.enable(gl);
         for (int i = 0; i < 5; i++){
         	skyTexture[i].enable(gl);
         }
 	}
 
 	@Override
 	public void dispose(GLAutoDrawable drawable) {
 		LOGGER.info("Getting rid of object references...");
 		animator = null;
 		window = null;
 		speedProvider = null;
 	}
 
 	@Override
 	public void display(GLAutoDrawable drawable) {
 		update();
 		render(drawable);
 	}
 	
 	public void render(GLAutoDrawable drawable) {
         GL2 gl = drawable.getGL().getGL2();
         gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);   
         gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
         gl.glMatrixMode(GL2.GL_MODELVIEW);
         gl.glLoadIdentity();
 
         setLights(gl); // positions lights
         setCam(gl);  // position camera
 
     	gl.glDisable(GL2.GL_DEPTH_TEST);
         drawSkybox(gl);
     	gl.glEnable(GL2.GL_DEPTH_TEST);
         
     	drawGround(gl);
         
 		for (int i = 1; i < num_rails; i++) {
 			gl.glPushMatrix();
 			gl.glTranslated(centerPos[i].getX(), centerPos[i].getY(), centerPos[i].getZ());
 			drawRail(gl);
 			gl.glPopMatrix();
 		}
 		drawBox(gl);
 	}
 	
     public void drawRail(GL2 gl) {
 
         railTexture.bind(gl);
         gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT); 
         gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
         for (int i = 1; i < Parser.faces.length; i++){
         	gl.glPushMatrix();
         	gl.glBegin(GL2.GL_QUADS);
        	gl.glColor3i(255, 166, 30);
         	gl.glNormal3f(Parser.normals[Parser.faces[i].a_n].nx, Parser.normals[Parser.faces[i].a_n].ny, Parser.normals[Parser.faces[i].a_n].nz);
        	
         	gl.glTexCoord2f(0.0f, 0.0f); 
         	gl.glVertex3f(Parser.vertices[Parser.faces[i].a].x,Parser.vertices[Parser.faces[i].a].y, Parser.vertices[Parser.faces[i].a].z ); 
             gl.glTexCoord2f(1.0f, 0.0f); 
             gl.glVertex3f(Parser.vertices[Parser.faces[i].b].x,Parser.vertices[Parser.faces[i].b].y, Parser.vertices[Parser.faces[i].b].z ); 
             gl.glTexCoord2f(1.0f, 1.0f); 
             gl.glVertex3f(Parser.vertices[Parser.faces[i].c].x,Parser.vertices[Parser.faces[i].c].y, Parser.vertices[Parser.faces[i].c].z ); 
             gl.glTexCoord2f(0.0f, 1.0f); 
             gl.glVertex3f(Parser.vertices[Parser.faces[i].d].x,Parser.vertices[Parser.faces[i].d].y, Parser.vertices[Parser.faces[i].d].z ); 
             
         	//gl.glRotated(90, 0, 0, 0);
             //gl.glScaled(0.5d, 0.5d, 0.5d);
         	gl.glEnd();
         	gl.glPopMatrix();
         	if (Parser.faces[i+1] == null) break;
         }
 
     }
     
     private void drawBox(GL2 gl) {
     	gl.glPushMatrix();
 		gl.glTranslated(centerPos[(pos+10)%num_rails].getX(), centerPos[(pos+10)%num_rails].getY()+0.2f, centerPos[(pos+10)%num_rails].getZ());
     	glut.glutSolidTeapot(0.2f);
     	gl.glPopMatrix();
     }
 	
     private void drawGround(GL2 gl) { 
         groundTexture.bind(gl);
         gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT); 
         gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
         gl.glBegin(GL2.GL_QUADS);
             gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(20.0f, 0.0f,  -20.0f);  // Bottom Left Of The Texture and Quad
             gl.glTexCoord2f(10.0f, 0.0f); gl.glVertex3f(-20.0f, 0.0f,  -20.0f);  // Bottom Right Of The Texture and Quad
             gl.glTexCoord2f(10.0f, 10.0f); gl.glVertex3f(-20.0f,  0.0f,  20.0f);  // Top Right Of The Texture and Quad
             gl.glTexCoord2f(0.0f, 10.0f); gl.glVertex3f(20.0f,  0.0f,  20.0f);  // Top Left Of The Texture and Quad
         gl.glEnd();
     }
     
     private void drawSkybox(GL2 gl) {
     	gl.glPushMatrix();
     	gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
     	//draw top
     	skyTexture[0].bind(gl);
         gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE); 
         gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
         gl.glBegin(GL2.GL_QUADS);
         gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-20f, 20f,  -20f);  // Bottom Left Of The Texture and Quad
         gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(20f, 20f,  -20f);  // Bottom Right Of The Texture and Quad
         gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(20f, 20f,  20f);  // Top Right Of The Texture and Quad
         gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-20f, 20f, 20f);  // Top Left Of The Texture and Quad
 	    gl.glEnd();
 		
 		//draw right
 		skyTexture[3].bind(gl);
 	    gl.glBegin(GL2.GL_QUADS);
 	        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-20f, 0f,  20f);  // Bottom Left Of The Texture and Quad
 	        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-20f, 0f,  -20f);  // Bottom Right Of The Texture and Quad
 	        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-20f, 20f,  -20f);  // Top Right Of The Texture and Quad
 	        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-20f, 20f,  20f);  // Top Left Of The Texture and Quad
 	    gl.glEnd();
 		
 		//draw back
 		skyTexture[2].bind(gl);
 	    gl.glBegin(GL2.GL_QUADS);
 	        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(20f, 0f,  20f);  // Bottom Left Of The Texture and Quad
 	        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-20f, 0f,  20f);  // Bottom Right Of The Texture and Quad
 	        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-20f, 20f,  20f);  // Top Right Of The Texture and Quad
 	        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(20f, 20f,  20f);  // Top Left Of The Texture and Quad
 	    gl.glEnd();
 	    
 		//draw left
 	    skyTexture[1].bind(gl);
 	    gl.glBegin(GL2.GL_QUADS);
 	        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(20f, 0f,  -20f);  // Bottom Left Of The Texture and Quad
 	        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(20f, 0f,  20f);  // Bottom Right Of The Texture and Quad
 	        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(20f, 20f,  20f);  // Top Right Of The Texture and Quad
 	        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(20f, 20f,  -20f);  // Top Left Of The Texture and Quad
 	    gl.glEnd();
 	    
 		//draw front quad
 	    skyTexture[4].bind(gl);
 	    gl.glBegin(GL2.GL_QUADS);
 	        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-20f, 0f,  -20f);  // Bottom Left Of The Texture and Quad
 	        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(20f, 0f,  -20f);  // Bottom Right Of The Texture and Quad
 	        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(20f, 20f,  -20f);  // Top Right Of The Texture and Quad
 	        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-20f, 20f,  -20f);  // Top Left Of The Texture and Quad
 	    gl.glEnd();
 		gl.glPopMatrix();
     }
     
     private Texture loadTexture(String string) {
         Texture texture = null;
         File imgFile = null;
         
         try {
             URL url = this.getClass().getClassLoader().getResource(string); 
             imgFile = new File(url.getFile());
             texture = TextureIO.newTexture(imgFile, true);
         } 
         catch (IOException e) {
             System.out.println("fail openning file... " + imgFile.getName());
             System.out.println(e);
         }
         
         return texture;
     }    
     
     
 	@Override
 	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
 			int height) {
         GL2 gl = drawable.getGL().getGL2();
 
         // avoid a divide by zero error!
         if (height <= 0) { 
         	height = 1;
         }
 
         gl.glViewport(0, 0, width, height);
         aspect = width / (float) height;
         setCam(gl);
 	}
 	
 	private void setCam(GL2 gl) {
         // set projection matrix
         gl.glMatrixMode(GL2.GL_PROJECTION);
         gl.glLoadIdentity();
         
         glu.gluPerspective(45.0f, aspect, NEAR, FAR);
         
         // set camera location and angle
         gl.glMatrixMode(GL2.GL_MODELVIEW);
         glu.gluLookAt(centerPos[pos].getX(), centerPos[pos].getY()+1, centerPos[pos].getZ(), 
         		centerPos[(pos+20)%num_rails].getX(), centerPos[(pos+15)%num_rails].getY(), centerPos[(pos+10)%num_rails].getZ(), 
        		0, 1, 0);
     }
 	
     public void setLights(GL2 gl) {
         float[] lightPos = {-2, 15, -2, 1};
        float[] lightAmbient = {0.2f, 0.2f, 0.2f, 1};
         float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1};
         float[] lightSpecular = {0.8f, 0.8f, 0.8f, 1};
 
         gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPos, 0);
         gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightAmbient, 0);
         gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, lightDiffuse, 0);
         gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, lightSpecular, 0);
 
         float[] lightPos2 = {5, 10, 5, 1};
         gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_POSITION, lightPos2, 0);
         gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_AMBIENT, lightAmbient, 0);
         gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_DIFFUSE, lightDiffuse, 0);
         gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_SPECULAR, lightSpecular, 0);
 
         float[] lightPos3 = {0, 5, 0, 1};
         gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_POSITION, lightPos3, 0);
         gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_AMBIENT, lightAmbient, 0);
         gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_DIFFUSE, lightDiffuse, 0);
         gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_SPECULAR, lightSpecular, 0);
 
         gl.glEnable(GL2.GL_LIGHTING); 
         gl.glDisable(GL2.GL_LIGHT0);
         gl.glEnable(GL2.GL_LIGHT1);
         gl.glEnable(GL2.GL_LIGHT2);
         gl.glEnable(GL2.GL_LIGHT3);
     }
 	
     private void update() {
		pos += speedProvider.getSpeed(this);
 		pos %= num_rails;
 	}
 	
 	public static void main(String[] args) throws FileNotFoundException {
         new Parser();
 		LOGGER.info("Starting scene rendering...");
 		new RollerCoaster();
 	}
 
 }
