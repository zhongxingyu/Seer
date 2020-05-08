 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.wfs.response;
 
 import net.opengis.wfs.FeatureCollectionType;
 import net.opengis.wfs.GetFeatureType;
 import net.opengis.wfs.QueryType;
 import org.geoserver.ows.util.OwsUtils;
 import org.geoserver.platform.Operation;
 import org.geoserver.platform.ServiceException;
 import org.geoserver.wfs.WFSGetFeatureOutputFormat;
 import org.geotools.data.FeatureStore;
 import org.geotools.data.shapefile.ShapefileDataStore;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureType;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 import javax.xml.namespace.QName;
 
 
 /**
  *
  * This class returns a shapefile encoded results of the users's query.
  *
  * Based on ShapeFeatureResponseDelegate.java from geoserver 1.5.x
  *
  * @author originally authored by Chris Holmes, The Open Planning Project, cholmes@openplans.org
  * @author ported to gs 1.6.x by Saul Farber, MassGIS, saul.farber@state.ma.us
  *
  */
 public class ShapeZipOutputFormat extends WFSGetFeatureOutputFormat {
     private final Logger LOGGER = Logger.getLogger(this.getClass().toString());
     private String outputFileName;
 
     public ShapeZipOutputFormat() {
         super("SHAPE-ZIP");
     }
 
     /**
      * @see WFSGetFeatureOutputFormat#getMimeType(Object, Operation)
      */
     public String getMimeType(Object value, Operation operation)
         throws ServiceException {
         return "application/zip";
     }
 
     /**
      * We abuse this method to pre-discover the query typenames so we know what to set in the
      * content-disposition header.
      */
     protected boolean canHandleInternal(Operation operation) {
         GetFeatureType request = (GetFeatureType) OwsUtils.parameter(operation.getParameters(),
                 GetFeatureType.class);
         outputFileName = ((QName) ((QueryType) request.getQuery().get(0)).getTypeName().get(0))
             .getLocalPart();
 
         return true;
     }
 
     public String[][] getHeaders(Object value, Operation operation)
         throws ServiceException {
         return (String[][]) new String[][] {
             { "Content-Disposition", "attachment; filename=" + outputFileName + ".zip" }
         };
     }
 
     /**
      * @see WFSGetFeatureOutputFormat#write(Object, OutputStream, Operation)
      */
     protected void write(FeatureCollectionType featureCollection, OutputStream output,
         Operation getFeature) throws IOException, ServiceException {
         //We might get multiple featurecollections in our response (multiple queries?) so we need to
         //write out multiple shapefile sets, one for each query response.
         File tempDir = createTempDirectory();
         ZipOutputStream zipOut = new ZipOutputStream(output);
 
         try {
             Iterator outputFeatureCollections = featureCollection.getFeature().iterator();
             FeatureCollection curCollection;
 
             while (outputFeatureCollections.hasNext()) {
                 curCollection = (FeatureCollection) outputFeatureCollections.next();
                 writeCollectionToShapefile(curCollection, tempDir);
 
                 String name = curCollection.getSchema().getTypeName();
                 String outputName = name.replaceAll("\\.", "_");
 
                 // read in and write out .shp
                 File f = new File(tempDir, name + ".shp");
                 ZipEntry entry = new ZipEntry(outputName + ".shp");
                 zipOut.putNextEntry(entry);
 
                 InputStream shp_in = new FileInputStream(f);
                 readInWriteOutBytes(zipOut, shp_in);
                 zipOut.closeEntry();
                 shp_in.close();
 
                 // read in and write out .dbf
                 f = new File(tempDir, name + ".dbf");
                 entry = new ZipEntry(outputName + ".dbf");
                 zipOut.putNextEntry(entry);
 
                 InputStream dbf_in = new FileInputStream(f);
                 readInWriteOutBytes(zipOut, dbf_in);
                 zipOut.closeEntry();
                 dbf_in.close();
 
                 // read in and write out .shx
                 f = new File(tempDir, name + ".shx");
                 entry = new ZipEntry(outputName + ".shx");
                 zipOut.putNextEntry(entry);
 
                 InputStream shx_in = new FileInputStream(f);
                 readInWriteOutBytes(zipOut, shx_in);
                 zipOut.closeEntry();
                 shx_in.close();
 
                 // if we generated the prj file, include it as well
                 f = new File(tempDir, name + ".prj");
                 entry = new ZipEntry(outputName + ".prj");
                 zipOut.putNextEntry(entry);
 
                 InputStream prj_in = new FileInputStream(f);
                 readInWriteOutBytes(zipOut, prj_in);
                 zipOut.closeEntry();
                 prj_in.close();
             }
 
             zipOut.finish();
             zipOut.flush();
 
             // This is an error, because this closes the output stream too... it's
             // not the right place to do so
             // zipOut.close();
         } finally {
             // make sure we remove the temp directory and its contents completely now
             if (!removeDirectory(tempDir)) {
                 LOGGER.warning("Could not delete temp directory: " + tempDir.getAbsolutePath());
             }
         }
     }
 
     private boolean removeDirectory(File tempDir) {
         if (!tempDir.exists() || !tempDir.isDirectory()) {
             return false;
         }
 
         File[] files = tempDir.listFiles();
 
         if (files == null) {
             return false;
         }
 
         for (int i = 0; i < files.length; i++) {
             if (files[i].isDirectory()) {
                 removeDirectory(files[i]);
             } else {
                 files[i].delete();
             }
         }
 
         return tempDir.delete();
     }
 
     /**
      * readInWriteOutBytes Description: Reads in the bytes from the
      * input stream and writes them to the output stream.
      *
      * @param output
      * @param in
      *
      * @throws IOException
      */
     private void readInWriteOutBytes(OutputStream output, InputStream in)
         throws IOException {
         int c;
 
         while (-1 != (c = in.read())) {
             output.write(c);
         }
     }
 
     /**
      * Write one featurecollection to an appropriately named shapefile.
      * @param c the featurecollection to write
      * @param tempDir the temp directory into which it should be written
      */
     private void writeCollectionToShapefile(FeatureCollection c, File tempDir) {
         FeatureType schema = c.getSchema();
 
         try {
            File file = new File(tempDir, schema.getTypeName() + ".shp");
             ShapefileDataStore sfds = new ShapefileDataStore(file.toURL());
 
             try {
                 sfds.createSchema(schema);
             } catch (NullPointerException e) {
                 LOGGER.warning(
                     "Error in shapefile schema. It is possible you don't have a geometry set in the output. \n"
                     + "Please specify a <wfs:PropertyName>geom_column_name</wfs:PropertyName> in the request");
                 throw new ServiceException(
                     "Error in shapefile schema. It is possible you don't have a geometry set in the output.");
             }
 
             FeatureStore store = (FeatureStore) sfds.getFeatureSource(schema.getTypeName());
             store.addFeatures(c);
         } catch (IOException ioe) {
             LOGGER.log(Level.WARNING,
                 "Error while writing featuretype '" + schema.getTypeName() + "' to shapefile.", ioe);
             throw new ServiceException(ioe);
         }
     }
 
     /**
      * Creates a temporary directory into which we'll write the various shapefile components for this response.
      *
      * Strategy is to leverage the system temp directory, then create a sub-directory.
      * @return
      */
     private File createTempDirectory() {
         try {
             File dummyTemp = File.createTempFile("blah", null);
             String sysTempDir = dummyTemp.getParentFile().getAbsolutePath();
             dummyTemp.delete();
 
             File reqTempDir = new File(sysTempDir + File.separator + "wfsshptemp" + Math.random());
             reqTempDir.mkdir();
 
             return reqTempDir;
         } catch (IOException e) {
             LOGGER.log(Level.WARNING,
                 "Unable to properly create a temporary directory when trying to output a shapefile.  Is the system temp directory writeable?",
                 e);
             throw new ServiceException(
                 "Error in shapefile schema. It is possible you don't have a geometry set in the output.");
         }
     }
 }
