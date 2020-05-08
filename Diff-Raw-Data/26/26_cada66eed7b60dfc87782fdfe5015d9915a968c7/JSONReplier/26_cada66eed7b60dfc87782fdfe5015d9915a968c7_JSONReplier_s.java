 /*
  *
  */
 
 package com.luntsys.luntbuild.repliers;
 
 import com.luntsys.luntbuild.builders.*;
 import com.luntsys.luntbuild.db.*;
 import com.luntsys.luntbuild.facades.lb12.ScheduleFacade;
 import com.luntsys.luntbuild.facades.Constants;
 import com.luntsys.luntbuild.facades.SearchCriteria;
 import com.luntsys.luntbuild.notifiers.*;
 import com.luntsys.luntbuild.utility.Luntbuild;
 import com.luntsys.luntbuild.utility.NotifierProperty;
 import com.luntsys.luntbuild.vcs.*;
 
 import org.springframework.dao.DataAccessException;
 
 import java.util.*;
 
 /**
  * JSON API replier implementation.
  *
  * @author Jason Archer
  */
 public class JSONReplier extends Replier {
 
     protected String callback = "";
 
     /**
      * Constructs a new JSON replier.
      */
     public JSONReplier() {
     }
 
     /**
      * Gets the display name for this replier.
      *
      * @return the display name for this replier
      */
     public String getDisplayName() {
         return "JSON";
     }
 
     /** 
      * Sets the callback function to send the reply to.
      * 
      * @param callback the callback function name
      */
     public void setCallback(String callback) {
         this.callback = callback;
     }
 
     /**
      * Gets the full reply based on the chosen method.
      * 
      * @return the full reply message
      */
     public String getReply() {
         if (source == null) {
             return null;
         }
 
         String reply = "";
         String header = "";
         String body = "";
         String footer = "";
         int results = 0;
         String[] sources = source.split("/");
         
         if ((callback != null) && !callback.equals("")) {
             header = callback + "(";
             footer = ")";
         }
 
         body = "{";
         body += "\"system\":";
         body += getSystem() + ",";
 
         if (method == Replier.ROOT || method == Replier.PROJECTS) {
             body += "\"results\":[";
 
             if (source.equals("")) {
                 // Get all projects
                 Iterator projects = Luntbuild.getDao().loadProjects().iterator();
                 while (projects.hasNext()) {
                     Project project = (Project) projects.next();
                     project = Luntbuild.getDao().loadProject(project.getId());
                     body += getProject(project) + ",";
                     results++;
                 }
                 body += "],";
                 body += "\"totalResults\":" + results + ",";
             } else {
                 body += "],";
                 body += "\"totalResults\":0,";
                 body += "\"error\":\"" + escape("Invalid source \"" + source + "\" for \"projects\"") + "\",";
             }
         } else if (method == Replier.SCHEDULES) {
             body += "\"results\":[";
 
             if (source.equals("")) {
                 // Get all schedules
                 Iterator schedules = Luntbuild.getDao().loadSchedules().iterator();
                 while (schedules.hasNext()) {
                     Schedule schedule = (Schedule) schedules.next();
                     body += getSchedule(schedule) + ",";
                     results++;
                 }
                 body += "],";
                 body += "\"totalResults\":" + results + ",";
             } else if (!source.equals("") && sources.length == 1) {
                 try {
                     // Get all schedules for this project
                     Project project = Luntbuild.getDao().loadProject(sources[0]);
                     Iterator schedules = project.getSchedules().iterator();
                     while (schedules.hasNext()) {
                         Schedule schedule = (Schedule) schedules.next();
                         body += getSchedule(schedule) + ",";
                         results++;
                     }
                     body += "],";
                 } catch (DataAccessException e) {
                     body += "],";
                     body += "\"error\":\"" + escape("Project not found") + "\",";
                 }
                 body += "\"totalResults\":" + results + ",";
             } else {
                 body += "],";
                 body += "\"totalResults\":0,";
                 body += "\"error\":\"" + escape("Invalid source \"" + source + "\" for \"schedules\"") + "\",";
             }
         } else if (method == Replier.BUILDS) {
             body += "\"results\":[";
 
             if (source.equals("")) {
                 // Get all builds
                 Iterator schedules = Luntbuild.getDao().loadSchedules().iterator();
                 while (schedules.hasNext()) {
                     Schedule schedule = (Schedule) schedules.next();
                     SearchCriteria searchCriteria = new SearchCriteria();
                     searchCriteria.setScheduleIds(new long[]{schedule.getId()});
                     Iterator builds = Luntbuild.getDao().searchBuilds(searchCriteria, 0, 0).iterator();
                     while (builds.hasNext()) {
                         Build build = (Build) builds.next();
                         body += getBuild(build) + ",";
                         results++;
                     }
                 }
                 body += "],";
                 body += "\"totalResults\":" + results + ",";
             } else if (!source.equals("") && sources.length == 1) {
                 try {
                     // Get all builds for this project
                     Project project = Luntbuild.getDao().loadProject(sources[0]);
                     Iterator schedules = project.getSchedules().iterator();
                     while (schedules.hasNext()) {
                         Schedule schedule = (Schedule) schedules.next();
                         SearchCriteria searchCriteria = new SearchCriteria();
                         searchCriteria.setScheduleIds(new long[]{schedule.getId()});
                         Iterator builds = Luntbuild.getDao().searchBuilds(searchCriteria, 0, 0).iterator();
                         while (builds.hasNext()) {
                             Build build = (Build) builds.next();
                             body += getBuild(build) + ",";
                             results++;
                         }
                     }
                     body += "],";
                 } catch (DataAccessException e) {
                     body += "],";
                     body += "\"error\":\"" + escape("Project not found") + "\",";
                 }
                 body += "\"totalResults\":" + results + ",";
             } else if (sources.length == 2) {
                 try {
                     // Get all builds for this schedule
                     Schedule schedule = Luntbuild.getDao().loadSchedule(sources[0], sources[1]);
                     SearchCriteria searchCriteria = new SearchCriteria();
                     searchCriteria.setScheduleIds(new long[]{schedule.getId()});
                     Iterator builds = Luntbuild.getDao().searchBuilds(searchCriteria, 0, 0).iterator();
                     while (builds.hasNext()) {
                         Build build = (Build) builds.next();
                         body += getBuild(build) + ",";
                         results++;
                     }
                     body += "],";
                 } catch (DataAccessException e) {
                     body += "],";
                     body += "\"error\":\"" + escape("Schedule not found") + "\",";
                 }
                 body += "\"totalResults\":" + results + ",";
             } else {
                 body += "],";
                 body += "\"totalResults\":0,";
                 body += "\"error\":\"" + escape("Invalid source \"" + source + "\" for \"builds\"") + "\",";
             }
         } else if (method == Replier.BUILD) {
             body += "\"results\":[";
 
             if (sources.length == 3) {
                 try {
                     // Get build
                     Build build = Luntbuild.getDao().loadBuild(sources[0], sources[1], sources[2]);
                     body += getBuild(build);
                     results = 1;
                     body += "],";
                 } catch (DataAccessException e) {
                     body += "],";
                     body += "\"error\":\"" + escape("Build not found") + "\",";
                 }
                 body += "\"totalResults\":" + results + ",";
             } else {
                 body += "],";
                 body += "\"totalResults\":0,";
                 body += "\"error\":\"" + escape("Invalid source \"" + source + "\" for \"build\"") + "\",";
             }
         } else if (method == Replier.USERS) {
             body += "\"results\":[";
 
             // Get all users
             Iterator users = Luntbuild.getDao().loadUsers().iterator();
             while (users.hasNext()) {
                 User user = (User) users.next();
                 body += getUser(user) + ",";
                 results++;
             }
             body += "],";
             body += "\"totalResults\":" + results + ",";
         } else if (method == Replier.USER) {
             body += "\"results\":[";
 
             if (!source.equals("") && sources.length == 1) {
                 try {
                     // Get user
                     User user = Luntbuild.getDao().loadUser(sources[0]);
                     body += getUser(user);
                     results = 1;
                     body += "],";
                 } catch (DataAccessException e) {
                     body += "],";
                     body += "\"error\":\"" + escape("User \"" + sources[0] + "\" not found") + "\",";
                 }
                 body += "\"totalResults\":" + results + ",";
             } else {
                 body += "],";
                 body += "\"totalResults\":0,";
                 body += "\"error\":\"" + escape("Invalid source \"" + source + "\" for \"user\"") + "\",";
             }
         } else {
             body += "\"error\":\"" + escape("Unsupported method") + "\",";
         }
         body += "}";
         body = body.replaceAll(",]","]");
         body = body.replaceAll(",}","}");
         body = body.replaceAll("\\{\\}","null");
 
         reply = header + body + footer;
 
         return reply;
     }
 
     /**
      * Escapes special characters in JSON text.
      * 
      * @param text the JSON text
      * @return the properly escaped JSON text
      */
     protected String escape(String text) {
         if (text == null) text = "";
         text = text.replaceAll("\\\\","\\\\");
         text = text.replaceAll("/","\\\\/");
         text = text.replaceAll("\"","\\\\\\\"");
 
         // Return escaped text
         return text;
     }
 
     /**
      * Converts a text area to a JSON string array.  Each line in the text area will be a separate element.
      * 
      * @param text the text area
      * @return the JSON string array
      */
     protected String toArray(String text) {
         if (text == null) text = "";
 
         String array = "";
         array += "[";
         String[] lines = text.split("\r*\n");
         for (int i = 0; i < lines.length; i++) {
             if (!Luntbuild.isEmpty(lines[i]))
                 array += "\"" + escape(lines[i]) + "\",";
         }
         array += "]";
         array = array.replaceAll(",]","]");
 
         // Return escaped text
         return array;
     }
 
     /**
      * Gets the "system" object containing system wide settings and users.
      * 
      * @return the system object
      */
     public String getSystem() {
         String JSONText = "";
         JSONText += "{";
        JSONText += "\"servletUrl\":\"" + escape((String) Luntbuild.getProperties().get(Constants.SERVLET_URL)) + "\",";
         JSONText += "\"workingDir\":\"" + escape((String) Luntbuild.getProperties().get(Constants.WORKING_DIR)) + "\",";
         JSONText += "\"publishDir\":\"" + escape((String) Luntbuild.getProperties().get(Constants.PUBLISH_DIR)) + "\",";
         JSONText += "\"pageRefreshInterval\":" + ((String) Luntbuild.getProperties().get(Constants.PAGE_REFRESH_INTERVAL)) + ",";
         JSONText += "\"buildThreadCount\":" + ((String) Luntbuild.getProperties().get(Constants.BUILD_THREAD_COUNT)) + ",";
         JSONText += "\"emailProperties\":[";
         Iterator emailprops = (new EmailNotifier()).getSystemLevelProperties().iterator();
         while (emailprops.hasNext()) {
             NotifierProperty property = (NotifierProperty) emailprops.next();
             JSONText += getNotifierProperty(property) + ",";
         }
         JSONText += "],";
         JSONText += "\"msnProperties\":[";
         Iterator msnprops = (new MsnNotifier()).getSystemLevelProperties().iterator();
         while (msnprops.hasNext()) {
             NotifierProperty property = (NotifierProperty) msnprops.next();
             JSONText += getNotifierProperty(property) + ",";
         }
         JSONText += "],";
         JSONText += "\"jabberProperties\":[";
         Iterator jabberprops = (new JabberNotifier()).getSystemLevelProperties().iterator();
         while (jabberprops.hasNext()) {
             NotifierProperty property = (NotifierProperty) jabberprops.next();
             JSONText += getNotifierProperty(property) + ",";
         }
         JSONText += "],";
         JSONText += "\"sametimeProperties\":[";
         Iterator sametimeprops = (new SametimeNotifier()).getSystemLevelProperties().iterator();
         while (sametimeprops.hasNext()) {
             NotifierProperty property = (NotifierProperty) sametimeprops.next();
             JSONText += getNotifierProperty(property) + ",";
         }
         JSONText += "],";
         JSONText += "\"users\":[";
         Iterator users = Luntbuild.getDao().loadUsers().iterator();
         while (users.hasNext()) {
             User user = (User) users.next();
             JSONText += getUser(user) + ",";
         }
         JSONText += "],";
 
         JSONText += "}";
 
         // Return JSON text
         return JSONText;
     }
 
     /**
      * Gets the property object of the specified system level property.
      * 
      * @param property the property
      * @return the property object
      */
     public String getNotifierProperty(NotifierProperty property) {
         String JSONText = "";
         JSONText += "{";
         JSONText += "\"key\":\"" + escape(property.getKey()) + "\",";
         JSONText += "\"value\":\"" + escape(property.getValue(Luntbuild.getProperties())) + "\",";
         JSONText += "\"displayName\":\"" + escape(property.getDisplayName()) + "\",";
         JSONText += "\"description\":\"" + escape(property.getDescription()) + "\",";
         JSONText += "\"notifierClass\":\"" + escape(property.getNotifierClass().getName()) + "\",";
 
         JSONText += "}";
 
         // Return JSON text
         return JSONText;
     }
 
     /**
      * Gets the property object of the specified user level property.
      * 
      * @param property the property
      * @param user the user
      * @return the property object
      */
     public String getNotifierProperty(NotifierProperty property, User user) {
         String JSONText = "";
         JSONText += "{";
         JSONText += "\"key\":\"" + escape(property.getKey()) + "\",";
         JSONText += "\"value\":\"" + escape(property.getValue(user.getContacts())) + "\",";
 
         JSONText += "}";
 
         // Return JSON text
         return JSONText;
     }
 
     /**
      * Gets the user object of the specified user.
      * 
      * @param user the user
      * @return the user object
      */
     public String getUser(User user) {
         return getUser(user, true);
     }
 
     /**
      * Gets the minimal or full user object of the specified user.
      * 
      * @param user the user
      * @param full set <code>false</code> for only minimal details
      * @return the user object
      */
     public String getUser(User user, boolean full) {
         String JSONText = "";
         JSONText += "{";
         JSONText += "\"id\":" + user.getId() + ",";
         JSONText += "\"name\":\"" + escape(user.getName()) + "\",";
         if (full) {
             JSONText += "\"fullname\":\"" + escape(user.getFullname()) + "\",";
             JSONText += "\"canCreateProject\":" + user.isCanCreateProject() + ",";
 
             // Hash table
             JSONText += "\"contacts\":[";
             Iterator emailprops = (new EmailNotifier()).getUserLevelProperties().iterator();
             while (emailprops.hasNext()) {
                 NotifierProperty property = (NotifierProperty) emailprops.next();
                 JSONText += getNotifierProperty(property, user) + ",";
             }
             Iterator msnprops = (new MsnNotifier()).getUserLevelProperties().iterator();
             while (msnprops.hasNext()) {
                 NotifierProperty property = (NotifierProperty) msnprops.next();
                 JSONText += getNotifierProperty(property, user) + ",";
             }
             Iterator jabberprops = (new JabberNotifier()).getUserLevelProperties().iterator();
             while (jabberprops.hasNext()) {
                 NotifierProperty property = (NotifierProperty) jabberprops.next();
                 JSONText += getNotifierProperty(property, user) + ",";
             }
             Iterator sametimeprops = (new SametimeNotifier()).getUserLevelProperties().iterator();
             while (sametimeprops.hasNext()) {
                 NotifierProperty property = (NotifierProperty) sametimeprops.next();
                 JSONText += getNotifierProperty(property, user) + ",";
             }
             JSONText += "],";
         }
 
         JSONText += "}";
 
         // Return JSON text
         return JSONText;
     }
 
     /**
      * Gets the project object of the specified project.
      * 
      * @param project the project
      * @return the project object
      */
     public String getProject(Project project) {
         String JSONText = "";
         JSONText += "{";
         JSONText += "\"id\":" + project.getId() + ",";
         JSONText += "\"name\":\"" + escape(project.getName()) + "\",";
         JSONText += "\"description\":\"" + escape(project.getDescription()) + "\",";
         JSONText += "\"variables\":" + toArray(project.getVariables()) + ",";
         JSONText += "\"logLevel\":\"" + escape(Constants.getLogLevelText(project.getLogLevel())) + "\",";
 
         // Lists
         JSONText += "\"projectAdmins\":[";
         Iterator adminlist = project.getMappedRolesUserList(Role.LUNTBUILD_PRJ_ADMIN).iterator();
         while (adminlist.hasNext()) {
             User user = (User) adminlist.next();
             JSONText += getUser(user, false) + ",";
         }
         JSONText += "],";
         JSONText += "\"projectBuilders\":[";
         Iterator builderslist = project.getMappedRolesUserList(Role.LUNTBUILD_PRJ_BUILDER).iterator();
         while (builderslist.hasNext()) {
             User user = (User) builderslist.next();
             JSONText += getUser(user, false) + ",";
         }
         JSONText += "],";
         JSONText += "\"projectViewers\":[";
         Iterator viewerlist = project.getMappedRolesUserList(Role.LUNTBUILD_PRJ_VIEWER).iterator();
         while (viewerlist.hasNext()) {
             User user = (User) viewerlist.next();
             JSONText += getUser(user, false) + ",";
         }
         JSONText += "],";
         JSONText += "\"notifyUsers\":[";
         Iterator notifylist = project.getNotifyMappings().iterator();
         while (notifylist.hasNext()) {
             User user = ((NotifyMapping) notifylist.next()).getUser();
             JSONText += getUser(user, false) + ",";
         }
         JSONText += "],";
         JSONText += "\"vcss\":[";
         Iterator vcslist = project.getVcsList().iterator();
         while (vcslist.hasNext()) {
             Vcs vcs = (Vcs) vcslist.next();
             JSONText += getVcs(vcs) + ",";
         }
         JSONText += "],";
         JSONText += "\"builders\":[";
         Iterator builderlist = project.getBuilderList().iterator();
         while (builderlist.hasNext()) {
             Builder builder = (Builder) builderlist.next();
             JSONText += getBuilder(builder) + ",";
         }
         JSONText += "],";
         JSONText += "\"notifiers\":[";
         Iterator notifierlist = Luntbuild.getNotifierInstances(Luntbuild.getNotifierClasses(project.getNotifiers())).iterator();
         while (notifierlist.hasNext()) {
             JSONText += "\"" + ((Notifier) notifierlist.next()).getDisplayName() + "\",";
         }
         JSONText += "],";
         JSONText += "\"schedules\":[";
         Iterator schedules = project.getSchedules().iterator();
         while (schedules.hasNext()) {
             Schedule schedule = (Schedule) schedules.next();
             JSONText += getSchedule(schedule) + ",";
         }
         JSONText += "],";
 
         JSONText += "}";
 
         // Return JSON text
         return JSONText;
     }
 
     /**
      * Gets the vcs object of the specified vcs.
      * 
      * @param vcs the vcs
      * @return the vcs object
      */
     public String getVcs(Vcs vcs) {
         String JSONText = "";
         JSONText += "{";
         JSONText += "\"class\":\"" + escape(vcs.getClass().getName()) + "\",";
         JSONText += "\"displayName\":\"" + escape(vcs.getDisplayName()) + "\",";
         JSONText += "\"quietPeriod\":\"" + escape(vcs.getQuietPeriod()) + "\",";
         if (vcs.getClass() == (new AccurevAdaptor()).getClass()) {
             AccurevAdaptor accurev = (AccurevAdaptor) vcs;
             JSONText += "\"user\":\"" + escape(accurev.getUser()) + "\",";
             JSONText += "\"password\":\"*****\",";
             JSONText += "\"modules\":[";
             Iterator modules = accurev.getModules().iterator();
             while (modules.hasNext()) {
                 AccurevAdaptor.AccurevModule accurevmodule = (AccurevAdaptor.AccurevModule) modules.next();
                 JSONText += "{";
                 JSONText += "\"depot\":\"" + escape(accurevmodule.getDepot()) + "\",";
                 JSONText += "\"srcPath\":\"" + escape(accurevmodule.getSrcPath()) + "\",";
                 JSONText += "\"backingStream\":\"" + escape(accurevmodule.getBackingStream()) + "\",";
                 JSONText += "\"buildStream\":\"" + escape(accurevmodule.getBuildStream()) + "\",";
                 JSONText += "\"label\":\"" + escape(accurevmodule.getLabel()) + "\",";
                 JSONText += "},";
             }
             JSONText += "],";
         } else if (vcs.getClass() == (new BaseClearcaseAdaptor()).getClass()) {
             BaseClearcaseAdaptor base = (BaseClearcaseAdaptor) vcs;
             JSONText += "\"viewTag\":\"" + escape(base.getViewTag()) + "\",";
             JSONText += "\"viewStgLoc\":\"" + escape(base.getViewStgLoc()) + "\",";
             JSONText += "\"vws\":\"" + escape(base.getVws()) + "\",";
             JSONText += "\"viewCfgSpec\":\"" + escape(base.getViewCfgSpec()) + "\",";
             JSONText += "\"modificationDetectionConfig\":\"" + escape(base.getModificationDetectionConfig()) + "\",";
             JSONText += "\"mkviewExtraOpts\":\"" + escape(base.getMkviewExtraOpts()) + "\",";
             JSONText += "\"cleartoolDir\":\"" + escape(base.getCleartoolDir()) + "\",";
             JSONText += "\"formatParams\":\"" + escape(base.getFormatParams()) + "\",";
         } else if (vcs.getClass() == (new CvsAdaptor()).getClass()) {
             CvsAdaptor cvs = (CvsAdaptor) vcs;
             JSONText += "\"cvsRoot\":\"" + escape(cvs.getCvsRoot()) + "\",";
             JSONText += "\"cvsPassword\":\"*****\",";
             JSONText += "\"cvsDir\":\"" + escape(cvs.getCvsDir()) + "\",";
             JSONText += "\"cygwinCvs\":" + cvs.isCygwinCvs() + ",";
             JSONText += "\"disableSuppressOption\":" + cvs.isDisableSuppressOption() + ",";
             JSONText += "\"disableHistoryCmd\":" + cvs.isDisableHistoryCmd() + ",";
             JSONText += "\"modules\":[";
             Iterator modules = cvs.getModules().iterator();
             while (modules.hasNext()) {
                 CvsAdaptor.CvsModule cvsmodule = (CvsAdaptor.CvsModule) modules.next();
                 JSONText += "{";
                 JSONText += "\"srcPath\":\"" + escape(cvsmodule.getSrcPath()) + "\",";
                 JSONText += "\"branch\":\"" + escape(cvsmodule.getBranch()) + "\",";
                 JSONText += "\"label\":\"" + escape(cvsmodule.getLabel()) + "\",";
                 JSONText += "},";
             }
             JSONText += "],";
         } else if (vcs.getClass() == (new DynamicClearcaseAdaptor()).getClass()) {
             DynamicClearcaseAdaptor dynamic = (DynamicClearcaseAdaptor) vcs;
             JSONText += "\"viewTag\":\"" + escape(dynamic.getViewTag()) + "\",";
             JSONText += "\"viewStgLoc\":\"" + escape(dynamic.getViewStgLoc()) + "\",";
             JSONText += "\"vws\":\"" + escape(dynamic.getVws()) + "\",";
             JSONText += "\"viewCfgSpec\":\"" + escape(dynamic.getViewCfgSpec()) + "\",";
             JSONText += "\"modificationDetectionConfig\":\"" + escape(dynamic.getModificationDetectionConfig()) + "\",";
             JSONText += "\"mkviewExtraOpts\":\"" + escape(dynamic.getMkviewExtraOpts()) + "\",";
             JSONText += "\"cleartoolDir\":\"" + escape(dynamic.getCleartoolDir()) + "\",";
             JSONText += "\"formatParams\":\"" + escape(dynamic.getFormatParams()) + "\",";
             JSONText += "\"mvfsPath\":\"" + escape(dynamic.getMvfsPath()) + "\",";
             JSONText += "\"projectPath\":\"" + escape(dynamic.getProjectPath()) + "\",";
         } else if (vcs.getClass() == (new FileSystemAdaptor()).getClass()) {
             FileSystemAdaptor file = (FileSystemAdaptor) vcs;
             JSONText += "\"sourceDir\":\"" + escape(file.getSourceDir()) + "\",";
         } else if (vcs.getClass() == (new MksAdaptor()).getClass()) {
             MksAdaptor mks = (MksAdaptor) vcs;
             JSONText += "\"defaultHostname\":\"" + escape(mks.getDefaultHostname()) + "\",";
             JSONText += "\"defaultPort\":" + mks.getDefaultPort() + ",";
             JSONText += "\"defaultUsername\":\"" + escape(mks.getDefaultUsername()) + "\",";
             JSONText += "\"defaultPassword\":\"*****\",";
             JSONText += "\"rootProject\":\"" + escape(mks.getRootProject()) + "\",";
             JSONText += "\"modules\":[";
             Iterator modules = mks.getModules().iterator();
             while (modules.hasNext()) {
                 MksAdaptor.MksModule mksmodule = (MksAdaptor.MksModule) modules.next();
                 JSONText += "{";
                 JSONText += "\"subproject\":\"" + escape(mksmodule.getSubproject()) + "\",";
                 JSONText += "\"version\":\"" + escape(mksmodule.getVersion()) + "\",";
                 JSONText += "\"projectFileName\":\"" + escape(mksmodule.getProjectFileName()) + "\",";
                 JSONText += "\"developmentPath\":\"" + escape(mksmodule.getDevelopmentPath()) + "\",";
                 JSONText += "\"external\":" + mksmodule.isExternal() + ",";
                 JSONText += "},";
             }
             JSONText += "],";
         } else if (vcs.getClass() == (new PerforceAdaptor()).getClass()) {
             PerforceAdaptor p4 = (PerforceAdaptor) vcs;
             JSONText += "\"p4Port\":\"" + escape(p4.getPort()) + "\",";
             JSONText += "\"p4User\":\"" + escape(p4.getUser()) + "\",";
             JSONText += "\"p4Password\":\"*****\",";
             JSONText += "\"lineEnd\":\"" + escape(p4.getLineEnd()) + "\",";
             JSONText += "\"p4Dir\":\"" + escape(p4.getP4Dir()) + "\",";
             JSONText += "\"changelist\":\"" + escape(p4.getChangelist()) + "\",";
             JSONText += "\"modules\":[";
             Iterator modules = p4.getModules().iterator();
             while (modules.hasNext()) {
                 PerforceAdaptor.PerforceModule p4module = (PerforceAdaptor.PerforceModule) modules.next();
                 JSONText += "{";
                 JSONText += "\"depotPath\":\"" + escape(p4module.getDepotPath()) + "\",";
                 JSONText += "\"label\":\"" + escape(p4module.getLabel()) + "\",";
                 JSONText += "\"clientPath\":\"" + escape(p4module.getClientPath()) + "\",";
                 JSONText += "},";
             }
             JSONText += "],";
         } else if (vcs.getClass() == (new StarteamAdaptor()).getClass()) {
             StarteamAdaptor star = (StarteamAdaptor) vcs;
             JSONText += "\"projectLocation\":\"" + escape(star.getProjectLocation()) + "\",";
             JSONText += "\"user\":\"" + escape(star.getUser()) + "\",";
             JSONText += "\"password\":\"*****\",";
             JSONText += "\"convertEOL\":\"" + escape(star.getConvertEOL()) + "\",";
             JSONText += "\"modules\":[";
             Iterator modules = star.getModules().iterator();
             while (modules.hasNext()) {
                 StarteamAdaptor.StarteamModule starmodule = (StarteamAdaptor.StarteamModule) modules.next();
                 JSONText += "{";
                 JSONText += "\"starteamView\":\"" + escape(starmodule.getStarteamView()) + "\",";
                 JSONText += "\"starteamPromotionState\":\"" + escape(starmodule.getStarteamPromotionState()) + "\",";
                 JSONText += "\"srcPath\":\"" + escape(starmodule.getSrcPath()) + "\",";
                 JSONText += "\"label\":\"" + escape(starmodule.getLabel()) + "\",";
                 JSONText += "\"destPath\":\"" + escape(starmodule.getDestPath()) + "\",";
                 JSONText += "},";
             }
             JSONText += "],";
         } else if (vcs.getClass() == (new SvnAdaptor()).getClass()) {
             SvnAdaptor svn = (SvnAdaptor) vcs;
             JSONText += "\"urlBase\":\"" + escape(svn.getUrlBase()) + "\",";
             JSONText += "\"trunk\":\"" + escape(svn.getTrunk()) + "\",";
             JSONText += "\"branches\":\"" + escape(svn.getBranches()) + "\",";
             JSONText += "\"tags\":\"" + escape(svn.getTags()) + "\",";
             JSONText += "\"user\":\"" + escape(svn.getUser()) + "\",";
             JSONText += "\"password\":\"*****\",";
             JSONText += "\"modules\":[";
             Iterator modules = svn.getModules().iterator();
             while (modules.hasNext()) {
                 SvnAdaptor.SvnModule svnmodule = (SvnAdaptor.SvnModule) modules.next();
                 JSONText += "{";
                 JSONText += "\"srcPath\":\"" + escape(svnmodule.getSrcPath()) + "\",";
                 JSONText += "\"branch\":\"" + escape(svnmodule.getBranch()) + "\",";
                 JSONText += "\"label\":\"" + escape(svnmodule.getLabel()) + "\",";
                 JSONText += "\"destPath\":\"" + escape(svnmodule.getDestPath()) + "\",";
                 JSONText += "},";
             }
             JSONText += "],";
         } else if (vcs.getClass() == (new SvnExeAdaptor()).getClass()) {
             SvnExeAdaptor svn = (SvnExeAdaptor) vcs;
             JSONText += "\"urlBase\":\"" + escape(svn.getUrlBase()) + "\",";
             JSONText += "\"trunk\":\"" + escape(svn.getTrunk()) + "\",";
             JSONText += "\"branches\":\"" + escape(svn.getBranches()) + "\",";
             JSONText += "\"tags\":\"" + escape(svn.getTags()) + "\",";
             JSONText += "\"user\":\"" + escape(svn.getUser()) + "\",";
             JSONText += "\"password\":\"*****\",";
             JSONText += "\"svnDir\":\"" + escape(svn.getSvnDir()) + "\",";
             JSONText += "\"modules\":[";
             Iterator modules = svn.getModules().iterator();
             while (modules.hasNext()) {
                 SvnAdaptor.SvnModule svnmodule = (SvnAdaptor.SvnModule) modules.next();
                 JSONText += "{";
                 JSONText += "\"srcPath\":\"" + escape(svnmodule.getSrcPath()) + "\",";
                 JSONText += "\"branch\":\"" + escape(svnmodule.getBranch()) + "\",";
                 JSONText += "\"label\":\"" + escape(svnmodule.getLabel()) + "\",";
                 JSONText += "\"destPath\":\"" + escape(svnmodule.getDestPath()) + "\",";
                 JSONText += "},";
             }
             JSONText += "],";
         } else if (vcs.getClass() == (new UCMClearcaseAdaptor()).getClass()) {
             UCMClearcaseAdaptor ucm = (UCMClearcaseAdaptor) vcs;
             JSONText += "\"viewStgLoc\":\"" + escape(ucm.getViewStgLoc()) + "\",";
             JSONText += "\"projectVob\":\"" + escape(ucm.getProjectVob()) + "\",";
             JSONText += "\"vws\":\"" + escape(ucm.getVws()) + "\",";
             JSONText += "\"stream\":\"" + escape(ucm.getStream()) + "\",";
             JSONText += "\"whatToBuild\":\"" + escape(ucm.getWhatToBuild()) + "\",";
             JSONText += "\"modificationDetectionConfig\":\"" + escape(ucm.getModificationDetectionConfig()) + "\",";
             JSONText += "\"mkviewExtraOpts\":\"" + escape(ucm.getMkviewExtraOpts()) + "\",";
             JSONText += "\"cleartoolDir\":\"" + escape(ucm.getCleartoolDir()) + "\",";
         } else if (vcs.getClass() == (new VssAdaptor()).getClass()) {
             VssAdaptor vss = (VssAdaptor) vcs;
             JSONText += "\"vssPath\":\"" + escape(vss.getVssPath()) + "\",";
             JSONText += "\"vssUser\":\"" + escape(vss.getVssUser()) + "\",";
             JSONText += "\"vssPassword\":\"*****\",";
             JSONText += "\"vssDir\":\"" + escape(vss.getSsDir()) + "\",";
             JSONText += "\"dateTimeFormat\":\"" + escape(vss.getDateTimeFormat()) + "\",";
             JSONText += "\"modules\":[";
             Iterator modules = vss.getModules().iterator();
             while (modules.hasNext()) {
                 VssAdaptor.VssModule vssmodule = (VssAdaptor.VssModule) modules.next();
                 JSONText += "{";
                 JSONText += "\"srcPath\":\"" + escape(vssmodule.getSrcPath()) + "\",";
                 JSONText += "\"label\":\"" + escape(vssmodule.getLabel()) + "\",";
                 JSONText += "\"destPath\":\"" + escape(vssmodule.getDestPath()) + "\",";
                 JSONText += "},";
             }
             JSONText += "],";
         }
 
         JSONText += "}";
 
         // Return JSON text
         return JSONText;
     }
 
     /**
      * Gets the builder object of the specified builder.
      * 
      * @param builder the builder
      * @return the builder object
      */
     public String getBuilder(Builder builder) {
         String JSONText = "";
         JSONText += "{";
         JSONText += "\"class\":\"" + escape(builder.getClass().getName()) + "\",";
         JSONText += "\"displayName\":\"" + escape(builder.getDisplayName()) + "\",";
         JSONText += "\"name\":\"" + escape(builder.getName()) + "\",";
         JSONText += "\"environments\":" + toArray(builder.getEnvironments()) + ",";
         JSONText += "\"buildSuccessCondition\":\"" + escape(builder.getBuildSuccessCondition()) + "\",";
         if (builder.getClass() == (new AntBuilder()).getClass()) {
             AntBuilder ant = (AntBuilder) builder;
             JSONText += "\"command\":\"" + escape(ant.getCommand()) + "\",";
             JSONText += "\"buildScriptPath\":\"" + escape(ant.getBuildScriptPath()) + "\",";
             JSONText += "\"targets\":\"" + escape(ant.getTargets()) + "\",";
             JSONText += "\"buildProperties\":" + toArray(ant.getBuildProperties()) + ",";
         } else if (builder.getClass() == (new CommandBuilder()).getClass()) {
             CommandBuilder cmd = (CommandBuilder) builder;
             JSONText += "\"command\":\"" + escape(cmd.getCommand()) + "\",";
             JSONText += "\"dirToRunCmd\":\"" + escape(cmd.getDirToRunCmd()) + "\",";
             JSONText += "\"waitForFinish\":\"" + escape(cmd.getWaitForFinish()) + "\",";
         } else if (builder.getClass() == (new Maven2Builder()).getClass()) {
             Maven2Builder maven2 = (Maven2Builder) builder;
             JSONText += "\"command\":\"" + escape(maven2.getCommand()) + "\",";
             JSONText += "\"dirToRunMaven\":\"" + escape(maven2.getDirToRunMaven()) + "\",";
             JSONText += "\"goals\":\"" + escape(maven2.getGoals()) + "\",";
             JSONText += "\"buildProperties\":" + toArray(maven2.getBuildProperties()) + ",";
         } else if (builder.getClass() == (new MavenBuilder()).getClass()) {
             MavenBuilder maven = (MavenBuilder) builder;
             JSONText += "\"command\":\"" + escape(maven.getCommand()) + "\",";
             JSONText += "\"dirToRunMaven\":\"" + escape(maven.getDirToRunMaven()) + "\",";
             JSONText += "\"goals\":\"" + escape(maven.getGoals()) + "\",";
             JSONText += "\"buildProperties\":" + toArray(maven.getBuildProperties()) + ",";
         } else if (builder.getClass() == (new RakeBuilder()).getClass()) {
             RakeBuilder rake = (RakeBuilder) builder;
             JSONText += "\"command\":\"" + escape(rake.getCommand()) + "\",";
             JSONText += "\"buildScriptPath\":\"" + escape(rake.getBuildScriptPath()) + "\",";
             JSONText += "\"targets\":\"" + escape(rake.getTargets()) + "\",";
             JSONText += "\"buildProperties\":" + toArray(rake.getBuildProperties()) + ",";
         }
 
         JSONText += "}";
 
         // Return JSON text
         return JSONText;
     }
 
     /**
      * Gets the schedule object of the specified schedule.
      * 
      * @param schedule the schedule
      * @return the schedule object
      */
     public String getSchedule(Schedule schedule) {
         ScheduleFacade schedfacade = schedule.getFacade();
         String JSONText = "";
         JSONText += "{";
         JSONText += "\"id\":" + schedfacade.getId() + ",";
         JSONText += "\"name\":\"" + escape(schedfacade.getName()) + "\",";
         JSONText += "\"description\":\"" + escape(schedfacade.getDescription()) + "\",";
         JSONText += "\"url\":\"" + escape(schedfacade.getUrl()) + "\",";
         JSONText += "\"scheduleDisabled\":" + schedfacade.isScheduleDisabled() + ",";
         JSONText += "\"buildType\":\"" + escape(Constants.getBuildTypeText(schedfacade.getBuildType())) + "\",";
         JSONText += "\"labelStrategy\":\"" + escape(Constants.getLabelStrategyText(schedfacade.getLabelStrategy())) + "\",";
         JSONText += "\"notifyStrategy\":\"" + escape(Constants.getNotifyStrategyText(schedfacade.getNotifyStrategy())) + "\",";
         JSONText += "\"postbuildStrategy\":\"" + escape(Constants.getPostbuildStrategyText(schedfacade.getPostbuildStrategy())) + "\",";
         JSONText += "\"triggerDependencyStrategy\":\"" + escape(Constants.getTriggerDependencyStrategyText(schedfacade.getTriggerDependencyStrategy())) + "\",";
         JSONText += "\"status\":\"" + escape(Constants.getScheduleStatusText(schedfacade.getStatus())) + "\",";
         JSONText += "\"statusDate\":\"" + escape(Luntbuild.DATE_DISPLAY_FORMAT.format(schedfacade.getStatusDate())) + "\",";
         JSONText += "\"buildNecessaryCondition\":\"" + escape(schedfacade.getBuildNecessaryCondition()) + "\",";
         JSONText += "\"buildCleanupStrategy\":\"" + escape(Constants.getBuildCleanupStrategyText(schedfacade.getBuildCleanupStrategy())) + "\",";
         JSONText += "\"buildCleanupStrategyData\":\"" + escape(schedfacade.getBuildCleanupStrategyData()) + "\",";
         JSONText += "\"nextVersion\":\"" + escape(schedfacade.getNextVersion()) + "\",";
         JSONText += "\"workingPath\":\"" + escape(schedfacade.getWorkingPath()) + "\",";
         JSONText += "\"publishDir\":\"" + escape(schedule.getPublishDir()) + "\",";
 
         // Enumerated values
         if (schedfacade.getTriggerType() == Constants.TRIGGER_TYPE_CRON) {
             JSONText += "\"triggerType\":\"cron\",";
             JSONText += "\"trigger\":\"" + escape(schedfacade.getCronExpression()) + "\",";
         }
         else if (schedfacade.getTriggerType() == Constants.TRIGGER_TYPE_MANUAL) {
             JSONText += "\"triggerType\":\"manual\",";
             JSONText += "\"trigger\":\"\",";
         }
         else if (schedfacade.getTriggerType() == Constants.TRIGGER_TYPE_SIMPLE) {
             JSONText += "\"triggerType\":\"simple\",";
             JSONText += "\"trigger\":\"" + escape(Long.toString(schedfacade.getRepeatInterval())) + "\",";
         }
         else {
             JSONText += "\"triggerType\":\"" + schedfacade.getTriggerType() + "\",";
             JSONText += "\"trigger\":\"\",";
         }
 
         // Lists
         JSONText += "\"associatedBuilderNames\":[";
         Iterator buildernames = schedule.getAssociatedBuilderNames().iterator();
         while (buildernames.hasNext()) {
             JSONText += "\"" + ((String) buildernames.next()) + "\",";
         }
         JSONText += "],";
         JSONText += "\"associatedPostbuilderNames\":[";
         Iterator postbuildernames = schedule.getAssociatedPostbuilderNames().iterator();
         while (postbuildernames.hasNext()) {
             JSONText += "\"" + ((String) postbuildernames.next()) + "\",";
         }
         JSONText += "],";
         JSONText += "\"dependentScheduleIds\":[";
         Iterator scheduleids = schedule.getDependentScheduleIds().iterator();
         while (scheduleids.hasNext()) {
             JSONText += ((String) scheduleids.next()) + ",";
         }
         JSONText += "],";
         JSONText += "\"builds\":[";
         SearchCriteria searchCriteria = new SearchCriteria();
         searchCriteria.setScheduleIds(new long[]{schedule.getId()});
         Iterator builds = Luntbuild.getDao().searchBuilds(searchCriteria, 0, 0).iterator();
         while (builds.hasNext()) {
             Build build = (Build) builds.next();
             JSONText += getBuild(build) + ",";
         }
         JSONText += "],";
 
         JSONText += "}";
 
         // Return JSON text
         return JSONText;
     }
 
     /**
      * Gets the build object of the specified build.
      * 
      * @param build the build
      * @return the build object
      */
     public String getBuild(Build build) {
         String JSONText = "";
         JSONText += "{";
         JSONText += "\"id\":" + build.getId() + ",";
         JSONText += "\"version\":\"" + escape(build.getVersion()) + "\",";
         JSONText += "\"status\":\"" + escape(Constants.getBuildStatusText(build.getStatus())) + "\",";
         JSONText += "\"startDate\":\"" + escape(Luntbuild.DATE_DISPLAY_FORMAT.format(build.getStartDate())) + "\",";
         JSONText += "\"endDate\":\"" + escape(Luntbuild.DATE_DISPLAY_FORMAT.format(build.getEndDate())) + "\",";
         JSONText += "\"haveLabelOnHead\":" + build.isHaveLabelOnHead() + ",";
         JSONText += "\"buildType\":\"" + escape(Constants.getBuildTypeText(build.getBuildType())) + "\",";
         JSONText += "\"rebuild\":" + build.isRebuild() + ",";
         JSONText += "\"url\":\"" + escape(build.getUrl()) + "\",";
         JSONText += "\"buildLogUrl\":\"" + escape(build.getBuildLogUrl()) + "\",";
         JSONText += "\"systemLogUrl\":\"" + escape(build.getSystemLogUrl()) + "\",";
         JSONText += "\"revisionLogUrl\":\"" + escape(build.getRevisionLogUrl()) + "\",";
         JSONText += "\"labelStrategy\":\"" + escape(Constants.getLabelStrategyText(build.getLabelStrategy())) + "\",";
         JSONText += "\"postbuildStrategy\":\"" + escape(Constants.getPostbuildStrategyText(build.getPostbuildStrategy())) + "\",";
         JSONText += "\"publishDir\":\"" + escape(build.getPublishDir()) + "\",";
         JSONText += "\"artifactsDir\":\"" + escape(build.getArtifactsDir()) + "\",";
         JSONText += "\"junitHtmlReportDir\":\"" + escape(build.getJunitHtmlReportDir()) + "\",";
 
         // Lists
         JSONText += "\"vcss\":[";
         Iterator vcslist = build.getVcsList().iterator();
         while (vcslist.hasNext()) {
             Vcs vcs = (Vcs) vcslist.next();
             JSONText += getVcs(vcs) + ",";
         }
         JSONText += "],";
         JSONText += "\"builders\":[";
         Iterator builderlist = build.getBuilderList().iterator();
         while (builderlist.hasNext()) {
             Builder builder = (Builder) builderlist.next();
             JSONText += getBuilder(builder) + ",";
         }
         JSONText += "],";
         JSONText += "\"postbuilders\":[";
         Iterator postbuilderlist = build.getPostbuilderList().iterator();
         while (postbuilderlist.hasNext()) {
             Builder postbuilder = (Builder) postbuilderlist.next();
             JSONText += getBuilder(postbuilder) + ",";
         }
         JSONText += "],";
 
         JSONText += "}";
 
         // Return JSON text
         return JSONText;
     }
 }
