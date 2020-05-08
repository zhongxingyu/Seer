 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  */
 package org.jenkinsci.plugins.darcs;
 
 import org.jenkinsci.plugins.darcs.browsers.DarcsRepositoryBrowser;
 import hudson.EnvVars;
 import hudson.FilePath;
 import hudson.FilePath.FileCallable;
 import hudson.Launcher;
 import hudson.Launcher.LocalLauncher;
 import hudson.init.InitMilestone;
 import hudson.init.Initializer;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.Items;
 import hudson.model.TaskListener;
 import hudson.remoting.VirtualChannel;
 import hudson.scm.ChangeLogParser;
 import hudson.scm.PollingResult;
 import hudson.scm.PollingResult.Change;
 import hudson.scm.SCM;
 import hudson.scm.SCMRevisionState;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.util.logging.Logger;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.xml.sax.SAXException;
 
 /**
  * Darcs is a patch based distributed version control system.
  *
  * Contains the job configuration options as fields.
  *
  * @see http://darcs.net/
  *
  * @author Sven Strittmatter <ich@weltraumschaf.de>
  * @author Ralph Lange <Ralph.Lange@gmx.de>
  */
 public class DarcsScm extends SCM implements Serializable {
 
     /**
      * Serial version UID.
      */
     private static final long serialVersionUID = 3L;
     /**
      * Logging facility.
      */
     private static final Logger LOGGER = Logger.getLogger(DarcsScm.class.getName());
     /**
      * Source repository URL from which we pull.
      */
     private final String source;
     /**
      * Local directory with repository.
      */
     private final String localDir;
     /**
      * Whether to wipe the checked out repository.
      */
     private final boolean clean;
     /**
      * Used repository browser.
      */
     private final DarcsRepositoryBrowser browser;
     /**
      * Reused parser.
      */
     private final transient DarcsChangeLogParser changelogParser;
 
     /**
      * Convenience constructor.
      *
      * Sets local directory to {@link #DEFAULT_LOCAL_DIR}, clean to {@code false} and browser to {@code null}.
      *
      * @param source repository URL from which we pull
      */
     public DarcsScm(final String source) throws SAXException {
         this(source, "", false, null);
     }
 
     /**
      * Dedicated constructor.
      *
      * @param source repository URL from which we pull
      * @param localDir Local directory in the workspace
      * @param clean {@code true} cleans the workspace, {@code false} not
      * @param browser the browser used to browse the repository
      */
     @DataBoundConstructor
     public DarcsScm(final String source, final String localDir, final boolean clean, final DarcsRepositoryBrowser browser)
        throws SAXException {
         super();
         this.source = source;
         this.clean = clean;
         this.browser = browser;
         this.localDir = localDir;
         this.changelogParser = new DarcsChangeLogParser();
     }
 
     /**
      * Get the repositories source URL.
      *
      * @return URL as string
      */
     public String getSource() {
         return source;
     }
 
     /**
      * Get the local directory in the workspace.
      *
      * @return relative path as string
      */
     public String getLocalDir() {
         return localDir;
     }
 
     /**
      * Whether to clean the workspace or not.
      *
      * @return {@code true} if clean is performed, {@code false} else
      */
     public boolean isClean() {
         return clean;
     }
 
     @Override
     public DarcsRepositoryBrowser getBrowser() {
         return browser;
     }
 
     @Override
     public boolean supportsPolling() {
         return false;
     }
 
     @Override
     public boolean requiresWorkspaceForPolling() {
         return false;
     }
 
     @Override
     public DarcsRevisionState calcRevisionsFromBuild(final AbstractBuild<?, ?> build, final Launcher launcher,
             final TaskListener listener) throws IOException, InterruptedException {
         final FilePath localPath = createLocalPath(build.getWorkspace());
         final DarcsRevisionState local = getRevisionState(launcher, listener, localPath.getRemote());
         listener.getLogger()
                 .println("[poll] Calculate revison from build " + local);
 
         return local;
     }
 
     @Override
     protected PollingResult compareRemoteRevisionWith(final AbstractProject<?, ?> project, final Launcher launcher,
        final FilePath workspace, final TaskListener listener, final SCMRevisionState baseline)
        throws IOException, InterruptedException {
         final PrintStream logger = listener.getLogger();
         final DarcsRevisionState localRevisionState;
 
         if (baseline instanceof DarcsRevisionState) {
             localRevisionState = (DarcsRevisionState) baseline;
         } else if (project.getLastBuild() != null) {
             localRevisionState = calcRevisionsFromBuild(project.getLastBuild(), launcher, listener);
         } else {
             localRevisionState = new DarcsRevisionState();
         }
 
         if (null != project.getLastBuild()) {
             logger.println("[poll] Last Build : #" + project.getLastBuild().getNumber());
         } else {
             // If we've never been built before, well, gotta build!
             logger.println("[poll] No previous build, so forcing an initial build.");
 
             return PollingResult.BUILD_NOW;
         }
 
         final Change change;
         final DarcsRevisionState remoteRevisionState = getRevisionState(launcher, listener, source);
 
         logger.printf("[poll] Current remote revision is %s. Local revision is %s.%n",
                 remoteRevisionState, localRevisionState);
 
        if (SCMRevisionState.NONE.equals(localRevisionState)) {
             logger.println("[poll] Does not have a local revision state.");
             change = Change.SIGNIFICANT;
         } else if (localRevisionState.getClass() != DarcsRevisionState.class) {
             // appears that other instances of None occur - its not a singleton.
             // so do a (fugly) class check.
             logger.println("[poll] local revision state is not of type darcs.");
             change = Change.SIGNIFICANT;
         } else if (!remoteRevisionState.equals(localRevisionState)) {
             logger.println("[poll] Local revision state differs from remote.");
 
             if (remoteRevisionState.getChanges().size()
                     < ((DarcsRevisionState) localRevisionState).getChanges().size()) {
                 final FilePath ws = project.getLastBuild().getWorkspace();
 
                 logger.printf("[poll] Remote repo has less patches than local: remote(%s) vs. local(%s). Will wipe "
                         + "workspace %s...%n",
                         remoteRevisionState.getChanges().size(),
                         ((DarcsRevisionState) localRevisionState).getChanges().size(),
                         (null != ws) ? ws.getRemote() : "null");
 
                 if (null != ws) {
                     ws.deleteRecursive();
                 }
             }
 
             change = Change.SIGNIFICANT;
         } else {
             change = Change.NONE;
         }
 
         return new PollingResult(localRevisionState, remoteRevisionState, change);
     }
 
     /**
      * Calculates the revision state of a repository (local or remote).
      *
      * @param launcher
      * @param listener
      * @param repo
      * @return
      * @throws InterruptedException
      */
     DarcsRevisionState getRevisionState(final Launcher launcher, final TaskListener listener, final String repo)
        throws InterruptedException {
         final DarcsCmd cmd;
 
         if (null == launcher) {
             /* Create a launcher on master
              * todo better grab a launcher on 'any slave'
              */
             cmd = new DarcsCmd(new LocalLauncher(listener), EnvVars.masterEnvVars, getDescriptor().getDarcsExe());
         } else {
             cmd = new DarcsCmd(launcher, EnvVars.masterEnvVars, getDescriptor().getDarcsExe());
         }
 
         DarcsRevisionState rev = null;
 
         try {
             final ByteArrayOutputStream changes = cmd.allChanges(repo);
             changelogParser.parse(changes);
             rev = new DarcsRevisionState(changelogParser.parse(changes));
         } catch (Exception e) {
             LOGGER.warning(String.format("Failed to get revision state for repository: %s", e));
         }
 
         return rev;
     }
 
     /**
      * Writes the change log of the last numPatches to the changeLog file.
      *
      * @param launcher
      * @param numPatches
      * @param workspace
      * @param changeLog
      * @throws InterruptedException
      */
     private void createChangeLog(final Launcher launcher, final int numPatches, final FilePath workspace,
             final File changeLog, final BuildListener listener) throws InterruptedException {
         if (0 == numPatches) {
             LOGGER.info("Creating empty changelog.");
             createEmptyChangeLog(changeLog, listener, "changelog");
             return;
         }
 
         final DarcsCmd cmd = new DarcsCmd(launcher, EnvVars.masterEnvVars,
                 getDescriptor().getDarcsExe());
         FileOutputStream fos = null;
 
         try {
             fos = new FileOutputStream(changeLog);
             final FilePath localPath = createLocalPath(workspace);
             final ByteArrayOutputStream changes = cmd.lastSummarizedChanges(localPath.getRemote(), numPatches);
             changes.writeTo(fos);
         } catch (Exception e) {
             final StringWriter w = new StringWriter();
             e.printStackTrace(new PrintWriter(w));
             LOGGER.warning(String.format("Failed to get log from repository: %s", w));
         } finally {
             if (null != fos) {
                 try {
                     fos.close();
                 } catch (IOException ex) {
                     throw new InterruptedException(String.format("Failed to close file output stream!%n%s", ex));
                 }
             }
         }
     }
 
     @Override
     public boolean checkout(final AbstractBuild<?, ?> build, final Launcher launcher, final FilePath workspace,
             final BuildListener listener, final File changelogFile) throws IOException, InterruptedException {
         final FilePath localPath = createLocalPath(workspace);
         final boolean existsRepoinWorkspace = localPath.act(new FileCallable<Boolean>() {
             private static final long serialVersionUID = 1L;
 
             public Boolean invoke(File ws, VirtualChannel channel) throws IOException {
                 final File file = new File(ws, "_darcs");
                 return file.exists();
             }
         });
 
         if (existsRepoinWorkspace && !isClean()) {
             return pullRepo(build, launcher, workspace, listener, changelogFile);
         } else {
             return getRepo(build, launcher, workspace, listener, changelogFile);
         }
     }
 
     /**
      * Counts the patches in a repository.
      *
      * @param build
      * @param launcher
      * @param workspace
      * @param listener
      * @return
      * @throws InterruptedException
      * @throws IOException
      */
     private int countPatches(final AbstractBuild<?, ?> build, final Launcher launcher, final FilePath workspace,
             final BuildListener listener) {
         try {
             final DarcsCmd cmd = new DarcsCmd(launcher,
                     build.getEnvironment(listener),
                     getDescriptor().getDarcsExe());
 
             final FilePath localPath = createLocalPath(workspace);
             return cmd.countChanges(localPath.getRemote());
         } catch (Exception e) {
             listener.error("Failed to count patches in workspace repo:%n", e.toString());
             return 0;
         }
     }
 
     /**
      * Pulls all patches from a remote repository in the workspace repository.
      *
      * @param build
      * @param launcher
      * @param workspace
      * @param listener
      * @param changelogFile
      * @return boolean
      * @throws InterruptedException
      * @throws IOException
      */
     private boolean pullRepo(final AbstractBuild<?, ?> build, final Launcher launcher, final FilePath workspace,
             final BuildListener listener, final File changelogFile) throws InterruptedException, IOException {
         LOGGER.info(String.format("Pulling repo from: %s", source));
         final int preCnt = countPatches(build, launcher, workspace, listener);
         LOGGER.info(String.format("Count of patches pre pulling is %d", preCnt));
 
         try {
             final DarcsCmd cmd = new DarcsCmd(launcher,
                     build.getEnvironment(listener),
                     getDescriptor().getDarcsExe());
             final FilePath localPath = createLocalPath(workspace);
             cmd.pull(localPath.getRemote(), source);
         } catch (Exception e) {
             listener.error("Failed to pull: " + e.toString());
             return false;
         }
 
         final int postCnt = countPatches(build, launcher, workspace, listener);
         LOGGER.info(String.format("Count of patches post pulling is %d", preCnt));
         createChangeLog(launcher, postCnt - preCnt, workspace, changelogFile, listener);
 
         return true;
     }
 
     /**
      * Gets a fresh copy of a remote repository.
      *
      * @param build
      * @param launcher
      * @param workspace
      * @param listener
      * @param changelogFile
      * @return boolean
      * @throws InterruptedException
      */
     private boolean getRepo(final AbstractBuild<?, ?> build, final Launcher launcher, final FilePath workspace,
             final BuildListener listener, final File changeLog) throws InterruptedException {
         LOGGER.info(String.format("Getting repo from: %s", source));
 
         try {
             final FilePath localPath = createLocalPath(workspace);
             localPath.deleteRecursive();
         } catch (IOException e) {
             e.printStackTrace(listener.error("Failed to clean the workspace"));
             return false;
         }
 
         try {
             final DarcsCmd cmd = new DarcsCmd(launcher, build.getEnvironment(listener), getDescriptor().getDarcsExe());
             final FilePath localPath = createLocalPath(workspace);
             cmd.get(localPath.getRemote(), source);
         } catch (Exception e) {
             e.printStackTrace(listener.error("Failed to get repo from " + source));
             return false;
         }
 
         return createEmptyChangeLog(changeLog, listener, "changelog");
     }
 
     @Override
     public ChangeLogParser createChangeLogParser() {
         return changelogParser;
     }
 
     @Override
     public DarcsScmDescriptor getDescriptor() {
         return (DarcsScmDescriptor) super.getDescriptor();
     }
 
     /**
      * Creates a local path relative to the given base.
      *
      * If {@link #localDir} is not {@link null} and not empty a relative path to the given base is created, else the
      * base pat itself is returned. *
      *
      * @param base base of the local path
      * @return local file path.
      */
     private FilePath createLocalPath(final FilePath base) {
         if (null != localDir && localDir.length() > 0) {
             return new FilePath(base, localDir);
         }
 
         return base;
     }
 
    /**
     * Add class name aliases for backward compatibility.
     */
     @Initializer(before = InitMilestone.PLUGINS_STARTED)
     public static void addAliases() {
        // until version 0.3.6 the descriptor was inner class of DarcsScm
         Items.XSTREAM2.addCompatibilityAlias("org.jenkinsci.plugins.darcs.DarcsScm$DescriptorImpl",
                 DarcsScmDescriptor.class);
     }
 
 }
