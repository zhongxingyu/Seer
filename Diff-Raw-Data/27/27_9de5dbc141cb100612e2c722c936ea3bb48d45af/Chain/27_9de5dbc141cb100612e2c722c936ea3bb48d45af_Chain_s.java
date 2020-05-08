 package de.ptb.epics.eve.data.scandescription;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventManager;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventMessage;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventMessageEnum;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventTypes;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateListener;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateProvider;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ModelUpdateEvent;
 import de.ptb.epics.eve.data.scandescription.errors.ChainError;
 import de.ptb.epics.eve.data.scandescription.errors.ChainErrorTypes;
 import de.ptb.epics.eve.data.scandescription.errors.IModelError;
 import de.ptb.epics.eve.data.scandescription.errors.IModelErrorProvider;
 
 /**
  * This class describes a chain in a scan description.
  * 
  * @author Stephan Rehfeld <stephan.rehfeld( -at -) ptb.de>
  * @author Marcus Michalsky
  */
 public class Chain implements IModelUpdateProvider, IModelUpdateListener, IModelErrorProvider {
 	
 	/**
 	 * The unique id of the chain.
 	 */
 	private int id;
 	
 	/**
 	 * The location and the filename, where the results should be saved.
 	 */
 	private String saveFilename;
 	
 	/**
 	 * A boolean field that indicates if the save should be manually confirmed by the user.
 	 */
 	private boolean confirmSave;
 	
 	/**
 	 * A boolean field that indicates if the datafile name should be extended by an automatic incremented number.
 	 */
 	private boolean autoNumber;
 	
 	/**
 	 * The PluginController for the Save Plugin.
 	 */
 	private final PluginController savePlugInController;
 	
 	
 	/**
 	 * A list of the ControlEvents, that holds the configuration for the redo events.
 	 */
 	private List<ControlEvent> redoEvents;
 	
 	/**
 	 * A list of  the ControlEvents, that holds the configuration for the break events.
 	 */
 	private List<ControlEvent> breakEvents;
 	
 	/**
 	 * A list of  the PauseEvents, that holds the configuration for the pause events.
 	 */
 	private List<PauseEvent> pauseEvents;
 	
 	/**
 	 * A list of  the ControlEvents, that holds the configuration for the break events.
 	 */
 	private List<ControlEvent> stopEvents;
 	
 	// TODO Caution, we have a list of ControlEvents called startEvents and 
 	// one StartEvent called startEvent, tidy up this
 	// For now just the first ControlEvent from startEvents is used as the event
 	// part for startEvent
 	/**
 	 * A list of  the ControlEvents, that holds the configuration for the break events.
 	 */
 	private List<ControlEvent> startEvents;
 	
 	/**
 	 * A reference to the event, that will start the chain.
 	 */
 	private StartEvent startEvent;
 	
 	/**
 	 * A list that holds all the scan moduls inside of the chain.
 	 */
 	private List<ScanModule> scanModuls;
 	
 	/**
 	 * A refrence to the scan description, that is the parent of this chain.
 	 */
 	private ScanDescription scanDescription;
 	
 	/**
 	 * A map that is mapping the id of a scan modul to the scan modul it self.
 	 */
 	private Map< Integer, ScanModule > scanModulsMap;
 	
 	/**
 	 * A List that is holding all object that needs to get an update message if this object was updated.
 	 */
 	private List<IModelUpdateListener> updateListener;
 	
 	/**
 	 * The control event manager for the break events.
 	 */
 	private ControlEventManager breakControlEventManager;
 	
 	/**
 	 * The control event manager for the start events. 
 	 */
 	private ControlEventManager startControlEventManager;
 	
 	/**
 	 * The control event manager for the stop events.
 	 */
 	private ControlEventManager stopControlEventManager;
 	
 	/**
 	 * The control event manager for the redo events.
 	 */
 	private ControlEventManager redoControlEventManager;
 	
 	/**
 	 * The control event manager for the pause events.
 	 */
 	private ControlEventManager pauseControlEventManager;
 	
 	/**
 	 * The comment for this chain.
 	 */
 	private String comment;
 	
 	/**
 	 * This attribute indicates if the scan description should be saved in the result file.
 	 */
 	private boolean saveScanDescription;
 	
 	/**
 	 * This constructor constructs a new Chain with a given id.
 	 * 
 	 * @param id The id of the chain. It must be a positive Integer, so at least 1.
 	 */
 	public Chain( final int id ) {
 		super();
 		if( id < 1 ) {
 			throw new IllegalArgumentException( "The parameter 'id' must be at least 1!" );
 		}
 		this.id = id;
 		this.scanModuls = new ArrayList<ScanModule>();
 		this.scanModulsMap = new HashMap< Integer, ScanModule >();
 		this.redoEvents = new ArrayList<ControlEvent>();
 		this.breakEvents = new ArrayList<ControlEvent>();
 		this.startEvents = new ArrayList<ControlEvent>();
 		this.stopEvents = new ArrayList<ControlEvent>();
 		this.pauseEvents = new ArrayList<PauseEvent>();
 		this.saveFilename = "";
 		this.updateListener = new ArrayList<IModelUpdateListener>();
 		
 		this.savePlugInController = new PluginController();
 		this.savePlugInController.addModelUpdateListener( this );
 		// TODO make start event a regular event
 		this.startEvent = null;
 		this.breakControlEventManager = new ControlEventManager( this, this.breakEvents, ControlEventTypes.CONTROL_EVENT );
 		this.startControlEventManager = new ControlEventManager( this, this.startEvents, ControlEventTypes.CONTROL_EVENT );
 		this.stopControlEventManager = new ControlEventManager( this, this.stopEvents, ControlEventTypes.CONTROL_EVENT );
 		this.redoControlEventManager = new ControlEventManager( this, this.redoEvents, ControlEventTypes.CONTROL_EVENT );
 		this.pauseControlEventManager = new ControlEventManager( this, this.pauseEvents, ControlEventTypes.PAUSE_EVENT );
 		
 		this.breakControlEventManager.addModelUpdateListener( this );
 		this.startControlEventManager.addModelUpdateListener( this );
 		this.stopControlEventManager.addModelUpdateListener( this );
 		this.redoControlEventManager.addModelUpdateListener( this );
 		this.pauseControlEventManager.addModelUpdateListener( this );
 			
 		this.comment = "";
 		this.saveScanDescription = false;
 		
 	}
 	
 	/**
 	 * Gives back the location and the filename where the results should be saved.
 	 * 
 	 * @return The location and the filename where the results should be saved. Never returns null.
 	 */
 	public String getSaveFilename() {
 		return saveFilename;
 	}
 
 	/**
 	 * Sets the location and the filename where the results should be save.
 	 *
 	 * @param saveFilename The location and the filename where the results should be saved. Must not be null!
 	 */
 	public void setSaveFilename( final String saveFilename ) {
 		if( saveFilename == null ) {
 			throw new IllegalArgumentException( "The parameter 'saveFilename' must not be null!" );
 		}
 		this.saveFilename = saveFilename;
 		updateListeners();
 		this.checkFileNameConstraints();
 	}
 
 	/**
 	 * Gives back a copy of the internal list, that is holding all scan modules.
 	 * 
 	 * @return A copy of the internal list, that is holding all scan modules. Never returns null.
 	 */
 	public List< ScanModule > getScanModuls() {
 		return new ArrayList< ScanModule >( this.scanModuls );
 	}
 	
 	/**
 	 * Adds a Scan Modul to the chain.
 	 * 
 	 * @param scanModul The Scan Modul that should be added to the chain. Must not be null!
 	 */
 	public void add( final ScanModule scanModul ) {
 		if( scanModul == null ) {
 			throw new IllegalArgumentException( "The parameter 'scanModul' must not be null!" );
 		}
 		this.scanModulsMap.put( scanModul.getId(), scanModul );
 		this.scanModuls.add( scanModul );
 		scanModul.setChain( this );
 		scanModul.addModelUpdateListener( this );
 		updateListeners();
 	}
 	
 	/**
 	 * Removes a Scan Modul from the chain.
 	 * 
 	 * @param scanModul The scan modul that should be removed from the chain. Must not be null!
 	 */
 	public void remove( final ScanModule scanModul ) {
 		if( scanModul == null ) {
 			throw new IllegalArgumentException( "The parameter 'scanModul' must not be null!" );
 		}
 		this.scanModuls.remove( scanModul );
 		this.scanModulsMap.remove( scanModul.getId() );
 		scanModul.removeModelUpdateListener( this );
 		scanModul.setChain( null );
 		updateListeners();
 	}
 	
 	/**
 	 * Gives back the ControlEvent for the break event.
 	 * 
 	 * @return The ControlEvent for the breakevent.
 	 */
 /***
 	public List<ControlEvent> getBreakEvents() {
 		return new ArrayList<ControlEvent>( this.breakEvents );
 	}
 ***/
 	
 	/**
 	 * Gives back if the saveing of the results have to be confirmed manually.
 	 * 
 	 * @return True or false.
 	 */
 	public boolean isConfirmSave() {
 		return confirmSave;
 	}
 	
 	/**
 	 * Sets if the saving of results have to be confirmed manually.
 	 * 
 	 * @param confirmSave True or false.
 	 */
 	public void setConfirmSave( final boolean confirmSave ) {
 		this.confirmSave = confirmSave;
 		updateListeners();
 	}
 	
 	/**
 	 * 
 	 * if enabled the datafile name will be extended by an automatic incremented number.
 	 * 
 	 * @return true if autonumbering is enabled 
 	 */
 	public boolean isAutoNumber() {
 		return autoNumber;
 	}
 	
	public void setAutoNumber( final boolean autoNumber ) {
 		this.autoNumber = autoNumber;
 		updateListeners();
 	}
 	
 	/**
 	 * Gives back the id of the Chain.
 	 * 
 	 * @return The id of the chain, that will be at least 1.
 	 */
 	public int getId() {
 		return this.id;
 	}
 	
 	/**
 	 * Sets the id of the chain.
 	 * 
 	 * @param id The id of the chain. Have to be at least 1.
 	 */
 	public void setId( final int id ) {
 		if( id < 1 ) {
 			throw new IllegalArgumentException( "The parameter 'id' must be at least 1!" );
 		}
 		this.id = id;
 		updateListeners();
 	}
 	
 
 	/**
 	 * Gives back the PluginController of the object, where you can set the PlugIn an the parameters.
 	 * 
 	 * @return Gives back the PluginController of this object. Never returns null.
 	 */
 	public PluginController getSavePluginController() {
 		return this.savePlugInController;
 	}
 	
 	/**
 	 * Gives back the start event. The start event starts the chain.
 	 * 
 	 * @return Gives back the StartEvent of the chain.
 	 */
 	public StartEvent getStartEvent() {
 		return this.startEvent;
 	}
 	
 	/**
 	 *	Sets the StartEvent that will start the Chain.
 	 * 
 	 * @param startEvent The Event that will start the Chain.
 	 */
 	public void setStartEvent( final StartEvent startEvent ) {
 		this.startEvent = startEvent;
 		updateListeners();
 	}
 	
 	/**
 	 * Gives back the correpondenting ScanModul for a id.
 	 * 
 	 * @param id The id of the Scan Modul that should be given back.
 	 * @return The ScanModul or 'null' if it's not found.
 	 */
 	public ScanModule getScanModulById( final int id ) {
 		return this.scanModulsMap.get( id );
 	}
 	
 	/**
 	 * Sets the scan description where this chain is in.
 	 * 
 	 * @param scanDescription The scan description where this chain is in.
 	 */
 	protected void setScanDescription( final ScanDescription scanDescription ) {
 		this.scanDescription = scanDescription;
 		this.checkAllConstraints();
 		updateListeners();
 	}
 	
 	/**
 	 * Returns the scan description where the Chain is in.
 	 * 
 	 * @return The scan description where the Chain is in.
 	 */
 	public ScanDescription getScanDescription() {
 		return this.scanDescription;
 	}
 	
 	/**
 	 * Adds a pause event to the chain.
 	 * 
 	 * @param pauseEvent The pause event that should be added to the chain.
 	 * @return Gives back 'true' if the event has been added and false if not.
 	 */
 	public boolean addPauseEvent( final PauseEvent pauseEvent ) {
 		if( this.pauseEvents.add( pauseEvent ) ) {
 			updateListeners();
 			pauseEvent.addModelUpdateListener( this.pauseControlEventManager );
 			this.pauseControlEventManager.updateEvent( new ModelUpdateEvent( this, new ControlEventMessage( pauseEvent, ControlEventMessageEnum.ADDED ) ) );
 			return true;
 		} 
 		return false;
 	}
 	
 	/**
 	 * Removes a pause event from the chain.
 	 * 
 	 * @param pauseEvent The pause event that should be removed from the chain.
 	 * @return Gives back 'true' if the event has been removed and false if not.
 	 */
 	public boolean removePauseEvent( final PauseEvent pauseEvent ) {
 		if( this.pauseEvents.remove( pauseEvent ) ) {
 			updateListeners();
 			pauseEvent.removeModelUpdateListener( this.pauseControlEventManager );
 			this.pauseControlEventManager.updateEvent( new ModelUpdateEvent( this, new ControlEventMessage( pauseEvent, ControlEventMessageEnum.REMOVED ) ) );
 			return true;
 		} 
 		return false;
 	}
 	
 	/**
 	 * Adds a break event to the chain.
 	 * 
 	 * @param breakEvent The break event that should be added to the chain.
 	 * @return Gives back 'true' if the event has been added and false if not.
 	 */
 	public boolean addBreakEvent( final ControlEvent breakEvent ) {
 		if( this.breakEvents.add( breakEvent ) ) {
 			updateListeners();
 			breakEvent.addModelUpdateListener( this.breakControlEventManager );
 			this.breakControlEventManager.updateEvent( new ModelUpdateEvent( this, new ControlEventMessage( breakEvent, ControlEventMessageEnum.ADDED ) ) );
 			return true;
 		} 
 		return false;
 	}
 	
 	/**
 	 * Removes a break event from the chain.
 	 * 
 	 * @param breakEvent The break event that should be removed from the chain.
 	 * @return Gives back 'true' if the event has been removed and false if not.
 	 */
 	public boolean removeBreakEvent( final ControlEvent breakEvent ) {
 		if( this.breakEvents.remove( breakEvent ) ) {
 			updateListeners();
 			breakEvent.removeModelUpdateListener( this.breakControlEventManager );
 			this.breakControlEventManager.updateEvent( new ModelUpdateEvent( this, new ControlEventMessage( breakEvent, ControlEventMessageEnum.REMOVED ) ) );
 			return true;
 		} 
 		return false;
 	}
 	
 	/**
 	 * Adds a start event to the chain.
 	 * 
 	 * @param startEvent The start event that should be added to the chain.
 	 * @return Gives back 'true' if the event has been added and false if not.
 	 */
 	public boolean addStartEvent( final ControlEvent startEvent ) {
 		if( this.startEvents.add( startEvent ) ) {
 			updateListeners();
 			startEvent.addModelUpdateListener( this.startControlEventManager );
 			this.startControlEventManager.updateEvent( new ModelUpdateEvent( this, new ControlEventMessage( startEvent, ControlEventMessageEnum.ADDED ) ) );
 			return true;
 		} 
 		return false;
 	}
 	
 	/**
 	 * Removes a start event from the chain.
 	 * 
 	 * @param startEvent The start event that should be removed from the chain.
 	 * @return Gives back 'true' if the event has been removed and false if not.
 	 */
 	public boolean removeStartEvent( final ControlEvent startEvent ) {
 		if( this.startEvents.remove( startEvent ) ) {
 			updateListeners();
 			startEvent.removeModelUpdateListener( this.startControlEventManager );
 			this.startControlEventManager.updateEvent( new ModelUpdateEvent( this, new ControlEventMessage( startEvent, ControlEventMessageEnum.REMOVED ) ) );
 			return true;
 		} 
 		return false;
 	}
 
 	/**
 	 * Adds a stop event to the chain.
 	 * 
 	 * @param stopEvent The stop event that should be added to the chain.
 	 * @return Gives back 'true' if the event has been added and false if not.
 	 */
 	public boolean addStopEvent( final ControlEvent stopEvent ) {
 		if( this.stopEvents.add( stopEvent ) ) {
 			updateListeners();
 			stopEvent.addModelUpdateListener( this.stopControlEventManager );
 			this.stopControlEventManager.updateEvent( new ModelUpdateEvent( this, new ControlEventMessage( stopEvent, ControlEventMessageEnum.ADDED ) ) );
 			return true;
 		} 
 		return false;
 	}
 	
 	/**
 	 * Removes a stop event from the chain.
 	 * 
 	 * @param stopEvent The stop event that should be removed from the chain.
 	 * @return Gives back 'true' if the event has been removed and false if not.
 	 */
 	public boolean removeStopEvent( final ControlEvent stopEvent ) {
 		if( this.stopEvents.remove( stopEvent ) ) {
 			updateListeners();
 			stopEvent.removeModelUpdateListener( this.stopControlEventManager );
 			this.stopControlEventManager.updateEvent( new ModelUpdateEvent( this, new ControlEventMessage( stopEvent, ControlEventMessageEnum.REMOVED ) ) );
 			return true;
 		} 
 		return false;
 	}
 
 	/**
 	 * Adds a redo event to the chain.
 	 * 
 	 * @param redoEvent The redo event that should be added to the chain.
 	 * @return Gives back 'true' if the event has been added and false if not.
 	 */
 	public boolean addRedoEvent( final ControlEvent redoEvent ) {
 		if( this.redoEvents.add( redoEvent ) ) {
 			updateListeners();
 			this.redoControlEventManager.updateEvent( new ModelUpdateEvent( this, new ControlEventMessage( redoEvent, ControlEventMessageEnum.ADDED ) ) );
 			redoEvent.addModelUpdateListener( this.redoControlEventManager );
 			return true;
 		} 
 		return false;
 	}
 	
 	/**
 	 * Removes a redo event from the chain.
 	 * 
 	 * @param redoEvent The redo event that should be removed from the chain.
 	 * @return Gives back 'true' if the event has been removed and false if not.
 	 */
 	public boolean removeRedoEvent( final ControlEvent redoEvent ) {
 		if( this.redoEvents.remove( redoEvent ) ) {
 			updateListeners();
 			this.redoControlEventManager.updateEvent( new ModelUpdateEvent( this, new ControlEventMessage( redoEvent, ControlEventMessageEnum.REMOVED ) ) );
 			redoEvent.removeModelUpdateListener( this.redoControlEventManager );
 			return true;
 		} 
 		return false;
 	}
 	
 	/**
 	 * Gives back if the pause event is one of the pause events of this chain.
 	 * 
 	 * @param pauseEvent The pause Event that should be checked.
 	 * @return Gives back 'true' if the event is one of the pause events.
 	 */
 	public boolean isPauseEventOfTheChain( final PauseEvent pauseEvent ) {
 		return this.pauseEvents.contains( pauseEvent );
 	}
 	
 	/**
 	 * Gives back if the control event is one of the redo events of this chain.
 	 * 
 	 * @param redoEvent The control Event that should be checked.
 	 * @return Gives back 'true' if the event is one of the redo events.
 	 */
 	public boolean isRedoEventOfTheChain( final ControlEvent redoEvent ) {
 		return this.redoEvents.contains( redoEvent );
 	}
 	
 	/**
 	 * Gives back if the control event is one of the break events of this chain.
 	 * 
 	 * @param breakEvent The control Event that should be checked.
 	 * @return Gives back 'true' if the event is one of the redo events.
 	 */
 	public boolean isBreakEventOfTheChain( final ControlEvent breakEvent ) {
 		return this.breakEvents.contains( breakEvent );
 	}
 	
 	/**
 	 * This methods checks if the given control event is a pause event, redo event or break event of this chain.
 	 * 
 	 * @param controlEvent The control Event that should be checked.
 	 * @return Gives back 'true' if the event is one of the event types of this chain.
 	 */
 	public boolean isAEventOfTheChain( final ControlEvent controlEvent ) {
 		return ( controlEvent instanceof PauseEvent && this.isPauseEventOfTheChain( (PauseEvent)controlEvent ) ) || this.isBreakEventOfTheChain( controlEvent ) || this.isRedoEventOfTheChain( controlEvent );
 	}
 	
 	/**
 	 * This method returns an iterator over all pause events.
 	 * 
 	 * @return An iterator over all pause events. Never returns 'null'.
 	 */
 	public Iterator< PauseEvent > getPauseEventsIterator() {
 		return this.pauseEvents.iterator();
 	}
 	
 	/**
 	 * This method returns an iterator over all start events.
 	 * 
 	 * @return An iterator over all start events. Never returns 'null'.
 	 */
 	public Iterator<ControlEvent> getStartEventsIterator() {
 		return this.startEvents.iterator();
 	}
 
 	/**
 	 * This method returns an iterator over all stop events.
 	 * 
 	 * @return An iterator over all stop events. Never returns 'null'.
 	 */
 	public Iterator<ControlEvent> getStopEventsIterator() {
 		return this.stopEvents.iterator();
 	}
 	
 	/**
 	 * This method returns an iterator over all break events.
 	 * 
 	 * @return An iterator over all break events. Never returns 'null'.
 	 */
 	public Iterator<ControlEvent> getBreakEventsIterator() {
 		return this.breakEvents.iterator();
 	}
 	
 	/**
 	 * This method returns an iterator over all redo events.
 	 * 
 	 * @return An iterator over all redo events. Never returns 'null'.
 	 */
 	public Iterator<ControlEvent> getRedoEventsIterator() {
 		return this.redoEvents.iterator();
 	}
 
 	/**
 	 * This method returns the control event manager of the break events.
 	 * 
 	 * @return The control event manager of the break events. Never returns null.
 	 */
 	public ControlEventManager getBreakControlEventManager() {
 		return breakControlEventManager;
 	}
 
 	/**
 	 * This method returns the control event manager of the start events.
 	 * 
 	 * @return The control event manager of the start events. Never returns null.
 	 */
 	public ControlEventManager getStartControlEventManager() {
 		return startControlEventManager;
 	}
 
 	/**
 	 * This method returns the control event manager of the stop events.
 	 * 
 	 * @return The control event manager of the stop events. Never returns null.
 	 */
 	public ControlEventManager getStopControlEventManager() {
 		return stopControlEventManager;
 	}
 
 	/**
 	 * This method returns the control event manager of the stop events.
 	 * 
 	 * @return The control event manager of the stop events. Never returns null.
 	 */
 	public ControlEventManager getPauseControlEventManager() {
 		return pauseControlEventManager;
 	}
 
 	/**
 	 * This method returns the control event manager of the redo events.
 	 * 
 	 * @return The control event manager of the redo events. Never returns null.
 	 */
 	public ControlEventManager getRedoControlEventManager() {
 		return redoControlEventManager;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateProvider#addModelUpdateListener(de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateListener)
 	 */
 	public boolean addModelUpdateListener( final IModelUpdateListener modelUpdateListener ) {
 		return this.updateListener.add( modelUpdateListener );
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateProvider#removeModelUpdateListener(de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateListener)
 	 */
 	public boolean removeModelUpdateListener( final IModelUpdateListener modelUpdateListener ) {
 		return this.updateListener.remove( modelUpdateListener );
 	}
 	
 	/**
 	 * By calling this methods all constraints of the objects are checked. Model errors are produced
 	 * if the object does not fit the constrains.
 	 */
 	private void checkAllConstraints() {
 		this.checkFileNameConstraints();
 	}
 	
 	/**
 	 * This method checks the constraints of the filename.
 	 */
 	private void checkFileNameConstraints() {
 		
 	}
 
 	/**
 	 * This method returns the comment.
 	 * 
 	 * @return The comment of the Chain. Never returns 'null'.
 	 */
 	public String getComment() {
 		return this.comment;
 	}
 
 	/**
 	 * This method sets the comment of the Chain.
 	 * 
 	 * @param comment The new comment for the Chain. Must not be 'null'.
 	 */
 	public void setComment( final String comment ) {
 		if( comment == null ) {
 			throw new IllegalArgumentException( "The parameter 'comment' must not be null!" );
 		}
 		this.comment = comment;
 		updateListeners();
 	}
 
 	/**
 	 * This method returns if the scan description should be saved in the results file.
 	 * 
 	 * @return Returns 'true' if the scan description should be saved in the scan description.
 	 */
 	public boolean isSaveScanDescription() {
 		return saveScanDescription;
 	}
 
 	/**
 	 * This method sets if the scan description should be saved in the results file.
 	 * 
 	 * @param saveScanDescription Pass 'true' if the scan description should be saved in the results file.
 	 */
 	public void setSaveScanDescription( final boolean saveScanDescription ) {
 		this.saveScanDescription = saveScanDescription;
 		updateListeners();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void updateEvent(final ModelUpdateEvent modelUpdateEvent) {
 		updateListeners();		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see de.ptb.epics.eve.data.scandescription.errors.IModelErrorProvider#getModelErrors()
 	 */
 	@Override
 	public List< IModelError > getModelErrors() {
 		final List< IModelError > errorList = new ArrayList< IModelError >();
 		if( this.saveFilename.equals( "" ) ) {
 			errorList.add( new ChainError( this, ChainErrorTypes.FILENAME_EMPTY ) );
 		}
 		errorList.addAll( this.savePlugInController.getModelErrors() );
 		errorList.addAll( this.pauseControlEventManager.getModelErrors() );
 		errorList.addAll( this.breakControlEventManager.getModelErrors() );
 		errorList.addAll( this.redoControlEventManager.getModelErrors() );
 		errorList.addAll( this.stopControlEventManager.getModelErrors() );
 		errorList.addAll( this.startControlEventManager.getModelErrors() );
 		final Iterator< ScanModule > it = this.scanModuls.iterator();
 		while( it.hasNext() ) {
 			errorList.addAll( it.next().getModelErrors() );
 		}
 		return errorList;
 	}
 	
 	private void updateListeners()
 	{
 		final CopyOnWriteArrayList<IModelUpdateListener> list = 
 			new CopyOnWriteArrayList<IModelUpdateListener>(this.updateListener);
 		
 		Iterator<IModelUpdateListener> it = list.iterator();
 		
 		while(it.hasNext()) {
 			it.next().updateEvent(new ModelUpdateEvent(this, null));
 		}
 	}
 }
