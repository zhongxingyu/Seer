 package de.ptb.epics.eve.data.scandescription;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.apache.log4j.Logger;
 
 import de.ptb.epics.eve.data.measuringstation.Detector;
 import de.ptb.epics.eve.data.measuringstation.DetectorChannel;
 import de.ptb.epics.eve.data.measuringstation.IMeasuringStation;
 import de.ptb.epics.eve.data.measuringstation.Motor;
 import de.ptb.epics.eve.data.measuringstation.MotorAxis;
 import de.ptb.epics.eve.data.measuringstation.PlugIn;
 import de.ptb.epics.eve.data.scandescription.axismode.AddMultiplyMode;
 import de.ptb.epics.eve.data.scandescription.errors.IModelError;
 import de.ptb.epics.eve.data.scandescription.errors.IModelErrorProvider;
 import de.ptb.epics.eve.data.scandescription.errors.ScanModuleError;
 import de.ptb.epics.eve.data.scandescription.errors.ScanModuleErrorTypes;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventManager;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventMessage;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventMessageEnum;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventTypes;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateListener;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateProvider;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ModelUpdateEvent;
 
 import de.ptb.epics.eve.util.math.statistics.DescriptiveStats;
 
 /**
  * This class represents a scan module.
  * 
  * @author Stephan Rehfeld <stephan.rehfeld( -at -) ptb.de>
  * @author Marcus Michalsky
  * @author Hartmut Scherr
  */
 public class ScanModule implements IModelUpdateListener, IModelUpdateProvider, 
 								IModelErrorProvider, PropertyChangeListener {
 	
 	private static Logger logger = Logger.getLogger(ScanModule.class.getName());
 	
 	// delegated observable
 	private PropertyChangeSupport propertyChangeSupport;
 
 	/** */
 	public static final String PARENT_CONNECTION_PROP = 
 			"ScanModule.PARENT_CONNECTION_PROP";
 	/** */
 	public static final String APPENDED_CONNECTION_PROP = 
 			"ScanModule.APPENDED_CONNECTION_PROP";
 	/** */
 	public static final String NESTED_CONNECTION_PROP = 
 			"ScanModule.NESTED_CONNECTION_PROP";
 	/** */
 	public static final String MAIN_AXIS_PROP = "mainAxis";
 	
 	/** */
 	public static final String CHANNELS_PROP = "channels";
 	
 	/** */
 	public static final String VALUE_COUNT_PROP = "valueCount";
 	
 	/** */
 	public static final String TRIGGER_DELAY_PROP = "triggerDelay";
 	
 	/** */
 	public static final String SETTLE_TIME_PROP = "settleTime";
 	
 	/** */
 	public static final String TRIGGER_CONFIRM_AXIS_PROP = "triggerConfirmAxis";
 
 	/** */
 	public static final String TRIGGER_CONFIRM_CHANNEL_PROP = "triggerConfirmChannel";
 	
 	/** */
 	public static final String TYPE_PROP = "type";
 	
 	/** */
 	public static int default_width = 70;
 	/** */
 	public static int default_height = 30;
 	
 	// the id of the scan module
 	private int id;
 	
 	// the type of the scan module
 	private ScanModuleTypes type;
 	
 	// the name of the scan module
 	private String name;
 	
 	// the number of measurements per position
 	private int valueCount;
 	
 	// the settle time
 	private double settleTime;
 	
 	// the trigger delay
 	private double triggerDelay;
 	
 	// indicates whether a trigger should be confirmed by hand
 	private boolean triggerConfirmAxis;
 	private boolean triggerConfirmChannel;
 	
 	// a list containing all prescans
 	private List<Prescan> prescans;
 	
 	// a list containing all postscans
 	private List<Postscan> postscans;
 	
 	// a list containing all channels
 	private List<Channel> channels;
 	
 	// a list containing all axes
 	private List<Axis> axes;
 	
 	// indicates whether an axis in the scan module is set as main axis
 	private Axis mainAxis;
 	
 	// a list containing all plot windows
 	private List<PlotWindow> plotWindows;
 	
 	// the connector to the appended scan module
 	private Connector appended;
 	
 	// the connector to the nested scan module
 	private Connector nested;
 	
 	// the connector to the parent element
 	private Connector parent;
 	
 	// list of ControlEvents for the trigger events
 	private List<ControlEvent> triggerEvents;
 	
 	// list of ControlEvents for the redo events
 	private List<ControlEvent> redoEvents;
 	
 	// list of ControlEvents for the break events
 	private List<ControlEvent> breakEvents;
 	
 	// list of PauseEvents for the pause events
 	private List<PauseEvent> pauseEvents;
 	
 	// The chain of the scan module
 	private Chain chain;
 	
 	// the x position of the scan module in the graphical editor
 	private int x;
 	
 	// the y position of the scan module in the graphical editor
 	private int y;
 	
 	private int width;
 	private int height;
 	
 	// the control event manager that controls the break events
 	private ControlEventManager breakControlEventManager;
 	
 	// the control event manager that controls the redo events
 	private ControlEventManager redoControlEventManager;
 	
 	// the control event manager that controls the trigger events
 	private ControlEventManager triggerControlEventManager;
 	
 	// the control event manager that controls the pause events
 	private ControlEventManager pauseControlEventManager;
 	
 	// List that is holding all objects that need to get an update message 
 	// if this object was updated.
 	private List<IModelUpdateListener> updateListener;
 		
 	// list that holds all Positionings
 	private List<Positioning> positionings;
 	
 	/**
 	 * Constructs a <code>ScanModule</code> with the given id.
 	 * 
 	 * @param id the id that should be set
 	 * @throws IllegalArgumentException if <code>id</code> is less than 1
 	 */
 	public ScanModule(final int id) {
 		if(id < 1) {
 			throw new IllegalArgumentException(
 					"The parameter 'id' must be larger than 0!");
 		}
 		this.id = id;
 		this.prescans = new ArrayList<Prescan>();
 		this.postscans = new ArrayList<Postscan>();
 		this.channels = new ArrayList<Channel>();
 		this.axes = new ArrayList<Axis>();
 		this.mainAxis = null;
 		this.plotWindows = new ArrayList<PlotWindow>();
 		this.valueCount = 1;
 		this.settleTime = 0.0;
 		this.triggerDelay = 0.0;
 		this.triggerEvents = new ArrayList<ControlEvent>();
 		this.redoEvents = new ArrayList<ControlEvent>();
 		this.breakEvents = new ArrayList<ControlEvent>();
 		this.pauseEvents = new ArrayList<PauseEvent>();
 		this.type = ScanModuleTypes.CLASSIC;
 		this.name = "";
 
 		this.triggerConfirmAxis = false;
 		this.triggerConfirmChannel = false;
 		
 		this.breakControlEventManager = new ControlEventManager(
 				this, this.breakEvents, ControlEventTypes.CONTROL_EVENT);
 		this.redoControlEventManager = new ControlEventManager(
 				this, this.redoEvents, ControlEventTypes.CONTROL_EVENT);
 		this.pauseControlEventManager = new ControlEventManager(
 				this, this.pauseEvents, ControlEventTypes.PAUSE_EVENT);
 		this.triggerControlEventManager = new ControlEventManager(
 				this, this.triggerEvents, ControlEventTypes.CONTROL_EVENT);
 		
 		this.breakControlEventManager.addModelUpdateListener(this);
 		this.redoControlEventManager.addModelUpdateListener(this);
 		this.pauseControlEventManager.addModelUpdateListener(this);
 		this.triggerControlEventManager.addModelUpdateListener(this);
 		
 		this.updateListener = new ArrayList<IModelUpdateListener>();
 		
 		this.positionings = new ArrayList<Positioning>();
 
 		this.width = ScanModule.default_width;
 		this.height = ScanModule.default_height;
 		
 		this.propertyChangeSupport = new PropertyChangeSupport(this);
 	}
 
 	/**
 	 * Gives back an Array that contains all Prescans.
 	 * 
 	 * @return An Array, that contains all Prescans.
 	 */
 	public Prescan[] getPrescans() {
 		return this.prescans.toArray(new Prescan[0]);
 	}
 	
 	/**
 	 * Gives back an Array that contains all Postscans.
 	 * 
 	 * @return An Array, that contains all Postscans.
 	 */
 	public Postscan[] getPostscans() {
 		return this.postscans.toArray(new Postscan[0]);
 	}
 	
 	/**
 	 * Gives back an Array that contains all channel behaviors.
 	 * 
 	 * @return An Array, that contains all channel behaviors.
 	 */
 	public Channel[] getChannels() {
 		return this.channels.toArray(new Channel[0]);
 	}
 	
 	/**
 	 * Gives back an Array that contains all axis behaviors.
 	 * 
 	 * @return An Array, that contains all axes behaviors.
 	 */
 	public Axis[] getAxes() {
 		return this.axes.toArray(new Axis[0]);
 	}
 	
 	/**
 	 * @return the mainAxis
 	 */
 	public Axis getMainAxis() {
 		return mainAxis;
 	}
 
 	/**
 	 * Gives back an Array that contains all plot windows.
 	 * 
 	 * @return An Array, that contains all plot windows.
 	 */
 	public PlotWindow[] getPlotWindows() {
 		return this.plotWindows.toArray(new PlotWindow[0]);
 	}
 	
 	/**
 	 * This method returns an array of all positionings.
 	 * 
 	 * @return An array of all positionings.
 	 */
 	public Positioning[] getPositionings() {
 		return this.positionings.toArray(new Positioning[0]);
 	}
 	
 	/**
 	 * Adds a prescan to the Scan Modul.
 	 * 
 	 * @param prescan The prescan that should be added to the Scan Modul.
 	 */
 	public void add(final Prescan prescan) {
 		this.prescans.add(prescan);
 		prescan.addModelUpdateListener(this);
 		updateListeners();
 	}
 	
 	/**
 	 * Adds a postscan to the Scan Modul.
 	 * 
 	 * @param postscan The postscan that should be added to the Scan Modul.
 	 */
 	public void add(final Postscan postscan) {
 		this.postscans.add(postscan);
 		postscan.addModelUpdateListener(this);
 		updateListeners();
 	}
 	
 	/**
 	 * Adds a channel behavior to the Scan Modul.
 	 * 
 	 * @param channel The channel behavior that should be added to the Scan 
 	 * Modul.
 	 */
 	public void add(final Channel channel) {
 		channel.addModelUpdateListener(this);
 		channel.addPropertyChangeListener("normalizeChannel", this);
 		this.channels.add(channel);
 		this.propertyChangeSupport.firePropertyChange(ScanModule.CHANNELS_PROP,
 				null, this.channels);
 		// channel has to notice if its normalize channel is removed
 		this.propertyChangeSupport.addPropertyChangeListener(
 				ScanModule.CHANNELS_PROP, channel);
 		propertyChangeSupport.firePropertyChange("addChannel", channel, null);
 		updateListeners();
 	}
 	
 	/**
 	 * Adds a axis behavior to the Scan Modul.
 	 * 
 	 * @param axis The axis behavior that should be added to the Scan Modul.
 	 */
 	public void add(final Axis axis) {
 		axis.addModelUpdateListener(this);
 		// scan module has to be notified if an axis is set as main axis:
 		axis.addPropertyChangeListener(AddMultiplyMode.MAIN_AXIS_PROP, this);
 		// each axis has to be notified that some axis is set as main axis 
 		// (or reset)
 		this.propertyChangeSupport.addPropertyChangeListener(
 				ScanModule.MAIN_AXIS_PROP, axis);
 		this.axes.add(axis);
 		if (axis.isMainAxis()) {
 			this.mainAxis = axis;
 		}
 		if (axis.getMode() instanceof AddMultiplyMode<?>) {
 			((AddMultiplyMode<?>)axis.getMode()).matchMainAxis(this.mainAxis);
 		}
 		propertyChangeSupport.firePropertyChange("addAxis", axis, null);
 		updateListeners();
 	}
 	
 	/**
 	 * Adds a plot window to the Scan Modul.
 	 * 
 	 * @param plotWindow The plot window that should be added to the Scan Modul.
 	 */
 	public void add(final PlotWindow plotWindow) {
 		this.plotWindows.add(plotWindow);
 		plotWindow.addModelUpdateListener(this);
 		updateListeners();
 	}
 	
 	/**
 	 * This methods adds a positioning to a scan module.
 	 * 
 	 * @param positioning The positioning to add.
 	 */
 	public void add(final Positioning positioning) {
 		this.positionings.add(positioning);
 		positioning.addModelUpdateListener(this);
 		updateListeners();
 	}
 	
 	/**
 	 * Removes a prescan from the Scan Modul.
 	 * 
 	 * @param prescan The prescan that should be removed.
 	 */
 	public void remove(final Prescan prescan) {
 		this.prescans.remove(prescan);
 		prescan.removeModelUpdateListener(this);
 		updateListeners();
 	}
 	
 	/**
 	 * Removes a postscan from the Scan Modul.
 	 * 
 	 * @param postscan The postscan that should be removed.
 	 */
 	public void remove(final Postscan postscan) {
 		this.postscans.remove(postscan);
 		postscan.removeModelUpdateListener(this);
 		updateListeners();
 	}
 	
 	/**
 	 * Removes a channel behavior from the Scan Modul.
 	 * 
 	 * @param channel The channel behavior that should be removed.
 	 */
 	public void remove(final Channel channel) {
 		// falls es DetektorReadyEvents zu dem Channel gibt, werden diese 
 		// entfernt
 		if (channel.getDetectorReadyEvent() != null) {
 			channel.getScanModule().getChain().getScanDescription().
 					removeEventById(channel.getDetectorReadyEvent().getID());
 			channel.setDetectorReadyEvent(null);
 		}
 
 		// 1. log off listener
 		channel.removeModelUpdateListener(this);
 		channel.removePropertyChangeListener("normalizeChannel", this);
 		// channel no longer needs to listen to changes
 		this.propertyChangeSupport.removePropertyChangeListener(
 				ScanModule.CHANNELS_PROP, channel);
 		// 2. remove channel
 		this.channels.remove(channel);
 		// 3. tell that channel was removed
 		this.propertyChangeSupport.firePropertyChange(ScanModule.CHANNELS_PROP,
 				null, this.channels);
 		propertyChangeSupport.firePropertyChange("removeChannel", channel, null);
 		propertyChangeSupport.firePropertyChange("removePosChannel", channel, null);
 		updateListeners();
 	}
 	
 	/**
 	 * Removes a axis behavior from the Scan Modul.
 	 * 
 	 * @param axis The axis behavior that should be removed.
 	 */
 	public void remove(final Axis axis) {
 		axis.removeModelUpdateListener(this);
 		axis.removePropertyChangeListener(AddMultiplyMode.MAIN_AXIS_PROP, this);
 		this.propertyChangeSupport.removePropertyChangeListener(
 				ScanModule.MAIN_AXIS_PROP, axis);
 
 		this.axes.remove(axis);
 		
 		propertyChangeSupport.firePropertyChange("removeAxis", axis, null);
 		propertyChangeSupport.firePropertyChange("removePosAxis", axis, null);
 		
 		if (this.mainAxis != null && this.mainAxis.equals(axis)) {
 			this.propertyChangeSupport.firePropertyChange(
 					ScanModule.MAIN_AXIS_PROP, this.mainAxis,
 					this.mainAxis = null);
 		}
 		updateListeners();
 	}
 	
 	/**
 	 * Removes a plot window behavior from the Scan Modul.
 	 * 
 	 * @param plotWindow The plot window that should be removed.
 	 */
 	public void remove(final PlotWindow plotWindow) {
 		// remove property Listener of plotWindow
 		this.removePropertyChangeListener("removeAxis", plotWindow);
 		this.removePropertyChangeListener("removeChannel", plotWindow);
 		this.removePropertyChangeListener("addAxis", plotWindow);
 		
 		propertyChangeSupport.firePropertyChange("removePlot", plotWindow, null);
 
 		this.plotWindows.remove(plotWindow);
 		plotWindow.removeModelUpdateListener(this);
 		updateListeners();
 	}
 	
 	/**
 	 * This method removes a positioning from the scan module.
 	 * 
 	 * @param positioning The positioning to remove.
 	 */
 	public void remove(final Positioning positioning) {
 		this.positionings.remove(positioning);
 		positioning.removeModelUpdateListener(this);
 		updateListeners();
 	}
 	
 	/**
 	 * Gives back the connector, that brings you to the appended scan modul.
 	 * 
 	 * @return The connector to the appended scan modul or null if it's not 
 	 * setted.
 	 */
 	public Connector getAppended() {
 		return appended;
 	}
 
 
 	/**
 	 * Sets the connector, that brings you to the appended scan modul.
 	 * 
 	 * @param appended The connector that brings you to the appended scan modul.
 	 */
 	public void setAppended(final Connector appended) {
 		this.propertyChangeSupport.firePropertyChange(
 				ScanModule.APPENDED_CONNECTION_PROP, this.appended,
 				this.appended = appended);
 		updateListeners();
 	}
 
 	/**
 	 * Gives back the id of the scan modul.
 	 * 
 	 * @return The id of the scan modul.
 	 */
 	public int getId() {
 		return id;
 	}
 
 	/**
 	 * Sets the id of the scan modul.
 	 * 
 	 * @param id The new id of the scan modul.
 	 */
 	public void setId(final int id) {
 		if(id < 1) {
 			throw new IllegalArgumentException(
 					"The parameter 'id' must be larger than 0!");
 		}
 		this.id = id;
 		updateListeners();
 	}
 
 	/**
 	 * Gives back the name of the scan modul.
 	 * 
 	 * @return The name of the scan modul.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Sets the name of the scan module.
 	 * 
 	 * @param name The name of the scan modul. Must not be null!
 	 */
 	public void setName(final String name) {
 		this.propertyChangeSupport.firePropertyChange("name", this.name,
 				this.name = name);
 		updateListeners();
 	}
 
 	/**
 	 * Gives back the Connector that brings you to the nested scan module.
 	 * 
 	 * @return The connector to the nested scan module.
 	 */
 	public Connector getNested() {
 		return nested;
 	}
 
 	/**
 	 * Sets the Connector to the nested scan module.
 	 * 
 	 * @param nested The Connector to the nested scan module.
 	 */
 	public void setNested(final Connector nested) {
 		this.propertyChangeSupport.firePropertyChange(
 				ScanModule.NESTED_CONNECTION_PROP, this.nested,
 				this.nested = nested);
 		updateListeners();
 	}
 
 	/**
 	 * Gives back the Connector that brings you to the parent element.
 	 * 
 	 * @return The Connector to the parent Element.
 	 */
 	public Connector getParent() {
 		return parent;
 	}
 
 	/**
 	 * Sets the Connector to the parent element.
 	 * 
 	 * @param parent The Connector to the parent element.
 	 */
 	public void setParent(final Connector parent) {
 		this.propertyChangeSupport.firePropertyChange(PARENT_CONNECTION_PROP,
 				this.parent, this.parent = parent);
 		updateListeners();
 	}
 
 
 
 	/**
 	 * Gives back the settle time of the scan module.
 	 * 
 	 * @return The settle time.
 	 */
 	public double getSettleTime() {
 		return settleTime;
 	}
 
 	/**
 	 * Sets the settle time.
 	 * 
 	 * @param settletime The settle time.
 	 */
 	public void setSettleTime(final double settletime) {
 		this.propertyChangeSupport.firePropertyChange(
 				ScanModule.SETTLE_TIME_PROP, this.settleTime,
 				this.settleTime = settletime);
 		updateListeners();
 	}
 
 	/**
 	 * Returns whether trigger configrmation is enabled.
 	 * 
 	 * @return whether trigger confirmation is enabled
 	 */
 	public boolean isTriggerConfirmAxis() {
 		return triggerConfirmAxis;
 	}
 
 	/**
 	 * Sets if trigger have to be confirmed by hand.
 	 * 
 	 * @param triggerconfirmaxis
 	 *            <code>true</code> to enable manual trigger, <code>false</code>
 	 *            otherwise
 	 */
 	public void setTriggerConfirmAxis(final boolean triggerconfirmaxis) {
 		this.propertyChangeSupport.firePropertyChange(
 				ScanModule.TRIGGER_CONFIRM_AXIS_PROP, this.triggerConfirmAxis,
 				this.triggerConfirmAxis = triggerconfirmaxis);
 		updateListeners();
 	}
 
 	/**
 	 * @return the triggerconfirmchannel
 	 */
 	public boolean isTriggerConfirmChannel() {
 		return triggerConfirmChannel;
 	}
 
 	/**
 	 * @param triggerconfirmchannel the triggerconfirmchannel to set
 	 */
 	public void setTriggerConfirmChannel(boolean triggerconfirmchannel) {
 		this.propertyChangeSupport.firePropertyChange(
 				ScanModule.TRIGGER_CONFIRM_CHANNEL_PROP,
 				this.triggerConfirmChannel,
 				this.triggerConfirmChannel = triggerconfirmchannel);
 		updateListeners();
 	}
 
 	/**
 	 * Gives back the trigger delay.
 	 * 
 	 * @return The trigger delay
 	 */
 	public double getTriggerDelay() {
 		return triggerDelay;
 	}
 
 	/**
 	 * Sets the trigger delay
 	 * 
 	 * @param triggerdelay The trigger delay
 	 */
 	public void setTriggerDelay(final double triggerdelay) {
 		this.propertyChangeSupport.firePropertyChange(
 				ScanModule.TRIGGER_DELAY_PROP, this.triggerDelay,
 				this.triggerDelay = triggerdelay);
 		updateListeners();
 	}
 
 
 	/**
 	 * @return the valuecount
 	 */
 	public int getValueCount() {
 		return valueCount;
 	}
 
 	/**
 	 * @param valuecount the valuecount to set
 	 * @throws IllegalArgumentException if <code>valuecount</code> < 1
 	 */
 	public void setValueCount(int valuecount) {
 		if (valuecount < 1) {
 			throw new IllegalArgumentException("valuecount must be > 0!");
 		}
 		this.propertyChangeSupport.firePropertyChange(
 				ScanModule.VALUE_COUNT_PROP, this.valueCount,
 				this.valueCount = valuecount);
 		updateListeners();
 	}
 
 	/**
 	 * Gives back the type of the scan modul.
 	 * 
 	 * @return The type of the scan modul.
 	 */
 	public ScanModuleTypes getType() {
 		return type;
 	}
 
 	/**
 	 * Sets the type of the scan modul.
 	 * 
 	 * @param type The type of the scan modul.
 	 */
 	public void setType(final ScanModuleTypes type) {
 		this.propertyChangeSupport.firePropertyChange(ScanModule.TYPE_PROP,
 				this.type, this.type = type);
 		updateListeners();
 	}
 
 	/**
 	 * Gives back the Chain, where this scan modul is in.
 	 * 
 	 * @return The Chain where the scan modul is in or null if it is in no 
 	 * chain.
 	 */
 	public Chain getChain() {
 		return chain;
 	}
 
 	/**
 	 * Sets the chain, where this scan modul is in. This method gets called by 
 	 * the add and remove method of Chain.
 	 * 
 	 * @param chain The Chain where the scan modul is in.
 	 */
 	protected void setChain(final Chain chain) {
 		this.chain = chain;
 	}
 
 	/**
 	 * Returns all used plot window Ids.
 	 * 
 	 * @return all used plot wind Ids
 	 */
 	public List<Integer> getPlotIds() {
 		List<Integer> list = new ArrayList<Integer>();
 		for(PlotWindow pw : this.plotWindows) {
 			list.add(pw.getId());
 		}
 		return list;
 	}
 	
 	/**
 	 * Gives back the x-position of the scan modul in the graphic diagramm.
 	 * 
 	 * @return The x-position of the scan modul in the graphic diagram.
 	 */
 	public int getX() {
 		return x;
 	}
 
 	/**
 	 * Sets the x-position in the graphical diagramm.
 	 * 
 	 * @param x The x-position in the graphical diagramm.
 	 */
 	public void setX(final int x) {
 		this.propertyChangeSupport.firePropertyChange("x", this.x, this.x = x);
 		updateListeners();
 	}
 
 	/**
 	 * Gives back the y-position in the graphical diagram.
 	 * 
 	 * @return The y-position in the graphical diagram.
 	 */
 	public int getY() {
 		return y;
 	}
 
 	/**
 	 * Sets the y-position in the graphical diagram
 	 * 
 	 * @param y The y-position in the graphical diagram.
 	 */
 	public void setY(final int y) {
 		this.propertyChangeSupport.firePropertyChange("y", this.y, this.y = y);
 		updateListeners();
 	}
 
 	/**
 	 * @return the width
 	 */
 	public int getWidth() {
 		return width;
 	}
 
 	/**
 	 * @return the height
 	 */
 	public int getHeight() {
 		return height;
 	}
 
 	/**
 	 * Adds a pause event to the scan modul.
 	 * 
 	 * @param pauseEvent The pause event that should be added to the scan modul.
 	 * @return Gives back 'true' if the event has been added and false if not.
 	 */
 	public boolean addPauseEvent(final PauseEvent pauseEvent) {
 		if (this.pauseEvents.add(pauseEvent)) {
 			pauseEvent.addModelUpdateListener(this.pauseControlEventManager);
 			this.pauseControlEventManager.updateEvent(new ModelUpdateEvent(
 					this, new ControlEventMessage(pauseEvent, 
 					ControlEventMessageEnum.ADDED)));
 			return true;
 		} 
 		return false;
 	}
 	
 	/**
 	 * Removes a pause event from the scan modul.
 	 * 
 	 * @param pauseEvent The pause event that should be removed from the scan 
 	 * modul.
 	 * @return Gives back 'true' if the event has been removed and false if not.
 	 */
 	public boolean removePauseEvent(final PauseEvent pauseEvent) {
 		if (this.pauseEvents.remove(pauseEvent)) {
 			pauseEvent.removeModelUpdateListener(this.pauseControlEventManager);
 			this.pauseControlEventManager.updateEvent(new ModelUpdateEvent( 
 					this, new ControlEventMessage(pauseEvent, 
 					ControlEventMessageEnum.REMOVED)));
 			return true;
 		} 
 		return false;
 	}
 	
 	/**
 	 * Adds a break event to the scan modul.
 	 * 
 	 * @param breakEvent The break event that should be added to the scan modul.
 	 * @return Gives back 'true' if the event has been added and false if not.
 	 */
 	public boolean addBreakEvent(final ControlEvent breakEvent) {
 		if (this.breakEvents.add(breakEvent)) {
 			breakEvent.addModelUpdateListener(this.breakControlEventManager);
 			this.breakControlEventManager.updateEvent(new ModelUpdateEvent(
 					this, new ControlEventMessage(breakEvent, 
 					ControlEventMessageEnum.ADDED)));
 			return true;
 		} 
 		return false;
 	}
 	
 	/**
 	 * Removes a break event from the scan modul.
 	 * 
 	 * @param breakEvent The break event that should be removed from the scan 
 	 * modul.
 	 * @return Gives back 'true' if the event has been removed and false if not.
 	 */
 	public boolean removeBreakEvent(final ControlEvent breakEvent) {
 		if (this.breakEvents.remove(breakEvent)) {
 			breakEvent.removeModelUpdateListener(this.breakControlEventManager);
 			this.breakControlEventManager.updateEvent(new ModelUpdateEvent(
 					this, new ControlEventMessage(breakEvent, 
 					ControlEventMessageEnum.REMOVED)));
 			return true;
 		} 
 		return false;
 	}
 	
 	/**
 	 * Adds a redo event to the scan modul.
 	 * 
 	 * @param redoEvent The redo event that should be added to the scan modul.
 	 * @return Gives back 'true' if the event has been added and false if not.
 	 */
 	public boolean addRedoEvent(final ControlEvent redoEvent) {
 		if (this.redoEvents.add(redoEvent)) {
 // Die wesentlichen Aufgaben soll der EventManager übernehmen
 // 1.) Hinzufügen und entfernen eines Events
 // 2.) Listener im Model an und abmelden
 // Vom Prinzip her bleibt nur sowas wie diese Zeile übrig:
 //			this.redoControlEventManager.addControlEvent(redoEvent);
 //			updateListeners();
 			this.redoControlEventManager.updateEvent(new ModelUpdateEvent(
 					this, new ControlEventMessage(redoEvent, 
 					ControlEventMessageEnum.ADDED)));
 // auch in den eventManager:
 			redoEvent.addModelUpdateListener(this.redoControlEventManager);
 			return true;
 		} 
 		return false;
 	}
 	
 	/**
 	 * Removes a redo event from the scan modul.
 	 * 
 	 * @param redoEvent The redo event that should be removed from the scan 
 	 * modul.
 	 * @return Gives back 'true' if the event has been removed and false if not.
 	 */
 	public boolean removeRedoEvent(final ControlEvent redoEvent) {
 		if (this.redoEvents.remove(redoEvent)) {
 			this.redoControlEventManager.updateEvent(new ModelUpdateEvent(
 					this, new ControlEventMessage(redoEvent, 
 					ControlEventMessageEnum.REMOVED)));
 			redoEvent.removeModelUpdateListener(this.redoControlEventManager);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Adds a trigger event to the scan modul.
 	 * 
 	 * @param triggerEvent The trigger event that should be added to the scan 
 	 * modul.
 	 * @return Gives back 'true' if the event has been added and false if not.
 	 */
 	public boolean addTriggerEvent(final ControlEvent triggerEvent) {
 		if (this.triggerEvents.add(triggerEvent)) {
 			this.triggerControlEventManager.updateEvent(new ModelUpdateEvent(
 					this, new ControlEventMessage(triggerEvent, 
 					ControlEventMessageEnum.ADDED)));
 			triggerEvent.addModelUpdateListener(
 					this.triggerControlEventManager);
 			return true;
 		} 
 		return false;
 	}
 	
 	/**
 	 * Removes a trigger event from the scan modul.
 	 * 
 	 * @param triggerEvent The trigger event that should be removed from the 
 	 * scan modul.
 	 * @return Gives back 'true' if the event has been removed and false if not.
 	 */
 	public boolean removeTriggerEvent(final ControlEvent triggerEvent) {
 		if (this.triggerEvents.remove(triggerEvent)) {
 			this.triggerControlEventManager.updateEvent(new ModelUpdateEvent(
 					this, new ControlEventMessage(triggerEvent, 
 					ControlEventMessageEnum.REMOVED)));
 			triggerEvent.removeModelUpdateListener(
 					this.triggerControlEventManager);
 			return true;
 		} 
 		return false;
 	}
 	
 	/**
 	 * Checks whether the given pause event is a pause event of the scan module.
 	 * 
 	 * @param controlEvent the pause event that should be checked
 	 * @return <code>true</code> if the given pause event is a pause event of 
 	 * 			the scan module, <code>false</code> otherwise
 	 */
 	public boolean isPauseEventOfScanModule(final PauseEvent controlEvent) {
 		return this.pauseEvents.contains(controlEvent);
 	}
 	
 	/**
 	 * Checks whether the given control event is a redo event of the scan 
 	 * module.
 	 * 
 	 * @param redoEvent the control event that should be checked
 	 * @return <code>true</code> if the given control event is a redo event of 
 	 * 			the scan module, <code>false</code> otherwise
 	 */
 	public boolean isRedoEventOfScanModule(final ControlEvent redoEvent) {
 		return this.redoEvents.contains(redoEvent);
 	}
 	
 	/**
 	 * Checks whether the given control event is a break event of the scan 
 	 * module.
 	 * 
 	 * @param breakEvent the control event that should be checked
 	 * @return <code>true</code> if the given control event is a break event of 
 	 * 			the scan module, <code>false</code> otherwise
 	 */
 	public boolean isBreakEventOfScanModule(final ControlEvent breakEvent) {
 		return this.breakEvents.contains(breakEvent);
 	}
 	
 	/**
 	 * Checks whether the given control event is a trigger event of the scan 
 	 * module.
 	 * 
 	 * @param triggerEvent the control event that should be checked
 	 * @return <code>true</code> if the given control event is a trigger event 
 	 * 			of the scan module, <code>false</code> otherwise
 	 */
 	public boolean isTriggerEventOfScanModule(final ControlEvent triggerEvent) {
 		return this.triggerEvents.contains(triggerEvent);
 	}
 
 	
 	
 	/**
 	 * Checks whether the given control event is a pause, redo or break event 
 	 * of the scan module.
 	 * 
 	 * @param controlEvent the control event that should be checked
 	 * @return <code>true</code> if the given control event is a pause, redo or 
 	 * 			break event of the scan module
 	 */
 	public boolean isAEventOfTheScanModul(final ControlEvent controlEvent) {
 		return (controlEvent instanceof PauseEvent && 
 				this.isPauseEventOfScanModule((PauseEvent)controlEvent)) || 
 				this.isBreakEventOfScanModule(controlEvent) || 
 				this.isRedoEventOfScanModule(controlEvent) || 
 				this.isTriggerEventOfScanModule(controlEvent);
 	}
 	
 	/**
 	 * Returns an {@link java.util.Iterator} ofthe pause events.
 	 * 
 	 * @return an {@link java.util.Iterator} of the pause events
 	 * @deprecated use {@link #getPauseControlEventManager()} and 
 	 * 	{@link de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventManager#getControlEventsList()}
 	 * in conjunction with the for each loop
 	 * @see <a href="http://docs.oracle.com/javase/1.5.0/docs/guide/language/foreach.html">oracle documentation</a>
 	 */
 	public Iterator<PauseEvent> getPauseEventsIterator() {
 		return this.pauseEvents.iterator();
 	}
 	
 	/**
 	 * Returns an {@link java.util.Iterator} of the break events.
 	 * 
 	 * @return an {@link java.util.Iterator} of the break events
 	 * @deprecated use {@link #getBreakControlEventManager()} and 
 	 * 	{@link de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventManager#getControlEventsList()}
 	 * in conjunction with the for each loop
 	 * @see <a href="http://docs.oracle.com/javase/1.5.0/docs/guide/language/foreach.html">oracle documentation</a>
 	 */
 	public Iterator<ControlEvent> getBreakEventsIterator() {
 		return this.breakEvents.iterator();
 	}
 	
 	/**
 	 * Returns an {@link java.util.Iterator} of the redo events.
 	 * 
 	 * @return an {@link java.util.Iterator} of the redo events
 	 * @deprecated use {@link #getRedoControlEventManager()} and 
 	 * 	{@link de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventManager#getControlEventsList()}
 	 * in conjunction with the for each loop
 	 * @see <a href="http://docs.oracle.com/javase/1.5.0/docs/guide/language/foreach.html">oracle documentation</a>
 	 */
 	public Iterator<ControlEvent> getRedoEventsIterator() {
 		return this.redoEvents.iterator();
 	}
 	/**
 	 * Returns an {@link java.util.Iterator} of the trigger events.
 	 * 
 	 * @return an {@link java.util.Iterator} of the trigger events
 	 * @deprecated use {@link #getTriggerControlEventManager()} and 
 	 * 	{@link de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventManager#getControlEventsList()}
 	 * in conjunction with the for each loop
 	 * @see <a href="http://docs.oracle.com/javase/1.5.0/docs/guide/language/foreach.html">oracle documentation</a>
 	 */
 	public Iterator<ControlEvent> getTriggerEventsIterator() {
 		return this.triggerEvents.iterator();
 	}
 
 	/**
 	 * Returns the control event manager of the break events.
 	 * 
 	 * @return the control event manager of the break events
 	 */
 	public ControlEventManager getBreakControlEventManager() {
 		return breakControlEventManager;
 	}
 
 	/**
 	 * Returns the control event manager of the pause events.
 	 * 
 	 * @return the control event manager of the pause events
 	 */
 	public ControlEventManager getPauseControlEventManager() {
 		return pauseControlEventManager;
 	}
 
 	/**
 	 * Returns the control event manager of the redo events.
 	 * 
 	 * @return the control event manager of the redo events
 	 */
 	public ControlEventManager getRedoControlEventManager() {
 		return redoControlEventManager;
 	}
 	
 	/**
 	 * Returns the control event manager of the trigger events.
 	 * 
 	 * @return the control event manager of the trigger events
 	 */
 	public ControlEventManager getTriggerControlEventManager() {
 		return triggerControlEventManager;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void updateEvent(final ModelUpdateEvent modelUpdateEvent) {
 		if(logger.isDebugEnabled()) {
 			if(modelUpdateEvent != null) {
 				logger.debug(modelUpdateEvent.getSender());
 			}
 			logger.debug("null");
 		}
 		updateListeners();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean addModelUpdateListener(
 			final IModelUpdateListener modelUpdateListener) {
 		return this.updateListener.add(modelUpdateListener);
 	}
 
 	/**
 	 * {@inheritDoc} 
 	 */
 	@Override
 	public boolean removeModelUpdateListener(
 			final IModelUpdateListener modelUpdateListener) {
 		return this.updateListener.remove(modelUpdateListener);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public List<IModelError> getModelErrors() {
 		final List<IModelError> errorList = new ArrayList<IModelError>();
 		
 		errorList.addAll(this.pauseControlEventManager.getModelErrors());
 		errorList.addAll(this.breakControlEventManager.getModelErrors());
 		errorList.addAll(this.redoControlEventManager.getModelErrors());
 		errorList.addAll(this.triggerControlEventManager.getModelErrors());
 		
 		for(Axis axis : this.axes) {
 			errorList.addAll(axis.getModelErrors());
 		}
 		for(Channel channel : this.channels) {
 			errorList.addAll(channel.getModelErrors());
 		}
 		for(Prescan prescan : this.prescans) {
 			errorList.addAll(prescan.getModelErrors());
 		}
 		for(Postscan postscan : this.postscans) {
 			errorList.addAll(postscan.getModelErrors());
 		}
 		for(Positioning positioning : this.positionings) {
 			errorList.addAll(positioning.getModelErrors());
 		}
 		for(PlotWindow plotwindow : this.plotWindows) {
 			errorList.addAll(plotwindow.getModelErrors());
 		}
 
 		if(Double.compare(this.triggerDelay, Double.NaN) == 0) {
 			errorList.add(new ScanModuleError(this, 
 					ScanModuleErrorTypes.TRIGGER_DELAY_NOT_POSSIBLE));
 		}
 		if(Double.compare(this.settleTime, Double.NaN) == 0) {
 			errorList.add(new ScanModuleError(this, 
 					ScanModuleErrorTypes.SETTLE_TIME_NOT_POSSIBLE));
 		}
 		return errorList;
 	}
 	
 	/*
 	 * 
 	 */
 	private void updateListeners() {
 		final CopyOnWriteArrayList<IModelUpdateListener> list = 
 			new CopyOnWriteArrayList<IModelUpdateListener>(this.updateListener);
 		
 		for(IModelUpdateListener imul : list) {
 			imul.updateEvent(new ModelUpdateEvent(this, null));
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void propertyChange(PropertyChangeEvent e) {
 		if (e.getSource() instanceof AddMultiplyMode<?> &&
 				e.getPropertyName().equals(AddMultiplyMode.MAIN_AXIS_PROP)) {
 			Axis newAxis = null;
 			if ((Boolean)e.getNewValue()) {
 				newAxis = ((AddMultiplyMode<?>) e.getSource()).getAxis();
 			}
 			if (this.mainAxis != null) {
 				// remove Listener of old main axis
 				this.mainAxis.removePropertyChangeListener(
 						AddMultiplyMode.STEPCOUNT_PROP, this);
 			}
 			this.propertyChangeSupport.firePropertyChange(
 					ScanModule.MAIN_AXIS_PROP, this.mainAxis,
 					this.mainAxis = newAxis);
 			if (this.mainAxis != null) {
 				// register listener to new main axis
 				this.mainAxis.addPropertyChangeListener(
 						AddMultiplyMode.STEPCOUNT_PROP, this);
 			}
 			if (logger.isDebugEnabled()) {
 				logger.debug("Axis " + this.mainAxis.getMotorAxis().getName()
 						+ " has been set as main axis");
 			}
 		} else if (e.getPropertyName().equals(AddMultiplyMode.STEPCOUNT_PROP)) { 
 			for (Axis axis : this.axes) {
 				if (axis.getMode() instanceof AddMultiplyMode<?>) {
 					((AddMultiplyMode<?>) axis.getMode())
 							.matchMainAxis(this.mainAxis);
 				}
 			}
 		} else if (e.getPropertyName().equals("normalizeChannel")) {
 			if(e.getNewValue() != null) {
 				return;
 			}
 			// the plot windows are only informed if the normalize channel 
 			// was deleted
 			for(PlotWindow plotWindow : this.plotWindows) {
 				plotWindow.normalizeChannelChanged((Channel)e.getSource(), 
 						(DetectorChannel)e.getOldValue());
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * 
 	 * @param propertyName the name of the property
 	 * @param listener the {@link java.beans.PropertyChangeListener}
 	 * @see {@link java.beans.PropertyChangeSupport#addPropertyChangeListener(String, PropertyChangeListener)}
 	 */
 	public void addPropertyChangeListener(String propertyName,
 			PropertyChangeListener listener) {
 		this.propertyChangeSupport.addPropertyChangeListener(
 				propertyName, listener);
 	}
 	
 	/**
 	 * 
 	 * 
 	 * @param propertyName the name of the property
 	 * @param listener {@link java.beans.PropertyChangeListener}
 	 * @see {@link java.beans.PropertyChangeSupport#removePropertyChangeListener(String, PropertyChangeListener)}
 	 */
 	public void removePropertyChangeListener(String propertyName,
 			PropertyChangeListener listener) {
 		this.propertyChangeSupport.removePropertyChangeListener(
 				propertyName, listener);
 	}
 	
 	/**
 	 * Adds all available motor axes of the given device definition to the 
 	 * scan module setting each to motion disabled.
 	 * 
 	 * @param measuringStation the device definition containing the axes
 	 * @since 1.9
 	 */
 	public void saveAllAxisPositions(IMeasuringStation measuringStation) {
 		// get available motor axes
 		List<MotorAxis> motorAxes = new ArrayList<MotorAxis>();
 		for(Motor m : measuringStation.getMotors()) {
 			for(MotorAxis ma : m.getAxes()) {
 				motorAxes.add(ma);
 			}
 		}
 		// create axes
 		for(MotorAxis ma : motorAxes) {
 			final Axis axis = new Axis(this);
 			axis.setMotorAxis(ma);
 			axis.setStepfunction(Stepfunctions.PLUGIN);
 			PlugIn motionDisabled = measuringStation
 					.getPluginByName("MotionDisabled");
 			axis.setPluginController(new PluginController(motionDisabled));
 			axis.getPluginController().setPlugin(motionDisabled);
 			this.add(axis);
 		}
 	}
 	
 	/**
 	 * Adds all available detector channels of the given device definition to 
 	 * the scan module setting each to average count 1.
 	 * 
 	 * @param measuringStation the device definition containing the channels
 	 * @since 1.9
 	 */
 	public void saveAllChannelValues(IMeasuringStation measuringStation) {
 		// get available channels
 		List<DetectorChannel> detectorChannels = new ArrayList<DetectorChannel>();
 		for (Detector det : measuringStation.getDetectors()) {
 			for (DetectorChannel ch : det.getChannels()) {
 				detectorChannels.add(ch);
 			}
 		}
 		// creating channels
 		for (DetectorChannel ch : detectorChannels) {
 			Channel channel = new Channel(this);
 			channel.setDetectorChannel(ch);
 			channel.setAverageCount(1);
 			this.add(channel);
 		}
 	}
 	
 	/**
 	 * Removes all axes, channels, prescans, postscans, positiongs and 
 	 * plot windows.
 	 * 
 	 * @since 1.1
 	 */
 	public void removeAll() {
 		this.removeAllAxes();
 		this.removeAllChannels();
 		this.removeAllPrescans();
 		this.removeAllPostscans();
 		this.removeAllPositionings();
 		this.removeAllPlotWindows();
 	}
 	
 	/**
 	 * Removes all axes.
 	 * 
 	 * @since 1.1
 	 */
 	public void removeAllAxes() {
 		for(Axis a : this.getAxes()) {
 			this.remove(a);
 		}
 	}
 	
 	/**
 	 * Removes all channels.
 	 * 
 	 * @since 1.1
 	 */
 	public void removeAllChannels() {
 		for(Channel ch : this.getChannels()) {
 			this.remove(ch);
 		}
 	}
 	
 	/**
 	 * Removes all prescans.
 	 * 
 	 * @since 1.1
 	 */
 	public void removeAllPrescans() {
 		for(Prescan pre : this.getPrescans()) {
 			this.remove(pre);
 		}
 	}
 	
 	/**
 	 * Removes all postscans.
 	 * 
 	 * @since 1.1
 	 */
 	public void removeAllPostscans() {
 		for(Postscan post : this.getPostscans()) {
 			this.remove(post);
 		}
 	}
 	
 	/**
 	 * Removes all positionings.
 	 * 
 	 * @since 1.1
 	 */
 	public void removeAllPositionings() {
 		for(Positioning pos : this.getPositionings()) {
 			this.remove(pos);
 		}
 	}
 	
 	/**
 	 * Removes all plot windows.
 	 * 
 	 * @since 1.1
 	 */
 	public void removeAllPlotWindows() {
 		for(PlotWindow plot : this.getPlotWindows()) {
 			this.remove(plot);
 		}
 	}
 	
 	/**
 	 * Returns the number of devices.
 	 * 
 	 * @return the number of devices
 	 * @since 1.1
 	 */
 	public int getDeviceCount() {
 		int count = 0;
 		
 		count += this.axes.size();
 		count += this.channels.size();
 		count += this.prescans.size();
 		count += this.postscans.size();
 		count += this.positionings.size();
 		count += this.plotWindows.size();
 		
 		return count;
 	}
 	
 	/**
 	 * Returns the number of motor positions.
 	 * 
 	 * @return the number of motor positions or <code>null</code> if 
 	 * 			calculation is not possible
 	 * @author Marcus Michalsky
 	 * @since 1.10
 	 */
 	public Integer getPositionCount() {
 		// a main axis defines the global position count (of a scan module)
 		if (this.getMainAxis() != null) {
 			return this.getMainAxis().getMode().getPositionCount();
 		}
 		List<Double> positionCounts = new ArrayList<Double>();
 		for (Axis axis : this.getAxes()) {
 			// if any axis position count is not available -> abort
 			if (axis.getMode().getPositionCount() == null) {
 				return null;
 			}
 			positionCounts.add(axis.getMode().getPositionCount().doubleValue());
 		}
 		DescriptiveStats stats = new DescriptiveStats(positionCounts);
 		stats.calculateStats();
 		// no main axis, no uncalculatable axes -> return max
 		return stats.getMaximum().intValue();
 	}
 }
