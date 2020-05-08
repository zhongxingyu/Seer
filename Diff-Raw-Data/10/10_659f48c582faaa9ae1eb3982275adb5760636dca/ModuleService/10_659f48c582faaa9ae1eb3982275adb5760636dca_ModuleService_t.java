 package org.jtheque.modules.impl;
 
 /*
  * Copyright JTheque (Baptiste Wicht)
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
  */
 
 import org.jtheque.core.able.ICore;
 import org.jtheque.core.utils.SimplePropertiesCache;
 import org.jtheque.core.utils.WeakEventListenerList;
 import org.jtheque.i18n.able.ILanguageService;
 import org.jtheque.images.able.IImageService;
 import org.jtheque.modules.able.IModuleDescription;
 import org.jtheque.modules.able.IModuleLoader;
 import org.jtheque.modules.able.IModuleService;
 import org.jtheque.modules.able.IRepository;
 import org.jtheque.modules.able.Module;
 import org.jtheque.modules.able.ModuleListener;
 import org.jtheque.modules.able.ModuleState;
 import org.jtheque.modules.able.Resources;
 import org.jtheque.modules.able.SwingLoader;
import org.jtheque.modules.impl.ModuleContainer.Builder;
 import org.jtheque.modules.utils.ImageResource;
 import org.jtheque.modules.utils.ModuleResourceCache;
 import org.jtheque.states.able.IStateService;
 import org.jtheque.ui.able.IUIUtils;
 import org.jtheque.update.able.IUpdateService;
 import org.jtheque.utils.StringUtils;
 import org.jtheque.utils.collections.ArrayUtils;
 import org.jtheque.utils.collections.CollectionUtils;
 import org.jtheque.utils.io.CopyException;
 import org.jtheque.utils.io.FileUtils;
 import org.jtheque.utils.ui.SwingUtils;
 
 import org.osgi.framework.BundleException;
 import org.slf4j.LoggerFactory;
 import org.springframework.core.io.UrlResource;
 
 import javax.annotation.PreDestroy;
 import javax.annotation.Resource;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import static org.jtheque.modules.able.ModuleState.*;
 
 /**
  * A module manager implementation. It manage the cycle life of the modules.
  *
  * @author Baptiste Wicht
  */
 public final class ModuleService implements IModuleService {
    private static final Module DUMMY = new Builder().build();

     private final WeakEventListenerList listeners = new WeakEventListenerList();
     private final List<Module> modules = new ArrayList<Module>(10);
     private final Map<String, SwingLoader> loaders = new HashMap<String, SwingLoader>(10);
 
     private final IModuleLoader moduleLoader;
 
     /**
      * The application repository.
      */
     private IRepository repository;
 
     /**
      * The configuration of the module manager. It seems the informations about the modules who're installed or
      * disabled.
      */
     private ModuleConfiguration configuration;
 
     @Resource
     private ICore core;
 
     @Resource
     private IStateService stateService;
 
     @Resource
     private IImageService imageService;
 
     @Resource
     private IUpdateService updateService;
 
     @Resource
     private ILanguageService languageService;
 
     @Resource
     private IUIUtils uiUtils;
 
     /**
      * Indicate if there is a collection module.
      */
     private boolean collectionModule;
 
     /**
      * Construct a new ModuleService.
      *
      * @param moduleLoader The module loader.
      */
     public ModuleService(IModuleLoader moduleLoader) {
         super();
 
         this.moduleLoader = moduleLoader;
 
         Runtime.getRuntime().addShutdownHook(new ModuleStopHook());
     }
 
     @Override
     public void load() {
         SwingUtils.assertNotEDT("load()");
 
         //Load all modules
         modules.addAll(moduleLoader.loadModules());
 
         configuration = stateService.getState(new ModuleConfiguration());
 
         CollectionUtils.filter(modules, new CoreVersionFilter(core, uiUtils));
         CollectionUtils.sort(modules, new ModuleComparator());
 
         for (Module module : modules) {
             //Configuration
             if (configuration.containsModule(module)) {
                 module.setState(configuration.getState(module.getId()));
             } else {
                 module.setState(INSTALLED);
                 configuration.add(module);
             }
 
             //If a collection module must be launched
             if (canBeLoaded(module) && module.isCollection()) {
                 collectionModule = true;
             }
 
             //Indicate the module as installed
             fireModuleInstalled(module);
         }
     }
 
     /**
      * Test if the module can be loaded.
      *
      * @param module The module to test.
      *
      * @return true if the module can be loaded else false.
      */
     private static boolean canBeLoaded(Module module) {
         return module.getState() != DISABLED;
     }
 
     /**
      * Plug the modules.
      */
     @Override
     public void startModules() {
         SwingUtils.assertNotEDT("startModules()");
 
         ModuleStarter starter = new ModuleStarter();
 
         for (Module module : modules) {
             if (canBeLoaded(module) && areAllDependenciesSatisfied(module)) {
                 starter.addModule(module);
             }
         }
 
         starter.startAll();
     }
 
     /**
      * Unplug the modules.
      */
     @Override
     @PreDestroy
     public void stopModules() {
         List<Module> modulesToUnplug = CollectionUtils.copyOf(modules);
 
         CollectionUtils.reverse(modulesToUnplug);
 
         for (Module module : modulesToUnplug) {
             if (module.getState() == STARTED) {
                 stopModule(module);
             }
         }
     }
 
     @Override
     public Collection<Module> getModules() {
         return modules;
     }
 
     @Override
     public Collection<IModuleDescription> getModulesFromRepository() {
         return getRepository().getModules();
     }
 
     @Override
     public IRepository getRepository() {
         if (repository == null) {
             repository = new RepositoryReader().read(core.getApplication().getRepository());
         }
 
         return repository;
     }
 
     @Override
     public void registerSwingLoader(String moduleId, SwingLoader swingLoader) {
         loaders.put(moduleId, swingLoader);
     }
 
     @Override
     public boolean needTwoPhasesLoading(Module module) {
         return module.isCollection() && !SimplePropertiesCache.get("collectionChosen", Boolean.class);
     }
 
     @Override
     public void startModule(Module module) {
         SwingUtils.assertNotEDT("startModule(Module)");
 
         if (module.getState() == STARTED) {
             throw new IllegalStateException("The module is already started. ");
         }
 
         if (needTwoPhasesLoading(module)) {
             throw new IllegalStateException("The module needs a collection");
         }
 
         LoggerFactory.getLogger(getClass()).debug("Start module {}", module.getBundle().getSymbolicName());
 
         //Add images resources
         loadImageResources(module);
 
         try {
             module.getBundle().start();
         } catch (BundleException e) {
             LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
         }
 
         if (loaders.containsKey(module.getId())) {
             loaders.get(module.getId()).afterAll();
             loaders.remove(module.getId());
         }
 
         setState(module, STARTED);
 
         fireModuleStarted(module);
 
         LoggerFactory.getLogger(getClass()).debug("Module {} started", module.getBundle().getSymbolicName());
     }
 
     /**
      * Load the image resources of the module.
      *
      * @param module The module to load the image resources for.
      */
     private void loadImageResources(Module module) {
         for (ImageResource imageResource : module.getResources().getImageResources()) {
             String resource = imageResource.getResource();
 
             if (resource.startsWith("classpath:")) {
                 imageService.registerResource(imageResource.getName(),
                         new UrlResource(module.getBundle().getResource(resource.substring(10))));
             }
         }
     }
 
     @Override
     public void stopModule(Module module) {
         SwingUtils.assertNotEDT("stopModule(Module)");
 
         if (module.getState() != STARTED) {
             throw new IllegalStateException("The module is already started. ");
         }
 
         LoggerFactory.getLogger(getClass()).debug("Stop module {}", module.getBundle().getSymbolicName());
 
         setState(module, INSTALLED);
 
         fireModuleStopped(module);
 
         Resources resources = module.getResources();
 
         if (resources != null) {
             for (ImageResource imageResource : resources.getImageResources()) {
                 imageService.releaseResource(imageResource.getName());
             }
         }
 
         ModuleResourceCache.removeModule(module.getId());
 
         try {
             module.getBundle().stop();
         } catch (BundleException e) {
             LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
         }
 
         LoggerFactory.getLogger(getClass()).debug("Module {} stopped", module.getBundle().getSymbolicName());
     }
 
     @Override
     public void enableModule(Module module) {
         if (module.getState() == DISABLED) {
             setState(module, INSTALLED);
         }
     }
 
     @Override
     public void disableModule(Module module) {
         if (module.getState() == STARTED) {
             stopModule(module);
         }
 
         setState(module, DISABLED);
     }
 
     @Override
     public void installModule(File file) {
         File moduleFile = installModuleFile(file);
 
         if (moduleFile != null) {
             Module module = moduleLoader.installModule(moduleFile);
 
             if (module == null) {
                 FileUtils.delete(moduleFile);
 
                 uiUtils.displayI18nText("error.module.not.installed");
             } else if (exists(module.getId())) {
                 uiUtils.displayI18nText("errors.module.install.already.exists");
             } else {
                 module.setState(INSTALLED);
 
                 modules.add(module);
                 configuration.add(module);
 
                 fireModuleInstalled(module);
 
                 uiUtils.displayI18nText("message.module.installed");
             }
         }
     }
 
     /**
      * Install the module file. It seems copy it into the application directory and make verifications for the existance
      * of the file.
      *
      * @param file The file of the module.
      *
      * @return The file were the module has been installed.
      */
     private File installModuleFile(File file) {
         File target = file;
 
         if (!FileUtils.isFileInDirectory(file, core.getFolders().getModulesFolder())) {
             target = new File(core.getFolders().getModulesFolder(), file.getName());
 
             if (target.exists()) {
                 uiUtils.displayI18nText("errors.module.install.already.exists");
 
                 return null;
             } else {
                 try {
                     FileUtils.copy(file, target);
                 } catch (CopyException e) {
                     LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
 
                     uiUtils.displayI18nText("errors.module.install.copy");
 
                     return null;
                 }
             }
         }
 
         return target;
     }
 
     /**
      * Indicate if a module with the given id exists or not.
      *
      * @param id The id to search for.
      *
      * @return true if a module exists with this id otherwise false.
      */
     private boolean exists(String id) {
         for (Module module : modules) {
             if (id.equals(module.getId())) {
                 return true;
             }
         }
 
         return false;
     }
 
     @Override
     public void install(String url) {
         InstallationResult result = updateService.install(url);
 
         if (result.isInstalled()) {
             Module module = moduleLoader.installModule(new File(core.getFolders().getModulesFolder(), result.getJarFile()));
 
             module.setState(INSTALLED);
 
             modules.add(module);
 
             configuration.add(module);
 
             fireModuleInstalled(module);
 
             uiUtils.displayI18nText("message.module.repository.installed");
         } else {
             uiUtils.displayI18nText("error.repository.module.not.installed");
         }
     }
 
     @Override
     public void uninstallModule(Module module) {
         if (module.getState() == STARTED) {
             stopModule(module);
         }
 
         moduleLoader.uninstallModule(module);
 
         configuration.remove(module);
         modules.remove(module);
 
         try {
             module.getBundle().uninstall();
         } catch (BundleException e) {
             LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
         }
 
         //Delete the bundle file
         FileUtils.delete(StringUtils.delete(module.getBundle().getLocation(), "file:"));
 
         fireModuleUninstalled(module);
 
         for (ModuleListener listener : ModuleResourceCache.getResource(module.getId(), ModuleListener.class)) {
             listeners.remove(ModuleListener.class, listener);
         }
 
         ModuleResourceCache.removeModule(module.getId());
     }
 
     @Override
     public void addModuleListener(String moduleId, ModuleListener listener) {
         listeners.add(ModuleListener.class, listener);
 
         ModuleResourceCache.addResource(moduleId, ModuleListener.class, listener);
     }
 
     @Override
     public String canBeStarted(Module module) {
         if (module.getCoreVersion() != null && module.getCoreVersion().isGreaterThan(ICore.VERSION)) {
             return getMessage("modules.message.versionproblem");
         }
 
         if (!areAllDependenciesSatisfiedAndActive(module)) {
             return getMessage("error.module.not.loaded.dependency");
         }
 
         return "";
     }
 
     @Override
     public String canBeStopped(Module module) {
         if (module.getState() != STARTED) {
             return getMessage("error.module.not.started");
         }
 
         if (isThereIsActiveDependenciesOn(module)) {
             return getMessage("error.module.dependencies");
         }
 
         return "";
     }
 
     @Override
     public String canBeUninstalled(Module module) {
         if (module.getState() == STARTED && isThereIsActiveDependenciesOn(module)) {
             return getMessage("error.module.dependencies");
         }
 
         return "";
     }
 
     @Override
     public String canBeDisabled(Module module) {
         if (module.getState() == DISABLED) {
             return getMessage("error.module.not.enabled");
         } else if (module.getState() == STARTED && isThereIsActiveDependenciesOn(module)) {
             return getMessage("error.module.dependencies");
         }
 
         return "";
     }
 
     /**
      * Test if there is a dependency on the given module.
      *
      * @param module The module to test for dependencies.
      *
      * @return true if there is a dependency on the given module.
      */
     private boolean isThereIsActiveDependenciesOn(Module module) {
         for (Module other : modules) {
             if (other != module && other.getState() == STARTED &&
                     ArrayUtils.contains(other.getDependencies(), module.getId())) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * Return the internationalized message with the given key.
      *
      * @param key      The i18n key.
      * @param replaces The i18n replaces.
      *
      * @return The internationalized message.
      */
     private String getMessage(String key, String... replaces) {
         return languageService.getMessage(key, replaces);
     }
 
     @Override
     public Module getModuleById(String id) {
         Module module = null;
 
         for (Module m : modules) {
             if (id.equals(m.getId())) {
                 module = m;
                 break;
             }
         }
 
         return module;
     }
 
     @Override
     public boolean isInstalled(String module) {
         return getModuleById(module) != null;
     }
 
     @Override
     public boolean hasCollectionModule() {
         return collectionModule;
     }
 
     /**
      * Set the state of a module.
      *
      * @param module The module to set the state.
      * @param state  The state.
      */
     private void setState(Module module, ModuleState state) {
         module.setState(state);
 
         configuration.setState(module.getId(), state);
     }
 
     /**
      * Fire a module started event.
      *
      * @param module The started module.
      */
     private void fireModuleStarted(Module module) {
         for (ModuleListener listener : listeners.getListeners(ModuleListener.class)) {
             listener.moduleStarted(module);
         }
     }
 
     /**
      * Fire a module stopped event.
      *
      * @param module The stopped module.
      */
     private void fireModuleStopped(Module module) {
         for (ModuleListener listener : listeners.getListeners(ModuleListener.class)) {
             listener.moduleStopped(module);
         }
     }
 
     /**
      * Fire a module installed event.
      *
      * @param module The installed module.
      */
     private void fireModuleInstalled(Module module) {
         for (ModuleListener listener : listeners.getListeners(ModuleListener.class)) {
             listener.moduleInstalled(module);
         }
     }
 
     /**
      * Fire a module uninstalled event.
      *
      * @param module The uninstalled module.
      */
     private void fireModuleUninstalled(Module module) {
         for (ModuleListener listener : listeners.getListeners(ModuleListener.class)) {
             listener.moduleUninstalled(module);
         }
     }
 
     /**
      * Indicate if all the dependencies of the module are satisfied.
      *
      * @param module The module to test.
      *
      * @return <code>true</code> if all the dependencies are satisfied else <code>false</code>.
      */
     private boolean areAllDependenciesSatisfied(Module module) {
         if (StringUtils.isEmpty(module.getDependencies())) {
             return true;
         }
 
         for (String dependency : module.getDependencies()) {
             Module resolvedDependency = getModuleById(dependency);
 
             if (resolvedDependency == null || !canBeLoaded(resolvedDependency)) {
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * Indicate if all the dependencies of the given module are satisfied.
      *
      * @param module The module to test.
      *
      * @return true if the all the dependencies of the module are satisfied else false.
      */
     private boolean areAllDependenciesSatisfiedAndActive(Module module) {
         if (StringUtils.isEmpty(module.getDependencies())) {
             return true;
         }
 
         for (String dependencyId : module.getDependencies()) {
             Module dependency = getModuleById(dependencyId);
 
             if (dependency == null || dependency.getState() != STARTED) {
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * A Shutdown Hook to stop modules.
      *
      * @author Baptiste Wicht
      */
     private final class ModuleStopHook extends Thread {
         @Override
         public void run() {
             stopModules();
         }
     }
 
     private class ModuleStarter {
         private final BlockingQueue<Module> ready = new LinkedBlockingQueue<Module>(5);
         private final Set<Module> delay = Collections.synchronizedSet(new HashSet<Module>(150));
         private int threads;
 
         public void addModule(Module module) {
             if (StringUtils.isEmpty(canBeStarted(module))) {
                 ready.add(module);
             } else {
                 delay.add(module);
             }
         }
 
         public void startAll(){
             int size = ready.size() + delay.size();
 
             if(size == 0){
                 return;
             }
 
             threads = Math.min(size, Runtime.getRuntime().availableProcessors());
 
             LoggerFactory.getLogger(getClass()).info("Start {} threads to start the modules", threads);
 
             Collection<Thread> starters = new ArrayList<Thread>(threads);
 
             for(int i = 0; i < threads; i++){
                 Thread thread = new ModuleStarterRunnable(this);
                 thread.setName("ModuleStarter" + i);
                 thread.start();
 
                 starters.add(thread);
             }
 
             joinAll(starters);
         }
 
         private void joinAll(Iterable<Thread> threads) {
             for(Thread thread : threads){
                 try {
                     thread.join();
                 } catch (InterruptedException e) {
                     LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
                 }
             }
         }
 
         public void fireStarted() {
             if(!delay.isEmpty()){
                 for (Iterator<Module> iterator = delay.iterator(); iterator.hasNext(); ) {
                     Module module = iterator.next();
 
                     if (StringUtils.isEmpty(canBeStarted(module))) {
                         try {
                             ready.put(module);
                             iterator.remove();
                         } catch (InterruptedException e) {
                             LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
                         }
                     }
                 }
 
                 if(delay.isEmpty()){
                     for (int i = 0; i < threads; i++) {
                        ready.add(DUMMY);
                     }
                 }
             }
         }
     }
 
     private class ModuleStarterRunnable extends Thread {
         private final ModuleStarter moduleStarter;
 
         private ModuleStarterRunnable(ModuleStarter moduleStarter) {
             super();
 
             this.moduleStarter = moduleStarter;
         }
 
         @Override
         public void run() {
             while(true){
                 if (moduleStarter.ready.size() + moduleStarter.delay.size() == 0) {
                     return;
                 }
 
                 try {
                     Module module = moduleStarter.ready.take();
 
                    if(module == DUMMY){
                         return;
                     }
 
                     startModule(module);
 
                     moduleStarter.fireStarted();
                 } catch (InterruptedException e) {
                     LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
                     return;
                 }
             }
         }
     }
 }
