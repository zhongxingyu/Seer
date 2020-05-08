 package eu.play_project.play_platformservices.tests;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 
 import javax.xml.namespace.QName;
 import javax.xml.ws.Service;
 
 import org.etsi.uri.gcm.util.GCM;
 import org.junit.Test;
 import org.objectweb.fractal.adl.ADLException;
 import org.objectweb.fractal.adl.Factory;
 import org.objectweb.fractal.api.Component;
 import org.objectweb.fractal.api.NoSuchInterfaceException;
 import org.objectweb.fractal.api.control.IllegalLifeCycleException;
 import org.objectweb.proactive.core.component.adl.FactoryFactory;
 import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
 
 import eu.play_project.play_commons.constants.Constants;
 import eu.play_project.play_platformservices.PlayPlatformservices;
 import eu.play_project.play_platformservices.api.QueryDispatchException;
 
 public class PlatformservicesTest {
 
 	@Test
 	public void testCxfSoap() {
 		
 		PlayPlatformservices playPlatformservices = new PlayPlatformservices();
 		playPlatformservices.initComponentActivity(null);
 		playPlatformservices.endComponentActivity(null);
 		
 	}
 	
 	@Test
 	public void testPlatformservicesComponent() throws ADLException, IllegalLifeCycleException,
 			NoSuchInterfaceException, InterruptedException, QueryDispatchException {
 		/*
 		 * Start Platformservices server
 		 */
 		CentralPAPropertyRepository.JAVA_SECURITY_POLICY
 		.setValue("proactive.java.policy");
 
 		CentralPAPropertyRepository.GCM_PROVIDER
 		.setValue("org.objectweb.proactive.core.component.Fractive");
 		
 		Factory factory = FactoryFactory.getFactory();
 		HashMap<String, Object> context = new HashMap<String, Object>();
 		
 		Component root = (Component) factory.newComponent("PlatformServicesTest", context);
 		GCM.getGCMLifeCycleController(root).startFc();
 		
 		/*
 		 * Start client and get WSDL
 		 */
 		URL wsdl = null;
 		String address = Constants.getProperties().getProperty("platfomservices.querydispatchapi.endpoint");
 		
 		try {
 			wsdl = new URL(address + "?wsdl");
 		} catch (MalformedURLException e) {
 		e.printStackTrace();
 		}
 
 		QName serviceName = new QName("http://play_platformservices.play_project.eu/", "QueryDispatchApi");
 
 		Service service = Service.create(wsdl, serviceName);
 		service.getPort(eu.play_project.play_platformservices.api.QueryDispatchApi.class);
 
 		
 		/*
 		 * Stop server
 		 */
 		GCM.getGCMLifeCycleController(root).stopFc();
		GCM.getGCMLifeCycleController(root).terminateGCMComponent();
 	}
 
 }
