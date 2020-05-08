 package edu.mines.acmX.exhibit.input_services.hardware;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Test;
 
import edu.mines.acmX.exhibit.module_manager.DependencyType;
import edu.mines.acmX.exhibit.module_manager.ModuleMetaData;
 
 /**
  * JUnit tests for the HardwareManager.
  * This includes checking the validity of the meta data, as well checking
  * permissions for a given ModuleMetaData.
  * 
  * @author Aakash Shah
  * @author Ryan Stauffer
  *
  * @see {@link ModuleMetaData}
  */
 
 public class HardwareManagerTest {
 	
 	public static final String BASE_FILE = "src/test/resources/input_services/";
 	
 	/**
 	 * Checks to ensure the driver classes given by the manifest file exists and 
 	 * is able to be initialized.
 	 * @throws HardwareManagerManifestException
 	 * @throws DeviceConnectionException 
 	 */
 	@Test(expected=HardwareManagerManifestException.class)
 	public void testMissingDriverClass() throws HardwareManagerManifestException, DeviceConnectionException  {
 		HardwareManager.setManifestFilepath(BASE_FILE + "BadMissingDriverClass.xml");
 	}
 	
 	/**
 	 * Checks to ensure the interfaces given by the manifest file exists
 	 * and is able to be initialized.
 	 * @throws HardwareManagerManifestException
 	 * @throws DeviceConnectionException 
 	 */
 	@Test(expected=HardwareManagerManifestException.class)
 	public void testMissingInterface() throws HardwareManagerManifestException, DeviceConnectionException  {
 		HardwareManager.setManifestFilepath(BASE_FILE + "BadMissingInterface.xml");
 	}
 	
 	/**
 	 * Checks for disjointness between the functionalities and those supported
 	 * by devices.
 	 * @throws HardwareManagerManifestException
 	 * @throws DeviceConnectionException 
 	 */
 	@Test(expected=HardwareManagerManifestException.class)
 	public void testSupportsVersusAvailableFunctionalities()
 			throws HardwareManagerManifestException, DeviceConnectionException {
 		HardwareManager.setManifestFilepath(BASE_FILE + "BadDisjointSupportList.xml");
 	}
 	
 	/**
 	 * Ensures that optional functionalities requested by a module do not
 	 * prevent the module from running if not supported.
 	 * 
 	 * @throws HardwareManagerManifestException
 	 * @throws DeviceConnectionException
 	 * @throws BadDeviceFunctionalityRequestException
 	 */
 	@Test
 	public void testModulePermissionsPassingOptional()
 			throws 	HardwareManagerManifestException, DeviceConnectionException,
 					BadDeviceFunctionalityRequestException {
 		
 		HardwareManager.setManifestFilepath(BASE_FILE + "ModulePermissionsTest.xml");
 		HardwareManager hm = HardwareManager.getInstance();
 		
 		Map<String, DependencyType> inputTypes = new HashMap<String, DependencyType>();
 		inputTypes.put("image2d", DependencyType.OPTIONAL);
 		
 		hm.setRunningModulePermissions(inputTypes);
 	}
 	
 	/**
 	 * Ensures that requried functionalities prevent the module from running if
 	 * not supported.
 	 * 
 	 * @throws HardwareManagerManifestException
 	 * @throws DeviceConnectionException
 	 * @throws BadDeviceFunctionalityRequestException
 	 */
 	@Test
 	public void testModulePermissionsPassingRequired()
 			throws 	HardwareManagerManifestException, DeviceConnectionException,
 					BadDeviceFunctionalityRequestException {
 		
 		HardwareManager.setManifestFilepath(BASE_FILE + "ModulePermissionsTest.xml");
 		HardwareManager hm = HardwareManager.getInstance();
 		
 		Map<String, DependencyType> inputTypes = new HashMap<String, DependencyType>();
 		inputTypes.put("depth", DependencyType.REQUIRED);
 		
 		hm.setRunningModulePermissions(inputTypes);
 	}
 	
 	/**
 	 * Ensures that required functionalities requested by a module that are not
 	 * supported, prevent the module from running.
 	 * 
 	 * @throws HardwareManagerManifestException
 	 * @throws DeviceConnectionException
 	 * @throws BadDeviceFunctionalityRequestException
 	 */
 	@Test(expected=BadDeviceFunctionalityRequestException.class)
 	public void testModulePermissionsFailing()
 			throws 	HardwareManagerManifestException, DeviceConnectionException,
 					BadDeviceFunctionalityRequestException {
 		
 		HardwareManager.setManifestFilepath(BASE_FILE + "ModulePermissionsTest.xml");
 		
 		HardwareManager hm = HardwareManager.getInstance();
 		
 		Map<String, DependencyType> inputTypes = new HashMap<String, DependencyType>();
 		inputTypes.put("image2d", DependencyType.REQUIRED);
 		
 		hm.setRunningModulePermissions(inputTypes);
 	}
 	
 	/**
 	 * Checks whether a functionality that does not exist throws an exception
 	 * when requested the list of drivers that support it.
 	 * 
 	 * @throws HardwareManagerManifestException
 	 * @throws DeviceConnectionException
 	 * @throws BadFunctionalityRequestException
 	 */
 	@Test(expected=BadFunctionalityRequestException.class)
 	public void testBadFunctionalityRequest()
 			throws 	HardwareManagerManifestException, DeviceConnectionException,
 					BadFunctionalityRequestException {
 		
 		HardwareManager hm = HardwareManager.getInstance();
 		hm.getDevices("BAD_FUNCTIONALITY_REQUEST");
 	}
 }
