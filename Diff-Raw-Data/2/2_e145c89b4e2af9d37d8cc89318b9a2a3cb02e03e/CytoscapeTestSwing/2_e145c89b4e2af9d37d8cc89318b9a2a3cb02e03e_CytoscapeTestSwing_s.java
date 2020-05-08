 package cytoscape.unitTests;
 
 import java.awt.*;
 import java.util.*;
 import javax.swing.*;
 
 import swingunit.extensions.ExtendedRobotEventFactory;
 import swingunit.framework.EventPlayer;
 import swingunit.framework.ExecuteException;
 import swingunit.framework.FinderMethodSet;
 import swingunit.framework.RobotEventFactory;
 import swingunit.framework.Scenario;
 import swingunit.framework.TestUtility;
 import junit.framework.TestCase;
 
 import cytoscape.*;
 import cytoscape.view.*;
 
 public class CytoscapeTestSwing extends TestCase {
 	private Scenario scenario;
 	private RobotEventFactory robotEventFactory = new ExtendedRobotEventFactory();
 	private FinderMethodSet methodSet = new FinderMethodSet();
 	private Robot robot;
 	
 	private CyMain application;
 	
 	/*
 	 * @see TestCase#setUp()
 	 */
 	protected void setUp() throws Exception {
 		System.setProperty("TestSetting","testData/TestSetting.properties");
 
 		// Start application.
 		Runnable r = new Runnable() {
 			public void run() {
 				try {
 				String[] args = {};
 				application = new CyMain(args);
 				} catch (Exception e) { e.printStackTrace(); }
 			}
 		};
 		SwingUtilities.invokeAndWait(r);
 
 
 		robot = new Robot();
 		TestUtility.waitForCalm();
 		
 		// To make sure to load the scenario file. 
 		// CytoscapeTestSwing.xml is placed on the same package directory.
 		String filePath = CytoscapeTestSwing.class.getResource("CytoscapeTestSwing.xml").getFile();
 		// Create Scenario object and create XML file.
 		scenario = new Scenario(robotEventFactory, methodSet);
 		scenario.read(filePath);
 	}
 
 	/*
 	 * @see TestCase#tearDown()
 	 */
 	protected void tearDown() throws Exception {
 		// Terminate application.
 		Runnable r = new Runnable() {
 			public void run() {
 			//	if(application != null) {
 			//		application.setVisible(false);
 			//	}
 			}
 		};
 		SwingUtilities.invokeAndWait(r);
 
 		application = null;
 		scenario = null;
 		robot = null;
 	}
 
 	public void testOpenCysFile() throws ExecuteException {
 		// Use keyword substitution.
		scenario.setTestSetting("OPEN_CYS_FILE","FILE_TO_OPEN","galFiltered.sif.cys");
 		EventPlayer player = new EventPlayer(scenario);
 		player.run(robot, "OPEN_CYS_FILE");
 
 		// write assertion code here.
 		Set s = Cytoscape.getNetworkSet();
 		assertTrue("exected 1, got: " + s.size(), s.size() == 1 );
 	}
 }
