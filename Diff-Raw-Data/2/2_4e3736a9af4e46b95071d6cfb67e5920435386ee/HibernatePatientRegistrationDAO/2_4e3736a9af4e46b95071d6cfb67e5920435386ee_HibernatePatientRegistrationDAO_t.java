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
 package org.openmrs.module.patientregistration.service.db;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.CacheMode;
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.openmrs.Cohort;
 import org.openmrs.Concept;
 import org.openmrs.EncounterType;
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.patientregistration.PatientRegistrationConstants;
 import org.openmrs.module.patientregistration.PatientRegistrationGlobalProperties;
 import org.openmrs.module.patientregistration.PatientRegistrationUtil;
 import org.openmrs.module.patientregistration.UserActivity;
 import org.openmrs.module.patientregistration.util.DuplicatePatient;
 
 
 
 /**
  * Core implementation of the DAO
  */
 public class HibernatePatientRegistrationDAO implements PatientRegistrationDAO {
 	
 	protected final Log log = LogFactory.getLog(getClass());
 	//***** PROPERTIES *****
 	
     private SessionFactory sessionFactory;
     
     public void setSessionFactory(SessionFactory sessionFactory) {
         this.sessionFactory = sessionFactory;
     }
 
 	/**
 	 * @see PatientRegistrationDAO#getNumberOfEncountersByAddress(Map, String, EncounterType, Location)
 	 */
 	public Map<String, Integer> getNumberOfRegistrationsByAddress(Map<String, String> filterCriteria, String addressField, EncounterType encounterType, Location location, boolean groupByPatient) {
 		Map<String, Integer> m = new TreeMap<String, Integer>();
 		List<String> keys = PatientRegistrationUtil.getAddressHierarchyValues(addressField, filterCriteria);
 		for (String key : keys) {
 			m.put(key, new Integer(0));
 		}
 		m.put("other", new Integer(0));
 		m.put("unspecified", new Integer(0));
 
 		// First we'll get a Map of encounterId -> patientId, and get a list of all patients to aggregate
 		Map<Integer, Integer> encounterToPatient = getEncounterToPatientMap(encounterType, location);
 		List<Integer> patientIds = new ArrayList<Integer>();
 		if (groupByPatient) {
 			patientIds.addAll(new HashSet<Integer>(encounterToPatient.values()));
 		}
 		else {
 			patientIds.addAll(encounterToPatient.values());
 		}
 		
 		// Now we can get the unique address entries
 		Map<Integer, String> addressValues = getAddressValuesForCohort(new Cohort(encounterToPatient.values()), addressField, filterCriteria);
 		
 		for (Integer patientId : patientIds) {
 			String addressValue = addressValues.get(patientId);
 			if (addressValue != null) {
 				String keyToAdd = StringUtils.isBlank(addressValue) ? "unspecified" : (keys.contains(addressValue) ? addressValue : "other");
 				m.put(keyToAdd, m.get(keyToAdd) + 1);
 			}
 		}
 		
 		return m;
 	}
 	
 	/**
 	 * @return a Map of encounterId to patientId for the matching encounterType and location.
 	 * If encounterType or location are null, it will restrict to the supported encounter types and locations if defined
 	 */
 	public Map<Integer, Integer> getEncounterToPatientMap(EncounterType encounterType, Location location) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("select e.encounter_id, p.patient_id from encounter e, patient p ");
 		sb.append("where e.patient_id = p.patient_id and e.voided = 0 and p.voided = 0 ");
 		if (encounterType != null) {
 			sb.append("and e.encounter_type = " + encounterType.getId() + " ");
 		}
 		else {
 	    	List<EncounterType> types = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_REGISTRATION_ENCOUNTER_TYPES();
 	    	if (!types.isEmpty()) {
 	    		sb.append("and e.encounter_type in (");
 	    		for (Iterator<EncounterType> iter = types.iterator(); iter.hasNext();) {
 	    			sb.append(iter.next().getId() + (iter.hasNext() ? "," : ""));
 	    		}
 	    		sb.append(") ");
 	    	}
 		}
 		if (location != null) {
 			sb.append("and e.location_id = " + location.getId() + " ");
 		}
 		else {
 	    	List<Location> locations = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_REGISTRATION_LOCATIONS();
 	    	if (!locations.isEmpty()) {
 	    		sb.append("and e.location_id in (");
 	    		for (Iterator<Location> iter = locations.iterator(); iter.hasNext();) {
 	    			sb.append(iter.next().getId() + (iter.hasNext() ? "," : ""));
 	    		}
 	    		sb.append(") ");
 	    	}
 		}
 		
 		List<List<Object>> queryResults = Context.getAdministrationService().executeSQL(sb.toString(), true);
 		Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
 		for (List<Object> row : queryResults) {
 			ret.put(Integer.parseInt(row.get(0).toString()), Integer.parseInt(row.get(1).toString()));
 		}
 		return ret;
 	}
 	
 	/**
 	 * @return the relevant person address value for each patient in the passed cohort if they have a person address, limited by the
 	 * passed filter criteria
 	 */
 	@SuppressWarnings("unchecked")
 	public Map<Integer, String> getAddressValuesForCohort(Cohort c, String addressField, Map<String, String> filterCriteria) {
 		Map<Integer, String> ret = new HashMap<Integer, String>();
 		if (!c.isEmpty()) {
 			StringBuilder sql = new StringBuilder();
 			sql.append("select p.personId, a."+addressField+" from Person p, PersonAddress a ");
 			sql.append("where p.personId = a.person.personId and p.voided = false and a.voided = false ");
 			// NOTE: Removed 'and a.preferred = true ' since many/most addresses didn't have this set properly (MS)
 			sql.append("and p.personId in ("+ c.getCommaSeparatedPatientIds() + ") ");
 			for (String filterKey : filterCriteria.keySet()) {
 				sql.append("and a." + filterKey + " = :" + filterKey + " ");
 			}
 			Query query = sessionFactory.getCurrentSession().createQuery(sql.toString());
 			for (String filterKey : filterCriteria.keySet()) {
 				query.setParameter(filterKey, filterCriteria.get(filterKey));
 			}
 			query.setCacheMode(CacheMode.IGNORE);
 			List<Object[]> queryResults = query.list();
 			for (Object[] row : queryResults) {
 				ret.put(Integer.valueOf(row[0].toString()), row[1] == null ? "" : row[1].toString());
 			}
 		}
 		return ret;
 	}
 	
 	public Set<Integer> getDistinctDuplicateObs(Integer conceptId){
 		Set<Integer> distinctDuplicates = null;
 		if(conceptId!=null){
 			StringBuilder sql = new StringBuilder();
 			sql.append("SELECT distinct(o.person_id) ");
 			sql.append("FROM obs o ");
 			sql.append("WHERE  o.concept_id=").append(conceptId.toString());
 			sql.append(" ORDER BY o.obs_datetime desc, o.person_id");
 			List<List<Object>> queryResults = Context.getAdministrationService().executeSQL(sql.toString(), true);
 			if(queryResults!=null && queryResults.size()>0){				
 				distinctDuplicates = new HashSet<Integer>();
 				for (List<Object> row : queryResults) {
 					Integer patientId = Integer.parseInt(row.get(0).toString());
 					if(patientId!=null){
 						distinctDuplicates.add(patientId);
 					}
 				}
 			}else{
 				log.debug("we got not duplicates");
 			}
 		}
 		
 		return distinctDuplicates;
 	}
 	
 	public List<DuplicatePatient> getDuplicatePatients(Patient patient){
 		List<DuplicatePatient> duplicatePatients = null;
 		if(patient!=null && patient.getId()!=null && (patient.getBirthdate()!=null)){
 			Calendar birthdate = Calendar.getInstance();
 			birthdate.setTime(patient.getBirthdate());
 			Integer birthYear = new Integer(birthdate.get(Calendar.YEAR)); 
 			Integer intervalYear = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_BIRTH_YEAR_INTERVAL();
 			Integer minYear = new Integer(birthYear.intValue() - intervalYear.intValue()); 
 			if(minYear.intValue()<0){
 				minYear = 0;
 			}
 			PatientIdentifierType zlIdentifierType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_IDENTIFIER_TYPE();
 			PatientIdentifierType dossierType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_NUMERO_DOSSIER();	
 			StringBuilder sql = new StringBuilder();
 			sql.append("SELECT s.person_id as PatientId, ");
 			sql.append("n.given_name as FirstName, n.family_name as LastName, ");
 			sql.append("s.gender as Gender, s.birthdate as Birthdate, ");
 			sql.append("a.address1 as Address1, ");
 			sql.append("a.city_village as Commune, s.date_created as PersonDateCreated, ");
 			if(zlIdentifierType!=null){	
 				sql.append("id1.identifier as ZLEMRID, ");
 			}
 			if(dossierType!=null){
 				sql.append("id3.identifier as NimewoDosye, ");		
 			}
 			sql.append("Min(e.date_created) as FirstEncounterDate ");			
 			sql.append("FROM person as s JOIN person_name as n ON (s.person_id=n.person_id) ");
 			sql.append("LEFT JOIN person_address as a ON (s.person_id = a.person_id) ");			
 			if(zlIdentifierType!=null){						
 				sql.append("LEFT JOIN patient_identifier as id1 ON (s.person_id = id1.patient_id and id1.identifier_type=").append(zlIdentifierType.getId().intValue()).append(") ");		
 			}
 			if(dossierType!=null){
 				sql.append("LEFT JOIN patient_identifier as id3 ON (s.person_id = id3.patient_id and id3.identifier_type=").append(dossierType.getId().intValue()).append(") ");
 			}
 			sql.append("LEFT outer JOIN encounter as e ON (s.person_id = e.patient_id) ");			
 			sql.append("WHERE s.voided=0 and n.voided=0 and n.given_name=\"").append(patient.getGivenName()).append("\" ");
 			sql.append("AND n.family_name=\"").append(patient.getFamilyName()).append("\" ");
 			sql.append("AND YEAR(s.birthdate) BETWEEN ").append(minYear.toString()).append(" AND ").append(birthYear.intValue()+ intervalYear.intValue()).append(" ");
 			if (patient.getPersonAddress()!=null){
 				String cityVillage = patient.getPersonAddress().getCityVillage();
 				if(StringUtils.isNotBlank(cityVillage)){					
 					sql.append("AND ((a.city_village is null) OR TRIM(a.city_village)='' OR (a.city_village=\"").append(cityVillage).append("\")) ");
 				}
 			}
 			sql.append("GROUP BY PatientId ");
 			sql.append("ORDER BY PatientId, PersonDateCreated, FirstEncounterDate");
 			
 			List<List<Object>> queryResults = Context.getAdministrationService().executeSQL(sql.toString(), true);
 			if(queryResults!=null && queryResults.size()>0){
 				duplicatePatients = new ArrayList<DuplicatePatient>();
 				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 				SimpleDateFormat bdf = new SimpleDateFormat("yyyy-MM-dd");
 				Date date = null;
 				Object fieldValue= null;
 				for (List<Object> row : queryResults) {
 					Integer patientId = Integer.parseInt(row.get(0).toString());
 					if(patientId.compareTo(patient.getId())==0){
 						continue;
 					}
 					
 					DuplicatePatient duplicatePatient = new DuplicatePatient();
 					duplicatePatient.setPatientId(patientId);
 					duplicatePatient.setFirstName(row.get(1).toString());
 					duplicatePatient.setLastName(row.get(2).toString());
 					duplicatePatient.setGender(row.get(3).toString());
 					try {						
 						date = bdf.parse(row.get(4).toString());
 						if(date!=null){
 							duplicatePatient.setBirthdate(date);
 						}
 					} catch (ParseException e) {
 						log.error("failed to parse date", e);
 					}	
 					fieldValue = row.get(5); 
 					if(fieldValue!=null){
 						duplicatePatient.setAddress1(fieldValue.toString());
 					}
 					fieldValue = row.get(6); 
 					if(fieldValue!=null){
 						duplicatePatient.setCityVillage(fieldValue.toString());
 					}
 					fieldValue = row.get(7); 
 					if(fieldValue!=null){
 						try {
 							date = sdf.parse(fieldValue.toString());
 							if(date!=null){
 								duplicatePatient.setPersonDateCreated(date);
 							}
 						} catch (ParseException e) {
 							log.error("failed to parse date", e);
 						}							
 					}
 					fieldValue = row.get(8); 
 					if(fieldValue!=null){
 						duplicatePatient.setZlEmrId(fieldValue.toString());
 					}
 					fieldValue = row.get(9); 
 					if(fieldValue!=null){
 						duplicatePatient.setDossierNumber(fieldValue.toString());
 					}
 					fieldValue = row.get(10); 
 					if(fieldValue!=null){
 						try {
 							date = sdf.parse(fieldValue.toString());
 							if(date!=null){
 								duplicatePatient.setFirstEncounterDate(date);
 							}
 						} catch (ParseException e) {
 							log.error("failed to parse date", e);
 						}
 					}
 					
 					duplicatePatients.add(duplicatePatient);
 				}
 			}
 			
 		}
 		return duplicatePatients;
 	}
 	
 	public Set<String> searchNames(String name, String nameField) {
 		Set<String> firstNames=null;
 		if(StringUtils.isNotBlank(name)){
 			StringBuilder sql = new StringBuilder();
 			sql.append("select distinct(n.").append(nameField).append(") ");
 			sql.append("from PersonName n ");			
 			sql.append("where n.").append(nameField).append(" like '%").append(name).append("%' ");
 			sql.append("group by n.").append(nameField).append(" ");
 			sql.append("order by n.").append(nameField).append(" ");
 			try{
 				Query query = sessionFactory.getCurrentSession().createQuery(sql.toString());		
 				query.setCacheMode(CacheMode.IGNORE);
 				List<String> queryResults = (List<String>)query.list();
 				if(queryResults!=null && queryResults.size()>0){
 					firstNames = new HashSet<String>();
 					for(String personName : queryResults){
 						firstNames.add(personName);
 					}					
 				}				
 			}catch(Exception e){
 				log.error("error retrieving patient names", e);
 			}						
 		}
 		return firstNames;
 	}
 	public Map<String, Integer> searchNamesByOccurence(String name, String nameField) {
 		Map<String, Integer> nameOccurences = new HashMap<String, Integer>();
 		if(StringUtils.isNotBlank(name)){
 			StringBuilder sql = new StringBuilder();
 			sql.append("select distinct(n.").append(nameField).append("), count(*) ");
 			sql.append("from PersonName n ");			
 			sql.append("where n.").append(nameField).append(" like '%").append(name).append("%' ");
 			sql.append("group by n.").append(nameField).append(" ");
 			sql.append("order by count(*) desc, n.").append(nameField).append(" ");
 			try{
 				Query query = sessionFactory.getCurrentSession().createQuery(sql.toString());		
 				query.setCacheMode(CacheMode.IGNORE);								
 				
 				List<Object[]> queryResults = query.list();
 				for (Object[] row : queryResults) {
 					nameOccurences.put(row[0] == null ? "" : row[0].toString(), Integer.valueOf(row[1].toString()));
 				}										
 			}catch(Exception e){
 				log.error("error retrieving patient names", e);
 			}						
 		}
 		return nameOccurences;
 	}
 	
 	public List<Patient> getPatientsByNameId(List<Integer> nameIds){
 		List<Patient> patients = null;
 		if(nameIds==null || (nameIds!=null && nameIds.size()<1)){
 			return null;
 		}
 		try{
 			Query query = sessionFactory.getCurrentSession().createQuery("from Patient as p where p.personId in (:nameIds)");
 			query.setParameterList("nameIds", nameIds);
 			query.setCacheMode(CacheMode.IGNORE);											
 			patients = query.list();		
 			if(patients!=null && patients.size()>0){
 				return patients;
 				
 			}
 													
 		}catch(Exception e){
 			log.error("error retrieving name phonetics", e);
 		}	
 		return patients;
 		
 	}
 	public List<Integer> getPhoneticsPersonId(String firstName, String lastName) {
 		
 		List<Integer> queryResults = null;
 		if (StringUtils.isBlank(firstName) || (StringUtils.isBlank(lastName))){
 			return queryResults;
 		}
 		StringBuilder sql = new StringBuilder();
		sql.append("select distinct np1.person.personId ");
 		sql.append("from NamePhonetic np1 ");
 		sql.append("where np1.renderedString like '").append(firstName).append("%' ");
 		sql.append("and np1.nameField=1 ");
 		sql.append("and np1.personName.personNameId in ");
 		sql.append("(select np2.personName.personNameId from NamePhonetic np2 ");
 		sql.append("where np2.renderedString like '").append(lastName).append("%' ");
 		sql.append("and np2.nameField=3) ");
 		try{
 			Query query = sessionFactory.getCurrentSession().createQuery(sql.toString());		
 			query.setCacheMode(CacheMode.IGNORE);								
 			
 			queryResults = query.list();		
 			if(queryResults!=null && queryResults.size()>0){
 				return queryResults;
 				
 			}
 													
 		}catch(Exception e){
 			log.error("error retrieving name phonetics", e);
 		}	
 		
 		return queryResults;
 	}
 	
 	public List<String> getDistinctObs(Integer conceptId){
 		List<String> distinctObs=null;
 		if(conceptId==null){
 			return distinctObs;
 		}
 		
 		StringBuilder sql = new StringBuilder();
 		sql.append("select distinct(trim(value_text)) as NonCodedDiagnoses");
 		sql.append(" from Obs where voided=0 and concept_id=").append(conceptId.toString());
 		sql.append(" order by value_text");
 		try{
 			Query query = sessionFactory.getCurrentSession().createQuery(sql.toString());
 			query.setCacheMode(CacheMode.IGNORE);				
 			distinctObs = query.list();
 			if(distinctObs!=null && distinctObs.size()>0){
 				List<String> cleanObs = new ArrayList<String>();
 				for(String obs : distinctObs){
 					cleanObs.add(obs.replace("\\", " "));
 				}
 				return cleanObs;
 			}
 		}catch(Exception e){
 			log.error("error retrieving distinct obs", e);
 		}
 		return distinctObs;
 	}
 	/**
 	 * @see PatientRegistrationDAO#saveUserActivity(UserActivity)
 	 */
 	public UserActivity saveUserActivity(UserActivity userActivity) {
 		sessionFactory.getCurrentSession().saveOrUpdate(userActivity);
 		return userActivity;
 	}
 }
