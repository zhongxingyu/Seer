 package info.freelibrary.bagit;
 
 import static org.junit.Assert.fail;
 
 import info.freelibrary.util.FileUtils;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.junit.Test;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 
 /**
  * These tests mostly just to test the examples' syntax; testing functionality
  * is done by the regular unit tests.
  * 
  * @author Kevin S. Clarke <ksclarke@gmail.com>
  * 
  */
 public class ExamplesTest {
 
 	private static final String WORK_DIR = "src/test/resources/bagTests";
 	private static final String DATA_DIR = "src/test/resources/bags/dryad_630/data/";
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		System.setProperty(Bag.WORK_DIR, WORK_DIR);
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 		FileUtils.delete(new File(WORK_DIR));
 	}
 
 	@SuppressWarnings("unused")
 	@Test
 	public void testCreateExamples() {
 		try {
 			Bag bag1 = new Bag("new_bag");
 			Bag bag2 = new Bag("src/test/resources/bags/dryad_630");
 			Bag bag3 = new Bag("src/test/resources/bags/dryad_630.tar.gz");
 		}
 		catch (IOException details) {
 			fail(details.getMessage());
 		}
 	}
 	
	@Test
 	public void testAddMetadata() {
 		try {
 			Bag bag = new Bag("new_bag_metadata");
 			BagInfo bagInfo = bag.getBagInfo();
 
 			bagInfo.addMetadata(BagInfo.CONTACT_NAME_TAG, "Kevin S. Clarke");
 			bagInfo.addMetadata(BagInfo.CONTACT_PHONE_TAG, "ksclarke@gmail.com");
 			bagInfo.addMetadata("Alt-Email", "thetrashcan@gmail.com");
 		}
 		catch (IOException details) {
 			fail(details.getMessage());
 		}
 	}
 
 	@Test
 	public void testAddMetadataProperties() {
 		try {
 			Properties metadata = new Properties();
 			File properties = new File(
 					"src/test/resources/files/test.properties");
 			try {
 				metadata.load(new FileReader(properties));
 			}
 			catch (IOException details) {
 				fail(details.getMessage());
 			}
 
 			@SuppressWarnings("unused")
 			Bag bag = new Bag("bag_for_metadata", metadata);
 		}
 		catch (IOException details) {
 			fail(details.getMessage());
 		}
 	}
 
 	@Test
 	public void testAddData() {
 		try {
 			Bag bag = new Bag("dryad_630");
 			File dataFile = new File(DATA_DIR + "datafile-1/ApineCYTB.nexus");
 			File dataDir = new File(DATA_DIR + "datafile-2/");
 
 			bag.addData(dataFile, dataDir);
 		}
 		catch (IOException details) {
 			fail(details.getMessage());
 		}
 	}
 
 	@Test
 	public void testValidate() {
 		try {
 			Bag bag = new Bag("src/test/resources/bags/dryad_630.tar.gz");
 
 			try {
 				new BagValidator().validate(bag);
 			}
 			catch (BagException details) {
 				switch (details.getReason()) {
 				case BagException.PAYLOAD_MANIFEST_DIFFERS_FROM_DATADIR:
 					fail(details.getMessage());
 					break;
 				default:
 					fail(details.getMessage());
 				}
 			}
 		}
 		catch (IOException details) {
 			fail(details.getMessage());
 		}
 	}
 
 	@Test
 	public void testIsValid() {
 		try {
 			Bag bag = new Bag("src/test/resources/bags/dryad_630.tar.gz");
 
 			if (new BagValidator().isValid(bag)) {
 				// do nothing...
 			}
 			else {
 				fail("Bag isn't valid");
 			}
 		}
 		catch (IOException details) {
 			fail(details.getMessage());
 		}
 	}
 
 	@Test
 	public void testWriteBag() {
 		try {
 			Bag bag = new Bag("src/test/resources/bags/dryad_630");
 
 			try {
 				new BagValidator().validate(bag).toTarBZip2();
 			}
 			catch (BagException details) {
 				fail(details.getMessage());
 			}
 			catch (IOException details) {
 				fail(details.getMessage());
 			}
 		}
 		catch (IOException details) {
 			fail(details.getMessage());
 		}
	}
 }
