 package gov.usgs.cida.gdp.coreprocessing.analysis.grid;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import gov.usgs.cida.gdp.coreprocessing.analysis.GeoToolsNetCDFUtility;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Formatter;
 
 
 import org.geotools.data.FeatureSource;
 import org.geotools.data.FileDataStore;
 import org.geotools.data.FileDataStoreFinder;
 import org.geotools.feature.FeatureCollection;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 
 import org.opengis.referencing.operation.TransformException;
 
 import ucar.ma2.InvalidRangeException;
 import ucar.ma2.Range;
 import ucar.nc2.constants.FeatureType;
 import ucar.nc2.dt.GridCoordSystem;
 import ucar.nc2.dt.GridDatatype;
 import ucar.nc2.dt.grid.GridDataset;
 import ucar.nc2.ft.FeatureDataset;
 import ucar.nc2.ft.FeatureDatasetFactoryManager;
 import ucar.unidata.geoloc.LatLonPointImpl;
 import ucar.unidata.geoloc.LatLonRect;
 
 import static gov.usgs.cida.gdp.coreprocessing.GridCellHelper.*;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 public class GridUtilityTest {
 
     private String ncLocation;
     private String sfLocation;
     private FeatureDataset dataset;
     private FileDataStore dataStore;
     private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GridUtilityTest.class);
 
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
         log.debug("Started testing class.");
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws Exception {
         log.debug("Ended testing class.");
     }
 
     @Before
     public void setUp() throws IOException, InvalidRangeException {
         ncLocation = getResourceDir() + File.separator + "testGridCellCoverageTYX.ncml";
         sfLocation = getResourceDir() + File.separator + "Trout_Lake_HRUs_rotated_geo_WGS84.shp";
         dataStore = FileDataStoreFinder.getDataStore(new File(sfLocation));
     }
 
     @After
     public void tearDown() throws IOException {
         if (dataset != null) {
             dataset.close();
         }
         if (dataStore != null) {
             dataStore.dispose();
         }
     }
 
     @Test
     public void testRangesFromLatLonRect_Standard() throws TransformException, IOException, InvalidRangeException {
         LatLonRect llr = makeLatLonRect(ncLocation);
         GridCoordSystem gcs = getGridCoordinateSystem();
         Range[] range = GridUtility.getRangesFromLatLonRect(llr, gcs);
         assertNotNull(range);
     }
 
     @Test(expected = InvalidRangeException.class)
     public void testRangesFromLatLonRect_InvalidRange() throws InvalidRangeException, IOException {
         getFeatureCollection(ncLocation);
         LatLonRect llr = new LatLonRect(
                 new LatLonPointImpl(-1, -1),
                 new LatLonPointImpl(-1, -1));
         GridCoordSystem gcs = getGridCoordinateSystem();
         GridUtility.getRangesFromLatLonRect(llr, gcs);
     }
 
     @Test
     public void testTopOfGrid() throws IOException, InvalidRangeException {
         ncLocation = getResourceDir() + File.separator + "testCoverageTYXSmallestValid.ncml";
         getFeatureCollection(ncLocation);
         LatLonRect llr = new LatLonRect(
                 new LatLonPointImpl(42, -91),
                 new LatLonPointImpl(42, -89));
         GridCoordSystem gcs = getGridCoordinateSystem();
         Range[] range = GridUtility.getRangesFromLatLonRect(llr, gcs);
         assertTrue("Range should be 3x3 since they are buffered", (range[0].length() == 3 && range[1].length() == 3));
     }
 
     @Test
     public void testBottomOfGrid() throws IOException, InvalidRangeException {
         ncLocation = getResourceDir() + File.separator + "testCoverageTYXSmallestValid.ncml";
         getFeatureCollection(ncLocation);
         LatLonRect llr = new LatLonRect(
                 new LatLonPointImpl(40, -91),
                 new LatLonPointImpl(40, -89));
         GridCoordSystem gcs = getGridCoordinateSystem();
         Range[] range = GridUtility.getRangesFromLatLonRect(llr, gcs);
         assertTrue("Range should be 3x3 since they are buffered", (range[0].length() == 3 && range[1].length() == 3));
     }
 
     @Test
     public void testLeftOfGrid() throws IOException, InvalidRangeException {
         ncLocation = getResourceDir() + File.separator + "testCoverageTYXSmallestValid.ncml";
         getFeatureCollection(ncLocation);
         LatLonRect llr = new LatLonRect(
                 new LatLonPointImpl(40, -91),
                 new LatLonPointImpl(42, -91));
         GridCoordSystem gcs = getGridCoordinateSystem();
         Range[] range = GridUtility.getRangesFromLatLonRect(llr, gcs);
         assertTrue("Range should be 3x3 since they are buffered", (range[0].length() == 3 && range[1].length() == 3));
     }
 
     @Test
     public void testMiddleOfGrid() throws IOException, InvalidRangeException {
         ncLocation = getResourceDir() + File.separator + "testCoverageTYXSmallestValid.ncml";
         getFeatureCollection(ncLocation);
         LatLonRect llr = new LatLonRect(
                 new LatLonPointImpl(41, -90),
                 new LatLonPointImpl(41, -90));
         GridCoordSystem gcs = getGridCoordinateSystem();
         Range[] range = GridUtility.getRangesFromLatLonRect(llr, gcs);
         assertTrue("Range should be 3x3 since they are buffered", (range[0].length() == 3 && range[1].length() == 3));
     }
 
     @Test
     public void testMiddleOfBiggerGrid() throws IOException, InvalidRangeException {
         ncLocation = getResourceDir() + File.separator + "testCoverageTYXFourByFour.ncml";
         getFeatureCollection(ncLocation);
         LatLonRect llr = new LatLonRect(
                 new LatLonPointImpl(41, -90),
                 new LatLonPointImpl(42, -89));
         GridCoordSystem gcs = getGridCoordinateSystem();
         Range[] range = GridUtility.getRangesFromLatLonRect(llr, gcs);
        assertTrue("Range should be 4x4 since they are buffered", (range[0].length() == 4 && range[1].length() == 4));
     }
 
     @Test
     public void testTopMiddleOfBiggerGrid() throws IOException, InvalidRangeException {
         ncLocation = getResourceDir() + File.separator + "testCoverageTYXFourByFour.ncml";
         getFeatureCollection(ncLocation);
         LatLonRect llr = new LatLonRect(
                 new LatLonPointImpl(43, -90),
                 new LatLonPointImpl(42, -89));
         GridCoordSystem gcs = getGridCoordinateSystem();
         Range[] range = GridUtility.getRangesFromLatLonRect(llr, gcs);
        assertTrue("Range should be 4x3 since they are buffered", (range[0].length() == 4 && range[1].length() == 3));
     }
 
     @Test
     public void testBottomMiddleOfBiggerGrid() throws IOException, InvalidRangeException {
         ncLocation = getResourceDir() + File.separator + "testCoverageTYXFourByFour.ncml";
         getFeatureCollection(ncLocation);
         LatLonRect llr = new LatLonRect(
                 new LatLonPointImpl(41, -90),
                 new LatLonPointImpl(40, -89));
         GridCoordSystem gcs = getGridCoordinateSystem();
         Range[] range = GridUtility.getRangesFromLatLonRect(llr, gcs);
        assertTrue("Range should be 4x3 since they are buffered", (range[0].length() == 4 && range[1].length() == 3));
     }
 
     @Test
     public void testLeftMiddleOfBiggerGrid() throws IOException, InvalidRangeException {
         ncLocation = getResourceDir() + File.separator + "testCoverageTYXFourByFour.ncml";
         getFeatureCollection(ncLocation);
         LatLonRect llr = new LatLonRect(
                 new LatLonPointImpl(41, -91),
                 new LatLonPointImpl(42, -90));
         GridCoordSystem gcs = getGridCoordinateSystem();
         Range[] range = GridUtility.getRangesFromLatLonRect(llr, gcs);
        assertTrue("Range should be 3x4 since they are buffered", (range[0].length() == 3 && range[1].length() == 4));
     }
 
     private FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatureCollection(String ncLocation) throws IOException {
         dataset = FeatureDatasetFactoryManager.open(FeatureType.GRID, ncLocation, null, new Formatter());
         dataStore = FileDataStoreFinder.getDataStore(new File(sfLocation));
         FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource();
         FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = featureSource.getFeatures();
         return featureCollection;
     }
 
     private GridCoordSystem getGridCoordinateSystem() {
         GridDataset gd = (GridDataset) dataset;
         GridDatatype gdt = gd.findGridDatatype(GridTypeTest.DATATYPE_RH);
         return gdt.getCoordinateSystem();
     }
 
     private LatLonRect makeLatLonRect(String ncLocation) throws TransformException, IOException {
         return GeoToolsNetCDFUtility.getLatLonRectFromEnvelope(getFeatureCollection(ncLocation).getBounds());
     }
 }
