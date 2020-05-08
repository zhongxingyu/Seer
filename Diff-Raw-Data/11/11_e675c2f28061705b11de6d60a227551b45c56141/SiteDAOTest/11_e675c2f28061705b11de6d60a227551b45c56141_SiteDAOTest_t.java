 package com.test.dao;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.PersistenceUnit;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.test.domain.Site;
 
 import junit.framework.TestCase;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations={"classpath:app.xml"})
 public class SiteDAOTest extends TestCase {
 
 	private Logger logger = LoggerFactory.getLogger(getClass());
 
 	@PersistenceUnit(name="test")
 	EntityManagerFactory entityManagerFactory;
 	
 	@Autowired
 	private SiteDAO siteDAO;
 	
 	@Test
 	public void test () {
 		EntityManager entityManager = entityManagerFactory.createEntityManager();
 		Site site = entityManager.find(Site.class, 1);
 
 		entityManager.getTransaction().begin();
 		int ltl = site.getLoginTimeLimit()+1;
 		site.setLoginTimeLimit(ltl);
 
 		entityManager.merge(site);
 		entityManager.getTransaction().commit();
 
 		logger.info("Site: {}", site.toString());
 
 	}
 	
 	@Test
 	public void testReadSiteBySiteId() {
 		Site site = siteDAO.readSiteBySiteId(1);
 		siteDAO.readSiteBySiteId(1);
 		
 		assertNotNull("Site not found", site);
 		
 		logger.debug("Site: {}", site.toString());
 
 		assertTrue("Site is not cached", entityManagerFactory.getCache().contains(Site.class, site.getSiteId()));
 		
 		assertEquals("Site id is invalid", site.getSiteId(), 1);
 		
 	}
 	
 	@Test
 	public void testUpdateSite() {
 		Site site = siteDAO.readSiteBySiteId(1);
 
 		int ltl = site.getLoginTimeLimit()+1;
 		
 		site.setLoginTimeLimit(ltl);
 
 		siteDAO.updateSite(site);
 		
 		site = siteDAO.readSiteBySiteId(1);
 		logger.debug("Site: {}", site.toString());
 		
 		assertEquals(ltl, site.getLoginTimeLimit());
 		
 	}
 	
 	@Test
 	public void testReadSiteBySiteCode() {
 		Site site = siteDAO.readSiteBySiteCode("UK.EN.TST");
 		logger.debug("Site: {}"+ site.toString());
 		assertEquals(site.getSiteCode(), "UK.EN.TST");
 	}
 	
 }
