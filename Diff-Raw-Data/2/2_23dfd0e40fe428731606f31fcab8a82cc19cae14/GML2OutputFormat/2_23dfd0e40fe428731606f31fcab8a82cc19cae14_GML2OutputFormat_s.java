 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.wfs.xml;
 
 import net.opengis.wfs.FeatureCollectionType;
 import org.geoserver.ows.util.ResponseUtils;
 import org.geoserver.platform.Operation;
 import org.geoserver.platform.ServiceException;
 import org.geoserver.wfs.WFS;
 import org.geoserver.wfs.WFSGetFeatureOutputFormat;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureType;
 import org.geotools.gml.producer.FeatureTransformer;
 import org.geotools.gml.producer.FeatureTransformer.FeatureTypeNamespaces;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.global.GeoServer;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.zip.GZIPOutputStream;
 import javax.xml.transform.TransformerException;
 
 
 /**
  * Encodes features in Geographic Markup Language (GML) version 2.
  *
  * <p>
  * GML2-GZIP format is just GML2 with gzip compression. If GML2-GZIP format was
  * requested, <code>getContentEncoding()</code> will retutn
  * <code>"gzip"</code>, otherwise will return <code>null</code>
  * </p>
  *
  * @author Gabriel Rold?n
  * @version $Id$
  */
 public class GML2OutputFormat extends WFSGetFeatureOutputFormat {
     private static final int NO_FORMATTING = -1;
     private static final int INDENT_SIZE = 2;
     public static final String formatName = "GML2";
     public static final String formatNameCompressed = "GML2-GZIP";
 
     /**
      * This is a "magic" class provided by Geotools that writes out GML for an
      * array of FeatureResults.
      *
      * <p>
      * This class seems to do all the work, if you have a problem with GML you
      * will need to hunt it down. We supply all of the header information in
      * the execute method, and work through the featureList in the writeTo
      * method.
      * </p>
      *
      * <p>
      * This value will be <code>null</code> until execute is called.
      * </p>
      */
     private FeatureTransformer transformer;
 
     /** will be true if GML2-GZIP output format was requested */
     private boolean compressOutput = false;
 
     /**
      * WFS configuration
      */
     private WFS wfs;
 
     /**
      * GeoServer configuration
      */
     private GeoServer geoServer;
 
     /**
      * The catalog
      */
     private Data catalog;
 
     /**
      * Creates the producer with a reference to the GetFeature operation
      * using it.
      */
     public GML2OutputFormat(WFS wfs, GeoServer geoServer, Data catalog) {
         super("GML2");
 
         this.wfs = wfs;
         this.geoServer = geoServer;
         this.catalog = catalog;
     }
 
     /**
     * prepares for encoding into GML2 format, optionally compressing its
     * output in gzip, if outputFormat is equal to GML2-GZIP
     *
     * @param outputFormat DOCUMENT ME!
     * @param results DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
     public void prepare(String outputFormat, FeatureCollectionType results)
         throws IOException {
         this.compressOutput = formatNameCompressed.equalsIgnoreCase(outputFormat);
 
         transformer = createTransformer();
 
         FeatureTypeNamespaces ftNames = transformer.getFeatureTypeNamespaces();
         Map ftNamespaces = new HashMap();
 
         //TODO: the srs is a back, it only will work property when there is 
         // one type, we really need to set it on the feature level
         int srs = -1;
 
         for (Iterator f = results.getFeature().iterator(); f.hasNext();) {
             //FeatureResults features = (FeatureResults) f.next();
             FeatureCollection features = (FeatureCollection) f.next();
             FeatureType featureType = features.getSchema();
 
             FeatureTypeInfo meta = catalog.getFeatureTypeInfo(featureType.getTypeName(),
                     featureType.getNamespace().toString());
 
             String prefix = meta.getNameSpace().getPrefix();
             String uri = meta.getNameSpace().getURI();
 
             ftNames.declareNamespace(features.getSchema(), prefix, uri);
 
             if (ftNamespaces.containsKey(uri)) {
                 String location = (String) ftNamespaces.get(uri);
                 ftNamespaces.put(uri, location + "," + meta.getName());
             } else {
                 String location = typeSchemaLocation(wfs, meta);
                 ftNamespaces.put(uri, location);
             }
 
             srs = Integer.parseInt(meta.getSRS());
         }
 
         System.setProperty("javax.xml.transform.TransformerFactory",
             "org.apache.xalan.processor.TransformerFactoryImpl");
 
         transformer.setIndentation(wfs.isVerbose() ? INDENT_SIZE : (NO_FORMATTING));
         transformer.setNumDecimals(geoServer.getNumDecimals());
         transformer.setFeatureBounding(wfs.isFeatureBounding());
         transformer.setEncoding(wfs.getCharSet());
 
         String wfsSchemaloc = wfsSchemaLocation(wfs);
         transformer.addSchemaLocation("http://www.opengis.net/wfs", wfsSchemaloc);
 
         for (Iterator it = ftNamespaces.keySet().iterator(); it.hasNext();) {
             String uri = (String) it.next();
             transformer.addSchemaLocation(uri, (String) ftNamespaces.get(uri));
         }
 
         transformer.setGmlPrefixing(wfs.getCiteConformanceHacks());
 
         if (results.getLockId() != null) {
             transformer.setLockId(results.getLockId());
         }
 
         if (srs != -1) {
             transformer.setSrsName(wfs.getSrsPrefix() + srs);
         }
     }
 
     public Set getOutputFormats() {
         return new HashSet(Arrays.asList(new String[] { "GML2", "text/xml; subtype=gml/2.1.2" }));
     }
 
     /**
       * DOCUMENT ME!
       *
       * @return DOCUMENT ME!
       */
     public String getContentEncoding() {
         return compressOutput ? "gzip" : null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param output DOCUMENT ME!
      *
      * @throws ServiceException DOCUMENT ME!
      * @throws IOException DOCUMENT ME!
      * @throws IllegalStateException DOCUMENT ME!
      */
     public void encode(OutputStream output, FeatureCollectionType results)
         throws ServiceException, IOException {
         if (results == null) {
             throw new IllegalStateException("It seems prepare() has not been called"
                 + " or has not succeed");
         }
 
         GZIPOutputStream gzipOut = null;
 
         if (compressOutput) {
             gzipOut = new GZIPOutputStream(output);
             output = gzipOut;
         }
 
         // execute should of set all the header information
         // including the lockID
         //
         // execute should also fail if all of the locks could not be aquired
         List resultsList = results.getFeature();
         FeatureCollection[] featureResults = (FeatureCollection[]) resultsList.toArray(new FeatureCollection[resultsList
                 .size()]);
 
         try {
             transformer.transform(featureResults, output);
 
             //we need to "finish" here because if not,it is possible that the gzipped
             //content do not gets completely written
             if (gzipOut != null) {
                 gzipOut.finish();
                 gzipOut.flush();
             }
         } catch (TransformerException gmlException) {
             String msg = " error:" + gmlException.getMessage();
             throw new ServiceException(msg, gmlException);
         }
     }
 
     protected void write(FeatureCollectionType featureCollection, OutputStream output,
         Operation getFeature) throws IOException, ServiceException {
         prepare("GML2", featureCollection);
         encode(output, featureCollection);
     }
 
     protected FeatureTransformer createTransformer() {
         return new FeatureTransformer();
     }
 
     protected String wfsSchemaLocation(WFS wfs) {
         return ResponseUtils.appendPath(wfs.getSchemaBaseURL(), "wfs/1.0.0/WFS-basic.xsd");
     }
 
     protected String typeSchemaLocation(WFS wfs, FeatureTypeInfo meta) {
         return ResponseUtils.appendQueryString(wfs.getOnlineResource().toString(),
            "version=1.0.0&request=DescribeFeatureType&typeName=" + meta.getName());
     }
 }
