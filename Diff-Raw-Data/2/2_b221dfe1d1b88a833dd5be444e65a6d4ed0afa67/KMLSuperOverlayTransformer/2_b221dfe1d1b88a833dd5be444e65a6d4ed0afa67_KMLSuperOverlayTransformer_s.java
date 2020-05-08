 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.responses.map.kml;
 
 import java.util.Arrays;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.namespace.QName;
 
 import org.geoserver.ows.HttpErrorCodeException;
 import org.geotools.data.FeatureSource;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.map.MapLayer;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.geotools.xml.transform.Translator;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.vfny.geoserver.global.MapLayerInfo;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.wms.WMSMapContext;
 import org.vfny.geoserver.wms.WmsException;
 import org.xml.sax.ContentHandler;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 
 public class KMLSuperOverlayTransformer extends KMLTransformerBase {
     /**
      * logger
      */
     static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.kml");
 
     /**
      * The map context
      */
     final WMSMapContext mapContext;
 
     public KMLSuperOverlayTransformer(WMSMapContext mapContext) {
         this.mapContext = mapContext;
         setNamespaceDeclarationEnabled(false);
     }
 
     public Translator createTranslator(ContentHandler handler) {
         return new KMLSuperOverlayerTranslator(handler);
     }
 
     class KMLSuperOverlayerTranslator extends KMLTranslatorSupport {
         public KMLSuperOverlayerTranslator(ContentHandler contentHandler) {
             super(contentHandler);
         }
 
         public void encode(Object o) throws IllegalArgumentException {
             MapLayer mapLayer = (MapLayer) o;
 
             //calculate closest resolution
             ReferencedEnvelope extent = mapContext.getAreaOfInterest();
             
             //zoom out until the entire bounds requested is covered by a 
             //single tile
             Envelope top = KMLUtils.expandToTile(extent);
             int i = KMLUtils.findZoomLevel(extent);
 
 
             LOGGER.fine("request = " + extent);
             LOGGER.fine("top level = " + top);
 
             //start document
             if (isStandAlone()) {
                 start( "kml" );
             }
             
             start("Document");
 
             //encode top level region
             encodeRegion(top, 256, -1);
 
             //encode the network links
             if (top != KMLUtils.WORLD_BOUNDS_WGS84) {
                 //top left
                 Envelope e00 = new Envelope(top.getMinX(), top.getMinX() + (top.getWidth() / 2d),
                         top.getMaxY() - (top.getHeight() / 2d), top.getMaxY());
 
                 //top right
                 Envelope e01 = new Envelope(e00.getMaxX(), top.getMaxX(), e00.getMinY(),
                         e00.getMaxY());
 
                 //bottom left
                 Envelope e10 = new Envelope(e00.getMinX(), e00.getMaxX(), top.getMinY(),
                         e00.getMinY());
 
                 //bottom right
                 Envelope e11 = new Envelope(e01.getMinX(), e01.getMaxX(), e10.getMinY(),
                         e10.getMaxY());
 
                 encodeNetworkLink(e00, "00", mapLayer);
                 encodeNetworkLink(e01, "01", mapLayer);
                 encodeNetworkLink(e10, "10", mapLayer);
                 encodeNetworkLink(e11, "11", mapLayer);
             } else {
                 //divide up horizontally by two
                 Envelope e0 = new Envelope(top.getMinX(), top.getMinX() + (top.getWidth() / 2d),
                         top.getMinY(), top.getMaxY());
                 Envelope e1 = new Envelope(e0.getMaxX(), top.getMaxX(), top.getMinY(), top.getMaxY());
 
                 encodeNetworkLink(e0, "0", mapLayer);
                 encodeNetworkLink(e1, "1", mapLayer);
             }
 
             //encode the ground overlay(s)
             if (top == KMLUtils.WORLD_BOUNDS_WGS84) {
                 //special case for top since it does not line up as a proper
                 //tile -> split it in two
                 encodeTileForViewing(mapLayer, i, new Envelope(-180, 0, -90, 90));
                 encodeTileForViewing(mapLayer, i, new Envelope(0, 180, -90, 90));
             } else {
                 //encode straight up
                 encodeTileForViewing(mapLayer, i, top);
             }
 
             //end document
             end("Document");
             
             if (isStandAlone()) {
                 end( "kml" );
             }
         }
 
         void encodeTileForViewing(MapLayer mapLayer, int drawOrder, Envelope box){
             if (shouldDrawVectorLayer(mapLayer, box))
                 encodeKMLLink(mapLayer, drawOrder, box);
             if (shouldDrawWMSOverlay(mapLayer, box))
                 encodeGroundOverlay(mapLayer, drawOrder, box);
         }
 
         private boolean shouldDrawVectorLayer(MapLayer layer, Envelope box){
             // should draw as vector if the layer is a vector layer, and based on mode
             // full: yes, if any regionated vectors are present at this zoom level
             // background: yes, if any regionated vectors are present at this zoom level
             // overview: is the non-regionated feature count for this tile below the cutoff?
             // raster: no
             if (!isVectorLayer(layer)) return false;
 
             String regionateMode = (String)mapContext.getRequest().getFormatOptions().get("regionateMode");
 
             if ("raster".equals(regionateMode)) return false;
 
             if ("overview".equals(regionateMode)) {
                 // the sixteen here is mostly arbitrary, designed to indicate a couple of regionated levels above the bottom of the hierarchy
                 return featuresInTile(layer, box, false) <= getFeatureTypeInfo(layer).getRegionateFeatureLimit(); 
             }
 
             return featuresInTile(layer, box, true) > 0;
         }
 
         private boolean shouldDrawWMSOverlay(MapLayer layer, Envelope box){
             // should draw based on the mode:
             // full: no
             // background: yes
             // overview: is the non-regionated feature count for this tile above the cutoff?
             String regionateMode = (String)mapContext.getRequest().getFormatOptions().get("regionateMode");
             if ("background".equals(regionateMode) || "raster".equals(regionateMode)) return true;
             if ("overview".equals(regionateMode))
                 return featuresInTile(layer, box, false) > getFeatureTypeInfo(layer).getRegionateFeatureLimit();
 
             return false;
         }
 
         void encodeKMLLink(MapLayer mapLayer, int drawOrder, Envelope box){
             String regionateMode = (String)mapContext.getRequest().getFormatOptions().get("regionateMode");
             if ("overview".equalsIgnoreCase(regionateMode)){
                 start("NetworkLink");
                 element("visibility", "1");
                 start("Link");
                 element("href", KMLUtils.getMapUrl(
                             mapContext,
                             mapLayer,
                             0,
                             box,
                             new String[] { 
                             "width", "256",
                             "height", "256",
                             "format_options", "",
                             "superoverlay", "false"
                             },
                             true
                             )
                        );
                 end("Link");
                 encodeRegion(box, 128, -1);
                 end("NetworkLink");
             } else {
                 start("NetworkLink");
                 element("visibility", "1");
                 start("Link");
                 element("href", KMLUtils.getMapUrl(
                             mapContext,
                             mapLayer,
                             0,
                             box,
                             new String[] { 
                             "width", "256",
                             "height", "256",
                             "format_options", "regionateBy:auto"
                             },
                             true
                             )
                        );
                 end("Link");
                 encodeRegion(box, 128, -1);
                 end("NetworkLink");
             }
         }
 
         boolean isVectorLayer(MapLayer layer){
             int index = Arrays.asList(mapContext.getLayers()).indexOf(layer);
             MapLayerInfo info = mapContext.getRequest().getLayers()[index];
             return (info.getType() == MapLayerInfo.TYPE_VECTOR 
                     || info.getType() == MapLayerInfo.TYPE_REMOTE_VECTOR);
         }
 
         private FeatureTypeInfo getFeatureTypeInfo(MapLayer layer){
             for (MapLayerInfo info : mapContext.getRequest().getLayers()) 
                 if (info.getName().equals(layer.getTitle()))
                     return info.getFeature();
             return null;
         }
 
         private int featuresInTile(MapLayer mapLayer, Envelope bounds, boolean regionate){
             if (!isVectorLayer(mapLayer)) return 1; // for coverages, we want raster tiles everywhere
             Envelope originalBounds = mapContext.getRequest().getBbox();
             mapContext.getRequest().setBbox(bounds);
             mapContext.setAreaOfInterest(bounds);
 
             String originalRegionateBy = null;
             if (regionate){
                 originalRegionateBy = 
                     (String)mapContext.getRequest().getFormatOptions().get("regionateby");
                 if (originalRegionateBy == null)
                     mapContext.getRequest().getFormatOptions().put("regionateby","auto");
             }
 
             int numFeatures = 0;
 
             try{
                 numFeatures = 
                     (KMLUtils.loadFeatureCollection(
                         (FeatureSource<SimpleFeatureType, SimpleFeature>) mapLayer.getFeatureSource(),
                         mapLayer,
                         mapContext
                         ).size()/* == 0*/);
             } catch (WmsException e) {
                 LOGGER.severe("Caught the WmsException!");
                 numFeatures = -1;
             } catch (HttpErrorCodeException e) {
                 if (e.getErrorCode() == 204){
                     mapContext.getRequest().getFormatOptions().remove("regionateby");
                     numFeatures = 0;
                 } else {
                     LOGGER.log(
                             Level.WARNING,
                             "Failure while checking whether a regionated child tile " +
                             "contained features!",
                             e
                       );
                 }
             } catch (Exception e){
                 // Probably just trying to regionate a raster layer...
                 LOGGER.log(
                         Level.WARNING,
                         "Failure while checking whether a regionated child tile contained features!",
                         e
                   );
             }
 
             mapContext.getRequest().setBbox(originalBounds);
             mapContext.setAreaOfInterest(originalBounds);
             if (regionate && originalRegionateBy == null){
                 mapContext.getRequest().getFormatOptions().remove("regionateby");
             }
 
             return numFeatures;
         }
 
         void encodeGroundOverlay(MapLayer mapLayer, int drawOrder, Envelope box) {
             start("GroundOverlay");
             element("drawOrder", "" + drawOrder);
 
             start("Icon");
 
             String href = KMLUtils.getMapUrl(mapContext, mapLayer, 0, box,
                     new String[] { 
                         "width", "256", 
                         "height", "256", 
                         "format", "image/png",
                         "transparent", "true"
                     },
                     true
                     );
             element("href", href);
             LOGGER.fine(href);
             end("Icon");
             encodeRegion(box, 256, 512);
 
             encodeLatLonBox(box);
             end("GroundOverlay");
         }
 
         void encodeRegion(Envelope box, int minLodPixels, int maxLodPixels) {
             //top level region
             start("Region");
 
             start("Lod");
             element("minLodPixels", "128");
             //element("minLodPixels", "" + minLodPixels );
             element("maxLodPixels", "" + maxLodPixels);
             //element("maxLodPixels", "-1" );
             end("Lod");
 
             encodeLatLonAltBox(box);
 
             end("Region");
         }
 
         void encodeNetworkLink(Envelope box, String name, MapLayer mapLayer) {
             start("NetworkLink");
             element("name", name);
 
             encodeRegion(box, 512, -1);
 
             start("Link");
 
             String getMap = KMLUtils.getMapUrl(mapContext, mapLayer, 0, box,
                     new String[] {
                         "format", KMLMapProducer.MIME_TYPE, "width", "256", "height", "256",
                         "superoverlay", "true"
                     }, false);
 
             element("href", getMap);
             LOGGER.fine("Network link " + name + ":" + getMap);
 
             element("viewRefreshMode", "onRegion");
 
             end("Link");
 
             end("NetworkLink");
         }
 
         void encodeLatLonAltBox(Envelope box) {
             start("LatLonAltBox");
             encodeBox(box);
             end("LatLonAltBox");
         }
 
         void encodeLatLonBox(Envelope box) {
             start("LatLonBox");
             encodeBox(box);
             end("LatLonBox");
         }
 
         void encodeBox(Envelope box) {
             element("north", String.valueOf(box.getMaxY()));
             element("south", String.valueOf(box.getMinY()));
             element("east", String.valueOf(box.getMaxX()));
             element("west", String.valueOf(box.getMinX()));
         }
     }
 }
