 package thredds.catalog;
 
 import com.google.common.collect.Lists;
 import gov.usgs.cida.ncetl.utils.FileHelper;
 import java.io.*;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.LinkedList;
 import java.util.List;
import org.apache.commons.io.FileUtils;
 import static org.junit.Assert.*;
 import org.junit.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ucar.nc2.constants.FeatureType;
 
 /**
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class CatalogTest {
 
     private static InvCatalogFactory factory = null;
     private static Logger log = LoggerFactory.getLogger(CatalogTest.class);
    private static File tmpCatalog;
 
     @BeforeClass
     public static void setUpClass() throws Exception {
         factory = new InvCatalogFactory("testFactory", true);
        tmpCatalog = new File(FileHelper.getTempDirectory() + CatalogHelper.getDefaultCatalogFilename());
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {

     }
 
     @Before
     public void setUp() {
     }
 
     @After
    public void tearDown() throws IOException {
        if (tmpCatalog.exists()) {
            FileUtils.forceDelete(tmpCatalog);
        }
     }
 
     @Ignore
     @Test
     public void readCatalog() {
 
         InvCatalogImpl readXML = factory.readXML(
                 "file://" + FileHelper.getBaseDirectory() + "catalog.xml");
         List<InvDataset> datasets = readXML.getDatasets();
         assertEquals(datasets.size(), 2);
         InvDataset dataset = datasets.get(0);
         DataFormatType dft = dataset.getDataFormatType();
         assertTrue(dft.equals(DataFormatType.NETCDF));
     }
 
     //@Ignore
     @Test
     public void writeCatalog() throws URISyntaxException, FileNotFoundException,
                                       IOException {
        URI uri = tmpCatalog.toURI();
         InvCatalogImpl impl = new InvCatalogImpl("Test Catalog", "1.0", uri);
        FileOutputStream fos = new FileOutputStream(tmpCatalog);
         impl.writeXML(fos);
        assertTrue(tmpCatalog.exists());
 
     }
     
     @Test
     public void testDataset() throws URISyntaxException, IOException {
        CatalogHelper.setupCatalog(tmpCatalog, "testcat");
        InvCatalog readCatalog = CatalogHelper.readCatalog(tmpCatalog.toURI());
         InvDatasetImpl ds = new InvDatasetImpl(null, "test");
         
         ThreddsMetadata tmd = new ThreddsMetadata(true);
         tmd.addProperty(new InvProperty("test", "value"));
         tmd.setDataType(FeatureType.GRID);
         tmd.addVariables(new ThreddsMetadata.Variables("temp"));
         InvMetadata im = new InvMetadata(ds, true, tmd);
 
         ThreddsMetadata setThis = new ThreddsMetadata(false);
         setThis.addMetadata(im);
         ds.setLocalMetadata(setThis);
         //ds.finish();
 
         InvCatalogModifier mod = new InvCatalogModifier(readCatalog);
         LinkedList<InvDataset> dsList = Lists.newLinkedList();
         dsList.add(ds);
         mod.setDatasets(dsList);
         CatalogHelper.writeCatalog(readCatalog);
        BufferedReader buf = new BufferedReader(new FileReader(tmpCatalog));
         String line = "";
         StringBuilder total = new StringBuilder();
         while ((line = buf.readLine()) != null) {
             total.append(line);
         }
         System.out.println(total.toString());
         assertEquals(ds.getLocalMetadataInheritable().getDataType(), FeatureType.GRID);
         assertEquals(ds.getLocalMetadata().isInherited(), false);
         assertTrue(total.toString().contains("metadata inherited"));
     }
 }
