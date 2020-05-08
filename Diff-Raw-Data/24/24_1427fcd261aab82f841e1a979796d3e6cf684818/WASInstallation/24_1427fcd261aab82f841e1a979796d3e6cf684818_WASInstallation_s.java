 /*
  * The MIT License
  *
 * Copyright (c) 2009-2010, Manufacture Française des Pneumatiques Michelin, Romain Seguy
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package com.michelin.cio.hudson.plugins.wasbuilder;
 
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.Functions;
 import hudson.Launcher;
 import hudson.Util;
 import hudson.model.EnvironmentSpecific;
 import hudson.model.Hudson;
 import hudson.model.Node;
 import hudson.model.TaskListener;
 import hudson.remoting.Callable;
 import hudson.slaves.NodeSpecific;
 import hudson.tools.ToolDescriptor;
 import hudson.tools.ToolInstallation;
 import hudson.util.FormValidation;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import net.sf.json.JSONObject;
 import org.apache.commons.lang.StringUtils;
 import org.jvnet.localizer.ResourceBundleHolder;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * Corresponds to an IBM WebSphere Application Server installation (currently,
  * it has been tested with WAS 6.0 and WAS 7.0) or an Administration Thin Client
  * (cf. {@link http://publib.boulder.ibm.com/infocenter/wasinfo/v7r0/topic/com.ibm.websphere.nd.multiplatform.doc/info/ae/ae/txml_adminclient.html).
  *
  * <p>To use a {@link WASBuildStep} build step, it is mandatory to define an
  * installation: No default installations can be assumed as we necessarily need
  * {@code wsadmin.bat}/{@code wsadmin.sh}.</p>
  *
  * @author Romain Seguy (http://openromain.blogspot.com)
  */
 public class WASInstallation extends ToolInstallation implements NodeSpecific<WASInstallation>, EnvironmentSpecific<WASInstallation> {
 
     public final static String WSADMIN_BAT = "wsadmin.bat";
     public final static String WSADMIN_SH = "wsadmin.sh";
 
     /**
      * Represents the wsadmin command to actually invoke (most of the time, this
      * has to be something like $WAS_HOME/bin/wsadmin.sh).
      */
     private final String wsadminCommand;
 
     @DataBoundConstructor
     public WASInstallation(String name, String home, String wsadminCommand) {
         super(name, removeTrailingBackslash(home), Collections.EMPTY_LIST);
         if(StringUtils.isBlank(wsadminCommand)) {
             this.wsadminCommand = "${WSADMIN}";
         }
         else {
             this.wsadminCommand = wsadminCommand;
         }
     }
 
     public WASInstallation forEnvironment(EnvVars env) {
         return new WASInstallation(getName(), env.expand(getHome()), getWsadminCommand());
     }
 
     public WASInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
         return new WASInstallation(getName(), translateFor(node, log), getWsadminCommand());
     }
 
     public static WASInstallation getWasInstallationByName(String installationName) {
         for(WASInstallation installation: Hudson.getInstance().getDescriptorByType(WASInstallation.DescriptorImpl.class).getInstallations()) {
             if(installationName != null && installation.getName().equals(installationName)) {
                 return installation;
             }
         }
 
         return null;
     }
 
     public String getWsadminCommand() {
         if(StringUtils.isBlank(wsadminCommand)) {
             return "${WSADMIN}";
         }
         return wsadminCommand;
     }
 
     public String getWsadminExecutable(Launcher launcher) throws IOException, InterruptedException {
         return launcher.getChannel().call(new Callable<String,IOException>() {
             public String call() throws IOException {
                 String wsadminFilePath = null;
                 // 1st try: do we work with a plain WAS installation?
                 File wsadminFile = getWsadminFile("bin");
                 if(wsadminFile.exists()) {
                     wsadminFilePath = wsadminFile.getPath();
                 }
                 else {
                     // 2nd try: do we work with an administration thin client?
                     wsadminFile = getWsadminFile(null);
                     if(wsadminFile.exists()) {
                         wsadminFilePath = wsadminFile.getPath();
                     }
                     else {
                         return null;
                     }
                 }
 
                 return getWsadminCommand().replace("${WSADMIN}", wsadminFilePath);
             }
         });
     }
 
     /**
      * Returns a {@link File} representing {@code wsadmin.bat}/{@code wsadmin.sh}.
      */
     private File getWsadminFile(String binFolder) {
         String wsadminFileName = WSADMIN_SH;
 
         if(!StringUtils.isEmpty(binFolder)) {
             binFolder = binFolder + "/";
         }
         else {
             binFolder = "";
         }
 
         if(Functions.isWindows()) {
             wsadminFileName = WSADMIN_BAT;
         }
 
         return new File(Util.replaceMacro(getHome(), EnvVars.masterEnvVars), binFolder + wsadminFileName);
     }
 
     /**
      * Removes the '\' or '/' character that may be present at the end of the
      * specified string.
      */
     private static String removeTrailingBackslash(String s) {
         return StringUtils.removeEnd(StringUtils.removeEnd(s, "/"), "\\");
     }
 
     @Extension
     public static class DescriptorImpl extends ToolDescriptor<WASInstallation> {
 
         private List<WASServer> servers;
         private boolean createLocks = true;
 
         public DescriptorImpl() {
             // let's avoid a NullPointerException in getInstallations()
             setInstallations(new WASInstallation[0]);
 
             load();
         }
 
         /**
          * Returns the possible connection types to WAS.
          *
          * <p>This method needs to be placed here so that the list can be
          * accessible from WASInstallation's global.jelly file: global.jelly
          * is not able to access such a method if it is placed, even statically,
          * into WASServer.</p>
          */
         public String[] getConntypes() {
             return WASServer.CONNTYPES;
         }
 
         public boolean getCreateLocks() {
             return createLocks;
         }
 
         public void setCreateLocks(boolean createLocks) {
             this.createLocks = createLocks;
         }
 
         @Override
         public String getDisplayName() {
             return ResourceBundleHolder.get(WASBuildStep.class).format("DisplayName");
         }
 
         public WASServer[] getServers() {
             if(servers != null) {
                 return servers.toArray(new WASServer[0]);
             }
 
             return null;
         }
 
         private void setServers(WASServer... servers) {
             if(servers != null) {
                 if(this.servers != null) {
                     this.servers.clear();
                 }
                 else {
                     this.servers = new ArrayList<WASServer>();
                 }
 
                 // only servers which have a name are added to the list, other
                 // ones are dropped
                 for(WASServer server: servers) {
                     if(StringUtils.isNotEmpty(server.getName())) {
                         this.servers.add(server);
                     }
                 }
 
                 // we sort the servers list, otherwise it will become a pain to
                 // know what's already created and what has to be created
                 Collections.sort(this.servers, new Comparator<WASServer>() {
                     public int compare(WASServer server1, WASServer server2) {
                         return server1.getName().compareToIgnoreCase(server2.getName());
                     }
                 });
             }
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
             setInstallations(
                     req.bindJSONToList(
                             WASInstallation.class,
                             formData.get("wasinstall")).toArray(new WASInstallation[0]));
             setServers(
                     req.bindJSONToList(
                             WASServer.class,
                             formData.get("wasserver")).toArray(new WASServer[0]));
             setCreateLocks(formData.getBoolean("createLocks"));
 
             save();
 
             if(getCreateLocks()) {
                 createLocks();
             }
 
             return true;
         }
 
         /**
          * Creates, for each defined WAS server, a corresponding lock (from the
          * locks-and-latches plug-in).
          *
          * <p>All the processing here is done using the reflection API: This is
          * purposely done to avoid having a dependency between this plug-in and
          * the locks-and-latches one which would make it mandatory to install it.
          * </p>
          */
         private void createLocks() {
             // is the locks-and-latches plugin installed?
             if(Hudson.getInstance().getPlugin("locks-and-latches") != null) {
                 try {
                     ClassLoader hudsonClassLoader = Hudson.getInstance().getPluginManager().uberClassLoader;
 
                     // LockWrapper.DescriptorImpl lockWrapperDescriptor = LockWrapper.getDescriptor();
                     Class lockWrapperClass = hudsonClassLoader.loadClass("hudson.plugins.locksandlatches.LockWrapper");
                     Field lockWrapperDescriptorField = lockWrapperClass.getDeclaredField("DESCRIPTOR");
                     Object lockWrapperDescriptor = lockWrapperDescriptorField.get(null);
 
                     // String[] lockNames = lockWrapperDescriptor.getLockNames();
                     Class descriptorImplClass = hudsonClassLoader.loadClass("hudson.plugins.locksandlatches.LockWrapper$DescriptorImpl");
                     Method getLockNamesMethod = descriptorImplClass.getMethod("getLockNames");
                     String[] lockNames = (String[]) getLockNamesMethod.invoke(lockWrapperDescriptor);
 
                     // we ensure each server has a lock with the same name
                     List<WASServer> createLockFor = new ArrayList<WASServer>();
                     if(getServers() != null) {
                         for(WASServer server: getServers()) {
                             if(lockNames != null) {
                                 boolean found = false;
                                 for(String lockName: lockNames) {
                                     if(lockName.equals(server.getName())) {
                                         found = true;
                                         break;
                                     }
                                 }
                                 if(!found) {
                                     createLockFor.add(server);
                                 }
                             }
                             else {
                                 createLockFor.add(server);
                             }
                         }
                     }
 
                     // we create the new locks if required
                     if(!createLockFor.isEmpty()) {
                         // new LockWrapper.LockConfig(...)
                         Class lockConfigClass = hudsonClassLoader.loadClass("hudson.plugins.locksandlatches.LockWrapper$LockConfig");
                         Constructor lockConfigConstructor = lockConfigClass.getConstructor(String.class);
 
                         List newLocks = new ArrayList();
                         for(WASServer server: createLockFor) {
                             // newLocks.add(new LockWrapper.LockConfig(server.getName()));
                             newLocks.add(lockConfigConstructor.newInstance(server.getName()));
                             LOGGER.info("The locks-and-latches plugin is installed: Adding new lock for WAS server " + server.getName());
                         }
 
                         // lockWrapperDescriptor.getLocks().addAll(newLocks);
                         Method getLocksMethod = descriptorImplClass.getMethod("getLocks");
                         ((List) getLocksMethod.invoke(lockWrapperDescriptor)).addAll(newLocks);
 
                         // lockWrapperDescriptor.save();
                         Method saveMethod = descriptorImplClass.getMethod("save");
                         saveMethod.invoke(lockWrapperDescriptor);
                     }
                 } catch (Exception e) {
                     LOGGER.warning("Can't automatically add locks for WAS servers; The following exception occurred while reflecting the locks-and-latches plugin: " + e);
                 }
             }
             else {
                 LOGGER.warning("The locks-and-latches plugin is not installed: Can't automatically add locks for each WAS server.");
             }
         }
 
         /**
          * Checks if the installation folder is valid.
          */
         public FormValidation doCheckHome(@QueryParameter File value) {
             if(value == null || value.getPath().length() == 0) {
                 return FormValidation.error(ResourceBundleHolder.get(WASInstallation.class).format("InstallationFolderMustBeSet"));
             }
 
             if(!value.isDirectory()) {
                 return FormValidation.error(ResourceBundleHolder.get(WASInstallation.class).format("NotAFolder", value));
             }
 
             // let's check for the wsadmin file existence
             if(Functions.isWindows()) {
                 boolean noWsadminBat = false;           // plain WAS installation
                 boolean noWsadminThinClientBat = false; // WAS administration thin client
 
                 File wsadminFile = new File(value, "bin\\" + WSADMIN_BAT);
                 if(!wsadminFile.exists()) {
                     noWsadminBat = true;
 
                     wsadminFile = new File(value, WSADMIN_BAT);
                     if(!wsadminFile.exists()) {
                         noWsadminThinClientBat = true;
                     }
                 }
 
                 if(noWsadminThinClientBat || noWsadminThinClientBat && noWsadminBat) {
                     return FormValidation.error(ResourceBundleHolder.get(WASInstallation.class).format("NotAWASInstallationFolder", value));
                 }
             }
             else {
                 boolean noWsadminSh = false;           // plain WAS installation
                 boolean noWsadminThinClientSh = false; // WAS administration thin client
 
                 File wsadminFile = new File(value, "bin/" + WSADMIN_SH);
                 if(!wsadminFile.exists()) {
                     noWsadminSh = true;
 
                     wsadminFile = new File(value, WSADMIN_SH);
                     if(!wsadminFile.exists()) {
                         noWsadminThinClientSh = true;
                     }
                 }
 
                 if(noWsadminThinClientSh || noWsadminThinClientSh && noWsadminSh) {
                     return FormValidation.error(ResourceBundleHolder.get(WASInstallation.class).format("NotAWASInstallationFolder", value));
                 }
             }
 
             return FormValidation.ok();
         }
 
         // --- WASServer checks ---
 
         public FormValidation doCheckName(@QueryParameter String value) {
             if(value == null || value.length() == 0) {
                 return FormValidation.error(ResourceBundleHolder.get(WASServer.class).format("NameMustBeSet"));
             }
 
             return FormValidation.ok();
         }
 
         public FormValidation doCheckConntype(@QueryParameter String value) {
             if(value == null) {
                 return FormValidation.error(ResourceBundleHolder.get(WASServer.class).format("ConntypeMustBeSet"));
             }
 
             if(!Arrays.asList(WASServer.CONNTYPES).contains(value)) {
                 return FormValidation.error(ResourceBundleHolder.get(WASServer.class).format("InvalidConntype", value));
             }
 
             return FormValidation.ok();
         }
 
         public FormValidation doCheckHost(@QueryParameter String value) throws IOException, ServletException {
             if(value == null || value.length() == 0) {
                 return FormValidation.error(ResourceBundleHolder.get(WASServer.class).format("HostMustBeSet"));
             }
 
             InetAddress inetAddress;
             try {
                 inetAddress = InetAddress.getByName(value);
             }
             catch(UnknownHostException uhe) {
                 return FormValidation.error(ResourceBundleHolder.get(WASServer.class).format("HostNotValid", value));
             }
             catch(SecurityException se) {
                 return FormValidation.error(ResourceBundleHolder.get(WASServer.class).format("HostSecurityException", value));
             }
 
             try {
                 if(!inetAddress.isReachable(1000)) {
                     throw new IOException();
                 }
             }
             catch(IOException ioe) {
                 return FormValidation.error(ResourceBundleHolder.get(WASServer.class).format("HostCantBeReached", value));
             }
 
             return FormValidation.ok();
         }
 
         public FormValidation doCheckPort(@QueryParameter String value) {
             if(value == null || value.length() == 0) {
                 return FormValidation.error(ResourceBundleHolder.get(WASServer.class).format("PortMustBeSet"));
             }
             
             int port;
             try {
                 port = Integer.parseInt(value);
                 if(port < 0 || port > 65535) {
                     return FormValidation.error(ResourceBundleHolder.get(WASServer.class).format("PortMustBeInteger"));
                 }
             }
             catch(NumberFormatException nfe) {
                 return FormValidation.error(ResourceBundleHolder.get(WASServer.class).format("PortMustBeInteger"));
             }
             
             if(port < 1024 || port > 49151) {
                 return FormValidation.warning(ResourceBundleHolder.get(WASServer.class).format("PortNotPreferredValue", port));
             }
             
             return FormValidation.ok();
         }
 
         public FormValidation doCheckUser(@QueryParameter String value) {
             if(value == null || value.length() == 0) {
                 return FormValidation.warning(ResourceBundleHolder.get(WASServer.class).format("UserMustBeSetIfSecurityEnabled"));
             }
 
             return FormValidation.ok();
         }
 
         public FormValidation doCheckPassword(@QueryParameter String value) {
             if(value == null || value.length() == 0) {
                 return FormValidation.warning(ResourceBundleHolder.get(WASServer.class).format("PasswordMustBeSetIfSecurityEnabled"));
             }
 
             return FormValidation.ok();
         }
 
     }
 
     private final static Logger LOGGER = Logger.getLogger(WASInstallation.class.getName());
 
 }
