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
 
 package org.patientview.patientview;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
import org.patientview.patientview.logon.LogonUtils;
import org.patientview.patientview.model.User;
import org.patientview.patientview.user.UserUtils;
import org.patientview.utils.LegacySpringUtils;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class ContactFormAction extends Action {
 
     public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                  HttpServletResponse response) throws Exception {
 
        User user = UserUtils.retrieveUser(request);

        if (user != null) {
             String message = request.getParameter("message");
             String type = request.getParameter("type");
             String email = request.getParameter("email");
             String subject = "Renal Patient View Enquiry";
            String nhsno = request.getParameter("usermapping.nhsno");
 
            message = createMessage(message, user.getName(), email, nhsno);
 
             ServletContext context = request.getSession().getServletContext();
 
             if ("unit".equals(type)) {
                 // Send to unit
                 String unitEmail = request.getParameter("unit.renaladminemail");
                 if (unitEmail != null && unitEmail.length() > 0) {
                     if (email != null && email.length() > 0) {
                         EmailUtils.sendEmail(context, email, unitEmail, subject, message);
                     } else {
                         EmailUtils.sendEmail(context, unitEmail, subject, message);
                     }
                 }
             } else if ("admin".equals(type)) {
                 // Send  to admin
                 String superadminEmail = context.getInitParameter("admin.email.to");
 
                 if (email != null && email.length() > 0) {
                     EmailUtils.sendEmail(context, email, superadminEmail, subject, message);
                 } else {
                     EmailUtils.sendEmail(context, superadminEmail, subject, message);
                 }
             }
         } else if (!LegacySpringUtils.getSecurityUserManager().isRolePresent("patient")) {
             return LogonUtils.logonChecks(mapping, request, "control");
         }
         request.setAttribute("emailSent", true);
         return LogonUtils.logonChecks(mapping, request);
     }
 
 
    private String createMessage(String message, String name, String email, String nhsno) {
         String completeMessage = "";
 
        completeMessage += "Patient name - " + name + "\n";
        completeMessage += "NHS no - " + nhsno + "\n";
         completeMessage += "Email - " + email + "\n";
         completeMessage += "\n";
         completeMessage += "Message:" + "\n";
         completeMessage += "------------" + "\n";
         completeMessage += message + "\n";
         completeMessage += "------------" + "\n";
 
         return completeMessage;
     }
 }
