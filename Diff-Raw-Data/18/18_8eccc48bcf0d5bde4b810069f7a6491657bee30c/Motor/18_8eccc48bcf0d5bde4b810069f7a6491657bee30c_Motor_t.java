 package de.ptb.epics.eve.data.measuringstation;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import de.ptb.epics.eve.data.measuringstation.exceptions.ParentNotAllowedException;
 
 /**
  * This class represents a Motor. The main purpose of this class is the 
  * management of the motor axis.
  * 
  * @see de.ptb.epics.eve.data.measuringstation.MotorAxis
  * @author Stephan Rehfeld <stephan.rehfeld( -at -) ptb.de>
  * @version 1.3
  */
 public class Motor extends AbstractMainPhaseDevice {
 
 	/**
 	 * A List, that is holding all axis of this motor
 	 */
 	private List<MotorAxis> axis;
 	
 	/**
 	 * This constructor constructs a and empty Motor.
 	 *
 	 */
 	public Motor() {
 		this.axis = new ArrayList<MotorAxis>();
 	}
 	
 	/**
 	 * Returns a copy of the internal List, that is holding the MotorAxis
 	 * objects.
 	 * 
 	 * @return A List of MotorAxis objects of the Motor.
 	 */
 	public List<MotorAxis> getAxis() {	
 		return new ArrayList<MotorAxis>(this.axis);
 	}
 	
 	/**
 	 * Returns an Iterator over the internal list of MotorAxis objects.
 	 * 
 	 * @return An Iterator object.
 	 */
 	public Iterator<MotorAxis> axisIterator() {
 		return this.axis.iterator();
 	}
 
 	/**
 	 * Adds a MotorAxis to this Motor and sets the Motor as the Parent of the
 	 * MotorAxis.
 	 * 
 	 * @param axis The MotorAxis object, that should be added.
 	 * @return TRUE if the axis has been added.
 	 * @throws IllegalArgumentException if the argument is <code>null</code>
 	 * @throws IllegalArgumentException if axis not allowed for this motor
 	 */
 	public boolean add(final MotorAxis axis) {
 		if(axis == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'axis' must not be null!");
 		}
 		
 		final boolean result = this.axis.add(axis);
 		if(result) {
 			try {
 				axis.setParent( this );
 			} catch( final ParentNotAllowedException e ) {
 				this.axis.remove( axis );
 				throw new IllegalArgumentException("The axis you have passend " +
 						"didn't accept the Motor as parent. " +
 						"Please check your implementation!", e);				
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Removes a MotorAxis from this Motor.
 	 * 
 	 * @param axis A MotorAxis object.
 	 * @return <code>true</code> if the MotorAxis has been removed.
 	 * @throws IllegalArgumentException if the argument is <code>null</code>
 	 */
 	public boolean remove(final MotorAxis axis) {
 		if(axis == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'axis' must not be null!");
 		}
 		final boolean result = this.axis.remove(axis);
 		if(result) {
 			try {
 				axis.setParent(null);
 			} catch(final ParentNotAllowedException e) { }
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * @return a hash 
 	 */
 	@Override
 	public int hashCode() {
 		return super.hashCode();
 	}
 
 	/**
 	 * Checks if argument and calling object are equal.
 	 * 
 	 * @return <code>true</code> if objects are equal, 
 	 * 			</code>false</code> otherwise
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (!super.equals(obj)) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Clones the current Motor.
 	 * 
 	 * @return a clone of the calling Motor
 	 */
 	@Override
 	public Object clone() {
 		
 		final Motor motor = new Motor();
 		for(final MotorAxis axis : this.axis) {
 			motor.add((MotorAxis)axis.clone());
 		}
 		motor.setClassName(this.getClassName());
 		motor.setTrigger((Function)
 				(this.getTrigger()!=null?this.getTrigger().clone():null));
 		motor.setName(this.getName());
 		motor.setId(this.getID());
 		motor.setUnit((Unit)
 				(this.getUnit()!=null?this.getUnit().clone():null));
 		
 		for(final Option option : this.getOptions()) {
 //	TODO, wegnehmen: 	this.add( (Option)option.clone() );
 			motor.add((Option)option.clone());
 		}
 		return motor;
 	}
 }
