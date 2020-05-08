 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.wiredwidgets.cow.server.service;
 
 import static org.wiredwidgets.cow.server.transform.v2.bpmn20.Bpmn20ProcessBuilder.PROCESS_EXIT_PROPERTY;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.xml.bind.JAXBElement;
 
 import org.apache.log4j.Logger;
 import org.jbpm.process.audit.JPAProcessInstanceDbLog;
 import org.jbpm.process.audit.ProcessInstanceLog;
 import org.jbpm.process.audit.VariableInstanceLog;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.convert.TypeDescriptor;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 import org.wiredwidgets.cow.server.api.model.v2.*;
 import org.wiredwidgets.cow.server.api.model.v2.Task;
 import org.wiredwidgets.cow.server.api.service.*;
 import org.wiredwidgets.cow.server.api.service.Variable;
 import org.wiredwidgets.cow.server.completion.Evaluator;
 import org.wiredwidgets.cow.server.completion.EvaluatorFactory;
 import org.wiredwidgets.cow.server.completion.ProcessInstanceInfo;
 import org.wiredwidgets.cow.server.repo.ProcessInstanceLogRepository;
 import org.wiredwidgets.cow.server.transform.v2.bpmn20.Bpmn20ProcessBuilder;
 
 
 /**
  *
  * @author FITZPATRICK
  */
 @Transactional
 @Component
 public class ProcessInstanceServiceImpl extends AbstractCowServiceImpl implements ProcessInstanceService {
 
     @Autowired
 	ProcessInstanceLogRepository processInstanceLogRepo;
     
     @Autowired
     ProcessService processService;
     
     @Autowired
     TaskService taskService;
     
     @Autowired
     EvaluatorFactory evaluatorFactory;
        
     public static Logger log = Logger.getLogger(ProcessInstanceServiceImpl.class);
     private static TypeDescriptor JBPM_PROCESS_INSTANCE_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.drools.runtime.process.ProcessInstance.class));
     private static TypeDescriptor JBPM_PROCESS_INSTANCE_LOG_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(ProcessInstanceLog.class));
     //private static TypeDescriptor JBPM_HISTORY_PROCESS_INSTANCE_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.jbpm.api.history.HistoryProcessInstance.class));
     private static TypeDescriptor COW_PROCESS_INSTANCE_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(ProcessInstance.class));
 
   
     @Override
     public String executeProcess(ProcessInstance instance) {
     	
         Map<String, Object> vars = new HashMap<String, Object>();
         Map<String, Object> processVars = new HashMap<String, Object>();
         
         //content.put("content", new HashMap<String,Object>());
         if (instance.getVariables() != null) {
             for (Variable variable : instance.getVariables().getVariables()) {
                 vars.put(variable.getName(), variable.getValue());
             }
         }
         // COW-65 save history for all variables
         // org.jbpm.api.ProcessInstance pi = executionService.startProcessInstanceByKey(instance.getProcessDefinitionKey(), vars);
         
         if (vars.size() > 0) {
         	processVars.put(Bpmn20ProcessBuilder.VARIABLES_PROPERTY, vars);
         }
         
         if (instance.getName() != null) {
         	processVars.put(Bpmn20ProcessBuilder.PROCESS_INSTANCE_NAME_PROPERTY, instance.getName());
         }
                 
         org.drools.runtime.process.ProcessInstance pi = kSession.startProcess(instance.getProcessDefinitionKey(), processVars);
         instance.setId(Long.toString(pi.getId()));
         /*
          * //create the process name as a history-tracked variable if
          * (instance.getName() != null) { //
          * executionService.createVariable(pi.getId(), "_name",
          * instance.getName(), true); vars.put("_name", instance.getName()); }          *
          * setVariables(pi.getId(), vars); // COW-65
          *
          * // add the instance id as a variable so it can be passed to a
          * subprocess // executionService.createVariable(pi.getId(), "_id",
          * pi.getId(), false);
          *
          * if (instance.getPriority() != null) {
          * updateProcessInstancePriority(instance.getPriority().intValue(), pi);
          * }
          */
         
         // construct a process instance ID in the required format
         return pi.getProcessId() + "." + Long.toString(pi.getId());
     }
 
     @Transactional(readOnly = true)
     @Override
     public ProcessInstance getProcessInstance(Long id) {
     	return converter.convert(JPAProcessInstanceDbLog.findProcessInstance(id), ProcessInstance.class);
     }
 
     //@Transactional(readOnly = true)
     @Override
     public List<ProcessInstance> findAllProcessInstances() {
         return this.getCOWProcessInstances();
     }
 
     @Override
     public boolean deleteProcessInstance(Long id) {
     	org.drools.runtime.process.ProcessInstance instance = kSession.getProcessInstance(id);
     	if (instance == null) {
     		return false;
     	}
     	else {
 	        try {
 	            kSession.abortProcessInstance(id);
 	            return true;
 	        } catch (IllegalArgumentException e) {
 	            log.error(e);
 	            return false;
 	        }
     	}
     }
 
     @Override
     public void deleteProcessInstancesByKey(String key) {
         List<ProcessInstanceLog> procList = processInstanceLogRepo.findByProcessId(key);
         for (ProcessInstanceLog pil : procList) {
             try {
                kSession.abortProcessInstance(pil.getId());
             } catch (IllegalArgumentException e) {
                 log.error(e);
             }
         }
     }
 
     @Transactional(readOnly = true)
     @Override
     public List<ProcessInstance> findProcessInstancesByKey(String key) {
         List<ProcessInstanceLog> processInstances = JPAProcessInstanceDbLog.findActiveProcessInstances(key);
         return this.convertProcessInstanceLogs(processInstances);
     }
 
     @Override
     public boolean updateProcessInstance(ProcessInstance instance) {
         return false;//throw new UnsupportedOperationException("Not supported yet.");
     }
     
     @Override
 	public void signalProcessInstance(long id, String signal, String value) {
     	kSession.signalEvent(signal, value, id);
     }
 
     @Override
     public ProcessInstance getProcessInstanceStatus(Long processInstanceId) {
 		ProcessInstanceLog pil = JPAProcessInstanceDbLog.findProcessInstance(processInstanceId);
 		String exitValue = getProcessInstanceVariable(processInstanceId, PROCESS_EXIT_PROPERTY);
 		org.wiredwidgets.cow.server.api.model.v2.Process process = processService.getV2Process(pil.getProcessId());
 		ProcessInstanceInfo info = new ProcessInstanceInfo(taskService.getHistoryActivities(processInstanceId), pil.getStatus(), 
 				getProcessInstanceVariables(processInstanceId));
 		
 		String instanceId = process.getKey() + "." + processInstanceId;
 		
 		Evaluator evaluator = evaluatorFactory.getProcessEvaluator(instanceId, process, info);
 		evaluator.evaluate();
 		
 		ProcessInstance pi = getProcessInstance(processInstanceId);
 		pi.setProcess(process);
 		pi.getStatusSummaries().addAll(info.getStatusSummary());
         return pi;
     }
 
     @Override
     public List<ProcessInstance> findAllHistoryProcessInstances() {
         return this.convertProcessInstanceLogs(processInstanceLogRepo.findByStatus(2));
     }
 
     @Override
     public List<ProcessInstance> findHistoryProcessInstances(String key, Date endedAfter, boolean ended) {
         return this.convertProcessInstanceLogs(findJbpmHistoryProcessInstances(key, endedAfter, ended));
     }
  
     @SuppressWarnings("unchecked")
 	private List<ProcessInstance> convertProcessInstances(List<org.drools.runtime.process.ProcessInstance> source) {
         return (List<ProcessInstance>) converter.convert(source, JBPM_PROCESS_INSTANCE_LIST, COW_PROCESS_INSTANCE_LIST);
     }
 
     @SuppressWarnings("unchecked")
     private List<ProcessInstance> convertProcessInstanceLogs(List<ProcessInstanceLog> source) {
         return (List<ProcessInstance>) converter.convert(source, JBPM_PROCESS_INSTANCE_LOG_LIST, COW_PROCESS_INSTANCE_LIST);
     }
 
     private List<ProcessInstance> getCOWProcessInstances() {
         //Collection<org.drools.runtime.process.ProcessInstance> processColl = kSession.getProcessInstances();
         List<ProcessInstanceLog> allProcessInstances = JPAProcessInstanceDbLog.findProcessInstances();
         List<ProcessInstanceLog> activeProcessInstances = new ArrayList<ProcessInstanceLog>();
         
         for (ProcessInstanceLog processInstance: allProcessInstances){
             if (processInstance.getEnd() == null){
                 activeProcessInstances.add(processInstance);
             } 
         }
         return this.convertProcessInstanceLogs(activeProcessInstances);
 
     }
     
     private List<ProcessInstanceLog> findJbpmHistoryProcessInstances(String key, Date endedAfter, boolean ended) {
         List<ProcessInstanceLog> instances = new ArrayList<ProcessInstanceLog>();
 
         if(key != null  && !key.trim().equals("")){
             if (endedAfter != null){
                 instances.addAll(processInstanceLogRepo.findByProcessIdAndStatusAndEndAfter(key, 2, endedAfter));
             } else{
                 instances.addAll(processInstanceLogRepo.findByProcessIdAndStatus(key, 2));
             }
         } else {
             if (endedAfter != null){
                 instances.addAll(processInstanceLogRepo.findByStatusAndEndAfter(2, endedAfter));
             } else{
                 instances.addAll(processInstanceLogRepo.findByStatus(2));                
             }
         }
         return instances;
     }
     
     private String getProcessInstanceVariable(Long id, String name) {
         List<VariableInstanceLog> vars = JPAProcessInstanceDbLog.findVariableInstances(id, name);
         String value = null;       
         if (vars != null && vars.size() > 0 ){
             value = vars.get(0).getValue();
         }    	
         return value;
     }
    
     private Map<String, String> getProcessInstanceVariables(Long id) {
     	Map<String, String> vars = new HashMap<String, String>();
     	List<VariableInstanceLog> logs = JPAProcessInstanceDbLog.findVariableInstances(id);
     	for (VariableInstanceLog log : logs) {
     		vars.put(log.getVariableId(), log.getValue());
     	}
     	return vars;
     }
     
 }
