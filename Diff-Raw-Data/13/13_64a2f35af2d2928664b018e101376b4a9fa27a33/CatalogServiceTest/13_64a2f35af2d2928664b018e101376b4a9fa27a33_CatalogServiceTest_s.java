 package org.emsionline.emsiweb.service.orderform;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import org.emsionline.emsiweb.domain.orderform.CatalogCategory;
 import org.emsionline.emsiweb.domain.orderform.CatalogItem;
 import org.emsionline.emsiweb.domain.orderform.CatalogType;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"/test-root-context.xml"})
 public class CatalogServiceTest {
 	
 	final static Logger logger = LoggerFactory.getLogger(CatalogServiceTest.class);
 	
 	@Autowired
 	private CatalogService catalogService;
 	
 	@Test
 	public void testRetrieveAllCatalogItems() {
 		List<CatalogItem> list = catalogService.findAllCatalogItems();
 		assertNotNull(list);
 		assertTrue(list.size() > 0);
 	}
 	
 	@Test
 	public void testRetrieveAllCatalogTypes() {
 		List<CatalogType> list = catalogService.findAllCatalogTypes();
 		assertTrue(list.size() > 0);
 	}
 	
 	@Test
 	public void testRetrieveAllCategories() {
 		List<CatalogCategory> list = catalogService.findAllCategories();
 		assertTrue(list.size() > 0);
 	}
 	
 	@Test
 	public void testRetrieveCategoriesByCatalogType() {
		List<CatalogCategory> list = catalogService.findCategoriesByCatalogType("BOOK");
 		assertTrue(list.size() > 0);
 	}
 	
 	@Test
 	public void testRetrieveCatalogItemsByCategoryCode() {
		List<CatalogItem> list = catalogService.findCatalogItemsByCategoryCode("1A");
 		assertTrue(list.size() > 0);
 	}
 }
