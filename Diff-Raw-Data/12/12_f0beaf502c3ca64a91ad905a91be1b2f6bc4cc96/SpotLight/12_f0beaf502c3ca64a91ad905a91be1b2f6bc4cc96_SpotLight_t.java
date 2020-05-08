 package de.tum.in.cindy3dplugin.jogl.lighting;
 
 import javax.media.opengl.GL2;
 
 import org.apache.commons.math.geometry.Vector3D;
 
 import de.tum.in.cindy3dplugin.LightModificationInfo.LightFrame;
 import de.tum.in.cindy3dplugin.LightModificationInfo.LightType;
 
 /**
 * Represents a spot light source. A splot light is defined by the emitting
 * position, the light direction and a falloff angle.
  */
 public class SpotLight extends Light {
 	/**
 	 * Light position. Default position is (0, 0, 0).
 	 */
 	private Vector3D position = new Vector3D(0, 0, 0);
 	/**
 	 * Light main direction. Default direction is (0, -1, 0).
 	 */
 	private Vector3D direction = new Vector3D(0, -1, 0);
 	/**
 	 * Spot light cut off angle. Initial value is 180
 	 */
 	private double cutoff = 180;
 	/**
 	 * Spot light attenuation exponent. Initial value is 0.
 	 */
 	private double exponent = 0;
 	
 	/**
 	 * Sets the position of the spot light.
 	 * 
 	 * @param position
 	 *            new position
 	 */
 	public void setPosition(Vector3D position) {
 		this.position = position;
 	}
 
 	/**
 	 * Sets the direction of the spot light.
 	 * 
 	 * @param direction
 	 *            new direction
 	 */
 	public void setDirection(Vector3D direction) {
 		this.direction = direction;
 	}
 	
 	/**
 	 * Sets the cutoff angle of the spotlight. Additionally, the angle is
 	 * clamped and adjusted.
 	 * 
 	 * @param cutoffAngle
 	 *            new cutoff angle
 	 */
 	public void setCutoffAngle(double cutoffAngle) {
 		cutoff = Math.max(0.0, Math.min(cutoffAngle, 180));
 		if (cutoff > 90) {
 			cutoff = 180;
 		}
 	}
 	
 	/**
 	 * Sets the spot exponent. Additionally, the exponent is clamped to the
 	 * range [0,128].
 	 * 
 	 * @param exponent
 	 *            new spot exponent
 	 */
 	public void setExponent(double exponent) {
 		this.exponent = Math.max(0.0, Math.min(exponent, 128.0));
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.jogl.lighting.Light#setGLState(javax.media.opengl.GL2, int)
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
 
 		gl.glLightfv(light, GL2.GL_SPOT_DIRECTION, new float[] {
 				(float) direction.getX(), (float) direction.getY(),
 				(float) direction.getZ()}, 0);
 		gl.glLightf(light, GL2.GL_SPOT_CUTOFF, (float) cutoff);
 		gl.glLightf(light, GL2.GL_SPOT_EXPONENT, (float) exponent);
 
 		if (frame == LightFrame.CAMERA) {
 			gl.glPopMatrix();
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.jogl.lighting.Light#getShaderFillIn(int)
 	 */
 	@Override
 	public String getShaderFillIn(int light) {
 		return "spotLight(position, normal, eye, " + light + ");";
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.jogl.lighting.Light#getType()
 	 */
 	@Override
 	public LightType getType() {
 		return LightType.SPOT_LIGHT;
 	}
 
 }
