 package org.springframework.integration.activiti.signup;
 
 import org.activiti.engine.impl.bpmn.BpmnActivityBehavior;
 import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
 import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
 import org.springframework.stereotype.Component;
 
 
 @Component
 public class SendEmail extends BpmnActivityBehavior implements ActivityBehavior {
 
 	public void execute(ActivityExecution activityExecution) throws Exception {
 		System.out.println(getClass() + ": sending email ");
 
		this.performDefaultOutgoingBehavior(activityExecution);
 	}
 }
