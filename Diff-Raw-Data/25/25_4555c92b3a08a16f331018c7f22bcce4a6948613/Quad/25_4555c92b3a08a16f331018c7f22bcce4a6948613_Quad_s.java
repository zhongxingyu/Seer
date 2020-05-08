 package com.awevation.spuzzle;
 
 import android.util.Log;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.nio.ShortBuffer;
 import android.opengl.GLES20;
 import android.opengl.Matrix;
 import android.opengl.GLUtils;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import java.io.InputStream;
 import android.content.Context;
 import android.content.res.Resources;
 
 class Quad {
     private String TAG = "Quad";
 
     private FloatBuffer vertexBuffer;
     private FloatBuffer texCoBuffer;
     private ShortBuffer indicesBuffer;
     private IntBuffer texBuffer;
     private float height;
     private float width;
     public float xPos = 0;
     public float yPos = 0;
     public float zPos = 0;
     private float xVel = 0;
     private float yVel = 0;
     private float xAcc = 0;
     private float yAcc = 0;
     private float angle = 0;
     private float scale = 0;
     private int mProgram;
     private int posAttr;
     private int texAttr;
     private int uSampler;
     private float[] mMatrix = new float[16]; //The model matrix, for the local coordinate system
     private float[] rMatrix = new float[16]; //rotation
     private float[] mvMatrix = new float[16];
     private int mMVMatrixHandle;
     private int mPMatrixHandle;
     private float alpha = 0f;
     private Context context;
     private int[] texture = new int[1];
 
     private final String vertexShaderCode =
 	"attribute vec4 vPosition;" +
 	"attribute vec2 aTexCo;" +
 	"varying vec2 vTexCo;" +
 	"uniform mat4 uMVMatrix;" +
 	"uniform mat4 uPMatrix;" +
 	"void main() {" +
 	"  gl_Position = uPMatrix * uMVMatrix * vPosition;" +
 	"  vTexCo = aTexCo;" +
 	"}";
 
     private final String fragmentShaderCode =
 	"precision mediump float;" +
 	"uniform sampler2D uSampler;" +
 	"uniform vec4 vColor;" +
 	"varying vec2 vTexCo;" +
 	"void main() {" +
 	//"gl_FragColor = vec4(0.0, 0.0, 1.0, 1.0);" +
 	"gl_FragColor = texture2D(uSampler, vec2(vTexCo));" +
         	"if(gl_FragColor.a < 0.1) {" +
 		"	discard;" +
 		"}" +
 	"}";
 
     static final int COORDS_PER_VERTEX = 3;
 
     private final int vertexCount = quadCoords.length / COORDS_PER_VERTEX;
     private final int vertexStride = COORDS_PER_VERTEX * 4; // bytes per vertex
 
     static float quadCoords[] = {
 	-0.5f,  0.5f, 0.0f,   // top left
 	-0.5f, -0.5f, 0.0f,   // bottom left
 	 0.5f, -0.5f, 0.0f,   // bottom right
 	 0.5f,  0.5f, 0.0f  // top right
     };
 
     static float texCoords[] = {
 	0.0f, 0.0f,
 	0.0f, 1.0f,
	1.0f, 0.0f,
	1.0f, 1.0f							    
     };
 
     private short indices[] = {1, 0, 2, 3};
 
     public Quad(Context context) {
 	this.context = context;
 	init();
     }
 
     public Quad(Context context, float xPos, float yPos) {
 	this.context = context;
 	init();
 	this.xPos = xPos;
 	this.yPos = yPos;
     }
 
     public void init() {
 	ByteBuffer vbb = ByteBuffer.allocateDirect(quadCoords.length * 4); //each float is four bytes
 	vbb.order(ByteOrder.nativeOrder());
 	vertexBuffer = vbb.asFloatBuffer();
 	vertexBuffer.put(quadCoords);
 	vertexBuffer.position(0);
 
 	ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2); //shorts are just two bytes!!!
 	ibb.order(ByteOrder.nativeOrder());
 	indicesBuffer = ibb.asShortBuffer();
 	indicesBuffer.put(indices);
 	indicesBuffer.position(0);
 
 	ByteBuffer tcbb = ByteBuffer.allocateDirect(texCoords.length * 4);
 	tcbb.order(ByteOrder.nativeOrder());
 	texCoBuffer = tcbb.asFloatBuffer();
 	texCoBuffer.put(texCoords);
 	texCoBuffer.position(0);
 	
 	ByteBuffer tbb = ByteBuffer.allocateDirect(256*256*4);
 	tbb.order(ByteOrder.nativeOrder());
 	texBuffer = tbb.asIntBuffer();
 
 	int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
 	int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
 
 	mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
 	GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
 	GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
 	GLES20.glLinkProgram(mProgram);                  // creates OpenGL ES program executables
 
 	// We need to flip the textures vertically:
        	android.graphics.Matrix flip = new android.graphics.Matrix();
  	flip.postScale(1f, -1f);
 	BitmapFactory.Options opts = new BitmapFactory.Options();
 	opts.inScaled = false;
 	        
 	// Load up, and flip the texture:
 	Bitmap temp = BitmapFactory.decodeResource(context.getResources(), R.drawable.anser, opts);
 	Bitmap bmp = Bitmap.createBitmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), flip, true);
 	temp.recycle();
      
 	//Bitmap bmp;
 	//BitmapFactory bmfactory = new BitmapFactory();
 	//Resources res = context.getResources();
 	//bmp = bmfactory.decodeResource(res, R.drawable.anser);
 	bmp.copyPixelsToBuffer(texBuffer);
 
 	GLES20.glGenTextures(1, texture, 0);
 	GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
 	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
 	GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
 	GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
 	//GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA4, 256, 256, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, texBuffer);
 	GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
 	//GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
 	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
 	bmp.recycle();
     }
 
     public void setXVel(float xVel) {
 	this.xVel = xVel;
     }
 
     public void setYVel(float yVel) {
 	this.yVel = yVel;
     }
 
     public void setXAcc(float xAcc) {
 	this.xAcc = xAcc;
     }
 
     public void setYAcc(float yAcc) {
 	this.yAcc = yAcc;
     }
 
     private float alphaInc = 0.01f;
 
     public void update(float dt) {
 	scale = 20f;
 
 	xVel += xAcc * dt;
 	yVel += yAcc * dt;
 
 	xPos += xVel;
 	yPos += yVel;
 
 	if(yPos +10 > 130) {
 	yPos -= yVel;
 	yVel = 0.f;
 	if(xVel > 0f) {
 	xVel -= 0.05f;
 	} else if (xVel < 0) {
 	xVel += 0.05f;
 	}
 	}
 
 	if(yPos < 15) {
 	yPos = 15;
 	yVel = 0.f;
 	if(xVel > 0f) {
 	xVel -= 0.05f;
 	} else if (xVel < 0) {
 	xVel += 0.05f;
 	}
 	} 
 
 
 	if(alpha > 1.0f || alpha < 0.0f ) {
 	    alphaInc *= -1;
 	}
 
 	alpha += alphaInc;
 
	angle += 1f;
 
 	if(angle > 360f) {
 	    angle = 0f;
 	}
     }
 
     public void draw(float[] mvMatrix, float[] pMatrix) {
 	Matrix.setIdentityM(mMatrix, 0);
 	//Matrix.setIdentityM(mvMatrix, 0);
 
 	Matrix.translateM(mMatrix, 0, xPos, yPos, 0.f);
 	Matrix.multiplyMM(mvMatrix, 0, mvMatrix, 0, mMatrix, 0);
 	Matrix.scaleM(mvMatrix, 0, scale, scale, 0f);
 	Matrix.rotateM(mvMatrix, 0, angle, 0f, 0f, 1f);
 
 	GLES20.glUseProgram(mProgram);
 
 	posAttr = GLES20.glGetAttribLocation(mProgram, "vPosition");
 	texAttr = GLES20.glGetAttribLocation(mProgram, "aTexCo");
 	uSampler = GLES20.glGetUniformLocation(mProgram, "uSampler");
 
 	GLES20.glVertexAttribPointer(posAttr, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, vertexBuffer);
 	GLES20.glVertexAttribPointer(texAttr, 2, GLES20.GL_FLOAT, false, 0, texCoBuffer);
 
 	GLES20.glEnableVertexAttribArray(posAttr);
 	GLES20.glEnableVertexAttribArray(texAttr);
 
 	GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
 	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
 	//GLES20.glUniform4f(texAttr, );
 	GLES20.glUniform1i(uSampler, 0);
 
 	mMVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVMatrix");
 	mPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uPMatrix");
 
 	GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvMatrix, 0);
 	GLES20.glUniformMatrix4fv(mPMatrixHandle, 1, false, pMatrix, 0);
 
 	GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, 4, GLES20.GL_UNSIGNED_SHORT, indicesBuffer);
 
 	GLES20.glDisableVertexAttribArray(posAttr);
 	GLES20.glDisableVertexAttribArray(texAttr);
 
 	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
     }
 }
 
