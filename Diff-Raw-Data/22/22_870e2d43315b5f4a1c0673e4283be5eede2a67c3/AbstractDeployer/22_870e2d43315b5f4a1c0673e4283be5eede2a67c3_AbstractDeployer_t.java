 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.common;
 
 import org.apache.commons.logging.Log;
 
 import javax.jbi.management.DeploymentException;
 
 /**
  * Base classes for custom artifacts deployers.
  * 
  * @author Guillaume Nodet
  * @version $Revision$
  * @since 3.0
  */
 public abstract class AbstractDeployer implements Deployer {
 
     protected final transient Log logger;
     
     protected BaseComponent component;
     
     public AbstractDeployer(BaseComponent component) {
         this.component = component;
         this.logger = component.logger;
     }
     
    protected DeploymentException failure(String task, String info, Throwable e) {
         ManagementSupport.Message msg = new ManagementSupport.Message();
         msg.setComponent(component.getComponentName());
         msg.setTask(task);
         msg.setResult("FAILED");
         msg.setType("ERROR");
         msg.setMessage(info);
         msg.setException(e);
         return new DeploymentException(ManagementSupport.createComponentMessage(msg));
     }
     
     public void undeploy(ServiceUnit su) throws DeploymentException {
     }
     
 }
