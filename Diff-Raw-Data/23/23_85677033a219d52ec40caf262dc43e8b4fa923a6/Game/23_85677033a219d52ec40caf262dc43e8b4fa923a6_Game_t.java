 package uk.co.zutty.glarena;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.*;
 import org.lwjgl.util.glu.GLU;
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector3f;
 import uk.co.zutty.glarena.util.MatrixUtils;
 
 import static org.lwjgl.opengl.GL11.*;
 
 public class Game {
     // Entry point for the application
     public static void main(String[] args) {
         new Game();
     }
 
     private ShaderProgram shader;
 
     private Matrix4f projectionMatrix = null;
     private Matrix4f viewMatrix = null;
     private Vector3f cameraPos = null;
     private Entity entity;
 
     public Game() {
         // Initialize OpenGL (Display)
         this.setupOpenGL();
 
         glEnable(GL_DEPTH_TEST);
         glEnable(GL_CULL_FACE);
         glCullFace(GL_BACK);
         glClearDepth(1);
 
         this.setupShaders();
         this.setupQuad();
         this.setupMatrices();
 
         Display.setVSyncEnabled(true);
 
         while (!Display.isCloseRequested()) {
             // Do a single loop (logic/render)
             this.loopCycle();
 
             Display.update();
             Display.sync(60);
         }
 
         this.destroyOpenGL();
     }
 
     private void setupMatrices() {
         projectionMatrix = MatrixUtils.frustum(Display.getWidth(), Display.getHeight(), 60, 0.1f, 100.0f);
         viewMatrix = new Matrix4f();
     }
 
     private void setupOpenGL() {
         // Setup an OpenGL context with API version 3.2
         try {
             PixelFormat pixelFormat = new PixelFormat();
             ContextAttribs contextAttributes = new ContextAttribs(3, 2)
                     .withForwardCompatible(true)
                     .withProfileCore(true);
 
             Display.setDisplayMode(new DisplayMode(1024, 768));
             Display.setTitle("3D Game");
             Display.create(pixelFormat, contextAttributes);
 
             GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
         } catch (LWJGLException e) {
             e.printStackTrace();
             System.exit(-1);
         }
 
         // Setup an XNA like background color
         GL11.glClearColor(0.4f, 0.6f, 0.9f, 0f);
 
         // Map the internal OpenGL coordinate system to the entire screen
         //GL11.glViewport(0, 0, WIDTH, HEIGHT);
 
         this.exitOnGLError("setupOpenGL");
     }
 
     private void setupQuad() {
        cameraPos = new Vector3f(0f, 10f, -5f);
 
         this.exitOnGLError("setupQuad");
 
         entity = new Gunship(shader);
     }
 
     private void setupShaders() {
         shader = ShaderProgram.build("/shaders/vec.glsl", "/shaders/frag.glsl");
 
         shader.bindAttribLocation(0, "in_Position");
         shader.bindAttribLocation(1, "foobar");
         shader.bindAttribLocation(2, "in_TextureCoord");
 
         shader.link();
         shader.validate();
 
         // Get matrices uniform locations
         shader.initUniform("projectionMatrix");
         shader.initUniform("viewMatrix");
         shader.initUniform("modelMatrix");
 
         this.exitOnGLError("setupShaders");
     }
 
 
     private void logicCycle() {
 
         if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
             cameraPos.z -= 0.01f;
         } else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
             cameraPos.z += 0.01f;
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
             cameraPos.x -= 0.01f;
         } else if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
             cameraPos.x += 0.01f;
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
             cameraPos.y -= 0.01f;
         } else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
             cameraPos.y += 0.01f;
         }
 
         entity.update();
 
         //-- Update matrices
         // Reset view and model matrices
         viewMatrix = MatrixUtils.lookAt(cameraPos.x, cameraPos.y, cameraPos.z,
                0.0f, 0f, -1f,
                 0.0f, 1.0f, 0.0f);
 
         this.exitOnGLError("logicCycle");
     }
 
     private void renderCycle() {
         GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 
         shader.use();
 
         entity.render(projectionMatrix, viewMatrix);
 
         ShaderProgram.useNone();
 
         this.exitOnGLError("renderCycle");
     }
 
     private void loopCycle() {
         // Update logic
         this.logicCycle();
         // Update rendered frame
         this.renderCycle();
 
         this.exitOnGLError("loopCycle");
     }
 
     private void destroyOpenGL() {
         shader.destroy();
 
         entity.destroy();
 
         this.exitOnGLError("destroyOpenGL");
 
         Display.destroy();
     }
 
     private void exitOnGLError(String errorMessage) {
         int errorValue = GL11.glGetError();
 
         if (errorValue != GL11.GL_NO_ERROR) {
             String errorString = GLU.gluErrorString(errorValue);
             System.err.println("ERROR - " + errorMessage + ": " + errorString);
 
             if (Display.isCreated()) Display.destroy();
             System.exit(-1);
         }
     }
 }
