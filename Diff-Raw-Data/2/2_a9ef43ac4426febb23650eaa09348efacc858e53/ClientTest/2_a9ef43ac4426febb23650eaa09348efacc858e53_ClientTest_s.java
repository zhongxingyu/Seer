 /**
  * 
  */
 package gov.bnl.unitconversion;
 
 import static gov.bnl.unitconversion.Device.DeviceBuilder.device;
 
 import gov.bnl.unitconversion.Conversion;
 import gov.bnl.unitconversion.ConversionClient;
 import gov.bnl.unitconversion.Device;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Map;
 
 import javax.ws.rs.core.MultivaluedMap;
 
 import org.junit.Assert;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.internal.runners.statements.Fail;
 import org.junit.rules.ExpectedException;
 
 import com.sun.jersey.core.util.MultivaluedMapImpl;
 
 /**
  * @author shroffk
  * 
  */
 public class ClientTest {
 
     @Rule
     public ExpectedException exception = ExpectedException.none();
 
     @Test
     public void testCreateClient() {
 
 	String serviceURL = "http://localhost:8000/magnets";
 	ConversionClient client = new ConversionClient(serviceURL);
 
 	exception.expect(IOException.class);
 	client = new ConversionClient("https://localhost:9432/magnets");
 
     }
 
     @Test
     public void testListSystems() {
 	Collection<String> testSystems = Arrays.asList("Linac", "LBT",
 		"Booster", "BST", "Storage Ring");
 	ConversionClient client = new ConversionClient(
 		"http://localhost:8000/magnets");
 	Collection<String> systems;
 	try {
 	    systems = client.listSystems();
 	    Assert.assertEquals(testSystems, systems);
 	} catch (IOException e) {
 	    Assert.fail(e.getMessage());
 	}
     }
 
     @Test
     public void testFindDeviceByName() {
 	/**
 	 * {"type_description": "68mm, SHORT SEXTUPOLE", "vendor":
 	 * "Danfysik, Denmark", "name": "SH1G2C30A", "install_id": 3, "system":
 	 * "Storage Ring", "cmpnt_type_name": "Sext A"}
 	 */
 	Device device_SH1G2C30A = device("SH1G2C30A").system("Storage Ring")
 		.installId(172).componentTypeName("Sext A").inventoryId(599)
 		.serialNumber(79).typeDescription("68mm, SHORT SEXTUPOLE")
 		.vendor("Danfysik, Denmark").build();
 	ConversionClient client = new ConversionClient(
 		"http://localhost:8000/magnets");
 	Collection<Device> devices;
 	try {
 	    devices = client.findDevices("SH1G2C30A");
 	    Assert.assertTrue("Failed to find device",
 		    devices.contains(device_SH1G2C30A));
 	} catch (IOException e) {
 	    e.printStackTrace();
 	}
 
 	// When seraching for SH*G2C30A we expect 3 device returned
 	//
 	// {"installId": 3, "vendor": "Danfysik, Denmark", "name": "SH1G2C30A",
 	// "serialNumber": "79",
 	// "system": "Storage Ring", "componentType": "Sext A", "inventoryId":
 	// 430,
 	// "typeDescription": "68mm, SHORT SEXTUPOLE"},
 	//
 	// {"installId": 7, "vendor": "Danfysik, Denmark", "name": "SH3G2C30A",
 	// "serialNumber": "83",
 	// "system": "Storage Ring", "componentType": "Sext A", "inventoryId":
 	// 434,
 	// "typeDescription": "68mm, SHORT SEXTUPOLE"},
 	//
 	// {"installId": 9, "vendor": "Danfysik, Denmark", "name": "SH4G2C30A",
 	// "serialNumber": "85",
 	// "system": "Storage Ring", "componentType": "Sext A", "inventoryId":
 	// 436,
 	// "typeDescription": "68mm, SHORT SEXTUPOLE"}
 	Device deviceSH1G2C30A = device("SH1G2C30A").installId(172)
 		.vendor("Danfysik, Denmark").serialNumber(79)
 		.system("Storage Ring").componentTypeName("Sext A")
 		.inventoryId(599).typeDescription("68mm, SHORT SEXTUPOLE")
 		.build();
 
 	Device deviceSH3G2C30A = device("SH3G2C30A").installId(176)
 		.vendor("Danfysik, Denmark").serialNumber(83)
 		.system("Storage Ring").componentTypeName("Sext A")
 		.inventoryId(603).typeDescription("68mm, SHORT SEXTUPOLE")
 		.build();
 
 	Device deviceSH4G2C30A = device("SH4G2C30A").installId(178)
 		.vendor("Danfysik, Denmark").serialNumber(85)
 		.system("Storage Ring").componentTypeName("Sext A")
 		.inventoryId(605).typeDescription("68mm, SHORT SEXTUPOLE")
 		.build();
 	Device[] expectedDevices = { deviceSH1G2C30A, deviceSH3G2C30A,
 		deviceSH4G2C30A };
 
 	try {
 	    devices = client.findDevices("SH*G2C30A");
 	    Assert.assertTrue("Failed to find devices", devices.size() == 3);
 	    Assert.assertArrayEquals("Device search result do not match:",
 		    expectedDevices, devices.toArray(new Device[3]));
 	} catch (IOException e) {
 	    Assert.fail(e.getMessage());
 	}
 
     }
 
     /**
      * A Test to check the searching of Devices based on various search
      * criteria.
      */
     @Test
     public void testFindDevice() {
 	// {"installId": 886,
 	// "vendor": "BINP, Russia",
 	// "name": "QH2G6C23B",
 	// "serialNumber": "12",
 	// "system": "Storage Ring",
 	// "componentType": "Quad Cp",
 	// "inventoryId": 280,
 	// "typeDescription": "66mm, LONG, DBL COIL KINKED QUAD"}]
 	Device deviceQH2G6C23B = device("QH2G6C23B").installId(886)
 		.vendor("BINP, Russia").serialNumber(12).system("Storage Ring")
 		.componentTypeName("Quad Cp").inventoryId(280)
 		.typeDescription("66mm, LONG, DBL COIL KINKED QUAD").build();
 
 	// QM2G4C01B
 
 	// A1SD2
 
 	Collection<Device> results;
 	ConversionClient client = new ConversionClient(
 		"http://localhost:8000/magnets");
 	MultivaluedMap<String, String> searchParameters = new MultivaluedMapImpl();
 	try {
 	    // Search by system
 	    searchParameters.add("system", "Storage Ring");
 	    results = client.findDevices(searchParameters);
 	    Assert.assertTrue("Failed to search by system",
 		    results.contains(deviceQH2G6C23B));
 	    for (Device device : results) {
 		Assert.assertTrue("Failed to serach correctly:", device
 			.getSystem().equalsIgnoreCase("Storage Ring"));
 	    }
 	    // Search by componentType
 	    searchParameters.clear();
 	    searchParameters.add("cmpnt_type", "Quad Cp");
 	    results = client.findDevices(searchParameters);
 	    Assert.assertTrue("Failed to search by componentType",
 		    results.contains(deviceQH2G6C23B));
 	    for (Device device : results) {
 		Assert.assertTrue("Failed to serach correctly:", device
 			.getComponentType().equalsIgnoreCase("Quad Cp"));
 	    }
 	    // Search by serialNumber
 	    searchParameters.clear();
 	    searchParameters.add("serialno", String.valueOf(12));
 	    results = client.findDevices(searchParameters);
 	    Assert.assertTrue("Failed to search by serialNumber",
 		    results.contains(deviceQH2G6C23B));
 	    for (Device device : results) {
 		Assert.assertTrue("Failed to serach correctly:",
 			device.getSerialNumber() == 12);
 	    }
 	} catch (Exception e) {
 	    Assert.fail(e.getMessage());
 	}
     }
 
     /**
      * Search for Devices using a combination for search parameters
      */
     @Test
     public void testFindDevice2() {
 
     }
 
     @Test
     public void testConversion() {
 	// {"LN-SO5": {
 	// "municonvChain": {
 	// "standard": {
 	// "algorithms": {
 	// "i2b": {
 	// "function": "-0.000423222575196*input -0.00021717376728",
 	// "resultUnit": "T",
 	// "algorithmId": 0,
 	// "initialUnit": "A",
 	// "auxInfo": 0}
 	// }, "description": "average solenoid measurement data"}},
 	// "municonv": {
 	// "standard": {
 	// "measurementData": {
 	// "direction": ["na", "na", "na", "na", "na", "na", "na", "na", "na",
 	// "na", "na", "na", "na", "na", "na", "na", "na", "na", "na", "na",
 	// "na"],
 	// "currentUnit": "A",
 	// "magneticLength": ["", "", "", "", "", "", "", "", "", "", "", "",
 	// "", "", "", "", "", "", "", "", ""],
 	// "serialNumber": 53,
 	// "current": [0.0, 5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0,
 	// 50.0, 55.0, 60.0, 65.0, 70.0, 75.0, 80.0, 85.0, 90.0, 95.0, 100.0],
 	// "field": [-0.000132, -0.001228, -0.002381, -0.003523, -0.004655,
 	// -0.005798, -0.00693, -0.00806, -0.009285, -0.010423, -0.011546,
 	// -0.012694, -0.013817, -0.014941, -0.016098, -0.017217, -0.018358,
 	// -0.019476, -0.020613, -0.021764, -0.022898],
 	// "fieldError": ["", "", "", "", "", "", "", "", "", "", "", "", "",
 	// "", "", "", "", "", "", "", ""],
 	// "currentError": ["", "", "", "", "", "", "", "", "", "", "", "", "",
 	// "", "", "", "", "", "", "", ""],
 	// "fieldUnit": "T"},
 	// "algorithms": {
 	// "i2b": {
 	// "function": "0.000228046038239*input + 0.000113748",
 	// "resultUnit": "T",
 	// "algorithmId": 0,
 	// "initialUnit": "A",
 	// "auxInfo": 0}},
 	// "description": "individual solenoid measurement data"}}}}
 	
 	ConversionClient client = new ConversionClient(
 		"http://localhost:8000/magnets");
 	try {
 	    Collection<Device> result = client.getConversionInfo("LN-SO5");
 	    Assert.assertTrue("Failed to gather conversionInfo",
 		    result.size() == 1);
 	} catch (Exception e) {
 	    Assert.fail(e.getMessage());
 	}
 
     }
 
     @Test
     public void testConversion2() {
 	ConversionClient client = new ConversionClient(
 		"http://localhost:8000/magnets");
 	try {
 	    Collection<Device> result = client.getConversionInfo("QH*G6C23B");
 	    Assert.assertTrue("Failed to gather conversionInfo",
		    result.size() == 1);
 	} catch (Exception e) {
 	    Assert.fail(e.getMessage());
 	}
 
     }
 
 }
