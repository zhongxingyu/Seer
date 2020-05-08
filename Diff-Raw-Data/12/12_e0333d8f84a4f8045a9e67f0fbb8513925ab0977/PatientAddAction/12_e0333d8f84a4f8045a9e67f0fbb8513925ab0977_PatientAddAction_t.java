 /*
  * PatientView
  *
  * Copyright (c) Worth Solutions Limited 2004-2013
  *
  * This file is part of PatientView.
  *
  * PatientView is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  * PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License along with PatientView in a file
  * titled COPYING. If not, see <http://www.gnu.org/licenses/>.
  *
  * @package PatientView
  * @link http://www.patientview.org
  * @author PatientView <info@patientview.org>
  * @copyright Copyright (c) 2004-2013, Worth Solutions Limited
  * @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
  */
 
 package org.patientview.patientview.logon;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
import org.patientview.model.Patient;
 import org.patientview.model.Unit;
 import org.patientview.patientview.logging.AddLog;
 import org.patientview.patientview.model.User;
 import org.patientview.patientview.model.UserMapping;
 import org.patientview.patientview.unit.UnitUtils;
 import org.patientview.patientview.user.UserUtils;
 import org.patientview.service.UnitManager;
 import org.patientview.utils.LegacySpringUtils;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.List;
 
 public class PatientAddAction extends Action {
 
     public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                  HttpServletResponse response) throws Exception {
 
         String username = BeanUtils.getProperty(form, "username");
         String password = LogonUtils.generateNewPassword();
         String gppassword = LogonUtils.generateNewPassword();
         String name = BeanUtils.getProperty(form, "name");
         String email = BeanUtils.getProperty(form, "email");
         String nhsno = BeanUtils.getProperty(form, "nhsno").trim();
         String unitcode = BeanUtils.getProperty(form, "unitcode");
         String overrideDuplicateNhsno = BeanUtils.getProperty(form, "overrideDuplicateNhsno");
         String overrideInvalidNhsno = BeanUtils.getProperty(form, "overrideInvalidNhsno");
         boolean dummypatient = "true".equals(BeanUtils.getProperty(form, "dummypatient"));
 
         UnitManager unitManager = LegacySpringUtils.getUnitManager();
         Unit unit = unitManager.get(unitcode);
         if ("radargroup".equalsIgnoreCase(unit.getSourceType())) {
             request.setAttribute("radarGroupPatient", unit.getName());
             return mapping.findForward("input");
         }
 
         PatientLogon patientLogon =
                 new PatientLogon(username, password, name, email, false, true, dummypatient, null, 0, false);
 
         UserMapping userMapping = new UserMapping(username, unitcode, nhsno);
         UserMapping userMappingPatientEnters = new UserMapping(username, UnitUtils.PATIENT_ENTERS_UNITCODE, nhsno);
 
         PatientLogon gpPatientLogon =
                 new PatientLogon(username + "-GP", gppassword, name + "-GP", null, false, true, dummypatient,
                         null, 0, false);
 
         UserMapping userMappingGp = new UserMapping(username + "-GP", unitcode, nhsno);
 
         User existingUser = LegacySpringUtils.getUserManager().get(username);
 
         List existingPatientsWithSameNhsno = findExistingPatientsWithSameNhsno(nhsno);
 
         String mappingToFind = "";
         if (!"on".equals(overrideInvalidNhsno) && !UserUtils.isNhsNumberValid(nhsno)) {
             request.setAttribute(LogonUtils.INVALID_NHSNO, nhsno);
 
             if (UserUtils.isNhsNumberValidWhenUppercaseLettersAreAllowed(nhsno)) {
                 request.setAttribute(LogonUtils.OFFER_TO_ALLOW_INVALID_NHSNO, nhsno);
             }
 
             mappingToFind = "input";
         }
 
         if (existingUser != null) {
             request.setAttribute(LogonUtils.USER_ALREADY_EXISTS, username);
             patientLogon.setUsername("");
             mappingToFind = "input";
         }
 
         if (existingPatientsWithSameNhsno != null && !existingPatientsWithSameNhsno.isEmpty()
                 && !overrideDuplicateNhsno.equals("on")) {
             for (Object obj : existingPatientsWithSameNhsno) {
                 UserMapping userMappingWithSameNhsno = (UserMapping) obj;
                 if (userMappingWithSameNhsno.getUnitcode().equalsIgnoreCase(unitcode)) {
                     request.setAttribute(LogonUtils.PATIENT_ALREADY_IN_UNIT, nhsno);
                     mappingToFind = "input";
                 }
             }
             if ("".equals(mappingToFind)) {
                 request.setAttribute(LogonUtils.NHSNO_ALREADY_EXISTS, nhsno);
                 request.setAttribute(LogonUtils.PATIENTS_WITH_SAME_NHSNO, existingPatientsWithSameNhsno.get(0));
                 mappingToFind = "samenhsno";
             }
         }
 
         if (mappingToFind.equals("")) {
             PatientLogon hashedPatient = (PatientLogon) patientLogon.clone();
             PatientLogon hashedGp = (PatientLogon) gpPatientLogon.clone();
 
             hashedPatient.setPassword(LogonUtils.hashPassword(hashedPatient.getPassword()));
             hashedGp.setPassword(LogonUtils.hashPassword(hashedGp.getPassword()));
 
             LegacySpringUtils.getUserManager().saveUserFromPatient(hashedPatient);
             LegacySpringUtils.getUserManager().saveUserFromPatient(hashedGp);
 
             LegacySpringUtils.getUserManager().save(userMapping);
             LegacySpringUtils.getUserManager().save(userMappingPatientEnters);
             LegacySpringUtils.getUserManager().save(userMappingGp);
 
            if (LegacySpringUtils.getPatientManager().get(nhsno, unitcode) != null) {
                Patient patient = new Patient();
                patient.setNhsno(nhsno);
                patient.setUnitcode(unitcode);
                patient.setEmailAddress(email);
                LegacySpringUtils.getPatientManager().save(patient);
            }
 
 
             AddLog.addLog(LegacySpringUtils.getSecurityUserManager().getLoggedInUsername(), AddLog.PATIENT_ADD,
                     patientLogon.getUsername(),
                     userMapping.getNhsno(), userMapping.getUnitcode(), "");
             mappingToFind = "success";
         }
 


         List<Unit> units = LegacySpringUtils.getUnitManager().getAll(false);
 
         request.setAttribute("units", units);
         request.setAttribute("patient", patientLogon);
         request.setAttribute("userMapping", userMapping);
         request.getSession().setAttribute("gp", gpPatientLogon);
         request.getSession().setAttribute("userMappingGp", userMappingGp);
 
         return mapping.findForward(mappingToFind);
     }
 
     private List findExistingPatientsWithSameNhsno(String nhsno) {
         return LegacySpringUtils.getUserManager().getUserMappingsForNhsNo(nhsno);
     }
 }
