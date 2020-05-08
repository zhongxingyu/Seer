 package com.wideplay.warp.internal;
 
 import com.google.inject.Module;
 import com.wideplay.warp.Warp;
 
 import java.util.LinkedList;
 import java.util.List;
import java.util.Collections;
 
 /**
  * Created with IntelliJ IDEA.
  * On: Mar 24, 2007 10:50:28 PM
  *
  * @author Dhanji R. Prasanna
  */
 class WarpConfigurerImpl implements Warp {
     private final List<Module> guiceModules = new LinkedList<Module>();
 
    public WarpConfigurerImpl(Module... modules) {
        Collections.addAll(guiceModules, modules);
     }
 
     public void install(Module guiceModule) {
         guiceModules.add(guiceModule);
     }
 
 
     public List<Module> getGuiceModules() {
         return guiceModules;
     }
 }
