 package backbone;
 
 import java.io.Serializable;
 
 /**
  * Attribute abstract class
  * 
  * extended by all Attribute containers
  * @author pclay
  *
  */
 public abstract class Attribute implements Serializable {
 	/**
 	 * What the Attribute is called by the GUI
 	 */
 	private String title;
 
 	//NOT ACTUALLY USED
 	protected float conflictMagnitude;
 
 	/**
 	 * The default constructor. Called by sub-classes
 	 * 
 	 * @param title what the Attribute is called by the GUI
 	 */
 	public Attribute(String title) {
 		this.title = title;
 		conflictMagnitude = 0;
 	}
 
 	/**
 	 * Currently, all Attributes are editable
 	 * @return true
 	 */
 	public boolean isEditable() {
 		return true;
 	}
 
 	/**
 	 * All the different types of attributes.
 	 * Used primarily for checking before a cast.
 	 * 
 	 * @author pclay
 	 *
 	 */
 	public enum Type {
 		INT, STRING, BOOLEAN, DOUBLE, MEMBER, GROUPING, UNIT
 	}
 
	/**
	 * Returns what type the Attribute is
	 * 
	 * Required by all Attributes
	 * @return a Type, what type the Attribute is
	 */
 	public abstract Type getType();
 
	/**
	 * Used for GUI and file i/o
	 * 
	 * @return the title
	 */
 	public String getTitle() {
 		return title;
 	}
 
	/**
	 * @deprecated
	 */
 	public float getConflictMagnitude() {
 		return conflictMagnitude;
 	}
 }
