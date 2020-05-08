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
 package org.bedework.webcommon.event;
 
 import org.bedework.appcommon.AccessXmlUtil;
 import org.bedework.appcommon.ClientError;
 import org.bedework.appcommon.ClientMessage;
 import org.bedework.appcommon.client.Client;
 import org.bedework.appcommon.client.IcalCallbackcb;
 import org.bedework.calfacade.BwAttendee;
 import org.bedework.calfacade.BwCalendar;
 import org.bedework.calfacade.BwCategory;
 import org.bedework.calfacade.BwContact;
 import org.bedework.calfacade.BwDateTime;
 import org.bedework.calfacade.BwEvent;
 import org.bedework.calfacade.BwLocation;
 import org.bedework.calfacade.BwXproperty;
 import org.bedework.calfacade.base.StartEndComponent;
 import org.bedework.calfacade.exc.CalFacadeAccessException;
 import org.bedework.calfacade.exc.CalFacadeException;
 import org.bedework.calfacade.exc.ValidationError;
 import org.bedework.calfacade.svc.EventInfo;
 import org.bedework.calfacade.svc.EventInfo.UpdateResult;
 import org.bedework.calfacade.util.CalFacadeUtil;
 import org.bedework.calfacade.util.ChangeTable;
 import org.bedework.calfacade.util.ChangeTableEntry;
 import org.bedework.icalendar.IcalTranslator;
 import org.bedework.icalendar.Icalendar;
 import org.bedework.webcommon.Attendees;
 import org.bedework.webcommon.BwActionFormBase;
 import org.bedework.webcommon.BwRequest;
 import org.bedework.webcommon.BwWebUtil;
 import org.bedework.webcommon.TimeDateComponents;
 
 import edu.rpi.cmt.access.Acl;
 import edu.rpi.cmt.calendar.IcalDefs;
 import edu.rpi.cmt.calendar.PropertyIndex.PropertyInfoIndex;
 import edu.rpi.cmt.timezones.Timezones;
 import edu.rpi.sss.util.DateTimeUtil;
 import edu.rpi.sss.util.Util;
 
 import net.fortuna.ical4j.model.Dur;
 import net.fortuna.ical4j.model.Recur;
 import org.apache.struts.upload.FormFile;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 /** Action to add or modify an Event. The form has an addingEvent property to
  * distinguish.
  *
  * <p>Form should contain an initialized BwEvent object.
  *
  * <p>Optional request parameters:<ul>
  *      <li>  submitAndSend present to send scheduling request</li>
  *      <li>  saveState:   yes - to save state in form but suppress
  *                         validation and db update</li>
  *      <li>  subname:     Name of a subscription to an external calendar</li>
  *      <li>  calPath:     Path to a (writeable) calendar collection</li>
  *      <li>  schedule:    request or publish</li>
  *      <li>  category:    multi-valued categories to add to event</li>
  *      <li>  delcategory: multi-valued categories to remove from event</li>
  * </ul>
  *
  * <p>Forwards to:<ul>
  *      <li>"noAction"          input error or we want to ignore the request.</li>
  *      <li>forwardNoAccess     user not authorized.</li>
  *      <li>forwardRetry        invalid entity.</li>
  *      <li>"copy"              User wants to duplicate event.</li>
  *      <li>"delete"            User wants to delete event.</li>
  *      <li>"success"           added/updated ok.</li>
  * </ul>
  *
  * @author Mike Douglass
  */
 public class UpdateEventAction extends EventActionBase {
   /* (non-Javadoc)
    * @see org.bedework.webcommon.BwAbstractAction#doAction(javax.servlet.http.HttpServletRequest, org.bedework.webcommon.BwSession, org.bedework.webcommon.BwActionFormBase)
    */
   @Override
   public int doAction(final BwRequest request,
                       final BwActionFormBase form) throws Throwable {
     Client cl = request.getClient();
 
     boolean publicAdmin = cl.getPublicAdmin();
     boolean submitApp = form.getSubmitApp();
 
     String submitterEmail = null;
     //String submitter = null;
 
     boolean sendInvitations = request.present("submitAndSend");
     boolean publishEvent = request.present("publishEvent");
     boolean updateSubmitEvent = request.present("updateSubmitEvent");
 
     if ((publicAdmin && !form.getAuthorisedUser()) ||
         form.getGuest()) {
       cl.rollback();
       return forwardNoAccess;
     }
 
     if (request.present("access")) {
       // Fail this to stop someone screwing around with the access
       cl.rollback();
       return forwardNoAccess;
     }
 
     EventInfo ei = form.getEventInfo();
     BwEvent ev = ei.getEvent();
     if (ev == null) {
       return forwardNoAction;
     }
 
     if (publishEvent && (ev.getRecurrenceId() != null)) {
       // Cannot publish an instance - only the master
       cl.rollback();
       return forwardError;
     }
 
     /*
     BwEvent savedEv = form.retrieveSavedEvent();
     if (savedEv == null) {
       // Should barf
       savedEv = new BwEventObj();
     }*/
 
     /* This should be done by a wrapper */
     ChangeTable changes = ei.getChangeset(cl.getCurrentPrincipalHref());
 
     /*
     BwEventAnnotation ann = null;
     if (ev instanceof BwEventProxy) {
       ann = ((BwEventProxy)ev).getRef();
       svci.reAttach(ann);
     } else {
       svci.reAttach(ev);
     }*/
 
     if (request.hasCopy()) {
       /* XXX I don't think we ever get here */
       copyEvent(ev, form);
 
       return forwardCopy;
     }
 
     ChangeTableEntry cte = null;
 
     /* -------------------------- Recurrences ------------------------------ */
     if (request.getBooleanReqPar("recurring", false)) {
       /* recurring turned on - if "freq" is null or "NONE" we get a null rrule
        * which mean leave the recurrences alone.
        */
       String rrule = getRrule(request, form);
       Collection<String> oldRrules = null;
 
       if (form.getErrorsEmitted()) {
         // Unrecoverable? Error in form.
         cl.rollback();
         return forwardValidationError;
       }
 
       if (rrule != null) {
         Collection<String> rrules = ev.getRrules();
 
         boolean rruleChanged = false;
 
         if (rrules == null) {
           Set<String> newRrules = new TreeSet<String>();
           newRrules.add(rrule);
           ev.setRrules(newRrules);
           rruleChanged = true;
         } else {
           if (rrules.size() == 0) {
             rruleChanged = true;
           } else if (rrules.size() > 1) {
             oldRrules = new ArrayList<String>(rrules);
             rrules.clear();
             rruleChanged = true;
           } else {
             String oldRrule = rrules.iterator().next();
             rruleChanged = !rrule.equals(oldRrule);
 
             if (rruleChanged) {
               oldRrules = new ArrayList<String>(rrules);
               rrules.clear();
             }
           }
         }
 
         if (rruleChanged) {
           ev.addRrule(rrule);
 
           cte = changes.getEntry(PropertyInfoIndex.RRULE);
           cte.setChanged(oldRrules, ev.getRrules());
           cte.setRemovedValues(oldRrules);
           cte.setAddedValues(new TreeSet<String>(ev.getRrules()));
         }
       }
 
       ev.setRecurring(ev.isRecurringEntity());
     } else if (ev.getRecurring() == null) {
       // Adding non-recurring
       ev.setRecurring(false);
     } else if (ev.testRecurring()) {
       // Turned recurring off
       /* Clear out any recurrence info */
 
       Collection<String> rrules = ev.getRrules();
 
       if (!Util.isEmpty(rrules)) {
         cte = changes.getEntry(PropertyInfoIndex.RRULE);
         cte.setChanged(rrules, null);
         cte.setRemovedValues(new ArrayList<String>(rrules));
 
         rrules.clear();
       }
 
       // Rdates and exdates should get handled below
       //ev.setRecurring(false);
     }
 
     /* ------------------------ Rdates and exdates -------------------------- */
 
     boolean evDateOnly = false;
     if (!form.getAddingEvent()) {
       evDateOnly = ev.getDtstart().getDateType();
     } else if (!request.present("initDates")) {
       evDateOnly = form.getEventDates().getStartDate().getDateOnly();
     }
 
     updateRExdates(request, ev, evDateOnly, changes);
 
     /* ----------------------- Change attendee list ------------------------- */
 
     if (request.present("editEventAttendees")) {
       int res = initMeeting(request, form, true);
 
       if (res != forwardSuccess) {
         return res;
       }
 
       return forwardEditEventAttendees;
     }
 
     /* -------------------- Turn event into meeting ------------------------- */
 
     if (request.present("makeEventIntoMeeting")) {
       int res = initMeeting(request, form, true);
 
       if (res != forwardSuccess) {
         return res;
       }
 
       return forwardEventIntoMeeting;
     }
 
     if (request.present("initDates")) {
       ev.setDtstart(request.getDateTime("meetingStartdt",
                                         "meetingStartTzid"));
       ev.setDuration("P" + request.getReqPar("meetingDuration") + "M");
       ev.setDtend(ev.getDtstart().addDur(new Dur(ev.getDuration())));
       ev.setEndType(StartEndComponent.endTypeDuration);
 
       form.getEventDates().setFromEvent(ev);
 
       return forwardEventDatesInited;
     }
 
     boolean adding = form.getAddingEvent();
 
     if (request.hasDelete()) {
       // Delete button in form
       return forwardDelete;
     }
 
     /* -------------------------- Collection ------------------------------ */
 
     if (!request.setEventCalendar(ei, changes)) {
       return forwardRetry;
     }
 
     String colPath = ev.getColPath();
     BwCalendar evCol = null;
     if (colPath != null) {
       evCol = cl.getCollection(colPath);
     }
 
     String submissionsRoot = form.getUnencodedSubmissionsRoot();
 
     if (publishEvent) {
       /* Event MUST NOT be in a submission calendar */
       if (colPath.startsWith(submissionsRoot)) {
         form.getErr().emit(ValidationError.inSubmissionsCalendar);
         cl.rollback();
         return forwardValidationError;
       }
     } else if (updateSubmitEvent) {
       /* Event MUST be in a submission calendar */
       if (!colPath.startsWith(submissionsRoot)) {
         form.getErr().emit(ValidationError.notSubmissionsCalendar);
         cl.rollback();
         return forwardValidationError;
       }
     }
 
     /* ------------------------ Text fields ------------------------------ */
 
     setEventText(request, ev, adding, changes);
 
     /* ---------------------- Uploaded image ----------------------------- */
 
     List<BwXproperty> extras = new ArrayList<BwXproperty>();
 
     FormFile ff = form.getEventImageUpload();
 
     if ((ff != null) && (ff.getFileSize() > 0)) {
       ProcessedImage pi = processImage(request, ff);
 
       if (!pi.OK) {
         if (!pi.retry) {
           cl.rollback();
           return forwardValidationError;
         }
         return forwardRetry;
       }
 
       BwXproperty imageXp = new BwXproperty();
       imageXp.setName(BwXproperty.bedeworkImage);
       imageXp.setValue(pi.image.getColPath() + "/" + pi.image.getName());
       extras.add(imageXp);
 
       imageXp = new BwXproperty();
       imageXp.setName(BwXproperty.bedeworkThumbImage);
       imageXp.setValue(pi.thumbnail.getColPath() + "/" + pi.thumbnail.getName());
       extras.add(imageXp);
     }
 
     /* ----------------------- X-properties ------------------------------ */
 
     int res = processXprops(request, ev, extras, publishEvent, changes);
     if (res == forwardValidationError) {
       cl.rollback();
       return res;
     }
 
     if ((publishEvent || updateSubmitEvent)) {
       // We might need the submitters info */
 
       List<BwXproperty> xps = ev.getXproperties(BwXproperty.bedeworkSubmitterEmail);
 
       if (!Util.isEmpty(xps)) {
         submitterEmail = xps.iterator().next().getValue();
       }
     }
 
     /* -------------------------- Dates ------------------------------ */
 
     res = form.getEventDates().updateEvent(ei);
     if (res == forwardValidationError) {
       cl.rollback();
       return res;
     }
 
     if (!BwWebUtil.validateEventDates(request, ei)) {
       return forwardRetry;
     }
 
     /* -------------------------- Location ------------------------------ */
 
     if (publicAdmin) {
       if (!adminEventLocation(form, ei)) {
         return forwardRetry;
       }
     } else {
       if (setEventLocation(request, ei, form, submitApp)) {
         // RFC says maybe for this.
         //incSequence = true;
       }
     }
 
     /* -------------------------- Contact ------------------------------ */
 
     if (!setEventContact(form, submitApp)) {
       return forwardRetry;
     }
 
     /* -------------------------- Aliases ------------------------------ */
 
     Set<BwCategory> cats = setEventAliases(request, ev);
     if (publicAdmin && !updateSubmitEvent && Util.isEmpty(cats)) {
       form.getErr().emit(ValidationError.missingTopic);
       return forwardRetry;
     }
 
     /* -------------------------- Categories ------------------------------ */
 
     SetEntityCategoriesResult secr = setEntityCategories(request, cats,
                                                          ev, changes);
     if (secr.rcode != forwardSuccess) {
       cl.rollback();
       return secr.rcode;
     }
 
     /* ------------------------- Link ---------------------------- */
 
     String link = Util.checkNull(form.getEventLink());
     if ((link != null) && (Util.validURI(link) == null)) {
       form.getErr().emit(ValidationError.invalidUri);
       return forwardRetry;
     }
 
     if (!Util.equalsString(ev.getLink(), link)) {
       changes.changed(PropertyInfoIndex.URL.getPname(), ev.getLink(), link);
       ev.setLink(link);
     }
 
     /* ------------------------- Cost ---------------------------- */
 
     String cost = Util.checkNull(form.getEventCost());
     if (!Util.equalsString(ev.getCost(), cost)) {
       changes.changed(PropertyInfoIndex.COST.getPname(), ev.getCost(), cost);
       ev.setCost(cost);
     }
 
     /* ------------------------- Transparency ---------------------------- */
 
     String transp = Util.checkNull(form.getTransparency());
     if (!Util.equalsString(ev.getTransparency(), transp)) {
       changes.changed(PropertyInfoIndex.TRANSP.getPname(), ev.getTransparency(),
                       transp);
       ev.setTransparency(transp);
     }
 
     /* ------------------------- publick ---------------------------- */
 
     ev.setPublick(publicAdmin);
 
     /* ------------------------- Status ---------------------------- */
 
     /* Set the status for the event, confirmed, tentative or canceled
      */
     String fStatus = form.getEventStatus();
 
     if (!Util.equalsString(fStatus, ev.getStatus())) {
       /* Changing the status */
 //      if (BwEvent.statusCancelled.equals(fStatus)) {
   //      canceling = true;
     //  }
 
       changes.changed(PropertyInfoIndex.STATUS.getPname(), ev.getStatus(),
                       fStatus);
       ev.setStatus(fStatus);
     }
 
     /* ------------------ final validation -------------------------- */
 
     /* If we're updating but not publishing a submitted event, treat it as
      * if it were the submit app.
      */
     List<ValidationError>  ves = BwWebUtil.validateEvent(cl,
                                 updateSubmitEvent || submitApp,
                                 publicAdmin,
                                 ev);
 
     if (ves != null) {
       for (org.bedework.calfacade.exc.ValidationError ve: ves) {
         form.getErr().emit(ve.getErrorCode(), ve.getExtra());
       }
       return forwardRetry;
     }
 
     /* ------------------------- Scheduling ---------------------------- */
 
     /* -------------------------- Attendees ---------------------------- */
 
     Attendees atts = form.getAttendees();
 
     cte = changes.getEntry(PropertyInfoIndex.ATTENDEE);
 
     if (adding) {
       ev.setAttendees(atts.getAttendees());
     }
 
     if (atts.getAddedAttendees() != null) {
       for (BwAttendee att: atts.getAddedAttendees()) {
         cte.addAddedValue(att);
       }
     }
 
     if (atts.getDeletedAttendees() != null) {
       for (BwAttendee att: atts.getDeletedAttendees()) {
         cte.addRemovedValue(att);
       }
     }
 
     if (request.getErrFlag()) {
       return forwardRetry;
     }
 
     /* ------------- web submit - copy entities and change owner ------------ */
 
     if (publishEvent) {
       copyEntities(ev);
       changeOwner(ev, cl);
 
       // Do the same for any overrides
 
       if (ev.getRecurring() &&
           (ei.getOverrideProxies() != null)) {
         for (BwEvent oev: ei.getOverrideProxies()) {
           copyEntities(oev);
           changeOwner(oev, cl);
           oev.setColPath(ev.getColPath());
         }
       }
     }
 
     /* --------------------- Add or update the event ------------------------ */
 
     if (debug) {
       debugMsg(changes.toString());
     }
 
     try {
       UpdateResult ur = null;
       ei.setNewEvent(adding);
 
       if (adding) {
         ur = cl.addEvent(ei,
                          !sendInvitations, false,
                          true);
       } else {
         ur = cl.updateEvent(ei, !sendInvitations, null);
       }
 
       if ((ur != null) && (ur.schedulingResult != null)) {
         emitScheduleStatus(form, ur.schedulingResult, false);
       }
 
       form.assignAddingEvent(false);
 
       /* -------------------------- Access ------------------------------ */
 
       String aclStr = request.getReqPar("acl");
       if (aclStr != null) {
         Acl acl = new AccessXmlUtil(null, cl).getAcl(aclStr, true);
 
         cl.changeAccess(ev, acl.getAces(), true);
       }
     } catch (CalFacadeException cfe) {
       cl.rollback();
 
       if (CalFacadeException.noRecurrenceInstances.equals(cfe.getMessage())) {
         form.getErr().emit(ClientError.noRecurrenceInstances,
                            ev.getUid());
         return forwardValidationError;
       }
 
       if (CalFacadeException.duplicateGuid.equals(cfe.getMessage())) {
         form.getErr().emit(ClientError.duplicateUid);
         return forwardDuplicate;
       }
 
       throw cfe;
     }
 
     if ((publishEvent || updateSubmitEvent) &&
         request.getBooleanReqPar("submitNotification", false)) {
       notifySubmitter(ei, submitterEmail, form);
     }
 
     if (publicAdmin) {
       /* See if we need to notify event registration system for add/update */
       BwXproperty evregprop = ev.findXproperty(BwXproperty.bedeworkEventRegStart);
 
       if (evregprop != null) {
         // Registerable event
         notifyEventReg(ei, form);
       }
 
       resetEvent(form);
     } else {
       form.refreshIsNeeded();
     }
 
     if (adding) {
       form.setEventInfo(null);
       form.getEventDates().setNewEvent(form.getEvent());
 
       if (ev.getEntityType() == IcalDefs.entityTypeTodo) {
         form.getMsg().emit(ClientMessage.addedTasks, 1);
       } else {
         form.getMsg().emit(ClientMessage.addedEvents, 1);
       }
     } else  if (ev.getEntityType() == IcalDefs.entityTypeTodo) {
       form.getMsg().emit(ClientMessage.updatedTask);
     } else {
       form.getMsg().emit(ClientMessage.updatedEvent);
     }
 
     return forwardSuccess;
   }
 
   /* This is a bedework function in which we specify which set of aliases
    * we used to add the event. Aliases are used to filter the data and provide a
    * view for users, e.g category="Films"
    *
    * We need these aliases to provide a way of informing the user what they can
    * subscribe to in order to see the events of interest.
    *
    * We also use them to set and unset categories, allowing event submitters to
    * consider only topic areas and leave it up to system administrators to
    * define which categories get set
    *
    * Each alias is a virtual path. For example "/user/adgrp_Eng/Lectures/Lectures"
    * might be a real path with two components
    * "/user/adgrp_Eng/Lectures" and
    * "Lectures"
    *
    * "/user/adgrp_Eng/Lectures" is aliased to "/public/aliases/Lectures" which
    * is a folder containing the alias "/public/aliases/Lectures/Lectures" which
    * is aliased to the single calendar.
    */
   private Set<BwCategory> setEventAliases(final BwRequest request,
                                           final BwEvent ev) throws Throwable {
     Client cl = request.getClient();
 
     if (!cl.getPublicAdmin() &&
         !request.getBwForm().getSubmitApp()) {
       return null;
     }
 
     /* The set of categories references by the aliases and their parents */
     Set<BwCategory> cats = new TreeSet<BwCategory>();
 
     Collection<BwXproperty> aliases = ev.getXproperties(BwXproperty.bedeworkAlias);
 
     if (Util.isEmpty(aliases)) {
       return cats;
     }
 
     for (BwXproperty alias: aliases) {
       Collection<BwCalendar> cols = null;
 
       try {
         cols = cl.decomposeVirtualPath(alias.getValue());
       } catch (CalFacadeException cfe) {
         request.getErr().emit(ClientError.unknownCalendar, alias);
       }
 
       if (Util.isEmpty(cols)) {
         continue;
       }
 
       /* For each alias in the returned list add any category to the set.
        *
        * For the last alias entry in the list work up to the root adding any
        * categories in.
        */
 
       BwCalendar curCol = null;
 
       for (BwCalendar col: cols) {
         if (!Util.isEmpty(col.getCategories())) {
           cats.addAll(col.getCategories());
         }
         if (col.getAlias()) {
           curCol = col;
         }
       }
 
       while (curCol != null) {
         try {
           curCol = cl.getCollection(curCol.getColPath());
           if (curCol != null) {
             cats.addAll(curCol.getCategories());
           }
         } catch (CalFacadeAccessException cfae) {
           // We'll assume that's OK. We'll get that for /user at least.
           break;
         }
       }
     }
 
     return cats;
   }
 
   private void changeOwner(final BwEvent ev,
                            final Client cl) throws CalFacadeException {
     cl.claimEvent(ev);
     ev.setCreatorHref(cl.getCurrentPrincipalHref());
   }
 
   private void copyEntities(final BwEvent ev) throws CalFacadeException {
     /* Copy event entities */
     BwLocation loc = ev.getLocation();
     if ((loc != null) && !loc.getPublick()) {
       loc = (BwLocation)loc.clone();
       loc.setOwnerHref(null);
       loc.setCreatorHref(null);
       loc.setPublick(true);
       ev.setLocation(loc);
     }
 
     BwContact contact = ev.getContact();
     if ((contact != null)  && !contact.getPublick()) {
       contact = (BwContact)contact.clone();
       contact.setOwnerHref(null);
       contact.setCreatorHref(null);
       contact.setPublick(true);
       ev.setLocation(loc);
       ev.setContact(contact);
     }
   }
 
   /** Update rdates and exdates.
    *
    * @param request
    * @param event
    * @param evDateOnly
    * @param changes
    * @return boolean  true if changed.
    * @throws Throwable
    */
   private boolean updateRExdates(final BwRequest request,
                                  final BwEvent event,
                                  final boolean evDateOnly,
                                  final ChangeTable changes) throws Throwable {
     Collection<BwDateTime> reqDates = request.getRdates(evDateOnly);
     Collection<BwDateTime> evDates = new TreeSet<BwDateTime>();
     if (event.getRdates() != null) {
       evDates.addAll(event.getRdates());
     }
 
     Collection<BwDateTime> added = null;
     Collection<BwDateTime> removed = null;
 
     boolean rdChanged = false;
 
     if (reqDates.isEmpty()) {
       if (!Util.isEmpty(evDates)) {
         removed = evDates;
         event.getRdates().clear();
         rdChanged = true;
       }
     } else if (Util.isEmpty(evDates)) {
       for (BwDateTime dt: reqDates) {
         event.addRdate(dt);
       }
 
       added = reqDates;
       event.setRecurring(true);
       rdChanged = true;
     } else {
       added = new TreeSet<BwDateTime>();
       removed = new TreeSet<BwDateTime>();
       rdChanged = CalFacadeUtil.updateCollection(false, reqDates,
                                                  event.getRdates(),
                                                  added, removed);
     }
 
     if (rdChanged) {
       ChangeTableEntry cte = changes.getEntry(PropertyInfoIndex.RDATE);
       cte.setChanged(evDates, event.getRdates());
       cte.setRemovedValues(removed);
       cte.setAddedValues(added);
     }
 
     boolean exdChanged = false;
 
     evDates.clear();
     if (event.getExdates() != null) {
       evDates.addAll(event.getExdates());
     }
     added = null;
     removed = null;
 
     reqDates = request.getExdates(evDateOnly);
 
     if (Util.isEmpty(reqDates)) {
       if (!Util.isEmpty(evDates)) {
         removed = evDates;
         event.getExdates().clear();
         exdChanged = true;
       }
     } else if (Util.isEmpty(evDates)) {
       for (BwDateTime dt: reqDates) {
         event.addExdate(dt);
       }
 
       added = reqDates;
       exdChanged = true;
     } else {
       added = new TreeSet<BwDateTime>();
       removed = new TreeSet<BwDateTime>();
       exdChanged = CalFacadeUtil.updateCollection(false, reqDates,
                                                   event.getExdates(),
                                                   added, removed);
     }
 
     if (exdChanged) {
       ChangeTableEntry cte = changes.getEntry(PropertyInfoIndex.EXDATE);
       cte.setChanged(evDates, event.getExdates());
       cte.setRemovedValues(removed);
       cte.setAddedValues(added);
     }
 
     return rdChanged || exdChanged;
   }
 
   private static final String alwaysRemove1 =
       "BEGIN:VEVENT\r\nUID:123456\r\nDTSTART;TYPE=DATE:20080212T000000\r\n";
 
   private static final String alwaysRemove2 =
       "BEGIN:VEVENT\nUID:123456\nDTSTART;TYPE=DATE:20080212T000000\n";
 
   /* return forwardNoAction for no change
    * forward success for change otherwise error.
    */
   private int processXprops(final BwRequest request,
                             final BwEvent event,
                             final List<BwXproperty> extras,
                             final boolean publishEvent,
                             final ChangeTable changes) throws Throwable {
     ChangeTableEntry cte = changes.getEntry(PropertyInfoIndex.XPROP);
     List<String> unparsedxps = request.getReqPars("xproperty");
 
     List<BwXproperty> added = null;
     List<BwXproperty> removed = null;
 
     List<BwXproperty> xprops = null;
 
    List<BwXproperty> evxprops = new ArrayList<BwXproperty>();
 
    if (!Util.isEmpty(event.getXproperties())) {
      evxprops.addAll(event.getXproperties());
     }
 
     /* When the xproperties get emitted we don't emit the set marked for skipping
      * We have to preserve those now.
      */
 
     if (unparsedxps != null) {
       xprops = parseXprops(request, /*publishEvent*/false);
 
       if (!Util.isEmpty(extras)) {
         if (xprops == null) {
           xprops = extras;
         } else {
           xprops.addAll(extras);
         }
       }
     }
 
     if (Util.isEmpty(xprops)) {
       if (Util.isEmpty(evxprops)) {
         return forwardNoAction;
       }
 
       for (BwXproperty xp:  evxprops) {
         if (xp.getSkipJsp()) {
           if (!xp.getName().equals(BwXproperty.bedeworkIcal) ||
               (xp.getValue() == null)) {
             continue;
           }
 
           if (!xp.getValue().startsWith(alwaysRemove1) &&
               !xp.getValue().startsWith(alwaysRemove2)) {
             continue;
           }
         }
 
         // Remove this one
         if (removed == null) {
           removed = new ArrayList<BwXproperty>();
         }
 
         removed.add(xp);
       }
 
       if (removed != null) {
         cte.setChanged(evxprops, event.getXproperties());
         cte.setRemovedValues(removed);
       }
 
       return forwardSuccess;
     }
 
     if (Util.isEmpty(evxprops)) {
       if (event.getXproperties() == null) {
         event.setXproperties(xprops);
       } else {
         event.getXproperties().addAll(xprops);
       }
 
       cte.setChanged(evxprops, event.getXproperties());
       cte.setAddedValues(xprops);
 
       return forwardSuccess;
     }
 
     // Add the skipped set to the 'new' set
 
     for (BwXproperty xp:  evxprops) {
       if (xp.getSkipJsp()) {
         if (xp.getName().equals(BwXproperty.bedeworkIcal)) {
           if ((xp.getValue() != null) &&
               (xp.getValue().startsWith(alwaysRemove1) ||
                xp.getValue().startsWith(alwaysRemove2))) {
             continue; // Bogus x-prop - remove it
           }
         }
 
         xprops.add(xp);
       }
     }
 
     added = new ArrayList<BwXproperty>();
     removed = new ArrayList<BwXproperty>();
 
     if (CalFacadeUtil.updateCollection(true, // clone them
                                        xprops, // make it look like this
                                        event.getXproperties(), // change this (to)
                                        added, removed)) {
       cte.setChanged(evxprops, event.getXproperties());
       cte.setAddedValues(added);
       cte.setRemovedValues(removed);
 
       return forwardSuccess;
     }
 
     return forwardNoAction;
   }
 
   /* This is a bit bogus but it will get us going. Make up a fake calendar,
    * parse it with ical4j and then extract the xproperties.
    *
    * We'll be rebuilding all of this anyway.
    */
   private List<BwXproperty> parseXprops(final BwRequest request,
                                         final boolean publishEvent) throws Throwable {
     Collection<String> unparsedxps = request.getReqPars("xproperty");
 
     if (unparsedxps == null) {
       return null;
     }
 
     StringBuilder sb = new StringBuilder();
     sb.append("BEGIN:VCALENDAR\n" +
               "VERSION:2.0\n" +
               "PRODID:-//xyz.com//EN\n" +
               "BEGIN:VEVENT\n" +
               "UID:123456\n" +
               "DTSTART;TYPE=DATE:20080212\n");
 
     for (String unparsedxp: unparsedxps) {
       /* Probably not the most efficient way */
       String val = unparsedxp.replace("\r\n", "\\n");
       val = val.replace("\n", "\\n");
       val = val.replace("\r", "\\n");
 
       sb.append(val);
       sb.append("\n");
     }
 
     sb.append("END:VEVENT\n" +
               "END:VCALENDAR\n");
 
     Client cl = request.getClient();
     IcalTranslator trans = new IcalTranslator(new IcalCallbackcb(cl));
     Icalendar c = trans.fromIcal(null, new StringReader(sb.toString()));
 
     BwEvent ev = c.getEventInfo().getEvent();
 
     List<BwXproperty> xprops = ev.getXproperties();
 
     List<BwXproperty> strippedXprops = new ArrayList<BwXproperty>();
 
     if (!publishEvent) {
       for (BwXproperty xp: xprops) {
         if (xp.getSkipJsp()) {
           // Should not be here
           continue;
         }
         strippedXprops.add(xp);
       }
       return strippedXprops;
     }
 
     /* Some x-properties added by the submission client contain private
      * information. We will remove all except a known set. Any that need to be
      * retained should be added to the set.
      *
      * The request parameter xprop-preserve names a property to save.
      */
 
     Map<String, String> plistMap = new HashMap<String, String>();
     for (String plistName: request.getReqPars("xprop-preserve")) {
       plistMap.put(plistName, plistName);
     }
 
     for (BwXproperty xp: xprops) {
       if (plistMap.get(xp.getName()) != null) {
         strippedXprops.add(xp);
       }
     }
 
     return strippedXprops;
   }
 
   private static HashMap<String, String> validFreq = new HashMap<String, String>();
   static {
     /* Block use of over-frequent recurrences */
     validFreq.put("HOURLY", "HOURLY");
     validFreq.put("DAILY", "DAILY");
     validFreq.put("WEEKLY", "WEEKLY");
     validFreq.put("MONTHLY", "MONTHLY");
     validFreq.put("YEARLY", "YEARLY");
   }
 
   /** Create a recurrence rule from the incoming request.
    *
    * <p>Incoming parameters are the following:</br>
    * <em>freq</em>HOURLY, DAILY, WEEKLY, MONTHLY, YEARLY</br>
    * <em>interval</em>Optional, default 1</br>
    * <em>count</em>Optional but only count OR until</br>
    * <em>until</em>Optional date, yyyyMMdd or UTC date/time yyyyMMddThhmmssZ</br>
    *
    * <p>If freq=WEEKLY, MONTHLY or YEARLY the following can be used</br>
    * <em>weekdays</em>Any value, same as byday=MO,TU,WE,TH,FR</br>
    * <em>weekends</em>Any value, same as byday=SA,SU</br>
    * <em>byday</em>List of days from SA,SU,MO,TU,WE,TH,FR</br>
    *
    * <p>If freq=MONTHLY or YEARLY the following can be used</br>
    * <em>bymonthday</em>Comma separated list of integer -31...31</br>
    *
    * <p>If freq=YEARLY the following can be used</br>
    * <em>bymonth</em>Comma separated list of integer 1...12</br>
    * <em>byyearday</em>Comma separated list of integer -366...366</br>
    *
    * <p>e.g. Every Friday the 13th,</br>
    *  RRULE:FREQ=MONTHLY;BYDAY=FR;BYMONTHDAY=13
    *
    * @param request
    * @param form
    * @return String or null
    * @throws Throwable
    */
   public String getRrule(final BwRequest request,
                          final BwActionFormBase form) throws Throwable {
     StringBuilder rrule = new StringBuilder();
     String freq = request.getReqPar("freq");
     if ((freq == null) || ("NONE".equals(freq)) ){
       return null;
     }
 
     if (validFreq.get(freq) == null) {
       form.getErr().emit("org.bedework.bad.request", "freq");
       return null;
     }
 
     rrule.append("FREQ=");
     rrule.append(freq);
 
     Integer interval = request.getIntReqPar("interval",
                                             ValidationError.invalidRecurInterval);
     if (interval != null) {
       rrule.append(";INTERVAL=");
       rrule.append(interval);
     }
 
     Integer count = request.getIntReqPar("count",
                                          ValidationError.invalidRecurCount);
     String until = request.getReqPar("until");
 
     if ((count != null) && (until != null)) {
       form.getErr().emit(ValidationError.invalidRecurCountAndUntil);
       return null;
     }
 
     if (until != null) {
       // RECUR This needs fixing when we get the widgets sorted out.
       /* For the time being, if the start is a date/time and we get date only for
        * until, add the time part of the start.
        *
        *  Also use the start timezone for until.
        */
       TimeDateComponents start = form.getEventDates().getStartDate();
 
       if (DateTimeUtil.isISODate(until)) {
         if (!start.getDateOnly()) {
           // Append start time.
           until += "T" + start.getDateTime().getDtval().substring(8);
         }
       }
       if (DateTimeUtil.isISODateTime(until)) {
         // Floating - convert to UTC using start timezone.
         until = Timezones.getUtc(until, start.getTzid());
       } else if (!DateTimeUtil.isISODateTimeUTC(until)) {
         form.getErr().emit(ValidationError.invalidRecurUntil, "until");
         return null;
       }
 
       rrule.append(";UNTIL=");
       rrule.append(until);
     }
 
     if (count != null) {
       if (count < 0) {
         form.getErr().emit(ValidationError.invalidRecurCount, count);
         return null;
       }
       rrule.append(";COUNT=");
 
       rrule.append(count);
     }
 
     if ("DAILY".equals(freq)) {
       // Any more to do?
     } else {
       String byday = request.getReqPar("byday");
 
       if (byday != null) {
         rrule.append(";BYDAY=");
         rrule.append(byday);
       }
 
       if ("WEEKLY".equals(freq)) {
         // Anything else here?
       } else {
         String bymonthday = request.getReqPar("bymonthday");
         if (bymonthday != null) {
           rrule.append(";BYMONTHDAY=");
           rrule.append(bymonthday);
         }
 
         if ("MONTHLY".equals(freq)) {
           // Anything else here?
         } else {
           String bymonth = request.getReqPar("bymonth");
           if (bymonth != null) {
             rrule.append(";BYMONTH=");
             rrule.append(bymonth);
           }
 
           String byyearday = request.getReqPar("byyearday");
           if (byyearday != null) {
             rrule.append(";BYYEARDAY=");
             rrule.append(byyearday);
           }
         }
       }
     }
 
     String rruleStr = rrule.toString();
 
     try {
       new Recur(rruleStr);
     } catch (Throwable t) {
       form.getErr().emit(ValidationError.invalidRecurRule);
       return null;
     }
 
     if (debug) {
       debugMsg(rruleStr);
     }
 
     return rruleStr;
   }
 }
