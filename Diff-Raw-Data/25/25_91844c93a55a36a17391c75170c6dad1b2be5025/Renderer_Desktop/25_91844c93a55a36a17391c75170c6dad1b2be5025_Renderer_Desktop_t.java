 package px.tileEngineV2.desktop.graphics;
 
 import java.nio.FloatBuffer;
 
 import javax.media.opengl.GL4;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLEventListener;
 
 import org.lwjgl.util.vector.Matrix3f;
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector2f;
 import org.lwjgl.util.vector.Vector3f;
 import org.lwjgl.util.vector.Vector4f;
 
 import px.tileEngineV2.core.GameCore;
 import px.tileEngineV2.desktop.core.GameCore_Desktop;
 import px.tileEngineV2.graphics.Renderer;
 import px.tileEngineV2.graphics.Texture;
 import px.tileEngineV2.world.Tile;
 
 import com.jogamp.opengl.util.FPSAnimator;
 
 /**Core OpenGL Renderer implementation for Linux and Windows, all drawing done by in-game
  * actors must be done through a class like this.
  * @author Michael Stopa */
 public class Renderer_Desktop extends Renderer implements GLEventListener {
     
     // ++++ ++++ Constants ++++ ++++
     
     public static final int SCREEN_MIN_SIZE = Tile.TILE_SIZE * 12;
     
     // ++++ ++++ Data ++++ ++++
     
     // ++++ OpenGL Management ++++
     
     //Drawing Context
     private GL4 gl;
     private GLAutoDrawable autoDrawable;
     private FPSAnimator animator;
     
     //OpenGL Objects
     private int vao;
     private int vbo;
     
     //Shader Program
     private int shaderProgram;
     
     //Uniforms
     private int transUniform;
     private int depthUniform;
     private int modelUniform;
     private int viewUniform;
     private int projUniform;
     private int tintUniform;
     
     private Texture_Desktop tex;
     private TextureLoader loader;
     
     // ++++ Transformation ++++
     
     private Matrix4f projTransform = new Matrix4f();
     
     // ++++ ++++ Initialization ++++ ++++
 
     @Override
     public void init(GLAutoDrawable drawable) {
         autoDrawable = drawable;
         gl = drawable.getGL().getGL4();
         
         //OpenGL Fixed Function Configuration
         
         gl.glClearColor(0.0f, 0.0f, 0.1f, 0.0f);
         
         gl.glEnable(GL4.GL_DEPTH_TEST);
         gl.glDepthFunc(GL4.GL_LEQUAL);
         
         gl.glEnable(GL4.GL_BLEND);
         gl.glBlendFunc(GL4.GL_SRC_ALPHA, GL4.GL_ONE_MINUS_SRC_ALPHA);
         
         gl.glEnable(GL4.GL_CULL_FACE);
         gl.glCullFace(GL4.GL_BACK);
         
         //VAO VBO Shader Initialization
         
         int[] temp = new int[1];
         
         //Create VAO
         gl.glGenVertexArrays(1, temp, 0);
         vao = temp[0];
         gl.glBindVertexArray(vao);
         
         //Compile Shader Program
         shaderProgram = ShaderLoader.loadShaders(drawable,
                 "res/shaders/vertex.glsl", "res/shaders/fragment.glsl");
         gl.glUseProgram(shaderProgram);
         
         transUniform = gl.glGetUniformLocation(shaderProgram, "trans");
         depthUniform = gl.glGetUniformLocation(shaderProgram, "depth");
         modelUniform = gl.glGetUniformLocation(shaderProgram, "model");
         viewUniform = gl.glGetUniformLocation(shaderProgram, "view");
         tintUniform = gl.glGetUniformLocation(shaderProgram, "tint");
         
         float[] vertices = {
                 //Position         //Texcoord
                -0.5f, 0.5f, 0.0f, 0.0f, //Top Left
                -0.5f,-0.5f, 0.0f, 1.0f, //Bottom Left
                 0.5f, 0.5f, 1.0f, 0.0f, //Top Right
                 0.5f,-0.5f, 1.0f, 1.0f //Bottom Right
         };
         
         //Store verts in a Vertex Buffer Object
         gl.glGenBuffers(1, temp, 0);
         vbo = temp[0];
         gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo);
         gl.glBufferData(GL4.GL_ARRAY_BUFFER, vertices.length * 4,
                 FloatBuffer.wrap(vertices), GL4.GL_STATIC_DRAW);
         
         int positionAttribute = gl.glGetAttribLocation(shaderProgram, "position");
         gl.glEnableVertexAttribArray(positionAttribute);
         gl.glVertexAttribPointer(positionAttribute, 2, GL4.GL_FLOAT, false, 4 * 4, 0);
 
         int texcoordAttribute = gl.glGetAttribLocation(shaderProgram, "texcoord");
         gl.glEnableVertexAttribArray(texcoordAttribute);
         gl.glVertexAttribPointer(texcoordAttribute, 2, GL4.GL_FLOAT, false, 4 * 4, 2 * 4);
         
         //Unbind all
         gl.glBindVertexArray(0);
         gl.glUseProgram(0);
         gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, 0);
         gl.glBindTexture(GL4.GL_TEXTURE_2D, 0);
         
         loader = new TextureLoader();
         tex = new Texture_Desktop(loader, "res/textures/splash.png");
     }
     
     // ++++ ++++ Disposal ++++ ++++
 
     @Override
     public void dispose(GLAutoDrawable drawable) {
         autoDrawable = drawable;
         gl = drawable.getGL().getGL4();
         int[] temp = new int[1];
 
         temp[0] = vao;
         gl.glDeleteVertexArrays(1, temp, 0);
         temp[0] = vbo;
         gl.glDeleteBuffers(1, temp, 0);
         gl.glDeleteProgram(shaderProgram);
         
     }
     
     // ++++ ++++ Rendering ++++ ++++
 
     @Override
    public void drawQuad(Texture texture, Matrix4f model, float depth,
             Vector4f tint) {
         gl.glBindTexture(GL4.GL_TEXTURE_2D, ((Texture_Desktop)texture).getGLTexture());
         
         Matrix3f trans = texture.getTransform();
         FloatBuffer transBuffer = FloatBuffer.allocate(9);
         trans.store(transBuffer);
         transBuffer.rewind();
         
         FloatBuffer modelBuffer = FloatBuffer.allocate(16);
         model.store(modelBuffer);
         modelBuffer.rewind();
         
         Matrix4f view = new Matrix4f();
         FloatBuffer viewBuffer = FloatBuffer.allocate(16);
         //GameCore.getInstance().getWorld().getCamera().getViewTransform()
         //        .store(viewBuffer);
         view.store(viewBuffer);
         viewBuffer.rewind();
         
         FloatBuffer projBuffer = FloatBuffer.allocate(16);
         projTransform.store(projBuffer);
         projBuffer.rewind();
         
         gl.glUniformMatrix3fv(transUniform, 1, false, transBuffer);
        gl.glUniform1f(depthUniform, depth);
         gl.glUniformMatrix4fv(modelUniform, 1, false, modelBuffer);
         gl.glUniformMatrix4fv(viewUniform, 1, false, viewBuffer);
         gl.glUniformMatrix4fv(projUniform, 1, false, projBuffer);
         gl.glUniform4f(tintUniform, tint.x, tint.y, tint.z, tint.z);
         
         gl.glDrawArrays(GL4.GL_TRIANGLE_STRIP, 0, 4);
         
         gl.glBindTexture(GL4.GL_TEXTURE_2D, 0);
     }
 
     @Override
     public void drawText(String text, Vector3f location, Vector2f size,
             Vector4f color) {
         // TODO Draw Text
     }
 
     @Override
     public void display(GLAutoDrawable drawable) {
         autoDrawable = drawable;
         gl = drawable.getGL().getGL4();
         
         gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);
         
         gl.glUseProgram(shaderProgram);
         gl.glBindVertexArray(vao);
         
        drawQuad(tex, new Matrix4f(), 0f, new Vector4f(1f,1f,1f,1f));
         //TODO Draw all objects in Current World/Battle/Cutscene/Menu
         //TODO Create new view transform from World.getCamera() data.
 
         gl.glBindVertexArray(0);
         gl.glUseProgram(0);
     }
 
     @Override
     public void reshape(GLAutoDrawable drawable, int x, int y, int width,
             int height) {
         autoDrawable = drawable;
         gl = drawable.getGL().getGL4();
 
         int scaleX;
         int scaleY;
         if (width >= height) {
             scaleY = height / SCREEN_MIN_SIZE;
             scaleX = scaleY * (width/height);
         } else {
             scaleX = width / SCREEN_MIN_SIZE;
             scaleY = scaleX * (height/width);
         }
         
 //        projTransform = Matrix4f.scale(
 //                new Vector3f(scaleX, scaleY, 1f), new Matrix4f(), null);
     }
     
     // ++++ ++++ Accessors ++++ ++++
     
     public GL4 getContext() {
         return gl;
     }
     
     public GLAutoDrawable getAutoDrawable() {
         return autoDrawable;
     }
 
     @Override
     public void start() {
         //TODO fetch refresh rate from GameCore.
         animator = new FPSAnimator(((GameCore_Desktop)GameCore.getInstance())
                 .getFrame().getCanvas(), 60);
         animator.start();
     }
     
 }
