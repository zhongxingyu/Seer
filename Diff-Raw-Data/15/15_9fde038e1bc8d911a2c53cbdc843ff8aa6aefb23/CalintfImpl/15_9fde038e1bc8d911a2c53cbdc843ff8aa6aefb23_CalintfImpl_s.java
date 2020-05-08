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
 
 import edu.rpi.cct.uwcal.access.PrivilegeDefs;
 
 import org.bedework.calenv.CalEnv;
 import org.bedework.calfacade.BwAlarm;
 import org.bedework.calfacade.BwCalendar;
 import org.bedework.calfacade.BwCategory;
 import org.bedework.calfacade.BwDateTime;
 import org.bedework.calfacade.BwEvent;
 import org.bedework.calfacade.BwLocation;
 import org.bedework.calfacade.BwRWStats;
 import org.bedework.calfacade.BwSponsor;
 import org.bedework.calfacade.BwStats;
 import org.bedework.calfacade.BwSynchData;
 import org.bedework.calfacade.BwSynchInfo;
 import org.bedework.calfacade.BwSynchState;
 import org.bedework.calfacade.BwSystem;
 import org.bedework.calfacade.BwTimeZone;
 import org.bedework.calfacade.BwUser;
 import org.bedework.calfacade.CalFacadeAccessException;
 import org.bedework.calfacade.CalFacadeDefs;
 import org.bedework.calfacade.CalFacadeException;
 import org.bedework.calfacade.filter.BwFilter;
 import org.bedework.calfacade.ifs.CalTimezones;
 import org.bedework.calfacade.ifs.Calintf;
 import org.bedework.calfacade.ifs.CalintfInfo;
 import org.bedework.calfacade.ifs.Groups;
 import org.bedework.icalendar.IcalTranslator;
 
 import java.sql.Timestamp;
 import java.util.Collection;
 import java.util.TreeSet;
 
 import net.fortuna.ical4j.model.Calendar;
 import net.fortuna.ical4j.model.Component;
 import net.fortuna.ical4j.model.component.VTimeZone;
 
 import org.apache.log4j.Logger;
 
 import org.hibernate.cfg.Configuration;
 import org.hibernate.SessionFactory;
 
 /** Implementation of CalIntf which uses hibernate as its persistance engine.
  *
  * <p>We assume this interface is accessing public events or private events.
  * In either case it may be read-only or read/write. It is up to the caller
  * to set the appropriate access.
  *
  * <p>Write access to public objects may be restricted to only those owned
  * by the supplied owner. It is up to the caller to determine the setting for
  * the modifyAll flag.
  *
  * <p>The following methods always work within the above context, e.g. 'all'
  * for an object initialised for public access means all public objects of
  * a requested class. For a personal object it means all objects owned by
  * the current user.
  *
  * <p>Currently some classes are only available as public objects. This
  * might change.
  *
  * <p>A public object in readonly mode will deliver all public objects
  * within the given constraints, regardless of ownership, e.g all events for
  * given day or all public categories.
  *
  * <p>A public object in read/write will enforce ownership on display and
  * on update. This might require a client to obtain two or more objects to
  * get the appropriate behaviour. For example, an admin client will only
  * allow update of events owned by the current user but must display all
  * public categories for use.
  *
  * @author Mike Douglass   douglm@rpi.edu
  */
 public class CalintfImpl implements Calintf, PrivilegeDefs {
   private boolean debug;
 
   private BwSystem syspars;
 
   private BwStats stats = new BwRWStats();
 
   private CalTimezonesImpl timezones;
 
   private static CalintfInfo info = new CalintfInfo(
        true,     // handlesLocations
        true,     // handlesSponsors
        true      // handlesCategories
      );
 
   /** For evaluating access control
    */
   private AccessUtil access;
 
   /* From core environment properties * /
   private String systemId;
 
   private String publicCalendarRoot;
   private String userCalendarRoot;
   private String userDefaultCalendar;
   private String defaultTrashCalendar;
   */
   private String publicCalendarRootPath;
   private String userCalendarRootPath;
 
   /** Ensure we don't open while open
    */
   private boolean isOpen;
 
   /* * This is all the personal calendars modified while the current transaction
    *  has been in progress. Just before commit we update the lastmod off all
    *  the users on this list.
    * /
   private Vector personalModified;
   */
 
   /** User for whom we maintain this facade
    */
   private BwUser user;
 
   private Events events;
 
   private EventProperties categories;
 
   private EventProperties locations;
 
   private EventProperties sponsors;
 
   /** True if this is superuser access
    */
   private boolean superUser;
 
   private int currentMode = CalintfUtil.guestMode;
 
   /** Non-null if this is for synchronization. Identifies the client end.
    */
   private String synchId;
 
   /** Prevent updates.
    */
   //sprivate boolean readOnly;
 
   /** Ignore owner for superuser
    */
   private boolean ignoreCreator;
 
   /** Current hibernate session - exists only across one user interaction
    */
   private HibSession sess;
 
   /** We make this static for this implementation so that there is only one
    * SessionFactory per server for the calendar.
    *
    * <p>static fields used this way are illegal in the j2ee specification
    * though we might get away with it here as the session factory only
    * contains parsed mappings for the calendar configuration. This should
    * be the same for any machine in a cluster so it might work OK.
    *
    * <p>It might be better to find some other approach for the j2ee world.
    */
   private static SessionFactory sessFactory;
 
   static {
     /** Get a new hibernate session factory. This is configured from an
      * application resource hibernate.cfg.xml
      */
     try {
       sessFactory = new Configuration().configure().buildSessionFactory();
     } catch (Throwable t) {
       Logger.getLogger(CalintfImpl.class).error("Failed to get session factory", t);
     }
   }
 
   private transient Logger log;
 
   /** When we were created for debugging */
   private Timestamp objTimestamp;
 
   /* ====================================================================
    *                   initialisation
    * ==================================================================== */
 
   /* (non-Javadoc)
    * @see org.bedework.calfacade.Calintf#init(org.bedework.calfacade.BwUser, java.lang.String, boolean, boolean, boolean, java.lang.String, boolean)
    */
   public boolean init(String authenticatedUser,
                       String user,
                       boolean publicAdmin,
                       boolean superUser,
                       Groups groups,
                       String synchId,
                       boolean debug) throws CalFacadeException {
     this.debug = debug;
     boolean userCreated = false;
 
     if (authenticatedUser == null) {
       this.superUser = false;  // be safe
     }
 
     BwUser authUser;
 
     try {
       access = new AccessUtil(superUser, debug);
 
       objTimestamp = new Timestamp(System.currentTimeMillis());
 
       this.superUser = superUser;
       this.synchId = synchId;
       log = Logger.getLogger(getClass());
 
       if ((synchId != null) && publicAdmin) {
         throw new CalFacadeException("synch only valid for non admin");
       }
 
       /** Define the roots of the calendars.
        * /
       publicCalendarRoot = CalEnv.getGlobalProperty("public.calroot");
       userCalendarRoot = CalEnv.getGlobalProperty("user.calroot");
       userDefaultCalendar = CalEnv.getGlobalProperty("default.user.calendar");
       defaultTrashCalendar = CalEnv.getGlobalProperty("default.trash.calendar");
 
       systemId = CalEnv.getGlobalProperty("systemid");
       */
     } catch (Throwable t) {
       throw new CalFacadeException(t);
     }
 
     publicCalendarRootPath = "/" + getSyspars().getPublicCalendarRoot();
     userCalendarRootPath = "/" + getSyspars().getUserCalendarRoot();
     //systemId = getSyspars().getSystemid();
 
     if (user == null) {
       user = authenticatedUser;
     }
 
     ignoreCreator = false;
 
     if (authenticatedUser == null) {
       // Unauthenticated use
       // Ensure no guest mods
 //      this.readOnly = true;
       currentMode = CalintfUtil.guestMode;
       authUser = new BwUser();
       this.user = getUser(user);
     } else {
 //      this.readOnly = false;
 
       authUser = getUser(authenticatedUser);
       if (authUser == null) {
         /* Add the user to the database. Presumably this is first logon
          */
         getLogger().info("Add new user " + authenticatedUser);
         addUser(new BwUser(authenticatedUser));
         authUser = getUser(authenticatedUser);
         userCreated = true;
       }
 
       if (!publicAdmin) {
         currentMode = CalintfUtil.userMode;
       } else {
         currentMode = CalintfUtil.publicAdminMode;
       }
 
       logon(authUser);
 
       getLogger().info("Authenticated user " + authenticatedUser +
                        " logged on");
       ignoreCreator = superUser;
 
       if (authenticatedUser.equals(user)) {
         this.user = authUser;
       } else {
         this.user = getUser(user);
         if (this.user == null) {
           throw new CalFacadeException("User " + user + " does not exist.");
         }
       }
     }
 
     authUser.setGroups(groups.getAllGroups(authUser));
     access.setAuthUser(authUser);
 
     events = new Events(this, access, sess, this.user, debug);
 
     categories = new EventProperties(this,
                                      "word", BwCategory.class.getName(),
                                      "getCategoryRefs",
                                      -1, debug);
     locations = new EventProperties(this,
                                     "address", BwLocation.class.getName(),
                                      "getLocationRefs",
                                      CalFacadeDefs.maxReservedLocationId, debug);
     sponsors = new EventProperties(this,
                                    "name", BwSponsor.class.getName(),
                                    "getSponsorRefs",
                                    CalFacadeDefs.maxReservedSponsorId, debug);
 
     timezones = new CalTimezonesImpl(this, getStats(), publicAdmin, debug);
     timezones.setDefaultTimeZoneId(getSyspars().getTzid());
 
     return userCreated;
   }
 
   public BwStats getStats() throws CalFacadeException {
     return stats;
   }
 
   public BwSystem getSyspars() throws CalFacadeException {
     if (syspars == null) {
       String name;
 
       try {
         name = CalEnv.getGlobalProperty("system.name");
       } catch (Throwable t) {
         throw new CalFacadeException(t);
       }
 
       sess.namedQuery("getSystemPars");
 
       sess.setString("name", name);
 
       syspars = (BwSystem)sess.getUnique();
 
       if (syspars == null) {
         throw new CalFacadeException("No system parameters with name " + name);
       }
 
       if (debug) {
         trace("Read system parameters: " + syspars);
       }
     }
     return syspars;
   }
 
   public void updateSyspars(BwSystem val) throws CalFacadeException {
     checkOpen();
     sess.update(val);
   }
 
   public CalTimezones getTimezones() throws CalFacadeException {
     return timezones;
   }
 
   public CalintfInfo getInfo() throws CalFacadeException {
     return info;
   }
 
   public boolean getDebug() throws CalFacadeException {
     return debug;
   }
 
   public void setUser(String val) throws CalFacadeException {
     refreshEvents();
 
     user = getUser(val);
     if (this.user == null) {
       throw new CalFacadeException("User " + val + " does not exist.");
     }
 
     logon(user);
 
     if (debug) {
       log.debug("User " + val + " set in calintf");
     }
   }
 
   /* ====================================================================
    *                   Misc methods
    * ==================================================================== */
 
   public void flushAll() throws CalFacadeException {
     if (debug) {
       log.debug("flushAll for " + objTimestamp);
     }
     if (sess == null) {
       return;
     }
 
     try {
     if (sess.isOpen()) {
       sess.reconnect();  // for close
       sess.close();
     }
     } finally {
       isOpen = false;
       sess = null;
     }
   }
 
   public synchronized void open() throws CalFacadeException {
     if (isOpen) {
       throw new CalFacadeException("Already open");
     }
 
     isOpen = true;
 
     if (sess == null) {
       if (debug) {
         log.debug("New hibernate session for " + objTimestamp);
       }
       sess = new HibSession(sessFactory, log);
       if (events != null) {
         events.setHibSession(sess);
       }
     } else {
       if (debug) {
         log.debug("Reconnect hibernate session for " + objTimestamp);
       }
       sess.reconnect();
     }
 
     if (access != null) {
       access.open();
     }
 
 //    personalModified = new Vector();
   }
 
   public synchronized void close() throws CalFacadeException {
     if (!isOpen) {
       if (debug) {
         log.debug("Close for " + objTimestamp + " closed session");
       }
       return;
     }
 
     if (debug) {
       log.debug("Close for " + objTimestamp);
     }
 
     try {
       if (sess != null) {
         sess.disconnect();
       }
     } catch (Throwable t) {
       try {
         sess.close();
       } finally {}
       sess = null; // Discard on error
     } finally {
       isOpen = false;
     }
 
     if (access != null) {
       access.close();
     }
   }
 
   public void beginTransaction() throws CalFacadeException {
     checkOpen();
 //    sess.close();
     if (debug) {
       log.debug("Begin transaction for " + objTimestamp);
     }
     sess.beginTransaction();
   }
 
   public void endTransaction() throws CalFacadeException {
     checkOpen();
 
     if (debug) {
       log.debug("End transaction for " + objTimestamp);
     }
 
     /* Update the lastmods for any changed users
      * /
 
     Iterator it = personalModified.iterator();
 
     while (it.hasNext()) {
       BwUser u = (BwUser)it.next();
     }*/
 
     /* Just commit */
     sess.flush();
     sess.commit();
   }
 
   public void rollbackTransaction() throws CalFacadeException {
     checkOpen();
     sess.rollback();
   }
 
   public Object getDbSession() throws CalFacadeException {
     return sess;
   }
 
   /* ====================================================================
    *                   General data methods
    * ==================================================================== */
 
   /*
   public void refresh() throws CalFacadeException {
     checkOpen();
     sess.flush();
   }*/
 
   public void refreshEvents() throws CalFacadeException {
     checkOpen();
     sess.flush();
   }
 
   /*
   public void lockRead(Object val) throws CalFacadeException {
     checkOpen();
     sess.lockRead(val);
   }
 
   public void lockMod(Object val) throws CalFacadeException {
     checkOpen();
     sess.lockUpdate(val);
   }
   */
 
   /* ====================================================================
    *                   Global parameters
    * ==================================================================== */
 
   public long getPublicLastmod() throws CalFacadeException {
     checkOpen();
     return 0; // for the moment
   }
 
   public String getSysid() throws CalFacadeException {
     return getSyspars().getSystemid();
   }
 
   /* ====================================================================
    *                   Users
    * ==================================================================== */
 
   public BwUser getUser() throws CalFacadeException {
     return user;
   }
 
   public BwUser getUser(int id) throws CalFacadeException {
     checkOpen();
     StringBuffer q = new StringBuffer();
 
     q.append("from ");
     q.append(BwUser.class.getName());
     q.append(" as u where u.userid = :userid");
 
     sess.createQuery(q.toString());
 
     sess.setInt("userid", id);
 
     return (BwUser)sess.getUnique();
   }
 
 
   public BwUser getUser(String user) throws CalFacadeException {
     checkOpen();
     StringBuffer q = new StringBuffer();
 
     q.append("from ");
     q.append(BwUser.class.getName());
     q.append(" as u where u.account = :account");
     sess.createQuery(q.toString());
 
     sess.setString("account", user);
 
     return (BwUser)sess.getUnique();
   }
 
   public void addUser(BwUser user) throws CalFacadeException {
     checkOpen();
 
     user.setCategoryAccess(access.getDefaultPersonalAccess());
     user.setLocationAccess(access.getDefaultPersonalAccess());
     user.setSponsorAccess(access.getDefaultPersonalAccess());
 
     user.setQuota(getSyspars().getDefaultUserQuota());
 
     sess.save(user);
 
     /* Add a user collection to the userCalendarRoot and then a default
        calendar collection. */
 
     sess.namedQuery("getCalendarByPath");
 
     String path =  "/" + getSyspars().getUserCalendarRoot();
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
 
    sess.save(user);
   }
 
   public void updateUser() throws CalFacadeException {
     updateUser(getUser());
   }
 
   public void updateUser(BwUser user) throws CalFacadeException {
     checkOpen();
    sess.save(user);
   }
 
   public Collection getInstanceOwners() throws CalFacadeException {
     checkOpen();
     StringBuffer q = new StringBuffer();
 
     q.append("from ");
     q.append(BwUser.class.getName());
     q.append(" as u where u.instanceOwner=true");
     sess.createQuery(q.toString());
 
     return sess.getList();
   }
 
   /*
   public void deleteUser(BwUser user) throws CalFacadeException {
     checkOpen();
     throw new CalFacadeException("Unimplemented");
   }*/
 
   private void logon(BwUser val) throws CalFacadeException {
     checkOpen();
     Timestamp now = new Timestamp(System.currentTimeMillis());
 
     val.setLogon(now);
     val.setLastAccess(now);
    sess.saveOrUpdate(val);
   }
 
   /* ====================================================================
    *                   Access
    * ==================================================================== */
 
   public void changeAccess(Object o, Collection aces) throws CalFacadeException {
     checkOpen();
     access.changeAccess(o, aces);
     sess.saveOrUpdate(o);
   }
 
   public Collection getAces(Object o) throws CalFacadeException {
     checkOpen();
     return access.getAces(o);
   }
 
   /* ====================================================================
    *                   Timezones
    * ==================================================================== */
 
   public void saveTimeZone(String tzid, VTimeZone vtz) throws CalFacadeException {
     checkOpen();
 
     BwTimeZone tz = new BwTimeZone();
 
     if (currentMode == CalintfUtil.publicAdminMode) {
       requireSuper();
     }
 
     if (currentMode == CalintfUtil.guestMode) {
       throw new CalFacadeAccessException();
     }
 
     tz.setTzid(tzid);
     tz.setPublick(currentMode == CalintfUtil.publicAdminMode);
     tz.setOwner(user);
 
     StringBuffer sb = new StringBuffer();
 
     sb.append("BEGIN:VCALENDAR\n");
     sb.append("PRODID:-//RPI//BEDEWORK//US\n");
     sb.append("VERSION:2.0\n");
     sb.append(vtz.toString());
     sb.append("END:VCALENDAR\n");
 
     tz.setVtimezone(sb.toString());
 
     sess.save(tz);
   }
 
   public VTimeZone getTimeZone(final String id, BwUser owner) throws CalFacadeException {
     if (currentMode == CalintfUtil.publicAdminMode ||
         currentMode == CalintfUtil.guestMode) {
       sess.namedQuery("getPublicTzByTzid");
     } else {
       if (owner == null) {
         owner = user;
       }
       sess.namedQuery("getTzByTzid");
       sess.setEntity("owner", owner);
     }
 
     sess.setString("tzid", id);
 
     BwTimeZone tz = (BwTimeZone)sess.getUnique();
 
     if (tz == null) {
       return null;
     }
 
     Calendar cal = IcalTranslator.getCalendar(tz.getVtimezone());
 
     VTimeZone vtz = (VTimeZone)cal.getComponents().getComponent(Component.VTIMEZONE);
     if (vtz == null) {
       throw new CalFacadeException("Incorrectly stored timezone");
     }
 
     return vtz;
   }
 
   public Collection getTimeZones() throws CalFacadeException {
     if (currentMode == CalintfUtil.publicAdminMode ||
         currentMode == CalintfUtil.guestMode) {
       sess.namedQuery("getPublicTimezones");
 
       return sess.getList();
     }
 
     sess.namedQuery("getMergedTimezones");
     sess.setEntity("owner", user);
 
     return sess.getList();
   }
 
   public Collection getPublicTimeZones() throws CalFacadeException {
     sess.namedQuery("getPublicTimezones");
 
     return sess.getList();
   }
 
   public void clearPublicTimezones() throws CalFacadeException {
     checkOpen();
     requireSuper();
 
     /* Delete all public timezones */
     sess.namedQuery("deleteAllPublicTzs");
     /*int numDeleted =*/ sess.executeUpdate();
   }
 
   /* ====================================================================
    *                   Calendars and search
    * ==================================================================== */
 
   public BwCalendar getPublicCalendars() throws CalFacadeException {
     checkOpen();
 
     sess.namedQuery("getCalendarByPath");
     sess.setString("path", publicCalendarRootPath);
     sess.cacheableQuery();
 
     /* XXX access checks */
     return (BwCalendar)sess.getUnique();
   }
 
   public Collection getPublicCalendarCollections() throws CalFacadeException {
     checkOpen();
 
     sess.namedQuery("getPublicCalendarCollections");
     sess.cacheableQuery();
 
     return access.checkAccess(sess.getList(), privWrite, true);
   }
 
   public BwCalendar getCalendars() throws CalFacadeException {
     if (currentMode == CalintfUtil.guestMode) {
       return getPublicCalendars();
     }
 
     checkOpen();
 
     sess.namedQuery("getCalendarByPath");
     sess.setString("path", userCalendarRootPath + "/" + user.getAccount());
     sess.cacheableQuery();
 
     return (BwCalendar)sess.getUnique();
   }
 
   public Collection getCalendarCollections() throws CalFacadeException {
     checkOpen();
 
     sess.namedQuery("getUserCalendarCollections");
     sess.setEntity("owner", user);
     sess.cacheableQuery();
 
     return access.checkAccess(sess.getList(), privWrite, true);
   }
 
   public Collection getAddContentPublicCalendarCollections()
           throws CalFacadeException {
     checkOpen();
 
     sess.namedQuery("getPublicCalendarCollections");
     sess.cacheableQuery();
 
     return access.checkAccess(sess.getList(), privWriteContent, true);
   }
 
   public Collection getAddContentCalendarCollections()
           throws CalFacadeException {
     if (currentMode == CalintfUtil.guestMode) {
       return new TreeSet();
     }
 
     checkOpen();
 
     sess.namedQuery("getUserCalendarCollections");
     sess.setEntity("owner", user);
     sess.cacheableQuery();
 
     return access.checkAccess(sess.getList(), privWriteContent, true);
   }
 
   public BwCalendar getCalendar(int val) throws CalFacadeException {
     checkOpen();
 
     sess.namedQuery("getCalendarById");
     sess.setInt("id", val);
     sess.cacheableQuery();
 
     return (BwCalendar)sess.getUnique();
   }
 
   public BwCalendar getCalendar(String path) throws CalFacadeException{
     checkOpen();
 
     sess.namedQuery("getCalendarByPath");
     sess.setString("path", path);
     sess.cacheableQuery();
 
     BwCalendar cal = (BwCalendar)sess.getUnique();
 
     if (cal != null) {
       access.accessible(cal, privRead, false);
     }
 
     return cal;
   }
 
   public BwCalendar getDefaultCalendar() throws CalFacadeException {
     StringBuffer sb = new StringBuffer();
 
     sb.append("/");
     sb.append(getSyspars().getUserCalendarRoot());
     sb.append("/");
     sb.append(user.getAccount());
     sb.append("/");
     sb.append(getSyspars().getUserDefaultCalendar());
 
     return getCalendar(sb.toString());
   }
 
   public BwCalendar getTrashCalendar() throws CalFacadeException {
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
     checkOpen();
 
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
     checkOpen();
     sess.update(val);
   }
 
   public boolean deleteCalendar(BwCalendar val) throws CalFacadeException {
     checkOpen();
 
     BwCalendar parent = val.getCalendar();
     if (parent == null) {
       throw new CalFacadeException(CalFacadeException.cannotDeleteCalendarRoot);
     }
 
     //sess.delete(val);
     parent.removeChild(val);
     sess.update(parent);
 
     return true;
   }
 
   public boolean checkCalendarRefs(BwCalendar val) throws CalFacadeException {
     checkOpen();
 
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
    *                   Filters and search
    * ==================================================================== */
 
   public void setSearch(String val) throws CalFacadeException {
     checkOpen();
   }
 
   public String getSearch() throws CalFacadeException {
     checkOpen();
 
     return null;
   }
 
   /*
   public BwFilter getFilter(String name) throws CalFacadeException {
     checkOpen();
     if (currentMode != CalintfUtil.guestMode) {
       return null;
     }
 
     sess.createQuery("from " + BwFilter.class.getName() + " as cal " +
                      "where cal.name = :name");
     sess.setString("name", name);
 
     return (BwFilter)sess.getUnique();
   }
 
   public BwFilter getFilter(int id) throws CalFacadeException {
     checkOpen();
 
 /*    Object o = sess.get(FilterVO.class, id);
     if (o != null) {
       return (FilterVO)o;
     }
 * /
     sess.createQuery("from " + BwFilter.class.getName() + " as cal " +
                      "where cal.id = :id");
     sess.setInt("id", id);
 
     return (BwFilter)sess.getUnique();
   } */
 
   public void addFilter(BwFilter val) throws CalFacadeException {
     checkOpen();
     sess.save(val);
   }
 
   public void updateFilter(BwFilter val) throws CalFacadeException {
     checkOpen();
     sess.saveOrUpdate(val);
   }
 
   /* ====================================================================
    *                   Categories
    * ==================================================================== */
 
   public Collection getCategories(BwUser owner, BwUser creator)
         throws CalFacadeException {
     checkOpen();
 
     return access.checkAccess(categories.get(owner, creator), privRead, true);
   }
 
   public BwCategory getCategory(int id) throws CalFacadeException {
     checkOpen();
 
     return (BwCategory)categories.get(id, currentMode, ignoreCreator);
   }
 
   public BwCategory findCategory(BwCategory val) throws CalFacadeException {
     checkOpen();
 
     return (BwCategory)categories.find(val.getWord(), val.getOwner());
   }
 
   public void addCategory(BwCategory val) throws CalFacadeException {
     checkOpen();
 
     categories.add(val);
   }
 
   public void updateCategory(BwCategory val) throws CalFacadeException {
     checkOpen();
 
     categories.update(val);
   }
 
   public boolean deleteCategory(BwCategory val) throws CalFacadeException {
     checkOpen();
 
     categories.delete(val);
 
     return true;
   }
 
   public Collection getCategoryRefs(BwCategory val) throws CalFacadeException {
     checkOpen();
 
     return categories.getRefs(val);
   }
 
   /* ====================================================================
    *                   Locations
    * ==================================================================== */
 
   public Collection getLocations(BwUser owner, BwUser creator)
         throws CalFacadeException {
     checkOpen();
 
     return access.checkAccess(locations.get(owner, creator), privRead, true);
   }
 
   public BwLocation getLocation(int id) throws CalFacadeException {
     checkOpen();
 
     return (BwLocation)locations.get(id, currentMode, ignoreCreator);
   }
 
   public BwLocation findLocation(BwLocation val) throws CalFacadeException {
     checkOpen();
 
     return (BwLocation)locations.find(val.getAddress(), val.getOwner());
   }
 
   public void addLocation(BwLocation val) throws CalFacadeException {
     checkOpen();
 
     locations.add(val);
   }
 
   public void updateLocation(BwLocation val) throws CalFacadeException {
     checkOpen();
 
     locations.update(val);
   }
 
   public boolean deleteLocation(BwLocation val) throws CalFacadeException {
     checkOpen();
 
     locations.delete(val);
 
     return true;
   }
 
   public Collection getLocationRefs(BwLocation val) throws CalFacadeException {
     checkOpen();
 
     return locations.getRefs(val);
   }
 
   /* ====================================================================
    *                   Sponsors
    * ==================================================================== */
 
   public Collection getSponsors(BwUser owner, BwUser creator)
         throws CalFacadeException {
     checkOpen();
 
     return access.checkAccess(sponsors.get(owner, creator), privRead, true);
   }
 
   public BwSponsor getSponsor(int id) throws CalFacadeException {
     checkOpen();
 
     return (BwSponsor)sponsors.get(id, currentMode, ignoreCreator);
   }
 
   public BwSponsor findSponsor(BwSponsor val) throws CalFacadeException {
     checkOpen();
 
     return (BwSponsor)sponsors.find(val.getName(), val.getOwner());
   }
 
   public void addSponsor(BwSponsor val) throws CalFacadeException {
     checkOpen();
 
     sponsors.add(val);
   }
 
   public void updateSponsor(BwSponsor val) throws CalFacadeException {
     checkOpen();
 
     sponsors.update(val);
   }
 
   public boolean deleteSponsor(BwSponsor val) throws CalFacadeException {
     checkOpen();
 
     sponsors.delete(val);
 
     return true;
   }
 
   public Collection getSponsorRefs(BwSponsor val) throws CalFacadeException {
     checkOpen();
 
     return sponsors.getRefs(val);
   }
 
   /* ====================================================================
    *                   Events
    * ==================================================================== */
 
   public Collection getEvents(BwCalendar calendar, BwFilter filter,
                               BwDateTime startDate, BwDateTime endDate,
                               int recurRetrieval)
           throws CalFacadeException {
     return events.getEvents(calendar, filter,
                             startDate, endDate, recurRetrieval,
                             currentMode, ignoreCreator);
   }
 
   public BwEvent getEvent(int id) throws CalFacadeException {
     checkOpen();
     return events.getEvent(id);
   }
 
  public Collection getEvent(String guid, String rid,
                            Integer seqnum,
                            int recurRetrieval) throws CalFacadeException {
     checkOpen();
     return events.getEvent(guid, rid, seqnum, recurRetrieval);
   }
 
   public void addEvent(BwEvent val,
                        Collection overrides) throws CalFacadeException {
     checkOpen();
     events.addEvent(val, overrides);
   }
 
   public void updateEvent(BwEvent val) throws CalFacadeException {
     checkOpen();
     events.updateEvent(val);
   }
 
   public DelEventResult deleteEvent(BwEvent val) throws CalFacadeException {
     checkOpen();
     return events.deleteEvent(val);
   }
 
   public boolean editable(BwEvent val) throws CalFacadeException {
     checkOpen();
 
     if (currentMode == CalintfUtil.guestMode) {
       return false;
     }
 
     if (val.getPublick() != (currentMode == CalintfUtil.publicAdminMode)) {
       return false;
     }
 
     return user.equals(val.getCreator());
   }
 
   /* ====================================================================
    *                       Caldav support
    * Caldav as it stands at the moment requires that we save the arbitary
    * names clients might assign to events.
    * ==================================================================== */
 
   public Collection getEventsByName(BwCalendar cal, String val)
           throws CalFacadeException {
     checkOpen();
     return events.getEventsByName(cal, val);
   }
 
   /* ====================================================================
    *                   Synchronization
    * ==================================================================== */
 
   public BwSynchInfo getSynchInfo() throws CalFacadeException {
     checkOpen();
 
     if (debug) {
       getLogger().debug("getSynchInfo for user=" + user +
                         " deviceId=" + synchId);
     }
 
     sess.namedQuery("getSynchInfo");
     sess.setEntity("user", user);
     sess.setString("deviceId", synchId);
 
     BwSynchInfo si = (BwSynchInfo)sess.getUnique();
 
     return si;
   }
 
   public void addSynchInfo(BwSynchInfo val) throws CalFacadeException {
     checkOpen();
 
     if (debug) {
       getLogger().debug("addSynchInfo for user=" + val.getUser() +
                         " deviceId=" + val.getDeviceId());
     }
 
     sess.save(val);
   }
 
   public void updateSynchInfo(BwSynchInfo val) throws CalFacadeException {
     checkOpen();
 
     sess.saveOrUpdate(val);
   }
 
   public BwSynchState getSynchState(BwEvent ev)
       throws CalFacadeException {
     checkOpen();
 
     sess.namedQuery("getSynchState");
     sess.setEntity("event", ev);
     sess.setEntity("user", user);
     sess.setString("deviceId", synchId);
 
     BwSynchState ss = (BwSynchState)sess.getUnique();
 
     return ss;
   }
 
   public Collection getDeletedSynchStates() throws CalFacadeException {
     checkOpen();
 
     sess.namedQuery("getDeletedSynchStates");
     sess.setEntity("user", user);
     sess.setString("deviceId", synchId);
     sess.setInt("synchState", BwSynchState.DELETED);
 
     return sess.getList();
   }
 
   public void addSynchState(BwSynchState val) throws CalFacadeException {
     checkOpen();
 
     sess.save(val);
   }
 
   public void updateSynchState(BwSynchState val) throws CalFacadeException {
     checkOpen();
 
     sess.saveOrUpdate(val);
   }
 
   public int setSynchState(BwEvent ev, int state)
         throws CalFacadeException {
     checkOpen();
 
     sess.namedQuery("setSynchState");
     sess.setInt("state", state);
     sess.setInt("eventid", ev.getId());
 
     return sess.executeUpdate();
   }
 
   private int setSynchStateUser(BwEvent ev, int state)
         throws CalFacadeException {
     checkOpen();
 
     sess.namedQuery("setSynchStateUser");
     sess.setInt("state", state);
     sess.setEntity("user", user);
     sess.setInt("eventid", ev.getId());
 
     return sess.executeUpdate();
   }
 
   public void getSynchData(BwSynchState val) throws CalFacadeException {
     checkOpen();
 
     sess.namedQuery("getSynchData");
     sess.setEntity("user", val.getUser());
     sess.setInt("eventid", val.getEvent().getId());
 
     BwSynchData sd = (BwSynchData)sess.getUnique();
 
     val.setSynchData(sd);
   }
 
   public void setSynchData(BwSynchState val) throws CalFacadeException {
     checkOpen();
 
     sess.saveOrUpdate(val);
     setSynchStateUser(val.getEvent(), BwSynchState.CLIENT_MODIFIED_UNDELIVERED);
     sess.saveOrUpdate(val.getSynchData());
   }
 
   public void updateSynchStates() throws CalFacadeException {
     checkOpen();
 
     sess.namedQuery("updateSynchStates1");
     sess.setInt("newstate", BwSynchState.SYNCHRONIZED);
     sess.setInt("userid", user.getId());
     sess.setString("deviceid", synchId);
     sess.setInt("state", BwSynchState.MODIFIED);
 
     sess.namedQuery("updateSynchStates2");
     sess.setInt("newstate", BwSynchState.CLIENT_DELETED);
     sess.setInt("userid", user.getId());
     sess.setString("deviceid", synchId);
     sess.setInt("state", BwSynchState.CLIENT_DELETED_UNDELIVERED);
 
     sess.namedQuery("updateSynchStates2");
     sess.setInt("newstate", BwSynchState.CLIENT_MODIFIED);
     sess.setInt("userid", user.getId());
     sess.setString("deviceid", synchId);
     sess.setInt("state", BwSynchState.CLIENT_MODIFIED_UNDELIVERED);
   }
 
   /* ====================================================================
    *                       Alarms
    * ==================================================================== */
 
   public Collection getAlarms(BwEvent event, BwUser user) throws CalFacadeException {
     checkOpen();
 
     sess.namedQuery("getAlarms");
     sess.setEntity("ev", event);
     sess.setEntity("user", user);
 
     return sess.getList();
   }
 
   public void addAlarm(BwAlarm val) throws CalFacadeException {
     checkOpen();
 
     sess.save(val);
   }
 
   public void updateAlarm(BwAlarm val) throws CalFacadeException {
     checkOpen();
     sess.saveOrUpdate(val);
   }
 
   public Collection getUnexpiredAlarms(BwUser user) throws CalFacadeException {
     checkOpen();
 
     if (user == null) {
       sess.namedQuery("getUnexpiredAlarms");
     } else {
       sess.namedQuery("getUnexpiredAlarmsUser");
       sess.setEntity("user", user);
     }
 
     return sess.getList();
   }
 
   public Collection getUnexpiredAlarms(BwUser user, long triggerTime)
           throws CalFacadeException {
     checkOpen();
 
     sess.namedQuery("getUnexpiredAlarmsUserTime");
     sess.setEntity("user", user);
     sess.setLong("tt", triggerTime);
 
     return sess.getList();
   }
 
   /* ====================================================================
    *                   Arbitrary query
    * ==================================================================== */
 
   /*
   public Collection arbQuery(String q) throws CalFacadeException {
     checkOpen();
     sess.createQuery(q);
 
     return sess.getList();
   }*/
 
   /* ====================================================================
    *                   Private methods
    * ==================================================================== */
 
   private void checkOpen() throws CalFacadeException {
     if (!isOpen) {
       throw new CalFacadeException("Calintf call when closed");
     }
   }
 
   /*
   private void updated(BwUser user) {
     personalModified.add(user);
   }
 
   /** Get a logger for messages
    */
   private Logger getLogger() {
     if (log == null) {
       log = Logger.getLogger(this.getClass());
     }
 
     return log;
   }
 
   /*
   private void error(String msg) {
     getLogger().error(msg);
   }*/
 
   private void trace(String msg) {
     getLogger().debug(msg);
   }
 
   /* Ensure the current user has super user access.
    */
   private void requireSuper() throws CalFacadeException {
     if ((currentMode == CalintfUtil.publicAdminMode) && superUser) {
       return;
     }
 
     throw new CalFacadeAccessException();
   }
 
   /* Ensure the current user is not a guest.
    * /
   private void requireNotGuest() throws CalFacadeException {
     if (!user.isGuest())  {
       return;
     }
 
     throw new CalFacadeAccessException();
   }*/
 }
 
