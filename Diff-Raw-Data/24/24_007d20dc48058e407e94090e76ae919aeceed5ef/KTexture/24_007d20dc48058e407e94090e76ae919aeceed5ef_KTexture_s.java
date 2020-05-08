 package com.kapouta.katools;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 import javax.microedition.khronos.opengles.GL10;
 
 public class KTexture {
 	static final float[] DEFAULT_SQUARE_TEX_VERTEXES = {
 			// cenas
 			-1.0f, -1.0f, 0.0f, // V1 - bottom left
 			-1.0f, 1.0f, 0.0f, // V2 - top left
 			1.0f, -1.0f, 0.0f, // V3 - bottom right
 			1.0f, 1.0f, 0.0f // V4 - top right
 	};
 
 	static final float DEFAULT_TEX_COORDINATES[] = {
 			// Mapping coordinates for the vertices
 			0.0f, 1.0f, // top left (V2)
 			0.0f, 0.0f, // bottom left (V1)
 			1.0f, 1.0f, // top right (V4)
 			1.0f, 0.0f // bottom right (V3)
 	};
 
 	public int id;
 	public float[] vertexes;
 	public FloatBuffer buffer = null;
 	public FloatBuffer texture; // buffer holding the texture
 								// coordinates
 
	public static KTexture createTexture(String filepath, float z){
 		return new KTexture(filepath, z);
 	}
 
 	public static KTexture createTexture(String filepath, float glx1,
 			float gly1, float glx2, float gly2, float z) {
 		KTexture tex = new KTexture();
 
 		ByteBuffer byteBuffer = ByteBuffer
 				.allocateDirect(DEFAULT_TEX_COORDINATES.length * 4);
 		byteBuffer.order(ByteOrder.nativeOrder());
 		tex.texture = byteBuffer.asFloatBuffer();
 		tex.texture.put(DEFAULT_TEX_COORDINATES);
 		tex.texture.position(0);
 
 		tex.vertexes = new float[] {
 				// cenas
 				glx1, gly1, z, // V1 - bottom left
 				glx1, gly2, z, // V2 - top left
 				glx2, gly1, z, // V3 - bottom right
 				glx2, gly2, z // V4 - top right
 		};
 
		tex.makeBuffer();
 
 		return tex;
 	}
 
 	private KTexture() {
 
 	}
 
 	public KTexture(String filepath, float z) {
 		// / KTexture texData = new KTexture();
 
 		ByteBuffer byteBuffer = ByteBuffer
 				.allocateDirect(DEFAULT_TEX_COORDINATES.length * 4);
 		byteBuffer.order(ByteOrder.nativeOrder());
 		this.texture = byteBuffer.asFloatBuffer();
 		this.texture.put(DEFAULT_TEX_COORDINATES);
 		this.texture.position(0);
 
		this.vertexes = DEFAULT_SQUARE_TEX_VERTEXES;
 		this.setDepth(z);
		this.makeBuffer();
 
 	}
 
 	public void setDepth(float z) {
 		for (int zvertex = 2; zvertex < vertexes.length; zvertex += 3) {
 			vertexes[zvertex] = z;
 		}
 	}
 
	public void makeBuffer() {
 		ByteBuffer vbb2 = ByteBuffer.allocateDirect(
 		// (# of coordinate values * 4 bytes per float)
				DEFAULT_SQUARE_TEX_VERTEXES.length * 4);
 		vbb2.order(ByteOrder.nativeOrder());// use the device hardware's
 											// native
 											// byte order
 		buffer = vbb2.asFloatBuffer(); // create a floating point buffer
 										// from
 										// the ByteBuffer
		buffer.put(DEFAULT_SQUARE_TEX_VERTEXES); // add the coordinates to the
 		// FloatBuffer
 
 		buffer.position(0); // set the buffer to read the first
 							// coordinate
 	}
 
 	public void draw(GL10 gl) {
 		// bind the previously generated texture
 		gl.glBindTexture(GL10.GL_TEXTURE_2D, id);
 
 		// Point to our buffers
 		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 
 		// Set the face rotation
 		gl.glFrontFace(GL10.GL_CW);
 
 		// Point to our vertex buffer
 		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buffer);
 		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buffer);
 		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texture);
 
 		// Draw the vertices as triangle strip
 		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertexes.length / 3);
 
 		// Disable the client state before leaving
 		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 	}
 }
