 package org.robot.gtf.service.impl.mongodb;
 
 import java.util.Map;
 import org.robot.gtf.service.to.ProjectTO;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 
 /**
  * Represents a MongoDB-Document for projects and is at the the time responsible for mapping from
  * the corresponding ProjectTO to the JSON-Document and vice versa. 
  * @author thomas.jaspers
  */
 public class ProjectDocument {
 
 	private static final String DOCUMENT_ATTRIBUTE_ID = "_id";
 	
 	private static final String DOCUMENT_ATTRIBUTE_NAME = "name";
 	
 	private static final String DOCUMENT_ATTRIBUTE_DESC = "desc";
 	
 	private static final String DOCUMENT_ATTRIBUTE_RUNTIME_DEFINITIONS = "runtime_definitions";
 	
 	
 	/**
 	 * Building a ProjectTO from a given JSON document.
 	 * @param jsonDoc JSON Document
 	 * @return ProjectTO
 	 */
 	public ProjectTO buildProjectTO(DBObject jsonDoc) {
 		ProjectTO projectTO = new ProjectTO();
 		projectTO.setId((String) jsonDoc.get(DOCUMENT_ATTRIBUTE_ID));
 		projectTO.setName((String) jsonDoc.get(DOCUMENT_ATTRIBUTE_NAME));
 		projectTO.setDescription((String) jsonDoc.get(DOCUMENT_ATTRIBUTE_DESC));
 		
 		Map<String, Map<String, String>> environmentParameter = 
 				(Map<String, Map<String, String>>) jsonDoc.get(DOCUMENT_ATTRIBUTE_RUNTIME_DEFINITIONS);
 		projectTO.setEnvironmentParameter(environmentParameter);
 				
 		return projectTO;
 	}
     
 	/**
 	 * Returns the JSON representation of the contained ProjectTO
 	 * @param ProjectTO
 	 * @return JSON Document
 	 */
     public DBObject buildJsonDocument(ProjectTO projectTO) {
     	BasicDBObject jsonDoc = new BasicDBObject();
     	
     	// Setting basic attributes
         jsonDoc.put(DOCUMENT_ATTRIBUTE_ID, projectTO.getId());
         jsonDoc.put(DOCUMENT_ATTRIBUTE_NAME, projectTO.getName());
         jsonDoc.put(DOCUMENT_ATTRIBUTE_DESC, projectTO.getDescription());
         
         // Setting the List of environments, which contains a list of parameters
         BasicDBObject environments = new BasicDBObject();
         for (String envName : projectTO.getEnvironmentParameter().keySet()) {
         	BasicDBObject envParams = new BasicDBObject();
        		envParams.putAll(projectTO.getEnvironmentParameter().get(envName));
         	environments.put(envName, envParams);
         }
         
         jsonDoc.put(DOCUMENT_ATTRIBUTE_RUNTIME_DEFINITIONS, environments);
         return jsonDoc;
     }
 }
