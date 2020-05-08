 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.cleia.dao.impl;
 
 import com.abada.cleia.dao.PatientDao;
 import com.abada.cleia.dao.ProcessInstanceDao;
 import com.abada.cleia.entity.temporal.PatientHasProcessInstanceInfo;
 import com.abada.cleia.entity.user.Patient;
 import com.abada.cleia.entity.user.PatientHasProcessInstance;
 import com.abada.jbpm.integration.console.ProcessManagement;
 import com.abada.jbpm.integration.console.task.PatientTaskManagement;
 import com.abada.jbpm.process.audit.ProcessInstanceDbLog;
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jbpm.process.audit.ProcessInstanceLog;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author katsu
  */
 public class ProcessInstanceDaoImpl implements PatientTaskManagement, ProcessInstanceDao{
 
     private static final Log logger = LogFactory.getLog(ProcessInstanceDaoImpl.class);
     @PersistenceContext(unitName = "cleiaPU")
     private EntityManager entityManager;
     private PatientDao patientDao;
     private ProcessManagement processManagement;
     private ProcessInstanceDbLog processInstanceDbLog;        
 
     public PatientDao getPatientDao() {
         return patientDao;
     }
 
     public void setPatientDao(PatientDao patientDao) {
         this.patientDao = patientDao;
     }
 
     public ProcessManagement getProcessManagement() {
         return processManagement;
     }
 
     public void setProcessManagement(ProcessManagement processManagement) {
         this.processManagement = processManagement;
     }
 
     public ProcessInstanceDbLog getProcessInstanceDbLog() {
         return processInstanceDbLog;
     }
 
     public void setProcessInstanceDbLog(ProcessInstanceDbLog processInstanceDbLog) {
         this.processInstanceDbLog = processInstanceDbLog;
     }
     
     @Transactional(value = "cleia-txm", readOnly = true)
     public List<Long> getProcessInstancesIdsForPatient(Long patientId) {
         Patient patient=entityManager.find(Patient.class, patientId);
         if (patient!=null && patient.getProcessInstances()!=null){
             List<Long> result=new ArrayList<Long>();            
             for (PatientHasProcessInstance pi:patient.getProcessInstances()){
                 result.add(pi.getProcessInstanceId());
             }
             return result;
         }
         return null;
     }
 
     @Transactional(value = "cleia-txm")
     public void addPInstancePatient(Long patientId, Long processInstanceId) {
         PatientHasProcessInstance add=new PatientHasProcessInstance();        
         add.setProcessInstanceId(processInstanceId);
         Patient patient=entityManager.find(Patient.class, patientId);
         patient.addPatientHasProcessInstance(add);
         entityManager.persist(add);
     }
 
     @Transactional(value = "cleia-txm", readOnly = true)
     public PatientHasProcessInstanceInfo getProcessInstanceFromProcessIntance(Long patientId, Long pInstance) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
     public List<PatientHasProcessInstanceInfo> getProcessInstance(Long patientId) {
         Patient patient=patientDao.getPatientById(patientId);
         if (patient != null && patient.getProcessInstances() != null && !patient.getProcessInstances().isEmpty()) {
             List<PatientHasProcessInstanceInfo> result = new ArrayList<PatientHasProcessInstanceInfo>();
             for (PatientHasProcessInstance og : patient.getProcessInstances()) {
                 //if (og instanceof Oncoguide) {
                 ProcessInstanceLog pil = processInstanceDbLog.findProcessInstance(og.getProcessInstanceId());
                 if (pil != null) {
                     PatientHasProcessInstanceInfo po = new PatientHasProcessInstanceInfo();
                     po.setPatientId(patientId);
                     try {
                         po.setProcessName(processManagement.getProcessDefinition(pil.getProcessId()).getName());
                     } catch (Exception e) {
                         logger.warn("Process without name. " + pil.getProcessId());
                     }
                     po.setProcessId(pil.getProcessId());
                     po.setProcessInstanceId(((Long) pil.getProcessInstanceId()).toString());
                     po.setStart(pil.getStart());
                     po.setEnd(pil.getEnd());
                     result.add(po);
                 }
                 //}
             }
             return result;
         }
         return null;
     }
     
 }
