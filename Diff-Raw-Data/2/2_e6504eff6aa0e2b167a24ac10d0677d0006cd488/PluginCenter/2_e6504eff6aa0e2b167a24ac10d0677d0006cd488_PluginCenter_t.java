 /**
  * *****************************************************************************
  *
  * Copyright (c) 2012 Oracle Corporation.
  *
  * All rights reserved. This program and the accompanying materials are made
  * available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *
  * Winston Prakash
  *
  ******************************************************************************
  */
 package org.eclipse.hudson.plugins;
 
 import hudson.ProxyConfiguration;
 import hudson.Util;
 import hudson.markup.MarkupFormatter;
 import hudson.security.Permission;
 import hudson.util.DaemonThreadFactory;
 import hudson.util.VersionNumber;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.*;
 import java.util.concurrent.*;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.FilenameUtils;
 import org.eclipse.hudson.plugins.InstalledPluginManager.InstalledPluginInfo;
 import org.eclipse.hudson.plugins.UpdateSiteManager.AvailablePluginInfo;
 import org.eclipse.hudson.security.HudsonSecurityEntitiesHolder;
 import org.eclipse.hudson.security.HudsonSecurityManager;
 import org.kohsuke.stapler.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Plugin center for installing, updating and disabling plugins
  *
  * @since 3.0.0
  * @author Winston Prakash
  */
 final public class PluginCenter {
 
     private Logger logger = LoggerFactory.getLogger(PluginCenter.class);
     private File pluginsDir;
     private UpdateSiteManager updateSiteManager;
     private InstalledPluginManager installedPluginManager;
     private ProxyConfiguration proxyConfig;
     List<PluginInstallationJob> installationsJobs = new CopyOnWriteArrayList<PluginInstallationJob>();
     private ExecutorService installerService = Executors.newSingleThreadExecutor(
             new DaemonThreadFactory(new ThreadFactory() {
         @Override
         public Thread newThread(Runnable r) {
             Thread t = new Thread(r);
             t.setName("Update center installer thread");
             return t;
         }
     }));
     private HudsonSecurityManager hudsonSecurityManager;
     private File hudsonHomeDir;
 
     public PluginCenter(File homeDir) throws MalformedURLException, IOException {
         hudsonHomeDir = homeDir;
         pluginsDir = new File(hudsonHomeDir, "plugins");
         if (!pluginsDir.exists()) {
             pluginsDir.mkdirs();
         }
         hudsonSecurityManager = HudsonSecurityEntitiesHolder.getHudsonSecurityManager();
         proxyConfig = new ProxyConfiguration(homeDir);
         updateSiteManager = new UpdateSiteManager("default", hudsonHomeDir, proxyConfig);
         installedPluginManager = new InstalledPluginManager(pluginsDir);
     }
 
     public boolean needsAdminLogin() {
         return !hudsonSecurityManager.hasPermission(Permission.HUDSON_ADMINISTER);
     }
 
     public ProxyConfiguration getProxyConfig() {
         return proxyConfig;
     }
 
     public UpdateSiteManager getUpdateSiteManager() {
         return updateSiteManager;
     }
 
     public List<AvailablePluginInfo> getAvailablePlugins(String pluginType) {
         return updateSiteManager.getAvailablePlugins(pluginType);
     }
 
     public List<AvailablePluginInfo> getCategorizedAvailablePlugins(String pluginType, String category) {
         return updateSiteManager.getCategorizedAvailablePlugins(pluginType, category);
     }
 
     public MarkupFormatter getMarkupFormatter() {
         return hudsonSecurityManager.getMarkupFormatter();
     }
 
     public List<AvailablePluginInfo> getInstalledPlugins() {
         List<AvailablePluginInfo> installedPlugins = new ArrayList<AvailablePluginInfo>();
         Set<String> installedPluginNames = installedPluginManager.getInstalledPluginNames();
         Set<String> availablePluginNames = updateSiteManager.getAvailablePluginNames();
         for (String pluginName : availablePluginNames) {
             AvailablePluginInfo availablePlugin = updateSiteManager.getAvailablePlugin(pluginName);
            if (!UpdateSiteManager.MANDATORY.equals(availablePlugin.getType()) && installedPluginNames.contains(pluginName)) {
                 installedPlugins.add(availablePlugin);
             }
         }
         return installedPlugins;
     }
 
     public List<AvailablePluginInfo> getUpdatablePlugins() {
         List<AvailablePluginInfo> updatablePlugins = new ArrayList<AvailablePluginInfo>();
         Set<String> installedPluginNames = installedPluginManager.getInstalledPluginNames();
         Set<String> availablePluginNames = updateSiteManager.getAvailablePluginNames();
         for (String pluginName : availablePluginNames) {
             AvailablePluginInfo availablePlugin = updateSiteManager.getAvailablePlugin(pluginName);
             if (installedPluginNames.contains(pluginName)) {
                 InstalledPluginInfo installedPlugin = installedPluginManager.getInstalledPlugin(pluginName);
                 if (isNewerThan(availablePlugin.getVersion(), installedPlugin.getVersion())) {
                     updatablePlugins.add(availablePlugin);
                 }
             }
         }
         return updatablePlugins;
     }
 
     public boolean isInstalled(AvailablePluginInfo availablePlugin) {
         Set<String> installedPluginNames = installedPluginManager.getInstalledPluginNames();
         return installedPluginNames.contains(availablePlugin.getName());
     }
 
     public boolean isUpdatable(AvailablePluginInfo availablePlugin) {
         Set<String> installedPluginNames = installedPluginManager.getInstalledPluginNames();
         if (installedPluginNames.contains(availablePlugin.getName())) {
             //Installed
             InstalledPluginInfo installedPlugin = installedPluginManager.getInstalledPlugin(availablePlugin.getName());
             return isNewerThan(availablePlugin.getVersion(), installedPlugin.getVersion());
         }
         return false;
     }
 
     public InstalledPluginInfo getInstalled(AvailablePluginInfo plugin) {
         return installedPluginManager.getInstalledPlugin(plugin.getName());
     }
 
     // For test purpose
     Future<PluginInstallationJob> install(AvailablePluginInfo plugin) {
         return install(plugin, true);
     }
 
     public Future<PluginInstallationJob> install(AvailablePluginInfo plugin, boolean useProxy) {
         for (AvailablePluginInfo dep : getNeededDependencies(plugin)) {
             install(dep, useProxy);
         }
         return submitInstallationJob(plugin, useProxy);
     }
 
     public boolean isProxyNeeded() {
         try {
             // Try opening a URL and see if the proxy works fine
             proxyConfig.openUrl(new URL("http://www.google.com"));
         } catch (IOException ex) {
             logger.debug(ex.getLocalizedMessage());
             return true;
         }
         return false;
     }
 
     public static boolean disableUpdateCenterSwitch() {
         return Boolean.getBoolean("hudson.pluginManager.disableUpdateCenterSwitch");
     }
 
     public HttpResponse doUpdatePlugin(@QueryParameter String pluginName) {
         return doInstallPlugin(pluginName);
     }
 
     public HttpResponse doInstallPlugin(@QueryParameter String pluginName) {
         if (!hudsonSecurityManager.hasPermission(Permission.HUDSON_ADMINISTER)) {
             return HttpResponses.forbidden();
         }
         AvailablePluginInfo plugin = updateSiteManager.getAvailablePlugin(pluginName);
         if (plugin != null) {
             try {
                 PluginInstallationJob installJob = null;
                 // If the plugin is already being installed, don't schedule another. Make the search thread safe
                 List<PluginInstallationJob> jobs = Collections.synchronizedList(installationsJobs);
                 synchronized (jobs) {
                     for (PluginInstallationJob job : jobs) {
                         if (job.getName().equals(pluginName)) {
                             installJob = job;
                         }
                     }
                 }
                 // No previous install of the plugn, create new
                 if (installJob == null) {
                     Future<PluginInstallationJob> newJob = install(plugin, false);
                     installJob = newJob.get();
                 }
                 if (!installJob.getStatus()) {
                     return new ErrorHttpResponse("Plugin " + pluginName + " could not be installed. " + installJob.getErrorMsg());
                 }
             } catch (Exception ex) {
                 return new ErrorHttpResponse("Plugin " + pluginName + " could not be installed. " + ex.getLocalizedMessage());
             }
             installedPluginManager.loadInstalledPlugins();
             return HttpResponses.ok();
         }
         return new ErrorHttpResponse("Plugin " + pluginName + " is not a valid plugin");
     }
 
     public HttpResponse doEnablePlugin(@QueryParameter String pluginName, @QueryParameter boolean enable) {
         if (!hudsonSecurityManager.hasPermission(Permission.HUDSON_ADMINISTER)) {
             return HttpResponses.forbidden();
         }
         InstalledPluginInfo plugin = installedPluginManager.getInstalledPlugin(pluginName);
         try {
            plugin.setEnable(enable);  
         } catch (Exception ex) {
             return new ErrorHttpResponse("Plugin " + pluginName + " could not be enabled/disabled. " + ex.getLocalizedMessage());
         }
         return HttpResponses.ok();
     }
     
     public HttpResponse doDowngradePlugin(@QueryParameter String pluginName) {
         if (!hudsonSecurityManager.hasPermission(Permission.HUDSON_ADMINISTER)) {
             return HttpResponses.forbidden();
         }
         InstalledPluginInfo plugin = installedPluginManager.getInstalledPlugin(pluginName);
         try {
            plugin.downgade();  
         } catch (Exception ex) {
             return new ErrorHttpResponse("Plugin " + pluginName + " could not be reverted to previous version. " + ex.getLocalizedMessage());
         }
         return HttpResponses.ok();
     }
     
     public HttpResponse doUnpinPlugin(@QueryParameter String pluginName) {
         if (!hudsonSecurityManager.hasPermission(Permission.HUDSON_ADMINISTER)) {
             return HttpResponses.forbidden();
         }
         InstalledPluginInfo plugin = installedPluginManager.getInstalledPlugin(pluginName);
         try {
            plugin.unpin();  
         } catch (Exception ex) {
             return new ErrorHttpResponse("Plugin " + pluginName + " could not be unpinned. " + ex.getLocalizedMessage());
         }
         return HttpResponses.ok();
     }
 
     public HttpResponse doUploadPlugin(StaplerRequest request) throws IOException, ServletException {
         if (!hudsonSecurityManager.hasPermission(Permission.HUDSON_ADMINISTER)) {
             return HttpResponses.forbidden();
         }
         try {
             List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
             for (FileItem fileItem : items) {
                 if (fileItem.getFieldName().equals("file")) {
                     String fileName = FilenameUtils.getName(fileItem.getName());
                     if ("".equals(fileName) || !fileName.endsWith(".hpi")) {
                         return new ErrorHttpResponse("File " + fileName + " may not be a plugin");
                     }
                     File uploadedPluginFile = new File(pluginsDir, fileName);
                     fileItem.write(uploadedPluginFile);
                     return HttpResponses.plainText("Plugin " + fileName + " successfully uploaded.");
                 }
             }
 
         } catch (Exception exc) {
             return new ErrorHttpResponse(exc.getLocalizedMessage());
         }
         return new ErrorHttpResponse("Failed to upload plugin");
     }
 
     public HttpResponse doSearchPlugins(@QueryParameter String searchStr, @QueryParameter boolean searchDescription) {
         PluginSearchList pluginSearchList = new PluginSearchList(this, searchStr, searchDescription);
         return HttpResponses.forwardToView(pluginSearchList, "index.jelly");
     }
 
     public static class PluginSearchList {
 
         private Set<AvailablePluginInfo> searhcedPlugins;
         private PluginCenter pluginCenter;
 
         public PluginSearchList(PluginCenter pluginCenter, String searchStr, boolean searchDescription) {
             this.pluginCenter = pluginCenter;
             searhcedPlugins = pluginCenter.getUpdateSiteManager().searchPlugins(searchStr, searchDescription);
         }
 
         public Set<AvailablePluginInfo> getPlugins() {
             return searhcedPlugins;
         }
 
         public PluginCenter getPluginCenter() {
             return pluginCenter;
         }
     }
 
     private static class ErrorHttpResponse implements HttpResponse {
 
         private String message;
 
         ErrorHttpResponse(String message) {
             this.message = message;
         }
 
         @Override
         public void generateResponse(StaplerRequest sr, StaplerResponse rsp, Object o) throws IOException, ServletException {
             rsp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             rsp.setContentType("text/plain;charset=UTF-8");
             PrintWriter w = new PrintWriter(rsp.getWriter());
             w.println(message);
             w.close();
         }
     }
 
     public HttpResponse doProxyConfigure(
             @QueryParameter("proxy.server") String server,
             @QueryParameter("proxy.port") String port,
             @QueryParameter("proxy.noProxyFor") String noProxyFor,
             @QueryParameter("proxy.userName") String userName,
             @QueryParameter("proxy.password") String password,
             @QueryParameter("proxy.authNeeded") String authNeeded) throws IOException {
 
         if (!hudsonSecurityManager.hasPermission(Permission.HUDSON_ADMINISTER)) {
             return HttpResponses.forbidden();
         }
 
         try {
             boolean proxySet = setProxy(server, port, noProxyFor, userName, password, authNeeded);
             if (proxySet) {
                 proxyConfig.save();
             }
             // Try opening a URL and see if the proxy works fine
             proxyConfig.openUrl(new URL("http://www.google.com"));
 
         } catch (IOException ex) {
             return HttpResponses.error(HttpServletResponse.SC_BAD_REQUEST, ex);
         }
         return HttpResponses.ok();
     }
 
     private boolean setProxy(String server, String port, String noProxyFor,
             String userName, String password, String authNeeded) throws IOException {
         server = Util.fixEmptyAndTrim(server);
 
         if ((server != null) && !"".equals(server)) {
             // If port is not specified assume it is port 80 (usual default for HTTP port)
             int portNumber = 80;
             if (!"".equals(Util.fixNull(port))) {
                 portNumber = Integer.parseInt(Util.fixNull(port));
             }
 
             boolean proxyAuthNeeded = "on".equals(Util.fixNull(authNeeded));
             if (!proxyAuthNeeded) {
                 userName = "";
                 password = "";
             }
 
             proxyConfig.configure(server, portNumber, Util.fixEmptyAndTrim(noProxyFor),
                     Util.fixEmptyAndTrim(userName), Util.fixEmptyAndTrim(password), "on".equals(Util.fixNull(authNeeded)));
             return true;
 
         } else {
             proxyConfig.getXmlFile().delete();
             proxyConfig.name = null;
             return true;
         }
     }
 
     public HttpResponse doConfigureUpdateSite(@QueryParameter String siteUrl) throws IOException {
         if (!hudsonSecurityManager.hasPermission(Permission.HUDSON_ADMINISTER)) {
             return HttpResponses.forbidden();
         }
 
         try {
             updateSiteManager.verifyUpdateSite(siteUrl);
             updateSiteManager.setUpdateSiteUrl(siteUrl);
             // Ok a valid update site URL set it to the plugin manager. 
             // For now let Plugin Manager periodically update the local cache.
             //Hudson.getInstance().getPluginManager().doSiteConfigure(site);
 
         } catch (IOException ex) {
             return new ErrorHttpResponse("Update Site Could not be set. " + ex.getLocalizedMessage());
         }
         return HttpResponses.ok();
     }
 
     public HttpResponse doRefreshUpdateCenter() throws IOException {
 
         if (!hudsonSecurityManager.hasPermission(Permission.HUDSON_ADMINISTER)) {
             return HttpResponses.forbidden();
         }
         // Check if you are able to connect
         try {
             URL updateCenterRemoteUrl = new URL(updateSiteManager.getUpdateSiteUrl());
             proxyConfig.openUrl(updateCenterRemoteUrl);
         } catch (Exception exc) {
             return new ErrorHttpResponse("Could not connect to  " + updateSiteManager.getUpdateSiteUrl() + ". "
                     + "If you are behind a firewall set HTTP proxy and try again.");
         }
 
         try {
             updateSiteManager.refreshFromUpdateSite();
         } catch (IOException ex) {
             return new ErrorHttpResponse("Updates could not be refreshed. " + ex.getLocalizedMessage());
         }
         return HttpResponses.ok();
     }
 
     private Future<PluginInstallationJob> submitInstallationJob(AvailablePluginInfo plugin, boolean useProxy) {
         PluginInstallationJob newJob = new PluginInstallationJob(plugin, pluginsDir, useProxy);
         installationsJobs.add(newJob);
         return installerService.submit(newJob, newJob);
     }
 
     private boolean isNewerThan(String availableVersion, String installedVersion) {
         try {
             return new VersionNumber(installedVersion).compareTo(new VersionNumber(availableVersion)) < 0;
         } catch (IllegalArgumentException e) {
             // couldn't parse as the version number.
             return false;
         }
     }
 
     private List<AvailablePluginInfo> getNeededDependencies(AvailablePluginInfo pluginInfo) {
         List<AvailablePluginInfo> deps = new ArrayList<AvailablePluginInfo>();
 
         if ((pluginInfo != null) && (pluginInfo.getDependencies().size() > 0)) {
             for (Map.Entry<String, String> e : pluginInfo.getDependencies().entrySet()) {
                 AvailablePluginInfo depPlugin = updateSiteManager.getAvailablePlugin(e.getKey());
                 if (depPlugin != null) {
                     VersionNumber requiredVersion = new VersionNumber(e.getValue());
 
                     // Is the plugin installed already? If not, add it.
                     InstalledPluginInfo current = installedPluginManager.getInstalledPlugin(depPlugin.getName());
 
                     if (current == null) {
                         deps.add(depPlugin);
                     } else if (current.isOlderThan(requiredVersion)) {
                         deps.add(depPlugin);
                     }
                 } else {
                     logger.error("Could not find " + e.getKey() + " which is required by " + pluginInfo.getDisplayName());
                 }
             }
         }
 
         return deps;
     }
 
     public String getLastUpdatedString() {
         return "17 Minutes";
         //return Hudson.getInstance().getUpdateCenter().getLastUpdatedString();
     }
 }
