 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.wms.util;
 
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Array;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.geoserver.config.GeoServer;
 import org.geoserver.ows.util.KvpUtils;
 import org.geoserver.platform.ServiceException;
 import org.geoserver.wms.MapLayerInfo;
 import org.geoserver.wms.WMS;
 import org.geotools.map.MapLayer;
 import org.geotools.styling.Style;
 import org.vfny.geoserver.util.Requests;
 import org.vfny.geoserver.wms.WMSMapContext;
 import org.vfny.geoserver.wms.requests.GetMapRequest;
 import org.vfny.geoserver.wms.requests.WMSRequest;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 /**
  * Utility class for creating wms requests.
  *
  * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
  * @see Requests
  */
 public class WMSRequests {
     /**
      * Returns the base url for a wms request, something of the form:
      * <pre>&lt;protocol>://&lt;server>:&lt;port>/&lt;context>/wms</pre>
      *
      * @param request The request.
      * @param geoServer GeoServer configuration.
      *
      * @return The base for wms requests.
      */
     public static String getBaseUrl( HttpServletRequest request, WMS config ) {
         String baseUrl = Requests.getBaseUrl( request, config.getGeoServer() );
         baseUrl = Requests.appendContextPath(baseUrl, "wms" );
         
         return baseUrl;
     }
 
     /**
      * Returns the base url for a wms request, something of the form:
      * <pre>&lt;protocol>://&lt;server>:&lt;port>/&lt;context>/wms</pre>
      *
      * @param req The wms request.
      * 
      * @return The base for wms requests.
      */
     public static String getBaseUrl( WMSRequest request ) {
         return getBaseUrl( request.getHttpServletRequest(), request.getWMS() );  
     }
     
     /**
      * Returns the base url for a wms request, something of the form:
      * <pre>&lt;protocol>://&lt;server>:&lt;port>/&lt;context>/wms</pre>
      * 
      * @param map A wms map context.
      *
      * @return The base for wms requests.
      */
     public static String getBaseUrl( WMSMapContext map ) {
         return getBaseUrl( map.getRequest() );
     }
 
     /**
      * Encodes the url of a GetMap request pointing to a tile cache if one
      * exists.
      * <p>
      * The tile cache location is determined from {@link GeoServer#getTileCache()}.
      * If the above method returns null this method falls back to the behaviour
      * of {@link #getGetMapUrl(WMSMapContext, MapLayer, Envelope, String[])}.
      * </p>
      * <p>
      * If the <tt>layer</tt> argument is <code>null</code>, the request is
      * made including all layers in the <tt>mapContexT</tt>.
      * </p>
      * <p>
      * If the <tt>bbox</tt> argument is <code>null</code>. {@link WMSMapContext#getAreaOfInterest()}
      * is used for the bbox parameter.
      * </p>
      *
      * @param req The getMap request.
      * @param layer The Map layer, may be <code>null</code>.
      * @param layerIndex The index of the layer in the request
      * @param bbox The bounding box of the request, may be <code>null</code>.
      * @param kvp Additional or overidding kvp parameters, may be <code>null</code>
      *
      * @return The full url for a getMap request.
      */
     public static String getTiledGetMapUrl(GetMapRequest req, MapLayer layer, int layerIndex, Envelope bbox, String[] kvp) {
         String baseUrl = getTileCacheBaseUrl(req.getHttpServletRequest(),
                     req.getWMS().getGeoServer());
         
         if ( baseUrl == null ) {
             return getGetMapUrl( req, layer, layerIndex, bbox, kvp );
 
         }
         
         return getGetMapUrl( baseUrl, req, layer.getTitle(), layerIndex, layer.getStyle().getName(), bbox, kvp );
     }
     
     /**
      * Returns the full url to the tile cache used by GeoServer ( if any ).
      * <p>
      * If the tile cache set in the configuration ({@link GeoServer#getTileCache()})
      * is set to an asbsolute url, it is simply returned. Otherwise the value
      * is appended to the scheme and host of the supplied <tt>request</tt>.
      * </p>
      * @param request The request.
      * @param geoServer The geoserver configuration.
      *
      * @return The url to the tile cache, or <code>null</code> if no tile
      * cache set.
      */
     private static String getTileCacheBaseUrl(HttpServletRequest request, GeoServer geoServer) {
         //first check if tile cache set
         String tileCacheBaseUrl = (String) geoServer.getGlobal().getMetadata().get( "tileCache");
 
         if (tileCacheBaseUrl != null) {
             //two possibilities, local path, or full remote path
             try {
                 new URL(tileCacheBaseUrl);
 
                 //full url, return it
                 return tileCacheBaseUrl;
             } catch (MalformedURLException e1) {
                 //try relative to the same host as request
                 try {
                     String url = Requests.appendContextPath(request.getScheme() + "://" + request.getServerName(),
                             tileCacheBaseUrl);
                     new URL(url);
 
                     //cool return it
                     return url;
                 } catch (MalformedURLException e2) {
                     //out of guesses
                 }
             }
         }
 
         return null;
     }
 
     /**
      * Encodes the url of a GetMap request.
      * <p>
      * If the <tt>layer</tt> argument is <code>null</code>, the request is
      * made including all layers in the <tt>mapContexT</tt>.
      * </p>
      * <p>
      * If the <tt>bbox</tt> argument is <code>null</code>. {@link WMSMapContext#getAreaOfInterest()}
      * is used for the bbox parameter.
      * </p>
      *
      * @param req The getMap request
      * @param layer The Map layer, may be <code>null</code>.
      * @param layerIndex The index of the layer in the request
      * @param bbox The bounding box of the request, may be <code>null</code>.
      * @param kvp Additional or overidding kvp parameters, may be <code>null</code>
      *
      * @return The full url for a getMap request.
      */
     public static String getGetMapUrl(GetMapRequest req, MapLayer layer, int layerIndex, Envelope bbox, String[] kvp) {
         //base url
         String baseUrl = getBaseUrl( req );
     
         String layerName = layer != null ? layer.getTitle() : null;
         String style = layer != null ? layer.getStyle().getName() : null;
         
         return getGetMapUrl( baseUrl, req, layerName, layerIndex, style, bbox, kvp );
     }
 
     /**
      * Encodes the url of a GetMap request.
      * <p>
      * If the <tt>layer</tt> argument is <code>null</code>, the request is
      * made including all layers in the <tt>mapContexT</tt>.
      * </p>
      * <p>
      * If the <tt>style</tt> argument is not <code>null</code> and the <tt>layer</tt>
      * argument is <code>null</code>, then the default style for that layer 
      * is used.
      * </p>
      * <p>
      * If the <tt>bbox</tt> argument is <code>null</code>. {@link WMSMapContext#getAreaOfInterest()}
      * is used for the bbox parameter.
      * </p>
      *
      * @param req The getMap request
      * @param layer The layer name, may be <code>null</code>.
      * @param layerIndex The index of the layer in the request. 
      * @param style The style name, may be <code>null</code>
      * @param bbox The bounding box of the request, may be <code>null</code>.
      * @param kvp Additional or overidding kvp parameters, may be <code>null</code>
      *
      * @return The full url for a getMap request.
      */
     public static String getGetMapUrl(GetMapRequest req, String layer, int layerIndex, String style, Envelope bbox, String[] kvp) {
         //base url
         String baseUrl = getBaseUrl( req );
     
         return getGetMapUrl( baseUrl, req, layer, layerIndex, style, bbox, kvp );
     }
     
     /**
      * Encodes the url of a GetLegendGraphic request.
      *
      * @param req The wms request.
      * @param layer The Map layer, may not be <code>null</code>.
      * @param kvp Additional or overidding kvp parameters, may be <code>null</code>
      *
      * @return The full url for a getMap request.
      */
     public static String getGetLegendGraphicUrl( WMSRequest req, MapLayer layer, String[] kvp ) {
         //parameters
         HashMap params = new HashMap();
 
         params.put("service", "wms");
         params.put("request", "GetLegendGraphic");
         params.put("version", "1.1.1");
         params.put("format", "image/png");
         params.put("layer", layer.getTitle());
         params.put("style", layer.getStyle().getName());
         params.put("height", "20");
         params.put("width", "20");
 
         //overrides / additions
         for (int i = 0; (kvp != null) && (i < kvp.length); i += 2) {
             params.put(kvp[i], kvp[i + 1]);
         }
 
         return encode(getBaseUrl(req),params);    
 
     }
 
     /**
      * Helper method for encoding GetMap request.
      *
      */
     static String getGetMapUrl( String baseUrl, GetMapRequest req, String layer, int layerIndex, String style, Envelope bbox, String[] kvp ) {
         //parameters
         HashMap params = new HashMap();
 
         params.put("service", "wms");
         params.put("request", "GetMap");
         params.put("version", "1.1.1");
 
         params.put( "format", req.getFormat() );
         
         StringBuffer layers = new StringBuffer();
         StringBuffer styles = new StringBuffer();
         
         boolean useLayerIndex = true;
         int count=0;
         for ( int i = 0; i < req.getLayers().length; i++ ) {
             if (layer != null && layer.equals( req.getLayers()[i].getName() ) ) {
                ++count;
             }
         }
         // only one of each layer in the request
         if (count == 1) {
            useLayerIndex = false;
         }
         
         if ( layer != null ) {
              layers.append( layer );
              if ( style != null ) {
                  styles.append( style );
              }
              else {
                  //use default for layer
                  if (useLayerIndex) {
                      styles.append( req.getLayers()[layerIndex].getDefaultStyle().getName() );                   
                  } else {
                      for ( int i = 0; i < req.getLayers().length; i++ ) {
                          if ( layer.equals( req.getLayers()[i].getName() ) ) {
                              styles.append( req.getLayers()[i].getDefaultStyle().getName() );
                          }
                      }                   
                  }
              }
         }
         else {
             //no layer specified, use layers+styles specified by request
             for ( int i = 0; i < req.getLayers().length; i++ ) {
                 MapLayerInfo mapLayer = req.getLayers()[ i ];
                 Style s = (Style) req.getStyles().get( 0 );
                 
                 layers.append( mapLayer.getName() ).append( "," );
                 styles.append( s.getName() ).append( "," );   
 
             }
 
             layers.setLength(layers.length() - 1);
             styles.setLength(styles.length() - 1);
         }
 
         params.put("layers", layers.toString());
         params.put("styles", styles.toString());
 
         //filters, we grab them from the original raw kvp since re-encoding 
         // them from objects is kind of silly
         if (layer != null) {
             //only get filters for the layer
             int index = 0;
             
             if (useLayerIndex) {
                index = layerIndex;
             }else{
                 for ( ; index < req.getLayers().length; index++) {
                     if ( req.getLayers()[index].getName().equals( layer ) ) {
                         break;
                     }
                 }
             }
 
             if ( req.getRawKvp().get("filter") != null ) {
                 //split out the filter we need
                 List filters = KvpUtils.readFlat((String) req.getRawKvp().get("filter"),
                         KvpUtils.OUTER_DELIMETER);
                 params.put( "filter", filters.get(index) );
             }
             else if ( req.getRawKvp().get("cql_filter") != null ) {
                 //split out the filter we need
                 List filters = KvpUtils.readFlat((String) req.getRawKvp().get("cql_filter"),
                         KvpUtils.CQL_DELIMITER);
                 params.put( "cql_filter", filters.get(index) );
             }
             else if ( req.getRawKvp().get("featureid") != null  ) {
                 //semantics of feautre id slightly different, replicate entire value
                 params.put("featureid", req.getRawKvp().get("featureid"));
             }
         
         }
         else {
             //include all
             if ( req.getRawKvp().get("filter") != null ) {
                 params.put("filter",req.getRawKvp().get("filter") );
             }
             else if ( req.getRawKvp().get("cql_filter") != null ) {
                 params.put("cql_filter",req.getRawKvp().get("cql_filter") );
             }
             else if (req.getRawKvp().get("featureid") != null  ) {
                 params.put("featureid", req.getRawKvp().get("featureid"));
             }
         }
         
         //image params
         params.put("height", String.valueOf(req.getHeight()));
         params.put("width", String.valueOf(req.getWidth()));
         params.put( "transparent", ""+req.isTransparent() );
         
         //bbox
         if (bbox == null) {
             bbox = req.getBbox();
         }
         if (bbox != null) {
             params.put("bbox", encode(bbox));    
         }
         
         //srs
         params.put( "srs", req.getSRS() );
         
         //format options
         if ( req.getFormatOptions() != null && !req.getFormatOptions().isEmpty()) {
             params.put( "format_options", encodeFormatOptions(req.getFormatOptions()));
         }
         
        // view params
        if ( req.getViewParams() != null && !req.getViewParams().isEmpty()) {
            params.put( "viewParams", encodeFormatOptions(req.getViewParams()));
        }
        
         //overrides / additions
         for (int i = 0; (kvp != null) && (i < kvp.length); i += 2) {
             params.put(kvp[i], kvp[i + 1]);
         }
 
         return encode(baseUrl, params);
     }
 
     /**
      * Encodes a map of formation options to be used as the value in a kvp.
      *
      * @param formatOptions The map of formation options.
      * 
      * @return A string of the form 'key1:value1,value2;key2:value1;...', or the empty 
      *  string if the formatOptions map is empty. 
      *
      */
     public static String encodeFormatOptions( Map formatOptions ) {
         if ( formatOptions == null || formatOptions.isEmpty() ) {
             return "";
         }
         
         StringBuffer sb = new StringBuffer();
         for ( Iterator e = formatOptions.entrySet().iterator(); e.hasNext(); ) {
             Map.Entry entry = (Map.Entry) e.next();
             String key = (String) entry.getKey();
             Object val = entry.getValue();
             
             sb.append( key ).append( ":" );
             if ( val instanceof Collection ) {
                 Iterator i = ((Collection)val).iterator();
                 while( i.hasNext() ) {
                     sb.append( i.next() ).append( "," );
                 }
                 sb.setLength(sb.length()-1);
             }
             else if ( val.getClass().isArray() ) {
                 int len = Array.getLength( val );
                 for ( int i = 0; i < len; i++ ) {
                     Object o = Array.get( val, i );
                     if ( o != null ) {
                         sb.append( o ).append( "," );
                     }
                 }
                 sb.setLength(sb.length()-1);
             }
             else {
                 sb.append( val.toString() );
             }
             sb.append( ";" );
         }
         
         sb.setLength( sb.length() );
         return sb.toString();
     }
     
     /**
      * Helper method to encode an envelope to be used in a wms request.
      */
     static String encode(Envelope box) {
         return new StringBuffer().append(box.getMinX()).append(",").append(box.getMinY()).append(",")
                                  .append(box.getMaxX()).append(",").append(box.getMaxY()).toString();
     }
 
     /**
      * Helper method for encoding a baseurl and some kvp params into a single
      * url.
      *
      * @param baseUrl The base url of the encoded request.
      * @param kvp The key value pairts of the request.
      *
      * @return The full request url.
      */
     static String encode(String baseUrl, Map kvp) {
         StringBuffer query = new StringBuffer();
       
         for (Iterator e = kvp.entrySet().iterator(); e.hasNext();) {
             Map.Entry entry = (Map.Entry) e.next();
             String name = (String)entry.getKey();
             String value = (String)entry.getValue();
             try {
                name = URLEncoder.encode(name, "UTF-8");
                if(value == null){
                    value = "";
                }else{
                    value = URLEncoder.encode(value, "UTF-8");
                }
             } catch (UnsupportedEncodingException uex) {
                throw new ServiceException("Error encoding name-value pairs" + uex.getMessage(), uex);
             }
             query.append(name).append("=").append(value).append("&");
         }
 
         query.setLength(query.length() - 1);
         return Requests.appendQueryString( baseUrl, query.toString() );
     }
 }
