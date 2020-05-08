 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.cleia.dao.impl;
 
 /*
  * #%L
  * Cleia
  * %%
  * Copyright (C) 2013 Abada Servicios Desarrollo (investigacion@abadasoft.com)
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/gpl-3.0.html>.
  * #L%
  */
 import com.abada.cleia.dao.PatientDao;
 import com.abada.cleia.dao.UserDao;
 import com.abada.cleia.entity.user.Id;
 import com.abada.cleia.entity.user.Patient;
 import com.abada.springframework.orm.jpa.support.JpaDaoUtils;
 import com.abada.springframework.web.servlet.command.extjs.gridpanel.GridRequest;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import javax.annotation.Resource;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author katsu
  */
 public class PatientDaoImpl extends JpaDaoUtils implements PatientDao {
 
     private static final Log logger = LogFactory.getLog(PatientDaoImpl.class);
     @PersistenceContext(unitName = "cleiaPU")
     private EntityManager entityManager;
     @Resource(name = "userDao")
     private UserDao userDao;
 
     /**
      * obtained from patient id
      *
      * @param patientId
      * @return
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public Patient getPatientById(long patientId) {
         Patient result = entityManager.find(Patient.class, patientId);
         if (result != null) {
             result.getUser().getGroups().size();
             result.getUser().getRoles().size();
             result.getUser().getIds().size();
             result.getProcessInstances().size();
         }
         return result;
     }
 
     /**
      * obtained all patient
      *
      * @return
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public List<Patient> getAllPatients() {
         List<Patient> lpatient = entityManager.createQuery("SELECT p FROM Patient p").getResultList();
         for (Patient patient : lpatient) {
             patient.getUser().getGroups().size();
             patient.getUser().getRoles().size();
             patient.getUser().getIds().size();
             patient.getProcessInstances().size();
         }
         return lpatient;
 
     }
 
     /**
      * Search a list of patients by params
      *
      * @param params
      * @return
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public List<Patient> getPatientUser(GridRequest filters, String username) {
         List<Patient> lp = new ArrayList<Patient>();
         Patient patient = (Patient) entityManager.createQuery("select p from Patient p where p.user.username = :username").setParameter("username", username).getSingleResult();
 
 
         patient.getUser().getGroups().size();
         patient.getUser().getRoles().size();
         patient.getUser().getIds().size();
         patient.getProcessInstances().size();
         lp.add(patient);
 
 
         return lp;
 
     }
 
     /**
      * Gets the size of {@link Patient}
      *
      * @param filters
      * @return Long
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public Long loadSizeAll(GridRequest filters) {
         List<Long> result = this.find(entityManager, "select count(*) from Patient p" + filters.getQL("p", true), filters.getParamsValues());
         return result.get(0);
     }
 
     /**
      * Search a list of patients by params
      *
      * @param params
      * @return
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public List<Patient> getAll(GridRequest filters) {
         List<Patient> lpatient = this.find(entityManager, "select p from Patient p" + filters.getQL("p", true), filters.getParamsValues(), filters.getStart(), filters.getLimit());
         for (Patient patient : lpatient) {
             patient.getUser().getGroups().size();
             patient.getUser().getRoles().size();
             patient.getUser().getIds().size();
             patient.getMedicals().size();
             patient.getProcessInstances().size();
         }
         return lpatient;
     }
 
     @Transactional(value = "cleia-txm", readOnly = true)
     public List<Patient> getAllbyMedical(GridRequest filters,String usernameMedical) {        
         Map<String,Object> params=filters.getParamsValues();
         params.put("usernameMedical", usernameMedical);
         List<Patient> lpatient = this.find(entityManager, "select p from Patient p inner join p.medicals m where m.patient.user.username = :usernameMedical " + filters.getQL("p", false), params, filters.getStart(), filters.getLimit());
         return lpatient;
     }
 
     @Transactional(value = "cleia-txm", readOnly = true)
     public Long loadSizeAllbyMedical(GridRequest filters,String usernameMedical) {        
         Map<String,Object> params=filters.getParamsValues();
         params.put("usernameMedical", usernameMedical);
         List<Long> result = this.find(entityManager, "select count(*) from Patient p inner join p.medicals m where m.patient.user.username = :usernameMedical " + filters.getQL("p", false), params, filters.getStart(), filters.getLimit());
         return result.get(0);
     }
 
     /**
      * setting patient
      *
      * @param patient
      * @param p
      */
     @Transactional("cleia-txm")
     public void updatePatient(Patient patient, Patient p) {
         patient.setGenre(p.getGenre());
         patient.setName(p.getName());
         patient.setAddress(p.getAddress());
         patient.setSurname(p.getSurname());
         patient.setSurname1(p.getSurname1());
         patient.setBirthDay(p.getBirthDay());
         patient.setProcessInstances(p.getProcessInstances());
     }
 
     /**
      * get List Id patient
      *
      * @param idpatient
      * @return
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public List<Id> getIdsForPatient(Long idpatient) {
         List<Patient> lp = entityManager.createQuery("SELECT p FROM Patient p WHERE p.id=?").setParameter(1, idpatient).getResultList();
         if (lp.size() > 0) {
             return lp.get(0).getUser().getIds();
         }
         return null;
     }
 
     /**
      * insert patient
      *
      * @param patient
      * @throws Exception
      */
     @Transactional(value = "cleia-txm")
     public void postPatient(Patient patient) throws Exception {
 
         List<Patient> lpatientid = findPatientsbylisId(patient.getUser().getIds(), Boolean.FALSE);
         if (lpatientid != null && lpatientid.isEmpty()) {
             try {
 
                 if (patient.getUser() != null && patient.getUser().getId() <= 0) {
                     userDao.postUser(patient.getUser());
                     patient.setId(patient.getUser().getId());
                 } else if (patient.getUser() != null) {
                     patient.setId(patient.getUser().getId());
                     patient.getUser().setRoles(entityManager.find(patient.getUser().getClass(), patient.getUser().getId()).getRoles());
                     userDao.putUser(patient.getUser().getId(), patient.getUser());
                     patient.setUser(entityManager.find(patient.getUser().getClass(), patient.getUser().getId()));
                 }
                 entityManager.persist(patient);
 
             } catch (Exception e) {
                 throw new Exception("Error. Ha ocurrido un error al insertar el paciente " + patient.getName() + " " + patient.getSurname() + " " + patient.getSurname1() + " " + e.toString());
             }
         } else {
             throw new Exception("Error. El patient " + patient.getName() + " ya existe con esos identificadores");
         }
     }
 
     /**
      * update patient
      *
      * @param idpatient
      * @param patient
      */
     @Transactional(value = "cleia-txm")
     public void putPatient(Long idpatient, Patient patient) throws Exception {
         Patient patient1 = entityManager.find(Patient.class, idpatient);
         if (patient1 != null) {
 
             try {
                 patient.getUser().setRoles(entityManager.find(patient.getUser().getClass(), patient.getUser().getId()).getRoles());
                 userDao.putUser(patient.getUser().getId(), patient.getUser());
                 this.updatePatient(patient1, patient);
             } catch (Exception e) {
                 throw new Exception("Error. Ha ocurrido un error al modificar el paciente "
                         + patient.getName() + " " + patient.getSurname() + " " + patient.getSurname(), e);
             }
         } else {
             throw new Exception("Error. El paciente no existe");
         }
     }
 
     /**
      * update patient data
      *
      * @param idpatient
      * @param patient
      */
     @Transactional(value = "cleia-txm")
     public void putPatientData(Long idpatient, Patient patient) throws Exception {
         Patient patient1 = entityManager.find(Patient.class, idpatient);
         if (patient1 != null) {
             /*Modificamos el paciente*/
             try {
                 this.updatePatient(patient1, patient);
                 userDao.updateUser(patient1.getUser(), patient.getUser());
             } catch (Exception e) {
                 throw new Exception("Error. Ha ocurrido un error al modificar el paciente "
                         + patient1.getName() + " " + patient1.getSurname() + " " + patient1.getSurname1());
             }
         } else {
             throw new Exception("Error. El paciente no existe");
         }
     }
 
     /**
      * enabled or disable patient
      *
      * @param idpatient
      * @param enable
      */
     @Transactional(value = "cleia-txm")
     public void enableDisablePatient(Long idpatient, boolean enable) throws Exception {
         Patient patient = entityManager.find(Patient.class, idpatient);
         String habilitar = "";
         if (patient != null) {
             if ((!patient.getUser().isEnabled() && enable) || (patient.getUser().isEnabled() && !enable)) {
                 try {
                     patient.getUser().setEnabled(enable);
                 } catch (Exception e) {
                     if (enable) {
                         habilitar = "habilitar";
                     } else {
                         habilitar = "deshabilitar";
                     }
                     throw new Exception("Error. Ha ocurrido un error al" + habilitar + " al paciente " + patient.getName() + " " + patient.getSurname() + " " + patient.getSurname1());
                 }
             } else {
                 if (!enable) {
                     habilitar = "deshabilitado";
                 } else {
                     habilitar = "habilitado";
                 }
                 throw new Exception("Error. El paciente " + patient.getName() + " " + patient.getSurname() + " " + patient.getSurname1() + " ya esta " + habilitar);
             }
         } else {
             throw new Exception("Error. El paciente no existe");
         }
     }
 
     /**
      * find patient by list id
      *
      * @param asList
      * @param object
      */
     @Transactional(value = "cleia-txm")
     public List<Patient> findPatientsbylisId(List<Id> asList, Boolean repeatable) throws Exception {
         List<Patient> p = new ArrayList<Patient>();
         if (asList != null && !asList.isEmpty()) {
             int append = 0;
             StringBuilder query = new StringBuilder();
             query.append("SELECT p FROM Patient p join p.user.ids idss WHERE idss.id in (select distinct pid.id from Id pid where ");
             for (Id pid : asList) {
                 if (pid.getValue() != null && !pid.getValue().equals("") && pid.getType() != null && pid.getType().getValue() != null) {
                     append++;
                     if (append != 1) {
                         query.append(" or ");
                     }
                     query.append("pid.value='").append(pid.getValue()).append("'");
                     if (repeatable != null) {
                         query.append(" and pid.type.repeatable=").append(repeatable);
                     }
 
                     query.append(" and pid.type.value='").append(pid.getType().getValue()).append("'");
                 } else {
                     throw new Exception("Error. Ha ocurrido un error en uno de los identificadores");
                 }
             }
             if (append != 0) {
                 query.append(")");
                 p = entityManager.createQuery(query.toString()).getResultList();
                 for (Patient patient : p) {
                     patient.getUser().getGroups().size();
                     patient.getUser().getRoles().size();
                     patient.getUser().getIds().size();
                     patient.getProcessInstances().size();
                     patient.getMedicals().size();
                 }
 
             }
         }
         return p;
     }
     
      /**
      * Returns a list of patient not medical
      *
      * @return
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public List<Patient> getPatientnotmedical(GridRequest filters) {
         List<Patient> lpatient = this.find(entityManager, "SELECT p FROM Patient p WHERE p.id not in (select distinct m.id from Medical m)" + filters.getQL("p", false), filters.getParamsValues(), filters.getStart(), filters.getLimit());
         for (Patient patient : lpatient) {
             patient.getUser().getGroups().size();
             patient.getUser().getRoles().size();
             patient.getUser().getIds().size();
             patient.getProcessInstances().size();
         }
         return lpatient;
     }
     
      /**
      * Obtiene el tamaño de {@link User}
      *
      * @param filters
      * @return Long
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public Long getPatientnotmedicalsize(GridRequest filters) {
         List<Long> result = this.find(entityManager, "SELECT count(*) FROM Patient p WHERE p.id not in (select distinct m.id from Medical m)" + filters.getQL("p", false), filters.getParamsValues());
         return result.get(0);
     }
 
     @Transactional(value = "cleia-txm", readOnly = true)
     public Patient getPatientByUsername(String username) {
         Patient result=(Patient)this.entityManager.createQuery("select  p from Patient p where p.user.username = :username").setParameter("username", username).getSingleResult();
         if (result != null) {
             result.getUser().getGroups().size();
             result.getUser().getRoles().size();
             result.getUser().getIds().size();
             result.getProcessInstances().size();
         }
         return result;
     }
 }
