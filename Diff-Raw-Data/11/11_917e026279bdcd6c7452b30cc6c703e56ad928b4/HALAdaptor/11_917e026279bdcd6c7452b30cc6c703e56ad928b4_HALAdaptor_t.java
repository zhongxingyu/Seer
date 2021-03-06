 package org.accada.ale.server.readers.hal;
 
 import org.accada.ale.server.Tag;
 import org.accada.ale.server.readers.BaseReader;
 import org.accada.ale.server.readers.IdentifyThread;
 import org.accada.ale.server.readers.LRSpec;
 import org.accada.ale.wsdl.ale.epcglobal.ImplementationException;
 import org.accada.reader.hal.HardwareAbstraction;
 import org.accada.reader.hal.HardwareException;
 import org.accada.reader.hal.Observation;
 import org.accada.reader.hal.Trigger;
 import org.accada.reader.hal.impl.sim.SimulatorController;
 import org.apache.log4j.Logger;
 
 /**
  * adaptor for all HAL devices.
  * this adaptor allows to attach hal devices directly to the ale
  * @author sawielan
  *
  */
 public class HALAdaptor extends BaseReader {
 	
 	/** logger. */
 	private static final Logger LOG = Logger.getLogger(HALAdaptor.class);
 	
 	/** whenever the hal device does not support auto-polling we need to install a polling thread. */
 	private IdentifyThread identifyThread = null;
 	
 	/** interface to the HAL device. */
 	private HardwareAbstraction hal = null;
 	
 	/** the time intervall in which the reader will look for new tags. */
 	private long pollingFrequency = -1;
 	
 	/** indicates whether the hal needs a pollingThread or not . */
 	private boolean autoPolling = false;
 	
 	/**
 	 * constructor for the HAL adaptor.
 	 */
 	public HALAdaptor() {
 		super();
 	}
 
 	/**
 	 * initializes a HALAdaptor. this method must be called befor the Adaptor can
 	 * be used.
  	 * @param name the name for the reader encapsulated by this adaptor.
 	 * @param spec the specification that describes the current reader.
 	 * @throws ImplementationException whenever an internal error occurs.
 	 */
 	public void initialize(String name, LRSpec spec) throws ImplementationException {
 		super.initialize(name, spec);
 
 		pollingFrequency = Long.parseLong(logicalReaderProperties.get("pollingFrequency"));
 		
 		// create the hal device
 		hal = new SimulatorController(name, logicalReaderProperties.get("propertiesFile"));
 		
 		// now need to determine whether the HAL device supports auto-polling or 
 		// whether we need to install a polling thread
 		if (hal.supportsAsynchronousIdentify()) {
 			setAutoPolling(true);
 		} else {
 			setAutoPolling(false);
 		}
 	}
 
 	/**
 	 * sets up a reader.
 	 * @throws ImplementationException whenever an internal error occured
 	 */
 	@Override
 	public void connectReader() throws ImplementationException {
 		if (!isConnected()) {
 			if (!isAutoPolling()) {
 				// create the polling thread
 				identifyThread = new IdentifyThread(this);
 				identifyThread.setPollingFrequency(pollingFrequency);
 				identifyThread.start();
 				identifyThread.suspendIdentify();
 			}
 			
 			setConnected();
 		}
 	}
 
 	/**
 	 * destroys a reader.
 	 * @throws ImplementationException whenever an internal error occured
 	 */
 	@Override
 	public void disconnectReader() throws ImplementationException {
 		if (isConnected()) {
 			if (isAutoPolling()) {
 				try {
					hal.stopAsynchronousIdentify();		
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			
 			} else {
 				// use the identifyThread
 				identifyThread.stopIdentify();
 			}
 			
 			setDisconnected();
 			setStopped();
 		}
 	}
 
 	/**
 	 * starts a base reader to read tags.
 	 *
 	 */
 	@Override
 	public synchronized  void start() {
 		if (!isConnected()) {
 				try {
 					connectReader();
 				} catch (ImplementationException e) {
 					LOG.info("could not start the reader " + readerName);
 					e.printStackTrace();
 				}
 		}
 		
 		if (!isStarted()) {
 			if (isAutoPolling()) {
 				try {
 					Trigger trigger = null;
 					if (pollingFrequency == 0) {
 						trigger = Trigger.createContinuousTrigger();
 					} else {
 						trigger = Trigger.createTimerTrigger(pollingFrequency);
 					}
					hal.startAsynchronousIdentify(hal.getReadPointNames(), trigger);		
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				
 			} else {
 				// use the identify thread
 				identifyThread.resumeIdentify();
 			}
 			
 			setStarted();
 		}
 	}
 
 	/**
 	 * stops a reader from reading tags.
 	 *
 	 */
 	@Override
 	public synchronized  void stop() {
 		if (isStarted()) {
 			
 			if (isAutoPolling()) {
 				try {
					hal.stopAsynchronousIdentify();
 				} catch (Exception e) {
 					LOG.info("could not stop the reader " + readerName);
 				}
 			 
 			} else {
 				// use the identify Thread
 				identifyThread.suspendIdentify();
 			}
 			
 			setStopped();
 		}
 	}
 
 	/**
 	 * updates a reader according the specified LRSpec.
 	 * @param spec LRSpec for the reader
 	 * @throws ImplementationException whenever an internal error occurs
 	 */
 	@Override
 	public synchronized  void update(LRSpec spec) throws ImplementationException {
 		
 		// we update the properties, so stop the reader from retrieving tags
 		stop();
 		// set the specification
 		setLRSpec(spec);
 		// extract the pollingFrequency
 		pollingFrequency = Long.parseLong(logicalReaderProperties.get("pollingFrequency"));
 		
 		// restart the reader
 		start();
 	}
 	
 	/**
 	 * whenever a new Tag is read a notification is sent to the observers.
 	 * @param tag a tag read on the reader
 	 */
 	@Override
 	public void addTag(Tag tag) {
 		LOG.debug("HALAdaptor: notifying observers");
 		tag.addTrace(getName());
 		
 		setChanged();
 		notifyObservers(tag);
 	}
 
 	/**
 	 * Triggers the identification of all tags that are currently available 
 	 * on the reader. this method is used when the IdentifyThread is used to poll the adaptor.
 	 * @param readPointNames the readers/sources that have to be polled
 	 * @return a set of Observations
 	 * @throws HardwareException whenever an internal hardware error occurs (as reader not available...)
 	 */
 	@Override
 	public Observation[] identify(String[] readPointNames)
 			throws HardwareException {
 		
 		LOG.debug("got observation trigger");
 		Observation[] observations = null;
 		if (countObservers() > 0) {
 			
 			observations = hal.identify(hal.getReadPointNames());
 			for (Observation observation : observations) {
 				
 				// For each tag create a new Tag
 				for (String tagobserved : observation.getIds()) {
 					Tag tag = new Tag(getName());
 					tag.setTagID(tagobserved);
 					tag.setTimestamp(observation.getTimestamp());
 					addTag(tag);
 				}
 			}
 		}
 		return observations;
 	}
 
 	/**
 	 * indicates whether this HAL device needs a polling mechanism or not.
 	 * @return boolean indicating the polling - capabilities
 	 */
 	private boolean isAutoPolling() {
 		return autoPolling;
 	}
 
 	/**
 	 * sets the polling capabilities.
 	 * @param autoPolling boolean indicating if polling is supported by the HAL device
 	 */
 	private void setAutoPolling(boolean autoPolling) {
 		this.autoPolling = autoPolling;
 	}
 }
