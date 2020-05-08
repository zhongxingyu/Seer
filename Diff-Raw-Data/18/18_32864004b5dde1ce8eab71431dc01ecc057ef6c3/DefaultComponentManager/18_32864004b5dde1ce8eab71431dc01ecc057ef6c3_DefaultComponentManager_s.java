 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package midgard.components;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 import midgard.componentmodel.IComponent;
 import midgard.componentmodel.IProxyComponent;
 import midgard.repositories.IComponentRepositoryManager;
 import midgard.services.Service;
 
 /**
  *
  * @author fenrrir
  */
 public class DefaultComponentManager extends Service implements IComponentManager{
 
    private String [] requiredInterfaces = {"ComponentRepositoryManager"};
     private IComponentRepositoryManager repository = null;
 
 
     public void setConfigurationParameter(String name, Object value){
         if (name.equals("IComponentRepositoryManager")){
             repository = (IComponentRepositoryManager) value;
         }
     }
 
     public void setConfigurationParameters(Hashtable params){
         super.setConfigurationParameters(params);
         if (params.containsKey("IComponentRepositoryManager")){
             setConfigurationParameter("IComponentRepositoryManager",
                                       params.get("IComponentRepositoryManager"));
         }
         
     }
 
     public void changeImplementation(IProxyComponent proxy, IComponent old, IComponent comp) {
     }
 
     public void changeImplementation(IProxyComponent proxy, IComponent comp) {
     }
 
     public void destroyComponent(IComponent component) {
     }
 
     public void initializeComponent(IComponent component) {
     }
 
     public void loadComponent(IComponent component) {
     }
 
     public void pauseComponent(IComponent component) {
     }
 
     public void resumeComponent(IComponent component) {
     }
 
     public Vector getInterfaceNames() {
         return repository.getInterfaceNames();
     }
 
     public IComponent getImplementationOfInterface(String name) {
         return repository.getImplementationOfInterface(name);
     }
 
     public Vector getComponentsFromInterface() {
         return repository.getComponentsFromInterface();
     }
 
     public Vector getComponentNames() {
         return repository.getComponentNames();
     }
 
     public IComponent getComponent(String name) {
         return repository.getComponent(name);
     }
     
 
 
 }
