 package thredds.catalog;
 
 import gov.usgs.cida.ncetl.utils.FileHelper;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public final class CatalogHelper {
 
     private static final Logger LOG = LoggerFactory.getLogger(CatalogHelper.class);
     private static final String DEFAULT_CATALOG_FILENAME = "catalog.xml";
     private static final String DEFAULT_CATALOG_LOCATION = FileHelper.dirAppend(
             FileHelper.getDatasetsDirectory(), DEFAULT_CATALOG_FILENAME);
     private static final String DEFAULT_CATALOG_NAME = "ncETL generated THREDDS catalog";
 
     private CatalogHelper() {
     }
 
     /**
      * Sets up a location in the default location location.
      * 
      * @throws URISyntaxException
      * @throws IOException 
      */
     public static void setupCatalog() throws URISyntaxException, IOException  {
         setupCatalog(new File(DEFAULT_CATALOG_LOCATION), DEFAULT_CATALOG_NAME);
     }
     
     public static void setupCatalog(File location) throws URISyntaxException, IOException {
         setupCatalog(location, DEFAULT_CATALOG_NAME);
     }
     
     public static void setupCatalog(File location, String catalogName) throws URISyntaxException, IOException {
         if (!location.exists()) {
             createNewCatalog(catalogName, location.getPath());
         }
     }
     
     public static void createNewCatalog(String name, String absolutePath) throws URISyntaxException, IOException {
         URI uri = new URI("file://" + absolutePath);
         InvCatalogImpl impl = createCatalogImpl(name, uri);
         writeCatalog(impl);
     }
 
     public static InvCatalog readCatalog(URI absoluteURI) {
         InvCatalogFactory factory = new InvCatalogFactory("FileReadFactory",
                                                           false);
         return factory.readXML(absoluteURI.toString());
     }
 
     public static void writeCatalog(InvCatalog cat) throws IOException {
         InvCatalogImpl impl = (InvCatalogImpl) cat;
         File file = new File(impl.getBaseURI());
 
         FileOutputStream fos = null;
         try {
             fos = new FileOutputStream(file);
             impl.writeXML(fos);
         }
         finally {
             IOUtils.closeQuietly(fos);
         }
     }
 
     protected static InvCatalogImpl createCatalogImpl(String name, URI uri) {
         InvCatalogFactory factory = new InvCatalogFactory("CatalogFactory", true);
         InvCatalogImpl impl = new InvCatalogImpl(name, "1.0", uri);
        //impl.setCatalogFactory(factory);
        //impl.setCatalogConverterToVersion1();
         return impl;
     }
 
     public static void addDataset(InvCatalog cat, InvDataset dataset) {
         InvCatalogImpl concreteCat = (InvCatalogImpl) cat;
         InvDatasetImpl concreteDataset = (InvDatasetImpl) dataset;
         concreteCat.addDatasetByID(concreteDataset);
     }
 
     public static void removeDataset(InvCatalog cat, String datasetId) {
         InvCatalogImpl concreteCat = (InvCatalogImpl) cat;
         InvDatasetImpl ds = (InvDatasetImpl) new InvDatasetWrapper("blah",
                                                                    datasetId).build();
         concreteCat.removeDatasetByID(ds);
     }
 
     public static void addService(InvCatalog cat, InvService serv) {
         InvCatalogImpl concreteCat = (InvCatalogImpl) cat;
         concreteCat.addService(serv);
     }
 
     public static void removeService(InvCatalog cat, String serviceName) {
         InvCatalogImpl concreteCat = (InvCatalogImpl) cat;
         InvService serv = cat.serviceHash.remove(serviceName);
         concreteCat.services.remove(serv);
     }
 
     public static String getDefaultCatalogFilename() {
         return DEFAULT_CATALOG_FILENAME;
     }
 
     public static String getDefaultCatalogName() {
         return DEFAULT_CATALOG_NAME;
     }
     
     public static URI setupDatasetDirectory(String datasetName) {
         URI catalogUri = null;
         try {
             File dsDir = FileHelper.createDatasetDirectory(datasetName);
             File catalogFile = new File(dsDir.getPath() + File.separator + "catalog.xml");
             setupCatalog(catalogFile);
             catalogUri = catalogFile.toURI();
         }
         catch (IOException ex) {
             LOG.debug("unable to setup dataset directory", ex);
         }
         catch (URISyntaxException ex) {
             LOG.debug("file specified incorrectly", ex);
         }
         return catalogUri;
     }
 }
