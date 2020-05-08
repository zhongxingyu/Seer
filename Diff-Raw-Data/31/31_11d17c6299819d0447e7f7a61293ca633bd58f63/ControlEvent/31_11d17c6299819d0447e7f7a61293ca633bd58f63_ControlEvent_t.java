 package de.ptb.epics.eve.data.scandescription;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import de.ptb.epics.eve.data.EventTypes;
 import de.ptb.epics.eve.data.measuringstation.Event;
 import de.ptb.epics.eve.data.scandescription.errors.ControlEventError;
 import de.ptb.epics.eve.data.scandescription.errors.ControlEventErrorTypes;
 import de.ptb.epics.eve.data.scandescription.errors.IModelError;
 import de.ptb.epics.eve.data.scandescription.errors.IModelErrorProvider;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventMessage;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventMessageEnum;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateListener;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateProvider;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ModelUpdateEvent;
 
 /**
  * A ControlEvent is used to encapsulate a normal event and specify on which
  * conditions it should be activated. 
  * 
  * @author Stephan Rehfeld <stephan.rehfeld( -at -) ptb.de>
  * @author Marcus Michalsky
  */
 public class ControlEvent implements IModelUpdateListener, IModelUpdateProvider, 
 														IModelErrorProvider {
 
 	// the event
 	private Event event;
 	
 	// the conditions
 	private Limit limit;
 	
 	// the event type
 	protected EventTypes eventType;
 	
 	// the event id
 	private String eventId;
 	
 	/** A list of model update listener. */
 	protected List<IModelUpdateListener> modelUpdateListener;
 	
 	/**
 	 * Constructs a <code>ControlEvent</code>.
 	 *
 	 * @param type the type of the control event
 	 */
 	public ControlEvent(final EventTypes type) {
 		this.eventType = type;
 		this.modelUpdateListener = new ArrayList<IModelUpdateListener>();
 		this.limit = new Limit();
 		this.limit.addModelUpdateListener(this);
 		if((type != EventTypes.MONITOR) && (type != EventTypes.DETECTOR)) {
 			event = new Event(type); 
 		}
 	}
 	
 	/**
 	 * Better Constructor.
 	 * 
 	 * @param type see {@link de.ptb.epics.eve.data.EventTypes}
 	 * @param event the event
 	 * @param id the id
 	 */
 	public ControlEvent(final EventTypes type, final Event event, final String id) {
 		this(type);
 		this.setEvent(event);
 		this.setId(id);
 
 		switch (this.eventType) {
 			case MONITOR:
 				this.limit.setType(event.getMonitor().getDataType().getType());
 				break;
 		}
 	}
 	
 	/**
 	 * Returns the type of the control event.
 	 * 
 	 * @return the type of the control event
 	 */
 	public EventTypes getEventType() {
 		return eventType;
 	}
 
 	/**
 	 * Returns the event that is encapsulated by this ControlEvent.
 	 * 
 	 * @return the encapsulated event
 	 */
 	public Event getEvent() {
 		return event;
 	}
 
 	/**
 	 * Sets the event that should be controlled by this ControlEvent.
 	 * 
 	 * @param event the event that should be controlled by this ControlEvent.
 	 */
 	public void setEvent(final Event event) {
 		if (event != null) {
 			this.event = event;
 			this.limit.setValue(this.limit.getValue());
 			updateListeners();
 		}
 	}
 
 	/**
 	 * Gives back the Limit object of this ControlEvent. All manipulations
 	 * should be done with this object.
 	 * 
 	 * @return The Limit object. Never returns null.
 	 */
 	public Limit getLimit() {
 		return limit;
 	}
 	
 	/**
 	 * This method return the id of the control event.
 	 * 
 	 * @return Returns the id of the control event.
 	 */
 	public String getDeviceId () {
		return eventId;
 	}
 
 	/**
 	 * This method return the id of the device from the control event.
 	 * 
 	 * @return Returns the id of the device.
 	 */
 	public String getId () {
 		return eventId;
 	}
 	
 	/**
 	 * This method sets the id of this control event.
 	 * 
 	 * @param id The new id of this control event.
 	 */
 	public void setId(final String id) {
 		eventId = id;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void updateEvent(final ModelUpdateEvent modelUpdateEvent) {
 		if(modelUpdateEvent.getSender() == this.limit) {
 			updateListeners();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean addModelUpdateListener(
 			final IModelUpdateListener modelUpdateListener) {
 		return this.modelUpdateListener.add(modelUpdateListener);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean removeModelUpdateListener(
 			final IModelUpdateListener modelUpdateListener) {
 		return this.modelUpdateListener.remove(modelUpdateListener);
 	}
 
 	/**
 	 *{@inheritDoc}
 	 */
 	@Override
 	public List<IModelError> getModelErrors() {
 		final List<IModelError> errorList = new ArrayList<IModelError>();
 		
 		if(this.event.getType() == EventTypes.MONITOR) {
 			// limit 
 			if (this.limit.getValue() == null || this.limit.getValue().isEmpty()) {
 				errorList.add(new ControlEventError(
 					this, ControlEventErrorTypes.MONITOR_LIMIT_NOT_SET));
 			}
 			else if(this.event.getMonitor().getAccess() != null && 
 					!this.event.getMonitor().getAccess().
 					isValuePossible(this.limit.getValue())) {
 				errorList.add(new ControlEventError(
 					this, ControlEventErrorTypes.VALUE_NOT_POSSIBLE));
 			}
 		}
 		return errorList;
 	}
 
 	/*
 	 * 
 	 */
 	private void updateListeners() {
 		final CopyOnWriteArrayList<IModelUpdateListener> list = 
 			new CopyOnWriteArrayList<IModelUpdateListener>(this.modelUpdateListener);
 		
 		for(IModelUpdateListener imul : list) {
 			imul.updateEvent(new ModelUpdateEvent(this,
 					new ControlEventMessage(this,
 							ControlEventMessageEnum.UPDATED)));
 		}
 	}
 }
