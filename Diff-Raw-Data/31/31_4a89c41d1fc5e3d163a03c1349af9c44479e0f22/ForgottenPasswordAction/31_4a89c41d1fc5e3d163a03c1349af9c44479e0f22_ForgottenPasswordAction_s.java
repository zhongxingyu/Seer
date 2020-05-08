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
 
 import org.patientview.patientview.logging.AddLog;
 import org.patientview.patientview.logon.LogonUtils;
 import org.patientview.patientview.model.User;
 import org.patientview.patientview.user.UserUtils;
 import org.patientview.utils.LegacySpringUtils;
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class ForgottenPasswordAction extends Action {
 
     public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                  HttpServletResponse response) throws Exception {
         String username = request.getParameter("username");
         String email = request.getParameter("email");
         String forwardMapping = "success";
 
         if (username != null && username.trim().length() > 0 && email != null && email.trim().length() > 0) {
             User user = LegacySpringUtils.getUserManager().get(username);
 
             request.setAttribute("foundUser", user != null);
 
             if (user != null) {
                 String storedEmail = user.getEmail();
                 if (storedEmail != null && user.getEmail().trim().length() > 0) {
                     if (email.equalsIgnoreCase(storedEmail)) {
                         String password = LogonUtils.generateNewPassword();
                         user.setPassword(LogonUtils.hashPassword(password));
                         user.setFirstlogon(true);
 
                         // Email password
                         String message = "[This is an automated email from Renal PatientView - "
                                 + "do not reply to this email]\n"
                                 + "\n"
                                 + "Hello User,\n"
                                 + "\n"
                                 + "We received a request on the web site to reset your password. "
                                 + "Your new password is\n\n"
                                 + password + "\n"
                                 + "\n"
                                 + "Enjoy the site,\n"
                                 + "\n"
                                 + "Renal Patient View";
                        String fromAddress = request.getSession().getServletContext().getInitParameter("noreply.email");
                         EmailUtils.sendEmail(request.getSession().getServletContext(), fromAddress, user.getEmail(),
                                 "[Renal PatientView] Your password has been reset", message);
                         LegacySpringUtils.getUserManager().save(user);
 
                         AddLog.addLog("system", AddLog.PASSWORD_RESET_FORGOTTEN, username, "",
                                 UserUtils.retrieveUsersRealUnitcodeBestGuess(username), user.getEmail());
                     } else {
                         request.setAttribute("noMatch", true);
                         forwardMapping = "input";
                     }
                 } else {
                     request.setAttribute("nullEmail", true);
                     forwardMapping = "input";
                 }
             } else {
                 request.setAttribute("nullUser", true);
                 forwardMapping = "input";
             }
         } else {
             request.setAttribute("nullUser", true);
             forwardMapping = "input";
         }
         return mapping.findForward(forwardMapping);
     }
 }
