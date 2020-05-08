 /**
  *
  */
 package ca.eandb.jmist.framework;
 
 import ca.eandb.jmist.math.Vector3;
 
 /**
  * Represents something that can be translated (moved) around
  * in three dimensional space.
  * @author Brad Kimmel
  */
 public interface Translatable3 {
 
 	/**
 	 * Translates the object along the specified vector.
	 * @param v The vector to translate the object by.
 	 */
 	void translate(Vector3 v);
 
 }
