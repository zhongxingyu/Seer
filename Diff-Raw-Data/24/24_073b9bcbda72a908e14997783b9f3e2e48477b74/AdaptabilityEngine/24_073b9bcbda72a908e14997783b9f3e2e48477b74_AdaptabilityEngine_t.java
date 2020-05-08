 package org.unbiquitous.uos.core.adaptabitilyEngine;
 
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.logging.Logger;
 
 import org.unbiquitous.uos.core.SecurityManager;
 import org.unbiquitous.uos.core.UOSComponent;
 import org.unbiquitous.uos.core.UOSComponentFactory;
 import org.unbiquitous.uos.core.UOSLogging;
 import org.unbiquitous.uos.core.applicationManager.ApplicationDeployer;
 import org.unbiquitous.uos.core.applicationManager.ApplicationManager;
 import org.unbiquitous.uos.core.applicationManager.UOSMessageContext;
 import org.unbiquitous.uos.core.connectivity.ConnectivityManager;
 import org.unbiquitous.uos.core.deviceManager.DeviceDao;
 import org.unbiquitous.uos.core.deviceManager.DeviceManager;
 import org.unbiquitous.uos.core.driverManager.DriverDao;
 import org.unbiquitous.uos.core.driverManager.DriverDeployer;
 import org.unbiquitous.uos.core.driverManager.DriverManager;
 import org.unbiquitous.uos.core.driverManager.DriverManagerException;
 import org.unbiquitous.uos.core.driverManager.ReflectionServiceCaller;
 import org.unbiquitous.uos.core.messageEngine.MessageEngine;
 import org.unbiquitous.uos.core.messageEngine.MessageEngineException;
 import org.unbiquitous.uos.core.messageEngine.NotifyHandler;
 import org.unbiquitous.uos.core.messageEngine.ServiceCallHandler;
 import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
 import org.unbiquitous.uos.core.messageEngine.dataType.UpNetworkInterface;
 import org.unbiquitous.uos.core.messageEngine.messages.Notify;
 import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
 import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall.ServiceType;
 import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;
 import org.unbiquitous.uos.core.network.connectionManager.ConnectionManagerControlCenter;
 import org.unbiquitous.uos.core.network.loopback.LoopbackDevice;
 import org.unbiquitous.uos.core.network.model.NetworkDevice;
 import org.unbiquitous.uos.core.network.model.connection.ClientConnection;
 import org.unbiquitous.uos.core.ontologyEngine.Ontology;
 
 /**
  * Class responsible for receiving Service Calls from applications and delegating it to the appropriated providers.
  * 
  * @author Fabricio Nogueira Buzeto
  *
  */
 public class AdaptabilityEngine implements ServiceCallHandler,
 											NotifyHandler,
 											UOSComponent{
 	
 	private static Logger logger = UOSLogging.getLogger();
 	
 	protected DriverManager driverManager;
 	protected UpDevice currentDevice;
 	protected ConnectionManagerControlCenter connectionManagerControlCenter;
 	protected MessageEngine messageEngine;
 	protected EventManager eventManager;
 	protected ConnectivityManager connectivityManager;
 	protected ResourceBundle properties;
 	protected ApplicationManager applicationManager;
 
 	private DeviceManager deviceManager;
 
 
 	/**
 	 * Method responsible for creating a call for {@link AdaptabilityEngine#callService(String, ServiceCall)} with the following parameters.
 	 * 
 	 * @param deviceName Device providing the service to be called.
 	 * @param serviceName The name of the service to be called.
 	 * @param driverName The name of the driver which possess the informed service.
 	 * @param instanceId The instance ID of the driver.
 	 * @param parameters The parameters for the service.
 	 * @return Service Response for the called service.
 	 * @throws ServiceCallException
 	 */
 	public ServiceResponse callService(
 									UpDevice device,
 									String serviceName, 
 									String driverName, 
 									String instanceId,
 									String securityType,
 									Map<String,Object> parameters) throws ServiceCallException{
 		ServiceCall serviceCall = new ServiceCall();
 		serviceCall.setDriver(driverName);
 		serviceCall.setInstanceId(instanceId);
 		serviceCall.setService(serviceName);
 		serviceCall.setParameters(parameters);
 		serviceCall.setSecurityType(securityType);
 		
 		return callService(device, serviceCall);
 	}
 	
 	/**
 	 * Method responsible for calling a service according to the ServiceCall informed.
 	 * 
 	 * @param deviceName Device providing the service to be called.
 	 * @param serviceCall Objetc representig the service call to be placed.
 	 * @return Service Response for the called service.
 	 * @throws ServiceCallException
 	 */
 	public ServiceResponse callService(UpDevice device, ServiceCall serviceCall) throws ServiceCallException{
 		if (	serviceCall == null ||
 				serviceCall.getDriver() == null || serviceCall.getDriver().isEmpty() ||
 				serviceCall.getService() == null || serviceCall.getService().isEmpty()){
 			throw new IllegalArgumentException("Service Driver or Service Name is empty");
 		}
 		
 		StreamConnectionThreaded[] streamConnectionThreadeds = null;
 		
 		UOSMessageContext messageContext = new UOSMessageContext();
 		messageContext.setCallerDevice(new LoopbackDevice(1)); // FIXME: Tales - 21/07/2012 
 																// Linha de codigo necessária para que o objeto 'messageContext' tenha um 'callerDevice'. 
 																// Caso prossiga sem o mesmo uma 'NullpointerException' é lançada.
 		
 		// In case of a Stream Service, a Stream Channel must be opened
 		if(serviceCall.getServiceType().equals(ServiceType.STREAM)){
 			streamConnectionThreadeds = openStreamChannel(device, serviceCall, messageContext);
 		}
 		
 		/* Verify Device Name or the main device object itself
 		 * If the device corresponds to the current device instance, make a local service call
 		 */
 		if (isLocalCall(device)){
 			return localServiceCall(serviceCall, streamConnectionThreadeds, messageContext);
 		}else{
 			return remoteServiceCall(device, serviceCall,streamConnectionThreadeds, messageContext);
 		}
 	}
 
 	private boolean isLocalCall(UpDevice device) {
 		return device == null || device.getName() == null ||
 				device.getName().equalsIgnoreCase(currentDevice.getName());
 	}
 
 	private ServiceResponse remoteServiceCall(UpDevice device,
 			ServiceCall serviceCall,
 			StreamConnectionThreaded[] streamConnectionThreadeds,
 			UOSMessageContext messageContext) throws ServiceCallException {
 		// If not a local service call, delegate to the serviceHandler
 		try{
 			ServiceResponse response = messageEngine.callService(device, serviceCall); // FIXME: Response can be null
 			response.setMessageContext(messageContext);
 			return response;
 		}catch (MessageEngineException e){
 			closeStreamChannels(streamConnectionThreadeds);
 			throw new ServiceCallException(e);
 		}
 	}
 
 	private ServiceResponse localServiceCall(ServiceCall serviceCall,
 			StreamConnectionThreaded[] streamConnectionThreadeds,
 			UOSMessageContext messageContext) throws ServiceCallException {
 		logger.info("Handling Local ServiceCall");
 		
 		try {
 			// in the case of a local service call, must inform that the current device is the same.
 			//FIXME : AdaptabilityEngine : Must set the local device  
 			//messageContext.setCallerDevice(callerDevice)
 			ServiceResponse response = handleServiceCall(serviceCall, messageContext);
 			response.setMessageContext(messageContext);
 			
 			return response;
 		} catch (DriverManagerException e) {
 			// if there was an opened stream channel, it must be closed
 			closeStreamChannels(streamConnectionThreadeds);
 			throw new ServiceCallException(e);
 		}
 	}
 
 	/**
 	 * Method responsible for closing opened Stream Channels
 	 * 
 	 * @param streamConnectionThreadeds Array with the opened streams to be properly closed
 	 */
 	private void closeStreamChannels(
 			StreamConnectionThreaded[] streamConnectionThreadeds) {
 		if (streamConnectionThreadeds != null){
 			for (int i = 0; i < streamConnectionThreadeds.length; i++) {
 				streamConnectionThreadeds[i].interrupt();
 			}
 		}
 	}
 
 	/**
 	 * Method responsible for opening the Stream Channels, if needed
 	 * 
 	 * @param device The called device
 	 * @param serviceCall The ServiceCall message
 	 * @return An array of StreamConnectionThreaded objects with the opened streams
 	 * @throws ServiceCallException
 	 */
 	private StreamConnectionThreaded[] openStreamChannel(UpDevice device,
 			ServiceCall serviceCall, UOSMessageContext messageContext)
 			throws ServiceCallException {
 		StreamConnectionThreaded[] streamConnectionThreadeds = null;
 		
 		try{
 			
 			//Channel type decision
 			String netType = null;
 			if(serviceCall.getChannelType() != null){
 				netType = serviceCall.getChannelType();
 			}else{
 				UpNetworkInterface network = this.connectivityManager.getAppropriateInterface(device, serviceCall);
 				netType = network.getNetType();
 			}
 			
 			int channels = serviceCall.getChannels();
 			streamConnectionThreadeds = new StreamConnectionThreaded[channels];
 			String[] channelIDs = new String[channels];
 			
 			for (int i = 0; i < channels; i++) {
 				NetworkDevice networkDevice = connectionManagerControlCenter.getAvailableNetworkDevice(netType);
 				channelIDs[i] = connectionManagerControlCenter.getChannelID(networkDevice.getNetworkDeviceName());
 				StreamConnectionThreaded streamConnectionThreaded = new StreamConnectionThreaded(messageContext, networkDevice);
 				streamConnectionThreaded.start();
 				streamConnectionThreadeds[i] = streamConnectionThreaded;
 			}
 			
 			serviceCall.setChannelIDs(channelIDs);
 			serviceCall.setChannelType(netType);
 			
 		}catch (Exception e) {
 			throw new ServiceCallException(e);
 		}
 		return streamConnectionThreadeds;
 	}
 	
 	
 	/**
 	 * Inner class for waiting a connection in case of stream service type.
 	 */
 	private class StreamConnectionThreaded extends Thread{
 		private UOSMessageContext msgContext;
 		private NetworkDevice networkDevice;
 		
 		public StreamConnectionThreaded(UOSMessageContext msgContext, NetworkDevice networkDevice){
 			this.msgContext = msgContext;
 			this.networkDevice = networkDevice;
 		}
 		
 		public void run(){
 			try {
 				ClientConnection con = connectionManagerControlCenter.openPassiveConnection(networkDevice.getNetworkDeviceName(), networkDevice.getNetworkDeviceType());
 				msgContext.addDataStreams(con.getDataInputStream(), con.getDataOutputStream());
 			} catch (Exception e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 	
 	/**
 	 * Sends a notify message to the device informed.
 	 * 
 	 * @param notify Notify message to be sent.
 	 * @param device Device which is going to receive the notofy event
 	 * @throws MessageEngineException
 	 */
 	public void sendEventNotify(Notify notify, UpDevice device) throws NotifyException{
 		eventManager.sendEventNotify(notify,device);
 	}
 	
 	/**
 	 * Register a Listener for a event, driver and device specified.
 	 * 
 	 * @param listener UosEventListener responsible for dealing with the event.
 	 * @param device Device which event must be listened
 	 * @param driver Driver responsible for the event.
 	 * @param eventKey EventKey that identifies the wanted event to be listened.
 	 * @throws NotifyException In case of an error.
 	 */
 	public void registerForEvent(UosEventListener listener, UpDevice device, String driver, String eventKey) throws NotifyException{
 		eventManager.registerForEvent(listener, device, driver, null, eventKey);
 	}
 	
 	/**
 	 * Register a Listener for a event, driver and device specified.
 	 * 
 	 * @param listener UosEventListener responsible for dealing with the event.
 	 * @param device Device which event must be listened
 	 * @param driver Driver responsible for the event.
 	 * @param instanceId Instance Identifier of the driver to be registered upon.
 	 * @param eventKey EventKey that identifies the wanted event to be listened.
 	 * @throws NotifyException In case of an error.
 	 */
 	public void registerForEvent(UosEventListener listener, UpDevice device, String driver, String instanceId, String eventKey) throws NotifyException{
 		eventManager.registerForEvent(listener, device, driver, instanceId, eventKey);
 	}
 	
 	/**
 	 * Removes a listener for receiving Notify events and notifies the event driver of its removal.
 	 * 
 	 * @param listener Listener to be removed.
 	 * @throws NotifyException
 	 */
 	public void unregisterForEvent(UosEventListener listener) throws NotifyException{
 		eventManager.unregisterForEvent(listener, null, null, null, null);
 	}
 	
 	/**
 	 * Removes a listener for receiving Notify events and notifies the event driver of its removal.
 	 * 
 	 * @param listener Listener to be removed.
 	 * @param driver Driver from which the listener must be removed (If not informed all drivers will be considered).
 	 * @param instanceId InstanceId from the Driver which the listener must be removed (If not informed all instances will be considered).
 	 * @param eventKey EventKey from which the listener must be removed (If not informed all events will be considered).
 	 * @throws NotifyException
 	 */
 	public void unregisterForEvent(UosEventListener listener, UpDevice device, String driver, String instanceId, String eventKey) throws NotifyException{
 		eventManager.unregisterForEvent(listener, device, driver, instanceId, eventKey);
 	}
 	
 	/**
 	 * @see NotifyHandler#handleNofify(Notify)
 	 */
 	public void handleNofify(Notify notify, UpDevice device) throws DriverManagerException {
 		eventManager.handleNofify(notify, device);
 	}
 	
 	/**
 	 * ServiceCallHandler#handleServiceCall(ServiceCall)
 	 */
 	@Override
 	public ServiceResponse handleServiceCall(ServiceCall serviceCall, UOSMessageContext messageContext)
 			throws DriverManagerException {
 		if (isApplicationCall(serviceCall)){
 			return applicationManager.handleServiceCall(serviceCall, messageContext);
 		}else{
 			return driverManager.handleServiceCall(serviceCall, messageContext);
 		}
 	}
 
 	private boolean isApplicationCall(ServiceCall serviceCall) {
 		return serviceCall.getDriver() != null && serviceCall.getDriver().equals("app");
 	}
 	
 	
 	/************************ USO Compoment ***************************/
 	
 	@Override
 	public void create(ResourceBundle properties) {
 		this.properties = properties;
 	}
 	@Override
 	public void init(UOSComponentFactory factory) {
 		SmartSpaceGateway gateway = factory.gateway(new SmartSpaceGateway());
 		currentDevice = factory.currentDevice();
 		
 		this.connectionManagerControlCenter = factory.get(ConnectionManagerControlCenter.class);
 		
 		DriverDao driverDao = factory.get(DriverDao.class);
 		DeviceDao deviceDao = factory.get(DeviceDao.class);
 		
 		this.driverManager = new DriverManager(	currentDevice, 
 						driverDao, 
 						deviceDao, 
 						new ReflectionServiceCaller(connectionManagerControlCenter));
 		
 		// Deploy service-drivers
 		DriverDeployer driverDeployer = new DriverDeployer(driverManager,properties);
 		driverDeployer.deployDrivers();
 		
 		this.messageEngine = factory.get(MessageEngine.class);
 		this.eventManager = new EventManager(messageEngine);
 		this.connectivityManager = factory.get(ConnectivityManager.class);
 		
 		deviceManager = new DeviceManager(
 				currentDevice, 
 				deviceDao,  
 				driverDao, 
 				connectionManagerControlCenter, 
 				factory.get(ConnectivityManager.class), 
 				gateway, driverManager);
 		
 		connectionManagerControlCenter.radarControlCenter().setListener(deviceManager);
 		this.messageEngine.setDeviceManager(deviceManager);
 		
 		applicationManager = new ApplicationManager(properties,gateway);
 		ApplicationDeployer applicationDeployer = new ApplicationDeployer(properties,applicationManager);
		
		initGateway(factory, gateway, applicationDeployer);
		
 		applicationDeployer.deployApplications();
 		applicationManager.startApplications();
 		
 		driverManager.initDrivers(gateway);
 		
		
 		
 	}
 
 	private void initGateway(UOSComponentFactory factory,
 			SmartSpaceGateway gateway, ApplicationDeployer applicationDeployer){
 		try {
 			Ontology ontology = null;
 			if (properties.containsKey("ubiquitos.ontology.path")){ //TODO: hack because the way Ontology is initialized
 				ontology = new Ontology(properties);
 			}
 			gateway
 			.init(	this, currentDevice, 
 					factory.get(SecurityManager.class),
 					factory.get(ConnectivityManager.class),
 					deviceManager, 
 					driverManager, 
 					applicationDeployer, 
 					ontology);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	@Override
 	public void start() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void stop() {
 		try {
 			driverManager.tearDown();
 			applicationManager.tearDown();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	public DeviceManager deviceManager(){
 		return deviceManager;
 	}
 	
 	public DriverManager driverManager(){
 		return driverManager;
 	}
 	
 	public ApplicationManager applicationManager(){
 		return applicationManager;
 	}
 }
