 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.responses.map.georss;
 
 import org.geoserver.wms.util.WMSRequests;
 import org.geotools.data.FeatureSource;
 import org.geotools.feature.Feature;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureIterator;
 import org.geotools.map.MapLayer;
 import org.geotools.xml.transform.Translator;
 import org.vfny.geoserver.util.Requests;
 import org.vfny.geoserver.wms.WMSMapContext;
 import org.vfny.geoserver.wms.responses.featureInfo.FeatureTemplate;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.helpers.AttributesImpl;
 import java.io.IOException;
 import java.util.logging.Level;
 
 
 /**
  * Encodes an RSS feed tagged with geo information.
  *
  * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
  *
  */
 public class RSSGeoRSSTransformer extends GeoRSSTransformerBase {
     public Translator createTranslator(ContentHandler handler) {
         return new RSSGeoRSSTranslator(handler);
     }
 
     class RSSGeoRSSTranslator extends GeoRSSTranslatorSupport {
         public RSSGeoRSSTranslator(ContentHandler contentHandler) {
             super(contentHandler, null, null);
         }
 
         public void encode(Object o) throws IllegalArgumentException {
             WMSMapContext map = (WMSMapContext) o;
 
             AttributesImpl atts = new AttributesImpl();
             atts.addAttribute(null, "version", "version", null, "2.0");
 
             start("rss", atts);
             start("channel");
 
             StringBuffer title = new StringBuffer();
 
             for (int i = 0; i < map.getLayerCount(); i++) {
                 MapLayer layer = map.getLayer(i);
                 title.append(layer.getTitle()).append(",");
             }
             title.setLength(title.length()-1);
             
             element( "title", title.toString() );
             
             start( "link" );
             
             cdata(WMSRequests.getGetMapUrl(map.getRequest(),null,null,null));
             end( "link" );
             
             //element( "description", "description" );
 
             //items
             try {
                 encodeItems(map);
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
 
             end("channel");
             end("rss");
         }
 
         void encodeItems(WMSMapContext map) throws IOException {
             for (int i = 0; i < map.getLayerCount(); i++) {
                 MapLayer layer = map.getLayer(i);
 
                 FeatureCollection features = null;
 
                 try {
                     FeatureSource source = layer.getFeatureSource();
                     features = source.getFeatures();
                 } catch (Exception e) {
                     String msg = "Unable to encode map layer: " + layer;
                     LOGGER.log(Level.SEVERE, msg, e);
                 }
 
                 if (features != null) {
                     FeatureIterator iterator = null;
 
                     try {
                         iterator = features.features();
 
                         while (iterator.hasNext()) {
                             encodeItem(iterator.next(), map, layer);
                         }
                     } finally {
                         if (iterator != null) {
                             features.close(iterator);
                         }
                     }
                 }
             }
         }
 
         void encodeItem(Feature feature, WMSMapContext map, MapLayer layer)
             throws IOException {
             start("item");
 
             FeatureTemplate template = new FeatureTemplate();
 
            element("title", template.title(feature));

             //create the link as getFeature request with fid filter
             //TODO: throw this into a utility class
             //TODO: use an html based output format
             String link = Requests.getBaseUrl(map.getRequest().getHttpServletRequest(),
                     map.getRequest().getGeoServer());
             link += ("wfs?request=getfeature&service=wfs&version=1.0.0&featureid="
             + feature.getID());
 
             start("link");
             cdata(link);
             end("link");
 
             //element( "description", template.execute(feature));
            start("description");
            cdata(template.description(feature));
            end("description");

             encodeGeometry(feature);
 
             end("item");
         }
     }
 }
