 /*
 * Copyright (C) 2011 Rodrigo Pinheiro Marques de Araujo
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
 
 package midgard.adaptation;
 
 import java.util.Hashtable;
 import java.util.Vector;
 import midgard.componentmodel.Component;
 import midgard.componentmodel.IComponent;
 import midgard.components.IComponentManager;
 import midgard.components.IComponentProfileManager;
 import midgard.events.IEvent;
 import midgard.events.IEventManager;
 import midgard.kernel.ClassLoader;
 import midgard.repositories.IAdaptationProfileRepositoryManager;
 import midgard.services.IService;
 import midgard.tasks.ITaskManager;
 
 /**
  *
  * @author fenrrir
  */
 public class DefaultAdaptationProfileManager extends Component
         implements IAdaptationProfileManager {
     
     private IAdaptationProfileRepositoryManager repository;
     private IComponentManager componentManager;
     private ITaskManager taskManager;
     private IComponentProfileManager componentProfileManager;
     private IEventManager eventManager;
     private Hashtable cache;
 
     public String[] getRequiredInterfaces() {
         return new String [] {
             IAdaptationProfileRepositoryManager.class.getName(),
             IComponentManager.class.getName(),
             ITaskManager.class.getName(),
             IComponentProfileManager.class.getName(),
             IEventManager.class.getName()
         };
     }
 
 
     public void initialize(){
         super.initialize();
 
         repository = (IAdaptationProfileRepositoryManager) getConnectedComponents()
                 .get(IAdaptationProfileRepositoryManager.class.getName());
        repository.open();
         componentManager = (IComponentManager) getConnectedComponents()
                 .get(IComponentManager.class.getName());
         taskManager = (ITaskManager)getConnectedComponents()
                 .get(ITaskManager.class.getName());
         componentProfileManager = (IComponentProfileManager) getConnectedComponents()
                 .get(IComponentProfileManager.class.getName());
         eventManager = (IEventManager) getConnectedComponents()
                 .get(IEventManager.class.getName());
 
         cache = new Hashtable();
     }
 
     public IAdaptationProfile getProfile(String name) {
         if (!cache.containsKey(name)){
             IAdaptationProfile profile  = repository.getProfile(name);
 
             IComponent component;
             String componentName;
             Vector components;
             
             components = profile.connectToComponents();
 
             for (int i=0; i < components.size(); i++){
                 componentName = (String) components.elementAt(i);
                 component = componentManager.resolveComponent(componentName);
                 component.registerEventListener(profile);
             }
 
             components = profile.connectComponentsToMe();
 
             for (int i=0; i < components.size(); i++){
                 componentName = (String) components.elementAt(i);
                 component = componentManager.resolveComponent(componentName);
                 profile.registerEventListener(component);
             }
 
 
             profile.setHelper(this);
             cache.put(name, profile);
             return profile;
         }
         return (IAdaptationProfile) cache.get(name);
 
     }
 
     public Vector listProfiles() {
         return repository.list();
     }
 
     public void loadProfile(String name) {
         IAdaptationProfile profile = getProfile(name);
     }
 
     public void unloadProfile(String name) {
         if (cache.containsKey(name)){
             IAdaptationProfile profile = (IAdaptationProfile) cache.get(name);
             profile.clear();
 
             IComponent component;
             String componentName;
             Vector components;
 
             components = profile.connectToComponents();
 
             for (int i=0; i < components.size(); i++){
                 componentName = (String) components.elementAt(i);
                 component = componentManager.resolveComponent(componentName);
                 component.removeEventListener(profile);
             }
         }
     }
 
     public void changeComponent(String old, String newc) {
         componentManager.changeImplementation(old, newc);
     }
 
     public void changeComponentProfile(String old, String newc) {
         componentProfileManager.changeProfile(old, newc);
     }
 
     public void destroyComponent(String name) {
         IComponent component = componentManager.resolveComponent(name);
         componentManager.destroyComponent(component);
     }
 
     public void destroyComponentProfile(String name) {
         componentProfileManager.destroyProfile(name);
     }
 
     public void destroyCustomEvent(String name) {
         eventManager.destroyCustomEvent(name);
     }
 
     public void destroyTask(String name) {
         taskManager.destroyTask(name);
     }
 
     public void loadComponent(String name) {
         componentManager.resolveComponent(name);
     }
 
     public void loadComponentProfile(String name) {
         componentProfileManager.loadAndinitializeProfile(name);
     }
 
     public void loadCustomEvent(String name) {
         eventManager.loadAndInitializeCustomEvent(name);
     }
 
     public void loadTask(String name) {
         taskManager.loadAndInitializeTask(name);
     }
 
     public void pauseComponent(String name) {
         IComponent component = componentManager.resolveComponent(name);
         componentManager.pauseComponent(component);
     }
 
     public void pauseComponentProfile(String name) {
         componentProfileManager.pauseProfile(name);
     }
 
     public void pauseCustomEvent(String name) {
         eventManager.pauseCustomEvent(name);
     }
 
     public void pauseTask(String name) {
         taskManager.pauseTask(name);
     }
 
     public void profileFireEvent(IAdaptationProfile profile, String eventType, Object content) {
         try {
             IEvent event = (IEvent) ClassLoader.newInstanceOf(eventType);
             event.setContentObject(content);
             profile.fireEvent(event);
         } catch (IllegalAccessException ex) {
             ex.printStackTrace();
         } catch (InstantiationException ex) {
             ex.printStackTrace();
         } catch (ClassNotFoundException ex) {
             ex.printStackTrace();
         }
     }
 
     public void resumeComponent(String name) {
         IComponent component = componentManager.resolveComponent(name);
         componentManager.resumeComponent(component);
     }
 
     public void resumeComponentProfile(String name) {
         componentProfileManager.resumeProfile(name);
     }
 
     public void resumeCustomEvent(String name) {
         eventManager.resumeCustomEvent(name);
     }
 
     public void resumeTask(String name) {
         taskManager.resumeTask(name);
     }
 
     public void startService(String name) {
         IService service = (IService) componentManager.resolveComponent(name);
         service.startService();
     }
 
     public void stopService(String name) {
         IService service = (IService) componentManager.resolveComponent(name);
         service.stopService();
     }
 
 }
