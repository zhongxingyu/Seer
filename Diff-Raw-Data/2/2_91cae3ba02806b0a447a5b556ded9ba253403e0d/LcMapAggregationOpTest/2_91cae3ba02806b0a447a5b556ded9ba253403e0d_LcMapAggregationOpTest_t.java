 package org.esa.cci.lc.aggregation;
 
 import org.esa.beam.binning.operator.FormatterConfig;
 import org.esa.beam.dataio.netcdf.metadata.profiles.beam.BeamNetCdf4WriterPlugIn;
 import org.esa.beam.framework.dataio.ProductIOPlugInManager;
 import org.esa.beam.framework.datamodel.Band;
 import org.esa.beam.framework.datamodel.CrsGeoCoding;
 import org.esa.beam.framework.datamodel.MetadataAttribute;
 import org.esa.beam.framework.datamodel.MetadataElement;
 import org.esa.beam.framework.datamodel.Product;
 import org.esa.beam.framework.datamodel.ProductData;
 import org.esa.beam.framework.gpf.GPF;
 import org.esa.beam.framework.gpf.OperatorException;
 import org.esa.beam.framework.gpf.OperatorSpiRegistry;
 import org.esa.cci.lc.subset.PredefinedRegion;
 import org.esa.cci.lc.util.LcHelper;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.hamcrest.core.IsNull;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import javax.media.jai.operator.ConstantDescriptor;
 import java.io.File;
 import java.io.IOException;
 
 import static org.hamcrest.core.Is.*;
 import static org.junit.Assert.*;
 import static org.junit.matchers.JUnitMatchers.*;
 
 
 public class LcMapAggregationOpTest {
 
     private static LcMapAggregationOp.Spi aggregationSpi;
     private static BeamNetCdf4WriterPlugIn beamNetCdf4WriterPlugIn;
 
     @BeforeClass
     public static void beforeClass() {
         aggregationSpi = new LcMapAggregationOp.Spi();
         OperatorSpiRegistry spiRegistry = GPF.getDefaultInstance().getOperatorSpiRegistry();
         spiRegistry.addOperatorSpi(aggregationSpi);
         beamNetCdf4WriterPlugIn = new BeamNetCdf4WriterPlugIn();
         ProductIOPlugInManager.getInstance().addWriterPlugIn(beamNetCdf4WriterPlugIn);
     }
 
     @AfterClass
     public static void afterClass() {
         OperatorSpiRegistry spiRegistry = GPF.getDefaultInstance().getOperatorSpiRegistry();
         spiRegistry.removeOperatorSpi(aggregationSpi);
         ProductIOPlugInManager.getInstance().removeWriterPlugIn(beamNetCdf4WriterPlugIn);
     }
 
     @Test()
     public void testTargetProductCreation_Binning() throws Exception {
         // preparation
         LcMapAggregationOp aggregationOp = createAggrOp();
         aggregationOp.setGridName(PlanetaryGridName.GEOGRAPHIC_LAT_LON);
         aggregationOp.setSourceProduct(createSourceProduct());
         int numMajorityClasses = 2;
         aggregationOp.setNumMajorityClasses(numMajorityClasses);
         aggregationOp.setNumRows(4);
         aggregationOp.formatterConfig = createFormatterConfig();
         aggregationOp.outputTargetProduct = true;
 
         // execution
         Product targetProduct = aggregationOp.getTargetProduct();
 
         // verification
         int numObsAndPasses = 2;
         int numPFTs = 14;
         int numAccuracyBands = 1;
         final int expectedNumBands = LCCS.getInstance().getNumClasses()
                                      + numMajorityClasses
                                      + numObsAndPasses
                                      + numPFTs
                                      + numAccuracyBands;
         assertThat(targetProduct.getNumBands(), is(expectedNumBands));
     }
 
     @Test()
     public void testTargetProductCreation_WithOnlyPFTClasses() throws Exception {
         // preparation
         LcMapAggregationOp aggregationOp = createAggrOp();
         aggregationOp.setGridName(PlanetaryGridName.GEOGRAPHIC_LAT_LON);
         aggregationOp.setSourceProduct(createSourceProduct());
         aggregationOp.setOutputLCCSClasses(false);
         int numMajorityClasses = 0;
         aggregationOp.setNumMajorityClasses(numMajorityClasses);
         aggregationOp.setNumRows(4);
         aggregationOp.formatterConfig = createFormatterConfig();
         aggregationOp.outputTargetProduct = true;
 
         // execution
         Product targetProduct = aggregationOp.getTargetProduct();
 
         // verification
         int numObsAndPasses = 2;
         int numPFTs = 14;
         int numAccuracyBands = 1;
         final int expectedNumBands = numMajorityClasses
                                      + numObsAndPasses
                                      + numPFTs
                                      + numAccuracyBands;
         assertThat(targetProduct.getNumBands(), is(expectedNumBands));
     }
 
     @Test
     public void testDefaultValues() {
         LcMapAggregationOp aggrOp = (LcMapAggregationOp) aggregationSpi.createOperator();
         assertThat(aggrOp.getGridName(), isNull());
 //        assertThat(aggrOp.getPixelSizeX(), is(0.1));
 //        assertThat(aggrOp.getPixelSizeY(), is(0.1));
         assertThat(aggrOp.isOutputLCCSClasses(), is(true));
         assertThat(aggrOp.getNumMajorityClasses(), is(5));
         assertThat(aggrOp.isOutputPFTClasses(), is(true));
         assertThat(aggrOp.getNumRows(), is(2160));
 
         final Product sourceProduct = new Product("dummy", "t", 20, 10);
         sourceProduct.setFileLocation(new File(".", "a-b-c-d-e-f-g-h.nc"));
         aggrOp.setSourceProduct(sourceProduct);
         aggrOp.setGridName(PlanetaryGridName.GEOGRAPHIC_LAT_LON);
         LcHelper.ensureTargetDir(aggrOp.getTargetDir(), aggrOp.getSourceProduct());
         FormatterConfig formatterConfig = aggrOp.createDefaultFormatterConfig();
         assertThat(formatterConfig.getOutputType(), is("Product"));
        assertThat(formatterConfig.getOutputFile(), is("a-b-c-d-aggregated-0.083333Deg-e-f-g-h.nc"));
     }
 
     private IsNull isNull() {
         return new IsNull();
     }
 
     @Test
     public void testNumRows_LessThanTwo() {
         LcMapAggregationOp aggrOp = createAggrOp();
         aggrOp.setNumRows(1);
         try {
             aggrOp.initialize();
         } catch (OperatorException oe) {
             String message = oe.getMessage().toLowerCase();
             assertThat(message, containsString("rows"));
         }
     }
 
     @Test
     public void testNumRows_OddValue() {
         LcMapAggregationOp aggrOp = createAggrOp();
         aggrOp.setNumRows(23);
         try {
             aggrOp.initialize();
         } catch (OperatorException oe) {
             String message = oe.getMessage().toLowerCase();
             assertThat(message, containsString("rows"));
         }
     }
 
     @Test
     public void testNoOutputSelected() {
         LcMapAggregationOp aggrOp = createAggrOp();
         aggrOp.setOutputLCCSClasses(false);
         aggrOp.setOutputPFTClasses(false);
         aggrOp.setNumMajorityClasses(0);
         try {
             aggrOp.initialize();
         } catch (OperatorException oe) {
             String message = oe.getMessage();
             assertThat(message, containsString("LCCS"));
             assertThat(message, containsString("majority"));
             assertThat(message, containsString("PFT"));
         }
     }
 
 
     @Test
     public void testRegionEnvelope_WithPredefinedRegion() throws Exception {
         LcMapAggregationOp aggrOp = createAggrOp();
         aggrOp.setPredefinedRegion(PredefinedRegion.GREENLAND);
 
         final ReferencedEnvelope region = aggrOp.getRegionEnvelope();
         assertEquals(PredefinedRegion.GREENLAND.getEast(), region.getMaximum(0), 1.0e-6);
         assertEquals(PredefinedRegion.GREENLAND.getNorth(), region.getMaximum(1), 1.0e-6);
         assertEquals(PredefinedRegion.GREENLAND.getWest(), region.getMinimum(0), 1.0e-6);
         assertEquals(PredefinedRegion.GREENLAND.getSouth(), region.getMinimum(1), 1.0e-6);
     }
 
     @Test
     public void testRegionEnvelope_WithUserdefinedRegion() throws Exception {
         LcMapAggregationOp aggrOp = createAggrOp();
         aggrOp.setNorth(88.8f);
         aggrOp.setEast(10.4f);
         aggrOp.setSouth(54.4f);
         aggrOp.setWest(-36.63f);
 
         final ReferencedEnvelope region = aggrOp.getRegionEnvelope();
         assertEquals(-36.63f, region.getMinimum(0), 1.0e-6);
         assertEquals(88.8f, region.getMaximum(1), 1.0e-6);
         assertEquals(10.4f, region.getMaximum(0), 1.0e-6);
         assertEquals(54.4f, region.getMinimum(1), 1.0e-6);
     }
 
     @Test
     public void testOnlyOneIsTrue() {
         final boolean O = false;
         final boolean I = true;
 
         assertThat(LcMapAggregationOp.onlyOneIsTrue(O, O, O), is(false));
         assertThat(LcMapAggregationOp.onlyOneIsTrue(O, O, I), is(true));
         assertThat(LcMapAggregationOp.onlyOneIsTrue(O, I, O), is(true));
         assertThat(LcMapAggregationOp.onlyOneIsTrue(O, I, I), is(false));
         assertThat(LcMapAggregationOp.onlyOneIsTrue(I, O, O), is(true));
         assertThat(LcMapAggregationOp.onlyOneIsTrue(I, O, I), is(false));
         assertThat(LcMapAggregationOp.onlyOneIsTrue(I, I, O), is(false));
         assertThat(LcMapAggregationOp.onlyOneIsTrue(I, I, I), is(false));
     }
 
     private FormatterConfig createFormatterConfig() throws IOException {
         final FormatterConfig formatterConfig = new FormatterConfig();
         formatterConfig.setOutputFormat("NetCDF4-BEAM");
         File tempFile = File.createTempFile("BEAM-TEST_", ".nc");
         tempFile.deleteOnExit();
         formatterConfig.setOutputFile(tempFile.getAbsolutePath());
         formatterConfig.setOutputType("Product");
         return formatterConfig;
     }
 
     private Product createSourceProduct() throws Exception {
         final Integer width = 3600;
         final Integer height = 1800;
         final Product product = new Product("P", "T", width, height);
         product.setFileLocation(new File("/blah/ESACCI-LC-L4-LCCS-Map-300m-P5Y-2010-v2.nc"));
         final Band classesBand = product.addBand("lccs_class", ProductData.TYPE_UINT8);
         classesBand.setSourceImage(ConstantDescriptor.create(width.floatValue(), height.floatValue(),
                                                              new Byte[]{10}, null));
         final Band processedFlag = product.addBand("processed_flag", ProductData.TYPE_INT8);
         processedFlag.setSourceImage(ConstantDescriptor.create(width.floatValue(), height.floatValue(),
                                                                new Byte[]{1}, null));
         final Band currentPixelState = product.addBand("current_pixel_state", ProductData.TYPE_INT8);
         currentPixelState.setSourceImage(ConstantDescriptor.create(width.floatValue(), height.floatValue(),
                                                                    new Byte[]{1}, null));
         final Band observationBand = product.addBand("observation_count", ProductData.TYPE_INT8);
         observationBand.setSourceImage(ConstantDescriptor.create(width.floatValue(), height.floatValue(),
                                                                  new Float[]{10f}, null));
         final Band algoConfidBand = product.addBand("algorithmic_confidence_level", ProductData.TYPE_FLOAT32);
         algoConfidBand.setSourceImage(ConstantDescriptor.create(width.floatValue(), height.floatValue(),
                                                                 new Float[]{10f}, null));
         final Band overallConfidBand = product.addBand("overall_confidence_level", ProductData.TYPE_INT8);
         overallConfidBand.setSourceImage(ConstantDescriptor.create(width.floatValue(), height.floatValue(),
                                                                    new Float[]{10f}, null));
         product.setGeoCoding(new CrsGeoCoding(DefaultGeographicCRS.WGS84, width, height, -179.95, 89.95, 0.1, 0.1));
         MetadataElement globalAttributes = new MetadataElement("Global_Attributes");
         globalAttributes.addAttribute(new MetadataAttribute("id", ProductData.createInstance("ESACCI-LC-L4-LCCS-Map-300m-P5Y-2010-v2"), true));
         globalAttributes.addAttribute(new MetadataAttribute("time_coverage_duration", ProductData.createInstance("P5Y"), true));
         globalAttributes.addAttribute(new MetadataAttribute("time_coverage_resolution", ProductData.createInstance("P7D"), true));
         globalAttributes.addAttribute(new MetadataAttribute("time_coverage_start", ProductData.createInstance("2000"), true));
         globalAttributes.addAttribute(new MetadataAttribute("time_coverage_end", ProductData.createInstance("2012"), true));
         globalAttributes.addAttribute(new MetadataAttribute("product_version", ProductData.createInstance("1.0"), true));
         globalAttributes.addAttribute(new MetadataAttribute("geospatial_lat_min", ProductData.createInstance("-90"), true));
         globalAttributes.addAttribute(new MetadataAttribute("geospatial_lat_max", ProductData.createInstance("90"), true));
         globalAttributes.addAttribute(new MetadataAttribute("geospatial_lon_min", ProductData.createInstance("-180"), true));
         globalAttributes.addAttribute(new MetadataAttribute("geospatial_lon_max", ProductData.createInstance("180"), true));
         product.getMetadataRoot().addElement(globalAttributes);
         return product;
     }
 
     private LcMapAggregationOp createAggrOp() {
         LcMapAggregationOp aggregationOp = (LcMapAggregationOp) aggregationSpi.createOperator();
         aggregationOp.setTargetDir(new File("."));
         return aggregationOp;
     }
 }
