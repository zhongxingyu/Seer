 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.responses.wms.map.svg;
 
 import com.vividsolutions.jts.geom.Envelope;
 import org.geotools.data.FeatureResults;
 import org.geotools.styling.Style;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.requests.wms.GetMapRequest;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.List;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author Gabriel Roldn
  * @version $Id: EncoderConfig.java,v 1.3 2004/04/06 12:12:18 cholmesny Exp $
  */
 public class EncoderConfig {
     /** the XML and SVG header */
     public static final String SVG_HEADER =
         "<?xml version=\"1.0\" standalone=\"no\"?>\n\t"
         + "<!DOCTYPE svg \n\tPUBLIC \"-//W3C//DTD SVG 20001102//EN\" \n\t\"http://www.w3.org/TR/2000/CR-SVG-20001102/DTD/svg-20001102.dtd\">\n"
        + "<svg xmlns=\"http://www.w3.org/2000/svg\" \n\tstroke=\"green\" \n\tfill=\"none\" \n\tstroke-width=\"0.001%\" \n\twidth=\"_width_\" \n\theight=\"_height_\" \n\tviewBox=\"_viewBox_\" \n\tpreserveAspectRatio=\"xMidYMid meet\">\n";
 
     /** the SVG closing element */
     public static final String SVG_FOOTER = "</svg>\n";
     private GetMapRequest request;
     private FeatureTypeInfo[] requestedLayers;
     private FeatureResults[] resultLayers;
     private Style[] styles;
 
     /**
      * tells whether all the geometries in FeatureResults will be written as a
      * single SVG graphic element. If this field is set to true, overrides the
      * writting of attributes and ids. It is usefull to significantly reduce
      * the size of the resulting SVG content when no other data than the
      * graphics is needed for a single layer
      */
     private boolean collectGeometries = false;
 
     /** the minimun distance beteen encoded points */
     private double minCoordDistance = -1;
 
     /**
      * defaults to <code>true</code>, and means if the xml header and svg
      * element should be printed. Some applications may need to not get that
      * encoded to directly parse and add the content to a working client. This
      * field is setted thru the SVGHEADER custom request parameter
      */
     private boolean writeHeader = true;
 
     /**
      * Creates a new EncoderConfig object.
      *
      * @param request DOCUMENT ME!
      * @param requestedLayers DOCUMENT ME!
      * @param resultLayers DOCUMENT ME!
      * @param styles DOCUMENT ME!
      */
     public EncoderConfig(GetMapRequest request,
         FeatureTypeInfo[] requestedLayers, FeatureResults[] resultLayers,
         Style[] styles) {
         this.request = request;
         this.requestedLayers = requestedLayers;
         this.resultLayers = resultLayers;
         this.styles = styles;
     }
 
     public List getAttributes(String typeName) throws IOException {
         List atts = Collections.EMPTY_LIST;
 
         if (!request.getAttributes().isEmpty()) {
             int layerIndex = -1;
             int lCount = requestedLayers.length;
 
             for (int i = 0; i < lCount; i++) {
                 if (requestedLayers[i].getFeatureType().getTypeName().equals(typeName)) {
                     layerIndex = i;
 
                     break;
                 }
             }
 
             atts = (List) request.getAttributes().get(layerIndex);
         }
 
         return atts;
     }
 
     public FeatureTypeInfo[] getLayers() {
         return requestedLayers;
     }
 
     public FeatureResults[] getResults() {
         return resultLayers;
     }
 
     public Style[] getStyles() {
         return styles;
     }
 
     public GetMapRequest getRequest() {
         return request;
     }
 
     /**
      * the referene space is an Envelope object wich is used to translate Y
      * coordinates to an SVG viewbox space. It is necessary due to the
      * different origin of Y coordinates in SVG space and in Coordinates space
      *
      * @return DOCUMENT ME!
      */
     public Envelope getReferenceSpace() {
         return request.getBbox();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public double getMinCoordDistance() {
         if (minCoordDistance == -1) {
             double blurFactor = request.getGeneralizationFactor();
 
             if (blurFactor > 0) {
                 Envelope env = getReferenceSpace();
                 double maxDimension = Math.max(env.getWidth(), env.getHeight());
                 this.minCoordDistance = maxDimension / blurFactor;
             } else {
                 this.minCoordDistance = 0;
             }
         }
 
         return minCoordDistance;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public int getMapHeight() {
         return request.getHeight();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public int getMapWidth() {
         return request.getWidth();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public boolean isCollectGeometries() {
         return request.isCollectGeometries();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public boolean isWriteHeader() {
         return request.getWriteSvgHeader();
     }
 }
