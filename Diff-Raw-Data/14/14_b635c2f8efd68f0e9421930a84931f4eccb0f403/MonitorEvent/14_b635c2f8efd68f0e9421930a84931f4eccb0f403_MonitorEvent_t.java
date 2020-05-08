 package de.ptb.epics.eve.data.measuringstation.event;
 
 import de.ptb.epics.eve.data.TypeValue;
 import de.ptb.epics.eve.data.measuringstation.Access;
 import de.ptb.epics.eve.data.measuringstation.DetectorChannel;
 import de.ptb.epics.eve.data.measuringstation.Device;
 import de.ptb.epics.eve.data.measuringstation.MotorAxis;
 import de.ptb.epics.eve.data.measuringstation.Option;
 
 /**
  * A monitor event represents an option of a device (e.g. the value of a 
  * detector channel or the position of a discrete motor axis). When it is 
  * triggered depends on its usage as described in 
  * {@link de.ptb.epics.eve.data.scandescription.ControlEvent}.
  * 
  * @author Marcus Michalsky
  * @since 1.19
  */
 public class MonitorEvent extends Event {
 	private Access access;
 	private TypeValue typeValue;
 	private String id;
 	private String name;
 	
 	/**
 	 * Constructs a monitor event for a motor axis.
 	 * 
 	 * @param ma the motor axis
 	 */
 	public MonitorEvent(MotorAxis ma) {
 		super();
 		this.access = ma.getPosition().getAccess();
 		this.typeValue = ma.getPosition().getValue();
		this.adjustTypeValue();
 		this.id = ma.getID();
 		this.name = ma.getName() + " " + (char) 187 + " " + "Position";
 	}
 	
 	/**
 	 * Constructs a monitor event for a detector channel.
 	 * 
 	 * @param ch the detector channel
 	 */
 	public MonitorEvent(DetectorChannel ch) {
 		super();
 		this.access = ch.getRead().getAccess();
 		this.typeValue = ch.getRead().getValue();
		this.adjustTypeValue();
 		this.id = ch.getID();
 		this.name = ch.getName() + " " + (char) 187 + " Value";
 	}
 	
 	/**
 	 * Constructs a monitor event for a (pre-postscan device).
 	 * 
 	 * @param device the device
 	 */
 	public MonitorEvent(Device device) {
 		super();
 		this.access = device.getValue().getAccess();
 		this.typeValue = device.getValue().getValue();
		this.adjustTypeValue();
 		this.id = device.getID();
 		this.name = device.getName() + " " + (char) 187 + " Value";
 	}
 	
 	/**
 	 * Constructs a monitor event for an option.
 	 * 
 	 * @param option the option
 	 */
 	public MonitorEvent(Option option) {
 		super();
 		this.access = option.getValue().getAccess();
 		this.typeValue = option.getValue().getValue();
		this.adjustTypeValue();
 		this.id = option.getID();
 		this.name = option.getParent().getName() + 
 					" " + (char) 187 + " "+ 
 					option.getName();
 	}
 	
	private void adjustTypeValue() {
		if (this.typeValue == null) {
			this.typeValue = new TypeValue(this.access.getType());
		}
	}
	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getId() {
 		return this.id;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getName() {
 		return this.name;
 	}
 
 	/**
 	 * @return the access
 	 */
 	public Access getAccess() {
 		return access;
 	}
 
 	/**
 	 * @return the typeValue
 	 */
 	public TypeValue getTypeValue() {
 		return typeValue;
 	}
 }
