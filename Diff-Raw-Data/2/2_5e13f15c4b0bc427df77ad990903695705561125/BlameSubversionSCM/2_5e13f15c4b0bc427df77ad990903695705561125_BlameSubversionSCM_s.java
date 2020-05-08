 /*
  * The MIT License
  * 
  * Copyright (c) 2004-2010, Sun Microsystems, Inc., Kohsuke Kawaguchi, Fulvio Cavarretta,
  * Jean-Baptiste Quenot, Luca Domenico Milanesio, Renaud Bruyeron, Stephen Connolly,
  * Tom Huybrechts, Yahoo! Inc.
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
 package hudson.scm;
 
 import static hudson.Util.fixEmptyAndTrim;
 import static java.util.logging.Level.FINE;
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.Util;
 import hudson.XmlFile;
 import hudson.FilePath.FileCallable;
 import hudson.matrix.MatrixBuild;
 import hudson.matrix.MatrixConfiguration;
 import hudson.matrix.MatrixRun;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.Cause;
 import hudson.model.Hudson;
 import hudson.model.ParametersAction;
 import hudson.model.TaskListener;
 import hudson.model.Cause.UpstreamCause;
 import hudson.remoting.Callable;
 import hudson.remoting.Channel;
 import hudson.remoting.VirtualChannel;
 import hudson.scm.UserProvidedCredential.AuthenticationManagerImpl;
 import hudson.scm.subversion.Messages;
 import hudson.triggers.SCMTrigger;
 import hudson.util.IOException2;
 import hudson.util.Scrambler;
 import hudson.util.Secret;
 import hudson.util.StreamCopyThread;
 import hudson.util.XStream2;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.UUID;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import javax.xml.transform.stream.StreamResult;
 
 import net.sf.json.JSONObject;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.taskdefs.Chmod;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.export.Exported;
 import org.kohsuke.stapler.export.ExportedBean;
 import org.tmatesoft.svn.core.SVNAuthenticationException;
 import org.tmatesoft.svn.core.SVNDepth;
 import org.tmatesoft.svn.core.SVNDirEntry;
 import org.tmatesoft.svn.core.SVNErrorCode;
 import org.tmatesoft.svn.core.SVNErrorMessage;
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.SVNNodeKind;
 import org.tmatesoft.svn.core.SVNURL;
 import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
 import org.tmatesoft.svn.core.auth.ISVNAuthenticationOutcomeListener;
 import org.tmatesoft.svn.core.auth.ISVNAuthenticationProvider;
 import org.tmatesoft.svn.core.auth.SVNAuthentication;
 import org.tmatesoft.svn.core.auth.SVNPasswordAuthentication;
 import org.tmatesoft.svn.core.auth.SVNSSHAuthentication;
 import org.tmatesoft.svn.core.auth.SVNSSLAuthentication;
 import org.tmatesoft.svn.core.auth.SVNUserNameAuthentication;
 import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
 import org.tmatesoft.svn.core.internal.io.dav.http.DefaultHTTPConnectionFactory;
 import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
 import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
 import org.tmatesoft.svn.core.internal.wc.SVNExternal;
 import org.tmatesoft.svn.core.internal.wc.admin.SVNAdminAreaFactory;
 import org.tmatesoft.svn.core.io.SVNRepository;
 import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
 import org.tmatesoft.svn.core.wc.SVNClientManager;
 import org.tmatesoft.svn.core.wc.SVNInfo;
 import org.tmatesoft.svn.core.wc.SVNRevision;
 import org.tmatesoft.svn.core.wc.SVNUpdateClient;
 import org.tmatesoft.svn.core.wc.SVNWCClient;
 import org.tmatesoft.svn.core.wc.SVNWCUtil;
 
 import com.thoughtworks.xstream.XStream;
 import com.trilead.ssh2.DebugLogger;
 import com.trilead.ssh2.SCPClient;
 import com.trilead.ssh2.crypto.Base64;
 
 /**
  * Blame Subversion SCM.
  * 
  * 
  * Modify: changed by tang
  * 
  * @author tang,Kohsuke Kawaguchi
  */
 public class BlameSubversionSCM extends SCM implements Serializable {
 	/**
 	 * the locations field is used to store all configured SVN locations (with
 	 * their local and remote part). Direct access to this field should be
 	 * avoided and the getLocations() method should be used instead. This is
 	 * needed to make importing of old hudson-configurations possible as
 	 * getLocations() will check if the modules field has been set and import
 	 * the data.
 	 * 
 	 * @since 1.91
 	 */
 
 	private boolean alwaysCollectSVNInfo;
 
 	public boolean getAlwaysCollectSVNInfo() {
 		return alwaysCollectSVNInfo;
 	}
 
 	private ModuleLocation[] locations = new ModuleLocation[0];
 
 	private boolean useUpdate;
 	private boolean doRevert;
 	// private final BlameSubversionRepositoryBrowser browser;
 	private String excludedRegions;
 	private String includedRegions;
 	private String excludedUsers;
 	/**
 	 * Revision property names that are ignored for the sake of polling.
 	 * Whitespace separated, possibly null.
 	 */
 	private String excludedRevprop;
 	private String excludedCommitMessages;
 
 	// No longer in use but left for serialization compatibility.
 	@Deprecated
 	private String modules;
 
 	// /**
 	// * @deprecated as of 1.286
 	// */
 	// public BlameSubversionSCM(String[] remoteLocations, String[]
 	// localLocations,
 	// boolean useUpdate, BlameSubversionRepositoryBrowser browser) {
 	// this(remoteLocations,localLocations, useUpdate, browser, null, null,
 	// null);
 	// }
 	//
 	// /**
 	// * @deprecated as of 1.311
 	// */
 	// public BlameSubversionSCM(String[] remoteLocations, String[]
 	// localLocations,
 	// boolean useUpdate, BlameSubversionRepositoryBrowser browser, String
 	// excludedRegions) {
 	// this(ModuleLocation.parse(remoteLocations,localLocations), useUpdate,
 	// false, browser, excludedRegions, null, null, null);
 	// }
 	//
 	// /**
 	// * @deprecated as of 1.315
 	// */
 	// public BlameSubversionSCM(String[] remoteLocations, String[]
 	// localLocations,
 	// boolean useUpdate, BlameSubversionRepositoryBrowser browser, String
 	// excludedRegions, String excludedUsers, String excludedRevprop) {
 	// this(ModuleLocation.parse(remoteLocations,localLocations), useUpdate,
 	// false, browser, excludedRegions, excludedUsers, excludedRevprop, null);
 	// }
 	//
 	// /**
 	// * @deprecated as of 1.315
 	// */
 	// public BlameSubversionSCM(List<ModuleLocation> locations,
 	// boolean useUpdate, BlameSubversionRepositoryBrowser browser, String
 	// excludedRegions) {
 	// this(locations, useUpdate, false, browser, excludedRegions, null, null,
 	// null);
 	// }
 	//    
 	// /**
 	// * @deprecated as of 1.324
 	// */
 	// public BlameSubversionSCM(List<ModuleLocation> locations,
 	// boolean useUpdate, BlameSubversionRepositoryBrowser browser, String
 	// excludedRegions, String excludedUsers, String excludedRevprop) {
 	// this(locations, useUpdate, false, browser, excludedRegions,
 	// excludedUsers, excludedRevprop, null);
 	// }
 	//
 	// /**
 	// * @deprecated as of 1.328
 	// */
 	// public BlameSubversionSCM(List<ModuleLocation> locations,
 	// boolean useUpdate, BlameSubversionRepositoryBrowser browser, String
 	// excludedRegions, String excludedUsers, String excludedRevprop, String
 	// excludedCommitMessages) {
 	// this(locations, useUpdate, false, browser, excludedRegions,
 	// excludedUsers, excludedRevprop, excludedCommitMessages);
 	// }
 	//
 	// /**
 	// * @deprecated as of 1.xxx
 	// */
 	// public BlameSubversionSCM(List<ModuleLocation> locations,
 	// boolean useUpdate, boolean doRevert, BlameSubversionRepositoryBrowser
 	// browser, String excludedRegions, String excludedUsers, String
 	// excludedRevprop, String excludedCommitMessages) {
 	// this(locations, useUpdate, doRevert, browser, excludedRegions,
 	// excludedUsers, excludedRevprop, excludedCommitMessages, null);
 	// }
 	//    
 	// @DataBoundConstructor
 	// public BlameSubversionSCM(List<ModuleLocation> locations,
 	// boolean useUpdate, boolean doRevert, BlameSubversionRepositoryBrowser
 	// browser, String excludedRegions, String excludedUsers, String
 	// excludedRevprop, String excludedCommitMessages,
 	// String includedRegions) {
 	//
 	// for (Iterator<ModuleLocation> itr = locations.iterator(); itr.hasNext();)
 	// {
 	// ModuleLocation ml = itr.next();
 	// if(ml.remote==null) itr.remove();
 	// }
 	// this.locations = locations.toArray(new ModuleLocation[locations.size()]);
 	//
 	// this.useUpdate = useUpdate;
 	// this.doRevert = doRevert;
 	// this.browser = browser;
 	// this.excludedRegions = excludedRegions;
 	// this.excludedUsers = excludedUsers;
 	// this.excludedRevprop = excludedRevprop;
 	// this.excludedCommitMessages = excludedCommitMessages;
 	// this.includedRegions = includedRegions;
 	// }
 
 	// /**
 	// * Convenience constructor, especially during testing.
 	// */
 	// public BlameSubversionSCM(String svnUrl) {
 	// this(svnUrl,".");
 	// }
 
 	// /**
 	// * Convenience constructor, especially during testing.
 	// */
 	// public BlameSubversionSCM(String svnUrl, String local) {
 	// this(new String[]{svnUrl},new String[]{local},true,null,null,null,null);
 	// }
 
 	@DataBoundConstructor
 	public BlameSubversionSCM(boolean alwaysCollectSVNInfo) {
 		this.alwaysCollectSVNInfo = alwaysCollectSVNInfo;
 	}
 
 	/**
 	 * @deprecated as of 1.91. Use {@link #getLocations()} instead.
 	 */
 	public String getModules() {
 		return null;
 	}
 
 	/**
 	 * list of all configured svn locations
 	 * 
 	 * @since 1.91
 	 */
 	@Exported
 	public ModuleLocation[] getLocations() {
 		return getLocations(null);
 	}
 
 	/**
 	 * list of all configured svn locations, expanded according to build
 	 * parameters values;
 	 * 
 	 * @param build
 	 *            If non-null, variable expansions are performed against the
 	 *            build parameters.
 	 * 
 	 * @since 1.252
 	 */
 	public ModuleLocation[] getLocations(AbstractBuild<?, ?> build) {
 		// check if we've got a old location
 		if (modules != null) {
 			// import the old configuration
 			List<ModuleLocation> oldLocations = new ArrayList<ModuleLocation>();
 			StringTokenizer tokens = new StringTokenizer(modules);
 			while (tokens.hasMoreTokens()) {
 				// the remote (repository location)
 				// the normalized name is always without the trailing '/'
 				String remoteLoc = Util.removeTrailingSlash(tokens.nextToken());
 
 				oldLocations.add(new ModuleLocation(remoteLoc, null));
 			}
 
 			locations = oldLocations.toArray(new ModuleLocation[oldLocations
 					.size()]);
 			modules = null;
 		}
 
 		if (build == null)
 			return locations;
 
 		ModuleLocation[] outLocations = new ModuleLocation[locations.length];
 		for (int i = 0; i < outLocations.length; i++) {
 			outLocations[i] = locations[i].getExpandedLocation(build);
 		}
 
 		return outLocations;
 	}
 
 	@Exported
 	public boolean isUseUpdate() {
 		return useUpdate;
 	}
 
 	@Exported
 	public boolean isDoRevert() {
 		return doRevert;
 	}
 
 	// @Override
 	// @Exported
 	// public BlameSubversionRepositoryBrowser getBrowser() {
 	// return browser;
 	// }
 
 	@Exported
 	public String getExcludedRegions() {
 		return excludedRegions;
 	}
 
 	public String[] getExcludedRegionsNormalized() {
 		return (excludedRegions == null || excludedRegions.trim().equals("")) ? null
 				: excludedRegions.split("[\\r\\n]+");
 	}
 
 	private Pattern[] getExcludedRegionsPatterns() {
 		String[] excluded = getExcludedRegionsNormalized();
 		if (excluded != null) {
 			Pattern[] patterns = new Pattern[excluded.length];
 
 			int i = 0;
 			for (String excludedRegion : excluded) {
 				patterns[i++] = Pattern.compile(excludedRegion);
 			}
 
 			return patterns;
 		}
 
 		return new Pattern[0];
 	}
 
 	@Exported
 	public String getIncludedRegions() {
 		return includedRegions;
 	}
 
 	public String[] getIncludedRegionsNormalized() {
 		return (includedRegions == null || includedRegions.trim().equals("")) ? null
 				: includedRegions.split("[\\r\\n]+");
 	}
 
 	private Pattern[] getIncludedRegionsPatterns() {
 		String[] included = getIncludedRegionsNormalized();
 		if (included != null) {
 			Pattern[] patterns = new Pattern[included.length];
 
 			int i = 0;
 			for (String includedRegion : included) {
 				patterns[i++] = Pattern.compile(includedRegion);
 			}
 
 			return patterns;
 		}
 
 		return new Pattern[0];
 	}
 
 	@Exported
 	public String getExcludedUsers() {
 		return excludedUsers;
 	}
 
 	public Set<String> getExcludedUsersNormalized() {
 		String s = fixEmptyAndTrim(excludedUsers);
 		if (s == null)
 			return Collections.emptySet();
 
 		Set<String> users = new HashSet<String>();
 		for (String user : s.split("[\\r\\n]+"))
 			users.add(user.trim());
 		return users;
 	}
 
 	@Exported
 	public String getExcludedRevprop() {
 		return excludedRevprop;
 	}
 
 	private String getExcludedRevpropNormalized() {
 		String s = fixEmptyAndTrim(getExcludedRevprop());
 		if (s != null)
 			return s;
 		return getDescriptor().getGlobalExcludedRevprop();
 	}
 
 	@Exported
 	public String getExcludedCommitMessages() {
 		return excludedCommitMessages;
 	}
 
 	public String[] getExcludedCommitMessagesNormalized() {
 		String s = fixEmptyAndTrim(excludedCommitMessages);
 		return s == null ? new String[0] : s.split("[\\r\\n]+");
 	}
 
 	private Pattern[] getExcludedCommitMessagesPatterns() {
 		String[] excluded = getExcludedCommitMessagesNormalized();
 		Pattern[] patterns = new Pattern[excluded.length];
 
 		int i = 0;
 		for (String excludedCommitMessage : excluded) {
 			patterns[i++] = Pattern.compile(excludedCommitMessage);
 		}
 
 		return patterns;
 	}
 
 	/**
 	 * Sets the <tt>SVN_REVISION</tt> environment variable during the build.
 	 */
 	@Override
 	public void buildEnvVars(AbstractBuild<?, ?> build, Map<String, String> env) {
 		super.buildEnvVars(build, env);
 
 		ModuleLocation[] svnLocations = getLocations(build);
 
 		try {
 			Map<String, Long> revisions = parseRevisionFile(build);
 			if (svnLocations.length == 1) {
 				Long rev = revisions.get(svnLocations[0].remote);
 				if (rev != null)
 					env.put("SVN_REVISION", rev.toString());
 			}
 			// it's not clear what to do if there are more than one modules.
 			// if we always return locations[0].remote, it'll be difficult
 			// to change this later (to something more sensible, such as
 			// choosing the "root module" or whatever), so let's not set
 			// anything for now.
 			// besides, one can always use 'svnversion' to obtain the revision
 			// more explicitly.
 		} catch (IOException e) {
 			// ignore this error
 		}
 	}
 
 	/**
 	 * Called after checkout/update has finished to compute the changelog.
 	 */
 	private boolean calcChangeLog(AbstractBuild<?, ?> build,
 			File changelogFile, BuildListener listener, List<External> externals)
 			throws IOException, InterruptedException {
 
 		if (build.getPreviousBuild() == null) {
 			// nothing to compare against
 			return createEmptyChangeLog(changelogFile, listener, "log");
 		}
 
 		// some users reported that the file gets created with size 0. I suspect
 		// maybe some XSLT engine doesn't close the stream properly.
 		// so let's do it by ourselves to be really sure that the stream gets
 		// closed.
 		OutputStream os = new BufferedOutputStream(new FileOutputStream(
 				changelogFile));
 		boolean created;
 		try {
 			created = new BlameSubversionChangeLogBuilder(build, listener, this)
 					.run(externals, new StreamResult(os));
 		} finally {
 			os.close();
 		}
 		if (!created)
 			createEmptyChangeLog(changelogFile, listener, "log");
 
 		// TODO: add upstream info
 
 		return true;
 	}
 
 	/* package */static Map<String, Long> parseRevisionFile(
 			AbstractBuild<?, ?> build) throws IOException {
 		return parseRevisionFile(build, false);
 	}
 
 	/**
 	 * Reads the revision file of the specified build (or the closest, if the
 	 * flag is so specified.)
 	 * 
 	 * @param findClosest
 	 *            If true, this method will go back the build history until it
 	 *            finds a revision file. A build may not have a revision file
 	 *            for any number of reasons (such as failure, interruption,
 	 *            etc.)
 	 * @return map from {@link SvnInfo#url Subversion URL} to its revision.
 	 */
 	/* package */static Map<String, Long> parseRevisionFile(
 			AbstractBuild<?, ?> build, boolean findClosest) throws IOException {
 		Map<String, Long> revisions = new HashMap<String, Long>(); // module ->
 		// revision
 
 		if (findClosest) {
 			for (AbstractBuild<?, ?> b = build; b != null; b = b
 					.getPreviousBuild()) {
 				if (getRevisionFile(b).exists()) {
 					build = b;
 					break;
 				}
 			}
 		}
 
 		{// read the revision file of the build
 			File file = getRevisionFile(build);
 			if (!file.exists())
 				// nothing to compare against
 				return revisions;
 
 			BufferedReader br = new BufferedReader(new FileReader(file));
 			try {
 				String line;
 				while ((line = br.readLine()) != null) {
 					int index = line.lastIndexOf('/');
 					if (index < 0) {
 						continue; // invalid line?
 					}
 					try {
 						revisions.put(line.substring(0, index), Long
 								.parseLong(line.substring(index + 1)));
 					} catch (NumberFormatException e) {
 						// perhaps a corrupted line. ignore
 					}
 				}
 			} finally {
 				br.close();
 			}
 		}
 
 		return revisions;
 	}
 
 	/**
 	 * Parses the file that stores the locations in the workspace where modules
 	 * loaded by svn:external is placed.
 	 * 
 	 * <p>
 	 * Note that the format of the file has changed in 1.180 from simple text
 	 * file to XML.
 	 * 
 	 * @return immutable list. Can be empty but never null.
 	 */
 	/* package */static List<External> parseExternalsFile(
 			AbstractProject project) throws IOException {
 		File file = getExternalsFile(project);
 		if (file.exists()) {
 			try {
 				return (List<External>) new XmlFile(External.XSTREAM, file)
 						.read();
 			} catch (IOException e) {
 				// in < 1.180 this file was a text file, so it may fail to parse
 				// as XML,
 				// in which case let's just fall back
 			}
 		}
 
 		return Collections.emptyList();
 	}
 
 	/**
 	 * Polling can happen on the master and does not require a workspace.
 	 */
 	@Override
 	public boolean requiresWorkspaceForPolling() {
 		return false;
 	}
 
 	public boolean checkout(AbstractBuild build, Launcher launcher,
 			FilePath workspace, final BuildListener listener, File changelogFile)
 			throws IOException, InterruptedException {
 		if (!alwaysCollectSVNInfo
 				&& !isBuildTriggeredByUpStreamJob(build.getCauses())) {
 			return true;
 		}
 		return recordTheUpstreamRecipients(build, workspace, listener,
 				changelogFile);
 	}
 
 	private boolean isBuildTriggeredByUpStreamJob(List<Cause> causes) {
 		return causes != null & causes.size() > 0
 				& (causes.get(causes.size() - 1) instanceof UpstreamCause);
 	}
 
 	protected PollingResult compareRemoteRevisionWith(
 			AbstractProject<?, ?> project, Launcher launcher,
 			FilePath workspace, TaskListener listener, SCMRevisionState baseline)
 			throws IOException, InterruptedException {
 
 		return PollingResult.NO_CHANGES;
 	}
 
 	private boolean recordTheUpstreamRecipients(AbstractBuild build,
 			FilePath workspace, final BuildListener listener, File changelogFile) {
 
 		// copy the ChangeLog from the latest build of upstream project
 
 		// get the upstream projects
 		List<AbstractProject> upstreamProjects = build.getProject()
 				.getUpstreamProjects();
 
 		// 1 get the lastest build of upstream project who triger this build
 		AbstractBuild upStreamLastTriggerBuild = getTrigerBuild(upstreamProjects);
 		
 		
 
 		if (upStreamLastTriggerBuild != null) {
 			// 2 get the ChanangeLog from the lastest build, and print the Changelog
 			ChangeLogSet<ChangeLogSet.Entry> upStreamBuildChangeSet = upStreamLastTriggerBuild
 					.getChangeSet();
 			listener.getLogger().println(
 					"begin get changelog from the project "
 							+ upStreamLastTriggerBuild.getProject().getName()
 							+ " and the build number is "
 							+ upStreamLastTriggerBuild.getNumber());
 
 			for (ChangeLogSet.Entry entry : upStreamBuildChangeSet) {
 				listener.getLogger().println(
 						"the committer is :" + entry.getAuthor());
 				listener.getLogger().println("message   is :" + entry.getMsg());
 				listener.getLogger().println(
 						"affect path   is :" + entry.getAffectedPaths());
 			}
 
 			// 3 save the ChanangeLog from the lastest build to this ChangeLog
 			copyChangeLogFromTriggerJob(build,upStreamLastTriggerBuild);
 			copyRevisionFromTriggerJob(build,upStreamLastTriggerBuild);
 			listener.getLogger().println(
 					"end copy the changelog from the project "
 							+ upStreamLastTriggerBuild.getProject().getName());
 
 		}else if(build.getProject() instanceof MatrixConfiguration){
 			   
 			MatrixConfiguration configuration = (MatrixConfiguration)build.getProject();
 			//no need svn log, because it is printed before
 			copyChangeLogFromTriggerJob(build,configuration.getParent().getLastBuild());
 			copyRevisionFromTriggerJob(build,configuration.getParent().getLastBuild());
 		}
 
 		return true;
 
 	}
 
 	private AbstractBuild getTrigerBuild(List<AbstractProject> upstreamProjects) {
 		AbstractBuild upStreamLastTriggerBuild = null;
 		for (AbstractProject project : upstreamProjects) {
 
 			AbstractBuild upStreamLastBuild = (AbstractBuild) project
 					.getLastBuild();
 			upStreamLastTriggerBuild = upStreamLastTriggerBuild == null ? upStreamLastBuild
 					: upStreamLastTriggerBuild;
 
 			// find the latest build of all upstreamProjects
 			if (upStreamLastBuild.getTimeInMillis() > upStreamLastTriggerBuild
 					.getTimeInMillis()) {
 				upStreamLastTriggerBuild = upStreamLastBuild;
 			}
 		}
 		return upStreamLastTriggerBuild;
 	}
 
 	private void copyChangeLogFromTriggerJob(AbstractBuild build,
 			AbstractBuild triggerBuild) {
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(
 					triggerBuild.getRootDir() + "/" + "changelog.xml"));
 
 			List<String> buffer = new ArrayList<String>();
 
 			String s;
 			while ((s = reader.readLine()) != null) {
 				buffer.add(s);
 			}
 			reader.close();
 
 			writeChgLog(buffer, build.getRootDir() + "/" + "changelog.xml");
 
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 
 			return;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return;
 		}
 	}
 	private void copyRevisionFromTriggerJob(AbstractBuild build,
 			AbstractBuild triggerBuild) {
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(
 					triggerBuild.getRootDir() + "/" + "revision.txt"));
 
 			List<String> buffer = new ArrayList<String>();
 
 			String s;
 			while ((s = reader.readLine()) != null) {
				buffer.add(s);
 			}
 			reader.close();
 
 			writeChgLog(buffer, build.getRootDir() + "/" + "revision.txt");
 
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 
 			return;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return;
 		}
 		
 	}
 	
 
 	private void writeChgLog(List<String> buffer, String filename)
 			throws IOException {
 		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
 		for (String str : buffer) {
 			writer.write(str);
 		}
 		writer.close();
 	}
 
 	private boolean isSVNRepositoryURLNull(AbstractBuild build) {
 		ModuleLocation[] locations = this.getLocations(build);
 		for (ModuleLocation location : locations) {
 			if (location.local == null || location.remote.trim().equals("")) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Either run "svn co" or "svn up" equivalent.
 	 */
 	private static class CheckOutTask implements FileCallable<List<External>> {
 		private final ISVNAuthenticationProvider authProvider;
 		private final Date timestamp;
 		// true to "svn update", false to "svn checkout".
 		private boolean update;
 		private boolean revert;
 		private final TaskListener listener;
 		private final ModuleLocation[] locations;
 		private final RevisionParameterAction revisions;
 
 		public CheckOutTask(AbstractBuild<?, ?> build,
 				BlameSubversionSCM parent, Date timestamp, boolean update,
 				boolean revert, TaskListener listener) {
 			this.authProvider = parent.getDescriptor()
 					.createAuthenticationProvider(build.getParent());
 			this.timestamp = timestamp;
 			this.update = update;
 			this.revert = revert;
 			this.listener = listener;
 			this.locations = parent.getLocations(build);
 			revisions = build.getAction(RevisionParameterAction.class);
 		}
 
 		public List<External> invoke(File ws, VirtualChannel channel)
 				throws IOException {
 			final SVNClientManager manager = createSvnClientManager(authProvider);
 			try {
 				final SVNUpdateClient svnuc = manager.getUpdateClient();
 				final SVNWCClient svnwc = manager.getWCClient();
 				final List<External> externals = new ArrayList<External>(); // store
 				// discovered
 				// externals
 				// to
 				// here
 				if (update) {
 					for (final ModuleLocation l : locations) {
 						try {
 							File local = new File(ws, l.getLocalDir());
 							svnuc
 									.setEventHandler(new BlameSubversionUpdateEventHandler(
 											listener.getLogger(), externals,
 											local, l.getLocalDir()));
 
 							SVNRevision r = getRevision(l);
 
 							if (revert) {
 								listener.getLogger().println(
 										"Reverting " + l.remote);
 								svnwc.doRevert(new File[] { local
 										.getCanonicalFile() },
 										SVNDepth.INFINITY, null);
 							}
 							listener.getLogger()
 									.println("Updating " + l.remote);
 							svnuc.doUpdate(local.getCanonicalFile(), r,
 									SVNDepth.INFINITY, true, false);
 
 						} catch (final SVNException e) {
 							if (e.getErrorMessage().getErrorCode() == SVNErrorCode.WC_LOCKED) {
 								// work space locked. try fresh check out
 								listener
 										.getLogger()
 										.println(
 												"Workspace appear to be locked, so getting a fresh workspace");
 								update = false;
 								return invoke(ws, channel);
 							}
 							if (e.getErrorMessage().getErrorCode() == SVNErrorCode.WC_OBSTRUCTED_UPDATE) {
 								// HUDSON-1882. If existence of local files
 								// cause an update to fail,
 								// revert to fresh check out
 								listener.getLogger().println(e.getMessage()); // show
 								// why
 								// this
 								// happened.
 								// Sometimes
 								// this
 								// is
 								// caused
 								// by
 								// having
 								// a
 								// build
 								// artifact
 								// in
 								// the
 								// repository.
 								listener
 										.getLogger()
 										.println(
 												"Updated failed due to local files. Getting a fresh workspace");
 								update = false;
 								return invoke(ws, channel);
 							}
 
 							e.printStackTrace(listener
 									.error("Failed to update " + l.remote));
 							// trouble-shooting probe for #591
 							if (e.getErrorMessage().getErrorCode() == SVNErrorCode.WC_NOT_LOCKED) {
 								listener
 										.getLogger()
 										.println(
 												"Polled jobs are "
 														+ Hudson
 																.getInstance()
 																.getDescriptorByType(
 																		SCMTrigger.DescriptorImpl.class)
 																.getItemsBeingPolled());
 							}
 							return null;
 						}
 					}
 				} else {
 					Util.deleteContentsRecursive(ws);
 
 					// buffer the output by a separate thread so that the update
 					// operation
 					// won't be blocked by the remoting of the data
 					PipedOutputStream pos = new PipedOutputStream();
 					StreamCopyThread sct = new StreamCopyThread(
 							"svn log copier", new PipedInputStream(pos),
 							listener.getLogger());
 					sct.start();
 
 					ModuleLocation location = null;
 					try {
 						for (final ModuleLocation l : locations) {
 							location = l;
 							listener.getLogger().println(
 									"Checking out " + l.remote);
 
 							File local = new File(ws, l.getLocalDir());
 							svnuc
 									.setEventHandler(new BlameSubversionUpdateEventHandler(
 											new PrintStream(pos), externals,
 											local, l.getLocalDir()));
 							svnuc.doCheckout(l.getSVNURL(), local
 									.getCanonicalFile(), SVNRevision.HEAD,
 									getRevision(l), SVNDepth.INFINITY, true);
 						}
 					} catch (SVNException e) {
 						e.printStackTrace(listener.error("Failed to check out "
 								+ location.remote));
 						return null;
 					} finally {
 						try {
 							pos.close();
 						} finally {
 							try {
 								sct.join(); // wait for all data to be piped.
 							} catch (InterruptedException e) {
 								throw new IOException2("interrupted", e);
 							}
 						}
 					}
 				}
 
 				try {
 					for (final ModuleLocation l : locations) {
 						SVNDirEntry dir = manager.createRepository(
 								l.getSVNURL(), true).info("/", -1);
 						if (dir != null) {// I don't think this can ever be
 							// null, but be defensive
 							if (dir.getDate() != null
 									&& dir.getDate().after(new Date())) // see
 								// http://www.nabble.com/NullPointerException-in-SVN-Checkout-Update-td21609781.html
 								// that
 								// reported
 								// this
 								// being
 								// null.
 								listener
 										.getLogger()
 										.println(
 												Messages
 														.SubversionSCM_ClockOutOfSync());
 						}
 					}
 				} catch (SVNAuthenticationException e) {
 					// if we don't have access to '/', ignore. error
 					LOGGER.log(Level.FINE,
 							"Failed to estimate the remote time stamp", e);
 				} catch (SVNException e) {
 					LOGGER.log(Level.INFO,
 							"Failed to estimate the remote time stamp", e);
 				}
 
 				return externals;
 			} finally {
 				manager.dispose();
 			}
 		}
 
 		private SVNRevision getRevision(ModuleLocation l) {
 			// for the SVN revision, we will use the first off:
 			// - a @NNN prefix of the SVN url
 			// - a value found in a RevisionParameterAction
 			// - the revision corresponding to the build timestamp
 
 			SVNRevision r = null;
 			if (revisions != null) {
 				r = revisions.getRevision(l.getURL());
 			}
 			if (r == null) {
 				r = SVNRevision.create(timestamp);
 			}
 			r = l.getRevision(r);
 			return r;
 		}
 
 		private static final long serialVersionUID = 1L;
 	}
 
 	/**
 	 * Creates {@link SVNClientManager}.
 	 * 
 	 * <p>
 	 * This method must be executed on the slave where svn operations are
 	 * performed.
 	 * 
 	 * @param authProvider
 	 *            The value obtained from
 	 *            {@link DescriptorImpl#createAuthenticationProvider(AbstractProject)}
 	 *            . If the operation runs on slaves, (and properly remoted, if
 	 *            the svn operations run on slaves.)
 	 */
 	public static SVNClientManager createSvnClientManager(
 			ISVNAuthenticationProvider authProvider) {
 		ISVNAuthenticationManager sam = SVNWCUtil
 				.createDefaultAuthenticationManager();
 		sam.setAuthenticationProvider(authProvider);
 		return SVNClientManager.newInstance(SVNWCUtil
 				.createDefaultOptions(true), sam);
 	}
 
 	/**
 	 * Creates {@link SVNClientManager} for code running on the master.
 	 * <p>
 	 * CAUTION: this code only works when invoked on master. On slaves, use
 	 * {@link #createSvnClientManager(ISVNAuthenticationProvider)} and get
 	 * {@link ISVNAuthenticationProvider} from the master via remoting.
 	 */
 	public static SVNClientManager createSvnClientManager(
 			AbstractProject context) {
 		return createSvnClientManager(Hudson.getInstance().getDescriptorByType(
 				DescriptorImpl.class).createAuthenticationProvider(context));
 	}
 
 	public static final class SvnInfo implements Serializable,
 			Comparable<SvnInfo> {
 		/**
 		 * Decoded repository URL.
 		 */
 		public final String url;
 		public final long revision;
 
 		public SvnInfo(String url, long revision) {
 			this.url = url;
 			this.revision = revision;
 		}
 
 		public SvnInfo(SVNInfo info) {
 			this(info.getURL().toDecodedString(), info.getCommittedRevision()
 					.getNumber());
 		}
 
 		public SVNURL getSVNURL() throws SVNException {
 			return SVNURL.parseURIDecoded(url);
 		}
 
 		public int compareTo(SvnInfo that) {
 			int r = this.url.compareTo(that.url);
 			if (r != 0)
 				return r;
 
 			if (this.revision < that.revision)
 				return -1;
 			if (this.revision > that.revision)
 				return +1;
 			return 0;
 		}
 
 		@Override
 		public boolean equals(Object o) {
 			if (this == o)
 				return true;
 			if (o == null || getClass() != o.getClass())
 				return false;
 
 			SvnInfo svnInfo = (SvnInfo) o;
 			return revision == svnInfo.revision && url.equals(svnInfo.url);
 
 		}
 
 		@Override
 		public int hashCode() {
 			int result;
 			result = url.hashCode();
 			result = 31 * result + (int) (revision ^ (revision >>> 32));
 			return result;
 		}
 
 		@Override
 		public String toString() {
 			return String.format("%s (rev.%s)", url, revision);
 		}
 
 		private static final long serialVersionUID = 1L;
 	}
 
 	/**
 	 * Information about svn:external
 	 */
 	static final class External implements Serializable {
 		/**
 		 * Relative path within the workspace where this <tt>svn:exteranls</tt>
 		 * exist.
 		 */
 		final String path;
 
 		/**
 		 * External SVN URL to be fetched.
 		 */
 		final String url;
 
 		/**
 		 * If the svn:external link is with the -r option, its number. Otherwise
 		 * -1 to indicate that the head revision of the external repository
 		 * should be fetched.
 		 */
 		final long revision;
 
 		/**
 		 * @param modulePath
 		 *            The root of the current module that svn was checking out
 		 *            when it hits 'ext'. Since we call svnkit multiple times in
 		 *            general case to check out from multiple locations, we use
 		 *            this to make the path relative to the entire workspace,
 		 *            not just the particular module.
 		 */
 		External(String modulePath, SVNExternal ext) {
 			this.path = modulePath + '/' + ext.getPath();
 			this.url = ext.getResolvedURL().toDecodedString();
 			this.revision = ext.getRevision().getNumber();
 		}
 
 		/**
 		 * Returns true if this reference is to a fixed revision.
 		 */
 		boolean isRevisionFixed() {
 			return revision != -1;
 		}
 
 		private static final long serialVersionUID = 1L;
 
 		private static final XStream XSTREAM = new XStream2();
 		static {
 			XSTREAM.alias("external", External.class);
 		}
 	}
 
 	/**
 	 * Gets the SVN metadata for the given local workspace.
 	 * 
 	 * @param workspace
 	 *            The target to run "svn info".
 	 */
 	private static SVNInfo parseSvnInfo(File workspace,
 			ISVNAuthenticationProvider authProvider) throws SVNException {
 		final SVNClientManager manager = createSvnClientManager(authProvider);
 		try {
 			final SVNWCClient svnWc = manager.getWCClient();
 			return svnWc.doInfo(workspace, SVNRevision.WORKING);
 		} finally {
 			manager.dispose();
 		}
 	}
 
 	/**
 	 * Gets the SVN metadata for the remote repository.
 	 * 
 	 * @param remoteUrl
 	 *            The target to run "svn info".
 	 */
 	private static SVNInfo parseSvnInfo(SVNURL remoteUrl,
 			ISVNAuthenticationProvider authProvider) throws SVNException {
 		final SVNClientManager manager = createSvnClientManager(authProvider);
 		try {
 			final SVNWCClient svnWc = manager.getWCClient();
 			return svnWc.doInfo(remoteUrl, SVNRevision.HEAD, SVNRevision.HEAD);
 		} finally {
 			manager.dispose();
 		}
 	}
 
 	/**
 	 * Checks .svn files in the workspace and finds out revisions of the modules
 	 * that the workspace has.
 	 * 
 	 * @return null if the parsing somehow fails. Otherwise a map from the
 	 *         repository URL to revisions.
 	 */
 	private static class BuildRevisionMapTask implements
 			FileCallable<Map<String, SvnInfo>> {
 		private final ISVNAuthenticationProvider authProvider;
 		private final TaskListener listener;
 		private final List<External> externals;
 		private final ModuleLocation[] locations;
 
 		public BuildRevisionMapTask(AbstractBuild<?, ?> build,
 				BlameSubversionSCM parent, TaskListener listener,
 				List<External> externals) {
 			this.authProvider = parent.getDescriptor()
 					.createAuthenticationProvider(build.getParent());
 			this.listener = listener;
 			this.externals = externals;
 			this.locations = parent.getLocations(build);
 		}
 
 		public Map<String, SvnInfo> invoke(File ws, VirtualChannel channel)
 				throws IOException {
 			Map<String/* module name */, SvnInfo> revisions = new HashMap<String, SvnInfo>();
 
 			final SVNClientManager manager = createSvnClientManager(authProvider);
 			try {
 				final SVNWCClient svnWc = manager.getWCClient();
 				// invoke the "svn info"
 				for (ModuleLocation module : locations) {
 					try {
 						SvnInfo info = new SvnInfo(svnWc.doInfo(new File(ws,
 								module.getLocalDir()), SVNRevision.WORKING));
 						revisions.put(info.url, info);
 					} catch (SVNException e) {
 						e.printStackTrace(listener
 								.error("Failed to parse svn info for "
 										+ module.remote));
 					}
 				}
 				for (External ext : externals) {
 					try {
 						SvnInfo info = new SvnInfo(svnWc.doInfo(new File(ws,
 								ext.path), SVNRevision.WORKING));
 						revisions.put(info.url, info);
 					} catch (SVNException e) {
 						e.printStackTrace(listener
 								.error("Failed to parse svn info for external "
 										+ ext.url + " at " + ext.path));
 					}
 
 				}
 
 				return revisions;
 			} finally {
 				manager.dispose();
 			}
 		}
 
 		private static final long serialVersionUID = 1L;
 	}
 
 	/**
 	 * Gets the file that stores the revision.
 	 */
 	public static File getRevisionFile(AbstractBuild build) {
 		return new File(build.getRootDir(), "revision.txt");
 	}
 
 	/**
 	 * Gets the file that stores the externals.
 	 */
 	private static File getExternalsFile(AbstractProject project) {
 		return new File(project.getRootDir(), "svnexternals.txt");
 	}
 
 	/**
 	 * Returns true if we can use "svn update" instead of "svn checkout"
 	 */
 	private static class IsUpdatableTask implements FileCallable<Boolean> {
 		private final TaskListener listener;
 		private final ISVNAuthenticationProvider authProvider;
 		private final ModuleLocation[] locations;
 
 		IsUpdatableTask(AbstractBuild<?, ?> build, BlameSubversionSCM parent,
 				TaskListener listener) {
 			this.authProvider = parent.getDescriptor()
 					.createAuthenticationProvider(build.getParent());
 			this.listener = listener;
 			this.locations = parent.getLocations(build);
 		}
 
 		public Boolean invoke(File ws, VirtualChannel channel)
 				throws IOException {
 			for (ModuleLocation l : locations) {
 				String moduleName = l.getLocalDir();
 				File module = new File(ws, moduleName).getCanonicalFile(); // canonicalize
 				// to
 				// remove
 				// ".."
 				// and
 				// ".".
 				// See
 				// #474
 
 				if (!module.exists()) {
 					listener.getLogger().println(
 							"Checking out a fresh workspace because " + module
 									+ " doesn't exist");
 					return false;
 				}
 
 				try {
 					SVNInfo svnkitInfo = parseSvnInfo(module, authProvider);
 					SvnInfo svnInfo = new SvnInfo(svnkitInfo);
 
 					String url = l.getURL();
 					if (!svnInfo.url.equals(url)) {
 						listener.getLogger().println(
 								"Checking out a fresh workspace because the workspace is not "
 										+ url);
 						return false;
 					}
 				} catch (SVNException e) {
 					if (e.getErrorMessage().getErrorCode() == SVNErrorCode.WC_NOT_DIRECTORY) {
 						listener.getLogger().println(
 								"Checking out a fresh workspace because there's no workspace at "
 										+ module);
 					} else {
 						listener
 								.getLogger()
 								.println(
 										"Checking out a fresh workspace because Hudson failed to detect the current workspace "
 												+ module);
 						e.printStackTrace(listener.error(e.getMessage()));
 					}
 					return false;
 				}
 			}
 			return true;
 		}
 
 		private static final long serialVersionUID = 1L;
 	}
 
 	@Override
 	public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> build,
 			Launcher launcher, TaskListener listener) throws IOException,
 			InterruptedException {
 		// exclude locations that are svn:external-ed with a fixed revision.
 		Map<String, Long> wsRev = parseRevisionFile(build, true);
 		for (External e : parseExternalsFile(build.getProject()))
 			if (e.isRevisionFixed())
 				wsRev.remove(e.url);
 
 		return new SVNRevisionState(wsRev);
 	}
 
 	public ChangeLogParser createChangeLogParser() {
 		return new BlameSubversionChangeLogParser();
 	}
 
 	@Override
 	public DescriptorImpl getDescriptor() {
 		return (DescriptorImpl) super.getDescriptor();
 	}
 
 	@Override
 	public FilePath getModuleRoot(FilePath workspace) {
 		if (getLocations().length > 0)
 			return workspace.child(getLocations()[0].getLocalDir());
 		return workspace;
 	}
 
 	@Override
 	public FilePath[] getModuleRoots(FilePath workspace) {
 		final ModuleLocation[] moduleLocations = getLocations();
 		if (moduleLocations.length > 0) {
 			FilePath[] moduleRoots = new FilePath[moduleLocations.length];
 			for (int i = 0; i < moduleLocations.length; i++) {
 				moduleRoots[i] = workspace.child(moduleLocations[i]
 						.getLocalDir());
 			}
 			return moduleRoots;
 		}
 		return new FilePath[] { getModuleRoot(workspace) };
 	}
 
 	private static String getLastPathComponent(String s) {
 		String[] tokens = s.split("/");
 		return tokens[tokens.length - 1]; // return the last token
 	}
 
 	@Extension
 	public static class DescriptorImpl extends
 			SCMDescriptor<BlameSubversionSCM> implements
 			hudson.model.ModelObject {
 		/**
 		 * SVN authentication realm to its associated credentials. This is the
 		 * global credential repository.
 		 */
 		private final Map<String, Credential> credentials = new Hashtable<String, Credential>();
 
 		/**
 		 * Stores name of Subversion revision property to globally exclude
 		 */
 		private String globalExcludedRevprop = null;
 
 		/**
 		 * Stores {@link SVNAuthentication} for a single realm.
 		 * 
 		 * <p>
 		 * {@link Credential} holds data in a persistence-friendly way, and it's
 		 * capable of creating {@link SVNAuthentication} object, to be passed to
 		 * SVNKit.
 		 */
 		public static abstract class Credential implements Serializable {
 			/**
 			 * @param kind
 			 *            One of the constants defined in
 			 *            {@link ISVNAuthenticationManager}, indicating what
 			 *            subype of {@link SVNAuthentication} is expected.
 			 */
 			public abstract SVNAuthentication createSVNAuthentication(
 					String kind) throws SVNException;
 		}
 
 		/**
 		 * Username/password based authentication.
 		 */
 		public static final class PasswordCredential extends Credential {
 			private final String userName;
 			private final String password; // scrambled by base64
 
 			public PasswordCredential(String userName, String password) {
 				this.userName = userName;
 				this.password = Scrambler.scramble(password);
 			}
 
 			@Override
 			public SVNAuthentication createSVNAuthentication(String kind) {
 				if (kind.equals(ISVNAuthenticationManager.SSH))
 					return new SVNSSHAuthentication(userName, Scrambler
 							.descramble(password), -1, false);
 				else
 					return new SVNPasswordAuthentication(userName, Scrambler
 							.descramble(password), false);
 			}
 		}
 
 		/**
 		 * Publickey authentication for Subversion over SSH.
 		 */
 		public static final class SshPublicKeyCredential extends Credential {
 			private final String userName;
 			private final String passphrase; // scrambled by base64
 			private final String id;
 
 			/**
 			 * @param keyFile
 			 *            stores SSH private key. The file will be copied.
 			 */
 			public SshPublicKeyCredential(String userName, String passphrase,
 					File keyFile) throws SVNException {
 				this.userName = userName;
 				this.passphrase = Scrambler.scramble(passphrase);
 
 				Random r = new Random();
 				StringBuilder buf = new StringBuilder();
 				for (int i = 0; i < 16; i++)
 					buf.append(Integer.toHexString(r.nextInt(16)));
 				this.id = buf.toString();
 
 				try {
 					FileUtils.copyFile(keyFile, getKeyFile());
 				} catch (IOException e) {
 					throw new SVNException(SVNErrorMessage.create(
 							SVNErrorCode.AUTHN_CREDS_UNAVAILABLE,
 							"Unable to save private key"), e);
 				}
 			}
 
 			/**
 			 * Gets the location where the private key will be permanently
 			 * stored.
 			 */
 			private File getKeyFile() {
 				File dir = new File(Hudson.getInstance().getRootDir(),
 						"subversion-credentials");
 				if (dir.mkdirs()) {
 					// make sure the directory exists. if we created it, try to
 					// set the permission to 600
 					// since this is sensitive information
 					try {
 						Chmod chmod = new Chmod();
 						chmod.setProject(new Project());
 						chmod.setFile(dir);
 						chmod.setPerm("600");
 						chmod.execute();
 					} catch (Throwable e) {
 						// if we failed to set the permission, that's fine.
 						LOGGER.log(Level.WARNING,
 								"Failed to set directory permission of " + dir,
 								e);
 					}
 				}
 				return new File(dir, id);
 			}
 
 			@Override
 			public SVNSSHAuthentication createSVNAuthentication(String kind)
 					throws SVNException {
 				if (kind.equals(ISVNAuthenticationManager.SSH)) {
 					try {
 						Channel channel = Channel.current();
 						String privateKey;
 						if (channel != null) {
 							// remote
 							privateKey = channel
 									.call(new Callable<String, IOException>() {
 										public String call() throws IOException {
 											return FileUtils.readFileToString(
 													getKeyFile(), "iso-8859-1");
 										}
 									});
 						} else {
 							privateKey = FileUtils.readFileToString(
 									getKeyFile(), "iso-8859-1");
 						}
 						return new SVNSSHAuthentication(userName, privateKey
 								.toCharArray(), Scrambler
 								.descramble(passphrase), -1, false);
 					} catch (IOException e) {
 						throw new SVNException(SVNErrorMessage.create(
 								SVNErrorCode.AUTHN_CREDS_UNAVAILABLE,
 								"Unable to load private key"), e);
 					} catch (InterruptedException e) {
 						throw new SVNException(SVNErrorMessage.create(
 								SVNErrorCode.AUTHN_CREDS_UNAVAILABLE,
 								"Unable to load private key"), e);
 					}
 				} else
 					return null; // unknown
 			}
 		}
 
 		/**
 		 * SSL client certificate based authentication.
 		 */
 		public static final class SslClientCertificateCredential extends
 				Credential {
 			private final Secret certificate;
 			private final String password; // scrambled by base64
 
 			public SslClientCertificateCredential(File certificate,
 					String password) throws IOException {
 				this.password = Scrambler.scramble(password);
 				this.certificate = Secret.fromString(new String(Base64
 						.encode(FileUtils.readFileToByteArray(certificate))));
 			}
 
 			@Override
 			public SVNAuthentication createSVNAuthentication(String kind) {
 				if (kind.equals(ISVNAuthenticationManager.SSL))
 					try {
 						return new SVNSSLAuthentication(Base64
 								.decode(certificate.toString().toCharArray()),
 								Scrambler.descramble(password), false);
 					} catch (IOException e) {
 						throw new Error(e); // can't happen
 					}
 				else
 					return null; // unexpected authentication type
 			}
 		}
 
 		/**
 		 * Remoting interface that allows remote
 		 * {@link ISVNAuthenticationProvider} to read from local
 		 * {@link DescriptorImpl#credentials}.
 		 */
 		interface RemotableSVNAuthenticationProvider extends Serializable {
 			Credential getCredential(SVNURL url, String realm);
 
 			/**
 			 * Indicates that the specified credential worked.
 			 */
 			void acknowledgeAuthentication(String realm, Credential credential);
 		}
 
 		/**
 		 * There's no point in exporting multiple
 		 * {@link RemotableSVNAuthenticationProviderImpl} instances, so let's
 		 * just use one instance.
 		 */
 		private transient final RemotableSVNAuthenticationProviderImpl remotableProvider = new RemotableSVNAuthenticationProviderImpl();
 
 		private final class RemotableSVNAuthenticationProviderImpl implements
 				RemotableSVNAuthenticationProvider {
 			public Credential getCredential(SVNURL url, String realm) {
 				for (BlameSubversionCredentialProvider p : BlameSubversionCredentialProvider
 						.all()) {
 					Credential c = p.getCredential(url, realm);
 					if (c != null) {
 						LOGGER.fine(String.format(
 								"getCredential(%s)=>%s by %s", realm, c, p));
 						return c;
 					}
 				}
 				LOGGER.fine(String.format("getCredential(%s)=>%s", realm,
 						credentials.get(realm)));
 				return credentials.get(realm);
 			}
 
 			public void acknowledgeAuthentication(String realm,
 					Credential credential) {
 				// this notification is only used on the project-local store.
 			}
 
 			/**
 			 * When sent to the remote node, send a proxy.
 			 */
 			private Object writeReplace() {
 				return Channel.current().export(
 						RemotableSVNAuthenticationProvider.class, this);
 			}
 		}
 
 		/**
 		 * See
 		 * {@link DescriptorImpl#createAuthenticationProvider(AbstractProject)}.
 		 */
 		private static final class SVNAuthenticationProviderImpl implements
 				ISVNAuthenticationProvider, ISVNAuthenticationOutcomeListener,
 				Serializable {
 			/**
 			 * Project-scoped authentication source. For historical reasons, can
 			 * be null.
 			 */
 			private final RemotableSVNAuthenticationProvider local;
 
 			/**
 			 * System-wide authentication source. Used as a fallback.
 			 */
 			private final RemotableSVNAuthenticationProvider global;
 
 			/**
 			 * The {@link Credential} used to create the last
 			 * {@link SVNAuthentication} that we've tried.
 			 */
 			private Credential lastCredential;
 
 			public SVNAuthenticationProviderImpl(
 					RemotableSVNAuthenticationProvider local,
 					RemotableSVNAuthenticationProvider global) {
 				this.global = global;
 				this.local = local;
 			}
 
 			private SVNAuthentication fromProvider(SVNURL url, String realm,
 					String kind, RemotableSVNAuthenticationProvider src,
 					String debugName) throws SVNException {
 				if (src == null)
 					return null;
 
 				Credential cred = src.getCredential(url, realm);
 				LOGGER.fine(String.format(
 						"%s.requestClientAuthentication(%s,%s,%s)=>%s",
 						debugName, kind, url, realm, cred));
 				this.lastCredential = cred;
 				if (cred != null)
 					return cred.createSVNAuthentication(kind);
 				return null;
 			}
 
 			public SVNAuthentication requestClientAuthentication(String kind,
 					SVNURL url, String realm, SVNErrorMessage errorMessage,
 					SVNAuthentication previousAuth, boolean authMayBeStored) {
 
 				try {
 					SVNAuthentication auth = fromProvider(url, realm, kind,
 							local, "local");
 
 					// first try the local credential, then the global
 					// credential.
 					if (auth == null
 							|| compareSVNAuthentications(auth, previousAuth))
 						auth = fromProvider(url, realm, kind, global, "global");
 
 					if (previousAuth != null
 							&& compareSVNAuthentications(auth, previousAuth)) {
 						// See HUDSON-2909
 						// this comparison is necessary, unlike the original fix
 						// of HUDSON-2909, since SVNKit may use
 						// other ISVNAuthenticationProviders and their failed
 						// auth might be passed to us.
 						// see HUDSON-3936
 						LOGGER
 								.fine("Previous authentication attempt failed, so aborting: "
 										+ previousAuth);
 						return null;
 					}
 
 					if (auth == null
 							&& ISVNAuthenticationManager.USERNAME.equals(kind)) {
 						// this happens with file:// URL and svn+ssh (in this
 						// case this method gets invoked twice.)
 						// The base class does this, too.
 						// user auth shouldn't be null.
 						return new SVNUserNameAuthentication("", false);
 					}
 
 					return auth;
 				} catch (SVNException e) {
 					LOGGER.log(Level.SEVERE, "Failed to authorize", e);
 					throw new RuntimeException("Failed to authorize", e);
 				}
 			}
 
 			public void acknowledgeAuthentication(boolean accepted,
 					String kind, String realm, SVNErrorMessage errorMessage,
 					SVNAuthentication authentication) throws SVNException {
 				if (accepted && local != null)
 					local.acknowledgeAuthentication(realm, lastCredential);
 			}
 
 			public int acceptServerAuthentication(SVNURL url, String realm,
 					Object certificate, boolean resultMayBeStored) {
 				return ACCEPTED_TEMPORARY;
 			}
 
 			private static final long serialVersionUID = 1L;
 		}
 
 		@Override
 		public SCM newInstance(StaplerRequest staplerRequest,
 				JSONObject jsonObject) throws FormException {
 			return super.newInstance(staplerRequest, jsonObject);
 		}
 
 		public DescriptorImpl() {
 			super(BlameSubversionRepositoryBrowser.class);
 			load();
 		}
 
 		protected DescriptorImpl(Class clazz,
 				Class<? extends RepositoryBrowser> repositoryBrowser) {
 			super(clazz, repositoryBrowser);
 		}
 
 		public String getDisplayName() {
 			return "Blame Subversion";
 		}
 
 		public String getGlobalExcludedRevprop() {
 			return globalExcludedRevprop;
 		}
 
 		@Override
 		public boolean configure(StaplerRequest req, JSONObject formData)
 				throws FormException {
 			globalExcludedRevprop = fixEmptyAndTrim(req
 					.getParameter("svn.global_excluded_revprop"));
 
 			// Save configuration
 			save();
 
 			return super.configure(req, formData);
 		}
 
 		@Override
 		public boolean isBrowserReusable(BlameSubversionSCM x,
 				BlameSubversionSCM y) {
 			ModuleLocation[] xl = x.getLocations(), yl = y.getLocations();
 			if (xl.length != yl.length)
 				return false;
 			for (int i = 0; i < xl.length; i++)
 				if (!xl[i].getURL().equals(yl[i].getURL()))
 					return false;
 			return true;
 		}
 
 		/**
 		 * Creates {@link ISVNAuthenticationProvider} backed by
 		 * {@link #credentials}. This method must be invoked on the master, but
 		 * the returned object is remotable.
 		 * 
 		 * <p>
 		 * Therefore, to access {@link ISVNAuthenticationProvider}, you need to
 		 * call this method on the master, then pass the object to the slave
 		 * side, then call
 		 * {@link BlameSubversionSCM#createSvnClientManager(ISVNAuthenticationProvider)}
 		 * on the slave.
 		 * 
 		 * @see BlameSubversionSCM#createSvnClientManager(ISVNAuthenticationProvider)
 		 */
 		public ISVNAuthenticationProvider createAuthenticationProvider(
 				AbstractProject<?, ?> inContextOf) {
 			return new SVNAuthenticationProviderImpl(inContextOf == null ? null
 					: new PerJobCredentialStore(inContextOf), remotableProvider);
 		}
 
 		/**
 		 * @deprecated as of 1.18 Now that Hudson allows different credentials
 		 *             to be given in different jobs, The caller should use
 		 *             {@link #createAuthenticationProvider(AbstractProject)} to
 		 *             indicate the project in which the subversion operation is
 		 *             performed.
 		 */
 		public ISVNAuthenticationProvider createAuthenticationProvider() {
 			return new SVNAuthenticationProviderImpl(null, remotableProvider);
 		}
 
 		/**
 		 * Submits the authentication info.
 		 */
 		// TODO: stapler should do multipart/form-data handling
 		// public void doPostCredential(StaplerRequest req, StaplerResponse rsp)
 		// throws IOException, ServletException {
 		// Hudson.getInstance().checkPermission(Hudson.ADMINISTER);
 		//
 		// MultipartFormDataParser parser = new MultipartFormDataParser(req);
 		//
 		// // we'll record what credential we are trying here.
 		// StringWriter log = new StringWriter();
 		// PrintWriter logWriter = new PrintWriter(log);
 		//
 		// UserProvidedCredential upc =
 		// UserProvidedCredential.fromForm(req,parser);
 		//
 		// try {
 		// postCredential(parser.get("url"), upc, logWriter);
 		// rsp.sendRedirect("credentialOK");
 		// } catch (SVNException e) {
 		// logWriter.println("FAILED: "+e.getErrorMessage());
 		// req.setAttribute("message",log.toString());
 		// req.setAttribute("pre",true);
 		// req.setAttribute("exception",e);
 		// rsp.forward(Hudson.getInstance(),"error",req);
 		// } finally {
 		// upc.close();
 		// }
 		// }
 
 		/**
 		 * @deprecated as of 1.18 Use
 		 *             {@link #postCredential(AbstractProject, String, String, String, File, PrintWriter)}
 		 */
 		public void postCredential(String url, String username,
 				String password, File keyFile, PrintWriter logWriter)
 				throws SVNException, IOException {
 			postCredential(null, url, username, password, keyFile, logWriter);
 		}
 
 		public void postCredential(AbstractProject inContextOf, String url,
 				String username, String password, File keyFile,
 				PrintWriter logWriter) throws SVNException, IOException {
 			postCredential(url, new UserProvidedCredential(username, password,
 					keyFile, inContextOf), logWriter);
 		}
 
 		/**
 		 * Submits the authentication info.
 		 * 
 		 * This code is fairly ugly because of the way SVNKit handles
 		 * credentials.
 		 */
 		public void postCredential(String url,
 				final UserProvidedCredential upc, PrintWriter logWriter)
 				throws SVNException, IOException {
 			SVNRepository repository = null;
 
 			try {
 				// the way it works with SVNKit is that
 				// 1) svnkit calls AuthenticationManager asking for a
 				// credential.
 				// this is when we can see the 'realm', which identifies the
 				// user domain.
 				// 2) DefaultSVNAuthenticationManager returns the username and
 				// password we set below
 				// 3) if the authentication is successful, svnkit calls back
 				// acknowledgeAuthentication
 				// (so we store the password info here)
 				repository = SVNRepositoryFactory.create(SVNURL
 						.parseURIDecoded(url));
 				repository.setTunnelProvider(SVNWCUtil
 						.createDefaultOptions(true));
 				AuthenticationManagerImpl authManager = upc.new AuthenticationManagerImpl(
 						logWriter) {
 					@Override
 					protected void onSuccess(String realm, Credential cred) {
 						LOGGER.info("Persisted " + cred + " for " + realm);
 						credentials.put(realm, cred);
 						save();
 						if (upc.inContextOf != null)
 							new PerJobCredentialStore(upc.inContextOf)
 									.acknowledgeAuthentication(realm, cred);
 
 					}
 				};
 				authManager.setAuthenticationForced(true);
 				repository.setAuthenticationManager(authManager);
 				repository.testConnection();
 				authManager.checkIfProtocolCompleted();
 			} finally {
 				if (repository != null)
 					repository.closeSession();
 			}
 		}
 
 		/**
 		 * validate the value for a remote (repository) location.
 		 */
 		// public FormValidation doCheckRemote(StaplerRequest req,
 		// @AncestorInPath AbstractProject context, @QueryParameter String
 		// value) {
 		// // syntax check first
 		// String url = Util.nullify(value);
 		// if (url == null)
 		// return FormValidation.ok();
 		//
 		// // remove unneeded whitespaces
 		// url = url.trim();
 		// if(!URL_PATTERN.matcher(url).matches())
 		// return FormValidation.errorWithMarkup(
 		// Messages.SubversionSCM_doCheckRemote_invalidUrl());
 		//
 		// // Test the connection only if we have admin permission
 		// if (!Hudson.getInstance().hasPermission(Hudson.ADMINISTER))
 		// return FormValidation.ok();
 		//
 		// try {
 		// SVNURL repoURL = SVNURL.parseURIDecoded(url);
 		// if (checkRepositoryPath(context,repoURL)!=SVNNodeKind.NONE)
 		// // something exists
 		// return FormValidation.ok();
 		//
 		// SVNRepository repository = null;
 		// try {
 		// repository = getRepository(context,repoURL);
 		// long rev = repository.getLatestRevision();
 		// // now go back the tree and find if there's anything that exists
 		// String repoPath = getRelativePath(repoURL, repository);
 		// String p = repoPath;
 		// while(p.length()>0) {
 		// p = SVNPathUtil.removeTail(p);
 		// if(repository.checkPath(p,rev)==SVNNodeKind.DIR) {
 		// // found a matching path
 		// List<SVNDirEntry> entries = new ArrayList<SVNDirEntry>();
 		// repository.getDir(p,rev,false,entries);
 		//
 		// // build up the name list
 		// List<String> paths = new ArrayList<String>();
 		// for (SVNDirEntry e : entries)
 		// if(e.getKind()==SVNNodeKind.DIR)
 		// paths.add(e.getName());
 		//
 		// String head = SVNPathUtil.head(repoPath.substring(p.length() + 1));
 		// String candidate = EditDistance.findNearest(head,paths);
 		//
 		// return FormValidation.error(
 		// Messages.SubversionSCM_doCheckRemote_badPathSuggest(p, head,
 		// candidate != null ? "/" + candidate : ""));
 		// }
 		// }
 		//
 		// return FormValidation.error(
 		// Messages.SubversionSCM_doCheckRemote_badPath(repoPath));
 		// } finally {
 		// if (repository != null)
 		// repository.closeSession();
 		// }
 		// } catch (SVNException e) {
 		// LOGGER.log(Level.INFO,
 		// "Failed to access subversion repository "+url,e);
 		// String message = Messages.SubversionSCM_doCheckRemote_exceptionMsg1(
 		// Util.escape(url), Util.escape(e.getErrorMessage().getFullMessage()),
 		// "javascript:document.getElementById('svnerror').style.display='block';"
 		// + "document.getElementById('svnerrorlink').style.display='none';"
 		// + "return false;")
 		// + "<br/><pre id=\"svnerror\" style=\"display:none\">"
 		// + Functions.printThrowable(e) + "</pre>"
 		// + Messages.SubversionSCM_doCheckRemote_exceptionMsg2(
 		// "descriptorByName/"+BlameSubversionSCM.class.getName()+"/enterCredential?"
 		// + url);
 		// return FormValidation.errorWithMarkup(message);
 		// }
 		// }
 
 		public SVNNodeKind checkRepositoryPath(AbstractProject context,
 				SVNURL repoURL) throws SVNException {
 			SVNRepository repository = null;
 
 			try {
 				repository = getRepository(context, repoURL);
 				repository.testConnection();
 
 				long rev = repository.getLatestRevision();
 				String repoPath = getRelativePath(repoURL, repository);
 				return repository.checkPath(repoPath, rev);
 			} finally {
 				if (repository != null)
 					repository.closeSession();
 			}
 		}
 
 		protected SVNRepository getRepository(AbstractProject context,
 				SVNURL repoURL) throws SVNException {
 			SVNRepository repository = SVNRepositoryFactory.create(repoURL);
 
 			ISVNAuthenticationManager sam = SVNWCUtil
 					.createDefaultAuthenticationManager();
 			sam = new FilterSVNAuthenticationManager(sam) {
 				// If there's no time out, the blocking read operation may hang
 				// forever, because TCP itself
 				// has no timeout. So always use some time out. If the
 				// underlying implementation gives us some
 				// value (which may come from ~/.subversion), honor that, as
 				// long as it sets some timeout value.
 				@Override
 				public int getReadTimeout(SVNRepository repository) {
 					int r = super.getReadTimeout(repository);
 					if (r <= 0)
 						r = DEFAULT_TIMEOUT;
 					return r;
 				}
 			};
 			sam
 					.setAuthenticationProvider(createAuthenticationProvider(context));
 			repository.setTunnelProvider(SVNWCUtil.createDefaultOptions(true));
 			repository.setAuthenticationManager(sam);
 
 			return repository;
 		}
 
 		public static String getRelativePath(SVNURL repoURL,
 				SVNRepository repository) throws SVNException {
 			String repoPath = repoURL.getPath().substring(
 					repository.getRepositoryRoot(false).getPath().length());
 			if (!repoPath.startsWith("/"))
 				repoPath = "/" + repoPath;
 			return repoPath;
 		}
 
 		/**
 		 * validate the value for a local location (local checkout directory).
 		 */
 		// public FormValidation doCheckLocal(@QueryParameter String value)
 		// throws IOException, ServletException {
 		// String v = Util.nullify(value);
 		// if (v == null)
 		// // local directory is optional so this is ok
 		// return FormValidation.ok();
 		//
 		// v = v.trim();
 		//
 		// // check if a absolute path has been supplied
 		// // (the last check with the regex will match windows drives)
 		// if (v.startsWith("/") || v.startsWith("\\") || v.startsWith("..") ||
 		// v.matches("^[A-Za-z]:"))
 		// return FormValidation.error("absolute path is not allowed");
 		//
 		// // all tests passed so far
 		// return FormValidation.ok();
 		// }
 
 		/**
 		 * Validates the excludeRegions Regex
 		 */
 		// public FormValidation doCheckExcludedRegions(@QueryParameter String
 		// value) throws IOException, ServletException {
 		// for (String region : Util.fixNull(value).trim().split("[\\r\\n]+"))
 		// try {
 		// Pattern.compile(region);
 		// } catch (PatternSyntaxException e) {
 		// return FormValidation.error("Invalid regular expression. " +
 		// e.getMessage());
 		// }
 		// return FormValidation.ok();
 		// }
 
 		/**
 		 * Validates the includedRegions Regex
 		 */
 		// public FormValidation doCheckIncludedRegions(@QueryParameter String
 		// value) throws IOException, ServletException {
 		// return doCheckExcludedRegions(value);
 		// }
 
 		/**
 		 * Regular expression for matching one username. Matches 'windows' names
 		 * ('DOMAIN&#92;user') and 'normal' names ('user'). Where user (and
 		 * DOMAIN) has one or more characters in 'a-zA-Z_0-9')
 		 */
 		private static final Pattern USERNAME_PATTERN = Pattern
 				.compile("(\\w+\\\\)?+(\\w+)");
 
 		/**
 		 * Validates the excludeUsers field
 		 */
 		// public FormValidation doCheckExcludedUsers(@QueryParameter String
 		// value) throws IOException, ServletException {
 		// for (String user : Util.fixNull(value).trim().split("[\\r\\n]+")) {
 		// user = user.trim();
 		//
 		// if ("".equals(user)) {
 		// continue;
 		// }
 		//
 		// if (!USERNAME_PATTERN.matcher(user).matches()) {
 		// return FormValidation.error("Invalid username: " + user);
 		// }
 		// }
 		//
 		// return FormValidation.ok();
 		// }
 
 		/**
 		 * Validates the excludeCommitMessages field
 		 */
 		// public FormValidation doCheckExcludedCommitMessages(@QueryParameter
 		// String value) throws IOException, ServletException {
 		// for (String message : Util.fixNull(value).trim().split("[\\r\\n]+"))
 		// {
 		// try {
 		// Pattern.compile(message);
 		// } catch (PatternSyntaxException e) {
 		// return FormValidation.error("Invalid regular expression. " +
 		// e.getMessage());
 		// }
 		// }
 		// return FormValidation.ok();
 		// }
 
 		/**
 		 * Validates the remote server supports custom revision properties
 		 */
 		// public FormValidation
 		// doCheckRevisionPropertiesSupported(@AncestorInPath AbstractProject
 		// context, @QueryParameter String value) throws IOException,
 		// ServletException {
 		// String v = Util.fixNull(value).trim();
 		// if (v.length() == 0)
 		// return FormValidation.ok();
 		//
 		// // Test the connection only if we have admin permission
 		// if (!Hudson.getInstance().hasPermission(Hudson.ADMINISTER))
 		// return FormValidation.ok();
 		//
 		// try {
 		// SVNURL repoURL = SVNURL.parseURIDecoded(v);
 		// if (checkRepositoryPath(context,repoURL)!=SVNNodeKind.NONE)
 		// // something exists
 		// return FormValidation.ok();
 		//
 		// SVNRepository repository = null;
 		// try {
 		// repository = getRepository(context,repoURL);
 		// if (repository.hasCapability(SVNCapability.LOG_REVPROPS))
 		// return FormValidation.ok();
 		// } finally {
 		// if (repository != null)
 		// repository.closeSession();
 		// }
 		// } catch (SVNException e) {
 		// String message="";
 		// message += "Unable to access "+Util.escape(v)+" : "+Util.escape(
 		// e.getErrorMessage().getFullMessage());
 		// LOGGER.log(Level.INFO,
 		// "Failed to access subversion repository "+v,e);
 		// return FormValidation.errorWithMarkup(message);
 		// }
 		//
 		// return
 		// FormValidation.warning(Messages.SubversionSCM_excludedRevprop_notSupported(v));
 		// }
 
 		static {
 			new Initializer();
 		}
 
 	}
 
 	public boolean repositoryLocationsNoLongerExist(AbstractBuild<?, ?> build,
 			TaskListener listener) {
 		PrintStream out = listener.getLogger();
 
 		for (ModuleLocation l : getLocations(build))
 			try {
 				if (getDescriptor().checkRepositoryPath(build.getProject(),
 						l.getSVNURL()) == SVNNodeKind.NONE) {
 					out.println("Location '" + l.remote + "' does not exist");
 
 					ParametersAction params = build
 							.getAction(ParametersAction.class);
 					if (params != null) {
 						// since this is used to disable projects, be
 						// conservative
 						LOGGER.fine("Location could be expanded on build '"
 								+ build + "' parameters values:");
 						return false;
 					}
 					return true;
 				}
 			} catch (SVNException e) {
 				// be conservative, since we are just trying to be helpful in
 				// detecting
 				// non existent locations. If we can't detect that, we'll do
 				// nothing
 				LOGGER.log(FINE, "Location check failed", e);
 			}
 		return false;
 	}
 
 	static final Pattern URL_PATTERN = Pattern
 			.compile("(https?|svn(\\+[a-z0-9]+)?|file)://.+");
 
 	private static final long serialVersionUID = 1L;
 
 	// noop, but this forces the initializer to run.
 	public static void init() {
 	}
 
 	static {
 		new Initializer();
 	}
 
 	private static final class Initializer {
 		static {
 			if (Boolean.getBoolean("hudson.spool-svn"))
 				DAVRepositoryFactory.setup(new DefaultHTTPConnectionFactory(
 						null, true, null));
 			else
 				DAVRepositoryFactory.setup(); // http, https
 			SVNRepositoryFactoryImpl.setup(); // svn, svn+xxx
 			FSRepositoryFactory.setup(); // file
 
 			// disable the connection pooling, which causes problems like
 			// http://www.nabble.com/SSH-connection-problems-p12028339.html
 			if (System.getProperty("svnkit.ssh2.persistent") == null)
 				System.setProperty("svnkit.ssh2.persistent", "false");
 
 			// use SVN1.4 compatible workspace by default.
 			SVNAdminAreaFactory
 					.setSelector(new BlameSubversionWorkspaceSelector());
 		}
 	}
 
 	/**
 	 * small structure to store local and remote (repository) location
 	 * information of the repository. As a addition it holds the invalid field
 	 * to make failure messages when doing a checkout possible
 	 */
 	@ExportedBean
 	public static final class ModuleLocation implements Serializable {
 		/**
 		 * Subversion URL to check out.
 		 * 
 		 * This may include "@NNN" at the end to indicate a fixed revision.
 		 */
 		@Exported
 		public final String remote;
 
 		/**
 		 * Remembers the user-given value. Can be null.
 		 * 
 		 * @deprecated Code should use {@link #getLocalDir()}. This field is
 		 *             only intended for form binding.
 		 */
 		@Exported
 		public final String local;
 
 		/**
 		 * Cache of the repository UUID.
 		 */
 		private transient volatile UUID repositoryUUID;
 		private transient volatile SVNURL repositoryRoot;
 
 		@DataBoundConstructor
 		public ModuleLocation(String remote, String local) {
 			this.remote = Util.removeTrailingSlash(Util.fixNull(remote).trim());
 			this.local = fixEmptyAndTrim(local);
 		}
 
 		/**
 		 * Local directory to place the file to. Relative to the workspace root.
 		 */
 		public String getLocalDir() {
 			if (local == null)
 				return getLastPathComponent(remote);
 			return local;
 		}
 
 		/**
 		 * Returns the pure URL portion of {@link #remote} by removing possible
 		 * "@NNN" suffix.
 		 */
 		public String getURL() {
 			int idx = remote.lastIndexOf('@');
 			if (idx > 0) {
 				try {
 					String n = remote.substring(idx + 1);
 					Long.parseLong(n);
 					return remote.substring(0, idx);
 				} catch (NumberFormatException e) {
 					// not a revision number
 				}
 			}
 			return remote;
 		}
 
 		/**
 		 * Gets {@link #remote} as {@link SVNURL}.
 		 */
 		public SVNURL getSVNURL() throws SVNException {
 			return SVNURL.parseURIEncoded(getURL());
 		}
 
 		/**
 		 * Repository UUID. Lazy computed and cached.
 		 */
 		public UUID getUUID(AbstractProject context) throws SVNException {
 			if (repositoryUUID == null || repositoryRoot == null) {
 				synchronized (this) {
 					SVNRepository r = openRepository(context);
 					r.testConnection(); // make sure values are fetched
 					repositoryUUID = UUID
 							.fromString(r.getRepositoryUUID(false));
 					repositoryRoot = r.getRepositoryRoot(false);
 				}
 			}
 			return repositoryUUID;
 		}
 
 		public SVNRepository openRepository(AbstractProject context)
 				throws SVNException {
 			return Hudson.getInstance().getDescriptorByType(
 					DescriptorImpl.class).getRepository(context, getSVNURL());
 		}
 
 		public SVNURL getRepositoryRoot(AbstractProject context)
 				throws SVNException {
 			getUUID(context);
 			return repositoryRoot;
 		}
 
 		/**
 		 * Figures out which revision to check out.
 		 * 
 		 * If {@link #remote} is {@code url@rev}, then this method returns that
 		 * specific revision.
 		 * 
 		 * @param defaultValue
 		 *            If "@NNN" portion is not in the URL, this value will be
 		 *            returned. Normally, this is the SVN revision timestamped
 		 *            at the build date.
 		 */
 		public SVNRevision getRevision(SVNRevision defaultValue) {
 			int idx = remote.lastIndexOf('@');
 			if (idx > 0) {
 				try {
 					String n = remote.substring(idx + 1);
 					return SVNRevision.create(Long.parseLong(n));
 				} catch (NumberFormatException e) {
 					// not a revision number
 				}
 			}
 			return defaultValue;
 		}
 
 		private String getExpandedRemote(AbstractBuild<?, ?> build) {
 			String outRemote = remote;
 
 			ParametersAction parameters = build
 					.getAction(ParametersAction.class);
 			if (parameters != null)
 				outRemote = parameters.substitute(build, remote);
 
 			return outRemote;
 		}
 
 		/**
 		 * Expand location value based on Build parametric execution.
 		 * 
 		 * @param build
 		 *            Build instance for expanding parameters into their values
 		 * 
 		 * @return Output ModuleLocation expanded according to Build parameters
 		 *         values.
 		 */
 		public ModuleLocation getExpandedLocation(AbstractBuild<?, ?> build) {
 			return new ModuleLocation(getExpandedRemote(build), getLocalDir());
 		}
 
 		@Override
 		public String toString() {
 			return remote;
 		}
 
 		private static final long serialVersionUID = 1L;
 
 		public static List<ModuleLocation> parse(String[] remoteLocations,
 				String[] localLocations) {
 			List<ModuleLocation> modules = new ArrayList<ModuleLocation>();
 			if (remoteLocations != null && localLocations != null) {
 				int entries = Math.min(remoteLocations.length,
 						localLocations.length);
 
 				for (int i = 0; i < entries; i++) {
 					// the remote (repository) location
 					String remoteLoc = Util.nullify(remoteLocations[i]);
 
 					if (remoteLoc != null) {// null if skipped
 						remoteLoc = Util.removeTrailingSlash(remoteLoc.trim());
 						modules.add(new ModuleLocation(remoteLoc, Util
 								.nullify(localLocations[i])));
 					}
 				}
 			}
 			return modules;
 		}
 	}
 
 	private static final Logger LOGGER = Logger
 			.getLogger(BlameSubversionSCM.class.getName());
 
 	/**
 	 * Network timeout in milliseconds. The main point of this is to prevent
 	 * infinite hang, so it should be a rather long value to avoid accidental
 	 * time out problem.
 	 */
 	public static int DEFAULT_TIMEOUT = Integer.getInteger(
 			BlameSubversionSCM.class.getName() + ".timeout", 3600 * 1000);
 
 	/**
 	 * Enables trace logging of Ganymed SSH library.
 	 * <p>
 	 * Intended to be invoked from Groovy console.
 	 */
 	public static void enableSshDebug(Level level) {
 		if (level == null)
 			level = Level.FINEST; // default
 
 		final Level lv = level;
 
 		com.trilead.ssh2.log.Logger.enabled = true;
 		com.trilead.ssh2.log.Logger.logger = new DebugLogger() {
 			private final Logger LOGGER = Logger.getLogger(SCPClient.class
 					.getPackage().getName());
 
 			public void log(int level, String className, String message) {
 				LOGGER.log(lv, className + ' ' + message);
 			}
 		};
 	}
 
 	/* package */static boolean compareSVNAuthentications(SVNAuthentication a1,
 			SVNAuthentication a2) {
 		if (a1 == null && a2 == null)
 			return true;
 		if (a1 == null || a2 == null)
 			return false;
 		if (a1.getClass() != a2.getClass())
 			return false;
 
 		try {
 			return describeBean(a1).equals(describeBean(a2));
 		} catch (IllegalAccessException e) {
 			return false;
 		} catch (InvocationTargetException e) {
 			return false;
 		} catch (NoSuchMethodException e) {
 			return false;
 		}
 	}
 
 	/**
 	 * In preparation for a comparison, char[] needs to be converted that
 	 * supports value equality.
 	 */
 	private static Map describeBean(Object o) throws InvocationTargetException,
 			NoSuchMethodException, IllegalAccessException {
 		Map<?, ?> m = PropertyUtils.describe(o);
 		for (Entry e : m.entrySet()) {
 			Object v = e.getValue();
 			if (v instanceof char[]) {
 				char[] chars = (char[]) v;
 				e.setValue(new String(chars));
 			}
 		}
 		return m;
 	}
 
 }
