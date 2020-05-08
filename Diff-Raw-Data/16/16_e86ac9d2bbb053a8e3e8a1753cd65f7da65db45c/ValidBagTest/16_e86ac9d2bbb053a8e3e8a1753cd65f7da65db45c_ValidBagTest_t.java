 package info.freelibrary.bagit;
 
 import static org.junit.Assert.*;
 
import info.freelibrary.util.FileUtils;

import java.io.File;
 import java.io.IOException;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ValidBagTest {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(ValidBagTest.class);
 	
 	private static final String WORK_DIR = "src/test/resources/bagTests";
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		System.setProperty(Bag.WORK_DIR, WORK_DIR);
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
		FileUtils.delete(new File(WORK_DIR));
 	}
 
 	@Test
 	public void testValidBag() {
 		try {
 			Bag bag = new Bag("papas_got_a_brand_new").complete();
 			ValidBag validBag = new BagValidator().validate(bag);
 			
 			validBag.toDir();
 		}
 		catch (IOException details) {
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.debug(details.getMessage(), details);
 			}
 			
 			fail(details.getMessage());
 		}
 		catch (BagException details) {
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.debug(details.getMessage(), details);
 			}
 			
 			fail(details.getMessage());
 		}
 	}
 
 }
