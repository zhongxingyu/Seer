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
 
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.time.DateUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.Criteria;
 import org.hibernate.ScrollMode;
 import org.hibernate.ScrollableResults;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import org.openmrs.Cohort;
 import org.openmrs.Encounter;
 import org.openmrs.Location;
 import org.openmrs.Obs;
 import org.openmrs.OpenmrsObject;
 import org.openmrs.api.db.DAOException;
 import org.openmrs.module.clinicalsummary.cache.CacheUtils;
 import org.openmrs.module.clinicalsummary.db.CoreDAO;
 import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
 import org.openmrs.module.clinicalsummary.util.FetchOrdering;
 import org.openmrs.module.clinicalsummary.util.FetchRestriction;
 import org.openmrs.util.OpenmrsUtil;
 
 /**
  *
  */
 public class HibernateCoreDAO implements CoreDAO {
 
 	private Log log = LogFactory.getLog(HibernateCoreDAO.class);
 
 	private SessionFactory sessionFactory;
 
 	/**
 	 * Method that will be called by Spring to inject the Hibernate's SessionFactory.
 	 *
 	 * @param sessionFactory the session factory to be injected
 	 */
 	public void setSessionFactory(final SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 
 	/**
 	 * @see CoreDAO#getPatientObservations(Integer, java.util.Map, org.openmrs.module.clinicalsummary.util.FetchRestriction)
 	 */
 	public List<Obs> getPatientObservations(final Integer patientId, final Map<String, Collection<OpenmrsObject>> restrictions,
 	                                        final FetchRestriction fetchRestriction) throws DAOException {
 		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
 		criteria.add(Restrictions.eq("personId", patientId));
 
 		criteria.setFirstResult(0);
 		if (fetchRestriction.getStart() != null)
 			criteria.setFirstResult(fetchRestriction.getStart());
 
 		if (fetchRestriction.getSize() != null && fetchRestriction.getSize() == 1)
 			criteria.setMaxResults(fetchRestriction.getSize());
 
 		Order order = Order.desc("obsDatetime");
 		if (OpenmrsUtil.nullSafeEquals(FetchOrdering.ORDER_ASCENDING, fetchRestriction.getFetchOrdering()))
 			order = Order.asc("obsDatetime");
 
 		criteria.addOrder(order);
 
 		Obs observation = new Obs();
 		for (String property : restrictions.keySet()) {
 			Collection<OpenmrsObject> propertyValues = restrictions.get(property);
 			if (CollectionUtils.isNotEmpty(propertyValues) && PropertyUtils.isReadable(observation, property)) {
 				criteria.add(Restrictions.in(property, propertyValues));
 				criteria.addOrder(Order.asc(property));
 			}
 		}
 
 		criteria.add(Restrictions.eq("voided", Boolean.FALSE));
 
 		List<Obs> observations = new ArrayList<Obs>();
 
 		// TODO: further optimization would be adding start date and end date parameter in the obs restrictions
 		ScrollableResults scrollableResults = criteria.scroll(ScrollMode.FORWARD_ONLY);
 
 		Integer size = fetchRestriction.getSize();
 
 		// scroll the results
 		Obs processedObs = null;
 		while (scrollableResults.next() && OpenmrsUtil.compareWithNullAsGreatest(observations.size(), size) == -1) {
 			Obs obs = (Obs) scrollableResults.get(0);
 			// TODO: thanks to Ampath for the duplicate data, we need to sanitize the query results here
 			if (processedObs != null && !obs.isObsGrouping()) {
 				if (DateUtils.isSameDay(processedObs.getObsDatetime(), obs.getObsDatetime())
 						&& OpenmrsUtil.nullSafeEquals(processedObs.getConcept(), obs.getConcept())
 						&& OpenmrsUtil.nullSafeEquals(processedObs.getValueCoded(), obs.getValueCoded())
 						&& (OpenmrsUtil.nullSafeEquals(processedObs.getValueNumeric(), obs.getValueNumeric())))
 					continue;
 			}
 			processedObs = obs;
 			observations.add(obs);
 		}
 		scrollableResults.close();
 		return observations;
 	}
 
 	/**
 	 * @see CoreDAO#getPatientEncounters(Integer, java.util.Map, org.openmrs.module.clinicalsummary.util.FetchRestriction)
 	 */
 	public List<Encounter> getPatientEncounters(final Integer patientId, final Map<String, Collection<OpenmrsObject>> restrictions,
 	                                            final FetchRestriction fetchRestriction) throws DAOException {
 		// create a hibernate criteria on the encounter object
 		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Encounter.class);
 		// restrict the encounter that will be returned to specific patient only
 		criteria.add(Restrictions.eq("patientId", patientId));
 
 		// default ordering for the returned encounter is descending on encounter datetime
 		Order order = Order.desc("encounterDatetime");
 		// change the default ordering if the user pass the ascending ordering in the parameters
 		if (OpenmrsUtil.nullSafeEquals(FetchOrdering.ORDER_ASCENDING, fetchRestriction.getFetchOrdering()))
 			order = Order.asc("encounterDatetime");
 		// add the ordering object to the criteria
 		criteria.addOrder(order);
 
 		// always get from the first result
 		criteria.setFirstResult(0);
 		// set the first result to a number if the user pass any value on this parameter (currently not being used)
 		if (fetchRestriction.getStart() != null)
 			criteria.setFirstResult(fetchRestriction.getStart());
 
 		// specify how many records should be returned
 		if (fetchRestriction.getSize() != null)
 			criteria.setMaxResults(fetchRestriction.getSize());
 
 		// create a dummy encounter object
 		Encounter encounter = new Encounter();
 		// iterate over each property in the restriction map
 		for (String property : restrictions.keySet()) {
 			// get the actual object that will restrict the encounter. this will contains the list of encounter type or list of location
 			// or list of provider (currently not being used) passed from caller
 			Collection<OpenmrsObject> propertyValues = restrictions.get(property);
 			// check to make sure the list is not empty and the property is readable. example of the property for encounter are
 			// encounterType or location of the encounter
 			if (CollectionUtils.isNotEmpty(propertyValues) && PropertyUtils.isReadable(encounter, property)) {
 				// create a restriction on the property with the above list as the value
 				criteria.add(Restrictions.in(property, propertyValues));
 				// add ordering on that property to prevent slowness when ordering only on encounter datetime (1.6.x only)
 				criteria.addOrder(Order.asc(property));
 			}
 		}
 
 		// exclude all voided encounters
 		criteria.add(Restrictions.eq("voided", Boolean.FALSE));
 
 		List<Encounter> encounters = new ArrayList<Encounter>();
 
 		// scroll the results and add them to the above list of encounter
 		Integer counter = 0;
 		ScrollableResults scrollableResults = criteria.scroll(ScrollMode.FORWARD_ONLY);
 		while (scrollableResults.next())
 			encounters.add((Encounter) scrollableResults.get(0));
 		scrollableResults.close();
 		return encounters;
 	}
 
 	/**
 	 * @see CoreDAO#getCohort(org.openmrs.Location, java.util.Date, java.util.Date)
 	 */
 	public Cohort getCohort(final Location location, final Date startDate, final Date endDate) throws DAOException {
 		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
 
 		if (location != null)
 			criteria.add(Restrictions.eq("location", location));
 		else
 			criteria.add(Restrictions.isNull("location"));
 
 		if (startDate != null)
 			criteria.add(Restrictions.ge("dateCreated", startDate));
 
 		if (endDate != null)
 			criteria.add(Restrictions.le("dateCreated", endDate));
 
 		criteria.setProjection(Projections.property("person.personId"));
 		criteria.add(Restrictions.eq("voided", Boolean.FALSE));
 		return new Cohort(criteria.list());
 	}
 
 	/**
 	 * @see CoreDAO#getCohort(org.openmrs.Location, java.util.Date, java.util.Date)
 	 */
 	public Cohort getReturnDateCohort(final Date startDate, final Date endDate) throws DAOException {
 		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
 		criteria.add(Restrictions.eq("concept", CacheUtils.getConcept(EvaluableNameConstants.RETURN_VISIT_DATE)));
 
 		if (startDate != null)
			criteria.add(Restrictions.ge("valueDatetime", startDate));
 
 		if (endDate != null)
			criteria.add(Restrictions.le("valueDatetime", endDate));
 
 		criteria.setProjection(Projections.property("person.personId"));
 		criteria.add(Restrictions.eq("voided", Boolean.FALSE));
 		return new Cohort(criteria.list());
 	}
 }
