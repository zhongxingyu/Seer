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
 
 import org.patientview.patientview.model.Unit;
 import org.patientview.patientview.model.UserMapping;
 import org.patientview.patientview.user.NhsnoUnitcode;
 import org.patientview.utils.LegacySpringUtils;
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 public class PatientEditAction extends Action {
 
     private static final int YEAR_START = 0;
     private static final int YEAR_END = 4;
     private static final int MONTH_START = 5;
     private static final int MONTH_END = 7;
     private static final int DAY_START = 8;
     private static final int DAY_END = 10;
     private static final int HOUR_START = 11;
     private static final int HOUR_END = 13;
     private static final int MINUTE_START = 14;
     private static final int MINUTE_END = 16;
     private static final int SECOND_START = 17;
     private static final int SECOND_END = 19;
 
     public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                  HttpServletResponse response) throws Exception {
         String username = BeanUtils.getProperty(form, "username");
         String password = BeanUtils.getProperty(form, "password");
         String name = BeanUtils.getProperty(form, "name");
         String email = BeanUtils.getProperty(form, "email");
         boolean emailverified = "true".equals(BeanUtils.getProperty(form, "emailverified"));
         String nhsno = BeanUtils.getProperty(form, "nhsno");
         String unitcode = BeanUtils.getProperty(form, "unitcode");
         String overrideDuplicateNhsno = BeanUtils.getProperty(form, "overrideDuplicateNhsno");
         boolean firstlogon = "true".equals(BeanUtils.getProperty(form, "firstlogon"));
         boolean dummypatient = "true".equals(BeanUtils.getProperty(form, "dummypatient"));
         Calendar lastlogoncal = createDatestamp(BeanUtils.getProperty(form, "lastlogon"));
         Date lastlogon = lastlogoncal == null ? null : lastlogoncal.getTime();
         String failedlogonsstring = BeanUtils.getProperty(form, "failedlogons");
         int failedlogons = Integer.decode(failedlogonsstring);
         boolean accountlocked = "true".equals(BeanUtils.getProperty(form, "accountlocked"));
         String mappingToFind = "";
 
         List duplicateUsers = findDuplicateUsers(nhsno, username);
 
         if (!duplicateUsers.isEmpty() && !overrideDuplicateNhsno.equals("on")) {
             request.setAttribute(LogonUtils.NHSNO_ALREADY_EXISTS, nhsno);
             mappingToFind = "input";
             NhsnoUnitcode nhsnoThing = new NhsnoUnitcode(nhsno, unitcode);
             request.setAttribute("nhsnot", nhsnoThing);
         } else {
 
             PatientLogon patient =
                     new PatientLogon(username, password, name, email, emailverified, firstlogon, dummypatient,
                             lastlogon, failedlogons, accountlocked);
             LegacySpringUtils.getUserManager().saveUserFromPatient(patient);
 
             List<UserMapping> userMappings = findUsersSiblings(username, unitcode);
 
             for (UserMapping userMapping : userMappings) {
                 userMapping.setNhsno(nhsno);
                 LegacySpringUtils.getUserManager().save(userMapping);
             }
 
             Unit unit = LegacySpringUtils.getUnitManager().get(unitcode);
 
             if (unit != null) {
                 request.setAttribute("unit", unit);
             }
 
             List patients = LegacySpringUtils.getPatientManager().getUnitPatientsAllWithTreatmentDao(unitcode);
            request.setAttribute("patients", patients);
             mappingToFind = "success";
         }
 
         return mapping.findForward(mappingToFind);
     }
 
     private List<UserMapping> findUsersSiblings(String username, String unitcode) throws Exception {
         return LegacySpringUtils.getUserManager().getUsersSiblings(username, unitcode);
     }
 
     private List findDuplicateUsers(String nhsno, String username) throws Exception {
         return LegacySpringUtils.getUserManager().getDuplicateUsers(nhsno, username);
     }
 
     private static Calendar createDatestamp(String dateTimeString) {
         Calendar datestamp = null;
 
         if (!"".equals(dateTimeString)) {
             datestamp = Calendar.getInstance();
 
             int year = Integer.parseInt(dateTimeString.substring(YEAR_START, YEAR_END));
             int month = Integer.parseInt(dateTimeString.substring(MONTH_START, MONTH_END));
             int day = Integer.parseInt(dateTimeString.substring(DAY_START, DAY_END));
 
             datestamp.set(year, month - 1, day, 0, 0, 0);
             datestamp.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dateTimeString.substring(HOUR_START, HOUR_END)));
             datestamp.set(Calendar.MINUTE, Integer.parseInt(dateTimeString.substring(MINUTE_START, MINUTE_END)));
 
             if (dateTimeString.length() == SECOND_END) {
                 datestamp.set(Calendar.SECOND, Integer.parseInt(dateTimeString.substring(SECOND_START, SECOND_END)));
             }
 
             datestamp.set(Calendar.MILLISECOND, 0);
         }
         return datestamp;
     }
 }
