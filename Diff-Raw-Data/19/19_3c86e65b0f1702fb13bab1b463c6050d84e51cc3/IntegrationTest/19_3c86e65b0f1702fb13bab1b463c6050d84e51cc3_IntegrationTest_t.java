 package org.unbiquitous.uos.core.integration;
 
 import static org.fest.assertions.api.Assertions.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ListResourceBundle;
 import java.util.Map.Entry;
 import java.util.ResourceBundle;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.unbiquitous.uos.core.ContextException;
 import org.unbiquitous.uos.core.UOSApplicationContext;
 import org.unbiquitous.uos.core.driver.DeviceDriver;
 import org.unbiquitous.uos.core.driverManager.DriverData;
 
 
 public class IntegrationTest {
 	
     @Before public void setUp() throws IOException{
 		new File("resources/owl/uoscontext.owl").createNewFile();
 	}
 	
 	@After public void tearDown(){
 		new File("resources/owl/uoscontext.owl").delete();
 	}
 	
 	//TODO: Better explain the purpose of this test
 	@Test public void execute() throws Exception{
 		//Driver Side
 		// TODO: This bypass does not test how the middleware instantiates drivers and applications
 		String pcName = "my.pc";
 		UOSApplicationContext pc = startContext(pcName);
 		EchoDriver echo = new EchoDriver();
 		pc.getDriverManager().deployDriver(echo.getDriver(), echo);
 		pc.getDriverManager().initDrivers(pc.getGateway());//TODO: What an ugly thing to do, should be initialized automaticali
 
 		//App side
 		String cellName = "my.cell";
 		UOSApplicationContext cell = startContext(cellName);
 		PingApp ping = new PingApp();
 		cell.getApplicationManager().deploy(ping, "pingApp"); //TODO: id should be plausibly auto assigned
 		
 		//promote radar handshake		
 		cell.getRadarControlCenter().deviceEntered(new IntegrationDevice(pcName));
 		
 		//Test if handshake was successfull
 		assertThat(cell.getGateway().listDrivers(echo.getDriver().getName())).
 			isNotEmpty();
 		assertThat(cell.getGateway().listDrivers("uos.DeviceDriver")).hasSize(2);
		assertThat(cell.getGateway().listDrivers(null)).hasSize(3);
 		assertThat(cell.getGateway().listDevices()).hasSize(2);
 		assertThat(pc.getGateway().listDrivers("uos.DeviceDriver")).hasSize(2);
		assertThat(pc.getGateway().listDrivers(null)).hasSize(3);
 		assertThat(pc.getGateway().listDevices()).hasSize(2);
 		
 		synchronized (PingApp.instance) {
 			PingApp.instance.wait(5000);
 		}
 		
 		//checkresults
 		for (Entry<String, Boolean> e : ping.assertions.entrySet()){
 			assertTrue(e.getKey(),e.getValue());
 		}
 		for (Entry<String, Boolean> e : echo.assertions.entrySet()){
 			assertTrue(e.getKey(),e.getValue());
 		}
 		
 		//Estimulate the deviceLeft
 		cell.getRadarControlCenter().deviceLeft(new IntegrationDevice(pcName));

 		assertThat(cell.getGateway().listDrivers("uos.DeviceDriver")).hasSize(1);
		assertThat(cell.getGateway().listDrivers(null)).hasSize(1);
 		assertThat(cell.getGateway().listDevices()).hasSize(1);
 		pc.getRadarControlCenter().deviceLeft(new IntegrationDevice(cellName));
 		assertThat(pc.getGateway().listDrivers("uos.DeviceDriver")).hasSize(1);
		assertThat(pc.getGateway().listDrivers(null)).hasSize(2);
 		assertThat(pc.getGateway().listDevices()).hasSize(1);
 		
		//finish instances
		pc.tearDown();
		cell.tearDown();
 	}
 
 	private UOSApplicationContext startContext(final String deviceName) throws ContextException{
 		ResourceBundle pcBundle = new ListResourceBundle() {
 			protected Object[][] getContents() {
 				return new Object[][] {
 					{"ubiquitos.message.response.timeout", "100"}, //Optional
 					{"ubiquitos.message.response.retry", "30"},//Optional
 					{"ubiquitos.connectionManager", IntegrationConnectionManager.class.getName()},
 					{"ubiquitos.uos.deviceName", deviceName}, //TODO: Should not be mandatory, and could be automatic
 					{"ubiquitos.driver.deploylist", DeviceDriver.class.getName()}, //TODO: Should not be mandatory
 					{"ubiquitos.ontology.path","resources/owl/uoscontext.owl"}, //TODO: Should not be mandatory
 		        };
 			}
 		};
 		
 		UOSApplicationContext instance = new UOSApplicationContext();
 		instance.init(pcBundle);
 		return instance;
 	}
 	
 }
