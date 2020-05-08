 package polly.moduleloader;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import polly.moduleloader.annotations.None;
 import polly.moduleloader.annotations.Provide;
 import polly.moduleloader.annotations.Require;
 
 
 public class DefaultModuleLoader implements ModuleLoader {
 
     private static Logger logger = Logger.getLogger(DefaultModuleLoader.class
         .getName());
 
     private Map<Class<?>, Provider> setupProvides;
     private Map<Integer, Provider> providedStates;;
 
     private Map<Provider, Set<Class<?>>> beforeSetupReq;
     private Map<Provider, Set<Integer>> requiredStates;
     private Map<Class<?>, Object> provides;
     private Set<Provider> providers;
     private Set<Integer> state;
     private Provider startUp;
 
 
 
     public DefaultModuleLoader() {
         this.setupProvides = new HashMap<Class<?>, Provider>();
         this.providedStates = new HashMap<Integer, Provider>();
         this.beforeSetupReq = new HashMap<Provider, Set<Class<?>>>();
         this.requiredStates = new HashMap<Provider, Set<Integer>>();
         this.provides = new HashMap<Class<?>, Object>();
         this.providers = new HashSet<Provider>();
         this.state = new HashSet<Integer>();
     }
     
     
     
     public void exportToDot(File output) throws IOException {
         PrintWriter w = null;
         try {
             w = new PrintWriter(new FileWriter(output));
             
             w.println("digraph modules {");
             if (this.startUp != null) {
                 w.println("    node [shape=ellipse fillcolor=cadetblue color=black style=\"filled,solid\"] " + 
                         this.startUp.getName());
             }
             w.println("     node [shape=ellipse fillcolor=lightblue2 color=black style=\"filled,solid\"]");
             for (Provider provider : this.providers) {
                 if (provider != this.startUp) {
                     w.println("    " + provider.getName() + ";");
                 }
             }
             
             w.println();
             w.println("    node [shape=ellipse fillcolor=azure2 color=azure4 style=\"dashed,filled\"];");
             for (Entry<Class<?>, Provider> e : this.setupProvides.entrySet()) {
                 w.println("    " + e.getKey().getSimpleName());
             }
             
             for (Entry<Provider, Set<Class<?>>> e : this.beforeSetupReq.entrySet()) {
                 for (Class<?> cls : e.getValue()) {
                     w.println("    " + cls.getSimpleName() + "->" + e.getKey().getName());
                 }
             }
             
             for (Entry<Class<?>, Provider> e : this.setupProvides.entrySet()) {
                 w.println("    " + e.getValue().getName() + "->" + 
                         e.getKey().getSimpleName());
             }
             
             w.println("}");
             w.flush();
         } finally {
             if (w != null) {
                 w.close();
             }
         }
     }
 
 
 
     private void processModule(Provider provider) {
         Class<?> cls = provider.getClass();
 
         polly.moduleloader.annotations.Module an = cls.getAnnotation(
         		polly.moduleloader.annotations.Module.class);
         
         if (an == null) {
             throw new ModuleDependencyException("module " + provider
                 + " is not annotated");
         }
         
         if (an.startUp()) {
             if (this.startUp != null) {
                 throw new ModuleDependencyException("there is already a startup module: " 
                         + this.startUp);
             }
             this.startUp = provider;
         }
 
         for (Provide p : an.provides()) {
             if (p.component() != None.class) {
                 this.willProvideDuringSetup(p.component(), provider);
             }
             if (p.state() >= 0) {
                 this.willSetState(p.state(), provider);
             }
         }
 
         for (Require r : an.requires()) {
             if (r.component() != None.class) {
                 this.requireBeforeSetup(r.component(), provider);
             }
             if (r.state() >= 0) {
                 this.requireState(r.state(), provider);
             }
         }
     }
 
 
 
     @Override
     public <T> void willProvideDuringSetup(Class<T> component, Provider provider) {
         Provider m = this.setupProvides.get(component);
         if (m != null) {
             throw new ModuleDependencyException("Module '" + provider
                 + "' cannot provide component '" + component
                 + "' because it is already provided by module '" + m + "'");
         }
 
         Set<Class<?>> requires = this.beforeSetupReq.get(provider);
         if (requires != null && requires.contains(component)) {
             throw new ModuleDependencyException("Module '" + provider + ""
                 + "' cannot provide '" + component
                 + "' because it already requires it.");
         }
 
         this.setupProvides.put(component, provider);
     }
 
 
 
     @Override
     public <T> void requireBeforeSetup(Class<?> component, Provider provider) {
         Provider mod = this.setupProvides.get(component);
         if (mod == provider) {
             throw new ModuleDependencyException("Module '" + provider
                 + "' cannot require '" + component
                 + "' because it already provides it.");
         }
 
         // TODO: check cyclic dependency
 
         Set<Class<?>> set = this.beforeSetupReq.get(provider);
         if (set == null) {
             set = new HashSet<Class<?>>();
             this.beforeSetupReq.put(provider, set);
         }
         set.add(component);
     }
     
     
     
     @Override
     public boolean checkRequires(Class<?> component, Provider provider) {
         return this.beforeSetupReq.get(provider).contains(component);
     }
 
 
 
     @Override
     public void provideComponent(Object component) {
         this.provideComponentAs(component.getClass(), component);
     }
 
 
 
     @Override
     public void provideComponentAs(Class<?> type, Object component) {
         if (type == null) {
             throw new ModuleDependencyException("Provided type cannot be null");
         } else if (component == null) {
             throw new ModuleDependencyException("Provided component for '"
                 + type + "' cannot be null");
         }
         this.provides.put(type, component);
     }
 
 
 
     @Override
     public void registerModule(Provider provider) {
         this.processModule(provider);
         this.providers.add(provider);
     }
 
 
 
     @Override
     public <T> T requireNow(Class<T> component) {
         Object comp = this.provides.get(component);
         if (comp != null) {
             return component.cast(comp);
         } else {
             throw new IllegalArgumentException("component '" + component
                 + "' not provided");
         }
     }
 
 
 
     private boolean isProvided(Class<?> component) {
         return this.provides.get(component) != null;
     }
 
 
 
     @Override
     public void runSetup() throws SetupException {
         if (this.startUp != null) {
             this.runModuleSetup(this.startUp, new HashSet<Provider>());
         }
         for (Provider provider : this.providers) {
             this.runModuleSetup(provider, new HashSet<Provider>());
         }
     }
 
 
 
     private void runModuleSetup(Provider provider, Set<Provider> callSet)
             throws SetupException {
         if (provider.isSetup()) {
             return;
         }
 
         callSet.add(provider);
         Set<Class<?>> required = this.beforeSetupReq.get(provider);
         if (required != null) {
 
             logger.trace("Resolving " + required.size()
                 + " requirements for module '" + provider + "': ");
 
             for (Class<?> component : required) {
 
                 if (this.isProvided(component)) {
                     continue;
                 }
 
                 // find module that provides this component
                 Provider mod = this.setupProvides.get(component);
                 if (mod == null) {
                     throw new ModuleDependencyException(
                         "invalid dependency. no module provides '" + component
                             + "' during setup");
                 } else if (callSet.contains(mod)) {
                     throw new ModuleDependencyException(
                         "invalid cyclic dependency between module '" + mod
                             + "' and '" + provider + "'");
                 } else if (mod != provider) {
                     this.runModuleSetup(mod, callSet);
                 }
             }
         }
        logger.info("Running setup for '" + provider + "'...");
         provider.setupModule();
        logger.trace("Success");
         callSet.remove(provider);
 
         // check if all components that 'module' claimed to provide are
         // actually provided now
         for (Entry<Class<?>, Provider> entry : this.setupProvides.entrySet()) {
             if (entry.getValue() == provider && !this.isProvided(entry.getKey())) {
                 throw new ModuleDependencyException("Module '" + provider
                     + "' claimed to provide '" + entry.getKey()
                     + "' but did not");
             }
         }
     }
 
 
 
     @Override
     public boolean isStateSet(int state) {
         return this.state.contains(state);
     }
 
 
 
     @Override
     public void addState(int state) {
         this.state.add(state);
     }
 
 
 
     @Override
     public void requireState(int state, Provider provider) {
         Provider mod = this.providedStates.get(state);
         if (mod == provider) {
             throw new ModuleDependencyException("Module '" + provider
                 + "' cannot require state '" + state
                 + "' because it already provides it.");
         }
 
         Set<Integer> set = this.requiredStates.get(provider);
         if (set == null) {
             set = new HashSet<Integer>();
             this.requiredStates.put(provider, set);
         }
         set.add(state);
     }
 
 
 
     @Override
     public void willSetState(int state, Provider provider) {
         Provider m = this.providedStates.get(state);
         if (m != null) {
             throw new ModuleDependencyException("State '" + state
                 + "' already provided by module '" + m + "'");
         }
 
         Set<Integer> requires = this.requiredStates.get(provider);
         if (requires != null && requires.contains(state)) {
             throw new ModuleDependencyException("Module '" + provider + ""
                 + "' cannot provide state '" + state
                 + "' because it already requires it.");
         }
         this.providedStates.put(state, provider);
     }
 
 
 
     @Override
     public void runModules() throws Exception {
         for (Provider provider : this.providers) {
             this.runModule(provider, new HashSet<Provider>());
         }
     }
 
 
 
     private void runModule(Provider provider, Set<Provider> callSet) throws Exception {
         if (provider.isRun()) {
             return;
         }
 
         callSet.add(provider);
         Set<Integer> required = this.requiredStates.get(provider);
         if (required != null) {
 
             logger.trace("Requiring " + required.size()
                 + " states for module '" + provider + "': ");
 
             for (Integer state : required) {
 
                 if (this.isStateSet(state)) {
                     continue;
                 }
 
                 // find module that provides this component
                 Provider mod = this.providedStates.get(state);
                 if (mod == null) {
                     throw new ModuleDependencyException(
                         "invalid dependency. no module provides state '"
                             + state + "' during run");
                 } else if (callSet.contains(mod)) {
                     throw new ModuleDependencyException(
                         "invalid cyclic dependency between module '" + mod
                             + "' and '" + provider + "'");
                 } else if (mod != provider) {
                     this.runModule(mod, callSet);
                 }
             }
         }
        logger.info("Running Module for '" + provider + "'");
         provider.runModule();
        logger.trace("Success");
        callSet.remove(provider);
        
         // check if all states that 'module' claimed to provide are
         // actually provided now
         for (Entry<Integer, Provider> entry : this.providedStates.entrySet()) {
             if (entry.getValue() == provider && !this.isStateSet(entry.getKey())) {
                 throw new ModuleDependencyException("Module '" + provider
                     + "' claimed to provide state '" + entry.getKey()
                     + "' but did not");
             }
         }
     }
 
     
     
     public void dispose() {
         for (Provider mod : this.providers) {
             mod.dispose();
         }
         if (this.startUp != null) {
             this.startUp.dispose();
         }
         this.beforeSetupReq.clear();
         this.providers.clear();
         this.providedStates.clear();
         this.provides.clear();
         this.requiredStates.clear();
         this.setupProvides.clear();
         this.state.clear();
     }
 }
