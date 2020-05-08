 /*
  * #%L
  * Bitrepository Audit Trail Service
  * 
  * $Id$
  * $HeadURL$
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
 package org.bitrepository.audittrails.webservice;
 
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 
 import org.bitrepository.audittrails.AuditTrailService;
 import org.bitrepository.audittrails.AuditTrailServiceFactory;
 import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Path("/AuditTrailService")
 
 public class RestAuditTrailService {
     /** The log.*/
     private Logger log = LoggerFactory.getLogger(getClass());
     private AuditTrailService service;
     
     public RestAuditTrailService() {
         service = AuditTrailServiceFactory.getAuditTrailService();	
     }
     
     @POST
     @Path("/queryAuditTrailEvents/")
     @Consumes("application/x-www-form-urlencoded")
     @Produces("application/json")
     public String queryAuditTrailEvents(
             @FormParam ("fromDate") String fromDate,
             @FormParam ("toDate") String toDate,
             @FormParam ("fileID") String fileID,
             @FormParam ("reportingComponent") String reportingComponent,
             @FormParam ("actor") String actor,
             @FormParam ("action") String action) {
         Date from = makeDateObject(fromDate);
         Date to = makeDateObject(toDate);
         String filteredAction;
         if(action.equals("ALL")) {
             filteredAction = null;
         } else {
             filteredAction = action;
         }
         
         Collection<AuditTrailEvent> events = service.queryAuditTrailEvents(from, to, contentOrNull(fileID),
                 contentOrNull(reportingComponent), contentOrNull(actor), filteredAction);
         
         JSONArray array = new JSONArray();
         if(events != null) {
             log.debug("Got " + events.size() + " AuditTrailEvents!");
             for(AuditTrailEvent event : events) {
                 array.put(makeJSONEntry(event));
             }
         } else {
             log.debug("Got null queryAuditTrailEvents call!");
         }
         return array.toString();
     }
     
     private JSONObject makeJSONEntry(AuditTrailEvent event) {
         JSONObject obj = new JSONObject();
         try {
             obj.put("fileID", event.getFileID());
             obj.put("reportingComponent", event.getReportingComponent());
             obj.put("actor", event.getActorOnFile());
             obj.put("action", event.getActionOnFile());
             obj.put("timeStamp", event.getActionDateTime());
             obj.put("info", event.getInfo());
             obj.put("auditTrailInfo", event.getAuditTrailInformation());
             return obj;
         } catch (JSONException e) {
             return (JSONObject) JSONObject.NULL;
         }
     }
     
     private Date makeDateObject(String dateStr) {
         if(dateStr == null || dateStr.trim().isEmpty()) {
             return null;
         } else {
             String[] components = dateStr.split("/");
             int year = Integer.parseInt(components[2]);
             int month = Integer.parseInt(components[0]);
             int day = Integer.parseInt(components[1]);
             Calendar time = Calendar.getInstance();
            time.set(year, month, day);
             
             return time.getTime();
         }
     }
     
     private String contentOrNull(String input) {
         if(input != null && input.trim().isEmpty()) {
             return null;
         } else {
             return input.trim();
         }
     }
 }
