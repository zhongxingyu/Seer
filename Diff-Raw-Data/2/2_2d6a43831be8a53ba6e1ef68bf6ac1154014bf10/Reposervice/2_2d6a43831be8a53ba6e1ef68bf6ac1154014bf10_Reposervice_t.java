 /*
  * #%L
  * Bitrepository Webclient
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
 package org.bitrepository.webservice;
 
 import java.util.List;
 
 import org.bitrepository.BasicClient;
 import org.bitrepository.BasicClientFactory;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.common.utils.SettingsUtils;
 import org.bitrepository.settings.repositorysettings.Collection;
 import org.bitrepository.settings.repositorysettings.ProtocolSettings;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 /**
  * The class exposes the REST webservices provided by the Bitrepository-webclient using Jersey. 
  */
 
 @Path("/reposervice")
 public class Reposervice {
 
     private BasicClient client;
 
 
     public Reposervice() {
         client = BasicClientFactory.getInstance();
     }
     
     @GET
     @Path("/getCollectionIDs")
     @Produces(MediaType.APPLICATION_JSON)
     public String getCollectionIDs() {
         JSONArray array = new JSONArray();
         for(String collectionID : client.getCollectionIDs()) {
             array.put(collectionID);
         }
         
         return array.toString();
     }
     
     @GET
     @Path("/getCollectionName")
     @Produces(MediaType.APPLICATION_JSON)
     public String getCollectionName(@QueryParam("collectionID") String collectionID) {
         return SettingsUtils.getCollectionName(collectionID);
     }
     
     @GET
     @Path("/getCollections")
     @Produces(MediaType.APPLICATION_JSON)
     public String getCollections() throws JSONException {
         JSONArray array = new JSONArray();
         JSONObject obj;
         for(String collectionID : client.getCollectionIDs()) {
             obj = new JSONObject();
             obj.put("collectionID", collectionID);
             obj.put("collectionName", SettingsUtils.getCollectionName(collectionID));
             array.put((JSONObject) obj);
         }
         
         return array.toString();
     }
     
     @GET
     @Path("/getRepositoryName")
     @Produces(MediaType.APPLICATION_JSON)
     public String getRepositoryName() {
         return SettingsUtils.getRepositoryName();
     }
     
     @GET
     @Path("/getConfigurationOverview")
     @Produces(MediaType.APPLICATION_JSON)
     public String getConfigurationOverview() throws JSONException {
         JSONObject config = new JSONObject();
         config.put("repositoryName", SettingsUtils.getRepositoryName());
         config.put("collections", makeCollectionsArray());
         config.put("protocolSettings", makeProtocolSettingsObj());
         return config.toString();
     }
     
     private JSONObject makeProtocolSettingsObj() throws JSONException {
         JSONObject obj = new JSONObject();
         ProtocolSettings protocolSettings = client.getSettings().getRepositorySettings().getProtocolSettings();
         obj.put("allowedFileIDPattern", protocolSettings.getAllowedFileIDPattern());
         obj.put("defaultChecksumType", protocolSettings.getDefaultChecksumType());
         obj.put("requireMessageAuth", protocolSettings.isRequireMessageAuthentication());
         obj.put("requireOperationAuth", protocolSettings.isRequireOperationAuthorization());
         obj.put("requireChecksumForDestructiveReq", protocolSettings.isRequireChecksumForDestructiveRequests());
         obj.put("requireChecksumForNewFile", protocolSettings.isRequireChecksumForNewFileRequests());
         return obj; 
     }
 
     private JSONArray makeCollectionsArray() throws JSONException {
         JSONArray array = new JSONArray();
         Settings settings = client.getSettings();
         List<Collection> collections = settings.getRepositorySettings().getCollections().getCollection();
         for(Collection c : collections) {
             JSONObject obj = new JSONObject();
             obj.put("collectionID", c.getID());
            obj.put("collectionName", SettingsUtils.getCollectionName(c.getName()));
             JSONArray pillarArray = new JSONArray();
             List<String> pillars = c.getPillarIDs().getPillarID();
             for(String p : pillars) {
                 pillarArray.put(p);
             }
             obj.put("pillars", pillarArray);
             array.put(obj);
         }
         return array;
     }
     
     /**
      * getSettingsSummary provides a summary of some important settings of the Bitrepository collection, herein:
      * - The message bus which that is communicated with
      * - The Pillars in the collection
      * - The Bitrepository collection ID
      * @return The current settings formatted as HTML 
      */
     @GET
     @Path("/getSettingsSummary")
     @Produces("text/plain")
     public String getSummarySettings() {
         return client.getSettingsSummary();
     }
 }
