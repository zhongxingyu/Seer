 package com.nxj.application;
 
 import com.nxj.application.listeners.ShutdownListener;
 import com.nxj.annotation.AnnotationProcessor;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.logging.Logger;
 import javassist.Modifier;
 import org.reflections.Reflections;
 
 /**
  * Copyright 2012 Noblexity Advertising
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 /**
  * @author Petr Stuchl4n3k Stuchlik <stuchl4n3k@gmail.com>
  * @author Milan Felix Sulc <rkfelix@gmail.com>
  * 
  * @nxj 0.1
  * @version 1.0
  */
 public class Application {
 
     protected Config config;
     protected List<Controller> controllers;
     protected List<View> views;
     protected List<Module> modules;
     private List<ShutdownListener> shutdownListeners;
     private static Application instance;
     private Logging logging;
     private static final Logger logger = Logger.getLogger(Application.class.getName());
     private Reflections reflections;
     private AnnotationProcessor annotationProcessor;
 
     /**
      * Private constructor for Singleton pattern.
      */
     private Application() {
         controllers = new ArrayList<>();
         views = new ArrayList<>();
         modules = new ArrayList<>();
         shutdownListeners = new ArrayList<>();
         reflections = new Reflections("");
         annotationProcessor = new AnnotationProcessor(this);
         logging = new Logging();
     }
 
     /**
      * Universal Singleton/context getter.
      *
      * @return Application context.
      */
     public static Application getInstance() {
         if (instance == null) {
             instance = new Application();
         }
         return instance;
     }
 
     /**
      * Bootstraps this Application. Bootstrapping initalizes loads all
      * the Modules and instantiates the Controllers.
      */
     public void bootstrap() {
         bootstrapModules();
         bootstrapControllers();
     }
 
     /**
      * Bootstraps this Application. Bootstrapping initalizes Config, loads all
      * the Modules and instantiates the Controllers.
      *
      * @param configPath
      */
     public void bootstrap(String configPath) {
         config = Config.getConfigBundle(configPath);
         bootstrap();
     }
 
     /**
      * Bootstraps this Application. Bootstrapping initalizes Config, loads all
      * the Modules and instantiates the Controllers.
      *
      * @param bundle
      */
     public void bootstrap(ResourceBundle bundle) {
         config = Config.getConfig(bundle);
         bootstrap();
     }
 
     /**
      * View creator method. Makes it possible to use Dependency injection in
      * Views and Annotation processing.
      *
      * @param viewClass View class to create.
      * @return Initialized view object.
      */
     public <V extends View> V createView(Class<? extends View> viewClass) {
         try {
             View view = null;
             view = viewClass.newInstance();
             // After the View has been initialized, process the annotaions.
             annotationProcessor.processAnnotations(view);
 
             // Add the view to the collection.
             views.add(view);
 
             return (V) view;
         } catch (InstantiationException | IllegalAccessException ex) {
         }
         throw new NoClassDefFoundError("Could not find the specified view " + viewClass);
     }
 
     /**
      * Gets a Config instance.
      *
      * @return
      */
     public Config getConfig() {
         return config;
     }
 
     /**
      * Gets a Logging instance.
      * @return 
      */
     public Logging getLogger() {
         return logging;
     }
 
     /**
      * Locates a Controller by given name. If the name argument is e.g. "Main",
      * then this method would look for a class "MainController".
      *
      * @param name Name of the Controller to find.
      * @return Instantiated controller.
      */
     public Controller getController(String name) {
         String className = null;
         for (Controller ctrl : controllers) {
             className = ctrl.getClass().getSimpleName();
             if (className.equalsIgnoreCase(name + "Controller")) {
                 return ctrl;
             }
         }
         throw new NoClassDefFoundError("Controller of name " + name + " could not be found!");
     }
 
     /**
      * Locates a Controller by given class.
      *
      * @param ctrlClass A controller class to find.
      * @return Controller of class ctrlClass.
      */
     public <C extends Controller> C getController(Class<? extends Controller> ctrlClass) {
         for (Controller ctrl : controllers) {
             if (ctrl.getClass() == ctrlClass) {
                 return (C) ctrl;
             }
         }
         throw new NoClassDefFoundError("Could not find the specified controller " + ctrlClass);
     }
 
     /**
      * Returns a list of all active Modules.
      *
      * @return Module list.
      */
     public List<Module> getModules() {
         return modules;
     }
 
     public void shutdown() {
         // Invoke all shutdown listeners
         while (!shutdownListeners.isEmpty()) {
             shutdownListeners.remove(shutdownListeners.size() - 1).onShutdown();
         }
        
        // Flush logger
        logging.flush();
        
        // Hard exit
         System.exit(0);
     }
 
     public void addShutdownListener(ShutdownListener listener) {
         if (!shutdownListeners.contains(listener)) {
             shutdownListeners.add(listener);
         }
     }
 
     public void removeShutdownListener(ShutdownListener listener) {
         shutdownListeners.remove(listener);
     }
 
     /**
      * A helper method which initializes all the Controllers, performs
      * Dependency injection and Annotation processing.
      */
     protected void bootstrapControllers() {
         // Create controller instances.
         Set<Class<? extends Controller>> classes = reflections.getSubTypesOf(Controller.class);
         for (Class<? extends Controller> cls : classes) {
             try {
                 Controller ctrl = cls.newInstance();
                 ctrl.setContext(this);
                 controllers.add(ctrl);
             } catch (InstantiationException | IllegalAccessException ex) {
                 ex.printStackTrace();
             }
         }
 
         // After the controllers have been initialized, process the annotaions.
         annotationProcessor.processAnnotations(controllers);
 
         // Register all controllers as shutdownListeners.
         shutdownListeners.addAll(controllers);
 
         // Finally bootstrap all controllers.
         for (Controller ctrl : controllers) {
             ctrl.onBootstrap();
         }
     }
 
     /**
      * A helper method which initializes all the Modules, performs Dependency
      * injection.
      */
     protected void bootstrapModules() {
         Set<Class<? extends Module>> classes = reflections.getSubTypesOf(Module.class);
         for (Class<? extends Module> cls : classes) {
             // Instantiate only non-abstract Module-subclasses.
             if (!Modifier.isAbstract(cls.getModifiers())) {
                 try {
                     Module mod = cls.newInstance();
                     mod.setContext(this);
                     modules.add(mod);
                 } catch (InstantiationException | IllegalAccessException ex) {
                     ex.printStackTrace();
                 }
             }
         }
     }
 }
