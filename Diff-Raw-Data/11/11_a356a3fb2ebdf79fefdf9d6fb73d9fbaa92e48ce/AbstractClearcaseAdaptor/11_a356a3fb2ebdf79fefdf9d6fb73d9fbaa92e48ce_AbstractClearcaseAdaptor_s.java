 /*
  * Copyright luntsys (c) 2004-2005, Date: 2004-7-23 Time: 10:06
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 1. Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer. 2. Redistributions in
  * binary form must reproduce the above copyright notice, this list of
  * conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
 package com.luntsys.luntbuild.vcs;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.StringReader;
 import java.text.DateFormatSymbols;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 
 import com.luntsys.luntbuild.ant.Commandline;
 import com.luntsys.luntbuild.db.Build;
 import com.luntsys.luntbuild.db.Schedule;
 import com.luntsys.luntbuild.facades.lb12.BaseClearcaseAdaptorFacade;
 import com.luntsys.luntbuild.facades.lb12.VcsFacade;
 import com.luntsys.luntbuild.utility.DisplayProperty;
 import com.luntsys.luntbuild.utility.Luntbuild;
 import com.luntsys.luntbuild.utility.MyExecTask;
 import com.luntsys.luntbuild.utility.Revisions;
 import com.luntsys.luntbuild.utility.ValidationException;
 
 /**
  * Class responsible for providing common processing strategies for base (i.e.,
  * static) and dynamic Clearcase usage.
  */
 public abstract class AbstractClearcaseAdaptor extends Vcs {
 
     static final long serialVersionUID = 1L;
 
     protected static final SimpleDateFormat CMD_DATE_FORMAT =
             new SimpleDateFormat("dd-MMMM-yyyy.HH:mm:ss",
                     new DateFormatSymbols(Locale.ENGLISH));
 
     /**
      * Server storage location to create clearcase view.
      */
     protected String viewStgLoc;
 
     protected String vws;
 
     protected String viewTag;
 
     /**
      * Config spec for the clearcase view
      */
     protected String viewCfgSpec;
 
     protected String cleartoolDir;
 
     /**
      * Config to detect modifications. This is used to dertermine whether or not
      * need to perform next build
      */
     protected String modificationDetectionConfig;
 
     protected String mkviewExtraOpts;
 
     protected String formatParams;
 
     /**
      * The ucm stream attach to
      */
     protected String ucmStream;
 
     /**
      * 3-way variable (<code>null</code>, <code>TRUE</code>,
      * <code>FALSE</code>) to capture whether the view exists. Using this is
      * helpful in that a <code>null</code> value can signify "unknown." if the
      * value is <code>null</code>, then <code>ccViewExists</code> will
      * compute one. This saves us the potentially costly lsview invocation.
      */
     protected Boolean m_viewExists;
 
     public final String getIconName() {
         return "baseclearcase.jpg";
     }
 
     /**
      * Allows subclasses to provide their own list of properties.
      *
      * @return List of DisplayProperty objects
      */
     protected abstract List getClearcaseAdaptorProperties();
 
     public final String getModificationDetectionConfig() {
         return this.modificationDetectionConfig;
     }
 
     public final void setModificationDetectionConfig(
             final String modificationDetectionConfig) {
         this.modificationDetectionConfig = modificationDetectionConfig;
     }
 
     public final String getViewStgLoc() {
         return this.viewStgLoc;
     }
 
     public final void setViewStgLoc(final String viewStgLoc) {
         this.viewStgLoc = viewStgLoc;
     }
 
     public final String getVws() {
         return this.vws;
     }
 
     public final void setVws(final String vws) {
         this.vws = vws;
     }
 
     public String getViewTag() {
         return viewTag;
     }
 
     public void setViewTag(String viewTag) {
         this.viewTag = viewTag;
     }
 
     public final String getViewName(final Schedule schedule) {
         return (null == getViewTag() || getViewTag().length() == 0)
                 ? (Luntbuild.getHostName() + "-" + schedule.getJobName())
                 : getViewTag();
     }
 
     public final String getViewCfgSpec() {
         return this.viewCfgSpec;
     }
 
     public final void setViewCfgSpec(final String viewCfgSpec) {
         this.viewCfgSpec = viewCfgSpec;
     }
 
     public final String getMkviewExtraOpts() {
         return this.mkviewExtraOpts;
     }
 
     public final void setMkviewExtraOpts(final String mkviewExtraOpts) {
         this.mkviewExtraOpts = mkviewExtraOpts;
     }
 
     public final String getUcmStream() {
         return this.ucmStream;
     }
 
     public final void setUcmStream(final String ucmStream) {
         this.ucmStream = ucmStream;
     }
 
     public final List getVcsSpecificProperties() {
         final List properties = new ArrayList();
         properties.add(new DisplayProperty() {
             public String getDescription() {
                 return "Explicitly defines the view tag to use.  Default is "
                         + "a combination of your luntbuild installation directory, "
                         + "project name, etc.";
             }
 
             public String getDisplayName() {
                 return "View tag";
             }
 
             public String getValue() {
                 return getViewTag();
             }
 
             public void setValue(String value) {
                 setViewTag(value);
             }
 
             public boolean isRequired() {
                 return false;
             }
         });
         properties.add(new DisplayProperty() {
             public String getDisplayName() {
                 return "Clearcase view stgloc name";
             }
 
             public String getDescription() {
                 return "Name of the Clearcase server-side view storage location which will be used as"
                         + "-stgloc option when creating Clearcase view for the current project. Either this property or "
                         + "\"Explicit path for view storage\" property should be specified.";
             }
 
             public boolean isRequired() {
                 return false;
             }
 
             public String getValue() {
                 return getViewStgLoc();
             }
 
             public void setValue(final String value) {
                 setViewStgLoc(value);
             }
         });
         properties.add(new DisplayProperty() {
             public String getDisplayName() {
                 return "Explicit path for view storage";
             }
 
             public String getDescription() {
                 return "This property is required only when the \"Clearcase view stgloc name\" property is empty. "
                         + "If specified, it will be used as -vws option instead of using the -stgloc option to create Clearcase "
                         + "view for the current project.\n"
                         + "NOTE. This value should be a writable UNC path on Windows platform.";
             }
 
             public boolean isRequired() {
                 return false;
             }
 
             public String getValue() {
                 return getVws();
             }
 
             public void setValue(final String value) {
                 setVws(value);
             }
         });
         properties.add(new DisplayProperty() {
             public String getDisplayName() {
                 return "Config spec";
             }
 
             public String getDescription() {
                 return "Config spec used by Luntbuild to create Clearcase snapshot view for a build.";
             }
 
             public boolean isMultiLine() {
                 return true;
             }
 
             public String getValue() {
                 return getViewCfgSpec();
             }
 
             public void setValue(final String value) {
                 setViewCfgSpec(value);
             }
         });
         properties.add(new DisplayProperty() {
             public String getDisplayName() {
                 return "Modification detection config";
             }
 
             public String getDescription() {
                 return "This property will take effect if there are some LATEST versions from some branch "
                         + "to fetch in the above config spec. It is used by Luntbuild to determine, if there "
                         + "are any changes in the repository since the last build. "
                         + "This property consists of multiple entries, where each entry is of the format "
                         + "\"<path>[:<branch>]\". <path> is a path inside a vob, which should be visible "
                         + "by the above config spec. Luntbuild will lookup any changes at any branch "
                         + "inside this path recursively, or it will lookup changes at the specified branch, if <branch> is "
                         + "specified. Multiple entries are separated by \";\" or line terminator. Refer to "
                         + "the User's Guide for details.";
             }
 
             public boolean isMultiLine() {
                 return true;
             }
 
             public boolean isRequired() {
                 return false;
             }
 
             public String getValue() {
                 return getModificationDetectionConfig();
             }
 
             public void setValue(final String value) {
                 setModificationDetectionConfig(value);
             }
         });
         properties.add(new DisplayProperty() {
             public String getDisplayName() {
                 return "Options for mkview command";
             }
 
             public String getDescription() {
                 return "You may optionally specify extra options for the cleartool mkview "
                         + "sub command used by Luntbuild to create related Clearcase snapshot "
                         + "view for the current project. Options that can be specified here are restricted to -tmode, "
                         + "-ptime, and -cachesize. For example you can specify \"-tmode insert_cr\" "
                         + "to use Windows end of line text mode.";
             }
 
             public boolean isRequired() {
                 return false;
             }
 
             public String getValue() {
                 return getMkviewExtraOpts();
             }
 
             public void setValue(final String value) {
                 setMkviewExtraOpts(value);
             }
         });
         properties.add(new DisplayProperty() {
             public String getDisplayName() {
                 return "Path for cleartool executable";
             }
 
             public String getDescription() {
                 return "The directory path, where your cleartool executable file resides in. "
                         + "It should be specified here, if it does not exist in the system path.";
             }
 
             public boolean isRequired() {
                 return false;
             }
 
             public String getValue() {
                 return getCleartoolDir();
             }
 
             public void setValue(final String value) {
                 setCleartoolDir(value);
             }
         });
         properties.add(new DisplayProperty() {
             public String getDescription() {
                 return "Additional information to pull from the "
                         + "cleartool lshistory command.  Please see the "
                         + "clearcase man pages on fmt_ccase for more "
                         + "information.  The value entered here will be "
                         + "appended to the information already retrieved by "
                         + "luntbuild (date:%d user:%u action:%e %n\\n)";
             }
 
             public String getDisplayName() {
                 return "History format parameters";
             }
 
             public String getValue() {
                 return getFormatParams();
             }
 
             public void setValue(final String value) {
                 setFormatParams(value);
             }
 
             public boolean isRequired() {
                 return false;
             }
         });
         properties.addAll(getClearcaseAdaptorProperties());
         return properties;
     }
 
     public final String getCleartoolDir() {
         return this.cleartoolDir;
     }
 
     public final void setCleartoolDir(final String cleartoolDir) {
         this.cleartoolDir = cleartoolDir;
     }
 
     public final void validateProperties() {
         super.validateProperties();
         if (Luntbuild.isEmpty(this.viewStgLoc) && Luntbuild.isEmpty(this.vws)) {
             throw new ValidationException(
                     "Both \"Clearcase view storage name\" and  \"Explicit path for view storage\" "
                             + "are empty. You should specify at least one of them to store the view information");
         }
         if (!Luntbuild.isEmpty(getModificationDetectionConfig())) {
             final BufferedReader reader =
                     new BufferedReader(
                             new StringReader(getModificationDetectionConfig()
                                     .replace(';', '\n')));
             try {
                 String line;
                 while ((line = reader.readLine()) != null) {
                     final String fields[] = line.split(":");
                     if (fields.length != 1 && fields.length != 2) {
                         throw new ValidationException(
                                 "Invalid entry of the property \"modification detection config\": "
                                         + line);
                     }
                 }
             } catch (final IOException e) {
                 // ignores
             }
         }
         validateClearcaseAdaptorProperties();
     }
 
     /**
      * Allow subclasses to validate their properties.
      */
     protected abstract void validateClearcaseAdaptorProperties();
 
     /**
      * Build the executable part of a commandline object
      *
      * @return
      */
     protected final Commandline buildCleartoolExecutable() {
         final Commandline cmdLine = new Commandline();
         if (Luntbuild.isEmpty(getCleartoolDir())) {
             cmdLine.setExecutable("cleartool");
         } else {
             cmdLine.setExecutable(Luntbuild.concatPath(getCleartoolDir(),
                     "cleartool"));
         }
         return cmdLine;
     }
 
     /**
      * Does the clearcase view represented by this vcs object exists
      *
      * @param antProject
      * @return
      */
     protected final boolean ccViewExists(final Schedule schedule,
             final Project antProject) {
         if (null == this.m_viewExists) {
             final Commandline cmdLine = buildCleartoolExecutable();
             cmdLine.createArgument().setLine("lsview " + getViewName(schedule));
             try {
                 new MyExecTask("lsview", antProject, cmdLine, Project.MSG_INFO)
                         .execute();
                 this.m_viewExists = Boolean.TRUE;
             } catch (final BuildException e) {
                 this.m_viewExists = Boolean.FALSE;
             }
         }
         return this.m_viewExists.booleanValue();
     }
 
     /**
      * Delete the the clearcase view represented by this vcs object
      *
      * @param viewPath path to snapshot view
      * @param antProject
      */
     protected final void deleteCcView(final String viewPath,
             final Project antProject) {
         final Commandline cmdLine = buildCleartoolExecutable();
         cmdLine.createArgument().setLine("rmview -force");
         cmdLine.createArgument().setValue(viewPath);
         try {
             new MyExecTask("rmview", antProject, cmdLine, Project.MSG_INFO)
                     .execute();
         } finally {
             this.m_viewExists = null;
         }
     }
 
     /**
      * Helper method that can be called to ensure a view exists and create it if
      * it doesn't.
      *
      * @param schedule
      * @param antProject
      */
     protected void ensureViewPresent(Schedule schedule, Project antProject) {
         if (!ccViewExists(schedule, antProject)) {
             createCcView(schedule, antProject);
         }
     }
 
     /**
      * Create a clearcase view represented by this vcs object
      *
      * @param antProject
      */
     protected final void createCcView(final Schedule schedule,
             final Project antProject) {
         final String workingDir = getClearcaseWorkDirRaw(schedule);
         final Commandline cmdLine = buildCleartoolExecutable();
         String options = "-tag " + getViewName(schedule);
         if (!Luntbuild.isEmpty(getMkviewExtraOpts())) {
             options += getMkviewExtraOpts();
         }
         if (!Luntbuild.isEmpty(getUcmStream())) {
             options += " -stream " + getUcmStream();
         }
         if (!Luntbuild.isEmpty(this.vws)) {
             if (isSnapshot()) {
                 options += " -vws " + this.vws;
             }
         } else {
             options += " -stgloc " + this.viewStgLoc;
         }
         cmdLine.createArgument().setLine(
                 "mkview " + (isSnapshot() ? "-snapshot " : "") + options);
 
         cmdLine.createArgument().setValue(isSnapshot() ? workingDir : this.vws);
         new MyExecTask("mkview", antProject, cmdLine, Project.MSG_INFO)
                 .execute();
         m_viewExists = null;
         postCreateCcView(schedule, antProject);
     }
 
     /**
      * @param schedule
      * @return the clearcase working directory. This is the directory from which
      *         clearcase commands should be invoked.
      */
     protected abstract String getClearcaseWorkDirRaw(final Schedule schedule);
 
     /**
      * Same as {{@link #getClearcaseWorkDirRaw(Schedule)}, but with
      * backslashes escaped for OGNL's benefit.
      *
      * @param schedule
      * @return
      * @see #getClearcaseWorkDirRaw(Schedule)
      */
     public final String getClearcaseWorkDir(final Schedule schedule) {
         // deal with backslashes for OGNL's benefit
         return getClearcaseWorkDirRaw(schedule).replaceAll("\\\\", "\\\\\\\\");
     }
 
     /**
      * Method invoked after a view is created, allowing subclasses to do some
      * post processing.
      *
      * @param schedule
      * @param antProject
      */
     protected abstract void postCreateCcView(Schedule schedule,
             Project antProject);
 
     /**
      * @return Whether this adaptor defines a snapshot or dynamic view.
      */
     protected abstract boolean isSnapshot();
 
     /**
      * Method invoked after a view's config spec has been set, allowing
      * subclasses to do some post processing.
      *
      * @param antProject
      * @param workingDir
      */
     protected abstract void postSetCs(Project antProject, String workingDir);
 
     /**
      * Set the config spec for specified clearcase view
      *
      * @param antProject
      */
     public final void setCcViewCfgSpec(final Schedule schedule,
             final String viewCfgSpec, final int outputLogPriority,
             final Project antProject) {
         final String workingDir = getClearcaseWorkDirRaw(schedule);
         File cfgSpecFile = null;
         PrintStream cfgSpecStream = null;
         try {
             cfgSpecFile =
                     File.createTempFile(getViewName(schedule), "cfgspec",
                             new File(Luntbuild.installDir + "/tmp"));
             cfgSpecStream = new PrintStream(new FileOutputStream(cfgSpecFile));
             final BufferedReader reader =
                     new BufferedReader(new StringReader(viewCfgSpec.replace(
                             ';', '\n')));
             String line;
             while ((line = reader.readLine()) != null) {
                 cfgSpecStream.println(line);
             }
             cfgSpecStream.close();
             cfgSpecStream = null;
 
             final Commandline cmdLine = buildCleartoolExecutable();
             cmdLine.createArgument().setLine(
                     "setcs -tag " + getViewName(schedule));
             cmdLine.createArgument().setValue(cfgSpecFile.getAbsolutePath());
 
             new MyExecTask("setcs", antProject, workingDir, cmdLine, null,
                     "yes\n", outputLogPriority).execute();
         } catch (final FileNotFoundException e) {
             throw new BuildException(e.getMessage());
         } catch (final IOException e) {
             throw new BuildException(e.getMessage());
         } finally {
             if (cfgSpecStream != null) {
                 cfgSpecStream.close();
             }
             if (cfgSpecFile != null) {
                 cfgSpecFile.delete();
             }
             postSetCs(antProject, workingDir);
         }
     }
 
     /**
      * Determines if a given config spec denotes some LATEST version
      *
      * @return
      */
     protected final boolean containLatestVersion() {
         final BufferedReader reader =
                 new BufferedReader(new StringReader(this.viewCfgSpec.replace(
                         ';', '\n')));
         try {
             String line;
             final Pattern pattern =
                     Pattern.compile("^\\s*element(.*)",
                             Pattern.CASE_INSENSITIVE);
             while ((line = reader.readLine()) != null) {
                 final Matcher matcher = pattern.matcher(line);
                 if (matcher.find()) {
                     if (matcher.group(1).matches(".*LATEST.*")) {
                         return true;
                     }
                 }
             }
         } catch (final IOException e) {
         }
         return false;
     }
 
     /**
      * Allows subclasses to do whatever prep work is necessary just before
      * invoking lshistory.
      *
      * @param workingSchedule
      * @param antProject
      * @param workingDir
      */
     protected abstract void prepForHistory(Schedule workingSchedule,
             Project antProject, String workingDir);
 
     public final Revisions getRevisionsSince(final Date sinceDate,
             final Schedule workingSchedule, final Project antProject) {
         ensureViewPresent(workingSchedule, antProject);
 
         final String workingDir = getClearcaseWorkDirRaw(workingSchedule);
         final Revisions revisions = new Revisions();
 
         if (!containLatestVersion()
                 || Luntbuild.isEmpty(getModificationDetectionConfig())) {
             return revisions;
         }
 
         // prepare project working directory to run cleartool history command
         prepForHistory(workingSchedule, antProject, workingDir);
 
         final BufferedReader reader =
                 new BufferedReader(new StringReader(
                         getModificationDetectionConfig().replace(';', '\n')));
         try {
             String line;
             String path, branch;
             while ((line = reader.readLine()) != null) {
                 final String fields[] = line.split(":");
                 if (fields.length == 2) {
                     path = Luntbuild.concatPath(workingDir, fields[0].trim());
                     branch = fields[1].trim();
                 } else if (fields.length == 1) {
                     path = Luntbuild.concatPath(workingDir, fields[0].trim());
                     branch = null;
                 } else {
                     throw new BuildException(
                             "Invalid entry of the property \"modification "
                                     + "detection config\": " + line);
                 }
                 if (!new File(path).exists()) {
                     throw new BuildException(
                             "Invalid entry of property \"modification "
                                     + "detection config\": "
                                     + line
                                     + ", path not found using specified config spec");
                 }
                 final Commandline cmdLine = buildCleartoolExecutable();
                 cmdLine.createArgument().setLine(
                         "lshistory -fmt \"date:%d user:%u action:%e %n\n"
                                 + getFormatParams() + "\" -nco -r");
                 if (branch != null) {
                     cmdLine.createArgument().setLine("-branch " + branch);
                 }
                 cmdLine.createArgument().setLine(
                         "-since " + CMD_DATE_FORMAT.format(sinceDate));
                 cmdLine.createArgument().setValue(path);
                 final Pattern authorPattern =
                         Pattern.compile("date:.*user:(.*)action:.*");
                 new MyExecTask("history", antProject, workingDir, cmdLine,
                         null, null, -1) {
                     public void handleStdout(final String line) {
                         revisions.getChangeLogs().add(line);
                         revisions.setFileModified(true);
                         final Matcher matcher = authorPattern.matcher(line);
                         if (matcher.find()) {
                             revisions.getChangeLogins().add(
                                     matcher.group(1).trim());
                         }
                     }
                 }.execute();
             }
         } catch (final IOException e) {
             // ignores
         }
 
         return revisions;
     }
 
     public final String getFormatParams() {
         return this.formatParams;
     }
 
     public final void setFormatParams(final String logFormat) {
         this.formatParams = logFormat;
     }
 
     public final Module createNewModule() {
         return null; // module definition not applicable for current vcs
     }
 
     public final Module createNewModule(Module module) {
         return null; // module definition not applicable for current vcs
     }
 
     public final void saveToFacade(
             final com.luntsys.luntbuild.facades.lb12.VcsFacade facade) {
         final BaseClearcaseAdaptorFacade baseClearcaseFacade =
                 (BaseClearcaseAdaptorFacade) facade;
         baseClearcaseFacade.setMkviewExtraOpts(getMkviewExtraOpts());
         baseClearcaseFacade
                 .setModificationDetectionConfig(getModificationDetectionConfig());
         baseClearcaseFacade.setViewCfgSpec(getViewCfgSpec());
         baseClearcaseFacade.setViewStgLoc(getViewStgLoc());
         baseClearcaseFacade.setVws(getVws());
         baseClearcaseFacade.setCleartoolDir(getCleartoolDir());
         baseClearcaseFacade.setFormatParams(getFormatParams());
         baseClearcaseFacade.setViewTag(getViewTag());
         saveAdditionalStuffToFacade(facade);
     }
 
     protected abstract void saveAdditionalStuffToFacade(VcsFacade facade);
 
     protected abstract void loadAdditionalStuffFromFacade(VcsFacade facade);
 
     public final void loadFromFacade(VcsFacade facade) {
         if (!(facade instanceof BaseClearcaseAdaptorFacade))
             throw new RuntimeException("Invalid facade class: "
                     + facade.getClass().getName());
         BaseClearcaseAdaptorFacade baseClearcaseFacade =
                 (BaseClearcaseAdaptorFacade) facade;
         setMkviewExtraOpts(baseClearcaseFacade.getMkviewExtraOpts());
         setModificationDetectionConfig(baseClearcaseFacade
                 .getModificationDetectionConfig());
         setViewCfgSpec(baseClearcaseFacade.getViewCfgSpec());
         setViewStgLoc(baseClearcaseFacade.getViewStgLoc());
         setVws(baseClearcaseFacade.getVws());
         setCleartoolDir(baseClearcaseFacade.getCleartoolDir());
         setViewTag(baseClearcaseFacade.getViewTag());
         loadAdditionalStuffFromFacade(facade);
     }
 
     /**
      * Helper method that will invoke cleartool
      *
      * @param build
      * @param project
      * @param cleartoolCmd
      * @param argLine
      */
     protected void cleartool(Build build, Project project, String cleartoolCmd,
             String argLine) {
         Commandline cmd = buildCleartoolExecutable();
         cmd.createArgument().setValue(cleartoolCmd);
         cmd.createArgument().setLine(argLine);
         new MyExecTask(cleartoolCmd, project, getClearcaseWorkDirRaw(build
                 .getSchedule()), cmd, null, null, Project.MSG_INFO).execute();
     }
 }
