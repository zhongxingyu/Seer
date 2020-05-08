 package be.kuleuven.cs.gridlock.examples;
 
 import be.kuleuven.cs.gridlock.configuration.Configuration;
 import be.kuleuven.cs.gridlock.configuration.FileConfigurationHelper;
 import be.kuleuven.cs.gridlock.coordination.VehicleCoordination;
 import be.kuleuven.cs.gridlock.simulation.SimulationContext;
 
 /**
  * This is an example on how to use GridLock with predefined routes
  *
  * @author Rutger Claes <rutger.claes@cs.kuleuven.be>
  */
 public class MatLabExample {

     public static void main( String[] args ) {
         MatLabExample exampleSimulation = new MatLabExample();
         exampleSimulation.setup();
         exampleSimulation.run();
     }
 
     private SimulationContext context;
 
     private void setup() {
         // First we load the configuration.  The FileConfigurationHelper will load
         // the configuration file from the resources folder.
         Configuration configuration = FileConfigurationHelper.load( MatLabExample.class.getResource( "/matlab/configuration.properties" ) );
 
         // Once we have the configuration, we can use it to construct a simulation
         // context
         this.context = SimulationContext.loadSimulationContext( configuration );
 
         // Once the context is created, components can be added to the simulation
         // Here we only add a vehicle coordination component.  The type of coordination
         // is determined in the VehicleCoordination component based on the configuration.
         // When the component is added to the context, it will have access to the
         // configuration file
         this.context.addSimulationComponent( new VehicleCoordination() );
     }
 
     private void run() {
         this.context.getSimulation().run();
     }
 }
