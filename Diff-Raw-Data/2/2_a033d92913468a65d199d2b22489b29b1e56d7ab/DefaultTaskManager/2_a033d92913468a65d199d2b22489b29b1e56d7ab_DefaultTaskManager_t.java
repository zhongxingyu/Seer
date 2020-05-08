 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package midgard.tasks;
 
 import java.util.Vector;
 import midgard.app.IAppManager;
 import midgard.app.IAppRepositoryManager;
 import midgard.componentmodel.IComponent;
 import midgard.components.IComponentManager;
 import midgard.repositories.ITaskRepositoryManager;
 import midgard.services.Service;
 
 /**
  *
  * @author fenrrir
  */
 public class DefaultTaskManager extends Service
         implements ITaskManager{
 
     private IComponentManager componentManager;
     private IAppRepositoryManager appRepository;
     private ITaskRepositoryManager repository;
 
 
 
     public String[] getRequiredInterfaces() {
         return new String [] {
             IComponentManager.class.getName(),
            IAppRepositoryManager.class.getName(),
             ITaskRepositoryManager.class.getName()
         };
     }
     
 
 
     public void initialize() {
         super.initialize();
         appRepository = (IAppRepositoryManager) getConnectedComponents()
                 .get(IAppRepositoryManager.class.getName());
         componentManager = (IComponentManager) getConnectedComponents()
                 .get(IComponentManager.class.getName());
         repository = (ITaskRepositoryManager) getConnectedComponents()
                 .get(ITaskRepositoryManager.class.getName());
     }
 
 
     public void startService() {
         super.startService();
 
         Vector tasks = listInstalledTasks();
         String taskName;
 
         for (int i=0; i < tasks.size(); i++){
             taskName = (String)tasks.elementAt(i);
             loadAndInitializeTask(taskName);
         }
     }
 
     public void stopService() {
         super.stopService();
 
         Vector tasks = listInstalledTasks();
         String taskName;
 
         for (int i=0; i < tasks.size(); i++){
             taskName = (String)tasks.elementAt(i);
             destroyTask(taskName);
         }
     }
 
 
     public void destroyTask(String name) {
         IComponent component = componentManager.getComponent(name);
         destroyTask((ITask) component);
     }
 
     public void destroyTask(ITask task) {
         Vector components;
         IComponent component;
 
         if (task.onEvent()){
             components = task.connectAtComponents();
             for (int i=0; i<components.size(); i++){
                 component = componentManager
                         .resolveComponent((String) components.elementAt(i)  );
                 component.removeEventListener(task);
             }
         }
 
         componentManager.destroyComponent((IComponent) task);
     }
 
 
     public Vector listAllTasks() {
         return repository.list();
     }
 
     public Vector listInstalledTasks() {
         return appRepository.listTasks();
     }
 
     public void loadAndInitializeTask(String name) {
         ITask task = getTask(name);
         
         Vector components;
         IComponent component;
 
         if (task.onEvent()){
             components = task.connectAtComponents();
             for (int i=0; i<components.size(); i++){
                 component = componentManager
                         .resolveComponent((String) components.elementAt(i)  );
                 component.registerEventListener(task);
             }
         }
     }
 
     public void pauseTask(String name) {
         IComponent component = componentManager.getComponent(name);
         pauseTask((ITask) component);
     }
 
     public void pauseTask(ITask task) {
         componentManager.pauseComponent((ITask) task);
     }
 
     public void resumeTask(String name) {
         IComponent component = componentManager.getComponent(name);
         resumeTask((ITask) component);
     }
 
     public void resumeTask(ITask task) {
         componentManager.resumeComponent((ITask) task);
     }
 
     public ITask getTask(String name) {
         return (ITask) componentManager.resolveComponent(name);
     }
 
 
 
 }
