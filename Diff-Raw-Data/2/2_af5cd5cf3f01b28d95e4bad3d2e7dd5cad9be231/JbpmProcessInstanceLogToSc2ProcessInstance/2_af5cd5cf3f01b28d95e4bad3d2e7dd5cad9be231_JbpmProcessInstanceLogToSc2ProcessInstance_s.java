 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.wiredwidgets.cow.server.convert;
 
 import static org.drools.runtime.process.ProcessInstance.STATE_ABORTED;
 import static org.drools.runtime.process.ProcessInstance.STATE_ACTIVE;
 import static org.drools.runtime.process.ProcessInstance.STATE_COMPLETED;
 import static org.drools.runtime.process.ProcessInstance.STATE_PENDING;
 import static org.drools.runtime.process.ProcessInstance.STATE_SUSPENDED;
 import static org.wiredwidgets.cow.server.transform.v2.bpmn20.Bpmn20ProcessBuilder.PROCESS_INSTANCE_NAME_PROPERTY;
 
 import java.util.List;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.jbpm.process.audit.JPAProcessInstanceDbLog;
 import org.jbpm.process.audit.ProcessInstanceLog;
 import org.jbpm.process.audit.VariableInstanceLog;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.wiredwidgets.cow.server.api.service.ProcessInstance;
 
 /**
  *
  * @author FITZPATRICK
  */
 @Component
 public class JbpmProcessInstanceLogToSc2ProcessInstance extends AbstractConverter<ProcessInstanceLog, ProcessInstance>{
 	
     //@Autowired
     //protected StatefulKnowledgeSession kSession;	
 
     @Override
     public ProcessInstance convert(ProcessInstanceLog source) {
         ProcessInstance target = new ProcessInstance();
         target.setProcessDefinitionId(source.getProcessId());
         target.setKey(source.getProcessId());
 
         // for compatibility with REST API, preserve the JBPM 4.x convention
         // where the process instance ID = processID + "." + id
         target.setId(source.getProcessId() + "." + Long.toString(source.getProcessInstanceId()));
         long parentInstanceId = source.getParentProcessInstanceId();
         if (parentInstanceId > 0) {
         	ProcessInstanceLog parent = JPAProcessInstanceDbLog.findProcessInstance(parentInstanceId);
         	target.setParentId(parent.getProcessId() + "." + parentInstanceId);
         }
         target.setStartTime(convert(source.getStart(), XMLGregorianCalendar.class));
         target.setEndTime(convert(source.getEnd(), XMLGregorianCalendar.class));
         
         switch (source.getStatus()) {
         	case STATE_ABORTED :
         		target.setState("aborted");
         		break;
         	case STATE_ACTIVE :
         		target.setState("active");
         		break;
         	case STATE_COMPLETED :
         		target.setState("completed");
         		break;
         	case STATE_PENDING :
         		target.setState("pending");
         		break;
         	case STATE_SUSPENDED :
         		target.setState("suspended");
         		break;
         }
         
         // process instance name
         // WorkflowProcessInstance pi = (WorkflowProcessInstance) kSession.getProcessInstance(source.getProcessInstanceId());
        target.setName(getVariable(source.getId(), PROCESS_INSTANCE_NAME_PROPERTY));
                             
         return target;
     }
     
     private String getVariable(Long id, String name) {
         List<VariableInstanceLog> vars = JPAProcessInstanceDbLog.findVariableInstances(id, name);
         String value = null;       
         if (vars != null && vars.size() > 0 ){
             value = vars.get(0).getValue();
         }    	
         return value;
     }
     
 }
