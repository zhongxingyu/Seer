 /*
  * The MIT License
  *
  * Copyright (c) 2012, Jiri Svitak
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package org.jenkinsci.plugins.jbpm;
 
 import hudson.model.ParameterValue;
 import hudson.model.Result;
 import hudson.model.AbstractProject;
 import hudson.model.Cause;
 import hudson.model.Hudson;
 import hudson.model.ParametersAction;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Future;
 
 import org.drools.command.Context;
 import org.drools.command.impl.GenericCommand;
 import org.drools.command.impl.KnowledgeCommandContext;
 import org.drools.process.instance.impl.WorkItemImpl;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.drools.runtime.process.NodeInstance;
 import org.drools.runtime.process.NodeInstanceContainer;
 import org.drools.runtime.process.ProcessInstance;
 import org.drools.runtime.process.WorkItem;
 import org.drools.runtime.process.WorkItemHandler;
 import org.drools.runtime.process.WorkItemManager;
 import org.drools.runtime.process.WorkflowProcessInstance;
 import org.jbpm.process.workitem.AbstractWorkItemHandler;
 import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
 
 /**
  * 
  * Implements a handler for the JenkinsJob work item, which launches jobs and
  * waits for their completion.
  * 
  * @author Jiri Svitak
  * 
  */
 @SuppressWarnings("rawtypes")
 public class JenkinsJobWorkItemHandler implements WorkItemHandler {
 
     private static StatefulKnowledgeSession session = null;
     
     public static void setSession(StatefulKnowledgeSession ksession) {
         session = ksession;
     }
     
     public void executeWorkItem(final WorkItem workItem,
             final WorkItemManager workItemManager) {
 
         new Thread(new Runnable() {
 
             public void run() {
 
                 String jobName;
                 if (SessionUtil.isPersistenceEnabled()) {
                     jobName = getNameFromPersistedSession(workItem);
                 } else {
                     jobName = ((AbstractWorkItemHandler) workItemManager)
                             .getNodeInstance(workItem).getNodeName();
                 }
 
                 JbpmPluginLogger.info("Entered: " + jobName);
                 Hudson h = Hudson.getInstance();
                 AbstractProject ap = h.getItemByFullName(jobName,
                         AbstractProject.class);
 
                 @SuppressWarnings("unchecked")
                 List<ParameterValue> parameters = (List<ParameterValue>) workItem
                         .getParameter("jobParameters");
 
                 Future future;
                 if (parameters != null) {
                     future = ap.scheduleBuild2(0, new WorkItemCause(),
                             new ParametersAction(parameters));
                 } else {
                     future = ap.scheduleBuild2(0, new WorkItemCause());
                 }
                 Result result = null;
                 synchronized (future) {
                     try {
                         future.wait();
                         result = ap.getBuilds().getLastBuild().getResult();
                     } catch (InterruptedException e) {
                         result = Result.ABORTED;
                         JbpmPluginLogger.error(e.toString());
                     }
                 }
 
                 Map<String, Object> workItemResults = new HashMap<String, Object>();
                 workItemResults.put("jobResult", result);
                workItemManager.completeWorkItem(workItem.getId(),
                         workItemResults);
 
                 JbpmPluginLogger.info("Left: " + jobName + " with result "
                         + result.toString());
 
             }
 
         }).start();
 
     }
 
     public void abortWorkItem(WorkItem workItem, WorkItemManager workItemManager) {
 
     }
 
     public static class WorkItemCause extends Cause {
         @Override
         public String getShortDescription() {
             return "Invoked by jBPM workflow plugin";
         }
     }
     
     private String getNameFromPersistedSession(final WorkItem workItem) {
         return session.execute(new GenericCommand<String> () {
             private static final long serialVersionUID = 1L;
             public String execute(Context context) {
                 StatefulKnowledgeSession ksession = ((KnowledgeCommandContext) context).getStatefulKnowledgesession();
                 ProcessInstance processInstance = ksession.getProcessInstance(((WorkItemImpl) workItem).getProcessInstanceId());
                 NodeInstance nodeInstance = findWorkItemNodeInstance(workItem.getId(), ((WorkflowProcessInstance) processInstance).getNodeInstances());
                 return nodeInstance.getNodeName();
             }
         
         });
     }
     
     private static WorkItemNodeInstance findWorkItemNodeInstance(long workItemId, Collection<NodeInstance> nodeInstances) {
         for (NodeInstance nodeInstance: nodeInstances) {
             if (nodeInstance instanceof WorkItemNodeInstance) {
                 WorkItemNodeInstance workItemNodeInstance = (WorkItemNodeInstance) nodeInstance;
                 if (workItemId == workItemNodeInstance.getWorkItem().getId()) {
                     return workItemNodeInstance;
                 }
             }
             if (nodeInstance instanceof NodeInstanceContainer) {
                 WorkItemNodeInstance result = findWorkItemNodeInstance(workItemId, ((NodeInstanceContainer) nodeInstance).getNodeInstances());
                 if (result != null) {
                     return result;
                 }
             }
         }
         return null;
     }
 
 }
