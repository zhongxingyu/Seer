 package de.tum.in.cindy3dplugin.jogl.lighting;
 
 import javax.media.opengl.GL2;
 
 import org.apache.commons.math.geometry.Vector3D;
 
 import de.tum.in.cindy3dplugin.LightModificationInfo.LightFrame;
 import de.tum.in.cindy3dplugin.LightModificationInfo.LightType;
 
 /**
 * Represents a point light source. A point light is defined by its emitting
 * position.
  */
 public class PointLight extends Light {
 	/**
 	 * Position of the light source. Default position is the origin (0, 0, 0).
 	 */
 	private Vector3D position = new Vector3D(0, 0, 0);
 
 	/**
 	 * Set the position of the light source.
 	 * 
 	 * @param position
 	 *            new position
 	 */
 	public void setPosition(Vector3D position) {
 		this.position = position;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.tum.in.cindy3dplugin.jogl.Light#setGLState()
 	 */
 	@Override
 	public void setGLState(GL2 gl, int light) {
 		super.setGLState(gl, light);
 
 		if (frame == LightFrame.CAMERA) {
 			gl.glMatrixMode(GL2.GL_MODELVIEW);
 			gl.glPushMatrix();
 			gl.glLoadIdentity();
 		}
 
 		gl.glLightfv(light, GL2.GL_POSITION,
 				new float[] { (float) position.getX(), (float) position.getY(),
 						(float) position.getZ(), 1.0f }, 0);
 
 		if (frame == LightFrame.CAMERA) {
 			gl.glPopMatrix();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.tum.in.cindy3dplugin.jogl.Light#setShaderFillIn()
 	 */
 	@Override
 	public String getShaderFillIn(int light) {
 		return "pointLight(position, normal, eye, " + light + ");";
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.tum.in.cindy3dplugin.jogl.Light#getType()
 	 */
 	@Override
 	public LightType getType() {
 		return LightType.POINT_LIGHT;
 	}
 }
