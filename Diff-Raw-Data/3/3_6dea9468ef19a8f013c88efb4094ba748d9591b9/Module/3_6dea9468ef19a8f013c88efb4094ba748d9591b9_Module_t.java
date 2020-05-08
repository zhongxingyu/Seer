 package jDistsim.core.simulation.modules;
 
 import jDistsim.application.designer.common.Application;
 import jDistsim.core.simulation.modules.common.ModuleProperties;
 import jDistsim.core.simulation.modules.common.ModuleProperty;
 import jDistsim.core.simulation.simulator.ISimulator;
 import jDistsim.core.simulation.simulator.entity.Entity;
 import jDistsim.ui.module.ModuleView;
 import jDistsim.utils.collection.ReadOnlyList;
 import jDistsim.utils.collection.observable.ObservableList;
 import jDistsim.utils.logging.Logger;
 import jDistsim.utils.pattern.observer.IObserver;
 import jDistsim.utils.pattern.observer.Observable;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * Author: Jirka Pénzeš
  * Date: 24.11.12
  * Time: 12:16
  */
 public abstract class Module extends Observable implements IObserver, Cloneable {
 
     private String identifier;
     private final ModuleView view;
     private final ObservableList<ModuleConnectedPoint> inputConnectedPoints;
     private final ObservableList<ModuleConnectedPoint> outputConnectedPoints;
     private final ModuleProperties properties;
 
     public Module(ModuleView view, ModuleConfiguration moduleConfiguration) {
         this.identifier = moduleConfiguration.getBaseIdentifier();
         this.view = view;
         this.properties = new ModuleProperties(this);
 
         inputConnectedPoints = new ObservableList<>(this);
         outputConnectedPoints = new ObservableList<>(this);
 
         initializeDefaultValues();
        initialize();
     }
 
     protected abstract void initializeDefaultValues();
 
     private void initialize() {
         properties.set(new ModuleProperty("identifier", getIdentifier(), "identifier"));
         properties.set(new ModuleProperty("correct", false, "correct"));
 
         setInputPointsProperties();
         setOutputPointsProperties();
         setChildProperty();
     }
 
     public void initializeForSimulation(ISimulator simulator) {
         resetBaseStates(simulator);
         resetStates(simulator);
     }
 
     protected abstract void resetStates(ISimulator simulator);
 
     private void resetBaseStates(ISimulator simulator) {
 
     }
 
     public void execute(ISimulator simulator, Entity entity) {
         preExecute(simulator, entity);
         logic(simulator, entity);
     }
 
     protected abstract void logic(ISimulator simulator, Entity entity);
 
     protected void preExecute(ISimulator simulator, Entity entity) {
         entity.getAttributes().put("modelLifeCycle", "->" + getIdentifier());
         entity.getAttributes().put("distributedLifeCycle", "->" + getLongIdentifier());
     }
 
 
     public void setIdentifier(String identifier) {
         this.identifier = identifier;
         properties.get("identifier").setValue(identifier);
         //notifyObservers("identifier");
     }
 
     public String getIdentifier() {
         return identifier;
     }
 
     public String getLongIdentifier() {
         return Application.global().getModelName() + "." + getIdentifier();
     }
 
     public ModuleView getView() {
         return view;
     }
 
     public ModuleProperties getProperties() {
         return properties;
     }
 
     public Iterable<Module> getAllOutputDependencies() {
         List<Module> allDependencies = new ArrayList<>();
         for (ModuleConnectedPoint connectedPoint : getOutputConnectedPoints()) {
             for (Module module : connectedPoint.getDependencies()) {
                 allDependencies.add(module);
             }
         }
         return allDependencies;
     }
 
     public void addInputPoint(ModuleConnectedPoint moduleConnectedPoint) {
         inputConnectedPoints.add(moduleConnectedPoint);
     }
 
     public void addOutputPoint(ModuleConnectedPoint moduleConnectedPoint) {
         outputConnectedPoints.add(moduleConnectedPoint);
     }
 
     public ReadOnlyList<ModuleConnectedPoint> getInputConnectedPoints() {
         return inputConnectedPoints;
     }
 
     public ReadOnlyList<ModuleConnectedPoint> getOutputConnectedPoints() {
         return outputConnectedPoints;
     }
 
     public boolean canInputConnected() {
         for (ModuleConnectedPoint moduleConnectedPoint : inputConnectedPoints) {
             if (moduleConnectedPoint.canBeConnected())
                 return true;
         }
         return false;
     }
 
     public boolean canOutputConnected() {
         for (ModuleConnectedPoint moduleConnectedPoint : outputConnectedPoints) {
             if (moduleConnectedPoint.canBeConnected())
                 return true;
         }
         return false;
     }
 
     public boolean isCreateModule() {
         return this instanceof RootModule;
     }
 
     @Override
     public void update(Observable observable, Object arguments) {
         Logger.log("module -> update");
         if (observable == inputConnectedPoints)
             setInputPointsProperties();
 
         if (observable == outputConnectedPoints)
             setOutputPointsProperties();
 
         Logger.log("module property changed");
         notifyObservers("propertyChanged");
     }
 
     private void setOutputPointsProperties() {
         Logger.log("set output points");
         properties.set(new ModuleProperty("outputPoints.size", outputConnectedPoints.size(), "count of output points"));
         for (int index = 0; index < outputConnectedPoints.size(); index++) {
             ModuleConnectedPoint connectedPoint = outputConnectedPoints.get(index);
 
             String description = "output point " + (index + 1);
             String pointName = "outputPoint_" + (index + 1);
 
             properties.set(new ModuleProperty(pointName + ".maxCapacity", connectedPoint.getCapacity(), description + " max capacity"));
             properties.set(new ModuleProperty(pointName + ".isFull", connectedPoint.isFull(), description + " is full"));
             properties.set(new ModuleProperty(pointName + ".occupied", connectedPoint.getDependencies().size(), description + " occupied"));
         }
     }
 
     private void setInputPointsProperties() {
         Logger.log("set input points");
         properties.set(new ModuleProperty("inputPoints.size", inputConnectedPoints.size(), "count of input points"));
         for (int index = 0; index < inputConnectedPoints.size(); index++) {
             ModuleConnectedPoint connectedPoint = inputConnectedPoints.get(index);
 
             String description = "input point " + (index + 1);
             String pointName = "inputPoint_" + (index + 1);
 
             properties.set(new ModuleProperty(pointName + ".maxCapacity", connectedPoint.getCapacity(), description + " max capacity"));
             properties.set(new ModuleProperty(pointName + ".isFull", connectedPoint.isFull(), description + " is full"));
             properties.set(new ModuleProperty(pointName + ".occupied", connectedPoint.getDependencies().size(), description + " occupied"));
         }
     }
 
     public void refreshProperties() {
         properties.set(new ModuleProperty("correct", isValid(), "correct"));
         setInputPointsProperties();
         setOutputPointsProperties();
         setChildProperty();
     }
 
     protected abstract void setChildProperty();
 
     public boolean isValid() {
         for (ModuleConnectedPoint connectedPoint : getInputConnectedPoints()) {
             if (connectedPoint.getDependencies().size() == 0)
                 return false;
         }
         for (ModuleConnectedPoint connectedPoint : getOutputConnectedPoints()) {
             if (connectedPoint.getDependencies().size() == 0)
                 return false;
         }
         return true;
     }
 
     @Override
     protected Object clone() throws CloneNotSupportedException {
         try {
             final Module result = (Module) super.clone();
             return result;
         } catch (final CloneNotSupportedException ex) {
             throw new AssertionError();
         }
     }
 }
