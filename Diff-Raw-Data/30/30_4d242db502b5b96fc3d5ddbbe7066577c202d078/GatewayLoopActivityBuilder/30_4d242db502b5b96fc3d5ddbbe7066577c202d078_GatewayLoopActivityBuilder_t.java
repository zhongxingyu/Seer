 /**
  * Approved for Public Release: 10-4800. Distribution Unlimited.
  * Copyright 2011 The MITRE Corporation,
  * Licensed under the Apache License,
  * Version 2.0 (the "License");
  *
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied.
  *
  * See the License for the specific language governing permissions and limitations under the License.
  */
 
 package org.wiredwidgets.cow.server.transform.v2;
 
 import org.apache.log4j.Logger;
 import org.wiredwidgets.cow.server.api.model.v2.Loop;
 
 /**
  * A Loop is implemented as an Activity followed by a Task in which a decision is made
  * This requires multiple lastNode instances
  * @author JKRANES
  */
 public class GatewayLoopActivityBuilder extends ActivityBuilderImpl<Loop> {
 
     private static Logger log = Logger.getLogger(GatewayLoopActivityBuilder.class);
 
     public GatewayLoopActivityBuilder(ProcessContext context, Loop loop, ActivityBuilderFactory factory) {
         super(context, loop, factory);
     }
 
     @Override
     public void build() {
         Loop source = getActivity();
 
         // start with a converging gateway
         NodeBuilder convergingGatewayBuilder = this.createNodeBuilder(getContext(), source, NodeType.CONVERGING_EXCLUSIVE_GATEWAY);
         convergingGatewayBuilder.build(this);
         setLinkTarget(convergingGatewayBuilder);
 
         ActivityBuilder activityBuilder = createActivityBuilder(source.getActivity().getValue());
              
         activityBuilder.build(this);
         convergingGatewayBuilder.link(activityBuilder);
 
         // Following the Activity we insert a Task for making a decision
         // whether to repeat the Activity or continue
         
         // ActivityBuilder loopTaskBuilder = createActivityBuilder(source.getLoopTask());
         NodeBuilder loopTaskBuilder = createNodeBuilder(getContext(), source, NodeType.LOOP_TASK);
         loopTaskBuilder.build(this);
  
         activityBuilder.link(loopTaskBuilder);
 
         // following the task we have a diverging gateway
         NodeBuilder divergingGatewayBuilder = this.createNodeBuilder(getContext(), source, NodeType.DIVERGING_EXCLUSIVE_GATEWAY);
         
         
         // a bit of a hack to inject the name of the decision variable into the gateway builder
         divergingGatewayBuilder.setBuildProperty("decisionVar", loopTaskBuilder.getBuildProperty("decisionVar"));     
                    
         divergingGatewayBuilder.build(this);
 
         loopTaskBuilder.link(divergingGatewayBuilder);
 
         // Link the diverging gateway back to the converging gateway
         divergingGatewayBuilder.link(convergingGatewayBuilder, source.getRepeatName());
 
         setLinkSource(divergingGatewayBuilder);
        
        // Use a named transition for the continue path
        // This name should be used in the UI
        // NOTE: this MUST go after the build() call
        // loopTaskBuilder.setLinkTransitionName(source.getDoneName());
        divergingGatewayBuilder.setLinkTransitionName(source.getDoneName());
     }
 }
