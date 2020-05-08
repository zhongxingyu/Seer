 package jDistsim.core.simulation.modules.lib.sender;
 
 import jDistsim.core.simulation.distributed.DistributedModule;
 import jDistsim.core.simulation.modules.ModuleConfiguration;
 import jDistsim.core.simulation.simulator.ISimulator;
 import jDistsim.core.simulation.simulator.entity.Entity;
 import jDistsim.ui.module.ModuleView;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 1.3.13
  * Time: 20:27
  */
 public class Sender extends DistributedModule {
 

     public Sender(ModuleView view, ModuleConfiguration moduleConfiguration) {
         super(view, moduleConfiguration);
     }
 
     @Override
     protected void initializeDefaultValues() {
     }
 
     @Override
     protected void resetStates(ISimulator simulator) {
     }
 
     @Override
     protected void logic(ISimulator simulator, Entity entity) {
     }
 
     @Override
     protected void setChildProperty() {
        /*
        getProperties().set(new ModuleProperty("address", modelDefinition.getAddress(), "d.address"));
        getProperties().set(new ModuleProperty("rmiModelName", modelDefinition.getRmiModelName(), "d.rmi-name"));
        getProperties().set(new ModuleProperty("port", modelDefinition.getPort(), "d.port"));
        getProperties().set(new ModuleProperty("modelName", modelDefinition.getModelName(), "d.name"));
        getProperties().set(new ModuleProperty("lookahead", modelDefinition.isLookahead(), "d.lookahead"));
        */
     }
 }
