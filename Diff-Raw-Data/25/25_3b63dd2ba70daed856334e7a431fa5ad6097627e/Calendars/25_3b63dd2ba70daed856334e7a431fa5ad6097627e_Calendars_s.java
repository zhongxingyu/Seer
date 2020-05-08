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
 package org.bedework.calcore.hibernate;
 
 import org.bedework.calfacade.BwCalendar;
 import org.bedework.calfacade.BwUser;
 import org.bedework.calfacade.ifs.CalendarsI;
 import org.bedework.calfacade.ifs.Calintf;
 import org.bedework.calfacade.CalFacadeException;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 /** Class to encapsulate most of what we do with calendars
  *
  * @author Mike Douglass   douglm@rpi.edu
  */
 class Calendars extends CalintfHelper implements CalendarsI {
   private String publicCalendarRootPath;
   private String userCalendarRootPath;
 
   /** Constructor
    *
    * @param cal
    * @param access
    * @param user
    * @param debug
    * @throws CalFacadeException
    */
   public Calendars(Calintf cal, AccessUtil access, BwUser user, boolean debug)
                   throws CalFacadeException {
     super(cal, access, user, debug);
 
     publicCalendarRootPath = "/" + getSyspars().getPublicCalendarRoot();
     userCalendarRootPath = "/" + getSyspars().getUserCalendarRoot();
   }
 
   /** Called after a user has been added to the system.
    *
    * @throws CalFacadeException
    */
  public void addNewCalendars() throws CalFacadeException {
     HibSession sess = getSess();
 
     /* Add a user collection to the userCalendarRoot and then a default
        calendar collection. */
 
     sess.namedQuery("getCalendarByPath");
 
     String path =  userCalendarRootPath;
     sess.setString("path", path);
 
     BwCalendar userrootcal = (BwCalendar)sess.getUnique();
 
     if (userrootcal == null) {
       throw new CalFacadeException("No user root at " + path);
     }
 
     path += "/" + user.getAccount();
     sess.namedQuery("getCalendarByPath");
     sess.setString("path", path);
 
     BwCalendar usercal = (BwCalendar)sess.getUnique();
     if (usercal != null) {
       throw new CalFacadeException("User calendar already exists at " + path);
     }
 
     /* Create a folder for the user */
     usercal = new BwCalendar();
     usercal.setName(user.getAccount());
     usercal.setCreator(user);
     usercal.setOwner(user);
     usercal.setPublick(false);
     usercal.setPath(path);
     usercal.setCalendar(userrootcal);
     userrootcal.addChild(usercal);
 
     sess.save(userrootcal);
 
     /* Create a default calendar */
     BwCalendar cal = new BwCalendar();
     cal.setName(getSyspars().getUserDefaultCalendar());
     cal.setCreator(user);
     cal.setOwner(user);
     cal.setPublick(false);
     cal.setPath(path + "/" + getSyspars().getUserDefaultCalendar());
     cal.setCalendar(usercal);
     cal.setCalendarCollection(true);
     usercal.addChild(cal);
 
     /* Add the trash calendar */
     cal = new BwCalendar();
     cal.setName(getSyspars().getDefaultTrashCalendar());
     cal.setCreator(user);
     cal.setOwner(user);
     cal.setPublick(false);
     cal.setPath(path + "/" + getSyspars().getDefaultTrashCalendar());
     cal.setCalendar(usercal);
     cal.setCalendarCollection(true);
     usercal.addChild(cal);
 
     /* Add the inbox */
     cal = new BwCalendar();
     cal.setName(getSyspars().getUserInbox());
     cal.setCreator(user);
     cal.setOwner(user);
     cal.setPublick(false);
     cal.setPath(path + "/" + getSyspars().getUserInbox());
     cal.setCalendar(usercal);
     cal.setCalendarCollection(true);
     usercal.addChild(cal);
 
     /* Add the outbox */
     cal = new BwCalendar();
     cal.setName(getSyspars().getUserOutbox());
     cal.setCreator(user);
     cal.setOwner(user);
     cal.setPublick(false);
     cal.setPath(path + "/" + getSyspars().getUserOutbox());
     cal.setCalendar(usercal);
     cal.setCalendarCollection(true);
     usercal.addChild(cal);
 
     sess.save(usercal);
 
     sess.update(user);
   }
 
   /* ====================================================================
    *                   CalendarsI methods
    * ==================================================================== */
 
   public BwCalendar getPublicCalendars() throws CalFacadeException {
     HibSession sess = getSess();
 
     sess.namedQuery("getCalendarByPath");
     sess.setString("path", publicCalendarRootPath);
     sess.cacheableQuery();
 
     BwCalendar cal = (BwCalendar)sess.getUnique();
 
     return cloneAndCheckAccess(cal, privRead, noAccessReturnsNull);
   }
 
   public Collection getPublicCalendarCollections() throws CalFacadeException {
     HibSession sess = getSess();
 
     sess.namedQuery("getPublicCalendarCollections");
     sess.cacheableQuery();
 
     return access.checkAccess(sess.getList(), privWrite, true);
   }
 
   public BwCalendar getCalendars() throws CalFacadeException {
     HibSession sess = getSess();
 
     sess.namedQuery("getCalendarByPath");
     sess.setString("path", userCalendarRootPath + "/" + user.getAccount());
     sess.cacheableQuery();
 
     BwCalendar cal = (BwCalendar)sess.getUnique();
 
     return cloneAndCheckAccess(cal, privRead, noAccessReturnsNull);
   }
 
   public Collection getCalendarCollections() throws CalFacadeException {
     HibSession sess = getSess();
 
     sess.namedQuery("getUserCalendarCollections");
     sess.setEntity("owner", user);
     sess.cacheableQuery();
 
     return access.checkAccess(sess.getList(), privWrite, noAccessReturnsNull);
   }
 
   public Collection getAddContentPublicCalendarCollections()
           throws CalFacadeException {
     HibSession sess = getSess();
 
     sess.namedQuery("getPublicCalendarCollections");
     sess.cacheableQuery();
 
     return access.checkAccess(sess.getList(), privWriteContent, noAccessReturnsNull);
   }
 
   public Collection getAddContentCalendarCollections()
           throws CalFacadeException {
     HibSession sess = getSess();
 
     sess.namedQuery("getUserCalendarCollections");
     sess.setEntity("owner", user);
     sess.cacheableQuery();
 
     return access.checkAccess(sess.getList(), privWriteContent, noAccessReturnsNull);
   }
 
   public BwCalendar getCalendar(int val) throws CalFacadeException {
     HibSession sess = getSess();
 
     sess.namedQuery("getCalendarById");
     sess.setInt("id", val);
     sess.cacheableQuery();
 
     BwCalendar cal = (BwCalendar)sess.getUnique();
 
     if (cal != null) {
       access.accessible(cal, privRead, false);
     }
 
     return cal;
   }
 
   public BwCalendar getCalendar(String path) throws CalFacadeException {
     HibSession sess = getSess();
 
     sess.namedQuery("getCalendarByPath");
     sess.setString("path", path);
     sess.cacheableQuery();
 
     BwCalendar cal = (BwCalendar)sess.getUnique();
 
     if (cal != null) {
       access.accessible(cal, privRead, false);
     }
 
     return cal;
   }
 
   public BwCalendar getDefaultCalendar(BwUser user) throws CalFacadeException {
     StringBuffer sb = new StringBuffer();
 
     sb.append("/");
     sb.append(getSyspars().getUserCalendarRoot());
     sb.append("/");
     sb.append(user.getAccount());
     sb.append("/");
     sb.append(getSyspars().getUserDefaultCalendar());
 
     return getCalendar(sb.toString());
   }
 
   public BwCalendar getTrashCalendar(BwUser user) throws CalFacadeException {
     StringBuffer sb = new StringBuffer();
 
     sb.append("/");
     sb.append(getSyspars().getUserCalendarRoot());
     sb.append("/");
     sb.append(user.getAccount());
     sb.append("/");
     sb.append(getSyspars().getDefaultTrashCalendar());
 
     return getCalendar(sb.toString());
   }
 
   public void addCalendar(BwCalendar val, BwCalendar parent) throws CalFacadeException {
     HibSession sess = getSess();
 
     /* We need write access to the parent */
     access.accessible(parent, privWrite, false);
 
     /** Is the parent a calendar collection?
      */
 /*    sess.namedQuery("countCalendarEventRefs");
     sess.setEntity("cal", parent);
 
     Integer res = (Integer)sess.getUnique();
 
     if (res.intValue() > 0) {*/
 
     if (parent.getCalendarCollection()) {
       throw new CalFacadeException(CalFacadeException.illegalCalendarCreation);
     }
 
     /* Ensure the path is unique */
     String path = parent.getPath();
     if (path == null) {
       if (parent.getPublick()) {
         path = "";
       } else {
         path = "/users/" + parent.getOwner().getAccount();
       }
     }
 
     path += "/" + val.getName();
 
     sess.namedQuery("getCalendarByPath");
     sess.setString("path", path);
 
     if (sess.getUnique() != null) {
       throw new CalFacadeException(CalFacadeException.duplicateCalendar);
     }
 
     val.setPath(path);
     val.setOwner(user);
     val.setCalendar(parent);
     parent.addChild(val);
 
     sess.save(parent);
   }
 
   public void updateCalendar(BwCalendar val) throws CalFacadeException {
     getSess().update(val);
   }
 
   public boolean deleteCalendar(BwCalendar val) throws CalFacadeException {
     HibSession sess = getSess();
 
     BwCalendar parent = val.getCalendar();
     if (parent == null) {
       throw new CalFacadeException(CalFacadeException.cannotDeleteCalendarRoot);
     }
 
     /* Objects are probably clones - fetch the real ones.
      */
     parent = getCalendar(parent.getPath());
     if (parent == null) {
       throw new CalFacadeException(CalFacadeException.cannotDeleteCalendarRoot);
     }
 
     val = getCalendar(val.getPath());
     if (val == null) {
       throw new CalFacadeException(CalFacadeException.calendarNotFound);
     }
 
     if (val.getChildren().size() > 0) {
       throw new CalFacadeException(CalFacadeException.calendarNotEmpty);
     }
 
     parent.removeChild(val);
     sess.update(parent);
 
     return true;
   }
 
   public boolean checkCalendarRefs(BwCalendar val) throws CalFacadeException {
     HibSession sess = getSess();
 
     sess.namedQuery("countCalendarEventRefs");
     sess.setEntity("cal", val);
 
     Integer res = (Integer)sess.getUnique();
 
     if (debug) {
       trace(" ----------- count = " + res);
     }
 
     if (res == null) {
       return false;
     }
 
     return res.intValue() > 0;
   }
 
   /* ====================================================================
    *                   Private methods
    * ==================================================================== */
 
   /* Returns the cloned (sub)tree of calendars to which user has access
    *
    * @return BwCalendar   (sub)root with all accessible children attached
    * @throws CalFacadeException
    */
   private BwCalendar cloneAndCheckAccess(BwCalendar root, int desiredAccess,
                            boolean nullForNoAccess) throws CalFacadeException {
     return cloneAndCheckOne(root, desiredAccess, nullForNoAccess);
   }
 
   private BwCalendar cloneAndCheckOne(BwCalendar subroot, int desiredAccess,
                            boolean nullForNoAccess) throws CalFacadeException {
     if (!access.accessible(subroot, desiredAccess, nullForNoAccess)) {
       return null;
     }
 
     BwCalendar cal = (BwCalendar)subroot.clone();
     // XXX Temp fix - add id to the clone
     cal.setId(subroot.getId());
 
     Iterator it = subroot.iterateChildren();
     while (it.hasNext()) {
       BwCalendar child = (BwCalendar)it.next();
 
       child = cloneAndCheckOne(child, desiredAccess, nullForNoAccess);
       if (child != null) {
         cal.addChild(child);
       }
     }
 
     return cal;
   }
 }
