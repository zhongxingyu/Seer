package org.societies.integration.test.bit.useservice;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.net.URL;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.societies.api.schema.servicelifecycle.model.Service;
 import org.societies.api.schema.servicelifecycle.model.ServiceResourceIdentifier;
 import org.societies.api.schema.servicelifecycle.model.ServiceStatus;
 import org.societies.api.schema.servicelifecycle.servicecontrol.ServiceControlResult;
 import org.societies.example.calculator.ICalc;
 
 /**
  * This test case aims to verify that a service
  * - nor installed neither started can't be used
  * - installer but not started can't be used
  * @author Olivier Maridat (Trialog)
  *
  */
 public class SpecificTestCaseUpperTester {
 	private static Logger LOG = LoggerFactory.getLogger(SpecificTestCaseUpperTester.class);
 
 	/**
 	 * Injection of ICalc interface
 	 */
 	public static ICalc calculatorService;
 	/**
 	 * URL of the JAR of the Calculator 3P service Bundle
 	 */
 	private static String serviceBundleUrl;
 	/**
 	 * Id of the Calculator 3P service
 	 */
 	public static ServiceResourceIdentifier calculatorServiceId;
 
 
 	/**
 	 * This method is called only one time, at the very beginning of the process
 	 * (after the constructor) in order to initialize the process.
 	 */
 	@BeforeClass
 	public static void initialization() {
 		LOG.info("[#713] Initialization");
 		LOG.info("[#713] Prerequisite: The CSS is created");
 		LOG.info("[#713] Prerequisite: The user is logged to the CSS");
 
 		serviceBundleUrl = "file:C:/Application/Virgo/repository/usr/Calculator.jar";
 	}
 
 	/**
 	 * This method is called before every @Test methods.
 	 */
 	@Before
 	public void setUp() {
 		LOG.info("[#713] SpecificTestCaseUpperTester::setUp");
 	}
 
 	/**
 	 * This method is called after every @Test methods
 	 */
 	@After
 	public void tearDown() {
 		LOG.info("[#713] tearDown");
 	}
 
 
 	/**
 	 * Verify that the service is nor installed nor started
 	 * and try to consume the calculator service
 	 * 
 	 */
 	@Test(expected=NullPointerException.class)
 	public void bodyUseNotAvailableService() {
 		LOG.info("[#713] bodyUseNotAvailableService");
 
 		// --- Preamble: stop/uninstall the Calculator service if necessary
 		try {
 			// -- Search all local services
 			Future<List<Service>> asyncServices = TestCase713.serviceDiscovery.getLocalServices();
 			List<Service> services = asyncServices.get();
 
 			// -- Verify that the Calculator Service is really not available
 			// - Search the Calculator Service
 			for(Service service : services) {
 				// - Calculator found
 				if (service.getServiceLocation().value().equals(serviceBundleUrl)) {
 					// - Retrieve service id
 					calculatorServiceId = service.getServiceIdentifier();
 					// - Service is running: Stop the service
 					if (service.getServiceStatus().equals(ServiceStatus.STARTED)) {
 						LOG.info("[#713] Stop the service");
 						Future<ServiceControlResult> asynchResult = TestCase713.serviceControl.stopService(calculatorServiceId);
 						ServiceControlResult scresult = asynchResult.get();
 						if (!scresult.equals(ServiceControlResult.SUCCESS)) {
 							throw new Exception("Can't stop the service. Returned value: "+scresult.value());
 						}
 					}
 
 					// - Uninstall the service
 					LOG.info("[#713] Uninstall the service");
 					Future<ServiceControlResult> asynchResult = TestCase713.serviceControl.uninstallService(calculatorServiceId);
 					ServiceControlResult scresult = asynchResult.get();
 					if (!scresult.equals(ServiceControlResult.SUCCESS)) {
 						throw new Exception("Can't uninstall the service. Returned value: "+scresult.value());
 					}
 					break;
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			LOG.info("[#713] Unknown Exception", e);
 			fail("[#713] Unknown Exception: "+e.getMessage());
 			return;
 		}
 
 		
 		// --- Body
 		// -- Consume the service
 		int expected = 3;
 		int actual = 0;
 		try {
 			actual = NominalTestCaseLowerTester.calculatorService.Add(1, 2).get();
 			LOG.info("[#713] Consume Calculator Service 1+2="+actual);
 			assertEquals("[#713] Consume Calculator Service", expected, actual);
 		} catch (InterruptedException e) {
 			LOG.info("[#713] InterruptedException", e);
 			fail("[#713] InterruptedException: "+e.getMessage());
 		} catch (ExecutionException e) {
 			LOG.info("[#713] ExecutionException", e);
 			fail("[#713] ExecutionException: "+e.getMessage());
 		}
 	}
 
 	/**
 	 * Verify that the service is installed but not started
 	 * and try to consume the calculator service
 	 */
 	@Test(expected=NullPointerException.class)
 	public void bodyUseStillNotAvailableService() {
 		LOG.info("[#713] bodyUseStillNotAvailableService");
 
 		// --- Preamble: install/stop the Calculator service if necessary
 		try {
 			// -- Search all local services
 			Future<List<Service>> asyncServices = TestCase713.serviceDiscovery.getLocalServices();
 			List<Service> services = asyncServices.get();
 
 			// -- Verify that the Calculator Service is really not available
 			// - Search the Calculator Service
 			boolean found = false;
 			for(Service service : services) {
 				// - Calculator found
 				if (service.getServiceLocation().value().equals(serviceBundleUrl)) {
 					found = true;
 					// - Retrieve the service id
 					calculatorServiceId = service.getServiceIdentifier();
 					// - Service is running: Stop the service
 					if (service.getServiceStatus().equals(ServiceStatus.STARTED)) {
 						LOG.info("[#713] Stop the service");
 						Future<ServiceControlResult> asynchResult = TestCase713.serviceControl.stopService(calculatorServiceId);
 						ServiceControlResult scresult = asynchResult.get();
 						if (!scresult.equals(ServiceControlResult.SUCCESS)) {
 							throw new Exception("Can't stop the service. Returned value: "+scresult.value());
 						}
 					}
 					break;
 				}
 			}
 			// - Service not installed: install it
 			if (!found) {
 				// - Install the service
 				LOG.info("[#713] Install the service");
 				URL serviceUrl = new URL(serviceBundleUrl);
 				Future<ServiceControlResult> asynchResult = TestCase713.serviceControl.installService(serviceUrl, "");
 				ServiceControlResult scresult = asynchResult.get();
 				if (!scresult.equals(ServiceControlResult.SUCCESS)) {
 					throw new Exception("Can't install the service. Returned value: "+scresult.value());
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			LOG.info("[#713] Unknown Exception", e);
 			fail("[#713] Unknown Exception: "+e.getMessage());
 			return;
 		}
 
 		// --- Body
 		try {
 			// -- Consume the service
 			int expected = 3;
 			int actual = 0;
 			actual = NominalTestCaseLowerTester.calculatorService.Add(1, 2).get();
 			LOG.info("[#713] Consume Calculator Service 1+2="+actual);
 			assertEquals("[#713] Consume Calculator Service", expected, actual);
 		} catch (InterruptedException e) {
 			LOG.info("[#713] InterruptedException", e);
 			fail("[#713] InterruptedException: "+e.getMessage());
 		} catch (ExecutionException e) {
 			LOG.info("[#713] ExecutionException", e);
 			fail("[#713] ExecutionException: "+e.getMessage());
 		}
 
 
 		// --- Postemble
 		try {
 			LOG.info("[#713] Uninstall the service");
 			Future<ServiceControlResult> asynchResult = TestCase713.serviceControl.uninstallService(NominalTestCaseLowerTester.calculatorServiceId);
 			ServiceControlResult scresult = asynchResult.get();
 			if (!scresult.equals(ServiceControlResult.SUCCESS)) {
 				throw new Exception("Can't uninstall the service. Returned value: "+scresult.value());
 			}
 		}
 		catch (Exception e)
 		{
 			LOG.info("[#713] Unknown Exception", e);
 			fail("[#713] Unknown Exception: "+e.getMessage());
 			return;
 		}
 	}
 
 	public void setCalculatorService(ICalc calculatorService) {
 		LOG.info("[#713] Calculator Service injected");
 		this.calculatorService = calculatorService;
 	}
 }
