 package es.upm.dit.gsi.shanks.model.event;
 
 import java.util.HashMap;
 import sim.engine.Steppable;
 import es.upm.dit.gsi.shanks.model.element.NetworkElement;
 import es.upm.dit.gsi.shanks.model.element.exception.UnsupportedNetworkElementStatusException;
 import es.upm.dit.gsi.shanks.model.scenario.Scenario;
 
 //Under construction
 
 
 public abstract class Event {
 
     /**
      * Metodos para properties en NetworkElement
      *     -changePropertiesOfNetworkElement(NetworkElement) --> A que elemento le cambiamos sus properties
      *     estas properties vienen asignadas en la declaracion de cada evento
      *     Se llama en las clases que extiendan de Scenario.
      *     -addAfectedPropertiesOfElement(String, Object) --> Las properties de un elemento que
      *     este evento es capaz de cambiar.
      *   
      *     
      * Metodos para status en NetworkElement
      *     -changeStatus(NetworkElement) --> A que elemento le cambiamos sus estados
      *     estos estados modificados vienen asignados en la declaracion de cada evento
      *     Se llama en las clases que extiendan de Scenario.
      *     -addAffectedPropertiesOfElement(String, Boolean) --> Los estados de un elemento que
      *     este evento es capaz de cambiar.
      *   
      *     
      * Metodos para Properties de Scenario
      *     -changePropertiesOfScenario(Scenario) --> A que scenario le cambiamos las properties
      *     estas properties vienen asignadas en la declaracion del evento.
      *     Se llama en las clases que extiendan de Scenario.
      *     -addAffectedPropertiesOfScenario(String, Object) --> Las properties del scenario que
      *     este evento es capaz de cambiar
      */
     
     private String name;
     private Steppable generator;
     
     private HashMap<String, Object> propertiesOfElementToChange;
     private HashMap<String, Boolean> statusOfElementToChange;
     private HashMap<String, Object> propertiesOfScenarioToChange;
     
     public boolean launch;
     
     public Event(String name, Steppable generator){
         this.name = name;
         this.generator = generator;
         
         this.propertiesOfElementToChange = new HashMap<String, Object>();
         this.statusOfElementToChange = new HashMap<String, Boolean>();
         this.propertiesOfScenarioToChange = new HashMap<String, Object>();
         
         launch = false;
     }
     
     public void setGenerator(Steppable gen){
         this.generator = gen;
     }
     
     public Steppable getGenerator(){
         return generator;
     }
     
     public String getName(){
         return name;
     }
     
     public HashMap<String, Object> getPropertiesAffectedOfElement(){
         return propertiesOfElementToChange;
     }
     
     public HashMap<String, Boolean> getStatusAffectedOfElement(){
         return statusOfElementToChange;
     }
     
     public HashMap<String, Object> getPropertiesAffectedOfScenario(){
         return propertiesOfScenarioToChange;
     }
     
     public boolean isLaunched(){
         return launch;
     }
     
     public void launchEvent(){
         this.launch = true;
     }
     
     
     
     public void changePropertiesOfScenario(Scenario scenario){
         if(this.launch){
             for(String property : propertiesOfScenarioToChange.keySet()){
                 if(scenario.getProperties().containsKey(property)){
                     scenario.getProperties().put(property, 
                             propertiesOfScenarioToChange.get(property));
                 }
             }
         }
     }
     
     public void changeStatus(NetworkElement element) throws UnsupportedNetworkElementStatusException{
         if(this.launch){
             for(String state : statusOfElementToChange.keySet()){
                 if(element.getStatus().containsKey(state)){
                     element.getStatus().put(state, statusOfElementToChange.get(state));
                 }
             }
         }
     }
     
     public void changePropertiesOfNetworkElement(NetworkElement element) throws UnsupportedNetworkElementStatusException{
         if(this.launch){
             for(String prop : propertiesOfElementToChange.keySet()){
                 if(element.getProperties().containsKey(prop)){
                     element.getProperties().put(prop, propertiesOfElementToChange.get(prop));
                 }
             } 
         }
     }
     
     public void addAffectedPropertiesOfScenario(String property, Object value){
         propertiesOfScenarioToChange.put(property, value);
     }
     
     public void addAffectedPropertiesOfElement(String property, Object value){
         propertiesOfElementToChange.put(property, value);
     }
     
     public void addAffectedStatesOfElement(String state, Boolean value){
         statusOfElementToChange.put(state, value);
     }
     
     public abstract void addChanges();
     
     
 }
