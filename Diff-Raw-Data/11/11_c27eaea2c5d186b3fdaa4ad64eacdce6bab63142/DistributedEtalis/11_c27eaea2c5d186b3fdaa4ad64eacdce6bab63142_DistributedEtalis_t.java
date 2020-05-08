 package eu.play_project.dcep.distributedetalis;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.objectweb.proactive.Body;
 import org.objectweb.proactive.Service;
 import org.objectweb.proactive.core.component.body.ComponentEndActive;
 import org.objectweb.proactive.core.component.body.ComponentInitActive;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.jtalis.core.JtalisContextImpl;
 
 import eu.play_project.dcep.api.DcepManagementException;
 import eu.play_project.dcep.api.DcepManagmentApi;
 import eu.play_project.dcep.api.DcepMonitoringApi;
 import eu.play_project.dcep.api.measurement.NodeMeasuringResult;
 import eu.play_project.dcep.distributedetalis.api.ConfigApi;
 import eu.play_project.dcep.distributedetalis.api.Configuration;
 import eu.play_project.dcep.distributedetalis.api.DEtalisConfigApi;
 import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
 import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
 import eu.play_project.dcep.distributedetalis.api.EcConnectionManager;
 import eu.play_project.dcep.distributedetalis.api.SimplePublishApi;
 import eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit;
 import eu.play_project.play_platformservices.api.EpSparqlQuery;
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
 	private Logger logger;
 	private Map<String, EpSparqlQuery> registeredQueries = Collections.synchronizedMap(new HashMap<String, EpSparqlQuery>());
 	private EcConnectionManager ecConnectionManager;
 	private MeasurementUnit measurementUnit;
 	private PrologSemWebLib semWebLib;
 	private boolean init = false;
 	private final Set<SimplePublishApi> eventSinks = Collections
 			.synchronizedSet(new HashSet<SimplePublishApi>());
 
 	Service service;
 
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
 
 		logger = LoggerFactory.getLogger(DistributedEtalis.class);
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
 	public void registerEventPattern(EpSparqlQuery epSparqlQuery) {
 		if (!init) {
 			throw new IllegalStateException(this.getClass().getSimpleName()+ " has not been initialized.");
 		}
 		if (epSparqlQuery.getQueryDetails() == null) {
 			throw new IllegalArgumentException("QueryDetails is not set");
 		}
 		logger.info("New event pattern is being registered at {} with queryId = {}",
 				this.getClass().getSimpleName(), epSparqlQuery
 				.getQueryDetails().getQueryId());
 		logger.info("ELE: " + epSparqlQuery.getEleQuery());
 
 		if(this.registeredQueries.containsKey(epSparqlQuery.getQueryDetails().getQueryId())) {
 			String error = "Pattern ID already exists: " + epSparqlQuery.getQueryDetails().getQueryId();
 			logger.error(error);
 			//throw new DcepManagementException(error);
 			// FIXME stuehmer: revert to descriptive messages
 		}
 		
 		this.registeredQueries.put(epSparqlQuery.getQueryDetails().getQueryId(), epSparqlQuery);
 		
 		logger.debug("Register query: " + epSparqlQuery.getEleQuery());
 		
 		etalis.addDynamicRuleWithId("'" + epSparqlQuery.getQueryDetails().getQueryId() + "'" + epSparqlQuery.getQueryDetails().getEtalisProperty(), epSparqlQuery.getEleQuery());
 		// Start tumbling window. (If a tumbling window was defined.) 
 		etalis.getEngineWrapper().executeGoal(epSparqlQuery.getQueryDetails().getTumblingWindow());
 		
 		//Register db queries.
 		for (String dbQuerie : epSparqlQuery.getQueryDetails().getRdfDbQueries()) {
 			System.out.println(dbQuerie);
 			etalis.getEngineWrapper().executeGoal("assert(" + dbQuerie + ")");
 		}
 		
 		//Register ele querie.	
 		this.ecConnectionManager.registerEventPattern(epSparqlQuery);
 	}
 
 	@Override
 	public EpSparqlQuery getRegisteredEventPattern(String queryId) throws DcepManagementException {
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
 	public Map<String, EpSparqlQuery> getRegisteredEventPatterns() {
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
 			etalis.removeDynamicRule(queryId);
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
 	public void measurePerformance(int measuringPeriod) {
 		if (!init) {
 			throw new IllegalStateException(this.getClass().getSimpleName()
 					+ " has not been initialized.");
 		}
 
 		if(measurementUnit==null){
 			//Configure and set MeasuremntUnit.
			//measurementUnit = new MeasurementUnit(this, (PlayJplEngineWrapper)etalis.getEngineWrapper(), semWebLib);
 		}
 		
 		measurementUnit.startMeasurement(measuringPeriod);
 	}
 
 	@Override
 	public NodeMeasuringResult getMeasurementData() {
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
 		return eventInputProvider;
 	}
 
 	@Override
 	public void publish(CompoundEvent event) {
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
 	public Logger getLogger() {
 		return logger;
 	}
 
 	@Override
 	public Map<String, EpSparqlQuery> getRegisteredQueries() {
 		return registeredQueries;
 	}
 
 	@Override
 	public void setRegisteredQueries(Map<String, EpSparqlQuery> registeredQueries) {
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
 		return eventOutputProvider;
 	}
 }
