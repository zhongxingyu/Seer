 package com.worthsoln.patientview.messaging;
 
 import com.worthsoln.actionutils.ActionUtils;
 import com.worthsoln.ibd.action.BaseAction;
 import com.worthsoln.patientview.logon.UnitAdmin;
 import com.worthsoln.patientview.model.Unit;
 import com.worthsoln.patientview.model.User;
 import com.worthsoln.patientview.user.UserUtils;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.springframework.util.StringUtils;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 public class ConversationsAction extends BaseAction {
 
     public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                  HttpServletResponse response) throws Exception {
         // set current nav
         ActionUtils.setUpNavLink(mapping.getParameter(), request);
 
         User user = UserUtils.retrieveUser(request);
 
         // if the logged in user has not got an email set then show the no email message - only really for patients
         if (!StringUtils.hasText(user.getEmail())) {
             request.setAttribute(Messaging.NO_EMAIL_SET_PARAM, true);
         } else {

             List<Unit> units = getUnitManager().getLoggedInUsersUnits();
 
             List<User> unitAdminRecipients = new ArrayList<User>();
             List<User> unitStaffRecipients = new ArrayList<User>();
             List<User> patientRecipients = new ArrayList<User>();
 
             // patients and unit staff/admin get addresses for unit admin and staff
             // unit staff and admin also get patients
             for (Unit unit : units) {
                 if (!unit.getUnitcode().equalsIgnoreCase("patient")) {
                     List<UnitAdmin> unitAdmins = getUnitManager().getUnitUsers(unit.getUnitcode());
 
                     for (UnitAdmin unitAdmin : unitAdmins) {
                         User unitUser = getUserManager().get(unitAdmin.getUsername());
 
                         if (StringUtils.hasText(unitUser.getEmail())) {
                             if (!unitUser.equals(user)) {
                                 if (unitAdmin.getRole().equals("unitadmin")) {
                                     unitAdminRecipients.add(unitUser);
                                 } else if (unitAdmin.getRole().equals("unitstaff")) {
                                     unitStaffRecipients.add(unitUser);
                                 }
                             }
                         }
                     }
                 }
             }
 
             if (getSecurityUserManager().isRolePresent("unitadmin")
                     || getSecurityUserManager().isRolePresent("unitstaff")
                     || getSecurityUserManager().isRolePresent("superadmin")) {
                 for (Unit unit : units) {
                     if (!unit.getUnitcode().equalsIgnoreCase("patient")) {
                         List<User> unitPatients = getUnitManager().getUnitPatientUsers(unit.getUnitcode(),
                                 unit.getSpecialty());
 
                         for (User unitPatient : unitPatients) {
                             if (canIncludePatient(unitPatient)) {
                                 patientRecipients.add(unitPatient);
                             }
                         }
                     }
                 }
             }
 
             // order the recipients by name
             Collections.sort(unitAdminRecipients, new UserComparator());
             Collections.sort(unitStaffRecipients, new UserComparator());
             Collections.sort(patientRecipients, new UserComparator());
 
            request.setAttribute(Messaging.CONVERSATIONS_PARAM, getMessageManager().getConversations(user.getId()));
            request.setAttribute(Messaging.UNIT_ADMIN_RECIPIENTS_PARAM, unitAdminRecipients);
            request.setAttribute(Messaging.UNIT_STAFF_RECIPIENTS_PARAM, unitStaffRecipients);
            request.setAttribute(Messaging.PATIENT_RECIPIENTS_PARAM, patientRecipients);
         }
 
         return mapping.findForward(SUCCESS);
     }
 
     /**
      * exclude patients that have no got an email set
      * exlude patients with '-gp' or 'dummy' in the name
       */
     private boolean canIncludePatient(User patient) {
         return StringUtils.hasText(patient.getEmail())
                 && patient.getName() != null
                 && !patient.getName().toLowerCase().contains("-gp")
                && !patient.getName().toLowerCase().contains("dummy");
     }
 
     private class UserComparator implements Comparator<User> {
         @Override
         public int compare(User o1, User o2) {
             return o1.getName().compareTo(o2.getName());
         }
     }
 }
