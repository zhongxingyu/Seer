 package com.hruivo.voidframework;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.nio.ShortBuffer;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import android.graphics.Bitmap;
 import android.opengl.GLUtils;
 
 
 public class Mesh {
 
 	private FloatBuffer verticesBuffer = null;
 	private ShortBuffer indicesBuffer = null;
 	private int numOfIndices = -1;
 	
 	private float[] rgba = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
 	private FloatBuffer colorBuffer = null;
 	
 	private FloatBuffer textureBuffer = null;
 	private int textureId = -1;
 	private Bitmap bitmap;
 	private boolean shouldLoadTexture = false;
 	
 	
 	public Vector3 Position;
 	public Vector3 Rotation;
 	
 	
 	public Mesh() {
 		Position = new Vector3();
 		Rotation = new Vector3();
 	}
 	
 	protected void setVertices(float[] vertices) {
 		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
 		vbb.order(ByteOrder.nativeOrder());
 		verticesBuffer = vbb.asFloatBuffer();
 		verticesBuffer.put(vertices);
 		verticesBuffer.position(0);
 	}
 	
 	protected void setIndices(short[] indices) {
 		ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
 		ibb.order(ByteOrder.nativeOrder());
 		indicesBuffer = ibb.asShortBuffer();
 		indicesBuffer.put(indices);
 		indicesBuffer.position(0);
 		numOfIndices = indices.length;
 	}
 	
 	public void setColor(float red, float green, float blue, float alpha) {
 		rgba[0] = red;
 		rgba[1] = green;
 		rgba[2] = blue;
 		rgba[3] = alpha;
 	}
 	
 	protected void setColors(float[] colors) {
 		ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
 		cbb.order(ByteOrder.nativeOrder());
 		colorBuffer = cbb.asFloatBuffer();
 		colorBuffer.put(colors);
 		colorBuffer.position(0);
 	}
 	
 	protected void setTextureCoordinates(float[] textureCoords) {
 		ByteBuffer tbb = ByteBuffer.allocateDirect(textureCoords.length * 4);
 		tbb.order(ByteOrder.nativeOrder());
 		textureBuffer = tbb.asFloatBuffer();
 		textureBuffer.put(textureCoords);
 		textureBuffer.position(0);
 	}
 	
 	public void loadBitmap(Bitmap bmp) {
 		this.bitmap = bmp;
 		shouldLoadTexture = true;
 	}
 	
 	private void loadGlTexture(GL10 gl) {
 		int[] textures = new int[1];
 		gl.glGenTextures(1, textures, 0);
 		textureId = textures[0];
 		
 		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
 		
 		// Create Nearest Filtered Texture
 		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
 				GL10.GL_NEAREST);
 		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
 				GL10.GL_NEAREST);
 	 
 		// Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
 		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
 				GL10.GL_REPEAT);
 		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
 				GL10.GL_REPEAT);
 		
 		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
 		
 		bitmap.recycle();
 		bitmap = null;
 	}
 	
 	public void draw(GL10 gl) {
		gl.glFrontFace(GL10.GL_CW);
 		gl.glEnable(GL10.GL_CULL_FACE);
 		gl.glCullFace(GL10.GL_BACK);
 		
 		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
 			
 			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, verticesBuffer);
 			gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
 			if(colorBuffer != null) {
 				gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
 				gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
 			}
 			
 			if(shouldLoadTexture) {
 				loadGlTexture(gl);
 				shouldLoadTexture = false;
 			}
 			if(textureId != -1 && textureBuffer != null) {
 				gl.glEnable(GL10.GL_TEXTURE_2D);
 				gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 				
 				gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
 				gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
 			}
 			
 			gl.glPushMatrix();
 				gl.glTranslatef(Position.X, Position.Y, Position.Z);
 				gl.glRotatef(Rotation.X, 1, 0, 0);
 				gl.glRotatef(Rotation.Y, 0, 1, 0);
 				gl.glRotatef(Rotation.Z, 0, 0, 1);			
 				
 				gl.glDrawElements(GL10.GL_TRIANGLES, numOfIndices, GL10.GL_UNSIGNED_SHORT, indicesBuffer);
 			gl.glPopMatrix();
 			
 			if(textureId != -1 && textureBuffer != null) {
 				gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 			}
 			
 		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glDisable(GL10.GL_CULL_FACE);
 	}
 	
 	public void update(float dt) {
 		
 	}
 	
 }
