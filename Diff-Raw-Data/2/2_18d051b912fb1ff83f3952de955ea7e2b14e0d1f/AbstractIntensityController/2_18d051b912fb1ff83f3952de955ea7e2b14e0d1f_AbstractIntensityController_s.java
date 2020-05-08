 package jgame.controller;
 
 import jgame.Context;
 import jgame.GObject;
 
 /**
  * A controller that controls the intensity of an object on one or more
  * channels.
  * 
  * @author William Chargin
  * 
  */
 public abstract class AbstractIntensityController implements Controller {
 
 	/**
 	 * Indicates that the controller should control alpha.
 	 */
 	public static final int ALPHA = 1;
 
 	/**
 	 * Indicates that the controller should control scale.
 	 */
 	public static final int SCALE = 2;
 
 	/**
	 * The property mask. Default is {@link #ALPHA}.
 	 */
 	private int properties = ALPHA | SCALE;
 
 	/**
 	 * Adds the given property to the control set. The property should be one
 	 * of:
 	 * <ul>
 	 * <li>{@link #ALPHA}</li>
 	 * <li>{@link #SCALE}</li>
 	 * </ul>
 	 * 
 	 * @param property
 	 *            the property to add
 	 */
 	public void addProperty(int property) {
 		properties |= property;
 	}
 
 	/**
 	 * Clears the property mask. This is equivalent to calling
 	 * {@link #removeProperty(int)} on every property set.
 	 */
 	public void clearProperties() {
 		properties = 0;
 	}
 
 	@Override
 	public void controlObject(GObject target, Context context) {
 		// How much? Ask the subclass.
 		double factor = getFactor(target, context);
 
 		// Can we set alpha?
 		if ((properties & ALPHA) != 0) {
 			// Set it between zero and one.
 			target.setAlpha(Math.max(0, Math.min(1, factor)));
 		}
 
 		// Can we set scale?
 		if ((properties & SCALE) != 0) {
 			// Do so.
 			target.setScale(factor);
 		}
 	}
 
 	/**
 	 * Gets the factor for the intensity.
 	 * 
 	 * @param target
 	 *            the object to control
 	 * @param context
 	 *            the relevant context
 	 * @return the factor
 	 */
 	public abstract double getFactor(GObject target, Context context);
 
 	/**
 	 * Gets the mask of properties controlled by this controller. You can also
 	 * use {@link #isPropertyControlled(int)} to test individual properties.
 	 * 
 	 * @return the bitmask
 	 */
 	public int getProperties() {
 		return properties;
 	}
 
 	/**
 	 * Tests whether the given property is part of the control set. The property
 	 * should be one of:
 	 * <ul>
 	 * <li>{@link #ALPHA}</li>
 	 * <li>{@link #SCALE}</li>
 	 * </ul>
 	 * 
 	 * @param property
 	 *            the property to test
 	 * @return {@code true} if it is part of the control set, or {@code false}
 	 *         if it is not
 	 */
 	public boolean isPropertyControlled(int property) {
 		return (properties & property) != 0;
 	}
 
 	/**
 	 * Removes the given property from the control set. The property should be
 	 * one of:
 	 * <ul>
 	 * <li>{@link #ALPHA}</li>
 	 * <li>{@link #SCALE}</li>
 	 * </ul>
 	 * 
 	 * @param property
 	 *            the property to remove
 	 */
 	public void removeProperty(int property) {
 		properties &= ~property;
 	}
 
 	/**
 	 * Sets the mask of properties controlled by this controller. You can also
 	 * use {@link #addProperty(int)} and {@link #removeProperty(int)} to set
 	 * individual properties.
 	 * 
 	 * @param properties
 	 *            the new bitmask
 	 */
 	public void setProperties(int properties) {
 		this.properties = properties;
 	}
 
 	/**
 	 * Sets the mask of properties controlled by this controller to the
 	 * conjunction of all the given properties. You can also use
 	 * {@link #addProperty(int)} and {@link #removeProperty(int)} to set
 	 * individual properties.
 	 * 
 	 * @param properties
 	 *            the new set of properties
 	 */
 	public void setProperties(int... properties) {
 		// Clear the properties.
 		clearProperties();
 
 		// Loop over each property.
 		for (int property : properties) {
 			// Add it.
 			addProperty(property);
 		}
 	}
 
 }
