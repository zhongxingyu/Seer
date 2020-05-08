 package eu.play_project.dcep.distributedetalis;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import jpl.PrologException;
 
 import org.objectweb.proactive.Body;
 import org.objectweb.proactive.Service;
 import org.objectweb.proactive.core.component.body.ComponentEndActive;
 import org.objectweb.proactive.core.component.body.ComponentInitActive;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.bridge.SLF4JBridgeHandler;
 
 import com.jtalis.core.JtalisContextImpl;
 
 import eu.play_project.dcep.api.DcepManagementException;
 import eu.play_project.dcep.api.DcepManagmentApi;
 import eu.play_project.dcep.api.DcepMonitoringApi;
 import eu.play_project.dcep.api.measurement.MeasurementConfig;
 import eu.play_project.dcep.api.measurement.NodeMeasurementResult;
 import eu.play_project.dcep.distributedetalis.api.ConfigApi;
 import eu.play_project.dcep.distributedetalis.api.Configuration;
 import eu.play_project.dcep.distributedetalis.api.DEtalisConfigApi;
 import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
 import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
 import eu.play_project.dcep.distributedetalis.api.EcConnectionManager;
 import eu.play_project.dcep.distributedetalis.api.EcConnectionmanagerException;
 import eu.play_project.dcep.distributedetalis.api.SimplePublishApi;
 import eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit;
 import eu.play_project.play_platformservices.api.BdplQuery;
 import fr.inria.eventcloud.api.CompoundEvent;
 
 /**
  * Distributed Etalis component. This component is a standalone event processing
  * agent which can be instantiated multiple times to create a distributed
  * network.
  * 
  * @author Stefan Obermeier
  * @author Roland Stühmer
  */
 public class DistributedEtalis implements DcepMonitoringApi, DcepManagmentApi,
 		DistributedEtalisTestApi, ComponentInitActive, ComponentEndActive,
 		ConfigApi, DEtalisConfigApi, Serializable {
 
 	private static final long serialVersionUID = 100L;
 	private String name;
 	private JtalisContextImpl etalis; // ETALIS Object
 	private JtalisOutputProvider eventOutputProvider;
 	private JtalisInputProvider eventInputProvider;
 	private final Logger logger = LoggerFactory.getLogger(DistributedEtalis.class);
 	private Map<String, BdplQuery> registeredQueries = Collections.synchronizedMap(new HashMap<String, BdplQuery>());
 	private EcConnectionManager ecConnectionManager;
 	private MeasurementUnit measurementUnit;
 	private PrologSemWebLib semWebLib;
 	private boolean init = false;
 	private final Set<SimplePublishApi> eventSinks = Collections
 			.synchronizedSet(new HashSet<SimplePublishApi>());
 
 	Service service;
 
 	{
 		/*
 		 * Set up logging for jtalis (using JUL -> slf4j)
 		 */
 		
 		// Optionally remove existing handlers attached to j.u.l root logger
 		SLF4JBridgeHandler.removeHandlersForRootLogger();
 		
 		// add SLF4JBridgeHandler to j.u.l's root logger, should be done once
 		// during the initialization phase of your application
 		SLF4JBridgeHandler.install();
 	}
 	
 	// Only for ProActive
 	public DistributedEtalis() {
 	}
 
 	// Only for local use (testing)
 	public DistributedEtalis(String name) {
 		this.name = name;
 		this.initComponentActivity(null);
 	}
 
 	@Override
 	public void initComponentActivity(Body body) {
 		logger.info("Initialising {} component.", this.getClass().getSimpleName());
 	}
 
 	@Override
 	public void endComponentActivity(Body arg0) {
 		logger.info("Terminating {} component.", this.getClass()
 				.getSimpleName());
 		if (init) {
 			if(ecConnectionManager!=null) this.ecConnectionManager.destroy();
 			this.etalis.shutdown();
 			this.eventSinks.clear();
 			this.init = false;
 		}
 	}
 
 	@Override
 	public void registerEventPattern(BdplQuery bdplQuery) throws DcepManagementException {
 		if (!init) {
 			throw new IllegalStateException(this.getClass().getSimpleName()+ " has not been initialized.");
 		}
 		if (bdplQuery.getDetails() == null) {
 			throw new IllegalArgumentException("QueryDetails is not set");
 		}
 		logger.info("New event pattern is being registered at {} with queryId = {}",
 				this.getClass().getSimpleName(), bdplQuery
 				.getDetails().getQueryId());
 		logger.debug("ELE: {}", bdplQuery.getEleQuery());
 
 		if(this.registeredQueries.containsKey(bdplQuery.getDetails().getQueryId())) {
 			String error = "Pattern ID already exists: " + bdplQuery.getDetails().getQueryId();
 			logger.error(error);
 			throw new DcepManagementException(error);
 		}
 		
 		try {
 			this.registeredQueries.put(bdplQuery.getDetails().getQueryId(), bdplQuery);
 		
 			logger.debug("Register query: {}", bdplQuery.getEleQuery());
 			
 			etalis.addDynamicRuleWithId("'" + bdplQuery.getDetails().getQueryId() + "'" + bdplQuery.getDetails().getEtalisProperty(), bdplQuery.getEleQuery());
 			// Start tumbling window. (If a tumbling window was defined.)
 			if (!etalis.getEngineWrapper().executeGoal(bdplQuery.getDetails().getTumblingWindow())) {
 				throw new DistributedEtalisException("Error registering tumbling window for queryId " + bdplQuery.getDetails().getQueryId());
 			}
 			
 			//Register db queries.
 			for (String dbQuerie : bdplQuery.getDetails().getRdfDbQueries()) {
 				if (!etalis.getEngineWrapper().executeGoal("assert(" + dbQuerie + ")")) {
 					throw new DistributedEtalisException("Error registering RdfDbQueries for queryId " + bdplQuery.getDetails().getQueryId());
 				}
 			}
 			
 			// Configure ETALIS to inform output listener if complex event of new type appeared.
 			etalis.addEventTrigger(bdplQuery.getDetails().getComplexType() + "/_");
 		
 			// Make subscriptions.
 			this.ecConnectionManager.registerEventPattern(bdplQuery);
 		} catch (PrologException e) {
 			this.unregisterEventPattern(bdplQuery.getDetails().getQueryId());
 			throw new DcepManagementException(e.getMessage());
 		} catch (EcConnectionmanagerException e) {
 			this.unregisterEventPattern(bdplQuery.getDetails().getQueryId());
 			throw new DcepManagementException(e.getMessage());
 		} catch (Exception e) {
 			this.unregisterEventPattern(bdplQuery.getDetails().getQueryId());
 			throw new DcepManagementException(e.getMessage());
 		}
 	}
 
 	@Override
 	public BdplQuery getRegisteredEventPattern(String queryId) throws DcepManagementException {
 		if (!init) {
 			throw new IllegalStateException(this.getClass().getSimpleName()
 					+ " has not been initialized.");
 		}
 		
 		if (this.registeredQueries.get(queryId) != null) {
 			return this.registeredQueries.get(queryId);
 		}
 		else {
 			throw new DcepManagementException("No event pattern is registered with id: " + queryId);
 		}
 	}
 
 	@Override
 	public Map<String, BdplQuery> getRegisteredEventPatterns() {
 		if (!init) {
 			throw new IllegalStateException(this.getClass().getSimpleName()
 					+ " has not been initialized.");
 		}
 
 		return this.registeredQueries;
 	}
 
 	@Override
 	public void unregisterEventPattern(String queryId) {
 		if (!init) {
 			throw new IllegalStateException(this.getClass().getSimpleName()
 					+ " has not been initialized.");
 		}
 
 		if (this.registeredQueries.containsKey(queryId)) {
 			logger.info("Removing event pattern at 'DistributedEtalis' Rule ID = "
 					+ queryId);
 			try {
 				etalis.removeDynamicRule(queryId);
 			} catch (PrologException e) {
 				logger.warn(String.format("Problem removing event pattern '%s': %s: %s", queryId, e.getClass().getSimpleName(), e.getMessage()));
 			}
 			this.ecConnectionManager.unregisterEventPattern(registeredQueries
 					.get(queryId));
 			this.registeredQueries.remove(queryId);
 		}
 		else {
 			logger.warn("Event pattern to be removed was not found at 'DistributedEtalis' Rule ID = "
 					+ queryId);
 		}
 	}
 
 	@Override
 	public NodeMeasurementResult getMeasuredData(String queryId) {
 		if (!init) {
 			throw new IllegalStateException(this.getClass().getSimpleName()
 					+ " has not been initialized.");
 		}
 
 		return measurementUnit.getMeasuringResults();
 	}
 
 	@Override
 	public void setConfig(Configuration configuration) throws DistributedEtalisException {
 		configuration.configure(this);
 		init = true;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public Service getService() {
 		return service;
 	}
 
 	public void setService(Service service) {
 		this.service = service;
 	}
 
 	@Override
 	public JtalisInputProvider getEventInputProvider() {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}
		
 		return eventInputProvider;
 	}
 
 	@Override
 	public void publish(CompoundEvent event) {
 		if (!init) {
 			throw new IllegalStateException(this.getClass().getSimpleName()
 					+ " has not been initialized.");
 		}
 		
 		eventInputProvider.notify(event);
 	}
 
 	@Override
 	public void attach(SimplePublishApi subscriber) {
 		logger.debug("New subscriber.");
 		this.eventSinks.add(subscriber);
 	}
 
 	@Override
 	public void detach(SimplePublishApi subscriber) {
 		this.eventSinks.remove(subscriber);
 	}
 
 	@Override
 	public void setEcConnectionManager(EcConnectionManager ecConnectionManager) {
 		this.ecConnectionManager = ecConnectionManager;
 	}
 
 	@Override
 	public void setEventOutputProvider(JtalisOutputProvider eventOutputProvider) {
 		this.eventOutputProvider = eventOutputProvider;
 	}
 
 	@Override
 	public void setEventInputProvider(JtalisInputProvider eventInputProvider) {
 		this.eventInputProvider = eventInputProvider;
 	}
 
 	@Override
 	public void setSemWebLib(PrologSemWebLib semWebLib) {
 		this.semWebLib = semWebLib;
 	}
 
 	@Override
 	public void setEtalis(JtalisContextImpl etalis) {
 		this.etalis = etalis;
 	}
 
 	@Override
 	public DistributedEtalis getDistributedEtalis(){
 		return this;
 	}
 
 	@Override
 	public Map<String, BdplQuery> getRegisteredQueries() {
 		return registeredQueries;
 	}
 
 	@Override
 	public void setRegisteredQueries(Map<String, BdplQuery> registeredQueries) {
 		this.registeredQueries = registeredQueries;
 	}
 
 	@Override
 	public EcConnectionManager getEcConnectionManager() {
 		return ecConnectionManager;
 	}
 
 	@Override
 	public Set<SimplePublishApi> getEventSinks() {
 		return eventSinks;
 	}
 
 	@Override
 	public JtalisContextImpl getEtalis() {	
 		return etalis;
 	}
 
 	@Override
	public JtalisOutputProvider getEventOutputProvider() {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}
		
 		return eventOutputProvider;
 	}
 
 	@Override
 	public void measurePerformance(MeasurementConfig config) {
 		if (!init) {
 			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
 		}
 		measurementUnit.startMeasurement(config.getMeasurementPeriod());
 	}
 	
 	@Override
 	public void setMeasurementUnit(MeasurementUnit measurementUnit) {
 		this.measurementUnit = measurementUnit;
 	}
 }
