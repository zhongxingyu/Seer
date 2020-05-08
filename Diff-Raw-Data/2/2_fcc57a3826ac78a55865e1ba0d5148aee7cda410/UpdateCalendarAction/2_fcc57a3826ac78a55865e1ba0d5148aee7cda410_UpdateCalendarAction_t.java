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
 
 package org.bedework.webcommon.calendars;
 
 import org.bedework.calfacade.BwCalendar;
 import org.bedework.calsvci.CalSvcI;
 import org.bedework.webcommon.BwAbstractAction;
 import org.bedework.webcommon.BwActionFormBase;
 import org.bedework.webcommon.BwSession;
 
 import edu.rpi.sss.util.Util;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /** This action updates a calendar.
  *
  * <p>Parameters are:<ul>
  *      <li>"calendar.summary"            Summary for calendar</li>
  *      <li>"calendar.description"        Description for calendar</li>
  *      <li>"calendarCollection"          Calendar/Folder flag   true/false</li>
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
 public class UpdateCalendarAction extends BwAbstractAction {
   /* (non-Javadoc)
    * @see org.bedework.webcommon.BwAbstractAction#doAction(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.bedework.webcommon.BwSession, org.bedework.webcommon.BwActionFormBase)
    */
   public String doAction(HttpServletRequest request,
                          HttpServletResponse response,
                          BwSession sess,
                          BwActionFormBase form) throws Throwable {
     if (form.getGuest()) {
       return "noAccess"; // First line of defence
     }
 
     String reqpar = request.getParameter("delete");
 
     if (reqpar != null) {
       return "delete";
     }
 
     CalSvcI svci = form.fetchSvci();
     boolean add = form.getAddingCalendar();
 
     /** We are just updating from the current form values.
      */
     if (!validateCalendar(request, form, add)) {
       return "retry";
     }
 
     BwCalendar cal = form.getCalendar();
 
     if (add) {
       BwCalendar parent = svci.getCalendar(form.getParentCalendarId());
 
       if (parent == null) {
         return "error";
       }
       svci.addCalendar(cal, parent);
     } else {
       svci.updateCalendar(cal);
     }
 
     updateAuthPrefs(form, null, null, null, cal);
 
     if (add) {
       form.getMsg().emit("org.bedework.client.message.calendar.added");
     } else {
       form.getMsg().emit("org.bedework.client.message.calendar.updated");
     }
 
     return "continue";
   }
 
   /** Validate a calendar - we do not create these as a side effect.
    *
    * @return boolean  false means something wrong, message emitted
    */
   private boolean validateCalendar(HttpServletRequest request,
                                    BwActionFormBase form,
                                    boolean add) throws Throwable {
     boolean ok = true;
 
     BwCalendar cal = form.getCalendar();
 
     Boolean cc = getBooleanReqPar(request, "calendarCollection");
 
     if (add) {
       cal.setName(Util.checkNull(cal.getName()));
 
       if (cc != null) {
         cal.setCalendarCollection(cc.booleanValue());
       }
     } else {
       // Update
       if (cc != null) {
         boolean newCC = cc.booleanValue();
         if (newCC != cal.getCalendarCollection()) {
           // Can only change for an empty object.
           if ((cal.getChildren().size() != 0) ||
               form.fetchSvci().getCalendarInuse(cal)) {
             form.getErr().emit("org.bedework.validation.error.forbidden.calmode");
             return false;
           }
         }
        
        cal.setCalendarCollection(newCC);
       }
     }
 
     cal.setSummary(Util.checkNull(cal.getSummary()));
     cal.setDescription(Util.checkNull(cal.getDescription()));
 
     if (cal.getName() == null) {
       form.getErr().emit("org.bedework.validation.error.missingfield",
                          "name");
       ok = false;
     }
 
     form.setCalendar(cal);
 
     return ok;
   }
 }
 
