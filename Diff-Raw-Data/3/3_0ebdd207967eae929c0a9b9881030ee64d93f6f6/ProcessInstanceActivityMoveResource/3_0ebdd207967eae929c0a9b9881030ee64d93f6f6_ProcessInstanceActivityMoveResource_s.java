 package org.activiti.rest.api.process;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.activiti.engine.ActivitiException;
 import org.activiti.engine.RuntimeService;
 import org.activiti.engine.impl.ProcessEngineImpl;
 import org.activiti.engine.impl.db.DbSqlSession;
 import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
 import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
 import org.activiti.engine.impl.pvm.process.ActivityImpl;
 import org.activiti.engine.runtime.ProcessInstance;
 import org.activiti.rest.api.ActivitiUtil;
 import org.activiti.rest.api.SecuredResource;
 import org.apache.commons.lang.StringUtils;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ObjectNode;
 import org.restlet.representation.Representation;
 import org.restlet.resource.Put;
 
 public class ProcessInstanceActivityMoveResource extends SecuredResource {
 
 	@Put
 	public ObjectNode moveCurrentActivity(Representation entity) {
 		ObjectNode responseJSON = new ObjectMapper().createObjectNode();
 
 		String processInstanceId = (String) getRequest().getAttributes().get("processInstanceId");
 
 		if (processInstanceId == null) {
 			throw new ActivitiException("No process instance is provided");
 		}
 
 		try {
 			// check authentication
 			if (authenticate() == false)
 				return null;
 
 			// extract request parameters
 			Map<String, Object> variables = new HashMap<String, Object>();
 			String startParams = entity.getText();
 			if (StringUtils.isNotEmpty(startParams)) {
 				JsonNode startJSON = new ObjectMapper().readTree(startParams);
 				variables.putAll(retrieveVariables(startJSON));
 			}
 
 			// extract activity ids
 			String sourceActivityId = (String) variables.remove("sourceActivityId");
 			String targetActivityId = (String) variables.remove("targetActivityId");
 
 			if (sourceActivityId == null || targetActivityId == null) {
 				responseJSON.put("success", false);
 				responseJSON.put("failureReason", "Request is missing sourceActivityId and targetActivityId");
 				return responseJSON;
 			}
 
 			RuntimeService runtimeService = ActivitiUtil.getRuntimeService();
 			ExecutionEntity execution = (ExecutionEntity) runtimeService
 					.createExecutionQuery()
 					.processInstanceId(processInstanceId)
 					.activityId(sourceActivityId).singleResult();
 			
 			ProcessInstance instance = runtimeService
 					.createProcessInstanceQuery()
 					.processInstanceId(execution.getProcessInstanceId())
 					.singleResult();
 			
 			ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ActivitiUtil
 					.getRepositoryService().getProcessDefinition(instance.getProcessDefinitionId());
 
 			ActivityImpl targetActivity = definition.findActivity(targetActivityId);
 			execution.setActivity(targetActivity);
 		
 			DbSqlSession ses = (DbSqlSession) ((ProcessEngineImpl) ActivitiUtil.getProcessEngine()).getDbSqlSessionFactory().openSession();
 			ses.update(execution);
			ses.flush();			
 					
 			responseJSON.put("success", true);
 			return responseJSON;
 
 		} catch (Exception ex) {
 			throw new ActivitiException(
 					"Failed to move current activity for instance id "
 							+ processInstanceId, ex);
 		}
 	}
 }
