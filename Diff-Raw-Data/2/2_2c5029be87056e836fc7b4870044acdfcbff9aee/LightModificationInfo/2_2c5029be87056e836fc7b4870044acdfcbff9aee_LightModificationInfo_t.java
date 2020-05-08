 package de.tum.in.cindy3dplugin;
 
 import java.awt.Color;
 
 /**
  * Stores attributes for the modification of a light source.
  */
 public class LightModificationInfo {
 	/**
 	 * Type of the light source.
 	 */
 	public enum LightType {
 		/**
 		 * Point light source
 		 */
 		POINT_LIGHT,
 		/**
 		 * Directional light source
 		 */
 		DIRECTIONAL_LIGHT,
 		/**
 		 * Spot light source
 		 */
 		SPOT_LIGHT
 	}
 
 	/**
 	 * Frame of reference for light source coordinates.
 	 */
 	public enum LightFrame {
 		/**
 		 * Camera space
 		 */
 		CAMERA,
 		/**
 		 * World space
 		 */
 		WORLD
 	}
 
 	/**
 	 * Ambient color
 	 */
 	private Color ambient = null;
 	/**
 	 * Diffuse color
 	 */
 	private Color diffuse = null;
 	/**
 	 * Specular color
 	 */
 	private Color specular = null;
 	/**
 	 * Frame of reference
 	 */
 	private LightFrame frame = null;
 	/**
 	 * Position
 	 */
 	private double[] position = null;
 	/**
 	 * Direction
 	 */
 	private double[] direction = null;
 
 	/**
 	 * Light type
 	 */
 	private LightType type;
 
 	/**
 	 * Creates a LightModificationInfo for a given light source type.
 	 * 
 	 * @param type
 	 *            light source type
 	 */
 	public LightModificationInfo(LightType type) {
 		this.type = type;
 	}
 
 	/**
 	 * @return true if the ambient color is set
 	 */
 	public boolean hasAmbient() {
 		return ambient != null;
 	}
 
 	/**
 	 * @return ambient color
 	 */
 	public Color getAmbient() {
 		return ambient;
 	}
 
 	/**
 	 * @param ambient
 	 *            new ambient color
 	 */
 	public void setAmbient(Color ambient) {
 		this.ambient = ambient;
 	}
 
 	/**
 	 * @return true if the diffuse color is set
 	 */
 	public boolean hasDiffuse() {
 		return diffuse != null;
 	}
 
 	/**
 	 * @return diffuse color
 	 */
 	public Color getDiffuse() {
 		return diffuse;
 	}
 
 	/**
 	 * @param diffuse
 	 *            new diffuse color
 	 */
 	public void setDiffuse(Color diffuse) {
 		this.diffuse = diffuse;
 	}
 
 	/**
 	 * @return true if the specular color is set
 	 */
 	public boolean hasSpecular() {
 		return specular != null;
 	}
 
 	/**
 	 * @return specular color
 	 */
 	public Color getSpecular() {
 		return specular;
 	}
 
 	/**
 	 * @param specular
 	 *            new specular color
 	 */
 	public void setSpecular(Color specular) {
 		this.specular = specular;
 	}
 
 	/**
 	 * @return true if the frame of reference is set
 	 */
 	public boolean hasFrame() {
 		return frame != null;
 	}
 
 	/**
 	 * @return frame of reference
 	 */
 	public LightFrame getFrame() {
 		return frame;
 	}
 
 	/**
 	 * @param frame
 	 *            new frame of reference
 	 */
 	public void setFrame(LightFrame frame) {
 		this.frame = frame;
 	}
 
 	/**
 	 * @return true if the position is set
 	 */
 	public boolean hasPosition() {
 		return position != null;
 	}
 
 	/**
 	 * @return position
 	 */
 	public double[] getPosition() {
 		return position;
 	}
 
 	/**
 	 * @param position
 	 *            new position
 	 */
 	public void setPosition(double[] position) {
 		this.position = position;
 	}
 
 	/**
 	 * @return true if the direction is set
 	 */
 	public boolean hasDirection() {
		return direction != null;
 	}
 
 	/**
 	 * @return direction
 	 */
 	public double[] getDirection() {
 		return direction;
 	}
 
 	/**
 	 * @param direction
 	 *            new direction
 	 */
 	public void setDirection(double[] direction) {
 		this.direction = direction;
 	}
 
 	/**
 	 * @return light type
 	 */
 	public LightType getType() {
 		return type;
 	}
 
 	/**
 	 * @param type
 	 *            new light type
 	 */
 	public void setType(LightType type) {
 		this.type = type;
 	}
 }
