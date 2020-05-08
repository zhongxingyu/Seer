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
 package org.bedework.webclient;
 
 //import org.bedework.calfacade.BwDateTime;
 import org.bedework.appcommon.FormattedFreeBusy;
 import org.bedework.appcommon.MyCalendarVO;
 import org.bedework.appcommon.TimeView;
 import org.bedework.calfacade.BwDuration;
 import org.bedework.calfacade.BwFreeBusy;
 import org.bedework.calfacade.BwUser;
 import org.bedework.calfacade.CalFacadeAccessException;
 import org.bedework.calfacade.CalFacadeUtil;
 import org.bedework.calfacade.ifs.CalTimezones;
 import org.bedework.calsvci.CalSvcI;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * Action to fetch free busy information
  * <p>Request parameters - all optional:<ul>
  *      <li>  userid:   whose free busy we want - default to current user</li>.
  *      <li>  calendar: name of the calendar - default to subscriptions
  *                      specified by user</li>.
  *      <li>  start:    start of period - default to beginning of this week</li>.
  *      <li>  end:      end of period - default to end of this week</li>.
  *      <li>  interval: default entire period or a multiplier</li>.
  *      <li>  intunit:  default to hours, "minutes", "hours, "days", "weeks"
  *                      "months"</li>.
  * </ul>
  * <p>e.g interval=30 and intunit="minutes" means half hour intervals
  * <p>Forwards to:<ul>
  *      <li>"doNothing"    input error or we want to ignore the request.</li>
  *      <li>"noAccess"     No acccess to free busy</li>
  *      <li>"notFound"     event not found.</li>
  *      <li>"error"        input error - correct and retry.</li>
  *      <li>"success"      fetched OK.</li>
  * </ul>
  *
  * <p>If no period is given return this week. If no interval and intunit is
  * supplied default to 1 hour intervals during the workday.
  *
  * @author Mike Douglass douglm @ rpi.edu
  */
 public class BwFreeBusyAction extends BwCalAbstractAction {
   /* (non-Javadoc)
    * @see org.bedework.webclient.BwCalAbstractAction#doAction(javax.servlet.http.HttpServletRequest, org.bedework.webclient.BwActionForm)
    */
   public String doAction(HttpServletRequest request,
                          BwActionForm form) throws Throwable {
     BwUser user;
     CalSvcI svci = form.fetchSvci();
 
     gotoDateView(this, form,
                  form.getDate(),
                  form.getViewTypeI(),
                  debug);
 
     String userId = getReqPar(request, "userid");
 
     if (userId != null) {
       user = svci.findUser(userId);
       if (user == null) {
         form.getErr().emit("org.bedework.client.error.usernotfound");
         return "notFound";
       }
     } else {
       user = svci.getUser();
     }
 
     MyCalendarVO scal;
     Calendar start;
     Calendar end;
     Calendar endDay;
 
     String st = getReqPar(request, "start");
 
     if (st == null) {
       /* Set period and start from the current timeview */
       TimeView tv = form.getCurTimeView();
 
       scal = tv.getFirstDay();
       start = scal.getCalendar();
       end = tv.getLastDay().getTomorrow().getCalendar();
     } else {
       Locale loc = Locale.getDefault();  // XXX Locale
 
       Date jdt = CalFacadeUtil.fromISODate(st);
       scal = new MyCalendarVO(jdt, loc);
 
       scal = scal.getFirstDayOfThisWeek();
       start = scal.getCalendar();
       //BwDateTime startDt = form.getEventStartDate().getDateTime();
 
       end = scal.getNextWeek().getCalendar();
     }
 
     endDay = scal.getTomorrow().getCalendar();
 
     int interval = getIntReqPar(request, "interval", 1);
     if (interval <= 0) {
       form.getErr().emit("org.bedework.client.error.badinterval");
       return "error";
     }
 
     String intunitStr = getReqPar(request, "intunit");
     BwDuration dur = new BwDuration();
 
     if (intunitStr != null) {
       if ("minutes".equals(intunitStr)) {
         dur.setMinutes(interval);
       } else if ("hours".equals(intunitStr)) {
         dur.setHours(interval);
       } else if ("days".equals(intunitStr)) {
         dur.setDays(interval);
       } else if ("weeks".equals(intunitStr)) {
         dur.setWeeks(interval);
       } else {
         form.getErr().emit("org.bedework.client.error.badintervalunit");
         return "error";
       }
     } else {
       dur.setHours(interval);
     }
 
     //int maxRequests = 1000;
     CalTimezones tzs = svci.getTimezones();
 
     Collection freeBusy = new ArrayList();
     /* Get the free busy in daily chunks and process for display
      */
     while (start.before(end)) {
       Date sdt = start.getTime();
       Date edt = endDay.getTime();
 
       if (debug) {
         debugMsg("getFreeBusy for start =  " + sdt +
                  " end = " + edt);
       }
 
       try {
         BwFreeBusy fb = svci.getFreeBusy(null, user,
                                          CalFacadeUtil.getDateTime(sdt, false, false, tzs),
                                          CalFacadeUtil.getDateTime(edt, false, false, tzs),
                                          dur, true);
 
         FormattedFreeBusy ffb = new FormattedFreeBusy(fb);
         freeBusy.add(ffb);
 
         start.add(Calendar.DAY_OF_MONTH, 1);
         endDay.add(Calendar.DAY_OF_MONTH, 1);
       } catch (CalFacadeAccessException cfae) {
         form.getErr().emit("org.bedework.client.error.noaccess");
         return "noAccess";
       }
     }
 
     form.assignFreeBusy(freeBusy);
 
     return "success";
   }
 }
