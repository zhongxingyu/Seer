 package eu.play_project.dcep;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.etsi.uri.gcm.util.GCM;
 import org.objectweb.fractal.adl.ADLException;
 import org.objectweb.fractal.adl.Factory;
 import org.objectweb.fractal.api.Component;
 import org.objectweb.fractal.api.NoSuchInterfaceException;
 import org.objectweb.fractal.api.control.IllegalLifeCycleException;
 import org.objectweb.proactive.Body;
 import org.objectweb.proactive.core.component.adl.FactoryFactory;
 import org.objectweb.proactive.core.component.body.ComponentEndActive;
 import org.objectweb.proactive.core.component.body.ComponentInitActive;
 import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import eu.play_project.dcep.api.DcepManagmentApi;
 import eu.play_project.dcep.api.DcepMonitoringApi;
 import eu.play_project.dcep.api.measurement.NodeMeasuringResult;
 import eu.play_project.dcep.distributedetalis.DistributedEtalis;
 import eu.play_project.dcep.distributedetalis.api.ConfigApi;
 import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
 import eu.play_project.dcep.distributedetalis.api.SimplePublishApi;
 import eu.play_project.dcep.distributedetalis.configurations.DefaultConfiguration;
 import eu.play_project.play_platformservices.api.EpSparqlQuery;
 import fr.inria.eventcloud.api.CompoundEvent;
 
 /**
  * Distributed Complex Event Processing (DCEP) component. This component is a
  * container to hold one or more instances of {@linkplain DistributedEtalis} to
  * create a distributed network.
  * 
  * @author Stefan Obermeier
  * @author Roland Stühmer
  */
 public class Dcep implements DcepMonitoringApi, DcepManagmentApi,
 		ComponentInitActive, ComponentEndActive, DistributedEtalisTestApi,
 		Serializable {
 
 	private static final long serialVersionUID = 1L;
 	private DistributedEtalisTestApi dEtalisTest;
 	private DcepMonitoringApi dEtalisMonitoring;
 	private DcepManagmentApi dEtalisManagment;
 	private ConfigApi configApi;
 	private Component dEtalis;
 	private Logger logger;
 	private boolean init = false; // Shows if variables for other components are
 									// initialized.
 
 	public Dcep() {
 	}
 
 	@Override
 	public void initComponentActivity(Body body) {
 		logger = LoggerFactory.getLogger(this.getClass());
 		logger.info("Initialising {} component.", this.getClass()
 				.getSimpleName());
 
 		CentralPAPropertyRepository.GCM_PROVIDER
 				.setValue("org.objectweb.proactive.core.component.Fractive");
 	}
 
 	@Override
 	public void endComponentActivity(Body arg0) {
 		logger.info("Terminating {} component.", this.getClass()
 				.getSimpleName());
 		try {
 			GCM.getGCMLifeCycleController(this.dEtalis).stopFc();
 			GCM.getGCMLifeCycleController(this.dEtalis).terminateGCMComponent();
 		} catch (IllegalLifeCycleException e) {
 			logger.error("Error terminating subcomponent.", e);
 		} catch (NoSuchInterfaceException e) {
 			logger.error("Error terminating subcomponent.", e);
 		}
 	}
 
 	@Override
 	public void registerEventPattern(EpSparqlQuery epSparqlQuery) {
 
 		logger.debug("Pattern reached DCEP facade: "
 				+ epSparqlQuery.getEleQuery());
 		
 		if(!init); init();
 		dEtalisManagment.registerEventPattern(epSparqlQuery);
 	}
 
 	@Override
 	public EpSparqlQuery getRegisteredEventPattern(String queryId) {
 		if(!init); init();
 		return dEtalisManagment.getRegisteredEventPattern(queryId);
 	}
 
 	@Override
 	public Map<String, EpSparqlQuery> getRegisteredEventPatterns() {
 		if(!init); init();
 		return dEtalisManagment.getRegisteredEventPatterns();
 	}
 
 	@Override
 	public void unregisterEventPattern(String queryID) {
 		if(!init); init();
 		dEtalisManagment.unregisterEventPattern(queryID);
 	}
 
 	@Override
 	public NodeMeasuringResult measurePerformance(int period) {
 		if(!init); init();
 		return dEtalisMonitoring.measurePerformance(period);
 	}
 
 	@Override
 	public NodeMeasuringResult getMeasurementData() {
 		if(!init); init();
 		return dEtalisMonitoring.getMeasurementData();
 	}
 
 	@Override
 	public void publish(CompoundEvent event) {
 		if(!init); init();
 		dEtalisTest.publish(event);
 	}
 
 	@Override
 	public void attach(SimplePublishApi subscriber) {
 		if(!init); init();
 		dEtalisTest.attach(subscriber);
 	}
 
 	@Override
 	public void detach(SimplePublishApi subscriber) {
 		if(!init); init();
 		dEtalisTest.detach(subscriber);
 	}
 
 	/**
 	 * Init connections to dEtalis components.
 	 * 
 	 * @return
 	 */
 	private boolean init() {
 
 		if (!init) {
 			Factory factory;
 
 			try {
 				factory = FactoryFactory.getFactory();
 
 				HashMap<String, Object> context = new HashMap<String, Object>();
 
 				this.dEtalis = (Component) factory.newComponent(
 						"DistributedEtalis", context);
 				GCM.getGCMLifeCycleController(dEtalis).startFc();
 
 				dEtalisTest = ((DistributedEtalisTestApi) dEtalis
 						.getFcInterface("DistributedEtalisTestApi"));
 				dEtalisManagment = ((DcepManagmentApi) dEtalis
 						.getFcInterface("DcepManagmentApi"));
 				dEtalisMonitoring = ((DcepMonitoringApi) dEtalis
 						.getFcInterface("DcepMonitoringApi"));
 				configApi = ((ConfigApi)dEtalis);
 				configApi.setConfig(new DefaultConfiguration());
 			} catch (NoSuchInterfaceException e) {
 				logger.error("Error: ", e);
 			} catch (ADLException e) {
 				logger.error("Error: ", e);
 			} catch (IllegalLifeCycleException e) {
 				logger.error("Error: ", e);
 			}
 			init = true;
 		}
 		return init;
 	}
 }
