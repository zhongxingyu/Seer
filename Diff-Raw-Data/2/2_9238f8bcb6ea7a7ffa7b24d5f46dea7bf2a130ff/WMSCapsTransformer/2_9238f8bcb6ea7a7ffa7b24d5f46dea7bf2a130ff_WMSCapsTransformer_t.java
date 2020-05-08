 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.responses.helpers;
 
 import com.vividsolutions.jts.geom.Envelope;
 import org.apache.xalan.transformer.TransformerIdentityImpl;
 import org.geotools.geometry.GeneralEnvelope;
 import org.geotools.referencing.CRS;
 import org.geotools.styling.Style;
 import org.geotools.xml.transform.TransformerBase;
 import org.geotools.xml.transform.Translator;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.springframework.context.ApplicationContext;
 import org.vfny.geoserver.global.CoverageInfo;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.global.LegendURL;
 import org.vfny.geoserver.global.MapLayerInfo;
 import org.vfny.geoserver.global.MetaDataLink;
 import org.vfny.geoserver.global.WMS;
 import org.vfny.geoserver.util.requests.CapabilitiesRequest;
 import org.vfny.geoserver.wms.requests.GetLegendGraphicRequest;
 import org.vfny.geoserver.wms.responses.DescribeLayerResponse;
 import org.vfny.geoserver.wms.responses.GetFeatureInfoResponse;
 import org.vfny.geoserver.wms.responses.GetLegendGraphicResponse;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.helpers.AttributesImpl;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 
 
 /**
  * Geotools xml framework based encoder for a Capabilities WMS 1.1.1 document.
  *
  * @author Gabriel Roldan, Axios Engineering
  * @version $Id
  */
 public class WMSCapsTransformer extends TransformerBase {
     /** fixed MIME type for the returned capabilities document */
     public static final String WMS_CAPS_MIME = "application/vnd.ogc.wms_xml";
 
     /** DOCUMENT ME! */
     private String schemaBaseUrl;
     private Set formats;
     private ApplicationContext applicationContext;
 
     /**
      * Creates a new WMSCapsTransformer object.
      *
      * @param schemaBaseUrl
      *            needed to get the schema base URL
      * @param formats
      * @param applicationContext
      *
      * @throws NullPointerException
      *             if <code>schemaBaseUrl</code> is null;
      */
     public WMSCapsTransformer(String schemaBaseUrl, Set formats,
         ApplicationContext applicationContext) {
         super();
         this.formats = formats;
 
         if (schemaBaseUrl == null) {
             throw new NullPointerException();
         }
 
         this.schemaBaseUrl = schemaBaseUrl;
         this.setNamespaceDeclarationEnabled(false);
         this.applicationContext = applicationContext;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param handler
      *            DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public Translator createTranslator(ContentHandler handler) {
         return new CapabilitiesTranslator(handler, formats, applicationContext);
     }
 
     /**
      * Gets the <code>Transformer</code> created by the overriden method in
      * the superclass and adds it the system DOCTYPE token pointing to the
      * Capabilities DTD on this server instance.
      *
      * <p>
      * The DTD is set at the fixed location given by the
      * <code>schemaBaseUrl</code> passed to the constructor <code>+
      * "wms/1.1.1/WMS_MS_Capabilities.dtd</code>.
      * </p>
      *
      * @return a Transformer propoerly configured to produce DescribeLayer
      *         responses.
      *
      * @throws TransformerException
      *             if it is thrown by <code>super.createTransformer()</code>
      */
     public Transformer createTransformer() throws TransformerException {
         Transformer transformer = super.createTransformer();
         String dtdUrl = this.schemaBaseUrl + "wms/1.1.1/WMS_MS_Capabilities.dtd"; // DJB: fixed this to
                                                                                   // point to correct
                                                                                   // location
 
         transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dtdUrl);
 
         return transformer;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @author Gabriel Roldan, Axios Engineering
      * @version $Id
      */
     private static class CapabilitiesTranslator extends TranslatorSupport {
         /** DOCUMENT ME! */
         private static final Logger LOGGER = Logger.getLogger(CapabilitiesTranslator.class.getPackage()
                                                                                           .getName());
 
         /** DOCUMENT ME! */
         private static final String EPSG = "EPSG:";
 
         /** DOCUMENT ME! */
         private static AttributesImpl wmsVersion = new AttributesImpl();
 
         /** DOCUMENT ME! */
         private static final String XLINK_NS = "http://www.w3.org/1999/xlink";
 
         static {
             wmsVersion.addAttribute("", "version", "version", "", "1.1.1");
         }
 
         /**
          * The request from wich all the information needed to produce the
          * capabilities document can be obtained
          */
         private CapabilitiesRequest request;
         private Set formats;
         private ApplicationContext applicationContext;
 
         /**
          * Creates a new CapabilitiesTranslator object.
          *
          * @param handler
          *            content handler to send sax events to.
          */
         public CapabilitiesTranslator(ContentHandler handler, Set formats,
             ApplicationContext applicationContext) {
             super(handler, null, null);
             this.formats = formats;
             this.applicationContext = applicationContext;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param o
          *            the <code>CapabilitiesRequest</code>
          *
          * @throws IllegalArgumentException
          *             DOCUMENT ME!
          */
         public void encode(Object o) throws IllegalArgumentException {
             if (!(o instanceof CapabilitiesRequest)) {
                 throw new IllegalArgumentException();
             }
 
             this.request = (CapabilitiesRequest) o;
 
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.fine(new StringBuffer("producing a capabilities document for ").append(
                         request).toString());
             }
 
             start("WMT_MS_Capabilities", wmsVersion);
             handleService();
             handleCapability();
             end("WMT_MS_Capabilities");
         }
 
         /**
          * Encodes the service metadata section of a WMS capabilities document.
          */
         private void handleService() {
             WMS wms = (WMS) request.getServiceRef().getServiceRef();
             start("Service");
 
             element("Name", "OGC:WMS");
             element("Title", wms.getTitle());
             element("Abstract", wms.getAbstract());
 
             handleKeywordList(wms.getKeywords());
 
             AttributesImpl orAtts = new AttributesImpl();
             orAtts.addAttribute("", "xmlns:xlink", "xmlns:xlink", "", XLINK_NS);
             orAtts.addAttribute(XLINK_NS, "xlink:type", "xlink:type", "", "simple");
             orAtts.addAttribute("", "xlink:href", "xlink:href", "",
                 wms.getOnlineResource().toExternalForm());
             element("OnlineResource", null, orAtts);
 
             handleContactInfo(wms.getGeoServer());
 
             element("Fees", wms.getFees());
             element("AccessConstraints", wms.getAccessConstraints());
             end("Service");
         }
 
         /**
          * Encodes contact information in the WMS capabilities document
          * @param geoServer
          */
         public void handleContactInfo(GeoServer geoServer) {
             start("ContactInformation");
 
             start("ContactPersonPrimary");
             element("ContactPerson", geoServer.getContactPerson());
             element("ContactOrganization", geoServer.getContactOrganization());
             end("ContactPersonPrimary");
 
             element("ContactPosition", geoServer.getContactPosition());
 
             start("ContactAddress");
             element("AddressType", geoServer.getAddressType());
             element("Address", geoServer.getAddress());
             element("City", geoServer.getAddressCity());
             element("StateOrProvince", geoServer.getAddressState());
             element("PostCode", geoServer.getAddressPostalCode());
             element("Country", geoServer.getAddressCountry());
             end("ContactAddress");
 
             element("ContactVoiceTelephone", geoServer.getContactVoice());
             element("ContactFacsimileTelephone", geoServer.getContactFacsimile());
             element("ContactElectronicMailAddress", geoServer.getContactEmail());
 
             end("ContactInformation");
         }
 
         /**
          * Turns the keyword list to XML
          *
          * @param keywords
          */
         private void handleKeywordList(List keywords) {
             start("KeywordList");
 
             for (Iterator it = keywords.iterator(); it.hasNext();) {
                 element("Keyword", String.valueOf(it.next()));
             }
 
             end("KeywordList");
         }
 
         /**
          * Turns the metadata URL list to XML
          *
          * @param keywords
          */
         private void handleMetadataList(List metadataURLs) {
             if (metadataURLs == null) {
                 return;
             }
 
             for (Iterator it = metadataURLs.iterator(); it.hasNext();) {
                 MetaDataLink link = (MetaDataLink) it.next();
 
                 AttributesImpl lnkAtts = new AttributesImpl();
                 lnkAtts.addAttribute("", "type", "type", "", link.getMetadataType());
                 start("MetadataURL", lnkAtts);
 
                 element("Format", link.getType());
 
                 AttributesImpl orAtts = new AttributesImpl();
                 orAtts.addAttribute("", "xmlns:xlink", "xmlns:xlink", "", XLINK_NS);
                 orAtts.addAttribute(XLINK_NS, "xlink:type", "xlink:type", "", "simple");
                 orAtts.addAttribute("", "xlink:href", "xlink:href", "", link.getContent());
                 element("OnlineResource", null, orAtts);
 
                 end("MetadataURL");
             }
         }
 
         /**
          * Encodes the capabilities metadata section of a WMS capabilities
          * document
          */
         private void handleCapability() {
             start("Capability");
             handleRequest();
             handleException();
             handleSLD();
             handleLayers();
             end("Capability");
         }
 
         /**
          * DOCUMENT ME!
          */
         private void handleRequest() {
             WMS wms = (WMS) request.getServiceRef().getServiceRef();
 
             start("Request");
 
             start("GetCapabilities");
             element("Format", WMS_CAPS_MIME);
 
             // @HACK: pointer to the WMS dispatcher
             String serviceUrl = request.getBaseUrl() + "wms?SERVICE=WMS&";
 
             handleDcpType(serviceUrl, serviceUrl);
             end("GetCapabilities");
 
             start("GetMap");
 
             for (Iterator it = formats.iterator(); it.hasNext();) {
                 element("Format", String.valueOf(it.next()));
             }
 
             handleDcpType(serviceUrl, null);
             end("GetMap");
 
             start("GetFeatureInfo");
 
             for (Iterator it = GetFeatureInfoResponse.getFormats().iterator(); it.hasNext();) {
                 element("Format", String.valueOf(it.next()));
             }
 
             handleDcpType(serviceUrl, serviceUrl);
             end("GetFeatureInfo");
 
             start("DescribeLayer");
             element("Format", DescribeLayerResponse.DESCLAYER_MIME_TYPE);
             handleDcpType(serviceUrl, null);
             end("DescribeLayer");
 
             start("GetLegendGraphic");
 
             for (Iterator it = GetLegendGraphicResponse.getFormats(applicationContext).iterator();
                     it.hasNext();) {
                 element("Format", String.valueOf(it.next()));
             }
 
             handleDcpType(serviceUrl, null);
             end("GetLegendGraphic");
 
             end("Request");
         }
 
         /**
          * Encodes a <code>DCPType</code> fragment for HTTP GET and POST
          * methods.
          *
          * @param getUrl the URL of the onlineresource for HTTP GET method
          *        requests
          * @param postUrl the URL of the onlineresource for HTTP POST method
          *        requests
          */
         private void handleDcpType(String getUrl, String postUrl) {
             AttributesImpl orAtts = new AttributesImpl();
             orAtts.addAttribute("", "xmlns:xlink", "xmlns:xlink", "", XLINK_NS);
             orAtts.addAttribute("", "xlink:type", "xlink:type", "", "simple");
             orAtts.addAttribute("", "xlink:href", "xlink:href", "", getUrl);
             start("DCPType");
             start("HTTP");
 
             if (getUrl != null) {
                 start("Get");
                 element("OnlineResource", null, orAtts);
                 end("Get");
             }
 
             if (postUrl != null) {
                 orAtts.setAttribute(2, "", "xlink:href", "xlink:href", "", postUrl);
                 start("Post");
                 element("OnlineResource", null, orAtts);
                 end("Post");
             }
 
             end("HTTP");
             end("DCPType");
         }
 
         /**
          * DOCUMENT ME!
          */
         private void handleException() {
             start("Exception");
 
             WMS wms = (WMS) request.getServiceRef().getServiceRef();
             Iterator it = Arrays.asList(wms.getExceptionFormats()).iterator();
 
             while (it.hasNext()) {
                 element("Format", String.valueOf(it.next()));
             }
 
             end("Exception");
         }
 
         /**
          * DOCUMENT ME!
          */
         private void handleSLD() {
             AttributesImpl sldAtts = new AttributesImpl();
             WMS config = (WMS) request.getServiceRef().getServiceRef();
             String supportsSLD = config.supportsSLD() ? "1" : "0";
             String supportsUserLayer = config.supportsUserLayer() ? "1" : "0";
             String supportsUserStyle = config.supportsUserStyle() ? "1" : "0";
             String supportsRemoteWFS = config.supportsRemoteWFS() ? "1" : "0";
             sldAtts.addAttribute("", "SupportSLD", "SupportSLD", "", supportsSLD);
             sldAtts.addAttribute("", "UserLayer", "UserLayer", "", supportsUserLayer);
             sldAtts.addAttribute("", "UserStyle", "UserStyle", "", supportsUserStyle);
             sldAtts.addAttribute("", "RemoteWFS", "RemoteWFS", "", supportsRemoteWFS);
 
             start("UserDefinedSymbolization", sldAtts);
             //          djb: this was removed, even though they are correct - the CITE tests have an incorrect DTD
             //       element("SupportedSLDVersion","1.0.0");  //djb: added that we support this.  We support partial 1.1
             end("UserDefinedSymbolization");
 
             //element("UserDefinedSymbolization", null, sldAtts);
         }
 
         /**
          * Handles the encoding of the layers elements.
          *
          * <p>
          * This method does a search over the SRS of all the layers to see if
          * there are at least a common one, as needed by the spec: "<i>The root
          * Layer element shall include a sequence of zero or more &lt;SRS&gt;
          * elements listing all SRSes that are common to all subsidiary layers.
          * Use a single SRS element with empty content (like so:
          * "&lt;SRS&gt;&lt;/SRS&gt;") if there is no common SRS."</i>
          * </p>
          *
          * <p>
          * By the other hand, this search is also used to collecto the whole
          * latlon bbox, as stated by the spec: <i>"The bounding box metadata in
          * Capabilities XML specify the minimum enclosing rectangle for the
          * layer as a whole."</i>
          * </p>
          *
          * @task TODO: manage this differently when we have the layer list of
          *       the WMS service decoupled from the feature types configured for
          *       the server instance. (This involves nested layers,
          *       gridcoverages, etc)
          */
         private void handleLayers() {
             WMS wms = (WMS) request.getServiceRef().getServiceRef();
             start("Layer");
 
             Data catalog = wms.getData();
             Collection ftypes = catalog.getFeatureTypeInfos().values();
             Collection coverages = catalog.getCoverageInfos().values();
 
             element("Title", wms.getTitle());
             element("Abstract", wms.getAbstract());
 
             handleRootSRSAndBbox(ftypes, MapLayerInfo.TYPE_VECTOR);
 
             // now encode each layer individually
             LayerTree featuresLayerTree = new LayerTree(ftypes);
             handleFeaturesTree(featuresLayerTree);
 
             LayerTree coveragesLayerTree = new LayerTree(coverages);
             handleCoveragesTree(coveragesLayerTree);
 
             end("Layer");
         }
 
         /**
          * Called from <code>handleLayers()</code>, does the first iteration
          * over the available featuretypes to look for common SRS's and
          * summarize their LatLonBBox'es, to state at the root layer.
          *
          * <p>
          * NOTE: by now we just have "layer.getSRS()", so the search is done
          * against this only SRS.
          * </p>
          *
          * @param ftypes
          *            DOCUMENT ME!
          *
          * @throws RuntimeException
          *             DOCUMENT ME!
          *
          * @task TODO: figure out how to incorporate multiple SRS using the
          *       reprojection facilities from gt2
          */
         private void handleRootSRSAndBbox(Collection ftypes, int TYPE) {
             String commonSRS = "";
             boolean isCommonSRS = true;
             Envelope latlonBbox = new Envelope();
             Envelope layerBbox = null;
 
             if (LOGGER.isLoggable(Level.FINER)) {
                 LOGGER.finer("Collecting summarized latlonbbox and common SRS...");
             }
 
             if (TYPE == MapLayerInfo.TYPE_VECTOR) {
                 FeatureTypeInfo layer;
 
                 for (Iterator it = ftypes.iterator(); it.hasNext();) {
                     layer = (FeatureTypeInfo) it.next();
 
                     if (layer.isEnabled()) {
                         try {
                             layerBbox = layer.getLatLongBoundingBox();
                         } catch (IOException e) {
                             throw new RuntimeException("Can't obtain latLonBBox of "
                                 + layer.getName() + ": " + e.getMessage(), e);
                         }
 
                         latlonBbox.expandToInclude(layerBbox);
 
                         String layerSRS = layer.getSRS();
 
                         if ("".equals(commonSRS)) {
                             commonSRS = layerSRS;
                         } else if (!commonSRS.equals(layerSRS)) {
                             isCommonSRS = false;
                         }
                     }
                 }
 
                 if (isCommonSRS) {
                     commonSRS = EPSG + commonSRS;
                     LOGGER.fine("Common SRS is " + commonSRS);
                 } else {
                     commonSRS = "";
                     LOGGER.fine(
                         "No common SRS, don't forget to incorporate reprojection support...");
                 }
 
                 if (!(commonSRS.equals(""))) {
                     comment("common SRS:");
                     element("SRS", commonSRS);
                 }
 
                 // okay - we've sent out the commonSRS, if it exists.
                 comment("All supported EPSG projections:");
 
                 try {
                     Set s = CRS.getSupportedCodes("EPSG");
                     Iterator it = s.iterator();
                     String currentSRS;
 
                     while (it.hasNext()) {
                         // do not output srs if it was output as common srs
                         // note, if commonSRS is "", this will not match
                         currentSRS = it.next().toString();
 
                         if (!currentSRS.equals(commonSRS)) {
                             element("SRS", currentSRS);
                         }
                     }
                 } catch (Exception e) {
                     if (LOGGER.isLoggable(Level.WARNING)) {
                         LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
                     }
                 }
 
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.fine("Summarized LatLonBBox is " + latlonBbox);
                 }
 
                 handleLatLonBBox(latlonBbox);
             } else if (TYPE == MapLayerInfo.TYPE_RASTER) {
                 CoverageInfo layer;
 
                 for (Iterator it = ftypes.iterator(); it.hasNext();) {
                     layer = (CoverageInfo) it.next();
 
                     if (layer.isEnabled()) {
                         final GeneralEnvelope bbox = layer.getWGS84LonLatEnvelope();
                         layerBbox = new Envelope(bbox.getLowerCorner().getOrdinate(0),
                                 bbox.getUpperCorner().getOrdinate(0),
                                 bbox.getLowerCorner().getOrdinate(1),
                                 bbox.getUpperCorner().getOrdinate(1));
 
                         latlonBbox.expandToInclude(layerBbox);
 
                         String layerSRS = ((!layer.getSrsName().equals("UNKNOWN"))
                             ? layer.getSrsName() : layer.getNativeCRS());
 
                         if ("".equals(commonSRS)) {
                             commonSRS = layerSRS;
                         } else if (!commonSRS.equals(layerSRS)) {
                             isCommonSRS = false;
                         }
                     }
                 }
 
                 if (isCommonSRS) {
                     // commonSRS = EPSG + commonSRS;
                     if (LOGGER.isLoggable(Level.FINE)) {
                         LOGGER.fine(new StringBuffer("Common SRS is ").append(commonSRS).toString());
                     }
                 } else {
                     commonSRS = "";
 
                     if (LOGGER.isLoggable(Level.FINE)) {
                         LOGGER.fine(
                             "No common SRS, don't forget to incorporate reprojection support...");
                     }
                 }
 
                 if (!(commonSRS.equals(""))) {
                     comment("common SRS:");
                     element("SRS", commonSRS);
                 }
 
                 // okay - we've sent out the commonSRS, if it exists.
                 comment("All supported EPSG projections:");
 
                 try {
                     String currentSRS;
                     ArrayList SRSs = new ArrayList(ftypes.size());
 
                     for (Iterator it = ftypes.iterator(); it.hasNext();) {
                         layer = (CoverageInfo) it.next();
 
                         if (layer.isEnabled()) {
                             List s = layer.getRequestCRSs();
                             Iterator it_1 = s.iterator();
 
                             while (it_1.hasNext()) {
                                 // do not output srs if it was output as common
                                 // srs
                                 // note, if commonSRS is "", this will not match
                                 currentSRS = it_1.next().toString();
 
                                 if (!currentSRS.equals(commonSRS) && !SRSs.contains(currentSRS)) {
                                     SRSs.add(currentSRS);
                                    element("SRS", EPSG + currentSRS);
                                 }
                             }
                         }
                     }
                 } catch (Exception e) {
                     if (LOGGER.isLoggable(Level.WARNING)) {
                         LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
                     }
                 }
 
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.fine("Summarized LatLonBBox is " + latlonBbox);
                 }
 
                 handleLatLonBBox(latlonBbox);
             }
         }
 
         /**
          * @param featuresLayerTree
          */
         private void handleFeaturesTree(LayerTree featuresLayerTree) {
             final Collection data = featuresLayerTree.getData();
             final Collection childrens = featuresLayerTree.getChildrens();
             FeatureTypeInfo fLayer;
 
             for (Iterator it = data.iterator(); it.hasNext();) {
                 fLayer = (FeatureTypeInfo) it.next();
 
                 if (fLayer.isEnabled()) {
                     handleFeatureType(fLayer);
                 }
             }
 
             LayerTree layerTree;
 
             for (Iterator it = childrens.iterator(); it.hasNext();) {
                 layerTree = (LayerTree) it.next();
                 start("Layer");
                 element("Name", layerTree.getName());
                 element("Title", layerTree.getName());
                 handleFeaturesTree(layerTree);
                 end("Layer");
             }
         }
 
         /**
          * Calls super.handleFeatureType to add common FeatureType content such
          * as Name, Title and LatLonBoundingBox, and then writes WMS specific
          * layer properties as Styles, Scale Hint, etc.
          *
          * @param ftype
          *            The featureType to write out.
          *
          * @throws RuntimeException
          *             DOCUMENT ME!
          *
          * @task TODO: write wms specific elements.
          */
         protected void handleFeatureType(FeatureTypeInfo ftype) {
             // HACK: by now all our layers are queryable, since they reference
             // only featuretypes managed by this server
             AttributesImpl qatts = new AttributesImpl();
             qatts.addAttribute("", "queryable", "queryable", "", "1");
             start("Layer", qatts);
             element("Name", ftype.getName());
             element("Title", ftype.getTitle());
             element("Abstract", ftype.getAbstract());
 
             handleKeywordList(ftype.getKeywords());
 
             /**
              * @task REVISIT: should getSRS() return the full URL? no - the spec
              *       says it should be a set of <SRS>EPSG:#</SRS>...
              */
             element("SRS", EPSG + ftype.getSRS());
 
             // DJB: I want to be nice to the people reading the capabilities
             // file - I'm going to get the
             // human readable name and stick it in the capabilities file
             // NOTE: this isnt well done because "comment()" isnt in the
             // ContentHandler interface...
             try {
                 CoordinateReferenceSystem crs = CRS.decode(EPSG + ftype.getSRS(), true);
                 String desc = new StringBuffer("WKT definition of this CRS:\n").append(crs.toWKT())
                                                                                .toString();
                 comment(desc);
             } catch (Exception e) {
                 if (LOGGER.isLoggable(Level.WARNING)) {
                     LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
                 }
             }
 
             Envelope bbox = null;
             Envelope llbbox = null;
 
             try {
                 bbox = ftype.getBoundingBox();
                 llbbox = ftype.getLatLongBoundingBox();
             } catch (IOException ex) {
                 throw new RuntimeException("Can't obtain latLongBBox of " + ftype.getName() + ": "
                     + ex.getMessage(), ex);
             }
 
             handleLatLonBBox(llbbox);
             handleBBox(bbox, EPSG + ftype.getSRS());
             // handle metadata URLs
             handleMetadataList(ftype.getMetadataLinks());
 
             // add the layer style
             start("Style");
 
             Style ftStyle = ftype.getDefaultStyle();
             element("Name", ftStyle.getName());
             element("Title", ftStyle.getTitle());
             element("Abstract", ftStyle.getAbstract());
             handleLegendURL(ftype);
             end("Style");
 
             final ArrayList styles = ftype.getStyles();
             Iterator s_IT = styles.iterator();
 
             while (s_IT.hasNext()) {
                 ftStyle = (Style) s_IT.next();
                 start("Style");
                 element("Name", ftStyle.getName());
                 element("Title", ftStyle.getTitle());
                 element("Abstract", ftStyle.getAbstract());
                 handleLegendURL(ftype);
                 end("Style");
             }
 
             end("Layer");
         }
 
         /**
          * @param coveragesLayerTree
          */
         private void handleCoveragesTree(LayerTree coveragesLayerTree) {
             final Collection data = coveragesLayerTree.getData();
             final Collection childrens = coveragesLayerTree.getChildrens();
             CoverageInfo cLayer;
 
             for (Iterator it = data.iterator(); it.hasNext();) {
                 cLayer = (CoverageInfo) it.next();
 
                 if (cLayer.isEnabled()) {
                     handleCoverage(cLayer);
                 }
             }
 
             LayerTree layerTree;
 
             for (Iterator it = childrens.iterator(); it.hasNext();) {
                 layerTree = (LayerTree) it.next();
                 start("Layer");
                 element("Name", layerTree.getName());
                 element("Title", layerTree.getName());
                 handleCoveragesTree(layerTree);
                 end("Layer");
             }
         }
 
         protected void handleCoverage(CoverageInfo coverage) {
             // HACK: by now all our layers are queryable, since they reference
             // only featuretypes managed by this server
             AttributesImpl qatts = new AttributesImpl();
             qatts.addAttribute("", "queryable", "queryable", "", "0");
             // qatts.addAttribute("", "opaque", "opaque", "", "1");
             // qatts.addAttribute("", "cascaded", "cascaded", "", "1");
             start("Layer", qatts);
             element("Name", coverage.getName());
             element("Title", coverage.getLabel());
             element("Abstract", coverage.getDescription());
 
             handleKeywordList(coverage.getKeywords());
 
             String desc = "WKT definition of this CRS:\n" + coverage.getSrsWKT();
             comment(desc);
 
             String authority = ((!coverage.getSrsName().equals("UNKNOWN")) ? coverage.getSrsName()
                                                                            : coverage.getNativeCRS());
             /*CoordinateReferenceSystem crs = coverage.getCrs();
             if (crs != null && !crs.getIdentifiers().isEmpty()) {
                     Identifier[] idents = (Identifier[]) crs.getIdentifiers()
                                     .toArray(new Identifier[crs.getIdentifiers().size()]);
                     authority = idents[0].toString();
             } else if (crs != null && crs instanceof DerivedCRS) {
                     final CoordinateReferenceSystem baseCRS = ((DerivedCRS) crs)
                                     .getBaseCRS();
                     if (baseCRS != null && !baseCRS.getIdentifiers().isEmpty())
                             authority = ((Identifier[]) baseCRS.getIdentifiers()
                                             .toArray(
                                                             new Identifier[baseCRS.getIdentifiers()
                                                                             .size()]))[0].toString();
                     else
                             authority = coverage.getNativeCRS();
             } else if (crs != null && crs instanceof ProjectedCRS) {
                     final CoordinateReferenceSystem baseCRS = ((ProjectedCRS) crs)
                                     .getBaseCRS();
                     if (baseCRS != null && !baseCRS.getIdentifiers().isEmpty())
                             authority = ((Identifier[]) baseCRS.getIdentifiers()
                                             .toArray(
                                                             new Identifier[baseCRS.getIdentifiers()
                                                                             .size()]))[0].toString();
                     else
                             authority = coverage.getNativeCRS();
             } else
                     authority = coverage.getNativeCRS();*/
             element("SRS", authority);
 
             GeneralEnvelope bounds = null;
             GeneralEnvelope llBounds = null;
 
             // try {
             // We need LON/LAT Envelopes
             // TODO check for BBOX, maybe it should be expressed in original
             // CRS coords!!
             final GeneralEnvelope latLonEnvelope = coverage.getWGS84LonLatEnvelope();
             // final CoordinateReferenceSystem llCRS = latLonEnvelope
             // .getCoordinateReferenceSystem();
             bounds = coverage.getEnvelope();
             llBounds = latLonEnvelope;
 
             // bounds =
             // CoverageStoreUtils.adjustEnvelopeLongitudeFirst(llCRS,
             // coverage.getEnvelope());
             // llBounds =
             // CoverageStoreUtils.adjustEnvelopeLongitudeFirst(llCRS,
             // // latLonEnvelope);
             // } catch (MismatchedDimensionException e) {
             //	
             // } catch (IndexOutOfBoundsException e) {
             //			
             // }
             final Envelope bbox = new Envelope(bounds.getLowerCorner().getOrdinate(0),
                     bounds.getUpperCorner().getOrdinate(0), bounds.getLowerCorner().getOrdinate(1),
                     bounds.getUpperCorner().getOrdinate(1));
 
             final Envelope llBbox = new Envelope(llBounds.getLowerCorner().getOrdinate(0),
                     llBounds.getUpperCorner().getOrdinate(0),
                     llBounds.getLowerCorner().getOrdinate(1),
                     llBounds.getUpperCorner().getOrdinate(1));
 
             handleLatLonBBox(llBbox);
             handleBBox(bbox, authority);
 
             // add the layer style
             start("Style");
 
             Style cvStyle = coverage.getDefaultStyle();
             element("Name", cvStyle.getName());
             element("Title", cvStyle.getTitle());
             element("Abstract", cvStyle.getAbstract());
             handleLegendURL(coverage);
             end("Style");
 
             final ArrayList styles = coverage.getStyles();
             Iterator s_IT = styles.iterator();
 
             while (s_IT.hasNext()) {
                 cvStyle = (Style) s_IT.next();
                 start("Style");
                 element("Name", cvStyle.getName());
                 element("Title", cvStyle.getTitle());
                 element("Abstract", cvStyle.getAbstract());
                 handleLegendURL(coverage);
                 end("Style");
             }
 
             end("Layer");
         }
 
         /**
          * Writes layer LegendURL pointing to the user supplied icon URL, if
          * any, or to the proper GetLegendGraphic operation if an URL was not
          * supplied by configuration file.
          *
          * <p>
          * It is common practice to supply a URL to a WMS accesible legend
          * graphic when it is difficult to create a dynamic legend for a layer.
          * </p>
          *
          * @param ft
          *            The FeatureTypeInfo that holds the legendURL to write out,
          *            or<code>null</code> if dynamically generated.
          *
          * @task TODO: figure out how to unhack legend parameters such as WIDTH,
          *       HEIGHT and FORMAT
          */
         protected void handleLegendURL(Object layer) {
             LegendURL legend = null;
             String layerName = null;
 
             if (layer instanceof FeatureTypeInfo) {
                 legend = ((FeatureTypeInfo) layer).getLegendURL();
                 layerName = ((FeatureTypeInfo) layer).getName();
             } else if (layer instanceof CoverageInfo) {
                 layerName = ((CoverageInfo) layer).getName();
             }
 
             if (legend != null) {
                 if (LOGGER.isLoggable(Level.CONFIG)) {
                     LOGGER.config("using user supplied legend URL");
                 }
 
                 AttributesImpl attrs = new AttributesImpl();
                 attrs.addAttribute("", "width", "width", "", String.valueOf(legend.getWidth()));
                 attrs.addAttribute("", "height", "height", "", String.valueOf(legend.getHeight()));
 
                 start("LegendURL", attrs);
 
                 element("Format", legend.getFormat());
                 attrs.clear();
                 attrs.addAttribute("", "xmlns:xlink", "xmlns:xlink", "", XLINK_NS);
                 attrs.addAttribute(XLINK_NS, "type", "xlink:type", "", "simple");
                 attrs.addAttribute(XLINK_NS, "href", "xlink:href", "", legend.getOnlineResource());
 
                 element("OnlineResource", null, attrs);
 
                 end("LegendURL");
             } else {
                 String defaultFormat = GetLegendGraphicRequest.DEFAULT_FORMAT;
 
                 if (!GetLegendGraphicResponse.supportsFormat(defaultFormat, applicationContext)) {
                     if (LOGGER.isLoggable(Level.WARNING)) {
                         LOGGER.warning(new StringBuffer("Default legend format (").append(
                                 defaultFormat)
                                                                                   .append(")is not supported (jai not available?), can't add LegendURL element")
                                                                                   .toString());
                     }
 
                     return;
                 }
 
                 if (LOGGER.isLoggable(Level.CONFIG)) {
                     LOGGER.config("Adding GetLegendGraphic call as LegendURL");
                 }
 
                 AttributesImpl attrs = new AttributesImpl();
                 attrs.addAttribute("", "width", "width", "",
                     String.valueOf(GetLegendGraphicRequest.DEFAULT_WIDTH));
 
                 // DJB: problem here is that we do not know the size of the
                 // legend apriori - we need
                 // to make one and find its height. Not the best way, but it
                 // would work quite well.
                 // This was advertising a 20*20 icon, but actually producing
                 // ones of a different size.
                 // An alternative is to just scale the resulting icon to what
                 // the server requested, but this isnt
                 // the nicest thing since warped images dont look nice. The
                 // client should do the warping.
 
                 // however, to actually estimate the size is a bit difficult.
                 // I'm going to do the scaling
                 // so it obeys the what the request says. For people with a
                 // problem with that should consider
                 // changing the default size here so that the request is for the
                 // correct size.
                 attrs.addAttribute("", "height", "height", "",
                     String.valueOf(GetLegendGraphicRequest.DEFAULT_HEIGHT));
 
                 start("LegendURL", attrs);
 
                 element("Format", defaultFormat);
                 attrs.clear();
 
                 StringBuffer onlineResource = new StringBuffer(this.request.getBaseUrl());
                 onlineResource.append("wms/GetLegendGraphic?VERSION=");
                 onlineResource.append(GetLegendGraphicRequest.SLD_VERSION);
                 onlineResource.append("&FORMAT=");
                 onlineResource.append(defaultFormat);
                 onlineResource.append("&WIDTH=");
                 onlineResource.append(GetLegendGraphicRequest.DEFAULT_WIDTH);
                 onlineResource.append("&HEIGHT=");
                 onlineResource.append(GetLegendGraphicRequest.DEFAULT_HEIGHT);
                 onlineResource.append("&LAYER=");
                 onlineResource.append(layerName);
 
                 attrs.addAttribute("", "xmlns:xlink", "xmlns:xlink", "", XLINK_NS);
                 attrs.addAttribute(XLINK_NS, "type", "xlink:type", "", "simple");
                 attrs.addAttribute(XLINK_NS, "href", "xlink:href", "", onlineResource.toString());
                 element("OnlineResource", null, attrs);
 
                 end("LegendURL");
             }
         }
 
         /**
          * Encodes a LatLonBoundingBox for the given Envelope.
          *
          * @param bbox
          */
         private void handleLatLonBBox(Envelope bbox) {
             String minx = String.valueOf(bbox.getMinX());
             String miny = String.valueOf(bbox.getMinY());
             String maxx = String.valueOf(bbox.getMaxX());
             String maxy = String.valueOf(bbox.getMaxY());
 
             AttributesImpl bboxAtts = new AttributesImpl();
             bboxAtts.addAttribute("", "minx", "minx", "", minx);
             bboxAtts.addAttribute("", "miny", "miny", "", miny);
             bboxAtts.addAttribute("", "maxx", "maxx", "", maxx);
             bboxAtts.addAttribute("", "maxy", "maxy", "", maxy);
 
             element("LatLonBoundingBox", null, bboxAtts);
         }
 
         /**
          * adds a comment to the output xml file. THIS IS A BIG HACK. TODO: do
          * this in the correct manner!
          *
          * @param comment
          */
         public void comment(String comment) {
             if (contentHandler instanceof TransformerIdentityImpl) // HACK HACK
                                                                    // HACK --
                                                                    // not sure
                                                                    // of the
                                                                    // proper
                                                                    // way to do
                                                                    // this.
              {
                 try {
                     TransformerIdentityImpl ch = (TransformerIdentityImpl) contentHandler;
                     ch.comment(comment.toCharArray(), 0, comment.length());
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         }
 
         /**
          * Encodes a BoundingBox for the given Envelope.
          *
          * @param bbox
          */
         private void handleBBox(Envelope bbox, String SRS) {
             String minx = String.valueOf(bbox.getMinX());
             String miny = String.valueOf(bbox.getMinY());
             String maxx = String.valueOf(bbox.getMaxX());
             String maxy = String.valueOf(bbox.getMaxY());
 
             AttributesImpl bboxAtts = new AttributesImpl();
             bboxAtts.addAttribute("", "SRS", "SRS", "", SRS);
             bboxAtts.addAttribute("", "minx", "minx", "", minx);
             bboxAtts.addAttribute("", "miny", "miny", "", miny);
             bboxAtts.addAttribute("", "maxx", "maxx", "", maxx);
             bboxAtts.addAttribute("", "maxy", "maxy", "", maxy);
 
             element("BoundingBox", null, bboxAtts);
         }
     }
 }
 
 
 /**
  * A Class to manage the WMS Layer structure
  *
  * @author fabiania
  *
  * TODO To change the template for this generated type comment go to Window -
  * Preferences - Java - Code Style - Code Templates
  */
 class LayerTree {
     private String name;
     private Collection childrens;
     private Collection data;
 
     /**
      * @param name
      *            String
      */
     public LayerTree(String name) {
         this.name = name;
         this.childrens = new ArrayList();
         this.data = new ArrayList();
     }
 
     /**
      * @param c
      *            Collection
      */
     public LayerTree(Collection c) {
         this.name = "";
         this.childrens = new ArrayList();
         this.data = new ArrayList();
 
         for (Iterator it = c.iterator(); it.hasNext();) {
             Object element = it.next();
 
             if (element instanceof CoverageInfo) {
                 CoverageInfo cLayer = (CoverageInfo) element;
 
                 if (cLayer.isEnabled()) {
                     String wmsPath = (((cLayer.getWmsPath() != null)
                         && (cLayer.getWmsPath().length() > 0)) ? cLayer.getWmsPath() : "/");
 
                     if (wmsPath.startsWith("/")) {
                         wmsPath = wmsPath.substring(1, wmsPath.length());
                     }
 
                     String[] treeStructure = wmsPath.split("/");
                     addToNode(this, treeStructure, cLayer);
                 }
             } else if (element instanceof FeatureTypeInfo) {
                 FeatureTypeInfo fLayer = (FeatureTypeInfo) element;
 
                 if (fLayer.isEnabled()) {
                     String wmsPath = (((fLayer.getWmsPath() != null)
                         && (fLayer.getWmsPath().length() > 0)) ? fLayer.getWmsPath() : "/");
 
                     if (wmsPath.startsWith("/")) {
                         wmsPath = wmsPath.substring(1, wmsPath.length());
                     }
 
                     String[] treeStructure = wmsPath.split("/");
                     addToNode(this, treeStructure, fLayer);
                 }
             }
         }
     }
 
     /**
      * @param tree
      * @param treeStructure
      * @param layer
      */
     private void addToNode(LayerTree tree, String[] treeStructure, CoverageInfo layer) {
         final int length = treeStructure.length;
 
         if ((length == 0) || (treeStructure[0].length() == 0)) {
             tree.data.add(layer);
         } else {
             LayerTree node = tree.getNode(treeStructure[0]);
 
             if (node == null) {
                 node = new LayerTree(treeStructure[0]);
                 tree.childrens.add(node);
             }
 
             String[] subTreeStructure = new String[length - 1];
             System.arraycopy(treeStructure, 1, subTreeStructure, 0, length - 1);
             addToNode(node, subTreeStructure, layer);
         }
     }
 
     private void addToNode(LayerTree tree, String[] treeStructure, FeatureTypeInfo layer) {
         final int length = treeStructure.length;
 
         if ((length == 0) || (treeStructure[0].length() == 0)) {
             tree.data.add(layer);
         } else {
             LayerTree node = tree.getNode(treeStructure[0]);
 
             if (node == null) {
                 node = new LayerTree(treeStructure[0]);
                 tree.childrens.add(node);
             }
 
             String[] subTreeStructure = new String[length - 1];
             System.arraycopy(treeStructure, 1, subTreeStructure, 0, length - 1);
             addToNode(node, subTreeStructure, layer);
         }
     }
 
     /**
      * @param string
      * @return
      */
     public LayerTree getNode(String name) {
         LayerTree node = null;
 
         for (Iterator it = this.childrens.iterator(); it.hasNext();) {
             LayerTree tmpNode = (LayerTree) it.next();
 
             if (tmpNode.name.equals(name)) {
                 node = tmpNode;
             }
         }
 
         return node;
     }
 
     public Collection getChildrens() {
         return childrens;
     }
 
     public Collection getData() {
         return data;
     }
 
     public String getName() {
         return name;
     }
 }
