 package ussr.remote;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.net.MalformedURLException;
 import java.rmi.AccessException;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import ussr.builder.Loader;
 import ussr.builder.simulationLoader.SimulationXMLFileLoader;
 import ussr.description.setup.WorldDescription;
 import ussr.model.Controller;
 import ussr.physics.PhysicsFactory;
 import ussr.physics.PhysicsParameters;
 import ussr.physics.PhysicsSimulation;
 import ussr.physics.PhysicsFactory.Options;
 import ussr.remote.facade.ParameterHolder;
 import ussr.remote.facade.RemoteActiveSimulation;
 import ussr.remote.facade.RemotePhysicsSimulation;
 import ussr.remote.facade.RemotePhysicsSimulationImpl;
 import ussr.remote.facade.XMLSimulationProvider;
 import ussr.remote.facade.XMLSimulationProviderInter;
 import ussr.samples.GenericSimulation;
 
 /***
  * Simulation launcher for use with SimulationLauncherServer.  Connects to the server, allowing the
  * server to control how a simulation is started.
  * 
  * (The main class used to launch the simulation for subsequent hookup to and control by the frontend.)
  * @author ups
  */
 public class SimulationClient extends UnicastRemoteObject implements RemoteActiveSimulation {
 
     private PhysicsSimulation simulation;
     private RemotePhysicsSimulation simulationWrapper;
     private SimulationServer server;
     private static ReturnValueHandler returnHandler;
     
     private XMLSimulationProviderInter xmlSimulationProvider;
  
 	public SimulationClient(int portNumber, int idNumber) throws RemoteException {
         connectToServer(portNumber,idNumber);
     }
 
     private void connectToServer(int portNumber, int idNumber) throws RemoteException {
         String locator = "//:"+portNumber+"/"+SimulationServer.SERVER_RMI_ID;
         try {
           server = (SimulationServer)Naming.lookup(locator);
         } catch(NotBoundException exn) {
           throw new Error("Registry on "+locator+" found, but simulation server object was not found");
         } catch(AccessException exn) {
           throw new Error("Access denied when querying "+locator);
         } catch(RemoteException exn) {
           throw new Error("Could not contact registry on "+locator);
         } catch(MalformedURLException exn) {
           throw new Error( "Illegal machine name or simulation server name, machine: " + locator + ", migration server: " + SimulationServer.SERVER_RMI_ID );
         }
         server.register(idNumber, this);
     }
 
     public synchronized RemotePhysicsSimulation getSimulation() throws RemoteException {
         if(simulation==null) return null;
         if(simulation!=null && simulationWrapper==null)
             // simulationWrapper = MagicRemoteWrapper.wrap(RemotePhysicsSimulation.class, simulation);
             simulationWrapper = new RemotePhysicsSimulationImpl(simulation);
         return simulationWrapper;
     }
 
     public synchronized void start(PhysicsParameters parameters, Options options, WorldDescription world) throws RemoteException {
         PhysicsParameters.set(parameters);
         PhysicsFactory.getOptions().set(options);
         simulation = PhysicsFactory.createSimulator();
         simulation.setWorld(world);
     }
 
     public void start(String simulationXML, Set<Class<? extends Controller>> controllers) throws RemoteException {
         List<String> controllerNames = new ArrayList<String>();
         for(Class<? extends Controller> controllerClass: controllers)
             controllerNames.add(controllerClass.getCanonicalName());
         Loader simulation = new Loader(simulationXML, controllerNames);
         this.simulation = Loader.getPhysicsSimulation();
         simulation.start(true);
     }
    
 
 
     /**
      * Starts a remote simulation from XML file.
      * @param simulationXMLFile, the location of xml file.
      * */
 	public void start(String simulationXMLFile) throws RemoteException {  
     	SimulationXMLFileLoader simulationLoader = new SimulationXMLFileLoader(simulationXMLFile);
     	xmlSimulationProvider = new XMLSimulationProvider(simulationLoader);    	
     	this.simulation = SimulationXMLFileLoader.getPhysicsSimulation(); 
     	if (simulation!=null){
         simulationLoader.start(true);
     	}
     }
 	
     
     
     public XMLSimulationProviderInter getXmlSimulationProvider()throws RemoteException {
 		return xmlSimulationProvider;
 	}
    
 
 	public void start(Class<?> mainClass) throws RemoteException {
         this.start(mainClass,null,null);
     }
 
     public void start(Class<?> mainClass, ParameterHolder parameter, ReturnValueHandler handler) throws RemoteException {
         ParameterHolder.set(parameter);
         returnHandler = handler;
         Method[] allMethods = mainClass.getMethods();
         for(int i=0; i<allMethods.length; i++) {
             Method main = allMethods[i]; 
             if(main.getName().equals("main") && Modifier.isStatic(main.getModifiers())) {
                 try {
                 	backgroundWaitAndSetSimulation();
                     main.invoke(null, new Object[] { new String[0] });
                 } catch (IllegalArgumentException e) {
                     throw new Error("Illegal main method declaration: incorrect parameter(s)");
                 } catch (IllegalAccessException e) {
                     throw new Error("Cannot access main method");
                 } catch (InvocationTargetException e) {
                    throw new Error("Main method generated an exception: "+e.getCause());
                 }
                 return;
             }
         }
         throw new Error("No main method found in class");
     }
 
     private void backgroundWaitAndSetSimulation() {
     	new Thread() {
     		public void run() {
     			while(GenericSimulation.getPhysicsSimulation()==null) {
     				synchronized(GenericSimulation.class) {
     					try {
 							GenericSimulation.class.wait();
 						} catch (InterruptedException e) {
 							throw new Error("Unexpected interruption, unable to read simulation ref");
 						}
     				}
     			}
     			simulation = GenericSimulation.getPhysicsSimulation();
     		}
     	}.start();
 	}
 
 	public static ReturnValueHandler getReturnHandler() {
         return returnHandler;
     }
     
     public static void main(String argv[]) throws RemoteException {
     	System.out.println("Starting simulator");
         if(argv.length!=1) throw new Error("Incorrect invocation");
         String spec = argv[0];
         if(!spec.startsWith("@") || spec.indexOf("_")<0) throw new Error("Incorrect port spec");
         String[] parts = spec.substring(1).split("_");
         int portNumber = Integer.parseInt(parts[0]);
         int idNumber = Integer.parseInt(parts[1]);
         new SimulationClient(portNumber,idNumber);
     }
 
     /**
      * Sets simulation window position on the screen.
      * @param x, x position of simulation window.
      * @param y, y position of simulation window.
      */
 	public void setWindowPosition(int x, int y) throws RemoteException {
 		PhysicsFactory.getOptions().setWindowPosition(x,y);
 	}
 
 }
