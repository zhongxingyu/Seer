 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.requests;
 
 import org.geoserver.ows.util.KvpMap;
 import org.geoserver.ows.util.KvpUtils;
 import org.geoserver.platform.ServiceException;
 import org.geoserver.wms.kvp.GetMapKvpRequestReader;
 import org.geotools.data.wms.response.GetFeatureInfoResponse;
 import org.vfny.geoserver.Request;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.global.MapLayerInfo;
 import org.vfny.geoserver.global.WMS;
 import org.vfny.geoserver.wms.WmsException;
 import org.vfny.geoserver.wms.servlets.WMService;
 
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.logging.Logger;
 import javax.servlet.http.HttpServletRequest;
 import org.vfny.geoserver.global.CoverageInfo;
 
 
 /**
  * Builds a GetFeatureInfo request object given by a set of CGI parameters
  * supplied in the constructor.
  *
  * <p>
  * Request parameters:
  * </p>
  *
  * @author Gabriel Roldan, Axios Engineering
  * @version $Id$
  */
 public class GetFeatureInfoKvpReader extends WmsKvpRequestReader {
     /** DOCUMENT ME! */
     private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.vfny.geoserver.requests.readers.wms");
 
     /** the request wich will be built by getRequest method */
     private GetFeatureInfoRequest request;
 
     /** GetMap request reader used to parse the map context parameters needed. */
     private GetMapKvpReader getMapReader;
 
     /**
      * Creates a new GetMapKvpReader object.
      * @param kvpPairs Key Values pairs of the request
      * @param wms The wms config object.
      */
     public GetFeatureInfoKvpReader(Map kvpPairs, WMS wms) {
         super(kvpPairs, wms);
         getMapReader = new GetMapKvpReader(kvpPairs, wms);
     }
 
     /**
      * Produces a <code>GetMapRequest</code> instance by parsing the GetMap
      * mandatory, optional and custom parameters.
      *
      * @param httpRequest the servlet request who's application object holds
      *        the server configuration
      *
      * @return a <code>GetMapRequest</code> completely setted up upon the
      *         parameters passed to this reader
      *
      * @throws ServiceException DOCUMENT ME!
      * @throws WmsException DOCUMENT ME!
      */
     public Request getRequest(HttpServletRequest httpRequest)
         throws ServiceException {
         //WMService service = (WMService) getServiceRef();
         //WMS wms = service.getWMS();
         request = new GetFeatureInfoRequest(getWMS());
         request.setHttpServletRequest(httpRequest);
 
         String version = getRequestVersion();
         request.setVersion(version);
 
         
         getMapReader.setStylesRequired(false);
         GetMapRequest getMapPart = (GetMapRequest) getMapReader.getRequest(httpRequest);
         
         request.setGetMapRequest(getMapPart);
 
         MapLayerInfo[] layers = parseLayers(getWMS());
         request.setQueryLayers(layers);
 
         String format = getValue("INFO_FORMAT");
 
         if (format == null) {
             //HACK: how we define the default info format?
             //Not sure I understand the question.  If we don't like it here
             //then put it as the default in the FeatureInfoRequest.  If
             //we want to allow users to be able to set it then we can put
             //it as a config parameter in the WMS service section. -ch
             format = "text/plain";
         } else {
             final List formats = org.vfny.geoserver.wms.responses.GetFeatureInfoResponse.getFormats();
             if(!formats.contains(format)) {
                 throw new WmsException("Invalid format '" + format + "', supported formats are " 
                         + formats, "InvalidParameterValue", "info_format");
             }
         }
 
         request.setInfoFormat(format);
 
         request.setFeatureCount(1); // DJB: according to the WMS spec (7.3.3.7 FEATURE_COUNT) this should be 1.  also tested for by cite
 
         try {
             int maxFeatures = Integer.parseInt(getValue("FEATURE_COUNT"));
             request.setFeatureCount(maxFeatures);
         } catch (NumberFormatException ex) {
             //do nothing, FEATURE_COUNT is optional
         }
 
         try {
             int x = Integer.parseInt(getValue("X"));
             int y = Integer.parseInt(getValue("Y"));
             request.setXPixel(x);
             request.setYPixel(y);
         } catch (NumberFormatException ex) {
             throw new WmsException("X and Y incorrectly specified");
         }
 
         String exceptionsFormat = getValue("EXCEPTIONS");
         request.setExeptionFormat(exceptionsFormat);
 
         return request;
     }
 
     /**
      * Obtains the FeatureTypeInfo objects of the layers to query
      * given by the <code>QUERY_LAYERS</code>parameter.
      *
      * @return the list of layers to query.
      *
      * @throws WmsException if the parameter <code>QUERY_LAYERS</code> does not
      * exists, has no layer names, or has at least an invalid layer name.
      */
     private MapLayerInfo[] parseLayers(WMS wms) throws WmsException {
         List layers = readFlat(getValue("QUERY_LAYERS"), INNER_DELIMETER);
         
         // expand base layers, if there is any
         if(wms.getBaseMapLayers() != null) {
             for (int i = 0; i < layers.size(); i++) {
                 String layerGroup = (String) wms.getBaseMapLayers().get(layers.get(i));
                 if(layerGroup != null) {
                     List layerGroupExpanded = readFlat(layerGroup, INNER_DELIMETER);
                     layers.remove(i);
                     layers.addAll(i, layerGroupExpanded);
                 }
             }
         }
         
         // remove coverage layers, we cannot query them
         Data catalog = request.getWMS().getData();
         /*for (Iterator it = layers.iterator(); it.hasNext();) {
             String layerName = (String) it.next();
             if(catalog.getLayerType(layerName) != Data.TYPE_VECTOR)
                 it.remove();
         }*/        
         int layerCount = layers.size();
         if (layerCount == 0) {
             throw new WmsException("No QUERY_LAYERS has been requested, or no queriable layer in the request anyways", getClass().getName());
         }
         MapLayerInfo[] layerInfos = new MapLayerInfo[layerCount];
         String layerName = null;
         for (int i = 0; i < layerCount; i++) {
             layerName = (String) layers.get(i); 
             layerInfos[i] = catalog.getMapLayerInfo(layerName);
         }
 
         return layerInfos;
     }
 }
