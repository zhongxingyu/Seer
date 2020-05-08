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
 
 
 
 package de.tarent.maven.plugins.pkg;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
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
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.ProjectBuildingException;
 import org.apache.maven.project.artifact.InvalidDependencyVersionException;
 
 import de.tarent.maven.plugins.pkg.generator.WrapperScriptGenerator;
 import de.tarent.maven.plugins.pkg.map.Entry;
 import de.tarent.maven.plugins.pkg.map.PackageMap;
 import de.tarent.maven.plugins.pkg.map.Visitor;
 import de.tarent.maven.plugins.pkg.packager.DebPackager;
 import de.tarent.maven.plugins.pkg.packager.IpkPackager;
 import de.tarent.maven.plugins.pkg.packager.IzPackPackager;
 import de.tarent.maven.plugins.pkg.packager.Packager;
 
 /**
  * Creates a package file for the project and the given distribution.
  * 
  * @execute phase="package"
  * @goal pkg
  */
 public class Packaging
     extends AbstractPackagingMojo
 {
 
   /**
    * The <code>Helper</code> class mainly provides task oriented methods
    * which can be directly used by the packager implementations.
    * 
    * <p>The idea is that all packagers basically do the same actions (copy jars,
    * create classpath and dependency line, ...) but operate on different directories
    * and/or files names. Therefore the packager implementation must set the various
    * file names and directories and afterwards call the task-oriented methods.</p>
    * 
    * <p>The method's documentation describes their relationship and usage of variables.</p>
    * 
    * <p>There is a common relationship between the method with
    * <code>target</code> and <code>dest</code> in their name.
    * <code>target</code> means a file location or directory which is available
    * on the target device (= after installation). Thus such a file must not
    * be used for file operations at packaging time since it will simply not be
    * valid. In contrast a method having <code>dest</code> in their name denotes
    * the file of the corresponding <code>target</code> entry at packaging time.
    * </p>
    * 
    * <p>All <code>getTarget...</code>-method with a corresponding
    * <code>getDest...</code> method can provide a default value whose generation
    * is described in the method's documentation. However if a different value
    * is provided by the packager implementation by calling the corresponding
    * setter the automatic generation is prevented. By doing so a packager can
    * customize all file and directory locations.</p>
    * 
    * <p>A notable exception to this rule is the {@link #getDstScriptDir()} method
    * which fails with an exception if no non-null value for the
    * <code>dstScriptDir</code> property has been set.</p>
    * 
    * @author Robert Schuster (robert.schuster@tarent.de)
    */
   public class Helper
   {
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
      * (e.g. starts with "${project.outputdir}/")
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
      * Location of the path which contains JNI libraries on the target device. (e.g. /usr/lib/jni:/usr/lib).
      */
     File targetLibraryPath;
 
     File targetRoot;
 
     File targetStarterDir;
 
     File targetSysconfDir;
 
     File targetWrapperScriptFile;
 
     File tempRoot;
 
     Helper()
     {
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
     public long copyArtifacts(Set artifacts) throws MojoExecutionException
     {
       return Packaging.this.copyArtifacts(getLog(), artifacts,
                                           getDstBundledJarDir());
     }
 
     /**
      * Copies all kinds of auxialiary files to their respective destination.
      * 
      * <p>The method consults the srcAuxFilesDir, srcSysconfFilesDir,
      * srcDatarootFilesDir, srcDataFilesDir, srcJNIFilesDir properties as well
      * as their corresponding destination properties for this.</p>
      * 
      * <p>The return value is the amount of bytes copied.</p>
      * 
      * @return
      * @throws MojoExecutionException
      */
     public long copyFiles() throws MojoExecutionException
     {
       long size = 0;
       Log l = getLog();
       size = Utils.copyFiles(l, getSrcAuxFilesDir(), getDstAuxDir(),
                              dc.auxFiles, "aux file");
 
       size = Utils.copyFiles(l, getSrcSysconfFilesDir(), getDstSysconfDir(),
                              dc.sysconfFiles, "sysconf file");
 
       size = Utils.copyFiles(l, getSrcDatarootFilesDir(), getDstDatarootDir(),
                              dc.datarootFiles, "dataroot file");
 
       size = Utils.copyFiles(l, getSrcDataFilesDir(), getDstDataDir(),
                              dc.dataFiles, "data file");
 
       size = Utils.copyFiles(l, getSrcJNIFilesDir(), getDstJNIDir(),
                              dc.jniFiles, "JNI library");
 
       size = Utils.copyFiles(l, getSrcJarFilesDir(), getDstBundledJarDir(),
               dc.jarFiles, "jar file");
 
       return size;
     }
 
     /**
      * Copies the project's artifact file possibly renaming it.
      * <p>
      * For the destination the value of the property <code<dstArtifactFile</code>
      * is used.
      * </p>
      * 
      * @throws MojoExecutionException
      */
     public void copyProjectArtifact() throws MojoExecutionException
     {
       Utils.copyProjectArtifact(getLog(), getSrcArtifactFile(),
                                 getDstArtifactFile());
     }
 
     /**
      * Copies the pre-install, pre-removal, post-install and post-removal
      * scripts (if applicable) to their proper location which is given
      * through the <code>dstScriptDir</code> property.
      * 
      * <p>To find out the source directory for the scripts the
      * <code>srcAuxFilesFir</code> property is consulted.</p>
      * 
      * @throws MojoExecutionException
      */
     public void copyScripts() throws MojoExecutionException
     {
       File dir = getDstScriptDir();
       if (dc.preinstScript != null)
         writeScript("pre-install",
                    new File(getSrcAuxFilesDir(), dc.preinstScript),
                    new File(dir, "preinst"),
                    this);
 
       if (dc.prermScript != null)
         writeScript("pre-remove",
                     new File(getSrcAuxFilesDir(), dc.prermScript),
                     new File(dir, "prerm"),
                     this);
 
       if (dc.postinstScript != null)
         writeScript("post-install",
                     new File(getSrcAuxFilesDir(), dc.postinstScript),
                     new File(dir, "postinst"),
                     this);
 
       if (dc.postrmScript != null)
         writeScript("post-remove",
                     new File(getSrcAuxFilesDir(), dc.postrmScript),
                     new File(dir, "postrm"),
                     this);
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
     public Set createClasspathLine(Path bcp, Path cp)
         throws MojoExecutionException
     {
       return Packaging.this.createClasspathLine(getLog(),
                                                 getTargetBundledJarDir(), bcp, cp,
                                                 getTargetArtifactFile());
     }
 
     /** Returns a string containing the programs dependencies.
      * 
      * @return
      * @throws MojoExecutionException
      */
     public String createDependencyLine() throws MojoExecutionException
     {
       return Packaging.this.createDependencyLine();
     }
     
     /** Returns a string containing the programs recommends.
      * 
      * @return
      * @throws MojoExecutionException
      */
     public String createRecommendsLine() throws MojoExecutionException
     {
       return Packaging.this.createRecommendsLine();
     }
     
     /** Returns a string containing the programs suggests.
      * 
      * @return
      * @throws MojoExecutionException
      */
     public String createSuggestsLine() throws MojoExecutionException
     {
       return Packaging.this.createSuggestsLine();
     }
     
     /** Returns a string containing the virtual packages this package provides.
      * 
      * @return
      * @throws MojoExecutionException
      */
     public String createProvidesLine() throws MojoExecutionException
     {
       return Packaging.this.createProvidesLine();
     }
     
     /** Returns a string containing the packages this package conflicts with. 
      * 
      * @return
      * @throws MojoExecutionException
      */
     public String createConflictsLine() throws MojoExecutionException
     {
       return Packaging.this.createConflictsLine();
     }
     
     /** Returns a string containing the packages this package replaces.
      * 
      * @return
      * @throws MojoExecutionException
      */
     public String createReplacesLine() throws MojoExecutionException
     {
       return Packaging.this.createReplacesLine();
     }
 
     /**
      * Generates a wrapper script for the application. If the
      * <code>windows</code> flag has been set another script is generated
      * for that OS.
      * 
      * <p>The method consults the properties <code>targetJNIDir</code>,
      * <code>dstWrapperScriptFile</code> and possibly
      * <code>dstWrapperScriptFileWindows</code> for its work.</p>
      * 
      * @param bundledArtifacts
      * @param bcp
      * @param cp
      * @param windows
      * @throws MojoExecutionException
      */
     public void generateWrapperScript(Set bundledArtifacts, Path bcp,
                                       Path classpath, boolean windows)
         throws MojoExecutionException
     {
       Log l = getLog();
       WrapperScriptGenerator gen = new WrapperScriptGenerator();
       gen.setMaxJavaMemory(dc.maxJavaMemory);
       
       gen.setCustomCodeUnix(dc.customCodeUnix);
 
       if (getTargetLibraryPath() != null)
         gen.setLibraryPath(new Path(getTargetLibraryPath()));
 
       gen.setProperties(dc.systemProperties);
 
       // Set to default Classmap file on Debian/Ubuntu systems.
       // TODO: make this configurable
       if (dc.isAotCompile())
         gen.setClassmapFile("/var/lib/gcj-4.1/classmap.db");
 
       if (dc.isAdvancedStarter())
         {
           l.info("setting up advanced starter");
           Utils.setupStarter(l, dc.getMainClass(), getDstStarterDir(), classpath);
 
           // Sets main class and classpath for the wrapper script.
           gen.setMainClass("_Starter");
           gen.setClasspath(new Path(getTargetStarterDir()));
         }
       else
         {
           l.info("using traditional starter");
           gen.setMainClass(dc.getMainClass());
 
           // All Jars have to reside inside the libraryRoot.
           gen.setClasspath(classpath);
         }
 
       Utils.createFile(getDstWrapperScriptFile(), "wrapper script");
 
       try
         {
           gen.generate(getDstWrapperScriptFile());
         }
       catch (IOException ioe)
         {
           throw new MojoExecutionException(
                                            "IOException while generating wrapper script",
                                            ioe);
         }
 
       if (windows)
         {
           Utils.createFile(getDstWindowsWrapperScriptFile(), "windows batch");
 
           gen.setCustomCodeWindows(dc.customCodeWindows);
 
           try
             {
               gen.generate(getDstWindowsWrapperScriptFile());
             }
           catch (IOException ioe)
             {
               throw new MojoExecutionException(
                                                "IOException while generating windows batch file",
                                                ioe);
             }
         }
 
       // Make the wrapper script executable.
       Utils.makeExecutable(getDstWrapperScriptFile(), "wrapper script");
 
     }
 
     public String getAotPackageName()
     {
       if (aotPackageName == null)
         aotPackageName = Utils.gcjise(artifactId, dc.getSection(),
                                       pm.isDebianNaming());
 
       return aotPackageName;
     }
 
     public File getAotPkgDir()
     {
       if (aotPkgDir == null)
         aotPkgDir = new File(getTempRoot(), aotPackageName + "-"
                                             + getPackageVersion());
 
       return aotPkgDir;
     }
 
     public String getArtifactId()
     {
       return artifactId;
     }
 
     public File getBasePkgDir()
     {
       if (basePkgDir == null)
         basePkgDir = new File(getTempRoot(), getPackageName() + "-"
                                              + getPackageVersion());
 
       return basePkgDir;
     }
 
     public File getDstArtifactFile()
     {
       if (dstArtifactFile == null)
         dstArtifactFile = new File(getBasePkgDir(),
                                    getTargetArtifactFile().toString());
 
       return dstArtifactFile;
     }
 
     public File getDstAuxDir()
     {
       return dstAuxDir;
     }
 
     public File getDstBinDir()
     {
       if (dstBinDir == null)
         dstBinDir = new File(getBasePkgDir(), getTargetBinDir().toString());
 
       return dstBinDir;
     }
 
     public File getDstBundledJarDir()
     {
       if (dstBundledJarDir == null)
         dstBundledJarDir = new File(basePkgDir,
                                     getTargetBundledJarDir().toString());
 
       return dstBundledJarDir;
     }
 
     public File getDstDataDir()
     {
       if (dstDataDir == null)
         dstDataDir = new File(getBasePkgDir(), getTargetDataDir().toString());
 
       return dstDataDir;
     }
 
     public File getDstDatarootDir()
     {
       if (dstDatarootDir == null)
         dstDatarootDir = new File(getBasePkgDir(),
                                   getTargetDatarootDir().toString());
 
       return dstDatarootDir;
     }
 
     public File getDstJNIDir()
     {
       if (dstJNIDir == null)
         dstJNIDir = new File(getBasePkgDir(), getTargetJNIDir().toString());
 
       return dstJNIDir;
     }
 
     public File getDstRoot()
     {
       if (dstRoot == null)
         dstRoot = new File(getBasePkgDir(), getTargetRoot().toString());
 
       return dstRoot;
     }
 
     public File getDstScriptDir()
     {
       if (dstScriptDir == null)
         throw new UnsupportedOperationException("This dstScriptDir property has to be provided explicitly in advance!");
 
       return dstScriptDir;
     }
 
     public File getDstStarterDir()
     {
       if (dstStarterDir == null)
         dstStarterDir = new File(getBasePkgDir(),
                                  getTargetStarterDir().toString());
 
       return dstStarterDir;
     }
 
     public File getDstSysconfDir()
     {
       if (dstSysconfDir == null)
         dstSysconfDir = new File(getBasePkgDir(),
                                  getTargetSysconfDir().toString());
 
       return dstSysconfDir;
     }
 
     public File getDstWindowsWrapperScriptFile()
     {
       if (dstWindowsWrapperScriptFile == null)
         dstWindowsWrapperScriptFile = new File(
                                                getDstWrapperScriptFile().getAbsolutePath()
                                                    + ".bat");
 
       return dstWindowsWrapperScriptFile;
     }
 
     public File getDstWrapperScriptFile()
     {
       if (dstWrapperScriptFile == null)
         // Use the provided wrapper script name or the default.
         dstWrapperScriptFile = new File(getBasePkgDir(),
                                         getTargetWrapperScriptFile().toString());
 
       return dstWrapperScriptFile;
     }
 
     public String getJavaExec()
     {
       return javaExec;
     }
     
     public String get7ZipExec()
     {
       return _7zipExec;
     }
 
     public File getOutputDirectory()
     {
       return outputDirectory;
     }
 
     public String getPackageName()
     {
       if (packageName == null)
         packageName = Utils.createPackageName(artifactId, dc.getSection(),
                                               pm.isDebianNaming());
 
       return packageName;
     }
 
     public String getPackageVersion()
     {
       if (packageVersion == null)
         packageVersion = fixVersion(version) + "-0" + sanitizePackageVersion(dc.chosenTarget)
                          + (dc.revision.length() == 0 ? "" : "-" + dc.revision);
 
       return packageVersion;
     }
 
     public String getProjectDescription()
     {
       return project.getDescription();
     }
 
     public String getProjectUrl()
     {
       return project.getUrl();
     }
 
     public File getSrcArtifactFile()
     {
       if (srcArtifactFile == null)
         srcArtifactFile = new File(outputDirectory.getPath(), 
         		finalName + "." + project.getPackaging());
       
       return srcArtifactFile;
     }
 
     public File getSrcAuxFilesDir()
     {
       return (dc.srcAuxFilesDir.length() == 0) ? new File(project.getBasedir(),
                                                           DEFAULT_SRC_AUXFILESDIR)
                                               : new File(project.getBasedir(),
                                                          dc.srcAuxFilesDir);
     }
 
     public File getSrcDataFilesDir()
     {
       return (dc.srcDataFilesDir.length() == 0) ? getSrcAuxFilesDir()
                                                : new File(project.getBasedir(),
                                                           dc.srcDataFilesDir);
     }
 
     public File getSrcDatarootFilesDir()
     {
       return (dc.srcDatarootFilesDir.length() == 0) ? getSrcAuxFilesDir()
                                                    : new File(
                                                               project.getBasedir(),
                                                               dc.srcDatarootFilesDir);
     }
 
     /**
      * Returns the directory containing the izpack helper files. If not
      * specified otherwise this directory is identical to the source directory
      * of the aux files.
      * 
      * @return
      */
     public File getSrcIzPackFilesDir()
     {
       return (dc.srcIzPackFilesDir.length() == 0 ? getSrcAuxFilesDir()
                                                 : new File(
                                                            project.getBasedir(),
                                                            dc.srcIzPackFilesDir));
     }
 
     public File getSrcJarFilesDir()
     {
       return (dc.srcJarFilesDir.length() == 0) ? getSrcAuxFilesDir()
                                               : new File(project.getBasedir(),
                                                          dc.srcJarFilesDir);
     }
 
     public File getSrcJNIFilesDir()
     {
       return (dc.srcJNIFilesDir.length() == 0) ? getSrcAuxFilesDir()
                                               : new File(project.getBasedir(),
                                                          dc.srcJNIFilesDir);
     }
 
     public File getSrcSysconfFilesDir()
     {
       return (dc.srcSysconfFilesDir.length() == 0) ? getSrcAuxFilesDir()
                                                   : new File(
                                                              project.getBasedir(),
                                                              dc.srcSysconfFilesDir);
     }
 
     public File getTargetArtifactFile()
     {
       if (targetArtifactFile == null)
         targetArtifactFile = new File(
                                       (dc.isBundleAll() || pm.hasNoPackages() ? getTargetBundledJarDir()
                                                                              : new File(
                                                                                         pm.getDefaultJarPath())),
                                       artifactId + "." + project.getPackaging());
 
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
     public File getTargetBinDir()
     {
       if (targetBinDir == null)
         targetBinDir = (dc.bindir.length() == 0 ? new File(
                                                            getTargetRoot(),
                                                            pm.getDefaultBinPath())
                                                : new File(dc.bindir));
 
       return targetBinDir;
     }
 
     public File getTargetBundledJarDir()
     {
       if (targetBundledJarDir == null)
         targetBundledJarDir = (dc.bundledJarDir.length() == 0 ? new File(
                                                                          getTargetRoot(),
                                                                          new File(
                                                                                   pm.getDefaultJarPath(),
                                                                                   artifactId).toString())
                                                              : new File(
                                                                         dc.bundledJarDir));
 
       return targetBundledJarDir;
     }
 
     public File getTargetDataDir()
     {
       if (targetDataDir == null)
         targetDataDir = (dc.datadir.length() == 0 ? new File(
                                                              getTargetDatarootDir(),
                                                              project.getName())
                                                  : new File(dc.datadir));
 
       return targetDataDir;
     }
 
     public File getTargetDatarootDir()
     {
       if (targetDatarootDir == null)
         targetDatarootDir = (dc.datarootdir.length() == 0 ? new File(
                                                                      getTargetRoot(),
                                                                      "usr/share")
                                                          : new File(
                                                                     dc.datarootdir));
 
       return targetDatarootDir;
     }
 
     /**
      * Returns the directory to which the JNI-files should be copied.
      * Consists of the target-root-directory and the _first_ directory specified
      * in the defaultJNIPath-configuration-parameter.
      * Example: If the defaultJNIPath is "/usr/lib/jni:/usr/lib" the target-jni-directory
      * is "/usr/lib/jni".
      * 
      * @return
      */
     public File getTargetJNIDir()
     {
       if (targetJNIDir == null)
         targetJNIDir = new File(getTargetRoot(), pm.getDefaultJNIPath().split(":")[0]);
 
       return targetJNIDir;
     }
     
     public File getTargetLibraryPath() {
     	if(targetLibraryPath == null)
     		targetLibraryPath = new File(getTargetRoot(), pm.getDefaultJNIPath());
     	
     	return targetLibraryPath;
     }
 
     public File getTargetRoot()
     {
       if (targetRoot == null)
         targetRoot = new File(dc.prefix);
 
       return targetRoot;
     }
 
     public File getTargetStarterDir()
     {
       if (targetStarterDir == null)
         targetStarterDir = new File(getTargetBundledJarDir(), "_starter");
 
       return targetStarterDir;
     }
 
     public File getTargetSysconfDir()
     {
       if (targetSysconfDir == null)
         targetSysconfDir = (dc.sysconfdir.length() == 0 ? new File(
                                                                    getTargetRoot(),
                                                                    "etc")
                                                        : new File(dc.sysconfdir));
 
       return targetSysconfDir;
     }
 
     public File getTargetWrapperScriptFile()
     {
       if (targetWrapperScriptFile == null)
         targetWrapperScriptFile = new File(
                                            getTargetBinDir(),
                                            (dc.wrapperScriptName != null ? dc.wrapperScriptName
                                                                         : artifactId));
 
       return targetWrapperScriptFile;
     }
 
     public File getTempRoot()
     {
       if (tempRoot == null)
         tempRoot = new File(buildDir, pm.getPackaging() + "-tmp");
 
       return tempRoot;
     }
 
     public void prepareAotDirectories() throws MojoExecutionException
     {
       prepareDirectories(getLog(), tempRoot, aotPkgDir, null);
     }
 
     public void prepareInitialDirectories() throws MojoExecutionException
     {
       prepareDirectories(getLog(), tempRoot, basePkgDir, dstJNIDir);
     }
 
     public void setAotPackageName(String aotPackageName)
     {
       this.aotPackageName = aotPackageName;
     }
 
     public void setAotPkgDir(File aotPkgDir)
     {
       this.aotPkgDir = aotPkgDir;
     }
 
     public void setBasePkgDir(File basePkgDir)
     {
       this.basePkgDir = basePkgDir;
     }
 
     public void setDstArtifactFile(File dstArtifactFile)
     {
       this.dstArtifactFile = dstArtifactFile;
     }
 
     public void setDstAuxDir(File dstAuxFileDir)
     {
       this.dstAuxDir = dstAuxFileDir;
     }
 
     public void setDstBinDir(File dstBinDir)
     {
       this.dstBinDir = dstBinDir;
     }
 
     public void setDstBundledJarDir(File dstBundledArtifactsDir)
     {
       this.dstBundledJarDir = dstBundledArtifactsDir;
     }
 
     public void setDstDataDir(File dstDataDir)
     {
       this.dstDataDir = dstDataDir;
     }
 
     public void setDstDatarootDir(File dstDatarootDir)
     {
       this.dstDatarootDir = dstDatarootDir;
     }
 
     public void setDstJNIDir(File dstJNIDir)
     {
       this.dstJNIDir = dstJNIDir;
     }
 
     public void setDstRoot(File dstRoot)
     {
 
       this.dstRoot = dstRoot;
     }
 
     public void setDstScriptDir(File dstScriptDir)
     {
       this.dstScriptDir = dstScriptDir;
     }
 
     public void setDstStarterDir(File dstStarterDir)
     {
       this.dstStarterDir = dstStarterDir;
     }
 
     public void setDstSysconfDir(File dstSysconfDir)
     {
       this.dstSysconfDir = dstSysconfDir;
     }
 
     public void setDstWindowsWrapperScriptFile(File windowsWrapperScriptFile)
     {
       this.dstWindowsWrapperScriptFile = windowsWrapperScriptFile;
     }
 
     public void setDstWrapperScriptFile(File wrapperScriptFile)
     {
       this.dstWrapperScriptFile = wrapperScriptFile;
     }
 
     public void setPackageName(String packageName)
     {
       this.packageName = packageName;
     }
 
     public void setPackageVersion(String packageVersion)
     {
       this.packageVersion = packageVersion;
     }
 
     public void setTargetArtifactFile(File targetArtifactFile)
     {
       this.targetArtifactFile = targetArtifactFile;
     }
 
     public void setTargetBinDir(File targetBinDir)
     {
       this.targetBinDir = targetBinDir;
     }
 
     public void setTargetBundledJarDir(File targetJarDir)
     {
       this.targetBundledJarDir = targetJarDir;
     }
 
     public void setTargetDataDir(File targetDataDir)
     {
       this.targetDataDir = targetDataDir;
     }
 
     public void setTargetDatarootDir(File targetDatarootDir)
     {
       this.targetDatarootDir = targetDatarootDir;
     }
 
     public void setTargetJNIDir(File targetJNIDir)
     {
       this.targetJNIDir = targetJNIDir;
     }
 
     public void setTargetRoot(File targetRoot)
     {
       this.targetRoot = targetRoot;
     }
 
     public void setTargetStarterDir(File targetStarterDir)
     {
       this.targetStarterDir = targetStarterDir;
     }
 
     public void setTargetSysconfDir(File targetSysconfDir)
     {
       this.targetSysconfDir = targetSysconfDir;
     }
 
     public void setTargetWrapperScriptFile(File targetWrapperScriptFile)
     {
       this.targetWrapperScriptFile = targetWrapperScriptFile;
     }
 
     public void setTempRoot(File tempRoot)
     {
       this.tempRoot = tempRoot;
     }
   }
 
   static final String DEFAULT_SRC_AUXFILESDIR = "src/main/auxfiles";
 
   private TargetConfiguration dc;
 
   /**
    * @parameter
    * @required
    */
   protected TargetConfiguration defaults;
 
   /**
    * @parameter
    */
   protected List<TargetConfiguration> targetConfigurations;
 
   private PackageMap pm;
 
   /**
    * Validates arguments and test tools.
    * 
    * @throws MojoExecutionException
    */
   void checkEnvironment(Log l) throws MojoExecutionException
   {
     l.info("distribution             : " + dc.chosenDistro);
     l.info("package system           : " + pm.getPackaging());
     l.info("default package map      : "
            + (defaultPackageMapURL == null ? "built-in"
                                           : defaultPackageMapURL.toString()));
     l.info("auxiliary package map    : "
            + (auxPackageMapURL == null ? "no" : auxPackageMapURL.toString()));
     l.info("type of project          : "
            + ((dc.getMainClass() != null) ? "application" : "library"));
     l.info("section                  : " + dc.getSection());
     l.info("bundle all dependencies  : " + ((dc.isBundleAll()) ? "yes" : "no"));
     l.info("ahead of time compilation: " + ((dc.isAotCompile()) ? "yes" : "no"));
     l.info("custom jar libraries     : "
             + ((dc.jarFiles.isEmpty()) ? "<none>"
                                       : String.valueOf(dc.jarFiles.size())));
     l.info("JNI libraries            : "
            + ((dc.jniFiles.isEmpty()) ? "<none>"
                                      : String.valueOf(dc.jniFiles.size())));
     l.info("auxiliary file source dir: "
            + (dc.srcAuxFilesDir.length() == 0 ? (DEFAULT_SRC_AUXFILESDIR + " (default)")
                                              : dc.srcAuxFilesDir));
     l.info("auxiliary files          : "
            + ((dc.auxFiles.isEmpty()) ? "<none>"
                                      : String.valueOf(dc.auxFiles.size())));
     l.info("prefix                   : "
            + (dc.prefix.length() == 1 ? "/ (default)" : dc.prefix));
     l.info("sysconf files source dir : "
            + (dc.srcSysconfFilesDir.length() == 0 ? (DEFAULT_SRC_AUXFILESDIR + " (default)")
                                                  : dc.srcSysconfFilesDir));
     l.info("sysconfdir               : "
            + (dc.sysconfdir.length() == 0 ? "(default)" : dc.sysconfdir));
     l.info("dataroot files source dir: "
            + (dc.srcDatarootFilesDir.length() == 0 ? (DEFAULT_SRC_AUXFILESDIR + " (default)")
                                                   : dc.srcDatarootFilesDir));
     l.info("dataroot                 : "
            + (dc.datarootdir.length() == 0 ? "(default)" : dc.datarootdir));
     l.info("data files source dir    : "
            + (dc.srcDataFilesDir.length() == 0 ? (DEFAULT_SRC_AUXFILESDIR + " (default)")
                                               : dc.srcDataFilesDir));
     l.info("datadir                  : "
            + (dc.datadir.length() == 0 ? "(default)" : dc.datadir));
     l.info("bindir                   : "
            + (dc.bindir.length() == 0 ? "(default)" : dc.bindir));
 
     if (dc.chosenDistro == null)
       throw new MojoExecutionException("No distribution configured!");
 
     if (dc.isAotCompile())
       {
         l.info("aot compiler             : " + dc.getGcjExec());
         l.info("aot classmap generator   : " + dc.getGcjDbToolExec());
       }
 
     if (dc.getMainClass() == null)
       {
         if (! "libs".equals(dc.getSection()))
           throw new MojoExecutionException(
                                            "section has to be 'libs' if no main class is given.");
 
         if (dc.isBundleAll())
           throw new MojoExecutionException(
                                            "Bundling dependencies to a library makes no sense.");
       }
     else
       {
         if ("libs".equals(dc.getSection()))
           throw new MojoExecutionException(
                                            "Set a proper section if main class parameter is set.");
       }
 
     if (dc.isAotCompile())
       {
         AotCompileUtils.setGcjExecutable(dc.getGcjExec());
         AotCompileUtils.setGcjDbToolExecutable(dc.getGcjDbToolExec());
 
         AotCompileUtils.checkToolAvailability();
       }
   }
   
   protected final void writeScript(String item, File srcScriptFile, File dstScriptFile, Helper ph)
   throws MojoExecutionException
   {
     Utils.createFile(dstScriptFile, item + " file");
     // Write a #/bin/sh header
     
     Utils.makeExecutable(dstScriptFile, item + " file");
     
     try
     {
       PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(dstScriptFile)));
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
       writer.println("mainClass=\"" + dc.mainClass + "\"");
       writer.println("scriptType=\"" + item + "\"");
       writer.println();
       writer.println("distro=\"" + dc.chosenDistro + "\"");
       writer.println("distroLabel=\"" + pm.getDistroLabel() + "\"");
       writer.println("packaging=\"" + pm.getPackaging() + "\"");
       writer.println();
       writer.println("# What follows is the content script file " + srcScriptFile.getName());
       writer.println();
       
       // Now append the real script
       IOUtils.copy(new FileInputStream(srcScriptFile), writer);
       
       writer.close();
     }
     catch (IOException ioe)
     {
       throw new MojoExecutionException("IO error while writing the script file " + dstScriptFile, ioe);
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
   protected final Set createClasspathLine(final Log l,
                                           final File targetJarPath,
                                           final Path bcp,
                                           final Path cp,
                                           File targetArtifactFile)
       throws MojoExecutionException
   {
     final Set<Artifact> bundled = new HashSet<Artifact>();
 
     l.info("resolving dependency artifacts");
 
     Set dependencies = null;
     try
       {
         // Notice only compilation dependencies which are Jars.
         // Shared Libraries ("so") are filtered out because the
         // JNI dependency is solved by the system already.
         AndArtifactFilter andFilter = new AndArtifactFilter();
         andFilter.add(new ScopeArtifactFilter(Artifact.SCOPE_COMPILE));
         andFilter.add(new TypeArtifactFilter("jar"));
 
         dependencies = findArtifacts(andFilter);
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
         if (dc.isBundleAll())
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
                 sb.append(pm.getDefaultJarPath());
                 sb.append("/");
               }
 
             sb.append(fileName);
             
             b.append(sb.toString());
           }
       }
 
     };
 
     pm.iterateDependencyArtifacts(l, dependencies, v, true);
     
     // Add the custom jar files to the classpath
     for (Iterator<JarFile> ite = dc.jarFiles.iterator(); ite.hasNext();)
     {
     	AuxFile auxFile = ite.next();
     	
     	cp.append(targetJarPath.toString()
     			  + "/" + new File(auxFile.from).getName());
     }
     
     // Add the project's own artifact at last. This way we can
     // save the deletion of the colon added in the loops above.
     cp.append(targetArtifactFile.toString());
 
     return bundled;
   }
 
   /**
    * Investigates the project's runtime dependencies and creates a dependency
    * line suitable for the control file from them.
    * 
    * @return
    */
   protected final String createDependencyLine() throws MojoExecutionException
   {
     String defaults = pm.getDefaultDependencyLine();
     StringBuffer manualDeps = new StringBuffer();
     Iterator ite = dc.manualDependencies.iterator();
     while (ite.hasNext())
       {
         String dep = (String) ite.next();
 
         manualDeps.append(dep);
         manualDeps.append(", ");
       }
 
     if (manualDeps.length() >= 2)
       manualDeps.delete(manualDeps.length() - 2, manualDeps.length());
 
     // If all dependencies should be bundled the package will only
     // need the default Java dependencies of the system and the remainder
     // of the method can be skipped.
     if (dc.isBundleAll())
       return Utils.joinDependencyLines(defaults, manualDeps.toString());
 
     Set runtimeDeps = null;
 
     try
       {
         AndArtifactFilter andFilter = new AndArtifactFilter();
         andFilter.add(new ScopeArtifactFilter(Artifact.SCOPE_COMPILE));
         andFilter.add(new TypeArtifactFilter("jar"));
 
         runtimeDeps = findArtifacts(andFilter);
 
         andFilter = new AndArtifactFilter();
         andFilter.add(new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME));
         andFilter.add(new TypeArtifactFilter("jar"));
 
         runtimeDeps.addAll(findArtifacts(andFilter));
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
     final Log l = getLog();
 
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
 
     pm.iterateDependencyArtifacts(l, runtimeDeps, v, true);
 
     return Utils.joinDependencyLines(line.toString(), manualDeps.toString());
   }
   
   /**
    * Creates the "Recommends"-line for the package control file
    * 
    * @return recommends line
    * @throws MojoExecutionException
    */
   protected final String createRecommendsLine() throws MojoExecutionException
   {
 	  return createPackageLine(dc.recommends);
   }
   
   /**
    * Creates the "Suggests"-line for the package control file
    * 
    * @return suggests line
    * @throws MojoExecutionException
    */
   protected final String createSuggestsLine() throws MojoExecutionException
   {
 	  return createPackageLine(dc.suggests);
   }
   
   /**
    * Creates the "Provides"-line for the package control file
    * 
    * @return provides line
    * @throws MojoExecutionException
    */
   protected final String createProvidesLine() throws MojoExecutionException
   {
 	  return createPackageLine(dc.provides);
   }
   
   /**
    * Creates the "Conflicts"-line for the package control file
    * 
    * @return conflicts line
    * @throws MojoExecutionException
    */
   protected final String createConflictsLine() throws MojoExecutionException
   {
 	  return createPackageLine(dc.conflicts);
   }
   
   /**
    * Creates the "Replaces"-line for the package control file
    * 
    * @return suggests line
    * @throws MojoExecutionException
    */
   protected final String createReplacesLine() throws MojoExecutionException
   {
 	  return createPackageLine(dc.replaces);
   }
   
   protected final String createPackageLine(List packageDescriptors)
   {
 	  if(packageDescriptors == null || packageDescriptors.isEmpty())
 		  return null;
 	  
 	  StringBuffer packageLine = new StringBuffer();
 	  Iterator ite = packageDescriptors.iterator();
 	  while (ite.hasNext())
 	  {
 		  String packageDescriptor = (String) ite.next();
 
 		  packageLine.append(packageDescriptor);
 		  packageLine.append(", ");
 	  }
 
 	  // Remove last ", "
 	  if (packageLine.length() >= 2)
 		  packageLine.delete(packageLine.length() - 2, packageLine.length());
 	  
 	  return packageLine.toString();
   }
 
   public void execute() throws MojoExecutionException, MojoFailureException
   {
 	String t = (target != null) ? target : defaultTarget;
     String d = (distro != null) ? distro : defaultDistro;
     
     // Generate merged distro configuration.
     dc = getMergedConfiguration(t, d);
     dc.chosenDistro = d;
     dc.chosenTarget = t;
 
     // Retrieve package map for chosen distro.
     pm = new PackageMap(defaultPackageMapURL, auxPackageMapURL, d,
                         dc.bundleDependencies);
 
     Helper ph = new Helper();
 
     String packaging = pm.getPackaging();
     if (packaging == null)
       throw new MojoExecutionException(
                                        "Package maps document set no packaging for distro: "
                                            + dc.chosenDistro);
     
     // Store configuration in plugin-context for later use by signer- and deploy-goal
     getPluginContext().put("dc", dc);
     getPluginContext().put("pm", pm);
 
     // Create packager according to the chosen packaging type.
     Packager packager;
     if ("deb".equals(packaging))
       packager = new DebPackager();
     else if ("ipk".equals(packaging))
       packager = new IpkPackager();
     else if ("izpack".equals(packaging))
       packager = new IzPackPackager();
     else
       throw new MojoExecutionException("Unsupported packaging type: "
                                        + packaging);
 
     checkEnvironment(getLog());
 
     packager.checkEnvironment(getLog(), ph, dc);
 
     packager.execute(getLog(), ph, dc, pm);
   }
 
   /**
    * Takes the default configuration and the custom one into account and creates
    * a merged one.
    * 
    * @param distro
    * @return
    */
   private TargetConfiguration getMergedConfiguration(String target, String distro)
       throws MojoExecutionException
   {
     Iterator<TargetConfiguration> ite = targetConfigurations.iterator();
     while (ite.hasNext())
       {
         TargetConfiguration dc = ite.next();
         
         // The target configuration should be for the requested target.
         if (!dc.target.equals(target))
         	continue;
         
         TargetConfiguration merged = getMergedConfiguration(dc.parent, distro);
 
         // Checks whether this targetconfiguration supports
         // the wanted distro.
        if (dc.distros.contains(distro))
           {
             // Stores the chosen distro in the configuration for later use.
             dc.chosenDistro = distro;
 
             // Returns a configuration that is merged with
             // the default configuration-
             return dc.merge(merged);
           }
       }
 
     // No special config for chosen distro available.
     return new TargetConfiguration().merge(defaults);
   }
 
   /**
    * Creates the temporary and package base directory.
    * 
    * @param l
    * @param basePkgDir
    * @throws MojoExecutionException
    */
   final void prepareDirectories(Log l, File tempRoot, File basePkgDir,
                                 File jniDir) throws MojoExecutionException
   {
     l.info("creating temporary directory: " + tempRoot.getAbsolutePath());
 
     if (! tempRoot.exists() && ! tempRoot.mkdirs())
       throw new MojoExecutionException("Could not create temporary directory.");
 
     l.info("cleaning the temporary directory");
     try
       {
         FileUtils.cleanDirectory(tempRoot);
       }
     catch (IOException ioe)
       {
         throw new MojoExecutionException(
                                          "Exception while cleaning temporary directory.",
                                          ioe);
       }
 
     l.info("creating package directory: " + basePkgDir.getAbsolutePath());
     if (! basePkgDir.mkdirs())
       throw new MojoExecutionException("Could not create package directory.");
 
     if (jniDir != null && dc.jniFiles != null && dc.jniFiles.size() > 0)
       {
         if (! jniDir.mkdirs())
           throw new MojoExecutionException("Could not create JNI directory.");
       }
 
   }
 }
