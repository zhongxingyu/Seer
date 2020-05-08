 package ussr.remote;
 
 
 import java.io.IOException;
 import java.rmi.RemoteException;
 import ussr.aGui.GeneralController;
 import ussr.aGui.MainFrameSeparate;
 import ussr.aGui.MainFrameSeparateController;
 import ussr.aGui.tabs.SimulationTab;
 import ussr.aGui.tabs.controllers.ConsoleTabController;
 import ussr.remote.facade.ActiveSimulation;
 import ussr.remote.facade.GUICallbackControlImpl;
 
 import ussr.remote.facade.RemotePhysicsSimulation;
 
 /**
  * Is responsible for connecting GUI with remote simulation.
  * @author Konstantinas
  */
 public class GUISimulationAdapter {
 
 	public static final int SERVER_PORT = 54323;
 
 	/**
 	 * The server to run simulation on.
 	 */
 	private static SimulationLauncherServer server;
 
 	public GUISimulationAdapter(){
 		// Start a simulation server (one that manages a number of running simulation processes)
 		try {
 			server = new SimulationLauncherServer(AbstractSimulationBatch.SERVER_PORT);
 		} catch (RemoteException e) {
 			throw new Error("Unable to create server: "+e);
 		}      
 	} 
 	
 	/**
 	 * Starts simulation from specified xml file.
 	 * @param simulationXMLFile
 	 */
 	public static void runSimulation(final String simulationXMLFile) throws IOException { 
 
 		// Start a simulation server process
 		final ActiveSimulation simulation;
 		try {
 			simulation = server.launchSimulation();
 		} catch (IOException e) {
 			throw new Error("Unable to start simulation subprocess: "+e);
 		}
 
 		// Get standard output and error streams and direct them to GUI console tab, so that the buffer is not full, which will cause simulation to stop.
 		ConsoleTabController.appendStreamToConsole("StandardOut", simulation.getStandardOut());
 		ConsoleTabController.appendStreamToConsole("Error/Info/Warning", simulation.getStandardErr());
 
 
 		// Wait for simulation process to be ready to start a new simulation
 		if(!simulation.isReady()) {
 			System.out.println("Waiting for simulation");
 			simulation.waitForReady();
 		}
 		// Start a simulation in the remote process
 		new Thread() {
 			public void run() {
 				try {
 					// Start using an xml file for a robot and a controller (both loaded by simulator process)
 					//simulation.start("samples/atron/car.xml", ussr.samples.atron.simulations.ATRONCarController1.class);
 					//simulation.start(SnakeCarDemo.class);
 					simulation.start(simulationXMLFile);
 					//ATRONSimulation1.class,ATRONCarSimulation
 					//NO ATRONRoleSimulation.class(broken), CommunicationDemo(null),ATRONTestSimulation.class(null),
 					//ConveyorSimulation(null),CrawlerSimulation(null),EightToCarSimulation(broken),SnakeCarDemo.class(null);
 				} catch (RemoteException e) {
 					// Normal or abnormal termination, inspection of remote exception currently needed to determine...
 					System.err.println("Simulation stopped");
 				}
 			}
 		}.start();
 
 
 		// Obtain a reference to remote PhysicsSimulation object (must wait for it to be instantiated remotely)
 		RemotePhysicsSimulation sim = null;
 		while(sim==null) {
 			System.out.println("Simulation still null");
 			sim = simulation.getSimulation();
 			try {
 				Thread.sleep(1000);
 			} catch(InterruptedException exn) {
 				throw new Error("Unexpected interruption");
 			}
 		}
 
 		callBackGUI(sim);

 	}
 	
 	/**
 	 * Sets remote objects of simulation in GUI and adapts it to simulation.
 	 */
 	private static void callBackGUI(RemotePhysicsSimulation remotePhysicsSimulation)throws IOException {
 		remotePhysicsSimulation.setGUICallbackControl(new GUICallbackControlImpl() );
 		GeneralController.setRemotePhysicsSimulation(remotePhysicsSimulation);
 		MainFrameSeparateController.setRendererControl(remotePhysicsSimulation.getRendererControl());
		GeneralController.setBuilderControl(remotePhysicsSimulation.getBuilderControl());
 		MainFrameSeparate.setMainFrameSeparateEnabled(true);
 		SimulationTab.setTabVisible(true);
 	}
 }
