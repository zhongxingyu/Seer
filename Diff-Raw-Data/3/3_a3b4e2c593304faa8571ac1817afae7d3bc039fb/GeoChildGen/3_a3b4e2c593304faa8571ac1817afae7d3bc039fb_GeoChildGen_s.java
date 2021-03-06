 package com.bc.merci.geochildgen;
 
 import com.bc.childgen.ChildGenException;
 import com.bc.childgen.ChildGeneratorFactory;
 import com.bc.childgen.ChildGeneratorImpl;
 import com.bc.util.bean.BeanUtils;
 import com.bc.util.file.StreamUtils;
 import com.bc.util.file.ZipUtils;
 import com.bc.util.geom.Geometry;
 import com.bc.util.geom.GeometryParser;
 import com.bc.util.geom.GeometryUtils;
 import com.bc.util.geom.RangeConverter;
 import com.bc.util.product.ProductHelper;
 import com.bc.util.sql.BcDatabase;
 import com.bc.util.sql.DataSourceConfig;
 import com.bc.util.sql.SqlUtils;
 import org.esa.beam.framework.dataio.ProductIO;
 import org.esa.beam.framework.datamodel.GeoCoding;
 import org.esa.beam.framework.datamodel.Product;
 import org.esa.beam.util.logging.BeamLogManager;
 import org.esa.beam.util.math.Range;
 
 import javax.sql.DataSource;
 import java.io.*;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class GeoChildGen {
 
     /**
      * @param params command line parameter
      * @throws IOException       on disk access errors
      * @throws ParseException    on malformed parameters
      * @throws ChildGenException on any other failure
      * @throws SQLException      on database access failures
      */
     public static void run(CmdLineParams params) throws IOException, ParseException, SQLException, ChildGenException {
         BeamLogManager.removeRootLoggerHandlers();
         final String outputDirName = params.getOutputDirName();
         final File outputDir = new File(outputDirName);
 
         if (!(outputDir.isDirectory() || outputDir.mkdirs())) {
             System.err.println("Output directory '" + outputDir.getAbsolutePath() +
                     "' does not exist or could not be created");
             return;
         }
 
         final GeoChildGenProperties config = readProperties(params.getPropertiesFileName());
         final List<Geometry> siteGeometryList;
         if (params.isDatabaseUsed()) {
             siteGeometryList = getGeometryFromDatabase(config.getDataSourceConfig());
         } else {
             siteGeometryList = parseGeometryProperties(config.getGeometry());
         }
 
         for (int i = 0; i < params.getInputFileNameList().size(); ++i) {
             final String inputFileName = params.getInputFileNameList().get(i);
             final File inputFile = new File(inputFileName);
             if (!inputFile.isFile()) {
                 System.err.println("Input file '" + inputFile.getAbsolutePath() + "' does not exist");
                 continue;
             }
 
             final Product product = ProductIO.readProduct(inputFile, null);
             if (product == null) {
                 System.err.println("No product reader was found to open file '" + inputFile.getAbsolutePath() + "'");
                 continue;
             }
 
             final Geometry productBoundary = ProductHelper.extractGeoBoundary(product, PRODUCT_BOUNDARY_STEP);
             final GeoCoding geoCoding = product.getGeoCoding();
             final int width = product.getSceneRasterWidth();
             final int height = product.getSceneRasterHeight();
 
             for (int j = 0; j < siteGeometryList.size(); ++j) {
                 final Geometry siteGeometry = siteGeometryList.get(j);
 
                 if (isIntersection(siteGeometry, productBoundary)) {
                     if (params.isCreateChildOption()) {
                         final Range[] ranges = intersectionRange(siteGeometry, productBoundary, geoCoding, width, height);
                         for (int k = 0; k < ranges.length; k++) {
                             createChildProduct(inputFile, ranges[k], outputDir, config.getChildProductOriginatorId());
                         }
                     } else {
                         final File outputFile = new File(outputDir.getAbsolutePath() + File.separator + inputFile.getName());
                         if (!outputFile.createNewFile()) {
                             System.err.println("File '" + outputFile.getAbsolutePath() + "' already exists or could not be created");
                             break;
                         }
 
                         copyProduct(inputFile, outputFile);
                     }
                 } else {
                     System.out.println("No intersections with file " + inputFileName);
                 }
             }
 
             product.dispose();
         }
     }
 
     static void printUsageTo(OutputStream out) {
         final PrintStream stream = new PrintStream(out);
 
         stream.print("geochildgen version ");
         stream.println(CmdLineConstants.VERSION_STRING);
         stream.println();
         stream.print("Usage: geochildgen [");
         stream.print(CmdLineConstants.GEO_PROPS_OPTION);
         stream.print(" <propertiesFile>] ");
         stream.print("[");
         stream.print(CmdLineConstants.DB_PROPS_OPTION);
         stream.println(" <propertiesFile>]");
         stream.print("       [");
         stream.print(CmdLineConstants.SITE_CAT_OPTION);
         stream.print(" <cat_a, cat_b, ...>] [");
         stream.print(CmdLineConstants.CREATE_CHILD_OPTION);
         stream.println("]");
         stream.print("       [");
         stream.print(CmdLineConstants.OUT_DIR_OPTION);
         stream.println(" <outputDir>] <inputFile>...");
         stream.println();
         stream.println("Options:");
         stream.print("    ");
         stream.print(CmdLineConstants.GEO_PROPS_OPTION);
         stream.println(" - select to use geometry properties");
         stream.println("         from <propertiesFile>");
         stream.print("    ");
         stream.print(CmdLineConstants.DB_PROPS_OPTION);
         stream.println(" - select to use Site geometries from database");
         stream.println("         defined in <propertiesFile>");
         stream.print("    ");
         stream.print(CmdLineConstants.SITE_CAT_OPTION);
         stream.println(" - define site categories to be used as comma separated");
         stream.println("         list of category name. Use together with -d option.");
         stream.print("    ");
         stream.print(CmdLineConstants.CREATE_CHILD_OPTION);
         stream.println(" - select to create a child product in <outputDir>.");
         stream.println("         If not set, intersecting products are copied.");
         stream.print("    ");
         stream.print(CmdLineConstants.OUT_DIR_OPTION);
         stream.println(" - defines the <outputDir>.");
         stream.println("         If not set, current directory is used.");
     }
 
     ///////////////////////////////////////////////////////////////////////////
     /////// END OF PUBLIC
     ///////////////////////////////////////////////////////////////////////////
 
     private static final int PRODUCT_BOUNDARY_STEP = 128;
     private static final int MINIMUM_NUMBER_OF_LINES = 300;
     private static final int MAX_PRODUCT_WIDTH = 2241;
 
     private static void copyProduct(File inputFile, File outputFile) throws IOException {
         final FileInputStream fis = new FileInputStream(inputFile);
         final FileOutputStream fos = new FileOutputStream(outputFile);
 
         try {
             StreamUtils.copyStream(fis, fos);
             System.out.println("copied product '" + inputFile.getPath() + "'");
         } catch (IOException e) {
             System.err.println("Failed to copy '" + inputFile.getAbsolutePath() +
                     "' to output directory");
         }
 
         fis.close();
         fos.close();
     }
 
     /**
      * @param productFile              the input file
      * @param range                    the line range
      * @param outputDir                the target directory
      * @param childProductOriginatorId the originator identifier
      * @throws com.bc.childgen.ChildGenException
      *          when something goes wrong
      */
     private static void createChildProduct(final File productFile, final Range range,
                                            final File outputDir, final String childProductOriginatorId) throws ChildGenException {
         final int firstLine = (int) range.getMin();
         final int lastLine = (int) range.getMax();
 
         final ChildGeneratorImpl childGenerator = ChildGeneratorFactory.createChildGenerator(productFile.getName());
 
         File expandedFile = null;
 
         try {
             File workFile = productFile;
             if (ZipUtils.isCompressedFileName(productFile)) {
                 expandedFile = ZipUtils.unpackInTempDir(productFile);
                 workFile = expandedFile;
             }
 
//            final ProductSubsetDef subset = new ProductSubsetDef();
//            subset.setRegion(0, firstLine, MAX_PRODUCT_WIDTH, lastLine - firstLine);

             childGenerator.process(workFile, outputDir, childProductOriginatorId, 1, firstLine, lastLine);
 
             System.out.println("created child product of '" + productFile.getPath() + "'");
         } catch (IOException e) {
             final File childProduct = childGenerator.getTargetProduct();
             if (childProduct != null && childProduct.exists()) {
                 childProduct.delete();
             }
             System.err.println("Failed to write child product to '" + outputDir.getAbsolutePath() + "'");
             System.err.println(e.getMessage());            
         } finally {
             if (expandedFile != null) {
                 expandedFile.delete();
             }
         }
     }
 
     /**
      * @param siteGeometry
      * @param productBoundary
      * @param geoCoding
      * @param width
      * @param height
      * @return the line range of the intersection
      */
     private static Range[] intersectionRange(final Geometry siteGeometry,
                                              final Geometry productBoundary, final GeoCoding geoCoding,
                                              final int width, final int height) {
         final Range[] ranges = RangeConverter.getRangeFromPolygonGeometry(siteGeometry, productBoundary, geoCoding,
                 width, height);
 
         for (int i = 0; i < ranges.length; i++) {
             RangeConverter.adjustRange(ranges[i], height, MINIMUM_NUMBER_OF_LINES, ranges[i]);
         }
 
         return ranges;
     }
 
     /**
      * @param geometry
      * @param geometry2
      * @return whether they intersect or not
      */
     private static boolean isIntersection(final Geometry geometry, final Geometry geometry2) {
         final int status = GeometryUtils.performGeometryOp(GeometryUtils.GEOM_OP_INTERSECTS,
                 geometry, geometry2);
 
         return status == Geometry.TRUE;
     }
 
     /**
      * @param propertiesFileName
      * @return
      * @throws IOException
      * @throws ParseException
      */
     private static GeoChildGenProperties readProperties(final String propertiesFileName)
             throws IOException, ParseException {
         File propertiesFile = new File(propertiesFileName);
 
         if (!propertiesFile.isFile()) {
             System.err.println("Properties file '" + propertiesFileName + "' does not exist");
         }
 
         GeoChildGenProperties config = new GeoChildGenProperties();
         BeanUtils.setBeanProperties(config, propertiesFile);
 
         return config;
     }
 
     private static List<Geometry> getGeometryFromDatabase(DataSourceConfig dataSourceConfig) throws SQLException {
         final DataSource datasource = SqlUtils.createDatasource(dataSourceConfig);
         System.out.println("Accessing database:");
         System.out.println("  url : " + dataSourceConfig.getUrl());
         System.out.println("  user: " + dataSourceConfig.getUsername());
 
         final BcDatabase db = new BcDatabase(datasource);
         final List<Geometry> geometries = GeoChildGenUtils.getGeometriesFromDb(db, null);
 
         System.out.println("Read '" + geometries.size() + "' geometry/-ies from database");
 
         return geometries;
     }
 
     /**
      * @param geometries
      * @throws ParseException
      */
     private static List<Geometry> parseGeometryProperties(final String[] geometries)
             throws ParseException {
         final List<Geometry> geometryList = new ArrayList<Geometry>();
         final GeometryParser parser = new GeometryParser();
 
         for (int i = 0; i < geometries.length; ++i) {
             geometryList.add(parser.parseWKT(geometries[i]));
         }
 
         System.out.println("Read '" + geometries.length + "' geometry/-ies from file");
 
         return geometryList;
     }
 }
