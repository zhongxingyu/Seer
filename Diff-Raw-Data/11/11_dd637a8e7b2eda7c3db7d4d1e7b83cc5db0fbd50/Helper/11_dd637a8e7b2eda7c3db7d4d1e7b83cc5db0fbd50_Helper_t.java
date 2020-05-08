 package de.tarent.maven.plugins.pkg.helper;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
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
 import de.tarent.maven.plugins.pkg.AuxFile;
 import de.tarent.maven.plugins.pkg.JarFile;
 import de.tarent.maven.plugins.pkg.Packaging;
 import de.tarent.maven.plugins.pkg.Path;
 import de.tarent.maven.plugins.pkg.TargetConfiguration;
 import de.tarent.maven.plugins.pkg.Utils;
 import de.tarent.maven.plugins.pkg.generator.WrapperScriptGenerator;
 import de.tarent.maven.plugins.pkg.map.Entry;
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
 	
 	String aotPackageName;
 
 	/**
 	 * The base directory for the gcj package.
 	 */
 	File aotPkgDir;
 
 	/**
 	 * All files belonging to the package are put into this directory. For deb
 	 * packaging the layout inside is done according to `man dpkg-deb`.
 	 */
 	File basePkgDir;
 
 	/**
 	 * The destination file for the project's artifact inside the the package at
 	 * construction time (equals ${basePkgDir}/${targetArtifactFile}).
 	 */
 	File dstArtifactFile;
 
 	File dstAuxDir;
 
 	File dstBinDir;
 
 	File dstBundledJarDir;
 
 	File dstDataDir;
 
 	File dstDatarootDir;
 
 	/**
 	 * The destination directory for JNI libraries at package building time
 	 * (e.g. starts with "${packaging.getProject().outputdir}/")
 	 */
 	File dstJNIDir;
 
 	File dstRoot;
 
 	File dstScriptDir;
 
 	File dstStarterDir;
 
 	File dstSysconfDir;
 
 	File dstWindowsWrapperScriptFile;
 
 	File dstWrapperScriptFile;
 
 	String packageName;
 
 	String packageVersion;
 
 	/**
 	 * A file pointing at the source jar (it *MUST* be a jar).
 	 */
 	File srcArtifactFile;
 
 	File targetAuxDir;
 
 	TargetConfiguration targetConfiguration;
 
 
 	/**
 	 * Location of the project's artifact on the target system (needed for the
 	 * classpath construction). (e.g. /usr/share/java/app-2.0-SNAPSHOT.jar)
 	 */
 	File targetArtifactFile;
 
 	File targetBinDir;
 
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
 	File targetBundledJarDir;
 
 	File targetDataDir;
 
 	File targetDatarootDir;
 
 	/**
 	 * Location of the JNI libraries on the target device (e.g. /usr/lib/jni).
 	 */
 	File targetJNIDir;
 
 	/**
 	 * Location of the path which contains JNI libraries on the target device.
 	 * (e.g. /usr/lib/jni:/usr/lib).
 	 */
 	File targetLibraryPath;
 
 	File targetRoot;
 
 	File targetStarterDir;
 
 	File targetSysconfDir;
 
 	File targetWrapperScriptFile;
 
 	File tempRoot;
 	
 	Log l;
 	
 	protected Packaging packaging;
 	
 	public File getTargetAuxDir() {
 		return targetAuxDir;
 	}
 
 	public void setTargetAuxDir(File targetAuxDir) {
 		this.targetAuxDir = targetAuxDir;
 	}
 	
 	public Helper(TargetConfiguration targetConfiguration, Packaging packaging) {
 		
 		this.targetConfiguration = targetConfiguration;
 		this.packaging = packaging;
 		this.l = packaging.getLog();
 		
 	}
 
 	public TargetConfiguration getTargetConfiguration() {
 		return targetConfiguration;
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
 	public long copyArtifacts(Set<Artifact> artifacts) throws MojoExecutionException {
 		return Utils.copyArtifacts(l, artifacts, getDstBundledJarDir());
 	}
 
 	/**
 	 * Copies all kinds of auxialiary files to their respective destination.
 	 * 
 	 * <p>
 	 * The method consults the getSrcAuxFilesDir(), srcSysconfFilesDir,
 	 * srcDatarootFilesDir, srcDataFilesDir, getSrcJNIFilesDir() properties as well
 	 * as their corresponding destination properties for this.
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
 		size += Utils.copyFiles(l, getSrcAuxFilesDir(), getDstAuxDir(), targetConfiguration.getAuxFiles(), "aux file");
 
 		size += Utils.copyFiles(l, getSrcBinFilesDir(), getDstBinDir(), targetConfiguration.getBinFiles(), "bin file", true);
 
 		size += Utils.copyFiles(l, getSrcSysconfFilesDir(), getDstSysconfDir(), targetConfiguration.getSysconfFiles(), "sysconf file");
 
 		size += Utils.copyFiles(l, getSrcDatarootFilesDir(), getDstDatarootDir(), targetConfiguration.getDatarootFiles(), "dataroot file");
 
 		size += Utils.copyFiles(l, getSrcDataFilesDir(), getDstDataDir(), targetConfiguration.getDataFiles(), "data file");
 
 		size += Utils.copyFiles(l, getSrcJNIFilesDir(), getDstJNIDir(), targetConfiguration.getJniFiles(), "JNI library");
 
 		size += Utils.copyFiles(l, getSrcJarFilesDir(), getDstBundledJarDir(), targetConfiguration.getJarFiles(), "jar file");
 
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
 	    
 		if (targetConfiguration.getPreinstScript() != null)
 			bytesCopied +=  writeScript("pre-install", 
 					new File(getSrcAuxFilesDir(), targetConfiguration.getPreinstScript()), 
 					new File(dir, "preinst"), this);
 
 		if (targetConfiguration.getPrermScript() != null)
 			bytesCopied += writeScript("pre-remove", 
 					new File(getSrcAuxFilesDir(), targetConfiguration.getPrermScript()), 
 					new File(dir, "prerm"), this);
 
 		if (targetConfiguration.getPostinstScript() != null)
 			bytesCopied += writeScript("post-install", 
 					new File(getSrcAuxFilesDir(), targetConfiguration.getPostinstScript()), 
 					new File(dir, "postinst"),
 					this);
 
 		if (targetConfiguration.getPostrmScript() != null)
 			bytesCopied += writeScript("post-remove", 
 					new File(getSrcAuxFilesDir(), targetConfiguration.getPostrmScript()), 
 					new File(dir, "postrm"), this);
 		
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
 	public Set<Artifact> createClasspathLine(Path bcp, Path cp) throws MojoExecutionException {
 		return createClasspathLine(l, getTargetBundledJarDir(), bcp, cp, getTargetArtifactFile());
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
 	public void generateWrapperScript(Set<Artifact> bundledArtifacts, Path bcp, Path classpath, boolean windows)
 			throws MojoExecutionException {
 		WrapperScriptGenerator gen = new WrapperScriptGenerator();
 		gen.setMaxJavaMemory(targetConfiguration.getMaxJavaMemory());
 
 		gen.setCustomCodeUnix(targetConfiguration.getCustomCodeUnix());
 
 		if (getTargetLibraryPath() != null)
 			gen.setLibraryPath(new Path(getTargetLibraryPath()));
 
 		gen.setProperties(targetConfiguration.getSystemProperties());
 
 		// Set to default Classmap file on Debian/Ubuntu systems.
 		// TODO: make this configurable
 		if (targetConfiguration.isAotCompile())
 			gen.setClassmapFile("/var/lib/gcj-4.1/classmap.db");
 
 		if (targetConfiguration.isAdvancedStarter()) {
 			l.info("setting up advanced starter");
 			Utils.setupStarter(l, targetConfiguration.getMainClass(), getDstStarterDir(), classpath);
 
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
 			throw new MojoExecutionException("IOException while generating wrapper script", ioe);
 		}
 
 		if (windows) {
 			Utils.createFile(getDstWindowsWrapperScriptFile(), "windows batch");
 
 			gen.setCustomCodeWindows(targetConfiguration.getCustomCodeWindows());
 
 			try {
 				gen.generate(getDstWindowsWrapperScriptFile());
 			} catch (IOException ioe) {
 				throw new MojoExecutionException("IOException while generating windows batch file", ioe);
 			}
 		}
 
 		// Make the wrapper script executable.
 		Utils.makeExecutable(getDstWrapperScriptFile(), "wrapper script");
 
 	}
 
 	public String getAotPackageName() {
 		if (aotPackageName == null)
 			aotPackageName = Utils.gcjise(getArtifactId(), targetConfiguration.getSection(), packaging.getPm().isDebianNaming());
 
 		return aotPackageName;
 	}
 
 	public File getAotPkgDir() {
 		if (aotPkgDir == null)
 			aotPkgDir = new File(getTempRoot(), aotPackageName + "-" + getPackageVersion());
 
 		return aotPkgDir;
 	}
 
 	public String getArtifactId() {
 		return packaging.getProject().getArtifactId();
 	}
 
 	public File getBasePkgDir() {
 		if (basePkgDir == null)
 			basePkgDir = new File(getTempRoot(), getPackageName() + "-" + getPackageVersion());
 
 		return basePkgDir;
 	}
 
 	public File getDstArtifactFile() {
 		if (dstArtifactFile == null)
 			dstArtifactFile = new File(getBasePkgDir(), getTargetArtifactFile().toString());
 
 		return dstArtifactFile;
 	}
 
 	public File getDstAuxDir() {
 		return dstAuxDir;
 	}
 
 	public File getDstBinDir() {
 		if (dstBinDir == null)
 			dstBinDir = new File(getBasePkgDir(), getTargetBinDir().toString());
 
 		return dstBinDir;
 	}
 
 	public File getDstBundledJarDir() {
 		if (dstBundledJarDir == null)
 			dstBundledJarDir = new File(basePkgDir, getTargetBundledJarDir().toString());
 
 		return dstBundledJarDir;
 	}
 
 	public File getDstDataDir() {
 		if (dstDataDir == null)
 			dstDataDir = new File(getBasePkgDir(), getTargetDataDir().toString());
 
 		return dstDataDir;
 	}
 
 	public File getDstDatarootDir() {
 		if (dstDatarootDir == null)
 			dstDatarootDir = new File(getBasePkgDir(), getTargetDatarootDir().toString());
 
 		return dstDatarootDir;
 	}
 
 	public File getDstJNIDir() {
 		if (dstJNIDir == null)
 			dstJNIDir = new File(getBasePkgDir(), getTargetJNIDir().toString());
 
 		return dstJNIDir;
 	}
 
 	public File getDstRoot() {
 		if (dstRoot == null)
 			dstRoot = new File(getBasePkgDir(), getTargetRoot().toString());
 
 		return dstRoot;
 	}
 
 	public File getDstScriptDir() {
 		if (dstScriptDir == null)
 			throw new UnsupportedOperationException(
 					"This dstScriptDir property has to be provided explicitly in advance!");
 
 		return dstScriptDir;
 	}
 
 	public File getDstStarterDir() {
 		if (dstStarterDir == null)
 			dstStarterDir = new File(getBasePkgDir(), getTargetStarterDir().toString());
 
 		return dstStarterDir;
 	}
 
 	public File getDstSysconfDir() {
 		if (dstSysconfDir == null)
 			dstSysconfDir = new File(getBasePkgDir(), getTargetSysconfDir().toString());
 
 		return dstSysconfDir;
 	}
 
 	public File getDstWindowsWrapperScriptFile() {
 		if (dstWindowsWrapperScriptFile == null)
 			dstWindowsWrapperScriptFile = new File(getDstWrapperScriptFile().getAbsolutePath() + ".bat");
 
 		return dstWindowsWrapperScriptFile;
 	}
 
 	public File getDstWrapperScriptFile() {
 		if (dstWrapperScriptFile == null)
 			// Use the provided wrapper script name or the default.
 			dstWrapperScriptFile = new File(getBasePkgDir(), getTargetWrapperScriptFile().toString());
 
 		return dstWrapperScriptFile;
 	}
 
 	public String getJavaExec() {
 		return packaging.getJavaExec();
 	}
 
 	public String get7ZipExec() {
 		return packaging.get_7zipExec();
 	}
 
 	public File getOutputDirectory() {
 		return packaging.getOutputDirectory();
 	}
 
 	public String getPackageName() {
 		if (packageName == null)
 			packageName = Utils.createPackageName(packaging.getProject().getArtifactId(), targetConfiguration.getSection(), packaging.getPm().isDebianNaming());
 
 		return packageName;
 	}
 
 	public String getPackageVersion() {
 		if (packageVersion == null)
 			packageVersion = Utils.fixVersion(packaging.getProject().getVersion()) + "-0" + Utils.sanitizePackageVersion(targetConfiguration.getChosenTarget())
 					+ (targetConfiguration.getRevision().length() == 0 ? "" : "-" + targetConfiguration.getRevision());
 
 		return packageVersion;
 	}
 
 	public String getProjectDescription() {
 		return packaging.getProject().getDescription();
 	}
 
 	public String getProjectUrl() {
 		return packaging.getProject().getUrl();
 	}
 
 	public File getSrcArtifactFile() {
 		if (srcArtifactFile == null)
 			srcArtifactFile = new File(packaging.getOutputDirectory().getPath(), packaging.getFinalName() + "." + packaging.getProject().getPackaging());
 
 		return srcArtifactFile;
 	}
 
 	public File getSrcAuxFilesDir() {
 		return (targetConfiguration.getSrcAuxFilesDir().length() == 0) ? new File(packaging.getProject().getBasedir(), Packaging.getDefaultSrcAuxfilesdir()) : new File(
 				packaging.getProject().getBasedir(), targetConfiguration.getSrcAuxFilesDir());
 	}
 
 	public File getSrcDataFilesDir() {
 		return (targetConfiguration.getSrcDataFilesDir().length() == 0) ? getSrcAuxFilesDir() : new File(packaging.getProject().getBasedir(),
 				targetConfiguration.getSrcDataFilesDir());
 	}
 
 	public File getSrcDatarootFilesDir() {
 		return (targetConfiguration.getSrcDatarootFilesDir().length() == 0) ? getSrcAuxFilesDir() : new File(packaging.getProject().getBasedir(),
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
 		return (targetConfiguration.getSrcIzPackFilesDir().length() == 0 ? getSrcAuxFilesDir() : new File(packaging.getProject().getBasedir(),
 				targetConfiguration.getSrcIzPackFilesDir()));
 	}
 
 	public File getSrcJarFilesDir() {
 		return (targetConfiguration.getSrcJarFilesDir().length() == 0) ? getSrcAuxFilesDir() : new File(packaging.getProject().getBasedir(),
 				targetConfiguration.getSrcJarFilesDir());
 	}
 
 	public File getSrcJNIFilesDir() {
 		return (targetConfiguration.getSrcJNIFilesDir().length() == 0) ? getSrcAuxFilesDir() : new File(packaging.getProject().getBasedir(),
 				targetConfiguration.getSrcJNIFilesDir());
 	}
 
 	public File getSrcSysconfFilesDir() {
 		return (targetConfiguration.getSrcSysconfFilesDir().length() == 0) ? getSrcAuxFilesDir() : new File(packaging.getProject().getBasedir(),
 				targetConfiguration.getSrcSysconfFilesDir());
 	}
 
 	public File getSrcBinFilesDir() {
 		return (targetConfiguration.getSrcBinFilesDir().length() == 0) ? getSrcAuxFilesDir() : new File(packaging.getProject().getBasedir(),
 				targetConfiguration.getSrcBinFilesDir());
 	}
 
 	public File getTargetArtifactFile() {
 		if (targetArtifactFile == null)
 			targetArtifactFile = new File((targetConfiguration.isBundleAll() || packaging.getPm().hasNoPackages() ? getTargetBundledJarDir()
 					: new File(packaging.getPm().getDefaultJarPath())), packaging.getProject().getArtifactId() + "." + packaging.getProject().getPackaging());
 
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
 		if (targetBinDir == null)
 			targetBinDir = (targetConfiguration.getBindir().length() == 0 ? new File(getTargetRoot(), packaging.getPm().getDefaultBinPath()) : new File(
 					targetConfiguration.getBindir()));
 
 		return targetBinDir;
 	}
 
 	public File getTargetBundledJarDir() {
 		if (targetBundledJarDir == null)
 			targetBundledJarDir = (targetConfiguration.getBundledJarDir().length() == 0 ? new File(getTargetRoot(), new File(
 					packaging.getPm().getDefaultJarPath(), packaging.getProject().getArtifactId()).toString()) : new File(targetConfiguration.getBundledJarDir()));
 
 		return targetBundledJarDir;
 	}
 
 	public File getTargetDataDir() {
 		if (targetDataDir == null)
 			targetDataDir = (targetConfiguration.getDatadir().length() == 0 ? new File(getTargetDatarootDir(), packaging.getProject().getName()) : new File(
 					targetConfiguration.getDatadir()));
 
 		return targetDataDir;
 	}
 
 	public File getTargetDatarootDir() {
 		if (targetDatarootDir == null)
 			targetDatarootDir = (targetConfiguration.getDatarootdir().length() == 0 ? new File(getTargetRoot(), "usr/share") : new File(
 					targetConfiguration.getDatarootdir()));
 
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
 		if (targetJNIDir == null)
 			targetJNIDir = new File(getTargetRoot(), packaging.getPm().getDefaultJNIPath().split(":")[0]);
 
 		return targetJNIDir;
 	}
 
 	public File getTargetLibraryPath() {
 		if (targetLibraryPath == null)
 			targetLibraryPath = new File(getTargetRoot(), packaging.getPm().getDefaultJNIPath());
 
 		return targetLibraryPath;
 	}
 
 	public File getTargetRoot() {
 		if (targetRoot == null)
 			targetRoot = new File(targetConfiguration.getPrefix());
 
 		return targetRoot;
 	}
 
 	public File getTargetStarterDir() {
 		if (targetStarterDir == null)
 			targetStarterDir = new File(getTargetBundledJarDir(), "_starter");
 
 		return targetStarterDir;
 	}
 
 	public File getTargetSysconfDir() {
 		if (targetSysconfDir == null)
 			targetSysconfDir = (targetConfiguration.getSysconfdir().length() == 0 ? new File(getTargetRoot(), "etc")
 					: new File(targetConfiguration.getSysconfdir()));
 
 		return targetSysconfDir;
 	}
 
 	public File getTargetWrapperScriptFile() {
 		if (targetWrapperScriptFile == null)
 			targetWrapperScriptFile = new File(getTargetBinDir(), (targetConfiguration.getWrapperScriptName() != null ? targetConfiguration.getWrapperScriptName()
 					: packaging.getProject().getArtifactId()));
 
 		return targetWrapperScriptFile;
 	}
 
 	public File getTempRoot() {
 		if (tempRoot == null)
 			tempRoot = new File(packaging.getBuildDir(), packaging.getPm().getPackaging() + "-tmp");
 
 		return tempRoot;
 	}
 
 	public void prepareAotDirectories() throws MojoExecutionException {
 		prepareDirectories(l, tempRoot, aotPkgDir, null);
 	}
 
 	public void prepareInitialDirectories() throws MojoExecutionException {
 		prepareDirectories(l, tempRoot, basePkgDir, dstJNIDir);
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
 
 	public void setTempRoot(File tempRoot) {
 		this.tempRoot = tempRoot;
 	}
 
 	public String generatePackageFileName() {
 
 		StringBuilder packageName = new StringBuilder();
 		packageName.append(getPackageName().toLowerCase());
 		packageName.append("_");
 		packageName.append(getPackageVersion());
 		packageName.append("_all.deb");
 		return packageName.toString();
 
 	}
 
 	final void prepareDirectories(Log l, File tempRoot, File basePkgDir, File jniDir) throws MojoExecutionException {
 		if (l != null)
 			l.info("creating temporary directory: " + tempRoot.getAbsolutePath());
 
 		if (!tempRoot.exists() && !tempRoot.mkdirs())
 			throw new MojoExecutionException("Could not create temporary directory.");
 		
 		if (l != null)
 			l.info("cleaning the temporary directory");
 		
 		try {
 			FileUtils.cleanDirectory(tempRoot);
 		} catch (IOException ioe) {
 			throw new MojoExecutionException("Exception while cleaning temporary directory.", ioe);
 		}
 		
 		if (l != null)
 			l.info("creating package directory: " + basePkgDir.getAbsolutePath());
 		
 		if (!basePkgDir.mkdirs())
 			throw new MojoExecutionException("Could not create package directory.");
 
 		if (jniDir != null && targetConfiguration.getJniFiles() != null && targetConfiguration.getJniFiles().size() > 0) {
 			if (!jniDir.mkdirs())
 				throw new MojoExecutionException("Could not create JNI directory.");
 		}
 
 	}
 	
 
 	  
 	  protected final long writeScript(String item, File srcScriptFile, File dstScriptFile, Helper ph)
 	  throws MojoExecutionException
 	  {
 	    Utils.createFile(dstScriptFile, item + " file");
 	    // Write a #/bin/sh header
 	    
 	    Utils.makeExecutable(dstScriptFile, item + " file");
 	    PrintWriter writer = null;
 	    try
 	    {
 	      writer = new PrintWriter(new BufferedWriter(new FileWriter(dstScriptFile)));
 	      writer.println("#!/bin/sh");
 	      writer.println("# This script is partly autogenerated by the " + getClass().getName() + " class");
 	      writer.println("# of the maven-pkg-plugin. The autogenerated part adds some variables of the packaging");
 	      writer.println("# to the top of the script which can be used in the lower manual part.");
 	      writer.println();
 	      writer.println("prefix=\"" + ph.getTargetRoot() + "\"");
 	      writer.println("bindir=\"" + ph.getTargetBinDir() + "\"");
 	      writer.println("datadir=\"" + ph.getTargetDataDir() + "\"");
 	      writer.println("datarootdir=\"" + ph.getTargetDatarootDir() + "\"");
 	      writer.println("sysconfdir=\"" + ph.getTargetSysconfDir() + "\"");
 	      writer.println("jnidir=\"" + ph.getTargetJNIDir() + "\"");
 	      writer.println("bundledjardir=\"" + ph.getTargetBundledJarDir() + "\"");
 	      writer.println("wrapperscriptfile=\"" + ph.getTargetWrapperScriptFile() + "\"");
 	      writer.println("version=\"" + ph.getPackageVersion() + "\"");
 	      writer.println("name=\"" + ph.getPackageName() + "\"");
 	      writer.println("mainClass=\"" + targetConfiguration.getMainClass() + "\"");
 	      writer.println("scriptType=\"" + item + "\"");
 	      writer.println();
 	      writer.println("distro=\"" + targetConfiguration.getChosenDistro() + "\"");
 	      writer.println("distroLabel=\"" + packaging.getPm().getDistroLabel() + "\"");
 	      writer.println("packaging=\"" + packaging.getPm().getPackaging() + "\"");
 	      writer.println();
 	      writer.println("# What follows is the content script file " + srcScriptFile.getName());
 	      writer.println();
 	      
 	      // Now append the real script
 	      IOUtils.copy(new FileInputStream(srcScriptFile), writer);
 	      
 	      return dstScriptFile.length();
 	    }
 	    catch (IOException ioe)
 	    {
 	      throw new MojoExecutionException("IO error while writing the script file " + dstScriptFile, ioe);
 	    }finally{
 	    	if(writer!=null){
 	    		writer.close();
 	    	}
 	    }
 	  }
 
 	/**
 	   * Creates the bootclasspath and classpath line from the project's
 	   * dependencies and returns the artifacts which will be bundled with the
 	   * package.
 	   * 
 	   * @param pm The package map used to resolve the Jar file names.
 	   * @param bundled A set used to track the bundled jars for later file-size
 	   *          calculations.
 	   * @param bcp StringBuilder which contains the boot classpath line at the end
 	   *          of the method.
 	   * @param cp StringBuilder which contains the classpath line at the end of the
 	   *          method.
 	   * @return
 	   */
 	  protected final Set<Artifact> createClasspathLine(final Log l,
 	                                          final File targetJarPath,
 	                                          final Path bcp,
 	                                          final Path cp,
 	                                          File targetArtifactFile)
 	      throws MojoExecutionException
 	  {
 	    final Set<Artifact> bundled = new HashSet<Artifact>();
 	
 	    l.info("resolving dependency artifacts");
 	
 	    Set dependencies = new HashSet();
 	    try
 	      {
 	        // Notice only compilation dependencies which are Jars.
 	        // Shared Libraries ("so") are filtered out because the
 	        // JNI dependency is solved by the system already.
 	        AndArtifactFilter compileFilter = new AndArtifactFilter();
 	        compileFilter.add(new ScopeArtifactFilter(Artifact.SCOPE_COMPILE));
 	        compileFilter.add(new TypeArtifactFilter("jar"));
 	
 	        dependencies.addAll(Utils.findArtifacts(compileFilter, packaging.getFactory(), packaging.getResolver(), 
 	        		packaging.getProject(), packaging.getProject().getArtifact(), packaging.getLocalRepo(), 
 	        		packaging.getRemoteRepos(), packaging.getMetadataSource()));
 
 
 	        AndArtifactFilter runtimeFilter = new AndArtifactFilter();
 	        runtimeFilter.add(new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME));
 	        runtimeFilter.add(new TypeArtifactFilter("jar"));
 	        
 	        dependencies.addAll(Utils.findArtifacts(runtimeFilter, packaging.getFactory(), packaging.getResolver(), 
 	        		packaging.getProject(), packaging.getProject().getArtifact(), packaging.getLocalRepo(), 
	        		packaging.getRemoteRepos(), packaging.getMetadataSource()));


	        AndArtifactFilter providedFilter = new AndArtifactFilter();
	        providedFilter.add(new ScopeArtifactFilter(Artifact.SCOPE_PROVIDED));
	        providedFilter.add(new TypeArtifactFilter("jar"));
	        
	        dependencies.addAll(Utils.findArtifacts(providedFilter, packaging.getFactory(), packaging.getResolver(), 
	        		packaging.getProject(), packaging.getProject().getArtifact(), packaging.getLocalRepo(), 
	        		packaging.getRemoteRepos(), packaging.getMetadataSource()));	     
 	        
 	      }
 	    catch (ArtifactNotFoundException anfe)
 	      {
 	        throw new MojoExecutionException(
 	                                         "Exception while resolving dependencies",
 	                                         anfe);
 	      }
 	    catch (InvalidDependencyVersionException idve)
 	      {
 	        throw new MojoExecutionException(
 	                                         "Exception while resolving dependencies",
 	                                         idve);
 	      }
 	    catch (ProjectBuildingException pbe)
 	      {
 	        throw new MojoExecutionException(
 	                                         "Exception while resolving dependencies",
 	                                         pbe);
 	      }
 	    catch (ArtifactResolutionException are)
 	      {
 	        throw new MojoExecutionException(
 	                                         "Exception while resolving dependencies",
 	                                         are);
 	      }
 	
 	    Visitor v = new Visitor()
 	    {
 	      public void bundle(Artifact artifact)
 	      {
 	        // Put to artifacts which will be bundled (allows copying and filesize
 	        // summing later).
 	        bundled.add(artifact);
 	
 	        // TODO: Perhaps one want a certain bundled dependency in boot
 	        // classpath.
 	
 	        // Bundled Jars will always live in targetJarPath
 	        File file = artifact.getFile();
 	        if (file != null)
 	          cp.append(targetJarPath.toString() + "/" + file.getName());
 	        else
 	          l.warn("Cannot put bundled artifact " + artifact.getArtifactId()
 	                 + " to Classpath.");
 	      }
 	
 	      public void visit(Artifact artifact, Entry entry)
 	      {
 	        // If all dependencies should be bundled take a short-cut to bundle()
 	        // thereby overriding what was configured through property files.
 	        if (targetConfiguration.isBundleAll())
 	          {
 	            bundle(artifact);
 	            return;
 	          }
 	
 	        Path b = (entry.isBootClasspath) ? bcp : cp;
 	
 	        Iterator<String> ite = entry.jarFileNames.iterator();
 	        while (ite.hasNext())
 	          {
 	        	StringBuilder sb = new StringBuilder(); 
 	            String fileName = ite.next();
 	
 	            // Prepend default Jar path if file is not absolute.
 	            if (fileName.charAt(0) != '/')
 	              {
 	                sb.append(packaging.getPm().getDefaultJarPath());
 	                sb.append("/");
 	              }
 	
 	            sb.append(fileName);
 	            
 	            b.append(sb.toString());
 	          }
 	      }
 	
 	    };
 	
 	    packaging.getPm().iterateDependencyArtifacts(l, dependencies, v, true);
 	    
 	    // Add the custom jar files to the classpath
 	    for (Iterator<JarFile> ite = targetConfiguration.getJarFiles().iterator(); ite.hasNext();)
 	    {
 	    	AuxFile auxFile = ite.next();
 	    	
 	    	cp.append(targetJarPath.toString()
 	    			  + "/" + new File(auxFile.getFrom()).getName());
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
 	  public final String createConflictsLine() throws MojoExecutionException
 	  {
 		  return createPackageLine(targetConfiguration.getConflicts());
 	  }
 
 	/**
 	   * Investigates the project's runtime dependencies and creates a dependency
 	   * line suitable for the control file from them.
 	   * 
 	   * @return
 	   */
 	  public final String createDependencyLine() throws MojoExecutionException
 	  {
 	    String defaults = packaging.getPm().getDefaultDependencyLine();
 	    StringBuffer manualDeps = new StringBuffer();
 	    Iterator<String> ite = targetConfiguration.getManualDependencies().iterator();
 	    while (ite.hasNext())
 	      {
 	        String dep = ite.next();
 	
 	        manualDeps.append(dep);
 	        manualDeps.append(", ");
 	      }
 	
 	    if (manualDeps.length() >= 2)
 	      manualDeps.delete(manualDeps.length() - 2, manualDeps.length());
 	
 	    // If all dependencies should be bundled the package will only
 	    // need the default Java dependencies of the system and the remainder
 	    // of the method can be skipped.
 	    if (targetConfiguration.isBundleAll())
 	      return Utils.joinDependencyLines(defaults, manualDeps.toString());
 	
 	    Set runtimeDeps = null;
 	
 	    try
 	      {
 	        AndArtifactFilter andFilter = new AndArtifactFilter();
 	        andFilter.add(new ScopeArtifactFilter(Artifact.SCOPE_COMPILE));
 	        andFilter.add(new TypeArtifactFilter("jar"));
 	
 	        runtimeDeps = Utils.findArtifacts(andFilter, packaging.getFactory(), packaging.getResolver(), 
 	        		packaging.getProject(), packaging.getProject().getArtifact(), 
 	        		packaging.getLocalRepo(), packaging.getRemoteRepos(), packaging.getMetadataSource());
 	
 	        andFilter = new AndArtifactFilter();
 	        andFilter.add(new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME));
 	        andFilter.add(new TypeArtifactFilter("jar"));
 	
 	        runtimeDeps.addAll(Utils.findArtifacts(andFilter, packaging.getFactory(), packaging.getResolver(), 
 	        		packaging.getProject(), packaging.getProject().getArtifact(), 
 	        		packaging.getLocalRepo(), packaging.getRemoteRepos(), packaging.getMetadataSource()));
 	      }
 	    catch (ArtifactNotFoundException anfe)
 	      {
 	        throw new MojoExecutionException(
 	                                         "Exception while resolving dependencies",
 	                                         anfe);
 	      }
 	    catch (InvalidDependencyVersionException idve)
 	      {
 	        throw new MojoExecutionException(
 	                                         "Exception while resolving dependencies",
 	                                         idve);
 	      }
 	    catch (ProjectBuildingException pbe)
 	      {
 	        throw new MojoExecutionException(
 	                                         "Exception while resolving dependencies",
 	                                         pbe);
 	      }
 	    catch (ArtifactResolutionException are)
 	      {
 	        throw new MojoExecutionException(
 	                                         "Exception while resolving dependencies",
 	                                         are);
 	      }
 	
 	    final StringBuilder line = new StringBuilder();
 	
 	    // Add default system dependencies for Java packages.
 	    line.append(defaults);
 	
 	    // Visitor implementation which creates the dependency line.
 	    Visitor v = new Visitor()
 	    {
 	      Set processedDeps = new HashSet();
 	
 	      public void bundle(Artifact _)
 	      {
 	        // Nothing to do for bundled artifacts.
 	      }
 	
 	      public void visit(Artifact artifact, Entry entry)
 	      {
 	        // Certain Maven Packages have only one package in the target system.
 	        // If that one was already added we should not add it any more.
 	        if (processedDeps.contains(entry.dependencyLine))
 	          return;
 	
 	        if (entry.dependencyLine.length() == 0)
 	          l.warn("Invalid package name for artifact: " + entry.artifactSpec);
 	
 	        line.append(", ");
 	        line.append(entry.dependencyLine);
 	
 	        // Mark as included dependency.
 	        processedDeps.add(entry.dependencyLine);
 	      }
 	    };
 	
 	    packaging.getPm().iterateDependencyArtifacts(l, runtimeDeps, v, true);
 	
 	    return Utils.joinDependencyLines(line.toString(), manualDeps.toString());
 	  }
 
 	protected final String createPackageLine(List<String> packageDescriptors)
 	  {
 		  if(packageDescriptors == null || packageDescriptors.isEmpty())
 			  return null;
 		  
 		  StringBuffer packageLine = new StringBuffer();
 		  Iterator<String> ite = packageDescriptors.iterator();
 		  while (ite.hasNext())
 		  {
 			  String packageDescriptor = ite.next();
 	
 			  packageLine.append(packageDescriptor);
 			  packageLine.append(", ");
 		  }
 	
 		  // Remove last ", "
 		  if (packageLine.length() >= 2)
 			  packageLine.delete(packageLine.length() - 2, packageLine.length());
 		  
 		  return packageLine.toString();
 	  }
 
 	/**
 	   * Creates the "Provides"-line for the package control file
 	   * 
 	   * @return provides line
 	   * @throws MojoExecutionException
 	   */
 	  public final String createProvidesLine() throws MojoExecutionException
 	  {
 		  return createPackageLine(targetConfiguration.getProvides());
 	  }
 
 	/**
 	   * Creates the "Recommends"-line for the package control file
 	   * 
 	   * @return recommends line
 	   * @throws MojoExecutionException
 	   */
 	  public final String createRecommendsLine() throws MojoExecutionException
 	  {
 		  return createPackageLine(targetConfiguration.getRecommends());
 	  }
 
 	/**
 	   * Creates the "Replaces"-line for the package control file
 	   * 
 	   * @return suggests line
 	   * @throws MojoExecutionException
 	   */
 	  public final String createReplacesLine() throws MojoExecutionException
 	  {
 		  return createPackageLine(targetConfiguration.getReplaces());
 	  }
 
 	/**
 	   * Creates the "Suggests"-line for the package control file
 	   * 
 	   * @return suggests line
 	   * @throws MojoExecutionException
 	   */
 	  public final String createSuggestsLine() throws MojoExecutionException
 	  {
 		  return createPackageLine(targetConfiguration.getSuggests());
 	  }
 
 	  
 	  /**
 	   * Checks if the packaging type of the current Maven project belongs to the packaging types that, when used, 
 	   * woint contain the main artifact for the project (if any) in the final package.    
 	   * @return
 	   */
 	  
 	  protected final boolean packagingTypeBelongsToIgnoreList(){
 		boolean inList = false;
 		
 		  if (l!=null)
 			  l.info("ignorePackagingTypes set. Contains: " + packaging.getIgnorePackagingTypes() 
 					+ " . Project packaging is "+packaging.getProject().getPackaging());
 		  
 		  for(String s : packaging.getIgnorePackagingTypes().split(",")){
 			  if(packaging.getProject().getPackaging().compareToIgnoreCase(s)==0){
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
 				return Utils.copyProjectArtifact(l, getSrcArtifactFile(), getDstArtifactFile());
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
 		public long createCopyrightFile() throws MojoExecutionException{
 			File copyright = new File(dstScriptDir,"copyright");
 			
 			PrintWriter w = null;
 			Iterator<License> ite = null;
 			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
 					
 			try{
 				ite = ((List<License>)packaging.getProject().getLicenses()).iterator();
 			}catch(Exception ex){
 				/*
 				 * If an error occurs, it is most probably because no licences are found.
 				 * If this is the case we just return 0 (file is 0 bytes in size)
 				 */
 				return 0;
 			}
 			
 			try{
 				FileUtils.forceMkdir(copyright.getParentFile());
 				copyright.createNewFile();
 				w = new PrintWriter(new BufferedWriter(new FileWriter(copyright)));
 				w.println("Maintainer: " + targetConfiguration.getMaintainer());				
 				w.println("Date: " + formatter.format(Calendar.getInstance().getTime()));
 				
 				while(ite.hasNext()){
 					License l = ite.next();
 					w.println("License:" +l.getName());
 					try{
 						w.print(Utils.getTextFromUrl(l.getUrl()));
 					}catch (MalformedURLException ex) {
 						// Nothing to worry about
 					}
 					w.println();
 				}				
 			}catch(IOException ex){
 				throw new MojoExecutionException("Error writing to copyright file",ex);
 			}finally{
 				if (w!=null){
 					w.close();
 				}
 			}
 			
 			return copyright.length();
 		}
 	  
 }
