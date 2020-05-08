 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.wms.kvp;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.geoserver.ows.FlatKvpParser;
 import org.vfny.geoserver.global.CoverageInfo;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.global.MapLayerInfo;
 import org.vfny.geoserver.global.WMS;
 import org.vfny.geoserver.wms.WmsException;
 
 
 public class LayersKvpParser extends FlatKvpParser {
     Data catalog;
     private WMS wms;
 
     public LayersKvpParser(WMS wms) {
         super("layers", MapLayerInfo.class);
         this.wms = wms;
         this.catalog = wms.getData();
     }
 
     protected Object parse(List values) throws Exception {
         MapLayerInfo[] layers;
 
         List layerNames = values;
         List realLayerNames = new ArrayList();
 
         String layerName = null;
 
         int l_counter = 0;
 
         ////
         // Expand the eventually WMS grouped layers into the same WMS Path element
         ////
         for (Iterator it = layerNames.iterator(); it.hasNext();) {
             layerName = (String) it.next();
 
             Integer layerType = catalog.getLayerType(layerName);
 
             if (layerType == null) {
                 if(wms.getBaseMapLayers().containsKey(layerName)) {
                     realLayerNames.add(layerName);
                     l_counter++;
                 } else {
                     ////
                     // Search for grouped layers (attention: heavy process)
                     ////
                     String catalogLayerName = null;
     
                     for (Iterator c_keys = catalog.getLayerNames().iterator(); c_keys.hasNext();) {
                         catalogLayerName = (String) c_keys.next();
     
                         try {
                             FeatureTypeInfo ftype = findFeatureLayer(catalogLayerName);
                             String wmsPath = ftype.getWmsPath();
     
                             if ((wmsPath != null) && wmsPath.matches(".*/" + layerName)) {
                                 realLayerNames.add(catalogLayerName);
                                 l_counter++;
                             }
                        } catch (WmsException e_1) {
                             try {
                                 CoverageInfo cv = findCoverageLayer(catalogLayerName);
                                 String wmsPath = cv.getWmsPath();
     
                                 if ((wmsPath != null) && wmsPath.matches(".*/" + layerName)) {
                                     realLayerNames.add(catalogLayerName);
                                     l_counter++;
                                 }
                            } catch (WmsException e_2) {
                             }
                         }
                     }
                 }
             } else {
                 realLayerNames.add(layerName);
                 l_counter++;
             }
         }
 
         int layerCount = realLayerNames.size();
 
         if (layerCount == 0) {
             throw new WmsException("No LAYERS has been requested", getClass().getName());
         }
 
         layers = new MapLayerInfo[layerCount];
 
         for (int i = 0; i < layerCount; i++) {
             layerName = (String) layerNames.get(i);
             layers[i] = buildMapLayerInfo(layerName);
         }
 
         return layers;
     }
     
     private MapLayerInfo buildMapLayerInfo(String layerName) throws Exception {
         MapLayerInfo li = new MapLayerInfo();
 
         FeatureTypeInfo ftype = findFeatureLayer(layerName);
         if (ftype != null) {
             li.setFeature(ftype);
         } else {
             CoverageInfo cv = findCoverageLayer(layerName);
             if (cv != null) {
                 li.setCoverage(cv);
             } else {
                 if (wms.getBaseMapLayers().containsKey(layerName)) {
                     String styleCsl = (String) wms.getBaseMapStyles().get(layerName);
                     String layerCsl = (String) wms.getBaseMapLayers().get(layerName);
                     LayersKvpParser lparser = new LayersKvpParser(wms);
                     StylesKvpParser sparser = new StylesKvpParser(wms.getData());
                     MapLayerInfo[] layerArray = (MapLayerInfo[]) lparser.parse(layerCsl);
                     List styleList = (List) sparser.parse(styleCsl);
                     li.setBase(layerName, new ArrayList(Arrays.asList(layerArray)), styleList);
                 } else {
                     throw new WmsException("Layer " + layerName + " could not be found");
                 }
             }
         }
         return li;
     }
 
     FeatureTypeInfo findFeatureLayer(String layerName)
         throws WmsException {
         FeatureTypeInfo ftype = null;
         Integer layerType = catalog.getLayerType(layerName);
 
         if (Data.TYPE_VECTOR != layerType) {
             return null;
         } else {
             ftype = catalog.getFeatureTypeInfo(layerName);
         }
 
         return ftype;
     }
 
     CoverageInfo findCoverageLayer(String layerName) throws WmsException {
         CoverageInfo cv = null;
         Integer layerType = catalog.getLayerType(layerName);
 
         if (Data.TYPE_RASTER != layerType) {
             return null;
         } else {
             cv = catalog.getCoverageInfo(layerName);
         }
 
         return cv;
     }
 }
