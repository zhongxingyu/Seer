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
 package org.apache.servicemix.jbi.management.task;
 
import javax.jbi.management.DeploymentServiceMBean;

 import org.apache.servicemix.jbi.framework.AdminCommandsServiceMBean;
 import org.apache.tools.ant.Project;
 
 
 /**
  *
  * JbiTaskTest
  * @version $Revision$
  */
 public class JbiTaskTest extends JbiTaskSupport {
    
     private JbiTask jbiTask;
     
     /*
      * @see TestCase#setUp()
      */
     protected void setUp() throws Exception {
         super.setUp();        
         jbiTask = new JbiTask(){
             protected void doExecute(AdminCommandsServiceMBean acs) throws Exception {
             }
         };
         jbiTask.setProject(new Project());
         jbiTask.init();
         jbiTask.connect();
     }
 
     /*
      * @see TestCase#tearDown()
      */
     protected void tearDown() throws Exception {
         jbiTask.close();
         super.tearDown();
     }
 
    public void testGetDeploymentService() throws Exception {
        DeploymentServiceMBean mbean = jbiTask.getDeploymentService();
        assertNotNull(mbean);
        mbean.getDeployedServiceAssemblies();
    }
    
     public void testGetAdminCommandsService() throws Exception {
         AdminCommandsServiceMBean mbean = jbiTask.getAdminCommandsService();
         assertNotNull(mbean);
     }
 }
