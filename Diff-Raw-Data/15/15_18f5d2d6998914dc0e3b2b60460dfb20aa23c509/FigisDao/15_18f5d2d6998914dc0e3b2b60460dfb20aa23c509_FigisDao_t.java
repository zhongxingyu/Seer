 package org.fao.fi.figis.dao;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.NonUniqueResultException;
 import javax.persistence.Query;
 
 import org.fao.fi.dao.Dao;
 import org.fao.fi.figis.domain.Observation;
 import org.fao.fi.figis.domain.ObservationDomain;
 import org.fao.fi.figis.domain.ObservationXml;
 import org.fao.fi.figis.domain.RefVme;
 import org.fao.fi.figis.domain.RefWaterArea;
 import org.fao.fi.figis.domain.VmeObservation;
 import org.fao.fi.figis.domain.VmeObservationDomain;
 import org.fao.fi.figis.domain.VmeObservationPk;
 import org.fao.fi.figis.domain.rule.DomainRule4ObservationXmlId;
 import org.fao.fi.figis.domain.rule.Figis;
 import org.fao.fi.vme.VmeException;
 import org.fao.fi.vme.dao.config.FigisDB;
 
 /**
  * The dao in order to dconnect to the Figis database. Connection details to be found in
  * /vme-configuration/src/main/resources/META_VME-INF/persistence.xml
  * 
  * 
  * 
  * 
  * @author Erik van Ingen
  * 
  */
 
 @Singleton
 public class FigisDao extends Dao {
 
 	// private final static Logger logger = LoggerFactory.getLogger(FigisDao.class);
 	private DomainRule4ObservationXmlId rule = new DomainRule4ObservationXmlId();
 
 	/**
 	 * Hi Fabrizio,
 	 * 
 	 * Water area refs for VME are loaded in RTMS from the Access DB, in the future from iMarine. To be sure that water
 	 * area refs coming from VME, are consistently loaded in devel, fiqa and prod, I propose to have a reserved range
 	 * from 6000-8000.
 	 * 
 	 * select max(cd_water_area) from ref_water_area --5075
 	 * 
 	 * What do you think?
 	 * 
 	 * Kind Regards, Erik
 	 */
 	public static final int START_WATER_AREA_REF = 60000;
 	public static final int END_WATER_AREA_REF = 80000;
 
 	@Inject
 	@FigisDB
 	private EntityManager em;
 
 	public EntityManager getEm() {
 		return em;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<RefVme> loadRefVmes() {
 		return (List<RefVme>) this.generateTypedQuery(em, RefVme.class).getResultList();
 	}
 
 	public List<?> loadObjects(Class<?> clazz) {
 		return this.generateTypedQuery(em, clazz).getResultList();
 	}
 
 	public Object find(Class<?> clazz, Object id) {
 		return em.find(clazz, id);
 	}
 
 	public void merge(Object object) {
 		em.getTransaction().begin();
 		em.merge(object);
 		em.getTransaction().commit();
 	}
 
 	public void persist(Object object) {
 		em.getTransaction().begin();
 		em.persist(object);
 		em.getTransaction().commit();
 	}
 
 	public void remove(Object object) {
 		em.getTransaction().begin();
 		em.remove(object);
 		em.getTransaction().commit();
 	}
 
 	/**
 	 * 
 	 * 
 	 * 
 	 * @param r
 	 *            the object to be synced
 	 */
 	public void syncRefVme(RefVme r) {
 		RefVme found = em.find(RefVme.class, r.getId());
 		if (found == null) {
 			this.persist(r);
 		} else {
 			this.merge(r);
 		}
 	}
 
 	/**
 	 * persists the VmeObservationDomain, assuming the RefVme is pre existing
 	 * 
 	 * @param vod
 	 */
 	public void syncVmeObservationDomain(VmeObservationDomain vod) {
 		// precondition
 
 		if (vod.getRefVme().getId() == null) {
 			throw new VmeException("FigisDao Exception, detected a non registered RefVme.");
 		}
 
 		em.getTransaction().begin();
 
 		// logic
 		List<ObservationDomain> oList = vod.getObservationDomainList();
 		for (ObservationDomain od : oList) {
 
 			// find VmeObservation
 			VmeObservation vo = findVmeObservationByVme(vod.getRefVme().getId(), od.getReportingYear());
 			if (vo == null) {
 				// create VmeObservation plus the derived objects.
 				persistObservationDomain(od, vod.getRefVme().getId());
 			} else {
 				// VmeObservation exists. sync it plus the derived objects.
 				updateObservationDomain(od, vo.getId().getObservationId());
 			}
 
 		}
 
 		em.getTransaction().commit();
 	}
 
 	/**
 	 * The observation may need to be updated or needs to be inserted because it is new.
 	 * 
 	 * 
 	 * @param od
 	 * @param vmeId
 	 */
 	private void updateObservationDomain(ObservationDomain od, Long observationId) {
 
 		Observation o = em.find(Observation.class, observationId);
 
 		if (o == null) {
 			// it can be a new observation, which first needs to be stored.
 			o = new Observation(od);
 			em.persist(o);
 		}
 
 		o.setCollection(od.getCollection());
 		o.setObservationsPerLanguage(od.getObservationsPerLanguage());
 		o.setOrder(od.getOrder());
 
 		List<ObservationXml> xmlList = od.getObservationsPerLanguage();
 		for (ObservationXml xml : xmlList) {
 			xml.setObservation(o);
 			rule.composeId(xml);
 			ObservationXml xmlFound = em.find(ObservationXml.class, xml.getId());
 			if (xmlFound == null) {
 				xml.setObservation(o);
 				// genereate the id for the xml, based upon the id of the observation
 				rule.composeId(xml);
 				em.persist(xml);
 			} else {
 				xmlFound.setCreationDate(xml.getCreationDate());
 				xmlFound.setLanguage(xml.getLanguage());
 				xmlFound.setLastEditDate(xml.getLastEditDate());
 				xmlFound.setObservation(xml.getObservation());
 				xmlFound.setStatus(xml.getStatus());
 				xmlFound.setXml(xml.getXml());
 				em.merge(xmlFound);
 			}
 		}
 		o.setObservationsPerLanguage(xmlList);
 		em.merge(o);
 	}
 
 	/**
 	 * 
 	 * @param od
 	 * @param vmeId
 	 */
 	private void persistObservationDomain(ObservationDomain od, Long vmeId) {
 		// first the observation needs to be persisted in order to get an id
 		Observation o = new Observation(od);
 		em.persist(o);
 		od.setId(o.getId());
 
 		// then a VmeObservation can be persisted.
 		VmeObservation vo = new VmeObservation();
 		VmeObservationPk pk = new VmeObservationPk();
 		vo.setId(pk);
 		pk.setObservationId(o.getId());
 		pk.setVmeId(vmeId);
 		pk.setReportingYear(od.getReportingYear());
 		em.persist(vo);
 
 		List<ObservationXml> xmlList = od.getObservationsPerLanguage();
 		for (ObservationXml observationXml : xmlList) {
 			observationXml.setObservation(o);
 			// genereate the id for the xml, based upon the id of the observation
 			rule.composeId(observationXml);
 			em.persist(observationXml);
 		}
 		o.setObservationsPerLanguage(xmlList);
 		em.merge(o);
 
 	}
 
 	public Long count(Class<?> clazz) {
 		return count(em, clazz);
 	}
 
 	/**
 	 * find the VmeObservation by the vme id and reporting year.
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public VmeObservation findVmeObservationByVme(Long vmeId, String reportingYear) {
 		Query query = em
 				.createQuery("select vo from VmeObservation vo where vo.id.vmeId = :vmeId and vo.id.reportingYear = :reportingYear");
 		query.setParameter("vmeId", vmeId);
 		query.setParameter("reportingYear", reportingYear);
 		VmeObservation vo = null;
 		try {
 			vo = (VmeObservation) query.getSingleResult();
 		} catch (NoResultException e) {
 			// is valid, a new object needs to be created.
 		}
 		return vo;
 	}
 
 	/**
 	 * find the VmeObservation by the vme id and reporting year.
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public VmeObservation findFirstVmeObservation(Long vmeId, String year) {
 		Query query = em
				.createQuery("select vo from VmeObservation vo where vo.id.vmeId = :vmeId and vo.id.reportingYear <= :year order by vo.id.reportingYear desc");
 		query.setParameter("vmeId", vmeId);
 		query.setParameter("year", year);
 		VmeObservation vo = null;
 		try {
 			List<?> list = query.getResultList();
 			if (list.size()>0){
 				vo = (VmeObservation) list.get(0);
 			}
 		} catch (NoResultException e) {
 			// is valid, a new object needs to be created.
 		}
 		return vo;
 	}
 	
 	/**
 	 * find the VmeObservation by the vme id
 	 * 
 	 * Wrong, you can find more VmeObservation by the vmeId
 	 * 
 	 * 
 	 * 
 	 * 
 	 * @param id
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public List<VmeObservation> findVmeObservationByVme(Long vmeId) {
 		Query query = em.createQuery("select vo from VmeObservation vo where vo.id.vmeId = :vmeId ");
 		query.setParameter("vmeId", vmeId);
 		List<VmeObservation> r = null;
 		try {
 			r = query.getResultList();
 		} catch (NoResultException e) {
 			// is valid, a new object needs to be created.
 		}
 
 		return r;
 	}
 
 	public VmeObservationDomain findVod(Long vmeId) {
 		VmeObservationDomain vod = new VmeObservationDomain();
 		List<ObservationDomain> vodList = new ArrayList<ObservationDomain>();
 		List<VmeObservation> VmeObservationList = findVmeObservationByVme(vmeId);
 		for (VmeObservation vmeObservation : VmeObservationList) {
 			Observation observation = em.find(Observation.class, vmeObservation.getId().getObservationId());
 			String reportingYear = vmeObservation.getId().getReportingYear();
 			ObservationXml xml = findEnglishXml(vmeObservation.getId().getObservationId());
 			List<ObservationXml> observationsPerLanguage = new ArrayList<ObservationXml>();
 			observationsPerLanguage.add(xml);
 			ObservationDomain od = new ObservationDomain(observation, reportingYear, observationsPerLanguage);
 			vodList.add(od);
 		}
 		RefVme refVme = em.find(RefVme.class, vmeId);
 		vod.setRefVme(refVme);
 		vod.setObservationDomainList(vodList);
 		return vod;
 	}
 
 	/**
 	 * This one removes the whole vme, except for the reference data.
 	 * 
 	 * This only works that a Vme in FIGIS consists of a VmeObservation, one or more Observation(s) and one or more
 	 * ObaservationXml(s)
 	 * 
 	 * 
 	 * @param vmeId
 	 */
 	public void removeVme(Long vmeId) {
 		em.getTransaction().begin();
 		List<VmeObservation> voList = findVmeObservationByVme(vmeId);
 		for (VmeObservation vo : voList) {
 			Observation o = (Observation) this.find(Observation.class, vo.getId().getObservationId());
 			ObservationXml xmlFound = findEnglishXml(o.getId());
 			if (xmlFound != null) {
 				em.remove(xmlFound);
 				em.remove(o);
 				em.remove(vo);
 			}
 		}
 		em.getTransaction().commit();
 
 	}
 
 	private ObservationXml findEnglishXml(Long id) {
 		DomainRule4ObservationXmlId rule = new DomainRule4ObservationXmlId();
 		String xmlId = rule.composeId(id, Figis.EN);
 		ObservationXml xmlFound = (ObservationXml) this.find(ObservationXml.class, xmlId);
 		return xmlFound;
 	}
 
 	public void syncRefWaterArea(String externalId, RefWaterArea refWaterArea) {
 
 		em.getTransaction().begin();
 
 		String queryString2 = " select r from RefWaterArea r where r.externalId = :externalId ";
 		Query query = em.createQuery(queryString2);
 		query.setParameter("externalId", externalId);
 		RefWaterArea found = null;
 		try {
 			found = (RefWaterArea) query.getSingleResult();
 		} catch (NoResultException e) {
 			// is valid, a new object needs to be created.
 		} catch (NonUniqueResultException e) {
 			// elements are not unique!
 			throw new VmeException(externalId + " is not unique, data is not consisten!");
 		}
 		if (found != null) {
 			refWaterArea.setId(found.getId());
 			em.merge(refWaterArea);
 		} else {
 			String queryString1 = " select max(id) from RefWaterArea  where id >= " + START_WATER_AREA_REF
 					+ " and id <= " + END_WATER_AREA_REF;
 			Object object = em.createQuery(queryString1).getSingleResult();
 			if (object == null) {
 				refWaterArea.setId(START_WATER_AREA_REF);
 			} else {
 				long l = ((Long) object).intValue();
 				l++;
 				refWaterArea.setId(l);
 			}
 			em.persist(refWaterArea);
 		}
 		em.getTransaction().commit();
 
 	}
 }
