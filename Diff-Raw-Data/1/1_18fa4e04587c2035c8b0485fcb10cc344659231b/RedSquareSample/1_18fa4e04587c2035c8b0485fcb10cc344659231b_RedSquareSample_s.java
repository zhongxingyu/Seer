 package net.jbboehr.ovr;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.media.opengl.awt.GLCanvas;
 
 import com.jogamp.opengl.util.Animator;
 import com.jogamp.opengl.util.GLArrayDataServer;
 import com.jogamp.opengl.util.PMVMatrix;
 import com.jogamp.opengl.util.glsl.ShaderCode;
 import com.jogamp.opengl.util.glsl.ShaderProgram;
 import com.jogamp.opengl.util.glsl.ShaderState;
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2ES2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.GLUniformData;
 
 /**
  * From the jogamp unit tests
  */
 public class RedSquareSample implements GLEventListener {
     private ShaderState st;
     private PMVMatrix pmvMatrix;
     private GLUniformData pmvMatrixUniform;
     private GLArrayDataServer vertices ;
     private GLArrayDataServer colors ;
     private long t0;
     private int swapInterval = 0;
     private float aspect = 1.0f;
     private boolean doRotate = true;
     private boolean clearBuffers = true;
     
     private DistortionCorrection barrelDistort;
 	private boolean barrelDistortEnabled = true;
 
     public RedSquareSample(int swapInterval) {
         this.swapInterval = swapInterval;
     }
 
     public RedSquareSample() {
         this.swapInterval = 1;
     }
         
     public void setAspect(float aspect) { this.aspect = aspect; }
     public void setDoRotation(boolean rotate) { this.doRotate = rotate; }
     public void setClearBuffers(boolean v) { clearBuffers = v; }
     
     public void init(GLAutoDrawable glad) {
         System.err.println(Thread.currentThread()+" RedSquareES2.init ...");
         GL2ES2 gl = glad.getGL().getGL2ES2();
         
         System.err.println("RedSquareES2 init on "+Thread.currentThread());
         System.err.println("Chosen GLCapabilities: " + glad.getChosenGLCapabilities());
         System.err.println("INIT GL IS: " + gl.getClass().getName());
         System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
         System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
         System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));
         System.err.println("GL GLSL: "+gl.hasGLSL()+", has-compiler-func: "+gl.isFunctionAvailable("glCompileShader")+", version "+(gl.hasGLSL() ? gl.glGetString(GL2ES2.GL_SHADING_LANGUAGE_VERSION) : "none"));
         System.err.println("GL FBO: basic "+ gl.hasBasicFBOSupport()+", full "+gl.hasFullFBOSupport());
         System.err.println("GL Profile: "+gl.getGLProfile());
         System.err.println("GL Renderer Quirks:" + gl.getContext().getRendererQuirks().toString());
         System.err.println("GL:" + gl + ", " + gl.getContext().getGLVersion());
 
         st = new ShaderState();
         st.setVerbose(true);
         final ShaderCode vp0 = ShaderCode.create(gl, GL2ES2.GL_VERTEX_SHADER, this.getClass(), "shader",
                 "shader/bin", "RedSquareShader", true);
         final ShaderCode fp0 = ShaderCode.create(gl, GL2ES2.GL_FRAGMENT_SHADER, this.getClass(), "shader",
                 "shader/bin", "RedSquareShader", true);
         //vp0.defaultShaderCustomization(gl, true, ShaderCode.es2_default_precision_fp);
         //fp0.defaultShaderCustomization(gl, true, ShaderCode.es2_default_precision_vp);
         final ShaderProgram sp0 = new ShaderProgram();
         sp0.add(gl, vp0, System.err);
         sp0.add(gl, fp0, System.err);
         st.attachShaderProgram(gl, sp0, true);
         
         // setup mgl_PMVMatrix
         pmvMatrix = new PMVMatrix();
         pmvMatrix.glMatrixMode(PMVMatrix.GL_PROJECTION);
         pmvMatrix.glLoadIdentity();
         pmvMatrix.glMatrixMode(PMVMatrix.GL_MODELVIEW);
         pmvMatrix.glLoadIdentity();       
         pmvMatrixUniform = new GLUniformData("mgl_PMVMatrix", 4, 4, pmvMatrix.glGetPMvMatrixf()); // P, Mv
         st.ownUniform(pmvMatrixUniform);
         st.uniform(gl, pmvMatrixUniform);        
         
         // Allocate Vertex Array
         vertices = GLArrayDataServer.createGLSL("mgl_Vertex", 3, GL.GL_FLOAT, false, 4, GL.GL_STATIC_DRAW);
         vertices.putf(-2); vertices.putf( 2); vertices.putf( 0);
         vertices.putf( 2); vertices.putf( 2); vertices.putf( 0);
         vertices.putf(-2); vertices.putf(-2); vertices.putf( 0);
         vertices.putf( 2); vertices.putf(-2); vertices.putf( 0);
         vertices.seal(gl, true);
         st.ownAttribute(vertices, true);
         vertices.enableBuffer(gl, false);
         
         // Allocate Color Array
         colors= GLArrayDataServer.createGLSL("mgl_Color", 4, GL.GL_FLOAT, false, 4, GL.GL_STATIC_DRAW);
         colors.putf(1); colors.putf(0); colors.putf(0); colors.putf(1);
         colors.putf(0); colors.putf(0); colors.putf(1); colors.putf(1);
         colors.putf(1); colors.putf(0); colors.putf(0); colors.putf(1);
         colors.putf(1); colors.putf(0); colors.putf(0); colors.putf(1);
         colors.seal(gl, true);          
         st.ownAttribute(colors, true);
         colors.enableBuffer(gl, false);
         
         // OpenGL Render Settings
         gl.glEnable(GL2ES2.GL_DEPTH_TEST);
         st.useProgram(gl, false);        
 
         if( barrelDistortEnabled ) {
         	barrelDistort = new DistortionCorrection(640, 480, glad.getGL());
         }
 	    
         t0 = System.currentTimeMillis();
         System.err.println(Thread.currentThread()+" RedSquareES2.init FIN");
     }
 
     public void display(GLAutoDrawable glad) {
         long t1 = System.currentTimeMillis();
 
         GL2ES2 gl = glad.getGL().getGL2ES2();
 
         // Start custom
         if( barrelDistortEnabled ) {
         	barrelDistort.beginOffScreenRenderPass();
         }
         // End custom
         
         if( clearBuffers ) {
             gl.glClearColor(64, 64, 64, 0);
             gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
         }
         
         st.useProgram(gl, true);
         // One rotation every four seconds
         pmvMatrix.glMatrixMode(PMVMatrix.GL_MODELVIEW);
         pmvMatrix.glLoadIdentity();
         pmvMatrix.glTranslatef(0, 0, -10);
         if(doRotate) {
             float ang = ((float) (t1 - t0) * 360.0F) / 4000.0F;
             pmvMatrix.glRotatef(ang, 0, 0, 1);
             pmvMatrix.glRotatef(ang, 0, 1, 0);
         }
         st.uniform(gl, pmvMatrixUniform);        
 
         // Draw a square
         vertices.enableBuffer(gl, true);
         colors.enableBuffer(gl, true);
         gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4);
         vertices.enableBuffer(gl, false);
         colors.enableBuffer(gl, false);
         st.useProgram(gl, false);
         
         
         // Start custom
         if( barrelDistortEnabled ) {
         	barrelDistort.endOffScreenRenderPass();
         	barrelDistort.renderToScreen();
        	glad.swapBuffers();
         }
         // End custom
     }
 
     public void reshape(GLAutoDrawable glad, int x, int y, int width, int height) {
         System.err.println(Thread.currentThread()+" RedSquareES2.reshape "+x+"/"+y+" "+width+"x"+height+", swapInterval "+swapInterval+", drawable 0x"+Long.toHexString(glad.getHandle()));
         // Thread.dumpStack();
         GL2ES2 gl = glad.getGL().getGL2ES2();
         
         if(-1 != swapInterval) {        
             gl.setSwapInterval(swapInterval); // in case switching the drawable (impl. may bound attribute there)
         }
         
         st.useProgram(gl, true);
         // Set location in front of camera
         pmvMatrix.glMatrixMode(PMVMatrix.GL_PROJECTION);
         pmvMatrix.glLoadIdentity();
         pmvMatrix.gluPerspective(45.0F, ( (float) width / (float) height ) / aspect, 1.0F, 100.0F);
         //pmvMatrix.glOrthof(-4.0f, 4.0f, -4.0f, 4.0f, 1.0f, 100.0f);
         st.uniform(gl, pmvMatrixUniform);
         st.useProgram(gl, false);
         
         System.err.println(Thread.currentThread()+" RedSquareES2.reshape FIN");
     }
 
     public void dispose(GLAutoDrawable glad) {
         System.err.println(Thread.currentThread()+" RedSquareES2.dispose ... ");
         GL2ES2 gl = glad.getGL().getGL2ES2();
         st.destroy(gl);
         st = null;
         pmvMatrix.destroy();
         pmvMatrix = null;
         System.err.println(Thread.currentThread()+" RedSquareES2.dispose FIN");
     }    
     
     public static void main(String[] args) {
         final GLCanvas canvas = new GLCanvas();
         final Frame frame = new Frame("Jogl Quad drawing");
         final Animator animator = new Animator(canvas);
         //final Animator animator = new Animator(canvas);
         canvas.addGLEventListener(new RedSquareSample());
         frame.add(canvas);
         frame.setSize(640, 480);
         frame.setResizable(false);
         frame.addWindowListener(new WindowAdapter() {
                 public void windowClosing(WindowEvent e) {
                         animator.stop();
                         frame.dispose();
                         System.exit(0);
                 }
         });
         frame.setVisible(true);
         animator.start();
         canvas.requestFocus();
     }
 }
