 /*
  Copyright (c) 2000-2005 University of Washington.  All rights reserved.
 
  Redistribution and use of this distribution in source and binary forms,
  with or without modification, are permitted provided that:
 
    The above copyright notice and this permission notice appear in
    all copies and supporting documentation;
 
    The name, identifiers, and trademarks of the University of Washington
    are not used in advertising or publicity without the express prior
    written permission of the University of Washington;
 
    Recipients acknowledge that this distribution is made available as a
    research courtesy, "as is", potentially with defects, without
    any obligation on the part of the University of Washington to
    provide support, services, or repair;
 
    THE UNIVERSITY OF WASHINGTON DISCLAIMS ALL WARRANTIES, EXPRESS OR
    IMPLIED, WITH REGARD TO THIS SOFTWARE, INCLUDING WITHOUT LIMITATION
    ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
    PARTICULAR PURPOSE, AND IN NO EVENT SHALL THE UNIVERSITY OF
    WASHINGTON BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
    DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
    PROFITS, WHETHER IN AN ACTION OF CONTRACT, TORT (INCLUDING
    NEGLIGENCE) OR STRICT LIABILITY, ARISING OUT OF OR IN CONNECTION WITH
    THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 /* **********************************************************************
     Copyright 2005 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
     Redistribution and use of this distribution in source and binary forms,
     with or without modification, are permitted provided that:
        The above copyright notice and this permission notice appear in all
         copies and supporting documentation;
 
         The name, identifiers, and trademarks of Rensselaer Polytechnic
         Institute are not used in advertising or publicity without the
         express prior written permission of Rensselaer Polytechnic Institute;
 
     DISCLAIMER: The software is distributed" AS IS" without any express or
     implied warranty, including but not limited to, any implied warranties
     of merchantability or fitness for a particular purpose or any warrant)'
     of non-infringement of any current or pending patent rights. The authors
     of the software make no representations about the suitability of this
     software for any particular purpose. The entire risk as to the quality
     and performance of the software is with the user. Should the software
     prove defective, the user assumes the cost of all necessary servicing,
     repair or correction. In particular, neither Rensselaer Polytechnic
     Institute, nor the authors of the software are liable for any indirect,
     special, consequential, or incidental damages related to the software,
     to the maximum extent the law permits.
 */
 
 package org.bedework.webcommon.pref;
 
 import org.bedework.calfacade.BwCalendar;
 import org.bedework.calfacade.BwUser;
 import org.bedework.calfacade.svc.BwPreferences;
 import org.bedework.calsvci.CalSvcI;
 import org.bedework.webcommon.BwAbstractAction;
 import org.bedework.webcommon.BwActionFormBase;
 import org.bedework.webcommon.BwSession;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /** This action updates a calendar.
  *
  * <p>Parameters are:<ul>
  *      <li>"user"             User whos prefs we're changing - superuser only</li>
  *      <li>"preferredView"    Name of preferred view</li>
  *      <li>"viewPeriod"       day/week/month/year</li>
  *      <li>"skin"             Name of default skin</li>
  *      <li>"skinStyle"        Name of default skin style</li>
 
  *      <li>"email"            Email address of user</li>
  *      <li>"calPath"          Path to default calendar</li>
  *      <li>"userMode"         User interface mode</li>
  *      <li>"workDays"         7-character string representing workdays,
  *                             "W" representing each workday, space otherwise;
  *                             e.g. " WWWWW " is a typical Mon-Fri workweek</li>
  *      <li>"workDayStart"     In minutes, e.g. e.g. 14:30 is 870 and 17:30 is 1050</li>
  *      <li>"workDayEnd"       In minutes</li>
  *      <li>"preferredEndType" For adding events: "duration" or "date"
 
  * </ul>
  *
  * <p>Forwards to:<ul>
  *      <li>"noAccess"     user not authorised.</li>
  *      <li>"error"        for problems.</li>
  *      <li>"notFound"     no such calendar.</li>
  *      <li>"continue"     continue on to update page.</li>
  *      <li>"delete"       for confirmation.</li>
  * </ul>
  *
  * @author Mike Douglass   douglm@rpi.edu
  */
 public class UpdatePrefsAction extends BwAbstractAction {
   /* (non-Javadoc)
    * @see org.bedework.webcommon.BwAbstractAction#doAction(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.bedework.webcommon.BwSession, org.bedework.webcommon.BwActionFormBase)
    */
   public String doAction(HttpServletRequest request,
                          HttpServletResponse response,
                          BwSession sess,
                          BwActionFormBase form) throws Throwable {
     CalSvcI svc = form.fetchSvci();
     BwPreferences prefs;
 
     /* Refetch the prefs */
    if (getPublicAdmin(form) && (getReqPar(request, "user") != null)) {
       /* Fetch a given users preferences */
       if (!form.getCurUserSuperUser()) {
         return "noAccess"; // First line of defence
       }
 
       BwUser user = findUser(request, form);
       if (user == null) {
         return "notFound";
       }
 
       prefs = svc.getUserPrefs(user);
     } else {
       prefs = svc.getUserPrefs();
     }
 
     String str = getReqPar(request, "preferredView");
     if (str != null) {
       if (svc.findView(str) == null) {
         form.getErr().emit("org.bedework.client.error.viewnotfound", str);
         return "notFound";
       }
 
       prefs.setPreferredView(str);
     }
 
     str = getReqPar(request, "viewPeriod");
     if (str != null) {
       prefs.setPreferredViewPeriod(form.validViewPeriod(str));
     }
 
     str = getReqPar(request, "skin");
     if (str != null) {
       prefs.setSkinName(str);
     }
 
     str = getReqPar(request, "skinStyle");
     if (str != null) {
       prefs.setSkinStyle(str);
     }
 
     str = getReqPar(request, "email");
     if (str != null) {
       prefs.setEmail(str);
     }
 
     str = getReqPar(request, "newCalPath");
     if (str != null) {
       BwCalendar cal = svc.getCalendar(str);
       if (cal == null) {
         form.getErr().emit("org.bedework.client.error.nosuchcalendar", str);
         return "notFound";
       }
       prefs.setDefaultCalendar(cal);
     }
 
     int mode = getIntReqPar(request, "userMode", -1);
 
     if (mode != -1) {
       if ((mode < 0) || (mode > BwPreferences.maxMode)) {
         form.getErr().emit("org.bedework.client.error.badPref", "userMode");
         return "badPref";
       }
 
       prefs.setUserMode(mode);
     }
 
     str = getReqPar(request, "workDays");
     if (str != null) {
       // XXX validate
       prefs.setWorkDays(str);
     }
 
     int minutes = getIntReqPar(request, "workDayStart", -1);
 
     if (minutes != -1) {
       if ((minutes < 0) || (minutes > 24 * 60 - 1)) {
         form.getErr().emit("org.bedework.client.error.badPref", "workDayStart");
         return "badPref";
       }
 
       prefs.setWorkdayStart(minutes);
     }
 
     minutes = getIntReqPar(request, "workDayEnd", -1);
 
     if (minutes != -1) {
       if ((minutes < 0) || (minutes > 24 * 60 - 1)) {
         form.getErr().emit("org.bedework.client.error.badPref", "workDayEnd");
         return "badPref";
       }
 
       prefs.setWorkdayEnd(minutes);
     }
 
     if (prefs.getWorkdayStart() > prefs.getWorkdayEnd()) {
       form.getErr().emit("org.bedework.client.error.badPref",
                          "workDayStart > workDayEnd");
       return "badPref";
     }
 
     str = getReqPar(request, "preferredEndType");
     if (str != null) {
       if ("duration".equals(str) || "date".equals(str)) {
         prefs.setPreferredEndType(str);
       } else {
         form.getErr().emit("org.bedework.client.error.badPref", "preferredEndType");
         return "badPref";
       }
     }
 
     svc.updateUserPrefs(prefs);
     form.setUserPreferences(prefs);
     form.getMsg().emit("org.bedework.client.message.prefs.updated");
     return "success";
   }
 }
