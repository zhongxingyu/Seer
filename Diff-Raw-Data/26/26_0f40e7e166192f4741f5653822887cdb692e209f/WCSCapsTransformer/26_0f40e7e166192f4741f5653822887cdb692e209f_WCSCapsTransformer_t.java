 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wcs.responses;
 
 import static org.geoserver.ows.util.ResponseUtils.*;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 
 import org.geoserver.catalog.Catalog;
 import org.geoserver.catalog.CoverageInfo;
 import org.geoserver.catalog.MetadataLinkInfo;
 import org.geoserver.config.ContactInfo;
 import org.geoserver.config.GeoServerInfo;
 import org.geoserver.ows.URLMangler.URLType;
 import org.geoserver.ows.util.ResponseUtils;
 import org.geoserver.wcs.WCSInfo;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.xml.transform.TransformerBase;
 import org.geotools.xml.transform.Translator;
 import org.springframework.context.ApplicationContext;
 import org.vfny.geoserver.global.CoverageInfoLabelComparator;
 import org.vfny.geoserver.util.requests.CapabilitiesRequest;
 import org.vfny.geoserver.wcs.requests.WCSRequest;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.AttributesImpl;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last
  *         modification)
  * @author $Author: Simone Giannecchini (simboss1@gmail.com) $ (last
  *         modification)
  */
 public class WCSCapsTransformer extends TransformerBase {
     /** DOCUMENT ME! */
     private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(WCSCapsTransformer.class.getPackage()
                                                                                   .getName());
     protected static final String WCS_URI = "http://www.opengis.net/wcs";
 
     /** DOCUMENT ME! */
     protected static final String WFS_URI = "http://www.opengis.net/wcs";
 
     /** DOCUMENT ME! */
     protected static final String CUR_VERSION = "1.0.0";
 
     /** DOCUMENT ME! */
     protected static final String XSI_PREFIX = "xsi";
 
     /** DOCUMENT ME! */
     protected static final String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";
     
     private static final String[] EXCEPTION_FORMATS = {
         "application/vnd.ogc.se_xml", "application/vnd.ogc.se_inimage",
         "application/vnd.ogc.se_blank"
     };
 
     /** DOCUMENT ME! */
 	private String baseUrl;
 
 	private ApplicationContext applicationContext;
 	
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @uml.property name="request"
 	 * @uml.associationEnd multiplicity="(0 1)"
 	 */
 	protected WCSRequest request;
 
 	/**
 	 * Creates a new WFSCapsTransformer object.
 	 */
 	public WCSCapsTransformer(String baseUrl,
 			ApplicationContext applicationContext) {
 		super();
 		if (baseUrl == null) {
 			throw new NullPointerException();
 		}
 
 		this.baseUrl = baseUrl;
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
     	return new WCSCapsTranslator(handler, applicationContext);
     }
 
     /**
 	 * DOCUMENT ME!
 	 * 
 	 * @return a Transformer propoerly configured to produce DescribeLayer
 	 *         responses.
 	 * 
 	 * @throws TransformerException
 	 *             if it is thrown by <code>super.createTransformer()</code>
 	 */
 	public Transformer createTransformer() throws TransformerException {
 		return super.createTransformer();
 	}
 	
     /**
      * DOCUMENT ME!
      *
      * @author Gabriel Roldan, Axios Engineering
      * @version $Id
      */
     private static class WCSCapsTranslator extends TranslatorSupport {
     	/**
 		 * DOCUMENT ME!
 		 * 
 		 * @uml.property name="request"
 		 * @uml.associationEnd multiplicity="(0 1)"
 		 */
 		private CapabilitiesRequest request;
 
 		private ApplicationContext applicationContext;
 
 		/**
 		 * Creates a new WFSCapsTranslator object.
 		 * 
 		 * @param handler
 		 *            DOCUMENT ME!
 		 */
 		public WCSCapsTranslator(ContentHandler handler,
 				ApplicationContext applicationContext) {
 			super(handler, null, null);
 			this.applicationContext = applicationContext;
 		}
 
         /**
          * Encode the object.
          *
          * @param o
          *            The Object to encode.
          *
          * @throws IllegalArgumentException
          *             if the Object is not encodeable.
          */
         public void encode(Object o) throws IllegalArgumentException {
             if (!(o instanceof CapabilitiesRequest)) {
                 throw new IllegalArgumentException(new StringBuffer("Not a CapabilitiesRequest: ").append(
                         o).toString());
             }
 
             this.request = (CapabilitiesRequest) o;
 			final WCSInfo wcs = (WCSInfo) request.getServiceConfig();
 
             final AttributesImpl attributes = new AttributesImpl();
             attributes.addAttribute("", "version", "version", "", CUR_VERSION);
             attributes.addAttribute("", "xmlns", "xmlns", "", WCS_URI);
 
             attributes.addAttribute("", "xmlns:xlink", "xmlns:xlink", "",
                 "http://www.w3.org/1999/xlink");
             attributes.addAttribute("", "xmlns:ogc", "xmlns:ogc", "", "http://www.opengis.net/ogc");
             attributes.addAttribute("", "xmlns:gml", "xmlns:gml", "", "http://www.opengis.net/gml");
 
             final String prefixDef = new StringBuffer("xmlns:").append(XSI_PREFIX).toString();
             attributes.addAttribute("", prefixDef, prefixDef, "", XSI_URI);
 
             final String locationAtt = new StringBuffer(XSI_PREFIX).append(":schemaLocation")
                                                                    .toString();
 
             GeoServerInfo gsInfo = wcs.getGeoServer().getGlobal();
 			final String locationDef = WCS_URI + " " + 
 			    buildURL(request.getBaseUrl(), appendPath(SCHEMAS, "schemas/wcs/1.0.0/wcsCapabilities.xsd"), null, URLType.RESOURCE);
 
             attributes.addAttribute("", locationAtt, locationAtt, "", locationDef);
             attributes.addAttribute("", "updateSequence", "updateSequence", "", gsInfo.getUpdateSequence() + "");
             start("WCS_Capabilities", attributes);
 
             handleService();
             handleCapabilities();
 
             end("WCS_Capabilities");
         }
 
         /**
          * Handles the service section of the capabilities document.
          *
          * @param config
          *            The OGC service to transform.
          *
          * @throws SAXException
          *             For any errors.
          */
         private void handleService() {
             final WCSInfo wcs = (WCSInfo) request.getServiceConfig();
             AttributesImpl attributes = new AttributesImpl();
            //attributes.addAttribute("", "version", "version", "", CUR_VERSION);
             start("Service", attributes);
             if(wcs.getMetadataLink() != null)
                 handleMetadataLink(Collections.singletonList(wcs.getMetadataLink()));
             element("description", wcs.getAbstract());
             element("name", wcs.getName());
             element("label", wcs.getTitle());
             handleKeywords(wcs.getKeywords());
             handleContact(wcs);
 
             String fees = wcs.getFees();
 
             if ((fees == null) || "".equals(fees)) {
                 fees = "NONE";
             }
 
             element("fees", fees);
 
             String accessConstraints = wcs.getAccessConstraints();
 
             if ((accessConstraints == null) || "".equals(accessConstraints)) {
                 accessConstraints = "NONE";
             }
 
             element("accessConstraints", accessConstraints);
             end("Service");
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param serviceConfig
          *            DOCUMENT ME!
          *
          * @throws SAXException
          *             DOCUMENT ME!
          */
         private void handleCapabilities() {
             final WCSInfo wcs = (WCSInfo) request.getServiceConfig();
             start("Capability");
             handleRequest(wcs);
             handleExceptions(wcs);
             handleVendorSpecifics(wcs);
             end("Capability");
 
             handleContentMetadata(wcs);
         }
 
         /**
          * Handles the request portion of the document, printing out the
          * capabilities and where to bind to them.
          *
          * @param config
          *            The global wms.
          *
          * @throws SAXException
          *             For any problems.
          */
         private void handleRequest(WCSInfo config) {
             start("Request");
             handleCapability(config, "GetCapabilities");
             handleCapability(config, "DescribeCoverage");
             handleCapability(config, "GetCoverage");
             end("Request");
         }
 
         private void handleCapability(WCSInfo config, String capabilityName) {
             AttributesImpl attributes = new AttributesImpl();
             start(capabilityName);
 
             start("DCPType");
             start("HTTP");
 
             String baseURL = buildURL(request.getBaseUrl(), "wcs", null, URLType.EXTERNAL);
             // ensure ends in "?" or "&"
             if(baseURL.indexOf('?') == -1) {
                 baseURL = ResponseUtils.appendQueryString(baseURL, "");
             }
 
             attributes.addAttribute("", "xlink:href", "xlink:href", "", baseURL);
 
             start("Get");
             start("OnlineResource", attributes);
             end("OnlineResource");
             end("Get");
             end("HTTP");
             end("DCPType");
 
             attributes = new AttributesImpl();
             attributes.addAttribute("", "xlink:href", "xlink:href", "", baseURL);
 
             start("DCPType");
             start("HTTP");
             start("Post");
             start("OnlineResource", attributes);
             end("OnlineResource");
             end("Post");
             end("HTTP");
             end("DCPType");
             end(capabilityName);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param kwords
          *            DOCUMENT ME!
          *
          * @throws SAXException
          *             DOCUMENT ME!
          */
         private void handleKeywords(List kwords) {
             start("keywords");
 
             if (kwords != null) {
                 for (Iterator it = kwords.iterator(); it.hasNext();) {
                     element("keyword", it.next().toString());
                 }
             }
 
             end("keywords");
         }
 
         /**
          * Handles contacts.
          *
          * @param config
          *            the service.
          */
         private void handleContact(WCSInfo config) {
             String tmp = "";
 
             GeoServerInfo geoServer = config.getGeoServer().getGlobal();
 			ContactInfo contact = geoServer.getContact();
 			String cp = contact.getContactPerson();
 			String org = contact.getContactOrganization();
 			if (((cp != null) && (cp != ""))
                     || ((org != null) && (org != ""))) {
                 start("responsibleParty");
 
                 tmp = cp;
 
                 if ((tmp != null) && (tmp != "")) {
                     element("individualName", tmp);
 
                     tmp = org;
 
                     if ((tmp != null) && (tmp != "")) {
                         element("organisationName", tmp);
                     }
                 } else {
                     tmp = org;
 
                     if ((tmp != null) && (tmp != "")) {
                         element("organisationName", tmp);
                     }
                 }
 
                 tmp = contact.getContactPosition();
 
                 if ((tmp != null) && (tmp != "")) {
                     element("positionName", tmp);
                 }
 
                 start("contactInfo");
 
                 start("phone");
                 tmp = contact.getContactVoice();
 
                 if ((tmp != null) && (tmp != "")) {
                     element("voice", tmp);
                 }
 
                 tmp = contact.getContactFacsimile();
 
                 if ((tmp != null) && (tmp != "")) {
                     element("facsimile", tmp);
                 }
 
                 end("phone");
 
                 start("address");
                 tmp = contact.getAddressType();
 
                 if ((tmp != null) && (tmp != "")) {
                     String addr = "";
                     addr = contact.getAddress();
 
                     if ((addr != null) && (addr != "")) {
                         element("deliveryPoint", tmp + " " + addr);
                     }
                 } else {
                     tmp = contact.getAddress();
 
                     if ((tmp != null) && (tmp != "")) {
                         element("deliveryPoint", tmp);
                     }
                 }
 
                 tmp = contact.getAddressCity();
 
                 if ((tmp != null) && (tmp != "")) {
                     element("city", tmp);
                 }
 
                 tmp = contact.getAddressState();
 
                 if ((tmp != null) && (tmp != "")) {
                     element("administrativeArea", tmp);
                 }
 
                 tmp = contact.getAddressPostalCode();
 
                 if ((tmp != null) && (tmp != "")) {
                     element("postalCode", tmp);
                 }
 
                 tmp = contact.getAddressCountry();
 
                 if ((tmp != null) && (tmp != "")) {
                     element("country", tmp);
                 }
 
                 tmp = contact.getContactEmail();
 
                 if ((tmp != null) && (tmp != "")) {
                     element("electronicMailAddress", tmp);
                 }
 
                 end("address");
 
                 tmp = geoServer.getOnlineResource();
 
                 if ((tmp != null) && (tmp != "")) {
                     AttributesImpl attributes = new AttributesImpl();
                     attributes.addAttribute("", "xlink:href", "xlink:href", "", tmp);
                     start("onlineResource", attributes);
                     end("onlineResource");
                 }
 
                 end("contactInfo");
 
                 end("responsibleParty");
             }
         }
 
         /**
          * Handles the printing of the exceptions information, prints the
          * formats that GeoServer can return exceptions in.
          *
          * @param config
          *            The wms service global config.
          *
          * @throws SAXException
          *             For any problems.
          */
         private void handleExceptions(WCSInfo config) {
             start("Exception");
 
             for(String format : EXCEPTION_FORMATS) {
             	element("Format", format);
             }
 
             end("Exception");
         }
 
         /**
          * Handles the vendor specific capabilities. Right now there are none,
          * so we do nothing.
          *
          * @param config
          *            The global config that may contain vendor specifics.
          *
          * @throws SAXException
          *             For any problems.
          */
         private void handleVendorSpecifics(WCSInfo config) {
         }
 
         private void handleEnvelope(ReferencedEnvelope envelope) {
             AttributesImpl attributes = new AttributesImpl();
 
             attributes.addAttribute("", "srsName", "srsName", "", /*"urn:ogc:def:crs:OGC:1.3:CRS84"*/
                 "WGS84(DD)");
             start("lonLatEnvelope", attributes);
             element("gml:pos",
                 new StringBuffer(Double.toString(envelope.getLowerCorner().getOrdinate(0))).append(
                     " ").append(envelope.getLowerCorner().getOrdinate(1)).toString());
             element("gml:pos",
                 new StringBuffer(Double.toString(envelope.getUpperCorner().getOrdinate(0))).append(
                     " ").append(envelope.getUpperCorner().getOrdinate(1)).toString());
             end("lonLatEnvelope");
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param metadataLink
          *            DOCUMENT ME!
          *
          * @throws SAXException
          *             DOCUMENT ME!
          */
         private void handleMetadataLink(List<MetadataLinkInfo> links) {
             for (MetadataLinkInfo mdl : links) {
                 AttributesImpl attributes = new AttributesImpl();
 
                 if ((mdl.getAbout() != null) && (mdl.getAbout() != "")) {
                     attributes.addAttribute("", "about", "about", "", mdl.getAbout());
                 }
 
                 // if( mdl.getType() != null && mdl.getType() != "" ) {
                 // attributes.addAttribute("", "type", "type", "",
                 // mdl.getType());
                 // }
                 if ((mdl.getMetadataType() != null) && (mdl.getMetadataType() != "")) {
                     attributes.addAttribute("", "metadataType", "metadataType", "",
                         mdl.getMetadataType());
                 }
 
                 if (attributes.getLength() > 0) {
                     start("metadataLink", attributes);
                     // characters(mdl.getContent());
                     end("metadataLink");
                 }
             }
         }
 
         private void handleContentMetadata(WCSInfo config) {
             AttributesImpl attributes = new AttributesImpl();
            //attributes.addAttribute("", "version", "version", "", CUR_VERSION);
 
             start("ContentMetadata", attributes);
 
             Catalog catalog = config.getGeoServer().getCatalog();
             List<CoverageInfo> coverages = catalog.getCoverages();
             
             // filter out disabled coverages
             for (Iterator it = coverages.iterator(); it.hasNext();) {
                 CoverageInfo cv = (CoverageInfo) it.next();
                 if(!cv.enabled())
                     it.remove();
             }
             
             // filter out coverages that are not in the requested namespace
             if(request.getNamespace() != null) {
                 String namespace = request.getNamespace();
                 for (Iterator it = coverages.iterator(); it.hasNext();) {
                     CoverageInfo cv = (CoverageInfo) it.next();
                     if(!namespace.equals(cv.getStore().getWorkspace().getName()))
                         it.remove();
                 }
             }
             
             Collections.sort(coverages, new CoverageInfoLabelComparator());
             for (Iterator i = coverages.iterator(); i.hasNext();) {
                 handleCoverageOfferingBrief(config,
                     (CoverageInfo) i.next());
             }
 
             end("ContentMetadata");
         }
 
         private void handleCoverageOfferingBrief(WCSInfo config, CoverageInfo cv) {
             if (cv.enabled()) {
                 start("CoverageOfferingBrief");
 
                 String tmp;
 
                 handleMetadataLink(cv.getMetadataLinks());
                 tmp = cv.getDescription();
 
                 if ((tmp != null) && (tmp != "")) {
                     element("description", tmp);
                 }
 
                 tmp = cv.getName();
 
                 if ((tmp != null) && (tmp != "")) {
                     element("name", tmp);
                 }
 
                 tmp = cv.getTitle();
 
                 if ((tmp != null) && (tmp != "")) {
                     element("label", tmp);
                 }
 
                 handleEnvelope(cv.getLatLonBoundingBox());
                 handleKeywords(cv.getKeywords());
 
                 end("CoverageOfferingBrief");
             }
         }
     }
 }
