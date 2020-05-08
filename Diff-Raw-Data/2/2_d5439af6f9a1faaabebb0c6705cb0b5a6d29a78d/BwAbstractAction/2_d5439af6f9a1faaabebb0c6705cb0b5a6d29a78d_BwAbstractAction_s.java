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
 package org.bedework.webcommon;
 
 import org.bedework.appcommon.AdminConfig;
 import org.bedework.appcommon.BedeworkDefs;
 import org.bedework.appcommon.CalendarInfo;
 import org.bedework.appcommon.ClientError;
 import org.bedework.appcommon.ClientMessage;
 import org.bedework.appcommon.ConfigCommon;
 import org.bedework.appcommon.ImageProcessing;
 import org.bedework.appcommon.InOutBoxInfo;
 import org.bedework.appcommon.MyCalendarVO;
 import org.bedework.appcommon.NotificationInfo;
 import org.bedework.appcommon.TimeView;
 import org.bedework.appcommon.client.AdminClientImpl;
 import org.bedework.appcommon.client.Client;
 import org.bedework.appcommon.client.ClientImpl;
 import org.bedework.appcommon.client.ROClientImpl;
 import org.bedework.appcommon.client.SearchParams;
 import org.bedework.caldav.util.filter.FilterBase;
 import org.bedework.caldav.util.filter.ObjectFilter;
 import org.bedework.caldav.util.filter.OrFilter;
 import org.bedework.calfacade.BwCalendar;
 import org.bedework.calfacade.BwCategory;
 import org.bedework.calfacade.BwDateTime;
 import org.bedework.calfacade.BwDuration;
 import org.bedework.calfacade.BwEvent;
 import org.bedework.calfacade.BwEventObj;
 import org.bedework.calfacade.BwFilterDef;
 import org.bedework.calfacade.BwLocation;
 import org.bedework.calfacade.BwPreferences;
 import org.bedework.calfacade.BwPrincipal;
 import org.bedework.calfacade.BwResource;
 import org.bedework.calfacade.BwResourceContent;
 import org.bedework.calfacade.BwString;
 import org.bedework.calfacade.RecurringRetrievalMode;
 import org.bedework.calfacade.RecurringRetrievalMode.Rmode;
 import org.bedework.calfacade.ScheduleResult;
 import org.bedework.calfacade.ScheduleResult.ScheduleRecipientResult;
 import org.bedework.calfacade.base.BwTimeRange;
 import org.bedework.calfacade.base.CategorisedEntity;
 import org.bedework.calfacade.configs.AuthProperties;
 import org.bedework.calfacade.exc.CalFacadeAccessException;
 import org.bedework.calfacade.exc.CalFacadeException;
 import org.bedework.calfacade.exc.ValidationError;
 import org.bedework.calfacade.filter.BwCategoryFilter;
 import org.bedework.calfacade.filter.BwCreatorFilter;
 import org.bedework.calfacade.locale.BwLocale;
 import org.bedework.calfacade.svc.EventInfo;
 import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
 import org.bedework.calfacade.util.BwDateTimeUtil;
 import org.bedework.calfacade.util.ChangeTable;
 import org.bedework.calfacade.util.ChangeTableEntry;
 import org.bedework.calsvci.SchedulingI.FbResponses;
 import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
 import org.bedework.util.calendar.ScheduleStates;
 import org.bedework.util.calendar.XcalUtil;
 import org.bedework.util.misc.Util;
 import org.bedework.util.servlet.filters.ConfiguredXSLTFilter.XSLTConfig;
 import org.bedework.util.servlet.filters.PresentationState;
 import org.bedework.util.struts.Request;
 import org.bedework.util.struts.UtilAbstractAction;
 import org.bedework.util.struts.UtilActionForm;
 import org.bedework.util.timezones.DateTimeUtil;
 import org.bedework.util.timezones.Timezones;
 import org.bedework.webcommon.config.ClientConfigurations;
 
 import net.fortuna.ical4j.model.Dur;
 import org.apache.struts.upload.FormFile;
 import org.apache.struts.util.MessageResources;
 
 import java.io.ByteArrayInputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /** This abstract action performs common setup actions before the real
  * action method is called.
  *
  * @author  Mike Douglass  douglm@rpi.edu
  */
 public abstract class BwAbstractAction extends UtilAbstractAction
                                        implements ForwardDefs {
   /** Name of the init parameter holding our name */
   private static final String appNameInitParameter = "bwappname";
 
   static HashMap<String, Integer> viewTypeMap =
           new HashMap<>();
 
   static {
     for (int i = 0; i < BedeworkDefs.viewPeriodNames.length; i++) {
       viewTypeMap.put(BedeworkDefs.viewPeriodNames[i], i);
     }
   }
 
   @Override
   public String getId() {
     return getClass().getName();
   }
 
   /** This is the routine which does the work.
    *
    * @param request   For request pars and BwSession
    * @param frm       Action form
    * @return int      forward index
    * @throws Throwable
    */
   public abstract int doAction(BwRequest request,
                                BwActionFormBase frm) throws Throwable;
 
   @Override
   public String performAction(final Request request,
                               final MessageResources messages) throws Throwable {
     final BwActionFormBase form = (BwActionFormBase)request.getForm();
     String adminUserId = null;
 
     final BwCallback cb = getCb(request, form);
 
     final int status = cb.in(request);
     if (status != HttpServletResponse.SC_OK) {
       request.getResponse().setStatus(status);
       getLogger().error("Callback.in status=" + status);
       return forwards[forwardError];
     }
 
     setConfig(request);
 
     if (form.getConfig().getGuestMode()) {
       form.assignCurrentUser(null);
     } else {
       adminUserId = form.fetchCurrentAdminUser();
       if (adminUserId == null) {
         adminUserId = form.getCurrentUser();
       }
     }
 
     if (debug) {
       debugMsg("About to get state");
     }
 
     final BwSession bsess = getState(request, form, messages, adminUserId);
 
     if (bsess == null) {
       /* An error should have been emitted.*/
       return forwards[forwardError];
     }
 
     if (debug) {
       debugMsg("About to get state");
     }
 
     form.setSession(bsess);
 
     final BwRequest bwreq = new BwRequest(request, bsess, this);
 
     if (bwreq.present("refresh")) {
       bwreq.refresh();
     }
 
     final Collection<Locale> reqLocales = request.getLocales();
     final String reqLoc = request.getReqPar("locale");
 
     if (reqLoc != null) {
       if ("default".equals(reqLoc)) {
         form.setRequestedLocale(null);
       } else {
         try {
           final Locale loc = Util.makeLocale(reqLoc);
           form.setRequestedLocale(loc); // Make it stick
         } catch (Throwable t) {
           // Ignore bad parameter?
         }
       }
     }
 
     checkMvarReq(bwreq);
 
     final Client cl = bwreq.getClient();
 
     final Locale loc = cl.getUserLocale(reqLocales,
                                   form.getRequestedLocale());
     final BwModuleState mstate = bwreq.getModule().getState();
 
     if (loc != null) {
       BwLocale.setLocale(loc);
       final Locale cloc = form.getCurrentLocale();
       if ((cloc == null) | (!cloc.equals(loc))) {
         mstate.setRefresh(true);
       }
       form.setCurrentLocale(loc);
     }
 
     form.assignSuggestionEnabled(cl.getSystemProperties().getSuggestionEnabled());
     form.assignWorkflowEnabled(cl.getSystemProperties().getWorkflowEnabled());
     form.assignWorkflowRoot(cl.getSystemProperties().getWorkflowRoot());
     form.assignCurUserSuperUser(cl.isSuperUser());
     form.assignAdminGroupMaintOK(cl.getAdminGroupMaintOK());
     form.assignUserMaintOK(cl.getUserMaintOK());
     form.assignOneGroup(cl.getOneGroup());
     form.assignAdminUserPrincipal(cl.getCurrentPrincipal());
 
     // We need to have set the current locale before we do this.
     mstate.setCalInfo(CalendarInfo.getInstance());
 
     form.setGuest(bsess.isGuest());
 
     final BwPreferences prefs = cl.getPreferences();
 
     if (form.getGuest()) {
       // force public view on - off by default
       form.setPublicView(true);
     } else {
       form.assignImageUploadDirectory(prefs.getDefaultImageDirectory());
     }
 
     if (form.getNewSession()) {
       if (debug) {
         traceConfig(request);
       }
 
       form.setHour24(form.getConfig().getHour24());
       if (!cl.getPublicAdmin() &&
               !form.getSubmitApp() &&
               !form.getGuest()) {
         form.setHour24(prefs.getHour24());
       }
 
       form.setEndDateType(BwPreferences.preferredEndTypeDuration);
       if (!cl.getPublicAdmin() && !form.getGuest()) {
         form.setEndDateType(prefs.getPreferredEndType());
       }
 
       bsess.embedFilters(bwreq);
 
       if (debug) {
         debugMsg("Filters embedded");
       }
 
       if (!cl.getPublicAdmin()) {
         final String viewType = request.getReqPar("viewType");
         if (viewType != null) {
           mstate.setViewType(viewType);
         } else {
           mstate.setViewType(prefs.getPreferredViewPeriod());
         }
       }
     }
 
     /*if (debug) {
       BwFilter filter = bwreq.getFilter(debug);
       if (filter != null) {
         debugMsg(filter.toString());
       }
     }*/
 
     if (debug) {
       debugMsg("About to prepare render");
     }
 
     bsess.prepareRender(bwreq);
 
     if (debug) {
       debugMsg("current change token: " +
                        bwreq.getSessionAttr(BwSession.changeTokenAttr));
     }
 
     try{
       final String tzid = prefs.getDefaultTzid();
 
       if (tzid != null) {
         Timezones.setThreadDefaultTzid(tzid);
       }
     } catch (Throwable t) {
     }
 
     if (form.getDirInfo() == null) {
       form.setDirInfo(cl.getDirectoryInfo());
     }
 
     final PresentationState ps = getPresentationState(request);
 
     if (ps.getAppRoot() == null) {
       ps.setAppRoot(suffixRoot(request,
                                form.getConfig().getAppRoot()));
       ps.setBrowserResourceRoot(suffixRoot(request,
                                            form.getConfig().getBrowserResourceRoot()));
 
       // Set the default skin
 
       if (ps.getSkinName() == null) {
         // No skin name supplied - use the default
         final String skinName = prefs.getSkinName();
 
         ps.setSkinName(skinName);
         ps.setSkinNameSticky(true);
       }
     }
 
     /* Set up ready for the action - may reset svci */
 
     final int temp = actionSetup(bwreq, form);
     if (temp != forwardNoAction) {
       return forwards[temp];
     }
 
     /* see if we got cancelled */
 
     final String reqpar = request.getReqPar("cancelled");
 
     if (reqpar != null) {
       /** Set the objects to null so we get new ones.
        */
       form.getMsg().emit(ClientMessage.cancelled);
       return forwards[forwardCancelled];
     }
 
     if (!cl.getPublicAdmin() && !form.getGuest()) {
       InOutBoxInfo ib = form.getInBoxInfo();
       if (ib == null) {
         ib = new InOutBoxInfo(cl, true);
         form.setInBoxInfo(ib);
       } else {
         ib.refresh(cl, false);
       }
 
       NotificationInfo ni = form.getNotificationInfo();
       if (ni == null) {
         ni = new NotificationInfo();
         form.setNotificationInfo(ni);
       }
 
       ni.refresh(cl, false);
     }
 
     String forward;
 
     try {
       if (bwreq.present("viewType")) {
         mstate.setViewType(bwreq.getReqPar("viewType"));
 
         gotoDateView(bwreq,
                      mstate.getDate(),
                      mstate.getCurViewPeriod());
       }
 
       forward = forwards[doAction(bwreq, form)];
 
 //      bsess.prepareRender(bwreq);
     } catch (CalFacadeAccessException cfae) {
       form.getErr().emit(ClientError.noAccess);
       forward = forwards[forwardNoAccess];
       cl.rollback();
     } catch (CalFacadeException cfe) {
       form.getErr().emit(cfe.getMessage(), cfe.getExtra());
       form.getErr().emit(cfe);
 
       forward = forwards[forwardError];
       cl.rollback();
     } catch (Throwable t) {
       form.getErr().emit(t);
       forward = forwards[forwardError];
       cl.rollback();
     }
 
     return forward;
   }
 
   /** Called just before action.
    *
    * @param request
    * @param form
    * @return int foward index
    * @throws Throwable
    */
   public int actionSetup(final BwRequest request,
                          final BwActionFormBase form) throws Throwable {
     final Client cl = request.getClient();
 
     if (cl.getPublicAdmin()) {
       return AdminUtil.actionSetup(request);
     }
 
     // Not public admin.
 
     final ConfigCommon conf = form.getConfig();
 
     /*
     if (form.getNewSession()) {
       // Try to enable supersuer mode for personal clients.
       // First time through here for this session. svci is still set up for the
       // authenticated user. Set access rights.
       if (form.getCurrentUser().equals("root")) {
         form.fetchSvci().setSuperUser(true);
       }
     }
     */
 
     String refreshAction = request.getRefreshAction();
     Integer refreshInt = request.getRefreshInt();
 
     if (refreshAction == null) {
       refreshAction = conf.getRefreshAction();
     }
 
     if (refreshAction == null) {
       refreshAction = request.getActionPath();
     }
 
     if (refreshAction != null) {
       if (refreshInt == null) {
         refreshInt =  conf.getRefreshInterval();
       }
 
       setRefreshInterval(request.getRequest(), request.getResponse(),
                          refreshInt, refreshAction, form);
     }
 
     //if (debug) {
     //  log.debug("curTimeView=" + form.getCurTimeView());
     //}
 
     return forwardNoAction;
   }
 
   /** Set the config object.
    *
    * @param request
    * @throws Throwable
    */
   public void setConfig(final Request request) throws Throwable {
     BwActionFormBase form = (BwActionFormBase)request.getForm();
 
     if (form.configSet()) {
       return;
     }
 
     HttpSession session = request.getRequest().getSession();
     ServletContext sc = session.getServletContext();
 
     String appname = sc.getInitParameter("bwappname");
 
     if ((appname == null) || (appname.length() == 0)) {
       appname = "unknown-app-name";
     }
 
     String appType = sc.getInitParameter("bwapptype");
 
     if ((appType == null) || (appType.length() == 0)) {
       throw new Exception("No bwapptype context param");
     }
 
     ConfigCommon conf = ClientConfigurations.getConfigs().getClientConfig(appname);
     if (conf == null) {
       throw new CalFacadeException("No config available for app " + appname);
     }
 
 //    conf = (ConfigCommon)conf.clone();
     form.setConfig(conf); // So we can get an svci object and set defaults
     form.assignAppType(appType);
   }
 
   @Override
   public PresentationState getPresentationState(final Request req) throws Throwable {
     /* First try to get it from the module. */
 
     BwActionFormBase form = (BwActionFormBase)req.getForm();
 
     BwModule module = form.fetchModule(req.getModuleName());
     BwModuleState mstate = module.getState();
 
     PresentationState ps = mstate.getPresentationState();
 
     if (ps == null) {
       ps = new PresentationState();
       initPresentationState(req, ps);
 
       mstate.setPresentationState(ps);
     }
 
     XSLTConfig xc = mstate.getXsltConfig();
 
     if (xc == null) {
       Object o = req.getRequestAttr(ModuleXsltFilter.globalsName);
 
       if (o instanceof XSLTConfig) {
         xc = (XSLTConfig)o;
       } else {
         xc = new XSLTConfig();
       }
 
       mstate.setXsltConfig(xc);
     }
 
     req.setRequestAttr(ModuleXsltFilter.globalsName, xc);
 
     return ps;
   }
 
   protected int setSearchParams(final BwRequest request,
                                 final SearchParams params,
                                 final String viewMode) throws Throwable {
     final BwActionFormBase form = request.getBwForm();
     final BwModuleState mstate = request.getModule().getState();
     final Client cl = request.getClient();
 
     params.setPublicIndexRequested(request.present("public"));
 
     String startStr = request.getReqPar("start");
     String endStr = request.getReqPar("end");
 
     filterAndQuery(request, params);
 
     String sort = request.getReqPar("sort");
     if (sort == null) {
       // TODO - this shouldn't be a fixed string
       sort = "dtstart.utc:asc";
     }
     params.setSort(cl.parseSort(sort));
 
     if (Client.gridViewMode.equals(viewMode)) {
       TimeView tv = mstate.getCurTimeView();
       if (tv == null) {
         // Pretty much broken here
         params.setFromDate(todaysDateTime());
         return forwardSuccess;
       }
 
       // Ignore any end date
       if (startStr == null) {
         params.setFromDate(tv.getViewStart());
         params.setToDate(tv.getViewEnd());
         return forwardSuccess;
       }
 
       // Set current timeview to given date - rounded approopriately
 
       final BwDateTime bdt = BwDateTimeUtil.getDateTime(
               XcalUtil.getIcalFormatDateTime(startStr),
               true,
               false, null);
       gotoDateView(request,
                    bdt.getDtval(),
                    mstate.getCurViewPeriod());
       tv = mstate.getCurTimeView();
 
       params.setFromDate(tv.getViewStart());
       params.setToDate(tv.getViewEnd());
 
       return forwardSuccess;
     }
 
     if (startStr == null) {
       final String lim = mstate.getSearchLimits();
       if ((lim != null) && (!"none".equals(lim))) {  // there are limits
         if ("beforeToday".equals(lim)) {
           endStr = DateTimeUtil.isoDate(DateTimeUtil.yesterday());
         } else if ("fromToday".equals(lim)) {
           startStr = DateTimeUtil.isoDate(new java.util.Date());
         }
       }
     }
 
     final AuthProperties authp = cl.getAuthProperties();
 
     final int days = request.getIntReqPar("days", -32767);
 //    if (days < 0) {
   //    days = authp.getDefaultWebCalPeriod();
     //}
 
     if ((startStr == null) && (endStr == null)) {
       if (!cl.getWebSubmit() && !cl.getPublicAdmin()) {
         if (!form.getListAllEvents()) {
           params.setFromDate(todaysDateTime());
 
           if (days > 0) {
             params.setToDate(params.getFromDate().addDur(new Dur(days, 0,
                                                                  0, 0)));
           }
         }
       }
     } else if ((endStr != null) || (days > 0)) {
       int max = 0;
 
       if (!cl.isSuperUser()) {
         max = authp.getMaxWebCalPeriod();
       }
 
       final BwTimeRange tr =
               BwDateTimeUtil.getPeriod(startStr,
                                        endStr,
                                        java.util.Calendar.DATE,
                                        days,
                                        java.util.Calendar.DATE,
                                        max);
 
       if (tr == null) {
         form.getErr().emit(ClientError.badRequest, "dates");
         return forwardNoAction;
       }
 
       params.setFromDate(tr.getStart());
       params.setToDate(tr.getEnd());
     } else {
       params.setFromDate(BwDateTimeUtil.getDateTime(
               XcalUtil.getIcalFormatDateTime(startStr),
               true,
               false, null));
     }
 
     if (params.getFromDate() != null) {
       gotoDateView(request,
                    params.getFromDate().getDtval(),
                    mstate.getCurViewPeriod());
     }
 
     final int offset = request.getIntReqPar("offset", -1);
 
     if (offset > 0) {
       params.setCurOffset(offset);
     }
 
     int count = request.getIntReqPar("count", -1);
 
     if (count < 0) {
       count = cl.getPreferences().getPageSize();
     }
 
     if (count < 0) {
       count = 20;
     }
 
     if (count > 250) {
       count = 250;
     }
 
     params.setPageSize(count);
 
     params.setFormat(request.getReqPar("format"));
 
     return forwardSuccess;
   }
 
   private void filterAndQuery(final BwRequest request,
                               final SearchParams params) throws Throwable {
     final BwActionFormBase form = request.getBwForm();
     final Client cl = request.getClient();
 
     params.setQuery(request.getReqPar("query"));
     params.setRelevance(request.getBooleanReqPar("relevance", false));
 
     FilterBase filter = null;
     final BwFilterDef fd = request.getFilterDef();
 
     if (fd != null) {
       filter = fd.getFilters();
     }
 
     if (cl.getWebSubmit() || cl.getPublicAdmin()) {
       boolean ignoreCreator = cl.getPublicAdmin() && form.getListAllEvents();
 
 //      if ((cal != null) &&
 //              (cal.getPath().startsWith(form.getConfig().getSubmissionRoot()))) {
 //        ignoreCreator = true;
 //      } else
       if (!ignoreCreator && form.getCurUserSuperUser()) {
         ignoreCreator = "yes".equals(request.getReqPar("ignoreCreator"));
       }
 
       if (!ignoreCreator) {
         final BwCreatorFilter crefilter = new BwCreatorFilter(null);
         crefilter.setEntity(cl.getCurrentPrincipalHref());
 
         filter = FilterBase.addAndChild(filter, crefilter);
       }
     }
 
     params.setFilter(filter);
     params.setSort(cl.parseSort(request.getReqPar("sort")));
   }
 
   protected BwDateTime todaysDateTime() throws Throwable {
     return BwDateTimeUtil.getDateTime(DateTimeUtil.isoDate(),
                                       true,
                                       false,   // floating
                                       null);   // tzid
   }
 
   protected FilterBase addor(FilterBase filter, final BwCategory cat) {
     ObjectFilter<BwCategory> f = new BwCategoryFilter(null);
     f.setEntity(cat);
     f.setExact(false);
 
     if (filter == null) {
       return f;
     }
 
     if (!(filter instanceof OrFilter)) {
       FilterBase orFilter = new OrFilter();
       orFilter.addChild(filter);
       filter = orFilter;
     }
 
     filter.addChild(f);
 
     return filter;
   }
 
   protected void emitScheduleStatus(final BwActionFormBase form,
                                     final ScheduleResult sr,
                                     final boolean errorsOnly) {
     if (sr.errorCode != null) {
       form.getErr().emit(sr.errorCode, sr.extraInfo);
     }
 
     if (sr.ignored) {
       form.getMsg().emit(ClientMessage.scheduleIgnored);
     }
 
     if (sr.reschedule) {
       form.getMsg().emit(ClientMessage.scheduleRescheduled);
     }
 
     if (sr.update) {
       form.getMsg().emit(ClientMessage.scheduleUpdated);
     }
 
     for (ScheduleRecipientResult srr: sr.recipientResults.values()) {
       if (srr.getStatus() == ScheduleStates.scheduleDeferred) {
         form.getMsg().emit(ClientMessage.scheduleDeferred, srr.recipient);
       } else if (srr.getStatus() == ScheduleStates.scheduleNoAccess) {
         form.getErr().emit(ClientError.noSchedulingAccess, srr.recipient);
       } else if (!errorsOnly) {
         form.getMsg().emit(ClientMessage.scheduleSent, srr.recipient);
       }
     }
   }
 
   /** Find a principal object given a "user" request parameter.
    *
    * @param request     BwRequest for parameters
    * @return BwPrincipal     null if not found. Messages emitted
    * @throws Throwable
    */
   protected BwPrincipal findPrincipal(final BwRequest request) throws Throwable {
     String str = request.getReqPar("user");
     if (str == null) {
       request.getErr().emit(ClientError.unknownUser, "null");
       return null;
     }
 
     BwPrincipal p = request.getClient().getUser(str);
     if (p == null) {
       request.getErr().emit(ClientError.unknownUser, str);
       return null;
     }
 
     return p;
   }
 
   protected static class SetEntityCategoriesResult {
     /** rc */
     public int rcode = forwardNoAction;
 
     /** Number of BwCategory created */
     public int numCreated;
 
     /** Number of BwCategory added */
     public int numAdded;
 
     /** Number of BwCategory removed */
     public int numRemoved;
   }
 
   /** Set the entity categories based on multivalued request parameter "categoryKey".
    *
    * <p>We build a list of categories then update the membership of the entity
    * category collection to correspond.
    *
    * @param request
    * @param extraCats Categories to add as a result of other operations
    * @param changes
    * @param ent
    * @return setEventCategoriesResult  with rcode = error forward or
    *                    forwardNoAction for validated OK or
    *                    forwardSuccess for calendar changed
    * @throws Throwable
    */
   protected SetEntityCategoriesResult setEntityCategories(final BwRequest request,
                                                           final Set<BwCategory> extraCats,
                                                           final CategorisedEntity ent,
                                                           final ChangeTable changes) throws Throwable {
     BwActionFormBase form = request.getBwForm();
 
     // XXX We should use the change table code for this.
     SetEntityCategoriesResult secr = new SetEntityCategoriesResult();
 
     /* categories already set in event */
     Set<BwCategory> evcats = ent.getCategories();
 
     Collection<BwCategory> defCats = request.getSess().embedCategories(request, true,
                                                                        BwSession.defaultEntity);
 
 
     /* Get the uids */
     Collection<String> strCatUids = request.getReqPars("catUid");
 
     /* Remove all categories if we don't supply any
      */
 
     if (Util.isEmpty(strCatUids) &&
         Util.isEmpty(extraCats) &&
         Util.isEmpty(defCats)) {
       if (!Util.isEmpty(evcats)) {
         if (changes != null) {
           ChangeTableEntry cte = changes.getEntry(PropertyInfoIndex.CATEGORIES);
           cte.setRemovedValues(new ArrayList<BwCategory>(evcats));
         }
 
         secr.numRemoved = evcats.size();
         evcats.clear();
       }
       secr.rcode = forwardSuccess;
       return secr;
     }
 
     Client cl = request.getClient();
     Set<BwCategory> cats = new TreeSet<>();
 
     if (extraCats != null) {
       cats.addAll(extraCats);
     }
 
     if (!Util.isEmpty(defCats)) {
       for (BwCategory defcat: defCats) {
         cats.add(cl.getPersistentCategory(defcat.getUid()));
       }
     }
 
     if (!Util.isEmpty(strCatUids)) {
       buildList:
       for (String catUid: strCatUids) {
         /* If it's in the event add it to the list we're building then move on
          * to the next requested category.
          */
         if (evcats != null) {
           for (BwCategory evcat: evcats) {
             if (evcat.getUid().equals(catUid)) {
               cats.add(evcat);
               continue buildList;
             }
           }
         }
 
         BwCategory cat = cl.getPersistentCategory(catUid);
 
         if (cat != null) {
           cats.add(cat);
         }
       }
     }
 
     /* See if the user is adding new categories */
 
     Collection<String> reqCatKeys = request.getReqPars("categoryKey");
 
     if (!Util.isEmpty(reqCatKeys)) {
       Collection<String> catKeys = new ArrayList<>();
 
       /* request parameter can be comma delimited list */
       for (String catkey: reqCatKeys) {
         String[] parts = catkey.split(",");
 
         for (String part: parts) {
           if (part == null) {
             continue;
           }
 
           part = part.trim();
 
           if (part.length() == 0) {
             continue;
           }
 
           catKeys.add(part);
         }
       }
 
       for (String catkey: catKeys) {
         // LANG - use current language code?
         BwString key = new BwString(null, catkey);
 
         BwCategory cat = cl.getCategoryByName(key);
         if (cat == null) {
           cat = BwCategory.makeCategory();
 
           cat.setOwnerHref(cl.getCurrentPrincipalHref());
           cat.setWord(key);
 
           cl.addCategory(cat);
           secr.numCreated++;
         }
 
         cats.add(cat);
       }
     }
 
     /* cats now contains category objects corresponding to the request parameters
      *
      * Now we need to add or remove any in the event but not in our list.
      */
 
     /* First make a list to remove - to avoid concurrent update
      * problems with the iterator
      */
 
     ArrayList<BwCategory> toRemove = new ArrayList<>();
 
     if (evcats != null) {
       for (BwCategory evcat: evcats) {
         if (cats.contains(evcat)) {
           cats.remove(evcat);
           continue;
         }
 
         toRemove.add(evcat);
       }
     }
 
     for (BwCategory cat: cats) {
       ent.addCategory(cat);
       secr.numAdded++;
     }
 
     for (BwCategory cat: toRemove) {
       if (evcats.remove(cat)) {
         secr.numRemoved++;
       }
     }
 
     if ((changes != null)  &&
         (secr.numAdded > 0) && (secr.numRemoved > 0)) {
       ChangeTableEntry cte = changes.getEntry(PropertyInfoIndex.CATEGORIES);
       cte.setRemovedValues(toRemove);
       cte.setAddedValues(cats);
     }
 
     secr.rcode = forwardSuccess;
 
     if (secr.numCreated > 0) {
       form.getMsg().emit(ClientMessage.addedCategories, secr.numCreated);
     }
 
     return secr;
   }
 
   /**
    * @param request
    * @param form
    * @param atts
    * @param st
    * @param et
    * @param intunitStr
    * @param interval
    * @return int
    * @throws Throwable
    */
   public int doFreeBusy(final BwRequest request,
                         final BwActionFormBase form,
                         final Attendees atts,
                         final String st,
                         final String et,
                         final String intunitStr,
                         final int interval) throws Throwable {
     Client cl = request.getClient();
     BwModuleState mstate = request.getModule().getState();
 
     /*  Start of getting date/time - make a common method? */
 
     Calendar start;
     Calendar end;
 
     if (st == null) {
       /* Set period and start from the current timeview */
       TimeView tv = request.getSess().getCurTimeView(request);
 
       /* Clone calendar so we don't mess up time */
       start = (Calendar)tv.getFirstDay().clone();
       end = tv.getLastDay();
       end.add(Calendar.DATE, 1);
     } else {
       start = mstate.getCalInfo().getFirstDayOfThisWeek(Timezones.getDefaultTz(),
                                                       DateTimeUtil.fromISODate(st));
 
       // Set end to 1 week on.
       end = (Calendar)start.clone();
       end.add(Calendar.WEEK_OF_YEAR, 1);
     }
 
     // Don't allow more than a month
     Calendar check = Calendar.getInstance(mstate.getCalInfo().getLocale());
     check.setTime(start.getTime());
     check.add(Calendar.DATE, 32);
 
     if (check.before(end)) {
       return forwardBadRequest;
     }
 
     BwDateTime sdt = BwDateTimeUtil.getDateTime(start.getTime());
     BwDateTime edt = BwDateTimeUtil.getDateTime(end.getTime());
 
     /*  End of getting date/time - make a common method? */
 
     String originator = cl.getCurrentCalendarAddress();
     BwEvent fbreq = BwEventObj.makeFreeBusyRequest(sdt, edt,
                                                    null,     // organizer
                                                    originator,
                                                    atts.getAttendees(),
                                                    atts.getRecipients());
     if (fbreq == null) {
       return forwardBadRequest;
     }
 
     ScheduleResult sr = cl.schedule(new EventInfo(fbreq),
                                     fbreq.getScheduleMethod(),
                                     null, null, false);
     if (debug) {
       debugMsg(sr.toString());
     }
 
     if (sr.recipientResults != null) {
       for (ScheduleRecipientResult srr: sr.recipientResults.values()) {
         if (srr.getStatus() !=ScheduleStates.scheduleOk) {
           form.getMsg().emit(ClientMessage.freebusyUnavailable, srr.recipient);
         }
       }
     }
 
     BwDuration dur = new BwDuration();
 
     if (interval <= 0) {
       form.getErr().emit(ClientError.badInterval, interval);
       return forwardError;
     }
 
     if (intunitStr != null) {
       if ("minutes".equals(intunitStr)) {
         dur.setMinutes(interval);
       } else if ("hours".equals(intunitStr)) {
         dur.setHours(interval);
       } else if ("days".equals(intunitStr)) {
         dur.setDays(interval);
       } else if ("weeks".equals(intunitStr)) {
         dur.setWeeks(interval);
       } else {
         form.getErr().emit(ClientError.badIntervalUnit, interval);
         return forwardError;
       }
     } else {
       dur.setHours(interval);
     }
 
     FbResponses resps = cl.aggregateFreeBusy(sr, sdt, edt, dur);
     form.setFbResponses(resps);
 
     FormattedFreeBusy ffb = new FormattedFreeBusy(resps.getAggregatedResponse(),
                                                   mstate.getCalInfo().getLocale());
 
     form.setFormattedFreeBusy(ffb);
 
     emitScheduleStatus(form, sr, true);
 
     return forwardSuccess;
   }
 
   /** Method to retrieve an event. An event is identified by the calendar +
    * guid + recurrence id. We also take the subscription id as a parameter so
    * we can pass it along in the result for display purposes.
    *
    * <p>We cannot just take the calendar from the subscription, because the
    * calendar has to be the actual collection containing the event. A
    * subscription may be to higher up the tree (i.e. a folder).
    *
    * <p>It may be more appropriate to simply encode a url to the event.
    *
    * <p>Request parameters<ul>
    *      <li>"subid"    subscription id for event. < 0 if there is none
    *                     e.g. displayed directly from calendar.</li>
    *      <li>"calPath"  Path of calendar to search.</li>
    *      <li>"guid" | "eventName"    guid or name of event.</li>
    *      <li>"recurrenceId"   recurrence-id of event instance - possibly null.</li>
    * </ul>
    * <p>If the recurrenceId is null and the event is a recurring event we
    * should return the master event only,
    *
    * @param request   BwRequest for parameters
    * @param mode recurrence mode
    * @return EventInfo or null if not found
    * @throws Throwable
    */
   protected EventInfo findEvent(final BwRequest request,
                                 final Rmode mode) throws Throwable {
     Client cl = request.getClient();
     EventInfo ev = null;
 
     BwCalendar cal = request.getCalendar(true);
 
     if (cal == null) {
       return null;
     }
 
     String guid = request.getReqPar("guid");
     String eventName = request.getReqPar("eventName");
 
     if (guid != null) {
       if (debug) {
         debugMsg("Get event by guid");
       }
       String rid = request.getReqPar("recurrenceId");
       // DORECUR is this right?
       RecurringRetrievalMode rrm;
       if (mode == Rmode.overrides) {
         rrm = RecurringRetrievalMode.overrides;
         rid = null;
       } else if (mode == Rmode.expanded) {
         rrm = RecurringRetrievalMode.expanded;
       } else {
         rrm = new RecurringRetrievalMode(mode);
       }
 
       Collection<EventInfo> evs = cl.getEvent(cal.getPath(),
                                               guid, rid, rrm);
       if (debug) {
         debugMsg("Get event by guid found " + evs.size());
       }
       if (evs.size() == 1) {
         ev = evs.iterator().next();
      } else {
         // XXX this needs dealing with
         warn("Multiple result from getEvent");
       }
     } else if (eventName != null) {
       if (debug) {
         debugMsg("Get event by name");
       }
 
       ev = cl.getEvent(cal.getPath(), eventName,
                        RecurringRetrievalMode.overrides);
     }
 
     if (ev == null) {
       request.getForm().getErr().emit(ClientError.unknownEvent, /*eid*/guid);
       return null;
     } else if (debug) {
       debugMsg("Get event found " + ev.getEvent());
     }
 
     return ev;
   }
 
   protected BwLocation getLocation(final Client cl,
                                    final BwActionFormBase form,
                                    final String owner,
                                    final boolean webSubmit) throws Throwable {
     BwLocation loc = null;
 
     if (!webSubmit) {
       /* Check for user typing a new location into a text area.
        */
       String a = Util.checkNull(form.getLocationAddress().getValue());
       if (a != null) {
         // explicitly provided location overrides all others
         loc = BwLocation.makeLocation();
         BwString addr = new BwString(null, a);
         loc.setAddress(addr);
       }
     }
 
     /* No new location supplied - try to retrieve by uid
      */
     if (loc == null) {
       if (form.getLocationUid() != null) {
         loc = cl.getPersistentLocation(form.getLocationUid());
       }
     }
 
     if (loc != null) {
       loc.setLink(Util.checkNull(loc.getLink()));
       String ownerHref = owner;
 
       if (ownerHref == null) {
         ownerHref = cl.getCurrentPrincipalHref();
       }
 
       Client.CheckEntityResult<BwLocation> cer =
         cl.ensureLocationExists(loc, ownerHref);
 
       loc = cer.getEntity();
 
       if (cer.getAdded()) {
         form.getMsg().emit(ClientMessage.addedLocations, 1);
       }
     }
 
     return loc;
   }
 
   protected BwCalendar findCalendar(final BwRequest request,
                                     final String url) throws CalFacadeException {
     if (url == null) {
       return null;
     }
 
     return request.getClient().getCollection(url);
   }
 
   /** An image processed to produce a thumbnail and storeable resources
    *
    * @author douglm
    */
   public static class ProcessedImage {
     /** true for OK -otherwise an error has been emitted */
     public boolean OK;
 
     /** true for a possibly recoverable error - otherwise we rolled back and
      * should restart */
     public boolean retry;
 
     /** The file as uploaded */
     public BwResource image;
 
     /** Reduced to a thumbnail */
     public BwResource thumbnail;
   }
 
   /** Create resource entities based on the uploaded file.
    *
    * @param request
    * @param file - uploaded
    * @return never null.
    */
   protected ProcessedImage processImage(final BwRequest request,
                                         final FormFile file) {
     ProcessedImage pi = new ProcessedImage();
     Client cl = request.getClient();
 
     try {
       long maxSize = cl.getUserMaxEntitySize();
 
       if (file.getFileSize() > maxSize) {
         request.getErr().emit(ValidationError.tooLarge, file.getFileSize(), maxSize);
         pi.retry = true;
         return pi;
       }
 
       /* If the user has set a default images directory preference it must exist.
        * Otherwise we use a system default. For the moment we
        * try to create a folder called "Images"
        */
 
       BwCalendar imageCol = null;
 
       String imagecolPath = cl.getPreferences().getDefaultImageDirectory();
       if (imagecolPath == null) {
         BwCalendar home = cl.getHome();
 
         String imageColName = "Images";
 
         imagecolPath = Util.buildPath(false, home.getPath(), "/",
                                       imageColName);
 
 //        for (BwCalendar col: cl.getChildren(home)) {
 //          if (col.getName().equals(imageColName)) {
 //            imageCol = col;
 //            break;
 //          }
 //        }
 
         imageCol = cl.getCollection(imagecolPath);
 
         if (imageCol == null) {
           imageCol = new BwCalendar();
 
           imageCol.setSummary(imageColName);
           imageCol.setName(imageColName);
           imageCol = cl.addCollection(imageCol, home.getPath());
         }
       } else {
         imageCol = cl.getCollection(imagecolPath);
         if (imageCol == null) {
           request.getErr().emit(ClientError.missingImageDirectory);
           return pi;
         }
       }
 
       String fn = file.getFileName();
 
       /* See if the resource exists already */
 
       boolean replace = false;
       boolean replaceThumb = false;
       BwResourceContent rc;
       String thumbType = "png";
 
       pi.image = cl.getResource(
               Util.buildPath(false, imageCol.getPath(), "/", fn));
 
       if (pi.image != null) {
         if (!request.getBooleanReqPar("replaceImage", false)) {
           request.getErr().emit(ClientError.duplicateImage);
           pi.retry = true;
           return pi;
         }
 
         replace = true;
         cl.getResourceContent(pi.image);
         rc = pi.image.getContent();
       } else {
         pi.image = new BwResource();
         pi.image.setName(fn);
 
         rc = new BwResourceContent();
         pi.image.setContent(rc);
       }
 
       byte[] fileData = file.getFileData();
       byte[] thumbContent;
 
       try {
         thumbContent = ImageProcessing.createThumbnail(
                new ByteArrayInputStream(fileData),
                thumbType, 80);
       } catch (Throwable t) {
         /* Probably an image type we can't process or maybe not an image at all
          */
         if (debug) {
           error(t);
         }
 
         request.getErr().emit(ClientError.imageError);
         pi.retry = true;
         return pi;
       }
 
       rc.setContent(fileData);
 
       pi.image.setContentLength(fileData.length);
       pi.image.setContentType(file.getContentType());
 
       /* Make a thumbnail - first name */
 
       String thumbFn = makeThumbName(fn, thumbType);
 
       BwResourceContent thumbRc;
 
       pi.thumbnail = cl.getResource(
               Util.buildPath(false, imageCol.getPath(), "/", thumbFn));
 
       if (pi.thumbnail != null) {
         replaceThumb = true;
         cl.getResourceContent(pi.thumbnail);
         thumbRc = pi.thumbnail.getContent();
       } else {
         pi.thumbnail = new BwResource();
         pi.thumbnail.setName(thumbFn);
 
         thumbRc = new BwResourceContent();
         pi.thumbnail.setContent(thumbRc);
       }
 
       pi.thumbnail.setContentType("image/" + thumbType);
       thumbRc.setContent(thumbContent);
       pi.thumbnail.setContentLength(thumbContent.length);
 
       if (!replace) {
         cl.saveResource(imageCol.getPath(), pi.image);
       } else {
         cl.updateResource(pi.image, true);
       }
 
       if (!replaceThumb) {
         cl.saveResource(imageCol.getPath(), pi.thumbnail);
       } else {
         cl.updateResource(pi.thumbnail, true);
       }
 
       pi.OK = true;
     } catch (Throwable t) {
       if (debug) {
         error(t);
         request.getErr().emit(t);
       }
     }
 
     return pi;
   }
 
   private String makeThumbName(final String imageName,
                                final String thumbType) {
     int dotPos = imageName.lastIndexOf('.');
     String thumbFn;
 
     if (dotPos < 0) {
       thumbFn = imageName + "-thumb";
     } else {
       thumbFn = imageName.substring(0, dotPos) + "-thumb";
     }
 
     return thumbFn + "." + thumbType;
   }
 
   @Override
   protected boolean logOutCleanup(final HttpServletRequest request,
                                   final UtilActionForm form) {
     HttpSession hsess = request.getSession();
     BwCallback cb = (BwCallback)hsess.getAttribute(BwCallback.cbAttrName);
 
     if (cb == null) {
       if (form.getDebug()) {
         debugMsg("No cb object for logout");
       }
     } else {
       if (form.getDebug()) {
         debugMsg("cb object found for logout");
       }
       try {
         cb.out(request);
       } catch (Throwable t) {}
 
       try {
         cb.close(request, true);
       } catch (Throwable t) {}
     }
 
     return true;
   }
 
   /** Check for logout request. Overridden so we can close anything we
    * need to before the session is invalidated.
    *
    * @param request    HttpServletRequest
    * @return null for continue, forwardLoggedOut to end session.
    * @throws Throwable
    */
   protected String checkLogOut(final Request request)
           throws Throwable {
     String temp = request.getReqPar(requestLogout);
     if (temp != null) {
       HttpSession sess = request.getRequest().getSession(false);
 
       if (sess != null) {
         /* I don't think I need this - we didn't come through the svci filter on the
            way in?
         UWCalCallback cb = (UWCalCallback)sess.getAttribute(UWCalCallback.cbAttrName);
 
         try {
           if (cb != null) {
             cb.out();
           }
         } catch (Throwable t) {
           getLogger().error("Callback exception: ", t);
           /* Probably no point in throwing it. We're leaving anyway. * /
         } finally {
           if (cb != null) {
             try {
               cb.close();
             } catch (Throwable t) {
               getLogger().error("Callback exception: ", t);
               /* Probably no point in throwing it. We're leaving anyway. * /
             }
           }
         }
         */
 
         sess.invalidate();
       }
       return forwardLoggedOut;
     }
 
     return null;
   }
 
   /* ********************************************************************
                              view methods
      ******************************************************************** */
 
   /** Set the current date and/or view. The date may be null indicating we
    * should switch to a new view based on the current date.
    *
    * <p>newViewTypeI may be less than 0 indicating we stay with the current
    * view but switch to a new date.
    *
    * @param request       action form
    * @param date         String yyyymmdd date or null
    * @param newViewTypeI new view index or -1
    * @throws Throwable
    */
   protected void gotoDateView(final BwRequest request,
                               final String date,
                               int newViewTypeI) throws Throwable {
     final BwModuleState mstate = request.getModule().getState();
 
     //BwActionFormBase form = request.getBwForm();
     /* We get a new view if either the date changed or the view changed.
      */
     boolean newView = false;
 
     if (debug) {
       debugMsg("ViewTypeI=" + newViewTypeI);
     }
 
     MyCalendarVO dt;
 
     if (newViewTypeI == BedeworkDefs.todayView) {
       final Date jdt = new Date(System.currentTimeMillis());
       dt = new MyCalendarVO(jdt);
       newView = true;
       newViewTypeI = BedeworkDefs.dayView;
     } else if (date == null) {
       if (newViewTypeI == BedeworkDefs.dayView) {
         // selected specific day to display from personal event entry screen.
 
         final Date jdt = BwDateTimeUtil.getDate(mstate.getViewStartDate().getDateTime());
         dt = new MyCalendarVO(jdt);
         newView = true;
       } else {
         if (debug) {
           debugMsg("No date supplied: go with current date");
         }
 
         // Just stay here
         dt = mstate.getViewMcDate();
         if (dt == null) {
           // Just in case
           dt = new MyCalendarVO(new Date(System.currentTimeMillis()));
         }
       }
     } else {
       if (debug) {
         debugMsg("Date=" + date + ": go with that");
       }
 
       Date jdt = DateTimeUtil.fromISODate(date);
       dt = new MyCalendarVO(jdt);
       if (!checkDateInRange(request, dt.getYear())) {
         // Set it to today
         jdt = new Date(System.currentTimeMillis());
         dt = new MyCalendarVO(jdt);
       }
       newView = true;
     }
 
     if ((newViewTypeI >= 0) &&
         (newViewTypeI != mstate.getCurTimeView().getViewPeriod())) {
       // Change of view
       newView = true;
     }
 
     if (newView && (newViewTypeI < 0)) {
       newViewTypeI = mstate.getCurViewPeriod();
       if (newViewTypeI < 0) {
         newViewTypeI = BedeworkDefs.defaultView;
       }
     }
 
     final TimeDateComponents viewStart = mstate.getViewStartDate();
 
     if (!newView) {
       /* See if we were given an explicit date as view start date components.
          If so we'll set a new view of the same period as the current.
        */
       final int year = viewStart.getYear();
 
       if (checkDateInRange(request, year)) {
         final String vsdate = viewStart.getDateTime().getDtval().substring(0, 8);
         if (debug) {
           debugMsg("vsdate=" + vsdate);
         }
 
         if (!(vsdate.equals(request.getSess().getCurTimeView(request).getFirstDayFmt().getDateDigits()))) {
           newView = true;
           newViewTypeI = mstate.getCurViewPeriod();
           final Date jdt = DateTimeUtil.fromISODate(vsdate);
           dt = new MyCalendarVO(jdt);
         }
       }
     }
 
     if (newView) {
       mstate.setCurViewPeriod(newViewTypeI);
       mstate.setViewMcDate(dt);
       mstate.setRefresh(true);
       request.getClient().clearSearch();
     }
 
     final TimeView tv = request.getSess().getCurTimeView(request);
 
     /** Set first day, month and year
      */
 
     final Calendar firstDay = tv.getFirstDay();
 
     viewStart.setDay(firstDay.get(Calendar.DATE));
     viewStart.setMonth(firstDay.get(Calendar.MONTH) + 1);
     viewStart.setYear(firstDay.get(Calendar.YEAR));
 
     //form.getEventStartDate().setDateTime(tv.getCurDayFmt().getDateTimeString());
     //form.getEventEndDate().setDateTime(tv.getCurDayFmt().getDateTimeString());
   }
 
   /** Set the current date for view.
    *
    * @param request
    * @param date         String yyyymmdd date
    * @throws Throwable
    */
   protected void setViewDate(final BwRequest request,
                              final String date) throws Throwable {
     BwModuleState mstate = request.getModule().getState();
     Date jdt = DateTimeUtil.fromISODate(date);
     MyCalendarVO dt = new MyCalendarVO(jdt);
 
     if (debug) {
       debugMsg("calvo dt = " + dt);
     }
 
     if (!checkDateInRange(request, dt.getYear())) {
       // Set it to today
       jdt = new Date(System.currentTimeMillis());
       dt = new MyCalendarVO(jdt);
     }
     mstate.setViewMcDate(dt);
     mstate.setRefresh(true);
   }
 
   /* ********************************************************************
                              private methods
      ******************************************************************** */
 
   private boolean checkDateInRange(final BwRequest req,
                                    final int year) throws Throwable {
     BwActionFormBase form = req.getBwForm();
 
     // XXX make system parameters for allowable start/end year
     int thisYear = form.getToday().getFormatted().getYear();
 
     if ((year < (thisYear - 50)) || (year > (thisYear + 50))) {
       form.getErr().emit(ValidationError.invalidDate, year);
       return false;
     }
 
     return true;
   }
 
   /** Get the session state object for a web session. If we've already been
    * here it's embedded in the current session. Otherwise create a new one.
    *
    * <p>We also carry out a number of web related operations.
    *
    * @param request       HttpServletRequest Needed to locate session
    * @param form          Action form
    * @param messages      MessageResources needed for the resources
    * @param adminUserId   id we want to administer
    * @return BwSession null on failure
    * @throws Throwable
    */
   private synchronized BwSession getState(final Request request,
                                           final BwActionFormBase form,
                                           final MessageResources messages,
                                           final String adminUserId) throws Throwable {
     BwSession s = BwWebUtil.getState(request.getRequest());
     HttpSession sess = request.getRequest().getSession(false);
     String appName = getAppName(sess);
 
     if (s != null) {
       if (debug) {
         debugMsg("getState-- obtainedfrom session");
         debugMsg("getState-- timeout interval = " +
                  sess.getMaxInactiveInterval());
       }
 
       form.assignNewSession(false);
     } else {
       if (debug) {
         debugMsg("getState-- get new object");
       }
 
       form.assignNewSession(true);
 
       s = new BwSessionImpl(form.getConfig(),
                             form.getCurrentUser(),
                             appName);
 
       BwWebUtil.setState(request.getRequest(), s);
 
       String raddr = request.getRemoteAddr();
       String rhost = request.getRemoteHost();
       info("===============" + appName + ": New session (" +
                    s.getSessionNum() + ") from " +
                    rhost + "(" + raddr + ")");
 
       if (!form.getConfig().getPublicAdmin()) {
         /** Ensure the session timeout interval is longer than our refresh period
          */
         //  Should come from db -- int refInt = s.getRefreshInterval();
         int refInt = 60; // 1 min refresh?
 
         if (refInt > 0) {
           int timeout = sess.getMaxInactiveInterval();
 
           if (timeout <= refInt) {
             // An extra minute should do it.
             debugMsg("@+@+@+@+@+ set timeout to " + (refInt + 60));
             sess.setMaxInactiveInterval(refInt + 60);
           }
         }
       }
     }
 
     /** Ensure we have a CalSvcI object
      */
     checkSvci(request, s, adminUserId, false);
 
     return s;
   }
 
   private String getAppName(final HttpSession sess) {
     ServletContext sc = sess.getServletContext();
 
     String appname = sc.getInitParameter(appNameInitParameter);
     if (appname == null) {
       appname = "?";
     }
 
     return appname;
   }
 
   /** Ensure we have a CalAdminSvcI object for the given user.
    *
    * <p>For an admin client with a super user we may switch to a different
    * user to administer their events.
    *
    * @param request       for pars
    * @param user          String user we want to be
    * @param canSwitch     true if we should definitely allow user to switch
    *                      this allows a user to switch between and into
    *                      groups of which they are a member
    * @return boolean      false for problems.
    * @throws Throwable
    */
   boolean checkSvci(final Request request,
                     final BwSession sess,
                     final String user,
                     boolean canSwitch) throws Throwable {
     BwActionFormBase form = (BwActionFormBase)request.getForm();
     boolean publicAdmin = form.getConfig().getPublicAdmin();
     boolean guestMode = form.getConfig().getGuestMode();
     String calSuiteName = null;
 
 
     BwCallback cb = getCb(request, form);
     BwModule module = form.fetchModule(request.getModuleName());
     BwModuleState mstate = module.getState();
 
 //    Client client = BwWebUtil.getClient(request.getRequest());
     Client client = module.getClient();
 
     if (guestMode) {
       // A guest user using the public clients. Get the calendar suite from the
       // configuration
       calSuiteName = form.getConfig().getCalSuite();
     } else if (publicAdmin & (client != null)) {
       /* Calendar suite we are administering is the one we find attached to a
        * group as we proceed up the tree
        */
 
       /* Note that we redo this once we have a group set. The first call
          (before we have any client) has no group name set in the form
        */
       BwCalSuiteWrapper cs = AdminUtil.findCalSuite(request,
                                                     client);
       form.setCurrentCalSuite(cs);
 
       if (cs != null) {
         calSuiteName = cs.getName();
       }
 
       if (debug) {
         if (cs != null) {
           debugOut("Found calSuite " + cs);
         } else {
           debugOut("No calsuite found");
         }
       }
     } else {
       /* !publicAdmin: We're never allowed to switch identity as a user client.
        */
       if (!user.equals(form.getCurrentUser())) {
         return false;
       }
     }
 
     boolean reinitClient = false;
 
     try {
       /** Make some checks to see if this is an old - restarted session.
         If so discard the svc interface
        */
       if (client != null) {
         /* Not the first time through here so for a public admin client we
          * already have the authorised user's rights set in the form.
          */
 
         /* if (!client.isOpen()) {
           //svci.flushAll();
           reinitClient = true;
           info("Client interface discarded from old session");
           ((Callback)cb).closeNow(); // So we're not waiting for ourself
         } else */
         if (publicAdmin) {
           BwPrincipal pr = client.getCurrentPrincipal();
           if (pr == null) {
             throw new CalFacadeException("Null user for public admin.");
           }
 
           canSwitch = canSwitch || form.getCurUserContentAdminUser() ||
                   form.getCurUserSuperUser();
 
           String curUser = pr.getAccount();
 
           if (!user.equals(curUser)) {
             if (!canSwitch) {
               /** Trying to switch but not allowed */
               return false;
             }
 
             /** Switching user */
             client.endTransaction();
             client.close();
             reinitClient = true;
             sess.reset(request);
             cb.close(request.getRequest(), true); // So we're not waiting for ourself
           }
         }
 
         /* Already there and already opened */
         if (debug) {
           debugMsg("Client interface -- Obtained from session for user " +
                            client.getCurrentPrincipalHref());
         }
 
         if (reinitClient) {
           if (debug) {
             debugMsg("Client-- reinit for user " + user);
           }
 
           form.flushModules(request.getModuleName());
 
           if (guestMode) {
             ((ROClientImpl)client).reinit(form.getCurrentUser(),
                                           user,
                                           calSuiteName,
                                           form.getAppType(),
                                           true);
           } else if (publicAdmin) {
             ((AdminClientImpl)client).reinit(form.getCurrentUser(),
                                              user,
                                              calSuiteName,
                                              (AdminConfig)form.getConfig());
           } else {
             ((ClientImpl)client).reinit(form.getCurrentUser(),
                                         user,
                                         form.getAppType());
           }
 
           client.requestIn(request.getConversationType());
           mstate.setRefresh(true);
         }
       } else {
         if (debug) {
           debugMsg("Client-- getResource new object for user " + user);
         }
 
         if (guestMode) {
           client = new ROClientImpl(module.getModuleName(),
                                     form.getCurrentUser(),
                                     user,
                                     calSuiteName,
                                     form.getAppType(),
                                     true);
         } else if (publicAdmin) {
           client = new AdminClientImpl(module.getModuleName(),
                                        form.getCurrentUser(),
                                        user,
                                        calSuiteName,
                                        (AdminConfig)form.getConfig());
         } else {
           client = new ClientImpl(module.getModuleName(),
                                   form.getCurrentUser(),
                                   user,
                                   form.getAppType());
         }
 
         module.setClient(client);
         module.setRequest(request);
 
         module.requestIn();
         mstate.setRefresh(true);
         sess.reset(request);
       }
 
       if (client.getPublicAdmin()) {
         BwPrincipal pr = client.getCurrentPrincipal();
         form.assignCurrentAdminUser(pr.getAccount());
       }
     } catch (CalFacadeException cfe) {
       throw cfe;
     } catch (Throwable t) {
       throw new CalFacadeException(t);
     }
 
     return true;
   }
 
   private BwCallback getCb(final Request request,
                            final BwActionFormBase form) throws Throwable {
     HttpSession hsess = request.getRequest().getSession();
     BwCallback cb = (BwCallback)hsess.getAttribute(BwCallback.cbAttrName);
     if (cb == null) {
       /* create a call back object so the filter can open the service
       interface */
 
       cb = new BwCallbackImpl(form, request.getMapping());
       hsess.setAttribute(BwCallback.cbAttrName, cb);
     }
 
     if (debug) {
       debugMsg("checkSvci-- set req in cb - form action path = " +
                        request.getActionPath() +
                        " conv-type = " + request.getConversationType());
     }
 
     return cb;
   }
 
   /** Check for request parameter setting a module variable
    * We expect the request parameter to be of the form<br/>
    * setmvar=name(value) or <br/>
    * setmvar=name{value}<p>.
    *  Currently we're not escaping characters so if you want both right
    *  terminators in the value you're out of luck - actually we cheat a bit
    *  We just look at the last char and then look for that from the start.
    *
    * @param request  Needed to locate session
    * @return true if no error found.
    * @throws Throwable
    */
   private boolean checkMvarReq(final BwRequest request) throws Throwable {
     Collection<String> mvs = request.getReqPars("setmvar");
     if (mvs == null) {
       return true;
     }
 
     BwModuleState state = request.getModule().getState();
 
     for (String reqpar: mvs) {
       int start;
 
       if (reqpar.endsWith("}")) {
         start = reqpar.indexOf('{');
       } else if (reqpar.endsWith(")")) {
         start = reqpar.indexOf('(');
       } else {
         return false;
       }
 
       if (start < 0) {
         return false;
       }
 
       String varName = reqpar.substring(0, start);
       String varVal = reqpar.substring(start + 1, reqpar.length() - 1);
 
       if (varVal.length() == 0) {
         varVal = null;
       }
 
       if (!state.setVar(varName, varVal)) {
         return false;
       }
     }
 
     return true;
   }
 
   private String suffixRoot(final Request req,
                             final String val) throws Throwable {
     BwActionFormBase form = (BwActionFormBase)req.getForm();
 
     StringBuilder sb = new StringBuilder(val);
 
     /* If we're running as a portlet change the app root to point to a
      * portlet specific directory.
      */
     String portalPlatform = form.getConfig().getPortalPlatform();
 
     if (isPortlet && (portalPlatform != null)) {
       sb.append(".");
       sb.append(portalPlatform);
     }
 
     /* If calendar suite is non-null append that. */
     String calSuite = form.getConfig().getCalSuite();
     if (calSuite != null) {
       sb.append(".");
       sb.append(calSuite);
     }
 
     return sb.toString();
   }
 
   /** Return the value or a default if it's invalid
    *
    * @param val
    * @return String valid view period
    */
   public String validViewPeriod(String val) {
     int vt = BedeworkDefs.defaultView;
 
     val = Util.checkNull(val);
     if (val != null) {
       Integer i = viewTypeMap.get(val);
 
       if (i != null) {
         vt = i;
       }
     }
 
     return BedeworkDefs.viewPeriodNames[vt];
   }
 }
