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
 
 import org.bedework.caldav.util.notifications.NotificationType;
 import org.bedework.caldav.util.sharing.InviteReplyType;
 import org.bedework.caldav.util.sharing.ShareResultType;
 import org.bedework.caldav.util.sharing.ShareType;
 import org.bedework.calfacade.BwCalendar;
 import org.bedework.calfacade.BwCategory;
 import org.bedework.calfacade.BwContact;
 import org.bedework.calfacade.BwEvent;
 import org.bedework.calfacade.BwFilterDef;
 import org.bedework.calfacade.BwLocation;
 import org.bedework.calfacade.BwPrincipal;
 import org.bedework.calfacade.BwResource;
 import org.bedework.calfacade.EventPropertiesReference;
 import org.bedework.calfacade.RecurringRetrievalMode;
 import org.bedework.calfacade.ScheduleResult;
 import org.bedework.calfacade.base.BwShareableDbentity;
 import org.bedework.calfacade.exc.CalFacadeException;
 import org.bedework.calfacade.svc.BwView;
 import org.bedework.calfacade.svc.EventInfo;
 import org.bedework.calsvci.CalSvcFactoryDefault;
 import org.bedework.calsvci.CalSvcIPars;
 import org.bedework.calsvci.Categories;
 import org.bedework.calsvci.EventProperties;
 import org.bedework.calsvci.SharingI;
 
 import org.bedework.access.Ace;
 
 import java.util.Collection;
 
 /**
  * User: douglm Date: 6/27/13 Time: 2:05 PM
  */
 public class ClientImpl extends ROClientImpl {
   public ClientImpl(final String id,
                     final String authUser,
                     final String runAsUser)
           throws CalFacadeException {
     this(id);
 
     reinit(authUser, runAsUser);
   }
 
   protected ClientImpl(final String id) {
     super(id);
   }
 
   public void reinit(final String authUser,
                      final String runAsUser)
           throws CalFacadeException {
     currentPrincipal = null;
 
     pars = new CalSvcIPars(authUser,
                            runAsUser,
                            null,  // calSuiteName,
                            false, // publicAdmin,
                            false, // Allow non-admin super user
                            false, // service
                            false, // adminCanEditAllPublicCategories,
                            false, // adminCanEditAllPublicLocations,
                            false, // adminCanEditAllPublicSponsors,
                            false);    // sessionless
     pars.setLogId(id);
 
     svci = new CalSvcFactoryDefault().getSvc(pars);
   }
 
   @Override
   public Client copy(final String id) throws CalFacadeException {
     ClientImpl cl = new ClientImpl(id);
 
     cl.pars = (CalSvcIPars)pars.clone();
     cl.pars.setLogId(id);
 
     cl.svci = new CalSvcFactoryDefault().getSvc(cl.pars);
 
     return cl;
   }
 
   /* ------------------------------------------------------------
    *                     Principals
    * ------------------------------------------------------------ */
 
   @Override
   public boolean isGuest() {
     return false;
   }
 
   @Override
   public BwPrincipal getOwner() throws CalFacadeException {
     if (publicAdmin) {
       return svci.getUsersHandler().getPublicUser();
     }
 
     return getCurrentPrincipal();
   }
 
   /* ------------------------------------------------------------
    *                     Collections
    * ------------------------------------------------------------ */
 
   @Override
   public BwCalendar addCollection(final BwCalendar val,
                                   final String parentPath)
           throws CalFacadeException {
     return svci.getCalendarsHandler().add(val, parentPath);
   }
 
   @Override
   public void updateCollection(final BwCalendar col)
           throws CalFacadeException {
     svci.getCalendarsHandler().update(col);
   }
 
   @Override
   public boolean deleteCollection(final BwCalendar val,
                                   final boolean emptyIt)
           throws CalFacadeException {
    return svci.getCalendarsHandler().delete(val, emptyIt, true);
   }
 
   @Override
   public void moveCollection(final BwCalendar val,
                              final BwCalendar newParent)
           throws CalFacadeException {
     svci.getCalendarsHandler().move(val, newParent);
   }
 
   /* ------------------------------------------------------------
    *                     Categories
    * ------------------------------------------------------------ */
 
   @Override
   public boolean addCategory(final BwCategory val)
           throws CalFacadeException {
     return svci.getCategoriesHandler().add(val);
   }
 
   @Override
   public void updateCategory(final BwCategory val)
           throws CalFacadeException {
     BwCategory pval = svci.getCategoriesHandler().getPersistent(val.getUid());
 
     if (pval == null) {
       throw new CalFacadeException("No such category");
     }
 
     if (pval.updateFrom(val)) {
       svci.getCategoriesHandler().update(pval);
     }
   }
 
   private static class ClDeleteReffedEntityResult implements
           DeleteReffedEntityResult {
     private boolean deleted;
 
     private Collection<EventPropertiesReference> references;
 
     private ClDeleteReffedEntityResult(final boolean deleted) {
       this.deleted = deleted;
     }
 
     private ClDeleteReffedEntityResult(final Collection<EventPropertiesReference> references) {
       this.references = references;
     }
 
     @Override
     public boolean getDeleted() {
       return deleted;
     }
 
     @Override
     public Collection<EventPropertiesReference> getReferences() {
       return references;
     }
   }
   @Override
   public DeleteReffedEntityResult deleteCategory(final BwCategory val)
           throws CalFacadeException {
     if (val == null) {
       return null;
     }
 
     Categories cats = svci.getCategoriesHandler();
     int delResult = cats.delete(val);
 
     if (delResult == 2) {
       return new ClDeleteReffedEntityResult(cats.getRefs(val));
     }
 
     if (delResult == 1) {
       return null;
     }
 
     return new ClDeleteReffedEntityResult(true);
   }
 
   /* ------------------------------------------------------------
    *                     Contacts
    * ------------------------------------------------------------ */
 
   @Override
   public void addContact(final BwContact val)
           throws CalFacadeException {
     svci.getContactsHandler().add(val);
   }
 
   @Override
   public void updateContact(final BwContact val)
           throws CalFacadeException {
     BwContact pval = svci.getContactsHandler().getPersistent(val.getUid());
 
     if (pval == null) {
       throw new CalFacadeException("No such contact");
     }
 
     if (pval.updateFrom(val)) {
       svci.getContactsHandler().update(pval);
     }
   }
 
   @Override
   public DeleteReffedEntityResult deleteContact(final BwContact val)
           throws CalFacadeException {
     if (val == null) {
       return null;
     }
 
     EventProperties<BwContact> cs = svci.getContactsHandler();
     int delResult = cs.delete(val);
 
     if (delResult == 2) {
       return new ClDeleteReffedEntityResult(cs.getRefs(val));
     }
 
     if (delResult == 1) {
       return null;
     }
 
     return new ClDeleteReffedEntityResult(true);
   }
 
   private static class ClCheckEntityResult<T> implements CheckEntityResult<T> {
     private EventProperties.EnsureEntityExistsResult<T> eeer;
 
     @Override
     public boolean getAdded() {
       return eeer.added;
     }
 
     @Override
     public T getEntity() {
       return eeer.entity;
     }
   }
 
   @Override
   public CheckEntityResult<BwContact> ensureContactExists(final BwContact val,
                                                           final String ownerHref)
           throws CalFacadeException {
     ClCheckEntityResult<BwContact> cer = new ClCheckEntityResult<>();
 
     cer.eeer = svci.getContactsHandler().ensureExists(val, ownerHref);
 
     return cer;
   }
 
   /* ------------------------------------------------------------
    *                     Locations
    * ------------------------------------------------------------ */
 
   @Override
   public boolean addLocation(final BwLocation val)
           throws CalFacadeException {
     return svci.getLocationsHandler().add(val);
   }
 
   @Override
   public void updateLocation(final BwLocation val)
           throws CalFacadeException {
     BwLocation pval = svci.getLocationsHandler().getPersistent(val.getUid());
 
     if (pval == null) {
       throw new CalFacadeException("No such location");
     }
 
     if (pval.updateFrom(val)) {
       svci.getLocationsHandler().update(pval);
     }
   }
 
   @Override
   public DeleteReffedEntityResult deleteLocation(final BwLocation val)
           throws CalFacadeException {
     if (val == null) {
       return null;
     }
 
     EventProperties<BwLocation> locs = svci.getLocationsHandler();
     int delResult = locs.delete(val);
 
     if (delResult == 2) {
       return new ClDeleteReffedEntityResult(locs.getRefs(val));
     }
 
     if (delResult == 1) {
       return null;
     }
 
     return new ClDeleteReffedEntityResult(true);
   }
 
   @Override
   public CheckEntityResult<BwLocation> ensureLocationExists(final BwLocation val,
                                                             final String ownerHref)
           throws CalFacadeException {
     ClCheckEntityResult<BwLocation> cer = new ClCheckEntityResult<>();
 
     cer.eeer = svci.getLocationsHandler().ensureExists(val, ownerHref);
 
     return cer;
   }
 
   /* ------------------------------------------------------------
    *                     Events
    * ------------------------------------------------------------ */
 
   @Override
   public void claimEvent(final BwEvent ev) throws CalFacadeException {
     svci.getEventsHandler().claim(ev);
   }
 
   @Override
   public EventInfo.UpdateResult addEvent(final EventInfo ei,
                                          final boolean noInvites,
                                          final boolean scheduling,
                                          final boolean rollbackOnError)
           throws CalFacadeException {
     return svci.getEventsHandler().add(ei, noInvites, scheduling,
                                        false, rollbackOnError);
   }
 
   @Override
   public EventInfo.UpdateResult updateEvent(final EventInfo ei,
                                             final boolean noInvites,
                                             final String fromAttUri)
           throws CalFacadeException {
     return svci.getEventsHandler().update(ei, noInvites, fromAttUri);
   }
 
   @Override
   public boolean deleteEvent(final EventInfo ei,
                              final boolean sendSchedulingMessage)
           throws CalFacadeException {
     return svci.getEventsHandler().delete(ei, sendSchedulingMessage);
   }
 
   @Override
   public void markDeleted(final BwEvent event)
           throws CalFacadeException {
     svci.getEventsHandler().markDeleted(event);
   }
 
   /* ------------------------------------------------------------
    *                     Notifications
    * ------------------------------------------------------------ */
 
   @Override
   public void removeNotification(final NotificationType val)
           throws CalFacadeException {
     svci.getNotificationsHandler().remove(val);
   }
 
   /* ------------------------------------------------------------
    *                     Resources
    * ------------------------------------------------------------ */
 
   @Override
   public void saveResource(final String path,
                            final BwResource val) throws CalFacadeException {
     svci.getResourcesHandler().save(path, val);
   }
 
   @Override
   public void updateResource(final BwResource val,
                              final boolean updateContent)
           throws CalFacadeException {
     svci.getResourcesHandler().update(val, updateContent);
   }
 
   /* ------------------------------------------------------------
    *                     Scheduling
    * ------------------------------------------------------------ */
 
   @Override
   public ScheduleResult schedule(final EventInfo ei,
                                  final int method,
                                  final String recipient,
                                  final String fromAttUri,
                                  final boolean iSchedule)
           throws CalFacadeException {
     return svci.getScheduler().schedule(ei, method, recipient,
                                         fromAttUri, iSchedule);
   }
 
   @Override
   public ScheduleResult requestRefresh(final EventInfo ei,
                                        final String comment)
           throws CalFacadeException {
     return svci.getScheduler().requestRefresh(ei, comment);
   }
 
   /* ------------------------------------------------------------
    *                     Sharing
    * ------------------------------------------------------------ */
 
   @Override
   public ShareResultType share(final String principalHref,
                                final BwCalendar col,
                                final ShareType share)
           throws CalFacadeException {
     return svci.getSharingHandler().share(principalHref, col, share);
   }
 
   @Override
   public void publish(final BwCalendar col) throws CalFacadeException {
     svci.getSharingHandler().publish(col);
   }
 
   @Override
   public void unpublish(final BwCalendar col)
           throws CalFacadeException {
     svci.getSharingHandler().unpublish(col);
   }
 
   @Override
   public SharingI.ReplyResult sharingReply(final InviteReplyType reply)
           throws CalFacadeException {
     return svci.getSharingHandler().reply(getHome(), reply);
   }
 
   @Override
   public SharingI.SubscribeResult subscribe(final String colPath,
                                             final String subscribedName)
           throws CalFacadeException {
     return svci.getSharingHandler().subscribe(colPath, subscribedName);
   }
 
   @Override
   public SharingI.SubscribeResult subscribeExternal(final String extUrl,
                                                     final String subscribedName,
                                                     final int refresh,
                                                     final String remoteId,
                                                     final String remotePw)
           throws CalFacadeException {
     return svci.getSharingHandler().subscribeExternal(extUrl,
                                                       subscribedName,
                                                       refresh,
                                                       remoteId,
                                                       remotePw);
   }
 
   /* ------------------------------------------------------------
    *                     Views
    * ------------------------------------------------------------ */
 
   @Override
   public boolean addView(final BwView val,
                          final boolean makeDefault)
           throws CalFacadeException {
     return svci.getViewsHandler().add(val, makeDefault);
   }
 
   @Override
   public boolean addViewCollection(final String name,
                                    final String path)
           throws CalFacadeException {
     return svci.getViewsHandler().addCollection(name, path);
   }
 
   @Override
   public boolean removeViewCollection(final String name,
                                       final String path)
           throws CalFacadeException {
     return svci.getViewsHandler().removeCollection(name,
                                                    path);
   }
 
   @Override
   public boolean removeView(final BwView val)
           throws CalFacadeException {
     return svci.getViewsHandler().remove(val);
   }
 
   @Override
   public void moveContents(final BwCalendar cal,
                            final BwCalendar newCal)
           throws CalFacadeException {
     // TODO - getResource a set of keys then move each - or bulk mod?
 
     Collection<EventInfo> eis = svci.getEventsHandler().
             getEvents(cal,
                       null,
                       null,
                       null,
                       null, // retrieveList
                       RecurringRetrievalMode.overrides);
 
     for (EventInfo ei: eis) {
       BwEvent ev = ei.getEvent();
 
       ev.setColPath(newCal.getPath());
 
       svci.getEventsHandler().update(ei, false, null);
     }
 
     Collection<BwCalendar> cals = svci.getCalendarsHandler().getChildren(cal);
 
     for (BwCalendar c: cals) {
       svci.getCalendarsHandler().move(c, newCal);
     }
   }
 
   @Override
   public void changeAccess(final BwShareableDbentity ent,
                            final Collection<Ace> aces,
                            final boolean replaceAll)
           throws CalFacadeException {
     svci.changeAccess(ent, aces, replaceAll);
   }
 
   /* ------------------------------------------------------------
    *                   Filters
    * ------------------------------------------------------------ */
 
   @Override
   public void saveFilter(final BwFilterDef val)
           throws CalFacadeException {
     svci.getFiltersHandler().save(val);
   }
 
   @Override
   public void deleteFilter(final String name)
           throws CalFacadeException {
     svci.getFiltersHandler().delete(name);
   }
 }
