 package jDistsim.core.modules.factory;
 
 import jDistsim.core.modules.IModuleFactory;
 import jDistsim.core.modules.Module;
 import jDistsim.core.modules.ModuleConfiguration;
 import jDistsim.core.modules.ModuleConnectedPoint;
 import jDistsim.ui.module.moduleView.DelayModuleView;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 1.2.13
  * Time: 21:42
  */
 public class DelayModuleFactory implements IModuleFactory {
 
     private int currentNumber;
     private ModuleConfiguration moduleConfiguration;
 
     public DelayModuleFactory(ModuleConfiguration moduleConfiguration) {
         this.moduleConfiguration = moduleConfiguration;
     }
 
     public String createIdentifier() {
         return "delay" + ++currentNumber;
     }
 
     @Override
     public Module create() {
        Module module = new Module(new DelayModuleView(), moduleConfiguration, false);
        module.addOutputPoint(new ModuleConnectedPoint(1));
        module.addInputPoint(new ModuleConnectedPoint(Integer.MAX_VALUE));
         return module;
     }
 }
