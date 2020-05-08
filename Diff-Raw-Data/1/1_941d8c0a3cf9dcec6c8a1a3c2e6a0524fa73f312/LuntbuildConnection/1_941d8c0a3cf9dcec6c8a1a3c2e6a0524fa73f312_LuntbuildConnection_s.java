 package com.luntsys.luntbuild.luntclipse.core;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.eclipse.core.runtime.IStatus;
 
 import com.caucho.hessian.client.HessianProxyFactory;
 import com.luntsys.luntbuild.facades.BuildParams;
 import com.luntsys.luntbuild.facades.Constants;
 import com.luntsys.luntbuild.facades.ILuntbuild;
 import com.luntsys.luntbuild.facades.SearchCriteria;
 import com.luntsys.luntbuild.facades.lb12.AccurevAdaptorFacade;
 import com.luntsys.luntbuild.facades.lb12.AccurevModuleFacade;
 import com.luntsys.luntbuild.facades.lb12.AntBuilderFacade;
 import com.luntsys.luntbuild.facades.lb12.BaseClearcaseAdaptorFacade;
 import com.luntsys.luntbuild.facades.lb12.BuildFacade;
 import com.luntsys.luntbuild.facades.lb12.BuilderFacade;
 import com.luntsys.luntbuild.facades.lb12.CommandBuilderFacade;
 import com.luntsys.luntbuild.facades.lb12.CvsAdaptorFacade;
 import com.luntsys.luntbuild.facades.lb12.CvsModuleFacade;
 import com.luntsys.luntbuild.facades.lb12.FileSystemAdaptorFacade;
 import com.luntsys.luntbuild.facades.lb12.Maven2BuilderFacade;
 import com.luntsys.luntbuild.facades.lb12.MavenBuilderFacade;
 import com.luntsys.luntbuild.facades.lb12.ModuleFacade;
 import com.luntsys.luntbuild.facades.lb12.PerforceAdaptorFacade;
 import com.luntsys.luntbuild.facades.lb12.PerforceModuleFacade;
 import com.luntsys.luntbuild.facades.lb12.ProjectFacade;
 import com.luntsys.luntbuild.facades.lb12.RakeBuilderFacade;
 import com.luntsys.luntbuild.facades.lb12.ScheduleFacade;
 import com.luntsys.luntbuild.facades.lb12.StarteamAdaptorFacade;
 import com.luntsys.luntbuild.facades.lb12.StarteamModuleFacade;
 import com.luntsys.luntbuild.facades.lb12.SvnAdaptorFacade;
 import com.luntsys.luntbuild.facades.lb12.SvnModuleFacade;
 import com.luntsys.luntbuild.facades.lb12.UCMClearcaseAdaptorFacade;
 import com.luntsys.luntbuild.facades.lb12.VcsFacade;
 import com.luntsys.luntbuild.facades.lb12.VssAdaptorFacade;
 import com.luntsys.luntbuild.facades.lb12.VssModuleFacade;
 import com.luntsys.luntbuild.luntclipse.LuntclipseConstants;
 import com.luntsys.luntbuild.luntclipse.LuntclipsePlugin;
 import com.luntsys.luntbuild.luntclipse.core.NotificationMessage.SeverityLevel;
 import com.luntsys.luntbuild.luntclipse.model.AccuRevModuleData;
 import com.luntsys.luntbuild.luntclipse.model.BasicProjectData;
 import com.luntsys.luntbuild.luntclipse.model.Build;
 import com.luntsys.luntbuild.luntclipse.model.BuilderProjectData;
 import com.luntsys.luntbuild.luntclipse.model.ConnectionData;
 import com.luntsys.luntbuild.luntclipse.model.CvsModuleData;
 import com.luntsys.luntbuild.luntclipse.model.PerforceModuleData;
 import com.luntsys.luntbuild.luntclipse.model.ScheduleProjectData;
 import com.luntsys.luntbuild.luntclipse.model.StarTeamModuleData;
 import com.luntsys.luntbuild.luntclipse.model.SubversionModuleData;
 import com.luntsys.luntbuild.luntclipse.model.VcsProjectData;
 import com.luntsys.luntbuild.luntclipse.model.VisualSourcesafeModuleData;
 import com.luntsys.luntbuild.luntclipse.model.ConnectionData.NotifyCondition;
import com.luntsys.luntbuild.luntclipse.preferences.PreferenceHelper;
 import com.luntsys.luntbuild.luntclipse.views.LuntbuildViewer;
 
 /**
  * Helper class for obtains general services provided by luntbuild system. This
  * class provides support for HTTP request to Luntbuild system.
  *
  * @author 	 Roman Pichlk, Lubos Pochman
  * @version  $Revision: 1.14 $
  * @since 	 0.0.1
  */
 public class LuntbuildConnection {
 
     private HttpClient client = null;
     private ConnectionData connectionData = null;
     private TreeMap luntbuildData = null;
     private Map<String, Build> previousBuilds = new HashMap<String, Build>();
     private Build lastBuild = null;
     private ArrayList<NotificationMessage> newMessages = new ArrayList<NotificationMessage>();
     private ArrayList<NotificationMessage> errorMessages = new ArrayList<NotificationMessage>();;
     private boolean isFirstLoad = true;
 
     private LuntbuildViewer viewer = null;
     /**
      * Create luntbuild connection
      */
     public LuntbuildConnection(){
     }
 
     /**
      * Create luntbuild connection
      * @param data connection data
      */
     public LuntbuildConnection(ConnectionData data){
         this.connectionData = data;
     }
 
     /**
      * Connect to luntbuild
      */
     private ILuntbuild connect() {
 
     	if (this.connectionData.isPaused()) return null;
 
         HessianProxyFactory factory = new HessianProxyFactory();
         factory.setOverloadEnabled(true);
         factory.setUser(this.connectionData.getUser());
         factory.setPassword(this.connectionData.getPassword());
         try {
             String hessianURL =  this.connectionData.getUrl();
             if (getVersion() <= LuntclipseConstants.getVersion(LuntclipseConstants.LUNTBUILD_VERSION_121)) {
                 hessianURL +=  "/app?service=hessian";
             } else {
                 hessianURL += "/app.do?service=hessian";
             }
 
             //start HessianProxyFactory
             ILuntbuild luntbuild =
                 (ILuntbuild) factory.create(ILuntbuild.class, hessianURL);
 
             //start http client
             if (getVersion() < LuntclipseConstants.getVersion(LuntclipseConstants.LUNTBUILD_VERSION_13)) {
 	            try{
 	              login();
 	            } catch (Exception e) {
 	                LuntclipsePlugin.doLog(IStatus.WARNING, IStatus.OK,
 	                        "Cannot login into Lutbuild! Please verify connection URL.", e);
 	                this.errorMessages.add(new ErrorMessage("Cannot login into Lutbuild! Please verify connection URL."));
 	            }
             }
             return luntbuild;
         } catch (MalformedURLException e) {
             LuntclipsePlugin.doLog(IStatus.ERROR, IStatus.OK,
                     "Connection request to Luntbuild uses invalid URL: " + this.connectionData.getUrl(), e);
             this.errorMessages.add(
             		new ErrorMessage("Connection request to Luntbuild uses invalid URL: " + this.connectionData.getUrl()));
         }
         return null;
     }
 
     /**
      * Returns a array that represents a result of HTTP request.
      * @param url url
      * @return String content of url
      * @throws HttpException
      * @throws IOException
      */
     public String openURL(String url) throws IOException {
 
         ILuntbuild luntbuild = connect();
         if (luntbuild == null) return new String();
 
         InputStream input = null;
         byte[] buffer = new byte[10000];
         try {
             URL urlObj = new URL(url);
             URLConnection urlConnection = urlObj.openConnection();
             String cookie = this.connectionData.getUser() + ":" + this.connectionData.getPassword();
             byte[] encArr = Base64.encodeBase64(cookie.getBytes());
             String enc = new String(encArr);
             urlConnection.setRequestProperty("Authorization", "Basic " + enc);
             input = urlConnection.getInputStream();
             int data;
             int i = 0;
             while((data = input.read()) > -1) {
                 buffer[i++] = (byte)data;
                 if (i == buffer.length) {
                     byte[] tmpbuf = new byte[buffer.length * 2];
                     System.arraycopy(buffer, 0, tmpbuf, 0, buffer.length);
                     buffer = tmpbuf;
                 }
             }
             byte[] tmpbuf = new byte[i];
             System.arraycopy(buffer, 0, tmpbuf, 0, i);
             buffer = tmpbuf;
         } catch (Exception ex) {
             LuntclipsePlugin.doLog(IStatus.ERROR, IStatus.OK, "Cannot access URL: " + url, ex);
             this.errorMessages.add(new ErrorMessage("Cannot access URL: " + url));
         }finally{
             if (input != null) input.close();
         }
 
         return new String(buffer);
     }
 
     /**
      * Login to Luntbuild.
      */
     private void login() {
         this.client = new HttpClient();
 
         String loginURL = this.connectionData.getUrl();
 
         if (getVersion() <= LuntclipseConstants.getVersion(LuntclipseConstants.LUNTBUILD_VERSION_121)) {
             loginURL += "/j_acegi_security_check?j_username=" + this.connectionData.getUser() +
             "&j_password=" + this.connectionData.getPassword();
         } else {
             loginURL += "/j_acegi_security_check.do?j_username=" + this.connectionData.getUser() +
             "&j_password=" + this.connectionData.getPassword();
         }
 
         int numRetries = 5;
 
         //login first, we need valid session always!
         GetMethod method = new GetMethod(loginURL);
         Object result[] = null;
         //Provide custom retry handler is necessary
         method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                 new DefaultHttpMethodRetryHandler(0, false));
         int timeout = 10;
         this.client.getParams().setParameter("http.socket.timeout", new Integer(timeout * 1000));
         this.client.getParams().setParameter("http.connection.timeout", new Integer(timeout * 1000));
 
         for (int i = 0; i < numRetries; i++) {
             try{
                   int httpStatus = this.client.executeMethod(method);
                   byte response[] = null;
                   result = new Object[]{new Integer(httpStatus), response};
                   break;
             } catch (IOException e) {
                 if (e.getMessage().contains("No route to host")) break;
             } finally {
                   method.releaseConnection();
             }
         }
         //check returned HTTP status code
         if (((Integer)result[0]).intValue() != HttpStatus.SC_OK){
             this.errorMessages.add(
             		new ErrorMessage("Cannot login into Lutbuild! Please verify connection URL and Luntbuild version."));
             throw new RuntimeException(
                     "Cannot login into Lutbuild! Please verify connection URL and Luntbuild version.");
         }
     }
 
     /** Returns true if connected to Luntbuild
      * @return true if connected to Luntbuild
      */
     public boolean isConnected() {
         return connect() != null;
     }
 
     /**
      * @return Returns the connectionData.
      */
     public final ConnectionData getConnectionData() {
         return this.connectionData;
     }
 
     /**
      * @param connectionData The connectionData to set.
      */
     public final void setConnectionData(ConnectionData connectionData) {
         this.connectionData = connectionData;
     }
 
     /**
      * @return decoded version of connection as int
      */
     public final int getVersion() {
         return LuntclipseConstants.getVersion(this.connectionData.getVersion());
     }
 
     /**
      * Loads build data as Build[] from Luntbuild
      */
     public void loadBuildData() {
     	if (this.connectionData.isPaused()) return;
 
         ILuntbuild luntbuild = connect();
         if (luntbuild == null) {
             luntbuildData = null;
             return;
         }
 
         List buildInfoModel = new ArrayList();
         Build messenger;
 
         if(luntbuild == null) {
             messenger = new Build();
             messenger.setProjectName("Unable to connect to Luntbuild!");
         	this.errorMessages.add(new ErrorMessage("", "", "Unable to connect to Luntbuild!"));
             buildInfoModel.add(messenger);
 
             luntbuildData = new TreeMap();
             return;
         }
 
         List projects;
         try{
             projects = luntbuild.getAllProjects();
         }catch(Exception e){
             LuntclipsePlugin.doLog(IStatus.WARNING, IStatus.OK, e.getMessage(), e);
         	this.errorMessages.add(new ErrorMessage("", "", e.getMessage()));
             messenger = new Build();
             messenger.setProjectName("Unable to connect to Luntbuild!");
             buildInfoModel.add(messenger);
 
             luntbuildData = new TreeMap();
             return;
         }
 
         String projectName = "";
         List schedules;
         BuildFacade bf;
         TreeMap projectsMap = new TreeMap();
 
         for (Iterator iter = projects.iterator(); iter.hasNext();) {
             Object o = iter.next();
             if(o instanceof Map){
                 projectName = (String) ((Map)o).get("name");
             }else{
                 projectName = ((ProjectFacade)o).getName();
             }
 
             try {
             	projectsMap.put(projectName, null);
             } catch (Exception e) {
             	this.errorMessages.add(new ErrorMessage(projectName, e.getMessage()));
 			}
             buildInfoModel = new ArrayList();
 
             schedules = luntbuild.getAllSchedulesOfProject(projectName);
             for (Iterator iterator = schedules.iterator(); iterator.hasNext();) {
                 String scheduleName = "";
                 Object _o = iterator.next();
                 if(_o instanceof Map){
                     scheduleName = (String) ((Map)_o).get("name");
                 }else{
                     scheduleName = ((ScheduleFacade)_o).getName();
                 }
 
             	try {
 	                ScheduleFacade schedule =
 	                    luntbuild.getScheduleByName(projectName, scheduleName);
 	                messenger = new Build();
 	                messenger.setProjectName(projectName);
 	                messenger.setScheduleName(schedule.getName());
 	                messenger.setScheduleStatus(schedule.getStatus());
 	                messenger.setTriggerType(schedule.getTriggerType());
 
 	                bf = null;
 	                try {
 	                    if (getVersion() <=
 	                        LuntclipseConstants.getVersion(LuntclipseConstants.LUNTBUILD_VERSION_12)) {
 	                        bf = luntbuild.getLastBuild(schedule);
 	                    } else {
 	                        bf = luntbuild.getLastBuild(projectName, schedule.getName());
 	                    }
 	                } catch (Exception ex) {
 	                    LuntclipsePlugin.doLog(IStatus.ERROR, IStatus.OK,
 	                            "Cannot get last build for " + projectName + "/" + schedule.getName() +
 	                            " for connection " + getConnectionData().getName(), ex);
 	                	this.errorMessages.add(new ErrorMessage(projectName, scheduleName, ex.getMessage()));
 	                    bf = null;
 	                }
 
 	                if(bf != null){
 	                	boolean changed = false;
 	                	if (!this.isFirstLoad)
 	                		changed = checkNotification(projectName, scheduleName, schedule, bf);
 	                    messenger.setLastBuildStatus(bf.getStatus());
 	                    messenger.setVersion(bf.getVersion());
 	                    messenger.setBuildLogUrl(bf.getBuildLogUrl());
 	                    messenger.setRevisionLogUrl(bf.getRevisionLogUrl());
 	                    messenger.setDetailUrl(bf.getUrl());
 	                    try {
 	                        messenger.setSystemLogUrl(bf.getSystemLogUrl());
 	                    } catch (Exception e) {
 	                        // ignore for earlier versions
 	                    }
 	                    if (changed) this.lastBuild = messenger;
 	                }
 
 	                Date statusDate = schedule.getStatusDate();
 	                if(statusDate != null){
 	                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
 	                    messenger.setStatusDate(sdf.format(statusDate));
 	                }
 	                this.previousBuilds.put(projectName + scheduleName, messenger);
 	                buildInfoModel.add(messenger);
                 } catch (Exception e) {
                 	this.errorMessages.add(new ErrorMessage(projectName, scheduleName, e.getMessage()));
                 }
             }
             projectsMap.put(projectName, buildInfoModel);
         }
         this.isFirstLoad = false;
         luntbuildData = projectsMap;
     }
 
     private boolean checkNotification(String projectName, String scheduleName, ScheduleFacade scheduleFacade, BuildFacade buildFacade) {
     	Build previousBuild = getPreviousBuild(projectName, scheduleName);
         if (previousBuild == null ||
         		(previousBuild.getLastBuildStatus() != buildFacade.getStatus()) ||
         		(previousBuild.getScheduleStatus() != scheduleFacade.getStatus())) {
             NotificationMessage message = new NotificationMessage();
             message.setUrl(buildFacade.getBuildLogUrl());
             message.setDate(new Date());
 
             message.setContent("Build \"" + projectName + " / " + scheduleName + " / " +
             		buildFacade.getVersion() + "\" is " + LuntclipseConstants.buildStatus[buildFacade.getStatus()]);
             message.setBuildVersion(buildFacade.getVersion());
             if (buildFacade.getStatus() == LuntclipseConstants.BUILD_FAILED)
                 message.setSeverity(SeverityLevel.Error);
             else
                 message.setSeverity(SeverityLevel.Info);
             if (this.connectionData.getNotifyCondition() == NotifyCondition.BuildFinished) {
             	this.newMessages.add(message);
             } else if (this.connectionData.getNotifyCondition() == NotifyCondition.BuildFailed) {
                     if (buildFacade.getStatus() == LuntclipseConstants.BUILD_FAILED)
                     	this.newMessages.add(message);
             } else if (this.connectionData.getNotifyCondition() == NotifyCondition.BuildSucceeded) {
                     if (buildFacade.getStatus() == LuntclipseConstants.BUILD_SUCCESS)
                     	this.newMessages.add(message);
             }
             return true;
         }
         return false;
     }
 
     private Build getPreviousBuild(String projectName, String scheduleName) {
     	return this.previousBuilds.get(projectName + scheduleName);
     }
 
     public Build getLastBuild() {
     		return this.lastBuild;
     }
 
     public void triggerBuild(String projectName, String scheduleName, BuildParams buildParams) {
         ILuntbuild luntbuild = connect();
         if (luntbuild == null) return;
     	luntbuild.triggerBuild(projectName, scheduleName, buildParams);
     }
 
     public List searchBuilds(SearchCriteria criteria) {
         ILuntbuild luntbuild = connect();
         if (luntbuild == null) return new ArrayList();
     	return luntbuild.searchBuilds(criteria, 0, 0);
     }
 
     /**
      * @param build
      * @return true if build is running
      */
     public boolean isBuildRunning(Build build) {
         ILuntbuild luntbuild = connect();
         if (luntbuild == null) return false;
         ScheduleFacade schedule =
             luntbuild.getScheduleByName(build.getProjectName(), build.getScheduleName());
         BuildFacade bf = null;
         try {
             if (getVersion() <=
                 LuntclipseConstants.getVersion(LuntclipseConstants.LUNTBUILD_VERSION_12)) {
                 bf = luntbuild.getLastBuild(schedule);
             } else {
                 bf = luntbuild.getLastBuild(build.getProjectName(), schedule.getName());
             }
         } catch (Exception ex) {
             LuntclipsePlugin.doLog(IStatus.ERROR, IStatus.OK,
                     "Cannot get last build for " + build.getProjectName() + "/" + schedule.getName() +
                     " for connection " + getConnectionData().getName(), ex);
             this.errorMessages.add(
             		new ErrorMessage(
             				build.getProjectName(),
             				schedule.getName(),
             				build.getVersion(),
             				"Cannot get last build for connection " + getConnectionData().getName()));
             bf = null;
         }
         return bf.getStatus() == Constants.SCHEDULE_STATUS_RUNNING;
     }
 
     /**
      * @param projectName
      * @param scheduleName
      * @param condition
      * @return list of
      */
     public List searchBuilds(String projectName, String scheduleName, SearchCriteria condition) {
         ILuntbuild luntbuild = connect();
         if (luntbuild == null) return null;
 
         ScheduleFacade schedule = luntbuild.getScheduleByName(projectName, scheduleName);
         condition.setScheduleIds(new long[]{schedule.getId()});
         List builds = luntbuild.searchBuilds(condition, 0, 0);
 
         return  builds;
     }
 
     /**
      * @param build to delete
      */
     public void deleteBuild(BuildFacade build) {
         ILuntbuild luntbuild = connect();
         if (luntbuild == null) return;
 
         try {
             luntbuild.deleteBuild(build);
         } catch (Exception e) {
             LuntclipsePlugin.doLog(IStatus.INFO, IStatus.OK,
                     "Delete build not supported for Luntbuild version lower than 1.3 " +
                     "or you are not authorized to delete builds", null);
             this.errorMessages.add(new ErrorMessage("Delete build not supported for Luntbuild version lower than 1.3 " +
                             "or you are not authorized to delete builds"));
         }
     }
 
     /**
      * @param build to delete
      * @param projectName
      * @param scheduleName
      */
     public void moveBuild(BuildFacade build, String projectName, String scheduleName) {
         ILuntbuild luntbuild = connect();
         if (luntbuild == null) return;
 
         try {
             luntbuild.moveBuild(build, projectName, scheduleName);
         } catch (Exception e) {
             LuntclipsePlugin.doLog(IStatus.INFO, IStatus.OK,
                     "Move build not supported for Luntbuild version lower than 1.3. " +
                     "or you are not authorized to move builds", null);
             this.errorMessages.add(new ErrorMessage("Move build not supported for Luntbuild version lower than 1.3. " +
                     "or you are not authorized to move builds"));
 		}
     }
 
     /** Get a schedule by name for givewn project
      * @param projectName
      * @param scheduleName
      * @return ScheduleFacade
      */
     public ScheduleFacade getScheduleFacade(String projectName, String scheduleName) {
         ILuntbuild luntbuild = connect();
         if (luntbuild == null) return null;
         return luntbuild.getScheduleByName(projectName, scheduleName);
     }
 
     /**
      * @param projectName
      * @param scheduleName
      * @return schedule id or -1
      */
     public long getScheduleId(String projectName, String scheduleName) {
         ILuntbuild luntbuild = connect();
         if (luntbuild == null) return -1;
         ScheduleFacade schedule = luntbuild.getScheduleByName(projectName, scheduleName);
         if (schedule == null) return -1;
         return schedule.getId();
     }
 
     /**
      * @return luntbuild data
      */
     public Map getLuntbuildData() {
         return luntbuildData;
     }
 
     /**
      * @return true if Luntbuild build data is available
      */
     public boolean isDataAvailable() {
         return luntbuildData != null && !luntbuildData.isEmpty();
     }
 
     /** Returns true if connection user can create project.
      * @return true if connection user can create project.
      *
      * @since Luntbuild 1.3
      */
     public boolean canCreateProject() {
         ILuntbuild luntbuild = connect();
         if (luntbuild == null) return false;
         try {
             return luntbuild.canCreateProject(this.connectionData.getUser());
         } catch (Exception e) {
             return false;
         }
     }
 
     /** Returns list of users or empty list,
      * @return list of users or empty list
      *
      * @since Luntbuild 1.3
      */
     public List getUsers() {
         ILuntbuild luntbuild = connect();
         if (luntbuild == null) return new ArrayList();
         try {
             return luntbuild.getUsers();
         } catch (Exception e) {
             this.errorMessages.add(new ErrorMessage("Get users not supported for Luntbuild version lower than 1.3. " +
             	"or you are not authorized to get users"));
             return new ArrayList();
         }
      }
 
     /**
      * @return string array of project/schedule
      */
     public String[] getAllSchedules() {
         ILuntbuild luntbuild = connect();
         if (luntbuild == null || luntbuildData == null) return new String[] {};
 
         ArrayList schedules = new ArrayList();
         for (Iterator iter = luntbuildData.values().iterator(); iter.hasNext();) {
             List list = (List) iter.next();
             if (list == null) continue;
             for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                 Build bm = (Build) iterator.next();
                 schedules.add(bm.getProjectName() + "/" + bm.getScheduleName());
             }
         }
 
         return (String[])schedules.toArray(new String[schedules.size()]);
     }
 
     /**
      * @param name project name
      * @return true if project name exist
      */
     public boolean projectExist(String name) {
         ILuntbuild luntbuild = connect();
         if (luntbuild == null || luntbuildData == null) return false;
 
         return luntbuildData.containsKey(name);
     }
 
     /** Creates project
      * @param basicData basic project data
      * @param vcsData  list of VcsProjectData
      * @param builderData list of BuilderProjectData
      * @param scheduleData list of ScheduleProjectData
      */
     public void createProject(BasicProjectData basicData, List vcsData,
             List builderData, List scheduleData) {
         ILuntbuild luntbuild = connect();
         if (luntbuild == null) return;
 
         ProjectFacade project = null;
         try {
             project = luntbuild.getProjectByName(basicData.getName());
         } catch (Exception e) {
             project = null;
         }
         if (project != null) {
             editProject(basicData, vcsData, builderData, scheduleData);
             return;
         }
         project = new ProjectFacade();
 
         // Set data
         project.setName(basicData.getName());
         project.setDescription(basicData.getDescription());
         project.setVariables(basicData.getVariables());
         project.setLogLevel(basicData.getLogLevel());
 
         project.setNotifiers(basicData.getNotifyWithList());
         project.setNotifyUsers(basicData.getNotifyWho());
 
         project.setProjectAdmins(basicData.getAdmins());
         project.setProjectBuilders(basicData.getBuilders());
         project.setProjectViewers(basicData.getViewers());
 
         setVcs(project, vcsData);
         setBuilders(project, builderData);
         setSchedules(project, scheduleData);
 
         luntbuild.createProject(project);
     }
 
     /** Edits project
      * @param basicData basic project data
      * @param vcsData  list of VcsProjectData
      * @param builderData list of BuilderProjectData
      * @param scheduleData list of ScheduleProjectData
      */
     public void editProject(BasicProjectData basicData, List vcsData,
             List builderData, List scheduleData) {
         ILuntbuild luntbuild = connect();
         if (luntbuild == null) return;
         ProjectFacade project = null;
         try {
             project = luntbuild.getProjectByName(basicData.getName());
         } catch (Exception e) {
             project = null;
         }
         if (project == null) {
             createProject(basicData, vcsData, builderData, scheduleData);
             return;
         }
 
         // Set data
         project.setName(basicData.getName());
         project.setDescription(basicData.getDescription());
         project.setVariables(basicData.getVariables());
         project.setLogLevel(basicData.getLogLevel());
 
         project.setNotifiers(basicData.getNotifyWithList());
         project.setNotifyUsers(basicData.getNotifyWho());
 
         project.setProjectAdmins(basicData.getAdmins());
         project.setProjectBuilders(basicData.getBuilders());
         project.setProjectViewers(basicData.getViewers());
 
         project.getVcsList().clear();
         setVcs(project, vcsData);
         project.getBuilderList().clear();
         setBuilders(project, builderData);
         luntbuild.deleteAllSchedulesOfProject(project.getName());
         setSchedules(project, scheduleData);
 
         luntbuild.saveProject(project);
     }
 
     /** Deletes a project
      * @param projectName
      */
     public void deleteProject(String projectName) {
         if (projectName == null || projectName.trim().length() == 0) return;
         ILuntbuild luntbuild = connect();
         if (luntbuild == null) return;
         luntbuild.deleteProject(projectName);
     }
 
     /** Creates a project from data
      * @param project
      */
     public void createProject(List project) {
         createProject((BasicProjectData)project.get(0), (List)project.get(1),
                 (List)project.get(2), (List)project.get(3));
     }
 
     /** Create copy of project
      * @param projectName
      */
     public void copyProject(String projectName) {
         if (projectName == null || projectName.trim().length() == 0) return;
         List allData = getProjectData(projectName);
         LuntclipsePlugin.setBuildClipboard(allData);
     }
 
     /** Get data for project
      * @param projectName
      * @return list of BasicProjectData, VcsProjectData list, BuilderProjectData list, ScheduleProjectData list
      */
     public List getProjectData(String projectName) {
         ILuntbuild luntbuild = connect();
         if (luntbuild == null) return new ArrayList();
         ProjectFacade project = null;
         try {
             project = luntbuild.getProjectByName(projectName);
         } catch (Exception e) {
             project = null;
         }
         if (project == null) return new ArrayList();
 
         BasicProjectData basicData = new BasicProjectData();
         ArrayList vcsData = new ArrayList();
         ArrayList builderData = new ArrayList();
         ArrayList scheduleData = new ArrayList();
         // Set data
         basicData.setName(project.getName());
         basicData.setDescription(project.getDescription());
         basicData.setVariables(project.getVariables());
         basicData.setLogLevel(project.getLogLevel());
 
         basicData.setNotifyWithList(project.getNotifiers());
         basicData.setNotifyWho(project.getNotifyUsers());
 
         basicData.setAdmins(project.getProjectAdmins());
         basicData.setBuilders(project.getProjectBuilders());
         basicData.setViewers(project.getProjectViewers());
 
         setVcs(project.getVcsList(), vcsData);
         setBuilders(project.getBuilderList(), builderData);
         setSchedules(project.getName(), scheduleData);
 
         ArrayList allData = new ArrayList();
         allData.add(basicData);
         allData.add(vcsData);
         allData.add(builderData);
         allData.add(scheduleData);
 
         return allData;
     }
 
     private void setVcs(ProjectFacade project, List vcsData) {
         if (vcsData == null || project == null) return;
         for (Iterator iterator = vcsData.iterator(); iterator.hasNext();) {
             VcsProjectData data = (VcsProjectData)iterator.next();
 
             int type = data.getType();
             if (type == LuntclipseConstants.ACCUREV_ADAPTOR) {
                 AccurevAdaptorFacade vcs = new AccurevAdaptorFacade();
                 vcs.setQuietPeriod(data.getQuietPeriod());
                 vcs.setUser(data.getUser());
                 vcs.setPassword(data.getPassword());
                 addModules(vcs, data);
                 project.getVcsList().add(vcs);
             } else if (type == LuntclipseConstants.BASE_CLEARCASE_ADAPTOR) {
                 BaseClearcaseAdaptorFacade vcs = new BaseClearcaseAdaptorFacade();
                 vcs.setQuietPeriod(data.getQuietPeriod());
                 vcs.setViewStgLoc(data.getClearcaseViewStgloc());
                 vcs.setVws(data.getClearcaseViewStorage());
                 vcs.setViewCfgSpec(data.getClearcaseConfig());
                 vcs.setModificationDetectionConfig(data.getClearcaseModConfig());
                 vcs.setMkviewExtraOpts(data.getClearcaseViewOptions());
                 vcs.setCleartoolDir(data.getExePath());
                 addModules(vcs, data);
                 project.getVcsList().add(vcs);
             } else if (type == LuntclipseConstants.CVS_ADAPTOR) {
                 CvsAdaptorFacade vcs = new CvsAdaptorFacade();
                 vcs.setQuietPeriod(data.getQuietPeriod());
                 vcs.setCvsRoot(data.getCvsRoot());
                 vcs.setCvsPassword(data.getPassword());
                 vcs.setCygwinCvs(data.getCvsCygwin() == 0 ? "yes" : "no");
                 vcs.setDisableSuppressOption(data.getCvsLogCommand() == 0 ? "yes" : "no");
                 vcs.setDisableHistoryCmd(data.getCvsHistroy() == 0 ? "yes" : "no");
                 vcs.setCvsDir(data.getExePath());
                 addModules(vcs, data);
                 project.getVcsList().add(vcs);
             } else if (type == LuntclipseConstants.FILESYSTEM_ADAPTOR) {
                 FileSystemAdaptorFacade vcs = new FileSystemAdaptorFacade();
                 vcs.setQuietPeriod(data.getQuietPeriod());
                 vcs.setSourceDir(data.getFilesystemSource());
                 project.getVcsList().add(vcs);
             } else if (type == LuntclipseConstants.PERFORCE_ADAPTOR) {
                 PerforceAdaptorFacade vcs = new PerforceAdaptorFacade();
                 vcs.setQuietPeriod(data.getQuietPeriod());
                 vcs.setPort(data.getPerforcePort());
                 vcs.setUser(data.getUser());
                 vcs.setPassword(data.getPassword());
                 vcs.setLineEnd(LuntclipseConstants.perforceLineEnd[data.getPerforceLineEnd()]);
                 data.setExePath(vcs.getP4Dir());
                 addModules(vcs, data);
                 project.getVcsList().add(vcs);
             } else if (type == LuntclipseConstants.STARTEAM_ADAPTOR) {
                 StarteamAdaptorFacade vcs = new StarteamAdaptorFacade();
                 vcs.setQuietPeriod(data.getQuietPeriod());
                 vcs.setProjectLocation(data.getStarTeamLocation());
                 vcs.setUser(data.getUser());
                 vcs.setPassword(data.getPassword());
                 vcs.setConvertEOL(data.getStarTeamConvertEol() == 0 ? "yes" : "no");
                 addModules(vcs, data);
                 project.getVcsList().add(vcs);
             } else if (type == LuntclipseConstants.SUBVERSION_ADAPTOR) {
                 SvnAdaptorFacade vcs = new SvnAdaptorFacade();
                 vcs.setQuietPeriod(data.getQuietPeriod());
                 vcs.setUrlBase(data.getSvnUrl());
                 vcs.setUser(data.getUser());
                 vcs.setPassword(data.getPassword());
                 vcs.setTrunk(data.getSvnTrunkDir());
                 vcs.setBranches(data.getSvnBranchesDir());
                 vcs.setTags(data.getSvnTagsDir());
                 vcs.setSvnDir(data.getExePath());
                 addModules(vcs, data);
                 project.getVcsList().add(vcs);
             } else if (type == LuntclipseConstants.UCM_CLEARCASE_ADAPTOR) {
                 UCMClearcaseAdaptorFacade vcs = new UCMClearcaseAdaptorFacade();
                 vcs.setQuietPeriod(data.getQuietPeriod());
                 vcs.setWhatToBuild(data.getClearcaseBuildTarget());
                 vcs.setViewStgLoc(data.getClearcaseViewStgloc());
                 vcs.setProjectVob(data.getClearcaseVobTag());
                 vcs.setStream(data.getClearcaseStreamName());
                 vcs.setVws(data.getClearcaseViewStorage());
                 vcs.setModificationDetectionConfig(data.getClearcaseModConfig());
                 vcs.setMkviewExtraOpts(data.getClearcaseViewOptions());
                 vcs.setCleartoolDir(data.getExePath());
                 addModules(vcs, data);
                 project.getVcsList().add(vcs);
             } else if (type == LuntclipseConstants.VSS_ADAPTOR) {
                 VssAdaptorFacade vcs = new VssAdaptorFacade();
                 vcs.setQuietPeriod(data.getQuietPeriod());
                 vcs.setVssPath(data.getSourcesafePath());
                 vcs.setVssUser(data.getUser());
                 vcs.setVssPassword(data.getPassword());
                 vcs.setDateTimeFormat(data.getSourcesafeDatetime());
                 vcs.setSsDir(data.getExePath());
                 addModules(vcs, data);
                 project.getVcsList().add(vcs);
             }
         }
     }
 
     private void addModules(VcsFacade vcs, VcsProjectData data) {
         List modules = data.getModules();
         int type = data.getType();
         for (Iterator iterator = modules.iterator(); iterator.hasNext();) {
             if (type == LuntclipseConstants.ACCUREV_ADAPTOR) {
                 AccuRevModuleData moddata= (AccuRevModuleData)iterator.next();
                 AccurevModuleFacade module = new AccurevModuleFacade();
                 module.setBackingStream(moddata.getBackingStream());
                 module.setBuildStream(moddata.getBuildStream());
                 module.setDepot(moddata.getDepot());
                 module.setLabel(moddata.getLabel());
                 module.setSrcPath(moddata.getSrcPath());
                 vcs.getModules().add(module);
             } else if (type == LuntclipseConstants.CVS_ADAPTOR) {
                 CvsModuleData moddata= (CvsModuleData)iterator.next();
                 CvsModuleFacade module = new CvsModuleFacade();
                 module.setBranch(moddata.getBranch());
                 module.setDestPath(moddata.getDestPath());
                 module.setLabel(moddata.getLabel());
                 module.setSrcPath(moddata.getSourcePath());
                 vcs.getModules().add(module);
             } else if (type == LuntclipseConstants.PERFORCE_ADAPTOR) {
                 PerforceModuleData moddata= (PerforceModuleData)iterator.next();
                 PerforceModuleFacade module = new PerforceModuleFacade();
                 module.setClientPath(moddata.getClientPath());
                 module.setDepotPath(moddata.getDepotPath());
                 module.setLabel(moddata.getLabel());
                 vcs.getModules().add(module);
             } else if (type == LuntclipseConstants.STARTEAM_ADAPTOR) {
                 StarTeamModuleData moddata= (StarTeamModuleData)iterator.next();
                 StarteamModuleFacade module = new StarteamModuleFacade();
                 module.setDestPath(moddata.getDestPath());
                 module.setLabel(moddata.getLabel());
                 module.setSrcPath(moddata.getSrcPath());
                 module.setStarteamView(moddata.getStarteamView());
                 vcs.getModules().add(module);
             } else if (type == LuntclipseConstants.SUBVERSION_ADAPTOR) {
                 SubversionModuleData moddata= (SubversionModuleData)iterator.next();
                 SvnModuleFacade module = new SvnModuleFacade();
                 module.setBranch(moddata.getBranch());
                 module.setDestPath(moddata.getDestPath());
                 module.setLabel(moddata.getLabel());
                 module.setSrcPath(moddata.getSrcPath());
                 vcs.getModules().add(module);
             } else if (type == LuntclipseConstants.VSS_ADAPTOR) {
                 VisualSourcesafeModuleData moddata= (VisualSourcesafeModuleData)iterator.next();
                 VssModuleFacade module = new VssModuleFacade();
                 module.setBranch(moddata.getBranch());
                 module.setDestPath(moddata.getDestPath());
                 module.setLabel(moddata.getLabel());
                 module.setSrcPath(moddata.getSrcPath());
                 vcs.getModules().add(module);
             }
         }
     }
 
     private void setVcs(List vcsList, List vcsData) {
         if (vcsData == null || vcsList == null) return;
         for (Iterator iterator = vcsList.iterator(); iterator.hasNext();) {
             VcsFacade vcs = (VcsFacade)iterator.next();
 
             VcsProjectData data = new VcsProjectData();
             data.setQuietPeriod(vcs.getQuietPeriod());
             if (vcs instanceof AccurevAdaptorFacade) {
                 AccurevAdaptorFacade accu = (AccurevAdaptorFacade)vcs;
                 data.setType(LuntclipseConstants.ACCUREV_ADAPTOR);
                 data.setUser(accu.getUser());
                 data.setPassword(accu.getPassword());
             } else if (vcs instanceof BaseClearcaseAdaptorFacade) {
                 BaseClearcaseAdaptorFacade bc = (BaseClearcaseAdaptorFacade)vcs;
                 data.setType(LuntclipseConstants.BASE_CLEARCASE_ADAPTOR);
                 data.setClearcaseViewStgloc(bc.getViewStgLoc());
                 data.setClearcaseViewStorage(bc.getVws());
                 data.setClearcaseConfig(bc.getViewCfgSpec());
                 data.setClearcaseModConfig(bc.getModificationDetectionConfig());
                 data.setClearcaseViewOptions(bc.getMkviewExtraOpts());
                 data.setExePath(bc.getCleartoolDir());
            } else if (vcs instanceof CvsAdaptorFacade) {
                CvsAdaptorFacade cvs = (CvsAdaptorFacade)vcs;
                 data.setType(LuntclipseConstants.CVS_ADAPTOR);
                 data.setCvsRoot(cvs.getCvsRoot());
                 data.setPassword(cvs.getCvsPassword());
                 data.setCvsCygwin(cvs.getCygwinCvs().equalsIgnoreCase("yes") ? 0 : 1);
                 data.setCvsLogCommand(cvs.getDisableSuppressOption().equalsIgnoreCase("yes") ? 0 : 1);
                 data.setCvsHistroy(cvs.getDisableHistoryCmd().equalsIgnoreCase("yes") ? 0 : 1);
                 data.setExePath(cvs.getCvsDir());
             } else if (vcs instanceof FileSystemAdaptorFacade) {
                 FileSystemAdaptorFacade fs = (FileSystemAdaptorFacade)vcs;
                 data.setType(LuntclipseConstants.FILESYSTEM_ADAPTOR);
                 data.setFilesystemSource(fs.getSourceDir());
             } else if (vcs instanceof PerforceAdaptorFacade) {
                 PerforceAdaptorFacade pr = (PerforceAdaptorFacade)vcs;
                 data.setType(LuntclipseConstants.PERFORCE_ADAPTOR);
                 data.setPerforcePort(pr.getPort());
                 data.setUser(pr.getUser());
                 data.setPassword(pr.getPassword());
                 data.setPerforceLineEnd(LuntclipseConstants.perforceLineEndIndex(pr.getLineEnd()));
                 data.setExePath(pr.getP4Dir());
             } else if (vcs instanceof StarteamAdaptorFacade) {
                 StarteamAdaptorFacade st = (StarteamAdaptorFacade)vcs;
                 data.setType(LuntclipseConstants.STARTEAM_ADAPTOR);
                 data.setStarTeamLocation(st.getProjectLocation());
                 data.setUser(st.getUser());
                 data.setPassword(st.getPassword());
                 data.setStarTeamConvertEol(st.getConvertEOL().equalsIgnoreCase("yes") ? 0 : 1);
             } else if (vcs instanceof SvnAdaptorFacade) {
                 SvnAdaptorFacade svn = (SvnAdaptorFacade)vcs;
                 data.setType(LuntclipseConstants.SUBVERSION_ADAPTOR);
                 data.setSvnUrl(svn.getUrlBase());
                 data.setUser(svn.getUser());
                 data.setPassword(svn.getPassword());
                 data.setSvnTrunkDir(svn.getTrunk());
                 data.setSvnBranchesDir(svn.getBranches());
                 data.setSvnTagsDir(svn.getTags());
                 data.setExePath(svn.getSvnDir());
             } else if (vcs instanceof UCMClearcaseAdaptorFacade) {
                 UCMClearcaseAdaptorFacade uc = (UCMClearcaseAdaptorFacade)vcs;
                 data.setType(LuntclipseConstants.UCM_CLEARCASE_ADAPTOR);
                 data.setClearcaseBuildTarget(uc.getWhatToBuild());
                 data.setClearcaseViewStgloc(uc.getViewStgLoc());
                 data.setClearcaseVobTag(uc.getProjectVob());
                 data.setClearcaseStreamName(uc.getStream());
                 data.setClearcaseViewStorage(uc.getVws());
                 data.setClearcaseModConfig(uc.getModificationDetectionConfig());
                 data.setClearcaseViewOptions(uc.getMkviewExtraOpts());
                 data.setExePath(uc.getCleartoolDir());
             } else if (vcs instanceof VssAdaptorFacade) {
                 VssAdaptorFacade vss = (VssAdaptorFacade)vcs;
                 data.setType(LuntclipseConstants.VSS_ADAPTOR);
                 data.setSourcesafePath(vss.getVssPath());
                 data.setUser(vss.getVssUser());
                 data.setPassword(vss.getVssPassword());
                 data.setSourcesafeDatetime(vss.getDateTimeFormat());
                 data.setExePath(vss.getSsDir());
             }
 
             ArrayList dataModules = new ArrayList();
             List modules = vcs.getModules();
             for (Iterator iter = modules.iterator(); iter.hasNext();) {
                 ModuleFacade module = (ModuleFacade) iter.next();
                 if (module instanceof AccurevModuleFacade) {
                     AccuRevModuleData moddata = new AccuRevModuleData();
                     AccurevModuleFacade accu = (AccurevModuleFacade)module;
                     moddata.setBackingStream(accu.getBackingStream());
                     moddata.setBuildStream(accu.getBuildStream());
                     moddata.setDepot(accu.getDepot());
                     moddata.setLabel(accu.getLabel());
                     moddata.setSrcPath(accu.getSrcPath());
                     dataModules.add(moddata);
                 } else if (module instanceof CvsModuleFacade) {
                     CvsModuleData moddata = new CvsModuleData();
                     CvsModuleFacade cvs = (CvsModuleFacade)module;
                     moddata.setBranch(cvs.getBranch());
                     moddata.setDestPath(cvs.getDestPath());
                     moddata.setLabel(cvs.getLabel());
                     moddata.setSourcePath(cvs.getSrcPath());
                     dataModules.add(moddata);
                 } else if (module instanceof PerforceModuleFacade) {
                     PerforceModuleData moddata = new PerforceModuleData();
                     PerforceModuleFacade pr = (PerforceModuleFacade)module;
                     moddata.setClientPath(pr.getClientPath());
                     moddata.setDepotPath(pr.getDepotPath());
                     moddata.setLabel(pr.getLabel());
                     dataModules.add(moddata);
                 } else if (module instanceof StarteamModuleFacade) {
                     StarTeamModuleData moddata = new StarTeamModuleData();
                     StarteamModuleFacade st = (StarteamModuleFacade)module;
                     moddata.setDestPath(st.getDestPath());
                     moddata.setLabel(st.getLabel());
                     moddata.setSrcPath(st.getSrcPath());
                     moddata.setStarteamView(st.getStarteamView());
                     dataModules.add(moddata);
                 } else if (module instanceof SvnModuleFacade) {
                     SubversionModuleData moddata = new SubversionModuleData();
                     SvnModuleFacade svn = (SvnModuleFacade)module;
                     moddata.setBranch(svn.getBranch());
                     moddata.setDestPath(svn.getDestPath());
                     moddata.setLabel(svn.getLabel());
                     moddata.setSrcPath(svn.getSrcPath());
                     dataModules.add(moddata);
                 } else if (module instanceof VssModuleFacade) {
                     VisualSourcesafeModuleData moddata = new VisualSourcesafeModuleData();
                     VssModuleFacade vss = (VssModuleFacade)module;
                     moddata.setBranch(vss.getBranch());
                     moddata.setDestPath(vss.getDestPath());
                     moddata.setLabel(vss.getLabel());
                     moddata.setSrcPath(vss.getSrcPath());
                     dataModules.add(moddata);
                 }
             }
             data.setModules(dataModules);
 
             vcsData.add(data);
         }
     }
 
     private void setBuilders(ProjectFacade project, List buildersData) {
         if (buildersData == null || project == null) return;
         for (Iterator iterator = buildersData.iterator(); iterator.hasNext();) {
             BuilderProjectData data = (BuilderProjectData)iterator.next();;
 
             int type = data.getType();
             if (type == LuntclipseConstants.ANT_BUILDER) {
                 AntBuilderFacade antBuilder =  new AntBuilderFacade();
                 antBuilder.setName(data.getName());
                 antBuilder.setBuildSuccessCondition(data.getCondition());
                 antBuilder.setEnvironments(data.getEnvVars());
                 antBuilder.setBuildProperties(data.getProperties());
                 antBuilder.setBuildScriptPath(data.getScriptPath());
                 antBuilder.setBuildTargets(data.getTragets());
                 antBuilder.setCommand(data.getCommand());
                 project.getBuilderList().add(antBuilder);
             } else if (type == LuntclipseConstants.MAVEN_BUILDER) {
                 MavenBuilderFacade mavenBuilder =  new MavenBuilderFacade();
                 mavenBuilder.setName(data.getName());
                 mavenBuilder.setBuildSuccessCondition(data.getCondition());
                 mavenBuilder.setEnvironments(data.getEnvVars());
                 mavenBuilder.setBuildProperties(data.getProperties());
                 mavenBuilder.setDirToRunMaven(data.getScriptPath());
                 mavenBuilder.setGoals(data.getTragets());
                 mavenBuilder.setCommand(data.getCommand());
                 project.getBuilderList().add(mavenBuilder);
             } else if (type == LuntclipseConstants.MAVEN2_BUILDER) {
                 Maven2BuilderFacade maven2Builder =  new Maven2BuilderFacade();
                 maven2Builder.setName(data.getName());
                 maven2Builder.setBuildSuccessCondition(data.getCondition());
                 maven2Builder.setEnvironments(data.getEnvVars());
                 maven2Builder.setBuildProperties(data.getProperties());
                 maven2Builder.setDirToRunMaven(data.getScriptPath());
                 maven2Builder.setGoals(data.getTragets());
                 maven2Builder.setCommand(data.getCommand());
                 project.getBuilderList().add(maven2Builder);
             } else if (type == LuntclipseConstants.COMMAND_BUILDER) {
                 CommandBuilderFacade cmdBuilder =  new CommandBuilderFacade();
                 cmdBuilder.setName(data.getName());
                 cmdBuilder.setBuildSuccessCondition(data.getCondition());
                 cmdBuilder.setEnvironments(data.getEnvVars());
                 cmdBuilder.setDirToRunCmd(data.getScriptPath());
                 cmdBuilder.setCommand(data.getCommand());
                 cmdBuilder.setWaitForFinish(data.getWaitForFinish() ? "yes" : "no");
                 project.getBuilderList().add(cmdBuilder);
             } else if (type == LuntclipseConstants.RAKE_BUILDER) {
                 RakeBuilderFacade rakeBuilder =  new RakeBuilderFacade();
                 rakeBuilder.setName(data.getName());
                 rakeBuilder.setBuildSuccessCondition(data.getCondition());
                 rakeBuilder.setEnvironments(data.getEnvVars());
                 rakeBuilder.setBuildProperties(data.getProperties());
                 rakeBuilder.setBuildScriptPath(data.getScriptPath());
                 rakeBuilder.setBuildTargets(data.getTragets());
                 rakeBuilder.setCommand(data.getCommand());
                 project.getBuilderList().add(rakeBuilder);
             }
         }
     }
 
     private void setBuilders(List builders, List buildersData) {
         if (buildersData == null || builders == null) return;
         for (Iterator iterator = builders.iterator(); iterator.hasNext();) {
             BuilderFacade builder = (BuilderFacade)iterator.next();
 
             BuilderProjectData data = new BuilderProjectData();
             data.setName(builder.getName());
             data.setCondition(builder.getBuildSuccessCondition());
             data.setEnvVars(builder.getEnvironments());
             if (builder instanceof AntBuilderFacade) {
                 AntBuilderFacade antBuilder = (AntBuilderFacade) builder;
                 data.setType(LuntclipseConstants.ANT_BUILDER);
                 data.setProperties(antBuilder.getBuildProperties());
                 data.setScriptPath(antBuilder.getBuildScriptPath());
                 data.setTragets(antBuilder.getBuildTargets());
                 data.setCommand(antBuilder.getCommand());
             } else if (builder instanceof MavenBuilderFacade) {
                 MavenBuilderFacade mavenBuilder = (MavenBuilderFacade) builder;
                 data.setType(LuntclipseConstants.MAVEN_BUILDER);
                 data.setProperties(mavenBuilder.getBuildProperties());
                 data.setScriptPath(mavenBuilder.getDirToRunMaven());
                 data.setTragets(mavenBuilder.getGoals());
                 data.setCommand(mavenBuilder.getCommand());
             } else if (builder instanceof Maven2BuilderFacade) {
                 Maven2BuilderFacade maven2Builder = (Maven2BuilderFacade) builder;
                 data.setType(LuntclipseConstants.MAVEN2_BUILDER);
                 data.setProperties(maven2Builder.getBuildProperties());
                 data.setScriptPath(maven2Builder.getDirToRunMaven());
                 data.setTragets(maven2Builder.getGoals());
                 data.setCommand(maven2Builder.getCommand());
             } else if (builder instanceof CommandBuilderFacade) {
                 CommandBuilderFacade cmdBuilder = (CommandBuilderFacade) builder;
                 data.setType(LuntclipseConstants.COMMAND_BUILDER);
                 data.setScriptPath(cmdBuilder.getDirToRunCmd());
                 data.setCommand(cmdBuilder.getCommand());
                 data.setWaitForFinish(cmdBuilder.getWaitForFinish().equalsIgnoreCase("yes"));
             } else if (builder instanceof RakeBuilderFacade) {
                 RakeBuilderFacade rakeBuilder = (RakeBuilderFacade) builder;
                 data.setType(LuntclipseConstants.RAKE_BUILDER);
                 data.setProperties(rakeBuilder.getBuildProperties());
                 data.setScriptPath(rakeBuilder.getBuildScriptPath());
                 data.setTragets(rakeBuilder.getBuildTargets());
                 data.setCommand(rakeBuilder.getCommand());
             }
 
             buildersData.add(data);
         }
     }
 
     private void setSchedules(ProjectFacade project, List scheduleData) {
         ILuntbuild luntbuild = connect();
         if (luntbuild == null) return;
         if (project == null || scheduleData == null) return;
         List allSchedules = luntbuild.getAllSchedules();
         ArrayList scheduleList = new ArrayList();
 
         for (Iterator iterator = scheduleData.iterator(); iterator.hasNext();) {
             ScheduleProjectData data = (ScheduleProjectData)iterator.next();
             ScheduleFacade schedule = new ScheduleFacade();
 
             schedule.setProjectId(project.getId());
             schedule.setName(data.getName());
             schedule.setDescription(data.getDescription());
             schedule.setBuildCleanupStrategy(data.getBuildCleanup());
             schedule.setBuildCleanupStrategyData(data.getBuildCleanupData());
             schedule.setBuildNecessaryCondition(data.getBuildCondition());
             String[] builders = data.getBuilders();
             ArrayList list = new ArrayList();
             for (int i = 0; i < builders.length; i++) {
                 String builder = builders[i];
                 list.add(builder);
             }
             schedule.setAssociatedBuilderNames(list);
             schedule.setBuildType(data.getBuildType());
             String[] depScheduleArr = data.getDependentSchedules();
             List depIds = new ArrayList();
             for (int i = 0; i < depScheduleArr.length; i++) {
                 String name = depScheduleArr[i];
                 name = name.substring(name.indexOf("/") + 1);
                 depIds.add(Long.valueOf(getScheduleId(allSchedules, name)));
             }
             schedule.setDependentScheduleIds(depIds);
             schedule.setDependentScheduleIds(data.getDependentScheduleIds());
             schedule.setLabelStrategy(data.getLabelStrategy());
             schedule.setNextVersion(data.getNextBuildVersion());
             schedule.setNotifyStrategy(data.getNotifyStrategy());
             String[] postBuilderArr = data.getPostBuilders();
             List postBuilders = new ArrayList();
             for (int j = 0; j < postBuilderArr.length; j++) {
                 String builder = postBuilderArr[j];
                 postBuilders.add(builder);
             }
             schedule.setAssociatedPostbuilderNames(postBuilders);
             schedule.setPostbuildStrategy(data.getPostBuildStrategy());
             schedule.setTriggerType(data.getTrigerType());
             int type = schedule.getTriggerType();
             if (type == LuntclipseConstants.TRIGGER_TYPE_SIMPLE)
                 schedule.setRepeatInterval(Long.parseLong(data.getTriggerData()));
             else if (type == LuntclipseConstants.TRIGGER_TYPE_CRON)
                 schedule.setCronExpression(data.getTriggerData());
             schedule.setWorkingPath(data.getWorkDirectory());
             schedule.setStatus(data.getScheduleStatus());
             SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
             try {
                 schedule.setStatusDate(sdf.parse(data.getStatusDate()));
             } catch (Exception e) {
                 // do not set
             }
 
             scheduleList.add(schedule);
         }
         project.setScheduleList(scheduleList);
     }
 
     private void setSchedules(String projectName, List scheduleData) {
         ILuntbuild luntbuild = connect();
         if (luntbuild == null) return;
         if (projectName == null || scheduleData == null) return;
         List schedules = luntbuild.getAllSchedulesOfProject(projectName);
         List allSchedules = luntbuild.getAllSchedules();
 
         for (Iterator iterator = schedules.iterator(); iterator.hasNext();) {
             String scheduleName;
             Object _o = iterator.next();
             if(_o instanceof Map){
                 scheduleName = (String) ((Map)_o).get("name");
             }else{
                 scheduleName = ((ScheduleFacade)_o).getName();
             }
 
             ScheduleFacade schedule =
                 luntbuild.getScheduleByName(projectName, scheduleName);
             ScheduleProjectData data = new ScheduleProjectData();
             data.setName(schedule.getName());
             data.setDescription(schedule.getDescription());
             data.setBuildCleanup(schedule.getBuildCleanupStrategy());
             data.setBuildCleanupData(schedule.getBuildCleanupStrategyData());
             data.setBuildCondition(schedule.getBuildNecessaryCondition());
             List builders = schedule.getAssociatedBuilderNames();
             data.setBuilders((String[])builders.toArray(new String[builders.size()]));
             data.setBuildType(schedule.getBuildType());
             List depIds = schedule.getDependentScheduleIds();
             String[] depScheduleArr = new String[depIds.size()];
             int i = 0;
             for (Iterator iter = depIds.iterator(); iter.hasNext();) {
                 Long id = (Long) iter.next();
                 depScheduleArr[i++] = projectName + "/" + getScheduleName(allSchedules, id.longValue());
             }
             data.setDependentSchedules(depScheduleArr);
             data.setDependentScheduleIds(schedule.getDependentScheduleIds());
             data.setLabelStrategy(schedule.getLabelStrategy());
             data.setNextBuildVersion(schedule.getNextVersion());
             data.setNotifyStrategy(schedule.getNotifyStrategy());
             List postBuilders = schedule.getAssociatedPostbuilderNames();
             data.setPostBuilders((String[])postBuilders.toArray(new String[postBuilders.size()]));
             data.setPostBuildStrategy(schedule.getPostbuildStrategy());
             data.setTrigerType(schedule.getTriggerType());
             int type = schedule.getTriggerType();
             if (type == LuntclipseConstants.TRIGGER_TYPE_SIMPLE)
                 data.setTriggerData(Long.toString(schedule.getRepeatInterval()));
             else if (type == LuntclipseConstants.TRIGGER_TYPE_CRON)
                 data.setTriggerData(schedule.getCronExpression());
             data.setWorkDirectory(schedule.getWorkingPath());
             data.setScheduleStatus(schedule.getStatus());
 
             Date statusDate = schedule.getStatusDate();
             if(statusDate != null){
                 SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
                 data.setStatusDate(sdf.format(statusDate));
             }
 
             scheduleData.add(data);
         }
     }
 
     private String getScheduleName(List schedules, long id) {
         for (Iterator iterator = schedules.iterator(); iterator.hasNext();) {
             long scheduleId;
             Object _o = iterator.next();
             if(_o instanceof Map){
                 scheduleId = ((Long)((Map)_o).get("id")).longValue();
             }else{
                 scheduleId = ((ScheduleFacade)_o).getId();
             }
             if (scheduleId == id) {
                 String scheduleName;
                 if(_o instanceof Map){
                     scheduleName = (String) ((Map)_o).get("name");
                 }else{
                     scheduleName = ((ScheduleFacade)_o).getName();
                 }
                 return scheduleName;
             }
         }
         return "";
     }
 
     private long getScheduleId(List schedules, String name) {
         for (Iterator iterator = schedules.iterator(); iterator.hasNext();) {
             long scheduleId;
             String scheduleName;
             Object _o = iterator.next();
             if(_o instanceof Map){
                 scheduleName = (String)((Map)_o).get("name");
             }else{
                 scheduleName = ((ScheduleFacade)_o).getName();
             }
             if (scheduleName.equals(name)) {
                 if(_o instanceof Map){
                     scheduleId = ((Long)((Map)_o).get("id")).longValue();
                 }else{
                     scheduleId = ((ScheduleFacade)_o).getId();
                 }
                 return scheduleId;
             }
         }
         return -1;
     }
 
 	/**
 	 * @return Returns the newMessages.
 	 */
 	public final ArrayList<NotificationMessage> getErrorMessages() {
 		if (errorMessages == null) {
 			errorMessages = new ArrayList<NotificationMessage>();
 			return new ArrayList<NotificationMessage>();
 		}
 		ArrayList<NotificationMessage> msgs = new ArrayList<NotificationMessage>(errorMessages);
 		errorMessages = new ArrayList<NotificationMessage>();
 		return msgs;
 	}
 
 	/**
 	 * @return Returns the newMessages.
 	 */
 	public final ArrayList<NotificationMessage> getNewMessages() {
 		if (newMessages == null) {
 			newMessages = new ArrayList<NotificationMessage>();
 			return new ArrayList<NotificationMessage>();
 		}
 		ArrayList<NotificationMessage> msgs = new ArrayList<NotificationMessage>(newMessages);
 		newMessages = new ArrayList<NotificationMessage>();
 		return msgs;
 	}
 
 	/**
 	 * @return Returns the viewer.
 	 */
 	public final LuntbuildViewer getViewer() {
 		return viewer;
 	}
 
 	/**
 	 * @param viewer The viewer to set.
 	 */
 	public final void setViewer(LuntbuildViewer viewer) {
 		this.viewer = viewer;
 	}
 
 }
