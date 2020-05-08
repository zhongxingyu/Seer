 /*
  * #%L
  * Bitrepository Integrity Service
  * %%
  * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
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
 package org.bitrepository.integrityservice.web;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import org.bitrepository.common.utils.SettingsUtils;
 import org.bitrepository.common.utils.TimeUtils;
 import org.bitrepository.common.webobjects.StatisticsCollectionSize;
 import org.bitrepository.common.webobjects.StatisticsDataSize;
 import org.bitrepository.common.webobjects.StatisticsPillarSize;
 import org.bitrepository.integrityservice.IntegrityServiceManager;
 import org.bitrepository.integrityservice.cache.CollectionStat;
 import org.bitrepository.integrityservice.cache.IntegrityModel;
 import org.bitrepository.integrityservice.cache.PillarStat;
 
 @Path("/Statistics")
 public class RestStatisticsService {
     private IntegrityModel model;
 
     public RestStatisticsService() {
         this.model = IntegrityServiceManager.getIntegrityModel();
     }
      
     @GET
     @Path("/getDataSizeHistory/")
     @Produces(MediaType.APPLICATION_JSON)
     public List<StatisticsDataSize> getDataSizeHistory(@QueryParam("collectionID") String collectionID, 
             @QueryParam("maxEntries") @DefaultValue("1000") int maxEntries) {
         List<CollectionStat> stats = model.getLatestCollectionStat(collectionID, maxEntries);
         List<StatisticsDataSize> data = new ArrayList<StatisticsDataSize>();
         for(CollectionStat stat : stats) {
             StatisticsDataSize obj = new StatisticsDataSize();
             Date statTime = stat.getStatsTime();
             obj.setDateMillis(statTime.getTime());
             obj.setDateString(TimeUtils.shortDate(statTime));
             obj.setDataSize(stat.getDataSize());
             data.add(obj);
         }
         
         return data;
     }
     
     @GET
     @Path("/getLatestPillarDataSize/")
     @Produces(MediaType.APPLICATION_JSON)
     public List<StatisticsPillarSize> getLatestPillarDataSize() {
         return getCurrentPillarsDataSize();
     }
     
     @GET
     @Path("/getLatestcollectionDataSize/")
     @Produces(MediaType.APPLICATION_JSON)
     public List<StatisticsCollectionSize> getLatestCollectionDataSize() {
         List<StatisticsCollectionSize> data = new ArrayList<StatisticsCollectionSize>();
         List<CollectionStat> stats = getLatestCollectionStatistics();
         for(CollectionStat stat : stats) {
             StatisticsCollectionSize obj = new StatisticsCollectionSize();
             obj.setCollectionID(stat.getCollectionID());
             obj.setDataSize(stat.getDataSize());
             data.add(obj);
         }
         return data;    
     }
 
     public List<CollectionStat> getLatestCollectionStatistics() {
         List<CollectionStat> res = new ArrayList<CollectionStat>();
         for(String collection : SettingsUtils.getAllCollectionsIDs()) {
             List<CollectionStat> stats = model.getLatestCollectionStat(collection, 1);
             if(!stats.isEmpty()) {
                 res.add(stats.get(0));
             }
         }
         return res;
     }
 
     public List<StatisticsPillarSize> getCurrentPillarsDataSize() {
         Map<String, StatisticsPillarSize> stats = new HashMap<String, StatisticsPillarSize>();
         for(String pillar: SettingsUtils.getAllPillarIDs()) {
             StatisticsPillarSize stat = new StatisticsPillarSize();
             stat.setPillarID(pillar);
             stat.setDataSize(0L);
             stats.put(pillar, stat);
         }
         for(String collection : SettingsUtils.getAllCollectionsIDs()) {
             for(PillarStat pillarStat : model.getLatestPillarStats(collection)) {
                 StatisticsPillarSize stat = stats.get(pillarStat.getPillarID());
                 stat.setDataSize(stat.getDataSize() + pillarStat.getDataSize());
                 stats.put(stat.getPillarID(), stat);
             }
         }
        return new ArrayList<StatisticsPillarSize>(stats.values());
     }
 }
