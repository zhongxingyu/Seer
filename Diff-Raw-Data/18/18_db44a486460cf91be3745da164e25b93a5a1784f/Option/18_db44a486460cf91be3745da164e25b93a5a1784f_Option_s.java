 package de.ptb.epics.eve.data.measuringstation;
 
 import java.util.Iterator;
 import java.util.List;
 
 /**
  *  This class represents a motor axis of a device.
  * 
  * @author Stephan Rehfeld <stephan.rehfeld( -at -) ptb.de>
  * @author Marcus Michalsky
  */
 public class Option extends AbstractPrePostscanDevice {
 
 	private boolean monitor;
 	
 	/**
 	 * Constructor.
 	 */
 	public Option() {
 		super();
 		this.monitor = false;
 	}
 	
 	/**
 	 * Constructor.
 	 * 
 	 * @param monitor
 	 *            <code>true</code> if option should be monitored,
 	 *            <code>false</code> otherwise
 	 */
 	public Option(boolean monitor) {
 		super();
 		this.monitor = monitor;
 	}
 	
 	
 	
 	/**
 	 * @return the monitor
 	 */
 	public boolean isMonitor() {
 		return monitor;
 	}
 
 
 
 	/**
 	 * @param monitor the monitor to set
 	 */
 	public void setMonitor(boolean monitor) {
 		this.monitor = monitor;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Object clone() {
 		final Option option = new Option();
 
 		option.setClassName(this.getClassName());
 		option.setDisplaygroup(this.getDisplaygroup());
 		option.setValue((Function) (this.getValue() != null ? this.getValue()
 				.clone() : null));
 		this.setName(this.getName());
 		option.setName(this.getName());
 		option.setId(this.getID());
 		option.setUnit((Unit) (this.getUnit() != null ? this.getUnit().clone()
 				: null));
 		option.setMonitor(this.isMonitor());
 		return option;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * Always returns <code>null</code> (
 	 * {@link de.ptb.epics.eve.data.measuringstation.Option}s don't have
 	 * options.
 	 */
 	@Override
 	public List<Option> getOptions() {
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * Always returns <code>null</code> (
 	 * {@link de.ptb.epics.eve.data.measuringstation.Option}s don't have
 	 * options.
 	 */
 	@Override
 	public Iterator<Option> optionIterator() {
 		return null;
 	}
 }
