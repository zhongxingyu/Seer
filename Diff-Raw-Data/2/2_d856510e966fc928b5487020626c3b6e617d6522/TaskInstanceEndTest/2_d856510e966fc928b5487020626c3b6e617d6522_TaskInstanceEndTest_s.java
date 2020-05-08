 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jbpm.taskmgmt.exe;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.jbpm.graph.def.Node;
 import org.jbpm.graph.def.ProcessDefinition;
 import org.jbpm.graph.exe.ProcessInstance;
 import org.jbpm.graph.exe.Token;
 
 public class TaskInstanceEndTest extends TestCase {
 
   public void testStartTaskInstanceStressed() {
     ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
       "<process-definition>" +
       "  <start-state>" +
       "    <task name='get it going' />" +
       "    <transition name='stressed' to='calm down' />" +
      "    <transition name='relaxed' to='task first things first' />" +
       "  </start-state>" +
       "  <state name='calm down' />" +
       "  <state name='first things first' />" +
       "</process-definition>"
     );
     
     ProcessInstance processInstance = new ProcessInstance(processDefinition);
     TaskInstance startTaskInstance = processInstance.getTaskMgmtInstance().createStartTaskInstance();
     startTaskInstance.end("stressed");
 
     Node calmDown = processDefinition.getNode("calm down");
     assertEquals(calmDown, processInstance.getRootToken().getNode());
   }
 
   public void testStartTaskInstanceRelaxed() {
     ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
       "<process-definition>" +
       "  <start-state>" +
       "    <task name='get it going' />" +
       "    <transition name='stressed' to='calm down' />" +
       "    <transition name='relaxed' to='first things first' />" +
       "  </start-state>" +
       "  <state name='calm down' />" +
       "  <state name='first things first' />" +
       "</process-definition>"
     );
     
     ProcessInstance processInstance = new ProcessInstance(processDefinition);
     TaskInstance startTaskInstance = processInstance.getTaskMgmtInstance().createStartTaskInstance();
     startTaskInstance.end("relaxed");
 
     Node ftf = processDefinition.getNode("first things first");
     assertEquals(ftf, processInstance.getRootToken().getNode());
   }
 
   public void testTaskNodeStressed() {
     ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
       "<process-definition>" +
       "  <start-state>" +
       "    <transition to='get it going' />" +
       "  </start-state>" +
       "  <task-node name='get it going'>" +
       "    <task name='get it going' />" +
       "    <transition name='stressed' to='calm down' />" +
       "    <transition name='relaxed' to='first things first' />" +
       "  </task-node>" +
       "  <state name='calm down' />" +
       "  <state name='first things first' />" +
       "</process-definition>"
     );
     
     ProcessInstance processInstance = new ProcessInstance(processDefinition);
     Token rootToken = processInstance.getRootToken();
     processInstance.signal();
     List unfinishedTasks = new ArrayList(processInstance.getTaskMgmtInstance().getUnfinishedTasks(rootToken));
     assertNotNull(unfinishedTasks);
     assertEquals(1, unfinishedTasks.size());
     TaskInstance startTaskInstance = (TaskInstance) unfinishedTasks.get(0); 
     startTaskInstance.end("stressed");
 
     Node calmDown = processDefinition.getNode("calm down");
     assertEquals(calmDown, processInstance.getRootToken().getNode());
   }
 
   public void testTaskNodeRelaxed() {
     ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
       "<process-definition>" +
       "  <start-state>" +
       "    <transition to='get it going' />" +
       "  </start-state>" +
       "  <task-node name='get it going'>" +
       "    <task name='get it going' />" +
       "    <transition name='stressed' to='calm down' />" +
       "    <transition name='relaxed' to='first things first' />" +
       "  </task-node>" +
       "  <state name='calm down' />" +
       "  <state name='first things first' />" +
       "</process-definition>"
     );
     
     ProcessInstance processInstance = new ProcessInstance(processDefinition);
     Token rootToken = processInstance.getRootToken();
     processInstance.signal();
     List unfinishedTasks = new ArrayList(processInstance.getTaskMgmtInstance().getUnfinishedTasks(rootToken));
     assertNotNull(unfinishedTasks);
     assertEquals(1, unfinishedTasks.size());
     TaskInstance startTaskInstance = (TaskInstance) unfinishedTasks.get(0); 
     startTaskInstance.end("relaxed");
 
     Node ftf = processDefinition.getNode("first things first");
     assertEquals(ftf, rootToken.getNode());
   }
 
   public void testLastTaskNotSignalling() {
     ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
       "<process-definition>" +
       "  <start-state>" +
       "    <transition to='review' />" +
       "  </start-state>" +
       "  <task-node name='review'>" +
       "    <task name='primary review' />" +
       "    <task name='required secondary review' />" +
       "    <task name='optional secondary review' signalling='false' />" +
       "    <transition to='next phase' />" +
       "  </task-node>" +
       "  <state name='next phase' />" +
       "</process-definition>"
     );
     
     ProcessInstance processInstance = new ProcessInstance(processDefinition);
     Token rootToken = processInstance.getRootToken();
     processInstance.signal();
 
     assertEquals("review", rootToken.getNode().getName());
     
     Collection unfinishedTasks = processInstance.getTaskMgmtInstance().getUnfinishedTasks(rootToken);
     assertNotNull(unfinishedTasks);
     assertEquals(3, unfinishedTasks.size());
     TaskInstance taskInstance = findTaskInstance("primary review", unfinishedTasks); 
     taskInstance.end();
 
     assertEquals("review", rootToken.getNode().getName());
 
     unfinishedTasks = processInstance.getTaskMgmtInstance().getUnfinishedTasks(rootToken);
     assertNotNull(unfinishedTasks);
     assertEquals(2, unfinishedTasks.size());
     taskInstance = findTaskInstance("required secondary review", unfinishedTasks); 
     taskInstance.end();
 
     assertEquals("next phase", rootToken.getNode().getName());
   }
   
   public void testEndTasksAttribute() {
 	    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
 	      "<process-definition>" +
 	      "  <start-state>" +
 	      "    <transition to='task1' />" +
 	      "  </start-state>" +
 	      "  <task-node name='task1'>" +
 	      "    <task name='another task' blocking='false'/>" +
 	      "    <transition to='task2' />" +
 	      "  </task-node>" +
 	      "  <task-node name='task2' end-tasks='true' signal='first'>" +
 	      "    <task name='task one' blocking='false' />" +
 	      "    <task name='task two' blocking='false' />" +
 	      "    <transition to='next phase' />" +
 	      "  </task-node>" +
 	      "  <state name='next phase' />" +
 	      "</process-definition>"
 	    );
 	    
 	    ProcessInstance processInstance = new ProcessInstance(processDefinition);
 	    Token rootToken = processInstance.getRootToken();
 	    processInstance.signal();
 
 	    assertEquals("task1", rootToken.getNode().getName());
 	    
 	    processInstance.signal();
 	    assertEquals("task2", rootToken.getNode().getName());
 	    
 		 Collection unfinishedTasks;
 		 unfinishedTasks = processInstance.getTaskMgmtInstance().getUnfinishedTasks(rootToken);
 	    assertNotNull(unfinishedTasks);
 	    assertEquals(3, unfinishedTasks.size());
 
 	    TaskInstance taskInstance = findTaskInstance("task one", unfinishedTasks); 
 	    taskInstance.end();
 
 	    int n = 0;
 		 Collection tasks = processInstance.getTaskMgmtInstance().getTaskInstances();
 	    assertNotNull(unfinishedTasks);
 	    for (Iterator it = tasks.iterator(); it.hasNext();) {
 	   	 TaskInstance ti = (TaskInstance)it.next();
 	   	 if (!ti.hasEnded()) n++;
 	    }
 	    System.out.println("There are " + n + " unfinished tasks");
 	    assertEquals(1, n);
 
 	  }
 
 
   private TaskInstance findTaskInstance(String taskName, Collection taskInstances) {
     TaskInstance taskInstance = null;
     Iterator iter = taskInstances.iterator();
     while (iter.hasNext()) {
       TaskInstance candidate = (TaskInstance) iter.next();
       if (taskName.equals(candidate.getName())) {
         taskInstance = candidate;
       }
     }
     return taskInstance;
   }
 }
