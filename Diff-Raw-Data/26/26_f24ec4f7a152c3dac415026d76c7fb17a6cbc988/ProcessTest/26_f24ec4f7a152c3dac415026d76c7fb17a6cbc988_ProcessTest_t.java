 package org.jbpm.homeloan;
 
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.drools.runtime.process.ProcessInstance;
 import org.jbpm.test.JbpmJUnitTestCase;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * This is a sample file to test a process.
  */
 public class ProcessTest extends JbpmJUnitTestCase {
     private StatefulKnowledgeSession ksession;
 
     @Before
     public void setupTestCase() {
         ksession = createKnowledgeSession("homeloan.bpmn2");
         ksession.getWorkItemManager().registerWorkItemHandler("CreditReportNode", new CreditReportNodeWorkItemHandler());
         ksession.getWorkItemManager().registerWorkItemHandler("PrequalificationNode", new PrequalificationNodeWorkItemHandler());
     }
 
     @Test
     public void testProcess() {
         final ProcessInstance processInstance = ksession.startProcess("mortgages.homeloan");
 
         // Check whether the process instance has completed successfully.
         assertProcessInstanceCompleted(processInstance.getId(), ksession);
        assertNodeTriggered(processInstance.getId(), "Read application", "Credit Report Node", "Prequalification Node", "Communicate approval");
     }
 
 }
