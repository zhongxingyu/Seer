 package org.amplafi.flow.impl;
 
 import static com.sworddance.util.CUtilities.isNotEmpty;
 import static org.amplafi.flow.launcher.FlowLauncher.FLOW_STATE_JSON_KEY;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Collection;
 import java.util.Map;
 
 import org.amplafi.flow.Flow;
 import org.amplafi.flow.FlowActivity;
 import org.amplafi.flow.FlowActivityPhase;
 import org.amplafi.flow.FlowConstants;
 import org.amplafi.flow.FlowDefinitionsManager;
 import org.amplafi.flow.FlowPropertyDefinition;
 import org.amplafi.flow.FlowRenderer;
 import org.amplafi.flow.FlowState;
 import org.amplafi.flow.FlowStateJsonRenderer;
 import org.amplafi.flow.FlowStepDirection;
 import org.amplafi.flow.ServicesConstants;
 import org.amplafi.flow.validation.FlowValidationException;
 import org.amplafi.flow.validation.FlowValidationResult;
 import org.amplafi.flow.web.FlowRequest;
 import org.amplafi.json.JSONWriter;
 import org.amplafi.json.renderers.IterableJsonOutputRenderer;
 import org.apache.commons.lang.StringUtils;
 
 import com.sworddance.util.CUtilities;
 
 public class JsonFlowRenderer implements FlowRenderer {
 
 	private FlowDefinitionsManager flowDefinitionsManager;
 	
 	public JsonFlowRenderer() {
 	}
 
 	public JsonFlowRenderer(FlowDefinitionsManager flowDefinitionsManager) {
 		this.flowDefinitionsManager = flowDefinitionsManager;
 	}
 
 	@Override
 	public String getRenderResultType() {
 		return FlowConstants.JSON;
 	}
 
 	@Override
 	public void render(FlowState flowState, Writer writer) {
 		JSONWriter jsonWriter = getFlowStateWriter();
 		jsonWriter.object();
 		jsonWriter.keyValueIfNotNullValue(FLOW_STATE_JSON_KEY, flowState);
 		jsonWriter.endObject();
 		try {
 			writer.append(jsonWriter.toString());
 		} catch (IOException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 
 	protected JSONWriter getFlowStateWriter() {
 		JSONWriter jsonWriter = new JSONWriter();
 		jsonWriter.addRenderer(FlowState.class, new FlowStateJsonRenderer());
 		return jsonWriter;
 	}
 
 	@Override
 	public void renderError(FlowState flowState, String message,
 			Exception exception, Writer writer) {
 		JSONWriter jsonWriter = getFlowStateWriter();
         jsonWriter.object();
         jsonWriter.keyValueIfNotBlankValue(ServicesConstants.ERROR_MESSAGE, message);
         if (flowState != null) {
             jsonWriter.key(FLOW_STATE_JSON_KEY).value(flowState);
             // TODO : probably need to check on PropertyRequired.finish
             Map<String, FlowValidationResult> result = flowState.getFlowValidationResults(FlowActivityPhase.advance, FlowStepDirection.forward);
             writeValidationResult(jsonWriter, result);
         } else if (exception instanceof FlowValidationException) {
             FlowValidationException e = (FlowValidationException) exception;
             Map<String, FlowValidationResult> validationResult = CUtilities.createMap("flow-result", e.getFlowValidationResult());
             writeValidationResult(jsonWriter, validationResult);
        } else if (exception != null){
             jsonWriter.keyValueIfNotBlankValue("exception", exception.getMessage());
         }
         jsonWriter.endObject();
         
         try {
 			writer.append(jsonWriter.toString());
 		} catch (IOException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 	
 	private void writeValidationResult(JSONWriter jsonWriter, Map<String, FlowValidationResult> result) {
         if (result != null && !result.isEmpty()) {
             jsonWriter.key(ServicesConstants.VALIDATION_ERRORS).value(result);
         }
     }
 
 	@Override
 	public void describe(FlowRequest flowRequest) {
 		try{
 			Writer writer = flowRequest.getWriter();
 			String flowType = flowRequest.getFlowType();
 			if (StringUtils.isBlank(flowType)) {
 				Collection<String> flowTypes = flowDefinitionsManager.getFlowDefinitions().keySet();
 				JSONWriter jWriter = new JSONWriter();
 				IterableJsonOutputRenderer.INSTANCE.toJson(jWriter, flowTypes);
 				writer.append(jWriter.toString());
 			} else {
 				Flow flow = flowDefinitionsManager.getFlowDefinition(flowType);
 				JSONWriter jsonWriter = getFlowStateWriter();
 				jsonWriter.object();
 				renderFlowParameterJSON(jsonWriter, flow);
 				jsonWriter.endObject();
 				CharSequence description = jsonWriter.toString();
 				writer.append(description);
 			}
 		} catch (IOException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 
 	/**
 	 * TODO: Move in separate JsonRenderer when finalized
 	 */
 	private void renderFlowParameterJSON(JSONWriter jsonWriter, Flow flow) {
 		jsonWriter.keyValue("flowTitle", flow.getFlowTitle());
 		if (isNotEmpty(flow.getActivities())) {
 			jsonWriter.key("activities");
 			jsonWriter.array();
 			// TODO Kostya: describe the format in the tutorial..
 			for (FlowActivity flowActivity : flow.getActivities()) {
 				jsonWriter.object();
 				jsonWriter.keyValue("activity",	flowActivity.getFlowPropertyProviderName());
 				jsonWriter.keyValue("activityTitle", flowActivity.getActivityTitle());
 				jsonWriter.keyValue("invisible", flowActivity.isInvisible());
 				jsonWriter.keyValue("finishing", flowActivity.isFinishingActivity());
 				jsonWriter.key("parameters");
 				jsonWriter.array();
 				for (Map.Entry<String, FlowPropertyDefinition> entry : flowActivity.getPropertyDefinitions().entrySet()) {
 					final FlowPropertyDefinition definition = entry.getValue();
 					//Only describe properties that can be set from a client.
 					if (definition.getPropertyUsage().isExternallySettable() && definition.isExportable()) {
 						definition.toJson(jsonWriter);
 					}
 				}
 				jsonWriter.endArray();
 				jsonWriter.endObject();
 			}
 			jsonWriter.endArray();
 		}
 	}
 
 	public FlowDefinitionsManager getFlowDefinitionsManager() {
 		return flowDefinitionsManager;
 	}
 
 	public void setFlowDefinitionsManager(
 			FlowDefinitionsManager flowDefinitionsManager) {
 		this.flowDefinitionsManager = flowDefinitionsManager;
 	}
 }
