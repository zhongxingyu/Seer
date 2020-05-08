 package openGLCLInterfaces.openGL.buffers;
 
 import static org.lwjgl.opengl.GL15.*;
 
 import java.nio.*;
 
 import openGLCLInterfaces.Enumerations.BufferTarget;
 import openGLCLInterfaces.Enumerations.BufferUsage;
 
 import org.lwjgl.*;
 
 /**
  * An object that wraps the functionality of OpenGL's buffer objects.
  */
 public class BufferObject {
 	// the buffer id
 	private int bufferID;
 	private BufferTarget target;
 	private BufferUsage usage;
 
 	/**
 	 * Initializes a new buffer object.
 	 * 
 	 * @param target
 	 *            The target for this buffer object.
 	 * @param usage
 	 *            The usage information for memory management.
 	 */
 	public BufferObject(BufferTarget target, BufferUsage usage) {
 		this.bufferID = glGenBuffers();
 		this.target = target;
 		this.usage = usage;
 	}
 
 	/**
 	 * @return Returns the id of this buffer.
 	 */
 	public int getID() {
 		return this.bufferID;
 	}
 
 	/**
 	 * Unbinds the buffer from the target-
 	 */
 	public void unbind() {
 		glBindBuffer(target.getGLEnum(), 0);
 	}
 
 	/**
 	 * Deletes the buffer.
 	 */
 	public void delete() {
 		glDeleteBuffers(this.bufferID);
 	}
 
 	/**
 	 * Binds the buffer to the target.
 	 */
 	public void bind() {
 		glBindBuffer(target.getGLEnum(), this.bufferID);
 	}
 
 	/**
 	 * Buffers data to the buffer discarding old data.
 	 * 
 	 * @param values
 	 *            The data to buffer.
 	 */
 	public void bufferData(byte[] values) {
 		glBufferData(target.getGLEnum(), (ByteBuffer) BufferUtils.createByteBuffer(values.length)
 				.put(values).rewind(), this.usage.getGLEnum());
 	}
 
 	/**
 	 * Buffers data to the buffer discarding old data.
 	 * 
 	 * @param values
 	 *            The data to buffer.
 	 */
 	public void bufferData(float[] values) {
 		glBufferData(target.getGLEnum(), (FloatBuffer) BufferUtils.createFloatBuffer(values.length)
 				.put(values).rewind(), this.usage.getGLEnum());
 	}
 
 	/**
 	 * Buffers data to the buffer discarding old data.
 	 * 
 	 * @param values
 	 *            The data to buffer.
 	 */
 	public void bufferData(short[] values) {
 		glBufferData(target.getGLEnum(), (ShortBuffer) BufferUtils.createShortBuffer(values.length)
 				.put(values).rewind(), this.usage.getGLEnum());
 	}
 
 	/**
 	 * Buffers data to the buffer discarding old data.
 	 * 
 	 * @param values
 	 *            The data to buffer.
 	 */
 	public void bufferData(int[] values) {
 		glBufferData(target.getGLEnum(), (IntBuffer) BufferUtils.createIntBuffer(values.length)
 				.put(values).rewind(), this.usage.getGLEnum());
 	}
 
 	/**
 	 * Allocating a specific memory area.
 	 * 
	 * @param size
 	 *            The size of the area to allocate.
 	 */
 	public void bufferData(long size) {
 		glBufferData(target.getGLEnum(), size, this.usage.getGLEnum());
 	}
 
 	/**
 	 * Writes to the buffer at the index specified by offset.
 	 * 
 	 * @param offset
 	 *            The offset, which determines where the data should be written
 	 *            into the buffer.
 	 * @param values
 	 *            The data to write.
 	 */
 	public void bufferData(long offset, float[] values) {
 		glBufferSubData(target.getGLEnum(), offset,
 				(FloatBuffer) BufferUtils.createFloatBuffer(values.length).put(values).rewind());
 	}
 
 	/**
 	 * Writes to the buffer at the index specified by offset.
 	 * 
 	 * @param offset
 	 *            The offset, which determines where the data should be written
 	 *            into the buffer.
 	 * @param values
 	 *            The data to write.
 	 */
 	public void bufferData(long offset, byte[] values) {
 		glBufferSubData(target.getGLEnum(), offset,
 				(ByteBuffer) BufferUtils.createByteBuffer(values.length).put(values).rewind());
 	}
 
 	/**
 	 * Writes to the buffer at the index specified by offset.
 	 * 
 	 * @param offset
 	 *            The offset, which determines where the data should be written
 	 *            into the buffer.
 	 * @param values
 	 *            The data to write.
 	 */
 	public void bufferData(long offset, short[] values) {
 		glBufferSubData(target.getGLEnum(), offset,
 				(ShortBuffer) BufferUtils.createShortBuffer(values.length).put(values).rewind());
 	}
 
 	/**
 	 * Writes to the buffer at the index specified by offset.
 	 * 
 	 * @param offset
 	 *            The offset, which determines where the data should be written
 	 *            into the buffer.
 	 * @param values
 	 *            The data to write.
 	 */
 	public void bufferData(long offset, int[] values) {
 		glBufferSubData(target.getGLEnum(), offset,
 				(IntBuffer) BufferUtils.createIntBuffer(values.length).put(values).rewind());
 	}
 }
