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
 package org.bedework.dumprestore.restore.rules;
 
import org.bedework.calfacade.BwUser;
 import org.bedework.calfacade.svc.BwPreferences;
 import org.bedework.calfacade.svc.BwSubscription;
 import org.bedework.calfacade.svc.BwView;
 import org.bedework.dumprestore.restore.RestoreGlobals;
 
 /**
  * @author Mike Douglass   douglm@rpi.edu
  * @version 1.0
  */
 public class UserPrefsFieldRule extends EntityFieldRule {
   UserPrefsFieldRule(RestoreGlobals globals) {
     super(globals);
   }
 
   public void field(String name) throws java.lang.Exception{
     BwPreferences p = (BwPreferences)top();
 
     if (ownedEntityTags(p, name)) {
       return;
     }
 
     if (name.equals("email")) {
       p.setEmail(stringFld());
     } else if (name.equals("default-calendar")) {
       p.setDefaultCalendar(calendarFld());
     } else if (name.equals("skinName")) {
       p.setSkinName(stringFld());
     } else if (name.equals("skinStyle")) {
       p.setSkinStyle(stringFld());
     } else if (name.equals("preferredView")) {
       p.setPreferredView(stringFld());
     } else if (name.equals("subscriptions")) {
       // Nothing to do now
     } else if (name.equals("workDays")) {
       p.setWorkDays(stringFld());
     } else if (name.equals("workdayStart")) {
       p.setWorkdayStart(intFld());
     } else if (name.equals("workdayEnd")) {
       p.setWorkdayEnd(intFld());
 
     // subscription fields
 
     } else if (name.equals("subscription")) {
       globals.subscriptionsTbl.put(p.getOwner(), globals.curSub);
       p.addSubscription(globals.curSub);
       globals.curSub = null;
     } else if (name.equals("sub-id")) {
       globals.curSub = new BwSubscription();
       globals.curSub.setId(intFld());
     } else if (name.equals("sub-seq")) {
       globals.curSub.setSeq(intFld());
     } else if (name.equals("sub-name")) {
       globals.curSub.setName(stringFld());
     } else if (name.equals("sub-owner")) {
      BwUser sowner = userFld();
      
      if (!p.getOwner().equals(sowner)) {
         error("Subscription owners don't match for " + globals.curSub);
        error("  Found owner " + sowner + " expected " + p.getOwner());
       }
       globals.curSub.setOwner(p.getOwner());
     } else if (name.equals("sub-uri")) {
       globals.curSub.setUri(stringFld());
     } else if (name.equals("sub-affectsFreeBusy")) {
       globals.curSub.setAffectsFreeBusy(booleanFld());
     } else if (name.equals("sub-display")) {
       globals.curSub.setDisplay(booleanFld());
     } else if (name.equals("sub-style")) {
       globals.curSub.setStyle(stringFld());
     } else if (name.equals("sub-internalSubscription")) {
       globals.curSub.setInternalSubscription(booleanFld());
     } else if (name.equals("sub-emailNotifications")) {
       globals.curSub.setEmailNotifications(booleanFld());
     } else if (name.equals("sub-calendarDeleted")) {
       globals.curSub.setCalendarDeleted(booleanFld());
     } else if (name.equals("sub-unremoveable")) {
       globals.curSub.setUnremoveable(booleanFld());
 
     // view fields
 
     } else if (name.equals("view")) {
       p.addView(globals.curView);
       globals.curView = null;
     } else if (name.equals("view-id")) {
       globals.curView = new BwView();
       globals.curView.setId(intFld());
     } else if (name.equals("view-seq")) {
       globals.curView.setSeq(intFld());
     } else if (name.equals("view-name")) {
       globals.curView.setName(stringFld());
     } else if (name.equals("view-owner")) {
      BwUser vowner = userFld();
      
      if (!p.getOwner().equals(vowner)) {
        error("View owners don't match for " + globals.curView);
        error("  Found owner " + vowner + " expected " + p.getOwner());
       }
       globals.curView.setOwner(p.getOwner());
     } else if (name.equals("view-subscriptions")) {
     } else if (name.equals("view-sub-id")) {
       BwSubscription sub = globals.subscriptionsTbl.getSub(p.getOwner(), intFld());
 
       if (sub == null) {
         error("  Missing subscription " + intFld() + " for view " +
               globals.curView);
       } else {
         globals.curView.addSubscription(sub);
       }
     }
   }
 }
 
