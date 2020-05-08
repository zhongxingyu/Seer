 package jDistsim.demo;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 26.12.12
  * Time: 23:40
  */
 public class PrintModule extends Module {
 
 
     public PrintModule(String identifier) {
         super(identifier);
         createDependencyPoint();
     }
 
     @Override
     public void execute(Simulator simulator, Entity entity) {
         System.out.println("tick on " + getIdentifier() + ": " + simulator.getLocalTime() + " -> " + entity.getIdentifier() + " " + entity.toString());
        //System.out.println(entity.get("order"));
 
         double currentTime = simulator.getLocalTime();
         for (Module module : getAllDependencies()) {
             simulator.plan(currentTime, module, entity);
         }
     }
 
 
 }
