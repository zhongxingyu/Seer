 package org.sociotech.communitymashup.interfaceprovider.factory.impl;
 
 import java.util.HashMap;
import java.util.HashSet;
 import java.util.Map;
import java.util.Set;
 
 import org.eclipse.emf.ecore.EClass;
 import org.osgi.service.log.LogService;
 import org.sociotech.communitymashup.application.ApplicationPackage;
 import org.sociotech.communitymashup.application.FEEDInterface;
 import org.sociotech.communitymashup.application.Interface;
 import org.sociotech.communitymashup.application.RESTInterface;
 import org.sociotech.communitymashup.data.DataSet;
 import org.sociotech.communitymashup.interfaceprovider.facade.InterfaceServiceFacade;
 import org.sociotech.communitymashup.interfaceprovider.factory.callback.InterfaceProducedCallback;
 import org.sociotech.communitymashup.interfaceprovider.factory.facade.InterfaceFactoryFacade;
 import org.sociotech.communitymashup.interfaceprovider.instantiation.facade.InterfaceInstantiationServiceFacade;
 import org.sociotech.communitymashup.util.servicetracker.LogServiceTracker;
 import org.sociotech.communitymashup.util.servicetracker.callback.LogServiceTracked;
 
 /**
  * @author Peter Lachenmaier
  *
  * The interface factory produces interfaces. It looks for a interface Instantiation Service and uses this
  * Service to create instances based on a given configuration.
  */
 public class InterfaceFactoryImpl implements InterfaceFactoryFacade, LogServiceTracked {
 
 	// TODO create map to support future interfaces
 	/**
 	 * Local reference to an feed interface instantiation service. This one will be used to produce interfaces.
 	 * Make sure to maintain the reference by using {@link #addInterfaceInstantiationService(InterfaceInstantiationServiceFacade)}
 	 * and {@link #removeInterfaceInstantiationService(InterfaceInstantiationServiceFacade)}
 	 */
 	private InterfaceInstantiationServiceFacade feedInstantiationService = null;
 
 	/**
 	 * Local reference to an rest interface instantiation service. This one will be used to produce interfaces.
 	 * Make sure to maintain the reference by using {@link #addInterfaceInstantiationService(InterfaceInstantiationServiceFacade)}
 	 * and {@link #removeInterfaceInstantiationService(InterfaceInstantiationServiceFacade)}
 	 */
 	private InterfaceInstantiationServiceFacade restInstantiationService = null;
 
 	/**
 	 * This map contains interfaces and their data sets that are currently not instantiated due to missing instantiation service.
 	 */
 	private Map<Interface, DataSet> openInterfaces;
 
 	/**
	 * This set contains interfaces that are currently in production
	 */
	private Set<Interface> interfacesInProduction;

	/**
 	 * This map contains all callbacks that are needed after successful production.
 	 */
 	private Map<Interface, InterfaceProducedCallback> callbacks;
 	
 	/**
 	 * Constructs a new interface factory.
 	 */
 	public InterfaceFactoryImpl()
 	{
 		// create empty map
 		openInterfaces = new HashMap<Interface, DataSet>();
 		
		// create empty set for interfaces in production
		interfacesInProduction = new HashSet<Interface>();
		
 		// create empty callback map
 		callbacks = new HashMap<Interface, InterfaceProducedCallback>();
 		
 		// open log service tracker
 		openLogServiceTracker();
 	}
 
 	/**
 	 * Sets a reference to a mashup instantiation service which will be used for the creation of mashups.
 	 * 
 	 * @param interfaceInstantiationService 
 	 */
 	public void addInterfaceInstantiationService(InterfaceInstantiationServiceFacade interfaceInstantiationService)
 	{
 		if(interfaceInstantiationService == null)
 		{
 			return;
 		}
 
 		// get the interface the service is capable to instantiate
 		EClass instantiatableClass = interfaceInstantiationService.getInstantiatableInterfaceClass();
 
 		if(instantiatableClass == ApplicationPackage.eINSTANCE.getFEEDInterface())
 		{
 			// remove previously used one
 			removeInterfaceInstantiationService(feedInstantiationService);
 			feedInstantiationService = interfaceInstantiationService;
 
 			log("Got feed interface instantiation service.", LogService.LOG_DEBUG);
 		}
 
 		if(instantiatableClass == ApplicationPackage.eINSTANCE.getRESTInterface())
 		{
 			// remove previously used one
 			removeInterfaceInstantiationService(restInstantiationService);
 			restInstantiationService = interfaceInstantiationService;
 
 			log("Got rest interface instantiation service.", LogService.LOG_DEBUG);
 		}
 
 		if(openInterfaces.isEmpty())
 		{
 			// nothing to do
 			return;
 		}
 
 		// There are interfaces waiting for production
 
 		// temporary keep all open interfaces
 		Map<Interface, DataSet> tmpMap = new HashMap<Interface, DataSet>(openInterfaces);
 
 		// clear the list
 		openInterfaces.clear();
 
 		for(Interface configuration : tmpMap.keySet())
 		{
 			// produce every single interface
 			// will be re-added to openInterfaces list if it can not be produced
 			produceInterface(configuration, tmpMap.get(configuration));
 		}
 	}
 
 	/**
 	 * Removes the local references to the given interface instantiation service.
 	 * 
 	 * @param instantiationService References to this instantiation service will be removed 
 	 */
 	public void removeInterfaceInstantiationService(InterfaceInstantiationServiceFacade instantiationService)
 	{
 		if(instantiationService == null)
 		{
 			// nothing to do
 			return;
 		}
 
 		if(feedInstantiationService == instantiationService)
 		{
 			// add cleanup code here
 			feedInstantiationService = null;
 			log("Lost feed interface instantiation service", LogService.LOG_DEBUG);
 		}
 
 		if(restInstantiationService == instantiationService)
 		{
 			// add cleanup code here
 			restInstantiationService = null;
 			log("Lost rest interface instantiation service", LogService.LOG_DEBUG);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.interfaceprovider.factory.facade.InterfaceFactoryFacade#produceInterface(org.sociotech.communitymashup.application.Interface, org.sociotech.communitymashup.data.DataSet)
 	 */
 	@Override
 	public InterfaceServiceFacade produceInterface(Interface interfaceConfiguration, DataSet dataSet)
 	{
 		if(interfaceConfiguration == null)
 		{
 			// illegal configuration
 			return null;
 		}
 
 		InterfaceServiceFacade producedInterface = null;
 		
 		if(interfaceConfiguration instanceof FEEDInterface)
 		{
 			producedInterface = produceFeedInterface((FEEDInterface) interfaceConfiguration, dataSet);
 		}
 
 		if(interfaceConfiguration instanceof RESTInterface)
 		{
 			producedInterface = produceRESTInterface((RESTInterface) interfaceConfiguration, dataSet);
 		}
 		
 		if(producedInterface != null)
 		{
 			if(callbacks.containsKey(interfaceConfiguration))
 			{
 				// remove call back
 				InterfaceProducedCallback callback = callbacks.remove(interfaceConfiguration);
 				// call back
 				callback.interfaceProduced(interfaceConfiguration, producedInterface);
 			}
 			return producedInterface;
 		}
 
 		log("No implementation for producing interface " + interfaceConfiguration, LogService.LOG_ERROR);
 
 		return null;
 	}
 
 
 	private InterfaceServiceFacade produceRESTInterface(RESTInterface interfaceConfiguration, DataSet dataSet)
 	{
 		if(restInstantiationService == null)
 		{
 			// no instantiation service available, so keep configuration for later creation
 			openInterfaces.put(interfaceConfiguration, dataSet);
 
 			log("Keeping interface " + interfaceConfiguration + " for later production.", LogService.LOG_DEBUG);
 
 			return null;
 		}
 
		// TODO this is a workaround, maybe remove later
		// check to do not produce twice when interface is already in production
		// happens when a interface changes during the production and therefore gets destroyed and recreated
		if(interfacesInProduction.contains(interfaceConfiguration)) {
			return null;
		}
		
		// indicate that the interface is in production
		interfacesInProduction.add(interfaceConfiguration);
		
 		InterfaceServiceFacade newInterfaceService = restInstantiationService.instantiate(interfaceConfiguration, dataSet);
 
		// indicate finished production
		interfacesInProduction.remove(interfaceConfiguration);
		
 		log("Produced rest interface.", LogService.LOG_DEBUG);
 
 		// remove it from open interfaces
 		openInterfaces.remove(interfaceConfiguration);
 
 		// and return it
 		return newInterfaceService;
 	}
 
 
 	private InterfaceServiceFacade produceFeedInterface(FEEDInterface interfaceConfiguration, DataSet dataSet)
 	{
 		if(feedInstantiationService == null)
 		{
 			// no instantiation service available, so keep configuration for later creation
 			openInterfaces.put(interfaceConfiguration, dataSet);
 
 			log("Keeping interface " + interfaceConfiguration + " for later production.", LogService.LOG_DEBUG);
 
 			return null;
 		}
 
 		InterfaceServiceFacade newInterfaceService = feedInstantiationService.instantiate(interfaceConfiguration, dataSet);
 
 		// remove it from open interfaces
 		openInterfaces.remove(interfaceConfiguration);
 
 		log("Produced feed interface.", LogService.LOG_DEBUG);
 
 		// and return it
 		return newInterfaceService;
 	}
 
 	/**
 	 * Tracker for log services.
 	 */
 	private LogServiceTracker logServiceTracker = null;
 
 	/**
 	 * The used log service
 	 */
 	private LogService logService = null;
 
 	/**
 	 * Logs a message with the given log level using the OSGi log service.
 	 * 
 	 * @param message
 	 *            Message to log
 	 * @param logLevel
 	 *            log level: {@link LogService#LOG_DEBUG},
 	 *            {@link LogService#LOG_ERROR}, {@link LogService#LOG_INFO} or
 	 *            {@link LogService#LOG_WARNING}
 	 */
 	public void log(String message, int logLevel) {
 
 		if (logService != null)
 		{
 			logService.log(logLevel, message);
 		} 
 		else
 		{
 			System.out.println(message);
 		}
 	}	
 
 	/**
 	 * Opens a tracker to get noticed on appearing or disappearing log services
 	 */
 	private void openLogServiceTracker() {
 
 		// create new service tracker and keep reference
 		this.logServiceTracker  = new LogServiceTracker(InterfaceFactoryBundleActivator.getContext(), this);
 
 		// open it
 		this.logServiceTracker.open();		
 	}
 
 	/**
 	 * Uses the given log service for logging.
 	 * 
 	 * @param logService Log service to use for logging.
 	 */
 	@Override
 	public void gotLogService(LogService logService) {
 		
 		this.logService = logService;
 		// log first message with new log service
 		log("Set new log service.", LogService.LOG_DEBUG);
 	}
 
 	/**
 	 * @param logService
 	 */
 	@Override
 	public void lostLogService(LogService logService) {
 
 		if(logService != null && logService == this.logService)
 		{
 			log("Lost log service.", LogService.LOG_WARNING);
 			// set to null if it is the used log service
 			this.logService = null;
 		}
 	}
 	
 	/**
 	 * Stops the interface factory
 	 */
 	public void stop()
 	{
 		// stop log service tracker
 		if(logServiceTracker != null)
 		{
 			logServiceTracker.close();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.interfaceprovider.factory.facade.InterfaceFactoryFacade#produceInterfaceAndCallback(org.sociotech.communitymashup.application.Interface, org.sociotech.communitymashup.data.DataSet, org.sociotech.communitymashup.interfaceprovider.factory.callback.InterfaceProducedCallback)
 	 */
 	@Override
 	public InterfaceServiceFacade produceInterfaceAndCallback(Interface interfaceConfiguration, DataSet dataSet, InterfaceProducedCallback callback) {
 		if(interfaceConfiguration == null)
 		{
 			return null;
 		}
 		
 		InterfaceServiceFacade producedInterface = produceInterface(interfaceConfiguration, dataSet);
 		if(producedInterface != null)
 		{
 			// directly produced -> so return
 			return producedInterface;
 		}
 		
 		if(callback != null)
 		{
 			// put callback in map
 			callbacks.put(interfaceConfiguration, callback);
 		}
 		
 		return null;
 	}
 }
