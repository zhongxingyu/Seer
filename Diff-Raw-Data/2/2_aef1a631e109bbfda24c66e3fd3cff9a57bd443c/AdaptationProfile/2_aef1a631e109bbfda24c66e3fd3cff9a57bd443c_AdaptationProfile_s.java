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
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 import midgard.componentmodel.Component;
 import midgard.events.IEvent;
 import midgard.events.IListener;
 import midgard.utils.NumericUtils;
 import midgard.web.json.JSONArray;
 import midgard.web.json.JSONException;
 import midgard.web.json.JSONObject;
 
 /**
  *
  * @author fenrrir
  */
 public class AdaptationProfile 
                     implements IAdaptationProfile {
 
     private JSONObject configuration = null;
     private String name;
     private Vector requiredEvents = null;
     private IAdaptationProfileHelper helper = null;
     private Vector componentsToMe = null;
     private Vector components = null;
     private Hashtable map = null;
     private Vector listeners = null;
 
 
    public void AdaptationProfile(){
         requiredEvents = new Vector();
         components = new Vector();
         componentsToMe = new Vector();
         map = new Hashtable();
         listeners = new Vector();
     }
 
     public void clear() {
         requiredEvents.removeAllElements();
         components.removeAllElements();
         componentsToMe.removeAllElements();
         map.clear();
         listeners.removeAllElements();
 
         requiredEvents = null;
         configuration = null;
         helper = null;
         components = null;
         componentsToMe = null;
         map = null;
         listeners = null;
     }
 
 
 
     public String getProfileName() {
         return name;
     }
 
     public void setProfileName(String name) {
         this.name = name;
     }
 
 
     public Vector connectComponentsToMe() {
         return componentsToMe;
     }
 
     public Vector connectToComponents() {
         return components;
     }
 
     public JSONObject getConfiguration() {
         return configuration;
     }
 
     public Vector requiredEvents() {
         return requiredEvents;
     }
 
     public void setConfiguration(JSONObject conf) {
         configuration = conf;
     }
 
     public void fireEvent(IEvent event) {
         for (int i=0; i< listeners.size(); i++){
             IListener listener = (IListener) listeners.elementAt(i);
             listener.newEventArrived(event);
         }
     }
 
     public Hashtable getCacheFiredEvents() {
         return new Hashtable();
     }
 
     public Vector getEventHistory(IEvent event) {
         return new Vector();
     }
 
     public Vector getListeners() {
         return listeners;
     }
 
     public void registerEventListener(IListener listener) {
         if (!listeners.contains(listener))
             listeners.addElement(listener);
     }
 
     public void removeEventListener(IListener listener) {
         listeners.removeElement(listener);
     }
 
 
 
     public void setHelper(IAdaptationProfileHelper helper) {
         this.helper = helper;
     }
 
     public void setup() {
         try {
             JSONArray array = configuration.getJSONArray("connectComponentsToMe");
             for (int i=0; i < array.length(); i++){
                 String componentName = array.getString(i);
                 componentsToMe.addElement(componentName);
             }
         } catch (JSONException ex) {
             
         }
 
         try {
             JSONArray array = configuration.getJSONArray("connectToComponents");
             for (int i=0; i < array.length(); i++){
                 String componentName = array.getString(i);
                 components.addElement(componentName);
             }
         } catch (JSONException ex) {
 
         }
 
         try {
             JSONObject json = configuration.getJSONObject("actions");
             Enumeration actions = json.keys();
             while (actions.hasMoreElements()){
                 String actionName = (String)actions.nextElement();
                 JSONObject action = json.getJSONObject(actionName);
                 String eventName = action.getJSONObject("require").getString("event");
                 map.put(eventName, action);
                 requiredEvents.addElement(eventName);
             }
         } catch (JSONException ex) {
 
         }
 
     }
 
     public void newEventArrived(IEvent event) {
         
         String name = event.getClass().getName();
 
         if (map.containsKey(name)){
             JSONObject action =  (JSONObject) map.get(name);
             try {
                 JSONObject conditional = action
                         .getJSONObject("require")
                             .getJSONObject("conditional");
 
                 String type = conditional.getString("type");
                 Double value = new Double(conditional.getDouble("value"));
                 Double eventValue = (Double) event.getContentObject();
 
                 if (type.equals("lt")){
                     if (!NumericUtils.lt( eventValue.doubleValue(),
                             value.doubleValue() ))
                         return;
                 }
 
                 if (type.equals("gt")){
                     if (!NumericUtils.gt( eventValue.doubleValue(),
                             value.doubleValue() ))
                         return;
                 }
 
                 if (type.equals("eq")){
                     if (!NumericUtils.eq( eventValue.doubleValue(),
                             value.doubleValue() ) )
                         return;
                 }
 
             } catch (JSONException ex) {
                 ex.printStackTrace();
             }
             try {
                 JSONObject call = action.getJSONObject("call");
                 String command = call.getString("command");
                 String param;
 
 
                 if (command.equals("startService")){
                     param = call.getString("param");
                     helper.startService(param);
 
                 }
 
                 if (command.equals("stopService")){
                     param = call.getString("param");
                     helper.stopService(param);
 
                 }
 
                 if (command.equals("loadComponent")){
                     param = call.getString("param");
                     helper.loadComponent(param);
                 }
 
                 if (command.equals("pauseComponent")){
                     param = call.getString("param");
                     helper.pauseComponent(param);
 
                 }
 
                 if (command.equals("resumeComponent")){
                     param = call.getString("param");
                     helper.resumeComponent(param);
                 }
 
                 if (command.equals("destroyComponent")){
                     param = call.getString("param");
                     helper.destroyComponent(param);
                 }
 
                 if (command.equals("changeComponent")){
                     param = call.getString("param");
                     String param2 = call.getString("param2");
                     helper.changeComponent(param, param2);
                 }
 
 
                 if (command.equals("loadComponentProfile")){
                     param = call.getString("param");
                     helper.loadComponentProfile(param);
                 }
 
                 if (command.equals("pauseComponentProfile")){
                     param = call.getString("param");
                     helper.pauseComponentProfile(param);
 
                 }
 
                 if (command.equals("resumeComponentProfile")){
                     param = call.getString("param");
                     helper.resumeComponentProfile(param);
                 }
 
                 if (command.equals("destroyComponentProfile")){
                     param = call.getString("param");
                     helper.destroyComponentProfile(param);
                 }
 
                 if (command.equals("changeComponentProfile")){
                     param = call.getString("param");
                     String param2 = call.getString("param2");
                     helper.changeComponentProfile(param, param2);
                 }
 
                 if (command.equals("loadTask")){
                     param = call.getString("param");
                     helper.loadTask(param);
                 }
 
                 if (command.equals("pauseTask")){
                     param = call.getString("param");
                     helper.pauseTask(param);
 
                 }
 
                 if (command.equals("resumeTask")){
                     param = call.getString("param");
                     helper.resumeTask(param);
                 }
 
                 if (command.equals("destroyTask")){
                     param = call.getString("param");
                     helper.destroyTask(param);
                 }
 
                 if (command.equals("loadCustomEvent")){
                     param = call.getString("param");
                     helper.loadCustomEvent(param);
                 }
 
                 if (command.equals("pauseCustomEvent")){
                     param = call.getString("param");
                     helper.pauseCustomEvent(param);
 
                 }
 
                 if (command.equals("resumeCustomEvent")){
                     param = call.getString("param");
                     helper.resumeCustomEvent(param);
                 }
 
                 if (command.equals("destroyCustomEvent")){
                     param = call.getString("param");
                     helper.destroyCustomEvent(param);
                 }
 
                 if (command.equals("fireEvent")){
                     param = call.getString("type");
                     Object param2 = call.get("param");
                     helper.profileFireEvent(this, param, param2);
                 }
 
             } catch (JSONException ex) {
                 ex.printStackTrace();
             }
 
 
         }
 
     }
 
 }
