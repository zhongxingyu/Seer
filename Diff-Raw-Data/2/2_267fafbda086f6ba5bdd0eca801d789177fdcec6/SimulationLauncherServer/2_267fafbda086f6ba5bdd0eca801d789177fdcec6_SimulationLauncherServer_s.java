 package ussr.remote;
 
 import java.io.File;
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * Generic class for launching simulator instances.  Instances of this class act
  * as RMI servers that running simulations connect to after launch but before
  * starting.  This provides the means to configure, launch, and control the
  * running simulation.  Remote exceptions etc. are somewhat encapsulated, but
  * remote semantics must still be expected (e.g., call-by-value causes parameters
  * and return values to be copied).
  * 
  * (Server object running on the frontend, allowing simulation processes to connect to the frontend)
  * @author ups
  *
  */
 
 public class SimulationLauncherServer extends UnicastRemoteObject implements SimulationServer {
 
     private static final String WIN_CLIENT_COMMAND = "launchers/ussr-client.bat"; 
     private static final String NIX_CLIENT_COMMAND = "launchers/ussr-client";
 
     /**
      * Counter passed to client simulations used to hook them back up to the corresponding ActiveSimulation object
      */
     private int hookupIDcounter = 0;
     
     /**
      * Currently active simulations launched by this server
      */
     private List<RMIProcessActiveSimulation> activeSimulations = new ArrayList<RMIProcessActiveSimulation>();
 
     /**
      * The port used by this server
      */
     private int port;
     
     /**
      * Create a simulation launcher server that expects RMI name server connections over the specified port
      * @param port the port used for RMI name server connections
      * @throws RemoteException if instantiation of the server failed
      */
     protected SimulationLauncherServer(int port) throws RemoteException {
         super(port);
         this.port = port;
         this.initializeRMIService(port);
     }
 
     public ActiveSimulation launchSimulation() throws IOException {
         String command;
        if(File.pathSeparatorChar=='\\')
             command = WIN_CLIENT_COMMAND;
         else
             command = NIX_CLIENT_COMMAND;
         return this.launchSimulation(command, "");
     }
 
     
     /**
      * Launch a simulation
      * @param command
      * @param arguments
      * @return
      * @throws IOException
      */
     private synchronized ActiveSimulation launchSimulation(String command, String arguments) throws IOException {
         int id = hookupIDcounter++;
         String commandString = command+insertInformation(port,id)+arguments;
         Process process;
         process = Runtime.getRuntime().exec(commandString);
         RMIProcessActiveSimulation simulation = new RMIProcessActiveSimulation(id,process);
         activeSimulations.add(simulation);
         return simulation;
     }
 
     /**
      * Format the unique port number and integer ID used to associate the launched simulation
      * with the corresponding ActiveSimulation object 
      * @param id
      * @return
      */
     private String insertInformation(int port, int id) {
         return " @"+port+"_"+id+" ";
     }
 
     public void register(int id, RemoteActiveSimulation remoteSimulation) {
         for(RMIProcessActiveSimulation simulation: activeSimulations)
             if(simulation.getID()==id) {
                 simulation.setRemoteSimulation(remoteSimulation);
                 return;
             }
         throw new Error("Internal error in simulation server: unique ID "+id+" not recognized");
     }
 
     public void initializeRMIService(int portNumber) {
         Registry registry;
         // Create registry
         try {
           registry = LocateRegistry.createRegistry(portNumber);
         } catch(RemoteException exn) {
           throw new Error("Could not create registry on port " + portNumber);
         }
         // Bind self using new registry
         try {
           registry.bind(SimulationServer.SERVER_RMI_ID, this);
         } catch(java.rmi.ConnectException exn) {
           throw new Error("Could not connect to local registry: " + exn);
         } catch(Exception exn) {
           throw new Error("Unknown error - could not create server: " + exn);
         }
     }
 
 }
