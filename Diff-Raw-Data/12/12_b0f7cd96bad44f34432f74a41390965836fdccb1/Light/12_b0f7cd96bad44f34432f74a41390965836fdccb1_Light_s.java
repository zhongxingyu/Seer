 package js.gles11.tools;
 
 import javax.microedition.khronos.opengles.GL10;
 
 public class Light
 {
 	private int index = GL10.GL_LIGHT0;
 	private GL10 gl = null;
 
	private float[] values = new float[] { 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 1 };
 
 	// field sequence - r g b a
 
 	// offsets
 	// 0 - position
 	// 4 - ambient
 	// 8 - diffuse
 	// 12 - specular
 
 	public Light setProfile(float constantAttenuation, float linearAttenuation, float quadraticAttenuation)
 	{
 		gl.glLightf(index, GL10.GL_CONSTANT_ATTENUATION, constantAttenuation);
 		gl.glLightf(index, GL10.GL_LINEAR_ATTENUATION, linearAttenuation);
 		gl.glLightf(index, GL10.GL_QUADRATIC_ATTENUATION, quadraticAttenuation);
 
 		return this;
 	}
 
 	/**
 	 * 
 	 * @param gl
 	 * @param index
 	 *            - zero based!
 	 * @return
 	 */
 	public Light init(GL10 gl, int index)
 	{
 		this.index = GL10.GL_LIGHT0 + index;
 		this.gl = gl;
 
 		gl.glLightfv(index, GL10.GL_POSITION, values, 0);
 		gl.glLightfv(index, GL10.GL_AMBIENT, values, 4);
 		gl.glLightfv(index, GL10.GL_DIFFUSE, values, 8);
 		gl.glLightfv(index, GL10.GL_SPECULAR, values, 12);
 
 		setProfile(1, 0, 0);
 
 		return this;
 	}
 
 	public Light on()
 	{
 		gl.glEnable(index);
 		return this;
 	}
 
 	public Light off()
 	{
 		gl.glDisable(index);
 		return this;
 	}
 
 	public Light setPosition(float x, float y, float z)
 	{
 		values[0] = x;
 		values[1] = y;
 		values[2] = z;
 		gl.glLightfv(index, GL10.GL_POSITION, values, 0);
 		return this;
 	}
 
 	public Light setAmbient(float r, float g, float b, float a)
 	{
 		values[4] = r;
 		values[5] = g;
 		values[6] = b;
 		values[7] = a;
 		gl.glLightfv(index, GL10.GL_POSITION, values, 4);
 		return this;
 	}
 
 	public Light setDiffuse(float r, float g, float b, float a)
 	{
 		values[8] = r;
 		values[9] = g;
 		values[10] = b;
 		values[11] = a;
 		gl.glLightfv(index, GL10.GL_POSITION, values, 8);
 		return this;
 	}
 
 	public Light setSpecular(GL10 gl, float r, float g, float b, float a)
 	{
 		values[12] = r;
 		values[13] = g;
 		values[14] = b;
 		values[15] = a;
 		gl.glLightfv(index, GL10.GL_POSITION, values, 12);
 		return this;
 	}
 }
