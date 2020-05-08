 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations
  * (FAO). All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,this
  * list of conditions and the following disclaimer. 2. Redistributions in binary
  * form must reproduce the above copyright notice,this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. 3. Neither the name of FAO nor the names of its
  * contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
  * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.services.boundary.ws;
 
 import java.util.HashMap;
 import java.util.List;
 import javax.annotation.Resource;
 import javax.ejb.EJB;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 import javax.xml.ws.WebServiceContext;
 import org.sola.services.boundary.transferobjects.configuration.ConfigMapLayerTO;
 import org.sola.services.boundary.transferobjects.configuration.MapDefinitionTO;
 import org.sola.services.common.ServiceConstants;
 import org.sola.services.common.faults.FaultUtility;
 import org.sola.services.common.faults.SOLAFault;
 import org.sola.services.common.faults.UnhandledFault;
 import org.sola.services.common.webservices.AbstractWebService;
 import org.sola.services.ejb.search.businesslogic.SearchEJBLocal;
 import org.sola.services.ejb.search.repository.entities.ConfigMapLayer;
 import org.sola.services.ejb.search.spatial.QueryForNavigation;
 import org.sola.services.ejb.search.spatial.ResultForNavigationInfo;
 
 /**
  *
  * @author Elton Manoku
  */
 @WebService(serviceName = "spatial-service", targetNamespace = ServiceConstants.SPATIAL_WS_NAMESPACE)
 public class Spatial extends AbstractWebService {
 
     @EJB
     SearchEJBLocal searchEJB;
     @Resource
     private WebServiceContext wsContext;
 
     @WebMethod(operationName = "GetMapDefinition")
     public MapDefinitionTO getMapDefinition(@WebParam(name = "languageCode") String languageCode)
             throws UnhandledFault, SOLAFault {
 
         final Object[] result = {null};
         final String languageCodeTmp = languageCode;
         runGeneralMethodNoUser(wsContext, new Runnable() {
 
             @Override
             public void run() {
                 HashMap<String, String> mapSettings = searchEJB.getMapSettingList();
                 List<ConfigMapLayer> configMapLayerList = searchEJB.getConfigMapLayerList(languageCodeTmp);
                 MapDefinitionTO mapDefinition = new MapDefinitionTO();
                 mapDefinition.setSrid(Integer.parseInt(mapSettings.get("map-srid")));
                 mapDefinition.setWktOfCrs(mapSettings.get("wkt-of-crs"));
                 mapDefinition.setWest(Double.parseDouble(mapSettings.get("map-west")));
                 mapDefinition.setSouth(Double.parseDouble(mapSettings.get("map-south")));
                 mapDefinition.setEast(Double.parseDouble(mapSettings.get("map-east")));
                 mapDefinition.setNorth(Double.parseDouble(mapSettings.get("map-north")));
                 mapDefinition.setSnapTolerance(Double.parseDouble(mapSettings.get("map-tolerance")));
                 mapDefinition.setSurveyPointShiftRuralArea(
                         Double.parseDouble(mapSettings.get("map-shift-tolerance-rural")));
                 mapDefinition.setSurveyPointShiftUrbanArea(
                         Double.parseDouble(mapSettings.get("map-shift-tolerance-urban")));
                 for (ConfigMapLayer configMapLayer : configMapLayerList) {
                     ConfigMapLayerTO configMapLayerTO = new ConfigMapLayerTO();
                     configMapLayerTO.setId(configMapLayer.getId());
                     configMapLayerTO.setPojoQueryName(configMapLayer.getPojoQueryName());
                     configMapLayerTO.setPojoQueryNameForSelect(configMapLayer.getPojoQueryNameForSelect());
                     configMapLayerTO.setPojoStructure(configMapLayer.getPojoStructure());
                     configMapLayerTO.setShapeLocation(configMapLayer.getShapeLocation());
                     configMapLayerTO.setStyle(configMapLayer.getStyle());
                     configMapLayerTO.setTypeCode(configMapLayer.getTypeCode());
                     configMapLayerTO.setTitle(configMapLayer.getTitle());
                     configMapLayerTO.setWmsLayers(configMapLayer.getWmsLayers());
                     configMapLayerTO.setWmsUrl(configMapLayer.getWmsUrl());
                     mapDefinition.getLayers().add(configMapLayerTO);
                 }
                 result[0] = mapDefinition;
             }
         });
 
         return (MapDefinitionTO) result[0];
     }
 
     /**
      * Dummy method to check the web service instance is working
      */
     @WebMethod(operationName = "CheckConnection")
     public boolean CheckConnection() {
         return true;
     }
 
     @WebMethod(operationName = "GetSpatialForNavigation")
     public ResultForNavigationInfo GetSpatialForNavigation(QueryForNavigation spatialQuery)
             throws SOLAFault, UnhandledFault {
 
         final Object[] result = {null};
         final QueryForNavigation spatialQueryTmp = spatialQuery;
 
         runGeneralMethodNoUser(wsContext, new Runnable() {
 
             @Override
             public void run() {
                 result[0] = searchEJB.getSpatialResult(spatialQueryTmp);
             }
         });
        
        return (ResultForNavigationInfo)result[0];
     }
 }
