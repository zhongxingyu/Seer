 package com.mclinic.api.dao.impl;
 
 import java.util.List;
 
 import com.burkeware.search.api.Context;
 import com.burkeware.search.api.RestAssuredService;
 import com.burkeware.search.api.logger.Logger;
 import com.burkeware.search.api.util.StringUtil;
 import com.google.inject.Inject;
 import com.mclinic.api.dao.CohortDao;
 import com.mclinic.api.model.Cohort;
 import com.mclinic.util.Constants;
 
 public class CohortDaoImpl implements CohortDao {
 	
 	@Inject
	private static RestAssuredService service; 
 	
 	@Inject
 	private Logger log;
 	
 	private String TAG = "CohortDao"; 
 	
 	@Override
 	public Cohort createCohort(Cohort cohort) {
 		try {
 			service.createObject(cohort, Context.getResource(Constants.COHORT));
 		} catch (Exception e) {
 			log.debug(TAG, "Error creating cohort " + e.getLocalizedMessage());
 		}
 		return null;
 	}
 
 	@Override
 	public Cohort updateCohort(Cohort cohort) {
 		try {
 			service.updateObject(cohort, Context.getResource(Constants.COHORT));
 		} catch (Exception e) {
 			log.debug(TAG, "Error updating cohort " + e.getLocalizedMessage());
 		}
 		return null;
 	}
 
 	@Override
 	public Cohort getCohortByUUID(String uuid) {
 		try {
 			return service.getObject("uuid: " + StringUtil.quote(uuid), Cohort.class);
 		} catch (Exception e) {
 			log.debug(TAG, "Error in getCohortByUUID " + e.getLocalizedMessage());
 		}
 		return null;
 	}
 	
 	@Override
 	public List<Cohort> getCohortsByName(String name) {
 		try {
 			return service.getObjects("name: " + StringUtil.quote(name + "*"), Cohort.class);
 		} catch (Exception e) {
 			log.debug(TAG, "Error in getCohortsByName " + e.getLocalizedMessage());
 		}
 		return null;
 	}
 
 	@Override
 	public List<Cohort> getAllCohorts() {
 		try {
 			return service.getObjects(null, Cohort.class);
 		} catch (Exception e) {
 			log.debug(TAG, "Error fetching all cohorts " + e.getLocalizedMessage());
 		}
 		return null;
 	}
 
 	@Override
 	public void deleteCohort(Cohort cohort) {
 		try {
 			service.invalidate(cohort, Context.getResource(Constants.COHORT));
 		} catch (Exception e) {
 			log.debug(TAG, "Error deleting cohort " + e.getLocalizedMessage());
 		}
 	}
 
 	@Override
 	public void deleteAllCohorts() {
 		// TODO Auto-generated method stub
 	}
 }
