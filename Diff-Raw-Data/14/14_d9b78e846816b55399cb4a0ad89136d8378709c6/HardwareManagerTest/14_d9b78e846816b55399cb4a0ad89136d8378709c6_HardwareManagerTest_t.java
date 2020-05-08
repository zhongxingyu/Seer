 package edu.mines.acmX.exhibit.input_services.hardware;
 
 import org.junit.Test;
 
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
 		System.out.println("Running testMissingDriverClass");
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
 		System.out.println("Running testMissingInterface");
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
 		System.out.println("Running testSupportsVersusAvailableFunctionalities");
 		HardwareManager.setManifestFilepath(BASE_FILE + "BadDisjointSupportList.xml");
 	}
 	
 }
