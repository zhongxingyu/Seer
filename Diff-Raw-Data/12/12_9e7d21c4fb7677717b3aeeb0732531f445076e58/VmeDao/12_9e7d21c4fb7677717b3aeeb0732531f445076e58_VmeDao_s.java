 package org.vme.service.dao.sources.vme;
 
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 
 import org.fao.fi.vme.domain.model.GeneralMeasure;
 import org.fao.fi.vme.domain.model.GeoRef;
 import org.fao.fi.vme.domain.model.InformationSource;
 import org.fao.fi.vme.domain.model.Vme;
 import org.fao.fi.vme.domain.model.extended.FisheryAreasHistory;
 import org.fao.fi.vme.domain.model.extended.VMEsHistory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.vme.service.dao.config.vme.VmeDB;
 import org.vme.service.dao.impl.AbstractJPADao;
 
 /**
  * * The dao in order to dconnect to the vme database. Connection details to be found in
  * /vme-configuration/src/main/resources/META_VME-INF/persistence.xml
  * 
  * @author Erik van Ingen
  * 
  */
 public class VmeDao extends AbstractJPADao {
 	static final private Logger LOG = LoggerFactory.getLogger(VmeDao.class);
 	
 	@Inject
 	@VmeDB
 	private EntityManager em;
 
 	public EntityManager getEm() {
 		return em;
 	}
 
 	public List<Vme> loadVmes() {
 		return (List<Vme>) this.generateTypedQuery(em, Vme.class).getResultList();
 	}
 
 	public List<?> loadObjects(Class<?> clazz) {
 		return this.generateTypedQuery(em, clazz).getResultList();
 	}
 
 	public Vme findVme(Long id) {
 		return em.find(Vme.class, id);
 	}
 
 	public Object persist(Object object) {
 		EntityTransaction et = em.getTransaction();
 		
 		try {
 			et.begin();
 			em.persist(object);
 			em.flush();
 			et.commit();
 			
 			LOG.debug("Object {} has been stored into persistence", object);
 			
 			return object;
 		} catch(Throwable t) {
 			LOG.error("Unable to store object {} into persistence: {} [ {} ]", object, t.getClass().getSimpleName(), t.getMessage());
 
 			et.rollback();
 			
 			return null;
 		}
 	}
 
 	public void remove(Object object) {
 		EntityTransaction et = em.getTransaction();
 		
 		try {
 			et.begin();
 			em.remove(object);
 			et.commit();
 			
 			LOG.debug("Object {} has been removed from persistence", object);
 		} catch(Throwable t) {
 			LOG.error("Unable to remove object {} from persistence: {} [ {} ]", object, t.getClass().getSimpleName(), t.getMessage(), t);
 			
 			et.rollback();
 		}
 	}
 
 	public Object merge(Object object) {
 		EntityTransaction et = em.getTransaction();
 		
 		try {
 			et.begin();
 			em.merge(object);
 			em.flush();
 			et.commit();
 			
 			LOG.debug("Object {} has been merged into persistence", object);
 			
 			return object;
 		} catch(Throwable t) {
 			LOG.error("Unable to merge object {} into persistence: {} [ {} ]", object, t.getClass().getSimpleName(), t.getMessage());
 
 			et.rollback();
 			
 			return null;
 		}
 	}
 
 	public Vme saveVme(Vme vme) {
 		EntityTransaction et = em.getTransaction();
 
 		try {
 			et.begin();
 			
 			for (GeneralMeasure o : vme.getRfmo().getGeneralMeasureList()) {
 				em.persist(o);
 			}
 
 			for (FisheryAreasHistory h : vme.getRfmo().getHasFisheryAreasHistory()) {
 				em.persist(h);
 			}
 
 			for (VMEsHistory h : vme.getRfmo().getHasVmesHistory()) {
 				em.persist(h);
 			}
 
 			for (InformationSource informationSource : vme.getRfmo().getInformationSourceList()) {
 				em.persist(informationSource);
 			}
 
 			em.persist(vme.getRfmo());
 
 			for (GeoRef geoRef : vme.getGeoRefList()) {
 				em.persist(geoRef);
 			}
 
 			em.persist(vme);
 
 			et.commit();
			
			em.flush();
			
 			LOG.debug("Vme {} has been saved into persistence", vme);
 			
 			return vme;
 		} catch(Throwable t) {
 			LOG.error("Unable to save Vme {} into persistence: {} [ {} ]", vme, t.getClass().getSimpleName(), t.getMessage());
 			
 			et.rollback();
 			
 			return null;
 		}
 	}
 
 	public Long count(Class<?> clazz) {
 		return count(em, clazz);
 	}
 }
