 package jDistsim.core.simulation.simulator;
 
 import jDistsim.core.simulation.modules.Module;
 import jDistsim.core.simulation.modules.RootModule;
 import jDistsim.core.simulation.animation.ISimulationAnimator;
 import jDistsim.core.simulation.model.ISimulationModelValidator;
 import jDistsim.core.simulation.simulator.entity.Entity;
 import jDistsim.core.simulation.simulator.event.Calendar;
 import jDistsim.core.simulation.simulator.event.EventContainer;
 import jDistsim.core.simulation.simulator.event.ScheduleEvent;
 import jDistsim.core.simulation.exception.EventNotFoundException;
 import jDistsim.core.simulation.exception.ModelNotValidException;
 import jDistsim.core.simulation.exception.SimulatorCoreException;
 import jDistsim.core.simulation.exception.TimeNotSynchronizedException;
 import jDistsim.core.simulation.validator.ValidatorException;
 import jDistsim.core.simulation.validator.ValidatorResult;
 
 import java.io.Serializable;
 import java.util.Date;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 18.2.13
  * Time: 10:38
  */
 public abstract class BaseSimulator implements ISimulator, Serializable {
 
     private double localTime;
     private boolean run;
    private Calendar<ScheduleEvent> calendar;
     private SimulatorEnvironment environment;
     private SimulatorOutput output;
     private ISimulatorEndCondition endCondition;
     private ISimulationModelValidator modelValidator;
     private ISimulationAnimator animator;
 
     public BaseSimulator(ISimulationModelValidator modelValidator) {
         this(modelValidator, new ISimulatorEndCondition() {
             @Override
             public boolean occurred(SimulatorEnvironment environment) {
                 return false;
             }
         });
     }
 
     public BaseSimulator(ISimulationModelValidator modelValidator, ISimulatorEndCondition endCondition) {
         this(modelValidator, 0, endCondition);
     }
 
     public BaseSimulator(ISimulationModelValidator modelValidator, double initialLocalTime, ISimulatorEndCondition endCondition) {
         this.localTime = initialLocalTime;
         this.endCondition = endCondition;
         this.modelValidator = modelValidator;
 
         environment = new SimulatorEnvironment();
         output = new SimulatorOutput();
         calendar = new Calendar<>();
         run = false;
     }
 
     @Override
     public SimulatorEnvironment getEnvironment() {
         return environment;
     }
 
     @Override
     public SimulatorOutput getOutput() {
         return output;
     }
 
     @Override
     public double getLocalTime() {
         return localTime;
     }
 
     @Override
     public void setAnimator(ISimulationAnimator animator) {
         this.animator = animator;
     }
 
     @Override
     public void plan(double time, Module module, Entity entity) {
         if (time < localTime)
             throw new TimeNotSynchronizedException(time, localTime);
 
         ScheduleEvent scheduleEvent = new ScheduleEvent(time, new EventContainer(module, entity));
         calendar.put(scheduleEvent);
     }
 
     protected abstract void classification(Module module);
 
     protected abstract boolean canExecute();
 
     @Override
     public void simulate(ISimulationModel simulationModel) throws SimulatorCoreException {
         try {
             prepareOutput();
             showSimulatorInfo();
             validateModel(simulationModel);
 
             prepare(simulationModel);
            initializeSimulator(simulationModel);
             initializeModules(simulationModel);
 
             output.drawSeparateLine();
             if (simulationModel == null)
                 return;
 
             output.sendToOutput(SimulatorOutput.MessageType.Standard, "Start simulation");
             run = true;
             while (run && !endCondition.occurred(environment)) {
                 while (calendar.isEmpty() || !canExecute()) ;
                 if (!run) break;
 
                ScheduleEvent scheduleEvent = calendar.poll();
                 if (scheduleEvent == null)
                     throw new EventNotFoundException();
 
                 localTime = scheduleEvent.getTime();
 
                 Module module = scheduleEvent.getEventContainer().getModule();
                 Entity entity = scheduleEvent.getEventContainer().getEntity();
 
                 drawCurrentPrecessInfo(module, entity);
                 setEntityState(entity, module);
 
                 classification(module);
                 animate(entity);
                 module.execute(this, entity);
                 updateEnvironment();
             }
 
         } catch (Exception exception) {
             output.drawSeparateLine();
             output.sendToOutput(SimulatorOutput.MessageType.Error, "Error -> " + exception.getClass().getSimpleName());
             output.sendToOutput(SimulatorOutput.MessageType.Error, "Unexpected end of simulation");
             throw new SimulatorCoreException(exception);
         }
         output.drawSeparateLine();
         output.sendToOutput(SimulatorOutput.MessageType.Standard, "End of simulation");
     }
 
     protected abstract void prepare(ISimulationModel simulationModel);
 
     private void animate(Entity entity) {
         if (entity == null) return;
         String moduleFrom = entity.getAttributes().get("previousModule").getValue();
         String moduleTo = entity.getAttributes().get("currentModule").getValue();
         String iconName = entity.getAttributes().get("iconName").getValue();
         animator.animate(moduleFrom, moduleTo, iconName);
     }
 
     private void drawCurrentPrecessInfo(Module module, Entity entity) {
         String entityName = "";
         if (entity != null)
             entityName = " {entity: " + entity.getShortIdentifier() + "}";
         output.sendToOutput(SimulatorOutput.MessageType.Standard, "Local time: " + getLocalTime() + " Process module " + module.getIdentifier() + entityName);
     }
 
     private void updateEnvironment() {
         environment.setLocalTime(getLocalTime());
     }
 
     private void showSimulatorInfo() {
         output.sendToOutput(SimulatorOutput.MessageType.Standard, "Application jDistSim 0.7 by Jirka Penzes");
         output.sendToOutput(SimulatorOutput.MessageType.Standard, "Simulator started on " + new Date());
         output.drawSeparateLine();
         output.sendToOutput(SimulatorOutput.MessageType.Standard, "Preparing simulator ... ");
     }
 
     private void prepareOutput() {
         output.clearOutput();
     }
 
     @Override
     public void stop() {
         run = false;
         output.drawSeparateLine();
         output.sendToOutput(SimulatorOutput.MessageType.Standard, "End of simulation -> explicit stop");
     }
 
     protected void validateModel(ISimulationModel simulationModel) {
         output.sendToOutput(SimulatorOutput.MessageType.Standard, "Validate model");
         ValidatorResult validatorResult = modelValidator.validateModel(simulationModel);
 
         if (validatorResult.isValid()) {
             output.sendToOutput(SimulatorOutput.MessageType.Standard, "Model is valid");
             return;
         }
 
         for (ValidatorException exception : validatorResult.getValidatorExceptions()) {
             output.sendToOutput(SimulatorOutput.MessageType.Error, "Validator error -> " + exception.getOriginator() + " -> " + exception.getMessage());
         }
 
         throw new ModelNotValidException(validatorResult);
     }
 
     protected void initializeSimulator(ISimulationModel simulationModel) {
         output.sendToOutput(SimulatorOutput.MessageType.Standard, "Initialize simulator");
 
         getEnvironment().setModelName(simulationModel.getModelName());
         for (RootModule rootModule : simulationModel.getRootModules()) {
             plan(rootModule.getFirsCreation(), rootModule, null);
         }
     }
 
     private void initializeModules(ISimulationModel simulationModel) {
         output.sendToOutput(SimulatorOutput.MessageType.Standard, "Initialize modules");
         for (Module module : simulationModel.getModules()) {
             output.sendToOutput(SimulatorOutput.MessageType.Standard, "Initialize -> " + module.getIdentifier());
             module.initializeForSimulation(this);
         }
     }
 
     public void setEntityState(Entity entity, Module module) {
         if (entity == null) return;
         String previousModule = entity.getAttributes().get("currentModule").getValue();
         entity.getAttributes().put("previousModule", previousModule);
         entity.getAttributes().put("currentModule", module.getIdentifier());
         // output.sendToOutput(entity.getAttributes().get("previousModule").getValue() + " -> " + entity.getAttributes().get("currentModule").getValue());
     }
 }
