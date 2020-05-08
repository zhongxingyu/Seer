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
  *      <li>"user"            User whos prefs we're changing - superuser only</li>
  *      <li>"view"            Name of preferred view</li>
  *      <li>"viewPeriod"      day/week/month/year</li>
  *      <li>"skin"            Name of default skin</li>
  *      <li>"skinStyle"       Name of default skin style</li>
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
     if (!form.getUserAuth().isSuperUser()) {
       return "noAccess"; // First line of defence
     }
 
     CalSvcI svc = form.getCalSvcI();
 
     String str = getReqPar(request, "user");
     if (str == null) {
       form.getErr().emit("org.bedework.client.notfound", str);
       return "notFound";
     }
 
     BwUser user = svc.findUser(str);
     if (user == null) {
       form.getErr().emit("org.bedework.client.notfound", str);
       return "notFound";
     }
 
     BwPreferences prefs = svc.getUserPrefs(user);
 
     str = getReqPar(request, "view");
     if (str != null) {
       if (svc.findView(str) == null) {
         form.getErr().emit("org.bedework.client.notfound", str);
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
 
     svc.updateUserPrefs(prefs);

     return "success";
   }
 }
 
