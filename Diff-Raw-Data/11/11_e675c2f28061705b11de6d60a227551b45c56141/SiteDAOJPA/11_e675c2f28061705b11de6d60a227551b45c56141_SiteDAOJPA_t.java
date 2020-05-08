 package com.test.dao.jpa;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.test.dao.SiteDAO;
 import com.test.domain.Site;
 
 public class SiteDAOJPA implements SiteDAO {
 
 	//should be threadsafe but available only in managed environment, e.g.: spring
	@PersistenceContext(name="iur")
 	EntityManager entityManager;
 	
 	@Override
 	public Site readSiteBySiteId(int siteId) {
 		
 		Site site = entityManager.find(Site.class, siteId);
 
		entityManager.merge(site);

 		return site;
 	}
 	
 	@Override
 	public Site readSiteBySiteCode(String siteCode) {
 		
 		Query q = entityManager.createNamedQuery("Site.readSiteCode", Site.class);
 		q.setParameter("siteCode", siteCode);
 		
 		Site site = (Site)q.getSingleResult();
 		
 		return site;
 		
 	}
 	
 	@Override
 	@Transactional
 	public void updateSite(Site site) {
 		entityManager.merge(site);
 	}
 
 }
