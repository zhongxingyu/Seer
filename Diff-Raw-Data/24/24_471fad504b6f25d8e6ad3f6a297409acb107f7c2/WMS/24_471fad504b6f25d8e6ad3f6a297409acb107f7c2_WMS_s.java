 package org.geoserver.wms;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.nio.charset.Charset;
 import java.util.List;
 import java.util.Map;
 
 import org.geoserver.catalog.Catalog;
 import org.geoserver.catalog.CoverageInfo;
 import org.geoserver.catalog.FeatureTypeInfo;
 import org.geoserver.catalog.LayerGroupInfo;
 import org.geoserver.catalog.LayerInfo;
 import org.geoserver.catalog.NamespaceInfo;
 import org.geoserver.catalog.StyleInfo;
 import org.geoserver.config.GeoServer;
 import org.geoserver.config.GeoServerInfo;
 import org.geoserver.jai.JAIInfo;
 import org.geoserver.wms.WatermarkInfo.Position;
 import org.geotools.styling.Style;
 import org.geotools.util.Version;
 import org.opengis.feature.type.Name;
 
 /**
  * 
  * @author Gabriel Roldan
  * 
  */
 public class WMS {
 
     public static final String WEB_CONTAINER_KEY = "WMS";
 
     /**
      * SVG renderers.
      */
     public static final String SVG_SIMPLE = "Simple";
 
     public static final String SVG_BATIK = "Batik";
 
     /**
      * Interpolation Types
      */
     public static final String INT_NEAREST = "Nearest";
 
     public static final String INT_BIlINEAR = "Bilinear";
 
     public static final String INT_BICUBIC = "Bicubic";
 
     private final GeoServer geoserver;
 
     public WMS(GeoServer geoserver) {
         this.geoserver = geoserver;
     }
 
     private Catalog getCatalog() {
         return geoserver.getCatalog();
     }
 
     public WMSInfo getServiceInfo() {
         return geoserver.getService(WMSInfo.class);
     }
 
     public Style getStyleByName(String styleName) throws IOException {
         StyleInfo styleInfo = getCatalog().getStyleByName(styleName);
         return styleInfo == null ? null : styleInfo.getStyle();
     }
 
     public LayerInfo getLayerByName(String layerName) {
         return getCatalog().getLayerByName(layerName);
     }
 
     public LayerGroupInfo getLayerGroupByName(String layerGroupName) {
         return getCatalog().getLayerGroupByName(layerGroupName);
     }
 
     public boolean isEnabled() {
        return getServiceInfo().isEnabled();
     }
 
     public String getVersion() {
        List<Version> versions = getServiceInfo().getVersions();
         String version;
         if (versions.size() > 0) {
             version = versions.get(0).toString();
         } else {
             // shouldn't a version be set?
             version = "1.1.1";
         }
         return version;
     }
 
     public GeoServer getGeoServer() {
         return this.geoserver;
     }
 
     public String getInterpolation() {
         return getServiceInfo().getInterpolation();
     }
 
     public Boolean getPNGNativeAcceleration() {
         JAIInfo jaiInfo = getJaiInfo();
         return Boolean.valueOf(jaiInfo.isPngAcceleration());
     }
 
     public Boolean getJPEGNativeAcceleration() {
         JAIInfo jaiInfo = getJaiInfo();
         return Boolean.valueOf(jaiInfo.isJpegAcceleration());
     }
 
     private JAIInfo getJaiInfo() {
         GeoServer geoServer = getGeoServer();
         GeoServerInfo global = geoServer.getGlobal();
         Map<String, Serializable> metadata = global.getMetadata();
         JAIInfo jaiInfo = (JAIInfo) metadata.get(JAIInfo.KEY);
         if (jaiInfo == null) {
             jaiInfo = new JAIInfo();
         }
         return jaiInfo;
     }
 
     public Charset getCharSet() {
         GeoServer geoServer2 = getGeoServer();
         GeoServerInfo global = geoServer2.getGlobal();
         String charset = global.getCharset();
         return Charset.forName(charset);
     }
 
     public String getProxyBaseUrl() {
         GeoServer geoServer = getGeoServer();
         GeoServerInfo global = geoServer.getGlobal();
         String proxyBaseUrl = global.getProxyBaseUrl();
         return proxyBaseUrl;
     }
 
     public int getUpdateSequence() {
         GeoServerInfo global = getGeoServer().getGlobal();
         return global.getUpdateSequence();
     }
 
     public int getWatermarkTransparency() {
         WatermarkInfo watermark = getServiceInfo().getWatermark();
         return watermark.getTransparency();
     }
 
     public int getWatermarkPosition() {
         WatermarkInfo watermark = getServiceInfo().getWatermark();
         Position position = watermark.getPosition();
         return position.getCode();
     }
 
     public boolean isGlobalWatermarking() {
         WatermarkInfo watermark = getServiceInfo().getWatermark();
         return watermark.isEnabled();
     }
 
     public String getGlobalWatermarkingURL() {
         WatermarkInfo watermark = getServiceInfo().getWatermark();
         return watermark.getURL();
     }
 
     public FeatureTypeInfo getFeatureTypeInfo(final Name name) {
         Catalog catalog = getCatalog();
         FeatureTypeInfo resource = catalog.getResourceByName(name, FeatureTypeInfo.class);
         return resource;
     }
 
     public CoverageInfo getCoverageInfo(final Name name) {
         Catalog catalog = getCatalog();
         CoverageInfo resource = catalog.getResourceByName(name, CoverageInfo.class);
         return resource;
     }
 
     public List<LayerInfo> getLayers() {
         Catalog catalog = getCatalog();
         return catalog.getLayers();
     }
 
     public String getNamespaceByPrefix(final String prefix) {
         Catalog catalog = getCatalog();
         NamespaceInfo namespaceInfo = catalog.getNamespaceByPrefix(prefix);
         return namespaceInfo == null ? null : namespaceInfo.getURI();
     }
 
     public List<LayerGroupInfo> getLayerGroups() {
         Catalog catalog = getCatalog();
         List<LayerGroupInfo> layerGroups = catalog.getLayerGroups();
         return layerGroups;
     }
 
     /**
      * Informs the user that this WMS supports SLD. We don't currently handle sld, still needs to be
      * rolled in from geotools, so this now must be false.
      * 
      * //djb: we support it now
      * 
      * @return false
      */
     public boolean supportsSLD() {
         return true; // djb: we support it now
     }
 
     /**
      * Informs the user that this WMS supports User Layers
      * <p>
      * We support this both remote wfs and inlineFeature
      * </p>
      * 
      * @return true
      */
     public boolean supportsUserLayer() {
         return true;
     }
 
     /**
      * Informs the user that this WMS supports User Styles
      * 
      * @return true
      */
     public boolean supportsUserStyle() {
         return true;
     }
 
     /**
      * Informs the user that this WMS supports Remote WFS.
      * 
      * @return true
      */
     public boolean supportsRemoteWFS() {
         return true;
     }
 
     public void setSvgRenderer(String svgRendererHint) {
         WMSInfo serviceInfo = getServiceInfo();
         serviceInfo.getMetadata().put("svgRenderer", svgRendererHint);
     }
 
     public String getSvgRenderer() {
         WMSInfo serviceInfo = getServiceInfo();
         String svgRendererHint = (String) serviceInfo.getMetadata().get("svgRenderer");
         return svgRendererHint;
     }
 
     public boolean isSvgAntiAlias() {
         WMSInfo serviceInfo = getServiceInfo();
         Boolean svgAntiAlias = (Boolean) serviceInfo.getMetadata().get("svgAntiAlias");
         return svgAntiAlias == null ? true : svgAntiAlias.booleanValue();
     }
 
     public int getNumDecimals() {
         GeoServerInfo global = getGeoServer().getGlobal();
         return global.getNumDecimals();
     }
 
     public String getNameSpacePrefix(final String nsUri) {
         Catalog catalog = getCatalog();
         NamespaceInfo ns = catalog.getNamespaceByURI(nsUri);
         return ns == null ? null : ns.getPrefix();
     }
 }
