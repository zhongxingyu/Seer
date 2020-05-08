 package org.amplafi.flow.translator;
 
 import static com.sworddance.util.CUtilities.isNotEmpty;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.amplafi.flow.Flow;
 import org.amplafi.flow.FlowActivity;
 import org.amplafi.flow.FlowActivityPhase;
 import org.amplafi.flow.FlowConstants;
import org.amplafi.flow.FlowException;
 import org.amplafi.flow.FlowExecutionException;
 import org.amplafi.flow.FlowManagement;
 import org.amplafi.flow.FlowPropertyDefinition;
 import org.amplafi.flow.FlowRenderer;
 import org.amplafi.flow.FlowState;
 import org.amplafi.flow.FlowStepDirection;
 import org.amplafi.flow.ServicesConstants;
 import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImplementor;
 import org.amplafi.flow.impl.FlowStateImplementor;
 import org.amplafi.flow.validation.FlowValidationException;
 import org.amplafi.flow.validation.FlowValidationResult;
 import org.amplafi.flow.validation.FlowValidationTracking;
import org.amplafi.json.IJsonWriter;
 import org.amplafi.json.JSONWriter;
 import org.amplafi.json.JsonConstruct;
 import org.amplafi.json.renderers.IterableJsonOutputRenderer;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.sworddance.util.CUtilities;
 
 public class JsonFlowRenderer implements FlowRenderer {
 
      private Log log;
 
     public JsonFlowRenderer() {
     }
 
     @Override
     public String getRenderResultType() {
         return FlowConstants.JSON;
     }
 
     @Override
     public void render(Writer writer, FlowState flowState, String errorMessage,
             Exception exception) {
         JSONWriter jsonWriter = getFlowStateWriter();
         if (errorMessage != null || exception != null) {
             renderError(flowState, errorMessage, exception, writer);
         } else {
             boolean singlePropertyFlow = flowState.getFlowPropertyDefinition(FlowConstants.FSSINGLE_PROPERTY_NAME) != null;
             if ( singlePropertyFlow ) {
                 serializeSinglePropertyValue((FlowStateImplementor) flowState, jsonWriter);
             } else {
                 // HACK : NEED SECURITY CHECKS to make sure only visible values are exported.
                 // TODO : filter by ExternalPropertyAccessRestriction.isReadable() on each property
                 jsonWriter.object();
                 jsonWriter.value(flowState);
                 jsonWriter.endObject();
             }
             try {
                 String string = jsonWriter.toString();
                 if ( StringUtils.isNotBlank(string)) {
                     writer.append(string);
                 }
             } catch (IOException e) {
                 throw new FlowExecutionException(e);
             }
         }
     }
     private void serializeSinglePropertyValue(FlowStateImplementor flowState, JSONWriter jsonWriter) {
         String singlePropertyName = flowState.getProperty(FlowConstants.FSSINGLE_PROPERTY_NAME);
         FlowPropertyDefinitionImplementor flowPropertyDefinition = flowState.getFlowPropertyDefinition(singlePropertyName);
         // TODO : SECURITY : HACK This important security check to make sure that secure properties are not released
         // to users. This security check needs to built in to the flow code itself. We must not rely on the renderer to do
         // security checks.
         // this is an important valid use case for generating a temp api key that is returned via a callback uri not directly
         if ( flowPropertyDefinition.isExportable()) {
             String rawProperty = flowState.getRawProperty(flowState, flowPropertyDefinition);
             //Only request property from flow state when there is no raw (already serialized) property available.
             //Avoids re-serialization overhead and allows JsonSelfRenderers not to implement from json.
             if (rawProperty != null) {
                 JsonConstruct jsonConstruct = JsonConstruct.Parser.toJsonConstruct(rawProperty);
                 if (jsonConstruct != null) {
                     jsonWriter.value(jsonConstruct);
                 } else {
                     jsonWriter.append(rawProperty);
                 }
             } else {
                 Object propertyValue = flowState.getPropertyWithDefinition(flowState, flowPropertyDefinition);
                 flowPropertyDefinition.serialize(jsonWriter, propertyValue);
             }
         }
     }
 
     protected JSONWriter getFlowStateWriter() {
         JSONWriter jsonWriter = new JSONWriter();
         jsonWriter.addRenderer(FlowState.class, new FlowStateJsonRenderer());
         return jsonWriter;
     }
 
     protected void renderError(FlowState flowState, String message, Exception exception, Writer writer) {
         JSONWriter jsonWriter = getFlowStateWriter();
         try {
             jsonWriter.object();
             jsonWriter.keyValueIfNotBlankValue(ServicesConstants.ERROR, message);
             if (flowState != null) {
                 jsonWriter.value(flowState);
                 // TODO : probably need to check on PropertyRequired.finish
                 Map<String, FlowValidationResult> result = flowState.getFlowValidationResults(FlowActivityPhase.advance, FlowStepDirection.forward);
                 writeValidationResult(jsonWriter, result);
             }
             if (exception instanceof FlowValidationException) {
                 FlowValidationException e = (FlowValidationException) exception;
                 Map<String, FlowValidationResult> validationResult = CUtilities.createMap("flow-result", e.getFlowValidationResult());
                 writeValidationResult(jsonWriter, validationResult);
            } else if ( exception instanceof FlowException) {
                FlowException flowException = (FlowException) exception;
                jsonWriter.keyValueIfNotBlankValue("exception", flowException.getMessage());

                IJsonWriter jsonWriterForLog = getFlowStateWriter();
                FlowStateJsonRenderer.INSTANCE.toJson(jsonWriterForLog, flowException.getFlowState());

                getLog().error("A FlowException terminated flow execution:"+jsonWriterForLog, flowException);
             } else if (exception != null){
                 jsonWriter.keyValueIfNotBlankValue("exception", exception.getMessage());
                getLog().error("A non-FlowException terminated flow execution.", exception);
             }
             jsonWriter.endObject();
             writer.append(jsonWriter.toString());
         } catch (IOException e) {
            getLog().error("While doing a renderError", e);
         } catch (Exception e) {
             try {
                 writer.append("{" +ServicesConstants.ERROR + ": 'Failed to render flow state. Cause: "+ e.getMessage() + "'}");
                 getLog().error("Failed to render flow state.", e);
             } catch (IOException e1) {
                // we are writing to a stringwriter so for it to have a IOException...
                getLog().fatal("renderError: things are really ugly",e1);
             }
         }
     }
 
     private void writeValidationResult(JSONWriter jsonWriter, Map<String, FlowValidationResult> result) {
         if (result != null && !result.isEmpty()) {
             jsonWriter.key(ServicesConstants.VALIDATION_ERRORS);
             jsonWriter.array();
             for (FlowValidationResult flowValidationResult : result.values()) {
                 List<FlowValidationTracking> trackings = flowValidationResult.getTrackings();
                 for (FlowValidationTracking tracking : trackings) {
                     jsonWriter.object();
                     jsonWriter.keyValue("code", tracking.getMessageKey());
                     Object[] messageParameters = tracking.getMessageParameters();
                     if (CUtilities.isNotEmpty(messageParameters)) {
                         jsonWriter.key("details");
                         jsonWriter.array();
                         for (Object parameter : messageParameters) {
                             if (parameter instanceof String) {
                                 //Try to convert to json first to avoid unneeded quotes escaping.
                                 String p = (String) parameter;
                                 parameter = JsonConstruct.Parser.toJsonConstruct(p);
                                 if (parameter == null) {
                                     parameter = p;
                                 }
                             }
                             jsonWriter.value(parameter);
                         }
                         jsonWriter.endArray();
                     }
                     jsonWriter.endObject();
                 }
             }
             jsonWriter.endArray();
         }
     }
 
     @Override
     public void describeFlow(Writer writer, Flow flowType) {
         try{
             JSONWriter jsonWriter = getFlowStateWriter();
             jsonWriter.object();
             renderFlowParameterJSON(jsonWriter, flowType);
             jsonWriter.endObject();
             CharSequence description = jsonWriter.toString();
             writer.append(description);
         } catch (IOException e) {
             throw new FlowExecutionException(e);
         }
     }
 
     @Override
     public void describeApi(Writer writer, FlowManagement flowManagement) {
         try {
             Collection<String> flowTypes = flowManagement.listAvailableFlows();
             List<String> orderedList = new ArrayList<>(flowTypes);
             Collections.sort(orderedList);
             JSONWriter jWriter = new JSONWriter();
             IterableJsonOutputRenderer.INSTANCE.toJson(jWriter, orderedList);
             writer.append(jWriter.toString());
         } catch (IOException e) {
             throw new FlowExecutionException(e);
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
                 jsonWriter.keyValue("activity",    flowActivity.getFlowPropertyProviderName());
                 jsonWriter.keyValue("activityTitle", flowActivity.getActivityTitle());
                 jsonWriter.keyValue("invisible", flowActivity.isInvisible());
                 jsonWriter.keyValue("finishing", flowActivity.isFinishingActivity());
                 jsonWriter.key("parameters");
                 jsonWriter.array();
                 for (Map.Entry<String, FlowPropertyDefinition> entry : flowActivity.getPropertyDefinitions().entrySet()) {
                     final FlowPropertyDefinition definition = entry.getValue();
                     //Only describe properties that can be set from a client.
                     if (definition.getPropertyUsage().isExternallySettable() && definition.isExportable()) {
                         jsonWriter.object();
                         jsonWriter.keyValue("name", definition.getName());
                         jsonWriter.keyValue("type", definition.getDataClass().getSimpleName());
                         jsonWriter.keyValue("req", definition.getPropertyRequired());
                         jsonWriter.endObject();
                     }
                 }
                 jsonWriter.endArray();
                 jsonWriter.endObject();
             }
             jsonWriter.endArray();
         }
     }
 
     public Log getLog() {
         if (log == null) {
             log = LogFactory.getLog(JsonFlowRenderer.class);
             log.warn("Log wasn't injected by a dependency injection framework, initializing it manually.");
         }
         return log;
     }
 
     public void setLog(Log log) {
         this.log = log;
     }
 }
