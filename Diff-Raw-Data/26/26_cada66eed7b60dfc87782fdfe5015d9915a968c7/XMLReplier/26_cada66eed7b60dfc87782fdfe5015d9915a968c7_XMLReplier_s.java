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
  * XML API replier implementation.
  *
  * @author Jason Archer
  */
 public class XMLReplier extends Replier {
 
     /**
      * Constructs a new XML replier.
      */
     public XMLReplier() {
     }
 
     /**
      * Gets the display name for this replier.
      *
      * @return the display name for this replier
      */
     public String getDisplayName() {
         return "XML";
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
         String[] sources = source.split("/");
 
         header += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
         header += "<luntbuild>";
         body += getSystem();
 
         if (method == Replier.ROOT || method == Replier.PROJECTS) {
             body += "<results>";
 
             if (source.equals("")) {
                 // Get all projects
                 Iterator projects = Luntbuild.getDao().loadProjects().iterator();
                 while (projects.hasNext()) {
                     Project project = (Project) projects.next();
                     project = Luntbuild.getDao().loadProject(project.getId());
                     body += getProject(project);
                 }
                 body += "</results>";
             } else {
                 body += "</results>";
                 body += "<error>" + escape("Invalid source \"" + source + "\" for \"projects\"") + "</error>";
             }
         } else if (method == Replier.SCHEDULES) {
             body += "<results>";
 
             if (source.equals("")) {
                 // Get all schedules
                 Iterator schedules = Luntbuild.getDao().loadSchedules().iterator();
                 while (schedules.hasNext()) {
                     Schedule schedule = (Schedule) schedules.next();
                     body += getSchedule(schedule);
                 }
                 body += "</results>";
             } else if (!source.equals("") && sources.length == 1) {
                 try {
                     // Get all schedules for this project
                     Project project = Luntbuild.getDao().loadProject(sources[0]);
                     Iterator schedules = project.getSchedules().iterator();
                     while (schedules.hasNext()) {
                         Schedule schedule = (Schedule) schedules.next();
                         body += getSchedule(schedule);
                     }
                     body += "</results>";
                 } catch (DataAccessException e) {
                     body += "</results>";
                     body += "<error>" + escape("Project not found") + "</error>";
                 }
             } else {
                 body += "</results>";
                 body += "<error>" + escape("Invalid source \"" + source + "\" for \"schedules\"") + "</error>";
             }
         } else if (method == Replier.BUILDS) {
             body += "<results>";
 
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
                         body += getBuild(build);
                     }
                 }
                 body += "</results>";
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
                             body += getBuild(build);
                         }
                     }
                     body += "</results>";
                 } catch (DataAccessException e) {
                     body += "</results>";
                     body += "<error>" + escape("Project not found") + "</error>";
                 }
             } else if (sources.length == 2) {
                 try {
                     // Get all builds for this schedule
                     Schedule schedule = Luntbuild.getDao().loadSchedule(sources[0], sources[1]);
                     SearchCriteria searchCriteria = new SearchCriteria();
                     searchCriteria.setScheduleIds(new long[]{schedule.getId()});
                     Iterator builds = Luntbuild.getDao().searchBuilds(searchCriteria, 0, 0).iterator();
                     while (builds.hasNext()) {
                         Build build = (Build) builds.next();
                         body += getBuild(build);
                     }
                     body += "</results>";
                 } catch (DataAccessException e) {
                     body += "</results>";
                     body += "<error>" + escape("Schedule not found") + "</error>";
                 }
             } else {
                 body += "</results>";
                 body += "<error>" + escape("Invalid source \"" + source + "\" for \"builds\"") + "</error>";
             }
         } else if (method == Replier.BUILD) {
             body += "<results>";
 
             if (sources.length == 3) {
                 try {
                     // Get build
                     Build build = Luntbuild.getDao().loadBuild(sources[0], sources[1], sources[2]);
                     body += getBuild(build);
                     body += "</results>";
                 } catch (DataAccessException e) {
                     body += "</results>";
                     body += "<error>" + escape("Build not found") + "</error>";
                 }
             } else {
                 body += "</results>";
                 body += "<error>" + escape("Invalid source \"" + source + "\" for \"build\"") + "</error>";
             }
         } else if (method == Replier.USERS) {
             body += "<results>";
 
             // Get all users
             Iterator users = Luntbuild.getDao().loadUsers().iterator();
             while (users.hasNext()) {
                 User user = (User) users.next();
                 body += getUser(user);
             }
             body += "</results>";
         } else if (method == Replier.USER) {
             body += "<results>";
 
             if (!source.equals("") && sources.length == 1) {
                 try {
                     // Get user
                     User user = Luntbuild.getDao().loadUser(sources[0]);
                     body += getUser(user);
                     body += "</results>";
                 } catch (DataAccessException e) {
                     body += "</results>";
                     body += "<error>" + escape("User \"" + sources[0] + "\" not found") + "</error>";
                 }
             } else {
                 body += "</results>";
                 body += "<error>" + escape("Invalid source \"" + source + "\" for \"user\"") + "</error>";
             }
         } else {
             body += "<error>" + escape("Unsupported method") + "</error>";
         }
         footer += "</luntbuild>";
 
         reply = header + body + footer;
 
         return reply;
     }
 
     /**
      * Escapes special characters in the XML.
      * 
      * @param text the XML
      * @return the properly escaped XML
      */
     protected String escape(String text) {
         if (text == null) text = "";
         text = text.replaceAll("&","&amp;");
         text = text.replaceAll("<","&lt;");
         text = text.replaceAll(">","&gt;");
 
         // Return escaped text
         return text;
     }
 
     /**
      * Converts a text area to a list of XML elements.  Each line in the text area will be a separate element.
      * 
      * @param element the element name to use
      * @param text the text area
      * @return the XML elements
      */
     protected String toArray(String element, String text) {
         if (text == null) text = "";
 
         String array = "";
         String[] lines = text.split("\r*\n");
         for (int i = 0; i < lines.length; i++) {
             if (!Luntbuild.isEmpty(lines[i]))
                 array += "<" + element + ">" + escape(lines[i]) + "</" + element + ">";
         }
 
         // Return escaped text
         return array;
     }
 
     /**
      * Gets the "system" object containing system wide settings and users.
      * 
      * @return the system object
      */
     public String getSystem() {
         String XMLText = "";
         XMLText += "<system>";
        XMLText += "<servletUrl>" + escape((String) Luntbuild.getProperties().get(Constants.SERVLET_URL)) + "</servletUrl>";
         XMLText += "<workingDir>" + escape((String) Luntbuild.getProperties().get(Constants.WORKING_DIR)) + "</workingDir>";
         XMLText += "<publishDir>" + escape((String) Luntbuild.getProperties().get(Constants.PUBLISH_DIR)) + "</publishDir>";
         XMLText += "<pageRefreshInterval>" + ((String) Luntbuild.getProperties().get(Constants.PAGE_REFRESH_INTERVAL)) + "</pageRefreshInterval>";
         XMLText += "<buildThreadCount>" + ((String) Luntbuild.getProperties().get(Constants.BUILD_THREAD_COUNT)) + "</buildThreadCount>";
         XMLText += "<emailProperties>";
         Iterator emailprops = (new EmailNotifier()).getSystemLevelProperties().iterator();
         while (emailprops.hasNext()) {
             NotifierProperty property = (NotifierProperty) emailprops.next();
             XMLText += getNotifierProperty(property);
         }
         XMLText += "</emailProperties>";
         XMLText += "<msnProperties>";
         Iterator msnprops = (new MsnNotifier()).getSystemLevelProperties().iterator();
         while (msnprops.hasNext()) {
             NotifierProperty property = (NotifierProperty) msnprops.next();
             XMLText += getNotifierProperty(property);
         }
         XMLText += "</msnProperties>";
         XMLText += "<jabberProperties>";
         Iterator jabberprops = (new JabberNotifier()).getSystemLevelProperties().iterator();
         while (jabberprops.hasNext()) {
             NotifierProperty property = (NotifierProperty) jabberprops.next();
             XMLText += getNotifierProperty(property);
         }
         XMLText += "</jabberProperties>";
         XMLText += "<sametimeProperties>";
         Iterator sametimeprops = (new SametimeNotifier()).getSystemLevelProperties().iterator();
         while (sametimeprops.hasNext()) {
             NotifierProperty property = (NotifierProperty) sametimeprops.next();
             XMLText += getNotifierProperty(property);
         }
         XMLText += "</sametimeProperties>";
         XMLText += "<users>";
         Iterator users = Luntbuild.getDao().loadUsers().iterator();
         while (users.hasNext()) {
             User user = (User) users.next();
             XMLText += getUser(user);
         }
         XMLText += "</users>";
 
         XMLText += "</system>";
 
         // Return XML text
         return XMLText;
     }
 
     /**
      * Gets the property object of the specified system level property.
      * 
      * @param property the property
      * @return the property object
      */
     public String getNotifierProperty(NotifierProperty property) {
         String XMLText = "";
         XMLText += "<notifierProperty>";
         XMLText += "<key>" + escape(property.getKey()) + "</key>";
         XMLText += "<value>" + escape(property.getValue(Luntbuild.getProperties())) + "</value>";
         XMLText += "<displayName>" + escape(property.getDisplayName()) + "</displayName>";
         XMLText += "<description>" + escape(property.getDescription()) + "</description>";
         XMLText += "<notifierClass>" + escape(property.getNotifierClass().getName()) + "</notifierClass>";
 
         XMLText += "</notifierProperty>";
 
         // Return XML text
         return XMLText;
     }
 
     /**
      * Gets the property object of the specified user level property.
      * 
      * @param property the property
      * @param user the user
      * @return the property object
      */
     public String getNotifierProperty(NotifierProperty property, User user) {
         String XMLText = "";
         XMLText += "<notifierProperty>";
         XMLText += "<key>" + escape(property.getKey()) + "</key>";
         XMLText += "<value>" + escape(property.getValue(user.getContacts())) + "</value>";
 
         XMLText += "</notifierProperty>";
 
         // Return XML text
         return XMLText;
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
         String XMLText = "";
         XMLText += "<user>";
         XMLText += "<id>" + user.getId() + "</id>";
         XMLText += "<name>" + escape(user.getName()) + "</name>";
         if (full) {
             XMLText += "<fullname>" + escape(user.getFullname()) + "</fullname>";
             XMLText += "<canCreateProject>" + user.isCanCreateProject() + "</canCreateProject>";
 
             // Hash table
             XMLText += "<contacts>";
             Iterator emailprops = (new EmailNotifier()).getUserLevelProperties().iterator();
             while (emailprops.hasNext()) {
                 NotifierProperty property = (NotifierProperty) emailprops.next();
                 XMLText += getNotifierProperty(property, user);
             }
             Iterator msnprops = (new MsnNotifier()).getUserLevelProperties().iterator();
             while (msnprops.hasNext()) {
                 NotifierProperty property = (NotifierProperty) msnprops.next();
                 XMLText += getNotifierProperty(property, user);
             }
             Iterator jabberprops = (new JabberNotifier()).getUserLevelProperties().iterator();
             while (jabberprops.hasNext()) {
                 NotifierProperty property = (NotifierProperty) jabberprops.next();
                 XMLText += getNotifierProperty(property, user);
             }
             Iterator sametimeprops = (new SametimeNotifier()).getUserLevelProperties().iterator();
             while (sametimeprops.hasNext()) {
                 NotifierProperty property = (NotifierProperty) sametimeprops.next();
                 XMLText += getNotifierProperty(property, user);
             }
             XMLText += "</contacts>";
         }
 
         XMLText += "</user>";
 
         // Return XML text
         return XMLText;
     }
 
     /**
      * Gets the project object of the specified project.
      * 
      * @param project the project
      * @return the project object
      */
     public String getProject(Project project) {
         String XMLText = "";
         XMLText += "<project>";
         XMLText += "<id>" + project.getId() + "</id>";
         XMLText += "<name>" + escape(project.getName()) + "</name>";
         XMLText += "<description>" + escape(project.getDescription()) + "</description>";
         XMLText += "<variables>" + toArray("variable", project.getVariables()) + "</variables>";
         XMLText += "<logLevel>" + escape(Constants.getLogLevelText(project.getLogLevel())) + "</logLevel>";
 
         // Lists
         XMLText += "<projectAdmins>";
         Iterator adminlist = project.getMappedRolesUserList(Role.LUNTBUILD_PRJ_ADMIN).iterator();
         while (adminlist.hasNext()) {
             User user = (User) adminlist.next();
             XMLText += getUser(user, false);
         }
         XMLText += "</projectAdmins>";
         XMLText += "<projectBuilders>";
         Iterator builderslist = project.getMappedRolesUserList(Role.LUNTBUILD_PRJ_BUILDER).iterator();
         while (builderslist.hasNext()) {
             User user = (User) builderslist.next();
             XMLText += getUser(user, false);
         }
         XMLText += "</projectBuilders>";
         XMLText += "<projectViewers>";
         Iterator viewerlist = project.getMappedRolesUserList(Role.LUNTBUILD_PRJ_VIEWER).iterator();
         while (viewerlist.hasNext()) {
             User user = (User) viewerlist.next();
             XMLText += getUser(user, false);
         }
         XMLText += "</projectViewers>";
         XMLText += "<notifyUsers>";
         Iterator notifylist = project.getNotifyMappings().iterator();
         while (notifylist.hasNext()) {
             User user = ((NotifyMapping) notifylist.next()).getUser();
             XMLText += getUser(user, false);
         }
         XMLText += "</notifyUsers>";
         XMLText += "<vcss>";
         Iterator vcslist = project.getVcsList().iterator();
         while (vcslist.hasNext()) {
             Vcs vcs = (Vcs) vcslist.next();
             XMLText += getVcs(vcs);
         }
         XMLText += "</vcss>";
         XMLText += "<builders>";
         Iterator builderlist = project.getBuilderList().iterator();
         while (builderlist.hasNext()) {
             Builder builder = (Builder) builderlist.next();
             XMLText += getBuilder(builder);
         }
         XMLText += "</builders>";
         XMLText += "<notifiers>";
         Iterator notifierlist = Luntbuild.getNotifierInstances(Luntbuild.getNotifierClasses(project.getNotifiers())).iterator();
         while (notifierlist.hasNext()) {
             XMLText += "<notifierName>" + ((Notifier) notifierlist.next()).getDisplayName() + "</notifierName>";
         }
         XMLText += "</notifiers>";
         XMLText += "<schedules>";
         Iterator schedules = project.getSchedules().iterator();
         while (schedules.hasNext()) {
             Schedule schedule = (Schedule) schedules.next();
             XMLText += getSchedule(schedule);
         }
         XMLText += "</schedules>";
 
         XMLText += "</project>";
 
         // Return XML text
         return XMLText;
     }
 
     /**
      * Gets the vcs object of the specified vcs.
      * 
      * @param vcs the vcs
      * @return the vcs object
      */
     public String getVcs(Vcs vcs) {
         String XMLText = "";
         XMLText += "<vcs>";
         XMLText += "<class>" + escape(vcs.getClass().getName()) + "</class>";
         XMLText += "<displayName>" + escape(vcs.getDisplayName()) + "</displayName>";
         XMLText += "<quietPeriod>" + escape(vcs.getQuietPeriod()) + "</quietPeriod>";
         if (vcs.getClass() == (new AccurevAdaptor()).getClass()) {
             AccurevAdaptor accurev = (AccurevAdaptor) vcs;
             XMLText += "<user>" + escape(accurev.getUser()) + "</user>";
             XMLText += "<password>*****</password>";
             XMLText += "<modules>";
             Iterator modules = accurev.getModules().iterator();
             while (modules.hasNext()) {
                 AccurevAdaptor.AccurevModule accurevmodule = (AccurevAdaptor.AccurevModule) modules.next();
                 XMLText += "<module>";
                 XMLText += "<depot>" + escape(accurevmodule.getDepot()) + "</depot>";
                 XMLText += "<srcPath>" + escape(accurevmodule.getSrcPath()) + "</srcPath>";
                 XMLText += "<backingStream>" + escape(accurevmodule.getBackingStream()) + "</backingStream>";
                 XMLText += "<buildStream>" + escape(accurevmodule.getBuildStream()) + "</buildStream>";
                 XMLText += "<label>" + escape(accurevmodule.getLabel()) + "</label>";
                 XMLText += "</module>";
             }
             XMLText += "</modules>";
         } else if (vcs.getClass() == (new BaseClearcaseAdaptor()).getClass()) {
             BaseClearcaseAdaptor base = (BaseClearcaseAdaptor) vcs;
             XMLText += "<viewTag>" + escape(base.getViewTag()) + "</viewTag>";
             XMLText += "<viewStgLoc>" + escape(base.getViewStgLoc()) + "</viewStgLoc>";
             XMLText += "<vws>" + escape(base.getVws()) + "</vws>";
             XMLText += "<viewCfgSpec>" + escape(base.getViewCfgSpec()) + "</viewCfgSpec>";
             XMLText += "<modificationDetectionConfig>" + escape(base.getModificationDetectionConfig()) + "</modificationDetectionConfig>";
             XMLText += "<mkviewExtraOpts>" + escape(base.getMkviewExtraOpts()) + "</mkviewExtraOpts>";
             XMLText += "<cleartoolDir>" + escape(base.getCleartoolDir()) + "</cleartoolDir>";
             XMLText += "<formatParams>" + escape(base.getFormatParams()) + "</formatParams>";
         } else if (vcs.getClass() == (new CvsAdaptor()).getClass()) {
             CvsAdaptor cvs = (CvsAdaptor) vcs;
             XMLText += "<cvsRoot>" + escape(cvs.getCvsRoot()) + "</cvsRoot>";
             XMLText += "<cvsPassword>*****</cvsPassword>";
             XMLText += "<cvsDir>" + escape(cvs.getCvsDir()) + "</cvsDir>";
             XMLText += "<cygwinCvs>" + cvs.isCygwinCvs() + "</cygwinCvs>";
             XMLText += "<disableSuppressOption>" + cvs.isDisableSuppressOption() + "</disableSuppressOption>";
             XMLText += "<disableHistoryCmd>" + cvs.isDisableHistoryCmd() + "</disableHistoryCmd>";
             XMLText += "<modules>";
             Iterator modules = cvs.getModules().iterator();
             while (modules.hasNext()) {
                 CvsAdaptor.CvsModule cvsmodule = (CvsAdaptor.CvsModule) modules.next();
                 XMLText += "<module>";
                 XMLText += "<srcPath>" + escape(cvsmodule.getSrcPath()) + "</srcPath>";
                 XMLText += "<branch>" + escape(cvsmodule.getBranch()) + "</branch>";
                 XMLText += "<label>" + escape(cvsmodule.getLabel()) + "</label>";
                 XMLText += "</module>";
             }
             XMLText += "</modules>";
         } else if (vcs.getClass() == (new DynamicClearcaseAdaptor()).getClass()) {
             DynamicClearcaseAdaptor dynamic = (DynamicClearcaseAdaptor) vcs;
             XMLText += "<viewTag>" + escape(dynamic.getViewTag()) + "</viewTag>";
             XMLText += "<viewStgLoc>" + escape(dynamic.getViewStgLoc()) + "</viewStgLoc>";
             XMLText += "<vws>" + escape(dynamic.getVws()) + "</vws>";
             XMLText += "<viewCfgSpec>" + escape(dynamic.getViewCfgSpec()) + "</viewCfgSpec>";
             XMLText += "<modificationDetectionConfig>" + escape(dynamic.getModificationDetectionConfig()) + "</modificationDetectionConfig>";
             XMLText += "<mkviewExtraOpts>" + escape(dynamic.getMkviewExtraOpts()) + "</mkviewExtraOpts>";
             XMLText += "<cleartoolDir>" + escape(dynamic.getCleartoolDir()) + "</cleartoolDir>";
             XMLText += "<formatParams>" + escape(dynamic.getFormatParams()) + "</formatParams>";
             XMLText += "<mvfsPath>" + escape(dynamic.getMvfsPath()) + "</mvfsPath>";
             XMLText += "<projectPath>" + escape(dynamic.getProjectPath()) + "</projectPath>";
         } else if (vcs.getClass() == (new FileSystemAdaptor()).getClass()) {
             FileSystemAdaptor file = (FileSystemAdaptor) vcs;
             XMLText += "<sourceDir>" + escape(file.getSourceDir()) + "</sourceDir>";
         } else if (vcs.getClass() == (new MksAdaptor()).getClass()) {
             MksAdaptor mks = (MksAdaptor) vcs;
             XMLText += "<defaultHostname>" + escape(mks.getDefaultHostname()) + "</defaultHostname>";
             XMLText += "<defaultPort>" + mks.getDefaultPort() + "</getDefaultPort>";
             XMLText += "<defaultUsername>" + escape(mks.getDefaultUsername()) + "</defaultUsername>";
             XMLText += "<defaultPassword>*****</defaultPassword>";
             XMLText += "<rootProject>" + escape(mks.getRootProject()) + "</rootProject>";
             XMLText += "<modules>";
             Iterator modules = mks.getModules().iterator();
             while (modules.hasNext()) {
                 MksAdaptor.MksModule mksmodule = (MksAdaptor.MksModule) modules.next();
                 XMLText += "<module>";
                 XMLText += "<subproject>" + escape(mksmodule.getSubproject()) + "</subproject>";
                 XMLText += "<version>" + escape(mksmodule.getVersion()) + "</version>";
                 XMLText += "<projectFileName>" + escape(mksmodule.getProjectFileName()) + "</projectFileName>";
                 XMLText += "<developmentPath>" + escape(mksmodule.getDevelopmentPath()) + "</developmentPath>";
                 XMLText += "<external>" + mksmodule.isExternal() + "</external>";
                 XMLText += "</module>";
             }
             XMLText += "</modules>";
         } else if (vcs.getClass() == (new PerforceAdaptor()).getClass()) {
             PerforceAdaptor p4 = (PerforceAdaptor) vcs;
             XMLText += "<p4Port>" + escape(p4.getPort()) + "</p4Port>";
             XMLText += "<p4User>" + escape(p4.getUser()) + "</p4User>";
             XMLText += "<p4Password>*****</p4Password>";
             XMLText += "<lineEnd>" + escape(p4.getLineEnd()) + "</lineEnd>";
             XMLText += "<p4Dir>" + escape(p4.getP4Dir()) + "</p4Dir>";
             XMLText += "<changelist>" + escape(p4.getChangelist()) + "</changelist>";
             XMLText += "<modules>";
             Iterator modules = p4.getModules().iterator();
             while (modules.hasNext()) {
                 PerforceAdaptor.PerforceModule p4module = (PerforceAdaptor.PerforceModule) modules.next();
                 XMLText += "<module>";
                 XMLText += "<depotPath>" + escape(p4module.getDepotPath()) + "</depotPath>";
                 XMLText += "<label>" + escape(p4module.getLabel()) + "</label>";
                 XMLText += "<clientPath>" + escape(p4module.getClientPath()) + "</clientPath>";
                 XMLText += "</module>";
             }
             XMLText += "</modules>";
         } else if (vcs.getClass() == (new StarteamAdaptor()).getClass()) {
             StarteamAdaptor star = (StarteamAdaptor) vcs;
             XMLText += "<projectLocation>" + escape(star.getProjectLocation()) + "</projectLocation>";
             XMLText += "<user>" + escape(star.getUser()) + "</user>";
             XMLText += "<password>*****</password>";
             XMLText += "<convertEOL>" + escape(star.getConvertEOL()) + "</convertEOL>";
             XMLText += "<modules>";
             Iterator modules = star.getModules().iterator();
             while (modules.hasNext()) {
                 StarteamAdaptor.StarteamModule starmodule = (StarteamAdaptor.StarteamModule) modules.next();
                 XMLText += "<module>";
                 XMLText += "<starteamView>" + escape(starmodule.getStarteamView()) + "</starteamView>";
                 XMLText += "<starteamPromotionState>" + escape(starmodule.getStarteamPromotionState()) + "</starteamPromotionState>";
                 XMLText += "<srcPath>" + escape(starmodule.getSrcPath()) + "</srcPath>";
                 XMLText += "<label>" + escape(starmodule.getLabel()) + "</label>";
                 XMLText += "<destPath>" + escape(starmodule.getDestPath()) + "</destPath>";
                 XMLText += "</module>";
             }
             XMLText += "</modules>";
         } else if (vcs.getClass() == (new SvnAdaptor()).getClass()) {
             SvnAdaptor svn = (SvnAdaptor) vcs;
             XMLText += "<urlBase>" + escape(svn.getUrlBase()) + "</urlBase>";
             XMLText += "<trunk>" + escape(svn.getTrunk()) + "</trunk>";
             XMLText += "<branches>" + escape(svn.getBranches()) + "</branches>";
             XMLText += "<tags>" + escape(svn.getTags()) + "</tags>";
             XMLText += "<user>" + escape(svn.getUser()) + "</user>";
             XMLText += "<password>*****</password>";
             XMLText += "<modules>";
             Iterator modules = svn.getModules().iterator();
             while (modules.hasNext()) {
                 SvnAdaptor.SvnModule svnmodule = (SvnAdaptor.SvnModule) modules.next();
                 XMLText += "<module>";
                 XMLText += "<srcPath>" + escape(svnmodule.getSrcPath()) + "</srcPath>";
                 XMLText += "<branch>" + escape(svnmodule.getBranch()) + "</branch>";
                 XMLText += "<label>" + escape(svnmodule.getLabel()) + "</label>";
                 XMLText += "<destPath>" + escape(svnmodule.getDestPath()) + "</destPath>";
                 XMLText += "</module>";
             }
             XMLText += "</modules>";
         } else if (vcs.getClass() == (new SvnExeAdaptor()).getClass()) {
             SvnExeAdaptor svn = (SvnExeAdaptor) vcs;
             XMLText += "<urlBase>" + escape(svn.getUrlBase()) + "</urlBase>";
             XMLText += "<trunk>" + escape(svn.getTrunk()) + "</trunk>";
             XMLText += "<branches>" + escape(svn.getBranches()) + "</branches>";
             XMLText += "<tags>" + escape(svn.getTags()) + "</tags>";
             XMLText += "<user>" + escape(svn.getUser()) + "</user>";
             XMLText += "<password>*****</password>";
             XMLText += "<svnDir>" + escape(svn.getSvnDir()) + "</svnDir>";
             XMLText += "<modules>";
             Iterator modules = svn.getModules().iterator();
             while (modules.hasNext()) {
                 SvnAdaptor.SvnModule svnmodule = (SvnAdaptor.SvnModule) modules.next();
                 XMLText += "<module>";
                 XMLText += "<srcPath>" + escape(svnmodule.getSrcPath()) + "</srcPath>";
                 XMLText += "<branch>" + escape(svnmodule.getBranch()) + "</branch>";
                 XMLText += "<label>" + escape(svnmodule.getLabel()) + "</label>";
                 XMLText += "<destPath>" + escape(svnmodule.getDestPath()) + "</destPath>";
                 XMLText += "</module>";
             }
             XMLText += "</modules>";
         } else if (vcs.getClass() == (new UCMClearcaseAdaptor()).getClass()) {
             UCMClearcaseAdaptor ucm = (UCMClearcaseAdaptor) vcs;
             XMLText += "<viewStgLoc>" + escape(ucm.getViewStgLoc()) + "</viewStgLoc>";
             XMLText += "<projectVob>" + escape(ucm.getProjectVob()) + "</projectVob>";
             XMLText += "<vws>" + escape(ucm.getVws()) + "</vws>";
             XMLText += "<stream>" + escape(ucm.getStream()) + "</stream>";
             XMLText += "<whatToBuild>" + escape(ucm.getWhatToBuild()) + "</whatToBuild>";
             XMLText += "<modificationDetectionConfig>" + escape(ucm.getModificationDetectionConfig()) + "</modificationDetectionConfig>";
             XMLText += "<mkviewExtraOpts>" + escape(ucm.getMkviewExtraOpts()) + "</mkviewExtraOpts>";
             XMLText += "<cleartoolDir>" + escape(ucm.getCleartoolDir()) + "</cleartoolDir>";
         } else if (vcs.getClass() == (new VssAdaptor()).getClass()) {
             VssAdaptor vss = (VssAdaptor) vcs;
             XMLText += "<vssPath>" + escape(vss.getVssPath()) + "</vssPath>";
             XMLText += "<vssUser>" + escape(vss.getVssUser()) + "</vssUser>";
             XMLText += "<vssPassword>*****</vssPassword>";
             XMLText += "<vssDir>" + escape(vss.getSsDir()) + "</vssDir>";
             XMLText += "<dateTimeFormat>" + escape(vss.getDateTimeFormat()) + "</dateTimeFormat>";
             XMLText += "<modules>";
             Iterator modules = vss.getModules().iterator();
             while (modules.hasNext()) {
                 VssAdaptor.VssModule vssmodule = (VssAdaptor.VssModule) modules.next();
                 XMLText += "<module>";
                 XMLText += "<srcPath>" + escape(vssmodule.getSrcPath()) + "</srcPath>";
                 XMLText += "<label>" + escape(vssmodule.getLabel()) + "</label>";
                 XMLText += "<destPath>" + escape(vssmodule.getDestPath()) + "</destPath>";
                 XMLText += "</module>";
             }
             XMLText += "</modules>";
         }
 
         XMLText += "</vcs>";
 
         // Return XML text
         return XMLText;
     }
 
     /**
      * Gets the builder object of the specified builder.
      * 
      * @param builder the builder
      * @return the builder object
      */
     public String getBuilder(Builder builder) {
         String XMLText = "";
         XMLText += "<builder>";
         XMLText += "<class>" + escape(builder.getClass().getName()) + "</class>";
         XMLText += "<displayName>" + escape(builder.getDisplayName()) + "</displayName>";
         XMLText += "<name>" + escape(builder.getName()) + "</name>";
         XMLText += "<environments>" + toArray("variable", builder.getEnvironments()) + "</environments>";
         XMLText += "<buildSuccessCondition>" + escape(builder.getBuildSuccessCondition()) + "</buildSuccessCondition>";
         if (builder.getClass() == (new AntBuilder()).getClass()) {
             AntBuilder ant = (AntBuilder) builder;
             XMLText += "<command>" + escape(ant.getCommand()) + "</command>";
             XMLText += "<buildScriptPath>" + escape(ant.getBuildScriptPath()) + "</buildScriptPath>";
             XMLText += "<targets>" + escape(ant.getTargets()) + "</targets>";
             XMLText += "<buildProperties>" + toArray("property", ant.getBuildProperties()) + "</buildProperties>";
         } else if (builder.getClass() == (new CommandBuilder()).getClass()) {
             CommandBuilder cmd = (CommandBuilder) builder;
             XMLText += "<command>" + escape(cmd.getCommand()) + "</command>";
             XMLText += "<dirToRunCmd>" + escape(cmd.getDirToRunCmd()) + "</dirToRunCmd>";
             XMLText += "<waitForFinish>" + escape(cmd.getWaitForFinish()) + "</waitForFinish>";
         } else if (builder.getClass() == (new Maven2Builder()).getClass()) {
             Maven2Builder maven2 = (Maven2Builder) builder;
             XMLText += "<command>" + escape(maven2.getCommand()) + "</command>";
             XMLText += "<dirToRunMaven>" + escape(maven2.getDirToRunMaven()) + "</dirToRunMaven>";
             XMLText += "<goals>" + escape(maven2.getGoals()) + "</goals>";
             XMLText += "<buildProperties>" + toArray("property", maven2.getBuildProperties()) + "</buildProperties>";
         } else if (builder.getClass() == (new MavenBuilder()).getClass()) {
             MavenBuilder maven = (MavenBuilder) builder;
             XMLText += "<command>" + escape(maven.getCommand()) + "</command>";
             XMLText += "<dirToRunMaven>" + escape(maven.getDirToRunMaven()) + "</dirToRunMaven>";
             XMLText += "<goals>" + escape(maven.getGoals()) + "</goals>";
             XMLText += "<buildProperties>" + toArray("property", maven.getBuildProperties()) + "</buildProperties>";
         } else if (builder.getClass() == (new RakeBuilder()).getClass()) {
             RakeBuilder rake = (RakeBuilder) builder;
             XMLText += "<command>" + escape(rake.getCommand()) + "</command>";
             XMLText += "<buildScriptPath>" + escape(rake.getBuildScriptPath()) + "</buildScriptPath>";
             XMLText += "<targets>" + escape(rake.getTargets()) + "</targets>";
             XMLText += "<buildProperties>" + toArray("property", rake.getBuildProperties()) + "</buildProperties>";
         }
 
         XMLText += "</builder>";
 
         // Return XML text
         return XMLText;
     }
 
     /**
      * Gets the schedule object of the specified schedule.
      * 
      * @param schedule the schedule
      * @return the schedule object
      */
     public String getSchedule(Schedule schedule) {
         ScheduleFacade schedfacade = schedule.getFacade();
         String XMLText = "";
         XMLText += "<schedule>";
         XMLText += "<id>" + schedfacade.getId() + "</id>";
         XMLText += "<name>" + escape(schedfacade.getName()) + "</name>";
         XMLText += "<description>" + escape(schedfacade.getDescription()) + "</description>";
         XMLText += "<url>" + escape(schedfacade.getUrl()) + "</url>";
         XMLText += "<scheduleDisabled>" + schedfacade.isScheduleDisabled() + "</scheduleDisabled>";
         XMLText += "<buildType>" + escape(Constants.getBuildTypeText(schedfacade.getBuildType())) + "</buildType>";
         XMLText += "<labelStrategy>" + escape(Constants.getLabelStrategyText(schedfacade.getLabelStrategy())) + "</labelStrategy>";
         XMLText += "<notifyStrategy>" + escape(Constants.getNotifyStrategyText(schedfacade.getNotifyStrategy())) + "</notifyStrategy>";
         XMLText += "<postbuildStrategy>" + escape(Constants.getPostbuildStrategyText(schedfacade.getPostbuildStrategy())) + "</postbuildStrategy>";
         XMLText += "<triggerDependencyStrategy>" + escape(Constants.getTriggerDependencyStrategyText(schedfacade.getTriggerDependencyStrategy())) + "</triggerDependencyStrategy>";
         XMLText += "<status>" + escape(Constants.getScheduleStatusText(schedfacade.getStatus())) + "</status>";
         XMLText += "<statusDate>" + escape(Luntbuild.DATE_DISPLAY_FORMAT.format(schedfacade.getStatusDate())) + "</statusDate>";
         XMLText += "<buildNecessaryCondition>" + escape(schedfacade.getBuildNecessaryCondition()) + "</buildNecessaryCondition>";
         XMLText += "<buildCleanupStrategy>" + escape(Constants.getBuildCleanupStrategyText(schedfacade.getBuildCleanupStrategy())) + "</buildCleanupStrategy>";
         XMLText += "<buildCleanupStrategyData>" + escape(schedfacade.getBuildCleanupStrategyData()) + "</buildCleanupStrategyData>";
         XMLText += "<nextVersion>" + escape(schedfacade.getNextVersion()) + "</nextVersion>";
         XMLText += "<workingPath>" + escape(schedfacade.getWorkingPath()) + "</workingPath>";
         XMLText += "<publishDir>" + escape(schedule.getPublishDir()) + "</publishDir>";
 
         // Enumerated values
         if (schedfacade.getTriggerType() == Constants.TRIGGER_TYPE_CRON) {
             XMLText += "<triggerType>cron</triggerType>";
             XMLText += "<trigger>" + escape(schedfacade.getCronExpression()) + "</trigger>";
         }
         else if (schedfacade.getTriggerType() == Constants.TRIGGER_TYPE_MANUAL) {
             XMLText += "<triggerType>manual</triggerType>";
             XMLText += "<trigger></trigger>";
         }
         else if (schedfacade.getTriggerType() == Constants.TRIGGER_TYPE_SIMPLE) {
             XMLText += "<triggerType>simple</triggerType>";
             XMLText += "<trigger>" + escape(Long.toString(schedfacade.getRepeatInterval())) + "</trigger>";
         }
         else {
             XMLText += "<triggerType>" + schedfacade.getTriggerType() + "</triggerType>";
             XMLText += "<trigger></trigger>";
         }
 
         // Lists
         XMLText += "<associatedBuilderNames>";
         Iterator buildernames = schedule.getAssociatedBuilderNames().iterator();
         while (buildernames.hasNext()) {
             XMLText += "<builderName>" + ((String) buildernames.next()) + "</builderName>";
         }
         XMLText += "</associatedBuilderNames>";
         XMLText += "<associatedPostbuilderNames>";
         Iterator postbuildernames = schedule.getAssociatedPostbuilderNames().iterator();
         while (postbuildernames.hasNext()) {
             XMLText += "<builderName>" + ((String) postbuildernames.next()) + "</builderName>";
         }
         XMLText += "</associatedPostbuilderNames>";
         XMLText += "<dependentScheduleIds>";
         Iterator scheduleids = schedule.getDependentScheduleIds().iterator();
         while (scheduleids.hasNext()) {
             XMLText += "<dependentScheduleId>" + ((String) scheduleids.next()) + "</dependentScheduleId>";
         }
         XMLText += "</dependentScheduleIds>";
         XMLText += "<builds>";
         SearchCriteria searchCriteria = new SearchCriteria();
         searchCriteria.setScheduleIds(new long[]{schedule.getId()});
         Iterator builds = Luntbuild.getDao().searchBuilds(searchCriteria, 0, 0).iterator();
         while (builds.hasNext()) {
             Build build = (Build) builds.next();
             XMLText += getBuild(build);
         }
         XMLText += "</builds>";
 
         XMLText += "</schedule>";
 
         // Return XML text
         return XMLText;
     }
 
     /**
      * Gets the build object of the specified build.
      * 
      * @param build the build
      * @return the build object
      */
     public String getBuild(Build build) {
         String XMLText = "";
         XMLText += "<build>";
         XMLText += "<id>" + build.getId() + "</id>";
         XMLText += "<version>" + escape(build.getVersion()) + "</version>";
         XMLText += "<status>" + escape(Constants.getBuildStatusText(build.getStatus())) + "</status>";
         XMLText += "<startDate>" + escape(Luntbuild.DATE_DISPLAY_FORMAT.format(build.getStartDate())) + "</startDate>";
         XMLText += "<endDate>" + escape(Luntbuild.DATE_DISPLAY_FORMAT.format(build.getEndDate())) + "</endDate>";
         XMLText += "<haveLabelOnHead>" + build.isHaveLabelOnHead() + "</haveLabelOnHead>";
         XMLText += "<buildType>" + escape(Constants.getBuildTypeText(build.getBuildType())) + "</buildType>";
         XMLText += "<rebuild>" + build.isRebuild() + "</rebuild>";
         XMLText += "<url>" + escape(build.getUrl()) + "</url>";
         XMLText += "<buildLogUrl>" + escape(build.getBuildLogUrl()) + "</buildLogUrl>";
         XMLText += "<systemLogUrl>" + escape(build.getSystemLogUrl()) + "</systemLogUrl>";
         XMLText += "<revisionLogUrl>" + escape(build.getRevisionLogUrl()) + "</revisionLogUrl>";
         XMLText += "<labelStrategy>" + escape(Constants.getLabelStrategyText(build.getLabelStrategy())) + "</labelStrategy>";
         XMLText += "<postbuildStrategy>" + escape(Constants.getPostbuildStrategyText(build.getPostbuildStrategy())) + "</postbuildStrategy>";
         XMLText += "<publishDir>" + escape(build.getPublishDir()) + "</publishDir>";
         XMLText += "<artifactsDir>" + escape(build.getArtifactsDir()) + "</artifactsDir>";
         XMLText += "<junitHtmlReportDir>" + escape(build.getJunitHtmlReportDir()) + "</junitHtmlReportDir>";
 
         // Lists
         XMLText += "<vcss>";
         Iterator vcslist = build.getVcsList().iterator();
         while (vcslist.hasNext()) {
             Vcs vcs = (Vcs) vcslist.next();
             XMLText += getVcs(vcs);
         }
         XMLText += "</vcss>";
         XMLText += "<builders>";
         Iterator builderlist = build.getBuilderList().iterator();
         while (builderlist.hasNext()) {
             Builder builder = (Builder) builderlist.next();
             XMLText += getBuilder(builder);
         }
         XMLText += "</builders>";
         XMLText += "<postbuilders>";
         Iterator postbuilderlist = build.getPostbuilderList().iterator();
         while (postbuilderlist.hasNext()) {
             Builder postbuilder = (Builder) postbuilderlist.next();
             XMLText += getBuilder(postbuilder);
         }
         XMLText += "</postbuilders>";
 
         XMLText += "</build>";
 
         // Return XML text
         return XMLText;
     }
 }
