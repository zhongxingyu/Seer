 package de.ptb.epics.eve.data.measuringstation.filter.tests;
 
 import static de.ptb.epics.eve.data.tests.internal.LogFileStringGenerator.*;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.RollingFileAppender;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.xml.sax.SAXException;
 
 import de.ptb.epics.eve.data.measuringstation.DetectorChannel;
 import de.ptb.epics.eve.data.measuringstation.IMeasuringStation;
 import de.ptb.epics.eve.data.measuringstation.MotorAxis;
 import de.ptb.epics.eve.data.measuringstation.filter.ExcludeDevicesOfScanModuleFilter;
 import de.ptb.epics.eve.data.measuringstation.processors.MeasuringStationLoader;
 import de.ptb.epics.eve.data.scandescription.Axis;
 import de.ptb.epics.eve.data.scandescription.Channel;
 import de.ptb.epics.eve.data.scandescription.ScanModule;
 import de.ptb.epics.eve.data.tests.internal.Configurator;
 
 /**
  * <code>ExcludeDevicesOfScanModuleFilterTest</code> contains 
  * <a href="http://www.junit.org/">JUnit</a>-Tests for 
  * {@link de.ptb.epics.eve.data.measuringstation.filter.ExcludeDevicesOfScanModuleFilter}.
  * 
  * @author Marcus Michalsky
  * @since 0.4.1
  */
 public class ExcludeDevicesOfScanModuleFilterTest {
 	
 	private static Logger logger = 
 		Logger.getLogger(ExcludeDevicesOfScanModuleFilterTest.class.getName());
 	
 	private static File schemaFile;
 	private static File descriptionFile;
 	private static IMeasuringStation measuringStation;
 	
 	private ExcludeDevicesOfScanModuleFilter filteredMeasuringStation;
 	
 	/**
 	 * Tests the exclusion of axes (motor axes) in an 
 	 * {@link de.ptb.epics.eve.data.measuringstation.filter.ExcludeDevicesOfScanModuleFilter} 
 	 * which are added to a scan module.
 	 */
 	@Ignore("has to be updated")
 	@Test
 	public void testExcludeAxis()
 	{
 		
 		
 		// create a measuring station with filtered axes
 		filteredMeasuringStation = new ExcludeDevicesOfScanModuleFilter(
 				true, false, false, false, false);
 		// assign the source measuring station
 		filteredMeasuringStation.setSource(measuringStation);
 		// check if everything worked so far
 		assertNotNull(filteredMeasuringStation);
 		
 		// create a scan module with id = 1
 		ScanModule scanModule = new ScanModule(1);
 		// assign it to the filtered station
 		filteredMeasuringStation.setScanModule(scanModule);
 		
 		// the motor axis should be present
 		assertNotNull(filteredMeasuringStation.getMotorAxisById("Counter01.01"));
 		
 		// add the axis to the scan module
 		MotorAxis ma = filteredMeasuringStation.getMotorAxisById("Counter01.01");
 		Axis a = new Axis(scanModule);
 		a.setMotorAxis(ma);
 		scanModule.add(a);
 		
 		// since we added the axis to the scan module AND set the filter to 
 		// filter axes, it shouldn't be present anymore
 		assertNull(filteredMeasuringStation.getMotorAxisById("Counter01.01"));
 		
 		// repeat the same procedure with a detector channel
 		assertNotNull(filteredMeasuringStation.
 				getDetectorChannelById("ringCurrent1chan1"));
 		
 		DetectorChannel detCh = 
 			measuringStation.getDetectorChannelById("ringCurrent1chan1");
 		Channel ch = new Channel(scanModule);
 		ch.setDetectorChannel(detCh);
 		scanModule.add(ch);
 		
 		// since we constructed the filter with "false" as second argument
 		// the detector should still be there
 		assertNotNull(filteredMeasuringStation.
 				getDetectorChannelById("ringCurrent1chan1"));
 	}
 	
 	/**
 	 * Tests the exclusion of channels (detector channels) in an 
 	 * {@link de.ptb.epics.eve.data.measuringstation.filter.ExcludeDevicesOfScanModuleFilter} 
 	 * which are added to a scan module.
 	 */
 	@Ignore("has to be updated")
 	@Test
 	public void testExcludeChannel()
 	{
 		// create a measuring station with filtered channels
 		filteredMeasuringStation = new ExcludeDevicesOfScanModuleFilter(
 				false, true, false, false, false);
 		// assign the source measuring station
 		filteredMeasuringStation.setSource(measuringStation);
 		// check if everything worked so far
 		assertNotNull(filteredMeasuringStation);
 		
 		// create a scan module with id = 1
 		ScanModule scanModule = new ScanModule(1);
 		// assign it to the filtered station
 		filteredMeasuringStation.setScanModule(scanModule);
 		
 		// the detector channel should be present
 		assertNotNull(filteredMeasuringStation.
 				getDetectorChannelById("ringCurrent1chan1"));
 		
 		// add the channel to the scan module
 		DetectorChannel detCh = 
 			measuringStation.getDetectorChannelById("ringCurrent1chan1");
 		Channel ch = new Channel(scanModule);
 		ch.setDetectorChannel(detCh);
 		scanModule.add(ch);
 		
 		// since we added the channel to the scan module AND set the filter to 
 		// filter channels, it shouldn't be present anymore
 		assertNull(filteredMeasuringStation.
 				getDetectorChannelById("ringCurrent1chan1"));
 		
 		// repeat the same procedure with a motor axis
 		assertNotNull(filteredMeasuringStation.getMotorAxisById("Counter01.01"));
 		
 		MotorAxis ma = filteredMeasuringStation.getMotorAxisById("Counter01.01");
 		Axis a = new Axis(scanModule);
 		a.setMotorAxis(ma);
 		scanModule.add(a);
 		
 		// since we constructed the filter with "false" as first argument
 		// the axis should still be there
 		assertNotNull(filteredMeasuringStation.getMotorAxisById("Counter01.01"));
 	}
 	
 	// ***********************************************************************
 	// ***********************************************************************
 	// ***********************************************************************
 	
 	/**
 	 * class wide setup method
 	 */
 	@BeforeClass
 	public static void runBeforeClass() {
 		
 		Configurator.configureLogging();
 		
 		((RollingFileAppender)logger.getAppender(
 				"ExcludeDevicesOfScanModuleFilterTestAppender")).rollOver();
 		
 		// run for one time before all test cases
 		schemaFile = new File("xml/scml.xsd");
		descriptionFile = new File("xml/test.xml");
 		
 		final MeasuringStationLoader measuringStationLoader = 
 			new MeasuringStationLoader(schemaFile);
 		
 		try {
 			measuringStationLoader.load(descriptionFile);
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		measuringStation = measuringStationLoader.getMeasuringStation();
 		
		assertNotNull(measuringStation);
 		classSetUp(logger);
 	}
 	
 	/**
 	 * test wide setup method
 	 */
 	@Before
 	public void beforeEveryTest()
 	{
 		testSetUp(logger);
 	}
 	
 	/**
 	 * test wide tear down method
 	 */
 	@After
 	public void afterEveryTest()
 	{
 		filteredMeasuringStation = null;
 		testTearDown(logger);
 	}
 	
 	/**
 	 * class wide tear down method
 	 */
 	@AfterClass
 	public static void afterClass()
 	{
 		schemaFile = null;
 		descriptionFile = null;
 		
 		classTearDown(logger);
 	}
 }
