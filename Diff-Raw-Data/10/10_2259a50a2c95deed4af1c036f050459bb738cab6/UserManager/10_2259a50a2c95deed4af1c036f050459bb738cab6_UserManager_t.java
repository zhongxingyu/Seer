 /*
  * Copyright 2006-2007, AVANE sarl, and individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *
  * @author jvdrean
  */
 package com.xpn.xwiki.plugin.chronopolys;
 
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.cache.api.XWikiCache;
 import com.xpn.xwiki.cache.api.XWikiCacheNeedsRefreshException;
 import com.xpn.xwiki.cache.api.XWikiCacheService;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.api.Object;
 
 import java.util.*;
import org.apache.commons.lang.StringUtils;
 
 public class UserManager
 {
     // Deadline types
   
     public static final int DEADLINE_MILESTONE = 1;
     
     public static final int DEADLINE_PHASE = 0;
 
     public static final int DEADLINE_TASK = 2;
 
     public static final int DEADLINE_MEETING = 3;
     
     public static final int DEADLINE_PROJECT = 4;
 
     public static final int usersCacheCapacity = 100;
 
     public static final int userdataCacheCapacity = 100;
 
     private ChronopolysPlugin plugin;
 
     public UserManager(ChronopolysPlugin plugin)
     {
         this.plugin = plugin;
     }
 
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     // Caches
 
     protected XWikiCache usersCache;
 
     protected XWikiCache userdataCache;
 
     /*
      * Flush users cache (contains xwiki users)
      */
     public void flushUsersCache()
     {
         if (usersCache != null) {
             usersCache.flushAll();
             usersCache = null;
         }
     }
 
     /*
      * Flush user's datas cache (contains user's tasks, meetings and deadlines map)
      */
     public void flushUserdataCache()
     {
         if (userdataCache != null) {
             userdataCache.flushAll();
             userdataCache = null;
         }
     }
 
     /*
      * Init user datas cache
      */
     private void initUserdataCache(XWikiContext context) throws XWikiException
     {
         if (userdataCache == null) {
             XWikiCacheService cacheService = context.getWiki().getCacheService();
             userdataCache =
                 cacheService.newCache("xwiki.chronopolys.userdata.cache", userdataCacheCapacity);
         }
     }
 
     /*
      * Get all the wiki users
      */
     public List getXWikiUsers(XWikiContext context) throws XWikiException
     {
         List list = null;
         String key = context.getDatabase();
 
         // init cache if necessary
         if (usersCache == null) {
             XWikiCacheService cacheService = context.getWiki().getCacheService();
             usersCache = cacheService.newCache("xwiki.chronopolys.users.cache", usersCacheCapacity);
         }
         synchronized (key) {
             try {
                 // retreive users list from cache
                 list = (List) usersCache.getFromCache(key);
             } catch (XWikiCacheNeedsRefreshException e) {
                 // update the users list cache
                 usersCache.cancelUpdate(key);
                 String hql = ", BaseObject as obj where obj.name=doc.fullName"
                     + " and obj.className='XWiki.XWikiUsers'";
                 list = context.getWiki().getStore().searchDocumentsNames(hql, context);
                 usersCache.putInCache(key, list);
             }
         }
 
         return list;
     }
 
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     // User utilities
 
     /*
      * Set a new password to the given user and send him an email with the new credentials
      */
 
     public boolean resetPassword(String user, XWikiContext context)
     {
         if (context.getDoc().getFullName() != "XWiki.XWikiLogin") {
             try {
                 if (!context.getWiki().exists("XWiki." + user, context)) {
                     return false;
                 }
                 String password = context.getWiki().generateRandomString(6);
                 String email = this.getUserEmail(user, context);
                 ArrayList rcpt = new ArrayList();
                 rcpt.add(email);
                 XWikiDocument userdoc = context.getWiki().getDocument(user, context);
                 userdoc.setStringValue("XWiki.XWikiUsers", "password", password);
                 context.getWiki().saveDocument(userdoc, context);
                 context.getWiki().sendConfirmationEmail(userdoc.getName(), password, email,
                     "message", "invitation_email_content", context);
                 context.put("email", email);
                 return true;
             } catch (XWikiException e) {
                 return false;
             }
         } else {
             return false;
         }
     }
 
     /*
      * Get user's email from his profile
      */
     public String getUserEmail(String user, XWikiContext context) throws XWikiException
     {
         return context.getWiki().getDocument(user, context).getStringValue("email");
     }
 
     /*
      * Get user's language from his profile
      */
     public String getUserLanguage(String user, XWikiContext context) throws XWikiException
     {
    	String userLanguage = context.getWiki().getDocument(user, context).getStringValue("chronolanguage");    	
    	if (StringUtils.isBlank(userLanguage)) {
    		return context.getWiki().getLanguagePreference(context);
    	}
    	
        return userLanguage;
     }
 
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     // Rights
 
     /*
      * Is the current user a chronopolys manager ?
      */
 
     public boolean isManager(XWikiContext context) throws XWikiException
     {
         return context.getWiki().getUser(context.getLocalUser(), context)
             .isUserInGroup("ChronoAdmin.ManagerGroup") ||
             context.getWiki().getUser(context.getLocalUser(), context)
                 .isUserInGroup(context.getDatabase() + ":ChronoAdmin.ManagerGroup") ||
             context.getWiki().getRightService().hasAdminRights(context);
     }
 
     /*
     * Is the current user a chronopolys administrator ?
     */
     public boolean isAdmin(XWikiContext context) throws XWikiException
     {
         return context.getWiki().getUser(context.getLocalUser(), context)
             .isUserInGroup("ChronoAdmin.AdminGroup") ||
             context.getWiki().getUser(context.getLocalUser(), context)
                 .isUserInGroup(context.getDatabase() + ":ChronoAdmin.AdminGroup") ||
             context.getWiki().getRightService().hasAdminRights(context);
     }
 
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     // User personnal datas
 
     /*
      * Get user's next deadlines (meetings, tasks, project's ends, phase's ends)
      */
 
     public List getMyLastModifications(int limit, XWikiContext context) throws XWikiException
     {
         return plugin.getUtils().intelliSubList(limit, 0, this.getMyLastModifications(context));
     }
     
     public List getMyLastModifications(int limit, int start, XWikiContext context) throws XWikiException
     {
         return plugin.getUtils().intelliSubList(limit, start, this.getMyLastModifications(context));
     }
 
     /*
      * Get last modifications in user's projects
      */
     public List getMyLastModifications(XWikiContext context) throws XWikiException
     {
         TreeSet<Modification> modifications = new TreeSet<Modification>();
         ArrayList<Modification> list = new ArrayList<Modification>();
         List projects = plugin.getProjectManager().getMyProjects(context);
 
         for (Iterator it = projects.iterator(); it.hasNext();) {
             String name = (String) it.next();
             List mods = plugin.getProjectManager().getProject(name, context)
                 .getLastModifications();
             modifications.addAll(mods);
         }
 
         list.addAll(modifications);
         return list;
     }
     /* public List getMyLastModifications(XWikiContext context) throws XWikiException {
         String key = context.getDatabase() + ':' + context.getLocalUser() + "-lastmodifications";
         ArrayList<Modification> list = null;
 
         initUserdataCache(context);
 
         synchronized (key) {
             try {
                 list = (ArrayList<Modification>) userdataCache.getFromCache(key);
             } catch (XWikiCacheNeedsRefreshException e) {
                 userdataCache.cancelUpdate(key);
 
                 TreeSet<Modification> modifications = new TreeSet<Modification>();
                 list = new ArrayList<Modification>();
 
                 // projects
                 List projects = plugin.getProjectManager().getMyProjects(context);
                 for (Iterator it = projects.iterator(); it.hasNext();) {
                     String page = (String) it.next();
                     XWikiDocument doc =
                         context.getWiki()
                             .getDocument(page + "." + Project.PROJECT_HOMEDOC, context);
                     List mods =
                         plugin.getProjectManager().getProject(doc.getSpace(), context)
                             .getLastModifications(context);
                     for (Iterator i = mods.iterator(); i.hasNext();) {
                         String modName = (String) i.next();
                         XWikiDocument modDoc = context.getWiki().getDocument(modName, context);
                         Date date = modDoc.getDate();
                         modifications.add(new Modification(modDoc.getFullName(), modDoc.getComment()
                                 , date));
                     }
                 }
                 list.addAll(modifications);
                 userdataCache.putInCache(key, list);
             }
         }
         return list;
 
     }*/
 
     /*
     * Get user's next deadlines (meetings, tasks, project's ends, phase's ends)
     */
 
     public List getMyNextDeadlines(int limit, XWikiContext context) throws XWikiException
     {
         return plugin.getUtils().intelliSubList(limit, 0, this.getMyNextDeadlines(context));
     }
     
     public List getMyNextDeadlines(int limit, int start, XWikiContext context) throws XWikiException
     {
         return plugin.getUtils().intelliSubList(limit, start, this.getMyNextDeadlines(context));
     }
 
     /*
     * Get user's next deadlines (meetings, tasks, project's ends, phase's ends)
     */
     public List getMyNextDeadlines(XWikiContext context) throws XWikiException
     {
         String key = context.getDatabase() + ':' + context.getLocalUser() + "-deadlines";
         ArrayList<Deadline> list = null;
         Date today = new Date();
 
         initUserdataCache(context);
 
         synchronized (key) {
             try {
                 list = (ArrayList<Deadline>) userdataCache.getFromCache(key);
             } catch (XWikiCacheNeedsRefreshException e) {
                 userdataCache.cancelUpdate(key);
                 TreeSet<Deadline> deadlines = new TreeSet<Deadline>();
                 list = new ArrayList<Deadline>();
 
                 // meetings
                 List meetings = this.getMyMeetings(context);
                 for (Iterator it = meetings.iterator(); it.hasNext();) {
                     String page = (String) it.next();
                     XWikiDocument doc = context.getWiki().getDocument(page, context);
                     Date date = (Date) doc.newDocument(context).getValue("meetingend");
                     deadlines.add(new Deadline(doc.getFullName(), doc.getDisplayTitle(context),
                         DEADLINE_MEETING, date));
                 }
 
                 // tasks
                 List tasks = this.getMyTasks(context);
                 for (Iterator it = tasks.iterator(); it.hasNext();) {
                     String page = (String) it.next();
                     XWikiDocument doc = context.getWiki().getDocument(page, context);
                     Date date = (Date) doc.newDocument(context).getValue("taskduedate");
                     deadlines.add(new Deadline(doc.getFullName(), doc.getDisplayTitle(context),
                         DEADLINE_TASK, date));
                 }
 
                 // projects
                 List projects = plugin.getProjectManager().getMyProjects(context);
                 for (Iterator it = projects.iterator(); it.hasNext();) {
                     String page = (String) it.next();
                     XWikiDocument doc =
                         context.getWiki()
                             .getDocument(page + "." + Project.PROJECT_HOMEDOC, context);
                     /* Date date = (Date) doc.newDocument(context).getValue("end");
                     if (date.after(today)) {
                         deadlines.add(new Deadline(doc.getFullName(),
                             doc.display("name", "view", context), DEADLINE_PROJECT, date));
                     } */
 
                     // Phases
                     List phases =
                         plugin.getProjectManager().getProject(doc.getSpace(), context).getPhases();
                     for (Iterator i = phases.iterator(); i.hasNext();) {
                         Object phase = (Object) i.next();
                         Date date = (Date) phase.getProperty("end").getValue();
                         if (date.after(today)) {
                             int type = Integer.parseInt((String)phase.getProperty("type").getValue());
                             deadlines.add(new Deadline(doc.getFullName(),
                                 phase.display("name", "view").toString(),
                                 type, date));
                         }
                     }
                 }
                 list.addAll(deadlines);
                 userdataCache.putInCache(key, list);
             }
         }
         return list;
     }
 
     /*
     * Get number of projects the user receive notification's from
     */
     public int getMyProjectSubscriptionsNb
         (XWikiContext
             context) throws XWikiException
     {
         return this.getMyProjectSubscriptions(context).size();
     }
 
     /*
     * Get projects the user receive notification's from
     */
     public List getMyProjectSubscriptions
         (XWikiContext
             context) throws XWikiException
     {
         initUserdataCache(context);
         List list = null;
         String key = context.getDatabase() + ':' + context.getLocalUser() + "-subscriptions";
         synchronized (key) {
             try {
                 list = (List) userdataCache.getFromCache(key);
             } catch (XWikiCacheNeedsRefreshException e) {
                 userdataCache.cancelUpdate(key);
                 String hql =
                     "select pdoc.fullName from XWikiDocument doc, XWikiDocument pdoc, BaseObject obj, StringProperty prop, " +
                         "BaseObject pobj, StringProperty pprop where doc.fullName=obj.name and doc.name='ProjectMembers' " +
                         "and obj.className='ChronoClasses.NotificationsClass' and obj.id=prop.id.id and prop.id.name='member' " +
                         "and prop.value='" + context.getLocalUser() +
                         "' and pdoc.web=doc.web and pdoc.name='WebHome' " +
                         "and pdoc.fullName=pobj.name and pobj.className='ChronoClasses.ProjectClass' " +
                         "and pobj.id=pprop.id.id and pprop.id.name='status' and pprop.value='1'";
                 list = context.getWiki().search(hql, context);
                 userdataCache.putInCache(key, list);
             }
         }
         return list;
     }
 
     /*
     * Get the number of task for the current user
     */
     public int getMyTasksNb
         (XWikiContext
             context) throws XWikiException
     {
         return this.getMyTasks(context).size();
     }
 
     /*
     * Get the current user's opened tasks (with index limits)
     */
     public List getMyTasks
         (
             int limit,
             int start, XWikiContext
             context) throws XWikiException
     {
         List list = this.getMyTasks(context);
         return plugin.getUtils().intelliSubList(limit, start, list);
     }
 
     /*
     * Get all the current user's opened tasks
     */
     public List getMyTasks(XWikiContext context) throws XWikiException
     {
         initUserdataCache(context);
         List list = null;
         String key = context.getDatabase() + ':' + context.getLocalUser() + "-tasks";
         synchronized (key) {
             try {
                 list = (List) userdataCache.getFromCache(key);
             } catch (XWikiCacheNeedsRefreshException e) {
                 userdataCache.cancelUpdate(key);
                 String hql =
                     "select distinct doc.fullName, tdate.value from XWikiDocument doc, BaseObject obj, StringProperty prop, StringProperty list," +
                         " DateProperty tdate, StringProperty completion where doc.fullName=obj.name " +
                         "and obj.className='ChronoClasses.ProjectArticleClass' and obj.id=prop.id.id and obj.id=list.id.id " +
                         "and obj.id=tdate.id.id and obj.id=completion.id.id and prop.id.name='taskassignee' " +
                         "and prop.value='" + context.getLocalUser() +
                         "' and list.id.name='type' and list.value='task' " +
                         "and tdate.id.name='taskduedate' and completion.id.name='taskcompletion' and completion.value != '100%' order by tdate.value";
                 // workaround for HSQLDB distinct/order limitation
                 List tmplist = context.getWiki().search(hql, context);
                 list = new ArrayList();
                 for (int i = 0; i < tmplist.size(); i++) {
                     String task = (String) ((java.lang.Object[]) tmplist.get(i))[0];
                     if (task != null) {
                         list.add(task);
                     }
                 }
                 userdataCache.putInCache(key, list);
             }
         }
         return list;
     }
 
     /*
     * Get number of next meetings for the current user
     */
     public int getMyMeetingsNb
         (XWikiContext
             context) throws XWikiException
     {
         return this.getMyMeetings(context).size();
     }
 
     /*
     * Get forthcoming meetings for the current user (with limits)
     */
     public List getMyMeetings
         (
             int limit,
             int start, XWikiContext
             context) throws XWikiException
     {
         List list = this.getMyMeetings(context);
         return plugin.getUtils().intelliSubList(limit, start, list);
     }
 
     /*
     * Get next meetings for the current user
     */
     public List getMyMeetings(XWikiContext context) throws XWikiException
     {
         initUserdataCache(context);
         List list = null;
         String key = context.getDatabase() + ':' + context.getLocalUser() + "-meetings";
         synchronized (key) {
             try {
                 list = (List) userdataCache.getFromCache(key);
             } catch (XWikiCacheNeedsRefreshException e) {
                 userdataCache.cancelUpdate(key);
                 String hql =
                     "select distinct doc.fullName, mdate.value from XWikiDocument doc, BaseObject obj, BaseObject robj, StringProperty rprop, " +
                         "StringProperty list, DateProperty mdate, StringProperty status where doc.fullName=obj.name " +
                         "and obj.className='ChronoClasses.ProjectArticleClass' and doc.fullName=robj.name " +
                         "and robj.className='ChronoClasses.ProjectArticleRsvpClass' and robj.id=rprop.id.id " +
                         "and robj.id=status.id.id and obj.id=list.id.id and obj.id=mdate.id.id and rprop.id.name='member' " +
                         "and rprop.value='" + context.getLocalUser() +
                         "' and status.id.name='status' and status.value!='no' " +
                         "and list.id.name='type' and list.value='meeting' and mdate.id.name='meetingstart' " +
                         "and mdate.value>=current_date order by mdate.value";
                 // workaround for HSQLDB distinct/order limitation
                 List tmplist = context.getWiki().search(hql, context);
                 list = new ArrayList();
                 for (int i = 0; i < tmplist.size(); i++) {
                     String meeting = (String) ((java.lang.Object[]) tmplist.get(i))[0];
                     if (meeting != null) {
                         list.add(meeting);
                     }
                 }
                 userdataCache.putInCache(key, list);
             }
         }
         return list;
     }
 
     /*
     * Get the number of projects the current user is member of
     */
     public int getMyProjectsNb
         (XWikiContext
             context) throws XWikiException
     {
         return plugin.getProjectManager().getMyProjects(context).size();
     }
 
     /*
     * Get projects the current user is member of (with limits)
     */
     public List getMyProjects
         (
             int limit,
             int start, XWikiContext
             context) throws XWikiException
     {
         List list = plugin.getProjectManager().getMyProjects(context);
         return plugin.getUtils().intelliSubList(limit, start, list);
     }
 
     /*
     * Set the context's language from the user chronopolys preference (dirty fix which works well)
     */
     public void setLanguage(String user, XWikiContext context)
     {
         try {
             String clang = context.getRequest().get("lang");
             if (clang != null && !"".equals(clang)) {
                 context.setLanguage(clang);
                 XWikiDocument docuser = context.getWiki().getDocument(user, context);
                 docuser.setStringValue("XWiki.XWikiUsers", "chronolanguage", clang);
                 docuser.setComment("editlanguage");
                 context.getWiki().saveDocument(docuser, context);
             } else {
                 XWikiDocument docuser = context.getWiki().getDocument(user, context);
                 String language = (String) docuser.newDocument(context).getValue("chronolanguage");
                 if (language != null && !language.equals("")) {
                     context.setLanguage(language);
                 } else {
                     language = context.getWiki().getXWikiPreference("default_language", context);
                     if (language != null && !language.equals("")) {
                         context.setLanguage(language);
                     }
                 }
             }
         } catch (XWikiException e) {
             context.setLanguage("en");
         }
     }
 }
