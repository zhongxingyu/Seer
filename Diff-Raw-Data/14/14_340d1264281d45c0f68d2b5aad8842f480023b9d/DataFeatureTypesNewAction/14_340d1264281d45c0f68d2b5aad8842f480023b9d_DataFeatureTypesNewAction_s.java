 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 
 package org.vfny.geoserver.action.data;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.geotools.data.DataStore;
 import org.geotools.data.FeatureSource;
 import org.geotools.feature.FeatureType;
 import org.vfny.geoserver.action.ConfigAction;
 import org.vfny.geoserver.config.DataConfig;
 import org.vfny.geoserver.config.DataStoreConfig;
 import org.vfny.geoserver.config.FeatureTypeConfig;
 import org.vfny.geoserver.form.data.DataFeatureTypesNewForm;
 import org.vfny.geoserver.global.UserContainer;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 import java.io.IOException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 /**
  * DataFeatureTypesNewAction purpose.
  * 
  * <p>
  * Description of DataFeatureTypesNewAction ...
  * </p>
  * 
  * <p>
  * Capabilities:
  * </p>
  * 
  * <ul>
  * <li>
  * Feature: description
  * </li>
  * </ul>
  * 
  * <p>
  * Example Use:
  * </p>
  * <pre><code>
  * DataFeatureTypesNewAction x = new DataFeatureTypesNewAction(...);
  * </code></pre>
  *
  * @author rgould, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @version $Id: DataFeatureTypesNewAction.java,v 1.13 2004/03/09 01:37:40 dmzwiers Exp $
  */
 public class DataFeatureTypesNewAction extends ConfigAction {
     public final static String NEW_FEATURE_TYPE_KEY = "newFeatureType";
 
     public ActionForward execute(ActionMapping mapping,
         ActionForm incomingForm, UserContainer user,  HttpServletRequest request,
         HttpServletResponse response) throws IOException {
         DataFeatureTypesNewForm form = (DataFeatureTypesNewForm) incomingForm;
         String selectedNewFeatureType = form.getSelectedNewFeatureType();
 
         DataConfig dataConfig = (DataConfig) request.getSession()
                                                     .getServletContext()
                                                     .getAttribute(DataConfig.CONFIG_KEY);
         int index = selectedNewFeatureType.indexOf(DataConfig.SEPARATOR);
         String dataStoreID = selectedNewFeatureType.substring(0, index);
         String featureTypeName = selectedNewFeatureType.substring(index
                 + DataConfig.SEPARATOR.length());
 
         DataStoreConfig dsConfig = dataConfig.getDataStore(dataStoreID);
         DataStore dataStore = dsConfig.findDataStore(request.getSession().getServletContext());
 
         FeatureType featureType = dataStore.getSchema(featureTypeName);
 
         FeatureTypeConfig ftConfig = new FeatureTypeConfig(dataStoreID,
                 featureType, false);
 
         // What is the Spatial Reference System for this FeatureType?
         // 
         // getDefaultGeometry().getCoordinateSystem() should help but is null
         // getDefaultGeometry().getGeometryFactory() could help, with getSRID(), but it is null
         // 
         // So we will use 0 which means Cartisian Coordinates aka don't know
         //
         // Only other thing we could do is ask for a geometry and see what it's
         // SRID number is?
         //
         ftConfig.setSRS(0);
         
         
         FeatureSource fs = dataStore.getFeatureSource(featureType.getTypeName());
                 
         // TODO translate to lat long, pending
         ftConfig.setLatLongBBox(DataStoreUtils.getBoundingBoxEnvelope(fs));
         
         //Extent ex = featureType.getDefaultGeometry().getCoordinateSystem().getValidArea();
         //ftConfig.setLatLongBBox(ex);
 
        request.getSession().setAttribute(DataConfig.SELECTED_FEATURE_TYPE,
            ftConfig);
         request.getSession().removeAttribute(DataConfig.SELECTED_ATTRIBUTE_TYPE);
 
        return mapping.findForward("config.data.type");
     }
 }
