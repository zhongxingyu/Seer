 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package midgard.naming;
 
 import java.util.Hashtable;
 import midgard.componentmodel.IComponent;
 import midgard.repositories.IComponentRepositoryManager;
 import midgard.services.Service;
 
 /**
  *
  * @author fenrrir
  */
 public class DefaultNameService extends Service implements INameService {
 
     private IComponentRepositoryManager repository = null;
 
    public String[] getRequiredInterfaces(){
        return new String [] {"IComponentRepositoryManager"};
    }

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
 
     public IComponent resolveName(String name) {
         return repository.getComponent(name);
     }
 
 }
