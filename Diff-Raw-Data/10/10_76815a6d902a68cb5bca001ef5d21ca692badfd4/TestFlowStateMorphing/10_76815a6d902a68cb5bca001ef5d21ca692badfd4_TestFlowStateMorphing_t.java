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
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertNull;
 import static org.testng.Assert.fail;
 
 import java.util.HashMap;
 
 import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;
 import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImplementor;
 import org.amplafi.flow.impl.FlowActivityImpl;
 import org.amplafi.flow.impl.FlowImpl;
 import org.amplafi.flow.impl.FlowStateImplementor;
 import org.testng.annotations.Test;
 
 public class TestFlowStateMorphing {
 
     private static final String FIRST_FLOW = "FirstFlow";
 
     private static final String MORPHED_FLOW = "MorphedFlow";
 
     private static final String FS_MORPH_FLOW = "fsMorphFlow";
 
     /**
      * In this case flowstate morphing is happening becasue the value dynamic FPD is favorable for morphing
      */
     @Test
     public void testPositiveStateFlowMorphingPositive() {
         FlowActivityImpl fa1 = createFA("FA-1");
         FlowPropertyDefinitionImplementor morphFlowFPD = new FlowPropertyDefinitionBuilder(FS_MORPH_FLOW, Boolean.class).initAutoCreate().toFlowPropertyDefinition();
         fa1.addPropertyDefinitions(morphFlowFPD);
 
         FlowActivityImpl fa2 = createFA("FA-2");
         FlowActivityImpl fa3 = createFA("FA-3");
         FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
 
         flowTestingUtils.getFlowDefinitionsManager().addDefinitions(new FlowImpl(FIRST_FLOW, fa1,fa2,fa3));
 
         FlowActivityImpl fa4 = createFA("FA-4");
         FlowActivityImpl fa5 = createFA("FA-5");
         flowTestingUtils.getFlowDefinitionsManager().addDefinitions(new FlowImpl(MORPHED_FLOW, fa2,fa4,fa5));
         FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
 
         FlowState flowState = flowManagement.startFlowState(FIRST_FLOW, true, null, false);
         assertEquals(flowState.getFlow().getFlowPropertyProviderName(), FIRST_FLOW);
         assertEquals(flowState.getCurrentActivityByName(), "FA-1");
         flowState.next();
         assertEquals(flowState.getCurrentActivityByName(), "FA-2");
 
         flowState.morphFlow(MORPHED_FLOW, null);
         assertEquals(flowState.getFlow().getFlowPropertyProviderName(), MORPHED_FLOW);
         assertEquals(flowState.getCurrentActivityByName(), "FA-2");
         flowState.next();
         assertEquals(flowState.getCurrentActivityByName(), "FA-4");
 
         flowState.morphFlow(FIRST_FLOW, null);
         assertEquals(flowState.getCurrentActivityByName(), "FA-3");
     }
 
     /**
      * In this case there is no morphing happening
      */
     @Test
     public void testNegativeStateFlowMorphing() {
         FlowActivityImpl fa1 = createFA("FA-1");
        FlowPropertyDefinitionImplementor morphFlowFPD = new FlowPropertyDefinitionBuilder(FS_MORPH_FLOW, Boolean.class).initAutoCreate().toFlowPropertyDefinition();
        fa1.addPropertyDefinitions(morphFlowFPD);
 
         FlowActivityImpl fa2 = createFA("FA-2");
         FlowActivityImpl fa3 = createFA("FA-3");
         FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
         flowTestingUtils.addFlowDefinition(FIRST_FLOW, fa1,fa2,fa3);
         FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
 
         FlowStateImplementor flowState = flowManagement.startFlowState(FIRST_FLOW, true, null, false);
         assertEquals(flowState.getFlow().getFlowPropertyProviderName(), FIRST_FLOW);
         assertEquals(flowState.getCurrentActivity().getFlowPropertyProviderName(), "FA-1");
         flowState.next();
         assertEquals(flowState.getCurrentActivity().getFlowPropertyProviderName(), "FA-2");
         flowState.setPropertyWithDefinition(fa1,morphFlowFPD,false);
         flowState.next();
         assertEquals(flowState.getCurrentActivity().getFlowPropertyProviderName(), "FA-3");
         flowState.finishFlow();
         assertNull(flowManagement.getCurrentFlowState(), "there shouldn't be a flow running");
     }
 
     /**
      * Test no common FAs
      */
     @Test
     public void testNoCommonFAs() {
         FlowActivityImpl fa1 = createFA("FA-1");
         fa1.addPropertyDefinitions(new FlowPropertyDefinitionBuilder(FS_MORPH_FLOW, Boolean.class).initAutoCreate());
 
         FlowActivityImpl fa2 = createFA("FA-2");
         FlowActivityImpl fa3 = createFA("FA-3");
 
         FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
         flowTestingUtils.getFlowDefinitionsManager().addDefinitions(new FlowImpl(FIRST_FLOW, fa1,fa2,fa3));
 
         FlowActivityImpl fa4 = createFA("FA-4");
         FlowActivityImpl fa5 = createFA("FA-5");
         flowTestingUtils.getFlowDefinitionsManager().addDefinitions(new FlowImpl(MORPHED_FLOW, fa4, fa5));
 
         FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
         FlowState flowState = flowManagement.startFlowState(FIRST_FLOW, true, null, false);
         assertEquals(flowState.getFlow().getFlowPropertyProviderName(), FIRST_FLOW);
         assertEquals(flowState.getCurrentActivity().getFlowPropertyProviderName(), "FA-1");
         flowState.next();
         assertEquals(flowState.getCurrentActivity().getFlowPropertyProviderName(), "FA-2");
 
         flowState.morphFlow(MORPHED_FLOW, new HashMap<String,String>());
         assertEquals(flowState.getFlow().getFlowPropertyProviderName(), MORPHED_FLOW);
         assertEquals(flowState.getCurrentActivity().getFlowPropertyProviderName(), "FA-4");
     }
 
     /**
      * Test no FAs not in order
      */
     @Test(expectedExceptions = IllegalStateException.class)
     public void testFAsNotInOrder() {
         FlowActivityImpl fa1 = createFA("FA-1");
         fa1.addPropertyDefinitions(new FlowPropertyDefinitionBuilder(FS_MORPH_FLOW, Boolean.class).initAutoCreate());
 
         FlowActivityImpl fa2 = createFA("FA-2");
         FlowActivityImpl fa3 = createFA("FA-3");
 
         FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
         flowTestingUtils.getFlowDefinitionsManager().addDefinitions(new FlowImpl(FIRST_FLOW, fa1,fa2,fa3));
 
         flowTestingUtils.getFlowDefinitionsManager().addDefinitions(new FlowImpl(MORPHED_FLOW, fa3, fa2, fa1));
 
         FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
 
         FlowState flowState = flowManagement.startFlowState(FIRST_FLOW, true, null, false);
         assertEquals(flowState.getFlow().getFlowPropertyProviderName(), FIRST_FLOW);
         assertEquals(flowState.getCurrentActivity().getFlowPropertyProviderName(), "FA-1");
         flowState.next();
         assertEquals(flowState.getCurrentActivity().getFlowPropertyProviderName(), "FA-2");
         flowState.morphFlow(MORPHED_FLOW, new HashMap<String,String>());
         fail();
     }
 
     private FlowActivityImpl createFA(String name) {
         FlowActivityImpl activity = new FlowActivityImpl().initInvisible(false);
         activity.setFlowPropertyProviderName(name);
         return activity;
     }
 }
