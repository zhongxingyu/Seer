 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.wms;
 
 import com.vividsolutions.jts.geom.Envelope;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.referencing.CRS;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.NoSuchAuthorityCodeException;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.global.MapLayerInfo;
 import org.vfny.geoserver.wms.requests.DescribeLayerRequest;
 import org.vfny.geoserver.wms.requests.GetFeatureInfoRequest;
 import org.vfny.geoserver.wms.requests.GetLegendGraphicRequest;
 import org.vfny.geoserver.wms.requests.GetMapRequest;
 import org.vfny.geoserver.wms.requests.WMSCapabilitiesRequest;
 import org.vfny.geoserver.wms.responses.DescribeLayerResponse;
 import org.vfny.geoserver.wms.responses.GetFeatureInfoResponse;
 import org.vfny.geoserver.wms.responses.GetLegendGraphicResponse;
 import org.vfny.geoserver.wms.responses.GetMapResponse;
 import org.vfny.geoserver.wms.responses.WMSCapabilitiesResponse;
 import org.vfny.geoserver.wms.servlets.Capabilities;
 import org.vfny.geoserver.wms.servlets.DescribeLayer;
 import org.vfny.geoserver.wms.servlets.GetFeatureInfo;
 import org.vfny.geoserver.wms.servlets.GetLegendGraphic;
 import org.vfny.geoserver.wms.servlets.GetMap;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 
 public class DefaultWebMapService implements WebMapService, ApplicationContextAware {
     /**
      * default for 'format' parameter.
      */
     public static String FORMAT = "image/png";
 
     /**
      * default for 'styles' parameter.
      */
     public static List STYLES = Collections.EMPTY_LIST;
 
     /**
      * default for 'height' parameter.
      */
     public static int HEIGHT = 512;
 
     /**
      * default for 'height' parameter.
      */
     public static int WIDTH = 512;
 
     /**
      * default for 'srs' parameter.
      */
     public static String SRS = "EPSG:4326";
 
     /**
      * default for 'transparent' parameter.
      */
     public static Boolean TRANSPARENT = Boolean.TRUE;
 
     /**
      * default for 'bbox' paramter
      */
     public static ReferencedEnvelope BBOX = new ReferencedEnvelope(new Envelope(-180, 180, -90, 90),
             DefaultGeographicCRS.WGS84);
     
     
     /**
      * The current getMap request
      */
     GetMapRequest getMap = null;
     
     /**
      * Application context
      */
     ApplicationContext context;
 
     public void setApplicationContext(ApplicationContext context)
         throws BeansException {
         this.context = context;
     }
 
     public WMSCapabilitiesResponse getCapabilities(WMSCapabilitiesRequest request) {
         Capabilities capabilities = (Capabilities) context.getBean("wmsGetCapabilities");
 
         return (WMSCapabilitiesResponse) capabilities.getResponse();
     }
 
     public WMSCapabilitiesResponse capabilities(WMSCapabilitiesRequest request) {
         return getCapabilities(request);
     }
 
     public DescribeLayerResponse describeLayer(DescribeLayerRequest request) {
         DescribeLayer describeLayer = (DescribeLayer) context.getBean("wmsDescribeLayer");
 
         return (DescribeLayerResponse) describeLayer.getResponse();
     }
 
     public GetMapResponse getMap(GetMapRequest request) {
         GetMap getMap = (GetMap) context.getBean("wmsGetMap");
 
         return (GetMapResponse) getMap.getResponse();
     }
 
     public GetMapResponse map(GetMapRequest request) {
         return getMap(request);
     }
 
     public GetFeatureInfoResponse getFeatureInfo(GetFeatureInfoRequest request) {
         GetFeatureInfo getFeatureInfo = (GetFeatureInfo) context.getBean("wmsGetFeatureInfo");
 
         return (GetFeatureInfoResponse) getFeatureInfo.getResponse();
     }
 
     public GetLegendGraphicResponse getLegendGraphic(GetLegendGraphicRequest request) {
         GetLegendGraphic getLegendGraphic = (GetLegendGraphic) context.getBean("wmsGetLegendGraphic");
 
         return (GetLegendGraphicResponse) getLegendGraphic.getResponse();
     }
 
     //refector operations
     public GetMapResponse reflect(GetMapRequest request) {
         return getMapReflect(request);
     }
 
     public GetMapResponse getMapReflect(GetMapRequest request) {
         getMap = (GetMapRequest) request;
 
         //set the defaults
         if (getMap.getFormat() == null) {
             getMap.setFormat(FORMAT);
         }
         
         if ((getMap.getStyles() == null) || getMap.getStyles().isEmpty()) {
             //set styles to be the defaults for the specified layers
             //TODO: should this be part of core WMS logic? is so lets throw this
             // into the GetMapKvpRequestReader
             if ((getMap.getLayers() != null) && (getMap.getLayers().length > 0)) {
                 ArrayList styles = new ArrayList(getMap.getLayers().length);
 
                 for (int i = 0; i < getMap.getLayers().length; i++) {
                     styles.add(getMap.getLayers()[i].getDefaultStyle());
                 }
 
                 getMap.setStyles(styles);
             } else {
                 getMap.setStyles(STYLES);
             }
         }
         
         this.autoSetBoundsAndSize();
         
         return getMap(getMap);
     }
     
     /**
      * This method tries to automatically determine SRS, bounding box and output
      *  size based on the layers provided by the user and any other parameters.
      * 
      * If all layers use the same SRS, and the user does not specify otherwise, 
      * that SRS will be returned. If they differ, EPSG:4326 will be used. An 
      * exception is thrown if the user specifies an SRS that is not shared by all
      * layers and is not EPSG:4326.
      * 
      * If bounds are not specified by the user, they are automatically se to the
      * union of the bounds of all layers. 
      * 
      * The size of the output image defaults to 512 pixels, the height is
      * automatically determined based on the width to height ratio of the 
      * requested layers. This is also true if either height or width are
      * specified by the user. If both height and width are specified by the user,
      * the automatically determined bounding box will be adjusted to fit inside 
      * these bounds.
      */
     private void autoSetBoundsAndSize() {
     	// Get the layers
         MapLayerInfo[] layers = getMap.getLayers();
         
    	 	// Determine the SRS first, to keep the code readable
         String reqSRS = getMap.getSRS();
         boolean specifiedSRS = (reqSRS != null);
         boolean force4326 = (!specifiedSRS || reqSRS.equals("EPSG:4326"));
         
 		for(int i=0; specifiedSRS && !force4326 && i<layers.length; i++) {
 			FeatureTypeInfo fti = layers[i].getFeature();
 			String curSRS = "EPSG:"+fti.getSRS();
 			
 			// Compare to the specified SRS
 			if(! reqSRS.equals(curSRS)) {
 		    	throw new RuntimeException(
 		    			"Cannot force SRS "+reqSRS+" when native is "+ curSRS
 		    			+", try not specifying the SRS.");
 			}
 		}
 		
 		if(force4326) {
 			getMap.setSRS("EPSG:4326");
 			try {
 				getMap.setCrs(CRS.decode("EPSG:4326"));
 			} catch (NoSuchAuthorityCodeException e) {
 				e.printStackTrace();
 			} catch (FactoryException e) {
 				e.printStackTrace();
 			}
 		} else {
 			//Already set getMap.setSRS(reqSRS);
 		}
 		
 		// Ready to determine the bounds based on the layers, if not specified
         Envelope layerbbox = null;
 
         boolean specifiedBBOX = (getMap.getBbox() != null);
         if(specifiedBBOX) {
         	layerbbox = getMap.getBbox();		
         } else {
         	// Get the bounding box from the layers
         	for(int i=0; i<layers.length; i++) {
         		Envelope curbbox = null;
         		try {
         			if(force4326) {
         				curbbox = layers[i].getFeature().getLatLongBoundingBox();
         			} else {
         				curbbox = layers[i].getFeature().getBoundingBox();
         			}
         		} catch(IOException e) {
         			// Do nothing
         		}
         		if(i == 0) {
         			layerbbox = curbbox;
         		} else {
         			layerbbox.expandToInclude(curbbox);
         		}
         	}
         }
        
         // Just in case
         if(layerbbox == null) layerbbox = BBOX;
         
         double bbheight = layerbbox.getHeight();
         double bbwidth = layerbbox.getWidth();
         double bbratio = bbwidth/bbheight;
        
        if(!specifiedBBOX) {
        	// Zoom out 5%, accomodates for reprojection etc
        	layerbbox.expandBy(layerbbox.getWidth()/40,layerbbox.getHeight()/40);
    	}
        
        
         double mheight = getMap.getHeight();
         double mwidth = getMap.getWidth();
        
         if(mheight > 0.5 && mwidth > 0.5 && specifiedBBOX) {
         	// This person really doesnt want our help
         } else {
         	if(mheight > 0.5 && mwidth > 0.5) {
         		// Fully specified, need to adjust bbox
         		double mratio = mwidth/mheight;
         		// Adjust bounds to be less than ideal to meet spec
         		if(bbratio > mratio) {
         			// Too wide, need to increase height of bb
         			double diff = ((bbwidth / mratio ) - bbheight)/2;
         			layerbbox.expandBy(0, diff);
         		} else {
         			// Too tall, need to increase width of bb
         			double diff = ((bbheight * mratio ) - bbwidth)/2;
         			layerbbox.expandBy(diff, 0);
         		}
         	} else if(mheight > 0.5) {
         		mwidth = bbratio * mheight;
         	} else { 
         		if(mwidth > 0.5) {
         			// We're set
         		} else {
         			// Fall through to the default
         			mwidth = WIDTH;
         		}
         		mheight = mwidth / bbratio;
         	}
         	

         	// Actually set the bounding box and size of image
         	getMap.setBbox(layerbbox);    
         	getMap.setWidth((int)mwidth);
         	getMap.setHeight((int)mheight);
         }
     }     
 }
