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
 package org.eclipse.hudson.init;
 
 import hudson.ProxyConfiguration;
 import hudson.Util;
 import hudson.XmlFile;
 import hudson.markup.MarkupFormatter;
 import hudson.model.Hudson;
 import hudson.model.User;
 import hudson.security.Permission;
 import hudson.triggers.SafeTimerTask;
 import hudson.triggers.Trigger;
 import hudson.util.DaemonThreadFactory;
 import hudson.util.HudsonFailedToLoad;
 import hudson.util.HudsonIsLoading;
 import hudson.util.VersionNumber;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.lang.reflect.Constructor;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.*;
 import java.util.concurrent.*;
 import java.util.logging.Level;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.io.FileUtils;
 import org.eclipse.hudson.WebAppController;
 import org.eclipse.hudson.plugins.InstalledPluginManager;
 import org.eclipse.hudson.plugins.InstalledPluginManager.InstalledPluginInfo;
 import org.eclipse.hudson.plugins.PluginInstallationJob;
 import org.eclipse.hudson.plugins.UpdateSiteManager;
 import org.eclipse.hudson.plugins.UpdateSiteManager.AvailablePluginInfo;
 import org.eclipse.hudson.security.HudsonSecurityEntitiesHolder;
 import org.eclipse.hudson.security.HudsonSecurityManager;
 import org.kohsuke.stapler.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Provides support for initial setup during first run. Gives opportunity to
  * Hudson Admin to - Install mandatory, featured and recommended plugins -
  * Update compatiblity, featured and recommended plugins suitable for current
  * Hudson - Provide Authentication if needed - Setup proxy if required
  *
  * @author Winston Prakash
  */
 final public class InitialSetup {
 
     private Logger logger = LoggerFactory.getLogger(InitialSetup.class);
     private File pluginsDir;
     private ServletContext servletContext;
     private UpdateSiteManager updateSiteManager;
     private InstalledPluginManager installedPluginManager;
     private List<AvailablePluginInfo> installedRecommendedPlugins = new ArrayList<AvailablePluginInfo>();
     private List<AvailablePluginInfo> installableRecommendedPlugins = new ArrayList<AvailablePluginInfo>();
     private List<AvailablePluginInfo> updatableRecommendedPlugins = new ArrayList<AvailablePluginInfo>();
     private List<AvailablePluginInfo> installedFeaturedPlugins = new ArrayList<AvailablePluginInfo>();
     private List<AvailablePluginInfo> installableFeaturedPlugins = new ArrayList<AvailablePluginInfo>();
     private List<AvailablePluginInfo> updatableFeaturedPlugins = new ArrayList<AvailablePluginInfo>();
     private List<AvailablePluginInfo> installedCompatibilityPlugins = new ArrayList<AvailablePluginInfo>();
     private List<AvailablePluginInfo> installableCompatibilityPlugins = new ArrayList<AvailablePluginInfo>();
     private List<AvailablePluginInfo> updatableCompatibilityPlugins = new ArrayList<AvailablePluginInfo>();
     private ProxyConfiguration proxyConfig;
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
     private XmlFile initSetupFile;
     private File hudsonHomeDir;
     private boolean proxyNeeded = false;
     private List<PluginInstallationJob> installationsJobs = new CopyOnWriteArrayList<PluginInstallationJob>();
     private static ClassLoader outerClassLoader;
     private static ClassLoader initialClassLoader;
     private static int highInitThreadNumber = 0;
     private static InitialSetup INSTANCE;
 
     public InitialSetup(File homeDir, ServletContext context) throws MalformedURLException, IOException {
         hudsonHomeDir = homeDir;
         pluginsDir = new File(homeDir, "plugins");
         servletContext = context;
         hudsonSecurityManager = HudsonSecurityEntitiesHolder.getHudsonSecurityManager();
         proxyConfig = new ProxyConfiguration(homeDir);
         updateSiteManager = new UpdateSiteManager("default", hudsonHomeDir, proxyConfig);
         installedPluginManager = new InstalledPluginManager(pluginsDir);
         initSetupFile = new XmlFile(new File(homeDir, "initSetup.xml"));
         refreshUpdateCenterMetadataCache();
         check();
         // This is only created once during startup, so is effectively a singleton
         INSTANCE = this;
     }
     
     public static InitialSetup getLastInitialSetup() {
         return INSTANCE;
     }
 
     public boolean needsInitSetup() {
         if (initSetupFile.exists()) {
             return false;
         } else {
             if (Boolean.getBoolean("skipInitSetup")) {
                 try {
                     initSetupFile.write("Hudson 3.0 Initial Setup Done");
                 } catch (IOException ex) {
                     logger.error(ex.getLocalizedMessage());
                 }
                 return false;
             } else {
                 return true;
             }
         }
     }
 
     public boolean needsAdminLogin() {
         return !hudsonSecurityManager.hasPermission(Permission.HUDSON_ADMINISTER);
     }
 
     public ServletContext getServletContext() {
         return servletContext;
     }
 
     public ProxyConfiguration getProxyConfig() {
         return proxyConfig;
     }
 
     public MarkupFormatter getMarkupFormatter() {
         return hudsonSecurityManager.getMarkupFormatter();
     }
 
     public List<AvailablePluginInfo> getInstalledRecommendedPlugins() {
         return installedRecommendedPlugins;
     }
 
     public List<AvailablePluginInfo> getInstallableRecommendedPlugins() {
         return installableRecommendedPlugins;
     }
 
     public List<AvailablePluginInfo> getUpdatableRecommendedPlugins() {
         return updatableRecommendedPlugins;
     }
 
     public List<AvailablePluginInfo> getInstalledFeaturedPlugins() {
         return installedFeaturedPlugins;
     }
 
     public List<AvailablePluginInfo> getInstallableFeaturedPlugins() {
         return installableFeaturedPlugins;
     }
 
     public List<AvailablePluginInfo> getUpdatableFeaturedPlugins() {
         return updatableFeaturedPlugins;
     }
 
     public List<AvailablePluginInfo> getInstallableCompatibilityPlugins() {
         return installableCompatibilityPlugins;
     }
 
     public List<AvailablePluginInfo> getInstalledCompatibilityPlugins() {
         return installedCompatibilityPlugins;
     }
 
     public List<AvailablePluginInfo> getUpdatableCompatibilityPlugins() {
         return updatableCompatibilityPlugins;
     }
 
     public InstalledPluginInfo getInstalled(AvailablePluginInfo plugin) {
         return installedPluginManager.getInstalledPlugin(plugin.getName());
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
 
     public Future<PluginInstallationJob> install(AvailablePluginInfo plugin) {
         for (AvailablePluginInfo dep : getNeededDependencies(plugin)) {
             install(dep);
         }
         return submitInstallationJob(plugin);
     }
 
     public boolean isProxyNeeded() {
         return proxyNeeded;
     }
 
     public HttpResponse doinstallPlugin(@QueryParameter String pluginName) {
         if (!hudsonSecurityManager.hasPermission(Permission.HUDSON_ADMINISTER)) {
             return HttpResponses.forbidden();
         }
         AvailablePluginInfo plugin = updateSiteManager.getAvailablePlugin(pluginName);
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
                 Future<PluginInstallationJob> newJob = install(plugin);
                 installJob = newJob.get();
             }
             if (!installJob.getStatus()) {
                 return new ErrorHttpResponse("Plugin " + pluginName + " could not be installed. " + installJob.getErrorMsg());
             }
         } catch (Exception ex) {
             return new ErrorHttpResponse("Plugin " + pluginName + " could not be installed. " + ex.getLocalizedMessage());
         }
         reCheck();
         return HttpResponses.ok();
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
             return new ErrorHttpResponse(ex.getLocalizedMessage());
         }
         return HttpResponses.ok();
     }
 
     public HttpResponse doFinish() {
         try {
             initSetupFile.write("Hudson 3.0 Initial Setup Done");
         } catch (IOException ex) {
             logger.error(ex.getLocalizedMessage());
         }
         invokeHudson();
 
         return HttpResponses.ok();
     }
     
    private class OuterClassLoader extends ClassLoader {
         OuterClassLoader(ClassLoader parent) {
             super(parent);
         }
     }
    
    public void invokeHudson() {
        invokeHudson(false);
    }
 
     public void invokeHudson(boolean restart) {
         final WebAppController controller = WebAppController.get();
         
         if (initialClassLoader == null) {
             initialClassLoader = getClass().getClassLoader();
         }
         
         Class hudsonIsLoadingClass;
         try {
             outerClassLoader = new OuterClassLoader(initialClassLoader);
             
             hudsonIsLoadingClass = outerClassLoader.loadClass("hudson.util.HudsonIsLoading");
             HudsonIsLoading hudsonIsLoading = (HudsonIsLoading) hudsonIsLoadingClass.newInstance();
             Class runnableClass = outerClassLoader.loadClass("org.eclipse.hudson.init.InitialRunnable");
             Constructor ctor = runnableClass.getDeclaredConstructors()[0];
             ctor.setAccessible(true);
            InitialRunnable initialRunnable = (InitialRunnable) ctor.newInstance(controller, logger, hudsonHomeDir, servletContext, restart);
  
             controller.install(hudsonIsLoading);
             Thread initThread = new Thread(initialRunnable, "hudson initialization thread "+(++highInitThreadNumber));
             initThread.setContextClassLoader(outerClassLoader);
             initThread.start();
             
         } catch (Exception ex) {
             logger.error("Hudson failed to load!!!", ex);
         }
         
         /** Above replaces these lines
         controller.install(new HudsonIsLoading());
 
         new Thread("hudson initialization thread") {
         }.start();
         */
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
             return false;
         }
     }
 
     private Future<PluginInstallationJob> submitInstallationJob(AvailablePluginInfo plugin) {
         PluginInstallationJob newJob = new PluginInstallationJob(plugin, pluginsDir, proxyConfig);
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
 
     void reCheck() {
         installedRecommendedPlugins.clear();
         installableRecommendedPlugins.clear();
         updatableRecommendedPlugins.clear();
         installedFeaturedPlugins.clear();
         installableFeaturedPlugins.clear();
         updatableFeaturedPlugins.clear();
         installableCompatibilityPlugins.clear();
         installedCompatibilityPlugins.clear();
         updatableCompatibilityPlugins.clear();
         installedPluginManager.loadInstalledPlugins();
         check();
     }
 
     private void check() {
         if (!pluginsDir.exists()) {
             pluginsDir.mkdirs();
         }
         Set<String> installedPluginNames = installedPluginManager.getInstalledPluginNames();
         Set<String> availablePluginNames = updateSiteManager.getAvailablePluginNames();
         for (String pluginName : availablePluginNames) {
             AvailablePluginInfo availablePlugin = updateSiteManager.getAvailablePlugin(pluginName);
             if (installedPluginNames.contains(pluginName)) {
                 //Installed
                 InstalledPluginInfo installedPlugin = installedPluginManager.getInstalledPlugin(pluginName);
                 if (availablePlugin.getType().equals(UpdateSiteManager.COMPATIBILITY)) {
                     //Installed Compatibility Plugin
                     if (isNewerThan(availablePlugin.getVersion(), installedPlugin.getVersion())) {
                         //Updatabale Compatibility Plugin update needed
                         updatableCompatibilityPlugins.add(availablePlugin);
                     } else {
                         //Installed Compatibility Plugin. No updates available
                         installedCompatibilityPlugins.add(availablePlugin);
                     }
                 } else if (availablePlugin.getType().equals(UpdateSiteManager.FEATURED)) {
                     if (isNewerThan(availablePlugin.getVersion(), installedPlugin.getVersion())) {
                         //Updatabale featured Plugin update needed
                         updatableFeaturedPlugins.add(availablePlugin);
                     } else {
                         //Installed featured Plugin. No updates available
                         installedFeaturedPlugins.add(availablePlugin);
                     }
                 } else if (availablePlugin.getType().equals(UpdateSiteManager.RECOMMENDED)) {
                     if (isNewerThan(availablePlugin.getVersion(), installedPlugin.getVersion())) {
                         //Updatabale recommended Plugin update needed
                         updatableRecommendedPlugins.add(availablePlugin);
                     } else {
                         //Installed recommended Plugin. No updates available
                         installedRecommendedPlugins.add(availablePlugin);
                     }
                 }
 
             } else {
                 //Not installed
                 if (availablePlugin.getType().equals(UpdateSiteManager.COMPATIBILITY)) {
                     //Mandatory Plugin. Need to be installed
                     installableCompatibilityPlugins.add(availablePlugin);
                 }
                 if (availablePlugin.getType().equals(UpdateSiteManager.FEATURED)) {
                     //Featured Plugin. Available for installation
                     installableFeaturedPlugins.add(availablePlugin);
                 }
                 if (availablePlugin.getType().equals(UpdateSiteManager.RECOMMENDED)) {
                     //Recommended Plugin. Available for installation
                     installableRecommendedPlugins.add(availablePlugin);
                 }
             }
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
 
     protected void refreshUpdateCenterMetadataCache() throws IOException {
 
         try {
             updateSiteManager.refreshFromUpdateSite();
             return;
         } catch (Exception exc) {
             proxyNeeded = true;
             logger.info("Could not fetch update center metadata from " + updateSiteManager.getUpdateSiteUrl() + ". Using bundled update center metadata.");
         }
 
 
         URL updateCenterJsonUrl = servletContext.getResource("/WEB-INF/update-center.json");
         if (updateCenterJsonUrl != null) {
             long lastModified = updateCenterJsonUrl.openConnection().getLastModified();
             File localCacheFile = new File(hudsonHomeDir, "updates/default.json");
 
             if (!localCacheFile.exists() || (localCacheFile.lastModified() < lastModified)) {
                 String jsonStr = org.apache.commons.io.IOUtils.toString(updateCenterJsonUrl.openStream());
                 jsonStr = jsonStr.trim();
                 if (jsonStr.startsWith("updateCenter.post(")) {
                     jsonStr = jsonStr.substring("updateCenter.post(".length());
                 }
                 if (jsonStr.endsWith(");")) {
                     jsonStr = jsonStr.substring(0, jsonStr.lastIndexOf(");"));
                 }
                 FileUtils.writeStringToFile(localCacheFile, jsonStr);
                 localCacheFile.setLastModified(lastModified);
                 updateSiteManager.refresh();
             }
         }
     }
 }
