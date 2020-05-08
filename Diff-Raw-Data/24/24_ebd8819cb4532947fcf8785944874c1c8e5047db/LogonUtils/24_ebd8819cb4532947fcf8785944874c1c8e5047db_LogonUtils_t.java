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
 
 import com.Ostermiller.util.RandPass;
 import org.patientview.patientview.logging.AddLog;
 import org.patientview.patientview.model.SplashPage;
 import org.patientview.patientview.model.SplashPageUserSeen;
 import org.patientview.patientview.model.User;
 import org.patientview.patientview.splashpage.SplashPageUtils;
 import org.patientview.patientview.user.UserUtils;
 import org.patientview.utils.LegacySpringUtils;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import java.util.List;
 
 public final class LogonUtils {
 
     public static final String USER_ALREADY_EXISTS = "userAlreadyExists";
     public static final String NHSNO_ALREADY_EXISTS = "nhsnoAlreadyExists";
     public static final String PATIENTS_WITH_SAME_NHSNO = "nhsnoAlreadyExists";
     public static final String INVALID_NHSNO = "invalidNhsno";
     public static final String OFFER_TO_ALLOW_INVALID_NHSNO = "offerToAllowInvalidNhsno";
     public static final String PATIENT_ALREADY_IN_UNIT = "patientAlreadyInUnit";
     public static final int NEW_PASSWORD_LENGTH = 8;
 
     private LogonUtils() {
     }
 
     public static ActionForward logonChecks(ActionMapping mapping, HttpServletRequest request, String defaultForward) {
         String resultForward = defaultForward;
 
         // access the "user principal" from spring rather than request
         String username = LegacySpringUtils.getSecurityUserManager().getLoggedInUsername();
 
         if (username != null) {
             User user = LegacySpringUtils.getUserManager().get(username);
 
             if (user.isFirstlogon()) {
                 if (LegacySpringUtils.getUserManager().getCurrentSpecialtyRole(user).equalsIgnoreCase("patient")) {
                     resultForward = "patientPasswordChangeInput";
                 } else {
                     resultForward = "controlPasswordChangeInput";
                 }
                 request.setAttribute("firstLogon", "true");
             } else {
                 HttpSession session = request.getSession();
                 Object splashPageViewedThisSession = session.getAttribute("splashPageViewed");
 
                 if (splashPageViewedThisSession == null) {
                     SplashPage splashPage = activeSplashPage(user);
 
                     if (null != splashPage) {
                         // Note: This may need to be extended for other roles?
                         resultForward = "patient";
                         request.setAttribute("splashPage", splashPage);
                         session.setAttribute("splashPageViewed", "splashPageViewed");
                     }
                 }
             }
         }
 
         recordLogon(request);
         return mapping.findForward(resultForward);
     }
 
     private static SplashPage activeSplashPage(User user) {
         SplashPage returnSplashPage = null;
 
        String specialtyRole = LegacySpringUtils.getUserManager().getCurrentSpecialtyRole(user);
        if (specialtyRole != null && specialtyRole.equalsIgnoreCase("patient")) {
             List<SplashPage> splashpages = SplashPageUtils.retrieveSplashPagesForPatient(user);
             List<SplashPageUserSeen> splashPagesUserHasSeen = SplashPageUtils.retrieveSplashPagesPatientHasSeen(user);
 
             for (SplashPage splashPage : splashpages) {
                 boolean userHasSeenThisSplashPage = false;
 
                 for (SplashPageUserSeen splashPageUserSeen : splashPagesUserHasSeen) {
                     if (splashPage.getId() == splashPageUserSeen.getSplashpageid()) {
                         userHasSeenThisSplashPage = true;
                         break;
                     }
                 }
 
                 if (!userHasSeenThisSplashPage) {
                     returnSplashPage = splashPage;
                     markSplashPageAsSeenByUser(returnSplashPage, user);
                     break;
                 }
             }
         }
 
         return returnSplashPage;
     }
 
     private static void markSplashPageAsSeenByUser(SplashPage splashPage, User user) {
         SplashPageUserSeen splashPageUserSeen = new SplashPageUserSeen(user.getUsername(), splashPage.getId());
 
         LegacySpringUtils.getSplashPageManager().save(splashPageUserSeen);
     }
 
     private static void recordLogon(HttpServletRequest request) {
 
         // access the "user principal" from spring rather than request
         String username = LegacySpringUtils.getSecurityUserManager().getLoggedInUsername();
 
         HttpSession session = request.getSession();
         String logonRecorded = (String) session.getAttribute("logonRecorded");
         if (logonRecorded == null && username != null) {
             String unitCode = UserUtils.retrieveUsersRealUnitcodeBestGuess(username);
             String nhsno = UserUtils.retrieveUsersRealNhsnoBestGuess(username);
             AddLog.addLog(username, AddLog.LOGGED_ON, username, nhsno, unitCode, "");
             session.setAttribute("logonRecorded", "true");
         }
     }
 
     public static ActionForward logonChecks(ActionMapping mapping, HttpServletRequest request) {
         return logonChecks(mapping, request, "success");
     }
 
     public static String generateNewPassword() {
         return new RandPass(RandPass.NONCONFUSING_ALPHABET).getPass(NEW_PASSWORD_LENGTH);
     }
 
     public static String displayRole(String role) {
         String displayRole = "";
         if ("unitadmin".equals(role)) {
             displayRole = "Unit Admin";
         } else if ("unitstaff".equals(role)) {
             displayRole = "Unit Staff";
         } else if ("patient".equals(role)) {
             displayRole = "Patient";
         } else if ("superadmin".equals(role)) {
             displayRole = "Super Admin";
         } else {
             displayRole = "Role Unknown";
         }
         return displayRole;
     }
 
     public static String hashPassword(String password) {
         return DigestUtils.sha256Hex(password);
     }
 }
