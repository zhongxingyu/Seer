 package es.upm.dit.gsi.shanks.model.ftth.scenario;
 
 import es.upm.dit.gsi.shanks.model.element.device.DeviceDefinitions;
 import es.upm.dit.gsi.shanks.model.element.link.LinkDefinitions;
 import es.upm.dit.gsi.shanks.model.ftth.element.device.GatewayRouter;
 import es.upm.dit.gsi.shanks.model.ftth.element.device.OLT;
 import es.upm.dit.gsi.shanks.model.ftth.element.device.ONT;
 import es.upm.dit.gsi.shanks.model.ftth.element.device.Splitter;
 import es.upm.dit.gsi.shanks.model.ftth.element.link.OpticalFiber;
 import es.upm.dit.gsi.shanks.model.ftth.failure.ONTFailure;
 import es.upm.dit.gsi.shanks.model.scenario.ScenarioDefinitions;
 import es.upm.dit.gsi.shanks.model.scenario.Scenario;
 
//LOOK una clase como esta es la que cualquiera debe implementar para crear su propio escenario
 
 public class ScenarioFTTH extends Scenario {
 
     public ScenarioFTTH() {
         super(ScenarioDefinitions.SCENARIO_FTTH_ACCESS_NETWORK);
     }
 
     @Override
     public void addNetworkElements() {
         // TODO rellenar con los dispositivos de FTTH, por ejemplo:
         this.addNetworkElement(new OLT("OLT", DeviceDefinitions.DEVICE_OLT));
         this.addNetworkElement(new OpticalFiber("Fiber1", LinkDefinitions.OPTICAL_FIBER));
         this.addNetworkElement(new ONT("ONT", DeviceDefinitions.DEVICE_ONT));
         this.addNetworkElement(new Splitter("Splitter1-4", DeviceDefinitions.DEVICE_SPLITTER4));
         this.addNetworkElement(new Splitter("Splitter1-16", DeviceDefinitions.DEVICE_SPLITTER16));
         this.addNetworkElement(new GatewayRouter("Gateway de Juan", DeviceDefinitions.DEVICE_GATEWAY));
     }
 
     @Override
     public void addPossibleFailures() {
         // TODO rellenar los posibles tipos de failures, por ejemplo:
         this.addPossibleFailure(ONTFailure.class);
     }
     
    // TODO hay que añadir la manera de situarlos "en el espacio", ahora mismo eso esta hecho en Simulation.java
 
 }
