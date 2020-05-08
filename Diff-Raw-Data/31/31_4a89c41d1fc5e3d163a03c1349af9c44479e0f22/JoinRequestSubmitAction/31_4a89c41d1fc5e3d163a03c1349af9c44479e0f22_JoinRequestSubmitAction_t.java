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
 
 package org.patientview.patientview.join;
 
 import org.patientview.ibd.action.BaseAction;
 import org.patientview.patientview.EmailUtils;
 import org.patientview.patientview.logon.LogonUtils;
 import org.patientview.patientview.model.JoinRequest;
 import org.patientview.patientview.model.Unit;
 import org.patientview.patientview.unit.UnitUtils;
 import org.patientview.patientview.user.UserUtils;
 import org.patientview.utils.LegacySpringUtils;
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 import org.apache.struts.action.DynaActionForm;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 public class JoinRequestSubmitAction extends BaseAction {
 
     public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                  HttpServletResponse response)
             throws Exception {
 
         /**
          * validate form
          */
         DynaActionForm dynaForm = (DynaActionForm) form;
 
         if (!validate(dynaForm, request)) {
             /**
              * set bean values for success screen
              */
             List<Unit> units = LegacySpringUtils.getUnitManager().getAllDisregardingSpeciality(false);
             request.setAttribute("units", units);
 
             return mapping.findForward(INPUT);
         }
 
         /**
          * get data from form
          */
         String firstName = BeanUtils.getProperty(form, "firstName");
         String lastName = BeanUtils.getProperty(form, "lastName");
         Date dateOfBirth = convertFormDateString("dateOfBirth", dynaForm);
         String nhsNo = BeanUtils.getProperty(form, "nhsNo");
         String unitcode = BeanUtils.getProperty(form, "unitcode");
         String email = BeanUtils.getProperty(form, "email");
 
         /**
          * save the join request
          */
         JoinRequest joinRequest = new JoinRequest(firstName, lastName, dateOfBirth, nhsNo, unitcode, email,
                 new GregorianCalendar().getTime());
         LegacySpringUtils.getJoinRequestManager().save(joinRequest);
 
         /**
          * send a mail to rpv admin
          */
         sendJoinRequestEmailToRPVAdmin(request, unitcode, joinRequest);
 
         return LogonUtils.logonChecks(mapping, request);
     }
 
     private static void sendJoinRequestEmailToRPVAdmin(HttpServletRequest request, String unitcode,
                                                        JoinRequest joinRequest) {
         /**
          * from
          */
        String fromAddress = LegacySpringUtils.getContextProperties().getProperty("noreply.email");
 
         /**
          * to
          */
         Unit unit = UnitUtils.retrieveUnit(unitcode);
         String toAddress = EmailUtils.getUnitOrSystemAdminEmailAddress(request.getSession().getServletContext(), unit);
 
         /**
          * subject
          */
         String subject = "[PatientView] New join request from patient";
 
         /**
          * body
          */
         String message = "Hello,\n"
                 + "\n"
                 + "A patient has made a request on the website to join PatientView at your unit. "
                 + "Their details are below.\n"
                 + "\n"
                 + "First name: " + joinRequest.getFirstName() + "\n"
                 + "Last name: " + joinRequest.getLastName() + "\n"
                 + "NHS No: " + joinRequest.getNhsNo() + "\n"
                 + "Unit code: " + joinRequest.getUnitcode() + "\n"
                 + "Date of birth: " + joinRequest.getDateOfBirthFormatted() + "\n"
                 + "Email address: " + joinRequest.getEmail() + "\n"
                 + "\n"
                 + "Please verify these details and follow up this request with the patient "
                 + "using your usual process to consent and add patients to PatientView."
                 + "\n";
 
         /**
          * send the mail
          */
         EmailUtils.sendEmail(request.getSession().getServletContext(), fromAddress, toAddress, subject, message);
     }
 
     private boolean validate(DynaActionForm form, HttpServletRequest request) {
         ActionMessages actionErrors = new ActionMessages();
 
         // Comment out the ones that do not require validation
         if (form.get("firstName") == null || ((String) form.get("firstName")).length() == 0) {
             actionErrors.add("firstName", new ActionMessage("firstName.required"));
         }
 
         if (form.get("lastName") == null || ((String) form.get("lastName")).length() == 0) {
             actionErrors.add("lastName", new ActionMessage("lastName.required"));
         }
 
         if (form.get("dateOfBirth") == null || ((String) form.get("dateOfBirth")).length() == 0) {
             actionErrors.add("dateOfBirth", new ActionMessage("dateOfBirth.required"));
         }
 
         if (StringUtils.isNotEmpty((String) form.get("nhsNo"))) {
             try {
                 if (!UserUtils.isNhsNumberValid((String) form.get("nhsNo"))) {
                     actionErrors.add("nhsNo", new ActionMessage("nhsno.checksum"));
                 }
             } catch (NumberFormatException e) {
                 actionErrors.add("nhsNo", new ActionMessage("nhsno.checksum"));
             }
         }
 
         if (form.get("unitcode") == null || ((String) form.get("unitcode")).length() == 0
                 || (form.get("unitcode")).equals("-1")) {
             actionErrors.add("unitcode", new ActionMessage("unitcode.required"));
         }
 
         if (form.get("email") == null || ((String) form.get("email")).length() == 0) {
             actionErrors.add("email", new ActionMessage("email.required"));
         } else if (((String) form.get("email")).indexOf("@") < 0 || ((String) form.get("email")).indexOf(".") < 0) {
             actionErrors.add("email", new ActionMessage("email.valid"));
         }
 
         if (form.get("antiSpamAnswer") == null || !(form.get("antiSpamAnswer")).equals(
                 request.getSession().getAttribute("ANTI_SPAM_ANSWER"))) {
             actionErrors.add("antiSpamAnswer", new ActionMessage("antispam.wrong.answer"));
         }
 
         if (actionErrors.size() > 0) {
             saveErrors(request, actionErrors);
             return false;
         }
 
         return true;
     }
 }
