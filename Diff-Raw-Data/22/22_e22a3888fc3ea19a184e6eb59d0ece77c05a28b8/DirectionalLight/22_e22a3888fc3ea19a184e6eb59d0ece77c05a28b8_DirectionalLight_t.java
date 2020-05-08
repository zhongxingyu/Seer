 package de.tum.in.cindy3dplugin.jogl.lighting;
 
 import javax.media.opengl.GL2;
 
 import org.apache.commons.math.geometry.Vector3D;
 
 import de.tum.in.cindy3dplugin.LightModificationInfo.LightFrame;
 import de.tum.in.cindy3dplugin.LightModificationInfo.LightType;
 
 /**
 * Represents a directional light source. A directional light is defined
 * by a direction from which the light rays originate.
  */
 public class DirectionalLight extends Light {
 	/**
 	 * Light direction. Default direction is (0, -1, 0), so the light comes from
 	 * above.
 	 */
 	private Vector3D direction = new Vector3D(0, -1, 0);
 
 	/**
 	 * Sets the direction of the directional light.
 	 * 
 	 * @param direction
	 *            new direction
 	 */
 	public void setDirection(Vector3D direction) {
 		this.direction = direction;
 	}
 
 	/* (non-Javadoc)
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
 		
 		gl.glLightfv(
 				light,
 				GL2.GL_POSITION,
 				new float[] { (float) -direction.getX(),
 						(float) -direction.getY(), (float) -direction.getZ(),
 						0.0f }, 0);
 		
 		if (frame == LightFrame.CAMERA) {
 			gl.glPopMatrix();
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.jogl.Light#getShaderFillIn()
 	 */
 	@Override
 	public String getShaderFillIn(int light) {
 		return "directionalLight(normal, eye, " + light + ");";
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.jogl.Light#getType()
 	 */
 	@Override
 	public LightType getType() {
 		return LightType.DIRECTIONAL_LIGHT;
 	}
 }
