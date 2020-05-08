 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.clinicalsummary.db.hibernate;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import org.openmrs.Cohort;
 import org.openmrs.Concept;
 import org.openmrs.Encounter;
 import org.openmrs.EncounterType;
 import org.openmrs.Location;
 import org.openmrs.Obs;
 import org.openmrs.Patient;
 import org.openmrs.api.db.DAOException;
 import org.openmrs.module.clinicalsummary.SummaryError;
 import org.openmrs.module.clinicalsummary.SummaryIndex;
 import org.openmrs.module.clinicalsummary.SummaryTemplate;
 import org.openmrs.module.clinicalsummary.db.SummaryDAO;
 
 /**
  * Hibernate operation from the summary module
  */
 public class HibernateSummaryDAO implements SummaryDAO {
 	
 	public SessionFactory sessionFactory;
 	
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 	
 	/**
 	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#saveTemplate(org.openmrs.module.clinicalsummary.SummaryTemplate)
 	 */
 	public SummaryTemplate saveTemplate(SummaryTemplate summary) throws DAOException {
 		if (summary.isPreferred()) {
 			String stringQuery = "UPDATE SummaryTemplate s SET s.preferred = :preferred";
 			sessionFactory.getCurrentSession().createQuery(stringQuery).setBoolean("preferred", false).executeUpdate();
 		}
 		sessionFactory.getCurrentSession().saveOrUpdate(summary);
 		return summary;
 	}
 	
 	/**
 	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#saveTemplate(org.openmrs.module.clinicalsummary.SummaryTemplate)
 	 */
 	public SummaryTemplate retireTemplate(SummaryTemplate summary) throws DAOException {
 		sessionFactory.getCurrentSession().saveOrUpdate(summary);
 		return summary;
 	}
 	
 	/**
 	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getTemplate(java.lang.Integer)
 	 */
 	public SummaryTemplate getTemplate(Integer id) throws DAOException {
 		return (SummaryTemplate) sessionFactory.getCurrentSession().get(SummaryTemplate.class, id);
 	}
 	
 	/**
 	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getClinicalSummaries()
 	 */
 	@SuppressWarnings("unchecked")
 	public List<SummaryTemplate> getAllTemplates(boolean includeRetired) throws DAOException {
 		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(SummaryTemplate.class);
 		criteria.add(Restrictions.eq("retired", includeRetired));
 		return criteria.list();
 	}
 	
 	/**
 	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getPatientsEncountersByTypes(org.openmrs.Cohort, java.util.List)
 	 */
 	public Encounter getLatestEncounter(Patient patient) throws DAOException {
 		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Encounter.class);
 		
 		criteria.createAlias("patient", "patient");
 		criteria.add(Restrictions.eq("patient.patientId", patient.getPatientId()));
 		
 		criteria.add(Restrictions.eq("voided", false));
 		
 		return (Encounter) criteria.uniqueResult();
 	}
 	
 	/**
 	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getPatientsEncountersByTypes(org.openmrs.Cohort, java.util.List)
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Encounter> getEncounters(Cohort cohort, Collection<EncounterType> encounterTypes) throws DAOException {
 		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Encounter.class);
 		
 		criteria.createAlias("patient", "patient");
 		criteria.add(Restrictions.in("patient.patientId", cohort.getMemberIds()));
 		
 		criteria.add(Restrictions.eq("voided", false));
 		criteria.add(Restrictions.in("encounterType", encounterTypes));
 		
 		criteria.addOrder(Order.desc("patient.patientId"));
 		criteria.addOrder(Order.desc("encounterId"));
 		criteria.addOrder(Order.desc("encounterDatetime"));
 		
 		return criteria.list();
 	}
 	
 	/**
 	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getObservationsByEncounterType(org.openmrs.Cohort, org.openmrs.Concept, List)
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Obs> getObservations(Cohort cohort, Concept concept, Collection<EncounterType> encounterTypes) {
 		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
 		criteria.createAlias("person", "person");
 		
 		
 		if (!encounterTypes.isEmpty()) {
 			criteria.createAlias("encounter", "encounter");
 			criteria.add(Restrictions.in("encounter.encounterType", encounterTypes));
 		}
 		
 		criteria.add(Restrictions.in("person.personId", cohort.getMemberIds()));
 		criteria.add(Restrictions.eq("concept", concept));
 		criteria.add(Restrictions.eq("voided", false));
 		
 		criteria.addOrder(Order.desc("person.personId"));
 		criteria.addOrder(Order.desc("obsDatetime"));
 		return criteria.list();
 	}
 	
 	/**
 	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getPatientsByLocation(org.openmrs.Location, java.util.Date, java.util.Date)
 	 */
 	public Cohort getPatientsByLocation(Location location, Date startDate, Date endDate) {
 		
 		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
 		criteria.createAlias("encounter", "encounter");
 		criteria.createAlias("person", "person");
 		
 		if (location != null)
 			criteria.add(Restrictions.eq("encounter.location", location));
 		else
 			criteria.add(Restrictions.isNull("encounter.location"));
 		
 		if (startDate != null)
 			criteria.add(Restrictions.ge("dateCreated", startDate));
 		
 		if (endDate != null)
 			criteria.add(Restrictions.le("dateCreated", endDate));
 		
 		criteria.add(Restrictions.eq("voided", false));
 		criteria.addOrder(Order.desc("person.personId"));
 		
 		criteria.setProjection(Projections.property("person.personId"));
 		return new Cohort(criteria.list());
 	}
 	
 	/**
 	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#saveError(org.openmrs.module.clinicalsummary.SummaryError)
 	 */
 	public SummaryError saveError(SummaryError summaryError) throws DAOException {
 		sessionFactory.getCurrentSession().saveOrUpdate(summaryError);
 		return summaryError;
 	}
 	
 	/**
 	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getAllErrors()
 	 */
 	@SuppressWarnings("unchecked")
 	public List<SummaryError> getAllErrors() {
 		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(SummaryError.class);
 		return criteria.list();
 	}
 	
 	/**
 	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#deleteError(org.openmrs.module.clinicalsummary.SummaryError)
 	 */
 	public void deleteError(SummaryError summaryError) {
 		Query query = sessionFactory.getCurrentSession().createQuery("delete from SummaryError where summaryErrorId = :id");
 		query.setInteger("id", summaryError.getSummaryErrorId());
 		query.executeUpdate();
 	}
 	
 	/**
 	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#saveSummaryIndex(org.openmrs.module.clinicalsummary.SummaryIndex)
 	 */
 	public SummaryIndex saveSummaryIndex(SummaryIndex summaryIndex) throws DAOException {
 		sessionFactory.getCurrentSession().saveOrUpdate(summaryIndex);
 		return summaryIndex;
 	}
 	
 	/**
 	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getAllIndexes()
 	 */
 	@SuppressWarnings("unchecked")
 	public List<SummaryIndex> getAllIndexes() {
 		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(SummaryIndex.class);
 		return criteria.list();
 	}
 
 	/**
 	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getEarliestIndex(org.openmrs.Location)
 	 */
 	@Override
     public Date getEarliestIndex(Location location) {
 		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(SummaryIndex.class);
 		
 		if (location != null)
 			criteria.add(Restrictions.eq("location", location));
 		else
 			criteria.add(Restrictions.isNull("location"));
 		
 		criteria.setProjection(Projections.min("initialDate"));
 		return (Date) criteria.uniqueResult();
     }
 
 	/**
 	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#updateIndexesInitialDate(org.openmrs.Location, java.util.Date)
 	 */
 	@Override
 	public Integer updateIndexesInitialDate(Location location, Date initialDate) {
 		String hqlString = "UPDATE SummaryIndex i SET i.initialDate = :initialDate WHERE i.location = :location";
 		int totalUpdated = sessionFactory.getCurrentSession().createQuery(hqlString).setDate("initialDate", initialDate)
 		        .setParameter("location", location).executeUpdate();
 		return totalUpdated;
 	}
 	
 	/**
 	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getIndexByPatient(org.openmrs.Patient)
 	 */
 	public SummaryIndex getIndex(Patient patient, SummaryTemplate template) {
 		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(SummaryIndex.class);
 		criteria.add(Restrictions.eq("patient", patient));
 		criteria.add(Restrictions.eq("template", template));
 		return (SummaryIndex) criteria.uniqueResult();
 	}
 	
 	/**
 	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getIndexByPatient(org.openmrs.Patient)
 	 */
 	public SummaryIndex getIndex(Integer indexId) {
 		return (SummaryIndex) sessionFactory.getCurrentSession().get(SummaryIndex.class, indexId);
 	}
 	
 	/**
 	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getIndexes(org.openmrs.Location,
 	 *      org.openmrs.module.clinicalsummary.SummaryTemplate, java.util.Date, java.util.Date,
 	 *      java.util.Date, java.util.Date)
 	 */
 	@SuppressWarnings("unchecked")
 	public List<SummaryIndex> getIndexes(Location location, SummaryTemplate template, Date startReturnDate, Date endReturnDate) throws DAOException {
 		
 		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(SummaryIndex.class);
 		criteria.createAlias("patient", "patient");
 		
 		if (location != null)
 			criteria.add(Restrictions.eq("location", location));
 		else
 			criteria.add(Restrictions.isNull("location"));
 		
 		if (template != null)
 			criteria.add(Restrictions.eq("template", template));
 		
 		if (startReturnDate != null)
 			criteria.add(Restrictions.ge("returnDate", startReturnDate));
 		
 		if (endReturnDate != null)
 			criteria.add(Restrictions.le("returnDate", endReturnDate));
 		
 		Date earliestDate = getEarliestIndex(location);
		criteria.add(Restrictions.ge("dateGenerated", earliestDate));
 		
 		criteria.addOrder(Order.desc("returnDate"));
 		criteria.addOrder(Order.desc("patient.patientId"));
 		
 		return criteria.list();
 	}
 	
 	/*
 	 * Two methods for the Datatables plugin 
 	 */
 
 	/**
 	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#findIndexes(java.lang.String, java.lang.String[], int, int)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<SummaryIndex> findIndexes(String search, String sortOrder, int sortColumn, int displayStart, int displayLength) {
 		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(SummaryIndex.class);
 		IndexFinder finder = new IndexFinder(criteria);
 		finder.setSearch(search);
 		finder.setOrdering(sortOrder, sortColumn);
 		finder.setRecord(displayStart, displayLength);
 	    return criteria.list();
 	}
 	
 	/**
 	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#countIndexes(java.lang.String, java.lang.String[])
 	 */
 	public Integer countIndexes(String search) {
 		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(SummaryIndex.class);
 		IndexFinder finder = new IndexFinder(criteria);
 		finder.setSearch(search);
 		criteria.setProjection(Projections.rowCount());
 	    return (Integer) criteria.uniqueResult();
     }
 }
