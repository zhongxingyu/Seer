 package de.ptb.epics.eve.data.scandescription;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import de.ptb.epics.eve.data.DataTypes;
 import de.ptb.epics.eve.data.measuringstation.MotorAxis;
 import de.ptb.epics.eve.data.scandescription.errors.AxisError;
 import de.ptb.epics.eve.data.scandescription.errors.AxisErrorTypes;
 import de.ptb.epics.eve.data.scandescription.errors.IModelError;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateListener;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ModelUpdateEvent;
 
 /**
  * This class describes a behavior of an axis during a main phase of a
  * Scan Modul.
  * 
  * @author Stephan Rehfeld <stephan.rehfeld( -at -) ptb.de>
  * @author Marcus Michalsky
  */
 public class Axis extends AbstractMainPhaseBehavior {
 
 	
 	/*
 	 * The step function of this axis
 	 */
 	private Stepfunctions stepfunction;
 
 	/*
 	 * the file path of the position file
 	 */
 	private String positionfile;
 	
 	/*
 	 * the start value (of the triple start, stop, step width)
 	 * (used if neither a position file nor a position plug in is used)
 	 */
 	private String start;
 	
 	/*
 	 * the stop value (of the triple start, stop, step width)
 	 * (used if neither a position file nor a position plug in is used)
 	 */
 	private String stop;
 	
 	/*
 	 * the step width (of the triple start, stop, step width)
 	 *(used if neither a position file nor a position plug in is used)
 	 */
 	private String stepwidth;
 	
 	/*
 	 * the step count
 	 */
 	private double stepcount;
 	
 	/*
 	 * default position mode is absolute
 	 */
 	private PositionMode positionMode = PositionMode.ABSOLUTE;
 	
 	/*
 	 * the position list
 	 */
 	private String positionlist;
 	
 	/*
 	 * indicates whether the axis is the main axis of the scan module
 	 */
 	private boolean isMainAxis = false;
 	
 	/*
 	 * the scan module the axis corresponds to
 	 */
 	private ScanModule scanModule;
 	
 	/*
 	 * the plug in controller
 	 */
 	private PluginController positionPluginController;
 	
 	/**
 	 * Constructs an <code>Axis</code>.
 	 * 
 	 * @param scanModule the scan module the axis corresponds to
 	 * @throws IllegalArgumentException if the argument is <code>null</code>
 	 */
 	public Axis(final ScanModule scanModule) {
 		if(scanModule == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'scanModule' must not be null!");
 		}
 		this.scanModule = scanModule;
 	}
 	
 	/**
 	 * Returns the scan module the axis is corresponding to.
 	 * 
 	 * @return the corresponding scan module
 	 */
 	public ScanModule getScanModule() {
 		return this.scanModule;
 	}
 	/**
 	 * Returns the path of the position file.
 	 * 
 	 * @return the path of the position file or <code>null</code> if none is set
 	 */
 	public String getPositionfile() {
 		return this.positionfile;
 	}
 
 	/**
 	 * Sets the path of the position file.
 	 * 
 	 * @param positionfile the path of the position file
 	 */
 	public void setPositionfile(final String positionfile) {
 		this.positionfile = positionfile;
 		updateListeners();
 	}
 
 	/**
 	 * Returns the plug in controller of the position plug in.
 	 * 
 	 * @return the plug in controller of the position plug in or 
 	 * 		   <code>null</code> if none is set
 	 */
 	public PluginController getPositionPluginController() {
 		return this.positionPluginController;
 	}
 
 	/**
 	 * Sets the plug in controller of the position plug in.
 	 * 
 	 * @param positionPluginController the plug in controller that should be set
 	 */
 	public void setPositionPluginController(
 			final PluginController positionPluginController) {
 		if(this.positionPluginController != null) {
 			this.positionPluginController.removeModelUpdateListener(this);
 		}
 		this.positionPluginController = positionPluginController;
 		if(this.positionPluginController != null) {
 			this.positionPluginController.addModelUpdateListener(this);
 		}
 		updateListeners();
 	}
 
 	/**
 	 * Returns the start value.
 	 * 
 	 * @return the start value
 	 */
 	public String getStart() {
 		return this.start;
 	}
 
 	/**
 	 * Sets the start value.
 	 * 
 	 * @param start the start value that should be set
 	 */
 	public void setStart(final String start) {
 		this.start = start;
 		updateListeners();
 	}
 
 	/**
 	 * Returns the step function.
 	 * 
 	 * @return the step function
 	 */
 	public String getStepfunctionString() {
 		return Stepfunctions.stepfunctionToString(stepfunction);
 	}
 
 	/**
 	 * Returns the step function 
 	 * 
 	 * @return the step function
 	 */
 	public Stepfunctions getStepfunctionEnum() {
 		return stepfunction;
 	}
 
 	/**
 	 * Sets the step function.
 	 *  
 	 * @param stepfunctionString the step function that should be set
 	 * @throws IllegalArgumentException if the argument is <code>null</code>
 	 */
 	public void setStepfunction(final String stepfunctionString) {
 		if(stepfunctionString == null) {
 			throw new IllegalArgumentException(
 					"The parameter stepfunction must not be null!");
 		}
 		this.stepfunction = Stepfunctions.stepfunctionToEnum(stepfunctionString);
 		updateListeners();
 	}
 
 	/**
 	 * Returns the step width.
 	 * 
 	 * @return the step width or <code>null</code> if none is set
 	 */
 	public String getStepwidth() {
 		return this.stepwidth;
 	}
 
 	/**
 	 * Sets the step width.
 	 * 
 	 * @param stepwidth the step width that should be set
 	 */
 	public void setStepwidth(final String stepwidth) {
 		this.stepwidth = stepwidth;
 		updateListeners();
 	}
 
 	/**
 	 * Returns the stop value
 	 * 
 	 * @return the stop value or <code>null</code> if none is set
 	 */
 	public String getStop() {
 		return this.stop;
 	}
 
 	/**
 	 * Sets the stop value.
 	 * 
 	 * @param stop the stop value that should be set
 	 */
 	public void setStop(final String stop) {
 		this.stop = stop;
 		updateListeners();
 	}
 	
 	/**
 	 * Returns the motor axis.
 	 * 
 	 * @return the motor axis
 	 */
 	public MotorAxis getMotorAxis() {
 		return (MotorAxis)this.abstractDevice;
 	}
 	
 	/**
 	 * Sets the motor axis.
 	 * 
 	 * @param motorAxis the motor axis that should be set
 	 */
 	public void setMotorAxis(final MotorAxis motorAxis) {
 		this.abstractDevice = motorAxis;
 
 		this.setStepfunction("Add");
 		String formattedText = this.getDefaultValue();
 		this.setStart(formattedText);
 		this.setStop(formattedText);
 		this.setStepwidth("0");
 		updateListeners();
 	}
 	
 	/**
 	 * Returns the step count.
 	 * 
 	 * @return the step count
 	 */
 	public double getStepCount() {
 		return this.stepcount;
 	}
 	
 	/**
 	 * Sets the step count.
 	 * 
 	 * @param stepcount the step count that should be set
 	 */
 	public void setStepCount(final double stepcount) {
 		this.stepcount = stepcount;
 		updateListeners();
 	}
 	
 	/**
 	 * Returns the position list.
 	 * 
 	 * @return the position list or <code>null</code> if none is set
 	 */
 	public String getPositionlist() {
 		return this.positionlist;
 	}
 	
 	/**
 	 * Sets the position list.
 	 * 
 	 * @param positionlist the position list that should be set
 	 */
 	public void setPositionlist( final String positionlist ) {
 		this.positionlist = positionlist;
 		updateListeners();
 	}
 
 	/**
 	 * Checks whether the axis is the main axis of the scan module.
 	 * 
 	 * @return <code>true</code> if the axis is the main axis, 
 	 * 		   <code>false</code> otherwise
 	 */
 	public boolean isMainAxis() {
 		return this.isMainAxis;
 	}
 
 	/**
 	 * Sets whether the axis is the main axis of the scan module.
 	 * 
 	 * @param isMainAxis <code>true</code> to set the axis as main axisPass,
 	 * 		  <code>false</code> otherwise
 	 */
 	public void setMainAxis(final boolean isMainAxis) {
 		this.isMainAxis = isMainAxis;
 		updateListeners();
 	}
 
 	/**
 	 * Returns the position mode.
 	 * 
 	 * @return the position mode
 	 */
 	public PositionMode getPositionMode() {
 		return this.positionMode;
 	}
 
 	/**
 	 * Sets the position mode.
 	 * 
 	 * @param positionMode the position mode that should be set
 	 */
 	public void setPositionMode(final PositionMode positionMode) {
 		this.positionMode = positionMode;
 		updateListeners();
 	}
 
 	/**
 	 * Checks whether a value is valid for the behavior. Used to check 
 	 * possible input parameters for start, stop and step width.
 	 * 
 	 * @param value the value that should be checked.
 	 * @return <code>true</code> if the value is valid, 
 	 * 		   <code>false</code> otherwise
 	 * @throws IllegalArgumentException if the argument is <code>null</code>
 	 */
 	public boolean isValuePossible(final String value) {
 		return this.getMotorAxis().isValuePossible(value);
 	}
 	
 	/**
 	 * Return a well-formatted string with a valid value for the datatype.
 	 * If value can not be converted, return a default value
 	 * 
 	 * @param value The value that will be formatted.
 	 * @return a well-formatted string with a valid value
 	 */
 	public String formatValueDefault( final String value ) {
 		return this.getMotorAxis().formatValueDefault( value );
 	}
 
 	/**
 	 * Return a well-formatted string with a valid value for the datatype.
 	 * If value can not be converted, return null
 	 * 
 	 * @param value The value that will be formatted.
 	 * @return a well-formatted string or null
 	 */
 	public String formatValue( final String value ) {
 		return this.getMotorAxis().formatValue( value );
 	}
 
 	/**
 	 * Return a well-formatted string with a valid value for the datatype.
 	 * If value can not be converted, return a default value
 	 * 
 	 * @return a well-formatted string with a valid default value
 	 */
 	public String getDefaultValue() {
 		return this.getMotorAxis().getDefaultValue( );
 	}
 	
 	/**
 	 * This method returns the type of the data type of the motor axis.
 	 * 
 	 * @return The data type of the motor axis.
 	 */
 	public DataTypes getType() {
 		return this.getMotorAxis().getType();
 	}
 
 	/**
 	 * {@inheritDoc} 
 	 */
 	@Override
 	public List<IModelError> getModelErrors() {
 		final List< IModelError > errorList = new ArrayList< IModelError >();
 		if( this.stepfunction == Stepfunctions.ADD || this.stepfunction == Stepfunctions.MULTIPLY ) {
 
 			if( this.start == null || this.start.equals( "" ) ) {
 				errorList.add( new AxisError( this, AxisErrorTypes.START_NOT_SET ) );
 			} else if( !this.getMotorAxis().isValuePossible( this.start ) ) {
 				errorList.add( new AxisError( this, AxisErrorTypes.START_VALUE_NOT_POSSIBLE ) );
 			}
 			if( this.stop == null || this.stop.equals( "" ) ) {
 				errorList.add( new AxisError( this, AxisErrorTypes.STOP_NOT_SET ) );
 			} else if( !this.getMotorAxis().isValuePossible( this.stop ) ) {
 				errorList.add( new AxisError( this, AxisErrorTypes.STOP_VALUE_NOT_POSSIBLE ) );
 			}
 			if( this.stepwidth == null || this.stepwidth.equals( "" ) ) {
 				errorList.add( new AxisError( this, AxisErrorTypes.STEPWIDTH_NOT_SET ) );
 			}
 			if( this.stepcount == -1.0) {
 				errorList.add( new AxisError( this, AxisErrorTypes.STEPCOUNT_NOT_SET ) );
 			}
 			
 		} else if( this.stepfunction == Stepfunctions.FILE ) {
 			if( this.positionfile == null || this.positionfile.equals( "" ) ) {
 				errorList.add( new AxisError( this, AxisErrorTypes.FILENAME_NOT_SET ) );
 			}
 		} else if( this.stepfunction == Stepfunctions.POSITIONLIST ) {
 			if( this.positionlist == null || this.positionlist.equals( "" ) ) {
 				errorList.add( new AxisError( this, AxisErrorTypes.POSITIONLIST_NOT_SET ) );
 			}
 		} else if( this.stepfunction == Stepfunctions.PLUGIN ) {
			if( this.positionPluginController != null ) {
 				errorList.addAll( this.positionPluginController.getModelErrors() );
 				if (this.getPositionPluginController().getModelErrors().size() > 0 ) {
 					errorList.add( new AxisError( this, AxisErrorTypes.PLUGIN_ERROR ) );
 				}
 			}
 		}
 		return errorList;
 	}
 
 	/*
 	 * 
 	 */
 	private void updateListeners()
 	{
 		final CopyOnWriteArrayList<IModelUpdateListener> list = 
 			new CopyOnWriteArrayList<IModelUpdateListener>(modelUpdateListener);
 		
 		Iterator<IModelUpdateListener> it = list.iterator();
 		
 		while(it.hasNext()) {
 			it.next().updateEvent(new ModelUpdateEvent(this, null));
 		}
 	}	
 }
