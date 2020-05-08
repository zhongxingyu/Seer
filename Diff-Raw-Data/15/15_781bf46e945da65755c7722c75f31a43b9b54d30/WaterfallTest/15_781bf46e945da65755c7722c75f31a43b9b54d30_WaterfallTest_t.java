 
 package gov.usgs.cida.watersmart.parse.column;
 
 import gov.usgs.cida.watersmart.common.ModelType;
 import gov.usgs.cida.watersmart.common.RunMetadata;
 import gov.usgs.cida.watersmart.parse.CreateDSGFromZip;
 import gov.usgs.cida.watersmart.parse.CreateDSGFromZip.ReturnInfo;
 import java.io.File;
 import java.io.IOException;
 import javax.xml.stream.XMLStreamException;
 import org.apache.commons.io.FileUtils;
 import static org.junit.Assert.*;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class WaterfallTest {
 
     public WaterfallTest() {
     }
     
     private static final String outputDir = System.getProperty("java.io.tmpdir") + "/waterfall";
     private static File sampleFile = null;
     
     @BeforeClass
     public static void setupClass() throws IOException {
         FileUtils.forceMkdir(new File(outputDir));
         sampleFile = new File(WaterfallTest.class.getClassLoader()
                .getResource("gov/usgs/cida/watersmart/netcdf/WATERFALL_realupload.zip")
                 .getFile());
     }
     
     @AfterClass
     public static void tearDownClass() throws IOException {
         FileUtils.deleteDirectory(new File(outputDir));
     }
 
     @Test
     public void testNetCDFFromWaterfall() throws IOException, XMLStreamException {
         RunMetadata runMeta = new RunMetadata(ModelType.WATERFALL, "1", "test", "1", "1", "2012-07-10T00:00:00Z", 
             "Special", "comments", "jiwalker@usgs.gov", "http://cida-wiwsc-gdp2qa.er.usgs.gov:8082/geoserver/nwc/ows", 
             "nwc:se_sites", "site_no");
         ReturnInfo info = CreateDSGFromZip.create(sampleFile, runMeta);
         File ncFile = new File("/tmp/waterfall/" + info.filename);
         
        assertEquals(info.filename, "WATERFALL_realupload.nc");
         assertEquals(FileUtils.sizeOf(ncFile), 3185373L);
         FileUtils.deleteQuietly(ncFile);
     }
 
 }
