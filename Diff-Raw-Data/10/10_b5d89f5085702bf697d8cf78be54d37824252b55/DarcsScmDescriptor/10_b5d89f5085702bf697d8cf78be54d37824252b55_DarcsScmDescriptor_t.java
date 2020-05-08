 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package org.jenkinsci.plugins.darcs;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.Util;
 import hudson.XmlFile;
 import hudson.model.Hudson;
 import hudson.model.TaskListener;
 import hudson.scm.RepositoryBrowser;
 import hudson.scm.SCM;
 import hudson.scm.SCMDescriptor;
 import hudson.util.FormValidation;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.logging.Level;
 import static java.util.logging.Level.WARNING;
 import java.util.logging.Logger;
 import jenkins.model.Jenkins;
 import net.sf.json.JSONObject;
 import org.jenkinsci.plugins.darcs.browsers.DarcsRepositoryBrowser;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.framework.io.ByteBuffer;
 
 /**
  * Inner class of the SCM descriptor.
  *
  * Contains the global configuration options as fields.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 @Extension
 public class DarcsScmDescriptor extends SCMDescriptor<DarcsScm> {
 
     /**
      * Logging facility.
      */
     private static final Logger LOGGER = Logger.getLogger(DarcsScmDescriptor.class.getName());
     /**
      * String displayed in the Jenkins front end.
      */
     private static final String DISPLAY_NAME = "Darcs";
     /**
      * Default name of the Darcs binary.
      */
     private static final String DEFAULT_EXE = "darcs";
     /**
      * The executable.
      *
      * May be null.
      */
     private String darcsExe;
 
     /**
      * Dedicated constructor.
      */
     public DarcsScmDescriptor() {
         super(DarcsScm.class, DarcsRepositoryBrowser.class);
         load();
     }
 
     /**
      * Own implementation of XML configuration loading to inject {@link Jenkins#XSTREAM2} for unmarshalling.
      *
      * TODO Remove this, if it possible to inject XStream into parent implementation.
     *      Since 1.494 Dexcriptor#getConfigFile() is not private anymore. override load()
     *      and call getConfigFile().getXStream().addCompatibilityAlias()
     *
     * <code>
     * public void load() {
     *      getConfigFile().getXStream().addCompatibilityAlias("org.jenkinsci.plugins.darcs.DarcsScm$DescriptorImpl",
     *                                                         DarcsScmDescriptor.class);
     *      super.load();
     * }
     * </code>
      */
     @Override
     public void load() {
         final XmlFile file = new XmlFile(Jenkins.XSTREAM2,
                                          new File(Jenkins.getInstance().getRootDir(), getId() + ".xml"));
         if (!file.exists()) {
             return;
         }
 
         try {
             file.unmarshal(this);
         } catch (IOException e) {
             LOGGER.log(Level.WARNING, "Failed to load " + file, e);
         }
 
         final Class<? extends RepositoryBrowser> rb = repositoryBrowser;
         if (repositoryBrowser != rb) { // XStream may overwrite even the final field.
             try {
                 final Field f = SCMDescriptor.class.getDeclaredField("repositoryBrowser");
                 f.setAccessible(true);
                 f.set(this, rb);
             } catch (NoSuchFieldException e) {
                 LOGGER.log(WARNING, "Failed to overwrite the repositoryBrowser field", e);
             } catch (IllegalAccessException e) {
                 LOGGER.log(WARNING, "Failed to overwrite the repositoryBrowser field", e);
             }
         }
     }
 
     /**
      * Returns the display name.
      *
      * @return display name string
      */
     public String getDisplayName() {
         return DISPLAY_NAME;
     }
 
     /**
      * Returns the executable.
      *
      * @return if {@link #darcsExe} is {@code null} {@link #DEFAULT_EXE} will be returned
      */
     public String getDarcsExe() {
         return null == darcsExe
                 ? DEFAULT_EXE
                 : darcsExe;
     }
 
     @Override
     public SCM newInstance(StaplerRequest req, JSONObject formData) throws FormException {
         return super.newInstance(req, formData);
     }
 
     @Override
     public boolean configure(final StaplerRequest req, final JSONObject formData) throws FormException {
         darcsExe = Util.fixEmpty(req.getParameter("darcs.darcsExe").trim());
         save();
 
         return true;
     }
 
     /**
      * Validated the given executable string if it is a valid Darcs executable.
      *
      * @param value string from the plugin configuration field
      * @return validation object which indicates validation state
      */
     public FormValidation doDarcsExeCheck(@QueryParameter final String value) {
         return FormValidation.validateExecutable(value, new FormValidation.FileValidator() {
             @Override
             public FormValidation validate(final File exe) {
                 try {
                     final Launcher launcher = Hudson.getInstance().createLauncher(TaskListener.NULL);
                     final Launcher.ProcStarter proc = launcher.launch()
                             .cmds(exe, "--version")
                             .stdout(new ByteBuffer());
 
                     if (proc.join() == 0) {
                         return FormValidation.ok();
                     } else {
                         return FormValidation.warning("Could not locate the executable in path");
                     }
                 } catch (IOException e) {
                     // failed
                     LOGGER.log(Level.WARNING, e.toString());
                 } catch (InterruptedException e) {
                     // failed
                     LOGGER.log(Level.WARNING, e.toString());
                 }
 
                 return FormValidation.error("Unable to check darcs version");
             }
         });
     }
 }
