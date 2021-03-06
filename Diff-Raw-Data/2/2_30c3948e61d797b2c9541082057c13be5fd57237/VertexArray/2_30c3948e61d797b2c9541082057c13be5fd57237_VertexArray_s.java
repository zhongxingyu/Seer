 package com.badlogic.gdx.graphics.glutils;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.GL11;
 import com.badlogic.gdx.graphics.VertexAttribute;
 import com.badlogic.gdx.graphics.VertexAttributes;
 import com.badlogic.gdx.graphics.VertexAttributes.Usage;
 import com.badlogic.gdx.utils.BufferUtils;
 import com.badlogic.gdx.utils.GdxRuntimeException;
 
 /**
  * <p>
  * Convenience class for working with OpenGL vertex arrays. It interleaves all
  * data in the order you specified in the constructor via
  * {@link VertexAttribute}.
  * </p>
  * 
  * <p>
  * This class does not support shaders and for that matter OpenGL ES 2.0. For
  * this {@link VertexBufferObject}s are needed.
  * </p>
  * 
  * @author mzechner
  * 
  */
 public class VertexArray implements VertexData {
 	final VertexAttributes attributes;
 	final FloatBuffer buffer;
 	final ByteBuffer byteBuffer;
 	boolean isBound = false;
 
 	/**
 	 * Constructs a new interleaved VertexArray
 	 * 
 	 * @param numVertices
 	 *            the maximum number of vertices
 	 * @param attributes
 	 *            the {@link VertexAttributes}
 	 */
 	public VertexArray(int numVertices, VertexAttribute... attributes) {
 		this.attributes = new VertexAttributes(attributes);
 		byteBuffer = ByteBuffer.allocateDirect(this.attributes.vertexSize
 				* numVertices);
 		byteBuffer.order(ByteOrder.nativeOrder());
 		buffer = byteBuffer.asFloatBuffer();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void dispose() {
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public FloatBuffer getBuffer() {
 		return buffer;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getNumVertices() {
 		return byteBuffer.limit() / attributes.vertexSize;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public int getNumMaxVertices() {
 		return byteBuffer.capacity() / attributes.vertexSize;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setVertices(float[] vertices, int offset, int count) {
 		BufferUtils.copy(vertices, byteBuffer, count, offset);
 		buffer.position(0);
 		buffer.limit(count);
				
		if( isBound ) bind();
 	}
 
 	@Override
 	public void bind() {
 		GL10 gl = Gdx.gl10;
 		int textureUnit = 0;
 		int numAttributes = attributes.size();
 
 		for (int i = 0; i < numAttributes; i++) {
 			VertexAttribute attribute = attributes.get(i);
 
 			switch (attribute.usage) {
 			case Usage.Position:
 				byteBuffer.position(attribute.offset);
 				gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
 				gl.glVertexPointer(attribute.numComponents, GL10.GL_FLOAT,
 						attributes.vertexSize, byteBuffer);
 				break;
 
 			case Usage.Color:
 			case Usage.ColorPacked:
 				int colorType = GL10.GL_FLOAT;
 				if (attribute.usage == Usage.ColorPacked)
 					colorType = GL11.GL_UNSIGNED_BYTE;
 				byteBuffer.position(attribute.offset);
 				gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
 				gl.glColorPointer(attribute.numComponents, colorType,
 						attributes.vertexSize, byteBuffer);
 				break;
 
 			case Usage.Normal:
 				byteBuffer.position(attribute.offset);
 				gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
 				gl.glNormalPointer(GL10.GL_FLOAT, attributes.vertexSize,
 						byteBuffer);
 				break;
 
 			case Usage.TextureCoordinates:
 				gl.glClientActiveTexture(GL10.GL_TEXTURE0 + textureUnit);
 				gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 				byteBuffer.position(attribute.offset);
 				gl.glTexCoordPointer(attribute.numComponents, GL10.GL_FLOAT,
 						attributes.vertexSize, byteBuffer);
 				textureUnit++;
 				break;
 
 			default:
 				throw new GdxRuntimeException("unkown vertex attribute type: "
 						+ attribute.usage);
 			}
 		}
 		
 		isBound = true;
 	}
 
 	@Override
 	public void unbind() {
 		GL10 gl = Gdx.gl10;
 		int textureUnit = 0;
 		int numAttributes = attributes.size();
 
 		for (int i = 0; i < numAttributes; i++) {
 
 			VertexAttribute attribute = attributes.get(i);
 			switch (attribute.usage) {
 			case Usage.Position:
 				break; // no-op, we also need a position bound in gles
 			case Usage.Color:
 			case Usage.ColorPacked:
 				gl.glDisableClientState(GL11.GL_COLOR_ARRAY);
 				break;
 			case Usage.Normal:
 				gl.glDisableClientState(GL11.GL_NORMAL_ARRAY);
 				break;
 			case Usage.TextureCoordinates:
 				gl.glClientActiveTexture(GL11.GL_TEXTURE0 + textureUnit);
 				gl.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
 				textureUnit++;
 				break;
 			default:
 				throw new GdxRuntimeException("unkown vertex attribute type: "
 						+ attribute.usage);
 			}
 		}
 		
 		isBound = false;
 	}
 
 	@Override
 	public VertexAttributes getAttributes() {
 		return attributes;
 	}
 }
