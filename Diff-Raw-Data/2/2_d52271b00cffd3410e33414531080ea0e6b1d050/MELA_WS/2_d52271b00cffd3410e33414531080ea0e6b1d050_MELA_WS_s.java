 /**
  * Copyright 2013 Technische Universitat Wien (TUW), Distributed Systems Group
  * E184
  *
  * This work was partially supported by the European Commission in terms of the
  * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package at.ac.tuwien.dsg.mela.analysisservice.apis.webAPI;
 
 import at.ac.tuwien.dsg.mela.common.monitoringConcepts.Metric;
 import at.ac.tuwien.dsg.mela.common.requirements.Requirements;
 import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElementMonitoringSnapshot;
 import at.ac.tuwien.dsg.mela.common.monitoringConcepts.ServiceMonitoringSnapshot;
 import at.ac.tuwien.dsg.mela.analysisservice.control.SystemControl;
 import at.ac.tuwien.dsg.mela.analysisservice.control.SystemControlFactory;
 import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;
 import at.ac.tuwien.dsg.mela.common.monitoringConcepts.Action;
 import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
 import at.ac.tuwien.dsg.mela.analysisservice.utils.Configuration;
 import at.ac.tuwien.dsg.mela.analysisservice.utils.exceptions.ConfigurationException;
 import com.thoughtworks.xstream.XStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import org.apache.log4j.Level;
 
 /**
  * Author: Daniel Moldovan E-Mail: d.moldovan@dsg.tuwien.ac.at  *
  *
  */
 @Path("/")
 public class MELA_WS {
 
     private static SystemControl systemControl;
 
     static {
 //        Configuration.getLogger(MELA_WS.class).log(Level.INFO, "MELA started");
         systemControl = SystemControlFactory.getSystemControlInstance();
     }
     @Context
     private UriInfo context;
 
     public MELA_WS() {
     }
 
     @POST
     @Path("/elasticitypathway")
     @Consumes("application/xml")
     @Produces("application/json")
     public String getElasticityPathwayInJSON(MonitoredElement element) {
 //        ElasticitySpace elasticitySpace = systemControl.getElasticitySpace();
 //        Map<Metric, List<MetricValue>> map = elasticitySpace.getMonitoredDataForService(element);
 //        
 //        
 //      ElasticitySpace elasticitySpace = systemControl.getElasticitySpace();
 //        Map<Metric, List<MetricValue>> map = systemControl.getElasticityPathway(element);
 //        
 //        if (map == null) {
 //            Configuration.getLogger(this.getClass()).log(Level.ERROR, "Service Element " + element.getId() + " at level " + element.getLevel() + " was not found in service structure");
 //            JSONObject elSpaceJSON = new JSONObject();
 //            elSpaceJSON.put("name", "Service not found");
 //            return elSpaceJSON.toJSONString();
 //        }
 
 //        Runtime.getRuntime().gc();
         return systemControl.getElasticityPathway(element);
 //        return ConvertToJSON.convertElasticityPathway(new ArrayList<Metric>(map.keySet()), neurons);
     }
 
     /**
      *
      * @param element the MonitoredElement for which the elasticity space must
      * be returned. Needs BOTH the Element ID and the Element LEVEL (SERVICE,
      * SERVICE_TOPOLOGY, etc)
      * @return the elasticity space in JSON
      */
     @POST
     @Path("/elasticityspace")
     @Consumes("application/xml")
     @Produces("application/json")
     public String getLatestElasticitySpaceInJSON(MonitoredElement element) {
        return systemControl.getElasticityPathway(element);
     }
 
     /**
      *
      * @param compositionRulesConfiguration the metric composition rules, both
      * the HISTORICAL and MULTI_LEVEL rules
      */
     @PUT
     @Path("/metricscompositionrules")
     @Consumes("application/xml")
     public void putCompositionRules(CompositionRulesConfiguration compositionRulesConfiguration) {
         if (compositionRulesConfiguration != null) {
             systemControl.setCompositionRulesConfiguration(compositionRulesConfiguration);
         } else {
             Configuration.getLogger(this.getClass()).log(Level.WARN, "supplied compositionRulesConfiguration is null");
         }
     }
 
     /**
      *
      * @param element the service topology to be monitored
      */
     @PUT
     @Path("/servicedescription")
     @Consumes("application/xml")
     public void putServiceDescription(MonitoredElement element) {
         if (element != null) {
             systemControl.setServiceConfiguration(element);
         } else {
             Configuration.getLogger(this.getClass()).log(Level.WARN, "supplied service description is null");
         }
     }
 
     /**
      *
      * @param element refreshes the VM's attached to each Service Unit. For a
      * structural update, use "PUT servicedescription", as in such a case the
      * elasticity signature needs to be recomputed
      */
     @POST
     @Path("/servicedescription")
     @Consumes("application/xml")
     public void updateServiceDescription(MonitoredElement element) {
         if (element != null) {
             systemControl.updateServiceConfiguration(element);
         } else {
             Configuration.getLogger(this.getClass()).log(Level.WARN, "supplied service description is null");
         }
     }
 
     /**
      *
      * @param requirements service behavior limits on metrics directly measured
      * or obtained from metric composition
      */
     @PUT
     @Path("/servicerequirements")
     @Consumes("application/xml")
     public void putServiceRequirements(Requirements requirements) {
         if (requirements != null) {
             systemControl.setRequirements(requirements);
         } else {
             Configuration.getLogger(this.getClass()).log(Level.WARN, "supplied service requirements are null");
         }
     }
 
     /**
      * Method used to list for a particular service unit ID what are the
      * available metrics that can be monitored directly
      *
      * @param serviceID ID of the service UNIT from which to return the
      * available monitored data (before composition) for the VMS belonging to
      * the SERVICE UNIT example:
      * http://localhost:8080/MELA/REST_WS/metrics?serviceID=CassandraController
      * @return
      */
     @GET
     @Path("/metrics")
     @Produces("application/xml")
     public String getAvailableMetrics(@QueryParam("serviceID") String serviceID) {
         try {
             List<String> strings = new ArrayList<String>();
             for (Metric metric : systemControl.getAvailableMetricsForMonitoredElement(new MonitoredElement(serviceID))) {
                 strings.add(metric.getName());
             }
             XStream xStream = new XStream();
             xStream.alias("Metrics", List.class);
             xStream.alias("Metric", String.class);
             String result = xStream.toXML(strings);
             return result;
         } catch (ConfigurationException ex) {
             Logger.getLogger(MELA_WS.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
             return "<Metrics/>";
         }
 
     }
 
     /**
      * Method for retrieving an easy to display JSON string of the latest
      * monitored Data complete with composed metrics
      *
      * @return JSON representation of the monitored data. JSON Format:
      * {"name":"ServiceEntityName","type":"MonitoredElementType","children":[{"name":"metric
      * value [metricName]","type":"metric"}]} JSON Example:
      * {"name":"LoadBalancer","children":[{"name":"1
      * [vmCount]","type":"metric"},{"name":"51
      * [clients]","type":"metric"},{"name":"10.99.0.62","children":[{"name":"51
      * [activeConnections]","type":"metric"},{"name":"1
      * [vmCount]","type":"metric"}],"type":"VM"}],"type":"SERVICE_UNIT"}
      */
     @GET
     @Path("/monitoringdataJSON")
     @Produces("application/json")
     public String getLatestMonitoringDataInJSON() {
         return systemControl.getLatestMonitoringDataINJSON();
      
     }
 
     @GET
     @Path("/monitoringdataXML")
     @Produces("application/xml")
     public MonitoredElementMonitoringSnapshot getLatestMonitoringDataInXML() {
         ServiceMonitoringSnapshot monitoringSnapshot = systemControl.getLatestMonitoringData();
         if (monitoringSnapshot != null && !monitoringSnapshot.getMonitoredData().isEmpty()) {
             return monitoringSnapshot.getMonitoredData(MonitoredElement.MonitoredElementLevel.SERVICE).values().iterator().next();
         } else {
             return new MonitoredElementMonitoringSnapshot();
         }
     }
 
     @GET
     @Path("/metriccompositionrules")
     @Produces("application/json")
     public String getMetricCompositionRules() {
         return systemControl.getMetricCompositionRules();
         
 
     }
 
     @POST
     @Path("/addexecutingactions")
     public void addExecutingAction(List<Action> executingActions) {
         for (Action action : executingActions) {
             systemControl.addExecutingAction(action.getTargetEntityID(), action.getAction());
         }
     }
 
     @POST
     @Path("/removeexecutingactions")
     public void removeExecutingAction(List<Action> executingActions) {
         for (Action action : executingActions) {
             systemControl.removeExecutingAction(action.getTargetEntityID(), action.getAction());
         }
     }
 }
