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
 import org.bedework.calfacade.BwSystem;
 import org.bedework.calfacade.BwUser;
 import org.bedework.calfacade.ifs.CalendarsI;
 import org.bedework.calfacade.ifs.Calintf;
 import org.bedework.calfacade.CalFacadeException;
 
 import edu.rpi.cct.uwcal.access.Acl.CurrentAccess;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.TreeSet;
 
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
    * @param currentMode
    * @param debug
    * @throws CalFacadeException
    */
   public Calendars(Calintf cal, AccessUtil access,
                    int currentMode, boolean debug)
                   throws CalFacadeException {
     super(cal, access, currentMode, debug);
 
     publicCalendarRootPath = "/" + getSyspars().getPublicCalendarRoot();
     userCalendarRootPath = "/" + getSyspars().getUserCalendarRoot();
   }
 
   /** Called after a user has been added to the system.
    *
    * @param user
    * @throws CalFacadeException
    */
   public void addNewCalendars(BwUser user) throws CalFacadeException {
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
     cal.setCalType(BwCalendar.calTypeCollection);
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
 
     return postGet(sess.getList(), privWrite);
   }
 
   public BwCalendar getCalendars() throws CalFacadeException {
     return getCalendars(getUser(), privRead);
   }
 
   public BwCalendar getCalendars(BwUser user,
                                  int desiredAccess) throws CalFacadeException {
     HibSession sess = getSess();
 
     sess.namedQuery("getCalendarByPath");
     sess.setString("path", userCalendarRootPath + "/" + user.getAccount());
     sess.cacheableQuery();
 
     BwCalendar cal = (BwCalendar)sess.getUnique();
 
     return cloneAndCheckAccess(cal, desiredAccess, noAccessReturnsNull);
   }
 
   public Collection getCalendarCollections() throws CalFacadeException {
     HibSession sess = getSess();
 
     sess.namedQuery("getUserCalendarCollections");
     sess.setEntity("owner", getUser());
     sess.cacheableQuery();
 
     return postGet(sess.getList(), privWrite);
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
     sess.setEntity("owner", getUser());
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
       // Need to clone for this
       //cal.setCurrentAccess(access.checkAccess(cal, privRead, false));
       access.checkAccess(cal, privRead, false);
     }
 
     return cal;
   }
 
   public BwCalendar getCalendar(String path,
                                 int desiredAccess) throws CalFacadeException {
     return getCalendar(path, desiredAccess, true);
   }
 
   private BwCalendar getCalendar(String path,
                                  int desiredAccess,
                                  boolean cloneIt) throws CalFacadeException {
     HibSession sess = getSess();
 
     sess.namedQuery("getCalendarByPath");
     sess.setString("path", path);
     sess.cacheableQuery();
 
     BwCalendar cal = (BwCalendar)sess.getUnique();
 
     if (cal != null) {
       // Need to clone for this
       if (!cloneIt) {
         access.checkAccess(cal, desiredAccess, false);
       } else {
         cal = cloneAndCheckOne(cal, desiredAccess, false);
       }
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
 
     return getCalendar(sb.toString(), privRead);
   }
 
   public BwCalendar getSpecialCalendar(BwUser user,
                                        int calType,
                                        boolean create) throws CalFacadeException {
     StringBuffer sb = new StringBuffer();
     String name;
     BwSystem sys = getSyspars();
 
     if (calType == BwCalendar.calTypeBusy) {
       name = sys.getBusyCalendar();
     } else if (calType == BwCalendar.calTypeDeleted) {
       name = sys.getDeletedCalendar();
     } else if (calType == BwCalendar.calTypeInbox) {
       name = sys.getUserInbox();
     } else if (calType == BwCalendar.calTypeOutbox) {
       name = sys.getUserOutbox();
     } else if (calType == BwCalendar.calTypeTrash) {
       name = sys.getDefaultTrashCalendar();
     } else {
       // Not supported
       return null;
     }
 
     sb.append(userCalendarRootPath);
     sb.append("/");
     sb.append(user.getAccount());
 
     String pathTo = sb.toString();
 
     sb.append("/");
     sb.append(name);
 
     BwCalendar cal = getCalendar(sb.toString(), privRead);
 
     if ((cal != null) || !create) {
       return cal;
     }
 
     /*
     BwCalendar parent = getCalendar(pathTo, privRead);
 
     if (parent == null) {
       throw new CalFacadeException("org.bedework.calcore.calendars.unabletocreate");
     }
     */
 
     cal = new BwCalendar();
     cal.setName(name);
     cal.setOwner(user);
     cal.setCreator(user);
     cal.setCalendarCollection(true);
     cal.setCalType(calType);
     addCalendar(cal, pathTo);
 
     return cal;
   }
 
   public void addCalendar(BwCalendar val, String parentPath) throws CalFacadeException {
     HibSession sess = getSess();
 
     /* We need write content access to the parent */
     BwCalendar parent = getCalendar(parentPath, privWriteContent, false);
     if (parent == null) {
       throw new CalFacadeException("org.bedework.error.nosuchcalendarpath",
                                    parentPath);
     }
 
     /** Is the parent a calendar collection?
      */
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
     if (val.getOwner() == null) {
       val.setOwner(getUser());
     }
     val.setCalendar(parent);
     val.setPublick(parent.getPublick());
     if (val.getCalendarCollection()) {
       val.setCalType(BwCalendar.calTypeCollection);
     } else {
       val.setCalType(BwCalendar.calTypeFolder);
     }
     parent.addChild(val);
 
     sess.update(parent);
   }
 
   public void updateCalendar(BwCalendar val) throws CalFacadeException {
    access.checkAccess(val, privWrite, false);
     getSess().update(val);
   }
 
   public void changeAccess(BwCalendar cal,
                            Collection aces) throws CalFacadeException {
     HibSession sess = getSess();
 
     cal = getCalendar(cal.getPath(), privWriteAcl, false);
     access.changeAccess(cal, aces);
     sess.saveOrUpdate(cal);
   }
 
   public boolean deleteCalendar(BwCalendar val) throws CalFacadeException {
     HibSession sess = getSess();
 
     BwCalendar parent = val.getCalendar();
     if (parent == null) {
       throw new CalFacadeException(CalFacadeException.cannotDeleteCalendarRoot);
     }
 
     /* Objects are probably clones - fetch the real ones.
      */
     parent = getCalendar(parent.getPath(), privRead, false);
     if (parent == null) {
       throw new CalFacadeException(CalFacadeException.cannotDeleteCalendarRoot);
     }
 
     val = getCalendar(val.getPath(), privUnbind, false);
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
 
   /* Return a Collection of the calendars after checking access
    *
    */
   private Collection postGet(Collection cals, int desiredAccess)
              throws CalFacadeException {
     TreeSet out = new TreeSet();
 
     Iterator it = cals.iterator();
 
     while (it.hasNext()) {
       BwCalendar cal = (BwCalendar)it.next();
       CurrentAccess ca = access.checkAccess(cal, desiredAccess,
                                             noAccessReturnsNull);
       if (ca != null) {
         //cal.setCurrentAccess(ca);
         out.add(cal);
       }
     }
 
     return out;
   }
 
   /* Returns the cloned (sub)tree of calendars to which user has access
    *
    * <p>This needs a rethink - cloning effectively disables optimistic
    * locking - at least I think so.
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
     CurrentAccess ca = access.checkAccess(subroot, desiredAccess,
                                           nullForNoAccess);
 
     if (!ca.accessAllowed) {
       return null;
     }
 
     BwCalendar cal = (BwCalendar)subroot.shallowClone();
     // XXX Temp fix - add id to the clone - only until we get rid of ids in
     //  admin preferred calendars
     cal.setId(subroot.getId());
 
     cal.setCurrentAccess(ca);
 
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
