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
 
 import org.patientview.patientview.logging.AddLog;
 import org.patientview.patientview.model.User;
 import org.patientview.patientview.model.UserMapping;
 import org.patientview.patientview.unit.UnitUtils;
 import org.patientview.patientview.user.EmailVerificationUtils;
 import org.patientview.utils.LegacySpringUtils;
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.List;
 
 public class UnitAdminAddAction extends Action {
 
     public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                  HttpServletResponse response) throws Exception {
         String username = BeanUtils.getProperty(form, "username");
         String password = LogonUtils.generateNewPassword();
         String name = BeanUtils.getProperty(form, "name");
         String email = BeanUtils.getProperty(form, "email");
         String unitcode = BeanUtils.getProperty(form, "unitcode");
         String role = BeanUtils.getProperty(form, "role");
         boolean isRecipient = "true".equals(BeanUtils.getProperty(form, "isrecipient"));
         boolean isClinician = "true".equals(BeanUtils.getProperty(form, "isclinician"));
         UnitAdmin unitAdmin = new UnitAdmin(username, password, name, email, false, role, true);
         unitAdmin.setIsrecipient(isRecipient);
         unitAdmin.setIsclinician(isClinician);
 
         List<UserMapping> usermappingList = LegacySpringUtils.getUserManager().getUserMappings(username);
 
         String mappingToFind;
         if (!usermappingList.isEmpty()) {
 
             // Note: legacy code assumes that there is a unique results here
             List<UserMapping> userMappings = LegacySpringUtils.getUserManager().getUserMappings(username, unitcode);
             UserMapping userMapping = null;
             if (userMappings != null && userMappings.size() > 0) {
                 userMapping = userMappings.get(0);
             }
 
             if (userMapping != null) {
                 request.setAttribute(LogonUtils.USER_ALREADY_EXISTS, username);
                 unitAdmin.setUsername("");
                 UnitUtils.putRelevantUnitsInRequest(request);
                 mappingToFind = "input";
             } else {
                 UserMapping userMappingNew = new UserMapping(username, unitcode, "");
                 request.setAttribute("usermapping", userMappingNew);
                 mappingToFind = "existinguser";
             }
         } else {
             // create the new user
             UnitAdmin hashedUnitAdmin = (UnitAdmin) unitAdmin.clone();
             hashedUnitAdmin.setPassword(LogonUtils.hashPassword(hashedUnitAdmin.getPassword()));
             User user = LegacySpringUtils.getUserManager().saveUserFromUnitAdmin(hashedUnitAdmin, unitcode);
 
            UserMapping userMapping = new UserMapping(username, unitcode, "");
            LegacySpringUtils.getUserManager().save(userMapping);

             // create mappings in radar if they dont already exist
             if (!LegacySpringUtils.getUserManager().userExistsInRadar(user.getId())) {
                 LegacySpringUtils.getUserManager().createProfessionalUserInRadar(user, unitcode);
             }
 
             AddLog.addLog(LegacySpringUtils.getSecurityUserManager().getLoggedInUsername(), AddLog.ADMIN_ADD,
                     unitAdmin.getUsername(), "",
                     unitcode, "");
             EmailVerificationUtils.createEmailVerification(hashedUnitAdmin.getUsername(), hashedUnitAdmin.getEmail(),
                     request);
             mappingToFind = "success";
         }
         request.setAttribute("adminuser", unitAdmin);
         return mapping.findForward(mappingToFind);
     }
 
 }
