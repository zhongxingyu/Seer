 /**
  * Copyright 2010 OpenEngSB Division, Vienna University of Technology
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.openengsb.opencit.core.projectmanager.internal;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openengsb.core.common.context.ContextCurrentService;
 import org.openengsb.core.common.workflow.WorkflowException;
 import org.openengsb.core.common.workflow.WorkflowService;
 import org.openengsb.domain.scm.ScmDomain;
 
 public class ScmStatePoller {
 
     private Log log = LogFactory.getLog(this.getClass());
 
     private ScmDomain scm;
 
     private String projectId;
 
     private WorkflowService workflowService;
 
     private ContextCurrentService contextService;
 
     private boolean stopped = false;
 
     private long timeout;
 
     public void start() {
         Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
             @Override
             public void run() {
                 stop();
             }
         }));
         new Thread(new Runnable() {
             @Override
             public void run() {
                 doRun();
             }
         }).start();
     }
 
     public void doRun() {
         log.info("ScmStatePoller started");
         while (!stopped) {
             if (scm.poll()) {
                 log.info("ScmStatePoller found SCM change - starting CI & T workflow.");
                 runFlow();
             } else {
                 waitForFixedTime();
             }
         }
     }
 
    private synchronized void waitForFixedTime() {
         try {
             wait(timeout);
         } catch (InterruptedException ie) {
             log.warn("ScmStatePoller was interrupted.", ie);
             Thread.interrupted();
         }
     }
 
     private void runFlow() {
         contextService.setThreadLocalContext(projectId);
         try {
             workflowService.startFlow("ci");
         } catch (WorkflowException e) {
             throw new RuntimeException(e);
         }
     }
 
    public synchronized void stop() {
         log.info("ScmStatePoller stopped");
         stopped = true;
         notifyAll();
     }
 
     public void setContextService(ContextCurrentService contextService) {
         this.contextService = contextService;
     }
 
     public void setProjectId(String projectId) {
         this.projectId = projectId;
     }
 
     public void setScm(ScmDomain scm) {
         this.scm = scm;
     }
 
     public void setWorkflowService(WorkflowService workflowService) {
         this.workflowService = workflowService;
     }
 
     public void setTimeout(long timeout) {
         this.timeout = timeout;
     }
 
 }
