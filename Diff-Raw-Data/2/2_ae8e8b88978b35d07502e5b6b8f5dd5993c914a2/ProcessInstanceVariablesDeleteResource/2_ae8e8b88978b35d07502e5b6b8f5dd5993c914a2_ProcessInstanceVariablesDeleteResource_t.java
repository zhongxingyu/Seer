 /* Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.activiti.rest.api.process;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.activiti.engine.ActivitiException;
 import org.activiti.engine.history.HistoricProcessInstance;
 import org.activiti.rest.api.ActivitiUtil;
 import org.activiti.rest.api.SecuredResource;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.ObjectNode;
 import org.restlet.representation.Representation;
 import org.restlet.resource.Delete;
 
 public class ProcessInstanceVariablesDeleteResource extends SecuredResource {
 
 	@Delete
 	public ObjectNode deleteProcessInstanceVariables(Representation entity) {
 		try {
 			if (authenticate() == false) {
 				return null;
 			}
 
 			String processInstanceId = (String) getRequest().getAttributes()
 					.get("processInstanceId");
 
 			if (processInstanceId == null) {
 				throw new ActivitiException("No process instance id provided");
 			}
 
 			HistoricProcessInstance instance = ActivitiUtil.getHistoryService()
 					.createHistoricProcessInstanceQuery()
 					.processInstanceId(processInstanceId).singleResult();
 
 			if (instance == null) {
 				throw new ActivitiException(
 						"Process instance not found for id "
 								+ processInstanceId);
 			}
 
 			if (instance.getEndTime() != null) {
 				throw new ActivitiException("Process instance with id "
 						+ processInstanceId + " already ended");
 			}
 
 			List<String> variableNames = new ArrayList<String>();
 
 			String startParams = entity.getText();
 			JsonNode startJSON = new ObjectMapper().readTree(startParams);
 			ArrayNode variablesJSON = (ArrayNode) startJSON.get("variables");
 
 			for (JsonNode variableJSON : variablesJSON) {
 				String variableName = variableJSON.get("variableName")
 						.getTextValue();
 
 				variableNames.add(variableName);
 			}
 
 			ActivitiUtil.getRuntimeService().removeVariables(processInstanceId,
 					variableNames);
 
 			ObjectNode successNode = new ObjectMapper().createObjectNode();
 			successNode.put("success", true);
 			return successNode;
 
 		} catch (Exception e) {
			throw new ActivitiException("Failed to delete process variables", e);
 		}
 	}
 
 }
