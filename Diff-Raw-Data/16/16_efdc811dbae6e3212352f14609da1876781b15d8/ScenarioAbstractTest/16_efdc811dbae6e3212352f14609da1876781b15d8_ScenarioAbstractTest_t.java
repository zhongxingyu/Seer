 package eu.play_project.dcep.tests;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.etsi.uri.gcm.util.GCM;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.objectweb.fractal.adl.ADLException;
 import org.objectweb.fractal.adl.Factory;
 import org.objectweb.fractal.api.Component;
 import org.objectweb.fractal.api.NoSuchInterfaceException;
 import org.objectweb.fractal.api.control.IllegalLifeCycleException;
 import org.objectweb.proactive.core.component.adl.FactoryFactory;
 import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
 import eu.play_project.play_platformservices.api.QueryDispatchApi;
 import eu.play_project.querydispatcher.bdpl.tests.BdplEleTest;
 
 
 /**
  * Abstract test class dealing with initiation of the platform before each test.
  * Instatiating the platform is done with {@link BeforeClass} because the
  * native Prolog engine can be instatiated only once.
  */
 public class ScenarioAbstractTest {
 	public static QueryDispatchApi queryDispatchApi;
 	public static DistributedEtalisTestApi testApi;
 	boolean start = false;
 	public static Component root;
 	public static boolean test;
 	public final Logger logger = LoggerFactory.getLogger(ScenarioAbstractTest.class);
 	
 	@BeforeClass
 	public static void instantiatePlayPlatform()
 			throws IllegalLifeCycleException, NoSuchInterfaceException,
 			ADLException {
 
 		CentralPAPropertyRepository.JAVA_SECURITY_POLICY
 				.setValue("proactive.java.policy");
 
 		CentralPAPropertyRepository.GCM_PROVIDER
 				.setValue("org.objectweb.proactive.core.component.Fractive");
 
 		
 		Factory factory = FactoryFactory.getFactory();
 		HashMap<String, Object> context = new HashMap<String, Object>();
 
 		root = (Component) factory.newComponent("PsDcepComponent", context);
 		GCM.getGCMLifeCycleController(root).startFc();
 
 		queryDispatchApi = ((eu.play_project.play_platformservices.api.QueryDispatchApi) root.getFcInterface("QueryDispatchApi"));
 		testApi = (DistributedEtalisTestApi) root.getFcInterface("DistributedEtalisTestApi");
 		
 		try {
 			Thread.sleep(10000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@AfterClass
	public static void terminatePlayPlatform() {
 		// Stop and terminate GCM Components
 		try {
 			GCM.getGCMLifeCycleController(root).stopFc();
 			// Terminate all subcomponents.
 			 for(Component subcomponent : GCM.getContentController(root).getFcSubComponents()){
 				GCM.getGCMLifeCycleController(subcomponent).terminateGCMComponent();
 			 }
 	
 		} catch (IllegalLifeCycleException e) {
 			e.printStackTrace();
 		} catch (NoSuchInterfaceException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public String getSparqlQueries(String queryFile) throws IOException{
 		return BdplEleTest.getSparqlQuery(queryFile);
 	}
 
 }
