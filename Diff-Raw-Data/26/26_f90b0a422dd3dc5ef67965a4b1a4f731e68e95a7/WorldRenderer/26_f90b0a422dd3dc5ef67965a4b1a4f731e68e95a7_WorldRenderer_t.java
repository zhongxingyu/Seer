 package engine.open2d.renderer;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.LinkedHashSet;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.Context;
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 import android.opengl.Matrix;
 import android.util.Log;
 import android.view.MotionEvent;
 
 public class WorldRenderer implements GLSurfaceView.Renderer{
 	private final static int BYTESPERFLOATS = 4;
 	
 	Context activityContext;
 	
 	private float[] modelMatrix = new float[16];
 	private float[] viewMatrix = new float[16];
 	private float[] projectionMatrix = new float[16];
 	
 	private float[] MVMatrix = new float[16];
 	private float[] MVPMatrix = new float[16];
 	
 	ArrayList<ShaderHandler> shaders = null;
 	
 	//singlton design pattern
<<<<<<< HEAD
     public WorldRenderer(final Context activityContext) { 
     	this.activityContext = activityContext;
     }
=======
    private WorldRenderer() {}
>>>>>>> a4d078071ae44599debaa8fc0740426cd7a21909
 
     /*
     private static class WorldRendererHolder { 
    	public static final WorldRenderer INSTANCE = new WorldRenderer();
     }
 
     public static WorldRenderer getInstance() {
    	return WorldRendererHolder.INSTANCE;
     }
     */
 	//end singleton
     
     
 	public void initSetup(){
 		
 		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
 		
 		GLES20.glEnable(GLES20.GL_CULL_FACE);
 		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
 		GLES20.glEnable(GLES20.GL_BLEND);
 		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
 		
 	    Matrix.setLookAtM(viewMatrix, 0, 0.0f, 0.0f, -0.5f, 0.0f, 0.0f, -5.0f, 0.0f, 1.0f, 0.0f);
 	    
 	    buildShaders();
 	    
 	    //build textures
 	}
 	
 	private void buildShaders(){
 	    if(shaders == null)
 			throw new RuntimeException("no shaders present");
 	    
 	    for(ShaderHandler shader:shaders)
 	    	shader.buildShaderProgram();
 	}
 	
 	@Override
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		initSetup();
 	}
 	
 	public void passTouchEvents(MotionEvent e){}
 
 	@Override
 	public void onDrawFrame(GL10 gl) {}
 
 	@Override
 	public void onSurfaceChanged(GL10 gl, int width, int height) {}
 	
 }
