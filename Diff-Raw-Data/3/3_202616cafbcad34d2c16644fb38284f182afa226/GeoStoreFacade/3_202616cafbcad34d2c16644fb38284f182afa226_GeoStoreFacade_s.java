 /*
  *  GeoBatch - Open Source geospatial batch processing system
  *  https://github.com/nfms4redd/nfms-geobatch
  *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
  *  http://www.geo-solutions.it
  *
  *  GPLv3 + Classpath exception
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package it.geosolutions.geobatch.unredd.script.util;
 
 import it.geosolutions.unredd.geostore.model.AttributeDef;
 import it.geosolutions.unredd.geostore.model.ReverseAttributeDef;
 import it.geosolutions.unredd.geostore.model.UNREDDCategories;
 import it.geosolutions.unredd.geostore.model.UNREDDChartScript;
 import it.geosolutions.unredd.geostore.model.UNREDDLayerUpdate;
 import it.geosolutions.unredd.geostore.model.UNREDDStatsDef;
 import it.geosolutions.geostore.core.model.Resource;
 import it.geosolutions.geostore.services.dto.ShortResource;
 import it.geosolutions.geostore.services.dto.search.AndFilter;
 import it.geosolutions.geostore.services.dto.search.AttributeFilter;
 import it.geosolutions.geostore.services.dto.search.BaseField;
 import it.geosolutions.geostore.services.dto.search.CategoryFilter;
 import it.geosolutions.geostore.services.dto.search.FieldFilter;
 import it.geosolutions.geostore.services.dto.search.SearchFilter;
 import it.geosolutions.geostore.services.dto.search.SearchOperator;
 import it.geosolutions.geostore.services.rest.model.RESTResource;
 import it.geosolutions.geobatch.unredd.script.exception.GeoStoreException;
 import it.geosolutions.geobatch.unredd.script.model.GeoStoreConfig;
 import it.geosolutions.geostore.services.rest.model.RESTStoredData;
 import it.geosolutions.unredd.geostore.model.*;
 import it.geosolutions.unredd.geostore.utils.NameUtils;
 
 import java.io.File;
 import java.util.*;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Basic GeoStoreUtil operations.
  *
  * A GeoStoreUtil action is used to execute the various operations.
  */
 public abstract class GeoStoreFacade {
 
     private final Logger LOGGER = LoggerFactory.getLogger(GeoStoreFacade.class);
 
     private String gsurl;
     private String gsuser;
     private String gspwd;
 
     public GeoStoreFacade(String url, String user, String pwd) {
 
         this.gsurl = url;
         this.gsuser = user;
         this.gspwd = pwd;
 
     }
 
     public GeoStoreFacade(GeoStoreConfig config, File tempDir) {
 
         this.gsurl = config.getUrl();
         this.gsuser = config.getUsername();
         this.gspwd = config.getPassword();
 
     }
 
     /**
      * Insert or update the storedData of a StatsData.
      */
     public void setStatsData(Resource statsDef, String statsContent, String year, String month, String day) throws GeoStoreException {
         try {
             Resource statsData = this.searchStatsData(statsDef.getName(), year, month, day);
 
             if (statsData == null) {
                 LOGGER.info("No StatsData found for " + statsDef.getName()
                         + ", Year=" + year + ", Month=" + month
                         + ". Inserting StatsData");
                 insertStatsData(statsDef.getName(), year, month, statsContent, day);
 
             } else {
                 long id = statsData.getId();
                 LOGGER.info("StatsData found for " + statsDef.getName()
                         + ", Year=" + year + ", Month=" + month
                         + ". Updating StatsData " + id);
                 updateData(id, statsContent);
             }
         } catch (GeoStoreException ex) {
             throw ex;
         } catch (Exception ex) {
             LOGGER.error("Error computing stats: " + ex.getMessage(), ex);
             throw new GeoStoreException("Error while setting StatsData", ex);
         }
     }
 
     public void insertStatsData(String statsDefName, String year, String month, String day, String content) throws GeoStoreException {
         try {
             RESTResource statsDataResource = createStatsDataResource(statsDefName, year, month, day, content);
             insert(statsDataResource);
         } catch (Exception e) {
             throw new GeoStoreException("Error while inserting StatsData: " + statsDefName, e);
         }
     }
 
 
     /**
      * Generic search in GeoStoreUtil.
      *
      * @param filter the filter to apply for searching
      * @param getShortResource true if a list of resource is required, false if a RESTResource list is sufficient
      *
      * @return always a not null list
      * @throws GeoStoreException
      */
     abstract protected List search(SearchFilter filter, boolean getShortResource) throws GeoStoreException;
     abstract protected List search(SearchFilter filter, boolean getShortResource, String fileNameHint) throws GeoStoreException;
 
     /**
      * generic insert into geostore
      *
      * @param resource the resource to insert
      * @throws GeoStoreException 
      */
     abstract public Long insert(RESTResource resource) throws GeoStoreException;
     abstract public void updateData(long id, String data) throws GeoStoreException;
     
     /**
      * ************
      * this method allows to search a layer resource given its name
      *
      * @param layername the name of the layer resource to find
      * @return
      * @throws GeoStoreException 
      */
     public Resource searchLayer(String layername) throws GeoStoreException {
         if(LOGGER.isInfoEnabled())
             LOGGER.info("Searching Layer " + layername);
         // the filter to search a resource in the layer category
         SearchFilter filter = new AndFilter(
                 new FieldFilter(BaseField.NAME, layername, SearchOperator.EQUAL_TO),
                 createCategoryFilter(UNREDDCategories.LAYER));
 
         List<Resource> list = search(filter, false, "searchLayer_"+layername+"_");
         return getSingleResource(list);
     }
 
     protected Resource getSingleResource(List<Resource> list) {
         if (list == null || list.isEmpty()) {
             return null;
         } else {
             Resource r0 = list.get(0);
             if(list.size() > 1)
                 LOGGER.warn("Found " + list.size() + " resources of type " + r0.getCategory().getName() + " -- sample: "+ r0 );
             return r0;
         }
     }
 
     public Resource searchLayerUpdate(String layer, String year, String month, String day) throws GeoStoreException {
         String layerSnapshot = NameUtils.buildLayerUpdateName(layer, year, month, day);
         if(LOGGER.isInfoEnabled())
             LOGGER.info("Searching LayerUpdate " + layerSnapshot);
         
         SearchFilter filter = new AndFilter(
                 new FieldFilter(BaseField.NAME, layerSnapshot, SearchOperator.EQUAL_TO),
                 createCategoryFilter(UNREDDCategories.LAYERUPDATE));
         return getSingleResource(search(filter, false, "searchLU_"+layer+"_"));
     }
 
     public List<Resource> searchLayerUpdateByLayer(String layername) throws GeoStoreException {
         SearchFilter filter = new AndFilter(
                 createCategoryFilter(UNREDDCategories.LAYERUPDATE),
                 createAttributeFilter(UNREDDLayerUpdate.Attributes.LAYER, layername));
         return search(filter, false);
     }
 
     public List searchStatsDefByLayer(String layername, boolean getShortResource) throws GeoStoreException {
         if(LOGGER.isInfoEnabled())
             LOGGER.info("Searching StatsDef by layer " + layername);
         SearchFilter filter = new AndFilter(
                 createCategoryFilter(UNREDDCategories.STATSDEF),
                 createAttributeFilter(UNREDDStatsDef.ReverseAttributes.LAYER, layername));
         return search(filter, getShortResource);
     }
 
     public Resource searchStatsDefByName(String statsdefname) throws GeoStoreException {
         if(LOGGER.isInfoEnabled())
             LOGGER.info("Searching StatsDef " + statsdefname);
         SearchFilter filter = new AndFilter(
                 new FieldFilter(BaseField.NAME, statsdefname, SearchOperator.EQUAL_TO),
                 createCategoryFilter(UNREDDCategories.STATSDEF));
         List<Resource> list = search(filter, false, "searchStatsDef_"+statsdefname);
         return getSingleResource(list);
     }
 
     public Resource searchStatsData(String statsDefName, String year, String month, String day) throws GeoStoreException {
 
         String statsDataName = NameUtils.buildStatsDataName(statsDefName, year, month, day);
         if(LOGGER.isInfoEnabled())
             LOGGER.info("Searching StatsData" + statsDataName);
 
         SearchFilter filter = new AndFilter(
                 new FieldFilter(BaseField.NAME, statsDataName, SearchOperator.EQUAL_TO),
                 createCategoryFilter(UNREDDCategories.STATSDATA));
         return getSingleResource(search(filter, false, "searchStatsData_"+statsDataName));
     }
 
     public boolean existStatsData(String statsDefName, String year, String month, String day) throws GeoStoreException {
         String statsDataName = NameUtils.buildStatsDataName(statsDefName, year, month, day);
         SearchFilter filter = new AndFilter(
                 new FieldFilter(BaseField.NAME, statsDataName, SearchOperator.EQUAL_TO),
                 createCategoryFilter(UNREDDCategories.STATSDATA));
         List<ShortResource> list = search(filter, true);
         return list != null && ! list.isEmpty(); // will be not null only if at least 1 entry exist; we'll check also for isEMpty to be protected from future changes
     }
 
     public Resource searchChartScript(String scriptname) throws GeoStoreException {
         SearchFilter filter = new AndFilter(
                 new FieldFilter(BaseField.NAME, scriptname, SearchOperator.EQUAL_TO),
                 createCategoryFilter(UNREDDCategories.CHARTSCRIPT));
 
         List<Resource> scripts = search(filter, false);
         return getSingleResource(scripts);
     }
 
     public List<Resource> searchChartScriptByStatsDef(String statsDefName) throws GeoStoreException {
         SearchFilter filter = new AndFilter(
                 createCategoryFilter(UNREDDCategories.CHARTSCRIPT),
                 createAttributeFilter(UNREDDChartScript.ReverseAttributes.STATSDEF, statsDefName));
         return search(filter, false);
     }
 
     public List<ShortResource> searchChartDataByChartScript(String chartScriptName) throws GeoStoreException {
         SearchFilter filter = new AndFilter(
                 createCategoryFilter(UNREDDCategories.CHARTDATA),
                 createAttributeFilter(UNREDDChartData.Attributes.CHARTSCRIPT, chartScriptName));
         return search(filter, true);
     }
 
     public List<Resource> searchChartDataPublished(String chartScriptName) throws GeoStoreException {
         SearchFilter filter = new AndFilter(
                 createCategoryFilter(UNREDDCategories.CHARTDATA),
                 createAttributeFilter(UNREDDChartData.Attributes.CHARTSCRIPT, chartScriptName),
                 createAttributeFilter(UNREDDChartData.Attributes.PUBLISHED, "true"));
         return search(filter, false);
     }
 
     public void insertLayerUpdate(String layername, String year, String month, String day) throws GeoStoreException {
         RESTResource res = createLayerUpdate(layername, year, month, day);
         try {
             insert(res);
         } catch (Exception e) {
             LOGGER.error("Error while inserting LayerUpdate: " + res, e);
             throw new GeoStoreException("Error while inserting LayerUpdate on Layer " + layername, e);
         }
     }
 
     /**
      * Delete a resource.
      * Delete the resource identified by id.
      *
      * @param id
      */
     abstract public void delete(long id) throws GeoStoreException;
 
 
     /**
      * delete all the resources in the geostore repository
      *
      * @deprecated DANGEROUS!
      */
     public void delete() throws GeoStoreException {
         SearchFilter filter = new FieldFilter(BaseField.NAME, "*", SearchOperator.IS_NOT_NULL);
         List<ShortResource> resourceList = search(filter, true);
 
         if (resourceList == null || resourceList.isEmpty()) {
             LOGGER.info("No Resource to delete");
             return;
         }
         LOGGER.warn("Deleting " + resourceList.size() + " resources");
         for (ShortResource shortResource : resourceList) {
             LOGGER.info("Deleting " + shortResource);
             delete(shortResource.getId());
         }
     }
 
 
     private static <A extends AttributeDef> AttributeFilter createAttributeFilter(A att, String value) {
         return new AttributeFilter(att.getName(), value, att.getDataType(), SearchOperator.EQUAL_TO);
     }
 
     private static <R extends ReverseAttributeDef> AttributeFilter createAttributeFilter(R att, String value) {
         return new AttributeFilter(value, att.getName(), att.getType(), SearchOperator.EQUAL_TO);
     }
 
     private static CategoryFilter createCategoryFilter(UNREDDCategories category) {
         return new CategoryFilter(category.getName(), SearchOperator.EQUAL_TO);
     }
 
     protected static RESTResource createStatsDataResource(String statsDefName, String year, String month, String day, String csv) {
         UNREDDStatsData statsData = new UNREDDStatsData();
         statsData.setAttribute(UNREDDStatsData.Attributes.STATSDEF, statsDefName);
         statsData.setAttribute(UNREDDStatsData.Attributes.YEAR, year);
         if (month != null) {
             statsData.setAttribute(UNREDDStatsData.Attributes.MONTH, month);
         }
 
         RESTResource res = statsData.createRESTResource();
 
         String resName = NameUtils.buildStatsDataName(statsDefName, year, month, day);
         res.setName(resName);
 
         RESTStoredData storedData = new RESTStoredData();
         storedData.setData(csv);
 
         res.setStore(storedData);
 
         return res;
     }
 
     protected static  RESTResource createLayerUpdate(String layername, String year, String month, String day) {
         UNREDDLayerUpdate layerUpdate = new UNREDDLayerUpdate();
         layerUpdate.setAttribute(UNREDDLayerUpdate.Attributes.LAYER, layername);
         layerUpdate.setAttribute(UNREDDLayerUpdate.Attributes.YEAR, year);
         if (month != null) {
             layerUpdate.setAttribute(UNREDDLayerUpdate.Attributes.MONTH, month);
         }
         RESTResource res = layerUpdate.createRESTResource();
         String resName = NameUtils.buildLayerUpdateName(layername, year, month, day);
         res.setName(resName);
         return res;
     }
 
     public String getConfigPassword() {
         return gspwd;
     }
 
     public String getConfigUrl() {
         return gsurl;
     }
 
     public String getConfigUsername() {
         return gsuser;
     }
 
 }
