 /*
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy
  * of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
  * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
  * the specific language governing permissions and limitations under the
  * License.
  */
 
 package org.amplafi.flow;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.amplafi.flow.FlowManagement;
 import org.amplafi.flow.impl.FlowActivityImpl;
 import org.amplafi.flow.impl.FlowStateImpl;
 import org.amplafi.flow.FlowStateJsonRenderer;
 import org.amplafi.flow.validation.FlowValidationResultJsonRenderer;
 import org.amplafi.flow.validation.FlowValidationTrackingJsonRenderer;
 import org.amplafi.json.JSONStringer;
 import org.amplafi.json.JSONWriter;
 import org.amplafi.json.renderers.MapJsonRenderer;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 import static com.sworddance.util.CUtilities.*;
 
 public class TestFlowStateJsonRenderer extends Assert {
 
     // when TODO in FactoryFlowPropertyDefinitionProvider is fixed, these values will not be in the outputed properties.
     private static final String EMPTY_FLOW_TRANSITIONS = "\"fsFlowTransitions\":{}";
     private static final String EMPTY_FLOW_TRANSITIONS_FIRST = EMPTY_FLOW_TRANSITIONS+",";
     /**
      * This tests the JSON rendering of a flow state with no fsParameters.
      */
     @Test
     public void testSimpleFlowState() {
         FlowStateImpl flowState = newFlowState();
         JSONWriter jsonWriter = getJsonWriter();
        jsonWriter.object().key("flowState").value(flowState).endObject();
        assertEquals(jsonWriter.toString(), "{\"flowState\":{\""+FlowStateJsonRenderer.FS_COMPLETE+"\":true,\"" +
        		FlowStateJsonRenderer.FS_LOOKUP_KEY+"\":\""+flowState.getLookupKey()+"\",\"" +
                FlowStateJsonRenderer.FS_PARAMETERS+"\":{" +
                		EMPTY_FLOW_TRANSITIONS +
                		"}" +
        		"}}");
 
     }
 
     /**
      * This tests the JSON rendering of a flow state with two string parameters.
      */
     @Test
     public void testCompleteFlowState() {
         FlowStateImpl flowState = newFlowState();
         Map<String, String> trustedValues = createMap(
             "property1", "value1",
             "property2", "value2");
         flowState.copyTrustedValuesMapToFlowState(trustedValues);
         JSONWriter jsonWriter = getJsonWriter();
        jsonWriter.object().key("flowState").value(flowState).endObject();
        assertEquals(jsonWriter.toString(), "{\"flowState\":{\""+FlowStateJsonRenderer.FS_COMPLETE+"\":true,\""+
        		FlowStateJsonRenderer.FS_LOOKUP_KEY+"\":\""+flowState.getLookupKey()+"\",\"" +
                FlowStateJsonRenderer.FS_PARAMETERS+"\":{" +
                EMPTY_FLOW_TRANSITIONS_FIRST +
                		"\"property1\":\"value1\",\"property2\":\"value2\"}}}");
     }
 
     /**
      * This tests that a object represented in the flow state is properly rendered when serializing
      * to JSON. The object should already bee stored in JSON format so we would want to see it
      * re-rendered as a string, which would give all of the double quotes the escape character.
      */
     @Test
     public void testCompleteFlowStateWithSimpleObject() {
         Map<String, String> testObject = new LinkedHashMap<String, String>();
         testObject.put("objectParameter1", "parameterValue1");
         testObject.put("objectParameter2", "parameterValue2");
         FlowStateImpl flowState = newFlowState();
         flowState.<String> setProperty("property1", "value1");
         flowState.<String> setProperty("property2", "value2");
         flowState.<Map<String, String>> setProperty("objectProperty", testObject);
 
         JSONWriter jsonWriter = getJsonWriter();
        jsonWriter.object().key("flowState").value(flowState).endObject();
        assertEquals(jsonWriter.toString(), "{\"flowState\":{\"" + FlowStateJsonRenderer.FS_COMPLETE + "\":true,\""
            + FlowStateJsonRenderer.FS_LOOKUP_KEY + "\":\"" + flowState.getLookupKey() + "\",\"" + FlowStateJsonRenderer.FS_PARAMETERS
            + "\":{" +
            EMPTY_FLOW_TRANSITIONS_FIRST +
            "\"property1\":\"value1\",\"property2\":\"value2\""
            + ",\"objectProperty\":{\"objectParameter1\":\"parameterValue1\",\"objectParameter2\":\"parameterValue2\"}}}}");
     }
 
     private FlowStateImpl newFlowState() {
         FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
         String flowTypeName = flowTestingUtils.addFlowDefinition(new FlowActivityImpl());
         FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
         FlowStateImpl flowState = flowManagement.startFlowState(flowTypeName, true, null);
         flowState.finishFlow();
         return flowState;
     }
     private JSONWriter getJsonWriter() {
         MapJsonRenderer mapJsonRenderer = new MapJsonRenderer();
         JSONWriter jsonWriter = new JSONStringer();
         jsonWriter.addRenderer(FlowValidationResultJsonRenderer.INSTANCE);
         jsonWriter.addRenderer(FlowValidationTrackingJsonRenderer.INSTANCE);
         jsonWriter.addRenderer(new FlowStateJsonRenderer());
         jsonWriter.addRenderer(mapJsonRenderer);
         return jsonWriter;
     }
 }
