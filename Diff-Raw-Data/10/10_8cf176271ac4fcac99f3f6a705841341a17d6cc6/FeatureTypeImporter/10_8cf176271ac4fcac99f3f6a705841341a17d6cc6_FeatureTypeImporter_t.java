 package org.geoserver.uploader;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.geoserver.catalog.Catalog;
 import org.geoserver.catalog.CatalogBuilder;
 import org.geoserver.catalog.DataStoreInfo;
 import org.geoserver.catalog.FeatureTypeInfo;
 import org.geoserver.catalog.LayerInfo;
 import org.geoserver.catalog.NamespaceInfo;
 import org.geoserver.catalog.ProjectionPolicy;
 import org.geoserver.catalog.StyleInfo;
 import org.geoserver.catalog.WorkspaceInfo;
 import org.geoserver.importer.StyleGenerator;
 import org.geotools.data.DataStore;
 import org.geotools.data.DataStoreFactorySpi;
 import org.geotools.data.DataStoreFinder;
 import org.geotools.data.DefaultTransaction;
 import org.geotools.data.FeatureWriter;
 import org.geotools.data.Query;
 import org.geotools.data.Transaction;
 import org.geotools.data.shapefile.ShapefileDataStoreFactory;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureIterator;
 import org.geotools.data.simple.SimpleFeatureSource;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.geotools.referencing.CRS;
 import org.geotools.util.logging.Logging;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
 class FeatureTypeImporter extends LayerImporter {
 
     private static final Logger LOGGER = Logging.getLogger(FeatureTypeImporter.class);
 
     private static final List<String> supportedExtensions = Arrays.asList(".shp");
 
     private DataStoreInfo storeInfo;
 
     public FeatureTypeImporter(Catalog catalog, WorkspaceInfo targetWorkspace,
             DataStoreInfo targetDataStore) {
         super(catalog, targetWorkspace);
         this.storeInfo = targetDataStore;
     }
 
     @Override
     public LayerInfo importFromFile(File file) throws IOException {
         if (storeInfo == null) {
             file = ensureUnique(workspaceInfo, file);
         }
         /*
          * If there's a .prj file next to the given file in ESRI format, once it's parsed by the
          * DataStore, CRS.lookUpCRS won't match an EPSG anymore. So make an attempt to convert the
          * .prj file before we get the DataStore
          */
         convertPrjWKTToEPSG(new File(file.getParentFile(),
                 FilenameUtils.getBaseName(file.getName()) + ".prj"));
 
         final DataStoreFactorySpi dsf = getDataStoreFactory(file);
         final Map<? extends String, ? extends Serializable> connectionParameters;
         connectionParameters = getConnectionParameters(file,
                 catalog.getNamespaceByPrefix(workspaceInfo.getName()));
         SimpleFeatureSource featureSource = getFeatureSource(file.getName(), connectionParameters);
 
         CatalogBuilder builder = new CatalogBuilder(catalog);
         builder.setWorkspace(workspaceInfo);
 
         boolean addStoreInfoToCatalog = true;
         if (storeInfo == null) {
             storeInfo = builder.buildDataStore(file.getName());
             storeInfo.setDescription(title);
             storeInfo.setType(dsf.getDisplayName());
             storeInfo.getConnectionParameters().putAll(connectionParameters);
         } else {
             addStoreInfoToCatalog = false;
             featureSource = importToStore(featureSource, (DataStoreInfo) storeInfo);
         }
 
         builder.setStore(storeInfo);
 
         FeatureTypeInfo ftInfo = builder.buildFeatureType(featureSource);
         if (ftInfo.getSRS() == null) {
             boolean extensive = true;
             builder.lookupSRS(ftInfo, extensive);
             if (ftInfo.getSRS() == null) {
                 ftInfo.setSRS("EPSG:4326");
                 ftInfo.setProjectionPolicy(ProjectionPolicy.FORCE_DECLARED);
             }
         }
         builder.setupBounds(ftInfo);
 
         String title = super.title;
         if (null == title || title.trim().length() == 0) {
            title = ftInfo.getName();
         }
         ftInfo.setTitle(title);
         ftInfo.setAbstract(_abstract);
 
         LayerInfo layerInfo = builder.buildLayer(ftInfo);
 
         StyleGenerator styles = new StyleGenerator(catalog);
         StyleInfo style = styles.getStyle(ftInfo);
         try {
             catalog.add(style);
             layerInfo.setDefaultStyle(style);
             if (addStoreInfoToCatalog) {
                 catalog.add(storeInfo);
             }
             catalog.add(ftInfo);
             catalog.add(layerInfo);
         } catch (RuntimeException e) {
             // wouldn't it be cool to have catalog transactions?
             try {
                 catalog.remove(layerInfo);
             } finally {
             }
             try {
                 catalog.remove(style);
             } finally {
             }
             try {
                 catalog.remove(ftInfo);
             } finally {
             }
             try {
                 catalog.remove(storeInfo);
             } finally {
             }
             throw e;
         }
         return layerInfo;
 
     }
 
     private void convertPrjWKTToEPSG(final File prjFile) {
         if (!prjFile.exists()) {
             return;
         }
         String wkt;
         try {
             wkt = FileUtils.readFileToString(prjFile);
             CoordinateReferenceSystem parsed = CRS.parseWKT(wkt);
             Integer epsgCode = CRS.lookupEpsgCode(parsed, false);
             CoordinateReferenceSystem epsgCrs = null;
             if (epsgCode == null) {
                 epsgCode = CRS.lookupEpsgCode(parsed, true);
                 if (epsgCode != null) {
                     epsgCrs = CRS.decode("EPSG:" + epsgCode);
                 }
                 if (epsgCrs != null) {
                     String epsgWKT = epsgCrs.toWKT();
                     FileUtils.writeStringToFile(prjFile, epsgWKT);
                 }
             }
         } catch (IOException e) {
             LOGGER.log(Level.FINE, "Problem reading .prj file " + prjFile.getName(), e);
             // no problem
         } catch (FactoryException e) {
             LOGGER.log(Level.FINE, "Problem parsing .prj file " + prjFile.getName(), e);
             // no problem
         }
     }
 
     private DataStoreFactorySpi getDataStoreFactory(File file) {
         // TODO: support more formats
         ShapefileDataStoreFactory shpFac = new ShapefileDataStoreFactory();
         return shpFac;
     }
 
     private SimpleFeatureSource importToStore(final SimpleFeatureSource source,
             final DataStoreInfo targetInfo) throws IOException {
 
         final DataStore dataStore = (DataStore) targetInfo.getDataStore(null);
 
         final String sourceTypeName = source.getName().getLocalPart();
         String targetTypeName = sourceTypeName.toLowerCase();
         int tries = 0;
         while (Arrays.asList(dataStore.getTypeNames()).contains(targetTypeName)) {
             tries++;
             targetTypeName = sourceTypeName.toLowerCase() + tries;
         }
 
         final SimpleFeatureType sourceType = source.getSchema();
         SimpleFeatureType targetType = sourceType;
 
         if (!sourceTypeName.equals(targetTypeName)) {
             SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
             builder.setName(targetTypeName);
             builder.addAll(sourceType.getAttributeDescriptors());
             targetType = builder.buildFeatureType();
         }
 
         dataStore.createSchema(targetType);
 
         final int count = source.getCount(Query.ALL);
 
         SimpleFeatureCollection sourceFeatures = source.getFeatures();
         SimpleFeatureIterator features = sourceFeatures.features();
         try {
             FeatureWriter<SimpleFeatureType, SimpleFeature> writer;
             Transaction transaction = new DefaultTransaction();// Transaction.AUTO_COMMIT;
             writer = dataStore.getFeatureWriterAppend(targetTypeName, transaction);
             try {
                 int inserted = 0;
                 while (features.hasNext()) {
                     SimpleFeature sourceF = features.next();
                     SimpleFeature newF = writer.next();
                     newF.setAttributes(sourceF.getAttributes());
                     writer.write();
                     inserted++;
                 }
                 transaction.commit();
             } catch (IOException e) {
                 transaction.rollback();
                 // TODO: there's no dataStore.dropSchema()....
                 throw e;
             } catch (RuntimeException rte) {
                 transaction.rollback();
             } finally {
                 transaction.close();
                 writer.close();
             }
         } finally {
             features.close();
         }
 
         SimpleFeatureSource imported;
         imported = dataStore.getFeatureSource(targetType.getName());
         return imported;
     }
 
     private SimpleFeatureSource getFeatureSource(final String typeName,
             final Map<? extends String, ? extends Serializable> connectionParameters)
             throws IOException {
 
         SimpleFeatureSource featureSource;
 
         DataStore dataStore;
         dataStore = DataStoreFinder.getDataStore(connectionParameters);
         featureSource = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
 
         return featureSource;
     }
 
     @SuppressWarnings({ "unchecked", "rawtypes" })
     private Map<? extends String, ? extends Serializable> getConnectionParameters(File file,
             NamespaceInfo namespaceInfo) {
 
         Map params = new HashMap<String, String>();
         try {
             params.put(ShapefileDataStoreFactory.URLP.key, file.toURI().toURL());
         } catch (MalformedURLException e) {
             throw new RuntimeException(e);
         }
         params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, "false");
         params.put("namespace", namespaceInfo.getURI());
 
         return params;
     }
 
     public static boolean canHandle(File spatialFile) {
         String extension = VFSWorker.getExtension(spatialFile.getName().toLowerCase());
         boolean canHandle = supportedExtensions.contains(extension);
         return canHandle;
     }
 }
