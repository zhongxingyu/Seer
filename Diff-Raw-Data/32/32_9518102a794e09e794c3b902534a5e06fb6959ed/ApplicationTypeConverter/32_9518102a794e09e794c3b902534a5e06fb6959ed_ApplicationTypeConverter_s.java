 package com.photon.phresco.service.converters;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.springframework.data.document.mongodb.MongoOperations;
 import org.springframework.data.document.mongodb.query.Criteria;
 import org.springframework.data.document.mongodb.query.Query;
 
 import com.photon.phresco.commons.model.ApplicationType;
 import com.photon.phresco.commons.model.TechnologyGroup;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.service.api.Converter;
 import com.photon.phresco.service.dao.ApplicationTypeDAO;
 import com.photon.phresco.util.ServiceConstants;
 
 public class ApplicationTypeConverter implements Converter<ApplicationTypeDAO, ApplicationType>, ServiceConstants {
 
 	@Override
 	public ApplicationType convertDAOToObject(ApplicationTypeDAO applicationTypeDAO,
 			MongoOperations mongoOperation) throws PhrescoException {
 		ApplicationType applicationType = new ApplicationType();
 		applicationType.setId(applicationTypeDAO.getId());
 		applicationType.setName(applicationTypeDAO.getName());
 		applicationType.setSystem(applicationTypeDAO.isSystem());
 		if(CollectionUtils.isNotEmpty(applicationTypeDAO.getTechGroupIds())) {
 			List<TechnologyGroup> techGroups = mongoOperation.find(TECH_GROUP_COLLECTION_NAME, 
 					new Query(Criteria.whereId().in(applicationTypeDAO.getTechGroupIds().toArray())), TechnologyGroup.class);
 			applicationType.setTechGroups(techGroups);
 		}
 		applicationType.setCustomerIds(applicationTypeDAO.getCustomerIds());
 		return applicationType;
 	}
 
 	@Override
 	public ApplicationTypeDAO convertObjectToDAO(ApplicationType applicationType)
 			throws PhrescoException {
 		ApplicationTypeDAO applicationTypeDAO = new ApplicationTypeDAO();
 		applicationTypeDAO.setId(applicationType.getId());
 		applicationTypeDAO.setName(applicationType.getName());
 		List<String> techIds = new ArrayList<String>();
 		List<TechnologyGroup> techGroups = applicationType.getTechGroups();
 		for (TechnologyGroup technologyGroup : techGroups) {
 			techIds.add(technologyGroup.getId());
 		}
 		applicationTypeDAO.setTechGroupIds(techIds);
 		applicationTypeDAO.setSystem(applicationType.isSystem());
 		applicationTypeDAO.setCustomerIds(applicationType.getCustomerIds());
 		return applicationTypeDAO;
 	}
 
 }
