 package test.applet;
 
 import java.applet.Applet;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.Insets;
 import java.awt.Rectangle;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.nio.FloatBuffer;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2ES2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.GLProfile;
 import javax.media.opengl.GLUniformData;
 import javax.media.opengl.awt.GLCanvas;
 
 import com.jogamp.newt.awt.NewtCanvasAWT;
 import com.jogamp.newt.opengl.GLWindow;
 import com.jogamp.opengl.util.FPSAnimator;
 import com.jogamp.opengl.util.GLArrayDataServer;
 import com.jogamp.opengl.util.glsl.ShaderCode;
 import com.jogamp.opengl.util.glsl.ShaderProgram;
 import com.jogamp.opengl.util.glsl.ShaderState;
 
 @SuppressWarnings("serial")
 public class AnimApplet extends Applet {
   static public int AWT  = 0;
   static public int NEWT = 1;
   
   static public int APPLET_WIDTH  = 500;
   static public int APPLET_HEIGHT = 290;
   static public int TARGET_FPS    = 120;
   static public int TOOLKIT       = NEWT;
   
   //////////////////////////////////////////////////////////////////////////////
   
   static private Frame frame;
   static private AnimApplet applet;
   private GLCanvas awtCanvas;
   private GLWindow newtWindow;
   private NewtCanvasAWT newtCanvas;
   private DrawListener drawListener;
   private FPSAnimator animator;
   
   private int width;
   private int height;
   
   private long millisOffset;
   private int frameCount;
   private float frameRate;
   
   private ShaderCode vertShader;
   private ShaderCode fragShader;
   private ShaderProgram shaderProg;
   private ShaderState shaderState;
   private GLUniformData resolution;
   private GLUniformData time;  
   private GLArrayDataServer vertices;   
   
   private int fcount = 0, lastm = 0;  
   private int fint = 1;
   
   public void init() {
     setSize(APPLET_WIDTH, APPLET_HEIGHT);
     setPreferredSize(new Dimension(APPLET_WIDTH, APPLET_HEIGHT));
     width = APPLET_WIDTH;
     height = APPLET_HEIGHT;
   }
   
   public void start() {
     if (TOOLKIT == AWT) {
       animator = new FPSAnimator(awtCanvas, TARGET_FPS);
       animator.start();      
     } else if (TOOLKIT == AWT) {
       animator = new FPSAnimator(newtWindow, TARGET_FPS);
       animator.start();      
     }
   }
   
   private class DrawListener implements GLEventListener {
     public void display(GLAutoDrawable drawable) {
       draw(drawable.getGL().getGL2ES2());
     }
     public void dispose(GLAutoDrawable drawable) { }
     public void init(GLAutoDrawable drawable) { 
       setup(drawable.getGL().getGL2ES2());
     }
     public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) { }    
   }  
   
    private void initGL() {
     GLProfile profile = GLProfile.getDefault();
     GLCapabilities caps = new GLCapabilities(profile);
     caps.setBackgroundOpaque(true);
     caps.setOnscreen(true);
     caps.setSampleBuffers(false);
     
     drawListener = new DrawListener();
     if (TOOLKIT == AWT) {
       awtCanvas = new GLCanvas(caps);
       awtCanvas.setBounds(0, 0, applet.width, applet.height);
       awtCanvas.setBackground(new Color(0xFFCCCCCC, true));
       awtCanvas.setFocusable(true); 
       
       applet.setLayout(new BorderLayout());
       applet.add(awtCanvas, BorderLayout.CENTER);
       
       awtCanvas.addGLEventListener(drawListener);      
     } else if (TOOLKIT == NEWT) {      
       newtWindow = GLWindow.create(caps);      
       newtCanvas = new NewtCanvasAWT(newtWindow);
       newtCanvas.setBounds(0, 0, applet.width, applet.height);
       newtCanvas.setBackground(new Color(0xFFCCCCCC, true));
       newtCanvas.setFocusable(true);
 
       applet.setLayout(new BorderLayout());
       applet.add(newtCanvas, BorderLayout.CENTER);
 
       newtWindow.addGLEventListener(drawListener);
     }
   }
   
   private void setup(GL2ES2 gl) {
     vertShader = ShaderCode.create(gl, GL2ES2.GL_VERTEX_SHADER, this.getClass(), "shaders",
         "shaders/bin", "landscape", true);
     fragShader = ShaderCode.create(gl, GL2ES2.GL_FRAGMENT_SHADER, this.getClass(), "shaders",
         "shaders/bin", "landscape", true);
     shaderProg = new ShaderProgram();
     shaderProg.add(gl, vertShader, System.err);
     shaderProg.add(gl, fragShader, System.err); 
     
     shaderState = new ShaderState();
     shaderState.attachShaderProgram(gl, shaderProg, true);
     
     resolution = new GLUniformData("iResolution", 3, FloatBuffer.wrap(new float[] {width, height, 0}));
     shaderState.ownUniform(resolution);
     shaderState.uniform(gl, resolution);    
     
     time = new GLUniformData("iGlobalTime", 0.0f);
     shaderState.ownUniform(time);
         
     vertices = GLArrayDataServer.createGLSL("inVertex", 2, GL.GL_FLOAT, false, 4, GL.GL_STATIC_DRAW);
     vertices.putf(-1.0f); vertices.putf(-1.0f);
     vertices.putf(+1.0f); vertices.putf(-1.0f);
     vertices.putf(-1.0f); vertices.putf(+1.0f);
     vertices.putf(+1.0f); vertices.putf(+1.0f);
     vertices.seal(gl, true);
     shaderState.ownAttribute(vertices, true);
   }
 
   private void draw(GL2ES2 gl) {
     gl.glClearColor(0.5f, 0.1f, 0.1f, 1);
     gl.glClear(GL2ES2.GL_COLOR_BUFFER_BIT);
     
     shaderState.useProgram(gl, true);    
     
     time.setData((System.currentTimeMillis() - millisOffset) / 1000.0f);
     shaderState.uniform(gl, time);
     vertices.enableBuffer(gl, true);
     gl.glDrawArrays(GL2ES2.GL_TRIANGLE_STRIP, 0, 4);
     vertices.enableBuffer(gl, false);
     
     shaderState.useProgram(gl, false);
     
     // Compute current framerate and printout.
     frameCount++;      
     fcount += 1;
     int m = (int) (System.currentTimeMillis() - millisOffset);
     if (m - lastm > 1000 * fint) {
       frameRate = (float)(fcount) / fint;
       fcount = 0;
       lastm = m;
     }         
     if (frameCount % TARGET_FPS == 0) {
       System.out.println("FrameCount: " + frameCount + " - " + 
                          "FrameRate: " + frameRate);
     }    
   }  
   
   static public void main(String[] args) {    
     GraphicsEnvironment environment = 
         GraphicsEnvironment.getLocalGraphicsEnvironment();
     GraphicsDevice displayDevice = environment.getDefaultScreenDevice();
 
     frame = new Frame(displayDevice.getDefaultConfiguration());
     frame.setBackground(new Color(0xCC, 0xCC, 0xCC));
     frame.setTitle("Test Applet");
     
     try {
       Class<?> c = Thread.currentThread().getContextClassLoader().
           loadClass(AnimApplet.class.getName());
       applet = (AnimApplet) c.newInstance();
     } catch (Exception e) {
       throw new RuntimeException(e);
     }    
     
     frame.setLayout(null);
     frame.add(applet);
     frame.pack();
     frame.setResizable(false);
     
     applet.init();
     
     Insets insets = frame.getInsets();
     int windowW = applet.width + insets.left + insets.right;
     int windowH = applet.height + insets.top + insets.bottom;
     frame.setSize(windowW, windowH);    
     
     Rectangle screenRect = displayDevice.getDefaultConfiguration().getBounds();    
     frame.setLocation(screenRect.x + (screenRect.width - applet.width) / 2,
         screenRect.y + (screenRect.height - applet.height) / 2);    
     
     int usableWindowH = windowH - insets.top - insets.bottom;
     applet.setBounds((windowW - applet.width)/2,
                      insets.top + (usableWindowH - applet.height)/2,
                      applet.width, applet.height);
     
     // This allows to close the frame.
     frame.addWindowListener(new WindowAdapter() {
       public void windowClosing(WindowEvent e) {
         applet.animator.stop();
         System.exit(0);
       }
     });
         
     applet.initGL();
     frame.setVisible(true);
     applet.start();
   }
 }
