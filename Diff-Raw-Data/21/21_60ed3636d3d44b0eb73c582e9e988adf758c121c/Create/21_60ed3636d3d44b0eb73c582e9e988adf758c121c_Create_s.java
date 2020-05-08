 package jDistsim.core.simulation.modules.lib.create;
 
 import jDistsim.core.simulation.exception.EntityNotCreatedException;
import jDistsim.core.simulation.modules.ITimeAffectModule;
 import jDistsim.core.simulation.modules.Module;
 import jDistsim.core.simulation.modules.ModuleConfiguration;
 import jDistsim.core.simulation.modules.RootModule;
 import jDistsim.core.simulation.modules.common.ModuleProperty;
 import jDistsim.core.simulation.simulator.ISimulator;
 import jDistsim.core.simulation.simulator.SimulatorOutput;
 import jDistsim.core.simulation.simulator.entity.Attribute;
 import jDistsim.core.simulation.simulator.entity.AttributeCollection;
 import jDistsim.core.simulation.simulator.entity.Entity;
 import jDistsim.ui.module.ModuleView;
 import jDistsim.utils.common.Counter;
 
 import java.util.Random;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 21.2.13
  * Time: 22:36
  */
public class Create extends RootModule implements ITimeAffectModule {
 
     private AttributeCollection initialEntityAttributes;
     private Counter entityCounter;
     private Random random;
 
     public Create(ModuleView view, ModuleConfiguration moduleConfiguration) {
         super(view, moduleConfiguration);
     }
 
     @Override
     protected void initializeDefaultValues() {
         entityCounter = new Counter();
         random = new Random(0);
         baseEntityName = "entity_" + entityCounter.nextValue();
         arrivalsType = TimeBetweenArrivalsType.Constant;
         arrivalsTypeValue = 1;
         maxArrivals = Double.POSITIVE_INFINITY;
         entityPerInterval = 1;
         iconName = "box";
         setFirsCreation(0.0);
 
         initialEntityAttributes = new AttributeCollection();
         initialEntityAttributes.put("creator", getLongIdentifier());
         initialEntityAttributes.put("iconName", iconName);
     }
 
     @Override
     protected void resetStates(ISimulator simulator) {
         entityCounter = new Counter();
     }
 
     @Override
     protected void setChildProperty() {
         getProperties().set(new ModuleProperty("baseEntityName", getBaseEntityName(), "entity name"));
         getProperties().set(new ModuleProperty("arrivalsType", getArrivalsType() + "(" + getArrivalsTypeValue() + ")", "arrivals type"));
         getProperties().set(new ModuleProperty("maxArrivals", getMaxArrivals(), "max arrivals"));
         getProperties().set(new ModuleProperty("entityPerInterval", getEntityPerInterval(), "per interval"));
         getProperties().set(new ModuleProperty("iconName", getIconName(), "entity icon"));
         getProperties().set(new ModuleProperty("firstCreation", getFirsCreation(), "first creation"));
     }
 
     private Entity makeEntity(ISimulator simulator) {
         String entityName = baseEntityName + entityCounter.nextValue();
         Entity entity = new Entity(entityName, getIdentifier(), simulator.getEnvironment().getModelName());
         for (Attribute attribute : initialEntityAttributes) {
             entity.getAttributes().put(attribute);
         }
         entity.getAttributes().put("iconName", getIconName());
         entity.getAttributes().put("creationTime", String.valueOf(simulator.getLocalTime()));
         entity.getAttributes().put("currentModule", getIdentifier());
         return entity;
     }
 
     @Override
     public void logic(ISimulator simulator) {
         double currentTime = simulator.getLocalTime();
 
        if (entityCounter.getCurrentValue() >= maxArrivals) {
            simulator.getOutput().sendToOutput(SimulatorOutput.MessageType.Warning, "Entity not created -> max arrivals -> true");
            return;
        }

         for (Module module : getAllOutputDependencies()) {
             for (int index = 0; index < entityPerInterval; index++) {
                 Entity entity = makeEntity(simulator);
                 simulator.plan(currentTime, module, entity);
             }
         }
 
         try {
             double timeInterval = arrivalsTypeValue;
             if (arrivalsType == TimeBetweenArrivalsType.Random_Expo)
                 timeInterval = Math.round((random.nextDouble() * arrivalsTypeValue + 1) * 100) / 100;
 
             simulator.plan(currentTime + timeInterval, (Module) clone(), null);
         } catch (CloneNotSupportedException exception) {
             throw new EntityNotCreatedException(getIdentifier());
         }
     }

    @Override
    public double getMinimalAffectTime() {
        return arrivalsTypeValue;
    }
 }
