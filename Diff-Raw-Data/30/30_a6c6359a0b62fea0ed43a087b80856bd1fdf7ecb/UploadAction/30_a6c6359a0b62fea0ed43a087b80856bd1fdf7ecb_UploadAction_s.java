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
 
 package org.bedework.webcommon.misc;
 
 import org.bedework.calfacade.BwCalendar;
 import org.bedework.calfacade.BwEvent;
 import org.bedework.calfacade.CalFacadeException;
 import org.bedework.calfacade.svc.EventInfo;
 import org.bedework.calsvci.CalSvcI;
 import org.bedework.icalendar.IcalTranslator;
 import org.bedework.webcommon.BwAbstractAction;
 import org.bedework.webcommon.BwActionFormBase;
 import org.bedework.webcommon.BwSession;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Collection;
 import java.util.Iterator;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.upload.FormFile;
 
 /**
  * Action to upload an icalendar file..
  * <p>No request parameters (other than updates to email and subject)
  * <p>Forwards to:<ul>
  *      <li>"retry"        email options still not valid.</li>
  *      <li>"noEvent"      no event was selected.</li>
  *      <li>"success"      mailed (or queued) ok.</li>
  * </ul>
  */
 public class UploadAction extends BwAbstractAction {
   public String doAction(HttpServletRequest request,
                          HttpServletResponse response,
                          BwSession sess,
                          BwActionFormBase form) throws Throwable {
     if (form.getGuest()) {
       return "noAccess"; // First line of defence
     }
 
     CalSvcI svci = form.fetchSvci();
     BwCalendar cal = null;
 
     int calId = getIntReqPar(request, "calId", -1);
     if (calId >= 0) {
       cal = svci.getCalendar(calId);
     }
 
     if (cal == null) {
       if (getPublicAdmin(form)) {
         // Must specify a calendar for public events
         form.getErr().emit("org.bedework.client.error.missingcalendar");
         return "retry";
       }
      
       // Use preferred calendar
       cal = svci.getPreferredCalendar();
     }
 
     FormFile upFile = form.getUploadFile();
 
     if (upFile == null) {
       // Just forget it
       return "success";
     }
 
     String fileName = upFile.getFileName();
 
     if ((fileName == null) || (fileName.length() == 0)) {
       return "retry";
     }
 
     try {
       // To catch some of the parser errors
 
       InputStream is = upFile.getInputStream();
      
       IcalTranslator trans = new IcalTranslator(svci.getIcalCallback(), debug);
      
       Collection objs = trans.fromIcal(cal, new InputStreamReader(is));
      
       Iterator it = objs.iterator();
      
       while (it.hasNext()) {
         Object o = it.next();
        
         if (o instanceof EventInfo) {
           EventInfo ei = (EventInfo)o;
           BwEvent ev = ei.getEvent();
          
           if (ei.getNewEvent()) {
             svci.addEvent(cal, ev, ei.getOverrides());
           } else {
             svci.updateEvent(ev);
           }
         }
       }
     } catch (CalFacadeException cfe) {
       form.getErr().emit(cfe.getMessage(), cfe.getExtra());
       return "baddata";
     }
 
    form.getMsg().emit("org.bedework.message.added.events", 1);
 
     return "success";
   }
 }
