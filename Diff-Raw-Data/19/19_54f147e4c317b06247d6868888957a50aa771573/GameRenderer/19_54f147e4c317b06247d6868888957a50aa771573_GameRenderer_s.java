 package com.detour.raw;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 import android.opengl.Matrix;
 
 public class GameRenderer implements GLSurfaceView.Renderer{
 	
 	private static final String TAG = "GameRenderer";
 	Context mContext;
 	Bitmap bitmap;
 	
 	private float red = 0.0f;
 	private float green = 0.0f;
 	private float blue = 0.0f;
 	
 	Shader shader;
 	int program;
 	FPSCounter fps;
 	Sprite sprite;
 	Sprite sprite2;
 	int x = 0;
 	
 	private int muMVPMatrixHandle;
 	
 	private float[] mMVPMatrix = new float[16];
 	private float[] mProjMatrix = new float[16];
 	private float[] mVMatrix = new float[16];
 	private float[] mMMatrix = new float[16];
	private float[] mVPMatrix = new float[16];
	private float[] mIMatrix = new float[16];
 	
 	//int[] vertexShader;
 	//int[] fragmentShader;
 	//int program;
 	//String vShaderSource = "";
 	//String fShaderSource = "";
 	
 	
 	public GameRenderer(Context context){
 		mContext = context;
 		
 		//create objects/sprites
 		sprite = new Sprite(mContext);
 		sprite2 = new Sprite(mContext);
 		fps = new FPSCounter();
 	}
 	
 	@Override
 	public void onDrawFrame(GL10 gl) {
 		
 		GLES20.glClearColor(red, green, blue, 1.0f);
 		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
 		
 		GLES20.glUseProgram(program);
 		//Matrix.setIdentityM(mIMatrix, 0);
 		
		Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix , 0);
 
         GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
 		
 		//sprite.draw();
 		if(x>3){
 			x=0;
 		}
 		if(x%2==0){
 			sprite.draw();
 		}else{
 			sprite2.draw();
 		}
 		x++;
 		
 		//fps.calculate();
 		//fps.draw(gl);
 	}
 	
 	@Override
 	public void onSurfaceChanged(GL10 gl, int width, int height) {
 		
 		GLES20.glViewport(0, 0, width, height);
		float ratio = (float)(width/height);
 		Matrix.orthoM(mProjMatrix, 0, -ratio, ratio, -1, 1, 0.5f, 10);
		//Matrix.setLookAtM(mVMatrix, 0, 0, 0, -1.0f, 0.0f, 0f, 0f, 0f, 1.0f, 0.0f);
 	}
 
 	@Override
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		// TODO Auto-generated method stub
 		
 		/*int error = GLES20.glGetError();
 		Log.d(LOG_TAG, ""+error);*/
 		
 		shader = new Shader(R.raw.sprite_vs, R.raw.sprite_fs, mContext);
 		program = shader.getProgram();
 		
 		muMVPMatrixHandle = GLES20.glGetUniformLocation(program, "u_MVPMatrix");
 		
 		//GLES20.glEnable(GLES20.GL_TEXTURE_2D);
 		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
 		GLES20.glClearDepthf(1.0f);
 		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
 		GLES20.glDepthMask(true);
 		GLES20.glEnable(GLES20.GL_CULL_FACE);
 		GLES20.glCullFace(GLES20.GL_BACK);
 		GLES20.glClearColor(red, green, blue, 1.0f);
 		
 		//load sprite/object textures (preferably loop through an array of all sprites).
 		sprite.loadGLTexture(R.drawable.raw1a, program);
 		sprite2.loadGLTexture(R.drawable.raw2, program);
 		
		Matrix.setLookAtM(mVMatrix, 0, 0, 0, -1.0f, 0.0f, 0f, 0f, 0f, 1.0f, 0.0f);
 		
 		System.gc();
 	}
 	
 }
