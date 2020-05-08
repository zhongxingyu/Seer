 /*
  * Maven Packaging Plugin,
  * Maven plugin to package a Project (deb, ipk, izpack)
  * Copyright (C) 2000-2008 tarent GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License,version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  * 02110-1301, USA.
  *
  * tarent GmbH., hereby disclaims all copyright
  * interest in the program 'Maven Packaging Plugin'
  * Signature of Elmar Geese, 11 March 2008
  * Elmar Geese, CEO tarent GmbH.
  */
 
 /*
  * Maven Packaging Plugin,
  * Maven plugin to package a Project (deb and izpack)
  * Copyright (C) 2000-2007 tarent GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License,version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  * 02110-1301, USA.
  *
  * tarent GmbH., hereby disclaims all copyright
  * interest in the program 'Maven Packaging Plugin'
  * Signature of Elmar Geese, 14 June 2007
  * Elmar Geese, CEO tarent GmbH.
  */
 
 /* $Id: AbstractPackagingMojo.java,v 1.16 2007/08/07 11:29:59 robert Exp $
  *
 * pkg-maven-plugin, Packaging plugin for Maven2 
  * Copyright (C) 2007 tarent GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  *
  * tarent GmbH., hereby disclaims all copyright
  * interest in the program 'maven-pkg-plugin'
  * written by Robert Schuster, Fabian Koester. 
  * signature of Elmar Geese, 1 June 2002
  * Elmar Geese, CEO tarent GmbH
  */
 
 package de.tarent.maven.plugins.pkg;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactResolver;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.PluginManager;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.MavenProjectBuilder;
 
 import de.tarent.maven.plugins.pkg.helper.ArtifactInclusionStrategy;
 import de.tarent.maven.plugins.pkg.helper.Helper;
 import de.tarent.maven.plugins.pkg.map.PackageMap;
 
 /**
  * Base Mojo for all packaging mojos. It provides convenient access to a mean to
  * resolve the project's complete dependencies.
  */
 public abstract class AbstractPackagingMojo extends AbstractMojo {
 
 	private static final String DEFAULT_SRC_AUXFILESDIR = "src/main/auxfiles";
 
 	public static String getDefaultSrcAuxfilesdir() {
 		return DEFAULT_SRC_AUXFILESDIR;
 	}
 
 	/**
 	 * @parameter expression="${project}"
 	 * @required
 	 * @readonly
 	 */
 	protected MavenProject project;
 
 	/**
 	 * Artifact factory, needed to download source jars.
 	 * 
 	 * @component role="org.apache.maven.project.MavenProjectBuilder"
 	 * @required
 	 * @readonly
 	 */
 	protected MavenProjectBuilder mavenProjectBuilder;
 
 	/**
 	 * Temporary directory that contains the files to be assembled.
 	 * 
 	 * @parameter expression="${project.build.directory}"
 	 * @required
 	 * @readonly
 	 */
 	protected File buildDir;
 
 	/**
 	 * Used to look up Artifacts in the remote repository.
 	 * 
 	 * @component role="org.apache.maven.artifact.factory.ArtifactFactory"
 	 * @required
 	 * @readonly
 	 */
 	protected ArtifactFactory factory;
 
 	/**
 	 * Used to look up Artifacts in the remote repository.
 	 * 
 	 * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
 	 * @required
 	 * @readonly
 	 */
 	protected ArtifactResolver resolver;
 
 	/**
 	 * Used to look up Artifacts in the remote repository.
 	 * 
 	 * @component 
 	 *            role="org.apache.maven.artifact.metadata.ArtifactMetadataSource"
 	 * @required
 	 * @readonly
 	 */
 	protected ArtifactMetadataSource metadataSource;
 
 	/**
 	 * Location of the local repository.
 	 * 
 	 * @parameter expression="${localRepository}"
 	 * @readonly
 	 * @required
 	 */
 	protected ArtifactRepository local;
 
 	/**
 	 * List of Remote Repositories used by the resolver
 	 * 
 	 * @parameter expression="${project.remoteArtifactRepositories}"
 	 * @readonly
 	 * @required
 	 */
 	protected List<ArtifactRepository> remoteRepos;
 
 	/**
 	 * @parameter expression="${project.artifact}"
 	 * @required
 	 * @readonly
 	 */
 	protected Artifact artifact;
 
 	/**
 	 * @parameter expression="${project.artifactId}"
 	 * @required
 	 * @readonly
 	 */
 	protected String artifactId;
 
 	/**
 	 * @parameter expression="${project.build.finalName}"
 	 * @required
 	 * @readonly
 	 */
 	protected String finalName;
 
 	/**
 	 * @parameter expression="${project.build.directory}"
 	 * @required
 	 * @readonly
 	 */
 	protected File outputDirectory;
 
 	/**
 	 * @parameter expression="${project.version}"
 	 * @required
 	 * @readonly
 	 */
 	protected String version;
 
 	/**
 	 * JVM binary used to run Java programs from within the Mojo.
 	 * 
 	 * @parameter expression="${javaExec}" default-value="java"
 	 * @required
 	 * 
 	 */
 	protected String javaExec;
 
 	/**
 	 * 7Zip binary used to run Java programs from within the Mojo.
 	 * 
 	 * @parameter expression="${7zipExec}" default-value="7zr"
 	 * @required
 	 * 
 	 */
 	protected String _7zipExec;
 
 	/**
 	 * Location of the custom package map file. When specifying this one the
 	 * internal package map will be overridden completely.
 	 * 
 	 * @parameter expression="${defPackageMapURL}"
 	 */
 	protected URL defaultPackageMapURL;
 
 	/**
 	 * Location of the auxiliary package map file. When this is specified the
 	 * information in the document will be added to the default one.
 	 * 
 	 * @parameter expression="${auxPackageMapURL}"
 	 */
 	protected URL auxPackageMapURL;
 
 	/**
 	 * Overrides "defaultDistro" parameter. For use on the command-line.
 	 * 
 	 * @parameter expression="${distro}"
 	 */
 	protected String distro;
 
 	/**
 	 * Overrides "defaultIgnorePackagingTypes" defines a list of comma
 	 * speparated packaging types that, when used, will skip copying the main
 	 * artifact for the project (if any) in the final package. For use on the
 	 * command-line.
 	 * 
 	 * @parameter expression="${ignorePackagingTypes}" default-value="pom"
 	 * @required
 	 */
 	protected String ignorePackagingTypes;
 
 	/**
 	 * Parameter with a comma separated list of targets. For use on the
 	 * command-line.
 	 * 
 	 * @parameter
 	 */
 	protected String target;
 
 	/**
 	 * This parameter allows overriding the target set in the POM through the
 	 * command line
 	 * 
 	 * @parameter expression="${target}"
 	 */
 	protected String overrideTarget;
 
 	/**
 	 * @parameter
 	 */
 	protected List<TargetConfiguration> targetConfigurations;
 
 	/**
 	 * The Maven Session Object
 	 * 
 	 * @parameter expression="${session}"
 	 * @required
 	 * @readonly
 	 */
 	protected MavenSession session;
 	/**
 	 * The Maven PluginManager Object
 	 * 
 	 * @component
 	 * @required
 	 */
 	protected PluginManager pluginManager;
 
 	/**
 	 * This parameter allows overriding the deletion of the temp directory where
 	 * packages are built.<br/>
 	 * 
 	 * @parameter expression="${keepPkgTmp}"
 	 */
 	protected boolean keepPkgTmp;
 
 	public MavenSession getSession() {
 		return session;
 	}
 
 	public PluginManager getPluginManager() {
 		return pluginManager;
 	}
 
 	public void setSignPassPhrase(String phrase) {
 		this.signPassPhrase = phrase;
 	}
 
 	public String getSignPassPhrase() {
 		return signPassPhrase;
 	}
 
 	protected String signPassPhrase;
 
 	private File tempRoot;
 
 	public String get_7zipExec() {
 		return _7zipExec;
 	}
 
 	public File getBuildDir() {
 		return buildDir;
 	}
 
 	public ArtifactFactory getFactory() {
 		return factory;
 	}
 
 	public String getFinalName() {
 		return finalName;
 	}
 
 	public String getIgnorePackagingTypes() {
 		return ignorePackagingTypes;
 	}
 
 	public String getJavaExec() {
 		return javaExec;
 	}
 
 	public ArtifactRepository getLocalRepo() {
 		return local;
 	}
 
 	public ArtifactMetadataSource getMetadataSource() {
 		return metadataSource;
 	}
 
 	public File getOutputDirectory() {
 		return outputDirectory;
 	}
 
 	public MavenProject getProject() {
 		return project;
 	}
 
 	public List<ArtifactRepository> getRemoteRepos() {
 		return remoteRepos;
 	}
 
 	public ArtifactResolver getResolver() {
 		return resolver;
 	}
 
 	public List<TargetConfiguration> getTargetConfigurations() {
 		return targetConfigurations;
 	}
 
 	protected final boolean packagingTypeBelongsToIgnoreList() {
 		boolean inList = false;
 		Log l = getLog();
 		l.info("ignorePackagingTypes set. Contains: " + ignorePackagingTypes
 				+ " . Project packaging is " + project.getPackaging());
 		for (String s : ignorePackagingTypes.split(",")) {
 			if (project.getPackaging().compareToIgnoreCase(s) == 0) {
 				inList = true;
 			}
 		}
 		return inList;
 	}
 
 	/**
 	 * 
 	 * Returns the comma separated target list as String[].</br></br>
 	 * 
 	 * In order to allow multiple target to be called through the command line
 	 * we allow the user to provide a comma separated list (Maven < 3.0.3 does
 	 * not transform comma separated values from the command line to String[]).
 	 * 
 	 * @param target
 	 * @return
 	 */
 	protected String[] getTargets() throws MojoExecutionException {
 
 		String[] targetArray = null;
 
 		if (overrideTarget != null) {
 			targetArray = overrideTarget.split(",");
 		} else if (target != null) {
 			targetArray = target.split(",");
 		} else {
 			throw new MojoExecutionException(
 					"No target(s) specified for execution.");
 		}
 
 		return targetArray;
 
 	}
 
 	public File getTempRoot() {
 		if (tempRoot == null) {
 			tempRoot = new File(getBuildDir(), "pkg-tmp");
 		}
 		return tempRoot;
 	}
 
 	/**
 	 * Validates arguments and test tools.
 	 * 
 	 * @throws MojoExecutionException
 	 */
 	protected void checkEnvironment(Log l, TargetConfiguration tc)
 			throws MojoExecutionException {
 		// l.info("distribution             : " + tc.getChosenDistro());
 		l.info("default package map      : "
 				+ (defaultPackageMapURL == null ? "built-in"
 						: defaultPackageMapURL.toString()));
 		l.info("auxiliary package map    : "
 				+ (auxPackageMapURL == null ? "no" : auxPackageMapURL
 						.toString()));
 		l.info("type of project          : "
 				+ ((tc.getMainClass() != null) ? "application" : "library"));
 		l.info("section                  : " + tc.getSection());
 		l.info("bundle all dependencies  : "
 				+ ((tc.isBundleAll()) ? "yes" : "no"));
 		l.info("ahead of time compilation: "
 				+ ((tc.isAotCompile()) ? "yes" : "no"));
 		l.info("custom jar libraries     : "
 				+ ((tc.getJarFiles().isEmpty()) ? "<none>" : String.valueOf(tc
 						.getJarFiles().size())));
 		l.info("JNI libraries            : "
 				+ ((tc.getJniFiles().isEmpty()) ? "<none>" : String.valueOf(tc
 						.getJniFiles().size())));
 		l.info("auxiliary file source dir: "
 				+ (tc.getSrcAuxFilesDir().length() == 0 ? (getDefaultSrcAuxfilesdir() + " (default)")
 						: tc.getSrcAuxFilesDir()));
 		l.info("auxiliary files          : "
 				+ ((tc.getAuxFiles().isEmpty()) ? "<none>" : String.valueOf(tc
 						.getAuxFiles().size())));
 		l.info("prefix                   : "
 				+ (tc.getPrefix().length() == 1 ? "/ (default)" : tc
 						.getPrefix()));
 		l.info("sysconf files source dir : "
 				+ (tc.getSrcSysconfFilesDir().length() == 0 ? (getDefaultSrcAuxfilesdir() + " (default)")
 						: tc.getSrcSysconfFilesDir()));
 		l.info("sysconfdir               : "
 				+ (tc.getSysconfdir().length() == 0 ? "(default)" : tc
 						.getSysconfdir()));
 		l.info("dataroot files source dir: "
 				+ (tc.getSrcDatarootFilesDir().length() == 0 ? (getDefaultSrcAuxfilesdir() + " (default)")
 						: tc.getSrcDatarootFilesDir()));
 		l.info("dataroot                 : "
 				+ (tc.getDatarootdir().length() == 0 ? "(default)" : tc
 						.getDatarootdir()));
 		l.info("data files source dir    : "
 				+ (tc.getSrcDataFilesDir().length() == 0 ? (getDefaultSrcAuxfilesdir() + " (default)")
 						: tc.getSrcDataFilesDir()));
 		l.info("datadir                  : "
 				+ (tc.getDatadir().length() == 0 ? "(default)" : tc
 						.getDatadir()));
 		l.info("bindir                   : "
 				+ (tc.getBindir().length() == 0 ? "(default)" : tc.getBindir()));
 
 		/*
 		 * if (ws.getChosenDistro() == null) { throw new
 		 * MojoExecutionException("No distribution configured!"); }
 		 */
 
 		if (tc.isAotCompile()) {
 			l.info("aot compiler             : " + tc.getGcjExec());
 			l.info("aot classmap generator   : " + tc.getGcjDbToolExec());
 		}
 
 		if (tc.getMainClass() == null) {
 			if (!"libs".equals(tc.getSection())) {
 				throw new MojoExecutionException(
 						"section has to be 'libs' if no main class is given.");
 			}
 			if (tc.isBundleAll()) {
 				throw new MojoExecutionException(
 						"Bundling dependencies to a library makes no sense.");
 			}
 		} else {
 			if ("libs".equals(tc.getSection())) {
 				throw new MojoExecutionException(
 						"Set a proper section if main class parameter is set.");
 			}
 		}
 
 		if (tc.isAotCompile()) {
 			AotCompileUtils.setGcjExecutable(tc.getGcjExec());
 			AotCompileUtils.setGcjDbToolExecutable(tc.getGcjDbToolExec());
 
 			AotCompileUtils.checkToolAvailability();
 		}
 	}
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
 
 		// We will merge all targetConfigurations with their parents,
 		// so tat all configurations are ready to be used from this point on
 
 		Utils.mergeAllConfigurations(targetConfigurations);
 
 		// For some tasks it is practical to have the TargetConfiguration
 		// instances as a
 		// map. This transformation step also serves as check for double
 		// entries.
 		Map<String, TargetConfiguration> targetConfigurationMap = Utils
 				.toMap(targetConfigurations);
 
 		// Container for collecting target configurations that have been built.
 		// This
 		// is used to make sure that TCs are not build repeatedly when the given
 		// target
 		// configuration have a dependency to a common target configuration.
 		HashSet<String> finishedTargets = new HashSet<String>();
 
 		checkIfPackagesWillOverwrite();
 
 		for (String t : getTargets()) {
 			// A single target (and all its dependent target configurations are
 			// supposed to use the same
 			// distro value).
 			String d = (distro != null) ? distro : Utils.getDefaultDistro(t,
 					targetConfigurations, getLog());
 
 			// Retrieve all target configurations that need to be build for /t/
 			List<TargetConfiguration> buildChain = Utils.createBuildChain(t, d,
 					targetConfigurations);
 
 			for (TargetConfiguration tc : buildChain) {
 				if (!finishedTargets.contains(tc.getTarget()) && tc.isReady()) {
 					WorkspaceSession ws = new WorkspaceSession();
 					ws.setMojo(this); // its us
 					ws.setTargetConfigurationMap(targetConfigurationMap);
 					ws.setTargetConfiguration(tc);
 
 					// Populates session with PackageMap, Helper and resolved
 					// relations
 					prepareWorkspaceSession(ws, d);
 
 					executeTargetConfiguration(ws);
 
 					// Mark as done.
 					finishedTargets.add(tc.getTarget());
 				}
 
 			}
 		}
 		getLog().info(
				"pkg-maven-plugin goal succesfully executed for "
 						+ finishedTargets.size() + " target(s).");
 		cleanUp();
 	}
 
 	/**
 	 * Removes "pkg-tmp" from the target directory of the project. This can be
 	 * overriden if removePkgTmp is set to false.
 	 * 
 	 * @throws MojoExecutionException
 	 */
 	private void cleanUp() throws MojoExecutionException {
 		File pkgTmp = new File(getBuildDir(), "pkg-tmp");
 		if (pkgTmp.exists() && !keepPkgTmp) {
 			try {
 				FileUtils.deleteDirectory(pkgTmp);
 			} catch (IOException e) {
 				throw new MojoExecutionException(
 						"Unable to remove temporary directory pkg-tmp");
 			}
 		}
 
 	}
 
 	private void prepareWorkspaceSession(WorkspaceSession ws, String distro)
 			throws MojoExecutionException, MojoFailureException {
 
 		AbstractPackagingMojo mojo = ws.getMojo();
 		TargetConfiguration tc = ws.getTargetConfiguration();
 		Map<String, TargetConfiguration> tcMap = ws.getTargetConfigurationMap();
 
 		ArtifactInclusionStrategy strategy = ArtifactInclusionStrategy
 				.getStrategyInstance(tc.getArtifactInclusion());
 		ws.setArtifactInclusionStrategy(strategy);
 
 		// At first we create the various work objects that we need to process
 		// the
 		// request to package what is specified in 'tc' and test their validity.
 
 		// Resolve all the relations of the given target configuration. This can
 		// fail
 		// with an exception if there is a configuration mistake.
 		List<TargetConfiguration> resolvedRelations = Utils
 				.resolveConfigurations(tc.getRelations(), tcMap);
 
 		// Retrieve package map for chosen distro.
 		PackageMap pm = new PackageMap(defaultPackageMapURL, auxPackageMapURL,
 				distro, tc.getBundleDependencies());
 
 		String packaging = pm.getPackaging();
 
 		if (packaging == null) {
 			throw new MojoExecutionException(
 					"Package maps document set no packaging for distro: "
 							+ distro);
 		}
 
 		// Create packager and packaging helper according to the chosen
 		// packaging type.
 		// Note: Helper is historically strongly dependent on the mojo, the
 		// package and
 		// the targetconfiguration because this makes method calls to the helper
 		// so neatly
 		// short and uniform among all Packager implementations, ie. there're
 		// almost no
 		// arguments needed and all packagers call the same stuff while in
 		// reality they're
 		// subtle differences between them.
 		Helper ph = new Helper();
 		ph.init(mojo, pm, tc, resolvedRelations, distro);
 
 		// Finally now that we know that our cool newly created work objects are
 		// prepared and can be used (none of them is null) we stuff them
 		// into the session and run the actual packaging steps.
 		ws.setResolvedRelations(resolvedRelations);
 		ws.setPackageMap(pm);
 		ws.setHelper(ph);
 	}
 
 	protected abstract void executeTargetConfiguration(
 			WorkspaceSession workspaceSession) throws MojoExecutionException,
 			MojoFailureException;
 
 	/**
 	 * Ensures that execution is aborted if the packages generated would
 	 * overwrite (i.e. have the same filename).
 	 * 
 	 * @throws MojoExecutionException
 	 */
 	private void checkIfPackagesWillOverwrite() throws MojoExecutionException {
 
 		// All filenames will be stored here
 		Set<String> filenames = new HashSet<String>();
 		// We will need a helper to generate the filename
 		Helper ph;
 		// And a packageMap, as it is there where it is set if the package name
 		// should be lowercase
 		PackageMap pm;
 
 		// We will iterate through all selected targets...
 		for (String target : getTargets()) {
 
 			// ... and will grab each of them in form of a targetConfiguration
 			TargetConfiguration currentTarget = Utils
 					.getTargetConfigurationFromString(target,
 							targetConfigurations);
 
 			// This method could provide wrong results if the configuration is
 			// not ready
 			if (!currentTarget.isReady()) {
 				StringBuilder sb = new StringBuilder();
 				sb.append("The targetConfiguration \"");
 				sb.append(currentTarget.getTarget());
 				sb.append("\" is not ready. Are you "
 						+ "executing this method before "
 						+ "merging all Configurations?");
 				getLog().error(sb.toString());
 				throw new MojoExecutionException(sb.toString());
 			}
 
 			// The packageMap needs to be defined for each targetConfiguration
 			pm = new PackageMap(defaultPackageMapURL, auxPackageMapURL, distro,
 					currentTarget.getBundleDependencies());
 			// The helper is initialised for each targetConfiguration- resolved
 			// relations are not relevant
 			ph = new Helper();
 			ph.init(this, pm, currentTarget, null,
 					currentTarget.getDefaultDistro());
 
 			// Each filename will be included in the set until one duplicate is
 			// found
 			if (!filenames.add(ph.getPackageFileName())) {
 				StringBuilder sb = new StringBuilder();
 				sb.append("The package filename \"");
 				sb.append(ph.getPackageFileName());
 				sb.append("\" has been calculated for more than one "
 						+ "configuration. This would lead to "
 						+ "files being overwritten.");
 				sb.append("Please check your POM and "
 						+ "consider using packageNameSuffix "
 						+ "or packageVersionSuffix.");
 				getLog().error(sb.toString());
 				throw new MojoExecutionException(sb.toString());
 			}
 		}
 	}
 
 }
