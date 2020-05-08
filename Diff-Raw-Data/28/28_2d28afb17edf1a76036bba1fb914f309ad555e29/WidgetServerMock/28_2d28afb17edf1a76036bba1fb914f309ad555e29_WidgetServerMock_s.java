 /*******************************************************************************
  * Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 
 package mocks;
 
 import models.ServerNode;
 import models.Widget;
 import models.WidgetInstance;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import server.WidgetServer;
 
 public class WidgetServerMock implements WidgetServer {
     private static Logger logger = LoggerFactory.getLogger(WidgetServerMock.class);
 
     public void undeploy(String instanceId) {
         logger.info("undeploying : {}", instanceId);
     }
 
     @Override
     public Widget.Status getWidgetStatus(ServerNode serverNode) {
         logger.info("getting widget status : {}",  serverNode.getNodeId());
         return null;
     }
 
 	@Override
	public WidgetInstance deploy(Widget widget, ServerNode server) {
 		logger.info("deploying : {}", server.getPublicIP());
 		return null;
 	}
 }
