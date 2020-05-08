 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.wms.kvp;
 
 import org.geoserver.ows.HttpServletRequestAware;
 import org.geoserver.ows.KvpRequestReader;
 import org.geotools.data.DefaultQuery;
 import org.geotools.data.FeatureReader;
 import org.geotools.data.Query;
 import org.geotools.data.Transaction;
 import org.geotools.data.crs.ForceCoordinateSystemFeatureReader;
 import org.geotools.data.memory.MemoryDataStore;
 import org.geotools.factory.CommonFactoryFinder;
 import org.geotools.feature.FeatureType;
 import org.geotools.referencing.CRS;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.geotools.styling.FeatureTypeConstraint;
 import org.geotools.styling.NamedLayer;
 import org.geotools.styling.NamedStyle;
 import org.geotools.styling.SLDParser;
 import org.geotools.styling.Style;
 import org.geotools.styling.StyleAttributeExtractor;
 import org.geotools.styling.StyleFactory;
 import org.geotools.styling.StyledLayer;
 import org.geotools.styling.StyledLayerDescriptor;
 import org.geotools.styling.UserLayer;
 import org.opengis.filter.Filter;
 import org.opengis.filter.FilterFactory;
 import org.opengis.filter.Id;
 import org.opengis.filter.identity.FeatureId;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.global.MapLayerInfo;
 import org.vfny.geoserver.global.TemporaryFeatureTypeInfo;
 import org.vfny.geoserver.global.WMS;
 import org.vfny.geoserver.util.Requests;
 import org.vfny.geoserver.util.SLDValidator;
 import org.vfny.geoserver.wms.WmsException;
 import org.vfny.geoserver.wms.requests.GetMapKvpReader;
 import org.vfny.geoserver.wms.requests.GetMapRequest;
 import org.vfny.geoserver.wms.servlets.GetMap;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import javax.servlet.http.HttpServletRequest;
 
 
 public class GetMapKvpRequestReader extends KvpRequestReader implements HttpServletRequestAware {
     /**
      * get map
      */
     GetMap getMap;
 
     /**
      * current request
      */
     HttpServletRequest httpRequest;
 
     /**
      * style factory
      */
     StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
 
     /**
      * filter factory
      */
     FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);
 
     /**
      * Flag to control wether styles are mandatory
      */
     boolean styleRequired;
     
     /**
      * The WMS service, that we use to pick up base layer definitions
      */
     WMS wms;
 
     public GetMapKvpRequestReader(GetMap getMap, WMS wms) {
         super(GetMapRequest.class);
 
         this.getMap = getMap;
         this.wms = wms;
     }
 
     public void setHttpRequest(HttpServletRequest httpRequest) {
         this.httpRequest = httpRequest;
     }
 
     public void setStyleFactory(StyleFactory styleFactory) {
         this.styleFactory = styleFactory;
     }
 
     public void setFilterFactory(FilterFactory filterFactory) {
         this.filterFactory = filterFactory;
     }
 
     public boolean isStyleRequired() {
         return styleRequired;
     }
 
     public void setStyleRequired(boolean styleRequired) {
         this.styleRequired = styleRequired;
     }
 
     public Object createRequest() throws Exception {
         GetMapRequest request = new GetMapRequest(getMap);
         request.setHttpServletRequest(httpRequest);
 
         return request;
     }
 
     public Object read(Object request, Map kvp) throws Exception {
         GetMapRequest getMap = (GetMapRequest) super.read(request, kvp);
 
         //do some additional checks
 
         // srs
         String epsgCode = getMap.getSRS();
 
         if (epsgCode != null) {
             try {
                 //set the crs as well
                 CoordinateReferenceSystem mapcrs = CRS.decode(epsgCode);
                 getMap.setCrs(mapcrs);
             } catch (Exception e) {
                 //couldnt make it - we send off a service exception with the correct info
                 throw new WmsException(e.getLocalizedMessage(), "InvalidSRS");
             }
         }
 
         //styles
         // process SLD_BODY, SLD, then STYLES parameter
         if (getMap.getSldBody() != null) {
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.fine("Getting layers and styles from SLD_BODY");
             }
 
             if (getMap.getValidateSchema().booleanValue()) {
                 List errors = validateSld(new ByteArrayInputStream(getMap.getSldBody().getBytes()));
 
                 if (errors.size() != 0) {
                     throw new WmsException(SLDValidator.getErrorMessage(
                             new ByteArrayInputStream(getMap.getSldBody().getBytes()), errors));
                 }
             }
 
             StyledLayerDescriptor sld = parseSld(new ByteArrayInputStream(
                         getMap.getSldBody().getBytes()));
             processSld(getMap, sld);
         } else if (getMap.getSld() != null) {
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.fine("Getting layers and styles from reomte SLD");
             }
 
             URL sldUrl = getMap.getSld();
 
             if (getMap.getValidateSchema().booleanValue()) {
                 InputStream input = Requests.getInputStream(sldUrl);
                 List errors = null;
 
                 try {
                     errors = validateSld(input);
                 } finally {
                     input.close();
                 }
 
                 if ((errors != null) && (errors.size() != 0)) {
                     input = Requests.getInputStream(sldUrl);
 
                     try {
                         throw new WmsException(SLDValidator.getErrorMessage(input, errors));
                     } finally {
                         input.close();
                     }
                 }
             }
 
             //JD: GEOS-420, Wrap the sldUrl in getINputStream method in order
             // to do compression
             InputStream input = Requests.getInputStream(sldUrl);
 
             try {
                 StyledLayerDescriptor sld = parseSld(input);
                 processSld(getMap, sld);
             } finally {
                 input.close();
             }
         } else {
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.fine("Getting layers and styles from LAYERS and STYLES");
             }
             
             // first, expand base layers and default styles
             if(getMap.getLayers() != null) {
                 List oldLayers = new ArrayList(Arrays.asList(getMap.getLayers()));
                 List oldStyles = new ArrayList(getMap.getStyles());
                 List newLayers = new ArrayList();
                 List newStyles = new ArrayList();
                 
                 for (int i = 0; i < oldLayers.size(); i++) {
                     MapLayerInfo info  = (MapLayerInfo) oldLayers.get(i);
                     Style style = oldStyles.isEmpty() ? null : (Style) oldStyles.get(i);
                     if(info.getType() == MapLayerInfo.TYPE_BASEMAP) {
                         List subLayers = info.getSubLayers();
                         newLayers.addAll(subLayers);
                         List currStyles = info.getStyles();
                         for (int j = 0; j < subLayers.size(); j++) {
                             MapLayerInfo currLayer = (MapLayerInfo) subLayers.get(j);
                             Style currStyle = currStyles.isEmpty() ? null: (Style) currStyles.get(j)  ;
                             if(currStyle != null) 
                                 newStyles.add(currStyle);
                             else
                                 newStyles.add(currLayer.getDefaultStyle());
                         }
                     } else {
                         newLayers.add(info);
                         if(style != null)
                             newStyles.add(style);
                         else
                             newStyles.add(info.getDefaultStyle());
                     }
                 }
                 getMap.setLayers(newLayers);
                 getMap.setStyles(newStyles);
             }
             
             
             // then proceed with standard processing
             MapLayerInfo[] layers = getMap.getLayers();
             if ((layers != null) && (layers.length > 0)) {
                 List styles = getMap.getStyles();
 
                 if (layers.length != styles.size()) {
                     String msg = layers.length + " layers requested, but found " + styles.size()
                         + " styles specified. "
                         + "Since SLD parameter is not yet implemented, the STYLES parameter "
                         + "is mandatory and MUST have exactly one value per requested layer";
                     throw new WmsException(msg, getClass().getName());
                 }
 
                 for (int i = 0; i < getMap.getStyles().size(); i++) {
                     Style currStyle = (Style) getMap.getStyles().get(i);
                     MapLayerInfo currLayer = layers[i];
 
                     if (currLayer.getType() == MapLayerInfo.TYPE_VECTOR) {
                         try {
                             checkStyle(currStyle, layers[i].getFeature().getFeatureType());
                         } catch (IOException e) {
                             throw new WmsException("Error obtaining FeatureType for layer "
                                 + layers[i].getName());
                         }
 
                         if (LOGGER.isLoggable(Level.FINE)) {
                             LOGGER.fine(new StringBuffer("establishing ").append(
                                     currStyle.getName()).append(" style for ")
                                                                          .append(layers[i].getName())
                                                                          .toString());
                         }
                     } else if (currLayer.getType() == MapLayerInfo.TYPE_RASTER) {
                         /**
                          * @task TODO: Check for Style Coverage Compatibility ...
                          */
                     }
                 }
             }
         }
 
         //filters
         // in case of a mixed request, get with sld in post body, layers
         // are not parsed, so we can't parse filters neither...
         if ((getMap.getLayers() != null) && (getMap.getLayers().length > 0)) {
             List filters = (getMap.getFilter() != null) ? getMap.getFilter() : Collections.EMPTY_LIST;
             List cqlFilters = (getMap.getCQLFilter() != null) ? getMap.getCQLFilter()
                                                               : Collections.EMPTY_LIST;
             List featureId = (getMap.getFeatureId() != null) ? getMap.getFeatureId()
                                                              : Collections.EMPTY_LIST;
 
             if (!featureId.isEmpty()) {
                 if (!filters.isEmpty()) {
                     throw new WmsException("GetMap KVP request contained "
                         + "conflicting filters.  Filter: " + filters + ", fid: " + featureId);
                 }
 
                 filters = new ArrayList(featureId.size());
 
                 for (Iterator i = featureId.iterator(); i.hasNext();) {
                     FeatureId fid = filterFactory.featureId((String) i.next());
                     Id filter = filterFactory.id(Collections.singleton(fid));
                     filters.add(filter);
                 }
             }
 
             if (!cqlFilters.isEmpty()) {
                 if (!filters.isEmpty()) {
                     throw new WmsException("GetMap KVP request contained "
                         + "conflicting filters.  Filter: " + filters + ", fid: " + featureId
                         + ", cql: " + cqlFilters);
                 }
 
                 filters = cqlFilters;
             }
 
             getMap.setFilter(filters);
 
             int numLayers = getMap.getLayers().length;
 
             if (!filters.isEmpty() && (numLayers != filters.size())) {
                 // as in wfs getFeatures, perform lenient parsing, if just one filter, it gets
                 // applied to all layers
                 if (filters.size() == 1) {
                     Filter f = (Filter) filters.get(0);
                     filters = new ArrayList(numLayers);
 
                     for (int i = 0; i < numLayers; i++) {
                         filters.add(f);
                     }
                 } else {
                     String msg = numLayers + " layers requested, but found " + filters.size()
                         + " filters specified. "
                         + "When you specify the FILTER parameter, you must provide just one, \n"
                         + " that will be applied to all layers, or exactly one for each requested layer";
                     throw new WmsException(msg, getClass().getName());
                 }
             }
         }
 
         return getMap;
     }
 
     /**
      * validates an sld document.
      *
      */
     private List validateSld(InputStream input) {
         // user requested to validate the schema.
         SLDValidator validator = new SLDValidator();
 
         return validator.validateSLD(input, httpRequest.getSession().getServletContext());
     }
 
     /**
      * Parses an sld document.
      */
     private StyledLayerDescriptor parseSld(InputStream input) {
         SLDParser parser = new SLDParser(styleFactory, input);
 
         return parser.parseSLD();
     }
     
     /**
      * Looks in <code>sld</code> for the layers and styles to use in the map
      * composition and sets them to the <code>request</code>
      *
      * <p>
      * If <code>sld</code> is used in "library" mode, that is, the LAYERS param
      * is also present, saying what layers must be taken in count, then only
      * the layers from the LAYERS parameter are used and <code>sld</code> is
      * used as a style library, which means that for each layer requested
      * through LAYERS=..., if a style if found in it for that layer it is
      * used, and if not, the layers default is used.
      * </p>
      *
      * <p>
      * By the other hand, if the LAYERS parameter is not present all the layers
      * found in <code>sld</code> are setted to <code>request</code>.
      * </p>
      *
      * @param request the GetMap request to which to set the layers and styles
      * @param sld a SLD document to take layers and styles from, following the
      *        "literal" or "library" rule.
      *
      * @throws WmsException if anything goes wrong
      * @throws RuntimeException DOCUMENT ME!
      */
     public static void processSld(final GetMapRequest request, final StyledLayerDescriptor sld)
         throws WmsException {
         MapLayerInfo[] libraryModeLayers = null;
 
         if ((request.getLayers() != null) && (request.getLayers().length > 0)) {
             if (LOGGER.isLoggable(Level.INFO)) {
                 LOGGER.info("request comes in \"library\" mode");
             }
 
             libraryModeLayers = request.getLayers();
         }
 
         final StyledLayer[] styledLayers = sld.getStyledLayers();
         final int slCount = styledLayers.length;
 
         if (slCount == 0) {
             throw new WmsException("SLD document contains no layers");
         }
 
         final List layers = new ArrayList();
         final List styles = new ArrayList();
 
         MapLayerInfo currLayer = null;
         Style currStyle = null;
 
         if (null != libraryModeLayers) {
             int lCount = libraryModeLayers.length;
 
             for (int i = 0; i < lCount; i++) {
                 currLayer = libraryModeLayers[i];
 
                 if (currLayer.getType() == MapLayerInfo.TYPE_VECTOR) {
                     currStyle = findStyleOf(request, currLayer.getFeature(), styledLayers);
                 } else if (currLayer.getType() == MapLayerInfo.TYPE_RASTER) {
                     currStyle = findStyle(request, "raster");
                 }
 
                 layers.add(currLayer);
                 styles.add(currStyle);
             }
         } else {
             StyledLayer sl = null;
             String layerName;
             UserLayer ul;
 
             for (int i = 0; i < slCount; i++) {
                 sl = styledLayers[i];
                 layerName = sl.getName();
 
                 if (null == layerName) {
                     throw new WmsException("A UserLayer without layer name was passed");
                 }
 
                 currLayer = new MapLayerInfo();
 
                 // handle the InLineFeature stuff
                 // TODO: add support for remote WFS here
                 if ((sl instanceof UserLayer)
                         && ((((UserLayer) sl)).getInlineFeatureDatastore() != null)) {
                     // SPECIAL CASE - we make the temporary version
                     ul = ((UserLayer) sl);
 
                     try {
                         initializeInlineFeatureLayer(request, ul, currLayer);
                     } catch (Exception e) {
                         throw new WmsException(e);
                     }
                 } else {
                     try {
                         currLayer.setFeature(GetMapKvpReader.findFeatureLayer(request, layerName));
                     } catch (WmsException e) {
                         currLayer.setCoverage(GetMapKvpReader.findCoverageLayer(request, layerName));
                     }
                 }
 
                 if (currLayer.getType() == MapLayerInfo.TYPE_VECTOR) {
                     // currStyle = findStyleOf(request, currLayer,
                     // styledLayers); // DJB: this looks like a bug, we should
                     // get the style from styledLayers[i]
 
                     // the correct thing to do its grab the style from
                     // styledLayers[i]
                     // inside the styledLayers[i] will either be :
                     // a) nothing - in which case grab the layer's default style
                     // b) a set of:
                     // i) NameStyle -- grab it from the pre-loaded styles
                     // ii)UserStyle -- grab it from the sld the user uploaded
                     //
                     // NOTE: we're going to get a set of layer->style pairs for
                     // (b).
                     addStyles(request, currLayer, styledLayers[i], layers, styles);
                 } else if (currLayer.getType() == MapLayerInfo.TYPE_RASTER) {
                     currStyle = findStyle(request, "raster");
 
                     layers.add(currLayer);
                     styles.add(currStyle);
                 }
             }
         }
 
         request.setLayers((MapLayerInfo[]) layers.toArray(new MapLayerInfo[layers.size()]));
         request.setStyles(styles);
     }
 
     /**
      * the correct thing to do its grab the style from styledLayers[i]
     * inside the styledLayers[i] will either be :
     *  a) nothing - in which case grab the layer's default style
     *  b) a set of:
     * i) NameStyle -- grab it from the pre-loaded styles
     *  ii)UserStyle -- grab it from the sld the user uploaded
     *
     *  NOTE: we're going to get a set of layer->style pairs for (b).
     *        these are added to layers,styles
     *
     *   NOTE: we also handle some featuretypeconstraints
     *
      * @param request
      * @param currLayer
      * @param layer
      * @param layers
      * @param styles
      */
     public static void addStyles(GetMapRequest request, MapLayerInfo currLayer, StyledLayer layer,
         List layers, List styles) throws WmsException {
         if (currLayer == null) {
             return; // protection
         }
 
         Style[] layerStyles = null;
         FeatureTypeConstraint[] ftcs = null;
 
         if (layer instanceof NamedLayer) {
             ftcs = ((NamedLayer) layer).getLayerFeatureConstraints();
             layerStyles = ((NamedLayer) layer).getStyles();
         } else if (layer instanceof UserLayer) {
             ftcs = ((UserLayer) layer).getLayerFeatureConstraints();
             layerStyles = ((UserLayer) layer).getUserStyles();
         }
 
         // DJB: TODO: this needs to do the whole thing, not just names
         if (ftcs != null) {
             FeatureTypeConstraint ftc;
             final int length = ftcs.length;
 
             for (int t = 0; t < length; t++) {
                 ftc = ftcs[t];
 
                 if (ftc.getFeatureTypeName() != null) {
                     String ftc_name = ftc.getFeatureTypeName();
 
                     // taken from lite renderer
                     boolean matches;
 
                     try {
                         matches = currLayer.getFeature().getFeatureType()
                                            .isDescendedFrom(null, ftc_name)
                             || currLayer.getFeature().getFeatureType().getTypeName()
                                         .equalsIgnoreCase(ftc_name);
                     } catch (Exception e) {
                         matches = false; // bad news
                     }
 
                     if (!matches) {
                         continue; // this layer is fitered out
                     }
                 }
             }
         }
 
         // handle no styles -- use default
         if ((layerStyles == null) || (layerStyles.length == 0)) {
             layers.add(currLayer);
             styles.add(currLayer.getDefaultStyle());
 
             return;
         }
 
         final int length = layerStyles.length;
         Style s;
 
         for (int t = 0; t < length; t++) {
             if (layerStyles[t] instanceof NamedStyle) {
                 layers.add(currLayer);
                 s = findStyle(request, ((NamedStyle) layerStyles[t]).getName());
 
                 if (s == null) {
                     throw new WmsException("couldnt find style named '"
                         + ((NamedStyle) layerStyles[t]).getName() + "'");
                 }
 
                 styles.add(s);
             } else {
                 layers.add(currLayer);
                 styles.add(layerStyles[t]);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param request
      * @param currStyleName
      *
      * @return the configured style named <code>currStyleName</code> or
      *         <code>null</code> if such a style does not exists on this
      *         server.
      */
     public static Style findStyle(GetMapRequest request, String currStyleName) {
         Style currStyle;
         Map configuredStyles = request.getWMS().getData().getStyles();
 
         currStyle = (Style) configuredStyles.get(currStyleName);
 
         return currStyle;
     }
 
     /**
      * Finds the style for <code>layer</code> in <code>styledLayers</code> or
      * the layer's default style if <code>styledLayers</code> has no a
      * UserLayer or a NamedLayer with the same name than <code>layer</code>
      * <p>
      * This method is used to parse the style of a layer for SLD and SLD_BODY parameters,
      * both in library and literal mode. Thus, once the declared style for the given layer
      * is found, it is checked for validity of appliance for that layer (i.e., whether the
      * featuretype contains the attributes needed for executing the style filters).
      * </p>
      *
      * @param request used to find out an internally configured style when referenced by name by a NamedLayer
      *
      * @param layer one of the internal FeatureType that was requested through the LAYERS parameter
      * or through and SLD document when the request is in literal mode.
      * @param styledLayers a set of StyledLayers from where to find the SLD layer with the same
      * name as <code>layer</code> and extract the style to apply.
      *
      * @return the Style applicable to <code>layer</code> extracted from <code>styledLayers</code>.
      *
      * @throws RuntimeException if one of the StyledLayers is neither a UserLayer nor a NamedLayer. This
      * shuoldn't happen, since the only allowed subinterfaces of StyledLayer are NamedLayer and UserLayer.
      * @throws WmsException
      */
     private static Style findStyleOf(GetMapRequest request, FeatureTypeInfo layer,
         StyledLayer[] styledLayers) throws WmsException {
         Style style = null;
         String layerName = layer.getName();
         StyledLayer sl;
 
         for (int i = 0; i < styledLayers.length; i++) {
             sl = styledLayers[i];
 
             if (layerName.equals(sl.getName())) {
                 if (sl instanceof UserLayer) {
                     Style[] styles = ((UserLayer) sl).getUserStyles();
 
                     if ((null != styles) && (0 < styles.length)) {
                         style = styles[0];
                     }
                 } else if (sl instanceof NamedLayer) {
                     Style[] styles = ((NamedLayer) sl).getStyles();
 
                     if ((null != styles) && (0 < styles.length)) {
                         style = styles[0];
                     }
 
                     if (style instanceof NamedStyle) {
                         style = findStyle(request, style.getName());
                     }
                 } else {
                     throw new RuntimeException("Unknown layer type: " + sl);
                 }
 
                 break;
             }
         }
 
         if (null == style) {
             style = layer.getDefaultStyle();
         }
 
         FeatureType type;
 
         try {
             type = layer.getFeatureType();
         } catch (IOException ioe) {
             throw new RuntimeException("Error getting FeatureType, this should never happen!");
         }
 
         checkStyle(style, type);
 
         return style;
     }
 
     /**
      * Checks to make sure that the style passed in can process the
      * FeatureType.
      *
      * @param style The style to check
      * @param fType The source requested.
      *
      * @throws WmsException DOCUMENT ME!
      */
     private static void checkStyle(Style style, FeatureType fType)
         throws WmsException {
         StyleAttributeExtractor sae = new StyleAttributeExtractor();
         sae.visit(style);
 
         String[] styleAttributes = sae.getAttributeNames();
         String attName;
         final int length = styleAttributes.length;
 
         for (int i = 0; i < length; i++) {
             attName = styleAttributes[i];
 
             if (fType.getAttributeType(attName) == null) {
                 throw new WmsException("The requested Style can not be used with "
                     + "this featureType.  The style specifies an attribute of " + attName
                     + " and the featureType definition is: " + fType);
             }
         }
     }
 
     /**
      * Method to initialize a user layer which contains inline features.
      *
      * @param httpRequest The request
      * @param mapLayer The map layer.
      *
      * @throws Exception
      */
 
     //JD: the reason this method is static is to share logic among the xml
     // and kvp reader, ugh...
     public static void initializeInlineFeatureLayer(GetMapRequest getMapRequest, UserLayer ul,
         MapLayerInfo currLayer) throws Exception {
         //SPECIAL CASE - we make the temporary version
         currLayer.setFeature(new TemporaryFeatureTypeInfo(ul.getInlineFeatureDatastore()));
 
         //what if they didn't put an "srsName" on their geometry in their inlinefeature?
         //I guess we should assume they mean their geometry to exist in the output SRS of the
         //request they're making.
         if (ul.getInlineFeatureType().getDefaultGeometry().getCoordinateSystem() == null) {
             LOGGER.warning(
                 "No CRS set on inline features default geometry.  Assuming the requestor has their inlinefeatures in the boundingbox CRS.");
 
             FeatureType currFt = ul.getInlineFeatureType();
             Query q = new DefaultQuery(currFt.getTypeName(), Filter.INCLUDE);
             FeatureReader ilReader = ul.getInlineFeatureDatastore()
                                        .getFeatureReader(q, Transaction.AUTO_COMMIT);
             CoordinateReferenceSystem crs = (getMapRequest.getCrs() == null)
                 ? DefaultGeographicCRS.WGS84 : getMapRequest.getCrs();
             MemoryDataStore reTypedDS = new MemoryDataStore(new ForceCoordinateSystemFeatureReader(
                         ilReader, crs));
             currLayer.setFeature(new TemporaryFeatureTypeInfo(reTypedDS));
         }
     }
 }
