 package thredds.catalog;
 
 import com.google.common.collect.Lists;
 import gov.usgs.cida.ncetl.utils.FileHelper;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.LinkedList;
 import java.util.List;
 import javax.crypto.spec.IvParameterSpec;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.junit.Ignore;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import thredds.catalog.parser.jdom.InvCatalogFactory10;
 import ucar.nc2.constants.FeatureType;
 
 /**
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class CatalogTest {
 
     private static InvCatalogFactory factory = null;
     private static Logger log = LoggerFactory.getLogger(CatalogTest.class);
 
     @BeforeClass
     public static void setUpClass() throws Exception {
         factory = new InvCatalogFactory("testFactory", true);
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
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
         File f = File.createTempFile("catalog_new", "xml");
         f.deleteOnExit();
         URI uri = f.toURI();
         InvCatalogImpl impl = new InvCatalogImpl("Test Catalog", "1.0", uri);
         FileOutputStream fos = new FileOutputStream(f);
         impl.writeXML(fos);
         assertTrue(f.exists());
 
     }
     
     @Test
     public void testDataset() throws URISyntaxException, IOException {
         CatalogHelper.setupCatalog(new File("/tmp/catalog.xml"), "testcat");
         InvCatalog readCatalog = CatalogHelper.readCatalog(new URI("file:///tmp/catalog.xml"));
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
         BufferedReader buf = new BufferedReader(new FileReader("/tmp/catalog.xml"));
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
