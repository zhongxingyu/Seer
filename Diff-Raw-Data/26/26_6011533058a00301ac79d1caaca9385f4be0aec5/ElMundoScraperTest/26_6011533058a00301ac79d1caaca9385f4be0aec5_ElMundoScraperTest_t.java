 package net.pdp7.tvguide.scraper.es.elmundo;
 
 
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 
 import net.pdp7.tvguide.dao.EpgDao;
 import net.pdp7.tvguide.scraper.es.elmundo.ElMundoScraper;
 import net.pdp7.tvguide.test.TestUtils;
 import junit.framework.TestCase;
 
 public class ElMundoScraperTest extends TestCase {
 
 	public void test() throws Exception {
 		TestUtils.logging();
		new ElMundoScraper(new EpgDao(new SimpleJdbcTemplate(TestUtils.testDataSource())))
			.scrapeChannel(1);
//			.scrapeAll();
		
 	}
 	
 }
