 /*
  * #%L
  * Bitrepository Monitoring Service
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.monitoringservice.webservice;
 
 import java.util.Map;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.bitrepository.common.utils.TimeUtils;
 import org.bitrepository.monitoringservice.MonitoringService;
 import org.bitrepository.monitoringservice.MonitoringServiceFactory;
 import org.bitrepository.monitoringservice.status.ComponentStatus;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 @Path("/MonitoringService")
 public class RestMonitoringService {
     
     private MonitoringService service;
     
     public RestMonitoringService() {
         service = MonitoringServiceFactory.getMonitoringService();
     }
     
     @GET
     @Path("/getMonitoringConfiguration/")
     @Produces("application/json")
     public String getMonitoringServiceConfiguration() {
         JSONArray array = new JSONArray();
         
         array.put(makeConfigurationEntry("Check interval", TimeUtils.millisecondsToHuman(service.getCollectionInterval())));
         array.put(makeConfigurationEntry("Max retries", Long.toString(service.getMaxRetries())));
         
         return array.toString();
     }
     
     @GET
     @Path("/getComponentStatus/")
     @Produces("application/json")
     public String getComponentStatus() {
         Map<String, ComponentStatus> statusMap = service.getStatus();
         JSONArray array = new JSONArray();
         
         for(String component : statusMap.keySet()) {
             array.put(makeJSONStatusObject(component, statusMap.get(component)));
         }
         return array.toString();
     }
     
     private JSONObject makeJSONStatusObject(String componentID, ComponentStatus status) {
         JSONObject obj = new JSONObject();
         try {
             obj.put("componentID", componentID);
             obj.put("status", status.getStatus());
             obj.put("info", status.getInfo());
             XMLGregorianCalendar cal = status.getLastReply();
             if(cal != null) {
                obj.put("timeStamp", TimeUtils.shortDate(cal));
            } else {
                obj.put("timeStamp", "N/A");    
             }
            
             return obj;
         } catch (JSONException e) {
             return (JSONObject) JSONObject.NULL;
         }
     }
     
     private JSONObject makeConfigurationEntry(String option, String value) {
         JSONObject obj = new JSONObject();
         try {
             obj.put("confOption", option);            
             obj.put("confValue", value);
             return obj;
         } catch (JSONException e) {
             return (JSONObject) JSONObject.NULL;
         }
     }
 }
