 package de.tarent.maven.plugins.pkg.helper;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.io.filefilter.TrueFileFilter;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionException;
 import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
 import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
 import org.apache.maven.artifact.resolver.filter.TypeArtifactFilter;
 import org.apache.maven.model.License;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.ProjectBuildingException;
 import org.apache.maven.project.artifact.InvalidDependencyVersionException;
 
 import de.tarent.maven.plugins.pkg.AbstractPackagingMojo;
 import de.tarent.maven.plugins.pkg.AuxFile;
 import de.tarent.maven.plugins.pkg.JarFile;
 import de.tarent.maven.plugins.pkg.Packaging;
 import de.tarent.maven.plugins.pkg.Path;
 import de.tarent.maven.plugins.pkg.TargetConfiguration;
 import de.tarent.maven.plugins.pkg.Utils;
 import de.tarent.maven.plugins.pkg.generator.WrapperScriptGenerator;
 import de.tarent.maven.plugins.pkg.map.Entry;
 import de.tarent.maven.plugins.pkg.map.PackageMap;
 import de.tarent.maven.plugins.pkg.map.Visitor;
 
 /**
  * The <code>Helper</code> class mainly provides task oriented methods which can
  * be directly used by the packager implementations.
  * 
  * <p>
  * The idea is that all packagers basically do the same actions (copy jars,
  * create classpath and dependency line, ...) but operate on different
  * directories and/or files names. Therefore the packager implementation must
  * set the various file names and directories and afterwards call the
  * task-oriented methods.
  * </p>
  * 
  * <p>
  * The method's documentation describes their relationship and usage of
  * variables.
  * </p>
  * 
  * <p>
  * There is a common relationship between the method with <code>target</code>
  * and <code>dest</code> in their name. <code>target</code> means a file
  * location or directory which is available on the target device (= after
  * installation). Thus such a file must not be used for file operations at
  * packaging time since it will simply not be valid. In contrast a method having
  * <code>dest</code> in their name denotes the file of the corresponding
  * <code>target</code> entry at packaging time.
  * </p>
  * 
  * <p>
  * All <code>getTarget...</code>-method with a corresponding
  * <code>getDest...</code> method can provide a default value whose generation
  * is described in the method's documentation. However if a different value is
  * provided by the packager implementation by calling the corresponding setter
  * the automatic generation is prevented. By doing so a packager can customize
  * all file and directory locations.
  * </p>
  * 
  * <p>
  * A notable exception to this rule is the {@link #getDstScriptDir()} method
  * which fails with an exception if no non-null value for the
  * <code>dstScriptDir</code> property has been set.
  * </p>
  * 
  * @author Robert Schuster (robert.schuster@tarent.de)
  */
 public class Helper {
 
 	private String aotPackageName;
 
 	/**
 	 * The base directory for the gcj package.
 	 */
 	private File aotPkgDir;
 
 	/**
 	 * All files belonging to the package are put into this directory. For deb
 	 * packaging the layout inside is done according to `man dpkg-deb`.
 	 */
 	private File basePkgDir;
 
 	/**
 	 * The destination file for the project's artifact inside the the package at
 	 * construction time (equals ${basePkgDir}/${targetArtifactFile}).
 	 */
 	private File dstArtifactFile;
 
 	private File dstAuxDir;
 
 	private File dstBinDir;
 
 	private File dstBundledJarDir;
 
 	private File dstDataDir;
 
 	private File dstDatarootDir;
 
 	/**
 	 * The destination directory for JNI libraries at package building time
 	 * (e.g. starts with "${packaging.getProject().outputdir}/")
 	 */
 	private File dstJNIDir;
 
 	private File dstRoot;
 
 	private File dstScriptDir;
 
 	private File dstStarterDir;
 
 	private File dstSysconfDir;
 
 	private File dstWindowsWrapperScriptFile;
 
 	private File dstWrapperScriptFile;
 
 	private String packageName;
 
 	private String packageVersion;
 
 	/**
 	 * A file pointing at the source jar (it *MUST* be a jar).
 	 */
 	private File srcArtifactFile;
 
 	private File targetAuxDir;
 
 	/**
 	 * Location of the project's artifact on the target system (needed for the
 	 * classpath construction). (e.g. /usr/share/java/app-2.0-SNAPSHOT.jar)
 	 */
 	private File targetArtifactFile;
 
 	private File targetBinDir;
 
 	/**
 	 * Location of the project's dependency artifacts on the target system
 	 * (needed for classpath construction.).
 	 * <p>
 	 * If the path contains a variable that is to be replaced by an installer it
 	 * must not be used in actual file operations! To prevent this from
 	 * happening provide explicit value for all properties which use
 	 * {@link #getTargetArtifactFile()}
 	 * </p>
 	 * (e.g. ${INSTALL_DIR}/libs)
 	 */
 	private File targetBundledJarDir;
 
 	private File targetDataDir;
 
 	private File targetDatarootDir;
 
 	/**
 	 * Location of the JNI libraries on the target device (e.g. /usr/lib/jni).
 	 */
 	private File targetJNIDir;
 
 	/**
 	 * Location of the path which contains JNI libraries on the target device.
 	 * (e.g. /usr/lib/jni:/usr/lib).
 	 */
 	private File targetLibraryPath;
 
 	private File targetRoot;
 
 	private File targetStarterDir;
 
 	private File targetSysconfDir;
 
 	private File targetWrapperScriptFile;
 
 	/**
 	 * Convenience field that denotes the BUILD directory
 	 */
 	private File baseBuildDir;
 	/**
 	 * Convenience field that denotes the SPECS directory
 	 */
 	private File baseSpecsDir;
 
 	private Log l;
 
 	/**
 	 * Strategy implementation in use by the instance. By default it is the
 	 * Debian one.
 	 */
 	private Strategy strategy = DEB_STRATEGY;
 
 	/**
 	 * Reference to the {@link AbstractPackagingMojo}. This gives access to
 	 * Maven objects but also general configuration parameters of this plugin.
 	 * 
 	 */
 	protected AbstractPackagingMojo apm;
 
 	/**
 	 * Reference to the package map of the distro configured in the target
 	 * configuration.
 	 */
 	protected PackageMap packageMap;
 
 	/**
 	 * Reference to the {@link TargetConfiguration} this helper instance is
 	 * working for.
 	 */
 	protected TargetConfiguration targetConfiguration;
 
 	/**
 	 * Reference to the {@link TargetConfiguration} instances which are denoted
 	 * by the relations property of the main target configuration.
 	 */
 	protected List<TargetConfiguration> resolvedRelations;
 
 	/**
 	 * The distribution which is chosen to be built. This is not handled by
 	 * Maven2 but only by the Packaging class.
 	 */
 	private String chosenDistro;
 
 	private File dstSBinDir;
 
 	private File targetSBinDir;
 
 	public void setChosenDistro(String chosenDistro) {
 		this.chosenDistro = chosenDistro;
 	}
 
 	public String getChosenDistro() {
 		return chosenDistro;
 	}
 
 	/**
 	 * DEB-specific bits of the Helper.
 	 * 
 	 * <P>
 	 * Set a {@link Helper} instance to use this {@link Strategy} implementation
 	 * if the helper is used for Debian packaging.
 	 * </p>
 	 * 
 	 * <p>
 	 * The Deb strategy is the default.
 	 * </p>
 	 */
 	public static final Strategy DEB_STRATEGY = new Strategy() {
 
 		@Override
 		protected File getDstArtifactFile(Helper instance) {
 			if (instance.dstArtifactFile == null) {
 				instance.dstArtifactFile = new File(instance.getBasePkgDir(),
 						instance.getTargetArtifactFile().toString());
 			}
 			return instance.dstArtifactFile;
 		}
 
 		@Override
 		protected void prepareInitialDirectories(Helper instance)
 				throws MojoExecutionException {
 			instance.prepareDirectories(instance.l, instance.basePkgDir,
 					instance.dstJNIDir);
 		}
 
 		@Override
 		protected String getPackageFileName(Helper instance) {
 
 			StringBuilder packageName = new StringBuilder();
 			packageName.append(getPackageFileNameWithoutExtension(instance));
 			packageName.append(".deb");
 			return packageName.toString();
 
 		}
 
 		@Override
 		protected String getPackageFileNameWithoutExtension(Helper instance) {
 
 			StringBuilder packageName = new StringBuilder();
 			packageName.append(instance.getPackageName().toLowerCase());
 			if (!Utils.checkDebianPackageNameConvention(packageName.toString())) {
 				instance.l
 						.warn("Filename for debian package does not follow the debian naming convention, please check your input");
 			}
 			packageName.append("_");
 			packageName.append(instance.getPackageVersion());
 			if (!Utils.checkDebianPackageVersionConvention(instance
 					.getPackageVersion())) {
 				instance.l
 						.warn("Version for debian package does not follow the debian naming convention, please check your input");
 			}
 			packageName.append("_all");
 			return packageName.toString();
 
 		}
 
 		@Override
 		protected String getArchitecture(Helper instance) {
 			return instance.targetConfiguration.getArchitecture();
 		}
 	};
 
 	/**
 	 * RPM specific bits of the Helper.
 	 * 
 	 * <P>
 	 * Set a {@link Helper} instance to use this {@link Strategy} implementation
 	 * if the helper is used for RPM packaging.
 	 * </p>
 	 */
 	public static final Strategy RPM_STRATEGY = new Strategy() {
 
 		/**
 		 * Uses getBaseBuildDir to determine the file name.
 		 */
 		@Override
 		protected File getDstArtifactFile(Helper instance) {
 			if (instance.dstArtifactFile == null) {
 				instance.dstArtifactFile = new File(instance.getBaseBuildDir(),
 						instance.getTargetArtifactFile().toString());
 			}
 			return instance.dstArtifactFile;
 		}
 
 		/**
 		 * Sets up directories for RPM compliance
 		 */
 		@Override
 		protected void prepareInitialDirectories(Helper instance)
 				throws MojoExecutionException {
 			// Basically what the DEB Strategy does is a common behavior between
 			// both.
 			DEB_STRATEGY.prepareInitialDirectories(instance);
 
 			instance.setBaseBuildDir(new File(instance.basePkgDir, "/BUILD"));
 			instance.setBaseSpecsDir(new File(instance.basePkgDir, "/SPECS"));
 			// Creating folder structure for RPM creation. Older versions of
 			// rpmbuild
 			// do not automatically create the directories as needed
 			try {
 				FileUtils.forceMkdir(new File(instance.basePkgDir, "/RPMS"));
 				FileUtils.forceMkdir(new File(instance.basePkgDir,
 						"/RPMS/x86_64"));
 				FileUtils
 						.forceMkdir(new File(instance.basePkgDir, "/RPMS/i386"));
 			} catch (IOException e) {
 				throw new MojoExecutionException(
 						"Error creating needed folder structure", e);
 			}
 
 		}
 
 		@Override
 		protected String getPackageFileName(Helper instance) {
 			StringBuilder rpmPackageName = new StringBuilder();
 			rpmPackageName.append(getPackageFileNameWithoutExtension(instance));
 			rpmPackageName.append(".rpm");
 			return rpmPackageName.toString();
 		}
 
 		@Override
 		protected String getPackageFileNameWithoutExtension(Helper instance) {
 			StringBuilder rpmPackageName = new StringBuilder();
 			rpmPackageName.append(instance.getPackageName());
 			rpmPackageName.append("-");
 			rpmPackageName.append(instance.getPackageVersion()
 					.replace("-", "_"));
 			if (instance.targetConfiguration.getRevision() != null) {
 				rpmPackageName.append("-");
 				rpmPackageName.append(instance.targetConfiguration
 						.getRevision());
 			} else {
 				// Release version defaults to "1"
 				rpmPackageName.append("-1");
 			}
 			rpmPackageName.append(".");
 			rpmPackageName.append(getArchitecture(instance));
 			return rpmPackageName.toString();
 		}
 
 		@Override
 		protected String getArchitecture(Helper instance) {
 			String arch = instance.targetConfiguration.getArchitecture();
 			// all translates to noarch in RPM
 			if ("all".equals(arch)) {
 				return "noarch";
 			}
 			return arch;
 		}
 	};
 
 	public File getTargetAuxDir() {
 		return targetAuxDir;
 	}
 
 	public void setTargetAuxDir(File targetAuxDir) {
 		this.targetAuxDir = targetAuxDir;
 	}
 
 	public Helper() {
 		// Intentionally empty.
 	}
 
 	public final void init(AbstractPackagingMojo mojo, PackageMap packageMap,
 			TargetConfiguration targetConfiguration,
 			List<TargetConfiguration> resolvedRelations, String chosenDistro) {
 		if (apm != null) {
 			throw new IllegalStateException(
 					"Helper instance is already initialized.");
 		}
 		this.apm = mojo;
 		this.packageMap = packageMap;
 		this.targetConfiguration = targetConfiguration;
 		this.resolvedRelations = resolvedRelations;
 		this.l = apm.getLog();
 		this.chosenDistro = chosenDistro;
 
 	}
 
 	/**
 	 * Copies the given set of artifacts to the location specified by
 	 * {@link #getDstBundledJarDir()}.
 	 * 
 	 * @param artifacts
 	 * @param dst
 	 * @return
 	 * @throws MojoExecutionException
 	 */
 	public long copyArtifacts(Set<Artifact> artifacts)
 			throws MojoExecutionException {
 		return Utils.copyArtifacts(l, artifacts, getDstBundledJarDir());
 	}
 
 	/**
 	 * Copies all kinds of auxialiary files to their respective destination.
 	 * 
 	 * <p>
 	 * The method consults the getSrcAuxFilesDir(), srcSysconfFilesDir,
 	 * srcDatarootFilesDir, srcDataFilesDir, getSrcJNIFilesDir() properties as
 	 * well as their corresponding destination properties for this.
 	 * </p>
 	 * 
 	 * <p>
 	 * The return value is the amount of bytes copied.
 	 * </p>
 	 * 
 	 * @return
 	 * @throws MojoExecutionException
 	 */
 	public long copyFiles() throws MojoExecutionException {
 		long size = 0;
 		size += Utils.copyFiles(l, getSrcAuxFilesDir(), getDstAuxDir(),
 				targetConfiguration.getAuxFiles(), "aux file");
 
 		size += Utils.copyFiles(l, getSrcBinFilesDir(), getDstBinDir(),
 				targetConfiguration.getBinFiles(), "bin file", true);
 
 		size += Utils.copyFiles(l, getSrcSBinFilesDir(), getDstSBinDir(),
 				targetConfiguration.getSBinFiles(), "sbin file", true);
 
 		size += Utils.copyFiles(l, getSrcSysconfFilesDir(), getDstSysconfDir(),
 				targetConfiguration.getSysconfFiles(), "sysconf file");
 
 		size += Utils.copyFiles(l, getSrcDatarootFilesDir(),
 				getDstDatarootDir(), targetConfiguration.getDatarootFiles(),
 				"dataroot file");
 
 		size += Utils.copyFiles(l, getSrcDataFilesDir(), getDstDataDir(),
 				targetConfiguration.getDataFiles(), "data file");
 
 		size += Utils.copyFiles(l, getSrcJNIFilesDir(), getDstJNIDir(),
 				targetConfiguration.getJniFiles(), "JNI library");
 
 		size += Utils.copyFiles(l, getSrcJarFilesDir(), getDstBundledJarDir(),
 				targetConfiguration.getJarFiles(), "jar file");
 
 		return size;
 	}
 
 	/**
 	 * Copies the pre-install, pre-removal, post-install and post-removal
 	 * scripts (if applicable) to their proper location which is given through
 	 * the <code>dstScriptDir</code> property.
 	 * 
 	 * <p>
 	 * To find out the source directory for the scripts the
 	 * <code>srcAuxFilesFir</code> property is consulted.
 	 * </p>
 	 * 
 	 * @throws MojoExecutionException
 	 */
 	public long copyScripts() throws MojoExecutionException {
 		final File dir = getDstScriptDir();
 		long bytesCopied = 0;
 
 		if (targetConfiguration.getPreinstScript() != null) {
 			bytesCopied += writeScript(
 					"pre-install",
 					new File(getSrcAuxFilesDir(), targetConfiguration
 							.getPreinstScript()), new File(dir, "preinst"),
 					this);
 		}
 		if (targetConfiguration.getPrermScript() != null) {
 			bytesCopied += writeScript("pre-remove", new File(
 					getSrcAuxFilesDir(), targetConfiguration.getPrermScript()),
 					new File(dir, "prerm"), this);
 		}
 		if (targetConfiguration.getPostinstScript() != null) {
 			bytesCopied += writeScript(
 					"post-install",
 					new File(getSrcAuxFilesDir(), targetConfiguration
 							.getPostinstScript()), new File(dir, "postinst"),
 					this);
 		}
 		if (targetConfiguration.getPostrmScript() != null) {
 			bytesCopied += writeScript(
 					"post-remove",
 					new File(getSrcAuxFilesDir(), targetConfiguration
 							.getPostrmScript()), new File(dir, "postrm"), this);
 		}
 		return bytesCopied;
 	}
 
 	/**
 	 * Creates a classpath line that consists of all the project' artifacts as
 	 * well as the project's own artifact.
 	 * <p>
 	 * The filename of the project's own artifact is taken from the result of
 	 * {@link #getTargetArtifactFile()}.
 	 * </p>
 	 * <p>
 	 * The method returns a set of artifact instance which will be bundled with
 	 * the package.
 	 * </p>
 	 * 
 	 * @param bcp
 	 * @param cp
 	 * @throws MojoExecutionException
 	 */
 	public void createClasspathLine(Path bcp, Path cp)
 			throws MojoExecutionException {
 		createClasspathLine(l, getTargetBundledJarDir(), bcp, cp,
 				getTargetArtifactFile());
 	}
 
 	/**
 	 * Generates a wrapper script for the application. If the
 	 * <code>windows</code> flag has been set another script is generated for
 	 * that OS.
 	 * 
 	 * <p>
 	 * The method consults the properties <code>targetJNIDir</code>,
 	 * <code>dstWrapperScriptFile</code> and possibly
 	 * <code>dstWrapperScriptFileWindows</code> for its work.
 	 * </p>
 	 * 
 	 * @param bundledArtifacts
 	 * @param bcp
 	 * @param cp
 	 * @param windows
 	 * @throws MojoExecutionException
 	 */
 	public void generateWrapperScript(Path bcp, Path classpath, boolean windows)
 			throws MojoExecutionException {
 		WrapperScriptGenerator gen = new WrapperScriptGenerator();
 		gen.setMaxJavaMemory(targetConfiguration.getMaxJavaMemory());
 
 		gen.setCustomCodeUnix(targetConfiguration.getCustomCodeUnix());
 
 		if (getTargetLibraryPath() != null) {
 			gen.setLibraryPath(new Path(getTargetLibraryPath()));
 		}
 		gen.setProperties(targetConfiguration.getSystemProperties());
 
 		// Set to default Classmap file on Debian/Ubuntu systems.
 		// TODO: make this configurable
 		if (targetConfiguration.isAotCompile()) {
 			gen.setClassmapFile("/var/lib/gcj-4.1/classmap.db");
 		}
 		if (targetConfiguration.isAdvancedStarter()) {
 			l.info("setting up advanced starter");
 			Utils.setupStarter(l, targetConfiguration.getMainClass(),
 					getDstStarterDir(), classpath);
 
 			// Sets main class and classpath for the wrapper script.
 			gen.setMainClass("_Starter");
 			gen.setClasspath(new Path(getTargetStarterDir()));
 		} else {
 			l.info("using traditional starter");
 			gen.setMainClass(targetConfiguration.getMainClass());
 
 			// All Jars have to reside inside the libraryRoot.
 			gen.setClasspath(classpath);
 		}
 
 		Utils.createFile(getDstWrapperScriptFile(), "wrapper script");
 
 		try {
 			gen.generate(getDstWrapperScriptFile());
 		} catch (IOException ioe) {
 			throw new MojoExecutionException(
 					"IOException while generating wrapper script", ioe);
 		}
 
 		if (windows) {
 			Utils.createFile(getDstWindowsWrapperScriptFile(), "windows batch");
 
 			gen.setCustomCodeWindows(targetConfiguration.getCustomCodeWindows());
 
 			try {
 				gen.generate(getDstWindowsWrapperScriptFile());
 			} catch (IOException ioe) {
 				throw new MojoExecutionException(
 						"IOException while generating windows batch file", ioe);
 			}
 		}
 
 		// Make the wrapper script executable.
 		Utils.makeExecutable(getDstWrapperScriptFile(), "wrapper script");
 
 	}
 
 	public String getAotPackageName() {
 		if (aotPackageName == null) {
 			aotPackageName = Utils.gcjise(getArtifactId(),
 					targetConfiguration.getSection(),
 					packageMap.isDebianNaming());
 		}
 		return aotPackageName;
 	}
 
 	public File getAotPkgDir() {
 		if (aotPkgDir == null) {
 			aotPkgDir = new File(apm.getTempRoot(), aotPackageName + "-"
 					+ getPackageVersion());
 		}
 		return aotPkgDir;
 	}
 
 	public String getArtifactId() {
 		return apm.getProject().getArtifactId();
 	}
 
 	public File getBasePkgDir() {
 		if (basePkgDir == null) {
 			basePkgDir = new File(apm.getTempRoot(),
 					targetConfiguration.getTarget() + "/" + getPackageName()
 							+ "-" + getPackageVersion());
 		}
 		return basePkgDir;
 	}
 
 	public File getDstAuxDir() {
 		return dstAuxDir;
 	}
 
 	public File getDstBinDir() {
 		if (dstBinDir == null) {
 			dstBinDir = new File(getBasePkgDir(), getTargetBinDir().toString());
 		}
 		return dstBinDir;
 	}
 
 	public File getDstSBinDir() {
 		if (dstSBinDir == null) {
 			dstSBinDir = new File(getBasePkgDir(), getTargetSBinDir()
 					.toString());
 		}
 		return dstSBinDir;
 	}
 
 	public File getDstBundledJarDir() {
 		if (dstBundledJarDir == null) {
 			dstBundledJarDir = new File(basePkgDir, getTargetBundledJarDir()
 					.toString());
 		}
 		return dstBundledJarDir;
 	}
 
 	public File getDstDataDir() {
 		if (dstDataDir == null) {
 			dstDataDir = new File(getBasePkgDir(), getTargetDataDir()
 					.toString());
 		}
 		return dstDataDir;
 	}
 
 	public File getDstDatarootDir() {
 		if (dstDatarootDir == null) {
 			dstDatarootDir = new File(getBasePkgDir(), getTargetDatarootDir()
 					.toString());
 		}
 		return dstDatarootDir;
 	}
 
 	public File getDstJNIDir() {
 		if (dstJNIDir == null) {
 			dstJNIDir = new File(getBasePkgDir(), getTargetJNIDir().toString());
 		}
 		return dstJNIDir;
 	}
 
 	public File getDstRoot() {
 		if (dstRoot == null) {
 			dstRoot = new File(getBasePkgDir(), getTargetRoot().toString());
 		}
 		return dstRoot;
 	}
 
 	public File getDstScriptDir() {
 		if (dstScriptDir == null) {
 			throw new UnsupportedOperationException(
 					"This dstScriptDir property has to be provided explicitly in advance!");
 		}
 		return dstScriptDir;
 	}
 
 	public File getDstStarterDir() {
 		if (dstStarterDir == null) {
 			dstStarterDir = new File(getBasePkgDir(), getTargetStarterDir()
 					.toString());
 		}
 		return dstStarterDir;
 	}
 
 	public File getDstSysconfDir() {
 		if (dstSysconfDir == null) {
 			dstSysconfDir = new File(getBasePkgDir(), getTargetSysconfDir()
 					.toString());
 		}
 		return dstSysconfDir;
 	}
 
 	public File getDstWindowsWrapperScriptFile() {
 		if (dstWindowsWrapperScriptFile == null) {
 			dstWindowsWrapperScriptFile = new File(getDstWrapperScriptFile()
 					.getAbsolutePath() + ".bat");
 		}
 		return dstWindowsWrapperScriptFile;
 	}
 
 	public File getDstWrapperScriptFile() {
 		if (dstWrapperScriptFile == null) {
 			// Use the provided wrapper script name or the default.
 			dstWrapperScriptFile = new File(getBasePkgDir(),
 					getTargetWrapperScriptFile().toString());
 		}
 		return dstWrapperScriptFile;
 	}
 
 	public String getJavaExec() {
 		return apm.getJavaExec();
 	}
 
 	public String get7ZipExec() {
 		return apm.get_7zipExec();
 	}
 
 	public File getOutputDirectory() {
 		return apm.getOutputDirectory();
 	}
 
 	public String getPackageName() {
 		if (packageName == null) {
 			packageName = Utils.createPackageName(apm.getProject()
 					.getArtifactId(), targetConfiguration
 					.getPackageNameSuffix(), targetConfiguration.getSection(),
 					packageMap.isDebianNaming());
 		}
 		return packageName;
 	}
 
 	public String getPackageVersion() {
 		if (packageVersion == null) {
 			String versionSuffix = (targetConfiguration
 					.getPackageVersionSuffix() == null ? "" : "-0"
 					+ targetConfiguration.getPackageVersionSuffix());
 			String revisionSuffix = (targetConfiguration.getRevision() == null ? ""
 					: "-" + targetConfiguration.getRevision());
 			packageVersion = apm.getProject().getVersion() + versionSuffix
 					+ revisionSuffix;
 		}
 		return packageVersion;
 	}
 
 	public String getProjectDescription() {
 		return apm.getProject().getDescription();
 	}
 
 	public String getProjectUrl() {
 		return apm.getProject().getUrl();
 	}
 
 	public File getSrcArtifactFile() {
 		if (srcArtifactFile == null) {
 			srcArtifactFile = new File(apm.getOutputDirectory().getPath(),
 					apm.getFinalName() + "." + apm.getProject().getPackaging());
 		}
 		return srcArtifactFile;
 	}
 
 	public File getSrcAuxFilesDir() {
 		return (targetConfiguration.getSrcAuxFilesDir().length() == 0) ? new File(
 				apm.getProject().getBasedir(),
 				Packaging.getDefaultSrcAuxfilesdir()) : new File(apm
 				.getProject().getBasedir(),
 				targetConfiguration.getSrcAuxFilesDir());
 	}
 
 	public File getSrcDataFilesDir() {
 		return (targetConfiguration.getSrcDataFilesDir().length() == 0) ? getSrcAuxFilesDir()
 				: new File(apm.getProject().getBasedir(),
 						targetConfiguration.getSrcDataFilesDir());
 	}
 
 	public File getSrcDatarootFilesDir() {
 		return (targetConfiguration.getSrcDatarootFilesDir().length() == 0) ? getSrcAuxFilesDir()
 				: new File(apm.getProject().getBasedir(),
 						targetConfiguration.getSrcDatarootFilesDir());
 	}
 
 	/**
 	 * Returns the directory containing the izpack helper files. If not
 	 * specified otherwise this directory is identical to the source directory
 	 * of the aux files.
 	 * 
 	 * @return
 	 */
 	public File getSrcIzPackFilesDir() {
 		return (targetConfiguration.getSrcIzPackFilesDir().length() == 0 ? getSrcAuxFilesDir()
 				: new File(apm.getProject().getBasedir(),
 						targetConfiguration.getSrcIzPackFilesDir()));
 	}
 
 	public File getSrcJarFilesDir() {
 		return (targetConfiguration.getSrcJarFilesDir().length() == 0) ? getSrcAuxFilesDir()
 				: new File(apm.getProject().getBasedir(),
 						targetConfiguration.getSrcJarFilesDir());
 	}
 
 	public File getSrcJNIFilesDir() {
 		return (targetConfiguration.getSrcJNIFilesDir().length() == 0) ? getSrcAuxFilesDir()
 				: new File(apm.getProject().getBasedir(),
 						targetConfiguration.getSrcJNIFilesDir());
 	}
 
 	public File getSrcSysconfFilesDir() {
 		return (targetConfiguration.getSrcSysconfFilesDir().length() == 0) ? getSrcAuxFilesDir()
 				: new File(apm.getProject().getBasedir(),
 						targetConfiguration.getSrcSysconfFilesDir());
 	}
 
 	public File getSrcBinFilesDir() {
 		return (targetConfiguration.getSrcBinFilesDir().length() == 0) ? getSrcAuxFilesDir()
 				: new File(apm.getProject().getBasedir(),
 						targetConfiguration.getSrcBinFilesDir());
 	}
 
 	public File getSrcSBinFilesDir() {
 		return (targetConfiguration.getSrcSBinFilesDir().length() == 0) ? getSrcAuxFilesDir()
 				: new File(apm.getProject().getBasedir(),
 						targetConfiguration.getSrcSBinFilesDir());
 	}
 
 	public File getTargetArtifactFile() {
 		if (targetArtifactFile == null) {
 			targetArtifactFile = new File((targetConfiguration.isBundleAll()
 					|| packageMap.hasNoPackages() ? getTargetBundledJarDir()
 					: new File(packageMap.getDefaultJarPath())), apm
 					.getProject().getArtifactId()
 					+ "."
 					+ apm.getProject().getPackaging());
 		}
 		return targetArtifactFile;
 	}
 
 	/**
 	 * Returns the location of the user-level binaries on the target device.
 	 * <p>
 	 * If {@link #setTargetBinDir(File)} has not been called to set this value a
 	 * default value is generated as follows:
 	 * <ul>
 	 * <li>if the distro config defined a non-zero length bindir that one is
 	 * used</li>
 	 * <li>otherwise the distro's default bindir prepended by the prefix of the
 	 * distro configuration is used</li>
 	 * </ul>
 	 * </p>
 	 * <p>
 	 * Therefore the <code>targetBinDir</code> property is dependent on the
 	 * <code>targetRoot</code> property. Check the details for
 	 * {@link #getTargetRoot()}.
 	 * </p>
 	 * 
 	 * @return
 	 */
 	public File getTargetBinDir() {
 		if (targetBinDir == null) {
 			targetBinDir = (targetConfiguration.getBindir().length() == 0 ? new File(
 					getTargetRoot(), packageMap.getDefaultBinPath())
 					: new File(targetConfiguration.getBindir()));
 		}
 		return targetBinDir;
 	}
 
 	/**
 	 * Returns the location of the user-level binaries on the target device.
 	 * <p>
 	 * If {@link #setTargetBinDir(File)} has not been called to set this value a
 	 * default value is generated as follows:
 	 * <ul>
 	 * <li>if the distro config defined a non-zero length bindir that one is
 	 * used</li>
 	 * <li>otherwise the distro's default bindir prepended by the prefix of the
 	 * distro configuration is used</li>
 	 * </ul>
 	 * </p>
 	 * <p>
 	 * Therefore the <code>targetBinDir</code> property is dependent on the
 	 * <code>targetRoot</code> property. Check the details for
 	 * {@link #getTargetRoot()}.
 	 * </p>
 	 * 
 	 * @return
 	 */
 	public File getTargetSBinDir() {
 		if (targetSBinDir == null) {
 			targetSBinDir = (targetConfiguration.getSBindir().length() == 0 ? new File(
 					getTargetRoot(), packageMap.getDefaultSBinPath())
 					: new File(targetConfiguration.getSBindir()));
 		}
 		return targetSBinDir;
 	}
 
 	public File getTargetBundledJarDir() {
 		if (targetBundledJarDir == null) {
 			targetBundledJarDir = (targetConfiguration.getBundledJarDir()
 					.length() == 0 ? new File(getTargetRoot(), new File(
 					packageMap.getDefaultJarPath(), apm.getProject()
 							.getArtifactId()).toString()) : new File(
 					targetConfiguration.getBundledJarDir()));
 		}
 		return targetBundledJarDir;
 	}
 
 	public File getTargetDataDir() {
 		if (targetDataDir == null) {
 			targetDataDir = (targetConfiguration.getDatadir().length() == 0 ? new File(
 					getTargetDatarootDir(), apm.getProject().getName())
 					: new File(targetConfiguration.getDatadir()));
 		}
 		return targetDataDir;
 	}
 
 	public File getTargetDatarootDir() {
 		if (targetDatarootDir == null) {
 			targetDatarootDir = (targetConfiguration.getDatarootdir().length() == 0 ? new File(
 					getTargetRoot(), "usr/share") : new File(
 					targetConfiguration.getDatarootdir()));
 		}
 		return targetDatarootDir;
 	}
 
 	/**
 	 * Returns the directory to which the JNI-files should be copied. Consists
 	 * of the target-root-directory and the _first_ directory specified in the
 	 * defaultJNIPath-configuration-parameter. Example: If the defaultJNIPath is
 	 * "/usr/lib/jni:/usr/lib" the target-jni-directory is "/usr/lib/jni".
 	 * 
 	 * @return
 	 */
 	public File getTargetJNIDir() {
 		if (targetJNIDir == null) {
 			targetJNIDir = new File(getTargetRoot(), packageMap
 					.getDefaultJNIPath().split(":")[0]);
 		}
 		return targetJNIDir;
 	}
 
 	public File getTargetLibraryPath() {
 		if (targetLibraryPath == null) {
 			targetLibraryPath = new File(getTargetRoot(),
 					packageMap.getDefaultJNIPath());
 		}
 		return targetLibraryPath;
 	}
 
 	public File getTargetRoot() {
 		if (targetRoot == null) {
 			targetRoot = new File(targetConfiguration.getPrefix());
 		}
 		return targetRoot;
 	}
 
 	public File getTargetStarterDir() {
 		if (targetStarterDir == null) {
 			targetStarterDir = new File(getTargetBundledJarDir(), "_starter");
 		}
 		return targetStarterDir;
 	}
 
 	public File getTargetSysconfDir() {
 		if (targetSysconfDir == null) {
 			targetSysconfDir = (targetConfiguration.getSysconfdir().length() == 0 ? new File(
 					getTargetRoot(), "etc") : new File(
 					targetConfiguration.getSysconfdir()));
 		}
 		return targetSysconfDir;
 	}
 
 	public File getTargetWrapperScriptFile() {
 		if (targetWrapperScriptFile == null) {
 			targetWrapperScriptFile = new File(
 					getTargetBinDir(),
 					(targetConfiguration.getWrapperScriptName() != null ? targetConfiguration
 							.getWrapperScriptName() : apm.getProject()
 							.getArtifactId()));
 		}
 		return targetWrapperScriptFile;
 	}
 
 	/*
 	 * public File getTempRoot() { if (tempRoot == null){ tempRoot = new
 	 * File(apm.getBuildDir(), packageMap.getPackaging() + "-tmp"); } return
 	 * tempRoot; }
 	 */
 	public void prepareAotDirectories() throws MojoExecutionException {
 		prepareDirectories(l, aotPkgDir, null);
 	}
 
 	public void setAotPackageName(String aotPackageName) {
 		this.aotPackageName = aotPackageName;
 	}
 
 	public void setAotPkgDir(File aotPkgDir) {
 		this.aotPkgDir = aotPkgDir;
 	}
 
 	public void setBasePkgDir(File basePkgDir) {
 		this.basePkgDir = basePkgDir;
 	}
 
 	public void setDstArtifactFile(File dstArtifactFile) {
 		this.dstArtifactFile = dstArtifactFile;
 	}
 
 	public void setDstAuxDir(File dstAuxFileDir) {
 		this.dstAuxDir = dstAuxFileDir;
 	}
 
 	public void setDstBinDir(File dstBinDir) {
 		this.dstBinDir = dstBinDir;
 	}
 
 	public void setDstSBinDir(File dstSBinDir) {
 		this.dstSBinDir = dstSBinDir;
 	}
 
 	public void setDstBundledJarDir(File dstBundledArtifactsDir) {
 		this.dstBundledJarDir = dstBundledArtifactsDir;
 	}
 
 	public void setDstDataDir(File dstDataDir) {
 		this.dstDataDir = dstDataDir;
 	}
 
 	public void setDstDatarootDir(File dstDatarootDir) {
 		this.dstDatarootDir = dstDatarootDir;
 	}
 
 	public void setDstJNIDir(File dstJNIDir) {
 		this.dstJNIDir = dstJNIDir;
 	}
 
 	public void setDstRoot(File dstRoot) {
 
 		this.dstRoot = dstRoot;
 	}
 
 	public void setDstScriptDir(File dstScriptDir) {
 		this.dstScriptDir = dstScriptDir;
 	}
 
 	public void setDstStarterDir(File dstStarterDir) {
 		this.dstStarterDir = dstStarterDir;
 	}
 
 	public void setDstSysconfDir(File dstSysconfDir) {
 		this.dstSysconfDir = dstSysconfDir;
 	}
 
 	public void setDstWindowsWrapperScriptFile(File windowsWrapperScriptFile) {
 		this.dstWindowsWrapperScriptFile = windowsWrapperScriptFile;
 	}
 
 	public void setDstWrapperScriptFile(File wrapperScriptFile) {
 		this.dstWrapperScriptFile = wrapperScriptFile;
 	}
 
 	public void setPackageName(String packageName) {
 		this.packageName = packageName;
 	}
 
 	public void setPackageVersion(String packageVersion) {
 		this.packageVersion = packageVersion;
 	}
 
 	public void setTargetArtifactFile(File targetArtifactFile) {
 		this.targetArtifactFile = targetArtifactFile;
 	}
 
 	public void setTargetBinDir(File targetBinDir) {
 		this.targetBinDir = targetBinDir;
 	}
 
 	public void setTargetBundledJarDir(File targetJarDir) {
 		this.targetBundledJarDir = targetJarDir;
 	}
 
 	public void setTargetDataDir(File targetDataDir) {
 		this.targetDataDir = targetDataDir;
 	}
 
 	public void setTargetDatarootDir(File targetDatarootDir) {
 		this.targetDatarootDir = targetDatarootDir;
 	}
 
 	public void setTargetJNIDir(File targetJNIDir) {
 		this.targetJNIDir = targetJNIDir;
 	}
 
 	public void setTargetRoot(File targetRoot) {
 		this.targetRoot = targetRoot;
 	}
 
 	public void setTargetStarterDir(File targetStarterDir) {
 		this.targetStarterDir = targetStarterDir;
 	}
 
 	public void setTargetSysconfDir(File targetSysconfDir) {
 		this.targetSysconfDir = targetSysconfDir;
 	}
 
 	public void setTargetWrapperScriptFile(File targetWrapperScriptFile) {
 		this.targetWrapperScriptFile = targetWrapperScriptFile;
 	}
 
 	final void prepareDirectories(Log l, File basePkgDir, File jniDir)
 			throws MojoExecutionException {
 
 		if (l != null) {
 			l.info("creating package directory: "
 					+ basePkgDir.getAbsolutePath());
 		}
 		if (!basePkgDir.mkdirs()) {
 			throw new MojoExecutionException(
 					"Could not create package directory.");
 		}
 		if (jniDir != null && targetConfiguration.getJniFiles() != null
 				&& targetConfiguration.getJniFiles().size() > 0
 				&& !jniDir.mkdirs()) {
 			throw new MojoExecutionException("Could not create JNI directory.");
 		}
 
 	}
 
 	protected final long writeScript(String item, File srcScriptFile,
 			File dstScriptFile, Helper ph) throws MojoExecutionException {
 		Utils.createFile(dstScriptFile, item + " file");
 		// Write a #/bin/sh header
 
 		Utils.makeExecutable(dstScriptFile, item + " file");
 		PrintWriter writer = null;
 		try {
 			writer = new PrintWriter(new BufferedWriter(new FileWriter(
 					dstScriptFile)));
 			writer.println("#!/bin/sh");
 			writer.println("# This script is partly autogenerated by the "
 					+ getClass().getName() + " class");
			writer.println("# of the maven-pkg-plugin. The autogenerated part adds some variables of the packaging");
 			writer.println("# to the top of the script which can be used in the lower manual part.");
 			writer.println();
 			writer.println("prefix=\"" + ph.getTargetRoot() + "\"");
 			writer.println("bindir=\"" + ph.getTargetBinDir() + "\"");
 			writer.println("datadir=\"" + ph.getTargetDataDir() + "\"");
 			writer.println("datarootdir=\"" + ph.getTargetDatarootDir() + "\"");
 			writer.println("sysconfdir=\"" + ph.getTargetSysconfDir() + "\"");
 			writer.println("jnidir=\"" + ph.getTargetJNIDir() + "\"");
 			writer.println("bundledjardir=\"" + ph.getTargetBundledJarDir()
 					+ "\"");
 			writer.println("wrapperscriptfile=\""
 					+ ph.getTargetWrapperScriptFile() + "\"");
 			writer.println("version=\"" + ph.getPackageVersion() + "\"");
 			writer.println("name=\"" + ph.getPackageName() + "\"");
 			writer.println("mainClass=\"" + targetConfiguration.getMainClass()
 					+ "\"");
 			writer.println("scriptType=\"" + item + "\"");
 			writer.println();
 			writer.println("distro=\"" + getChosenDistro() + "\"");
 			writer.println("distroLabel=\"" + packageMap.getDistroLabel()
 					+ "\"");
 			writer.println("packaging=\"" + packageMap.getPackaging() + "\"");
 			writer.println();
 			writer.println("# What follows is the content script file "
 					+ srcScriptFile.getName());
 			writer.println();
 
 			// Now append the real script
 			IOUtils.copy(new FileInputStream(srcScriptFile), writer);
 
 			return dstScriptFile.length();
 		} catch (IOException ioe) {
 			throw new MojoExecutionException(
 					"IO error while writing the script file " + dstScriptFile,
 					ioe);
 		} finally {
 			if (writer != null) {
 				writer.close();
 			}
 		}
 	}
 
 	/**
 	 * Creates the bootclasspath and classpath line from the project's
 	 * dependencies.
 	 * 
 	 * @param pm
 	 *            The package map used to resolve the Jar file names.
 	 * @param bundled
 	 *            A set used to track the bundled jars for later file-size
 	 *            calculations.
 	 * @param bcp
 	 *            StringBuilder which contains the boot classpath line at the
 	 *            end of the method.
 	 * @param cp
 	 *            StringBuilder which contains the classpath line at the end of
 	 *            the method.
 	 */
 	protected final void createClasspathLine(final Log l,
 			final File targetJarPath, final Path bcp, final Path cp,
 			File targetArtifactFile) throws MojoExecutionException {
 		// final Set<Artifact> bundled = new HashSet<Artifact>();
 
 		l.info("resolving dependency artifacts");
 
 		Set<Artifact> dependencies = new HashSet<Artifact>();
 		try {
 			// Notice only compilation dependencies which are Jars.
 			// Shared Libraries ("so") are filtered out because the
 			// JNI dependency is solved by the system already.
 
 			// Here a filter for depencies of the COMPILE scope is created
 			AndArtifactFilter compileFilter = new AndArtifactFilter();
 			compileFilter.add(new ScopeArtifactFilter(Artifact.SCOPE_COMPILE));
 			compileFilter.add(new TypeArtifactFilter("jar"));
 
 			// The result of the COMPILE filter will be added to the depencies
 			// set
 			dependencies.addAll(Utils.findArtifacts(compileFilter, apm
 					.getFactory(), apm.getResolver(), apm.getProject(), apm
 					.getProject().getArtifact(), apm.getLocalRepo(), apm
 					.getRemoteRepos(), apm.getMetadataSource()));
 
 			// Here a filter for depencies of the RUNTIME scope is created
 			AndArtifactFilter runtimeFilter = new AndArtifactFilter();
 			runtimeFilter.add(new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME));
 			runtimeFilter.add(new TypeArtifactFilter("jar"));
 
 			// The result of the RUNTIME filter will be added to the depencies
 			// set
 			dependencies.addAll(Utils.findArtifacts(runtimeFilter, apm
 					.getFactory(), apm.getResolver(), apm.getProject(), apm
 					.getProject().getArtifact(), apm.getLocalRepo(), apm
 					.getRemoteRepos(), apm.getMetadataSource()));
 
 			// Here a filter for depencies of the PROVIDED scope is created
 			AndArtifactFilter providedFilter = new AndArtifactFilter();
 			providedFilter
 					.add(new ScopeArtifactFilter(Artifact.SCOPE_PROVIDED));
 			providedFilter.add(new TypeArtifactFilter("jar"));
 
 			// The result of the PROVIDED filter will be added to the depencies
 			// set
 			dependencies.addAll(Utils.findArtifacts(providedFilter, apm
 					.getFactory(), apm.getResolver(), apm.getProject(), apm
 					.getProject().getArtifact(), apm.getLocalRepo(), apm
 					.getRemoteRepos(), apm.getMetadataSource()));
 
 		} catch (ArtifactNotFoundException anfe) {
 			throw new MojoExecutionException(
 					"Exception while resolving dependencies", anfe);
 		} catch (InvalidDependencyVersionException idve) {
 			throw new MojoExecutionException(
 					"Exception while resolving dependencies", idve);
 		} catch (ProjectBuildingException pbe) {
 			throw new MojoExecutionException(
 					"Exception while resolving dependencies", pbe);
 		} catch (ArtifactResolutionException are) {
 			throw new MojoExecutionException(
 					"Exception while resolving dependencies", are);
 		}
 
 		Visitor v = new Visitor() {
 			public void bundle(Artifact artifact) {
 				// Nothing to do here. bundleDependencies should take care of
 				// this.
 
 			}
 
 			public void visit(Artifact artifact, Entry entry) {
 				// If all dependencies should be bundled skip adding them to the
 				// classpath
 				// thereby overriding what was configured through property
 				// files.
 				if (targetConfiguration.isBundleAll()) {
 					return;
 				}
 
 				Path b = (entry.isBootClasspath) ? bcp : cp;
 
 				Iterator<String> ite = entry.jarFileNames.iterator();
 				while (ite.hasNext()) {
 					StringBuilder sb = new StringBuilder();
 					String fileName = ite.next();
 
 					// Prepend default Jar path if file is not absolute.
 					if (fileName.charAt(0) != '/') {
 						sb.append(packageMap.getDefaultJarPath());
 						sb.append("/");
 					}
 
 					sb.append(fileName);
 
 					b.append(sb.toString());
 				}
 			}
 
 		};
 
 		packageMap.iterateDependencyArtifacts(l, dependencies, v, true);
 
 		// Add the custom jar files to the classpath
 		for (Iterator<JarFile> ite = targetConfiguration.getJarFiles()
 				.iterator(); ite.hasNext();) {
 			AuxFile auxFile = ite.next();
 
 			cp.append(targetJarPath.toString() + "/"
 					+ new File(auxFile.getFrom()).getName());
 		}
 
 		// Add the project's own artifact at last. This way we can
 		// save the deletion of the colon added in the loops above.
 		cp.append(targetArtifactFile.toString());
 
 		// return bundled;
 	}
 
 	/**
 	 * Prepares the dependencies to be copied into the package.
 	 * 
 	 * @param bcp
 	 * @param cp
 	 * @return
 	 * @throws MojoExecutionException
 	 */
 	public Set<Artifact> bundleDependencies(Set<Artifact> resolvedDependencies,
 			Path bcp, Path cp) throws MojoExecutionException {
 		return bundleDependencies(l, resolvedDependencies,
 				getTargetBundledJarDir(), bcp, cp, getTargetArtifactFile());
 	}
 
 	/**
 	 * Investigates the project's runtime dependencies and prepares them to be
 	 * copied into the package.
 	 * 
 	 * @param l
 	 * @param targetJarPath
 	 * @param bcp
 	 * @param cp
 	 * @param targetArtifactFile
 	 * @return
 	 * @throws MojoExecutionException
 	 */
 	protected final Set<Artifact> bundleDependencies(final Log l,
 			final Set<Artifact> resolvedDependencies, final File targetJarPath,
 			final Path bcp, final Path cp, File targetArtifactFile)
 			throws MojoExecutionException {
 
 		final Set<Artifact> bundled = new HashSet<Artifact>();
 
 		l.info("Copying dependencies into package");
 
 		Visitor v = new Visitor() {
 			public void bundle(Artifact artifact) {
 				// Put to artifacts which will be bundled (allows copying and
 				// filesize
 				// summing later).
 				bundled.add(artifact);
 
 				// TODO: Perhaps one want a certain bundled dependency in boot
 				// classpath.
 
 				// Bundled Jars will always live in targetJarPath
 				File file = artifact.getFile();
 				if (file != null) {
 					cp.append(targetJarPath.toString() + "/" + file.getName());
 				} else {
 					l.warn("Cannot bundle artifact " + artifact.getArtifactId());
 				}
 			}
 
 			public void visit(Artifact artifact, Entry entry) {
 
 				/**
 				 * Only if we wish to bundle dependency artifacts we will do so
 				 */
 				if (targetConfiguration.isBundleDependencyArtifacts()) {
 
 					// If all dependencies should be bundled take a short-cut to
 					// bundle()
 					// thereby overriding what was configured through property
 					// files.
 					if (targetConfiguration.isBundleAll()) {
 						bundle(artifact);
 						return;
 					}
 
 					Path b = (entry.isBootClasspath) ? bcp : cp;
 
 					Iterator<String> ite = entry.jarFileNames.iterator();
 
 					while (ite.hasNext()) {
 						StringBuilder sb = new StringBuilder();
 						String fileName = ite.next();
 
 						// Prepend default Jar path if file is not absolute.
 						if (fileName.charAt(0) != '/') {
 							sb.append(packageMap.getDefaultJarPath());
 							sb.append("/");
 						}
 
 						sb.append(fileName);
 
 						b.append(sb.toString());
 					}
 				}
 			}
 
 		};
 
 		packageMap.iterateDependencyArtifacts(l, resolvedDependencies, v, true);
 
 		// Add the custom jar files to the classpath
 		for (Iterator<JarFile> ite = targetConfiguration.getJarFiles()
 				.iterator(); ite.hasNext();) {
 			AuxFile auxFile = ite.next();
 
 			cp.append(targetJarPath.toString() + "/"
 					+ new File(auxFile.getFrom()).getName());
 		}
 
 		// Add the project's own artifact at last. This way we can
 		// save the deletion of the colon added in the loops above.
 		cp.append(targetArtifactFile.toString());
 
 		return bundled;
 	}
 
 	/**
 	 * Creates the "Conflicts"-line for the package control file
 	 * 
 	 * @return conflicts line
 	 * @throws MojoExecutionException
 	 */
 	public final String createConflictsLine() throws MojoExecutionException {
 		return createPackageLine(targetConfiguration.getConflicts());
 	}
 
 	public Set<Artifact> resolveProjectDependencies()
 			throws MojoExecutionException {
 		Set<Artifact> resolvedDeps = new HashSet<Artifact>();
 		try {
 			// Notice only compilation dependencies which are Jars.
 			// Shared Libraries ("so") are filtered out because the
 			// JNI dependency is solved by the system already.
 
 			// Here a filter for depencies of the COMPILE scope is created
 			AndArtifactFilter compileFilter = new AndArtifactFilter();
 			compileFilter.add(new ScopeArtifactFilter(Artifact.SCOPE_COMPILE));
 			compileFilter.add(new TypeArtifactFilter("jar"));
 
 			// The result of the COMPILE filter will be added to the depencies
 			// set
 			resolvedDeps.addAll(Utils.findArtifacts(compileFilter, apm
 					.getFactory(), apm.getResolver(), apm.getProject(), apm
 					.getProject().getArtifact(), apm.getLocalRepo(), apm
 					.getRemoteRepos(), apm.getMetadataSource()));
 
 			// Here a filter for depencies of the RUNTIME scope is created
 			AndArtifactFilter runtimeFilter = new AndArtifactFilter();
 			runtimeFilter.add(new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME));
 			runtimeFilter.add(new TypeArtifactFilter("jar"));
 
 			// The result of the RUNTIME filter will be added to the depencies
 			// set
 			resolvedDeps.addAll(Utils.findArtifacts(runtimeFilter, apm
 					.getFactory(), apm.getResolver(), apm.getProject(), apm
 					.getProject().getArtifact(), apm.getLocalRepo(), apm
 					.getRemoteRepos(), apm.getMetadataSource()));
 
 			// Here a filter for depencies of the PROVIDED scope is created
 			AndArtifactFilter providedFilter = new AndArtifactFilter();
 			providedFilter
 					.add(new ScopeArtifactFilter(Artifact.SCOPE_PROVIDED));
 			providedFilter.add(new TypeArtifactFilter("jar"));
 
 			// The result of the PROVIDED filter will be added to the depencies
 			// set
 			resolvedDeps.addAll(Utils.findArtifacts(providedFilter, apm
 					.getFactory(), apm.getResolver(), apm.getProject(), apm
 					.getProject().getArtifact(), apm.getLocalRepo(), apm
 					.getRemoteRepos(), apm.getMetadataSource()));
 		} catch (ArtifactNotFoundException anfe) {
 			throw new MojoExecutionException(
 					"Exception while resolving dependencies", anfe);
 		} catch (InvalidDependencyVersionException idve) {
 			throw new MojoExecutionException(
 					"Exception while resolving dependencies", idve);
 		} catch (ProjectBuildingException pbe) {
 			throw new MojoExecutionException(
 					"Exception while resolving dependencies", pbe);
 		} catch (ArtifactResolutionException are) {
 			throw new MojoExecutionException(
 					"Exception while resolving dependencies", are);
 		}
 
 		return resolvedDeps;
 	}
 
 	/**
 	 * Investigates the project's runtime dependencies and creates a dependency
 	 * line suitable for the control file from them.
 	 * 
 	 * @return
 	 */
 	public final String createDependencyLine(Set<Artifact> resolvedDependencies)
 			throws MojoExecutionException {
 		String defaults = packageMap.getDefaultDependencyLine();
 		StringBuffer manualDeps = new StringBuffer();
 		Iterator<String> ite = targetConfiguration.getManualDependencies()
 				.iterator();
 		while (ite.hasNext()) {
 			String dep = ite.next();
 
 			manualDeps.append(dep);
 			manualDeps.append(", ");
 		}
 
 		// Handle dependencies to other targetconfigurations which create binary
 		// packages.
 		List<String> relatedPackageNames = Utils.createPackageNames(apm
 				.getProject().getArtifactId(), resolvedRelations, packageMap
 				.isDebianNaming());
 
 		ite = relatedPackageNames.iterator();
 		while (ite.hasNext()) {
 			String tcName = ite.next();
 			manualDeps.append(tcName);
 			manualDeps.append(", ");
 		}
 
 		if (manualDeps.length() >= 2) {
 			manualDeps.delete(manualDeps.length() - 2, manualDeps.length());
 		}
 		// If all dependencies should be bundled the package will only
 		// need the default Java dependencies of the system and the remainder
 		// of the method can be skipped.
 		if (targetConfiguration.isBundleAll()) {
 			return Utils.joinDependencyLines(defaults, manualDeps.toString());
 		}
 
 		final StringBuilder line = new StringBuilder();
 
 		// Add default system dependencies for Java packages.
 		line.append(defaults);
 
 		// Visitor implementation which creates the dependency line.
 		Visitor v = new Visitor() {
 			Set<String> processedDeps = new HashSet<String>();
 
 			public void bundle(Artifact _) {
 				// Nothing to do here. bundleDependencies should take care of
 				// this.
 			}
 
 			public void visit(Artifact artifact, Entry entry) {
 				// Certain Maven Packages have only one package in the target
 				// system.
 				// If that one was already added we should not add it any more.
 				if (processedDeps.contains(entry.dependencyLine)) {
 					return;
 				}
 				if (entry.dependencyLine.length() == 0) {
 					l.warn("Invalid package name for artifact: "
 							+ entry.artifactSpec);
 				}
 				line.append(", ");
 				line.append(entry.dependencyLine);
 
 				// Mark as included dependency.
 				processedDeps.add(entry.dependencyLine);
 			}
 		};
 
 		packageMap.iterateDependencyArtifacts(l, resolvedDependencies, v, true);
 
 		return Utils
 				.joinDependencyLines(line.toString(), manualDeps.toString());
 	}
 
 	protected final String createPackageLine(List<String> packageDescriptors) {
 		if (packageDescriptors == null || packageDescriptors.isEmpty()) {
 			return null;
 		}
 		StringBuffer packageLine = new StringBuffer();
 		Iterator<String> ite = packageDescriptors.iterator();
 		while (ite.hasNext()) {
 			String packageDescriptor = ite.next();
 
 			packageLine.append(packageDescriptor);
 			packageLine.append(", ");
 		}
 
 		// Remove last ", "
 		if (packageLine.length() >= 2) {
 			packageLine.delete(packageLine.length() - 2, packageLine.length());
 		}
 		return packageLine.toString();
 	}
 
 	/**
 	 * Creates the "Provides"-line for the package control file
 	 * 
 	 * @return provides line
 	 * @throws MojoExecutionException
 	 */
 	public final String createProvidesLine() throws MojoExecutionException {
 		return createPackageLine(targetConfiguration.getProvides());
 	}
 
 	/**
 	 * Creates the "Recommends"-line for the package control file
 	 * 
 	 * @return recommends line
 	 * @throws MojoExecutionException
 	 */
 	public final String createRecommendsLine() throws MojoExecutionException {
 		return createPackageLine(targetConfiguration.getRecommends());
 	}
 
 	/**
 	 * Creates the "Replaces"-line for the package control file
 	 * 
 	 * @return suggests line
 	 * @throws MojoExecutionException
 	 */
 	public final String createReplacesLine() throws MojoExecutionException {
 		return createPackageLine(targetConfiguration.getReplaces());
 	}
 
 	/**
 	 * Creates the "Suggests"-line for the package control file
 	 * 
 	 * @return suggests line
 	 * @throws MojoExecutionException
 	 */
 	public final String createSuggestsLine() throws MojoExecutionException {
 		return createPackageLine(targetConfiguration.getSuggests());
 	}
 
 	/**
 	 * Checks if the packaging type of the current Maven project belongs to the
 	 * packaging types that, when used, woint contain the main artifact for the
 	 * project (if any) in the final package.
 	 * 
 	 * @return
 	 */
 
 	protected final boolean packagingTypeBelongsToIgnoreList() {
 		boolean inList = false;
 
 		if (l != null) {
 			l.info("ignorePackagingTypes set. Contains: "
 					+ apm.getIgnorePackagingTypes()
 					+ " . Project packaging is "
 					+ apm.getProject().getPackaging());
 		}
 		for (String s : apm.getIgnorePackagingTypes().split(",")) {
 			if (apm.getProject().getPackaging().compareToIgnoreCase(s) == 0) {
 				inList = true;
 			}
 		}
 		return inList;
 	}
 
 	/**
 	 * Copies the project's artifact file possibly renaming it and returns its
 	 * size (important for Debian and iPKG).
 	 * <p>
 	 * For the destination the value of the property
 	 * <code>dstArtifactFile</code> is used.
 	 * </p>
 	 * 
 	 * @throws MojoExecutionException
 	 */
 	public long copyProjectArtifact() throws MojoExecutionException {
 		if (!packagingTypeBelongsToIgnoreList()) {
 			return Utils.copyProjectArtifact(l, getSrcArtifactFile(),
 					getDstArtifactFile());
 		} else {
 			l.info("Packaging type for this project has been found in the packageTypeIngore list. "
 					+ "No main artifact will be bundled.");
 			return 0;
 		}
 	}
 
 	/**
 	 * Creates a copyright file under the DEBIAN directory of the package.</br>
 	 * 
 	 * The approach followed here is simplistic.
 	 * 
 	 * @return
 	 * @throws MojoExecutionException
 	 * @throws IOException
 	 */
 	@SuppressWarnings("unchecked")
 	public long createCopyrightFile() throws MojoExecutionException {
 		File copyright = new File(dstScriptDir, "copyright");
 
 		PrintWriter w = null;
 		Iterator<License> ite = null;
 		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
 
 		try {
 			ite = ((List<License>) apm.getProject().getLicenses()).iterator();
 		} catch (Exception ex) {
 			/*
 			 * If an error occurs, it is most probably because no licences are
 			 * found. If this is the case we just return 0 (file is 0 bytes in
 			 * size)
 			 */
 			return 0;
 		}
 
 		try {
 			FileUtils.forceMkdir(copyright.getParentFile());
 			copyright.createNewFile();
 			w = new PrintWriter(new BufferedWriter(new FileWriter(copyright)));
 			w.println("Maintainer: " + targetConfiguration.getMaintainer());
 			w.println("Date: "
 					+ formatter.format(Calendar.getInstance().getTime()));
 
 			while (ite.hasNext()) {
 				License l = ite.next();
 				w.println("License:" + l.getName());
 				try {
 					w.print(Utils.getTextFromUrl(l.getUrl()));
 				} catch (MalformedURLException ex) {
 					// Nothing to worry about
 				}
 				w.println();
 			}
 		} catch (IOException ex) {
 			throw new MojoExecutionException("Error writing to copyright file",
 					ex);
 		} finally {
 			if (w != null) {
 				w.close();
 			}
 		}
 
 		return copyright.length();
 	}
 
 	public File getBaseBuildDir() {
 		return baseBuildDir;
 	}
 
 	public void setBaseBuildDir(File baseBuildDir) {
 		this.baseBuildDir = baseBuildDir;
 	}
 
 	public File getBaseSpecsDir() {
 		return baseSpecsDir;
 	}
 
 	public void setBaseSpecsDir(File baseSpecsDir) {
 		this.baseSpecsDir = baseSpecsDir;
 	}
 
 	/**
 	 * Creates a .rpmmacros file in the users home directory in order to be able
 	 * to specify a Build Area other than the user's home.
 	 * 
 	 * If a .rpmmacros file already exists will be backed up. This file can be
 	 * restored using {@link #restorerpmmacrosfile}.
 	 * 
 	 * @param l
 	 * @param basedirectory
 	 * @throws IOException
 	 * @throws MojoExecutionException
 	 */
 	public void createRpmMacrosFile() throws IOException,
 			MojoExecutionException {
 		String userHome = System.getProperty("user.home");
 		File original = new File(userHome + "/.rpmmacros");
 
 		if (original.exists()) {
 			if (l != null) {
 				l.info("File " + userHome
 						+ "/.rpmmacros found. Creating back-up.");
 			}
 			File backup = new File(userHome + "/.rpmmacros_bck");
 			FileUtils.copyFile(original, backup);
 		}
 		original.delete();
 		if (!original.exists()) {
 			if (l != null) {
 				l.info("Creating " + userHome + "/.rpmmacros file.");
 			}
 			PrintWriter p = new PrintWriter(original);
 			p.print("%_topdir       ");
 			p.println(getBasePkgDir().getAbsolutePath());
 			p.print("%tmppath       ");
 			p.println(getBasePkgDir().getAbsolutePath());
 
 			if (targetConfiguration.getMaintainer() != null) {
 				if (l != null) {
 					l.info("Maintainer found, its name could be used to sign the RPM.");
 				}
 				p.print("%_gpg_name       ");
 				p.println(targetConfiguration.getMaintainer());
 			}
 
 			p.close();
 		}
 	}
 
 	/**
 	 * 
 	 * Removes the new macros file and restores the backup created by
 	 * {@link #createrpmmacrosfile}
 	 * 
 	 * @param l
 	 * @throws IOException
 	 */
 	public void restoreRpmMacrosFileBackup(Log l) throws IOException {
 		String userHome = System.getProperty("user.home");
 		File original = new File(userHome + "/.rpmmacros");
 		File backup = new File(userHome + "/.rpmmacros_bck");
 		if (backup.exists()) {
 			if (l != null) {
 				l.info("Restoring .rpmmacros backup file.");
 			}
 			if (original.delete()) {
 				FileUtils.copyFile(backup, original);
 			}
 		} else {
 			original.delete();
 		}
 	}
 
 	public List<AuxFile> generateFilelist() throws MojoExecutionException {
 		List<AuxFile> list = new ArrayList<AuxFile>();
 
 		for (File file : FileUtils.listFiles(getBaseBuildDir(),
 				TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
 			list.add(new AuxFile(file.getAbsolutePath().replace(
 					getBaseBuildDir().toString(), "")));
 		}
 		return list;
 	}
 
 	/**
 	 * Returns a comma separated list of License names based on the licenses
 	 * provided in the POM
 	 * 
 	 * @return
 	 * @throws MojoExecutionException
 	 */
 	public String getLicense() throws MojoExecutionException {
 
 		return Utils.getConsolidatedLicenseString(apm.getProject());
 	}
 
 	/**
 	 * Sets the {@link Helper} instance's behavioral strategy.
 	 * 
 	 * <p>
 	 * Little functionality of this class is dependent on the packaging type
 	 * (deb, rpm, whatnot). In order to set up the correct one this method is to
 	 * be used.
 	 * </p>
 	 * 
 	 * @param strategy
 	 */
 	public void setStrategy(Strategy strategy) {
 		this.strategy = strategy;
 	}
 
 	/**
 	 * Provides the same functionality as getDstArtifactFile in the superclass,
 	 * but using getBaseBuildDir instead of getBasePkgDir
 	 */
 	public File getDstArtifactFile() {
 		// Implementation note: Subtle differences between DEB and RPM.
 		return strategy.getDstArtifactFile(this);
 	}
 
 	public String getPackageFileName() {
 		// Implementation note: Subtle differences between DEB and RPM.
 		return strategy.getPackageFileName(this);
 	}
 
 	public String getPackageFileNameWithoutExtension() {
 		// Implementation note: Subtle differences between DEB and RPM.
 		return strategy.getPackageFileNameWithoutExtension(this);
 	}
 
 	public void prepareInitialDirectories() throws MojoExecutionException {
 		// Implementation note: Subtle differences between DEB and RPM.
 		strategy.prepareInitialDirectories(this);
 	}
 
 	public String getArchitecture() throws MojoExecutionException {
 		// Implementation note: Subtle differences between DEB and RPM.
 		return strategy.getArchitecture(this);
 	}
 
 	/**
 	 * Abstracts all the functionality that is different between different
 	 * packaging types.
 	 * 
 	 * <p>
 	 * Relates to subtle differences between RPM and DEB packaging.
 	 * </p>
 	 * 
 	 * @author Robert Schuster <r.schuster@tarent.de>
 	 * 
 	 */
 	static abstract class Strategy {
 
 		protected abstract File getDstArtifactFile(Helper instance);
 
 		protected abstract void prepareInitialDirectories(Helper instance)
 				throws MojoExecutionException;
 
 		protected abstract String getPackageFileName(Helper instance);
 
 		protected abstract String getPackageFileNameWithoutExtension(
 				Helper instance);
 
 		protected abstract String getArchitecture(Helper instance);
 	}
 
 }
