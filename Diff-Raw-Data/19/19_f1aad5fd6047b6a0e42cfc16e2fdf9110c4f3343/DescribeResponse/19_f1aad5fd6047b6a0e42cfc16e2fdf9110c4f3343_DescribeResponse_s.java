 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wcs.responses;
 
 import java.awt.geom.AffineTransform;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TreeSet;
 import java.util.logging.Logger;
 
 import org.geoserver.catalog.Catalog;
 import org.geoserver.catalog.CoverageDimensionInfo;
 import org.geoserver.catalog.CoverageInfo;
 import org.geoserver.catalog.MetadataLinkInfo;
 import org.geoserver.config.GeoServer;
 import org.geoserver.config.ServiceInfo;
 import org.geoserver.wcs.WCSInfo;
 import org.geotools.factory.Hints;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.referencing.ReferencingFactoryFinder;
 import org.opengis.coverage.grid.GridGeometry;
 import org.opengis.referencing.crs.CRSAuthorityFactory;
 import org.opengis.referencing.crs.CRSFactory;
 import org.opengis.referencing.datum.DatumFactory;
 import org.opengis.referencing.operation.CoordinateOperationFactory;
 import org.opengis.referencing.operation.MathTransform;
 import org.opengis.referencing.operation.MathTransformFactory;
 import org.vfny.geoserver.Request;
 import org.vfny.geoserver.Response;
 import org.vfny.geoserver.wcs.WcsException;
 import org.vfny.geoserver.wcs.requests.DescribeRequest;
 import org.vfny.geoserver.wcs.requests.WCSRequest;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last
  *         modification)
  * @author $Author: Simone Giannecchini (simboss1@gmail.com) $ (last
  *         modification)
  */
 public class DescribeResponse implements Response {
     private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.vfny.geoserver.responses");
     private static final String CURR_VER = "\"1.0.0\"";
     private static final String WCS_URL = "http://www.opengis.net/wcs";
     private static final String WCS_NAMESPACE = new StringBuffer("\n  xmlns=\"").append(WCS_URL)
                                                                                 .append("\"")
                                                                                 .toString();
     private static final String XLINK_URL = "\"http://www.w3.org/1999/xlink\"";
     private static final String XLINK_NAMESPACE = new StringBuffer("\n  xmlns:xlink=").append(XLINK_URL)
                                                                                       .toString();
     private static final String OGC_URL = "\"http://www.opengis.net/ogc\"";
     private static final String OGC_NAMESPACE = new StringBuffer("\n  xmlns:ogc=").append(OGC_URL)
                                                                                   .toString();
     private static final String GML_URL = "\"http://www.opengis.net/gml\"";
     private static final String GML_NAMESPACE = new StringBuffer("\n  xmlns:gml=").append(GML_URL)
                                                                                   .toString();
     private static final String SCHEMA_URI = "\"http://www.w3.org/2001/XMLSchema-instance\"";
     private static final String XSI_NAMESPACE = new StringBuffer("\n  xmlns:xsi=").append(SCHEMA_URI)
                                                                                   .toString();
 
     /** Fixed return footer information */
     private static final String FOOTER = "\n</CoverageDescription>";
 
     /**
      *
      * @uml.property name="request"
      * @uml.associationEnd multiplicity="(0 1)"
      */
     private DescribeRequest request;
 
     /** Main XML class for interpretation and response. */
     private String xmlResponse = new String();
 
     /**
      * The default datum factory.
      *
      * @uml.property name="datumFactory"
      * @uml.associationEnd multiplicity="(1 1)"
      */
     protected final DatumFactory datumFactory = ReferencingFactoryFinder.getDatumFactory(null);
 
     /**
      * The service configuration bean this response works upon
      */
     private WCSInfo wcs;
 
     public DescribeResponse(final WCSInfo wcs) {
         this.wcs = wcs;
     }
 
     /**
      * Returns any extra headers that this service might want to set in the HTTP response object.
      * @see org.vfny.geoserver.Response#getResponseHeaders()
      */
     public HashMap getResponseHeaders() {
         return null;
     }
 
     /**
      * The default coordinate reference system factory.
      */
 
     // protected final static CRSFactory crsFactory =
     // FactoryFinder.getCRSFactory(new
     // Hints(Hints.CRS_AUTHORITY_FACTORY,EPSGCRSAuthorityFactory.class));
     protected final static CRSFactory crsFactory = ReferencingFactoryFinder.getCRSFactory(new Hints(
                 Hints.CRS_AUTHORITY_FACTORY, CRSAuthorityFactory.class));
 
     /**
      * The default math transform factory.
      *
      * @uml.property name="mtFactory"
      * @uml.associationEnd multiplicity="(1 1)"
      */
     protected final MathTransformFactory mtFactory = ReferencingFactoryFinder
         .getMathTransformFactory(null);
 
     /**
      * The default transformations factory.
      */
     protected final static CoordinateOperationFactory opFactory = ReferencingFactoryFinder
         .getCoordinateOperationFactory(new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE));
 
     public void execute(Request req) throws WcsException {
         WCSRequest request = (WCSRequest) req;
 
         if (!(request instanceof DescribeRequest)) {
             throw new WcsException(new StringBuffer(
                     "illegal request type, expected DescribeRequest, got ").append(request)
                                                                                                            .toString());
         }
 
         DescribeRequest wcsRequest = (DescribeRequest) request;
         this.request = wcsRequest;
         LOGGER.finer("processing describe request" + wcsRequest);
 
         String outputFormat = wcsRequest.getOutputFormat();
 
         if (!outputFormat.equalsIgnoreCase("XMLSCHEMA")) {
             throw new WcsException(new StringBuffer("output format: ").append(outputFormat)
                                                                       .append(" not ")
                                                                       .append("supported by geoserver")
                                                                       .toString());
         }
 
         // generates response, using general function
         xmlResponse = generateCoverages(wcsRequest);
 
         if (!request.getWCS().getGeoServer().getGlobal().isVerbose()) {
             xmlResponse = xmlResponse.replaceAll(">\n[ \\t\\n]*", ">");
             xmlResponse = xmlResponse.replaceAll("\n[ \\t\\n]*", " ");
         }
     }
 
     public String getContentType(GeoServer gs) {
         return "text/xml; charset=" + gs.getGlobal().getCharset();
     }
 
     public String getContentEncoding() {
         return null;
     }
 
     public String getContentDisposition() {
         return null;
     }
 
     public void writeTo(OutputStream out) throws WcsException {
         try {
             final Charset encoding = Charset.forName(wcs.getGeoServer().getGlobal().getCharset());
             Writer writer = new OutputStreamWriter(out, encoding);
             writer.write(xmlResponse);
             writer.flush();
         } catch (IOException ex) {
             throw new WcsException(ex, "", getClass().getName());
         }
     }
 
     private final String generateCoverages(DescribeRequest wcsRequest)
         throws WcsException {
         List requestedTypes = wcsRequest.getCoverages();
 
         // Initialize return information and intermediate return objects
         StringBuffer tempResponse = new StringBuffer();
 
         // ComplexType table = new ComplexType();
         if (requestedTypes.size() == 0) {
             // if there are no specific requested types then get all.
         	Catalog catalog = wcsRequest.getWCS().getGeoServer().getCatalog();
         	requestedTypes = new ArrayList();
         	for (CoverageInfo ci : catalog.getCoverages()) {
 				requestedTypes.add(ci.getName());
 			}
         }
 
         tempResponse.append("<?xml version=\"1.0\" encoding=\"")
                     .append(wcs.getGeoServer().getGlobal().getCharset()).append("\"?>")
                     .append("\n<CoverageDescription version=").append(CURR_VER).append(" ")
                     .toString();
 
         tempResponse.append(WCS_NAMESPACE);
         tempResponse.append(XLINK_NAMESPACE);
         tempResponse.append(OGC_NAMESPACE);
         tempResponse.append(GML_NAMESPACE);
         tempResponse.append(XSI_NAMESPACE);
         /*tempResponse.append(" xsi:schemaLocation=\"").append(WCS_URL).append(
                         " ").append(request.getSchemaBaseUrl()).append(
                         "wcs/1.0.0/describeCoverage.xsd\">\n\n");*/
         tempResponse.append(" xsi:schemaLocation=\"").append(WCS_URL).append(" ")
                     .append("http://schemas.opengis.net/wcs/1.0.0/")
                     .append("describeCoverage.xsd\">\n\n");
 
         tempResponse.append(generateSpecifiedCoverages(requestedTypes, wcsRequest.getWCS()));
 
         tempResponse.append(FOOTER);
 
         return tempResponse.toString();
     }
 
     private String generateSpecifiedCoverages(List requestedTypes, WCSInfo gs)
         throws WcsException {
         String tempResponse = new String();
         String curCoverageName = new String();
 
         final int length = requestedTypes.size();
         CoverageInfo meta;
 
         Catalog catalog = gs.getGeoServer().getCatalog();
         for (int i = 0; i < length; i++) {
             curCoverageName = requestedTypes.get(i).toString();
 
			meta = catalog.getCoverage(curCoverageName);
 
             if (meta == null) {
                 throw new WcsException(new StringBuffer("Coverage ").append(curCoverageName)
                                                                     .append(" does ")
                                                                     .append("not exist on this server")
                                                                     .toString());
             }
 
             try {
             	tempResponse = tempResponse + printElement(meta);
             } catch(Exception e) {
             	throw new WcsException(e);
             }
         }
 
         tempResponse = tempResponse + "\n\n";
 
         return tempResponse;
     }
 
     private static String printElement(CoverageInfo cv) throws Exception {
         StringBuffer tempResponse = new StringBuffer();
 
         tempResponse.append("\n <CoverageOffering>");
 
         if(cv.getMetadataLinks() != null) {
         	for (MetadataLinkInfo link : cv.getMetadataLinks()) {
         		tempResponse.append("\n  <metadataLink about=\"").append(link.getAbout())
                 .append("\" metadataType=\"").append(link.getMetadataType())
                 .append("\"/>");
 			}
         }
 
         String tmp = cv.getDescription();
 
         if ((tmp != null) && (tmp != "")) {
             tempResponse.append("\n  <description>").append(tmp).append("</description>");
         }
 
         tmp = cv.getName();
 
         if ((tmp != null) && (tmp != "")) {
             tempResponse.append("\n  <name>").append(tmp).append("</name>");
         }
 
         tmp = cv.getTitle();
 
         if ((tmp != null) && (tmp != "")) {
             tempResponse.append("\n  <label>").append(tmp).append("</label>");
         }
 
         final ReferencedEnvelope envelope = cv.getLatLonBoundingBox();
 
         tempResponse.append("\n  <lonLatEnvelope" + " srsName=\"WGS84(DD)\"") /*urn:ogc:def:crs:OGC:1.3:CRS84*/
                     .append(">");
         tempResponse.append("\n   <gml:pos>").append(envelope.getLowerCorner().getOrdinate(0))
                     .append(" ").append(envelope.getLowerCorner().getOrdinate(1))
                     .append("</gml:pos>");
         tempResponse.append("\n   <gml:pos>").append(envelope.getUpperCorner().getOrdinate(0))
                     .append(" ").append(envelope.getUpperCorner().getOrdinate(1))
                     .append("</gml:pos>");
         /*tempResponse.append("\n   <gml:timePosition></gml:timePosition>");
         tempResponse.append("\n   <gml:timePosition></gml:timePosition>");*/
         tempResponse.append("\n  </lonLatEnvelope>");
 
         if ((cv.getKeywords() != null) && (cv.getKeywords().size() > 0)) {
             tempResponse.append("\n  <keywords>");
 
             for (int i = 0; i < cv.getKeywords().size(); i++)
                 tempResponse.append("\n   <keyword>" + cv.getKeywords().get(i) + "</keyword>");
 
             tempResponse.append("\n  </keywords>");
         }
 
         // TODO we need to signal somehow that something went wrong
         ReferencedEnvelope cvEnvelope = cv.getBoundingBox();
         tempResponse.append("\n  <domainSet>");
         tempResponse.append("\n   <spatialDomain>");
         // Envelope
         String userDefinedCrsIdentifier = cv.getSRS();
         tempResponse.append("\n    <gml:Envelope")
                     .append((((userDefinedCrsIdentifier != null) && (userDefinedCrsIdentifier != ""))
             ? new StringBuffer(" srsName=\"").append(userDefinedCrsIdentifier).append("\"").toString() : ""))
                     .append(">");
         tempResponse.append("\n       <gml:pos>")
                     .append((cvEnvelope != null)
             ? new StringBuffer(Double.toString(cvEnvelope.getLowerCorner().getOrdinate(0))).append(
                 " ").append(cvEnvelope.getLowerCorner().getOrdinate(1)).toString() : "")
                     .append("</gml:pos>");
         tempResponse.append("\n       <gml:pos>")
                     .append((cvEnvelope != null)
             ? new StringBuffer(Double.toString(cvEnvelope.getUpperCorner().getOrdinate(0))).append(
                 " ").append(cvEnvelope.getUpperCorner().getOrdinate(1)).toString() : "")
                     .append("</gml:pos>");
         tempResponse.append("\n    </gml:Envelope>");
 
         // Grid
 		GridGeometry  grid      = cv.getGrid();
 		MathTransform gridToCRS = grid.getGridToCRS();
 		final int gridDimension = gridToCRS != null ? gridToCRS.getSourceDimensions() : 0;
 
 		// RectifiedGrid
 		tempResponse.append("\n    <gml:RectifiedGrid").append(
 				(gridToCRS != null) ? new StringBuffer(" dimension=\"").append(
 						gridDimension).append("\"").toString() : "")
 				.append(">");
 
 		String lowers = "";
 		String upers = "";
 
 		for (int r = 0; r < gridDimension; r++) {
 			lowers += (grid.getGridRange().getLower(r) + " ");
 			upers += (grid.getGridRange().getUpper(r) + " ");
 		}
 
 		tempResponse.append("\n       <gml:limits>");
 		tempResponse.append("\n         <gml:GridEnvelope>");
 		tempResponse.append("\n         <gml:low>"
 				+ ((cvEnvelope != null) ? lowers : "") + "</gml:low>");
 		tempResponse.append("\n         <gml:high>"
 				+ ((cvEnvelope != null) ? upers : "") + "</gml:high>");
 		tempResponse.append("\n         </gml:GridEnvelope>");
 		tempResponse.append("\n       </gml:limits>");
 
 		for (CoverageDimensionInfo dimension : cv.getDimensions()) {
 				tempResponse.append("\n       <gml:axisName>" + dimension.getName()
 						+ "</gml:axisName>");
 		}
 
 		tempResponse.append("\n       <gml:origin>");
 		tempResponse.append("\n       <gml:pos>"
 				+ (gridToCRS != null ? ((AffineTransform) gridToCRS).getTranslateX() + " " + ((AffineTransform) gridToCRS).getTranslateY() 
 						: ((cvEnvelope != null) ? (cvEnvelope.getLowerCorner() .getOrdinate(0) + " " + cvEnvelope.getUpperCorner().getOrdinate(1)) : "")) 
 				+ "</gml:pos>");
 		tempResponse.append("\n       </gml:origin>");
 		tempResponse.append("\n       <gml:offsetVector>"
 				+ (gridToCRS != null ? ((AffineTransform) gridToCRS).getScaleX() + " " + ((AffineTransform) gridToCRS).getShearX()
 						: ((cvEnvelope != null) ? ((cvEnvelope.getUpperCorner().getOrdinate(0) - cvEnvelope.getLowerCorner().getOrdinate(0)) / (grid.getGridRange().getUpper(0) - grid.getGridRange().getLower(0))) : 0.0) + " 0.0")
 				+ "</gml:offsetVector>");
 		tempResponse.append("\n       <gml:offsetVector>"
 				+ (gridToCRS != null ? ((AffineTransform) gridToCRS).getShearY() + " " + ((AffineTransform) gridToCRS).getScaleY()
 						: "0.0 " + ((cvEnvelope != null) ? ((cvEnvelope.getLowerCorner().getOrdinate(1) - cvEnvelope.getUpperCorner().getOrdinate(1)) / (grid.getGridRange().getUpper(1) - grid.getGridRange().getLower(1))) : -0.0))
 				+ "</gml:offsetVector>");
 		tempResponse.append("\n    </gml:RectifiedGrid>");
 		tempResponse.append("\n   </spatialDomain>");
 		
 		
         tempResponse.append("\n  </domainSet>");
 
         // rangeSet
         TreeSet nodataValues = new TreeSet();
 
         try {
             if (cv.getDimensions().size() > 0) {
                 int numSampleDimensions = cv.getDimensions().size();
                 tempResponse.append("\n  <rangeSet>");
                 tempResponse.append("\n   <RangeSet>");
                 //tempResponse.append("\n    <!--  WARNING: Mandatory metadata '..._rangeset_name' was missing in this context.  --> ");
                 tempResponse.append("\n    <name>" + cv.getName() + "</name>");
                 tempResponse.append("\n    <label>" + cv.getTitle() + "</label>");
                 tempResponse.append("\n      <axisDescription>");
                 tempResponse.append("\n        <AxisDescription>");
                 tempResponse.append("\n          <name>Band</name>");
                 tempResponse.append("\n          <label>Band</label>");
                 tempResponse.append("\n          <values>");
 
                 if (numSampleDimensions == 1) {
                     tempResponse.append("\n            <singleValue>").append("1")
                                 .append("</singleValue>");
                 } else {
                     tempResponse.append("\n            <interval>");
                     tempResponse.append("\n              <min>1</min>");
                     tempResponse.append("\n              <max>" + numSampleDimensions + "</max>");
                     tempResponse.append("\n            </interval>");
                 }
 
                 tempResponse.append("\n          </values>");
                 tempResponse.append("\n        </AxisDescription>");
                 tempResponse.append("\n      </axisDescription>");
 
                 for(CoverageDimensionInfo dim : cv.getDimensions())
                 	for(Double nullValue : dim.getNullValues())
                         if (!nodataValues.contains(nullValue)) {
                             nodataValues.add(nullValue);
                         }
 
                 tempResponse.append("\n      <nullValues>");
 
                 if (nodataValues.size() > 0) {
                     if (nodataValues.size() == 1) {
                         tempResponse.append("\n        <singleValue>"
                             + (Double) nodataValues.first() + "</singleValue>");
                     } else {
                         tempResponse.append("\n        <interval>");
                         tempResponse.append("\n          <min>" + (Double) nodataValues.first()
                             + "</min>");
                         tempResponse.append("\n          <max>" + (Double) nodataValues.last()
                             + "</max>");
                         tempResponse.append("\n        <interval>");
                     }
                 } else {
                     tempResponse.append("\n        <singleValue>0</singleValue>");
                 }
 
                 tempResponse.append("\n      </nullValues>");
 
                 tempResponse.append("\n   </RangeSet>");
                 tempResponse.append("\n  </rangeSet>");
             }
         } catch (Exception e) {
             // TODO Handle this exceptions ...
             e.printStackTrace();
         }
 
         if (((cv.getRequestSRS() != null) && (cv.getRequestSRS().size() > 0))
                 || ((cv.getResponseSRS() != null) && (cv.getResponseSRS().size() > 0))) {
             tempResponse.append("\n  <supportedCRSs>");
 
             if ((cv.getResponseSRS() != null) && (cv.getResponseSRS().size() > 0)
                     && (cv.getRequestSRS() != null) && (cv.getRequestSRS().size() > 0)) {
                 tempResponse.append("\n    <requestResponseCRSs>");
 
                 ArrayList CRSs = new ArrayList();
 
                 for (int i = 0; i < cv.getRequestSRS().size(); i++)
                     if (!CRSs.contains(cv.getRequestSRS().get(i))) {
                         CRSs.add(cv.getRequestSRS().get(i));
                     }
 
                 for (int i = 0; i < cv.getResponseSRS().size(); i++)
                     if (!CRSs.contains(cv.getResponseSRS().get(i))) {
                         CRSs.add(cv.getResponseSRS().get(i));
                     }
 
                 for (int i = 0; i < CRSs.size(); i++)
                     tempResponse.append(CRSs.get(i) + " ");
 
                 tempResponse.append("\n    </requestResponseCRSs>");
             } else {
                 if ((cv.getRequestSRS() != null) && (cv.getRequestSRS().size() > 0)) {
                     for (int i = 0; i < cv.getRequestSRS().size(); i++)
                         tempResponse.append("\n    <requestCRSs>" + cv.getRequestSRS().get(i)
                             + "</requestCRSs>");
                 }
 
                 if ((cv.getResponseSRS() != null) && (cv.getResponseSRS().size() > 0)) {
                     for (int i = 0; i < cv.getResponseSRS().size(); i++)
                         tempResponse.append("\n    <responseCRSs>" + cv.getResponseSRS().get(i)
                             + "</responseCRSs>");
                 }
             }
 
             tempResponse.append("\n  </supportedCRSs>");
         }
 
         final String nativeFormat = (((cv.getNativeFormat() != null)
             && cv.getNativeFormat().equalsIgnoreCase("GEOTIFF")) ? "GeoTIFF" : cv.getNativeFormat());
         String supportedFormat = "";
 
         if (((cv.getSupportedFormats() != null) && (cv.getSupportedFormats().size() > 0))) {
             tempResponse.append("\n  <supportedFormats"
                 + (((nativeFormat != null) && (nativeFormat != ""))
                 ? (" nativeFormat=\"" + nativeFormat + "\"") : "") + ">");
 
             for (int i = 0; i < cv.getSupportedFormats().size(); i++) {
                 supportedFormat = (String) cv.getSupportedFormats().get(i);
                 supportedFormat = (supportedFormat.equalsIgnoreCase("GEOTIFF") ? "GeoTIFF"
                                                                                : supportedFormat);
                 tempResponse.append("\n    <formats>" + supportedFormat + "</formats>");
             }
 
             tempResponse.append("\n  </supportedFormats>");
         }
 
         if (((cv.getInterpolationMethods() != null) && (cv.getInterpolationMethods().size() > 0))) {
             tempResponse.append("\n  <supportedInterpolations"
                 + (((cv.getDefaultInterpolationMethod() != null)
                 && (cv.getDefaultInterpolationMethod() != ""))
                 ? (" default=\"" + cv.getDefaultInterpolationMethod() + "\"") : "") + ">");
 
             for (int i = 0; i < cv.getInterpolationMethods().size(); i++)
                 tempResponse.append("\n    <interpolationMethod>"
                     + cv.getInterpolationMethods().get(i) + "</interpolationMethod>");
 
             tempResponse.append("\n  </supportedInterpolations>");
         }
 
         tempResponse.append("\n </CoverageOffering>");
 
         return tempResponse.toString();
     } 
     
     /*
     * (non-Javadoc)
     *
     * @see org.vfny.geoserver.responses.Response#abort()
     */
     public void abort(ServiceInfo gs) {
         // nothing to undo
     }
 }
