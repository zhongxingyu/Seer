 package org.emsionline.emsiweb.service;
 
import java.util.Map;
 
 import org.emsionline.emsiweb.domain.Church;
 import org.emsionline.emsiweb.domain.ChurchOrg;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "/test-root-context.xml" })
 public class ChurchServiceTest {
 
 	final static Logger logger = LoggerFactory.getLogger(ChurchServiceTest.class);
 
 	
 	@Autowired
 	private ChurchService churchService;
 
 	@Test
 	public void testRetrieve() {
 		Church church = churchService.findById(new Long(1));
 
 		assertEquals("brooklyn", church.getChurchPath());
 		
 		logger.info("church path: " + church.getChurchPath());
 		/*
 		Map<String, LocalizedChurchDetails> details = church.getChurchDetails();
 		logger.info("church details size: " + details.size());
 		
 		for (String locale : details.keySet()) {
 			logger.info("  [" + locale + "] " + details.get(locale).getChurchId() + " - " + details.get(locale).getChurchName() );
 		}
 		
 
 		//church = churchService.findById(new Long(4));
 
 		//assertEquals("rotterdam", church.getChurchPath());
 		*/
 		 
 	}
 	
 	@Ignore
 	@Test
 	public void testRetrieveOrg() {
 		Church church = churchService.findById(new Long(1));
 
 		assertEquals("towaco", church.getChurchPath());
 
 		ChurchOrg org = church.getChurchOrg();
 		
 		assertNotNull(org);
 		assertEquals("america-east", org.getChurchOrgPath());
 	}
 	
 	@Ignore
 	@Test
 	public void testModify() {
 		Church church = churchService.findById(new Long(1));
 
 		assertEquals("towaco", church.getChurchPath());
 		
 		logger.info("church path: " + church.getChurchPath());
 		/*
 		Map<String, LocalizedChurchDetails> details = church.getChurchDetails();
 		logger.info("church details size: " + details.size());
 		
 		for (String locale : details.keySet()) {
 			logger.info("  [" + locale + "] " + details.get(locale).getChurchId() + " - " + details.get(locale).getChurchName() );
 		}
 				
 		//churchService.save(church);
 		
 		Church church2 = churchService.findById(new Long(1));
 		logger.info("church path: " + church2.getChurchPath());
 
 		//church = churchService.findById(new Long(4));
 
 		//assertEquals("rotterdam", church.getChurchPath());
 		 
 		 */
 	}
 
 }
