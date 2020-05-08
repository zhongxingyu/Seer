 package de.ptb.epics.eve.data.scandescription;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import de.ptb.epics.eve.data.measuringstation.DetectorChannel;
 import de.ptb.epics.eve.data.measuringstation.Event;
 import de.ptb.epics.eve.data.scandescription.errors.ChannelError;
 import de.ptb.epics.eve.data.scandescription.errors.ChannelErrorTypes;
 import de.ptb.epics.eve.data.scandescription.errors.IModelError;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventManager;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventMessage;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventMessageEnum;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventTypes;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateListener;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ModelUpdateEvent;
 
 /**
  * This class describes the behavior of a detector during the main phase.
  * 
  * @author Stephan Rehfeld <stephan.rehfeld( -at -) ptb.de>
  * @author Hartmut Scherr
  * @author Marcus Michalsky
  */
 public class Channel extends AbstractMainPhaseBehavior {
 
 	/*
 	 * This attribute controls how often the detector should be read to make
 	 * an average value. default value is 1.
 	 * If input is invalid, set 0
 	 */
 	private int averageCount = 1;
 	
 	/*
 	 * The parent scan module of this channel.
 	 */
 	private ScanModule parentScanModule;
 	
 	/*
 	 * The max deviation of this channel.
 	 * Double.NaN = value is invalid
 	 * Double.NEGATIVE_INFINITY = value is empty
 	 */
 	private double maxDeviation = Double.NEGATIVE_INFINITY;
 	
 	/*
 	 * The minimum for this channel.
 	 * Double.NaN = value is invalid
 	 * Double.NEGATIVE_INFINITY = value is empty
 	 */
 	private double minimum = Double.NEGATIVE_INFINITY;
 	
 	/*
 	 * The maximum attempts for reading the channel.
 	 * -1 = value is invalid
 	 * Integer.MIN_VALUE = value is empty
 	 */
 	private int maxAttempts = Integer.MIN_VALUE;
 
 	/*
 	 * A flag if a trigger of this detector must be confirmed.
 	 */
 	private boolean confirmTrigger = false;
 	
 	/*
 	 * A flag for repeat on redo.
 	 */
 	private boolean repeatOnRedo = false;
 	
 	/*
 	 * The detector ready event.
 	 */
 	private Event detectorReadyEvent;
 
 	/*
 	 * A list of the ControlEvents, that holds the configuration for the redo events.
 	 */
 	private List< ControlEvent > redoEvents;
 	
 	/*
 	 * This control event manager controls the redo events.
 	 */
 	private ControlEventManager redoControlEventManager;
 	
 	/**
 	 * Constructs a <code>Channel</code>.
 	 * 
 	 * @param parentScanModule The parent scan module.
 	 * @throws IllegalArgumentException if the parameter is <code>null</code>.
 	 */
 	public Channel(final ScanModule parentScanModule) {
 		if(parentScanModule == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'parentScanModul' must not be null!");
 
 		}
 		this.parentScanModule = parentScanModule;
 
 		this.redoEvents = new ArrayList<ControlEvent>();
 		this.redoControlEventManager = new ControlEventManager(
 				this, this.redoEvents, ControlEventTypes.CONTROL_EVENT);
 		this.redoControlEventManager.addModelUpdateListener(this);
 	}
 
 	/**
 	 * Adds a redo event. 
 	 * 
 	 * @param redoEvent the redo event that should be added
 	 * @return <code>true</code> if the event has been added, 
 	 * 			<code>false</code> otherwise
 	 */
 	public boolean addRedoEvent(final ControlEvent redoEvent) {
 		if(this.redoEvents.add(redoEvent)) {
 			updateListeners();
 			redoEvent.addModelUpdateListener(this.redoControlEventManager);
 			this.redoControlEventManager.updateEvent(new ModelUpdateEvent(this, 
 				new ControlEventMessage(redoEvent, ControlEventMessageEnum.ADDED)));
 			return true;
 		} 
 		return false;
 	}
 
 	/**
 	 * Removes a redo event.
 	 * 
 	 * @param redoEvent the redo event that should be removed
 	 * @return <code>true</code> if the event has been removed, 
 	 * 			<code>false</code> otherwise
 	 */
 	public boolean removeRedoEvent(final ControlEvent redoEvent) {
 		if( this.redoEvents.remove(redoEvent)) {
 			updateListeners();
 			this.redoControlEventManager.updateEvent(new ModelUpdateEvent(this, 
 				new ControlEventMessage(redoEvent, ControlEventMessageEnum.REMOVED)));
 			redoEvent.removeModelUpdateListener(this.redoControlEventManager);
 			return true;
 		} 
 		return false;
 	}
 
 	/**
 	 * Returns an iterator of the redo events.
 	 * 
 	 * @return an iterator of the redo events
 	 */
 	public Iterator<ControlEvent> getRedoEventsIterator() {
 		return this.redoEvents.iterator();
 	}
 	
 	/**
 	 * Returns how often the detector should be read to make an average
 	 * result for the measuring.
 	 * 
 	 * @return the average count
 	 */
 	public int getAverageCount() {
 		return this.averageCount;
 	}
 	
 	/**
 	 * Sets how often the detector should be read to make an average result
 	 * for the measuring.
 	 * 
 	 * @param averageCount How often the detector should be read.
 	 * @throws IllegalArgumentException if <code>averageCount</code> 
 	 * 		   is less than zero
 	 */
 	public void setAverageCount(final int averageCount) {
 		if(averageCount < 0) {
 			throw new IllegalArgumentException(
 					"The average must be larger than 0.");
 		}
 		this.averageCount = averageCount;
 		updateListeners();
 	}
 
 	/**
 	 * Returns the parent scan module of the channel.
 	 * 
 	 * @return the parent scan module
 	 */
 	public ScanModule getParentScanModul() {
 		return this.parentScanModule;
 	}
 
 	/**
 	 * Checks whether a trigger of the detector must be confirmed.
 	 * 
 	 * @return <code>true</code> if a trigger of the detector must be confirmed, 
 	 * 			<code>false</code> otherwise
 	 */
 	public boolean isConfirmTrigger() {
 		return confirmTrigger;
 	}
 
 	/**
 	 * This method sets if a trigger of this channel must be confirmed.
 	 * 
 	 * @param confirmTrigger Pass 'true' if a trigger of this channel must be confirmed and 'false' if not.
 	 */
 	public void setConfirmTrigger(final boolean confirmTrigger) {
 		this.confirmTrigger = confirmTrigger;
 		updateListeners();
 	}
 
 	/**
 	 * This method returns the maximum attempts to read the detector.
 	 * 
 	 * @return The maximum attempts to read the detector.
 	 */
 	public int getMaxAttempts() {
 		return maxAttempts;
 	}
 
 	/**
 	 * This method sets the maximum attempts to read the detector.
 	 * 
 	 * @param maxAttempts The maximum attempts to read the detector.
 	 */
 	public void setMaxAttempts( final int maxAttempts ) {
 		this.maxAttempts = maxAttempts;
 		updateListeners();
 	}
 
 	/**
 	 * This method returns the maximum deviation between the taken values.
 	 *  
 	 * @return The maximum deviation.
 	 */
 	public double getMaxDeviation() {
 		return this.maxDeviation;
 	}
 
 	/**
 	 * This method sets the maximum deviation.
 	 * 
 	 * @param maxDeviation The new maximimum deviation.
 	 */
 	public void setMaxDeviation( final double maxDeviation ) {
 		this.maxDeviation = maxDeviation;
 		updateListeners();
 	}
 
 	/**
 	 * This method returns the minimum.
 	 * 
 	 * @return The minimum for this channel.
 	 */
 	public double getMinumum() {
 		return this.minimum;
 	}
 
 	/**
 	 * This method sets the minimum for this channel.
 	 * 
 	 * @param minumum The new minimum for this channel.
 	 */
 	public void setMinumum(final double minumum) {
 		this.minimum = minumum;
 		updateListeners();
 	}
 
 	/**
 	 * This method returns if the channel should repeat on redo.
 	 * 
 	 * @return Returns 'true' if the channel repeats reading on a redo event.
 	 */
 	public boolean isRepeatOnRedo() {
 		return repeatOnRedo;
 	}
 
 	/**
 	 * This methods sets if the read of the detector should be repeated on a redo event.
 	 * 
 	 * @param repeatOnRedo Pass 'true' if the detector read should be repeated on a redo event.
 	 */
 	public void setRepeatOnRedo(final boolean repeatOnRedo) {
 		this.repeatOnRedo = repeatOnRedo;
 		updateListeners();
 	}
 	
 	/**
 	 * This method returns the detector ready event.
 	 * 
 	 * @return The detector ready event.
 	 */
 	public Event getDetectorReadyEvent() {
 		return detectorReadyEvent;
 	}
 	
 	/**
 	 * 
 	 * @return true if channel sends an ready event
 	 */
 	public Boolean hasReadyEvent() {
 		if (detectorReadyEvent != null){
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * 
 	 * @param detectorReadyEvent add a detectorReadyEvent
 	 */
 	public void setDetectorReadyEvent(final Event detectorReadyEvent) {
 		this.detectorReadyEvent = detectorReadyEvent;
 		updateListeners();
 	}
 
 	/**
 	 * This method returns 
 	 * 
 	 * @return
 	 */
 	public ControlEventManager getRedoControlEventManager() {
 		return redoControlEventManager;
 	}
 	
 	/**
 	 * Sets the detector channel that will be controlled by this behavior.
 	 * 
 	 * @param detectorChannel The detector channel that will be controlles by this behavior.
 	 */
 	public void setDetectorChannel(final DetectorChannel detectorChannel) {
 		this.abstractDevice = detectorChannel;
 		updateListeners();
 	}
 	
 	/**
 	 * Gives back the detector channel that is controlled by this behavior.
 	 * 
 	 * @return The detector channel that is controlled by this behavior.
 	 */
 	public DetectorChannel getDetectorChannel() {
 		return (DetectorChannel)this.abstractDevice;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public List<IModelError> getModelErrors() {
 		final List<IModelError> modelErrors = new ArrayList<IModelError>();
		// TODO von Hartmut: maxDeviation und minimum liefern keine ChannelErrors mehr!
		// wieso gibt es dann invalid Zustände und Fehlertypen dafür ?
		
 		if(Double.compare(this.maxDeviation, Double.NaN) == 0) {
 			modelErrors.add(new ChannelError(this, ChannelErrorTypes.MAX_DEVIATION_NOT_POSSIBLE));
		} else if(!this.getDetectorChannel().getRead().isValuePossible(Double.toString(maxDeviation))) {
			modelErrors.add(new ChannelError(this, ChannelErrorTypes.MAX_DEVIATION_NOT_POSSIBLE));
 		}
 
 		if( Double.compare(this.minimum, Double.NaN) == 0 ) {
 			modelErrors.add(new ChannelError(this, ChannelErrorTypes.MINIMUM_NOT_POSSIBLE));
		} else if(!this.getDetectorChannel().getRead().isValuePossible(Double.toString(minimum))) {
			modelErrors.add(new ChannelError(this, ChannelErrorTypes.MINIMUM_NOT_POSSIBLE));
		}

		if( this.maxAttempts == -1.0) {
			modelErrors.add(new ChannelError(
					this, ChannelErrorTypes.MAX_ATTEMPTS_NOT_POSSIBLE));
 		}
 
 		if(this.redoControlEventManager.getModelErrors().size() > 0) {
 			modelErrors.addAll(this.redoControlEventManager.getModelErrors());
 			modelErrors.add(new ChannelError(this, ChannelErrorTypes.PLUGIN_ERROR));
 		}
 		return modelErrors;
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
