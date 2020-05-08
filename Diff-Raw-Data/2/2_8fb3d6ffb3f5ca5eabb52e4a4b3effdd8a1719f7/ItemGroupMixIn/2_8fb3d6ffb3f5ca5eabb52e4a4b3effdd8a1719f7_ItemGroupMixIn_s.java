 /*******************************************************************************
  *
  * Copyright (c) 2004-2009 Oracle Corporation.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * 
  *    Kohsuke Kawaguchi, CloudBees, Inc.
  *
  *
  *******************************************************************************/ 
 
 package hudson.model;
 
 import hudson.Util;
 import hudson.model.listeners.ItemListener;
 import hudson.security.AccessControlled;
 import hudson.util.CopyOnWriteMap;
 import hudson.util.Function1;
 import hudson.util.IOUtils;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Map;
 import org.eclipse.hudson.security.team.Team;
 import org.eclipse.hudson.security.team.TeamManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Defines a bunch of static methods to be used as a "mix-in" for
  * {@link ItemGroup} implementations. Not meant for a consumption from outside
  * {@link ItemGroup}s.
  *
  * @author Kohsuke Kawaguchi
  */
 public abstract class ItemGroupMixIn {
 
     private transient Logger logger = LoggerFactory.getLogger(ItemGroupMixIn.class);
     
     /**
      * {@link ItemGroup} for which we are working.
      */
     private final ItemGroup parent;
     private final AccessControlled acl;
 
     protected ItemGroupMixIn(ItemGroup parent, AccessControlled acl) {
         this.parent = parent;
         this.acl = acl;
     }
 
     /*
      * Callback methods to be implemented by the ItemGroup implementation.
      */
     /**
      * Adds a newly created item to the parent.
      */
     protected abstract void add(TopLevelItem item);
 
     /**
      * Assigns the root directory for a prospective item.
      */
     protected abstract File getRootDirFor(String name);
 
 
     /*
      * The rest is the methods that provide meat.
      */
     /**
      * Loads all the child {@link Item}s.
      *
      * @param modulesDir Directory that contains sub-directories for each child
      * item.
      */
     public static <K, V extends Item> Map<K, V> loadChildren(ItemGroup parent, File modulesDir, Function1<? extends K, ? super V> key) {
         modulesDir.mkdirs(); // make sure it exists
 
         File[] subdirs = modulesDir.listFiles(new FileFilter() {
             public boolean accept(File child) {
                 return child.isDirectory();
             }
         });
         CopyOnWriteMap.Tree<K, V> configurations = new CopyOnWriteMap.Tree<K, V>();
         for (File subdir : subdirs) {
             try {
                 V item = (V) Items.load(parent, subdir);
                 configurations.put(key.call(item), item);
             } catch (IOException e) {
                 e.printStackTrace(); // TODO: logging
             }
         }
 
         return configurations;
     }
     /**
      * {@link Item} -> name function.
      */
     public static final Function1<String, Item> KEYED_BY_NAME = new Function1<String, Item>() {
         public String call(Item item) {
             return item.getName();
         }
     };
 
     /**
      * Creates a {@link TopLevelItem} from the submission of the
      * '/lib/hudson/newFromList/formList' or throws an exception if it fails.
      */
     public synchronized TopLevelItem createTopLevelItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
         acl.checkPermission(Job.CREATE);
 
         TopLevelItem result;
 
         String requestContentType = req.getContentType();
         if (requestContentType == null) {
             throw new Failure("No Content-Type header set");
         }
 
         boolean isXmlSubmission = requestContentType.startsWith("application/xml") || requestContentType.startsWith("text/xml");
 
         String name = req.getParameter("name");
         if (name == null) {
             throw new Failure("Query parameter 'name' is required");
         }
 
         Team requestedTeam = null;
 
         {   // check if the name looks good
             Hudson hudson = Hudson.getInstance();
             Hudson.checkGoodName(name);
             name = name.trim();
             
             if (hudson.isTeamManagementEnabled() && (name.indexOf(TeamManager.TEAM_SEPARATOR) != -1)) {
                 throw new Failure("The job name cannot contain" + TeamManager.TEAM_SEPARATOR + "when team management is enabled. ");
             }
             String existingJobName = name;
             if (hudson.isTeamManagementEnabled()){
                 existingJobName = hudson.getTeamManager().getTeamQualifiedJobName(name);
             }
             if (parent.getItem(existingJobName) != null) {
                 throw new Failure(Messages.Hudson_JobAlreadyExists(existingJobName));
             }
             
             // see if team requested
             if (hudson.isTeamManagementEnabled()) {
                 String team = req.getParameter("team");
                 if (team != null){
                     String teamName = team.trim();
                     if (teamName.length() > 0) {
                         try {
                             requestedTeam = hudson.getTeamManager().findTeam(teamName);
                         } catch (TeamManager.TeamNotFoundException ex) {
                            logger.error("Requested team " + teamName + " not found");
                         }
                     }
                 }
             }
         }
         
         String mode = req.getParameter("mode");
         if (mode != null && mode.equals("copy")) {
             String from = req.getParameter("from");
 
             // resolve a name to Item
             Item src = parent.getItem(from);
             if (src == null) {
                 src = Hudson.getInstance().getItemByFullName(from);
             }
 
             if (src == null) {
                 if (Util.fixEmpty(from) == null) {
                     throw new Failure("Specify which job to copy");
                 } else {
                     throw new Failure("No such job: " + from);
                 }
             }
             if (!(src instanceof TopLevelItem)) {
                 throw new Failure(from + " cannot be copied");
             }
 
             result = copy((TopLevelItem) src, name);
         } else {
             if (isXmlSubmission) {
                 result = createProjectFromXML(name, req.getInputStream());
                 rsp.setStatus(HttpServletResponse.SC_OK);
                 return result;
             } else {
                 if (mode == null) {
                     throw new Failure("No mode given");
                 }
 
                 // create empty job and redirect to the project config screen
                 result = createProject(Items.getDescriptor(mode), name, true);
             }
         }
         
         if (Hudson.getInstance().isTeamManagementEnabled() && requestedTeam != null) {
             TeamManager teamManager = Hudson.getInstance().getTeamManager();
             teamManager.ensureJobInTeam(result, requestedTeam);
         }
 
         rsp.sendRedirect2(redirectAfterCreateItem(req, result));
         return result;
     }
 
     /**
      * Computes the redirection target URL for the newly created
      * {@link TopLevelItem}.
      */
     protected String redirectAfterCreateItem(StaplerRequest req, TopLevelItem result) throws IOException {
         return req.getContextPath() + '/' + result.getUrl() + "configure";
     }
 
     /**
      * Copies an existing {@link TopLevelItem} to a new name.
      *
      * The caller is responsible for calling
      * {@link ItemListener#fireOnCopied(Item, Item)}. This method cannot do that
      * because it doesn't know how to make the newly added item reachable from
      * the parent.
      */
     @SuppressWarnings({"unchecked" })
     public synchronized <T extends TopLevelItem> T copy(T src, String name) throws IOException {
         acl.checkPermission(Job.CREATE);
 
         T result = (T) createProject(src.getDescriptor(), name, false);
 
         // copy config
         Util.copyFile(Items.getConfigFile(src).getFile(), Items.getConfigFile(result).getFile());
 
         // reload from the new config
         result = (T) Items.load(parent, result.getRootDir());
         result.onCopiedFrom(src);
 
         add(result);
         ItemListener.fireOnCopied(src, result);
 
         return result;
     }
 
     public synchronized TopLevelItem createProjectFromXML(String name, InputStream xml) throws IOException {
         acl.checkPermission(Job.CREATE);
 
         // place it as config.xml
         File configXml = Items.getConfigFile(getRootDirFor(name)).getFile();
         configXml.getParentFile().mkdirs();
         try {
             IOUtils.copy(xml, configXml);
 
             // load it
             TopLevelItem result = (TopLevelItem) Items.load(parent, configXml.getParentFile());
             add(result);
 
             ItemListener.fireOnCreated(result);
             Hudson.getInstance().rebuildDependencyGraph();
 
             return result;
         } catch (IOException e) {
             // if anything fails, delete the config file to avoid further confusion
             Util.deleteRecursive(configXml.getParentFile());
             throw e;
         }
     }
 
     public synchronized TopLevelItem createProject(TopLevelItemDescriptor type, String name, boolean notify)
             throws IOException {
         acl.checkPermission(Job.CREATE);
 
         Hudson hudson = Hudson.getInstance();
         String existingJobName = name;
         if (hudson.isTeamManagementEnabled()) {
             existingJobName = hudson.getTeamManager().getTeamQualifiedJobName(name);
         }
         if (parent.getItem(existingJobName) != null) {
             throw new IllegalArgumentException("Job with name " + name + " already exists");
         }
 
         TopLevelItem item;
         try {
             item = type.newInstance(parent, name);
         } catch (Exception e) {
             throw new IllegalArgumentException(e);
         }
         item.onCreatedFromScratch();
         item.save();
         add(item);
 
         if (notify) {
             ItemListener.fireOnCreated(item);
         }
 
         return item;
     }
 }
