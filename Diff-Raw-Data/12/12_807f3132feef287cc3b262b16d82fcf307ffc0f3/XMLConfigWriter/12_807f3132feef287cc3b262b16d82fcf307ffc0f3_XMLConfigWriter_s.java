 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.global.xml;
 
 import java.awt.geom.AffineTransform;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.transform.TransformerException;
 
 import org.apache.commons.io.FileUtils;
 import org.geotools.filter.FilterTransformer;
 import org.geotools.geometry.GeneralEnvelope;
 import org.geotools.util.logging.Logging;
 import org.opengis.coverage.grid.GridGeometry;
 import org.opengis.referencing.operation.MathTransform;
 import org.opengis.util.InternationalString;
 import org.vfny.geoserver.global.ConfigurationException;
 import org.vfny.geoserver.global.CoverageDimension;
 import org.vfny.geoserver.global.MetaDataLink;
 import org.vfny.geoserver.global.dto.AttributeTypeInfoDTO;
 import org.vfny.geoserver.global.dto.ContactDTO;
 import org.vfny.geoserver.global.dto.CoverageInfoDTO;
 import org.vfny.geoserver.global.dto.CoverageStoreInfoDTO;
 import org.vfny.geoserver.global.dto.DataDTO;
 import org.vfny.geoserver.global.dto.DataStoreInfoDTO;
 import org.vfny.geoserver.global.dto.FeatureTypeInfoDTO;
 import org.vfny.geoserver.global.dto.GeoServerDTO;
 import org.vfny.geoserver.global.dto.NameSpaceInfoDTO;
 import org.vfny.geoserver.global.dto.ServiceDTO;
 import org.vfny.geoserver.global.dto.StyleDTO;
 import org.vfny.geoserver.global.dto.WCSDTO;
 import org.vfny.geoserver.global.dto.WFSDTO;
 import org.vfny.geoserver.global.dto.WMSDTO;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 /**
  * XMLConfigWriter purpose.
  * 
  * <p>
  * This class is intended to store a configuration to be written and complete
  * the output to XML.
  * </p>
  * 
  * <p>
  * </p>
  * 
  * @author dzwiers, Refractions Research, Inc.
  * @version $Id$
  */
 public class XMLConfigWriter {
     /** Used internally to create log information to detect errors. */
     private static final Logger LOGGER = org.geotools.util.logging.Logging
             .getLogger("org.vfny.geoserver.global");
 
     /**
      * XMLConfigWriter constructor.
      * 
      * <p>
      * Should never be called.
      * </p>
      */
     private XMLConfigWriter() {
     }
 
     public static void store(DataDTO data, File root) throws ConfigurationException {
         if (LOGGER.isLoggable(Level.FINE)) {
             LOGGER.fine("In method store DataDTO");
         }
 
         if (data == null) {
             throw new ConfigurationException("DataDTO is null: cannot write.");
         }
 
         WriterUtils.initFile(root, true);
 
         // boolean inDataDir = GeoserverDataDirectory.isTrueDataDir();
 
         // We're just checking if it's actually a data_dir, not trying to
         // to do backwards compatibility. So if an old data_dir is made in
         // the old way, on save it'll come to the new way.
         File fileDir = root; // ? root : new File(root, "WEB-INF/");
         File configDir = WriterUtils.initFile(fileDir, true);
 
         File catalogFile = WriterUtils.initWriteFile(new File(configDir, "catalog.xml"), false);
 
         try {
             Writer fw = new OutputStreamWriter(new FileOutputStream(catalogFile),
                     getDefaultEncoding());
             storeCatalog(new WriterHelper(fw), data);
             fw.close();
         } catch (IOException e) {
             throw new ConfigurationException("Store" + root, e);
         }
 
         File dataDir;
 
         // if (!inDataDir) {
         // dataDir = WriterUtils.initFile(new File(root, "data/"), true);
         // } else {
         dataDir = root;
 
         // }
         File featureTypeDir = WriterUtils.initFile(new File(dataDir, "featureTypes/"), true);
         storeFeatures(featureTypeDir, data);
 
         File coverageDir = WriterUtils.initFile(new File(dataDir, "coverages/"), true);
         storeCoverages(coverageDir, data);
     }
 
     /**
      * Returns the default encoding for configuration files. For the moment we
      * default to UTF8, but we may want to make this user configurable (UTF-16
      * may be needed?)
      * 
      * @return
      */
     private static String getDefaultEncoding() {
         return "UTF-8";
     }
 
     public static void store(WCSDTO wcs, WMSDTO wms, WFSDTO wfs, GeoServerDTO geoServer, File root)
             throws ConfigurationException {
         if (LOGGER.isLoggable(Level.FINEST)) {
             LOGGER.finest("In method store WCSDTO,WMSDTO,WFSDTO, GeoServerDTO");
         }
 
         if (geoServer == null) {
             throw new ConfigurationException(
                     "null parameter in store(WCSDTO,WMSDTO,WFSDTO, GeoServerDTO): cannot write.");
         }
 
         WriterUtils.initFile(root, true);
 
         // boolean inDataDir = GeoserverDataDirectory.isTrueDataDir();
 
         // We're just checking if it's actually a data_dir, not trying to
         // to do backwards compatibility. So if an old data_dir is made in
         // the old way, on save it'll come to the new way.
         File fileDir = root; // inDataDir ? root : new File(root,
         // "WEB-INF/");
         File configDir = WriterUtils.initFile(fileDir, true);
         File configFile = WriterUtils.initWriteFile(new File(configDir, "services.xml"), false);
 
         try {
             Writer fw = new OutputStreamWriter(new FileOutputStream(configFile),
                     getDefaultEncoding());
             storeServices(new WriterHelper(fw), wcs, wms, wfs, geoServer);
             fw.close();
         } catch (IOException e) {
             throw new ConfigurationException("Store" + root, e);
         }
     }
 
     public synchronized static void store(WCSDTO wcs, WMSDTO wms, WFSDTO wfs, GeoServerDTO geoServer,
             DataDTO data, File root) throws ConfigurationException {
         store(wcs, wms, wfs, geoServer, root);
         store(data, root);
     }
 
     /**
      * storeServices purpose.
      * 
      * <p>
      * Writes the services.xml file from the model in memory.
      * </p>
      * 
      * @param cw
      *            The Configuration Writer
      * @param wms
      *            DOCUMENT ME!
      * @param wfs
      *            DOCUMENT ME!
      * @param geoServer
      *            DOCUMENT ME!
      * 
      * @throws ConfigurationException
      *             When an IO exception occurs.
      */
     protected static void storeServices(WriterHelper cw, WCSDTO wcs, WMSDTO wms, WFSDTO wfs,
             GeoServerDTO geoServer) throws ConfigurationException {
         if (LOGGER.isLoggable(Level.FINER)) {
             LOGGER.finer("In method storeServices");
         }
 
         cw.writeln("<?config.xml version=\"1.0\" encoding=\"UTF-8\"?>");
         cw.comment("Service level configuration");
         cw.openTag("serverConfiguration");
 
         GeoServerDTO g = geoServer;
 
         if (g != null) {
             cw.openTag("global");
 
             if (g.getLog4jConfigFile() != null) {
                 cw.textTag("log4jConfigFile", g.getLog4jConfigFile());
             }
 
             cw.valueTag("suppressStdOutLogging", g.getSuppressStdOutLogging() + "");
 
             if (g.getLogLocation() != null) {
                 cw.textTag("logLocation", g.getLogLocation());
             }
 
             cw.valueTag("JaiMemoryCapacity", "" + g.getJaiMemoryCapacity());
             cw.valueTag("JaiMemoryThreshold", "" + g.getJaiMemoryThreshold());
             cw.valueTag("JaiTileThreads", "" + g.getJaiTileThreads());
             cw.valueTag("JaiTilePriority", "" + g.getJaiTilePriority());
             cw.valueTag("JaiRecycling", "" + g.getJaiRecycling());
             cw.valueTag("ImageIOCache", "" + g.getImageIOCache());
             cw.valueTag("JaiJPEGNative", "" + g.getJaiJPEGNative());
             cw.valueTag("JaiPNGNative", "" + g.getJaiPNGNative());
             cw.valueTag("JaiMosaicNative", "" + g.getJaiMosaicNative());
 
             /*
              * if(g.getBaseUrl()!=null && g.getBaseUrl()!=""){ cw.comment("The
              * base URL where this servlet will run. If running locally\n"+
              * "then http://localhost:8080 (or whatever port you're running
              * on)\n"+ "should work. If you are serving to the world then this
              * must be\n"+ "the location where the geoserver servlets appear");
              * cw.textTag("URL",g.getBaseUrl()); }
              */
             cw.comment("Sets the max number of Features returned by GetFeature");
             cw.valueTag("maxFeatures", "" + g.getMaxFeatures());
             cw.comment("Whether newlines and indents should be returned in \n"
                     + "XML responses.  Default is false");
             cw.valueTag("verbose", "" + g.isVerbose());
             cw.comment("Whether the Service Exceptions returned to clients should contain\n"
                     + "full java stack traces (useful for debugging). ");
             cw.valueTag("verboseExceptions", "" + g.isVerboseExceptions());
             cw.comment("Sets the max number of decimal places past the zero returned in\n"
                     + "a GetFeature response.  Default is 4");
             cw.valueTag("numDecimals", "" + g.getNumDecimals());
 
             if (g.getCharSet() != null) {
                 cw
                         .comment("Sets the global character set.  This could use some more testing\n"
                                 + "from international users, but what it does is sets the encoding\n"
                                 + "globally for all postgis database connections (the charset tag\n"
                                 + "in FeatureTypeConfig), as well as specifying the encoding in the return\n"
                                 + "config.xml header and mime type.  The default is UTF-8.  Also be warned\n"
                                 + "that GeoServer does not check if the CharSet is valid before\n"
                                 + "attempting to use it, so it will fail miserably if a bad charset\n"
                                 + "is used.");
                 cw.valueTag("charSet", g.getCharSet().toString());
             }
 
             if ((g.getSchemaBaseUrl() != null) && (g.getSchemaBaseUrl() != "")) {
                 cw.comment("Define a base url for the location of the wfs schemas.\n"
                         + "By default GeoServer loads and references its own at\n"
                         + "<URL>/data/capabilities. Uncomment to enable.  The\n"
                         + "standalone Tomcat server needs SchemaBaseUrl defined\n"
                         + "for validation.");
                 cw.textTag("SchemaBaseUrl", g.getSchemaBaseUrl());
             }
 
             if ((g.getProxyBaseUrl() != null) && (g.getSchemaBaseUrl() != "")) {
                 cw.comment("Define a base url for the geoserver application.\n"
                         + "By default GeoServer uses the local one, but it may "
                         + "be wrong if you're using a reverse proxy in front of Geoserver");
                 cw.textTag("ProxyBaseUrl", g.getProxyBaseUrl());
             }
 
             // removed, the user is now stored in the users.properties file
             // if ((g.getAdminUserName() != null) && (g.getAdminUserName() !=
             // "")) {
             // cw.comment("Defines the user name of the administrator for log
             // in\n"
             // + "to the web based administration tool.");
             // cw.textTag("adminUserName", g.getAdminUserName());
             // }
             //
             // if ((g.getAdminPassword() != null) && (g.getAdminPassword() !=
             // "")) {
             // cw.comment("Defines the password of the administrator for log
             // in\n"
             // + "to the web based administration tool.");
             // cw.textTag("adminPassword", g.getAdminPassword());
             // }
 
             if (g.getContact() != null) {
                 storeContact(g.getContact(), cw);
             }
 
             //if ((g.getTileCache() != null) && !"".equals(g.getTileCache().trim())) {
             //    cw.comment("Defines hte location of a tile cache (full url or relative path)");
             //    cw.textTag("tileCache", g.getTileCache());
             //}
 
             cw.comment("Stores the current updateSequence");
             cw.textTag("updateSequence", g.getUpdateSequence() + "");
 
             cw.closeTag("global");
         }
 
         if (!((wcs == null) && (wfs == null) && (wms == null))) {
             cw.openTag("services");
 
             if (wcs != null) {
                 storeService(wcs, cw);
             }
 
             if (wfs != null) {
                 storeService(wfs, cw);
             }
 
             if (wms != null) {
                 storeService(wms, cw);
             }
 
             // Z39.50 is not used in the current system.
             cw.closeTag("services");
         }
 
         cw.closeTag("serverConfiguration");
     }
 
     /**
      * storeContact purpose.
      * 
      * <p>
      * Writes a contact into the WriterUtils provided from the ContactConfig
      * provided.
      * </p>
      * 
      * @param c
      *            The ContactConfig to write.
      * @param cw
      *            The Configuration Writer
      * 
      * @throws ConfigurationException
      *             When an IO exception occurs.
      */
     protected static void storeContact(ContactDTO c, WriterHelper cw) throws ConfigurationException {
         if (LOGGER.isLoggable(Level.FINER)) {
             LOGGER.finer("In method storeContact");
         }
 
         if ((c != null) && !c.equals(new ContactDTO())) {
             cw.openTag("ContactInformation");
             cw.openTag("ContactPersonPrimary");
             cw.textTag("ContactPerson", c.getContactPerson());
             cw.textTag("ContactOrganization", c.getContactOrganization());
             cw.closeTag("ContactPersonPrimary");
             cw.textTag("ContactPosition", c.getContactPosition());
             cw.openTag("ContactAddress");
             cw.textTag("AddressType", c.getAddressType());
             cw.textTag("Address", c.getAddress());
             cw.textTag("City", c.getAddressCity());
             cw.textTag("StateOrProvince", c.getAddressState());
             cw.textTag("PostCode", c.getAddressPostalCode());
             cw.textTag("Country", c.getAddressCountry());
             cw.closeTag("ContactAddress");
             cw.textTag("ContactVoiceTelephone", c.getContactVoice());
             cw.textTag("ContactFacsimileTelephone", c.getContactFacsimile());
             cw.textTag("ContactElectronicMailAddress", c.getContactEmail());
             cw.textTag("ContactOnlineResource", c.getOnlineResource());
             cw.closeTag("ContactInformation");
         }
     }
 
     /**
      * storeService purpose.
      * 
      * <p>
      * Writes a service into the WriterUtils provided from the WFS or WMS object
      * provided.
      * </p>
      * 
      * @param obj
      *            either a WFS or WMS object.
      * @param cw
      *            The Configuration Writer
      * 
      * @throws ConfigurationException
      *             When an IO exception occurs or the object provided is not of
      *             the correct type.
      */
     protected static void storeService(Object obj, WriterHelper cw) throws ConfigurationException {
         if (LOGGER.isLoggable(Level.FINER)) {
             LOGGER.finer("In method storeService");
         }
 
         ServiceDTO s = null;
         String u = null;
         String t = "";
 
         boolean fBounds = false;
         boolean srsXmlStyle = false;
         int serviceLevel = 0;
         String svgRenderer = null;
         Map baseMapLayers = null;
         Map baseMapStyles = null;
         Map baseMapEnvelopes = null;
         boolean svgAntiAlias = false;
         boolean globalWatermarking = false;
         String globalWatermarkingURL = null;
         int watermarkTransparency = 0;
         int watermarkPosition = 8;
         String allowInterpolation = null;
         boolean citeConformanceHacks = false;
 
         if (obj instanceof WCSDTO) {
             WCSDTO w = (WCSDTO) obj;
             s = w.getService();
             t = "WCS";
 
             // citeConformanceHacks = w.getCiteConformanceHacks();
         } else if (obj instanceof WFSDTO) {
             WFSDTO w = (WFSDTO) obj;
             s = w.getService();
             t = "WFS";
 
             fBounds = w.isFeatureBounding();
             srsXmlStyle = w.isSrsXmlStyle();
             serviceLevel = w.getServiceLevel();
             citeConformanceHacks = w.getCiteConformanceHacks();
         } else if (obj instanceof WMSDTO) {
             WMSDTO w = (WMSDTO) obj;
             s = w.getService();
             t = "WMS";
             svgRenderer = w.getSvgRenderer();
             svgAntiAlias = w.getSvgAntiAlias();
             globalWatermarking = w.getGlobalWatermarking();
             globalWatermarkingURL = w.getGlobalWatermarkingURL();
             watermarkTransparency = w.getWatermarkTransparency();
             watermarkPosition = w.getWatermarkPosition();
             allowInterpolation = w.getAllowInterpolation();
             baseMapLayers = w.getBaseMapLayers();
             baseMapStyles = w.getBaseMapStyles();
             baseMapEnvelopes = w.getBaseMapEnvelopes();
         } else {
             throw new ConfigurationException("Invalid object: not WMS or WFS or WCS");
         }
 
         Map atrs = new HashMap();
         atrs.put("type", t);
         atrs.put("enabled", s.isEnabled() + "");
         cw.openTag("service", atrs);
         cw.comment("ServiceDTO elements, needed for the capabilities document\n"
                 + "Title and OnlineResource are the two required");
 
         if ((s.getName() != null) && (s.getName() != "")) {
             cw.textTag("name", s.getName());
         }
 
         if ((s.getTitle() != null) && (s.getTitle() != "")) {
             cw.textTag("title", s.getTitle());
         }
 
         if ((s.getAbstract() != null) && (s.getAbstract() != "")) {
             cw.textTag("abstract", s.getAbstract());
         }
 
         if (s.getMetadataLink() != null) {
             MetaDataLink ml = s.getMetadataLink();
             Map mlAttr = new HashMap();
             mlAttr.put("about", ml.getAbout());
             mlAttr.put("type", ml.getType());
             mlAttr.put("metadataType", ml.getMetadataType());
             cw.textTag("metadataLink", mlAttr, ml.getContent());
         }
 
         if (!s.getKeywords().isEmpty()) {
             cw.openTag("keywords");
 
             for (int i = 0; i < s.getKeywords().size(); i++) {
                 String str = s.getKeywords().get(i).toString();
                 cw.textTag("keyword", str.replaceAll("\\r", ""));
             }
 
             cw.closeTag("keywords");
         }
 
         if (s.getOnlineResource() != null) {
             cw.textTag("onlineResource", s.getOnlineResource().toString());
         }
 
         if ((s.getFees() != null) && (s.getFees() != "")) {
             cw.textTag("fees", s.getFees());
         }
 
         if ((s.getAccessConstraints() != null) && (s.getAccessConstraints() != "")) {
             cw.textTag("accessConstraints", s.getAccessConstraints());
         }
 
         if (fBounds) {
             cw.valueTag("featureBounding", fBounds + "");
         }
 
         // if (srsXmlStyle) {
         cw.valueTag("srsXmlStyle", srsXmlStyle + "");
 
         // }
         if (serviceLevel != 0) {
             cw.valueTag("serviceLevel", serviceLevel + "");
         }
 
         if (obj instanceof WFSDTO) // DJB: this method (storeService) doesnt
         // separate WFS and WMS very well!
         {
             cw.textTag("citeConformanceHacks", citeConformanceHacks + "");
         }
 
         if ((s.getMaintainer() != null) && (s.getMaintainer() != "")) {
             cw.textTag("maintainer", s.getMaintainer());
         }
 
         if (svgRenderer != null) {
             cw.textTag("svgRenderer", svgRenderer);
         }
 
         if ((baseMapLayers != null) && (baseMapStyles != null) && (baseMapEnvelopes != null)) {
             cw.openTag("BaseMapGroups");
 
             // for each title/layer combo, write it out
             String[] titles = (String[]) baseMapLayers.keySet().toArray(new String[0]);
 
             for (int i = 0; i < titles.length; i++) {
                 HashMap titleMap = new HashMap();
                 titleMap.put("baseMapTitle", titles[i]);
                 cw.openTag("BaseMapGroup", titleMap);
                 cw.textTag("baseMapLayers", (String) baseMapLayers.get(titles[i]));
                 cw.textTag("baseMapStyles", (String) baseMapStyles.get(titles[i]));
 
                 GeneralEnvelope e = (GeneralEnvelope) baseMapEnvelopes.get(titles[i]);
                 Map m = new HashMap();
 
                 m.put("srsName", e.getCoordinateReferenceSystem().getIdentifiers().toArray()[0]
                         .toString());
 
                 if (!e.isNull()) {
                     cw.openTag("baseMapEnvelope", m);
                     cw.textTag("pos", e.getLowerCorner().getOrdinate(0) + " "
                             + e.getLowerCorner().getOrdinate(1));
                     cw.textTag("pos", e.getUpperCorner().getOrdinate(0) + " "
                             + e.getUpperCorner().getOrdinate(1));
                     cw.closeTag("baseMapEnvelope");
                 }
 
                 cw.closeTag("BaseMapGroup");
             }
 
             cw.closeTag("BaseMapGroups");
         }
 
         if (obj instanceof WMSDTO) {
             Set limitedCrsListForCapabilities = ((WMSDTO) obj).getCapabilitiesCrs();
             StringBuffer sb = new StringBuffer();
             for (Iterator it = limitedCrsListForCapabilities.iterator(); it.hasNext();) {
                 sb.append(it.next());
                 if (it.hasNext()) {
                     sb.append(", ");
                 }
             }
             cw.comment("List of EPSG codes used to limit the number of SRS elements\n"
                     + "shown in the WMS GetCapabilities document");
             cw.textTag("capabilitiesCrsList", sb.toString());
 
             cw.textTag("svgAntiAlias", svgAntiAlias + "");
             cw.textTag("globalWatermarking", globalWatermarking + "");
 
             if (globalWatermarkingURL != null) {
                 cw.textTag("globalWatermarkingURL", globalWatermarkingURL);
             }
 
             cw.textTag("globalWatermarkingTransparency", watermarkTransparency + "");
             cw.textTag("globalWatermarkingPosition", watermarkPosition + "");
 
             if (allowInterpolation != null) {
                 cw.textTag("allowInterpolation", allowInterpolation);
             }
         }
 
         if ((s.getStrategy() != null) && !"".equals(s.getStrategy())) {
             cw.textTag("serviceStrategy", s.getStrategy());
         }
 
         if (s.getPartialBufferSize() != 0) {
             cw.textTag("partialBufferSize", s.getPartialBufferSize() + "");
         }
 
         cw.closeTag("service");
     }
 
     /**
      * storeCatalog purpose.
      * 
      * <p>
      * Writes a catalog into the WriterUtils provided from Data provided in
      * memory.
      * </p>
      * 
      * @param cw
      *            The Configuration Writer
      * @param data
      *            DOCUMENT ME!
      * 
      * @throws ConfigurationException
      *             When an IO exception occurs.
      */
     protected static void storeCatalog(WriterHelper cw, DataDTO data) throws ConfigurationException {
         if (LOGGER.isLoggable(Level.FINER)) {
             LOGGER.finer("In method storeCatalog");
         }
 
         cw.writeln("<?config.xml version=\"1.0\" encoding=\"UTF-8\"?>");
         cw.openTag("catalog");
 
         // DJB: this used to not put in a datastores tag if there were none
         // defined.
         // this caused the loader to blow up. I changed it so it puts an empty
         // <datastore> here!
         cw.openTag("datastores");
         cw.comment("a datastore configuration element serves as a common data source connection\n"
                 + "parameters repository for all featuretypes it holds.");
 
         Iterator i = data.getDataStores().keySet().iterator();
 
         while (i.hasNext()) {
             String s = (String) i.next();
             DataStoreInfoDTO ds = (DataStoreInfoDTO) data.getDataStores().get(s);
 
             if (ds != null) {
                 storeDataStore(cw, ds);
             }
         }
 
         cw.closeTag("datastores");
 
         // DJB: since datastore screws up if the tag is missing, I'm fixing it
         // here too
         cw.openTag("formats");
         cw.comment("a format configuration element serves as a common data source\n"
                 + "parameters repository for all coverages it holds.");
 
         i = data.getFormats().keySet().iterator();
 
         while (i.hasNext()) {
             String s = (String) i.next();
             CoverageStoreInfoDTO df = (CoverageStoreInfoDTO) data.getFormats().get(s);
 
             if (df != null) {
                 storeFormat(cw, df);
             }
         }
 
         cw.closeTag("formats");
         cw.comment("Defines namespaces to be used by the datastores.");
         cw.openTag("namespaces");
 
         i = data.getNameSpaces().keySet().iterator();
 
         while (i.hasNext()) {
             String s = (String) i.next();
             NameSpaceInfoDTO ns = (NameSpaceInfoDTO) data.getNameSpaces().get(s);
 
             if (ns != null) {
                 storeNameSpace(cw, ns);
             }
         }
 
         cw.closeTag("namespaces");
 
         // DJB: since datastore screws up if the tag is missing, I'm fixing it
         // here too
         cw.openTag("styles");
         cw.comment("Defines the style ids and file name to be used by the wms.");
 
         i = data.getStyles().keySet().iterator();
 
         while (i.hasNext()) {
             String s = (String) i.next();
             StyleDTO st = (StyleDTO) data.getStyles().get(s);
 
             if (st != null) {
                 storeStyle(cw, st);
             }
         }
 
         cw.closeTag("styles");
 
         cw.closeTag("catalog");
     }
 
     /**
      * storeDataStore purpose.
      * 
      * <p>
      * Writes a DataStoreInfo into the WriterUtils provided.
      * </p>
      * 
      * @param cw
      *            The Configuration Writer
      * @param ds
      *            The Datastore.
      * 
      * @throws ConfigurationException
      *             When an IO exception occurs.
      */
     protected static void storeDataStore(WriterHelper cw, DataStoreInfoDTO ds)
             throws ConfigurationException {
         if (LOGGER.isLoggable(Level.FINER)) {
             LOGGER.finer("In method storeDataStore");
         }
 
         Map temp = new HashMap();
 
         if (ds.getId() != null) {
             temp.put("id", ds.getId());
         }
 
         temp.put("enabled", ds.isEnabled() + "");
 
         if (ds.getNameSpaceId() != null) {
             temp.put("namespace", ds.getNameSpaceId());
         }
 
         cw.openTag("datastore", temp);
 
         if ((ds.getAbstract() != null) && (ds.getAbstract() != "")) {
             cw.textTag("abstract", ds.getAbstract());
         }
 
         if ((ds.getTitle() != null) && (ds.getTitle() != "")) {
             cw.textTag("title", ds.getTitle());
         }
 
         if (ds.getConnectionParams().size() != 0) {
             cw.openTag("connectionParams");
 
             Iterator i = ds.getConnectionParams().keySet().iterator();
             temp = new HashMap();
 
             while (i.hasNext()) {
                 String key = (String) i.next();
                 temp.put("name", key);
                 temp.put("value", ds.getConnectionParams().get(key).toString());
                 cw.attrTag("parameter", temp);
             }
 
             cw.closeTag("connectionParams");
         }
 
         cw.closeTag("datastore");
     }
 
     /**
      * storeFormat purpose.
      * 
      * <p>
      * Writes a CoverageStoreInfo into the WriterUtils provided.
      * </p>
      * 
      * @param cw
      *            The Configuration Writer
      * @param store
      *            The Format.
      * 
      * @throws ConfigurationException
      *             When an IO exception occurs.
      */
     protected static void storeFormat(WriterHelper cw, CoverageStoreInfoDTO df)
             throws ConfigurationException {
         if (LOGGER.isLoggable(Level.FINE)) {
             LOGGER.fine("In method storeFormat");
         }
 
         Map temp = new HashMap();
 
         if (df.getId() != null) {
             temp.put("id", df.getId());
         }
 
         temp.put("enabled", df.isEnabled() + "");
 
         if (df.getNameSpaceId() != null) {
             temp.put("namespace", df.getNameSpaceId());
         }
 
         cw.openTag("format", temp);
 
         if ((df.getAbstract() != null) && (df.getAbstract() != "")) {
             cw.textTag("description", df.getAbstract());
         }
 
         if ((df.getTitle() != null) && (df.getTitle() != "")) {
             cw.textTag("title", df.getTitle());
         }
 
         if ((df.getType() != null) && (df.getType() != "")) {
             cw.textTag("type", df.getType());
         }
 
         if ((df.getUrl() != null) && (df.getUrl() != "")) {
             cw.textTag("url", df.getUrl());
         }
 
         cw.closeTag("format");
     }
 
     /**
      * storeNameSpace purpose.
      * 
      * <p>
      * Writes a NameSpaceInfoDTO into the WriterUtils provided.
      * </p>
      * 
      * @param cw
      *            The Configuration Writer
      * @param ns
      *            The NameSpaceInfo.
      * 
      * @throws ConfigurationException
      *             When an IO exception occurs.
      */
     protected static void storeNameSpace(WriterHelper cw, NameSpaceInfoDTO ns)
             throws ConfigurationException {
         if (LOGGER.isLoggable(Level.FINER)) {
             LOGGER.finer("In method storeNameSpace");
         }
 
         Map attr = new HashMap();
 
         if ((ns.getUri() != null) && (ns.getUri() != "")) {
             attr.put("uri", ns.getUri());
         }
 
         if ((ns.getPrefix() != null) && (ns.getPrefix() != "")) {
             attr.put("prefix", ns.getPrefix());
         }
 
         if (ns.isDefault()) {
             attr.put("default", "true");
         }
 
         if (attr.size() != 0) {
             cw.attrTag("namespace", attr);
         }
     }
 
     /**
      * storeStyle purpose.
      * 
      * <p>
      * Writes a StyleDTO into the WriterUtils provided.
      * </p>
      * 
      * @param cw
      *            The Configuration Writer
      * @param s
      *            The StyleDTO.
      * 
      * @throws ConfigurationException
      *             When an IO exception occurs.
      */
     protected static void storeStyle(WriterHelper cw, StyleDTO s) throws ConfigurationException {
         if (LOGGER.isLoggable(Level.FINER)) {
             LOGGER.finer(new StringBuffer("In method storeStyle: ").append(s).toString());
         }
 
         Map attr = new HashMap();
 
         if ((s.getId() != null) && (s.getId() != "")) {
             attr.put("id", s.getId());
         }
 
         if (s.getFilename() != null) {
             attr.put("filename", s.getFilename().getName());
         }
 
         if (s.isDefault()) {
             attr.put("default", "true");
         }
 
         if (LOGGER.isLoggable(Level.FINER)) {
             LOGGER.finer(new StringBuffer("storing style ").append(attr).toString());
         }
 
         if (attr.size() != 0) {
             cw.attrTag("style", attr);
         }
     }
 
     /**
      * storeStyle purpose.
      * 
      * <p>
      * Sets up writing FeatureTypes into their Directories.
      * </p>
      * 
      * @param dir
      *            The FeatureTypes directory
      * @param data
      *            DOCUMENT ME!
      * 
      * @throws ConfigurationException
      *             When an IO exception occurs.
      * 
      * @see storeFeature(FeatureTypeInfo,File)
      */
     protected static void storeFeatures(File dir, DataDTO data) throws ConfigurationException {
         if (LOGGER.isLoggable(Level.FINER)) {
             LOGGER.finer("In method storeFeatures");
         }
 
         // write them
         Iterator i = data.getFeaturesTypes().keySet().iterator();
 
         while (i.hasNext()) {
             String s = (String) i.next();
             FeatureTypeInfoDTO ft = (FeatureTypeInfoDTO) data.getFeaturesTypes().get(s);
 
             if (ft != null) {
                 String ftDirName = ft.getDirName();
 
                 try { // encode the file name (this is to catch colons in FT
                     // names)
                     ftDirName = URLEncoder.encode(ftDirName, getDefaultEncoding());
 
                     if (LOGGER.isLoggable(Level.FINER)) {
                         LOGGER.finer(new StringBuffer("Writing encoded URL: ").append(ftDirName)
                                 .toString());
                     }
                 } catch (UnsupportedEncodingException e1) {
                     throw new ConfigurationException(e1);
                 }
 
                 File dir2 = WriterUtils.initWriteFile(new File(dir, ftDirName), true);
 
                 storeFeature(ft, dir2);
 
                 if (ft.getSchemaAttributes() != null) {
                     if (LOGGER.isLoggable(Level.FINER)) {
                         LOGGER.finer(new StringBuffer(ft.getKey())
                                 .append(" writing schema.xml w/ ").append(
                                         ft.getSchemaAttributes().size()).toString());
                     }
 
                     storeFeatureSchema(ft, dir2);
                 }
             }
         }
 
         // delete old ones that are not overwritten
         // I'm changing this action, as it is directly leading to users not
         // being able to create their own shapefiles in the web admin tool.
         // since their shit always gets deleted. The behaviour has now changed
         // to just getting rid of the geoserver config files, info.xml and
         // schema.xml and leaving any others. We should revisit this, I
         // do think getting rid of stale featureTypes is a good thing. For 1.3
         // I want to look into directly uploading shapefiles, and perhaps they
         // would then go in a 'shapefile' directory, next to featureTypes or
         // or something, so that the featureTypes directory only contains
         // the info, and schema and those sorts of files. But I do kind of like
         // being able to access the shapefiles directly from the web app, and
         // indeed have had thoughts of expanding that, so that users could
         // always download the full shape for a layer, generated automatically
         // if it's from another datastore. Though I suppose that is not
         // mutually exclusive, just a little wasting of space, for shapefiles
         // would be held twice.
         // JD: changing this to back up rather than delete
         File[] fa = dir.listFiles();
 
         for (int j = 0; j < fa.length; j++) {
             if ( fa[j].getName().startsWith( ".") ) {
                 //ignore it
                 continue;
             }
             
             // find dir name
             i = data.getFeaturesTypes().values().iterator();
 
             FeatureTypeInfoDTO fti = null;
 
             while ((fti == null) && i.hasNext()) {
                 FeatureTypeInfoDTO ft = (FeatureTypeInfoDTO) i.next();
                 String ftDirName = ft.getDirName();
 
                 try { // encode the file name (this is to catch colons in FT
                     // names)
                     ftDirName = URLEncoder.encode(ftDirName, getDefaultEncoding());
 
                     if (LOGGER.isLoggable(Level.FINER)) {
                         LOGGER
                                 .finer(new StringBuffer("Decoded URL: ").append(ftDirName)
                                         .toString());
                     }
                 } catch (UnsupportedEncodingException e1) {
                     throw new ConfigurationException(e1);
                 }
 
                 if (ftDirName.equals(fa[j].getName())) {
                     fti = ft;
                 }
             }
 
             if (fti == null) {
                 // delete it
                 File[] files = fa[j].listFiles();
 
                 if (files != null) {
                     for (int x = 0; x < files.length; x++) {
                         // hold on to the data, but be sure to get rid of the
                         // geoserver config shit, as these were deleted.
                         if (files[x].getName().equals("info.xml")
                                 || files[x].getName().equals("schema.xml")) {
                             // sorry for the hardcodes, I don't remember
                             // if/where
                             // we have these file names.
                             try {
                                 WriterUtils.backupFile( files[x], true );
                             } 
                             catch (IOException e) {
                                 LOGGER.severe( "Unable to backup: " + files[x].getAbsolutePath() );
                             }
                             //files[x].delete();
                         }
                     }
                 }
 
                 // rename it if its not a backup
                 if ( !fa[j].getName().endsWith( ".bak") ) {
                     try {
                         WriterUtils.backupDirectory( fa[j] );
                     }
                     catch (IOException e) {
                         LOGGER.severe( "Unable to backup: " + fa[j].getAbsolutePath() );
                     }
                 } 
             }
         }
     }
 
     /**
      * storeStyle purpose.
      * 
      * <p>
      * Writes a FeatureTypes into it's Directory.
      * </p>
      * 
      * @param ft
      *            DOCUMENT ME!
      * @param dir
      *            The particular FeatureTypeInfo directory
      * 
      * @throws ConfigurationException
      *             When an IO exception occurs.
      * 
      * @see storeFeatures(File)
      */
     protected static void storeFeature(FeatureTypeInfoDTO ft, File dir)
             throws ConfigurationException {
         if (LOGGER.isLoggable(Level.FINER)) {
             LOGGER.finer("In method storeFeature");
         }
 
         File ftInfo = new File(dir, "info.xml");
         //back up file if it already exists
         if ( ftInfo.exists() ) {
             try {
                 WriterUtils.backupFile( ftInfo, true );
             } 
             catch (IOException e) {
                 LOGGER.severe( "Unable to backup: " + ftInfo.getAbsolutePath() );
             }
         }
         
         File f = WriterUtils.initWriteFile(ftInfo, false);
 
         try {
             Writer fw = new OutputStreamWriter(new FileOutputStream(f), getDefaultEncoding());
             WriterHelper cw = new WriterHelper(fw);
             Map m = new HashMap();
 
             // oh the horror, a string comparison using !=... well, this
             // class is going to die soon so I won't touch it...
             if ((ft.getDataStoreId() != null) && (ft.getDataStoreId() != "")) {
                 m.put("datastore", ft.getDataStoreId());
             }
 
             cw.openTag("featureType", m);
 
             if ((ft.getName() != null) && (ft.getName() != "")) {
                 cw.textTag("name", ft.getName());
             }
 
             if ((ft.getAlias() != null) && !ft.getAlias().equals("")) {
                 cw.textTag("alias", ft.getAlias());
             }
 
             cw.comment("native wich EPGS code for the FeatureTypeInfoDTO");
             cw.textTag("SRS", ft.getSRS() + "");
 
             cw.textTag("SRSHandling", String.valueOf(ft.getSRSHandling()));
 
             if ((ft.getTitle() != null) && (ft.getTitle() != "")) {
                 cw.textTag("title", ft.getTitle());
             }
 
             if ((ft.getAbstract() != null) && (ft.getAbstract() != "")) {
                 cw.textTag("abstract", ft.getAbstract());
             }
 
             if ((ft.getWmsPath() != null) && (ft.getWmsPath() != "")) {
                 cw.textTag("wmspath", ft.getWmsPath());
             }
 
             cw.valueTag("numDecimals", ft.getNumDecimals() + "");
 
             if ((ft.getKeywords() != null) && (ft.getKeywords().size() != 0)) {
                 String s = "";
                 Iterator i = ft.getKeywords().iterator();
 
                 if (i.hasNext()) {
                     s = i.next().toString();
 
                     while (i.hasNext()) {
                         s = s + ", " + i.next().toString();
                     }
                 }
 
                 cw.textTag("keywords", s);
             }
 
             if ((ft.getMetadataLinks() != null) && (ft.getMetadataLinks().size() != 0)) {
                 cw.openTag("metadataLinks");
 
                 for (Iterator it = ft.getMetadataLinks().iterator(); it.hasNext();) {
                     MetaDataLink ml = (MetaDataLink) it.next();
                     Map mlAttr = new HashMap();
                     mlAttr.put("about", ml.getAbout());
                     mlAttr.put("type", ml.getType());
                     mlAttr.put("metadataType", ml.getMetadataType());
                     cw.textTag("metadataLink", mlAttr, ml.getContent());
                 }
 
                 cw.closeTag("metadataLinks");
             }
 
             if (ft.getLatLongBBox() != null) {
                 m = new HashMap();
 
                 Envelope e = ft.getLatLongBBox();
 
                 // from creation, isn't stored otherwise
                 if (!e.isNull()) {
                     m.put("dynamic", "false");
                     m.put("minx", e.getMinX() + "");
                     m.put("miny", e.getMinY() + "");
                     m.put("maxx", e.getMaxX() + "");
                     m.put("maxy", e.getMaxY() + "");
                 } else {
                     m.put("dynamic", "true");
                 }
 
                 cw.attrTag("latLonBoundingBox", m);
             }
 
             if (ft.getNativeBBox() != null) {
                 m = new HashMap();
 
                 Envelope e = ft.getNativeBBox();
 
                 // from creation, isn't stored otherwise
                 if (!e.isNull()) {
                     m.put("dynamic", "false");
                     m.put("minx", e.getMinX() + "");
                     m.put("miny", e.getMinY() + "");
                     m.put("maxx", e.getMaxX() + "");
                     m.put("maxy", e.getMaxY() + "");
                 } else {
                     m.put("dynamic", "true");
                 }
 
                 cw.attrTag("nativeBBox", m);
             }
 
             if ((ft.getDefaultStyle() != null) && (ft.getDefaultStyle() != "")) {
                 cw.comment("the default style this FeatureTypeInfoDTO can be represented by.\n"
                         + "at least must contain the \"default\" attribute ");
                 m = new HashMap();
                 m.put("default", ft.getDefaultStyle());
 
                 final ArrayList styles = ft.getStyles();
 
                 if (styles.isEmpty()) {
                     cw.attrTag("styles", m);
                 } else {
                     cw.openTag("styles", m);
 
                     Iterator s_IT = styles.iterator();
 
                     while (s_IT.hasNext())
                         cw.textTag("style", (String) s_IT.next());
 
                     cw.closeTag("styles");
                 }
             }
 
             m = new HashMap();
 
             if (ft.getCacheMaxAge() != null) {
                 m.put("maxage", ft.getCacheMaxAge());
             }
 
             if (ft.isCachingEnabled()) {
                 m.put("enabled", "true");
             } else {
                 m.put("enabled", "false");
             }
 
             cw.attrTag("cacheinfo", m);
             cw.attrTag("searchable", Collections.singletonMap("enabled", Boolean.toString(ft.isIndexingEnabled())));
            cw.attrTag("regionateAttribute", Collections.singletonMap("value", ft.getRegionateAttribute()));
            cw.attrTag("regionateStrategy", Collections.singletonMap("value", ft.getRegionateStrategy()));
             cw.attrTag("regionateFeatureLimit", Collections.singletonMap("value", Integer.toString(ft.getRegionateFeatureLimit())));
 
             if (ft.getDefinitionQuery() != null) {
                 cw.openTag("definitionQuery");
 
                 /*
                  * @REVISIT: strongly test this works.
                  */
 
                 /*
                  * StringWriter sw = new StringWriter();
                  * org.geotools.filter.XMLEncoder xe = new
                  * org.geotools.filter.XMLEncoder(sw);
                  * xe.encode(ft.getDefinitionQuery());
                  * cw.writeln(sw.toString()); cw.closeTag("definitionQuery");
                  */
                 FilterTransformer ftransformer = new FilterTransformer();
                 ftransformer.setOmitXMLDeclaration(true);
                 ftransformer.setNamespaceDeclarationEnabled(false);
 
                 String sfilter = ftransformer.transform(ft.getDefinitionQuery());
                 cw.writeln(sfilter);
             }
 
             cw.textTag("maxFeatures", String.valueOf(ft.getMaxFeatures()));
 
             cw.closeTag("featureType");
             fw.close();
         } catch (IOException e) {
             throw new ConfigurationException(e);
         } catch (TransformerException e) {
             throw new ConfigurationException(e);
         }
     }
 
     protected static void storeFeatureSchema(FeatureTypeInfoDTO fs, File dir)
             throws ConfigurationException {
         if ((fs.getSchemaBase() == null) || (fs.getSchemaBase() == "")) {
             // LOGGER.info( "No schema base" );
             if (LOGGER.isLoggable(Level.FINER)) {
                 LOGGER
                         .finer(new StringBuffer(fs.getKey()).append(" has not schemaBase")
                                 .toString());
             }
 
             return;
         }
 
         if ((fs.getSchemaName() == null) || (fs.getSchemaName() == "")) {
             // Should assume Null?
             // LOGGER.info( "No schema name" ); // Do we even have a field for
             // this?
             if (LOGGER.isLoggable(Level.FINER)) {
                 LOGGER
                         .finer(new StringBuffer(fs.getKey()).append(" has not schemaName")
                                 .toString());
             }
 
             return;
         }
 
         File ftSchema = new File(dir, "schema.xml");
         
         //backup file if it already exists
         if ( ftSchema.exists() ) {
             try {
                 WriterUtils.backupFile( ftSchema, true );
             } 
             catch (IOException e) {
                 LOGGER.severe( "Unable to backup: " + ftSchema.getAbsolutePath() );
             }
         }
         File f = WriterUtils.initWriteFile(ftSchema, false);
 
         try {
             Writer fw = new OutputStreamWriter(new FileOutputStream(f), getDefaultEncoding());
             storeFeatureSchema(fs, fw);
             fw.close();
         } catch (IOException e) {
             throw new ConfigurationException(e);
         }
     }
 
     public static void storeFeatureSchema(FeatureTypeInfoDTO fs, Writer w)
             throws ConfigurationException {
         WriterHelper cw = new WriterHelper(w);
         HashMap m = new HashMap();
         String t = fs.getSchemaName();
 
         if (t != null) {
             if (!"_Type".equals(t.substring(t.length() - 5))) {
                 t = t + "_Type";
             }
 
             m.put("name", t);
         }
 
         cw.openTag("xs:complexType", m);
         cw.openTag("xs:complexContent");
         m = new HashMap();
         t = fs.getSchemaBase();
 
         if (t != null) {
             m.put("base", t);
         }
 
         cw.openTag("xs:extension", m);
         cw.openTag("xs:sequence");
 
         for (int i = 0; i < fs.getSchemaAttributes().size(); i++) {
             AttributeTypeInfoDTO ati = (AttributeTypeInfoDTO) fs.getSchemaAttributes().get(i);
             m = new HashMap();
             m.put("nillable", "" + ati.isNillable());
             m.put("minOccurs", "" + ati.getMinOccurs());
             m.put("maxOccurs", "" + ati.getMaxOccurs());
 
             NameSpaceTranslator nst_xs = NameSpaceTranslatorFactory.getInstance()
                     .getNameSpaceTranslator("xs");
             NameSpaceTranslator nst_gml = NameSpaceTranslatorFactory.getInstance()
                     .getNameSpaceTranslator("gml");
 
             if (!ati.isComplex()) {
                 if (ati.getName() == ati.getType()) {
                     String r = "";
                     NameSpaceElement nse = nst_xs.getElement(ati.getType());
 
                     if (nse == null) {
                         nse = nst_gml.getElement(ati.getType());
                     }
 
                     r = nse.getQualifiedTypeRefName();
                     m.put("ref", r);
                 } else {
                     m.put("name", ati.getName());
 
                     String r = "";
                     NameSpaceElement nse = nst_xs.getElement(ati.getType());
 
                     if (nse == null) {
                         nse = nst_gml.getElement(ati.getType());
                         r = nse.getQualifiedTypeDefName(); // Def
                     } else {
                         r = nse.getQualifiedTypeRefName(); // Ref
                     }
 
                     m.put("type", r);
                 }
 
                 cw.attrTag("xs:element", m);
             } else {
                 m.put("name", ati.getName());
                 cw.openTag("xs:element", m);
                 cw.writeln(ati.getType());
                 cw.closeTag("xs:element");
             }
         }
 
         cw.closeTag("xs:sequence");
         cw.closeTag("xs:extension");
         cw.closeTag("xs:complexContent");
         cw.closeTag("xs:complexType");
     }
 
     protected static void storeCoverages(File dir, DataDTO data) throws ConfigurationException {
         if (LOGGER.isLoggable(Level.FINE)) {
             LOGGER.fine("In method storeCoverages");
         }
 
         // write them
         Iterator i = data.getCoverages().keySet().iterator();
 
         while (i.hasNext()) {
             String s = (String) i.next();
             CoverageInfoDTO cv = (CoverageInfoDTO) data.getCoverages().get(s);
 
             if (cv != null) {
                 File dir2 = WriterUtils.initWriteFile(new File(dir, cv.getDirName()), true);
 
                 storeCoverage(cv, dir2);
             }
         }
 
         File[] fa = dir.listFiles();
 
         for (int j = 0; j < fa.length; j++) {
             if (fa[j].isDirectory()) {
                 // find dir name
                 i = data.getCoverages().values().iterator();
 
                 CoverageInfoDTO cvi = null;
 
                 while ((cvi == null) && i.hasNext()) {
                     CoverageInfoDTO cv = (CoverageInfoDTO) i.next();
 
                     if (cv.getDirName().equals(fa[j].getName())) {
                         cvi = cv;
                     }
                 }
 
                 if (cvi == null) {
                     // delete it
                     File[] t = fa[j].listFiles();
 
                     if (t != null) {
                         for (int x = 0; x < t.length; x++) {
                             // hold on to the data, but be sure to get rid of
                             // the
                             // geoserver config shit, as these were deleted.
                             if (t[x].getName().equals("info.xml")) {
                                 // sorry for the hardcodes, I don't remember
                                 // if/where
                                 // we have these file names.
                                 t[x].delete();
                             }
                         }
                     }
 
                     if (fa[j].listFiles().length == 0) {
                         fa[j].delete();
                     }
                 }
             }
         }
     }
 
     protected static void storeCoverage(CoverageInfoDTO cv, File dir) throws ConfigurationException {
         if (LOGGER.isLoggable(Level.FINE)) {
             LOGGER.fine("In method storeCoverage");
         }
 
         File f = WriterUtils.initWriteFile(new File(dir, "info.xml"), false);
 
         try {
             Writer fw = new OutputStreamWriter(new FileOutputStream(f), getDefaultEncoding());
             WriterHelper cw = new WriterHelper(fw);
             Map m = new HashMap();
 
             if ((cv.getFormatId() != null) && (cv.getFormatId() != "")) {
                 m.put("format", cv.getFormatId());
             }
 
             cw.openTag("coverage", m);
 
             if ((cv.getName() != null) && (cv.getName() != "")) {
                 cw.textTag("name", cv.getName());
             }
 
             if ((cv.getLabel() != null) && (cv.getLabel() != "")) {
                 cw.textTag("label", cv.getLabel());
             }
 
             if ((cv.getDescription() != null) && (cv.getDescription() != "")) {
                 cw.textTag("description", cv.getDescription());
             }
 
             if ((cv.getWmsPath() != null) && (cv.getWmsPath() != "")) {
                 cw.textTag("wmspath", cv.getWmsPath());
             }
 
             m = new HashMap();
 
             if ((cv.getMetadataLink() != null)) {
                 m.put("about", cv.getMetadataLink().getAbout());
                 m.put("type", cv.getMetadataLink().getType());
                 m.put("metadataType", cv.getMetadataLink().getMetadataType());
 
                 cw.openTag("metadataLink", m);
                 cw.writeln(cv.getMetadataLink().getContent());
                 cw.closeTag("metadataLink");
             }
 
             if ((cv.getKeywords() != null) && (cv.getKeywords().size() != 0)) {
                 String s = "";
                 Iterator i = cv.getKeywords().iterator();
 
                 if (i.hasNext()) {
                     s = i.next().toString();
 
                     while (i.hasNext()) {
                         s = s + "," + i.next().toString();
                     }
                 }
 
                 cw.textTag("keywords", s);
             }
 
             if ((cv.getDefaultStyle() != null) && (cv.getDefaultStyle() != "")) {
                 cw.comment("the default style this CoverageInfoDTO can be represented by.\n"
                         + "at least must contain the \"default\" attribute ");
                 m = new HashMap();
                 m.put("default", cv.getDefaultStyle());
 
                 final ArrayList styles = cv.getStyles();
 
                 if (styles.isEmpty()) {
                     cw.attrTag("styles", m);
                 } else {
                     cw.openTag("styles", m);
 
                     Iterator s_IT = styles.iterator();
 
                     while (s_IT.hasNext())
                         cw.textTag("style", (String) s_IT.next());
 
                     cw.closeTag("styles");
                 }
             }
 
             // //
             // storing the envelope.
             // The native crs wkt is stored as the crs attribute. The user defined srs identifier as
             // the srsName atribute
             // //
             if (cv.getEnvelope() != null) {
                 GeneralEnvelope e = cv.getEnvelope();
                 m = new HashMap();
 
                 String userDefinedCrsIdentifier = cv.getUserDefinedCrsIdentifier();
                 if ((userDefinedCrsIdentifier != null) && (userDefinedCrsIdentifier != "")) {
                     m.put("srsName", userDefinedCrsIdentifier);
                 }
 
                 String nativeCrsWkt = cv.getNativeCrsWKT();
                 m.put("crs", nativeCrsWkt.replaceAll("\"", "'").replaceAll("\r\n", "\n"));
 
                 if (!e.isNull()) {
                 	cw.comment("crs: native CRS definition, srsName: user defined CRS identifier");
                     cw.openTag("envelope", m);
                     cw.textTag("pos", e.getLowerCorner().getOrdinate(0) + " "
                             + e.getLowerCorner().getOrdinate(1));
                     cw.textTag("pos", e.getUpperCorner().getOrdinate(0) + " "
                             + e.getUpperCorner().getOrdinate(1));
                     cw.closeTag("envelope");
                 }
             }
 
             // //
             // AlFa: storing the grid-geometry
             // //
             if (cv.getGrid() != null) {
                 GridGeometry g = cv.getGrid();
                 MathTransform tx = g.getGridToCRS();
 
                 InternationalString[] dimNames = cv.getDimensionNames();
                 m = new HashMap();
 
                 m.put("dimension", new Integer(g.getGridRange().getDimension()));
 
                 String lowers = "";
                 String upers = "";
 
                 for (int r = 0; r < g.getGridRange().getDimension(); r++) {
                     lowers += (g.getGridRange().getLower(r) + " ");
                     upers += (g.getGridRange().getUpper(r) + " ");
                 }
 
                 cw.openTag("grid", m);
                 cw.textTag("low", lowers);
                 cw.textTag("high", upers);
 
                 if (dimNames != null) {
                     for (int dn = 0; dn < dimNames.length; dn++)
                         cw.textTag("axisName", dimNames[dn].toString());
                 }
 
                 // //
                 // AlFa: storing geo-transform
                 // //
                 if (tx instanceof AffineTransform) {
                     AffineTransform aTX = (AffineTransform) tx;
                     cw.openTag("geoTransform");
                     cw.textTag("scaleX", String.valueOf(aTX.getScaleX()));
                     cw.textTag("scaleY", String.valueOf(aTX.getScaleY()));
                     cw.textTag("shearX", String.valueOf(aTX.getShearX()));
                     cw.textTag("shearY", String.valueOf(aTX.getShearY()));
                     cw.textTag("translateX", String.valueOf(aTX.getTranslateX()));
                     cw.textTag("translateY", String.valueOf(aTX.getTranslateY()));
                     cw.closeTag("geoTransform");
                 }
 
                 cw.closeTag("grid");
             }
 
             if (cv.getDimensions() != null) {
                 CoverageDimension[] dims = cv.getDimensions();
 
                 for (int d = 0; d < dims.length; d++) {
                     Double[] nulls = dims[d].getNullValues();
                     cw.openTag("CoverageDimension");
                     cw.textTag("name", dims[d].getName());
                     cw.textTag("description", dims[d].getDescription());
 
                     if (dims[d].getRange() != null) {
                         cw.openTag("interval");
                         cw.textTag("min", Double.toString(dims[d].getRange().getMinimum(true)));
                         cw.textTag("max", Double.toString(dims[d].getRange().getMaximum(true)));
                         cw.closeTag("interval");
                     }
                     else
                     {
                         cw.openTag("interval");
                         cw.textTag("min", Double.toString(Double.NEGATIVE_INFINITY));
                         cw.textTag("max", Double.toString(Double.POSITIVE_INFINITY));
                         cw.closeTag("interval");
                     }
 
                     if (nulls != null) {
                         cw.openTag("nullValues");
                         for (int n = 0; n < nulls.length; n++) {
                             cw.textTag("value", nulls[n].toString());
                         }
                         cw.closeTag("nullValues");
                     }
                     cw.closeTag("CoverageDimension");
                 }
             }
 
             cw.openTag("supportedCRSs");
 
             if ((cv.getRequestCRSs() != null) && (cv.getRequestCRSs().size() != 0)) {
                 String s = "";
                 Iterator i = cv.getRequestCRSs().iterator();
 
                 if (i.hasNext()) {
                     s = i.next().toString();
 
                     while (i.hasNext()) {
                         s = s + "," + i.next().toString();
                     }
                 }
 
                 cw.textTag("requestCRSs", s);
             }
 
             if ((cv.getResponseCRSs() != null) && (cv.getResponseCRSs().size() != 0)) {
                 String s = "";
                 Iterator i = cv.getResponseCRSs().iterator();
 
                 if (i.hasNext()) {
                     s = i.next().toString();
 
                     while (i.hasNext()) {
                         s = s + "," + i.next().toString();
                     }
                 }
 
                 cw.textTag("responseCRSs", s);
             }
 
             cw.closeTag("supportedCRSs");
 
             m = new HashMap();
 
             if ((cv.getNativeFormat() != null) && (cv.getNativeFormat() != "")) {
                 m.put("nativeFormat", cv.getNativeFormat());
             }
 
             cw.openTag("supportedFormats", m);
 
             if ((cv.getSupportedFormats() != null) && (cv.getSupportedFormats().size() != 0)) {
                 String s = "";
                 Iterator i = cv.getSupportedFormats().iterator();
 
                 if (i.hasNext()) {
                     s = i.next().toString();
 
                     while (i.hasNext()) {
                         s = s + "," + i.next().toString();
                     }
                 }
 
                 cw.textTag("formats", s);
             }
 
             cw.closeTag("supportedFormats");
 
             m = new HashMap();
 
             if ((cv.getDefaultInterpolationMethod() != null)
                     && (cv.getDefaultInterpolationMethod() != "")) {
                 m.put("default", cv.getDefaultInterpolationMethod());
             }
 
             cw.openTag("supportedInterpolations", m);
 
             if ((cv.getInterpolationMethods() != null)
                     && (cv.getInterpolationMethods().size() != 0)) {
                 String s = "";
                 Iterator i = cv.getInterpolationMethods().iterator();
 
                 if (i.hasNext()) {
                     s = i.next().toString();
 
                     while (i.hasNext()) {
                         s = s + "," + i.next().toString();
                     }
                 }
 
                 cw.textTag("interpolationMethods", s);
             }
 
             cw.closeTag("supportedInterpolations");
 
             // ///////////////////////////////////////////////////////////////////////
             //
             // STORING READ PARAMETERS
             //
             // ///////////////////////////////////////////////////////////////////////
             if ((cv.getParameters() != null) && (cv.getParameters().size() != 0)) {
                 cw.openTag("parameters");
                 final Iterator i = cv.getParameters().keySet().iterator();
                 final HashMap temp = new HashMap();
 
                 while (i.hasNext()) {
                     String key = (String) i.next();
                     if (cv.getParameters().get(key) != null) {
                         temp.put("name", key);
                         temp.put("value", cv.getParameters().get(key).toString().replaceAll(
                                 "\"", "'"));
                     }
                     cw.attrTag("parameter", temp);
                 }
 
                 cw.closeTag("parameters");
             }
 
             cw.closeTag("coverage");
             fw.close();
         } catch (IOException e) {
             throw new ConfigurationException(e);
         }
     }
 
     /**
      * WriterUtils purpose.
      * 
      * <p>
      * This is a static class which is used by XMLConfigWriter for File IO
      * validation tests.
      * </p>
      * 
      * <p>
      * </p>
      * 
      * @author dzwiers, Refractions Research, Inc.
      * @version $Id$
      */
     public static class WriterUtils {
         /** Used internally to create log information to detect errors. */
         private static final Logger LOGGER = Logging.getLogger("org.vfny.geoserver.global");
 
         /**
          * WriterUtils constructor.
          * 
          * <p>
          * Static class, should never be used.
          * </p>
          */
         private WriterUtils() {
         }
 
         /**
          * initFile purpose.
          * 
          * <p>
          * Checks to ensure the handle exists. If the handle is a directory and
          * not created, it is created
          * </p>
          * 
          * @param f
          *            the File handle
          * @param isDir
          *            true when the handle is intended to be a directory.
          * 
          * @return The file passed in.
          * 
          * @throws ConfigurationException
          *             When an IO error occurs or the handle is invalid.
          */
         public static File initFile(File f, boolean isDir) throws ConfigurationException {
             if (!f.exists()) {
                 if (LOGGER.isLoggable(Level.FINER)) {
                     LOGGER.finer(new StringBuffer("Creating File: ").append(f.toString())
                             .toString());
                 }
 
                 if (isDir) {
                     if (!f.mkdir()) {
                         throw new ConfigurationException(
                                 "Path specified does not have a valid file.\n" + f + "\n\n");
                     }
                 } else {
                     try {
                         if (LOGGER.isLoggable(Level.SEVERE)) {
                             LOGGER.severe(new StringBuffer("Attempting to create file:").append(
                                     f.getAbsolutePath()).toString());
                         }
 
                         if (!f.createNewFile()) {
                             throw new ConfigurationException(
                                     "Path specified does not have a valid file.\n" + f + "\n\n");
                         }
                     } catch (IOException e) {
                         throw new ConfigurationException(e);
                     }
                 }
             }
 
             if (isDir && !f.isDirectory()) {
                 throw new ConfigurationException("Path specified does not have a valid file.\n" + f
                         + "\n\n");
             }
 
             if (!isDir && !f.isFile()) {
                 throw new ConfigurationException("Path specified does not have a valid file.\n" + f
                         + "\n\n");
             }
 
             if (LOGGER.isLoggable(Level.FINER)) {
                 LOGGER.finer(new StringBuffer("File is valid: ").append(f).toString());
             }
 
             return f;
         }
 
         /**
          * initFile purpose.
          * 
          * <p>
          * Checks to ensure the handle exists and can be writen to. If the
          * handle is a directory and not created, it is created
          * </p>
          * 
          * @param f
          *            the File handle
          * @param isDir
          *            true when the handle is intended to be a directory.
          * 
          * @return The file passed in.
          * 
          * @throws ConfigurationException
          *             When an IO error occurs or the handle is invalid.
          */
         public static File initWriteFile(File f, boolean isDir) throws ConfigurationException {
             initFile(f, isDir);
 
             if (!f.canWrite()) {
                 throw new ConfigurationException("Cannot Write to file: " + f.toString());
             }
 
             return f;
         }
         
         public static void backupFile( File f, boolean rename ) throws IOException {
             File b = new File( f.getAbsolutePath() + ".bak" );
             
             //kill the pre-existing backup if it exists
             if ( b.exists() ) {
                 WriterUtils.delete( b );
             }
             
             if( rename ) {
                 f.renameTo( b );
             }
             else {
                 FileUtils.copyFile( f, b );
             }
             
         }
         
         /**
          * Backs up a directory by appending .bak to its name.
          */
         public static void backupDirectory(File d) throws IOException {
             File b = new File( d.getAbsolutePath() + ".bak" );
             if ( b.exists() ) {
                 //kill the old backup
                 
                 //should be a directory, but check anyways
                 if ( b.isDirectory() ) {
                     deleteDirectory( b );     
                 }
                 else {
                     b.delete();
                 }
                
             }
             
             //rename directory
             d.renameTo( b );
         }
         
         /**
          * Deletes a file, handling the case in which it is a directory.
          */
         public static void delete( File f ) throws IOException {
             if ( f.isDirectory() ) {
                 WriterUtils.deleteDirectory( f );
             }
             else {
                 f.delete();
             }
         }
         /**
          * Recursively deletes a directory.
          */
         public static void deleteDirectory( File d ) throws IOException {
             File[] ls = d.listFiles();
             for ( int i = 0; i < ls.length; i++ ) {
                 if ( ls[i].isDirectory() ) {
                     deleteDirectory(ls[i]);
                 }
                 else {
                     ls[i].delete();
                 }
             }
             d.delete();
         }
     }
 }
