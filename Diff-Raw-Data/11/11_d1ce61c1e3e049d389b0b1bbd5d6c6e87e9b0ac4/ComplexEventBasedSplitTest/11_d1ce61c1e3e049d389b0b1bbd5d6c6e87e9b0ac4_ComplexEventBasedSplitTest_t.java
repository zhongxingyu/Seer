 package org.jboss.tools.bpmn2.ui.bot.complex.test.testcase;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.Arrays;
 
 import org.jboss.tools.bpmn2.reddeer.editor.ElementType;
 import org.jboss.tools.bpmn2.reddeer.editor.jbpm.activities.UserTask;
 import org.jboss.tools.bpmn2.reddeer.editor.jbpm.catchevents.SignalIntermediateCatchEvent;
 import org.jboss.tools.bpmn2.reddeer.editor.jbpm.gateways.Direction;
 import org.jboss.tools.bpmn2.reddeer.editor.jbpm.gateways.EventBasedGateway;
 import org.jboss.tools.bpmn2.ui.bot.complex.test.JBPM6ComplexTest;
 import org.jboss.tools.bpmn2.ui.bot.complex.test.TestPhase;
 import org.jboss.tools.bpmn2.ui.bot.complex.test.JBPM6ComplexTestDefinitionRequirement.JBPM6ComplexTestDefinition;
 import org.jboss.tools.bpmn2.ui.bot.complex.test.TestPhase.Phase;
 import org.jboss.tools.bpmn2.ui.bot.test.jbpm.JbpmAssertions;
 import org.jboss.tools.bpmn2.ui.bot.test.jbpm.PersistenceWorkItemHandler;
 import org.jboss.tools.bpmn2.ui.bot.test.jbpm.TriggeredNodesListener;
 import org.kie.api.runtime.KieSession;
 import org.kie.api.runtime.process.ProcessInstance;
 import org.kie.api.runtime.process.WorkItem;
 import org.kie.api.runtime.process.WorkflowProcessInstance;
 
 @JBPM6ComplexTestDefinition(projectName="JBPM6ComplexTest",
 							importFolder="resources/bpmn2/model/base",
 							openFile="BaseBPMN2-EventBasedSplit.bpmn2",
							saveAs="BPMN2-EventBasedSplit.bpmn2",
							knownIssues={"1184422"})
 public class ComplexEventBasedSplitTest extends JBPM6ComplexTest{
 
 	@TestPhase(phase=Phase.MODEL)
 	public void model() {
 		UserTask task1 = new UserTask("Email1");
 		
 		EventBasedGateway gateway1 = (EventBasedGateway) task1.append("Split", ElementType.EVENT_BASED_GATEWAY);
 		gateway1.setDirection(Direction.DIVERGING);
 		gateway1.connectTo(new SignalIntermediateCatchEvent("Event1"));
 		gateway1.connectTo(new SignalIntermediateCatchEvent("Event2"));
 	}
 	
 	@TestPhase(phase=Phase.RUN)
 	public void run(KieSession kSession) {
 		PersistenceWorkItemHandler handler = new PersistenceWorkItemHandler();
 		kSession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
 		
 		TriggeredNodesListener triggered = new TriggeredNodesListener(
 				Arrays.asList("StartProcess", "Email1", "Split", "Script1", "Join", "Script", "Email2", "EndProcess"), Arrays.asList("Script2"));
 		kSession.addEventListener(triggered);
 		
 		ProcessInstance processInstance = kSession.startProcess("BPMN2EventBasedSplit");
 		
 		WorkItem item = handler.getWorkItem("Email1");
 		handler.completeWorkItem(item, kSession.getWorkItemManager());
 		kSession.signalEvent("Signal1", "Signal1 sended");
 		item = handler.getWorkItem("Email2");
 		handler.completeWorkItem(item, kSession.getWorkItemManager());
 		
 		JbpmAssertions.assertProcessInstanceCompleted(processInstance, kSession);
 		assertEquals("Process variable "+VARIABLE1+" didn't changed.", "Signal1 sended", ((WorkflowProcessInstance) processInstance).getVariable(VARIABLE1));
 	}
 }
