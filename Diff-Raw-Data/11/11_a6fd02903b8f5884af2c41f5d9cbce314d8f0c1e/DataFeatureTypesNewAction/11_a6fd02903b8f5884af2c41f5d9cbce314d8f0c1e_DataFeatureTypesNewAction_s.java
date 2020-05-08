 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 
 package org.vfny.geoserver.action.data;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.geotools.data.DataStore;
 import org.geotools.data.FeatureSource;
 import org.geotools.feature.FeatureType;
 import org.opengis.metadata.Identifier;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.vfny.geoserver.action.ConfigAction;
 import org.vfny.geoserver.config.DataConfig;
 import org.vfny.geoserver.config.DataStoreConfig;
 import org.vfny.geoserver.config.FeatureTypeConfig;
 import org.vfny.geoserver.form.data.DataFeatureTypesNewForm;
 import org.vfny.geoserver.global.UserContainer;
 
 
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
  * @author $Author: cholmesny $ (last modification)
  * @version $Id: DataFeatureTypesNewAction.java,v 1.15 2004/09/17 16:34:47 cholmesny Exp $
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
 
         FeatureTypeConfig ftConfig;
         //JD: GEOS-399, wrap rest of method in try catch block in order to 
         // report back nicely to app
 		try {
 			FeatureType featureType = dataStore.getSchema(featureTypeName);
 
 			ftConfig = new FeatureTypeConfig(dataStoreID,
 			        featureType, false);
 
 			// DJB: this comment looks old - SRS support is much better now.  
 			//     TODO: delete this comment (but wait a bit)
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
 			
 			// attempt to get a better SRS
 			try {
 				CoordinateReferenceSystem crs = featureType.getDefaultGeometry().getCoordinateSystem();
             if (crs != null) {
 				Set idents = crs.getIdentifiers();
 				Iterator it = idents.iterator();
 				while (it.hasNext())
 				{
 					Identifier id = (Identifier) it.next();
 					if (id.toString().indexOf("EPSG:") != -1)    // this should probably use the Citation, but this is easier!
 					{
 						//we have an EPSG #, so lets use it!
 						String str_num = id.toString().substring(id.toString().indexOf(':')+1);
 						int num = Integer.parseInt(str_num);
 						ftConfig.setSRS(num);
 						break;  // take the first EPSG
 					}
 				}
             }
 			}catch(Exception e)
 			{
 				e.printStackTrace(); // not a big deal - we'll default to 0.
 			}
 			
 			
 			FeatureSource fs = dataStore.getFeatureSource(featureType.getTypeName());
 			        
 			// TODO translate to lat long, pending
 			//This should not be done by default, as it is an expensive operation.
 			//especially for very large tables.  User may know it, if not he
 			//can hit the generate button (which is why it's there).
 			//ftConfig.setLatLongBBox(DataStoreUtils.getBoundingBoxEnvelope(fs));
 			
 			//Extent ex = featureType.getDefaultGeometry().getCoordinateSystem().getValidArea();
 			//ftConfig.setLatLongBBox(ex);
 		} 
 		catch (IOException e) {
 			e.printStackTrace();
 			
 			ActionErrors errors = new ActionErrors();
             errors.add(ActionErrors.GLOBAL_ERROR,
                 new ActionError("error.exception", e.getMessage()));
 
             saveErrors(request, errors);
 
             return mapping.findForward("config.data.type.new");
 		}
 
         request.getSession().setAttribute(DataConfig.SELECTED_FEATURE_TYPE, ftConfig);
         request.getSession().removeAttribute(DataConfig.SELECTED_ATTRIBUTE_TYPE);
 
         user.setFeatureTypeConfig( ftConfig );
         return mapping.findForward("config.data.type.editor");
     }
     
     
 }
