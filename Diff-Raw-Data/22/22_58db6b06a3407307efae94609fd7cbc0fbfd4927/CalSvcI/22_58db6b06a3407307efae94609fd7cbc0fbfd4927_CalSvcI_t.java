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
 
 package org.bedework.calsvci;
 
 import org.bedework.calfacade.BwAlarm;
 import org.bedework.calfacade.BwCalendar;
 import org.bedework.calfacade.BwCategory;
 import org.bedework.calfacade.BwDateTime;
 import org.bedework.calfacade.BwDuration;
 import org.bedework.calfacade.BwEvent;
 import org.bedework.calfacade.BwEventAlarm;
 import org.bedework.calfacade.BwFreeBusy;
 import org.bedework.calfacade.BwLocation;
 import org.bedework.calfacade.BwPrincipal;
 import org.bedework.calfacade.BwSponsor;
 import org.bedework.calfacade.BwStats;
 import org.bedework.calfacade.BwSynchInfo;
 import org.bedework.calfacade.BwSynchState;
 import org.bedework.calfacade.BwSystem;
 import org.bedework.calfacade.BwUser;
 import org.bedework.calfacade.CalFacadeException;
 import org.bedework.calfacade.base.BwShareableDbentity;
 import org.bedework.calfacade.filter.BwFilter;
 import org.bedework.calfacade.ifs.CalTimezones;
 import org.bedework.calfacade.ifs.Groups;
 import org.bedework.calfacade.svc.BwPreferences;
 import org.bedework.calfacade.svc.BwSubscription;
 import org.bedework.calfacade.svc.BwView;
 import org.bedework.calfacade.svc.EventInfo;
 import org.bedework.calfacade.svc.UserAuth;
 import org.bedework.icalendar.IcalCallback;
 
 import edu.rpi.cct.uwcal.resources.Resources;
 
 import java.io.Serializable;
 import java.util.Collection;
 
 import net.fortuna.ical4j.model.component.VTimeZone;
 import net.fortuna.ical4j.model.TimeZone;
 
 /** This is the service interface to the calendar suite. This will be
  * used by web applications and web services as well as other applications
  * which wish to integrate calendar actions into their interface.
  *
  * <p>This is a high level interface which carries out commonly used
  * calendar operations which may involve a number of interactions with the
  * underlying database implementation.
  *
  * @author Mike Douglass       douglm@rpi.edu
  */
 public abstract class CalSvcI implements Serializable {
   /** (Re)initialise the object for a particular use.
    *
    * @param pars        Defines the global parameters for the object
    * @throws CalFacadeException
    */
   public abstract void init(CalSvcIPars pars) throws CalFacadeException;
 
   /** Called after init to flag this user as a super user.
    *
    * @param val       true for a super user
   * @throws CalFacadeException
    */
  public abstract void setSuperUser(boolean val) throws CalFacadeException;
 
   /** Called after init to flag this user as a super user.
    *
    * @return boolean true if super user
    */
   public abstract boolean getSuperUser();
 
   /** Get the current stats
    *
    * @return BwStats object
    * @throws CalFacadeException if not admin
    */
   public abstract BwStats getStats() throws CalFacadeException;
 
   /** Enable/disable db statistics
    *
    * @param enable       boolean true to turn on db statistics collection
    * @throws CalFacadeException if not admin
    */
   public abstract void setDbStatsEnabled(boolean enable) throws CalFacadeException;
 
   /**
    *
    * @return boolean true if statistics collection enabled
    * @throws CalFacadeException if not admin
    */
   public abstract boolean getDbStatsEnabled() throws CalFacadeException;
 
   /** Dump db statistics
    *
    * @throws CalFacadeException if not admin
    */
   public abstract void dumpDbStats() throws CalFacadeException;
 
   /** Get db statistics
    *
    * @return Collection of BwStats.StatsEntry objects
    * @throws CalFacadeException if not admin
    */
   public abstract Collection getDbStats() throws CalFacadeException;
 
   /** Get the system pars
    *
    * @return BwSystem object
    * @throws CalFacadeException if not admin
    */
   public abstract BwSystem getSyspars() throws CalFacadeException;
 
   /** Update the system pars
    *
    * @param val BwSystem object
    * @throws CalFacadeException if not admin
    */
   public abstract void updateSyspars(BwSystem val) throws CalFacadeException;
 
   /** Get the timezones cache object
    *
    * @return CalTimezones object
    * @throws CalFacadeException if not admin
    */
   public abstract CalTimezones getTimezones() throws CalFacadeException;
 
   /** Log the current stats
    *
    * @throws CalFacadeException if not admin
    */
   public abstract void logStats() throws CalFacadeException;
 
   /** Flush any backend data we may be hanging on to ready for a new
    * sequence of interactions. This is intended to help with web based
    * applications, especially those which follow the action/render url
    * pattern used in portlets.
    *
    * <p>A flushAll can discard a back end session allowing open to get a
    * fresh one. close() can then be either a no-op or something like a
    * hibernate disconnect.
    *
    * <p>This method should be called before calling open (or after calling
    * close).
    *
    * @throws CalFacadeException
    */
   public abstract void flushAll() throws CalFacadeException;
 
   /** Signal the start of a sequence of operations. These overlap transactions
    * in that there may be 0 to many transactions started and ended within an
    * open/close call and many open/close calls within a transaction.
    *
    * <p>The open close methods are mainly associated with web style requests
    * and open will usually be called early in the incoming request handling
    * and close will always be called on the way out allowing the interface an
    * opportunity to reacquire (on open) and release (on close) any resources
    * such as connections.
    *
    * @throws CalFacadeException
    */
   public abstract void open() throws CalFacadeException;
 
   /**
    * @return boolean true if open
    */
   public abstract boolean isOpen();
 
   /** Call on the way out after handling a request..
    *
    * @throws CalFacadeException
    */
   public abstract void close() throws CalFacadeException;
 
   /** Start a (possibly long-running) transaction. In the web environment
    * this might do nothing. The endTransaction method should in some way
    * check version numbers to detect concurrent updates and fail with an
    * exception.
    *
    * @throws CalFacadeException
    */
   public abstract void beginTransaction() throws CalFacadeException;
 
   /** End a (possibly long-running) transaction. In the web environment
    * this should in some way check version numbers to detect concurrent updates
    * and fail with an exception.
    *
    * @throws CalFacadeException
    */
   public abstract void endTransaction() throws CalFacadeException;
 
   /** Call if there has been an error during an update process.
    *
    * @throws CalFacadeException
    */
   public abstract void rollbackTransaction() throws CalFacadeException;
 
   /**
    * @return IcalCallback for ical
    */
   public abstract IcalCallback getIcalCallback();
 
   /** Return a string valued calendar environment property
    *
    * @param name
    * @return String property value
    * @throws CalFacadeException
    */
   public abstract String getEnvProperty(String name) throws CalFacadeException;
 
   /** Get the set of internationalized resources for this session
    *
    * @return Resources
    */
   public abstract Resources getResources();
 
   /** Get a name uniquely.identifying this system. This should take the form <br/>
    *   name@host
    * <br/>where<ul>
    * <li>name identifies the particular calendar system at the site</li>
    * <li>host part identifies the domain of the site.</li>..
    * </ul>
    *
    * @return String    globally unique system identifier.
    * @throws CalFacadeException
    */
   public abstract String getSysid() throws CalFacadeException;
 
   /** Returns a value object representing the current user.
    *
    * @return UserVO       representing the current user
    * @throws CalFacadeException
    */
   public abstract BwUser getUser() throws CalFacadeException;
 
   /** Switch to the given non-null user. The access rights are determined by
    * the previous call to init.
    *
    * @param val         String user id
    * @throws CalFacadeException
    */
   public abstract void setUser(String val) throws CalFacadeException;
 
   /** Find the user with the given account name..
    *
    * @param val         String user id
    * @return BwUser       representing the user
    * @throws CalFacadeException
    */
   public abstract BwUser findUser(String val) throws CalFacadeException;
 
   /** Add an entry for the user.
    *
    * @param user
    * @throws CalFacadeException
    */
   public abstract void addUser(BwUser user) throws CalFacadeException;
 
   /** Returns a Collection of instance owners.
    *
    * @return Collection    of BwUser
    * @throws CalFacadeException
    */
   public abstract Collection getInstanceOwners() throws CalFacadeException;
 
   /** Get a UserAuth object which allows the application to determine what
    * special rights the user has.
    *
    * <p>Note that the returned object may require a one time initialisation
    * call to set it up correctly.
    * @see org.bedework.calfacade.svc.UserAuth#initialise(String, UserAuth.CallBack, int, boolean)
    * @see org.bedework.calfacade.svc.UserAuth#initialise(String, UserAuth.CallBack, Object, boolean)
    *
    * @param user         String account name
    * @param par          parameter object
    * @return UserAuth    implementation.
    * @throws CalFacadeException
    */
   public abstract UserAuth getUserAuth(String user, Object par)
       throws CalFacadeException;
 
   /** Get an initialised UserAuth object
    *
    * @return UserAuth    implementation.
    * @throws CalFacadeException
    */
   public abstract UserAuth getUserAuth() throws CalFacadeException;
 
   /** Get a Groups object which allows the application to handle
    * groups. This will be the default object for the current usage, i.e. admin
    * or user.
    *
    * @return Groups    implementation.
    * @throws CalFacadeException
    */
   public abstract Groups getGroups() throws CalFacadeException;
 
   /** Get a Groups object for users. This allows the admin client to
    * display or manipulate user groups.
    *
    * @return Groups    implementation.
    * @throws CalFacadeException
    */
   public abstract Groups getUserGroups() throws CalFacadeException;
 
   /** Get a Groups object for administrators. This allows the admin client to
    * display or manipulate administrator groups.
    *
    * @return Groups    implementation.
    * @throws CalFacadeException
    */
   public abstract Groups getAdminGroups() throws CalFacadeException;
 
   /** Refresh the users events
    *
    * @throws CalFacadeException
    */
   public abstract void refreshEvents() throws CalFacadeException;
 
   /** Return true if we believe a refresh is required. After a return of
    * true this method willreturn false until somebody updates the db.
    *
    * @return boolean    time to refresh
    * @throws CalFacadeException
    */
   public abstract boolean refreshNeeded() throws CalFacadeException;
 
   /* ====================================================================
    *                   Preferences
    * ==================================================================== */
 
   /** Returns the current user preferences.
    *
    * @return BwPreferences   prefs for the current user
    * @throws CalFacadeException
    */
   public abstract BwPreferences getUserPrefs() throws CalFacadeException;
 
   /** Returns the given user preferences.
    *
    * @param user
    * @return BwPreferences   prefs for the given user
    * @throws CalFacadeException
    */
   public abstract BwPreferences getUserPrefs(BwUser user) throws CalFacadeException;
 
   /** Update the current user preferences.
    *
    * @param  val     BwPreferences prefs for the current user
    * @throws CalFacadeException
    */
   public abstract void updateUserPrefs(BwPreferences val) throws CalFacadeException;
 
   /* ====================================================================
    *                   Access
    * ==================================================================== */
 
   /** Change the access to the given calendar entity.
   *
   * @param ent      BwShareableDbentity
   * @param aces     Collection of ace
   * @throws CalFacadeException
   */
  public abstract void changeAccess(BwShareableDbentity ent,
                                    Collection aces) throws CalFacadeException;
 
   /* ====================================================================
    *                   Timezones
    * ==================================================================== */
 
   /** Save a timezone definition in the database. The timezone is in the
    * form of a complete calendar definition containing a single VTimeZone
    * object.
    *
    * <p>The calendar must be on a path from a timezone root
    *
    * @param tzid
    * @param timezone
    * @throws CalFacadeException
    */
   public abstract void saveTimeZone(String tzid, VTimeZone timezone)
         throws CalFacadeException;
 
   /** Register a timezone object in the current session.
    *
    * @param id
    * @param timezone
    * @throws CalFacadeException
    */
   public abstract void registerTimeZone(String id, TimeZone timezone)
       throws CalFacadeException;
 
   /** Get a timezone object given the id. This will return transient objects
    * registered in the timezone directory
    *
    * @param id
    * @return TimeZone with id or null
    * @throws CalFacadeException
    */
   public abstract TimeZone getTimeZone(final String id) throws CalFacadeException;
 
   /** Find a timezone object in the database given the id. This bypasses
    * the directory.
    *
    * @param id
    * @param owner     event owner or null for current user
    * @return VTimeZone with id or null
    * @throws CalFacadeException
    */
   public abstract VTimeZone findTimeZone(final String id, BwUser owner) throws CalFacadeException;
 
   /** Clear all public timezone objects
    *
    * <p>Will remove all public timezones in preparation for a replacement
    * (presumably)
    *
    * @throws CalFacadeException
    */
   public abstract void clearPublicTimezones() throws CalFacadeException;
 
   /** Refresh the public timezone table - presumably after a call to clearPublicTimezones.
    * and many calls to saveTimeZone.
    *
    * @throws CalFacadeException
    */
   public abstract void refreshTimezones() throws CalFacadeException;
 
   /* ====================================================================
    *                   Calendars
    * ==================================================================== */
 
   /** Returns the tree of public calendars
    *
    * @return BwCalendar   root with all children attached
    * @throws CalFacadeException
    */
   public abstract BwCalendar getPublicCalendars() throws CalFacadeException;
 
   /** Return a list of public calendar collections.
    *
    * <p>Caldav currently does not allow collections inside collections so that
    * calendar collections are the leaf nodes only.
    *
    * @return Collection   of CalendarVO
    * @throws CalFacadeException
    */
   public abstract Collection getPublicCalendarCollections() throws CalFacadeException;
 
   /** Returns calendars owned by the current user.
    *
    * <p>For anonymous (public events) access, this method returns the same
    * as getPublicCalendars().
    *
    * <p>For authenticated, personal access this always returns the user
    * entry in the /user calendar tree, e.g. for user smithj it would return
    * an entry smithj
    *
    * @return BwCalendar   root with all children attached
    * @throws CalFacadeException
    */
   public abstract BwCalendar getCalendars() throws CalFacadeException;
 
   /** Return a list of user calendar collections.
    *
    * <p>Caldav currently does not allow collections inside collections so that
    * calendar collections are the leaf nodes only.
    *
    * @return Collection   of CalendarVO
    * @throws CalFacadeException
    */
   public abstract Collection getCalendarCollections() throws CalFacadeException;
 
   /** Return a list of calendars in which calendar objects can be
    * placed by the current user.
    *
    * <p>Caldav currently does not allow collections inside collections so that
    * calendar collections are the leaf nodes only.
    *
    * @return Collection   of BwCalendar
    * @throws CalFacadeException
    */
   public abstract Collection getAddContentCalendarCollections()
           throws CalFacadeException;
 
   /** Return true if calendar is in use for events.
    *
    * @param val   BwCalendar
    * @return boolean true for in use
    * @throws CalFacadeException
    */
   public abstract boolean getCalendarInuse(BwCalendar val)
           throws CalFacadeException;
 
   /** Get a calendar we are interested in. This is represented by the id
    * of a calendar.
    *
    * @deprecated
    * @param  val     int id of calendar
    * @return CalendarVO null for unknown calendar
    * @throws CalFacadeException
    */
   public abstract BwCalendar getCalendar(int val) throws CalFacadeException;
 
   /** Get a calendar given the path
    *
    * @param  path     String path of calendar
    * @return BwCalendar null for unknown calendar
    * @throws CalFacadeException
    */
   public abstract BwCalendar getCalendar(String path) throws CalFacadeException;
 
   /** set the default calendar for the current user.
    *
    * @param  val    BwCalendar
    * @throws CalFacadeException
    */
   public abstract void setPreferredCalendar(BwCalendar  val) throws CalFacadeException;
 
   /** Get the default calendar for the current user.
    *
    * @return BwCalendar null for unknown calendar
    * @throws CalFacadeException
    */
   public abstract BwCalendar getPreferredCalendar() throws CalFacadeException;
 
   /** Add a calendar object
    *
    * <p>The new calendar object will be added to the db. If the indicated parent
    * is null it will be added as a root level calendar.
    *
    * <p>Certain restrictions apply, mostly because of interoperability issues.
    * A calendar cannot be added to another calendar which already contains
    * entities, e.g. events etc.
    *
    * <p>Names cannot contain certain characters - (complete this)
    *
    * <p>Name must be unique at this level, i.e. all paths must be unique
    *
    * @param  val     BwCalendar new object
    * @param  parentPath  String path to parent.
    * @throws CalFacadeException
    */
   public abstract void addCalendar(BwCalendar val, String parentPath)
       throws CalFacadeException;
 
   /** Update a calendar object
    *
    * @param  val     CalendarVO object
    * @throws CalFacadeException
    */
   public abstract void updateCalendar(BwCalendar val) throws CalFacadeException;
 
   /** Delete a calendar. Also remove it from the current user preferences (if any).
    *
    * @param val      CalendarVO calendar
    * @return int     0 if it was deleted.
    *                 1 if it didn't exist
    *                 2 if in use
    * @throws CalFacadeException
    */
   public abstract int deleteCalendar(BwCalendar val) throws CalFacadeException;
 
   /* ====================================================================
    *                   Views
    * ==================================================================== */
 
   /** Add a view.
    *
    * @param  val           BwView to add
    * @param  makeDefault   boolean true for make this the default.
    * @return boolean false view not added, true - added.
    * @throws CalFacadeException
    */
   public abstract boolean addView(BwView val,
                                   boolean makeDefault) throws CalFacadeException;
 
   /** Remove the view.
    *
    * @param  val     BwView
    * @return boolean false - view not found.
    * @throws CalFacadeException
    */
   public abstract boolean removeView(BwView val) throws CalFacadeException;
 
   /** Find the named view.
    *
    * @param  val     String view name - null means default
    * @return BwView  null view not found.
    * @throws CalFacadeException
    */
   public abstract BwView findView(String val) throws CalFacadeException;
 
   /** Add a subscription to the named view.
    *
    * @param  name    String view name - null means default
    * @param  sub     BwSubscription to add
    * @return boolean false view not found, true - subscription added.
    * @throws CalFacadeException
    */
   public abstract boolean addViewSubscription(String name,
                                               BwSubscription sub) throws CalFacadeException;
 
   /** Remove a subscription from the named view.
    *
    * @param  name    String view name - null means default
    * @param  sub     BwSubscription to add
    * @return boolean false view not found, true - subscription added.
    * @throws CalFacadeException
    */
   public abstract boolean removeViewSubscription(String name,
                                                  BwSubscription sub) throws CalFacadeException;
 
   /** Return the collection of views - named collections of subscriptions
    *
    * @return collection of views
    * @throws CalFacadeException
    */
   public abstract Collection getViews() throws CalFacadeException;
 
   /* ====================================================================
    *                   Current selection
    * This defines how we select events to display.
    * ==================================================================== */
 
   /** Set the view to the given named view. Null means reset to default.
    * Unset current subscriptions.
    *
    * @param  val     String view name - null for default
    * @return boolean false - view not found.
    * @throws CalFacadeException
    */
   public abstract boolean setCurrentView(String val) throws CalFacadeException;
 
   /** Get the current view we have set
    *
    * @return BwView    named Collection of BwSubscription or null for default
    * @throws CalFacadeException
    */
   public abstract BwView getCurrentView() throws CalFacadeException;
 
   /** Set the view to the given collection of subscriptions.
    * Unset current view.
    *
    * @param  val     Collection
    * @throws CalFacadeException
    */
   public abstract void setCurrentSubscriptions(Collection val)
           throws CalFacadeException;
 
   /** Get the current subscriptions we have set
    *
    * @return Collection of BwSubscription or null
    * @throws CalFacadeException
    */
   public abstract Collection getCurrentSubscriptions() throws CalFacadeException;
 
   /* ====================================================================
    *                   Search and filters
    * ==================================================================== */
 
   /** Set a search filter using the suppplied search string
    *
    * @param val    String search parameters
    * @throws CalFacadeException
    */
   public abstract void setSearch(String val) throws CalFacadeException;
 
   /** Return the current search string
    *
    * @return  String     search parameters
    * @throws CalFacadeException
    */
   public abstract String getSearch() throws CalFacadeException;
 
   /** Add a filter to the database. All references must be set.
    *
    * @param val
    * @throws CalFacadeException
    */
   public abstract void addFilter(BwFilter val) throws CalFacadeException;
 
   /** Update a filter
    *
    * @param  val           FilterVO object to upate
    * @throws CalFacadeException
    */
   public abstract void updateFilter(BwFilter val) throws CalFacadeException;
 
   /* ====================================================================
    *                   Subscriptions
    * ==================================================================== */
 
   /** Add a subscription for the current user.
    *
    * @param val      BwSubscription object
    * @throws CalFacadeException
    */
   public abstract void addSubscription(BwSubscription val)
           throws CalFacadeException;
 
   /** Find the named subscription for the current user.
    *
    * @param name     Name for subscription
    * @return BwSubscription object or null for not found
    * @throws CalFacadeException
    */
   public abstract BwSubscription findSubscription(String name) throws CalFacadeException;
 
   /** Remove a subscription for the current user.
    *
    * @param val      BwSubscription object
    * @throws CalFacadeException
    */
   public abstract void removeSubscription(BwSubscription val) throws CalFacadeException;
 
   /** Update subscriptions after a change.
    *
    * @param val      BwSubscription object
    * @throws CalFacadeException
    */
   public abstract void updateSubscription(BwSubscription val) throws CalFacadeException;
 
   /** Get this users subscription.
    *
    * @return Collection of subscriptions
    * @throws CalFacadeException
    */
   public abstract Collection getSubscriptions() throws CalFacadeException;
 
   /** Fetch the subscription from the db given an id
    *
    * @param id
    * @return the subscription
    * @throws CalFacadeException
    */
   public abstract BwSubscription getSubscription(int id) throws CalFacadeException;
 
   /** Ensure the subscription has a calendar object attached.
    *
    *  <p>No change will take place for a subscription witht the calendar marked
    *  as deleted or for an external subscription.
    *
    *  <p>If a calendar is already attached just returns with that.
    *
    *  <p>Otherwise attempts to fetch the calendar. On failure marks it as deleted
    *  and returns null, else embeds the object and returns with it.
    *
    * @param val      BwSubscription object
    * @return BwCalendar   or null
    * @throws CalFacadeException
    */
   public abstract BwCalendar getSubCalendar(BwSubscription val) throws CalFacadeException;
 
   /* ====================================================================
    *                   Free busy
    * ==================================================================== */
 
   /** Get the free busy for the given principal. A granularity of null results
    * in a list of busy periods. Otherwise the start to end time is divided up
    * into granularity sized chunks which will only be reported if one or more
    * events fall in the segment.
    *
    * @param cal    Calendar to provide free-busy for. Null or the user root
    *               for default collection (as specified by user).
    * @param who
    * @param start
    * @param end
    * @param granularity BwDuration object defining how to divide free/busy
    *                    null for one big glob. or set to e.g. 1 hour for
    *                    hourly chunks.
    * @param returnAll   if true return entries for free time otherwise just for busy
    *                    (only for granularity not null)
    * @return BwFreeBusy
    * @throws CalFacadeException
    */
   public abstract BwFreeBusy getFreeBusy(BwCalendar cal, BwPrincipal who,
                                          BwDateTime start, BwDateTime end,
                                          BwDuration granularity,
                                          boolean returnAll)
           throws CalFacadeException;
 
   /* ====================================================================
    *                   Categories
    * ==================================================================== */
 
   /** Return a collection of BwCategory objects for the current user.
    * Returns an empty collection for no categories.
    *
    * @return Collection     of categories
    * @throws CalFacadeException
    */
   public abstract Collection getCategories() throws CalFacadeException;
 
   /** Return all public categories. Returns an empty  collection for no categories.
    *
    * @return Collection        of categories
    * @throws CalFacadeException
    */
   public abstract Collection getPublicCategories() throws CalFacadeException;
 
   /** Return all editable categories. Returns an empty collection for no categories.
    *
    * @return Collection        of categories
    * @throws CalFacadeException
    */
   public abstract Collection getEditableCategories() throws CalFacadeException;
 
   /** Add a Category to the database. The id will be set in the parameter
    * object.
    *
    * @param val   BwCategory object to be added
    * @throws CalFacadeException
    */
   public abstract void addCategory(BwCategory val) throws CalFacadeException;
 
   /** Replace a category in the database.
    *
    * @param val   BwCategory object to be replaced
    * @throws CalFacadeException
    */
   public abstract void replaceCategory(BwCategory val) throws CalFacadeException;
 
   /** Delete a category with the given id. Also remove it from the current
    * user preferences (if any).
    *
    * @param val      BwCategory category
    * @return int     0 if it was deleted.
    *                 1 if it didn't exist
    *                 2 if in use
    * @throws CalFacadeException
    */
   public abstract int deleteCategory(BwCategory val) throws CalFacadeException;
 
   /** Search the categories owned by this user for a matching name
    *
    * @param val          BwCategory object to match for
    * @return BwCategory matching object or null if no match.
    * @throws CalFacadeException
    */
   public abstract BwCategory findCategory(BwCategory val) throws CalFacadeException;
 
   /** Ensure a category exists. If it does sets the id in the object and returns
    * false (not added).
    * If not creates the entity then sets the new id in the object and returns
    * true (added)..
    *
    * @param category   BwCategory value object. If this object has the id set,
    *                  we assume the check was made previously.
    * @return boolean  true if we added the entry
    * @throws CalFacadeException
    */
   public abstract boolean ensureCategoryExists(BwCategory category)
       throws CalFacadeException;
 
   /** Return given category
    *
    * @param id          int id of the category
    * @return BwCategory  category value object
    * @throws CalFacadeException
    */
   public abstract BwCategory getCategory(int id) throws CalFacadeException;
 
   /* ====================================================================
    *                   Locations
    * ==================================================================== */
 
   /** Return all locations for this user. Returns an empty collection for
    * no locations. If this is a public admin instance, this will return all
    * public locations owned by the current user. Otherwise it returns all
    * personal locations owned by the user.
    *
    * @return Collection        of location value objects
    * @throws CalFacadeException
    */
   public abstract Collection getLocations() throws CalFacadeException;
 
   /** Return public locations - excluding reserved entries. Returns an empty
    * collection for no locations.
    *
    * @return Collection        of location value objects
    * @throws CalFacadeException
    */
   public abstract Collection getPublicLocations() throws CalFacadeException;
 
   /** Return all editable locations. Returns an empty collection for no locations.
    *
    * @return Collection        of location value objects
    * @throws CalFacadeException
    */
   public abstract Collection getEditableLocations() throws CalFacadeException;
 
   /** Add a location to the database. The id will be set in the parameter
    * object.
    *
    * @param val   LocationVO object to be added
    * @return boolean true for added, false for already exists
    * @throws CalFacadeException
    */
   public abstract boolean addLocation(BwLocation val) throws CalFacadeException;
 
   /** Replace a location in the database.
    *
    * @param val   LocationVO object to be replaced
    * @throws CalFacadeException
    */
   public abstract void replaceLocation(BwLocation val) throws CalFacadeException;
 
   /** Return a location with the given id
    *
    * @param id     int id of the location
    * @return LocationVo object representing the location in question
    *                     null if it doesn't exist.
    * @throws CalFacadeException
    */
   public abstract BwLocation getLocation(int id) throws CalFacadeException;
 
   /** Search the public locations for a matching address and owner
    *
    * @param val         BwLocation object to match for
    * @return BwLocation matching object or null if no match.
    * @throws CalFacadeException
    */
   public abstract BwLocation findLocation(BwLocation val) throws CalFacadeException;
 
   /** Delete a given location. If this is public admin the location must be
    * public.
    *
    * @param val      LocationVO object to be deleted
    * @return int     0 if it was deleted.
    *                 1 if it didn't exist
    *                 2 if in use
    * @throws CalFacadeException
    */
   public abstract int deleteLocation(BwLocation val) throws CalFacadeException;
 
   /** Ensure a location exists. If it already does returns the object.
    * If not creates the entity and sets the new id in the object and returns
    * null (added).
    *
    * @param val     LocationVO value object. If this object has the id set,
    *                we assume the check was made previously.
    * @return BwLocation  null if we added the entry
    * @throws CalFacadeException
    */
   public abstract BwLocation ensureLocationExists(BwLocation val)
       throws CalFacadeException;
 
   /** Return ids of events referencing the given location
    *
    * @param val      BwLocation object to be checked
    * @return Collection of Integer
    * @throws CalFacadeException
    */
   public abstract Collection getLocationRefs(BwLocation val) throws CalFacadeException;
 
   /* ====================================================================
    *                   Sponsors
    * ==================================================================== */
 
   /** Return a collection of BwSponsor objects for the current user.
    * Returns an empty collection for no sponsors.
    *
    * @return Collection     of sponsors
    * @throws CalFacadeException
    */
   public abstract Collection getSponsors() throws CalFacadeException;
 
   /** Return all public sponsors. Returns an empty  collection for no sponsors.
    *
    * @return Collection        of sponsor value objects
    * @throws CalFacadeException
    */
   public abstract Collection getPublicSponsors() throws CalFacadeException;
 
   /** Return all editable sponsors. Returns an empty collection for no sponsors.
    *
    * @return Collection        of sponsor value objects
    * @throws CalFacadeException
    */
   public abstract Collection getEditableSponsors() throws CalFacadeException;
 
   /** Add a sponsor to the database. The id will be set in the parameter
    * object.
    *
    * @param val   SponsorVO object to be added
    * @return boolean true for added, false for already exists
    * @throws CalFacadeException
    */
   public abstract boolean addSponsor(BwSponsor val) throws CalFacadeException;
 
   /** Replace a sponsor in the database.
    *
    * @param val   SponsorVO object to be replaced
    * @throws CalFacadeException
    */
   public abstract void replaceSponsor(BwSponsor val) throws CalFacadeException;
 
   /** Return sponsor with the given id
    *
    * @param id         int id of the sponsor
    * @return SponsorVo object representing the sponsor in question
    *                     null if it doesn't exist.
    * @throws CalFacadeException
    */
   public abstract BwSponsor getSponsor(int id) throws CalFacadeException;
 
   /** Search the public sponsors for a matching name and owner
    *
    * @param s          BwSponsor object to match for
    * @return BwSponsor matching object or null if no match.
    * @throws CalFacadeException
    */
   public abstract BwSponsor findSponsor(BwSponsor s) throws CalFacadeException;
 
   /** Delete a sponsor with the given id. Also remove it from the current
    * user preferences (if any).
    *
    * @param val      SponsorVO object to be deleted
    * @return int     0 if it was deleted.
    *                 1 if it didn't exist
    *                 2 if in use
    * @throws CalFacadeException
    */
   public abstract int deleteSponsor(BwSponsor val) throws CalFacadeException;
 
   /** Ensure a sponsor exists. If it already does returns the object.
    * If not creates the entity and sets the new id in the object and returns
    * null (added).
    *
    * @param sponsor   SponsorVO value object. If this object has the id set,
    *                  we assume the check was made previously.
    * @return BwSponsor  null if we added the entry
    * @throws CalFacadeException
    */
   public abstract BwSponsor ensureSponsorExists(BwSponsor sponsor)
       throws CalFacadeException;
 
   /** Return ids of events referencing the given sponsor
    *
    * @param val      BwSponsor object to be checked
    * @return Collection of Integer
    * @throws CalFacadeException
    */
   public abstract Collection getSponsorRefs(BwSponsor val) throws CalFacadeException;
 
   /* ====================================================================
    *                   Events
    * ==================================================================== */
 
   /** Return a single event for the current user
    *
    * @param   eventId   int id of the event
    * @return  EventInfo   value object representing event.
    * @throws CalFacadeException
    *
    * @deprecated - other calendar systems won't support this. Doesn't make sense
    *               for recurring events.
    */
   public abstract EventInfo getEvent(int eventId) throws CalFacadeException;
 
   /** Return one or more events for the current user using the calendar, guid
    * and the recurrence id as a key.
    *
    * <p>For non-recurring events, one and only one event should be reurned.
    * For recurring events, the 'master' event defining the rules together
    * with any exceptions should be returned.
    *
    * @param   sub       BwSubscription object
    * @param   cal       BwCalendar object
    * @param   guid      String guid for the event
    * @param   recurrenceId String recurrence id or null
    * @param recurRetrieval Takes value defined in CalFacadeDefs.
    * @return  Collection of EventInfo objects representing event(s).
    * @throws CalFacadeException
    */
   public abstract Collection getEvent(BwSubscription sub, BwCalendar cal,
                                       String guid,
                                       String recurrenceId,
                                       int recurRetrieval)
         throws CalFacadeException;
 
   /**
    */
   public static class DelEventResult {
     /** */
     public boolean eventDeleted;
     /** */
     public boolean locationDeleted;
     /** */
     public int alarmsDeleted;
 
     /**
      * @param eventDeleted
      * @param locationDeleted
      * @param alarmsDeleted
      */
     public DelEventResult(boolean eventDeleted,
                           boolean locationDeleted,
                           int alarmsDeleted) {
       this.eventDeleted = eventDeleted;
       this.locationDeleted = locationDeleted;
       this.alarmsDeleted = alarmsDeleted;
     }
   }
 
   /** Return filtered events for the current user.
    *
    * @param sub          BwSubscription object - null means use current view otherwise
    *                     only for given subscribed calendar
    * @param recurRetrieval Takes value defined in.CalFacadeDefs
    * @return Collection of EventInfo objects
    * @throws CalFacadeException
    */
   public abstract Collection getEvents(BwSubscription sub,
                           int recurRetrieval) throws CalFacadeException;
 
   /** Return the events for the current user within the given date and time
    * range.
    *
    * @param sub          BwSubscription object - null means use current view otherwise
    *                     only for given subscribed calendar
    * @param filter       BwFilter object restricting search or null.
    * @param startDate    DateTimeVO start - may be null
    * @param endDate      DateTimeVO end - may be null.
    * @param recurRetrieval Takes value defined in.CalFacadeDefs
    * @return Collection  populated event value objects
    * @throws CalFacadeException
    */
   public abstract Collection getEvents(BwSubscription sub, BwFilter filter,
                                        BwDateTime startDate, BwDateTime endDate,
                                        int recurRetrieval) throws CalFacadeException;
 
   /** Delete an event. Optionally delete the event location if it is no
    * longer in use.
    *
    * @param event              EventVO object to be deleted
    * @param delUnreffedLoc     delete the location if unreferenced
    * @return DelEventResult    result.
    * @throws CalFacadeException
    */
   public abstract DelEventResult deleteEvent(BwEvent event,
                                              boolean delUnreffedLoc)
       throws CalFacadeException;
 
   /** This class allows add and update event to signal changes back to the
    * caller.
    */
   public static class EventUpdateResult {
     /** */
     public int locationsAdded;
     /** */
     public int locationsRemoved;
 
     /** */
     public int sponsorsAdded;
     /** */
     public int sponsorsRemoved;
 
     /** */
     public int categoriesAdded;
     /** */
     public int categoriesRemoved;
   }
 
   /** Add an event and ensure its location and sponsor exist.
    *
    * <p>For public events some calendar implementors choose to allow the
    * dynamic creation of locations and sponsors. For each of those, if we have
    * an id, then the object represents a preexisting database item.
    *
    * <p>Otherwise the client has provided information which will be used to
    * locate an already existing location or sponsor. Failing that we use the
    * information to create a new entry.
    *
    * <p>For user clients, we generally assume no sponsor and the location is
    * optional. However, both conditions are enforced at the application level.
    *
    * <p>On return the event object will have been updated. In addition the
    * location and sponsor may have been updated.
    *
    * <p>The event to be added may be a reference to another event. In this case
    * a number of fields should have been copied from that event. Other fields
    * will come from the target.
    *
    * @param cal          BwCalendar defining recipient calendar
    * @param event        BwEvent object to be added
    * @param overrides    Collection of BwEventProxy objects which override instances
    *                     of the new event
    * @return EventUpdateResult Counts of changes.
    * @throws CalFacadeException
    */
   public abstract EventUpdateResult addEvent(BwCalendar cal,
                                              BwEvent event,
                                              Collection overrides) throws CalFacadeException;
 
   /** Update an event.
    *
    * @param event         updated EventVO object
    * @throws CalFacadeException
    */
   public abstract void updateEvent(BwEvent event) throws CalFacadeException;
 
   /** For an event to which we have write access we simply mark it deleted.
    *
    * <p>Otherwise we add an annotation maarking the event as deleted.
    *
    * @param event
    * @throws CalFacadeException
    */
   public abstract void markDeleted(BwEvent event) throws CalFacadeException;
 
   /* ====================================================================
    *                       Caldav support
    * Caldav as it stands at the moment requires that we save the arbitary
    * names clients might assign to events.
    * ==================================================================== */
 
   /** Get events given the calendar and String name. Return null for not
    * found. For non-recurring there should be only one event. Otherwise we
    * return the currently expanded set of recurring events.
    *
    * @param cal        CalendarVO object
    * @param val        String possible name
    * @return Collection of EventInfo or null
    * @throws CalFacadeException
    */
   public abstract Collection findEventsByName(BwCalendar cal, String val)
           throws CalFacadeException;
 
   /* ====================================================================
    *                   Synchronization
    * ==================================================================== */
 
   /** Get the synch info for the current device and user.
    *
    * @return SynchInfoVO object or null.
    * @throws CalFacadeException
    */
    public abstract BwSynchInfo getSynchInfo() throws CalFacadeException;
 
   /** Add the synch info for the current device and user.
    *
    * @param val    SynchInfoVO object to add
    * @throws CalFacadeException
    */
   public abstract void addSynchInfo(BwSynchInfo val) throws CalFacadeException;
 
   /** Update the synch info for the current device and user. There must be
    * exactly one object to update.
    *
    * @param val    SynchInfoVO object to update
    * @throws CalFacadeException
    */
   public abstract void updateSynchInfo(BwSynchInfo val)
       throws CalFacadeException;
 
   /** Return synchronization state for the given event in the current
    * synchronization context as defined by the initialisation parameters,
    * that is the user and synchId. If the event has never been involved in a
    * synch process there will be no related synch state object.
    *
    * @param ev            EventVO object .
    * @return SynchStateVO object or null.
    * @throws CalFacadeException
    */
   public abstract BwSynchState getSynchState(BwEvent ev)
       throws CalFacadeException;
 
   /** Get the deleted synch states for the current user
    *
    * @return Collection of synch state objects (possibly empty)
    * @throws CalFacadeException
    */
   public abstract Collection getDeletedSynchStates() throws CalFacadeException;
 
   /** Add the synch state for the given event and user.
    *
    * @param val      SynchStateVO object.
    * @throws CalFacadeException
    */
   public abstract void addSynchState(BwSynchState val)
       throws CalFacadeException;
 
   /** Update the synch state for the given event and user. There must be
    * exactly one object to update.
    *
    * @param val    SynchStateVO object with associated event and user
    * @throws CalFacadeException
    */
   public abstract void updateSynchState(BwSynchState val)
       throws CalFacadeException;
 
   /** Get the synch data associated with the given object..
    *
    * @param val    SynchStateVO object
    * @throws CalFacadeException
    */
   public abstract void getSynchData(BwSynchState val) throws CalFacadeException;
 
   /** Set the synch state and data for the given user and event.
    * There may be zero to many objects to update.
    *
    * @param val    SynchStateVO object with associated event and user
    * @throws CalFacadeException
    */
   public abstract void setSynchData(BwSynchState val) throws CalFacadeException;
 
   /** Update the synch states for the current user and device.
    *
    * <p>This is called at the termination of synchronization to set any state
    * objects appropriately.
    *
    * <p><table cols="2">
    * <th><td>Old state</td><td>New state</td></th>
    * <tr><td>UNKNOWN</td><td>SYNCHRONIZED</td></tr>
    * <tr><td>SYNCHRONIZED</td><td>SYNCHRONIZED</td></tr>
    * <tr><td>NEW</td><td>SYNCHRONIZED</td></tr>
    * <tr><td>MODIFIED</td><td>SYNCHRONIZED</td></tr>
    * <tr><td>DELETED</td><td>entry deleted</td></tr>
    * <tr><td>CLIENT_DELETED</td><td>unchanged</td></tr>
    * <tr><td>CLIENT_DELETED_UNDELIVERED</td><td>CLIENT_DELETED</td></tr>
    * <tr><td>CLIENT_MODIFIED</td><td>unchanged</td></tr>
    * <tr><td>CLIENT_MODIFIED_UNDELIVERED</td><td>CLIENT_MODIFIED</td></tr>
    * </table>
    *
    * @throws CalFacadeException
    */
   public abstract void updateSynchStates() throws CalFacadeException;
 
   /* ====================================================================
    *                       Alarms
    * This is currently under development at RPI
    * ==================================================================== */
 
   /** Get any alarms for the given event and user.
    *
    * @param event      BwEvent event
    * @param user       BwUser representing user
    * @return Collection of ValarmVO.
    * @throws CalFacadeException
    */
   public abstract Collection getAlarms(BwEvent event, BwUser user) throws CalFacadeException;
 
   /** Set an alarm on the event.
    *
    * <p>The supplied alarm object has been appropriately initialised.
    * We validate it then set the alarm on the event for the current user.
    *
    * @param  event          BwEvent object
    * @param  alarm          BwEventAlarm object
    * @throws CalFacadeException
    */
   public abstract void setAlarm(BwEvent event,
                                 BwEventAlarm alarm) throws CalFacadeException;
 
   /** Update an alarm.
    *
    * @param val   ValarmVO  object .
    * @throws CalFacadeException
    */
   public abstract void updateAlarm(BwAlarm val) throws CalFacadeException;
 
   /** Return all unexpired alarms. If user is null all unexpired alarms will
    * be retrieved.
    *
    * @param user
    * @return Collection of unexpired alarms.
    * @throws CalFacadeException
    */
   public abstract Collection getUnexpiredAlarms(BwUser user) throws CalFacadeException;
 
   /** Return all unexpired alarms before a given time. If user is null all
    * unexpired alarms will be retrieved.
    *
    * @param user
    * @param triggerTime
    * @return Collection of unexpired alarms.
    * @throws CalFacadeException
    */
   public abstract Collection getUnexpiredAlarms(BwUser user, long triggerTime)
           throws CalFacadeException;
 }
 
