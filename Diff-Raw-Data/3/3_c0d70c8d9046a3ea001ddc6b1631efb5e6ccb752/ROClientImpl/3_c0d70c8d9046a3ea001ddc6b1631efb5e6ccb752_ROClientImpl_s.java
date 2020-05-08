 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 package org.bedework.appcommon.client;
 
 import org.bedework.access.Ace;
 import org.bedework.access.Acl;
 import org.bedework.appcommon.CollectionCollator;
 import org.bedework.appcommon.EventFormatter;
 import org.bedework.caldav.util.notifications.NotificationType;
 import org.bedework.caldav.util.sharing.InviteReplyType;
 import org.bedework.caldav.util.sharing.ShareResultType;
 import org.bedework.caldav.util.sharing.ShareType;
 import org.bedework.calfacade.BwAuthUser;
 import org.bedework.calfacade.BwCalendar;
 import org.bedework.calfacade.BwCategory;
 import org.bedework.calfacade.BwContact;
 import org.bedework.calfacade.BwDateTime;
 import org.bedework.calfacade.BwDuration;
 import org.bedework.calfacade.BwEvent;
 import org.bedework.calfacade.BwFilterDef;
 import org.bedework.calfacade.BwGroup;
 import org.bedework.calfacade.BwLocation;
 import org.bedework.calfacade.BwOrganizer;
 import org.bedework.calfacade.BwPreferences;
 import org.bedework.calfacade.BwPrincipal;
 import org.bedework.calfacade.BwProperty;
 import org.bedework.calfacade.BwResource;
 import org.bedework.calfacade.BwString;
 import org.bedework.calfacade.BwSystem;
 import org.bedework.calfacade.DirectoryInfo;
 import org.bedework.calfacade.RecurringRetrievalMode;
 import org.bedework.calfacade.ScheduleResult;
 import org.bedework.calfacade.SubContext;
 import org.bedework.calfacade.base.BwShareableDbentity;
 import org.bedework.calfacade.base.CategorisedEntity;
 import org.bedework.calfacade.base.UpdateFromTimeZonesInfo;
 import org.bedework.calfacade.configs.AuthProperties;
 import org.bedework.calfacade.configs.BasicSystemProperties;
 import org.bedework.calfacade.configs.SystemProperties;
 import org.bedework.calfacade.exc.CalFacadeException;
 import org.bedework.calfacade.filter.SortTerm;
 import org.bedework.calfacade.indexing.BwIndexer;
 import org.bedework.calfacade.indexing.BwIndexer.Position;
 import org.bedework.calfacade.indexing.SearchResult;
 import org.bedework.calfacade.indexing.SearchResultEntry;
 import org.bedework.calfacade.locale.BwLocale;
 import org.bedework.calfacade.mail.Message;
 import org.bedework.calfacade.svc.BwAdminGroup;
 import org.bedework.calfacade.svc.BwCalSuite;
 import org.bedework.calfacade.svc.BwView;
 import org.bedework.calfacade.svc.EventInfo;
 import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
 import org.bedework.calfacade.synch.BwSynchInfo;
 import org.bedework.calsvci.CalSvcFactoryDefault;
 import org.bedework.calsvci.CalSvcI;
 import org.bedework.calsvci.CalSvcIPars;
 import org.bedework.calsvci.SchedulingI;
 import org.bedework.calsvci.SharingI;
 import org.bedework.icalendar.IcalTranslator;
 import org.bedework.sysevents.events.HttpEvent;
 import org.bedework.sysevents.events.HttpOutEvent;
 import org.bedework.sysevents.events.SysEventBase;
 import org.bedework.util.misc.Util;
 import org.bedework.util.struts.Request;
 
 import org.apache.log4j.Logger;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
import java.util.TreeSet;
 
 /**
  * User: douglm Date: 6/27/13 Time: 2:03
  */
 public class ROClientImpl implements Client {
   protected boolean debug;
 
   protected String id;
 
   protected CalSvcIPars pars;
 
   protected CalSvcI svci;
 
   protected boolean publicView;
 
   protected boolean superUser;
 
   protected boolean publicAdmin;
 
   protected BwPrincipal currentPrincipal;
   private String currentCalendarAddress;
 
   private Collection<Locale>supportedLocales;
 
   private ClientState cstate;
 
   private transient CollectionCollator<BwCalendar> calendarCollator;
   private String appType;
 
   private BwIndexer publicIndexer;
   private BwIndexer userIndexer;
   private SearchResult lastSearch;
   private List<SearchResultEntry> lastSearchEntries;
 
   protected class AccessChecker implements BwIndexer.AccessChecker {
     @Override
     public Acl.CurrentAccess checkAccess(final BwShareableDbentity ent,
                                          final int desiredAccess,
                                          final boolean returnResult)
             throws CalFacadeException {
       return svci.checkAccess(ent, desiredAccess, returnResult);
     }
   }
 
   protected AccessChecker accessChecker = new AccessChecker();
 
   public ROClientImpl(final String id,
                       final String authUser,
                       final String runAsUser,
                       final String calSuiteName,
                       final boolean publicView)
           throws CalFacadeException {
     this(id);
 
     reinit(authUser, runAsUser, calSuiteName, publicView);
   }
 
   protected ROClientImpl(final String id) {
     this.id = id;
     cstate = new ClientState(this);
   }
 
   public void reinit(final String authUser,
                      final String runAsUser,
                      final String calSuiteName,
                      final boolean publicView)
           throws CalFacadeException {
     currentPrincipal = null;
 
     pars = new CalSvcIPars(authUser,
                            runAsUser,
                            calSuiteName,
                            false, // publicAdmin,
                            false, // Allow non-admin super user
                            false, // service
                            false, // adminCanEditAllPublicCategories,
                            false, // adminCanEditAllPublicLocations,
                            false, // adminCanEditAllPublicSponsors,
                            false);    // sessionless
     pars.setLogId(id);
     svci = new CalSvcFactoryDefault().getSvc(pars);
     this.publicView = publicView;
   }
 
   @Override
   public Client copy(final String id) throws CalFacadeException {
     ROClientImpl cl = new ROClientImpl(id);
 
     cl.pars = (CalSvcIPars)pars.clone();
     cl.pars.setLogId(id);
 
     cl.svci = new CalSvcFactoryDefault().getSvc(cl.pars);
     cl.publicView = publicView;
 
     return cl;
   }
 
   @Override
   public void requestIn(final int conversationType)
           throws CalFacadeException {
     svci.postNotification(new HttpEvent(SysEventBase.SysCode.WEB_IN));
 
     if (conversationType == Request.conversationTypeUnknown) {
       svci.open();
       svci.beginTransaction();
       return;
     }
 
     if (svci.isRolledback()) {
       svci.close();
     }
 
     if (conversationType == Request.conversationTypeOnly) {
               /* if a conversation is already started on entry, end it
                   with no processing of changes. */
       if (svci.isOpen()) {
         svci.endTransaction();
       }
     }
 
     if (conversationType == Request.conversationTypeProcessAndOnly) {
       if (svci.isOpen()) {
         svci.flushAll();
         svci.endTransaction();
         svci.close();
       }
     }
 
     svci.open();
     svci.beginTransaction();
   }
 
   @Override
   public void requestOut(final int conversationType,
                          final int actionType,
                          final long reqTimeMillis)
           throws CalFacadeException {
     svci.postNotification(new HttpOutEvent(SysEventBase.SysCode.WEB_OUT,
                                            reqTimeMillis));
 
     if (!isOpen()) {
       return;
     }
 
     if (conversationType == Request.conversationTypeUnknown) {
       if (actionType != Request.actionTypeAction) {
         flushAll();
       }
     } else {
       if ((conversationType == Request.conversationTypeEnd) ||
               (conversationType == Request.conversationTypeOnly)) {
         flushAll();
       }
     }
 
     svci.endTransaction();
   }
 
   @Override
   public boolean isOpen() {
     return svci.isOpen();
   }
 
   @Override
   public void close() throws CalFacadeException {
     svci.close();
   }
 
   @Override
   public void flushAll() throws CalFacadeException {
     svci.flushAll();
   }
 
   @Override
   public void endTransaction() throws CalFacadeException {
     svci.endTransaction();
   }
 
   @Override
   public boolean getPublicAdmin() {
     return publicAdmin;
   }
 
   @Override
   public void setAppType(final String val) {
     appType = val;
   }
 
   @Override
   public String getAppType() {
     return appType;
   }
 
   @Override
   public BwSystem getSyspars() throws CalFacadeException {
     return svci.getSysparsHandler().get();
   }
 
   @Override
   public void updateSyspars(final BwSystem val)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public AuthProperties getAuthProperties()
           throws CalFacadeException {
     return svci.getAuthProperties();
   }
 
   @Override
   public SystemProperties getSystemProperties()
           throws CalFacadeException {
     return svci.getSystemProperties();
   }
 
   @Override
   public void rollback() {
     try {
       svci.rollbackTransaction();
     } catch (Throwable t) {}
 
     try {
       svci.endTransaction();
     } catch (Throwable t) {}
   }
 
   @Override
   public long getUserMaxEntitySize() throws CalFacadeException {
     return svci.getUserMaxEntitySize();
   }
 
   @Override
   public boolean isPrincipal(final String val)
           throws CalFacadeException {
     return svci.getDirectories().isPrincipal(val);
   }
 
   /* ------------------------------------------------------------
    *                     Directories
    * ------------------------------------------------------------ */
 
   @Override
   public DirectoryInfo getDirectoryInfo() throws CalFacadeException {
     return svci.getDirectories().getDirectoryInfo();
   }
 
   @Override
   public String getCalendarAddress(final String user)
           throws CalFacadeException {
     BwPrincipal u = svci.getUsersHandler().getUser(user);
     if (u == null) {
       return null;
     }
 
     return svci.getDirectories().principalToCaladdr(u);
   }
 
   @Override
   public String uriToCaladdr(final String val)
           throws CalFacadeException {
     return svci.getDirectories().uriToCaladdr(val);
   }
 
   @Override
   public boolean getUserMaintOK() throws CalFacadeException {
     return svci.getUserAuth().getUserMaintOK();
   }
 
   @Override
   public BwPrincipal calAddrToPrincipal(final String cua)
           throws CalFacadeException {
     return svci.getDirectories().caladdrToPrincipal(cua);
   }
 
   /* ------------------------------------------------------------
    *                     Principals
    * ------------------------------------------------------------ */
 
   @Override
   public boolean isSuperUser() {
     return superUser;
   }
 
   @Override
   public boolean isGuest() {
     return true;
   }
 
   @Override
   public BwPrincipal getCurrentPrincipal() throws CalFacadeException {
     if (currentPrincipal == null) {
       currentPrincipal = (BwPrincipal)svci.getPrincipal().clone();
     }
 
     return currentPrincipal;
   }
 
   @Override
   public BwPrincipal getOwner() throws CalFacadeException {
     return getCurrentPrincipal();
   }
 
   @Override
   public String getCurrentPrincipalHref() throws CalFacadeException {
     return getCurrentPrincipal().getPrincipalRef();
   }
 
   public String getCurrentCalendarAddress() throws CalFacadeException {
     if (currentCalendarAddress == null) {
       currentCalendarAddress = svci.getDirectories().principalToCaladdr(getCurrentPrincipal());
     }
 
     return currentCalendarAddress;
   }
 
   @Override
   public BwPrincipal getUser(final String val)
           throws CalFacadeException {
     return svci.getUsersHandler().getUser(val);
   }
 
   @Override
   public BwPrincipal getUserAlways(final String val)
           throws CalFacadeException {
     return svci.getUsersHandler().getAlways(val);
   }
 
   @Override
   public String makePrincipalUri(final String id,
                                  final int whoType)
           throws CalFacadeException {
     return svci.getDirectories().makePrincipalUri(id, whoType);
   }
 
   @Override
   public BwPrincipal getPrincipal(final String href)
           throws CalFacadeException {
     return svci.getDirectories().getPrincipal(href);
   }
 
   @Override
   public boolean validPrincipal(final String href)
           throws CalFacadeException {
     return svci.getDirectories().validPrincipal(href);
   }
 
   /* ------------------------------------------------------------
    *                     Admin users
    * ------------------------------------------------------------ */
 
   @Override
   public void addUser(final String account)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public BwAuthUser getAuthUser(final String userid)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public void updateAuthUser(final BwAuthUser val)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public Collection<BwAuthUser> getAllAuthUsers()
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   /* ------------------------------------------------------------
    *                     Admin Groups
    * ------------------------------------------------------------ */
 
   @Override
   public String getAdminGroupsIdPrefix()
           throws CalFacadeException {
     return svci.getAdminDirectories().getAdminGroupsIdPrefix();
   }
 
   @Override
   public Collection<BwGroup> getAdminGroups(final boolean getMembers)
           throws CalFacadeException {
     return svci.getAdminDirectories().getAll(getMembers);
   }
 
   @Override
   public Collection<BwGroup> getAllAdminGroups(final BwPrincipal val)
           throws CalFacadeException {
     return svci.getAdminDirectories().getAllGroups(val);
   }
 
   @Override
   public void addAdminGroup(final BwAdminGroup group)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public void removeAdminGroup(final BwAdminGroup group)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public void updateAdminGroup(final BwAdminGroup group)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public boolean getAdminGroupMaintOK() throws CalFacadeException {
     return svci.getAdminDirectories().getGroupMaintOK();
   }
 
   @Override
   public BwAdminGroup findAdminGroup(final String name)
           throws CalFacadeException {
     return (BwAdminGroup)svci.getAdminDirectories().findGroup(name);
   }
 
   @Override
   public void getAdminGroupMembers(final BwAdminGroup group)
           throws CalFacadeException {
     svci.getAdminDirectories().getMembers(group);
   }
 
   @Override
   public void addAdminGroupMember(final BwAdminGroup group,
                                   final BwPrincipal val)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public void removeAdminGroupMember(final BwAdminGroup group,
                                      final BwPrincipal val)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   /* ------------------------------------------------------------
    *                     Groups
    * ------------------------------------------------------------ */
 
   @Override
   public BwGroup findGroup(final String name)
           throws CalFacadeException {
     return svci.getDirectories().findGroup(name);
   }
 
   @Override
   public Collection<BwGroup> findGroupParents(final BwGroup group)
           throws CalFacadeException {
     return svci.getDirectories().findGroupParents(group);
   }
 
   @Override
   public Collection<BwGroup> getGroups(final BwPrincipal val)
           throws CalFacadeException {
     return svci.getDirectories().getGroups(val);
   }
 
   @Override
   public Collection<BwGroup> getAllGroups(final boolean populate)
           throws CalFacadeException {
     return svci.getDirectories().getAll(populate);
   }
 
   @Override
   public void getMembers(final BwGroup group)
           throws CalFacadeException {
     svci.getDirectories().getMembers(group);
   }
 
   /* ------------------------------------------------------------
    *                     Preferences
    * ------------------------------------------------------------ */
 
   @Override
   public BwPreferences getPreferences() throws CalFacadeException {
     return svci.getPrefsHandler().get();
   }
 
   @Override
   public BwPreferences getPreferences(final String user)
           throws CalFacadeException {
     return null;
   }
 
   @Override
   public void updatePreferences(final BwPreferences val)
           throws CalFacadeException {
     svci.getPrefsHandler().update(val);
   }
 
   @Override
   public String getPreferredCollectionPath(final String compName)
           throws CalFacadeException {
     return svci.getCalendarsHandler().getPreferred(compName);
   }
 
   /** Set false to inhibit lastLocale stuff */
   public static boolean tryLastLocale = true;
 
   /* (non-Javadoc)
    * @see org.bedework.calsvci.PreferencesI#getUserLocale(java.util.Collection, java.util.Locale)
    */
   @Override
   public Locale getUserLocale(final Collection<Locale> locales,
                               final Locale locale) throws CalFacadeException {
     Collection<Locale> sysLocales = getSupportedLocales();
 
     if (locale != null) {
       /* See if it's acceptable */
       Locale l = BwLocale.matchLocales(sysLocales, locale);
       if (l != null) {
         if (debug) {
           debugMsg("Setting locale to " + l);
         }
         return l;
       }
     }
 
     /* See if the user expressed a preference */
     Collection<BwProperty> properties = getPreferences().getProperties();
     String preferredLocaleStr = null;
     String lastLocaleStr = null;
 
     if (properties != null) {
       for (BwProperty prop: properties) {
         if (preferredLocaleStr == null) {
           if (prop.getName().equals(BwPreferences.propertyPreferredLocale)) {
             preferredLocaleStr = prop.getValue();
             if (!tryLastLocale) {
               break;
             }
           }
         }
 
         if (tryLastLocale) {
           if (lastLocaleStr == null) {
             if (prop.getName().equals(BwPreferences.propertyLastLocale)) {
               lastLocaleStr = prop.getValue();
             }
           }
         }
 
         if ((preferredLocaleStr != null) &&
                 (lastLocaleStr != null)) {
           break;
         }
       }
     }
 
     if (preferredLocaleStr != null) {
       Locale l = BwLocale.matchLocales(sysLocales,
                                        BwLocale.makeLocale(preferredLocaleStr));
       if (l != null) {
         if (debug) {
           debugMsg("Setting locale to " + l);
         }
         return l;
       }
     }
 
     if (lastLocaleStr != null) {
       Locale l = BwLocale.matchLocales(sysLocales,
                                        BwLocale.makeLocale(lastLocaleStr));
       if (l != null) {
         if (debug) {
           debugMsg("Setting locale to " + l);
         }
         return l;
       }
     }
 
     /* See if the supplied list has a match in the supported locales */
 
     if (locales != null) {
       // We had an ACCEPT-LANGUAGE header
 
       for (Locale l: locales) {
         l = BwLocale.matchLocales(sysLocales, l);
         if (l != null) {
           if (debug) {
             debugMsg("Setting locale to " + l);
           }
           return l;
         }
       }
     }
 
     /* Use the first from supported locales -
      * there's always at least one in the collection */
     Locale l = sysLocales.iterator().next();
 
     if (debug) {
       debugMsg("Setting locale to " + l);
     }
     return l;
   }
 
   /* ------------------------------------------------------------
    *                     Collections
    * ------------------------------------------------------------ */
 
   @Override
   public BwCalendar getHome() throws CalFacadeException {
     return svci.getCalendarsHandler().getHome();
   }
 
   @Override
   public BwCalendar getCollection(final String path)
           throws CalFacadeException {
     return svci.getCalendarsHandler().get(path);
   }
 
   @Override
   public boolean collectionExists(final String path)
           throws CalFacadeException {
     return getCollection(path) != null;
   }
 
   @Override
   public BwCalendar getSpecial(final int calType,
                                final boolean create)
           throws CalFacadeException {
     return svci.getCalendarsHandler().getSpecial(calType, create);
   }
 
   @Override
   public BwCalendar addCollection(final BwCalendar val,
                                   final String parentPath)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public void updateCollection(final BwCalendar col)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public boolean deleteCollection(final BwCalendar val,
                                   final boolean emptyIt)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public BwCalendar resolveAlias(final BwCalendar val,
                                  final boolean resolveSubAlias,
                                  final boolean freeBusy)
           throws CalFacadeException {
     return svci.getCalendarsHandler().resolveAlias(val,
                                                    resolveSubAlias,
                                                    freeBusy);
   }
 
   @Override
   public Collection<BwCalendar> getChildren(final BwCalendar col)
           throws CalFacadeException {
     return svci.getCalendarsHandler().getChildren(col);
   }
 
   @Override
   public void moveCollection(final BwCalendar val,
                              final BwCalendar newParent)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public Collection<BwCalendar> decomposeVirtualPath(final String vpath)
           throws CalFacadeException {
     return svci.getCalendarsHandler().decomposeVirtualPath(vpath);
   }
 
   @Override
   public Collection<BwCalendar> getAddContentCollections(final boolean includeAliases)
           throws CalFacadeException {
     return getCalendarCollator().getCollatedCollection(
             svci.getCalendarsHandler().getAddContentCollections(includeAliases));
   }
 
   @Override
   public BwCalendar getPublicCalendars() throws CalFacadeException {
     return svci.getCalendarsHandler().getPublicCalendars();
   }
 
   @Override
   public BwCalendar getHome(final BwPrincipal principal,
                             final boolean freeBusy)
           throws CalFacadeException {
     return svci.getCalendarsHandler().getHome(principal, freeBusy);
   }
 
   /* ------------------------------------------------------------
    *                     Categories
    * ------------------------------------------------------------ */
 
   @Override
   public BwCategory getCategoryByName(final BwString name)
           throws CalFacadeException {
     return svci.getCategoriesHandler().find(name);
   }
 
   @Override
   public BwCategory getCategory(final String uid)
           throws CalFacadeException {
     return svci.getCategoriesHandler().get(uid);
   }
 
   @Override
   public BwCategory getPersistentCategory(final String uid)
           throws CalFacadeException {
     return svci.getCategoriesHandler().getPersistent(uid);
   }
 
   @Override
   public Collection<BwCategory> getCategories()
           throws CalFacadeException {
     return svci.getCategoriesHandler().get();
   }
 
   @Override
   public Collection<BwCategory> getCategories(final Collection<String> uids)
           throws CalFacadeException {
     return svci.getCategoriesHandler().get(uids);
   }
 
   @Override
   public Collection<BwCategory> getPublicCategories()
           throws CalFacadeException {
     return svci.getCategoriesHandler().getPublic();
   }
 
   @Override
   public Collection<BwCategory> getEditableCategories()
           throws CalFacadeException {
     return svci.getCategoriesHandler().getEditable();
   }
 
   @Override
   public boolean addCategory(final BwCategory val)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public void updateCategory(final BwCategory val)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public DeleteReffedEntityResult deleteCategory(final BwCategory val)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   /* ------------------------------------------------------------
    *                     Contacts
    * ------------------------------------------------------------ */
 
   @Override
   public BwContact getContact(final String uid)
           throws CalFacadeException {
     return svci.getContactsHandler().get(uid);
   }
 
   @Override
   public BwContact getPersistentContact(final String uid)
           throws CalFacadeException {
     return svci.getContactsHandler().getPersistent(uid);
   }
 
   @Override
   public Collection<BwContact> getContacts()
           throws CalFacadeException {
     return svci.getContactsHandler().get();
   }
 
   @Override
   public Collection<BwContact> getPublicContacts()
           throws CalFacadeException {
     return svci.getContactsHandler().getPublic();
   }
 
   @Override
   public Collection<BwContact> getEditableContacts()
           throws CalFacadeException {
     return svci.getContactsHandler().getEditable();
   }
 
   @Override
   public BwContact findContact(final BwString val)
           throws CalFacadeException {
     return svci.getContactsHandler().findPersistent(val);
   }
 
   @Override
   public void addContact(final BwContact val)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public void updateContact(final BwContact val)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public DeleteReffedEntityResult deleteContact(final BwContact val)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public CheckEntityResult<BwContact> ensureContactExists(final BwContact val,
                                                           final String ownerHref)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   /* ------------------------------------------------------------
    *                     Locations
    * ------------------------------------------------------------ */
 
   @Override
   public BwLocation getLocation(final String uid)
           throws CalFacadeException {
     return svci.getLocationsHandler().get(uid);
   }
 
   @Override
   public BwLocation getPersistentLocation(final String uid)
           throws CalFacadeException {
     return svci.getLocationsHandler().getPersistent(uid);
   }
 
   @Override
   public Collection<BwLocation> getLocations()
           throws CalFacadeException {
     return svci.getLocationsHandler().get();
   }
 
   @Override
   public Collection<BwLocation> getPublicLocations()
           throws CalFacadeException {
     return svci.getLocationsHandler().getPublic();
   }
 
   @Override
   public Collection<BwLocation> getEditableLocations()
           throws CalFacadeException {
     return svci.getLocationsHandler().getEditable();
   }
 
   @Override
   public BwLocation findLocation(final BwString address)
           throws CalFacadeException {
     return svci.getLocationsHandler().findPersistent(address);
   }
 
   @Override
   public boolean addLocation(final BwLocation val)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public void updateLocation(final BwLocation val)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public DeleteReffedEntityResult deleteLocation(final BwLocation val)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public CheckEntityResult<BwLocation> ensureLocationExists(final BwLocation val,
                                                             final String ownerHref)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   /* ------------------------------------------------------------
    *                     Events
    * ------------------------------------------------------------ */
 
   @Override
   public void claimEvent(final BwEvent ev) throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public Collection<EventInfo> getEvent(final String path,
                                         final String guid,
                                         final String rid,
                                         final RecurringRetrievalMode recurRetrieval,
                                         final boolean scheduling)
           throws CalFacadeException {
     return svci.getEventsHandler().get(path, guid, rid,
                                        recurRetrieval, scheduling);
   }
 
   @Override
   public SearchParams getSearchParams() {
     return cstate.getSearchParams();
   }
 
   @Override
   public EventInfo getEvent(final String colPath,
                             final String name,
                             final RecurringRetrievalMode recurRetrieval)
           throws CalFacadeException {
     return svci.getEventsHandler().get(colPath, name, recurRetrieval);
   }
 
   @Override
   public Collection<EventInfo> getEvents(final String filter,
                                          final BwDateTime startDate,
                                          final BwDateTime endDate,
                                          final boolean expand)
           throws CalFacadeException {
     if (filter == null) {
       return null;
     }
 
     BwFilterDef fd = new BwFilterDef();
     fd.setDefinition(filter);
 
     parseFilter(fd);
 
     RecurringRetrievalMode rrm;
     if (expand) {
       rrm = RecurringRetrievalMode.expanded;
     } else {
       rrm = RecurringRetrievalMode.overrides;
     }
 
     return svci.getEventsHandler().getEvents(null,
                                              fd.getFilters(),
                                              startDate,
                                              endDate,
                                              null,
                                              rrm);
   }
 
   @Override
   public EventInfo.UpdateResult addEvent(final EventInfo ei,
                                          final boolean noInvites,
                                          final boolean scheduling,
                                          final boolean rollbackOnError)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public EventInfo.UpdateResult updateEvent(final EventInfo ei,
                                             final boolean noInvites,
                                             final String fromAttUri)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public boolean deleteEvent(final EventInfo ei,
                              final boolean sendSchedulingMessage)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public void markDeleted(final BwEvent event)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   /* ------------------------------------------------------------
    *                     Notifications
    * ------------------------------------------------------------ */
 
   @Override
   public NotificationType findNotification(final String name)
           throws CalFacadeException {
     return svci.getNotificationsHandler().find(name);
   }
 
   @Override
   public void removeNotification(final NotificationType val)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   /* ------------------------------------------------------------
    *                     Resources
    * ------------------------------------------------------------ */
 
   @Override
   public void saveResource(final String path,
                            final BwResource val) throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public BwResource getResource(final String path) throws CalFacadeException {
     return svci.getResourcesHandler().get(path);
   }
 
   @Override
   public void getResourceContent(final BwResource val)
           throws CalFacadeException {
     svci.getResourcesHandler().getContent(val);
   }
 
   @Override
   public List<BwResource> getAllResources(final String path)
           throws CalFacadeException {
     return svci.getResourcesHandler().getAll(path);
   }
 
   @Override
   public void updateResource(final BwResource val,
                              final boolean updateContent)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public BwEvent getFreeBusy(final Collection<BwCalendar> fbset,
                              final BwPrincipal who,
                              final BwDateTime start,
                              final BwDateTime end,
                              final BwOrganizer org,
                              final String uid,
                              final String exceptUid)
           throws CalFacadeException {
     return svci.getScheduler().getFreeBusy(fbset, who, start, end,
                                            org, uid, exceptUid);
   }
 
   /* ------------------------------------------------------------
    *                     Scheduling
    * ------------------------------------------------------------ */
 
   @Override
   public SchedulingI.FbResponses aggregateFreeBusy(final ScheduleResult sr,
                                                    final BwDateTime start,
                                                    final BwDateTime end,
                                                    final BwDuration granularity)
           throws CalFacadeException {
     return svci.getScheduler().aggregateFreeBusy(sr, start, end, granularity);
   }
 
   @Override
   public ScheduleResult schedule(final EventInfo ei,
                                  final int method,
                                  final String recipient,
                                  final String fromAttUri,
                                  final boolean iSchedule)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public EventInfo getStoredMeeting(final BwEvent ev)
           throws CalFacadeException {
     return svci.getScheduler().getStoredMeeting(ev);
   }
 
   @Override
   public ScheduleResult requestRefresh(final EventInfo ei,
                                        final String comment)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   /* ------------------------------------------------------------
    *                     Sharing
    * ------------------------------------------------------------ */
 
   @Override
   public ShareResultType share(final String principalHref,
                                final BwCalendar col,
                                final ShareType share)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public void publish(final BwCalendar col)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public void unpublish(final BwCalendar col)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public SharingI.ReplyResult sharingReply(final InviteReplyType reply)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public SharingI.SubscribeResult subscribe(final String colPath,
                                             final String subscribedName)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public SharingI.SubscribeResult subscribeExternal(final String extUrl,
                                                     final String subscribedName,
                                                     final int refresh,
                                                     final String remoteId,
                                                     final String remotePw)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   /* ------------------------------------------------------------
    *                     Views
    * ------------------------------------------------------------ */
 
   @Override
   public BwSynchInfo getSynchInfo() throws CalFacadeException {
     return svci.getSynch().getSynchInfo();
   }
 
   /* ------------------------------------------------------------
    *                     Views
    * ------------------------------------------------------------ */
 
   @Override
   public BwView getView(final String val) throws CalFacadeException {
     return svci.getViewsHandler().find(val);
   }
 
   @Override
   public boolean addView(final BwView val,
                          final boolean makeDefault)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public Collection<BwView> getAllViews() throws CalFacadeException {
     return svci.getViewsHandler().getAll();
   }
 
   @Override
   public boolean addViewCollection(final String name,
                                    final String path)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public boolean removeViewCollection(final String name,
                                       final String path)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public boolean removeView(final BwView val)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   /* ------------------------------------------------------------
    *                     State of client
    * ------------------------------------------------------------ */
 
   @Override
   public void flushState() throws CalFacadeException {
   }
 
   /* ------------------------------------------------------------
    *                     State of current admin group
    * ------------------------------------------------------------ */
 
   @Override
   public void setGroupSet(final boolean val) throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public boolean getGroupSet() {
     return false;  //To change body of implemented methods use File | Settings | File Templates.
   }
 
   @Override
   public void setChoosingGroup(final boolean val) throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public boolean getChoosingGroup() {
     return false;  //To change body of implemented methods use File | Settings | File Templates.
   }
 
   @Override
   public void setOneGroup(final boolean val) throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public boolean getOneGroup() {
     return false;
   }
 
   @Override
   public void setAdminGroupName(final String val) throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public String getAdminGroupName() {
     return null;
   }
 
   /* ------------------------------------------------------------
    *                     Misc
    * ------------------------------------------------------------ */
 
   @Override
   public void moveContents(final BwCalendar cal,
                            final BwCalendar newCal)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public UpdateFromTimeZonesInfo updateFromTimeZones(final int limit,
                                                      final boolean checkOnly,
                                                      final UpdateFromTimeZonesInfo info)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public void postMessage(final Message val)
           throws CalFacadeException {
     svci.getMailer().post(val);
   }
 
   @Override
   public SearchResult search(final SearchParams params) throws CalFacadeException {
     cstate.setSearchParams(params);
 
     lastSearchEntries = null;
 
     String start = null;
     String end = null;
 
     if (params.getFromDate() != null) {
       start = params.getFromDate().getDtval();
     }
 
     if (params.getToDate() != null) {
       end = params.getToDate().getDtval();
     }
 
     lastSearch = getIndexer(params.getPublick()).search(
             params.getQuery(),
             params.getFilter(),
             params.getSort(),
             start,
             end,
             params.getPageSize(),
             accessChecker,
             RecurringRetrievalMode.expanded);
 
     return lastSearch;
   }
 
   @Override
   public List<SearchResultEntry> getSearchResult(Position pos) throws CalFacadeException {
     if (lastSearch == null) {
       return null;
     }
 
     if ((pos == Position.current) && (lastSearchEntries != null)) {
       return lastSearchEntries;
     }
 
     lastSearchEntries = formatSearchResult(lastSearch.getIndexer().
             getSearchResult(lastSearch, pos, PrivilegeDefs.privAny));
 
     return lastSearchEntries;
   }
 
   @Override
   public List<SearchResultEntry> getSearchResult(final int start,
                                                  final int num) throws CalFacadeException {
     if (lastSearch == null) {
       return new ArrayList<>(0);
     }
 
     return formatSearchResult(lastSearch.getIndexer().
             getSearchResult(lastSearch, start, num, PrivilegeDefs.privAny));
   }
 
   private List<SearchResultEntry> formatSearchResult(
           List<SearchResultEntry> entries) throws CalFacadeException {
     IcalTranslator trans = new IcalTranslator(new IcalCallbackcb(this));
 
     for (SearchResultEntry sre: entries) {
       Object o = sre.getEntity();
 
       if (o instanceof CategorisedEntity) {
         restoreCategories((CategorisedEntity)o);
       }
 
       if (!(o instanceof EventInfo)) {
         continue;
       }
 
       BwEvent ev = ((EventInfo)o).getEvent();
 
       if (ev.getLocationUid() != null){
         ev.setLocation(getLocation(ev.getLocationUid()));
       }
 
       EventFormatter ef = new EventFormatter(this,
                                              trans,
                                              (EventInfo)o);
       sre.setEntity(ef);
     }
 
     return entries;
   }
 
   private void restoreCategories(final CategorisedEntity ce) throws CalFacadeException {
     Set<String> uids = ce.getCategoryUids();
     if (Util.isEmpty(uids)) {
       return;
     }
 
 //    Set<String> catUids = new TreeSet<>();
 
     for (Object o: uids) {
       String uid = (String)o;
 //      catUids.add(uid);
 
       ce.addCategory(getCategory(uid));
     }
   }
 
   @Override
   public void changeAccess(final BwShareableDbentity ent,
                            final Collection<Ace> aces,
                            final boolean replaceAll)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   /* ------------------------------------------------------------
    *                   Calendar Suites
    * ------------------------------------------------------------ */
 
 
   @Override
   public void setCalSuite(final String name)
           throws CalFacadeException {
     svci.setCalSuite(name);
   }
 
   @Override
   public BwCalSuiteWrapper getCalSuite() throws CalFacadeException {
     return svci.getCalSuitesHandler().get();
   }
 
   @Override
   public BwCalSuiteWrapper getCalSuite(final BwAdminGroup group)
           throws CalFacadeException {
     return svci.getCalSuitesHandler().get(group);
   }
 
   @Override
   public Collection<BwCalSuite> getContextCalSuites()
           throws CalFacadeException {
     Map<String, SubContext> suiteToContextMap = new HashMap<>();
 
     for (SubContext subContext : getSyspars().getContexts()) {
       suiteToContextMap.put(subContext.getCalSuite(), subContext);
     }
 
     Collection<BwCalSuite> suites = svci.getCalSuitesHandler().getAll();
     for (BwCalSuite cs : suites) {
       SubContext subContext = suiteToContextMap.get(cs.getName());
 
       if (subContext != null) {
         cs.setContext(subContext.getContextName());
         cs.setDefaultContext(subContext.getDefaultContext());
       } else {
         cs.setContext(null);
         cs.setDefaultContext(false);
       }
     }
     return suites;
   }
 
   @Override
   public BwCalSuiteWrapper getCalSuite(final String name)
           throws CalFacadeException {
     return svci.getCalSuitesHandler().get(name);
   }
 
   @Override
   public BwCalSuiteWrapper addCalSuite(final String name,
                                        final String adminGroupName,
                                        final String rootCollectionPath,
                                        final String submissionsPath)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public void updateCalSuite(final BwCalSuiteWrapper cs,
                              final String adminGroupName,
                              final String rootCollectionPath,
                              final String submissionsPath)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public void deleteCalSuite(final BwCalSuiteWrapper val)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   /* ------------------------------------------------------------
    *                   Calendar Suite Resources
    * ------------------------------------------------------------ */
 
   @Override
   public List<BwResource> getCSResources(final BwCalSuite suite,
                                          final String rc)
           throws CalFacadeException {
     return svci.getResourcesHandler().getAll(getCSResourcesPath(suite,
                                                                 rc));
   }
 
   @Override
   public BwResource getCSResource(final BwCalSuite suite,
                                   final String name,
                                   final String rc)
           throws CalFacadeException {
     try {
       BwResource r = svci.getResourcesHandler().
               get(Util.buildPath(false,
                                  getCSResourcesPath(suite, rc),
                                  "/",
                                  name));
       if (r != null) {
         svci.getResourcesHandler().getContent(r);
       }
 
       return r;
     } catch (CalFacadeException cfe) {
       if (CalFacadeException.collectionNotFound.equals(cfe.getMessage())) {
         // Collection does not exist (yet)
 
         return null;
       }
 
       throw cfe;
     }
   }
 
   @Override
   public void addCSResource(final BwCalSuite suite,
                             final BwResource res,
                             final String rc)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public void deleteCSResource(final BwCalSuite suite,
                                final String name,
                                final String rc)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   /* ------------------------------------------------------------
    *                   Filters
    * ------------------------------------------------------------ */
 
   @Override
   public BwFilterDef getFilter(final String name)
           throws CalFacadeException {
     return svci.getFiltersHandler().get(name);
   }
 
   @Override
   public void parseFilter(final BwFilterDef val)
           throws CalFacadeException {
     svci.getFiltersHandler().parse(val);
   }
 
   @Override
   public List<SortTerm> parseSort(final String val)
           throws CalFacadeException {
     return svci.getFiltersHandler().parseSort(val);
   }
 
   @Override
   public Collection<BwFilterDef> getAllFilters()
           throws CalFacadeException {
     return svci.getFiltersHandler().getAll();
   }
 
   @Override
   public void validateFilter(final String val)
           throws CalFacadeException {
     svci.getFiltersHandler().validate(val);
   }
 
   @Override
   public void saveFilter(final BwFilterDef val)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   @Override
   public void deleteFilter(final String name)
           throws CalFacadeException {
     throw new CalFacadeException("org.bedework.read.only.client");
   }
 
   /* ------------------------------------------------------------
    *                   protected methods
    * ------------------------------------------------------------ */
 
   /**
    * @param msg
    */
   protected void debugMsg(final String msg) {
     Logger.getLogger(this.getClass()).debug(msg);
   }
 
   protected String getCSResourcesPath(final BwCalSuite suite,
                                       final String rc) throws CalFacadeException {
     if (rc == "global") {
       return getBasicSyspars().getGlobalResourcesPath();
     }
 
     BwPrincipal eventsOwner = getPrincipal(suite.getGroup().getOwnerHref());
 
     String home = svci.getPrincipalInfo().getCalendarHomePath(eventsOwner);
 
     BwPreferences prefs = getPreferences(eventsOwner.getPrincipalRef());
 
     String col = null;
 
     if (rc == "admin") {
       col = prefs.getAdminResourcesDirectory();
 
       if (col == null) {
         col = ".adminResources";
       }
     } else if (rc == "calsuite") {
       col = prefs.getSuiteResourcesDirectory();
 
       if (col == null) {
         col = ".csResources";
       }
     }
 
     if (col != null) {
       return Util.buildPath(false, home, "/", col);
     }
 
     throw new RuntimeException("System error");
   }
 
   protected BasicSystemProperties getBasicSyspars() throws CalFacadeException {
     return svci.getBasicSystemProperties();
   }
 
   protected CollectionCollator<BwCalendar> getCalendarCollator() {
     if (calendarCollator == null) {
       calendarCollator = new CollectionCollator<BwCalendar>();
     }
 
     return calendarCollator;
   }
 
   protected BwIndexer getIndexer(boolean publick) throws CalFacadeException {
     if (publick) {
       if (publicIndexer == null) {
         publicIndexer = svci.getIndexer(true);
       }
 
       return publicIndexer;
     }
 
     if (userIndexer == null) {
       userIndexer = svci.getIndexer(false);
     }
 
     return userIndexer;
   }
 
   /* ------------------------------------------------------------
    *                   private methods
    * ------------------------------------------------------------ */
 
   private Collection<Locale> getSupportedLocales() throws CalFacadeException {
     if (supportedLocales != null) {
       return supportedLocales;
     }
 
     supportedLocales = new ArrayList<Locale>();
 
     String ll = getSystemProperties().getLocaleList();
 
     if (ll == null) {
       supportedLocales.add(BwLocale.getLocale());
       return supportedLocales;
     }
 
     try {
       int pos = 0;
 
       while (pos < ll.length()) {
         int nextPos = ll.indexOf(",", pos);
         if (nextPos < 0) {
           supportedLocales.add(BwLocale.makeLocale(ll.substring(pos)));
           break;
         }
 
         supportedLocales.add(BwLocale.makeLocale(ll.substring(pos, nextPos)));
         pos = nextPos + 1;
       }
     } catch (CalFacadeException cfe) {
       throw cfe;
     } catch (Throwable t) {
       throw new CalFacadeException(CalFacadeException.badSystemLocaleList,
                                    ll);
     }
 
     if (supportedLocales.isEmpty()) {
       supportedLocales.add(BwLocale.getLocale());
     }
 
     return supportedLocales;
   }
 }
